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
  /** A dose de referência da cobertura da vacina, com a meta em % (ADR-0149). */
  medeCobertura: boolean;
  metaCobertura?: number | null;
}

export interface EsquemaVacinalRequestDto {
  imunobiologicoId: string;
  dose: DoseVacinal;
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
  medeCobertura?: boolean | null;
  metaCobertura?: number | null;
}

/** O pedido de ajuste com os dados atuais da dose, trocando só o que vem em {@code mudancas}. */
export function esquemaRequest(e: EsquemaVacinalDto, mudancas: Partial<EsquemaVacinalRequestDto>): EsquemaVacinalRequestDto {
  return {
    imunobiologicoId: e.imunobiologicoId,
    dose: e.dose,
    publico: e.publico,
    idadeMinimaMeses: e.idadeMinimaMeses,
    idadeRecomendadaMeses: e.idadeRecomendadaMeses,
    idadeMaximaMeses: e.idadeMaximaMeses,
    intervaloMinimoDias: e.intervaloMinimoDias,
    repeticaoAnos: e.repeticaoAnos,
    contaNaSituacao: e.contaNaSituacao,
    observacao: e.observacao,
    vigenteDesde: e.vigenteDesde,
    vigenteAte: e.vigenteAte,
    ordem: e.ordem,
    ativo: e.ativo,
    medeCobertura: e.medeCobertura,
    metaCobertura: e.metaCobertura,
    ...mudancas,
  };
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
  /** Na unidade com sala de vacina (ADR-0148): o lote do estoque. */
  loteEstoqueId?: string | null;
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
  loteEstoqueId?: string | null;
  frascoId?: string | null;
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
  /** As contraindicações por ESAVI e as doses já notificadas na Vigilância (ADR-0150). */
  contraindicacoes: ContraindicacaoVacinalDto[];
  dosesNotificadas: string[];
}

export interface ContraindicacaoVacinalDto {
  imunobiologicoId: string;
  sigla: string;
  nome: string;
  desde: string;
  motivo: string;
  notificacaoId?: string | null;
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
  equipeId?: string | null;
  microareaId?: string | null;
  acsNome?: string | null;
  endereco?: string | null;
}

// ── Cobertura (ADR-0149) ──

export type ClasseAbandonoVacinal = 'BAIXA' | 'MEDIA' | 'ALTA';

export const CLASSES_ABANDONO: Record<ClasseAbandonoVacinal, string> = { BAIXA: 'Baixo', MEDIA: 'Médio', ALTA: 'Alto' };

export interface CoberturaVacinaDto {
  esquemaId: string;
  imunobiologicoId: string;
  sigla: string;
  nome: string;
  dose: DoseVacinal;
  doseRotulo: string;
  idadeRecomendadaMeses: number;
  meta: number;
  coorte: number;
  vacinados: number;
  cobertura?: number | null;
  atingida: boolean;
  homogeneidadeTerritorios?: number | null;
  comPrimeiraDose?: number | null;
  abandono?: number | null;
  classeAbandono?: ClasseAbandonoVacinal | null;
}

export interface CoberturaItemDto {
  esquemaId: string;
  coorte: number;
  vacinados: number;
  cobertura?: number | null;
  atingida: boolean;
}

export interface CoberturaRecorteDto {
  id?: string | null;
  nome: string;
  homogeneidade?: number | null;
  vacinasNaMeta: number;
  vacinasAvaliadas: number;
  itens: CoberturaItemDto[];
}

export interface CoberturaVacinalDto {
  nivel: 'REDE' | 'UNIDADE' | 'EQUIPE' | 'MICROAREA';
  abrangencia: string;
  unidadeId?: string | null;
  equipeId?: string | null;
  microareaId?: string | null;
  dataReferencia: string;
  homogeneidade?: number | null;
  vacinasNaMeta: number;
  vacinasAvaliadas: number;
  abandonoAlto: number;
  vacinas: CoberturaVacinaDto[];
  recorteRotulo?: string | null;
  recortes: CoberturaRecorteDto[];
}

export interface FiltroCobertura {
  unidadeId?: string;
  equipeId?: string;
  microareaId?: string;
  data?: string;
}

/** "95%" ou "—". */
export function percentual(v: number | null | undefined): string {
  return v == null ? '—' : `${Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`;
}

/** A classe da tag pela meta: atingida, perto (até 10 pontos abaixo) ou abaixo; sem coorte, neutra. */
export function classeCobertura(cobertura: number | null | undefined, meta: number): string {
  if (cobertura == null) {
    return 'tag tag--muted';
  }
  return cobertura >= meta ? 'tag tag--ok' : cobertura >= meta - 10 ? 'tag tag--warn' : 'tag tag--alert';
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

// ── Sala de vacina: estoque e cadeia de frio (ADR-0148) ──

export type SituacaoLoteImunobiologico = 'DISPONIVEL' | 'SOB_SUSPEITA' | 'INUTILIZADO';
export type TipoMovimentacaoImunobiologico = 'ENTRADA' | 'APLICACAO' | 'ESTORNO_APLICACAO' | 'PERDA_FISICA' | 'PERDA_TECNICA' | 'AJUSTE_INVENTARIO';
export type MotivoPerdaImunobiologico =
  | 'VENCIMENTO'
  | 'QUEBRA'
  | 'FALHA_EQUIPAMENTO'
  | 'FALTA_ENERGIA'
  | 'TRANSPORTE'
  | 'EXCURSAO_TEMPERATURA'
  | 'FRASCO_ABERTO_VENCIDO'
  | 'OUTRO';
export type TurnoLeituraTemperatura = 'MANHA' | 'TARDE';

export const SITUACOES_LOTE: Record<SituacaoLoteImunobiologico, { rotulo: string; classe: string }> = {
  DISPONIVEL: { rotulo: 'Disponível', classe: 'ok' },
  SOB_SUSPEITA: { rotulo: 'Sob suspeita', classe: 'alert' },
  INUTILIZADO: { rotulo: 'Inutilizado', classe: 'muted' },
};

export const TIPOS_MOVIMENTACAO: Record<TipoMovimentacaoImunobiologico, string> = {
  ENTRADA: 'Entrada',
  APLICACAO: 'Aplicação',
  ESTORNO_APLICACAO: 'Estorno de aplicação',
  PERDA_FISICA: 'Perda física',
  PERDA_TECNICA: 'Perda técnica',
  AJUSTE_INVENTARIO: 'Ajuste de inventário',
};

/** Os motivos que a equipe informa; a perda do frasco aberto vencido é apurada pelo sistema. */
export const MOTIVOS_PERDA: Partial<Record<MotivoPerdaImunobiologico, string>> = {
  QUEBRA: 'Quebra do frasco',
  VENCIMENTO: 'Vencimento do lote',
  FALHA_EQUIPAMENTO: 'Falha do equipamento',
  FALTA_ENERGIA: 'Falta de energia',
  TRANSPORTE: 'Problema no transporte',
  EXCURSAO_TEMPERATURA: 'Excursão de temperatura',
  OUTRO: 'Outro',
};

export const TURNOS: Record<TurnoLeituraTemperatura, string> = { MANHA: 'Manhã', TARDE: 'Tarde' };

export interface LoteImunobiologicoDto {
  uuid: string;
  unidadeId: string;
  imunobiologicoId: string;
  sigla: string;
  imunobiologicoNome: string;
  numeroLote: string;
  fabricante?: string | null;
  validade: string;
  doses: number;
  camaraId?: string | null;
  camaraNome?: string | null;
  situacao: SituacaoLoteImunobiologico;
  sobSuspeitaDesde?: string | null;
  parecer?: string | null;
  vencido: boolean;
  venceEm30Dias: boolean;
  aplicavel: boolean;
}

export interface FrascoAbertoDto {
  uuid: string;
  loteId: string;
  sigla: string;
  numeroLote: string;
  abertoEm: string;
  validoAte: string;
  dosesNoFrasco: number;
  dosesUsadas: number;
  dosesRestantes: number;
  minutosRestantes: number;
}

export interface EstoqueSalaVacinaDto {
  unidadeId: string;
  unidadeNome: string;
  temSalaDeVacina: boolean;
  porVacina: { imunobiologicoId: string; sigla: string; nome: string; doses: number; lotes: number }[];
  lotes: LoteImunobiologicoDto[];
  frascos: FrascoAbertoDto[];
  aplicadasMes: number;
  perdaTecnicaMes: number;
  perdaFisicaMes: number;
}

export interface MovimentacaoImunobiologicoDto {
  uuid: string;
  tipo: TipoMovimentacaoImunobiologico;
  doses: number;
  saldoApos: number;
  motivoPerda?: MotivoPerdaImunobiologico | null;
  justificativa?: string | null;
  registroVacinacaoId?: string | null;
  frascoId?: string | null;
  registradoEm: string;
  registradoPorCpf?: string | null;
}

export interface EntradaLoteRequestDto {
  unidadeId: string;
  imunobiologicoId: string;
  numeroLote: string;
  fabricante?: string | null;
  validade: string;
  doses: number;
  camaraId?: string | null;
  justificativa?: string | null;
}

export interface LeituraTemperaturaDto {
  uuid: string;
  camaraId: string;
  camaraNome: string;
  lidaEm: string;
  turno: TurnoLeituraTemperatura;
  temperaturaAtual: number;
  temperaturaMinima: number;
  temperaturaMaxima: number;
  excursao: boolean;
  profissionalNome?: string | null;
  observacao?: string | null;
  lotesPostosSobSuspeita: number;
}

export interface CamaraRefrigeracaoDto {
  uuid: string;
  unidadeId: string;
  nome: string;
  temperaturaMinima: number;
  temperaturaMaxima: number;
  ativa: boolean;
  ultimaLeitura?: LeituraTemperaturaDto | null;
  lotesSobSuspeita: number;
}

export interface CadeiaFrioDto {
  unidadeId: string;
  data: string;
  camaras: CamaraRefrigeracaoDto[];
  leituras: LeituraTemperaturaDto[];
  pendencias: string[];
  lotesSobSuspeita: LoteImunobiologicoDto[];
}

export interface LeituraTemperaturaRequestDto {
  lidaEm?: string | null;
  turno?: TurnoLeituraTemperatura | null;
  temperaturaAtual: number;
  temperaturaMinima: number;
  temperaturaMaxima: number;
  profissionalMatricula?: string | null;
  observacao?: string | null;
}

/** "3 h 20 min", "45 min". */
export function prazoTexto(minutos: number): string {
  if (minutos <= 0) {
    return 'vencido';
  }
  const h = Math.floor(minutos / 60);
  const m = minutos % 60;
  if (h >= 48) {
    return `${Math.floor(h / 24)} dias`;
  }
  return h ? `${h} h${m ? ` ${m} min` : ''}` : `${m} min`;
}

/** "2026-11-30" → "30/11/2026". */
export function dataBr(iso: string | null | undefined): string {
  return iso ? `${iso.slice(8, 10)}/${iso.slice(5, 7)}/${iso.slice(0, 4)}` : '—';
}
