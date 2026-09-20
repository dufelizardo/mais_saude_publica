import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Modal } from '../../../shared/modal/modal';
import { VagaResponseDto, StatusVaga } from '../../../core/models/vaga';
import { CargoResponseDto } from '../../../core/models/cargo';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { VagaService } from '../../../core/services/vaga';
import { CargoService } from '../../../core/services/cargo';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';

@Component({
  selector: 'app-vagas',
  imports: [ReactiveFormsModule, RouterLink, Modal],
  templateUrl: './vagas.html',
  styleUrl: './vagas.css',
})
export class Vagas {
  private readonly fb = inject(FormBuilder);
  private readonly vagaService = inject(VagaService);
  private readonly cargoService = inject(CargoService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);

  protected readonly vagas = signal<VagaResponseDto[]>([]);
  protected readonly cargos = signal<CargoResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    unidadeId: ['', [Validators.required]],
    cargoId: ['', [Validators.required]],
    quantidade: ['', [Validators.required]],
    status: ['ABERTA' as StatusVaga, [Validators.required]],
  });

  constructor() {
    this.carregar();
    this.cargoService.listar().subscribe({
      next: (cargos) => this.cargos.set(cargos),
      error: () => this.cargos.set([]),
    });
    this.unidadeSaudeService.listar().subscribe({
      next: (unidades) => this.unidades.set(unidades),
      error: () => this.unidades.set([]),
    });
  }

  private carregar(): void {
    this.carregando.set(true);
    this.vagaService.listar().subscribe({
      next: (vagas) => {
        this.vagas.set(vagas);
        this.carregando.set(false);
      },
      error: () => {
        this.vagas.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.form.reset({ unidadeId: '', cargoId: '', quantidade: '', status: 'ABERTA' });
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
    this.vagaService
      .criar({
        unidadeId: raw.unidadeId,
        cargoId: raw.cargoId,
        quantidade: Number(raw.quantidade),
        status: raw.status,
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
          this.errorMessage.set(body?.message ?? 'Não foi possível cadastrar a vaga. Tente novamente.');
        },
      });
  }
}
