/** Cadastro territorial, fatia F3a — ADR-0127. */

import { EnderecoRequestDto } from './profissional';

export type TipoImovel = 'DOMICILIO' | 'COMERCIO' | 'ESCOLA' | 'INSTITUICAO' | 'TERRENO_BALDIO' | 'PONTO_ESTRATEGICO' | 'OUTRO';
export type SituacaoMoradia = 'PROPRIO' | 'FINANCIADO' | 'ALUGADO' | 'ARRENDADO' | 'CEDIDO' | 'OCUPACAO' | 'OUTRA';
export type LocalizacaoDomicilio = 'URBANA' | 'RURAL';
export type AbastecimentoAgua = 'REDE_ENCANADA' | 'POCO_OU_NASCENTE' | 'CISTERNA' | 'CARRO_PIPA' | 'OUTRO';
export type EscoamentoSanitario = 'REDE_COLETORA' | 'FOSSA_SEPTICA' | 'FOSSA_RUDIMENTAR' | 'DIRETO_RIO_LAGO_MAR' | 'CEU_ABERTO' | 'OUTRO';
export type DestinoLixo = 'COLETADO' | 'QUEIMADO_OU_ENTERRADO' | 'CEU_ABERTO' | 'OUTRO';
export type FaixaRendaFamiliar = 'ATE_UM_QUARTO_SM' | 'ATE_MEIO_SM' | 'ATE_UM_SM' | 'ATE_DOIS_SM' | 'ATE_TRES_SM' | 'ATE_QUATRO_SM' | 'ACIMA_DE_QUATRO_SM';
export type Parentesco =
  | 'RESPONSAVEL' | 'CONJUGE' | 'FILHO' | 'ENTEADO' | 'NETO' | 'PAI_OU_MAE' | 'SOGRO' | 'IRMAO' | 'GENRO_OU_NORA' | 'OUTRO_PARENTE' | 'NAO_PARENTE';

export const TIPOS_IMOVEL: Record<TipoImovel, { rotulo: string; sigla: string }> = {
  DOMICILIO: { rotulo: 'Domicílio', sigla: 'D' },
  COMERCIO: { rotulo: 'Comércio', sigla: 'C' },
  ESCOLA: { rotulo: 'Escola', sigla: 'E' },
  INSTITUICAO: { rotulo: 'Instituição', sigla: 'I' },
  TERRENO_BALDIO: { rotulo: 'Terreno baldio', sigla: 'T' },
  PONTO_ESTRATEGICO: { rotulo: 'Ponto estratégico', sigla: 'PE' },
  OUTRO: { rotulo: 'Outro', sigla: 'O' },
};

export const SITUACOES_MORADIA: Record<SituacaoMoradia, string> = {
  PROPRIO: 'Próprio', FINANCIADO: 'Financiado', ALUGADO: 'Alugado', ARRENDADO: 'Arrendado', CEDIDO: 'Cedido', OCUPACAO: 'Ocupação', OUTRA: 'Outra',
};
export const LOCALIZACOES: Record<LocalizacaoDomicilio, string> = { URBANA: 'Urbana', RURAL: 'Rural' };
export const AGUA: Record<AbastecimentoAgua, string> = {
  REDE_ENCANADA: 'Rede encanada', POCO_OU_NASCENTE: 'Poço ou nascente', CISTERNA: 'Cisterna', CARRO_PIPA: 'Carro-pipa', OUTRO: 'Outro',
};
export const ESGOTO: Record<EscoamentoSanitario, string> = {
  REDE_COLETORA: 'Rede coletora', FOSSA_SEPTICA: 'Fossa séptica', FOSSA_RUDIMENTAR: 'Fossa rudimentar',
  DIRETO_RIO_LAGO_MAR: 'Direto para rio, lago ou mar', CEU_ABERTO: 'Céu aberto', OUTRO: 'Outro',
};
export const LIXO: Record<DestinoLixo, string> = { COLETADO: 'Coletado', QUEIMADO_OU_ENTERRADO: 'Queimado ou enterrado', CEU_ABERTO: 'Céu aberto', OUTRO: 'Outro' };
export const RENDAS: Record<FaixaRendaFamiliar, string> = {
  ATE_UM_QUARTO_SM: 'Até ¼ de salário mínimo', ATE_MEIO_SM: 'Até ½ salário mínimo', ATE_UM_SM: 'Até 1 salário mínimo',
  ATE_DOIS_SM: 'Até 2 salários mínimos', ATE_TRES_SM: 'Até 3 salários mínimos', ATE_QUATRO_SM: 'Até 4 salários mínimos',
  ACIMA_DE_QUATRO_SM: 'Acima de 4 salários mínimos',
};
export const PARENTESCOS: Record<Parentesco, string> = {
  RESPONSAVEL: 'Responsável', CONJUGE: 'Cônjuge', FILHO: 'Filho(a)', ENTEADO: 'Enteado(a)', NETO: 'Neto(a)', PAI_OU_MAE: 'Pai ou mãe',
  SOGRO: 'Sogro(a)', IRMAO: 'Irmão(ã)', GENRO_OU_NORA: 'Genro ou nora', OUTRO_PARENTE: 'Outro parente', NAO_PARENTE: 'Não parente',
};

export interface EnderecoDto {
  cep?: string | null;
  logradouro?: string | null;
  numeroLogradouro?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cidade?: string | null;
  estado?: string | null;
  ddd?: string | null;
}

export interface ImovelDto {
  uuid: string;
  microareaId: string;
  microareaCodigo: string;
  tipo: TipoImovel;
  endereco?: EnderecoDto | null;
  enderecoTexto: string;
  latitude?: number | null;
  longitude?: number | null;
  ativo: boolean;
  domicilios: number;
  familias: number;
  pessoas: number;
}

export interface MembroFamiliaDto {
  uuid: string;
  pacienteId: string;
  nome: string;
  dataNascimento?: string | null;
  parentesco: Parentesco;
  inicio: string;
  fim?: string | null;
  motivoSaida?: string | null;
  enderecoDiverge: boolean;
}

export interface FamiliaDto {
  uuid: string;
  domicilioId: string;
  responsavelId: string;
  responsavelNome: string;
  rendaFamiliar?: FaixaRendaFamiliar | null;
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
  membros: MembroFamiliaDto[];
  antigos: MembroFamiliaDto[];
}

export interface DomicilioDto {
  uuid: string;
  imovelId: string;
  identificacao?: string | null;
  situacao?: SituacaoMoradia | null;
  localizacao?: LocalizacaoDomicilio | null;
  comodos?: number | null;
  abastecimentoAgua?: AbastecimentoAgua | null;
  escoamentoSanitario?: EscoamentoSanitario | null;
  destinoLixo?: DestinoLixo | null;
  energiaEletrica?: boolean | null;
  ativo: boolean;
  familias: FamiliaDto[];
  anteriores: FamiliaDto[];
}

export interface ImovelDetalheDto {
  imovel: ImovelDto;
  areaId: string;
  areaCodigo: string;
  domicilios: DomicilioDto[];
}

export interface ImovelRequestDto {
  microareaId?: string | null;
  tipo: TipoImovel;
  endereco: EnderecoRequestDto;
  latitude?: number | null;
  longitude?: number | null;
  ativo: boolean;
}

export interface DomicilioRequestDto {
  imovelId: string;
  identificacao?: string | null;
  situacao?: SituacaoMoradia | null;
  localizacao?: LocalizacaoDomicilio | null;
  comodos?: number | null;
  abastecimentoAgua?: AbastecimentoAgua | null;
  escoamentoSanitario?: EscoamentoSanitario | null;
  destinoLixo?: DestinoLixo | null;
  energiaEletrica?: boolean | null;
  ativo: boolean;
}

export interface FamiliaRequestDto {
  domicilioId: string;
  responsavelId: string;
  rendaFamiliar?: FaixaRendaFamiliar | null;
  inicio?: string | null;
  membros?: { pacienteId: string; parentesco: Parentesco }[];
}
