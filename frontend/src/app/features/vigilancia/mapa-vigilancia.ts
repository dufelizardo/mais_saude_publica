import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { AgravoDto, CLASSIFICACOES, ClassificacaoCaso, MapaVigilanciaDto, SurtoDto } from '../../core/models/vigilancia';
import { VigilanciaService } from '../../core/services/vigilancia';
import { Mapa, PinoMapa, PoligonoMapa } from '../../shared/mapa/mapa';
import { CurvaEpidemica } from './curva-epidemica';
import { NovoSurto } from './surtos';

/** Faixas de cor das microáreas pelo número de casos (do mais claro ao mais escuro). */
const FAIXAS = ['#fde68a', '#fbbf24', '#f97316', '#dc2626'];
/** Microárea declarada no surto, ainda sem caso. */
const COR_DO_SURTO = '#a78bfa';

/**
 * Mapa dos casos (ADR-0130): as microáreas pintadas pelo número de casos no período, os pontos dos casos (só para quem
 * investiga, e nunca dos agravos sensíveis), a curva por semana epidemiológica e quantos casos ficaram sem local. Com o
 * surto (ADR-0131), só os casos dele, no período dele, e as microáreas declaradas em destaque; quem investiga abre um
 * surto com os casos de uma microárea.
 */
@Component({
  selector: 'app-mapa-vigilancia',
  imports: [Mapa, CurvaEpidemica],
  template: `
    <div class="filters" style="margin-bottom: var(--s-3);">
      <select class="sel" aria-label="Surto do mapa" [value]="surtoId()" (change)="surtoId.set($any($event.target).value)">
        <option value="">Todos os casos</option>
        @for (s of surtos(); track s.uuid) { <option [value]="s.uuid">Surto: {{ s.nome }}</option> }
      </select>
      @if (!surtoId()) {
        <select class="sel" aria-label="Agravo do mapa" [value]="agravoId()" (change)="agravoId.set($any($event.target).value)">
          <option value="">Todos os agravos</option>
          @for (a of agravos(); track a.uuid) { <option [value]="a.uuid">{{ a.nome }}</option> }
        </select>
        <select class="sel" aria-label="Período do mapa" [value]="semanas()" (change)="semanas.set(+$any($event.target).value)">
          @for (s of periodos; track s) { <option [value]="s">Últimas {{ s }} semanas</option> }
        </select>
      }
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
        @if (surtoId()) {
          <span class="cell-sub"><span aria-hidden="true" [style.background]="corDoSurto" style="display: inline-block; width: 12px; height: 12px; border-radius: 3px; vertical-align: middle;"></span> Microárea do surto, sem caso</span>
        }
      </div>
      @if (d.microareas.length) {
        <h4 class="mo-grupo">Casos por microárea</h4>
        <ul class="eq-prof" aria-label="Casos por microárea">
          @for (m of d.microareas; track m.microareaId) {
            <li class="mo-row">
              <span class="cell-main">Microárea {{ m.codigo }} <span class="cell-sub" style="display: inline;">· área {{ m.areaCodigo }}</span>@if (m.doSurto) { <span class="tag tag--purple">Do surto</span> }</span>
              <span class="row-actions">
                <span class="mono">{{ m.casos }}</span>
                @if (podeAbrirSurto() && m.notificacoesIds?.length) {
                  <button type="button" class="link-btn" (click)="surtoDaMicroarea(m.microareaId, m.notificacoesIds!)"
                    [attr.aria-label]="'Abrir surto com os casos da microárea ' + m.codigo">Abrir surto</button>
                }
              </span>
            </li>
          }
        </ul>
      }
      <h4 class="mo-grupo">Curva por semana epidemiológica</h4>
      <app-curva-epidemica [curva]="d.curva" />
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
  readonly surtos = input<SurtoDto[]>([]);
  /** O surto escolhido fora do mapa ("Ver no mapa", na gaveta do surto). */
  readonly surtoInicial = input<{ id: string; seq: number } | null>(null);
  readonly podeInvestigar = input(false);
  readonly abrir = output<string>();
  readonly abrirSurto = output<NovoSurto>();

  protected readonly classificacoes = CLASSIFICACOES;
  protected readonly listaClassificacoes = Object.keys(CLASSIFICACOES) as ClassificacaoCaso[];
  protected readonly periodos = [4, 8, 12, 26, 52];
  protected readonly corDoSurto = COR_DO_SURTO;

  protected readonly agravoId = signal('');
  protected readonly surtoId = signal('');
  protected readonly semanas = signal(12);
  protected readonly classificacao = signal('');
  protected readonly dados = signal<MapaVigilanciaDto | null>(null);
  protected readonly erro = signal(false);
  private seq = 0;

  /** Abrir surto pela microárea: quem investiga, com o agravo escolhido e fora de um surto. */
  protected readonly podeAbrirSurto = computed(() => this.podeInvestigar() && !!this.agravoId() && !this.surtoId());

  constructor() {
    effect(() => {
      const s = this.surtoInicial();
      if (s) this.surtoId.set(s.id);
    });
    effect(() => {
      const surto = this.surtoId();
      const ate = new Date();
      const de = new Date(ate.getTime() - (this.semanas() * 7 - 1) * 86400000);
      const filtro = surto ? { surtoId: surto, classificacao: this.classificacao() || undefined }
        : { agravoId: this.agravoId() || undefined, unidadeId: this.unidadeId() || undefined, de: iso(de), ate: iso(ate),
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
      descricao: `Microárea ${m.codigo} da área ${m.areaCodigo}: ${m.casos} ${m.casos === 1 ? 'caso' : 'casos'}${m.doSurto ? ', do surto' : ''}`,
      cor: m.casos ? FAIXAS[Math.min(FAIXAS.length - 1, Math.floor((m.casos - 1) / passo))] : COR_DO_SURTO,
    }));
  });

  protected readonly pinos = computed<PinoMapa[]>(() => (this.dados()?.pontos ?? []).map((p) => ({
    id: p.notificacaoId, lat: p.latitude, lng: p.longitude, sigla: '!', classe: 'unit-pin t-closed',
    descricao: `Caso de ${p.agravoCodigo}${p.classificacao ? ' · ' + CLASSIFICACOES[p.classificacao].rotulo : ''}`,
  })));

  protected escolher(id: string): void {
    if (this.dados()?.pontos.some((p) => p.notificacaoId === id)) this.abrir.emit(id);
  }

  protected surtoDaMicroarea(microareaId: string, notificacoesIds: string[]): void {
    this.abrirSurto.emit({ agravoId: this.agravoId(), notificacoesIds, microareasIds: [microareaId], seq: ++this.seq });
  }

  protected data(v: string): string {
    const [a, m, d] = v.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}

function iso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
