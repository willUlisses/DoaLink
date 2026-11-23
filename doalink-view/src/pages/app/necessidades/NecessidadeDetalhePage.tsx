import { ArrowLeft, Building2, Calendar, ClipboardList, MapPin, Phone, ShieldAlert } from 'lucide-react'
import { useCallback } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Badge } from '../../../components/ui/Badge.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { StatusBadge } from '../../../components/ui/StatusBadge.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useAuth } from '../../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { buscarNecessidadePorId } from '../../../services/necessidades.service.ts'
import {
  formatarDataHora,
  formatarEnderecoCompleto,
  formatarQuantidade,
} from '../../../utils/formatters.ts'
import { LABELS_CATEGORIA_ITEM, SIGLAS_UNIDADE } from '../../../utils/labels.ts'
import { formatarTelefone } from '../../../utils/masks.ts'

export function NecessidadeDetalhePage() {
  const { id } = useParams()
  const necessidadeId = Number(id)
  const validId = Number.isFinite(necessidadeId) && necessidadeId > 0
  useDocumentTitle(validId ? `Necessidade #${necessidadeId}` : 'Detalhes da necessidade')

  const { user } = useAuth()
  const fetcher = useCallback(() => buscarNecessidadePorId(necessidadeId), [necessidadeId])
  const { data: necessidade, loading, error, reload: loadNecessidade } = useAsyncResource(
    fetcher,
    [necessidadeId],
    validId,
  )
  const isForbidden = error?.status === 403
  const errorMessage = !validId ? 'Identificador de necessidade inválido.' : error?.message ?? null

  const backTarget =
    user?.tipoUsuario === 'RECEPTOR'
      ? { to: '/app/minhas-necessidades', label: 'Minhas necessidades' }
      : { to: '/app/necessidades', label: 'Necessidades abertas' }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[
            { label: 'Painel', to: '/app' },
            { label: backTarget.label, to: backTarget.to },
            { label: Number.isFinite(necessidadeId) ? `Necessidade #${necessidadeId}` : 'Detalhes' },
          ]}
        />
        <Link
          to={backTarget.to}
          className="inline-flex items-center gap-1.5 text-sm font-medium text-ink-secondary hover:text-ink"
        >
          <ArrowLeft className="h-4 w-4" aria-hidden="true" />
          <span>Voltar para {backTarget.label}</span>
        </Link>
      </div>

      {loading ? <CardListSkeleton count={1} /> : null}

      {!loading && isForbidden ? (
        <Card className="text-center" padding="lg">
          <div className="mx-auto mb-3 flex h-11 w-11 items-center justify-center rounded-full bg-warning-soft text-warning">
            <ShieldAlert className="h-5 w-5" aria-hidden="true" />
          </div>
          <h1 className="text-xl font-bold text-ink">Acesso restrito a esta necessidade</h1>
          <p className="mt-2 text-sm text-ink-secondary">{errorMessage}</p>
          <div className="mt-5 flex justify-center">
            <Button asChild variant="secondary">
              <Link to={backTarget.to}>Ir para {backTarget.label}</Link>
            </Button>
          </div>
        </Card>
      ) : null}

      {!loading && errorMessage && !isForbidden ? (
        <ErrorState message={errorMessage} onRetry={loadNecessidade} />
      ) : null}

      {!loading && !errorMessage && necessidade ? (
        <Card className="space-y-5 border-t-4 border-t-receptor">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h1 className="text-2xl font-bold text-ink">Necessidade #{necessidade.id}</h1>
              <p className="mt-1 flex items-center gap-1.5 text-xs text-ink-muted">
                <Calendar className="h-3.5 w-3.5" aria-hidden="true" />
                <span>Registrada em {formatarDataHora(necessidade.criadoEm)}</span>
              </p>
            </div>
            <StatusBadge situacao={necessidade.situacao} />
          </div>

          <div className="grid grid-cols-1 gap-4 rounded-card border border-border bg-canvas p-4 sm:grid-cols-2">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                Organização receptora
              </p>
              <p className="mt-1 flex items-center gap-1.5 text-sm font-semibold text-ink">
                <Building2 className="h-4 w-4 text-receptor-aa" aria-hidden="true" />
                <span>{necessidade.receptor.nome}</span>
              </p>
              <p className="mt-0.5 text-xs text-ink-secondary">{necessidade.receptor.email}</p>
              {necessidade.receptor.telefone ? (
                <p className="mt-0.5 flex items-center gap-1.5 text-xs text-ink-secondary">
                  <Phone className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
                  <span>{formatarTelefone(necessidade.receptor.telefone)}</span>
                </p>
              ) : null}
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                Local de entrega
              </p>
              <p className="mt-1 flex items-start gap-1.5 text-sm text-ink">
                <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-receptor-aa" aria-hidden="true" />
                <span>{formatarEnderecoCompleto(necessidade.localEntrega)}</span>
              </p>
            </div>
          </div>

          <div>
            <h2 className="mb-3 flex items-center gap-2 text-base font-bold text-ink">
              <ClipboardList className="h-4 w-4 text-receptor-aa" aria-hidden="true" />
              <span>Itens solicitados ({necessidade.itens.length})</span>
            </h2>
            <ul className="space-y-2">
              {necessidade.itens.map((item) => (
                <li
                  key={item.id}
                  className="flex flex-col justify-between gap-2 rounded-button border border-border bg-surface p-3.5 sm:flex-row sm:items-center"
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
          </div>
        </Card>
      ) : null}
    </div>
  )
}
