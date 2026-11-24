import { api } from './api.ts'
import type { PageResponse } from '../types/api.ts'
import type {
  OfertaDoacaoRequest,
  OfertaDoacaoResponse,
  OfertaFiltros,
} from '../types/oferta.ts'

export async function listarOfertasDisponiveis(
  filtros: OfertaFiltros = {},
): Promise<PageResponse<OfertaDoacaoResponse>> {
  const params: Record<string, string | number> = {
    page: filtros.page ?? 0,
    size: filtros.size ?? 12,
    sort: filtros.sort ?? 'criadoEm,desc',
  }

  if (filtros.categoria) {
    params.categoria = filtros.categoria
  }
  if (filtros.cidade && filtros.cidade.trim() !== '') {
    params.cidade = filtros.cidade.trim()
  }
  if (filtros.estado && filtros.estado.trim() !== '') {
    params.estado = filtros.estado.trim().toUpperCase()
  }

  const { data } = await api.get<PageResponse<OfertaDoacaoResponse>>('/ofertas', { params })
  return data
}

export async function listarMinhasOfertas(): Promise<OfertaDoacaoResponse[]> {
  const { data } = await api.get<OfertaDoacaoResponse[]>('/ofertas/minhas')
  return data
}

export async function buscarOfertaPorId(id: number): Promise<OfertaDoacaoResponse> {
  const { data } = await api.get<OfertaDoacaoResponse>(`/ofertas/${id}`)
  return data
}

export async function criarOferta(payload: OfertaDoacaoRequest): Promise<OfertaDoacaoResponse> {
  const { data } = await api.post<OfertaDoacaoResponse>('/ofertas', payload)
  return data
}

export async function cancelarOferta(id: number): Promise<void> {
  await api.patch(`/ofertas/${id}/cancelar`)
}

