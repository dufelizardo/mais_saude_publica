import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { SetorResponseDto, TipoSetor } from '../../../core/models/setor';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { SetorService } from '../../../core/services/setor';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';

@Component({
  selector: 'app-setores',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './setores.html',
  styleUrl: './setores.css',
})
export class Setores {
  private readonly fb = inject(FormBuilder);
  private readonly setorService = inject(SetorService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);

  protected readonly tipos: TipoSetor[] = ['ADMINISTRATIVO', 'ASSISTENCIAL', 'APOIO', 'TECNICO'];

  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly filtroTipo = signal('');
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<SetorResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly setoresFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const tipo = this.filtroTipo();
    return this.setores().filter((s) => {
      const bateNome = !termo || s.nome.toLowerCase().includes(termo);
      const bateTipo = !tipo || s.tipo === tipo;
      return bateNome && bateTipo;
    });
  });

  protected readonly form = this.fb.nonNullable.group({
    unidadeId: ['', [Validators.required]],
    nome: ['', [Validators.required]],
    codigo: ['', [Validators.required]],
    tipo: ['ADMINISTRATIVO' as TipoSetor, [Validators.required]],
    ativo: [true],
    matriculaResponsavel: [''],
  });

  constructor() {
    this.carregarUnidades();
    this.carregarSetores();
  }

  private carregarUnidades(): void {
    this.unidadeSaudeService.listar().subscribe({
      next: (unidades) => this.unidades.set(unidades),
      error: () => this.unidades.set([]),
    });
  }

  private carregarSetores(): void {
    this.carregando.set(true);
    this.setorService.listar().subscribe({
      next: (setores) => {
        this.setores.set(setores);
        this.carregando.set(false);
      },
      error: () => {
        this.setores.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({ unidadeId: '', nome: '', codigo: '', tipo: 'ADMINISTRATIVO', ativo: true, matriculaResponsavel: '' });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(setor: SetorResponseDto): void {
    this.editando.set(setor);
    this.form.reset({
      unidadeId: setor.unidadeUuid,
      nome: setor.nome,
      codigo: setor.codigo,
      tipo: setor.tipo,
      ativo: setor.ativo,
      matriculaResponsavel: setor.responsavelMatricula ?? '',
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
      unidadeId: raw.unidadeId,
      nome: raw.nome,
      codigo: raw.codigo,
      tipo: raw.tipo,
      ativo: raw.ativo,
      matriculaResponsavel: raw.matriculaResponsavel || undefined,
    };

    const editando = this.editando();
    const request = editando
      ? this.setorService.atualizar(editando.uuid, dto)
      : this.setorService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarSetores();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar o setor. Tente novamente.');
      },
    });
  }
}
