import { z } from 'zod'

export const criarReservaSchema = z.object({
  precisaTransporte: z.boolean(),
})

export type CriarReservaFormValues = z.infer<typeof criarReservaSchema>

export const confirmarCodigoTransporteSchema = z.object({
  codigo: z
    .string()
    .trim()
    .regex(/^\d{6}$/, 'Informe o código numérico de 6 dígitos.'),
})

export type ConfirmarCodigoTransporteFormValues = z.infer<typeof confirmarCodigoTransporteSchema>
