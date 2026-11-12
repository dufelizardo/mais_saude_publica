import { DocumentoDto } from './documento';

/** Vigilância Sanitária, fatia V3a (ADR-0199). */
export type GrauRiscoSanitario = 'BAIXO' | 'MEDIO' | 'ALTO';
export type CanalDenuncia = 'TELEFONE' | 'PRESENCIAL' | 'OUVIDORIA' | 'INTERNET' | 'OUTRO';
export type SituacaoDenuncia = 'RECEBIDA' | 'EM_APURACAO' | 'PROCEDENTE' | 'IMPROCEDENTE';
export type MotivoInspecao = 'LICENCIAMENTO' | 'PROGRAMADA' | 'DENUNCIA' | 'RETORNO' | 'SURTO' | 'OUTRO';
export type ResultadoInspecao = 'SATISFATORIA' | 'COM_RESSALVAS' | 'INSATISFATORIA';
export type RespostaRoteiro = 'CONFORME' | 'NAO_CONFORME' | 'NAO_SE_APLICA';
export type GravidadeIrregularidade = 'LEVE' | 'GRAVE' | 'GRAVISSIMA';
export type SituacaoIrregularidade = 'ABERTA' | 'CORRIGIDA' | 'AUTUADA';
export type SituacaoLicenca = 'VIGENTE' | 'VENCIDA' | 'DISPENSADA' | 'SUSPENSA' | 'CASSADA';
export type MotivoPendenciaLicenca = 'SEM_LICENCA' | 'VENCIDA' | 'VENCENDO' | 'SUSPENSA' | 'CASSADA';

export const RISCOS: Record<GrauRiscoSanitario, string> = { BAIXO: 'I · baixo', MEDIO: 'II · médio', ALTO: 'III · alto' };
export const CANAIS_DENUNCIA: Record<CanalDenuncia, string> = { TELEFONE: 'Telefone', PRESENCIAL: 'Presencial', OUVIDORIA: 'Ouvidoria', INTERNET: 'Internet', OUTRO: 'Outro' };
export const SITUACOES_DENUNCIA: Record<SituacaoDenuncia, string> = { RECEBIDA: 'Recebida', EM_APURACAO: 'Em apuração', PROCEDENTE: 'Procedente', IMPROCEDENTE: 'Improcedente' };
export const MOTIVOS_INSPECAO: Record<MotivoInspecao, string> = {
  LICENCIAMENTO: 'Licenciamento', PROGRAMADA: 'Programada', DENUNCIA: 'Denúncia', RETORNO: 'Retorno', SURTO: 'Surto', OUTRO: 'Outro',
};
export const RESULTADOS_INSPECAO: Record<ResultadoInspecao, string> = { SATISFATORIA: 'Satisfatória', COM_RESSALVAS: 'Com ressalvas', INSATISFATORIA: 'Insatisfatória' };
export const RESPOSTAS_ROTEIRO: Record<RespostaRoteiro, string> = { CONFORME: 'Conforme', NAO_CONFORME: 'Não conforme', NAO_SE_APLICA: 'Não se aplica' };
export const GRAVIDADES: Record<GravidadeIrregularidade, string> = { LEVE: 'Leve', GRAVE: 'Grave', GRAVISSIMA: 'Gravíssima' };
export const SITUACOES_IRREGULARIDADE: Record<SituacaoIrregularidade, string> = { ABERTA: 'Aberta', CORRIGIDA: 'Corrigida', AUTUADA: 'Autuada' };
export const SITUACOES_LICENCA: Record<SituacaoLicenca, string> = {
  VIGENTE: 'Vigente', VENCIDA: 'Vencida', DISPENSADA: 'Dispensada', SUSPENSA: 'Suspensa', CASSADA: 'Cassada',
};
export const MOTIVOS_PENDENCIA_LICENCA: Record<MotivoPendenciaLicenca, string> = {
  SEM_LICENCA: 'Sem licença', VENCIDA: 'Licença vencida', VENCENDO: 'Vence em até 30 dias', SUSPENSA: 'Licença suspensa', CASSADA: 'Licença cassada',
};

export interface AtividadeDto { cnae: string; descricao: string; risco: GrauRiscoSanitario }

export interface LicencaSanitariaDto {
  uuid: string;
  estabelecimentoId: string;
  estabelecimentoNome: string;
  numero: string;
  dispensa: boolean;
  emitidaEm: string;
  validade: string | null;
  situacao: SituacaoLicenca;
  observacao: string | null;
  inspecaoId: string | null;
}

export interface EstabelecimentoReguladoDto {
  uuid: string;
  documento: string;
  razaoSocial: string;
  nomeFantasia: string | null;
  endereco: string;
  latitude: number | null;
  longitude: number | null;
  microareaId: string | null;
  microareaCodigo: string | null;
  areaNome: string | null;
  unidadeId: string | null;
  unidadeNome: string | null;
  telefone: string | null;
  responsavelLegal: string | null;
  responsavelTecnico: string | null;
  registroConselhoRt: string | null;
  ativo: boolean;
  risco: GrauRiscoSanitario;
  atividades: AtividadeDto[];
  licencaAtual: LicencaSanitariaDto | null;
  irregularidadesAbertas: number;
  ultimaInspecao: string | null;
}

export interface ItemRoteiroDto { codigo: string; texto: string; gravidade: GravidadeIrregularidade }

export interface RoteiroInspecaoDto { uuid: string; codigo: string; nome: string; ativo: boolean; versaoId: string | null; versao: number; itens: ItemRoteiroDto[] }

export interface DenunciaSanitariaDto {
  uuid: string;
  protocolo: string;
  canal: CanalDenuncia;
  descricao: string;
  estabelecimentoId: string | null;
  estabelecimentoNome: string | null;
  estabelecimentoDescricao: string | null;
  anonima: boolean;
  denuncianteNome: string | null;
  denuncianteContato: string | null;
  situacao: SituacaoDenuncia;
  recebidaEm: string;
  conclusao: string | null;
  concluidaEm: string | null;
  diasEmAberto: number;
  inspecoes: number;
}

export interface IrregularidadeSanitariaDto {
  uuid: string;
  inspecaoId: string;
  dataInspecao: string;
  estabelecimentoId: string;
  estabelecimentoNome: string;
  itemCodigo: string | null;
  descricao: string;
  enquadramento: string | null;
  gravidade: GravidadeIrregularidade;
  prazoCorrecao: string | null;
  situacao: SituacaoIrregularidade;
  prazoVencido: boolean;
  corrigidaEm: string | null;
  inspecaoCorrecaoId: string | null;
}

export interface InspecaoSanitariaDto {
  uuid: string;
  estabelecimentoId: string;
  estabelecimentoNome: string;
  documento: string;
  motivo: MotivoInspecao;
  dataInspecao: string;
  denunciaId: string | null;
  denunciaProtocolo: string | null;
  surtoId: string | null;
  inspecaoOrigemId: string | null;
  roteiroVersaoId: string | null;
  roteiroNome: string | null;
  roteiroVersao: number | null;
  itens: ItemRoteiroDto[];
  respostas: Record<string, RespostaRoteiro>;
  resultado: ResultadoInspecao;
  relatorio: string;
  fiscais: { matricula: string; nome: string }[];
  irregularidades: IrregularidadeSanitariaDto[];
  corrigidas: string[];
  assinada: boolean;
  hashConteudo: string | null;
  registradoEm: string;
}

export interface EstabelecimentoDetalheDto {
  estabelecimento: EstabelecimentoReguladoDto;
  licencas: LicencaSanitariaDto[];
  inspecoes: InspecaoSanitariaDto[];
  irregularidades: IrregularidadeSanitariaDto[];
  documentos: DocumentoDto[];
}

export interface PendenciasVisaDto {
  licencas: { estabelecimentoId: string; estabelecimentoNome: string; risco: GrauRiscoSanitario; motivo: MotivoPendenciaLicenca; validade: string | null }[];
  prazosVencidos: IrregularidadeSanitariaDto[];
  denunciasParadas: DenunciaSanitariaDto[];
}

export interface EstabelecimentoRequest {
  documento: string;
  razaoSocial: string;
  nomeFantasia: string | null;
  endereco: string;
  telefone: string | null;
  responsavelLegal: string | null;
  responsavelTecnico: string | null;
  registroConselhoRt: string | null;
  ativo?: boolean;
  atividades: AtividadeDto[];
}

export interface InspecaoRequest {
  estabelecimentoId: string;
  motivo: MotivoInspecao;
  dataInspecao: string | null;
  denunciaId: string | null;
  inspecaoOrigemId: string | null;
  roteiroVersaoId: string | null;
  respostas: Record<string, RespostaRoteiro>;
  resultado: ResultadoInspecao;
  relatorio: string;
  fiscais: string[];
  irregularidades: { itemCodigo: string | null; descricao: string; enquadramento: string | null; gravidade: GravidadeIrregularidade; prazoCorrecao: string | null }[];
  corrigidas: string[];
}
