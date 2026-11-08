import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import {
  CATEGORIAS_MATERIAL,
  CategoriaMaterial,
  ItemDeSaldoDto,
  LoteMaterialDto,
  MOTIVOS_PERDA,
  MaterialEstoqueDto,
  MotivoPerdaEstoque,
  MovimentacaoEstoqueDto,
  ORIGENS_ENTRADA,
  OrigemEntradaEstoque,
  SaldoEstoqueDto,
  TIPOS_MOVIMENTACAO,
  TipoMovimentacaoEstoque,
  UNIDADES_MEDIDA,
  UnidadeMedidaMaterial,
} from '../../../core/models/estoque';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { SetorResponseDto } from '../../../core/models/setor';
import { AuthService } from '../../../core/services/auth';
import { EstoqueService } from '../../../core/services/estoque';
import { SetorService } from '../../../core/services/setor';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { dataBr, moeda, proximaAba } from '../../../shared/formato';

type Aba = 'saldo' | 'movimentacao' | 'alertas' | 'catalogo';
type Gaveta =
  | { tipo: 'material'; item: MaterialEstoqueDto | null }
  | { tipo: 'entrada'; item: ItemDeSaldoDto | null }
  | { tipo: 'saida'; item: ItemDeSaldoDto }
  | { tipo: 'perda'; item: ItemDeSaldoDto; lote: LoteMaterialDto }
  | { tipo: 'ajuste'; item: ItemDeSaldoDto; lote: LoteMaterialDto }
  | { tipo: 'minimo'; item: ItemDeSaldoDto | null };

/**
 * Estoque e Almoxarifado (ADR-0177): o saldo da unidade por material, com os lotes; o livro de movimentação; os alertas de mínimo e
 * validade; e o catálogo. Medicamento é da Farmácia e vacina, da Sala de Vacina.
 */
@Component({
  selector: 'app-estoque',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './estoque.html',
})
export class Estoque {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly service = inject(EstoqueService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly setorService = inject(SetorService);

  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'saldo', rotulo: 'Saldo' },
    { id: 'movimentacao', rotulo: 'Movimentação' },
    { id: 'alertas', rotulo: 'Alertas' },
    { id: 'catalogo', rotulo: 'Catálogo' },
  ];
  protected readonly categorias = CATEGORIAS_MATERIAL;
  protected readonly listaCategorias = Object.entries(CATEGORIAS_MATERIAL) as [CategoriaMaterial, string][];
  protected readonly unidadesMedida = UNIDADES_MEDIDA;
  protected readonly listaUnidadesMedida = Object.entries(UNIDADES_MEDIDA) as [UnidadeMedidaMaterial, string][];
  protected readonly tipos = TIPOS_MOVIMENTACAO;
  protected readonly listaTipos = Object.entries(TIPOS_MOVIMENTACAO) as [TipoMovimentacaoEstoque, string][];
  protected readonly origens = ORIGENS_ENTRADA;
  protected readonly listaOrigens = Object.entries(ORIGENS_ENTRADA) as [OrigemEntradaEstoque, string][];
  protected readonly motivos = MOTIVOS_PERDA;
  protected readonly listaMotivos = Object.entries(MOTIVOS_PERDA) as [MotivoPerdaEstoque, string][];
  protected readonly dataBr = dataBr;
  protected readonly moeda = moeda;

  protected readonly aba = signal<Aba>('saldo');
  protected readonly unidades = signal<{ uuid: string; nome: string }[]>([]);
  protected readonly unidadeId = signal('');
  protected readonly saldo = signal<SaldoEstoqueDto | null>(null);
  protected readonly movimentos = signal<MovimentacaoEstoqueDto[]>([]);
  protected readonly materiais = signal<MaterialEstoqueDto[]>([]);
  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly carregando = signal(true);
  protected readonly filtroTipo = signal<TipoMovimentacaoEstoque | ''>('');
  protected readonly busca = signal('');
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly materialForm = this.fb.nonNullable.group({
    codigo: [''], nome: [''], catmat: [''], categoria: ['MEDICO_HOSPITALAR' as CategoriaMaterial], unidadeMedida: ['UNIDADE' as UnidadeMedidaMaterial],
    controlaLote: [true], descricao: [''], ativo: [true],
  });
  protected readonly entradaForm = this.fb.nonNullable.group({
    materialId: [''], quantidade: [''], origem: ['NOTA_FISCAL' as OrigemEntradaEstoque], numeroLote: [''], validade: [''], custoUnitario: [''],
    documento: [''], fornecedor: [''],
  });
  protected readonly saidaForm = this.fb.nonNullable.group({ quantidade: [''], loteId: [''], setorId: [''], justificativa: [''] });
  protected readonly perdaForm = this.fb.nonNullable.group({ quantidade: [''], motivo: ['VENCIMENTO' as MotivoPerdaEstoque], justificativa: [''] });
  protected readonly ajusteForm = this.fb.nonNullable.group({ quantidadeContada: [''], justificativa: [''] });
  protected readonly minimoForm = this.fb.nonNullable.group({ materialId: [''], quantidadeMinima: [''] });

  /** O material escolhido na entrada decide se pede lote e validade. */
  protected readonly materialDaEntrada = signal<MaterialEstoqueDto | null>(null);

  protected readonly itensFiltrados = computed(() => {
    const b = this.busca().trim().toLowerCase();
    return (this.saldo()?.itens ?? []).filter((i) => !b || i.nome.toLowerCase().includes(b) || i.codigo.toLowerCase().includes(b));
  });
  protected readonly materiaisAtivos = computed(() => this.materiais().filter((m) => m.ativo));

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => {
      const lista = l.map((u) => ({ uuid: u.uuid, nome: u.nome })).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
      this.unidades.set(lista);
      if (lista.length) this.mudarUnidade(lista[0].uuid);
      else this.carregando.set(false);
    });
    this.carregarMateriais();
    this.setorService.listar().pipe(catchError(() => of([]))).subscribe((s) => this.setores.set(s.filter((x) => x.ativo)));
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected mudarUnidade(id: string): void {
    this.unidadeId.set(id);
    this.carregar();
  }

  private carregar(): void {
    const id = this.unidadeId();
    if (!id) return;
    this.carregando.set(true);
    this.service.saldo(id).pipe(catchError(() => of(null))).subscribe((s) => {
      this.saldo.set(s);
      this.carregando.set(false);
    });
    this.carregarMovimentos();
  }

  protected carregarMovimentos(): void {
    const id = this.unidadeId();
    if (!id) return;
    this.service.movimentacoes(id, this.filtroTipo()).pipe(catchError(() => of([]))).subscribe((m) => this.movimentos.set(m));
  }

  private carregarMateriais(): void {
    this.service.materiais().pipe(catchError(() => of([]))).subscribe((m) => this.materiais.set(m));
  }

  protected selecionarAba(a: Aba): void {
    this.aba.set(a);
  }

  protected navegarAbas(event: KeyboardEvent): void {
    const proxima = proximaAba(event, this.abas.map((a) => a.id), this.aba());
    if (proxima) {
      this.aba.set(proxima);
      setTimeout(() => document.getElementById('tab-' + proxima)?.focus());
    }
  }

  protected contagem(a: Aba): number | null {
    const s = this.saldo();
    if (a === 'saldo') return s?.itens.length ?? null;
    if (a === 'alertas') return s ? s.abaixoDoMinimo + s.vencendo + s.vencidosComSaldo : null;
    if (a === 'catalogo') return this.materiais().length;
    return null;
  }

  protected setoresDaUnidade(): SetorResponseDto[] {
    return this.setores().filter((s) => s.unidadeUuid === this.unidadeId());
  }

  protected abaixoDoMinimo(): ItemDeSaldoDto[] {
    return (this.saldo()?.itens ?? []).filter((i) => i.abaixoDoMinimo);
  }

  protected rotuloValidade(dias: number): string {
    if (dias < 0) return `venceu há ${-dias} dia(s)`;
    if (dias === 0) return 'vence hoje';
    return `vence em ${dias} dia(s)`;
  }

  // ── Gavetas ──

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected abrirMaterial(m: MaterialEstoqueDto | null): void {
    this.materialForm.reset({
      codigo: m?.codigo ?? '', nome: m?.nome ?? '', catmat: m?.catmat ?? '', categoria: m?.categoria ?? 'MEDICO_HOSPITALAR',
      unidadeMedida: m?.unidadeMedida ?? 'UNIDADE', controlaLote: m?.controlaLote ?? true, descricao: m?.descricao ?? '', ativo: m?.ativo ?? true,
    });
    this.abrir({ tipo: 'material', item: m });
  }

  protected abrirEntrada(item: ItemDeSaldoDto | null): void {
    this.entradaForm.reset({ materialId: item?.materialId ?? '', quantidade: '', origem: 'NOTA_FISCAL', numeroLote: '', validade: '', custoUnitario: '', documento: '', fornecedor: '' });
    this.mudarMaterialDaEntrada(item?.materialId ?? '');
    this.abrir({ tipo: 'entrada', item });
  }

  protected mudarMaterialDaEntrada(id: string): void {
    this.materialDaEntrada.set(this.materiais().find((m) => m.uuid === id) ?? null);
  }

  protected abrirSaida(item: ItemDeSaldoDto): void {
    this.saidaForm.reset({ quantidade: '', loteId: '', setorId: '', justificativa: '' });
    this.abrir({ tipo: 'saida', item });
  }

  protected abrirPerda(item: ItemDeSaldoDto, lote: LoteMaterialDto): void {
    this.perdaForm.reset({ quantidade: '', motivo: lote.vencido ? 'VENCIMENTO' : 'AVARIA', justificativa: '' });
    this.abrir({ tipo: 'perda', item, lote });
  }

  protected abrirAjuste(item: ItemDeSaldoDto, lote: LoteMaterialDto): void {
    this.ajusteForm.reset({ quantidadeContada: '', justificativa: '' });
    this.abrir({ tipo: 'ajuste', item, lote });
  }

  protected abrirMinimo(item: ItemDeSaldoDto | null): void {
    this.minimoForm.reset({ materialId: item?.materialId ?? '', quantidadeMinima: item?.minimo != null ? String(item.minimo) : '' });
    this.abrir({ tipo: 'minimo', item });
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  private validar(erros: Record<string, string>): boolean {
    this.erros.set(erros);
    this.erroApi.set(null);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
    return !primeiro;
  }

  private inteiro(v: string): number | null {
    const n = Number(String(v).trim());
    return String(v).trim() !== '' && Number.isInteger(n) ? n : null;
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    const erros: Record<string, string> = {};
    const unidadeId = this.unidadeId();
    switch (g.tipo) {
      case 'material': {
        const v = this.materialForm.getRawValue();
        if (!v.codigo.trim()) erros['codigoMaterial'] = 'Informe o código.';
        if (!v.nome.trim()) erros['nomeMaterial'] = 'Informe o nome.';
        if (!this.validar(erros)) return;
        const dto = { codigo: v.codigo.trim(), nome: v.nome.trim(), catmat: v.catmat.trim() || null, categoria: v.categoria, unidadeMedida: v.unidadeMedida,
          controlaLote: v.controlaLote, descricao: v.descricao.trim() || null, ativo: v.ativo };
        this.enviar(g.item ? this.service.atualizarMaterial(g.item.uuid, dto) : this.service.criarMaterial(dto), g.item ? 'Material atualizado' : 'Material cadastrado',
          (m) => m.nome, true);
        return;
      }
      case 'entrada': {
        const v = this.entradaForm.getRawValue();
        const m = this.materialDaEntrada();
        const qtd = this.inteiro(v.quantidade);
        if (!v.materialId) erros['materialEntrada'] = 'Escolha o material.';
        if (qtd == null || qtd <= 0) erros['quantidadeEntrada'] = 'Informe uma quantidade inteira maior que zero.';
        if (m?.controlaLote && !v.numeroLote.trim()) erros['loteEntrada'] = 'Informe o lote.';
        if (m?.controlaLote && !v.validade) erros['validadeEntrada'] = 'Informe a validade.';
        if (v.origem === 'NOTA_FISCAL' && !v.documento.trim()) erros['documentoEntrada'] = 'Informe o número da nota fiscal.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.entrada({
          unidadeId, materialId: v.materialId, quantidade: qtd!, origem: v.origem, numeroLote: m?.controlaLote ? v.numeroLote.trim() : null,
          validade: m?.controlaLote ? v.validade : null, custoUnitario: v.custoUnitario.trim() ? Number(v.custoUnitario.replace(',', '.')) : null,
          documento: v.documento.trim() || null, fornecedor: v.fornecedor.trim() || null,
        }), 'Entrada registrada', (mv) => `${mv.materialNome} · saldo do lote ${mv.saldoApos}`);
        return;
      }
      case 'saida': {
        const v = this.saidaForm.getRawValue();
        const qtd = this.inteiro(v.quantidade);
        if (qtd == null || qtd <= 0) erros['quantidadeSaida'] = 'Informe uma quantidade inteira maior que zero.';
        else if (qtd > g.item.total) erros['quantidadeSaida'] = `Há ${g.item.total} em estoque.`;
        if (!this.validar(erros)) return;
        this.enviar(this.service.saida({ unidadeId, materialId: g.item.materialId, quantidade: qtd!, loteId: v.loteId || null, setorId: v.setorId || null,
          justificativa: v.justificativa.trim() || null }), 'Saída registrada', (l) => `${g.item.nome} · ${l.length} lote(s)`);
        return;
      }
      case 'perda': {
        const v = this.perdaForm.getRawValue();
        const qtd = this.inteiro(v.quantidade);
        if (qtd == null || qtd <= 0) erros['quantidadePerda'] = 'Informe uma quantidade inteira maior que zero.';
        else if (qtd > g.lote.quantidade) erros['quantidadePerda'] = `O lote tem ${g.lote.quantidade}.`;
        if (!this.validar(erros)) return;
        this.enviar(this.service.perda({ loteId: g.lote.uuid, quantidade: qtd!, motivo: v.motivo, justificativa: v.justificativa.trim() || null }),
          'Perda registrada', (mv) => `${mv.materialNome} · saldo do lote ${mv.saldoApos}`);
        return;
      }
      case 'ajuste': {
        const v = this.ajusteForm.getRawValue();
        const qtd = this.inteiro(v.quantidadeContada);
        if (qtd == null || qtd < 0) erros['contagemAjuste'] = 'Informe a quantidade contada.';
        else if (qtd === g.lote.quantidade) erros['contagemAjuste'] = 'A contagem confere com o saldo.';
        if (!v.justificativa.trim()) erros['justificativaAjuste'] = 'Justifique o ajuste.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.ajuste({ loteId: g.lote.uuid, quantidadeContada: qtd!, justificativa: v.justificativa.trim() }), 'Ajuste registrado',
          (mv) => `${mv.materialNome} · saldo do lote ${mv.saldoApos}`);
        return;
      }
      case 'minimo': {
        const v = this.minimoForm.getRawValue();
        const qtd = this.inteiro(v.quantidadeMinima);
        if (!v.materialId) erros['materialMinimo'] = 'Escolha o material.';
        if (qtd == null || qtd < 0) erros['quantidadeMinimo'] = 'Informe um número inteiro.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.definirMinimo(unidadeId, v.materialId, qtd!), 'Mínimo definido', (i) => `${i.nome}: ${i.minimo}`);
      }
    }
  }

  private enviar<T>(req: Observable<T>, titulo: string, detalhe: (r: T) => string, catalogo = false): void {
    this.submitting.set(true);
    req.subscribe({
      next: (r) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe: detalhe(r) });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        if (catalogo) this.carregarMateriais();
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }
}
