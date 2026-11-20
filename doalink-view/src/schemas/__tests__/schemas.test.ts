import { describe, expect, it } from 'vitest'
import { cadastroSchema } from '../auth.schema.ts'
import { criarOfertaSchema } from '../oferta.schema.ts'
import { criarDisponibilidadeSchema } from '../transporte.schema.ts'
import { obterDataLocalHojeISO } from '../../utils/formatters.ts'
import { validarCnpj, validarCpf } from '../../utils/validators.ts'

describe('Validadores de CPF e CNPJ', () => {
  it('valida CPFs válidos e rejeita inválidos ou sequências repetidas', () => {
    expect(validarCpf('52998224725')).toBe(true)
    expect(validarCpf('529.982.247-25')).toBe(true)
    expect(validarCpf('111.111.111-11')).toBe(false)
    expect(validarCpf('52998224726')).toBe(false)
    expect(validarCpf('12345')).toBe(false)
  })

  it('valida CNPJs válidos e rejeita inválidos ou sequências repetidas', () => {
    expect(validarCnpj('11222333000181')).toBe(true)
    expect(validarCnpj('11.222.333/0001-81')).toBe(true)
    expect(validarCnpj('00.000.000/0000-00')).toBe(false)
    expect(validarCnpj('11.222.333/0001-82')).toBe(false)
  })
})

describe('cadastroSchema', () => {
  const enderecoValido = {
    cep: '01310-100',
    estado: 'SP',
    cidade: 'São Paulo',
    bairro: 'Bela Vista',
    logradouro: 'Avenida Paulista',
    numero: '1000',
    complemento: '',
  }

  it('valida cadastro de Doador PF e rejeita senha maior que 60 caracteres', () => {
    const ok = cadastroSchema.safeParse({
      tipoUsuario: 'DOADOR',
      tipoPessoa: 'FISICA',
      nome: 'João Silva',
      cpf: '529.982.247-25',
      cnpj: '',
      email: 'joao@exemplo.com',
      senha: 'a'.repeat(60),
      endereco: { cep: '', estado: '', cidade: '', bairro: '', logradouro: '' },
    })
    expect(ok.success).toBe(true)

    const senhaLonga = cadastroSchema.safeParse({
      tipoUsuario: 'DOADOR',
      tipoPessoa: 'FISICA',
      nome: 'João Silva',
      cpf: '529.982.247-25',
      cnpj: '',
      email: 'joao@exemplo.com',
      senha: 'a'.repeat(61),
      endereco: { cep: '', estado: '', cidade: '', bairro: '', logradouro: '' },
    })
    expect(senhaLonga.success).toBe(false)
  })

  it('exige endereço completo e CNPJ válido para Receptor, aceitando senha até 72 chars', () => {
    const semEndereco = cadastroSchema.safeParse({
      tipoUsuario: 'RECEPTOR',
      tipoPessoa: 'JURIDICA',
      nome: 'ONG Solidária',
      cnpj: '11.222.333/0001-81',
      email: 'ong@exemplo.org',
      senha: 'b'.repeat(72),
      endereco: { cep: '', estado: '', cidade: '', bairro: '', logradouro: '' },
    })
    expect(semEndereco.success).toBe(false)

    const completo = cadastroSchema.safeParse({
      tipoUsuario: 'RECEPTOR',
      tipoPessoa: 'JURIDICA',
      nome: 'ONG Solidária',
      cnpj: '11.222.333/0001-81',
      email: 'ong@exemplo.org',
      senha: 'b'.repeat(72),
      endereco: enderecoValido,
    })
    expect(completo.success).toBe(true)
  })
})

describe('criarOfertaSchema', () => {
  it('aceita oferta sem endereço quando informarEndereco é falso e exige endereço quando verdadeiro', () => {
    const semEndereco = criarOfertaSchema.safeParse({
      itens: [
        {
          tipoItemDoacao: 'NAO_PERECIVEL',
          tipoUnidade: 'KG',
          descricao: 'Arroz tipo 1',
          quantidade: 10,
        },
      ],
      informarEndereco: false,
      endereco: { cep: '', estado: '', cidade: '', bairro: '', logradouro: '' },
    })
    expect(semEndereco.success).toBe(true)

    const enderecoIncompleto = criarOfertaSchema.safeParse({
      itens: [
        {
          tipoItemDoacao: 'NAO_PERECIVEL',
          tipoUnidade: 'KG',
          descricao: 'Arroz tipo 1',
          quantidade: 10,
        },
      ],
      informarEndereco: true,
      endereco: { cep: '123', estado: '', cidade: '', bairro: '', logradouro: '' },
    })
    expect(enderecoIncompleto.success).toBe(false)
  })

  it('rejeita quantidade <= 0 e lista vazia de itens', () => {
    const res = criarOfertaSchema.safeParse({
      itens: [
        {
          tipoItemDoacao: 'HIGIENE',
          tipoUnidade: 'CAIXAS',
          descricao: 'Sabonetes',
          quantidade: 0,
        },
      ],
      informarEndereco: false,
      endereco: { cep: '', estado: '', cidade: '', bairro: '', logradouro: '' },
    })
    expect(res.success).toBe(false)
  })
})

describe('criarDisponibilidadeSchema', () => {
  const origemValida = {
    cep: '01310100',
    estado: 'SP',
    cidade: 'São Paulo',
    bairro: 'Bela Vista',
    logradouro: 'Av Paulista',
    numero: '100',
    complemento: '',
  }

  it('aceita data de hoje (fuso local) e rejeita data passada', () => {
    const hoje = obterDataLocalHojeISO()
    const valido = criarDisponibilidadeSchema.safeParse({
      tipoVeiculo: 'UTILITARIO_LEVE',
      capacidade: 250,
      tipoUnidade: 'KG',
      dataDisponivel: hoje,
      horarioDisponivel: '09:30',
      origem: origemValida,
    })
    expect(valido.success).toBe(true)

    const passado = criarDisponibilidadeSchema.safeParse({
      tipoVeiculo: 'UTILITARIO_LEVE',
      capacidade: 250,
      tipoUnidade: 'KG',
      dataDisponivel: '2020-01-01',
      horarioDisponivel: '',
      origem: origemValida,
    })
    expect(passado.success).toBe(false)
  })
})

