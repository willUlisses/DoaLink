import type { EnderecoResponse } from '../types/endereco.ts'

export function obterDataLocalHojeISO(): string {
  const agora = new Date()
  const ano = agora.getFullYear()
  const mes = String(agora.getMonth() + 1).padStart(2, '0')
  const dia = String(agora.getDate()).padStart(2, '0')
  return `${ano}-${mes}-${dia}`
}

export function formatarData(isoDate: string | null | undefined): string {
  if (!isoDate) return '—'
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(isoDate)
  if (match) {
    return `${match[3]}/${match[2]}/${match[1]}`
  }
  const date = new Date(isoDate)
  if (Number.isNaN(date.getTime())) return isoDate
  return date.toLocaleDateString('pt-BR')
}

export function formatarDataHora(isoDateTime: string | null | undefined): string {
  if (!isoDateTime) return '—'
  const date = new Date(isoDateTime)
  if (Number.isNaN(date.getTime())) return isoDateTime
  return date.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatarHora(time: string | null | undefined): string {
  if (!time) return 'Horário flexível'
  const match = /^(\d{2}):(\d{2})/.exec(time)
  return match ? `${match[1]}:${match[2]}` : time
}

export function formatarQuantidade(valor: number): string {
  return new Intl.NumberFormat('pt-BR', {
    maximumFractionDigits: 2,
  }).format(valor)
}

export function formatarEnderecoResumo(endereco: EnderecoResponse | null | undefined): string {
  if (!endereco || (!endereco.cidade && !endereco.estado)) {
    return 'Local a combinar'
  }
  if (endereco.cidade && endereco.estado) {
    return `${endereco.cidade} / ${endereco.estado}`
  }
  return endereco.cidade || endereco.estado || 'Local a combinar'
}

export function formatarEnderecoCompleto(endereco: EnderecoResponse | null | undefined): string {
  if (!endereco) return 'Não informado'
  if (!endereco.logradouro) {
    return formatarEnderecoResumo(endereco)
  }
  const numero = endereco.numero && endereco.numero.trim() !== '' ? endereco.numero : 'S/N'
  const complemento = endereco.complemento ? ` (${endereco.complemento})` : ''
  const cep = endereco.cep ? ` — CEP ${endereco.cep.replace(/^(\d{5})(\d{3})$/, '$1-$2')}` : ''
  return `${endereco.logradouro}, ${numero}${complemento} — ${endereco.bairro ?? ''}, ${endereco.cidade ?? ''}/${endereco.estado ?? ''}${cep}`
}

