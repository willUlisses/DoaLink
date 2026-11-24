import { api, type RetriableRequestConfig } from './api.ts'
import type { MensagemResponse } from '../types/api.ts'
import type {
  CadastroComumRequest,
  CadastroReceptorRequest,
  LoginRequest,
  RedefinirSenhaRequest,
  ReenviarCodigoRequest,
  SolicitarRecuperacaoSenhaRequest,
  UsuarioResponse,
  ValidarTokenResponse,
  VerificarEmailRequest,
} from '../types/auth.ts'

export async function cadastrarDoador(payload: CadastroComumRequest): Promise<UsuarioResponse> {
  const { data } = await api.post<UsuarioResponse>('/auth/doadores/cadastro', payload)
  return data
}

export async function cadastrarTransportador(
  payload: CadastroComumRequest,
): Promise<UsuarioResponse> {
  const { data } = await api.post<UsuarioResponse>('/auth/transportadores/cadastro', payload)
  return data
}

export async function cadastrarReceptor(
  payload: CadastroReceptorRequest,
): Promise<UsuarioResponse> {
  const { data } = await api.post<UsuarioResponse>('/auth/receptores/cadastro', payload)
  return data
}

export async function verificarEmail(payload: VerificarEmailRequest): Promise<UsuarioResponse> {
  const { data } = await api.post<UsuarioResponse>('/auth/verificar-email', payload)
  return data
}

export async function reenviarCodigo(payload: ReenviarCodigoRequest): Promise<MensagemResponse> {
  const { data } = await api.post<MensagemResponse>('/auth/reenviar-codigo', payload)
  return data
}

export async function login(payload: LoginRequest): Promise<UsuarioResponse> {
  const { data } = await api.post<UsuarioResponse>('/auth/login', payload)
  return data
}

export async function refreshSession(): Promise<UsuarioResponse> {
  const { data } = await api.post<UsuarioResponse>('/auth/refresh', undefined, {
    _skipAuthRefresh: true,
  } as RetriableRequestConfig)
  return data
}

export async function logout(): Promise<void> {
  await api.post('/auth/logout', undefined, {
    _skipAuthRefresh: true,
  } as RetriableRequestConfig)
}

export async function solicitarRecuperacaoSenha(
  payload: SolicitarRecuperacaoSenhaRequest,
): Promise<MensagemResponse> {
  const { data } = await api.post<MensagemResponse>('/auth/recuperar-senha', payload)
  return data
}

export async function validarTokenRecuperacao(token: string): Promise<ValidarTokenResponse> {
  const { data } = await api.get<ValidarTokenResponse>('/auth/recuperar-senha/validar', {
    params: { token },
  })
  return data
}

export async function redefinirSenha(
  payload: RedefinirSenhaRequest,
): Promise<MensagemResponse> {
  const { data } = await api.post<MensagemResponse>('/auth/redefinir-senha', payload)
  return data
}

