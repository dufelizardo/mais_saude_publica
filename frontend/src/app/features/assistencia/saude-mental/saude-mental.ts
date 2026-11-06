import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { EquipeResumoDto, MembroEquipeDto, VinculoEquipeDto } from '../../../core/models/equipe';
import { ProcedimentoReguladoResponseDto } from '../../../core/models/regulacao';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { MOTIVOS_SAIDA, MotivoSaidaAcompanhamento, PERIODICIDADES, PeriodicidadeAcompanhamento, SITUACOES_ACOMPANHAMENTO } from '../../../core/models/programa';
import {
  AcompanhamentoSaudeMentalDto,
  CasosSaudeMentalDto,
  DESFECHOS,
  DESFECHOS_QUE_ACOMPANHAM,
  DesfechoAcolhimentoSaudeMental,
  CompartilhamentoCuidadoDto,
  CrisesSaudeMentalDto,
  DESFECHOS_CRISE,
  ETAPAS_CRISE,
  EventoCriseDto,
  NIVEIS_RISCO,
  TIPOS_CRISE,
  TIPOS_RISCO,
  PAPEIS_REDE,
  PRIORIDADES_ENCAMINHAMENTO,
  SITUACOES_TRANSFERENCIA,
  STATUS_ENCAMINHAMENTO,
  TransferenciaCuidadoDto,
  TransferenciasCuidadoDto,
  PapelRedeApoio,
  PendenciaSaudeMental,
  PessoaRedeApoioDto,
  ProjetoTerapeuticoDto,
  RISCOS,
  RiscoInicialSaudeMental,
  SITUACOES_INTERVENCAO,
  SITUACOES_OBJETIVO,
  SituacaoIntervencaoSaudeMental,
  SituacaoObjetivoTerapeutico,
  TipoIntervencaoSaudeMentalDto,
} from '../../../core/models/saude-mental';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { EquipeService } from '../../../core/services/equipe';
import { PacienteService } from '../../../core/services/paciente';
import { RegulacaoService } from '../../../core/services/regulacao';
import { semVinculo } from '../../../core/services/prontuario';
import { SaudeMentalService } from '../../../core/services/saude-mental';
import { IntersetorialidadeService } from '../../../core/services/intersetorialidade';
import {
  EncaminhamentoIntersetorialDto,
  InstituicaoExternaDto,
  SETORES_INTERSETORIAIS,
  SITUACOES_ENCAMINHAMENTO_INTERSETORIAL,
  TIPOS_INSTITUICAO,
} from '../../../core/models/intersetorialidade';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { AcessoProntuario } from '../../../shared/acesso-prontuario/acesso-prontuario';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta =
  | { tipo: 'acolher' }
  | { tipo: 'avaliar'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'referencia'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'encerrar'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'pts'; caso: AcompanhamentoSaudeMentalDto; base: ProjetoTerapeuticoDto | null }
  | { tipo: 'intervencao'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'rede'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'sairRede'; caso: AcompanhamentoSaudeMentalDto; pessoa: PessoaRedeApoioDto }
  | { tipo: 'intersetorial'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'retornoIntersetorial'; caso: AcompanhamentoSaudeMentalDto; enc: EncaminhamentoIntersetorialDto; modo: 'recebimento' | 'retorno' | 'cancelamento' }
  | { tipo: 'encaminhar'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'compartilhar'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'fimCompartilhamento'; caso: AcompanhamentoSaudeMentalDto; compartilhamento: CompartilhamentoCuidadoDto }
  | { tipo: 'transferir'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'aceitar'; transferencia: TransferenciaCuidadoDto }
  | { tipo: 'recusar'; transferencia: TransferenciaCuidadoDto }
  | { tipo: 'cancelarTransferencia'; caso: AcompanhamentoSaudeMentalDto; transferencia: TransferenciaCuidadoDto }
  | { tipo: 'risco'; caso: AcompanhamentoSaudeMentalDto }
  | { tipo: 'novaCrise'; caso: AcompanhamentoSaudeMentalDto | null }
  | { tipo: 'crise'; criseId: string };

type Aba = 'resumo' | 'risco' | 'pts' | 'intervencoes' | 'rede' | 'atencao' | 'acolhimentos' | 'avaliacoes' | 'referencia';

/**
 * Saúde Mental, fatia SM1 (ADR-0120): os casos do escopo (meus, da equipe, todos), o acolhimento que abre o caso, e o
 * detalhe com acolhimentos, avaliações e a referência como histórico. O conteúdo clínico só aparece com
 * {@code conteudoLiberado}; sem ele, o caso mostra só quem é a referência e oferece o acesso justificado (ADR-0076).
 * Com a SM2 (ADR-0121): o PTS versionado (elaborar e revisar a partir da versão vigente), as intervenções e a rede de
 * apoio, e os indicadores "Sem PTS" e "PTS a revisar".
 */
@Component({
  selector: 'app-saude-mental',
  imports: [ReactiveFormsModule, RouterLink, Drawer, AcessoProntuario],
  templateUrl: './saude-mental.html',
})
export class SaudeMental {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly service = inject(SaudeMentalService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly equipeService = inject(EquipeService);
  private readonly pacienteService = inject(PacienteService);
  private readonly regulacaoService = inject(RegulacaoService);
  private readonly intersetorialService = inject(IntersetorialidadeService);
  /** As instituições ativas do catálogo intersetorial (ADR-0155), carregadas ao encaminhar. */
  protected readonly instituicoes = signal<InstituicaoExternaDto[]>([]);

  protected readonly riscos = RISCOS;
  protected readonly listaRiscos = Object.keys(RISCOS) as RiscoInicialSaudeMental[];
  protected readonly desfechos = DESFECHOS;
  protected readonly listaDesfechos = Object.keys(DESFECHOS) as DesfechoAcolhimentoSaudeMental[];
  protected readonly situacoes = SITUACOES_ACOMPANHAMENTO;
  protected readonly motivosSaida = MOTIVOS_SAIDA;
  protected readonly listaMotivosSaida = Object.keys(MOTIVOS_SAIDA) as MotivoSaidaAcompanhamento[];
  protected readonly periodicidades = PERIODICIDADES;
  protected readonly listaPeriodicidades = (Object.keys(PERIODICIDADES) as PeriodicidadeAcompanhamento[]).filter((p) => p !== 'SEM_PERIODICIDADE');
  protected readonly situacoesObjetivo = SITUACOES_OBJETIVO;
  protected readonly listaSituacoesObjetivo = Object.keys(SITUACOES_OBJETIVO) as SituacaoObjetivoTerapeutico[];
  protected readonly situacoesIntervencao = SITUACOES_INTERVENCAO;
  protected readonly papeisRede = PAPEIS_REDE;
  protected readonly listaPapeisRede = Object.keys(PAPEIS_REDE) as PapelRedeApoio[];
  protected readonly setoresIntersetoriais = SETORES_INTERSETORIAIS;
  protected readonly tiposInstituicao = TIPOS_INSTITUICAO;
  protected readonly situacoesIntersetoriais = SITUACOES_ENCAMINHAMENTO_INTERSETORIAL;
  protected readonly statusEncaminhamento = STATUS_ENCAMINHAMENTO;
  protected readonly prioridades = PRIORIDADES_ENCAMINHAMENTO;
  protected readonly listaPrioridades = Object.keys(PRIORIDADES_ENCAMINHAMENTO);
  protected readonly situacoesTransferencia = SITUACOES_TRANSFERENCIA;
  protected readonly tiposRisco = TIPOS_RISCO;
  protected readonly listaTiposRisco = Object.keys(TIPOS_RISCO);
  protected readonly niveisRisco = NIVEIS_RISCO;
  protected readonly listaNiveisRisco = Object.keys(NIVEIS_RISCO);
  protected readonly tiposCrise = TIPOS_CRISE;
  protected readonly listaTiposCrise = Object.keys(TIPOS_CRISE);
  protected readonly etapasCrise = ETAPAS_CRISE;
  protected readonly listaEtapasCrise = Object.keys(ETAPAS_CRISE);
  protected readonly desfechosCrise = DESFECHOS_CRISE;
  protected readonly listaDesfechosCrise = Object.keys(DESFECHOS_CRISE);

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly casos = signal<CasosSaudeMentalDto | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  protected readonly escopo = signal<'' | 'TECNICO' | 'EQUIPE'>('');
  protected readonly pendencia = signal<PendenciaSaudeMental>('');
  protected readonly tipos = signal<TipoIntervencaoSaudeMentalDto[]>([]);
  /** Crises do escopo (ADR-0123) e a crise aberta na gaveta. */
  protected readonly crisesEscopo = signal<CrisesSaudeMentalDto | null>(null);
  protected readonly criseAberta = signal<EventoCriseDto | null>(null);
  /** Transferências pendentes do escopo (ADR-0122): `null` sem acesso. */
  protected readonly transferencias = signal<TransferenciasCuidadoDto | null>(null);
  protected readonly procedimentos = signal<ProcedimentoReguladoResponseDto[]>([]);
  /** Equipes que podem ser parceiras no compartilhamento (todas as ativas) e as do apoio matricial da referência. */
  protected readonly todasEquipes = signal<EquipeResumoDto[]>([]);
  protected readonly equipesMatriciamento = signal<VinculoEquipeDto[]>([]);
  /** Versão do PTS aberta no histórico (null: nenhuma). */
  protected readonly versaoAberta = signal<number | null>(null);
  protected readonly unidadeFiltro = signal('');
  protected readonly pacienteFiltro = signal<string | null>(null);
  protected readonly busca = signal('');
  protected readonly selecionadoId = signal<string | null>(null);
  protected readonly detalhe = signal<AcompanhamentoSaudeMentalDto | null>(null);
  protected readonly bloqueio = signal<string | null>(null);
  protected readonly aba = signal<Aba>('resumo');

  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly buscaPaciente = signal('');
  protected readonly equipes = signal<EquipeResumoDto[]>([]);
  protected readonly membros = signal<MembroEquipeDto[]>([]);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly acolherForm = this.fb.nonNullable.group({
    pacienteId: [''],
    unidadeId: [''],
    motivoProcura: [''],
    necessidadeImediata: [''],
    vulnerabilidade: [''],
    riscoInicial: ['' as RiscoInicialSaudeMental | ''],
    desfecho: ['' as DesfechoAcolhimentoSaudeMental | ''],
    encaminhamentoDestino: [''],
    equipeId: [''],
    tecnicoMatricula: [''],
  });
  protected readonly avaliacaoForm = this.fb.nonNullable.group({
    contextoHistoria: [''],
    aspectosPsicossociais: [''],
    fatoresRisco: [''],
    fatoresProtecao: [''],
    necessidades: [''],
    cid: [''],
  });
  protected readonly referenciaForm = this.fb.nonNullable.group({ equipeId: [''], tecnicoMatricula: [''], motivo: [''] });
  protected readonly encerrarForm = this.fb.nonNullable.group({ motivoSaida: ['' as MotivoSaidaAcompanhamento | ''], observacao: [''], fim: [''] });
  protected readonly ptsForm = this.fb.nonNullable.group({
    necessidades: [''],
    periodicidadeRevisao: ['TRIMESTRAL' as PeriodicidadeAcompanhamento],
    revisarEm: [''],
    pactuadoComPessoa: [false],
    pactuacaoObservacao: [''],
    sinteseRevisao: [''],
    objetivos: this.fb.array<FormGroup>([]),
    previstas: this.fb.array<FormGroup>([]),
  });
  protected readonly intervencaoForm = this.fb.nonNullable.group({
    tipoId: [''], situacao: ['REALIZADA' as SituacaoIntervencaoSaudeMental], realizadaEm: [''], resumo: [''], compartilhadoComMatricula: [''], previstaId: [''],
    equipeMatriciadaId: [''],
  });
  protected readonly redeForm = this.fb.nonNullable.group({ nome: [''], papel: ['' as PapelRedeApoio | ''], vinculo: [''], telefone: [''], observacao: [''] });
  protected readonly sairRedeForm = this.fb.nonNullable.group({ motivo: [''] });
  protected readonly intersetorialForm = this.fb.nonNullable.group({ instituicaoId: [''], motivo: [''], enviadoEm: [''], retornoEsperadoEm: [''] });
  protected readonly retornoIntersetorialForm = this.fb.nonNullable.group({ data: [''], desfecho: ['' as 'CONCLUIDO' | 'RECUSADO' | ''], texto: [''] });
  protected readonly encaminharForm = this.fb.nonNullable.group({
    modo: ['NOVO' as 'NOVO' | 'VINCULAR'], solicitacaoId: [''], procedimentoId: [''], cid: [''], justificativa: [''], prioridade: [''], profissionalMatricula: [''],
  });
  protected readonly compartilharForm = this.fb.nonNullable.group({ equipeId: [''], objetivo: [''] });
  protected readonly motivoRedeForm = this.fb.nonNullable.group({ motivo: [''] });
  protected readonly transferirForm = this.fb.nonNullable.group({ unidadeDestinoId: [''], resumo: [''] });
  protected readonly aceiteForm = this.fb.nonNullable.group({ equipeId: [''], tecnicoMatricula: [''] });
  protected readonly riscoForm = this.fb.nonNullable.group({
    tipo: [''], nivel: [''], fatores: [''], medidas: [''], planoSeguranca: [false], frequenciaContato: [''], reavaliarEm: [''],
  });
  protected readonly criseForm = this.fb.nonNullable.group({ pacienteId: [''], unidadeId: [''], tipo: [''], descricao: [''], localOcorrencia: [''] });
  protected readonly etapaForm = this.fb.nonNullable.group({ etapa: [''], descricao: [''] });
  protected readonly desfechoForm = this.fb.nonNullable.group({ desfecho: [''] });
  protected readonly notificacaoForm = this.fb.nonNullable.group({ numero: [''] });

  protected get objetivos(): FormArray<FormGroup> {
    return this.ptsForm.controls.objetivos;
  }

  protected get previstas(): FormArray<FormGroup> {
    return this.ptsForm.controls.previstas;
  }

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────

  protected readonly podeAcolher = computed(() => this.pode('SAUDE_MENTAL.ACOLHER'));
  protected readonly podeAcompanhar = computed(() => this.pode('SAUDE_MENTAL.ACOMPANHAR'));

  protected readonly filtrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    return (this.casos()?.lista ?? []).filter((c) => !q || `${c.pacienteNome} ${c.tecnicoNome ?? ''} ${c.equipeNome ?? ''}`.toLowerCase().includes(q));
  });

  protected readonly pacientesEncontrados = computed(() => {
    const q = this.buscaPaciente().trim().toLowerCase();
    if (q.length < 2) return [];
    const digitos = q.replace(/\D/g, '');
    return this.pacientes()
      .filter((p) => p.ativo && (p.nome.toLowerCase().includes(q) || (digitos.length >= 3 && ((p.cpf ?? '').includes(digitos) || (p.cartaoSus ?? '').includes(digitos)))))
      .slice(0, 8);
  });

  /** O desfecho escolhido no acolhimento pede equipe e técnico de referência. */
  protected readonly acolhimentoAcompanha = signal(false);
  protected readonly acolhimentoEncaminha = signal(false);

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.service.tiposIntervencao().pipe(catchError(() => of([]))).subscribe((t) => this.tipos.set(t));
    this.carregarTransferencias();
    this.carregarCrises();
    this.pacienteFiltro.set(this.route.snapshot.queryParamMap.get('paciente'));
    this.selecionadoId.set(this.route.snapshot.queryParamMap.get('caso'));
    this.carregar();
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.service.casos(this.filtroCasos()).subscribe({
      next: (r) => {
        this.casos.set(r);
        this.carregando.set(false);
        const atual = this.selecionadoId();
        if (atual) this.selecionar(atual, false);
        else if (this.pacienteFiltro() && r.lista.length) this.selecionar(r.lista[0].uuid, false);
      },
      error: (e: HttpErrorResponse) => {
        this.carregando.set(false);
        this.casos.set(null);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar os casos.');
      },
    });
  }

  /** Crises das unidades do escopo (ADR-0123). */
  protected carregarCrises(): void {
    if (!this.pode('SAUDE_MENTAL.CRISE', 'SAUDE_MENTAL.ACOMPANHAR')) return;
    this.service.crises(this.unidadeFiltro() || undefined).pipe(catchError(() => of(null))).subscribe((c) => this.crisesEscopo.set(c));
  }

  /** As crises em curso ou com notificação pendente: o painel do topo. */
  protected crisesEmAtencao(): EventoCriseDto[] {
    return (this.crisesEscopo()?.lista ?? []).filter((c) => c.situacao === 'EM_CURSO' || c.notificacaoPendente);
  }

  /** Transferências que chegam e saem das unidades do escopo (ADR-0122). */
  protected carregarTransferencias(): void {
    if (!this.pode('SAUDE_MENTAL.ACOMPANHAR')) return;
    this.service.transferencias(this.unidadeFiltro() || undefined).pipe(catchError(() => of(null))).subscribe((t) => this.transferencias.set(t));
  }

  private filtroCasos() {
    return { unidadeId: this.unidadeFiltro() || undefined, pacienteId: this.pacienteFiltro() || undefined, escopo: this.escopo(), pendencia: this.pendencia() };
  }

  protected filtrarPendencia(p: PendenciaSaudeMental): void {
    this.pendencia.set(this.pendencia() === p ? '' : p);
    this.carregar();
  }

  protected filtrarEscopo(e: '' | 'TECNICO' | 'EQUIPE'): void {
    this.escopo.set(e);
    this.carregar();
  }

  protected filtrarUnidade(uuid: string): void {
    this.unidadeFiltro.set(uuid);
    this.carregar();
  }

  protected limparPaciente(): void {
    this.pacienteFiltro.set(null);
    this.router.navigate([], { queryParams: { paciente: null }, queryParamsHandling: 'merge', replaceUrl: true });
    this.carregar();
  }

  /** Abre o caso: leitura auditada, com vínculo; sem conteúdo liberado, mostra só a referência. */
  protected selecionar(uuid: string, atualizarUrl = true): void {
    this.selecionadoId.set(uuid);
    this.aba.set('resumo');
    this.versaoAberta.set(null);
    this.bloqueio.set(null);
    if (atualizarUrl) this.router.navigate([], { queryParams: { caso: uuid }, queryParamsHandling: 'merge', replaceUrl: true });
    this.service.caso(uuid).subscribe({
      next: (d) => this.detalhe.set(d),
      error: (e: unknown) => {
        this.detalhe.set(null);
        this.bloqueio.set(semVinculo(e) ?? 'Não foi possível abrir o caso.');
      },
    });
  }

  protected recarregarDetalhe(): void {
    const id = this.selecionadoId();
    if (id) this.selecionar(id, false);
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  protected dataHora(iso: string | null | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  protected iniciais(nome: string): string {
    return nome
      .split(/\s+/)
      .filter((p) => p.length > 2 && !p.endsWith('.'))
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  protected erroCampo(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    if (g.tipo === 'acolher') return 'Acolhimento em saúde mental';
    if (g.tipo === 'avaliar') return 'Nova avaliação';
    if (g.tipo === 'referencia') return 'Trocar referência';
    if (g.tipo === 'pts') return g.base ? `Revisar o PTS (versão ${g.base.versao + 1})` : 'Elaborar o PTS';
    if (g.tipo === 'intervencao') return 'Registrar intervenção';
    if (g.tipo === 'rede') return 'Incluir na rede de apoio';
    if (g.tipo === 'sairRede') return 'Tirar da rede de apoio';
    if (g.tipo === 'intersetorial') return 'Encaminhar a outro setor';
    if (g.tipo === 'retornoIntersetorial') {
      return g.modo === 'recebimento' ? 'A instituição recebeu' : g.modo === 'retorno' ? 'Retorno da instituição' : 'Cancelar o encaminhamento';
    }
    if (g.tipo === 'encaminhar') return 'Encaminhar pela Regulação';
    if (g.tipo === 'compartilhar') return 'Compartilhar o cuidado';
    if (g.tipo === 'fimCompartilhamento') return 'Encerrar o compartilhamento';
    if (g.tipo === 'transferir') return 'Transferir o cuidado';
    if (g.tipo === 'aceitar') return 'Aceitar a transferência';
    if (g.tipo === 'recusar') return 'Recusar a transferência';
    if (g.tipo === 'cancelarTransferencia') return 'Cancelar a transferência';
    if (g.tipo === 'risco') return 'Avaliar risco';
    if (g.tipo === 'novaCrise') return 'Registrar crise';
    if (g.tipo === 'crise') return 'Crise';
    return 'Encerrar o caso';
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirAcolher(): void {
    this.acolherForm.reset({
      pacienteId: '', unidadeId: this.unidadeFiltro(), motivoProcura: '', necessidadeImediata: '', vulnerabilidade: '',
      riscoInicial: '', desfecho: '', encaminhamentoDestino: '', equipeId: '', tecnicoMatricula: '',
    });
    this.acolhimentoAcompanha.set(false);
    this.acolhimentoEncaminha.set(false);
    this.buscaPaciente.set('');
    this.membros.set([]);
    if (!this.pacientes().length) this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    this.carregarEquipes(this.unidadeFiltro());
    this.abrir({ tipo: 'acolher' });
  }

  protected escolherPaciente(p: PacienteResponseDto): void {
    this.acolherForm.patchValue({ pacienteId: p.uuid });
    this.buscaPaciente.set(p.nome);
  }

  protected escolherUnidadeAcolhimento(uuid: string): void {
    this.acolherForm.patchValue({ unidadeId: uuid, equipeId: '', tecnicoMatricula: '' });
    this.membros.set([]);
    this.carregarEquipes(uuid);
  }

  protected escolherDesfecho(d: string): void {
    this.acolherForm.patchValue({ desfecho: d as DesfechoAcolhimentoSaudeMental | '' });
    this.acolhimentoAcompanha.set(DESFECHOS_QUE_ACOMPANHAM.includes(d as DesfechoAcolhimentoSaudeMental));
    this.acolhimentoEncaminha.set(d === 'ENCAMINHAMENTO');
  }

  /** Equipe de referência: os técnicos são os membros vigentes dela. */
  protected escolherEquipe(uuid: string, form: 'acolher' | 'referencia'): void {
    (form === 'acolher' ? this.acolherForm : this.referenciaForm).patchValue({ equipeId: uuid, tecnicoMatricula: '' });
    this.membros.set([]);
    if (!uuid) return;
    this.equipeService.buscar(uuid).pipe(catchError(() => of(null))).subscribe((e) => this.membros.set(e?.membros.filter((m) => !m.fim) ?? []));
  }

  private carregarEquipes(unidadeId: string): void {
    this.equipes.set([]);
    if (!unidadeId) return;
    this.equipeService.listar(unidadeId).pipe(catchError(() => of(null))).subscribe((r) => this.equipes.set(r?.lista.filter((e) => e.ativa) ?? []));
  }

  protected abrirAvaliar(caso: AcompanhamentoSaudeMentalDto): void {
    this.avaliacaoForm.reset({ contextoHistoria: '', aspectosPsicossociais: '', fatoresRisco: '', fatoresProtecao: '', necessidades: '', cid: '' });
    this.abrir({ tipo: 'avaliar', caso });
  }

  protected abrirReferencia(caso: AcompanhamentoSaudeMentalDto): void {
    this.referenciaForm.reset({ equipeId: '', tecnicoMatricula: '', motivo: '' });
    this.membros.set([]);
    this.carregarEquipes(caso.unidadeId);
    this.abrir({ tipo: 'referencia', caso });
  }

  protected abrirEncerrar(caso: AcompanhamentoSaudeMentalDto): void {
    this.encerrarForm.reset({ motivoSaida: '', observacao: '', fim: '' });
    this.abrir({ tipo: 'encerrar', caso });
  }

  /** Elaborar (sem base) ou revisar: a revisão vem pré-preenchida com a versão vigente. */
  protected abrirPts(caso: AcompanhamentoSaudeMentalDto): void {
    const base = caso.pts.find((p) => p.vigente) ?? null;
    this.ptsForm.reset({
      necessidades: base?.necessidades ?? '', periodicidadeRevisao: base?.periodicidadeRevisao ?? 'TRIMESTRAL', revisarEm: '',
      pactuadoComPessoa: base?.pactuadoComPessoa ?? false, pactuacaoObservacao: base?.pactuacaoObservacao ?? '', sinteseRevisao: '',
    });
    this.objetivos.clear();
    this.previstas.clear();
    (base?.objetivos.length ? base.objetivos : [null]).forEach((o) => this.objetivos.push(this.novoObjetivo(o)));
    (base?.intervencoesPrevistas ?? []).forEach((i) => this.previstas.push(this.novaPrevista(i)));
    this.membros.set([]);
    if (caso.equipeId) {
      this.equipeService.buscar(caso.equipeId).pipe(catchError(() => of(null))).subscribe((e) => this.membros.set(e?.membros.filter((m) => !m.fim) ?? []));
    }
    this.abrir({ tipo: 'pts', caso, base });
  }

  private novoObjetivo(o: ProjetoTerapeuticoDto['objetivos'][number] | null): FormGroup {
    return this.fb.nonNullable.group({
      descricao: [o?.descricao ?? ''], meta: [o?.meta ?? ''], prazo: [o?.prazo ?? ''],
      responsavelMatricula: [o?.responsavelMatricula ?? ''], situacao: [(o?.situacao ?? 'EM_ANDAMENTO') as SituacaoObjetivoTerapeutico],
    });
  }

  private novaPrevista(i: ProjetoTerapeuticoDto['intervencoesPrevistas'][number] | null): FormGroup {
    return this.fb.nonNullable.group({
      tipoId: [i?.tipoId ?? ''], frequencia: [i?.frequencia ?? ''], responsavelMatricula: [i?.responsavelMatricula ?? ''], servico: [i?.servico ?? ''],
    });
  }

  protected adicionarObjetivo(): void {
    this.objetivos.push(this.novoObjetivo(null));
  }

  protected removerObjetivo(i: number): void {
    this.objetivos.removeAt(i);
  }

  protected adicionarPrevista(): void {
    this.previstas.push(this.novaPrevista(null));
  }

  protected removerPrevista(i: number): void {
    this.previstas.removeAt(i);
  }

  protected abrirIntervencao(caso: AcompanhamentoSaudeMentalDto): void {
    this.intervencaoForm.reset({ tipoId: '', situacao: 'REALIZADA', realizadaEm: '', resumo: '', compartilhadoComMatricula: '', previstaId: '', equipeMatriciadaId: '' });
    this.equipesMatriciamento.set([]);
    if (caso.equipeId) {
      // Apoio matricial da ADR-0103, nos dois sentidos: as equipes que a referência apoia e as que a apoiam.
      this.equipeService.buscar(caso.equipeId).pipe(catchError(() => of(null)))
        .subscribe((e) => this.equipesMatriciamento.set([...(e?.apoiadas ?? []), ...(e?.apoiadaPor ?? [])]));
    }
    this.abrir({ tipo: 'intervencao', caso });
  }

  /** As intervenções previstas do PTS vigente, para ligar a intervenção realizada. */
  protected previstasVigentes(caso: AcompanhamentoSaudeMentalDto) {
    return caso.pts.find((p) => p.vigente)?.intervencoesPrevistas ?? [];
  }

  protected abrirRede(caso: AcompanhamentoSaudeMentalDto): void {
    this.redeForm.reset({ nome: '', papel: '', vinculo: '', telefone: '', observacao: '' });
    this.abrir({ tipo: 'rede', caso });
  }

  protected abrirSairRede(caso: AcompanhamentoSaudeMentalDto, pessoa: PessoaRedeApoioDto): void {
    this.sairRedeForm.reset({ motivo: '' });
    this.abrir({ tipo: 'sairRede', caso, pessoa });
  }

  protected abrirIntersetorial(caso: AcompanhamentoSaudeMentalDto): void {
    const prazo = new Date();
    prazo.setDate(prazo.getDate() + 30);
    this.intersetorialForm.reset({ instituicaoId: '', motivo: '', enviadoEm: this.hojeIso(), retornoEsperadoEm: prazo.toISOString().slice(0, 10) });
    this.intersetorialService.listar({ ativas: true }).pipe(catchError(() => of([]))).subscribe((l) => this.instituicoes.set(l));
    this.abrir({ tipo: 'intersetorial', caso });
  }

  protected abrirRetornoIntersetorial(caso: AcompanhamentoSaudeMentalDto, enc: EncaminhamentoIntersetorialDto, modo: 'recebimento' | 'retorno' | 'cancelamento'): void {
    this.retornoIntersetorialForm.reset({ data: this.hojeIso(), desfecho: '', texto: '' });
    this.abrir({ tipo: 'retornoIntersetorial', caso, enc, modo });
  }

  /** O tipo escolhido na intervenção é a discussão de caso em apoio matricial. */
  protected tipoEhMatriciamento(): boolean {
    return this.tipos().find((t) => t.uuid === this.intervencaoForm.controls.tipoId.value)?.codigo === 'MATRICIAMENTO';
  }

  protected abrirEncaminhar(caso: AcompanhamentoSaudeMentalDto): void {
    this.encaminharForm.reset({ modo: 'NOVO', solicitacaoId: '', procedimentoId: '', cid: '', justificativa: '', prioridade: '', profissionalMatricula: '' });
    if (!this.procedimentos().length) {
      this.regulacaoService.listarProcedimentos().pipe(catchError(() => of([]))).subscribe((p) => this.procedimentos.set(p.filter((x) => x.ativo)));
    }
    this.abrir({ tipo: 'encaminhar', caso });
  }

  protected abrirCompartilhar(caso: AcompanhamentoSaudeMentalDto): void {
    this.compartilharForm.reset({ equipeId: '', objetivo: '' });
    if (!this.todasEquipes().length) {
      this.equipeService.listar().pipe(catchError(() => of(null))).subscribe((r) => this.todasEquipes.set(r?.lista.filter((e) => e.ativa) ?? []));
    }
    this.abrir({ tipo: 'compartilhar', caso });
  }

  /** Equipes para o compartilhamento: todas as ativas, menos a de referência e as já parceiras. */
  protected equipesParceiras(caso: AcompanhamentoSaudeMentalDto): EquipeResumoDto[] {
    const fora = new Set([caso.equipeId, ...caso.compartilhamentos.filter((c) => !c.fim).map((c) => c.equipeId)]);
    return this.todasEquipes().filter((e) => !fora.has(e.uuid));
  }

  protected abrirFimCompartilhamento(caso: AcompanhamentoSaudeMentalDto, compartilhamento: CompartilhamentoCuidadoDto): void {
    this.motivoRedeForm.reset({ motivo: '' });
    this.abrir({ tipo: 'fimCompartilhamento', caso, compartilhamento });
  }

  protected abrirTransferir(caso: AcompanhamentoSaudeMentalDto): void {
    this.transferirForm.reset({ unidadeDestinoId: '', resumo: '' });
    this.abrir({ tipo: 'transferir', caso });
  }

  protected abrirAceitar(transferencia: TransferenciaCuidadoDto): void {
    this.aceiteForm.reset({ equipeId: '', tecnicoMatricula: '' });
    this.membros.set([]);
    this.carregarEquipes(transferencia.unidadeDestinoId);
    this.abrir({ tipo: 'aceitar', transferencia });
  }

  protected escolherEquipeAceite(uuid: string): void {
    this.aceiteForm.patchValue({ equipeId: uuid, tecnicoMatricula: '' });
    this.membros.set([]);
    if (!uuid) return;
    this.equipeService.buscar(uuid).pipe(catchError(() => of(null))).subscribe((e) => this.membros.set(e?.membros.filter((m) => !m.fim) ?? []));
  }

  protected abrirRecusar(transferencia: TransferenciaCuidadoDto): void {
    this.motivoRedeForm.reset({ motivo: '' });
    this.abrir({ tipo: 'recusar', transferencia });
  }

  protected abrirCancelarTransferencia(caso: AcompanhamentoSaudeMentalDto, transferencia: TransferenciaCuidadoDto): void {
    this.motivoRedeForm.reset({ motivo: '' });
    this.abrir({ tipo: 'cancelarTransferencia', caso, transferencia });
  }

  protected abrirRisco(caso: AcompanhamentoSaudeMentalDto, tipo = ''): void {
    this.riscoForm.reset({ tipo, nivel: '', fatores: '', medidas: '', planoSeguranca: false, frequenciaContato: '', reavaliarEm: '' });
    this.abrir({ tipo: 'risco', caso });
  }

  protected riscosVigentes(caso: AcompanhamentoSaudeMentalDto) {
    return caso.riscos.filter((r) => r.vigente && r.nivel !== 'SEM_RISCO_ATUAL');
  }

  protected abrirNovaCrise(caso: AcompanhamentoSaudeMentalDto | null): void {
    this.criseForm.reset({ pacienteId: caso?.pacienteId ?? '', unidadeId: caso?.unidadeId ?? this.unidadeFiltro(), tipo: '', descricao: '', localOcorrencia: '' });
    this.buscaPaciente.set(caso?.pacienteNome ?? '');
    if (!caso && !this.pacientes().length) this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    this.abrir({ tipo: 'novaCrise', caso });
  }

  protected escolherPacienteCrise(p: PacienteResponseDto): void {
    this.criseForm.patchValue({ pacienteId: p.uuid });
    this.buscaPaciente.set(p.nome);
  }

  protected abrirCrise(criseId: string): void {
    this.criseAberta.set(null);
    this.etapaForm.reset({ etapa: '', descricao: '' });
    this.desfechoForm.reset({ desfecho: '' });
    this.notificacaoForm.reset({ numero: '' });
    this.abrir({ tipo: 'crise', criseId });
    this.service.crise(criseId).subscribe({ next: (c) => this.criseAberta.set(c), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected registrarEtapa(c: EventoCriseDto): void {
    const v = this.etapaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.etapa) erros['etapaCrise'] = 'Escolha a etapa.';
    if (!v.descricao.trim()) erros['descricaoEtapa'] = 'Descreva a etapa.';
    if (!this.validar(erros)) return;
    this.enviarCrise(this.service.etapaCrise(c.uuid, v.etapa, v.descricao.trim()), 'Etapa registrada');
    this.etapaForm.reset({ etapa: '', descricao: '' });
  }

  protected encerrarCrise(c: EventoCriseDto): void {
    const v = this.desfechoForm.getRawValue();
    if (!this.validar(v.desfecho ? {} : { desfechoCrise: 'Escolha o desfecho.' })) return;
    this.enviarCrise(this.service.encerrarCrise(c.uuid, v.desfecho), 'Crise encerrada');
  }

  protected registrarNotificacao(c: EventoCriseDto): void {
    this.enviarCrise(this.service.notificarCrise(c.uuid, this.notificacaoForm.getRawValue().numero.trim()), 'Notificação registrada');
  }

  private enviarCrise(req: Observable<EventoCriseDto>, titulo: string): void {
    this.submitting.set(true);
    this.erroApi.set(null);
    req.subscribe({
      next: (c) => {
        this.submitting.set(false);
        this.criseAberta.set(c);
        this.mostrarToast(titulo, c.pacienteNome);
        this.carregarCrises();
        if (c.acompanhamentoId && this.selecionadoId() === c.acompanhamentoId) this.recarregarDetalhe();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected transferenciaPendente(caso: AcompanhamentoSaudeMentalDto): TransferenciaCuidadoDto | undefined {
    return caso.transferencias.find((t) => t.situacao === 'PROPOSTA');
  }

  protected ptsVigente(caso: AcompanhamentoSaudeMentalDto): ProjetoTerapeuticoDto | undefined {
    return caso.pts.find((p) => p.vigente);
  }

  protected redeVigente(caso: AcompanhamentoSaudeMentalDto): PessoaRedeApoioDto[] {
    return caso.redeApoio.filter((p) => !p.fim);
  }

  protected redeAnterior(caso: AcompanhamentoSaudeMentalDto): PessoaRedeApoioDto[] {
    return caso.redeApoio.filter((p) => !!p.fim);
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    if (g.tipo === 'acolher') this.salvarAcolhimento();
    else if (g.tipo === 'avaliar') this.salvarAvaliacao(g.caso);
    else if (g.tipo === 'referencia') this.salvarReferencia(g.caso);
    else if (g.tipo === 'pts') this.salvarPts(g.caso, g.base);
    else if (g.tipo === 'intervencao') this.salvarIntervencao(g.caso);
    else if (g.tipo === 'rede') this.salvarRede(g.caso);
    else if (g.tipo === 'sairRede') this.salvarSairRede(g.caso, g.pessoa);
    else if (g.tipo === 'intersetorial') this.salvarIntersetorial(g.caso);
    else if (g.tipo === 'retornoIntersetorial') this.salvarRetornoIntersetorial(g.caso, g.enc, g.modo);
    else if (g.tipo === 'encaminhar') this.salvarEncaminhamento(g.caso);
    else if (g.tipo === 'compartilhar') this.salvarCompartilhamento(g.caso);
    else if (g.tipo === 'fimCompartilhamento') this.salvarFimCompartilhamento(g.caso, g.compartilhamento);
    else if (g.tipo === 'transferir') this.salvarTransferencia(g.caso);
    else if (g.tipo === 'aceitar') this.salvarAceite(g.transferencia);
    else if (g.tipo === 'recusar') this.salvarDecisao(g.transferencia, 'recusar');
    else if (g.tipo === 'cancelarTransferencia') this.salvarDecisao(g.transferencia, 'cancelar', g.caso);
    else if (g.tipo === 'risco') this.salvarRisco(g.caso);
    else if (g.tipo === 'novaCrise') this.salvarCrise();
    else if (g.tipo === 'encerrar') this.salvarEncerramento(g.caso);
  }

  private validar(erros: Record<string, string>): boolean {
    this.erros.set(erros);
    const campos = Object.keys(erros);
    if (campos.length) {
      setTimeout(() => document.getElementById('f-' + campos[0])?.focus());
      return false;
    }
    return true;
  }

  private hojeIso(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  private salvarAcolhimento(): void {
    const v = this.acolherForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['buscaPaciente'] = 'Escolha o paciente na lista.';
    if (!v.unidadeId) erros['unidadeAcolhimento'] = 'Escolha a unidade.';
    if (!v.motivoProcura.trim()) erros['motivoProcura'] = 'Descreva o motivo da procura.';
    if (!v.riscoInicial) erros['riscoInicial'] = 'Informe o risco percebido.';
    if (!v.desfecho) erros['desfecho'] = 'Escolha o desfecho.';
    if (v.desfecho === 'ENCAMINHAMENTO' && !v.encaminhamentoDestino.trim()) erros['encaminhamentoDestino'] = 'Informe para onde.';
    if (this.acolhimentoAcompanha() && !v.equipeId) erros['equipeReferencia'] = 'Escolha a equipe de referência.';
    if (this.acolhimentoAcompanha() && !v.tecnicoMatricula) erros['tecnicoReferencia'] = 'Escolha o técnico de referência.';
    if (!this.validar(erros)) return;
    this.submitting.set(true);
    this.service.acolher({
      pacienteId: v.pacienteId, unidadeId: v.unidadeId, motivoProcura: v.motivoProcura.trim(),
      necessidadeImediata: v.necessidadeImediata.trim() || null, vulnerabilidade: v.vulnerabilidade.trim() || null,
      riscoInicial: v.riscoInicial as RiscoInicialSaudeMental, desfecho: v.desfecho as DesfechoAcolhimentoSaudeMental,
      encaminhamentoDestino: v.encaminhamentoDestino.trim() || null,
      equipeId: this.acolhimentoAcompanha() ? v.equipeId || null : null,
      tecnicoMatricula: this.acolhimentoAcompanha() ? v.tecnicoMatricula || null : null,
    }).subscribe({
      next: (a) => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast('Acolhimento registrado', a.aviso ?? (a.acompanhamentoId ? 'O caso foi aberto.' : DESFECHOS[a.desfecho!]));
        if (a.acompanhamentoId) this.selecionadoId.set(a.acompanhamentoId);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private salvarAvaliacao(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.avaliacaoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.contextoHistoria.trim()) erros['contextoHistoria'] = 'Descreva o contexto e a história.';
    if (!v.necessidades.trim()) erros['necessidades'] = 'Descreva as necessidades identificadas.';
    if (v.cid.trim() && !/^[A-Za-z][0-9]{2}(\.?[0-9A-Za-z]{1,2})?$/.test(v.cid.trim())) erros['cid'] = 'CID-10 inválido (ex.: F32.1).';
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.avaliar(caso.uuid, {
      contextoHistoria: v.contextoHistoria.trim(), aspectosPsicossociais: v.aspectosPsicossociais.trim() || null,
      fatoresRisco: v.fatoresRisco.trim() || null, fatoresProtecao: v.fatoresProtecao.trim() || null,
      necessidades: v.necessidades.trim(), cid: v.cid.trim() || null,
    }), 'Avaliação registrada', caso, 'avaliacoes');
  }

  private salvarReferencia(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.referenciaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.equipeId) erros['equipeTroca'] = 'Escolha a equipe de referência.';
    if (!v.tecnicoMatricula) erros['tecnicoTroca'] = 'Escolha o técnico de referência.';
    if (!v.motivo.trim()) erros['motivoTroca'] = 'Informe o motivo da troca.';
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.trocarReferencia(caso.uuid, { equipeId: v.equipeId, tecnicoMatricula: v.tecnicoMatricula, motivo: v.motivo.trim() }),
      'Referência trocada', caso, 'referencia');
  }

  private salvarEncerramento(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.encerrarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.motivoSaida) erros['motivoSaidaCaso'] = 'Escolha o motivo.';
    if (!v.observacao.trim()) erros['observacaoSaidaCaso'] = 'Descreva a saída.';
    if (v.fim && v.fim > this.hojeIso()) erros['fimCaso'] = 'A saída não pode ser no futuro.';
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.encerrar(caso.uuid, v.motivoSaida as MotivoSaidaAcompanhamento, v.observacao.trim(), v.fim),
      'Caso encerrado', caso, 'resumo');
  }

  private salvarPts(caso: AcompanhamentoSaudeMentalDto, base: ProjetoTerapeuticoDto | null): void {
    const v = this.ptsForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.necessidades.trim()) erros['necessidadesPts'] = 'Descreva as necessidades.';
    if (base && !v.sinteseRevisao.trim()) erros['sinteseRevisao'] = 'Escreva a síntese do que mudou nesta revisão.';
    if (v.revisarEm && v.revisarEm < this.hojeIso()) erros['revisarEm'] = 'A revisão não pode ser no passado.';
    const objetivos = v.objetivos as { descricao: string; meta: string; prazo: string; responsavelMatricula: string; situacao: SituacaoObjetivoTerapeutico }[];
    if (!objetivos.length) erros['objetivoDescricao-0'] = 'Inclua pelo menos um objetivo.';
    objetivos.forEach((o, i) => {
      if (!o.descricao.trim()) erros['objetivoDescricao-' + i] = 'Descreva o objetivo.';
    });
    const previstas = v.previstas as { tipoId: string; frequencia: string; responsavelMatricula: string; servico: string }[];
    previstas.forEach((p, i) => {
      if (!p.tipoId) erros['previstaTipo-' + i] = 'Escolha o tipo.';
    });
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.salvarPts(caso.uuid, {
      versaoBase: base?.versao ?? null,
      necessidades: v.necessidades.trim(),
      periodicidadeRevisao: v.periodicidadeRevisao,
      revisarEm: v.revisarEm || null,
      pactuadoComPessoa: v.pactuadoComPessoa,
      pactuacaoObservacao: v.pactuacaoObservacao.trim() || null,
      sinteseRevisao: base ? v.sinteseRevisao.trim() : null,
      objetivos: objetivos.map((o) => ({ descricao: o.descricao.trim(), meta: o.meta.trim() || null, prazo: o.prazo || null,
        responsavelMatricula: o.responsavelMatricula || null, situacao: o.situacao })),
      intervencoesPrevistas: previstas.map((p) => ({ tipoId: p.tipoId, frequencia: p.frequencia.trim() || null,
        responsavelMatricula: p.responsavelMatricula || null, servico: p.servico.trim() || null })),
    }), base ? 'PTS revisado' : 'PTS elaborado', caso, 'pts');
  }

  private salvarIntervencao(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.intervencaoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.tipoId) erros['tipoIntervencao'] = 'Escolha o tipo.';
    if (!v.resumo.trim()) erros['resumoIntervencao'] = 'Escreva o resumo.';
    if (v.realizadaEm && new Date(v.realizadaEm).getTime() > Date.now()) erros['realizadaEm'] = 'A intervenção não pode ser no futuro.';
    if (this.tipoEhMatriciamento() && !v.equipeMatriciadaId) erros['equipeMatriciada'] = 'Escolha a equipe com quem o caso foi discutido.';
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.registrarIntervencao(caso.uuid, {
      tipoId: v.tipoId, situacao: v.situacao, resumo: v.resumo.trim(),
      realizadaEm: v.realizadaEm ? new Date(v.realizadaEm).toISOString() : null,
      compartilhadoComMatricula: v.compartilhadoComMatricula.trim() || null, previstaId: v.previstaId || null,
      equipeMatriciadaId: this.tipoEhMatriciamento() ? v.equipeMatriciadaId || null : null,
    }), v.situacao === 'REALIZADA' ? 'Intervenção registrada' : 'Falta registrada', caso, 'intervencoes');
  }

  private salvarRede(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.redeForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nomeRede'] = 'Informe o nome.';
    if (!v.papel) erros['papelRede'] = 'Escolha o papel.';
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.incluirNaRede(caso.uuid, {
      nome: v.nome.trim(), papel: v.papel as PapelRedeApoio, vinculo: v.vinculo.trim() || null,
      telefone: v.telefone.trim() || null, observacao: v.observacao.trim() || null,
    }), 'Incluído na rede de apoio', caso, 'rede');
  }

  private salvarSairRede(caso: AcompanhamentoSaudeMentalDto, pessoa: PessoaRedeApoioDto): void {
    const v = this.sairRedeForm.getRawValue();
    if (!this.validar(v.motivo.trim() ? {} : { motivoSaidaRede: 'Informe o motivo.' })) return;
    this.enviarCaso(this.service.encerrarNaRede(caso.uuid, pessoa.uuid, v.motivo.trim()), 'Saiu da rede de apoio', caso, 'rede');
  }

  private salvarIntersetorial(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.intersetorialForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.instituicaoId) erros['instituicaoIntersetorial'] = 'Escolha a instituição.';
    if (!v.motivo.trim()) erros['motivoIntersetorial'] = 'Informe o motivo do encaminhamento.';
    if (v.enviadoEm && v.enviadoEm > this.hojeIso()) erros['enviadoIntersetorial'] = 'O envio não pode ser no futuro.';
    if (!v.retornoEsperadoEm) erros['prazoIntersetorial'] = 'Informe até quando se espera o retorno.';
    else if (v.retornoEsperadoEm < (v.enviadoEm || this.hojeIso())) erros['prazoIntersetorial'] = 'O retorno esperado não pode ser antes do envio.';
    if (!this.validar(erros)) return;
    this.enviarCaso(this.intersetorialService.encaminhar(caso.uuid, {
      instituicaoId: v.instituicaoId, motivo: v.motivo.trim(), enviadoEm: v.enviadoEm || null, retornoEsperadoEm: v.retornoEsperadoEm,
    }), 'Encaminhado a outro setor', caso, 'rede');
  }

  private salvarRetornoIntersetorial(caso: AcompanhamentoSaudeMentalDto, enc: EncaminhamentoIntersetorialDto, modo: 'recebimento' | 'retorno' | 'cancelamento'): void {
    const v = this.retornoIntersetorialForm.getRawValue();
    const erros: Record<string, string> = {};
    if (modo !== 'cancelamento' && v.data && (v.data < enc.enviadoEm || v.data > this.hojeIso())) erros['dataIntersetorial'] = 'A data vai do envio até hoje.';
    if (modo === 'retorno' && !v.desfecho) erros['desfechoIntersetorial'] = 'Escolha o desfecho.';
    if (modo !== 'recebimento' && !v.texto.trim()) {
      erros['textoIntersetorial'] = modo === 'retorno' ? 'Descreva o retorno da instituição.' : 'Informe o motivo.';
    }
    if (!this.validar(erros)) return;
    const req = modo === 'recebimento' ? this.intersetorialService.receber(caso.uuid, enc.uuid, v.data || null)
      : modo === 'retorno' ? this.intersetorialService.retorno(caso.uuid, enc.uuid, { data: v.data || null, desfecho: v.desfecho as 'CONCLUIDO' | 'RECUSADO', retorno: v.texto.trim() })
        : this.intersetorialService.cancelar(caso.uuid, enc.uuid, v.texto.trim());
    this.enviarCaso(req, modo === 'recebimento' ? 'Recebimento registrado' : modo === 'retorno' ? 'Retorno registrado' : 'Encaminhamento cancelado', caso, 'rede');
  }

  private salvarRisco(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.riscoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.tipo) erros['tipoRisco'] = 'Escolha o tipo de risco.';
    if (!v.nivel) erros['nivelRisco'] = 'Escolha o nível.';
    if ((v.nivel === 'MODERADO' || v.nivel === 'ALTO') && !v.medidas.trim()) erros['medidasRisco'] = 'Descreva as medidas adotadas.';
    if (v.nivel === 'ALTO' && !v.reavaliarEm) erros['reavaliarEm'] = 'Com risco alto, informe quando reavaliar.';
    if (v.reavaliarEm && v.reavaliarEm < this.hojeIso()) erros['reavaliarEm'] = 'A reavaliação não pode ser no passado.';
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.avaliarRisco(caso.uuid, {
      tipo: v.tipo, nivel: v.nivel, fatores: v.fatores.trim() || null, medidas: v.medidas.trim() || null, planoSeguranca: v.planoSeguranca,
      frequenciaContato: v.frequenciaContato.trim() || null, reavaliarEm: v.reavaliarEm || null,
    }), 'Risco avaliado', caso, 'risco');
  }

  private salvarCrise(): void {
    const v = this.criseForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['buscaPacienteCrise'] = 'Escolha o paciente na lista.';
    if (!v.unidadeId) erros['unidadeCrise'] = 'Escolha a unidade.';
    if (!v.tipo) erros['tipoCrise'] = 'Escolha o tipo.';
    if (!v.descricao.trim()) erros['descricaoCrise'] = 'Descreva a crise.';
    if (!this.validar(erros)) return;
    this.submitting.set(true);
    this.service.registrarCrise({ pacienteId: v.pacienteId, unidadeId: v.unidadeId, tipo: v.tipo, descricao: v.descricao.trim(),
      localOcorrencia: v.localOcorrencia.trim() || null }).subscribe({
      next: (c) => {
        this.submitting.set(false);
        this.mostrarToast('Crise registrada', c.notificacaoSugerida ? `${c.pacienteNome} · notificação compulsória sugerida` : c.pacienteNome);
        this.carregarCrises();
        this.abrirCrise(c.uuid);
        if (c.acompanhamentoId && this.selecionadoId() === c.acompanhamentoId) this.recarregarDetalhe();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private salvarEncaminhamento(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.encaminharForm.getRawValue();
    const erros: Record<string, string> = {};
    if (v.modo === 'VINCULAR') {
      if (!v.solicitacaoId.trim()) erros['solicitacaoId'] = 'Informe o número da solicitação.';
    } else {
      if (!v.procedimentoId) erros['procedimentoEncaminhamento'] = 'Escolha o procedimento.';
      if (!/^[A-Za-z][0-9]{2}(\.?[0-9A-Za-z]{1,2})?$/.test(v.cid.trim())) erros['cidEncaminhamento'] = 'Informe o CID-10 (ex.: F32.1).';
      if (v.justificativa.trim().length < 10) erros['justificativaEncaminhamento'] = 'Escreva a justificativa clínica (10 caracteres ou mais).';
      if (!v.prioridade) erros['prioridadeEncaminhamento'] = 'Escolha a prioridade.';
    }
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.encaminhar(caso.uuid, v.modo === 'VINCULAR'
      ? { solicitacaoId: v.solicitacaoId.trim() }
      : { procedimentoId: v.procedimentoId, cid: v.cid.trim(), justificativa: v.justificativa.trim(), prioridade: v.prioridade,
        profissionalMatricula: v.profissionalMatricula.trim() || null }), 'Encaminhamento registrado', caso, 'atencao');
  }

  private salvarCompartilhamento(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.compartilharForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.equipeId) erros['equipeParceira'] = 'Escolha a equipe parceira.';
    if (!v.objetivo.trim()) erros['objetivoCompartilhamento'] = 'Descreva o objetivo.';
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.compartilhar(caso.uuid, v.equipeId, v.objetivo.trim()), 'Cuidado compartilhado', caso, 'atencao');
  }

  private salvarFimCompartilhamento(caso: AcompanhamentoSaudeMentalDto, c: CompartilhamentoCuidadoDto): void {
    const v = this.motivoRedeForm.getRawValue();
    if (!this.validar(v.motivo.trim() ? {} : { motivoRede: 'Informe o motivo.' })) return;
    this.enviarCaso(this.service.encerrarCompartilhamento(caso.uuid, c.uuid, v.motivo.trim()), 'Compartilhamento encerrado', caso, 'atencao');
  }

  private salvarTransferencia(caso: AcompanhamentoSaudeMentalDto): void {
    const v = this.transferirForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.unidadeDestinoId) erros['unidadeDestino'] = 'Escolha a unidade de destino.';
    if (!v.resumo.trim()) erros['resumoTransferencia'] = 'Escreva o resumo de contrarreferência.';
    if (!this.validar(erros)) return;
    this.enviarCaso(this.service.proporTransferencia(caso.uuid, v.unidadeDestinoId, v.resumo.trim()), 'Transferência proposta', caso, 'atencao');
    this.carregarTransferencias();
  }

  private salvarAceite(t: TransferenciaCuidadoDto): void {
    const v = this.aceiteForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.equipeId) erros['equipeAceite'] = 'Escolha a equipe de referência.';
    if (!v.tecnicoMatricula) erros['tecnicoAceite'] = 'Escolha o técnico de referência.';
    if (!this.validar(erros)) return;
    this.submitting.set(true);
    this.service.aceitarTransferencia(t.uuid, v.equipeId, v.tecnicoMatricula).subscribe({
      next: (d) => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast('Transferência aceita', `${t.pacienteNome} · caso aberto na ${t.unidadeDestinoNome}`);
        this.selecionadoId.set(d.uuid);
        this.carregarTransferencias();
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private salvarDecisao(t: TransferenciaCuidadoDto, acao: 'recusar' | 'cancelar', caso?: AcompanhamentoSaudeMentalDto): void {
    const v = this.motivoRedeForm.getRawValue();
    if (!this.validar(v.motivo.trim() ? {} : { motivoRede: 'Informe o motivo.' })) return;
    this.submitting.set(true);
    (acao === 'recusar' ? this.service.recusarTransferencia(t.uuid, v.motivo.trim()) : this.service.cancelarTransferencia(t.uuid, v.motivo.trim())).subscribe({
      next: () => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(acao === 'recusar' ? 'Transferência recusada' : 'Transferência cancelada', t.pacienteNome);
        this.carregarTransferencias();
        if (caso) this.recarregarDetalhe();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private enviarCaso(req: Observable<AcompanhamentoSaudeMentalDto>, titulo: string, caso: AcompanhamentoSaudeMentalDto, aba: Aba): void {
    this.submitting.set(true);
    req.subscribe({
      next: (d) => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(titulo, caso.pacienteNome);
        this.detalhe.set(d);
        this.aba.set(aba);
        this.service.casos(this.filtroCasos()).subscribe({ next: (r) => this.casos.set(r), error: () => undefined });
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private falhou(e: HttpErrorResponse): void {
    this.submitting.set(false);
    this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}
