import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { TreinamentoResponseDto } from '../../../core/models/treinamento';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { TreinamentoService } from '../../../core/services/treinamento';

@Component({
  selector: 'app-treinamentos',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './treinamentos.html',
  styleUrl: './treinamentos.css',
})
export class Treinamentos {
  private readonly fb = inject(FormBuilder);
  private readonly treinamentoService = inject(TreinamentoService);

  protected readonly treinamentos = signal<TreinamentoResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required]],
    cargaHoraria: [''],
    validadeMeses: [''],
    obrigatorio: [false],
  });

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.treinamentoService.listarCatalogo().subscribe({
      next: (treinamentos) => {
        this.treinamentos.set(treinamentos);
        this.carregando.set(false);
      },
      error: () => {
        this.treinamentos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.form.reset({ nome: '', cargaHoraria: '', validadeMeses: '', obrigatorio: false });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected fecharModal(): void {
    this.modalAberto.set(false);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    this.treinamentoService
      .criar({
        nome: raw.nome,
        cargaHoraria: raw.cargaHoraria ? Number(raw.cargaHoraria) : undefined,
        validadeMeses: raw.validadeMeses ? Number(raw.validadeMeses) : undefined,
        obrigatorio: raw.obrigatorio,
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.modalAberto.set(false);
          this.carregar();
        },
        error: (error: HttpErrorResponse) => {
          this.submitting.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.errorMessage.set(body?.message ?? 'Não foi possível cadastrar o treinamento. Tente novamente.');
        },
      });
  }
}
