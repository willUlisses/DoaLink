import { BookmarkCheck, KeyRound, ShieldCheck } from 'lucide-react'
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
  autorizarInicioTransporte,
  listarReservasMinhasOfertasDoador,
} from '../../../services/reservas.service.ts'
import type { ReservaEntregaResponse } from '../../../types/reserva.ts'

export function ReservasDoadorPage() {
  useDocumentTitle('Reservas das minhas ofertas')
  const { showToast } = useToast()

  const [reservaParaAutorizar, setReservaParaAutorizar] = useState<ReservaEntregaResponse | null>(
    null,
  )
  const [authorizing, setAuthorizing] = useState(false)

  const fetcher = useCallback(() => listarReservasMinhasOfertasDoador(), [])
  const { data, loading, error, reload: loadReservas } = useAsyncResource(fetcher, [])
  const reservas = data ?? []

  const handleConfirmAutorizar = async () => {
    if (!reservaParaAutorizar) return
    setAuthorizing(true)
    try {
      const resp = await autorizarInicioTransporte(reservaParaAutorizar.id)
      showToast({
        variant: 'success',
        title: `Transporte autorizado — Reserva #${reservaParaAutorizar.id}`,
        description: resp.codigoRetirada
          ? `Código de retirada: ${resp.codigoRetirada}. Informe-o ao transportador apenas na coleta física.`
          : 'Código de retirada gerado com sucesso.',
      })
      setReservaParaAutorizar(null)
      loadReservas()
    } catch (err) {
      showToast({
        variant: 'error',
        title: 'Não foi possível autorizar o transporte',
        description: normalizeApiError(err).message,
      })
    } finally {
      setAuthorizing(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Reservas recebidas' }]} />
        <h1 className="text-2xl font-bold text-ink">Reservas das minhas ofertas</h1>
        <p className="mt-1 text-sm text-ink-secondary">
          Acompanhe qual ONG reservou cada oferta, autorize o início do transporte e consulte o
          código de retirada quando pendente.
        </p>
      </div>

      <InfoBanner tone="info">
        Quando um transportador aceitar a operação, autorize o início do transporte para gerar o
        código de retirada. O código nunca é enviado automaticamente ao motorista — entregue-o
        somente na coleta presencial dos itens.
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
            <ReservaCard
              key={reserva.id}
              reserva={reserva}
              showFullDetails
              showTimeline
              actions={
                <>
                  {reserva.ofertaId ? (
                    <Button asChild variant="secondary" size="sm">
                      <Link
                        to={`/app/ofertas/${reserva.ofertaId}`}
                        state={{ from: '/app/reservas-ofertas' }}
                      >
                        Ver oferta
                      </Link>
                    </Button>
                  ) : null}
                  {reserva.situacao === 'AGUARDANDO_AUTORIZACAO_DOADOR' && reserva.transportador ? (
                    <Button
                      variant="doador"
                      size="sm"
                      leftIcon={<ShieldCheck className="h-4 w-4" />}
                      onClick={() => setReservaParaAutorizar(reserva)}
                    >
                      Autorizar início do transporte
                    </Button>
                  ) : null}
                  {reserva.situacao === 'AGUARDANDO_CONFIRMACAO_RETIRADA' &&
                  reserva.codigoRetirada ? (
                    <span className="inline-flex items-center gap-1 text-xs font-semibold text-doador-aa">
                      <KeyRound className="h-3.5 w-3.5" aria-hidden="true" />
                      <span>Código disponível acima</span>
                    </span>
                  ) : null}
                </>
              }
            />
          ))}
        </div>
      ) : null}

      <ConfirmDialog
        open={Boolean(reservaParaAutorizar)}
        title="Autorizar início do transporte?"
        description={`Deseja autorizar o início do transporte da Reserva #${reservaParaAutorizar?.id ?? ''}? Um código de retirada será gerado para você repassar ao motorista no momento da coleta.`}
        confirmLabel="Autorizar início"
        cancelLabel="Voltar"
        confirmVariant="doador"
        loading={authorizing}
        onConfirm={handleConfirmAutorizar}
        onClose={() => !authorizing && setReservaParaAutorizar(null)}
      />
    </div>
  )
}
