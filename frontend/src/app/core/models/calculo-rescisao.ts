export type TipoDesligamento =
  | 'SEM_JUSTA_CAUSA'
  | 'COM_JUSTA_CAUSA'
  | 'PEDIDO_DEMISSAO'
  | 'TERMINO_CONTRATO'
  | 'APOSENTADORIA'
  | 'FALECIMENTO';

export interface CalculoRescisaoRequestDto {
  matriculaProfissional: string;
  tipoDesligamento: TipoDesligamento;
  avisoPrevio: number;
  feriasVencidas: number;
  feriasProporcionais: number;
  decimoTerceiroProporcional: number;
  multaFgts: number;
  total: number;
  documentoTrctUrl?: string;
}

export interface CalculoRescisaoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  tipoDesligamento: TipoDesligamento;
  avisoPrevio: number;
  feriasVencidas: number;
  feriasProporcionais: number;
  decimoTerceiroProporcional: number;
  multaFgts: number;
  total: number;
  documentoTrctUrl?: string;
}
