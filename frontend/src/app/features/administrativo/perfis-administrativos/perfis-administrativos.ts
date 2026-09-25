import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { PerfilAdministrativoResponseDto } from '../../../core/models/perfil-administrativo';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { PerfilAdministrativoService } from '../../../core/services/perfil-administrativo';

@Component({
  selector: 'app-perfis-administrativos',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './perfis-administrativos.html',
  styleUrl: './perfis-administrativos.css',
})
export class PerfisAdministrativos {
  private readonly fb = inject(FormBuilder);
  private readonly perfilService = inject(PerfilAdministrativoService);

  protected readonly perfis = signal<PerfilAdministrativoResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<PerfilAdministrativoResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly perfisFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    if (!termo) {
      return this.perfis();
    }
    return this.perfis().filter((p) => p.nome.toLowerCase().includes(termo) || p.codigo.toLowerCase().includes(termo));
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
    this.perfilService.listar().subscribe({
      next: (perfis) => {
        this.perfis.set(perfis);
        this.carregando.set(false);
      },
      error: () => {
        this.perfis.set([]);
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

  protected abrirEdicao(perfil: PerfilAdministrativoResponseDto): void {
    this.editando.set(perfil);
    this.form.reset({
      codigo: perfil.codigo,
      nome: perfil.nome,
      descricao: perfil.descricao ?? '',
      ativo: perfil.ativo,
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
      ? this.perfilService.atualizar(editando.uuid, dto)
      : this.perfilService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregar();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar o perfil administrativo. Tente novamente.');
      },
    });
  }
}
