import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { AgendamentoResponseDto, StatusAgendamento, TipoAgendamento } from '../../../core/models/agendamento';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AgendamentoService } from '../../../core/services/agendamento';
import { PacienteService } from '../../../core/services/paciente';

@Component({
  selector: 'app-agendamentos',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './agendamentos.html',
  styleUrl: './agendamentos.css',
})
export class Agendamentos {
  private readonly fb = inject(FormBuilder);
  private readonly agendamentoService = inject(AgendamentoService);
  private readonly pacienteService = inject(PacienteService);

  protected readonly tipos: TipoAgendamento[] = ['CONSULTA', 'PROCEDIMENTO', 'RETORNO'];
  protected readonly statusList: StatusAgendamento[] = ['AGENDADO', 'CONFIRMADO', 'REALIZADO', 'CANCELADO'];

  protected readonly agendamentos = signal<AgendamentoResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<AgendamentoResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    pacienteId: ['', [Validators.required]],
    profissionalMatricula: ['', [Validators.required]],
    dataHora: ['', [Validators.required]],
    status: ['AGENDADO' as StatusAgendamento, [Validators.required]],
    tipo: ['CONSULTA' as TipoAgendamento, [Validators.required]],
    observacao: [''],
  });

  constructor() {
    this.carregarAgendamentos();
    this.pacienteService.listar().subscribe({
      next: (pacientes) => this.pacientes.set(pacientes),
      error: () => this.pacientes.set([]),
    });
  }

  private carregarAgendamentos(): void {
    this.carregando.set(true);
    this.agendamentoService.listar().subscribe({
      next: (agendamentos) => {
        this.agendamentos.set(agendamentos);
        this.carregando.set(false);
      },
      error: () => {
        this.agendamentos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({
      pacienteId: '',
      profissionalMatricula: '',
      dataHora: '',
      status: 'AGENDADO',
      tipo: 'CONSULTA',
      observacao: '',
    });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(agendamento: AgendamentoResponseDto): void {
    this.editando.set(agendamento);
    this.form.reset({
      pacienteId: agendamento.pacienteUuid,
      profissionalMatricula: agendamento.profissionalMatricula,
      dataHora: agendamento.dataHora,
      status: agendamento.status,
      tipo: agendamento.tipo,
      observacao: agendamento.observacao ?? '',
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
      pacienteId: raw.pacienteId,
      profissionalMatricula: raw.profissionalMatricula,
      dataHora: raw.dataHora,
      status: raw.status,
      tipo: raw.tipo,
      observacao: raw.observacao || undefined,
    };

    const editando = this.editando();
    const request = editando
      ? this.agendamentoService.atualizar(editando.uuid, dto)
      : this.agendamentoService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarAgendamentos();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar o agendamento. Tente novamente.');
      },
    });
  }
}
