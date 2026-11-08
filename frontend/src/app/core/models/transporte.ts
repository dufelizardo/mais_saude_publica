/** Transporte, frota e plantão (ADR-0178). */
export type TipoVeiculo = 'USA' | 'USB' | 'MOTOLANCIA' | 'AEROMEDICO' | 'AMBULANCIA_TRANSPORTE' | 'VAN_SANITARIA' | 'MICRO_ONIBUS' | 'CARRO_ADMINISTRATIVO';
export type SituacaoVeiculo = 'DISPONIVEL' | 'EM_SERVICO' | 'MANUTENCAO' | 'INATIVO';
export type FuncaoTripulante = 'CONDUTOR' | 'TECNICO_ENFERMAGEM' | 'ENFERMEIRO' | 'MEDICO';

export const TIPOS_VEICULO: Record<TipoVeiculo, string> = {
  USA: 'USA · suporte avançado',
  USB: 'USB · suporte básico',
  MOTOLANCIA: 'Motolância',
  AEROMEDICO: 'Aeromédico',
  AMBULANCIA_TRANSPORTE: 'Ambulância de transporte',
  VAN_SANITARIA: 'Van sanitária',
  MICRO_ONIBUS: 'Micro-ônibus',
  CARRO_ADMINISTRATIVO: 'Carro administrativo',
};

export const SITUACOES_VEICULO: Record<SituacaoVeiculo, { rotulo: string; classe: string }> = {
  DISPONIVEL: { rotulo: 'Disponível', classe: 'ok' },
  EM_SERVICO: { rotulo: 'Em serviço', classe: 'purple' },
  MANUTENCAO: { rotulo: 'Em manutenção', classe: 'warn' },
  INATIVO: { rotulo: 'Inativo', classe: 'muted' },
};

export const FUNCOES_TRIPULANTE: Record<FuncaoTripulante, string> = {
  CONDUTOR: 'Condutor',
  TECNICO_ENFERMAGEM: 'Técnico de enfermagem',
  ENFERMEIRO: 'Enfermeiro',
  MEDICO: 'Médico',
};

/** Composição mínima por tipo, para orientar quem monta o plantão (a API confere). */
export const COMPOSICAO_MINIMA: Partial<Record<TipoVeiculo, string>> = {
  USB: 'condutor e técnico de enfermagem',
  USA: 'condutor, enfermeiro e médico',
  AEROMEDICO: 'enfermeiro e médico',
  MOTOLANCIA: 'técnico de enfermagem ou enfermeiro',
};

export interface TripulanteDto {
  profissionalId: string;
  nome: string;
  matricula: string;
  funcao: FuncaoTripulante;
}

export interface PlantaoDto {
  uuid: string;
  veiculoId: string;
  prefixo: string;
  tipo: TipoVeiculo;
  inicio: string;
  fim: string;
  vigente: boolean;
  observacao?: string | null;
  tripulacao: TripulanteDto[];
}

export interface VeiculoDto {
  uuid: string;
  placa: string;
  prefixo: string;
  tipo: TipoVeiculo;
  doSamu: boolean;
  modelo?: string | null;
  ano?: number | null;
  lugares?: number | null;
  maca: boolean;
  cadeiraDeRodas: boolean;
  quilometragem?: number | null;
  unidadeId: string;
  unidadeNome: string;
  situacao: SituacaoVeiculo;
  situacaoDesde?: string | null;
  observacao?: string | null;
  plantaoVigente?: PlantaoDto | null;
  historico?: { situacao: SituacaoVeiculo; inicio: string; motivo?: string | null }[] | null;
  plantoes?: PlantaoDto[] | null;
}

export interface VeiculoRequestDto {
  placa: string;
  prefixo: string;
  tipo: TipoVeiculo;
  modelo?: string | null;
  ano?: number | null;
  lugares?: number | null;
  maca: boolean;
  cadeiraDeRodas: boolean;
  quilometragem?: number | null;
  unidadeId: string;
  observacao?: string | null;
}

export interface ViaturaDisponivelDto {
  veiculoId: string;
  prefixo: string;
  tipo: TipoVeiculo;
  unidadeNome: string;
  plantaoAte: string;
}
