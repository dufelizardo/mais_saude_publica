import { PublicoComunicacao, SituacaoConteudo } from './comunicacao';

/** Competência do catálogo (ADR-0173). */
export interface CompetenciaDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
}

/** Conteúdo educativo na versão em vigor. */
export interface ConteudoDoTreinamentoDto {
  grupoId: string;
  uuid: string;
  titulo: string;
  versao: number;
  situacao: SituacaoConteudo;
  publico: PublicoComunicacao;
  validade: string;
  vencido: boolean;
}

export interface EducacaoDoTreinamentoDto {
  treinamentoId: string;
  treinamentoNome: string;
  competencias: CompetenciaDto[];
  conteudos: ConteudoDoTreinamentoDto[];
}

export interface CompetenciaDoProfissionalDto {
  competenciaId: string;
  codigo: string;
  nome: string;
  treinamentoNome: string;
  dataConclusao: string;
  dataValidade?: string | null;
  vencida: boolean;
}

export interface UsoDoConteudoDto {
  grupoId: string;
  treinamentos: string[];
  /** C3 (ADR-0195): em quantas ações educativas e quantas pessoas alcançou. */
  acoesEducativas: number;
  pessoasAlcancadas: number;
}
