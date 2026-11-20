export function Skeleton({ className = '' }: { className?: string }) {
  return (
    <div
      aria-hidden="true"
      className={`animate-pulse rounded-[8px] bg-surface-subtle ${className}`}
    />
  )
}

export function CardListSkeleton({ count = 3 }: { count?: number }) {
  return (
    <div
      role="status"
      aria-label="Carregando conteúdo"
      className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3"
    >
      {Array.from({ length: count }).map((_, index) => (
        <div
          key={index}
          className="rounded-[12px] border border-border bg-surface p-5 shadow-[var(--shadow-card)]"
        >
          <div className="flex items-center justify-between gap-3 mb-4">
            <Skeleton className="h-5 w-28" />
            <Skeleton className="h-5 w-20 rounded-full" />
          </div>
          <Skeleton className="h-4 w-3/4 mb-2" />
          <Skeleton className="h-4 w-1/2 mb-5" />
          <div className="space-y-2 border-t border-border pt-4">
            <Skeleton className="h-3.5 w-full" />
            <Skeleton className="h-3.5 w-5/6" />
          </div>
        </div>
      ))}
      <span className="sr-only">Carregando...</span>
    </div>
  )
}

