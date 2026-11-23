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
import { LABELS_SITUACAO_OFERTA, LABELS_SITUACAO_RESERVA } from '../../../utils/labels.ts'

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
          description: `A reserva foi cancelada e a oferta retornou para "${LABELS_SITUACAO_OFERTA.DISPONIVEL}".`,
        })
      } else {
        await concluirReserva(pendingAction.reserva.id)
        showToast({
          variant: 'success',
          title: 'Reserva finalizada',
          description: `A reserva e a oferta foram atualizadas para "${LABELS_SITUACAO_RESERVA.CONCLUIDA}".`,
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
              Acompanhe suas reservas, visualize os contatos e endereços de retirada e finalize as
              reservas concluídas.
            </p>
          </div>
          <Button asChild variant="receptor">
            <Link to="/app/ofertas">Buscar ofertas disponíveis</Link>
          </Button>
        </div>
      </div>

      <InfoBanner tone="info">
        O cancelamento é permitido apenas antes do vínculo com um transportador (
        <strong>{LABELS_SITUACAO_RESERVA.AGUARDANDO_TRANSPORTE}</strong> ou{' '}
        <strong>{LABELS_SITUACAO_RESERVA.AGUARDANDO_RETIRADA}</strong>) e devolve a oferta para{' '}
        <strong>{LABELS_SITUACAO_OFERTA.DISPONIVEL}</strong>.
      </InfoBanner>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadReservas} /> : null}

      {!loading && !error && reservas.length === 0 ? (
        <EmptyState
          icon={<BookmarkCheck className="h-6 w-6" />}
          title="Nenhuma reserva realizada"
          description="Explore as ofertas disponíveis e reserve os itens necessários para sua organização."
          action={
            <Button asChild variant="receptor">
              <Link to="/app/ofertas">Ver ofertas disponíveis</Link>
            </Button>
          }
        />
      ) : null}

      {!loading && !error && reservas.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {reservas.map((reserva) => {
            const canCancel =
              reserva.situacao === 'AGUARDANDO_TRANSPORTE' ||
              reserva.situacao === 'AGUARDANDO_RETIRADA'
            const canConclude =
              reserva.situacao === 'AGUARDANDO_RETIRADA' ||
              reserva.situacao === 'TRANSPORTE_VINCULADO'

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
                          Finalizar reserva
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
            : 'Confirmar finalização da reserva?'
        }
        description={
          pendingAction?.type === 'cancel'
            ? `Ao cancelar a Reserva #${pendingAction.reserva.id}, a oferta voltará para a situação "${LABELS_SITUACAO_OFERTA.DISPONIVEL}".`
            : `Confirma que a doação da Reserva #${pendingAction?.reserva.id ?? ''} foi concluída? A reserva passará para "${LABELS_SITUACAO_RESERVA.CONCLUIDA}".`
        }
        confirmLabel={
          pendingAction?.type === 'cancel' ? 'Confirmar cancelamento' : 'Confirmar finalização'
        }
        confirmVariant={pendingAction?.type === 'cancel' ? 'destructive' : 'receptor'}
        loading={processing}
        onConfirm={handleConfirm}
        onClose={() => setPendingAction(null)}
      />
    </div>
  )
}
