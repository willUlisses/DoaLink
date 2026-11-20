import { Building2, Calendar, Flame, MapPin } from 'lucide-react'
import type { ReactNode } from 'react'
import { Badge } from '../../components/ui/Badge.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { StatusBadge } from '../../components/ui/StatusBadge.tsx'
import type { CampanhaEmergencialResponse } from '../../types/campanha.ts'
import {
  formatarDataHora,
  formatarEnderecoCompleto,
  formatarEnderecoResumo,
  formatarQuantidade,
} from '../../utils/formatters.ts'
import { LABELS_CATEGORIA_ITEM, SIGLAS_UNIDADE } from '../../utils/labels.ts'

interface CampanhaCardProps {
  campanha: CampanhaEmergencialResponse
  variant?: 'public' | 'owner'
  actions?: ReactNode
}

export function CampanhaCard({
  campanha,
  variant = 'public',
  actions,
}: CampanhaCardProps) {
  const localTexto =
    variant === 'owner'
      ? formatarEnderecoCompleto(campanha.enderecoRecebimento)
      : formatarEnderecoResumo(campanha.enderecoRecebimento)

  return (
    <Card className="flex h-full flex-col justify-between gap-4 border-t-4 border-t-receptor">
      <div className="space-y-3">
        <div className="flex flex-wrap items-start justify-between gap-2">
          <div className="flex items-center gap-2 text-danger">
            <Flame className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="text-xs font-semibold uppercase tracking-wide">
              Campanha #{campanha.id}
            </span>
          </div>
          <StatusBadge situacao={campanha.situacao} />
        </div>

        <div>
          <h2 className="text-base font-bold text-ink">{campanha.nome}</h2>
          {campanha.descricao ? (
            <p className="mt-1 line-clamp-3 text-xs leading-relaxed text-ink-secondary">
              {campanha.descricao}
            </p>
          ) : null}
        </div>

        <div className="space-y-1.5 text-sm">
          {campanha.receptor ? (
            <div className="flex items-center gap-2 text-ink">
              <Building2 className="h-4 w-4 shrink-0 text-receptor-aa" aria-hidden="true" />
              <span className="truncate font-medium">{campanha.receptor.nome}</span>
            </div>
          ) : null}

          <div className="flex items-start gap-2 text-ink-secondary">
            <MapPin className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="text-xs leading-relaxed">{localTexto}</span>
          </div>

          <div className="flex items-center gap-2 text-ink-muted">
            <Calendar className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="text-xs">Criada em {formatarDataHora(campanha.criadoEm)}</span>
          </div>
        </div>

        {campanha.itens.length > 0 ? (
          <div className="border-t border-border pt-3">
            <p className="mb-2 text-xs font-semibold text-ink-secondary">
              Necessidades emergenciais ({campanha.itens.length})
            </p>
            <ul className="space-y-1.5">
              {campanha.itens.slice(0, 3).map((item) => (
                <li
                  key={item.id}
                  className="flex items-center justify-between gap-2 rounded-[8px] bg-canvas px-2.5 py-1.5 text-xs"
                >
                  <span className="min-w-0 truncate text-ink">
                    {LABELS_CATEGORIA_ITEM[item.tipoItemDoacao]}
                    {item.descricao ? ` — ${item.descricao}` : ''}
                  </span>
                  <Badge tone="neutral" className="shrink-0">
                    {formatarQuantidade(item.quantidade)} {SIGLAS_UNIDADE[item.tipoUnidade]}
                  </Badge>
                </li>
              ))}
            </ul>
            {campanha.itens.length > 3 ? (
              <p className="mt-1.5 text-xs font-medium text-ink-secondary">
                + {campanha.itens.length - 3}{' '}
                {campanha.itens.length - 3 === 1 ? 'outro item' : 'outros itens'}
              </p>
            ) : null}
          </div>
        ) : null}
      </div>

      {actions ? (
        <div className="flex flex-wrap items-center justify-end gap-2 border-t border-border pt-3">
          {actions}
        </div>
      ) : null}
    </Card>
  )
}

