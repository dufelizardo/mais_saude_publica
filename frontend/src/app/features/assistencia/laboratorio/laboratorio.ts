import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AcessoDaInterface } from '../../../core/models/auth';
import {
  EtapaTrabalho,
  ExameLaboratorialResponseDto,
  InterpretacaoResultado,
  ItemPedidoExameDto,
  ItemTrabalhoExameDto,
  MaterialExame,
  MotivoRejeicaoAmostra,
  PedidoExameResponseDto,
  PedidoExameResumoDto,
  PrioridadeExame,
  SituacaoPedidoExame,
  StatusItemExame,
  TipoEventoExame,
  TipoResultadoExame,
} from '../../../core/models/laboratorio';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { LaboratorioService } from '../../../core/services/laboratorio';
import { PacienteService } from '../../../core/services/paciente';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';

type Aba = 'coletar' | 'analise' | 'liberar' | 'pedidos' | 'catalogo';

type Gaveta =
  | { tipo: 'pedido' }
  | { tipo: 'coleta'; grupo: GrupoColeta }
  | { tipo: 'resultado'; itemId: string; pedidoId: string }
  | { tipo: 'liberar'; itemId: string; pedidoId: string }
  | { tipo: 'rejeitar'; amostraCodigo: string; pedidoId: string }
  | { tipo: 'detalhe'; pedidoId: string }
  | { tipo: 'retificar'; itemId: string; pedidoId: string }
  | { tipo: 'cancelar'; itemId: string; pedidoId: string; exameNome: string }
  | { tipo: 'exame'; exame: ExameLaboratorialResponseDto | null };

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

export const MATERIAIS: Record<MaterialExame, string> = { SANGUE: 'Sangue', URINA: 'Urina', FEZES: 'Fezes', SECRECAO: 'Secreção', OUTRO: 'Outro' };

export const STATUS_ITEM: Record<StatusItemExame, { classe: string; rotulo: string }> = {
  SOLICITADO: { classe: 'info', rotulo: 'Aguardando coleta' },
  COLETADO: { classe: 'warn', rotulo: 'Em análise' },
  RESULTADO_REGISTRADO: { classe: 'purple', rotulo: 'Para liberar' },
  LIBERADO: { classe: 'ok', rotulo: 'Liberado' },
  CANCELADO: { classe: 'muted', rotulo: 'Cancelado' },
};

export const SITUACOES: Record<SituacaoPedidoExame, { classe: string; rotulo: string }> = {
  AGUARDANDO_COLETA: { classe: 'info', rotulo: 'Aguardando coleta' },
  EM_ANDAMENTO: { classe: 'warn', rotulo: 'Em andamento' },
  CONCLUIDO: { classe: 'ok', rotulo: 'Concluído' },
  CANCELADO: { classe: 'muted', rotulo: 'Cancelado' },
};

export const INTERPRETACOES: Record<InterpretacaoResultado, { classe: string; rotulo: string }> = {
  NORMAL: { classe: 'ok', rotulo: 'Dentro da referência' },
  ACIMA: { classe: 'alert', rotulo: 'Acima da referência' },
  ABAIXO: { classe: 'warn', rotulo: 'Abaixo da referência' },
};

const MOTIVOS_REJEICAO: Record<MotivoRejeicaoAmostra, string> = {
  HEMOLISADA: 'Hemolisada',
  INSUFICIENTE: 'Volume insuficiente',
  COAGULADA: 'Coagulada',
  IDENTIFICACAO_INCORRETA: 'Identificação incorreta',
  OUTRO: 'Outro',
};

const EVENTOS: Record<TipoEventoExame, string> = {
  SOLICITACAO: 'Pedido feito',
  COLETA: 'Coleta',
  REJEICAO_AMOSTRA: 'Amostra rejeitada',
  RESULTADO: 'Resultado registrado',
  LIBERACAO: 'Resultado liberado',
  RETIFICACAO: 'Resultado retificado',
  CANCELAMENTO: 'Exame cancelado',
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
})
export class Laboratorio {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly laboratorioService = inject(LaboratorioService);
  private readonly pacienteService = inject(PacienteService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);

  protected readonly formatCpf = formatCpf;
  protected readonly materiais = MATERIAIS;
  protected readonly listaMateriais = Object.keys(MATERIAIS) as MaterialExame[];
  protected readonly statusItem = STATUS_ITEM;
  protected readonly situacoes = SITUACOES;
  protected readonly interpretacoes = INTERPRETACOES;
  protected readonly motivosRejeicao = MOTIVOS_REJEICAO;
  protected readonly listaMotivos = Object.keys(MOTIVOS_REJEICAO) as MotivoRejeicaoAmostra[];
  protected readonly eventos = EVENTOS;

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  private readonly matriculaPadrao = signal('');

  protected readonly paraColetar = signal<ItemTrabalhoExameDto[]>([]);
  protected readonly emAnalise = signal<ItemTrabalhoExameDto[]>([]);
  protected readonly paraLiberar = signal<ItemTrabalhoExameDto[]>([]);
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
  });

  protected readonly coletaForm = this.fb.nonNullable.group({ unidadeColetaId: [''], laboratorioId: [''], profissionalMatricula: [''] });
  protected readonly resultadoForm = this.fb.nonNullable.group({ valorNumerico: [''], valorTexto: [''], observacao: [''], profissionalMatricula: [''] });
  protected readonly liberarForm = this.fb.nonNullable.group({ profissionalMatricula: [''] });
  protected readonly rejeitarForm = this.fb.nonNullable.group({ motivo: ['HEMOLISADA' as MotivoRejeicaoAmostra], observacao: [''], profissionalMatricula: [''] });
  protected readonly retificarForm = this.fb.nonNullable.group({
    valorNumerico: [''], valorTexto: [''], observacao: [''], motivo: [''], profissionalMatricula: [''],
  });
  protected readonly cancelarForm = this.fb.nonNullable.group({ motivo: [''], profissionalMatricula: [''] });
  protected readonly exameForm = this.fb.nonNullable.group({
    nome: [''],
    material: ['SANGUE' as MaterialExame],
    tipoResultado: ['NUMERICO' as TipoResultadoExame],
    unidadeMedida: [''],
    referenciaMinima: [''],
    referenciaMaxima: [''],
    referenciaTexto: [''],
    preparo: [''],
    prazoDias: [''],
    ativo: [true],
  });

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────

  protected readonly abas = computed<{ id: Aba; rotulo: string; contagem: number | null }[]>(() => [
    ...(this.pode('LABORATORIO.COLETAR') ? [{ id: 'coletar' as Aba, rotulo: 'Para coletar', contagem: this.gruposColeta().length }] : []),
    ...(this.pode('LABORATORIO.ANALISAR') ? [{ id: 'analise' as Aba, rotulo: 'Em análise', contagem: this.emAnalise().length }] : []),
    ...(this.pode('LABORATORIO.LIBERAR') ? [{ id: 'liberar' as Aba, rotulo: 'Para liberar', contagem: this.paraLiberar().length }] : []),
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
    this.authService.usuarioAtual().subscribe((u) => this.matriculaPadrao.set(u?.profissionalMatricula ?? ''));
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
      pedidos: this.laboratorioService.listarPedidos().pipe(catchError(() => of([]))),
      exames: this.laboratorioService.listarExames().pipe(catchError(() => of([]))),
    }).subscribe(({ coletar, analise, liberar, pedidos, exames }) => {
      this.paraColetar.set(coletar);
      this.emAnalise.set(analise);
      this.paraLiberar.set(liberar);
      this.pedidos.set(pedidos);
      this.exames.set(exames);
      this.carregando.set(false);
    });
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

  protected numero(v: number | null | undefined): string {
    return v === null || v === undefined ? '' : Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 4 });
  }

  protected faixa(e: { referenciaMinima?: number | null; referenciaMaxima?: number | null; referenciaTexto?: string | null; unidadeMedida?: string | null } | undefined | null): string {
    if (!e) return '—';
    if (e.referenciaTexto) return e.referenciaTexto;
    const un = e.unidadeMedida ? ` ${e.unidadeMedida}` : '';
    if (e.referenciaMinima != null && e.referenciaMaxima != null) return `${this.numero(e.referenciaMinima)} a ${this.numero(e.referenciaMaxima)}${un}`;
    if (e.referenciaMinima != null) return `≥ ${this.numero(e.referenciaMinima)}${un}`;
    if (e.referenciaMaxima != null) return `≤ ${this.numero(e.referenciaMaxima)}${un}`;
    return '—';
  }

  protected valorDe(i: ItemPedidoExameDto): string {
    const r = i.resultado;
    if (!r) return '—';
    return r.valorTexto ?? `${this.numero(r.valorNumerico)}${r.unidadeMedida ? ' ' + r.unidadeMedida : ''}`;
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
        next: (d) => this.detalhe.set(d),
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
      prioridade: 'ROTINA', profissionalMatricula: this.matriculaPadrao(),
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

  protected abrirExame(exame: ExameLaboratorialResponseDto | null = null): void {
    this.exameForm.reset({
      nome: exame?.nome ?? '',
      material: exame?.material ?? 'SANGUE',
      tipoResultado: exame?.tipoResultado ?? 'NUMERICO',
      unidadeMedida: exame?.unidadeMedida ?? '',
      referenciaMinima: exame?.referenciaMinima != null ? String(exame.referenciaMinima) : '',
      referenciaMaxima: exame?.referenciaMaxima != null ? String(exame.referenciaMaxima) : '',
      referenciaTexto: exame?.referenciaTexto ?? '',
      preparo: exame?.preparo ?? '',
      prazoDias: exame?.prazoDias != null ? String(exame.prazoDias) : '',
      ativo: exame?.ativo ?? true,
    });
    this.abrir({ tipo: 'exame', exame });
  }

  protected podeAgirNoItem(i: ItemPedidoExameDto): { retificar: boolean; cancelar: boolean } {
    return {
      retificar: i.status === 'LIBERADO' && this.pode('LABORATORIO.LIBERAR'),
      cancelar: (i.status === 'SOLICITADO' || i.status === 'COLETADO' || i.status === 'RESULTADO_REGISTRADO') && this.pode('EXAME.SOLICITAR', 'LABORATORIO.ANALISAR'),
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

  private salvarExame(exame: ExameLaboratorialResponseDto | null): void {
    const v = this.exameForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome do exame.';
    const min = v.referenciaMinima === '' ? null : Number(v.referenciaMinima);
    const max = v.referenciaMaxima === '' ? null : Number(v.referenciaMaxima);
    if (v.tipoResultado === 'NUMERICO' && min != null && max != null && min > max) erros['referenciaMaxima'] = 'A máxima precisa ser maior ou igual à mínima.';
    if (!this.validar(erros)) return;
    const numerico = v.tipoResultado === 'NUMERICO';
    const dto = {
      nome: v.nome.trim(),
      material: v.material,
      tipoResultado: v.tipoResultado,
      unidadeMedida: numerico ? v.unidadeMedida.trim() || undefined : undefined,
      referenciaMinima: numerico ? min : null,
      referenciaMaxima: numerico ? max : null,
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
