import {
  AlertTriangle,
  BookmarkCheck,
  CheckCircle2,
  Clock,
  Flame,
  KeyRound,
  Lock,
  PackageCheck,
  ShieldAlert,
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
  LABELS_SITUACAO_TRANSPORTADOR,
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
  | 'AGUARDANDO_AUTORIZACAO_DOADOR'
  | 'AGUARDANDO_CONFIRMACAO_RETIRADA'
  | 'EM_TRANSPORTE'
  | 'EM_INVESTIGACAO'
  | 'ATIVO'
  | 'SUSPENSO'

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
  AGUARDANDO_AUTORIZACAO_DOADOR: {
    label: LABELS_SITUACAO_RESERVA.AGUARDANDO_AUTORIZACAO_DOADOR,
    tone: 'warning',
    icon: <Clock className="h-3.5 w-3.5" />,
  },
  AGUARDANDO_CONFIRMACAO_RETIRADA: {
    label: LABELS_SITUACAO_RESERVA.AGUARDANDO_CONFIRMACAO_RETIRADA,
    tone: 'info',
    icon: <KeyRound className="h-3.5 w-3.5" />,
  },
  EM_TRANSPORTE: {
    label: LABELS_SITUACAO_RESERVA.EM_TRANSPORTE,
    tone: 'transportador',
    icon: <Truck className="h-3.5 w-3.5" />,
  },
  EM_INVESTIGACAO: {
    label: LABELS_SITUACAO_RESERVA.EM_INVESTIGACAO,
    tone: 'danger',
    icon: <AlertTriangle className="h-3.5 w-3.5" />,
  },
  ATIVO: {
    label: LABELS_SITUACAO_TRANSPORTADOR.ATIVO,
    tone: 'success',
    icon: <CheckCircle2 className="h-3.5 w-3.5" />,
  },
  SUSPENSO: {
    label: LABELS_SITUACAO_TRANSPORTADOR.SUSPENSO,
    tone: 'danger',
    icon: <ShieldAlert className="h-3.5 w-3.5" />,
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
