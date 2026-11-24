import type { ContatoResponse, EnderecoRequest, EnderecoResponse } from './endereco.ts'
import type { SituacaoCampanha, TipoItemDoacao, TipoUnidade } from './enums.ts'

export interface ItemCampanhaRequest {
  tipoItemDoacao: TipoItemDoacao
  tipoUnidade: TipoUnidade
  quantidade: number
  descricao?: string
}

export interface ItemCampanhaResponse {
  id: number
  tipoItemDoacao: TipoItemDoacao
  tipoUnidade: TipoUnidade
  quantidade: number
  descricao: string | null
}

export interface CampanhaEmergencialRequest {
  nome: string
  descricao?: string
  enderecoRecebimento: EnderecoRequest
  itens?: ItemCampanhaRequest[]
}

export interface CampanhaEmergencialResponse {
  id: number
  nome: string
  descricao: string | null
  situacao: SituacaoCampanha
  receptor: ContatoResponse | null
  enderecoRecebimento: EnderecoResponse | null
  itens: ItemCampanhaResponse[]
  criadoEm: string
  atualizadoEm: string | null
}

export interface CampanhaFiltros {
  situacao?: SituacaoCampanha
  page?: number
  size?: number
  sort?: string
}

