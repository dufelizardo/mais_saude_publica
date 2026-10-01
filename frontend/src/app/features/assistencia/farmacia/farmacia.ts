import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { AuthService } from '../../../core/services/auth';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';
import { DispensacaoResponseDto } from '../../../core/models/dispensacao';
import { LoteResponseDto } from '../../../core/models/lote';
import { MedicamentoResponseDto } from '../../../core/models/medicamento';
import { MotivoPerda, MovimentacaoFarmaciaResponseDto, TipoMovimentacaoFarmacia } from '../../../core/models/movimentacao-farmacia';
import { PacienteResponseDto } from '../../../core/models/paciente';
import {
  MotivoDivergenciaTransferencia,
  StatusTransferenciaFarmacia,
  TransferenciaFarmaciaResponseDto,
} from '../../../core/models/transferencia-farmacia';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { DispensacaoService } from '../../../core/services/dispensacao';
import { LoteService } from '../../../core/services/lote';
import { MedicamentoService } from '../../../core/services/medicamento';
import { MovimentacaoFarmaciaService } from '../../../core/services/movimentacao-farmacia';
import { PacienteService } from '../../../core/services/paciente';
import { TransferenciaFarmaciaService } from '../../../core/services/transferencia-farmacia';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { AcessoDaInterface } from '../../../core/models/auth';

type Aba = 'estoque' | 'disp' | 'transf' | 'med' | 'livro';

type Gaveta =
  | { tipo: 'med'; med: MedicamentoResponseDto | null }
  | { tipo: 'lote' }
  | { tipo: 'lote-edit'; lote: LoteInfo }
  | { tipo: 'disp' }
  | { tipo: 'mov' }
  | { tipo: 'disp-view'; disp: DispensacaoResponseDto }
  | { tipo: 'transf' }
  | { tipo: 'receber'; transf: TransferenciaFarmaciaResponseDto }
  | { tipo: 'cancelar'; transf: TransferenciaFarmaciaResponseDto }
  | { tipo: 'transf-view'; transf: TransferenciaFarmaciaResponseDto };

type FiltroStatusTransferencia = StatusTransferenciaFarmacia | 'TODAS';

interface LoteInfo {
  lote: LoteResponseDto;
  medicamento: MedicamentoResponseDto | undefined;
  /** Dias até o fim do dia da validade; negativo = vencido. */
  dias: number;
}

/** Janela de "vence em breve" do mockup — mesmo corte usado no filtro e no card de resumo. */
const JANELA_VENCIMENTO_DIAS = 90;

const TIPOS: Record<TipoMovimentacaoFarmacia, { classe: string; rotulo: string }> = {
  SALDO_INICIAL: { classe: 'purple', rotulo: 'Saldo inicial' },
  ENTRADA: { classe: 'ok', rotulo: 'Entrada' },
  DISPENSACAO: { classe: 'info', rotulo: 'Dispensação' },
  PERDA: { classe: 'alert', rotulo: 'Perda' },
  AJUSTE_INVENTARIO: { classe: 'warn', rotulo: 'Ajuste de inventário' },
  TRANSFERENCIA_SAIDA: { classe: 'muted', rotulo: 'Transferência enviada' },
  TRANSFERENCIA_ENTRADA: { classe: 'muted', rotulo: 'Transferência recebida' },
  TRANSFERENCIA_ESTORNO: { classe: 'muted', rotulo: 'Transferência cancelada' },
  INCORPORACAO_SAIDA: { classe: 'purple', rotulo: 'Incorporado a outro lote' },
  INCORPORACAO_ENTRADA: { classe: 'purple', rotulo: 'Lote duplicado incorporado' },
  ADMINISTRACAO: { classe: 'info', rotulo: 'Administração ao paciente' },
  ADMINISTRACAO_ESTORNO: { classe: 'muted', rotulo: 'Administração retificada' },
};

const STATUS_TRANSFERENCIA: Record<StatusTransferenciaFarmacia, { classe: string; rotulo: string }> = {
  EM_TRANSITO: { classe: 'warn', rotulo: 'Em trânsito' },
  RECEBIDA: { classe: 'ok', rotulo: 'Recebida' },
  RECEBIDA_COM_DIVERGENCIA: { classe: 'alert', rotulo: 'Recebida com divergência' },
  CANCELADA: { classe: 'muted', rotulo: 'Cancelada' },
};

/**
 * Tela da Farmácia (domínio #9) — estoque por lote, dispensações, catálogo de medicamentos e o
 * livro de estoque de cada lote, portada do mockup Farmacia.html. Ver ADR-0058 (frontend) e
 * ADR-0057 (livro de movimentação).
 */
@Component({
  selector: 'app-farmacia',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './farmacia.html',
  styleUrl: './farmacia.css',
})
export class Farmacia {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  /** O que a tela oferece (ADR-0079): sem a permissão, o botão nem aparece. A API continua decidindo. */
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  /** Matrícula do profissional logado (ADR-0065): valor inicial do profissional nos registros novos. */
  protected readonly matriculaPadrao = signal('');
  private readonly medicamentoService = inject(MedicamentoService);
  private readonly loteService = inject(LoteService);
  private readonly dispensacaoService = inject(DispensacaoService);
  private readonly movimentacaoService = inject(MovimentacaoFarmaciaService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly pacienteService = inject(PacienteService);
  private readonly transferenciaService = inject(TransferenciaFarmaciaService);

  protected readonly formatCpf = formatCpf;
  protected readonly tipos = TIPOS;
  protected readonly statusTransferencia = STATUS_TRANSFERENCIA;
  protected readonly filtrosStatusTransferencia: { valor: FiltroStatusTransferencia; rotulo: string }[] = [
    { valor: 'EM_TRANSITO', rotulo: 'Em trânsito' },
    { valor: 'RECEBIDA', rotulo: 'Recebidas' },
    { valor: 'RECEBIDA_COM_DIVERGENCIA', rotulo: 'Com divergência' },
    { valor: 'CANCELADA', rotulo: 'Canceladas' },
    { valor: 'TODAS', rotulo: 'Todas' },
  ];
  protected readonly motivosDivergencia: { valor: MotivoDivergenciaTransferencia; rotulo: string }[] = [
    { valor: 'AVARIA', rotulo: 'Avaria' },
    { valor: 'EXTRAVIO', rotulo: 'Extravio' },
    { valor: 'OUTRO', rotulo: 'Outro' },
  ];
  protected readonly motivos: { valor: MotivoPerda; rotulo: string }[] = [
    { valor: 'VENCIMENTO', rotulo: 'Vencimento' },
    { valor: 'AVARIA', rotulo: 'Avaria' },
    { valor: 'EXTRAVIO', rotulo: 'Extravio' },
    { valor: 'OUTRO', rotulo: 'Outro' },
  ];

  protected readonly medicamentos = signal<MedicamentoResponseDto[]>([]);
  protected readonly lotes = signal<LoteResponseDto[]>([]);
  protected readonly dispensacoes = signal<DispensacaoResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly transferencias = signal<TransferenciaFarmaciaResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly aba = signal<Aba>('estoque');
  protected readonly buscaEstoque = signal('');
  protected readonly filtroUnidade = signal('');
  protected readonly filtroVencimento = signal(false);
  protected readonly buscaDisp = signal('');
  protected readonly buscaMed = signal('');
  protected readonly filtroStatusTransf = signal<FiltroStatusTransferencia>('EM_TRANSITO');
  protected readonly filtroDestinoTransf = signal('');

  protected readonly livroLoteId = signal<string | null>(null);
  protected readonly extrato = signal<MovimentacaoFarmaciaResponseDto[]>([]);
  protected readonly extratoCarregando = signal(false);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly medForm = this.fb.nonNullable.group({
    nome: [''],
    principioAtivo: [''],
    apresentacao: [''],
    codigo: [''],
    ativo: [true],
  });

  protected readonly loteForm = this.fb.nonNullable.group({
    medicamentoId: [''],
    unidadeId: [''],
    numeroLote: [''],
    validade: [''],
    quantidade: ['0'],
    profissionalMatricula: [''],
  });

  protected readonly loteEditForm = this.fb.nonNullable.group({
    numeroLote: [''],
    validade: [''],
  });

  protected readonly dispForm = this.fb.nonNullable.group({
    loteId: [''],
    buscaPaciente: [''],
    pacienteId: [''],
    quantidade: [''],
    dataHora: [''],
    profissionalMatricula: [''],
    consultaId: [''],
  });

  protected readonly movForm = this.fb.nonNullable.group({
    tipo: ['PERDA' as 'PERDA' | 'AJUSTE_INVENTARIO'],
    loteId: [''],
    quantidade: [''],
    motivoPerda: ['' as MotivoPerda | ''],
    saldoContado: [''],
    justificativa: [''],
    profissionalMatricula: [''],
  });

  protected readonly transfForm = this.fb.nonNullable.group({
    loteId: [''],
    unidadeDestinoId: [''],
    quantidade: [''],
    profissionalMatricula: [''],
    observacao: [''],
  });

  protected readonly receberForm = this.fb.nonNullable.group({
    quantidadeRecebida: [''],
    profissionalMatricula: [''],
    motivoDivergencia: ['' as MotivoDivergenciaTransferencia | ''],
    justificativaDivergencia: [''],
  });

  protected readonly cancelarForm = this.fb.nonNullable.group({
    profissionalMatricula: [''],
    motivo: [''],
  });

  // ── Dados derivados ────────────────────────────────────────────────────────────────────────────

  private readonly medicamentoPorId = computed(() => new Map(this.medicamentos().map((m) => [m.uuid, m])));

  protected readonly lotesInfo = computed<LoteInfo[]>(() =>
    this.lotes().map((lote) => ({
      lote,
      medicamento: this.medicamentoPorId().get(lote.medicamentoUuid),
      dias: diasAte(lote.validade),
    })),
  );

  protected readonly lotesFiltrados = computed(() => {
    const q = this.buscaEstoque().trim().toLowerCase();
    const unidade = this.filtroUnidade();
    const soVencendo = this.filtroVencimento();
    return this.lotesInfo()
      .filter((i) => !unidade || i.lote.unidadeUuid === unidade)
      .filter((i) => !soVencendo || (i.dias >= 0 && i.dias <= JANELA_VENCIMENTO_DIAS && i.lote.quantidade > 0))
      .filter((i) => !q || `${i.lote.medicamentoNome} ${i.medicamento?.principioAtivo ?? ''} ${i.lote.numeroLote}`.toLowerCase().includes(q))
      .sort((a, b) => a.lote.validade.localeCompare(b.lote.validade));
  });

  protected readonly resumo = computed(() => {
    const meds = this.medicamentos();
    const ativos = meds.filter((m) => m.ativo).length;
    const comSaldo = this.lotesInfo().filter((i) => i.lote.quantidade > 0);
    const hoje = hojeIso();
    const dispHoje = this.dispensacoes().filter((d) => d.dataHora.startsWith(hoje));
    return {
      ativos,
      inativos: meds.length - ativos,
      lotesComSaldo: comSaldo.length,
      unidadesEmEstoque: comSaldo.reduce((acc, i) => acc + i.lote.quantidade, 0),
      vencendo: comSaldo.filter((i) => i.dias >= 0 && i.dias <= JANELA_VENCIMENTO_DIAS).length,
      vencidos: comSaldo.filter((i) => i.dias < 0).length,
      dispHoje: dispHoje.length,
      unidadesEntreguesHoje: dispHoje.reduce((acc, d) => acc + d.quantidade, 0),
    };
  });

  private readonly unidadePorLote = computed(() => new Map(this.lotes().map((l) => [l.uuid, l.unidadeNome])));

  protected readonly dispFiltradas = computed(() => {
    const q = this.buscaDisp().trim().toLowerCase();
    return this.dispensacoes()
      .filter((d) => !q || `${d.pacienteNome} ${d.medicamentoNome} ${d.loteNumeroLote}`.toLowerCase().includes(q))
      .sort((a, b) => b.dataHora.localeCompare(a.dataHora));
  });

  private readonly saldoPorMedicamento = computed(() => {
    const mapa = new Map<string, number>();
    for (const l of this.lotes()) mapa.set(l.medicamentoUuid, (mapa.get(l.medicamentoUuid) ?? 0) + l.quantidade);
    return mapa;
  });

  protected readonly medsFiltrados = computed(() => {
    const q = this.buscaMed().trim().toLowerCase();
    return this.medicamentos()
      .filter((m) => !q || `${m.nome} ${m.principioAtivo ?? ''} ${m.codigo ?? ''}`.toLowerCase().includes(q))
      .map((m) => ({ med: m, saldo: this.saldoPorMedicamento().get(m.uuid) ?? 0 }));
  });

  protected readonly livroLote = computed(() => this.lotesInfo().find((i) => i.lote.uuid === this.livroLoteId()) ?? null);

  private readonly dispensacaoPorId = computed(() => new Map(this.dispensacoes().map((d) => [d.uuid, d])));

  protected readonly emTransito = computed(() => this.transferencias().filter((t) => t.status === 'EM_TRANSITO').length);

  protected readonly transfFiltradas = computed(() => {
    const status = this.filtroStatusTransf();
    const destino = this.filtroDestinoTransf();
    return this.transferencias()
      .filter((t) => status === 'TODAS' || t.status === status)
      .filter((t) => !destino || t.unidadeDestinoId === destino);
  });

  protected readonly medicamentosAtivos = computed(() => this.medicamentos().filter((m) => m.ativo));

  constructor() {
    this.authService.usuarioAtual().subscribe((u) => this.matriculaPadrao.set(u?.profissionalMatricula ?? ''));
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.carregarTudo();
    this.unidadeSaudeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }


  /** Listagens vazias respondem 404 no backend — tratadas como lista vazia. */
  private carregarTudo(): void {
    this.carregando.set(true);
    forkJoin({
      medicamentos: this.medicamentoService.listar().pipe(catchError(() => of([]))),
      lotes: this.loteService.listar().pipe(catchError(() => of([]))),
      dispensacoes: this.dispensacaoService.listar().pipe(catchError(() => of([]))),
      transferencias: this.transferenciaService.listar().pipe(catchError(() => of([]))),
    }).subscribe(({ medicamentos, lotes, dispensacoes, transferencias }) => {
      this.medicamentos.set(medicamentos);
      this.lotes.set(lotes);
      this.dispensacoes.set(dispensacoes);
      this.transferencias.set(transferencias);
      this.carregando.set(false);
      if (this.aba() === 'livro') this.carregarExtrato();
    });
  }

  // ── Abas ───────────────────────────────────────────────────────────────────────────────────────

  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'estoque', rotulo: 'Estoque por lote' },
    { id: 'disp', rotulo: 'Dispensações' },
    { id: 'transf', rotulo: 'Transferências' },
    { id: 'med', rotulo: 'Medicamentos' },
    { id: 'livro', rotulo: 'Livro de estoque' },
  ];

  protected contagem(aba: Aba): number | null {
    if (aba === 'estoque') return this.lotes().length;
    if (aba === 'disp') return this.dispensacoes().length;
    if (aba === 'transf') return this.emTransito();
    if (aba === 'med') return this.medicamentos().length;
    return null;
  }

  protected selecionarAba(aba: Aba, focar = false): void {
    this.aba.set(aba);
    if (aba === 'livro') {
      if (!this.livroLoteId() && this.lotes().length) this.livroLoteId.set(this.lotes()[0].uuid);
      this.carregarExtrato();
    }
    if (focar) document.getElementById('tab-' + aba)?.focus();
  }

  protected navegarAbas(event: KeyboardEvent, indice: number): void {
    if (event.key !== 'ArrowRight' && event.key !== 'ArrowLeft') return;
    event.preventDefault();
    const passo = event.key === 'ArrowRight' ? 1 : -1;
    const proxima = this.abas[(indice + passo + this.abas.length) % this.abas.length];
    this.selecionarAba(proxima.id, true);
  }

  protected verExtrato(loteId: string): void {
    this.livroLoteId.set(loteId);
    this.selecionarAba('livro', true);
  }

  protected trocarLoteDoLivro(loteId: string): void {
    this.livroLoteId.set(loteId);
    this.carregarExtrato();
  }

  private carregarExtrato(): void {
    const loteId = this.livroLoteId();
    if (!loteId) {
      this.extrato.set([]);
      return;
    }
    this.extratoCarregando.set(true);
    this.movimentacaoService.extratoDoLote(loteId).pipe(catchError(() => of([]))).subscribe((movs) => {
      this.extrato.set(movs);
      this.extratoCarregando.set(false);
    });
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────────

  protected formatarNumero(v: number): string {
    return v.toLocaleString('pt-BR');
  }

  protected formatarData(iso: string): string {
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  /** `dataHora` da dispensação é LocalDateTime (sem fuso). */
  protected formatarDataHora(iso: string): string {
    return `${this.formatarData(iso)} · ${iso.slice(11, 16)}`;
  }

  /** `registradoEm` do livro é um Instant (UTC) — exibido no horário local. */
  protected formatarInstante(iso: string): string {
    const d = new Date(iso);
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} · ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  protected rotuloLote(i: LoteInfo): string {
    return `${i.lote.medicamentoNome} · ${i.lote.numeroLote} · ${i.lote.unidadeNome} · saldo ${this.formatarNumero(i.lote.quantidade)}`;
  }

  protected rotuloMotivo(motivo: MotivoPerda | null | undefined): string {
    return this.motivos.find((m) => m.valor === motivo)?.rotulo ?? '';
  }

  protected detalheMovimento(m: MovimentacaoFarmaciaResponseDto): string {
    switch (m.tipo) {
      case 'DISPENSACAO':
        return (m.dispensacaoId && this.dispensacaoPorId().get(m.dispensacaoId)?.pacienteNome) || 'Dispensação';
      case 'PERDA':
        return this.rotuloMotivo(m.motivoPerda) + (m.justificativa ? ' · ' + m.justificativa : '');
      case 'AJUSTE_INVENTARIO':
        return `Contado ${this.formatarNumero(m.saldoApos)} · ${m.justificativa ?? ''}`;
      case 'SALDO_INICIAL':
        return 'Saldo do lote antes do livro existir';
      case 'TRANSFERENCIA_SAIDA':
      case 'TRANSFERENCIA_ENTRADA':
      case 'TRANSFERENCIA_ESTORNO':
        return m.justificativa || 'Transferência entre unidades';
      case 'ADMINISTRACAO':
      case 'ADMINISTRACAO_ESTORNO':
        return m.justificativa || 'Administração ao paciente';
      case 'INCORPORACAO_SAIDA':
      case 'INCORPORACAO_ENTRADA':
        return 'Mesma remessa cadastrada duas vezes na unidade';
      default:
        return 'Entrada do lote';
    }
  }

  protected rotuloDivergencia(motivo: MotivoDivergenciaTransferencia | null | undefined): string {
    return this.motivosDivergencia.find((m) => m.valor === motivo)?.rotulo ?? '—';
  }

  protected classeDelta(v: number): string {
    return v > 0 ? 'delta-pos' : v < 0 ? 'delta-neg' : 'delta-zero';
  }

  protected sinal(v: number): string {
    return (v > 0 ? '+' : '') + this.formatarNumero(v);
  }

  protected unidadeDaDispensacao(d: DispensacaoResponseDto): string {
    return this.unidadePorLote().get(d.loteUuid) ?? '—';
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'med':
        return g.med ? 'Editar medicamento' : 'Novo medicamento';
      case 'lote':
        return 'Entrada de lote';
      case 'lote-edit':
        return 'Corrigir lote';
      case 'disp':
        return 'Registrar dispensação';
      case 'mov':
        return 'Perda ou ajuste de inventário';
      case 'disp-view':
        return 'Dispensação';
      case 'transf':
        return 'Transferir para outra unidade';
      case 'receber':
        return 'Conferir recebimento';
      case 'cancelar':
        return 'Cancelar transferência';
      case 'transf-view':
        return 'Transferência';
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

  protected abrirMedicamento(med: MedicamentoResponseDto | null = null): void {
    this.medForm.reset({
      nome: med?.nome ?? '',
      principioAtivo: med?.principioAtivo ?? '',
      apresentacao: med?.apresentacao ?? '',
      codigo: med?.codigo ?? '',
      ativo: med?.ativo ?? true,
    });
    this.abrir({ tipo: 'med', med });
  }

  protected abrirEntradaLote(): void {
    this.loteForm.reset({ medicamentoId: '', unidadeId: '', numeroLote: '', validade: '', quantidade: '0', profissionalMatricula: this.matriculaPadrao() });
    this.abrir({ tipo: 'lote' });
  }

  protected abrirCorrecaoLote(i: LoteInfo): void {
    this.loteEditForm.reset({ numeroLote: i.lote.numeroLote, validade: i.lote.validade });
    this.abrir({ tipo: 'lote-edit', lote: i });
  }

  protected abrirDispensacao(loteId = ''): void {
    this.dispForm.reset({
      loteId,
      buscaPaciente: '',
      pacienteId: '',
      quantidade: '',
      dataHora: agoraLocal(),
      profissionalMatricula: this.matriculaPadrao(),
      consultaId: '',
    });
    this.abrir({ tipo: 'disp' });
  }

  protected abrirMovimentacao(loteId = ''): void {
    this.movForm.reset({
      tipo: 'PERDA',
      loteId,
      quantidade: '',
      motivoPerda: '',
      saldoContado: '',
      justificativa: '',
      profissionalMatricula: this.matriculaPadrao(),
    });
    this.abrir({ tipo: 'mov' });
  }

  protected verDispensacao(disp: DispensacaoResponseDto): void {
    this.abrir({ tipo: 'disp-view', disp });
  }

  protected abrirTransferencia(loteId = ''): void {
    this.transfForm.reset({ loteId, unidadeDestinoId: '', quantidade: '', profissionalMatricula: this.matriculaPadrao(), observacao: '' });
    this.abrir({ tipo: 'transf' });
  }

  protected abrirRecebimento(transf: TransferenciaFarmaciaResponseDto): void {
    this.receberForm.reset({
      quantidadeRecebida: String(transf.quantidade),
      profissionalMatricula: this.matriculaPadrao(),
      motivoDivergencia: '',
      justificativaDivergencia: '',
    });
    this.abrir({ tipo: 'receber', transf });
  }

  protected abrirCancelamento(transf: TransferenciaFarmaciaResponseDto): void {
    this.cancelarForm.reset({ profissionalMatricula: this.matriculaPadrao(), motivo: '' });
    this.abrir({ tipo: 'cancelar', transf });
  }

  protected verTransferencia(transf: TransferenciaFarmaciaResponseDto): void {
    this.abrir({ tipo: 'transf-view', transf });
  }

  /** Só lotes com saldo e dentro da validade — lote vencido vai para perda, não circula (ADR-0059). */
  protected lotesTransferiveis(): LoteInfo[] {
    return this.lotesInfo().filter((i) => i.lote.quantidade > 0 && i.dias >= 0);
  }

  protected unidadesDestino(): UnidadeSaudeResponseDto[] {
    const origem = this.loteSelecionado(this.transfForm.controls.loteId.value)?.lote.unidadeUuid;
    return this.unidades().filter((u) => u.uuid !== origem);
  }

  /** Enviado − recebido na gaveta de conferência; nulo enquanto a quantidade não é válida. */
  protected divergenciaRecebimento(t: TransferenciaFarmaciaResponseDto): number | null {
    const v = this.receberForm.controls.quantidadeRecebida.value;
    const recebida = Number(v);
    return v === '' || !Number.isInteger(recebida) || recebida < 0 ? null : t.quantidade - recebida;
  }

  protected lotesComSaldo(): LoteInfo[] {
    return this.lotesInfo().filter((i) => i.lote.quantidade > 0);
  }

  protected loteSelecionado(loteId: string): LoteInfo | undefined {
    return this.lotesInfo().find((i) => i.lote.uuid === loteId);
  }

  /** Busca de paciente dentro da gaveta: filtra por nome ou CPF, sem listar a base inteira de uma vez. */
  protected pacientesFiltrados(): PacienteResponseDto[] {
    const q = this.dispForm.controls.buscaPaciente.value.trim().toLowerCase();
    const digitos = q.replace(/\D/g, '');
    const selecionado = this.dispForm.controls.pacienteId.value;
    const lista = this.pacientes().filter(
      (p) => !q || p.nome.toLowerCase().includes(q) || (digitos.length > 0 && p.cpf.replace(/\D/g, '').includes(digitos)),
    );
    const limitada = lista.slice(0, 50);
    const atual = this.pacientes().find((p) => p.uuid === selecionado);
    return atual && !limitada.includes(atual) ? [atual, ...limitada] : limitada;
  }

  protected justificativaObrigatoria(): boolean {
    const v = this.movForm.getRawValue();
    return v.tipo === 'AJUSTE_INVENTARIO' || v.motivoPerda === 'OUTRO';
  }

  protected saldoNoLivro(): number | null {
    const i = this.loteSelecionado(this.movForm.controls.loteId.value);
    return i ? i.lote.quantidade : null;
  }

  protected diferencaAjuste(): number | null {
    const saldo = this.saldoNoLivro();
    const contado = this.movForm.controls.saldoContado.value;
    return saldo == null || contado === '' ? null : Number(contado) - saldo;
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    switch (g.tipo) {
      case 'med':
        return this.salvarMedicamento(g.med);
      case 'lote':
        return this.salvarLote();
      case 'lote-edit':
        return this.salvarCorrecaoLote(g.lote);
      case 'disp':
        return this.salvarDispensacao();
      case 'mov':
        return this.salvarMovimentacao();
      case 'transf':
        return this.salvarTransferencia();
      case 'receber':
        return this.salvarRecebimento(g.transf);
      case 'cancelar':
        return this.salvarCancelamento(g.transf);
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

  private salvarMedicamento(med: MedicamentoResponseDto | null): void {
    const v = this.medForm.getRawValue();
    if (!this.validar(v.nome.trim() ? {} : { nome: 'Informe o nome do medicamento.' })) return;
    const dto = {
      nome: v.nome.trim(),
      principioAtivo: v.principioAtivo.trim() || undefined,
      apresentacao: v.apresentacao.trim() || undefined,
      codigo: v.codigo.trim() || undefined,
      ativo: v.ativo,
    };
    const req = med ? this.medicamentoService.atualizar(med.uuid, dto) : this.medicamentoService.criar(dto);
    this.enviar(req, med ? 'Medicamento atualizado' : 'Medicamento cadastrado', dto.nome);
  }

  private salvarLote(): void {
    const v = this.loteForm.getRawValue();
    const qtd = v.quantidade === '' ? NaN : Number(v.quantidade);
    const erros: Record<string, string> = {};
    if (!v.medicamentoId) erros['medicamentoId'] = 'Selecione o medicamento.';
    if (!v.unidadeId) erros['unidadeId'] = 'Selecione a unidade.';
    if (!v.numeroLote.trim()) erros['numeroLote'] = 'Informe o número do lote.';
    if (!v.validade) erros['validade'] = 'Informe a validade.';
    if (!Number.isInteger(qtd) || qtd < 0) erros['quantidade'] = 'Use um número inteiro igual ou maior que zero.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.loteService.criar({
        medicamentoId: v.medicamentoId,
        unidadeId: v.unidadeId,
        numeroLote: v.numeroLote.trim(),
        validade: v.validade,
        quantidade: qtd,
        profissionalMatricula: v.profissionalMatricula.trim() || undefined,
      }),
      'Entrada de lote registrada',
      `Lote ${v.numeroLote.trim()} com ${this.formatarNumero(qtd)} un. · entrada no livro`,
    );
  }

  private salvarCorrecaoLote(i: LoteInfo): void {
    const v = this.loteEditForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.numeroLote.trim()) erros['numeroLote'] = 'Informe o número do lote.';
    if (!v.validade) erros['validade'] = 'Informe a validade.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.loteService.corrigir(i.lote.uuid, { numeroLote: v.numeroLote.trim(), validade: v.validade }),
      'Lote corrigido',
      `Lote ${v.numeroLote.trim()} · validade ${this.formatarData(v.validade)}`,
    );
  }

  private salvarDispensacao(): void {
    const v = this.dispForm.getRawValue();
    const qtd = Number(v.quantidade);
    const erros: Record<string, string> = {};
    if (!v.loteId) erros['loteId'] = 'Selecione o lote.';
    if (!v.pacienteId) erros['pacienteId'] = 'Selecione o paciente.';
    if (v.quantidade === '' || !Number.isInteger(qtd) || qtd <= 0) erros['quantidade'] = 'A quantidade precisa ser maior que zero.';
    if (!v.dataHora) erros['dataHora'] = 'Informe data e hora.';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula do profissional.';
    const lote = this.loteSelecionado(v.loteId);
    if (!erros['quantidade'] && lote && qtd > lote.lote.quantidade) {
      erros['quantidade'] = `Máximo disponível: ${this.formatarNumero(lote.lote.quantidade)}.`;
    }
    if (!this.validar(erros)) return;
    const paciente = this.pacientes().find((p) => p.uuid === v.pacienteId);
    this.enviar(
      this.dispensacaoService.criar({
        loteId: v.loteId,
        pacienteId: v.pacienteId,
        profissionalMatricula: v.profissionalMatricula.trim(),
        consultaId: v.consultaId.trim() || undefined,
        quantidade: qtd,
        dataHora: v.dataHora,
      }),
      'Dispensação registrada',
      `${this.formatarNumero(qtd)} un. para ${paciente?.nome ?? 'o paciente'} · lote baixado no livro`,
    );
  }

  private salvarMovimentacao(): void {
    const v = this.movForm.getRawValue();
    const justificativa = v.justificativa.trim();
    const erros: Record<string, string> = {};
    if (!v.loteId) erros['loteId'] = 'Selecione o lote.';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula.';

    if (v.tipo === 'PERDA') {
      const qtd = Number(v.quantidade);
      if (v.quantidade === '' || !Number.isInteger(qtd) || qtd <= 0) erros['quantidade'] = 'Informe uma quantidade maior que zero.';
      if (!v.motivoPerda) erros['motivoPerda'] = 'Selecione o motivo.';
      if (v.motivoPerda === 'OUTRO' && !justificativa) erros['justificativa'] = 'Com motivo “Outro”, a justificativa é obrigatória.';
      const lote = this.loteSelecionado(v.loteId);
      if (!erros['quantidade'] && lote && qtd > lote.lote.quantidade) erros['quantidade'] = `Máximo: ${this.formatarNumero(lote.lote.quantidade)}.`;
      if (!this.validar(erros)) return;
      this.livroLoteId.set(v.loteId);
      this.enviar(
        this.movimentacaoService.registrar({
          loteId: v.loteId,
          tipo: 'PERDA',
          quantidade: qtd,
          motivoPerda: v.motivoPerda as MotivoPerda,
          justificativa: justificativa || undefined,
          profissionalMatricula: v.profissionalMatricula.trim(),
        }),
        'Perda registrada no livro',
        `${this.formatarNumero(qtd)} un. · ${this.rotuloMotivo(v.motivoPerda as MotivoPerda)}`,
      );
      return;
    }

    const contado = v.saldoContado === '' ? NaN : Number(v.saldoContado);
    if (!Number.isInteger(contado) || contado < 0) erros['saldoContado'] = 'Informe o saldo contado (0 ou mais).';
    if (!justificativa) erros['justificativa'] = 'A justificativa é obrigatória no ajuste.';
    if (!this.validar(erros)) return;
    const diferenca = this.diferencaAjuste() ?? 0;
    this.livroLoteId.set(v.loteId);
    this.enviar(
      this.movimentacaoService.registrar({
        loteId: v.loteId,
        tipo: 'AJUSTE_INVENTARIO',
        saldoContado: contado,
        justificativa,
        profissionalMatricula: v.profissionalMatricula.trim(),
      }),
      'Ajuste de inventário registrado',
      `Diferença ${this.sinal(diferenca)}`,
    );
  }

  private salvarTransferencia(): void {
    const v = this.transfForm.getRawValue();
    const qtd = Number(v.quantidade);
    const erros: Record<string, string> = {};
    if (!v.loteId) erros['loteId'] = 'Selecione o lote.';
    if (!v.unidadeDestinoId) erros['unidadeDestinoId'] = 'Selecione a unidade de destino.';
    if (v.quantidade === '' || !Number.isInteger(qtd) || qtd <= 0) erros['quantidade'] = 'A quantidade precisa ser maior que zero.';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula de quem envia.';
    const lote = this.loteSelecionado(v.loteId);
    if (!erros['quantidade'] && lote && qtd > lote.lote.quantidade) {
      erros['quantidade'] = `Máximo disponível: ${this.formatarNumero(lote.lote.quantidade)}.`;
    }
    if (!this.validar(erros)) return;
    const destino = this.unidades().find((u) => u.uuid === v.unidadeDestinoId);
    this.enviar(
      this.transferenciaService.enviar({
        loteOrigemId: v.loteId,
        unidadeDestinoId: v.unidadeDestinoId,
        quantidade: qtd,
        profissionalMatricula: v.profissionalMatricula.trim(),
        observacao: v.observacao.trim() || undefined,
      }),
      'Transferência enviada',
      `${this.formatarNumero(qtd)} un. em trânsito para ${destino?.nome ?? 'a unidade de destino'}`,
    );
  }

  private salvarRecebimento(t: TransferenciaFarmaciaResponseDto): void {
    const v = this.receberForm.getRawValue();
    const recebida = Number(v.quantidadeRecebida);
    const justificativa = v.justificativaDivergencia.trim();
    const erros: Record<string, string> = {};
    if (v.quantidadeRecebida === '' || !Number.isInteger(recebida) || recebida < 0) {
      erros['quantidadeRecebida'] = 'Informe a quantidade que chegou (0 ou mais).';
    } else if (recebida > t.quantidade) {
      erros['quantidadeRecebida'] = `Foram enviadas ${this.formatarNumero(t.quantidade)} unidades.`;
    }
    const matricula = v.profissionalMatricula.trim();
    if (!matricula) erros['profissionalMatricula'] = 'Informe a matrícula de quem conferiu.';
    else if (matricula === t.profissionalMatricula) {
      erros['profissionalMatricula'] = 'O recebimento precisa ser conferido por outro profissional, não por quem enviou.';
    }
    const divergente = !erros['quantidadeRecebida'] && recebida < t.quantidade;
    if (divergente && !v.motivoDivergencia) erros['motivoDivergencia'] = 'Selecione o motivo da divergência.';
    if (divergente && !justificativa) erros['justificativaDivergencia'] = 'Descreva o que aconteceu com o que não chegou.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.transferenciaService.receber(t.uuid, {
        quantidadeRecebida: recebida,
        profissionalMatricula: matricula,
        motivoDivergencia: divergente ? (v.motivoDivergencia as MotivoDivergenciaTransferencia) : undefined,
        justificativaDivergencia: divergente ? justificativa : undefined,
      }),
      divergente ? 'Recebimento registrado com divergência' : 'Recebimento registrado',
      divergente
        ? `${this.formatarNumero(recebida)} de ${this.formatarNumero(t.quantidade)} un. · ${this.formatarNumero(t.quantidade - recebida)} não chegaram`
        : `${this.formatarNumero(recebida)} un. em ${t.unidadeDestinoNome}`,
    );
  }

  private salvarCancelamento(t: TransferenciaFarmaciaResponseDto): void {
    const v = this.cancelarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.motivo.trim()) erros['motivo'] = 'Informe o motivo do cancelamento.';
    if (!v.profissionalMatricula.trim()) erros['profissionalMatricula'] = 'Informe a matrícula.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.transferenciaService.cancelar(t.uuid, { profissionalMatricula: v.profissionalMatricula.trim(), motivo: v.motivo.trim() }),
      'Transferência cancelada',
      `${this.formatarNumero(t.quantidade)} un. voltaram ao lote ${t.numeroLote} em ${t.unidadeOrigemNome}`,
    );
  }

  private enviar(req: ReturnType<MedicamentoService['criar']>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
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

function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function agoraLocal(): string {
  const d = new Date();
  return `${hojeIso()}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function diasAte(validade: string): number {
  return Math.floor((new Date(validade + 'T23:59:59').getTime() - Date.now()) / 864e5);
}
