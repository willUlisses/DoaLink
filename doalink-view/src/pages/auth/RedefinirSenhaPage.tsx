import { zodResolver } from '@hookform/resolvers/zod'
import { KeyRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { InfoBanner } from '../../components/feedback/InfoBanner.tsx'
import { Skeleton } from '../../components/feedback/Skeleton.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { PasswordInput } from '../../components/ui/PasswordInput.tsx'
import { useDocumentTitle } from '../../hooks/useDocumentTitle.ts'
import { useToast } from '../../hooks/useToast.ts'
import {
  redefinirSenhaSchema,
  type RedefinirSenhaFormValues,
} from '../../schemas/auth.schema.ts'
import { redefinirSenha, validarTokenRecuperacao } from '../../services/auth.service.ts'
import { applyApiErrorsToForm } from '../../utils/formErrors.ts'

type TokenStatus = 'checking' | 'valid' | 'invalid'

export function RedefinirSenhaPage() {
  useDocumentTitle('Redefinir senha')
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { showToast } = useToast()

  const token = searchParams.get('token')?.trim() ?? ''
  const [tokenStatus, setTokenStatus] = useState<TokenStatus>(token ? 'checking' : 'invalid')
  const [formError, setFormError] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return

    let active = true

    validarTokenRecuperacao(token)
      .then((res) => {
        if (!active) return
        setTokenStatus(res.valido ? 'valid' : 'invalid')
      })
      .catch(() => {
        if (!active) return
        setTokenStatus('invalid')
      })

    return () => {
      active = false
    }
  }, [token])

  const {
    register,
    handleSubmit,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<RedefinirSenhaFormValues>({
    resolver: zodResolver(redefinirSenhaSchema),
    defaultValues: {
      novaSenha: '',
      confirmarSenha: '',
    },
  })

  const onSubmit = async (values: RedefinirSenhaFormValues) => {
    setFormError(null)
    try {
      await redefinirSenha({
        token,
        novaSenha: values.novaSenha,
      })
      showToast({
        variant: 'success',
        title: 'Senha redefinida',
        description: 'Use sua nova senha para entrar no DoaLink.',
      })
      navigate('/login', { replace: true })
    } catch (error) {
      const message = applyApiErrorsToForm(error, setError, setFocus)
      setFormError(message)
      if (message.toLowerCase().includes('inválido') || message.toLowerCase().includes('expirado')) {
        setTokenStatus('invalid')
      }
    }
  }

  return (
    <div className="mx-auto max-w-md px-4 py-10 sm:py-14">
      <Card padding="lg">
        <div className="mb-6">
          <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-button bg-surface-subtle text-ink">
            <KeyRound className="h-5 w-5" aria-hidden="true" />
          </div>
          <h1 className="text-2xl font-bold text-ink">Redefinir senha</h1>
          <p className="mt-1 text-sm text-ink-secondary">
            Crie uma nova senha para sua conta. O link tem validade de 10 minutos e uso único.
          </p>
        </div>

        {tokenStatus === 'checking' ? (
          <div role="status" aria-label="Validando link de recuperação" className="space-y-3">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-11 w-full" />
          </div>
        ) : null}

        {tokenStatus === 'invalid' ? (
          <div className="space-y-5">
            <InfoBanner tone="danger" title="Link inválido ou expirado">
              {formError ??
                'Este link de recuperação não é válido, já foi utilizado ou ultrapassou o prazo de 10 minutos.'}
            </InfoBanner>
            <div className="flex flex-col gap-2">
              <Button asChild>
                <Link to="/recuperar-senha">Solicitar novo link</Link>
              </Button>
              <Button asChild variant="secondary">
                <Link to="/login">Voltar ao login</Link>
              </Button>
            </div>
          </div>
        ) : null}

        {tokenStatus === 'valid' ? (
          <>
            {formError ? (
              <div className="mb-5">
                <InfoBanner tone="danger">{formError}</InfoBanner>
              </div>
            ) : null}

            <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
              <PasswordInput
                label="Nova senha"
                autoComplete="new-password"
                placeholder="Entre 8 e 72 caracteres"
                hint="Mínimo 8, máximo 72 caracteres."
                required
                error={errors.novaSenha?.message}
                {...register('novaSenha')}
              />

              <PasswordInput
                label="Confirmar nova senha"
                autoComplete="new-password"
                placeholder="Repita a nova senha"
                required
                error={errors.confirmarSenha?.message}
                {...register('confirmarSenha')}
              />

              <Button
                type="submit"
                size="lg"
                loading={isSubmitting}
                loadingLabel="Salvando nova senha..."
                className="w-full"
              >
                Salvar nova senha
              </Button>
            </form>
          </>
        ) : null}
      </Card>
    </div>
  )
}
