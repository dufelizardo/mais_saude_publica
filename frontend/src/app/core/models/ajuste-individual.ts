export type MotivoAjusteIndividual = 'GRATIFICACAO_PESSOAL' | 'EQUIPARACAO_JUDICIAL';

export interface AjusteIndividualRequestDto {
  matriculaProfissional: string;
  valor: number;
  dataInicio: string;
  dataFim?: string;
  motivo: MotivoAjusteIndividual;
  referencia?: string;
}

export interface AjusteIndividualResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  valor: number;
  dataInicio: string;
  dataFim?: string;
  motivo: MotivoAjusteIndividual;
  referencia?: string;
}
