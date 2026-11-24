import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { CardListSkeleton } from '../components/feedback/Skeleton.tsx'
import { useAuth } from '../hooks/useAuth.ts'
import type { TipoUsuario } from '../types/enums.ts'

function SessionLoadingFallback() {
  return (
    <div className="mx-auto max-w-5xl px-4 py-12" role="status" aria-label="Verificando sessão">
      <CardListSkeleton count={3} />
    </div>
  )
}

export function PrivateRoute() {
  const { status, user } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return <SessionLoadingFallback />
  }

  if (status !== 'authenticated' || !user) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}

export function RoleRoute({ allowedRoles }: { allowedRoles: readonly TipoUsuario[] }) {
  const { status, user } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return <SessionLoadingFallback />
  }

  if (status !== 'authenticated' || !user) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (!allowedRoles.includes(user.tipoUsuario)) {
    return <Navigate to="/acesso-negado" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}

export function PublicOnlyRoute() {
  const { status, user } = useAuth()

  if (status === 'loading') {
    return <SessionLoadingFallback />
  }

  if (status === 'authenticated' && user) {
    return <Navigate to="/app" replace />
  }

  return <Outlet />
}

