import { lazy, Suspense, ViewTransition } from 'react'
import { Route, Routes } from 'react-router-dom'
import { CardListSkeleton } from '../components/feedback/Skeleton.tsx'
import { AppLayout } from '../components/layout/AppLayout.tsx'
import { PublicLayout } from '../components/layout/PublicLayout.tsx'
import { CadastroPage } from '../pages/auth/CadastroPage.tsx'
import { LoginPage } from '../pages/auth/LoginPage.tsx'
import { RecuperarSenhaPage } from '../pages/auth/RecuperarSenhaPage.tsx'
import { RedefinirSenhaPage } from '../pages/auth/RedefinirSenhaPage.tsx'
import { VerificarEmailPage } from '../pages/auth/VerificarEmailPage.tsx'
import { AcessoNegadoPage } from '../pages/errors/AcessoNegadoPage.tsx'
import { NotFoundPage } from '../pages/errors/NotFoundPage.tsx'
import { HomePage } from '../pages/HomePage.tsx'
import { TIPO_USUARIO } from '../types/enums.ts'
import { PrivateRoute, PublicOnlyRoute, RoleRoute } from './RouteGuards.tsx'

const DashboardPage = lazy(() =>
  import('../pages/app/DashboardPage.tsx').then((m) => ({ default: m.DashboardPage })),
)
const OfertasDisponiveisPage = lazy(() =>
  import('../pages/app/ofertas/OfertasDisponiveisPage.tsx').then((m) => ({
    default: m.OfertasDisponiveisPage,
  })),
)
const OfertaDetalhePage = lazy(() =>
  import('../pages/app/ofertas/OfertaDetalhePage.tsx').then((m) => ({
    default: m.OfertaDetalhePage,
  })),
)
const MinhasOfertasPage = lazy(() =>
  import('../pages/app/ofertas/MinhasOfertasPage.tsx').then((m) => ({
    default: m.MinhasOfertasPage,
  })),
)
const NovaOfertaPage = lazy(() =>
  import('../pages/app/ofertas/NovaOfertaPage.tsx').then((m) => ({ default: m.NovaOfertaPage })),
)
const NecessidadesPage = lazy(() =>
  import('../pages/app/necessidades/NecessidadesPage.tsx').then((m) => ({
    default: m.NecessidadesPage,
  })),
)
const NecessidadeDetalhePage = lazy(() =>
  import('../pages/app/necessidades/NecessidadeDetalhePage.tsx').then((m) => ({
    default: m.NecessidadeDetalhePage,
  })),
)
const MinhasNecessidadesPage = lazy(() =>
  import('../pages/app/necessidades/MinhasNecessidadesPage.tsx').then((m) => ({
    default: m.MinhasNecessidadesPage,
  })),
)
const NovaNecessidadePage = lazy(() =>
  import('../pages/app/necessidades/NovaNecessidadePage.tsx').then((m) => ({
    default: m.NovaNecessidadePage,
  })),
)
const CampanhasPage = lazy(() =>
  import('../pages/app/campanhas/CampanhasPage.tsx').then((m) => ({ default: m.CampanhasPage })),
)
const CampanhaDetalhePage = lazy(() =>
  import('../pages/app/campanhas/CampanhaDetalhePage.tsx').then((m) => ({
    default: m.CampanhaDetalhePage,
  })),
)
const MinhasCampanhasPage = lazy(() =>
  import('../pages/app/campanhas/MinhasCampanhasPage.tsx').then((m) => ({
    default: m.MinhasCampanhasPage,
  })),
)
const NovaCampanhaPage = lazy(() =>
  import('../pages/app/campanhas/NovaCampanhaPage.tsx').then((m) => ({
    default: m.NovaCampanhaPage,
  })),
)
const DisponibilidadesPage = lazy(() =>
  import('../pages/app/transporte/DisponibilidadesPage.tsx').then((m) => ({
    default: m.DisponibilidadesPage,
  })),
)
const EntregasDisponiveisPage = lazy(() =>
  import('../pages/app/transporte/EntregasDisponiveisPage.tsx').then((m) => ({
    default: m.EntregasDisponiveisPage,
  })),
)
const MinhasEntregasPage = lazy(() =>
  import('../pages/app/transporte/MinhasEntregasPage.tsx').then((m) => ({
    default: m.MinhasEntregasPage,
  })),
)
const MinhasReservasReceptorPage = lazy(() =>
  import('../pages/app/reservas/MinhasReservasReceptorPage.tsx').then((m) => ({
    default: m.MinhasReservasReceptorPage,
  })),
)
const ReservasDoadorPage = lazy(() =>
  import('../pages/app/reservas/ReservasDoadorPage.tsx').then((m) => ({
    default: m.ReservasDoadorPage,
  })),
)

function RouteSuspense({ children }: { children: React.ReactNode }) {
  return (
    <ViewTransition default="none" enter="vt-fade-in" exit="vt-fade-out">
      <Suspense fallback={<CardListSkeleton count={3} />}>{children}</Suspense>
    </ViewTransition>
  )
}

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<HomePage />} />

        <Route element={<PublicOnlyRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/cadastro" element={<CadastroPage />} />
          <Route path="/verificar-email" element={<VerificarEmailPage />} />
          <Route path="/recuperar-senha" element={<RecuperarSenhaPage />} />
        </Route>

        <Route path="/redefinir-senha" element={<RedefinirSenhaPage />} />
        <Route path="/acesso-negado" element={<AcessoNegadoPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>

      <Route element={<PrivateRoute />}>
        <Route path="/app" element={<AppLayout />}>
          <Route
            index
            element={
              <RouteSuspense>
                <DashboardPage />
              </RouteSuspense>
            }
          />

          {/* Rotas autenticadas para qualquer perfil */}
          <Route
            path="ofertas"
            element={
              <RouteSuspense>
                <OfertasDisponiveisPage />
              </RouteSuspense>
            }
          />
          <Route
            path="ofertas/:id"
            element={
              <RouteSuspense>
                <OfertaDetalhePage />
              </RouteSuspense>
            }
          />
          <Route
            path="necessidades"
            element={
              <RouteSuspense>
                <NecessidadesPage />
              </RouteSuspense>
            }
          />
          <Route
            path="necessidades/:id"
            element={
              <RouteSuspense>
                <NecessidadeDetalhePage />
              </RouteSuspense>
            }
          />
          <Route
            path="campanhas"
            element={
              <RouteSuspense>
                <CampanhasPage />
              </RouteSuspense>
            }
          />
          <Route
            path="campanhas/:id"
            element={
              <RouteSuspense>
                <CampanhaDetalhePage />
              </RouteSuspense>
            }
          />

          {/* Rotas exclusivas do Doador */}
          <Route element={<RoleRoute allowedRoles={[TIPO_USUARIO.DOADOR]} />}>
            <Route
              path="minhas-ofertas"
              element={
                <RouteSuspense>
                  <MinhasOfertasPage />
                </RouteSuspense>
              }
            />
            <Route
              path="ofertas/nova"
              element={
                <RouteSuspense>
                  <NovaOfertaPage />
                </RouteSuspense>
              }
            />
            <Route
              path="reservas-ofertas"
              element={
                <RouteSuspense>
                  <ReservasDoadorPage />
                </RouteSuspense>
              }
            />
          </Route>

          {/* Rotas exclusivas do Receptor */}
          <Route element={<RoleRoute allowedRoles={[TIPO_USUARIO.RECEPTOR]} />}>
            <Route
              path="minhas-necessidades"
              element={
                <RouteSuspense>
                  <MinhasNecessidadesPage />
                </RouteSuspense>
              }
            />
            <Route
              path="necessidades/nova"
              element={
                <RouteSuspense>
                  <NovaNecessidadePage />
                </RouteSuspense>
              }
            />
            <Route
              path="minhas-campanhas"
              element={
                <RouteSuspense>
                  <MinhasCampanhasPage />
                </RouteSuspense>
              }
            />
            <Route
              path="campanhas/nova"
              element={
                <RouteSuspense>
                  <NovaCampanhaPage />
                </RouteSuspense>
              }
            />
            <Route
              path="minhas-reservas"
              element={
                <RouteSuspense>
                  <MinhasReservasReceptorPage />
                </RouteSuspense>
              }
            />
          </Route>

          {/* Rotas exclusivas do Transportador */}
          <Route element={<RoleRoute allowedRoles={[TIPO_USUARIO.TRANSPORTADOR]} />}>
            <Route
              path="disponibilidades"
              element={
                <RouteSuspense>
                  <DisponibilidadesPage />
                </RouteSuspense>
              }
            />
            <Route
              path="entregas-disponiveis"
              element={
                <RouteSuspense>
                  <EntregasDisponiveisPage />
                </RouteSuspense>
              }
            />
            <Route
              path="minhas-entregas"
              element={
                <RouteSuspense>
                  <MinhasEntregasPage />
                </RouteSuspense>
              }
            />
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  )
}
