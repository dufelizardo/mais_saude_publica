import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AcessoDaInterface } from '../../../core/models/auth';
import { MODALIDADES_INTERNACAO, ModalidadeInternacao } from '../../../core/models/internacao';
import {
  CaraterInternacao,
  EVENTOS_LEITO,
  EVENTOS_SOLICITACAO_LEITO,
  FAIXAS_ETARIAS,
  FaixaEtariaLeito,
  PRIORIDADES_LEITO,
  PrioridadeLeito,
  STATUS_SOLICITACAO_LEITO,
  SolicitacaoLeitoDto,
  VagaLeitoDto,
  IndicadoresLeitosDto,
  InternacaoResponseDto,
  LeitoMapaDto,
  SEXOS_LEITO,
  SITUACOES_LEITO,
  SexoLeito,
  StatusInternacao,
  TIPOS_ALTA,
  TIPOS_LEITO,
  TipoAlta,
  TipoLeito,
} from '../../../core/models/internacao';
import { LimiteLeitosDto } from '../../../core/models/modelo-operacional';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { SetorResponseDto } from '../../../core/models/setor';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { InternacaoService } from '../../../core/services/internacao';
import { PacienteService } from '../../../core/services/paciente';
import { SetorService } from '../../../core/services/setor';
import { ModeloOperacionalService } from '../../../core/services/modelo-operacional';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';

type Aba = 'mapa' | 'internacoes' | 'central' | 'receber' | 'leitos';

type Gaveta =
  | { tipo: 'internar'; leito: LeitoMapaDto | null }
  | { tipo: 'internacao'; internacaoId: string }
  | { tipo: 'troca'; internacao: InternacaoResponseDto }
  | { tipo: 'alta'; internacao: InternacaoResponseDto }
  | { tipo: 'bloquear'; leito: LeitoMapaDto }
  | { tipo: 'leito'; leito: LeitoMapaDto | null }
  | { tipo: 'pedirVaga'; internacao: InternacaoResponseDto | null }
  | { tipo: 'pedidoLeito'; id: string }
  | { tipo: 'admitirRegulado'; pedido: SolicitacaoLeitoDto }
  | { tipo: 'recusarRegulado'; pedido: SolicitacaoLeitoDto };

interface GrupoSetor {
  setorId: string;
  setorNome: string;
  unidadeNome: string;
  leitos: LeitoMapaDto[];
  ocupados: number;
}

const CID = /^\s*[A-Za-z][0-9]{2}(\.?[0-9A-Za-z]{1,2})?\s*$/;

/**
 * Leitos (ADR-0099): mapa por setor com a ação de cada situação, internações, cadastro de leitos e as gavetas de
 * internação, detalhe, troca de leito, alta, bloqueio e leito. O backend é a ADR-0098.
 */
@Component({
  selector: 'app-leitos',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './leitos.html',
})
export class Leitos {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly internacaoService = inject(InternacaoService);
  private readonly pacienteService = inject(PacienteService);
  private readonly setorService = inject(SetorService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly modeloOperacionalService = inject(ModeloOperacionalService);

  protected readonly formatCpf = formatCpf;
  protected readonly tiposLeito = TIPOS_LEITO;
  protected readonly listaTipos = Object.keys(TIPOS_LEITO) as TipoLeito[];
  protected readonly sexos = SEXOS_LEITO;
  protected readonly listaSexos = Object.keys(SEXOS_LEITO) as SexoLeito[];
  protected readonly situacoes = SITUACOES_LEITO;
  protected readonly tiposAlta = TIPOS_ALTA;
  protected readonly listaTiposAlta = Object.keys(TIPOS_ALTA) as TipoAlta[];
  protected readonly eventos = EVENTOS_LEITO;
  protected readonly faixas = FAIXAS_ETARIAS;
  protected readonly listaFaixas = Object.keys(FAIXAS_ETARIAS) as FaixaEtariaLeito[];
  protected readonly prioridades = PRIORIDADES_LEITO;
  protected readonly listaPrioridades = Object.keys(PRIORIDADES_LEITO) as PrioridadeLeito[];
  protected readonly statusPedido = STATUS_SOLICITACAO_LEITO;
  protected readonly eventosPedido = EVENTOS_SOLICITACAO_LEITO;

  /** Central de regulação de leitos (ADR-0184). */
  protected readonly fila = signal<SolicitacaoLeitoDto[]>([]);
  protected readonly aReceber = signal<SolicitacaoLeitoDto[]>([]);
  protected readonly pedido = signal<SolicitacaoLeitoDto | null>(null);
  protected readonly vagas = signal<VagaLeitoDto[]>([]);

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  private readonly matriculaPadrao = signal('');

  protected readonly leitos = signal<LeitoMapaDto[]>([]);
  protected readonly indicadores = signal<IndicadoresLeitosDto | null>(null);
  protected readonly internacoes = signal<InternacaoResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly aba = signal<Aba>('mapa');
  protected readonly unidadeFiltro = signal('');
  protected readonly statusFiltro = signal<StatusInternacao | ''>('INTERNADO');
  protected readonly busca = signal('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly detalhe = signal<InternacaoResponseDto | null>(null);
  protected readonly erroDetalhe = signal<string | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly internarForm = this.fb.nonNullable.group({
    buscaPaciente: [''],
    pacienteId: [''],
    atendimentoId: [''],
    unidadeId: [''],
    leitoId: [''],
    medicoMatricula: [''],
    cid: [''],
    motivo: [''],
    carater: ['URGENCIA' as CaraterInternacao],
    previsaoAlta: [''],
    justificativaFaixaEtaria: [''],
  });
  protected readonly vagaForm = this.fb.nonNullable.group({
    buscaPaciente: [''], pacienteId: [''], unidadeOrigemId: [''], atendimentoId: [''], internacaoOrigemId: [''], profissionalMatricula: [''],
    cid: [''], quadroClinico: [''], tipoLeito: ['CLINICO' as TipoLeito], prioridade: ['AMARELO' as PrioridadeLeito],
  });
  protected readonly reservaForm = this.fb.nonNullable.group({
    modo: ['leito' as 'leito' | 'vagaZero' | 'negar'], leitoId: [''], unidadeDestinoId: [''], justificativa: [''], horas: ['6'],
    transporte: [''], motivo: [''], profissionalMatricula: [''],
  });
  protected readonly admissaoReguladaForm = this.fb.nonNullable.group({ medicoMatricula: [''], leitoId: [''], previsaoAlta: [''], justificativaFaixaEtaria: [''] });
  protected readonly recusaForm = this.fb.nonNullable.group({ motivo: [''], profissionalMatricula: [''] });
  protected readonly trocaForm = this.fb.nonNullable.group({ leitoId: [''], motivo: [''], profissionalMatricula: [''] });
  protected readonly altaForm = this.fb.nonNullable.group({ tipoAlta: ['MELHORADO' as TipoAlta], sumario: [''], medicoMatricula: [''], altaEm: [''] });
  protected readonly bloqueioForm = this.fb.nonNullable.group({ motivo: [''] });
  /** Internação psiquiátrica (ADR-0123): modalidade e comunicação ao Ministério Público, no detalhe. */
  protected readonly modalidades = MODALIDADES_INTERNACAO;
  protected readonly listaModalidades = Object.keys(MODALIDADES_INTERNACAO) as ModalidadeInternacao[];
  protected readonly modalidadeForm = this.fb.nonNullable.group({ modalidade: ['' as ModalidadeInternacao | ''], ordemJudicial: [''] });
  protected readonly mpForm = this.fb.nonNullable.group({ protocolo: [''], observacao: [''] });
  protected readonly erroPsiq = signal<string | null>(null);
  protected readonly salvandoPsiq = signal(false);
  protected readonly pendentesMp = signal<InternacaoResponseDto[]>([]);
  protected readonly leitoForm = this.fb.nonNullable.group({
    unidadeId: [''],
    setorId: [''],
    identificacao: [''],
    tipo: ['CLINICO' as TipoLeito],
    sexo: ['MISTO' as SexoLeito],
    faixaEtaria: ['ADULTO' as FaixaEtariaLeito],
    ativo: [true],
  });

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────

  protected readonly abas = computed<{ id: Aba; rotulo: string; contagem: number | null }[]>(() => [
    { id: 'mapa', rotulo: 'Mapa de leitos', contagem: this.leitosDoMapa().length },
    { id: 'internacoes', rotulo: 'Internações', contagem: this.internacoes().filter((i) => i.status === 'INTERNADO').length },
    ...(this.pode('LEITO.REGULAR') ? [{ id: 'central' as Aba, rotulo: 'Central de leitos', contagem: this.fila().filter((s) => s.status === 'SOLICITADA').length }] : []),
    ...(this.pode('INTERNACAO.GERENCIAR') ? [{ id: 'receber' as Aba, rotulo: 'A receber', contagem: this.aReceber().length }] : []),
    ...(this.pode('LEITO.GERENCIAR') ? [{ id: 'leitos' as Aba, rotulo: 'Cadastro de leitos', contagem: this.leitos().length }] : []),
  ]);

  /** Unidades que têm leito, para o filtro do mapa. */
  protected readonly unidadesComLeito = computed(() => {
    const ids = new Set(this.leitos().map((l) => l.unidadeId));
    return this.unidades().filter((u) => ids.has(u.uuid));
  });

  protected readonly leitosDoMapa = computed(() =>
    this.leitos().filter((l) => l.ativo && (!this.unidadeFiltro() || l.unidadeId === this.unidadeFiltro())),
  );

  protected readonly grupos = computed<GrupoSetor[]>(() => {
    const mapa = new Map<string, GrupoSetor>();
    for (const l of this.leitosDoMapa()) {
      const g = mapa.get(l.setorId) ?? { setorId: l.setorId, setorNome: l.setorNome, unidadeNome: l.unidadeNome, leitos: [], ocupados: 0 };
      g.leitos.push(l);
      if (l.situacao === 'OCUPADO') g.ocupados++;
      mapa.set(l.setorId, g);
    }
    return [...mapa.values()].sort((a, b) => `${a.unidadeNome} ${a.setorNome}`.localeCompare(`${b.unidadeNome} ${b.setorNome}`, 'pt-BR'));
  });

  protected readonly internacoesFiltradas = computed(() => {
    const q = this.busca().trim().toLowerCase();
    return this.internacoes()
      .filter((i) => !this.statusFiltro() || i.status === this.statusFiltro())
      .filter((i) => !this.unidadeFiltro() || i.unidadeId === this.unidadeFiltro())
      .filter((i) => !q || `${i.pacienteNome} ${i.leitoIdentificacao} ${i.setorNome} ${i.cid}`.toLowerCase().includes(q));
  });

  constructor() {
    this.authService.usuarioAtual().subscribe((u) => this.matriculaPadrao.set(u?.profissionalMatricula ?? ''));
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      const pedida = this.route.snapshot.queryParamMap.get('aba') as Aba | null;
      if (pedida && this.abas().some((x) => x.id === pedida)) this.aba.set(pedida);
    });
    this.carregarTudo();
    this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    this.unidadeSaudeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.setorService.listar().pipe(catchError(() => of([]))).subscribe((s) => this.setores.set(s));
    // "Internar" no atendimento abre a internação com paciente, unidade e atendimento (ADR-0099).
    const q = this.route.snapshot.queryParamMap;
    if (q.get('acao') === 'vaga') {
      this.abrirPedirVaga(null, q.get('pacienteId') ?? '', q.get('unidadeId') ?? '', q.get('atendimentoId') ?? '');
    }
    if (q.get('acao') === 'internar') {
      this.unidadeFiltro.set(q.get('unidadeId') ?? '');
      this.abrirInternar(null, q.get('pacienteId') ?? '', q.get('unidadeId') ?? '', q.get('atendimentoId') ?? '');
    }
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  private carregarTudo(): void {
    this.carregando.set(true);
    forkJoin({
      leitos: this.internacaoService.mapa(undefined, true).pipe(catchError(() => of([]))),
      internacoes: this.internacaoService.listar().pipe(catchError(() => of([]))),
      fila: this.pode('LEITO.REGULAR') ? this.internacaoService.filaDeLeitos().pipe(catchError(() => of([]))) : of([]),
      receber: this.pode('INTERNACAO.GERENCIAR') ? this.internacaoService.aReceber().pipe(catchError(() => of([]))) : of([]),
    }).subscribe(({ leitos, internacoes, fila, receber }) => {
      this.fila.set(fila);
      this.aReceber.set(receber);
      this.leitos.set(leitos);
      this.internacoes.set(internacoes);
      this.carregando.set(false);
      this.carregarIndicadores();
      this.carregarPendentesMp();
    });
  }

  protected carregarIndicadores(): void {
    this.internacaoService.indicadores(this.unidadeFiltro() || undefined).pipe(catchError(() => of(null))).subscribe((i) => this.indicadores.set(i));
  }

  protected filtrarUnidade(id: string): void {
    this.unidadeFiltro.set(id);
    this.carregarIndicadores();
    this.carregarPendentesMp();
  }

  protected selecionarAba(aba: Aba, focar = false): void {
    this.aba.set(aba);
    if (focar) document.getElementById('tab-' + aba)?.focus();
  }

  protected navegarAbas(event: KeyboardEvent, indice: number): void {
    if (event.key !== 'ArrowRight' && event.key !== 'ArrowLeft') return;
    event.preventDefault();
    const abas = this.abas();
    this.selecionarAba(abas[(indice + (event.key === 'ArrowRight' ? 1 : -1) + abas.length) % abas.length].id, true);
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────

  protected instante(iso: string | null | undefined): string {
    if (!iso) return '—';
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} · ${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  protected taxa(): string {
    const t = this.indicadores()?.taxaOcupacao;
    return t == null ? '—' : `${t.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`;
  }

  protected permanencia(): string {
    const m = this.indicadores()?.mediaPermanenciaDias;
    return m == null ? '—' : m.toLocaleString('pt-BR', { maximumFractionDigits: 1 });
  }

  protected pacientesFiltrados(): PacienteResponseDto[] {
    const q = this.internarForm.controls.buscaPaciente.value.trim().toLowerCase();
    const digitos = q.replace(/\D/g, '');
    const selecionado = this.internarForm.controls.pacienteId.value;
    const lista = this.pacientes()
      .filter((p) => p.ativo !== false)
      .filter((p) => !q || p.nome.toLowerCase().includes(q) || (digitos.length > 0 && (p.cpf ?? '').replace(/\D/g, '').includes(digitos)))
      .slice(0, 50);
    const atual = this.pacientes().find((p) => p.uuid === selecionado);
    return atual && !lista.includes(atual) ? [atual, ...lista] : lista;
  }

  /** Leitos livres da unidade escolhida, compatíveis com o sexo do paciente (enfermaria masculina, feminina ou mista). */
  protected leitosParaInternar(): LeitoMapaDto[] {
    const v = this.internarForm.getRawValue();
    const paciente = this.pacientes().find((p) => p.uuid === v.pacienteId);
    return this.leitos().filter(
      (l) => l.ativo && l.situacao === 'LIVRE' && (!v.unidadeId || l.unidadeId === v.unidadeId) && compativel(l.sexo, paciente?.sexo),
    );
  }

  protected leitosParaTroca(i: InternacaoResponseDto): LeitoMapaDto[] {
    const paciente = this.pacientes().find((p) => p.uuid === i.pacienteId);
    return this.leitos().filter(
      (l) => l.ativo && l.situacao === 'LIVRE' && l.unidadeId === i.unidadeId && l.uuid !== i.leitoId && compativel(l.sexo, paciente?.sexo),
    );
  }

  protected setoresAssistenciais(): SetorResponseDto[] {
    const unidade = this.leitoForm.controls.unidadeId.value;
    return this.setores().filter((s) => s.ativo && s.tipo === 'ASSISTENCIAL' && (!unidade || s.unidadeUuid === unidade));
  }

  protected unidadesComSetorAssistencial(): UnidadeSaudeResponseDto[] {
    const ids = new Set(this.setores().filter((s) => s.ativo && s.tipo === 'ASSISTENCIAL').map((s) => s.unidadeUuid));
    return this.unidades().filter((u) => ids.has(u.uuid));
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'internar':
        return g.leito ? `Internar no leito ${g.leito.identificacao}` : 'Internar paciente';
      case 'internacao':
        return 'Internação';
      case 'troca':
        return 'Trocar de leito';
      case 'alta':
        return 'Dar alta';
      case 'bloquear':
        return `Bloquear o leito ${g.leito.identificacao}`;
      case 'leito':
        return g.leito ? 'Editar leito' : 'Novo leito';
      case 'pedirVaga':
        return 'Pedir vaga de internação';
      case 'pedidoLeito':
        return 'Pedido de leito';
      case 'admitirRegulado':
        return 'Admitir paciente regulado';
      case 'recusarRegulado':
        return 'Recusar o paciente';
    }
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
    this.detalhe.set(null);
  }

  protected abrirInternar(leito: LeitoMapaDto | null, pacienteId = '', unidadeId = '', atendimentoId = ''): void {
    this.internarForm.reset({
      buscaPaciente: '',
      pacienteId,
      atendimentoId,
      unidadeId: leito?.unidadeId ?? unidadeId,
      leitoId: leito?.uuid ?? '',
      medicoMatricula: this.matriculaPadrao(),
      cid: '',
      motivo: '',
      carater: 'URGENCIA',
      previsaoAlta: '',
      justificativaFaixaEtaria: '',
    });
    this.abrir({ tipo: 'internar', leito });
  }

  protected abrirInternacao(internacaoId: string): void {
    this.detalhe.set(null);
    this.erroDetalhe.set(null);
    this.abrir({ tipo: 'internacao', internacaoId });
    this.internacaoService.buscar(internacaoId).subscribe({
      next: (d) => {
        this.detalhe.set(d);
        this.erroPsiq.set(null);
        this.modalidadeForm.reset({ modalidade: d.modalidade ?? '', ordemJudicial: d.ordemJudicial ?? '' });
        this.mpForm.reset({ protocolo: '', observacao: '' });
      },
      error: (e: HttpErrorResponse) =>
        this.erroDetalhe.set(e.status === 403 ? 'Esta internação é de uma unidade fora do seu acesso.' : 'Não foi possível abrir a internação.'),
    });
  }

  /** Involuntárias com comunicação ao Ministério Público pendente, no escopo (ADR-0123). */
  protected carregarPendentesMp(): void {
    this.internacaoService.listar({ unidadeId: this.unidadeFiltro() || undefined, comunicacaoMpPendente: true })
      .pipe(catchError(() => of([]))).subscribe((l) => this.pendentesMp.set(l));
  }

  protected salvarModalidade(d: InternacaoResponseDto): void {
    const v = this.modalidadeForm.getRawValue();
    if (!v.modalidade) { this.erroPsiq.set('Escolha a modalidade.'); return; }
    if (v.modalidade === 'COMPULSORIA' && !v.ordemJudicial.trim()) { this.erroPsiq.set('Na compulsória, informe o número da ordem judicial.'); return; }
    this.enviarPsiq(this.internacaoService.definirModalidade(d.uuid, v.modalidade, v.ordemJudicial.trim()), 'Modalidade registrada');
  }

  protected registrarComunicacaoMp(d: InternacaoResponseDto): void {
    const v = this.mpForm.getRawValue();
    if (!d.comunicacaoMpPendente) return;
    this.enviarPsiq(this.internacaoService.comunicarMp(d.uuid, { momento: d.comunicacaoMpPendente, protocolo: v.protocolo.trim() || null,
      observacao: v.observacao.trim() || null }), 'Comunicação ao Ministério Público registrada');
  }

  private enviarPsiq(req: Observable<InternacaoResponseDto>, titulo: string): void {
    this.salvandoPsiq.set(true);
    this.erroPsiq.set(null);
    req.subscribe({
      next: (d) => {
        this.salvandoPsiq.set(false);
        this.detalhe.set(d);
        this.mpForm.reset({ protocolo: '', observacao: '' });
        this.mostrarToast(titulo, d.pacienteNome);
        this.carregarPendentesMp();
      },
      error: (e: HttpErrorResponse) => {
        this.salvandoPsiq.set(false);
        this.erroPsiq.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar.');
      },
    });
  }

  protected abrirTroca(i: InternacaoResponseDto): void {
    this.trocaForm.reset({ leitoId: '', motivo: '', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'troca', internacao: i });
  }

  protected abrirAlta(i: InternacaoResponseDto): void {
    this.altaForm.reset({ tipoAlta: 'MELHORADO', sumario: '', medicoMatricula: this.matriculaPadrao(), altaEm: '' });
    this.abrir({ tipo: 'alta', internacao: i });
  }

  protected abrirBloqueio(leito: LeitoMapaDto): void {
    this.bloqueioForm.reset({ motivo: '' });
    this.abrir({ tipo: 'bloquear', leito });
  }

  /** O que ainda cabe no tipo escolhido, pelo limite da capacidade (ADR-0171). */
  protected readonly limitesDaUnidade = signal<LimiteLeitosDto[]>([]);

  protected carregarLimites(): void {
    const unidade = this.leitoForm.controls.unidadeId.value;
    if (!unidade) {
      this.limitesDaUnidade.set([]);
      return;
    }
    this.modeloOperacionalService.limitesDeLeitos(unidade).subscribe({ next: (l) => this.limitesDaUnidade.set(l), error: () => this.limitesDaUnidade.set([]) });
  }

  protected avisoDoLimite(): string {
    const l = this.limitesDaUnidade().find((x) => x.tiposDeLeito.includes(this.leitoForm.controls.tipo.value));
    if (!l || l.autorizados == null) return '';
    return `${l.capacidadeNome}: ${l.ativos} de ${l.autorizados} leito(s) autorizado(s).`;
  }

  protected abrirLeito(leito: LeitoMapaDto | null = null): void {
    this.leitoForm.reset({
      unidadeId: leito?.unidadeId ?? this.unidadeFiltro(),
      setorId: leito?.setorId ?? '',
      identificacao: leito?.identificacao ?? '',
      tipo: leito?.tipo ?? 'CLINICO',
      sexo: leito?.sexo ?? 'MISTO',
      faixaEtaria: leito?.faixaEtaria ?? 'ADULTO',
      ativo: leito?.ativo ?? true,
    });
    this.carregarLimites();
    this.abrir({ tipo: 'leito', leito });
  }

  // ── Ações diretas no leito ─────────────────────────────────────────────────────────────────

  protected liberar(l: LeitoMapaDto): void {
    this.enviar(this.internacaoService.liberar(l.uuid), 'Leito liberado', `${l.identificacao} · livre`);
  }

  protected desbloquear(l: LeitoMapaDto): void {
    this.enviar(this.internacaoService.desbloquear(l.uuid), 'Leito desbloqueado', `${l.identificacao} · livre`);
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    switch (g.tipo) {
      case 'internar':
        return this.salvarInternacao();
      case 'troca':
        return this.salvarTroca(g.internacao);
      case 'alta':
        return this.salvarAlta(g.internacao);
      case 'bloquear':
        return this.salvarBloqueio(g.leito);
      case 'leito':
        return this.salvarLeito(g.leito);
      case 'pedirVaga':
        return this.salvarPedidoDeVaga();
      case 'pedidoLeito':
        return this.salvarDecisao(g.id);
      case 'admitirRegulado':
        return this.salvarAdmissaoRegulada(g.pedido);
      case 'recusarRegulado':
        return this.salvarRecusa(g.pedido);
      default:
        return;
    }
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

  private salvarInternacao(): void {
    const v = this.internarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['pacienteId'] = 'Selecione o paciente.';
    if (!v.leitoId) erros['leitoId'] = 'Escolha um leito livre.';
    if (!v.medicoMatricula.trim()) erros['medicoMatricula'] = 'Informe a matrícula do médico responsável.';
    if (!CID.test(v.cid)) erros['cid'] = 'Informe o CID-10 principal (ex.: J18.9).';
    if (!v.motivo.trim()) erros['motivo'] = 'Informe o motivo da internação.';
    if (!this.validar(erros)) return;
    const paciente = this.pacientes().find((p) => p.uuid === v.pacienteId);
    const leito = this.leitos().find((l) => l.uuid === v.leitoId);
    this.enviar(
      this.internacaoService.internar({
        pacienteId: v.pacienteId,
        atendimentoId: v.atendimentoId || undefined,
        leitoId: v.leitoId,
        medicoMatricula: v.medicoMatricula.trim(),
        cid: v.cid.trim(),
        motivo: v.motivo.trim(),
        carater: v.carater,
        previsaoAlta: v.previsaoAlta || undefined,
        justificativaFaixaEtaria: v.justificativaFaixaEtaria.trim() || undefined,
      }),
      'Paciente internado',
      `${paciente?.nome ?? 'Paciente'} · ${leito?.identificacao ?? ''}`,
    );
  }

  private salvarTroca(i: InternacaoResponseDto): void {
    const v = this.trocaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.leitoId) erros['leitoDestino'] = 'Escolha o leito de destino.';
    if (!v.motivo.trim()) erros['motivoTroca'] = 'Informe o motivo da troca.';
    if (!v.profissionalMatricula.trim()) erros['matriculaTroca'] = 'Informe a matrícula de quem faz a troca.';
    if (!this.validar(erros)) return;
    const destino = this.leitos().find((l) => l.uuid === v.leitoId);
    this.enviar(
      this.internacaoService.trocarLeito(i.uuid, v.leitoId, v.profissionalMatricula.trim(), v.motivo.trim()),
      'Leito trocado',
      `${i.pacienteNome} · ${destino?.identificacao ?? ''}`,
    );
  }

  private salvarAlta(i: InternacaoResponseDto): void {
    const v = this.altaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (v.sumario.trim().length < 10) erros['sumario'] = 'Escreva o sumário de alta (pelo menos 10 caracteres).';
    if (!v.medicoMatricula.trim()) erros['matriculaAlta'] = 'Informe a matrícula do médico que dá a alta.';
    if (v.altaEm && new Date(v.altaEm) > new Date()) erros['altaEm'] = 'A alta não pode ser no futuro.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.internacaoService.darAlta(i.uuid, {
        tipoAlta: v.tipoAlta,
        sumario: v.sumario.trim(),
        medicoMatricula: v.medicoMatricula.trim(),
        altaEm: v.altaEm ? new Date(v.altaEm).toISOString() : undefined,
      }),
      'Alta registrada',
      `${i.pacienteNome} · ${this.tiposAlta[v.tipoAlta]}`,
    );
  }

  private salvarBloqueio(l: LeitoMapaDto): void {
    const motivo = this.bloqueioForm.controls.motivo.value.trim();
    if (!this.validar(motivo ? {} : { motivoBloqueio: 'Informe o motivo do bloqueio.' })) return;
    this.enviar(this.internacaoService.bloquear(l.uuid, motivo), 'Leito bloqueado', `${l.identificacao} · ${motivo}`);
  }

  private salvarLeito(leito: LeitoMapaDto | null): void {
    const v = this.leitoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.unidadeId) erros['unidadeLeito'] = 'Selecione a unidade.';
    if (!v.setorId) erros['setorLeito'] = 'Selecione o setor assistencial.';
    if (!v.identificacao.trim()) erros['identificacao'] = 'Informe a identificação do leito.';
    if (!this.validar(erros)) return;
    const dto = { unidadeId: v.unidadeId, setorId: v.setorId, identificacao: v.identificacao.trim(), tipo: v.tipo, sexo: v.sexo, faixaEtaria: v.faixaEtaria, ativo: v.ativo };
    this.enviar(
      leito ? this.internacaoService.atualizarLeito(leito.uuid, dto) : this.internacaoService.criarLeito(dto),
      leito ? 'Leito atualizado' : 'Leito cadastrado',
      dto.identificacao,
    );
  }

  // ── Central de regulação de leitos (ADR-0184) ──────────────────────────────────────────────

  protected abrirPedirVaga(internacao: InternacaoResponseDto | null, pacienteId = '', unidadeId = '', atendimentoId = ''): void {
    this.vagaForm.reset({
      buscaPaciente: '', pacienteId: internacao?.pacienteId ?? pacienteId, unidadeOrigemId: internacao?.unidadeId ?? unidadeId,
      atendimentoId, internacaoOrigemId: internacao?.uuid ?? '', profissionalMatricula: this.matriculaPadrao(), cid: internacao?.cid ?? '',
      quadroClinico: '', tipoLeito: 'CLINICO', prioridade: 'AMARELO',
    });
    this.abrir({ tipo: 'pedirVaga', internacao });
  }

  protected pacientesParaVaga(): PacienteResponseDto[] {
    const q = this.vagaForm.controls.buscaPaciente.value.trim().toLowerCase();
    const selecionado = this.vagaForm.controls.pacienteId.value;
    const lista = this.pacientes().filter((p) => p.ativo !== false && (!q || p.nome.toLowerCase().includes(q))).slice(0, 50);
    const atual = this.pacientes().find((p) => p.uuid === selecionado);
    return atual && !lista.includes(atual) ? [atual, ...lista] : lista;
  }

  protected abrirPedido(id: string): void {
    this.pedido.set(null);
    this.vagas.set([]);
    this.erroDetalhe.set(null);
    this.reservaForm.reset({ modo: 'leito', leitoId: '', unidadeDestinoId: '', justificativa: '', horas: '6', transporte: '', motivo: '',
      profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'pedidoLeito', id });
    this.internacaoService.pedidoDeLeito(id).subscribe({
      next: (p) => {
        this.pedido.set(p);
        if (p.status === 'SOLICITADA' && this.pode('LEITO.REGULAR')) {
          this.internacaoService.vagasCompativeis(id).pipe(catchError(() => of([]))).subscribe((v) => this.vagas.set(v));
        }
      },
      error: (e: HttpErrorResponse) => this.erroDetalhe.set(e.status === 403 ? 'Este pedido é de unidades fora do seu acesso.' : 'Não foi possível abrir o pedido.'),
    });
  }

  protected abrirAdmissaoRegulada(pedido: SolicitacaoLeitoDto): void {
    this.admissaoReguladaForm.reset({ medicoMatricula: this.matriculaPadrao(), leitoId: '', previsaoAlta: '', justificativaFaixaEtaria: '' });
    this.abrir({ tipo: 'admitirRegulado', pedido });
  }

  protected abrirRecusa(pedido: SolicitacaoLeitoDto): void {
    this.recusaForm.reset({ motivo: '', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'recusarRegulado', pedido });
  }

  /** Na vaga zero, os leitos livres da unidade de destino. */
  protected leitosLivresDe(unidadeId: string | null | undefined): LeitoMapaDto[] {
    return this.leitos().filter((l) => l.ativo && l.situacao === 'LIVRE' && l.unidadeId === unidadeId);
  }

  protected cancelarPedido(p: SolicitacaoLeitoDto): void {
    const motivo = 'Cancelado pela unidade de origem.';
    this.enviar(this.internacaoService.cancelarPedidoDeLeito(p.uuid, this.matriculaPadrao() || p.solicitadoPorMatricula, motivo),
      'Pedido de leito cancelado', p.pacienteNome);
  }

  private salvarPedidoDeVaga(): void {
    const v = this.vagaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['pacienteVaga'] = 'Selecione o paciente.';
    if (!v.unidadeOrigemId) erros['unidadeOrigem'] = 'Selecione a unidade de origem.';
    if (!CID.test(v.cid) || !v.cid.trim()) erros['cidVaga'] = 'Informe o CID-10 principal (ex.: J18.9).';
    if (!v.quadroClinico.trim()) erros['quadroClinico'] = 'Descreva o quadro clínico.';
    if (!v.profissionalMatricula.trim()) erros['matriculaVaga'] = 'Informe a matrícula de quem pede.';
    if (!this.validar(erros)) return;
    const paciente = this.pacientes().find((p) => p.uuid === v.pacienteId);
    this.enviar(
      this.internacaoService.pedirLeito({
        pacienteId: v.pacienteId, unidadeOrigemId: v.unidadeOrigemId, atendimentoId: v.atendimentoId || undefined,
        internacaoOrigemId: v.internacaoOrigemId || undefined, profissionalMatricula: v.profissionalMatricula.trim(), cid: v.cid.trim(),
        quadroClinico: v.quadroClinico.trim(), tipoLeito: v.tipoLeito, prioridade: v.prioridade,
      }),
      'Vaga pedida à central de leitos',
      `${paciente?.nome ?? 'Paciente'} · ${this.tiposLeito[v.tipoLeito]}`,
    );
  }

  private salvarDecisao(id: string): void {
    const v = this.reservaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.profissionalMatricula.trim()) erros['matriculaRegulador'] = 'Informe a matrícula do regulador.';
    if (v.modo === 'leito' && !v.leitoId) erros['vagaLeito'] = 'Escolha um dos leitos compatíveis.';
    if (v.modo === 'vagaZero' && !v.unidadeDestinoId) erros['unidadeVagaZero'] = 'Escolha a unidade que recebe.';
    if (v.modo === 'vagaZero' && !v.justificativa.trim()) erros['justificativaVagaZero'] = 'Justifique a vaga zero.';
    if (v.modo === 'negar' && !v.motivo.trim()) erros['motivoNegacao'] = 'Informe o motivo.';
    if (!this.validar(erros)) return;
    const p = this.pedido();
    const nome = p?.pacienteNome ?? 'Paciente';
    if (v.modo === 'negar') {
      this.enviar(this.internacaoService.negarPedidoDeLeito(id, v.profissionalMatricula.trim(), v.motivo.trim()), 'Pedido negado', nome);
      return;
    }
    const vaga = this.vagas().find((x) => x.leitoId === v.leitoId);
    this.enviar(
      this.internacaoService.reservarLeito(id, {
        profissionalMatricula: v.profissionalMatricula.trim(),
        leitoId: v.modo === 'leito' ? v.leitoId : undefined,
        unidadeDestinoId: v.modo === 'vagaZero' ? v.unidadeDestinoId : undefined,
        justificativaVagaZero: v.modo === 'vagaZero' ? v.justificativa.trim() : undefined,
        horas: v.modo === 'leito' ? Number(v.horas) || 6 : undefined,
        transporte: v.transporte.trim() || undefined,
      }),
      v.modo === 'leito' ? 'Leito reservado' : 'Vaga zero registrada',
      v.modo === 'leito' ? `${nome} · ${vaga?.identificacao ?? ''} em ${vaga?.unidadeNome ?? ''}` : nome,
    );
  }

  private salvarAdmissaoRegulada(p: SolicitacaoLeitoDto): void {
    const v = this.admissaoReguladaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.medicoMatricula.trim()) erros['medicoRegulado'] = 'Informe a matrícula do médico responsável.';
    if (p.vagaZero && !v.leitoId) erros['leitoRegulado'] = 'Escolha o leito livre onde o paciente fica.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.internacaoService.admitirRegulado(p.uuid, {
        medicoMatricula: v.medicoMatricula.trim(), leitoId: p.vagaZero ? v.leitoId : undefined, previsaoAlta: v.previsaoAlta || undefined,
        justificativaFaixaEtaria: v.justificativaFaixaEtaria.trim() || undefined,
      }),
      'Paciente admitido',
      `${p.pacienteNome} · vindo de ${p.unidadeOrigemNome}`,
    );
  }

  private salvarRecusa(p: SolicitacaoLeitoDto): void {
    const v = this.recusaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.motivo.trim()) erros['motivoRecusa'] = 'Informe o motivo da recusa.';
    if (!v.profissionalMatricula.trim()) erros['matriculaRecusa'] = 'Informe a matrícula.';
    if (!this.validar(erros)) return;
    this.enviar(this.internacaoService.recusarNoDestino(p.uuid, v.profissionalMatricula.trim(), v.motivo.trim()), 'Paciente recusado',
      `${p.pacienteNome} · volta para a fila da central`);
  }

  private enviar(req: Observable<unknown>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(titulo, detalhe);
        this.carregarTudo();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const mensagem = (error.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.';
        if (this.gaveta()) this.erroApi.set(mensagem);
        else this.mostrarToast('Não foi possível concluir', mensagem);
      },
    });
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}

/** Enfermaria mista recebe qualquer um; masculina ou feminina, só o mesmo sexo (sexo ignorado vai para mista). */
function compativel(leito: SexoLeito, paciente: string | null | undefined): boolean {
  return leito === 'MISTO' || !paciente || leito === paciente;
}
