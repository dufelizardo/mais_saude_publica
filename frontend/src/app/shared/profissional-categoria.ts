import { ProfissionalResponseDto, QuadroProfissionalResponseDto } from '../core/models/profissional';

/**
 * Categoria do profissional pelo conselho de classe (ADR-0072) — usada na tela Profissionais e no cabeçalho do
 * perfil. `classe` é a cor do protótipo Profissionais.html (avatar e etiqueta).
 */
export interface CategoriaProfissional {
  id: 'MED' | 'ENF' | 'ODO' | 'FAR' | 'OUT' | 'APO';
  rotulo: string;
  etiqueta: string;
  classe: 'md' | 'enf' | 'tec' | 'acs' | 'adm';
}

export const CATEGORIAS_PROFISSIONAL: CategoriaProfissional[] = [
  { id: 'MED', rotulo: 'Médicos', etiqueta: 'Medicina', classe: 'md' },
  { id: 'ENF', rotulo: 'Enfermagem', etiqueta: 'Enfermagem', classe: 'enf' },
  { id: 'ODO', rotulo: 'Odontologia', etiqueta: 'Odontologia', classe: 'tec' },
  { id: 'FAR', rotulo: 'Farmácia', etiqueta: 'Farmácia', classe: 'acs' },
  { id: 'OUT', rotulo: 'Outros conselhos', etiqueta: 'Saúde', classe: 'tec' },
  { id: 'APO', rotulo: 'Apoio e administrativo', etiqueta: 'Apoio', classe: 'adm' },
];

const POR_CONSELHO: Record<string, CategoriaProfissional['id']> = { CRM: 'MED', COREN: 'ENF', CRO: 'ODO', CRF: 'FAR' };

/** CRM-SP, crm, "CRM/SP" → Médicos; sem conselho → Apoio e administrativo. */
export function categoriaDoProfissional(p: ProfissionalResponseDto): CategoriaProfissional {
  const sigla = (p.conselhoClasse ?? '').trim().toUpperCase().split(/[\s\-/]/)[0];
  const id = !sigla ? 'APO' : (POR_CONSELHO[sigla] ?? 'OUT');
  return CATEGORIAS_PROFISSIONAL.find((c) => c.id === id)!;
}

/**
 * Situação para a etiqueta e o ponto do avatar. Licença médica não é nomeada — motivo de saúde é dado sensível
 * (ADR-0072): aparece só "Afastado até".
 */
export function situacaoDoProfissional(i: QuadroProfissionalResponseDto): { rotulo: string; classe: string; ponto: '' | 'away' | 'leave' | 'off' } {
  const p = i.profissional;
  if (!p.ativo) return { rotulo: `Desligado${p.dataDesligamento ? ' em ' + dataCurta(p.dataDesligamento, true) : ''}`, classe: 'muted', ponto: 'off' };
  if (i.afastamento?.tipo === 'FERIAS') return { rotulo: `Em férias até ${dataCurta(i.afastamento.dataFim)}`, classe: 'warn', ponto: 'away' };
  if (i.afastamento) return { rotulo: `Afastado até ${dataCurta(i.afastamento.dataFim)}`, classe: 'alert', ponto: 'leave' };
  if (!i.lotacao) return { rotulo: 'Sem lotação vigente', classe: 'warn', ponto: 'off' };
  return { rotulo: 'Ativo', classe: 'ok', ponto: '' };
}

function dataCurta(iso: string, comAno = false): string {
  const [a, m, d] = iso.slice(0, 10).split('-');
  return comAno ? `${d}/${m}/${a}` : `${d}/${m}`;
}
