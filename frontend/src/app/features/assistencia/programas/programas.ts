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
  AcompanhamentoDto,
  CORES_CATEGORIA,
  CategoriaProgramaDto,
  InscritosProgramaDto,
  MOTIVOS_SAIDA,
  MotivoSaidaAcompanhamento,
  PERIODICIDADES,
  SITUACOES_ACOMPANHAMENTO,
  PeriodicidadeAcompanhamento,
  ProgramaDto,
  ProgramaRequestDto,
  ProgramaResumoDto,
  SITUACOES_PROGRAMA,
  SexoPublico,
  SituacaoPrograma,
} from '../../../core/models/programa';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { PacienteService } from '../../../core/services/paciente';
import { AuthService } from '../../../core/services/auth';
import { EquipeService } from '../../../core/services/equipe';
import { ProfissionalService } from '../../../core/services/profissional';
import { ProgramaService } from '../../../core/services/programa';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { AcoesProgramaticas } from './acoes-programaticas';
import { Campanhas } from './campanhas';
import { SaudeBucalAConvocar } from './saude-bucal-a-convocar';
import { ElegiveisDoPrograma } from './elegiveis-do-programa';

type Gaveta =
  | { tipo: 'programa'; item: ProgramaDto | null }
  | { tipo: 'participacao'; programa: ProgramaDto }
  | { tipo: 'encerrar'; programa: ProgramaDto; participacaoId: string; nome: string }
  | { tipo: 'categorias' }
  | { tipo: 'inscrever'; programa: ProgramaDto }
  | { tipo: 'situacao'; acompanhamento: AcompanhamentoDto; nova: 'ATIVO' | 'FALTOSO' }
  | { tipo: 'saida'; acompanhamento: AcompanhamentoDto };

/**
 * Programas de Saúde (ADR-0118), pelo protótipo `Programas.html`: indicadores, pílulas por categoria, a grade de cartões e
 * o detalhe do programa escolhido, com o público-alvo e as unidades e equipes que o oferecem. Com o acompanhamento
 * (ADR-0119): os números de inscritos, faltosos e atrasados, a inscrição de pacientes e a lista de acompanhados do escopo,
 * com faltoso e saída. Os indicadores clínicos ficam "Em breve" (P6).
 */
@Component({
  selector: 'app-programas',
  imports: [ReactiveFormsModule, Drawer, ElegiveisDoPrograma, AcoesProgramaticas, Campanhas, SaudeBucalAConvocar],
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
  private readonly pacienteService = inject(PacienteService);

  protected readonly abrangencias = ABRANGENCIAS;
  protected readonly listaAbrangencias = Object.keys(ABRANGENCIAS) as AbrangenciaPrograma[];
  protected readonly situacoes = SITUACOES_PROGRAMA;
  protected readonly listaSituacoes = Object.keys(SITUACOES_PROGRAMA) as SituacaoPrograma[];
  protected readonly periodicidades = PERIODICIDADES;
  protected readonly listaPeriodicidades = Object.keys(PERIODICIDADES) as PeriodicidadeAcompanhamento[];
  protected readonly situacoesAcompanhamento = SITUACOES_ACOMPANHAMENTO;
  protected readonly motivosSaida = MOTIVOS_SAIDA;
  protected readonly listaMotivosSaida = Object.keys(MOTIVOS_SAIDA) as MotivoSaidaAcompanhamento[];

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
  /** Os programas (catálogo) ou as ações programáticas do escopo (ADR-0132). */
  protected readonly visao = signal<'programas' | 'acoes' | 'campanhas' | 'saude-bucal'>('programas');
  protected readonly versaoAcoes = signal(0);
  protected readonly detalhe = signal<ProgramaDto | null>(null);
  protected readonly mostrarEncerradas = signal(false);
  /** Inscritos do programa no escopo (ADR-0119): `undefined` carregando, `null` sem acesso. */
  protected readonly inscritos = signal<InscritosProgramaDto | null | undefined>(undefined);
  /** Filtro da lista de acompanhados: todos, só faltosos ou só atrasados. */
  protected readonly situacaoInscritos = signal<'' | 'FALTOSO' | 'ATRASADO'>('');
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly buscaPaciente = signal('');

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
  protected readonly inscreverForm = this.fb.nonNullable.group({ pacienteId: [''], unidadeId: [''], equipeId: [''], profissionalMatricula: [''], inicio: [''] });
  protected readonly situacaoAcompForm = this.fb.nonNullable.group({ motivo: [''] });
  protected readonly saidaForm = this.fb.nonNullable.group({ motivoSaida: ['' as MotivoSaidaAcompanhamento | ''], observacao: [''], fim: [''] });

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────

  protected readonly podeGerenciar = computed(() => this.pode('PROGRAMA.GERENCIAR'));
  protected readonly podeAcompanhar = computed(() => this.pode('PROGRAMA.ACOMPANHAR'));
  /** A avaliação coletiva de saúde bucal é do cirurgião-dentista (ADR-0140). */
  protected readonly podeAvaliarBucal = computed(() => this.pode('ODONTOLOGIA.REGISTRAR'));

  /** Inscritos filtrados pela situação escolhida na lista. */
  protected readonly inscritosFiltrados = computed(() => {
    const s = this.situacaoInscritos();
    return (this.inscritos()?.lista ?? []).filter((a) => !s || (s === 'FALTOSO' ? a.situacao === 'FALTOSO' : a.atrasado));
  });

  /** Pacientes para a gaveta de inscrição: busca por nome, CPF ou CNS, até 8. */
  protected readonly pacientesEncontrados = computed(() => {
    const q = this.buscaPaciente().trim().toLowerCase();
    if (q.length < 2) return [];
    const digitos = q.replace(/\D/g, '');
    return this.pacientes()
      .filter((p) => p.ativo && (p.nome.toLowerCase().includes(q) || (digitos.length >= 3 && ((p.cpf ?? '').includes(digitos) || (p.cartaoSus ?? '').includes(digitos)))))
      .slice(0, 8);
  });

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
      inscritos: ps.reduce((s, p) => s + (p.inscritos ?? 0), 0),
      faltosos: ps.reduce((s, p) => s + (p.faltosos ?? 0), 0),
      atrasados: ps.reduce((s, p) => s + (p.atrasados ?? 0), 0),
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
    this.carregarInscritos(uuid);
  }

  /** Lista de acompanhados do programa, só para quem acompanha (PROGRAMA.ACOMPANHAR); leitura auditada. */
  protected carregarInscritos(programaId: string): void {
    this.situacaoInscritos.set('');
    if (!this.podeAcompanhar()) {
      this.inscritos.set(null);
      return;
    }
    this.inscritos.set(undefined);
    this.service.inscritos(programaId).subscribe({ next: (r) => this.inscritos.set(r), error: () => this.inscritos.set(null) });
  }

  protected verPacientes(): void {
    document.getElementById('inscritos')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
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
    if (g.tipo === 'inscrever') return 'Inscrever paciente';
    if (g.tipo === 'situacao') return g.nova === 'FALTOSO' ? 'Marcar como faltoso' : 'Voltar a ativo';
    if (g.tipo === 'saida') return 'Registrar saída do programa';
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

  /** Muda depois de cada inscrição, para os elegíveis recarregarem (ADR-0128). */
  protected readonly versaoElegiveis = signal(0);

  /** "Inscrever" na lista de elegíveis: a inscrição já com o paciente, a unidade e a equipe da adscrição. */
  protected inscreverElegivel(programa: ProgramaDto, e: { pacienteId: string; nome: string; unidadeId: string; equipeId: string }): void {
    this.abrirInscrever(programa);
    this.inscreverForm.patchValue({ pacienteId: e.pacienteId, unidadeId: e.unidadeId });
    this.buscaPaciente.set(e.nome);
    this.carregarEquipes(e.unidadeId);
    this.inscreverForm.patchValue({ equipeId: e.equipeId });
  }

  protected abrirInscrever(programa: ProgramaDto): void {
    const unidades = this.unidadesDoPrograma(programa);
    this.inscreverForm.reset({ pacienteId: '', unidadeId: unidades.length === 1 ? unidades[0].unidadeId : '', equipeId: '', profissionalMatricula: '', inicio: '' });
    this.buscaPaciente.set('');
    if (!this.pacientes().length) this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    if (unidades.length === 1) this.carregarEquipes(unidades[0].unidadeId);
    else this.equipesDaUnidade.set([]);
    this.abrir({ tipo: 'inscrever', programa });
  }

  /** As unidades que oferecem o programa (uma por unidade, mesmo com várias equipes). */
  protected unidadesDoPrograma(programa: ProgramaDto): { unidadeId: string; unidadeNome: string }[] {
    const vistos = new Set<string>();
    return programa.participacoes.filter((x) => !vistos.has(x.unidadeId) && vistos.add(x.unidadeId)).map((x) => ({ unidadeId: x.unidadeId, unidadeNome: x.unidadeNome }));
  }

  protected escolherPaciente(p: PacienteResponseDto): void {
    this.inscreverForm.patchValue({ pacienteId: p.uuid });
    this.buscaPaciente.set(p.nome);
  }

  protected pacienteEscolhido(): PacienteResponseDto | undefined {
    const id = this.inscreverForm.controls.pacienteId.value;
    return id ? this.pacientes().find((p) => p.uuid === id) : undefined;
  }

  protected escolherUnidadeResponsavel(uuid: string): void {
    this.inscreverForm.patchValue({ unidadeId: uuid, equipeId: '' });
    this.carregarEquipes(uuid);
  }

  private carregarEquipes(uuid: string): void {
    this.equipesDaUnidade.set([]);
    if (!uuid) return;
    this.equipeService.listar(uuid).pipe(catchError(() => of(null))).subscribe((r) => this.equipesDaUnidade.set(r?.lista.filter((e) => e.ativa) ?? []));
  }

  protected abrirSituacao(acompanhamento: AcompanhamentoDto, nova: 'ATIVO' | 'FALTOSO'): void {
    this.situacaoAcompForm.reset({ motivo: '' });
    this.abrir({ tipo: 'situacao', acompanhamento, nova });
  }

  protected abrirSaida(acompanhamento: AcompanhamentoDto): void {
    this.saidaForm.reset({ motivoSaida: '', observacao: '', fim: '' });
    this.abrir({ tipo: 'saida', acompanhamento });
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
    else if (g.tipo === 'inscrever') this.salvarInscricao(g.programa);
    else if (g.tipo === 'situacao') this.salvarSituacaoAcompanhamento(g);
    else if (g.tipo === 'saida') this.salvarSaida(g.acompanhamento);
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

  private salvarInscricao(programa: ProgramaDto): void {
    const v = this.inscreverForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['buscaPaciente'] = 'Escolha o paciente na lista.';
    if (!v.unidadeId) erros['unidadeResponsavel'] = 'Escolha a unidade responsável.';
    if (v.inicio && v.inicio > this.hojeIso()) erros['inicioAcompanhamento'] = 'O início não pode ser no futuro.';
    if (!this.validar(erros)) return;
    this.submitting.set(true);
    this.service.inscrever(programa.resumo.uuid, {
      pacienteId: v.pacienteId, unidadeId: v.unidadeId, equipeId: v.equipeId || null,
      profissionalMatricula: v.profissionalMatricula.trim() || null, inicio: v.inicio || null,
    }).subscribe({
      next: (a) => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast('Paciente inscrito', a.avisoPublico ? `${a.pacienteNome} · atenção: ${a.avisoPublico}` : `${a.pacienteNome} · ${a.programaNome}`);
        this.atualizarDepoisDoAcompanhamento(programa.resumo.uuid);
        this.versaoElegiveis.update((n) => n + 1);
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível inscrever. Tente novamente.');
      },
    });
  }

  private salvarSituacaoAcompanhamento(g: Extract<Gaveta, { tipo: 'situacao' }>): void {
    const v = this.situacaoAcompForm.getRawValue();
    if (!this.validar(v.motivo.trim() ? {} : { motivoSituacao: 'Informe o motivo.' })) return;
    this.enviarAcompanhamento(this.service.mudarSituacao(g.acompanhamento.uuid, g.nova, v.motivo.trim()),
      g.nova === 'FALTOSO' ? 'Marcado como faltoso' : 'Voltou a ativo', g.acompanhamento);
  }

  private salvarSaida(a: AcompanhamentoDto): void {
    const v = this.saidaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.motivoSaida) erros['motivoSaida'] = 'Escolha o motivo.';
    if (!v.observacao.trim()) erros['observacaoSaida'] = 'Descreva a saída.';
    if (v.fim && v.fim > this.hojeIso()) erros['fimAcompanhamento'] = 'A saída não pode ser no futuro.';
    if (!this.validar(erros)) return;
    this.enviarAcompanhamento(this.service.encerrarAcompanhamento(a.uuid, v.motivoSaida as MotivoSaidaAcompanhamento, v.observacao.trim(), v.fim),
      'Saída registrada', a);
  }

  private enviarAcompanhamento(req: Observable<AcompanhamentoDto>, titulo: string, a: AcompanhamentoDto): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(titulo, `${a.pacienteNome} · ${a.programaNome}`);
        this.atualizarDepoisDoAcompanhamento(a.programaId);
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  /** Busca ativa do faltoso ou atrasado (ADR-0132): o responsável sugerido é o ACS da microárea. */
  protected gerarBuscaAtiva(a: AcompanhamentoDto): void {
    this.service.buscaAtiva(a.uuid).subscribe({
      next: (acao) => {
        this.mostrarToast('Busca ativa gerada', `${a.pacienteNome}${acao.responsavelNome ? ' · ' + acao.responsavelNome : ''}`);
        this.versaoAcoes.update((v) => v + 1);
      },
      error: (e: HttpErrorResponse) => this.mostrarToast('Não foi possível gerar a busca ativa',
        (e.error as ErrorResponseDto | undefined)?.message ?? 'Tente novamente.'),
    });
  }

  /** Recarrega os números do catálogo, o detalhe e a lista de inscritos. */
  private atualizarDepoisDoAcompanhamento(programaId: string): void {
    this.service.listar({ unidadeId: this.unidadeFiltro() || undefined }).subscribe((ps) => this.programas.set(ps));
    this.service.buscar(programaId).subscribe((d) => this.detalhe.set(d));
    this.service.inscritos(programaId).subscribe({ next: (r) => this.inscritos.set(r), error: () => this.inscritos.set(null) });
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
