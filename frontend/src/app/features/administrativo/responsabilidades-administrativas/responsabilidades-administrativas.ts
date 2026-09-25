import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { ErrorResponseDto, ProfissionalResponseDto } from '../../../core/models/profissional';
import { ResponsabilidadeAdministrativaResponseDto } from '../../../core/models/responsabilidade-administrativa';
import { SetorResponseDto } from '../../../core/models/setor';
import { ProfissionalService } from '../../../core/services/profissional';
import { ResponsabilidadeAdministrativaService } from '../../../core/services/responsabilidade-administrativa';
import { SetorService } from '../../../core/services/setor';

@Component({
  selector: 'app-responsabilidades-administrativas',
  imports: [ReactiveFormsModule, Modal, DatePipe],
  templateUrl: './responsabilidades-administrativas.html',
  styleUrl: './responsabilidades-administrativas.css',
})
export class ResponsabilidadesAdministrativas {
  private readonly fb = inject(FormBuilder);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly responsabilidadeService = inject(ResponsabilidadeAdministrativaService);
  private readonly setorService = inject(SetorService);

  protected readonly buscando = signal(false);
  protected readonly naoEncontrado = signal(false);
  protected readonly profissional = signal<ProfissionalResponseDto | null>(null);

  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly historico = signal<ResponsabilidadeAdministrativaResponseDto[]>([]);
  protected readonly carregandoHistorico = signal(false);

  protected readonly submittingNova = signal(false);
  protected readonly novaErrorMessage = signal<string | null>(null);

  protected readonly encerrarAlvo = signal<ResponsabilidadeAdministrativaResponseDto | null>(null);
  protected readonly submittingEncerrar = signal(false);
  protected readonly encerrarErrorMessage = signal<string | null>(null);

  protected readonly matriculaForm = this.fb.nonNullable.group({
    matricula: ['', [Validators.required]],
  });

  protected readonly novaForm = this.fb.nonNullable.group({
    setorId: ['', [Validators.required]],
    tipo: ['', [Validators.required]],
    descricao: [''],
    dataInicio: [this.hoje(), [Validators.required]],
  });

  protected readonly encerrarForm = this.fb.nonNullable.group({
    dataFim: ['', [Validators.required]],
  });

  protected buscar(): void {
    if (this.matriculaForm.invalid) {
      this.matriculaForm.markAllAsTouched();
      return;
    }

    this.buscando.set(true);
    this.naoEncontrado.set(false);
    this.profissional.set(null);

    const matricula = this.matriculaForm.getRawValue().matricula;
    this.profissionalService.buscarPorMatricula(matricula).subscribe({
      next: (profissional) => {
        this.buscando.set(false);
        this.profissional.set(profissional);
        this.carregarSetores();
        this.carregarHistorico(profissional.matricula);
      },
      error: () => {
        this.buscando.set(false);
        this.naoEncontrado.set(true);
      },
    });
  }

  private carregarSetores(): void {
    this.setorService.listar().subscribe({
      next: (setores) => this.setores.set(setores),
      error: () => this.setores.set([]),
    });
  }

  private carregarHistorico(matricula: string): void {
    this.carregandoHistorico.set(true);
    this.responsabilidadeService.listarHistorico(matricula).subscribe({
      next: (historico) => {
        this.historico.set(historico);
        this.carregandoHistorico.set(false);
      },
      error: () => {
        this.historico.set([]);
        this.carregandoHistorico.set(false);
      },
    });
  }

  protected situacao(item: ResponsabilidadeAdministrativaResponseDto): 'Vigente' | 'Encerrada' {
    return item.dataFim ? 'Encerrada' : 'Vigente';
  }

  protected registrarResponsabilidade(): void {
    const profissional = this.profissional();
    if (!profissional || this.novaForm.invalid) {
      this.novaForm.markAllAsTouched();
      return;
    }

    this.submittingNova.set(true);
    this.novaErrorMessage.set(null);

    const raw = this.novaForm.getRawValue();
    this.responsabilidadeService
      .criar({
        matriculaProfissional: profissional.matricula,
        setorId: raw.setorId,
        tipo: raw.tipo,
        descricao: raw.descricao || undefined,
        dataInicio: raw.dataInicio,
      })
      .subscribe({
        next: () => {
          this.submittingNova.set(false);
          this.novaForm.reset({ setorId: '', tipo: '', descricao: '', dataInicio: this.hoje() });
          this.carregarHistorico(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingNova.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.novaErrorMessage.set(body?.message ?? 'Não foi possível registrar a responsabilidade. Tente novamente.');
        },
      });
  }

  protected abrirEncerrar(item: ResponsabilidadeAdministrativaResponseDto): void {
    this.encerrarForm.reset({ dataFim: '' });
    this.encerrarErrorMessage.set(null);
    this.encerrarAlvo.set(item);
  }

  protected fecharEncerrar(): void {
    this.encerrarAlvo.set(null);
  }

  protected confirmarEncerrar(): void {
    const profissional = this.profissional();
    const alvo = this.encerrarAlvo();
    if (!profissional || !alvo || this.encerrarForm.invalid) {
      this.encerrarForm.markAllAsTouched();
      return;
    }

    this.submittingEncerrar.set(true);
    this.encerrarErrorMessage.set(null);

    const dataFim = this.encerrarForm.getRawValue().dataFim;
    this.responsabilidadeService.encerrar(alvo.uuid, dataFim).subscribe({
      next: () => {
        this.submittingEncerrar.set(false);
        this.encerrarAlvo.set(null);
        this.carregarHistorico(profissional.matricula);
      },
      error: (error: HttpErrorResponse) => {
        this.submittingEncerrar.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.encerrarErrorMessage.set(body?.message ?? 'Não foi possível encerrar a responsabilidade. Tente novamente.');
      },
    });
  }

  private hoje(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
