import type {
  SituacaoCampanha,
  SituacaoDisponibilidade,
  SituacaoNecessidade,
  SituacaoOferta,
  SituacaoReservaEntrega,
  TipoItemDoacao,
  TipoUnidade,
  TipoUsuario,
  TipoVeiculo,
} from '../types/enums.ts'

export const LABELS_TIPO_USUARIO: Record<TipoUsuario, string> = {
  DOADOR: 'Doador',
  TRANSPORTADOR: 'Transportador',
  RECEPTOR: 'Receptor (ONG)',
}

export const DESCRICOES_TIPO_USUARIO: Record<TipoUsuario, string> = {
  DOADOR: 'Publica ofertas de itens para doação e acompanha reservas.',
  TRANSPORTADOR: 'Disponibiliza veículo e capacidade para assumir entregas.',
  RECEPTOR: 'ONG com CNPJ que publica necessidades, campanhas e reserva ofertas.',
}

export const LABELS_CATEGORIA_ITEM: Record<TipoItemDoacao, string> = {
  PERECIVEL: 'Perecível',
  NAO_PERECIVEL: 'Não perecível',
  HIGIENE: 'Higiene',
  ROUPAS_E_TEXTIL: 'Roupas e têxtil',
  MEDICAMENTOS: 'Medicamentos',
  ELETRODOMESTICOS: 'Eletrodomésticos',
}

export const LABELS_UNIDADE: Record<TipoUnidade, string> = {
  KG: 'kg (Quilogramas)',
  T: 't (Toneladas)',
  G: 'g (Gramas)',
  ML: 'ml (Mililitros)',
  CAIXAS: 'Caixas',
  CESTAS_BASICAS: 'Cestas básicas',
  SEMUNIDADE: 'Unidades',
}

export const SIGLAS_UNIDADE: Record<TipoUnidade, string> = {
  KG: 'kg',
  T: 't',
  G: 'g',
  ML: 'ml',
  CAIXAS: 'cx',
  CESTAS_BASICAS: 'cestas',
  SEMUNIDADE: 'un',
}

export const LABELS_VEICULO: Record<TipoVeiculo, string> = {
  MOTO: 'Moto',
  CARRO_PASSEIO: 'Carro de passeio',
  UTILITARIO_LEVE: 'Utilitário leve (furgão/van)',
  CAMINHONETE: 'Caminhonete / Pickup',
  CAMINHAO_PEQUENO: 'Caminhão pequeno (VUC / 3/4)',
  CAMINHAO_MEDIO: 'Caminhão médio (toco / truck)',
  CAMINHAO_GRANDE: 'Caminhão grande (carreta / bitrem)',
  OUTRO: 'Outro',
}

export const LABELS_SITUACAO_OFERTA: Record<SituacaoOferta, string> = {
  DISPONIVEL: 'Disponível',
  RESERVADA: 'Reservada',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada',
}

export const LABELS_SITUACAO_NECESSIDADE: Record<SituacaoNecessidade, string> = {
  ABERTA: 'Aberta',
  ATENDIDA: 'Atendida',
  CANCELADA: 'Cancelada',
}

export const LABELS_SITUACAO_CAMPANHA: Record<SituacaoCampanha, string> = {
  ATIVA: 'Ativa',
  ENCERRADA: 'Encerrada',
  CANCELADA: 'Cancelada',
}

export const LABELS_SITUACAO_DISPONIBILIDADE: Record<SituacaoDisponibilidade, string> = {
  DISPONIVEL: 'Disponível',
  RESERVADA: 'Reservada',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada',
}

export const LABELS_SITUACAO_RESERVA: Record<SituacaoReservaEntrega, string> = {
  AGUARDANDO_TRANSPORTE: 'Aguardando transporte',
  AGUARDANDO_RETIRADA: 'Aguardando retirada',
  TRANSPORTE_VINCULADO: 'Transporte vinculado',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada',
}

