export interface EnderecoViaCep {
  estado: string
  cidade: string
  bairro: string
  logradouro: string
}

interface ViaCepResponse {
  erro?: boolean | string
  uf?: string
  localidade?: string
  bairro?: string
  logradouro?: string
}

const VIACEP_URL = 'https://viacep.com.br/ws'
const TIMEOUT_MS = 5000

/**
 * Consulta o ViaCEP. Retorna null quando o CEP não existe ou a consulta falha;
 * o preenchimento manual do endereço continua sempre possível.
 */
export async function buscarEnderecoPorCep(
  cep: string,
  signal?: AbortSignal,
): Promise<EnderecoViaCep | null> {
  const digits = cep.replace(/\D/g, '')
  if (digits.length !== 8) return null

  const timeout = new AbortController()
  const timer = setTimeout(() => timeout.abort(), TIMEOUT_MS)
  const onAbort = () => timeout.abort()
  signal?.addEventListener('abort', onAbort)

  try {
    const response = await fetch(`${VIACEP_URL}/${digits}/json/`, { signal: timeout.signal })
    if (!response.ok) return null
    const data = (await response.json()) as ViaCepResponse
    if (data.erro) return null
    return {
      estado: data.uf ?? '',
      cidade: data.localidade ?? '',
      bairro: data.bairro ?? '',
      logradouro: data.logradouro ?? '',
    }
  } catch {
    return null
  } finally {
    clearTimeout(timer)
    signal?.removeEventListener('abort', onAbort)
  }
}

