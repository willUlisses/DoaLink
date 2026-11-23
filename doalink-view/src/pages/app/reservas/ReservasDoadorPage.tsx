import { BookmarkCheck } from 'lucide-react'
import { useCallback } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { ReservaCard } from '../../../features/reservas/ReservaCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { listarReservasMinhasOfertasDoador } from '../../../services/reservas.service.ts'

export function ReservasDoadorPage() {
  useDocumentTitle('Reservas das minhas ofertas')
  const fetcher = useCallback(() => listarReservasMinhasOfertasDoador(), [])
  const { data, loading, error, reload: loadReservas } = useAsyncResource(fetcher, [])
  const reservas = data ?? []

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Reservas recebidas' }]} />
        <h1 className="text-2xl font-bold text-ink">Reservas das minhas ofertas</h1>
        <p className="mt-1 text-sm text-ink-secondary">
          Acompanhe qual ONG reservou cada oferta e se há um transportador vinculado à entrega.
        </p>
      </div>

      <InfoBanner tone="info">
        A conclusão da entrega ou retirada é registrada pelo Transportador (quando há transporte
        vinculado) ou pela ONG receptora (quando é retirada direta).
      </InfoBanner>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadReservas} /> : null}

      {!loading && !error && reservas.length === 0 ? (
        <EmptyState
          icon={<BookmarkCheck className="h-6 w-6" />}
          title="Nenhuma oferta reservada ainda"
          description="Assim que uma organização receptora reservar uma das suas ofertas disponíveis, o acompanhamento aparecerá aqui."
          action={
            <Button asChild variant="doador">
              <Link to="/app/minhas-ofertas">Ver minhas ofertas</Link>
            </Button>
          }
        />
      ) : null}

      {!loading && !error && reservas.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {reservas.map((reserva) => (
            <ReservaCard key={reserva.id} reserva={reserva} showFullDetails />
          ))}
        </div>
      ) : null}
    </div>
  )
}
