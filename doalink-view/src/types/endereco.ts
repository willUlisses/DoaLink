export interface EnderecoRequest {
  logradouro: string
  numero?: string
  complemento?: string
  bairro: string
  cidade: string
  estado: string
  cep: string
}

export interface EnderecoResponse {
  logradouro: string | null
  numero: string | null
  complemento: string | null
  bairro: string | null
  cidade: string | null
  estado: string | null
  cep: string | null
}

export interface ContatoResponse {
  nome: string
  email: string
  telefone: string | null
}

