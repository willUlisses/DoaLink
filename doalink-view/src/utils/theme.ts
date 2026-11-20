import type { TipoUsuario } from '../types/enums.ts'

export interface ProfileThemeTokens {
  label: string
  solidBg: string
  solidHoverBg: string
  softBg: string
  softText: string
  border: string
  accentBar: string
  ring: string
}

export const PROFILE_THEME: Record<TipoUsuario, ProfileThemeTokens> = {
  DOADOR: {
    label: 'Doador',
    solidBg: 'bg-doador-aa',
    solidHoverBg: 'hover:bg-doador-hover',
    softBg: 'bg-doador-soft',
    softText: 'text-doador-aa',
    border: 'border-doador-border',
    accentBar: 'border-t-doador',
    ring: 'focus-visible:ring-doador-aa',
  },
  TRANSPORTADOR: {
    label: 'Transportador',
    solidBg: 'bg-transportador-aa',
    solidHoverBg: 'hover:bg-transportador-hover',
    softBg: 'bg-transportador-soft',
    softText: 'text-transportador-aa',
    border: 'border-transportador-border',
    accentBar: 'border-t-transportador',
    ring: 'focus-visible:ring-transportador-aa',
  },
  RECEPTOR: {
    label: 'Receptor (ONG)',
    solidBg: 'bg-receptor-aa',
    solidHoverBg: 'hover:bg-receptor-hover',
    softBg: 'bg-receptor-soft',
    softText: 'text-receptor-aa',
    border: 'border-receptor-border',
    accentBar: 'border-t-receptor',
    ring: 'focus-visible:ring-receptor-aa',
  },
}

export type ProfileButtonVariant = 'primary' | 'doador' | 'transportador' | 'receptor'

export function variantForProfile(tipoUsuario?: TipoUsuario | null): ProfileButtonVariant {
  if (tipoUsuario === 'DOADOR') return 'doador'
  if (tipoUsuario === 'TRANSPORTADOR') return 'transportador'
  if (tipoUsuario === 'RECEPTOR') return 'receptor'
  return 'primary'
}
