import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { ProcedimentoResponseDto, StatusProcedimento } from '../../../core/models/procedimento';
import { ConsultaResponseDto } from '../../../core/models/consulta';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { ProcedimentoService } from '../../../core/services/procedimento';
import { ConsultaService } from '../../../core/services/consulta';

@Component({
  selector: 'app-procedimentos',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './procedimentos.html',
  styleUrl: './procedimentos.css',
})
export class Procedimentos {
  private readonly fb = inject(FormBuilder);
  private readonly procedimentoService = inject(ProcedimentoService);
  private readonly consultaService = inject(ConsultaService);

  protected readonly statusList: StatusProcedimento[] = ['AGENDADO', 'REALIZADO', 'CANCELADO'];

  protected readonly procedimentos = signal<ProcedimentoResponseDto[]>([]);
  protected readonly consultas = signal<ConsultaResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<ProcedimentoResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    consultaId: ['', [Validators.required]],
    profissionalMatricula: ['', [Validators.required]],
    tipo: ['', [Validators.required]],
    descricao: [''],
    dataRealizacao: ['', [Validators.required]],
    status: ['AGENDADO' as StatusProcedimento, [Validators.required]],
  });

  constructor() {
    this.carregarProcedimentos();
    this.consultaService.listar().subscribe({
      next: (consultas) => this.consultas.set(consultas),
      error: () => this.consultas.set([]),
    });
  }

  private carregarProcedimentos(): void {
    this.carregando.set(true);
    this.procedimentoService.listar().subscribe({
      next: (procedimentos) => {
        this.procedimentos.set(procedimentos);
        this.carregando.set(false);
      },
      error: () => {
        this.procedimentos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected descricaoConsulta(consultaUuid: string): string {
    const consulta = this.consultas().find((c) => c.uuid === consultaUuid);
    return consulta ? `${consulta.tipoConsulta} — ${consulta.dataHora}` : consultaUuid;
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({
      consultaId: '',
      profissionalMatricula: '',
      tipo: '',
      descricao: '',
      dataRealizacao: '',
      status: 'AGENDADO',
    });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(procedimento: ProcedimentoResponseDto): void {
    this.editando.set(procedimento);
    this.form.reset({
      consultaId: procedimento.consultaUuid,
      profissionalMatricula: procedimento.profissionalMatricula,
      tipo: procedimento.tipo,
      descricao: procedimento.descricao ?? '',
      dataRealizacao: procedimento.dataRealizacao,
      status: procedimento.status,
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
      consultaId: raw.consultaId,
      profissionalMatricula: raw.profissionalMatricula,
      tipo: raw.tipo,
      descricao: raw.descricao || undefined,
      dataRealizacao: raw.dataRealizacao,
      status: raw.status,
    };

    const editando = this.editando();
    const request = editando
      ? this.procedimentoService.atualizar(editando.uuid, dto)
      : this.procedimentoService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarProcedimentos();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar o procedimento. Tente novamente.');
      },
    });
  }
}
