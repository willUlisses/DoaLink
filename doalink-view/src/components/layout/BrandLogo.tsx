import { Link } from 'react-router-dom'

interface BrandLogoProps {
  to?: string
  compact?: boolean
}

export function BrandLogo({ to = '/', compact = false }: BrandLogoProps) {
  return (
    <Link
      to={to}
      className="inline-flex items-center gap-2.5 rounded-[8px] text-ink no-underline"
      aria-label="DoaLink — Página inicial"
    >
      <span
        className="relative flex h-9 w-9 shrink-0 items-center justify-center rounded-[10px] bg-ink text-white shadow-xs"
        aria-hidden="true"
      >
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M12 20.5C12 20.5 4.5 15.6 4.5 9.8C4.5 7.2 6.5 5.2 9 5.2C10.5 5.2 11.4 5.9 12 6.9C12.6 5.9 13.5 5.2 15 5.2C17.5 5.2 19.5 7.2 19.5 9.8C19.5 15.6 12 20.5 12 20.5Z"
            stroke="#FFFFFF"
            strokeWidth="2"
            strokeLinejoin="round"
          />
          <circle cx="9" cy="10.2" r="1.4" fill="#E4572E" />
          <circle cx="15" cy="10.2" r="1.4" fill="#3BA776" />
          <circle cx="12" cy="14" r="1.4" fill="#2A7FBA" />
        </svg>
      </span>
      {!compact ? (
        <span className="flex flex-col leading-none">
          <span className="text-lg font-bold tracking-tight text-ink">DoaLink</span>
          <span className="text-[11px] font-medium text-ink-secondary">
            Logística solidária
          </span>
        </span>
      ) : null}
    </Link>
  )
}

