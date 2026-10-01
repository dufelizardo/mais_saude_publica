import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ErrorResponseDto } from '../../core/models/profissional';
import { AcessoProntuarioDto, MotivoAcessoJustificado } from '../../core/models/prontuario';
import { ProntuarioService } from '../../core/services/prontuario';

const MOTIVOS: { valor: MotivoAcessoJustificado; rotulo: string }[] = [
  { valor: 'EMERGENCIA', rotulo: 'Emergência' },
  { valor: 'CONTINUIDADE_DO_CUIDADO', rotulo: 'Continuidade do cuidado' },
  { valor: 'REGULACAO_OU_ENCAMINHAMENTO', rotulo: 'Regulação ou encaminhamento' },
  { valor: 'OUTRO', rotulo: 'Outro' },
];

/** Mesmo mínimo do backend (app.security.prontuario-por-vinculo.justificativa-minimo). */
const MINIMO = 20;

/**
 * Prontuário por vínculo assistencial (ADR-0076), no lugar onde o prontuário aparece:
 * - com {@link bloqueio}: o aviso de que falta vínculo e o formulário do acesso justificado (inline, porque
 *   o prontuário às vezes já está dentro de uma gaveta); ao registrar, emite {@link liberado};
 * - com {@link acesso} justificado: a faixa "acesso justificado até…", lembrando que ficou na auditoria.
 */
@Component({
  selector: 'app-acesso-prontuario',
  imports: [ReactiveFormsModule],
  templateUrl: './acesso-prontuario.html',
})
export class AcessoProntuario {
  private readonly fb = inject(FormBuilder);
  private readonly prontuarioService = inject(ProntuarioService);

  @Input({ required: true }) pacienteId!: string;
  /** Mensagem da recusa por falta de vínculo; null quando não houve recusa. */
  @Input() bloqueio: string | null = null;
  @Input() acesso: AcessoProntuarioDto | null | undefined = null;
  @Output() liberado = new EventEmitter<void>();

  protected readonly motivos = MOTIVOS;
  protected readonly minimo = MINIMO;
  protected readonly aberto = signal(false);
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroTexto = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    motivo: ['EMERGENCIA' as MotivoAcessoJustificado],
    justificativa: [''],
  });

  protected hora(iso: string | null | undefined): string {
    if (!iso) return '';
    return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  protected abrir(): void {
    this.form.reset({ motivo: 'EMERGENCIA', justificativa: '' });
    this.erro.set(null);
    this.erroTexto.set(null);
    this.aberto.set(true);
    setTimeout(() => document.getElementById('f-acesso-motivo-' + this.pacienteId)?.focus());
  }

  protected enviar(): void {
    const v = this.form.getRawValue();
    const texto = v.justificativa.trim();
    if (texto.length < MINIMO) {
      this.erroTexto.set(`Descreva com pelo menos ${MINIMO} caracteres (faltam ${MINIMO - texto.length}).`);
      setTimeout(() => document.getElementById('f-acesso-texto-' + this.pacienteId)?.focus());
      return;
    }
    this.erroTexto.set(null);
    this.enviando.set(true);
    this.prontuarioService.justificarAcesso(this.pacienteId, { motivo: v.motivo, justificativa: texto }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.aberto.set(false);
        this.liberado.emit();
      },
      error: (e: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível registrar o acesso. Tente novamente.');
      },
    });
  }
}
