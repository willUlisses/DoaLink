import { Building2, HeartHandshake, Truck } from 'lucide-react'
import { useId } from 'react'
import { TIPO_USUARIO, type TipoUsuario } from '../../types/enums.ts'
import { DESCRICOES_TIPO_USUARIO, LABELS_TIPO_USUARIO } from '../../utils/labels.ts'

interface ProfileSelectorProps {
  value: TipoUsuario
  onChange: (value: TipoUsuario) => void
  error?: string
  compact?: boolean
}

const PROFILE_OPTIONS: Array<{
  value: TipoUsuario
  Icon: typeof HeartHandshake
  selectedClass: string
  iconBgClass: string
}> = [
  {
    value: TIPO_USUARIO.DOADOR,
    Icon: HeartHandshake,
    selectedClass: 'border-doador-aa bg-doador-soft ring-1 ring-doador-aa',
    iconBgClass: 'bg-doador-soft text-doador-aa',
  },
  {
    value: TIPO_USUARIO.RECEPTOR,
    Icon: Building2,
    selectedClass: 'border-receptor-aa bg-receptor-soft ring-1 ring-receptor-aa',
    iconBgClass: 'bg-receptor-soft text-receptor-aa',
  },
  {
    value: TIPO_USUARIO.TRANSPORTADOR,
    Icon: Truck,
    selectedClass: 'border-transportador-aa bg-transportador-soft ring-1 ring-transportador-aa',
    iconBgClass: 'bg-transportador-soft text-transportador-aa',
  },
]

export function ProfileSelector({
  value,
  onChange,
  error,
  compact = false,
}: ProfileSelectorProps) {
  const groupId = useId()

  return (
    <fieldset className={`flex flex-col ${compact ? 'gap-1.5' : 'gap-2'}`}>
      <legend id={groupId} className="text-sm font-medium text-ink mb-0.5">
        Qual é o seu perfil no DoaLink?
        <span className="ml-1 text-danger" aria-hidden="true">
          *
        </span>
      </legend>
      <div
        role="radiogroup"
        aria-labelledby={groupId}
        className="grid grid-cols-1 gap-2.5 sm:grid-cols-3"
      >
        {PROFILE_OPTIONS.map((option) => {
          const isSelected = value === option.value
          const Icon = option.Icon
          return (
            <label
              key={option.value}
              className={`relative flex flex-col rounded-card border cursor-pointer transition-colors duration-150 ${
                compact ? 'gap-1 p-3' : 'gap-2 p-4'
              } ${
                isSelected
                  ? option.selectedClass
                  : 'border-border bg-surface hover:border-border-strong'
              }`}
            >
              <input
                type="radio"
                name="tipoUsuarioSelector"
                value={option.value}
                checked={isSelected}
                onChange={() => onChange(option.value)}
                className="sr-only"
              />
              <div className="flex items-center gap-2">
                <span
                  className={`inline-flex ${compact ? 'h-7 w-7' : 'h-8 w-8'} items-center justify-center rounded-button ${option.iconBgClass}`}
                  aria-hidden="true"
                >
                  <Icon className="h-4 w-4" />
                </span>
                <span className="text-sm font-semibold text-ink">
                  {LABELS_TIPO_USUARIO[option.value]}
                </span>
              </div>
              <p className={`text-xs text-ink-secondary ${compact ? 'leading-snug' : 'leading-relaxed'}`}>
                {DESCRICOES_TIPO_USUARIO[option.value]}
              </p>
            </label>
          )
        })}
      </div>
      {error ? (
        <p role="alert" className="text-xs font-medium text-danger">
          {error}
        </p>
      ) : null}
    </fieldset>
  )
}

