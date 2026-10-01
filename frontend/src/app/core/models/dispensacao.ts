/** Farmácia (#9) — dispensação, imutável (ADR-0051). */

export interface DispensacaoRequestDto {
  loteId: string;
  pacienteId: string;
  profissionalMatricula: string;
  consultaId?: string;
  quantidade: number;
  dataHora: string;
}

export interface DispensacaoResponseDto {
  uuid: string;
  loteUuid: string;
  loteNumeroLote: string;
  medicamentoNome: string;
  pacienteUuid: string;
  pacienteNome: string;
  pacienteCpf?: string;
  profissionalMatricula: string;
  profissionalNome: string;
  consultaUuid?: string | null;
  quantidade: number;
  dataHora: string;
}
