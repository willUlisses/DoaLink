import { AlertTriangle, RotateCw } from 'lucide-react'
import { Button } from '../ui/Button.tsx'
import { Card } from '../ui/Card.tsx'

interface ErrorStateProps {
  title?: string
  message: string
  onRetry?: () => void
  retrying?: boolean
}

export function ErrorState({
  title = 'Não foi possível carregar os dados',
  message,
  onRetry,
  retrying = false,
}: ErrorStateProps) {
  return (
    <Card
      role="alert"
      className="flex flex-col items-center justify-center border-danger-border bg-danger-soft/40 py-10 text-center"
    >
      <div
        className="mb-3 flex h-11 w-11 items-center justify-center rounded-full bg-danger-soft text-danger"
        aria-hidden="true"
      >
        <AlertTriangle className="h-5 w-5" />
      </div>
      <h2 className="text-base font-semibold text-ink">{title}</h2>
      <p className="mt-1.5 max-w-md text-sm text-ink-secondary">{message}</p>
      {onRetry ? (
        <div className="mt-5">
          <Button
            variant="secondary"
            size="sm"
            loading={retrying}
            loadingLabel="Tentando novamente..."
            leftIcon={<RotateCw className="h-4 w-4" />}
            onClick={onRetry}
          >
            Tentar novamente
          </Button>
        </div>
      ) : null}
    </Card>
  )
}

