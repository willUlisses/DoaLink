import { z } from 'zod'
import { enderecoSchema } from './endereco.schema.ts'

const categoriasTuple = [
  'PERECIVEL',
  'NAO_PERECIVEL',
  'HIGIENE',
  'ROUPAS_E_TEXTIL',
  'MEDICAMENTOS',
  'ELETRODOMESTICOS',
] as const

const unidadesTuple = [
  'KG',
  'T',
  'G',
  'ML',
  'CAIXAS',
  'CESTAS_BASICAS',
  'SEMUNIDADE',
] as const

export const itemCampanhaSchema = z.object({
  tipoItemDoacao: z.enum(categoriasTuple, {
    error: 'Selecione a categoria do item.',
  }),
  tipoUnidade: z.enum(unidadesTuple, {
    error: 'Selecione a unidade de medida.',
  }),
  quantidade: z
    .number({ error: 'Informe uma quantidade numérica válida.' })
    .positive('A quantidade deve ser maior que zero.'),
  descricao: z
    .string()
    .trim()
    .max(500, 'A descrição do item deve ter no máximo 500 caracteres.')
    .optional()
    .or(z.literal('')),
})

export const criarCampanhaSchema = z.object({
  nome: z
    .string()
    .trim()
    .min(1, 'Informe o nome da campanha.')
    .max(150, 'O nome da campanha deve ter no máximo 150 caracteres.'),
  descricao: z
    .string()
    .trim()
    .max(1000, 'A descrição da campanha deve ter no máximo 1000 caracteres.')
    .optional()
    .or(z.literal('')),
  enderecoRecebimento: enderecoSchema,
  itens: z
    .array(itemCampanhaSchema)
    .max(50, 'A campanha pode conter no máximo 50 itens de necessidade.'),
})

export type CriarCampanhaFormValues = z.infer<typeof criarCampanhaSchema>

