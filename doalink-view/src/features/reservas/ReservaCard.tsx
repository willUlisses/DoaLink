import {
  AlertTriangle,
  Building2,
  Calendar,
  Clock,
  KeyRound,
  MapPin,
  Package,
  ShieldCheck,
  Truck,
  User,
} from 'lucide-react'
import type { ReactNode } from 'react'
import { Badge } from '../../components/ui/Badge.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { StatusBadge } from '../../components/ui/StatusBadge.tsx'
import type { ReservaEntregaResponse } from '../../types/reserva.ts'
import {
  formatarDataHora,
  formatarEnderecoCompleto,
  formatarEnderecoResumo,
  formatarQuantidade,
} from '../../utils/formatters.ts'
import { LABELS_CATEGORIA_ITEM, SIGLAS_UNIDADE } from '../../utils/labels.ts'
import { formatarTelefone } from '../../utils/masks.ts'

interface ReservaCardProps {
  reserva: ReservaEntregaResponse
  showFullDetails?: boolean
  showTimeline?: boolean
  actions?: ReactNode
}

export function ReservaCard({
  reserva,
  showFullDetails = true,
  showTimeline = false,
  actions,
}: ReservaCardProps) {
  const retiradaTexto = showFullDetails
    ? reserva.enderecoRetirada
      ? formatarEnderecoCompleto(reserva.enderecoRetirada)
      : 'Local de retirada a combinar com o doador'
    : formatarEnderecoResumo(reserva.enderecoRetirada)

  const entregaTexto = showFullDetails
    ? formatarEnderecoCompleto(reserva.enderecoEntrega)
    : formatarEnderecoResumo(reserva.enderecoEntrega)

  return (
    <Card className="flex h-full flex-col justify-between gap-4">
      <div className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
              Reserva #{reserva.id}
            </span>
            {reserva.ofertaId ? <Badge tone="neutral">Oferta #{reserva.ofertaId}</Badge> : null}
          </div>
          <StatusBadge situacao={reserva.situacao} />
        </div>

        <div className="grid grid-cols-1 gap-2 rounded-button bg-canvas p-3 text-xs sm:grid-cols-2">
          <div>
            <p className="font-semibold text-ink-secondary">Retirada (Doador)</p>
            <p className="mt-0.5 flex items-start gap-1.5 text-ink">
              <MapPin className="mt-0.5 h-3.5 w-3.5 shrink-0 text-doador-aa" aria-hidden="true" />
              <span>{retiradaTexto}</span>
            </p>
            {showFullDetails && reserva.doador ? (
              <p className="mt-1 flex items-center gap-1.5 text-ink-secondary">
                <User className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
                <span>
                  {reserva.doador.nome} ({reserva.doador.email}
                  {reserva.doador.telefone
                    ? ` • ${formatarTelefone(reserva.doador.telefone)}`
                    : ''}
                  )
                </span>
              </p>
            ) : null}
          </div>

          <div>
            <p className="font-semibold text-ink-secondary">Entrega (Receptor)</p>
            <p className="mt-0.5 flex items-start gap-1.5 text-ink">
              <MapPin
                className="mt-0.5 h-3.5 w-3.5 shrink-0 text-receptor-aa"
                aria-hidden="true"
              />
              <span>{entregaTexto}</span>
            </p>
            {showFullDetails && reserva.receptor ? (
              <p className="mt-1 flex items-center gap-1.5 text-ink-secondary">
                <Building2 className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
                <span>
                  {reserva.receptor.nome} ({reserva.receptor.email}
                  {reserva.receptor.telefone
                    ? ` • ${formatarTelefone(reserva.receptor.telefone)}`
                    : ''}
                  )
                </span>
              </p>
            ) : null}
          </div>
        </div>

        {!reserva.enderecoRetirada && reserva.situacao === 'AGUARDANDO_RETIRADA' ? (
          <p className="rounded-button border border-info-border bg-info-soft px-3 py-2 text-xs text-ink-secondary">
            Oferta sem endereço cadastrado — entre em contato com o doador para combinar a retirada
            ou entrega.
          </p>
        ) : null}

        {reserva.transportador ? (
          <div className="flex items-center gap-2 rounded-button border border-transportador-border bg-transportador-soft px-3 py-2 text-xs text-transportador-aa">
            <Truck className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span>
              Transportador responsável: <strong>{reserva.transportador.nome}</strong>
              {showFullDetails ? ` (${reserva.transportador.email}` : ''}
              {showFullDetails && reserva.transportador.telefone
                ? ` • ${formatarTelefone(reserva.transportador.telefone)}`
                : ''}
              {showFullDetails ? ')' : ''}
            </span>
          </div>
        ) : (
          <p className="text-xs text-ink-secondary">
            Modalidade:{' '}
            <strong>
              {reserva.precisaTransporte
                ? 'Necessita de transportador'
                : 'Retirada direta pelo receptor'}
            </strong>
          </p>
        )}

        {/* Código de retirada (visível somente para o doador enquanto pendente) */}
        {reserva.codigoRetirada && reserva.situacao === 'AGUARDANDO_CONFIRMACAO_RETIRADA' ? (
          <div className="rounded-card border border-doador-border bg-doador-soft/70 p-3.5 space-y-1.5">
            <p className="flex items-center gap-1.5 text-xs font-bold uppercase tracking-wide text-doador-aa">
              <KeyRound className="h-3.5 w-3.5" aria-hidden="true" />
              <span>Código de confirmação de retirada</span>
            </p>
            <p className="font-mono text-2xl font-bold tracking-[0.25em] text-ink">
              {reserva.codigoRetirada}
            </p>
            <p className="text-xs leading-relaxed text-ink-secondary">
              Informe este código ao transportador somente no momento da entrega física dos itens
              para retirada.
            </p>
          </div>
        ) : null}

        {/* Código de entrega (visível somente para o receptor quando em transporte) */}
        {reserva.codigoEntrega && reserva.situacao === 'EM_TRANSPORTE' ? (
          <div className="rounded-card border border-receptor-border bg-receptor-soft/70 p-3.5 space-y-2">
            <p className="flex items-center gap-1.5 text-xs font-bold uppercase tracking-wide text-receptor-aa">
              <ShieldCheck className="h-4 w-4" aria-hidden="true" />
              <span>Transporte em andamento — Código de entrega</span>
            </p>
            <p className="font-mono text-2xl font-bold tracking-[0.25em] text-ink">
              {reserva.codigoEntrega}
            </p>
            {reserva.codigoEntregaExpiraEm ? (
              <p className="flex items-center gap-1.5 text-xs font-medium text-ink">
                <Clock className="h-3.5 w-3.5 text-receptor-aa" aria-hidden="true" />
                <span>
                  Validade de 24h (expira em {formatarDataHora(reserva.codigoEntregaExpiraEm)})
                </span>
              </p>
            ) : null}
            <p className="text-xs leading-relaxed text-ink-secondary">
              Este código será utilizado pelo motorista para confirmar o recebimento.{' '}
              <strong>Não compartilhe este código antes de receber os itens fisicamente.</strong>
            </p>
          </div>
        ) : null}

        {/* Prazo de 24h visível para o transportador em andamento */}
        {!reserva.codigoEntrega &&
        reserva.situacao === 'EM_TRANSPORTE' &&
        reserva.codigoEntregaExpiraEm ? (
          <div className="flex items-center gap-2 rounded-button border border-info-border bg-info-soft px-3 py-2 text-xs text-ink-secondary">
            <Clock className="h-4 w-4 shrink-0 text-info" aria-hidden="true" />
            <span>
              Prazo limite para confirmar a entrega (24h):{' '}
              <strong className="text-ink">{formatarDataHora(reserva.codigoEntregaExpiraEm)}</strong>
            </span>
          </div>
        ) : null}

        {reserva.situacao === 'EM_INVESTIGACAO' ? (
          <div className="flex items-start gap-2 rounded-button border border-danger-border bg-danger-soft px-3 py-2.5 text-xs text-danger">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            <span>
              Operação bloqueada: o prazo de 24 horas para confirmação da entrega expirou após a
              retirada. Ocorrência registrada para investigação.
            </span>
          </div>
        ) : null}

        <div className="border-t border-border pt-3">
          <p className="mb-2 flex items-center gap-1.5 text-xs font-semibold text-ink-secondary">
            <Package className="h-3.5 w-3.5" aria-hidden="true" />
            <span>Itens ({reserva.itens.length})</span>
          </p>
          <ul className="space-y-1.5">
            {reserva.itens.map((item) => {
              const itemTexto = `${item.descricao} (${LABELS_CATEGORIA_ITEM[item.tipoItemDoacao]})`
              return (
                <li
                  key={item.id}
                  className="flex items-center justify-between gap-2 rounded-button bg-canvas px-2.5 py-1.5 text-xs"
                >
                  <span className="min-w-0 truncate text-ink" title={itemTexto}>
                    <span className="font-medium">{item.descricao}</span>
                    <span className="ml-1.5 text-ink-muted">
                      ({LABELS_CATEGORIA_ITEM[item.tipoItemDoacao]})
                    </span>
                  </span>
                  <Badge tone="neutral" className="shrink-0">
                    {formatarQuantidade(item.quantidade)} {SIGLAS_UNIDADE[item.tipoUnidade]}
                  </Badge>
                </li>
              )
            })}
          </ul>
        </div>

        <div className="space-y-1 text-xs text-ink-muted">
          <p className="flex items-center gap-1.5">
            <Calendar className="h-3.5 w-3.5" aria-hidden="true" />
            <span>Reservada em {formatarDataHora(reserva.criadoEm)}</span>
          </p>
          {showTimeline || reserva.situacao === 'CONCLUIDA' ? (
            <div className="grid grid-cols-1 gap-1 pt-1 sm:grid-cols-2">
              {reserva.aceitoEm ? (
                <p>Aceita em: {formatarDataHora(reserva.aceitoEm)}</p>
              ) : null}
              {reserva.autorizadoEm ? (
                <p>Autorizada em: {formatarDataHora(reserva.autorizadoEm)}</p>
              ) : null}
              {reserva.retiradaConfirmadaEm ? (
                <p>Retirada em: {formatarDataHora(reserva.retiradaConfirmadaEm)}</p>
              ) : null}
              {reserva.entregaConfirmadaEm ? (
                <p className="font-semibold text-success">
                  Entregue em: {formatarDataHora(reserva.entregaConfirmadaEm)}
                </p>
              ) : null}
            </div>
          ) : null}
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
