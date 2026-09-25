import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { AtendimentoResponseDto, StatusAtendimento, TipoAtendimento } from '../../../core/models/atendimento';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { SetorResponseDto } from '../../../core/models/setor';
import { AgendamentoResponseDto } from '../../../core/models/agendamento';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AtendimentoService } from '../../../core/services/atendimento';
import { PacienteService } from '../../../core/services/paciente';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { SetorService } from '../../../core/services/setor';
import { AgendamentoService } from '../../../core/services/agendamento';

@Component({
  selector: 'app-atendimentos',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './atendimentos.html',
  styleUrl: './atendimentos.css',
})
export class Atendimentos {
  private readonly fb = inject(FormBuilder);
  private readonly atendimentoService = inject(AtendimentoService);
  private readonly pacienteService = inject(PacienteService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly setorService = inject(SetorService);
  private readonly agendamentoService = inject(AgendamentoService);

  protected readonly tipos: TipoAtendimento[] = ['CONSULTA', 'URGENCIA', 'INTERNACAO'];
  protected readonly statusList: StatusAtendimento[] = ['AGENDADO', 'EM_ANDAMENTO', 'CONCLUIDO'];

  protected readonly atendimentos = signal<AtendimentoResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly agendamentos = signal<AgendamentoResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<AtendimentoResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    pacienteId: ['', [Validators.required]],
    profissionalMatricula: ['', [Validators.required]],
    unidadeId: ['', [Validators.required]],
    setorId: [''],
    agendamentoId: [''],
    tipo: ['CONSULTA' as TipoAtendimento, [Validators.required]],
    status: ['AGENDADO' as StatusAtendimento, [Validators.required]],
    dataHora: ['', [Validators.required]],
  });

  constructor() {
    this.carregarAtendimentos();
    this.pacienteService.listar().subscribe({
      next: (pacientes) => this.pacientes.set(pacientes),
      error: () => this.pacientes.set([]),
    });
    this.unidadeSaudeService.listar().subscribe({
      next: (unidades) => this.unidades.set(unidades),
      error: () => this.unidades.set([]),
    });
    this.setorService.listar().subscribe({
      next: (setores) => this.setores.set(setores),
      error: () => this.setores.set([]),
    });
    this.agendamentoService.listar().subscribe({
      next: (agendamentos) => this.agendamentos.set(agendamentos),
      error: () => this.agendamentos.set([]),
    });
  }

  private carregarAtendimentos(): void {
    this.carregando.set(true);
    this.atendimentoService.listar().subscribe({
      next: (atendimentos) => {
        this.atendimentos.set(atendimentos);
        this.carregando.set(false);
      },
      error: () => {
        this.atendimentos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({
      pacienteId: '',
      profissionalMatricula: '',
      unidadeId: '',
      setorId: '',
      agendamentoId: '',
      tipo: 'CONSULTA',
      status: 'AGENDADO',
      dataHora: '',
    });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(atendimento: AtendimentoResponseDto): void {
    this.editando.set(atendimento);
    this.form.reset({
      pacienteId: atendimento.pacienteUuid,
      profissionalMatricula: atendimento.profissionalMatricula,
      unidadeId: atendimento.unidadeUuid,
      setorId: atendimento.setorUuid ?? '',
      agendamentoId: atendimento.agendamentoUuid ?? '',
      tipo: atendimento.tipo,
      status: atendimento.status,
      dataHora: atendimento.dataHora,
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
      unidadeId: raw.unidadeId,
      setorId: raw.setorId || undefined,
      agendamentoId: raw.agendamentoId || undefined,
      tipo: raw.tipo,
      status: raw.status,
      dataHora: raw.dataHora,
    };

    const editando = this.editando();
    const request = editando
      ? this.atendimentoService.atualizar(editando.uuid, dto)
      : this.atendimentoService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarAtendimentos();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar o atendimento. Tente novamente.');
      },
    });
  }
}
