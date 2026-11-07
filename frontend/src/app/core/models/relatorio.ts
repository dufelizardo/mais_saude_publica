/** Item do catálogo de relatórios (ADR-0167). */
export interface RelatorioCatalogoDto {
  codigo: string;
  nome: string;
  categoria: string;
  descricao: string;
}

/** Um relatório pronto: os números já vêm como texto, no formato brasileiro. */
export interface RelatorioDto {
  codigo: string;
  nome: string;
  categoria: string;
  de: string;
  ate: string;
  abrangencia: string;
  resumo: { rotulo: string; valor: string }[];
  secoes: { titulo: string; colunas: string[]; linhas: string[][] }[];
  notas: string[];
}

export interface FiltroRelatorio {
  de?: string;
  ate?: string;
  unidadeId?: string;
}
