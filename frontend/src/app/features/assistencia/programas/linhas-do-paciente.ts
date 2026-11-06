import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { LinhaCuidadoDto, PercursoLinhaDto, SITUACOES_PERCURSO } from '../../../core/models/programa';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { LinhaCuidadoService } from '../../../core/services/linha-cuidado';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { PercursoLinha } from './percurso-linha';

/**
 * As linhas de cuidado do paciente, na aba Programas (ADR-0152): os percursos com a etapa atual e o atraso; abrir o
 * percurso mostra as etapas; quem acompanha abre uma linha nova.
 */
@Component({
  selector: 'app-linhas-do-paciente',
  imports: [Drawer, PercursoLinha],
  template: `
    <div class="lp-cab">
      <h4 class="mo-grupo">Linhas de cuidado</h4>
      @if (podeAcompanhar()) { <button type="button" class="link-btn" (click)="iniciarAbertura()">Abrir linha de cuidado</button> }
    </div>
    @if (percursos(); as lista) {
      <ul class="eq-prof" aria-label="Linhas de cuidado do paciente">
        @for (p of lista; track p.uuid) {
          <li class="mo-row">
            <span><span class="cell-main">{{ p.linhaNome }}</span>
              <span class="cell-sub">desde {{ data(p.inicio) }} · {{ p.unidadeNome }} · {{ p.obrigatoriasFeitas }} de {{ p.obrigatorias }}@if (p.etapaAtual && p.situacao === 'EM_CURSO') { · próxima: {{ p.etapaAtual }} }</span></span>
            <span>
              <span [class]="'tag tag--' + situacoes[p.situacao].classe">{{ situacoes[p.situacao].rotulo }}</span>
              @if (p.atrasado) { <span class="tag tag--alert">Atrasado</span> }
              <button type="button" class="link-btn" (click)="aberto.set(p.uuid)" [attr.aria-label]="'Ver percurso: ' + p.linhaNome">Ver</button>
            </span>
          </li>
        } @empty { <li class="cell-sub">Nenhuma linha de cuidado.</li> }
      </ul>
    } @else if (percursos() === null) {
      <p class="cell-sub">Seu acesso não inclui as linhas de cuidado do paciente.</p>
    }

    @if (aberto(); as id) {
      <app-drawer titulo="Percurso na linha de cuidado" (fechar)="aberto.set(null)">
        <div class="dw-body"><app-percurso-linha [percursoId]="id" [podeAcompanhar]="podeAcompanhar()" (alterado)="carregar()" /></div>
      </app-drawer>
    }
    @if (abrindo()) {
      <app-drawer titulo="Abrir linha de cuidado" (fechar)="abrindo.set(false)">
        <form class="dw-form" (submit)="$event.preventDefault(); abrir()" novalidate>
          <div class="dw-body">
            @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
            <div class="dw-field"><label for="f-lp-linha">Linha</label>
              <select id="f-lp-linha" (change)="linhaId.set($any($event.target).value)">
                <option value="" [selected]="!linhaId()">Escolha…</option>
                @for (l of linhas(); track l.uuid) { @if (l.ativa) { <option [value]="l.uuid" [selected]="l.uuid === linhaId()">{{ l.nome }}</option> } }
              </select></div>
            <div class="dw-field"><label for="f-lp-inicio">Início (última menstruação, nascimento, suspeita)</label>
              <input id="f-lp-inicio" type="date" [value]="inicio()" (input)="inicio.set($any($event.target).value)" /></div>
            <div class="dw-field"><label for="f-lp-unidade">Unidade responsável</label>
              <select id="f-lp-unidade" (change)="unidadeId.set($any($event.target).value)">
                <option value="" [selected]="!unidadeId()">Escolha…</option>
                @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="u.uuid === unidadeId()">{{ u.nome }}</option> }
              </select></div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="abrindo.set(false)">Cancelar</button>
            <button type="submit" class="btn" [disabled]="!linhaId() || !inicio() || !unidadeId()">Abrir</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `.lp-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-3); margin-top: var(--s-4); }`,
})
export class LinhasDoPaciente {
  private readonly service = inject(LinhaCuidadoService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  readonly pacienteId = input.required<string>();
  readonly podeAcompanhar = input(false);

  protected readonly situacoes = SITUACOES_PERCURSO;
  protected readonly percursos = signal<PercursoLinhaDto[] | null | undefined>(undefined);
  protected readonly aberto = signal<string | null>(null);
  protected readonly abrindo = signal(false);
  protected readonly linhas = signal<LinhaCuidadoDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly linhaId = signal('');
  protected readonly inicio = signal('');
  protected readonly unidadeId = signal('');
  protected readonly erro = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.pacienteId();
      untracked(() => this.carregar());
    });
  }

  protected carregar(): void {
    this.service.doPaciente(this.pacienteId()).pipe(catchError(() => of(null))).subscribe((l) => this.percursos.set(l));
  }

  protected iniciarAbertura(): void {
    this.erro.set(null);
    this.linhaId.set('');
    this.inicio.set('');
    if (!this.linhas().length) this.service.linhas().pipe(catchError(() => of([]))).subscribe((l) => this.linhas.set(l));
    if (!this.unidades().length) {
      this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => {
        this.unidades.set(l);
        if (l.length === 1) this.unidadeId.set(l[0].uuid);
      });
    }
    this.abrindo.set(true);
  }

  protected abrir(): void {
    this.service.abrir({ pacienteId: this.pacienteId(), linhaId: this.linhaId(), inicio: this.inicio(), unidadeId: this.unidadeId() }).subscribe({
      next: (p) => {
        this.abrindo.set(false);
        this.carregar();
        this.aberto.set(p.uuid);
      },
      error: (e: HttpErrorResponse) =>
        this.erro.set(e.status === 403 ? 'Seu acesso não inclui esta unidade.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível abrir.')),
    });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}
