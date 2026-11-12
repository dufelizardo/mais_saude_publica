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

// ── Redes temáticas e referência (ADR-0197) ──

export type RedeTematica = 'URGENCIA_EMERGENCIA' | 'CEGONHA_ALYNE' | 'PESSOA_COM_DEFICIENCIA' | 'DOENCAS_CRONICAS';

export const REDES_TEMATICAS: Record<RedeTematica, string> = {
  URGENCIA_EMERGENCIA: 'Rede de Atenção às Urgências e Emergências',
  CEGONHA_ALYNE: 'Rede Cegonha / Rede Alyne',
  PESSOA_COM_DEFICIENCIA: 'Rede de Cuidados à Pessoa com Deficiência',
  DOENCAS_CRONICAS: 'Rede de Atenção às Pessoas com Doenças Crônicas',
};

export interface PainelRedeTematicaDto {
  rede: RedeTematica;
  nome: string;
  abrangencia: string;
  componentes: { codigo: string; nome: string; pontos: { unidadeId: string; unidadeNome: string; unidadeTipo?: string | null; itens: string[] }[] }[];
  lacunas: string[];
}

export interface ReferenciaRedeDto {
  uuid: string;
  origemId: string;
  origemNome: string;
  origemTipo?: string | null;
  procedimentoId: string;
  procedimentoNome: string;
  executanteId: string;
  executanteNome: string;
  inicio: string;
  fim?: string | null;
  vigente: boolean;
  observacao?: string | null;
}

export interface PainelReferenciasDto {
  referencias: ReferenciaRedeDto[];
  indicadores: { procedimentoId: string; procedimentoNome: string; autorizacoes: number; foraDaReferencia: number; percentualFora: number }[];
}

export interface SugestaoReferenciaDto {
  referenciaId?: string | null;
  executanteId?: string | null;
  executanteNome?: string | null;
  origemNome?: string | null;
}
