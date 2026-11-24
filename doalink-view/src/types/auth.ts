import type { EnderecoRequest } from './endereco.ts'
import type { TipoUsuario } from './enums.ts'

export interface UsuarioResponse {
  id: number
  nome: string
  email: string
  tipoUsuario: TipoUsuario
}

export interface CadastroComumRequest {
  cpf?: string
  cnpj?: string
  email: string
  nome: string
  senha: string
}

export interface CadastroReceptorRequest {
  cnpj: string
  nome: string
  email: string
  senha: string
  endereco: EnderecoRequest
}

export interface LoginRequest {
  email: string
  senha: string
}

export interface VerificarEmailRequest {
  email: string
  codigo: string
}

export interface ReenviarCodigoRequest {
  email: string
}

export interface SolicitarRecuperacaoSenhaRequest {
  email: string
}

export interface RedefinirSenhaRequest {
  token: string
  novaSenha: string
}

export interface ValidarTokenResponse {
  valido: boolean
}

