import { ArrowLeft, Compass } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Card } from '../../components/ui/Card.tsx'
import { useAuth } from '../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../hooks/useDocumentTitle.ts'

export function NotFoundPage() {
  useDocumentTitle('Página não encontrada')
  const { user } = useAuth()

  return (
    <div className="mx-auto flex min-h-[70svh] max-w-lg items-center justify-center px-4 py-12">
      <Card className="w-full text-center" padding="lg">
        <div
          className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-surface-subtle text-ink-secondary"
          aria-hidden="true"
        >
          <Compass className="h-6 w-6" />
        </div>
        <p className="text-xs font-semibold uppercase tracking-wider text-ink-muted">Erro 404</p>
        <h1 className="mt-1 text-2xl font-bold text-ink">Página não encontrada</h1>
        <p className="mt-2 text-sm leading-relaxed text-ink-secondary">
          O endereço acessado não existe ou foi movido. Verifique a URL ou retorne para o início.
        </p>
        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <Link
            to={user ? '/app' : '/'}
            className="inline-flex min-h-10 items-center gap-2 rounded-[8px] bg-ink px-4 py-2 text-sm font-semibold text-white hover:bg-[#2B3544]"
          >
            <ArrowLeft className="h-4 w-4" aria-hidden="true" />
            <span>{user ? 'Ir para o painel' : 'Ir para o início'}</span>
          </Link>
        </div>
      </Card>
    </div>
  )
}

