import { Component, input } from '@angular/core';
import { EventoLinhaDoTempoDto } from '../../core/models/vigilancia';

/** Linha do tempo do caso ou do surto (ADR-0131), da mais antiga à mais recente. */
@Component({
  selector: 'app-linha-do-tempo',
  template: `
    @if (eventos().length) {
      <ol class="eq-prof" [attr.aria-label]="rotulo()" style="list-style: none; padding-left: 0;">
        @for (e of eventos(); track $index) {
          <li style="display: block; border-left: 2px solid var(--ink-200); padding-left: var(--s-3); margin-left: var(--s-1);">
            <span class="cell-main">{{ e.titulo }}</span>
            <span class="cell-sub">{{ quando(e.em) }}@if (e.autor) { · {{ e.autor }} }@if (e.detalhe) { · {{ e.detalhe }} }</span>
          </li>
        }
      </ol>
    } @else {
      <p class="cell-sub">Nada registrado ainda.</p>
    }
  `,
})
export class LinhaDoTempo {
  readonly eventos = input<EventoLinhaDoTempoDto[]>([]);
  readonly rotulo = input('Linha do tempo');

  protected quando(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
