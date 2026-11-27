import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, UserPlus } from 'lucide-react'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { InfoBanner } from '../../components/feedback/InfoBanner.tsx'
import { AddressFieldset } from '../../components/ui/AddressFieldset.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { Input } from '../../components/ui/Input.tsx'
import { PasswordInput } from '../../components/ui/PasswordInput.tsx'
import { ProfileBadge } from '../../components/ui/ProfileBadge.tsx'
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

function resolvePerfilInicial(param: string | null): TipoUsuario | null {
  if (param === TIPO_USUARIO.DOADOR) return TIPO_USUARIO.DOADOR
  if (param === TIPO_USUARIO.RECEPTOR) return TIPO_USUARIO.RECEPTOR
  if (param === TIPO_USUARIO.TRANSPORTADOR) return TIPO_USUARIO.TRANSPORTADOR
  return null
}

export function CadastroPage() {
  useDocumentTitle('Criar conta')
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { setPendingVerificationEmail } = useAuth()
  const { showToast } = useToast()
  const [formError, setFormError] = useState<string | null>(null)

  const perfilDaUrl = resolvePerfilInicial(searchParams.get('perfil'))
  const perfilPadrao = perfilDaUrl ?? TIPO_USUARIO.DOADOR
  const [step, setStep] = useState<1 | 2>(perfilDaUrl ? 2 : 1)

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
      tipoUsuario: perfilPadrao,
      tipoPessoa: perfilPadrao === 'RECEPTOR' ? 'JURIDICA' : 'FISICA',
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

  const handlePerfilSelect = (novoPerfil: TipoUsuario) => {
    setValue('tipoUsuario', novoPerfil, { shouldValidate: false })
    if (novoPerfil === 'RECEPTOR') {
      setValue('tipoPessoa', 'JURIDICA')
      setValue('cpf', '')
    }
    clearErrors()
    setFormError(null)
    setStep(2)
  }

  const handleVoltarEtapa = () => {
    clearErrors()
    setFormError(null)
    setStep(1)
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
    <div
      className={`mx-auto flex min-h-[calc(100svh-4.25rem)] w-full flex-col justify-center px-4 py-3 sm:px-6 sm:py-4 ${
        step === 2 ? (isReceptor ? 'max-w-5xl' : 'max-w-2xl') : 'max-w-xl'
      }`}
    >
      <Card padding="md">
        <div className="mb-3.5 border-b border-border pb-3">
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2.5">
              <div className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-button bg-surface-subtle text-ink">
                <UserPlus className="h-4.5 w-4.5" aria-hidden="true" />
              </div>
              <h1 className="text-lg font-bold tracking-tight text-ink sm:text-xl">
                Criar conta no DoaLink
              </h1>
            </div>
            <span className="text-xs font-medium text-ink-tertiary">Etapa {step} de 2</span>
          </div>
          <p className="mt-1 text-xs text-ink-secondary sm:text-sm">
            {step === 1
              ? 'Escolha como você deseja participar do DoaLink.'
              : 'Você receberá um código de 6 dígitos por e-mail para ativar sua conta.'}
          </p>
        </div>

        {step === 1 ? (
          <div className="space-y-4">
            <ProfileSelector
              value={tipoUsuario}
              onChange={handlePerfilSelect}
              error={errors.tipoUsuario?.message}
            />

            <p className="border-t border-border pt-3 text-center text-sm text-ink-secondary">
              Já possui cadastro?{' '}
              <Link to="/login" className="font-semibold text-ink hover:underline">
                Entrar
              </Link>
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-3">
            <div className="flex items-center justify-between gap-2">
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={handleVoltarEtapa}
                leftIcon={<ArrowLeft className="h-4 w-4" />}
                className="-ml-2 px-2.5"
              >
                Voltar
              </Button>
              <ProfileBadge tipoUsuario={tipoUsuario} />
            </div>

            {formError ? (
              <InfoBanner tone="danger" title="Não foi possível concluir o cadastro">
                {formError}
              </InfoBanner>
            ) : null}

            {!isReceptor ? (
              <div className="space-y-3">
                <fieldset className="flex flex-col gap-1">
                  <legend className="text-sm font-medium text-ink">
                    Tipo de documento
                  </legend>
                  <div className="grid grid-cols-2 rounded-button border border-border bg-surface-subtle p-1">
                    <label
                      className={`flex items-center justify-center rounded-md px-3 py-1.5 text-xs sm:text-sm cursor-pointer transition-colors ${
                        tipoPessoa === 'FISICA'
                          ? 'bg-surface font-semibold text-ink shadow-xs'
                          : 'font-medium text-ink-secondary hover:text-ink'
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
                      className={`flex items-center justify-center rounded-md px-3 py-1.5 text-xs sm:text-sm cursor-pointer transition-colors ${
                        tipoPessoa === 'JURIDICA'
                          ? 'bg-surface font-semibold text-ink shadow-xs'
                          : 'font-medium text-ink-secondary hover:text-ink'
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

                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <Input
                    label="Nome completo ou organização"
                    placeholder="Ex.: Maria Silva"
                    maxLength={150}
                    required
                    compact
                    error={errors.nome?.message}
                    {...register('nome')}
                  />

                  {showCnpj ? (
                    <Input
                      label="CNPJ"
                      placeholder="00.000.000/0000-00"
                      inputMode="numeric"
                      maxLength={18}
                      required
                      compact
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
                      compact
                      error={errors.cpf?.message}
                      {...cpfField}
                      onChange={(event) => {
                        setValue('cpf', mascararCpf(event.target.value), {
                          shouldValidate: Boolean(errors.cpf),
                        })
                      }}
                    />
                  )}

                  <Input
                    label="E-mail"
                    type="email"
                    autoComplete="email"
                    placeholder="contato@exemplo.com.br"
                    maxLength={254}
                    required
                    compact
                    error={errors.email?.message}
                    {...register('email')}
                  />

                  <PasswordInput
                    label="Senha"
                    autoComplete="new-password"
                    placeholder="Mínimo de 8 caracteres"
                    hint="Entre 8 e 60 caracteres."
                    required
                    compact
                    error={errors.senha?.message}
                    {...register('senha')}
                  />
                </div>
              </div>
            ) : (
              <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
                <fieldset className="flex h-full flex-col rounded-card border border-border bg-surface-subtle/50 p-3.5">
                  <legend className="px-1.5 text-sm font-semibold text-ink">
                    Dados da organização
                  </legend>
                  <p className="mb-2.5 text-xs text-ink-secondary">
                    Informações de identificação e acesso da ONG.
                  </p>
                  <div className="grid flex-1 grid-cols-1 content-between gap-2.5 sm:grid-cols-2">
                    <div className="sm:col-span-2">
                      <Input
                        label="Nome da organização (ONG)"
                        placeholder="Ex.: Instituto Esperança"
                        maxLength={150}
                        required
                        compact
                        error={errors.nome?.message}
                        {...register('nome')}
                      />
                    </div>

                    <div className="sm:col-span-2">
                      <Input
                        label="CNPJ"
                        placeholder="00.000.000/0000-00"
                        inputMode="numeric"
                        maxLength={18}
                        required
                        compact
                        error={errors.cnpj?.message}
                        {...cnpjField}
                        onChange={(event) => {
                          setValue('cnpj', mascararCnpj(event.target.value), {
                            shouldValidate: Boolean(errors.cnpj),
                          })
                        }}
                      />
                    </div>

                    <Input
                      label="E-mail"
                      type="email"
                      autoComplete="email"
                      placeholder="contato@exemplo.com.br"
                      maxLength={254}
                      required
                      compact
                      error={errors.email?.message}
                      {...register('email')}
                    />

                    <PasswordInput
                      label="Senha"
                      autoComplete="new-password"
                      placeholder="Mínimo de 8 caracteres"
                      hint="Entre 8 e 72 caracteres."
                      required
                      compact
                      error={errors.senha?.message}
                      {...register('senha')}
                    />
                  </div>
                </fieldset>

                <AddressFieldset
                  prefix="endereco"
                  legend="Endereço da organização"
                  description="Obrigatório para recebimento e entrega de doações."
                  register={register}
                  setValue={setValue}
                  errors={errors.endereco}
                  required
                  compact
                  className="h-full"
                />
              </div>
            )}

            <div className="flex flex-col-reverse items-center justify-between gap-2.5 border-t border-border pt-3 sm:flex-row">
              <p className="text-center text-sm text-ink-secondary sm:text-left">
                Já possui cadastro?{' '}
                <Link to="/login" className="font-semibold text-ink hover:underline">
                  Entrar
                </Link>
              </p>

              <Button
                type="submit"
                size="md"
                variant={variantForProfile(tipoUsuario)}
                loading={isSubmitting}
                loadingLabel="Criando conta..."
                className="w-full sm:w-auto sm:min-w-64"
              >
                Criar conta e enviar código
              </Button>
            </div>
          </form>
        )}
      </Card>
    </div>
  )
}
