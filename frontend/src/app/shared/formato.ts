/** Formatos de exibição comuns às telas de RH (ADR-0073). */

const MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

/** 1234.5 → "R$ 1.234,50"; ausente → "—". */
export function moeda(valor: number | null | undefined): string {
  return valor === null || valor === undefined ? '—' : MOEDA.format(valor);
}

/** "2026-09-30" → "30/09/2026"; ausente → "—". */
export function dataBr(iso: string | null | undefined): string {
  if (!iso) return '—';
  const [a, m, d] = iso.slice(0, 10).split('-');
  return `${d}/${m}/${a}`;
}

/** Hoje como "AAAA-MM-DD", no fuso do navegador. */
export function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

/** Setas esquerda/direita entre abas (`page-tabs`), com o foco acompanhando. */
export function proximaAba<T extends string>(event: KeyboardEvent, abas: readonly T[], atual: T): T | null {
  if (event.key !== 'ArrowRight' && event.key !== 'ArrowLeft') return null;
  event.preventDefault();
  const i = abas.indexOf(atual);
  const proxima = abas[(i + (event.key === 'ArrowRight' ? 1 : abas.length - 1)) % abas.length];
  setTimeout(() => document.getElementById('tab-' + proxima)?.focus());
  return proxima;
}
