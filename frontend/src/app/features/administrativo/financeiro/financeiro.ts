import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { FornecedorDto, InstrumentoDto } from '../../../core/models/compras';
import {
  DotacaoDto,
  EmpenhoDto,
  FONTES,
  FonteRecursoSaude,
  TIPOS_ALTERACAO,
  TIPOS_EMPENHO,
  TIPOS_MOVIMENTO,
  TipoAlteracaoDotacao,
  TipoEmpenho,
  TipoMovimentoEmpenho,
} from '../../../core/models/financeiro';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AuthService } from '../../../core/services/auth';
import { ComprasService } from '../../../core/services/compras';
import { FinanceiroService } from '../../../core/services/financeiro';
import { Drawer } from '../../../shared/drawer/drawer';
import { AbaLiquidacao, LiquidacaoFinanceiro } from './liquidacao-financeiro';

type Aba = 'dotacoes' | 'empenhos' | AbaLiquidacao;
type Gaveta = 'dotacao' | 'empenho' | null;

const GERENCIAR = 'FINANCEIRO.GERENCIAR';

/**
 * Financeiro, fatia F1 (ADR-0218): as dotações do Fundo Municipal de Saúde, com as suplementações e as anulações, e os empenhos com o
 * número do SIAFIC, o comprometido nas ordens de fornecimento, os reforços e as anulações. A plataforma acompanha; quem contabiliza é o
 * SIAFIC (Decreto 10.540/2020).
 */
@Component({
  selector: 'app-financeiro',
  imports: [Drawer, LiquidacaoFinanceiro],
  template: `
    <header class="page-head">
      <div>
        <h1>Financeiro</h1>
        <p>Dotações e empenhos do Fundo Municipal de Saúde, espelhados do SIAFIC</p>
      </div>
      <div class="page-actions">
        <div class="dw-field fn-exercicio"><label for="f-fn-exercicio">Exercício</label>
          <select id="f-fn-exercicio" (change)="exercicio.set(+$any($event.target).value); carregar()">
            @for (a of anos; track a) { <option [value]="a" [selected]="exercicio() === a">{{ a }}</option> }
          </select></div>
        @if (pode(GERENCIAR) && aba() === 'dotacoes') { <button type="button" class="btn" (click)="nova('dotacao')">Nova dotação</button> }
        @if (pode(GERENCIAR) && aba() === 'empenhos') { <button type="button" class="btn" (click)="nova('empenho')">Registrar empenho</button> }
      </div>
    </header>

    <div class="tabs" role="tablist" aria-label="Seções do financeiro">
      <button type="button" role="tab" id="tab-dotacoes" [attr.aria-selected]="aba() === 'dotacoes'" (click)="aba.set('dotacoes')">Dotações</button>
      <button type="button" role="tab" id="tab-empenhos" [attr.aria-selected]="aba() === 'empenhos'" (click)="aba.set('empenhos')">Empenhos</button>
      <button type="button" role="tab" id="tab-liquidar" [attr.aria-selected]="aba() === 'liquidar'" (click)="aba.set('liquidar')">A liquidar</button>
      <button type="button" role="tab" id="tab-liquidacoes" [attr.aria-selected]="aba() === 'liquidacoes'" (click)="aba.set('liquidacoes')">Liquidações</button>
      <button type="button" role="tab" id="tab-restos" [attr.aria-selected]="aba() === 'restos'" (click)="aba.set('restos')">Restos a pagar</button>
      <button type="button" role="tab" id="tab-execucao" [attr.aria-selected]="aba() === 'execucao'" (click)="aba.set('execucao')">Execução</button>
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (aviso()) { <div class="box box--info" role="status"><strong>{{ aviso() }}</strong></div> }

    @if (aba() === 'dotacoes') {
      <div class="fn-numeros" aria-label="Resumo do exercício">
        <div class="card fn-num"><span class="fn-num__v">{{ moeda(totais().autorizado) }}</span><span class="cell-sub">autorizado</span></div>
        <div class="card fn-num"><span class="fn-num__v">{{ moeda(totais().empenhado) }}</span><span class="cell-sub">empenhado</span></div>
        <div class="card fn-num"><span class="fn-num__v">{{ moeda(totais().disponivel) }}</span><span class="cell-sub">disponível</span></div>
      </div>
      <div class="tbl-wrap card">
        <table class="fn-tabela" aria-label="Dotações">
          <thead><tr><th scope="col">Dotação</th><th scope="col">Fonte</th><th scope="col">Autorizado</th><th scope="col">Empenhado</th><th scope="col">Disponível</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (d of dotacoes(); track d.uuid) {
              <tr>
                <td><span class="cell-main mono">{{ d.programaTrabalho }} · {{ d.naturezaDespesa }}</span><span class="cell-sub">{{ d.descricao }}</span></td>
                <td>{{ fontes[d.fonte] }}@if (d.fonteCodigo) { <span class="cell-sub">{{ d.fonteCodigo }}</span> }</td>
                <td class="mono">{{ moeda(d.autorizado) }}</td>
                <td class="mono">{{ moeda(d.empenhado) }}</td>
                <td class="mono">{{ moeda(d.disponivel) }}</td>
                <td class="row-actions"><button type="button" class="link-btn" (click)="abrirDotacao(d)" [attr.aria-label]="'Abrir dotação ' + d.programaTrabalho + ' ' + d.naturezaDespesa">Abrir</button></td>
              </tr>
            } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma dotação no exercício.</td></tr> }
          </tbody>
        </table>
      </div>
    } @else if (aba() !== 'empenhos') {
      <app-liquidacao-financeiro [aba]="$any(aba())" [exercicio]="exercicio()" [podeGerenciar]="pode(GERENCIAR)" [empenhos]="todosEmpenhos()" />
    } @else {
      <div class="tbl-wrap card">
        <table class="fn-tabela" aria-label="Empenhos">
          <thead><tr><th scope="col">Empenho</th><th scope="col">Fornecedor</th><th scope="col">Valor</th><th scope="col">Comprometido</th><th scope="col">Saldo</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (e of empenhos(); track e.uuid) {
              <tr>
                <td><span class="cell-main mono">{{ e.numero }}</span><span class="cell-sub">{{ tiposEmpenho[e.tipo] }} · {{ e.dotacaoRotulo }} · {{ data(e.data) }}</span></td>
                <td><span class="cell-main">{{ e.fornecedorNome }}</span>@if (e.instrumentoNumero) { <span class="cell-sub">{{ e.instrumentoNumero }}</span> }</td>
                <td class="mono">{{ moeda(e.valorAtual) }}</td>
                <td class="mono">{{ moeda(e.comprometido) }}</td>
                <td class="mono">{{ moeda(e.saldo) }}</td>
                <td class="row-actions"><button type="button" class="link-btn" (click)="abrirEmpenho(e)" [attr.aria-label]="'Abrir empenho ' + e.numero">Abrir</button></td>
              </tr>
            } @empty { <tr><td colspan="6" class="cell-sub">Nenhum empenho no exercício.</td></tr> }
          </tbody>
        </table>
      </div>
    }

    @if (gaveta() === 'dotacao') {
      <app-drawer titulo="Nova dotação" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvarDotacao()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <div class="dw-row">
              <div class="dw-field"><label for="f-dt-programa">Programa de trabalho <span class="req" aria-hidden="true">*</span></label><input id="f-dt-programa" type="text" placeholder="10.301.0010.2045" (input)="set('programa', $event)" /></div>
              <div class="dw-field"><label for="f-dt-natureza">Natureza da despesa <span class="req" aria-hidden="true">*</span></label><input id="f-dt-natureza" type="text" placeholder="3.3.90.30" (input)="set('natureza', $event)" /></div>
            </div>
            <div class="dw-field"><label for="f-dt-descricao">Descrição da ação <span class="req" aria-hidden="true">*</span></label><input id="f-dt-descricao" type="text" maxlength="300" (input)="set('descricao', $event)" /></div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-dt-fonte">Fonte <span class="req" aria-hidden="true">*</span></label>
                <select id="f-dt-fonte" (change)="set('fonte', $event)">
                  <option value="">Escolha…</option>
                  @for (x of listaFontes; track x) { <option [value]="x">{{ fontes[x] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-dt-fonte-codigo">Código da fonte<span class="opt">opcional</span></label><input id="f-dt-fonte-codigo" type="text" maxlength="20" (input)="set('fonteCodigo', $event)" /></div>
              <div class="dw-field"><label for="f-dt-valor">Valor da lei (R$) <span class="req" aria-hidden="true">*</span></label><input id="f-dt-valor" type="number" min="0.01" step="0.01" (input)="set('valor', $event)" /></div>
            </div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Registrar</button></div>
        </form>
      </app-drawer>
    }

    @if (gaveta() === 'empenho') {
      <app-drawer titulo="Registrar empenho" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvarEmpenho()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <p class="cell-sub">O empenho é emitido no SIAFIC; aqui ele é registrado com o mesmo número para acompanhar a execução.</p>
            <div class="dw-row">
              <div class="dw-field"><label for="f-em-numero">Número <span class="req" aria-hidden="true">*</span></label><input id="f-em-numero" type="text" maxlength="30" placeholder="2026NE000123" (input)="set('numero', $event)" /></div>
              <div class="dw-field"><label for="f-em-tipo">Tipo</label>
                <select id="f-em-tipo" (change)="set('tipo', $event)">
                  @for (t of listaTiposEmpenho; track t) { <option [value]="t" [selected]="(f()['tipo'] || 'ORDINARIO') === t">{{ tiposEmpenho[t] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-em-data">Data <span class="req" aria-hidden="true">*</span></label><input id="f-em-data" type="date" [max]="hoje" [value]="f()['data'] ?? hoje" (input)="set('data', $event)" /></div>
            </div>
            <div class="dw-field"><label for="f-em-dotacao">Dotação <span class="req" aria-hidden="true">*</span></label>
              <select id="f-em-dotacao" (change)="set('dotacaoId', $event)">
                <option value="">Escolha…</option>
                @for (d of dotacoes(); track d.uuid) { <option [value]="d.uuid">{{ d.programaTrabalho }} · {{ d.naturezaDespesa }} · {{ fontes[d.fonte] }} · disponível {{ moeda(d.disponivel) }}</option> }
              </select></div>
            <div class="dw-field"><label for="f-em-fornecedor">Fornecedor <span class="req" aria-hidden="true">*</span></label>
              <select id="f-em-fornecedor" (change)="set('fornecedorId', $event); f.set({ ...f(), instrumentoId: '' })">
                <option value="">Escolha…</option>
                @for (x of fornecedores(); track x.uuid) { <option [value]="x.uuid">{{ x.razaoSocial }}</option> }
              </select></div>
            <div class="dw-row">
              <div class="dw-field" style="flex: 2;"><label for="f-em-instrumento">Ata ou contrato<span class="opt">opcional</span></label>
                <select id="f-em-instrumento" (change)="set('instrumentoId', $event)">
                  <option value="">—</option>
                  @for (i of instrumentosDoFornecedor(); track i.uuid) { <option [value]="i.uuid">{{ i.numero }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-em-valor">Valor (R$) <span class="req" aria-hidden="true">*</span></label><input id="f-em-valor" type="number" min="0.01" step="0.01" (input)="set('valor', $event)" /></div>
            </div>
            <div class="dw-field"><label for="f-em-historico">Histórico<span class="opt">opcional</span></label><input id="f-em-historico" type="text" maxlength="500" (input)="set('historico', $event)" /></div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Registrar</button></div>
        </form>
      </app-drawer>
    }

    @if (dotacao(); as d) {
      <app-drawer [titulo]="'Dotação ' + d.programaTrabalho + ' · ' + d.naturezaDespesa" (fechar)="dotacao.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Ação</dt><dd>{{ d.descricao }}</dd></div>
            <div><dt>Fonte</dt><dd>{{ fontes[d.fonte] }}@if (d.fonteCodigo) { · {{ d.fonteCodigo }} }</dd></div>
            <div><dt>Lei</dt><dd>{{ moeda(d.valorInicial) }}</dd></div>
            <div><dt>Autorizado</dt><dd>{{ moeda(d.autorizado) }} <span class="cell-sub">+{{ moeda(d.suplementado) }} −{{ moeda(d.anulado) }}</span></dd></div>
            <div><dt>Empenhado</dt><dd>{{ moeda(d.empenhado) }}</dd></div>
            <div><dt>Disponível</dt><dd>{{ moeda(d.disponivel) }}</dd></div>
          </dl>
          <h3 class="mo-grupo fn-titulo">Alterações</h3>
          <ul class="fn-lista" aria-label="Alterações da dotação">
            @for (a of d.alteracoes; track a.uuid) { <li><span>{{ tiposAlteracao[a.tipo] }} · {{ a.ato }}</span><span class="cell-sub">{{ data(a.data) }} · {{ moeda(a.valor) }}</span></li> } @empty { <li class="cell-sub">Nenhuma.</li> }
          </ul>
          @if (pode(GERENCIAR)) {
            <div class="fn-linha">
              <div class="dw-field"><label for="f-al-tipo">Alteração</label>
                <select id="f-al-tipo" (change)="set('altTipo', $event)">
                  @for (t of listaTiposAlteracao; track t) { <option [value]="t" [selected]="(f()['altTipo'] || 'SUPLEMENTACAO') === t">{{ tiposAlteracao[t] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-al-valor">Valor (R$)</label><input id="f-al-valor" type="number" min="0.01" step="0.01" (input)="set('altValor', $event)" /></div>
              <div class="dw-field"><label for="f-al-ato">Ato</label><input id="f-al-ato" type="text" maxlength="60" placeholder="Decreto 12/2026" (input)="set('altAto', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="alterar(d)" [disabled]="enviando()">Registrar alteração</button>
            </div>
          }
        </div>
      </app-drawer>
    }

    @if (empenho(); as e) {
      <app-drawer [titulo]="'Empenho ' + e.numero" (fechar)="empenho.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Tipo</dt><dd>{{ tiposEmpenho[e.tipo] }} · {{ data(e.data) }}</dd></div>
            <div><dt>Dotação</dt><dd>{{ e.dotacaoRotulo }}</dd></div>
            <div><dt>Fornecedor</dt><dd>{{ e.fornecedorNome }}@if (e.instrumentoNumero) { · {{ e.instrumentoNumero }} }</dd></div>
            <div><dt>Valor</dt><dd>{{ moeda(e.valorAtual) }} <span class="cell-sub">inicial {{ moeda(e.valorInicial) }} +{{ moeda(e.reforcado) }} −{{ moeda(e.anulado) }}</span></dd></div>
            <div><dt>Comprometido</dt><dd>{{ moeda(e.comprometido) }}</dd></div>
            <div><dt>Saldo</dt><dd>{{ moeda(e.saldo) }}</dd></div>
            @if (e.historico) { <div><dt>Histórico</dt><dd>{{ e.historico }}</dd></div> }
          </dl>
          <h3 class="mo-grupo fn-titulo">Ordens de fornecimento</h3>
          <ul class="fn-lista" aria-label="Ordens do empenho">
            @for (o of e.ordens; track o.uuid) { <li><span class="mono">{{ o.numero }}</span><span class="cell-sub">{{ o.situacao }} · {{ moeda(o.valor) }}</span></li> } @empty { <li class="cell-sub">Nenhuma.</li> }
          </ul>
          <h3 class="mo-grupo fn-titulo">Reforços e anulações</h3>
          <ul class="fn-lista" aria-label="Movimentos do empenho">
            @for (m of e.movimentos; track m.uuid) { <li><span>{{ tiposMovimento[m.tipo] }} · {{ m.motivo }}</span><span class="cell-sub">{{ data(m.data) }} · {{ moeda(m.valor) }}</span></li> } @empty { <li class="cell-sub">Nenhum.</li> }
          </ul>
          @if (pode(GERENCIAR)) {
            <div class="fn-linha">
              <div class="dw-field"><label for="f-mv-tipo">Movimento</label>
                <select id="f-mv-tipo" (change)="set('movTipo', $event)">
                  @for (t of listaTiposMovimento; track t) { <option [value]="t" [selected]="(f()['movTipo'] || 'REFORCO') === t">{{ tiposMovimento[t] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-mv-valor">Valor (R$)</label><input id="f-mv-valor" type="number" min="0.01" step="0.01" (input)="set('movValor', $event)" /></div>
              <div class="dw-field" style="flex: 1;"><label for="f-mv-motivo">Motivo</label><input id="f-mv-motivo" type="text" maxlength="500" (input)="set('movMotivo', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="movimentar(e)" [disabled]="enviando()">Registrar movimento</button>
            </div>
          }
        </div>
      </app-drawer>
    }
  `,
  styles: `
    .fn-exercicio { margin: 0; }
    .fn-numeros { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: var(--s-2); margin-bottom: var(--s-3); }
    .fn-num { padding: var(--s-3); display: grid; gap: 2px; }
    .fn-num__v { font-size: 1.25rem; font-weight: 600; }
    .fn-tabela { min-width: 0; width: 100%; }
    .fn-titulo { margin-top: var(--s-4); }
    .fn-linha { display: flex; gap: var(--s-2); align-items: flex-end; flex-wrap: wrap; margin-bottom: var(--s-2); }
    .fn-linha .dw-field { margin: 0; }
    .fn-lista { margin: 0 0 var(--s-2); padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .fn-lista li { display: flex; justify-content: space-between; gap: var(--s-2); align-items: baseline; flex-wrap: wrap; }
  `,
})
export class Financeiro {
  private readonly service = inject(FinanceiroService);
  private readonly comprasService = inject(ComprasService);
  private readonly authService = inject(AuthService);

  protected readonly GERENCIAR = GERENCIAR;
  protected readonly fontes = FONTES;
  protected readonly listaFontes = Object.keys(FONTES) as FonteRecursoSaude[];
  protected readonly tiposEmpenho = TIPOS_EMPENHO;
  protected readonly listaTiposEmpenho = Object.keys(TIPOS_EMPENHO) as TipoEmpenho[];
  protected readonly tiposAlteracao = TIPOS_ALTERACAO;
  protected readonly listaTiposAlteracao = Object.keys(TIPOS_ALTERACAO) as TipoAlteracaoDotacao[];
  protected readonly tiposMovimento = TIPOS_MOVIMENTO;
  protected readonly listaTiposMovimento = Object.keys(TIPOS_MOVIMENTO) as TipoMovimentoEmpenho[];
  protected readonly hoje = hojeIso();
  protected readonly anos = [Number(this.hoje.slice(0, 4)) - 1, Number(this.hoje.slice(0, 4)), Number(this.hoje.slice(0, 4)) + 1];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('dotacoes');
  protected readonly exercicio = signal(Number(this.hoje.slice(0, 4)));
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly dotacoes = signal<DotacaoDto[]>([]);
  protected readonly empenhos = signal<EmpenhoDto[]>([]);
  /** Todos os exercícios, para a liquidação dos restos a pagar (ADR-0219). */
  protected readonly todosEmpenhos = signal<EmpenhoDto[]>([]);
  protected readonly fornecedores = signal<FornecedorDto[]>([]);
  private readonly instrumentos = signal<InstrumentoDto[]>([]);
  protected readonly gaveta = signal<Gaveta>(null);
  protected readonly dotacao = signal<DotacaoDto | null>(null);
  protected readonly empenho = signal<EmpenhoDto | null>(null);

  protected readonly totais = computed(() => this.dotacoes().reduce((t, d) => ({ autorizado: t.autorizado + d.autorizado, empenhado: t.empenhado + d.empenhado,
    disponivel: t.disponivel + d.disponivel }), { autorizado: 0, empenhado: 0, disponivel: 0 }));
  protected readonly instrumentosDoFornecedor = computed(() => this.instrumentos().filter((i) => i.fornecedorId === this.f()['fornecedorId']));

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      this.carregar();
      if (this.pode(GERENCIAR)) {
        this.comprasService.fornecedores().pipe(catchError(() => of([]))).subscribe((l) => this.fornecedores.set(l.filter((x) => x.ativo)));
        this.comprasService.instrumentos().pipe(catchError(() => of([]))).subscribe((l) => this.instrumentos.set(l));
      }
    });
  }

  protected pode(permissao: string): boolean {
    const a = this.acesso();
    return !a.restrito || a.permissoes.has(permissao);
  }

  protected carregar(): void {
    this.service.dotacoes(this.exercicio()).subscribe({ next: (l) => this.dotacoes.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
    this.service.empenhos(this.exercicio()).subscribe({ next: (l) => this.empenhos.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
    this.service.empenhos().pipe(catchError(() => of([]))).subscribe((l) => this.todosEmpenhos.set(l));
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected nova(g: Gaveta): void {
    this.f.set({});
    this.erroGaveta.set(null);
    this.gaveta.set(g);
  }

  protected salvarDotacao(): void {
    const v = this.f();
    if (!v['programa']?.trim() || !v['natureza']?.trim() || !v['descricao']?.trim() || !v['fonte'] || !v['valor']) {
      this.erroGaveta.set('Preencha o programa de trabalho, a natureza, a descrição, a fonte e o valor.');
      return;
    }
    this.executar(this.service.criarDotacao({ exercicio: this.exercicio(), programaTrabalho: v['programa'].trim(), descricao: v['descricao'].trim(),
      naturezaDespesa: v['natureza'].trim(), fonte: v['fonte'] as FonteRecursoSaude, fonteCodigo: v['fonteCodigo']?.trim() || null, valorInicial: Number(v['valor']) }), (d) => {
      this.gaveta.set(null);
      this.aviso.set(`Dotação ${d.programaTrabalho} · ${d.naturezaDespesa} registrada.`);
      this.carregar();
    });
  }

  protected salvarEmpenho(): void {
    const v = this.f();
    if (!v['numero']?.trim() || !v['dotacaoId'] || !v['fornecedorId'] || !v['valor']) {
      this.erroGaveta.set('Preencha o número, a dotação, o fornecedor e o valor.');
      return;
    }
    this.executar(this.service.empenhar({ numero: v['numero'].trim(), tipo: (v['tipo'] || 'ORDINARIO') as TipoEmpenho, dotacaoId: v['dotacaoId'],
      fornecedorId: v['fornecedorId'], instrumentoId: v['instrumentoId'] || null, valor: Number(v['valor']), data: v['data'] || this.hoje,
      historico: v['historico']?.trim() || null }), (e) => {
      this.gaveta.set(null);
      this.aviso.set(`Empenho ${e.numero} registrado.`);
      this.aba.set('empenhos');
      this.carregar();
    });
  }

  protected abrirDotacao(d: DotacaoDto): void {
    this.f.set({});
    this.erroGaveta.set(null);
    this.dotacao.set(d);
  }

  protected abrirEmpenho(e: EmpenhoDto): void {
    this.f.set({});
    this.erroGaveta.set(null);
    this.service.empenho(e.uuid).subscribe({ next: (x) => this.empenho.set(x), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  protected alterar(d: DotacaoDto): void {
    const v = this.f();
    if (!v['altValor'] || !v['altAto']?.trim()) {
      this.erroGaveta.set('Informe o valor e o ato da alteração.');
      return;
    }
    this.executar(this.service.alterarDotacao(d.uuid, { tipo: (v['altTipo'] || 'SUPLEMENTACAO') as TipoAlteracaoDotacao, valor: Number(v['altValor']),
      ato: v['altAto'].trim(), data: this.hoje, motivo: null }), (x) => {
      this.dotacao.set(x);
      this.f.set({});
      this.carregar();
    });
  }

  protected movimentar(e: EmpenhoDto): void {
    const v = this.f();
    if (!v['movValor'] || !v['movMotivo']?.trim()) {
      this.erroGaveta.set('Informe o valor e o motivo do movimento.');
      return;
    }
    this.executar(this.service.movimentar(e.uuid, { tipo: (v['movTipo'] || 'REFORCO') as TipoMovimentoEmpenho, valor: Number(v['movValor']), data: this.hoje,
      motivo: v['movMotivo'].trim() }), (x) => {
      this.empenho.set(x);
      this.f.set({});
      this.carregar();
    });
  }

  private executar<T>(chamada: Observable<T>, depois: (r: T) => void): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    chamada.subscribe({
      next: (r) => {
        this.enviando.set(false);
        depois(r);
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

/** Hoje no fuso de São Paulo, o dia que o servidor usa nas validações de data (o navegador pode estar em outro fuso). */
function hojeIso(): string {
  return new Date().toLocaleDateString('en-CA', { timeZone: 'America/Sao_Paulo' });
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
