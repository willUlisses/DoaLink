import { z } from 'zod'
import { obterDataLocalHojeISO } from '../utils/formatters.ts'
import { enderecoSchema } from './endereco.schema.ts'

const veiculosTuple = [
  'MOTO',
  'CARRO_PASSEIO',
  'UTILITARIO_LEVE',
  'CAMINHONETE',
  'CAMINHAO_PEQUENO',
  'CAMINHAO_MEDIO',
  'CAMINHAO_GRANDE',
  'OUTRO',
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

export const criarDisponibilidadeSchema = z.object({
  tipoVeiculo: z.enum(veiculosTuple, {
    error: 'Selecione o tipo de veículo.',
  }),
  capacidade: z
    .number({ error: 'Informe a capacidade em formato numérico.' })
    .positive('A capacidade deve ser maior que zero.'),
  tipoUnidade: z.enum(unidadesTuple, {
    error: 'Selecione a unidade da capacidade.',
  }),
  dataDisponivel: z
    .string()
    .trim()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Informe uma data válida.')
    .refine(
      (val) => val >= obterDataLocalHojeISO(),
      'A data disponível deve ser igual ou posterior a hoje.',
    ),
  horarioDisponivel: z
    .string()
    .trim()
    .refine(
      (val) => val === '' || /^([01]\d|2[0-3]):[0-5]\d$/.test(val),
      'Informe um horário válido no formato HH:mm.',
    )
    .optional()
    .or(z.literal('')),
  origem: enderecoSchema,
})

export type CriarDisponibilidadeFormValues = z.infer<typeof criarDisponibilidadeSchema>

