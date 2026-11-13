import { Component, Input, OnChanges, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { DeslocamentoAbertoDto, LOCAIS_DESLOCAMENTO } from '../../core/models/emergencia';
import { EmergenciaService } from '../../core/services/emergencia';

/** Aviso no prontuário (ADR-0193): o paciente está fora de casa por uma emergência, e onde. Sem deslocamento em aberto, não aparece. */
@Component({
  selector: 'app-deslocamento-paciente',
  template: `
    @for (d of lista(); track d.uuid) {
      <div class="box box--warn" role="status">
        <div><strong>Deslocado na emergência {{ d.emergenciaNome }}</strong> · {{ locais[d.destinoTipo] }}: {{ d.destino }} · desde {{ quando(d.saidaEm) }}</div>
      </div>
    }
  `,
})
export class DeslocamentoPaciente implements OnChanges {
  private readonly service = inject(EmergenciaService);

  @Input({ required: true }) pacienteId = '';

  protected readonly locais = LOCAIS_DESLOCAMENTO;
  protected readonly lista = signal<DeslocamentoAbertoDto[]>([]);

  ngOnChanges(): void {
    this.lista.set([]);
    if (!this.pacienteId) return;
    this.service.deslocamentosDoPaciente(this.pacienteId).pipe(catchError(() => of([] as DeslocamentoAbertoDto[]))).subscribe((l) => this.lista.set(l));
  }

  protected quando(iso: string): string {
    return new Date(iso).toLocaleDateString('pt-BR');
  }
}
