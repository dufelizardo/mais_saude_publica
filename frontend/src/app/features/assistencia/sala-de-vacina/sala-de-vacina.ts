import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import {
  AtrasadoVacinacaoDto,
  EsquemaVacinalDto,
  PUBLICOS,
  PublicoVacinal,
  VacinacaoDoDiaDto,
  idadeTexto,
} from '../../../core/models/imunizacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { RegistrarDose } from '../vacinacao/registrar-dose';

type Aba = 'dia' | 'atrasados' | 'calendario';

/**
 * Sala de Vacina (ADR-0147): o registro das doses do dia na unidade, com a contagem por vacina; os adscritos com dose
 * atrasada, para a busca ativa; e o calendário vacinal vigente.
 */
@Component({
  selector: 'app-sala-de-vacina',
  imports: [RouterLink, RegistrarDose],
  template: `
    <header class="page-head">
      <div>
        <h1>Sala de Vacina</h1>
        <p>Registro das doses, atrasados para a busca ativa e o calendário vacinal</p>
      </div>
      <div class="page-actions vac-filtros">
        <div class="dw-field">
          <label for="f-sv-unidade">Unidade</label>
          <select id="f-sv-unidade" [value]="unidadeId()" (change)="unidadeId.set($any($event.target).value)">
            <option value="">Escolha a unidade…</option>
            @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
          </select>
        </div>
        <button type="button" class="btn" (click)="registrando.set(true)" [disabled]="!unidadeId()">Registrar vacinação</button>
      </div>
    </header>

    <div class="page-tabs" role="tablist" aria-label="Sala de vacina">
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'dia'" (click)="aba.set('dia')">Doses do dia</button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'atrasados'" (click)="aba.set('atrasados')">Atrasados</button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'calendario'" (click)="aba.set('calendario')">Calendário vacinal</button>
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    @if (aba() === 'calendario') {
      <section class="card" style="padding: var(--s-5);" aria-label="Calendário vacinal">
        @for (g of calendarioPorPublico(); track g.publico) {
          <h3 class="mo-grupo" style="margin-top: var(--s-3);">{{ publicos[g.publico] }}</h3>
          <ul class="eq-prof">
            @for (e of g.esquemas; track e.uuid) {
              <li class="mo-row"><span><span class="cell-main">{{ e.sigla }} · {{ e.doseRotulo }}</span><span class="cell-sub">{{ e.observacao }}</span></span>
                @if (!e.contaNaSituacao) { <span class="tag tag--muted">Conforme a situação</span> }</li>
            }
          </ul>
        }
      </section>
    } @else if (!unidadeId()) {
      <p class="cell-sub">Escolha a unidade para ver as doses do dia e os atrasados.</p>
    } @else if (aba() === 'dia') {
      <section class="card" style="padding: var(--s-5);" aria-label="Doses do dia">
        <div class="vac-cab">
          <div class="dw-field" style="margin: 0;"><label for="f-sv-data">Dia</label><input id="f-sv-data" type="date" [value]="data()" (change)="data.set($any($event.target).value)" /></div>
          @if (dia(); as d) { <span class="mono" aria-label="Doses aplicadas no dia"><b>{{ d.total }}</b> dose(s) aplicada(s)</span> }
        </div>
        @if (dia(); as d) {
          @if (d.porImunobiologico.length) {
            <div class="vac-chips" aria-label="Doses por vacina">
              @for (c of d.porImunobiologico; track c.chave) { <span class="tag tag--info" [title]="c.rotulo">{{ c.chave }}: {{ c.quantidade }}</span> }
            </div>
          }
          <div class="tbl-wrap vac-tabela">
            <table aria-label="Doses aplicadas">
              <thead><tr><th scope="col">Hora</th><th scope="col">Paciente</th><th scope="col">Vacina</th><th scope="col">Lote</th><th scope="col">Quem aplicou</th></tr></thead>
              <tbody>
                @for (r of d.registros; track r.uuid) {
                  <tr>
                    <td class="mono">{{ r.aplicadaEm.slice(11, 16) }}</td>
                    <td><a [routerLink]="['/assistencia/pacientes']" [queryParams]="{ id: r.pacienteId, aba: 'vacinacao' }">{{ r.pacienteNome }}</a></td>
                    <td>{{ r.sigla }} · {{ r.doseRotulo }}</td>
                    <td class="mono">{{ r.lote ?? '—' }}</td>
                    <td>{{ r.profissionalNome ?? '—' }}</td>
                  </tr>
                } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma dose aplicada neste dia.</td></tr> }
              </tbody>
            </table>
          </div>
        }
      </section>
    } @else {
      <section class="card" style="padding: var(--s-5);" aria-label="Atrasados">
        <p class="cell-sub">Adscritos às equipes da unidade com alguma dose do calendário atrasada, os de atraso mais antigo primeiro.</p>
        @if (atrasados() === undefined) {
          <p class="cell-sub">Calculando…</p>
        } @else {
          <div class="tbl-wrap vac-tabela">
            <table aria-label="Pacientes com dose atrasada">
              <thead><tr><th scope="col">Paciente</th><th scope="col">Idade</th><th scope="col">Equipe · microárea</th><th scope="col">Doses atrasadas</th></tr></thead>
              <tbody>
                @for (a of atrasados() ?? []; track a.pacienteId) {
                  <tr>
                    <td><a [routerLink]="['/assistencia/pacientes']" [queryParams]="{ id: a.pacienteId, aba: 'vacinacao' }">{{ a.pacienteNome }}</a></td>
                    <td>{{ idade(a.idadeMeses) }}</td>
                    <td>{{ a.equipeNome ?? '—' }}@if (a.microareaNome) { · {{ a.microareaNome }} }</td>
                    <td>@for (x of a.atrasadas; track x) { <span class="tag tag--alert" style="margin: 0 4px 4px 0;">{{ x }}</span> }</td>
                  </tr>
                } @empty { <tr><td colspan="4" class="cell-sub">Nenhum adscrito com dose atrasada.</td></tr> }
              </tbody>
            </table>
          </div>
        }
      </section>
    }

    @if (registrando()) {
      <app-registrar-dose [unidades]="unidades()" [unidadeInicial]="unidadeId()" (salvo)="aoSalvar()" (fechar)="registrando.set(false)" />
    }
  `,
  styles: `
    .vac-filtros { display: flex; gap: var(--s-3); align-items: flex-end; flex-wrap: wrap; }
    .vac-filtros .dw-field { margin: 0; }
    .vac-cab { display: flex; justify-content: space-between; align-items: flex-end; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-3); }
    .vac-chips { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: var(--s-3); }
    .vac-tabela table { min-width: 0; width: 100%; }
  `,
})
export class SalaDeVacina {
  private readonly service = inject(ImunizacaoService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly publicos = PUBLICOS;
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly unidadeId = signal('');
  protected readonly data = signal(hojeIso());
  protected readonly aba = signal<Aba>('dia');
  protected readonly dia = signal<VacinacaoDoDiaDto | null>(null);
  protected readonly atrasados = signal<AtrasadoVacinacaoDto[] | null | undefined>(undefined);
  protected readonly calendario = signal<EsquemaVacinalDto[]>([]);
  protected readonly registrando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly calendarioPorPublico = computed(() => {
    const grupos = new Map<PublicoVacinal, EsquemaVacinalDto[]>();
    for (const e of this.calendario().filter((x) => x.ativo)) {
      grupos.set(e.publico, [...(grupos.get(e.publico) ?? []), e]);
    }
    return [...grupos].map(([publico, esquemas]) => ({ publico, esquemas }));
  });

  constructor() {
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => {
      this.unidades.set(l);
      if (l.length === 1) {
        this.unidadeId.set(l[0].uuid);
      }
    });
    this.service.calendario().pipe(catchError(() => of([]))).subscribe((l) => this.calendario.set(l));
    effect(() => {
      const u = this.unidadeId();
      const d = this.data();
      const a = this.aba();
      untracked(() => this.carregar(u, d, a));
    });
  }

  protected aoSalvar(): void {
    this.registrando.set(false);
    this.carregar(this.unidadeId(), this.data(), this.aba());
  }

  protected idade(meses: number): string {
    return idadeTexto(meses);
  }

  private carregar(unidadeId: string, data: string, aba: Aba): void {
    this.erro.set(null);
    if (!unidadeId) {
      return;
    }
    if (aba === 'dia') {
      this.service.doDia(unidadeId, data).subscribe({
        next: (d) => this.dia.set(d),
        error: (e: HttpErrorResponse) => this.falhou(e),
      });
    } else if (aba === 'atrasados') {
      this.atrasados.set(undefined);
      this.service.atrasados(unidadeId).subscribe({
        next: (l) => this.atrasados.set(l),
        error: (e: HttpErrorResponse) => {
          this.atrasados.set(null);
          this.falhou(e);
        },
      });
    }
  }

  private falhou(e: HttpErrorResponse): void {
    this.erro.set(e.status === 403 ? 'Seu acesso não inclui a sala de vacina desta unidade (VACINACAO.REGISTRAR).'
      : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar a sala de vacina.'));
  }
}

function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
