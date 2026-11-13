import { Component, Input, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import {
  ACEITACOES_DIETA,
  EVENTOS_LEITO,
  InternacaoResponseDto,
  MOTIVOS_NAO_ADMINISTRACAO_HOSP,
  PrescricaoHospitalarDto,
  TIPOS_ALTA,
} from '../../core/models/internacao';
import { InternacaoService } from '../../core/services/internacao';

/**
 * Internações do paciente (ADR-0100), no prontuário e no atendimento: leito, permanência, CID, médico responsável,
 * motivo, número da AIH (ADR-0186) e, depois da alta, o tipo e o sumário. Os movimentos e as prescrições com as checagens
 * (ADR-0187) ficam recolhidos; as prescrições carregam ao abrir.
 */
@Component({
  selector: 'app-internacoes-paciente',
  imports: [RouterLink],
  templateUrl: './internacoes-paciente.html',
})
export class InternacoesPaciente {
  private readonly internacaoService = inject(InternacaoService);

  @Input({ required: true }) internacoes: InternacaoResponseDto[] = [];
  @Input() vazio = 'Nenhuma internação.';

  protected readonly tiposAlta = TIPOS_ALTA;
  protected readonly eventos = EVENTOS_LEITO;
  protected readonly aceitacoes = ACEITACOES_DIETA;
  protected readonly motivosNaoAdm = MOTIVOS_NAO_ADMINISTRACAO_HOSP;
  protected readonly prescricoes = signal<Record<string, PrescricaoHospitalarDto[] | 'erro'>>({});

  protected carregarPrescricoes(internacaoId: string, aberto: boolean): void {
    if (!aberto || this.prescricoes()[internacaoId]) return;
    this.internacaoService.prescricoes(internacaoId).pipe(catchError(() => of('erro' as const)))
      .subscribe((p) => this.prescricoes.update((m) => ({ ...m, [internacaoId]: p })));
  }

  protected listaDe(internacaoId: string): PrescricaoHospitalarDto[] {
    const p = this.prescricoes()[internacaoId];
    return Array.isArray(p) ? p : [];
  }

  protected instante(iso: string | null | undefined): string {
    if (!iso) return '—';
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`;
  }
}
