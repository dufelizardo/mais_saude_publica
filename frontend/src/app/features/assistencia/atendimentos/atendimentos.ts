import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { NgTemplateOutlet } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';
import { AgendamentoResponseDto, StatusAgendamento, TipoAgendamento } from '../../../core/models/agendamento';
import { AtendimentoResponseDto, StatusAtendimento, TipoAtendimento } from '../../../core/models/atendimento';
import { ConsultaResponseDto, TipoConsulta } from '../../../core/models/consulta';
import { EvolucaoEnfermagemResponseDto } from '../../../core/models/evolucao-enfermagem';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ProcedimentoResponseDto, StatusProcedimento } from '../../../core/models/procedimento';
import { ErrorResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { ProntuarioAtendimentoDto, ProntuarioResponseDto } from '../../../core/models/prontuario';
import { SetorResponseDto } from '../../../core/models/setor';
import { ClassificacaoRisco, TriagemResponseDto } from '../../../core/models/triagem';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AgendamentoService } from '../../../core/services/agendamento';
import { AtendimentoService } from '../../../core/services/atendimento';
import { ConsultaService } from '../../../core/services/consulta';
import { EvolucaoEnfermagemService } from '../../../core/services/evolucao-enfermagem';
import { PacienteService } from '../../../core/services/paciente';
import { ProcedimentoService } from '../../../core/services/procedimento';
import { ProntuarioService } from '../../../core/services/prontuario';
import { SetorService } from '../../../core/services/setor';
import { TriagemService } from '../../../core/services/triagem';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';

type Aba = 'atend' | 'ag' | 'pront';

/**
 * Gaveta aberta. As de registro clínico (triagem, consulta, procedimento, evolução, desfecho) e a
 * edição do atendimento abrem de dentro do atendimento e voltam para ele ao salvar ou cancelar.
 */
type Gaveta =
  | { tipo: 'acolhimento' }
  | { tipo: 'ag'; ag: AgendamentoResponseDto | null }
  | { tipo: 'at'; at: AtendimentoResponseDto | null; voltar: boolean }
  | { tipo: 'at-view'; atId: string }
  | { tipo: 'tri'; atId: string; original: TriagemResponseDto | null }
  | { tipo: 'cons'; atId: string; original: ConsultaResponseDto | null }
  | { tipo: 'proc'; atId: string; consultaId: string; original: ProcedimentoResponseDto | null }
  | { tipo: 'proc-status'; atId: string; proc: ProcedimentoResponseDto }
  | { tipo: 'evo'; atId: string; original: EvolucaoEnfermagemResponseDto | null };

const RISCO: Record<ClassificacaoRisco, string> = {
  AZUL: 'Azul · não urgente',
  VERDE: 'Verde · pouco urgente',
  AMARELO: 'Amarelo · urgente',
  LARANJA: 'Laranja · muito urgente',
  VERMELHO: 'Vermelho · emergência',
};

const AT_STATUS: Record<StatusAtendimento, { classe: string; rotulo: string }> = {
  AGENDADO: { classe: 'info', rotulo: 'Aguardando' },
  EM_ANDAMENTO: { classe: 'warn', rotulo: 'Em andamento' },
  CONCLUIDO: { classe: 'ok', rotulo: 'Concluído' },
};

const AG_STATUS: Record<StatusAgendamento, { classe: string; rotulo: string }> = {
  AGENDADO: { classe: 'info', rotulo: 'Agendado' },
  CONFIRMADO: { classe: 'ok', rotulo: 'Confirmado' },
  REALIZADO: { classe: 'muted', rotulo: 'Realizado' },
  CANCELADO: { classe: 'alert', rotulo: 'Cancelado' },
};

const PROC_STATUS: Record<StatusProcedimento, { classe: string; rotulo: string }> = {
  AGENDADO: { classe: 'info', rotulo: 'Agendado' },
  REALIZADO: { classe: 'ok', rotulo: 'Realizado' },
  CANCELADO: { classe: 'alert', rotulo: 'Cancelado' },
};

/**
 * Tela Atendimentos — do agendamento ao prontuário, portada do mockup Atendimentos.html (ADR-0063).
 * Substitui as telas separadas de Atendimentos, Agendamentos, Consultas, Procedimentos e Prontuário.
 * Registros clínicos não são editados: correção é retificação (ADR-0062).
 */
@Component({
  selector: 'app-atendimentos',
  imports: [ReactiveFormsModule, RouterLink, NgTemplateOutlet, Drawer],
  templateUrl: './atendimentos.html',
  styleUrl: './atendimentos.css',
})
export class Atendimentos {
  private readonly fb = inject(FormBuilder);
  private readonly atendimentoService = inject(AtendimentoService);
  private readonly agendamentoService = inject(AgendamentoService);
  private readonly pacienteService = inject(PacienteService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly setorService = inject(SetorService);
  private readonly prontuarioService = inject(ProntuarioService);
  private readonly triagemService = inject(TriagemService);
  private readonly consultaService = inject(ConsultaService);
  private readonly procedimentoService = inject(ProcedimentoService);
  private readonly evolucaoService = inject(EvolucaoEnfermagemService);
  private readonly route = inject(ActivatedRoute);

  protected readonly formatCpf = formatCpf;
  protected readonly risco = RISCO;
  protected readonly riscos = Object.keys(RISCO) as ClassificacaoRisco[];
  protected readonly atStatus = AT_STATUS;
  protected readonly agStatus = AG_STATUS;
  protected readonly procStatus = PROC_STATUS;
  protected readonly atTipos: { valor: TipoAtendimento; rotulo: string }[] = [
    { valor: 'CONSULTA', rotulo: 'Consulta' },
    { valor: 'URGENCIA', rotulo: 'Urgência' },
    { valor: 'INTERNACAO', rotulo: 'Internação' },
  ];
  protected readonly atStatusLista = Object.keys(AT_STATUS) as StatusAtendimento[];
  protected readonly agTipos: { valor: TipoAgendamento; rotulo: string }[] = [
    { valor: 'CONSULTA', rotulo: 'Consulta' },
    { valor: 'PROCEDIMENTO', rotulo: 'Procedimento' },
    { valor: 'RETORNO', rotulo: 'Retorno' },
  ];
  protected readonly agStatusLista = Object.keys(AG_STATUS) as StatusAgendamento[];
  protected readonly consTipos: { valor: TipoConsulta; rotulo: string }[] = [
    { valor: 'PRIMEIRA', rotulo: 'Primeira consulta' },
    { valor: 'RETORNO', rotulo: 'Retorno' },
    { valor: 'URGENCIA', rotulo: 'Urgência' },
  ];
  protected readonly procStatusLista = Object.keys(PROC_STATUS) as StatusProcedimento[];
  protected readonly filtrosStatus: { valor: StatusAtendimento | ''; rotulo: string }[] = [
    { valor: '', rotulo: 'Todos' },
    { valor: 'AGENDADO', rotulo: 'Aguardando' },
    { valor: 'EM_ANDAMENTO', rotulo: 'Em andamento' },
    { valor: 'CONCLUIDO', rotulo: 'Concluídos' },
  ];

  protected readonly atendimentos = signal<AtendimentoResponseDto[]>([]);
  protected readonly agendamentos = signal<AgendamentoResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly aba = signal<Aba>('atend');
  protected readonly buscaAtend = signal('');
  protected readonly filtroStatus = signal<StatusAtendimento | ''>('');
  protected readonly filtroAgPaciente = signal('');

  protected readonly prontPacienteId = signal<string | null>(null);
  protected readonly prontuario = signal<ProntuarioResponseDto | null>(null);
  protected readonly prontuarioCarregando = signal(false);
  protected readonly buscaPor = signal<'cpf' | 'sus'>('cpf');
  protected readonly buscaDocumento = signal('');
  protected readonly buscaMensagem = signal<{ tipo: 'err' | 'warn'; texto: string } | null>(null);

  protected readonly gaveta = signal<Gaveta | null>(null);
  /** Registros do atendimento aberto na gaveta (vêm do prontuário do paciente). */
  protected readonly registros = signal<ProntuarioAtendimentoDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  // Acolhimento: busca por documento → paciente encontrado → dados do atendimento
  protected readonly acolhimentoPaciente = signal<PacienteResponseDto | null>(null);
  protected readonly acolhimentoBuscando = signal(false);

  protected readonly acolhimentoForm = this.fb.nonNullable.group({
    busca: ['cpf' as 'cpf' | 'sus'],
    documento: [''],
    unidadeId: [''],
    setorId: [''],
    profissionalMatricula: [''],
    tipo: ['CONSULTA' as TipoAtendimento],
  });

  protected readonly agForm = this.fb.nonNullable.group({
    buscaPaciente: [''],
    pacienteId: [''],
    dataHora: [''],
    profissionalMatricula: [''],
    tipo: ['CONSULTA' as TipoAgendamento],
    status: ['AGENDADO' as StatusAgendamento],
    observacao: [''],
  });

  protected readonly atForm = this.fb.nonNullable.group({
    buscaPaciente: [''],
    pacienteId: [''],
    agendamentoId: [''],
    unidadeId: [''],
    setorId: [''],
    dataHora: [''],
    profissionalMatricula: [''],
    tipo: ['CONSULTA' as TipoAtendimento],
    status: ['EM_ANDAMENTO' as StatusAtendimento],
  });

  protected readonly triForm = this.fb.nonNullable.group({
    dataHora: [''],
    profissionalMatricula: [''],
    classificacaoRisco: ['' as ClassificacaoRisco | ''],
    pressaoArterial: [''],
    temperatura: [''],
    saturacaoOxigenio: [''],
    frequenciaCardiaca: [''],
    peso: [''],
    observacoes: [''],
    motivoRetificacao: [''],
  });

  protected readonly consForm = this.fb.nonNullable.group({
    dataHora: [''],
    profissionalMatricula: [''],
    tipoConsulta: ['' as TipoConsulta | ''],
    queixaPrincipal: [''],
    diagnostico: [''],
    receituario: [''],
    examesSolicitados: [''],
    retorno: [''],
    motivoRetificacao: [''],
  });

  protected readonly procForm = this.fb.nonNullable.group({
    tipo: [''],
    dataRealizacao: [''],
    profissionalMatricula: [''],
    status: ['REALIZADO' as StatusProcedimento],
    descricao: [''],
    motivoRetificacao: [''],
  });

  protected readonly procStatusForm = this.fb.nonNullable.group({
    status: ['REALIZADO' as 'REALIZADO' | 'CANCELADO'],
    dataRealizacao: [''],
    justificativa: [''],
    profissionalMatricula: [''],
  });

  protected readonly evoForm = this.fb.nonNullable.group({
    dataHora: [''],
    profissionalMatricula: [''],
    descricao: [''],
    motivoRetificacao: [''],
  });

  // ── Dados derivados ────────────────────────────────────────────────────────────────────────────

  protected readonly resumo = computed(() => {
    const hoje = hojeIso();
    const atHoje = this.atendimentos().filter((a) => a.dataHora.startsWith(hoje));
    const agHoje = this.agendamentos().filter((a) => a.dataHora.startsWith(hoje) && a.status !== 'CANCELADO');
    return {
      atendimentosHoje: atHoje.length,
      emAndamentoHoje: atHoje.filter((a) => a.status === 'EM_ANDAMENTO').length,
      aguardando: this.atendimentos().filter((a) => a.status === 'AGENDADO').length,
      agendamentosHoje: agHoje.length,
      confirmadosHoje: agHoje.filter((a) => a.status === 'CONFIRMADO').length,
      riscoAlto: this.atendimentos().filter(
        (a) => a.status !== 'CONCLUIDO' && (a.classificacaoRiscoAtual === 'LARANJA' || a.classificacaoRiscoAtual === 'VERMELHO'),
      ).length,
    };
  });

  protected readonly atFiltrados = computed(() => {
    const q = this.buscaAtend().trim().toLowerCase();
    const status = this.filtroStatus();
    return this.atendimentos()
      .filter((a) => !status || a.status === status)
      .filter((a) => !q || a.pacienteNome.toLowerCase().includes(q))
      .sort((a, b) => b.dataHora.localeCompare(a.dataHora));
  });

  protected readonly agFiltrados = computed(() => {
    const p = this.filtroAgPaciente();
    return this.agendamentos()
      .filter((a) => !p || a.pacienteUuid === p)
      .sort((a, b) => a.dataHora.localeCompare(b.dataHora));
  });

  private readonly atendimentoPorAgendamento = computed(() => {
    const mapa = new Map<string, AtendimentoResponseDto>();
    for (const a of this.atendimentos()) if (a.agendamentoUuid) mapa.set(a.agendamentoUuid, a);
    return mapa;
  });

  protected readonly prontPaciente = computed(() => this.pacientes().find((p) => p.uuid === this.prontPacienteId()) ?? null);

  constructor() {
    this.carregarTudo();
    this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    this.unidadeSaudeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.setorService.listar().pipe(catchError(() => of([]))).subscribe((s) => this.setores.set(s));

    // Links de outras telas (e das rotas antigas): ?aba=ag|pront e ?pacienteId=
    const params = this.route.snapshot.queryParamMap;
    const aba = params.get('aba');
    const pacienteId = params.get('pacienteId');
    if (aba === 'ag' || aba === 'pront' || aba === 'atend') this.aba.set(aba);
    if (pacienteId && aba === 'pront') this.carregarProntuario(pacienteId);
    if (pacienteId && aba === 'ag') this.filtroAgPaciente.set(pacienteId);
  }

  /** Listagens vazias respondem 404 no backend — tratadas como lista vazia. */
  private carregarTudo(depois?: () => void): void {
    this.carregando.set(true);
    forkJoin({
      atendimentos: this.atendimentoService.listar().pipe(catchError(() => of([]))),
      agendamentos: this.agendamentoService.listar().pipe(catchError(() => of([]))),
    }).subscribe(({ atendimentos, agendamentos }) => {
      this.atendimentos.set(atendimentos);
      this.agendamentos.set(agendamentos);
      this.carregando.set(false);
      depois?.();
    });
  }

  // ── Abas ───────────────────────────────────────────────────────────────────────────────────────

  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'atend', rotulo: 'Atendimentos' },
    { id: 'ag', rotulo: 'Agendamentos' },
    { id: 'pront', rotulo: 'Prontuário' },
  ];

  protected contagem(aba: Aba): number | null {
    if (aba === 'atend') return this.atendimentos().length;
    if (aba === 'ag') return this.agendamentos().length;
    return null;
  }

  protected selecionarAba(aba: Aba, focar = false): void {
    this.aba.set(aba);
    if (focar) document.getElementById('tab-' + aba)?.focus();
  }

  protected navegarAbas(event: KeyboardEvent, indice: number): void {
    if (event.key !== 'ArrowRight' && event.key !== 'ArrowLeft') return;
    event.preventDefault();
    const passo = event.key === 'ArrowRight' ? 1 : -1;
    this.selecionarAba(this.abas[(indice + passo + this.abas.length) % this.abas.length].id, true);
  }

  // ── Prontuário ─────────────────────────────────────────────────────────────────────────────────

  protected verProntuario(pacienteId: string): void {
    this.gaveta.set(null);
    this.buscaMensagem.set(null);
    this.carregarProntuario(pacienteId);
    this.selecionarAba('pront', true);
  }

  protected carregarProntuario(pacienteId: string): void {
    this.prontPacienteId.set(pacienteId || null);
    this.prontuario.set(null);
    if (!pacienteId) return;
    this.prontuarioCarregando.set(true);
    this.prontuarioService.buscarPorPacienteId(pacienteId).pipe(catchError(() => of(null))).subscribe((p) => {
      this.prontuario.set(p);
      this.prontuarioCarregando.set(false);
    });
  }

  protected buscarPorDocumento(): void {
    const digitos = this.buscaDocumento().replace(/\D/g, '');
    if (!digitos) {
      this.buscaMensagem.set({ tipo: 'warn', texto: 'Digite o número para buscar.' });
      return;
    }
    this.buscarPaciente(this.buscaPor(), digitos).subscribe((paciente) => {
      if (!paciente) {
        this.buscaMensagem.set({ tipo: 'err', texto: `Nenhum paciente com este ${this.buscaPor() === 'cpf' ? 'CPF' : 'Cartão SUS'}.` });
        return;
      }
      this.buscaMensagem.set(null);
      this.carregarProntuario(paciente.uuid);
    });
  }

  private buscarPaciente(por: 'cpf' | 'sus', digitos: string): Observable<PacienteResponseDto | null> {
    const req = por === 'cpf' ? this.pacienteService.buscarPorCpf(digitos) : this.pacienteService.buscarPorCartaoSus(digitos);
    return new Observable((sub) => {
      req.pipe(catchError(() => of([] as PacienteResponseDto[]))).subscribe((lista) => {
        sub.next(lista.find((p) => p.ativo) ?? lista[0] ?? null);
        sub.complete();
      });
    });
  }

  protected atendimentosDoProntuario(): ProntuarioAtendimentoDto[] {
    const p = this.prontuario();
    return p ? [...p.atendimentos].sort((a, b) => b.atendimento.dataHora.localeCompare(a.atendimento.dataHora)) : [];
  }

  // ── Registros vigentes (ADR-0062) ──────────────────────────────────────────────────────────────

  protected triagensVigentes(r: ProntuarioAtendimentoDto | null): TriagemResponseDto[] {
    return (r?.triagens ?? []).filter((t) => !t.retificado).sort((a, b) => a.dataHora.localeCompare(b.dataHora));
  }

  protected consultasVigentes(r: ProntuarioAtendimentoDto | null) {
    return (r?.consultas ?? []).filter((c) => !c.consulta.retificado).sort((a, b) => a.consulta.dataHora.localeCompare(b.consulta.dataHora));
  }

  protected procedimentosVigentes(lista: ProcedimentoResponseDto[]): ProcedimentoResponseDto[] {
    return lista.filter((p) => !p.retificado);
  }

  protected evolucoesVigentes(r: ProntuarioAtendimentoDto | null): EvolucaoEnfermagemResponseDto[] {
    return (r?.evolucoes ?? []).filter((e) => !e.retificado).sort((a, b) => a.dataHora.localeCompare(b.dataHora));
  }

  protected riscoAtual(r: ProntuarioAtendimentoDto): ClassificacaoRisco | null {
    const t = this.triagensVigentes(r);
    return t.length ? t[t.length - 1].classificacaoRisco : null;
  }

  protected totalProcedimentos(r: ProntuarioAtendimentoDto | null): number {
    return this.consultasVigentes(r).reduce((acc, c) => acc + this.procedimentosVigentes(c.procedimentos).length, 0);
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────────

  protected formatarData(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  protected formatarDataHora(iso: string | null | undefined): string {
    return iso ? `${this.formatarData(iso)} · ${iso.slice(11, 16)}` : '—';
  }

  protected idade(nascimento: string | null | undefined): string {
    if (!nascimento) return '';
    const n = new Date(nascimento + 'T00:00:00');
    const hoje = new Date();
    let anos = hoje.getFullYear() - n.getFullYear();
    if (hoje.getMonth() < n.getMonth() || (hoje.getMonth() === n.getMonth() && hoje.getDate() < n.getDate())) anos--;
    return `${anos} anos`;
  }

  protected corRisco(r: ClassificacaoRisco): string {
    return RISCO[r].split(' · ')[0];
  }

  protected rotulo<T extends string>(lista: { valor: T; rotulo: string }[], valor: T | null | undefined): string {
    return lista.find((x) => x.valor === valor)?.rotulo ?? '—';
  }

  protected iniciais(nome: string): string {
    return nome.split(' ').filter(Boolean).map((p) => p[0]).slice(0, 2).join('').toUpperCase();
  }

  protected atendimento(atId: string): AtendimentoResponseDto | undefined {
    return this.atendimentos().find((a) => a.uuid === atId);
  }

  protected agendamento(agId: string | null | undefined): AgendamentoResponseDto | undefined {
    return agId ? this.agendamentos().find((a) => a.uuid === agId) : undefined;
  }

  protected atendimentoDoAgendamento(agId: string): AtendimentoResponseDto | undefined {
    return this.atendimentoPorAgendamento().get(agId);
  }

  protected podeIniciar(ag: AgendamentoResponseDto): boolean {
    return (ag.status === 'AGENDADO' || ag.status === 'CONFIRMADO') && !this.atendimentoDoAgendamento(ag.uuid);
  }

  protected setoresDaUnidade(unidadeId: string): SetorResponseDto[] {
    return this.setores().filter((s) => s.unidadeUuid === unidadeId && s.ativo);
  }

  protected agendamentosAbertosDoPaciente(pacienteId: string): AgendamentoResponseDto[] {
    return this.agendamentos().filter(
      (a) => a.pacienteUuid === pacienteId && (a.status === 'AGENDADO' || a.status === 'CONFIRMADO') && !this.atendimentoDoAgendamento(a.uuid),
    );
  }

  /** Busca de paciente nas gavetas: filtra por nome ou CPF, no máximo 50 por vez. */
  protected pacientesFiltrados(busca: string, selecionado: string): PacienteResponseDto[] {
    const q = busca.trim().toLowerCase();
    const digitos = q.replace(/\D/g, '');
    const lista = this.pacientes().filter(
      (p) => !q || p.nome.toLowerCase().includes(q) || (digitos.length > 0 && p.cpf.replace(/\D/g, '').includes(digitos)),
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
      case 'acolhimento':
        return 'Acolhimento';
      case 'ag':
        return g.ag ? 'Editar agendamento' : 'Novo agendamento';
      case 'at':
        return g.at ? 'Editar atendimento' : 'Iniciar atendimento';
      case 'at-view':
        return this.atendimento(g.atId)?.pacienteNome ?? 'Atendimento';
      case 'tri':
        return g.original ? 'Retificar triagem' : 'Nova triagem';
      case 'cons':
        return g.original ? 'Retificar consulta' : 'Nova consulta';
      case 'proc':
        return g.original ? 'Retificar procedimento' : 'Novo procedimento';
      case 'proc-status':
        return 'Desfecho do procedimento';
      case 'evo':
        return g.original ? 'Retificar evolução' : 'Nova evolução de enfermagem';
    }
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  /** Fecha a gaveta; as que abriram de dentro do atendimento voltam para ele. */
  protected fecharGaveta(): void {
    const g = this.gaveta();
    const atId = g && 'atId' in g && g.tipo !== 'at-view' ? g.atId : g?.tipo === 'at' && g.voltar && g.at ? g.at.uuid : null;
    if (atId) {
      this.abrirAtendimento(atId);
      return;
    }
    this.gaveta.set(null);
  }

  protected abrirAcolhimento(): void {
    this.acolhimentoPaciente.set(null);
    this.acolhimentoForm.reset({ busca: 'cpf', documento: '', unidadeId: '', setorId: '', profissionalMatricula: '', tipo: 'CONSULTA' });
    this.abrir({ tipo: 'acolhimento' });
  }

  protected buscarAcolhimento(): void {
    const v = this.acolhimentoForm.getRawValue();
    const digitos = v.documento.replace(/\D/g, '');
    if (!digitos) {
      this.erros.set({ documento: 'Digite o CPF ou o Cartão SUS.' });
      return;
    }
    this.erros.set({});
    this.acolhimentoBuscando.set(true);
    this.buscarPaciente(v.busca, digitos).subscribe((p) => {
      this.acolhimentoBuscando.set(false);
      this.acolhimentoPaciente.set(p);
      if (!p) this.erros.set({ documento: `Nenhum paciente com este ${v.busca === 'cpf' ? 'CPF' : 'Cartão SUS'}.` });
    });
  }

  protected abrirAgendamento(ag: AgendamentoResponseDto | null = null, pacienteId = ''): void {
    this.agForm.reset({
      buscaPaciente: '',
      pacienteId: ag?.pacienteUuid ?? pacienteId,
      dataHora: ag?.dataHora?.slice(0, 16) ?? '',
      profissionalMatricula: ag?.profissionalMatricula ?? '',
      tipo: ag?.tipo ?? 'CONSULTA',
      status: ag?.status ?? 'AGENDADO',
      observacao: ag?.observacao ?? '',
    });
    this.abrir({ tipo: 'ag', ag });
  }

  protected abrirNovoAtendimento(pacienteId = '', ag: AgendamentoResponseDto | null = null): void {
    this.atForm.reset({
      buscaPaciente: '',
      pacienteId: ag?.pacienteUuid ?? pacienteId,
      agendamentoId: ag?.uuid ?? '',
      unidadeId: '',
      setorId: '',
      dataHora: agoraLocal(),
      profissionalMatricula: ag?.profissionalMatricula ?? '',
      tipo: 'CONSULTA',
      status: 'EM_ANDAMENTO',
    });
    this.abrir({ tipo: 'at', at: null, voltar: false });
  }

  protected abrirEdicaoAtendimento(at: AtendimentoResponseDto): void {
    this.atForm.reset({
      buscaPaciente: '',
      pacienteId: at.pacienteUuid,
      agendamentoId: at.agendamentoUuid ?? '',
      unidadeId: at.unidadeUuid,
      setorId: at.setorUuid ?? '',
      dataHora: at.dataHora.slice(0, 16),
      profissionalMatricula: at.profissionalMatricula,
      tipo: at.tipo,
      status: at.status,
    });
    this.abrir({ tipo: 'at', at, voltar: true });
  }

  /** Abre o atendimento na gaveta larga, com os registros vindos do prontuário do paciente. */
  protected abrirAtendimento(atId: string): void {
    const at = this.atendimento(atId);
    this.registros.set(null);
    this.abrir({ tipo: 'at-view', atId });
    if (!at) return;
    this.prontuarioService.buscarPorPacienteId(at.pacienteUuid).pipe(catchError(() => of(null))).subscribe((p) => {
      this.registros.set(p?.atendimentos.find((a) => a.atendimento.uuid === atId) ?? null);
    });
  }

  protected abrirTriagem(atId: string, original: TriagemResponseDto | null = null): void {
    this.triForm.reset({
      dataHora: original?.dataHora?.slice(0, 16) ?? agoraLocal(),
      profissionalMatricula: original?.profissionalMatricula ?? '',
      classificacaoRisco: original?.classificacaoRisco ?? '',
      pressaoArterial: original?.pressaoArterial ?? '',
      temperatura: original?.temperatura?.toString() ?? '',
      saturacaoOxigenio: original?.saturacaoOxigenio?.toString() ?? '',
      frequenciaCardiaca: original?.frequenciaCardiaca?.toString() ?? '',
      peso: original?.peso?.toString() ?? '',
      observacoes: original?.observacoes ?? '',
      motivoRetificacao: '',
    });
    this.abrir({ tipo: 'tri', atId, original });
  }

  protected abrirConsulta(atId: string, original: ConsultaResponseDto | null = null): void {
    this.consForm.reset({
      dataHora: original?.dataHora?.slice(0, 16) ?? agoraLocal(),
      profissionalMatricula: original?.profissionalMatricula ?? '',
      tipoConsulta: original?.tipoConsulta ?? '',
      queixaPrincipal: original?.queixaPrincipal ?? '',
      diagnostico: original?.diagnostico ?? '',
      receituario: original?.receituario ?? '',
      examesSolicitados: original?.examesSolicitados ?? '',
      retorno: original?.retorno ?? '',
      motivoRetificacao: '',
    });
    this.abrir({ tipo: 'cons', atId, original });
  }

  protected abrirProcedimento(atId: string, consultaId: string, original: ProcedimentoResponseDto | null = null): void {
    this.procForm.reset({
      tipo: original?.tipo ?? '',
      dataRealizacao: original?.dataRealizacao?.slice(0, 16) ?? agoraLocal(),
      profissionalMatricula: original?.profissionalMatricula ?? '',
      status: original?.status ?? 'REALIZADO',
      descricao: original?.descricao ?? '',
      motivoRetificacao: '',
    });
    this.abrir({ tipo: 'proc', atId, consultaId, original });
  }

  protected abrirDesfecho(atId: string, proc: ProcedimentoResponseDto): void {
    this.procStatusForm.reset({ status: 'REALIZADO', dataRealizacao: agoraLocal(), justificativa: '', profissionalMatricula: '' });
    this.abrir({ tipo: 'proc-status', atId, proc });
  }

  protected abrirEvolucao(atId: string, original: EvolucaoEnfermagemResponseDto | null = null): void {
    this.evoForm.reset({
      dataHora: original?.dataHora?.slice(0, 16) ?? agoraLocal(),
      profissionalMatricula: original?.profissionalMatricula ?? '',
      descricao: original?.descricao ?? '',
      motivoRetificacao: '',
    });
    this.abrir({ tipo: 'evo', atId, original });
  }

  protected concluirAtendimento(at: AtendimentoResponseDto): void {
    this.submitting.set(true);
    this.atendimentoService
      .atualizar(at.uuid, {
        pacienteId: at.pacienteUuid,
        profissionalMatricula: at.profissionalMatricula,
        unidadeId: at.unidadeUuid,
        setorId: at.setorUuid || undefined,
        agendamentoId: at.agendamentoUuid || undefined,
        tipo: at.tipo,
        status: 'CONCLUIDO',
        dataHora: at.dataHora,
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.mostrarToast('Atendimento concluído', at.pacienteNome);
          this.carregarTudo(() => this.abrirAtendimento(at.uuid));
        },
        error: (e: HttpErrorResponse) => {
          this.submitting.set(false);
          this.erroApi.set(mensagem(e, 'Não foi possível concluir o atendimento.'));
        },
      });
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    switch (g.tipo) {
      case 'acolhimento':
        return this.salvarAcolhimento();
      case 'ag':
        return this.salvarAgendamento(g.ag);
      case 'at':
        return this.salvarAtendimento(g.at);
      case 'tri':
        return this.salvarTriagem(g.atId, g.original);
      case 'cons':
        return this.salvarConsulta(g.atId, g.original);
      case 'proc':
        return this.salvarProcedimento(g.atId, g.consultaId, g.original);
      case 'proc-status':
        return this.salvarDesfecho(g.atId, g.proc);
      case 'evo':
        return this.salvarEvolucao(g.atId, g.original);
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

  private exigir(valores: Record<string, string>, mensagens: Record<string, string>): Record<string, string> {
    const erros: Record<string, string> = {};
    for (const [campo, msg] of Object.entries(mensagens)) if (!String(valores[campo] ?? '').trim()) erros[campo] = msg;
    return erros;
  }

  private salvarAcolhimento(): void {
    const paciente = this.acolhimentoPaciente();
    const v = this.acolhimentoForm.getRawValue();
    const erros = this.exigir(v, { unidadeId: 'Selecione a unidade.', profissionalMatricula: 'Informe a matrícula.' });
    if (!paciente) erros['documento'] = 'Busque o paciente pelo CPF ou pelo Cartão SUS.';
    if (!this.validar(erros) || !paciente) return;
    const dataHora = agoraLocal();
    this.enviar(
      this.atendimentoService.criar({
        pacienteId: paciente.uuid,
        profissionalMatricula: v.profissionalMatricula.trim(),
        unidadeId: v.unidadeId,
        setorId: v.setorId || undefined,
        tipo: v.tipo,
        status: 'EM_ANDAMENTO',
        dataHora,
      }),
      'Atendimento iniciado',
      `${paciente.nome} · registre a triagem`,
      () => {
        // Acolhimento segue direto para a triagem do atendimento recém-aberto.
        const novo = this.atendimentos()
          .filter((a) => a.pacienteUuid === paciente.uuid)
          .sort((a, b) => b.dataHora.localeCompare(a.dataHora))[0];
        if (novo) this.abrirTriagem(novo.uuid);
        else this.gaveta.set(null);
      },
    );
  }

  private salvarAgendamento(ag: AgendamentoResponseDto | null): void {
    const v = this.agForm.getRawValue();
    const erros = this.exigir(v, {
      pacienteId: 'Selecione o paciente.',
      dataHora: 'Informe data e hora.',
      profissionalMatricula: 'Informe a matrícula.',
    });
    if (!this.validar(erros)) return;
    const dto = {
      pacienteId: v.pacienteId,
      profissionalMatricula: v.profissionalMatricula.trim(),
      dataHora: v.dataHora,
      status: v.status,
      tipo: v.tipo,
      observacao: v.observacao.trim() || undefined,
    };
    this.enviar(
      ag ? this.agendamentoService.atualizar(ag.uuid, dto) : this.agendamentoService.criar(dto),
      ag ? 'Agendamento atualizado' : 'Agendamento registrado',
      this.formatarDataHora(v.dataHora),
    );
  }

  private salvarAtendimento(at: AtendimentoResponseDto | null): void {
    const v = this.atForm.getRawValue();
    const erros = this.exigir(v, {
      pacienteId: 'Selecione o paciente.',
      unidadeId: 'Selecione a unidade.',
      dataHora: 'Informe data e hora.',
      profissionalMatricula: 'Informe a matrícula.',
    });
    if (!this.validar(erros)) return;
    const dto = {
      pacienteId: v.pacienteId,
      profissionalMatricula: v.profissionalMatricula.trim(),
      unidadeId: v.unidadeId,
      setorId: v.setorId || undefined,
      agendamentoId: v.agendamentoId || undefined,
      tipo: v.tipo,
      status: v.status,
      dataHora: v.dataHora,
    };
    const paciente = this.pacientes().find((p) => p.uuid === v.pacienteId);
    if (at) {
      this.enviar(this.atendimentoService.atualizar(at.uuid, dto), 'Atendimento atualizado', paciente?.nome ?? '', () =>
        this.abrirAtendimento(at.uuid),
      );
      return;
    }
    this.enviar(this.atendimentoService.criar(dto), 'Atendimento iniciado', paciente?.nome ?? '', () => {
      const novo = this.atendimentos()
        .filter((a) => a.pacienteUuid === v.pacienteId)
        .sort((a, b) => b.dataHora.localeCompare(a.dataHora))[0];
      if (novo) this.abrirAtendimento(novo.uuid);
      else this.gaveta.set(null);
    });
  }

  private exigirMotivo(original: unknown, motivo: string, erros: Record<string, string>): void {
    if (original && !motivo.trim()) erros['motivoRetificacao'] = 'Informe o motivo da retificação.';
  }

  private salvarTriagem(atId: string, original: TriagemResponseDto | null): void {
    const v = this.triForm.getRawValue();
    const erros = this.exigir(v, {
      dataHora: 'Informe data e hora.',
      profissionalMatricula: 'Informe a matrícula.',
      classificacaoRisco: 'Selecione a classificação de risco.',
    });
    this.exigirMotivo(original, v.motivoRetificacao, erros);
    if (!this.validar(erros)) return;
    const dto = {
      atendimentoId: atId,
      profissionalMatricula: v.profissionalMatricula.trim(),
      dataHora: v.dataHora,
      classificacaoRisco: v.classificacaoRisco as ClassificacaoRisco,
      pressaoArterial: v.pressaoArterial.trim() || undefined,
      temperatura: numero(v.temperatura),
      saturacaoOxigenio: numero(v.saturacaoOxigenio),
      frequenciaCardiaca: numero(v.frequenciaCardiaca),
      peso: numero(v.peso),
      observacoes: v.observacoes.trim() || undefined,
    };
    this.salvarRegistro(
      atId,
      original
        ? this.triagemService.retificar(original.uuid, { ...dto, motivoRetificacao: v.motivoRetificacao.trim() })
        : this.triagemService.criar(dto),
      original ? 'Triagem retificada' : 'Triagem registrada',
      this.corRisco(dto.classificacaoRisco),
    );
  }

  private salvarConsulta(atId: string, original: ConsultaResponseDto | null): void {
    const v = this.consForm.getRawValue();
    const erros = this.exigir(v, {
      dataHora: 'Informe data e hora.',
      profissionalMatricula: 'Informe a matrícula.',
      tipoConsulta: 'Selecione o tipo de consulta.',
    });
    this.exigirMotivo(original, v.motivoRetificacao, erros);
    if (!this.validar(erros)) return;
    const dto = {
      atendimentoId: atId,
      profissionalMatricula: v.profissionalMatricula.trim(),
      dataHora: v.dataHora,
      tipoConsulta: v.tipoConsulta as TipoConsulta,
      queixaPrincipal: v.queixaPrincipal.trim() || undefined,
      diagnostico: v.diagnostico.trim() || undefined,
      receituario: v.receituario.trim() || undefined,
      examesSolicitados: v.examesSolicitados.trim() || undefined,
      retorno: v.retorno || undefined,
    };
    this.salvarRegistro(
      atId,
      original
        ? this.consultaService.retificar(original.uuid, { ...dto, motivoRetificacao: v.motivoRetificacao.trim() })
        : this.consultaService.criar(dto),
      original ? 'Consulta retificada' : 'Consulta registrada',
      this.rotulo(this.consTipos, dto.tipoConsulta),
    );
  }

  private salvarProcedimento(atId: string, consultaId: string, original: ProcedimentoResponseDto | null): void {
    const v = this.procForm.getRawValue();
    const erros = this.exigir(v, {
      tipo: 'Informe o tipo de procedimento.',
      dataRealizacao: 'Informe a data.',
      profissionalMatricula: 'Informe a matrícula.',
    });
    this.exigirMotivo(original, v.motivoRetificacao, erros);
    if (!this.validar(erros)) return;
    const dto = {
      consultaId,
      profissionalMatricula: v.profissionalMatricula.trim(),
      tipo: v.tipo.trim(),
      descricao: v.descricao.trim() || undefined,
      dataRealizacao: v.dataRealizacao,
      status: v.status,
    };
    this.salvarRegistro(
      atId,
      original
        ? this.procedimentoService.retificar(original.uuid, { ...dto, motivoRetificacao: v.motivoRetificacao.trim() })
        : this.procedimentoService.criar(dto),
      original ? 'Procedimento retificado' : 'Procedimento registrado',
      dto.tipo,
    );
  }

  private salvarDesfecho(atId: string, proc: ProcedimentoResponseDto): void {
    const v = this.procStatusForm.getRawValue();
    const erros = this.exigir(v, { profissionalMatricula: 'Informe a matrícula.' });
    if (v.status === 'REALIZADO' && !v.dataRealizacao) erros['dataRealizacao'] = 'Informe quando foi realizado.';
    if (v.status === 'CANCELADO' && !v.justificativa.trim()) erros['justificativa'] = 'Informe por que foi cancelado.';
    if (!this.validar(erros)) return;
    this.salvarRegistro(
      atId,
      this.procedimentoService.alterarStatus(proc.uuid, {
        status: v.status,
        profissionalMatricula: v.profissionalMatricula.trim(),
        dataRealizacao: v.status === 'REALIZADO' ? v.dataRealizacao : undefined,
        justificativa: v.status === 'CANCELADO' ? v.justificativa.trim() : undefined,
      }),
      v.status === 'REALIZADO' ? 'Procedimento realizado' : 'Procedimento cancelado',
      proc.tipo,
    );
  }

  private salvarEvolucao(atId: string, original: EvolucaoEnfermagemResponseDto | null): void {
    const v = this.evoForm.getRawValue();
    const erros = this.exigir(v, {
      dataHora: 'Informe data e hora.',
      profissionalMatricula: 'Informe a matrícula.',
      descricao: 'Descreva a evolução.',
    });
    this.exigirMotivo(original, v.motivoRetificacao, erros);
    if (!this.validar(erros)) return;
    const dto = {
      atendimentoId: atId,
      profissionalMatricula: v.profissionalMatricula.trim(),
      dataHora: v.dataHora,
      descricao: v.descricao.trim(),
    };
    this.salvarRegistro(
      atId,
      original
        ? this.evolucaoService.retificar(original.uuid, { ...dto, motivoRetificacao: v.motivoRetificacao.trim() })
        : this.evolucaoService.criar(dto),
      original ? 'Evolução retificada' : 'Evolução registrada',
      this.atendimento(atId)?.pacienteNome ?? '',
    );
  }

  /** Registro clínico salvo → recarrega as listas (o resumo muda) e volta para o atendimento. */
  private salvarRegistro(atId: string, req: Observable<SuccessResponseDto>, titulo: string, detalhe: string): void {
    this.enviar(req, titulo, detalhe, () => this.abrirAtendimento(atId));
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string, depois?: () => void): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.mostrarToast(titulo, detalhe);
        const pront = this.prontPacienteId();
        this.carregarTudo(() => {
          if (pront) this.carregarProntuario(pront);
          if (depois) depois();
          else this.gaveta.set(null);
        });
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set(mensagem(e, 'Não foi possível salvar. Tente novamente.'));
      },
    });
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}

function mensagem(e: HttpErrorResponse, padrao: string): string {
  return (e.error as ErrorResponseDto | undefined)?.message ?? padrao;
}

function numero(v: string): number | undefined {
  const n = Number(v.replace(',', '.'));
  return v.trim() === '' || Number.isNaN(n) ? undefined : n;
}

function pad(v: number): string {
  return String(v).padStart(2, '0');
}

function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function agoraLocal(): string {
  const d = new Date();
  return `${hojeIso()}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
