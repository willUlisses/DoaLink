import { Calendar, Clock, MapPin, Truck } from 'lucide-react'
import type { ReactNode } from 'react'
import { Card } from '../../components/ui/Card.tsx'
import { StatusBadge } from '../../components/ui/StatusBadge.tsx'
import type { DisponibilidadeTransporteResponse } from '../../types/transporte.ts'
import {
  formatarData,
  formatarEnderecoCompleto,
  formatarHora,
  formatarQuantidade,
} from '../../utils/formatters.ts'
import { LABELS_VEICULO, SIGLAS_UNIDADE } from '../../utils/labels.ts'

interface DisponibilidadeCardProps {
  disponibilidade: DisponibilidadeTransporteResponse
  actions?: ReactNode
}

export function DisponibilidadeCard({
  disponibilidade,
  actions,
}: DisponibilidadeCardProps) {
  const percentualRestante =
    disponibilidade.capacidade > 0
      ? Math.max(
          0,
          Math.min(100, (disponibilidade.capacidadeRestante / disponibilidade.capacidade) * 100),
        )
      : 0

  return (
    <Card className="flex h-full flex-col justify-between gap-4 border-t-4 border-t-transportador">
      <div className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2 text-transportador-aa">
            <Truck className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="text-sm font-bold text-ink">
              {LABELS_VEICULO[disponibilidade.tipoVeiculo]}
            </span>
          </div>
          <StatusBadge situacao={disponibilidade.situacao} />
        </div>

        <div className="space-y-1.5 text-xs text-ink-secondary">
          <div className="flex items-center gap-2">
            <Calendar className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span>
              Data disponível:{' '}
              <strong className="text-ink">{formatarData(disponibilidade.dataDisponivel)}</strong>
            </span>
          </div>
          <div className="flex items-center gap-2">
            <Clock className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span>Horário: {formatarHora(disponibilidade.horarioDisponivel)}</span>
          </div>
          <div className="flex items-start gap-2">
            <MapPin className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            <span>Origem: {formatarEnderecoCompleto(disponibilidade.origem)}</span>
          </div>
        </div>

        <div className="rounded-button border border-border bg-canvas p-3">
          <div className="flex items-center justify-between text-xs">
            <span className="font-medium text-ink-secondary">Capacidade disponível</span>
            <span className="font-bold text-ink tabular-nums">
              {formatarQuantidade(disponibilidade.capacidadeRestante)} /{' '}
              {formatarQuantidade(disponibilidade.capacidade)}{' '}
              {SIGLAS_UNIDADE[disponibilidade.tipoUnidade]}
            </span>
          </div>
          <div
            className="mt-2 h-2 w-full overflow-hidden rounded-full bg-surface-subtle"
            role="progressbar"
            aria-label="Saldo de capacidade restante"
            aria-valuenow={Math.round(percentualRestante)}
            aria-valuemin={0}
            aria-valuemax={100}
          >
            <div
              className="h-full rounded-full bg-transportador-aa transition-all duration-200"
              style={{ width: `${percentualRestante}%` }}
            />
          </div>
        </div>
      </div>

      {actions ? (
        <div className="flex flex-wrap items-center justify-end gap-2 border-t border-border pt-3">
          {actions}
        </div>
      ) : null}
    </Card>
  )
}

