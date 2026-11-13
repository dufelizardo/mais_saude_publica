import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto, ProfissionalResponseDto } from '../../../core/models/profissional';
import {
  COMPOSICAO_MINIMA,
  DIAS_DA_SEMANA,
  FUNCOES_TRIPULANTE,
  FuncaoTripulante,
  MOTIVOS_TRANSPORTE,
  MotivoTransporte,
  PlantaoDto,
  QuilometragemDto,
  SITUACOES_SOLICITACAO_TRANSPORTE,
  SITUACOES_VEICULO,
  SITUACOES_VIAGEM,
  SituacaoSolicitacaoTransporte,
  SituacaoVeiculo,
  SolicitacaoTransporteDto,
  SolicitacaoTransporteRequestDto,
  TIPOS_VEICULO,
  TRECHOS_TRANSPORTE,
  TipoVeiculo,
  TrechoTransporte,
  VeiculoDto,
  ViagemDto,
  CessaoVeiculoDto,
  PedidoVeiculoEmergenciaDto,
} from '../../../core/models/transporte';
import { AuthService } from '../../../core/services/auth';
import { PacienteService } from '../../../core/services/paciente';
import { ProfissionalService } from '../../../core/services/profissional';
import { TransporteService } from '../../../core/services/transporte';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { proximaAba } from '../../../shared/formato';

type Aba = 'solicitacoes' | 'viagens' | 'frota' | 'plantoes' | 'quilometragem';
type Gaveta =
  | { tipo: 'veiculo'; item: VeiculoDto | null }
  | { tipo: 'situacao'; item: VeiculoDto }
  | { tipo: 'plantao'; item: VeiculoDto | null }
  | { tipo: 'solicitacao'; vinculo: { agendamentoId?: string; internacaoId?: string } }
  | { tipo: 'motivo'; acao: 'cancelarSolicitacao' | 'negarTfd'; item: SolicitacaoTransporteDto }
  | { tipo: 'motivo'; acao: 'cancelarViagem'; item: ViagemDto }
  | { tipo: 'viagem'; item: ViagemDto | null }
  | { tipo: 'km'; acao: 'saida' | 'chegada'; item: ViagemDto }
  | { tipo: 'embarque'; item: ViagemDto }
  | { tipo: 'ceder'; pedido: PedidoVeiculoEmergenciaDto }
  | { tipo: 'devolver'; cessao: CessaoVeiculoDto };

/** "2026-11-08T10:00" no fuso do navegador, para o input datetime-local. */
function localIso(d: Date): string {
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
}

function hojeIso(dias = 0): string {
  return localIso(new Date(Date.now() + dias * 86_400_000)).slice(0, 10);
}

/**
 * Frota e transporte: os veículos com a situação e o histórico, e o plantão das viaturas (ADR-0178); o transporte eletivo e o TFD
 * (ADR-0190): a fila de solicitações, a autorização da comissão de TFD, a agenda de viagens com os embarques e a quilometragem.
 */
@Component({
  selector: 'app-frota',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './frota.html',
})
export class Frota {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly service = inject(TransporteService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly pacienteService = inject(PacienteService);

  protected readonly tipos = TIPOS_VEICULO;
  protected readonly listaTipos = Object.entries(TIPOS_VEICULO) as [TipoVeiculo, string][];
  protected readonly situacoes = SITUACOES_VEICULO;
  protected readonly funcoes = FUNCOES_TRIPULANTE;
  protected readonly listaFuncoes = Object.entries(FUNCOES_TRIPULANTE) as [FuncaoTripulante, string][];
  protected readonly composicao = COMPOSICAO_MINIMA;
  protected readonly motivos = MOTIVOS_TRANSPORTE;
  protected readonly listaMotivos = Object.entries(MOTIVOS_TRANSPORTE) as [MotivoTransporte, string][];
  protected readonly trechos = TRECHOS_TRANSPORTE;
  protected readonly listaTrechos = Object.entries(TRECHOS_TRANSPORTE) as [TrechoTransporte, string][];
  protected readonly situacoesSolicitacao = SITUACOES_SOLICITACAO_TRANSPORTE;
  protected readonly listaSituacoesSolicitacao = Object.entries(SITUACOES_SOLICITACAO_TRANSPORTE) as [SituacaoSolicitacaoTransporte, { rotulo: string }][];
  protected readonly situacoesViagem = SITUACOES_VIAGEM;
  protected readonly diasDaSemana = DIAS_DA_SEMANA;

  protected readonly aba = signal<Aba>('frota');
  protected readonly veiculos = signal<VeiculoDto[]>([]);
  /** Os pedidos de veículo das emergências (ADR-0192). */
  protected readonly pedidosEmergencia = signal<PedidoVeiculoEmergenciaDto[]>([]);
  protected readonly disponiveisParaCeder = computed(() => this.veiculos().filter((v) => v.situacao === 'DISPONIVEL'));
  protected readonly plantoes = signal<PlantaoDto[]>([]);
  protected readonly solicitacoes = signal<SolicitacaoTransporteDto[]>([]);
  protected readonly viagens = signal<ViagemDto[]>([]);
  protected readonly km = signal<QuilometragemDto[]>([]);
  protected readonly unidades = signal<{ uuid: string; nome: string }[]>([]);
  protected readonly profissionais = signal<ProfissionalResponseDto[]>([]);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly selecionado = signal<VeiculoDto | null>(null);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly filtroSituacao = signal<SituacaoSolicitacaoTransporte | ''>('');
  protected readonly filtroMotivo = signal<MotivoTransporte | ''>('');
  protected readonly dataViagens = signal(hojeIso());
  protected readonly kmDe = signal(hojeIso(-30));
  protected readonly kmAte = signal(hojeIso());

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly veiculoForm = this.fb.nonNullable.group({
    placa: [''], prefixo: [''], tipo: ['USB' as TipoVeiculo], modelo: [''], ano: [''], lugares: [''], maca: [true], cadeiraDeRodas: [false],
    quilometragem: [''], unidadeId: [''], observacao: [''],
  });
  protected readonly situacaoForm = this.fb.nonNullable.group({ situacao: ['MANUTENCAO' as SituacaoVeiculo], motivo: [''], quilometragem: [''] });
  protected readonly plantaoForm = this.fb.nonNullable.group({
    veiculoId: [''], inicio: [''], fim: [''], observacao: [''],
    tripulacao: this.fb.array([this.tripulante('CONDUTOR'), this.tripulante('TECNICO_ENFERMAGEM')]),
  });
  protected readonly veiculoDoPlantao = signal<TipoVeiculo | null>(null);

  protected readonly solicitacaoForm = this.fb.nonNullable.group({
    buscaPaciente: [''], pacienteId: [''], unidadeId: [''], motivo: ['CONSULTA' as MotivoTransporte], trecho: ['IDA_E_VOLTA' as TrechoTransporte],
    dataHora: [''], destinoUnidadeId: [''], destino: [''], destinoMunicipio: [''], enderecoEmbarque: [''],
    maca: [false], cadeiraDeRodas: [false], oxigenio: [false], acompanhanteNome: [''], acompanhanteDocumento: [''],
    cid: [''], justificativa: [''], profissionalMatricula: [''], observacao: [''],
    repetir: [false], dias: [[] as string[]], recorrenteAte: [''],
  });
  protected readonly motivoForm = this.fb.nonNullable.group({ motivo: [''], recorrencia: [false] });
  protected readonly viagemForm = this.fb.nonNullable.group({ veiculoId: [''], condutorMatricula: [''], saida: [''], retorno: [''], destino: [''], observacao: [''] });
  protected readonly kmForm = this.fb.nonNullable.group({ quilometragem: [''] });
  protected readonly cessaoForm = this.fb.nonNullable.group({ veiculoId: [''], quilometragem: [''], observacao: [''] });
  /** Os escolhidos na gaveta de viagem e quem embarcou na gaveta de embarque. */
  protected readonly escolhidas = signal<Set<string>>(new Set());
  protected readonly presencas = signal<Record<string, { embarcou: boolean; motivo: string }>>({});
  /** Para o resumo da gaveta de viagem: os campos que mudam a compatibilidade. */
  protected readonly viagemVeiculo = signal('');
  protected readonly viagemJanela = signal<{ saida: string; retorno: string }>({ saida: '', retorno: '' });
  protected readonly motivoEscolhido = signal<MotivoTransporte>('CONSULTA');
  protected readonly repetir = signal(false);
  protected readonly buscaPaciente = signal('');

  protected readonly abas = computed<{ id: Aba; rotulo: string; contagem?: number | null }[]>(() => [
    ...(this.pode('TRANSPORTE.SOLICITAR', 'TRANSPORTE.CONSULTAR', 'TRANSPORTE.GERENCIAR', 'TFD.AUTORIZAR')
      ? [{ id: 'solicitacoes' as Aba, rotulo: 'Solicitações', contagem: this.solicitacoes().filter((s) => s.situacao === 'AGUARDANDO_AUTORIZACAO').length || null }] : []),
    ...(this.pode('TRANSPORTE.CONSULTAR', 'TRANSPORTE.GERENCIAR') ? [
      { id: 'viagens' as Aba, rotulo: 'Viagens' },
      { id: 'frota' as Aba, rotulo: 'Frota' },
      { id: 'plantoes' as Aba, rotulo: 'Plantões' },
      { id: 'quilometragem' as Aba, rotulo: 'Quilometragem' },
    ] : []),
  ]);

  protected readonly resumo = computed(() => {
    const v = this.veiculos();
    return {
      disponiveis: v.filter((x) => x.situacao === 'DISPONIVEL').length,
      emServico: v.filter((x) => x.situacao === 'EM_SERVICO').length,
      manutencao: v.filter((x) => x.situacao === 'MANUTENCAO').length,
      comPlantao: v.filter((x) => x.plantaoVigente).length,
      total: v.length,
      fila: this.solicitacoes().filter((s) => s.situacao === 'SOLICITADA' || s.situacao === 'AUTORIZADA').length,
      tfd: this.solicitacoes().filter((s) => s.situacao === 'AGUARDANDO_AUTORIZACAO').length,
    };
  });

  protected readonly solicitacoesFiltradas = computed(() => this.solicitacoes()
    .filter((s) => !this.filtroSituacao() || s.situacao === this.filtroSituacao())
    .filter((s) => !this.filtroMotivo() || s.motivo === this.filtroMotivo()));

  protected readonly veiculosEletivos = computed(() => this.veiculos().filter((v) => !v.doSamu && v.situacao !== 'INATIVO' && v.situacao !== 'MANUTENCAO'));

  /** Na gaveta de viagem: a fila (e, na troca, quem já está), com o aviso de quem não cabe na janela ou no veículo. */
  protected readonly candidatas = computed(() => {
    const g = this.gaveta();
    const atuais = g?.tipo === 'viagem' && g.item ? g.item.embarques.map((e) => e.solicitacaoId) : [];
    const janela = this.viagemJanela();
    const v = this.veiculos().find((x) => x.uuid === this.viagemVeiculo());
    return this.solicitacoes()
      .filter((s) => s.situacao === 'SOLICITADA' || s.situacao === 'AUTORIZADA' || atuais.includes(s.uuid))
      .map((s) => {
        const t = new Date(s.dataHora).getTime();
        const avisos: string[] = [];
        if (janela.saida && janela.retorno && (t < new Date(janela.saida).getTime() || t > new Date(janela.retorno).getTime())) avisos.push('fora da janela');
        if (v && s.maca && !v.maca) avisos.push('pede maca');
        if (v && s.cadeiraDeRodas && !v.cadeiraDeRodas) avisos.push('pede acesso para cadeira');
        return { s, avisos };
      });
  });

  protected readonly lugaresEscolhidos = computed(() => this.solicitacoes().filter((s) => this.escolhidas().has(s.uuid)).reduce((n, s) => n + s.lugares, 0));

  protected readonly lugaresDoVeiculo = computed(() => {
    const g = this.gaveta();
    const id = g?.tipo === 'viagem' && g.item ? g.item.veiculoId : this.viagemVeiculo();
    return this.veiculos().find((v) => v.uuid === id)?.lugares ?? null;
  });

  protected readonly pacientesFiltrados = computed(() => {
    const termo = this.buscaPaciente().trim().toLowerCase();
    const digitos = termo.replace(/\D/g, '');
    const l = this.pacientes();
    if (!termo) return l.slice(0, 50);
    return l.filter((p) => p.nome.toLowerCase().includes(termo) || (digitos && (p.cpf ?? '').includes(digitos))).slice(0, 50);
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      const primeira = this.abas()[0];
      if (primeira && !this.abas().some((x) => x.id === this.aba())) this.aba.set(primeira.id);
      this.carregar();
    });
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) =>
      this.unidades.set(l.map((u) => ({ uuid: u.uuid, nome: u.nome })).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))));
    this.route.queryParamMap.subscribe((q) => {
      const pedir = q.get('pedir') as MotivoTransporte | null;
      if (!pedir) return;
      this.aba.set('solicitacoes');
      this.abrirSolicitacao({ motivo: pedir, pacienteId: q.get('pacienteId'), unidadeId: q.get('unidadeId'),
        agendamentoId: q.get('agendamentoId') ?? undefined, internacaoId: q.get('internacaoId') ?? undefined });
    });
  }

  private tripulante(funcao: FuncaoTripulante | '' = '') {
    return this.fb.nonNullable.group({ matricula: [''], funcao: [funcao as FuncaoTripulante | ''] });
  }

  protected get tripulacao(): FormArray {
    return this.plantaoForm.controls.tripulacao;
  }

  private carregar(): void {
    if (this.pode('TRANSPORTE.CONSULTAR', 'TRANSPORTE.GERENCIAR')) {
      this.service.veiculos().pipe(catchError(() => of([]))).subscribe((v) => {
        this.veiculos.set(v);
        this.carregando.set(false);
      });
      this.service.plantoes().pipe(catchError(() => of([]))).subscribe((p) => this.plantoes.set(p));
      this.service.pedidosDeEmergencia().pipe(catchError(() => of([]))).subscribe((p) => this.pedidosEmergencia.set(p));
      this.carregarViagens();
    } else {
      this.carregando.set(false);
    }
    if (this.pode('TRANSPORTE.SOLICITAR', 'TRANSPORTE.CONSULTAR', 'TRANSPORTE.GERENCIAR', 'TFD.AUTORIZAR')) {
      this.service.solicitacoes().pipe(catchError(() => of([]))).subscribe((s) => this.solicitacoes.set(s));
    }
  }

  protected carregarViagens(): void {
    this.service.viagens(this.dataViagens(), 7).pipe(catchError(() => of([]))).subscribe((v) => this.viagens.set(v));
  }

  protected carregarKm(): void {
    this.service.quilometragem(this.kmDe(), this.kmAte()).pipe(catchError(() => of([]))).subscribe((k) => this.km.set(k));
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected irPara(a: Aba): void {
    this.aba.set(a);
    if (a === 'quilometragem' && !this.km().length) this.carregarKm();
  }

  protected navegarAbas(event: KeyboardEvent): void {
    const proxima = proximaAba(event, this.abas().map((a) => a.id), this.aba());
    if (proxima) this.irPara(proxima);
  }

  protected selecionar(uuid: string): void {
    this.service.veiculo(uuid).subscribe({ next: (v) => this.selecionado.set(v), error: () => this.selecionado.set(null) });
  }

  /** Vigente ou futuro: ainda dá para encerrar ou cancelar. */
  protected emAberto(p: PlantaoDto): boolean {
    return new Date(p.fim).getTime() > Date.now();
  }

  protected hora(iso?: string | null): string {
    return iso ? new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }) : '';
  }

  protected cancelavel(s: SolicitacaoTransporteDto): boolean {
    return s.situacao === 'SOLICITADA' || s.situacao === 'AGUARDANDO_AUTORIZACAO' || s.situacao === 'AUTORIZADA';
  }

  protected necessidades(s: { maca: boolean; cadeiraDeRodas: boolean; oxigenio: boolean; acompanhanteNome?: string | null }): string {
    return [s.maca ? 'maca' : '', s.cadeiraDeRodas ? 'cadeira de rodas' : '', s.oxigenio ? 'oxigênio' : '', s.acompanhanteNome ? 'acompanhante' : '']
      .filter(Boolean).join(' · ');
  }

  // ── Gavetas ──

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'veiculo': return g.item ? 'Editar ' + g.item.prefixo : 'Novo veículo';
      case 'situacao': return 'Situação de ' + g.item.prefixo;
      case 'plantao': return 'Novo plantão';
      case 'solicitacao': return 'Pedir transporte';
      case 'motivo': return g.acao === 'cancelarViagem' ? 'Cancelar a viagem de ' + g.item.prefixo
        : g.acao === 'negarTfd' ? 'Negar o TFD de ' + g.item.pacienteNome : 'Cancelar o transporte de ' + g.item.pacienteNome;
      case 'viagem': return g.item ? 'Embarques de ' + g.item.prefixo : 'Programar viagem';
      case 'km': return (g.acao === 'saida' ? 'Saída de ' : 'Chegada de ') + g.item.prefixo;
      case 'embarque': return 'Embarque · ' + g.item.prefixo;
      case 'ceder': return 'Ceder veículo · ' + g.pedido.emergenciaNome;
      case 'devolver': return 'Devolver ' + g.cessao.prefixo;
    }
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  private carregarProfissionais(): void {
    if (!this.profissionais().length) {
      this.profissionalService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.profissionais.set(l.filter((p) => p.ativo !== false)));
    }
  }

  protected abrirVeiculo(v: VeiculoDto | null): void {
    this.veiculoForm.reset({
      placa: v?.placa ?? '', prefixo: v?.prefixo ?? '', tipo: v?.tipo ?? 'USB', modelo: v?.modelo ?? '', ano: v?.ano != null ? String(v.ano) : '',
      lugares: v?.lugares != null ? String(v.lugares) : '', maca: v?.maca ?? true, cadeiraDeRodas: v?.cadeiraDeRodas ?? false,
      quilometragem: v?.quilometragem != null ? String(v.quilometragem) : '', unidadeId: v?.unidadeId ?? '', observacao: v?.observacao ?? '',
    });
    this.abrir({ tipo: 'veiculo', item: v });
  }

  protected abrirSituacao(v: VeiculoDto): void {
    this.situacaoForm.reset({ situacao: v.situacao === 'DISPONIVEL' ? 'MANUTENCAO' : 'DISPONIVEL', motivo: '', quilometragem: '' });
    this.abrir({ tipo: 'situacao', item: v });
  }

  protected abrirPlantao(v: VeiculoDto | null): void {
    const agora = new Date();
    agora.setMinutes(0, 0, 0);
    const fim = new Date(agora.getTime() + 12 * 3600_000);
    this.tripulacao.clear();
    const funcoesPadrao: FuncaoTripulante[] = v?.tipo === 'USA' ? ['CONDUTOR', 'ENFERMEIRO', 'MEDICO'] : v?.tipo === 'MOTOLANCIA' ? ['TECNICO_ENFERMAGEM']
      : v && !v.doSamu ? ['CONDUTOR'] : ['CONDUTOR', 'TECNICO_ENFERMAGEM'];
    funcoesPadrao.forEach((f) => this.tripulacao.push(this.tripulante(f)));
    this.plantaoForm.patchValue({ veiculoId: v?.uuid ?? '', inicio: localIso(agora), fim: localIso(fim), observacao: '' });
    this.veiculoDoPlantao.set(v?.tipo ?? null);
    this.carregarProfissionais();
    this.abrir({ tipo: 'plantao', item: v });
  }

  protected mudarVeiculoDoPlantao(id: string): void {
    this.veiculoDoPlantao.set(this.veiculos().find((v) => v.uuid === id)?.tipo ?? null);
  }

  protected adicionarTripulante(): void {
    this.tripulacao.push(this.tripulante());
  }

  protected removerTripulante(i: number): void {
    this.tripulacao.removeAt(i);
  }

  protected abrirSolicitacao(pre: { motivo?: MotivoTransporte; pacienteId?: string | null; unidadeId?: string | null; agendamentoId?: string;
    internacaoId?: string } = {}): void {
    const amanha = new Date(Date.now() + 86_400_000);
    amanha.setHours(8, 0, 0, 0);
    const motivo = pre.motivo ?? 'CONSULTA';
    this.solicitacaoForm.reset({
      buscaPaciente: '', pacienteId: pre.pacienteId ?? '', unidadeId: pre.unidadeId ?? '', motivo, trecho: motivo === 'ALTA_HOSPITALAR' ? 'IDA' : 'IDA_E_VOLTA',
      dataHora: pre.agendamentoId ? '' : localIso(amanha), destinoUnidadeId: '', destino: '', destinoMunicipio: '', enderecoEmbarque: '', maca: false,
      cadeiraDeRodas: false, oxigenio: false, acompanhanteNome: '', acompanhanteDocumento: '', cid: '', justificativa: '', profissionalMatricula: '',
      observacao: '', repetir: false, dias: [], recorrenteAte: '',
    });
    this.motivoEscolhido.set(motivo);
    this.repetir.set(false);
    this.buscaPaciente.set('');
    if (!this.pacientes().length) this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    this.carregarProfissionais();
    this.abrir({ tipo: 'solicitacao', vinculo: { agendamentoId: pre.agendamentoId, internacaoId: pre.internacaoId } });
  }

  protected alternarDia(dia: string, marcado: boolean): void {
    const atuais = this.solicitacaoForm.controls.dias.value;
    this.solicitacaoForm.controls.dias.setValue(marcado ? [...atuais, dia] : atuais.filter((d) => d !== dia));
  }

  protected abrirMotivo(acao: 'cancelarSolicitacao' | 'negarTfd', item: SolicitacaoTransporteDto): void {
    this.motivoForm.reset({ motivo: '', recorrencia: false });
    this.abrir({ tipo: 'motivo', acao, item });
  }

  protected abrirCancelarViagem(item: ViagemDto): void {
    this.motivoForm.reset({ motivo: '', recorrencia: false });
    this.abrir({ tipo: 'motivo', acao: 'cancelarViagem', item });
  }

  protected autorizar(s: SolicitacaoTransporteDto): void {
    this.enviar(this.service.autorizarTfd(s.uuid), 'TFD autorizado', (r) => r.pacienteNome, false);
  }

  protected abrirViagem(item: ViagemDto | null): void {
    const saida = new Date(Date.now() + 86_400_000);
    saida.setHours(6, 0, 0, 0);
    const retorno = new Date(saida.getTime() + 12 * 3600_000);
    const s = item ? localIso(new Date(item.saidaPrevista)) : localIso(saida);
    const r = item ? localIso(new Date(item.retornoPrevisto)) : localIso(retorno);
    this.viagemForm.reset({ veiculoId: item?.veiculoId ?? '', condutorMatricula: '', saida: s, retorno: r, destino: '', observacao: '' });
    this.viagemVeiculo.set(item?.veiculoId ?? '');
    this.viagemJanela.set({ saida: s, retorno: r });
    this.escolhidas.set(new Set(item ? item.embarques.map((e) => e.solicitacaoId) : []));
    this.carregarProfissionais();
    this.abrir({ tipo: 'viagem', item });
  }

  protected atualizarJanela(): void {
    const v = this.viagemForm.getRawValue();
    this.viagemJanela.set({ saida: v.saida, retorno: v.retorno });
    this.viagemVeiculo.set(v.veiculoId);
  }

  protected alternarEscolhida(id: string, marcado: boolean): void {
    const s = new Set(this.escolhidas());
    if (marcado) s.add(id);
    else s.delete(id);
    this.escolhidas.set(s);
  }

  protected abrirCeder(pedido: PedidoVeiculoEmergenciaDto): void {
    this.cessaoForm.reset({ veiculoId: '', quilometragem: '', observacao: '' });
    this.abrir({ tipo: 'ceder', pedido });
  }

  protected abrirDevolver(cessao: CessaoVeiculoDto): void {
    this.cessaoForm.reset({ veiculoId: '', quilometragem: cessao.kmInicio != null ? String(cessao.kmInicio) : '', observacao: '' });
    this.abrir({ tipo: 'devolver', cessao });
  }

  protected abrirKm(acao: 'saida' | 'chegada', item: ViagemDto): void {
    const sugestao = acao === 'saida' ? this.veiculos().find((v) => v.uuid === item.veiculoId)?.quilometragem : item.kmSaida;
    this.kmForm.reset({ quilometragem: sugestao != null ? String(sugestao) : '' });
    this.abrir({ tipo: 'km', acao, item });
  }

  protected abrirEmbarque(item: ViagemDto): void {
    const p: Record<string, { embarcou: boolean; motivo: string }> = {};
    item.embarques.forEach((e) => (p[e.solicitacaoId] = { embarcou: e.situacao !== 'FALTOU', motivo: '' }));
    this.presencas.set(p);
    this.abrir({ tipo: 'embarque', item });
  }

  protected marcarPresenca(id: string, campo: 'embarcou' | 'motivo', valor: boolean | string): void {
    this.presencas.update((p) => ({ ...p, [id]: { ...p[id], [campo]: valor } }));
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

  private numero(v: string): number | null {
    return String(v).trim() === '' ? null : Number(v);
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    const erros: Record<string, string> = {};
    switch (g.tipo) {
      case 'veiculo': {
        const v = this.veiculoForm.getRawValue();
        if (!/^[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}$/.test(v.placa.trim())) erros['placaVeiculo'] = 'Use o padrão ABC1234 ou o Mercosul ABC1D23.';
        if (!v.prefixo.trim()) erros['prefixoVeiculo'] = 'Informe o prefixo.';
        if (!v.unidadeId) erros['unidadeVeiculo'] = 'Escolha a unidade de lotação.';
        if (!this.validar(erros)) return;
        const dto = { placa: v.placa.trim(), prefixo: v.prefixo.trim(), tipo: v.tipo, modelo: v.modelo.trim() || null, ano: this.numero(v.ano),
          lugares: this.numero(v.lugares), maca: v.maca, cadeiraDeRodas: v.cadeiraDeRodas, quilometragem: this.numero(v.quilometragem), unidadeId: v.unidadeId,
          observacao: v.observacao.trim() || null };
        this.enviar(g.item ? this.service.atualizar(g.item.uuid, dto) : this.service.criar(dto), g.item ? 'Veículo atualizado' : 'Veículo cadastrado', (r) => r.prefixo, true);
        return;
      }
      case 'situacao': {
        const v = this.situacaoForm.getRawValue();
        if (!v.motivo.trim()) erros['motivoSituacao'] = 'Informe o motivo.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.mudarSituacao(g.item.uuid, v.situacao, v.motivo.trim(), this.numero(v.quilometragem)),
          SITUACOES_VEICULO[v.situacao].rotulo, (r) => r.prefixo, true);
        return;
      }
      case 'plantao': {
        const v = this.plantaoForm.getRawValue();
        if (!v.veiculoId) erros['veiculoPlantao'] = 'Escolha a viatura.';
        if (!v.inicio) erros['inicioPlantao'] = 'Informe o início.';
        if (!v.fim) erros['fimPlantao'] = 'Informe o fim.';
        else if (v.inicio && new Date(v.fim) <= new Date(v.inicio)) erros['fimPlantao'] = 'O fim precisa ser depois do início.';
        const tripulacao = v.tripulacao.filter((t) => t.matricula && t.funcao) as { matricula: string; funcao: FuncaoTripulante }[];
        if (!tripulacao.length) erros['tripulante0'] = 'Informe a tripulação.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.criarPlantao({ veiculoId: v.veiculoId, inicio: new Date(v.inicio).toISOString(), fim: new Date(v.fim).toISOString(), tripulacao,
          observacao: v.observacao.trim() || null }), 'Plantão registrado', (p) => p.prefixo, false);
        return;
      }
      case 'solicitacao':
        return this.salvarSolicitacao(g.vinculo);
      case 'motivo': {
        const m = this.motivoForm.getRawValue();
        if (!m.motivo.trim()) erros['motivoTransporte'] = 'Informe o motivo.';
        if (!this.validar(erros)) return;
        if (g.acao === 'cancelarViagem') {
          this.enviar(this.service.cancelarViagem(g.item.uuid, m.motivo.trim()), 'Viagem cancelada', (r) => r.prefixo, false);
        } else if (g.acao === 'negarTfd') {
          this.enviar(this.service.negarTfd(g.item.uuid, m.motivo.trim()), 'TFD negado', (r) => r.pacienteNome, false);
        } else {
          this.enviar(this.service.cancelarSolicitacao(g.item.uuid, m.motivo.trim(), m.recorrencia), 'Transporte cancelado',
            (r) => r.length > 1 ? `${r.length} sessões` : r[0]?.pacienteNome ?? '', false);
        }
        return;
      }
      case 'viagem': {
        const ids = [...this.escolhidas()];
        if (!ids.length) erros['solicitacoesViagem'] = 'Escolha ao menos um paciente.';
        if (g.item) {
          if (!this.validar(erros)) return;
          this.enviar(this.service.trocarEmbarques(g.item.uuid, ids), 'Embarques atualizados', (r) => r.prefixo, false);
          return;
        }
        const v = this.viagemForm.getRawValue();
        if (!v.veiculoId) erros['veiculoViagem'] = 'Escolha o veículo.';
        if (!v.condutorMatricula) erros['condutorViagem'] = 'Escolha o condutor.';
        if (!v.saida) erros['saidaViagem'] = 'Informe a saída.';
        if (!v.retorno) erros['retornoViagem'] = 'Informe o retorno.';
        else if (v.saida && new Date(v.retorno) <= new Date(v.saida)) erros['retornoViagem'] = 'O retorno vem depois da saída.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.programarViagem({ veiculoId: v.veiculoId, condutorMatricula: v.condutorMatricula, saidaPrevista: new Date(v.saida).toISOString(),
          retornoPrevisto: new Date(v.retorno).toISOString(), destino: v.destino.trim() || null, observacao: v.observacao.trim() || null, solicitacoes: ids }),
          'Viagem programada', (r) => `${r.prefixo} · ${r.embarques.length} paciente(s)`, false);
        return;
      }
      case 'km': {
        const km = this.numero(this.kmForm.getRawValue().quilometragem);
        if (km == null || km < 0) erros['kmViagem'] = 'Informe a quilometragem do painel.';
        if (!this.validar(erros)) return;
        this.enviar(g.acao === 'saida' ? this.service.sair(g.item.uuid, km!) : this.service.chegar(g.item.uuid, km!),
          g.acao === 'saida' ? 'Saída registrada' : 'Chegada registrada', (r) => r.kmPercorridos != null ? `${r.prefixo} · ${r.kmPercorridos} km` : r.prefixo, false);
        return;
      }
      case 'ceder': {
        const v = this.cessaoForm.getRawValue();
        if (!v.veiculoId) erros['veiculoCessao'] = 'Escolha o veículo.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.ceder(g.pedido.mobilizacaoId, v.veiculoId, v.observacao.trim() || null), 'Veículo cedido',
          (r) => (r.avisos.length ? r.avisos.join(' ') : `${r.cessao.prefixo} em serviço na emergência ${r.cessao.emergenciaNome}.`), false);
        return;
      }
      case 'devolver': {
        const v = this.cessaoForm.getRawValue();
        const km = this.numero(v.quilometragem);
        if (km != null && g.cessao.kmInicio != null && km < g.cessao.kmInicio) erros['kmCessao'] = `Na cessão estava em ${g.cessao.kmInicio}.`;
        if (!this.validar(erros)) return;
        this.enviar(this.service.devolver(g.cessao.uuid, km, v.observacao.trim() || null), 'Veículo devolvido',
          (c) => `${c.prefixo} disponível de novo.`, false);
        return;
      }
      case 'embarque': {
        const p = this.presencas();
        const itens = Object.entries(p).map(([solicitacaoId, x]) => ({ solicitacaoId, embarcou: x.embarcou, motivo: x.embarcou ? null : x.motivo.trim() || null }));
        this.enviar(this.service.registrarEmbarque(g.item.uuid, itens), 'Embarque registrado',
          (r) => `${r.embarques.filter((e) => e.situacao === 'EMBARCOU').length} embarcou(aram)`, false);
        return;
      }
    }
  }

  private salvarSolicitacao(vinculo: { agendamentoId?: string; internacaoId?: string }): void {
    const v = this.solicitacaoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['pacienteTransporte'] = 'Escolha o paciente.';
    if (!v.unidadeId) erros['unidadeTransporte'] = 'Escolha a unidade que pede.';
    if (!vinculo.agendamentoId && !v.dataHora) erros['dataHoraTransporte'] = 'Informe a data e a hora do compromisso.';
    if (!vinculo.agendamentoId && !v.destinoUnidadeId && !v.destino.trim() && v.motivo !== 'ALTA_HOSPITALAR') erros['destinoTransporte'] = 'Informe o destino.';
    if (v.maca && !v.acompanhanteNome.trim()) erros['acompanhanteTransporte'] = 'Quem vai de maca precisa de acompanhante.';
    if (v.motivo === 'TFD') {
      if (!v.destinoMunicipio.trim() && !v.destinoUnidadeId) erros['municipioTransporte'] = 'Informe o município de destino do TFD.';
      if (!v.cid.trim()) erros['cidTransporte'] = 'Informe o CID do laudo.';
      if (!v.justificativa.trim()) erros['justificativaTransporte'] = 'Informe a justificativa do laudo.';
      if (!v.profissionalMatricula) erros['profissionalTransporte'] = 'Escolha quem assina o laudo.';
    }
    const recorrente = v.repetir && (v.motivo === 'HEMODIALISE' || v.motivo === 'TERAPIA_SERIADA');
    if (recorrente) {
      if (!v.dias.length) erros['diasTransporte'] = 'Escolha os dias da semana.';
      if (!v.recorrenteAte) erros['ateTransporte'] = 'Informe até quando.';
    }
    if (!this.validar(erros)) return;
    const dto: SolicitacaoTransporteRequestDto = {
      pacienteId: v.pacienteId, unidadeId: v.unidadeId, motivo: v.motivo, trecho: v.trecho,
      agendamentoId: vinculo.agendamentoId ?? null, internacaoId: vinculo.internacaoId ?? null,
      dataHora: vinculo.agendamentoId ? null : v.dataHora ? v.dataHora + ':00' : null,
      enderecoEmbarque: v.enderecoEmbarque.trim() || null, destinoUnidadeId: v.destinoUnidadeId || null, destino: v.destino.trim() || null,
      destinoMunicipio: v.destinoMunicipio.trim() || null, maca: v.maca, cadeiraDeRodas: v.cadeiraDeRodas, oxigenio: v.oxigenio,
      acompanhanteNome: v.acompanhanteNome.trim() || null, acompanhanteDocumento: v.acompanhanteDocumento.trim() || null,
      cid: v.motivo === 'TFD' ? v.cid.trim() : null, justificativa: v.motivo === 'TFD' ? v.justificativa.trim() : null,
      profissionalMatricula: v.motivo === 'TFD' ? v.profissionalMatricula : null, observacao: v.observacao.trim() || null,
      diasDaSemana: recorrente ? v.dias : null, recorrenteAte: recorrente ? v.recorrenteAte : null,
    };
    if (recorrente) {
      this.enviar(this.service.solicitarRecorrente(dto), 'Sessões pedidas', (r) => `${r.length} transporte(s) de ${MOTIVOS_TRANSPORTE[v.motivo]}`, false);
    } else {
      this.enviar(this.service.solicitar(dto), v.motivo === 'TFD' ? 'TFD enviado à comissão' : 'Transporte pedido', (r) => r.pacienteNome, false);
    }
  }

  protected encerrarPlantao(p: PlantaoDto): void {
    this.enviar(this.service.encerrarPlantao(p.uuid), p.vigente ? 'Plantão encerrado' : 'Plantão cancelado', (r) => r.prefixo, false);
  }

  private enviar<T>(req: Observable<T>, titulo: string, detalhe: (r: T) => string, veiculo: boolean): void {
    this.submitting.set(true);
    req.subscribe({
      next: (r) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe: detalhe(r) });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        if (veiculo) this.selecionado.set(r as unknown as VeiculoDto);
        else if (this.selecionado()) this.selecionar(this.selecionado()!.uuid);
        this.carregar();
        if (this.aba() === 'quilometragem') this.carregarKm();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        const m = (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.';
        if (this.gaveta()) this.erroApi.set(m);
        else this.toast.set({ titulo: 'Não foi possível', detalhe: m });
      },
    });
  }
}
