import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { VagaResponseDto } from '../../../core/models/vaga';
import { CandidatoResponseDto, StatusCandidato } from '../../../core/models/candidato';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { VagaService } from '../../../core/services/vaga';
import { CandidatoService } from '../../../core/services/candidato';

@Component({
  selector: 'app-vaga-candidatos',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './vaga-candidatos.html',
  styleUrl: './vaga-candidatos.css',
})
export class VagaCandidatos {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly vagaService = inject(VagaService);
  private readonly candidatoService = inject(CandidatoService);

  private readonly vagaId = this.route.snapshot.paramMap.get('vagaId')!;

  protected readonly vaga = signal<VagaResponseDto | null>(null);
  protected readonly candidatos = signal<CandidatoResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required]],
    cpf: ['', [Validators.required]],
    curriculoUrl: [''],
    status: ['INSCRITO' as StatusCandidato, [Validators.required]],
  });

  constructor() {
    this.vagaService.buscarPorId(this.vagaId).subscribe({
      next: (vaga) => this.vaga.set(vaga),
    });
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.candidatoService.listarPorVaga(this.vagaId).subscribe({
      next: (candidatos) => {
        this.candidatos.set(candidatos);
        this.carregando.set(false);
      },
      error: () => {
        this.candidatos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    this.candidatoService
      .criar({
        vagaId: this.vagaId,
        nome: raw.nome,
        cpf: raw.cpf,
        curriculoUrl: raw.curriculoUrl || undefined,
        status: raw.status,
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.form.reset({ nome: '', cpf: '', curriculoUrl: '', status: 'INSCRITO' });
          this.carregar();
        },
        error: (error: HttpErrorResponse) => {
          this.submitting.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.errorMessage.set(body?.message ?? 'Não foi possível registrar o candidato. Tente novamente.');
        },
      });
  }
}
