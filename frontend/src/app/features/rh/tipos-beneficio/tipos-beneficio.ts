import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Modal } from '../../../shared/modal/modal';
import { CusteioBeneficio, TipoBeneficioResponseDto } from '../../../core/models/tipo-beneficio';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { TipoBeneficioService } from '../../../core/services/tipo-beneficio';

@Component({
  selector: 'app-tipos-beneficio',
  imports: [ReactiveFormsModule, RouterLink, Modal],
  templateUrl: './tipos-beneficio.html',
  styleUrl: './tipos-beneficio.css',
})
export class TiposBeneficio {
  private readonly fb = inject(FormBuilder);
  private readonly tipoBeneficioService = inject(TipoBeneficioService);

  protected readonly tipos = signal<TipoBeneficioResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  private readonly rotulosCusteio: Record<CusteioBeneficio, string> = {
    EMPRESA: 'Empresa',
    COMPARTILHADO: 'Compartilhado',
    PROFISSIONAL: 'Profissional',
  };

  protected rotuloCusteio(custeio: CusteioBeneficio): string {
    return this.rotulosCusteio[custeio];
  }

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required]],
    custeio: ['' as CusteioBeneficio | '', [Validators.required]],
  });

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.tipoBeneficioService.listar().subscribe({
      next: (tipos) => {
        this.tipos.set(tipos);
        this.carregando.set(false);
      },
      error: () => {
        this.tipos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.form.reset({ nome: '', custeio: '' });
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
    this.tipoBeneficioService
      .criar({
        nome: raw.nome,
        custeio: raw.custeio as CusteioBeneficio,
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
          this.errorMessage.set(body?.message ?? 'Não foi possível cadastrar o tipo de benefício. Tente novamente.');
        },
      });
  }
}
