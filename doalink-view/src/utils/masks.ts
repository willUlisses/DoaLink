import { limparDigitos } from './validators.ts'

export function mascararCpf(valor: string): string {
  const digits = limparDigitos(valor).slice(0, 11)
  return digits
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
}

export function mascararCnpj(valor: string): string {
  const digits = limparDigitos(valor).slice(0, 14)
  return digits
    .replace(/^(\d{2})(\d)/, '$1.$2')
    .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1/$2')
    .replace(/(\d{4})(\d{1,2})$/, '$1-$2')
}

export function mascararCep(valor: string): string {
  const digits = limparDigitos(valor).slice(0, 8)
  return digits.replace(/(\d{5})(\d{1,3})$/, '$1-$2')
}

export function formatarCep(cep: string | null | undefined): string {
  if (!cep) return ''
  const digits = limparDigitos(cep)
  if (digits.length !== 8) return cep
  return `${digits.slice(0, 5)}-${digits.slice(5)}`
}

export function mascararTelefone(valor: string): string {
  const digits = limparDigitos(valor).slice(0, 11)
  if (digits.length <= 2) return digits
  if (digits.length <= 6) {
    return `(${digits.slice(0, 2)}) ${digits.slice(2)}`
  }
  if (digits.length <= 10) {
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`
  }
  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`
}

export function formatarTelefone(telefone: string | null | undefined): string {
  if (!telefone) return ''
  const digits = limparDigitos(telefone)
  if (digits.length < 10 || digits.length > 11) return telefone
  return mascararTelefone(digits)
}

