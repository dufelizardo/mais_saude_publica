import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import {
  DeslocadoDto,
  EmergenciaDto,
  LOCAIS_DESLOCAMENTO,
  LocalDeslocamento,
  MARCADORES_VULNERABILIDADE,
  MarcadorVulnerabilidade,
  SITUACOES_DESLOCAMENTO,
  VulneraveisDto,
  VulneravelDto,
  RedeEmergenciaDto,
  RegulacaoEmergenciaDto,
} from '../../core/models/emergencia';
import { InstituicaoExternaDto } from '../../core/models/intersetorialidade';
import { ErrorResponseDto } from '../../core/models/profissional';
import { EmergenciaService } from '../../core/services/emergencia';
import { IntersetorialidadeService } from '../../core/services/intersetorialidade';
import { PacienteService } from '../../core/services/paciente';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { Drawer } from '../../shared/drawer/drawer';

type Gaveta =
  | { tipo: 'deslocar'; vulneravel: VulneravelDto | null }
  | { tipo: 'realocar'; d: DeslocadoDto }
  | { tipo: 'encerrar'; d: DeslocadoDto }
  | { tipo: 'transporte'; d: DeslocadoDto }
  | { tipo: 'encaminhar'; d: DeslocadoDto }
  | { tipo: 'regulacoes'; d: DeslocadoDto };

/**
 * As pessoas na emergência (ADR-0193): a ocupação dos destinos (sem nomes), os vulneráveis da área afetada (sob demanda, porque é dado
 * de paciente e a leitura é auditada) e os deslocados, com o registro, a realocação, o retorno, o transporte e o encaminhamento.
 */
@Component({
  selector: 'app-pessoas-emergencia',
  imports: [Drawer, RouterLink],
  template: `
    @let e = emergencia();
    <div class="co-secao">
      <div class="co-cab">
        <h3 class="mo-grupo" style="margin: 0;">Pessoas@if (e.deslocadosEmAberto) { <span class="tag tag--warn">{{ e.deslocadosEmAberto }} fora de casa</span> }</h3>
        @if (podeVer()) {
          <div class="row-actions">
            <button type="button" class="btn btn--ghost btn--sm" (click)="carregarVulneraveis()">{{ vulneraveis() ? 'Atualizar vulneráveis' : 'Ver vulneráveis' }}</button>
            <button type="button" class="btn btn--ghost btn--sm" (click)="carregarDeslocados()">{{ deslocados() ? 'Atualizar deslocados' : 'Ver deslocados' }}</button>
            <button type="button" class="btn btn--ghost btn--sm" (click)="carregarRede()">{{ rede() ? 'Atualizar compromissos' : 'Ver compromissos' }}</button>
            @if (ativa()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'deslocar', vulneravel: null })">Registrar deslocado</button> }
          </div>
        }
      </div>
      @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
      @if (e.abrigos?.length) {
        <ul class="eq-prof" aria-label="Ocupação dos destinos">
          @for (a of e.abrigos; track a.tipo + a.destino) {
            <li class="mo-row"><span><span class="cell-main">{{ a.destino }}</span><span class="cell-sub">{{ locais[a.tipo] }}</span></span><span class="mono">{{ a.pessoas }} pessoa(s)</span></li>
          }
        </ul>
      } @else { <p class="cell-sub">Nenhum paciente fora de casa registrado.</p> }

      @if (vulneraveis(); as v) {
        <h4 class="mo-grupo">Vulneráveis da área afetada</h4>
        <p class="cell-sub">{{ v.pessoasNaArea }} pessoa(s) cadastrada(s) nos domicílios da área · {{ v.vulneraveis }} com algum marcador. Programas sensíveis e saúde mental não aparecem.</p>
        <div class="filters" style="margin-bottom: var(--s-2);">
          <select class="sel" aria-label="Marcador" (change)="marcador.set($any($event.target).value); carregarVulneraveis()">
            <option value="">Todos os marcadores</option>
            @for (m of listaMarcadores; track m) { <option [value]="m" [selected]="marcador() === m">{{ marcadores[m] }}</option> }
          </select>
          <label class="cell-sub"><input type="checkbox" [checked]="todos()" (change)="todos.set($any($event.target).checked); carregarVulneraveis()" /> Incluir quem não tem marcador</label>
          <button type="button" class="btn btn--ghost btn--sm" (click)="baixarCsv()">Baixar CSV</button>
        </div>
        <div class="tbl-wrap"><table class="co-tabela" aria-label="Vulneráveis">
          <thead><tr><th scope="col">Pessoa</th><th scope="col">Endereço</th><th scope="col">Marcadores</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (p of v.lista; track p.pacienteId) {
              <tr>
                <td><span class="cell-main">{{ p.nome }}</span><span class="cell-sub">@if (p.idade != null) { {{ p.idade }} anos }@if (p.telefone) { · {{ p.telefone }} }</span></td>
                <td><span class="cell-sub">{{ p.endereco ?? '—' }}</span><span class="cell-sub">{{ p.microarea }}@if (p.acsNome) { · ACS {{ p.acsNome }} }</span></td>
                <td>@for (m of p.marcadores; track m) { <span class="tag tag--warn">{{ marcadores[m] }}</span> }@if (p.programas.length) { <span class="cell-sub">{{ p.programas.join(', ') }}</span> }</td>
                <td class="row-actions">@if (p.deslocado) { <span class="tag tag--muted">Em {{ p.deslocadoPara }}</span> } @else if (ativa()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'deslocar', vulneravel: p })" [attr.aria-label]="'Deslocar ' + p.nome">Deslocar</button> }</td>
              </tr>
            } @empty { <tr><td colspan="4"><span class="cell-sub">Ninguém com esse filtro na área afetada.</span></td></tr> }
          </tbody>
        </table></div>
      }

      @if (rede(); as r) {
        <h4 class="mo-grupo">Compromissos nas unidades afetadas</h4>
        <p class="cell-sub">Agendamentos e regulações dos próximos 30 dias nas unidades afetadas fora de operação. Quem redireciona é a Agenda ou a Regulação.</p>
        <ul class="eq-prof" aria-label="Compromissos nas unidades afetadas">
          @for (c of r.compromissos; track c.id) {
            <li class="mo-row"><span><span class="cell-main">{{ c.pacienteNome }} · {{ dataHora(c.dataHora) }}</span><span class="cell-sub">{{ c.tipo === 'AGENDAMENTO' ? 'Agendamento' : 'Regulação' }} · {{ c.descricao }} · {{ c.unidadeNome }}</span></span>
              <a class="link-btn" [routerLink]="[c.tipo === 'AGENDAMENTO' ? '/assistencia/agenda' : '/assistencia/regulacao']">Abrir {{ c.tipo === 'AGENDAMENTO' ? 'a Agenda' : 'a Regulação' }}</a></li>
          } @empty { <li><span class="cell-sub">Nenhum compromisso nas unidades afetadas fora de operação.</span></li> }
        </ul>
        <h4 class="mo-grupo">Regulações priorizadas</h4>
        <ul class="eq-prof" aria-label="Regulações priorizadas">
          @for (g of r.priorizadas; track g.id) {
            <li class="mo-row"><span><span class="cell-main">{{ g.pacienteNome }} · {{ g.procedimentoNome }}</span><span class="cell-sub">{{ g.prioridade }} · {{ g.status }} · {{ g.unidadeSolicitanteNome }}</span></span>
              @if (aberta()) { <button type="button" class="link-btn" (click)="retirar(g.id)" [attr.aria-label]="'Retirar a prioridade de ' + g.pacienteNome">Retirar</button> }</li>
          } @empty { <li><span class="cell-sub">Nenhuma regulação priorizada. Priorize a partir de um deslocado.</span></li> }
        </ul>
      }

      @if (deslocados(); as lista) {
        <h4 class="mo-grupo">Deslocados</h4>
        <ul class="eq-prof" aria-label="Pacientes deslocados">
          @for (d of lista; track d.uuid) {
            <li class="mo-row" style="display: block;">
              <div style="display: flex; justify-content: space-between; gap: var(--s-2); flex-wrap: wrap;">
                <span>
                  <span class="cell-main">{{ d.pacienteNome }} <span [class]="'tag tag--' + situacoes[d.situacao].classe">{{ situacoes[d.situacao].rotulo }}</span></span>
                  <span class="cell-sub">@if (d.origem) { {{ d.origem }} → }{{ d.destino }} ({{ locais[d.destinoTipo] }}) · desde {{ dataHora(d.saidaEm) }}@if (d.fimEm) { · até {{ dataHora(d.fimEm) }} }@if (d.motivoFim) { · {{ d.motivoFim }} }</span>
                  @if (d.necessidades) { <span class="cell-sub">Necessidades: {{ d.necessidades }}</span> }
                  @if (d.solicitacaoTransporteId) { <span class="cell-sub">Transporte ligado ({{ d.solicitacaoTransporteSituacao }})</span> }
                  @for (x of d.encaminhamentos; track x.uuid) { <span class="cell-sub">Encaminhado a {{ x.instituicaoNome }} · {{ x.situacao }}@if (x.retorno) { · {{ x.retorno }} }</span> }
                </span>
                @if (d.situacao === 'DESLOCADO' && aberta()) {
                  <span class="row-actions">
                    @if (ativa()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'realocar', d })" [attr.aria-label]="'Realocar ' + d.pacienteNome">Realocar</button> }
                    <button type="button" class="link-btn" (click)="executar(service.retornoDeslocado(e.uuid, d.uuid))" [attr.aria-label]="'Retorno de ' + d.pacienteNome">Retornou</button>
                    <button type="button" class="link-btn" (click)="abrir({ tipo: 'encerrar', d })" [attr.aria-label]="'Encerrar ' + d.pacienteNome">Encerrar</button>
                    <a class="link-btn" [routerLink]="['/administrativo/frota']" [queryParams]="{ pedir: 'OUTRO', pacienteId: d.pacienteId }" [attr.aria-label]="'Pedir transporte para ' + d.pacienteNome">Pedir transporte</a>
                    <button type="button" class="link-btn" (click)="abrir({ tipo: 'transporte', d })" [attr.aria-label]="'Ligar transporte de ' + d.pacienteNome">Ligar transporte</button>
                    <button type="button" class="link-btn" (click)="abrir({ tipo: 'encaminhar', d })" [attr.aria-label]="'Encaminhar ' + d.pacienteNome">Encaminhar</button>
                    @if (ativa()) { <button type="button" class="link-btn" (click)="abrirRegulacoes(d)" [attr.aria-label]="'Priorizar regulação de ' + d.pacienteNome">Priorizar regulação</button> }
                  </span>
                }
              </div>
            </li>
          } @empty { <li><span class="cell-sub">Nenhum deslocado registrado.</span></li> }
        </ul>
      }
    </div>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @switch (g.tipo) {
              @case ('encerrar') {
                <dl class="ro"><div><dt>Paciente</dt><dd>{{ g.d.pacienteNome }}</dd></div></dl>
                <div class="dw-field"><label for="f-pe-motivo">Motivo <span class="req" aria-hidden="true">*</span></label><input id="f-pe-motivo" type="text" maxlength="500" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)" placeholder="Ex.: mudou-se para outra cidade" /></div>
              }
              @case ('transporte') {
                <dl class="ro"><div><dt>Paciente</dt><dd>{{ g.d.pacienteNome }}</dd></div></dl>
                <div class="dw-field"><label for="f-pe-transporte">Identificador da solicitação de transporte <span class="req" aria-hidden="true">*</span></label><input id="f-pe-transporte" type="text" [value]="f()['transporte'] ?? ''" (input)="set('transporte', $event)" placeholder="Copie de Frota e transporte · Solicitações" /></div>
              }
              @case ('regulacoes') {
                <dl class="ro"><div><dt>Paciente</dt><dd>{{ g.d.pacienteNome }}</dd></div></dl>
                <p class="cell-sub">A emergência sobe a solicitação dentro da mesma cor na fila; quem decide é o regulador.</p>
                <ul class="eq-prof" aria-label="Regulações do paciente">
                  @for (x of regulacoes(); track x.id) {
                    <li class="mo-row"><span><span class="cell-main">{{ x.procedimentoNome }}</span><span class="cell-sub">{{ x.prioridade }} · {{ x.status }}</span></span>
                      @if (x.priorizada) { <span class="tag tag--ok">Priorizada</span> } @else { <button type="button" class="link-btn" (click)="priorizar(x.id)" [attr.aria-label]="'Priorizar ' + x.procedimentoNome">Priorizar</button> }</li>
                  } @empty { <li><span class="cell-sub">Nenhuma solicitação regulada em aberto.</span></li> }
                </ul>
              }
              @case ('encaminhar') {
                <dl class="ro"><div><dt>Paciente</dt><dd>{{ g.d.pacienteNome }}</dd></div></dl>
                <div class="dw-field"><label for="f-pe-instituicao">Instituição <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-pe-instituicao" (change)="f.set({ ...f(), instituicaoId: $any($event.target).value })">
                    <option value="">Escolha…</option>
                    @for (i of instituicoes(); track i.uuid) { <option [value]="i.uuid">{{ i.nome }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-pe-unidade-enc">Unidade que encaminha <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-pe-unidade-enc" (change)="f.set({ ...f(), unidadeId: $any($event.target).value })">
                    <option value="">Escolha…</option>
                    @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-pe-motivo-enc">Motivo <span class="req" aria-hidden="true">*</span></label><input id="f-pe-motivo-enc" type="text" maxlength="1000" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)" placeholder="Ex.: aluguel social, benefício eventual" /></div>
                <div class="dw-field"><label for="f-pe-retorno">Retorno esperado até <span class="req" aria-hidden="true">*</span></label><input id="f-pe-retorno" type="date" [value]="f()['retorno'] ?? ''" (input)="set('retorno', $event)" /></div>
              }
              @default {
                @if (g.tipo === 'deslocar') {
                  @if (g.vulneravel) {
                    <dl class="ro"><div><dt>Paciente</dt><dd>{{ g.vulneravel.nome }}</dd></div><div><dt>Endereço</dt><dd>{{ g.vulneravel.endereco ?? '—' }}</dd></div></dl>
                  } @else {
                    <div class="dw-field"><label for="f-pe-cpf">CPF do paciente <span class="req" aria-hidden="true">*</span></label><input id="f-pe-cpf" type="text" inputmode="numeric" [value]="f()['cpf'] ?? ''" (input)="set('cpf', $event)" /></div>
                  }
                } @else {
                  <dl class="ro"><div><dt>Paciente</dt><dd>{{ g.d.pacienteNome }}</dd></div><div><dt>Está em</dt><dd>{{ g.d.destino }}</dd></div></dl>
                }
                <div class="dw-field"><label for="f-pe-destino">Destino <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-pe-destino" (change)="f.set({ ...f(), destinoTipo: $any($event.target).value })">
                    <option value="">Escolha…</option>
                    @for (l of listaDestinos; track l) { <option [value]="l" [selected]="f()['destinoTipo'] === l">{{ locais[l] }}</option> }
                  </select></div>
                @if (f()['destinoTipo'] === 'UNIDADE_SAUDE') {
                  <div class="dw-field"><label for="f-pe-unidade">Unidade de saúde <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-pe-unidade" (change)="f.set({ ...f(), destinoUnidadeId: $any($event.target).value })">
                      <option value="">Escolha…</option>
                      @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                    </select></div>
                }
                @if (f()['destinoTipo'] === 'ABRIGO') {
                  <div class="dw-field"><label for="f-pe-abrigo">Abrigo da rede intersetorial<span class="opt">ou o nome do local abaixo</span></label>
                    <select id="f-pe-abrigo" (change)="f.set({ ...f(), destinoInstituicaoId: $any($event.target).value })">
                      <option value="">Nenhum</option>
                      @for (i of abrigos(); track i.uuid) { <option [value]="i.uuid">{{ i.nome }}</option> }
                    </select></div>
                }
                @if (f()['destinoTipo'] && f()['destinoTipo'] !== 'UNIDADE_SAUDE') {
                  <div class="dw-field"><label for="f-pe-descricao">{{ f()['destinoTipo'] === 'CASA_PARENTE' ? 'Endereço da casa' : f()['destinoTipo'] === 'ABRIGO' ? 'Nome do local' : 'Destino' }}</label><input id="f-pe-descricao" type="text" maxlength="500" [value]="f()['destinoDescricao'] ?? ''" (input)="set('destinoDescricao', $event)" placeholder="Ex.: Ginásio Municipal" /></div>
                }
                <div class="dw-field"><label for="f-pe-necessidades">Necessidades<span class="opt">opcional</span></label><input id="f-pe-necessidades" type="text" maxlength="1000" [value]="f()['necessidades'] ?? ''" (input)="set('necessidades', $event)" placeholder="Ex.: oxigênio, medicação contínua, fraldas" /></div>
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
export class PessoasEmergencia {
  protected readonly service = inject(EmergenciaService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly intersetorialService = inject(IntersetorialidadeService);
  private readonly pacienteService = inject(PacienteService);

  readonly emergencia = input.required<EmergenciaDto>();
  readonly podeVer = input(false);

  protected readonly marcadores = MARCADORES_VULNERABILIDADE;
  protected readonly listaMarcadores = Object.keys(MARCADORES_VULNERABILIDADE) as MarcadorVulnerabilidade[];
  protected readonly locais = LOCAIS_DESLOCAMENTO;
  protected readonly listaDestinos = (Object.keys(LOCAIS_DESLOCAMENTO) as LocalDeslocamento[]).filter((l) => l !== 'DOMICILIO');
  protected readonly situacoes = SITUACOES_DESLOCAMENTO;

  protected readonly ativa = computed(() => this.emergencia().situacao === 'ATIVA');
  protected readonly aberta = computed(() => this.emergencia().situacao === 'ATIVA' || this.emergencia().situacao === 'EM_DESMOBILIZACAO');
  protected readonly vulneraveis = signal<VulneraveisDto | null>(null);
  protected readonly deslocados = signal<DeslocadoDto[] | null>(null);
  protected readonly rede = signal<RedeEmergenciaDto | null>(null);
  protected readonly regulacoes = signal<RegulacaoEmergenciaDto[]>([]);
  protected readonly marcador = signal<MarcadorVulnerabilidade | ''>('');
  protected readonly todos = signal(false);
  protected readonly unidades = signal<{ uuid: string; nome: string }[]>([]);
  protected readonly instituicoes = signal<InstituicaoExternaDto[]>([]);
  protected readonly abrigos = computed(() => this.instituicoes().filter((i) => i.tipo === 'ACOLHIMENTO_INSTITUCIONAL'));
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});

  protected carregarVulneraveis(): void {
    this.erro.set(null);
    this.service.vulneraveis(this.emergencia().uuid, { todos: this.todos(), marcador: this.marcador() }).subscribe({
      next: (v) => this.vulneraveis.set(v),
      error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)),
    });
  }

  protected carregarDeslocados(): void {
    this.erro.set(null);
    this.service.deslocados(this.emergencia().uuid).subscribe({ next: (l) => this.deslocados.set(l), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }

  protected carregarRede(): void {
    this.erro.set(null);
    this.service.rede(this.emergencia().uuid).subscribe({ next: (r) => this.rede.set(r), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }

  protected abrirRegulacoes(d: DeslocadoDto): void {
    this.regulacoes.set([]);
    this.abrir({ tipo: 'regulacoes', d });
    this.service.regulacoesDoPaciente(this.emergencia().uuid, d.pacienteId).subscribe({
      next: (l) => this.regulacoes.set(l),
      error: (e: HttpErrorResponse) => this.erroGaveta.set(mensagem(e)),
    });
  }

  protected priorizar(solicitacaoId: string): void {
    const g = this.gaveta();
    this.erroGaveta.set(null);
    this.service.priorizarRegulacao(this.emergencia().uuid, solicitacaoId).subscribe({
      next: (r) => {
        this.rede.set(r);
        if (g?.tipo === 'regulacoes') this.service.regulacoesDoPaciente(this.emergencia().uuid, g.d.pacienteId).subscribe((l) => this.regulacoes.set(l));
      },
      error: (e: HttpErrorResponse) => this.erroGaveta.set(mensagem(e)),
    });
  }

  protected retirar(solicitacaoId: string): void {
    this.erro.set(null);
    this.service.retirarPrioridade(this.emergencia().uuid, solicitacaoId).subscribe({ next: (r) => this.rede.set(r), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }

  protected baixarCsv(): void {
    this.service.vulneraveisCsv(this.emergencia().uuid, { todos: this.todos(), marcador: this.marcador() }).subscribe((b) => {
      const url = URL.createObjectURL(b);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'vulneraveis.csv';
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  protected titulo(g: Gaveta): string {
    switch (g.tipo) {
      case 'deslocar': return 'Registrar deslocado';
      case 'realocar': return 'Realocar ' + g.d.pacienteNome;
      case 'encerrar': return 'Encerrar o deslocamento';
      case 'transporte': return 'Ligar transporte';
      case 'regulacoes': return 'Priorizar regulação';
      default: return 'Encaminhar à rede intersetorial';
    }
  }

  protected abrir(g: Gaveta): void {
    this.erroGaveta.set(null);
    this.f.set({});
    if (!this.unidades().length) {
      this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) =>
        this.unidades.set(l.map((u) => ({ uuid: u.uuid, nome: u.nome })).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))));
    }
    if (!this.instituicoes().length) this.intersetorialService.listar({ ativas: true }).pipe(catchError(() => of([]))).subscribe((l) => this.instituicoes.set(l));
    this.gaveta.set(g);
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected executar(req: Observable<DeslocadoDto[]>): void {
    this.erro.set(null);
    req.subscribe({ next: (l) => this.deslocados.set(l), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }

  protected salvar(g: Gaveta): void {
    if (this.enviando()) return;
    const id = this.emergencia().uuid;
    const f = this.f();
    const t = (k: string) => (f[k] ?? '').trim() || null;
    const destino = () => ({
      destinoTipo: (t('destinoTipo') ?? 'OUTRO') as LocalDeslocamento, destinoUnidadeId: t('destinoUnidadeId'), destinoInstituicaoId: t('destinoInstituicaoId'),
      destinoDescricao: t('destinoDescricao'), necessidades: t('necessidades'),
    });
    let falta: string | null = null;
    let req: Observable<DeslocadoDto[]> | null = null;
    switch (g.tipo) {
      case 'regulacoes':
        this.gaveta.set(null);
        return;
      case 'deslocar':
      case 'realocar':
        if (!t('destinoTipo')) falta = 'Escolha o destino.';
        else if (t('destinoTipo') === 'UNIDADE_SAUDE' && !t('destinoUnidadeId')) falta = 'Escolha a unidade de saúde.';
        else if (t('destinoTipo') === 'ABRIGO' && !t('destinoInstituicaoId') && !t('destinoDescricao')) falta = 'Escolha o abrigo ou informe o nome do local.';
        else if ((t('destinoTipo') === 'CASA_PARENTE' || t('destinoTipo') === 'OUTRO') && !t('destinoDescricao')) falta = 'Descreva o destino.';
        else if (g.tipo === 'deslocar' && !g.vulneravel && !t('cpf')) falta = 'Informe o CPF do paciente.';
        if (!falta && g.tipo === 'realocar') req = this.service.realocar(id, g.d.uuid, destino());
        break;
      case 'encerrar':
        if (!t('motivo')) falta = 'Informe o motivo.';
        else req = this.service.encerrarDeslocado(id, g.d.uuid, t('motivo')!);
        break;
      case 'transporte':
        if (!t('transporte')) falta = 'Informe a solicitação de transporte.';
        else req = this.service.ligarTransporteDeslocado(id, g.d.uuid, t('transporte')!);
        break;
      case 'encaminhar':
        if (!t('instituicaoId') || !t('unidadeId') || !t('motivo') || !t('retorno')) falta = 'Informe a instituição, a unidade, o motivo e o retorno esperado.';
        else req = this.service.encaminharDeslocado(id, g.d.uuid, { instituicaoId: t('instituicaoId')!, unidadeId: t('unidadeId')!, motivo: t('motivo')!,
          retornoEsperadoEm: t('retorno')! });
        break;
    }
    if (falta) {
      this.erroGaveta.set(falta);
      return;
    }
    this.enviando.set(true);
    if (g.tipo === 'deslocar') {
      const comPaciente = (pacienteId: string) => this.enviarReq(this.service.registrarDeslocado(id, { pacienteId, ...destino() }));
      if (g.vulneravel) comPaciente(g.vulneravel.pacienteId);
      else this.pacienteService.buscarPorCpf(t('cpf')!.replace(/\D/g, '')).pipe(catchError(() => of([]))).subscribe((l) => {
        if (!l.length) {
          this.enviando.set(false);
          this.erroGaveta.set('Nenhum paciente com esse CPF.');
        } else comPaciente(l[0].uuid);
      });
      return;
    }
    this.enviarReq(req!);
  }

  private enviarReq(req: Observable<DeslocadoDto[]>): void {
    req.subscribe({
      next: (l) => {
        this.enviando.set(false);
        this.gaveta.set(null);
        this.deslocados.set(l);
        if (this.vulneraveis()) this.carregarVulneraveis();
      },
      error: (e: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(e));
      },
    });
  }

  protected dataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui os dados de paciente da emergência.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
