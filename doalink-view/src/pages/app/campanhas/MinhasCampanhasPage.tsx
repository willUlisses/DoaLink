import { Flame, PlusCircle } from 'lucide-react'
import { useCallback } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { CampanhaCard } from '../../../features/campanhas/CampanhaCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { listarMinhasCampanhas } from '../../../services/campanhas.service.ts'
import { LABELS_SITUACAO_CAMPANHA } from '../../../utils/labels.ts'

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
          <Button asChild variant="receptor" leftIcon={<PlusCircle className="h-4 w-4" />}>
            <Link to="/app/campanhas/nova">Nova campanha</Link>
          </Button>
        </div>
      </div>

      <InfoBanner tone="info">
        Novas campanhas emergenciais são publicadas com situação{' '}
        <strong>{LABELS_SITUACAO_CAMPANHA.ATIVA}</strong>. As opções para editar, encerrar ou
        cancelar campanhas estarão disponíveis em uma atualização futura.
      </InfoBanner>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadMinhas} /> : null}

      {!loading && !error && campanhas.length === 0 ? (
        <EmptyState
          icon={<Flame className="h-6 w-6" />}
          title="Nenhuma campanha criada"
          description="Crie uma campanha emergencial para mobilizar doadores e transportadores em situações críticas."
          action={
            <Button asChild variant="receptor">
              <Link to="/app/campanhas/nova">Criar campanha emergencial</Link>
            </Button>
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
                <Button asChild variant="secondary" size="sm">
                  <Link
                    to={`/app/campanhas/${campanha.id}`}
                    state={{ from: '/app/minhas-campanhas' }}
                  >
                    Detalhes
                  </Link>
                </Button>
              }
            />
          ))}
        </div>
      ) : null}
    </div>
  )
}
