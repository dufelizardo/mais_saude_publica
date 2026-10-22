import { area } from '@turf/area';
import { difference } from '@turf/difference';
import { featureCollection, feature } from '@turf/helpers';
import { intersect } from '@turf/intersect';
import type { Feature, MultiPolygon, Polygon } from 'geojson';

/**
 * Contas de geometria na tela (ADR-0126), só para avisar enquanto se desenha. Quem decide é o backend (JTS, ADR-0125);
 * a mesma folga de ruído: menos de 12 m² não conta.
 */
const RUIDO_M2 = 12;

type Poligonal = Polygon | MultiPolygon;

function ler(texto: string | null | undefined): Feature<Poligonal> | null {
  if (!texto) return null;
  try {
    const g = JSON.parse(texto) as Poligonal;
    return g.type === 'Polygon' || g.type === 'MultiPolygon' ? feature(g) : null;
  } catch {
    return null;
  }
}

/** Metros quadrados de `a` que ficam fora de `b`. */
export function foraDe(a: string, b: string): number {
  const fa = ler(a);
  const fb = ler(b);
  if (!fa || !fb) return 0;
  const resto = difference(featureCollection([fa, fb]));
  return resto ? area(resto) : 0;
}

/** Metros quadrados em comum entre `a` e `b`. */
export function emComum(a: string, b: string): number {
  const fa = ler(a);
  const fb = ler(b);
  if (!fa || !fb) return 0;
  const comum = intersect(featureCollection([fa, fb]));
  return comum ? area(comum) : 0;
}

export function relevante(m2: number): boolean {
  return m2 > RUIDO_M2;
}

/** Área em hectares, para mostrar ao lado do desenho. */
export function hectares(texto: string | null | undefined): number | null {
  const f = ler(texto);
  return f ? Math.round(area(f) / 100) / 100 : null;
}

/**
 * O texto de um arquivo importado vira a geometria a guardar. GeoJSON passa como veio (o backend aceita Feature);
 * KML é convertido, e os polígonos dele viram um Polygon ou um MultiPolygon.
 */
export async function geometriaDoArquivo(nome: string, texto: string): Promise<string> {
  if (!/\.kml$/i.test(nome)) return texto.trim();
  const { kml } = await import('@tmcw/togeojson');
  const doc = new DOMParser().parseFromString(texto, 'text/xml');
  const poligonos: Polygon['coordinates'][] = [];
  for (const f of kml(doc).features) {
    const g = f.geometry;
    if (g?.type === 'Polygon') poligonos.push(semAltitude(g.coordinates));
    else if (g?.type === 'MultiPolygon') poligonos.push(...g.coordinates.map(semAltitude));
  }
  if (!poligonos.length) throw new Error('O KML não tem polígono.');
  return JSON.stringify(poligonos.length === 1 ? { type: 'Polygon', coordinates: poligonos[0] } : { type: 'MultiPolygon', coordinates: poligonos });
}

/** O KML traz altitude (lon, lat, alt); o GeoJSON guardado fica em 2D. */
function semAltitude(aneis: Polygon['coordinates']): Polygon['coordinates'] {
  return aneis.map((anel) => anel.map((p) => [p[0], p[1]]));
}
