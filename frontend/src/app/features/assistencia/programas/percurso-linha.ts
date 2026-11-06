import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, input, output, signal, untracked } from '@angular/core';
import { EtapaPercursoDto, PercursoLinhaDto, SITUACOES_ETAPA, SITUACOES_PERCURSO, TIPOS_ETAPA } from '../../../core/models/programa';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { LinhaCuidadoService } from '../../../core/services/linha-cuidado';

type Acao = { tipo: 'CUMPRIDA' | 'DISPENSADA'; etapa: EtapaPercursoDto } | { tipo: 'CONCLUIDO' | 'INTERROMPIDO' } | null;

/**
 * O percurso do paciente na linha de cuidado (ADR-0152): as etapas na ordem, com a situação calculada, o prazo e o que
 * cumpriu cada uma; quem acompanha registra a etapa manual, dispensa com o motivo e encerra.
 */
@Component({
  selector: 'app-percurso-linha',
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (percurso(); as p) {
      <dl class="ro">
        <div><dt>Paciente</dt><dd>{{ p.pacienteNome }}</dd></div>
        <div><dt>Linha</dt><dd>{{ p.linhaNome }}@if (p.sensivel) { <span class="tag tag--purple">Sensível</span> }</dd></div>
        <div><dt>Início</dt><dd>{{ data(p.inicio) }} · {{ p.unidadeNome }}@if (p.equipeNome) { · {{ p.equipeNome }} }</dd></div>
        <div><dt>Situação</dt><dd><span [class]="'tag tag--' + situacoesPercurso[p.situacao].classe">{{ situacoesPercurso[p.situacao].rotulo }}</span>
          @if (p.atrasado) { <span class="tag tag--alert">Atrasado</span> } · {{ p.obrigatoriasFeitas }} de {{ p.obrigatorias }} obrigatórias@if (p.motivoEncerramento) { · {{ p.motivoEncerramento }} }</dd></div>
      </dl>
      <ol class="pl-regua" aria-label="Etapas do percurso">
        @for (e of p.etapas; track e.etapaId) {
          <li class="pl-etapa">
            <div class="pl-etapa__cab">
              <span class="cell-main">{{ e.ordem }}. {{ e.nome }}@if (!e.obrigatoria) { <span class="cell-sub" style="display: inline;"> (opcional)</span> }</span>
              <span [class]="'tag tag--' + situacoes[e.situacao].classe">{{ situacoes[e.situacao].rotulo }}</span>
            </div>
            <span class="cell-sub">{{ tipos[e.tipo] }}@if (e.quantidade > 1) { · {{ e.realizadas }} de {{ e.quantidade }} }@if (e.prazo) { · até {{ data(e.prazo) }} }@if (e.cumpridaEm) { · cumprida em {{ data(e.cumpridaEm) }} }</span>
            @if (e.evidencia) { <span class="cell-sub">{{ e.evidencia }}</span> }
            @if (e.observacao) { <span class="cell-sub">Motivo: {{ e.observacao }}</span> }
            @if (podeAcompanhar() && p.situacao === 'EM_CURSO' && e.situacao !== 'CUMPRIDA' && e.situacao !== 'DISPENSADA') {
              <span class="row-actions">
                @if (e.tipo === 'MANUAL') { <button type="button" class="link-btn" (click)="iniciar({ tipo: 'CUMPRIDA', etapa: e })" [attr.aria-label]="'Registrar ' + e.nome">Registrar</button> }
                <button type="button" class="link-btn" (click)="iniciar({ tipo: 'DISPENSADA', etapa: e })" [attr.aria-label]="'Dispensar ' + e.nome">Dispensar</button>
              </span>
            }
          </li>
        }
      </ol>
      @if (acao(); as a) {
        <form class="pl-acao" (submit)="$event.preventDefault(); confirmar(p, a)" novalidate>
          <div class="dw-field">
            <label for="f-pl-observacao">{{ rotuloAcao(a) }}</label>
            <input id="f-pl-observacao" type="text" maxlength="500" [value]="texto()" (input)="texto.set($any($event.target).value)" />
          </div>
          <div class="row-actions">
            <button type="button" class="btn btn--ghost btn--sm" (click)="acao.set(null)">Cancelar</button>
            <button type="submit" class="btn btn--sm">Confirmar</button>
          </div>
        </form>
      } @else if (podeAcompanhar() && p.situacao === 'EM_CURSO') {
        <div class="row-actions" style="margin-top: var(--s-3);">
          <button type="button" class="btn btn--ghost btn--sm" (click)="iniciar({ tipo: 'INTERROMPIDO' })">Interromper</button>
          <button type="button" class="btn btn--sm" (click)="iniciar({ tipo: 'CONCLUIDO' })">Concluir o percurso</button>
        </div>
      }
    } @else if (!erro()) {
      <p class="cell-sub">Carregando o percurso…</p>
    }
  `,
  styles: `
    .pl-regua { list-style: none; margin: var(--s-3) 0 0; padding: 0; display: grid; gap: 8px; }
    .pl-etapa { display: grid; gap: 2px; padding: 8px 10px; border: 1px solid var(--ink-100); border-radius: 8px; }
    .pl-etapa__cab { display: flex; justify-content: space-between; gap: var(--s-2); align-items: center; }
    .pl-acao { margin-top: var(--s-3); padding: var(--s-3); border: 1px dashed var(--ink-200); border-radius: 8px; }
  `,
})
export class PercursoLinha {
  private readonly service = inject(LinhaCuidadoService);

  readonly percursoId = input.required<string>();
  readonly podeAcompanhar = input(false);
  readonly alterado = output<PercursoLinhaDto>();

  protected readonly situacoes = SITUACOES_ETAPA;
  protected readonly situacoesPercurso = SITUACOES_PERCURSO;
  protected readonly tipos = TIPOS_ETAPA;
  protected readonly percurso = signal<PercursoLinhaDto | null>(null);
  protected readonly acao = signal<Acao>(null);
  protected readonly texto = signal('');
  protected readonly erro = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.percursoId();
      untracked(() => {
        this.percurso.set(null);
        this.erro.set(null);
        this.service.percurso(id).subscribe({ next: (p) => this.percurso.set(p), error: (e: HttpErrorResponse) => this.falhou(e) });
      });
    });
  }

  protected iniciar(a: Exclude<Acao, null>): void {
    this.texto.set('');
    this.erro.set(null);
    this.acao.set(a);
  }

  protected rotuloAcao(a: Exclude<Acao, null>): string {
    switch (a.tipo) {
      case 'CUMPRIDA': return `Registrar "${a.etapa.nome}" · observação (opcional)`;
      case 'DISPENSADA': return `Dispensar "${a.etapa.nome}" · motivo`;
      case 'INTERROMPIDO': return 'Interromper o percurso · motivo';
      default: return 'Concluir o percurso · observação (opcional)';
    }
  }

  protected confirmar(p: PercursoLinhaDto, a: Exclude<Acao, null>): void {
    const texto = this.texto().trim() || null;
    const chamada = a.tipo === 'CUMPRIDA' || a.tipo === 'DISPENSADA'
      ? this.service.registrarEtapa(p.uuid, a.etapa.etapaId, a.tipo, texto)
      : this.service.encerrar(p.uuid, a.tipo, texto);
    chamada.subscribe({
      next: (novo) => {
        this.percurso.set(novo);
        this.acao.set(null);
        this.alterado.emit(novo);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  private falhou(e: HttpErrorResponse): void {
    this.erro.set(e.status === 403 ? 'Seu acesso não inclui este percurso.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.'));
  }
}
