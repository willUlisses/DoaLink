import type { ContatoResponse, EnderecoRequest, EnderecoResponse } from './endereco.ts'
import type {
  SituacaoOferta,
  SituacaoReservaEntrega,
  TipoItemDoacao,
  TipoUnidade,
} from './enums.ts'

export interface ItemDoacaoRequest {
  descricao: string
  tipoItemDoacao: TipoItemDoacao
  tipoUnidade: TipoUnidade
  quantidade: number
}

export interface ItemDoacaoResponse {
  id: number
  descricao: string
  tipoItemDoacao: TipoItemDoacao
  tipoUnidade: TipoUnidade
  quantidade: number
}

export interface OfertaDoacaoRequest {
  itens: ItemDoacaoRequest[]
  endereco?: EnderecoRequest
}

export interface OfertaDoacaoResponse {
  id: number
  situacao: SituacaoOferta
  doador: ContatoResponse | null
  receptor?: ContatoResponse | null
  localRetirada: EnderecoResponse | null
  itens: ItemDoacaoResponse[]
  reservaId?: number | null
  situacaoReserva?: SituacaoReservaEntrega | null
  transportador?: ContatoResponse | null
  codigoRetirada?: string | null
  criadoEm: string
  atualizadoEm: string | null
}

export interface OfertaFiltros {
  page?: number
  size?: number
  sort?: string
  categoria?: TipoItemDoacao | ''
  cidade?: string
  estado?: string
}
