import type { HTMLAttributes, ReactNode } from 'react'

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode
  padding?: 'none' | 'sm' | 'md' | 'lg'
}

const PADDING_MAP = {
  none: '',
  sm: 'p-4',
  md: 'p-5 sm:p-6',
  lg: 'p-6 sm:p-8',
}

export function Card({
  children,
  padding = 'md',
  className = '',
  ...rest
}: CardProps) {
  return (
    <div
      className={`rounded-[12px] border border-border bg-surface shadow-[var(--shadow-card)] ${PADDING_MAP[padding]} ${className}`}
      {...rest}
    >
      {children}
    </div>
  )
}

