export function limparDigitos(valor: string): string {
  return valor.replace(/\D/g, '')
}

export function validarCpf(valor: string): boolean {
  const digits = limparDigitos(valor)
  if (digits.length !== 11 || /^(\d)\1{10}$/.test(digits)) {
    return false
  }

  let soma = 0
  for (let i = 0; i < 9; i++) {
    soma += Number(digits[i]) * (10 - i)
  }
  let resto = (soma * 10) % 11
  if (resto === 10) resto = 0
  if (resto !== Number(digits[9])) return false

  soma = 0
  for (let i = 0; i < 10; i++) {
    soma += Number(digits[i]) * (11 - i)
  }
  resto = (soma * 10) % 11
  if (resto === 10) resto = 0
  return resto === Number(digits[10])
}

export function validarCnpj(valor: string): boolean {
  const digits = limparDigitos(valor)
  if (digits.length !== 14 || /^(\d)\1{13}$/.test(digits)) {
    return false
  }

  const calcDigito = (base: string, pesos: number[]) => {
    let soma = 0
    for (let i = 0; i < pesos.length; i++) {
      soma += Number(base[i]) * pesos[i]
    }
    const resto = soma % 11
    return resto < 2 ? 0 : 11 - resto
  }

  const d1 = calcDigito(digits.slice(0, 12), [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2])
  if (d1 !== Number(digits[12])) return false

  const d2 = calcDigito(digits.slice(0, 13), [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2])
  return d2 === Number(digits[13])
}

export function validarCep(valor: string): boolean {
  const digits = limparDigitos(valor)
  return digits.length === 8 && digits !== '00000000'
}

export function validarTelefoneOpcional(valor: string | null | undefined): boolean {
  if (!valor || valor.trim() === '') return true
  const digits = limparDigitos(valor)
  return digits.length >= 10 && digits.length <= 11
}

