import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { CapacidadeAdministrativaResponseDto } from '../../../core/models/capacidade-administrativa';
import { PerfilAdministrativoResponseDto } from '../../../core/models/perfil-administrativo';
import { PerfilPorTipoUnidadeResponseDto } from '../../../core/models/perfil-por-tipo-unidade';
import { ProcessoAdministrativoResponseDto } from '../../../core/models/processo-administrativo';
import { ErrorResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { TipoUnidadeDeSaude } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { CapacidadeAdministrativaService } from '../../../core/services/capacidade-administrativa';
import { PerfilAdministrativoService } from '../../../core/services/perfil-administrativo';
import { PerfilPorTipoUnidadeService } from '../../../core/services/perfil-por-tipo-unidade';
import { ProcessoAdministrativoService } from '../../../core/services/processo-administrativo';
import { Drawer } from '../../../shared/drawer/drawer';
import { proximaAba } from '../../../shared/formato';

type Aba = 'capacidades' | 'processos' | 'perfis' | 'tipos';

type Gaveta =
  | { tipo: 'capacidade'; item: CapacidadeAdministrativaResponseDto | null }
  | { tipo: 'processo'; item: ProcessoAdministrativoResponseDto | null }
  | { tipo: 'perfil'; item: PerfilAdministrativoResponseDto | null }
  | { tipo: 'mapeamento'; item: PerfilPorTipoUnidadeResponseDto | null };

/** Só os tipos que são unidade de saúde (ADR-0038, decisão 5): os níveis de gestão não têm perfil administrativo. */
const TIPOS_UNIDADE: { valor: TipoUnidadeDeSaude; rotulo: string }[] = [
  { valor: 'UBS', rotulo: 'UBS' },
  { valor: 'HOSPITAL', rotulo: 'Hospital' },
  { valor: 'UPA', rotulo: 'UPA' },
  { valor: 'LABORATORIO', rotulo: 'Laboratório' },
  { valor: 'CAPS', rotulo: 'CAPS' },
  { valor: 'CENTRO_ESPECIALIDADES', rotulo: 'Centro de especialidades' },
  { valor: 'CENTRO_REABILITACAO', rotulo: 'Centro de reabilitação' },
  { valor: 'POLICLINICA', rotulo: 'Policlínica' },
];

/**
 * Modelo administrativo (ADR-0074): o catálogo que descreve o que cada tipo de unidade faz administrativamente —
 * capacidades, os processos de cada capacidade, perfis administrativos e o perfil de cada tipo de unidade. Junta as
 * antigas telas Capacidades, Processos, Perfis administrativos e Perfil por tipo de unidade; as rotas antigas
 * redirecionam para a aba certa.
 */
@Component({
  selector: 'app-modelo-administrativo',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './modelo-administrativo.html',
})
export class ModeloAdministrativo {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly capacidadeService = inject(CapacidadeAdministrativaService);
  private readonly processoService = inject(ProcessoAdministrativoService);
  private readonly perfilService = inject(PerfilAdministrativoService);
  private readonly mapeamentoService = inject(PerfilPorTipoUnidadeService);
  private readonly route = inject(ActivatedRoute);

  protected readonly tiposUnidade = TIPOS_UNIDADE;
  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'capacidades', rotulo: 'Capacidades' },
    { id: 'processos', rotulo: 'Processos' },
    { id: 'perfis', rotulo: 'Perfis administrativos' },
    { id: 'tipos', rotulo: 'Perfil por tipo de unidade' },
  ];

  protected readonly capacidades = signal<CapacidadeAdministrativaResponseDto[]>([]);
  protected readonly processos = signal<ProcessoAdministrativoResponseDto[]>([]);
  protected readonly perfis = signal<PerfilAdministrativoResponseDto[]>([]);
  protected readonly mapeamentos = signal<PerfilPorTipoUnidadeResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly aba = signal<Aba>('capacidades');
  protected readonly busca = signal('');
  protected readonly filtroCapacidade = signal('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly catalogoForm = this.fb.nonNullable.group({ capacidadeId: [''], codigo: [''], nome: [''], descricao: [''], ativo: [true] });
  protected readonly mapeamentoForm = this.fb.nonNullable.group({ tipo: ['' as TipoUnidadeDeSaude | ''], perfilAdministrativoId: [''] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    const aba = this.route.snapshot.queryParamMap.get('aba');
    if (aba === 'processos' || aba === 'perfis' || aba === 'tipos') this.aba.set(aba);
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    forkJoin({
      capacidades: this.capacidadeService.listar().pipe(catchError(() => of([]))),
      processos: this.processoService.listar().pipe(catchError(() => of([]))),
      perfis: this.perfilService.listar().pipe(catchError(() => of([]))),
      mapeamentos: this.mapeamentoService.listar().pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.capacidades.set(r.capacidades);
      this.processos.set(r.processos);
      this.perfis.set(r.perfis);
      this.mapeamentos.set(r.mapeamentos);
      this.carregando.set(false);
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────────

  private filtrar<T extends { codigo: string; nome: string; descricao?: string; ativo: boolean }>(lista: T[]): T[] {
    const q = this.busca().trim().toLowerCase();
    return lista
      .filter((i) => !q || `${i.codigo} ${i.nome} ${i.descricao ?? ''}`.toLowerCase().includes(q))
      .sort((a, b) => Number(b.ativo) - Number(a.ativo) || a.codigo.localeCompare(b.codigo));
  }

  protected readonly capacidadesFiltradas = computed(() => this.filtrar(this.capacidades()));
  protected readonly perfisFiltrados = computed(() => this.filtrar(this.perfis()));
  protected readonly processosFiltrados = computed(() => {
    const c = this.filtroCapacidade();
    return this.filtrar(this.processos()).filter((p) => !c || p.capacidadeUuid === c);
  });

  protected readonly processosPorCapacidade = computed(() => {
    const mapa = new Map<string, number>();
    for (const p of this.processos()) mapa.set(p.capacidadeUuid, (mapa.get(p.capacidadeUuid) ?? 0) + 1);
    return mapa;
  });

  protected readonly tiposPorPerfil = computed(() => {
    const mapa = new Map<string, string[]>();
    for (const m of this.mapeamentos()) mapa.set(m.perfilAdministrativoUuid, [...(mapa.get(m.perfilAdministrativoUuid) ?? []), this.rotuloTipo(m.tipo)]);
    return mapa;
  });

  protected readonly mapeamentoPorTipo = computed(() => new Map(this.mapeamentos().map((m) => [m.tipo, m])));

  /** Tipos ainda sem perfil (e, na edição, o próprio tipo). */
  protected readonly tiposDisponiveis = computed(() => {
    const g = this.gaveta();
    const editando = g?.tipo === 'mapeamento' ? g.item?.tipo : null;
    return TIPOS_UNIDADE.filter((t) => t.valor === editando || !this.mapeamentoPorTipo().has(t.valor));
  });

  protected readonly resumo = computed(() => ({
    capacidades: this.capacidades().filter((c) => c.ativo).length,
    capacidadesInativas: this.capacidades().filter((c) => !c.ativo).length,
    processos: this.processos().filter((p) => p.ativo).length,
    capacidadesSemProcesso: this.capacidades().filter((c) => c.ativo && !this.processosPorCapacidade().has(c.uuid)).length,
    perfis: this.perfis().filter((p) => p.ativo).length,
    tiposComPerfil: this.mapeamentos().length,
  }));

  protected contagem(aba: Aba): number {
    return { capacidades: this.capacidades().length, processos: this.processos().length, perfis: this.perfis().length, tipos: this.mapeamentos().length }[aba];
  }

  protected rotuloTipo(t: TipoUnidadeDeSaude): string {
    return TIPOS_UNIDADE.find((x) => x.valor === t)?.rotulo ?? t;
  }

  // ── Abas ───────────────────────────────────────────────────────────────────────────────────────

  protected selecionarAba(aba: Aba): void {
    this.aba.set(aba);
    this.busca.set('');
  }

  protected navegarAbas(event: KeyboardEvent): void {
    const proxima = proximaAba(event, this.abas.map((a) => a.id), this.aba());
    if (proxima) this.selecionarAba(proxima);
  }

  /** Da capacidade para os processos dela. */
  protected verProcessos(c: CapacidadeAdministrativaResponseDto): void {
    this.selecionarAba('processos');
    this.filtroCapacidade.set(c.uuid);
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    const novo = !g.item;
    switch (g.tipo) {
      case 'capacidade': return novo ? 'Nova capacidade' : 'Editar capacidade';
      case 'processo': return novo ? 'Novo processo' : 'Editar processo';
      case 'perfil': return novo ? 'Novo perfil administrativo' : 'Editar perfil administrativo';
      case 'mapeamento': return novo ? 'Definir perfil de um tipo de unidade' : 'Trocar perfil do tipo de unidade';
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

  protected abrirCatalogo(tipo: 'capacidade' | 'processo' | 'perfil', item: { codigo: string; nome: string; descricao?: string; ativo: boolean; capacidadeUuid?: string } | null): void {
    this.catalogoForm.reset({
      capacidadeId: item?.capacidadeUuid ?? this.filtroCapacidade(),
      codigo: item?.codigo ?? '',
      nome: item?.nome ?? '',
      descricao: item?.descricao ?? '',
      ativo: item?.ativo ?? true,
    });
    this.abrir({ tipo, item } as Gaveta);
  }

  protected abrirMapeamento(item: PerfilPorTipoUnidadeResponseDto | null, tipo: TipoUnidadeDeSaude | '' = ''): void {
    this.mapeamentoForm.reset({ tipo: item?.tipo ?? tipo, perfilAdministrativoId: item?.perfilAdministrativoUuid ?? '' });
    this.abrir({ tipo: 'mapeamento', item });
  }

  /** O botão principal da aba aberta. */
  protected novoNaAba(): void {
    const aba = this.aba();
    if (aba === 'capacidades') this.abrirCatalogo('capacidade', null);
    if (aba === 'processos') this.abrirCatalogo('processo', null);
    if (aba === 'perfis') this.abrirCatalogo('perfil', null);
    if (aba === 'tipos') this.abrirMapeamento(null);
  }

  protected rotuloNovo(): string {
    return { capacidades: 'Nova capacidade', processos: 'Novo processo', perfis: 'Novo perfil', tipos: 'Definir perfil' }[this.aba()];
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
    if (!g) return;
    if (g.tipo === 'mapeamento') {
      this.salvarMapeamento(g.item);
      return;
    }
    const v = this.catalogoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (g.tipo === 'processo' && !v.capacidadeId) erros['capacidadeId'] = 'Escolha a capacidade.';
    if (!v.codigo.trim()) erros['codigo'] = 'Informe o código.';
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome.';
    if (!this.validar(erros)) return;
    const base = { codigo: v.codigo.trim(), nome: v.nome.trim(), descricao: v.descricao.trim() || undefined, ativo: v.ativo };
    const detalhe = `${base.codigo} · ${base.nome}`;
    if (g.tipo === 'capacidade') {
      this.enviar(g.item ? this.capacidadeService.atualizar(g.item.uuid, base) : this.capacidadeService.criar(base),
        g.item ? 'Capacidade atualizada' : 'Capacidade cadastrada', detalhe);
    } else if (g.tipo === 'processo') {
      const dto = { ...base, capacidadeId: v.capacidadeId };
      this.enviar(g.item ? this.processoService.atualizar(g.item.uuid, dto) : this.processoService.criar(dto),
        g.item ? 'Processo atualizado' : 'Processo cadastrado', detalhe);
    } else {
      this.enviar(g.item ? this.perfilService.atualizar(g.item.uuid, base) : this.perfilService.criar(base),
        g.item ? 'Perfil atualizado' : 'Perfil cadastrado', detalhe);
    }
  }

  private salvarMapeamento(item: PerfilPorTipoUnidadeResponseDto | null): void {
    const v = this.mapeamentoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.tipo) erros['tipo'] = 'Escolha o tipo de unidade.';
    if (!v.perfilAdministrativoId) erros['perfilAdministrativoId'] = 'Escolha o perfil.';
    if (!this.validar(erros)) return;
    const dto = { tipo: v.tipo as TipoUnidadeDeSaude, perfilAdministrativoId: v.perfilAdministrativoId };
    const perfil = this.perfis().find((p) => p.uuid === v.perfilAdministrativoId)?.nome ?? '';
    this.enviar(item ? this.mapeamentoService.atualizar(item.uuid, dto) : this.mapeamentoService.criar(dto),
      'Perfil definido', `${this.rotuloTipo(dto.tipo)} · ${perfil}`);
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }
}
