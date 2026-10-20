import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { AgravoDto, CLASSIFICACOES, ClassificacaoCaso, MapaVigilanciaDto } from '../../core/models/vigilancia';
import { VigilanciaService } from '../../core/services/vigilancia';
import { Mapa, PinoMapa, PoligonoMapa } from '../../shared/mapa/mapa';

/** Faixas de cor das microáreas pelo número de casos (do mais claro ao mais escuro). */
const FAIXAS = ['#fde68a', '#fbbf24', '#f97316', '#dc2626'];

/**
 * Mapa dos casos (ADR-0130): as microáreas pintadas pelo número de casos no período, os pontos dos casos (só para quem
 * investiga, e nunca dos agravos sensíveis), a curva por semana epidemiológica e quantos casos ficaram sem local.
 */
@Component({
  selector: 'app-mapa-vigilancia',
  imports: [Mapa],
  template: `
    <div class="filters" style="margin-bottom: var(--s-3);">
      <select class="sel" aria-label="Agravo do mapa" [value]="agravoId()" (change)="agravoId.set($any($event.target).value)">
        <option value="">Todos os agravos</option>
        @for (a of agravos(); track a.uuid) { <option [value]="a.uuid">{{ a.nome }}</option> }
      </select>
      <select class="sel" aria-label="Período do mapa" [value]="semanas()" (change)="semanas.set(+$any($event.target).value)">
        @for (s of periodos; track s) { <option [value]="s">Últimas {{ s }} semanas</option> }
      </select>
      <select class="sel" aria-label="Classificação do mapa" [value]="classificacao()" (change)="classificacao.set($any($event.target).value)">
        <option value="">Todas as classificações</option>
        @for (c of listaClassificacoes; track c) { <option [value]="c">{{ classificacoes[c].rotulo }}</option> }
      </select>
    </div>
    @if (dados(); as d) {
      <p class="cell-sub" aria-label="Resumo do mapa">
        {{ d.total }} {{ d.total === 1 ? 'caso' : 'casos' }} de {{ data(d.de) }} a {{ data(d.ate) }}@if (d.semLocal) { · {{ d.semLocal }} sem local }@if (!d.pontosVisiveis) { · os pontos dos casos ficam só para quem investiga }
      </p>
      <app-mapa [poligonos]="poligonos()" [pinos]="pinos()" [altura]="380" rotulo="Mapa dos casos" (selecionar)="escolher($event)" />
      <div class="legenda" aria-label="Legenda do mapa" style="display: flex; gap: var(--s-3); flex-wrap: wrap; margin-top: var(--s-2);">
        @for (f of faixas(); track f.cor) {
          <span class="cell-sub"><span aria-hidden="true" [style.background]="f.cor" style="display: inline-block; width: 12px; height: 12px; border-radius: 3px; vertical-align: middle;"></span> {{ f.rotulo }}</span>
        }
      </div>
      @if (d.microareas.length) {
        <h4 class="mo-grupo">Casos por microárea</h4>
        <ul class="eq-prof" aria-label="Casos por microárea">
          @for (m of d.microareas; track m.microareaId) {
            <li class="mo-row"><span class="cell-main">Microárea {{ m.codigo }} <span class="cell-sub" style="display: inline;">· área {{ m.areaCodigo }}</span></span><span class="mono">{{ m.casos }}</span></li>
          }
        </ul>
      }
      <h4 class="mo-grupo">Curva por semana epidemiológica</h4>
      <div class="curva" role="list" aria-label="Casos por semana epidemiológica"
        style="display: flex; align-items: flex-end; gap: 4px; height: 120px; border-bottom: 1px solid var(--ink-200); padding-top: var(--s-2);">
        @for (s of d.curva; track s.ano + '-' + s.semana) {
          <div role="listitem" [attr.aria-label]="'Semana ' + s.semana + '/' + s.ano + ': ' + s.casos + (s.casos === 1 ? ' caso' : ' casos')"
            [title]="'Semana ' + s.semana + '/' + s.ano + ' (de ' + data(s.inicio) + '): ' + s.casos"
            style="flex: 1; display: flex; flex-direction: column; justify-content: flex-end; align-items: center; height: 100%;">
            @if (s.casos) { <span class="mono" style="font-size: 11px;">{{ s.casos }}</span> }
            <div [style.height.%]="altura(s.casos)" style="width: 100%; min-height: 1px; background: var(--alert); border-radius: 2px 2px 0 0;"></div>
          </div>
        }
      </div>
      <div style="display: flex; justify-content: space-between;" class="cell-sub">
        @if (d.curva.length) { <span>SE {{ d.curva[0].semana }}/{{ d.curva[0].ano }}</span><span>SE {{ d.curva[d.curva.length - 1].semana }}/{{ d.curva[d.curva.length - 1].ano }}</span> }
      </div>
    } @else if (erro()) {
      <div class="box box--err" role="alert"><strong>Não foi possível carregar o mapa.</strong></div>
    } @else {
      <p class="cell-sub">Carregando o mapa…</p>
    }
  `,
})
export class MapaVigilancia {
  private readonly service = inject(VigilanciaService);

  readonly agravos = input<AgravoDto[]>([]);
  readonly unidadeId = input('');
  readonly abrir = output<string>();

  protected readonly classificacoes = CLASSIFICACOES;
  protected readonly listaClassificacoes = Object.keys(CLASSIFICACOES) as ClassificacaoCaso[];
  protected readonly periodos = [4, 8, 12, 26, 52];

  protected readonly agravoId = signal('');
  protected readonly semanas = signal(12);
  protected readonly classificacao = signal('');
  protected readonly dados = signal<MapaVigilanciaDto | null>(null);
  protected readonly erro = signal(false);

  constructor() {
    effect(() => {
      const ate = new Date();
      const de = new Date(ate.getTime() - (this.semanas() * 7 - 1) * 86400000);
      const filtro = { agravoId: this.agravoId() || undefined, unidadeId: this.unidadeId() || undefined, de: iso(de), ate: iso(ate),
        classificacao: this.classificacao() || undefined };
      this.erro.set(false);
      this.service.mapa(filtro).pipe(catchError(() => {
        this.erro.set(true);
        return of(null);
      })).subscribe((d) => this.dados.set(d));
    });
  }

  private readonly maximo = computed(() => Math.max(0, ...(this.dados()?.microareas ?? []).map((m) => m.casos)));

  protected readonly faixas = computed(() => {
    const max = this.maximo();
    if (!max) return [];
    const passo = Math.max(1, Math.ceil(max / FAIXAS.length));
    return FAIXAS.slice(0, Math.ceil(max / passo)).map((cor, i) => {
      const de = i * passo + 1;
      const ate = Math.min(max, (i + 1) * passo);
      return { cor, rotulo: de === ate ? `${de} ${de === 1 ? 'caso' : 'casos'}` : `${de} a ${ate} casos` };
    });
  });

  protected readonly poligonos = computed<PoligonoMapa[]>(() => {
    const max = this.maximo();
    const passo = Math.max(1, Math.ceil(max / FAIXAS.length));
    return (this.dados()?.microareas ?? []).filter((m) => m.geometria).map((m) => ({
      id: m.microareaId, geometria: m.geometria!, rotulo: `${m.codigo} · ${m.casos}`, tipo: 'microarea' as const,
      descricao: `Microárea ${m.codigo} da área ${m.areaCodigo}: ${m.casos} ${m.casos === 1 ? 'caso' : 'casos'}`,
      cor: FAIXAS[Math.min(FAIXAS.length - 1, Math.floor((m.casos - 1) / passo))],
    }));
  });

  protected readonly pinos = computed<PinoMapa[]>(() => (this.dados()?.pontos ?? []).map((p) => ({
    id: p.notificacaoId, lat: p.latitude, lng: p.longitude, sigla: '!', classe: 'unit-pin t-closed',
    descricao: `Caso de ${p.agravoCodigo}${p.classificacao ? ' · ' + CLASSIFICACOES[p.classificacao].rotulo : ''}`,
  })));

  protected escolher(id: string): void {
    if (this.dados()?.pontos.some((p) => p.notificacaoId === id)) this.abrir.emit(id);
  }

  protected altura(casos: number): number {
    const max = Math.max(1, ...(this.dados()?.curva ?? []).map((s) => s.casos));
    return casos ? Math.max(4, (casos / max) * 85) : 0;
  }

  protected data(v: string): string {
    const [a, m, d] = v.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}

function iso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
