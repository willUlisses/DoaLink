import { Package, PlusCircle, Trash2 } from 'lucide-react'
import { useCallback, useState } from 'react'
import { Link } from 'react-router-dom'
import { ConfirmDialog } from '../../../components/feedback/ConfirmDialog.tsx'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { OfertaCard } from '../../../features/ofertas/OfertaCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { useToast } from '../../../hooks/useToast.ts'
import { normalizeApiError } from '../../../services/api.ts'
import { cancelarOferta, listarMinhasOfertas } from '../../../services/ofertas.service.ts'
import type { OfertaDoacaoResponse } from '../../../types/oferta.ts'

export function MinhasOfertasPage() {
  useDocumentTitle('Minhas ofertas')
  const { showToast } = useToast()

  const [ofertaParaCancelar, setOfertaParaCancelar] = useState<OfertaDoacaoResponse | null>(null)
  const [canceling, setCanceling] = useState(false)

  const fetcher = useCallback(() => listarMinhasOfertas(), [])
  const { data, loading, error, reload: loadOfertas } = useAsyncResource(fetcher, [])
  const ofertas = data ?? []

  const handleConfirmCancel = async () => {
    if (!ofertaParaCancelar) return
    setCanceling(true)
    try {
      await cancelarOferta(ofertaParaCancelar.id)
      showToast({
        variant: 'info',
        title: 'Oferta cancelada',
        description: `A oferta #${ofertaParaCancelar.id} foi marcada como cancelada.`,
      })
      setOfertaParaCancelar(null)
      loadOfertas()
    } catch (err) {
      showToast({
        variant: 'error',
        title: 'Erro ao cancelar oferta',
        description: normalizeApiError(err).message,
      })
    } finally {
      setCanceling(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Minhas ofertas' }]} />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Minhas ofertas</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Acompanhe todas as ofertas publicadas por você e cancele ofertas ainda disponíveis.
            </p>
          </div>
          <Link
            to="/app/ofertas/nova"
            className="inline-flex min-h-10 items-center justify-center gap-2 rounded-[8px] bg-doador-aa px-4 py-2 text-sm font-semibold text-white hover:bg-doador-hover"
          >
            <PlusCircle className="h-4 w-4" aria-hidden="true" />
            <span>Nova oferta</span>
          </Link>
        </div>
      </div>

      <InfoBanner tone="info">
        A edição de ofertas após a criação não está disponível na API. Apenas ofertas com situação{' '}
        <strong>DISPONIVEL</strong> podem ser canceladas.
      </InfoBanner>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={loadOfertas} /> : null}

      {!loading && !error && ofertas.length === 0 ? (
        <EmptyState
          icon={<Package className="h-6 w-6" />}
          title="Você ainda não publicou ofertas"
          description="Registre sua primeira oferta com um ou mais itens para que ONGs receptoras possam reservar."
          action={
            <Link
              to="/app/ofertas/nova"
              className="inline-flex min-h-10 items-center justify-center gap-2 rounded-[8px] bg-doador-aa px-4 py-2 text-sm font-semibold text-white hover:bg-doador-hover"
            >
              Publicar primeira oferta
            </Link>
          }
        />
      ) : null}

      {!loading && !error && ofertas.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
          {ofertas.map((oferta) => (
            <OfertaCard
              key={oferta.id}
              oferta={oferta}
              variant="owner"
              actions={
                <>
                  <Link
                    to={`/app/ofertas/${oferta.id}`}
                    className="inline-flex min-h-9 items-center justify-center rounded-[8px] border border-border bg-surface px-3 py-1.5 text-sm font-semibold text-ink hover:bg-surface-subtle"
                  >
                    Detalhes
                  </Link>
                  {oferta.situacao === 'DISPONIVEL' ? (
                    <Button
                      variant="destructive"
                      size="sm"
                      leftIcon={<Trash2 className="h-3.5 w-3.5" />}
                      onClick={() => setOfertaParaCancelar(oferta)}
                    >
                      Cancelar
                    </Button>
                  ) : null}
                </>
              }
            />
          ))}
        </div>
      ) : null}

      <ConfirmDialog
        open={Boolean(ofertaParaCancelar)}
        title="Confirmar cancelamento da oferta"
        description={`Deseja realmente cancelar a Oferta #${ofertaParaCancelar?.id ?? ''}? Essa ação não pode ser desfeita.`}
        confirmLabel="Confirmar cancelamento"
        cancelLabel="Voltar"
        loading={canceling}
        onConfirm={handleConfirmCancel}
        onClose={() => setOfertaParaCancelar(null)}
      />
    </div>
  )
}
