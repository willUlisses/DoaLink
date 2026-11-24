import type { ReactNode } from 'react'

export type BadgeTone =
  | 'neutral'
  | 'success'
  | 'warning'
  | 'danger'
  | 'info'
  | 'doador'
  | 'transportador'
  | 'receptor'

export interface BadgeProps {
  tone?: BadgeTone
  icon?: ReactNode
  children: ReactNode
  className?: string
}

const TONE_CLASSES: Record<BadgeTone, string> = {
  neutral: 'bg-surface-subtle text-ink-secondary border-border',
  success: 'bg-success-soft text-success border-success-border',
  warning: 'bg-warning-soft text-warning border-warning-border',
  danger: 'bg-danger-soft text-danger border-danger-border',
  info: 'bg-info-soft text-info border-info-border',
  doador: 'bg-doador-soft text-doador-aa border-doador-border',
  transportador: 'bg-transportador-soft text-transportador-aa border-transportador-border',
  receptor: 'bg-receptor-soft text-receptor-aa border-receptor-border',
}

export function Badge({
  tone = 'neutral',
  icon,
  children,
  className = '',
}: BadgeProps) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-semibold leading-5 ${TONE_CLASSES[tone]} ${className}`}
    >
      {icon ? (
        <span className="inline-flex shrink-0" aria-hidden="true">
          {icon}
        </span>
      ) : null}
      <span>{children}</span>
    </span>
  )
}

