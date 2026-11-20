import { Calendar, MapPin, Package, User } from 'lucide-react'
import type { ReactNode } from 'react'
import { Badge } from '../../components/ui/Badge.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { StatusBadge } from '../../components/ui/StatusBadge.tsx'
import type { OfertaDoacaoResponse } from '../../types/oferta.ts'
import {
  formatarDataHora,
  formatarEnderecoCompleto,
  formatarEnderecoResumo,
  formatarQuantidade,
} from '../../utils/formatters.ts'
import { LABELS_CATEGORIA_ITEM, SIGLAS_UNIDADE } from '../../utils/labels.ts'

interface OfertaCardProps {
  oferta: OfertaDoacaoResponse
  variant?: 'public' | 'owner'
  actions?: ReactNode
}

export function OfertaCard({ oferta, variant = 'public', actions }: OfertaCardProps) {
  const localTexto =
    variant === 'owner'
      ? oferta.localRetirada
        ? formatarEnderecoCompleto(oferta.localRetirada)
        : 'Retirada a combinar após reserva'
      : formatarEnderecoResumo(oferta.localRetirada)

  return (
    <Card className="flex h-full flex-col justify-between gap-4 border-t-4 border-t-doador">
      <div className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
            Oferta #{oferta.id}
          </span>
          <StatusBadge situacao={oferta.situacao} />
        </div>

        <div className="space-y-1.5 text-sm">
          {oferta.doador ? (
            <div className="flex items-center gap-2 text-ink">
              <User className="h-4 w-4 shrink-0 text-ink-secondary" aria-hidden="true" />
              <span className="font-medium truncate">{oferta.doador.nome}</span>
            </div>
          ) : null}

          <div className="flex items-start gap-2 text-ink-secondary">
            <MapPin className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="text-xs leading-relaxed">{localTexto}</span>
          </div>

          <div className="flex items-center gap-2 text-ink-muted">
            <Calendar className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="text-xs">Publicada em {formatarDataHora(oferta.criadoEm)}</span>
          </div>
        </div>

        <div className="border-t border-border pt-3">
          <p className="mb-2 flex items-center gap-1.5 text-xs font-semibold text-ink-secondary">
            <Package className="h-3.5 w-3.5" aria-hidden="true" />
            <span>
              {oferta.itens.length} {oferta.itens.length === 1 ? 'item' : 'itens'} ofertados
            </span>
          </p>
          <ul className="space-y-1.5">
            {oferta.itens.slice(0, 3).map((item) => (
              <li
                key={item.id}
                className="flex items-center justify-between gap-2 rounded-[8px] bg-canvas px-2.5 py-1.5 text-xs"
              >
                <span className="min-w-0 truncate text-ink">
                  <span className="font-medium">{item.descricao}</span>
                  <span className="ml-1.5 text-ink-muted">
                    ({LABELS_CATEGORIA_ITEM[item.tipoItemDoacao]})
                  </span>
                </span>
                <Badge tone="neutral" className="shrink-0">
                  {formatarQuantidade(item.quantidade)} {SIGLAS_UNIDADE[item.tipoUnidade]}
                </Badge>
              </li>
            ))}
          </ul>
          {oferta.itens.length > 3 ? (
            <p className="mt-1.5 text-xs font-medium text-ink-secondary">
              + {oferta.itens.length - 3}{' '}
              {oferta.itens.length - 3 === 1 ? 'outro item' : 'outros itens'}
            </p>
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

