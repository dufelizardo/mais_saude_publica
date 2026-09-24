import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { NecessidadeDePessoalResponseDto } from '../../../core/models/necessidade-de-pessoal';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { SetorResponseDto } from '../../../core/models/setor';
import { CargoResponseDto } from '../../../core/models/cargo';
import { VagaResponseDto } from '../../../core/models/vaga';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { NecessidadeDePessoalService } from '../../../core/services/necessidade-de-pessoal';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { SetorService } from '../../../core/services/setor';
import { CargoService } from '../../../core/services/cargo';
import { VagaService } from '../../../core/services/vaga';

@Component({
  selector: 'app-necessidades-de-pessoal',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './necessidades-de-pessoal.html',
  styleUrl: './necessidades-de-pessoal.css',
})
export class NecessidadesDePessoal {
  private readonly fb = inject(FormBuilder);
  private readonly necessidadeService = inject(NecessidadeDePessoalService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly setorService = inject(SetorService);
  private readonly cargoService = inject(CargoService);
  private readonly vagaService = inject(VagaService);

  protected readonly necessidades = signal<NecessidadeDePessoalResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly cargos = signal<CargoResponseDto[]>([]);
  protected readonly vagas = signal<VagaResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<NecessidadeDePessoalResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly vincularAlvo = signal<NecessidadeDePessoalResponseDto | null>(null);
  protected readonly submittingVincular = signal(false);
  protected readonly vincularErrorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    unidadeId: ['', [Validators.required]],
    setorId: [''],
    cargoId: ['', [Validators.required]],
    quantidade: [1, [Validators.required, Validators.min(1)]],
    jornadaSemanalHoras: [40, [Validators.required, Validators.min(1)]],
    competenciasNecessarias: [''],
    justificativa: [''],
  });

  protected readonly vincularForm = this.fb.nonNullable.group({
    vagaId: ['', [Validators.required]],
  });

  constructor() {
    this.carregarCatalogos();
    this.carregarNecessidades();
  }

  private carregarCatalogos(): void {
    this.unidadeSaudeService.listar().subscribe({
      next: (unidades) => this.unidades.set(unidades),
      error: () => this.unidades.set([]),
    });
    this.setorService.listar().subscribe({
      next: (setores) => this.setores.set(setores),
      error: () => this.setores.set([]),
    });
    this.cargoService.listar().subscribe({
      next: (cargos) => this.cargos.set(cargos),
      error: () => this.cargos.set([]),
    });
    this.vagaService.listar().subscribe({
      next: (vagas) => this.vagas.set(vagas),
      error: () => this.vagas.set([]),
    });
  }

  private carregarNecessidades(): void {
    this.carregando.set(true);
    this.necessidadeService.listar().subscribe({
      next: (necessidades) => {
        this.necessidades.set(necessidades);
        this.carregando.set(false);
      },
      error: () => {
        this.necessidades.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({ unidadeId: '', setorId: '', cargoId: '', quantidade: 1, jornadaSemanalHoras: 40, competenciasNecessarias: '', justificativa: '' });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(necessidade: NecessidadeDePessoalResponseDto): void {
    this.editando.set(necessidade);
    this.form.reset({
      unidadeId: necessidade.unidadeUuid,
      setorId: necessidade.setorUuid ?? '',
      cargoId: necessidade.cargoUuid,
      quantidade: necessidade.quantidade,
      jornadaSemanalHoras: necessidade.jornadaSemanalHoras,
      competenciasNecessarias: necessidade.competenciasNecessarias ?? '',
      justificativa: necessidade.justificativa ?? '',
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
      setorId: raw.setorId || undefined,
      cargoId: raw.cargoId,
      quantidade: raw.quantidade,
      jornadaSemanalHoras: raw.jornadaSemanalHoras,
      competenciasNecessarias: raw.competenciasNecessarias || undefined,
      justificativa: raw.justificativa || undefined,
    };

    const editando = this.editando();
    const request = editando
      ? this.necessidadeService.atualizar(editando.uuid, dto)
      : this.necessidadeService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarNecessidades();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar a necessidade de pessoal. Tente novamente.');
      },
    });
  }

  protected abrirVincularVaga(necessidade: NecessidadeDePessoalResponseDto): void {
    this.vincularForm.reset({ vagaId: '' });
    this.vincularErrorMessage.set(null);
    this.vincularAlvo.set(necessidade);
  }

  protected fecharVincularVaga(): void {
    this.vincularAlvo.set(null);
  }

  protected confirmarVincularVaga(): void {
    const alvo = this.vincularAlvo();
    if (!alvo || this.vincularForm.invalid) {
      this.vincularForm.markAllAsTouched();
      return;
    }

    this.submittingVincular.set(true);
    this.vincularErrorMessage.set(null);

    const vagaId = this.vincularForm.getRawValue().vagaId;
    this.necessidadeService.vincularVaga(alvo.uuid, vagaId).subscribe({
      next: () => {
        this.submittingVincular.set(false);
        this.vincularAlvo.set(null);
        this.carregarNecessidades();
      },
      error: (error: HttpErrorResponse) => {
        this.submittingVincular.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.vincularErrorMessage.set(body?.message ?? 'Não foi possível vincular a vaga. Tente novamente.');
      },
    });
  }
}
