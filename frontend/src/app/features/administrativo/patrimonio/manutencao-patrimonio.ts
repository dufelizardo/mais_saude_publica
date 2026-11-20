import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { MaterialEstoqueDto } from '../../../core/models/estoque';
import {
  ContratoServicoDto,
  ExecucaoManutencao,
  ManutencaoDoBemDto,
  OrdemServicoDto,
  PRIORIDADES_MANUTENCAO,
  PrioridadeManutencao,
  ResumoManutencaoDto,
  SITUACOES_ORDEM_SERVICO,
  SituacaoOrdemServico,
  TIPOS_MANUTENCAO,
  TipoManutencao,
} from '../../../core/models/patrimonio';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { EstoqueService } from '../../../core/services/estoque';
import { PatrimonioService } from '../../../core/services/patrimonio';
import { Drawer } from '../../../shared/drawer/drawer';

/** Uma linha de peça na conclusão. */
interface LinhaPeca {
  materialId: string;
  descricao: string;
  quantidade: string;
  custoUnitario: string;
}

/**
 * Manutenção do Patrimônio, fatia PM2 (ADR-0216), em três usos:
 * - `painel`: a aba Manutenção, com o resumo, as ordens e a gaveta da ordem (triagem, início, conclusão com as peças, cancelamento);
 * - `bem`: a seção da ficha do bem, com o chamado (para qualquer pessoa), os planos e o histórico;
 * - `meus`: os chamados abertos por quem está logado.
 */
@Component({
  selector: 'app-manutencao-patrimonio',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (aviso()) { <div class="box box--info" role="status"><strong>{{ aviso() }}</strong></div> }

    @switch (modo) {
      @case ('painel') {
        @if (resumo(); as r) {
          <div class="mn-numeros" aria-label="Resumo da manutenção">
            <div class="card mn-num"><span class="mn-num__v">{{ r.abertas }}</span><span class="cell-sub">ordens abertas</span></div>
            <div class="card mn-num"><span class="mn-num__v">{{ r.emExecucao }}</span><span class="cell-sub">em execução</span></div>
            <div class="card mn-num"><span class="mn-num__v">{{ r.paradas }}</span><span class="cell-sub">com o bem parado</span></div>
            <div class="card mn-num"><span class="mn-num__v">{{ r.calibracoesVencidas.length }}</span><span class="cell-sub">calibrações vencidas</span></div>
          </div>
          @if (r.calibracoesVencidas.length) {
            <div class="box box--warn" role="status"><strong>Calibração vencida:</strong>
              @for (p of r.calibracoesVencidas; track p.uuid) { <span> {{ p.numeroTombamento }} {{ p.bemDescricao }} ({{ p.unidadeNome }});</span> }</div>
          }
          <h3 class="mo-grupo mn-titulo">Planos vencendo em 30 dias</h3>
          <ul class="mn-lista" aria-label="Planos vencendo">
            @for (p of r.planosVencendo; track p.uuid) {
              <li><span><span class="mono">{{ p.numeroTombamento }}</span> {{ p.bemDescricao }} <span class="cell-sub">{{ p.unidadeNome }}</span></span>
                <span class="cell-sub">{{ tipos[p.tipo] }} · {{ data(p.proximaEm) }}@if (p.vencido) { · <span class="tag tag--alert">vencido</span> }</span></li>
            } @empty { <li class="cell-sub">Nenhum plano vencendo.</li> }
          </ul>
        }
        <div class="mn-cab">
          <div class="dw-field"><label for="f-mn-situacao">Situação</label>
            <select id="f-mn-situacao" (change)="situacao.set($any($event.target).value); carregarOrdens()">
              <option value="">Todas</option>
              @for (s of listaSituacoes; track s) { <option [value]="s">{{ situacoes[s] }}</option> }
            </select></div>
        </div>
        <div class="tbl-wrap card">
          <table class="mn-tabela" aria-label="Ordens de serviço">
            <thead><tr><th scope="col">Ordem</th><th scope="col">Bem</th><th scope="col">Tipo</th><th scope="col">Prioridade</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (o of ordens(); track o.uuid) {
                <tr>
                  <td><span class="cell-main mono">{{ o.numero }}</span><span class="cell-sub">{{ data(o.abertaEm) }}</span></td>
                  <td><span class="cell-main">{{ o.bemDescricao }}</span><span class="cell-sub">{{ o.numeroTombamento }} · {{ o.unidadeNome }}</span></td>
                  <td>{{ tipos[o.tipo] }}@if (o.paradoDesde && (o.situacao === 'ABERTA' || o.situacao === 'EM_EXECUCAO')) { <span class="cell-sub">parado há {{ o.diasParado }} dia(s)</span> }</td>
                  <td>{{ o.prioridade ? prioridades[o.prioridade] : '—' }}</td>
                  <td><span [class]="'tag tag--' + tag(o.situacao)">{{ situacoes[o.situacao] }}</span></td>
                  <td class="row-actions"><button type="button" class="link-btn" (click)="abrirOrdem(o)" [attr.aria-label]="'Abrir ordem ' + o.numero">Abrir</button></td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma ordem de serviço.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('bem') {
        <h3 class="mo-grupo mn-titulo">Manutenção</h3>
        @if (podeAbrirChamado) {
          <div class="mn-linha">
            <div class="dw-field" style="flex: 1;"><label for="f-mn-defeito">Abrir chamado: defeito</label><input id="f-mn-defeito" type="text" maxlength="1000" [value]="f()['defeito'] ?? ''" (input)="set('defeito', $event)" /></div>
            <label class="mn-check"><input id="f-mn-parado" type="checkbox" [checked]="f()['parado'] === 'true'" (change)="f.set({ ...f(), parado: $any($event.target).checked ? 'true' : '' })" /> Bem parado</label>
            <button type="button" class="btn btn--ghost btn--sm" (click)="abrirChamado()" [disabled]="enviando()">Abrir chamado</button>
          </div>
        }
        @if (doBem(); as m) {
          <p class="cell-sub">Custo acumulado {{ moeda(m.custoTotal) }} · {{ m.diasParado }} dia(s) parado@if (m.calibracaoValidaAte) { · calibração válida até {{ data(m.calibracaoValidaAte) }} }@if (m.calibracaoVencida) { · <span class="tag tag--alert">calibração vencida</span> }</p>
          <ul class="mn-lista" aria-label="Planos de manutenção">
            @for (p of m.planos; track p.uuid) {
              <li><span>{{ tipos[p.tipo] }} a cada {{ p.periodicidadeDias }} dias@if (!p.ativo) { <span class="cell-sub">(inativo)</span> }</span>
                <span class="cell-sub">próxima {{ data(p.proximaEm) }}@if (p.vencido) { · <span class="tag tag--alert">vencido</span> }
                  @if (p.ativo && podeExecutar) { <button type="button" class="link-btn" (click)="desativarPlano(p.uuid)" [attr.aria-label]="'Desativar plano de ' + tipos[p.tipo]">Desativar</button> }</span></li>
            } @empty { <li class="cell-sub">Sem plano de manutenção.</li> }
          </ul>
          @if (podeExecutar) {
            <div class="mn-linha">
              <div class="dw-field"><label for="f-mn-plano-tipo">Novo plano</label>
                <select id="f-mn-plano-tipo" (change)="set('planoTipo', $event)">
                  <option value="">Tipo…</option>
                  @for (t of tiposPlano; track t) { <option [value]="t">{{ tipos[t] }}</option> }
                </select></div>
              <div class="dw-field" style="max-width: 120px;"><label for="f-mn-plano-dias">A cada (dias)</label><input id="f-mn-plano-dias" type="number" min="1" max="1825" (input)="set('planoDias', $event)" /></div>
              <div class="dw-field"><label for="f-mn-plano-ultima">Última execução<span class="opt">opcional</span></label><input id="f-mn-plano-ultima" type="date" [max]="hoje" (input)="set('planoUltima', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="criarPlano()" [disabled]="enviando()">Criar plano</button>
            </div>
          }
          <ul class="mn-lista" aria-label="Ordens de serviço do bem">
            @for (o of m.ordens; track o.uuid) {
              <li><span><span class="mono">{{ o.numero }}</span> {{ tipos[o.tipo] }}@if (o.defeito) { <span class="cell-sub">{{ o.defeito }}</span> }</span>
                <span class="cell-sub">{{ situacoes[o.situacao] }}@if (o.situacao === 'CONCLUIDA') { · {{ moeda(o.custoTotal) }} }</span></li>
            }
          </ul>
        }
      }
      @case ('meus') {
        <h3 class="mo-grupo mn-titulo">Meus chamados</h3>
        <div class="tbl-wrap card">
          <table class="mn-tabela" aria-label="Meus chamados">
            <thead><tr><th scope="col">Chamado</th><th scope="col">Bem</th><th scope="col">Defeito</th><th scope="col">Situação</th></tr></thead>
            <tbody>
              @for (o of ordens(); track o.uuid) {
                <tr>
                  <td><span class="cell-main mono">{{ o.numero }}</span><span class="cell-sub">{{ data(o.abertaEm) }}</span></td>
                  <td><span class="cell-main">{{ o.bemDescricao }}</span><span class="cell-sub">{{ o.numeroTombamento }}</span></td>
                  <td>{{ o.defeito }}</td>
                  <td><span [class]="'tag tag--' + tag(o.situacao)">{{ situacoes[o.situacao] }}</span></td>
                </tr>
              } @empty { <tr><td colspan="4" class="cell-sub">Nenhum chamado aberto por você.</td></tr> }
            </tbody>
          </table>
        </div>
      }
    }

    @if (ordem(); as o) {
      <app-drawer [titulo]="'Ordem ' + o.numero" (fechar)="ordem.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Bem</dt><dd>{{ o.numeroTombamento }} · {{ o.bemDescricao }} · {{ o.unidadeNome }}</dd></div>
            <div><dt>Tipo</dt><dd>{{ tipos[o.tipo] }}@if (o.prioridade) { · prioridade {{ prioridades[o.prioridade] }} }</dd></div>
            <div><dt>Situação</dt><dd>{{ situacoes[o.situacao] }}@if (o.motivoCancelamento) { · {{ o.motivoCancelamento }} }</dd></div>
            @if (o.defeito) { <div><dt>Defeito</dt><dd>{{ o.defeito }}@if (o.bemParado) { · bem parado }</dd></div> }
            @if (o.execucao) { <div><dt>Execução</dt><dd>{{ o.execucao === 'INTERNA' ? 'Equipe interna · ' + (o.tecnico ?? '') : 'Contrato ' + o.instrumentoNumero }}</dd></div> }
            @if (o.servicoRealizado) { <div><dt>Serviço</dt><dd>{{ o.servicoRealizado }} · {{ moeda(o.custoTotal) }}@if (o.diasParado != null) { · {{ o.diasParado }} dia(s) parado }</dd></div> }
            @if (o.certificado) { <div><dt>Calibração</dt><dd>certificado {{ o.certificado }}, válida até {{ data(o.calibracaoValidaAte!) }}</dd></div> }
          </dl>
          @if (o.pecas.length) {
            <ul class="mn-lista" aria-label="Peças">@for (p of o.pecas; track p.uuid) { <li><span>{{ p.quantidade }} × {{ p.descricao }}</span><span class="cell-sub">{{ p.custoUnitario != null ? moeda(p.custoUnitario) : '—' }}</span></li> }</ul>
          }

          @if (o.situacao === 'ABERTA') {
            <h3 class="mo-grupo mn-titulo">Triagem e início</h3>
            <div class="mn-linha">
              <div class="dw-field"><label for="f-mn-prioridade">Prioridade</label>
                <select id="f-mn-prioridade" (change)="set('prioridade', $event)">
                  @for (p of listaPrioridades; track p) { <option [value]="p" [selected]="(f()['prioridade'] || o.prioridade || 'NORMAL') === p">{{ prioridades[p] }}</option> }
                </select></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="triar(o)" [disabled]="enviando()">Salvar prioridade</button>
            </div>
            <div class="mn-linha">
              <div class="dw-field"><label for="f-mn-execucao">Execução</label>
                <select id="f-mn-execucao" (change)="set('execucao', $event)">
                  <option value="INTERNA" [selected]="f()['execucao'] !== 'CONTRATO'">Equipe interna</option>
                  <option value="CONTRATO" [selected]="f()['execucao'] === 'CONTRATO'">Contrato de serviço</option>
                </select></div>
              @if (f()['execucao'] === 'CONTRATO') {
                <div class="dw-field" style="flex: 1;"><label for="f-mn-contrato">Contrato</label>
                  <select id="f-mn-contrato" (change)="set('instrumentoId', $event)">
                    <option value="">Escolha…</option>
                    @for (c of contratos(); track c.uuid) { <option [value]="c.uuid">{{ c.numero }} · {{ c.fornecedorNome }}</option> }
                  </select></div>
              } @else {
                <div class="dw-field" style="flex: 1;"><label for="f-mn-tecnico">Técnico</label><input id="f-mn-tecnico" type="text" maxlength="150" (input)="set('tecnico', $event)" /></div>
              }
              <button type="button" class="btn btn--sm" (click)="iniciar(o)" [disabled]="enviando()">Iniciar</button>
            </div>
          }

          @if (o.situacao === 'EM_EXECUCAO') {
            <h3 class="mo-grupo mn-titulo">Conclusão</h3>
            <div class="dw-field"><label for="f-mn-servico">Serviço realizado <span class="req" aria-hidden="true">*</span></label><textarea id="f-mn-servico" rows="2" maxlength="1000" (input)="set('servico', $event)"></textarea></div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-mn-mao">Mão de obra (R$)<span class="opt">opcional</span></label><input id="f-mn-mao" type="number" min="0" step="0.01" (input)="set('maoDeObra', $event)" /></div>
              @if (o.tipo === 'CALIBRACAO') {
                <div class="dw-field"><label for="f-mn-certificado">Certificado</label><input id="f-mn-certificado" type="text" maxlength="60" (input)="set('certificado', $event)" /></div>
                <div class="dw-field"><label for="f-mn-validade">Válida até</label><input id="f-mn-validade" type="date" [min]="hoje" (input)="set('validade', $event)" /></div>
              }
            </div>
            <ul class="mn-pecas" aria-label="Peças da conclusão">
              @for (l of pecas(); track $index; let i = $index) {
                <li class="mn-linha">
                  <div class="dw-field" style="flex: 2;"><label [for]="'f-mn-peca-material-' + i">Material do Estoque</label>
                    <select [id]="'f-mn-peca-material-' + i" (change)="mudarPeca(i, 'materialId', $event)">
                      <option value="">— peça trazida pelo contrato —</option>
                      @for (m of materiais(); track m.uuid) { <option [value]="m.uuid" [selected]="l.materialId === m.uuid">{{ m.nome }} ({{ m.codigo }})</option> }
                    </select></div>
                  @if (!l.materialId) { <div class="dw-field" style="flex: 2;"><label [for]="'f-mn-peca-desc-' + i">Descrição</label><input [id]="'f-mn-peca-desc-' + i" type="text" maxlength="300" [value]="l.descricao" (input)="mudarPeca(i, 'descricao', $event)" /></div> }
                  <div class="dw-field" style="max-width: 90px;"><label [for]="'f-mn-peca-qtd-' + i">Qtd.</label><input [id]="'f-mn-peca-qtd-' + i" type="number" min="1" [value]="l.quantidade" (input)="mudarPeca(i, 'quantidade', $event)" /></div>
                  <div class="dw-field" style="max-width: 120px;"><label [for]="'f-mn-peca-custo-' + i">Custo un.</label><input [id]="'f-mn-peca-custo-' + i" type="number" min="0" step="0.01" [value]="l.custoUnitario" (input)="mudarPeca(i, 'custoUnitario', $event)" /></div>
                  <button type="button" class="link-btn" (click)="removerPeca(i)" [attr.aria-label]="'Remover peça ' + (i + 1)">Remover</button>
                </li>
              }
            </ul>
            <div class="mn-linha">
              <button type="button" class="btn btn--ghost btn--sm" (click)="adicionarPeca()">Adicionar peça</button>
              <button type="button" class="btn btn--sm" (click)="concluir(o)" [disabled]="enviando()">Concluir</button>
            </div>
          }

          @if (o.situacao === 'ABERTA' || o.situacao === 'EM_EXECUCAO') {
            <div class="mn-linha mn-cancelar">
              <div class="dw-field" style="flex: 1;"><label for="f-mn-motivo">Motivo do cancelamento</label><input id="f-mn-motivo" type="text" maxlength="500" (input)="set('motivo', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="cancelar(o)" [disabled]="enviando()">Cancelar ordem</button>
            </div>
          }
        </div>
      </app-drawer>
    }
  `,
  styles: `
    .mn-numeros { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: var(--s-2); margin-bottom: var(--s-3); }
    .mn-num { padding: var(--s-3); display: grid; gap: 2px; }
    .mn-num__v { font-size: 1.5rem; font-weight: 600; }
    .mn-cab { display: flex; gap: var(--s-3); align-items: flex-end; margin: var(--s-3) 0 var(--s-2); flex-wrap: wrap; }
    .mn-cab .dw-field, .mn-linha .dw-field { margin: 0; }
    .mn-tabela { min-width: 0; width: 100%; }
    .mn-titulo { margin-top: var(--s-4); }
    .mn-linha { display: flex; gap: var(--s-2); align-items: flex-end; flex-wrap: wrap; margin-bottom: var(--s-2); }
    .mn-check { display: flex; gap: var(--s-1); align-items: center; }
    .mn-lista, .mn-pecas { margin: 0 0 var(--s-2); padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .mn-lista li { display: flex; justify-content: space-between; gap: var(--s-2); align-items: baseline; flex-wrap: wrap; }
    .mn-cancelar { margin-top: var(--s-4); }
  `,
})
export class ManutencaoPatrimonio implements OnInit {
  @Input({ required: true }) modo!: 'painel' | 'bem' | 'meus';
  @Input() bemId: string | null = null;
  @Input() podeExecutar = false;
  @Input() podeAbrirChamado = false;

  private readonly service = inject(PatrimonioService);
  private readonly estoqueService = inject(EstoqueService);

  protected readonly tipos = TIPOS_MANUTENCAO;
  protected readonly tiposPlano: TipoManutencao[] = ['PREVENTIVA', 'CALIBRACAO', 'SEGURANCA_ELETRICA'];
  protected readonly prioridades = PRIORIDADES_MANUTENCAO;
  protected readonly listaPrioridades = Object.keys(PRIORIDADES_MANUTENCAO) as PrioridadeManutencao[];
  protected readonly situacoes = SITUACOES_ORDEM_SERVICO;
  protected readonly listaSituacoes = Object.keys(SITUACOES_ORDEM_SERVICO) as SituacaoOrdemServico[];
  protected readonly hoje = new Date().toLocaleDateString('en-CA', { timeZone: 'America/Sao_Paulo' });

  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly situacao = signal('');
  protected readonly resumo = signal<ResumoManutencaoDto | null>(null);
  protected readonly ordens = signal<OrdemServicoDto[]>([]);
  protected readonly doBem = signal<ManutencaoDoBemDto | null>(null);
  protected readonly ordem = signal<OrdemServicoDto | null>(null);
  protected readonly contratos = signal<ContratoServicoDto[]>([]);
  protected readonly materiais = signal<MaterialEstoqueDto[]>([]);
  protected readonly pecas = signal<LinhaPeca[]>([]);

  ngOnInit(): void {
    if (this.modo === 'painel') {
      this.carregarPainel();
    } else if (this.modo === 'bem') {
      this.carregarDoBem();
    } else {
      this.service.meusChamados().pipe(catchError(() => of([]))).subscribe((l) => this.ordens.set(l));
    }
  }

  private carregarPainel(): void {
    this.service.resumoManutencao().subscribe({ next: (r) => this.resumo.set(r), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
    this.carregarOrdens();
  }

  protected carregarOrdens(): void {
    this.service.ordensServico((this.situacao() || undefined) as SituacaoOrdemServico | undefined).subscribe({
      next: (l) => this.ordens.set(l),
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  private carregarDoBem(): void {
    if (!this.bemId) return;
    this.service.manutencaoDoBem(this.bemId).pipe(catchError(() => of(null))).subscribe((m) => this.doBem.set(m));
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  // ── Ficha do bem ──

  protected abrirChamado(): void {
    const defeito = this.f()['defeito']?.trim();
    if (!this.bemId || !defeito) {
      this.erro.set('Descreva o defeito.');
      return;
    }
    this.executar(this.service.abrirChamado(this.bemId, defeito, this.f()['parado'] === 'true'), (o) => {
      this.aviso.set(`Chamado ${o.numero} aberto.`);
      this.f.set({});
      this.carregarDoBem();
    });
  }

  protected criarPlano(): void {
    const v = this.f();
    if (!this.bemId || !v['planoTipo'] || !v['planoDias']) {
      this.erro.set('Escolha o tipo e a periodicidade do plano.');
      return;
    }
    this.executar(this.service.criarPlano(this.bemId, { tipo: v['planoTipo'] as TipoManutencao, periodicidadeDias: Number(v['planoDias']),
      ultimaExecucao: v['planoUltima'] || null, descricao: null }), (m) => {
      this.doBem.set(m);
      this.f.set({});
      this.aviso.set('Plano criado.');
    });
  }

  protected desativarPlano(planoId: string): void {
    this.executar(this.service.desativarPlano(planoId), (m) => this.doBem.set(m));
  }

  // ── Ordem ──

  protected abrirOrdem(o: OrdemServicoDto): void {
    this.ordem.set(o);
    this.f.set({});
    this.pecas.set([]);
    this.erroGaveta.set(null);
    if (!this.contratos().length) this.service.contratosDeServico().pipe(catchError(() => of([]))).subscribe((l) => this.contratos.set(l));
    if (!this.materiais().length) this.estoqueService.materiais(true).pipe(catchError(() => of([]))).subscribe((l) => this.materiais.set(l));
  }

  protected triar(o: OrdemServicoDto): void {
    this.naGaveta(this.service.triar(o.uuid, (this.f()['prioridade'] || o.prioridade || 'NORMAL') as PrioridadeManutencao));
  }

  protected iniciar(o: OrdemServicoDto): void {
    const execucao = (this.f()['execucao'] || 'INTERNA') as ExecucaoManutencao;
    this.naGaveta(this.service.iniciar(o.uuid, execucao, execucao === 'CONTRATO' ? this.f()['instrumentoId'] || null : null,
      execucao === 'INTERNA' ? this.f()['tecnico']?.trim() || null : null));
  }

  protected concluir(o: OrdemServicoDto): void {
    const v = this.f();
    if (!v['servico']?.trim()) {
      this.erroGaveta.set('Descreva o serviço realizado.');
      return;
    }
    this.naGaveta(this.service.concluir(o.uuid, {
      servicoRealizado: v['servico'].trim(), custoMaoDeObra: v['maoDeObra'] ? Number(v['maoDeObra']) : null,
      certificado: v['certificado']?.trim() || null, calibracaoValidaAte: v['validade'] || null,
      pecas: this.pecas().map((l) => ({ materialId: l.materialId || null, descricao: l.materialId ? null : l.descricao.trim() || null,
        quantidade: Number(l.quantidade) || 0, custoUnitario: l.custoUnitario ? Number(l.custoUnitario) : null })),
    }));
  }

  protected cancelar(o: OrdemServicoDto): void {
    const motivo = this.f()['motivo']?.trim();
    if (!motivo) {
      this.erroGaveta.set('Informe o motivo do cancelamento.');
      return;
    }
    this.naGaveta(this.service.cancelarOrdem(o.uuid, motivo));
  }

  protected adicionarPeca(): void {
    this.pecas.set([...this.pecas(), { materialId: '', descricao: '', quantidade: '1', custoUnitario: '' }]);
  }

  protected removerPeca(i: number): void {
    this.pecas.set(this.pecas().filter((_, k) => k !== i));
  }

  protected mudarPeca(i: number, campo: keyof LinhaPeca, ev: Event): void {
    const valor = (ev.target as HTMLInputElement).value;
    this.pecas.set(this.pecas().map((l, k) => (k === i ? { ...l, [campo]: valor } : l)));
  }

  private naGaveta(chamada: Observable<OrdemServicoDto>): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    chamada.subscribe({
      next: (o) => {
        this.enviando.set(false);
        this.ordem.set(o);
        this.f.set({});
        this.pecas.set([]);
        this.carregarPainel();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  private executar<T>(chamada: Observable<T>, depois: (r: T) => void): void {
    this.enviando.set(true);
    this.erro.set(null);
    chamada.subscribe({
      next: (r) => {
        this.enviando.set(false);
        depois(r);
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set(mensagem(x));
      },
    });
  }

  // ── Formatação ──

  protected tag(s: SituacaoOrdemServico): string {
    return s === 'ABERTA' ? 'warn' : s === 'EM_EXECUCAO' ? 'info' : s === 'CONCLUIDA' ? 'ok' : 'muted';
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
