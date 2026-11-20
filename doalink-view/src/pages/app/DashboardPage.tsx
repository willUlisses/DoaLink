import {
  ArrowRight,
  BookmarkCheck,
  CalendarClock,
  ClipboardList,
  Flame,
  Gift,
  Package,
  PlusCircle,
  Truck,
} from 'lucide-react'
import { useCallback } from 'react'
import { Link } from 'react-router-dom'
import { ErrorState } from '../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../components/feedback/Skeleton.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { ProfileBadge } from '../../components/ui/ProfileBadge.tsx'
import { useAsyncResource } from '../../hooks/useAsyncResource.ts'
import { useAuth } from '../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../hooks/useDocumentTitle.ts'
import { listarCampanhas, listarMinhasCampanhas } from '../../services/campanhas.service.ts'
import { listarMinhasDisponibilidades } from '../../services/disponibilidades.service.ts'
import { listarMinhasNecessidades } from '../../services/necessidades.service.ts'
import { listarMinhasOfertas, listarOfertasDisponiveis } from '../../services/ofertas.service.ts'
import {
  listarMinhasEntregasTransportador,
  listarMinhasReservasReceptor,
  listarReservasAguardandoTransporte,
  listarReservasMinhasOfertasDoador,
} from '../../services/reservas.service.ts'
import { PROFILE_THEME } from '../../utils/theme.ts'

interface DashboardSummary {
  ofertasDisponiveis: number
  campanhasAtivas: number
  minhasOfertas?: number
  minhasOfertasDisponiveis?: number
  reservasDoador?: number
  minhasReservasReceptor?: number
  minhasNecessidades?: number
  minhasCampanhas?: number
  aguardandoTransporte?: number
  minhasEntregas?: number
  minhasDisponibilidades?: number
}

export function DashboardPage() {
  useDocumentTitle('Painel inicial')
  const { user } = useAuth()

  const fetchSummary = useCallback(async (): Promise<DashboardSummary> => {
    if (!user) {
      return { ofertasDisponiveis: 0, campanhasAtivas: 0 }
    }

    if (user.tipoUsuario === 'DOADOR') {
      const [ofertasPage, campanhasPage, minhasOfertas, reservasDoador] = await Promise.all([
        listarOfertasDisponiveis({ page: 0, size: 1 }),
        listarCampanhas({ situacao: 'ATIVA', page: 0, size: 1 }),
        listarMinhasOfertas(),
        listarReservasMinhasOfertasDoador(),
      ])

      return {
        ofertasDisponiveis: ofertasPage.totalElements,
        campanhasAtivas: campanhasPage.totalElements,
        minhasOfertas: minhasOfertas.length,
        minhasOfertasDisponiveis: minhasOfertas.filter((o) => o.situacao === 'DISPONIVEL').length,
        reservasDoador: reservasDoador.length,
      }
    }

    if (user.tipoUsuario === 'RECEPTOR') {
      const [
        ofertasPage,
        campanhasPage,
        minhasReservas,
        minhasNecessidades,
        minhasCampanhas,
      ] = await Promise.all([
        listarOfertasDisponiveis({ page: 0, size: 1 }),
        listarCampanhas({ situacao: 'ATIVA', page: 0, size: 1 }),
        listarMinhasReservasReceptor(),
        listarMinhasNecessidades(),
        listarMinhasCampanhas(),
      ])

      return {
        ofertasDisponiveis: ofertasPage.totalElements,
        campanhasAtivas: campanhasPage.totalElements,
        minhasReservasReceptor: minhasReservas.length,
        minhasNecessidades: minhasNecessidades.length,
        minhasCampanhas: minhasCampanhas.length,
      }
    }

    const [
      ofertasPage,
      campanhasPage,
      aguardando,
      minhasEntregas,
      minhasDisponibilidades,
    ] = await Promise.all([
      listarOfertasDisponiveis({ page: 0, size: 1 }),
      listarCampanhas({ situacao: 'ATIVA', page: 0, size: 1 }),
      listarReservasAguardandoTransporte(),
      listarMinhasEntregasTransportador(),
      listarMinhasDisponibilidades(),
    ])

    return {
      ofertasDisponiveis: ofertasPage.totalElements,
      campanhasAtivas: campanhasPage.totalElements,
      aguardandoTransporte: aguardando.length,
      minhasEntregas: minhasEntregas.length,
      minhasDisponibilidades: minhasDisponibilidades.length,
    }
  }, [user])

  const {
    data: summary,
    loading,
    error,
    reload: loadDashboard,
  } = useAsyncResource(fetchSummary, [user?.id, user?.tipoUsuario], Boolean(user))

  if (!user) return null

  const theme = PROFILE_THEME[user.tipoUsuario]

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="flex items-center gap-2.5">
            <ProfileBadge tipoUsuario={user.tipoUsuario} />
          </div>
          <h1 className="mt-2 text-2xl font-bold text-ink sm:text-3xl">
            Olá, {user.nome}
          </h1>
          <p className="mt-1 text-sm text-ink-secondary">
            Acompanhe sua operação no DoaLink em tempo real.
          </p>
        </div>

        <div className="flex flex-wrap gap-2">
          {user.tipoUsuario === 'DOADOR' ? (
            <Link
              to="/app/ofertas/nova"
              className={`inline-flex min-h-10 items-center gap-2 rounded-[8px] px-4 py-2 text-sm font-semibold text-white transition-colors ${theme.solidBg} ${theme.solidHoverBg}`}
            >
              <PlusCircle className="h-4 w-4" aria-hidden="true" />
              <span>Publicar nova oferta</span>
            </Link>
          ) : null}

          {user.tipoUsuario === 'RECEPTOR' ? (
            <Link
              to="/app/ofertas"
              className={`inline-flex min-h-10 items-center gap-2 rounded-[8px] px-4 py-2 text-sm font-semibold text-white transition-colors ${theme.solidBg} ${theme.solidHoverBg}`}
            >
              <Gift className="h-4 w-4" aria-hidden="true" />
              <span>Explorar ofertas disponíveis</span>
            </Link>
          ) : null}

          {user.tipoUsuario === 'TRANSPORTADOR' ? (
            <Link
              to="/app/entregas-disponiveis"
              className={`inline-flex min-h-10 items-center gap-2 rounded-[8px] px-4 py-2 text-sm font-semibold text-white transition-colors ${theme.solidBg} ${theme.solidHoverBg}`}
            >
              <Truck className="h-4 w-4" aria-hidden="true" />
              <span>Ver entregas aguardando transporte</span>
            </Link>
          ) : null}
        </div>
      </div>

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && error ? (
        <ErrorState message={error.message} onRetry={loadDashboard} />
      ) : null}

      {!loading && !error && summary ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {user.tipoUsuario === 'DOADOR' ? (
            <>
              <Card>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                    Minhas ofertas
                  </span>
                  <Package className="h-4 w-4 text-doador-aa" aria-hidden="true" />
                </div>
                <p className="mt-2 text-3xl font-bold text-ink tabular-nums">
                  {summary.minhasOfertas ?? 0}
                </p>
                <p className="mt-1 text-xs text-ink-secondary">
                  {summary.minhasOfertasDisponiveis ?? 0} disponíveis para reserva
                </p>
                <Link
                  to="/app/minhas-ofertas"
                  className="mt-4 inline-flex items-center gap-1 text-xs font-semibold text-doador-aa hover:underline"
                >
                  <span>Gerenciar ofertas</span>
                  <ArrowRight className="h-3.5 w-3.5" aria-hidden="true" />
                </Link>
              </Card>

              <Card>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                    Reservas recebidas
                  </span>
                  <BookmarkCheck className="h-4 w-4 text-doador-aa" aria-hidden="true" />
                </div>
                <p className="mt-2 text-3xl font-bold text-ink tabular-nums">
                  {summary.reservasDoador ?? 0}
                </p>
                <p className="mt-1 text-xs text-ink-secondary">
                  Reservas vinculadas às suas ofertas
                </p>
                <Link
                  to="/app/reservas-ofertas"
                  className="mt-4 inline-flex items-center gap-1 text-xs font-semibold text-doador-aa hover:underline"
                >
                  <span>Acompanhar reservas</span>
                  <ArrowRight className="h-3.5 w-3.5" aria-hidden="true" />
                </Link>
              </Card>
            </>
          ) : null}

          {user.tipoUsuario === 'RECEPTOR' ? (
            <>
              <Card>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                    Minhas reservas
                  </span>
                  <BookmarkCheck className="h-4 w-4 text-receptor-aa" aria-hidden="true" />
                </div>
                <p className="mt-2 text-3xl font-bold text-ink tabular-nums">
                  {summary.minhasReservasReceptor ?? 0}
                </p>
                <p className="mt-1 text-xs text-ink-secondary">
                  Ofertas reservadas pela organização
                </p>
                <Link
                  to="/app/minhas-reservas"
                  className="mt-4 inline-flex items-center gap-1 text-xs font-semibold text-receptor-aa hover:underline"
                >
                  <span>Gerenciar reservas</span>
                  <ArrowRight className="h-3.5 w-3.5" aria-hidden="true" />
                </Link>
              </Card>

              <Card>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                    Minhas necessidades
                  </span>
                  <ClipboardList className="h-4 w-4 text-receptor-aa" aria-hidden="true" />
                </div>
                <p className="mt-2 text-3xl font-bold text-ink tabular-nums">
                  {summary.minhasNecessidades ?? 0}
                </p>
                <p className="mt-1 text-xs text-ink-secondary">
                  {summary.minhasCampanhas ?? 0} campanhas emergenciais registradas
                </p>
                <Link
                  to="/app/minhas-necessidades"
                  className="mt-4 inline-flex items-center gap-1 text-xs font-semibold text-receptor-aa hover:underline"
                >
                  <span>Ver necessidades</span>
                  <ArrowRight className="h-3.5 w-3.5" aria-hidden="true" />
                </Link>
              </Card>
            </>
          ) : null}

          {user.tipoUsuario === 'TRANSPORTADOR' ? (
            <>
              <Card>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                    Aguardando transporte
                  </span>
                  <Package className="h-4 w-4 text-transportador-aa" aria-hidden="true" />
                </div>
                <p className="mt-2 text-3xl font-bold text-ink tabular-nums">
                  {summary.aguardandoTransporte ?? 0}
                </p>
                <p className="mt-1 text-xs text-ink-secondary">
                  Reservas abertas para vínculo de entrega
                </p>
                <Link
                  to="/app/entregas-disponiveis"
                  className="mt-4 inline-flex items-center gap-1 text-xs font-semibold text-transportador-aa hover:underline"
                >
                  <span>Assumir entregas</span>
                  <ArrowRight className="h-3.5 w-3.5" aria-hidden="true" />
                </Link>
              </Card>

              <Card>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                    Minhas entregas e agenda
                  </span>
                  <CalendarClock className="h-4 w-4 text-transportador-aa" aria-hidden="true" />
                </div>
                <p className="mt-2 text-3xl font-bold text-ink tabular-nums">
                  {summary.minhasEntregas ?? 0}
                </p>
                <p className="mt-1 text-xs text-ink-secondary">
                  {summary.minhasDisponibilidades ?? 0} disponibilidades cadastradas
                </p>
                <Link
                  to="/app/minhas-entregas"
                  className="mt-4 inline-flex items-center gap-1 text-xs font-semibold text-transportador-aa hover:underline"
                >
                  <span>Ver minhas entregas</span>
                  <ArrowRight className="h-3.5 w-3.5" aria-hidden="true" />
                </Link>
              </Card>
            </>
          ) : null}

          <Card>
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                Rede DoaLink
              </span>
              <Flame className="h-4 w-4 text-danger" aria-hidden="true" />
            </div>
            <p className="mt-2 text-3xl font-bold text-ink tabular-nums">
              {summary.ofertasDisponiveis}
            </p>
            <p className="mt-1 text-xs text-ink-secondary">
              Ofertas disponíveis e {summary.campanhasAtivas} campanhas ativas
            </p>
            <Link
              to="/app/campanhas"
              className="mt-4 inline-flex items-center gap-1 text-xs font-semibold text-ink hover:underline"
            >
              <span>Ver campanhas emergenciais</span>
              <ArrowRight className="h-3.5 w-3.5" aria-hidden="true" />
            </Link>
          </Card>
        </div>
      ) : null}

      <InfoBanner tone="info" title="Fluxo operacional vigente">
        O DoaLink opera por listagem e reserva explícita: ofertas disponíveis são reservadas
        diretamente por ONGs receptoras e assumidas por transportadores disponíveis. Recursos de
        sugestões automáticas (matchmaking), rotas automáticas por mapas e histórico detalhado de
        etapas ainda não possuem endpoints na API.
      </InfoBanner>
    </div>
  )
}
