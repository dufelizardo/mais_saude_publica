import { AtendimentoResponseDto } from './atendimento';
import { ConsultaResponseDto } from './consulta';
import { ProcedimentoResponseDto } from './procedimento';

export interface ProntuarioConsultaDto {
  consulta: ConsultaResponseDto;
  procedimentos: ProcedimentoResponseDto[];
}

export interface ProntuarioAtendimentoDto {
  atendimento: AtendimentoResponseDto;
  consultas: ProntuarioConsultaDto[];
}

export interface ProntuarioResponseDto {
  pacienteUuid: string;
  pacienteNome: string;
  atendimentos: ProntuarioAtendimentoDto[];
}
