import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { CategoriaSalarialResponseDto } from '../../../core/models/categoria-salarial';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { CategoriaSalarialService } from '../../../core/services/categoria-salarial';

@Component({
  selector: 'app-categorias-salariais',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './categorias-salariais.html',
  styleUrl: './categorias-salariais.css',
})
export class CategoriasSalariais {
  private readonly fb = inject(FormBuilder);
  private readonly categoriaSalarialService = inject(CategoriaSalarialService);

  protected readonly categorias = signal<CategoriaSalarialResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<CategoriaSalarialResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly categoriasFiltradas = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    if (!termo) {
      return this.categorias();
    }
    return this.categorias().filter((c) => c.nome.toLowerCase().includes(termo));
  });

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required]],
    convencaoColetiva: [''],
  });

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.categoriaSalarialService.listar().subscribe({
      next: (categorias) => {
        this.categorias.set(categorias);
        this.carregando.set(false);
      },
      error: () => {
        this.categorias.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({ nome: '', convencaoColetiva: '' });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(categoria: CategoriaSalarialResponseDto): void {
    this.editando.set(categoria);
    this.form.reset({ nome: categoria.nome, convencaoColetiva: categoria.convencaoColetiva ?? '' });
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
      nome: raw.nome,
      convencaoColetiva: raw.convencaoColetiva || undefined,
    };

    const editando = this.editando();
    const request = editando
      ? this.categoriaSalarialService.atualizar(editando.uuid, dto)
      : this.categoriaSalarialService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregar();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar a categoria salarial. Tente novamente.');
      },
    });
  }
}
