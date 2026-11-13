import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../core/models/auth';
import { PacienteResponseDto } from '../../core/models/paciente';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  DANOS,
  FATORES,
  FatorContribuinte,
  GrauDano,
  IncidenteDto,
  NATUREZAS,
  NaturezaIncidente,
  PainelSegurancaDto,
  ProtocoloIncidenteDto,
  SITUACOES_INCIDENTE,
  SituacaoIncidente,
  TIPOS_INCIDENTE,
  TipoIncidente,
} from '../../core/models/qualidade';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { AuthService } from '../../core/services/auth';
import { PacienteService } from '../../core/services/paciente';
import { QualidadeService } from '../../core/services/qualidade';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { Drawer } from '../../shared/drawer/drawer';

type Aba = 'notificar' | 'incidentes' | 'painel';

/**
 * Segurança do paciente, fatia Q1 da Qualidade (ADR-0203): qualquer profissional notifica o incidente, anônimo ou identificado; o Núcleo
 * de Segurança do Paciente classifica pela ICPS, analisa (com a causa raiz no dano grave, no óbito e no never event), descarta o que não é
 * incidente e registra o Notivisa no prazo; o painel mostra só totais.
 */
@Component({
  selector: 'app-seguranca-do-paciente',
  imports: [Drawer, RouterLink],
  template: `
    <header class="page-head">
      <div>
        <h1>Segurança do paciente</h1>
        <p>Notificação de incidentes e análise pelo Núcleo de Segurança do Paciente (RDC 36/2013)</p>
      </div>
    </header>

    <div class="tabs" role="tablist" aria-label="Seções da segurança do paciente">
      @if (pode('QUALIDADE.NOTIFICAR')) { <button type="button" role="tab" id="tab-notificar" [attr.aria-selected]="aba() === 'notificar'" (click)="aba.set('notificar')">Notificar</button> }
      @if (pode('QUALIDADE.ANALISAR')) { <button type="button" role="tab" id="tab-incidentes" [attr.aria-selected]="aba() === 'incidentes'" (click)="irPara('incidentes')">Incidentes @if (abertos()) { <span class="cnt">{{ abertos() }}</span> }</button> }
      @if (podeAlguma('QUALIDADE.ANALISAR', 'INDICADOR.CONSULTAR')) { <button type="button" role="tab" id="tab-painel" [attr.aria-selected]="aba() === 'painel'" (click)="irPara('painel')">Painel</button> }
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    @switch (aba()) {
      @case ('notificar') {
        <form class="card sp-form" (submit)="$event.preventDefault(); notificar()" novalidate aria-label="Notificar incidente">
          <p class="cell-sub">A notificação serve para aprender e prevenir, não para punir. Você pode notificar sem se identificar.</p>
          @if (protocolo(); as p) { <div class="box box--ok" role="status"><strong>Notificação registrada: protocolo {{ p.numero }}{{ p.anonima ? ' (anônima)' : '' }}.</strong></div> }
          @if (erroForm()) { <div class="box box--err" role="alert"><strong>{{ erroForm() }}</strong></div> }
          <div class="dw-row">
            <div class="dw-field"><label for="f-sp-unidade">Unidade <span class="req" aria-hidden="true">*</span></label>
              <select id="f-sp-unidade" (change)="set('unidadeId', $event)">
                <option value="" [selected]="!f()['unidadeId']">Escolha…</option>
                @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['unidadeId'] === u.uuid">{{ u.nome }}</option> }
              </select></div>
            <div class="dw-field"><label for="f-sp-quando">Quando ocorreu <span class="req" aria-hidden="true">*</span></label><input id="f-sp-quando" type="datetime-local" [value]="f()['quando'] ?? ''" (input)="set('quando', $event)" /></div>
            <div class="dw-field"><label for="f-sp-local">Local <span class="req" aria-hidden="true">*</span></label><input id="f-sp-local" type="text" maxlength="200" placeholder="Ex.: enfermaria 2, leito 4" [value]="f()['local'] ?? ''" (input)="set('local', $event)" /></div>
          </div>
          <div class="dw-row">
            <div class="dw-field"><label for="f-sp-tipo">Tipo <span class="req" aria-hidden="true">*</span></label>
              <select id="f-sp-tipo" (change)="set('tipo', $event)">
                <option value="" [selected]="!f()['tipo']">Escolha…</option>
                @for (t of listaTipos; track t) { <option [value]="t" [selected]="f()['tipo'] === t">{{ tipos[t] }}</option> }
              </select></div>
            <div class="dw-field"><label for="f-sp-dano">Dano percebido<span class="opt">opcional</span></label>
              <select id="f-sp-dano" (change)="set('dano', $event)">
                <option value="">Não sei</option>
                @for (d of listaDanos; track d) { <option [value]="d">{{ danos[d] }}</option> }
              </select></div>
            <div class="dw-field"><label for="f-sp-paciente">Paciente<span class="opt">opcional</span></label>
              <input id="f-sp-paciente" type="search" placeholder="Nome ou CPF" [value]="busca()" (input)="busca.set($any($event.target).value); f.set({ ...f(), pacienteId: '' })" />
              @if (encontrados().length && !f()['pacienteId']) {
                <ul class="sp-sugestoes" role="listbox" aria-label="Pacientes encontrados">
                  @for (p of encontrados(); track p.uuid) { <li role="option" [attr.aria-selected]="false" (click)="escolherPaciente(p)">{{ p.nome }}</li> }
                </ul>
              }
            </div>
          </div>
          <div class="dw-field"><label for="f-sp-desc">O que aconteceu <span class="req" aria-hidden="true">*</span></label><textarea id="f-sp-desc" rows="4" maxlength="4000" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)"></textarea></div>
          <div class="dw-field"><label for="f-sp-acao">O que foi feito na hora<span class="opt">opcional</span></label><textarea id="f-sp-acao" rows="2" maxlength="2000" [value]="f()['acao'] ?? ''" (input)="set('acao', $event)"></textarea></div>
          <label class="chk"><input id="f-sp-anonima" type="checkbox" [checked]="f()['anonima'] === 'sim'" (change)="f.set({ ...f(), anonima: $any($event.target).checked ? 'sim' : 'nao' })" /> Notificar sem me identificar</label>
          <div><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Enviando…' : 'Notificar incidente' }}</button></div>
        </form>
      }
      @case ('incidentes') {
        <div class="sp-cab">
          <div class="dw-field" style="margin: 0;"><label for="f-sp-sit">Situação</label>
            <select id="f-sp-sit" (change)="situacao.set($any($event.target).value); carregar()">
              <option value="">Todas</option>
              @for (s of listaSituacoes; track s) { <option [value]="s" [selected]="situacao() === s">{{ situacoes[s] }}</option> }
            </select></div>
        </div>
        <div class="tbl-wrap card">
          <table class="sp-tabela" aria-label="Incidentes de segurança">
            <thead><tr><th scope="col">Protocolo</th><th scope="col">Ocorrência</th><th scope="col">Tipo</th><th scope="col">Classificação</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (i of incidentes(); track i.uuid) {
                <tr>
                  <td><span class="cell-main mono">{{ i.numero }}</span><span class="cell-sub">{{ i.unidadeNome }}</span></td>
                  <td>{{ dataHora(i.ocorridoEm) }}<span class="cell-sub">{{ i.local }}</span></td>
                  <td>{{ tipos[i.tipo] }}</td>
                  <td>@if (i.natureza) { {{ naturezas[i.natureza] }}<span class="cell-sub">dano {{ danos[i.dano!].toLowerCase() }}@if (i.neverEvent) { · never event }</span> } @else { <span class="cell-sub">a classificar</span> }</td>
                  <td><span [class]="'tag tag--' + tagSituacao(i)">{{ situacoes[i.situacao] }}</span>@if (i.notivisaObrigatoria && !i.numeroNotivisa) { <span [class]="'cell-sub' + (i.notivisaVencida ? ' sp-vencido' : '')">Notivisa até {{ dataHora(i.prazoNotivisa!) }}</span> }</td>
                  <td class="row-actions"><button type="button" class="link-btn" (click)="abrir(i.uuid)" [attr.aria-label]="'Abrir incidente ' + i.numero">Abrir</button></td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhum incidente.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('painel') {
        @if (painel(); as p) {
          <section class="stats" aria-label="Indicadores da segurança do paciente">
            <div class="stat"><div><div class="stat__lbl">Notificações</div><div class="stat__val mono">{{ p.total }}</div><div class="stat__sub">{{ p.aguardandoAnalise }} aguardando análise</div></div></div>
            <div class="stat"><div><div class="stat__lbl">Eventos adversos</div><div class="stat__val mono">{{ p.eventosAdversos }}</div><div class="stat__sub">com dano ao paciente</div></div></div>
            <div class="stat"><div><div class="stat__lbl">Notivisa pendente</div><div class="stat__val mono">{{ p.notivisaPendentes }}</div><div class="stat__sub">{{ p.notivisaVencidas }} com o prazo vencido</div></div></div>
          </section>
          <div class="sp-grade">
            <div class="card"><h3 class="mo-grupo">Por tipo</h3><ul class="sp-lista" aria-label="Incidentes por tipo">@for (x of p.porTipo; track x.codigo) { <li>{{ tipos[x.codigo] }} <span class="mono">{{ x.quantidade }}</span></li> } @empty { <li class="cell-sub">Nenhum.</li> }</ul></div>
            <div class="card"><h3 class="mo-grupo">Por natureza</h3><ul class="sp-lista" aria-label="Incidentes por natureza">@for (x of p.porNatureza; track x.codigo) { <li>{{ naturezas[x.codigo] }} <span class="mono">{{ x.quantidade }}</span></li> } @empty { <li class="cell-sub">Nenhum classificado.</li> }</ul></div>
            <div class="card"><h3 class="mo-grupo">Por dano</h3><ul class="sp-lista" aria-label="Incidentes por dano">@for (x of p.porDano; track x.codigo) { <li>{{ danos[x.codigo] }} <span class="mono">{{ x.quantidade }}</span></li> } @empty { <li class="cell-sub">Nenhum classificado.</li> }</ul></div>
          </div>
        }
      }
    }

    @if (detalhe(); as i) {
      <app-drawer [titulo]="'Incidente ' + i.numero" (fechar)="detalhe.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Ocorrência</dt><dd>{{ dataHora(i.ocorridoEm) }} · {{ i.local }} · {{ i.unidadeNome }}</dd></div>
            <div><dt>Tipo</dt><dd>{{ tipos[i.tipo] }}@if (i.danoPercebido) { · dano percebido {{ danos[i.danoPercebido].toLowerCase() }} }</dd></div>
            @if (i.pacienteNome) { <div><dt>Paciente</dt><dd>{{ i.pacienteNome }}</dd></div> }
            <div><dt>O que aconteceu</dt><dd>{{ i.descricao }}</dd></div>
            @if (i.acaoImediata) { <div><dt>Feito na hora</dt><dd>{{ i.acaoImediata }}</dd></div> }
            <div><dt>Notificado por</dt><dd>{{ i.anonima ? 'Anônimo' : (i.notificadorNome ?? '—') }}</dd></div>
            <div><dt>Situação</dt><dd>{{ situacoes[i.situacao] }}@if (i.motivoDescarte) { · {{ i.motivoDescarte }} }</dd></div>
            @if (i.natureza) { <div><dt>Classificação</dt><dd>{{ naturezas[i.natureza] }} · dano {{ danos[i.dano!].toLowerCase() }}@if (i.neverEvent) { · never event }</dd></div> }
            @if (i.fatores.length) { <div><dt>Fatores</dt><dd>{{ rotulosFatores(i) }}</dd></div> }
            @if (i.causaRaiz) { <div><dt>Causa raiz</dt><dd>{{ i.causaRaiz }}</dd></div> }
            @if (i.licoes) { <div><dt>Lições</dt><dd>{{ i.licoes }}</dd></div> }
            @if (i.situacao === 'CONCLUIDA' && pode('QUALIDADE.GERIR')) { <div><dt>Não conformidade</dt><dd><a class="link-btn" [routerLink]="['/qualidade/nao-conformidades']" [queryParams]="{ origem: 'INCIDENTE', incidente: i.uuid, unidade: i.unidadeId, referencia: 'Incidente ' + i.numero }">Abrir não conformidade</a></dd></div> }
            @if (i.notivisaObrigatoria) { <div><dt>Notivisa</dt><dd>@if (i.numeroNotivisa) { {{ i.numeroNotivisa }} em {{ dataHora(i.notivisaEm!) }} } @else { até {{ dataHora(i.prazoNotivisa!) }}@if (i.notivisaVencida) { <span class="tag tag--alert">Vencido</span> } }</dd></div> }
          </dl>

          @if (i.situacao === 'RECEBIDA' || i.situacao === 'EM_ANALISE') {
            <h3 class="mo-grupo sp-titulo">Classificação (OMS)</h3>
            <div class="dw-row">
              <div class="dw-field"><label for="f-sp-nat">Natureza</label>
                <select id="f-sp-nat" (change)="set('natureza', $event)">
                  @for (n of listaNaturezas; track n) { <option [value]="n" [selected]="f()['natureza'] === n">{{ naturezas[n] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-sp-cdano">Dano</label>
                <select id="f-sp-cdano" (change)="set('cdano', $event)">
                  @for (d of listaDanos; track d) { <option [value]="d" [selected]="f()['cdano'] === d">{{ danos[d] }}</option> }
                </select></div>
            </div>
            <label class="chk"><input id="f-sp-never" type="checkbox" [checked]="f()['never'] === 'sim'" (change)="f.set({ ...f(), never: $any($event.target).checked ? 'sim' : 'nao' })" /> Never event (não deveria ocorrer nunca)</label>
            <div class="row-actions"><button type="button" class="btn btn--sm" (click)="classificar(i)" [disabled]="enviando()">Classificar</button></div>
          }
          @if (i.situacao === 'EM_ANALISE') {
            <h3 class="mo-grupo sp-titulo">Análise</h3>
            <fieldset class="sp-grupo"><legend>Fatores contribuintes</legend>
              @for (x of listaFatores; track x) { <label class="chk"><input type="checkbox" [checked]="fatores().has(x)" (change)="alternarFator(x)" /> {{ fatoresRot[x] }}</label> }
            </fieldset>
            <div class="dw-field"><label for="f-sp-causa">Causa raiz @if (i.exigeCausaRaiz) { <span class="req" aria-hidden="true">*</span> } @else { <span class="opt">opcional</span> }</label><textarea id="f-sp-causa" rows="3" maxlength="4000" [value]="f()['causa'] ?? ''" (input)="set('causa', $event)"></textarea></div>
            <div class="dw-field"><label for="f-sp-licoes">Lições aprendidas<span class="opt">opcional</span></label><textarea id="f-sp-licoes" rows="2" maxlength="2000" [value]="f()['licoes'] ?? ''" (input)="set('licoes', $event)"></textarea></div>
            <div class="row-actions"><button type="button" class="btn btn--sm" (click)="concluir(i)" [disabled]="enviando()">Concluir análise</button></div>
          }
          @if (i.notivisaObrigatoria && !i.numeroNotivisa) {
            <h3 class="mo-grupo sp-titulo">Notificação ao Notivisa</h3>
            <div class="dw-row">
              <div class="dw-field"><label for="f-sp-notivisa">Número no Notivisa</label><input id="f-sp-notivisa" type="text" maxlength="40" [value]="f()['notivisa'] ?? ''" (input)="set('notivisa', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="notivisa(i)" [disabled]="enviando()">Registrar</button>
            </div>
          }
          @if (i.situacao === 'RECEBIDA' || i.situacao === 'EM_ANALISE') {
            <h3 class="mo-grupo sp-titulo">Descartar</h3>
            <div class="dw-row">
              <div class="dw-field"><label for="f-sp-descarte">Motivo (duplicada, fora do escopo)</label><input id="f-sp-descarte" type="text" maxlength="1000" [value]="f()['descarte'] ?? ''" (input)="set('descarte', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="descartar(i)" [disabled]="enviando()">Descartar</button>
            </div>
          }
        </div>
      </app-drawer>
    }
  `,
  styles: `
    .sp-form { display: grid; gap: var(--s-2); padding: var(--s-4); max-width: 960px; }
    .sp-cab { display: flex; gap: var(--s-3); align-items: flex-end; margin-bottom: var(--s-2); }
    .sp-tabela { min-width: 0; width: 100%; }
    .sp-titulo { margin-top: var(--s-4); }
    .sp-grupo { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); display: grid; gap: var(--s-1); }
    .sp-grade { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: var(--s-3); margin-top: var(--s-3); }
    .sp-grade .card { padding: var(--s-3); }
    .sp-lista { margin: 0; padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .sp-lista li { display: flex; justify-content: space-between; }
    .sp-sugestoes { list-style: none; margin: 0; padding: 0; border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); }
    .sp-sugestoes li { padding: var(--s-1) var(--s-2); cursor: pointer; }
    .sp-sugestoes li:hover { background: var(--ink-50); }
    .sp-vencido { color: var(--danger-600, #b42318); }
    .chk { display: flex; gap: var(--s-2); align-items: center; }
  `,
})
export class SegurancaDoPaciente {
  private readonly service = inject(QualidadeService);
  private readonly authService = inject(AuthService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly pacienteService = inject(PacienteService);

  protected readonly tipos = TIPOS_INCIDENTE;
  protected readonly listaTipos = Object.keys(TIPOS_INCIDENTE) as TipoIncidente[];
  protected readonly naturezas = NATUREZAS;
  protected readonly listaNaturezas = Object.keys(NATUREZAS) as NaturezaIncidente[];
  protected readonly danos = DANOS;
  protected readonly listaDanos = Object.keys(DANOS) as GrauDano[];
  protected readonly situacoes = SITUACOES_INCIDENTE;
  protected readonly listaSituacoes = Object.keys(SITUACOES_INCIDENTE) as SituacaoIncidente[];
  protected readonly fatoresRot = FATORES;
  protected readonly listaFatores = Object.keys(FATORES) as FatorContribuinte[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('notificar');
  protected readonly erro = signal<string | null>(null);
  protected readonly erroForm = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly protocolo = signal<ProtocoloIncidenteDto | null>(null);
  protected readonly situacao = signal<string>('');
  protected readonly incidentes = signal<IncidenteDto[]>([]);
  protected readonly painel = signal<PainelSegurancaDto | null>(null);
  protected readonly detalhe = signal<IncidenteDto | null>(null);
  protected readonly fatores = signal<Set<string>>(new Set());

  protected readonly abertos = computed(() => this.incidentes().filter((i) => i.situacao === 'RECEBIDA' || i.situacao === 'EM_ANALISE').length);
  protected readonly encontrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    if (q.length < 2) return [];
    const digitos = q.replace(/\D/g, '');
    return this.pacientes().filter((p) => p.ativo && (p.nome.toLowerCase().includes(q) || (digitos.length >= 3 && (p.cpf ?? '').includes(digitos)))).slice(0, 8);
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      if (!this.pode('QUALIDADE.NOTIFICAR')) this.aba.set(this.pode('QUALIDADE.ANALISAR') ? 'incidentes' : 'painel');
      this.carregar();
    });
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidades.set(l));
    this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.pacientes.set(l));
  }

  protected pode(permissao: string): boolean {
    const a = this.acesso();
    return !a.restrito || a.permissoes.has(permissao);
  }

  protected podeAlguma(...permissoes: string[]): boolean {
    return permissoes.some((p) => this.pode(p));
  }

  protected irPara(aba: Aba): void {
    this.aba.set(aba);
    this.carregar();
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected escolherPaciente(p: PacienteResponseDto): void {
    this.busca.set(p.nome);
    this.f.set({ ...this.f(), pacienteId: p.uuid });
  }

  protected notificar(): void {
    const c = this.f();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    this.erroForm.set(null);
    this.protocolo.set(null);
    if (!t('unidadeId') || !t('quando') || !t('local') || !t('tipo') || !t('descricao')) {
      this.erroForm.set('Informe a unidade, quando ocorreu, o local, o tipo e o que aconteceu.');
      return;
    }
    this.enviando.set(true);
    this.service.notificar({ unidadeId: t('unidadeId')!, pacienteId: t('pacienteId'), ocorridoEm: new Date(c['quando']).toISOString(), local: t('local')!,
      tipo: c['tipo'] as TipoIncidente, danoPercebido: (t('dano') as GrauDano | null), descricao: t('descricao')!, acaoImediata: t('acao'), anonima: c['anonima'] === 'sim' })
      .subscribe({
        next: (p) => {
          this.enviando.set(false);
          this.protocolo.set(p);
          this.f.set({ unidadeId: c['unidadeId'] });
          this.busca.set('');
        },
        error: (x: HttpErrorResponse) => {
          this.enviando.set(false);
          this.erroForm.set(mensagem(x));
        },
      });
  }

  protected abrir(id: string): void {
    this.erroGaveta.set(null);
    this.service.incidente(id).subscribe({ next: (i) => this.mostrar(i), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  private mostrar(i: IncidenteDto): void {
    this.detalhe.set(i);
    this.fatores.set(new Set(i.fatores));
    this.f.set({ natureza: i.natureza ?? 'EVENTO_ADVERSO', cdano: i.dano ?? 'LEVE', never: i.neverEvent ? 'sim' : 'nao', causa: i.causaRaiz ?? '', licoes: i.licoes ?? '' });
  }

  protected alternarFator(x: string): void {
    const s = new Set(this.fatores());
    if (s.has(x)) s.delete(x); else s.add(x);
    this.fatores.set(s);
  }

  protected classificar(i: IncidenteDto): void {
    const c = this.f();
    this.executar(this.service.classificar(i.uuid, { natureza: c['natureza'] as NaturezaIncidente, dano: c['cdano'] as GrauDano, tipo: null, neverEvent: c['never'] === 'sim' }));
  }

  protected concluir(i: IncidenteDto): void {
    const c = this.f();
    if (!this.fatores().size) {
      this.erroGaveta.set('Marque ao menos um fator contribuinte.');
      return;
    }
    this.executar(this.service.concluir(i.uuid, { fatores: [...this.fatores()], causaRaiz: (c['causa'] ?? '').trim() || null, licoes: (c['licoes'] ?? '').trim() || null }));
  }

  protected descartar(i: IncidenteDto): void {
    const motivo = (this.f()['descarte'] ?? '').trim();
    if (!motivo) {
      this.erroGaveta.set('Informe o motivo do descarte.');
      return;
    }
    this.executar(this.service.descartar(i.uuid, motivo));
  }

  protected notivisa(i: IncidenteDto): void {
    const numero = (this.f()['notivisa'] ?? '').trim();
    if (!numero) {
      this.erroGaveta.set('Informe o número da notificação no Notivisa.');
      return;
    }
    this.executar(this.service.notivisa(i.uuid, numero));
  }

  private executar(req: Observable<IncidenteDto>): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    req.subscribe({
      next: (i) => {
        this.enviando.set(false);
        this.mostrar(i);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected tagSituacao(i: IncidenteDto): string {
    if (i.situacao === 'CONCLUIDA') return 'ok';
    if (i.situacao === 'DESCARTADA') return '';
    return i.notivisaVencida ? 'alert' : 'warn';
  }

  protected rotulosFatores(i: IncidenteDto): string {
    return i.fatores.map((x) => this.fatoresRot[x]).join(', ');
  }

  protected dataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
  }

  protected carregar(): void {
    this.erro.set(null);
    if (this.pode('QUALIDADE.ANALISAR')) {
      this.service.incidentes((this.situacao() || undefined) as SituacaoIncidente | undefined).subscribe({
        next: (l) => this.incidentes.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
      });
    }
    if (this.podeAlguma('QUALIDADE.ANALISAR', 'INDICADOR.CONSULTAR')) {
      this.service.painel().pipe(catchError(() => of(null))).subscribe((p) => this.painel.set(p));
    }
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
