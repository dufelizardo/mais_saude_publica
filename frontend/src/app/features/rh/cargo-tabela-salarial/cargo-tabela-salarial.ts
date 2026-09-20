import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CargoResponseDto } from '../../../core/models/cargo';
import { TabelaSalarialResponseDto, MotivoTabelaSalarial } from '../../../core/models/tabela-salarial';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { CargoService } from '../../../core/services/cargo';
import { TabelaSalarialService } from '../../../core/services/tabela-salarial';

@Component({
  selector: 'app-cargo-tabela-salarial',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './cargo-tabela-salarial.html',
  styleUrl: './cargo-tabela-salarial.css',
})
export class CargoTabelaSalarial {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly cargoService = inject(CargoService);
  private readonly tabelaSalarialService = inject(TabelaSalarialService);

  private readonly cargoId = this.route.snapshot.paramMap.get('cargoId')!;

  protected readonly cargo = signal<CargoResponseDto | null>(null);
  protected readonly vigente = signal<TabelaSalarialResponseDto | null>(null);
  protected readonly historico = signal<TabelaSalarialResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  private readonly rotulosMotivo: Record<MotivoTabelaSalarial, string> = {
    DISSIDIO: 'Dissídio',
    REVISAO_PLANO_CARGOS_SALARIOS: 'Revisão do plano de cargos e salários',
  };

  protected rotuloMotivo(motivo: MotivoTabelaSalarial): string {
    return this.rotulosMotivo[motivo];
  }

  protected readonly historicoOrdenado = computed(() =>
    [...this.historico()].sort((a, b) => b.dataVigencia.localeCompare(a.dataVigencia)),
  );

  protected readonly form = this.fb.nonNullable.group({
    valorBase: ['', [Validators.required]],
    dataVigencia: ['', [Validators.required]],
    motivo: ['' as MotivoTabelaSalarial | '', [Validators.required]],
  });

  constructor() {
    this.cargoService.buscarPorId(this.cargoId).subscribe({
      next: (cargo) => this.cargo.set(cargo),
    });
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.tabelaSalarialService.listarPorCargo(this.cargoId).subscribe({
      next: (historico) => {
        this.historico.set(historico);
        this.carregando.set(false);
      },
      error: () => {
        this.historico.set([]);
        this.carregando.set(false);
      },
    });
    this.tabelaSalarialService.buscarVigente(this.cargoId).subscribe({
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
    this.tabelaSalarialService
      .criar({
        cargoId: this.cargoId,
        valorBase: Number(raw.valorBase),
        dataVigencia: raw.dataVigencia,
        motivo: raw.motivo as MotivoTabelaSalarial,
      })
      .subscribe({
        next: (response) => {
          this.submitting.set(false);
          this.successMessage.set(response.details);
          this.form.reset({ valorBase: '', dataVigencia: '', motivo: '' });
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
