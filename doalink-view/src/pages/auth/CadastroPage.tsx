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
    <div className="mx-auto max-w-2xl px-4 py-8 sm:py-12">
      <Card padding="lg">
        <div className="mb-6">
          <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-[10px] bg-surface-subtle text-ink">
            <UserPlus className="h-5 w-5" aria-hidden="true" />
          </div>
          <h1 className="text-2xl font-bold text-ink">Criar conta no DoaLink</h1>
          <p className="mt-1 text-sm text-ink-secondary">
            Após o cadastro, você receberá um código de 6 dígitos (validade de 90 segundos) para
            ativar sua conta.
          </p>
        </div>

        {formError ? (
          <div className="mb-6">
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
            <fieldset className="flex flex-col gap-2">
              <legend className="text-sm font-medium text-ink">Tipo de pessoa</legend>
              <div className="grid grid-cols-2 gap-3">
                <label
                  className={`flex items-center justify-center rounded-[10px] border px-3 py-2.5 text-sm font-medium cursor-pointer transition-colors ${
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
                  className={`flex items-center justify-center rounded-[10px] border px-3 py-2.5 text-sm font-medium cursor-pointer transition-colors ${
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
          ) : (
            <InfoBanner tone="info">
              Organizações receptoras utilizam obrigatoriamente CNPJ e endereço completo da sede ou
              ponto de recebimento.
            </InfoBanner>
          )}

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <Input
                label={isReceptor ? 'Nome da organização (ONG)' : 'Nome completo ou organização'}
                placeholder={isReceptor ? 'Ex.: Instituto Esperança' : 'Ex.: Maria Silva'}
                maxLength={150}
                required
                error={errors.nome?.message}
                {...register('nome')}
              />
            </div>

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

            <div className="sm:col-span-2">
              <PasswordInput
                label="Senha"
                autoComplete="new-password"
                placeholder="Mínimo de 8 caracteres"
                hint={`Informe entre 8 e ${isReceptor ? '72' : '60'} caracteres.`}
                required
                error={errors.senha?.message}
                {...register('senha')}
              />
            </div>
          </div>

          {isReceptor ? (
            <AddressFieldset
              prefix="endereco"
              legend="Endereço da organização"
              description="Obrigatório para recebimento e entrega de doações."
              register={register}
              setValue={setValue}
              errors={errors.endereco}
              required
            />
          ) : null}

          <Button
            type="submit"
            size="lg"
            variant={variantForProfile(tipoUsuario)}
            loading={isSubmitting}
            loadingLabel="Criando conta..."
            className="w-full"
          >
            Criar conta e enviar código
          </Button>
        </form>

        <p className="mt-6 border-t border-border pt-5 text-center text-sm text-ink-secondary">
          Já possui cadastro?{' '}
          <Link to="/login" className="font-semibold text-ink hover:underline">
            Entrar
          </Link>
        </p>
      </Card>
    </div>
  )
}
