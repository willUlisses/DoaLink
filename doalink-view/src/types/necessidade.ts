import type { ContatoResponse, EnderecoRequest, EnderecoResponse } from './endereco.ts'
import type { SituacaoNecessidade, TipoItemDoacao, TipoUnidade } from './enums.ts'

export interface ItemNecessidadeRequest {
  descricao?: string
  tipoItemDoacao: TipoItemDoacao
  tipoUnidade: TipoUnidade
  quantidade: number
}

export interface ItemNecessidadeResponse {
  id: number
  descricao: string | null
  tipoItemDoacao: TipoItemDoacao
  tipoUnidade: TipoUnidade
  quantidade: number
}

export interface NecessidadeDoacaoRequest {
  itens: ItemNecessidadeRequest[]
  localEntrega: EnderecoRequest
}

export interface NecessidadeDoacaoResponse {
  id: number
  situacao: SituacaoNecessidade
  receptor: ContatoResponse
  localEntrega: EnderecoResponse
  itens: ItemNecessidadeResponse[]
  criadoEm: string
  atualizadoEm: string | null
}

export interface NecessidadeFiltros {
  situacao?: SituacaoNecessidade
  page?: number
  size?: number
  sort?: string
}

