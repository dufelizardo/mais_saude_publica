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
