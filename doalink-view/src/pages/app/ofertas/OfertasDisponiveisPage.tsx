import { Filter, Gift, SearchX } from 'lucide-react'
import { useCallback, useState } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { Input } from '../../../components/ui/Input.tsx'
import { Pagination } from '../../../components/ui/Pagination.tsx'
import { Select } from '../../../components/ui/Select.tsx'
import { OfertaCard } from '../../../features/ofertas/OfertaCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useAuth } from '../../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { listarOfertasDisponiveis } from '../../../services/ofertas.service.ts'
import { TIPO_ITEM_DOACAO, UFS_BRASIL, type TipoItemDoacao } from '../../../types/enums.ts'
import { LABELS_CATEGORIA_ITEM } from '../../../utils/labels.ts'

const CATEGORIA_OPTIONS = Object.values(TIPO_ITEM_DOACAO).map((cat) => ({
  value: cat,
  label: LABELS_CATEGORIA_ITEM[cat],
}))

const UF_OPTIONS = UFS_BRASIL.map((uf) => ({ value: uf, label: uf }))

export function OfertasDisponiveisPage() {
  useDocumentTitle('Ofertas disponíveis')
  const { user } = useAuth()

  const [categoria, setCategoria] = useState<TipoItemDoacao | ''>('')
  const [cidadeInput, setCidadeInput] = useState('')
  const [estado, setEstado] = useState('')
  const [appliedFiltros, setAppliedFiltros] = useState<{
    categoria: TipoItemDoacao | ''
    cidade: string
    estado: string
  }>({ categoria: '', cidade: '', estado: '' })

  const [page, setPage] = useState(0)

  const hasActiveFilters = Boolean(
    appliedFiltros.categoria || appliedFiltros.cidade || appliedFiltros.estado,
  )

  const fetcher = useCallback(
    () =>
      listarOfertasDisponiveis({
        page,
        size: 12,
        categoria: appliedFiltros.categoria,
        cidade: appliedFiltros.cidade,
        estado: appliedFiltros.estado,
      }),
    [page, appliedFiltros],
  )

  const { data, loading, error, reload: fetchOfertas } = useAsyncResource(fetcher, [
    page,
    appliedFiltros.categoria,
    appliedFiltros.cidade,
    appliedFiltros.estado,
  ])

  const handleApplyFilters = (event: React.FormEvent) => {
    event.preventDefault()
    setPage(0)
    setAppliedFiltros({
      categoria,
      cidade: cidadeInput.trim(),
      estado,
    })
  }

  const handleClearFilters = () => {
    setCategoria('')
    setCidadeInput('')
    setEstado('')
    setPage(0)
    setAppliedFiltros({ categoria: '', cidade: '', estado: '' })
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Ofertas disponíveis' }]} />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Ofertas disponíveis</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Consulte ofertas publicadas com situação disponível. Apenas cidade e UF são exibidos
              antes da reserva.
            </p>
          </div>
          {user?.tipoUsuario === 'DOADOR' ? (
            <Link
              to="/app/ofertas/nova"
              className="inline-flex min-h-10 items-center justify-center rounded-[8px] bg-doador-aa px-4 py-2 text-sm font-semibold text-white hover:bg-doador-hover"
            >
              Publicar oferta
            </Link>
          ) : null}
        </div>
      </div>

      {user?.tipoUsuario === 'TRANSPORTADOR' ? (
        <InfoBanner tone="info">
          Como Transportador, você pode consultar a listagem geral de ofertas disponíveis, mas o
          detalhamento individual (`GET /ofertas/{'{id}'}`) e o endereço completo são liberados
          apenas quando a oferta é reservada e vinculada a você. Para assumir entregas, acesse{' '}
          <Link to="/app/entregas-disponiveis" className="font-semibold text-ink underline">
            Aguardando transporte
          </Link>
          .
        </InfoBanner>
      ) : null}

      <Card padding="sm">
        <form
          onSubmit={handleApplyFilters}
          className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4 lg:items-end"
        >
          <Select
            label="Categoria"
            placeholder="Todas as categorias"
            options={CATEGORIA_OPTIONS}
            value={categoria}
            onChange={(e) => setCategoria(e.target.value as TipoItemDoacao | '')}
          />
          <Input
            label="Cidade"
            placeholder="Ex.: Campinas"
            value={cidadeInput}
            onChange={(e) => setCidadeInput(e.target.value)}
          />
          <Select
            label="UF"
            placeholder="Todos os estados"
            options={UF_OPTIONS}
            value={estado}
            onChange={(e) => setEstado(e.target.value)}
          />
          <div className="flex gap-2">
            <Button
              type="submit"
              variant="primary"
              className="flex-1"
              leftIcon={<Filter className="h-4 w-4" />}
            >
              Filtrar
            </Button>
            {hasActiveFilters ? (
              <Button type="button" variant="secondary" onClick={handleClearFilters}>
                Limpar
              </Button>
            ) : null}
          </div>
        </form>
      </Card>

      {hasActiveFilters ? (
        <InfoBanner tone="info">
          Os filtros da API são aplicados sobre cada página retornada. Caso não encontre itens nesta
          página, navegue pelas páginas ou limpe os filtros.
        </InfoBanner>
      ) : null}

      {loading ? <CardListSkeleton count={6} /> : null}

      {!loading && error ? <ErrorState message={error.message} onRetry={fetchOfertas} /> : null}

      {!loading && !error && data && data.content.length === 0 ? (
        <EmptyState
          icon={
            hasActiveFilters ? (
              <SearchX className="h-6 w-6" />
            ) : (
              <Gift className="h-6 w-6" />
            )
          }
          title={
            hasActiveFilters
              ? 'Nenhuma oferta encontrada nesta página com esses filtros'
              : 'Nenhuma oferta disponível no momento'
          }
          description={
            hasActiveFilters
              ? 'Tente ajustar categoria, cidade ou UF, ou avance para outra página.'
              : 'Quando doadores publicarem novas ofertas disponíveis, elas aparecerão aqui.'
          }
          action={
            hasActiveFilters ? (
              <Button variant="secondary" onClick={handleClearFilters}>
                Limpar filtros
              </Button>
            ) : undefined
          }
        />
      ) : null}

      {!loading && !error && data && data.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {data.content.map((oferta) => (
              <OfertaCard
                key={oferta.id}
                oferta={oferta}
                variant="public"
                actions={
                  user?.tipoUsuario === 'TRANSPORTADOR' ? null : (
                    <Link
                      to={`/app/ofertas/${oferta.id}`}
                      className="inline-flex min-h-9 items-center justify-center rounded-[8px] border border-border bg-surface px-3 py-1.5 text-sm font-semibold text-ink hover:bg-surface-subtle"
                    >
                      {user?.tipoUsuario === 'RECEPTOR' ? 'Ver e reservar' : 'Ver detalhes'}
                    </Link>
                  )
                }
              />
            ))}
          </div>

          <Pagination
            page={data.number}
            totalPages={data.totalPages}
            onPageChange={setPage}
          />
        </>
      ) : null}
    </div>
  )
}
