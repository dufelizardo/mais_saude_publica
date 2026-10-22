/**
 * Fundo do mapa (ADR-0126). O padrão são os tiles do OpenStreetMap, com a atribuição exigida. A política de uso do OSM
 * não admite uso pesado em produção: a implantação troca a URL por um servidor de tiles próprio ou contratado.
 *
 * Para testes e ambientes sem internet, `localStorage['msp.mapa.tiles']` sobrepõe a URL; o valor `off` desliga o fundo
 * (os polígonos e pinos continuam).
 */
export const MAPA_TILES = {
  url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
  atribuicao: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
};

/** Centro e zoom antes de haver o que enquadrar: o Brasil inteiro. */
export const MAPA_CENTRO_PADRAO: [number, number] = [-15.8, -47.9];
export const MAPA_ZOOM_PADRAO = 4;

export function tilesDoMapa(): { url: string; atribuicao: string } | null {
  let local: string | null = null;
  try {
    local = localStorage.getItem('msp.mapa.tiles');
  } catch {
    local = null;
  }
  if (local === 'off') return null;
  return local ? { url: local, atribuicao: MAPA_TILES.atribuicao } : MAPA_TILES;
}
