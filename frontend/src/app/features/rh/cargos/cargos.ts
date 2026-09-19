import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Modal } from '../../../shared/modal/modal';
import { CargoResponseDto } from '../../../core/models/cargo';
import { CategoriaSalarialResponseDto } from '../../../core/models/categoria-salarial';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { CargoService } from '../../../core/services/cargo';
import { CategoriaSalarialService } from '../../../core/services/categoria-salarial';

@Component({
  selector: 'app-cargos',
  imports: [ReactiveFormsModule, RouterLink, Modal],
  templateUrl: './cargos.html',
  styleUrl: './cargos.css',
})
export class Cargos {
  private readonly fb = inject(FormBuilder);
  private readonly cargoService = inject(CargoService);
  private readonly categoriaSalarialService = inject(CategoriaSalarialService);

  protected readonly cargos = signal<CargoResponseDto[]>([]);
  protected readonly categorias = signal<CategoriaSalarialResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly filtroCategoria = signal('');
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<CargoResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly cargosFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const categoriaId = this.filtroCategoria();
    return this.cargos().filter((c) => {
      const bateNome = !termo || c.nome.toLowerCase().includes(termo);
      const bateCategoria = !categoriaId || c.categoriaUuid === categoriaId;
      return bateNome && bateCategoria;
    });
  });

  protected readonly form = this.fb.nonNullable.group({
    categoriaId: ['', [Validators.required]],
    nome: ['', [Validators.required]],
  });

  constructor() {
    this.carregarCategorias();
    this.carregarCargos();
  }

  private carregarCategorias(): void {
    this.categoriaSalarialService.listar().subscribe({
      next: (categorias) => this.categorias.set(categorias),
      error: () => this.categorias.set([]),
    });
  }

  private carregarCargos(): void {
    this.carregando.set(true);
    this.cargoService.listar().subscribe({
      next: (cargos) => {
        this.cargos.set(cargos);
        this.carregando.set(false);
      },
      error: () => {
        this.cargos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({ categoriaId: '', nome: '' });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(cargo: CargoResponseDto): void {
    this.editando.set(cargo);
    this.form.reset({ categoriaId: cargo.categoriaUuid, nome: cargo.nome });
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
    const dto = { categoriaId: raw.categoriaId, nome: raw.nome };

    const editando = this.editando();
    const request = editando
      ? this.cargoService.atualizar(editando.uuid, dto)
      : this.cargoService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarCargos();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar o cargo. Tente novamente.');
      },
    });
  }
}
