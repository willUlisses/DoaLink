import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { configureApiAuthHandlers } from '../services/api.ts'
import { logout, refreshSession } from '../services/auth.service.ts'
import type { UsuarioResponse } from '../types/auth.ts'
import { useToast } from '../hooks/useToast.ts'
import { AuthContext, type AuthStatus } from './auth-context.ts'

interface AuthProviderProps {
  children: ReactNode
  skipInitialRefresh?: boolean
  initialUser?: UsuarioResponse | null
}

export function AuthProvider({
  children,
  skipInitialRefresh = false,
  initialUser = null,
}: AuthProviderProps) {
  const { showToast } = useToast()
  const [user, setUser] = useState<UsuarioResponse | null>(initialUser)
  const [status, setStatus] = useState<AuthStatus>(
    initialUser ? 'authenticated' : skipInitialRefresh ? 'unauthenticated' : 'loading',
  )
  const [pendingVerificationEmail, setPendingVerificationEmail] = useState<string | null>(null)

  // Espelho do usuário para leitura síncrona fora do render (evita setState de outro provider
  // dentro do updater de setUser, que dispara "Cannot update a component while rendering").
  const userRef = useRef<UsuarioResponse | null>(initialUser)

  useEffect(() => {
    userRef.current = user
  }, [user])

  const clearSession = useCallback(() => {
    setUser(null)
    setStatus('unauthenticated')
  }, [])

  const setAuthenticatedUser = useCallback((nextUser: UsuarioResponse) => {
    setUser(nextUser)
    setPendingVerificationEmail(null)
    setStatus('authenticated')
  }, [])

  const logoutUser = useCallback(async () => {
    try {
      await logout()
    } catch {
      // Encerra sessão local mesmo que a chamada de logout falhe por rede
    } finally {
      clearSession()
    }
  }, [clearSession])

  useEffect(() => {
    configureApiAuthHandlers({
      onSessionExpired: () => {
        if (userRef.current !== null) {
          showToast({
            variant: 'warning',
            title: 'Sessão expirada',
            description: 'Entre novamente para continuar utilizando o DoaLink.',
          })
        }
        userRef.current = null
        setUser(null)
        setStatus('unauthenticated')
      },
    })

    return () => {
      configureApiAuthHandlers({})
    }
  }, [showToast])

  useEffect(() => {
    if (skipInitialRefresh) return

    let active = true
    refreshSession()
      .then((usuario) => {
        if (!active) return
        setUser(usuario)
        setStatus('authenticated')
      })
      .catch(() => {
        if (!active) return
        setUser(null)
        setStatus('unauthenticated')
      })

    return () => {
      active = false
    }
  }, [skipInitialRefresh])

  const value = useMemo(
    () => ({
      user,
      status,
      pendingVerificationEmail,
      setAuthenticatedUser,
      setPendingVerificationEmail,
      logoutUser,
      clearSession,
    }),
    [
      user,
      status,
      pendingVerificationEmail,
      setAuthenticatedUser,
      logoutUser,
      clearSession,
    ],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}

