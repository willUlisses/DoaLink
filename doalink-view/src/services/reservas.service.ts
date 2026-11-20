import { api } from './api.ts'
import type {
  ReservaEntregaRequest,
  ReservaEntregaResponse,
  VincularTransporteRequest,
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

export async function listarReservasMinhasOfertasDoador(): Promise<ReservaEntregaResponse[]> {
  const { data } = await api.get<ReservaEntregaResponse[]>('/reservas/minhas/doador')
  return data
}

export async function vincularTransporteAReserva(
  reservaId: number,
  payload: VincularTransporteRequest,
): Promise<ReservaEntregaResponse> {
  const { data } = await api.patch<ReservaEntregaResponse>(
    `/reservas/${reservaId}/vincular-transporte`,
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

