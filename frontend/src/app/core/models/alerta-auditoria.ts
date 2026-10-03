/** Alertas da auditoria (ADR-0096 e 0097). */

export type TipoAlertaAuditoria = 'RECUSAS_SEGUIDAS' | 'LOGIN_RECUSADO' | 'LEITURA_EM_MASSA' | 'LEITURA_FORA_DO_HORARIO' | 'ACESSO_JUSTIFICADO';
export type SeveridadeAlertaAuditoria = 'ALTA' | 'MEDIA';
export type StatusAlertaAuditoria = 'ABERTO' | 'PROCEDENTE' | 'IMPROCEDENTE';

export interface AlertaAuditoriaResponseDto {
  uuid: string;
  tipo: TipoAlertaAuditoria;
  severidade: SeveridadeAlertaAuditoria;
  status: StatusAlertaAuditoria;
  sujeito: string;
  usuarioCpf?: string | null;
  usuarioNome?: string | null;
  unidadeId?: string | null;
  unidadeNome?: string | null;
  pacienteId?: string | null;
  pacienteNome?: string | null;
  primeiroEventoEm: string;
  ultimoEventoEm: string;
  quantidade: number;
  descricao: string;
  detectadoEm: string;
  analisadoPorCpf?: string | null;
  analisadoPorNome?: string | null;
  analisadoEm?: string | null;
  parecer?: string | null;
}

export interface ResumoAlertasAuditoriaDto {
  abertos: number;
  abertosAlta: number;
  ultimos7Dias: number;
  procedentes: number;
}

export interface AnaliseAlertaAuditoriaRequestDto {
  conclusao: Exclude<StatusAlertaAuditoria, 'ABERTO'>;
  parecer: string;
}

export const TIPOS_ALERTA: Record<TipoAlertaAuditoria, { rotulo: string; descricao: string }> = {
  RECUSAS_SEGUIDAS: { rotulo: 'Recusas seguidas', descricao: 'Muitos acessos recusados (sem permissão ou fora do escopo) em pouco tempo.' },
  LOGIN_RECUSADO: { rotulo: 'Login recusado em sequência', descricao: 'Muitas tentativas de login erradas: pode ser alguém tentando descobrir a senha.' },
  LEITURA_EM_MASSA: { rotulo: 'Leitura em massa', descricao: 'Dados de muitos pacientes diferentes abertos em pouco tempo.' },
  LEITURA_FORA_DO_HORARIO: { rotulo: 'Leitura de madrugada', descricao: 'Leituras de dado de saúde de madrugada, fora de unidade que funciona 24 horas.' },
  ACESSO_JUSTIFICADO: { rotulo: 'Acesso justificado', descricao: 'Prontuário aberto sem vínculo assistencial: a supervisão revisa o motivo.' },
};

export const SEVERIDADES: Record<SeveridadeAlertaAuditoria, { rotulo: string; classe: string }> = {
  ALTA: { rotulo: 'Alta', classe: 'alert' },
  MEDIA: { rotulo: 'Média', classe: 'warn' },
};

export const STATUS_ALERTA: Record<StatusAlertaAuditoria, { rotulo: string; classe: string }> = {
  ABERTO: { rotulo: 'Aberto', classe: 'info' },
  PROCEDENTE: { rotulo: 'Procedente', classe: 'alert' },
  IMPROCEDENTE: { rotulo: 'Improcedente', classe: 'ok' },
};
