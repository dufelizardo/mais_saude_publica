// ── Animais das zoonoses, fatia V4b da Vigilância (ADR-0163) ──

export type EspecieAnimal = 'CAO' | 'GATO' | 'MORCEGO' | 'PRIMATA' | 'HERBIVORO' | 'OUTRA';
export type DesfechoObservacaoAnimal = 'SADIO' | 'MORTO' | 'DESAPARECIDO' | 'SUSPEITO_RAIVA';
export type TipoExameAnimal = 'RAIVA_IMUNOFLUORESCENCIA' | 'LEISHMANIOSE_TR_DPP' | 'LEISHMANIOSE_ELISA' | 'OUTRO';
export type ResultadoExameAnimal = 'REAGENTE' | 'NAO_REAGENTE' | 'INDETERMINADO';
export type DesfechoLeishmanioseCanina = 'EUTANASIA' | 'TRATAMENTO_VETERINARIO' | 'ACOMPANHAMENTO' | 'OBITO';

export const ESPECIES_ANIMAL: Record<EspecieAnimal, string> = {
  CAO: 'Cão',
  GATO: 'Gato',
  MORCEGO: 'Morcego',
  PRIMATA: 'Primata',
  HERBIVORO: 'Herbívoro',
  OUTRA: 'Outra',
};

export const DESFECHOS_OBSERVACAO: Record<DesfechoObservacaoAnimal, { rotulo: string; classe: string }> = {
  SADIO: { rotulo: 'Sadio aos 10 dias', classe: 'ok' },
  MORTO: { rotulo: 'Morto', classe: 'alert' },
  DESAPARECIDO: { rotulo: 'Desaparecido', classe: 'warn' },
  SUSPEITO_RAIVA: { rotulo: 'Suspeito de raiva', classe: 'alert' },
};

export const TIPOS_EXAME_ANIMAL: Record<TipoExameAnimal, string> = {
  RAIVA_IMUNOFLUORESCENCIA: 'Raiva · imunofluorescência',
  LEISHMANIOSE_TR_DPP: 'Leishmaniose · teste rápido (TR DPP)',
  LEISHMANIOSE_ELISA: 'Leishmaniose · ELISA',
  OUTRO: 'Outro',
};

export const RESULTADOS_EXAME_ANIMAL: Record<ResultadoExameAnimal, { rotulo: string; classe: string }> = {
  REAGENTE: { rotulo: 'Reagente', classe: 'alert' },
  NAO_REAGENTE: { rotulo: 'Não reagente', classe: 'ok' },
  INDETERMINADO: { rotulo: 'Indeterminado', classe: 'warn' },
};

export const DESFECHOS_LEISHMANIOSE: Record<DesfechoLeishmanioseCanina, string> = {
  EUTANASIA: 'Eutanásia',
  TRATAMENTO_VETERINARIO: 'Tratamento veterinário',
  ACOMPANHAMENTO: 'Acompanhamento',
  OBITO: 'Óbito',
};

export interface AnimalZoonosesDto {
  uuid: string;
  especie: EspecieAnimal;
  nome: string | null;
  descricao: string | null;
  tutorNome: string | null;
  tutorTelefone: string | null;
  imovelId: string | null;
  endereco: string;
  microareaCodigo: string | null;
  areaNome: string | null;
  emObservacao: boolean;
  registradoEm: string;
}

export interface ObservacaoAnimalDto {
  uuid: string;
  animalId: string;
  especie: EspecieAnimal;
  animalNome: string | null;
  tutorNome: string | null;
  tutorTelefone: string | null;
  endereco: string;
  notificacaoId: string | null;
  dataAgressao: string;
  inicio: string;
  fimPrevisto: string;
  desfecho: DesfechoObservacaoAnimal | null;
  desfechoEm: string | null;
  observacao: string | null;
  vencida: boolean;
}

export interface ExameAnimalDto {
  uuid: string;
  animalId: string;
  especie: EspecieAnimal;
  animalNome: string | null;
  endereco: string;
  observacaoId: string | null;
  tipo: TipoExameAnimal;
  dataColeta: string;
  resultado: ResultadoExameAnimal | null;
  resultadoEm: string | null;
  desfechoLeishmaniose: DesfechoLeishmanioseCanina | null;
  observacao: string | null;
}

export interface VacinacaoAntirrabicaDto {
  ano: number;
  caes: number;
  gatos: number;
  areas: { areaId: string; areaNome: string; caes: number; gatos: number }[];
  registros: { uuid: string; data: string; areaNome: string; microareaCodigo: string; posto: string; caes: number; gatos: number; responsavel: string }[];
}
