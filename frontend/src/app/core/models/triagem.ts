export type ClassificacaoRisco = 'AZUL' | 'VERDE' | 'AMARELO' | 'LARANJA' | 'VERMELHO';

export interface TriagemRequestDto {
  atendimentoId: string;
  profissionalMatricula: string;
  dataHora: string;
  pressaoArterial?: string;
  temperatura?: number;
  saturacaoOxigenio?: number;
  frequenciaCardiaca?: number;
  peso?: number;
  classificacaoRisco: ClassificacaoRisco;
  observacoes?: string;
}

export interface TriagemResponseDto {
  uuid: string;
  atendimentoUuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  dataHora: string;
  pressaoArterial?: string;
  temperatura?: number;
  saturacaoOxigenio?: number;
  frequenciaCardiaca?: number;
  peso?: number;
  classificacaoRisco: ClassificacaoRisco;
  observacoes?: string;
  /** Retificação (ADR-0062): versão que esta corrige, motivo, e se outra versão já corrige esta. */
  retificacaoDeUuid?: string | null;
  motivoRetificacao?: string | null;
  registradoEm?: string | null;
  registradoPorCpf?: string | null;
  retificado?: boolean;
  retificadoPorUuid?: string | null;
}
