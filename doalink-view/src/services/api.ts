import axios, { type AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import type { ErrorCode, ErrorResponsePayload, NormalizedApiError } from '../types/api.ts'

export interface RetriableRequestConfig extends InternalAxiosRequestConfig {
  _retried?: boolean
  _skipAuthRefresh?: boolean
}

const AUTH_EXCLUDED_PATHS = [
  '/auth/refresh',
  '/auth/login',
  '/auth/logout',
  '/auth/doadores/cadastro',
  '/auth/transportadores/cadastro',
  '/auth/receptores/cadastro',
  '/auth/verificar-email',
  '/auth/reenviar-codigo',
  '/auth/recuperar-senha',
  '/auth/recuperar-senha/validar',
  '/auth/redefinir-senha',
]

interface AuthInterceptorHandlers {
  onSessionExpired?: () => void
  onEmailNotVerified?: (message: string) => void
}

let handlers: AuthInterceptorHandlers = {}
let refreshPromise: Promise<void> | null = null

export function configureApiAuthHandlers(nextHandlers: AuthInterceptorHandlers): void {
  handlers = nextHandlers
}

export function resetRefreshStateForTests(): void {
  refreshPromise = null
}

function getBaseUrl(): string {
  const configured = import.meta.env.API_BASE_URL
  if (typeof configured === 'string' && configured.trim() !== '') {
    return configured.trim().replace(/\/+$/, '')
  }
  return 'http://localhost:8080'
}

function isAuthExcludedRequest(url: string | undefined): boolean {
  if (!url) return false
  const path = url.split('?')[0]
  return AUTH_EXCLUDED_PATHS.some((excluded) => path === excluded || path.endsWith(excluded))
}

function parseFieldErrors(status: number, message: string): Record<string, string> {
  const fieldErrors: Record<string, string> = {}

  if (status === 400 && message.includes(':')) {
    const segments = message.split(';')
    for (const segment of segments) {
      const idx = segment.indexOf(':')
      if (idx > 0) {
        const rawField = segment.slice(0, idx).trim()
        const msg = segment.slice(idx + 1).trim()
        if (rawField && msg && !rawField.includes(' ')) {
          const normalizedField = rawField.replace(/\[(\d+)\]/g, '.$1')
          fieldErrors[normalizedField] = msg
        }
      }
    }
  }

  if (status === 409) {
    const lower = message.toLowerCase()
    if (lower.includes('e-mail') || lower.includes('email')) {
      fieldErrors.email = message
    } else if (lower.includes('documento') || lower.includes('cpf') || lower.includes('cnpj')) {
      fieldErrors.documento = message
    }
  }

  return fieldErrors
}

function defaultMessageForStatus(status: number): string {
  switch (status) {
    case 400:
      return 'Verifique os dados informados e tente novamente.'
    case 401:
      return 'Sua sessão expirou ou suas credenciais são inválidas.'
    case 403:
      return 'Você não tem permissão para realizar esta ação.'
    case 404:
      return 'O registro solicitado não foi encontrado.'
    case 409:
      return 'Não foi possível concluir a operação devido a um conflito de estado ou dados já cadastrados.'
    default:
      return status >= 500
        ? 'O servidor enfrentou uma instabilidade momentânea. Tente novamente em instantes.'
        : 'Não foi possível concluir a requisição.'
  }
}

function codeForStatus(status: number, message: string): ErrorCode {
  if (status === 400) return 'VALIDATION_ERROR'
  if (status === 401) return 'UNAUTHORIZED'
  if (status === 403) {
    return message.toLowerCase().includes('verificad') ? 'EMAIL_NOT_VERIFIED' : 'FORBIDDEN'
  }
  if (status === 404) return 'NOT_FOUND'
  if (status === 409) return 'CONFLICT'
  return 'UNKNOWN_ERROR'
}

export function normalizeApiError(error: unknown): NormalizedApiError {
  if (isNormalizedApiError(error)) {
    return error
  }

  if (!axios.isAxiosError(error)) {
    return {
      status: 0,
      code: 'UNKNOWN_ERROR',
      message: 'Ocorreu um erro inesperado na comunicação com o sistema.',
      fieldErrors: {},
    }
  }

  const axiosError = error as AxiosError<ErrorResponsePayload>
  if (!axiosError.response) {
    return {
      status: 0,
      code: 'NETWORK_ERROR',
      message: 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.',
      fieldErrors: {},
    }
  }

  const { status, data } = axiosError.response
  const rawMessage =
    data && typeof data === 'object' && typeof data.message === 'string' && data.message.trim() !== ''
      ? data.message.trim()
      : ''

  const message =
    status >= 500
      ? defaultMessageForStatus(status)
      : rawMessage || defaultMessageForStatus(status)

  const code = codeForStatus(status, rawMessage || message)
  const fieldErrors = parseFieldErrors(status, rawMessage)

  return {
    status,
    code,
    message,
    fieldErrors,
  }
}

export function isNormalizedApiError(value: unknown): value is NormalizedApiError {
  if (typeof value !== 'object' || value === null) return false
  const candidate = value as Partial<NormalizedApiError>
  return (
    typeof candidate.status === 'number' &&
    typeof candidate.code === 'string' &&
    typeof candidate.message === 'string' &&
    typeof candidate.fieldErrors === 'object' &&
    candidate.fieldErrors !== null
  )
}

async function performSerializedRefresh(client: AxiosInstance): Promise<void> {
  if (!refreshPromise) {
    refreshPromise = client
      .post('/auth/refresh', undefined, {
        _skipAuthRefresh: true,
      } as RetriableRequestConfig)
      .then(() => undefined)
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

export function createApiClient(): AxiosInstance {
  const client = axios.create({
    baseURL: getBaseUrl(),
    withCredentials: true,
    timeout: 15000,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  client.interceptors.response.use(
    (response) => response,
    async (error: unknown) => {
      if (!axios.isAxiosError(error)) {
        return Promise.reject(normalizeApiError(error))
      }

      const config = error.config as RetriableRequestConfig | undefined
      const status = error.response?.status

      if (
        status === 401 &&
        config &&
        !config._retried &&
        !config._skipAuthRefresh &&
        !isAuthExcludedRequest(config.url)
      ) {
        config._retried = true
        try {
          await performSerializedRefresh(client)
          return await client.request(config)
        } catch (refreshError) {
          handlers.onSessionExpired?.()
          return Promise.reject(normalizeApiError(refreshError))
        }
      }

      const normalized = normalizeApiError(error)

      if (normalized.code === 'EMAIL_NOT_VERIFIED') {
        handlers.onEmailNotVerified?.(normalized.message)
      }

      return Promise.reject(normalized)
    },
  )

  return client
}

export const api = createApiClient()

