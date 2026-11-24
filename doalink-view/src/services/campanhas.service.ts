import { api } from './api.ts'
import type { PageResponse } from '../types/api.ts'
import type {
  CampanhaEmergencialRequest,
  CampanhaEmergencialResponse,
  CampanhaFiltros,
} from '../types/campanha.ts'

export async function listarCampanhas(
  filtros: CampanhaFiltros = {},
): Promise<PageResponse<CampanhaEmergencialResponse>> {
  const params: Record<string, string | number> = {
    page: filtros.page ?? 0,
    size: filtros.size ?? 12,
    sort: filtros.sort ?? 'criadoEm,desc',
  }
  if (filtros.situacao) {
    params.situacao = filtros.situacao
  }

  const { data } = await api.get<PageResponse<CampanhaEmergencialResponse>>('/campanhas', {
    params,
  })
  return data
}

export async function listarMinhasCampanhas(): Promise<CampanhaEmergencialResponse[]> {
  const { data } = await api.get<CampanhaEmergencialResponse[]>('/campanhas/minhas')
  return data
}

export async function buscarCampanhaPorId(id: number): Promise<CampanhaEmergencialResponse> {
  const { data } = await api.get<CampanhaEmergencialResponse>(`/campanhas/${id}`)
  return data
}

export async function criarCampanha(
  payload: CampanhaEmergencialRequest,
): Promise<CampanhaEmergencialResponse> {
  const { data } = await api.post<CampanhaEmergencialResponse>('/campanhas', payload)
  return data
}

