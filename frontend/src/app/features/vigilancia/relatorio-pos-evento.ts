import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { EmergenciaDto, RelatorioPosEventoDto, TIPOS_MOBILIZACAO, TipoMobilizacao } from '../../core/models/emergencia';
import { ErrorResponseDto } from '../../core/models/profissional';
import { EmergenciaService } from '../../core/services/emergencia';

interface Recomendacao { descricao: string; planoCodigo: string }

/**
 * Relatório pós-evento da emergência (ADR-0159): os números calculados do que foi registrado, o texto da equipe e as
 * recomendações (a que aponta um plano usado gera a revisão dele). Começa na desmobilização; finaliza depois de
 * encerrada; finalizado, só leitura.
 */
@Component({
  selector: 'app-relatorio-pos-evento',
  template: `
    <section class="rp" aria-label="Relatório pós-evento">
      <div class="rp-cab">
        <h3 class="mo-grupo" style="margin: 0;">Relatório pós-evento
          @if (relatorio()?.situacao === 'FINALIZADO') { <span class="tag tag--ok">Finalizado</span> } @else if (relatorio()?.situacao === 'RASCUNHO') { <span class="tag tag--info">Rascunho</span> }
        </h3>
        <button type="button" class="btn btn--ghost btn--sm" (click)="exportar()">Exportar números (CSV)</button>
      </div>
      @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
      @if (relatorio(); as r) {
        @let i = r.indicadores;
        <div class="rp-numeros" aria-label="Números do evento">
          <div class="rp-num"><b>{{ i.duracaoHoras }} h</b><span>duração</span></div>
          <div class="rp-num"><b>{{ i.horasAteAtivacao }} h</b><span>do evento à ativação</span></div>
          <div class="rp-num"><b>{{ i.nivelMaximo }}</b><span>nível máximo · {{ i.mudancasDeNivel }} mudança(s)</span></div>
          <div class="rp-num"><b>{{ i.acoesConcluidas }}/{{ i.acoesTotal }}</b><span>ações concluídas@if (i.acoesCanceladas) { · {{ i.acoesCanceladas }} cancelada(s) }</span></div>
          <div class="rp-num"><b>{{ i.percentualNoPrazo != null ? i.percentualNoPrazo + '%' : '—' }}</b><span>concluídas no prazo</span></div>
          <div class="rp-num"><b>{{ i.mobilizacoes }}</b><span>mobilização(ões) · {{ i.profissionaisMobilizados }} profissional(is)</span></div>
          <div class="rp-num"><b>+{{ i.capacidadeAtivada }}</b><span>capacidade emergencial ativada</span></div>
          <div class="rp-num"><b>{{ i.populacaoAfetada ?? '—' }}</b><span>pessoas afetadas · {{ i.unidadesAfetadas }} unidade(s)</span></div>
        </div>
        @if (i.mobilizacoesPorTipo.length) {
          <p class="cell-sub">Mobilizações: @for (c of i.mobilizacoesPorTipo; track c.chave; let ultimo = $last) { {{ rotuloTipo(c.chave) }} {{ c.quantidade }}@if (!ultimo) { · } }</p>
        }

        @if (editavel()) {
          <form class="dw-form" (submit)="$event.preventDefault(); salvar()" novalidate>
            <div class="dw-field"><label for="f-rp-resumo">Resumo</label><textarea id="f-rp-resumo" rows="3" maxlength="4000" [value]="resumo()" (input)="resumo.set($any($event.target).value)"></textarea></div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-rp-problemas">Problemas</label><textarea id="f-rp-problemas" rows="3" maxlength="4000" [value]="problemas()" (input)="problemas.set($any($event.target).value)"></textarea></div>
              <div class="dw-field"><label for="f-rp-licoes">Lições aprendidas</label><textarea id="f-rp-licoes" rows="3" maxlength="4000" [value]="licoes()" (input)="licoes.set($any($event.target).value)"></textarea></div>
            </div>
            <h4 class="mo-grupo">Recomendações</h4>
            @for (rec of recomendacoes(); track $index; let n = $index) {
              <div class="rp-linha">
                <input type="text" maxlength="1000" [attr.aria-label]="'Recomendação ' + (n + 1)" [value]="rec.descricao" (input)="mudar(n, 'descricao', $event)" placeholder="Ex.: abrir o abrigo já no nível 1" />
                <select [attr.aria-label]="'Plano da recomendação ' + (n + 1)" (change)="mudar(n, 'planoCodigo', $event)">
                  <option value="" [selected]="!rec.planoCodigo">Sem plano</option>
                  @for (p of r.planosUsados; track p.codigo) { <option [value]="p.codigo" [selected]="rec.planoCodigo === p.codigo">Revisar {{ p.nome }}</option> }
                </select>
                <button type="button" class="link-btn" (click)="remover(n)" [attr.aria-label]="'Remover recomendação ' + (n + 1)">Remover</button>
              </div>
            }
            <button type="button" class="link-btn" (click)="recomendacoes.set([...recomendacoes(), { descricao: '', planoCodigo: '' }])">Adicionar recomendação</button>
            <div class="row-actions" style="justify-content: flex-end; margin-top: var(--s-3);">
              <button type="submit" class="btn btn--ghost btn--sm" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar rascunho' }}</button>
              @if (podeFinalizar() && r.situacao === 'RASCUNHO') { <button type="button" class="btn btn--sm" (click)="finalizar()" [disabled]="emergencia().situacao !== 'ENCERRADA'" [title]="emergencia().situacao !== 'ENCERRADA' ? 'Encerre a emergência antes' : ''">Finalizar</button> }
            </div>
          </form>
        } @else {
          <dl class="info-list">
            <div><dt>Resumo</dt><dd>{{ r.resumo ?? '—' }}</dd></div>
            <div><dt>Problemas</dt><dd>{{ r.problemas ?? '—' }}</dd></div>
            <div><dt>Lições aprendidas</dt><dd>{{ r.licoes ?? '—' }}</dd></div>
          </dl>
        }
        @if (r.recomendacoes.length) {
          <ul class="eq-prof" aria-label="Recomendações do relatório">
            @for (rec of r.recomendacoes; track rec.uuid) {
              <li class="mo-row">
                <span><span class="cell-main">{{ rec.descricao }}</span>@if (rec.planoCodigo) { <span class="cell-sub">Plano {{ rec.planoCodigo }}@if (rec.planoRevisaoVersao) { · revisão aberta na versão {{ rec.planoRevisaoVersao }} }</span> }</span>
                @if (rec.planoCodigo && !rec.planoRevisaoId && podeRevisarPlano()) { <button type="button" class="link-btn" (click)="revisar(rec.uuid)" [attr.aria-label]="'Revisar plano ' + rec.planoCodigo">Revisar plano</button> }
              </li>
            }
          </ul>
        }
      }
    </section>
  `,
  styles: `
    .rp { margin-top: var(--s-4); }
    .rp-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-2); flex-wrap: wrap; margin-bottom: var(--s-2); }
    .rp-numeros { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: var(--s-2); margin-bottom: var(--s-3); }
    .rp-num { border: 1px solid var(--ink-200); border-radius: 10px; padding: var(--s-2) var(--s-3); display: grid; gap: 2px; }
    .rp-num b { font-size: 1.05rem; }
    .rp-num span { font-size: 0.75rem; color: var(--ink-500); }
    .rp-linha { display: flex; gap: var(--s-2); align-items: center; margin-bottom: var(--s-2); flex-wrap: wrap; }
    .rp-linha input, .rp-linha select { border: 1.5px solid var(--ink-200); border-radius: 10px; padding: 9px 10px; font: inherit; color: var(--ink-900); background: var(--white); min-height: 40px; }
    .rp-linha input { flex: 1 1 220px; }
  `,
})
export class RelatorioPosEvento {
  private readonly service = inject(EmergenciaService);

  readonly emergencia = input.required<EmergenciaDto>();
  readonly podeEscrever = input(false);
  readonly podeFinalizar = input(false);
  readonly podeRevisarPlano = input(false);

  protected readonly tipos = TIPOS_MOBILIZACAO as Record<TipoMobilizacao, string>;
  protected readonly relatorio = signal<RelatorioPosEventoDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly resumo = signal('');
  protected readonly problemas = signal('');
  protected readonly licoes = signal('');
  protected readonly recomendacoes = signal<Recomendacao[]>([]);
  protected readonly editavel = computed(() => this.podeEscrever() && this.relatorio()?.situacao !== 'FINALIZADO');

  constructor() {
    effect(() => {
      const e = this.emergencia();
      untracked(() => this.carregar(e.uuid));
    });
  }

  protected rotuloTipo(chave: string): string {
    return this.tipos[chave as TipoMobilizacao] ?? chave;
  }

  protected mudar(n: number, campo: keyof Recomendacao, ev: Event): void {
    const l = [...this.recomendacoes()];
    l[n] = { ...l[n], [campo]: (ev.target as HTMLInputElement).value };
    this.recomendacoes.set(l);
  }

  protected remover(n: number): void {
    this.recomendacoes.set(this.recomendacoes().filter((_, j) => j !== n));
  }

  protected salvar(): void {
    if (this.enviando()) return;
    this.enviando.set(true);
    this.erro.set(null);
    this.service.salvarRelatorio(this.emergencia().uuid, {
      resumo: this.resumo().trim() || null, problemas: this.problemas().trim() || null, licoes: this.licoes().trim() || null,
      recomendacoes: this.recomendacoes().filter((r) => r.descricao.trim()).map((r) => ({ descricao: r.descricao.trim(), planoCodigo: r.planoCodigo || null })),
    }).subscribe({
      next: (r) => {
        this.enviando.set(false);
        this.aplicar(r);
      },
      error: (e: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set(mensagem(e));
      },
    });
  }

  protected finalizar(): void {
    this.erro.set(null);
    this.service.finalizarRelatorio(this.emergencia().uuid).subscribe({ next: (r) => this.aplicar(r), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }

  protected revisar(recomendacaoId: string): void {
    this.erro.set(null);
    this.service.revisarPlanoDaRecomendacao(this.emergencia().uuid, recomendacaoId).subscribe({
      next: (r) => this.aplicar(r), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)),
    });
  }

  protected exportar(): void {
    this.service.relatorioCsv(this.emergencia().uuid).subscribe({
      next: (r) => {
        const url = URL.createObjectURL(r.body ?? new Blob());
        const link = document.createElement('a');
        link.href = url;
        link.download = 'relatorio-pos-evento.csv';
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.erro.set('Não foi possível exportar os números.'),
    });
  }

  private carregar(uuid: string): void {
    this.service.relatorio(uuid).subscribe({ next: (r) => this.aplicar(r), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }

  private aplicar(r: RelatorioPosEventoDto): void {
    this.relatorio.set(r);
    this.resumo.set(r.resumo ?? '');
    this.problemas.set(r.problemas ?? '');
    this.licoes.set(r.licoes ?? '');
    this.recomendacoes.set(r.recomendacoes.map((x) => ({ descricao: x.descricao, planoCodigo: x.planoCodigo ?? '' })));
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
