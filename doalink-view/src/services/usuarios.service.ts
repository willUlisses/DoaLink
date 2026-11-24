import { api } from './api.ts'
import type { ContatoResponse } from '../types/endereco.ts'
import type {
  ContatoRequest,
  UsuarioAtualizarRequest,
  UsuarioDetalheResponse,
} from '../types/usuario.ts'

export async function obterMeuPerfil(): Promise<UsuarioDetalheResponse> {
  const { data } = await api.get<UsuarioDetalheResponse>('/usuarios/me')
  return data
}

export async function atualizarMeuPerfil(
  payload: UsuarioAtualizarRequest,
): Promise<UsuarioDetalheResponse> {
  const { data } = await api.put<UsuarioDetalheResponse>('/usuarios/me', payload)
  return data
}

export async function obterMeuContato(): Promise<ContatoResponse> {
  const { data } = await api.get<ContatoResponse>('/usuarios/me/contato')
  return data
}

export async function atualizarMeuContato(payload: ContatoRequest): Promise<ContatoResponse> {
  const { data } = await api.put<ContatoResponse>('/usuarios/me/contato', payload)
  return data
}
