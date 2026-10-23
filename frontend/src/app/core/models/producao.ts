/** Produção assistencial (BI1, ADR-0143): só agregados, nunca o paciente. */

export interface ContagemProducaoDto {
  chave: string;
  rotulo: string;
  quantidade: number;
}

export interface AgendaProducaoDto {
  marcados: number;
  realizados: number;
  faltas: number;
  cancelados: number;
  encaixes: number;
  absenteismo?: number | null;
}

export interface ProducaoAssistencialDto {
  de: string;
  ate: string;
  anteriorDe: string;
  anteriorAte: string;
  unidadeId?: string | null;
  unidadeNome?: string | null;
  abrangencia: string;
  atendimentos: number;
  atendimentosAnterior: number;
  variacao?: number | null;
  mediaDiaria: number;
  porTipo: ContagemProducaoDto[];
  porEspecialidade: ContagemProducaoDto[];
  porCategoria: ContagemProducaoDto[];
  porFaixaEtaria: ContagemProducaoDto[];
  porSexo: ContagemProducaoDto[];
  porHora: number[];
  agenda: AgendaProducaoDto;
  agendaAnterior: AgendaProducaoDto;
}

export type GranularidadeSerie = 'DIA' | 'MES';

export interface SerieProducaoDto {
  granularidade: GranularidadeSerie;
  unidadeId?: string | null;
  pontos: { periodo: string; atendimentos: number; faltas: number }[];
}

export interface ProducaoUnidadeDto {
  unidadeId: string;
  unidadeNome: string;
  tipo: string;
  atendimentos: number;
  atendimentosAnterior: number;
  variacao?: number | null;
}

export interface FiltroProducao {
  de?: string;
  ate?: string;
  unidadeId?: string;
}

// ── Situação operacional (BI2, ADR-0144) ──

export interface AlertaSituacaoDto {
  tipo: 'FILA' | 'ESPERA' | 'AGENDA' | 'ESCALA';
  gravidade: 'ALTA' | 'MEDIA';
  titulo: string;
  detalhe: string;
  unidadeId?: string | null;
  unidadeNome?: string | null;
  rota: string;
}

export interface SituacaoAgoraDto {
  atualizadoEm: string;
  unidadeId?: string | null;
  abrangencia: string;
  emAtendimento: number;
  aguardandoTriagem: number;
  aguardandoConsulta: number;
  maiorEsperaMinutos?: number | null;
  alertas: AlertaSituacaoDto[];
}

export interface EstatisticaEsperaDto {
  amostra: number;
  mediaMinutos?: number | null;
  medianaMinutos?: number | null;
  p90Minutos?: number | null;
}

export interface EsperaAtendimentoDto {
  de: string;
  ate: string;
  unidadeId?: string | null;
  abrangencia: string;
  comChegada: number;
  descartados: number;
  primeiroContato: EstatisticaEsperaDto;
  consulta: EstatisticaEsperaDto;
}

export interface OcupacaoAgendaDto {
  de: string;
  ate: string;
  unidadeId?: string | null;
  abrangencia: string;
  ofertadas: number;
  ocupadas: number;
  percentual?: number | null;
  especialidades: { especialidade: string; ofertadas: number; ocupadas: number; percentual?: number | null }[];
}

export interface OcorrenciaEscalaDto {
  turnoId: string;
  unidadeId: string;
  unidadeNome: string;
  profissionalNome?: string | null;
  funcao?: string | null;
  tipoTurno?: string | null;
  inicio: string;
  fim: string;
  situacao: 'SEM_COBERTURA' | 'AFASTADO';
  motivo: string;
}
