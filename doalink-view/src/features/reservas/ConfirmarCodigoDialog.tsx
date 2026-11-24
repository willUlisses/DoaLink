import { zodResolver } from '@hookform/resolvers/zod'
import { KeyRound, X } from 'lucide-react'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { InfoBanner } from '../../components/feedback/InfoBanner.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Input } from '../../components/ui/Input.tsx'
import {
  confirmarCodigoTransporteSchema,
  type ConfirmarCodigoTransporteFormValues,
} from '../../schemas/reserva.schema.ts'

interface ConfirmarCodigoDialogProps {
  open: boolean
  mode: 'retirada' | 'entrega'
  reservaId: number | null
  loading?: boolean
  errorMessage?: string | null
  onConfirm: (codigo: string) => void
  onClose: () => void
}

export function ConfirmarCodigoDialog({
  open,
  mode,
  reservaId,
  loading = false,
  errorMessage,
  onConfirm,
  onClose,
}: ConfirmarCodigoDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<ConfirmarCodigoTransporteFormValues>({
    resolver: zodResolver(confirmarCodigoTransporteSchema),
    defaultValues: { codigo: '' },
  })

  useEffect(() => {
    if (!open) {
      reset({ codigo: '' })
      return
    }
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !loading) {
        onClose()
      }
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [open, loading, onClose, reset])

  if (!open || reservaId === null) return null

  const isRetirada = mode === 'retirada'
  const titulo = isRetirada
    ? `Confirmar retirada — Reserva #${reservaId}`
    : `Confirmar entrega — Reserva #${reservaId}`
  const descricao = isRetirada
    ? 'Solicite ao doador o código numérico de 6 dígitos para confirmar que você recebeu os itens para transporte.'
    : 'Solicite ao receptor (ONG) o código numérico de 6 dígitos para confirmar que os itens foram entregues.'

  const codigoField = register('codigo')

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/50 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirmar-codigo-title"
    >
      <div className="w-full max-w-md rounded-card border border-border bg-surface p-6 shadow-[var(--shadow-elevated)]">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <span className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-button bg-transportador-soft text-transportador-aa">
              <KeyRound className="h-4 w-4" aria-hidden="true" />
            </span>
            <h2 id="confirmar-codigo-title" className="text-lg font-bold text-ink">
              {titulo}
            </h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            aria-label="Fechar janela"
            className="rounded-button p-1 text-ink-secondary hover:bg-surface-subtle hover:text-ink cursor-pointer"
          >
            <X className="h-4 w-4" aria-hidden="true" />
          </button>
        </div>

        <p className="mt-3 text-sm leading-relaxed text-ink-secondary">{descricao}</p>

        {errorMessage ? (
          <div className="mt-4">
            <InfoBanner tone="danger">{errorMessage}</InfoBanner>
          </div>
        ) : null}

        <form
          onSubmit={handleSubmit((values) => onConfirm(values.codigo.trim()))}
          noValidate
          className="mt-5 space-y-4"
        >
          <Input
            label={isRetirada ? 'Código de retirada (6 dígitos)' : 'Código de entrega (6 dígitos)'}
            placeholder="000000"
            inputMode="numeric"
            maxLength={6}
            autoComplete="one-time-code"
            required
            className="text-center font-mono text-lg tracking-[0.35em]"
            error={errors.codigo?.message}
            {...codigoField}
            onChange={(event) => {
              const onlyDigits = event.target.value.replace(/\D/g, '').slice(0, 6)
              setValue('codigo', onlyDigits, { shouldValidate: Boolean(errors.codigo) })
            }}
          />

          <div className="flex flex-col-reverse gap-2 pt-2 sm:flex-row sm:justify-end">
            <Button type="button" variant="secondary" onClick={onClose} disabled={loading}>
              Cancelar
            </Button>
            <Button
              type="submit"
              variant="transportador"
              loading={loading}
              loadingLabel="Validando..."
            >
              {isRetirada ? 'Confirmar retirada' : 'Confirmar entrega'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
