/** Transporte, frota e plantão (ADR-0178). */
export type TipoVeiculo = 'USA' | 'USB' | 'MOTOLANCIA' | 'AEROMEDICO' | 'AMBULANCIA_TRANSPORTE' | 'VAN_SANITARIA' | 'MICRO_ONIBUS' | 'CARRO_ADMINISTRATIVO';
export type SituacaoVeiculo = 'DISPONIVEL' | 'EM_SERVICO' | 'MANUTENCAO' | 'INATIVO';

/** Veículos nas emergências (ADR-0192): o pedido da coordenação, a cessão pela frota e a devolução. */
export interface CessaoVeiculoDto {
  uuid: string;
  mobilizacaoId: string;
  emergenciaId: string;
  emergenciaNome: string;
  veiculoId: string;
  prefixo: string;
  placa: string;
  tipo: TipoVeiculo;
  unidadeNome: string;
  cedidaEm: string;
  devolvidaEm?: string | null;
  kmInicio?: number | null;
  kmFim?: number | null;
  observacao?: string | null;
}

export interface PedidoVeiculoEmergenciaDto {
  mobilizacaoId: string;
  emergenciaId: string;
  emergenciaNome: string;
  destinoId: string;
  destinoNome: string;
  recurso: string;
  quantidade?: number | null;
  responsavel?: string | null;
  inicio: string;
  fim?: string | null;
  situacao: 'SOLICITADA' | 'EM_ANDAMENTO' | 'CONCLUIDA' | 'CANCELADA';
  cessoes: CessaoVeiculoDto[];
  faltam?: number | null;
}

export interface CessaoFeitaDto {
  cessao: CessaoVeiculoDto;
  avisos: string[];
}

export interface FrotaDaEmergenciaDto {
  cedidos: CessaoVeiculoDto[];
  unidadesAfetadas: { veiculoId: string; prefixo: string; tipo: TipoVeiculo; unidadeNome: string; situacao: SituacaoVeiculo; cedidoA?: string | null }[];
  disponiveisPorTipo: { tipo: TipoVeiculo; quantidade: number }[];
}
export type FuncaoTripulante = 'CONDUTOR' | 'TECNICO_ENFERMAGEM' | 'ENFERMEIRO' | 'MEDICO';

export const TIPOS_VEICULO: Record<TipoVeiculo, string> = {
  USA: 'USA · suporte avançado',
  USB: 'USB · suporte básico',
  MOTOLANCIA: 'Motolância',
  AEROMEDICO: 'Aeromédico',
  AMBULANCIA_TRANSPORTE: 'Ambulância de transporte',
  VAN_SANITARIA: 'Van sanitária',
  MICRO_ONIBUS: 'Micro-ônibus',
  CARRO_ADMINISTRATIVO: 'Carro administrativo',
};

export const SITUACOES_VEICULO: Record<SituacaoVeiculo, { rotulo: string; classe: string }> = {
  DISPONIVEL: { rotulo: 'Disponível', classe: 'ok' },
  EM_SERVICO: { rotulo: 'Em serviço', classe: 'purple' },
  MANUTENCAO: { rotulo: 'Em manutenção', classe: 'warn' },
  INATIVO: { rotulo: 'Inativo', classe: 'muted' },
};

export const FUNCOES_TRIPULANTE: Record<FuncaoTripulante, string> = {
  CONDUTOR: 'Condutor',
  TECNICO_ENFERMAGEM: 'Técnico de enfermagem',
  ENFERMEIRO: 'Enfermeiro',
  MEDICO: 'Médico',
};

/** Composição mínima por tipo, para orientar quem monta o plantão (a API confere). */
export const COMPOSICAO_MINIMA: Partial<Record<TipoVeiculo, string>> = {
  USB: 'condutor e técnico de enfermagem',
  USA: 'condutor, enfermeiro e médico',
  AEROMEDICO: 'enfermeiro e médico',
  MOTOLANCIA: 'técnico de enfermagem ou enfermeiro',
};

export interface TripulanteDto {
  profissionalId: string;
  nome: string;
  matricula: string;
  funcao: FuncaoTripulante;
}

export interface PlantaoDto {
  uuid: string;
  veiculoId: string;
  prefixo: string;
  tipo: TipoVeiculo;
  inicio: string;
  fim: string;
  vigente: boolean;
  observacao?: string | null;
  tripulacao: TripulanteDto[];
}

export interface VeiculoDto {
  uuid: string;
  placa: string;
  prefixo: string;
  tipo: TipoVeiculo;
  doSamu: boolean;
  modelo?: string | null;
  ano?: number | null;
  lugares?: number | null;
  maca: boolean;
  cadeiraDeRodas: boolean;
  quilometragem?: number | null;
  unidadeId: string;
  unidadeNome: string;
  situacao: SituacaoVeiculo;
  situacaoDesde?: string | null;
  observacao?: string | null;
  plantaoVigente?: PlantaoDto | null;
  historico?: { situacao: SituacaoVeiculo; inicio: string; motivo?: string | null }[] | null;
  plantoes?: PlantaoDto[] | null;
}

export interface VeiculoRequestDto {
  placa: string;
  prefixo: string;
  tipo: TipoVeiculo;
  modelo?: string | null;
  ano?: number | null;
  lugares?: number | null;
  maca: boolean;
  cadeiraDeRodas: boolean;
  quilometragem?: number | null;
  unidadeId: string;
  observacao?: string | null;
}

export interface ViaturaDisponivelDto {
  veiculoId: string;
  prefixo: string;
  tipo: TipoVeiculo;
  unidadeNome: string;
  plantaoAte: string;
}

// ── Transporte eletivo e TFD (ADR-0190) ──

export type MotivoTransporte = 'CONSULTA' | 'EXAME' | 'HEMODIALISE' | 'TERAPIA_SERIADA' | 'ALTA_HOSPITALAR' | 'TFD' | 'OUTRO';
export type TrechoTransporte = 'IDA' | 'VOLTA' | 'IDA_E_VOLTA';
export type SituacaoSolicitacaoTransporte = 'SOLICITADA' | 'AGUARDANDO_AUTORIZACAO' | 'AUTORIZADA' | 'PROGRAMADA' | 'REALIZADA' | 'FALTOU' | 'NEGADA' | 'CANCELADA';
export type SituacaoViagem = 'PROGRAMADA' | 'EM_ANDAMENTO' | 'CONCLUIDA' | 'CANCELADA';
export type SituacaoEmbarque = 'PREVISTO' | 'EMBARCOU' | 'FALTOU';

export const MOTIVOS_TRANSPORTE: Record<MotivoTransporte, string> = {
  CONSULTA: 'Consulta',
  EXAME: 'Exame',
  HEMODIALISE: 'Hemodiálise',
  TERAPIA_SERIADA: 'Terapia seriada',
  ALTA_HOSPITALAR: 'Alta hospitalar',
  TFD: 'TFD',
  OUTRO: 'Outro',
};

export const TRECHOS_TRANSPORTE: Record<TrechoTransporte, string> = { IDA: 'Ida', VOLTA: 'Volta', IDA_E_VOLTA: 'Ida e volta' };

export const SITUACOES_SOLICITACAO_TRANSPORTE: Record<SituacaoSolicitacaoTransporte, { rotulo: string; classe: string }> = {
  SOLICITADA: { rotulo: 'Na fila', classe: 'info' },
  AGUARDANDO_AUTORIZACAO: { rotulo: 'Aguardando a comissão', classe: 'warn' },
  AUTORIZADA: { rotulo: 'Autorizada', classe: 'info' },
  PROGRAMADA: { rotulo: 'Programada', classe: 'purple' },
  REALIZADA: { rotulo: 'Realizada', classe: 'ok' },
  FALTOU: { rotulo: 'Faltou', classe: 'err' },
  NEGADA: { rotulo: 'Negada', classe: 'err' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

export const SITUACOES_VIAGEM: Record<SituacaoViagem, { rotulo: string; classe: string }> = {
  PROGRAMADA: { rotulo: 'Programada', classe: 'info' },
  EM_ANDAMENTO: { rotulo: 'Em andamento', classe: 'purple' },
  CONCLUIDA: { rotulo: 'Concluída', classe: 'ok' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

export const DIAS_DA_SEMANA: [string, string][] = [
  ['MONDAY', 'Seg'], ['TUESDAY', 'Ter'], ['WEDNESDAY', 'Qua'], ['THURSDAY', 'Qui'], ['FRIDAY', 'Sex'], ['SATURDAY', 'Sáb'], ['SUNDAY', 'Dom'],
];

export interface SolicitacaoTransporteDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  pacienteCns?: string | null;
  unidadeId: string;
  unidadeNome: string;
  motivo: MotivoTransporte;
  agendamentoId?: string | null;
  solicitacaoRegulacaoId?: string | null;
  internacaoId?: string | null;
  dataHora: string;
  enderecoEmbarque: string;
  destinoUnidadeId?: string | null;
  destino: string;
  destinoMunicipio?: string | null;
  trecho: TrechoTransporte;
  maca: boolean;
  cadeiraDeRodas: boolean;
  oxigenio: boolean;
  acompanhanteNome?: string | null;
  acompanhanteDocumento?: string | null;
  lugares: number;
  cid?: string | null;
  justificativa?: string | null;
  profissionalNome?: string | null;
  situacao: SituacaoSolicitacaoTransporte;
  motivoSituacao?: string | null;
  recorrenciaId?: string | null;
  observacao?: string | null;
  solicitadoEm: string;
  viagemId?: string | null;
}

export interface SolicitacaoTransporteRequestDto {
  pacienteId: string;
  unidadeId: string;
  motivo: MotivoTransporte;
  trecho: TrechoTransporte;
  agendamentoId?: string | null;
  solicitacaoRegulacaoId?: string | null;
  internacaoId?: string | null;
  dataHora?: string | null;
  enderecoEmbarque?: string | null;
  destinoUnidadeId?: string | null;
  destino?: string | null;
  destinoMunicipio?: string | null;
  maca: boolean;
  cadeiraDeRodas: boolean;
  oxigenio: boolean;
  acompanhanteNome?: string | null;
  acompanhanteDocumento?: string | null;
  cid?: string | null;
  justificativa?: string | null;
  profissionalMatricula?: string | null;
  observacao?: string | null;
  diasDaSemana?: string[] | null;
  recorrenteAte?: string | null;
}

export interface EmbarqueDto {
  solicitacaoId: string;
  pacienteId: string;
  pacienteNome: string;
  motivo: MotivoTransporte;
  dataHora: string;
  enderecoEmbarque: string;
  destino: string;
  trecho: TrechoTransporte;
  maca: boolean;
  cadeiraDeRodas: boolean;
  oxigenio: boolean;
  acompanhanteNome?: string | null;
  situacao: SituacaoEmbarque;
}

export interface ViagemDto {
  uuid: string;
  veiculoId: string;
  prefixo: string;
  tipo: TipoVeiculo;
  lugaresVeiculo?: number | null;
  unidadeId: string;
  unidadeNome: string;
  condutorId: string;
  condutorNome: string;
  saidaPrevista: string;
  retornoPrevisto: string;
  destino: string;
  situacao: SituacaoViagem;
  lugaresOcupados: number;
  kmSaida?: number | null;
  kmChegada?: number | null;
  kmPercorridos?: number | null;
  saidaEm?: string | null;
  chegadaEm?: string | null;
  motivoCancelamento?: string | null;
  observacao?: string | null;
  embarques: EmbarqueDto[];
}

export interface QuilometragemDto {
  veiculoId: string;
  prefixo: string;
  tipo: TipoVeiculo;
  unidadeNome: string;
  viagens: number;
  pacientes: number;
  km: number;
}
