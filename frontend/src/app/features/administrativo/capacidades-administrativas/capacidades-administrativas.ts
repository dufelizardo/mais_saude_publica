import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { CapacidadeAdministrativaResponseDto } from '../../../core/models/capacidade-administrativa';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { CapacidadeAdministrativaService } from '../../../core/services/capacidade-administrativa';

@Component({
  selector: 'app-capacidades-administrativas',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './capacidades-administrativas.html',
  styleUrl: './capacidades-administrativas.css',
})
export class CapacidadesAdministrativas {
  private readonly fb = inject(FormBuilder);
  private readonly capacidadeService = inject(CapacidadeAdministrativaService);

  protected readonly capacidades = signal<CapacidadeAdministrativaResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<CapacidadeAdministrativaResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly capacidadesFiltradas = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    if (!termo) {
      return this.capacidades();
    }
    return this.capacidades().filter((c) => c.nome.toLowerCase().includes(termo) || c.codigo.toLowerCase().includes(termo));
  });

  protected readonly form = this.fb.nonNullable.group({
    codigo: ['', [Validators.required]],
    nome: ['', [Validators.required]],
    descricao: [''],
    ativo: [true],
  });

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.capacidadeService.listar().subscribe({
      next: (capacidades) => {
        this.capacidades.set(capacidades);
        this.carregando.set(false);
      },
      error: () => {
        this.capacidades.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({ codigo: '', nome: '', descricao: '', ativo: true });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(capacidade: CapacidadeAdministrativaResponseDto): void {
    this.editando.set(capacidade);
    this.form.reset({
      codigo: capacidade.codigo,
      nome: capacidade.nome,
      descricao: capacidade.descricao ?? '',
      ativo: capacidade.ativo,
    });
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
    const dto = {
      codigo: raw.codigo,
      nome: raw.nome,
      descricao: raw.descricao || undefined,
      ativo: raw.ativo,
    };

    const editando = this.editando();
    const request = editando
      ? this.capacidadeService.atualizar(editando.uuid, dto)
      : this.capacidadeService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregar();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar a capacidade administrativa. Tente novamente.');
      },
    });
  }
}
