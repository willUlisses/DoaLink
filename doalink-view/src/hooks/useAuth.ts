import { use } from 'react'
import { AuthContext, type AuthContextValue } from '../contexts/auth-context.ts'

export function useAuth(): AuthContextValue {
  const ctx = use(AuthContext)
  if (!ctx) {
    throw new Error('useAuth deve ser utilizado dentro de AuthProvider.')
  }
  return ctx
}

