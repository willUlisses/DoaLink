import { BookmarkCheck, CheckCircle2, Trash2 } from 'lucide-react'
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
  cancelarReserva,
  concluirReserva,
  listarMinhasReservasReceptor,
} from '../../../services/reservas.service.ts'
import type { ReservaEntregaResponse } from '../../../types/reserva.ts'

type PendingReceptorAction =
  | { type: 'cancel'; reserva: ReservaEntregaResponse }
  | { type: 'conclude'; reserva: ReservaEntregaResponse }

export function MinhasReservasReceptorPage() {
  useDocumentTitle('Minhas reservas')
  const { showToast } = useToast()

  const [pendingAction, setPendingAction] = useState<PendingReceptorAction | null>(null)
  const [processing, setProcessing] = useState(false)

  const fetcher = useCallback(() => listarMinhasReservasReceptor(), [])
  const { data, loading, error, reload: loadReservas } = useAsyncResource(fetcher, [])
  const reservas = data ?? []

  const handleConfirm = async () => {
    if (!pendingAction) return
    setProcessing(true)
    try {
      if (pendingAction.type === 'cancel') {
        await cancelarReserva(pendingAction.reserva.id)
        showToast({
          variant: 'info',
          title: 'Reserva cancelada',
          description: 'A reserva foi cancelada e a oferta retornou para DISPONIVEL.',
        })
      } else {
        await concluirReserva(pendingAction.reserva.id)
        showToast({
          variant: 'success',
          title: 'Retirada concluída',
          description: 'A reserva e a oferta foram atualizadas para CONCLUIDA.',
        })
      }
      setPendingAction(null)
      loadReservas()
    } catch (err) {
      showToast({
        variant: 'error',
        title: 'Erro ao atualizar reserva',
        description: normalizeApiError(err).message,
      })
    } finally {
      setProcessing(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Minhas reservas' }]} />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Minhas reservas</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Acompanhe suas reservas, visualize os endereços completos de retirada e conclua
              retiradas diretas.
            </p>
          </div>
          <Link
            to="/app/ofertas"
            className="inline-flex min-h-10 items-center justify-center rounded-[8px] bg-receptor-aa px-4 py-2 text-sm font-semibold text-white hover:bg-receptor-hover"
          >
            Buscar ofertas disponíveis
          </Link>
        </div>
      </div>

      <InfoBanner tone="info">
        O cancelamento é permitido apenas antes do vínculo com um transportador (
        <strong>AGUARDANDO_TRANSPORTE</strong> ou <strong>AGUARDANDO_RETIRADA</strong>) e devolve a
        oferta para <strong>DISPONIVEL</strong>.
      </InfoBanner>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadReservas} /> : null}

      {!loading && !error && reservas.length === 0 ? (
        <EmptyState
          icon={<BookmarkCheck className="h-6 w-6" />}
          title="Nenhuma reserva realizada"
          description="Explore as ofertas disponíveis e reserve os itens necessários para sua organização."
          action={
            <Link
              to="/app/ofertas"
              className="inline-flex min-h-10 items-center justify-center rounded-[8px] bg-receptor-aa px-4 py-2 text-sm font-semibold text-white hover:bg-receptor-hover"
            >
              Ver ofertas disponíveis
            </Link>
          }
        />
      ) : null}

      {!loading && !error && reservas.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {reservas.map((reserva) => {
            const canCancel =
              reserva.situacao === 'AGUARDANDO_TRANSPORTE' ||
              reserva.situacao === 'AGUARDANDO_RETIRADA'
            const canConclude = reserva.situacao === 'AGUARDANDO_RETIRADA'

            return (
              <ReservaCard
                key={reserva.id}
                reserva={reserva}
                showFullDetails
                actions={
                  canCancel || canConclude ? (
                    <>
                      {canCancel ? (
                        <Button
                          variant="destructive"
                          size="sm"
                          leftIcon={<Trash2 className="h-3.5 w-3.5" />}
                          onClick={() => setPendingAction({ type: 'cancel', reserva })}
                        >
                          Cancelar reserva
                        </Button>
                      ) : null}
                      {canConclude ? (
                        <Button
                          variant="receptor"
                          size="sm"
                          leftIcon={<CheckCircle2 className="h-4 w-4" />}
                          onClick={() => setPendingAction({ type: 'conclude', reserva })}
                        >
                          Concluir retirada
                        </Button>
                      ) : null}
                    </>
                  ) : null
                }
              />
            )
          })}
        </div>
      ) : null}

      <ConfirmDialog
        open={Boolean(pendingAction)}
        title={
          pendingAction?.type === 'cancel'
            ? 'Cancelar reserva?'
            : 'Confirmar conclusão da retirada?'
        }
        description={
          pendingAction?.type === 'cancel'
            ? `Ao cancelar a Reserva #${pendingAction.reserva.id}, a oferta voltará para a situação DISPONIVEL.`
            : `Confirma que a retirada da Reserva #${pendingAction?.reserva.id ?? ''} foi realizada?`
        }
        confirmLabel={
          pendingAction?.type === 'cancel' ? 'Confirmar cancelamento' : 'Confirmar conclusão'
        }
        confirmVariant={pendingAction?.type === 'cancel' ? 'destructive' : 'receptor'}
        loading={processing}
        onConfirm={handleConfirm}
        onClose={() => setPendingAction(null)}
      />
    </div>
  )
}
