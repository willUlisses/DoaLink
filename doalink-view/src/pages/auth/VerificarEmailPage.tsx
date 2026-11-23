import { zodResolver } from '@hookform/resolvers/zod'
import { MailCheck, RefreshCw, Timer } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { InfoBanner } from '../../components/feedback/InfoBanner.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { Input } from '../../components/ui/Input.tsx'
import { useAuth } from '../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../hooks/useDocumentTitle.ts'
import { useToast } from '../../hooks/useToast.ts'
import { verificarEmailSchema, type VerificarEmailFormValues } from '../../schemas/auth.schema.ts'
import { isNormalizedApiError } from '../../services/api.ts'
import { reenviarCodigo, verificarEmail } from '../../services/auth.service.ts'
import { applyApiErrorsToForm } from '../../utils/formErrors.ts'

const CODE_TTL_SECONDS = 90

interface VerifyLocationState {
  email?: string
}

export function VerificarEmailPage() {
  useDocumentTitle('Confirmar e-mail')
  const { pendingVerificationEmail, setAuthenticatedUser } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()

  const locationState = location.state as VerifyLocationState | null
  const initialEmail = locationState?.email ?? pendingVerificationEmail ?? ''

  const [secondsLeft, setSecondsLeft] = useState(CODE_TTL_SECONDS)
  const [resending, setResending] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [infoMessage, setInfoMessage] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    getValues,
    setValue,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<VerificarEmailFormValues>({
    resolver: zodResolver(verificarEmailSchema),
    defaultValues: {
      email: initialEmail,
      codigo: '',
    },
  })

  useEffect(() => {
    if (secondsLeft <= 0) return
    const timer = window.setInterval(() => {
      setSecondsLeft((prev) => (prev > 0 ? prev - 1 : 0))
    }, 1000)
    return () => window.clearInterval(timer)
  }, [secondsLeft])

  const isExpired = secondsLeft <= 0

  const onSubmit = async (values: VerificarEmailFormValues) => {
    setFormError(null)
    setInfoMessage(null)

    try {
      const usuario = await verificarEmail({
        email: values.email.trim().toLowerCase(),
        codigo: values.codigo.trim(),
      })
      setAuthenticatedUser(usuario)
      showToast({
        variant: 'success',
        title: 'E-mail confirmado',
        description: 'Sua conta foi ativada e sua sessão foi iniciada.',
      })
      navigate('/app', { replace: true })
    } catch (error) {
      if (isNormalizedApiError(error)) {
        if (error.message.toLowerCase().includes('expirado')) {
          setSecondsLeft(0)
        }
      }
      const message = applyApiErrorsToForm(error, setError, setFocus)
      setFormError(message)
    }
  }

  const handleResend = async () => {
    const email = getValues('email').trim().toLowerCase()
    if (!email) {
      setError('email', {
        type: 'manual',
        message: 'Informe o e-mail cadastrado para reenviar o código.',
      })
      setFocus('email')
      return
    }

    setResending(true)
    setFormError(null)
    setInfoMessage(null)

    try {
      const resp = await reenviarCodigo({ email })
      setSecondsLeft(CODE_TTL_SECONDS)
      setValue('codigo', '')
      setInfoMessage(resp.mensagem)
      showToast({
        variant: 'info',
        title: 'Código reenviado',
        description: 'Um novo código de 6 dígitos válido por 90 segundos foi enviado.',
      })
    } catch (error) {
      const message = applyApiErrorsToForm(error, setError, setFocus)
      setFormError(message)
    } finally {
      setResending(false)
    }
  }

  const codigoField = register('codigo')

  return (
    <div className="mx-auto max-w-md px-4 py-10 sm:py-14">
      <Card padding="lg">
        <div className="mb-6">
          <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-button bg-receptor-soft text-receptor-aa">
            <MailCheck className="h-5 w-5" aria-hidden="true" />
          </div>
          <h1 className="text-2xl font-bold text-ink">Confirmar e-mail</h1>
          <p className="mt-1 text-sm text-ink-secondary">
            Digite o código numérico de 6 dígitos enviado para o seu e-mail. A conta só é ativada
            após a confirmação.
          </p>
        </div>

        <div
          role="status"
          aria-live="polite"
          className={`mb-5 flex items-center justify-between rounded-card border px-4 py-3 text-sm ${
            isExpired
              ? 'border-warning-border bg-warning-soft text-warning'
              : 'border-border bg-surface-subtle text-ink'
          }`}
        >
          <span className="inline-flex items-center gap-2 font-medium">
            <Timer className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span>{isExpired ? 'Código expirado' : 'Validade do código'}</span>
          </span>
          <span className="font-bold tabular-nums">
            {isExpired ? '0s' : `${secondsLeft}s`}
          </span>
        </div>

        {isExpired ? (
          <div className="mb-5">
            <InfoBanner tone="warning" title="Tempo esgotado">
              O código de 90 segundos expirou. Solicite um novo código antes de tentar confirmar.
            </InfoBanner>
          </div>
        ) : null}

        {infoMessage ? (
          <div className="mb-5">
            <InfoBanner tone="info">{infoMessage}</InfoBanner>
          </div>
        ) : null}

        {formError ? (
          <div className="mb-5">
            <InfoBanner tone="danger" title="Erro na verificação">
              {formError}
            </InfoBanner>
          </div>
        ) : null}

        <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
          <Input
            label="E-mail cadastrado"
            type="email"
            autoComplete="email"
            placeholder="nome@organizacao.org.br"
            required
            error={errors.email?.message}
            {...register('email')}
          />

          <Input
            label="Código de confirmação (6 dígitos)"
            inputMode="numeric"
            maxLength={6}
            placeholder="000000"
            autoComplete="one-time-code"
            className="text-center text-lg font-bold tracking-[0.35em] tabular-nums"
            required
            error={errors.codigo?.message}
            {...codigoField}
            onChange={(event) => {
              const onlyDigits = event.target.value.replace(/\D/g, '').slice(0, 6)
              setValue('codigo', onlyDigits, {
                shouldValidate: Boolean(errors.codigo),
              })
            }}
          />

          <Button
            type="submit"
            size="lg"
            loading={isSubmitting}
            loadingLabel="Verificando..."
            className="w-full"
          >
            Confirmar e ativar conta
          </Button>
        </form>

        <div className="mt-5 flex flex-col gap-3 border-t border-border pt-5">
          <Button
            type="button"
            variant="secondary"
            loading={resending}
            loadingLabel="Reenviando..."
            leftIcon={<RefreshCw className="h-4 w-4" />}
            onClick={handleResend}
            className="w-full"
          >
            Reenviar código
          </Button>

          <p className="text-center text-xs text-ink-secondary">
            Já confirmou anteriormente?{' '}
            <Link to="/login" className="font-semibold text-ink hover:underline">
              Voltar ao login
            </Link>
          </p>
        </div>
      </Card>
    </div>
  )
}

