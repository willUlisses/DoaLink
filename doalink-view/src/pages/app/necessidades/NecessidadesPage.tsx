import { ClipboardList, PlusCircle } from 'lucide-react'
import { useCallback, useState } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { Pagination } from '../../../components/ui/Pagination.tsx'
import { Select } from '../../../components/ui/Select.tsx'
import { NecessidadeCard } from '../../../features/necessidades/NecessidadeCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useAuth } from '../../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { listarNecessidades } from '../../../services/necessidades.service.ts'
import { SITUACAO_NECESSIDADE, type SituacaoNecessidade } from '../../../types/enums.ts'
import { LABELS_SITUACAO_NECESSIDADE } from '../../../utils/labels.ts'

const SITUACAO_OPTIONS = Object.values(SITUACAO_NECESSIDADE).map((s) => ({
  value: s,
  label: LABELS_SITUACAO_NECESSIDADE[s],
}))

export function NecessidadesPage() {
  useDocumentTitle('Necessidades de doação')
  const { user } = useAuth()

  const [situacao, setSituacao] = useState<SituacaoNecessidade>('ABERTA')
  const [page, setPage] = useState(0)

  const fetcher = useCallback(
    () => listarNecessidades({ situacao, page, size: 12 }),
    [situacao, page],
  )
  const { data, loading, error, reload: fetchNecessidades } = useAsyncResource(fetcher, [
    situacao,
    page,
  ])

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Necessidades' }]} />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Necessidades de doação</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Consulte as demandas publicadas por ONGs receptoras.
            </p>
          </div>
          {user?.tipoUsuario === 'RECEPTOR' ? (
            <Link
              to="/app/necessidades/nova"
              className="inline-flex min-h-10 items-center justify-center gap-2 rounded-[8px] bg-receptor-aa px-4 py-2 text-sm font-semibold text-white hover:bg-receptor-hover"
            >
              <PlusCircle className="h-4 w-4" aria-hidden="true" />
              <span>Registrar necessidade</span>
            </Link>
          ) : null}
        </div>
      </div>

      <Card padding="sm" className="max-w-xs">
        <Select
          label="Filtrar por situação"
          options={SITUACAO_OPTIONS}
          value={situacao}
          onChange={(e) => {
            setSituacao(e.target.value as SituacaoNecessidade)
            setPage(0)
          }}
        />
      </Card>

      {loading ? <CardListSkeleton count={6} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={fetchNecessidades} /> : null}

      {!loading && !error && data && data.content.length === 0 ? (
        <EmptyState
          icon={<ClipboardList className="h-6 w-6" />}
          title="Nenhuma necessidade encontrada"
          description={`Não há necessidades registradas com situação ${LABELS_SITUACAO_NECESSIDADE[situacao]}.`}
        />
      ) : null}

      {!loading && !error && data && data.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {data.content.map((nec) => (
              <NecessidadeCard
                key={nec.id}
                necessidade={nec}
                variant="public"
                actions={
                  <Link
                    to={`/app/necessidades/${nec.id}`}
                    className="inline-flex min-h-9 items-center justify-center rounded-[8px] border border-border bg-surface px-3 py-1.5 text-sm font-semibold text-ink hover:bg-surface-subtle"
                  >
                    Ver detalhes
                  </Link>
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
