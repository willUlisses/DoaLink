import type { ContatoResponse, EnderecoRequest, EnderecoResponse } from './endereco.ts'
import type { SituacaoDisponibilidade, TipoUnidade, TipoVeiculo } from './enums.ts'

export interface DisponibilidadeTransporteRequest {
  tipoVeiculo: TipoVeiculo
  capacidade: number
  tipoUnidade: TipoUnidade
  origem: EnderecoRequest
  dataDisponivel: string
  horarioDisponivel?: string
}

export interface DisponibilidadeTransporteResponse {
  id: number
  situacao: SituacaoDisponibilidade
  transportador: ContatoResponse | null
  tipoVeiculo: TipoVeiculo
  capacidade: number
  capacidadeRestante: number
  tipoUnidade: TipoUnidade
  origem: EnderecoResponse | null
  dataDisponivel: string
  horarioDisponivel: string | null
  criadoEm: string
  atualizadoEm: string | null
}

