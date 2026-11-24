import type { FieldValues, Path, UseFormSetError, UseFormSetFocus } from 'react-hook-form'
import { isNormalizedApiError } from '../services/api.ts'

export function applyApiErrorsToForm<TFieldValues extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<TFieldValues>,
  setFocus?: UseFormSetFocus<TFieldValues>,
  fieldAliases?: Record<string, Path<TFieldValues>>,
): string {
  if (!isNormalizedApiError(error)) {
    return 'Não foi possível concluir a operação. Tente novamente.'
  }

  let firstMappedField: Path<TFieldValues> | null = null

  for (const [rawField, message] of Object.entries(error.fieldErrors)) {
    const mappedField =
      fieldAliases?.[rawField] ?? (rawField as Path<TFieldValues>)

    setError(mappedField, {
      type: 'server',
      message,
    })

    if (!firstMappedField) {
      firstMappedField = mappedField
    }
  }

  if (firstMappedField && setFocus) {
    try {
      setFocus(firstMappedField)
    } catch {
      // Campo pode não estar montado no DOM no momento
    }
  }

  return error.message
}

