import type { EnderecoRequest, EnderecoResponse } from './endereco.ts'
import type { TipoPessoa, TipoUsuario } from './enums.ts'

export interface UsuarioDetalheResponse {
  id: number
  nome: string
  documento: string
  email: string
  emailContato: string
  telefone: string | null
  tipoUsuario: TipoUsuario
  tipoPessoa: TipoPessoa
  emailVerificado: boolean
  endereco: EnderecoResponse | null
}

export interface UsuarioAtualizarRequest {
  endereco?: EnderecoRequest
}

export interface ContatoRequest {
  email?: string
  telefone?: string | null
}
