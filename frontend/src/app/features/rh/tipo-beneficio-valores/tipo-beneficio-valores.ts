import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CusteioBeneficio, TipoBeneficioResponseDto } from '../../../core/models/tipo-beneficio';
import { ValorBeneficioResponseDto } from '../../../core/models/valor-beneficio';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { TipoBeneficioService } from '../../../core/services/tipo-beneficio';
import { ValorBeneficioService } from '../../../core/services/valor-beneficio';

@Component({
  selector: 'app-tipo-beneficio-valores',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './tipo-beneficio-valores.html',
  styleUrl: './tipo-beneficio-valores.css',
})
export class TipoBeneficioValores {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly tipoBeneficioService = inject(TipoBeneficioService);
  private readonly valorBeneficioService = inject(ValorBeneficioService);

  private readonly tipoId = this.route.snapshot.paramMap.get('tipoId')!;

  protected readonly tipo = signal<TipoBeneficioResponseDto | null>(null);
  protected readonly vigente = signal<ValorBeneficioResponseDto | null>(null);
  protected readonly historico = signal<ValorBeneficioResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  private readonly rotulosCusteio: Record<CusteioBeneficio, string> = {
    EMPRESA: 'Empresa',
    COMPARTILHADO: 'Compartilhado',
    PROFISSIONAL: 'Profissional',
  };

  protected rotuloCusteio(custeio: CusteioBeneficio): string {
    return this.rotulosCusteio[custeio];
  }

  protected readonly historicoOrdenado = computed(() =>
    [...this.historico()].sort((a, b) => b.dataVigencia.localeCompare(a.dataVigencia)),
  );

  protected readonly form = this.fb.nonNullable.group({
    valor: ['', [Validators.required]],
    dataVigencia: ['', [Validators.required]],
    motivo: [''],
  });

  constructor() {
    this.tipoBeneficioService.buscarPorId(this.tipoId).subscribe({
      next: (tipo) => this.tipo.set(tipo),
    });
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.valorBeneficioService.listarPorTipo(this.tipoId).subscribe({
      next: (historico) => {
        this.historico.set(historico);
        this.carregando.set(false);
      },
      error: () => {
        this.historico.set([]);
        this.carregando.set(false);
      },
    });
    this.valorBeneficioService.buscarVigente(this.tipoId).subscribe({
      next: (vigente) => this.vigente.set(vigente),
      error: () => this.vigente.set(null),
    });
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const raw = this.form.getRawValue();
    this.valorBeneficioService
      .criar({
        tipoBeneficioId: this.tipoId,
        valor: Number(raw.valor),
        dataVigencia: raw.dataVigencia,
        motivo: raw.motivo || undefined,
      })
      .subscribe({
        next: (response) => {
          this.submitting.set(false);
          this.successMessage.set(response.details);
          this.form.reset({ valor: '', dataVigencia: '', motivo: '' });
          this.carregar();
        },
        error: (error: HttpErrorResponse) => {
          this.submitting.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.errorMessage.set(body?.message ?? 'Não foi possível registrar o novo valor. Tente novamente.');
        },
      });
  }
}
