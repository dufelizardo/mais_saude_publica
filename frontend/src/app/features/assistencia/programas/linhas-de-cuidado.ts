import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { ImunobiologicoDto } from '../../../core/models/imunizacao';
import { ExameLaboratorialResponseDto } from '../../../core/models/laboratorio';
import {
  EtapaLinhaCuidadoRequestDto,
  LinhaCuidadoDto,
  PainelLinhasCuidadoDto,
  ProgramaResumoDto,
  ReferenciaPrazoEtapa,
  SugestaoLinhaCuidadoDto,
  TIPOS_ETAPA,
  TipoEtapaLinhaCuidado,
} from '../../../core/models/programa';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { ProcedimentoReguladoResponseDto } from '../../../core/models/regulacao';
import { EspecialidadeDto } from '../../../core/models/saude-bucal';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { LaboratorioService } from '../../../core/services/laboratorio';
import { LinhaCuidadoService } from '../../../core/services/linha-cuidado';
import { RegulacaoService } from '../../../core/services/regulacao';
import { SaudeBucalService } from '../../../core/services/saude-bucal';
import { Drawer } from '../../../shared/drawer/drawer';
import { PercursoLinha } from './percurso-linha';

type Gaveta = { tipo: 'percurso'; id: string } | { tipo: 'linha'; linha: LinhaCuidadoDto | null } | { tipo: 'abrir'; sugestao: SugestaoLinhaCuidadoDto } | null;
type EtapaEdicao = EtapaLinhaCuidadoRequestDto & { chave: number };

/**
 * Linhas de cuidado na tela de Programas (ADR-0152): o painel dos percursos em curso (atrasados primeiro), as sugestões
 * de abrir a linha para quem está no programa dela, e o catálogo com o editor de etapas, que é da gestão.
 */
@Component({
  selector: 'app-linhas-de-cuidado',
  imports: [Drawer, PercursoLinha],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (podeAcompanhar()) {
      <div class="lc-cab">
        <h3 class="mo-grupo">Percursos em curso</h3>
        <div class="lc-filtros">
          <div class="dw-field"><label for="f-lc-linha">Linha</label>
            <select id="f-lc-linha" [value]="linhaFiltro()" (change)="linhaFiltro.set($any($event.target).value)">
              <option value="">Todas as linhas</option>
              @for (l of linhas(); track l.uuid) { <option [value]="l.uuid">{{ l.nome }}</option> }
            </select>
          </div>
          <label class="switch" for="f-lc-atrasados"><span><b style="font-weight: 500;">Só atrasados</b></span>
            <input type="checkbox" role="switch" id="f-lc-atrasados" [checked]="atrasados()" (change)="atrasados.set($any($event.target).checked)" /></label>
        </div>
      </div>
      @if (painel(); as pn) {
        <p class="cell-sub">{{ pn.emCurso }} em curso · {{ pn.atrasados }} atrasado(s)</p>
        <div class="tbl-wrap lc-tabela">
          <table aria-label="Percursos nas linhas de cuidado">
            <thead><tr><th scope="col">Paciente</th><th scope="col">Linha</th><th scope="col">Início</th><th scope="col">Etapa atual</th><th scope="col"><span class="sr-only">Abrir</span></th></tr></thead>
            <tbody>
              @for (p of pn.percursos; track p.uuid) {
                <tr>
                  <td>{{ p.pacienteNome }}</td>
                  <td>{{ p.linhaNome }}<span class="cell-sub">{{ p.unidadeNome }}</span></td>
                  <td class="mono">{{ data(p.inicio) }}</td>
                  <td>{{ p.etapaAtual ?? 'Todas feitas' }}@if (p.atrasado) { <span class="tag tag--alert">Atrasado</span> }</td>
                  <td><button type="button" class="btn btn--ghost btn--sm" (click)="gaveta.set({ tipo: 'percurso', id: p.uuid })" [attr.aria-label]="'Abrir percurso: ' + p.pacienteNome + ' ' + p.linhaNome">Abrir</button></td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhum percurso em curso.</td></tr> }
            </tbody>
          </table>
        </div>
        @if (pn.sugestoes.length) {
          <h3 class="mo-grupo" style="margin-top: var(--s-4);">Sugestões: no programa, sem a linha</h3>
          <ul class="eq-prof" aria-label="Sugestões de linha de cuidado">
            @for (s of pn.sugestoes; track s.acompanhamentoId + s.linhaId) {
              <li class="mo-row"><span><span class="cell-main">{{ s.pacienteNome }}</span><span class="cell-sub">{{ s.linhaNome }} · {{ s.unidadeNome }} · no programa desde {{ data(s.inscritoDesde) }}</span></span>
                <button type="button" class="link-btn" (click)="abrirSugestao(s)" [attr.aria-label]="'Abrir linha: ' + s.pacienteNome + ' ' + s.linhaNome">Abrir a linha</button></li>
            }
          </ul>
        }
      }
    }

    <div class="lc-cab" style="margin-top: var(--s-4);">
      <h3 class="mo-grupo">Catálogo de linhas</h3>
      @if (podeGerenciar()) { <button type="button" class="btn btn--sm" (click)="editar(null)">Nova linha</button> }
    </div>
    <ul class="eq-prof" aria-label="Linhas de cuidado">
      @for (l of linhas(); track l.uuid) {
        <li class="lc-linha">
          <div class="mo-row"><span><span class="cell-main">{{ l.nome }}@if (!l.ativa) { <span class="tag tag--muted">Inativa</span> }@if (l.sensivel) { <span class="tag tag--purple">Sensível</span> }</span>
            <span class="cell-sub">@if (l.programaNome) { {{ l.programaNome }} · }{{ l.etapas.length }} etapa(s) · {{ l.percursosEmCurso }} em curso</span></span>
            @if (podeGerenciar()) { <button type="button" class="link-btn" (click)="editar(l)" [attr.aria-label]="'Editar linha: ' + l.nome">Editar</button> }</div>
          <ol class="cell-sub lc-etapas">@for (e of l.etapas; track e.uuid) { <li>{{ e.nome }} — {{ e.criterio }}</li> }</ol>
        </li>
      }
    </ul>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g.tipo === 'percurso' ? 'Percurso na linha de cuidado' : g.tipo === 'abrir' ? 'Abrir linha de cuidado' : (g.linha ? 'Editar linha' : 'Nova linha de cuidado')"
        [larga]="g.tipo === 'linha'" (fechar)="gaveta.set(null)">
        @switch (g.tipo) {
          @case ('percurso') {
            <div class="dw-body"><app-percurso-linha [percursoId]="g.id" [podeAcompanhar]="podeAcompanhar()" (alterado)="recarregar()" /></div>
          }
          @case ('abrir') {
            <form class="dw-form" (submit)="$event.preventDefault(); abrir(g.sugestao)" novalidate>
              <div class="dw-body">
                @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
                <dl class="ro"><div><dt>Paciente</dt><dd>{{ g.sugestao.pacienteNome }}</dd></div><div><dt>Linha</dt><dd>{{ g.sugestao.linhaNome }}</dd></div>
                  <div><dt>Unidade</dt><dd>{{ g.sugestao.unidadeNome }}</dd></div></dl>
                <div class="dw-field"><label for="f-lc-inicio">Início (última menstruação, nascimento, suspeita)</label>
                  <input id="f-lc-inicio" type="date" [value]="inicio()" (input)="inicio.set($any($event.target).value)" /></div>
              </div>
              <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="!inicio()">Abrir</button></div>
            </form>
          }
          @case ('linha') {
            <form class="dw-form" (submit)="$event.preventDefault(); salvarLinha(g.linha)" novalidate>
              <div class="dw-body">
                @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
                <div class="dw-row">
                  <div class="dw-field"><label for="f-lc-codigo">Código</label><input id="f-lc-codigo" type="text" maxlength="60" [value]="codigo()" (input)="codigo.set($any($event.target).value)" /></div>
                  <div class="dw-field"><label for="f-lc-nome">Nome</label><input id="f-lc-nome" type="text" maxlength="200" [value]="nome()" (input)="nome.set($any($event.target).value)" /></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-lc-programa">Programa<span class="opt">opcional</span></label>
                    <select id="f-lc-programa" (change)="programaId.set($any($event.target).value)">
                      <option value="" [selected]="!programaId()">Nenhum</option>
                      @for (p of programas(); track p.uuid) { <option [value]="p.uuid" [selected]="p.uuid === programaId()">{{ p.nome }}</option> }
                    </select></div>
                  <label class="switch" for="f-lc-ativa"><span><b style="font-weight: 500;">Ativa</b></span>
                    <input type="checkbox" role="switch" id="f-lc-ativa" [checked]="ativa()" (change)="ativa.set($any($event.target).checked)" /></label>
                </div>
                <h4 class="mo-grupo">Etapas, na ordem</h4>
                @for (e of etapas(); track e.chave; let i = $index) {
                  <fieldset class="lc-etapa" [attr.aria-label]="'Etapa ' + (i + 1)">
                    <div class="dw-row">
                      <div class="dw-field"><label [for]="'f-et-nome-' + i">{{ i + 1 }}. Nome</label><input [id]="'f-et-nome-' + i" type="text" maxlength="200" [value]="e.nome" (input)="mudar(i, { nome: $any($event.target).value })" /></div>
                      <div class="dw-field"><label [for]="'f-et-tipo-' + i">Cumpre por</label>
                        <select [id]="'f-et-tipo-' + i" (change)="mudar(i, { tipo: $any($event.target).value })">
                          @for (t of listaTipos; track t) { <option [value]="t" [selected]="t === e.tipo">{{ tipos[t] }}</option> }
                        </select></div>
                    </div>
                    <div class="dw-row">
                      @switch (e.tipo) {
                        @case ('ATENDIMENTO') { <div class="dw-field"><label [for]="'f-et-ref-' + i">Especialidade<span class="opt">opcional</span></label><select [id]="'f-et-ref-' + i" (change)="mudar(i, { especialidadeId: $any($event.target).value || null })"><option value="">Qualquer</option>@for (x of especialidades(); track x.uuid) { <option [value]="x.uuid" [selected]="x.uuid === e.especialidadeId">{{ x.nome }}</option> }</select></div> }
                        @case ('EXAME') { <div class="dw-field"><label [for]="'f-et-ref-' + i">Exame</label><select [id]="'f-et-ref-' + i" (change)="mudar(i, { exameId: $any($event.target).value || null })"><option value="">Escolha…</option>@for (x of exames(); track x.uuid) { <option [value]="x.uuid" [selected]="x.uuid === e.exameId">{{ x.nome }}</option> }</select></div> }
                        @case ('REGULACAO') { <div class="dw-field"><label [for]="'f-et-ref-' + i">Procedimento regulado</label><select [id]="'f-et-ref-' + i" (change)="mudar(i, { procedimentoId: $any($event.target).value || null })"><option value="">Escolha…</option>@for (x of procedimentos(); track x.uuid) { <option [value]="x.uuid" [selected]="x.uuid === e.procedimentoId">{{ x.nome }}</option> }</select></div> }
                        @case ('VACINA') { <div class="dw-field"><label [for]="'f-et-ref-' + i">Vacina</label><select [id]="'f-et-ref-' + i" (change)="mudar(i, { imunobiologicoId: $any($event.target).value || null })"><option value="">Escolha…</option>@for (x of vacinas(); track x.uuid) { <option [value]="x.uuid" [selected]="x.uuid === e.imunobiologicoId">{{ x.sigla }}</option> }</select></div> }
                        @case ('INTERNACAO') { <div class="dw-field"><label [for]="'f-et-ref-' + i">CIDs aceitos<span class="opt">opcional</span></label><input [id]="'f-et-ref-' + i" type="text" maxlength="200" placeholder="O80, O81" [value]="e.cids ?? ''" (input)="mudar(i, { cids: $any($event.target).value })" /></div> }
                      }
                      <div class="dw-field"><label [for]="'f-et-qtd-' + i">Vezes</label><input [id]="'f-et-qtd-' + i" type="number" min="1" [value]="e.quantidade ?? 1" (input)="mudar(i, { quantidade: +$any($event.target).value || 1 })" /></div>
                      <div class="dw-field"><label [for]="'f-et-prazo-' + i">Prazo (dias)</label><input [id]="'f-et-prazo-' + i" type="number" min="0" [value]="e.prazoDias ?? ''" (input)="mudar(i, { prazoDias: $any($event.target).value === '' ? null : +$any($event.target).value })" /></div>
                      <div class="dw-field"><label [for]="'f-et-desde-' + i">Contado</label>
                        <select [id]="'f-et-desde-' + i" (change)="mudar(i, { prazoDesde: $any($event.target).value })" [disabled]="i === 0">
                          <option value="INICIO" [selected]="e.prazoDesde !== 'ETAPA_ANTERIOR'">do início</option><option value="ETAPA_ANTERIOR" [selected]="e.prazoDesde === 'ETAPA_ANTERIOR'">da etapa anterior</option>
                        </select></div>
                    </div>
                    <div class="row-actions">
                      <label><input type="checkbox" [checked]="e.obrigatoria !== false" (change)="mudar(i, { obrigatoria: $any($event.target).checked })" /> Obrigatória</label>
                      @if (i > 0) { <button type="button" class="link-btn" (click)="subir(i)" [attr.aria-label]="'Subir a etapa ' + (i + 1)">Subir</button> }
                      <button type="button" class="link-btn" (click)="remover(i)" [attr.aria-label]="'Remover a etapa ' + (i + 1)">Remover</button>
                    </div>
                  </fieldset>
                }
                <button type="button" class="btn btn--ghost btn--sm" (click)="adicionar()">Adicionar etapa</button>
              </div>
              <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn">Salvar</button></div>
            </form>
          }
        }
      </app-drawer>
    }
  `,
  styles: `
    .lc-cab { display: flex; justify-content: space-between; align-items: flex-end; gap: var(--s-3); flex-wrap: wrap; }
    .lc-filtros { display: flex; gap: var(--s-3); align-items: flex-end; flex-wrap: wrap; }
    .lc-filtros .dw-field { margin: 0; }
    .lc-tabela table { min-width: 0; width: 100%; }
    .lc-linha { display: block; padding: 8px 0; border-bottom: 1px solid var(--ink-100); }
    .lc-etapas { margin: 4px 0 0 18px; padding: 0; }
    .lc-etapa { border: 1px solid var(--ink-100); border-radius: 8px; padding: 8px 10px; margin: 0 0 8px; }
  `,
})
export class LinhasDeCuidado {
  private readonly service = inject(LinhaCuidadoService);
  private readonly saudeBucal = inject(SaudeBucalService);
  private readonly laboratorio = inject(LaboratorioService);
  private readonly regulacao = inject(RegulacaoService);
  private readonly imunizacao = inject(ImunizacaoService);

  readonly programas = input<ProgramaResumoDto[]>([]);
  readonly unidades = input<UnidadeSaudeResponseDto[]>([]);
  readonly unidadeId = input('');
  readonly podeGerenciar = input(false);
  readonly podeAcompanhar = input(false);

  protected readonly tipos = TIPOS_ETAPA;
  protected readonly listaTipos = Object.keys(TIPOS_ETAPA) as TipoEtapaLinhaCuidado[];
  protected readonly linhas = signal<LinhaCuidadoDto[]>([]);
  protected readonly painel = signal<PainelLinhasCuidadoDto | null>(null);
  protected readonly linhaFiltro = signal('');
  protected readonly atrasados = signal(false);
  protected readonly gaveta = signal<Gaveta>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly inicio = signal('');
  // O editor da linha.
  protected readonly codigo = signal('');
  protected readonly nome = signal('');
  protected readonly programaId = signal('');
  protected readonly ativa = signal(true);
  protected readonly etapas = signal<EtapaEdicao[]>([]);
  protected readonly especialidades = signal<EspecialidadeDto[]>([]);
  protected readonly exames = signal<ExameLaboratorialResponseDto[]>([]);
  protected readonly procedimentos = signal<ProcedimentoReguladoResponseDto[]>([]);
  protected readonly vacinas = signal<ImunobiologicoDto[]>([]);
  private chaves = 0;
  private readonly catalogosCarregados = computed(() => this.especialidades().length > 0);

  constructor() {
    this.carregarLinhas();
    effect(() => {
      const filtro = { linhaId: this.linhaFiltro() || undefined, unidadeId: this.unidadeId() || undefined, atrasados: this.atrasados() };
      const pode = this.podeAcompanhar();
      untracked(() => {
        if (pode) {
          this.service.painel(filtro).pipe(catchError(() => of(null))).subscribe((p) => this.painel.set(p));
        }
      });
    });
  }

  protected recarregar(): void {
    this.carregarLinhas();
    this.service.painel({ linhaId: this.linhaFiltro() || undefined, unidadeId: this.unidadeId() || undefined, atrasados: this.atrasados() })
      .pipe(catchError(() => of(null))).subscribe((p) => this.painel.set(p));
  }

  protected abrirSugestao(s: SugestaoLinhaCuidadoDto): void {
    this.erroGaveta.set(null);
    this.inicio.set(s.inscritoDesde);
    this.gaveta.set({ tipo: 'abrir', sugestao: s });
  }

  protected abrir(s: SugestaoLinhaCuidadoDto): void {
    this.service.abrir({ pacienteId: s.pacienteId, linhaId: s.linhaId, inicio: this.inicio(), unidadeId: s.unidadeId, equipeId: s.equipeId ?? null,
      acompanhamentoId: s.acompanhamentoId }).subscribe({
      next: (p) => {
        this.recarregar();
        this.gaveta.set({ tipo: 'percurso', id: p.uuid });
      },
      error: (e: HttpErrorResponse) => this.erroGaveta.set(mensagem(e)),
    });
  }

  protected editar(l: LinhaCuidadoDto | null): void {
    this.erroGaveta.set(null);
    this.codigo.set(l?.codigo ?? '');
    this.nome.set(l?.nome ?? '');
    this.programaId.set(l?.programaId ?? '');
    this.ativa.set(l?.ativa ?? true);
    this.etapas.set((l?.etapas ?? []).map((e) => ({ chave: ++this.chaves, uuid: e.uuid, nome: e.nome, tipo: e.tipo, especialidadeId: e.especialidadeId,
      exameId: e.exameId, procedimentoId: e.procedimentoId, imunobiologicoId: e.imunobiologicoId, cids: e.cids, quantidade: e.quantidade,
      prazoDias: e.prazoDias, prazoDesde: e.prazoDesde, obrigatoria: e.obrigatoria })));
    if (!l) this.adicionar();
    if (!this.catalogosCarregados()) {
      this.saudeBucal.especialidades().pipe(catchError(() => of([]))).subscribe((x) => this.especialidades.set(x.filter((e) => e.ativa)));
      this.laboratorio.listarExames().pipe(catchError(() => of([]))).subscribe((x) => this.exames.set(x));
      this.regulacao.listarProcedimentos().pipe(catchError(() => of([]))).subscribe((x) => this.procedimentos.set(x));
      this.imunizacao.imunobiologicos().pipe(catchError(() => of([]))).subscribe((x) => this.vacinas.set(x.filter((i) => i.ativo)));
    }
    this.gaveta.set({ tipo: 'linha', linha: l });
  }

  protected adicionar(): void {
    this.etapas.update((l) => [...l, { chave: ++this.chaves, nome: '', tipo: 'ATENDIMENTO', quantidade: 1, prazoDesde: 'INICIO' as ReferenciaPrazoEtapa, obrigatoria: true }]);
  }

  protected mudar(i: number, m: Partial<EtapaEdicao>): void {
    this.etapas.update((l) => l.map((e, k) => (k === i ? { ...e, ...m } : e)));
  }

  protected subir(i: number): void {
    this.etapas.update((l) => {
      const n = [...l];
      [n[i - 1], n[i]] = [n[i], n[i - 1]];
      return n;
    });
  }

  protected remover(i: number): void {
    this.etapas.update((l) => l.filter((_, k) => k !== i));
  }

  protected salvarLinha(l: LinhaCuidadoDto | null): void {
    if (!this.codigo().trim() || !this.nome().trim() || !this.etapas().length || this.etapas().some((e) => !e.nome.trim())) {
      this.erroGaveta.set('Informe o código, o nome e o nome de cada etapa.');
      return;
    }
    const dto = {
      codigo: this.codigo().trim(), nome: this.nome().trim(), programaId: this.programaId() || null, ativa: this.ativa(),
      etapas: this.etapas().map(({ chave, ...e }, i) => ({ ...e, prazoDesde: i === 0 ? 'INICIO' as ReferenciaPrazoEtapa : e.prazoDesde })),
    };
    (l ? this.service.atualizar(l.uuid, dto) : this.service.criar(dto)).subscribe({
      next: () => {
        this.gaveta.set(null);
        this.carregarLinhas();
      },
      error: (e: HttpErrorResponse) => this.erroGaveta.set(mensagem(e)),
    });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  private carregarLinhas(): void {
    this.service.linhas().subscribe({ next: (l) => this.linhas.set(l), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }
}

function mensagem(e: HttpErrorResponse): string {
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
