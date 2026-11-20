import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from './Button.tsx'

interface PaginationProps {
  page: number
  totalPages: number
  onPageChange: (nextPage: number) => void
  disabled?: boolean
}

export function Pagination({
  page,
  totalPages,
  onPageChange,
  disabled = false,
}: PaginationProps) {
  if (totalPages <= 1) {
    return null
  }

  const isFirst = page <= 0
  const isLast = page >= totalPages - 1

  return (
    <nav
      aria-label="Paginação de resultados"
      className="flex flex-wrap items-center justify-between gap-3 rounded-[12px] border border-border bg-surface px-4 py-3"
    >
      <p className="text-sm text-ink-secondary">
        Página <span className="font-semibold text-ink">{page + 1}</span> de{' '}
        <span className="font-semibold text-ink">{totalPages}</span>
      </p>
      <div className="flex items-center gap-2">
        <Button
          variant="secondary"
          size="sm"
          disabled={disabled || isFirst}
          onClick={() => onPageChange(page - 1)}
          leftIcon={<ChevronLeft className="h-4 w-4" />}
        >
          Anterior
        </Button>
        <Button
          variant="secondary"
          size="sm"
          disabled={disabled || isLast}
          onClick={() => onPageChange(page + 1)}
        >
          <span>Próxima</span>
          <ChevronRight className="h-4 w-4 ml-1 inline" aria-hidden="true" />
        </Button>
      </div>
    </nav>
  )
}

