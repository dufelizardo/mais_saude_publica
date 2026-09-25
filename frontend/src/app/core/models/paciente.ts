import { EnderecoRequestDto, EnderecoResponseDto } from './profissional';

export type Sexo = 'MASCULINO' | 'FEMININO' | 'IGNORADO';

export interface PacienteRequestDto {
  nome: string;
  cpf: string;
  cartaoSus?: string;
  dataNascimento: string;
  sexo: Sexo;
  endereco: EnderecoRequestDto;
  telefones: string[];
  email?: string;
  ativo: boolean;
}

export interface PacienteResponseDto {
  uuid: string;
  nome: string;
  cpf: string;
  cartaoSus?: string;
  dataNascimento: string;
  sexo: Sexo;
  endereco: EnderecoResponseDto;
  telefones: string[];
  email?: string;
  ativo: boolean;
}
