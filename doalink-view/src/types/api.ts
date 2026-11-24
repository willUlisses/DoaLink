export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface MensagemResponse {
  mensagem: string
}

export interface ErrorResponsePayload {
  message?: string
  status?: number
}

export type ErrorCode =
  | 'VALIDATION_ERROR'
  | 'UNAUTHORIZED'
  | 'EMAIL_NOT_VERIFIED'
  | 'FORBIDDEN'
  | 'NOT_FOUND'
  | 'CONFLICT'
  | 'NETWORK_ERROR'
  | 'UNKNOWN_ERROR'

export interface NormalizedApiError {
  status: number
  code: ErrorCode
  message: string
  fieldErrors: Record<string, string>
}

