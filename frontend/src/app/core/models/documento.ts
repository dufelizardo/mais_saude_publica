/** Tipo de documento do catálogo (ADR-0172). */
export interface TipoDocumentoDto {
  uuid: string;
  codigo: string;
  nome: string;
  ativo: boolean;
}

export interface VersaoDocumentoDto {
  uuid: string;
  numero: number;
  nomeArquivo: string;
  tipoConteudo: string;
  tamanho: number;
  enviadoEm: string;
}

export interface DocumentoDto {
  uuid: string;
  tipoNome: string;
  titulo: string;
  dataDocumento: string;
  unidadeId: string;
  unidadeNome: string;
  retiradoEm?: string | null;
  motivoRetirada?: string | null;
  /** Da mais recente para a mais antiga. */
  versoes: VersaoDocumentoDto[];
}

export interface DocumentosDoPacienteDto {
  /** Pode abrir os arquivos. Quem só anexa vê o que anexou, sem abrir. */
  podeAbrir: boolean;
  documentos: DocumentoDto[];
  retirados: DocumentoDto[];
}
