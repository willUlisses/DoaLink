import { useId, type Ref, type TextareaHTMLAttributes } from 'react'

export interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string
  error?: string
  hint?: string
  ref?: Ref<HTMLTextAreaElement>
}

export function Textarea({
  label,
  error,
  hint,
  id,
  required,
  className = '',
  rows = 3,
  ref,
  ...rest
}: TextareaProps) {
  const generatedId = useId()
  const textareaId = id ?? generatedId
  const errorId = `${textareaId}-error`
  const hintId = `${textareaId}-hint`

  const describedByParts: string[] = []
  if (hint) describedByParts.push(hintId)
  if (error) describedByParts.push(errorId)

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={textareaId} className="text-sm font-medium text-ink">
        {label}
        {required ? (
          <span className="ml-1 text-danger" aria-hidden="true">
            *
          </span>
        ) : null}
      </label>
      <textarea
        ref={ref}
        id={textareaId}
        rows={rows}
        required={required}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={describedByParts.length > 0 ? describedByParts.join(' ') : undefined}
        className={`w-full rounded-[12px] border bg-surface px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-muted transition-colors duration-150 disabled:cursor-not-allowed disabled:bg-surface-subtle ${
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

