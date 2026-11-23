import { AlertCircle, CheckCircle2, Info, TriangleAlert } from 'lucide-react'
import type { ReactNode } from 'react'

export type InfoBannerTone = 'info' | 'warning' | 'danger' | 'success'

interface InfoBannerProps {
  tone?: InfoBannerTone
  title?: string
  children: ReactNode
  action?: ReactNode
  className?: string
  role?: 'status' | 'alert'
}

const TONE_STYLES: Record<InfoBannerTone, { container: string; iconColor: string; Icon: typeof Info }> = {
  info: {
    container: 'border-info-border bg-info-soft text-ink',
    iconColor: 'text-info',
    Icon: Info,
  },
  warning: {
    container: 'border-warning-border bg-warning-soft text-ink',
    iconColor: 'text-warning',
    Icon: TriangleAlert,
  },
  danger: {
    container: 'border-danger-border bg-danger-soft text-ink',
    iconColor: 'text-danger',
    Icon: AlertCircle,
  },
  success: {
    container: 'border-success-border bg-success-soft text-ink',
    iconColor: 'text-success',
    Icon: CheckCircle2,
  },
}

export function InfoBanner({
  tone = 'info',
  title,
  children,
  action,
  className = '',
  role,
}: InfoBannerProps) {
  const { container, iconColor, Icon } = TONE_STYLES[tone]
  const resolvedRole = role ?? (tone === 'danger' ? 'alert' : 'status')

  return (
    <div
      role={resolvedRole}
      className={`flex flex-col gap-3 rounded-card border p-4 sm:flex-row sm:items-start sm:justify-between ${container} ${className}`}
    >
      <div className="flex items-start gap-3">
        <Icon className={`mt-0.5 h-5 w-5 shrink-0 ${iconColor}`} aria-hidden="true" />
        <div className="text-sm leading-relaxed">
          {title ? <p className="font-semibold text-ink">{title}</p> : null}
          <div className="text-ink-secondary">{children}</div>
        </div>
      </div>
      {action ? <div className="shrink-0">{action}</div> : null}
    </div>
  )
}

