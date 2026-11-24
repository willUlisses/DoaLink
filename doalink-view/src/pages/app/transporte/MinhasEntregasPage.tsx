import { CheckCircle2, History, KeyRound, ShieldAlert, Truck } from 'lucide-react'
import { useCallback, useState } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { ConfirmarCodigoDialog } from '../../../features/reservas/ConfirmarCodigoDialog.tsx'
import { ReservaCard } from '../../../features/reservas/ReservaCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { useToast } from '../../../hooks/useToast.ts'
import { normalizeApiError } from '../../../services/api.ts'
import {
  confirmarEntregaTransporte,
  confirmarRetiradaTransporte,
  listarMinhasEntregasTransportador,
} from '../../../services/reservas.service.ts'
import { obterMeuPerfil } from '../../../services/usuarios.service.ts'
import type { ReservaEntregaResponse } from '../../../types/reserva.ts'

type CodigoDialogState = {
  reserva: ReservaEntregaResponse
  mode: 'retirada' | 'entrega'
} | null

export function MinhasEntregasPage() {
  useDocumentTitle('Meus transportes')
  const { showToast } = useToast()

  const [codigoDialog, setCodigoDialog] = useState<CodigoDialogState>(null)
  const [submitting, setSubmitting] = useState(false)
  const [codigoError, setCodigoError] = useState<string | null>(null)

  const fetcher = useCallback(async () => {
    const [entregasResp, perfilResp] = await Promise.all([
      listarMinhasEntregasTransportador(),
      obterMeuPerfil().catch(() => null),
    ])
    return { entregas: entregasResp, perfil: perfilResp }
  }, [])

  const { data, loading, error, reload: loadEntregas } = useAsyncResource(fetcher, [])
  const entregas = data?.entregas ?? []
  const contaSuspensa = data?.perfil?.situacaoConta === 'SUSPENSO'

  const handleConfirmCode = async (codigo: string) => {
    if (!codigoDialog) return
    setSubmitting(true)
    setCodigoError(null)
    try {
      if (codigoDialog.mode === 'retirada') {
        await confirmarRetiradaTransporte(codigoDialog.reserva.id, { codigo })
        showToast({
          variant: 'success',
          title: `Retirada da Reserva #${codigoDialog.reserva.id} confirmada`,
          description:
            'Transporte em andamento! O código de entrega com validade de 24h foi enviado para o receptor.',
        })
      } else {
        await confirmarEntregaTransporte(codigoDialog.reserva.id, { codigo })
        showToast({
          variant: 'success',
          title: `Entrega da Reserva #${codigoDialog.reserva.id} concluída`,
          description: 'Operação finalizada com sucesso.',
        })
      }
      setCodigoDialog(null)
      loadEntregas()
    } catch (err) {
      const normalized = normalizeApiError(err)
      setCodigoError(normalized.message)
      if (normalized.status === 403) {
        loadEntregas()
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Meus transportes' }]} />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Meus transportes</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Acompanhe os transportes assumidos por você, consulte os contatos para combinação
              externa e confirme retirada e entrega com os códigos de segurança.
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button asChild variant="secondary" leftIcon={<History className="h-4 w-4" />}>
              <Link to="/app/historico-transportes">Histórico</Link>
            </Button>
            <Button asChild variant="transportador">
              <Link to="/app/transportes-disponiveis">Transportes disponíveis</Link>
            </Button>
          </div>
        </div>
      </div>

      {contaSuspensa ? (
        <InfoBanner tone="danger" title="Conta suspensa e sob investigação">
          <span className="inline-flex items-center gap-1.5">
            <ShieldAlert className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span>
              {data?.perfil?.motivoSuspensao ||
                'Sua conta foi suspensa automaticamente após expiração do prazo de entrega de 24 horas.'}
            </span>
          </span>
        </InfoBanner>
      ) : (
        <InfoBanner tone="info">
          Aguarde o doador autorizar o início para obter o código de retirada. Após confirmar a
          retirada, o receptor receberá o código de entrega com validade de <strong>24 horas</strong>.
        </InfoBanner>
      )}

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadEntregas} /> : null}

      {!loading && !error && entregas.length === 0 ? (
        <EmptyState
          icon={<Truck className="h-6 w-6" />}
          title="Você ainda não assumiu transportes"
          description="Consulte as doações que aguardam transporte e aceite uma operação."
          action={
            <Button asChild variant="transportador">
              <Link to="/app/transportes-disponiveis">Ver transportes disponíveis</Link>
            </Button>
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
              showTimeline
              actions={
                <>
                  {reserva.situacao === 'AGUARDANDO_AUTORIZACAO_DOADOR' ? (
                    <span className="text-xs font-medium text-ink-secondary">
                      Aguardando autorização do doador para gerar o código de retirada.
                    </span>
                  ) : null}

                  {reserva.situacao === 'AGUARDANDO_CONFIRMACAO_RETIRADA' ? (
                    <Button
                      variant="transportador"
                      size="sm"
                      leftIcon={<KeyRound className="h-4 w-4" />}
                      onClick={() => {
                        setCodigoError(null)
                        setCodigoDialog({ reserva, mode: 'retirada' })
                      }}
                    >
                      Informar código de retirada
                    </Button>
                  ) : null}

                  {reserva.situacao === 'EM_TRANSPORTE' ? (
                    <Button
                      variant="transportador"
                      size="sm"
                      leftIcon={<CheckCircle2 className="h-4 w-4" />}
                      onClick={() => {
                        setCodigoError(null)
                        setCodigoDialog({ reserva, mode: 'entrega' })
                      }}
                    >
                      Informar código de entrega
                    </Button>
                  ) : null}

                  {reserva.situacao === 'CONCLUIDA' ? (
                    <span className="text-xs font-medium text-success">
                      Operação concluída (somente leitura)
                    </span>
                  ) : null}
                </>
              }
            />
          ))}
        </div>
      ) : null}

      <ConfirmarCodigoDialog
        open={Boolean(codigoDialog)}
        mode={codigoDialog?.mode ?? 'retirada'}
        reservaId={codigoDialog?.reserva.id ?? null}
        loading={submitting}
        errorMessage={codigoError}
        onConfirm={handleConfirmCode}
        onClose={() => {
          if (!submitting) {
            setCodigoDialog(null)
            setCodigoError(null)
          }
        }}
      />
    </div>
  )
}
