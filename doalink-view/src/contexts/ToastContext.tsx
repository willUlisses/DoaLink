import { useCallback, useMemo, useRef, useState, type ReactNode } from 'react'
import { ToastContext, type ToastInput, type ToastItem } from './toast-context.ts'

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([])
  const counterRef = useRef(0)

  const dismissToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((item) => item.id !== id))
  }, [])

  const showToast = useCallback(
    ({ durationMs = 5000, ...rest }: ToastInput) => {
      counterRef.current += 1
      const id = `toast-${counterRef.current}`
      setToasts((prev) => [...prev.slice(-3), { id, ...rest }])

      if (durationMs > 0) {
        window.setTimeout(() => {
          dismissToast(id)
        }, durationMs)
      }
    },
    [dismissToast],
  )

  const value = useMemo(
    () => ({ toasts, showToast, dismissToast }),
    [toasts, showToast, dismissToast],
  )

  return <ToastContext value={value}>{children}</ToastContext>
}

