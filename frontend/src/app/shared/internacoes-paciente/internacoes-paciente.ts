import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EVENTOS_LEITO, InternacaoResponseDto, TIPOS_ALTA } from '../../core/models/internacao';

/**
 * Internações do paciente (ADR-0100), no prontuário e no atendimento: leito, permanência, CID, médico responsável,
 * motivo e, depois da alta, o tipo e o sumário. Os movimentos ficam recolhidos.
 */
@Component({
  selector: 'app-internacoes-paciente',
  imports: [RouterLink],
  templateUrl: './internacoes-paciente.html',
})
export class InternacoesPaciente {
  @Input({ required: true }) internacoes: InternacaoResponseDto[] = [];
  @Input() vazio = 'Nenhuma internação.';

  protected readonly tiposAlta = TIPOS_ALTA;
  protected readonly eventos = EVENTOS_LEITO;

  protected instante(iso: string | null | undefined): string {
    if (!iso) return '—';
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`;
  }
}
