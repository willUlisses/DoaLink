import { AlertCircle, CheckCircle2, Info, TriangleAlert, X } from 'lucide-react'
import type { ToastVariant } from '../../contexts/toast-context.ts'
import { useToast } from '../../hooks/useToast.ts'

const VARIANT_META: Record<
  ToastVariant,
  { border: string; iconColor: string; Icon: typeof Info }
> = {
  success: {
    border: 'border-success-border bg-surface',
    iconColor: 'text-success',
    Icon: CheckCircle2,
  },
  error: {
    border: 'border-danger-border bg-surface',
    iconColor: 'text-danger',
    Icon: AlertCircle,
  },
  warning: {
    border: 'border-warning-border bg-surface',
    iconColor: 'text-warning',
    Icon: TriangleAlert,
  },
  info: {
    border: 'border-info-border bg-surface',
    iconColor: 'text-info',
    Icon: Info,
  },
}

export function ToastContainer() {
  const { toasts, dismissToast } = useToast()

  if (toasts.length === 0) {
    return null
  }

  return (
    <div
      aria-label="Notificações do sistema"
      className="pointer-events-none fixed bottom-4 right-4 z-50 flex w-full max-w-sm flex-col gap-2.5 px-4 sm:px-0"
    >
      {toasts.map((toast) => {
        const { border, iconColor, Icon } = VARIANT_META[toast.variant]
        const role = toast.variant === 'error' ? 'alert' : 'status'

        return (
          <div
            key={toast.id}
            role={role}
            aria-live={toast.variant === 'error' ? 'assertive' : 'polite'}
            className={`pointer-events-auto flex items-start gap-3 rounded-card border p-4 shadow-[var(--shadow-elevated)] ${border}`}
          >
            <Icon className={`mt-0.5 h-5 w-5 shrink-0 ${iconColor}`} aria-hidden="true" />
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold text-ink">{toast.title}</p>
              {toast.description ? (
                <p className="mt-0.5 text-xs leading-relaxed text-ink-secondary">
                  {toast.description}
                </p>
              ) : null}
            </div>
            <button
              type="button"
              onClick={() => dismissToast(toast.id)}
              aria-label="Fechar notificação"
              className="rounded-button p-1 text-ink-secondary hover:bg-surface-subtle hover:text-ink cursor-pointer"
            >
              <X className="h-4 w-4" aria-hidden="true" />
            </button>
          </div>
        )
      })}
    </div>
  )
}

