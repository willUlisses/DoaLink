import { useId, type InputHTMLAttributes, type Ref } from 'react'

export interface CheckboxProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: string
  description?: string
  error?: string
  ref?: Ref<HTMLInputElement>
}

export function Checkbox({
  label,
  description,
  error,
  id,
  className = '',
  ref,
  ...rest
}: CheckboxProps) {
  const generatedId = useId()
  const checkboxId = id ?? generatedId
  const descId = `${checkboxId}-desc`
  const errorId = `${checkboxId}-error`

  const describedBy: string[] = []
  if (description) describedBy.push(descId)
  if (error) describedBy.push(errorId)

  return (
    <div className="flex flex-col gap-1">
      <label
        htmlFor={checkboxId}
        className={`flex items-start gap-3 rounded-[12px] border border-border bg-surface p-3.5 cursor-pointer transition-colors hover:border-border-strong ${className}`}
      >
        <input
          ref={ref}
          id={checkboxId}
          type="checkbox"
          aria-invalid={error ? 'true' : undefined}
          aria-describedby={describedBy.length > 0 ? describedBy.join(' ') : undefined}
          className="mt-0.5 h-4 w-4 shrink-0 rounded border-border accent-ink cursor-pointer"
          {...rest}
        />
        <span className="flex flex-col gap-0.5">
          <span className="text-sm font-medium text-ink">{label}</span>
          {description ? (
            <span id={descId} className="text-xs text-ink-secondary">
              {description}
            </span>
          ) : null}
        </span>
      </label>
      {error ? (
        <p id={errorId} role="alert" className="text-xs font-medium text-danger">
          {error}
        </p>
      ) : null}
    </div>
  )
}

