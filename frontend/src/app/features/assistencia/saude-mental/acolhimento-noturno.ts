import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import { AcolhimentoNoturnoDto, AcompanhamentoSaudeMentalDto, PermanenciaAcolhimentoDto } from '../../../core/models/saude-mental';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { RapsService } from '../../../core/services/raps';
import { SaudeMentalService } from '../../../core/services/saude-mental';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta = { tipo: 'entrada' } | { tipo: 'prorrogar' | 'saida'; p: PermanenciaAcolhimentoDto } | { tipo: 'configurar' } | null;

/**
 * Acolhimento noturno do CAPS (ADR-0153): a ocupação de hoje (vagas e acolhidos), há quantos dias e quem passou da
 * saída prevista; a entrada do paciente em acompanhamento na unidade, a prorrogação e a saída; a configuração das vagas
 * e da permanência máxima, que é da gestão da rede.
 */
@Component({
  selector: 'app-acolhimento-noturno',
  imports: [RouterLink, Drawer],
  template: `
    <header class="page-head">
      <div>
        <h1>Acolhimento noturno</h1>
        <p>CAPS III e AD III · vagas, permanências e saídas</p>
      </div>
      <div class="page-actions an-filtros">
        <div class="dw-field"><label for="f-an-unidade">Unidade</label>
          <select id="f-an-unidade" (change)="unidadeId.set($any($event.target).value)">
            <option value="" [selected]="!unidadeId()">Escolha a unidade…</option>
            @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="u.uuid === unidadeId()">{{ u.nome }}</option> }
          </select></div>
        <a class="btn btn--ghost" routerLink="/assistencia/saude-mental">Voltar à Saúde Mental</a>
      </div>
    </header>
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (painel(); as a) {
      @if (!a.temAcolhimentoNoturno) {
        <div class="box box--info" role="status"><strong>{{ a.unidadeNome }} não tem acolhimento noturno.</strong> A unidade precisa da capacidade
          "Acolhimento noturno" no modelo operacional.</div>
      } @else {
        <div class="kpis" style="margin-bottom: var(--s-4);">
          <div class="kpi"><div class="kpi__value" aria-label="Ocupação">{{ a.ocupadas }} / {{ a.vagas ?? '—' }}</div><div class="kpi__label">Vagas ocupadas</div></div>
          <div class="kpi"><div class="kpi__value">{{ a.permanenciaMaximaDias ?? '—' }}</div><div class="kpi__label">Permanência máxima (dias)</div></div>
          <div class="kpi"><div class="kpi__value">{{ vencidas() }}</div><div class="kpi__label">Passaram da saída prevista</div></div>
        </div>
        <section class="card" style="padding: var(--s-5);" aria-label="Acolhidos">
          <div class="an-cab">
            <h3 class="mo-grupo">Acolhidos agora</h3>
            <div class="row-actions">
              <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'configurar' })">Vagas e permanência</button>
              <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'entrada' })" [disabled]="!a.vagas">Registrar entrada</button>
            </div>
          </div>
          @if (!a.vagas) { <p class="cell-sub">Configure as vagas e a permanência máxima antes da primeira entrada.</p> }
          <div class="tbl-wrap an-tabela">
            <table aria-label="Pacientes acolhidos">
              <thead><tr><th scope="col">Paciente</th><th scope="col">Entrada</th><th scope="col">Saída prevista</th><th scope="col" class="num">Dias</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
              <tbody>
                @for (p of a.acolhidos; track p.uuid) {
                  <tr>
                    <td>{{ p.pacienteNome }}@if (p.justificativaPrazo) { <span class="cell-sub">{{ p.justificativaPrazo }}</span> }</td>
                    <td class="mono">{{ data(p.entrada) }}</td>
                    <td class="mono">{{ data(p.saidaPrevista) }}@if (p.passouDaSaidaPrevista) { <span class="tag tag--alert">Vencida</span> }@if (p.alemDoMaximo) { <span class="tag tag--warn">Além do máximo</span> }</td>
                    <td class="num mono">{{ p.dias }}</td>
                    <td class="row-actions">
                      <button type="button" class="link-btn" (click)="abrir({ tipo: 'prorrogar', p })" [attr.aria-label]="'Prorrogar ' + p.pacienteNome">Prorrogar</button>
                      <button type="button" class="link-btn" (click)="abrir({ tipo: 'saida', p })" [attr.aria-label]="'Saída de ' + p.pacienteNome">Saída</button>
                    </td>
                  </tr>
                } @empty { <tr><td colspan="5" class="cell-sub">Ninguém acolhido agora.</td></tr> }
              </tbody>
            </table>
          </div>
          @if (a.saidasRecentes.length) {
            <h3 class="mo-grupo" style="margin-top: var(--s-4);">Saídas nos últimos 30 dias</h3>
            <ul class="eq-prof" aria-label="Saídas recentes">
              @for (p of a.saidasRecentes; track p.uuid) {
                <li class="mo-row"><span><span class="cell-main">{{ p.pacienteNome }}</span><span class="cell-sub">{{ data(p.entrada) }} a {{ data(p.saida!) }} · {{ p.dias }} dia(s) · {{ p.motivoSaida }}</span></span></li>
              }
            </ul>
          }
        </section>
      }
    } @else if (!unidadeId()) {
      <p class="cell-sub">Escolha a unidade.</p>
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @switch (g.tipo) {
              @case ('configurar') {
                <div class="dw-field"><label for="f-an-vagas">Vagas</label><input id="f-an-vagas" type="number" min="1" [value]="vagas()" (input)="vagas.set($any($event.target).value)" /></div>
                <div class="dw-field"><label for="f-an-maximo">Permanência máxima (dias)</label><input id="f-an-maximo" type="number" min="1" [value]="maximo()" (input)="maximo.set($any($event.target).value)" /></div>
              }
              @case ('entrada') {
                <div class="dw-field"><label for="f-an-paciente">Paciente em acompanhamento na unidade</label>
                  <select id="f-an-paciente" (change)="pacienteId.set($any($event.target).value)">
                    <option value="" [selected]="!pacienteId()">Escolha…</option>
                    @for (c of casos(); track c.uuid) { <option [value]="c.pacienteId" [selected]="c.pacienteId === pacienteId()">{{ c.pacienteNome }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-an-prevista">Saída prevista</label><input id="f-an-prevista" type="date" [value]="prevista()" (input)="prevista.set($any($event.target).value)" /></div>
                <div class="dw-field"><label for="f-an-justificativa">Justificativa<span class="opt">além da permanência máxima</span></label><input id="f-an-justificativa" type="text" maxlength="500" [value]="texto()" (input)="texto.set($any($event.target).value)" /></div>
              }
              @case ('prorrogar') {
                <dl class="ro"><div><dt>Paciente</dt><dd>{{ g.p.pacienteNome }} · desde {{ data(g.p.entrada) }}</dd></div></dl>
                <div class="dw-field"><label for="f-an-prevista">Nova saída prevista</label><input id="f-an-prevista" type="date" [value]="prevista()" (input)="prevista.set($any($event.target).value)" /></div>
                <div class="dw-field"><label for="f-an-justificativa">Justificativa<span class="opt">além da permanência máxima</span></label><input id="f-an-justificativa" type="text" maxlength="500" [value]="texto()" (input)="texto.set($any($event.target).value)" /></div>
              }
              @case ('saida') {
                <dl class="ro"><div><dt>Paciente</dt><dd>{{ g.p.pacienteNome }} · {{ g.p.dias }} dia(s)</dd></div></dl>
                <div class="dw-field"><label for="f-an-motivo">Motivo da saída</label><input id="f-an-motivo" type="text" maxlength="500" [value]="texto()" (input)="texto.set($any($event.target).value)" /></div>
              }
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn">Salvar</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .an-filtros { display: flex; gap: var(--s-3); align-items: flex-end; flex-wrap: wrap; }
    .an-filtros .dw-field { margin: 0; }
    .an-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-3); flex-wrap: wrap; }
    .an-tabela table { min-width: 0; width: 100%; }
  `,
})
export class AcolhimentoNoturno {
  private readonly service = inject(RapsService);
  private readonly saudeMental = inject(SaudeMentalService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly unidadeId = signal('');
  protected readonly painel = signal<AcolhimentoNoturnoDto | null>(null);
  protected readonly casos = signal<AcompanhamentoSaudeMentalDto[]>([]);
  protected readonly gaveta = signal<Gaveta>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly vagas = signal('');
  protected readonly maximo = signal('');
  protected readonly pacienteId = signal('');
  protected readonly prevista = signal('');
  protected readonly texto = signal('');

  constructor() {
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => {
      this.unidades.set(l);
      if (l.length === 1) this.unidadeId.set(l[0].uuid);
    });
    effect(() => {
      const u = this.unidadeId();
      untracked(() => this.carregar(u));
    });
  }

  protected vencidas(): number {
    return (this.painel()?.acolhidos ?? []).filter((p) => p.passouDaSaidaPrevista).length;
  }

  protected titulo(g: Exclude<Gaveta, null>): string {
    return g.tipo === 'configurar' ? 'Vagas e permanência máxima' : g.tipo === 'entrada' ? 'Entrada no acolhimento noturno'
      : g.tipo === 'prorrogar' ? 'Prorrogar a permanência' : 'Saída do acolhimento noturno';
  }

  protected abrir(g: Exclude<Gaveta, null>): void {
    const a = this.painel();
    this.erroGaveta.set(null);
    this.texto.set('');
    this.pacienteId.set('');
    this.vagas.set(a?.vagas != null ? String(a.vagas) : '');
    this.maximo.set(a?.permanenciaMaximaDias != null ? String(a.permanenciaMaximaDias) : '14');
    this.prevista.set(g.tipo === 'prorrogar' ? g.p.saidaPrevista : '');
    if (g.tipo === 'entrada') {
      this.saudeMental.casos({ unidadeId: this.unidadeId() }).pipe(catchError(() => of(null))).subscribe((c) => this.casos.set(c?.lista ?? []));
    }
    this.gaveta.set(g);
  }

  protected salvar(g: Exclude<Gaveta, null>): void {
    const texto = this.texto().trim() || null;
    let chamada: Observable<unknown>;
    switch (g.tipo) {
      case 'configurar': chamada = this.service.configurar(this.unidadeId(), Number(this.vagas()), Number(this.maximo())); break;
      case 'entrada': chamada = this.service.entrar({ unidadeId: this.unidadeId(), pacienteId: this.pacienteId(), saidaPrevista: this.prevista(), justificativa: texto }); break;
      case 'prorrogar': chamada = this.service.prorrogar(g.p.uuid, this.prevista(), texto); break;
      default: chamada = this.service.sair(g.p.uuid, texto ?? '');
    }
    chamada.subscribe({
      next: () => {
        this.gaveta.set(null);
        this.carregar(this.unidadeId());
      },
      error: (e: HttpErrorResponse) => this.erroGaveta.set(mensagem(e)),
    });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  private carregar(unidadeId: string): void {
    this.erro.set(null);
    this.painel.set(null);
    if (!unidadeId) return;
    this.service.acolhimento(unidadeId).subscribe({ next: (a) => this.painel.set(a), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
