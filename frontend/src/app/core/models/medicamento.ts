/** Farmácia (#9) — catálogo de medicamentos (ADR-0049). */

export interface MedicamentoRequestDto {
  nome: string;
  principioAtivo?: string;
  apresentacao?: string;
  codigo?: string;
  ativo: boolean;
}

export interface MedicamentoResponseDto {
  uuid: string;
  nome: string;
  principioAtivo?: string;
  apresentacao?: string;
  codigo?: string;
  ativo: boolean;
}
