import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnChanges, inject, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import {
  CentroCustoDto,
  EmpenhoDto,
  ExecucaoDto,
  FONTES,
  LiquidacaoDto,
  ORIGENS_LIQUIDACAO,
  OrigemLiquidacao,
  PendenciaLiquidacaoDto,
  RestoAPagarDto,
} from '../../../core/models/financeiro';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { FinanceiroService } from '../../../core/services/financeiro';
import { Drawer } from '../../../shared/drawer/drawer';

export type AbaLiquidacao = 'liquidar' | 'liquidacoes' | 'restos' | 'execucao';

/**
 * Financeiro, fatia F2 (ADR-0219): a fila a liquidar, a liquidação (do atesto ou avulsa) com as retenções, as liquidações com o pagamento
 * e a ordem cronológica, os restos a pagar e a execução do exercício.
 */
@Component({
  selector: 'app-liquidacao-financeiro',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (aviso()) { <div class="box box--info" role="status"><strong>{{ aviso() }}</strong></div> }

    @switch (aba) {
      @case ('liquidar') {
        @if (podeGerenciar) { <div class="lq-cab"><button type="button" class="btn btn--ghost" (click)="avulsa()">Liquidação avulsa</button></div> }
        <div class="tbl-wrap card">
          <table class="lq-tabela" aria-label="A liquidar">
            <thead><tr><th scope="col">Atesto</th><th scope="col">Credor</th><th scope="col">Atestado em</th><th scope="col">Valor</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (p of pendencias(); track p.origemId) {
                <tr>
                  <td><span class="cell-main">{{ p.descricao }}</span><span class="cell-sub">{{ origens[p.origem] }}@if (p.unidadeNome) { · {{ p.unidadeNome }} }@if (p.empenhoNumero) { · empenho {{ p.empenhoNumero }} }</span></td>
                  <td><span class="cell-main">{{ p.fornecedorNome }}</span>@if (p.instrumentoNumero) { <span class="cell-sub">{{ p.instrumentoNumero }}</span> }</td>
                  <td>{{ p.atestadoEm ? data(p.atestadoEm) : '—' }}</td>
                  <td class="mono">{{ moeda(p.valor) }}</td>
                  <td class="row-actions">@if (podeGerenciar) { <button type="button" class="link-btn" (click)="abrir(p)" [attr.aria-label]="'Liquidar ' + p.descricao">Liquidar</button> }</td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhum atesto à espera da liquidação.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('liquidacoes') {
        <div class="tbl-wrap card">
          <table class="lq-tabela" aria-label="Liquidações">
            <thead><tr><th scope="col">Liquidação</th><th scope="col">Empenho</th><th scope="col">Bruto e líquido</th><th scope="col">Vencimento</th><th scope="col">Pagamento</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (l of liquidacoes(); track l.uuid) {
                <tr>
                  <td><span class="cell-main">{{ l.descricao }}</span><span class="cell-sub">{{ l.fornecedorNome }}@if (l.notaFiscal) { · nota {{ l.notaFiscal }} }</span></td>
                  <td><span class="cell-main mono">{{ l.empenhoNumero }}</span><span class="cell-sub">{{ fontes[l.fonte] }}</span></td>
                  <td class="mono">{{ moeda(l.valorBruto) }}<span class="cell-sub">líquido {{ moeda(l.valorLiquido) }}</span></td>
                  <td>{{ data(l.vencimento) }}@if (l.vencida) { <span class="tag tag--alert">vencida</span> }</td>
                  <td>@if (l.paga) { {{ data(l.pagaEm!) }} <span class="cell-sub">OB {{ l.ordemBancaria }}</span> } @else { <span class="cell-sub">a pagar</span> }</td>
                  <td class="row-actions">@if (!l.paga && podeGerenciar) { <button type="button" class="link-btn" (click)="abrirPagamento(l)" [attr.aria-label]="'Pagar ' + l.descricao">Pagar</button> }</td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma liquidação.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('restos') {
        <p class="cell-sub">Empenhos de exercícios anteriores: processados (liquidados e não pagos) e não processados (empenhados e não liquidados).</p>
        <div class="tbl-wrap card">
          <table class="lq-tabela" aria-label="Restos a pagar">
            <thead><tr><th scope="col">Empenho</th><th scope="col">Credor</th><th scope="col">Processados</th><th scope="col">Não processados</th></tr></thead>
            <tbody>
              @for (r of restos(); track r.empenhoId) {
                <tr><td class="mono">{{ r.numero }} <span class="cell-sub">{{ r.exercicio }}</span></td><td>{{ r.fornecedorNome }}</td><td class="mono">{{ moeda(r.processado) }}</td><td class="mono">{{ moeda(r.naoProcessado) }}</td></tr>
              } @empty { <tr><td colspan="4" class="cell-sub">Nenhum resto a pagar.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('execucao') {
        @if (execucao(); as x) {
          <div class="lq-numeros" aria-label="Resumo da execução">
            <div class="card lq-num"><span class="lq-num__v">{{ x.aLiquidarQuantidade }}</span><span class="cell-sub">a liquidar · {{ moeda(x.aLiquidarValor) }}</span></div>
            <div class="card lq-num"><span class="lq-num__v">{{ x.vencidasQuantidade }}</span><span class="cell-sub">vencidas sem pagamento · {{ moeda(x.vencidasValor) }}</span></div>
            <div class="card lq-num"><span class="lq-num__v">{{ x.prazoMedioPagamentoDias ?? '—' }}</span><span class="cell-sub">dias do atesto ao pagamento</span></div>
          </div>
          <h3 class="mo-grupo lq-titulo">Por fonte</h3>
          <div class="tbl-wrap card">
            <table class="lq-tabela" aria-label="Execução por fonte">
              <thead><tr><th scope="col">Fonte</th><th scope="col">Autorizado</th><th scope="col">Empenhado</th><th scope="col">Liquidado</th><th scope="col">Pago</th></tr></thead>
              <tbody>
                @for (f of x.fontes; track f.fonte) {
                  <tr><td>{{ fontes[f.fonte] }}</td><td class="mono">{{ moeda(f.autorizado) }}</td><td class="mono">{{ moeda(f.empenhado) }}</td><td class="mono">{{ moeda(f.liquidado) }}</td><td class="mono">{{ moeda(f.pago) }}</td></tr>
                } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma dotação no exercício.</td></tr> }
              </tbody>
            </table>
          </div>
          <h3 class="mo-grupo lq-titulo">Por dotação</h3>
          <div class="tbl-wrap card">
            <table class="lq-tabela" aria-label="Execução por dotação">
              <thead><tr><th scope="col">Dotação</th><th scope="col">Autorizado</th><th scope="col">Empenhado</th><th scope="col">Liquidado</th><th scope="col">Pago</th></tr></thead>
              <tbody>
                @for (d of x.dotacoes; track d.dotacaoId) {
                  <tr><td class="mono">{{ d.rotulo }}</td><td class="mono">{{ moeda(d.autorizado) }}</td><td class="mono">{{ moeda(d.empenhado) }}</td><td class="mono">{{ moeda(d.liquidado) }}</td><td class="mono">{{ moeda(d.pago) }}</td></tr>
                }
              </tbody>
            </table>
          </div>
        }
      }
    }

    @if (gaveta() === 'liquidar') {
      <app-drawer [titulo]="pendencia() ? 'Liquidar: ' + pendencia()!.descricao : 'Liquidação avulsa'" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); liquidar()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @if (pendencia(); as p) {
              <dl class="ro">
                <div><dt>Credor</dt><dd>{{ p.fornecedorNome }}</dd></div>
                <div><dt>Valor do atesto</dt><dd>{{ moeda(p.valor) }}</dd></div>
              </dl>
            }
            <div class="dw-field"><label for="f-lq-empenho">Empenho <span class="req" aria-hidden="true">*</span></label>
              <select id="f-lq-empenho" (change)="set('empenhoId', $event)" [disabled]="!!pendencia()?.empenhoId">
                <option value="">Escolha…</option>
                @for (e of empenhosDoCredor(); track e.uuid) { <option [value]="e.uuid" [selected]="f()['empenhoId'] === e.uuid">{{ e.numero }} · {{ e.fornecedorNome }} · valor {{ moeda(e.valorAtual) }}</option> }
              </select></div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-lq-nota">Nota fiscal</label><input id="f-lq-nota" type="text" maxlength="60" (input)="set('nota', $event)" /></div>
              <div class="dw-field"><label for="f-lq-bruto">Valor bruto (R$) <span class="req" aria-hidden="true">*</span></label><input id="f-lq-bruto" type="number" min="0.01" step="0.01" [value]="f()['bruto'] ?? ''" (input)="set('bruto', $event)" /></div>
              <div class="dw-field"><label for="f-lq-vencimento">Vencimento</label><input id="f-lq-vencimento" type="date" (input)="set('vencimento', $event)" /></div>
            </div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-lq-iss">ISS</label><input id="f-lq-iss" type="number" min="0" step="0.01" (input)="set('iss', $event)" /></div>
              <div class="dw-field"><label for="f-lq-irrf">IRRF</label><input id="f-lq-irrf" type="number" min="0" step="0.01" (input)="set('irrf', $event)" /></div>
              <div class="dw-field"><label for="f-lq-inss">INSS</label><input id="f-lq-inss" type="number" min="0" step="0.01" (input)="set('inss', $event)" /></div>
              <div class="dw-field"><label for="f-lq-outras">Outras</label><input id="f-lq-outras" type="number" min="0" step="0.01" (input)="set('outras', $event)" /></div>
            </div>
            @if (!pendencia()) {
              <div class="dw-field"><label for="f-lq-justificativa">Justificativa (sem atesto na plataforma) <span class="req" aria-hidden="true">*</span></label><input id="f-lq-justificativa" type="text" maxlength="500" (input)="set('justificativa', $event)" /></div>
              <div class="dw-field"><label for="f-lq-atestado">Matrícula de quem atestou <span class="req" aria-hidden="true">*</span></label><input id="f-lq-atestado" type="text" maxlength="30" (input)="set('atestado', $event)" /></div>
              <div class="dw-field"><label for="f-lq-centro">Centro de custo <span class="req" aria-hidden="true">*</span></label>
                <select id="f-lq-centro" (change)="set('centroCustoId', $event)">
                  <option value="">Escolha…</option>
                  @for (c of centros(); track c.uuid) { <option [value]="c.uuid">{{ c.codigo }} · {{ c.nome }} · {{ c.unidadeNome }}</option> }
                </select></div>
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Liquidar</button></div>
        </form>
      </app-drawer>
    }

    @if (gaveta() === 'pagar' && liquidacao(); as l) {
      <app-drawer [titulo]="'Pagar: ' + l.descricao" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); pagar(l)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <dl class="ro">
              <div><dt>Credor</dt><dd>{{ l.fornecedorNome }}</dd></div>
              <div><dt>Líquido</dt><dd>{{ moeda(l.valorLiquido) }} <span class="cell-sub">bruto {{ moeda(l.valorBruto) }}</span></dd></div>
              <div><dt>Vencimento</dt><dd>{{ data(l.vencimento) }}</dd></div>
            </dl>
            <div class="dw-row">
              <div class="dw-field"><label for="f-pg-data">Data <span class="req" aria-hidden="true">*</span></label><input id="f-pg-data" type="date" [max]="hoje" [value]="f()['data'] ?? hoje" (input)="set('data', $event)" /></div>
              <div class="dw-field"><label for="f-pg-ob">Ordem bancária <span class="req" aria-hidden="true">*</span></label><input id="f-pg-ob" type="text" maxlength="60" (input)="set('ob', $event)" /></div>
            </div>
            <div class="dw-field"><label for="f-pg-justificativa">Justificativa fora da ordem cronológica<span class="opt">se houver outra vencida antes</span></label><input id="f-pg-justificativa" type="text" maxlength="500" (input)="set('foraDeOrdem', $event)" /></div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Pagar</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .lq-cab { display: flex; justify-content: flex-end; margin-bottom: var(--s-2); }
    .lq-tabela { min-width: 0; width: 100%; }
    .lq-titulo { margin-top: var(--s-4); }
    .lq-numeros { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: var(--s-2); margin-bottom: var(--s-3); }
    .lq-num { padding: var(--s-3); display: grid; gap: 2px; }
    .lq-num__v { font-size: 1.25rem; font-weight: 600; }
  `,
})
export class LiquidacaoFinanceiro implements OnChanges {
  @Input({ required: true }) aba!: AbaLiquidacao;
  @Input() exercicio = Number(hojeIso().slice(0, 4));
  @Input() podeGerenciar = false;
  @Input() empenhos: EmpenhoDto[] = [];

  private readonly service = inject(FinanceiroService);

  protected readonly origens = ORIGENS_LIQUIDACAO;
  protected readonly fontes = FONTES;
  protected readonly hoje = hojeIso();

  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly pendencias = signal<PendenciaLiquidacaoDto[]>([]);
  protected readonly liquidacoes = signal<LiquidacaoDto[]>([]);
  protected readonly restos = signal<RestoAPagarDto[]>([]);
  protected readonly execucao = signal<ExecucaoDto | null>(null);
  protected readonly gaveta = signal<'liquidar' | 'pagar' | null>(null);
  protected readonly pendencia = signal<PendenciaLiquidacaoDto | null>(null);
  protected readonly liquidacao = signal<LiquidacaoDto | null>(null);
  /** Os centros ativos, para a avulsa (ADR-0220). */
  protected readonly centros = signal<CentroCustoDto[]>([]);

  ngOnChanges(): void {
    this.carregar();
  }

  private carregar(): void {
    const erro = (x: HttpErrorResponse) => this.erro.set(mensagem(x));
    if (this.aba === 'liquidar') this.service.aLiquidar().subscribe({ next: (l) => this.pendencias.set(l), error: erro });
    if (this.aba === 'liquidacoes') this.service.liquidacoes().subscribe({ next: (l) => this.liquidacoes.set(l), error: erro });
    if (this.aba === 'restos') this.service.restosAPagar().subscribe({ next: (l) => this.restos.set(l), error: erro });
    if (this.aba === 'execucao') this.service.execucao(this.exercicio).subscribe({ next: (x) => this.execucao.set(x), error: erro });
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  /** Os empenhos que servem: do credor do atesto e, se houver, do mesmo contrato; na avulsa, todos. */
  protected empenhosDoCredor(): EmpenhoDto[] {
    const p = this.pendencia();
    if (!p) return this.empenhos;
    return this.empenhos.filter((e) => e.fornecedorId === p.fornecedorId && (!e.instrumentoId || !p.instrumentoId || e.instrumentoId === p.instrumentoId));
  }

  protected abrir(p: PendenciaLiquidacaoDto): void {
    this.pendencia.set(p);
    this.f.set({ bruto: String(p.valor), empenhoId: p.empenhoId ?? '' });
    this.erroGaveta.set(null);
    this.gaveta.set('liquidar');
  }

  protected avulsa(): void {
    this.pendencia.set(null);
    this.f.set({});
    this.service.centrosCusto().pipe(catchError(() => of([]))).subscribe((l) => this.centros.set(l.filter((c) => c.ativo)));
    this.erroGaveta.set(null);
    this.gaveta.set('liquidar');
  }

  protected liquidar(): void {
    const v = this.f();
    const p = this.pendencia();
    if (!v['empenhoId'] || !v['bruto']) {
      this.erroGaveta.set('Escolha o empenho e informe o valor bruto.');
      return;
    }
    if (!p && !v['centroCustoId']) {
      this.erroGaveta.set('Na avulsa, escolha o centro de custo.');
      return;
    }
    const n = (k: string) => (v[k] ? Number(v[k]) : null);
    this.executar(this.service.liquidar({ origem: (p?.origem ?? 'AVULSA') as OrigemLiquidacao, origemId: p?.origemId ?? null, empenhoId: v['empenhoId'],
      notaFiscal: v['nota']?.trim() || null, notaEmitidaEm: null, valorBruto: Number(v['bruto']), iss: n('iss'), irrf: n('irrf'), inss: n('inss'),
      outrasRetencoes: n('outras'), vencimento: v['vencimento'] || null, justificativa: v['justificativa']?.trim() || null,
      atestadoPorMatricula: v['atestado']?.trim() || null, centroCustoId: p ? null : v['centroCustoId'] || null }), (l) => this.aviso.set(`Liquidado: ${l.descricao}, líquido ${this.moeda(l.valorLiquido)}.`));
  }

  protected abrirPagamento(l: LiquidacaoDto): void {
    this.liquidacao.set(l);
    this.f.set({ data: this.hoje });
    this.erroGaveta.set(null);
    this.gaveta.set('pagar');
  }

  protected pagar(l: LiquidacaoDto): void {
    const v = this.f();
    if (!v['ob']?.trim()) {
      this.erroGaveta.set('Informe a ordem bancária.');
      return;
    }
    this.executar(this.service.pagar(l.uuid, { data: v['data'] || this.hoje, ordemBancaria: v['ob'].trim(), justificativaForaDeOrdem: v['foraDeOrdem']?.trim() || null }),
      (x) => this.aviso.set(`Pago: ${x.descricao}.`));
  }

  private executar<T>(chamada: Observable<T>, depois: (r: T) => void): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    chamada.subscribe({
      next: (r) => {
        this.enviando.set(false);
        this.gaveta.set(null);
        depois(r);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}

function hojeIso(): string {
  return new Date().toLocaleDateString('en-CA', { timeZone: 'America/Sao_Paulo' });
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
