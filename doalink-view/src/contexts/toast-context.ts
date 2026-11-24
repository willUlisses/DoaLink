import { createContext } from 'react'

export type ToastVariant = 'success' | 'error' | 'warning' | 'info'

export interface ToastItem {
  id: string
  variant: ToastVariant
  title: string
  description?: string
}

export interface ToastInput {
  variant: ToastVariant
  title: string
  description?: string
  durationMs?: number
}

export interface ToastContextValue {
  toasts: ToastItem[]
  showToast: (toast: ToastInput) => void
  dismissToast: (id: string) => void
}

export const ToastContext = createContext<ToastContextValue | null>(null)

