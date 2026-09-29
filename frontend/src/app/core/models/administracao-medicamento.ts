/** Enfermagem (#8) — checagem de medicamento prescrito, ligada à Farmácia (ADR-0064). */

export type SituacaoAdministracao = 'ADMINISTRADO' | 'NAO_ADMINISTRADO';

export type ViaAdministracao =
  | 'ORAL'
  | 'SUBLINGUAL'
  | 'INTRAMUSCULAR'
  | 'INTRAVENOSA'
  | 'SUBCUTANEA'
  | 'TOPICA'
  | 'INALATORIA'
  | 'RETAL'
  | 'OUTRA';

export type MotivoNaoAdministracao = 'RECUSA_DO_PACIENTE' | 'PACIENTE_AUSENTE' | 'MEDICAMENTO_EM_FALTA' | 'SUSPENSO_PELO_MEDICO' | 'OUTRO';

export interface AdministracaoMedicamentoRequestDto {
  atendimentoId: string;
  consultaId: string;
  medicamentoId: string;
  situacao: SituacaoAdministracao;
  loteId?: string;
  dose?: string;
  via?: ViaAdministracao;
  quantidade?: number;
  motivoNaoAdministracao?: MotivoNaoAdministracao;
  observacao?: string;
  dataHora: string;
  profissionalMatricula: string;
}

export interface AdministracaoMedicamentoResponseDto {
  uuid: string;
  atendimentoUuid: string;
  consultaUuid: string;
  medicamentoUuid: string;
  medicamentoNome: string;
  situacao: SituacaoAdministracao;
  loteUuid?: string | null;
  numeroLote?: string | null;
  dose?: string | null;
  via?: ViaAdministracao | null;
  quantidade?: number | null;
  motivoNaoAdministracao?: MotivoNaoAdministracao | null;
  observacao?: string | null;
  dataHora: string;
  profissionalMatricula: string;
  profissionalNome: string;
  retificacaoDeUuid?: string | null;
  motivoRetificacao?: string | null;
  registradoEm?: string | null;
  registradoPorCpf?: string | null;
  retificado?: boolean;
  retificadoPorUuid?: string | null;
}
