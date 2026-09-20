import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
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
import { RegistroPontoResponseDto, TipoRegistroPonto } from '../../core/models/registro-ponto';
import { RegistroPontoService } from '../../core/services/registro-ponto';
import { FolhaPagamentoResponseDto } from '../../core/models/folha-pagamento';
import { FolhaPagamentoService } from '../../core/services/folha-pagamento';
import { ExameOcupacionalResponseDto, TipoExameOcupacional, ResultadoExameOcupacional } from '../../core/models/exame-ocupacional';
import { ExameOcupacionalService } from '../../core/services/exame-ocupacional';
import { AcidenteTrabalhoResponseDto } from '../../core/models/acidente-trabalho';
import { AcidenteTrabalhoService } from '../../core/services/acidente-trabalho';
import { EpiResponseDto } from '../../core/models/epi';
import { EpiService } from '../../core/services/epi';
import { AjusteIndividualResponseDto, MotivoAjusteIndividual } from '../../core/models/ajuste-individual';
import { AdesaoBeneficioResponseDto } from '../../core/models/adesao-beneficio';
import { TipoBeneficioResponseDto } from '../../core/models/tipo-beneficio';
import { ParticipacaoTreinamentoResponseDto, TreinamentoResponseDto } from '../../core/models/treinamento';
import { AvaliacaoResponseDto, CicloAvaliacaoResponseDto } from '../../core/models/avaliacao';
import { CalculoRescisaoResponseDto, TipoDesligamento } from '../../core/models/calculo-rescisao';
import { ProfissionalService } from '../../core/services/profissional';
import { CepService } from '../../core/services/cep';
import { LotacaoService } from '../../core/services/lotacao';
import { AjusteIndividualService } from '../../core/services/ajuste-individual';
import { AdesaoBeneficioService } from '../../core/services/adesao-beneficio';
import { TipoBeneficioService } from '../../core/services/tipo-beneficio';
import { TreinamentoService } from '../../core/services/treinamento';
import { AvaliacaoService } from '../../core/services/avaliacao';
import { CalculoRescisaoService } from '../../core/services/calculo-rescisao';
import { formatCpf, formatTelefone } from '../../shared/format-mask';
import { Modal } from '../../shared/modal/modal';

type Aba = 'dados' | 'lotacao' | 'composicao' | 'ajustes' | 'afastamentos' | 'ponto' | 'folha' | 'sst' | 'treinamentos' | 'avaliacoes' | 'beneficios' | 'desligamento' | 'historico';
type AbaSst = 'exames' | 'acidentes' | 'epis';

interface EventoHistorico {
  data: string;
  tipo: string;
  descricao: string;
}

@Component({
  selector: 'app-profissional-perfil',
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe, Modal],
  templateUrl: './profissional-perfil.html',
  styleUrl: './profissional-perfil.css',
})
export class ProfissionalPerfil {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly cepService = inject(CepService);
  private readonly lotacaoService = inject(LotacaoService);
  private readonly ajusteIndividualService = inject(AjusteIndividualService);
  private readonly adesaoBeneficioService = inject(AdesaoBeneficioService);
  private readonly tipoBeneficioService = inject(TipoBeneficioService);
  private readonly treinamentoService = inject(TreinamentoService);
  private readonly avaliacaoService = inject(AvaliacaoService);
  private readonly calculoRescisaoService = inject(CalculoRescisaoService);
  private readonly cargoService = inject(CargoService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly composicaoRemuneratoriaService = inject(ComposicaoRemuneratoriaService);
  private readonly afastamentoService = inject(AfastamentoService);
  private readonly licencaService = inject(LicencaService);
  private readonly registroPontoService = inject(RegistroPontoService);
  private readonly folhaPagamentoService = inject(FolhaPagamentoService);
  private readonly exameOcupacionalService = inject(ExameOcupacionalService);
  private readonly acidenteTrabalhoService = inject(AcidenteTrabalhoService);
  private readonly epiService = inject(EpiService);

  protected readonly buscando = signal(false);
  protected readonly naoEncontrado = signal(false);
  protected readonly profissional = signal<ProfissionalResponseDto | null>(null);

  protected readonly abaAtiva = signal<Aba>('dados');

  protected readonly contatoModalAberto = signal(false);
  protected readonly submittingContato = signal(false);
  protected readonly contatoErrorMessage = signal<string | null>(null);
  protected readonly buscandoCepContato = signal(false);
  protected readonly cepContatoNaoEncontrado = signal(false);

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

  protected readonly registrosPonto = signal<RegistroPontoResponseDto[]>([]);
  protected readonly carregandoPonto = signal(false);
  protected readonly submittingPonto = signal(false);
  protected readonly pontoErrorMessage = signal<string | null>(null);

  protected readonly correcaoPontoAlvo = signal<RegistroPontoResponseDto | null>(null);
  protected readonly submittingCorrecaoPonto = signal(false);
  protected readonly correcaoPontoErrorMessage = signal<string | null>(null);
  protected readonly resolvendoCorrecaoUuid = signal<string | null>(null);
  private matriculaAtual = '';

  protected readonly folhas = signal<FolhaPagamentoResponseDto[]>([]);
  protected readonly carregandoFolhas = signal(false);
  protected readonly submittingFolha = signal(false);
  protected readonly folhaErrorMessage = signal<string | null>(null);

  protected readonly sstAbaAtiva = signal<AbaSst>('exames');

  protected readonly exames = signal<ExameOcupacionalResponseDto[]>([]);
  protected readonly carregandoExames = signal(false);
  protected readonly submittingExame = signal(false);
  protected readonly exameErrorMessage = signal<string | null>(null);

  protected readonly acidentes = signal<AcidenteTrabalhoResponseDto[]>([]);
  protected readonly carregandoAcidentes = signal(false);
  protected readonly submittingAcidente = signal(false);
  protected readonly acidenteErrorMessage = signal<string | null>(null);

  protected readonly epis = signal<EpiResponseDto[]>([]);
  protected readonly carregandoEpis = signal(false);
  protected readonly submittingEpi = signal(false);
  protected readonly epiErrorMessage = signal<string | null>(null);

  protected readonly ajustes = signal<AjusteIndividualResponseDto[]>([]);
  protected readonly carregandoAjustes = signal(false);
  protected readonly submittingAjuste = signal(false);
  protected readonly ajusteErrorMessage = signal<string | null>(null);

  protected readonly tiposBeneficioCatalogo = signal<TipoBeneficioResponseDto[]>([]);
  protected readonly beneficios = signal<AdesaoBeneficioResponseDto[]>([]);
  protected readonly carregandoBeneficios = signal(false);
  protected readonly submittingBeneficio = signal(false);
  protected readonly beneficioErrorMessage = signal<string | null>(null);
  protected readonly encerrarBeneficioAlvo = signal<AdesaoBeneficioResponseDto | null>(null);
  protected readonly submittingEncerrarBeneficio = signal(false);
  protected readonly encerrarBeneficioErrorMessage = signal<string | null>(null);

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

  protected readonly contatoForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    telefone: ['', [Validators.required]],
    cep: ['', [Validators.required]],
    logradouro: ['', [Validators.required]],
    numeroLogradouro: ['', [Validators.required]],
    complemento: [''],
    bairro: ['', [Validators.required]],
    cidade: ['', [Validators.required]],
    estado: ['', [Validators.required]],
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

  protected readonly beneficioForm = this.fb.nonNullable.group({
    tipoBeneficioId: ['', [Validators.required]],
    dataInicio: ['', [Validators.required]],
    quantidadeDependentes: [''],
  });

  protected readonly encerrarBeneficioForm = this.fb.nonNullable.group({
    dataFim: ['', [Validators.required]],
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

  protected readonly filtroPontoForm = this.fb.nonNullable.group({
    dataInicio: [''],
    dataFim: [''],
  });

  protected readonly pontoForm = this.fb.nonNullable.group({
    tipo: ['' as TipoRegistroPonto | '', [Validators.required]],
    dataHora: ['', [Validators.required]],
  });

  protected readonly correcaoPontoForm = this.fb.nonNullable.group({
    tipoProposto: ['' as TipoRegistroPonto | '', [Validators.required]],
    dataHoraProposta: ['', [Validators.required]],
    justificativa: ['', [Validators.required]],
  });

  protected readonly folhaForm = this.fb.nonNullable.group({
    competencia: ['', [Validators.required, Validators.pattern(/^\d{2}\/\d{4}$/)]],
    proventos: ['', [Validators.required]],
    descontos: ['', [Validators.required]],
    encargos: ['', [Validators.required]],
    total: ['', [Validators.required]],
  });

  protected readonly exameForm = this.fb.nonNullable.group({
    tipo: ['' as TipoExameOcupacional | '', [Validators.required]],
    dataRealizacao: ['', [Validators.required]],
    dataValidade: [''],
    resultado: ['' as ResultadoExameOcupacional | '', [Validators.required]],
    asoUrl: [''],
  });

  protected readonly acidenteForm = this.fb.nonNullable.group({
    dataHora: ['', [Validators.required]],
    descricao: ['', [Validators.required]],
    catEmitida: [false],
    catUrl: [''],
    diasAfastamento: [''],
  });

  protected readonly epiForm = this.fb.nonNullable.group({
    tipo: ['', [Validators.required]],
    numeroCA: [''],
    dataEntrega: ['', [Validators.required]],
    dataDevolucao: [''],
  });

  protected readonly historicoFuncional = computed<EventoHistorico[]>(() => {
    const eventos: EventoHistorico[] = [];

    for (const l of this.lotacaoHistorico()) {
      eventos.push({
        data: l.dataInicio,
        tipo: 'Lotação',
        descricao: `${l.motivo || 'Lotação'} — ${l.cargoNome} em ${l.unidadeNome}`,
      });
    }

    for (const a of this.afastamentos()) {
      const licenca = this.licencas().find((l) => l.afastamentoId === a.uuid);
      eventos.push({
        data: a.dataInicio,
        tipo: 'Afastamento',
        descricao: `${a.tipo} (${a.status})` + (licenca ? ` — licença: ${licenca.tipoLegal}` : ''),
      });
    }

    for (const aj of this.ajustes()) {
      eventos.push({
        data: aj.dataInicio,
        tipo: 'Ajuste individual',
        descricao: `${this.rotuloMotivoAjuste(aj.motivo)}: ${aj.valor}`,
      });
    }

    const profissional = this.profissional();
    if (profissional?.dataDesligamento) {
      eventos.push({
        data: profissional.dataDesligamento,
        tipo: 'Desligamento',
        descricao: 'Profissional desligado',
      });
    }

    return eventos.sort((a, b) => b.data.localeCompare(a.data));
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
    this.acidenteForm.controls.catEmitida.valueChanges.subscribe((catEmitida) => {
      const catUrlControl = this.acidenteForm.controls.catUrl;
      catUrlControl.setValidators(catEmitida ? [Validators.required] : []);
      catUrlControl.updateValueAndValidity();
    });

    this.treinamentoService.listarCatalogo().subscribe({
      next: (catalogo) => this.treinamentosCatalogo.set(catalogo),
      error: () => this.treinamentosCatalogo.set([]),
    });
    this.avaliacaoService.listarCiclos().subscribe({
      next: (ciclos) => this.ciclosAvaliacao.set(ciclos),
      error: () => this.ciclosAvaliacao.set([]),
    });
    this.tipoBeneficioService.listar().subscribe({
      next: (tipos) => this.tiposBeneficioCatalogo.set(tipos),
      error: () => this.tiposBeneficioCatalogo.set([]),
    });
    this.unidadeSaudeService.listar().subscribe({
      next: (unidades) => this.unidades.set(unidades),
      error: () => this.unidades.set([]),
    });
    this.cargoService.listar().subscribe({
      next: (cargos) => this.cargos.set(cargos),
      error: () => this.cargos.set([]),
    });

    this.contatoForm.controls.cep.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((cep) => this.buscarCepContato(cep));

    const cpfNaUrl = this.route.snapshot.queryParamMap.get('cpf');
    if (cpfNaUrl) {
      this.cpfForm.controls.cpf.setValue(formatCpf(cpfNaUrl));
      this.buscar();
    }
  }

  protected onCpfInput(event: Event): void {
    const valor = formatCpf((event.target as HTMLInputElement).value);
    this.cpfForm.controls.cpf.setValue(valor);
  }

  protected onTelefoneContatoInput(event: Event): void {
    const valor = formatTelefone((event.target as HTMLInputElement).value);
    this.contatoForm.controls.telefone.setValue(valor);
  }

  private buscarCepContato(cepDigitado: string): void {
    const cep = cepDigitado.replace(/\D/g, '');
    this.cepContatoNaoEncontrado.set(false);

    if (cep.length !== 8) {
      return;
    }

    this.buscandoCepContato.set(true);
    this.cepService.buscar(cep).subscribe({
      next: (endereco) => {
        this.buscandoCepContato.set(false);
        if (endereco.erro) {
          this.cepContatoNaoEncontrado.set(true);
          return;
        }
        this.contatoForm.patchValue({
          logradouro: endereco.logradouro,
          bairro: endereco.bairro,
          cidade: endereco.localidade,
          estado: endereco.uf,
        });
      },
      error: () => {
        this.buscandoCepContato.set(false);
        this.cepContatoNaoEncontrado.set(true);
      },
    });
  }

  protected abrirEditarContato(): void {
    const profissional = this.profissional();
    if (!profissional) {
      return;
    }

    this.contatoForm.reset({
      email: profissional.email,
      telefone: profissional.telefones[0] ?? '',
      cep: profissional.endereco.cep,
      logradouro: profissional.endereco.logradouro,
      numeroLogradouro: profissional.endereco.numeroLogradouro,
      complemento: profissional.endereco.complemento ?? '',
      bairro: profissional.endereco.bairro,
      cidade: profissional.endereco.cidade,
      estado: profissional.endereco.estado,
    });
    this.contatoErrorMessage.set(null);
    this.cepContatoNaoEncontrado.set(false);
    this.contatoModalAberto.set(true);
  }

  protected fecharContatoModal(): void {
    this.contatoModalAberto.set(false);
  }

  protected submitContato(): void {
    const profissional = this.profissional();
    if (!profissional || this.contatoForm.invalid) {
      this.contatoForm.markAllAsTouched();
      return;
    }

    this.submittingContato.set(true);
    this.contatoErrorMessage.set(null);

    const raw = this.contatoForm.getRawValue();
    this.profissionalService
      .atualizarContato(profissional.cpf, {
        email: raw.email,
        telefones: [raw.telefone],
        endereco: {
          cep: raw.cep,
          logradouro: raw.logradouro,
          numeroLogradouro: raw.numeroLogradouro,
          complemento: raw.complemento || undefined,
          bairro: raw.bairro,
          cidade: raw.cidade,
          estado: raw.estado,
        },
      })
      .subscribe({
        next: () => {
          this.submittingContato.set(false);
          this.contatoModalAberto.set(false);
          this.profissionalService.buscarPorCpf(profissional.cpf).subscribe({
            next: (atualizado) => this.profissional.set(atualizado),
          });
        },
        error: (error: HttpErrorResponse) => {
          this.submittingContato.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.contatoErrorMessage.set(body?.message ?? 'Não foi possível atualizar o contato. Tente novamente.');
        },
      });
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
        this.matriculaAtual = profissional.matricula;
        this.filtroPontoForm.reset({ dataInicio: '', dataFim: '' });
        this.carregarPonto();
        this.carregarFolhas(profissional.matricula);
        this.carregarExames(profissional.matricula);
        this.carregarAcidentes(profissional.matricula);
        this.carregarEpis(profissional.matricula);
        this.carregarTreinamentos(profissional.matricula);
        this.carregarAvaliacoes(profissional.matricula);
        this.carregarBeneficios(profissional.matricula);
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

  private carregarBeneficios(matricula: string): void {
    this.carregandoBeneficios.set(true);
    this.adesaoBeneficioService.listarHistorico(matricula).subscribe({
      next: (beneficios) => {
        this.beneficios.set(beneficios);
        this.carregandoBeneficios.set(false);
      },
      error: () => {
        this.beneficios.set([]);
        this.carregandoBeneficios.set(false);
      },
    });
  }

  protected situacaoBeneficio(adesao: AdesaoBeneficioResponseDto): 'Vigente' | 'Encerrado' {
    return adesao.dataFim ? 'Encerrado' : 'Vigente';
  }

  protected registrarBeneficio(): void {
    const profissional = this.profissional();
    if (!profissional || this.beneficioForm.invalid) {
      this.beneficioForm.markAllAsTouched();
      return;
    }

    this.submittingBeneficio.set(true);
    this.beneficioErrorMessage.set(null);

    const raw = this.beneficioForm.getRawValue();
    this.adesaoBeneficioService
      .criar({
        matriculaProfissional: profissional.matricula,
        tipoBeneficioId: raw.tipoBeneficioId,
        dataInicio: raw.dataInicio,
        quantidadeDependentes: raw.quantidadeDependentes ? Number(raw.quantidadeDependentes) : undefined,
      })
      .subscribe({
        next: () => {
          this.submittingBeneficio.set(false);
          this.beneficioForm.reset({ tipoBeneficioId: '', dataInicio: '', quantidadeDependentes: '' });
          this.carregarBeneficios(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingBeneficio.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.beneficioErrorMessage.set(body?.message ?? 'Não foi possível registrar a adesão. Tente novamente.');
        },
      });
  }

  protected abrirEncerrarBeneficio(adesao: AdesaoBeneficioResponseDto): void {
    this.encerrarBeneficioForm.reset({ dataFim: '' });
    this.encerrarBeneficioErrorMessage.set(null);
    this.encerrarBeneficioAlvo.set(adesao);
  }

  protected fecharEncerrarBeneficio(): void {
    this.encerrarBeneficioAlvo.set(null);
  }

  protected confirmarEncerrarBeneficio(): void {
    const profissional = this.profissional();
    const adesao = this.encerrarBeneficioAlvo();
    if (!profissional || !adesao || this.encerrarBeneficioForm.invalid) {
      this.encerrarBeneficioForm.markAllAsTouched();
      return;
    }

    this.submittingEncerrarBeneficio.set(true);
    this.encerrarBeneficioErrorMessage.set(null);

    const dataFim = this.encerrarBeneficioForm.getRawValue().dataFim;
    this.adesaoBeneficioService.encerrar(adesao.uuid, dataFim).subscribe({
      next: () => {
        this.submittingEncerrarBeneficio.set(false);
        this.encerrarBeneficioAlvo.set(null);
        this.carregarBeneficios(profissional.matricula);
      },
      error: (error: HttpErrorResponse) => {
        this.submittingEncerrarBeneficio.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.encerrarBeneficioErrorMessage.set(body?.message ?? 'Não foi possível encerrar a adesão. Tente novamente.');
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

  protected carregarPonto(): void {
    this.carregandoPonto.set(true);
    const raw = this.filtroPontoForm.getRawValue();
    this.registroPontoService.listarPorProfissional(this.matriculaAtual, raw.dataInicio || undefined, raw.dataFim || undefined).subscribe({
      next: (registros) => {
        this.registrosPonto.set(registros);
        this.carregandoPonto.set(false);
      },
      error: () => {
        this.registrosPonto.set([]);
        this.carregandoPonto.set(false);
      },
    });
  }

  protected registrarPonto(): void {
    const profissional = this.profissional();
    if (!profissional || this.pontoForm.invalid) {
      this.pontoForm.markAllAsTouched();
      return;
    }

    this.submittingPonto.set(true);
    this.pontoErrorMessage.set(null);

    const raw = this.pontoForm.getRawValue();
    this.registroPontoService
      .criar({
        matriculaProfissional: profissional.matricula,
        dataHora: raw.dataHora,
        tipo: raw.tipo as TipoRegistroPonto,
      })
      .subscribe({
        next: () => {
          this.submittingPonto.set(false);
          this.pontoForm.reset({ tipo: '', dataHora: '' });
          this.carregarPonto();
        },
        error: (error: HttpErrorResponse) => {
          this.submittingPonto.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.pontoErrorMessage.set(body?.message ?? 'Não foi possível registrar o ponto. Tente novamente.');
        },
      });
  }

  protected abrirSolicitarCorrecaoPonto(registro: RegistroPontoResponseDto): void {
    this.correcaoPontoForm.reset({
      tipoProposto: registro.tipo,
      dataHoraProposta: registro.dataHora,
      justificativa: '',
    });
    this.correcaoPontoErrorMessage.set(null);
    this.correcaoPontoAlvo.set(registro);
  }

  protected fecharCorrecaoPontoModal(): void {
    this.correcaoPontoAlvo.set(null);
  }

  protected confirmarSolicitarCorrecaoPonto(): void {
    const alvo = this.correcaoPontoAlvo();
    if (!alvo || this.correcaoPontoForm.invalid) {
      this.correcaoPontoForm.markAllAsTouched();
      return;
    }

    this.submittingCorrecaoPonto.set(true);
    this.correcaoPontoErrorMessage.set(null);

    const raw = this.correcaoPontoForm.getRawValue();
    this.registroPontoService
      .solicitarCorrecao(alvo.uuid, {
        dataHoraProposta: raw.dataHoraProposta,
        tipoProposto: raw.tipoProposto as TipoRegistroPonto,
        justificativa: raw.justificativa,
      })
      .subscribe({
        next: () => {
          this.submittingCorrecaoPonto.set(false);
          this.correcaoPontoAlvo.set(null);
          this.carregarPonto();
        },
        error: (error: HttpErrorResponse) => {
          this.submittingCorrecaoPonto.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.correcaoPontoErrorMessage.set(body?.message ?? 'Não foi possível solicitar a correção. Tente novamente.');
        },
      });
  }

  protected aprovarCorrecaoPonto(registro: RegistroPontoResponseDto): void {
    this.resolvendoCorrecaoUuid.set(registro.uuid);
    this.pontoErrorMessage.set(null);
    this.registroPontoService.aprovarCorrecao(registro.uuid).subscribe({
      next: () => {
        this.resolvendoCorrecaoUuid.set(null);
        this.carregarPonto();
      },
      error: (error: HttpErrorResponse) => {
        this.resolvendoCorrecaoUuid.set(null);
        const body = error.error as ErrorResponseDto | undefined;
        this.pontoErrorMessage.set(body?.message ?? 'Não foi possível aprovar a correção. Tente novamente.');
      },
    });
  }

  protected rejeitarCorrecaoPonto(registro: RegistroPontoResponseDto): void {
    this.resolvendoCorrecaoUuid.set(registro.uuid);
    this.pontoErrorMessage.set(null);
    this.registroPontoService.rejeitarCorrecao(registro.uuid).subscribe({
      next: () => {
        this.resolvendoCorrecaoUuid.set(null);
        this.carregarPonto();
      },
      error: (error: HttpErrorResponse) => {
        this.resolvendoCorrecaoUuid.set(null);
        const body = error.error as ErrorResponseDto | undefined;
        this.pontoErrorMessage.set(body?.message ?? 'Não foi possível rejeitar a correção. Tente novamente.');
      },
    });
  }

  private carregarFolhas(matricula: string): void {
    this.carregandoFolhas.set(true);
    this.folhaPagamentoService.listarPorProfissional(matricula).subscribe({
      next: (folhas) => {
        this.folhas.set(folhas);
        this.carregandoFolhas.set(false);
      },
      error: () => {
        this.folhas.set([]);
        this.carregandoFolhas.set(false);
      },
    });
  }

  protected registrarFolha(): void {
    const profissional = this.profissional();
    if (!profissional || this.folhaForm.invalid) {
      this.folhaForm.markAllAsTouched();
      return;
    }

    this.submittingFolha.set(true);
    this.folhaErrorMessage.set(null);

    const raw = this.folhaForm.getRawValue();
    this.folhaPagamentoService
      .criar({
        matriculaProfissional: profissional.matricula,
        competencia: raw.competencia,
        proventos: Number(raw.proventos),
        descontos: Number(raw.descontos),
        encargos: Number(raw.encargos),
        total: Number(raw.total),
      })
      .subscribe({
        next: () => {
          this.submittingFolha.set(false);
          this.folhaForm.reset({ competencia: '', proventos: '', descontos: '', encargos: '', total: '' });
          this.carregarFolhas(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingFolha.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.folhaErrorMessage.set(body?.message ?? 'Não foi possível registrar a folha de pagamento. Tente novamente.');
        },
      });
  }

  protected selecionarAbaSst(aba: AbaSst): void {
    this.sstAbaAtiva.set(aba);
  }

  private carregarExames(matricula: string): void {
    this.carregandoExames.set(true);
    this.exameOcupacionalService.listarPorProfissional(matricula).subscribe({
      next: (exames) => {
        this.exames.set(exames);
        this.carregandoExames.set(false);
      },
      error: () => {
        this.exames.set([]);
        this.carregandoExames.set(false);
      },
    });
  }

  protected registrarExame(): void {
    const profissional = this.profissional();
    if (!profissional || this.exameForm.invalid) {
      this.exameForm.markAllAsTouched();
      return;
    }

    this.submittingExame.set(true);
    this.exameErrorMessage.set(null);

    const raw = this.exameForm.getRawValue();
    this.exameOcupacionalService
      .criar({
        matriculaProfissional: profissional.matricula,
        tipo: raw.tipo as TipoExameOcupacional,
        dataRealizacao: raw.dataRealizacao,
        dataValidade: raw.dataValidade || undefined,
        resultado: raw.resultado as ResultadoExameOcupacional,
        asoUrl: raw.asoUrl || undefined,
      })
      .subscribe({
        next: () => {
          this.submittingExame.set(false);
          this.exameForm.reset({ tipo: '', dataRealizacao: '', dataValidade: '', resultado: '', asoUrl: '' });
          this.carregarExames(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingExame.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.exameErrorMessage.set(body?.message ?? 'Não foi possível registrar o exame. Tente novamente.');
        },
      });
  }

  private carregarAcidentes(matricula: string): void {
    this.carregandoAcidentes.set(true);
    this.acidenteTrabalhoService.listarPorProfissional(matricula).subscribe({
      next: (acidentes) => {
        this.acidentes.set(acidentes);
        this.carregandoAcidentes.set(false);
      },
      error: () => {
        this.acidentes.set([]);
        this.carregandoAcidentes.set(false);
      },
    });
  }

  protected registrarAcidente(): void {
    const profissional = this.profissional();
    if (!profissional || this.acidenteForm.invalid) {
      this.acidenteForm.markAllAsTouched();
      return;
    }

    this.submittingAcidente.set(true);
    this.acidenteErrorMessage.set(null);

    const raw = this.acidenteForm.getRawValue();
    this.acidenteTrabalhoService
      .criar({
        matriculaProfissional: profissional.matricula,
        dataHora: raw.dataHora,
        descricao: raw.descricao,
        catEmitida: raw.catEmitida,
        catUrl: raw.catUrl || undefined,
        diasAfastamento: raw.diasAfastamento ? Number(raw.diasAfastamento) : undefined,
      })
      .subscribe({
        next: () => {
          this.submittingAcidente.set(false);
          this.acidenteForm.reset({ dataHora: '', descricao: '', catEmitida: false, catUrl: '', diasAfastamento: '' });
          this.carregarAcidentes(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingAcidente.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.acidenteErrorMessage.set(body?.message ?? 'Não foi possível registrar o acidente. Tente novamente.');
        },
      });
  }

  private carregarEpis(matricula: string): void {
    this.carregandoEpis.set(true);
    this.epiService.listarPorProfissional(matricula).subscribe({
      next: (epis) => {
        this.epis.set(epis);
        this.carregandoEpis.set(false);
      },
      error: () => {
        this.epis.set([]);
        this.carregandoEpis.set(false);
      },
    });
  }

  /** Sem campo de situação no backend — derivado da presença de dataDevolucao (ver ADR-0027). */
  protected situacaoDoEpi(epi: EpiResponseDto): 'Em uso' | 'Devolvido' {
    return epi.dataDevolucao ? 'Devolvido' : 'Em uso';
  }

  protected registrarEpi(): void {
    const profissional = this.profissional();
    if (!profissional || this.epiForm.invalid) {
      this.epiForm.markAllAsTouched();
      return;
    }

    this.submittingEpi.set(true);
    this.epiErrorMessage.set(null);

    const raw = this.epiForm.getRawValue();
    this.epiService
      .criar({
        matriculaProfissional: profissional.matricula,
        tipo: raw.tipo,
        numeroCA: raw.numeroCA || undefined,
        dataEntrega: raw.dataEntrega,
        dataDevolucao: raw.dataDevolucao || undefined,
      })
      .subscribe({
        next: () => {
          this.submittingEpi.set(false);
          this.epiForm.reset({ tipo: '', numeroCA: '', dataEntrega: '', dataDevolucao: '' });
          this.carregarEpis(profissional.matricula);
        },
        error: (error: HttpErrorResponse) => {
          this.submittingEpi.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.epiErrorMessage.set(body?.message ?? 'Não foi possível registrar o EPI. Tente novamente.');
        },
      });
  }
}
