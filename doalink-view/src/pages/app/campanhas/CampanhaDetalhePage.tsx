import { ArrowLeft, Building2, Calendar, Flame, MapPin } from 'lucide-react'
import { useCallback } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Badge } from '../../../components/ui/Badge.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { StatusBadge } from '../../../components/ui/StatusBadge.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { buscarCampanhaPorId } from '../../../services/campanhas.service.ts'
import {
  formatarDataHora,
  formatarEnderecoCompleto,
  formatarQuantidade,
} from '../../../utils/formatters.ts'
import { LABELS_CATEGORIA_ITEM, SIGLAS_UNIDADE } from '../../../utils/labels.ts'

export function CampanhaDetalhePage() {
  const { id } = useParams()
  const campanhaId = Number(id)
  const validId = Number.isFinite(campanhaId) && campanhaId > 0
  useDocumentTitle(validId ? `Campanha #${campanhaId}` : 'Detalhes da campanha')

  const fetcher = useCallback(() => buscarCampanhaPorId(campanhaId), [campanhaId])
  const { data: campanha, loading, error, reload: loadCampanha } = useAsyncResource(
    fetcher,
    [campanhaId],
    validId,
  )
  const errorMessage = !validId ? 'Identificador de campanha inválido.' : error?.message ?? null

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[
            { label: 'Painel', to: '/app' },
            { label: 'Campanhas', to: '/app/campanhas' },
            { label: Number.isFinite(campanhaId) ? `Campanha #${campanhaId}` : 'Detalhes' },
          ]}
        />
        <Link
          to="/app/campanhas"
          className="inline-flex items-center gap-1.5 text-sm font-medium text-ink-secondary hover:text-ink"
        >
          <ArrowLeft className="h-4 w-4" aria-hidden="true" />
          <span>Voltar para campanhas</span>
        </Link>
      </div>

      {loading ? <CardListSkeleton count={1} /> : null}

      {!loading && errorMessage ? <ErrorState message={errorMessage} onRetry={loadCampanha} /> : null}

      {!loading && !errorMessage && campanha ? (
        <Card className="space-y-5 border-t-4 border-t-receptor">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <div className="flex items-center gap-2 text-danger">
                <Flame className="h-4 w-4" aria-hidden="true" />
                <span className="text-xs font-semibold uppercase tracking-wide">
                  Campanha Emergencial #{campanha.id}
                </span>
              </div>
              <h1 className="mt-1 text-2xl font-bold text-ink">{campanha.nome}</h1>
              <p className="mt-1 flex items-center gap-1.5 text-xs text-ink-muted">
                <Calendar className="h-3.5 w-3.5" aria-hidden="true" />
                <span>Criada em {formatarDataHora(campanha.criadoEm)}</span>
              </p>
            </div>
            <StatusBadge situacao={campanha.situacao} />
          </div>

          {campanha.descricao ? (
            <div className="rounded-[12px] border border-border bg-surface-subtle/50 p-4">
              <h2 className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                Contexto da emergência
              </h2>
              <p className="mt-1.5 whitespace-pre-line text-sm leading-relaxed text-ink">
                {campanha.descricao}
              </p>
            </div>
          ) : null}

          <div className="grid grid-cols-1 gap-4 rounded-[12px] border border-border bg-canvas p-4 sm:grid-cols-2">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                Organização responsável
              </p>
              {campanha.receptor ? (
                <>
                  <p className="mt-1 flex items-center gap-1.5 text-sm font-semibold text-ink">
                    <Building2 className="h-4 w-4 text-receptor-aa" aria-hidden="true" />
                    <span>{campanha.receptor.nome}</span>
                  </p>
                  <p className="mt-0.5 text-xs text-ink-secondary">{campanha.receptor.email}</p>
                </>
              ) : (
                <p className="mt-1 text-xs text-ink-secondary">Não informado</p>
              )}
            </div>

            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                Ponto de recebimento de doações
              </p>
              <p className="mt-1 flex items-start gap-1.5 text-sm text-ink">
                <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-receptor-aa" aria-hidden="true" />
                <span>{formatarEnderecoCompleto(campanha.enderecoRecebimento)}</span>
              </p>
            </div>
          </div>

          <div>
            <h2 className="mb-3 text-base font-bold text-ink">
              Necessidades emergenciais ({campanha.itens.length})
            </h2>
            {campanha.itens.length === 0 ? (
              <p className="text-sm text-ink-secondary">
                Nenhum item específico foi listado nesta campanha.
              </p>
            ) : (
              <ul className="space-y-2">
                {campanha.itens.map((item) => (
                  <li
                    key={item.id}
                    className="flex flex-col justify-between gap-2 rounded-[10px] border border-border bg-surface p-3.5 sm:flex-row sm:items-center"
                  >
                    <div>
                      <p className="text-sm font-semibold text-ink">
                        {LABELS_CATEGORIA_ITEM[item.tipoItemDoacao]}
                      </p>
                      {item.descricao ? (
                        <p className="text-xs text-ink-secondary">{item.descricao}</p>
                      ) : null}
                    </div>
                    <Badge tone="receptor">
                      {formatarQuantidade(item.quantidade)} {SIGLAS_UNIDADE[item.tipoUnidade]}
                    </Badge>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </Card>
      ) : null}
    </div>
  )
}
