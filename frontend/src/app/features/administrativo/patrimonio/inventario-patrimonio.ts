import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ACOES_INVENTARIO,
  CONSERVACOES,
  ConservacaoBem,
  InventarioDto,
  ItemInventarioDto,
  RESULTADOS_CONFERENCIA,
  ResultadoConferencia,
} from '../../../core/models/patrimonio';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { SetorResponseDto } from '../../../core/models/setor';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { PatrimonioService } from '../../../core/services/patrimonio';
import { Drawer } from '../../../shared/drawer/drawer';

/**
 * Inventário do Patrimônio, fatia PM3 (ADR-0217): a lista, a abertura com a comissão e a gaveta do inventário com a conferência item a
 * item, os achados a mais e o fechamento. Quem não tem permissão de Patrimônio e é da comissão vê os próprios inventários ({@code meus}).
 */
@Component({
  selector: 'app-inventario-patrimonio',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (aviso()) { <div class="box box--info" role="status"><strong>{{ aviso() }}</strong></div> }

    @if (podeGerenciar) {
      <div class="iv-cab"><button type="button" class="btn" (click)="novo()">Abrir inventário</button></div>
    }
    <div class="tbl-wrap card">
      <table class="iv-tabela" aria-label="Inventários">
        <thead><tr><th scope="col">Inventário</th><th scope="col">Unidade</th><th scope="col">Conferidos</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
        <tbody>
          @for (i of lista(); track i.uuid) {
            <tr>
              <td><span class="cell-main mono">{{ i.numero }}</span><span class="cell-sub">{{ data(i.abertoEm) }}@if (i.fechadoEm) { – {{ data(i.fechadoEm) }} }</span></td>
              <td>{{ i.unidadeNome }}</td>
              <td>{{ i.conferidos }} de {{ i.total }}</td>
              <td><span [class]="'tag tag--' + (i.situacao === 'ABERTO' ? 'warn' : 'ok')">{{ i.situacao === 'ABERTO' ? 'Aberto' : 'Fechado' }}</span></td>
              <td class="row-actions"><button type="button" class="link-btn" (click)="abrir(i.uuid)" [attr.aria-label]="'Abrir inventário ' + i.numero">Abrir</button></td>
            </tr>
          } @empty { <tr><td colspan="5" class="cell-sub">Nenhum inventário.</td></tr> }
        </tbody>
      </table>
    </div>

    @if (abrindo()) {
      <app-drawer titulo="Abrir inventário" (fechar)="abrindo.set(false)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <p class="cell-sub">Os bens tombados da unidade entram na lista. Enquanto o inventário estiver aberto, eles não são transferidos.</p>
            <div class="dw-field"><label for="f-iv-unidade">Unidade <span class="req" aria-hidden="true">*</span></label>
              <select id="f-iv-unidade" (change)="set('unidadeId', $event)">
                <option value="">Escolha…</option>
                @for (u of unidades; track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
              </select></div>
            <div class="dw-row">
              @for (k of [1, 2, 3]; track k) {
                <div class="dw-field"><label [for]="'f-iv-membro-' + k">Matrícula do membro {{ k }} <span class="req" aria-hidden="true">*</span></label><input [id]="'f-iv-membro-' + k" type="text" maxlength="30" (input)="set('m' + k, $event)" /></div>
              }
            </div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="abrindo.set(false)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Abrir</button></div>
        </form>
      </app-drawer>
    }

    @if (atual(); as inv) {
      <app-drawer [titulo]="'Inventário ' + inv.numero" [larga]="true" (fechar)="atual.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Unidade</dt><dd>{{ inv.unidadeNome }}</dd></div>
            <div><dt>Situação</dt><dd>{{ inv.situacao === 'ABERTO' ? 'Aberto' : 'Fechado em ' + data(inv.fechadoEm!) }}</dd></div>
            <div><dt>Comissão</dt><dd>@for (m of inv.membros; track m.matricula; let ultimo = $last) { {{ m.nome }}@if (!ultimo) {, } }</dd></div>
            <div><dt>Conferência</dt><dd>{{ inv.conferidos }} de {{ inv.total }} · {{ inv.naoEncontrados }} não encontrado(s) · {{ inv.outroLocal }} em outro local · {{ inv.inserviveis }} inservível(is)</dd></div>
          </dl>
          <div class="tbl-wrap">
            <table class="iv-tabela" aria-label="Bens do inventário">
              <thead><tr><th scope="col">Bem</th><th scope="col">Setor</th><th scope="col">Conferência</th>@if (inv.situacao === 'ABERTO') { <th scope="col"><span class="sr-only">Conferir</span></th> }</tr></thead>
              <tbody>
                @for (i of inv.itens; track i.uuid) {
                  <tr>
                    <td><span class="cell-main mono">{{ i.numeroTombamento }}</span><span class="cell-sub">{{ i.bemDescricao }}</span></td>
                    <td>{{ i.setorEsperadoNome ?? '—' }}</td>
                    <td>@if (i.resultado) { {{ resultados[i.resultado] }}@if (i.conservacao) { · {{ conservacoes[i.conservacao] }} }@if (i.setorEncontradoNome || i.unidadeEncontradaNome) { <span class="cell-sub">{{ i.unidadeEncontradaNome ?? '' }} {{ i.setorEncontradoNome ?? '' }}</span> }@if (i.acao) { <span class="tag tag--info">{{ acoes[i.acao] }}</span> } } @else { <span class="cell-sub">a conferir</span> }</td>
                    @if (inv.situacao === 'ABERTO') {
                      <td class="row-actions"><button type="button" class="link-btn" (click)="escolher(i)" [attr.aria-label]="'Conferir ' + i.numeroTombamento">Conferir</button></td>
                    }
                  </tr>
                }
              </tbody>
            </table>
          </div>

          @if (item(); as i) {
            <h3 class="mo-grupo iv-titulo">Conferir {{ i.numeroTombamento }}</h3>
            <div class="iv-linha">
              <div class="dw-field"><label for="f-iv-resultado">Resultado</label>
                <select id="f-iv-resultado" (change)="set('resultado', $event)">
                  @for (r of listaResultados; track r) { <option [value]="r" [selected]="(f()['resultado'] || 'ENCONTRADO') === r">{{ resultados[r] }}</option> }
                </select></div>
              @if (f()['resultado'] !== 'NAO_ENCONTRADO') {
                <div class="dw-field"><label for="f-iv-conservacao">Conservação</label>
                  <select id="f-iv-conservacao" (change)="set('conservacao', $event)">
                    <option value="">Escolha…</option>
                    @for (c of listaConservacoes; track c) { <option [value]="c">{{ conservacoes[c] }}</option> }
                  </select></div>
              }
              @if (f()['resultado'] === 'OUTRO_LOCAL') {
                <div class="dw-field"><label for="f-iv-onde">Unidade onde estava</label>
                  <select id="f-iv-onde" (change)="set('unidadeId', $event)">
                    <option value="">A própria</option>
                    @for (u of unidades; track u.uuid) { @if (u.uuid !== inv.unidadeId) { <option [value]="u.uuid">{{ u.nome }}</option> } }
                  </select></div>
                <div class="dw-field"><label for="f-iv-setor">Setor onde estava</label>
                  <select id="f-iv-setor" (change)="set('setorId', $event)">
                    <option value="">—</option>
                    @for (s of setoresDa(f()['unidadeId'] || inv.unidadeId); track s.uuid) { <option [value]="s.uuid">{{ s.nome }}</option> }
                  </select></div>
              }
              <button type="button" class="btn btn--sm" (click)="conferir(i)" [disabled]="enviando()">Registrar</button>
            </div>
          }

          <h3 class="mo-grupo iv-titulo">Achados a mais</h3>
          <ul class="iv-lista" aria-label="Achados a mais">
            @for (a of inv.achados; track a.uuid) { <li>{{ a.descricao }}@if (a.local) { <span class="cell-sub">{{ a.local }}</span> }</li> } @empty { <li class="cell-sub">Nenhum.</li> }
          </ul>
          @if (inv.situacao === 'ABERTO') {
            <div class="iv-linha">
              <div class="dw-field" style="flex: 2;"><label for="f-iv-achado">Bem achado fora da lista</label><input id="f-iv-achado" type="text" maxlength="500" (input)="set('achado', $event)" /></div>
              <div class="dw-field"><label for="f-iv-local">Local</label><input id="f-iv-local" type="text" maxlength="200" (input)="set('local', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="achado(inv)" [disabled]="enviando()">Registrar achado</button>
            </div>
            @if (podeGerenciar) {
              <div class="iv-linha iv-fechar"><button type="button" class="btn" (click)="fechar(inv)" [disabled]="enviando()">Fechar inventário</button></div>
            }
          }
        </div>
      </app-drawer>
    }
  `,
  styles: `
    .iv-cab { display: flex; justify-content: flex-end; margin-bottom: var(--s-2); }
    .iv-tabela { min-width: 0; width: 100%; }
    .iv-titulo { margin-top: var(--s-4); }
    .iv-linha { display: flex; gap: var(--s-2); align-items: flex-end; flex-wrap: wrap; margin-bottom: var(--s-2); }
    .iv-linha .dw-field { margin: 0; }
    .iv-lista { margin: 0 0 var(--s-2); padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .iv-fechar { margin-top: var(--s-4); }
  `,
})
export class InventarioPatrimonio implements OnInit {
  @Input() podeGerenciar = false;
  @Input() meus = false;
  @Input() unidades: UnidadeSaudeResponseDto[] = [];
  @Input() setores: SetorResponseDto[] = [];

  private readonly service = inject(PatrimonioService);

  protected readonly resultados = RESULTADOS_CONFERENCIA;
  protected readonly listaResultados = Object.keys(RESULTADOS_CONFERENCIA) as ResultadoConferencia[];
  protected readonly conservacoes = CONSERVACOES;
  protected readonly listaConservacoes = Object.keys(CONSERVACOES) as ConservacaoBem[];
  protected readonly acoes = ACOES_INVENTARIO;

  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly lista = signal<InventarioDto[]>([]);
  protected readonly abrindo = signal(false);
  protected readonly atual = signal<InventarioDto | null>(null);
  protected readonly item = signal<ItemInventarioDto | null>(null);

  ngOnInit(): void {
    this.carregar();
  }

  private carregar(): void {
    (this.meus ? this.service.meusInventarios() : this.service.inventarios()).subscribe({
      next: (l) => this.lista.set(l),
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  protected setoresDa(unidadeId: string): SetorResponseDto[] {
    return this.setores.filter((s) => s.unidadeUuid === unidadeId);
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected novo(): void {
    this.f.set({});
    this.erroGaveta.set(null);
    this.abrindo.set(true);
  }

  protected salvar(): void {
    const v = this.f();
    const membros = [v['m1'], v['m2'], v['m3']].map((m) => m?.trim() ?? '').filter((m) => !!m);
    if (!v['unidadeId'] || membros.length !== 3) {
      this.erroGaveta.set('Escolha a unidade e informe as três matrículas da comissão.');
      return;
    }
    this.enviando.set(true);
    this.service.abrirInventario(v['unidadeId'], membros, null).subscribe({
      next: (i) => {
        this.enviando.set(false);
        this.abrindo.set(false);
        this.aviso.set(`Inventário ${i.numero} aberto com ${i.total} bem(ns).`);
        this.carregar();
        this.atual.set(i);
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected abrir(id: string): void {
    this.erroGaveta.set(null);
    this.item.set(null);
    this.service.inventario(id).subscribe({ next: (i) => this.atual.set(i), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  protected escolher(i: ItemInventarioDto): void {
    this.f.set({ resultado: 'ENCONTRADO' });
    this.item.set(i);
  }

  protected conferir(i: ItemInventarioDto): void {
    const v = this.f();
    const resultado = (v['resultado'] || 'ENCONTRADO') as ResultadoConferencia;
    this.naGaveta(this.service.conferir(i.uuid, { resultado, conservacao: (v['conservacao'] || null) as ConservacaoBem | null,
      setorId: v['setorId'] || null, unidadeId: v['unidadeId'] || null, observacao: null }), () => this.item.set(null));
  }

  protected achado(inv: InventarioDto): void {
    const d = this.f()['achado']?.trim();
    if (!d) {
      this.erroGaveta.set('Descreva o bem achado.');
      return;
    }
    this.naGaveta(this.service.registrarAchado(inv.uuid, d, this.f()['local']?.trim() || null), () => this.f.set({}));
  }

  protected fechar(inv: InventarioDto): void {
    this.naGaveta(this.service.fecharInventario(inv.uuid), () => this.aviso.set(`Inventário ${inv.numero} fechado.`));
  }

  private naGaveta(chamada: Observable<InventarioDto>, depois: () => void): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    chamada.subscribe({
      next: (i) => {
        this.enviando.set(false);
        this.atual.set(i);
        depois();
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
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

