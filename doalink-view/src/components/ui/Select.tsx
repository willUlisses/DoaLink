import { ChevronDown } from 'lucide-react'
import { useId, type ReactNode, type Ref, type SelectHTMLAttributes } from 'react'

export interface SelectOption {
  value: string
  label: string
}

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  options?: SelectOption[]
  placeholder?: string
  error?: string
  hint?: string
  children?: ReactNode
  ref?: Ref<HTMLSelectElement>
}

export function Select({
  label,
  options,
  placeholder,
  error,
  hint,
  id,
  required,
  children,
  className = '',
  ref,
  ...rest
}: SelectProps) {
  const generatedId = useId()
  const selectId = id ?? generatedId
  const errorId = `${selectId}-error`
  const hintId = `${selectId}-hint`

  const describedByParts: string[] = []
  if (hint) describedByParts.push(hintId)
  if (error) describedByParts.push(errorId)

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={selectId} className="text-sm font-medium text-ink">
        {label}
        {required ? (
          <span className="ml-1 text-danger" aria-hidden="true">
            *
          </span>
        ) : null}
      </label>
      <div className="relative">
        <select
          ref={ref}
          id={selectId}
          required={required}
          aria-invalid={error ? 'true' : undefined}
          aria-describedby={describedByParts.length > 0 ? describedByParts.join(' ') : undefined}
          className={`w-full appearance-none rounded-[12px] border bg-surface py-2.5 pl-3.5 pr-10 text-sm text-ink transition-colors duration-150 disabled:cursor-not-allowed disabled:bg-surface-subtle disabled:text-ink-secondary ${
            error
              ? 'border-danger focus-visible:outline-danger'
              : 'border-border hover:border-border-strong'
          } ${className}`}
          {...rest}
        >
          {placeholder ? <option value="">{placeholder}</option> : null}
          {options
            ? options.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))
            : children}
        </select>
        <ChevronDown
          className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-secondary"
          aria-hidden="true"
        />
      </div>
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

