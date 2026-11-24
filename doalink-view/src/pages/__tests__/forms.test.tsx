import { act, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../../contexts/AuthContext.tsx'
import { ToastProvider } from '../../contexts/ToastContext.tsx'
import type { NormalizedApiError } from '../../types/api.ts'
import * as authService from '../../services/auth.service.ts'
import * as ofertasService from '../../services/ofertas.service.ts'
import { CadastroPage } from '../auth/CadastroPage.tsx'
import { LoginPage } from '../auth/LoginPage.tsx'
import { VerificarEmailPage } from '../auth/VerificarEmailPage.tsx'
import { NovaOfertaPage } from '../app/ofertas/NovaOfertaPage.tsx'

vi.mock('../../services/auth.service.ts', async () => {
  const actual = await vi.importActual<typeof import('../../services/auth.service.ts')>(
    '../../services/auth.service.ts',
  )
  return {
    ...actual,
    login: vi.fn(),
    cadastrarDoador: vi.fn(),
    cadastrarReceptor: vi.fn(),
    reenviarCodigo: vi.fn(),
  }
})

vi.mock('../../services/ofertas.service.ts', async () => {
  const actual = await vi.importActual<typeof import('../../services/ofertas.service.ts')>(
    '../../services/ofertas.service.ts',
  )
  return {
    ...actual,
    criarOferta: vi.fn(),
  }
})

describe('Fluxos críticos de formulário', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('LoginPage diferencia 403 de e-mail não verificado e direciona para confirmação', async () => {
    const user = userEvent.setup()
    const emailNotVerifiedError: NormalizedApiError = {
      status: 403,
      code: 'EMAIL_NOT_VERIFIED',
      message: 'E-mail não verificado. Confirme o código antes de acessar.',
      fieldErrors: {},
    }
    vi.mocked(authService.login).mockRejectedValueOnce(emailNotVerifiedError)

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={null}>
          <MemoryRouter initialEntries={['/login']}>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/verificar-email" element={<h1>Tela Verificar E-mail</h1>} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    await user.type(screen.getByLabelText(/e-mail/i), 'pendente@doalink.org')
    await user.type(screen.getByLabelText(/^senha/i), 'senha1234')
    await user.click(screen.getByRole('button', { name: /^entrar$/i }))

    expect(await screen.findByText(/confirmação de e-mail pendente/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /confirmar e-mail/i }))
    expect(await screen.findByRole('heading', { name: /tela verificar e-mail/i })).toBeInTheDocument()
  })

  it('CadastroPage alterna campos por perfil, remove pontuação do documento e mapeia erro 409 ao campo sem apagar valores válidos', async () => {
    const user = userEvent.setup()
    const conflictError: NormalizedApiError = {
      status: 409,
      code: 'CONFLICT',
      message: 'E-mail já cadastrado',
      fieldErrors: { email: 'E-mail já cadastrado' },
    }
    vi.mocked(authService.cadastrarDoador).mockRejectedValueOnce(conflictError)

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={null}>
          <MemoryRouter initialEntries={['/cadastro']}>
            <Routes>
              <Route path="/cadastro" element={<CadastroPage />} />
              <Route path="/verificar-email" element={<h1>Tela Verificar E-mail</h1>} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    // Alterna para Receptor e verifica que exige endereço e CNPJ
    await user.click(screen.getByText(/receptor \(ong\)/i))
    expect(screen.getByLabelText(/nome da organização/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^cnpj/i)).toBeInTheDocument()
    expect(screen.getByText(/endereço da organização/i)).toBeInTheDocument()

    // Volta para Doador e seleciona Pessoa Física (CPF)
    await user.click(screen.getByText(/^doador$/i))
    await user.click(screen.getByText(/pessoa física \(cpf\)/i))
    await user.type(screen.getByLabelText(/nome completo/i), 'Carlos Souza')
    await user.type(screen.getByLabelText(/^cpf/i), '52998224725')
    await user.type(screen.getByLabelText(/e-mail/i), 'carlos@exemplo.com')
    await user.type(screen.getByLabelText(/^senha/i), 'segredo123')

    await user.click(screen.getByRole('button', { name: /criar conta e enviar código/i }))

    await waitFor(() => {
      expect(authService.cadastrarDoador).toHaveBeenCalledWith({
        nome: 'Carlos Souza',
        email: 'carlos@exemplo.com',
        senha: 'segredo123',
        cpf: '52998224725',
      })
    })

    // Erro 409 mapeado ao campo e-mail mantendo os valores preenchidos
    const errorOccurrences = await screen.findAllByText('E-mail já cadastrado')
    expect(errorOccurrences.length).toBeGreaterThanOrEqual(2)
    expect(screen.getByLabelText(/e-mail/i)).toHaveAttribute('aria-invalid', 'true')
    expect(screen.getByLabelText(/nome completo/i)).toHaveValue('Carlos Souza')
  })

  it('NovaOfertaPage permite itens dinâmicos, mapeia erro 400 ao campo e bloqueia duplo envio', async () => {
    const user = userEvent.setup()

    let resolveRequest!: (value: never) => void
    const pendingPromise = new Promise<never>((resolve) => {
      resolveRequest = resolve
    })

    const validationError: NormalizedApiError = {
      status: 400,
      code: 'VALIDATION_ERROR',
      message: 'itens[0].quantidade: quantidade inválida no servidor',
      fieldErrors: { 'itens.0.quantidade': 'quantidade inválida no servidor' },
    }

    vi.mocked(ofertasService.criarOferta)
      .mockRejectedValueOnce(validationError)
      .mockReturnValueOnce(pendingPromise)

    render(
      <ToastProvider>
        <AuthProvider
          skipInitialRefresh
          initialUser={{
            id: 1,
            nome: 'Carlos',
            email: 'carlos@doalink.org',
            tipoUsuario: 'DOADOR',
          }}
        >
          <MemoryRouter initialEntries={['/app/ofertas/nova']}>
            <NovaOfertaPage />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    // Adiciona um segundo item e remove em seguida
    await user.click(screen.getByRole('button', { name: /adicionar item/i }))
    expect(screen.getByText('Item #2')).toBeInTheDocument()
    const removeButtons = screen.getAllByRole('button', { name: /remover/i })
    await user.click(removeButtons[1])
    expect(screen.queryByText('Item #2')).not.toBeInTheDocument()

    await user.type(screen.getByLabelText(/descrição do item/i), 'Feijão carioca')
    await user.click(screen.getByRole('button', { name: /publicar oferta/i }))

    // Erro 400 mapeado para itens.0.quantidade
    expect(await screen.findByText('quantidade inválida no servidor')).toBeInTheDocument()

    // Segundo envio fica em loading e desabilita o botão contra duplo clique
    const submitBtn = screen.getByRole('button', { name: /publicar oferta/i })
    await user.click(submitBtn)
    await user.click(submitBtn)

    expect(submitBtn).toBeDisabled()
    expect(ofertasService.criarOferta).toHaveBeenCalledTimes(2)
    resolveRequest(undefined as never)
  })

  it('VerificarEmailPage exibe contagem regressiva de 90s e permite reenviar código após expiração', async () => {
    vi.useFakeTimers()
    vi.mocked(authService.reenviarCodigo).mockResolvedValueOnce({
      mensagem: 'Se o e-mail estiver cadastrado e pendente, um novo código foi enviado.',
    })

    render(
      <ToastProvider>
        <AuthProvider skipInitialRefresh initialUser={null}>
          <MemoryRouter
            initialEntries={[{ pathname: '/verificar-email', state: { email: 'ana@doalink.org' } }]}
          >
            <VerificarEmailPage />
          </MemoryRouter>
        </AuthProvider>
      </ToastProvider>,
    )

    expect(screen.getByText('90s')).toBeInTheDocument()

    await act(async () => {
      vi.advanceTimersByTime(90_000)
    })

    expect(screen.getByText(/tempo esgotado/i)).toBeInTheDocument()
    vi.useRealTimers()

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: /reenviar código/i }))

    await waitFor(() => {
      expect(authService.reenviarCodigo).toHaveBeenCalledWith({ email: 'ana@doalink.org' })
    })
    expect(screen.getByText('90s')).toBeInTheDocument()
  })
})
