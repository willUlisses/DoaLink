import {
  BookmarkCheck,
  ClipboardList,
  Flame,
  Gift,
  History,
  LayoutDashboard,
  LogOut,
  Menu,
  Package,
  PlusCircle,
  Truck,
  UserCog,
  X,
} from 'lucide-react'
import { useEffect, useState, type ReactNode } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth.ts'
import { useToast } from '../../hooks/useToast.ts'
import type { TipoUsuario } from '../../types/enums.ts'
import { PROFILE_THEME } from '../../utils/theme.ts'
import { Button } from '../ui/Button.tsx'
import { ProfileBadge } from '../ui/ProfileBadge.tsx'
import { BrandLogo } from './BrandLogo.tsx'

interface NavItem {
  to: string
  label: string
  icon: ReactNode
  end?: boolean
}

function buildNavItems(tipoUsuario: TipoUsuario): NavItem[] {
  const inicio: NavItem = {
    to: '/app',
    label: 'Painel inicial',
    icon: <LayoutDashboard className="h-4 w-4" aria-hidden="true" />,
    end: true,
  }
  const campanhas: NavItem = {
    to: '/app/campanhas',
    label: 'Campanhas emergenciais',
    icon: <Flame className="h-4 w-4" aria-hidden="true" />,
    end: true,
  }
  const perfil: NavItem = {
    to: '/app/perfil',
    label: 'Meu perfil',
    icon: <UserCog className="h-4 w-4" aria-hidden="true" />,
  }

  if (tipoUsuario === 'DOADOR') {
    return [
      inicio,
      {
        to: '/app/minhas-ofertas',
        label: 'Minhas ofertas',
        icon: <Package className="h-4 w-4" aria-hidden="true" />,
      },
      {
        to: '/app/ofertas/nova',
        label: 'Nova oferta',
        icon: <PlusCircle className="h-4 w-4" aria-hidden="true" />,
      },
      {
        to: '/app/reservas-ofertas',
        label: 'Reservas recebidas',
        icon: <BookmarkCheck className="h-4 w-4" aria-hidden="true" />,
      },
      {
        to: '/app/necessidades',
        label: 'Necessidades abertas',
        icon: <ClipboardList className="h-4 w-4" aria-hidden="true" />,
        end: true,
      },
      campanhas,
      perfil,
    ]
  }

  if (tipoUsuario === 'RECEPTOR') {
    return [
      inicio,
      {
        to: '/app/ofertas',
        label: 'Ofertas disponíveis',
        icon: <Gift className="h-4 w-4" aria-hidden="true" />,
        end: true,
      },
      {
        to: '/app/minhas-reservas',
        label: 'Minhas reservas',
        icon: <BookmarkCheck className="h-4 w-4" aria-hidden="true" />,
      },
      {
        to: '/app/minhas-necessidades',
        label: 'Minhas necessidades',
        icon: <ClipboardList className="h-4 w-4" aria-hidden="true" />,
      },
      {
        to: '/app/minhas-campanhas',
        label: 'Minhas campanhas',
        icon: <Flame className="h-4 w-4" aria-hidden="true" />,
      },
      campanhas,
      perfil,
    ]
  }

  return [
    inicio,
    {
      to: '/app/transportes-disponiveis',
      label: 'Transportes disponíveis',
      icon: <Package className="h-4 w-4" aria-hidden="true" />,
    },
    {
      to: '/app/meus-transportes',
      label: 'Meus transportes',
      icon: <Truck className="h-4 w-4" aria-hidden="true" />,
    },
    {
      to: '/app/historico-transportes',
      label: 'Histórico de transportes',
      icon: <History className="h-4 w-4" aria-hidden="true" />,
    },
    campanhas,
    perfil,
  ]
}

export function AppLayout() {
  const { user, logoutUser } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [loggingOut, setLoggingOut] = useState(false)

  useEffect(() => {
    if (!mobileMenuOpen) return
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setMobileMenuOpen(false)
      }
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [mobileMenuOpen])

  if (!user) return null

  const theme = PROFILE_THEME[user.tipoUsuario]
  const navItems = buildNavItems(user.tipoUsuario)

  const handleLogout = async () => {
    setLoggingOut(true)
    await logoutUser()
    showToast({
      variant: 'info',
      title: 'Sessão encerrada',
      description: 'Você saiu da sua conta com segurança.',
    })
    navigate('/login', { replace: true })
  }

  const sidebarContent = (
    <div className="flex h-full flex-col justify-between gap-6 p-4">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <BrandLogo to="/app" />
          <button
            type="button"
            onClick={() => setMobileMenuOpen(false)}
            aria-label="Fechar menu de navegação"
            className="rounded-button p-1.5 text-ink-secondary hover:bg-surface-subtle hover:text-ink lg:hidden cursor-pointer"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>

        <div className="rounded-card border border-border bg-canvas p-3.5">
          <ProfileBadge tipoUsuario={user.tipoUsuario} />
          <p className="mt-2 truncate text-sm font-semibold text-ink">{user.nome}</p>
          <p className="truncate text-xs text-ink-secondary">{user.email}</p>
        </div>

        <nav aria-label="Menu principal" className="space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              state={
                item.to === '/app/perfil' && location.pathname !== '/app/perfil'
                  ? { from: location.pathname }
                  : undefined
              }
              onClick={() => setMobileMenuOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-2.5 rounded-button px-3 py-2.5 text-sm font-medium transition-colors duration-150 ${
                  isActive
                    ? `${theme.softBg} ${theme.softText} font-semibold`
                    : 'text-ink-secondary hover:bg-surface-subtle hover:text-ink'
                }`
              }
            >
              {item.icon}
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="border-t border-border pt-4">
        <button
          type="button"
          onClick={handleLogout}
          disabled={loggingOut}
          className="flex w-full items-center gap-2.5 rounded-button px-3 py-2.5 text-sm font-medium text-ink-secondary transition-colors hover:bg-danger-soft hover:text-danger disabled:opacity-60 cursor-pointer"
        >
          <LogOut className="h-4 w-4" aria-hidden="true" />
          <span>{loggingOut ? 'Saindo...' : 'Sair da conta'}</span>
        </button>
      </div>
    </div>
  )

  return (
    <div className="flex min-h-svh flex-col bg-canvas text-ink lg:flex-row">
      <a href="#main-content" className="skip-link">
        Pular para o conteúdo principal
      </a>

      {/* Barra superior para celular e tablet */}
      <header className="sticky top-0 z-30 flex items-center justify-between border-b border-border bg-surface px-4 py-3 lg:hidden">
        <BrandLogo to="/app" />
        <div className="flex items-center gap-2">
          <ProfileBadge tipoUsuario={user.tipoUsuario} className="hidden sm:inline-flex" />
          <Button
            variant="secondary"
            size="md"
            aria-label="Abrir menu de navegação"
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-sidebar"
            onClick={() => setMobileMenuOpen(true)}
            className="min-w-10 px-2.5"
          >
            <Menu className="h-5 w-5" aria-hidden="true" />
          </Button>
        </div>
      </header>

      {/* Drawer móvel */}
      {mobileMenuOpen ? (
        <div className="fixed inset-0 z-40 flex lg:hidden">
          <div
            className="fixed inset-0 bg-ink/40"
            aria-hidden="true"
            onClick={() => setMobileMenuOpen(false)}
          />
          <aside
            id="mobile-sidebar"
            aria-label="Navegação lateral"
            className="relative z-50 h-full w-72 max-w-[85vw] border-r border-border bg-surface shadow-[var(--shadow-elevated)]"
          >
            {sidebarContent}
          </aside>
        </div>
      ) : null}

      {/* Sidebar fixa no desktop */}
      <aside
        aria-label="Navegação lateral"
        className="hidden w-64 shrink-0 border-r border-border bg-surface lg:block"
      >
        <div className="sticky top-0 h-svh overflow-y-auto">{sidebarContent}</div>
      </aside>

      <main
        id="main-content"
        tabIndex={-1}
        className="min-w-0 flex-1 px-4 py-6 focus:outline-none sm:px-6 lg:px-8 lg:py-8"
      >
        <div className="mx-auto max-w-5xl">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
