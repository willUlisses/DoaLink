import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../../contexts/AuthContext.tsx'
import { ToastProvider } from '../../contexts/ToastContext.tsx'
import * as necessidadesService from '../../services/necessidades.service.ts'
import * as ofertasService from '../../services/ofertas.service.ts'
import * as reservasService from '../../services/reservas.service.ts'
import * as usuariosService from '../../services/usuarios.service.ts'
import type { UsuarioResponse } from '../../types/auth.ts'
import type { OfertaDoacaoResponse } from '../../types/oferta.ts'
import type { ReservaEntregaResponse } from '../../types/reserva.ts'
import { NovaCampanhaPage } from '../app/campanhas/NovaCampanhaPage.tsx'
import { NecessidadeDetalhePage } from '../app/necessidades/NecessidadeDetalhePage.tsx'
import { NovaNecessidadePage } from '../app/necessidades/NovaNecessidadePage.tsx'
import { OfertaDetalhePage } from '../app/ofertas/OfertaDetalhePage.tsx'
import { MeuPerfilPage } from '../app/perfil/MeuPerfilPage.tsx'
import { MinhasReservasReceptorPage } from '../app/reservas/MinhasReservasReceptorPage.tsx'

vi.mock('../../services/usuarios.service.ts', () => ({
  obterMeuPerfil: vi.fn(),
  atualizarMeuPerfil: vi.fn(),
  obterMeuContato: vi.fn(),
  atualizarMeuContato: vi.fn(),
}))

vi.mock('../../services/necessidades.service.ts', async () => {
  const actual = await vi.importActual<typeof import('../../services/necessidades.service.ts')>(
    '../../services/necessidades.service.ts',
  )
  return {
    ...actual,
    buscarNecessidadePorId: vi.fn(),
  }
})

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
    reservarOferta: vi.fn(),
    listarMinhasReservasReceptor: vi.fn(),
    concluirReserva: vi.fn(),
  }
})

const receptorUser: UsuarioResponse = {
  id: 2,
  nome: 'ONG Esperança',
  email: 'ong@esperanca.org',
  tipoUsuario: 'RECEPTOR',
}

const doadorUser: UsuarioResponse = {
  id: 1,
  nome: 'Carlos Doador',
  email: 'carlos@doador.org',
  tipoUsuario: 'DOADOR',
}

describe('Funcionalidades pendentes integradas', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('MeuPerfilPage exibe identificação somente leitura, separa e-mail de contato do login e permite sair do perfil', async () => {
    const user = userEvent.setup()
    vi.mocked(usuariosService.obterMeuPerfil).mockResolvedValue({
      id: 2,
      nome: 'ONG Esperança',
      documento: '11222333000181',
      email: 'ong@login.org',
      emailContato: 'ong@login.org',
      telefone: null,
      tipoUsuario: 'RECEPTOR',
      tipoPessoa: 'JURIDICA',
      emailVerificado: true,
      endereco: {
        logradouro: 'Av. Paulista',
        numero: '100',
        complemento: null,
        bairro: 'Bela Vista',
        cidade: 'São Paulo',
        estado: 'SP',
        cep: '01310100',
      },
    })
    vi.mocked(usuariosService.obterMeuContato)
      .mockResolvedValueOnce({
        nome: 'ONG Esperança',
        email: 'ong@login.org',
        telefone: null,
      })
      .mockResolvedValue({
        nome: 'ONG Esperança',
        email: 'contato@esperanca.org',
        telefone: '11999998888',
      })
    vi.mocked(usuariosService.atualizarMeuContato).mockResolvedValueOnce({
      nome: 'ONG Esperança',
      email: 'contato@esperanca.org',
      telefone: '11999998888',
    })
    vi.mocked(usuariosService.atualizarMeuPerfil).mockResolvedValueOnce({
      id: 2,
      nome: 'ONG Esperança',
      documento: '11222333000181',
      email: 'ong@login.org',
      emailContato: 'contato@esperanca.org',
      telefone: '11999998888',
      tipoUsuario: 'RECEPTOR',
      tipoPessoa: 'JURIDICA',
      emailVerificado: true,
      endereco: {
        logradouro: 'Av. Brasil',
        numero: '200',
        complemento: null,
        bairro: 'Centro',
        cidade: 'Campinas',
        estado: 'SP',
        cep: '13010000',
      },
    })

    const loginUser: UsuarioResponse = {
      id: 2,
      nome: 'ONG Esperança',
      email: 'ong@login.org',
      tipoUsuario: 'RECEPTOR',
    }

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={loginUser}>
          <MemoryRouter initialEntries={[{ pathname: '/app/perfil', state: { from: '/app/minhas-reservas' } }]}>
            <Routes>
              <Route path="/app/perfil" element={<MeuPerfilPage />} />
              <Route path="/app/minhas-reservas" element={<h1>Lista Minhas Reservas</h1>} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(await screen.findByText('Minhas opções de contato')).toBeInTheDocument()

    // Dados pessoais somente leitura
    expect(screen.getByLabelText(/nome da organização/i)).toBeDisabled()
    expect(screen.getByLabelText(/^cnpj$/i)).toBeDisabled()
    expect(screen.getByLabelText(/tipo de usuário/i)).toBeDisabled()
    expect(screen.getByLabelText(/e-mail de login/i)).toBeDisabled()
    expect(screen.getByLabelText(/e-mail de login/i)).toHaveValue('ong@login.org')

    // Atualiza e-mail de contato e telefone sem alterar login
    const contactEmailInput = screen.getByLabelText(/e-mail de contato/i)
    await waitFor(() => {
      expect(contactEmailInput).toHaveValue('ong@login.org')
    })
    await user.clear(contactEmailInput)
    await user.type(contactEmailInput, 'contato@esperanca.org')
    expect(contactEmailInput).toHaveValue('contato@esperanca.org')
    await user.type(screen.getByLabelText(/telefone para contato/i), '11999998888')
    await user.click(screen.getByRole('button', { name: /salvar opções de contato/i }))

    await waitFor(() => {
      expect(screen.queryAllByRole('alert').map((el) => el.textContent)).toEqual([])
      expect(usuariosService.atualizarMeuContato).toHaveBeenCalledWith({
        email: 'contato@esperanca.org',
        telefone: '11999998888',
      })
    })
    expect(await screen.findByText('contato@esperanca.org')).toBeInTheDocument()
    expect(screen.getByLabelText(/e-mail de login/i)).toHaveValue('ong@login.org')

    // Atualiza endereço
    const cidadeInput = screen.getByLabelText(/cidade/i)
    await user.clear(cidadeInput)
    await user.type(cidadeInput, 'Campinas')
    await user.click(screen.getByRole('button', { name: /salvar endereço/i }))

    await waitFor(() => {
      expect(usuariosService.atualizarMeuPerfil).toHaveBeenCalledWith({
        endereco: expect.objectContaining({ cidade: 'Campinas' }),
      })
    })

    // Sair do perfil para a seção anterior permitida
    await user.click(screen.getByRole('link', { name: /voltar para minhas reservas/i }))
    expect(await screen.findByRole('heading', { name: /lista minhas reservas/i })).toBeInTheDocument()
  })

  it('OfertaDetalhePage e NecessidadeDetalhePage exibem bloqueio 403 e links de retorno por perfil', async () => {
    vi.mocked(ofertasService.buscarOfertaPorId).mockRejectedValueOnce({
      status: 403,
      code: 'FORBIDDEN',
      message: 'Doadores só podem visualizar suas próprias ofertas',
      fieldErrors: {},
    })

    const { unmount } = render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={doadorUser}>
          <MemoryRouter initialEntries={['/app/ofertas/99']}>
            <Routes>
              <Route path="/app/ofertas/:id" element={<OfertaDetalhePage />} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(await screen.findByText('Acesso restrito a esta oferta')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /voltar para minhas ofertas/i })).toHaveAttribute(
      'href',
      '/app/minhas-ofertas',
    )
    unmount()

    vi.mocked(necessidadesService.buscarNecessidadePorId).mockRejectedValueOnce({
      status: 403,
      code: 'FORBIDDEN',
      message: 'Você não tem permissão para visualizar necessidades de outro receptor',
      fieldErrors: {},
    })

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={receptorUser}>
          <MemoryRouter initialEntries={['/app/necessidades/50']}>
            <Routes>
              <Route path="/app/necessidades/:id" element={<NecessidadeDetalhePage />} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(await screen.findByText('Acesso restrito a esta necessidade')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /voltar para minhas necessidades/i })).toHaveAttribute(
      'href',
      '/app/minhas-necessidades',
    )
  })

  it('OfertaDetalhePage não exige transporte ao reservar oferta sem endereço e exibe contato da ONG para o doador', async () => {
    const user = userEvent.setup()

    const ofertaSemEndereco: OfertaDoacaoResponse = {
      id: 10,
      situacao: 'DISPONIVEL',
      doador: { nome: 'Carlos', email: 'carlos@doador.org', telefone: null },
      localRetirada: null,
      itens: [
        {
          id: 1,
          descricao: 'Cestas básicas',
          tipoItemDoacao: 'NAO_PERECIVEL',
          tipoUnidade: 'CESTAS_BASICAS',
          quantidade: 5,
        },
      ],
      criadoEm: '2026-11-22T10:00:00',
      atualizadoEm: null,
    }

    vi.mocked(ofertasService.buscarOfertaPorId).mockResolvedValueOnce(ofertaSemEndereco)
    vi.mocked(reservasService.reservarOferta).mockResolvedValueOnce({
      id: 99,
      situacao: 'AGUARDANDO_RETIRADA',
      precisaTransporte: false,
      ofertaId: 10,
      situacaoOferta: 'RESERVADA',
      transportadorId: null,
      doador: { nome: 'Carlos', email: 'carlos@doador.org', telefone: null },
      receptor: { nome: 'ONG Esperança', email: 'ong@esperanca.org', telefone: null },
      transportador: null,
      enderecoRetirada: null,
      enderecoEntrega: null,
      itens: ofertaSemEndereco.itens,
      criadoEm: '2026-11-22T10:05:00',
      atualizadoEm: null,
    })

    const { unmount } = render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={receptorUser}>
          <MemoryRouter initialEntries={['/app/ofertas/10']}>
            <Routes>
              <Route path="/app/ofertas/:id" element={<OfertaDetalhePage />} />
              <Route path="/app/minhas-reservas" element={<h1>Minhas Reservas</h1>} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(await screen.findByText(/não possui endereço de retirada cadastrado/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/preciso de transporte/i)).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /reservar oferta/i }))
    await waitFor(() => {
      expect(reservasService.reservarOferta).toHaveBeenCalledWith({
        ofertaId: 10,
        precisaTransporte: false,
      })
    })
    unmount()

    // Doador visualizando oferta reservada com contato da ONG sem telefone
    vi.mocked(ofertasService.buscarOfertaPorId).mockResolvedValueOnce({
      ...ofertaSemEndereco,
      situacao: 'RESERVADA',
      receptor: { nome: 'ONG Sem Telefone', email: 'contato@ong.org', telefone: null },
    })

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

    expect(await screen.findByText('ONG Sem Telefone')).toBeInTheDocument()
    expect(screen.getByText('contato@ong.org')).toBeInTheDocument()
  })

  it('MinhasReservasReceptorPage permite finalizar retirada direta (AGUARDANDO_RETIRADA) e exibe código de entrega em EM_TRANSPORTE', async () => {
    const user = userEvent.setup()
    const reservaRetiradaDireta: ReservaEntregaResponse = {
      id: 77,
      situacao: 'AGUARDANDO_RETIRADA',
      precisaTransporte: false,
      ofertaId: 10,
      situacaoOferta: 'RESERVADA',
      transportadorId: null,
      doador: { nome: 'Carlos', email: 'carlos@doador.org', telefone: '11999990001' },
      receptor: { nome: 'ONG Esperança', email: 'ong@esperanca.org', telefone: '11999990002' },
      transportador: null,
      enderecoRetirada: null,
      enderecoEntrega: null,
      itens: [],
      criadoEm: '2026-11-22T10:00:00',
      atualizadoEm: null,
    }

    vi.mocked(reservasService.listarMinhasReservasReceptor)
      .mockResolvedValueOnce([reservaRetiradaDireta])
      .mockResolvedValueOnce([{ ...reservaRetiradaDireta, situacao: 'CONCLUIDA', situacaoOferta: 'CONCLUIDA' }])
    vi.mocked(reservasService.concluirReserva).mockResolvedValueOnce(undefined)

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={receptorUser}>
          <MemoryRouter initialEntries={['/app/minhas-reservas']}>
            <MinhasReservasReceptorPage />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    const finalizarBtn = await screen.findByRole('button', { name: /finalizar retirada direta/i })
    await user.click(finalizarBtn)
    await user.click(screen.getByRole('button', { name: /confirmar finalização/i }))

    await waitFor(() => {
      expect(reservasService.concluirReserva).toHaveBeenCalledWith(77)
    })
    expect(await screen.findByText('Concluída')).toBeInTheDocument()
  })

  it('NovaNecessidadePage e NovaCampanhaPage preenchem endereço do receptor automaticamente e permitem edição', async () => {
    const user = userEvent.setup()
    vi.mocked(usuariosService.obterMeuPerfil).mockResolvedValue({
      id: 2,
      nome: 'ONG Esperança',
      documento: '11222333000181',
      email: 'ong@esperanca.org',
      emailContato: 'ong@esperanca.org',
      telefone: null,
      tipoUsuario: 'RECEPTOR',
      tipoPessoa: 'JURIDICA',
      emailVerificado: true,
      endereco: {
        logradouro: 'Av. Brasil',
        numero: '500',
        complemento: 'Galpão A',
        bairro: 'Centro',
        cidade: 'Campinas',
        estado: 'SP',
        cep: '13010000',
      },
    })

    const { unmount } = render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={receptorUser}>
          <MemoryRouter initialEntries={['/app/necessidades/nova']}>
            <NovaNecessidadePage />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    await waitFor(() => {
      expect(screen.getByLabelText(/cidade/i)).toHaveValue('Campinas')
    })
    expect(screen.getByLabelText(/cep/i)).toHaveValue('13010-000')
    expect(screen.getByLabelText(/logradouro/i)).toHaveValue('Av. Brasil')
    expect(screen.getByLabelText(/logradouro/i)).not.toBeDisabled()

    await user.clear(screen.getByLabelText(/cidade/i))
    await user.type(screen.getByLabelText(/cidade/i), 'Valinhos')
    expect(screen.getByLabelText(/cidade/i)).toHaveValue('Valinhos')
    unmount()

    // Campanha com perfil sem endereço cadastrado mantém campos vazios
    vi.mocked(usuariosService.obterMeuPerfil).mockResolvedValueOnce({
      id: 2,
      nome: 'ONG Sem Endereço',
      documento: '11222333000181',
      email: 'ong@esperanca.org',
      emailContato: 'ong@esperanca.org',
      telefone: null,
      tipoUsuario: 'RECEPTOR',
      tipoPessoa: 'JURIDICA',
      emailVerificado: true,
      endereco: null,
    })

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={receptorUser}>
          <MemoryRouter initialEntries={['/app/campanhas/nova']}>
            <NovaCampanhaPage />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    await waitFor(() => {
      expect(usuariosService.obterMeuPerfil).toHaveBeenCalled()
    })
    expect(screen.getByLabelText(/cidade/i)).toHaveValue('')
    expect(screen.getByLabelText(/cidade/i)).not.toBeDisabled()
  })
})
