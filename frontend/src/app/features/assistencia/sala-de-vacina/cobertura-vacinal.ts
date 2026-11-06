import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { EquipeResumoDto } from '../../../core/models/equipe';
import {
  CLASSES_ABANDONO,
  CoberturaRecorteDto,
  CoberturaVacinalDto,
  classeCobertura,
  percentual,
} from '../../../core/models/imunizacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { EquipeService } from '../../../core/services/equipe';
import { ImunizacaoService } from '../../../core/services/imunizacao';

/**
 * Cobertura vacinal (ADR-0149), pelo cadastro: por vacina de referência, a coorte, as vacinadas e a cobertura contra a
 * meta, a homogeneidade (entre vacinas e entre territórios) e o abandono. Sem unidade, as unidades do acesso; com a
 * unidade, por equipe; com a equipe, por microárea. Clicar num recorte desce um nível. Só números.
 */
@Component({
  selector: 'app-cobertura-vacinal',
  template: `
    <section class="card" style="padding: var(--s-5);" aria-label="Cobertura vacinal">
      <div class="cob-filtros">
        @if (unidadeId()) {
          <div class="dw-field"><label for="f-cob-equipe">Equipe</label>
            <select id="f-cob-equipe" [value]="equipeId()" (change)="trocarEquipe($any($event.target).value)">
              <option value="">Todas as equipes</option>
              @for (e of equipes(); track e.uuid) { <option [value]="e.uuid">{{ e.nome }}</option> }
            </select>
          </div>
        }
        @if (equipeId()) {
          <div class="dw-field"><label for="f-cob-microarea">Microárea</label>
            <select id="f-cob-microarea" [value]="microareaId()" (change)="microareaId.set($any($event.target).value)">
              <option value="">Todas as microáreas</option>
              @for (m of microareas(); track m.id) { <option [value]="m.id">{{ m.nome }}</option> }
            </select>
          </div>
        }
        <div class="dw-field"><label for="f-cob-data">Referência</label><input id="f-cob-data" type="date" [value]="data()" (change)="data.set($any($event.target).value)" /></div>
      </div>
      @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
      @if (cobertura(); as c) {
        <p class="cell-sub">{{ c.abrangencia }} · crianças adscritas, coorte de 1 a 12 meses depois da idade recomendada de cada dose.</p>
        <div class="kpis" style="margin: var(--s-3) 0 var(--s-4);">
          <div class="kpi"><div class="kpi__value" aria-label="Homogeneidade entre vacinas">{{ pct(c.homogeneidade) }}</div><div class="kpi__label">Homogeneidade entre vacinas</div></div>
          <div class="kpi"><div class="kpi__value">{{ c.vacinasNaMeta }} de {{ c.vacinasAvaliadas }}</div><div class="kpi__label">Vacinas na meta</div></div>
          <div class="kpi"><div class="kpi__value">{{ c.abandonoAlto }}</div><div class="kpi__label">Vacinas com abandono alto (10% ou mais)</div></div>
        </div>
        <div class="tbl-wrap cob-tabela">
          <table aria-label="Cobertura por vacina">
            <thead><tr><th scope="col">Vacina</th><th scope="col">Coorte</th><th scope="col">Vacinadas</th><th scope="col">Cobertura</th><th scope="col">Meta</th>
              @if (c.recorteRotulo) { <th scope="col">Homogeneidade</th> }<th scope="col">Abandono</th></tr></thead>
            <tbody>
              @for (v of c.vacinas; track v.esquemaId) {
                <tr>
                  <td><span class="cell-main">{{ v.sigla }} · {{ v.doseRotulo }}</span></td>
                  <td class="mono">{{ v.coorte }}</td>
                  <td class="mono">{{ v.vacinados }}</td>
                  <td><span [class]="classe(v.cobertura, v.meta)">{{ pct(v.cobertura) }}</span></td>
                  <td class="mono">{{ pct(v.meta) }}</td>
                  @if (c.recorteRotulo) { <td class="mono">{{ pct(v.homogeneidadeTerritorios) }}</td> }
                  <td>@if (v.classeAbandono) { <span [class]="v.classeAbandono === 'ALTA' ? 'tag tag--alert' : v.classeAbandono === 'MEDIA' ? 'tag tag--warn' : 'tag tag--ok'">{{ pct(v.abandono) }} · {{ abandono[v.classeAbandono] }}</span> } @else { — }</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        @if (c.recorteRotulo) {
          <h3 class="mo-grupo" style="margin-top: var(--s-4);">Por {{ c.recorteRotulo.toLowerCase() }}</h3>
          <div class="tbl-wrap cob-tabela">
            <table [attr.aria-label]="'Cobertura por ' + c.recorteRotulo.toLowerCase()">
              <thead><tr><th scope="col">{{ c.recorteRotulo }}</th><th scope="col">Vacinas na meta</th>
                @for (v of c.vacinas; track v.esquemaId) { <th scope="col" [title]="v.nome">{{ v.sigla }}</th> }</tr></thead>
              <tbody>
                @for (r of c.recortes; track r.nome) {
                  <tr>
                    <td>@if (podeDescer(c, r)) { <button type="button" class="link-btn" (click)="descer(c, r)">{{ r.nome }}</button> } @else { {{ r.nome }} }</td>
                    <td class="mono">{{ r.vacinasNaMeta }} de {{ r.vacinasAvaliadas }}</td>
                    @for (i of r.itens; track i.esquemaId; let k = $index) {
                      <td><span [class]="classe(i.cobertura, c.vacinas[k].meta)" [title]="i.vacinados + ' de ' + i.coorte">{{ pct(i.cobertura) }}</span></td>
                    }
                  </tr>
                } @empty { <tr><td [attr.colspan]="2 + c.vacinas.length" class="cell-sub">Nenhuma criança adscrita na idade das coortes.</td></tr> }
              </tbody>
            </table>
          </div>
        }
      } @else if (!erro()) {
        <p class="cell-sub">Calculando…</p>
      }
    </section>
  `,
  styles: `
    .cob-filtros { display: flex; gap: var(--s-3); align-items: flex-end; flex-wrap: wrap; margin-bottom: var(--s-3); }
    .cob-filtros .dw-field { margin: 0; }
    .cob-tabela table { min-width: 0; width: 100%; }
  `,
})
export class CoberturaVacinal {
  private readonly service = inject(ImunizacaoService);
  private readonly equipeService = inject(EquipeService);

  /** Vazio: as unidades do acesso. */
  readonly unidadeId = input('');

  protected readonly abandono = CLASSES_ABANDONO;
  protected readonly equipes = signal<EquipeResumoDto[]>([]);
  protected readonly equipeId = signal('');
  protected readonly microareaId = signal('');
  protected readonly data = signal(hojeIso());
  protected readonly cobertura = signal<CoberturaVacinalDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  /** As microáreas da equipe, pelos recortes da cobertura da equipe. */
  private readonly microareasDaEquipe = signal<CoberturaRecorteDto[]>([]);
  protected readonly microareas = computed(() => this.microareasDaEquipe().filter((r) => !!r.id));

  constructor() {
    effect(() => {
      const u = this.unidadeId();
      untracked(() => {
        this.equipeId.set('');
        this.microareaId.set('');
        this.equipes.set([]);
        if (u) {
          this.equipeService.listar(u).pipe(catchError(() => of(null))).subscribe((r) => this.equipes.set(r?.lista ?? []));
        }
      });
    });
    effect(() => {
      const filtro = { unidadeId: this.unidadeId(), equipeId: this.equipeId(), microareaId: this.microareaId(), data: this.data() };
      untracked(() => this.carregar(filtro));
    });
  }

  protected trocarEquipe(id: string): void {
    this.microareaId.set('');
    this.microareasDaEquipe.set([]);
    this.equipeId.set(id);
  }

  protected podeDescer(c: CoberturaVacinalDto, r: CoberturaRecorteDto): boolean {
    return (c.nivel === 'UNIDADE' || c.nivel === 'EQUIPE') && !!r.id;
  }

  /** Da unidade para a equipe; da equipe para a microárea. Na rede, escolha a unidade no alto da tela. */
  protected descer(c: CoberturaVacinalDto, r: CoberturaRecorteDto): void {
    if (c.nivel === 'UNIDADE' && r.id) {
      this.trocarEquipe(r.id);
    } else if (c.nivel === 'EQUIPE' && r.id) {
      this.microareaId.set(r.id);
    }
  }

  protected pct(v: number | null | undefined): string {
    return percentual(v);
  }

  protected classe(v: number | null | undefined, meta: number): string {
    return classeCobertura(v, meta);
  }

  private carregar(filtro: { unidadeId: string; equipeId: string; microareaId: string; data: string }): void {
    this.cobertura.set(null);
    this.erro.set(null);
    this.service.cobertura(filtro).subscribe({
      next: (c) => {
        this.cobertura.set(c);
        if (c.nivel === 'EQUIPE') {
          this.microareasDaEquipe.set(c.recortes);
        }
      },
      error: (e: HttpErrorResponse) =>
        this.erro.set(e.status === 403 ? 'Seu acesso não inclui a cobertura vacinal desta abrangência.'
          : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível calcular a cobertura.')),
    });
  }
}

function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
