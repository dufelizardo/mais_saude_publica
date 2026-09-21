export type TipoLicenca = 'MATERNIDADE' | 'PATERNIDADE' | 'DOENCA' | 'ACIDENTE_DE_TRABALHO' | 'FALECIMENTO' | 'CASAMENTO' | 'OUTROS';
export type ResponsavelPagamentoLicenca = 'EMPRESA' | 'INSS' | 'MISTO';

export interface LicencaRequestDto {
  afastamentoId: string;
  tipoLegal: TipoLicenca;
  responsavelPagamento: ResponsavelPagamentoLicenca;
  documentoUrl?: string;
}

export interface LicencaResponseDto {
  uuid: string;
  afastamentoId: string;
  profissionalMatricula: string;
  profissionalNome: string;
  tipoLegal: TipoLicenca;
  responsavelPagamento: ResponsavelPagamentoLicenca;
  documentoUrl?: string;
}
