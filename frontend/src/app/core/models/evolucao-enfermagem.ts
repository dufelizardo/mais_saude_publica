export interface EvolucaoEnfermagemResponseDto {
  uuid: string;
  atendimentoUuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  dataHora: string;
  descricao: string;
  /** Retificação (ADR-0062): versão que esta corrige, motivo, e se outra versão já corrige esta. */
  retificacaoDeUuid?: string | null;
  motivoRetificacao?: string | null;
  registradoEm?: string | null;
  registradoPorCpf?: string | null;
  retificado?: boolean;
  retificadoPorUuid?: string | null;
}
