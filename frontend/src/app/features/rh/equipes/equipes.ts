import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import {
  EquipeResponseDto,
  EquipeResumoDto,
  EquipesResponseDto,
  FUNCOES_EQUIPE,
  FuncaoEquipe,
  GRUPOS_COMPOSICAO,
  MembroEquipeDto,
  TIPOS_EQUIPE,
  TipoEquipe,
} from '../../../core/models/equipe';
import { ErrorResponseDto, QuadroProfissionalResponseDto } from '../../../core/models/profissional';
import { DIAS_SEMANA } from '../../../core/models/rede-unidades';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { EquipeService } from '../../../core/services/equipe';
import { ProfissionalService } from '../../../core/services/profissional';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta =
  | { tipo: 'equipe'; equipe: EquipeResponseDto | null }
  | { tipo: 'membro'; equipe: EquipeResponseDto | null; matricula: string }
  | { tipo: 'saida'; equipe: EquipeResponseDto; membro: MembroEquipeDto };

/**
 * Equipes (ADR-0104), pelo protótipo `Equipes.html`: indicadores, abas por tipo com contagem, a grade de cartões de equipe
 * e o detalhe da equipe escolhida (membros, microáreas, reunião, apoio), com as gavetas de equipe, membro e saída. O
 * território, os programas e os indicadores assistenciais ficam "Em breve". O backend é a ADR-0103.
 */
@Component({
  selector: 'app-equipes',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './equipes.html',
})
export class Equipes {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly equipeService = inject(EquipeService);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);

  protected readonly tipos = TIPOS_EQUIPE;
  protected readonly listaTipos = Object.keys(TIPOS_EQUIPE) as TipoEquipe[];
  protected readonly funcoes = FUNCOES_EQUIPE;
  protected readonly listaFuncoes = Object.keys(FUNCOES_EQUIPE) as FuncaoEquipe[];
  protected readonly dias = DIAS_SEMANA;

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly dados = signal<EquipesResponseDto | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly quadro = signal<QuadroProfissionalResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly selecionadaId = signal<string | null>(null);
  protected readonly detalhe = signal<EquipeResponseDto | null>(null);

  protected readonly tipoAba = signal<TipoEquipe | ''>('');
  protected readonly busca = signal('');
  protected readonly unidadeFiltro = signal('');
  protected readonly situacaoFiltro = signal<'ATIVAS' | 'INCOMPLETAS' | 'INATIVAS' | ''>('ATIVAS');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly equipeForm = this.fb.nonNullable.group({
    unidadeId: [''],
    tipo: ['ESF' as TipoEquipe],
    nome: [''],
    ine: [''],
    microareas: [''],
    coordenadorMatricula: [''],
    reuniaoDia: [''],
    reuniaoInicio: [''],
    reuniaoFim: [''],
    reuniaoLocal: [''],
    ativa: [true],
  });
  protected readonly apoiadasMarcadas = signal<Set<string>>(new Set());
  protected readonly membroForm = this.fb.nonNullable.group({
    equipeId: [''],
    profissionalMatricula: [''],
    funcao: ['MEDICO' as FuncaoEquipe],
    microarea: [''],
    inicio: [''],
  });
  protected readonly saidaForm = this.fb.nonNullable.group({ motivo: [''], fim: [''] });

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────

  protected readonly podeEditar = computed(() => this.pode('EQUIPE.GERENCIAR'));

  protected readonly unidadesComEquipe = computed(() => {
    const ids = new Set((this.dados()?.lista ?? []).map((e) => e.unidadeId));
    return this.unidades().filter((u) => ids.has(u.uuid));
  });

  protected readonly contagemPorTipo = computed(() => {
    const mapa = new Map<TipoEquipe | '', number>();
    const lista = this.semAbaFiltrada();
    mapa.set('', lista.length);
    for (const e of lista) mapa.set(e.tipo, (mapa.get(e.tipo) ?? 0) + 1);
    return mapa;
  });

  /** Filtros sem a aba de tipo: base da contagem das abas. */
  private readonly semAbaFiltrada = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const s = this.situacaoFiltro();
    return (this.dados()?.lista ?? [])
      .filter((e) => !this.unidadeFiltro() || e.unidadeId === this.unidadeFiltro())
      .filter((e) => (s === 'ATIVAS' ? e.ativa : s === 'INATIVAS' ? !e.ativa : s === 'INCOMPLETAS' ? e.ativa && !e.completa : true))
      .filter((e) => !q || `${e.nome} ${e.unidadeNome} ${e.ine ?? ''} ${e.nomesMembros.join(' ')}`.toLowerCase().includes(q));
  });

  protected readonly filtradas = computed(() => this.semAbaFiltrada().filter((e) => !this.tipoAba() || e.tipo === this.tipoAba()));

  /** Profissionais com lotação vigente na unidade da equipe (o membro precisa estar lotado nela). */
  /** Método, não computed: a equipe escolhida vem do formulário. */
  protected lotadosNaUnidade(): QuadroProfissionalResponseDto[] {
    const g = this.gaveta();
    const unidade = g?.tipo === 'membro' ? (g.equipe?.resumo.unidadeId ?? this.unidadeDaEquipeEscolhida()) : '';
    return this.quadro()
      .filter((q) => q.lotacao && (!unidade || q.lotacao.unidadeUuid === unidade))
      .sort((a, b) => a.profissional.nome.localeCompare(b.profissional.nome, 'pt-BR'));
  }

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.unidadeSaudeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.profissionalService.quadro().pipe(catchError(() => of([]))).subscribe((q) => this.quadro.set(q));
    const q = this.route.snapshot.queryParamMap;
    this.selecionadaId.set(q.get('equipe'));
    this.unidadeFiltro.set(q.get('unidade') ?? '');
    this.carregar(() => {
      // "Atribuir equipe" em Profissionais: abre a inclusão já com o profissional (ADR-0104).
      if (q.get('acao') === 'membro' && q.get('matricula')) this.abrirMembro(null, q.get('matricula')!);
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected carregar(depois?: () => void): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.equipeService.listar().subscribe({
      next: (d) => {
        this.dados.set(d);
        this.carregando.set(false);
        const atual = this.selecionadaId();
        if (atual && d.lista.some((e) => e.uuid === atual)) this.selecionar(atual, false);
        else if (this.filtradas().length) this.selecionar(this.filtradas()[0].uuid, false);
        depois?.();
      },
      error: (e: HttpErrorResponse) => {
        this.carregando.set(false);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar as equipes.');
      },
    });
  }

  protected selecionar(uuid: string, atualizarUrl = true): void {
    this.selecionadaId.set(uuid);
    if (atualizarUrl) this.router.navigate([], { queryParams: { equipe: uuid }, queryParamsHandling: 'merge', replaceUrl: true });
    this.equipeService.buscar(uuid).subscribe({ next: (d) => this.detalhe.set(d), error: () => this.detalhe.set(null) });
  }

  protected teclaNoCartao(event: KeyboardEvent, uuid: string): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.selecionar(uuid);
    }
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────

  protected iniciais(nome: string): string {
    return nome
      .replace(/^(Dr|Dra|Enf|Téc|ACS)\.?\s+/i, '')
      .split(/\s+/)
      .filter((p) => p.length > 2)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  /** Percentual da composição mínima atendida (100% para tipos sem composição definida). */
  protected composicao(e: EquipeResumoDto): number {
    const grupos = GRUPOS_COMPOSICAO[e.tipo];
    return grupos ? Math.round(((grupos - e.faltando.length) * 100) / grupos) : 100;
  }

  protected faltando(e: EquipeResumoDto): string {
    return e.faltando.map((f) => f.split(' ou ').map((x) => this.funcoes[x as FuncaoEquipe] ?? x).join(' ou ')).join(', ');
  }

  protected microareas(e: EquipeResponseDto): { codigo: string; acs: MembroEquipeDto[] }[] {
    const codigos = (e.resumo.microareas ?? '').split(',').map((c) => c.trim()).filter((c) => c);
    const acs = e.membros.filter((m) => m.funcao === 'ACS');
    const extras = acs.map((m) => m.microarea).filter((c): c is string => !!c && !codigos.includes(c));
    return [...codigos, ...new Set(extras)].map((c) => ({ codigo: c, acs: acs.filter((m) => m.microarea === c) }));
  }

  /** Primeiro nome dos ACS da microárea, para o quadrinho do território. */
  protected nomesAcs(acs: MembroEquipeDto[]): string {
    return acs.map((m) => m.nome.split(/\s+/)[0]).join(', ');
  }

  protected situacaoMembro(m: MembroEquipeDto): string {
    if (m.afastamento) return `${m.afastamento.replace(/_/g, ' ').toLowerCase()}${m.afastadoAte ? ' até ' + this.data(m.afastadoAte) : ''}`;
    return 'Ativo';
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  protected rotuloDia(dia: string | null | undefined): string {
    return DIAS_SEMANA.find((d) => d.id === dia)?.rotulo ?? '—';
  }

  protected rotuloFuncoesClinicas(e: EquipeResumoDto): string {
    return e.tipo === 'ESB' ? 'Dentista · Téc./Aux. saúde bucal' : e.tipo === 'EMULTI' || e.tipo === 'CAPS_MULTI' ? 'Profissionais da equipe' : 'Médico · Enfermagem · outros';
  }

  protected erroCampo(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    if (g.tipo === 'equipe') return g.equipe ? 'Editar equipe' : 'Nova equipe';
    if (g.tipo === 'membro') return g.equipe ? `Adicionar membro · ${g.equipe.resumo.nome}` : 'Incluir em equipe';
    return 'Registrar saída';
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirEquipe(e: EquipeResponseDto | null = null): void {
    this.equipeForm.reset({
      unidadeId: e?.resumo.unidadeId ?? this.unidadeFiltro(),
      tipo: e?.resumo.tipo ?? (this.tipoAba() || 'ESF'),
      nome: e?.resumo.nome ?? '',
      ine: e?.resumo.ine ?? '',
      microareas: e?.resumo.microareas ?? '',
      coordenadorMatricula: e?.coordenadorMatricula ?? '',
      reuniaoDia: e?.reuniaoDia ?? '',
      reuniaoInicio: e?.reuniaoInicio?.slice(0, 5) ?? '',
      reuniaoFim: e?.reuniaoFim?.slice(0, 5) ?? '',
      reuniaoLocal: e?.reuniaoLocal ?? '',
      ativa: e?.resumo.ativa ?? true,
    });
    this.apoiadasMarcadas.set(new Set(e?.apoiadas.map((a) => a.uuid) ?? []));
    this.abrir({ tipo: 'equipe', equipe: e });
  }

  protected alternarApoiada(uuid: string): void {
    const s = new Set(this.apoiadasMarcadas());
    if (s.has(uuid)) s.delete(uuid);
    else s.add(uuid);
    this.apoiadasMarcadas.set(s);
  }

  /** Equipes que a eMulti pode apoiar: eSF e eAP. */
  protected apoiaveis(): EquipeResumoDto[] {
    return (this.dados()?.lista ?? []).filter((e) => (e.tipo === 'ESF' || e.tipo === 'EAB') && e.ativa);
  }

  /** Para a gaveta "Incluir em equipe" vinda de Profissionais: as equipes da unidade de lotação do profissional. */
  protected equipesParaIncluir(): EquipeResumoDto[] {
    const g = this.gaveta();
    if (g?.tipo !== 'membro' || g.equipe) return [];
    const lotacao = this.quadro().find((q) => q.profissional.matricula === this.membroForm.controls.profissionalMatricula.value)?.lotacao;
    return (this.dados()?.lista ?? []).filter((e) => e.ativa && (!lotacao || e.unidadeId === lotacao.unidadeUuid));
  }

  private unidadeDaEquipeEscolhida(): string {
    const id = this.membroForm.controls.equipeId.value;
    return (this.dados()?.lista ?? []).find((e) => e.uuid === id)?.unidadeId ?? '';
  }

  protected abrirMembro(e: EquipeResponseDto | null, matricula = ''): void {
    this.membroForm.reset({ equipeId: e?.resumo.uuid ?? '', profissionalMatricula: matricula, funcao: 'MEDICO', microarea: '', inicio: '' });
    this.abrir({ tipo: 'membro', equipe: e, matricula });
  }

  protected abrirSaida(e: EquipeResponseDto, m: MembroEquipeDto): void {
    this.saidaForm.reset({ motivo: '', fim: '' });
    this.abrir({ tipo: 'saida', equipe: e, membro: m });
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    if (g.tipo === 'equipe') this.salvarEquipe(g.equipe);
    else if (g.tipo === 'membro') this.salvarMembro(g.equipe);
    else this.salvarSaida(g.equipe, g.membro);
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

  private salvarEquipe(e: EquipeResponseDto | null): void {
    const v = this.equipeForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!e && !v.unidadeId) erros['unidadeId'] = 'Selecione a unidade.';
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome da equipe.';
    if (v.ine.trim() && !/^\d{10}$/.test(v.ine.trim())) erros['ine'] = 'O INE tem 10 dígitos.';
    if (v.reuniaoInicio && v.reuniaoFim && v.reuniaoFim <= v.reuniaoInicio) erros['reuniaoFim'] = 'A reunião precisa terminar depois de começar.';
    if (!this.validar(erros)) return;
    const dto = {
      unidadeId: e?.resumo.unidadeId ?? v.unidadeId,
      tipo: e?.resumo.tipo ?? v.tipo,
      nome: v.nome.trim(),
      ine: v.ine.trim() || undefined,
      ativa: v.ativa,
      microareas: v.microareas.trim() || undefined,
      coordenadorMatricula: v.coordenadorMatricula || undefined,
      reuniaoDia: v.reuniaoDia || undefined,
      reuniaoInicio: v.reuniaoInicio || undefined,
      reuniaoFim: v.reuniaoFim || undefined,
      reuniaoLocal: v.reuniaoLocal.trim() || undefined,
      apoiadasIds: (e?.resumo.tipo ?? v.tipo) === 'EMULTI' ? [...this.apoiadasMarcadas()] : [],
    };
    this.enviar(e ? this.equipeService.atualizar(e.resumo.uuid, dto) : this.equipeService.criar(dto), e ? 'Equipe atualizada' : 'Equipe cadastrada', dto.nome);
  }

  private salvarMembro(e: EquipeResponseDto | null): void {
    const v = this.membroForm.getRawValue();
    const erros: Record<string, string> = {};
    const equipeId = e?.resumo.uuid ?? v.equipeId;
    if (!equipeId) erros['equipeId'] = 'Escolha a equipe.';
    if (!v.profissionalMatricula) erros['profissionalMatricula'] = 'Escolha o profissional.';
    if (!this.validar(erros)) return;
    const nome = this.quadro().find((q) => q.profissional.matricula === v.profissionalMatricula)?.profissional.nome ?? v.profissionalMatricula;
    this.enviar(
      this.equipeService.adicionarMembro(equipeId, {
        profissionalMatricula: v.profissionalMatricula,
        funcao: v.funcao,
        microarea: v.microarea.trim() || undefined,
        inicio: v.inicio || undefined,
      }),
      'Membro incluído',
      `${nome} · ${this.funcoes[v.funcao]}`,
    );
  }

  private salvarSaida(e: EquipeResponseDto, m: MembroEquipeDto): void {
    const v = this.saidaForm.getRawValue();
    if (!this.validar(v.motivo.trim() ? {} : { motivoSaida: 'Informe o motivo da saída.' })) return;
    this.enviar(this.equipeService.registrarSaida(m.uuid, v.motivo.trim(), v.fim || undefined), 'Saída registrada', `${m.nome} · ${e.resumo.nome}`);
  }

  private enviar(req: Observable<EquipeResponseDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: (d) => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(titulo, detalhe);
        this.selecionadaId.set(d.resumo.uuid);
        this.carregar();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((error.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}
