export interface ResponsabilidadeAdministrativaRequestDto {
  matriculaProfissional: string;
  setorId: string;
  tipo: string;
  descricao?: string;
  dataInicio: string;
}

export interface ResponsabilidadeAdministrativaResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  setorUuid: string;
  setorNome: string;
  tipo: string;
  descricao?: string;
  dataInicio: string;
  dataFim?: string;
}
