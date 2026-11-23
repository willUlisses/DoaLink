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
import {
  LABELS_SITUACAO_CAMPANHA,
  LABELS_SITUACAO_NECESSIDADE,
  LABELS_SITUACAO_OFERTA,
  LABELS_SITUACAO_RESERVA,
} from '../../utils/labels.ts'
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
    label: LABELS_SITUACAO_OFERTA.DISPONIVEL,
    tone: 'success',
    icon: <Sparkles className="h-3.5 w-3.5" />,
  },
  ABERTA: {
    label: LABELS_SITUACAO_NECESSIDADE.ABERTA,
    tone: 'success',
    icon: <Sparkles className="h-3.5 w-3.5" />,
  },
  ATIVA: {
    label: LABELS_SITUACAO_CAMPANHA.ATIVA,
    tone: 'danger',
    icon: <Flame className="h-3.5 w-3.5" />,
  },
  RESERVADA: {
    label: LABELS_SITUACAO_OFERTA.RESERVADA,
    tone: 'info',
    icon: <BookmarkCheck className="h-3.5 w-3.5" />,
  },
  AGUARDANDO_TRANSPORTE: {
    label: LABELS_SITUACAO_RESERVA.AGUARDANDO_TRANSPORTE,
    tone: 'warning',
    icon: <Clock className="h-3.5 w-3.5" />,
  },
  AGUARDANDO_RETIRADA: {
    label: LABELS_SITUACAO_RESERVA.AGUARDANDO_RETIRADA,
    tone: 'info',
    icon: <PackageCheck className="h-3.5 w-3.5" />,
  },
  TRANSPORTE_VINCULADO: {
    label: LABELS_SITUACAO_RESERVA.TRANSPORTE_VINCULADO,
    tone: 'transportador',
    icon: <Truck className="h-3.5 w-3.5" />,
  },
  CONCLUIDA: {
    label: LABELS_SITUACAO_OFERTA.CONCLUIDA,
    tone: 'success',
    icon: <CheckCircle2 className="h-3.5 w-3.5" />,
  },
  ATENDIDA: {
    label: LABELS_SITUACAO_NECESSIDADE.ATENDIDA,
    tone: 'success',
    icon: <CheckCircle2 className="h-3.5 w-3.5" />,
  },
  ENCERRADA: {
    label: LABELS_SITUACAO_CAMPANHA.ENCERRADA,
    tone: 'neutral',
    icon: <Lock className="h-3.5 w-3.5" />,
  },
  CANCELADA: {
    label: LABELS_SITUACAO_OFERTA.CANCELADA,
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

