import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ErrorResponseDto, ProfissionalResponseDto } from '../../core/models/profissional';
import { ProfissionalService } from '../../core/services/profissional';
import { formatCpf } from '../../shared/format-mask';

@Component({
  selector: 'app-profissional-desligar',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profissional-desligar.html',
  styleUrl: './profissional-desligar.css',
})
export class ProfissionalDesligar {
  private readonly fb = inject(FormBuilder);
  private readonly profissionalService = inject(ProfissionalService);

  protected readonly buscando = signal(false);
  protected readonly naoEncontrado = signal(false);
  protected readonly profissional = signal<ProfissionalResponseDto | null>(null);

  protected readonly submitting = signal(false);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly cpfForm = this.fb.nonNullable.group({
    cpf: ['', [Validators.required]],
  });

  protected readonly desligamentoForm = this.fb.nonNullable.group({
    dataDesligamento: [this.hoje(), [Validators.required]],
  });

  protected onCpfInput(event: Event): void {
    const valor = formatCpf((event.target as HTMLInputElement).value);
    this.cpfForm.controls.cpf.setValue(valor);
  }

  protected buscar(): void {
    if (this.cpfForm.invalid) {
      this.cpfForm.markAllAsTouched();
      return;
    }

    this.buscando.set(true);
    this.naoEncontrado.set(false);
    this.profissional.set(null);
    this.successMessage.set(null);
    this.errorMessage.set(null);

    const cpf = this.cpfForm.getRawValue().cpf;
    this.profissionalService.buscarPorCpf(cpf).subscribe({
      next: (profissional) => {
        this.buscando.set(false);
        this.profissional.set(profissional);
      },
      error: () => {
        this.buscando.set(false);
        this.naoEncontrado.set(true);
      },
    });
  }

  protected confirmarDesligamento(): void {
    const profissional = this.profissional();
    if (this.desligamentoForm.invalid || !profissional) {
      this.desligamentoForm.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const dataDesligamento = this.desligamentoForm.getRawValue().dataDesligamento;

    this.profissionalService.desligar(profissional.cpf, dataDesligamento).subscribe({
      next: (response) => {
        this.submitting.set(false);
        this.successMessage.set(response.details);
        this.profissional.set(null);
        this.cpfForm.reset({ cpf: '' });
        this.desligamentoForm.reset({ dataDesligamento: this.hoje() });
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível desligar o profissional. Tente novamente.');
      },
    });
  }

  private hoje(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
