/** Trilha de auditoria (ADR-0070, ADR-0071). */

export type AcaoAuditoria =
  | 'LOGIN'
  | 'TROCA_DE_SENHA'
  | 'RECUPERACAO_DE_SENHA'
  | 'LEITURA'
  | 'CRIACAO'
  | 'ALTERACAO'
  | 'RETIFICACAO'
  | 'REVOGACAO'
  | 'ACESSO_JUSTIFICADO'
  | 'EXCLUSAO';

export type ResultadoAuditoria = 'PERMITIDO' | 'NEGADO';

export interface EventoAuditoriaResponseDto {
  uuid: string;
  ocorridoEm: string;
  usuarioCpf?: string | null;
  usuarioNome?: string | null;
  acao: AcaoAuditoria;
  resultado: ResultadoAuditoria;
  recurso: string;
  metodo: string;
  rota: string;
  statusHttp: number;
  registroId?: string | null;
  pacienteId?: string | null;
  pacienteNome?: string | null;
  unidadeId?: string | null;
  unidadeNome?: string | null;
  origemIp?: string | null;
  detalhe?: string | null;
}

export interface PaginaAuditoriaResponseDto {
  itens: EventoAuditoriaResponseDto[];
  total: number;
  pagina: number;
  tamanho: number;
  resumo: { leituras: number; alteracoes: number; negados: number; logins: number; loginsRecusados: number };
}

export interface FiltroAuditoria {
  usuarioCpf?: string;
  pacienteId?: string;
  registroId?: string;
  unidadeId?: string;
  acao?: AcaoAuditoria | '';
  resultado?: ResultadoAuditoria | '';
  desde?: string;
  ate?: string;
  pagina?: number;
  tamanho?: number;
}

/** Rótulo e cor de cada ação — os mesmos na tela Auditoria e no "Quem acessou" do atendimento. */
export const ACOES_AUDITORIA: Record<AcaoAuditoria, { rotulo: string; classe: string }> = {
  LOGIN: { rotulo: 'Login', classe: 'info' },
  TROCA_DE_SENHA: { rotulo: 'Troca de senha', classe: 'purple' },
  RECUPERACAO_DE_SENHA: { rotulo: 'Recuperação de senha', classe: 'purple' },
  LEITURA: { rotulo: 'Leitura', classe: 'info' },
  CRIACAO: { rotulo: 'Criação', classe: 'ok' },
  ALTERACAO: { rotulo: 'Alteração', classe: 'warn' },
  RETIFICACAO: { rotulo: 'Retificação', classe: 'warn' },
  REVOGACAO: { rotulo: 'Revogação', classe: 'alert' },
  ACESSO_JUSTIFICADO: { rotulo: 'Acesso justificado', classe: 'warn' },
  EXCLUSAO: { rotulo: 'Exclusão', classe: 'alert' },
};

const RECURSOS: Record<string, string> = {
  PRONTUARIO: 'Prontuário',
  PACIENTE: 'Paciente',
  ATENDIMENTO: 'Atendimento',
  AGENDAMENTO: 'Agendamento',
  TRIAGEM: 'Triagem',
  EVOLUCAO_ENFERMAGEM: 'Evolução de enfermagem',
  CONSULTA: 'Consulta',
  PROCEDIMENTO: 'Procedimento',
  ADMINISTRACAO_MEDICAMENTO: 'Medicação',
  DISPENSACAO: 'Dispensação',
  LOTE: 'Lote',
  MEDICAMENTO: 'Medicamento',
  MOVIMENTACAO_FARMACIA: 'Livro de estoque',
  TRANSFERENCIA_FARMACIA: 'Transferência',
  AUTH: 'Acesso ao sistema',
  USUARIO: 'Usuário',
  PAPEL: 'Perfil',
  ATRIBUICAO_ACESSO: 'Acesso concedido',
  AUDITORIA: 'Auditoria',
};

/** TRANSFERENCIA_FARMACIA → "Transferência"; recurso sem rótulo → "Cargo salarial" a partir do código. */
export function rotuloRecurso(recurso: string): string {
  if (RECURSOS[recurso]) return RECURSOS[recurso];
  const texto = recurso.toLowerCase().replace(/_/g, ' ');
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}
