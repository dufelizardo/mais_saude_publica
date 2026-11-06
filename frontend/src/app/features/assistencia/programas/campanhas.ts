import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../../core/models/profissional';
import {
  CampanhaDto,
  ProgramaResumoDto,
  SITUACOES_CAMPANHA,
  SexoPublico,
  SituacaoCampanha,
  TIPOS_CAMPANHA,
  TipoCampanha,
} from '../../../core/models/programa';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { ImunobiologicoDto } from '../../../core/models/imunizacao';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { ProgramaService } from '../../../core/services/programa';
import { Drawer } from '../../../shared/drawer/drawer';
import { AcoesProgramaticas } from './acoes-programaticas';

type Gaveta = { tipo: 'campanha'; campanha: CampanhaDto | null } | { tipo: 'detalhe'; id: string };

/**
 * Campanhas (ADR-0133): a lista com a situação pelas datas e o andamento da meta; o cadastro (período, público, meta e
 * programa); e o detalhe com os locais (unidades da rede e temporários), a publicação na consulta pública, o cancelamento
 * e as ações da campanha.
 */
@Component({
  selector: 'app-campanhas',
  imports: [ReactiveFormsModule, Drawer, AcoesProgramaticas],
  template: `
    <div class="filters" style="margin-bottom: var(--s-3);">
      <select class="sel" aria-label="Situação das campanhas" [value]="situacao()" (change)="situacao.set($any($event.target).value); carregar()">
        <option value="">Todas as situações</option>
        @for (s of listaSituacoes; track s) { <option [value]="s">{{ situacoes[s].rotulo }}</option> }
      </select>
      @if (podeGerenciar()) { <button type="button" class="btn btn--sm" style="margin-left: auto;" (click)="abrirCadastro(null)">Nova campanha</button> }
    </div>
    @if (campanhas(); as lista) {
      @if (lista.length) {
        <div class="tbl-wrap">
          <table aria-label="Campanhas">
            <thead><tr><th scope="col">Campanha</th><th scope="col">Período</th><th scope="col">Público</th><th scope="col">Meta</th><th scope="col">Situação</th><th scope="col" class="num">Ações</th></tr></thead>
            <tbody>
              @for (c of lista; track c.uuid) {
                <tr>
                  <td><span class="cell-main">{{ c.nome }}</span><span class="cell-sub">{{ tipos[c.tipo] }}@if (c.programaNome) { · {{ c.programaNome }} }</span></td>
                  <td class="mono">{{ data(c.inicio) }} a {{ data(c.fim) }}</td>
                  <td>{{ c.publico }}</td>
                  <td>@if (c.metaQuantidade) { <span class="mono">{{ c.realizado }} / {{ c.metaQuantidade }}</span> } @else { {{ c.metaDescricao ?? '—' }} }</td>
                  <td><span [class]="'tag tag--' + situacoes[c.situacao].classe">{{ situacoes[c.situacao].rotulo }}</span>@if (c.publicada) { <span class="tag tag--info">Publicada</span> }</td>
                  <td class="num"><button type="button" class="btn btn--ghost btn--sm" (click)="abrirDetalhe(c.uuid)" [attr.aria-label]="'Abrir campanha: ' + c.nome">Abrir</button></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else {
        <p class="cell-sub">Nenhuma campanha.</p>
      }
    } @else {
      <p class="cell-sub">Carregando as campanhas…</p>
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g.tipo === 'campanha' ? (g.campanha ? 'Editar campanha' : 'Nova campanha') : (detalhe()?.nome ?? 'Campanha')"
        [larga]="g.tipo === 'detalhe'" (fechar)="gaveta.set(null)">
        @if (g.tipo === 'campanha') {
          <form class="dw-form" [formGroup]="form" (ngSubmit)="salvarCampanha(g.campanha)" novalidate>
            <div class="dw-body">
              @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
              <div class="dw-field">
                <label for="f-campanha-nome">Nome</label>
                <input id="f-campanha-nome" type="text" maxlength="200" formControlName="nome" placeholder="Ex.: Vacinação contra a gripe 2026" />
              </div>
              <div class="dw-row">
                <div class="dw-field">
                  <label for="f-campanha-tipo">Tipo</label>
                  <select id="f-campanha-tipo" formControlName="tipo">
                    @for (t of listaTipos; track t) { <option [value]="t">{{ tipos[t] }}</option> }
                  </select>
                </div>
                <div class="dw-field">
                  <label for="f-campanha-programa">Programa<span class="opt">opcional</span></label>
                  <select id="f-campanha-programa" formControlName="programaId">
                    <option value="">Nenhum</option>
                    @for (p of programas(); track p.uuid) { <option [value]="p.uuid">{{ p.nome }}</option> }
                  </select>
                </div>
              </div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-campanha-inicio">Início</label><input id="f-campanha-inicio" type="date" formControlName="inicio" /></div>
                <div class="dw-field"><label for="f-campanha-fim">Fim</label><input id="f-campanha-fim" type="date" formControlName="fim" /></div>
              </div>
              <div class="dw-field">
                <label for="f-campanha-descricao">Descrição<span class="opt">aparece na consulta pública</span></label>
                <textarea id="f-campanha-descricao" rows="2" maxlength="2000" formControlName="descricao"></textarea>
              </div>
              <h4 class="mo-grupo">Público</h4>
              <div class="dw-row">
                <div class="dw-field"><label for="f-campanha-idade-min">Idade mínima</label><input id="f-campanha-idade-min" type="number" min="0" formControlName="idadeMinima" /></div>
                <div class="dw-field"><label for="f-campanha-idade-max">Idade máxima</label><input id="f-campanha-idade-max" type="number" min="0" formControlName="idadeMaxima" /></div>
                <div class="dw-field">
                  <label for="f-campanha-sexo">Sexo</label>
                  <select id="f-campanha-sexo" formControlName="sexo">
                    <option value="">Qualquer</option><option value="FEMININO">Feminino</option><option value="MASCULINO">Masculino</option>
                  </select>
                </div>
              </div>
              <div class="dw-field">
                <label for="f-campanha-publico">Público em palavras<span class="opt">opcional, substitui o texto montado</span></label>
                <input id="f-campanha-publico" type="text" maxlength="300" formControlName="publicoDescricao" placeholder="Ex.: crianças de 6 meses a 5 anos e gestantes" />
              </div>
              <h4 class="mo-grupo">Meta</h4>
              <div class="dw-row">
                <div class="dw-field"><label for="f-campanha-meta">Meta</label><input id="f-campanha-meta" type="text" maxlength="300" formControlName="metaDescricao" placeholder="Ex.: 90% de cobertura" /></div>
                <div class="dw-field"><label for="f-campanha-meta-qtd">Pessoas<span class="opt">opcional</span></label><input id="f-campanha-meta-qtd" type="number" min="1" formControlName="metaQuantidade" /></div>
              </div>
              @if (form.controls.tipo.value === 'VACINACAO') {
                <h4 class="mo-grupo">Vacinas da campanha</h4>
                <p class="dw-hint">O andamento conta as pessoas vacinadas com a campanha na dose. Sem vacina marcada, vale qualquer uma; com elas, a dose
                  de outra vacina não entra na campanha.</p>
                <div class="camp-vacinas" role="group" aria-label="Vacinas da campanha">
                  @for (i of imunobiologicos(); track i.uuid) {
                    <label><input type="checkbox" [checked]="vacinas().has(i.uuid)" (change)="alternarVacina(i.uuid)" /> {{ i.sigla }}</label>
                  }
                </div>
              }
            </div>
            <footer class="dw-foot">
              <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button>
              <button type="submit" class="btn" [disabled]="salvando()">{{ salvando() ? 'Salvando…' : 'Salvar campanha' }}</button>
            </footer>
          </form>
        } @else {
          <div class="dw-form">
            <div class="dw-body">
              @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
              @if (detalhe(); as c) {
                <dl class="ro">
                  <div><dt>Situação</dt><dd><span [class]="'tag tag--' + situacoes[c.situacao].classe">{{ situacoes[c.situacao].rotulo }}</span>@if (c.publicada) { <span class="tag tag--info">Publicada</span> }</dd></div>
                  <div><dt>Período</dt><dd>{{ data(c.inicio) }} a {{ data(c.fim) }} · {{ tipos[c.tipo] }}@if (c.programaNome) { · {{ c.programaNome }} }</dd></div>
                  <div><dt>Público</dt><dd>{{ c.publico }}</dd></div>
                  @if (c.descricao) { <div><dt>Descrição</dt><dd>{{ c.descricao }}</dd></div> }
                  @if (c.tipo === 'VACINACAO') { <div><dt>Vacinas</dt><dd>{{ c.imunobiologicoSiglas.length ? c.imunobiologicoSiglas.join(', ') : 'Qualquer vacina' }}</dd></div> }
                  @if (c.motivoCancelamento) { <div><dt>Cancelada</dt><dd>{{ c.motivoCancelamento }}</dd></div> }
                </dl>
                @if (c.metaQuantidade || c.metaDescricao) {
                  <h4 class="mo-grupo">Meta</h4>
                  <p class="cell-sub" aria-label="Andamento da meta">{{ c.metaDescricao ?? '' }}@if (c.metaQuantidade) { · {{ c.realizado }} de {{ c.metaQuantidade }} pessoas ({{ percentual(c) }}%) }</p>
                  @if (c.metaQuantidade) {
                    <div style="height: 8px; background: var(--ink-100); border-radius: 4px; overflow: hidden;" role="progressbar" [attr.aria-valuenow]="percentual(c)" aria-valuemin="0" aria-valuemax="100" aria-label="Meta da campanha">
                      <div [style.width.%]="percentual(c)" style="height: 100%; background: var(--ok);"></div>
                    </div>
                  }
                }
                @if (podeGerenciar() && c.situacao !== 'CANCELADA') {
                  <div class="row-actions" style="justify-content: flex-start; flex-wrap: wrap; margin: var(--s-3) 0;">
                    <button type="button" class="btn btn--ghost btn--sm" (click)="abrirCadastro(c)">Editar</button>
                    <button type="button" class="btn btn--ghost btn--sm" (click)="publicar(c, !c.publicada)">{{ c.publicada ? 'Retirar da consulta pública' : 'Publicar na consulta pública' }}</button>
                    <button type="button" class="btn btn--ghost btn--sm" (click)="cancelando.set(!cancelando())">Cancelar campanha…</button>
                  </div>
                  @if (cancelando()) {
                    <div class="dw-field">
                      <label for="f-campanha-motivo">Motivo do cancelamento</label>
                      <input id="f-campanha-motivo" type="text" maxlength="500" [value]="motivo()" (input)="motivo.set($any($event.target).value)" />
                    </div>
                    <button type="button" class="btn btn--sm" (click)="cancelar(c)" [disabled]="!motivo().trim()">Confirmar o cancelamento</button>
                  }
                }

                <h4 class="mo-grupo">Locais <span class="cell-sub" style="display: inline;">· {{ c.locais.length }}</span></h4>
                @if (c.locais.length) {
                  <ul class="eq-prof" aria-label="Locais da campanha">
                    @for (l of c.locais; track l.uuid) {
                      <li class="mo-row">
                        <span>
                          <span class="cell-main">{{ l.nome }}@if (l.temporario) { <span class="tag tag--purple">Temporário</span> }</span>
                          <span class="cell-sub">@if (l.endereco) { {{ l.endereco }} · }@if (l.funcionamento) { {{ l.funcionamento }} · }@if (l.temporario) { responsável: {{ l.unidadeNome }} }</span>
                        </span>
                        @if (podeGerenciar() && c.situacao !== 'CANCELADA') {
                          <button type="button" class="link-btn" (click)="removerLocal(l.uuid)" [attr.aria-label]="'Retirar local: ' + l.nome">Retirar</button>
                        }
                      </li>
                    }
                  </ul>
                } @else { <p class="cell-sub">Nenhum local ainda.</p> }
                @if (podeGerenciar() && c.situacao !== 'CANCELADA') {
                  <form [formGroup]="localForm" (ngSubmit)="incluirLocal(c)" novalidate aria-label="Novo local" style="margin-top: var(--s-2);">
                    <div class="dw-row">
                      <div class="dw-field">
                        <label for="f-local-tipo">Local</label>
                        <select id="f-local-tipo" formControlName="temporario">
                          <option value="nao">Unidade da rede</option>
                          <option value="sim">Local temporário (escola, praça, unidade móvel)</option>
                        </select>
                      </div>
                      <div class="dw-field">
                        <label for="f-local-unidade">{{ localForm.controls.temporario.value === 'sim' ? 'Unidade responsável' : 'Unidade' }}</label>
                        <select id="f-local-unidade" formControlName="unidadeId">
                          <option value="">Escolha…</option>
                          @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                        </select>
                      </div>
                    </div>
                    @if (localForm.controls.temporario.value === 'sim') {
                      <div class="dw-row">
                        <div class="dw-field"><label for="f-local-nome">Nome do local</label><input id="f-local-nome" type="text" maxlength="200" formControlName="nome" /></div>
                        <div class="dw-field"><label for="f-local-endereco">Endereço</label><input id="f-local-endereco" type="text" maxlength="500" formControlName="endereco" /></div>
                      </div>
                    }
                    <div class="dw-field"><label for="f-local-funcionamento">Funcionamento<span class="opt">opcional</span></label><input id="f-local-funcionamento" type="text" maxlength="200" formControlName="funcionamento" placeholder="Ex.: seg a sex, 8h às 17h" /></div>
                    <button type="submit" class="btn btn--ghost btn--sm">Incluir local</button>
                  </form>
                }

                <h4 class="mo-grupo">Ações da campanha</h4>
                <app-acoes-programaticas [campanhaFixa]="c" [unidades]="unidades()" [podeAcompanhar]="podeAcompanhar()" (alterada)="recarregar(c.uuid)" />
              } @else if (!erro()) {
                <p class="cell-sub">Carregando a campanha…</p>
              }
            </div>
            <footer class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Fechar</button></footer>
          </div>
        }
      </app-drawer>
    }
  `,
  styles: `
    .camp-vacinas { display: flex; flex-wrap: wrap; gap: 6px 14px; }
    .camp-vacinas label { display: flex; align-items: center; gap: 4px; font-size: 0.85rem; }
  `,
})
export class Campanhas {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProgramaService);
  private readonly imunizacaoService = inject(ImunizacaoService);

  readonly programas = input<ProgramaResumoDto[]>([]);
  readonly unidades = input<UnidadeSaudeResponseDto[]>([]);
  readonly podeGerenciar = input(false);
  readonly podeAcompanhar = input(false);

  protected readonly tipos = TIPOS_CAMPANHA;
  protected readonly listaTipos = Object.keys(TIPOS_CAMPANHA) as TipoCampanha[];
  protected readonly situacoes = SITUACOES_CAMPANHA;
  protected readonly listaSituacoes = Object.keys(SITUACOES_CAMPANHA) as SituacaoCampanha[];

  protected readonly situacao = signal('');
  protected readonly campanhas = signal<CampanhaDto[] | null>(null);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly detalhe = signal<CampanhaDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly salvando = signal(false);
  protected readonly cancelando = signal(false);
  protected readonly motivo = signal('');
  protected readonly form = this.fb.nonNullable.group({
    nome: '', tipo: 'VACINACAO', programaId: '', inicio: '', fim: '', descricao: '', idadeMinima: '', idadeMaxima: '', sexo: '',
    publicoDescricao: '', metaDescricao: '', metaQuantidade: '',
  });
  protected readonly localForm = this.fb.nonNullable.group({ temporario: 'nao', unidadeId: '', nome: '', endereco: '', funcionamento: '' });
  /** As vacinas da campanha de vacinação (ADR-0149). */
  protected readonly imunobiologicos = signal<ImunobiologicoDto[]>([]);
  protected readonly vacinas = signal<Set<string>>(new Set());
  protected readonly programasAtivos = computed(() => this.programas().filter((p) => p.situacao === 'ATIVO'));

  constructor() {
    this.carregar();
  }

  protected carregar(): void {
    this.service.campanhas({ situacao: this.situacao() || undefined }).pipe(catchError(() => of([]))).subscribe((c) => this.campanhas.set(c));
  }

  protected data(iso?: string | null): string {
    if (!iso) return '—';
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected percentual(c: CampanhaDto): number {
    return c.metaQuantidade ? Math.min(100, Math.round((c.realizado / c.metaQuantidade) * 100)) : 0;
  }

  protected abrirCadastro(c: CampanhaDto | null): void {
    this.form.reset({
      nome: c?.nome ?? '', tipo: c?.tipo ?? 'VACINACAO', programaId: c?.programaId ?? '', inicio: c?.inicio ?? '', fim: c?.fim ?? '',
      descricao: c?.descricao ?? '', idadeMinima: c?.idadeMinima != null ? String(c.idadeMinima) : '', idadeMaxima: c?.idadeMaxima != null ? String(c.idadeMaxima) : '',
      sexo: c?.sexo ?? '', publicoDescricao: c?.publicoDescricao ?? '', metaDescricao: c?.metaDescricao ?? '',
      metaQuantidade: c?.metaQuantidade != null ? String(c.metaQuantidade) : '',
    });
    this.vacinas.set(new Set(c?.imunobiologicoIds ?? []));
    if (!this.imunobiologicos().length) {
      this.imunizacaoService.imunobiologicos().pipe(catchError(() => of([])))
        .subscribe((l) => this.imunobiologicos.set(l.filter((i) => i.ativo).sort((a, b) => a.sigla.localeCompare(b.sigla))));
    }
    this.erro.set(null);
    this.gaveta.set({ tipo: 'campanha', campanha: c });
  }

  protected alternarVacina(id: string): void {
    this.vacinas.update((s) => {
      const n = new Set(s);
      if (!n.delete(id)) n.add(id);
      return n;
    });
  }

  protected abrirDetalhe(id: string): void {
    this.detalhe.set(null);
    this.erro.set(null);
    this.cancelando.set(false);
    this.localForm.reset({ temporario: 'nao', unidadeId: '', nome: '', endereco: '', funcionamento: '' });
    this.gaveta.set({ tipo: 'detalhe', id });
    this.recarregar(id);
  }

  protected recarregar(id: string): void {
    this.service.campanha(id).subscribe({ next: (c) => this.detalhe.set(c), error: (e: HttpErrorResponse) => this.falhou(e) });
    this.carregar();
  }

  protected salvarCampanha(atual: CampanhaDto | null): void {
    const v = this.form.getRawValue();
    if (!v.nome.trim() || !v.inicio || !v.fim) {
      this.erro.set('Informe o nome, o início e o fim.');
      return;
    }
    const numero = (x: string) => (String(x ?? '').trim() === '' ? null : Number(x));
    const dto = {
      nome: v.nome.trim(), tipo: v.tipo as TipoCampanha, programaId: v.programaId || null, inicio: v.inicio, fim: v.fim, descricao: v.descricao.trim() || null,
      idadeMinima: numero(v.idadeMinima), idadeMaxima: numero(v.idadeMaxima), sexo: (v.sexo || null) as SexoPublico | null,
      publicoDescricao: v.publicoDescricao.trim() || null, metaDescricao: v.metaDescricao.trim() || null, metaQuantidade: numero(v.metaQuantidade),
      imunobiologicoIds: v.tipo === 'VACINACAO' ? [...this.vacinas()] : [],
    };
    this.salvando.set(true);
    (atual ? this.service.atualizarCampanha(atual.uuid, dto) : this.service.criarCampanha(dto)).subscribe({
      next: (c) => {
        this.salvando.set(false);
        this.carregar();
        this.abrirDetalhe(c.uuid);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected publicar(c: CampanhaDto, publicada: boolean): void {
    this.service.publicarCampanha(c.uuid, publicada).subscribe({ next: (d) => this.atualizar(d), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected cancelar(c: CampanhaDto): void {
    this.service.cancelarCampanha(c.uuid, this.motivo().trim()).subscribe({
      next: (d) => {
        this.cancelando.set(false);
        this.motivo.set('');
        this.atualizar(d);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected incluirLocal(c: CampanhaDto): void {
    const v = this.localForm.getRawValue();
    const temporario = v.temporario === 'sim';
    if (!v.unidadeId || (temporario && !v.nome.trim())) {
      this.erro.set(temporario ? 'O local temporário pede o nome e a unidade responsável.' : 'Escolha a unidade.');
      return;
    }
    this.service.incluirLocalCampanha(c.uuid, {
      unidadeId: v.unidadeId, temporario, nome: temporario ? v.nome.trim() : null, endereco: temporario ? v.endereco.trim() || null : null,
      funcionamento: v.funcionamento.trim() || null,
    }).subscribe({
      next: (d) => {
        this.localForm.reset({ temporario: 'nao', unidadeId: '', nome: '', endereco: '', funcionamento: '' });
        this.atualizar(d);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected removerLocal(localId: string): void {
    this.service.removerLocalCampanha(localId).subscribe({ next: (d) => this.atualizar(d), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  private atualizar(c: CampanhaDto): void {
    this.erro.set(null);
    this.detalhe.set(c);
    this.carregar();
  }

  private falhou(e: HttpErrorResponse): void {
    this.salvando.set(false);
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir. Tente novamente.');
  }
}
