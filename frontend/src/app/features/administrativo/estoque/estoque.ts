import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, forkJoin, of } from 'rxjs';
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
  ConsumoSemSaldoDto,
  TIPOS_MOVIMENTACAO,
  TipoMovimentacaoEstoque,
  UNIDADES_MEDIDA,
  UnidadeMedidaMaterial,
  CentralEstoqueDto,
  InventarioEstoqueDto,
  MOTIVOS_DIVERGENCIA,
  MotivoDivergenciaEstoque,
  PrioridadeRequisicaoEstoque,
  RequisicaoEstoqueDto,
  SITUACOES_INVENTARIO,
  SITUACOES_REQUISICAO,
  TransferenciaEstoqueDto,
} from '../../../core/models/estoque';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { SetorResponseDto } from '../../../core/models/setor';
import { AuthService } from '../../../core/services/auth';
import { EstoqueService } from '../../../core/services/estoque';
import { SetorService } from '../../../core/services/setor';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { dataBr, moeda, proximaAba } from '../../../shared/formato';

type Aba = 'saldo' | 'movimentacao' | 'alertas' | 'requisicoes' | 'transferencias' | 'inventario' | 'catalogo';
type Gaveta =
  | { tipo: 'material'; item: MaterialEstoqueDto | null }
  | { tipo: 'entrada'; item: ItemDeSaldoDto | null }
  | { tipo: 'saida'; item: ItemDeSaldoDto }
  | { tipo: 'perda'; item: ItemDeSaldoDto; lote: LoteMaterialDto }
  | { tipo: 'ajuste'; item: ItemDeSaldoDto; lote: LoteMaterialDto }
  | { tipo: 'minimo'; item: ItemDeSaldoDto | null }
  | { tipo: 'requisicao' }
  | { tipo: 'atender'; requisicao: RequisicaoEstoqueDto }
  | { tipo: 'motivoRequisicao'; requisicao: RequisicaoEstoqueDto; acao: 'cancelamento' | 'negacao' }
  | { tipo: 'receber'; transferencia: TransferenciaEstoqueDto }
  | { tipo: 'transferir' }
  | { tipo: 'inventario'; inventario: InventarioEstoqueDto | null };

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
    { id: 'requisicoes', rotulo: 'Requisições' },
    { id: 'transferencias', rotulo: 'Transferências' },
    { id: 'inventario', rotulo: 'Inventário' },
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
  protected readonly situacoesRequisicao = SITUACOES_REQUISICAO;
  protected readonly situacoesInventario = SITUACOES_INVENTARIO;
  protected readonly motivosDivergencia_ = MOTIVOS_DIVERGENCIA;
  protected readonly listaMotivosDivergencia = Object.entries(MOTIVOS_DIVERGENCIA) as [MotivoDivergenciaEstoque, string][];
  protected readonly centrais = signal<CentralEstoqueDto[]>([]);
  protected readonly requisicoes = signal<RequisicaoEstoqueDto[]>([]);
  protected readonly transferencias = signal<TransferenciaEstoqueDto[]>([]);
  protected readonly inventarios = signal<InventarioEstoqueDto[]>([]);
  protected readonly itensRequisicao = signal<{ materialId: string; nome: string; quantidade: number }[]>([]);
  protected readonly itensTransferir = signal<{ loteId: string; rotulo: string; quantidade: number }[]>([]);
  protected readonly quantidades = signal<Record<string, string>>({});
  protected readonly motivosDivergencia = signal<Record<string, MotivoDivergenciaEstoque | ''>>({});
  protected readonly motivoAcao = signal('');
  protected readonly matricula = signal('');
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
  protected readonly requisicaoForm = this.fb.nonNullable.group({ centralId: [''], prioridade: ['NORMAL' as PrioridadeRequisicaoEstoque], observacao: [''], materialId: [''], quantidade: [''] });
  protected readonly transferirForm = this.fb.nonNullable.group({ destinoId: [''], loteId: [''], quantidade: [''], observacao: [''] });

  /** O material escolhido na entrada decide se pede lote e validade. */
  protected readonly materialDaEntrada = signal<MaterialEstoqueDto | null>(null);

  protected readonly itensFiltrados = computed(() => {
    const b = this.busca().trim().toLowerCase();
    return (this.saldo()?.itens ?? []).filter((i) => !b || i.nome.toLowerCase().includes(b) || i.codigo.toLowerCase().includes(b));
  });
  protected readonly materiaisAtivos = computed(() => this.materiais().filter((m) => m.ativo));

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.authService.usuarioAtual().subscribe((u) => this.matricula.set(u?.profissionalMatricula ?? ''));
    this.service.centrais().pipe(catchError(() => of([]))).subscribe((c) => this.centrais.set(c));
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
    this.carregarCentral();
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
    if (a === 'requisicoes') return this.requisicoes().filter((r) => r.situacao === 'ENVIADA' || r.situacao === 'ATENDIDA').length || null;
    if (a === 'transferencias') return this.transferencias().filter((t) => t.situacao === 'EM_TRANSITO').length || null;
    if (a === 'inventario') return this.inventarios().filter((i) => i.situacao === 'ABERTO').length || null;
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
    if (this.salvarCentral(g, erros)) return;
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

  /** Consumo sem saldo conferido pelo almoxarife: sai do alerta (ADR-0183). */
  protected conferirConsumo(c: ConsumoSemSaldoDto): void {
    if (this.submitting()) return;
    this.enviar(this.service.conferirConsumoSemSaldo(c.uuid), 'Consumo conferido', () => `${c.materialNome} · ${c.quantidadeFaltante}`);
  }

  protected dataHoraBr(iso: string): string {
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`;
  }


  // ── Central, requisição, transferência e inventário (ADR-0189) ──

  protected ehCentral(): boolean {
    return this.centrais().some((c) => c.unidadeId === this.unidadeId());
  }

  private carregarCentral(): void {
    const id = this.unidadeId();
    if (!id) return;
    if (this.pode('ESTOQUE.REQUISITAR', 'ESTOQUE.ATENDER', 'ESTOQUE.CONSULTAR', 'ESTOQUE.MOVIMENTAR', 'ESTOQUE.GERENCIAR')) {
      forkJoin({
        daUnidade: this.service.requisicoes({ unidadeId: id }).pipe(catchError(() => of([]))),
        daCentral: this.service.requisicoes({ centralId: id }).pipe(catchError(() => of([]))),
      }).subscribe(({ daUnidade, daCentral }) => {
        const vistos = new Set<string>();
        this.requisicoes.set([...daCentral, ...daUnidade].filter((r) => !vistos.has(r.uuid) && !!vistos.add(r.uuid)));
      });
      this.service.transferencias(id).pipe(catchError(() => of([]))).subscribe((t) => this.transferencias.set(t));
      this.service.inventarios(id).pipe(catchError(() => of([]))).subscribe((i) => this.inventarios.set(i));
    }
  }

  protected abrirRequisicao(): void {
    this.requisicaoForm.reset({ centralId: this.centrais().find((c) => c.unidadeId !== this.unidadeId())?.unidadeId ?? '', prioridade: 'NORMAL', observacao: '', materialId: '', quantidade: '' });
    this.itensRequisicao.set([]);
    this.abrir({ tipo: 'requisicao' });
  }

  protected incluirItemRequisicao(): void {
    const v = this.requisicaoForm.getRawValue();
    const qtd = this.inteiro(v.quantidade);
    const erros: Record<string, string> = {};
    if (!v.materialId) erros['materialRequisicao'] = 'Escolha o material.';
    if (qtd == null || qtd < 1) erros['quantidadeRequisicao'] = 'Informe a quantidade.';
    if (v.materialId && this.itensRequisicao().some((i) => i.materialId === v.materialId)) erros['materialRequisicao'] = 'O material já está na lista.';
    if (!this.validar(erros)) return;
    const m = this.materiais().find((x) => x.uuid === v.materialId);
    this.itensRequisicao.update((l) => [...l, { materialId: v.materialId, nome: m?.nome ?? '', quantidade: qtd! }]);
    this.requisicaoForm.patchValue({ materialId: '', quantidade: '' });
  }

  protected removerItemRequisicao(materialId: string): void {
    this.itensRequisicao.update((l) => l.filter((i) => i.materialId !== materialId));
  }

  /** Abre o atendimento da central (com os saldos) ou o recebimento da transferência. */
  protected abrirAtender(r: RequisicaoEstoqueDto): void {
    this.service.requisicao(r.uuid).subscribe((d) => {
      const q: Record<string, string> = {};
      d.itens.forEach((i) => (q[i.materialId] = String(Math.min(i.quantidadePedida, i.saldoNaCentral))));
      this.quantidades.set(q);
      this.motivoAcao.set('');
      this.abrir({ tipo: 'atender', requisicao: d });
    });
  }

  protected abrirMotivoRequisicao(r: RequisicaoEstoqueDto, acao: 'cancelamento' | 'negacao'): void {
    this.motivoAcao.set('');
    this.abrir({ tipo: 'motivoRequisicao', requisicao: r, acao });
  }

  protected abrirReceber(t: TransferenciaEstoqueDto): void {
    const q: Record<string, string> = {};
    const m: Record<string, MotivoDivergenciaEstoque | ''> = {};
    t.itens.forEach((i) => { q[i.uuid] = String(i.quantidadeEnviada); m[i.uuid] = ''; });
    this.quantidades.set(q);
    this.motivosDivergencia.set(m);
    this.abrir({ tipo: 'receber', transferencia: t });
  }

  protected abrirTransferir(): void {
    this.transferirForm.reset({ destinoId: '', loteId: '', quantidade: '', observacao: '' });
    this.itensTransferir.set([]);
    this.abrir({ tipo: 'transferir' });
  }

  /** Os lotes com saldo da unidade, para a transferência avulsa. */
  protected lotesDaUnidade(): { uuid: string; rotulo: string; quantidade: number }[] {
    return (this.saldo()?.itens ?? []).flatMap((i) => i.lotes.filter((l) => l.quantidade > 0 && !l.vencido)
      .map((l) => ({ uuid: l.uuid, rotulo: `${i.nome}${l.numeroLote ? ' · lote ' + l.numeroLote : ''} (saldo ${l.quantidade})`, quantidade: l.quantidade })));
  }

  protected incluirItemTransferir(): void {
    const v = this.transferirForm.getRawValue();
    const qtd = this.inteiro(v.quantidade);
    const lote = this.lotesDaUnidade().find((l) => l.uuid === v.loteId);
    const erros: Record<string, string> = {};
    if (!lote) erros['loteTransferir'] = 'Escolha o lote.';
    if (qtd == null || qtd < 1) erros['quantidadeTransferir'] = 'Informe a quantidade.';
    else if (lote && qtd > lote.quantidade) erros['quantidadeTransferir'] = `O lote tem ${lote.quantidade}.`;
    if (!this.validar(erros)) return;
    this.itensTransferir.update((l) => [...l.filter((x) => x.loteId !== v.loteId), { loteId: v.loteId, rotulo: lote!.rotulo, quantidade: qtd! }]);
    this.transferirForm.patchValue({ loteId: '', quantidade: '' });
  }

  protected abrirInventario(i: InventarioEstoqueDto | null): void {
    if (!i) {
      this.abrir({ tipo: 'inventario', inventario: null });
      return;
    }
    this.service.inventario(i.uuid).subscribe((d) => {
      const q: Record<string, string> = {};
      d.itens.forEach((x) => (q[x.uuid] = x.quantidadeContada != null ? String(x.quantidadeContada) : ''));
      this.quantidades.set(q);
      this.motivoAcao.set('');
      this.abrir({ tipo: 'inventario', inventario: d });
    });
  }

  protected definirQuantidade(chave: string, valor: string): void {
    this.quantidades.update((q) => ({ ...q, [chave]: valor }));
  }

  protected definirMotivoDivergencia(chave: string, valor: string): void {
    this.motivosDivergencia.update((m) => ({ ...m, [chave]: valor as MotivoDivergenciaEstoque | '' }));
  }

  /** Ações do inventário aberto que mantêm a gaveta aberta (contagem) ou a fecham (fechamento, cancelamento). */
  protected acaoInventario(i: InventarioEstoqueDto, acao: 'contagem' | 'fechamento' | 'cancelamento'): void {
    if (!this.matricula().trim()) { this.erroApi.set('Informe a sua matrícula.'); return; }
    const itens = acao === 'contagem' ? i.itens.filter((x) => this.quantidades()[x.uuid] !== '' && this.quantidades()[x.uuid] != null)
      .map((x) => ({ itemId: x.uuid, quantidade: Number(this.quantidades()[x.uuid]) })) : [];
    if (acao === 'contagem' && !itens.length) { this.erroApi.set('Informe ao menos uma contagem.'); return; }
    if (acao === 'cancelamento' && !this.motivoAcao().trim()) { this.erroApi.set('Informe o motivo do cancelamento.'); return; }
    const req = this.service.acaoInventario(i.uuid, acao, { profissionalMatricula: this.matricula().trim(), itens, motivo: this.motivoAcao().trim() || null });
    if (acao === 'contagem') {
      this.submitting.set(true);
      req.subscribe({
        next: (d) => { this.submitting.set(false); this.erroApi.set(null); this.gaveta.set({ tipo: 'inventario', inventario: d });
          this.toast.set({ titulo: 'Contagem registrada', detalhe: `${d.contados} de ${d.lotes} lote(s)` }); this.carregarCentral(); },
        error: (e: HttpErrorResponse) => { this.submitting.set(false); this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar.'); },
      });
      return;
    }
    this.enviar(req, acao === 'fechamento' ? 'Inventário fechado' : 'Inventário cancelado', (d) => `${d.comDiferenca} lote(s) com diferença`);
  }

  private salvarCentral(g: Gaveta, erros: Record<string, string>): boolean {
    if (!['requisicao', 'atender', 'motivoRequisicao', 'receber', 'transferir', 'inventario'].includes(g.tipo)) return false;
    const matricula = this.matricula().trim();
    if (!matricula) erros['matriculaCentral'] = 'Informe a sua matrícula.';
    const unidadeId = this.unidadeId();
    switch (g.tipo) {
      case 'requisicao': {
        const v = this.requisicaoForm.getRawValue();
        if (!v.centralId) erros['centralRequisicao'] = 'Escolha a central.';
        if (!this.itensRequisicao().length) erros['materialRequisicao'] = 'Inclua ao menos um material.';
        if (!this.validar(erros)) return true;
        this.submitting.set(true);
        this.service.criarRequisicao({ profissionalMatricula: matricula, unidadeId, centralId: v.centralId, prioridade: v.prioridade, observacao: v.observacao.trim() || null,
          itens: this.itensRequisicao().map((i) => ({ materialId: i.materialId, quantidade: i.quantidade })) }).subscribe({
          next: (r) => this.enviar(this.service.acaoRequisicao(r.uuid, 'envio', { profissionalMatricula: matricula }), 'Requisição enviada', (x) => `${x.centralNome} · ${x.itens.length} material(is)`),
          error: (e: HttpErrorResponse) => { this.submitting.set(false); this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar.'); },
        });
        return true;
      }
      case 'atender': {
        const itens = g.requisicao.itens.map((i) => ({ materialId: i.materialId, quantidade: this.inteiro(this.quantidades()[i.materialId] ?? '0') ?? -1 }));
        if (itens.some((i) => i.quantidade < 0)) erros['quantidadesAtender'] = 'Informe quantidades inteiras.';
        if (!this.validar(erros)) return true;
        this.enviar(this.service.acaoRequisicao(g.requisicao.uuid, 'atendimento', { profissionalMatricula: matricula, itens }), 'Requisição atendida',
          (r) => `${r.unidadeNome} · em trânsito`);
        return true;
      }
      case 'motivoRequisicao': {
        if (!this.motivoAcao().trim()) erros['motivoRequisicao'] = 'Informe o motivo.';
        if (!this.validar(erros)) return true;
        this.enviar(this.service.acaoRequisicao(g.requisicao.uuid, g.acao, { profissionalMatricula: matricula, motivo: this.motivoAcao().trim() }),
          g.acao === 'negacao' ? 'Requisição negada' : 'Requisição cancelada', (r) => r.unidadeNome);
        return true;
      }
      case 'receber': {
        const itens = g.transferencia.itens.map((i) => ({ itemId: i.uuid, quantidade: this.inteiro(this.quantidades()[i.uuid] ?? '') ?? -1,
          motivoDivergencia: this.motivosDivergencia()[i.uuid] || null, enviada: i.quantidadeEnviada }));
        if (itens.some((i) => i.quantidade < 0 || i.quantidade > i.enviada)) erros['quantidadesReceber'] = 'Informe o recebido de cada item, até o enviado.';
        else if (itens.some((i) => i.quantidade < i.enviada && !i.motivoDivergencia)) erros['quantidadesReceber'] = 'Informe o motivo de cada diferença.';
        if (!this.validar(erros)) return true;
        this.enviar(this.service.receber(g.transferencia.uuid, { profissionalMatricula: matricula,
          itens: itens.map((i) => ({ itemId: i.itemId, quantidade: i.quantidade, motivoDivergencia: i.quantidade < i.enviada ? i.motivoDivergencia : null })) }),
          'Transferência recebida', (t) => `${t.origemNome}${t.comDivergencia ? ' · com divergência' : ''}`);
        return true;
      }
      case 'transferir': {
        const v = this.transferirForm.getRawValue();
        if (!v.destinoId) erros['destinoTransferir'] = 'Escolha o destino.';
        if (!this.itensTransferir().length) erros['loteTransferir'] = 'Inclua ao menos um lote.';
        if (!this.validar(erros)) return true;
        this.enviar(this.service.transferir({ profissionalMatricula: matricula, unidadeId, destinoId: v.destinoId, observacao: v.observacao.trim() || null,
          itens: this.itensTransferir().map((i) => ({ loteId: i.loteId, quantidade: i.quantidade })) }), 'Transferência enviada', (t) => t.destinoNome);
        return true;
      }
      case 'inventario': {
        if (g.inventario) return true;
        if (!this.validar(erros)) return true;
        this.enviar(this.service.abrirInventario({ profissionalMatricula: matricula, unidadeId }), 'Inventário aberto', (i) => `${i.lotes} lote(s) para contar`);
        return true;
      }
      default:
        return false;
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
