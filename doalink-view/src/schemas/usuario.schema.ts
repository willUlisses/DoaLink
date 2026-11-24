import { z } from 'zod'
import { validarTelefoneOpcional } from '../utils/validators.ts'
import { enderecoSchema } from './endereco.schema.ts'

export const contatoSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Informe o e-mail de contato.')
    .max(254, 'O e-mail deve ter no máximo 254 caracteres.')
    .email('Informe um endereço de e-mail válido.'),
  telefone: z
    .string()
    .trim()
    .refine(validarTelefoneOpcional, 'Informe um telefone válido com DDD (10 ou 11 dígitos).')
    .optional()
    .or(z.literal('')),
})

export type ContatoFormValues = z.infer<typeof contatoSchema>

export const atualizarUsuarioSchema = z.object({
  endereco: enderecoSchema,
})

export type AtualizarUsuarioFormValues = z.infer<typeof atualizarUsuarioSchema>
