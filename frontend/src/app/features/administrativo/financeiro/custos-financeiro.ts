import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import {
  ApuracaoCustoDto,
  CATEGORIAS_CUSTO,
  CategoriaCusto,
  CentroCustoDto,
  IndicadoresCustoDto,
  TIPOS_CENTRO_CUSTO,
  TipoCentroCusto,
} from '../../../core/models/financeiro';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { SetorResponseDto } from '../../../core/models/setor';
import { FinanceiroService } from '../../../core/services/financeiro';
import { SetorService } from '../../../core/services/setor';
import { Drawer } from '../../../shared/drawer/drawer';

/**
 * Financeiro, fatia F3 (ADR-0220): a apuração de custos da competência por centro e categoria, os indicadores por unidade (custo por
 * atendimento e por paciente-dia contra a média do tipo) e os centros de custo.
 */
@Component({
  selector: 'app-custos-financeiro',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (aviso()) { <div class="box box--info" role="status"><strong>{{ aviso() }}</strong></div> }

    <div class="cu-cab">
      <div class="dw-field cu-mes"><label for="f-cu-competencia">Competência</label>
        <input id="f-cu-competencia" type="month" [max]="ultimoMes" [value]="competencia()" (change)="trocar($event)" /></div>
      @if (podeGerenciar) {
        @if (apuracao()?.situacao !== 'FECHADA') { <button type="button" class="btn" (click)="apurar()" [disabled]="enviando()">{{ apuracao() ? 'Refazer a apuração' : 'Apurar' }}</button> }
        @if (apuracao()?.situacao === 'ABERTA') { <button type="button" class="btn btn--ghost" (click)="fechar()" [disabled]="enviando()">Fechar a competência</button> }
      }
    </div>

    @if (apuracao(); as a) {
      <div class="cu-numeros" aria-label="Resumo da apuração">
        <div class="card cu-num"><span class="cu-num__v">{{ moeda(a.total) }}</span><span class="cell-sub">custo apurado · {{ a.situacao === 'FECHADA' ? 'fechada' : 'aberta' }}</span></div>
        <div class="card cu-num"><span class="cu-num__v">{{ moeda(a.naoAlocado) }}</span><span class="cell-sub">sem centro (folha sem lotação, avulsa antiga)</span></div>
        <div class="card cu-num"><span class="cu-num__v">{{ a.saidasSemCusto }}</span><span class="cell-sub">saídas sem preço, fora do total</span></div>
      </div>

      <h3 class="mo-grupo cu-titulo">Por unidade</h3>
      @if (indicadores(); as ind) {
        <div class="tbl-wrap card">
          <table class="cu-tabela" aria-label="Custo por unidade">
            <thead><tr><th scope="col">Unidade</th><th scope="col">Custo</th><th scope="col">Atendimentos</th><th scope="col">Por atendimento</th><th scope="col">Pacientes-dia</th><th scope="col">Por paciente-dia</th></tr></thead>
            <tbody>
              @for (u of ind.unidades; track u.unidadeId) {
                <tr>
                  <td><span class="cell-main">{{ u.unidadeNome }}</span><span class="cell-sub">{{ u.tipoUnidade }}</span></td>
                  <td class="mono">{{ moeda(u.total) }}</td>
                  <td class="mono">{{ u.atendimentos }}</td>
                  <td class="mono">@if (u.custoPorAtendimento !== null) { {{ moeda(u.custoPorAtendimento) }} @if (u.acimaDaMedia) { <span class="tag tag--alert">acima da média</span> }
                    @if (u.mediaDoTipo !== null) { <span class="cell-sub">média do tipo {{ moeda(u.mediaDoTipo) }}</span> } } @else { — }</td>
                  <td class="mono">{{ u.pacientesDia }}</td>
                  <td class="mono">{{ u.custoPorPacienteDia !== null ? moeda(u.custoPorPacienteDia) : '—' }}</td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhum custo na competência.</td></tr> }
            </tbody>
          </table>
        </div>
      }

      <h3 class="mo-grupo cu-titulo">Por centro de custo</h3>
      <div class="tbl-wrap card">
        <table class="cu-tabela cu-larga" aria-label="Custo por centro">
          <thead><tr><th scope="col">Centro</th>@for (c of categorias; track c) { <th scope="col">{{ rotulos[c] }}</th> }<th scope="col">Total</th></tr></thead>
          <tbody>
            @for (l of a.centros; track l.centroId) {
              <tr>
                <td><span class="cell-main">{{ l.codigo }} · {{ l.nome }}</span><span class="cell-sub">{{ l.unidadeNome }}@if (l.setorNome) { · {{ l.setorNome }} }</span></td>
                @for (c of categorias; track c) { <td class="mono">{{ l.valores[c] ? moeda(l.valores[c]!) : '—' }}</td> }
                <td class="mono"><strong>{{ moeda(l.total) }}</strong></td>
              </tr>
            } @empty { <tr><td [attr.colspan]="categorias.length + 2" class="cell-sub">Nenhum custo na competência.</td></tr> }
          </tbody>
        </table>
      </div>
    } @else if (carregado()) {
      <p class="cell-sub">A competência {{ competencia() }} ainda não foi apurada.</p>
    }

    <div class="cu-cab cu-titulo">
      <h3 class="mo-grupo">Centros de custo</h3>
      @if (podeGerenciar) { <button type="button" class="btn btn--ghost" (click)="novo()">Novo centro do setor</button> }
    </div>
    <div class="tbl-wrap card">
      <table class="cu-tabela" aria-label="Centros de custo">
        <thead><tr><th scope="col">Centro</th><th scope="col">Unidade e setor</th><th scope="col">Tipo</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
        <tbody>
          @for (c of centros(); track c.uuid) {
            <tr>
              <td><span class="cell-main">{{ c.codigo }} · {{ c.nome }}</span>@if (!c.ativo) { <span class="tag">inativo</span> }</td>
              <td>{{ c.unidadeNome }}<span class="cell-sub">{{ c.geral ? 'geral da unidade' : c.setorNome }}</span></td>
              <td>{{ tipos[c.tipo] }}</td>
              <td class="row-actions">@if (podeGerenciar) { <button type="button" class="link-btn" (click)="editar(c)" [attr.aria-label]="'Alterar ' + c.codigo">Alterar</button> }</td>
            </tr>
          } @empty { <tr><td colspan="4" class="cell-sub">Nenhum centro de custo.</td></tr> }
        </tbody>
      </table>
    </div>

    @if (gaveta()) {
      <app-drawer [titulo]="centro() ? 'Alterar ' + centro()!.codigo : 'Novo centro de custo'" (fechar)="gaveta.set(false)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @if (!centro()) {
              <div class="dw-field"><label for="f-cc-setor">Setor <span class="req" aria-hidden="true">*</span></label>
                <select id="f-cc-setor" (change)="set('setorId', $event)">
                  <option value="">Escolha…</option>
                  @for (s of setoresSemCentro(); track s.uuid) { <option [value]="s.uuid">{{ s.unidadeNome }} · {{ s.nome }}</option> }
                </select></div>
            }
            <div class="dw-field"><label for="f-cc-nome">Nome@if (!centro()) { <span class="opt">sem informar, o do setor</span> }</label>
              <input id="f-cc-nome" type="text" maxlength="150" [value]="f()['nome'] ?? ''" (input)="set('nome', $event)" /></div>
            <div class="dw-field"><label for="f-cc-tipo">Tipo@if (!centro()) { <span class="opt">sem informar, o do setor</span> }</label>
              <select id="f-cc-tipo" (change)="set('tipo', $event)">
                @if (!centro()) { <option value="">Pelo setor</option> }
                @for (t of tiposLista; track t) { <option [value]="t" [selected]="f()['tipo'] === t">{{ tipos[t] }}</option> }
              </select></div>
            @if (centro() && !centro()!.geral) {
              <label class="dw-check"><input id="f-cc-ativo" type="checkbox" [checked]="f()['ativo'] !== 'false'" (change)="marcar($event)" /> Ativo</label>
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(false)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Salvar</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .cu-cab { display: flex; flex-wrap: wrap; align-items: flex-end; gap: var(--s-2); margin-bottom: var(--s-3); }
    .cu-cab h3 { margin: 0; flex: 1; }
    .cu-mes { margin: 0; }
    .cu-tabela { min-width: 0; width: 100%; }
    .cu-larga { min-width: 900px; }
    .cu-titulo { margin-top: var(--s-4); }
    .cu-numeros { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: var(--s-2); margin-bottom: var(--s-3); }
    .cu-num { padding: var(--s-3); display: grid; gap: 2px; }
    .cu-num__v { font-size: 1.25rem; font-weight: 600; }
  `,
})
export class CustosFinanceiro implements OnInit {
  @Input() podeGerenciar = false;

  private readonly service = inject(FinanceiroService);
  private readonly setorService = inject(SetorService);

  protected readonly categorias = Object.keys(CATEGORIAS_CUSTO) as CategoriaCusto[];
  protected readonly rotulos = CATEGORIAS_CUSTO;
  protected readonly tipos = TIPOS_CENTRO_CUSTO;
  protected readonly tiposLista = Object.keys(TIPOS_CENTRO_CUSTO) as TipoCentroCusto[];
  /** A apuração é do mês encerrado: o anterior ao de hoje. */
  protected readonly ultimoMes = mesAnterior();

  protected readonly competencia = signal(mesAnterior());
  protected readonly apuracao = signal<ApuracaoCustoDto | null>(null);
  protected readonly indicadores = signal<IndicadoresCustoDto | null>(null);
  protected readonly carregado = signal(false);
  protected readonly centros = signal<CentroCustoDto[]>([]);
  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly gaveta = signal(false);
  protected readonly centro = signal<CentroCustoDto | null>(null);
  protected readonly f = signal<Record<string, string>>({});

  protected readonly setoresSemCentro = computed(() => {
    const com = new Set(this.centros().map((c) => c.setorId).filter((x) => !!x));
    return this.setores().filter((s) => s.ativo && !com.has(s.uuid));
  });

  ngOnInit(): void {
    this.carregarCentros();
    this.carregarApuracao();
  }

  private carregarCentros(): void {
    this.service.centrosCusto().subscribe({ next: (l) => this.centros.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  private carregarApuracao(): void {
    const c = this.competencia();
    this.carregado.set(false);
    this.service.apuracaoCusto(c).pipe(catchError(() => of(null))).subscribe((a) => {
      this.apuracao.set(a);
      this.carregado.set(true);
      if (a) {
        this.service.indicadoresCusto(c).pipe(catchError(() => of(null))).subscribe((i) => this.indicadores.set(i));
      } else {
        this.indicadores.set(null);
      }
    });
  }

  protected trocar(ev: Event): void {
    const v = (ev.target as HTMLInputElement).value;
    if (!v) return;
    this.competencia.set(v);
    this.aviso.set(null);
    this.carregarApuracao();
  }

  protected apurar(): void {
    this.executar(this.service.apurarCusto(this.competencia()), (a) => this.aviso.set(`Competência ${a.competencia} apurada: ${this.moeda(a.total)}.`), false);
  }

  protected fechar(): void {
    const a = this.apuracao();
    if (!a) return;
    this.executar(this.service.fecharApuracaoCusto(a.uuid), (x) => this.aviso.set(`Competência ${x.competencia} fechada; não muda mais.`), false);
  }

  protected novo(): void {
    this.centro.set(null);
    this.f.set({});
    this.erroGaveta.set(null);
    this.setorService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.setores.set(l));
    this.gaveta.set(true);
  }

  protected editar(c: CentroCustoDto): void {
    this.centro.set(c);
    this.f.set({ nome: c.nome, tipo: c.tipo, ativo: String(c.ativo) });
    this.erroGaveta.set(null);
    this.gaveta.set(true);
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected marcar(ev: Event): void {
    this.f.set({ ...this.f(), ativo: String((ev.target as HTMLInputElement).checked) });
  }

  protected salvar(): void {
    const v = this.f();
    const c = this.centro();
    if (c) {
      if (!v['nome']?.trim()) {
        this.erroGaveta.set('Informe o nome.');
        return;
      }
      this.executar(this.service.alterarCentroCusto(c.uuid, { nome: v['nome'].trim(), tipo: v['tipo'] as TipoCentroCusto, ativo: v['ativo'] !== 'false' }),
        (x) => this.aviso.set(`Centro ${x.codigo} alterado.`), true);
      return;
    }
    if (!v['setorId']) {
      this.erroGaveta.set('Escolha o setor.');
      return;
    }
    this.executar(this.service.criarCentroCusto({ setorId: v['setorId'], nome: v['nome']?.trim() || null, tipo: (v['tipo'] || null) as TipoCentroCusto | null }),
      (x) => this.aviso.set(`Centro ${x.codigo} · ${x.nome} criado.`), true);
  }

  private executar<T>(chamada: Observable<T>, depois: (r: T) => void, daGaveta: boolean): void {
    this.enviando.set(true);
    this.erro.set(null);
    this.erroGaveta.set(null);
    chamada.subscribe({
      next: (r) => {
        this.enviando.set(false);
        this.gaveta.set(false);
        depois(r);
        this.carregarCentros();
        this.carregarApuracao();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        (daGaveta ? this.erroGaveta : this.erro).set(mensagem(x));
      },
    });
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}

/** O mês anterior ao de hoje em Brasília, AAAA-MM. */
function mesAnterior(): string {
  const [a, m] = new Date().toLocaleDateString('en-CA', { timeZone: 'America/Sao_Paulo' }).split('-').map(Number);
  return m === 1 ? `${a - 1}-12` : `${a}-${String(m - 1).padStart(2, '0')}`;
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
