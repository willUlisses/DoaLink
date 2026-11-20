export const TIPO_USUARIO = {
  DOADOR: 'DOADOR',
  TRANSPORTADOR: 'TRANSPORTADOR',
  RECEPTOR: 'RECEPTOR',
} as const
export type TipoUsuario = (typeof TIPO_USUARIO)[keyof typeof TIPO_USUARIO]

export const TIPO_PESSOA = {
  FISICA: 'FISICA',
  JURIDICA: 'JURIDICA',
} as const
export type TipoPessoa = (typeof TIPO_PESSOA)[keyof typeof TIPO_PESSOA]

export const TIPO_ITEM_DOACAO = {
  PERECIVEL: 'PERECIVEL',
  NAO_PERECIVEL: 'NAO_PERECIVEL',
  HIGIENE: 'HIGIENE',
  ROUPAS_E_TEXTIL: 'ROUPAS_E_TEXTIL',
  MEDICAMENTOS: 'MEDICAMENTOS',
  ELETRODOMESTICOS: 'ELETRODOMESTICOS',
} as const
export type TipoItemDoacao = (typeof TIPO_ITEM_DOACAO)[keyof typeof TIPO_ITEM_DOACAO]

export const TIPO_UNIDADE = {
  KG: 'KG',
  T: 'T',
  G: 'G',
  ML: 'ML',
  CAIXAS: 'CAIXAS',
  CESTAS_BASICAS: 'CESTAS_BASICAS',
  SEMUNIDADE: 'SEMUNIDADE',
} as const
export type TipoUnidade = (typeof TIPO_UNIDADE)[keyof typeof TIPO_UNIDADE]

export const TIPO_VEICULO = {
  MOTO: 'MOTO',
  CARRO_PASSEIO: 'CARRO_PASSEIO',
  UTILITARIO_LEVE: 'UTILITARIO_LEVE',
  CAMINHONETE: 'CAMINHONETE',
  CAMINHAO_PEQUENO: 'CAMINHAO_PEQUENO',
  CAMINHAO_MEDIO: 'CAMINHAO_MEDIO',
  CAMINHAO_GRANDE: 'CAMINHAO_GRANDE',
  OUTRO: 'OUTRO',
} as const
export type TipoVeiculo = (typeof TIPO_VEICULO)[keyof typeof TIPO_VEICULO]

export const SITUACAO_OFERTA = {
  DISPONIVEL: 'DISPONIVEL',
  RESERVADA: 'RESERVADA',
  CONCLUIDA: 'CONCLUIDA',
  CANCELADA: 'CANCELADA',
} as const
export type SituacaoOferta = (typeof SITUACAO_OFERTA)[keyof typeof SITUACAO_OFERTA]

export const SITUACAO_NECESSIDADE = {
  ABERTA: 'ABERTA',
  ATENDIDA: 'ATENDIDA',
  CANCELADA: 'CANCELADA',
} as const
export type SituacaoNecessidade = (typeof SITUACAO_NECESSIDADE)[keyof typeof SITUACAO_NECESSIDADE]

export const SITUACAO_CAMPANHA = {
  ATIVA: 'ATIVA',
  ENCERRADA: 'ENCERRADA',
  CANCELADA: 'CANCELADA',
} as const
export type SituacaoCampanha = (typeof SITUACAO_CAMPANHA)[keyof typeof SITUACAO_CAMPANHA]

export const SITUACAO_DISPONIBILIDADE = {
  DISPONIVEL: 'DISPONIVEL',
  RESERVADA: 'RESERVADA',
  CONCLUIDA: 'CONCLUIDA',
  CANCELADA: 'CANCELADA',
} as const
export type SituacaoDisponibilidade =
  (typeof SITUACAO_DISPONIBILIDADE)[keyof typeof SITUACAO_DISPONIBILIDADE]

export const SITUACAO_RESERVA_ENTREGA = {
  AGUARDANDO_TRANSPORTE: 'AGUARDANDO_TRANSPORTE',
  AGUARDANDO_RETIRADA: 'AGUARDANDO_RETIRADA',
  TRANSPORTE_VINCULADO: 'TRANSPORTE_VINCULADO',
  CONCLUIDA: 'CONCLUIDA',
  CANCELADA: 'CANCELADA',
} as const
export type SituacaoReservaEntrega =
  (typeof SITUACAO_RESERVA_ENTREGA)[keyof typeof SITUACAO_RESERVA_ENTREGA]

export const UFS_BRASIL = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO',
  'MA', 'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI',
  'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
] as const

