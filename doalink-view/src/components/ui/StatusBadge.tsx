import {
  BookmarkCheck,
  CheckCircle2,
  Clock,
  Flame,
  Lock,
  PackageCheck,
  Sparkles,
  Truck,
  XCircle,
} from 'lucide-react'
import type { ReactNode } from 'react'
import { Badge, type BadgeTone } from './Badge.tsx'

export type AnySituacao =
  | 'DISPONIVEL'
  | 'RESERVADA'
  | 'CONCLUIDA'
  | 'CANCELADA'
  | 'ABERTA'
  | 'ATENDIDA'
  | 'ATIVA'
  | 'ENCERRADA'
  | 'AGUARDANDO_TRANSPORTE'
  | 'AGUARDANDO_RETIRADA'
  | 'TRANSPORTE_VINCULADO'

interface StatusMeta {
  label: string
  tone: BadgeTone
  icon: ReactNode
}

const STATUS_META: Record<AnySituacao, StatusMeta> = {
  DISPONIVEL: {
    label: 'Disponível',
    tone: 'success',
    icon: <Sparkles className="h-3.5 w-3.5" />,
  },
  ABERTA: {
    label: 'Aberta',
    tone: 'success',
    icon: <Sparkles className="h-3.5 w-3.5" />,
  },
  ATIVA: {
    label: 'Ativa',
    tone: 'danger',
    icon: <Flame className="h-3.5 w-3.5" />,
  },
  RESERVADA: {
    label: 'Reservada',
    tone: 'info',
    icon: <BookmarkCheck className="h-3.5 w-3.5" />,
  },
  AGUARDANDO_TRANSPORTE: {
    label: 'Aguardando transporte',
    tone: 'warning',
    icon: <Clock className="h-3.5 w-3.5" />,
  },
  AGUARDANDO_RETIRADA: {
    label: 'Aguardando retirada',
    tone: 'info',
    icon: <PackageCheck className="h-3.5 w-3.5" />,
  },
  TRANSPORTE_VINCULADO: {
    label: 'Transporte vinculado',
    tone: 'transportador',
    icon: <Truck className="h-3.5 w-3.5" />,
  },
  CONCLUIDA: {
    label: 'Concluída',
    tone: 'success',
    icon: <CheckCircle2 className="h-3.5 w-3.5" />,
  },
  ATENDIDA: {
    label: 'Atendida',
    tone: 'success',
    icon: <CheckCircle2 className="h-3.5 w-3.5" />,
  },
  ENCERRADA: {
    label: 'Encerrada',
    tone: 'neutral',
    icon: <Lock className="h-3.5 w-3.5" />,
  },
  CANCELADA: {
    label: 'Cancelada',
    tone: 'danger',
    icon: <XCircle className="h-3.5 w-3.5" />,
  },
}

export function StatusBadge({
  situacao,
  className = '',
}: {
  situacao: AnySituacao
  className?: string
}) {
  const meta = STATUS_META[situacao]
  return (
    <Badge tone={meta.tone} icon={meta.icon} className={className}>
      {meta.label}
    </Badge>
  )
}

