import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { ErrorResponseDto, ProfissionalResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { ResponsabilidadeAdministrativaResponseDto } from '../../../core/models/responsabilidade-administrativa';
import { SetorResponseDto, TipoSetor } from '../../../core/models/setor';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { ProfissionalService } from '../../../core/services/profissional';
import { ResponsabilidadeAdministrativaService } from '../../../core/services/responsabilidade-administrativa';
import { SetorService } from '../../../core/services/setor';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { dataBr, hojeIso, proximaAba } from '../../../shared/formato';

type Aba = 'setores' | 'responsabilidades';
type Gaveta =
  | { tipo: 'setor'; setor: SetorResponseDto | null }
  | { tipo: 'responsabilidade' }
  | { tipo: 'encerrar'; item: ResponsabilidadeAdministrativaResponseDto }
  | { tipo: 'responsaveis'; setor: SetorResponseDto };

const TIPOS: { valor: TipoSetor; rotulo: string; classe: string }[] = [
  { valor: 'ADMINISTRATIVO', rotulo: 'Administrativo', classe: 'info' },
  { valor: 'ASSISTENCIAL', rotulo: 'Assistencial', classe: 'ok' },
  { valor: 'APOIO', rotulo: 'Apoio', classe: 'muted' },
  { valor: 'TECNICO', rotulo: 'Técnico', classe: 'purple' },
];

/**
 * Setores (ADR-0074): os setores de cada unidade, com o responsável, e as responsabilidades administrativas de
 * cada profissional (fiscal de contrato, comissões…). Junta as antigas telas Setores e Responsabilidades
 * administrativas; a rota antiga das responsabilidades redireciona para a aba.
 */
@Component({
  selector: 'app-setores',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './setores.html',
  styleUrl: './setores.css',
})
export class Setores {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly setorService = inject(SetorService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly responsabilidadeService = inject(ResponsabilidadeAdministrativaService);
  private readonly route = inject(ActivatedRoute);

  protected readonly dataBr = dataBr;
  /** Responsáveis do setor aberto na gaveta (ADR-0083); null enquanto carrega. */
  protected readonly responsaveisDoSetor = signal<ResponsabilidadeAdministrativaResponseDto[] | null>(null);
  protected readonly tipos = TIPOS;
  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'setores', rotulo: 'Setores' },
    { id: 'responsabilidades', rotulo: 'Responsabilidades' },
  ];

  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly profissionais = signal<ProfissionalResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly aba = signal<Aba>('setores');
  protected readonly busca = signal('');
  protected readonly filtroTipo = signal<TipoSetor | ''>('');
  protected readonly filtroUnidade = signal('');
  protected readonly soSemResponsavel = signal(false);

  /** Profissional escolhido na aba Responsabilidades e o histórico dele (nulo = carregando). */
  protected readonly buscaProfissional = signal('');
  protected readonly profissional = signal<ProfissionalResponseDto | null>(null);
  protected readonly historico = signal<ResponsabilidadeAdministrativaResponseDto[] | null>(null);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly setorForm = this.fb.nonNullable.group({
    unidadeId: [''],
    nome: [''],
    codigo: [''],
    tipo: ['ADMINISTRATIVO' as TipoSetor],
    ativo: [true],
    matriculaResponsavel: [''],
  });
  protected readonly responsabilidadeForm = this.fb.nonNullable.group({ setorId: [''], tipo: [''], descricao: [''], dataInicio: [hojeIso()] });
  protected readonly encerrarForm = this.fb.nonNullable.group({ dataFim: [hojeIso()] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.carregar();
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.profissionalService.listar().pipe(catchError(() => of([]))).subscribe((p) => {
      this.profissionais.set(p);
      const matricula = this.route.snapshot.queryParamMap.get('matricula');
      const alvo = matricula ? p.find((x) => x.matricula === matricula) : null;
      if (alvo) this.escolherProfissional(alvo);
    });
    if (this.route.snapshot.queryParamMap.get('aba') === 'responsabilidades') this.aba.set('responsabilidades');
  }

  private carregar(): void {
    this.carregando.set(true);
    this.setorService.listar().pipe(catchError(() => of([]))).subscribe((s) => {
      this.setores.set(s);
      this.carregando.set(false);
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────────

  protected readonly filtrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const t = this.filtroTipo();
    const u = this.filtroUnidade();
    return this.setores()
      .filter((s) => (!q || `${s.nome} ${s.codigo} ${s.responsavelNome ?? ''}`.toLowerCase().includes(q))
        && (!t || s.tipo === t) && (!u || s.unidadeUuid === u) && (!this.soSemResponsavel() || !s.responsavelMatricula))
      .sort((a, b) => a.unidadeNome.localeCompare(b.unidadeNome) || a.nome.localeCompare(b.nome));
  });

  protected readonly unidadesComSetor = computed(() => {
    const mapa = new Map(this.setores().map((s) => [s.unidadeUuid, s.unidadeNome]));
    return [...mapa.entries()].map(([uuid, nome]) => ({ uuid, nome })).sort((a, b) => a.nome.localeCompare(b.nome));
  });

  protected readonly resumo = computed(() => {
    const ativos = this.setores().filter((s) => s.ativo);
    return {
      ativos: ativos.length,
      inativos: this.setores().length - ativos.length,
      semResponsavel: ativos.filter((s) => !s.responsavelMatricula).length,
      unidades: new Set(ativos.map((s) => s.unidadeUuid)).size,
      assistenciais: ativos.filter((s) => s.tipo === 'ASSISTENCIAL').length,
      administrativos: ativos.filter((s) => s.tipo === 'ADMINISTRATIVO').length,
      outros: ativos.filter((s) => s.tipo === 'APOIO' || s.tipo === 'TECNICO').length,
    };
  });

  protected readonly sugestoes = computed(() => {
    const q = this.buscaProfissional().trim().toLowerCase();
    if (q.length < 2) return [];
    return this.profissionais()
      .filter((p) => p.ativo && `${p.nome} ${p.matricula}`.toLowerCase().includes(q))
      .slice(0, 8);
  });

  protected readonly vigentes = computed(() => (this.historico() ?? []).filter((r) => !r.dataFim));
  protected readonly encerradas = computed(() => (this.historico() ?? []).filter((r) => r.dataFim));

  protected tipo(t: TipoSetor) {
    return TIPOS.find((x) => x.valor === t)!;
  }

  /** Nome do profissional pela matrícula digitada no responsável do setor. */
  protected nomeDaMatricula(matricula: string): string | null {
    const m = matricula.trim().toLowerCase();
    return m ? (this.profissionais().find((p) => p.matricula.toLowerCase() === m)?.nome ?? null) : null;
  }

  // ── Abas ───────────────────────────────────────────────────────────────────────────────────────

  protected selecionarAba(aba: Aba): void {
    this.aba.set(aba);
  }

  protected navegarAbas(event: KeyboardEvent): void {
    const proxima = proximaAba(event, this.abas.map((a) => a.id), this.aba());
    if (proxima) this.selecionarAba(proxima);
  }

  protected escolherProfissional(p: ProfissionalResponseDto): void {
    this.profissional.set(p);
    this.buscaProfissional.set('');
    this.aba.set('responsabilidades');
    this.carregarHistorico(p.matricula);
  }

  protected trocarProfissional(): void {
    this.profissional.set(null);
    this.historico.set(null);
  }

  private carregarHistorico(matricula: string): void {
    this.historico.set(null);
    this.responsabilidadeService.listarHistorico(matricula).pipe(catchError(() => of([]))).subscribe((h) =>
      this.historico.set([...h].sort((a, b) => b.dataInicio.localeCompare(a.dataInicio))),
    );
  }

  /** Quem tem ou teve responsabilidade neste setor, vigentes primeiro (ADR-0083). */
  protected verResponsabilidades(s: SetorResponseDto): void {
    this.responsaveisDoSetor.set(null);
    this.abrir({ tipo: 'responsaveis', setor: s });
    this.responsabilidadeService.listarPorSetor(s.uuid).pipe(catchError(() => of([])))
      .subscribe((l) => this.responsaveisDoSetor.set(l));
  }

  /** Da gaveta do setor para o histórico completo da pessoa, na aba Responsabilidades. */
  protected verHistoricoDe(matricula: string): void {
    const p = this.profissionais().find((x) => x.matricula === matricula);
    this.fecharGaveta();
    if (p) this.escolherProfissional(p);
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirSetor(setor: SetorResponseDto | null): void {
    this.setorForm.reset({
      unidadeId: setor?.unidadeUuid ?? this.filtroUnidade(),
      nome: setor?.nome ?? '',
      codigo: setor?.codigo ?? '',
      tipo: setor?.tipo ?? 'ADMINISTRATIVO',
      ativo: setor?.ativo ?? true,
      matriculaResponsavel: setor?.responsavelMatricula ?? '',
    });
    this.abrir({ tipo: 'setor', setor });
  }

  protected abrirResponsabilidade(): void {
    this.responsabilidadeForm.reset({ setorId: '', tipo: '', descricao: '', dataInicio: hojeIso() });
    this.abrir({ tipo: 'responsabilidade' });
  }

  protected abrirEncerrar(item: ResponsabilidadeAdministrativaResponseDto): void {
    this.encerrarForm.reset({ dataFim: hojeIso() });
    this.abrir({ tipo: 'encerrar', item });
  }

  protected teclaNaLinha(event: KeyboardEvent, s: SetorResponseDto): void {
    if (event.key === 'Enter' && this.pode('ORGANIZACAO.GERENCIAR')) {
      event.preventDefault();
      this.abrirSetor(s);
    }
  }

  // ── Salvar ─────────────────────────────────────────────────────────────────────────────────────

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  private validar(erros: Record<string, string>): boolean {
    this.erros.set(erros);
    this.erroApi.set(null);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) {
      setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
      return false;
    }
    return true;
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (g?.tipo === 'setor') this.salvarSetor(g.setor);
    if (g?.tipo === 'responsabilidade') this.salvarResponsabilidade();
    if (g?.tipo === 'encerrar') this.salvarEncerramento(g.item);
  }

  private salvarSetor(setor: SetorResponseDto | null): void {
    const v = this.setorForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.unidadeId) erros['unidadeId'] = 'Escolha a unidade.';
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome do setor.';
    if (!v.codigo.trim()) erros['codigo'] = 'Informe o código.';
    if (v.matriculaResponsavel.trim() && !this.nomeDaMatricula(v.matriculaResponsavel)) erros['matriculaResponsavel'] = 'Nenhum profissional com esta matrícula.';
    if (!this.validar(erros)) return;
    const dto = {
      unidadeId: v.unidadeId,
      nome: v.nome.trim(),
      codigo: v.codigo.trim(),
      tipo: v.tipo,
      ativo: v.ativo,
      matriculaResponsavel: v.matriculaResponsavel.trim() || undefined,
    };
    this.enviar(setor ? this.setorService.atualizar(setor.uuid, dto) : this.setorService.criar(dto),
      setor ? 'Setor atualizado' : 'Setor cadastrado', `${dto.nome} · ${dto.codigo}`, () => this.carregar());
  }

  private salvarResponsabilidade(): void {
    const p = this.profissional();
    if (!p) return;
    const v = this.responsabilidadeForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.setorId) erros['setorId'] = 'Escolha o setor.';
    if (!v.tipo.trim()) erros['tipo'] = 'Informe o tipo de responsabilidade.';
    if (!v.dataInicio) erros['dataInicio'] = 'Informe o início.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.responsabilidadeService.criar({
        matriculaProfissional: p.matricula,
        setorId: v.setorId,
        tipo: v.tipo.trim(),
        descricao: v.descricao.trim() || undefined,
        dataInicio: v.dataInicio,
      }),
      'Responsabilidade atribuída',
      `${p.nome} · ${v.tipo.trim()}`,
      () => this.carregarHistorico(p.matricula),
    );
  }

  private salvarEncerramento(item: ResponsabilidadeAdministrativaResponseDto): void {
    const v = this.encerrarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.dataFim) erros['dataFim'] = 'Informe a data de encerramento.';
    else if (v.dataFim < item.dataInicio) erros['dataFim'] = 'O encerramento não pode ser antes do início.';
    if (!this.validar(erros)) return;
    this.enviar(this.responsabilidadeService.encerrar(item.uuid, v.dataFim), 'Responsabilidade encerrada',
      `${item.tipo} · ${item.setorNome}`, () => this.carregarHistorico(item.profissionalMatricula));
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string, depois: () => void): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        depois();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }
}
