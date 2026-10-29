import { HttpErrorResponse } from '@angular/common/http';
import { Component, WritableSignal, computed, inject, input, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import {
  NivelEmergenciaDto,
  PlanoContingenciaDto,
  PlanoContingenciaRequestDto,
  SITUACOES_PLANO,
  TipoEmergenciaDto,
} from '../../core/models/emergencia';
import { ErrorResponseDto } from '../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { EmergenciaService } from '../../core/services/emergencia';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { Drawer } from '../../shared/drawer/drawer';

type Gaveta = { tipo: 'ver'; plano: PlanoContingenciaDto } | { tipo: 'editar'; plano: PlanoContingenciaDto | null } | { tipo: 'revogar'; plano: PlanoContingenciaDto };

interface Criterio { nivelId: string; descricao: string }
interface Acao { nivelId: string; descricao: string; responsavel: string; prazoHoras: string }
interface Recurso { descricao: string; quantidade: string }
interface Contato { nome: string; papel: string; telefone: string }

/**
 * Planos de contingência (ADR-0157): a lista das versões, o detalhe com critérios e ações por nível, e o editor do
 * rascunho. Publicar torna vigente e substitui a anterior; revisar abre um rascunho novo a partir da vigente.
 */
@Component({
  selector: 'app-planos-contingencia',
  imports: [Drawer],
  template: `
    <div class="pc-cab">
      <div class="seg" role="group" aria-label="Situação dos planos">
        <button type="button" [attr.aria-pressed]="!todas()" (click)="todas.set(false)">Em uso</button>
        <button type="button" [attr.aria-pressed]="todas()" (click)="todas.set(true)">Todas as versões</button>
      </div>
      @if (podeGerenciar()) { <button type="button" class="btn btn--sm" (click)="abrirEditor(null)">Novo plano</button> }
    </div>
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    <div class="tbl-wrap card">
      <table aria-label="Planos de contingência">
        <thead><tr><th scope="col">Plano</th><th scope="col">Tipo</th><th scope="col">Níveis</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
        <tbody>
          @for (p of visiveis(); track p.uuid) {
            <tr>
              <td><span class="cell-main">{{ p.nome }}</span><span class="cell-sub mono">{{ p.codigo }} · versão {{ p.versao }}</span></td>
              <td>{{ p.tipoNome }}</td>
              <td class="cell-sub">{{ nomesNiveis(p) }}</td>
              <td><span [class]="'tag tag--' + situacoes[p.situacao].classe">{{ situacoes[p.situacao].rotulo }}</span></td>
              <td class="row-actions"><button type="button" class="btn btn--ghost btn--sm" (click)="ver(p.uuid)" [attr.aria-label]="'Abrir ' + p.nome + ' versão ' + p.versao">Abrir</button></td>
            </tr>
          } @empty { <tr><td colspan="5" class="cell-sub">Nenhum plano de contingência.</td></tr> }
        </tbody>
      </table>
    </div>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" [larga]="g.tipo !== 'revogar'" (fechar)="gaveta.set(null)">
        @switch (g.tipo) {
          @case ('ver') {
            <div class="dw-form">
              <div class="dw-body">
                @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
                <dl class="ro">
                  <div><dt>Plano</dt><dd>{{ g.plano.codigo }} · versão {{ g.plano.versao }} · <span [class]="'tag tag--' + situacoes[g.plano.situacao].classe">{{ situacoes[g.plano.situacao].rotulo }}</span></dd></div>
                  <div><dt>Tipo</dt><dd>{{ g.plano.tipoNome }}</dd></div>
                  <div><dt>Vigência</dt><dd>{{ data(g.plano.vigenciaInicio) }} a {{ g.plano.vigenciaFim ? data(g.plano.vigenciaFim) : 'sem fim' }}</dd></div>
                  <div><dt>Objetivo</dt><dd>{{ g.plano.objetivo }}</dd></div>
                  @if (g.plano.unidades.length) { <div><dt>Unidades</dt><dd>{{ nomes(g.plano.unidades) }}</dd></div> }
                  @if (g.plano.motivoRevogacao) { <div><dt>Revogação</dt><dd>{{ g.plano.motivoRevogacao }}</dd></div> }
                </dl>
                @for (n of g.plano.niveis; track n.uuid) {
                  <h4 class="mo-grupo">{{ n.nome }}</h4>
                  <ul class="eq-prof" [attr.aria-label]="'Critérios e ações: ' + n.nome">
                    @for (c of criteriosDo(g.plano, n.uuid); track c.uuid) { <li><span class="cell-main">Critério</span><span class="cell-sub">{{ c.descricao }}</span></li> }
                    @for (a of acoesDo(g.plano, n.uuid); track a.uuid) {
                      <li><span class="cell-main">{{ a.descricao }}</span><span class="cell-sub">{{ a.responsavel }}@if (a.prazoHoras) { · em até {{ a.prazoHoras }} h }@if (a.unidadeNome) { · {{ a.unidadeNome }} }</span></li>
                    }
                  </ul>
                }
                @if (g.plano.recursos.length) {
                  <h4 class="mo-grupo">Recursos necessários</h4>
                  <ul class="eq-prof">@for (r of g.plano.recursos; track $index) { <li><span class="cell-main">{{ r.descricao }}@if (r.quantidade) { · {{ r.quantidade }} }</span></li> }</ul>
                }
                @if (g.plano.contatos.length) {
                  <h4 class="mo-grupo">Contatos</h4>
                  <ul class="eq-prof">@for (c of g.plano.contatos; track $index) { <li><span class="cell-main">{{ c.nome }}</span><span class="cell-sub">@if (c.papel) { {{ c.papel }} · }{{ c.telefone ?? '' }}</span></li> }</ul>
                }
                <h4 class="mo-grupo">Versões</h4>
                <ul class="eq-prof" aria-label="Versões do plano">
                  @for (v of g.plano.versoes; track v.uuid) {
                    <li><span class="cell-main">Versão {{ v.versao }} <span [class]="'tag tag--' + situacoes[v.situacao].classe">{{ situacoes[v.situacao].rotulo }}</span></span><span class="cell-sub">@if (v.publicadoEm) { publicada em {{ dataHora(v.publicadoEm) }} }@if (v.encerradoEm) { · até {{ dataHora(v.encerradoEm) }} }</span></li>
                  }
                </ul>
              </div>
              @if (podeGerenciar()) {
                <div class="dw-foot">
                  @if (g.plano.situacao === 'RASCUNHO') {
                    <button type="button" class="btn btn--ghost" (click)="abrirRevogar(g.plano)">Descartar</button>
                    <button type="button" class="btn btn--ghost" (click)="abrirEditor(g.plano)">Editar</button>
                    <button type="button" class="btn" (click)="acao(publicar(g.plano))">Publicar</button>
                  }
                  @if (g.plano.situacao === 'VIGENTE') {
                    <button type="button" class="btn btn--ghost" (click)="abrirRevogar(g.plano)">Revogar</button>
                    <button type="button" class="btn" (click)="acao(revisar(g.plano))">Revisar</button>
                  }
                </div>
              }
            </div>
          }
          @case ('revogar') {
            <form class="dw-form" (submit)="$event.preventDefault(); acao(revogar(g.plano))" novalidate>
              <div class="dw-body">
                @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
                <div class="dw-field"><label for="f-pc-motivo">Motivo <span class="req" aria-hidden="true">*</span></label><input id="f-pc-motivo" type="text" maxlength="500" [value]="motivo()" (input)="motivo.set($any($event.target).value)" /></div>
              </div>
              <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn">Confirmar</button></div>
            </form>
          }
          @case ('editar') {
            <form class="dw-form" (submit)="$event.preventDefault(); salvar(g.plano)" novalidate>
              <div class="dw-body">
                @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
                <div class="dw-row">
                  @if (!g.plano) { <div class="dw-field"><label for="f-pc-codigo">Código <span class="req" aria-hidden="true">*</span></label><input id="f-pc-codigo" type="text" maxlength="40" [value]="f()['codigo'] ?? ''" (input)="set('codigo', $event)" placeholder="Ex.: ENCHENTES" /></div> }
                  <div class="dw-field"><label for="f-pc-nome">Nome <span class="req" aria-hidden="true">*</span></label><input id="f-pc-nome" type="text" maxlength="200" [value]="f()['nome'] ?? ''" (input)="set('nome', $event)" /></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-pc-tipo">Tipo de emergência <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-pc-tipo" (change)="f.set({ ...f(), tipoId: $any($event.target).value })">
                      <option value="" [selected]="!f()['tipoId']">Escolha…</option>
                      @for (t of tipos(); track t.uuid) { <option [value]="t.uuid" [selected]="f()['tipoId'] === t.uuid">{{ t.nome }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-pc-inicio">Vigência · início <span class="req" aria-hidden="true">*</span></label><input id="f-pc-inicio" type="date" [value]="f()['inicio'] ?? ''" (input)="set('inicio', $event)" /></div>
                  <div class="dw-field"><label for="f-pc-fim">Vigência · fim<span class="opt">opcional</span></label><input id="f-pc-fim" type="date" [value]="f()['fim'] ?? ''" (input)="set('fim', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-pc-objetivo">Objetivo <span class="req" aria-hidden="true">*</span></label><textarea id="f-pc-objetivo" rows="2" maxlength="2000" [value]="f()['objetivo'] ?? ''" (input)="set('objetivo', $event)"></textarea></div>
                <fieldset class="dw-field"><legend>Níveis que o plano cobre <span class="req" aria-hidden="true">*</span></legend>
                  <div class="pc-checks">
                    @for (n of niveis(); track n.uuid) { <label><input type="checkbox" [checked]="niveisMarcados().has(n.uuid)" (change)="alternarNivel(n.uuid)" /> {{ n.nome }}</label> }
                  </div>
                </fieldset>
                <div class="dw-field"><label for="f-pc-unidades">Unidades envolvidas<span class="opt">opcional; Ctrl para várias</span></label>
                  <select id="f-pc-unidades" multiple size="4" (change)="unidadesEscolhidas($event)">
                    @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="unidadesMarcadas().has(u.uuid)">{{ u.nome }}</option> }
                  </select></div>

                <h4 class="mo-grupo">Critérios de ativação</h4>
                @for (c of criterios(); track $index; let i = $index) {
                  <div class="pc-linha">
                    <select [attr.aria-label]="'Nível do critério ' + (i + 1)" (change)="mudar(criterios, i, 'nivelId', $event)">
                      @for (n of niveisDoPlano(); track n.uuid) { <option [value]="n.uuid" [selected]="c.nivelId === n.uuid">{{ n.nome }}</option> }
                    </select>
                    <input type="text" maxlength="1000" [attr.aria-label]="'Critério ' + (i + 1)" [value]="c.descricao" (input)="mudar(criterios, i, 'descricao', $event)" placeholder="Ex.: rio acima de 4 m" />
                    <button type="button" class="link-btn" (click)="remover(criterios, i)" [attr.aria-label]="'Remover critério ' + (i + 1)">Remover</button>
                  </div>
                }
                <button type="button" class="link-btn" (click)="adicionarCriterio()" [disabled]="!niveisDoPlano().length">Adicionar critério</button>

                <h4 class="mo-grupo">Ações previstas</h4>
                @for (a of acoes(); track $index; let i = $index) {
                  <div class="pc-linha">
                    <select [attr.aria-label]="'Nível da ação ' + (i + 1)" (change)="mudar(acoes, i, 'nivelId', $event)">
                      @for (n of niveisDoPlano(); track n.uuid) { <option [value]="n.uuid" [selected]="a.nivelId === n.uuid">{{ n.nome }}</option> }
                    </select>
                    <input type="text" maxlength="1000" [attr.aria-label]="'Ação ' + (i + 1)" [value]="a.descricao" (input)="mudar(acoes, i, 'descricao', $event)" placeholder="Ação" />
                    <input type="text" maxlength="200" [attr.aria-label]="'Responsável pela ação ' + (i + 1)" [value]="a.responsavel" (input)="mudar(acoes, i, 'responsavel', $event)" placeholder="Responsável" />
                    <input type="number" min="1" class="pc-prazo" [attr.aria-label]="'Prazo em horas da ação ' + (i + 1)" [value]="a.prazoHoras" (input)="mudar(acoes, i, 'prazoHoras', $event)" placeholder="h" />
                    <button type="button" class="link-btn" (click)="remover(acoes, i)" [attr.aria-label]="'Remover ação ' + (i + 1)">Remover</button>
                  </div>
                }
                <button type="button" class="link-btn" (click)="adicionarAcao()" [disabled]="!niveisDoPlano().length">Adicionar ação</button>

                <h4 class="mo-grupo">Recursos necessários</h4>
                @for (r of recursos(); track $index; let i = $index) {
                  <div class="pc-linha">
                    <input type="text" maxlength="300" [attr.aria-label]="'Recurso ' + (i + 1)" [value]="r.descricao" (input)="mudar(recursos, i, 'descricao', $event)" placeholder="Ex.: kits de hidratação" />
                    <input type="number" min="1" class="pc-prazo" [attr.aria-label]="'Quantidade do recurso ' + (i + 1)" [value]="r.quantidade" (input)="mudar(recursos, i, 'quantidade', $event)" placeholder="qtd." />
                    <button type="button" class="link-btn" (click)="remover(recursos, i)" [attr.aria-label]="'Remover recurso ' + (i + 1)">Remover</button>
                  </div>
                }
                <button type="button" class="link-btn" (click)="recursos.set([...recursos(), { descricao: '', quantidade: '' }])">Adicionar recurso</button>

                <h4 class="mo-grupo">Contatos</h4>
                @for (c of contatos(); track $index; let i = $index) {
                  <div class="pc-linha">
                    <input type="text" maxlength="200" [attr.aria-label]="'Nome do contato ' + (i + 1)" [value]="c.nome" (input)="mudar(contatos, i, 'nome', $event)" placeholder="Nome" />
                    <input type="text" maxlength="200" [attr.aria-label]="'Papel do contato ' + (i + 1)" [value]="c.papel" (input)="mudar(contatos, i, 'papel', $event)" placeholder="Papel" />
                    <input type="tel" maxlength="30" [attr.aria-label]="'Telefone do contato ' + (i + 1)" [value]="c.telefone" (input)="mudar(contatos, i, 'telefone', $event)" placeholder="Telefone" />
                    <button type="button" class="link-btn" (click)="remover(contatos, i)" [attr.aria-label]="'Remover contato ' + (i + 1)">Remover</button>
                  </div>
                }
                <button type="button" class="link-btn" (click)="contatos.set([...contatos(), { nome: '', papel: '', telefone: '' }])">Adicionar contato</button>
              </div>
              <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar rascunho' }}</button></div>
            </form>
          }
        }
      </app-drawer>
    }
  `,
  styles: `
    .pc-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-3); margin-bottom: var(--s-3); flex-wrap: wrap; }
    .pc-checks { display: flex; flex-wrap: wrap; gap: var(--s-2) var(--s-4); }
    .pc-checks label { display: inline-flex; align-items: center; gap: var(--s-2); font-weight: 400; }
    .dw-field .pc-checks input { width: auto; min-height: 0; padding: 0; }
    .pc-linha { display: flex; gap: var(--s-2); align-items: center; margin-bottom: var(--s-2); flex-wrap: wrap; }
    .pc-linha input, .pc-linha select { border: 1.5px solid var(--ink-200); border-radius: 10px; padding: 9px 10px; font: inherit; color: var(--ink-900); background: var(--white); min-height: 40px; }
    .pc-linha input:focus, .pc-linha select:focus { outline: none; border-color: var(--blue-700); box-shadow: 0 0 0 4px rgba(19,81,180,0.12); }
    .pc-linha input[type='text'], .pc-linha input[type='tel'] { flex: 1 1 160px; }
    .pc-linha .pc-prazo { width: 84px; }
  `,
})
export class PlanosContingencia {
  private readonly service = inject(EmergenciaService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  readonly tipos = input<TipoEmergenciaDto[]>([]);
  readonly niveis = input<NivelEmergenciaDto[]>([]);
  readonly podeGerenciar = input(false);

  protected readonly situacoes = SITUACOES_PLANO;
  protected readonly planos = signal<PlanoContingenciaDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly todas = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly motivo = signal('');
  protected readonly f = signal<Record<string, string>>({});
  protected readonly niveisMarcados = signal<Set<string>>(new Set());
  protected readonly unidadesMarcadas = signal<Set<string>>(new Set());
  protected readonly criterios = signal<Criterio[]>([]);
  protected readonly acoes = signal<Acao[]>([]);
  protected readonly recursos = signal<Recurso[]>([]);
  protected readonly contatos = signal<Contato[]>([]);

  protected readonly visiveis = computed(() => this.planos().filter((p) => this.todas() || p.situacao === 'RASCUNHO' || p.situacao === 'VIGENTE'));
  protected readonly niveisDoPlano = computed(() => this.niveis().filter((n) => this.niveisMarcados().has(n.uuid)));

  constructor() {
    this.carregar();
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
  }

  protected titulo(g: Gaveta): string {
    if (g.tipo === 'ver') return g.plano.nome;
    if (g.tipo === 'revogar') return g.plano.situacao === 'RASCUNHO' ? 'Descartar o rascunho' : 'Revogar o plano';
    return g.plano ? `Editar o rascunho (versão ${g.plano.versao})` : 'Novo plano de contingência';
  }

  protected nomesNiveis(p: PlanoContingenciaDto): string {
    return p.niveis.map((n) => n.nome.split(' · ')[0]).join(', ');
  }

  protected nomes(l: { nome: string }[]): string {
    return l.map((x) => x.nome).join(', ');
  }

  protected criteriosDo(p: PlanoContingenciaDto, nivel: string) {
    return p.criterios.filter((c) => c.nivelId === nivel);
  }

  protected acoesDo(p: PlanoContingenciaDto, nivel: string) {
    return p.acoes.filter((a) => a.nivelId === nivel);
  }

  protected ver(uuid: string): void {
    this.erroGaveta.set(null);
    this.service.plano(uuid).subscribe({ next: (p) => this.gaveta.set({ tipo: 'ver', plano: p }), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }

  protected abrirRevogar(p: PlanoContingenciaDto): void {
    this.erroGaveta.set(null);
    this.motivo.set('');
    this.gaveta.set({ tipo: 'revogar', plano: p });
  }

  protected abrirEditor(p: PlanoContingenciaDto | null): void {
    this.erroGaveta.set(null);
    this.f.set(p ? { nome: p.nome, tipoId: p.tipoId, objetivo: p.objetivo, inicio: p.vigenciaInicio, fim: p.vigenciaFim ?? '' } : { inicio: new Date().toISOString().slice(0, 10) });
    this.niveisMarcados.set(new Set(p?.niveis.map((n) => n.uuid) ?? []));
    this.unidadesMarcadas.set(new Set(p?.unidades.map((u) => u.uuid) ?? []));
    this.criterios.set(p?.criterios.map((c) => ({ nivelId: c.nivelId, descricao: c.descricao })) ?? []);
    this.acoes.set(p?.acoes.map((a) => ({ nivelId: a.nivelId, descricao: a.descricao, responsavel: a.responsavel, prazoHoras: a.prazoHoras != null ? String(a.prazoHoras) : '' })) ?? []);
    this.recursos.set(p?.recursos.map((r) => ({ descricao: r.descricao, quantidade: r.quantidade != null ? String(r.quantidade) : '' })) ?? []);
    this.contatos.set(p?.contatos.map((c) => ({ nome: c.nome, papel: c.papel ?? '', telefone: c.telefone ?? '' })) ?? []);
    this.gaveta.set({ tipo: 'editar', plano: p });
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected mudar<T>(lista: WritableSignal<T[]>, i: number, campo: keyof T, ev: Event): void {
    const copia = [...lista()];
    copia[i] = { ...copia[i], [campo]: (ev.target as HTMLInputElement).value };
    lista.set(copia);
  }

  protected remover<T>(lista: WritableSignal<T[]>, i: number): void {
    lista.set(lista().filter((_, j) => j !== i));
  }

  protected alternarNivel(id: string): void {
    const s = new Set(this.niveisMarcados());
    if (s.has(id)) s.delete(id);
    else s.add(id);
    this.niveisMarcados.set(s);
  }

  protected unidadesEscolhidas(ev: Event): void {
    this.unidadesMarcadas.set(new Set(Array.from((ev.target as HTMLSelectElement).selectedOptions).map((o) => o.value)));
  }

  protected adicionarCriterio(): void {
    this.criterios.set([...this.criterios(), { nivelId: this.niveisDoPlano()[0].uuid, descricao: '' }]);
  }

  protected adicionarAcao(): void {
    this.acoes.set([...this.acoes(), { nivelId: this.niveisDoPlano()[0].uuid, descricao: '', responsavel: '', prazoHoras: '' }]);
  }

  protected publicar(p: PlanoContingenciaDto): Observable<PlanoContingenciaDto> {
    return this.service.publicarPlano(p.uuid);
  }

  protected revisar(p: PlanoContingenciaDto): Observable<PlanoContingenciaDto> {
    return this.service.revisarPlano(p.uuid);
  }

  protected revogar(p: PlanoContingenciaDto): Observable<PlanoContingenciaDto> {
    return this.service.revogarPlano(p.uuid, this.motivo().trim());
  }

  /** Executa a ação da gaveta e mostra a versão resultante. */
  protected acao(req: Observable<PlanoContingenciaDto>): void {
    this.erroGaveta.set(null);
    req.subscribe({
      next: (p) => {
        this.gaveta.set({ tipo: 'ver', plano: p });
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.erroGaveta.set(mensagem(e)),
    });
  }

  protected salvar(base: PlanoContingenciaDto | null): void {
    if (this.enviando()) return;
    const f = this.f();
    const t = (k: string) => (f[k] ?? '').trim();
    const niveis = [...this.niveisMarcados()];
    if ((!base && !t('codigo')) || !t('nome') || !t('tipoId') || !t('objetivo') || !t('inicio') || !niveis.length) {
      this.erroGaveta.set('Informe o código, o nome, o tipo, o objetivo, o início da vigência e os níveis.');
      return;
    }
    const validos = new Set(niveis);
    const dto: PlanoContingenciaRequestDto = {
      codigo: base ? null : t('codigo'), nome: t('nome'), tipoId: t('tipoId'), objetivo: t('objetivo'), vigenciaInicio: t('inicio'),
      vigenciaFim: t('fim') || null, nivelIds: niveis, unidadeIds: [...this.unidadesMarcadas()],
      criterios: this.criterios().filter((c) => c.descricao.trim() && validos.has(c.nivelId)).map((c) => ({ nivelId: c.nivelId, descricao: c.descricao.trim() })),
      acoes: this.acoes().filter((a) => a.descricao.trim() && validos.has(a.nivelId)).map((a) => ({
        nivelId: a.nivelId, descricao: a.descricao.trim(), responsavel: a.responsavel.trim(), prazoHoras: a.prazoHoras ? Number(a.prazoHoras) : null, unidadeId: null,
      })),
      recursos: this.recursos().filter((r) => r.descricao.trim()).map((r) => ({ descricao: r.descricao.trim(), quantidade: r.quantidade ? Number(r.quantidade) : null })),
      contatos: this.contatos().filter((c) => c.nome.trim()).map((c) => ({ nome: c.nome.trim(), papel: c.papel.trim() || null, telefone: c.telefone.trim() || null })),
    };
    this.enviando.set(true);
    this.service.salvarPlano(base?.uuid ?? null, dto).subscribe({
      next: (p) => {
        this.enviando.set(false);
        this.gaveta.set({ tipo: 'ver', plano: p });
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(e));
      },
    });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  private carregar(): void {
    this.service.planos().subscribe({ next: (l) => this.planos.set(l), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos: ações pedem a descrição e o responsável.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
