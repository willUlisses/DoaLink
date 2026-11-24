import { api } from './api.ts'
import type { PageResponse } from '../types/api.ts'
import type {
  NecessidadeDoacaoRequest,
  NecessidadeDoacaoResponse,
  NecessidadeFiltros,
} from '../types/necessidade.ts'

export async function listarNecessidades(
  filtros: NecessidadeFiltros = {},
): Promise<PageResponse<NecessidadeDoacaoResponse>> {
  const params: Record<string, string | number> = {
    page: filtros.page ?? 0,
    size: filtros.size ?? 12,
    sort: filtros.sort ?? 'criadoEm,desc',
  }
  if (filtros.situacao) {
    params.situacao = filtros.situacao
  }

  const { data } = await api.get<PageResponse<NecessidadeDoacaoResponse>>('/necessidades', {
    params,
  })
  return data
}

export async function listarMinhasNecessidades(): Promise<NecessidadeDoacaoResponse[]> {
  const { data } = await api.get<NecessidadeDoacaoResponse[]>('/necessidades/minhas')
  return data
}

export async function buscarNecessidadePorId(id: number): Promise<NecessidadeDoacaoResponse> {
  const { data } = await api.get<NecessidadeDoacaoResponse>(`/necessidades/${id}`)
  return data
}

export async function criarNecessidade(
  payload: NecessidadeDoacaoRequest,
): Promise<NecessidadeDoacaoResponse> {
  const { data } = await api.post<NecessidadeDoacaoResponse>('/necessidades', payload)
  return data
}

