import { Building2, ChevronRight, HeartHandshake, Truck } from 'lucide-react'
import { useId } from 'react'
import { TIPO_USUARIO, type TipoUsuario } from '../../types/enums.ts'
import { DESCRICOES_TIPO_USUARIO, LABELS_TIPO_USUARIO } from '../../utils/labels.ts'

interface ProfileSelectorProps {
  value: TipoUsuario
  onChange: (value: TipoUsuario) => void
  error?: string
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

export function ProfileSelector({ value, onChange, error }: ProfileSelectorProps) {
  const groupId = useId()

  return (
    <fieldset className="flex flex-col gap-2">
      <legend id={groupId} className="mb-0.5 text-sm font-medium text-ink">
        Qual é o seu perfil no DoaLink?
        <span className="ml-1 text-danger" aria-hidden="true">
          *
        </span>
      </legend>
      <div role="radiogroup" aria-labelledby={groupId} className="grid grid-cols-1 gap-3">
        {PROFILE_OPTIONS.map((option) => {
          const isSelected = value === option.value
          const Icon = option.Icon
          return (
            <button
              key={option.value}
              type="button"
              role="radio"
              aria-checked={isSelected}
              onClick={() => onChange(option.value)}
              className={`flex w-full items-center gap-3.5 rounded-card border p-4 text-left cursor-pointer transition-colors duration-150 ${
                isSelected
                  ? option.selectedClass
                  : 'border-border bg-surface hover:border-border-strong'
              }`}
            >
              <span
                className={`inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-button ${option.iconBgClass}`}
                aria-hidden="true"
              >
                <Icon className="h-4 w-4" />
              </span>
              <span className="min-w-0 flex-1">
                <span className="block text-sm font-semibold text-ink">
                  {LABELS_TIPO_USUARIO[option.value]}
                </span>
                <span className="mt-0.5 block text-xs leading-snug text-ink-secondary">
                  {DESCRICOES_TIPO_USUARIO[option.value]}
                </span>
              </span>
              <ChevronRight className="h-4 w-4 shrink-0 text-ink-tertiary" aria-hidden="true" />
            </button>
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
