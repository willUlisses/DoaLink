import { ChevronRight } from 'lucide-react'
import { Link } from 'react-router-dom'

export interface BreadcrumbItem {
  label: string
  to?: string
}

export function Breadcrumbs({ items }: { items: BreadcrumbItem[] }) {
  if (items.length === 0) return null

  return (
    <nav aria-label="Navegação estrutural" className="mb-4">
      <ol className="flex flex-wrap items-center gap-1.5 text-xs text-ink-secondary">
        {items.map((item, index) => {
          const isLast = index === items.length - 1
          return (
            <li key={`${item.label}-${index}`} className="inline-flex items-center gap-1.5">
              {index > 0 ? (
                <ChevronRight className="h-3.5 w-3.5 text-ink-muted" aria-hidden="true" />
              ) : null}
              {item.to && !isLast ? (
                <Link
                  to={item.to}
                  className="rounded px-1 py-0.5 font-medium text-ink-secondary hover:text-ink hover:underline"
                >
                  {item.label}
                </Link>
              ) : (
                <span
                  aria-current={isLast ? 'page' : undefined}
                  className={isLast ? 'font-semibold text-ink' : 'font-medium text-ink-secondary'}
                >
                  {item.label}
                </span>
              )}
            </li>
          )
        })}
      </ol>
    </nav>
  )
}

