import { Eye, EyeOff } from 'lucide-react'
import { useId, useState, type InputHTMLAttributes, type Ref } from 'react'

export interface PasswordInputProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: string
  error?: string
  hint?: string
  ref?: Ref<HTMLInputElement>
}

export function PasswordInput({
  label,
  error,
  hint,
  id,
  required,
  className = '',
  ref,
  ...rest
}: PasswordInputProps) {
  const [visible, setVisible] = useState(false)
  const generatedId = useId()
  const inputId = id ?? generatedId
  const errorId = `${inputId}-error`
  const hintId = `${inputId}-hint`

  const describedByParts: string[] = []
  if (hint) describedByParts.push(hintId)
  if (error) describedByParts.push(errorId)

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={inputId} className="text-sm font-medium text-ink">
        {label}
        {required ? (
          <span className="ml-1 text-danger" aria-hidden="true">
            *
          </span>
        ) : null}
      </label>
      <div className="relative">
        <input
          ref={ref}
          id={inputId}
          type={visible ? 'text' : 'password'}
          required={required}
          aria-invalid={error ? 'true' : undefined}
          aria-describedby={describedByParts.length > 0 ? describedByParts.join(' ') : undefined}
          className={`w-full rounded-[12px] border bg-surface py-2.5 pl-3.5 pr-11 text-sm text-ink placeholder:text-ink-muted transition-colors duration-150 disabled:cursor-not-allowed disabled:bg-surface-subtle ${
            error
              ? 'border-danger focus-visible:outline-danger'
              : 'border-border hover:border-border-strong'
          } ${className}`}
          {...rest}
        />
        <button
          type="button"
          onClick={() => setVisible((prev) => !prev)}
          aria-label={visible ? 'Ocultar senha' : 'Mostrar senha'}
          aria-pressed={visible}
          className="absolute right-2 top-1/2 -translate-y-1/2 rounded-[8px] p-1.5 text-ink-secondary hover:bg-surface-subtle hover:text-ink cursor-pointer"
        >
          {visible ? (
            <EyeOff className="h-4 w-4" aria-hidden="true" />
          ) : (
            <Eye className="h-4 w-4" aria-hidden="true" />
          )}
        </button>
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

