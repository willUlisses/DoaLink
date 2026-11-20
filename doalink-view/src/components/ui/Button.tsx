import { Loader2 } from 'lucide-react'
import type { ButtonHTMLAttributes, ReactNode, Ref } from 'react'

export type ButtonVariant =
  | 'primary'
  | 'secondary'
  | 'destructive'
  | 'ghost'
  | 'doador'
  | 'transportador'
  | 'receptor'

export type ButtonSize = 'sm' | 'md' | 'lg'

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  size?: ButtonSize
  loading?: boolean
  loadingLabel?: string
  leftIcon?: ReactNode
  ref?: Ref<HTMLButtonElement>
}

const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary: 'bg-ink text-white hover:bg-[#2B3544] border border-transparent',
  secondary:
    'bg-surface text-ink border border-border hover:bg-surface-subtle hover:border-border-strong',
  destructive: 'bg-danger text-white hover:bg-danger-hover border border-transparent',
  ghost: 'bg-transparent text-ink-secondary hover:bg-surface-subtle hover:text-ink border border-transparent',
  doador: 'bg-doador-aa text-white hover:bg-doador-hover border border-transparent',
  transportador:
    'bg-transportador-aa text-white hover:bg-transportador-hover border border-transparent',
  receptor: 'bg-receptor-aa text-white hover:bg-receptor-hover border border-transparent',
}

const SIZE_CLASSES: Record<ButtonSize, string> = {
  sm: 'min-h-9 px-3 py-1.5 text-sm',
  md: 'min-h-10 px-4 py-2 text-sm',
  lg: 'min-h-12 px-5 py-2.5 text-base',
}

export function Button({
  variant = 'primary',
  size = 'md',
  loading = false,
  loadingLabel,
  leftIcon,
  disabled,
  className = '',
  children,
  type = 'button',
  ref,
  ...rest
}: ButtonProps) {
  const isDisabled = Boolean(disabled || loading)

  return (
    <button
      ref={ref}
      type={type}
      disabled={isDisabled}
      aria-busy={loading || undefined}
      className={`inline-flex items-center justify-center gap-2 rounded-[8px] font-semibold transition-colors duration-150 cursor-pointer disabled:cursor-not-allowed disabled:opacity-60 ${VARIANT_CLASSES[variant]} ${SIZE_CLASSES[size]} ${className}`}
      {...rest}
    >
      {loading ? (
        <Loader2 className="h-4 w-4 animate-spin shrink-0" aria-hidden="true" />
      ) : leftIcon ? (
        <span className="inline-flex shrink-0" aria-hidden="true">
          {leftIcon}
        </span>
      ) : null}
      <span>{loading && loadingLabel ? loadingLabel : children}</span>
    </button>
  )
}
