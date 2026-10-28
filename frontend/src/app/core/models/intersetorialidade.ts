// ── Intersetorialidade (ADR-0154): o catálogo de instituições externas e o encaminhamento intersetorial ──

export type SetorIntersetorial = 'ASSISTENCIA_SOCIAL' | 'EDUCACAO' | 'GARANTIA_DE_DIREITOS' | 'TRABALHO_E_RENDA' | 'HABITACAO' | 'SEGURANCA_PUBLICA' | 'OUTRO';

export type TipoInstituicaoExterna =
  | 'CRAS'
  | 'CREAS'
  | 'CENTRO_POP'
  | 'ACOLHIMENTO_INSTITUCIONAL'
  | 'ESCOLA'
  | 'CONSELHO_TUTELAR'
  | 'DEFENSORIA_PUBLICA'
  | 'MINISTERIO_PUBLICO'
  | 'JUDICIARIO'
  | 'SERVICO_DE_EMPREGO'
  | 'HABITACAO'
  | 'SEGURANCA_PUBLICA'
  | 'OUTRO';

export type SituacaoEncaminhamentoIntersetorial = 'ENVIADO' | 'RECEBIDO' | 'CONCLUIDO' | 'RECUSADO' | 'CANCELADO';

export const SETORES_INTERSETORIAIS: Record<SetorIntersetorial, string> = {
  ASSISTENCIA_SOCIAL: 'Assistência social',
  EDUCACAO: 'Educação',
  GARANTIA_DE_DIREITOS: 'Garantia de direitos',
  TRABALHO_E_RENDA: 'Trabalho e renda',
  HABITACAO: 'Habitação',
  SEGURANCA_PUBLICA: 'Segurança pública',
  OUTRO: 'Outro',
};

/** O tipo e o setor dele (nulo em OUTRO, que vale para qualquer setor), como no backend. */
export const TIPOS_INSTITUICAO: Record<TipoInstituicaoExterna, { rotulo: string; setor: SetorIntersetorial | null }> = {
  CRAS: { rotulo: 'CRAS', setor: 'ASSISTENCIA_SOCIAL' },
  CREAS: { rotulo: 'CREAS', setor: 'ASSISTENCIA_SOCIAL' },
  CENTRO_POP: { rotulo: 'Centro POP', setor: 'ASSISTENCIA_SOCIAL' },
  ACOLHIMENTO_INSTITUCIONAL: { rotulo: 'Acolhimento institucional', setor: 'ASSISTENCIA_SOCIAL' },
  ESCOLA: { rotulo: 'Escola', setor: 'EDUCACAO' },
  CONSELHO_TUTELAR: { rotulo: 'Conselho Tutelar', setor: 'GARANTIA_DE_DIREITOS' },
  DEFENSORIA_PUBLICA: { rotulo: 'Defensoria Pública', setor: 'GARANTIA_DE_DIREITOS' },
  MINISTERIO_PUBLICO: { rotulo: 'Ministério Público', setor: 'GARANTIA_DE_DIREITOS' },
  JUDICIARIO: { rotulo: 'Judiciário', setor: 'GARANTIA_DE_DIREITOS' },
  SERVICO_DE_EMPREGO: { rotulo: 'Serviço de emprego', setor: 'TRABALHO_E_RENDA' },
  HABITACAO: { rotulo: 'Habitação', setor: 'HABITACAO' },
  SEGURANCA_PUBLICA: { rotulo: 'Segurança pública', setor: 'SEGURANCA_PUBLICA' },
  OUTRO: { rotulo: 'Outro', setor: null },
};

export const SITUACOES_ENCAMINHAMENTO_INTERSETORIAL: Record<SituacaoEncaminhamentoIntersetorial, { rotulo: string; classe: string }> = {
  ENVIADO: { rotulo: 'Enviado', classe: 'info' },
  RECEBIDO: { rotulo: 'Recebido', classe: 'info' },
  CONCLUIDO: { rotulo: 'Concluído', classe: 'ok' },
  RECUSADO: { rotulo: 'Recusado', classe: 'warn' },
  CANCELADO: { rotulo: 'Cancelado', classe: 'muted' },
};

export interface InstituicaoExternaDto {
  uuid: string;
  nome: string;
  setor: SetorIntersetorial;
  tipo: TipoInstituicaoExterna;
  endereco: string | null;
  telefone: string | null;
  email: string | null;
  responsavel: string | null;
  observacao: string | null;
  ativa: boolean;
  atualizadaEm: string;
}

export interface InstituicaoExternaRequestDto {
  nome: string;
  setor: SetorIntersetorial;
  tipo: TipoInstituicaoExterna;
  endereco?: string | null;
  telefone?: string | null;
  email?: string | null;
  responsavel?: string | null;
  observacao?: string | null;
  ativa?: boolean | null;
}

export interface EncaminhamentoIntersetorialDto {
  uuid: string;
  instituicaoId: string;
  instituicaoNome: string;
  setor: SetorIntersetorial;
  tipo: TipoInstituicaoExterna;
  instituicaoTelefone: string | null;
  motivo: string;
  enviadoEm: string;
  retornoEsperadoEm: string;
  situacao: SituacaoEncaminhamentoIntersetorial;
  semRetorno: boolean;
  recebidoEm: string | null;
  retornoEm: string | null;
  retorno: string | null;
  motivoCancelamento: string | null;
  profissionalNome: string | null;
}
