/** Gestão da Rede: oferta assistencial e cotas da PPI (ADR-0179). */
export interface CotaPpiDto {
  uuid: string;
  beneficiariaId: string;
  beneficiariaNome: string;
  beneficiariaTipo?: string | null;
  quantidade: number;
  utilizado: number;
  saldo: number;
}

export interface OfertaAssistencialDto {
  uuid: string;
  competencia: string;
  executanteId: string;
  executanteNome: string;
  procedimentoId: string;
  procedimentoNome: string;
  quantidade: number;
  pactuado: number;
  semPactuar: number;
  utilizado: number;
  extracotas: number;
  observacao?: string | null;
  cotas: CotaPpiDto[];
}

export interface PainelPpiDto {
  competencia: string;
  ofertado: number;
  pactuado: number;
  utilizado: number;
  extracotas: number;
  ofertas: OfertaAssistencialDto[];
}

/** O que a autorização vai encontrar: com oferta cadastrada, a cota que vale e o saldo. */
export interface SaldoPpiDto {
  controlada: boolean;
  ofertaId?: string | null;
  cotaId?: string | null;
  beneficiariaNome?: string | null;
  cota?: number | null;
  utilizado?: number | null;
  saldo?: number | null;
}

export interface CopiaPpiDto {
  origem: string;
  destino: string;
  ofertasCopiadas: number;
  cotasCopiadas: number;
  ofertasJaExistentes: number;
}
