import { api } from './api.ts'
import type {
  ConfirmarCodigoTransporteRequest,
  ReservaEntregaRequest,
  ReservaEntregaResponse,
} from '../types/reserva.ts'

export async function reservarOferta(
  payload: ReservaEntregaRequest,
): Promise<ReservaEntregaResponse> {
  const { data } = await api.post<ReservaEntregaResponse>('/reservas', payload)
  return data
}

export async function listarReservasAguardandoTransporte(): Promise<ReservaEntregaResponse[]> {
  const { data } = await api.get<ReservaEntregaResponse[]>('/reservas/aguardando-transporte')
  return data
}

export async function listarMinhasReservasReceptor(): Promise<ReservaEntregaResponse[]> {
  const { data } = await api.get<ReservaEntregaResponse[]>('/reservas/minhas/receptor')
  return data
}

export async function listarMinhasEntregasTransportador(): Promise<ReservaEntregaResponse[]> {
  const { data } = await api.get<ReservaEntregaResponse[]>('/reservas/minhas/transportador')
  return data
}

export async function listarHistoricoTransportador(): Promise<ReservaEntregaResponse[]> {
  const { data } = await api.get<ReservaEntregaResponse[]>(
    '/reservas/minhas/transportador/historico',
  )
  return data
}

export async function listarReservasMinhasOfertasDoador(): Promise<ReservaEntregaResponse[]> {
  const { data } = await api.get<ReservaEntregaResponse[]>('/reservas/minhas/doador')
  return data
}

export async function buscarReservaPorId(reservaId: number): Promise<ReservaEntregaResponse> {
  const { data } = await api.get<ReservaEntregaResponse>(`/reservas/${reservaId}`)
  return data
}

export async function aceitarTransporte(reservaId: number): Promise<ReservaEntregaResponse> {
  const { data } = await api.patch<ReservaEntregaResponse>(
    `/reservas/${reservaId}/aceitar-transporte`,
  )
  return data
}

export async function autorizarInicioTransporte(
  reservaId: number,
): Promise<ReservaEntregaResponse> {
  const { data } = await api.patch<ReservaEntregaResponse>(
    `/reservas/${reservaId}/autorizar-transporte`,
  )
  return data
}

export async function autorizarInicioTransportePorOferta(
  ofertaId: number,
): Promise<ReservaEntregaResponse> {
  const { data } = await api.patch<ReservaEntregaResponse>(
    `/ofertas/${ofertaId}/autorizar-transporte`,
  )
  return data
}

export async function confirmarRetiradaTransporte(
  reservaId: number,
  payload: ConfirmarCodigoTransporteRequest,
): Promise<ReservaEntregaResponse> {
  const { data } = await api.patch<ReservaEntregaResponse>(
    `/reservas/${reservaId}/confirmar-retirada`,
    payload,
  )
  return data
}

export async function confirmarEntregaTransporte(
  reservaId: number,
  payload: ConfirmarCodigoTransporteRequest,
): Promise<ReservaEntregaResponse> {
  const { data } = await api.patch<ReservaEntregaResponse>(
    `/reservas/${reservaId}/confirmar-entrega`,
    payload,
  )
  return data
}

export async function concluirReserva(reservaId: number): Promise<void> {
  await api.patch(`/reservas/${reservaId}/concluir`)
}

export async function cancelarReserva(reservaId: number): Promise<void> {
  await api.patch(`/reservas/${reservaId}/cancelar`)
}
