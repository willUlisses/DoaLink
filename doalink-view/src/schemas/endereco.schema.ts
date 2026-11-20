import { z } from 'zod'
import { UFS_BRASIL } from '../types/enums.ts'
import { validarCep } from '../utils/validators.ts'

export const enderecoSchema = z.object({
  cep: z
    .string()
    .trim()
    .min(1, 'Informe o CEP.')
    .refine(validarCep, 'Informe um CEP válido com 8 dígitos.'),
  estado: z
    .string()
    .trim()
    .refine(
      (v) => (UFS_BRASIL as readonly string[]).includes(v.toUpperCase()),
      'Selecione uma UF válida.',
    ),
  cidade: z
    .string()
    .trim()
    .min(1, 'Informe a cidade.')
    .max(255, 'A cidade deve ter no máximo 255 caracteres.'),
  bairro: z
    .string()
    .trim()
    .min(1, 'Informe o bairro.')
    .max(255, 'O bairro deve ter no máximo 255 caracteres.'),
  logradouro: z
    .string()
    .trim()
    .min(1, 'Informe o logradouro.')
    .max(255, 'O logradouro deve ter no máximo 255 caracteres.'),
  numero: z
    .string()
    .trim()
    .max(20, 'O número deve ter no máximo 20 caracteres.')
    .optional()
    .or(z.literal('')),
  complemento: z
    .string()
    .trim()
    .max(100, 'O complemento deve ter no máximo 100 caracteres.')
    .optional()
    .or(z.literal('')),
})

export type EnderecoFormValues = z.infer<typeof enderecoSchema>
