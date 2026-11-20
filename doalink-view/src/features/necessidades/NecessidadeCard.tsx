import { Building2, Calendar, ClipboardList, MapPin } from 'lucide-react'
import type { ReactNode } from 'react'
import { Badge } from '../../components/ui/Badge.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { StatusBadge } from '../../components/ui/StatusBadge.tsx'
import type { NecessidadeDoacaoResponse } from '../../types/necessidade.ts'
import {
  formatarDataHora,
  formatarEnderecoCompleto,
  formatarEnderecoResumo,
  formatarQuantidade,
} from '../../utils/formatters.ts'
import { LABELS_CATEGORIA_ITEM, SIGLAS_UNIDADE } from '../../utils/labels.ts'

interface NecessidadeCardProps {
  necessidade: NecessidadeDoacaoResponse
  variant?: 'public' | 'owner'
  actions?: ReactNode
}

export function NecessidadeCard({
  necessidade,
  variant = 'public',
  actions,
}: NecessidadeCardProps) {
  const localTexto =
    variant === 'owner'
      ? formatarEnderecoCompleto(necessidade.localEntrega)
      : formatarEnderecoResumo(necessidade.localEntrega)

  return (
    <Card className="flex h-full flex-col justify-between gap-4 border-t-4 border-t-receptor">
      <div className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
            Necessidade #{necessidade.id}
          </span>
          <StatusBadge situacao={necessidade.situacao} />
        </div>

        <div className="space-y-1.5 text-sm">
          <div className="flex items-center gap-2 text-ink">
            <Building2 className="h-4 w-4 shrink-0 text-receptor-aa" aria-hidden="true" />
            <span className="font-medium truncate">{necessidade.receptor.nome}</span>
          </div>

          <div className="flex items-start gap-2 text-ink-secondary">
            <MapPin className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="text-xs leading-relaxed">{localTexto}</span>
          </div>

          <div className="flex items-center gap-2 text-ink-muted">
            <Calendar className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="text-xs">Registrada em {formatarDataHora(necessidade.criadoEm)}</span>
          </div>
        </div>

        <div className="border-t border-border pt-3">
          <p className="mb-2 flex items-center gap-1.5 text-xs font-semibold text-ink-secondary">
            <ClipboardList className="h-3.5 w-3.5" aria-hidden="true" />
            <span>
              {necessidade.itens.length}{' '}
              {necessidade.itens.length === 1 ? 'item solicitado' : 'itens solicitados'}
            </span>
          </p>
          <ul className="space-y-1.5">
            {necessidade.itens.slice(0, 3).map((item) => (
              <li
                key={item.id}
                className="flex items-center justify-between gap-2 rounded-[8px] bg-canvas px-2.5 py-1.5 text-xs"
              >
                <span className="min-w-0 truncate text-ink">
                  <span className="font-medium">
                    {LABELS_CATEGORIA_ITEM[item.tipoItemDoacao]}
                  </span>
                  {item.descricao ? (
                    <span className="ml-1.5 text-ink-secondary">— {item.descricao}</span>
                  ) : null}
                </span>
                <Badge tone="neutral" className="shrink-0">
                  {formatarQuantidade(item.quantidade)} {SIGLAS_UNIDADE[item.tipoUnidade]}
                </Badge>
              </li>
            ))}
          </ul>
          {necessidade.itens.length > 3 ? (
            <p className="mt-1.5 text-xs font-medium text-ink-secondary">
              + {necessidade.itens.length - 3}{' '}
              {necessidade.itens.length - 3 === 1 ? 'outro item' : 'outros itens'}
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

