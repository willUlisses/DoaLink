import { createContext } from 'react'
import type { UsuarioResponse } from '../types/auth.ts'

export type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated'

export interface AuthContextValue {
  user: UsuarioResponse | null
  status: AuthStatus
  pendingVerificationEmail: string | null
  setAuthenticatedUser: (user: UsuarioResponse) => void
  setPendingVerificationEmail: (email: string | null) => void
  logoutUser: () => Promise<void>
  clearSession: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

