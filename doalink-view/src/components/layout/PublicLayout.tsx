import { ArrowRight, LogIn, UserPlus } from 'lucide-react'
import { Link, Outlet } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth.ts'
import { Button } from '../ui/Button.tsx'
import { BrandLogo } from './BrandLogo.tsx'

export function PublicLayout() {
  const { user, status } = useAuth()
  const isAuthenticated = status === 'authenticated' && user !== null

  return (
    <div className="flex min-h-svh flex-col bg-canvas text-ink">
      <a href="#main-content" className="skip-link">
        Pular para o conteúdo principal
      </a>

      <header className="sticky top-0 z-30 border-b border-border bg-surface/95 backdrop-blur-xs">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3.5 sm:px-6">
          <BrandLogo to="/" />

          <nav aria-label="Navegação principal" className="flex items-center gap-2 sm:gap-3">
            {isAuthenticated ? (
              <Button asChild rightIcon={<ArrowRight className="h-4 w-4" />}>
                <Link to="/app">Ir para o painel</Link>
              </Button>
            ) : (
              <>
                <Button
                  asChild
                  variant="secondary"
                  leftIcon={<LogIn className="h-4 w-4" />}
                  className="px-3.5"
                >
                  <Link to="/login">Entrar</Link>
                </Button>
                <Button
                  asChild
                  leftIcon={<UserPlus className="h-4 w-4" />}
                  className="px-3.5"
                >
                  <Link to="/cadastro">Cadastrar</Link>
                </Button>
              </>
            )}
          </nav>
        </div>
      </header>

      <main id="main-content" tabIndex={-1} className="flex-1 focus:outline-none">
        <Outlet />
      </main>

      <footer className="border-t border-border bg-surface py-6">
        <div className="mx-auto flex max-w-6xl flex-col items-start justify-between gap-3 px-4 text-xs text-ink-secondary sm:flex-row sm:items-center sm:px-6">
          <p>DoaLink — Conectando doadores, ONGs receptoras e transportadores.</p>
          <div className="flex flex-wrap items-center gap-4">
            <Link to="/login" className="hover:text-ink hover:underline">
              Entrar
            </Link>
            <Link to="/cadastro" className="hover:text-ink hover:underline">
              Criar conta
            </Link>
            <Link to="/recuperar-senha" className="hover:text-ink hover:underline">
              Recuperar senha
            </Link>
          </div>
        </div>
      </footer>
    </div>
  )
}

