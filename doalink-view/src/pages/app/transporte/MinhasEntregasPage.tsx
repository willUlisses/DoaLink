import { CheckCircle2, Truck } from 'lucide-react'
import { useCallback, useState } from 'react'
import { Link } from 'react-router-dom'
import { ConfirmDialog } from '../../../components/feedback/ConfirmDialog.tsx'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { ReservaCard } from '../../../features/reservas/ReservaCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { useToast } from '../../../hooks/useToast.ts'
import { normalizeApiError } from '../../../services/api.ts'
import {
  concluirReserva,
  listarMinhasEntregasTransportador,
} from '../../../services/reservas.service.ts'
import type { ReservaEntregaResponse } from '../../../types/reserva.ts'

export function MinhasEntregasPage() {
  useDocumentTitle('Minhas entregas')
  const { showToast } = useToast()

  const [reservaParaConcluir, setReservaParaConcluir] = useState<ReservaEntregaResponse | null>(
    null,
  )
  const [concluding, setConcluding] = useState(false)

  const fetcher = useCallback(() => listarMinhasEntregasTransportador(), [])
  const { data, loading, error, reload: loadEntregas } = useAsyncResource(fetcher, [])
  const entregas = data ?? []

  const handleConfirmConclude = async () => {
    if (!reservaParaConcluir) return
    setConcluding(true)
    try {
      await concluirReserva(reservaParaConcluir.id)
      showToast({
        variant: 'success',
        title: `Entrega #${reservaParaConcluir.id} concluída`,
        description: 'A reserva e a oferta foram atualizadas para CONCLUIDA.',
      })
      setReservaParaConcluir(null)
      loadEntregas()
    } catch (err) {
      showToast({
        variant: 'error',
        title: 'Não foi possível concluir a entrega',
        description: normalizeApiError(err).message,
      })
    } finally {
      setConcluding(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Minhas entregas' }]} />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Minhas entregas</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Entregas vinculadas às suas disponibilidades, com endereços completos e contatos das
              partes.
            </p>
          </div>
          <Link
            to="/app/entregas-disponiveis"
            className="inline-flex min-h-10 items-center justify-center rounded-[8px] bg-transportador-aa px-4 py-2 text-sm font-semibold text-white hover:bg-transportador-hover"
          >
            Assumir novas entregas
          </Link>
        </div>
      </div>

      <InfoBanner tone="info">
        A API atual não possui desvinculação de entrega após assumida nem rastreamento GPS em tempo
        real. Quando a entrega física finalizar, registre a conclusão abaixo.
      </InfoBanner>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadEntregas} /> : null}

      {!loading && !error && entregas.length === 0 ? (
        <EmptyState
          icon={<Truck className="h-6 w-6" />}
          title="Você ainda não assumiu entregas"
          description="Consulte reservas aguardando transporte e vincule uma de suas disponibilidades ativas."
          action={
            <Link
              to="/app/entregas-disponiveis"
              className="inline-flex min-h-10 items-center justify-center rounded-[8px] bg-transportador-aa px-4 py-2 text-sm font-semibold text-white hover:bg-transportador-hover"
            >
              Ver entregas disponíveis
            </Link>
          }
        />
      ) : null}

      {!loading && !error && entregas.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {entregas.map((reserva) => (
            <ReservaCard
              key={reserva.id}
              reserva={reserva}
              showFullDetails
              actions={
                reserva.situacao === 'TRANSPORTE_VINCULADO' ? (
                  <Button
                    variant="transportador"
                    size="sm"
                    leftIcon={<CheckCircle2 className="h-4 w-4" />}
                    onClick={() => setReservaParaConcluir(reserva)}
                  >
                    Concluir entrega
                  </Button>
                ) : null
              }
            />
          ))}
        </div>
      ) : null}

      <ConfirmDialog
        open={Boolean(reservaParaConcluir)}
        title="Confirmar conclusão da entrega?"
        description={`Confirma que a entrega da Reserva #${reservaParaConcluir?.id ?? ''} foi finalizada? A reserva e a oferta passarão para CONCLUIDA.`}
        confirmLabel="Confirmar conclusão"
        cancelLabel="Voltar"
        confirmVariant="transportador"
        loading={concluding}
        onConfirm={handleConfirmConclude}
        onClose={() => setReservaParaConcluir(null)}
      />
    </div>
  )
}
