import { Flame, PlusCircle } from 'lucide-react'
import { useCallback, useState } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { Pagination } from '../../../components/ui/Pagination.tsx'
import { Select } from '../../../components/ui/Select.tsx'
import { CampanhaCard } from '../../../features/campanhas/CampanhaCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useAuth } from '../../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { listarCampanhas } from '../../../services/campanhas.service.ts'
import { SITUACAO_CAMPANHA, type SituacaoCampanha } from '../../../types/enums.ts'
import { LABELS_SITUACAO_CAMPANHA } from '../../../utils/labels.ts'

const SITUACAO_OPTIONS = Object.values(SITUACAO_CAMPANHA).map((s) => ({
  value: s,
  label: LABELS_SITUACAO_CAMPANHA[s],
}))

export function CampanhasPage() {
  useDocumentTitle('Campanhas emergenciais')
  const { user } = useAuth()

  const [situacao, setSituacao] = useState<SituacaoCampanha>('ATIVA')
  const [page, setPage] = useState(0)

  const fetcher = useCallback(
    () => listarCampanhas({ situacao, page, size: 12 }),
    [situacao, page],
  )
  const { data, loading, error, reload: fetchCampanhas } = useAsyncResource(fetcher, [
    situacao,
    page,
  ])

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[{ label: 'Painel', to: '/app' }, { label: 'Campanhas emergenciais' }]}
        />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Campanhas emergenciais</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Ações emergenciais publicadas por ONGs receptoras com ponto de recebimento.
            </p>
          </div>
          {user?.tipoUsuario === 'RECEPTOR' ? (
            <Button asChild variant="receptor" leftIcon={<PlusCircle className="h-4 w-4" />}>
              <Link to="/app/campanhas/nova">Criar campanha</Link>
            </Button>
          ) : null}
        </div>
      </div>

      <Card padding="sm" className="max-w-xs">
        <Select
          label="Situação da campanha"
          options={SITUACAO_OPTIONS}
          value={situacao}
          onChange={(e) => {
            setSituacao(e.target.value as SituacaoCampanha)
            setPage(0)
          }}
        />
      </Card>

      {loading ? <CardListSkeleton count={6} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={fetchCampanhas} /> : null}

      {!loading && !error && data && data.content.length === 0 ? (
        <EmptyState
          icon={<Flame className="h-6 w-6" />}
          title="Nenhuma campanha encontrada"
          description={`Não existem campanhas com situação ${LABELS_SITUACAO_CAMPANHA[situacao]} no momento.`}
        />
      ) : null}

      {!loading && !error && data && data.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {data.content.map((campanha) => (
              <CampanhaCard
                key={campanha.id}
                campanha={campanha}
                variant="public"
                actions={
                  <Button asChild variant="secondary" size="sm">
                    <Link to={`/app/campanhas/${campanha.id}`}>Ver campanha</Link>
                  </Button>
                }
              />
            ))}
          </div>
          <Pagination page={data.number} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      ) : null}
    </div>
  )
}
