import { History, Truck } from 'lucide-react'
import { useCallback } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { ReservaCard } from '../../../features/reservas/ReservaCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { listarHistoricoTransportador } from '../../../services/reservas.service.ts'

export function HistoricoTransportesPage() {
  useDocumentTitle('Histórico de transportes')

  const fetcher = useCallback(() => listarHistoricoTransportador(), [])
  const { data, loading, error, reload: loadHistorico } = useAsyncResource(fetcher, [])
  const historico = data ?? []

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[{ label: 'Painel', to: '/app' }, { label: 'Histórico de transportes' }]}
        />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Histórico de transportes</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Consulte todos os transportes concluídos com datas de aceite, autorização, retirada e
              entrega. Operações finalizadas não podem ser alteradas.
            </p>
          </div>
          <Button asChild variant="secondary" leftIcon={<Truck className="h-4 w-4" />}>
            <Link to="/app/meus-transportes">Meus transportes</Link>
          </Button>
        </div>
      </div>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadHistorico} /> : null}

      {!loading && !error && historico.length === 0 ? (
        <EmptyState
          icon={<History className="h-6 w-6" />}
          title="Nenhum transporte concluído ainda"
          description="Após confirmar a entrega de uma doação com o código do receptor, ela aparecerá aqui."
          action={
            <Button asChild variant="transportador">
              <Link to="/app/meus-transportes">Ver meus transportes</Link>
            </Button>
          }
        />
      ) : null}

      {!loading && !error && historico.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {historico.map((reserva) => (
            <ReservaCard
              key={reserva.id}
              reserva={reserva}
              showFullDetails
              showTimeline
            />
          ))}
        </div>
      ) : null}
    </div>
  )
}
