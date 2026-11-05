import { Component, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { DESFECHOS_OBSERVACAO, ESPECIES_ANIMAL, ObservacaoAnimalDto } from '../../core/models/zoonoses';
import { ZoonosesService } from '../../core/services/zoonoses';

/**
 * O animal agressor do atendimento antirrábico notificado (ADR-0163): a observação de 10 dias e o desfecho, para quem
 * investiga decidir o esquema da pessoa. Quem registra é a equipe de zoonoses.
 */
@Component({
  selector: 'app-animal-agressor',
  imports: [RouterLink],
  template: `
    <h4 class="mo-grupo">Animal agressor</h4>
    @for (o of observacoes(); track o.uuid) {
      <div class="box box--info" aria-label="Observação do animal agressor">
        <div>
          <strong>{{ o.animalNome || especies[o.especie] }}</strong> · agressão em {{ data(o.dataAgressao) }} · observação até {{ data(o.fimPrevisto) }}.
          @if (o.desfecho) { Desfecho: <span [class]="'tag tag--' + desfechos[o.desfecho].classe">{{ desfechos[o.desfecho].rotulo }}</span> em {{ data(o.desfechoEm!) }}. }
          @else if (o.vencida) { <span class="tag tag--alert">Observação vencida sem desfecho</span> }
          @else { <span class="tag tag--info">Em observação</span> }
        </div>
      </div>
    } @empty {
      <p class="cell-sub">Nenhum animal em observação ligado a este atendimento. A equipe de zoonoses registra em <a routerLink="/vigilancia/vetores">Zoonoses e vetores</a>.</p>
    }
  `,
})
export class AnimalAgressor {
  private readonly service = inject(ZoonosesService);

  readonly notificacaoId = input.required<string>();

  protected readonly especies = ESPECIES_ANIMAL;
  protected readonly desfechos = DESFECHOS_OBSERVACAO;
  protected readonly observacoes = signal<ObservacaoAnimalDto[]>([]);

  constructor() {
    effect(() => {
      const id = this.notificacaoId();
      this.service.observacoes({ notificacaoId: id }).pipe(catchError(() => of([]))).subscribe((l) => this.observacoes.set(l));
    });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}
