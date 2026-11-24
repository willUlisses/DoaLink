import { AxiosError, AxiosHeaders, type AxiosAdapter, type InternalAxiosRequestConfig } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  configureApiAuthHandlers,
  createApiClient,
  isNormalizedApiError,
  resetRefreshStateForTests,
} from '../api.ts'

function createAxiosHttpError(
  config: InternalAxiosRequestConfig,
  status: number,
  data?: unknown,
): AxiosError {
  return new AxiosError(
    `Request failed with status code ${status}`,
    AxiosError.ERR_BAD_REQUEST,
    config,
    undefined,
    {
      status,
      statusText: String(status),
      headers: new AxiosHeaders(),
      config,
      data,
    },
  )
}

describe('Cliente HTTP centralizado (api.ts)', () => {
  beforeEach(() => {
    resetRefreshStateForTests()
    configureApiAuthHandlers({})
  })

  it('configura withCredentials=true e normaliza erros 400, 409 e 500', async () => {
    const client = createApiClient()
    expect(client.defaults.withCredentials).toBe(true)

    const adapter: AxiosAdapter = async (config) => {
      if (config.url === '/erro-400') {
        throw createAxiosHttpError(config, 400, {
          status: 400,
          message: 'itens[0].quantidade: deve ser maior que zero; email: inválido',
        })
      }
      if (config.url === '/erro-409') {
        throw createAxiosHttpError(config, 409, {
          status: 409,
          message: 'E-mail já cadastrado',
        })
      }
      throw createAxiosHttpError(config, 500, {
        status: 500,
        message: 'NullPointerException internal trace',
      })
    }
    client.defaults.adapter = adapter

    await expect(client.get('/erro-400')).rejects.toMatchObject({
      status: 400,
      code: 'VALIDATION_ERROR',
      fieldErrors: {
        'itens.0.quantidade': 'deve ser maior que zero',
        email: 'inválido',
      },
    })

    await expect(client.get('/erro-409')).rejects.toMatchObject({
      status: 409,
      code: 'CONFLICT',
      fieldErrors: {
        email: 'E-mail já cadastrado',
      },
    })

    try {
      await client.get('/erro-500')
    } catch (err) {
      expect(isNormalizedApiError(err)).toBe(true)
      if (isNormalizedApiError(err)) {
        expect(err.status).toBe(500)
        expect(err.message).not.toContain('NullPointerException')
      }
    }
  })

  it('faz refresh único em 401 e repete a requisição original uma única vez', async () => {
    const client = createApiClient()
    let ofertaCalls = 0
    let refreshCalls = 0

    const adapter: AxiosAdapter = async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1
        return {
          status: 200,
          statusText: 'OK',
          headers: new AxiosHeaders(),
          config,
          data: { id: 1, nome: 'Ana', email: 'ana@doalink.org', tipoUsuario: 'DOADOR' },
        }
      }

      if (config.url === '/ofertas/minhas') {
        ofertaCalls += 1
        if (ofertaCalls === 1) {
          throw createAxiosHttpError(config, 401)
        }
        return {
          status: 200,
          statusText: 'OK',
          headers: new AxiosHeaders(),
          config,
          data: [{ id: 10 }],
        }
      }

      throw new Error(`Unexpected url ${config.url}`)
    }
    client.defaults.adapter = adapter

    const response = await client.get('/ofertas/minhas')
    expect(response.data).toEqual([{ id: 10 }])
    expect(refreshCalls).toBe(1)
    expect(ofertaCalls).toBe(2)
  })

  it('serializa refreshes concorrentes quando múltiplas requisições recebem 401 simultaneamente', async () => {
    const client = createApiClient()
    let refreshCalls = 0
    const attempts = new Map<string, number>()

    const adapter: AxiosAdapter = async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1
        await new Promise((resolve) => setTimeout(resolve, 25))
        return {
          status: 200,
          statusText: 'OK',
          headers: new AxiosHeaders(),
          config,
          data: { id: 1, nome: 'Ana', email: 'ana@doalink.org', tipoUsuario: 'DOADOR' },
        }
      }

      const key = config.url ?? ''
      const count = (attempts.get(key) ?? 0) + 1
      attempts.set(key, count)

      if (count === 1) {
        throw createAxiosHttpError(config, 401)
      }

      return {
        status: 200,
        statusText: 'OK',
        headers: new AxiosHeaders(),
        config,
        data: { ok: key },
      }
    }
    client.defaults.adapter = adapter

    const [r1, r2, r3] = await Promise.all([
      client.get('/a'),
      client.get('/b'),
      client.get('/c'),
    ])

    expect(refreshCalls).toBe(1)
    expect(r1.data).toEqual({ ok: '/a' })
    expect(r2.data).toEqual({ ok: '/b' })
    expect(r3.data).toEqual({ ok: '/c' })
  })

  it('chama onSessionExpired se o refresh falhar e não entra em loop', async () => {
    const client = createApiClient()
    const onSessionExpired = vi.fn()
    configureApiAuthHandlers({ onSessionExpired })

    let refreshCalls = 0
    const adapter: AxiosAdapter = async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1
      }
      throw createAxiosHttpError(config, 401)
    }
    client.defaults.adapter = adapter

    await expect(client.get('/ofertas/minhas')).rejects.toMatchObject({ status: 401 })
    expect(refreshCalls).toBe(1)
    expect(onSessionExpired).toHaveBeenCalledTimes(1)
  })

  it('não faz refresh em 403 nem em /auth/refresh ou /auth/login', async () => {
    const client = createApiClient()
    const onEmailNotVerified = vi.fn()
    configureApiAuthHandlers({ onEmailNotVerified })

    let refreshCalls = 0
    const adapter: AxiosAdapter = async (config) => {
      if (config.url === '/auth/refresh') {
        refreshCalls += 1
        throw createAxiosHttpError(config, 401)
      }
      if (config.url === '/auth/login') {
        throw createAxiosHttpError(config, 401, { message: 'E-mail ou senha inválidos' })
      }
      if (config.url === '/recurso-403-email') {
        throw createAxiosHttpError(config, 403, {
          status: 403,
          message: 'E-mail não verificado. Confirme o código.',
        })
      }
      throw createAxiosHttpError(config, 403)
    }
    client.defaults.adapter = adapter

    await expect(client.post('/auth/login', {})).rejects.toMatchObject({
      status: 401,
      code: 'UNAUTHORIZED',
    })
    await expect(client.post('/auth/refresh')).rejects.toMatchObject({ status: 401 })
    await expect(client.get('/recurso-403-email')).rejects.toMatchObject({
      status: 403,
      code: 'EMAIL_NOT_VERIFIED',
    })
    await expect(client.get('/recurso-403-role')).rejects.toMatchObject({
      status: 403,
      code: 'FORBIDDEN',
    })

    // Apenas a chamada explícita a /auth/refresh contou; nenhum refresh automático foi disparado
    expect(refreshCalls).toBe(1)
    expect(onEmailNotVerified).toHaveBeenCalledTimes(1)
  })
})

