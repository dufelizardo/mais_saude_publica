import { HttpErrorResponse } from '@angular/common/http';
import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, catchError, map, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { EquipeResumoDto, FUNCOES_EQUIPE, FuncaoEquipe } from '../../../core/models/equipe';
import {
  AUSENCIAS,
  AusenciaEscalaDto,
  CopiaSemanaResponseDto,
  EscalaSemanaResponseDto,
  LinhaEscalaDto,
  TIPOS_TURNO,
  TipoTurno,
  TurnoEscalaDto,
} from '../../../core/models/escala';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { NIVEIS_DE_GESTAO } from '../../../core/models/rede-unidades';
import { AuthService } from '../../../core/services/auth';
import { EquipeService } from '../../../core/services/equipe';
import { EscalaService } from '../../../core/services/escala';
import { RedeUnidadesService } from '../../../core/services/rede-unidades';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';

type Aba = 'semana' | 'plantoes' | 'ferias';
type Gaveta =
  | { tipo: 'turno'; turno: TurnoEscalaDto | null }
  | { tipo: 'designar'; turno: TurnoEscalaDto }
  | { tipo: 'copiar'; resultado: CopiaSemanaResponseDto | null };

interface Dia {
  iso: string;
  dn: string;
  dt: number;
  hoje: boolean;
  fimDeSemana: boolean;
}

interface Plantao {
  chave: string;
  inicioEm: string;
  fimEm: string;
  tipo: TipoTurno;
  descricao?: string | null;
  turnos: TurnoEscalaDto[];
  vagas: TurnoEscalaDto[];
}

const DIAS_CURTOS = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];
const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

/** Data local em YYYY-MM-DD (sem passar por UTC). */
function isoLocal(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function segundaDe(iso: string): string {
  const d = new Date(iso + 'T12:00:00');
  d.setDate(d.getDate() - ((d.getDay() + 6) % 7));
  return isoLocal(d);
}

function somarDias(iso: string, dias: number): string {
  const d = new Date(iso + 'T12:00:00');
  d.setDate(d.getDate() + dias);
  return isoLocal(d);
}

function diasEntre(de: string, ate: string): number {
  return Math.round((new Date(ate + 'T12:00:00').getTime() - new Date(de + 'T12:00:00').getTime()) / 86400000);
}

/**
 * Escalas (ADR-0106), pelo protótipo `Escalas.html`: indicadores, abas (jornada semanal, plantões, férias e licenças; banco
 * de horas "Em breve"), navegação por semana, a grade de turnos por profissional com as ausências do RH, as vagas abertas,
 * o resumo da semana, os plantões e as férias e licenças, com as gavetas de turno, designação e cópia de semana. O backend é
 * a ADR-0105.
 */
@Component({
  selector: 'app-escalas',
  imports: [ReactiveFormsModule, NgTemplateOutlet, Drawer],
  templateUrl: './escalas.html',
})
export class Escalas {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly escalaService = inject(EscalaService);
  private readonly equipeService = inject(EquipeService);
  private readonly redeService = inject(RedeUnidadesService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);

  protected readonly tipos = TIPOS_TURNO;
  protected readonly listaTipos = Object.keys(TIPOS_TURNO) as TipoTurno[];
  protected readonly ausenciasRotulo = AUSENCIAS;
  protected readonly funcoes = FUNCOES_EQUIPE;
  protected readonly listaFuncoes = Object.keys(FUNCOES_EQUIPE) as FuncaoEquipe[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly unidades = signal<{ uuid: string; nome: string }[]>([]);
  protected readonly unidadeId = signal('');
  protected readonly semana = signal(segundaDe(isoLocal(new Date())));
  protected readonly equipeId = signal('');
  protected readonly equipes = signal<EquipeResumoDto[]>([]);
  protected readonly dados = signal<EscalaSemanaResponseDto | null>(null);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly aba = signal<Aba>('semana');
  protected readonly busca = signal('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly confirmarRemocao = signal(false);
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly turnoForm = this.fb.nonNullable.group({
    profissionalMatricula: [''],
    funcao: ['' as FuncaoEquipe | ''],
    tipo: ['MANHA' as TipoTurno],
    data: [''],
    inicio: ['07:00'],
    fim: ['13:00'],
    equipeId: [''],
    descricao: [''],
  });
  protected readonly designarForm = this.fb.nonNullable.group({ profissionalMatricula: [''], motivo: [''] });
  protected readonly copiarForm = this.fb.nonNullable.group({ origem: [''] });

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────

  protected readonly podeEditar = computed(() => this.pode('ESCALA.GERENCIAR'));

  protected readonly dias = computed<Dia[]>(() => {
    const hoje = isoLocal(new Date());
    return DIAS_CURTOS.map((dn, i) => {
      const iso = somarDias(this.semana(), i);
      return { iso, dn, dt: Number(iso.slice(8, 10)), hoje: iso === hoje, fimDeSemana: i >= 5 };
    });
  });

  protected readonly tituloSemana = computed(() => {
    const ini = this.semana();
    const fim = somarDias(ini, 6);
    const mi = MESES[Number(ini.slice(5, 7)) - 1];
    const mf = MESES[Number(fim.slice(5, 7)) - 1];
    return mi === mf ? `${Number(ini.slice(8))} a ${Number(fim.slice(8))} de ${mf}` : `${Number(ini.slice(8))} de ${mi} a ${Number(fim.slice(8))} de ${mf}`;
  });

  protected readonly ehEstaSemana = computed(() => this.semana() === segundaDe(isoLocal(new Date())));

  protected readonly linhas = computed(() => {
    const q = this.busca().trim().toLowerCase();
    return (this.dados()?.linhas ?? []).filter((l) => !q || `${l.nome} ${l.cargo ?? ''} ${l.matricula}`.toLowerCase().includes(q));
  });

  protected readonly mediaHoras = computed(() => {
    const d = this.dados();
    return d && d.profissionais ? Math.round((d.horasPrevistas / d.profissionais) * 10) / 10 : 0;
  });

  protected readonly cobertura = computed(() => {
    const d = this.dados();
    return d && d.turnos ? Math.round(((d.turnos - d.vagasAbertas) * 100) / d.turnos) : null;
  });

  /** Turnos de plantão da unidade na semana, agrupados por horário, tipo e local (um plantão tem vários profissionais). */
  protected readonly plantoes = computed<Plantao[]>(() => {
    const d = this.dados();
    if (!d) return [];
    const todos = [...d.linhas.flatMap((l) => l.turnos), ...d.vagas].filter((t) => t.unidadeId === d.unidadeId && TIPOS_TURNO[t.tipo].plantao);
    const grupos = new Map<string, Plantao>();
    for (const t of todos) {
      const chave = `${t.inicioEm}|${t.fimEm}|${t.tipo}|${t.descricao ?? ''}`;
      const g = grupos.get(chave) ?? { chave, inicioEm: t.inicioEm, fimEm: t.fimEm, tipo: t.tipo, descricao: t.descricao, turnos: [], vagas: [] };
      (t.vaga ? g.vagas : g.turnos).push(t);
      grupos.set(chave, g);
    }
    return [...grupos.values()].sort((a, b) => a.inicioEm.localeCompare(b.inicioEm));
  });

  /** Barras da linha do tempo dos próximos 30 dias (até três faixas, como no protótipo). */
  protected readonly faixas = computed(() => {
    const hoje = isoLocal(new Date());
    return (this.dados()?.ausencias ?? []).slice(0, 3).map((a, i) => {
      const ini = Math.max(0, diasEntre(hoje, a.inicio));
      const fim = Math.min(30, diasEntre(hoje, a.fim) + 1);
      return { a, classe: `b-${i + 1}`, left: (ini * 100) / 30, width: Math.max(4, ((fim - ini) * 100) / 30) };
    });
  });

  protected readonly marcasDoEixo = computed(() => {
    const hoje = isoLocal(new Date());
    return [0, 6, 12, 18, 24, 30].map((d) => {
      const iso = somarDias(hoje, d);
      return `${iso.slice(8, 10)} ${MESES[Number(iso.slice(5, 7)) - 1]}`;
    });
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    const q = this.route.snapshot.queryParamMap;
    if (q.get('semana')) this.semana.set(segundaDe(q.get('semana')!));
    this.equipeId.set(q.get('equipe') ?? '');
    const pedida = q.get('unidade') ?? '';
    this.listarUnidades().subscribe((lista) => {
      this.unidades.set(lista);
      const escolhida = pedida && lista.some((u) => u.uuid === pedida) ? pedida : (lista[0]?.uuid ?? pedida);
      if (escolhida) this.escolherUnidade(escolhida, false);
    });
  }

  /** As unidades de atendimento da rede; sem acesso à rede, a lista simples de unidades. */
  private listarUnidades(): Observable<{ uuid: string; nome: string }[]> {
    return this.redeService.rede().pipe(
      map((r) => r.rede.filter((u) => u.ativo && !NIVEIS_DE_GESTAO.includes(u.tipo)).map((u) => ({ uuid: u.uuid, nome: u.nome }))),
      catchError(() => this.unidadeSaudeService.listar().pipe(catchError(() => of([])))),
      map((l) => [...l].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))),
    );
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected escolherUnidade(uuid: string, limparEquipe = true): void {
    this.unidadeId.set(uuid);
    if (limparEquipe) this.equipeId.set('');
    this.equipeService.listar(uuid).pipe(catchError(() => of(null))).subscribe((r) => this.equipes.set(r?.lista.filter((e) => e.ativa) ?? []));
    this.carregar();
  }

  protected escolherEquipe(uuid: string): void {
    this.equipeId.set(uuid);
    this.carregar();
  }

  protected navegar(semanas: number): void {
    this.semana.set(semanas === 0 ? segundaDe(isoLocal(new Date())) : somarDias(this.semana(), semanas * 7));
    this.carregar();
  }

  protected carregar(): void {
    if (!this.unidadeId()) return;
    this.router.navigate([], {
      queryParams: { unidade: this.unidadeId(), semana: this.semana(), equipe: this.equipeId() || null },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
    this.carregando.set(true);
    this.erro.set(null);
    this.escalaService.semana(this.unidadeId(), this.semana(), this.equipeId() || undefined).subscribe({
      next: (d) => {
        this.dados.set(d);
        this.carregando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.dados.set(null);
        this.carregando.set(false);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar a escala.');
      },
    });
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────

  protected turnosDoDia(l: LinhaEscalaDto, dia: Dia): TurnoEscalaDto[] {
    return l.turnos.filter((t) => t.data === dia.iso);
  }

  protected ausenciaNoDia(l: LinhaEscalaDto, dia: Dia): AusenciaEscalaDto | undefined {
    return l.ausencias.find((a) => a.inicio <= dia.iso && dia.iso <= a.fim);
  }

  protected vagasDoDia(dia: Dia): TurnoEscalaDto[] {
    return (this.dados()?.vagas ?? []).filter((t) => t.data === dia.iso);
  }

  protected hora(iso: string): string {
    const h = iso.slice(11, 13);
    const m = iso.slice(14, 16);
    return m === '00' ? `${h}h` : `${h}h${m}`;
  }

  protected faixa(t: { inicioEm: string; fimEm: string }): string {
    return `${this.hora(t.inicioEm)}–${this.hora(t.fimEm)}`;
  }

  protected rotuloHoras(h: number): string {
    return `${String(h).replace('.', ',')}h`;
  }

  protected iniciais(nome: string | null | undefined): string {
    return (nome ?? '?')
      .replace(/^(Dr|Dra|Enf|Téc|ACS)\.?\s+/i, '')
      .split(/\s+/)
      .filter((p) => p.length > 2)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    return `${iso.slice(8, 10)}/${iso.slice(5, 7)}`;
  }

  protected diaMes(iso: string): { d: string; m: string; hoje: boolean } {
    return { d: iso.slice(8, 10), m: MESES[Number(iso.slice(5, 7)) - 1], hoje: iso.slice(0, 10) === isoLocal(new Date()) };
  }

  protected duracao(a: AusenciaEscalaDto): number {
    return diasEntre(a.inicio, a.fim) + 1;
  }

  protected outraUnidade(t: TurnoEscalaDto): boolean {
    return t.unidadeId !== this.dados()?.unidadeId;
  }

  /** Turno ainda não terminado e da unidade da tela: pode ser aberto para edição. */
  protected podeMexer(t: TurnoEscalaDto): boolean {
    return this.podeEditar() && !this.outraUnidade(t) && new Date(t.fimEm).getTime() > Date.now();
  }

  protected podeCriarNoDia(dia: Dia): boolean {
    return this.podeEditar() && dia.iso >= isoLocal(new Date());
  }

  protected erroCampo(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    if (g.tipo === 'turno') return g.turno ? (g.turno.vaga ? 'Vaga aberta' : 'Editar turno') : 'Novo turno';
    if (g.tipo === 'designar') return g.turno.vaga ? 'Designar profissional' : 'Trocar profissional';
    return 'Copiar semana';
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.confirmarRemocao.set(false);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirNovo(matricula = '', data = ''): void {
    const tipo: TipoTurno = 'MANHA';
    this.turnoForm.reset({
      profissionalMatricula: matricula,
      funcao: '',
      tipo,
      data: data || (this.semana() >= isoLocal(new Date()) ? this.semana() : isoLocal(new Date())),
      inicio: TIPOS_TURNO[tipo].inicio,
      fim: TIPOS_TURNO[tipo].fim,
      equipeId: this.equipeId(),
      descricao: '',
    });
    this.abrir({ tipo: 'turno', turno: null });
  }

  protected abrirTurno(t: TurnoEscalaDto): void {
    if (!this.podeMexer(t)) return;
    this.turnoForm.reset({
      profissionalMatricula: t.profissionalMatricula ?? '',
      funcao: t.funcao ?? '',
      tipo: t.tipo,
      data: t.data,
      inicio: t.inicioEm.slice(11, 16),
      fim: t.fimEm.slice(11, 16),
      equipeId: t.equipeId ?? '',
      descricao: t.descricao ?? '',
    });
    this.abrir({ tipo: 'turno', turno: t });
  }

  /** Ao trocar o tipo, sugere o horário dele. */
  protected aoTrocarTipo(): void {
    const t = TIPOS_TURNO[this.turnoForm.controls.tipo.value];
    this.turnoForm.patchValue({ inicio: t.inicio, fim: t.fim });
  }

  protected abrirDesignar(t: TurnoEscalaDto): void {
    if (!this.podeMexer(t)) return;
    this.designarForm.reset({ profissionalMatricula: '', motivo: '' });
    this.abrir({ tipo: 'designar', turno: t });
  }

  protected abrirCopiar(): void {
    this.copiarForm.reset({ origem: somarDias(this.semana(), -7) });
    this.abrir({ tipo: 'copiar', resultado: null });
  }

  /** Quem pode entrar no turno: as pessoas da semana, menos quem já está nele. */
  protected candidatos(atual?: string | null): LinhaEscalaDto[] {
    return (this.dados()?.linhas ?? []).filter((l) => l.matricula !== atual);
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    if (g.tipo === 'turno') this.salvarTurno(g.turno);
    else if (g.tipo === 'designar') this.salvarDesignacao(g.turno);
    else this.salvarCopia();
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

  private salvarTurno(t: TurnoEscalaDto | null): void {
    const v = this.turnoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.profissionalMatricula && !v.funcao) erros['funcao'] = 'A vaga aberta precisa da função que falta.';
    if (!v.data) erros['data'] = 'Informe a data.';
    else if (!t && v.data < isoLocal(new Date())) erros['data'] = 'Não se cria turno em dia que já passou.';
    if (!v.inicio) erros['inicio'] = 'Informe o início.';
    if (!v.fim) erros['fim'] = 'Informe o fim.';
    if (!this.validar(erros)) return;
    const dto = {
      unidadeId: this.unidadeId(),
      profissionalMatricula: v.profissionalMatricula || undefined,
      funcao: v.funcao || undefined,
      tipo: v.tipo,
      data: v.data,
      inicio: v.inicio,
      fim: v.fim,
      equipeId: v.equipeId || undefined,
      descricao: v.descricao.trim() || undefined,
    };
    const nome = this.dados()?.linhas.find((l) => l.matricula === v.profissionalMatricula)?.nome ?? 'Vaga aberta';
    this.enviar(t ? this.escalaService.atualizar(t.uuid, dto) : this.escalaService.criar(dto), t ? 'Turno atualizado' : 'Turno criado',
      `${nome} · ${TIPOS_TURNO[v.tipo].rotulo} · ${this.data(v.data)}`);
  }

  private salvarDesignacao(t: TurnoEscalaDto): void {
    const v = this.designarForm.getRawValue();
    if (!this.validar(v.profissionalMatricula ? {} : { profissionalMatricula: 'Escolha o profissional.' })) return;
    const nome = this.dados()?.linhas.find((l) => l.matricula === v.profissionalMatricula)?.nome ?? v.profissionalMatricula;
    this.enviar(this.escalaService.designar(t.uuid, v.profissionalMatricula, v.motivo.trim() || undefined),
      t.vaga ? 'Vaga preenchida' : 'Profissional trocado', `${nome} · ${this.faixa(t)} · ${this.data(t.data)}`);
  }

  private salvarCopia(): void {
    const v = this.copiarForm.getRawValue();
    if (!this.validar(v.origem ? {} : { origem: 'Escolha a semana de origem.' })) return;
    if (segundaDe(v.origem) === this.semana()) {
      this.validar({ origem: 'Escolha outra semana; o destino é a semana da tela.' });
      return;
    }
    this.submitting.set(true);
    this.escalaService.copiarSemana(this.unidadeId(), v.origem, this.semana()).subscribe({
      next: (r) => {
        this.submitting.set(false);
        this.gaveta.set({ tipo: 'copiar', resultado: r });
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected remover(t: TurnoEscalaDto): void {
    if (!this.confirmarRemocao()) {
      this.confirmarRemocao.set(true);
      return;
    }
    this.submitting.set(true);
    this.escalaService.remover(t.uuid).subscribe({
      next: () => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast('Turno removido', `${t.profissionalNome ?? 'Vaga aberta'} · ${this.faixa(t)} · ${this.data(t.data)}`);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private enviar(req: Observable<TurnoEscalaDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: (t) => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(titulo, t.alertas.length ? `${detalhe}. Atenção: ${t.alertas.join(' ')}` : detalhe);
        this.carregar();
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
    this.toastTimer = setTimeout(() => this.toast.set(null), 6000);
  }
}
