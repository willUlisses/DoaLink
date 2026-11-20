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

export const itemOfertaSchema = z.object({
  tipoItemDoacao: z.enum(categoriasTuple, {
    error: 'Selecione a categoria do item.',
  }),
  tipoUnidade: z.enum(unidadesTuple, {
    error: 'Selecione a unidade de medida.',
  }),
  descricao: z
    .string()
    .trim()
    .min(1, 'Informe a descrição do item.')
    .max(500, 'A descrição deve ter no máximo 500 caracteres.'),
  quantidade: z
    .number({ error: 'Informe uma quantidade numérica válida.' })
    .positive('A quantidade deve ser maior que zero.'),
})

const enderecoOpcionalRawSchema = z.object({
  cep: z.string(),
  estado: z.string(),
  cidade: z.string(),
  bairro: z.string(),
  logradouro: z.string(),
  numero: z.string().optional(),
  complemento: z.string().optional(),
})

export const criarOfertaSchema = z
  .object({
    itens: z
      .array(itemOfertaSchema)
      .min(1, 'Adicione pelo menos 1 item à oferta.')
      .max(50, 'A oferta pode conter no máximo 50 itens.'),
    informarEndereco: z.boolean(),
    endereco: enderecoOpcionalRawSchema,
  })
  .superRefine((data, ctx) => {
    if (data.informarEndereco) {
      const parsed = enderecoSchema.safeParse(data.endereco)
      if (!parsed.success) {
        for (const issue of parsed.error.issues) {
          ctx.addIssue({
            code: 'custom',
            path: ['endereco', ...issue.path],
            message: issue.message,
          })
        }
      }
    }
  })

export type CriarOfertaFormValues = z.infer<typeof criarOfertaSchema>

