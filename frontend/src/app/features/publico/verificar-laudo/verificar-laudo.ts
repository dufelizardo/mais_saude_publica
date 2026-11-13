import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { VerificacaoLaudoDto } from '../../../core/models/laboratorio';
import { LaboratorioService } from '../../../core/services/laboratorio';

/**
 * Verificação pública do laudo pelo código impresso no rodapé ou lido no QR code (ADR-0183). Sem login e sem o nome do
 * paciente: diz se o laudo vale ou foi substituído, com a unidade, os exames e o hash do PDF para conferir.
 */
@Component({
  selector: 'app-verificar-laudo',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './verificar-laudo.html',
})
export class VerificarLaudo {
  private readonly fb = inject(FormBuilder);
  private readonly laboratorioService = inject(LaboratorioService);

  protected readonly form = this.fb.nonNullable.group({ codigo: [inject(ActivatedRoute).snapshot.queryParamMap.get('codigo') ?? ''] });
  protected readonly resultado = signal<VerificacaoLaudoDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly consultando = signal(false);

  constructor() {
    if (this.form.controls.codigo.value) this.verificar();
  }

  protected verificar(): void {
    const codigo = this.form.controls.codigo.value.trim().toUpperCase();
    this.resultado.set(null);
    this.erro.set(null);
    if (!/^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$/.test(codigo)) {
      this.erro.set('Digite o código como está no rodapé do laudo, por exemplo ABCD-EFGH-JKLM.');
      setTimeout(() => document.getElementById('f-codigo')?.focus());
      return;
    }
    this.consultando.set(true);
    this.laboratorioService.verificarLaudo(codigo).subscribe({
      next: (r) => {
        this.consultando.set(false);
        this.resultado.set(r);
      },
      error: (e: HttpErrorResponse) => {
        this.consultando.set(false);
        this.erro.set(e.status === 404 ? 'Nenhum laudo foi emitido com este código. Confira as letras e os números.' : 'Não foi possível verificar agora. Tente de novo.');
      },
    });
  }

  protected dataHora(iso: string): string {
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} às ${p(d.getHours())}:${p(d.getMinutes())}`;
  }
}
