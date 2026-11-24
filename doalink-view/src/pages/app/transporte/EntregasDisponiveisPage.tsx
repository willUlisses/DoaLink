import { ExternalLink, Package, ShieldAlert, Truck } from 'lucide-react'
import { useCallback, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
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
  aceitarTransporte,
  listarReservasAguardandoTransporte,
} from '../../../services/reservas.service.ts'
import { obterMeuPerfil } from '../../../services/usuarios.service.ts'
import type { ReservaEntregaResponse } from '../../../types/reserva.ts'

export function EntregasDisponiveisPage() {
  useDocumentTitle('Transportes disponíveis')
  const { showToast } = useToast()
  const navigate = useNavigate()

  const [reservaParaAceitar, setReservaParaAceitar] = useState<ReservaEntregaResponse | null>(null)
  const [accepting, setAccepting] = useState(false)

  const fetcher = useCallback(async () => {
    const [reservasResp, perfilResp] = await Promise.all([
      listarReservasAguardandoTransporte(),
      obterMeuPerfil().catch(() => null),
    ])
    return { reservas: reservasResp, perfil: perfilResp }
  }, [])

  const { data, loading, error, reload: loadData } = useAsyncResource(fetcher, [])
  const reservas = data?.reservas ?? []
  const contaSuspensa = data?.perfil?.situacaoConta === 'SUSPENSO'

  const handleConfirmAccept = async () => {
    if (!reservaParaAceitar) return
    setAccepting(true)
    try {
      await aceitarTransporte(reservaParaAceitar.id)
      showToast({
        variant: 'success',
        title: `Transporte da Reserva #${reservaParaAceitar.id} aceito`,
        description:
          'Os contatos e endereços completos foram liberados em Meus transportes. Aguarde a autorização do doador.',
      })
      setReservaParaAceitar(null)
      navigate('/app/meus-transportes')
    } catch (err) {
      showToast({
        variant: 'error',
        title: 'Não foi possível aceitar o transporte',
        description: normalizeApiError(err).message,
      })
      setReservaParaAceitar(null)
      loadData()
    } finally {
      setAccepting(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[{ label: 'Painel', to: '/app' }, { label: 'Transportes disponíveis' }]}
        />
        <h1 className="text-2xl font-bold text-ink">Transportes disponíveis</h1>
        <p className="mt-1 text-sm text-ink-secondary">
          Visualize as doações que aguardam transporte solidário. Ao aceitar, os dados de contato e
          endereços completos serão liberados para combinar a retirada e entrega fora da plataforma.
        </p>
      </div>

      {contaSuspensa ? (
        <InfoBanner tone="danger" title="Conta suspensa para investigação">
          <span className="inline-flex items-center gap-1.5">
            <ShieldAlert className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span>
              {data?.perfil?.motivoSuspensao ||
                'Sua conta está temporariamente suspensa devido à expiração de um prazo de entrega. Você não pode aceitar novos transportes.'}
            </span>
          </span>
        </InfoBanner>
      ) : (
        <InfoBanner tone="info">
          Por privacidade, apenas cidade e UF são exibidos antes do aceite. Após assumir um
          transporte, entre em contato com o doador e o receptor para combinar os detalhes e aguarde
          a autorização do doador.
        </InfoBanner>
      )}

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadData} /> : null}

      {!loading && !error && reservas.length === 0 ? (
        <EmptyState
          icon={<Package className="h-6 w-6" />}
          title="Nenhum transporte disponível no momento"
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
                <>
                  {reserva.ofertaId ? (
                    <Button
                      asChild
                      variant="secondary"
                      size="sm"
                      leftIcon={<ExternalLink className="h-3.5 w-3.5" />}
                    >
                      <Link
                        to={`/app/ofertas/${reserva.ofertaId}`}
                        state={{ from: '/app/transportes-disponiveis' }}
                      >
                        Ver detalhes
                      </Link>
                    </Button>
                  ) : null}
                  <Button
                    variant="transportador"
                    size="sm"
                    disabled={contaSuspensa}
                    leftIcon={<Truck className="h-4 w-4" />}
                    onClick={() => setReservaParaAceitar(reserva)}
                  >
                    Aceitar transporte
                  </Button>
                </>
              }
            />
          ))}
        </div>
      ) : null}

      <ConfirmDialog
        open={Boolean(reservaParaAceitar)}
        title={`Aceitar transporte da Reserva #${reservaParaAceitar?.id ?? ''}?`}
        description="Ao aceitar, você assume o compromisso de combinar a retirada e entrega com o doador e o receptor, utilizando os códigos de segurança para confirmar cada etapa."
        confirmLabel="Confirmar aceite"
        cancelLabel="Voltar"
        confirmVariant="transportador"
        loading={accepting}
        onConfirm={handleConfirmAccept}
        onClose={() => !accepting && setReservaParaAceitar(null)}
      />
    </div>
  )
}
