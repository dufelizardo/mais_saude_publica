import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { AlertaLoteDto, ImunobiologicoDto, SITUACOES_SUSPENSAO, SuspensaoLoteDto, dataBr } from '../../../core/models/imunizacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta = { tipo: 'suspender'; alerta: AlertaLoteDto | null } | { tipo: 'decidir'; suspensao: SuspensaoLoteDto } | null;

/**
 * Alertas de ESAVI por lote (ADR-0206): o lote da rede (vacina e número) com dois ou mais ESAVI graves em 30 dias, ou um com desvio de
 * qualidade (A2), e os lotes suspensos. A coordenação da rede suspende (todos os lotes com o número ficam sob suspeita) e depois libera
 * ou inutiliza; a API responde 403 a quem gerencia só algumas unidades. Sem o paciente.
 */
@Component({
  selector: 'app-alertas-lote',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    <section class="card" style="padding: var(--s-5);" aria-label="Alertas de lote">
      <div class="al-cab">
        <h3 class="mo-grupo">Lotes com sinal de ESAVI</h3>
        <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'suspender', alerta: null })">Suspender outro lote</button>
      </div>
      <p class="cell-sub">Dois ou mais ESAVI graves em 30 dias, ou um com desvio de qualidade do produto (A2), no mesmo lote, juntando as doses do estoque e as de lote digitado.</p>
      <ul class="eq-prof" aria-label="Lotes em alerta">
        @for (a of alertas(); track a.imunobiologicoId + a.numeroLote) {
          <li class="mo-row al-item">
            <span>
              <span class="cell-main">{{ a.sigla }} · <span class="mono">{{ a.numeroLote }}</span>
                @if (a.suspensao; as s) { <span class="tag tag--{{ situacoes[s.situacao].classe }}">{{ situacoes[s.situacao].rotulo }}</span> }</span>
              <span class="cell-sub">{{ a.sinal }} · {{ a.dosesEmEstoque }} doses em {{ a.salasComEstoque }} sala(s)</span>
              @if (a.notificacoes.length) {
                <span class="cell-sub">Notificações: @for (n of a.notificacoes; track n.uuid; let ultimo = $last) { {{ data(n.dataNotificacao) }} em {{ n.unidadeNome }}{{ n.gravidade === 'GRAVE' ? ' (grave)' : '' }}{{ n.causalidade === 'A2_DESVIO_QUALIDADE' ? ' (A2)' : '' }}{{ ultimo ? '' : ';' }} }</span>
              }
              @if (a.suspensao; as s) { <span class="cell-sub">{{ s.motivo }}@if (s.parecer) { · parecer: {{ s.parecer }} }</span> }
            </span>
            <span class="al-acoes">
              @if (a.suspensao?.situacao === 'VIGENTE') {
                <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'decidir', suspensao: a.suspensao! })" [attr.aria-label]="'Decidir o lote ' + a.numeroLote">Decidir</button>
              } @else {
                <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'suspender', alerta: a })" [attr.aria-label]="'Suspender o lote ' + a.numeroLote">Suspender na rede</button>
              }
            </span>
          </li>
        } @empty { <li class="cell-sub">Nenhum lote em alerta.</li> }
      </ul>
    </section>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g.tipo === 'suspender' ? 'Suspender o lote na rede' : 'Decidir o lote suspenso'" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @if (g.tipo === 'suspender') {
              @if (g.alerta; as a) {
                <dl class="ro"><div><dt>Lote</dt><dd>{{ a.sigla }} · {{ a.numeroLote }}</dd></div><div><dt>Sinal</dt><dd>{{ a.sinal }}</dd></div></dl>
              } @else {
                <div class="dw-row">
                  <div class="dw-field">
                    <label for="f-al-imuno">Vacina <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-al-imuno" [value]="imunoId()" (change)="imunoId.set($any($event.target).value)">
                      <option value="">Escolha…</option>
                      @for (i of imunosAtivos(); track i.uuid) { <option [value]="i.uuid">{{ i.sigla }} · {{ i.nome }}</option> }
                    </select>
                  </div>
                  <div class="dw-field"><label for="f-al-lote">Número do lote <span class="req" aria-hidden="true">*</span></label><input id="f-al-lote" type="text" maxlength="40" [value]="numero()" (input)="numero.set($any($event.target).value)" /></div>
                </div>
              }
              <div class="dw-field"><label for="f-al-motivo">Motivo <span class="req" aria-hidden="true">*</span></label><textarea id="f-al-motivo" rows="3" maxlength="1000" [value]="texto()" (input)="texto.set($any($event.target).value)"></textarea></div>
              <p class="dw-hint">Todos os lotes com este número ficam sob suspeita em todas as salas, e a dose com o lote digitado é barrada, até a decisão.</p>
            } @else {
              <dl class="ro"><div><dt>Lote</dt><dd>{{ g.suspensao.sigla }} · {{ g.suspensao.numeroLote }} ({{ g.suspensao.lotesAlcancados }} lote(s) nas salas)</dd></div><div><dt>Motivo</dt><dd>{{ g.suspensao.motivo }}</dd></div></dl>
              <fieldset class="al-decisao"><legend>Decisão</legend>
                <label class="chk"><input type="radio" name="al-decisao" [checked]="decisao() === 'LIBERAR'" (change)="decisao.set('LIBERAR')" /> Liberar: os lotes voltam ao uso</label>
                <label class="chk"><input type="radio" name="al-decisao" [checked]="decisao() === 'INUTILIZAR'" (change)="decisao.set('INUTILIZAR')" /> Inutilizar: o saldo sai como perda em cada sala</label>
              </fieldset>
              <div class="dw-field"><label for="f-al-parecer">Parecer <span class="req" aria-hidden="true">*</span></label><textarea id="f-al-parecer" rows="3" maxlength="1000" [value]="texto()" (input)="texto.set($any($event.target).value)"></textarea></div>
            }
          </div>
          <div class="dw-foot">
            <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button>
            <button type="submit" class="btn" [disabled]="salvando()">{{ g.tipo === 'suspender' ? 'Suspender' : 'Registrar decisão' }}</button>
          </div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .al-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-3); }
    .al-cab .mo-grupo { margin: 0; }
    .al-item > span:first-child { display: grid; gap: 2px; }
    .al-acoes { white-space: nowrap; }
    .al-decisao { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); display: grid; gap: var(--s-1); margin-bottom: var(--s-3); }
    .chk { display: flex; gap: var(--s-2); align-items: center; }
  `,
})
export class AlertasLote {
  private readonly service = inject(ImunizacaoService);

  protected readonly situacoes = SITUACOES_SUSPENSAO;
  protected readonly alertas = signal<AlertaLoteDto[]>([]);
  protected readonly imunos = signal<ImunobiologicoDto[]>([]);
  protected readonly gaveta = signal<Gaveta>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly salvando = signal(false);
  protected readonly imunoId = signal('');
  protected readonly numero = signal('');
  protected readonly texto = signal('');
  protected readonly decisao = signal<'LIBERAR' | 'INUTILIZAR'>('LIBERAR');
  protected readonly imunosAtivos = computed(() => this.imunos().filter((i) => i.ativo));

  constructor() {
    this.service.imunobiologicos().pipe(catchError(() => of([]))).subscribe((l) => this.imunos.set(l));
    this.carregar();
  }

  protected data(iso: string): string {
    return dataBr(iso);
  }

  protected abrir(g: NonNullable<Gaveta>): void {
    this.erroGaveta.set(null);
    this.imunoId.set('');
    this.numero.set('');
    this.texto.set('');
    this.decisao.set('LIBERAR');
    this.gaveta.set(g);
  }

  protected salvar(g: NonNullable<Gaveta>): void {
    const texto = this.texto().trim();
    let chamada;
    if (g.tipo === 'suspender') {
      const imuno = g.alerta?.imunobiologicoId ?? this.imunoId();
      const numero = g.alerta?.numeroLote ?? this.numero().trim();
      if (!imuno || !numero || !texto) {
        this.erroGaveta.set('Informe a vacina, o lote e o motivo.');
        return;
      }
      chamada = this.service.suspenderLote({ imunobiologicoId: imuno, numeroLote: numero, motivo: texto });
    } else {
      if (!texto) {
        this.erroGaveta.set('Informe o parecer.');
        return;
      }
      chamada = this.service.decidirSuspensao(g.suspensao.uuid, this.decisao(), texto);
    }
    this.salvando.set(true);
    chamada.subscribe({
      next: () => {
        this.salvando.set(false);
        this.gaveta.set(null);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erroGaveta.set(e.status === 403 ? 'A suspensão do lote é da coordenação da rede (acesso a todas as unidades).' : mensagem(e));
      },
    });
  }

  private carregar(): void {
    this.erro.set(null);
    this.service.alertasLote().subscribe({
      next: (l) => this.alertas.set(l),
      error: (e: HttpErrorResponse) => this.erro.set(e.status === 403 ? 'Os alertas de lote são de quem gerencia a imunização.' : mensagem(e)),
    });
  }
}

function mensagem(e: HttpErrorResponse): string {
  return (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir a operação.';
}
