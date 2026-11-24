import { z } from 'zod'
import { validarCnpj, validarCpf } from '../utils/validators.ts'
import { enderecoSchema } from './endereco.schema.ts'

export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Informe seu e-mail.')
    .max(254, 'O e-mail deve ter no máximo 254 caracteres.')
    .email('Informe um endereço de e-mail válido.'),
  senha: z.string().min(1, 'Informe sua senha.'),
})

export type LoginFormValues = z.infer<typeof loginSchema>

const tipoUsuarioTuple = ['DOADOR', 'TRANSPORTADOR', 'RECEPTOR'] as const
const tipoPessoaTuple = ['FISICA', 'JURIDICA'] as const

const enderecoOpcionalSchema = z.object({
  cep: z.string(),
  estado: z.string(),
  cidade: z.string(),
  bairro: z.string(),
  logradouro: z.string(),
  numero: z.string().optional(),
  complemento: z.string().optional(),
})

export const cadastroSchema = z
  .object({
    tipoUsuario: z.enum(tipoUsuarioTuple, {
      error: 'Selecione um perfil de acesso.',
    }),
    tipoPessoa: z.enum(tipoPessoaTuple),
    nome: z
      .string()
      .trim()
      .min(2, 'Informe o nome completo ou razão social (mínimo 2 caracteres).')
      .max(150, 'O nome deve ter no máximo 150 caracteres.'),
    cpf: z.string().optional(),
    cnpj: z.string().optional(),
    email: z
      .string()
      .trim()
      .min(1, 'Informe o e-mail.')
      .max(254, 'O e-mail deve ter no máximo 254 caracteres.')
      .email('Informe um endereço de e-mail válido.'),
    senha: z.string().min(8, 'A senha deve ter no mínimo 8 caracteres.'),
    endereco: enderecoOpcionalSchema,
  })
  .superRefine((data, ctx) => {
    const maxSenha = data.tipoUsuario === 'RECEPTOR' ? 72 : 60
    if (data.senha.length > maxSenha) {
      ctx.addIssue({
        code: 'custom',
        path: ['senha'],
        message: `A senha deve ter no máximo ${maxSenha} caracteres para este perfil.`,
      })
    }

    const exigeCnpj = data.tipoUsuario === 'RECEPTOR' || data.tipoPessoa === 'JURIDICA'

    if (exigeCnpj) {
      const cnpj = data.cnpj?.trim() ?? ''
      if (!cnpj) {
        ctx.addIssue({
          code: 'custom',
          path: ['cnpj'],
          message: 'Informe o CNPJ.',
        })
      } else if (!validarCnpj(cnpj)) {
        ctx.addIssue({
          code: 'custom',
          path: ['cnpj'],
          message: 'CNPJ inválido. Verifique os dígitos informados.',
        })
      }
    } else {
      const cpf = data.cpf?.trim() ?? ''
      if (!cpf) {
        ctx.addIssue({
          code: 'custom',
          path: ['cpf'],
          message: 'Informe o CPF.',
        })
      } else if (!validarCpf(cpf)) {
        ctx.addIssue({
          code: 'custom',
          path: ['cpf'],
          message: 'CPF inválido. Verifique os dígitos informados.',
        })
      }
    }

    if (data.tipoUsuario === 'RECEPTOR') {
      const parsedEndereco = enderecoSchema.safeParse(data.endereco)
      if (!parsedEndereco.success) {
        for (const issue of parsedEndereco.error.issues) {
          ctx.addIssue({
            code: 'custom',
            path: ['endereco', ...issue.path],
            message: issue.message,
          })
        }
      }
    }
  })

export type CadastroFormValues = z.infer<typeof cadastroSchema>

export const verificarEmailSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Informe o e-mail cadastrado.')
    .email('Informe um endereço de e-mail válido.'),
  codigo: z
    .string()
    .trim()
    .regex(/^\d{6}$/, 'Informe o código numérico de 6 dígitos.'),
})

export type VerificarEmailFormValues = z.infer<typeof verificarEmailSchema>

export const reenviarCodigoSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Informe o e-mail cadastrado.')
    .email('Informe um endereço de e-mail válido.'),
})

export type ReenviarCodigoFormValues = z.infer<typeof reenviarCodigoSchema>

export const solicitarRecuperacaoSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Informe seu e-mail.')
    .email('Informe um endereço de e-mail válido.'),
})

export type SolicitarRecuperacaoFormValues = z.infer<typeof solicitarRecuperacaoSchema>

export const redefinirSenhaSchema = z
  .object({
    novaSenha: z
      .string()
      .min(8, 'A nova senha deve ter no mínimo 8 caracteres.')
      .max(72, 'A nova senha deve ter no máximo 72 caracteres.'),
    confirmarSenha: z.string().min(1, 'Confirme a nova senha.'),
  })
  .refine((data) => data.novaSenha === data.confirmarSenha, {
    path: ['confirmarSenha'],
    message: 'As senhas não conferem.',
  })

export type RedefinirSenhaFormValues = z.infer<typeof redefinirSenhaSchema>

