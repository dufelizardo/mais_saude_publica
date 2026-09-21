export type StatusCandidato = 'INSCRITO' | 'TRIAGEM' | 'ENTREVISTA' | 'APROVADO' | 'REPROVADO';

export interface CandidatoRequestDto {
  vagaId: string;
  nome: string;
  cpf: string;
  curriculoUrl?: string;
  status: StatusCandidato;
}

export interface CandidatoResponseDto {
  uuid: string;
  vagaId: string;
  nome: string;
  cpf: string;
  curriculoUrl?: string;
  status: StatusCandidato;
}
