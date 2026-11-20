import { zodResolver } from '@hookform/resolvers/zod'
import { X } from 'lucide-react'
import { useEffect, useId, useMemo, useRef } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { InfoBanner } from '../../components/feedback/InfoBanner.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Select } from '../../components/ui/Select.tsx'
import {
  vincularTransporteSchema,
  type VincularTransporteFormValues,
} from '../../schemas/reserva.schema.ts'
import type { ReservaEntregaResponse } from '../../types/reserva.ts'
import type { DisponibilidadeTransporteResponse } from '../../types/transporte.ts'
import {
  formatarData,
  formatarQuantidade,
  obterDataLocalHojeISO,
} from '../../utils/formatters.ts'
import { LABELS_VEICULO, SIGLAS_UNIDADE } from '../../utils/labels.ts'

interface VincularTransporteDialogProps {
  open: boolean
  reserva: ReservaEntregaResponse | null
  disponibilidades: DisponibilidadeTransporteResponse[]
  loading: boolean
  errorMessage: string | null
  onConfirm: (disponibilidadeId: number) => void
  onClose: () => void
}

export function VincularTransporteDialog({
  open,
  reserva,
  disponibilidades,
  loading,
  errorMessage,
  onConfirm,
  onClose,
}: VincularTransporteDialogProps) {
  const titleId = useId()
  const dialogRef = useRef<HTMLDivElement>(null)
  const previousFocusRef = useRef<HTMLElement | null>(null)

  const hojeISO = obterDataLocalHojeISO()

  const cargaTotal = useMemo(() => {
    if (!reserva) return 0
    return reserva.itens.reduce((acc, item) => acc + item.quantidade, 0)
  }, [reserva])

  const unidadesReserva = useMemo(() => {
    if (!reserva) return []
    return Array.from(new Set(reserva.itens.map((i) => i.tipoUnidade)))
  }, [reserva])

  const disponibilidadesCompatíveis = useMemo(() => {
    return disponibilidades.filter((d) => {
      const ativa = d.situacao === 'DISPONIVEL' || d.situacao === 'RESERVADA'
      const naoExpirada = d.dataDisponivel >= hojeISO
      const unidadeOk =
        unidadesReserva.length === 1 && unidadesReserva[0] === d.tipoUnidade
      const capacidadeOk = d.capacidadeRestante >= cargaTotal
      return ativa && naoExpirada && unidadeOk && capacidadeOk
    })
  }, [disponibilidades, hojeISO, unidadesReserva, cargaTotal])

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<VincularTransporteFormValues>({
    resolver: zodResolver(vincularTransporteSchema),
    defaultValues: { disponibilidadeId: '' },
  })

  useEffect(() => {
    if (!open) return
    previousFocusRef.current =
      document.activeElement instanceof HTMLElement ? document.activeElement : null
    reset({
      disponibilidadeId:
        disponibilidadesCompatíveis.length === 1
          ? String(disponibilidadesCompatíveis[0].id)
          : '',
    })
    dialogRef.current?.focus()

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !loading) {
        onClose()
        return
      }
      if (event.key === 'Tab' && dialogRef.current) {
        const focusables = Array.from(
          dialogRef.current.querySelectorAll<HTMLElement>(
            'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])',
          ),
        )
        if (focusables.length === 0) return
        const first = focusables[0]
        const last = focusables[focusables.length - 1]
        if (event.shiftKey && document.activeElement === first) {
          event.preventDefault()
          last.focus()
        } else if (!event.shiftKey && document.activeElement === last) {
          event.preventDefault()
          first.focus()
        }
      }
    }
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('keydown', onKeyDown)
      previousFocusRef.current?.focus()
    }
  }, [open, loading, onClose, reset, disponibilidadesCompatíveis])

  if (!open || !reserva) return null

  const options = disponibilidadesCompatíveis.map((d) => ({
    value: String(d.id),
    label: `${LABELS_VEICULO[d.tipoVeiculo]} — ${formatarData(d.dataDisponivel)} (saldo: ${formatarQuantidade(d.capacidadeRestante)} ${SIGLAS_UNIDADE[d.tipoUnidade]})`,
  }))

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/45 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !loading) {
          onClose()
        }
      }}
    >
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
        className="w-full max-w-lg rounded-[12px] border border-border bg-surface p-6 shadow-[var(--shadow-elevated)]"
      >
        <div className="flex items-start justify-between gap-4">
          <h2 id={titleId} className="text-lg font-bold text-ink">
            Assumir entrega da Reserva #{reserva.id}
          </h2>
          <button
            type="button"
            disabled={loading}
            onClick={onClose}
            aria-label="Fechar diálogo"
            className="rounded-[8px] p-1 text-ink-secondary hover:bg-surface-subtle hover:text-ink cursor-pointer"
          >
            <X className="h-4 w-4" aria-hidden="true" />
          </button>
        </div>

        <p className="mt-2 text-sm text-ink-secondary">
          Carga total dos itens:{' '}
          <strong className="text-ink">
            {formatarQuantidade(cargaTotal)}{' '}
            {unidadesReserva.length === 1 ? SIGLAS_UNIDADE[unidadesReserva[0]] : 'unidades mistas'}
          </strong>
          . Após vincular, os endereços completos de retirada e entrega serão liberados.
        </p>

        {unidadesReserva.length > 1 ? (
          <div className="mt-4">
            <InfoBanner tone="warning" title="Unidades incompatíveis na reserva">
              Esta reserva possui itens em unidades diferentes ({unidadesReserva.join(', ')}). A
              API exige que todos os itens possuam a mesma unidade da disponibilidade selecionada.
            </InfoBanner>
          </div>
        ) : null}

        {errorMessage ? (
          <div className="mt-4">
            <InfoBanner tone="danger">{errorMessage}</InfoBanner>
          </div>
        ) : null}

        {disponibilidadesCompatíveis.length === 0 ? (
          <div className="mt-4 space-y-4">
            <InfoBanner tone="info" title="Nenhuma disponibilidade compatível ativa">
              Você precisa de uma disponibilidade ativa (com data igual ou posterior a hoje, mesma
              unidade dos itens e saldo suficiente) para assumir esta entrega.
            </InfoBanner>
            <div className="flex justify-end gap-2">
              <Button variant="secondary" onClick={onClose}>
                Fechar
              </Button>
              <Link
                to="/app/disponibilidades"
                className="inline-flex min-h-10 items-center justify-center rounded-[8px] bg-transportador-aa px-4 py-2 text-sm font-semibold text-white hover:bg-transportador-hover"
              >
                Cadastrar disponibilidade
              </Link>
            </div>
          </div>
        ) : (
          <form
            onSubmit={handleSubmit((values) => onConfirm(Number(values.disponibilidadeId)))}
            noValidate
            className="mt-4 space-y-5"
          >
            <Select
              label="Escolha sua disponibilidade de transporte"
              placeholder="Selecione uma disponibilidade compatível"
              options={options}
              required
              error={errors.disponibilidadeId?.message}
              {...register('disponibilidadeId')}
            />

            <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <Button variant="secondary" onClick={onClose} disabled={loading}>
                Cancelar
              </Button>
              <Button
                type="submit"
                variant="transportador"
                loading={loading}
                loadingLabel="Vinculando..."
              >
                Vincular e assumir entrega
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
