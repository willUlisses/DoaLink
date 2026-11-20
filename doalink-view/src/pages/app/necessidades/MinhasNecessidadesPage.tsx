import { ClipboardList, PlusCircle } from 'lucide-react'
import { useCallback } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { NecessidadeCard } from '../../../features/necessidades/NecessidadeCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { listarMinhasNecessidades } from '../../../services/necessidades.service.ts'

export function MinhasNecessidadesPage() {
  useDocumentTitle('Minhas necessidades')
  const fetcher = useCallback(() => listarMinhasNecessidades(), [])
  const { data, loading, error, reload: loadMinhas } = useAsyncResource(fetcher, [])
  const necessidades = data ?? []

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Minhas necessidades' }]} />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Minhas necessidades</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Necessidades registradas pela sua organização.
            </p>
          </div>
          <Link
            to="/app/necessidades/nova"
            className="inline-flex min-h-10 items-center justify-center gap-2 rounded-[8px] bg-receptor-aa px-4 py-2 text-sm font-semibold text-white hover:bg-receptor-hover"
          >
            <PlusCircle className="h-4 w-4" aria-hidden="true" />
            <span>Nova necessidade</span>
          </Link>
        </div>
      </div>

      <InfoBanner tone="info">
        Conforme o contrato atual da API, necessidades registradas não possuem edição ou
        cancelamento após o envio.
      </InfoBanner>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadMinhas} /> : null}

      {!loading && !error && necessidades.length === 0 ? (
        <EmptyState
          icon={<ClipboardList className="h-6 w-6" />}
          title="Nenhuma necessidade registrada"
          description="Publique os itens que sua ONG precisa receber para dar visibilidade aos doadores."
          action={
            <Link
              to="/app/necessidades/nova"
              className="inline-flex min-h-10 items-center justify-center rounded-[8px] bg-receptor-aa px-4 py-2 text-sm font-semibold text-white hover:bg-receptor-hover"
            >
              Registrar primeira necessidade
            </Link>
          }
        />
      ) : null}

      {!loading && !error && necessidades.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
          {necessidades.map((nec) => (
            <NecessidadeCard
              key={nec.id}
              necessidade={nec}
              variant="owner"
              actions={
                <Link
                  to={`/app/necessidades/${nec.id}`}
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
