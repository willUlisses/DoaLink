import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, BookmarkCheck, Calendar, MapPin, Package, ShieldAlert, User } from 'lucide-react'
import { useCallback, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ConfirmDialog } from '../../../components/feedback/ConfirmDialog.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Badge } from '../../../components/ui/Badge.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { Checkbox } from '../../../components/ui/Checkbox.tsx'
import { StatusBadge } from '../../../components/ui/StatusBadge.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useAuth } from '../../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { useToast } from '../../../hooks/useToast.ts'
import { criarReservaSchema, type CriarReservaFormValues } from '../../../schemas/reserva.schema.ts'
import { isNormalizedApiError, normalizeApiError } from '../../../services/api.ts'
import { buscarOfertaPorId, cancelarOferta } from '../../../services/ofertas.service.ts'
import { reservarOferta } from '../../../services/reservas.service.ts'
import {
  formatarDataHora,
  formatarEnderecoCompleto,
  formatarEnderecoResumo,
  formatarQuantidade,
} from '../../../utils/formatters.ts'
import { LABELS_CATEGORIA_ITEM, SIGLAS_UNIDADE } from '../../../utils/labels.ts'

export function OfertaDetalhePage() {
  const { id } = useParams()
  const ofertaId = Number(id)
  const validId = Number.isFinite(ofertaId) && ofertaId > 0
  useDocumentTitle(validId ? `Oferta #${ofertaId}` : 'Detalhes da oferta')

  const { user } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()

  const [reserveError, setReserveError] = useState<string | null>(null)
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false)
  const [canceling, setCanceling] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { isSubmitting: reserving },
  } = useForm<CriarReservaFormValues>({
    resolver: zodResolver(criarReservaSchema),
    defaultValues: {
      precisaTransporte: true,
    },
  })

  const fetcher = useCallback(() => buscarOfertaPorId(ofertaId), [ofertaId])
  const { data: oferta, loading, error, reload: loadOferta } = useAsyncResource(
    fetcher,
    [ofertaId],
    validId,
  )

  const isForbidden = error?.status === 403
  const errorMessage = !validId ? 'Identificador de oferta inválido.' : error?.message ?? null

  const handleReservar = async (values: CriarReservaFormValues) => {
    if (!oferta) return
    setReserveError(null)
    try {
      const reserva = await reservarOferta({
        ofertaId: oferta.id,
        precisaTransporte: values.precisaTransporte,
      })
      showToast({
        variant: 'success',
        title: `Reserva #${reserva.id} confirmada`,
        description: values.precisaTransporte
          ? 'A reserva está aguardando vínculo de um transportador.'
          : 'Retirada direta registrada. Confira o endereço completo em Minhas reservas.',
      })
      navigate('/app/minhas-reservas')
    } catch (err) {
      const msg = isNormalizedApiError(err) ? err.message : normalizeApiError(err).message
      setReserveError(msg)
    }
  }

  const handleCancelarOferta = async () => {
    if (!oferta) return
    setCanceling(true)
    try {
      await cancelarOferta(oferta.id)
      showToast({
        variant: 'info',
        title: 'Oferta cancelada',
        description: `A oferta #${oferta.id} foi cancelada com sucesso.`,
      })
      setCancelDialogOpen(false)
      loadOferta()
    } catch (err) {
      showToast({
        variant: 'error',
        title: 'Não foi possível cancelar',
        description: normalizeApiError(err).message,
      })
    } finally {
      setCanceling(false)
    }
  }

  const isEnderecoCompleto = Boolean(oferta?.localRetirada?.logradouro)

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[
            { label: 'Painel', to: '/app' },
            { label: 'Ofertas disponíveis', to: '/app/ofertas' },
            { label: Number.isFinite(ofertaId) ? `Oferta #${ofertaId}` : 'Detalhes' },
          ]}
        />
        <Link
          to="/app/ofertas"
          className="inline-flex items-center gap-1.5 text-sm font-medium text-ink-secondary hover:text-ink"
        >
          <ArrowLeft className="h-4 w-4" aria-hidden="true" />
          <span>Voltar para ofertas</span>
        </Link>
      </div>

      {loading ? <CardListSkeleton count={1} /> : null}

      {!loading && isForbidden ? (
        <Card className="text-center" padding="lg">
          <div className="mx-auto mb-3 flex h-11 w-11 items-center justify-center rounded-full bg-warning-soft text-warning">
            <ShieldAlert className="h-5 w-5" aria-hidden="true" />
          </div>
          <h1 className="text-xl font-bold text-ink">Detalhe restrito para Transportador</h1>
          <p className="mt-2 text-sm text-ink-secondary">{errorMessage}</p>
        </Card>
      ) : null}

      {!loading && errorMessage && !isForbidden ? (
        <ErrorState message={errorMessage} onRetry={loadOferta} />
      ) : null}

      {!loading && !errorMessage && oferta ? (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <Card className="lg:col-span-2 space-y-5 border-t-4 border-t-doador">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h1 className="text-2xl font-bold text-ink">Oferta #{oferta.id}</h1>
                <p className="mt-1 flex items-center gap-1.5 text-xs text-ink-muted">
                  <Calendar className="h-3.5 w-3.5" aria-hidden="true" />
                  <span>Publicada em {formatarDataHora(oferta.criadoEm)}</span>
                </p>
              </div>
              <StatusBadge situacao={oferta.situacao} />
            </div>

            <div className="grid grid-cols-1 gap-4 rounded-[12px] border border-border bg-canvas p-4 sm:grid-cols-2">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                  Doador
                </p>
                {oferta.doador ? (
                  <div className="mt-1 space-y-0.5 text-sm">
                    <p className="flex items-center gap-1.5 font-semibold text-ink">
                      <User className="h-4 w-4 text-doador-aa" aria-hidden="true" />
                      <span>{oferta.doador.nome}</span>
                    </p>
                    {isEnderecoCompleto ? (
                      <>
                        <p className="text-xs text-ink-secondary">{oferta.doador.email}</p>
                        {oferta.doador.telefone ? (
                          <p className="text-xs text-ink-secondary">{oferta.doador.telefone}</p>
                        ) : null}
                      </>
                    ) : (
                      <p className="text-xs text-ink-muted">
                        Contato completo visível após vínculo de reserva.
                      </p>
                    )}
                  </div>
                ) : (
                  <p className="mt-1 text-xs text-ink-secondary">Não informado</p>
                )}
              </div>

              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                  Local de retirada
                </p>
                <p className="mt-1 flex items-start gap-1.5 text-sm text-ink">
                  <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-doador-aa" aria-hidden="true" />
                  <span>
                    {oferta.localRetirada
                      ? isEnderecoCompleto
                        ? formatarEnderecoCompleto(oferta.localRetirada)
                        : `${formatarEnderecoResumo(oferta.localRetirada)} (endereço completo liberado após reserva)`
                      : 'Local de retirada a combinar entre as partes'}
                  </span>
                </p>
              </div>
            </div>

            <div>
              <h2 className="mb-3 flex items-center gap-2 text-base font-bold text-ink">
                <Package className="h-4 w-4 text-doador-aa" aria-hidden="true" />
                <span>Itens da oferta ({oferta.itens.length})</span>
              </h2>
              <ul className="space-y-2">
                {oferta.itens.map((item) => (
                  <li
                    key={item.id}
                    className="flex flex-col justify-between gap-2 rounded-[10px] border border-border bg-surface p-3.5 sm:flex-row sm:items-center"
                  >
                    <div>
                      <p className="text-sm font-semibold text-ink">{item.descricao}</p>
                      <p className="text-xs text-ink-secondary">
                        Categoria: {LABELS_CATEGORIA_ITEM[item.tipoItemDoacao]}
                      </p>
                    </div>
                    <Badge tone="doador">
                      {formatarQuantidade(item.quantidade)} {SIGLAS_UNIDADE[item.tipoUnidade]}
                    </Badge>
                  </li>
                ))}
              </ul>
            </div>
          </Card>

          <div className="space-y-4">
            {user?.tipoUsuario === 'RECEPTOR' ? (
              <Card>
                <h2 className="text-base font-bold text-ink">Reservar esta oferta</h2>
                {oferta.situacao === 'DISPONIVEL' ? (
                  <form onSubmit={handleSubmit(handleReservar)} className="mt-3 space-y-4">
                    <p className="text-xs leading-relaxed text-ink-secondary">
                      Ao confirmar a reserva, a oferta muda para <strong>RESERVADA</strong> e o
                      endereço completo de retirada fica disponível na sua lista de reservas.
                    </p>

                    <Checkbox
                      label="Preciso de transporte para esta entrega"
                      description="Se marcado, a entrega ficará visível para transportadores assumirem. Se desmarcado, sua ONG fará a retirada direta."
                      {...register('precisaTransporte')}
                    />

                    {reserveError ? (
                      <InfoBanner tone="danger">{reserveError}</InfoBanner>
                    ) : null}

                    <Button
                      type="submit"
                      variant="receptor"
                      size="lg"
                      loading={reserving}
                      loadingLabel="Reservando..."
                      leftIcon={<BookmarkCheck className="h-4 w-4" />}
                      className="w-full"
                    >
                      Reservar oferta
                    </Button>
                  </form>
                ) : (
                  <div className="mt-3">
                    <InfoBanner tone="info">
                      Esta oferta está com situação <strong>{oferta.situacao}</strong> e não pode
                      mais ser reservada.
                    </InfoBanner>
                  </div>
                )}
              </Card>
            ) : null}

            {user?.tipoUsuario === 'DOADOR' ? (
              <Card className="space-y-3">
                <h2 className="text-base font-bold text-ink">Gestão da oferta</h2>
                <InfoBanner tone="info">
                  A edição de ofertas após a publicação não é suportada pela API. Caso necessário,
                  cancele uma oferta ainda disponível e publique uma nova.
                </InfoBanner>
                {oferta.situacao === 'DISPONIVEL' ? (
                  <Button
                    variant="destructive"
                    className="w-full"
                    onClick={() => setCancelDialogOpen(true)}
                  >
                    Cancelar oferta
                  </Button>
                ) : null}
              </Card>
            ) : null}
          </div>
        </div>
      ) : null}

      <ConfirmDialog
        open={cancelDialogOpen}
        title="Cancelar oferta de doação?"
        description="Esta ação altera a situação da oferta para CANCELADA e não pode ser desfeita."
        confirmLabel="Sim, cancelar oferta"
        cancelLabel="Manter oferta"
        loading={canceling}
        onConfirm={handleCancelarOferta}
        onClose={() => setCancelDialogOpen(false)}
      />
    </div>
  )
}
