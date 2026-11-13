export interface AcidenteTrabalhoRequestDto {
  matriculaProfissional: string;
  dataHora: string;
  descricao: string;
  catEmitida?: boolean;
  catUrl?: string;
  diasAfastamento?: number;
}

export interface AcidenteTrabalhoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  dataHora: string;
  descricao: string;
  catEmitida: boolean;
  catUrl?: string;
  diasAfastamento?: number;
  /** Notificado à vigilância (ADR-0207); só a marca, sem o conteúdo. */
  notificadoVigilancia?: boolean;
}
