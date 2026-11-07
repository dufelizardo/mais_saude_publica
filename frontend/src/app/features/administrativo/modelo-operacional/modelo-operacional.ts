import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { CapacidadeAssistencialDto, GRUPO_TRANSVERSAL, PerfilOperacionalDto, TipoServicoDto } from '../../../core/models/modelo-operacional';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AuthService } from '../../../core/services/auth';
import { ModeloOperacionalService } from '../../../core/services/modelo-operacional';
import { Drawer } from '../../../shared/drawer/drawer';
import { proximaAba } from '../../../shared/formato';

type Aba = 'perfis' | 'capacidades' | 'tipos';

type Gaveta =
  | { tipo: 'perfil'; item: PerfilOperacionalDto | null }
  | { tipo: 'capacidade'; item: CapacidadeAssistencialDto | null }
  | { tipo: 'tipoServico'; item: TipoServicoDto | null };

/**
 * Modelo operacional (ADR-0117): o catálogo do que as unidades operam (perfis) e conseguem fazer (capacidades,
 * agrupadas por perfil ou transversais) e os tipos de serviço em que se organizam (ADR-0165). As associações a cada
 * unidade ficam nas abas Capacidades e Serviços de Equipamentos de Saúde. Mesmo layout da tela Modelo administrativo (ADR-0074), no contexto assistencial.
 */
@Component({
  selector: 'app-modelo-operacional',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './modelo-operacional.html',
})
export class ModeloOperacional {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly service = inject(ModeloOperacionalService);
  private readonly route = inject(ActivatedRoute);

  protected readonly transversal = GRUPO_TRANSVERSAL;
  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'perfis', rotulo: 'Perfis operacionais' },
    { id: 'capacidades', rotulo: 'Capacidades assistenciais' },
    { id: 'tipos', rotulo: 'Tipos de serviço' },
  ];

  protected readonly perfis = signal<PerfilOperacionalDto[]>([]);
  protected readonly capacidades = signal<CapacidadeAssistencialDto[]>([]);
  protected readonly tipos = signal<TipoServicoDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly aba = signal<Aba>('perfis');
  protected readonly busca = signal('');
  /** Id do perfil, `TRANSVERSAL` ou vazio. */
  protected readonly filtroPerfil = signal('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly form = this.fb.nonNullable.group({ codigo: [''], nome: [''], descricao: [''], perfilId: [''], capacidadeExigidaId: [''], ativo: [true] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    const aba = this.route.snapshot.queryParamMap.get('aba');
    if (aba === 'capacidades' || aba === 'tipos') this.aba.set(aba);
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    forkJoin({
      perfis: this.service.perfis().pipe(catchError(() => of([]))),
      capacidades: this.service.capacidades().pipe(catchError(() => of([]))),
      tipos: this.service.tiposServico().pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.perfis.set(r.perfis);
      this.capacidades.set(r.capacidades);
      this.tipos.set(r.tipos);
      this.carregando.set(false);
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected readonly podeEditar = computed(() => this.pode('ORGANIZACAO.GERENCIAR'));

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────────

  private filtrar<T extends { codigo: string; nome: string; descricao?: string | null; ativo: boolean }>(lista: T[]): T[] {
    const q = this.busca().trim().toLowerCase();
    return lista
      .filter((i) => !q || `${i.codigo} ${i.nome} ${i.descricao ?? ''}`.toLowerCase().includes(q))
      .sort((a, b) => Number(b.ativo) - Number(a.ativo) || a.nome.localeCompare(b.nome));
  }

  protected readonly perfisFiltrados = computed(() => this.filtrar(this.perfis()));
  protected readonly capacidadesFiltradas = computed(() => {
    const f = this.filtroPerfil();
    return this.filtrar(this.capacidades()).filter((c) => !f || (f === 'TRANSVERSAL' ? !c.perfilId : c.perfilId === f));
  });

  protected readonly tiposFiltrados = computed(() => this.filtrar(this.tipos()));

  protected readonly resumo = computed(() => ({
    perfis: this.perfis().filter((p) => p.ativo).length,
    capacidades: this.capacidades().filter((c) => c.ativo).length,
    transversais: this.capacidades().filter((c) => c.ativo && !c.perfilId).length,
    unidadesComPerfil: this.perfis().reduce((s, p) => s + p.unidades, 0),
  }));

  protected contagem(aba: Aba): number {
    return aba === 'perfis' ? this.perfis().length : aba === 'capacidades' ? this.capacidades().length : this.tipos().length;
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

  /** Do perfil para as capacidades do grupo dele. */
  protected verCapacidades(p: PerfilOperacionalDto): void {
    this.selecionarAba('capacidades');
    this.filtroPerfil.set(p.uuid);
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    if (g.tipo === 'perfil') return g.item ? 'Editar perfil operacional' : 'Novo perfil operacional';
    if (g.tipo === 'tipoServico') return g.item ? 'Editar tipo de serviço' : 'Novo tipo de serviço';
    return g.item ? 'Editar capacidade assistencial' : 'Nova capacidade assistencial';
  }

  protected abrir(tipo: Gaveta['tipo'], item: PerfilOperacionalDto | CapacidadeAssistencialDto | TipoServicoDto | null): void {
    const perfilId = item && 'perfilId' in item ? (item.perfilId ?? '') : this.filtroPerfil() === 'TRANSVERSAL' ? '' : this.filtroPerfil();
    const capacidadeExigidaId = item && 'capacidadeExigidaId' in item ? (item.capacidadeExigidaId ?? '') : '';
    this.form.reset({ codigo: item?.codigo ?? '', nome: item?.nome ?? '', descricao: item?.descricao ?? '', perfilId, capacidadeExigidaId, ativo: item?.ativo ?? true });
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set({ tipo, item } as Gaveta);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected novoNaAba(): void {
    const aba = this.aba();
    this.abrir(aba === 'perfis' ? 'perfil' : aba === 'capacidades' ? 'capacidade' : 'tipoServico', null);
  }

  protected rotuloNovo(): string {
    const aba = this.aba();
    return aba === 'perfis' ? 'Novo perfil' : aba === 'capacidades' ? 'Nova capacidade' : 'Novo tipo de serviço';
  }

  /** Perfis que podem agrupar uma capacidade: os ativos, e o atual mesmo inativo (na edição). */
  protected perfisParaGrupo(g: Gaveta): PerfilOperacionalDto[] {
    const atual = g.tipo === 'capacidade' ? g.item?.perfilId : null;
    return this.perfis().filter((p) => p.ativo || p.uuid === atual);
  }

  /** Capacidades que um tipo pode exigir: as ativas, e a atual mesmo inativa (na edição). */
  protected capacidadesParaTipo(g: Gaveta): CapacidadeAssistencialDto[] {
    const atual = g.tipo === 'tipoServico' ? g.item?.capacidadeExigidaId : null;
    return this.capacidades().filter((c) => c.ativo || c.uuid === atual).sort((a, b) => a.nome.localeCompare(b.nome));
  }

  // ── Salvar ─────────────────────────────────────────────────────────────────────────────────────

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.codigo.trim()) erros['codigo'] = 'Informe o código.';
    else if (!/^[A-Za-z0-9_]+$/.test(v.codigo.trim())) erros['codigo'] = 'Use só letras, números e sublinhado (ex.: CAPS_AD).';
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome.';
    this.erros.set(erros);
    this.erroApi.set(null);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) {
      setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
      return;
    }
    const base = { codigo: v.codigo.trim(), nome: v.nome.trim(), descricao: v.descricao.trim() || null, ativo: v.ativo };
    const detalhe = `${base.codigo.toUpperCase()} · ${base.nome}`;
    if (g.tipo === 'perfil') {
      this.enviar(g.item ? this.service.atualizarPerfil(g.item.uuid, base) : this.service.criarPerfil(base),
        g.item ? 'Perfil atualizado' : 'Perfil cadastrado', detalhe);
    } else if (g.tipo === 'tipoServico') {
      const dto = { ...base, capacidadeExigidaId: v.capacidadeExigidaId || null };
      this.enviar(g.item ? this.service.atualizarTipoServico(g.item.uuid, dto) : this.service.criarTipoServico(dto),
        g.item ? 'Tipo de serviço atualizado' : 'Tipo de serviço cadastrado', detalhe);
    } else {
      const dto = { ...base, perfilId: v.perfilId || null };
      this.enviar(g.item ? this.service.atualizarCapacidade(g.item.uuid, dto) : this.service.criarCapacidade(dto),
        g.item ? 'Capacidade atualizada' : 'Capacidade cadastrada', detalhe);
    }
  }

  private enviar(req: Observable<unknown>, titulo: string, detalhe: string): void {
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
