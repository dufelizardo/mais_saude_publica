import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  OcorrenciaDto,
  PainelComprasDto,
  SANCOES,
  SITUACOES_SOLICITACAO,
  SituacaoSolicitacaoCompra,
  TIPOS_OCORRENCIA,
  TipoSancaoFornecedor,
} from '../../../core/models/compras';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { ComprasService } from '../../../core/services/compras';
import { Drawer } from '../../../shared/drawer/drawer';

/**
 * Painel de Compras, fatia CP3 (ADR-0214): o andamento das solicitações, o valor contratado, as ordens atrasadas, os contratos vencendo,
 * o desempenho dos fornecedores e as ocorrências abertas, que se encerram com a resposta do fornecedor ou viram sanção.
 */
@Component({
  selector: 'app-painel-compras',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (painel(); as p) {
      <section class="stats" aria-label="Resumo de Compras">
        <div class="stat"><div><div class="stat__lbl">Valor contratado</div><div class="stat__val mono">{{ moeda(p.valorContratado) }}</div><div class="stat__sub">{{ data(p.inicio) }} a {{ data(p.fim) }}</div></div></div>
        <div class="stat"><div><div class="stat__lbl">Ordens em aberto</div><div class="stat__val mono">{{ p.ordensEmAberto }}</div><div class="stat__sub">{{ p.ordensEmAtraso }} em atraso</div></div></div>
        <div class="stat"><div><div class="stat__lbl">Contratos vencendo</div><div class="stat__val mono">{{ p.contratosVencendo }}</div><div class="stat__sub">em até 90 dias</div></div></div>
        <div class="stat"><div><div class="stat__lbl">Itens com saldo baixo</div><div class="stat__val mono">{{ p.itensComSaldoBaixo }}</div><div class="stat__sub">abaixo de 20%</div></div></div>
        <div class="stat"><div><div class="stat__lbl">Ocorrências abertas</div><div class="stat__val mono">{{ p.ocorrenciasAbertas }}</div><div class="stat__sub">com fornecedores</div></div></div>
      </section>

      <div class="pc-grade">
        <div class="card"><h3 class="mo-grupo">Solicitações em andamento</h3>
          <ul class="pc-lista">@for (s of situacoesEmAndamento; track s) { <li>{{ situacoes[s] }} <span class="mono">{{ p.solicitacoesPorSituacao[s] ?? 0 }}</span></li> }</ul></div>
      </div>

      <h3 class="mo-grupo pc-titulo">Ordens atrasadas</h3>
      <div class="tbl-wrap card">
        <table class="pc-tabela" aria-label="Ordens atrasadas">
          <thead><tr><th scope="col">Ordem</th><th scope="col">Fornecedor</th><th scope="col">Unidade</th><th scope="col">Prazo</th><th scope="col">Atraso</th></tr></thead>
          <tbody>
            @for (o of p.atrasadas; track o.ordemId) { <tr><td class="mono">{{ o.numero }}</td><td>{{ o.fornecedorNome }}</td><td>{{ o.unidadeNome }}</td><td class="mono">{{ data(o.prazoEm) }}</td><td><span class="tag tag--alert">{{ o.diasDeAtraso }} dia(s)</span></td></tr> }
            @empty { <tr><td colspan="5" class="cell-sub">Nenhuma ordem atrasada.</td></tr> }
          </tbody>
        </table>
      </div>

      <h3 class="mo-grupo pc-titulo">Desempenho dos fornecedores</h3>
      <div class="tbl-wrap card">
        <table class="pc-tabela" aria-label="Desempenho dos fornecedores">
          <thead><tr><th scope="col">Fornecedor</th><th scope="col">Ordens</th><th scope="col">Entregas no prazo</th><th scope="col">Prazo médio</th><th scope="col">Recusado</th><th scope="col">Ocorrências</th></tr></thead>
          <tbody>
            @for (f of p.fornecedores; track f.fornecedorId) {
              <tr><td>{{ f.fornecedorNome }}</td><td class="mono">{{ f.ordens }}<span class="cell-sub">{{ f.entregas }} entrega(s)</span></td>
                <td class="mono">{{ f.percentualNoPrazo == null ? '—' : f.percentualNoPrazo.toLocaleString('pt-BR') + '%' }}</td>
                <td class="mono">{{ f.prazoMedioDias == null ? '—' : f.prazoMedioDias.toLocaleString('pt-BR') + ' dia(s)' }}</td>
                <td class="mono">{{ f.quantidadeRecusada }}</td><td class="mono">{{ f.ocorrencias }}</td></tr>
            } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma ordem no período.</td></tr> }
          </tbody>
        </table>
      </div>
    }

    <h3 class="mo-grupo pc-titulo">Ocorrências abertas</h3>
    <div class="tbl-wrap card">
      <table class="pc-tabela" aria-label="Ocorrências abertas">
        <thead><tr><th scope="col">Ocorrência</th><th scope="col">Fornecedor</th><th scope="col">Ordem</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
        <tbody>
          @for (o of ocorrencias(); track o.uuid) {
            <tr><td><span class="cell-main">{{ tipos[o.tipo] }}</span><span class="cell-sub" style="white-space: normal;">{{ data(o.registradaEm) }} · {{ o.descricao }}</span></td><td>{{ o.fornecedorNome }}</td><td class="mono">{{ o.ordemNumero ?? '—' }}</td>
              <td class="row-actions"><button type="button" class="link-btn" (click)="tratar(o)" [attr.aria-label]="'Tratar ocorrência ' + tipos[o.tipo] + ' de ' + o.fornecedorNome">Tratar</button></td></tr>
          } @empty { <tr><td colspan="4" class="cell-sub">Nenhuma ocorrência aberta.</td></tr> }
        </tbody>
      </table>
    </div>

    @if (tratando(); as o) {
      <app-drawer [titulo]="'Ocorrência: ' + tipos[o.tipo]" (fechar)="tratando.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Fornecedor</dt><dd>{{ o.fornecedorNome }}</dd></div>
            <div><dt>Ordem</dt><dd>{{ o.ordemNumero ?? '—' }}</dd></div>
            <div><dt>Descrição</dt><dd>{{ o.descricao }}</dd></div>
          </dl>
          <fieldset class="pc-grupo"><legend>Encerrar com a resposta do fornecedor</legend>
            <div class="dw-field"><label for="f-pc-resposta">Resposta</label><input id="f-pc-resposta" type="text" maxlength="500" [value]="f()['resposta'] ?? ''" (input)="set('resposta', $event)" /></div>
            <div class="row-actions"><button type="button" class="btn btn--sm" (click)="encerrar(o)" [disabled]="enviando()">Encerrar ocorrência</button></div>
          </fieldset>
          <fieldset class="pc-grupo"><legend>Converter em sanção (art. 156)</legend>
            <div class="dw-row">
              <div class="dw-field" style="flex: 2;"><label for="f-pc-sancao">Sanção</label>
                <select id="f-pc-sancao" (change)="set('tipo', $event)">
                  @for (t of listaSancoes; track t) { <option [value]="t" [selected]="f()['tipo'] === t">{{ sancoes[t] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-pc-inicio">Início</label><input id="f-pc-inicio" type="date" [value]="f()['inicio'] ?? ''" (input)="set('inicio', $event)" /></div>
              @if (f()['tipo'] === 'IMPEDIMENTO' || f()['tipo'] === 'INIDONEIDADE') {
                <div class="dw-field"><label for="f-pc-fim">Fim</label><input id="f-pc-fim" type="date" [value]="f()['fim'] ?? ''" (input)="set('fim', $event)" /></div>
              }
              @if (f()['tipo'] === 'MULTA') {
                <div class="dw-field"><label for="f-pc-valor">Valor (R$)</label><input id="f-pc-valor" type="number" min="0" step="0.01" [value]="f()['valor'] ?? ''" (input)="set('valor', $event)" /></div>
              }
            </div>
            <div class="dw-field"><label for="f-pc-motivo">Motivo</label><input id="f-pc-motivo" type="text" maxlength="1000" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)" /></div>
            <div class="row-actions"><button type="button" class="btn btn--ghost btn--sm" (click)="sancionar(o)" [disabled]="enviando()">Aplicar sanção</button></div>
          </fieldset>
        </div>
      </app-drawer>
    }
  `,
  styles: `
    .pc-grade { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: var(--s-3); margin-top: var(--s-3); }
    .pc-grade .card { padding: var(--s-3); }
    .pc-lista { margin: 0; padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .pc-lista li { display: flex; justify-content: space-between; gap: var(--s-2); }
    .pc-titulo { margin-top: var(--s-4); }
    .pc-tabela { min-width: 0; width: 100%; }
    .pc-grupo { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); margin: var(--s-2) 0; display: grid; gap: var(--s-1); }
  `,
})
export class PainelCompras {
  private readonly service = inject(ComprasService);

  protected readonly situacoes = SITUACOES_SOLICITACAO;
  protected readonly situacoesEmAndamento: SituacaoSolicitacaoCompra[] = ['RASCUNHO', 'ENVIADA', 'DEVOLVIDA', 'APROVADA', 'EM_COTACAO', 'COTADA'];
  protected readonly tipos = TIPOS_OCORRENCIA;
  protected readonly sancoes = SANCOES;
  protected readonly listaSancoes = Object.keys(SANCOES) as TipoSancaoFornecedor[];
  protected readonly painel = signal<PainelComprasDto | null>(null);
  protected readonly ocorrencias = signal<OcorrenciaDto[]>([]);
  protected readonly tratando = signal<OcorrenciaDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.service.painel().subscribe({ next: (p) => this.painel.set(p), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
    this.service.ocorrencias('ABERTA').subscribe({ next: (l) => this.ocorrencias.set(l), error: () => this.ocorrencias.set([]) });
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected tratar(o: OcorrenciaDto): void {
    this.erroGaveta.set(null);
    this.f.set({ tipo: 'ADVERTENCIA', inicio: hojeIso(), motivo: o.descricao.slice(0, 1000) });
    this.tratando.set(o);
  }

  protected encerrar(o: OcorrenciaDto): void {
    const resposta = (this.f()['resposta'] ?? '').trim();
    if (!resposta) {
      this.erroGaveta.set('Informe a resposta do fornecedor.');
      return;
    }
    this.concluir(this.service.encerrarOcorrencia(o.uuid, resposta));
  }

  protected sancionar(o: OcorrenciaDto): void {
    const c = this.f();
    const tipo = c['tipo'] as TipoSancaoFornecedor;
    const comFim = tipo === 'IMPEDIMENTO' || tipo === 'INIDONEIDADE';
    if (!c['inicio'] || !(c['motivo'] ?? '').trim() || (comFim && !c['fim']) || (tipo === 'MULTA' && !(Number(c['valor']) > 0))) {
      this.erroGaveta.set('Informe o início, o motivo e, conforme a sanção, o fim ou o valor.');
      return;
    }
    this.concluir(this.service.sancionarOcorrencia(o.uuid, { tipo, inicio: c['inicio'], fim: comFim ? c['fim'] : null,
      valor: tipo === 'MULTA' ? Number(c['valor']) : null, motivo: c['motivo'].trim(), processo: null }));
  }

  private concluir(req: ReturnType<ComprasService['encerrarOcorrencia']>): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    req.subscribe({
      next: () => {
        this.enviando.set(false);
        this.tratando.set(null);
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

/** Hoje no fuso de São Paulo, o dia que o servidor usa nas validações de data (o navegador pode estar em outro fuso). */
function hojeIso(): string {
  return new Date().toLocaleDateString('en-CA', { timeZone: 'America/Sao_Paulo' });
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
