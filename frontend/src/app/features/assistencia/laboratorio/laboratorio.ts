import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AcessoDaInterface } from '../../../core/models/auth';
import {
  CLASSIFICACOES_LAUDO,
  FORMAS_ENTREGA,
  MODALIDADES,
  tipoDoExame,
  CategoriaExame,
  ClassificacaoLaudo,
  ModalidadeImagem,
  INTERPRETACOES,
  MEIOS_COMUNICACAO,
  MATERIAIS,
  MOTIVOS_REJEICAO,
  STATUS_ITEM,
  UNIDADES_IDADE,
  critico,
  faixaCritica,
  faixaReferencia,
  numeroLaboratorio,
  valorResultado,
  EtapaTrabalho,
  ExameLaboratorialResponseDto,
  FaixaReferenciaExameDto,
  FaixaReferenciaExameRequestDto,
  FormaEntregaResultado,
  ItemPedidoExameDto,
  ItemTrabalhoExameDto,
  MaterialExame,
  MeioComunicacaoCritico,
  MotivoRejeicaoAmostra,
  PedidoExameResponseDto,
  PedidoExameResumoDto,
  PrioridadeExame,
  ResultadoAvisoExameDto,
  SituacaoPedidoExame,
  StatusItemExame,
  TipoEventoExame,
  TipoResultadoExame,
  UnidadeIdade,
} from '../../../core/models/laboratorio';
import { PacienteResponseDto, Sexo } from '../../../core/models/paciente';
import { ErrorResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { LaboratorioService } from '../../../core/services/laboratorio';
import { PacienteService } from '../../../core/services/paciente';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { EstoqueService } from '../../../core/services/estoque';
import { MaterialEstoqueDto } from '../../../core/models/estoque';
import { EmissaoLaudoDto, InsumoExameDto } from '../../../core/models/laboratorio';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';

type Aba = 'coletar' | 'analise' | 'liberar' | 'realizar' | 'laudar' | 'criticos' | 'ciencia' | 'pedidos' | 'catalogo';

type Gaveta =
  | { tipo: 'pedido' }
  | { tipo: 'coleta'; grupo: GrupoColeta }
  | { tipo: 'resultado'; itemId: string; pedidoId: string }
  | { tipo: 'liberar'; itemId: string; pedidoId: string }
  | { tipo: 'rejeitar'; amostraCodigo: string; pedidoId: string }
  | { tipo: 'detalhe'; pedidoId: string }
  | { tipo: 'retificar'; itemId: string; pedidoId: string }
  | { tipo: 'cancelar'; itemId: string; pedidoId: string; exameNome: string }
  | { tipo: 'exame'; exame: ExameLaboratorialResponseDto | null }
  | { tipo: 'comunicar'; aviso: ResultadoAvisoExameDto }
  | { tipo: 'entrega'; pedidoId: string }
  | { tipo: 'realizacao'; itemId: string; pedidoId: string }
  | { tipo: 'laudo'; itemId: string; pedidoId: string; adendo: boolean };

/** Os exames de um pedido que aguardam coleta: a coleta é por pedido. */
interface GrupoColeta {
  pedidoId: string;
  pacienteNome: string;
  unidadeSolicitanteNome: string;
  prioridade: PrioridadeExame;
  solicitadoEm: string;
  itens: ItemTrabalhoExameDto[];
  preparos: string[];
}

const SITUACOES: Record<SituacaoPedidoExame, { classe: string; rotulo: string }> = {
  AGUARDANDO_COLETA: { classe: 'info', rotulo: 'Aguardando coleta' },
  EM_ANDAMENTO: { classe: 'warn', rotulo: 'Em andamento' },
  CONCLUIDO: { classe: 'ok', rotulo: 'Concluído' },
  CANCELADO: { classe: 'muted', rotulo: 'Cancelado' },
};

const EVENTOS: Record<TipoEventoExame, string> = {
  SOLICITACAO: 'Pedido feito',
  COLETA: 'Coleta',
  REJEICAO_AMOSTRA: 'Amostra rejeitada',
  RESULTADO: 'Resultado registrado',
  LIBERACAO: 'Resultado liberado',
  RETIFICACAO: 'Resultado retificado',
  CANCELAMENTO: 'Exame cancelado',
  COMUNICACAO_CRITICO: 'Crítico comunicado',
  CIENCIA: 'Ciência de quem pediu',
  ENTREGA: 'Resultado entregue',
  REALIZACAO: 'Exame realizado',
  LAUDO: 'Laudo liberado',
  ADENDO: 'Adendo ao laudo',
};

const CID = /^\s*([A-Za-z][0-9]{2}(\.?[0-9A-Za-z]{1,2})?)?\s*$/;

/**
 * Laboratório assistencial (ADR-0094): listas de trabalho por etapa (coletar, analisar, liberar), pedidos e
 * catálogo, com as gavetas de pedido, coleta, resultado, liberação, rejeição de amostra, retificação, cancelamento e
 * exame do catálogo. O backend é a ADR-0093.
 */
@Component({
  selector: 'app-laboratorio',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './laboratorio.html',
  styleUrl: './laboratorio.css',
})
export class Laboratorio {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly laboratorioService = inject(LaboratorioService);
  private readonly pacienteService = inject(PacienteService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly estoqueService = inject(EstoqueService);

  protected readonly formatCpf = formatCpf;
  protected readonly materiais = MATERIAIS;
  protected readonly listaMateriais = Object.keys(MATERIAIS) as MaterialExame[];
  protected readonly modalidades = MODALIDADES;
  protected readonly listaModalidades = Object.keys(MODALIDADES) as ModalidadeImagem[];
  protected readonly classificacoesLaudo = CLASSIFICACOES_LAUDO;
  protected readonly listaClassificacoes = Object.keys(CLASSIFICACOES_LAUDO) as ClassificacaoLaudo[];
  protected readonly tipoDoExame = tipoDoExame;
  protected readonly statusItem = STATUS_ITEM;
  protected readonly situacoes = SITUACOES;
  protected readonly interpretacoes = INTERPRETACOES;
  protected readonly motivosRejeicao = MOTIVOS_REJEICAO;
  protected readonly listaMotivos = Object.keys(MOTIVOS_REJEICAO) as MotivoRejeicaoAmostra[];
  protected readonly eventos = EVENTOS;
  protected readonly unidadesIdade = UNIDADES_IDADE;
  protected readonly listaUnidadesIdade = Object.keys(UNIDADES_IDADE) as UnidadeIdade[];
  protected readonly critico = critico;
  protected readonly faixaCritica = faixaCritica;
  protected readonly meiosComunicacao = MEIOS_COMUNICACAO;
  protected readonly listaMeios = Object.keys(MEIOS_COMUNICACAO) as MeioComunicacaoCritico[];
  protected readonly formasEntrega = FORMAS_ENTREGA;
  protected readonly listaFormas = Object.keys(FORMAS_ENTREGA) as FormaEntregaResultado[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  private readonly matriculaPadrao = signal('');

  protected readonly paraColetar = signal<ItemTrabalhoExameDto[]>([]);
  protected readonly emAnalise = signal<ItemTrabalhoExameDto[]>([]);
  protected readonly paraLiberar = signal<ItemTrabalhoExameDto[]>([]);
  /** Exames de imagem a realizar e a laudar (ADR-0182). */
  protected readonly paraRealizar = signal<ItemTrabalhoExameDto[]>([]);
  protected readonly paraLaudar = signal<ItemTrabalhoExameDto[]>([]);
  /** Resultados para a ciência de quem pediu e críticos a comunicar (ADR-0181). */
  protected readonly paraCiencia = signal<ResultadoAvisoExameDto[]>([]);
  protected readonly criticosAComunicar = signal<ResultadoAvisoExameDto[]>([]);
  protected readonly matriculaCiencia = signal('');
  protected readonly erroCiencia = signal<string | null>(null);
  protected readonly pedidos = signal<PedidoExameResumoDto[]>([]);
  protected readonly exames = signal<ExameLaboratorialResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly aba = signal<Aba>('pedidos');
  protected readonly busca = signal('');
  protected readonly filtroSituacao = signal<SituacaoPedidoExame | ''>('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly detalhe = signal<PedidoExameResponseDto | null>(null);
  protected readonly erroDetalhe = signal<string | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  /** Exames marcados na gaveta de pedido; itens marcados na coleta. */
  protected readonly examesMarcados = signal<Set<string>>(new Set());
  protected readonly itensMarcados = signal<Set<string>>(new Set());

  protected readonly pedidoForm = this.fb.nonNullable.group({
    buscaPaciente: [''],
    pacienteId: [''],
    atendimentoId: [''],
    unidadeSolicitanteId: [''],
    buscaExame: [''],
    indicacaoClinica: [''],
    cid: [''],
    prioridade: ['ROTINA' as PrioridadeExame],
    profissionalMatricula: [''],
    unidadeExecutanteImagemId: [''],
  });

  protected readonly realizacaoForm = this.fb.nonNullable.group({
    realizadaEm: [''], equipamento: [''], contraste: [false], contrasteDescricao: [''], observacao: [''], profissionalMatricula: [''],
  });
  protected readonly laudoForm = this.fb.nonNullable.group({
    tecnica: [''], achados: [''], conclusao: [''], classificacao: ['NORMAL' as ClassificacaoLaudo], imagemUrl: [''], motivo: [''],
    profissionalMatricula: [''],
  });

  protected readonly coletaForm = this.fb.nonNullable.group({ unidadeColetaId: [''], laboratorioId: [''], profissionalMatricula: [''] });
  protected readonly resultadoForm = this.fb.nonNullable.group({ valorNumerico: [''], valorTexto: [''], observacao: [''], profissionalMatricula: [''] });
  protected readonly liberarForm = this.fb.nonNullable.group({ profissionalMatricula: [''] });
  protected readonly rejeitarForm = this.fb.nonNullable.group({ motivo: ['HEMOLISADA' as MotivoRejeicaoAmostra], observacao: [''], profissionalMatricula: [''] });
  protected readonly retificarForm = this.fb.nonNullable.group({
    valorNumerico: [''], valorTexto: [''], observacao: [''], motivo: [''], profissionalMatricula: [''],
  });
  protected readonly cancelarForm = this.fb.nonNullable.group({ motivo: [''], profissionalMatricula: [''] });
  protected readonly comunicarForm = this.fb.nonNullable.group({
    receptorNome: [''], meio: ['TELEFONE' as MeioComunicacaoCritico], comunicadoEm: [''], profissionalMatricula: [''],
  });
  protected readonly entregaForm = this.fb.nonNullable.group({
    unidadeId: [''], retirada: ['PACIENTE' as 'PACIENTE' | 'TERCEIRO'], terceiroNome: [''], terceiroDocumento: [''], terceiroParentesco: [''],
    forma: ['IMPRESSO_NA_UNIDADE' as FormaEntregaResultado], profissionalMatricula: [''],
  });
  protected readonly matriculaCienciaForm = this.fb.nonNullable.group({ matricula: [''] });
  protected readonly exameForm = this.fb.nonNullable.group({
    nome: [''],
    categoria: ['LABORATORIAL' as CategoriaExame],
    modalidade: ['RAIO_X' as ModalidadeImagem],
    material: ['SANGUE' as MaterialExame],
    tipoResultado: ['NUMERICO' as TipoResultadoExame],
    unidadeMedida: [''],
    referenciaMinima: [''],
    referenciaMaxima: [''],
    criticoMinimo: [''],
    criticoMaximo: [''],
    referenciaTexto: [''],
    preparo: [''],
    prazoDias: [''],
    ativo: [true],
  });

  /** Faixa por sexo e idade em edição na gaveta do exame (ADR-0180): nula fechada, vazia nova, uuid editando. */
  protected readonly faixaEditando = signal<string | null>(null);
  protected readonly erroFaixa = signal<string | null>(null);
  protected readonly salvandoFaixa = signal(false);

  /** Laudo em PDF e insumos (ADR-0183). */
  protected readonly emissoes = signal<EmissaoLaudoDto[]>([]);
  protected readonly baixandoLaudo = signal(false);
  protected readonly materiaisEstoque = signal<MaterialEstoqueDto[]>([]);
  protected readonly erroInsumo = signal<string | null>(null);
  protected readonly salvandoInsumo = signal(false);
  protected readonly insumoForm = this.fb.nonNullable.group({ materialId: [''], quantidade: ['1'], soComContraste: [false] });
  protected readonly faixaForm = this.fb.nonNullable.group({
    sexo: ['' as Sexo | ''],
    idadeMinima: ['0'],
    unidadeIdadeMinima: ['ANOS' as UnidadeIdade],
    idadeMaxima: [''],
    unidadeIdadeMaxima: ['ANOS' as UnidadeIdade],
    referenciaMinima: [''],
    referenciaMaxima: [''],
    criticoMinimo: [''],
    criticoMaximo: [''],
    descricao: [''],
  });

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────

  protected readonly abas = computed<{ id: Aba; rotulo: string; contagem: number | null }[]>(() => [
    ...(this.pode('LABORATORIO.COLETAR') ? [{ id: 'coletar' as Aba, rotulo: 'Para coletar', contagem: this.gruposColeta().length }] : []),
    ...(this.pode('LABORATORIO.ANALISAR') ? [{ id: 'analise' as Aba, rotulo: 'Em análise', contagem: this.emAnalise().length }] : []),
    ...(this.pode('LABORATORIO.LIBERAR') ? [{ id: 'liberar' as Aba, rotulo: 'Para liberar', contagem: this.paraLiberar().length }] : []),
    ...(this.pode('IMAGEM.REALIZAR') ? [{ id: 'realizar' as Aba, rotulo: 'Para realizar', contagem: this.paraRealizar().length }] : []),
    ...(this.pode('IMAGEM.LAUDAR') ? [{ id: 'laudar' as Aba, rotulo: 'Para laudar', contagem: this.paraLaudar().length }] : []),
    ...(this.pode('LABORATORIO.LIBERAR', 'IMAGEM.LAUDAR') ? [{ id: 'criticos' as Aba, rotulo: 'Críticos a comunicar', contagem: this.criticosAComunicar().length }] : []),
    ...(this.pode('EXAME.SOLICITAR') ? [{ id: 'ciencia' as Aba, rotulo: 'Para ciência', contagem: this.paraCiencia().length }] : []),
    { id: 'pedidos', rotulo: 'Pedidos', contagem: this.pedidos().length },
    { id: 'catalogo', rotulo: 'Catálogo de exames', contagem: this.exames().length },
  ]);

  protected readonly gruposColeta = computed<GrupoColeta[]>(() => {
    const mapa = new Map<string, GrupoColeta>();
    for (const i of this.paraColetar()) {
      const g = mapa.get(i.pedidoId) ?? {
        pedidoId: i.pedidoId,
        pacienteNome: i.pacienteNome,
        unidadeSolicitanteNome: i.unidadeSolicitanteNome,
        prioridade: i.prioridade,
        solicitadoEm: i.solicitadoEm,
        itens: [],
        preparos: [],
      };
      g.itens.push(i);
      if (i.preparo && !g.preparos.includes(i.preparo)) g.preparos.push(i.preparo);
      mapa.set(i.pedidoId, g);
    }
    return [...mapa.values()];
  });

  protected readonly pedidosFiltrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const s = this.filtroSituacao();
    return this.pedidos()
      .filter((p) => !s || p.situacao === s)
      .filter((p) => !q || `${p.pacienteNome} ${p.unidadeSolicitanteNome} ${p.itens.map((i) => i.exameNome).join(' ')}`.toLowerCase().includes(q));
  });

  protected readonly resumo = computed(() => ({
    coletar: this.paraColetar().length,
    analise: this.emAnalise().length,
    liberar: this.paraLiberar().length,
    urgentes: [...this.paraColetar(), ...this.emAnalise(), ...this.paraLiberar()].filter((i) => i.prioridade === 'URGENTE').length,
  }));

  protected readonly examesAtivos = computed(() => this.exames().filter((e) => e.ativo));

  /** O item e o exame do catálogo da gaveta aberta (resultado, liberação, retificação). */
  protected readonly itemAtual = computed<{ item: ItemPedidoExameDto; exame: ExameLaboratorialResponseDto | undefined } | null>(() => {
    const g = this.gaveta();
    const d = this.detalhe();
    if (!g || !d || !('itemId' in g)) return null;
    const item = d.itens.find((i) => i.uuid === g.itemId);
    return item ? { item, exame: this.exames().find((e) => e.uuid === item.exameId) } : null;
  });

  constructor() {
    this.authService.usuarioAtual().subscribe((u) => {
      this.matriculaPadrao.set(u?.profissionalMatricula ?? '');
      // Com login, a lista de ciência é sempre a do profissional logado (ADR-0181).
      if (u?.profissionalMatricula) this.verParaCiencia(u.profissionalMatricula);
    });
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      const pedida = this.route.snapshot.queryParamMap.get('aba') as Aba | null;
      const primeira = this.abas()[0]?.id ?? 'pedidos';
      this.aba.set(pedida && this.abas().some((x) => x.id === pedida) ? pedida : primeira);
    });
    this.carregarTudo();
    this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    this.unidadeSaudeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    // "Solicitar exames" no atendimento abre o pedido já com paciente, unidade e atendimento (ADR-0094).
    const q = this.route.snapshot.queryParamMap;
    if (q.get('acao') === 'pedido') {
      this.abrirPedido(q.get('pacienteId') ?? '', q.get('unidadeId') ?? '', q.get('atendimentoId') ?? '');
    }
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  private carregarTudo(): void {
    this.carregando.set(true);
    const etapa = (e: EtapaTrabalho) => this.laboratorioService.trabalho(e).pipe(catchError(() => of([])));
    forkJoin({
      coletar: etapa('PARA_COLETAR'),
      analise: etapa('EM_ANALISE'),
      liberar: etapa('PARA_LIBERAR'),
      realizar: this.pode('IMAGEM.REALIZAR') ? etapa('PARA_REALIZAR') : of([]),
      laudar: this.pode('IMAGEM.LAUDAR') ? etapa('PARA_LAUDAR') : of([]),
      pedidos: this.laboratorioService.listarPedidos().pipe(catchError(() => of([]))),
      exames: this.laboratorioService.listarExames().pipe(catchError(() => of([]))),
      criticos: this.pode('LABORATORIO.LIBERAR', 'IMAGEM.LAUDAR') ? this.laboratorioService.criticosAComunicar().pipe(catchError(() => of([]))) : of([]),
    }).subscribe(({ coletar, analise, liberar, realizar, laudar, pedidos, exames, criticos }) => {
      this.paraRealizar.set(realizar);
      this.paraLaudar.set(laudar);
      this.paraColetar.set(coletar);
      this.emAnalise.set(analise);
      this.paraLiberar.set(liberar);
      this.criticosAComunicar.set(criticos);
      this.pedidos.set(pedidos);
      this.exames.set(exames);
      this.carregando.set(false);
    });
    if (this.matriculaCiencia()) this.verParaCiencia(this.matriculaCiencia());
  }

  // ── Ciência de quem pediu (ADR-0181) ───────────────────────────────────────────────────────

  protected verParaCiencia(matricula: string): void {
    const m = matricula.trim();
    this.erroCiencia.set(null);
    if (!m) {
      this.erroCiencia.set('Informe a sua matrícula para ver os resultados dos seus pedidos.');
      return;
    }
    this.matriculaCiencia.set(m);
    this.matriculaCienciaForm.setValue({ matricula: m });
    this.laboratorioService.paraCiencia(m).subscribe({
      next: (lista) => this.paraCiencia.set(lista),
      error: (e: HttpErrorResponse) => {
        this.paraCiencia.set([]);
        this.erroCiencia.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar os resultados.');
      },
    });
  }

  protected darCiencia(a: ResultadoAvisoExameDto): void {
    if (this.submitting()) return;
    this.submitting.set(true);
    this.laboratorioService.darCiencia(a.itemId, this.matriculaCiencia()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.mostrarToast('Ciência registrada', `${a.exameNome} · ${a.pacienteNome}`);
        this.carregarTudo();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroCiencia.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível registrar a ciência.');
      },
    });
  }

  /** No detalhe: ciência de todos os liberados do pedido, por quem pediu. */
  protected darCienciaDoPedido(d: PedidoExameResponseDto): void {
    const matricula = this.matriculaCiencia() || this.matriculaPadrao() || d.profissionalSolicitanteMatricula;
    this.enviar(this.laboratorioService.darCienciaDoPedido(d.uuid, matricula), 'Ciência registrada', d.pacienteNome);
  }

  protected readonly exameNomeDe = (i: ItemPedidoExameDto) => i.exameNome;

  protected pendentesDeCiencia(d: PedidoExameResponseDto): ItemPedidoExameDto[] {
    return d.itens.filter((i) => i.status === 'LIBERADO' && i.resultado && !i.resultado.cienciaEm);
  }

  /** Críticos liberados que o médico ainda não viu: a entrega mostra o aviso. */
  protected criticosSemCiencia(d: PedidoExameResponseDto): ItemPedidoExameDto[] {
    return this.pendentesDeCiencia(d).filter((i) => critico(i.resultado?.interpretacao));
  }

  protected selecionarAba(aba: Aba, focar = false): void {
    this.aba.set(aba);
    if (focar) document.getElementById('tab-' + aba)?.focus();
  }

  protected navegarAbas(event: KeyboardEvent, indice: number): void {
    if (event.key !== 'ArrowRight' && event.key !== 'ArrowLeft') return;
    event.preventDefault();
    const abas = this.abas();
    this.selecionarAba(abas[(indice + (event.key === 'ArrowRight' ? 1 : -1) + abas.length) % abas.length].id, true);
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────

  protected formatarInstante(iso: string | null | undefined): string {
    if (!iso) return '—';
    const d = new Date(iso);
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} · ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  protected formatarDataHora(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [data, hora] = iso.split('T');
    const [y, m, d] = data.split('-');
    return `${d}/${m}/${y} · ${(hora ?? '').slice(0, 5)}`;
  }

  protected readonly numero = numeroLaboratorio;
  protected readonly faixa = faixaReferencia;

  protected valorDe(i: ItemPedidoExameDto): string {
    return valorResultado(i.resultado);
  }

  /** Itens de recoleta no grupo da coleta (ADR-0095). */
  protected recoletas(g: GrupoColeta): ItemTrabalhoExameDto[] {
    return g.itens.filter((i) => i.motivoRecoleta);
  }

  protected exameDoCatalogo(nome: string): ExameLaboratorialResponseDto | undefined {
    return this.exames().find((e) => e.nome === nome);
  }

  protected pacientesFiltrados(): PacienteResponseDto[] {
    const q = this.pedidoForm.controls.buscaPaciente.value.trim().toLowerCase();
    const digitos = q.replace(/\D/g, '');
    const selecionado = this.pedidoForm.controls.pacienteId.value;
    const lista = this.pacientes().filter((p) => !q || p.nome.toLowerCase().includes(q) || (digitos.length > 0 && (p.cpf ?? '').replace(/\D/g, '').includes(digitos)));
    const limitada = lista.slice(0, 50);
    const atual = this.pacientes().find((p) => p.uuid === selecionado);
    return atual && !limitada.includes(atual) ? [atual, ...limitada] : limitada;
  }

  protected examesParaMarcar(): ExameLaboratorialResponseDto[] {
    const q = this.pedidoForm.controls.buscaExame.value.trim().toLowerCase();
    return this.examesAtivos().filter((e) => !q || e.nome.toLowerCase().includes(q) || this.examesMarcados().has(e.uuid));
  }

  protected alternarExame(uuid: string): void {
    const s = new Set(this.examesMarcados());
    if (s.has(uuid)) s.delete(uuid);
    else s.add(uuid);
    this.examesMarcados.set(s);
  }

  protected alternarItem(uuid: string): void {
    const s = new Set(this.itensMarcados());
    if (s.has(uuid)) s.delete(uuid);
    else s.add(uuid);
    this.itensMarcados.set(s);
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'pedido':
        return 'Pedido de exames';
      case 'coleta':
        return 'Registrar coleta';
      case 'resultado':
        return 'Registrar resultado';
      case 'liberar':
        return 'Revisar e liberar';
      case 'rejeitar':
        return 'Rejeitar amostra';
      case 'detalhe':
        return 'Pedido de exames';
      case 'retificar':
        return 'Retificar resultado';
      case 'cancelar':
        return 'Cancelar exame';
      case 'exame':
        return g.exame ? 'Editar exame' : 'Novo exame';
      case 'comunicar':
        return 'Comunicar valor crítico';
      case 'entrega':
        return 'Registrar entrega';
      case 'realizacao':
        return 'Registrar realização';
      case 'laudo':
        return g.adendo ? 'Adendo ao laudo' : 'Laudo de imagem';
    }
  }

  private abrir(g: Gaveta, carregarPedido?: string): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
    if (carregarPedido) {
      this.detalhe.set(null);
      this.erroDetalhe.set(null);
      this.laboratorioService.buscarPedido(carregarPedido).subscribe({
        next: (d) => {
          this.detalhe.set(d);
          if (g.tipo === 'detalhe') this.carregarEmissoes(d.uuid);
        },
        error: (e: HttpErrorResponse) => this.erroDetalhe.set(e.status === 403 ? 'Este pedido é de unidades fora do seu acesso.' : 'Não foi possível abrir o pedido.'),
      });
    }
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
    this.detalhe.set(null);
  }

  protected abrirPedido(pacienteId = '', unidadeId = '', atendimentoId = ''): void {
    this.pedidoForm.reset({
      buscaPaciente: '', pacienteId, atendimentoId, unidadeSolicitanteId: unidadeId, buscaExame: '', indicacaoClinica: '', cid: '',
      prioridade: 'ROTINA', profissionalMatricula: this.matriculaPadrao(), unidadeExecutanteImagemId: '',
    });
    this.examesMarcados.set(new Set());
    this.abrir({ tipo: 'pedido' });
  }

  protected abrirColeta(grupo: GrupoColeta): void {
    const solicitante = this.unidades().find((u) => u.nome === grupo.unidadeSolicitanteNome)?.uuid ?? '';
    this.coletaForm.reset({ unidadeColetaId: solicitante, laboratorioId: '', profissionalMatricula: this.matriculaPadrao() });
    this.itensMarcados.set(new Set(grupo.itens.map((i) => i.itemId)));
    this.abrir({ tipo: 'coleta', grupo });
  }

  protected abrirResultado(i: { itemId: string; pedidoId: string }): void {
    this.resultadoForm.reset({ valorNumerico: '', valorTexto: '', observacao: '', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'resultado', itemId: i.itemId, pedidoId: i.pedidoId }, i.pedidoId);
  }

  protected abrirLiberacao(i: ItemTrabalhoExameDto): void {
    this.liberarForm.reset({ profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'liberar', itemId: i.itemId, pedidoId: i.pedidoId }, i.pedidoId);
  }

  protected abrirRejeicao(i: ItemTrabalhoExameDto): void {
    if (!i.amostraCodigo) return;
    this.rejeitarForm.reset({ motivo: 'HEMOLISADA', observacao: '', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'rejeitar', amostraCodigo: i.amostraCodigo, pedidoId: i.pedidoId }, i.pedidoId);
  }

  protected abrirDetalhe(pedidoId: string): void {
    this.abrir({ tipo: 'detalhe', pedidoId }, pedidoId);
  }

  protected abrirRetificacao(item: ItemPedidoExameDto, pedidoId: string): void {
    this.retificarForm.reset({
      valorNumerico: item.resultado?.valorNumerico != null ? String(item.resultado.valorNumerico) : '',
      valorTexto: item.resultado?.valorTexto ?? '',
      observacao: '',
      motivo: '',
      profissionalMatricula: this.matriculaPadrao(),
    });
    this.abrir({ tipo: 'retificar', itemId: item.uuid, pedidoId }, pedidoId);
  }

  protected abrirCancelamento(item: ItemPedidoExameDto, pedidoId: string): void {
    this.cancelarForm.reset({ motivo: '', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'cancelar', itemId: item.uuid, pedidoId, exameNome: item.exameNome });
  }

  protected abrirComunicacao(aviso: ResultadoAvisoExameDto): void {
    this.comunicarForm.reset({ receptorNome: '', meio: 'TELEFONE', comunicadoEm: '', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'comunicar', aviso });
  }

  protected abrirEntrega(d: PedidoExameResponseDto): void {
    this.entregaForm.reset({
      unidadeId: d.unidadeSolicitanteId, retirada: 'PACIENTE', terceiroNome: '', terceiroDocumento: '', terceiroParentesco: '',
      forma: 'IMPRESSO_NA_UNIDADE', profissionalMatricula: this.matriculaPadrao(),
    });
    this.abrir({ tipo: 'entrega', pedidoId: d.uuid }, d.uuid);
  }

  protected abrirExame(exame: ExameLaboratorialResponseDto | null = null): void {
    this.exameForm.reset({
      nome: exame?.nome ?? '',
      categoria: exame?.categoria ?? 'LABORATORIAL',
      modalidade: exame?.modalidade ?? 'RAIO_X',
      material: exame?.material ?? 'SANGUE',
      tipoResultado: exame?.tipoResultado ?? 'NUMERICO',
      unidadeMedida: exame?.unidadeMedida ?? '',
      referenciaMinima: exame?.referenciaMinima != null ? String(exame.referenciaMinima) : '',
      referenciaMaxima: exame?.referenciaMaxima != null ? String(exame.referenciaMaxima) : '',
      criticoMinimo: exame?.criticoMinimo != null ? String(exame.criticoMinimo) : '',
      criticoMaximo: exame?.criticoMaximo != null ? String(exame.criticoMaximo) : '',
      referenciaTexto: exame?.referenciaTexto ?? '',
      preparo: exame?.preparo ?? '',
      prazoDias: exame?.prazoDias != null ? String(exame.prazoDias) : '',
      ativo: exame?.ativo ?? true,
    });
    // A categoria não muda depois de criado (ADR-0182).
    if (exame) this.exameForm.controls.categoria.disable();
    this.erroInsumo.set(null);
    this.insumoForm.reset({ materialId: '', quantidade: '1', soComContraste: false });
    if (exame && this.pode('LABORATORIO.GERENCIAR')) this.carregarMateriaisEstoque();
    else this.exameForm.controls.categoria.enable();
    this.faixaEditando.set(null);
    this.erroFaixa.set(null);
    this.abrir({ tipo: 'exame', exame });
  }

  // ── Faixas por sexo e idade (ADR-0180) ─────────────────────────────────────────────────────

  protected novaFaixa(): void {
    this.faixaForm.reset({
      sexo: '', idadeMinima: '0', unidadeIdadeMinima: 'ANOS', idadeMaxima: '', unidadeIdadeMaxima: 'ANOS',
      referenciaMinima: '', referenciaMaxima: '', criticoMinimo: '', criticoMaximo: '', descricao: '',
    });
    this.erroFaixa.set(null);
    this.faixaEditando.set('');
    setTimeout(() => document.getElementById('f-faixaSexo')?.focus());
  }

  protected editarFaixa(f: FaixaReferenciaExameDto): void {
    const txt = (v: number | null | undefined) => (v != null ? String(v) : '');
    this.faixaForm.reset({
      sexo: f.sexo ?? '', idadeMinima: String(f.idadeMinima), unidadeIdadeMinima: f.unidadeIdadeMinima,
      idadeMaxima: txt(f.idadeMaxima), unidadeIdadeMaxima: f.unidadeIdadeMaxima ?? 'ANOS',
      referenciaMinima: txt(f.referenciaMinima), referenciaMaxima: txt(f.referenciaMaxima),
      criticoMinimo: txt(f.criticoMinimo), criticoMaximo: txt(f.criticoMaximo), descricao: f.descricao ?? '',
    });
    this.erroFaixa.set(null);
    this.faixaEditando.set(f.uuid);
    setTimeout(() => document.getElementById('f-faixaSexo')?.focus());
  }

  protected cancelarFaixa(): void {
    this.faixaEditando.set(null);
    this.erroFaixa.set(null);
  }

  protected salvarFaixa(exame: ExameLaboratorialResponseDto): void {
    const editando = this.faixaEditando();
    if (editando === null || this.salvandoFaixa()) return;
    const v = this.faixaForm.getRawValue();
    const num = (t: string) => (String(t).trim() === '' ? null : Number(t));
    if (String(v.idadeMinima).trim() === '') {
      this.erroFaixa.set('Informe a idade mínima (0 para desde o nascimento).');
      return;
    }
    if (num(v.referenciaMinima) == null && num(v.referenciaMaxima) == null) {
      this.erroFaixa.set('Informe ao menos um limite de referência.');
      return;
    }
    const maxima = num(v.idadeMaxima);
    const dto: FaixaReferenciaExameRequestDto = {
      sexo: v.sexo || null,
      idadeMinima: Number(v.idadeMinima),
      unidadeIdadeMinima: v.unidadeIdadeMinima,
      idadeMaxima: maxima,
      unidadeIdadeMaxima: maxima != null ? v.unidadeIdadeMaxima : null,
      referenciaMinima: num(v.referenciaMinima),
      referenciaMaxima: num(v.referenciaMaxima),
      criticoMinimo: num(v.criticoMinimo),
      criticoMaximo: num(v.criticoMaximo),
      descricao: v.descricao.trim() || undefined,
    };
    this.salvandoFaixa.set(true);
    const req = editando ? this.laboratorioService.atualizarFaixa(editando, dto) : this.laboratorioService.adicionarFaixa(exame.uuid, dto);
    req.subscribe({
      next: (atualizado) => this.aposFaixa(atualizado, editando ? 'Faixa atualizada' : 'Faixa incluída'),
      error: (e: HttpErrorResponse) => {
        this.salvandoFaixa.set(false);
        this.erroFaixa.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar a faixa.');
      },
    });
  }

  protected removerFaixa(f: FaixaReferenciaExameDto): void {
    if (this.salvandoFaixa()) return;
    this.salvandoFaixa.set(true);
    this.laboratorioService.removerFaixa(f.uuid).subscribe({
      next: (atualizado) => this.aposFaixa(atualizado, 'Faixa removida'),
      error: (e: HttpErrorResponse) => {
        this.salvandoFaixa.set(false);
        this.erroFaixa.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível remover a faixa.');
      },
    });
  }

  // ── Laudo em PDF e insumos (ADR-0183) ──────────────────────────────────────────────────────

  private carregarEmissoes(pedidoId: string): void {
    this.emissoes.set([]);
    this.laboratorioService.emissoesDoLaudo(pedidoId).pipe(catchError(() => of([]))).subscribe((e) => this.emissoes.set(e));
  }

  /** Baixa o PDF da emissão vigente e abre em outra aba; o código de verificação vai no aviso. */
  protected baixarLaudo(d: PedidoExameResponseDto): void {
    if (this.baixandoLaudo()) return;
    this.baixandoLaudo.set(true);
    this.erroDetalhe.set(null);
    this.laboratorioService.baixarLaudo(d.uuid).subscribe({
      next: (r) => {
        this.baixandoLaudo.set(false);
        const codigo = r.headers.get('X-Codigo-Verificacao') ?? '';
        const url = URL.createObjectURL(r.body!);
        const a = document.createElement('a');
        a.href = url;
        a.download = `laudo-${codigo || d.uuid}.pdf`;
        a.click();
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
        this.mostrarToast('Laudo em PDF emitido', `Código de verificação ${codigo}`);
        this.carregarEmissoes(d.uuid);
      },
      error: () => {
        this.baixandoLaudo.set(false);
        this.erroDetalhe.set('Não foi possível emitir o laudo em PDF.');
      },
    });
  }

  protected carregarMateriaisEstoque(): void {
    if (this.materiaisEstoque().length) return;
    this.estoqueService.materiais(true).pipe(catchError(() => of([]))).subscribe((m) => this.materiaisEstoque.set(m));
  }

  protected incluirInsumo(exame: ExameLaboratorialResponseDto): void {
    const v = this.insumoForm.getRawValue();
    const qtd = Number(v.quantidade);
    if (!v.materialId) {
      this.erroInsumo.set('Escolha o material do estoque.');
      return;
    }
    if (!Number.isInteger(qtd) || qtd < 1 || qtd > 100) {
      this.erroInsumo.set('A quantidade vai de 1 a 100.');
      return;
    }
    if ((exame.insumos ?? []).some((i) => i.materialId === v.materialId)) {
      this.erroInsumo.set('Este material já está nos insumos.');
      return;
    }
    const lista = [...(exame.insumos ?? []).map((i) => ({ materialId: i.materialId, quantidade: i.quantidade, soComContraste: i.soComContraste })),
      { materialId: v.materialId, quantidade: qtd, soComContraste: exame.categoria === 'IMAGEM' && v.soComContraste }];
    this.salvarInsumos(exame, lista, 'Insumo incluído');
  }

  protected removerInsumo(exame: ExameLaboratorialResponseDto, insumo: InsumoExameDto): void {
    const lista = (exame.insumos ?? []).filter((i) => i.uuid !== insumo.uuid)
      .map((i) => ({ materialId: i.materialId, quantidade: i.quantidade, soComContraste: i.soComContraste }));
    this.salvarInsumos(exame, lista, 'Insumo removido');
  }

  private salvarInsumos(exame: ExameLaboratorialResponseDto, lista: { materialId: string; quantidade: number; soComContraste: boolean }[], titulo: string): void {
    if (this.salvandoInsumo()) return;
    this.salvandoInsumo.set(true);
    this.erroInsumo.set(null);
    this.laboratorioService.definirInsumos(exame.uuid, lista).subscribe({
      next: (atualizado) => {
        this.salvandoInsumo.set(false);
        this.insumoForm.reset({ materialId: '', quantidade: '1', soComContraste: false });
        this.exames.update((l) => l.map((e) => (e.uuid === atualizado.uuid ? atualizado : e)));
        this.gaveta.set({ tipo: 'exame', exame: atualizado });
        this.mostrarToast(titulo, atualizado.nome);
      },
      error: (e: HttpErrorResponse) => {
        this.salvandoInsumo.set(false);
        this.erroInsumo.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar os insumos.');
      },
    });
  }

  private aposFaixa(exame: ExameLaboratorialResponseDto, titulo: string): void {
    this.salvandoFaixa.set(false);
    this.faixaEditando.set(null);
    this.erroFaixa.set(null);
    this.exames.update((lista) => lista.map((e) => (e.uuid === exame.uuid ? exame : e)));
    this.gaveta.set({ tipo: 'exame', exame });
    this.mostrarToast(titulo, exame.nome);
  }

  protected podeAgirNoItem(i: ItemPedidoExameDto): { retificar: boolean; cancelar: boolean; adendo: boolean } {
    return {
      retificar: i.status === 'LIBERADO' && i.categoria !== 'IMAGEM' && this.pode('LABORATORIO.LIBERAR'),
      cancelar: (i.status === 'SOLICITADO' || i.status === 'COLETADO' || i.status === 'REALIZADO' || i.status === 'RESULTADO_REGISTRADO')
        && this.pode('EXAME.SOLICITAR', 'LABORATORIO.ANALISAR', 'IMAGEM.REALIZAR'),
      adendo: i.status === 'LIBERADO' && i.categoria === 'IMAGEM' && this.pode('IMAGEM.LAUDAR'),
    };
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    switch (g.tipo) {
      case 'pedido':
        return this.salvarPedido();
      case 'coleta':
        return this.salvarColeta(g.grupo);
      case 'resultado':
        return this.salvarResultado(g.itemId);
      case 'liberar':
        return this.salvarLiberacao(g.itemId);
      case 'rejeitar':
        return this.salvarRejeicao(g.amostraCodigo);
      case 'retificar':
        return this.salvarRetificacao(g.itemId);
      case 'cancelar':
        return this.salvarCancelamento(g.itemId, g.exameNome);
      case 'exame':
        return this.salvarExame(g.exame);
      case 'comunicar':
        return this.salvarComunicacao(g.aviso);
      case 'entrega':
        return this.salvarEntrega(g.pedidoId);
      case 'realizacao':
        return this.salvarRealizacao(g.itemId);
      case 'laudo':
        return this.salvarLaudo(g.itemId, g.adendo);
      default:
        return;
    }
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

  private salvarPedido(): void {
    const v = this.pedidoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['pacienteId'] = 'Selecione o paciente.';
    if (!v.unidadeSolicitanteId) erros['unidadeSolicitanteId'] = 'Selecione a unidade que pede.';
    if (!this.examesMarcados().size) erros['buscaExame'] = 'Marque pelo menos um exame.';
    if (!v.indicacaoClinica.trim()) erros['indicacaoClinica'] = 'Informe a indicação clínica.';
    if (!CID.test(v.cid)) erros['cid'] = 'CID-10 inválido (ex.: I10, E11.9).';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula de quem pede.';
    if (this.temImagemMarcada() && !v.unidadeExecutanteImagemId) erros['unidadeExecutanteImagemId'] = 'Selecione o serviço de imagem.';
    if (!this.validar(erros)) return;
    const paciente = this.pacientes().find((p) => p.uuid === v.pacienteId);
    this.enviar(
      this.laboratorioService.solicitar({
        pacienteId: v.pacienteId,
        atendimentoId: v.atendimentoId || undefined,
        unidadeSolicitanteId: v.unidadeSolicitanteId,
        profissionalMatricula: v.profissionalMatricula.trim(),
        exameIds: [...this.examesMarcados()],
        indicacaoClinica: v.indicacaoClinica.trim(),
        cid: v.cid.trim() || undefined,
        prioridade: v.prioridade,
        unidadeExecutanteImagemId: this.temImagemMarcada() ? v.unidadeExecutanteImagemId : undefined,
      }),
      'Pedido de exames registrado',
      `${paciente?.nome ?? 'Paciente'} · ${this.examesMarcados().size} exame(s)`,
    );
  }

  private salvarColeta(grupo: GrupoColeta): void {
    const v = this.coletaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.unidadeColetaId) erros['unidadeColetaId'] = 'Selecione onde foi coletado.';
    if (!this.itensMarcados().size) erros['itens'] = 'Marque o que foi coletado.';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula de quem coletou.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.laboratorioService.coletar(grupo.pedidoId, {
        profissionalMatricula: v.profissionalMatricula.trim(),
        unidadeColetaId: v.unidadeColetaId,
        laboratorioId: v.laboratorioId || undefined,
        itemIds: [...this.itensMarcados()],
      }),
      'Coleta registrada',
      `${grupo.pacienteNome} · ${this.itensMarcados().size} exame(s)`,
    );
  }

  private salvarResultado(itemId: string): void {
    const atual = this.itemAtual();
    if (!atual) return;
    const v = this.resultadoForm.getRawValue();
    const numerico = atual.item.tipoResultado === 'NUMERICO';
    const erros: Record<string, string> = {};
    if (numerico && (v.valorNumerico === '' || isNaN(Number(v.valorNumerico)))) erros['valorNumerico'] = 'Informe o valor numérico.';
    if (!numerico && !v.valorTexto.trim()) erros['valorTexto'] = 'Informe o resultado.';
    if (!v.profissionalMatricula.trim()) erros['matriculaResultado'] = 'Informe a matrícula de quem analisou.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.laboratorioService.registrarResultado(itemId, {
        profissionalMatricula: v.profissionalMatricula.trim(),
        valorNumerico: numerico ? Number(v.valorNumerico) : undefined,
        valorTexto: numerico ? undefined : v.valorTexto.trim(),
        observacao: v.observacao.trim() || undefined,
      }),
      'Resultado registrado',
      `${atual.item.exameNome} · aguarda liberação`,
    );
  }

  private salvarLiberacao(itemId: string): void {
    const v = this.liberarForm.getRawValue();
    if (!this.validar(v.profissionalMatricula.trim() ? {} : { matriculaLiberacao: 'Informe a matrícula de quem libera.' })) return;
    this.enviar(this.laboratorioService.liberar(itemId, v.profissionalMatricula.trim()), 'Resultado liberado', this.itemAtual()?.item.exameNome ?? '');
  }

  private salvarRejeicao(codigo: string): void {
    const amostra = this.detalhe()?.amostras.find((a) => a.codigo === codigo);
    const v = this.rejeitarForm.getRawValue();
    if (!amostra) return;
    if (!this.validar(v.profissionalMatricula.trim() ? {} : { matriculaRejeicao: 'Informe a matrícula de quem rejeita.' })) return;
    this.enviar(
      this.laboratorioService.rejeitarAmostra(amostra.uuid, v.profissionalMatricula.trim(), v.motivo, v.observacao.trim() || undefined),
      'Amostra rejeitada',
      `${codigo} · exames voltam para recoleta`,
    );
  }

  private salvarRetificacao(itemId: string): void {
    const atual = this.itemAtual();
    if (!atual) return;
    const v = this.retificarForm.getRawValue();
    const numerico = atual.item.tipoResultado === 'NUMERICO';
    const erros: Record<string, string> = {};
    if (numerico && (v.valorNumerico === '' || isNaN(Number(v.valorNumerico)))) erros['valorNumericoRet'] = 'Informe o valor correto.';
    if (!numerico && !v.valorTexto.trim()) erros['valorTextoRet'] = 'Informe o resultado correto.';
    if (!v.motivo.trim()) erros['motivoRet'] = 'Informe o motivo da retificação.';
    if (!v.profissionalMatricula.trim()) erros['matriculaRet'] = 'Informe a matrícula de quem retifica.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.laboratorioService.retificar(itemId, {
        profissionalMatricula: v.profissionalMatricula.trim(),
        valorNumerico: numerico ? Number(v.valorNumerico) : undefined,
        valorTexto: numerico ? undefined : v.valorTexto.trim(),
        observacao: v.observacao.trim() || undefined,
        motivo: v.motivo.trim(),
      }),
      'Resultado retificado',
      atual.item.exameNome,
    );
  }

  private salvarCancelamento(itemId: string, exameNome: string): void {
    const v = this.cancelarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.motivo.trim()) erros['motivoCancelamento'] = 'Informe o motivo.';
    if (!v.profissionalMatricula.trim()) erros['matriculaCancelamento'] = 'Informe a matrícula.';
    if (!this.validar(erros)) return;
    this.enviar(this.laboratorioService.cancelar(itemId, v.profissionalMatricula.trim(), v.motivo.trim()), 'Exame cancelado', exameNome);
  }

  // ── Exames de imagem (ADR-0182) ────────────────────────────────────────────────────────────

  /** O pedido em montagem tem exame de imagem: pede o serviço de imagem. */
  protected temImagemMarcada(): boolean {
    const marcados = this.examesMarcados();
    return this.exames().some((e) => marcados.has(e.uuid) && e.categoria === 'IMAGEM');
  }

  protected abrirRealizacao(i: { itemId: string; pedidoId: string }): void {
    this.realizacaoForm.reset({
      realizadaEm: '', equipamento: '', contraste: false, contrasteDescricao: '', observacao: '', profissionalMatricula: this.matriculaPadrao(),
    });
    this.abrir({ tipo: 'realizacao', itemId: i.itemId, pedidoId: i.pedidoId }, i.pedidoId);
  }

  protected abrirLaudo(i: { itemId: string; pedidoId: string }, adendo = false, atual?: ItemPedidoExameDto): void {
    const r = adendo ? atual?.resultado : null;
    this.laudoForm.reset({
      tecnica: r?.laudoTecnica ?? '', achados: r?.laudoAchados ?? '', conclusao: r?.laudoConclusao ?? '',
      classificacao: (r?.interpretacao as ClassificacaoLaudo | undefined) ?? 'NORMAL', imagemUrl: r?.imagemUrl ?? '', motivo: '',
      profissionalMatricula: this.matriculaPadrao(),
    });
    this.abrir({ tipo: 'laudo', itemId: i.itemId, pedidoId: i.pedidoId, adendo }, i.pedidoId);
  }

  protected realizacaoDe(d: PedidoExameResponseDto, itemId: string) {
    return d.realizacoes.find((r) => r.itemId === itemId);
  }

  protected laudosDe(d: PedidoExameResponseDto): ItemPedidoExameDto[] {
    return d.itens.filter((i) => i.categoria === 'IMAGEM' && i.status === 'LIBERADO' && i.resultado?.laudoConclusao);
  }

  private salvarRealizacao(itemId: string): void {
    const v = this.realizacaoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (v.realizadaEm && new Date(v.realizadaEm).getTime() > Date.now() + 5 * 60 * 1000) erros['realizadaEm'] = 'A realização não pode estar no futuro.';
    if (v.contraste && !v.contrasteDescricao.trim()) erros['contrasteDescricao'] = 'Informe qual contraste foi usado.';
    if (!v.profissionalMatricula.trim()) erros['matriculaRealizacao'] = 'Informe a matrícula de quem realizou.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.laboratorioService.realizarImagem(itemId, {
        profissionalMatricula: v.profissionalMatricula.trim(),
        realizadaEm: v.realizadaEm ? (v.realizadaEm.length === 16 ? v.realizadaEm + ':00' : v.realizadaEm) : undefined,
        equipamento: v.equipamento.trim() || undefined,
        contraste: v.contraste,
        contrasteDescricao: v.contraste ? v.contrasteDescricao.trim() : undefined,
        observacao: v.observacao.trim() || undefined,
      }),
      'Exame realizado',
      `${this.itemAtual()?.item.exameNome ?? ''} · aguarda o laudo`,
    );
  }

  private salvarLaudo(itemId: string, adendo: boolean): void {
    const v = this.laudoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.achados.trim()) erros['achados'] = 'Descreva os achados.';
    if (!v.conclusao.trim()) erros['conclusao'] = 'Informe a conclusão.';
    if (v.imagemUrl.trim() && !v.imagemUrl.trim().toLowerCase().startsWith('https://')) erros['imagemUrl'] = 'O link precisa começar com https://.';
    if (adendo && !v.motivo.trim()) erros['motivoAdendo'] = 'Informe o motivo do adendo.';
    if (!v.profissionalMatricula.trim()) erros['matriculaLaudo'] = 'Informe a matrícula do radiologista.';
    if (!this.validar(erros)) return;
    const dto = {
      profissionalMatricula: v.profissionalMatricula.trim(),
      tecnica: v.tecnica.trim() || undefined,
      achados: v.achados.trim(),
      conclusao: v.conclusao.trim(),
      classificacao: v.classificacao,
      imagemUrl: v.imagemUrl.trim() || undefined,
      motivo: adendo ? v.motivo.trim() : undefined,
    };
    const exame = this.itemAtual()?.item.exameNome ?? '';
    this.enviar(
      adendo ? this.laboratorioService.adendo(itemId, dto) : this.laboratorioService.laudar(itemId, dto),
      adendo ? 'Adendo registrado' : 'Laudo liberado',
      `${exame} · ${CLASSIFICACOES_LAUDO[v.classificacao]}`,
    );
  }

  private salvarComunicacao(aviso: ResultadoAvisoExameDto): void {
    const v = this.comunicarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.receptorNome.trim()) erros['receptorNome'] = 'Informe quem recebeu a informação.';
    if (v.comunicadoEm && new Date(v.comunicadoEm).getTime() > Date.now() + 5 * 60 * 1000) erros['comunicadoEm'] = 'A comunicação não pode estar no futuro.';
    if (!v.profissionalMatricula.trim()) erros['matriculaComunicacao'] = 'Informe a matrícula de quem comunicou.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.laboratorioService.comunicarCritico(aviso.itemId, {
        profissionalMatricula: v.profissionalMatricula.trim(),
        receptorNome: v.receptorNome.trim(),
        meio: v.meio,
        comunicadoEm: v.comunicadoEm ? (v.comunicadoEm.length === 16 ? v.comunicadoEm + ':00' : v.comunicadoEm) : undefined,
      }),
      'Comunicação registrada',
      `${aviso.exameNome} · ${aviso.pacienteNome}`,
    );
  }

  private salvarEntrega(pedidoId: string): void {
    const v = this.entregaForm.getRawValue();
    const terceiro = v.retirada === 'TERCEIRO';
    const erros: Record<string, string> = {};
    if (!v.unidadeId) erros['unidadeEntrega'] = 'Selecione a unidade da entrega.';
    if (terceiro && !v.terceiroNome.trim()) erros['terceiroNome'] = 'Informe o nome de quem retirou.';
    if (terceiro && !v.terceiroDocumento.trim()) erros['terceiroDocumento'] = 'Informe o documento de quem retirou.';
    if (!v.profissionalMatricula.trim()) erros['matriculaEntrega'] = 'Informe a matrícula de quem entrega.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.laboratorioService.entregar(pedidoId, {
        profissionalMatricula: v.profissionalMatricula.trim(),
        unidadeId: v.unidadeId,
        retiradoPeloPaciente: !terceiro,
        terceiroNome: terceiro ? v.terceiroNome.trim() : undefined,
        terceiroDocumento: terceiro ? v.terceiroDocumento.trim() : undefined,
        terceiroParentesco: terceiro ? v.terceiroParentesco.trim() || undefined : undefined,
        forma: v.forma,
      }),
      'Entrega registrada',
      `${this.detalhe()?.pacienteNome ?? 'Paciente'} · ${terceiro ? 'retirado por ' + v.terceiroNome.trim() : 'retirado pelo paciente'}`,
    );
  }

  private salvarExame(exame: ExameLaboratorialResponseDto | null): void {
    const v = this.exameForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome do exame.';
    if (v.categoria === 'IMAGEM') {
      if (!this.validar(erros)) return;
      const imagem = {
        nome: v.nome.trim(),
        categoria: 'IMAGEM' as CategoriaExame,
        modalidade: v.modalidade,
        preparo: v.preparo.trim() || undefined,
        prazoDias: v.prazoDias === '' ? null : Number(v.prazoDias),
        ativo: v.ativo,
      };
      this.enviar(
        exame ? this.laboratorioService.atualizarExame(exame.uuid, imagem) : this.laboratorioService.criarExame(imagem),
        exame ? 'Exame atualizado' : 'Exame cadastrado',
        imagem.nome,
      );
      return;
    }
    const min = v.referenciaMinima === '' ? null : Number(v.referenciaMinima);
    const max = v.referenciaMaxima === '' ? null : Number(v.referenciaMaxima);
    if (v.tipoResultado === 'NUMERICO' && min != null && max != null && min > max) erros['referenciaMaxima'] = 'A máxima precisa ser maior ou igual à mínima.';
    const critMin = v.criticoMinimo === '' ? null : Number(v.criticoMinimo);
    const critMax = v.criticoMaximo === '' ? null : Number(v.criticoMaximo);
    if (v.tipoResultado === 'NUMERICO' && critMin != null && min != null && critMin > min) erros['criticoMinimo'] = 'O crítico mínimo fica abaixo da referência mínima.';
    if (v.tipoResultado === 'NUMERICO' && critMax != null && max != null && critMax < max) erros['criticoMaximo'] = 'O crítico máximo fica acima da referência máxima.';
    if (!this.validar(erros)) return;
    const numerico = v.tipoResultado === 'NUMERICO';
    const dto = {
      nome: v.nome.trim(),
      categoria: 'LABORATORIAL' as CategoriaExame,
      material: v.material,
      tipoResultado: v.tipoResultado,
      unidadeMedida: numerico ? v.unidadeMedida.trim() || undefined : undefined,
      referenciaMinima: numerico ? min : null,
      referenciaMaxima: numerico ? max : null,
      criticoMinimo: numerico ? critMin : null,
      criticoMaximo: numerico ? critMax : null,
      referenciaTexto: numerico ? undefined : v.referenciaTexto.trim() || undefined,
      preparo: v.preparo.trim() || undefined,
      prazoDias: v.prazoDias === '' ? null : Number(v.prazoDias),
      ativo: v.ativo,
    };
    this.enviar(
      exame ? this.laboratorioService.atualizarExame(exame.uuid, dto) : this.laboratorioService.criarExame(dto),
      exame ? 'Exame atualizado' : 'Exame cadastrado',
      dto.nome,
    );
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(titulo, detalhe);
        this.carregarTudo();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.erroApi.set(body?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}

function pad(v: number): string {
  return String(v).padStart(2, '0');
}
