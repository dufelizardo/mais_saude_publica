import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import {
  CobrancaIntersetorialDto,
  DominioOrigemEncaminhamento,
  ORIGENS_ENCAMINHAMENTO,
  PainelInstituicaoDto,
  PainelIntersetorialDto,
  SETORES_INTERSETORIAIS,
  SetorIntersetorial,
  TIPOS_INSTITUICAO,
} from '../../../core/models/intersetorialidade';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { IntersetorialidadeService } from '../../../core/services/intersetorialidade';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { dataBr, hojeIso } from '../../../shared/formato';

/**
 * Painel da rede intersetorial (IS2, ADR-0211): quais instituições recebem os encaminhamentos, quais respondem e quais
 * deixam o caso sem retorno. Enviados, desfechos, taxa e tempo médio são do período; em aberto e sem retorno, de hoje.
 * Não mostra o paciente nem o motivo: para cobrar a instituição bastam o número e as datas.
 */
@Component({
  selector: 'app-painel-intersetorial',
  imports: [Drawer],
  template: `
    <div class="toolbar" role="region" aria-label="Filtros do painel">
      <label class="pill pill--select">
        <span class="sr-only">Enviados desde</span>
        <input type="date" aria-label="Enviados desde" [value]="inicio()" [max]="fim()" (change)="inicio.set($any($event.target).value)" />
      </label>
      <label class="pill pill--select">
        <span class="sr-only">Enviados até</span>
        <input type="date" aria-label="Enviados até" [value]="fim()" [max]="hoje" (change)="fim.set($any($event.target).value)" />
      </label>
      <label class="pill pill--select">
        <span class="sr-only">Setor</span>
        <select aria-label="Setor do painel" (change)="setor.set($any($event.target).value)">
          <option value="" [selected]="!setor()">Todos os setores</option>
          @for (s of setores; track s) { <option [value]="s" [selected]="setor() === s">{{ rotuloSetor[s] }}</option> }
        </select>
      </label>
      <label class="pill pill--select">
        <span class="sr-only">Origem</span>
        <select aria-label="Origem do encaminhamento" (change)="origem.set($any($event.target).value)">
          <option value="" [selected]="!origem()">Todas as origens</option>
          @for (o of origens; track o) { <option [value]="o" [selected]="origem() === o">{{ rotuloOrigem[o] }}</option> }
        </select>
      </label>
      <label class="pill pill--select">
        <span class="sr-only">Unidade</span>
        <select aria-label="Unidade que encaminhou" (change)="unidade.set($any($event.target).value)">
          <option value="" [selected]="!unidade()">Todas as unidades</option>
          @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="unidade() === u.uuid">{{ u.nome }}</option> }
        </select>
      </label>
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    @if (painel(); as p) {
      <section class="stats" aria-label="Resumo da rede intersetorial">
        <div class="stat">
          <span class="stat__icon" aria-hidden="true"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg></span>
          <div><div class="stat__lbl">Enviados</div><div class="stat__val mono">{{ p.enviados }}</div><div class="stat__sub">de {{ data(p.inicio) }} a {{ data(p.fim) }}</div></div>
        </div>
        <div class="stat">
          <span class="stat__icon" aria-hidden="true"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg></span>
          <div><div class="stat__lbl">Em aberto</div><div class="stat__val mono">{{ p.emAberto }}</div><div class="stat__sub">hoje, de qualquer data</div></div>
        </div>
        <div class="stat">
          <span [class]="'stat__icon' + (p.semRetorno ? ' stat__icon--alert' : ' stat__icon--ok')" aria-hidden="true"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg></span>
          <div><div class="stat__lbl">Sem retorno</div><div class="stat__val mono">{{ p.semRetorno }}</div><div class="stat__sub">em aberto depois do prazo</div></div>
        </div>
        <div class="stat">
          <span class="stat__icon stat__icon--ok" aria-hidden="true"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg></span>
          <div><div class="stat__lbl">Taxa de resolução</div><div class="stat__val mono">{{ pct(p.taxaResolucao) }}</div><div class="stat__sub">@if (p.tempoMedioRetornoDias !== null) { retorno em {{ dias(p.tempoMedioRetornoDias) }} em média } @else { sem retorno no período }</div></div>
        </div>
      </section>

      <div class="tbl-wrap card">
        <table aria-label="Encaminhamentos por instituição">
          <thead>
            <tr>
              <th scope="col">Instituição</th>
              <th scope="col" class="num">Enviados</th>
              <th scope="col" class="num">Em aberto</th>
              <th scope="col" class="num">Sem retorno</th>
              <th scope="col" class="num">Concluídos</th>
              <th scope="col" class="num">Recusados</th>
              <th scope="col" class="num">Resolução</th>
              <th scope="col" class="num">Tempo médio</th>
              <th scope="col"><span class="sr-only">Ações</span></th>
            </tr>
          </thead>
          <tbody>
            @for (i of p.instituicoes; track i.instituicaoId) {
              <tr>
                <td>
                  <span class="cell-main">{{ i.nome }}@if (!i.ativa) { <span class="tag tag--muted">Inativa</span> }</span>
                  <span class="cell-sub">{{ tipos[i.tipo].rotulo }} · {{ porOrigem(i) }}</span>
                </td>
                <td class="num mono">{{ i.enviados }}</td>
                <td class="num mono">{{ i.emAberto }}</td>
                <td class="num mono">@if (i.semRetorno) { <span class="tag tag--warn">{{ i.semRetorno }}</span> } @else { 0 }</td>
                <td class="num mono">{{ i.concluidos }}</td>
                <td class="num mono">{{ i.recusados }}</td>
                <td class="num mono">{{ pct(i.taxaResolucao) }}</td>
                <td class="num mono">{{ i.tempoMedioRetornoDias !== null ? dias(i.tempoMedioRetornoDias) : '—' }}</td>
                <td class="row-actions">
                  @if (i.semRetorno) { <button type="button" class="link-btn" (click)="cobrar(i)" [attr.aria-label]="'Cobrar ' + i.nome">Cobrar</button> }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="9" class="cell-sub">{{ carregando() ? 'Carregando…' : 'Nenhum encaminhamento com esses filtros.' }}</td></tr>
            }
          </tbody>
        </table>
      </div>
    } @else if (carregando()) {
      <p class="cell-sub">Carregando…</p>
    }

    @if (cobranca(); as c) {
      <app-drawer [titulo]="'Sem retorno: ' + c.instituicao.nome" (fechar)="cobranca.set(null)">
        <div class="dw-body">
          <p class="cell-sub">
            @if (c.instituicao.responsavel) { {{ c.instituicao.responsavel }} · }@if (c.instituicao.telefone) { {{ c.instituicao.telefone }} }@if (c.instituicao.telefone && c.instituicao.email) { · }@if (c.instituicao.email) { {{ c.instituicao.email }} }
          </p>
          <p class="cell-sub">Para cobrar a instituição, use o número e a data do envio. O paciente e o motivo ficam com quem encaminhou.</p>
          <div class="tbl-wrap">
            <table aria-label="Encaminhamentos sem retorno">
              <thead><tr><th scope="col">Número</th><th scope="col">Enviado</th><th scope="col">Prazo</th><th scope="col" class="num">Atraso</th><th scope="col">Origem</th></tr></thead>
              <tbody>
                @for (e of c.semRetorno; track e.encaminhamentoId) {
                  <tr>
                    <td><span class="cell-main mono">{{ e.numero }}</span><span class="cell-sub">{{ e.situacao === 'RECEBIDO' ? 'Recebido em ' + data(e.recebidoEm) : 'Não confirmou o recebimento' }}</span></td>
                    <td>{{ data(e.enviadoEm) }}</td>
                    <td>{{ data(e.retornoEsperadoEm) }}</td>
                    <td class="num mono">{{ dias(e.diasDeAtraso) }}</td>
                    <td><span class="cell-main">{{ rotuloOrigem[e.origem] }}</span><span class="cell-sub">{{ e.unidadeNome }}</span></td>
                  </tr>
                } @empty {
                  <tr><td colspan="5" class="cell-sub">Nenhum encaminhamento sem retorno.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </app-drawer>
    }
  `,
})
export class PainelIntersetorial {
  private readonly service = inject(IntersetorialidadeService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly hoje = hojeIso();
  protected readonly setores = Object.keys(SETORES_INTERSETORIAIS) as SetorIntersetorial[];
  protected readonly rotuloSetor = SETORES_INTERSETORIAIS;
  protected readonly origens = Object.keys(ORIGENS_ENCAMINHAMENTO) as DominioOrigemEncaminhamento[];
  protected readonly rotuloOrigem = ORIGENS_ENCAMINHAMENTO;
  protected readonly tipos = TIPOS_INSTITUICAO;

  protected readonly inicio = signal(diasAtras(89));
  protected readonly fim = signal(hojeIso());
  protected readonly setor = signal<SetorIntersetorial | ''>('');
  protected readonly origem = signal<DominioOrigemEncaminhamento | ''>('');
  protected readonly unidade = signal('');
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);

  protected readonly painel = signal<PainelIntersetorialDto | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly cobranca = signal<CobrancaIntersetorialDto | null>(null);

  constructor() {
    this.unidadeService.listar().pipe(catchError(() => of([] as UnidadeSaudeResponseDto[]))).subscribe((l) => this.unidades.set(l));
    effect(() => {
      const filtro = { inicio: this.inicio(), fim: this.fim(), setor: this.setor(), origem: this.origem(), unidade: this.unidade() };
      untracked(() => this.carregar(filtro));
    });
  }

  protected cobrar(i: PainelInstituicaoDto): void {
    this.service.semRetorno(i.instituicaoId).subscribe({
      next: (c) => this.cobranca.set(c),
      error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)),
    });
  }

  protected porOrigem(i: PainelInstituicaoDto): string {
    const partes = this.origens.filter((o) => i.enviadosPorOrigem[o]).map((o) => `${this.rotuloOrigem[o]} ${i.enviadosPorOrigem[o]}`);
    return partes.length ? partes.join(' · ') : 'sem envios no período';
  }

  protected pct(v: number | null): string {
    return v === null ? '—' : `${v.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`;
  }

  protected dias(v: number): string {
    const n = v.toLocaleString('pt-BR', { maximumFractionDigits: 1 });
    return v === 1 ? '1 dia' : `${n} dias`;
  }

  protected data(iso: string | null): string {
    return dataBr(iso);
  }

  private carregar(filtro: Parameters<IntersetorialidadeService['painel']>[0]): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.service.painel(filtro).pipe(catchError((e: HttpErrorResponse) => {
      this.erro.set(mensagem(e));
      return of(null);
    })).subscribe((p) => {
      this.painel.set(p);
      this.carregando.set(false);
    });
  }
}

function diasAtras(n: number): string {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function mensagem(e: HttpErrorResponse): string {
  return e.status === 403 ? 'Seu acesso não inclui o painel da rede intersetorial.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar o painel.');
}
