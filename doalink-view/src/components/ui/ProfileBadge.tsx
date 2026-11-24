import { Building2, HeartHandshake, Truck } from 'lucide-react'
import type { TipoUsuario } from '../../types/enums.ts'
import { LABELS_TIPO_USUARIO } from '../../utils/labels.ts'
import { Badge, type BadgeTone } from './Badge.tsx'

const PROFILE_BADGE_MAP: Record<
  TipoUsuario,
  { tone: BadgeTone; Icon: typeof HeartHandshake }
> = {
  DOADOR: { tone: 'doador', Icon: HeartHandshake },
  TRANSPORTADOR: { tone: 'transportador', Icon: Truck },
  RECEPTOR: { tone: 'receptor', Icon: Building2 },
}

export function ProfileBadge({
  tipoUsuario,
  className = '',
}: {
  tipoUsuario: TipoUsuario
  className?: string
}) {
  const { tone, Icon } = PROFILE_BADGE_MAP[tipoUsuario]
  return (
    <Badge
      tone={tone}
      icon={<Icon className="h-3.5 w-3.5" aria-hidden="true" />}
      className={className}
    >
      {LABELS_TIPO_USUARIO[tipoUsuario]}
    </Badge>
  )
}

