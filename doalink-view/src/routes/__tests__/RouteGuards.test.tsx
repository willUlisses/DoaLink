import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { AuthProvider } from '../../contexts/AuthContext.tsx'
import { ToastProvider } from '../../contexts/ToastContext.tsx'
import type { UsuarioResponse } from '../../types/auth.ts'
import { AppRoutes } from '../router.tsx'

function renderAtRoute(initialPath: string, initialUser: UsuarioResponse | null) {
  return render(
    <ToastProvider>
      <AuthProvider skipInitialRefresh initialUser={initialUser}>
        <MemoryRouter initialEntries={[initialPath]}>
          <AppRoutes />
        </MemoryRouter>
      </AuthProvider>
    </ToastProvider>,
  )
}

describe('Proteção de rotas e redirecionamentos por perfil', () => {
  it('redireciona visitante não autenticado de rota protegida para /login', async () => {
    renderAtRoute('/app/minhas-ofertas', null)

    expect(
      await screen.findByRole('heading', { name: /entrar no doalink/i }),
    ).toBeInTheDocument()
  })

  it('redireciona usuário com perfil incorreto para /acesso-negado', async () => {
    const doador: UsuarioResponse = {
      id: 1,
      nome: 'Maria Doadora',
      email: 'maria@doalink.org',
      tipoUsuario: 'DOADOR',
    }

    renderAtRoute('/app/minhas-necessidades', doador)

    expect(
      await screen.findByRole('heading', { name: /acesso negado/i }),
    ).toBeInTheDocument()
  })

  it('permite acesso ao perfil autorizado na rota correspondente', async () => {
    const doador: UsuarioResponse = {
      id: 1,
      nome: 'Maria Doadora',
      email: 'maria@doalink.org',
      tipoUsuario: 'DOADOR',
    }

    renderAtRoute('/app/ofertas/nova', doador)

    expect(
      await screen.findByRole('heading', { name: /publicar oferta de doação/i }),
    ).toBeInTheDocument()
  })

  it('redireciona usuário já autenticado de /login para o painel', async () => {
    const receptor: UsuarioResponse = {
      id: 2,
      nome: 'ONG Esperança',
      email: 'contato@esperanca.org',
      tipoUsuario: 'RECEPTOR',
    }

    renderAtRoute('/login', receptor)

    expect(await screen.findByRole('heading', { name: /olá, ong esperança/i })).toBeInTheDocument()
  })
})

