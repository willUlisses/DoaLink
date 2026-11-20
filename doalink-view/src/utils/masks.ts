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

