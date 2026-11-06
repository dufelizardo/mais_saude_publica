import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import {
  InstituicaoExternaDto,
  SETORES_INTERSETORIAIS,
  SetorIntersetorial,
  TIPOS_INSTITUICAO,
  TipoInstituicaoExterna,
} from '../../../core/models/intersetorialidade';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AuthService } from '../../../core/services/auth';
import { IntersetorialidadeService } from '../../../core/services/intersetorialidade';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta = { instituicao: InstituicaoExternaDto | null } | null;

/**
 * Instituições da rede intersetorial (ADR-0154): o catálogo único de instituições de outros setores (CRAS, CREAS,
 * escolas, Conselho Tutelar...) para onde os domínios encaminham. Ler é de quem está logado; manter, de
 * INTERSETORIAL.GERENCIAR. O sistema não implementa esses setores.
 */
@Component({
  selector: 'app-instituicoes-externas',
  imports: [Drawer],
  template: `
    <header class="page-head">
      <div>
        <h1>Instituições da rede intersetorial</h1>
        <p>Assistência social, educação, garantia de direitos e outros setores · para onde os casos são encaminhados</p>
      </div>
      <div class="page-actions">
        @if (podeGerenciar()) { <button type="button" class="btn" (click)="abrir(null)">Nova instituição</button> }
      </div>
    </header>

    <div class="toolbar" role="region" aria-label="Filtros das instituições">
      <label class="search search--icon">
        <span class="sr-only">Buscar instituição</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.3-4.3"></path></svg>
        <input type="search" placeholder="Buscar por nome ou responsável…" [value]="busca()" (input)="busca.set($any($event.target).value)" />
      </label>
      <label class="pill pill--select">
        <span class="sr-only">Setor</span>
        <select (change)="setor.set($any($event.target).value)" aria-label="Setor">
          <option value="" [selected]="!setor()">Todos os setores</option>
          @for (s of setores; track s) { <option [value]="s" [selected]="setor() === s">{{ rotuloSetor[s] }}</option> }
        </select>
      </label>
      <div class="seg" role="group" aria-label="Situação">
        <button type="button" [attr.aria-pressed]="!inativas()" (click)="inativas.set(false)">Ativas</button>
        <button type="button" [attr.aria-pressed]="inativas()" (click)="inativas.set(true)">Com as inativas</button>
      </div>
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    <div class="tbl-wrap card">
      <table aria-label="Instituições externas">
        <thead><tr><th scope="col">Instituição</th><th scope="col">Setor</th><th scope="col">Contato</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
        <tbody>
          @for (i of visiveis(); track i.uuid) {
            <tr>
              <td>
                <span class="cell-main">{{ i.nome }}@if (!i.ativa) { <span class="tag tag--muted">Inativa</span> }</span>
                <span class="cell-sub">{{ tipos[i.tipo].rotulo }}@if (i.endereco) { · {{ i.endereco }} }</span>
              </td>
              <td>{{ rotuloSetor[i.setor] }}</td>
              <td>
                <span class="cell-main">{{ i.responsavel ?? '—' }}</span>
                <span class="cell-sub">@if (i.telefone) { {{ i.telefone }} }@if (i.telefone && i.email) { · }@if (i.email) { {{ i.email }} }</span>
              </td>
              <td class="row-actions">
                @if (podeGerenciar()) { <button type="button" class="link-btn" (click)="abrir(i)" [attr.aria-label]="'Editar ' + i.nome">Editar</button> }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="4" class="cell-sub">{{ carregando() ? 'Carregando…' : 'Nenhuma instituição no catálogo com esses filtros.' }}</td></tr>
          }
        </tbody>
      </table>
    </div>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g.instituicao ? 'Editar instituição' : 'Nova instituição'" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <div class="dw-field">
              <label for="f-ie-nome">Nome <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
              <input id="f-ie-nome" type="text" maxlength="200" [value]="nome()" (input)="nome.set($any($event.target).value)" placeholder="Ex.: CRAS Centro" />
            </div>
            <div class="dw-row">
              <div class="dw-field">
                <label for="f-ie-setor">Setor <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <select id="f-ie-setor" (change)="escolherSetor($any($event.target).value)">
                  <option value="" [selected]="!setorForm()">Escolha…</option>
                  @for (s of setores; track s) { <option [value]="s" [selected]="setorForm() === s">{{ rotuloSetor[s] }}</option> }
                </select>
              </div>
              <div class="dw-field">
                <label for="f-ie-tipo">Tipo <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <select id="f-ie-tipo" (change)="tipo.set($any($event.target).value)">
                  <option value="" [selected]="!tipo()">Escolha…</option>
                  @for (t of tiposDoSetor(); track t) { <option [value]="t" [selected]="tipo() === t">{{ tipos[t].rotulo }}</option> }
                </select>
              </div>
            </div>
            <div class="dw-field">
              <label for="f-ie-endereco">Endereço<span class="opt">opcional</span></label>
              <input id="f-ie-endereco" type="text" maxlength="300" [value]="endereco()" (input)="endereco.set($any($event.target).value)" />
            </div>
            <div class="dw-row">
              <div class="dw-field">
                <label for="f-ie-telefone">Telefone<span class="opt">opcional</span></label>
                <input id="f-ie-telefone" type="tel" maxlength="30" [value]="telefone()" (input)="telefone.set($any($event.target).value)" />
              </div>
              <div class="dw-field">
                <label for="f-ie-email">E-mail<span class="opt">opcional</span></label>
                <input id="f-ie-email" type="email" maxlength="120" [value]="email()" (input)="email.set($any($event.target).value)" />
              </div>
            </div>
            <div class="dw-field">
              <label for="f-ie-responsavel">Pessoa de contato<span class="opt">opcional</span></label>
              <input id="f-ie-responsavel" type="text" maxlength="120" [value]="responsavel()" (input)="responsavel.set($any($event.target).value)" />
            </div>
            <div class="dw-field">
              <label for="f-ie-observacao">Observação<span class="opt">opcional</span></label>
              <input id="f-ie-observacao" type="text" maxlength="500" [value]="observacao()" (input)="observacao.set($any($event.target).value)" placeholder="Ex.: atende os bairros do centro" />
            </div>
            @if (g.instituicao) {
              <div class="dw-field">
                <label class="switch" for="f-ie-ativa"><span><b style="font-weight: 500;">Ativa no catálogo</b><span class="cell-sub">Inativa não recebe encaminhamento novo; os já feitos continuam.</span></span><input type="checkbox" role="switch" id="f-ie-ativa" [checked]="ativa()" (change)="ativa.set($any($event.target).checked)" /></label>
              </div>
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar' }}</button></div>
        </form>
      </app-drawer>
    }
  `,
})
export class InstituicoesExternas {
  private readonly service = inject(IntersetorialidadeService);
  private readonly authService = inject(AuthService);

  protected readonly setores = Object.keys(SETORES_INTERSETORIAIS) as SetorIntersetorial[];
  protected readonly rotuloSetor = SETORES_INTERSETORIAIS;
  protected readonly tipos = TIPOS_INSTITUICAO;

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly podeGerenciar = computed(() => !this.acesso().restrito || this.acesso().permissoes.has('INTERSETORIAL.GERENCIAR'));

  protected readonly lista = signal<InstituicaoExternaDto[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly busca = signal('');
  protected readonly setor = signal<SetorIntersetorial | ''>('');
  protected readonly inativas = signal(false);
  protected readonly visiveis = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    return this.lista()
      .filter((i) => !this.setor() || i.setor === this.setor())
      .filter((i) => this.inativas() || i.ativa)
      .filter((i) => !termo || i.nome.toLowerCase().includes(termo) || (i.responsavel ?? '').toLowerCase().includes(termo));
  });

  protected readonly gaveta = signal<Gaveta>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly nome = signal('');
  protected readonly setorForm = signal<SetorIntersetorial | ''>('');
  protected readonly tipo = signal<TipoInstituicaoExterna | ''>('');
  protected readonly endereco = signal('');
  protected readonly telefone = signal('');
  protected readonly email = signal('');
  protected readonly responsavel = signal('');
  protected readonly observacao = signal('');
  protected readonly ativa = signal(true);
  /** Os tipos do setor escolhido, mais OUTRO, que vale para qualquer setor. */
  protected readonly tiposDoSetor = computed(() => {
    const s = this.setorForm();
    return (Object.keys(TIPOS_INSTITUICAO) as TipoInstituicaoExterna[]).filter((t) => !s || TIPOS_INSTITUICAO[t].setor === s || TIPOS_INSTITUICAO[t].setor === null);
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.carregar();
  }

  protected escolherSetor(s: SetorIntersetorial | ''): void {
    this.setorForm.set(s);
    const t = this.tipo();
    if (t && TIPOS_INSTITUICAO[t].setor !== null && TIPOS_INSTITUICAO[t].setor !== s) this.tipo.set('');
  }

  protected abrir(i: InstituicaoExternaDto | null): void {
    this.erroGaveta.set(null);
    this.nome.set(i?.nome ?? '');
    this.setorForm.set(i?.setor ?? '');
    this.tipo.set(i?.tipo ?? '');
    this.endereco.set(i?.endereco ?? '');
    this.telefone.set(i?.telefone ?? '');
    this.email.set(i?.email ?? '');
    this.responsavel.set(i?.responsavel ?? '');
    this.observacao.set(i?.observacao ?? '');
    this.ativa.set(i?.ativa ?? true);
    this.gaveta.set({ instituicao: i });
  }

  protected salvar(g: Exclude<Gaveta, null>): void {
    if (this.enviando()) return;
    const setor = this.setorForm();
    const tipo = this.tipo();
    if (!this.nome().trim() || !setor || !tipo) {
      this.erroGaveta.set('Informe o nome, o setor e o tipo.');
      return;
    }
    const dto = {
      nome: this.nome().trim(), setor, tipo, endereco: this.endereco().trim() || null, telefone: this.telefone().trim() || null,
      email: this.email().trim() || null, responsavel: this.responsavel().trim() || null, observacao: this.observacao().trim() || null,
      ativa: g.instituicao ? this.ativa() : null,
    };
    this.enviando.set(true);
    (g.instituicao ? this.service.atualizar(g.instituicao.uuid, dto) : this.service.criar(dto)).subscribe({
      next: () => {
        this.enviando.set(false);
        this.gaveta.set(null);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(e));
      },
    });
  }

  private carregar(): void {
    this.carregando.set(true);
    this.service.listar().pipe(catchError((e: HttpErrorResponse) => {
      this.erro.set(mensagem(e));
      return of([] as InstituicaoExternaDto[]);
    })).subscribe((l) => {
      this.lista.set(l);
      this.carregando.set(false);
    });
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
