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

export const itemNecessidadeSchema = z.object({
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
    .max(500, 'A descrição deve ter no máximo 500 caracteres.')
    .optional()
    .or(z.literal('')),
})

export const criarNecessidadeSchema = z.object({
  itens: z
    .array(itemNecessidadeSchema)
    .min(1, 'Adicione pelo menos 1 item necessário.')
    .max(50, 'A necessidade pode conter no máximo 50 itens.'),
  localEntrega: enderecoSchema,
})

export type CriarNecessidadeFormValues = z.infer<typeof criarNecessidadeSchema>

