import { X } from 'lucide-react'
import { useEffect, useId, useRef, type ReactNode } from 'react'
import { Button, type ButtonVariant } from '../ui/Button.tsx'

interface ConfirmDialogProps {
  open: boolean
  title: string
  description: string
  confirmLabel?: string
  cancelLabel?: string
  confirmVariant?: ButtonVariant
  loading?: boolean
  onConfirm: () => void
  onClose: () => void
  children?: ReactNode
}

function getFocusableElements(container: HTMLElement): HTMLElement[] {
  const selector =
    'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'
  return Array.from(container.querySelectorAll<HTMLElement>(selector))
}

export function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  confirmVariant = 'destructive',
  loading = false,
  onConfirm,
  onClose,
  children,
}: ConfirmDialogProps) {
  const titleId = useId()
  const descId = useId()
  const dialogRef = useRef<HTMLDivElement>(null)
  const previousFocusRef = useRef<HTMLElement | null>(null)

  useEffect(() => {
    if (!open) return

    previousFocusRef.current =
      document.activeElement instanceof HTMLElement ? document.activeElement : null

    const dialogNode = dialogRef.current
    if (dialogNode) {
      const focusables = getFocusableElements(dialogNode)
      ;(focusables[0] ?? dialogNode).focus()
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !loading) {
        event.preventDefault()
        onClose()
        return
      }
      if (event.key === 'Tab' && dialogNode) {
        const focusables = getFocusableElements(dialogNode)
        if (focusables.length === 0) return
        const first = focusables[0]
        const last = focusables[focusables.length - 1]
        if (event.shiftKey && document.activeElement === first) {
          event.preventDefault()
          last.focus()
        } else if (!event.shiftKey && document.activeElement === last) {
          event.preventDefault()
          first.focus()
        }
      }
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      previousFocusRef.current?.focus()
    }
  }, [open, loading, onClose])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/45 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !loading) {
          onClose()
        }
      }}
    >
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={descId}
        tabIndex={-1}
        className="w-full max-w-md rounded-card border border-border bg-surface p-6 shadow-[var(--shadow-elevated)]"
      >
        <div className="flex items-start justify-between gap-4">
          <h2 id={titleId} className="text-lg font-semibold text-ink">
            {title}
          </h2>
          <button
            type="button"
            disabled={loading}
            onClick={onClose}
            aria-label="Fechar diálogo"
            className="rounded-button p-1 text-ink-secondary hover:bg-surface-subtle hover:text-ink disabled:opacity-50 cursor-pointer"
          >
            <X className="h-4 w-4" aria-hidden="true" />
          </button>
        </div>

        <p id={descId} className="mt-2 text-sm leading-relaxed text-ink-secondary">
          {description}
        </p>

        {children ? <div className="mt-4">{children}</div> : null}

        <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
          <Button variant="secondary" onClick={onClose} disabled={loading}>
            {cancelLabel}
          </Button>
          <Button
            variant={confirmVariant}
            onClick={onConfirm}
            loading={loading}
            loadingLabel="Processando..."
          >
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  )
}

