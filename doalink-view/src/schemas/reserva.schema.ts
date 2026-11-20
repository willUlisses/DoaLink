import { z } from 'zod'

export const criarReservaSchema = z.object({
  precisaTransporte: z.boolean(),
})

export type CriarReservaFormValues = z.infer<typeof criarReservaSchema>

export const vincularTransporteSchema = z.object({
  disponibilidadeId: z
    .string()
    .trim()
    .min(1, 'Selecione uma disponibilidade de transporte ativa.'),
})

export type VincularTransporteFormValues = z.infer<typeof vincularTransporteSchema>

