export interface TreinamentoRequestDto {
  nome: string;
  cargaHoraria?: number;
  validadeMeses?: number;
  obrigatorio?: boolean;
}

export interface TreinamentoResponseDto {
  uuid: string;
  nome: string;
  cargaHoraria?: number;
  validadeMeses?: number;
  obrigatorio: boolean;
}

export interface ParticipacaoTreinamentoRequestDto {
  matriculaProfissional: string;
  treinamentoId: string;
  dataConclusao: string;
  certificadoUrl?: string;
}

export interface ParticipacaoTreinamentoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  treinamentoNome: string;
  dataConclusao: string;
  dataValidade?: string;
  certificadoUrl?: string;
}
