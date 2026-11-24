import type {
  SituacaoCampanha,
  SituacaoNecessidade,
  SituacaoOferta,
  SituacaoReservaEntrega,
  SituacaoTransportador,
  TipoEventoTransporte,
  TipoItemDoacao,
  TipoUnidade,
  TipoUsuario,
} from '../types/enums.ts'

export const LABELS_TIPO_USUARIO: Record<TipoUsuario, string> = {
  DOADOR: 'Doador',
  TRANSPORTADOR: 'Transportador',
  RECEPTOR: 'Receptor (ONG)',
}

export const DESCRICOES_TIPO_USUARIO: Record<TipoUsuario, string> = {
  DOADOR: 'Publica ofertas de itens para doação, autoriza retiradas e acompanha entregas.',
  TRANSPORTADOR:
    'Encontra ofertas aguardando transporte, assume entregas e confirma retirada e entrega com códigos de segurança.',
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

export const LABELS_SITUACAO_TRANSPORTADOR: Record<SituacaoTransportador, string> = {
  ATIVO: 'Conta ativa',
  SUSPENSO: 'Conta suspensa',
}

export const LABELS_SITUACAO_RESERVA: Record<SituacaoReservaEntrega, string> = {
  AGUARDANDO_TRANSPORTE: 'Aguardando transporte',
  AGUARDANDO_RETIRADA: 'Aguardando retirada',
  AGUARDANDO_AUTORIZACAO_DOADOR: 'Aguardando autorização do doador',
  AGUARDANDO_CONFIRMACAO_RETIRADA: 'Aguardando confirmação de retirada',
  EM_TRANSPORTE: 'Transporte em andamento',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada',
  EM_INVESTIGACAO: 'Em investigação',
}

export const LABELS_EVENTO_TRANSPORTE: Record<TipoEventoTransporte, string> = {
  TRANSPORTE_ACEITO: 'Transporte aceito pelo motorista',
  TRANSPORTE_AUTORIZADO_DOADOR: 'Início autorizado pelo doador',
  CODIGO_RETIRADA_GERADO: 'Código de retirada gerado',
  TENTATIVA_CODIGO_RETIRADA_INVALIDA: 'Tentativa inválida de código de retirada',
  CODIGO_RETIRADA_BLOQUEADO: 'Código de retirada bloqueado por excesso de tentativas',
  RETIRADA_CONFIRMADA: 'Retirada confirmada pelo transportador',
  CODIGO_ENTREGA_GERADO: 'Código de entrega gerado (validade de 24h)',
  TENTATIVA_CODIGO_ENTREGA_INVALIDA: 'Tentativa inválida de código de entrega',
  CODIGO_ENTREGA_BLOQUEADO: 'Código de entrega bloqueado por excesso de tentativas',
  ENTREGA_CONFIRMADA: 'Entrega confirmada pelo transportador',
  CODIGO_ENTREGA_EXPIRADO: 'Código de entrega expirado após 24 horas',
  TRANSPORTADOR_SUSPENSO: 'Conta do transportador suspensa',
  OCORRENCIA_REGISTRADA: 'Ocorrência registrada para investigação',
  TENTATIVA_USO_CODIGO_EXPIRADO: 'Tentativa de uso de código expirado',
}
