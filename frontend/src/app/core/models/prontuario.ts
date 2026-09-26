import { AtendimentoResponseDto } from './atendimento';
import { ConsultaResponseDto } from './consulta';
import { EvolucaoEnfermagemResponseDto } from './evolucao-enfermagem';
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
}

export interface ProntuarioResponseDto {
  pacienteUuid: string;
  pacienteNome: string;
  atendimentos: ProntuarioAtendimentoDto[];
}
