import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { ConsultaResponseDto, TipoConsulta } from '../../../core/models/consulta';
import { AtendimentoResponseDto } from '../../../core/models/atendimento';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { ConsultaService } from '../../../core/services/consulta';
import { AtendimentoService } from '../../../core/services/atendimento';

@Component({
  selector: 'app-consultas',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './consultas.html',
  styleUrl: './consultas.css',
})
export class Consultas {
  private readonly fb = inject(FormBuilder);
  private readonly consultaService = inject(ConsultaService);
  private readonly atendimentoService = inject(AtendimentoService);

  protected readonly tipos: TipoConsulta[] = ['PRIMEIRA', 'RETORNO', 'URGENCIA'];

  protected readonly consultas = signal<ConsultaResponseDto[]>([]);
  protected readonly atendimentos = signal<AtendimentoResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<ConsultaResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    atendimentoId: ['', [Validators.required]],
    profissionalMatricula: ['', [Validators.required]],
    dataHora: ['', [Validators.required]],
    tipoConsulta: ['PRIMEIRA' as TipoConsulta, [Validators.required]],
    queixaPrincipal: [''],
    diagnostico: [''],
    receituario: [''],
    examesSolicitados: [''],
    retorno: [''],
  });

  constructor() {
    this.carregarConsultas();
    this.atendimentoService.listar().subscribe({
      next: (atendimentos) => this.atendimentos.set(atendimentos),
      error: () => this.atendimentos.set([]),
    });
  }

  private carregarConsultas(): void {
    this.carregando.set(true);
    this.consultaService.listar().subscribe({
      next: (consultas) => {
        this.consultas.set(consultas);
        this.carregando.set(false);
      },
      error: () => {
        this.consultas.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({
      atendimentoId: '',
      profissionalMatricula: '',
      dataHora: '',
      tipoConsulta: 'PRIMEIRA',
      queixaPrincipal: '',
      diagnostico: '',
      receituario: '',
      examesSolicitados: '',
      retorno: '',
    });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(consulta: ConsultaResponseDto): void {
    this.editando.set(consulta);
    this.form.reset({
      atendimentoId: consulta.atendimentoUuid,
      profissionalMatricula: consulta.profissionalMatricula,
      dataHora: consulta.dataHora,
      tipoConsulta: consulta.tipoConsulta,
      queixaPrincipal: consulta.queixaPrincipal ?? '',
      diagnostico: consulta.diagnostico ?? '',
      receituario: consulta.receituario ?? '',
      examesSolicitados: consulta.examesSolicitados ?? '',
      retorno: consulta.retorno ?? '',
    });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected fecharModal(): void {
    this.modalAberto.set(false);
  }

  protected descricaoAtendimento(atendimentoUuid: string): string {
    const atendimento = this.atendimentos().find((a) => a.uuid === atendimentoUuid);
    return atendimento ? `${atendimento.pacienteNome} — ${atendimento.dataHora}` : atendimentoUuid;
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
      atendimentoId: raw.atendimentoId,
      profissionalMatricula: raw.profissionalMatricula,
      dataHora: raw.dataHora,
      tipoConsulta: raw.tipoConsulta,
      queixaPrincipal: raw.queixaPrincipal || undefined,
      diagnostico: raw.diagnostico || undefined,
      receituario: raw.receituario || undefined,
      examesSolicitados: raw.examesSolicitados || undefined,
      retorno: raw.retorno || undefined,
    };

    const editando = this.editando();
    const request = editando
      ? this.consultaService.atualizar(editando.uuid, dto)
      : this.consultaService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarConsultas();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar a consulta. Tente novamente.');
      },
    });
  }
}
