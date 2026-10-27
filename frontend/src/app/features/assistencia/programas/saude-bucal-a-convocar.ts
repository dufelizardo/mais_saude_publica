import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { ConvocacaoSaudeBucalDto, NECESSIDADES_TRATAMENTO, RISCOS_SAUDE_BUCAL } from '../../../core/models/saude-bucal';
import { SaudeBucalService } from '../../../core/services/saude-bucal';

/**
 * A convocar (ADR-0140): quem a avaliação coletiva de saúde bucal apontou com necessidade de tratamento e ainda não tem
 * plano de tratamento aberto, na unidade. A urgência primeiro; o atalho abre o prontuário do paciente para o atendimento.
 */
@Component({
  selector: 'app-saude-bucal-a-convocar',
  imports: [RouterLink],
  template: `
    @if (!unidadeId()) {
      <p class="cell-sub">Escolha a unidade para ver quem convocar.</p>
    } @else {
      <p class="cell-sub">Da avaliação coletiva (escola, creche, comunidade), quem precisa de tratamento odontológico e ainda não tem plano aberto.</p>
      <ul class="eq-prof" aria-label="A convocar em saúde bucal">
        @for (c of lista(); track c.pacienteId) {
          <li class="mo-row">
            <span>
              <span class="cell-main">{{ c.pacienteNome }} <span [class]="'tag tag--' + riscos[c.risco].classe">{{ riscos[c.risco].rotulo }}</span></span>
              <span class="cell-sub">{{ necessidades[c.necessidadeTratamento] }} · {{ c.programaNome }}@if (c.local) { · {{ c.local }} }@if (c.realizadaEm) { · {{ data(c.realizadaEm) }} }</span>
            </span>
            <a class="link-btn" routerLink="/assistencia/atendimentos" [queryParams]="{ aba: 'pront', pacienteId: c.pacienteId }" [attr.aria-label]="'Abrir prontuário: ' + c.pacienteNome">Abrir prontuário</a>
          </li>
        } @empty { <li class="cell-sub">Ninguém a convocar.</li> }
      </ul>
    }
  `,
})
export class SaudeBucalAConvocar {
  private readonly service = inject(SaudeBucalService);

  readonly unidadeId = input('');

  protected readonly riscos = RISCOS_SAUDE_BUCAL;
  protected readonly necessidades = NECESSIDADES_TRATAMENTO;
  protected readonly lista = signal<ConvocacaoSaudeBucalDto[]>([]);

  constructor() {
    effect(() => {
      const u = this.unidadeId();
      untracked(() => {
        if (!u) {
          this.lista.set([]);
          return;
        }
        this.service.aConvocar(u).pipe(catchError(() => of([]))).subscribe((l) => this.lista.set(l));
      });
    });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}
