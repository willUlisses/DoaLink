import type { ContatoResponse, EnderecoResponse } from './endereco.ts'
import type {
  SituacaoOferta,
  SituacaoReservaEntrega,
  TipoEventoTransporte,
} from './enums.ts'
import type { ItemDoacaoResponse } from './oferta.ts'

export interface ReservaEntregaRequest {
  ofertaId: number
  precisaTransporte?: boolean
}

export interface ConfirmarCodigoTransporteRequest {
  codigo: string
}

export interface EventoTransporteResponse {
  id: number
  tipoEvento: TipoEventoTransporte
  detalhes: string
  criadoEm: string
}

export interface ReservaEntregaResponse {
  id: number
  situacao: SituacaoReservaEntrega
  precisaTransporte: boolean
  ofertaId: number | null
  situacaoOferta: SituacaoOferta | null
  transportadorId?: number | null
  doador: ContatoResponse | null
  receptor: ContatoResponse | null
  transportador: ContatoResponse | null
  enderecoRetirada: EnderecoResponse | null
  enderecoEntrega: EnderecoResponse | null
  itens: ItemDoacaoResponse[]
  aceitoEm?: string | null
  autorizadoEm?: string | null
  retiradaConfirmadaEm?: string | null
  entregaConfirmadaEm?: string | null
  codigoRetirada?: string | null
  codigoEntrega?: string | null
  codigoEntregaExpiraEm?: string | null
  historicoEventos?: EventoTransporteResponse[]
  criadoEm: string
  atualizadoEm: string | null
}
