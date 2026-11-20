import { zodResolver } from '@hookform/resolvers/zod'
import { KeyRound } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { InfoBanner } from '../../components/feedback/InfoBanner.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { Input } from '../../components/ui/Input.tsx'
import { useDocumentTitle } from '../../hooks/useDocumentTitle.ts'
import {
  solicitarRecuperacaoSchema,
  type SolicitarRecuperacaoFormValues,
} from '../../schemas/auth.schema.ts'
import { solicitarRecuperacaoSenha } from '../../services/auth.service.ts'
import { applyApiErrorsToForm } from '../../utils/formErrors.ts'

export function RecuperarSenhaPage() {
  useDocumentTitle('Recuperar senha')
  const [submittedMessage, setSubmittedMessage] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<SolicitarRecuperacaoFormValues>({
    resolver: zodResolver(solicitarRecuperacaoSchema),
    defaultValues: { email: '' },
  })

  const onSubmit = async (values: SolicitarRecuperacaoFormValues) => {
    setFormError(null)
    setSubmittedMessage(null)

    try {
      const resp = await solicitarRecuperacaoSenha({
        email: values.email.trim().toLowerCase(),
      })
      setSubmittedMessage(resp.mensagem)
    } catch (error) {
      const message = applyApiErrorsToForm(error, setError, setFocus)
      setFormError(message)
    }
  }

  return (
    <div className="mx-auto max-w-md px-4 py-10 sm:py-14">
      <Card padding="lg">
        <div className="mb-6">
          <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-[10px] bg-surface-subtle text-ink">
            <KeyRound className="h-5 w-5" aria-hidden="true" />
          </div>
          <h1 className="text-2xl font-bold text-ink">Recuperar senha</h1>
          <p className="mt-1 text-sm text-ink-secondary">
            Informe seu e-mail cadastrado. Enviaremos um link de uso único com validade de 10
            minutos.
          </p>
        </div>

        {submittedMessage ? (
          <div className="mb-5">
            <InfoBanner tone="success" title="Solicitação registrada">
              {submittedMessage}
            </InfoBanner>
          </div>
        ) : null}

        {formError ? (
          <div className="mb-5">
            <InfoBanner tone="danger">{formError}</InfoBanner>
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

          <Button
            type="submit"
            size="lg"
            loading={isSubmitting}
            loadingLabel="Enviando..."
            className="w-full"
          >
            Enviar link de recuperação
          </Button>
        </form>

        <p className="mt-6 border-t border-border pt-5 text-center text-sm text-ink-secondary">
          Lembrou da senha?{' '}
          <Link to="/login" className="font-semibold text-ink hover:underline">
            Voltar ao login
          </Link>
        </p>
      </Card>
    </div>
  )
}

