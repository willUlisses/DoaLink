import { useId, type InputHTMLAttributes, type Ref } from 'react'

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
  hint?: string
  compact?: boolean
  ref?: Ref<HTMLInputElement>
}

export function Input({
  label,
  error,
  hint,
  compact = false,
  id,
  required,
  className = '',
  ref,
  ...rest
}: InputProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const errorId = `${inputId}-error`
  const hintId = `${inputId}-hint`

  const describedByParts: string[] = []
  if (hint) describedByParts.push(hintId)
  if (error) describedByParts.push(errorId)

  return (
    <div className={`flex flex-col ${compact ? 'gap-1' : 'gap-1.5'}`}>
      <label htmlFor={inputId} className="text-sm font-medium text-ink">
        {label}
        {required ? (
          <span className="ml-1 text-danger" aria-hidden="true">
            *
          </span>
        ) : null}
      </label>
      <input
        ref={ref}
        id={inputId}
        required={required}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={describedByParts.length > 0 ? describedByParts.join(' ') : undefined}
        className={`w-full rounded-card border bg-surface px-3.5 ${compact ? 'py-2' : 'py-2.5'} text-sm text-ink placeholder:text-ink-muted transition-colors duration-150 disabled:cursor-not-allowed disabled:bg-surface-subtle disabled:text-ink-secondary ${
          error
            ? 'border-danger focus-visible:outline-danger'
            : 'border-border hover:border-border-strong'
        } ${className}`}
        {...rest}
      />
      {hint && !error ? (
        <p id={hintId} className="text-xs text-ink-muted">
          {hint}
        </p>
      ) : null}
      {error ? (
        <p id={errorId} role="alert" className="text-xs font-medium text-danger">
          {error}
        </p>
      ) : null}
    </div>
  )
}

