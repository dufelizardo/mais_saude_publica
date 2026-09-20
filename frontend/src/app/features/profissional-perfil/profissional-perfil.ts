import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ProfissionalResponseDto, ErrorResponseDto } from '../../core/models/profissional';
import { LotacaoResponseDto } from '../../core/models/lotacao';
import { AjusteIndividualResponseDto, MotivoAjusteIndividual } from '../../core/models/ajuste-individual';
import { ParticipacaoTreinamentoResponseDto, TreinamentoResponseDto } from '../../core/models/treinamento';
import { ProfissionalService } from '../../core/services/profissional';
import { LotacaoService } from '../../core/services/lotacao';
import { AjusteIndividualService } from '../../core/services/ajuste-individual';
import { TreinamentoService } from '../../core/services/treinamento';
import { formatCpf } from '../../shared/format-mask';

type Aba = 'dados' | 'lotacao' | 'ajustes' | 'treinamentos';

@Component({
  selector: 'app-profissional-perfil',
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe],
  templateUrl: './profissional-perfil.html',
  styleUrl: './profissional-perfil.css',
})
export class ProfissionalPerfil {
  private readonly fb = inject(FormBuilder);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly lotacaoService = inject(LotacaoService);
  private readonly ajusteIndividualService = inject(AjusteIndividualService);
  private readonly treinamentoService = inject(TreinamentoService);

  protected readonly buscando = signal(false);
  protected readonly naoEncontrado = signal(false);
  protected readonly profissional = signal<ProfissionalResponseDto | null>(null);

  protected readonly abaAtiva = signal<Aba>('dados');

  protected readonly lotacaoVigente = signal<LotacaoResponseDto | null>(null);
  protected readonly lotacaoHistorico = signal<LotacaoResponseDto[]>([]);
  protected readonly carregandoLotacao = signal(false);

  protected readonly ajustes = signal<AjusteIndividualResponseDto[]>([]);
  protected readonly carregandoAjustes = signal(false);
  protected readonly submittingAjuste = signal(false);
  protected readonly ajusteErrorMessage = signal<string | null>(null);

  protected readonly treinamentosCatalogo = signal<TreinamentoResponseDto[]>([]);
  protected readonly participacoes = signal<ParticipacaoTreinamentoResponseDto[]>([]);
  protected readonly carregandoTreinamentos = signal(false);
  protected readonly submittingParticipacao = signal(false);
  protected readonly participacaoErrorMessage = signal<string | null>(null);

  protected readonly cpfForm = this.fb.nonNullable.group({
    cpf: ['', [Validators.required]],
  });

  protected readonly ajusteForm = this.fb.nonNullable.group({
    valor: ['', [Validators.required]],
    motivo: ['' as MotivoAjusteIndividual | '', [Validators.required]],
    dataInicio: ['', [Validators.required]],
    dataFim: [''],
    referencia: [''],
  });

  protected readonly participacaoForm = this.fb.nonNullable.group({
    treinamentoId: ['', [Validators.required]],
    dataConclusao: ['', [Validators.required]],
    certificadoUrl: [''],
  });

  constructor() {
    this.treinamentoService.listarCatalogo().subscribe({
      next: (catalogo) => this.treinamentosCatalogo.set(catalogo),
      error: () => this.treinamentosCatalogo.set([]),
    });
  }

  protected onCpfInput(event: Event): void {
    const valor = formatCpf((event.target as HTMLInputElement).value);
    this.cpfForm.controls.cpf.setValue(valor);
  }

  protected buscar(): void {
    if (this.cpfForm.invalid) {
      this.cpfForm.markAllAsTouched();
      return;
    }

    this.buscando.set(true);
    this.naoEncontrado.set(false);
    this.profissional.set(null);

    this.profissionalService.buscarPorCpf(this.cpfForm.getRawValue().cpf).subscribe({
      next: (profissional) => {
        this.buscando.set(false);
        this.profissional.set(profissional);
        this.abaAtiva.set('dados');
        this.carregarLotacao(profissional.matricula);
        this.carregarAjustes(profissional.matricula);
        this.carregarTreinamentos(profissional.matricula);
      },
      error: () => {
        this.buscando.set(false);
        this.naoEncontrado.set(true);
      },
    });
  }

  private carregarLotacao(matricula: string): void {
    this.carregandoLotacao.set(true);
    this.lotacaoService.buscarVigente(matricula).subscribe({
      next: (vigente) => this.lotacaoVigente.set(vigente),
      error: () => this.lotacaoVigente.set(null),
    });
    this.lotacaoService.listarHistorico(matricula).subscribe({
      next: (historico) => {
        this.lotacaoHistorico.set(historico);
        this.carregandoLotacao.set(false);
      },
      error: () => {
        this.lotacaoHistorico.set([]);
        this.carregandoLotacao.set(false);
      },
    });
  }

  protected selecionarAba(aba: Aba): void {
    this.abaAtiva.set(aba);
  }

  private carregarAjustes(matricula: string): void {
    this.carregandoAjustes.set(true);
    this.ajusteIndividualService.listarPorProfissional(matricula).subscribe({
      next: (ajustes) => {
        this.ajustes.set(ajustes);
        this.carregandoAjustes.set(false);
      },
      error: () => {
        this.ajustes.set([]);
        this.carregandoAjustes.set(false);
      },
    });
  }

  private readonly rotulosMotivoAjuste: Record<MotivoAjusteIndividual, string> = {
    GRATIFICACAO_PESSOAL: 'Gratificação pessoal',
    EQUIPARACAO_JUDICIAL: 'Equiparação judicial',
  };

  protected rotuloMotivoAjuste(motivo: MotivoAjusteIndividual): string {
    return this.rotulosMotivoAjuste[motivo];
  }

  protected registrarAjuste(): void {
    const profissional = this.profissional();
    if (!profissional || this.ajusteForm.invalid) {
      this.ajusteForm.markAllAsTouched();
      return;
    }

    this.submittingAjuste.set(true);
    this.ajusteErrorMessage.set(null);

    const raw = this.ajusteForm.getRawValue();
    this.ajusteIndividualService
      .criar({
        matriculaProfissional: profissional.matricula,
        valor: Number(raw.valor),
        dataInicio: raw.dataInicio,
        dataFim: raw.dataFim || undefined,
        motivo: raw.motivo as MotivoAjusteIndividual,
        referencia: raw.referencia || undefined,
      })
      .subscribe({
        next: () => {
          this.submittingAjuste.set(false);
          this.ajusteForm.reset({ valor: '', motivo: '', dataInicio: '', dataFim: '', referencia: '' });
          this.carregarAjustes(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingAjuste.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.ajusteErrorMessage.set(body?.message ?? 'Não foi possível registrar o ajuste. Tente novamente.');
        },
      });
  }

  private carregarTreinamentos(matricula: string): void {
    this.carregandoTreinamentos.set(true);
    this.treinamentoService.listarParticipacoesPorProfissional(matricula).subscribe({
      next: (participacoes) => {
        this.participacoes.set(participacoes);
        this.carregandoTreinamentos.set(false);
      },
      error: () => {
        this.participacoes.set([]);
        this.carregandoTreinamentos.set(false);
      },
    });
  }

  protected registrarParticipacao(): void {
    const profissional = this.profissional();
    if (!profissional || this.participacaoForm.invalid) {
      this.participacaoForm.markAllAsTouched();
      return;
    }

    this.submittingParticipacao.set(true);
    this.participacaoErrorMessage.set(null);

    const raw = this.participacaoForm.getRawValue();
    this.treinamentoService
      .registrarParticipacao({
        matriculaProfissional: profissional.matricula,
        treinamentoId: raw.treinamentoId,
        dataConclusao: raw.dataConclusao,
        certificadoUrl: raw.certificadoUrl || undefined,
      })
      .subscribe({
        next: () => {
          this.submittingParticipacao.set(false);
          this.participacaoForm.reset({ treinamentoId: '', dataConclusao: '', certificadoUrl: '' });
          this.carregarTreinamentos(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingParticipacao.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.participacaoErrorMessage.set(body?.message ?? 'Não foi possível registrar a participação. Tente novamente.');
        },
      });
  }
}
