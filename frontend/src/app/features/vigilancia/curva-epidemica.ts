import { Component, computed, input } from '@angular/core';

export interface SemanaDaCurva {
  ano: number;
  semana: number;
  inicio: string;
  casos: number;
}

/** Curva epidêmica em barras, por semana epidemiológica (ADR-0130; reaproveitada no surto, ADR-0131). */
@Component({
  selector: 'app-curva-epidemica',
  template: `
    <div class="curva" role="list" [attr.aria-label]="rotulo()"
      style="display: flex; align-items: flex-end; gap: 4px; height: 120px; border-bottom: 1px solid var(--ink-200); padding-top: var(--s-2);">
      @for (s of curva(); track s.ano + '-' + s.semana) {
        <div role="listitem" [attr.aria-label]="'Semana ' + s.semana + '/' + s.ano + ': ' + s.casos + (s.casos === 1 ? ' caso' : ' casos')"
          [title]="'Semana ' + s.semana + '/' + s.ano + ' (de ' + data(s.inicio) + '): ' + s.casos"
          style="flex: 1; display: flex; flex-direction: column; justify-content: flex-end; align-items: center; height: 100%;">
          @if (s.casos) { <span class="mono" style="font-size: 11px;">{{ s.casos }}</span> }
          <div [style.height.%]="altura(s.casos)" style="width: 100%; min-height: 1px; background: var(--alert); border-radius: 2px 2px 0 0;"></div>
        </div>
      }
    </div>
    <div style="display: flex; justify-content: space-between;" class="cell-sub">
      @if (curva().length) {
        <span>SE {{ curva()[0].semana }}/{{ curva()[0].ano }}</span><span>SE {{ curva()[curva().length - 1].semana }}/{{ curva()[curva().length - 1].ano }}</span>
      }
    </div>
  `,
})
export class CurvaEpidemica {
  readonly curva = input<SemanaDaCurva[]>([]);
  readonly rotulo = input('Casos por semana epidemiológica');

  private readonly maximo = computed(() => Math.max(1, ...this.curva().map((s) => s.casos)));

  protected altura(casos: number): number {
    return casos ? Math.max(4, (casos / this.maximo()) * 85) : 0;
  }

  protected data(v: string): string {
    const [a, m, d] = v.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}
