import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../core/models/auth';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  AuditoriaProtocoloDto,
  ChecklistDto,
  INDICADORES_QUALIDADE,
  IndicadorQualidade,
  LinhaIndicadorDto,
  MetaQualidadeDto,
  PainelQualidadeDto,
  RESPOSTAS_ITEM,
  RespostaItem,
  SITUACOES_INDICADOR,
  SituacaoIndicador,
} from '../../core/models/qualidade';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { AuthService } from '../../core/services/auth';
import { QualidadeService } from '../../core/services/qualidade';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';

type Aba = 'painel' | 'metas' | 'auditorias' | 'checklists';

/**
 * Indicadores de qualidade com metas e auditoria de protocolos, fatia Q3 da Qualidade (ADR-0205): o painel do mês com a meta da unidade e a
 * série de 12 meses (sem base, nunca zero), o histórico de metas, a auditoria de adesão por checklist versionado e o atalho para abrir a não
 * conformidade do indicador fora da meta.
 */
@Component({
  selector: 'app-indicadores-qualidade',
  imports: [RouterLink],
  template: `
    <header class="page-head">
      <div>
        <h1>Indicadores de qualidade</h1>
        <p>Metas por unidade, série de 12 meses e adesão aos protocolos</p>
      </div>
    </header>

    <div class="tabs" role="tablist" aria-label="Seções dos indicadores de qualidade">
      @if (podeAlguma('QUALIDADE.GERIR', 'INDICADOR.CONSULTAR')) {
        <button type="button" role="tab" id="tab-painel" [attr.aria-selected]="aba() === 'painel'" (click)="aba.set('painel')">Painel</button>
        <button type="button" role="tab" id="tab-metas" [attr.aria-selected]="aba() === 'metas'" (click)="aba.set('metas')">Metas</button>
      }
      @if (podeAlguma('QUALIDADE.GERIR', 'QUALIDADE.ANALISAR')) { <button type="button" role="tab" id="tab-auditorias" [attr.aria-selected]="aba() === 'auditorias'" (click)="aba.set('auditorias')">Auditorias de protocolo</button> }
      @if (pode('QUALIDADE.GERIR')) { <button type="button" role="tab" id="tab-checklists" [attr.aria-selected]="aba() === 'checklists'" (click)="aba.set('checklists')">Checklists</button> }
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (aviso()) { <div class="box box--ok" role="status"><strong>{{ aviso() }}</strong></div> }

    <div class="iq-cab">
      <div class="dw-field" style="margin: 0;"><label for="f-iq-unidade">Unidade</label>
        <select id="f-iq-unidade" (change)="unidadeId.set($any($event.target).value); carregar()">
          <option value="">Todas as visíveis</option>
          @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="unidadeId() === u.uuid">{{ u.nome }}</option> }
        </select></div>
      @if (aba() === 'painel') {
        <div class="dw-field" style="margin: 0;"><label for="f-iq-comp">Competência</label><input id="f-iq-comp" type="month" [value]="competencia()" (change)="competencia.set($any($event.target).value); carregar()" /></div>
      }
    </div>

    @switch (aba()) {
      @case ('painel') {
        @if (painel(); as p) {
          @if (!unidadeId()) { <p class="cell-sub">As metas valem por unidade: escolha uma para comparar.</p> }
          <div class="tbl-wrap card">
            <table class="iq-tabela" aria-label="Indicadores de qualidade">
              <thead><tr><th scope="col">Indicador</th><th scope="col">Valor</th><th scope="col">Meta</th><th scope="col">Situação</th><th scope="col">12 meses</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
              <tbody>
                @for (l of p.linhas; track l.indicador + (l.checklistId ?? '')) {
                  <tr>
                    <td><span class="cell-main">{{ l.nome }}</span><span class="cell-sub">{{ l.medida }} · {{ l.menorMelhor ? 'menor é melhor' : 'maior é melhor' }}</span></td>
                    <td class="mono">{{ l.valor == null ? '—' : num(l.valor) }}@if (l.denominador != null) { <span class="cell-sub">{{ num(l.numerador ?? 0) }} / {{ num(l.denominador) }}</span> }</td>
                    <td class="mono">{{ l.meta == null ? '—' : (l.menorMelhor ? '≤ ' : '≥ ') + num(l.meta) }}</td>
                    <td><span [class]="'tag tag--' + tagSituacao(l.situacao)">{{ situacoes[l.situacao] }}</span></td>
                    <td>
                      <svg class="iq-serie" viewBox="0 0 120 28" role="img" [attr.aria-label]="'Série de ' + l.nome + ': ' + serieTexto(l)">
                        <polyline [attr.points]="pontos(l)" fill="none" stroke="currentColor" stroke-width="1.5" />
                      </svg>
                    </td>
                    <td class="row-actions">
                      @if (l.situacao === 'FORA' && unidadeId() && pode('QUALIDADE.GERIR')) {
                        <a class="link-btn" [routerLink]="['/qualidade/nao-conformidades']" [queryParams]="{ origem: 'INDICADOR', unidade: unidadeId(), indicador: l.indicador, competencia: p.competencia, checklist: l.checklistId ?? '', descricao: l.nome + ' fora da meta em ' + p.competencia + ': ' + num(l.valor ?? 0) + ' ' + l.medida + ', meta ' + num(l.meta ?? 0) + '.' }" [attr.aria-label]="'Abrir não conformidade de ' + l.nome">Abrir não conformidade</a>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      }
      @case ('metas') {
        @if (pode('QUALIDADE.GERIR')) {
          <fieldset class="iq-grupo card"><legend>Definir meta</legend>
            <div class="dw-row">
              <div class="dw-field"><label for="f-meta-ind">Indicador</label>
                <select id="f-meta-ind" (change)="set('indicador', $event)">
                  <option value="" [selected]="!f()['indicador']">Escolha…</option>
                  @for (i of listaIndicadores; track i) { <option [value]="i" [selected]="f()['indicador'] === i">{{ indicadores[i] }}</option> }
                </select></div>
              @if (f()['indicador'] === 'ADESAO_PROTOCOLO') {
                <div class="dw-field"><label for="f-meta-check">Protocolo</label>
                  <select id="f-meta-check" (change)="set('checklistId', $event)">
                    <option value="" [selected]="!f()['checklistId']">Escolha…</option>
                    @for (c of checklists(); track c.uuid) { <option [value]="c.uuid" [selected]="f()['checklistId'] === c.uuid">{{ c.nome }}</option> }
                  </select></div>
              }
              <div class="dw-field"><label for="f-meta-valor">Meta</label><input id="f-meta-valor" type="number" min="0" step="0.01" [value]="f()['valor'] ?? ''" (input)="set('valor', $event)" /></div>
              <div class="dw-field"><label for="f-meta-desde">Vigente desde</label><input id="f-meta-desde" type="month" [value]="f()['desde'] ?? ''" (input)="set('desde', $event)" /></div>
            </div>
            <div class="row-actions"><button type="button" class="btn btn--sm" (click)="definirMeta()" [disabled]="enviando()">Definir meta</button></div>
          </fieldset>
        }
        <div class="tbl-wrap card">
          <table class="iq-tabela" aria-label="Metas dos indicadores">
            <thead><tr><th scope="col">Unidade</th><th scope="col">Indicador</th><th scope="col">Meta</th><th scope="col">Vigência</th></tr></thead>
            <tbody>
              @for (m of metas(); track m.uuid) {
                <tr [class.iq-antiga]="m.vigenteAte">
                  <td>{{ m.unidadeNome }}</td>
                  <td>{{ m.indicadorNome }}@if (m.checklistNome) { <span class="cell-sub">{{ m.checklistNome }}</span> }</td>
                  <td class="mono">{{ num(m.valor) }}</td>
                  <td class="mono">{{ mes(m.vigenteDesde) }} a {{ m.vigenteAte ? data(m.vigenteAte) : 'hoje' }}</td>
                </tr>
              } @empty { <tr><td colspan="4" class="cell-sub">Nenhuma meta.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('auditorias') {
        <fieldset class="iq-grupo card"><legend>Registrar auditoria</legend>
          <div class="dw-row">
            <div class="dw-field"><label for="f-aud-check">Protocolo</label>
              <select id="f-aud-check" (change)="escolherChecklist($any($event.target).value)">
                <option value="" [selected]="!auditando()">Escolha…</option>
                @for (c of checklists(); track c.uuid) { <option [value]="c.uuid" [selected]="auditando()?.uuid === c.uuid">{{ c.nome }} (v{{ c.versao }})</option> }
              </select></div>
            <div class="dw-field"><label for="f-aud-data">Data</label><input id="f-aud-data" type="date" [value]="f()['dataAuditoria'] ?? ''" (input)="set('dataAuditoria', $event)" /></div>
          </div>
          @if (auditando(); as c) {
            <ol class="iq-itens" aria-label="Itens do checklist">
              @for (i of c.itens; track i.codigo) {
                <li>
                  <span><span class="mono">{{ i.codigo }}</span> {{ i.texto }}</span>
                  <span class="iq-respostas" role="radiogroup" [attr.aria-label]="'Resposta do item ' + i.codigo">
                    @for (r of listaRespostas; track r) {
                      <label class="chk"><input type="radio" [name]="'resp-' + i.codigo" [checked]="respostas()[i.codigo] === r" (change)="responder(i.codigo, r)" /> {{ respostasRot[r] }}</label>
                    }
                  </span>
                </li>
              }
            </ol>
            <p class="cell-sub" aria-live="polite">Adesão parcial: {{ adesaoParcial() == null ? '—' : num(adesaoParcial()!) + '%' }}</p>
            <div class="dw-field"><label for="f-aud-obs">Observação<span class="opt">opcional</span></label><input id="f-aud-obs" type="text" maxlength="1000" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" /></div>
            <div class="row-actions"><button type="button" class="btn btn--sm" (click)="registrarAuditoria()" [disabled]="enviando()">Registrar auditoria</button></div>
          }
        </fieldset>
        <div class="tbl-wrap card">
          <table class="iq-tabela" aria-label="Auditorias de protocolo">
            <thead><tr><th scope="col">Data</th><th scope="col">Unidade</th><th scope="col">Protocolo</th><th scope="col">Adesão</th></tr></thead>
            <tbody>
              @for (a of auditorias(); track a.uuid) {
                <tr>
                  <td class="mono">{{ data(a.dataAuditoria) }}</td>
                  <td>{{ a.unidadeNome }}@if (a.setorNome) { <span class="cell-sub">{{ a.setorNome }}</span> }</td>
                  <td>{{ a.checklistNome }} <span class="cell-sub">versão {{ a.versao }}</span></td>
                  <td class="mono">{{ num(a.adesao) }}% <span class="cell-sub">{{ a.conformes }} de {{ a.aplicaveis }}</span></td>
                </tr>
              } @empty { <tr><td colspan="4" class="cell-sub">Nenhuma auditoria nos últimos três meses.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('checklists') {
        <fieldset class="iq-grupo card"><legend>{{ versionando() ? 'Nova versão de ' + versionando()!.nome : 'Novo checklist' }}</legend>
          @if (!versionando()) {
            <div class="dw-row">
              <div class="dw-field"><label for="f-ck-cod">Código</label><input id="f-ck-cod" type="text" maxlength="40" [value]="f()['codigo'] ?? ''" (input)="set('codigo', $event)" /></div>
              <div class="dw-field"><label for="f-ck-nome">Nome</label><input id="f-ck-nome" type="text" maxlength="200" [value]="f()['nome'] ?? ''" (input)="set('nome', $event)" /></div>
            </div>
          }
          <div class="dw-field"><label for="f-ck-itens">Itens, um por linha: código; texto</label><textarea id="f-ck-itens" rows="5" [value]="f()['itens'] ?? ''" (input)="set('itens', $event)"></textarea></div>
          <div class="row-actions">
            @if (versionando()) { <button type="button" class="btn btn--ghost btn--sm" (click)="versionando.set(null); f.set({})">Cancelar</button> }
            <button type="button" class="btn btn--sm" (click)="salvarChecklist()" [disabled]="enviando()">{{ versionando() ? 'Publicar versão' : 'Cadastrar checklist' }}</button>
          </div>
        </fieldset>
        <div class="iq-grade">
          @for (c of checklists(); track c.uuid) {
            <div class="card">
              <h3 class="mo-grupo">{{ c.nome }} <span class="cell-sub">{{ c.codigo }} · versão {{ c.versao }}</span></h3>
              <ol class="iq-lista">@for (i of c.itens; track i.codigo) { <li><span class="mono">{{ i.codigo }}</span> {{ i.texto }}</li> }</ol>
              <div class="row-actions"><button type="button" class="link-btn" (click)="novaVersao(c)" [attr.aria-label]="'Nova versão de ' + c.nome">Nova versão</button></div>
            </div>
          }
        </div>
      }
    }
  `,
  styles: `
    .iq-cab { display: flex; gap: var(--s-3); align-items: flex-end; margin-bottom: var(--s-2); flex-wrap: wrap; }
    .iq-tabela { min-width: 0; width: 100%; }
    .iq-serie { width: 120px; height: 28px; color: var(--brand-600, #1d4ed8); display: block; }
    .iq-grupo { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); margin: 0 0 var(--s-3); display: grid; gap: var(--s-2); }
    .iq-itens { margin: 0; padding-left: var(--s-4); display: grid; gap: var(--s-2); }
    .iq-itens li { display: grid; gap: var(--s-1); }
    .iq-respostas { display: flex; gap: var(--s-3); flex-wrap: wrap; }
    .iq-antiga { opacity: 0.7; }
    .iq-grade { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: var(--s-3); }
    .iq-grade .card { padding: var(--s-3); }
    .iq-lista { margin: 0 0 var(--s-2); padding-left: var(--s-4); display: grid; gap: var(--s-1); }
    .chk { display: flex; gap: var(--s-1); align-items: center; }
  `,
})
export class IndicadoresQualidade {
  private readonly service = inject(QualidadeService);
  private readonly authService = inject(AuthService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly indicadores = INDICADORES_QUALIDADE;
  protected readonly listaIndicadores = Object.keys(INDICADORES_QUALIDADE) as IndicadorQualidade[];
  protected readonly situacoes = SITUACOES_INDICADOR;
  protected readonly respostasRot = RESPOSTAS_ITEM;
  protected readonly listaRespostas = Object.keys(RESPOSTAS_ITEM) as RespostaItem[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('painel');
  protected readonly erro = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly unidadeId = signal('');
  protected readonly competencia = signal(mesAnterior());
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly painel = signal<PainelQualidadeDto | null>(null);
  protected readonly metas = signal<MetaQualidadeDto[]>([]);
  protected readonly checklists = signal<ChecklistDto[]>([]);
  protected readonly auditorias = signal<AuditoriaProtocoloDto[]>([]);
  protected readonly auditando = signal<ChecklistDto | null>(null);
  protected readonly versionando = signal<ChecklistDto | null>(null);
  protected readonly respostas = signal<Record<string, RespostaItem>>({});
  protected readonly adesaoParcial = computed(() => {
    const r = Object.values(this.respostas());
    const aplicaveis = r.filter((x) => x !== 'NAO_SE_APLICA').length;
    return aplicaveis ? Math.round((r.filter((x) => x === 'CONFORME').length * 1000) / aplicaveis) / 10 : null;
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      if (!this.podeAlguma('QUALIDADE.GERIR', 'INDICADOR.CONSULTAR')) this.aba.set('auditorias');
      this.carregar();
    });
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidades.set(l));
  }

  protected pode(permissao: string): boolean {
    const a = this.acesso();
    return !a.restrito || a.permissoes.has(permissao);
  }

  protected podeAlguma(...permissoes: string[]): boolean {
    return permissoes.some((p) => this.pode(p));
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected carregar(): void {
    this.erro.set(null);
    const unidade = this.unidadeId() || null;
    if (this.podeAlguma('QUALIDADE.GERIR', 'INDICADOR.CONSULTAR')) {
      this.service.indicadores(unidade, this.competencia() || null).subscribe({
        next: (p) => this.painel.set(p), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
      });
      this.service.metas(unidade).pipe(catchError(() => of([]))).subscribe((l) => this.metas.set(l));
    }
    this.service.checklists().pipe(catchError(() => of([]))).subscribe((l) => this.checklists.set(l));
    if (this.podeAlguma('QUALIDADE.GERIR', 'QUALIDADE.ANALISAR')) {
      this.service.auditoriasProtocolo(unidade).pipe(catchError(() => of([]))).subscribe((l) => this.auditorias.set(l));
    }
  }

  protected definirMeta(): void {
    const c = this.f();
    if (!this.unidadeId() || !c['indicador'] || !c['valor']) {
      this.erro.set('Escolha a unidade no alto da página, o indicador e a meta.');
      return;
    }
    this.enviar(this.service.definirMeta({ unidadeId: this.unidadeId(), indicador: c['indicador'], checklistId: c['checklistId'] || null,
      valor: Number(c['valor']), vigenteDesde: c['desde'] ? c['desde'] + '-01' : null }), 'Meta definida.');
  }

  protected escolherChecklist(id: string): void {
    this.auditando.set(this.checklists().find((c) => c.uuid === id) ?? null);
    this.respostas.set({});
  }

  protected responder(codigo: string, r: RespostaItem): void {
    this.respostas.set({ ...this.respostas(), [codigo]: r });
  }

  protected registrarAuditoria(): void {
    const c = this.auditando();
    if (!c) return;
    if (!this.unidadeId()) {
      this.erro.set('Escolha a unidade no alto da página.');
      return;
    }
    if (c.itens.some((i) => !this.respostas()[i.codigo])) {
      this.erro.set('Responda todos os itens do checklist.');
      return;
    }
    this.enviar(this.service.registrarAuditoria({ unidadeId: this.unidadeId(), versaoId: c.versaoId, dataAuditoria: this.f()['dataAuditoria'] || null,
      respostas: this.respostas(), observacao: (this.f()['observacao'] ?? '').trim() || null }), 'Auditoria registrada.', () => {
      this.auditando.set(null);
      this.respostas.set({});
    });
  }

  protected novaVersao(c: ChecklistDto): void {
    this.versionando.set(c);
    this.f.set({ itens: c.itens.map((i) => `${i.codigo}; ${i.texto}`).join('\n') });
  }

  protected salvarChecklist(): void {
    const c = this.f();
    const itens = (c['itens'] ?? '').split('\n').map((l) => l.trim()).filter((l) => l).map((l) => {
      const i = l.indexOf(';');
      return i < 0 ? { codigo: '', texto: l } : { codigo: l.slice(0, i).trim(), texto: l.slice(i + 1).trim() };
    });
    if (!itens.length || itens.some((i) => !i.codigo || !i.texto)) {
      this.erro.set('Escreva os itens como "código; texto", um por linha.');
      return;
    }
    const v = this.versionando();
    if (v) {
      this.enviar(this.service.novaVersaoChecklist(v.uuid, { itens }), 'Versão publicada.', () => this.versionando.set(null));
    } else {
      if (!(c['codigo'] ?? '').trim() || !(c['nome'] ?? '').trim()) {
        this.erro.set('Informe o código e o nome do checklist.');
        return;
      }
      this.enviar(this.service.criarChecklist({ codigo: c['codigo'].trim(), nome: c['nome'].trim(), itens }), 'Checklist cadastrado.');
    }
  }

  private enviar(req: ReturnType<QualidadeService['definirMeta']> | ReturnType<QualidadeService['criarChecklist']>
    | ReturnType<QualidadeService['registrarAuditoria']>, ok: string, depois?: () => void): void {
    this.enviando.set(true);
    this.erro.set(null);
    this.aviso.set(null);
    (req as ReturnType<QualidadeService['definirMeta']>).subscribe({
      next: () => {
        this.enviando.set(false);
        this.aviso.set(ok);
        this.f.set({});
        depois?.();
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set(mensagem(x));
      },
    });
  }

  protected tagSituacao(s: SituacaoIndicador): string {
    return s === 'DENTRO' ? 'ok' : s === 'FORA' ? 'alert' : s === 'SEM_META' ? 'info' : '';
  }

  protected pontos(l: LinhaIndicadorDto): string {
    const v = l.serie.map((p) => p.valor);
    const validos = v.filter((x): x is number => x != null);
    if (!validos.length) return '';
    const max = Math.max(...validos, l.meta ?? 0) || 1;
    return v.map((x, i) => (x == null ? null : `${(i * 120) / Math.max(v.length - 1, 1)},${26 - (x / max) * 24}`)).filter((p) => p).join(' ');
  }

  protected serieTexto(l: LinhaIndicadorDto): string {
    return l.serie.map((p) => `${p.competencia} ${p.valor == null ? 'sem base' : this.num(p.valor)}`).join('; ');
  }

  protected num(v: number): string {
    return v.toLocaleString('pt-BR', { maximumFractionDigits: 2 });
  }

  protected mes(iso: string): string {
    return `${iso.slice(5, 7)}/${iso.slice(0, 4)}`;
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}

function mesAnterior(): string {
  const d = new Date();
  d.setDate(1);
  d.setMonth(d.getMonth() - 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
