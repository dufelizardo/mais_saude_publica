import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  AgendaResponseDto,
  BlocoAgendaResponseDto,
  BloqueioAgendaResponseDto,
  DiaSemana,
  ItemAgendaDto,
  MotivoBloqueioAgenda,
} from '../../../core/models/agenda';
import { StatusAgendamento, TipoAgendamento } from '../../../core/models/agendamento';
import { AcessoDaInterface } from '../../../core/models/auth';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto, ProfissionalResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AgendaService } from '../../../core/services/agenda';
import { AgendamentoService } from '../../../core/services/agendamento';
import { AuthService } from '../../../core/services/auth';
import { PacienteService } from '../../../core/services/paciente';
import { ProfissionalService } from '../../../core/services/profissional';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';

type Visao = 'semana' | 'dia';

type Gaveta =
  | { tipo: 'marcar' }
  | { tipo: 'marcacao'; item: ItemAgendaDto }
  | { tipo: 'bloco' }
  | { tipo: 'encerrar'; bloco: BlocoAgendaResponseDto }
  | { tipo: 'bloqueio' };

/** Item posicionado no calendário: topo e altura em px, faixa (lane) quando há sobreposição. */
interface ItemNoDia {
  item: ItemAgendaDto;
  topo: number;
  altura: number;
  esquerda: number;
  largura: number;
  classe: string;
}

interface ColunaDia {
  data: string;
  rotulo: string;
  numero: number;
  hoje: boolean;
  fimDeSemana: boolean;
  marcacoes: number;
  itens: ItemNoDia[];
}

/** Altura de uma hora no calendário, como no protótipo. */
const HORA_PX = 60;
const META_OCUPACAO = 85;
const NOMES_DIA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const NOMES_MES = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho', 'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];

export const DIAS_SEMANA: { valor: DiaSemana; rotulo: string }[] = [
  { valor: 'MONDAY', rotulo: 'Segunda' },
  { valor: 'TUESDAY', rotulo: 'Terça' },
  { valor: 'WEDNESDAY', rotulo: 'Quarta' },
  { valor: 'THURSDAY', rotulo: 'Quinta' },
  { valor: 'FRIDAY', rotulo: 'Sexta' },
  { valor: 'SATURDAY', rotulo: 'Sábado' },
  { valor: 'SUNDAY', rotulo: 'Domingo' },
];

const TIPOS: Record<TipoAgendamento, string> = { CONSULTA: 'Consulta', PROCEDIMENTO: 'Procedimento', RETORNO: 'Retorno' };

const STATUS: Record<StatusAgendamento, string> = {
  AGENDADO: 'Agendado',
  CONFIRMADO: 'Confirmado',
  REALIZADO: 'Realizado',
  CANCELADO: 'Cancelado',
  FALTOU: 'Faltou',
};

const MOTIVOS: Record<MotivoBloqueioAgenda, string> = {
  FOLGA: 'Folga',
  REUNIAO: 'Reunião',
  CAPACITACAO: 'Capacitação',
  UNIDADE_FECHADA: 'Unidade fechada',
  OUTRO: 'Outro',
};

const AFASTAMENTOS: Record<string, string> = {
  FERIAS: 'Férias',
  LICENCA_MEDICA: 'Licença médica',
  LICENCA_PESSOAL: 'Licença pessoal',
  OUTROS: 'Afastamento',
};

/**
 * Agenda do profissional por unidade (ADR-0092), portada do protótipo Agenda.html: visões Semana e Dia, indicadores
 * do período, vagas clicáveis, marcações, encaixes, faltas, bloqueios e afastamentos do RH, agenda recorrente e
 * bloqueios no painel lateral. Sem domínio ainda (Em breve): visão Mês, atividades coletivas, programas,
 * especialidade e exportação. O backend é a ADR-0091.
 */
@Component({
  selector: 'app-agenda',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './agenda.html',
})
export class Agenda {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly agendaService = inject(AgendaService);
  private readonly agendamentoService = inject(AgendamentoService);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly pacienteService = inject(PacienteService);

  protected readonly formatCpf = formatCpf;
  protected readonly horaPx = HORA_PX;
  protected readonly meta = META_OCUPACAO;
  protected readonly diasSemana = DIAS_SEMANA;
  protected readonly tipos = TIPOS;
  protected readonly listaTipos = Object.keys(TIPOS) as TipoAgendamento[];
  protected readonly status = STATUS;
  protected readonly motivos = MOTIVOS;
  protected readonly listaMotivos = Object.keys(MOTIVOS) as MotivoBloqueioAgenda[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  private readonly matriculaLogada = signal('');

  protected readonly profissionais = signal<ProfissionalResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);

  protected readonly matricula = signal('');
  protected readonly unidadeId = signal('');
  protected readonly visao = signal<Visao>('semana');
  protected readonly referencia = signal(hojeIso());
  protected readonly filtroTipo = signal<TipoAgendamento | ''>('');

  protected readonly agenda = signal<AgendaResponseDto | null>(null);
  protected readonly blocos = signal<BlocoAgendaResponseDto[]>([]);
  protected readonly bloqueios = signal<BloqueioAgendaResponseDto[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erroAgenda = signal<string | null>(null);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly marcarForm = this.fb.nonNullable.group({
    buscaPaciente: [''],
    pacienteId: [''],
    dataHora: [''],
    tipo: ['CONSULTA' as TipoAgendamento],
    encaixe: [false],
    observacao: [''],
  });

  protected readonly blocoForm = this.fb.nonNullable.group({
    diaSemana: ['MONDAY' as DiaSemana],
    horaInicio: ['08:00'],
    horaFim: ['12:00'],
    duracaoMinutos: ['20'],
    tipo: ['CONSULTA' as TipoAgendamento],
    vigenteDesde: [''],
  });

  protected readonly encerrarForm = this.fb.nonNullable.group({ vigenteAte: [''] });

  protected readonly bloqueioForm = this.fb.nonNullable.group({
    escopo: ['profissional' as 'profissional' | 'unidade'],
    inicio: [''],
    fim: [''],
    motivo: ['REUNIAO' as MotivoBloqueioAgenda],
    descricao: [''],
  });

  // ── Período e calendário ─────────────────────────────────────────────────────────────────────

  protected readonly periodo = computed(() => {
    const ref = this.referencia();
    if (this.visao() === 'dia') return { de: ref, ate: ref };
    const de = segundaDe(ref);
    return { de, ate: somarDias(de, 6) };
  });

  protected readonly rotuloPeriodo = computed(() => {
    const { de, ate } = this.periodo();
    const [, , d1] = de.split('-').map(Number);
    const [ya, ma, d2] = ate.split('-').map(Number);
    return this.visao() === 'dia'
      ? `${nomeDia(de)}, ${d1} de ${NOMES_MES[ma - 1]} · ${ya}`
      : `${d1} a ${d2} de ${NOMES_MES[ma - 1]} · ${ya}`;
  });

  /** Da segunda ao domingo e, no dia, pela hora; os encerrados por último. */
  protected readonly blocosOrdenados = computed(() => {
    const ordem = DIAS_SEMANA.map((d) => d.valor);
    return [...this.blocos()].sort(
      (a, b) => Number(!!a.vigenteAte) - Number(!!b.vigenteAte) || ordem.indexOf(a.diaSemana) - ordem.indexOf(b.diaSemana) || a.horaInicio.localeCompare(b.horaInicio),
    );
  });

  protected readonly profissionalNome = computed(() => this.profissionais().find((p) => p.matricula === this.matricula())?.nome ?? '');
  protected readonly unidadeNome = computed(() => this.unidades().find((u) => u.uuid === this.unidadeId())?.nome ?? '');

  /** Faixa de horas do calendário: 7h às 19h, ampliada para caber o que houver fora disso. */
  protected readonly horas = computed(() => {
    let ini = 7;
    let fim = 19;
    for (const d of this.agenda()?.dias ?? []) {
      for (const i of d.itens) {
        if (i.tipo === 'AFASTAMENTO' || (i.tipo === 'BLOQUEIO' && duracaoMin(i) >= 23 * 60)) continue;
        ini = Math.min(ini, hora(i.inicio));
        fim = Math.max(fim, Math.ceil(hora(i.fim) + minuto(i.fim) / 60));
      }
    }
    return Array.from({ length: fim - ini }, (_, k) => ini + k);
  });

  protected readonly colunas = computed<ColunaDia[]>(() => {
    const ag = this.agenda();
    if (!ag) return [];
    const inicioDia = this.horas()[0] ?? 7;
    const fimDia = (this.horas().at(-1) ?? 18) + 1;
    const tipo = this.filtroTipo();
    const hoje = hojeIso();
    return ag.dias.map((d) => {
      const visiveis = d.itens.filter((i) => !tipo || !i.tipoAtendimento || i.tipoAtendimento === tipo || i.tipo === 'BLOQUEIO' || i.tipo === 'AFASTAMENTO');
      const dow = new Date(d.data + 'T12:00:00').getDay();
      return {
        data: d.data,
        rotulo: NOMES_DIA[dow] + (d.data === hoje ? ' · hoje' : ''),
        numero: Number(d.data.slice(8, 10)),
        hoje: d.data === hoje,
        fimDeSemana: dow === 0 || dow === 6,
        marcacoes: d.itens.filter((i) => i.tipo === 'MARCACAO' || i.tipo === 'ENCAIXE').length,
        itens: posicionar(visiveis, inicioDia, fimDia),
      };
    });
  });

  /** Linha de "agora", em px a partir do topo, quando hoje está no período e o horário cabe na faixa. */
  protected readonly agoraPx = computed(() => {
    const h = this.horas();
    if (!h.length) return null;
    const d = new Date();
    const min = (d.getHours() - h[0]) * 60 + d.getMinutes();
    return min < 0 || min > h.length * 60 ? null : (min * HORA_PX) / 60;
  });

  protected readonly resumoHoje = computed(() => {
    const dia = this.agenda()?.dias.find((d) => d.data === hojeIso());
    const itens = dia?.itens ?? [];
    return {
      marcacoes: itens.filter((i) => i.tipo === 'MARCACAO' || i.tipo === 'ENCAIXE').length,
      proximas: itens.filter((i) => (i.tipo === 'MARCACAO' || i.tipo === 'ENCAIXE') && i.inicio >= agoraLocal() && i.status !== 'FALTOU').slice(0, 6),
    };
  });

  /** Mini-mês do mês da referência, semanas começando na segunda. */
  protected readonly mini = computed(() => {
    const ref = this.referencia();
    const [y, m] = ref.split('-').map(Number);
    const primeiro = `${y}-${pad(m)}-01`;
    const inicio = segundaDe(primeiro);
    const { de, ate } = this.periodo();
    const comMarcacao = new Set((this.agenda()?.dias ?? []).filter((d) => d.itens.some((i) => i.tipo === 'MARCACAO' || i.tipo === 'ENCAIXE')).map((d) => d.data));
    const hoje = hojeIso();
    return {
      titulo: `${NOMES_MES[m - 1]} · ${y}`,
      dias: Array.from({ length: 42 }, (_, k) => {
        const data = somarDias(inicio, k);
        return {
          data,
          numero: Number(data.slice(8, 10)),
          outroMes: Number(data.slice(5, 7)) !== m,
          hoje: data === hoje,
          noPeriodo: data >= de && data <= ate,
          temMarcacao: comMarcacao.has(data),
        };
      }),
    };
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    const q = this.route.snapshot.queryParamMap;
    if (q.get('data')) this.referencia.set(q.get('data')!);
    if (q.get('visao') === 'dia') this.visao.set('dia');
    forkJoin({
      profissionais: this.profissionalService.listar().pipe(catchError(() => of([]))),
      unidades: this.unidadeSaudeService.listar().pipe(catchError(() => of([]))),
      usuario: this.authService.usuarioAtual().pipe(catchError(() => of(null))),
    }).subscribe(({ profissionais, unidades, usuario }) => {
      this.profissionais.set(profissionais);
      this.unidades.set(unidades);
      this.matriculaLogada.set(usuario?.profissionalMatricula ?? '');
      // Abre na agenda de quem está logado, quando é profissional; a URL manda.
      this.matricula.set(q.get('profissional') ?? (profissionais.some((p) => p.matricula === usuario?.profissionalMatricula) ? usuario!.profissionalMatricula! : ''));
      this.unidadeId.set(q.get('unidade') ?? (unidades.length === 1 ? unidades[0].uuid : ''));
      this.carregar();
    });
    this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected escolhido(): boolean {
    return !!this.matricula() && !!this.unidadeId();
  }

  private carregar(): void {
    this.router.navigate([], {
      queryParams: { profissional: this.matricula() || null, unidade: this.unidadeId() || null, data: this.referencia(), visao: this.visao() },
      replaceUrl: true,
    });
    if (!this.escolhido()) {
      this.agenda.set(null);
      return;
    }
    const { de, ate } = this.periodo();
    this.carregando.set(true);
    this.erroAgenda.set(null);
    forkJoin({
      agenda: this.agendaService.agenda(this.matricula(), this.unidadeId(), de, ate),
      blocos: this.agendaService.listarBlocos(this.matricula(), this.unidadeId()).pipe(catchError(() => of([]))),
      bloqueios: this.agendaService.listarBloqueios(this.matricula(), this.unidadeId(), de, ate).pipe(catchError(() => of([]))),
    }).subscribe({
      next: ({ agenda, blocos, bloqueios }) => {
        this.agenda.set(agenda);
        this.blocos.set(blocos);
        this.bloqueios.set(bloqueios);
        this.carregando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.agenda.set(null);
        this.carregando.set(false);
        this.erroAgenda.set(e.status === 403 ? 'Esta unidade está fora do seu acesso.' : 'Não foi possível carregar a agenda.');
      },
    });
  }

  protected escolherProfissional(matricula: string): void {
    this.matricula.set(matricula);
    this.carregar();
  }

  protected escolherUnidade(uuid: string): void {
    this.unidadeId.set(uuid);
    this.carregar();
  }

  protected mudarVisao(v: Visao): void {
    this.visao.set(v);
    this.carregar();
  }

  protected navegar(passo: number): void {
    this.referencia.set(somarDias(this.referencia(), passo * (this.visao() === 'dia' ? 1 : 7)));
    this.carregar();
  }

  protected irParaHoje(): void {
    this.referencia.set(hojeIso());
    this.carregar();
  }

  protected irParaDia(data: string): void {
    this.referencia.set(data);
    this.carregar();
  }

  protected mesAnterior(passo: number): void {
    const [y, m] = this.referencia().split('-').map(Number);
    const d = new Date(y, m - 1 + passo, 1);
    this.referencia.set(`${d.getFullYear()}-${pad(d.getMonth() + 1)}-01`);
    this.carregar();
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────

  protected horaDe(iso: string): string {
    return iso.slice(11, 16);
  }

  protected dataDe(iso: string): string {
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  protected rotuloItem(i: ItemAgendaDto): string {
    switch (i.tipo) {
      case 'VAGA':
        return 'Vaga livre';
      case 'BLOQUEIO':
        return i.descricao?.split(':')[0] ? MOTIVOS[i.descricao.split(':')[0] as MotivoBloqueioAgenda] ?? 'Bloqueio' : 'Bloqueio';
      case 'AFASTAMENTO':
        return AFASTAMENTOS[i.descricao ?? ''] ?? 'Afastamento';
      default:
        return i.pacienteNome ?? 'Marcação';
    }
  }

  protected descricaoBloqueio(i: ItemAgendaDto): string {
    const partes = (i.descricao ?? '').split(': ');
    return partes.length > 1 ? partes.slice(1).join(': ') : '';
  }

  protected podeMarcarNaVaga(i: ItemAgendaDto): boolean {
    return i.tipo === 'VAGA' && i.inicio > agoraLocal() && this.pode('AGENDAMENTO.GERENCIAR');
  }

  protected clicarItem(i: ItemAgendaDto): void {
    if (i.tipo === 'VAGA') {
      if (this.podeMarcarNaVaga(i)) this.abrirMarcar(i.inicio.slice(0, 16), i.tipoAtendimento ?? 'CONSULTA');
    } else if (i.tipo === 'MARCACAO' || i.tipo === 'ENCAIXE') {
      this.abrir({ tipo: 'marcacao', item: i });
    }
  }

  protected ariaItem(i: ItemAgendaDto): string {
    const quando = `${this.horaDe(i.inicio)} a ${this.horaDe(i.fim)}`;
    if (i.tipo === 'VAGA') return `Vaga livre às ${this.horaDe(i.inicio)} de ${this.dataDe(i.inicio)}`;
    if (i.tipo === 'MARCACAO' || i.tipo === 'ENCAIXE') {
      return `${i.tipo === 'ENCAIXE' ? 'Encaixe' : 'Marcação'} de ${i.pacienteNome} às ${this.horaDe(i.inicio)} de ${this.dataDe(i.inicio)}`;
    }
    return `${this.rotuloItem(i)}, ${quando}`;
  }

  protected rotuloDiaSemana(d: DiaSemana): string {
    return DIAS_SEMANA.find((x) => x.valor === d)?.rotulo ?? d;
  }

  protected pacientesFiltrados(): PacienteResponseDto[] {
    const q = this.marcarForm.controls.buscaPaciente.value.trim().toLowerCase();
    const digitos = q.replace(/\D/g, '');
    const selecionado = this.marcarForm.controls.pacienteId.value;
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

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'marcar':
        return 'Nova marcação';
      case 'marcacao':
        return g.item.tipo === 'ENCAIXE' ? 'Encaixe' : 'Marcação';
      case 'bloco':
        return 'Novo bloco da agenda';
      case 'encerrar':
        return 'Encerrar bloco da agenda';
      case 'bloqueio':
        return 'Bloquear a agenda';
    }
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  /** Sem vaga escolhida, abre como encaixe: o horário é livre. */
  protected abrirMarcar(dataHora = '', tipo: TipoAgendamento = 'CONSULTA'): void {
    this.marcarForm.reset({ buscaPaciente: '', pacienteId: '', dataHora, tipo, encaixe: !dataHora, observacao: '' });
    this.abrir({ tipo: 'marcar' });
  }

  protected abrirBloco(): void {
    this.blocoForm.reset({ diaSemana: 'MONDAY', horaInicio: '08:00', horaFim: '12:00', duracaoMinutos: '20', tipo: 'CONSULTA', vigenteDesde: hojeIso() });
    this.abrir({ tipo: 'bloco' });
  }

  protected abrirEncerrar(bloco: BlocoAgendaResponseDto): void {
    this.encerrarForm.reset({ vigenteAte: hojeIso() });
    this.abrir({ tipo: 'encerrar', bloco });
  }

  protected abrirBloqueio(): void {
    const dia = this.visao() === 'dia' ? this.referencia() : hojeIso();
    this.bloqueioForm.reset({ escopo: 'profissional', inicio: `${dia}T12:00`, fim: `${dia}T13:00`, motivo: 'REUNIAO', descricao: '' });
    this.abrir({ tipo: 'bloqueio' });
  }

  protected vagasDoBloco(): number {
    const v = this.blocoForm.getRawValue();
    const min = minutosEntre(v.horaInicio, v.horaFim);
    const dur = Number(v.duracaoMinutos);
    return min > 0 && dur > 0 ? Math.floor(min / dur) : 0;
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    switch (g.tipo) {
      case 'marcar':
        return this.salvarMarcacao();
      case 'bloco':
        return this.salvarBloco();
      case 'encerrar':
        return this.salvarEncerramento(g.bloco);
      case 'bloqueio':
        return this.salvarBloqueio();
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

  private salvarMarcacao(): void {
    const v = this.marcarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['pacienteId'] = 'Selecione o paciente.';
    if (!v.dataHora) erros['dataHora'] = 'Informe a data e a hora.';
    if (!this.validar(erros)) return;
    const paciente = this.pacientes().find((p) => p.uuid === v.pacienteId);
    this.enviar(
      this.agendamentoService.criar({
        pacienteId: v.pacienteId,
        profissionalMatricula: this.matricula(),
        dataHora: v.dataHora.length === 16 ? v.dataHora + ':00' : v.dataHora,
        status: 'AGENDADO',
        tipo: v.tipo,
        observacao: v.observacao.trim() || undefined,
        unidadeId: this.unidadeId(),
        encaixe: v.encaixe,
      }),
      v.encaixe ? 'Encaixe marcado' : 'Marcação feita',
      `${paciente?.nome ?? 'Paciente'} · ${this.dataDe(v.dataHora)} ${v.dataHora.slice(11, 16)}`,
    );
  }

  protected registrarFalta(i: ItemAgendaDto): void {
    if (!i.agendamentoId || this.submitting()) return;
    this.enviar(this.agendamentoService.registrarFalta(i.agendamentoId), 'Falta registrada', i.pacienteNome ?? '');
  }

  protected cancelarMarcacao(i: ItemAgendaDto): void {
    if (!i.agendamentoId || !i.pacienteId || this.submitting()) return;
    this.enviar(
      this.agendamentoService.atualizar(i.agendamentoId, {
        pacienteId: i.pacienteId,
        profissionalMatricula: this.matricula(),
        dataHora: i.inicio,
        status: 'CANCELADO',
        tipo: i.tipoAtendimento ?? 'CONSULTA',
        observacao: i.descricao ?? undefined,
        unidadeId: this.unidadeId(),
        encaixe: i.tipo === 'ENCAIXE',
      }),
      'Marcação cancelada',
      `${i.pacienteNome} · a vaga volta para a agenda`,
    );
  }

  private salvarBloco(): void {
    const v = this.blocoForm.getRawValue();
    const erros: Record<string, string> = {};
    const dur = Number(v.duracaoMinutos);
    if (!v.horaInicio) erros['horaInicio'] = 'Informe o início.';
    if (!v.horaFim || minutosEntre(v.horaInicio, v.horaFim) <= 0) erros['horaFim'] = 'O fim precisa ser depois do início.';
    if (!Number.isInteger(dur) || dur < 5 || dur > 240) erros['duracaoMinutos'] = 'Use de 5 a 240 minutos.';
    else if (!erros['horaFim'] && minutosEntre(v.horaInicio, v.horaFim) < dur) erros['duracaoMinutos'] = 'O bloco é menor que uma vaga.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.agendaService.criarBloco({
        profissionalMatricula: this.matricula(),
        unidadeId: this.unidadeId(),
        diaSemana: v.diaSemana,
        horaInicio: v.horaInicio,
        horaFim: v.horaFim,
        duracaoMinutos: dur,
        tipo: v.tipo,
        vigenteDesde: v.vigenteDesde || undefined,
      }),
      'Bloco criado',
      `${this.rotuloDiaSemana(v.diaSemana)}, ${v.horaInicio} às ${v.horaFim} · ${this.vagasDoBloco()} vagas`,
    );
  }

  private salvarEncerramento(bloco: BlocoAgendaResponseDto): void {
    const v = this.encerrarForm.getRawValue();
    if (!this.validar(v.vigenteAte ? {} : { vigenteAte: 'Informe o último dia do bloco.' })) return;
    this.enviar(this.agendaService.encerrarBloco(bloco.uuid, v.vigenteAte), 'Bloco encerrado', `Vale até ${this.dataDe(v.vigenteAte)}`);
  }

  private salvarBloqueio(): void {
    const v = this.bloqueioForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.inicio) erros['inicio'] = 'Informe o início.';
    if (!v.fim || (v.inicio && v.fim <= v.inicio)) erros['fim'] = 'O fim precisa ser depois do início.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.agendaService.criarBloqueio({
        profissionalMatricula: v.escopo === 'profissional' ? this.matricula() : undefined,
        unidadeId: this.unidadeId(),
        inicio: v.inicio + ':00',
        fim: v.fim + ':00',
        motivo: v.motivo,
        descricao: v.descricao.trim() || undefined,
      }),
      'Agenda bloqueada',
      `${MOTIVOS[v.motivo]} · ${this.dataDe(v.inicio)} ${v.inicio.slice(11, 16)}`,
    );
  }

  protected removerBloqueio(b: BloqueioAgendaResponseDto): void {
    if (this.submitting()) return;
    this.enviar(this.agendaService.removerBloqueio(b.uuid), 'Bloqueio removido', 'As vagas do período voltam para a agenda');
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(titulo, detalhe);
        this.carregar();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        const msg = body?.message ?? 'Não foi possível salvar. Tente novamente.';
        if (this.gaveta()) this.erroApi.set(msg);
        else this.mostrarToast('Não foi possível concluir', msg);
      },
    });
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}

// ── Apoio ────────────────────────────────────────────────────────────────────────────────────

function pad(v: number): string {
  return String(v).padStart(2, '0');
}

function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function agoraLocal(): string {
  const d = new Date();
  return `${hojeIso()}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
}

function somarDias(iso: string, dias: number): string {
  const [y, m, d] = iso.split('-').map(Number);
  const r = new Date(y, m - 1, d + dias);
  return `${r.getFullYear()}-${pad(r.getMonth() + 1)}-${pad(r.getDate())}`;
}

function segundaDe(iso: string): string {
  const [y, m, d] = iso.split('-').map(Number);
  const dow = new Date(y, m - 1, d).getDay();
  return somarDias(iso, dow === 0 ? -6 : 1 - dow);
}

function nomeDia(iso: string): string {
  const [y, m, d] = iso.split('-').map(Number);
  return ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'][new Date(y, m - 1, d).getDay()];
}

function hora(iso: string): number {
  return Number(iso.slice(11, 13));
}

function minuto(iso: string): number {
  return Number(iso.slice(14, 16));
}

function duracaoMin(i: ItemAgendaDto): number {
  return (new Date(i.fim).getTime() - new Date(i.inicio).getTime()) / 60000;
}

function minutosEntre(a: string, b: string): number {
  if (!a || !b) return 0;
  const [ha, ma] = a.split(':').map(Number);
  const [hb, mb] = b.split(':').map(Number);
  return hb * 60 + mb - (ha * 60 + ma);
}

function classeDe(i: ItemAgendaDto): string {
  if (i.tipo === 'VAGA') return 'appt vaga';
  if (i.tipo === 'BLOQUEIO' || i.tipo === 'AFASTAMENTO') return 'appt block';
  if (i.status === 'FALTOU') return 'appt miss';
  if (i.tipo === 'ENCAIXE') return 'appt encaixe';
  return i.tipoAtendimento === 'PROCEDIMENTO' ? 'appt exam' : 'appt consulta';
}

/**
 * Posiciona os itens do dia na faixa de horas. Itens que se sobrepõem formam um grupo, e só esse grupo divide a
 * largura em faixas — um encaixe sobre uma vaga não estreita o resto do dia.
 */
function posicionar(itens: ItemAgendaDto[], inicioDia: number, fimDia: number): ItemNoDia[] {
  const limiteIni = inicioDia * 60;
  const limiteFim = fimDia * 60;
  const minutos = (iso: string) => hora(iso) * 60 + minuto(iso);
  const medidos = [...itens]
    .sort((a, b) => a.inicio.localeCompare(b.inicio))
    .map((item) => {
      const ini = Math.max(limiteIni, minutos(item.inicio));
      // Item que vai até a meia-noite (afastamento, bloqueio do dia inteiro) termina no fim do dia.
      const fimBruto = item.fim.slice(0, 10) > item.inicio.slice(0, 10) ? 24 * 60 : minutos(item.fim);
      return { item, ini, fim: Math.min(limiteFim, Math.max(fimBruto, ini + 10)) };
    });

  const resultado: ItemNoDia[] = [];
  let grupo: { item: ItemAgendaDto; ini: number; fim: number; faixa: number }[] = [];
  let fimDoGrupo = -1;
  const fecharGrupo = () => {
    const total = Math.max(1, ...grupo.map((g) => g.faixa + 1));
    for (const g of grupo) {
      resultado.push({
        item: g.item,
        topo: ((g.ini - limiteIni) * HORA_PX) / 60,
        altura: Math.max(18, ((g.fim - g.ini) * HORA_PX) / 60 - 2),
        esquerda: (g.faixa * 100) / total,
        largura: 100 / total,
        classe: classeDe(g.item),
      });
    }
    grupo = [];
  };
  for (const m of medidos) {
    if (grupo.length && m.ini >= fimDoGrupo) fecharGrupo();
    const ocupadas = grupo.filter((g) => g.fim > m.ini).map((g) => g.faixa);
    let faixa = 0;
    while (ocupadas.includes(faixa)) faixa++;
    grupo.push({ ...m, faixa });
    fimDoGrupo = Math.max(grupo.length === 1 ? -1 : fimDoGrupo, m.fim);
  }
  if (grupo.length) fecharGrupo();
  return resultado;
}
