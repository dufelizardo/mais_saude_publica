import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ProfissionalResponseDto, ErrorResponseDto } from '../../core/models/profissional';
import { LotacaoResponseDto } from '../../core/models/lotacao';
import { CargoResponseDto } from '../../core/models/cargo';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { ComposicaoRemuneratoriaResponseDto } from '../../core/models/composicao-remuneratoria';
import { AfastamentoResponseDto, TipoAfastamento, StatusAfastamento } from '../../core/models/afastamento';
import { LicencaResponseDto, TipoLicenca, ResponsavelPagamentoLicenca } from '../../core/models/licenca';
import { CargoService } from '../../core/services/cargo';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { ComposicaoRemuneratoriaService } from '../../core/services/composicao-remuneratoria';
import { AfastamentoService } from '../../core/services/afastamento';
import { LicencaService } from '../../core/services/licenca';
import { AjusteIndividualResponseDto, MotivoAjusteIndividual } from '../../core/models/ajuste-individual';
import { ParticipacaoTreinamentoResponseDto, TreinamentoResponseDto } from '../../core/models/treinamento';
import { AvaliacaoResponseDto, CicloAvaliacaoResponseDto } from '../../core/models/avaliacao';
import { CalculoRescisaoResponseDto, TipoDesligamento } from '../../core/models/calculo-rescisao';
import { ProfissionalService } from '../../core/services/profissional';
import { LotacaoService } from '../../core/services/lotacao';
import { AjusteIndividualService } from '../../core/services/ajuste-individual';
import { TreinamentoService } from '../../core/services/treinamento';
import { AvaliacaoService } from '../../core/services/avaliacao';
import { CalculoRescisaoService } from '../../core/services/calculo-rescisao';
import { formatCpf } from '../../shared/format-mask';

type Aba = 'dados' | 'lotacao' | 'composicao' | 'ajustes' | 'afastamentos' | 'treinamentos' | 'avaliacoes' | 'desligamento';

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
  private readonly avaliacaoService = inject(AvaliacaoService);
  private readonly calculoRescisaoService = inject(CalculoRescisaoService);
  private readonly cargoService = inject(CargoService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly composicaoRemuneratoriaService = inject(ComposicaoRemuneratoriaService);
  private readonly afastamentoService = inject(AfastamentoService);
  private readonly licencaService = inject(LicencaService);

  protected readonly buscando = signal(false);
  protected readonly naoEncontrado = signal(false);
  protected readonly profissional = signal<ProfissionalResponseDto | null>(null);

  protected readonly abaAtiva = signal<Aba>('dados');

  protected readonly lotacaoVigente = signal<LotacaoResponseDto | null>(null);
  protected readonly lotacaoHistorico = signal<LotacaoResponseDto[]>([]);
  protected readonly carregandoLotacao = signal(false);
  protected readonly submittingTransferencia = signal(false);
  protected readonly transferenciaErrorMessage = signal<string | null>(null);

  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly cargos = signal<CargoResponseDto[]>([]);

  protected readonly composicao = signal<ComposicaoRemuneratoriaResponseDto | null>(null);
  protected readonly carregandoComposicao = signal(false);
  protected readonly composicaoNaoEncontrada = signal(false);

  protected readonly afastamentos = signal<AfastamentoResponseDto[]>([]);
  protected readonly carregandoAfastamentos = signal(false);
  protected readonly submittingAfastamento = signal(false);
  protected readonly afastamentoErrorMessage = signal<string | null>(null);

  protected readonly licencas = signal<LicencaResponseDto[]>([]);
  protected readonly carregandoLicencas = signal(false);
  protected readonly submittingLicenca = signal(false);
  protected readonly licencaErrorMessage = signal<string | null>(null);

  protected readonly ajustes = signal<AjusteIndividualResponseDto[]>([]);
  protected readonly carregandoAjustes = signal(false);
  protected readonly submittingAjuste = signal(false);
  protected readonly ajusteErrorMessage = signal<string | null>(null);

  protected readonly treinamentosCatalogo = signal<TreinamentoResponseDto[]>([]);
  protected readonly participacoes = signal<ParticipacaoTreinamentoResponseDto[]>([]);
  protected readonly carregandoTreinamentos = signal(false);
  protected readonly submittingParticipacao = signal(false);
  protected readonly participacaoErrorMessage = signal<string | null>(null);

  protected readonly ciclosAvaliacao = signal<CicloAvaliacaoResponseDto[]>([]);
  protected readonly avaliacoes = signal<AvaliacaoResponseDto[]>([]);
  protected readonly carregandoAvaliacoes = signal(false);
  protected readonly submittingAvaliacao = signal(false);
  protected readonly avaliacaoErrorMessage = signal<string | null>(null);

  protected readonly calculosRescisao = signal<CalculoRescisaoResponseDto[]>([]);
  protected readonly carregandoRescisao = signal(false);
  protected readonly submittingRescisao = signal(false);
  protected readonly rescisaoErrorMessage = signal<string | null>(null);

  protected readonly cpfForm = this.fb.nonNullable.group({
    cpf: ['', [Validators.required]],
  });

  protected readonly transferenciaForm = this.fb.nonNullable.group({
    unidadeId: ['', [Validators.required]],
    cargoId: ['', [Validators.required]],
    jornadaSemanalHoras: [''],
    dataInicio: ['', [Validators.required]],
    motivo: ['Transferência', [Validators.required]],
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

  protected readonly avaliacaoForm = this.fb.nonNullable.group({
    cicloId: ['', [Validators.required]],
    avaliador: ['', [Validators.required]],
    nota: ['', [Validators.required]],
    observacao: [''],
  });

  protected readonly afastamentoForm = this.fb.nonNullable.group({
    tipo: ['' as TipoAfastamento | '', [Validators.required]],
    dataInicio: ['', [Validators.required]],
    dataFim: ['', [Validators.required]],
    status: ['' as StatusAfastamento | '', [Validators.required]],
    observacao: [''],
  });

  protected readonly licencaForm = this.fb.nonNullable.group({
    afastamentoId: ['', [Validators.required]],
    tipoLegal: ['' as TipoLicenca | '', [Validators.required]],
    responsavelPagamento: ['' as ResponsavelPagamentoLicenca | '', [Validators.required]],
    documentoUrl: [''],
  });

  protected readonly afastamentosSemLicenca = computed(() => {
    const idsComLicenca = new Set(this.licencas().map((l) => l.afastamentoId));
    return this.afastamentos().filter((a) => !idsComLicenca.has(a.uuid));
  });

  protected readonly rescisaoForm = this.fb.nonNullable.group({
    tipoDesligamento: ['' as TipoDesligamento | '', [Validators.required]],
    avisoPrevio: ['', [Validators.required]],
    feriasVencidas: ['', [Validators.required]],
    feriasProporcionais: ['', [Validators.required]],
    decimoTerceiroProporcional: ['', [Validators.required]],
    multaFgts: ['', [Validators.required]],
    total: ['', [Validators.required]],
    documentoTrctUrl: [''],
  });

  constructor() {
    this.treinamentoService.listarCatalogo().subscribe({
      next: (catalogo) => this.treinamentosCatalogo.set(catalogo),
      error: () => this.treinamentosCatalogo.set([]),
    });
    this.avaliacaoService.listarCiclos().subscribe({
      next: (ciclos) => this.ciclosAvaliacao.set(ciclos),
      error: () => this.ciclosAvaliacao.set([]),
    });
    this.unidadeSaudeService.listar().subscribe({
      next: (unidades) => this.unidades.set(unidades),
      error: () => this.unidades.set([]),
    });
    this.cargoService.listar().subscribe({
      next: (cargos) => this.cargos.set(cargos),
      error: () => this.cargos.set([]),
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
        this.carregarComposicao(profissional.matricula);
        this.carregarAjustes(profissional.matricula);
        this.carregarAfastamentos(profissional.matricula);
        this.carregarLicencas(profissional.matricula);
        this.carregarTreinamentos(profissional.matricula);
        this.carregarAvaliacoes(profissional.matricula);
        this.carregarRescisao(profissional.matricula);
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

  private carregarComposicao(matricula: string): void {
    this.carregandoComposicao.set(true);
    this.composicaoNaoEncontrada.set(false);
    this.composicaoRemuneratoriaService.calcular(matricula).subscribe({
      next: (composicao) => {
        this.composicao.set(composicao);
        this.carregandoComposicao.set(false);
      },
      error: () => {
        this.composicao.set(null);
        this.composicaoNaoEncontrada.set(true);
        this.carregandoComposicao.set(false);
      },
    });
  }

  protected registrarTransferencia(): void {
    const profissional = this.profissional();
    if (!profissional || this.transferenciaForm.invalid) {
      this.transferenciaForm.markAllAsTouched();
      return;
    }

    this.submittingTransferencia.set(true);
    this.transferenciaErrorMessage.set(null);

    const raw = this.transferenciaForm.getRawValue();
    this.lotacaoService
      .criar({
        matriculaProfissional: profissional.matricula,
        unidadeId: raw.unidadeId,
        cargoId: raw.cargoId,
        jornadaSemanalHoras: raw.jornadaSemanalHoras ? Number(raw.jornadaSemanalHoras) : undefined,
        dataInicio: raw.dataInicio,
        motivo: raw.motivo,
      })
      .subscribe({
        next: () => {
          this.submittingTransferencia.set(false);
          this.transferenciaForm.reset({
            unidadeId: '',
            cargoId: '',
            jornadaSemanalHoras: '',
            dataInicio: '',
            motivo: 'Transferência',
          });
          this.carregarLotacao(profissional.matricula);
          this.carregarComposicao(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingTransferencia.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.transferenciaErrorMessage.set(body?.message ?? 'Não foi possível registrar a transferência. Tente novamente.');
        },
      });
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
          this.carregarComposicao(profissional.matricula);
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

  private carregarAvaliacoes(matricula: string): void {
    this.carregandoAvaliacoes.set(true);
    this.avaliacaoService.listarPorProfissional(matricula).subscribe({
      next: (avaliacoes) => {
        this.avaliacoes.set(avaliacoes);
        this.carregandoAvaliacoes.set(false);
      },
      error: () => {
        this.avaliacoes.set([]);
        this.carregandoAvaliacoes.set(false);
      },
    });
  }

  protected registrarAvaliacao(): void {
    const profissional = this.profissional();
    if (!profissional || this.avaliacaoForm.invalid) {
      this.avaliacaoForm.markAllAsTouched();
      return;
    }

    this.submittingAvaliacao.set(true);
    this.avaliacaoErrorMessage.set(null);

    const raw = this.avaliacaoForm.getRawValue();
    this.avaliacaoService
      .criar({
        matriculaProfissional: profissional.matricula,
        cicloId: raw.cicloId,
        avaliador: raw.avaliador,
        nota: Number(raw.nota),
        observacao: raw.observacao || undefined,
      })
      .subscribe({
        next: () => {
          this.submittingAvaliacao.set(false);
          this.avaliacaoForm.reset({ cicloId: '', avaliador: '', nota: '', observacao: '' });
          this.carregarAvaliacoes(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingAvaliacao.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.avaliacaoErrorMessage.set(body?.message ?? 'Não foi possível registrar a avaliação. Tente novamente.');
        },
      });
  }

  private carregarRescisao(matricula: string): void {
    this.carregandoRescisao.set(true);
    this.calculoRescisaoService.listarPorProfissional(matricula).subscribe({
      next: (calculos) => {
        this.calculosRescisao.set(calculos);
        this.carregandoRescisao.set(false);
      },
      error: () => {
        this.calculosRescisao.set([]);
        this.carregandoRescisao.set(false);
      },
    });
  }

  protected registrarRescisao(): void {
    const profissional = this.profissional();
    if (!profissional || this.rescisaoForm.invalid) {
      this.rescisaoForm.markAllAsTouched();
      return;
    }

    this.submittingRescisao.set(true);
    this.rescisaoErrorMessage.set(null);

    const raw = this.rescisaoForm.getRawValue();
    this.calculoRescisaoService
      .criar({
        matriculaProfissional: profissional.matricula,
        tipoDesligamento: raw.tipoDesligamento as TipoDesligamento,
        avisoPrevio: Number(raw.avisoPrevio),
        feriasVencidas: Number(raw.feriasVencidas),
        feriasProporcionais: Number(raw.feriasProporcionais),
        decimoTerceiroProporcional: Number(raw.decimoTerceiroProporcional),
        multaFgts: Number(raw.multaFgts),
        total: Number(raw.total),
        documentoTrctUrl: raw.documentoTrctUrl || undefined,
      })
      .subscribe({
        next: () => {
          this.submittingRescisao.set(false);
          this.rescisaoForm.reset({
            tipoDesligamento: '',
            avisoPrevio: '',
            feriasVencidas: '',
            feriasProporcionais: '',
            decimoTerceiroProporcional: '',
            multaFgts: '',
            total: '',
            documentoTrctUrl: '',
          });
          this.carregarRescisao(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingRescisao.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.rescisaoErrorMessage.set(body?.message ?? 'Não foi possível registrar o cálculo de rescisão. Tente novamente.');
        },
      });
  }

  private carregarAfastamentos(matricula: string): void {
    this.carregandoAfastamentos.set(true);
    this.afastamentoService.listarPorProfissional(matricula).subscribe({
      next: (afastamentos) => {
        this.afastamentos.set(afastamentos);
        this.carregandoAfastamentos.set(false);
      },
      error: () => {
        this.afastamentos.set([]);
        this.carregandoAfastamentos.set(false);
      },
    });
  }

  protected registrarAfastamento(): void {
    const profissional = this.profissional();
    if (!profissional || this.afastamentoForm.invalid) {
      this.afastamentoForm.markAllAsTouched();
      return;
    }

    this.submittingAfastamento.set(true);
    this.afastamentoErrorMessage.set(null);

    const raw = this.afastamentoForm.getRawValue();
    this.afastamentoService
      .criar({
        matriculaProfissional: profissional.matricula,
        tipo: raw.tipo as TipoAfastamento,
        dataInicio: raw.dataInicio,
        dataFim: raw.dataFim,
        status: raw.status as StatusAfastamento,
        observacao: raw.observacao || undefined,
      })
      .subscribe({
        next: () => {
          this.submittingAfastamento.set(false);
          this.afastamentoForm.reset({ tipo: '', dataInicio: '', dataFim: '', status: '', observacao: '' });
          this.carregarAfastamentos(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingAfastamento.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.afastamentoErrorMessage.set(body?.message ?? 'Não foi possível registrar o afastamento. Tente novamente.');
        },
      });
  }

  private carregarLicencas(matricula: string): void {
    this.carregandoLicencas.set(true);
    this.licencaService.listarPorProfissional(matricula).subscribe({
      next: (licencas) => {
        this.licencas.set(licencas);
        this.carregandoLicencas.set(false);
      },
      error: () => {
        this.licencas.set([]);
        this.carregandoLicencas.set(false);
      },
    });
  }

  protected registrarLicenca(): void {
    const profissional = this.profissional();
    if (!profissional || this.licencaForm.invalid) {
      this.licencaForm.markAllAsTouched();
      return;
    }

    this.submittingLicenca.set(true);
    this.licencaErrorMessage.set(null);

    const raw = this.licencaForm.getRawValue();
    this.licencaService
      .criar({
        afastamentoId: raw.afastamentoId,
        tipoLegal: raw.tipoLegal as TipoLicenca,
        responsavelPagamento: raw.responsavelPagamento as ResponsavelPagamentoLicenca,
        documentoUrl: raw.documentoUrl || undefined,
      })
      .subscribe({
        next: () => {
          this.submittingLicenca.set(false);
          this.licencaForm.reset({ afastamentoId: '', tipoLegal: '', responsavelPagamento: '', documentoUrl: '' });
          this.carregarLicencas(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingLicenca.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.licencaErrorMessage.set(body?.message ?? 'Não foi possível registrar a licença. Tente novamente.');
        },
      });
  }
}
