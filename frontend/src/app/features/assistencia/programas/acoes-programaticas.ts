import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../../core/models/profissional';
import {
  ACOES_SO_INDIVIDUAIS,
  AcaoProgramaticaDto,
  AcoesProgramaticasDto,
  AcompanhamentoDto,
  CampanhaDto,
  ProgramaResumoDto,
  RESULTADOS_BUSCA,
  ResultadoBuscaAtiva,
  SITUACOES_ACAO,
  TIPOS_ACAO,
  TipoAcaoProgramatica,
} from '../../../core/models/programa';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { ProgramaService } from '../../../core/services/programa';
import { Drawer } from '../../../shared/drawer/drawer';

type Periodo = 'semana' | 'quatro' | 'mes';
type Gaveta = { tipo: 'nova' } | { tipo: 'realizar'; acao: AcaoProgramaticaDto } | { tipo: 'situacao'; acao: AcaoProgramaticaDto; situacao: 'NAO_REALIZADA' | 'CANCELADA' };

/**
 * Ações programáticas (ADR-0132): as da semana (ou do período) no escopo, com as vencidas e as prontas para confirmar
 * primeiro; prever (individual pelo acompanhado, ou coletiva com os participantes), confirmar a realização (com o
 * atendimento sugerido e, na busca ativa, o resultado), não realizada, cancelar e agendar a consulta programada.
 */
@Component({
  selector: 'app-acoes-programaticas',
  imports: [ReactiveFormsModule, Drawer],
  template: `
    <div class="filters" style="margin-bottom: var(--s-3); flex-wrap: wrap;">
      @if (!programaFixo() && !campanhaFixa()) {
        <select class="sel" aria-label="Programa das ações" [value]="programaId()" (change)="programaId.set($any($event.target).value)">
          <option value="">Todos os programas</option>
          @for (p of programas(); track p.uuid) { <option [value]="p.uuid">{{ p.nome }}</option> }
        </select>
      }
      @if (!campanhaFixa()) {
        <div class="seg" role="group" aria-label="Período das ações">
          <button type="button" [attr.aria-pressed]="periodo() === 'semana'" (click)="periodo.set('semana')">Esta semana</button>
          <button type="button" [attr.aria-pressed]="periodo() === 'quatro'" (click)="periodo.set('quatro')">4 semanas</button>
          <button type="button" [attr.aria-pressed]="periodo() === 'mes'" (click)="periodo.set('mes')">Este mês</button>
        </div>
      }
      <select class="sel" aria-label="Situação das ações" [value]="situacao()" (change)="situacao.set($any($event.target).value)">
        <option value="">Todas as situações</option>
        @for (s of listaSituacoes; track s) { <option [value]="s">{{ situacoes[s].rotulo }}</option> }
      </select>
      @if (podeAcompanhar() && podePrever()) { <button type="button" class="btn btn--sm" (click)="abrirNova()" style="margin-left: auto;">Prever ação</button> }
    </div>
    @if (dados(); as d) {
      <p class="cell-sub" aria-label="Resumo das ações">{{ d.pendentes }} {{ d.pendentes === 1 ? 'pendente' : 'pendentes' }} no período · {{ d.vencidas }}
        {{ d.vencidas === 1 ? 'vencida' : 'vencidas' }} · {{ d.realizadasNoMes }} {{ d.realizadasNoMes === 1 ? 'realizada' : 'realizadas' }} no mês@if (d.prontasParaConfirmar) { · {{ d.prontasParaConfirmar }} {{ d.prontasParaConfirmar === 1 ? 'pronta' : 'prontas' }} para confirmar }</p>
      @if (d.lista.length) {
        <div class="tbl-wrap">
          <table aria-label="Ações programáticas">
            <thead><tr><th scope="col">Prevista</th><th scope="col">Ação</th><th scope="col">Quem</th><th scope="col">Responsável</th><th scope="col">Situação</th><th scope="col" class="num">Ações</th></tr></thead>
            <tbody>
              @for (a of d.lista; track a.uuid) {
                <tr>
                  <td class="mono">{{ data(a.dataPrevista) }}</td>
                  <td><span class="cell-main">{{ tipos[a.tipo] }}</span><span class="cell-sub">{{ a.campanhaNome ?? a.programaNome }}@if (a.descricao) { · {{ a.descricao }} }@if (a.agendamentoDataHora) { · agendada {{ dataHora(a.agendamentoDataHora) }} }</span></td>
                  <td>@if (a.campanhaId) { <span class="cell-main">{{ a.localNome ?? a.unidadeNome }}</span>@if (a.quantidadeAtendida != null) { <span class="cell-sub">{{ a.quantidadeAtendida }} atendido(s)</span> } } @else if (a.coletiva) { <span class="cell-main">{{ a.participantes.length }} participante(s)</span><span class="cell-sub">{{ a.local ?? a.unidadeNome }}</span> } @else { <span class="cell-main">{{ a.pacienteNome }}</span><span class="cell-sub">{{ a.unidadeNome }}</span> }</td>
                  <td>{{ a.responsavelNome ?? '—' }}</td>
                  <td>
                    <span [class]="'tag tag--' + situacoes[a.situacao].classe">{{ situacoes[a.situacao].rotulo }}</span>
                    @if (a.vencida) { <span class="tag tag--warn">Vencida</span> }
                    @if (a.prontaParaConfirmar) { <span class="tag tag--purple">Pronta para confirmar</span> }
                    @if (a.resultadoBusca) { <span class="cell-sub">{{ resultados[a.resultadoBusca] }}</span> }
                    @if (a.sugestao) { <span class="cell-sub" style="color: var(--warn);">{{ a.sugestao }}</span> }
                  </td>
                  <td class="num">
                    @if (podeAcompanhar() && a.situacao === 'PENDENTE') {
                      <div class="row-actions">
                        @if (!a.coletiva && a.tipo === 'CONSULTA_PROGRAMADA' && !a.agendamentoId) {
                          <button type="button" class="btn btn--ghost btn--sm" (click)="agendar(a)" [attr.aria-label]="'Agendar: ' + rotulo(a)">Agendar</button>
                        }
                        <button type="button" class="btn btn--ghost btn--sm" (click)="abrirRealizar(a)" [attr.aria-label]="'Confirmar: ' + rotulo(a)">Confirmar</button>
                        <button type="button" class="btn btn--ghost btn--sm" (click)="abrirSituacao(a, 'NAO_REALIZADA')" [attr.aria-label]="'Não realizada: ' + rotulo(a)">Não realizada</button>
                        <button type="button" class="btn btn--ghost btn--sm" (click)="abrirSituacao(a, 'CANCELADA')" [attr.aria-label]="'Cancelar: ' + rotulo(a)">Cancelar</button>
                      </div>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else {
        <p class="cell-sub">Nenhuma ação no período.</p>
      }
    } @else if (erroLista()) {
      <div class="box box--err" role="alert"><strong>Não foi possível carregar as ações.</strong></div>
    } @else {
      <p class="cell-sub">Carregando as ações…</p>
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g.tipo === 'nova' ? 'Prever ação programática' : g.tipo === 'realizar' ? 'Confirmar a realização' : (g.situacao === 'CANCELADA' ? 'Cancelar a ação' : 'Ação não realizada')"
        (fechar)="gaveta.set(null)">
        <form class="dw-form" [formGroup]="form" (ngSubmit)="salvar()" novalidate>
          <div class="dw-body">
            @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
            @switch (g.tipo) {
              @case ('nova') {
                @if (campanhaFixa(); as cf) {
                  <div class="dw-row">
                    <div class="dw-field">
                      <label for="f-acao-tipo-campanha">Ação</label>
                      <select id="f-acao-tipo-campanha" formControlName="tipo">
                        @for (t of tiposDaCampanha; track t) { <option [value]="t">{{ tipos[t] }}</option> }
                      </select>
                    </div>
                    <div class="dw-field">
                      <label for="f-acao-local-campanha">Local</label>
                      <select id="f-acao-local-campanha" formControlName="localCampanhaId">
                        <option value="">Escolha…</option>
                        @for (l of cf.locais; track l.uuid) { <option [value]="l.uuid">{{ l.nome }}</option> }
                      </select>
                    </div>
                  </div>
                } @else {
                <div class="dw-row">
                  @if (!programaFixo()) {
                    <div class="dw-field">
                      <label for="f-acao-programa">Programa</label>
                      <select id="f-acao-programa" formControlName="programaId" (change)="carregarAcompanhados($any($event.target).value)">
                        <option value="">Escolha…</option>
                        @for (p of programasAtivos(); track p.uuid) { <option [value]="p.uuid">{{ p.nome }}</option> }
                      </select>
                    </div>
                  }
                  <div class="dw-field">
                    <label for="f-acao-tipo">Ação</label>
                    <select id="f-acao-tipo" formControlName="tipo">
                      @for (t of listaTipos; track t) { <option [value]="t">{{ tipos[t] }}</option> }
                    </select>
                  </div>
                </div>
                <div class="dw-field">
                  <label for="f-acao-alcance">Para</label>
                  <select id="f-acao-alcance" formControlName="alcance">
                    <option value="individual">Um paciente acompanhado</option>
                    @if (!soIndividual()) { <option value="coletiva">Um grupo (atividade coletiva)</option> }
                  </select>
                </div>
                @if (form.controls.alcance.value === 'individual' || soIndividual()) {
                  <div class="dw-field">
                    <label for="f-acao-acompanhamento">Paciente</label>
                    <select id="f-acao-acompanhamento" formControlName="acompanhamentoId">
                      <option value="">Escolha…</option>
                      @for (a of acompanhados(); track a.uuid) { <option [value]="a.uuid">{{ a.pacienteNome }} · {{ a.unidadeNome }}</option> }
                    </select>
                  </div>
                } @else {
                  <div class="dw-row">
                    <div class="dw-field">
                      <label for="f-acao-unidade">Unidade</label>
                      <select id="f-acao-unidade" formControlName="unidadeId">
                        <option value="">Escolha…</option>
                        @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                      </select>
                    </div>
                    <div class="dw-field">
                      <label for="f-acao-local">Local<span class="opt">opcional</span></label>
                      <input id="f-acao-local" type="text" maxlength="200" formControlName="local" />
                    </div>
                  </div>
                  <fieldset class="dw-field">
                    <legend>Participantes</legend>
                    @if (acompanhados().length) {
                      <div class="radios" aria-label="Participantes" style="flex-direction: column; align-items: flex-start;">
                        @for (a of acompanhados(); track a.uuid) {
                          <label><input type="checkbox" [checked]="participantes().includes(a.pacienteId)" (change)="alternar(a.pacienteId, $any($event.target).checked)" /> {{ a.pacienteNome }}</label>
                        }
                      </div>
                    } @else { <p class="cell-sub">Escolha o programa para ver os acompanhados.</p> }
                  </fieldset>
                }
                }
                <div class="dw-row">
                  <div class="dw-field">
                    <label for="f-acao-data">Data prevista</label>
                    <input id="f-acao-data" type="date" formControlName="dataPrevista" />
                  </div>
                  <div class="dw-field">
                    <label for="f-acao-responsavel">Matrícula do responsável<span class="opt">opcional</span></label>
                    <input id="f-acao-responsavel" type="text" formControlName="responsavelMatricula" />
                  </div>
                </div>
                <div class="dw-field">
                  <label for="f-acao-descricao">Descrição<span class="opt">opcional</span></label>
                  <input id="f-acao-descricao" type="text" maxlength="1000" formControlName="descricao" />
                </div>
              }
              @case ('realizar') {
                <p class="cell-sub">{{ tipos[g.acao.tipo] }} · {{ g.acao.coletiva ? 'coletiva' : g.acao.pacienteNome }} · prevista para {{ data(g.acao.dataPrevista) }}</p>
                <div class="dw-field">
                  <label for="f-acao-realizada">Realizada em</label>
                  <input id="f-acao-realizada" type="date" formControlName="realizadaEm" />
                </div>
                @if (g.acao.atendimentoSugeridoId) {
                  <label class="switch" for="f-acao-atendimento">
                    <span><b style="font-weight: 500;">Ligar ao atendimento da marcação</b><span class="cell-sub">O atendimento agendado já foi concluído.</span></span>
                    <input type="checkbox" role="switch" id="f-acao-atendimento" formControlName="ligarAtendimento" />
                  </label>
                }
                @if (g.acao.tipo === 'BUSCA_ATIVA') {
                  <div class="dw-field">
                    <label for="f-acao-resultado">Resultado da busca</label>
                    <select id="f-acao-resultado" formControlName="resultadoBusca">
                      <option value="">Escolha…</option>
                      @for (r of listaResultados; track r) { <option [value]="r">{{ resultados[r] }}</option> }
                    </select>
                  </div>
                }
                @if (g.acao.coletiva) {
                  <div class="dw-field">
                    <label for="f-acao-quantidade">Pessoas atendidas<span class="opt">opcional</span></label>
                    <input id="f-acao-quantidade" type="number" min="0" formControlName="quantidadeAtendida" />
                  </div>
                }
                @if (g.acao.coletiva && !g.acao.campanhaId) {
                  <fieldset class="dw-field">
                    <legend>Quem participou</legend>
                    <div class="radios" aria-label="Quem participou" style="flex-direction: column; align-items: flex-start;">
                      @for (a of acompanhados(); track a.uuid) {
                        <label><input type="checkbox" [checked]="participantes().includes(a.pacienteId)" (change)="alternar(a.pacienteId, $any($event.target).checked)" /> {{ a.pacienteNome }}</label>
                      }
                    </div>
                  </fieldset>
                }
                <div class="dw-field">
                  <label for="f-acao-observacao">Observação<span class="opt">opcional</span></label>
                  <input id="f-acao-observacao" type="text" maxlength="1000" formControlName="observacao" />
                </div>
              }
              @case ('situacao') {
                <p class="cell-sub">{{ tipos[g.acao.tipo] }} · {{ g.acao.coletiva ? 'coletiva' : g.acao.pacienteNome }} · prevista para {{ data(g.acao.dataPrevista) }}</p>
                <div class="dw-field">
                  <label for="f-acao-motivo">Motivo</label>
                  <input id="f-acao-motivo" type="text" maxlength="500" formControlName="motivo" />
                </div>
              }
            }
          </div>
          <footer class="dw-foot">
            <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Voltar</button>
            <button type="submit" class="btn" [disabled]="salvando()">{{ salvando() ? 'Salvando…' : g.tipo === 'nova' ? 'Prever ação' : g.tipo === 'realizar' ? 'Confirmar' : 'Salvar' }}</button>
          </footer>
        </form>
      </app-drawer>
    }
  `,
})
export class AcoesProgramaticas {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProgramaService);
  private readonly router = inject(Router);

  readonly programas = input<ProgramaResumoDto[]>([]);
  readonly unidades = input<UnidadeSaudeResponseDto[]>([]);
  readonly unidadeId = input('');
  /** No detalhe do programa: só as ações dele. */
  readonly programaFixo = input<string | null>(null);
  /** No detalhe da campanha (ADR-0133): as ações dela, no período dela, nos locais dela. */
  readonly campanhaFixa = input<CampanhaDto | null>(null);
  /** Avisa quem mostra a campanha (o andamento da meta muda com a realização). */
  readonly alterada = output<void>();
  readonly podeAcompanhar = input(false);
  /** Muda para recarregar (ex.: uma busca ativa gerada na lista de inscritos). */
  readonly versao = input(0);

  protected readonly tipos = TIPOS_ACAO;
  protected readonly listaTipos = Object.keys(TIPOS_ACAO) as TipoAcaoProgramatica[];
  protected readonly tiposDaCampanha = this.listaTipos.filter((t) => !ACOES_SO_INDIVIDUAIS.includes(t));
  protected readonly situacoes = SITUACOES_ACAO;
  protected readonly listaSituacoes = Object.keys(SITUACOES_ACAO) as (keyof typeof SITUACOES_ACAO)[];
  protected readonly resultados = RESULTADOS_BUSCA;
  protected readonly listaResultados = Object.keys(RESULTADOS_BUSCA) as ResultadoBuscaAtiva[];

  protected readonly programaId = signal('');
  protected readonly periodo = signal<Periodo>('semana');
  protected readonly situacao = signal('');
  protected readonly dados = signal<AcoesProgramaticasDto | null>(null);
  protected readonly erroLista = signal(false);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly salvando = signal(false);
  protected readonly acompanhados = signal<AcompanhamentoDto[]>([]);
  protected readonly participantes = signal<string[]>([]);
  protected readonly form = this.fb.nonNullable.group({
    programaId: '', tipo: 'CONSULTA_PROGRAMADA', alcance: 'individual', acompanhamentoId: '', unidadeId: '', local: '', dataPrevista: '',
    responsavelMatricula: '', descricao: '', realizadaEm: '', ligarAtendimento: true, resultadoBusca: '', observacao: '', motivo: '',
    localCampanhaId: '', quantidadeAtendida: '',
  });
  private readonly tipoEscolhido = signal<TipoAcaoProgramatica>('CONSULTA_PROGRAMADA');

  protected readonly programasAtivos = computed(() => this.programas().filter((p) => p.situacao === 'ATIVO'));
  protected readonly soIndividual = computed(() => ACOES_SO_INDIVIDUAIS.includes(this.tipoEscolhido()));
  /** A campanha cancelada ou encerrada não recebe ação. */
  protected readonly podePrever = computed(() => {
    const c = this.campanhaFixa();
    return !c || c.situacao === 'PLANEJADA' || c.situacao === 'EM_ANDAMENTO';
  });

  constructor() {
    this.form.controls.tipo.valueChanges.subscribe((t) => this.tipoEscolhido.set(t as TipoAcaoProgramatica));
    effect(() => {
      this.versao();
      const filtro = this.filtro();
      untracked(() => this.carregar(filtro));
    });
  }

  private carregar(filtro: Parameters<ProgramaService['acoes']>[0]): void {
    this.erroLista.set(false);
    this.service.acoes(filtro).pipe(catchError(() => {
      this.erroLista.set(true);
      return of(null);
    })).subscribe((d) => this.dados.set(d));
  }

  private filtro(): Parameters<ProgramaService['acoes']>[0] {
    const c = this.campanhaFixa();
    if (c) return { campanhaId: c.uuid, de: c.inicio, ate: c.fim, situacao: this.situacao() || undefined };
    const [de, ate] = intervalo(this.periodo());
    return { programaId: this.programaFixo() || this.programaId() || undefined, unidadeId: this.unidadeId() || undefined, de, ate,
      situacao: this.situacao() || undefined };
  }

  private recarregar(): void {
    this.carregar(this.filtro());
  }

  protected data(iso?: string | null): string {
    if (!iso) return '—';
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string): string {
    return `${this.data(iso)} ${iso.slice(11, 16)}`;
  }

  protected rotulo(a: AcaoProgramaticaDto): string {
    return `${TIPOS_ACAO[a.tipo]} ${a.coletiva ? (a.local ?? 'coletiva') : a.pacienteNome}`;
  }

  protected carregarAcompanhados(programaId: string): void {
    this.acompanhados.set([]);
    this.participantes.set([]);
    if (!programaId) return;
    this.service.inscritos(programaId).pipe(catchError(() => of(null))).subscribe((r) => this.acompanhados.set(r?.lista ?? []));
  }

  protected alternar(pacienteId: string, marcado: boolean): void {
    this.participantes.update((l) => (marcado ? [...l, pacienteId] : l.filter((x) => x !== pacienteId)));
  }

  protected abrirNova(): void {
    const programa = this.programaFixo() || this.programaId();
    const c = this.campanhaFixa();
    const tipo = c ? (c.tipo === 'VACINACAO' ? 'VACINA' : 'MOBILIZACAO') : 'CONSULTA_PROGRAMADA';
    const data = c && hoje() < c.inicio ? c.inicio : hoje();
    this.form.reset({ programaId: programa, tipo, alcance: c ? 'coletiva' : 'individual', acompanhamentoId: '', unidadeId: this.unidadeId(), local: '',
      dataPrevista: data, responsavelMatricula: '', descricao: '', realizadaEm: '', ligarAtendimento: true, resultadoBusca: '', observacao: '', motivo: '',
      localCampanhaId: c?.locais.length === 1 ? c.locais[0].uuid : '', quantidadeAtendida: '' });
    this.tipoEscolhido.set(tipo as TipoAcaoProgramatica);
    this.erro.set(null);
    if (!c) this.carregarAcompanhados(programa);
    this.gaveta.set({ tipo: 'nova' });
  }

  protected abrirRealizar(a: AcaoProgramaticaDto): void {
    this.form.patchValue({ realizadaEm: hoje(), ligarAtendimento: true, resultadoBusca: '', observacao: '', quantidadeAtendida: '' });
    this.erro.set(null);
    if (a.coletiva && a.programaId && !a.campanhaId) {
      this.carregarAcompanhados(a.programaId);
      this.participantes.set(a.participantes.map((p) => p.pacienteId));
    }
    this.gaveta.set({ tipo: 'realizar', acao: a });
  }

  protected abrirSituacao(a: AcaoProgramaticaDto, situacao: 'NAO_REALIZADA' | 'CANCELADA'): void {
    this.form.patchValue({ motivo: '' });
    this.erro.set(null);
    this.gaveta.set({ tipo: 'situacao', acao: a, situacao });
  }

  /** Agendar a consulta programada: a Agenda abre com o paciente, o programa e a ação, e a marcação fica ligada a ela. */
  protected agendar(a: AcaoProgramaticaDto): void {
    this.router.navigate(['/assistencia/agenda'], { queryParams: { unidade: a.unidadeId, acao: a.uuid, acaoPaciente: a.pacienteId, acaoPrograma: a.programaId } });
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g) return;
    const v = this.form.getRawValue();
    if (g.tipo === 'nova' && this.campanhaFixa()) {
      if (!v.localCampanhaId || !v.dataPrevista) {
        this.erro.set('Escolha o local e a data.');
        return;
      }
      this.enviar(this.service.preverAcao({
        campanhaId: this.campanhaFixa()!.uuid, localCampanhaId: v.localCampanhaId, tipo: v.tipo as TipoAcaoProgramatica, dataPrevista: v.dataPrevista,
        responsavelMatricula: v.responsavelMatricula.trim() || null, descricao: v.descricao.trim() || null,
      }));
    } else if (g.tipo === 'nova') {
      const individual = v.alcance === 'individual' || this.soIndividual();
      if (!v.programaId || !v.dataPrevista || (individual && !v.acompanhamentoId) || (!individual && !v.unidadeId)) {
        this.erro.set(individual ? 'Escolha o programa, o paciente e a data.' : 'Escolha o programa, a unidade e a data.');
        return;
      }
      this.enviar(this.service.preverAcao({
        programaId: v.programaId, tipo: v.tipo as TipoAcaoProgramatica, acompanhamentoId: individual ? v.acompanhamentoId : null,
        unidadeId: individual ? null : v.unidadeId, local: individual ? null : v.local.trim() || null, participantesIds: individual ? [] : this.participantes(),
        dataPrevista: v.dataPrevista, responsavelMatricula: v.responsavelMatricula.trim() || null, descricao: v.descricao.trim() || null,
      }));
    } else if (g.tipo === 'realizar') {
      if (g.acao.tipo === 'BUSCA_ATIVA' && !v.resultadoBusca) {
        this.erro.set('Informe o resultado da busca.');
        return;
      }
      this.enviar(this.service.realizarAcao(g.acao.uuid, {
        realizadaEm: v.realizadaEm || null, atendimentoId: v.ligarAtendimento ? g.acao.atendimentoSugeridoId ?? null : null,
        resultadoBusca: (v.resultadoBusca || null) as ResultadoBuscaAtiva | null,
        participantesIds: g.acao.coletiva && !g.acao.campanhaId ? this.participantes() : null, observacao: v.observacao.trim() || null,
        quantidadeAtendida: g.acao.coletiva && String(v.quantidadeAtendida ?? '') !== '' ? Number(v.quantidadeAtendida) : null,
      }));
    } else {
      if (!v.motivo.trim()) {
        this.erro.set('Informe o motivo.');
        return;
      }
      this.enviar(this.service.mudarSituacaoAcao(g.acao.uuid, g.situacao, v.motivo.trim()));
    }
  }

  private enviar(req: ReturnType<ProgramaService['preverAcao']>): void {
    this.salvando.set(true);
    req.subscribe({
      next: () => {
        this.salvando.set(false);
        this.gaveta.set(null);
        this.recarregar();
        this.alterada.emit();
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar a ação.');
      },
    });
  }
}

function iso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function hoje(): string {
  return iso(new Date());
}

/** De segunda desta semana: 1 ou 4 semanas; ou o mês corrente. */
function intervalo(p: Periodo): [string, string] {
  const agora = new Date();
  if (p === 'mes') {
    return [iso(new Date(agora.getFullYear(), agora.getMonth(), 1)), iso(new Date(agora.getFullYear(), agora.getMonth() + 1, 0))];
  }
  const segunda = new Date(agora);
  segunda.setDate(agora.getDate() - ((agora.getDay() + 6) % 7));
  const fim = new Date(segunda);
  fim.setDate(segunda.getDate() + (p === 'semana' ? 6 : 27));
  return [iso(segunda), iso(fim)];
}
