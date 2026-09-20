export interface EnderecoRequestDto {
  cep: string;
  logradouro: string;
  numeroLogradouro: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  estado: string;
  ddd?: string;
}

export interface ProfissionalContatoRequestDto {
  endereco: EnderecoRequestDto;
  telefones: string[];
  email: string;
}

export interface ProfissionalRequestDto {
  cpf: string;
  nome: string;
  email: string;
  endereco: EnderecoRequestDto;
  telefones: string[];
  conselhoClasse?: string;
  numeroConselho?: string;
  dataAdmissao?: string;
}

export interface EnderecoResponseDto {
  cep: string;
  logradouro: string;
  numeroLogradouro: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  estado: string;
  ddd?: string;
}

export interface ProfissionalResponseDto {
  matricula: string;
  cpf: string;
  nome: string;
  conselhoClasse?: string;
  numeroConselho?: string;
  endereco: EnderecoResponseDto;
  telefones: string[];
  email: string;
  dataAdmissao?: string;
  dataDesligamento?: string;
  ativo: boolean;
}

export interface SuccessResponseDto {
  message: string;
  details: string;
}

export interface ErrorResponseDto {
  message: string;
  details: string;
}
