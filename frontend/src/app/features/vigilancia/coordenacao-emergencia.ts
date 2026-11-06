import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import {
  AcaoEmergencialDto,
  CapacidadeAtivadaDto,
  CapacidadeEmergencialDto,
  EmergenciaDto,
  MobilizacaoDto,
  PRIORIDADES_ACAO,
  PrioridadeAcaoEmergencial,
  RECURSOS_CAPACIDADE,
  SITUACOES_ACAO,
  SITUACOES_MOBILIZACAO,
  SituacaoAcaoEmergencial,
  SituacaoMobilizacao,
  TIPOS_MOBILIZACAO,
  TipoMobilizacao,
} from '../../core/models/emergencia';
import { ErrorResponseDto } from '../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { EmergenciaService } from '../../core/services/emergencia';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { Drawer } from '../../shared/drawer/drawer';

type Gaveta =
  | { tipo: 'acao' }
  | { tipo: 'situacaoAcao'; acao: AcaoEmergencialDto; situacao: SituacaoAcaoEmergencial }
  | { tipo: 'mobilizar' }
  | { tipo: 'situacaoMobilizacao'; mob: MobilizacaoDto; situacao: SituacaoMobilizacao }
  | { tipo: 'transferencia'; mob: MobilizacaoDto }
  | { tipo: 'capacidade' }
  | { tipo: 'desativarCapacidade'; cap: CapacidadeAtivadaDto };

/**
 * A coordenação da emergência (ADR-0158), no detalhe dela: o quadro de ações (atrasadas no alto; gerar do plano), as
 * mobilizações (o profissional mobilizado ganha turno na unidade de destino sem lotação; o medicamento segue a
 * transferência da Farmácia), a capacidade emergencial ativada e as unidades afetadas.
 */
@Component({
  selector: 'app-coordenacao-emergencia',
  imports: [Drawer],
  template: `
    @let e = emergencia();
    <div class="co-secao">
      <div class="co-cab">
        <h3 class="mo-grupo" style="margin: 0;">Ações@if (e.acoesAtrasadas) { <span class="tag tag--alert">{{ e.acoesAtrasadas }} atrasada(s)</span> }</h3>
        @if (ativa() && podeCoordenar()) {
          <div class="row-actions">
            <button type="button" class="btn btn--ghost btn--sm" (click)="executar(service.gerarAcoesDoPlano(e.uuid))">Gerar ações do plano</button>
            <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'acao' })">Nova ação</button>
          </div>
        }
      </div>
      @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
      @if (e.acoes.length) {
        <div class="tbl-wrap"><table class="co-tabela" aria-label="Ações emergenciais">
          <thead><tr><th scope="col">Ação</th><th scope="col">Prioridade</th><th scope="col">Prazo</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (a of e.acoes; track a.uuid) {
              <tr>
                <td><span class="cell-main">{{ a.descricao }}</span><span class="cell-sub">{{ a.responsavel }}@if (a.unidadeNome) { · {{ a.unidadeNome }} }@if (a.planoCodigo) { · plano {{ a.planoCodigo }} }@if (a.observacao) { · {{ a.observacao }} }@if (a.motivoCancelamento) { · {{ a.motivoCancelamento }} }</span></td>
                <td><span [class]="'tag tag--' + prioridades[a.prioridade].classe">{{ prioridades[a.prioridade].rotulo }}</span></td>
                <td class="mono">{{ a.prazo ? dataHora(a.prazo) : '—' }}@if (a.atrasada) { <span class="tag tag--alert">Atrasada</span> }</td>
                <td><span [class]="'tag tag--' + situacoesAcao[a.situacao].classe">{{ situacoesAcao[a.situacao].rotulo }}</span></td>
                <td class="row-actions">
                  @if (aberta() && podeCoordenar() && (a.situacao === 'PENDENTE' || a.situacao === 'EM_EXECUCAO')) {
                    @if (a.situacao === 'PENDENTE') { <button type="button" class="link-btn" (click)="abrir({ tipo: 'situacaoAcao', acao: a, situacao: 'EM_EXECUCAO' })" [attr.aria-label]="'Iniciar: ' + a.descricao">Iniciar</button> }
                    <button type="button" class="link-btn" (click)="abrir({ tipo: 'situacaoAcao', acao: a, situacao: 'CONCLUIDA' })" [attr.aria-label]="'Concluir: ' + a.descricao">Concluir</button>
                    <button type="button" class="link-btn" (click)="abrir({ tipo: 'situacaoAcao', acao: a, situacao: 'CANCELADA' })" [attr.aria-label]="'Cancelar: ' + a.descricao">Cancelar</button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table></div>
      } @else { <p class="cell-sub">Nenhuma ação ainda. Com um plano ativo, "Gerar ações do plano" traz as previstas do nível.</p> }
    </div>

    <div class="co-secao">
      <div class="co-cab">
        <h3 class="mo-grupo" style="margin: 0;">Mobilização</h3>
        @if (ativa() && podeCoordenar()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirMobilizar()">Mobilizar</button> }
      </div>
      @if (e.mobilizacoes.length) {
        <ul class="eq-prof" aria-label="Mobilizações">
          @for (m of e.mobilizacoes; track m.uuid) {
            <li class="mo-row">
              <span>
                <span class="cell-main">{{ tipos[m.tipo] }}: {{ m.recurso }}@if (m.quantidade) { · {{ m.quantidade }} } <span [class]="'tag tag--' + situacoesMob[m.situacao].classe">{{ situacoesMob[m.situacao].rotulo }}</span>@if (m.autorizaEscala) { <span class="tag tag--ok">Autoriza turno na Escala</span> }</span>
                <span class="cell-sub">@if (m.origemNome) { {{ m.origemNome }} → }{{ m.destinoNome }} · desde {{ data(m.inicio) }}@if (m.fim) { até {{ data(m.fim) }} }@if (m.responsavel) { · {{ m.responsavel }} }@if (m.transferenciaId) { · transferência da Farmácia ({{ m.transferenciaStatus }}) }@if (m.motivoCancelamento) { · {{ m.motivoCancelamento }} }</span>
              </span>
              @if (aberta() && podeCoordenar() && !m.transferenciaId && (m.situacao === 'SOLICITADA' || m.situacao === 'EM_ANDAMENTO')) {
                <span class="row-actions">
                  @if (m.situacao === 'SOLICITADA') { <button type="button" class="link-btn" (click)="abrir({ tipo: 'situacaoMobilizacao', mob: m, situacao: 'EM_ANDAMENTO' })" [attr.aria-label]="'Andamento: ' + m.recurso">Em andamento</button> }
                  @if (m.tipo === 'MEDICAMENTO') { <button type="button" class="link-btn" (click)="abrir({ tipo: 'transferencia', mob: m })" [attr.aria-label]="'Ligar transferência: ' + m.recurso">Ligar transferência</button> }
                  <button type="button" class="link-btn" (click)="abrir({ tipo: 'situacaoMobilizacao', mob: m, situacao: 'CONCLUIDA' })" [attr.aria-label]="'Concluir: ' + m.recurso">Concluir</button>
                  <button type="button" class="link-btn" (click)="abrir({ tipo: 'situacaoMobilizacao', mob: m, situacao: 'CANCELADA' })" [attr.aria-label]="'Cancelar: ' + m.recurso">Cancelar</button>
                </span>
              }
            </li>
          }
        </ul>
      } @else { <p class="cell-sub">Nenhum recurso mobilizado.</p> }
    </div>

    <div class="co-secao">
      <div class="co-cab">
        <h3 class="mo-grupo" style="margin: 0;">Capacidade emergencial</h3>
        @if (ativa() && podeCoordenar()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirCapacidade()">Ativar capacidade</button> }
      </div>
      @if (e.capacidades.length) {
        <ul class="eq-prof" aria-label="Capacidade ativada">
          @for (c of e.capacidades; track c.uuid) {
            <li class="mo-row">
              <span>
                <span class="cell-main">{{ c.unidadeNome }} · {{ recursos[c.recurso] }}: +{{ c.quantidade }} <span [class]="'tag tag--' + (c.situacao === 'ATIVA' ? 'ok' : 'muted')">{{ c.situacao === 'ATIVA' ? 'Ativa' : 'Desativada' }}</span></span>
                <span class="cell-sub">normal {{ c.normal }} · ativável {{ c.emergencial }} · {{ c.justificativa }}@if (c.justificativaFim) { · {{ c.justificativaFim }} }</span>
              </span>
              @if (ativa() && podeCoordenar() && c.situacao === 'ATIVA') { <button type="button" class="link-btn" (click)="abrir({ tipo: 'desativarCapacidade', cap: c })" [attr.aria-label]="'Desativar capacidade ' + c.unidadeNome">Desativar</button> }
            </li>
          }
        </ul>
      } @else { <p class="cell-sub">Nenhuma capacidade emergencial ativada.</p> }
    </div>

    @if (e.unidadesAfetadas.length) {
      <div class="co-secao">
        <h3 class="mo-grupo">Unidades afetadas</h3>
        <p class="cell-sub">Da última avaliação de impacto. A situação operacional muda em Administrativo · Equipamentos de Saúde.</p>
        <ul class="eq-prof" aria-label="Unidades afetadas">
          @for (u of e.unidadesAfetadas; track u.uuid) { <li><span class="cell-main">{{ u.nome }}</span><span class="cell-sub">{{ situacaoOperacional(u.situacaoOperacional) }}</span></li> }
        </ul>
      </div>
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @switch (g.tipo) {
              @case ('acao') {
                <div class="dw-field"><label for="f-co-descricao">Ação <span class="req" aria-hidden="true">*</span></label><input id="f-co-descricao" type="text" maxlength="1000" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)" /></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-co-responsavel">Responsável <span class="req" aria-hidden="true">*</span></label><input id="f-co-responsavel" type="text" maxlength="200" [value]="f()['responsavel'] ?? ''" (input)="set('responsavel', $event)" /></div>
                  <div class="dw-field"><label for="f-co-prioridade">Prioridade</label>
                    <select id="f-co-prioridade" (change)="f.set({ ...f(), prioridade: $any($event.target).value })">
                      @for (p of listaPrioridades; track p) { <option [value]="p" [selected]="(f()['prioridade'] || 'ALTA') === p">{{ prioridades[p].rotulo }}</option> }
                    </select></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-co-prazo">Prazo<span class="opt">opcional</span></label><input id="f-co-prazo" type="datetime-local" [value]="f()['prazo'] ?? ''" (input)="set('prazo', $event)" /></div>
                  <div class="dw-field"><label for="f-co-unidade">Unidade<span class="opt">opcional</span></label>
                    <select id="f-co-unidade" (change)="f.set({ ...f(), unidadeId: $any($event.target).value })">
                      <option value="" [selected]="!f()['unidadeId']">Nenhuma</option>
                      @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['unidadeId'] === u.uuid">{{ u.nome }}</option> }
                    </select></div>
                </div>
              }
              @case ('situacaoAcao') {
                <dl class="ro"><div><dt>Ação</dt><dd>{{ g.acao.descricao }}</dd></div><div><dt>Passa a</dt><dd>{{ situacoesAcao[g.situacao].rotulo }}</dd></div></dl>
                @if (g.situacao === 'CANCELADA') {
                  <div class="dw-field"><label for="f-co-motivo">Motivo <span class="req" aria-hidden="true">*</span></label><input id="f-co-motivo" type="text" maxlength="500" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)" /></div>
                } @else {
                  <div class="dw-field"><label for="f-co-observacao">Observação<span class="opt">opcional</span></label><input id="f-co-observacao" type="text" maxlength="1000" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" /></div>
                }
              }
              @case ('mobilizar') {
                <div class="dw-row">
                  <div class="dw-field"><label for="f-co-tipo">Tipo <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-co-tipo" (change)="f.set({ ...f(), tipo: $any($event.target).value })">
                      @for (t of listaTipos; track t) { <option [value]="t" [selected]="(f()['tipo'] || 'PROFISSIONAL') === t">{{ tipos[t] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-co-destino">Unidade de destino <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-co-destino" (change)="f.set({ ...f(), destinoId: $any($event.target).value })">
                      <option value="" [selected]="!f()['destinoId']">Escolha…</option>
                      @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['destinoId'] === u.uuid">{{ u.nome }}</option> }
                    </select></div>
                </div>
                @if ((f()['tipo'] || 'PROFISSIONAL') === 'PROFISSIONAL') {
                  <div class="dw-field"><label for="f-co-matricula">Matrícula do profissional <span class="req" aria-hidden="true">*</span></label><input id="f-co-matricula" type="text" [value]="f()['matricula'] ?? ''" (input)="set('matricula', $event)" /></div>
                  <div class="box box--info"><div>Enquanto a emergência estiver ativa e a mobilização em andamento, a Escala aceita turnos dele na unidade de destino, sem lotação lá. Os alertas de jornada e descanso continuam.</div></div>
                } @else {
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-co-recurso">Recurso <span class="req" aria-hidden="true">*</span></label><input id="f-co-recurso" type="text" maxlength="300" [value]="f()['recurso'] ?? ''" (input)="set('recurso', $event)" placeholder="Ex.: soro de reidratação oral" /></div>
                    <div class="dw-field"><label for="f-co-quantidade">Quantidade<span class="opt">opcional</span></label><input id="f-co-quantidade" type="number" min="1" [value]="f()['quantidade'] ?? ''" (input)="set('quantidade', $event)" /></div>
                  </div>
                }
                <div class="dw-row">
                  <div class="dw-field"><label for="f-co-origem">Origem<span class="opt">opcional</span></label>
                    <select id="f-co-origem" (change)="f.set({ ...f(), origemId: $any($event.target).value })">
                      <option value="" [selected]="!f()['origemId']">Nenhuma</option>
                      @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['origemId'] === u.uuid">{{ u.nome }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-co-fim">Até<span class="opt">opcional</span></label><input id="f-co-fim" type="date" [value]="f()['fim'] ?? ''" (input)="set('fim', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-co-responsavel">Responsável<span class="opt">opcional</span></label><input id="f-co-responsavel" type="text" maxlength="200" [value]="f()['responsavel'] ?? ''" (input)="set('responsavel', $event)" /></div>
              }
              @case ('situacaoMobilizacao') {
                <dl class="ro"><div><dt>Mobilização</dt><dd>{{ tipos[g.mob.tipo] }}: {{ g.mob.recurso }}</dd></div><div><dt>Passa a</dt><dd>{{ situacoesMob[g.situacao].rotulo }}</dd></div></dl>
                @if (g.situacao === 'CANCELADA') {
                  <div class="dw-field"><label for="f-co-motivo">Motivo <span class="req" aria-hidden="true">*</span></label><input id="f-co-motivo" type="text" maxlength="500" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)" /></div>
                }
                @if (g.mob.tipo === 'PROFISSIONAL' && g.situacao !== 'EM_ANDAMENTO') { <div class="box box--info"><div>A Escala deixa de aceitar turnos novos dele sem lotação; os já designados ficam.</div></div> }
              }
              @case ('transferencia') {
                <dl class="ro"><div><dt>Medicamento</dt><dd>{{ g.mob.recurso }} → {{ g.mob.destinoNome }}</dd></div></dl>
                <div class="dw-field"><label for="f-co-transferencia">Identificador da transferência <span class="req" aria-hidden="true">*</span></label><input id="f-co-transferencia" type="text" [value]="f()['transferencia'] ?? ''" (input)="set('transferencia', $event)" placeholder="Copie da Farmácia · Transferências" /></div>
                <div class="box box--info"><div>Ligada, a mobilização segue a transferência: recebida conclui, cancelada cancela.</div></div>
              }
              @case ('capacidade') {
                <div class="dw-field"><label for="f-co-capacidade">Capacidade <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-co-capacidade" (change)="f.set({ ...f(), capacidadeId: $any($event.target).value })">
                    <option value="" [selected]="!f()['capacidadeId']">Escolha…</option>
                    @for (c of capacidadesDisponiveis(); track c.uuid) { <option [value]="c.uuid" [selected]="f()['capacidadeId'] === c.uuid">{{ c.unidadeNome }} · {{ recursos[c.recurso] }} (resta {{ c.emergencial - c.ativadaAgora }})</option> }
                  </select>
                  @if (!capacidadesDisponiveis().length) { <p class="cell-sub">Nenhuma capacidade com saldo. Cadastre na aba Capacidade emergencial.</p> }
                </div>
                <div class="dw-field"><label for="f-co-qtd">Quantidade <span class="req" aria-hidden="true">*</span></label><input id="f-co-qtd" type="number" min="1" [value]="f()['quantidade'] ?? ''" (input)="set('quantidade', $event)" /></div>
                <div class="dw-field"><label for="f-co-justificativa">Justificativa <span class="req" aria-hidden="true">*</span></label><input id="f-co-justificativa" type="text" maxlength="1000" [value]="f()['justificativa'] ?? ''" (input)="set('justificativa', $event)" placeholder="Ex.: ala de observação aberta" /></div>
              }
              @case ('desativarCapacidade') {
                <dl class="ro"><div><dt>Capacidade</dt><dd>{{ g.cap.unidadeNome }} · {{ recursos[g.cap.recurso] }}: +{{ g.cap.quantidade }}</dd></div></dl>
                <div class="dw-field"><label for="f-co-justificativa">Justificativa <span class="req" aria-hidden="true">*</span></label><input id="f-co-justificativa" type="text" maxlength="500" [value]="f()['justificativa'] ?? ''" (input)="set('justificativa', $event)" /></div>
              }
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Voltar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar' }}</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .co-secao { margin-top: var(--s-4); }
    .co-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-2); flex-wrap: wrap; margin-bottom: var(--s-2); }
    .co-tabela { min-width: 0; width: 100%; }
  `,
})
export class CoordenacaoEmergencia {
  protected readonly service = inject(EmergenciaService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  readonly emergencia = input.required<EmergenciaDto>();
  readonly podeCoordenar = input(false);
  readonly alterada = output<EmergenciaDto>();

  protected readonly prioridades = PRIORIDADES_ACAO;
  protected readonly listaPrioridades = Object.keys(PRIORIDADES_ACAO) as PrioridadeAcaoEmergencial[];
  protected readonly situacoesAcao = SITUACOES_ACAO;
  protected readonly tipos = TIPOS_MOBILIZACAO;
  protected readonly listaTipos = Object.keys(TIPOS_MOBILIZACAO) as TipoMobilizacao[];
  protected readonly situacoesMob = SITUACOES_MOBILIZACAO;
  protected readonly recursos = RECURSOS_CAPACIDADE;

  protected readonly ativa = computed(() => this.emergencia().situacao === 'ATIVA');
  /** Ativa ou em desmobilização: o que ficou aberto ainda se conclui (ADR-0159). */
  protected readonly aberta = computed(() => this.emergencia().situacao !== 'ENCERRADA');
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly capacidades = signal<CapacidadeEmergencialDto[]>([]);
  protected readonly capacidadesDisponiveis = computed(() => this.capacidades().filter((c) => c.ativa && c.emergencial - c.ativadaAgora > 0));
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});

  protected titulo(g: Gaveta): string {
    switch (g.tipo) {
      case 'acao': return 'Nova ação emergencial';
      case 'situacaoAcao': return 'Situação da ação';
      case 'mobilizar': return 'Mobilizar recurso';
      case 'situacaoMobilizacao': return 'Situação da mobilização';
      case 'transferencia': return 'Ligar à transferência da Farmácia';
      case 'capacidade': return 'Ativar capacidade emergencial';
      default: return 'Desativar a capacidade';
    }
  }

  protected abrir(g: Gaveta): void {
    this.erroGaveta.set(null);
    this.f.set({});
    if (!this.unidades().length) this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.gaveta.set(g);
  }

  protected abrirMobilizar(): void {
    this.abrir({ tipo: 'mobilizar' });
  }

  protected abrirCapacidade(): void {
    this.service.capacidades().pipe(catchError(() => of([]))).subscribe((c) => this.capacidades.set(c));
    this.abrir({ tipo: 'capacidade' });
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  /** Ação sem gaveta (gerar do plano). */
  protected executar(req: Observable<EmergenciaDto>): void {
    this.erro.set(null);
    req.subscribe({ next: (d) => this.alterada.emit(d), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }

  protected salvar(g: Gaveta): void {
    if (this.enviando()) return;
    const id = this.emergencia().uuid;
    const f = this.f();
    const t = (k: string) => (f[k] ?? '').trim() || null;
    let req: Observable<EmergenciaDto>;
    let falta: string | null = null;
    switch (g.tipo) {
      case 'acao':
        if (!t('descricao') || !t('responsavel')) falta = 'Informe a ação e o responsável.';
        req = this.service.novaAcao(id, {
          descricao: t('descricao') ?? '', responsavel: t('responsavel') ?? '', unidadeId: t('unidadeId'),
          prioridade: (t('prioridade') ?? 'ALTA') as PrioridadeAcaoEmergencial, prazo: t('prazo') ? new Date(f['prazo']).toISOString() : null,
        });
        break;
      case 'situacaoAcao':
        if (g.situacao === 'CANCELADA' && !t('motivo')) falta = 'Informe o motivo.';
        req = this.service.situacaoDaAcao(id, g.acao.uuid, g.situacao, t('observacao'), t('motivo'));
        break;
      case 'mobilizar': {
        const tipo = (t('tipo') ?? 'PROFISSIONAL') as TipoMobilizacao;
        if (!t('destinoId')) falta = 'Escolha a unidade de destino.';
        else if (tipo === 'PROFISSIONAL' && !t('matricula')) falta = 'Informe a matrícula do profissional.';
        else if (tipo !== 'PROFISSIONAL' && !t('recurso')) falta = 'Descreva o recurso.';
        req = this.service.mobilizar(id, {
          tipo, origemId: t('origemId'), destinoId: t('destinoId') ?? '', profissionalMatricula: t('matricula'), recurso: t('recurso'),
          quantidade: t('quantidade') ? Number(f['quantidade']) : null, responsavel: t('responsavel'), inicio: null, fim: t('fim'),
        });
        break;
      }
      case 'situacaoMobilizacao':
        if (g.situacao === 'CANCELADA' && !t('motivo')) falta = 'Informe o motivo.';
        req = this.service.situacaoDaMobilizacao(id, g.mob.uuid, g.situacao, t('motivo'));
        break;
      case 'transferencia':
        if (!t('transferencia')) falta = 'Informe a transferência.';
        req = this.service.ligarTransferencia(id, g.mob.uuid, t('transferencia') ?? '');
        break;
      case 'capacidade':
        if (!t('capacidadeId') || !t('quantidade') || !t('justificativa')) falta = 'Escolha a capacidade e informe a quantidade e a justificativa.';
        req = this.service.ativarCapacidade(id, t('capacidadeId') ?? '', Number(f['quantidade']), t('justificativa') ?? '');
        break;
      default:
        if (!t('justificativa')) falta = 'Justifique a desativação.';
        req = this.service.desativarCapacidade(id, g.cap.uuid, t('justificativa') ?? '');
    }
    if (falta) {
      this.erroGaveta.set(falta);
      return;
    }
    this.enviando.set(true);
    req.subscribe({
      next: (d) => {
        this.enviando.set(false);
        this.gaveta.set(null);
        this.alterada.emit(d);
      },
      error: (e: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(e));
      },
    });
  }

  protected situacaoOperacional(s: string): string {
    return ({ EM_OPERACAO: 'Em operação', EM_OBRA: 'Em obra', INOPERANTE: 'Inoperante', EM_MANUTENCAO: 'Em manutenção' } as Record<string, string>)[s] ?? s;
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
