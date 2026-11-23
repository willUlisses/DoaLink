import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowLeft,
  BookmarkCheck,
  Building2,
  Calendar,
  Mail,
  MapPin,
  Package,
  Phone,
  ShieldAlert,
  User,
} from 'lucide-react'
import { useCallback, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
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
import {
  LABELS_CATEGORIA_ITEM,
  LABELS_SITUACAO_OFERTA,
  LABELS_SITUACAO_RESERVA,
  SIGLAS_UNIDADE,
} from '../../../utils/labels.ts'
import { formatarTelefone } from '../../../utils/masks.ts'

export function OfertaDetalhePage() {
  const { id } = useParams()
  const ofertaId = Number(id)
  const validId = Number.isFinite(ofertaId) && ofertaId > 0
  useDocumentTitle(validId ? `Oferta #${ofertaId}` : 'Detalhes da oferta')

  const { user } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const fromPath = (location.state as { from?: string } | null)?.from

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

  const possuiEnderecoRetirada = Boolean(
    oferta?.localRetirada && (oferta.localRetirada.logradouro || oferta.localRetirada.cidade),
  )

  const handleReservar = async (values: CriarReservaFormValues) => {
    if (!oferta) return
    setReserveError(null)
    const precisaTransporte = possuiEnderecoRetirada ? values.precisaTransporte : false
    try {
      const reserva = await reservarOferta({
        ofertaId: oferta.id,
        precisaTransporte,
      })
      showToast({
        variant: 'success',
        title: `Reserva #${reserva.id} confirmada`,
        description: precisaTransporte
          ? 'A reserva está aguardando vínculo de um transportador.'
          : 'Retirada registrada. Entre em contato com o doador para combinar os detalhes.',
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
  const backTarget =
    user?.tipoUsuario === 'DOADOR'
      ? fromPath === '/app/reservas-ofertas'
        ? { to: '/app/reservas-ofertas', label: 'Reservas recebidas' }
        : { to: '/app/minhas-ofertas', label: 'Minhas ofertas' }
      : user?.tipoUsuario === 'TRANSPORTADOR'
        ? fromPath === '/app/entregas-disponiveis'
          ? { to: '/app/entregas-disponiveis', label: 'Aguardando transporte' }
          : { to: '/app/minhas-entregas', label: 'Minhas entregas' }
        : fromPath === '/app/minhas-reservas'
          ? { to: '/app/minhas-reservas', label: 'Minhas reservas' }
          : { to: '/app/ofertas', label: 'Ofertas disponíveis' }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[
            { label: 'Painel', to: '/app' },
            { label: backTarget.label, to: backTarget.to },
            { label: Number.isFinite(ofertaId) ? `Oferta #${ofertaId}` : 'Detalhes' },
          ]}
        />
        <Link
          to={backTarget.to}
          className="inline-flex items-center gap-1.5 text-sm font-medium text-ink-secondary hover:text-ink"
        >
          <ArrowLeft className="h-4 w-4" aria-hidden="true" />
          <span>Voltar para {backTarget.label}</span>
        </Link>
      </div>

      {loading ? <CardListSkeleton count={1} /> : null}

      {!loading && isForbidden ? (
        <Card className="text-center" padding="lg">
          <div className="mx-auto mb-3 flex h-11 w-11 items-center justify-center rounded-full bg-warning-soft text-warning">
            <ShieldAlert className="h-5 w-5" aria-hidden="true" />
          </div>
          <h1 className="text-xl font-bold text-ink">Acesso restrito a esta oferta</h1>
          <p className="mt-2 text-sm text-ink-secondary">{errorMessage}</p>
          <div className="mt-5 flex justify-center">
            <Button asChild variant="secondary">
              <Link to={backTarget.to}>Ir para {backTarget.label}</Link>
            </Button>
          </div>
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

            <div className="grid grid-cols-1 gap-4 rounded-card border border-border bg-canvas p-4 sm:grid-cols-2">
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
                    {oferta.doador.email ? (
                      <>
                        <p className="text-xs text-ink-secondary">{oferta.doador.email}</p>
                        {oferta.doador.telefone ? (
                          <p className="text-xs text-ink-secondary">
                            {formatarTelefone(oferta.doador.telefone)}
                          </p>
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

            {oferta.receptor ? (
              <div className="rounded-card border border-receptor-border bg-receptor-soft/60 p-4 space-y-1.5">
                <p className="text-xs font-semibold uppercase tracking-wide text-receptor-aa">
                  ONG responsável pela reserva
                </p>
                <p className="flex items-center gap-2 text-sm font-semibold text-ink">
                  <Building2 className="h-4 w-4 shrink-0 text-receptor-aa" aria-hidden="true" />
                  <span>{oferta.receptor.nome}</span>
                </p>
                <p className="flex items-center gap-2 text-xs text-ink-secondary">
                  <Mail className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
                  <span>{oferta.receptor.email}</span>
                </p>
                {oferta.receptor.telefone ? (
                  <p className="flex items-center gap-2 text-xs text-ink-secondary">
                    <Phone className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
                    <span>{formatarTelefone(oferta.receptor.telefone)}</span>
                  </p>
                ) : null}
              </div>
            ) : null}

            <div>
              <h2 className="mb-3 flex items-center gap-2 text-base font-bold text-ink">
                <Package className="h-4 w-4 text-doador-aa" aria-hidden="true" />
                <span>Itens da oferta ({oferta.itens.length})</span>
              </h2>
              <ul className="space-y-2">
                {oferta.itens.map((item) => (
                  <li
                    key={item.id}
                    className="flex flex-col justify-between gap-2 rounded-button border border-border bg-surface p-3.5 sm:flex-row sm:items-center"
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
                      Ao confirmar a reserva, a oferta muda para{' '}
                      <strong>{LABELS_SITUACAO_OFERTA.RESERVADA}</strong> e os dados completos ficam
                      disponíveis na sua lista de reservas.
                    </p>

                    {possuiEnderecoRetirada ? (
                      <Checkbox
                        label="Preciso de transporte para esta entrega"
                        description="Se marcado, a entrega ficará visível para transportadores assumirem. Se desmarcado, sua ONG fará a retirada direta."
                        {...register('precisaTransporte')}
                      />
                    ) : (
                      <InfoBanner tone="info">
                        Esta oferta não possui endereço de retirada cadastrado. A reserva avançará
                        diretamente para{' '}
                        <strong>{LABELS_SITUACAO_RESERVA.AGUARDANDO_RETIRADA}</strong> para que sua
                        ONG entre em contato com o doador e combine a retirada ou entrega.
                      </InfoBanner>
                    )}

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
                      Esta oferta está com situação{' '}
                      <strong>{LABELS_SITUACAO_OFERTA[oferta.situacao]}</strong> e não pode mais ser
                      reservada.
                    </InfoBanner>
                  </div>
                )}
              </Card>
            ) : null}

            {user?.tipoUsuario === 'DOADOR' ? (
              <Card className="space-y-3">
                <h2 className="text-base font-bold text-ink">Gestão da oferta</h2>
                <InfoBanner tone="info">
                  Após publicada, uma oferta não pode ser editada. Caso precise alterar alguma
                  informação, cancele a oferta enquanto estiver com situação{' '}
                  <strong>{LABELS_SITUACAO_OFERTA.DISPONIVEL}</strong> e publique uma nova.
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
        description={`Esta ação altera a situação da oferta para "${LABELS_SITUACAO_OFERTA.CANCELADA}" e não pode ser desfeita.`}
        confirmLabel="Sim, cancelar oferta"
        cancelLabel="Manter oferta"
        loading={canceling}
        onConfirm={handleCancelarOferta}
        onClose={() => setCancelDialogOpen(false)}
      />
    </div>
  )
}
