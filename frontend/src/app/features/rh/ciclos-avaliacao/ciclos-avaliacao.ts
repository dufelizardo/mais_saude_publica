import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { CicloAvaliacaoResponseDto } from '../../../core/models/avaliacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AvaliacaoService } from '../../../core/services/avaliacao';

type StatusCiclo = 'Agendado' | 'Em andamento' | 'Encerrado';

@Component({
  selector: 'app-ciclos-avaliacao',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './ciclos-avaliacao.html',
  styleUrl: './ciclos-avaliacao.css',
})
export class CiclosAvaliacao {
  private readonly fb = inject(FormBuilder);
  private readonly avaliacaoService = inject(AvaliacaoService);

  protected readonly ciclos = signal<CicloAvaliacaoResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required]],
    dataInicio: ['', [Validators.required]],
    dataFim: ['', [Validators.required]],
  });

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.avaliacaoService.listarCiclos().subscribe({
      next: (ciclos) => {
        this.ciclos.set(ciclos);
        this.carregando.set(false);
      },
      error: () => {
        this.ciclos.set([]);
        this.carregando.set(false);
      },
    });
  }

  /**
   * Não existe campo de status no backend — só datas. Derivado aqui, decisão explícita do
   * usuário (ver ADR-0027): um ciclo "em andamento"/"encerrado" é sempre uma função de
   * dataInicio/dataFim vs. hoje, não precisa de um campo próprio pra ficar desatualizado.
   */
  protected statusDoCiclo(ciclo: CicloAvaliacaoResponseDto): StatusCiclo {
    const hoje = new Date().toISOString().slice(0, 10);
    if (hoje < ciclo.dataInicio) {
      return 'Agendado';
    }
    if (hoje > ciclo.dataFim) {
      return 'Encerrado';
    }
    return 'Em andamento';
  }

  protected abrirNovo(): void {
    this.form.reset({ nome: '', dataInicio: '', dataFim: '' });
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
    this.avaliacaoService
      .criarCiclo({ nome: raw.nome, dataInicio: raw.dataInicio, dataFim: raw.dataFim })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.modalAberto.set(false);
          this.carregar();
        },
        error: (error: HttpErrorResponse) => {
          this.submitting.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.errorMessage.set(body?.message ?? 'Não foi possível cadastrar o ciclo. Tente novamente.');
        },
      });
  }
}
