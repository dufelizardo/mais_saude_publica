import { Component, Input, OnChanges, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import {
  MOTIVOS_TRANSPORTE,
  SITUACOES_SOLICITACAO_TRANSPORTE,
  SolicitacaoTransporteDto,
  TRECHOS_TRANSPORTE,
} from '../../core/models/transporte';
import { TransporteService } from '../../core/services/transporte';

/**
 * Transportes do paciente no prontuário (ADR-0190): o motivo, quando, para onde e a situação (realizado, faltou, programado). Sem
 * acesso ao transporte ou sem pedidos, não aparece.
 */
@Component({
  selector: 'app-transportes-paciente',
  template: `
    @if (lista().length) {
      <section class="pront-exames" aria-labelledby="pront-transportes-h">
        <h4 id="pront-transportes-h">Transportes</h4>
        <ul class="eq-prof" aria-label="Transportes do paciente">
          @for (s of lista(); track s.uuid) {
            <li class="mo-row">
              <span>
                <span class="cell-main">{{ motivos[s.motivo] }} · {{ quando(s.dataHora) }}</span>
                <span class="cell-sub">{{ trechos[s.trecho] }} para {{ s.destino }}@if (s.acompanhanteNome) { · com acompanhante }@if (s.motivoSituacao) { · {{ s.motivoSituacao }} }</span>
              </span>
              <span [class]="'badge badge--' + situacoes[s.situacao].classe">{{ situacoes[s.situacao].rotulo }}</span>
            </li>
          }
        </ul>
      </section>
    }
  `,
})
export class TransportesPaciente implements OnChanges {
  private readonly service = inject(TransporteService);

  @Input({ required: true }) pacienteId = '';

  protected readonly motivos = MOTIVOS_TRANSPORTE;
  protected readonly trechos = TRECHOS_TRANSPORTE;
  protected readonly situacoes = SITUACOES_SOLICITACAO_TRANSPORTE;
  protected readonly lista = signal<SolicitacaoTransporteDto[]>([]);

  ngOnChanges(): void {
    this.lista.set([]);
    if (!this.pacienteId) return;
    this.service.solicitacoes({ pacienteId: this.pacienteId }).pipe(catchError(() => of([] as SolicitacaoTransporteDto[])))
      .subscribe((l) => this.lista.set(l.slice(0, 20)));
  }

  protected quando(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
