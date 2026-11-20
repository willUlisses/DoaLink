import { Inbox } from 'lucide-react'
import type { ReactNode } from 'react'
import { Card } from '../ui/Card.tsx'

interface EmptyStateProps {
  title: string
  description: string
  icon?: ReactNode
  action?: ReactNode
}

export function EmptyState({
  title,
  description,
  icon,
  action,
}: EmptyStateProps) {
  return (
    <Card className="flex flex-col items-center justify-center py-12 text-center">
      <div
        className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-surface-subtle text-ink-secondary"
        aria-hidden="true"
      >
        {icon ?? <Inbox className="h-6 w-6" />}
      </div>
      <h2 className="text-base font-semibold text-ink">{title}</h2>
      <p className="mt-1.5 max-w-md text-sm text-ink-secondary">{description}</p>
      {action ? <div className="mt-5">{action}</div> : null}
    </Card>
  )
}

