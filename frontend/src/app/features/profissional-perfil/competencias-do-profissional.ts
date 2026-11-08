import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { CompetenciaDoProfissionalDto } from '../../core/models/educacao-permanente';
import { EducacaoPermanenteService } from '../../core/services/educacao-permanente';
import { dataBr } from '../../shared/formato';

/** Competências desenvolvidas, pelas participações em treinamento (ADR-0173). */
@Component({
  selector: 'app-competencias-do-profissional',
  template: `
    <h3 class="mo-grupo" id="h-competencias">Competências desenvolvidas</h3>
    @if (competencias() === undefined) {
      <p class="cell-sub">Carregando…</p>
    } @else if (!competencias()?.length) {
      <p class="cell-sub">Nenhuma competência: os treinamentos concluídos ainda não têm competências ligadas.</p>
    } @else {
      <ul class="eq-prof" aria-labelledby="h-competencias">
        @for (c of competencias(); track c.competenciaId) {
          <li class="mo-row">
            <span>
              <span class="cell-main">{{ c.nome }}</span>
              <span class="cell-sub">{{ c.treinamentoNome }} · concluído em {{ dataBr(c.dataConclusao) }}@if (c.dataValidade) { · válido até {{ dataBr(c.dataValidade) }} }</span>
            </span>
            <div style="flex: none;">@if (c.vencida) { <span class="badge badge--alert">Vencida</span> } @else { <span class="badge badge--ok"><span class="dot" aria-hidden="true"></span> Em dia</span> }</div>
          </li>
        }
      </ul>
    }
  `,
})
export class CompetenciasDoProfissional {
  private readonly service = inject(EducacaoPermanenteService);

  readonly matricula = input.required<string>();
  /** Muda quando uma participação nova é registrada. */
  readonly versao = input(0);

  protected readonly dataBr = dataBr;
  protected readonly competencias = signal<CompetenciaDoProfissionalDto[] | null | undefined>(undefined);

  constructor() {
    effect(() => {
      const m = this.matricula();
      this.versao();
      untracked(() => this.service.doProfissional(m).pipe(catchError(() => of(null))).subscribe((l) => this.competencias.set(l)));
    });
  }
}
