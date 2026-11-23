import { Package, Truck } from 'lucide-react'
import { useCallback, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { ReservaCard } from '../../../features/reservas/ReservaCard.tsx'
import { VincularTransporteDialog } from '../../../features/reservas/VincularTransporteDialog.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { useToast } from '../../../hooks/useToast.ts'
import { normalizeApiError } from '../../../services/api.ts'
import { listarMinhasDisponibilidades } from '../../../services/disponibilidades.service.ts'
import {
  listarReservasAguardandoTransporte,
  vincularTransporteAReserva,
} from '../../../services/reservas.service.ts'
import type { ReservaEntregaResponse } from '../../../types/reserva.ts'

export function EntregasDisponiveisPage() {
  useDocumentTitle('Entregas aguardando transporte')
  const { showToast } = useToast()
  const navigate = useNavigate()

  const [selectedReserva, setSelectedReserva] = useState<ReservaEntregaResponse | null>(null)
  const [linking, setLinking] = useState(false)
  const [linkError, setLinkError] = useState<string | null>(null)

  const fetcher = useCallback(async () => {
    const [reservasResp, disponibilidadesResp] = await Promise.all([
      listarReservasAguardandoTransporte(),
      listarMinhasDisponibilidades(),
    ])
    return { reservas: reservasResp, disponibilidades: disponibilidadesResp }
  }, [])

  const { data, loading, error, reload: loadData } = useAsyncResource(fetcher, [])
  const reservas = data?.reservas ?? []
  const disponibilidades = data?.disponibilidades ?? []

  const handleConfirmBind = async (disponibilidadeId: number) => {
    if (!selectedReserva) return
    setLinking(true)
    setLinkError(null)
    try {
      await vincularTransporteAReserva(selectedReserva.id, { disponibilidadeId })
      showToast({
        variant: 'success',
        title: `Entrega da Reserva #${selectedReserva.id} assumida`,
        description: 'Os endereços completos agora estão disponíveis em Minhas entregas.',
      })
      setSelectedReserva(null)
      navigate('/app/minhas-entregas')
    } catch (err) {
      setLinkError(normalizeApiError(err).message)
    } finally {
      setLinking(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[{ label: 'Painel', to: '/app' }, { label: 'Aguardando transporte' }]}
        />
        <h1 className="text-2xl font-bold text-ink">Entregas aguardando transporte</h1>
        <p className="mt-1 text-sm text-ink-secondary">
          Por privacidade, apenas cidade e UF são exibidos antes do vínculo. Ao assumir a entrega,
          os endereços completos de retirada e entrega são liberados.
        </p>
      </div>

      <InfoBanner tone="info">
        Você pode vincular múltiplas entregas compatíveis a uma mesma disponibilidade de transporte,
        respeitando o saldo de capacidade. A sugestão automática de rotas e paradas por mapa estará
        disponível em uma atualização futura.
      </InfoBanner>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadData} /> : null}

      {!loading && !error && reservas.length === 0 ? (
        <EmptyState
          icon={<Package className="h-6 w-6" />}
          title="Nenhuma reserva aguardando transporte"
          description="Quando uma ONG reservar uma oferta solicitando transporte, ela aparecerá aqui."
        />
      ) : null}

      {!loading && !error && reservas.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {reservas.map((reserva) => (
            <ReservaCard
              key={reserva.id}
              reserva={reserva}
              showFullDetails={false}
              actions={
                <Button
                  variant="transportador"
                  size="sm"
                  leftIcon={<Truck className="h-4 w-4" />}
                  onClick={() => {
                    setLinkError(null)
                    setSelectedReserva(reserva)
                  }}
                >
                  Assumir entrega
                </Button>
              }
            />
          ))}
        </div>
      ) : null}

      <VincularTransporteDialog
        open={Boolean(selectedReserva)}
        reserva={selectedReserva}
        disponibilidades={disponibilidades}
        loading={linking}
        errorMessage={linkError}
        onConfirm={handleConfirmBind}
        onClose={() => {
          if (!linking) {
            setSelectedReserva(null)
            setLinkError(null)
          }
        }}
      />
    </div>
  )
}
