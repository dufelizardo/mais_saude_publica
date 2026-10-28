/** Imunização (ADR-0147): imunobiológicos, calendário vacinal, registro de vacinação e caderneta. */

export type TipoImunobiologico = 'VACINA' | 'SORO' | 'IMUNOGLOBULINA';
export type ViaVacina = 'INTRAMUSCULAR' | 'SUBCUTANEA' | 'INTRADERMICA' | 'ORAL' | 'INTRANASAL';
export type DoseVacinal = 'PRIMEIRA' | 'SEGUNDA' | 'TERCEIRA' | 'QUARTA' | 'REFORCO' | 'SEGUNDO_REFORCO' | 'UNICA' | 'ANUAL' | 'ADICIONAL';
export type PublicoVacinal = 'CRIANCA' | 'ADOLESCENTE' | 'ADULTO' | 'IDOSO' | 'GESTANTE';
export type EstrategiaVacinacao = 'ROTINA' | 'CAMPANHA' | 'BLOQUEIO' | 'INTENSIFICACAO' | 'ESPECIAL';
export type OrigemRegistroVacinacao = 'APLICADA' | 'TRANSCRITA';
export type LocalAplicacaoVacina =
  | 'DELTOIDE_DIREITO'
  | 'DELTOIDE_ESQUERDO'
  | 'VASTO_LATERAL_DIREITO'
  | 'VASTO_LATERAL_ESQUERDO'
  | 'VENTROGLUTEO'
  | 'ORAL'
  | 'OUTRO';
export type SituacaoDoseCalendario = 'APLICADA' | 'ATRASADA' | 'PROXIMA' | 'FUTURA' | 'FORA_DA_IDADE';

export const DOSES: Record<DoseVacinal, string> = {
  PRIMEIRA: '1ª dose',
  SEGUNDA: '2ª dose',
  TERCEIRA: '3ª dose',
  QUARTA: '4ª dose',
  REFORCO: 'Reforço',
  SEGUNDO_REFORCO: '2º reforço',
  UNICA: 'Dose única',
  ANUAL: 'Dose anual',
  ADICIONAL: 'Dose adicional',
};

export const VIAS: Record<ViaVacina, string> = {
  INTRAMUSCULAR: 'Intramuscular',
  SUBCUTANEA: 'Subcutânea',
  INTRADERMICA: 'Intradérmica',
  ORAL: 'Oral',
  INTRANASAL: 'Intranasal',
};

export const ESTRATEGIAS: Record<EstrategiaVacinacao, string> = {
  ROTINA: 'Rotina',
  CAMPANHA: 'Campanha',
  BLOQUEIO: 'Bloqueio',
  INTENSIFICACAO: 'Intensificação',
  ESPECIAL: 'Especial (CRIE)',
};

export const LOCAIS_APLICACAO: Record<LocalAplicacaoVacina, string> = {
  DELTOIDE_DIREITO: 'Deltoide direito',
  DELTOIDE_ESQUERDO: 'Deltoide esquerdo',
  VASTO_LATERAL_DIREITO: 'Vasto lateral da coxa direita',
  VASTO_LATERAL_ESQUERDO: 'Vasto lateral da coxa esquerda',
  VENTROGLUTEO: 'Ventroglúteo',
  ORAL: 'Oral',
  OUTRO: 'Outro',
};

export const PUBLICOS: Record<PublicoVacinal, string> = {
  CRIANCA: 'Criança',
  ADOLESCENTE: 'Adolescente',
  ADULTO: 'Adulto',
  IDOSO: 'Idoso',
  GESTANTE: 'Gestante',
};

export const SITUACOES_DOSE: Record<SituacaoDoseCalendario, { rotulo: string; classe: string }> = {
  APLICADA: { rotulo: 'Aplicada', classe: 'ok' },
  ATRASADA: { rotulo: 'Atrasada', classe: 'alert' },
  PROXIMA: { rotulo: 'Próxima', classe: 'warn' },
  FUTURA: { rotulo: 'Futura', classe: 'info' },
  FORA_DA_IDADE: { rotulo: 'Fora da idade', classe: 'muted' },
};

export interface ImunobiologicoDto {
  uuid: string;
  codigo: string;
  nome: string;
  sigla: string;
  tipo: TipoImunobiologico;
  via: ViaVacina;
  volumeDoseMl?: number | null;
  dosesPorFrasco: number;
  validadeAposAbertoHoras?: number | null;
  codigoSipni?: string | null;
  ativo: boolean;
}

export interface EsquemaVacinalDto {
  uuid: string;
  imunobiologicoId: string;
  imunobiologicoNome: string;
  sigla: string;
  dose: DoseVacinal;
  doseRotulo: string;
  publico: PublicoVacinal;
  idadeMinimaMeses?: number | null;
  idadeRecomendadaMeses?: number | null;
  idadeMaximaMeses?: number | null;
  intervaloMinimoDias?: number | null;
  repeticaoAnos?: number | null;
  contaNaSituacao: boolean;
  observacao?: string | null;
  vigenteDesde: string;
  vigenteAte?: string | null;
  ordem: number;
  ativo: boolean;
}

export interface RegistroVacinacaoRequestDto {
  pacienteId: string;
  imunobiologicoId: string;
  dose: DoseVacinal;
  unidadeId: string;
  origem: OrigemRegistroVacinacao;
  aplicadaEm?: string | null;
  profissionalMatricula?: string | null;
  lote?: string | null;
  fabricante?: string | null;
  via?: ViaVacina | null;
  localAplicacao?: LocalAplicacaoVacina | null;
  estrategia?: EstrategiaVacinacao | null;
  campanhaId?: string | null;
  gestante?: boolean;
  justificativa?: string | null;
  localOrigem?: string | null;
  observacao?: string | null;
  motivoRetificacao?: string | null;
}

export interface RegistroVacinacaoDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  imunobiologicoId: string;
  imunobiologicoNome: string;
  sigla: string;
  dose: DoseVacinal;
  doseRotulo: string;
  aplicadaEm: string;
  unidadeId: string;
  unidadeNome: string;
  profissionalMatricula?: string | null;
  profissionalNome?: string | null;
  origem: OrigemRegistroVacinacao;
  localOrigem?: string | null;
  lote?: string | null;
  fabricante?: string | null;
  via?: ViaVacina | null;
  localAplicacao?: LocalAplicacaoVacina | null;
  estrategia: EstrategiaVacinacao;
  campanhaId?: string | null;
  campanhaNome?: string | null;
  gestante: boolean;
  justificativa?: string | null;
  observacao?: string | null;
  retificacaoDeId?: string | null;
  motivoRetificacao?: string | null;
  registradoEm: string;
}

export interface DoseCalendarioDto {
  esquemaId: string;
  imunobiologicoId: string;
  imunobiologicoNome: string;
  sigla: string;
  dose: DoseVacinal;
  doseRotulo: string;
  publico: PublicoVacinal;
  observacao?: string | null;
  dataPrevista: string;
  situacao: SituacaoDoseCalendario;
  registroId?: string | null;
  aplicadaEm?: string | null;
}

export interface CadernetaVacinacaoDto {
  pacienteId: string;
  pacienteNome: string;
  dataNascimento?: string | null;
  idadeMeses?: number | null;
  situacaoDisponivel: boolean;
  emDia: boolean;
  atrasadas: number;
  proximas: number;
  calendario: DoseCalendarioDto[];
  recomendacoes: EsquemaVacinalDto[];
  doses: RegistroVacinacaoDto[];
}

export interface VacinacaoDoDiaDto {
  unidadeId: string;
  unidadeNome: string;
  data: string;
  total: number;
  porImunobiologico: { chave: string; rotulo: string; quantidade: number }[];
  registros: RegistroVacinacaoDto[];
}

export interface AtrasadoVacinacaoDto {
  pacienteId: string;
  pacienteNome: string;
  dataNascimento: string;
  idadeMeses: number;
  equipeNome?: string | null;
  microareaNome?: string | null;
  atrasadas: string[];
  atrasadaDesde: string;
}

/** "7 meses", "2 anos e 3 meses". */
export function idadeTexto(meses: number | null | undefined): string {
  if (meses == null) {
    return '—';
  }
  if (meses < 24) {
    return `${meses} ${meses === 1 ? 'mês' : 'meses'}`;
  }
  const anos = Math.floor(meses / 12);
  const resto = meses % 12;
  return `${anos} anos${resto ? ` e ${resto} ${resto === 1 ? 'mês' : 'meses'}` : ''}`;
}
