import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../../contexts/AuthContext.tsx'
import { ToastProvider } from '../../contexts/ToastContext.tsx'
import { AppRoutes } from '../../routes/router.tsx'
import * as ofertasService from '../../services/ofertas.service.ts'
import * as reservasService from '../../services/reservas.service.ts'
import * as usuariosService from '../../services/usuarios.service.ts'
import type { UsuarioResponse } from '../../types/auth.ts'
import type { OfertaDoacaoResponse } from '../../types/oferta.ts'
import type { ReservaEntregaResponse } from '../../types/reserva.ts'
import { OfertaDetalhePage } from '../app/ofertas/OfertaDetalhePage.tsx'
import { MinhasReservasReceptorPage } from '../app/reservas/MinhasReservasReceptorPage.tsx'
import { EntregasDisponiveisPage } from '../app/transporte/EntregasDisponiveisPage.tsx'
import { HistoricoTransportesPage } from '../app/transporte/HistoricoTransportesPage.tsx'
import { MinhasEntregasPage } from '../app/transporte/MinhasEntregasPage.tsx'

vi.mock('../../services/usuarios.service.ts', () => ({
  obterMeuPerfil: vi.fn(),
  atualizarMeuPerfil: vi.fn(),
  obterMeuContato: vi.fn(),
  atualizarMeuContato: vi.fn(),
}))

vi.mock('../../services/ofertas.service.ts', async () => {
  const actual = await vi.importActual<typeof import('../../services/ofertas.service.ts')>(
    '../../services/ofertas.service.ts',
  )
  return {
    ...actual,
    buscarOfertaPorId: vi.fn(),
  }
})

vi.mock('../../services/reservas.service.ts', async () => {
  const actual = await vi.importActual<typeof import('../../services/reservas.service.ts')>(
    '../../services/reservas.service.ts',
  )
  return {
    ...actual,
    listarReservasAguardandoTransporte: vi.fn(),
    listarMinhasEntregasTransportador: vi.fn(),
    listarHistoricoTransportador: vi.fn(),
    listarMinhasReservasReceptor: vi.fn(),
    aceitarTransporte: vi.fn(),
    autorizarInicioTransportePorOferta: vi.fn(),
    confirmarRetiradaTransporte: vi.fn(),
    confirmarEntregaTransporte: vi.fn(),
  }
})

const transportadorUser: UsuarioResponse = {
  id: 3,
  nome: 'Carlos Motorista',
  email: 'carlos@transp.org',
  tipoUsuario: 'TRANSPORTADOR',
}

const doadorUser: UsuarioResponse = {
  id: 1,
  nome: 'João Doador',
  email: 'joao@doador.org',
  tipoUsuario: 'DOADOR',
}

const receptorUser: UsuarioResponse = {
  id: 2,
  nome: 'ONG Esperança',
  email: 'ong@esperanca.org',
  tipoUsuario: 'RECEPTOR',
}

function perfilTransportador(situacaoConta: 'ATIVO' | 'SUSPENSO') {
  return {
    id: 3,
    nome: 'Carlos Motorista',
    documento: '22222222222',
    email: 'carlos@transp.org',
    emailContato: 'carlos@transp.org',
    telefone: '11999990003',
    tipoUsuario: 'TRANSPORTADOR' as const,
    tipoPessoa: 'FISICA' as const,
    emailVerificado: true,
    situacaoConta,
    motivoSuspensao: situacaoConta === 'SUSPENSO' ? 'Prazo de 24h expirado' : null,
    endereco: null,
  }
}

function reservaExemplo(
  id: number,
  situacao: ReservaEntregaResponse['situacao'],
  extra: Partial<ReservaEntregaResponse> = {},
): ReservaEntregaResponse {
  return {
    id,
    situacao,
    precisaTransporte: true,
    ofertaId: 10,
    situacaoOferta: situacao === 'CONCLUIDA' ? 'CONCLUIDA' : 'RESERVADA',
    transportadorId: situacao === 'AGUARDANDO_TRANSPORTE' ? null : 3,
    doador:
      situacao === 'AGUARDANDO_TRANSPORTE'
        ? null
        : { nome: 'João Doador', email: 'joao@doador.org', telefone: '11999990001' },
    receptor:
      situacao === 'AGUARDANDO_TRANSPORTE'
        ? null
        : { nome: 'ONG Esperança', email: 'ong@esperanca.org', telefone: '11999990002' },
    transportador:
      situacao === 'AGUARDANDO_TRANSPORTE'
        ? null
        : { nome: 'Carlos Motorista', email: 'carlos@transp.org', telefone: '11999990003' },
    enderecoRetirada: {
      logradouro: situacao === 'AGUARDANDO_TRANSPORTE' ? null : 'Rua das Flores',
      numero: situacao === 'AGUARDANDO_TRANSPORTE' ? null : '50',
      complemento: null,
      bairro: null,
      cidade: 'Campinas',
      estado: 'SP',
      cep: null,
    },
    enderecoEntrega: {
      logradouro: situacao === 'AGUARDANDO_TRANSPORTE' ? null : 'Av. Paulista',
      numero: situacao === 'AGUARDANDO_TRANSPORTE' ? null : '1000',
      complemento: null,
      bairro: null,
      cidade: 'São Paulo',
      estado: 'SP',
      cep: null,
    },
    itens: [
      {
        id: 1,
        descricao: 'Arroz',
        tipoItemDoacao: 'NAO_PERECIVEL',
        tipoUnidade: 'KG',
        quantidade: 20,
      },
    ],
    criadoEm: '2026-11-23T10:00:00',
    atualizadoEm: '2026-11-23T10:05:00',
    ...extra,
  }
}

describe('Fluxo reestruturado de transporte e layout de cadastro', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('Tela de cadastro não exibe footer e rota de disponibilidades foi removida', async () => {
    const { unmount } = render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={null}>
          <MemoryRouter initialEntries={['/cadastro']}>
            <AppRoutes />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(await screen.findByRole('heading', { name: /criar conta no doalink/i })).toBeInTheDocument()
    expect(screen.queryByRole('contentinfo')).not.toBeInTheDocument()
    unmount()

    vi.mocked(reservasService.listarMinhasEntregasTransportador).mockResolvedValue([])
    vi.mocked(usuariosService.obterMeuPerfil).mockResolvedValue(perfilTransportador('ATIVO'))

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={transportadorUser}>
          <MemoryRouter initialEntries={['/app/disponibilidades']}>
            <AppRoutes />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(await screen.findByRole('heading', { name: /página não encontrada/i })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /disponibilidades/i })).not.toBeInTheDocument()
  })

  it('Transportes disponíveis lista operações e permite aceitar transporte quando ativo, bloqueando quando suspenso', async () => {
    const user = userEvent.setup()
    vi.mocked(reservasService.listarReservasAguardandoTransporte).mockResolvedValue([
      reservaExemplo(50, 'AGUARDANDO_TRANSPORTE'),
    ])
    vi.mocked(usuariosService.obterMeuPerfil).mockResolvedValueOnce(perfilTransportador('ATIVO'))
    vi.mocked(reservasService.aceitarTransporte).mockResolvedValueOnce(
      reservaExemplo(50, 'AGUARDANDO_AUTORIZACAO_DOADOR'),
    )

    const { unmount } = render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={transportadorUser}>
          <MemoryRouter initialEntries={['/app/transportes-disponiveis']}>
            <Routes>
              <Route path="/app/transportes-disponiveis" element={<EntregasDisponiveisPage />} />
              <Route path="/app/meus-transportes" element={<h1>Meus Transportes Rota</h1>} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    const aceitarBtn = await screen.findByRole('button', { name: /aceitar transporte/i })
    expect(aceitarBtn).toBeEnabled()
    await user.click(aceitarBtn)
    await user.click(screen.getByRole('button', { name: /confirmar aceite/i }))

    await waitFor(() => {
      expect(reservasService.aceitarTransporte).toHaveBeenCalledWith(50)
    })
    expect(await screen.findByRole('heading', { name: /meus transportes rota/i })).toBeInTheDocument()
    unmount()

    vi.mocked(usuariosService.obterMeuPerfil).mockResolvedValueOnce(perfilTransportador('SUSPENSO'))
    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={transportadorUser}>
          <MemoryRouter initialEntries={['/app/transportes-disponiveis']}>
            <EntregasDisponiveisPage />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(await screen.findByText(/conta suspensa para investigação/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /aceitar transporte/i })).toBeDisabled()
  })

  it('Meus transportes exibe ações de acordo com o status e permite confirmar retirada e entrega por código', async () => {
    const user = userEvent.setup()
    vi.mocked(usuariosService.obterMeuPerfil).mockResolvedValue(perfilTransportador('ATIVO'))
    vi.mocked(reservasService.listarMinhasEntregasTransportador).mockResolvedValue([
      reservaExemplo(50, 'AGUARDANDO_AUTORIZACAO_DOADOR'),
      reservaExemplo(51, 'AGUARDANDO_CONFIRMACAO_RETIRADA'),
      reservaExemplo(52, 'EM_TRANSPORTE', { codigoEntregaExpiraEm: '2026-11-24T12:00:00' }),
      reservaExemplo(53, 'CONCLUIDA'),
    ])
    vi.mocked(reservasService.confirmarRetiradaTransporte).mockResolvedValueOnce(
      reservaExemplo(51, 'EM_TRANSPORTE'),
    )
    vi.mocked(reservasService.confirmarEntregaTransporte).mockResolvedValueOnce(
      reservaExemplo(52, 'CONCLUIDA'),
    )

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={transportadorUser}>
          <MemoryRouter initialEntries={['/app/meus-transportes']}>
            <MinhasEntregasPage />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(
      await screen.findByText(/aguardando autorização do doador para gerar o código de retirada/i),
    ).toBeInTheDocument()
    expect(screen.getByText(/operação concluída \(somente leitura\)/i)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /informar código de retirada/i }))
    await user.type(screen.getByLabelText(/código de retirada/i), '123456')
    await user.click(screen.getByRole('button', { name: /^confirmar retirada$/i }))

    await waitFor(() => {
      expect(reservasService.confirmarRetiradaTransporte).toHaveBeenCalledWith(51, {
        codigo: '123456',
      })
    })

    await user.click(screen.getByRole('button', { name: /informar código de entrega/i }))
    await user.type(screen.getByLabelText(/código de entrega/i), '654321')
    await user.click(screen.getByRole('button', { name: /^confirmar entrega$/i }))

    await waitFor(() => {
      expect(reservasService.confirmarEntregaTransporte).toHaveBeenCalledWith(52, {
        codigo: '654321',
      })
    })
  })

  it('Histórico apresenta operações concluídas em modo somente leitura', async () => {
    vi.mocked(reservasService.listarHistoricoTransportador).mockResolvedValue([
      reservaExemplo(60, 'CONCLUIDA', {
        aceitoEm: '2026-11-23T10:10:00',
        retiradaConfirmadaEm: '2026-11-23T11:00:00',
        entregaConfirmadaEm: '2026-11-23T13:00:00',
      }),
    ])

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={transportadorUser}>
          <MemoryRouter initialEntries={['/app/historico-transportes']}>
            <HistoricoTransportesPage />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(await screen.findByText('Reserva #60')).toBeInTheDocument()
    expect(screen.getByText('Concluída')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /informar código/i })).not.toBeInTheDocument()
  })

  it('Doador autoriza início do transporte em OfertaDetalhePage e consulta o código de retirada', async () => {
    const user = userEvent.setup()
    const ofertaBase: OfertaDoacaoResponse = {
      id: 10,
      situacao: 'RESERVADA',
      doador: { nome: 'João Doador', email: 'joao@doador.org', telefone: null },
      receptor: { nome: 'ONG Esperança', email: 'ong@esperanca.org', telefone: null },
      transportador: { nome: 'Carlos Motorista', email: 'carlos@transp.org', telefone: null },
      reservaId: 50,
      situacaoReserva: 'AGUARDANDO_AUTORIZACAO_DOADOR',
      codigoRetirada: null,
      localRetirada: null,
      itens: [],
      criadoEm: '2026-11-23T09:00:00',
      atualizadoEm: null,
    }

    vi.mocked(ofertasService.buscarOfertaPorId)
      .mockResolvedValueOnce(ofertaBase)
      .mockResolvedValueOnce({
        ...ofertaBase,
        situacaoReserva: 'AGUARDANDO_CONFIRMACAO_RETIRADA',
        codigoRetirada: '987654',
      })
    vi.mocked(reservasService.autorizarInicioTransportePorOferta).mockResolvedValueOnce(
      reservaExemplo(50, 'AGUARDANDO_CONFIRMACAO_RETIRADA', { codigoRetirada: '987654' }),
    )

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={doadorUser}>
          <MemoryRouter initialEntries={['/app/ofertas/10']}>
            <Routes>
              <Route path="/app/ofertas/:id" element={<OfertaDetalhePage />} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    const authBtn = await screen.findByRole('button', { name: /autorizar início do transporte/i })
    await user.click(authBtn)
    await user.click(screen.getByRole('button', { name: /autorizar início$/i }))

    await waitFor(() => {
      expect(reservasService.autorizarInicioTransportePorOferta).toHaveBeenCalledWith(10)
    })
    expect(await screen.findByText('987654')).toBeInTheDocument()
  })

  it('Receptor visualiza código de entrega, validade de 24h e orientação de segurança em Minhas reservas', async () => {
    vi.mocked(reservasService.listarMinhasReservasReceptor).mockResolvedValueOnce([
      reservaExemplo(70, 'EM_TRANSPORTE', {
        codigoEntrega: '456789',
        codigoEntregaExpiraEm: '2026-11-24T15:00:00',
      }),
    ])

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={receptorUser}>
          <MemoryRouter initialEntries={['/app/minhas-reservas']}>
            <MinhasReservasReceptorPage />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(await screen.findByText('456789')).toBeInTheDocument()
    expect(screen.getByText(/validade de 24h/i)).toBeInTheDocument()
    expect(
      screen.getByText(/não compartilhe este código antes de receber os itens fisicamente/i),
    ).toBeInTheDocument()
  })
})
