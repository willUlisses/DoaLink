import { use } from 'react'
import { ToastContext, type ToastContextValue } from '../contexts/toast-context.ts'

export function useToast(): ToastContextValue {
  const ctx = use(ToastContext)
  if (!ctx) {
    throw new Error('useToast deve ser utilizado dentro de ToastProvider.')
  }
  return ctx
}

