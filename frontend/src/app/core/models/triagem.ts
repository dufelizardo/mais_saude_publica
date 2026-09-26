export type ClassificacaoRisco = 'AZUL' | 'VERDE' | 'AMARELO' | 'LARANJA' | 'VERMELHO';

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
}
