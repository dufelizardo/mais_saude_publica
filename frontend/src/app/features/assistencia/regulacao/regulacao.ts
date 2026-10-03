import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AcessoDaInterface } from '../../../core/models/auth';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import {
  PrioridadeRegulacao,
  ProcedimentoReguladoResponseDto,
  SolicitacaoRegulacaoResponseDto,
  SolicitacaoRegulacaoResumoDto,
  StatusSolicitacaoRegulacao,
  TipoEventoRegulacao,
  TipoProcedimentoRegulado,
} from '../../../core/models/regulacao';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { PacienteService } from '../../../core/services/paciente';
import { RegulacaoService } from '../../../core/services/regulacao';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { AgendaService } from '../../../core/services/agenda';
import { ItemAgendaDto } from '../../../core/models/agenda';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';

type Aba = 'fila' | 'solicitacoes' | 'procedimentos';

type Gaveta =
  | { tipo: 'nova' }
  | { tipo: 'detalhe'; uuid: string }
  | { tipo: 'cancelar'; s: SolicitacaoRegulacaoResumoDto }
  | { tipo: 'agendar'; s: SolicitacaoRegulacaoResumoDto }
  | { tipo: 'realizar'; s: SolicitacaoRegulacaoResumoDto }
  | { tipo: 'falta'; s: SolicitacaoRegulacaoResumoDto }
  | { tipo: 'procedimento'; p: ProcedimentoReguladoResponseDto | null };

type Decisao = 'AUTORIZAR' | 'DEVOLVER' | 'NEGAR' | 'RECLASSIFICAR';

/** Rótulo e cor de cada prioridade; a classe reaproveita as cores da classificação de risco. */
export const PRIORIDADES: Record<PrioridadeRegulacao, { rotulo: string; descricao: string }> = {
  VERMELHO: { rotulo: 'Vermelho', descricao: 'emergência' },
  AMARELO: { rotulo: 'Amarelo', descricao: 'urgente' },
  VERDE: { rotulo: 'Verde', descricao: 'não urgente' },
  AZUL: { rotulo: 'Azul', descricao: 'eletivo' },
};

export const STATUS: Record<StatusSolicitacaoRegulacao, { classe: string; rotulo: string }> = {
  SOLICITADA: { classe: 'info', rotulo: 'Na fila' },
  DEVOLVIDA: { classe: 'warn', rotulo: 'Devolvida' },
  AUTORIZADA: { classe: 'ok', rotulo: 'Autorizada' },
  NEGADA: { classe: 'alert', rotulo: 'Negada' },
  AGENDADA: { classe: 'ok', rotulo: 'Agendada' },
  REALIZADA: { classe: 'purple', rotulo: 'Realizada' },
  FALTOU: { classe: 'alert', rotulo: 'Faltou' },
  CANCELADA: { classe: 'muted', rotulo: 'Cancelada' },
};

const TIPOS_PROCEDIMENTO: Record<TipoProcedimentoRegulado, string> = {
  CONSULTA_ESPECIALIZADA: 'Consulta especializada',
  EXAME: 'Exame',
  PROCEDIMENTO: 'Procedimento',
};

const EVENTOS: Record<TipoEventoRegulacao, string> = {
  SOLICITACAO: 'Solicitada',
  COMPLEMENTO: 'Complementada pelo solicitante',
  RECLASSIFICACAO: 'Prioridade reclassificada',
  AUTORIZACAO: 'Autorizada',
  DEVOLUCAO: 'Devolvida ao solicitante',
  NEGATIVA: 'Negada',
  CANCELAMENTO: 'Cancelada',
  AGENDAMENTO: 'Agendada na unidade executante',
  REALIZACAO: 'Atendida na unidade executante',
  FALTA: 'Paciente faltou',
};

const CANCELAVEIS: StatusSolicitacaoRegulacao[] = ['SOLICITADA', 'DEVOLVIDA', 'AUTORIZADA', 'AGENDADA'];
const CID = /^\s*[A-Za-z][0-9]{2}(\.?[0-9A-Za-z]{1,2})?\s*$/;

/**
 * Regulação do acesso (ADR-0088): a fila do regulador, as solicitações da rede e o catálogo de
 * procedimentos regulados, com as gavetas de nova solicitação, análise (autorizar com vaga, devolver,
 * negar, reclassificar), complemento, cancelamento e catálogo. O backend é a ADR-0087. A unidade
 * executante agenda, registra o atendimento com a contrarreferência ou a falta (ADR-0089).
 */
@Component({
  selector: 'app-regulacao',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './regulacao.html',
})
export class Regulacao {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly regulacaoService = inject(RegulacaoService);
  private readonly pacienteService = inject(PacienteService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly agendaService = inject(AgendaService);

  /** Vagas livres de quem vai atender, na unidade executante (ADR-0092): um clique preenche a data. */
  protected readonly vagasSugeridas = signal<ItemAgendaDto[]>([]);
  protected readonly vagasAlvo = signal<'decisao' | 'agendar' | null>(null);
  protected readonly carregandoVagas = signal(false);
  protected readonly erroVagas = signal<string | null>(null);

  protected readonly formatCpf = formatCpf;
  protected readonly prioridades = PRIORIDADES;
  protected readonly listaPrioridades = Object.keys(PRIORIDADES) as PrioridadeRegulacao[];
  protected readonly status = STATUS;
  protected readonly tiposProcedimento = TIPOS_PROCEDIMENTO;
  protected readonly listaTipos = Object.keys(TIPOS_PROCEDIMENTO) as TipoProcedimentoRegulado[];
  protected readonly eventos = EVENTOS;

  /** O que a tela oferece (ADR-0079): sem a permissão, o botão nem aparece. A API continua decidindo. */
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  /** Matrícula do profissional logado (ADR-0065): valor inicial em cada registro. */
  private readonly matriculaPadrao = signal('');

  protected readonly solicitacoes = signal<SolicitacaoRegulacaoResumoDto[]>([]);
  protected readonly fila = signal<SolicitacaoRegulacaoResumoDto[]>([]);
  protected readonly procedimentos = signal<ProcedimentoReguladoResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly aba = signal<Aba>('solicitacoes');
  protected readonly filtroProcedimento = signal('');
  protected readonly filtroPrioridade = signal<PrioridadeRegulacao | ''>('');
  protected readonly filtroStatus = signal<StatusSolicitacaoRegulacao | ''>('');
  protected readonly busca = signal('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly detalhe = signal<SolicitacaoRegulacaoResponseDto | null>(null);
  protected readonly erroDetalhe = signal<string | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly novaForm = this.fb.nonNullable.group({
    buscaPaciente: [''],
    pacienteId: [''],
    procedimentoId: [''],
    unidadeSolicitanteId: [''],
    cid: [''],
    prioridade: ['VERDE' as PrioridadeRegulacao],
    justificativa: [''],
    profissionalMatricula: [''],
  });

  protected readonly decisaoForm = this.fb.nonNullable.group({
    decisao: ['AUTORIZAR' as Decisao],
    unidadeExecutanteId: [''],
    dataHoraPrevista: [''],
    observacao: [''],
    motivo: [''],
    prioridade: ['' as PrioridadeRegulacao | ''],
    profissionalMatricula: [''],
    profissionalExecutanteMatricula: [''],
  });

  protected readonly agendarForm = this.fb.nonNullable.group({
    profissionalExecutanteMatricula: [''],
    dataHora: [''],
    profissionalMatricula: [''],
  });

  protected readonly realizarForm = this.fb.nonNullable.group({
    contrarreferencia: [''],
    profissionalMatricula: [''],
  });

  protected readonly faltaForm = this.fb.nonNullable.group({
    motivo: [''],
    profissionalMatricula: [''],
  });

  protected readonly complementoForm = this.fb.nonNullable.group({
    complemento: [''],
    profissionalMatricula: [''],
  });

  protected readonly cancelarForm = this.fb.nonNullable.group({
    motivo: [''],
    profissionalMatricula: [''],
  });

  protected readonly procedimentoForm = this.fb.nonNullable.group({
    nome: [''],
    tipo: ['CONSULTA_ESPECIALIZADA' as TipoProcedimentoRegulado],
    ativo: [true],
  });

  // ── Dados derivados ────────────────────────────────────────────────────────────────────────────

  protected readonly abas = computed<{ id: Aba; rotulo: string }[]>(() => [
    ...(this.pode('REGULACAO.REGULAR') ? [{ id: 'fila' as Aba, rotulo: 'Fila' }] : []),
    { id: 'solicitacoes', rotulo: 'Solicitações' },
    { id: 'procedimentos', rotulo: 'Procedimentos regulados' },
  ]);

  protected readonly filaFiltrada = computed(() => {
    const proc = this.filtroProcedimento();
    const prio = this.filtroPrioridade();
    return this.fila()
      .filter((s) => !proc || s.procedimentoId === proc)
      .filter((s) => !prio || s.prioridade === prio);
  });

  protected readonly solicitacoesFiltradas = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const st = this.filtroStatus();
    return this.solicitacoes()
      .filter((s) => !st || s.status === st)
      .filter((s) => !q || `${s.pacienteNome} ${s.procedimentoNome} ${s.unidadeSolicitanteNome}`.toLowerCase().includes(q));
  });

  protected readonly resumo = computed(() => {
    const todas = this.solicitacoes();
    const naFila = todas.filter((s) => s.status === 'SOLICITADA');
    return {
      naFila: naFila.length,
      urgentes: naFila.filter((s) => s.prioridade === 'VERMELHO' || s.prioridade === 'AMARELO').length,
      devolvidas: todas.filter((s) => s.status === 'DEVOLVIDA').length,
      autorizadas: todas.filter((s) => s.status === 'AUTORIZADA' || s.status === 'AGENDADA').length,
      esperaMedia: naFila.length ? Math.round(naFila.reduce((acc, s) => acc + diasDesde(s.solicitadoEm), 0) / naFila.length) : 0,
    };
  });

  protected readonly procedimentosAtivos = computed(() => this.procedimentos().filter((p) => p.ativo));

  constructor() {
    this.authService.usuarioAtual().subscribe((u) => this.matriculaPadrao.set(u?.profissionalMatricula ?? ''));
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      // A fila é a aba de quem regula; os demais começam pelas solicitações.
      const pedida = this.route.snapshot.queryParamMap.get('aba') as Aba | null;
      this.aba.set(pedida && this.abas().some((x) => x.id === pedida) ? pedida : this.pode('REGULACAO.REGULAR') ? 'fila' : 'solicitacoes');
    });
    this.carregarTudo();
    this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    this.unidadeSaudeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));

    // "Encaminhar" no atendimento abre a nova solicitação já com o paciente e a unidade (ADR-0088).
    const q = this.route.snapshot.queryParamMap;
    if (q.get('acao') === 'nova') {
      this.abrirNova(q.get('pacienteId') ?? '', q.get('unidadeId') ?? '');
    }
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  private carregarTudo(): void {
    this.carregando.set(true);
    forkJoin({
      solicitacoes: this.regulacaoService.listar().pipe(catchError(() => of([]))),
      fila: this.regulacaoService.fila().pipe(catchError(() => of([]))),
      procedimentos: this.regulacaoService.listarProcedimentos().pipe(catchError(() => of([]))),
    }).subscribe(({ solicitacoes, fila, procedimentos }) => {
      this.solicitacoes.set(solicitacoes);
      this.fila.set(fila);
      this.procedimentos.set(procedimentos);
      this.carregando.set(false);
    });
  }

  // ── Abas ───────────────────────────────────────────────────────────────────────────────────────

  protected contagem(aba: Aba): number {
    if (aba === 'fila') return this.fila().length;
    if (aba === 'solicitacoes') return this.solicitacoes().length;
    return this.procedimentos().length;
  }

  protected selecionarAba(aba: Aba, focar = false): void {
    this.aba.set(aba);
    if (focar) document.getElementById('tab-' + aba)?.focus();
  }

  protected navegarAbas(event: KeyboardEvent, indice: number): void {
    if (event.key !== 'ArrowRight' && event.key !== 'ArrowLeft') return;
    event.preventDefault();
    const abas = this.abas();
    const passo = event.key === 'ArrowRight' ? 1 : -1;
    this.selecionarAba(abas[(indice + passo + abas.length) % abas.length].id, true);
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────────

  protected espera(s: SolicitacaoRegulacaoResumoDto): string {
    const dias = diasDesde(s.solicitadoEm);
    return dias === 0 ? 'hoje' : dias === 1 ? '1 dia' : `${dias} dias`;
  }

  /** Instant (UTC) exibido no horário local. */
  protected formatarInstante(iso: string): string {
    const d = new Date(iso);
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} · ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  /** LocalDateTime da vaga, sem fuso. */
  protected formatarVaga(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [data, hora] = iso.split('T');
    const [y, m, d] = data.split('-');
    return `${d}/${m}/${y} · ${(hora ?? '').slice(0, 5)}`;
  }

  protected podeCancelar(s: SolicitacaoRegulacaoResumoDto): boolean {
    return CANCELAVEIS.includes(s.status) && this.pode('REGULACAO.SOLICITAR', 'REGULACAO.REGULAR');
  }

  protected podeVerDetalhe(): boolean {
    return this.pode('REGULACAO.SOLICITAR', 'REGULACAO.REGULAR');
  }

  protected pacientesFiltrados(): PacienteResponseDto[] {
    const q = this.novaForm.controls.buscaPaciente.value.trim().toLowerCase();
    const digitos = q.replace(/\D/g, '');
    const selecionado = this.novaForm.controls.pacienteId.value;
    const lista = this.pacientes().filter(
      (p) => !q || p.nome.toLowerCase().includes(q) || (digitos.length > 0 && (p.cpf ?? '').replace(/\D/g, '').includes(digitos)),
    );
    const limitada = lista.slice(0, 50);
    const atual = this.pacientes().find((p) => p.uuid === selecionado);
    return atual && !limitada.includes(atual) ? [atual, ...limitada] : limitada;
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'nova':
        return 'Nova solicitação de regulação';
      case 'detalhe':
        return 'Solicitação de regulação';
      case 'cancelar':
        return 'Cancelar solicitação';
      case 'agendar':
        return 'Agendar na unidade executante';
      case 'realizar':
        return 'Registrar atendimento';
      case 'falta':
        return 'Registrar falta';
      case 'procedimento':
        return g.p ? 'Editar procedimento regulado' : 'Novo procedimento regulado';
    }
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.vagasAlvo.set(null);
    this.vagasSugeridas.set([]);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
    this.detalhe.set(null);
  }

  protected abrirNova(pacienteId = '', unidadeId = ''): void {
    this.novaForm.reset({
      buscaPaciente: '',
      pacienteId,
      procedimentoId: '',
      unidadeSolicitanteId: unidadeId,
      cid: '',
      prioridade: 'VERDE',
      justificativa: '',
      profissionalMatricula: this.matriculaPadrao(),
    });
    this.abrir({ tipo: 'nova' });
  }

  protected abrirDetalhe(s: SolicitacaoRegulacaoResumoDto): void {
    this.detalhe.set(null);
    this.erroDetalhe.set(null);
    this.decisaoForm.reset({
      decisao: 'AUTORIZAR',
      unidadeExecutanteId: '',
      dataHoraPrevista: '',
      observacao: '',
      motivo: '',
      prioridade: '',
      profissionalMatricula: this.matriculaPadrao(),
      profissionalExecutanteMatricula: '',
    });
    this.complementoForm.reset({ complemento: '', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'detalhe', uuid: s.uuid });
    this.regulacaoService.buscar(s.uuid).subscribe({
      next: (d) => this.detalhe.set(d),
      error: (e: HttpErrorResponse) =>
        this.erroDetalhe.set(e.status === 403 ? 'Esta solicitação é de uma unidade fora do seu acesso.' : 'Não foi possível abrir a solicitação.'),
    });
  }

  protected abrirCancelamento(s: SolicitacaoRegulacaoResumoDto): void {
    this.cancelarForm.reset({ motivo: '', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'cancelar', s });
  }

  /** A executante agenda a autorizada com quem vai atender; a data já vem com a da vaga. */
  protected abrirAgendamento(s: SolicitacaoRegulacaoResumoDto): void {
    this.agendarForm.reset({
      profissionalExecutanteMatricula: '',
      dataHora: (s.dataHoraPrevista ?? '').slice(0, 16),
      profissionalMatricula: this.matriculaPadrao(),
    });
    this.abrir({ tipo: 'agendar', s });
  }

  protected abrirRealizacao(s: SolicitacaoRegulacaoResumoDto): void {
    this.realizarForm.reset({ contrarreferencia: '', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'realizar', s });
  }

  protected abrirFalta(s: SolicitacaoRegulacaoResumoDto): void {
    this.faltaForm.reset({ motivo: 'Não compareceu', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'falta', s });
  }

  protected podeAgendar(s: SolicitacaoRegulacaoResumoDto): boolean {
    return s.status === 'AUTORIZADA' && this.pode('AGENDAMENTO.GERENCIAR', 'REGULACAO.REGULAR');
  }

  protected podeRegistrarAtendimento(s: SolicitacaoRegulacaoResumoDto): boolean {
    return s.status === 'AGENDADA' && this.pode('CONSULTA.REGISTRAR', 'PROCEDIMENTO.REGISTRAR');
  }

  protected podeRegistrarFalta(s: SolicitacaoRegulacaoResumoDto): boolean {
    return s.status === 'AGENDADA' && this.pode('AGENDAMENTO.GERENCIAR', 'ATENDIMENTO.GERENCIAR');
  }

  protected buscarVagas(unidadeId: string | null | undefined, matricula: string, alvo: 'decisao' | 'agendar'): void {
    if (!unidadeId || !matricula.trim()) return;
    const hoje = new Date();
    const de = `${hoje.getFullYear()}-${pad(hoje.getMonth() + 1)}-${pad(hoje.getDate())}`;
    const fim = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() + 30);
    const ate = `${fim.getFullYear()}-${pad(fim.getMonth() + 1)}-${pad(fim.getDate())}`;
    this.vagasAlvo.set(alvo);
    this.carregandoVagas.set(true);
    this.erroVagas.set(null);
    this.agendaService.vagas(matricula.trim(), unidadeId, de, ate).subscribe({
      next: (v) => {
        this.vagasSugeridas.set(v.slice(0, 12));
        this.carregandoVagas.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.vagasSugeridas.set([]);
        this.carregandoVagas.set(false);
        this.erroVagas.set(e.status === 404 ? 'Matrícula não encontrada.' : 'Não foi possível buscar as vagas.');
      },
    });
  }

  protected escolherVaga(inicio: string, alvo: 'decisao' | 'agendar'): void {
    const valor = inicio.slice(0, 16);
    if (alvo === 'decisao') this.decisaoForm.controls.dataHoraPrevista.setValue(valor);
    else this.agendarForm.controls.dataHora.setValue(valor);
  }

  protected abrirProcedimento(p: ProcedimentoReguladoResponseDto | null = null): void {
    this.procedimentoForm.reset({ nome: p?.nome ?? '', tipo: p?.tipo ?? 'CONSULTA_ESPECIALIZADA', ativo: p?.ativo ?? true });
    this.abrir({ tipo: 'procedimento', p });
  }

  /** O regulador decide quando a solicitação está na fila; o solicitante complementa a devolvida. */
  protected podeDecidir(d: SolicitacaoRegulacaoResponseDto): boolean {
    return d.status === 'SOLICITADA' && this.pode('REGULACAO.REGULAR');
  }

  protected podeComplementar(d: SolicitacaoRegulacaoResponseDto): boolean {
    return d.status === 'DEVOLVIDA' && this.pode('REGULACAO.SOLICITAR');
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    switch (g.tipo) {
      case 'nova':
        return this.salvarNova();
      case 'detalhe': {
        const d = this.detalhe();
        if (!d) return;
        if (this.podeDecidir(d)) return this.salvarDecisao(d);
        if (this.podeComplementar(d)) return this.salvarComplemento(d);
        return;
      }
      case 'cancelar':
        return this.salvarCancelamento(g.s);
      case 'agendar':
        return this.salvarAgendamento(g.s);
      case 'realizar':
        return this.salvarRealizacao(g.s);
      case 'falta':
        return this.salvarFalta(g.s);
      case 'procedimento':
        return this.salvarProcedimento(g.p);
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

  private salvarNova(): void {
    const v = this.novaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['pacienteId'] = 'Selecione o paciente.';
    if (!v.procedimentoId) erros['procedimentoId'] = 'Selecione o procedimento.';
    if (!v.unidadeSolicitanteId) erros['unidadeSolicitanteId'] = 'Selecione a unidade que solicita.';
    if (!CID.test(v.cid)) erros['cid'] = 'Informe o CID-10 (ex.: I10, E11.9).';
    if (v.justificativa.trim().length < 10) erros['justificativa'] = 'Descreva a justificativa clínica (pelo menos 10 caracteres).';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula de quem solicita.';
    if (!this.validar(erros)) return;
    const procedimento = this.procedimentos().find((p) => p.uuid === v.procedimentoId);
    this.enviar(
      this.regulacaoService.solicitar({
        pacienteId: v.pacienteId,
        procedimentoId: v.procedimentoId,
        unidadeSolicitanteId: v.unidadeSolicitanteId,
        profissionalMatricula: v.profissionalMatricula.trim(),
        cid: v.cid.trim(),
        justificativa: v.justificativa.trim(),
        prioridade: v.prioridade,
      }),
      'Solicitação registrada',
      `${procedimento?.nome ?? 'Procedimento'} · ${PRIORIDADES[v.prioridade].rotulo}`,
    );
  }

  private salvarDecisao(d: SolicitacaoRegulacaoResponseDto): void {
    const v = this.decisaoForm.getRawValue();
    const matricula = v.profissionalMatricula.trim();
    const erros: Record<string, string> = {};
    if (!matricula) erros['matriculaRegulador'] = 'Informe a matrícula de quem regula.';
    if (v.decisao === 'AUTORIZAR') {
      if (!v.unidadeExecutanteId) erros['unidadeExecutanteId'] = 'Selecione onde o paciente será atendido.';
      if (!v.dataHoraPrevista) erros['dataHoraPrevista'] = 'Informe a data e a hora da vaga.';
      else if (v.dataHoraPrevista < agoraLocal()) erros['dataHoraPrevista'] = 'A vaga não pode estar no passado.';
    } else if (v.decisao === 'RECLASSIFICAR') {
      if (!v.prioridade) erros['prioridadeNova'] = 'Escolha a nova prioridade.';
      else if (v.prioridade === d.prioridade) erros['prioridadeNova'] = 'Escolha uma prioridade diferente da atual.';
      if (!v.motivo.trim()) erros['motivoDecisao'] = 'Informe o motivo.';
    } else if (!v.motivo.trim()) {
      erros['motivoDecisao'] = v.decisao === 'DEVOLVER' ? 'Diga o que falta para regular.' : 'Informe o motivo da negativa.';
    }
    if (!this.validar(erros)) return;
    const motivo = { profissionalMatricula: matricula, motivo: v.motivo.trim() };
    switch (v.decisao) {
      case 'AUTORIZAR':
        return this.enviar(
          this.regulacaoService.autorizar(d.uuid, {
            profissionalMatricula: matricula,
            unidadeExecutanteId: v.unidadeExecutanteId,
            dataHoraPrevista: v.dataHoraPrevista.length === 16 ? v.dataHoraPrevista + ':00' : v.dataHoraPrevista,
            observacao: v.observacao.trim() || undefined,
            profissionalExecutanteMatricula: v.profissionalExecutanteMatricula.trim() || undefined,
          }),
          v.profissionalExecutanteMatricula.trim() ? 'Solicitação autorizada e agendada' : 'Solicitação autorizada',
          `${d.pacienteNome} · ${this.formatarVaga(v.dataHoraPrevista)}`,
        );
      case 'DEVOLVER':
        return this.enviar(this.regulacaoService.devolver(d.uuid, motivo), 'Solicitação devolvida', d.pacienteNome);
      case 'NEGAR':
        return this.enviar(this.regulacaoService.negar(d.uuid, motivo), 'Solicitação negada', d.pacienteNome);
      case 'RECLASSIFICAR':
        return this.enviar(
          this.regulacaoService.reclassificar(d.uuid, { ...motivo, prioridade: v.prioridade as PrioridadeRegulacao }),
          'Prioridade reclassificada',
          `${d.pacienteNome} · ${PRIORIDADES[v.prioridade as PrioridadeRegulacao].rotulo}`,
        );
    }
  }

  private salvarComplemento(d: SolicitacaoRegulacaoResponseDto): void {
    const v = this.complementoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.complemento.trim()) erros['complemento'] = 'Responda ao que o regulador pediu.';
    if (!v.profissionalMatricula.trim()) erros['matriculaComplemento'] = 'Informe a matrícula de quem complementa.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.regulacaoService.complementar(d.uuid, { profissionalMatricula: v.profissionalMatricula.trim(), complemento: v.complemento.trim() }),
      'Solicitação complementada',
      'De volta à fila, na posição original',
    );
  }

  private salvarCancelamento(s: SolicitacaoRegulacaoResumoDto): void {
    const v = this.cancelarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.motivo.trim()) erros['motivo'] = 'Informe o motivo do cancelamento.';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula de quem cancela.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.regulacaoService.cancelar(s.uuid, { profissionalMatricula: v.profissionalMatricula.trim(), motivo: v.motivo.trim() }),
      'Solicitação cancelada',
      `${s.pacienteNome} · ${s.procedimentoNome}`,
    );
  }

  private salvarAgendamento(s: SolicitacaoRegulacaoResumoDto): void {
    const v = this.agendarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.profissionalExecutanteMatricula.trim()) erros['profissionalExecutanteMatricula'] = 'Informe a matrícula de quem vai atender.';
    if (!v.dataHora) erros['dataHora'] = 'Informe a data e a hora.';
    else if (v.dataHora < agoraLocal()) erros['dataHora'] = 'O agendamento não pode estar no passado.';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula de quem agenda.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.regulacaoService.agendar(s.uuid, {
        profissionalMatricula: v.profissionalMatricula.trim(),
        profissionalExecutanteMatricula: v.profissionalExecutanteMatricula.trim(),
        dataHora: v.dataHora.length === 16 ? v.dataHora + ':00' : v.dataHora,
      }),
      'Solicitação agendada',
      `${s.pacienteNome} · ${this.formatarVaga(v.dataHora)}`,
    );
  }

  private salvarRealizacao(s: SolicitacaoRegulacaoResumoDto): void {
    const v = this.realizarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (v.contrarreferencia.trim().length < 10) erros['contrarreferencia'] = 'Escreva a contrarreferência (pelo menos 10 caracteres).';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula de quem atendeu.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.regulacaoService.realizar(s.uuid, { profissionalMatricula: v.profissionalMatricula.trim(), contrarreferencia: v.contrarreferencia.trim() }),
      'Atendimento registrado',
      `Contrarreferência enviada para ${s.unidadeSolicitanteNome}`,
    );
  }

  private salvarFalta(s: SolicitacaoRegulacaoResumoDto): void {
    const v = this.faltaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.motivo.trim()) erros['motivo'] = 'Descreva a falta.';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula de quem registra.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.regulacaoService.registrarFalta(s.uuid, { profissionalMatricula: v.profissionalMatricula.trim(), motivo: v.motivo.trim() }),
      'Falta registrada',
      s.pacienteNome,
    );
  }

  private salvarProcedimento(p: ProcedimentoReguladoResponseDto | null): void {
    const v = this.procedimentoForm.getRawValue();
    if (!this.validar(v.nome.trim() ? {} : { nome: 'Informe o nome do procedimento.' })) return;
    const dto = { nome: v.nome.trim(), tipo: v.tipo, ativo: v.ativo };
    this.enviar(
      p ? this.regulacaoService.atualizarProcedimento(p.uuid, dto) : this.regulacaoService.criarProcedimento(dto),
      p ? 'Procedimento atualizado' : 'Procedimento cadastrado',
      dto.nome,
    );
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string): void {
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
        const body = error.error as ErrorResponseDto | undefined;
        this.erroApi.set(body?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}

function pad(v: number): string {
  return String(v).padStart(2, '0');
}

function diasDesde(iso: string): number {
  return Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 86_400_000));
}

/** "aaaa-mm-ddThh:mm" local, o formato do input datetime-local. */
function agoraLocal(): string {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
