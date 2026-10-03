import { Component, Input } from '@angular/core';
import {
  ExameProntuarioDto,
  INTERPRETACOES,
  MATERIAIS,
  STATUS_ITEM,
  faixaReferencia,
  valorResultado,
} from '../../core/models/laboratorio';

/**
 * Exames laboratoriais do paciente (ADR-0095), no prontuário e no atendimento: situação de cada exame e, depois da
 * liberação, valor, referência, interpretação e o laudo do pedido. Cancelados ficam de fora.
 */
@Component({
  selector: 'app-exames-paciente',
  templateUrl: './exames-paciente.html',
})
export class ExamesPaciente {
  @Input({ required: true }) exames: ExameProntuarioDto[] = [];
  @Input() vazio = 'Nenhum exame pedido.';

  protected readonly materiais = MATERIAIS;
  protected readonly statusItem = STATUS_ITEM;
  protected readonly interpretacoes = INTERPRETACOES;
  protected readonly faixa = faixaReferencia;
  protected readonly valor = valorResultado;

  protected visiveis(): ExameProntuarioDto[] {
    return this.exames.filter((e) => e.status !== 'CANCELADO');
  }

  protected data(iso: string): string {
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
  }
}
