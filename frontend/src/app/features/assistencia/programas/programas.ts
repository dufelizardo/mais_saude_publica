import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { EquipeResumoDto } from '../../../core/models/equipe';
import { ErrorResponseDto, ProfissionalResponseDto } from '../../../core/models/profissional';
import {
  ABRANGENCIAS,
  AbrangenciaPrograma,
  CORES_CATEGORIA,
  CategoriaProgramaDto,
  PERIODICIDADES,
  PeriodicidadeAcompanhamento,
  ProgramaDto,
  ProgramaRequestDto,
  ProgramaResumoDto,
  SITUACOES_PROGRAMA,
  SexoPublico,
  SituacaoPrograma,
} from '../../../core/models/programa';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { EquipeService } from '../../../core/services/equipe';
import { ProfissionalService } from '../../../core/services/profissional';
import { ProgramaService } from '../../../core/services/programa';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta =
  | { tipo: 'programa'; item: ProgramaDto | null }
  | { tipo: 'participacao'; programa: ProgramaDto }
  | { tipo: 'encerrar'; programa: ProgramaDto; participacaoId: string; nome: string }
  | { tipo: 'categorias' };

/**
 * Programas de Saúde (ADR-0118), pelo protótipo `Programas.html`: indicadores, pílulas por categoria, a grade de cartões e
 * o detalhe do programa escolhido, com o público-alvo e as unidades e equipes que o oferecem. O catálogo não tem dado de
 * paciente: inscritos, faltosos e os indicadores clínicos ficam "Em breve" (P2 e P6).
 */
@Component({
  selector: 'app-programas',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './programas.html',
})
export class Programas {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly service = inject(ProgramaService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly equipeService = inject(EquipeService);

  protected readonly abrangencias = ABRANGENCIAS;
  protected readonly listaAbrangencias = Object.keys(ABRANGENCIAS) as AbrangenciaPrograma[];
  protected readonly situacoes = SITUACOES_PROGRAMA;
  protected readonly listaSituacoes = Object.keys(SITUACOES_PROGRAMA) as SituacaoPrograma[];
  protected readonly periodicidades = PERIODICIDADES;
  protected readonly listaPeriodicidades = Object.keys(PERIODICIDADES) as PeriodicidadeAcompanhamento[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly categorias = signal<CategoriaProgramaDto[]>([]);
  protected readonly programas = signal<ProgramaResumoDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly profissionais = signal<ProfissionalResponseDto[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  protected readonly busca = signal('');
  protected readonly categoriaFiltro = signal('');
  protected readonly unidadeFiltro = signal('');
  protected readonly selecionadoId = signal<string | null>(null);
  protected readonly detalhe = signal<ProgramaDto | null>(null);
  protected readonly mostrarEncerradas = signal(false);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly equipesDaUnidade = signal<EquipeResumoDto[]>([]);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;
  /** Categoria em edição na gaveta de categorias (null: nova). */
  protected readonly categoriaEditando = signal<CategoriaProgramaDto | null>(null);

  protected readonly programaForm = this.fb.nonNullable.group({
    codigo: [''],
    nome: [''],
    descricao: [''],
    categoriaId: [''],
    abrangencia: ['MUNICIPAL' as AbrangenciaPrograma],
    situacao: ['ATIVO' as SituacaoPrograma],
    inicio: [''],
    fim: [''],
    coordenadorMatricula: [''],
    periodicidade: ['SEM_PERIODICIDADE' as PeriodicidadeAcompanhamento],
    sensivel: [false],
    idadeMinima: [''],
    idadeMaxima: [''],
    sexo: ['' as SexoPublico | ''],
    gestante: [false],
    condicoes: [''],
  });
  protected readonly participacaoForm = this.fb.nonNullable.group({ unidadeId: [''], equipeId: [''], inicio: [''] });
  protected readonly encerrarForm = this.fb.nonNullable.group({ motivo: [''], fim: [''] });
  protected readonly categoriaForm = this.fb.nonNullable.group({ nome: [''], ordem: [''], ativa: [true] });

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────

  protected readonly podeGerenciar = computed(() => this.pode('PROGRAMA.GERENCIAR'));

  protected readonly filtrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const cat = this.categoriaFiltro();
    return this.programas()
      .filter((p) => !cat || p.categoriaId === cat)
      .filter((p) => !q || `${p.nome} ${p.codigo} ${p.categoriaNome} ${p.descricao ?? ''}`.toLowerCase().includes(q));
  });

  protected readonly indicadores = computed(() => {
    const ps = this.programas();
    const ativos = ps.filter((p) => p.situacao === 'ATIVO');
    return {
      ativos: ativos.length,
      total: ps.length,
      categorias: this.categorias().filter((c) => c.ativa).length,
      comUnidades: ativos.filter((p) => p.unidades > 0).length,
      sensiveis: ps.filter((p) => p.sensivel).length,
    };
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.selecionadoId.set(this.route.snapshot.queryParamMap.get('programa'));
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.profissionalService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.profissionais.set(p.filter((x) => x.ativo !== false)));
    this.carregar();
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    forkJoin({
      categorias: this.service.categorias(),
      programas: this.service.listar({ unidadeId: this.unidadeFiltro() || undefined }),
    }).subscribe({
      next: (r) => {
        this.categorias.set(r.categorias);
        this.programas.set(r.programas);
        this.carregando.set(false);
        const atual = this.selecionadoId();
        if (atual && r.programas.some((p) => p.uuid === atual)) this.selecionar(atual, false);
        else if (this.filtrados().length) this.selecionar(this.filtrados()[0].uuid, false);
        else this.detalhe.set(null);
      },
      error: (e: HttpErrorResponse) => {
        this.carregando.set(false);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar os programas.');
      },
    });
  }

  protected filtrarUnidade(uuid: string): void {
    this.unidadeFiltro.set(uuid);
    this.carregar();
  }

  protected selecionar(uuid: string, atualizarUrl = true): void {
    this.selecionadoId.set(uuid);
    this.mostrarEncerradas.set(false);
    if (atualizarUrl) this.router.navigate([], { queryParams: { programa: uuid }, queryParamsHandling: 'merge', replaceUrl: true });
    this.service.buscar(uuid).subscribe({ next: (d) => this.detalhe.set(d), error: () => this.detalhe.set(null) });
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────

  protected corDaCategoria(categoriaId: string): string {
    const i = this.categorias().findIndex((c) => c.uuid === categoriaId);
    return 'pg__icon ' + CORES_CATEGORIA[(i < 0 ? 0 : i) % CORES_CATEGORIA.length];
  }

  protected sigla(nome: string): string {
    return nome
      .split(/[\s-]+/)
      .filter((p) => p.length > 2 || /^[A-Z]/.test(p))
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  protected iniciais(nome: string): string {
    return nome
      .split(/\s+/)
      .filter((p) => p.length > 2 && !p.endsWith('.'))
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  protected erroCampo(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    if (g.tipo === 'programa') return g.item ? 'Editar programa' : 'Novo programa';
    if (g.tipo === 'participacao') return 'Incluir unidade participante';
    if (g.tipo === 'encerrar') return 'Encerrar participação';
    return 'Categorias de programas';
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirPrograma(item: ProgramaDto | null = null): void {
    const r = item?.resumo;
    this.programaForm.reset({
      codigo: r?.codigo ?? '',
      nome: r?.nome ?? '',
      descricao: r?.descricao ?? '',
      categoriaId: r?.categoriaId ?? (this.categoriaFiltro() || (this.categorias().find((c) => c.ativa)?.uuid ?? '')),
      abrangencia: r?.abrangencia ?? 'MUNICIPAL',
      situacao: r?.situacao ?? 'ATIVO',
      inicio: r?.inicio ?? '',
      fim: r?.fim ?? '',
      coordenadorMatricula: r?.coordenadorMatricula ?? '',
      periodicidade: r?.periodicidade ?? 'SEM_PERIODICIDADE',
      sensivel: r?.sensivel ?? false,
      idadeMinima: r?.idadeMinima != null ? String(r.idadeMinima) : '',
      idadeMaxima: r?.idadeMaxima != null ? String(r.idadeMaxima) : '',
      sexo: r?.sexo ?? '',
      gestante: r?.gestante ?? false,
      condicoes: (r?.condicoes ?? []).join(', '),
    });
    this.abrir({ tipo: 'programa', item });
  }

  protected abrirParticipacao(programa: ProgramaDto): void {
    this.participacaoForm.reset({ unidadeId: '', equipeId: '', inicio: '' });
    this.equipesDaUnidade.set([]);
    this.abrir({ tipo: 'participacao', programa });
  }

  protected escolherUnidade(uuid: string): void {
    this.participacaoForm.patchValue({ unidadeId: uuid, equipeId: '' });
    this.equipesDaUnidade.set([]);
    if (!uuid) return;
    this.equipeService.listar(uuid).pipe(catchError(() => of(null))).subscribe((r) => this.equipesDaUnidade.set(r?.lista.filter((e) => e.ativa) ?? []));
  }

  protected abrirEncerrar(programa: ProgramaDto, participacaoId: string, nome: string): void {
    this.encerrarForm.reset({ motivo: '', fim: '' });
    this.abrir({ tipo: 'encerrar', programa, participacaoId, nome });
  }

  protected abrirCategorias(): void {
    this.editarCategoria(null);
    this.abrir({ tipo: 'categorias' });
  }

  protected editarCategoria(c: CategoriaProgramaDto | null): void {
    this.categoriaEditando.set(c);
    this.categoriaForm.reset({ nome: c?.nome ?? '', ordem: c ? String(c.ordem) : '', ativa: c?.ativa ?? true });
    this.erros.set({});
    this.erroApi.set(null);
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    if (g.tipo === 'programa') this.salvarPrograma(g.item);
    else if (g.tipo === 'participacao') this.salvarParticipacao(g.programa);
    else if (g.tipo === 'encerrar') this.salvarEncerramento(g);
    else this.salvarCategoria();
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

  private salvarPrograma(item: ProgramaDto | null): void {
    const v = this.programaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.codigo.trim()) erros['codigo'] = 'Informe o código.';
    else if (!/^[A-Za-z0-9_]+$/.test(v.codigo.trim())) erros['codigo'] = 'Use só letras, números e sublinhado (ex.: HIPERDIA).';
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome.';
    if (!v.categoriaId) erros['categoriaId'] = 'Escolha a categoria.';
    if (v.situacao === 'ENCERRADO' && !v.fim) erros['fim'] = 'Informe o fim do programa encerrado.';
    if (v.inicio && v.fim && v.fim < v.inicio) erros['fim'] = 'O fim não pode ser antes do início.';
    // input type="number" devolve número (ou null) no formulário reativo, não string.
    const min = String(v.idadeMinima ?? '').trim() === '' ? null : Number(v.idadeMinima);
    const max = String(v.idadeMaxima ?? '').trim() === '' ? null : Number(v.idadeMaxima);
    if (min != null && (!Number.isInteger(min) || min < 0 || min > 130)) erros['idadeMinima'] = 'Idade de 0 a 130 anos.';
    if (max != null && (!Number.isInteger(max) || max < 0 || max > 130)) erros['idadeMaxima'] = 'Idade de 0 a 130 anos.';
    if (min != null && max != null && max < min) erros['idadeMaxima'] = 'A idade máxima não pode ser menor que a mínima.';
    const condicoes = v.condicoes.split(/[\s,;]+/).map((c) => c.trim().toUpperCase()).filter((c) => c);
    const invalida = condicoes.find((c) => !/^[A-Z][0-9]{2}(\.[0-9A-Z]{1,2})?$/.test(c));
    if (invalida) erros['condicoes'] = `Código inválido: ${invalida} (use CID-10 ou CIAP-2, ex.: I10, E11, T90).`;
    if (!this.validar(erros)) return;
    const dto: ProgramaRequestDto = {
      codigo: v.codigo.trim(),
      nome: v.nome.trim(),
      descricao: v.descricao.trim() || null,
      categoriaId: v.categoriaId,
      abrangencia: v.abrangencia,
      situacao: v.situacao,
      inicio: v.inicio || null,
      fim: v.fim || null,
      coordenadorMatricula: v.coordenadorMatricula.trim() || null,
      periodicidade: v.periodicidade,
      sensivel: v.sensivel,
      idadeMinima: min,
      idadeMaxima: max,
      sexo: v.sexo || null,
      gestante: v.gestante,
      condicoes,
    };
    this.enviar(item ? this.service.atualizar(item.resumo.uuid, dto) : this.service.criar(dto),
      item ? 'Programa atualizado' : 'Programa cadastrado', dto.nome, true);
  }

  private salvarParticipacao(programa: ProgramaDto): void {
    const v = this.participacaoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.unidadeId) erros['unidadeId'] = 'Escolha a unidade.';
    if (v.inicio && v.inicio > this.hojeIso()) erros['inicioParticipacao'] = 'O início não pode ser no futuro.';
    if (!this.validar(erros)) return;
    const nome = this.unidades().find((u) => u.uuid === v.unidadeId)?.nome ?? '';
    this.enviar(this.service.incluirParticipacao(programa.resumo.uuid, v.unidadeId, v.equipeId, v.inicio),
      'Unidade incluída', `${programa.resumo.nome} · ${nome}`, false);
  }

  private salvarEncerramento(g: Extract<Gaveta, { tipo: 'encerrar' }>): void {
    const v = this.encerrarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.motivo.trim()) erros['motivoFim'] = 'Informe o motivo.';
    if (v.fim && v.fim > this.hojeIso()) erros['fimParticipacao'] = 'O fim não pode ser no futuro.';
    if (!this.validar(erros)) return;
    this.enviar(this.service.encerrarParticipacao(g.programa.resumo.uuid, g.participacaoId, v.motivo.trim(), v.fim),
      'Participação encerrada', `${g.programa.resumo.nome} · ${g.nome}`, false);
  }

  private salvarCategoria(): void {
    const v = this.categoriaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nomeCategoria'] = 'Informe o nome.';
    const ordem = String(v.ordem ?? '').trim() === '' ? null : Number(v.ordem);
    if (ordem != null && (!Number.isInteger(ordem) || ordem < 0)) erros['ordem'] = 'A ordem é um número inteiro, 0 ou mais.';
    if (!this.validar(erros)) return;
    const atual = this.categoriaEditando();
    const dto = { nome: v.nome.trim(), ordem, ativa: v.ativa };
    this.submitting.set(true);
    (atual ? this.service.atualizarCategoria(atual.uuid, dto) : this.service.criarCategoria(dto)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.mostrarToast(atual ? 'Categoria atualizada' : 'Categoria cadastrada', dto.nome);
        this.editarCategoria(null);
        this.service.categorias().subscribe((c) => this.categorias.set(c));
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  private enviar(req: Observable<ProgramaDto>, titulo: string, detalhe: string, recarregar: boolean): void {
    this.submitting.set(true);
    req.subscribe({
      next: (d) => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(titulo, detalhe);
        this.selecionadoId.set(d.resumo.uuid);
        this.detalhe.set(d);
        if (recarregar) this.carregar();
        else this.programas.update((ps) => ps.map((p) => (p.uuid === d.resumo.uuid ? d.resumo : p)));
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
