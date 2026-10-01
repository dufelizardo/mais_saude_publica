import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { CargoResponseDto } from '../../../core/models/cargo';
import { CategoriaSalarialResponseDto } from '../../../core/models/categoria-salarial';
import { ErrorResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { RegraAnuenioResponseDto } from '../../../core/models/regra-anuenio';
import { MotivoTabelaSalarial, TabelaSalarialResponseDto } from '../../../core/models/tabela-salarial';
import { AuthService } from '../../../core/services/auth';
import { CargoService } from '../../../core/services/cargo';
import { CategoriaSalarialService } from '../../../core/services/categoria-salarial';
import { RegraAnuenioService } from '../../../core/services/regra-anuenio';
import { TabelaSalarialService } from '../../../core/services/tabela-salarial';
import { Drawer } from '../../../shared/drawer/drawer';
import { dataBr, hojeIso, moeda, proximaAba } from '../../../shared/formato';

type Aba = 'cargos' | 'categorias' | 'anuenio';

type Gaveta =
  | { tipo: 'cargo'; cargo: CargoResponseDto | null }
  | { tipo: 'categoria'; categoria: CategoriaSalarialResponseDto | null }
  | { tipo: 'regra'; regra: RegraAnuenioResponseDto | null }
  | { tipo: 'tabela'; cargo: CargoResponseDto };

const MOTIVOS: { valor: MotivoTabelaSalarial; rotulo: string }[] = [
  { valor: 'DISSIDIO', rotulo: 'Dissídio' },
  { valor: 'REVISAO_PLANO_CARGOS_SALARIOS', rotulo: 'Revisão do plano de cargos e salários' },
];

/**
 * Cargos & salários (ADR-0073): a estrutura de remuneração numa tela só — cargos (com a tabela salarial de cada um
 * em gaveta), categorias salariais e regras de anuênio. Junta as antigas telas Categorias salariais, Cargos,
 * Tabela salarial e Regras de anuênio; as rotas antigas redirecionam para cá.
 */
@Component({
  selector: 'app-cargos-e-salarios',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './cargos-e-salarios.html',
})
export class CargosESalarios {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly cargoService = inject(CargoService);
  private readonly categoriaService = inject(CategoriaSalarialService);
  private readonly regraService = inject(RegraAnuenioService);
  private readonly tabelaService = inject(TabelaSalarialService);
  private readonly route = inject(ActivatedRoute);

  protected readonly moeda = moeda;
  protected readonly dataBr = dataBr;
  protected readonly motivos = MOTIVOS;
  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'cargos', rotulo: 'Cargos' },
    { id: 'categorias', rotulo: 'Categorias salariais' },
    { id: 'anuenio', rotulo: 'Regras de anuênio' },
  ];

  protected readonly cargos = signal<CargoResponseDto[]>([]);
  protected readonly categorias = signal<CategoriaSalarialResponseDto[]>([]);
  protected readonly regras = signal<RegraAnuenioResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly aba = signal<Aba>('cargos');
  protected readonly busca = signal('');
  protected readonly filtroCategoria = signal('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  /** Histórico da tabela salarial do cargo aberto na gaveta; nulo = carregando. */
  protected readonly historico = signal<TabelaSalarialResponseDto[] | null>(null);
  protected readonly vigente = signal<TabelaSalarialResponseDto | null>(null);

  protected readonly cargoForm = this.fb.nonNullable.group({ categoriaId: [''], nome: [''], descricao: [''] });
  protected readonly categoriaForm = this.fb.nonNullable.group({ nome: [''], convencaoColetiva: [''] });
  protected readonly regraForm = this.fb.nonNullable.group({ categoriaId: [''], percentualPorAno: [''], tetoAnos: [''] });
  protected readonly valorForm = this.fb.nonNullable.group({ valorBase: [''], dataVigencia: [hojeIso()], motivo: ['' as MotivoTabelaSalarial | ''] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    const params = this.route.snapshot.queryParamMap;
    const aba = params.get('aba');
    if (aba === 'categorias' || aba === 'anuenio') this.aba.set(aba);
    const cargoId = params.get('cargo');
    this.carregar(() => {
      const cargo = cargoId ? this.cargos().find((c) => c.uuid === cargoId) : null;
      if (cargo) this.abrirTabela(cargo);
    });
  }

  /** Listagens vazias respondem 404 no backend — tratadas como lista vazia. */
  private carregar(depois?: () => void): void {
    this.carregando.set(true);
    forkJoin({
      cargos: this.cargoService.listar().pipe(catchError(() => of([]))),
      categorias: this.categoriaService.listar().pipe(catchError(() => of([]))),
      regras: this.regraService.listar().pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.cargos.set(r.cargos);
      this.categorias.set(r.categorias);
      this.regras.set(r.regras);
      this.carregando.set(false);
      depois?.();
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────────

  protected readonly cargosFiltrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const cat = this.filtroCategoria();
    return this.cargos()
      .filter((c) => (!q || `${c.nome} ${c.descricao ?? ''}`.toLowerCase().includes(q)) && (!cat || c.categoriaUuid === cat))
      .sort((a, b) => a.nome.localeCompare(b.nome));
  });

  protected readonly categoriasFiltradas = computed(() => {
    const q = this.busca().trim().toLowerCase();
    return this.categorias()
      .filter((c) => !q || `${c.nome} ${c.convencaoColetiva ?? ''}`.toLowerCase().includes(q))
      .sort((a, b) => a.nome.localeCompare(b.nome));
  });

  protected readonly regrasOrdenadas = computed(() => [...this.regras()].sort((a, b) => a.categoriaNome.localeCompare(b.categoriaNome)));

  protected readonly cargosPorCategoria = computed(() => {
    const mapa = new Map<string, number>();
    for (const c of this.cargos()) mapa.set(c.categoriaUuid, (mapa.get(c.categoriaUuid) ?? 0) + 1);
    return mapa;
  });

  protected readonly regraPorCategoria = computed(() => new Map(this.regras().map((r) => [r.categoriaUuid, r])));

  protected readonly categoriasSemRegra = computed(() => this.categorias().filter((c) => !this.regraPorCategoria().has(c.uuid)));

  protected readonly resumo = computed(() => ({
    categorias: this.categorias().length,
    convencoes: new Set(this.categorias().map((c) => c.convencaoColetiva).filter(Boolean)).size,
    cargos: this.cargos().length,
    semDescricao: this.cargos().filter((c) => !c.descricao).length,
    regras: this.regras().length,
    semRegra: this.categoriasSemRegra().length,
  }));

  protected contagem(aba: Aba): number {
    return aba === 'cargos' ? this.cargos().length : aba === 'categorias' ? this.categorias().length : this.regras().length;
  }

  protected rotuloMotivo(m: MotivoTabelaSalarial): string {
    return MOTIVOS.find((x) => x.valor === m)?.rotulo ?? m;
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

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'cargo': return g.cargo ? 'Editar cargo' : 'Novo cargo';
      case 'categoria': return g.categoria ? 'Editar categoria salarial' : 'Nova categoria salarial';
      case 'regra': return g.regra ? 'Editar regra de anuênio' : 'Nova regra de anuênio';
      case 'tabela': return `Tabela salarial · ${g.cargo.nome}`;
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

  protected abrirCargo(cargo: CargoResponseDto | null): void {
    this.cargoForm.reset({ categoriaId: cargo?.categoriaUuid ?? this.filtroCategoria(), nome: cargo?.nome ?? '', descricao: cargo?.descricao ?? '' });
    this.abrir({ tipo: 'cargo', cargo });
  }

  protected abrirCategoria(categoria: CategoriaSalarialResponseDto | null): void {
    this.categoriaForm.reset({ nome: categoria?.nome ?? '', convencaoColetiva: categoria?.convencaoColetiva ?? '' });
    this.abrir({ tipo: 'categoria', categoria });
  }

  protected abrirRegra(regra: RegraAnuenioResponseDto | null): void {
    this.regraForm.reset({
      categoriaId: regra?.categoriaUuid ?? '',
      percentualPorAno: regra ? String(regra.percentualPorAno) : '',
      tetoAnos: regra?.tetoAnos != null ? String(regra.tetoAnos) : '',
    });
    this.abrir({ tipo: 'regra', regra });
  }

  /** Tabela salarial do cargo: valor vigente, histórico e registro de um valor novo (reajuste). */
  protected abrirTabela(cargo: CargoResponseDto): void {
    this.valorForm.reset({ valorBase: '', dataVigencia: hojeIso(), motivo: '' });
    this.historico.set(null);
    this.vigente.set(null);
    this.abrir({ tipo: 'tabela', cargo });
    this.carregarTabela(cargo.uuid);
  }

  private carregarTabela(cargoId: string): void {
    forkJoin({
      historico: this.tabelaService.listarPorCargo(cargoId).pipe(catchError(() => of([]))),
      vigente: this.tabelaService.buscarVigente(cargoId).pipe(catchError(() => of(null))),
    }).subscribe((r) => {
      this.historico.set([...r.historico].sort((a, b) => b.dataVigencia.localeCompare(a.dataVigencia)));
      this.vigente.set(r.vigente);
    });
  }

  protected teclaNaLinha(event: KeyboardEvent, cargo: CargoResponseDto): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.abrirTabela(cargo);
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
    if (!g) return;
    if (g.tipo === 'cargo') this.salvarCargo(g.cargo);
    if (g.tipo === 'categoria') this.salvarCategoria(g.categoria);
    if (g.tipo === 'regra') this.salvarRegra(g.regra);
    if (g.tipo === 'tabela') this.salvarValor(g.cargo);
  }

  private salvarCargo(cargo: CargoResponseDto | null): void {
    const v = this.cargoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.categoriaId) erros['categoriaId'] = 'Escolha a categoria salarial.';
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome do cargo.';
    if (!this.validar(erros)) return;
    const dto = { categoriaId: v.categoriaId, nome: v.nome.trim(), descricao: v.descricao.trim() || undefined };
    this.enviar(cargo ? this.cargoService.atualizar(cargo.uuid, dto) : this.cargoService.criar(dto),
      cargo ? 'Cargo atualizado' : 'Cargo cadastrado', dto.nome);
  }

  private salvarCategoria(categoria: CategoriaSalarialResponseDto | null): void {
    const v = this.categoriaForm.getRawValue();
    if (!this.validar(v.nome.trim() ? {} : { nome: 'Informe o nome da categoria.' })) return;
    const dto = { nome: v.nome.trim(), convencaoColetiva: v.convencaoColetiva.trim() || undefined };
    this.enviar(categoria ? this.categoriaService.atualizar(categoria.uuid, dto) : this.categoriaService.criar(dto),
      categoria ? 'Categoria atualizada' : 'Categoria cadastrada', dto.nome);
  }

  private salvarRegra(regra: RegraAnuenioResponseDto | null): void {
    const v = this.regraForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.categoriaId) erros['categoriaId'] = 'Escolha a categoria.';
    const pct = Number(v.percentualPorAno.replace(',', '.'));
    if (!v.percentualPorAno || !(pct > 0)) erros['percentualPorAno'] = 'Informe o percentual por ano (maior que zero).';
    if (v.tetoAnos && !(Number(v.tetoAnos) > 0)) erros['tetoAnos'] = 'O teto precisa ser um número de anos maior que zero.';
    if (!this.validar(erros)) return;
    const dto = { categoriaId: v.categoriaId, percentualPorAno: pct, tetoAnos: v.tetoAnos ? Number(v.tetoAnos) : undefined };
    const nome = this.categorias().find((c) => c.uuid === v.categoriaId)?.nome ?? '';
    this.enviar(regra ? this.regraService.atualizar(regra.uuid, dto) : this.regraService.criar(dto),
      regra ? 'Regra de anuênio atualizada' : 'Regra de anuênio cadastrada', `${nome} · ${pct}% ao ano`);
  }

  private salvarValor(cargo: CargoResponseDto): void {
    const v = this.valorForm.getRawValue();
    const erros: Record<string, string> = {};
    const valor = Number(v.valorBase.replace(/\./g, '').replace(',', '.'));
    if (!v.valorBase || !(valor > 0)) erros['valorBase'] = 'Informe o valor base (maior que zero).';
    if (!v.dataVigencia) erros['dataVigencia'] = 'Informe a data de vigência.';
    if (!v.motivo) erros['motivo'] = 'Escolha o motivo.';
    if (!this.validar(erros)) return;
    this.submitting.set(true);
    this.tabelaService
      .criar({ cargoId: cargo.uuid, valorBase: valor, dataVigencia: v.dataVigencia, motivo: v.motivo as MotivoTabelaSalarial })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.mostrarToast('Novo valor registrado', `${cargo.nome} · ${moeda(valor)} a partir de ${dataBr(v.dataVigencia)}`);
          this.valorForm.reset({ valorBase: '', dataVigencia: hojeIso(), motivo: '' });
          this.carregarTabela(cargo.uuid);
        },
        error: (e: HttpErrorResponse) => this.falhou(e),
      });
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.mostrarToast(titulo, detalhe);
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
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}
