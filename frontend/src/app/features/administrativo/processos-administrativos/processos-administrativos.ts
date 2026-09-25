import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { ProcessoAdministrativoResponseDto } from '../../../core/models/processo-administrativo';
import { CapacidadeAdministrativaResponseDto } from '../../../core/models/capacidade-administrativa';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { ProcessoAdministrativoService } from '../../../core/services/processo-administrativo';
import { CapacidadeAdministrativaService } from '../../../core/services/capacidade-administrativa';

@Component({
  selector: 'app-processos-administrativos',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './processos-administrativos.html',
  styleUrl: './processos-administrativos.css',
})
export class ProcessosAdministrativos {
  private readonly fb = inject(FormBuilder);
  private readonly processoService = inject(ProcessoAdministrativoService);
  private readonly capacidadeService = inject(CapacidadeAdministrativaService);

  protected readonly processos = signal<ProcessoAdministrativoResponseDto[]>([]);
  protected readonly capacidades = signal<CapacidadeAdministrativaResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly filtroCapacidade = signal('');
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<ProcessoAdministrativoResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly processosFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const capacidadeId = this.filtroCapacidade();
    return this.processos().filter((p) => {
      const bateNome = !termo || p.nome.toLowerCase().includes(termo) || p.codigo.toLowerCase().includes(termo);
      const bateCapacidade = !capacidadeId || p.capacidadeUuid === capacidadeId;
      return bateNome && bateCapacidade;
    });
  });

  protected readonly form = this.fb.nonNullable.group({
    capacidadeId: ['', [Validators.required]],
    codigo: ['', [Validators.required]],
    nome: ['', [Validators.required]],
    descricao: [''],
    ativo: [true],
  });

  constructor() {
    this.carregarCapacidades();
    this.carregarProcessos();
  }

  private carregarCapacidades(): void {
    this.capacidadeService.listar().subscribe({
      next: (capacidades) => this.capacidades.set(capacidades),
      error: () => this.capacidades.set([]),
    });
  }

  private carregarProcessos(): void {
    this.carregando.set(true);
    this.processoService.listar().subscribe({
      next: (processos) => {
        this.processos.set(processos);
        this.carregando.set(false);
      },
      error: () => {
        this.processos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({ capacidadeId: '', codigo: '', nome: '', descricao: '', ativo: true });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(processo: ProcessoAdministrativoResponseDto): void {
    this.editando.set(processo);
    this.form.reset({
      capacidadeId: processo.capacidadeUuid,
      codigo: processo.codigo,
      nome: processo.nome,
      descricao: processo.descricao ?? '',
      ativo: processo.ativo,
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
      capacidadeId: raw.capacidadeId,
      codigo: raw.codigo,
      nome: raw.nome,
      descricao: raw.descricao || undefined,
      ativo: raw.ativo,
    };

    const editando = this.editando();
    const request = editando
      ? this.processoService.atualizar(editando.uuid, dto)
      : this.processoService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarProcessos();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar o processo administrativo. Tente novamente.');
      },
    });
  }
}
