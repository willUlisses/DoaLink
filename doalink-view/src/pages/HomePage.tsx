import {
  ArrowRight,
  Building2,
  CheckCircle2,
  HeartHandshake,
  ShieldCheck,
  Truck,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { Button } from '../components/ui/Button.tsx'
import { Card } from '../components/ui/Card.tsx'
import { useAuth } from '../hooks/useAuth.ts'
import { useDocumentTitle } from '../hooks/useDocumentTitle.ts'

const PERFIS = [
  {
    key: 'doador',
    titulo: 'Doador',
    corBarra: 'border-t-4 border-t-doador',
    corIcone: 'bg-doador-soft text-doador-aa',
    variant: 'doador' as const,
    Icon: HeartHandshake,
    descricao:
      'Publique ofertas com múltiplos itens, acompanhe reservas em andamento e contribua com campanhas emergenciais.',
    itens: [
      'Cadastro como Pessoa Física ou Jurídica',
      'Local de retirada opcional na oferta',
      'Acompanhamento de reservas e transportadores',
    ],
    to: '/cadastro?perfil=DOADOR',
    cta: 'Cadastrar como Doador',
  },
  {
    key: 'receptor',
    titulo: 'Receptor (ONG)',
    corBarra: 'border-t-4 border-t-receptor',
    corIcone: 'bg-receptor-soft text-receptor-aa',
    variant: 'receptor' as const,
    Icon: Building2,
    descricao:
      'Reserve ofertas disponíveis, publique necessidades da sua instituição e organize campanhas emergenciais.',
    itens: [
      'Cadastro institucional com CNPJ e endereço',
      'Reserva direta com ou sem necessidade de transporte',
      'Criação de campanhas emergenciais ativas',
    ],
    to: '/cadastro?perfil=RECEPTOR',
    cta: 'Cadastrar como Receptor',
  },
  {
    key: 'transportador',
    titulo: 'Transportador',
    corBarra: 'border-t-4 border-t-transportador',
    corIcone: 'bg-transportador-soft text-transportador-aa',
    variant: 'transportador' as const,
    Icon: Truck,
    descricao:
      'Informe sua disponibilidade de veículo e capacidade para assumir entregas que aguardam transporte solidário.',
    itens: [
      'Cadastro como Pessoa Física ou Jurídica',
      'Controle automático de capacidade restante',
      'Endereços exatos liberados após o vínculo',
    ],
    to: '/cadastro?perfil=TRANSPORTADOR',
    cta: 'Cadastrar como Transportador',
  },
] as const

export function HomePage() {
  useDocumentTitle('Conectando quem doa com quem precisa')
  const { user, status } = useAuth()
  const isAuthenticated = status === 'authenticated' && user !== null

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6 sm:py-16">
      {/* Hero */}
      <section className="mx-auto max-w-3xl text-center">
        <span className="inline-flex items-center gap-2 rounded-full border border-border bg-surface px-3.5 py-1 text-xs font-semibold text-ink-secondary shadow-2xs">
          <ShieldCheck className="h-3.5 w-3.5 text-receptor-aa" aria-hidden="true" />
          <span>Plataforma de intermediação e logística de doações</span>
        </span>

        <h1 className="mt-5 text-3xl font-bold tracking-tight text-ink sm:text-5xl sm:leading-[1.12]">
          Conectando quem doa, quem precisa e quem transporta.
        </h1>

        <p className="mt-4 text-base leading-relaxed text-ink-secondary sm:text-lg">
          O DoaLink organiza ofertas de doação, reservas de ONGs receptoras e disponibilidade de
          transporte em um fluxo transparente, seguro e com privacidade de endereços.
        </p>

        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          {isAuthenticated ? (
            <Button
              asChild
              size="lg"
              rightIcon={<ArrowRight className="h-4 w-4" />}
              className="px-6 text-sm"
            >
              <Link to="/app">Acessar meu painel</Link>
            </Button>
          ) : (
            <>
              <Button
                asChild
                size="lg"
                rightIcon={<ArrowRight className="h-4 w-4" />}
                className="px-6 text-sm"
              >
                <Link to="/cadastro">Criar conta gratuita</Link>
              </Button>
              <Button asChild variant="secondary" size="lg" className="px-6 text-sm">
                <Link to="/login">Já tenho conta</Link>
              </Button>
            </>
          )}
        </div>
      </section>

      {/* Perfis */}
      <section aria-labelledby="perfis-heading" className="mt-16">
        <div className="mb-8 text-center">
          <h2 id="perfis-heading" className="text-2xl font-bold text-ink">
            Escolha seu papel na rede solidária
          </h2>
          <p className="mt-2 text-sm text-ink-secondary">
            Cada conta atua em um perfil exclusivo para manter clareza operacional e segurança.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
          {PERFIS.map((perfil) => {
            const Icon = perfil.Icon
            return (
              <Card
                key={perfil.key}
                className={`flex flex-col justify-between ${perfil.corBarra}`}
              >
                <div>
                  <div className="flex items-center gap-3">
                    <span
                      className={`inline-flex h-10 w-10 items-center justify-center rounded-button ${perfil.corIcone}`}
                      aria-hidden="true"
                    >
                      <Icon className="h-5 w-5" />
                    </span>
                    <h3 className="text-lg font-bold text-ink">{perfil.titulo}</h3>
                  </div>

                  <p className="mt-3 text-sm leading-relaxed text-ink-secondary">
                    {perfil.descricao}
                  </p>

                  <ul className="mt-4 space-y-2">
                    {perfil.itens.map((item) => (
                      <li key={item} className="flex items-start gap-2 text-xs text-ink-secondary">
                        <CheckCircle2
                          className="mt-0.5 h-3.5 w-3.5 shrink-0 text-ink-muted"
                          aria-hidden="true"
                        />
                        <span>{item}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="mt-6 pt-4 border-t border-border">
                  <Button
                    asChild
                    variant={perfil.variant}
                    rightIcon={<ArrowRight className="h-4 w-4" />}
                    className="w-full"
                  >
                    <Link to={isAuthenticated ? '/app' : perfil.to}>
                      {isAuthenticated ? 'Acessar painel' : perfil.cta}
                    </Link>
                  </Button>
                </div>
              </Card>
            )
          })}
        </div>
      </section>

      {/* Fluxo transparente */}
      <section aria-labelledby="fluxo-heading" className="mt-16">
        <Card className="bg-surface">
          <h2 id="fluxo-heading" className="text-lg font-bold text-ink">
            Como funciona na prática
          </h2>
          <div className="mt-5 grid grid-cols-1 gap-6 sm:grid-cols-3">
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-doador-aa">
                1. Oferta publicada
              </p>
              <p className="mt-1 text-sm text-ink-secondary">
                O doador cadastra os itens disponíveis. Nas listagens públicas, apenas cidade e UF
                aparecem para preservar a privacidade.
              </p>
            </div>
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-receptor-aa">
                2. Reserva pela ONG
              </p>
              <p className="mt-1 text-sm text-ink-secondary">
                O receptor escolhe uma oferta disponível e informa se retirará diretamente ou se
                aguardará um transportador.
              </p>
            </div>
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-transportador-aa">
                3. Entrega vinculada
              </p>
              <p className="mt-1 text-sm text-ink-secondary">
                Um transportador com capacidade compatível assume a entrega, libera os endereços
                completos entre as partes e conclui após a entrega.
              </p>
            </div>
          </div>
        </Card>
      </section>
    </div>
  )
}

