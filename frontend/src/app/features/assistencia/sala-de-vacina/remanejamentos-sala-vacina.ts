import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import {
  CamaraRefrigeracaoDto,
  MOTIVOS_REMANEJAMENTO,
  RemanejamentoDto,
  SITUACOES_REMANEJAMENTO,
  dataBr,
} from '../../../core/models/imunizacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta = { tipo: 'receber' | 'recusar'; r: RemanejamentoDto } | null;

/**
 * Remanejamentos da sala de vacina (ADR-0206): os que chegam, para receber com a temperatura da caixa (fora de 2 a 8 °C, o lote chega
 * sob suspeita) ou recusar; os enviados em trânsito, que a coordenação da origem pode cancelar; e o histórico. O envio sai do estoque.
 */
@Component({
  selector: 'app-remanejamentos-sala-vacina',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    <section class="card" style="padding: var(--s-5);" aria-label="A receber">
      <h3 class="mo-grupo">A receber</h3>
      <ul class="eq-prof">
        @for (r of aReceber(); track r.uuid) {
          <li class="mo-row">
            <span><span class="cell-main">{{ r.sigla }} · <span class="mono">{{ r.numeroLote }}</span> · {{ r.doses }} doses</span>
              <span class="cell-sub">De {{ r.unidadeOrigemNome }} · {{ motivos[r.motivo] }} · validade {{ data(r.validade) }}@if (r.observacao) { · {{ r.observacao }} }</span></span>
            <span class="rem-acoes">
              <button type="button" class="link-btn" (click)="abrir({ tipo: 'recusar', r })" [attr.aria-label]="'Recusar o remanejamento do lote ' + r.numeroLote">Recusar</button>
              <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'receber', r })" [attr.aria-label]="'Receber o lote ' + r.numeroLote">Receber</button>
            </span>
          </li>
        } @empty { <li class="cell-sub">Nada a receber.</li> }
      </ul>
    </section>

    <section class="card" style="padding: var(--s-5); margin-top: var(--s-4);" aria-label="Remanejamentos">
      <h3 class="mo-grupo">Enviados e recebidos</h3>
      <div class="tbl-wrap rem-tabela">
        <table aria-label="Histórico de remanejamentos">
          <thead><tr><th scope="col">Vacina · lote</th><th scope="col">De → para</th><th scope="col">Doses</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (r of historico(); track r.uuid) {
              <tr>
                <td>{{ r.sigla }} · <span class="mono">{{ r.numeroLote }}</span></td>
                <td>{{ r.unidadeOrigemNome }} → {{ r.unidadeDestinoNome }}<span class="cell-sub">enviado em {{ quando(r.enviadoEm) }}</span></td>
                <td class="mono">{{ r.doses }}</td>
                <td><span class="tag tag--{{ situacoes[r.situacao].classe }}">{{ situacoes[r.situacao].rotulo }}</span>
                  @if (r.temperaturaChegada != null) { <span class="cell-sub" [class.rem-fora]="r.chegouForaDaFaixa">caixa a {{ r.temperaturaChegada }} °C{{ r.chegouForaDaFaixa ? ': lote sob suspeita' : '' }}</span> }
                  @if (r.motivoRecusa) { <span class="cell-sub">{{ r.motivoRecusa }}</span> }</td>
                <td class="rem-acoes">
                  @if (r.situacao === 'ENVIADO' && r.unidadeOrigemId === unidadeId()) {
                    <button type="button" class="link-btn" (click)="cancelar(r)" [disabled]="salvando()" [attr.aria-label]="'Cancelar o remanejamento do lote ' + r.numeroLote">Cancelar</button>
                  }
                </td>
              </tr>
            } @empty { <tr><td colspan="5" class="cell-sub">Nenhum remanejamento. Para enviar, use "Remanejar" no lote do estoque.</td></tr> }
          </tbody>
        </table>
      </div>
    </section>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g.tipo === 'receber' ? 'Receber remanejamento' : 'Recusar remanejamento'" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <dl class="ro"><div><dt>Lote</dt><dd>{{ g.r.sigla }} · {{ g.r.numeroLote }}, {{ g.r.doses }} doses, de {{ g.r.unidadeOrigemNome }}</dd></div></dl>
            @if (g.tipo === 'receber') {
              <div class="dw-row">
                <div class="dw-field"><label for="f-rec-temp">Temperatura da caixa na chegada (°C) <span class="req" aria-hidden="true">*</span></label><input id="f-rec-temp" type="number" step="0.1" [value]="temperatura()" (input)="temperatura.set($any($event.target).value)" /></div>
                <div class="dw-field">
                  <label for="f-rec-camara">Câmara<span class="opt">opcional</span></label>
                  <select id="f-rec-camara" [value]="camaraId()" (change)="camaraId.set($any($event.target).value)">
                    <option value="">—</option>
                    @for (c of camaras(); track c.uuid) { @if (c.ativa) { <option [value]="c.uuid">{{ c.nome }}</option> } }
                  </select>
                </div>
              </div>
              <p class="dw-hint">Fora de 2 a 8 °C, o lote entra sob suspeita e espera a avaliação da coordenação.</p>
            } @else {
              <div class="dw-field"><label for="f-rec-motivo">Motivo da recusa <span class="req" aria-hidden="true">*</span></label><textarea id="f-rec-motivo" rows="2" maxlength="500" [value]="motivoRecusa()" (input)="motivoRecusa.set($any($event.target).value)"></textarea></div>
              <p class="dw-hint">As doses voltam ao estoque da origem.</p>
            }
          </div>
          <div class="dw-foot">
            <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button>
            <button type="submit" class="btn" [disabled]="salvando()">{{ g.tipo === 'receber' ? 'Receber' : 'Recusar' }}</button>
          </div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .rem-acoes { white-space: nowrap; display: flex; gap: var(--s-3); align-items: center; justify-content: flex-end; }
    .rem-tabela table { min-width: 0; width: 100%; }
    .rem-fora { color: var(--danger-600, #b42318); }
  `,
})
export class RemanejamentosSalaVacina {
  private readonly service = inject(ImunizacaoService);

  readonly unidadeId = input.required<string>();
  readonly versao = input(0);

  protected readonly motivos = MOTIVOS_REMANEJAMENTO;
  protected readonly situacoes = SITUACOES_REMANEJAMENTO;
  protected readonly lista = signal<RemanejamentoDto[]>([]);
  protected readonly camaras = signal<CamaraRefrigeracaoDto[]>([]);
  protected readonly gaveta = signal<Gaveta>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly salvando = signal(false);
  protected readonly temperatura = signal('');
  protected readonly camaraId = signal('');
  protected readonly motivoRecusa = signal('');

  protected readonly aReceber = computed(() => this.lista().filter((r) => r.situacao === 'ENVIADO' && r.unidadeDestinoId === this.unidadeId()));
  protected readonly historico = computed(() => this.lista().filter((r) => !(r.situacao === 'ENVIADO' && r.unidadeDestinoId === this.unidadeId())));

  constructor() {
    effect(() => {
      const u = this.unidadeId();
      this.versao();
      untracked(() => this.carregar(u));
    });
  }

  protected data(iso: string): string {
    return dataBr(iso);
  }

  protected quando(instante: string): string {
    const d = new Date(instante);
    return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  }

  protected abrir(g: NonNullable<Gaveta>): void {
    this.erroGaveta.set(null);
    this.temperatura.set('');
    this.camaraId.set('');
    this.motivoRecusa.set('');
    this.gaveta.set(g);
  }

  protected salvar(g: NonNullable<Gaveta>): void {
    let chamada;
    if (g.tipo === 'receber') {
      if (this.temperatura() === '' || Number.isNaN(Number(this.temperatura()))) {
        this.erroGaveta.set('Informe a temperatura da caixa na chegada.');
        return;
      }
      chamada = this.service.receberRemanejamento(g.r.uuid, Number(this.temperatura()), this.camaraId() || null);
    } else {
      if (!this.motivoRecusa().trim()) {
        this.erroGaveta.set('Informe o motivo da recusa.');
        return;
      }
      chamada = this.service.recusarRemanejamento(g.r.uuid, this.motivoRecusa().trim());
    }
    this.salvando.set(true);
    chamada.subscribe({
      next: () => {
        this.salvando.set(false);
        this.gaveta.set(null);
        this.carregar(this.unidadeId());
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erroGaveta.set(mensagem(e));
      },
    });
  }

  protected cancelar(r: RemanejamentoDto): void {
    this.salvando.set(true);
    this.service.cancelarRemanejamento(r.uuid).subscribe({
      next: () => {
        this.salvando.set(false);
        this.carregar(this.unidadeId());
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erro.set(mensagem(e));
      },
    });
  }

  private carregar(unidadeId: string): void {
    this.erro.set(null);
    this.service.remanejamentos(unidadeId).subscribe({
      next: (l) => this.lista.set(l),
      error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)),
    });
    this.service.cadeiaDeFrio(unidadeId).pipe(catchError(() => of(null))).subscribe((c) => this.camaras.set(c?.camaras ?? []));
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 403) return 'Seu acesso não inclui esta operação nesta unidade.';
  return (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir a operação.';
}
