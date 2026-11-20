import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowRight, LogIn } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { InfoBanner } from '../../components/feedback/InfoBanner.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { Input } from '../../components/ui/Input.tsx'
import { PasswordInput } from '../../components/ui/PasswordInput.tsx'
import { useAuth } from '../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../hooks/useDocumentTitle.ts'
import { useToast } from '../../hooks/useToast.ts'
import { loginSchema, type LoginFormValues } from '../../schemas/auth.schema.ts'
import { isNormalizedApiError } from '../../services/api.ts'
import { login } from '../../services/auth.service.ts'
import { applyApiErrorsToForm } from '../../utils/formErrors.ts'

interface LocationState {
  from?: {
    pathname: string
    search?: string
  }
}

export function LoginPage() {
  useDocumentTitle('Entrar')
  const { setAuthenticatedUser, setPendingVerificationEmail } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const [formError, setFormError] = useState<string | null>(null)
  const [unverifiedEmail, setUnverifiedEmail] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      senha: '',
    },
  })

  const state = location.state as LocationState | null
  const redirectTarget =
    state?.from?.pathname && state.from.pathname.startsWith('/app')
      ? `${state.from.pathname}${state.from.search ?? ''}`
      : '/app'

  const onSubmit = async (values: LoginFormValues) => {
    setFormError(null)
    setUnverifiedEmail(null)

    try {
      const usuario = await login({
        email: values.email.trim().toLowerCase(),
        senha: values.senha,
      })
      setAuthenticatedUser(usuario)
      showToast({
        variant: 'success',
        title: `Bem-vindo(a), ${usuario.nome}`,
        description: 'Sessão iniciada com sucesso.',
      })
      navigate(redirectTarget, { replace: true })
    } catch (error) {
      if (isNormalizedApiError(error) && error.code === 'EMAIL_NOT_VERIFIED') {
        const normalizedEmail = values.email.trim().toLowerCase()
        setPendingVerificationEmail(normalizedEmail)
        setUnverifiedEmail(normalizedEmail)
        setFormError(error.message)
        return
      }
      const message = applyApiErrorsToForm(error, setError, setFocus)
      setFormError(message)
    }
  }

  return (
    <div className="mx-auto flex max-w-md flex-col px-4 py-10 sm:py-14">
      <Card padding="lg">
        <div className="mb-6">
          <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-[10px] bg-surface-subtle text-ink">
            <LogIn className="h-5 w-5" aria-hidden="true" />
          </div>
          <h1 className="text-2xl font-bold text-ink">Entrar no DoaLink</h1>
          <p className="mt-1 text-sm text-ink-secondary">
            Acesse sua conta com o e-mail e a senha cadastrados.
          </p>
        </div>

        {formError ? (
          <div className="mb-5">
            <InfoBanner
              tone={unverifiedEmail ? 'warning' : 'danger'}
              title={unverifiedEmail ? 'Confirmação de e-mail pendente' : 'Não foi possível entrar'}
              action={
                unverifiedEmail ? (
                  <Button
                    size="sm"
                    variant="secondary"
                    onClick={() =>
                      navigate('/verificar-email', { state: { email: unverifiedEmail } })
                    }
                  >
                    Confirmar e-mail
                  </Button>
                ) : undefined
              }
            >
              {formError}
            </InfoBanner>
          </div>
        ) : null}

        <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
          <Input
            label="E-mail"
            type="email"
            autoComplete="email"
            placeholder="nome@organizacao.org.br"
            required
            error={errors.email?.message}
            {...register('email')}
          />

          <PasswordInput
            label="Senha"
            autoComplete="current-password"
            placeholder="Digite sua senha"
            required
            error={errors.senha?.message}
            {...register('senha')}
          />

          <div className="flex items-center justify-end">
            <Link
              to="/recuperar-senha"
              className="text-xs font-medium text-ink-secondary hover:text-ink hover:underline"
            >
              Esqueci minha senha
            </Link>
          </div>

          <Button
            type="submit"
            size="lg"
            loading={isSubmitting}
            loadingLabel="Entrando..."
            className="w-full"
            leftIcon={<ArrowRight className="h-4 w-4" />}
          >
            Entrar
          </Button>
        </form>

        <p className="mt-6 border-t border-border pt-5 text-center text-sm text-ink-secondary">
          Ainda não tem conta?{' '}
          <Link to="/cadastro" className="font-semibold text-ink hover:underline">
            Criar cadastro
          </Link>
        </p>
      </Card>
    </div>
  )
}

