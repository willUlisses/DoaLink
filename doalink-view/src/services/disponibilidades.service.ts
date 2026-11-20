import { api } from './api.ts'
import type {
  DisponibilidadeTransporteRequest,
  DisponibilidadeTransporteResponse,
} from '../types/transporte.ts'

export async function listarMinhasDisponibilidades(): Promise<DisponibilidadeTransporteResponse[]> {
  const { data } = await api.get<DisponibilidadeTransporteResponse[]>('/disponibilidades/minhas')
  return data
}

export async function criarDisponibilidade(
  payload: DisponibilidadeTransporteRequest,
): Promise<DisponibilidadeTransporteResponse> {
  const { data } = await api.post<DisponibilidadeTransporteResponse>('/disponibilidades', payload)
  return data
}

export async function cancelarDisponibilidade(id: number): Promise<void> {
  await api.patch(`/disponibilidades/${id}/cancelar`)
}

export async function encerrarDisponibilidade(id: number): Promise<void> {
  await api.patch(`/disponibilidades/${id}/encerrar`)
}

