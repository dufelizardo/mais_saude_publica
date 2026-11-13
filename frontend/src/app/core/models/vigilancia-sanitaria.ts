import { DocumentoDto } from './documento';

/** Vigilância Sanitária, fatia V3a (ADR-0199). */
export type GrauRiscoSanitario = 'BAIXO' | 'MEDIO' | 'ALTO';
export type CanalDenuncia = 'TELEFONE' | 'PRESENCIAL' | 'OUVIDORIA' | 'INTERNET' | 'OUTRO' | 'VIGILANCIA';
export type SituacaoDenuncia = 'RECEBIDA' | 'EM_APURACAO' | 'PROCEDENTE' | 'IMPROCEDENTE';
export type MotivoInspecao = 'LICENCIAMENTO' | 'PROGRAMADA' | 'DENUNCIA' | 'RETORNO' | 'SURTO' | 'OUTRO';
export type ResultadoInspecao = 'SATISFATORIA' | 'COM_RESSALVAS' | 'INSATISFATORIA';
export type RespostaRoteiro = 'CONFORME' | 'NAO_CONFORME' | 'NAO_SE_APLICA';
export type GravidadeIrregularidade = 'LEVE' | 'GRAVE' | 'GRAVISSIMA';
export type SituacaoIrregularidade = 'ABERTA' | 'CORRIGIDA' | 'AUTUADA';
export type SituacaoLicenca = 'VIGENTE' | 'VENCIDA' | 'DISPENSADA' | 'SUSPENSA' | 'CASSADA';
export type MotivoPendenciaLicenca = 'SEM_LICENCA' | 'VENCIDA' | 'VENCENDO' | 'SUSPENSA' | 'CASSADA';

export const RISCOS: Record<GrauRiscoSanitario, string> = { BAIXO: 'I · baixo', MEDIO: 'II · médio', ALTO: 'III · alto' };
export const CANAIS_DENUNCIA: Record<CanalDenuncia, string> = { TELEFONE: 'Telefone', PRESENCIAL: 'Presencial', OUVIDORIA: 'Ouvidoria', INTERNET: 'Internet', OUTRO: 'Outro', VIGILANCIA: 'Vigilância (saúde do trabalhador)' };
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
  processos: { processoId: string; numero: string; estabelecimentoNome: string; motivo: MotivoPendenciaProcesso; desde: string }[];
  medidas: { medidaId: string; numero: string; estabelecimentoNome: string; motivo: 'CAUTELAR_VENCENDO' | 'CAUTELAR_VENCIDA'; fimPrevisto: string }[];
}

// ── Processo sanitário e medidas (ADR-0200) ──

export type FaseProcessoSanitario = 'AGUARDANDO_CIENCIA' | 'EM_DEFESA' | 'AGUARDANDO_DECISAO' | 'DECIDIDO' | 'EM_RECURSO' | 'ENCERRADO';
export type FormaCiencia = 'PESSOAL' | 'POSTAL' | 'EDITAL' | 'RECUSA';
export type ResultadoDecisaoSanitaria = 'ARQUIVAMENTO' | 'PENALIDADE';
export type ResultadoRecursoSanitario = 'PROVIDO' | 'PARCIALMENTE_PROVIDO' | 'IMPROVIDO';
export type TipoPenalidadeSanitaria = 'ADVERTENCIA' | 'MULTA' | 'APREENSAO' | 'INUTILIZACAO' | 'INTERDICAO_PARCIAL' | 'INTERDICAO_TOTAL' | 'SUSPENSAO_VENDAS' | 'SUSPENSAO_LICENCA' | 'CANCELAMENTO_LICENCA';
export type TipoMedidaSanitaria = 'INTERDICAO_CAUTELAR' | 'INTERDICAO' | 'APREENSAO';
export type TipoTermoSanitario = 'AUTO_INFRACAO' | 'DECISAO' | 'DECISAO_RECURSO' | 'INTERDICAO' | 'APREENSAO' | 'DESINTERDICAO';
export type AtenuanteSanitaria = 'ACAO_NAO_FUNDAMENTAL' | 'ERRADA_COMPREENSAO' | 'PROCUROU_REPARAR' | 'SOFREU_COACAO' | 'PRIMARIO';
export type AgravanteSanitaria = 'REINCIDENCIA' | 'VANTAGEM_PECUNIARIA' | 'COAGIU_OUTREM' | 'CONSEQUENCIA_CALAMITOSA' | 'NAO_TOMOU_PROVIDENCIAS' | 'FRAUDE_OU_MA_FE';
export type MotivoPendenciaProcesso = 'CIENCIA_DO_AUTO' | 'REVELIA' | 'DECISAO_PARADA' | 'CIENCIA_DA_DECISAO' | 'A_ENCERRAR';

export const FASES_PROCESSO: Record<FaseProcessoSanitario, string> = {
  AGUARDANDO_CIENCIA: 'Aguardando ciência', EM_DEFESA: 'Em defesa', AGUARDANDO_DECISAO: 'Aguardando decisão', DECIDIDO: 'Decidido', EM_RECURSO: 'Em recurso', ENCERRADO: 'Encerrado',
};
export const FORMAS_CIENCIA: Record<FormaCiencia, string> = { PESSOAL: 'Pessoal', POSTAL: 'Por AR', EDITAL: 'Por edital', RECUSA: 'Recusa certificada' };
export const PENALIDADES: Record<TipoPenalidadeSanitaria, string> = {
  ADVERTENCIA: 'Advertência', MULTA: 'Multa', APREENSAO: 'Apreensão', INUTILIZACAO: 'Inutilização', INTERDICAO_PARCIAL: 'Interdição parcial',
  INTERDICAO_TOTAL: 'Interdição total', SUSPENSAO_VENDAS: 'Suspensão de vendas', SUSPENSAO_LICENCA: 'Suspensão da licença', CANCELAMENTO_LICENCA: 'Cancelamento da licença',
};
export const TIPOS_MEDIDA: Record<TipoMedidaSanitaria, string> = { INTERDICAO_CAUTELAR: 'Interdição cautelar', INTERDICAO: 'Interdição', APREENSAO: 'Apreensão' };
export const TIPOS_TERMO: Record<TipoTermoSanitario, string> = {
  AUTO_INFRACAO: 'Auto de infração', DECISAO: 'Decisão', DECISAO_RECURSO: 'Julgamento do recurso', INTERDICAO: 'Termo de interdição', APREENSAO: 'Termo de apreensão',
  DESINTERDICAO: 'Termo de desinterdição',
};
export const ATENUANTES: Record<AtenuanteSanitaria, string> = {
  ACAO_NAO_FUNDAMENTAL: 'Ação não fundamental para o evento', ERRADA_COMPREENSAO: 'Errada compreensão da norma', PROCUROU_REPARAR: 'Procurou reparar o dano',
  SOFREU_COACAO: 'Sofreu coação', PRIMARIO: 'Infrator primário',
};
export const AGRAVANTES: Record<AgravanteSanitaria, string> = {
  REINCIDENCIA: 'Reincidência', VANTAGEM_PECUNIARIA: 'Vantagem pecuniária', COAGIU_OUTREM: 'Coagiu outrem', CONSEQUENCIA_CALAMITOSA: 'Consequência calamitosa',
  NAO_TOMOU_PROVIDENCIAS: 'Não tomou providências', FRAUDE_OU_MA_FE: 'Fraude ou má-fé',
};
export const MOTIVOS_PENDENCIA_PROCESSO: Record<MotivoPendenciaProcesso, string> = {
  CIENCIA_DO_AUTO: 'Ciência do auto por registrar', REVELIA: 'Prazo de defesa vencido (revelia)', DECISAO_PARADA: 'Decisão parada há mais de 30 dias',
  CIENCIA_DA_DECISAO: 'Ciência da decisão por registrar', A_ENCERRAR: 'Prazo de recurso vencido: encerrar',
};

export interface TermoSanitarioDto { uuid: string; codigo: string; tipo: TipoTermoSanitario; numero: string; emitidoEm: string; versaoId: string | null }
export interface PenalidadeSanitariaDto { uuid: string; tipo: TipoPenalidadeSanitaria; valorMulta: number | null; suspensaoAte: string | null; descricao: string | null; revogada: boolean }

export interface MedidaSanitariaDto {
  uuid: string; numero: string; estabelecimentoId: string; estabelecimentoNome: string; processoId: string | null; processoNumero: string | null;
  tipo: TipoMedidaSanitaria; total: boolean; descricao: string; produto: string | null; lote: string | null; quantidade: string | null; localGuarda: string | null;
  inicio: string; fimPrevisto: string | null; situacao: 'VIGENTE' | 'ENCERRADA'; encerradaEm: string | null; motivoEncerramento: string | null; vencida: boolean;
  termos: TermoSanitarioDto[];
}

export interface ProcessoSanitarioDto {
  uuid: string; numero: string; estabelecimentoId: string; estabelecimentoNome: string; documento: string; inspecaoId: string | null;
  dispositivoLegal: string; descricao: string; gravidade: GravidadeIrregularidade; fase: FaseProcessoSanitario; dataLavratura: string;
  fiscais: { matricula: string; nome: string }[]; irregularidades: IrregularidadeSanitariaDto[];
  cienciaAutoEm: string | null; formaCienciaAuto: FormaCiencia | null; prazoDefesa: string | null; defesaEm: string | null; defesaTexto: string | null;
  defesaIntempestiva: boolean | null; revelia: boolean; decisaoResultado: ResultadoDecisaoSanitaria | null; dataDecisao: string | null; decisaoFundamentacao: string | null;
  atenuantes: AtenuanteSanitaria[]; agravantes: AgravanteSanitaria[]; cienciaDecisaoEm: string | null; formaCienciaDecisao: FormaCiencia | null; prazoRecurso: string | null;
  recursoEm: string | null; recursoTexto: string | null; recursoResultado: ResultadoRecursoSanitario | null; dataJulgamentoRecurso: string | null;
  recursoFundamentacao: string | null; encerradoEm: string | null; penalidades: PenalidadeSanitariaDto[]; termos: TermoSanitarioDto[]; medidas: MedidaSanitariaDto[];
}

export interface InterdicaoDto { uuid: string; numero: string; estabelecimentoNome: string; tipo: TipoMedidaSanitaria; total: boolean; inicio: string; fimPrevisto: string | null }

export interface VerificacaoTermoDto { codigo: string; tipo: TipoTermoSanitario; numero: string; emitidoEm: string; estabelecimento: string; documento: string; integro: boolean; hashPdf: string }
export interface VerificacaoLicencaDto { numero: string; estabelecimento: string; documento: string; dispensa: boolean; emitidaEm: string; validade: string | null; situacao: SituacaoLicenca }

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
