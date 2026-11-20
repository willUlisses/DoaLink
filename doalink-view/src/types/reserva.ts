import type { ContatoResponse, EnderecoResponse } from './endereco.ts'
import type { SituacaoOferta, SituacaoReservaEntrega } from './enums.ts'
import type { ItemDoacaoResponse } from './oferta.ts'

export interface ReservaEntregaRequest {
  ofertaId: number
  precisaTransporte: boolean
}

export interface VincularTransporteRequest {
  disponibilidadeId: number
}

export interface ReservaEntregaResponse {
  id: number
  situacao: SituacaoReservaEntrega
  precisaTransporte: boolean
  ofertaId: number | null
  situacaoOferta: SituacaoOferta | null
  disponibilidadeId: number | null
  doador: ContatoResponse | null
  receptor: ContatoResponse | null
  transportador: ContatoResponse | null
  enderecoRetirada: EnderecoResponse | null
  enderecoEntrega: EnderecoResponse | null
  itens: ItemDoacaoResponse[]
  criadoEm: string
  atualizadoEm: string | null
}

