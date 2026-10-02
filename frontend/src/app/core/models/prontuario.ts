import { AdministracaoMedicamentoResponseDto } from './administracao-medicamento';
import { AtendimentoResponseDto } from './atendimento';
import { ConsultaResponseDto } from './consulta';
import { EvolucaoEnfermagemResponseDto } from './evolucao-enfermagem';
import { ExameProntuarioDto } from './laboratorio';
import { ProcedimentoResponseDto } from './procedimento';
import { TriagemResponseDto } from './triagem';

export interface ProntuarioConsultaDto {
  consulta: ConsultaResponseDto;
  procedimentos: ProcedimentoResponseDto[];
}

export interface ProntuarioAtendimentoDto {
  atendimento: AtendimentoResponseDto;
  triagens: TriagemResponseDto[];
  evolucoes: EvolucaoEnfermagemResponseDto[];
  consultas: ProntuarioConsultaDto[];
  /** Checagens de medicamento (ADR-0064); ausente em respostas anteriores. */
  administracoes?: AdministracaoMedicamentoResponseDto[];
}

/** Em que se baseou a abertura do prontuário (ADR-0076). */
export interface AcessoProntuarioDto {
  base: 'LIVRE' | 'VINCULO' | 'JUSTIFICADO' | 'PERMISSAO_AMPLA';
  descricao?: string | null;
  expiraEm?: string | null;
}

export interface ProntuarioResponseDto {
  pacienteUuid: string;
  pacienteNome: string;
  atendimentos: ProntuarioAtendimentoDto[];
  /** Ausente em respostas anteriores à ADR-0076. */
  acesso?: AcessoProntuarioDto;
  /** Exames laboratoriais do paciente (ADR-0095); ausente em respostas anteriores. */
  exames?: ExameProntuarioDto[];
}

export type MotivoAcessoJustificado = 'EMERGENCIA' | 'CONTINUIDADE_DO_CUIDADO' | 'REGULACAO_OU_ENCAMINHAMENTO' | 'OUTRO';

export interface AcessoJustificadoRequestDto {
  motivo: MotivoAcessoJustificado;
  justificativa: string;
}

export interface AcessoJustificadoResponseDto {
  uuid: string;
  pacienteUuid: string;
  motivo: MotivoAcessoJustificado;
  concedidoEm: string;
  expiraEm: string;
}
