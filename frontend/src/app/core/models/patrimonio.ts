/** Patrimônio, fatia PM1 (ADR-0215): o bem tombado, o termo de responsabilidade e a transferência entre unidades. */

export type ClasseBem = 'EQUIPAMENTO_MEDICO_HOSPITALAR' | 'INFORMATICA' | 'MOBILIARIO' | 'VEICULO' | 'ELETRODOMESTICO' | 'OUTRO';
export type SituacaoBem = 'A_TOMBAR' | 'EM_USO' | 'OCIOSO' | 'EM_MANUTENCAO' | 'BAIXADO';
export type OrigemBem = 'COMPRA' | 'DOACAO' | 'CESSAO_COMODATO' | 'TRANSFERENCIA_ENTE';
export type SituacaoTransferenciaBem = 'PENDENTE' | 'ACEITA' | 'RECUSADA' | 'CANCELADA';

export const CLASSES_BEM: Record<ClasseBem, string> = {
  EQUIPAMENTO_MEDICO_HOSPITALAR: 'Equipamento médico-hospitalar',
  INFORMATICA: 'Informática',
  MOBILIARIO: 'Mobiliário',
  VEICULO: 'Veículo',
  ELETRODOMESTICO: 'Eletrodoméstico',
  OUTRO: 'Outro',
};

export const SITUACOES_BEM: Record<SituacaoBem, string> = {
  A_TOMBAR: 'A tombar',
  EM_USO: 'Em uso',
  OCIOSO: 'Ocioso',
  EM_MANUTENCAO: 'Em manutenção',
  BAIXADO: 'Baixado',
};

export const ORIGENS_BEM: Record<OrigemBem, string> = {
  COMPRA: 'Compra',
  DOACAO: 'Doação',
  CESSAO_COMODATO: 'Cessão ou comodato',
  TRANSFERENCIA_ENTE: 'Transferência de outro ente',
};

export const SITUACOES_TRANSFERENCIA: Record<SituacaoTransferenciaBem, string> = {
  PENDENTE: 'À espera do aceite',
  ACEITA: 'Aceita',
  RECUSADA: 'Recusada',
  CANCELADA: 'Cancelada',
};

export interface TransferenciaBemDto {
  uuid: string;
  bemId: string;
  numeroTombamento: string | null;
  bemDescricao: string;
  origemId: string;
  origemNome: string;
  destinoId: string;
  destinoNome: string;
  motivo: string;
  enviadaEm: string;
  situacao: SituacaoTransferenciaBem;
  resolvidaEm: string | null;
  motivoRecusa: string | null;
}

export interface BemDto {
  uuid: string;
  numeroTombamento: string | null;
  descricao: string;
  classe: ClasseBem | null;
  marca: string | null;
  modelo: string | null;
  numeroSerie: string | null;
  unidadeId: string;
  unidadeNome: string;
  setorId: string | null;
  setorNome: string | null;
  situacao: SituacaoBem;
  origem: OrigemBem;
  valorAquisicao: number | null;
  dataAquisicao: string | null;
  documento: string | null;
  fornecedorNome: string | null;
  tombadoEm: string | null;
  responsavelMatricula: string | null;
  responsavelNome: string | null;
  responsavelDesde: string | null;
  veiculoId: string | null;
  veiculoPlaca: string | null;
  transferenciaPendente: TransferenciaBemDto | null;
  observacao: string | null;
  /** Depreciação linear da classe, só para leitura (ADR-0217). */
  valorContabil: number | null;
}

export interface ResponsavelBemDto {
  uuid: string;
  matricula: string;
  nome: string;
  desde: string;
  ate: string | null;
}

export interface DetalheBemDto {
  bem: BemDto;
  responsaveis: ResponsavelBemDto[];
  transferencias: TransferenciaBemDto[];
  baixa: BaixaBemDto | null;
}

export interface BemRequest {
  descricao: string;
  classe: ClasseBem;
  marca: string | null;
  modelo: string | null;
  numeroSerie: string | null;
  unidadeId: string;
  setorId: string | null;
  origem: OrigemBem;
  valorAquisicao: number | null;
  dataAquisicao: string;
  documento: string | null;
  responsavelMatricula: string;
  observacao: string | null;
}

// ── Manutenção, fatia PM2 (ADR-0216) ──

export type TipoManutencao = 'CORRETIVA' | 'PREVENTIVA' | 'CALIBRACAO' | 'SEGURANCA_ELETRICA';
export type PrioridadeManutencao = 'BAIXA' | 'NORMAL' | 'ALTA' | 'URGENTE';
export type SituacaoOrdemServico = 'ABERTA' | 'EM_EXECUCAO' | 'CONCLUIDA' | 'CANCELADA';
export type ExecucaoManutencao = 'INTERNA' | 'CONTRATO';

export const TIPOS_MANUTENCAO: Record<TipoManutencao, string> = {
  CORRETIVA: 'Corretiva',
  PREVENTIVA: 'Preventiva',
  CALIBRACAO: 'Calibração',
  SEGURANCA_ELETRICA: 'Segurança elétrica',
};

export const PRIORIDADES_MANUTENCAO: Record<PrioridadeManutencao, string> = { BAIXA: 'Baixa', NORMAL: 'Normal', ALTA: 'Alta', URGENTE: 'Urgente' };

export const SITUACOES_ORDEM_SERVICO: Record<SituacaoOrdemServico, string> = {
  ABERTA: 'Aberta',
  EM_EXECUCAO: 'Em execução',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada',
};

export interface PlanoManutencaoDto {
  uuid: string;
  bemId: string;
  numeroTombamento: string | null;
  bemDescricao: string;
  unidadeNome: string;
  tipo: TipoManutencao;
  periodicidadeDias: number;
  ultimaExecucao: string | null;
  proximaEm: string;
  descricao: string | null;
  ativo: boolean;
  vencido: boolean;
}

export interface PecaOrdemServicoDto {
  uuid: string;
  materialId: string | null;
  descricao: string;
  quantidade: number;
  custoUnitario: number | null;
}

export interface OrdemServicoDto {
  uuid: string;
  numero: string;
  bemId: string;
  numeroTombamento: string | null;
  bemDescricao: string;
  unidadeId: string;
  unidadeNome: string;
  tipo: TipoManutencao;
  planoId: string | null;
  defeito: string | null;
  bemParado: boolean;
  paradoDesde: string | null;
  abertaEm: string;
  prioridade: PrioridadeManutencao | null;
  situacao: SituacaoOrdemServico;
  execucao: ExecucaoManutencao | null;
  instrumentoId: string | null;
  instrumentoNumero: string | null;
  tecnico: string | null;
  iniciadaEm: string | null;
  concluidaEm: string | null;
  servicoRealizado: string | null;
  custoMaoDeObra: number | null;
  custoPecas: number;
  custoTotal: number;
  diasParado: number | null;
  certificado: string | null;
  calibracaoValidaAte: string | null;
  motivoCancelamento: string | null;
  pecas: PecaOrdemServicoDto[];
}

export interface ManutencaoDoBemDto {
  planos: PlanoManutencaoDto[];
  ordens: OrdemServicoDto[];
  custoTotal: number;
  diasParado: number;
  calibracaoValidaAte: string | null;
  calibracaoVencida: boolean;
}

export interface ResumoManutencaoDto {
  abertas: number;
  emExecucao: number;
  paradas: number;
  planosVencendo: PlanoManutencaoDto[];
  calibracoesVencidas: PlanoManutencaoDto[];
  equipamentosParados: OrdemServicoDto[];
}

export interface ContratoServicoDto {
  uuid: string;
  numero: string;
  fornecedorNome: string;
  fim: string;
}

export interface ConclusaoOrdemServicoRequest {
  servicoRealizado: string;
  custoMaoDeObra: number | null;
  certificado: string | null;
  calibracaoValidaAte: string | null;
  pecas: { materialId: string | null; descricao: string | null; quantidade: number; custoUnitario: number | null }[];
}

// ── Inventário, baixa e painel, fatia PM3 (ADR-0217) ──

export type MotivoBaixaBem = 'INSERVIVEL' | 'EXTRAVIO' | 'FURTO_ROUBO' | 'DOACAO' | 'ALIENACAO' | 'DEVOLUCAO_CESSAO';
export type SituacaoBaixaBem = 'PENDENTE' | 'CONFIRMADA' | 'RECUSADA';
export type ResultadoConferencia = 'ENCONTRADO' | 'NAO_ENCONTRADO' | 'OUTRO_LOCAL';
export type ConservacaoBem = 'BOM' | 'REGULAR' | 'RUIM' | 'INSERVIVEL';
export type AcaoInventario = 'SETOR_CORRIGIDO' | 'TRANSFERENCIA' | 'BAIXA_SUGERIDA';

export const MOTIVOS_BAIXA: Record<MotivoBaixaBem, string> = {
  INSERVIVEL: 'Inservível',
  EXTRAVIO: 'Extravio',
  FURTO_ROUBO: 'Furto ou roubo',
  DOACAO: 'Doação a terceiro',
  ALIENACAO: 'Alienação',
  DEVOLUCAO_CESSAO: 'Devolução da cessão',
};
export const SITUACOES_BAIXA: Record<SituacaoBaixaBem, string> = { PENDENTE: 'À espera da confirmação', CONFIRMADA: 'Confirmada', RECUSADA: 'Recusada' };
export const RESULTADOS_CONFERENCIA: Record<ResultadoConferencia, string> = { ENCONTRADO: 'Encontrado', NAO_ENCONTRADO: 'Não encontrado', OUTRO_LOCAL: 'Em outro local' };
export const CONSERVACOES: Record<ConservacaoBem, string> = { BOM: 'Bom', REGULAR: 'Regular', RUIM: 'Ruim', INSERVIVEL: 'Inservível' };
export const ACOES_INVENTARIO: Record<AcaoInventario, string> = {
  SETOR_CORRIGIDO: 'Setor corrigido',
  TRANSFERENCIA: 'Transferência aberta',
  BAIXA_SUGERIDA: 'Baixa sugerida',
};

export interface BaixaBemDto {
  uuid: string;
  bemId: string;
  numeroTombamento: string | null;
  bemDescricao: string;
  unidadeNome: string;
  motivo: MotivoBaixaBem;
  processo: string;
  boletimOcorrencia: string | null;
  dataBaixa: string;
  observacao: string | null;
  valorAquisicao: number | null;
  valorContabil: number | null;
  situacao: SituacaoBaixaBem;
  resolvidaEm: string | null;
  motivoRecusa: string | null;
}

export interface ItemInventarioDto {
  uuid: string;
  bemId: string;
  numeroTombamento: string | null;
  bemDescricao: string;
  setorEsperadoNome: string | null;
  resultado: ResultadoConferencia | null;
  conservacao: ConservacaoBem | null;
  setorEncontradoNome: string | null;
  unidadeEncontradaNome: string | null;
  observacao: string | null;
  conferidoEm: string | null;
  acao: AcaoInventario | null;
}

export interface InventarioDto {
  uuid: string;
  numero: string;
  unidadeId: string;
  unidadeNome: string;
  situacao: 'ABERTO' | 'FECHADO';
  abertoEm: string;
  fechadoEm: string | null;
  observacao: string | null;
  membros: { matricula: string; nome: string }[];
  total: number;
  conferidos: number;
  naoEncontrados: number;
  outroLocal: number;
  inserviveis: number;
  itens: ItemInventarioDto[];
  achados: { uuid: string; descricao: string; local: string | null }[];
}

export interface PainelPatrimonioDto {
  bens: number;
  valorAquisicao: number;
  valorContabil: number;
  ociosos: number;
  aTombar: number;
  inventariosAbertos: number;
  baixasNoAno: number;
  baixasPendentes: BaixaBemDto[];
  porUnidade: { unidadeId: string; unidadeNome: string; bens: number; valorAquisicao: number; valorContabil: number; ultimoInventario: string | null;
    semInventarioHa12Meses: boolean }[];
  porClasse: { classe: ClasseBem; bens: number; valorAquisicao: number; valorContabil: number }[];
  manutencao: { bemId: string; numeroTombamento: string | null; descricao: string; unidadeNome: string; custo: number; diasParado: number }[];
}
