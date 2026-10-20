import { Component, ElementRef, OnDestroy, afterNextRender, effect, input, output, signal, viewChild } from '@angular/core';
import type * as Leaflet from 'leaflet';
import { MAPA_CENTRO_PADRAO, MAPA_ZOOM_PADRAO, tilesDoMapa } from '../../core/config/mapa';

/** Um polígono do mapa, com a geometria em GeoJSON (texto, WGS84). */
export interface PoligonoMapa {
  id: string;
  geometria: string;
  /** Rótulo permanente no centro (ex.: o código da microárea). */
  rotulo?: string;
  /** Nome acessível e dica (ex.: "Microárea 07, ACS Carlos"). */
  descricao: string;
  tipo: 'area' | 'microarea' | 'sem-acs' | 'vizinha';
}

/** Um pino (unidade de saúde). */
export interface PinoMapa {
  id: string;
  lat: number;
  lng: number;
  /** Texto curto dentro do pino (sigla). */
  sigla: string;
  descricao: string;
  /** Classes do marcador da lista (`unit-pin t-…`). */
  classe: string;
  destaque?: boolean;
}

/** O que está sendo desenhado: a geometria de partida (ou nula) e o nome, para as instruções. */
export interface EdicaoMapa {
  id: string;
  geometria: string | null;
  nome: string;
}

/**
 * Mapa compartilhado (ADR-0126): Leaflet com os tiles configurados (OpenStreetMap por padrão, com a atribuição), carregado
 * sob demanda junto com o CSS. Mostra polígonos e pinos, avisa a seleção, marca um ponto por clique e, em edição, desenha
 * e edita polígonos com o leaflet-geoman. A lista ao lado continua sendo a navegação principal; aqui polígonos e pinos
 * também recebem foco e nome acessível.
 */
@Component({
  selector: 'app-mapa',
  template: `
    <div class="mapa" [style.height.px]="altura()">
      <div #alvo class="mapa__alvo" role="application" [attr.aria-label]="rotulo()"></div>
      @if (!pronto()) { <div class="mapa__carregando">Carregando o mapa…</div> }
    </div>
  `,
})
export class Mapa implements OnDestroy {
  readonly poligonos = input<PoligonoMapa[]>([]);
  readonly pinos = input<PinoMapa[]>([]);
  readonly selecionado = input<string | null>(null);
  readonly altura = input(320);
  readonly rotulo = input('Mapa');
  /** Em edição, o desenho de partida; nulo fora da edição. */
  readonly edicao = input<EdicaoMapa | null>(null);
  /** Um clique no mapa vira um ponto (marcar a unidade). */
  readonly marcarPonto = input(false);

  readonly selecionar = output<string>();
  readonly ponto = output<{ lat: number; lng: number }>();
  /** O desenho mudou: Polygon ou MultiPolygon em GeoJSON, ou nulo sem polígono. */
  readonly desenho = output<string | null>();

  private readonly alvo = viewChild.required<ElementRef<HTMLDivElement>>('alvo');
  protected readonly pronto = signal(false);

  private L?: typeof Leaflet;
  private mapa?: Leaflet.Map;
  private camadaPoligonos?: Leaflet.FeatureGroup;
  private camadaPinos?: Leaflet.FeatureGroup;
  private camadaEdicao?: Leaflet.FeatureGroup;
  private edicaoAtual: string | null = null;
  private enquadrado = false;
  /** Força um novo enquadramento (a lista virou mapa, por exemplo). */
  readonly reenquadrar = input(0);

  constructor() {
    afterNextRender(() => void this.iniciar());
    effect(() => {
      const poligonos = this.poligonos();
      const pinos = this.pinos();
      const selecionado = this.selecionado();
      if (this.pronto()) this.desenhar(poligonos, pinos, selecionado);
    });
    effect(() => {
      this.reenquadrar();
      if (this.pronto()) {
        this.ultimoConjunto = undefined;
        this.mapa!.invalidateSize();
        this.desenhar(this.poligonos(), this.pinos(), this.selecionado());
      }
    });
    effect(() => {
      const edicao = this.edicao();
      if (this.pronto()) this.editar(edicao);
    });
    effect(() => {
      const marcar = this.marcarPonto();
      if (this.pronto()) this.alvo().nativeElement.classList.toggle('mapa__alvo--marcar', marcar);
    });
  }

  ngOnDestroy(): void {
    this.mapa?.remove();
  }

  private async iniciar(): Promise<void> {
    carregarCss('/leaflet/leaflet.css');
    carregarCss('/leaflet/leaflet-geoman.css');
    const modulo = await import('leaflet');
    const L = (this.L = ((modulo as unknown as { default?: typeof Leaflet }).default ?? modulo) as typeof Leaflet);
    await import('@geoman-io/leaflet-geoman-free');
    const mapa = (this.mapa = L.map(this.alvo().nativeElement, { center: MAPA_CENTRO_PADRAO, zoom: MAPA_ZOOM_PADRAO, keyboard: true }));
    const tiles = tilesDoMapa();
    if (tiles) L.tileLayer(tiles.url, { attribution: tiles.atribuicao, maxZoom: 19 }).addTo(mapa);
    else mapa.attributionControl.addAttribution('Sem fundo de mapa');
    this.camadaPoligonos = L.featureGroup().addTo(mapa);
    this.camadaPinos = L.featureGroup().addTo(mapa);
    this.camadaEdicao = L.featureGroup().addTo(mapa);
    mapa.pm.setLang('pt_br');
    mapa.on('click', (e: Leaflet.LeafletMouseEvent) => {
      if (this.marcarPonto()) this.ponto.emit({ lat: arredondar(e.latlng.lat), lng: arredondar(e.latlng.lng) });
    });
    for (const evento of ['pm:create', 'pm:remove', 'pm:cut']) {
      mapa.on(evento, (e: { layer?: Leaflet.Layer }) => {
        if (evento === 'pm:create' && e.layer) {
          this.camadaPoligonos?.removeLayer(e.layer);
          this.camadaEdicao?.addLayer(e.layer);
          this.ouvirEdicao(e.layer);
        }
        this.emitirDesenho();
      });
    }
    this.pronto.set(true);
    // O contêiner pode ter mudado de tamanho depois do primeiro desenho (painel, aba).
    setTimeout(() => mapa.invalidateSize(), 0);
  }

  // ── Camadas ─────────────────────────────────────────────────────────────────────────────────────

  private desenhar(poligonos: PoligonoMapa[], pinos: PinoMapa[], selecionado: string | null): void {
    const L = this.L!;
    this.camadaPoligonos!.clearLayers();
    this.camadaPinos!.clearLayers();
    const editando = this.edicao()?.id;
    for (const p of poligonos) {
      if (p.id === editando) continue;
      const geo = ler(p.geometria);
      if (!geo) continue;
      const camada = L.geoJSON(geo as GeoJSON.GeoJsonObject, { style: estilo(p.tipo, p.id === selecionado), pmIgnore: true } as Leaflet.GeoJSONOptions);
      camada.eachLayer((l) => {
        if (p.rotulo) l.bindTooltip(p.rotulo, { permanent: true, direction: 'center', className: 'mapa__rotulo' });
        else l.bindTooltip(p.descricao);
        if (p.tipo !== 'vizinha' && p.tipo !== 'area') {
          l.on('click', () => this.selecionar.emit(p.id));
        }
        l.on('add', () => acessivel((l as Leaflet.Path).getElement?.() as HTMLElement | undefined, p.descricao, () => this.selecionar.emit(p.id),
          p.tipo !== 'vizinha' && p.tipo !== 'area'));
      });
      camada.addTo(this.camadaPoligonos!);
      if (p.tipo === 'area') camada.bringToBack();
    }
    for (const p of pinos) {
      const icone = L.divIcon({ className: '', html: `<span class="${p.classe}${p.destaque ? ' unit-pin--destaque' : ''}">${escapar(p.sigla)}</span>`, iconSize: [28, 28], iconAnchor: [14, 14] });
      const m = L.marker([p.lat, p.lng], { icon: icone, title: p.descricao, alt: p.descricao, keyboard: true, pmIgnore: true } as Leaflet.MarkerOptions);
      m.on('click', () => this.selecionar.emit(p.id));
      m.on('keypress', (e: Leaflet.LeafletKeyboardEvent) => {
        if (e.originalEvent.key === 'Enter') this.selecionar.emit(p.id);
      });
      m.addTo(this.camadaPinos!);
    }
    this.enquadrar([...poligonos.map((p) => p.id), ...pinos.map((p) => p.id)].join('|'));
  }

  /** Enquadra tudo quando o conjunto de camadas muda (outra área, outro filtro); a seleção não mexe na vista. */
  private ultimoConjunto: string | undefined;
  private enquadrar(conjunto: string): void {
    if (this.enquadrado && conjunto === this.ultimoConjunto) return;
    this.ultimoConjunto = conjunto;
    // Com polígonos, eles mandam no enquadramento (os pinos da rede podem estar longe); sem eles, os pinos.
    const limites = this.L!.latLngBounds([]);
    for (const g of [this.camadaPoligonos!, this.camadaEdicao!]) {
      const b = g.getBounds();
      if (b.isValid()) limites.extend(b);
    }
    if (!limites.isValid()) {
      const b = this.camadaPinos!.getBounds();
      if (b.isValid()) limites.extend(b);
    }
    if (limites.isValid()) {
      this.mapa!.fitBounds(limites.pad(0.15), { maxZoom: 16 });
      this.enquadrado = true;
    }
  }

  // ── Edição ──────────────────────────────────────────────────────────────────────────────────────

  private editar(edicao: EdicaoMapa | null): void {
    const mapa = this.mapa!;
    const chave = edicao ? `${edicao.id}` : null;
    if (chave === this.edicaoAtual) return;
    this.edicaoAtual = chave;
    this.camadaEdicao!.clearLayers();
    if (!edicao) {
      mapa.pm.removeControls();
      mapa.pm.disableDraw();
      this.desenhar(this.poligonos(), this.pinos(), this.selecionado());
      return;
    }
    const geo = edicao.geometria ? ler(edicao.geometria) : null;
    if (geo) {
      this.L!.geoJSON(geo as GeoJSON.GeoJsonObject).eachLayer((l) => {
        this.camadaEdicao!.addLayer(l);
        this.ouvirEdicao(l);
      });
    }
    mapa.pm.addControls({
      position: 'topleft',
      drawMarker: false, drawCircleMarker: false, drawPolyline: false, drawRectangle: true, drawCircle: false, drawText: false,
      drawPolygon: true, editMode: true, dragMode: true, cutPolygon: true, removalMode: true, rotateMode: false,
    });
    mapa.pm.setGlobalOptions({ layerGroup: this.camadaEdicao, snappable: true, snapDistance: 15 } as Leaflet.PM.GlobalOptions);
    this.desenhar(this.poligonos(), this.pinos(), this.selecionado());
    const b = this.camadaEdicao!.getBounds();
    if (b.isValid()) mapa.fitBounds(b.pad(0.3), { maxZoom: 17 });
  }

  private ouvirEdicao(l: Leaflet.Layer): void {
    l.on('pm:edit pm:dragend pm:cut pm:remove', () => this.emitirDesenho());
  }

  private emitirDesenho(): void {
    const poligonos: GeoJSON.Position[][][] = [];
    this.camadaEdicao!.eachLayer((l) => {
      const g = (l as Leaflet.Polygon).toGeoJSON?.()?.geometry;
      if (g?.type === 'Polygon') poligonos.push(g.coordinates);
      else if (g?.type === 'MultiPolygon') poligonos.push(...g.coordinates);
    });
    if (!poligonos.length) this.desenho.emit(null);
    else this.desenho.emit(JSON.stringify(poligonos.length === 1
      ? { type: 'Polygon', coordinates: poligonos[0] }
      : { type: 'MultiPolygon', coordinates: poligonos }));
  }
}

// ── Apoio ─────────────────────────────────────────────────────────────────────────────────────────

function carregarCss(href: string): void {
  if (document.querySelector(`link[href="${href}"]`)) return;
  const link = document.createElement('link');
  link.rel = 'stylesheet';
  link.href = href;
  document.head.appendChild(link);
}

function ler(texto: string): unknown {
  try {
    return JSON.parse(texto);
  } catch {
    return null;
  }
}

function arredondar(v: number): number {
  return Math.round(v * 1e6) / 1e6;
}

function escapar(texto: string): string {
  return texto.replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]!);
}

/** Polígono com foco, nome e Enter, além do clique. */
function acessivel(el: HTMLElement | undefined, nome: string, acao: () => void, interativo: boolean): void {
  if (!el) return;
  el.setAttribute('aria-label', nome);
  el.setAttribute('data-nome', nome);
  if (!interativo) return;
  el.setAttribute('role', 'button');
  el.setAttribute('tabindex', '0');
  el.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      acao();
    }
  });
}

/** Cores do protótipo: a área em contorno, a microárea colorida, a sem ACS em âmbar, a vizinha apagada. */
function estilo(tipo: PoligonoMapa['tipo'], selecionado: boolean): Leaflet.PathOptions {
  const base: Record<PoligonoMapa['tipo'], Leaflet.PathOptions> = {
    area: { color: '#1d4ed8', weight: 3, fill: false, dashArray: '6 4' },
    microarea: { color: '#0f766e', weight: 2, fillColor: '#14b8a6', fillOpacity: 0.25 },
    'sem-acs': { color: '#b45309', weight: 2, fillColor: '#f59e0b', fillOpacity: 0.3 },
    vizinha: { color: '#64748b', weight: 1, fillColor: '#94a3b8', fillOpacity: 0.1 },
  };
  return selecionado ? { ...base[tipo], weight: 4, fillOpacity: 0.45 } : base[tipo];
}
