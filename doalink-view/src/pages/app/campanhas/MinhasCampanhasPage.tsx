import { Flame, PlusCircle } from 'lucide-react'
import { useCallback } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { CampanhaCard } from '../../../features/campanhas/CampanhaCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { listarMinhasCampanhas } from '../../../services/campanhas.service.ts'

export function MinhasCampanhasPage() {
  useDocumentTitle('Minhas campanhas')
  const fetcher = useCallback(() => listarMinhasCampanhas(), [])
  const { data, loading, error, reload: loadMinhas } = useAsyncResource(fetcher, [])
  const campanhas = data ?? []

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Minhas campanhas' }]} />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Minhas campanhas emergenciais</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Campanhas cadastradas pela sua organização receptora.
            </p>
          </div>
          <Link
            to="/app/campanhas/nova"
            className="inline-flex min-h-10 items-center justify-center gap-2 rounded-[8px] bg-receptor-aa px-4 py-2 text-sm font-semibold text-white hover:bg-receptor-hover"
          >
            <PlusCircle className="h-4 w-4" aria-hidden="true" />
            <span>Nova campanha</span>
          </Link>
        </div>
      </div>

      <InfoBanner tone="info">
        A API atual permite criar e consultar campanhas emergenciais (iniciadas como{' '}
        <strong>ATIVA</strong>). Endpoints para editar dados, encerrar ou cancelar uma campanha
        ainda não existem no back-end.
      </InfoBanner>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadMinhas} /> : null}

      {!loading && !error && campanhas.length === 0 ? (
        <EmptyState
          icon={<Flame className="h-6 w-6" />}
          title="Nenhuma campanha criada"
          description="Crie uma campanha emergencial para mobilizar doadores e transportadores em situações críticas."
          action={
            <Link
              to="/app/campanhas/nova"
              className="inline-flex min-h-10 items-center justify-center rounded-[8px] bg-receptor-aa px-4 py-2 text-sm font-semibold text-white hover:bg-receptor-hover"
            >
              Criar campanha emergencial
            </Link>
          }
        />
      ) : null}

      {!loading && !error && campanhas.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
          {campanhas.map((campanha) => (
            <CampanhaCard
              key={campanha.id}
              campanha={campanha}
              variant="owner"
              actions={
                <Link
                  to={`/app/campanhas/${campanha.id}`}
                  className="inline-flex min-h-9 items-center justify-center rounded-[8px] border border-border bg-surface px-3 py-1.5 text-sm font-semibold text-ink hover:bg-surface-subtle"
                >
                  Detalhes
                </Link>
              }
            />
          ))}
        </div>
      ) : null}
    </div>
  )
}
