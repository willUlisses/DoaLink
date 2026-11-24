import { zodResolver } from '@hookform/resolvers/zod'
import { UserPlus } from 'lucide-react'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { InfoBanner } from '../../components/feedback/InfoBanner.tsx'
import { AddressFieldset } from '../../components/ui/AddressFieldset.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { Input } from '../../components/ui/Input.tsx'
import { PasswordInput } from '../../components/ui/PasswordInput.tsx'
import { ProfileSelector } from '../../components/ui/ProfileSelector.tsx'
import { useAuth } from '../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../hooks/useDocumentTitle.ts'
import { useToast } from '../../hooks/useToast.ts'
import { cadastroSchema, type CadastroFormValues } from '../../schemas/auth.schema.ts'
import {
  cadastrarDoador,
  cadastrarReceptor,
  cadastrarTransportador,
} from '../../services/auth.service.ts'
import { TIPO_USUARIO, type TipoPessoa, type TipoUsuario } from '../../types/enums.ts'
import { applyApiErrorsToForm } from '../../utils/formErrors.ts'
import { mascararCnpj, mascararCpf } from '../../utils/masks.ts'
import { variantForProfile } from '../../utils/theme.ts'
import { limparDigitos } from '../../utils/validators.ts'

function resolvePerfilInicial(param: string | null): TipoUsuario {
  if (param === TIPO_USUARIO.RECEPTOR) return TIPO_USUARIO.RECEPTOR
  if (param === TIPO_USUARIO.TRANSPORTADOR) return TIPO_USUARIO.TRANSPORTADOR
  return TIPO_USUARIO.DOADOR
}

export function CadastroPage() {
  useDocumentTitle('Criar conta')
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { setPendingVerificationEmail } = useAuth()
  const { showToast } = useToast()
  const [formError, setFormError] = useState<string | null>(null)

  const perfilInicial = resolvePerfilInicial(searchParams.get('perfil'))

  const {
    register,
    control,
    handleSubmit,
    setValue,
    setError,
    setFocus,
    clearErrors,
    formState: { errors, isSubmitting },
  } = useForm<CadastroFormValues>({
    resolver: zodResolver(cadastroSchema),
    defaultValues: {
      tipoUsuario: perfilInicial,
      tipoPessoa: perfilInicial === 'RECEPTOR' ? 'JURIDICA' : 'FISICA',
      nome: '',
      cpf: '',
      cnpj: '',
      email: '',
      senha: '',
      endereco: {
        cep: '',
        estado: '',
        cidade: '',
        bairro: '',
        logradouro: '',
        numero: '',
        complemento: '',
      },
    },
  })

  const tipoUsuario = useWatch({ control, name: 'tipoUsuario' })
  const tipoPessoa = useWatch({ control, name: 'tipoPessoa' })
  const isReceptor = tipoUsuario === 'RECEPTOR'
  const showCnpj = isReceptor || tipoPessoa === 'JURIDICA'

  const handlePerfilChange = (novoPerfil: TipoUsuario) => {
    setValue('tipoUsuario', novoPerfil, { shouldValidate: false })
    if (novoPerfil === 'RECEPTOR') {
      setValue('tipoPessoa', 'JURIDICA')
      setValue('cpf', '')
    }
    clearErrors()
    setFormError(null)
  }

  const handleTipoPessoaChange = (novaPessoa: TipoPessoa) => {
    setValue('tipoPessoa', novaPessoa, { shouldValidate: false })
    if (novaPessoa === 'FISICA') {
      setValue('cnpj', '')
    } else {
      setValue('cpf', '')
    }
    clearErrors(['cpf', 'cnpj'])
  }

  const cpfField = register('cpf')
  const cnpjField = register('cnpj')

  const onSubmit = async (values: CadastroFormValues) => {
    setFormError(null)
    const emailNormalizado = values.email.trim().toLowerCase()

    try {
      if (values.tipoUsuario === 'RECEPTOR') {
        await cadastrarReceptor({
          nome: values.nome.trim(),
          email: emailNormalizado,
          senha: values.senha,
          cnpj: limparDigitos(values.cnpj ?? ''),
          endereco: {
            cep: limparDigitos(values.endereco.cep),
            estado: values.endereco.estado.trim().toUpperCase(),
            cidade: values.endereco.cidade.trim(),
            bairro: values.endereco.bairro.trim(),
            logradouro: values.endereco.logradouro.trim(),
            numero: values.endereco.numero?.trim() || undefined,
            complemento: values.endereco.complemento?.trim() || undefined,
          },
        })
      } else {
        const comumPayload = {
          nome: values.nome.trim(),
          email: emailNormalizado,
          senha: values.senha,
          ...(values.tipoPessoa === 'FISICA'
            ? { cpf: limparDigitos(values.cpf ?? '') }
            : { cnpj: limparDigitos(values.cnpj ?? '') }),
        }

        if (values.tipoUsuario === 'DOADOR') {
          await cadastrarDoador(comumPayload)
        } else {
          await cadastrarTransportador(comumPayload)
        }
      }

      setPendingVerificationEmail(emailNormalizado)
      showToast({
        variant: 'success',
        title: 'Cadastro criado',
        description: 'Enviamos um código de 6 dígitos para confirmar seu e-mail.',
      })
      navigate('/verificar-email', {
        replace: true,
        state: { email: emailNormalizado },
      })
    } catch (error) {
      const message = applyApiErrorsToForm(error, setError, setFocus, {
        documento: showCnpj ? 'cnpj' : 'cpf',
      })
      setFormError(message)
    }
  }

  return (
    <div className="mx-auto flex min-h-[calc(100svh-4.25rem)] w-full max-w-5xl flex-col justify-center px-4 py-6 sm:px-6 lg:py-8">
      <Card className="p-6 sm:p-8">
        <div className="mb-6 flex flex-col gap-3 border-b border-border pb-5 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-start gap-3.5">
            <div className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-button bg-surface-subtle text-ink">
              <UserPlus className="h-5 w-5" aria-hidden="true" />
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">
                Criar conta no DoaLink
              </h1>
              <p className="mt-1 text-sm text-ink-secondary">
                Após o cadastro, você receberá um código de 6 dígitos (validade de 90 segundos) para
                ativar sua conta.
              </p>
            </div>
          </div>

          <p className="shrink-0 text-sm text-ink-secondary">
            Já possui cadastro?{' '}
            <Link to="/login" className="font-semibold text-ink hover:underline">
              Entrar
            </Link>
          </p>
        </div>

        {formError ? (
          <div className="mb-5">
            <InfoBanner tone="danger" title="Não foi possível concluir o cadastro">
              {formError}
            </InfoBanner>
          </div>
        ) : null}

        <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-6">
          <ProfileSelector
            value={tipoUsuario}
            onChange={handlePerfilChange}
            error={errors.tipoUsuario?.message}
          />

          {!isReceptor ? (
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-12 lg:items-start">
              <fieldset className="flex flex-col gap-1.5 sm:col-span-2 lg:col-span-5">
                <legend className="text-sm font-medium text-ink">Tipo de pessoa</legend>
                <div className="grid grid-cols-2 gap-3">
                  <label
                    className={`flex items-center justify-center rounded-card border px-3 py-2.5 text-sm font-medium cursor-pointer transition-colors ${
                      tipoPessoa === 'FISICA'
                        ? 'border-ink bg-surface-subtle text-ink font-semibold'
                        : 'border-border bg-surface text-ink-secondary hover:border-border-strong'
                    }`}
                  >
                    <input
                      type="radio"
                      name="tipoPessoaRadio"
                      checked={tipoPessoa === 'FISICA'}
                      onChange={() => handleTipoPessoaChange('FISICA')}
                      className="sr-only"
                    />
                    <span>Pessoa Física (CPF)</span>
                  </label>
                  <label
                    className={`flex items-center justify-center rounded-card border px-3 py-2.5 text-sm font-medium cursor-pointer transition-colors ${
                      tipoPessoa === 'JURIDICA'
                        ? 'border-ink bg-surface-subtle text-ink font-semibold'
                        : 'border-border bg-surface text-ink-secondary hover:border-border-strong'
                    }`}
                  >
                    <input
                      type="radio"
                      name="tipoPessoaRadio"
                      checked={tipoPessoa === 'JURIDICA'}
                      onChange={() => handleTipoPessoaChange('JURIDICA')}
                      className="sr-only"
                    />
                    <span>Pessoa Jurídica (CNPJ)</span>
                  </label>
                </div>
              </fieldset>

              <div className="lg:col-span-4">
                <Input
                  label="Nome completo ou organização"
                  placeholder="Ex.: Maria Silva"
                  maxLength={150}
                  required
                  error={errors.nome?.message}
                  {...register('nome')}
                />
              </div>

              <div className="lg:col-span-3">
                {showCnpj ? (
                  <Input
                    label="CNPJ"
                    placeholder="00.000.000/0000-00"
                    inputMode="numeric"
                    maxLength={18}
                    required
                    error={errors.cnpj?.message}
                    {...cnpjField}
                    onChange={(event) => {
                      setValue('cnpj', mascararCnpj(event.target.value), {
                        shouldValidate: Boolean(errors.cnpj),
                      })
                    }}
                  />
                ) : (
                  <Input
                    label="CPF"
                    placeholder="000.000.000-00"
                    inputMode="numeric"
                    maxLength={14}
                    required
                    error={errors.cpf?.message}
                    {...cpfField}
                    onChange={(event) => {
                      setValue('cpf', mascararCpf(event.target.value), {
                        shouldValidate: Boolean(errors.cpf),
                      })
                    }}
                  />
                )}
              </div>

              <div className="lg:col-span-6">
                <Input
                  label="E-mail"
                  type="email"
                  autoComplete="email"
                  placeholder="contato@exemplo.com.br"
                  maxLength={254}
                  required
                  error={errors.email?.message}
                  {...register('email')}
                />
              </div>

              <div className="lg:col-span-6">
                <PasswordInput
                  label="Senha"
                  autoComplete="new-password"
                  placeholder="Mínimo de 8 caracteres"
                  hint="Informe entre 8 e 60 caracteres."
                  required
                  error={errors.senha?.message}
                  {...register('senha')}
                />
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-12 lg:items-start">
              <div className="space-y-4 lg:col-span-5">
                <InfoBanner tone="info" className="text-xs">
                  Organizações receptoras utilizam obrigatoriamente CNPJ e endereço completo da sede
                  ou ponto de recebimento.
                </InfoBanner>

                <Input
                  label="Nome da organização (ONG)"
                  placeholder="Ex.: Instituto Esperança"
                  maxLength={150}
                  required
                  error={errors.nome?.message}
                  {...register('nome')}
                />

                <Input
                  label="CNPJ"
                  placeholder="00.000.000/0000-00"
                  inputMode="numeric"
                  maxLength={18}
                  required
                  error={errors.cnpj?.message}
                  {...cnpjField}
                  onChange={(event) => {
                    setValue('cnpj', mascararCnpj(event.target.value), {
                      shouldValidate: Boolean(errors.cnpj),
                    })
                  }}
                />

                <Input
                  label="E-mail"
                  type="email"
                  autoComplete="email"
                  placeholder="contato@exemplo.com.br"
                  maxLength={254}
                  required
                  error={errors.email?.message}
                  {...register('email')}
                />

                <PasswordInput
                  label="Senha"
                  autoComplete="new-password"
                  placeholder="Mínimo de 8 caracteres"
                  hint="Informe entre 8 e 72 caracteres."
                  required
                  error={errors.senha?.message}
                  {...register('senha')}
                />
              </div>

              <div className="lg:col-span-7">
                <AddressFieldset
                  prefix="endereco"
                  legend="Endereço da organização"
                  description="Obrigatório para recebimento e entrega de doações."
                  register={register}
                  setValue={setValue}
                  errors={errors.endereco}
                  required
                />
              </div>
            </div>
          )}

          <div className="flex justify-end border-t border-border pt-5">
            <Button
              type="submit"
              size="lg"
              variant={variantForProfile(tipoUsuario)}
              loading={isSubmitting}
              loadingLabel="Criando conta..."
              className="w-full sm:w-auto sm:min-w-72"
            >
              Criar conta e enviar código
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
