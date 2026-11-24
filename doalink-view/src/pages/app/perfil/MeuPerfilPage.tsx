import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, Lock, Mail, MapPin, Phone, Save, ShieldAlert, UserCheck } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation } from 'react-router-dom'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { AddressFieldset } from '../../../components/ui/AddressFieldset.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { Input } from '../../../components/ui/Input.tsx'
import { ProfileBadge } from '../../../components/ui/ProfileBadge.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useAuth } from '../../../hooks/useAuth.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { useToast } from '../../../hooks/useToast.ts'
import {
  atualizarUsuarioSchema,
  contatoSchema,
  type AtualizarUsuarioFormValues,
  type ContatoFormValues,
} from '../../../schemas/usuario.schema.ts'
import {
  atualizarMeuContato,
  atualizarMeuPerfil,
  obterMeuContato,
  obterMeuPerfil,
} from '../../../services/usuarios.service.ts'
import type { ContatoResponse } from '../../../types/endereco.ts'
import type { TipoUsuario } from '../../../types/enums.ts'
import type { UsuarioDetalheResponse } from '../../../types/usuario.ts'
import { applyApiErrorsToForm } from '../../../utils/formErrors.ts'
import { formatarEnderecoCompleto } from '../../../utils/formatters.ts'
import { LABELS_TIPO_USUARIO } from '../../../utils/labels.ts'
import {
  formatarCep,
  formatarTelefone,
  mascararCnpj,
  mascararCpf,
  mascararTelefone,
} from '../../../utils/masks.ts'
import { variantForProfile } from '../../../utils/theme.ts'
import { limparDigitos } from '../../../utils/validators.ts'

interface PerfilNavLink {
  to: string
  label: string
}

const ROTAS_PERMITIDAS_POR_PERFIL: Record<TipoUsuario, PerfilNavLink[]> = {
  DOADOR: [
    { to: '/app/minhas-ofertas', label: 'Minhas ofertas' },
    { to: '/app/ofertas/nova', label: 'Nova oferta' },
    { to: '/app/reservas-ofertas', label: 'Reservas recebidas' },
    { to: '/app/necessidades', label: 'Necessidades abertas' },
    { to: '/app/campanhas', label: 'Campanhas emergenciais' },
    { to: '/app', label: 'Painel inicial' },
  ],
  RECEPTOR: [
    { to: '/app/minhas-reservas', label: 'Minhas reservas' },
    { to: '/app/ofertas', label: 'Ofertas disponíveis' },
    { to: '/app/minhas-necessidades', label: 'Minhas necessidades' },
    { to: '/app/minhas-campanhas', label: 'Minhas campanhas' },
    { to: '/app/campanhas', label: 'Campanhas emergenciais' },
    { to: '/app', label: 'Painel inicial' },
  ],
  TRANSPORTADOR: [
    { to: '/app/meus-transportes', label: 'Meus transportes' },
    { to: '/app/transportes-disponiveis', label: 'Transportes disponíveis' },
    { to: '/app/historico-transportes', label: 'Histórico de transportes' },
    { to: '/app/campanhas', label: 'Campanhas emergenciais' },
    { to: '/app', label: 'Painel inicial' },
  ],
}

function resolverDestinoRetorno(tipoUsuario: TipoUsuario | undefined, fromPath: unknown): PerfilNavLink {
  if (!tipoUsuario) {
    return { to: '/app', label: 'Painel inicial' }
  }
  const opcoes = ROTAS_PERMITIDAS_POR_PERFIL[tipoUsuario]
  if (typeof fromPath === 'string' && fromPath.startsWith('/app') && fromPath !== '/app/perfil') {
    const correspondencia = opcoes.find((item) => item.to === fromPath)
    if (correspondencia) {
      return correspondencia
    }
    const permitidoPorPrefixo =
      fromPath.startsWith('/app/campanhas') ||
      (tipoUsuario === 'DOADOR' && fromPath.startsWith('/app/necessidades')) ||
      (tipoUsuario === 'RECEPTOR' && fromPath.startsWith('/app/ofertas'))
    if (permitidoPorPrefixo) {
      return { to: fromPath, label: 'página anterior' }
    }
  }
  return opcoes[0]
}

export function MeuPerfilPage() {
  useDocumentTitle('Meu perfil')
  const { user } = useAuth()
  const { showToast } = useToast()
  const location = useLocation()
  const fromState = (location.state as { from?: string } | null)?.from

  const [contactError, setContactError] = useState<string | null>(null)
  const [addressError, setAddressError] = useState<string | null>(null)
  const [savedPerfil, setSavedPerfil] = useState<UsuarioDetalheResponse | null>(null)
  const [savedContato, setSavedContato] = useState<ContatoResponse | null>(null)

  const fetcher = useCallback(async () => {
    const [perfilResp, contatoResp] = await Promise.all([obterMeuPerfil(), obterMeuContato()])
    return { perfil: perfilResp, contato: contatoResp }
  }, [])

  const { data, loading, error, reload: loadPerfil } = useAsyncResource(fetcher, [])
  const perfil = savedPerfil ?? data?.perfil ?? null
  const contato = savedContato ?? data?.contato ?? null

  const {
    register: registerContact,
    handleSubmit: handleSubmitContact,
    setValue: setContactValue,
    setError: setContactFieldError,
    setFocus: setContactFocus,
    reset: resetContact,
    formState: { errors: contactErrors, isSubmitting: savingContact },
  } = useForm<ContatoFormValues>({
    resolver: zodResolver(contatoSchema),
    defaultValues: {
      email: user?.email ?? '',
      telefone: '',
    },
  })

  const {
    register: registerAddress,
    handleSubmit: handleSubmitAddress,
    setValue: setAddressValue,
    setError: setAddressFieldError,
    setFocus: setAddressFocus,
    reset: resetAddress,
    formState: { errors: addressErrors, isSubmitting: savingAddress },
  } = useForm<AtualizarUsuarioFormValues>({
    resolver: zodResolver(atualizarUsuarioSchema),
    defaultValues: {
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

  useEffect(() => {
    if (!data) return
    const { perfil: perfilInicial, contato: contatoInicial } = data
    const emailContato =
      contatoInicial.email || perfilInicial.emailContato || perfilInicial.email || user?.email || ''
    const telefoneFormatado = contatoInicial.telefone
      ? mascararTelefone(contatoInicial.telefone)
      : ''

    resetContact({
      email: emailContato,
      telefone: telefoneFormatado,
    })

    resetAddress({
      endereco: {
        cep: perfilInicial.endereco?.cep ? formatarCep(perfilInicial.endereco.cep) : '',
        estado: perfilInicial.endereco?.estado ?? '',
        cidade: perfilInicial.endereco?.cidade ?? '',
        bairro: perfilInicial.endereco?.bairro ?? '',
        logradouro: perfilInicial.endereco?.logradouro ?? '',
        numero: perfilInicial.endereco?.numero ?? '',
        complemento: perfilInicial.endereco?.complemento ?? '',
      },
    })
  }, [data, resetContact, resetAddress, user?.email])

  const tipoUsuarioAtual = perfil?.tipoUsuario ?? user?.tipoUsuario
  const isReceptor = tipoUsuarioAtual === 'RECEPTOR'
  const retorno = resolverDestinoRetorno(tipoUsuarioAtual, fromState)
  const atalhosPerfil = tipoUsuarioAtual ? ROTAS_PERMITIDAS_POR_PERFIL[tipoUsuarioAtual] : []

  const onSaveContact = async (values: ContatoFormValues) => {
    setContactError(null)
    try {
      const atualizado = await atualizarMeuContato({
        email: values.email.trim().toLowerCase(),
        telefone: values.telefone?.trim() ? limparDigitos(values.telefone) : '',
      })

      setSavedContato(atualizado)
      resetContact({
        email: atualizado.email,
        telefone: atualizado.telefone ? mascararTelefone(atualizado.telefone) : '',
      })

      showToast({
        variant: 'success',
        title: 'Opções de contato atualizadas',
        description:
          'Seu e-mail e telefone de contato foram salvos sem alterar seu e-mail de login.',
      })
    } catch (err) {
      const msg = applyApiErrorsToForm(err, setContactFieldError, setContactFocus)
      setContactError(msg)
    }
  }

  const onSaveAddress = async (values: AtualizarUsuarioFormValues) => {
    setAddressError(null)
    try {
      const atualizado = await atualizarMeuPerfil({
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

      setSavedPerfil(atualizado)
      resetAddress({
        endereco: {
          cep: atualizado.endereco?.cep ? formatarCep(atualizado.endereco.cep) : '',
          estado: atualizado.endereco?.estado ?? '',
          cidade: atualizado.endereco?.cidade ?? '',
          bairro: atualizado.endereco?.bairro ?? '',
          logradouro: atualizado.endereco?.logradouro ?? '',
          numero: atualizado.endereco?.numero ?? '',
          complemento: atualizado.endereco?.complemento ?? '',
        },
      })

      showToast({
        variant: 'success',
        title: 'Endereço atualizado',
        description: 'O endereço da sua conta foi salvo com sucesso.',
      })
    } catch (err) {
      const msg = applyApiErrorsToForm(err, setAddressFieldError, setAddressFocus)
      setAddressError(msg)
    }
  }

  const telefoneFieldContact = registerContact('telefone')

  const documentoFormatado = perfil?.documento
    ? perfil.tipoPessoa === 'FISICA'
      ? mascararCpf(perfil.documento)
      : mascararCnpj(perfil.documento)
    : 'Não informado'

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Meu perfil' }]} />
        <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
          <Link
            to={retorno.to}
            className="inline-flex items-center gap-1.5 text-sm font-medium text-ink-secondary hover:text-ink"
          >
            <ArrowLeft className="h-4 w-4" aria-hidden="true" />
            <span>Voltar para {retorno.label}</span>
          </Link>
        </div>

        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold text-ink">Meu perfil</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Consulte os dados da conta e mantenha seu endereço e opções de contato atualizados.
            </p>
          </div>
          {user ? <ProfileBadge tipoUsuario={user.tipoUsuario} /> : null}
        </div>

        {atalhosPerfil.length > 0 ? (
          <nav aria-label="Atalhos das seções permitidas" className="mt-4 flex flex-wrap gap-2">
            {atalhosPerfil.map((item) => (
              <Button key={item.to} asChild variant="secondary" size="sm">
                <Link to={item.to}>{item.label}</Link>
              </Button>
            ))}
          </nav>
        ) : null}
      </div>

      {!perfil && loading ? <CardListSkeleton count={2} /> : null}

      {!perfil && !loading && error ? <ErrorState message={error.message} onRetry={loadPerfil} /> : null}

      {perfil && contato ? (
        <div className="space-y-6">
          {perfil.situacaoConta === 'SUSPENSO' ? (
            <InfoBanner tone="danger" title="Conta de transportador suspensa para investigação">
              <span className="inline-flex items-center gap-1.5">
                <ShieldAlert className="h-4 w-4 shrink-0" aria-hidden="true" />
                <span>
                  {perfil.motivoSuspensao ||
                    'Sua conta está suspensa devido à expiração de um prazo de entrega de 24 horas.'}
                </span>
              </span>
            </InfoBanner>
          ) : null}

          {/* Dados de identificação da conta — somente leitura */}
          <Card className="space-y-4">
            <div className="flex items-start gap-3">
              <span className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-button bg-surface-subtle text-ink">
                <Lock className="h-5 w-5" aria-hidden="true" />
              </span>
              <div>
                <h2 className="text-lg font-bold text-ink">Dados de identificação da conta</h2>
                <p className="text-xs text-ink-secondary">
                  Estes dados identificam sua conta na plataforma e são exibidos apenas para leitura.
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Input
                label={isReceptor ? 'Nome da organização (ONG)' : 'Nome'}
                value={perfil.nome}
                readOnly
                disabled
              />
              <Input
                label={perfil.tipoPessoa === 'FISICA' ? 'CPF' : 'CNPJ'}
                value={documentoFormatado}
                readOnly
                disabled
              />
              <Input
                label="Tipo de usuário"
                value={LABELS_TIPO_USUARIO[perfil.tipoUsuario]}
                readOnly
                disabled
              />
              <Input
                label="Tipo de pessoa"
                value={perfil.tipoPessoa === 'FISICA' ? 'Pessoa Física' : 'Pessoa Jurídica'}
                readOnly
                disabled
              />
              <div className="sm:col-span-2">
                <Input
                  label="E-mail de login (acesso à conta)"
                  type="email"
                  value={perfil.email}
                  hint="Utilizado exclusivamente para entrar na plataforma. Alterar o e-mail de contato não altera este e-mail de acesso."
                  readOnly
                  disabled
                />
              </div>
            </div>
          </Card>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
            {/* Seção Minhas opções de contato */}
            <Card className="space-y-4 lg:col-span-5 border-t-4 border-t-receptor">
              <div className="flex items-start gap-3">
                <span className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-button bg-receptor-soft text-receptor-aa">
                  <Phone className="h-5 w-5" aria-hidden="true" />
                </span>
                <div>
                  <h2 className="text-lg font-bold text-ink">Minhas opções de contato</h2>
                  <p className="text-xs leading-relaxed text-ink-secondary">
                    {isReceptor
                      ? 'Informações exibidas para doadores e transportadores nas suas reservas e necessidades.'
                      : 'Informações exibidas na comunicação com ONGs e transportadores nas suas doações.'}
                  </p>
                </div>
              </div>

              <div className="rounded-card border border-border bg-canvas p-3.5 space-y-2 text-xs">
                <p className="font-semibold uppercase tracking-wide text-ink-muted">
                  Contato atual exibido
                </p>
                <p className="flex items-center gap-2 text-ink">
                  <UserCheck className="h-3.5 w-3.5 shrink-0 text-receptor-aa" aria-hidden="true" />
                  <span className="font-medium">{contato.nome}</span>
                </p>
                <p className="flex items-center gap-2 text-ink-secondary">
                  <Mail className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
                  <span>{contato.email}</span>
                </p>
                {contato.telefone ? (
                  <p className="flex items-center gap-2 text-ink-secondary">
                    <Phone className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
                    <span>{formatarTelefone(contato.telefone)}</span>
                  </p>
                ) : (
                  <p className="text-ink-muted">Nenhum telefone de contato cadastrado.</p>
                )}
              </div>

              {contactError ? <InfoBanner tone="danger">{contactError}</InfoBanner> : null}

              <form onSubmit={handleSubmitContact(onSaveContact)} noValidate className="space-y-4">
                <Input
                  label="E-mail de contato"
                  type="email"
                  required
                  hint="Utilizado para comunicação no DoaLink. Alterá-lo não modifica seu e-mail de login."
                  error={contactErrors.email?.message}
                  {...registerContact('email')}
                />

                <Input
                  label="Telefone para contato"
                  placeholder="(11) 99999-9999"
                  inputMode="tel"
                  maxLength={15}
                  hint="Opcional. Usado para alinhar retiradas e entregas."
                  error={contactErrors.telefone?.message}
                  {...telefoneFieldContact}
                  onChange={(event) => {
                    setContactValue('telefone', mascararTelefone(event.target.value), {
                      shouldValidate: Boolean(contactErrors.telefone),
                    })
                  }}
                />

                <Button
                  type="submit"
                  variant={variantForProfile(perfil.tipoUsuario)}
                  loading={savingContact}
                  loadingLabel="Salvando..."
                  leftIcon={<Save className="h-4 w-4" />}
                  className="w-full"
                >
                  Salvar opções de contato
                </Button>
              </form>
            </Card>

            {/* Seção Endereço editável */}
            <Card className="space-y-4 lg:col-span-7">
              <div>
                <h2 className="text-lg font-bold text-ink">
                  {isReceptor ? 'Endereço da organização' : 'Endereço'}
                </h2>
                <p className="text-xs text-ink-secondary">
                  {isReceptor
                    ? 'O endereço da organização é usado como sugestão inicial ao criar necessidades e campanhas.'
                    : 'Mantenha seu endereço atualizado para facilitar retiradas e entregas.'}
                </p>
              </div>

              {perfil.endereco ? (
                <div className="flex items-start gap-2 rounded-card border border-border bg-canvas p-3 text-xs text-ink-secondary">
                  <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-ink" aria-hidden="true" />
                  <span>
                    <strong className="text-ink">Endereço salvo:</strong>{' '}
                    {formatarEnderecoCompleto(perfil.endereco)}
                  </span>
                </div>
              ) : null}

              {addressError ? <InfoBanner tone="danger">{addressError}</InfoBanner> : null}

              <form onSubmit={handleSubmitAddress(onSaveAddress)} noValidate className="space-y-4">
                <AddressFieldset
                  compact
                  prefix="endereco"
                  legend={isReceptor ? 'Endereço da organização' : 'Endereço'}
                  register={registerAddress}
                  setValue={setAddressValue}
                  errors={addressErrors.endereco}
                  required
                />

                <div className="flex justify-end">
                  <Button
                    type="submit"
                    variant={variantForProfile(perfil.tipoUsuario)}
                    loading={savingAddress}
                    loadingLabel="Salvando endereço..."
                    leftIcon={<Save className="h-4 w-4" />}
                  >
                    Salvar endereço
                  </Button>
                </div>
              </form>
            </Card>
          </div>
        </div>
      ) : null}
    </div>
  )
}
