import { ArrowLeft, ShieldAlert } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Button } from '../../components/ui/Button.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { useAuth } from '../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../hooks/useDocumentTitle.ts'

export function AcessoNegadoPage() {
  useDocumentTitle('Acesso negado')
  const { user } = useAuth()

  return (
    <div className="mx-auto flex min-h-[70svh] max-w-lg items-center justify-center px-4 py-12">
      <Card className="w-full text-center" padding="lg">
        <div
          className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-danger-soft text-danger"
          aria-hidden="true"
        >
          <ShieldAlert className="h-6 w-6" />
        </div>
        <h1 className="text-2xl font-bold text-ink">Acesso negado</h1>
        <p className="mt-2 text-sm leading-relaxed text-ink-secondary">
          Seu perfil atual não possui permissão para acessar esta área ou executar esta operação.
        </p>
        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <Button asChild leftIcon={<ArrowLeft className="h-4 w-4" />}>
            <Link to={user ? '/app' : '/'}>
              {user ? 'Voltar ao painel' : 'Voltar à página inicial'}
            </Link>
          </Button>
        </div>
      </Card>
    </div>
  )
}

