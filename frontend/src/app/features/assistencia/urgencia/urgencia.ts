import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { PrioridadeRegulacao } from '../../../core/models/regulacao';
import {
  CentralUrgenciaDto,
  DECISOES,
  DESFECHOS,
  DecisaoRegulacaoUrgencia,
  DesfechoOcorrenciaUrgencia,
  DestinoUrgenciaDto,
  EVENTOS_URGENCIA,
  LOCAIS,
  LocalOcorrencia,
  ORIGENS,
  OcorrenciaUrgenciaDto,
  OrigemOcorrenciaUrgencia,
  PainelUrgenciaDto,
  RECURSOS,
  SITUACOES_URGENCIA,
  TipoEventoUrgencia,
  TipoRecursoUrgencia,
  AtendimentoAphDto,
  DESFECHOS_APH,
  DesfechoAph,
  FichaAphDto,
  FichaAphRequestDto,
  MECANISMOS_TRAUMA,
  MecanismoTrauma,
  NATUREZAS_APH,
  NaturezaAph,
  PROCEDIMENTOS_APH,
  ProcedimentoAph,
  RegistroAphRequestDto,
  SinaisAphDto,
  VIAS_APH,
  ViaAph,
} from '../../../core/models/urgencia';
import { AuthService } from '../../../core/services/auth';
import { PacienteService } from '../../../core/services/paciente';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { UrgenciaService } from '../../../core/services/urgencia';
import { Drawer } from '../../../shared/drawer/drawer';
import { PRIORIDADES } from '../regulacao/regulacao';

type Gaveta =
  | { tipo: 'chamado' }
  | { tipo: 'regular'; o: OcorrenciaUrgenciaDto }
  | { tipo: 'destino'; o: OcorrenciaUrgenciaDto }
  | { tipo: 'reclassificar'; o: OcorrenciaUrgenciaDto }
  | { tipo: 'encerrar'; o: OcorrenciaUrgenciaDto }
  | { tipo: 'cancelar'; o: OcorrenciaUrgenciaDto }
  | { tipo: 'paciente'; o: OcorrenciaUrgenciaDto }
  | { tipo: 'aph'; atendimento: AtendimentoAphDto };

/** ISO para o valor de um input datetime-local (hora local). */
function paraLocal(iso: string): string {
  const d = new Date(iso);
  const p = (v: number) => String(v).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
}

/** O próximo passo do deslocamento, na ordem que o serviço exige. */
const PROXIMO_PASSO: Partial<Record<TipoEventoUrgencia | 'NENHUM', TipoEventoUrgencia>> = {
  NENHUM: 'ENVIO_DE_RECURSO',
  ENVIO_DE_RECURSO: 'CHEGADA_AO_LOCAL',
  CHEGADA_AO_LOCAL: 'SAIDA_DO_LOCAL',
  SAIDA_DO_LOCAL: 'CHEGADA_AO_DESTINO',
};

/**
 * Regulação de urgência (ADR-0176): o painel da central do 192 — aguardando regulação primeiro, depois a gravidade e o tempo de
 * espera —, a ficha com a linha do tempo e as ações de cada papel: o atendente abre e registra os deslocamentos, o médico
 * regulador decide e indica o destino, a unidade de destino recebe.
 */
@Component({
  selector: 'app-urgencia',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './urgencia.html',
})
export class Urgencia implements OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly service = inject(UrgenciaService);
  private readonly pacienteService = inject(PacienteService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly prioridades = PRIORIDADES;
  protected readonly listaPrioridades = Object.keys(PRIORIDADES) as PrioridadeRegulacao[];
  protected readonly origens = ORIGENS;
  protected readonly situacoes = SITUACOES_URGENCIA;
  protected readonly decisoes = DECISOES;
  protected readonly listaDecisoes = Object.entries(DECISOES) as [DecisaoRegulacaoUrgencia, string][];
  protected readonly recursos = RECURSOS;
  protected readonly listaRecursos = Object.entries(RECURSOS) as [TipoRecursoUrgencia, string][];
  protected readonly desfechos = DESFECHOS;
  protected readonly listaDesfechos = (Object.entries(DESFECHOS) as [DesfechoOcorrenciaUrgencia, string][])
    .filter(([d]) => d !== 'ORIENTADO' && d !== 'RECEBIDO_NO_DESTINO');
  protected readonly eventos = EVENTOS_URGENCIA;
  protected readonly locais = LOCAIS;
  protected readonly listaLocais = Object.entries(LOCAIS) as [LocalOcorrencia, string][];

  protected readonly painel = signal<PainelUrgenciaDto | null>(null);

  /** Ficha do APH (ADR-0188). */
  protected readonly naturezasAph = NATUREZAS_APH;
  protected readonly listaNaturezasAph = Object.keys(NATUREZAS_APH) as NaturezaAph[];
  protected readonly mecanismosTrauma = MECANISMOS_TRAUMA;
  protected readonly listaMecanismos = Object.keys(MECANISMOS_TRAUMA) as MecanismoTrauma[];
  protected readonly procedimentosAph = PROCEDIMENTOS_APH;
  protected readonly listaProcedimentosAph = Object.keys(PROCEDIMENTOS_APH) as ProcedimentoAph[];
  protected readonly desfechosAph = DESFECHOS_APH;
  protected readonly listaDesfechosAph = Object.keys(DESFECHOS_APH) as DesfechoAph[];
  protected readonly viasAph = VIAS_APH;
  protected readonly listaViasAph = Object.keys(VIAS_APH) as ViaAph[];
  protected readonly atendimentosAph = signal<AtendimentoAphDto[]>([]);
  protected readonly fichaAberta = signal<FichaAphDto | null>(null);
  protected readonly fichaDaSelecionada = signal<FichaAphDto | null>(null);
  protected readonly procedimentosSel = signal<Set<ProcedimentoAph>>(new Set());
  protected readonly matriculaAph = signal('');
  protected readonly centrais = signal<CentralUrgenciaDto[]>([]);
  protected readonly destinos = signal<DestinoUrgenciaDto[]>([]);
  protected readonly unidades = signal<{ uuid: string; nome: string }[]>([]);
  protected readonly centralId = signal('');
  protected readonly carregando = signal(true);
  protected readonly selecionada = signal<OcorrenciaUrgenciaDto | null>(null);
  protected readonly agora = signal(Date.now());
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  private readonly matriculaPadrao = signal('');
  private readonly relogio = setInterval(() => this.agora.set(Date.now()), 30000);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly chamadoForm = this.fb.nonNullable.group({
    centralId: [''],
    origem: ['CHAMADO_192' as OrigemOcorrenciaUrgencia],
    solicitanteNome: [''],
    solicitanteTelefone: [''],
    unidadeSolicitanteId: [''],
    cpfPaciente: [''],
    descricaoPaciente: [''],
    localTipo: ['' as LocalOcorrencia | ''],
    endereco: [''],
    referencia: [''],
    queixa: [''],
  });
  protected readonly regularForm = this.fb.nonNullable.group({
    profissionalMatricula: [''],
    prioridade: ['' as PrioridadeRegulacao | ''],
    decisao: ['ENVIO_DE_RECURSO' as DecisaoRegulacaoUrgencia],
    conduta: [''],
    veiculoId: [''],
    tipoRecurso: ['' as TipoRecursoUrgencia | ''],
    recursoIdentificacao: [''],
    destinoId: [''],
    vagaZero: [false],
    justificativaVagaZero: [''],
  });
  protected readonly destinoForm = this.fb.nonNullable.group({ profissionalMatricula: [''], destinoId: [''], vagaZero: [false], justificativaVagaZero: [''] });
  protected readonly reclassificarForm = this.fb.nonNullable.group({ profissionalMatricula: [''], prioridade: ['' as PrioridadeRegulacao | ''], motivo: [''] });
  protected readonly encerrarForm = this.fb.nonNullable.group({ profissionalMatricula: [''], desfecho: ['' as DesfechoOcorrenciaUrgencia | ''], observacao: [''] });
  protected readonly motivoForm = this.fb.nonNullable.group({ motivo: [''] });
  protected readonly pacienteForm = this.fb.nonNullable.group({ cpf: [''] });
  protected readonly fichaForm = this.fb.nonNullable.group({
    natureza: ['' as NaturezaAph | ''], mecanismoTrauma: ['' as MecanismoTrauma | ''], situacaoEncontrada: [''], hemorragia: ['' as '' | 'SIM' | 'NAO'],
    viaAerea: [''], respiracao: [''], circulacao: [''], glasgowOcular: [''], glasgowVerbal: [''], glasgowMotor: [''], pupilas: [''], exposicao: [''],
    procedimentoOutro: [''], impressaoDiagnostica: [''], cid: [''], desfecho: ['' as DesfechoAph | ''], recusaNome: [''], obitoEm: [''],
    obitoConstatadoPorMatricula: [''], recebidoPor: [''], entregueEm: [''],
  });
  protected readonly sinaisForm = this.fb.nonNullable.group({
    paSistolica: [''], paDiastolica: [''], frequenciaCardiaca: [''], frequenciaRespiratoria: [''], saturacao: [''], temperatura: [''], glicemia: [''], dor: [''], glasgow: [''],
  });
  protected readonly medicamentoAphForm = this.fb.nonNullable.group({ nome: [''], dose: [''], via: ['INTRAVENOSA' as ViaAph] });
  protected readonly adendoForm = this.fb.nonNullable.group({ texto: [''] });

  /** A decisão escolhida na gaveta decide se pede recurso e destino. */
  protected readonly decisaoNaGaveta = signal<DecisaoRegulacaoUrgencia>('ENVIO_DE_RECURSO');
  protected readonly vagaZeroNaGaveta = signal(false);
  protected readonly origemNaGaveta = signal<OrigemOcorrenciaUrgencia>('CHAMADO_192');

  protected readonly abertas = computed(() => this.painel()?.abertas ?? []);
  protected readonly viaturas = computed(() => this.painel()?.viaturas ?? []);
  /** Com viatura da frota escolhida, o tipo e o texto saem dela. */
  protected readonly viaturaNaGaveta = signal('');

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.authService.usuarioAtual().subscribe((u) => this.matriculaPadrao.set(u?.profissionalMatricula ?? ''));
    forkJoin({
      centrais: this.service.centrais().pipe(catchError(() => of([]))),
      destinos: this.service.destinos().pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.centrais.set(r.centrais);
      this.destinos.set(r.destinos);
      if (r.centrais.length === 1) this.centralId.set(r.centrais[0].unidadeId);
      this.carregar();
    });
  }

  ngOnDestroy(): void {
    clearInterval(this.relogio);
  }

  protected carregar(): void {
    this.service.painel(this.centralId() || null).pipe(catchError(() => of(null))).subscribe((p) => {
      this.painel.set(p);
      this.carregando.set(false);
    });
    this.service.destinos().pipe(catchError(() => of([]))).subscribe((d) => this.destinos.set(d));
    this.carregarAtendimentos();
  }

  protected mudarCentral(id: string): void {
    this.centralId.set(id);
    this.carregar();
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected selecionar(uuid: string): void {
    this.service.detalhe(uuid).subscribe({ next: (o) => this.selecionada.set(o), error: () => this.selecionada.set(null) });
    this.fichaDaSelecionada.set(null);
    if (this.pode('URGENCIA.REGULAR', 'URGENCIA.RECEBER', 'APH.REGISTRAR', 'ATENDIMENTO.GERENCIAR')) {
      this.service.fichaAph(uuid).pipe(catchError(() => of(null))).subscribe((f) => this.fichaDaSelecionada.set(f));
    }
  }

  /** "12 min" ou "1 h 05" desde o chamado. */
  protected espera(o: OcorrenciaUrgenciaDto): string {
    const min = Math.max(0, Math.floor((this.agora() - new Date(o.recebidaEm).getTime()) / 60000));
    return min < 60 ? `${min} min` : `${Math.floor(min / 60)} h ${String(min % 60).padStart(2, '0')}`;
  }

  protected hora(iso?: string | null): string {
    if (!iso) return '';
    const d = new Date(iso);
    return d.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
  }

  protected minutos(v?: number | null): string {
    return v == null ? '—' : `${v.toLocaleString('pt-BR')} min`;
  }

  protected proximoPasso(o: OcorrenciaUrgenciaDto): TipoEventoUrgencia | null {
    if (!o.tipoRecurso || (o.situacao !== 'REGULADA' && o.situacao !== 'EM_ATENDIMENTO')) return null;
    const passo = PROXIMO_PASSO[o.ultimoDeslocamento ?? 'NENHUM'] ?? null;
    return passo === 'CHEGADA_AO_DESTINO' && !o.destinoId ? null : passo;
  }

  protected podeReceber(o: OcorrenciaUrgenciaDto): boolean {
    if (!o.destinoId || (o.situacao !== 'REGULADA' && o.situacao !== 'EM_ATENDIMENTO') || o.decisao === 'ORIENTACAO') return false;
    return !o.tipoRecurso || o.ultimoDeslocamento === 'SAIDA_DO_LOCAL' || o.ultimoDeslocamento === 'CHEGADA_AO_DESTINO';
  }

  protected aberta(o: OcorrenciaUrgenciaDto): boolean {
    return o.situacao === 'RECEBIDA' || o.situacao === 'REGULADA' || o.situacao === 'EM_ATENDIMENTO';
  }

  // ── Gavetas ──

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected abrirChamado(): void {
    this.chamadoForm.reset({ centralId: this.centralId() || (this.centrais()[0]?.unidadeId ?? ''), origem: 'CHAMADO_192', solicitanteNome: '', solicitanteTelefone: '',
      unidadeSolicitanteId: '', cpfPaciente: '', descricaoPaciente: '', localTipo: '', endereco: '', referencia: '', queixa: '' });
    this.origemNaGaveta.set('CHAMADO_192');
    if (!this.unidades().length) {
      this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) =>
        this.unidades.set(l.map((u) => ({ uuid: u.uuid, nome: u.nome })).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))));
    }
    this.abrir({ tipo: 'chamado' });
  }

  protected abrirRegular(o: OcorrenciaUrgenciaDto): void {
    this.regularForm.reset({ profissionalMatricula: this.matriculaPadrao(), prioridade: '', decisao: o.unidadeSolicitanteId && o.origem === 'UNIDADE_DE_SAUDE' ? 'TRANSFERENCIA' : 'ENVIO_DE_RECURSO',
      conduta: '', veiculoId: '', tipoRecurso: '', recursoIdentificacao: '', destinoId: '', vagaZero: false, justificativaVagaZero: '' });
    this.decisaoNaGaveta.set(this.regularForm.getRawValue().decisao);
    this.viaturaNaGaveta.set('');
    this.vagaZeroNaGaveta.set(false);
    this.abrir({ tipo: 'regular', o });
  }

  protected abrirDestino(o: OcorrenciaUrgenciaDto): void {
    this.destinoForm.reset({ profissionalMatricula: this.matriculaPadrao(), destinoId: o.destinoId ?? '', vagaZero: false, justificativaVagaZero: '' });
    this.vagaZeroNaGaveta.set(false);
    this.abrir({ tipo: 'destino', o });
  }

  protected abrirReclassificar(o: OcorrenciaUrgenciaDto): void {
    this.reclassificarForm.reset({ profissionalMatricula: this.matriculaPadrao(), prioridade: '', motivo: '' });
    this.abrir({ tipo: 'reclassificar', o });
  }

  protected abrirEncerrar(o: OcorrenciaUrgenciaDto): void {
    this.encerrarForm.reset({ profissionalMatricula: this.matriculaPadrao(), desfecho: '', observacao: '' });
    this.abrir({ tipo: 'encerrar', o });
  }

  protected abrirCancelar(o: OcorrenciaUrgenciaDto): void {
    this.motivoForm.reset({ motivo: '' });
    this.abrir({ tipo: 'cancelar', o });
  }

  protected abrirPaciente(o: OcorrenciaUrgenciaDto): void {
    this.pacienteForm.reset({ cpf: '' });
    this.abrir({ tipo: 'paciente', o });
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

  private destinoNome(id: string): string {
    return this.destinos().find((d) => d.unidadeId === id)?.nome ?? '';
  }

  protected destinoSemVaga(id: string): boolean {
    const d = this.destinos().find((x) => x.unidadeId === id);
    return !!d && d.leitosAtivos > 0 && d.leitosLivres === 0;
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    const erros: Record<string, string> = {};
    if (g.tipo === 'chamado') {
      const v = this.chamadoForm.getRawValue();
      if (!v.centralId) erros['centralChamado'] = 'Escolha a central.';
      if (v.origem === 'CHAMADO_192' && !v.solicitanteTelefone.trim()) erros['telefoneChamado'] = 'Informe o telefone de retorno.';
      if (v.origem === 'UNIDADE_DE_SAUDE' && !v.unidadeSolicitanteId) erros['unidadeChamado'] = 'Escolha a unidade que pede.';
      if (!v.queixa.trim()) erros['queixaChamado'] = 'Descreva a queixa.';
      if (!this.validar(erros)) return;
      const cpf = v.cpfPaciente.replace(/\D/g, '');
      const paciente$: Observable<string | null> = cpf
        ? new Observable((s) => {
            this.pacienteService.buscarPorCpf(cpf).subscribe({ next: (l) => { s.next(l[0]?.uuid ?? null); s.complete(); }, error: () => { s.next(null); s.complete(); } });
          })
        : of(null);
      this.submitting.set(true);
      paciente$.subscribe((pacienteId) => {
        if (cpf && !pacienteId) {
          this.submitting.set(false);
          this.validar({ cpfChamado: 'Nenhum paciente com esse CPF; deixe em branco e descreva a pessoa.' });
          return;
        }
        this.enviar(this.service.abrir({
          centralId: v.centralId, origem: v.origem, solicitanteNome: v.solicitanteNome.trim() || null, solicitanteTelefone: v.solicitanteTelefone.trim() || null,
          unidadeSolicitanteId: v.origem === 'UNIDADE_DE_SAUDE' ? v.unidadeSolicitanteId : null, pacienteId, descricaoPaciente: v.descricaoPaciente.trim() || null,
          localTipo: v.localTipo || null, endereco: v.endereco.trim() || null, referencia: v.referencia.trim() || null, queixa: v.queixa.trim(),
        }), 'Ocorrência aberta');
      });
      return;
    }
    if (g.tipo === 'regular') {
      const v = this.regularForm.getRawValue();
      if (!v.profissionalMatricula.trim()) erros['reguladorRegular'] = 'Informe a matrícula do médico regulador.';
      if (!v.prioridade) erros['prioridadeRegular'] = 'Classifique a gravidade.';
      if (!v.conduta.trim()) erros['condutaRegular'] = 'Descreva a conduta.';
      const comRecurso = v.decisao === 'ENVIO_DE_RECURSO' || v.decisao === 'TRANSFERENCIA';
      if (v.decisao === 'ENVIO_DE_RECURSO' && !v.tipoRecurso && !v.veiculoId) erros['recursoRegular'] = 'Escolha a viatura ou o tipo de recurso.';
      if ((v.decisao === 'ENCAMINHAMENTO' || v.decisao === 'TRANSFERENCIA') && !v.destinoId) erros['destinoRegular'] = 'Escolha o destino.';
      if (v.vagaZero && !v.justificativaVagaZero.trim()) erros['vagaZeroRegular'] = 'Justifique a vaga zero.';
      if (!this.validar(erros)) return;
      const orientacao = v.decisao === 'ORIENTACAO';
      this.enviar(this.service.regular(g.o.uuid, {
        profissionalMatricula: v.profissionalMatricula.trim(), prioridade: v.prioridade as PrioridadeRegulacao, decisao: v.decisao, conduta: v.conduta.trim(),
        veiculoId: comRecurso && v.veiculoId ? v.veiculoId : null,
        tipoRecurso: comRecurso && v.tipoRecurso ? v.tipoRecurso : null, recursoIdentificacao: comRecurso && !v.veiculoId ? v.recursoIdentificacao.trim() || null : null,
        destinoId: orientacao ? null : v.destinoId || null, vagaZero: !orientacao && !!v.destinoId && v.vagaZero,
        justificativaVagaZero: !orientacao && v.vagaZero ? v.justificativaVagaZero.trim() : null,
      }), orientacao ? 'Orientação registrada' : 'Ocorrência regulada');
      return;
    }
    if (g.tipo === 'destino') {
      const v = this.destinoForm.getRawValue();
      if (!v.profissionalMatricula.trim()) erros['reguladorDestino'] = 'Informe a matrícula do médico regulador.';
      if (!v.destinoId) erros['destinoDestino'] = 'Escolha o destino.';
      if (v.vagaZero && !v.justificativaVagaZero.trim()) erros['vagaZeroDestino'] = 'Justifique a vaga zero.';
      if (!this.validar(erros)) return;
      this.enviar(this.service.destino(g.o.uuid, { profissionalMatricula: v.profissionalMatricula.trim(), destinoId: v.destinoId, vagaZero: v.vagaZero,
        justificativaVagaZero: v.vagaZero ? v.justificativaVagaZero.trim() : null }), 'Destino: ' + this.destinoNome(v.destinoId));
      return;
    }
    if (g.tipo === 'reclassificar') {
      const v = this.reclassificarForm.getRawValue();
      if (!v.profissionalMatricula.trim()) erros['reguladorReclassificar'] = 'Informe a matrícula do médico regulador.';
      if (!v.prioridade) erros['prioridadeReclassificar'] = 'Escolha a nova gravidade.';
      if (!v.motivo.trim()) erros['motivoReclassificar'] = 'Informe o motivo.';
      if (!this.validar(erros)) return;
      this.enviar(this.service.reclassificar(g.o.uuid, { profissionalMatricula: v.profissionalMatricula.trim(), prioridade: v.prioridade as PrioridadeRegulacao,
        motivo: v.motivo.trim() }), 'Gravidade reclassificada');
      return;
    }
    if (g.tipo === 'encerrar') {
      const v = this.encerrarForm.getRawValue();
      if (!v.profissionalMatricula.trim()) erros['reguladorEncerrar'] = 'Informe a matrícula do médico regulador.';
      if (!v.desfecho) erros['desfechoEncerrar'] = 'Escolha o desfecho.';
      if (!v.observacao.trim()) erros['observacaoEncerrar'] = 'Descreva o encerramento.';
      if (!this.validar(erros)) return;
      this.enviar(this.service.encerrar(g.o.uuid, { profissionalMatricula: v.profissionalMatricula.trim(), desfecho: v.desfecho as DesfechoOcorrenciaUrgencia,
        observacao: v.observacao.trim() }), 'Ocorrência encerrada');
      return;
    }
    if (g.tipo === 'cancelar') {
      const v = this.motivoForm.getRawValue();
      if (!v.motivo.trim()) erros['motivoCancelar'] = 'Informe o motivo.';
      if (!this.validar(erros)) return;
      this.enviar(this.service.cancelar(g.o.uuid, v.motivo.trim()), 'Ocorrência cancelada');
      return;
    }
    if (g.tipo === 'aph') {
      this.salvarFicha(g.atendimento);
      return;
    }
    const cpf = this.pacienteForm.getRawValue().cpf.replace(/\D/g, '');
    if (cpf.length !== 11) erros['cpfPaciente'] = 'Informe o CPF com 11 dígitos.';
    if (!this.validar(erros)) return;
    this.submitting.set(true);
    this.pacienteService.buscarPorCpf(cpf).subscribe({
      next: (l) => {
        if (!l.length) {
          this.submitting.set(false);
          this.validar({ cpfPaciente: 'Nenhum paciente com esse CPF.' });
          return;
        }
        this.enviar(this.service.vincularPaciente(g.o.uuid, l[0].uuid), 'Paciente identificado');
      },
      error: () => {
        this.submitting.set(false);
        this.validar({ cpfPaciente: 'Não foi possível buscar o paciente.' });
      },
    });
  }

  protected registrarPasso(o: OcorrenciaUrgenciaDto, tipo: TipoEventoUrgencia): void {
    this.enviar(this.service.deslocamento(o.uuid, tipo), this.eventos[tipo]);
  }

  protected receber(o: OcorrenciaUrgenciaDto): void {
    this.enviar(this.service.receber(o.uuid), 'Paciente recebido');
  }


  // ── Ficha do APH (ADR-0188) ──

  protected carregarAtendimentos(): void {
    if (!this.pode('APH.REGISTRAR')) return;
    this.service.atendimentosAph(this.centralId() || null).pipe(catchError(() => of([]))).subscribe((a) => this.atendimentosAph.set(a));
  }

  /** Abre a gaveta da ficha: carrega a que existe ou começa uma em branco. */
  protected abrirFichaAph(a: AtendimentoAphDto): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.fichaAberta.set(null);
    this.preencherFicha(null);
    this.sinaisForm.reset({ paSistolica: '', paDiastolica: '', frequenciaCardiaca: '', frequenciaRespiratoria: '', saturacao: '', temperatura: '', glicemia: '', dor: '', glasgow: '' });
    this.medicamentoAphForm.reset({ nome: '', dose: '', via: 'INTRAVENOSA' });
    this.adendoForm.reset({ texto: '' });
    this.matriculaAph.set(this.matriculaPadrao());
    this.gaveta.set({ tipo: 'aph', atendimento: a });
    if (a.fichaId) {
      this.service.fichaAph(a.ocorrenciaId).subscribe({ next: (f) => { this.fichaAberta.set(f); this.preencherFicha(f); }, error: () => this.fichaAberta.set(null) });
    }
  }

  private preencherFicha(f: FichaAphDto | null): void {
    this.fichaForm.reset({
      natureza: f?.natureza ?? '', mecanismoTrauma: f?.mecanismoTrauma ?? '', situacaoEncontrada: f?.situacaoEncontrada ?? '',
      hemorragia: f?.hemorragiaExsanguinante == null ? '' : f.hemorragiaExsanguinante ? 'SIM' : 'NAO',
      viaAerea: f?.viaAerea ?? '', respiracao: f?.respiracao ?? '', circulacao: f?.circulacao ?? '',
      glasgowOcular: f?.glasgowOcular?.toString() ?? '', glasgowVerbal: f?.glasgowVerbal?.toString() ?? '', glasgowMotor: f?.glasgowMotor?.toString() ?? '',
      pupilas: f?.pupilas ?? '', exposicao: f?.exposicao ?? '', procedimentoOutro: f?.procedimentoOutro ?? '',
      impressaoDiagnostica: f?.impressaoDiagnostica ?? '', cid: f?.cid ?? '', desfecho: f?.desfecho ?? '', recusaNome: f?.recusaNome ?? '',
      obitoEm: f?.obitoEm ? paraLocal(f.obitoEm) : '', obitoConstatadoPorMatricula: '', recebidoPor: f?.recebidoPor ?? '',
      entregueEm: f?.entregueEm ? paraLocal(f.entregueEm) : '',
    });
    this.procedimentosSel.set(new Set(f?.procedimentos ?? []));
  }

  protected alternarProcedimento(p: ProcedimentoAph, marcado: boolean): void {
    const s = new Set(this.procedimentosSel());
    if (marcado) s.add(p); else s.delete(p);
    this.procedimentosSel.set(s);
  }

  protected glasgowNaFicha(): number | null {
    const v = this.fichaForm.getRawValue();
    return v.glasgowOcular && v.glasgowVerbal && v.glasgowMotor ? Number(v.glasgowOcular) + Number(v.glasgowVerbal) + Number(v.glasgowMotor) : null;
  }

  private salvarFicha(a: AtendimentoAphDto): void {
    const v = this.fichaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (this.procedimentosSel().has('OUTRO') && !v.procedimentoOutro.trim()) erros['procedimentoOutro'] = 'Descreva o outro procedimento.';
    if (!this.validar(erros)) return;
    const n = (x: string) => (x ? Number(x) : null);
    const dto: FichaAphRequestDto = {
      natureza: v.natureza || null, mecanismoTrauma: v.natureza === 'TRAUMA' ? v.mecanismoTrauma || null : null,
      situacaoEncontrada: v.situacaoEncontrada.trim() || null, hemorragiaExsanguinante: v.hemorragia === '' ? null : v.hemorragia === 'SIM',
      viaAerea: v.viaAerea.trim() || null, respiracao: v.respiracao.trim() || null, circulacao: v.circulacao.trim() || null,
      glasgowOcular: n(v.glasgowOcular), glasgowVerbal: n(v.glasgowVerbal), glasgowMotor: n(v.glasgowMotor),
      pupilas: v.pupilas.trim() || null, exposicao: v.exposicao.trim() || null, procedimentos: [...this.procedimentosSel()],
      procedimentoOutro: v.procedimentoOutro.trim() || null, impressaoDiagnostica: v.impressaoDiagnostica.trim() || null, cid: v.cid.trim() || null,
      desfecho: v.desfecho || null, recusaNome: v.recusaNome.trim() || null, obitoEm: v.obitoEm ? new Date(v.obitoEm).toISOString() : null,
      obitoConstatadoPorMatricula: v.obitoConstatadoPorMatricula.trim() || (this.fichaAberta()?.obitoConstatadoPorNome ? undefined : null) || null,
      recebidoPor: v.recebidoPor.trim() || null, entregueEm: v.entregueEm ? new Date(v.entregueEm).toISOString() : null,
    };
    this.acaoAph(this.service.salvarFichaAph(a.ocorrenciaId, dto), 'Ficha salva');
  }

  protected registrarSinais(): void {
    const f = this.fichaAberta();
    if (!f) return;
    const v = this.sinaisForm.getRawValue();
    const n = (x: string) => (x.trim() ? Number(x.replace(',', '.')) : undefined);
    const dto: RegistroAphRequestDto = {
      profissionalMatricula: this.matriculaAph(), paSistolica: n(v.paSistolica), paDiastolica: n(v.paDiastolica), frequenciaCardiaca: n(v.frequenciaCardiaca),
      frequenciaRespiratoria: n(v.frequenciaRespiratoria), saturacao: n(v.saturacao), temperatura: n(v.temperatura), glicemia: n(v.glicemia),
      dor: n(v.dor), glasgow: n(v.glasgow),
    };
    this.acaoAph(this.service.registroAph(f.uuid, 'sinais-vitais', dto), 'Sinais vitais registrados', () =>
      this.sinaisForm.reset({ paSistolica: '', paDiastolica: '', frequenciaCardiaca: '', frequenciaRespiratoria: '', saturacao: '', temperatura: '', glicemia: '', dor: '', glasgow: '' }));
  }

  protected registrarMedicamentoAph(): void {
    const f = this.fichaAberta();
    if (!f) return;
    const v = this.medicamentoAphForm.getRawValue();
    this.acaoAph(this.service.registroAph(f.uuid, 'medicamentos', { profissionalMatricula: this.matriculaAph(), nome: v.nome.trim(), dose: v.dose.trim(), via: v.via }),
      'Medicamento registrado', () => this.medicamentoAphForm.reset({ nome: '', dose: '', via: 'INTRAVENOSA' }));
  }

  protected finalizarFicha(): void {
    const f = this.fichaAberta();
    if (!f) return;
    this.acaoAph(this.service.registroAph(f.uuid, 'finalizacao', { profissionalMatricula: this.matriculaAph() }), 'Ficha finalizada');
  }

  protected registrarAdendo(): void {
    const f = this.fichaAberta();
    if (!f) return;
    this.acaoAph(this.service.registroAph(f.uuid, 'adendo', { profissionalMatricula: this.matriculaAph(), texto: this.adendoForm.getRawValue().texto.trim() }),
      'Adendo registrado', () => this.adendoForm.reset({ texto: '' }));
  }

  /** Ações da ficha: a gaveta fica aberta com a ficha atualizada. */
  private acaoAph(req: Observable<FichaAphDto>, titulo: string, depois?: () => void): void {
    if (!this.matriculaAph().trim()) {
      this.erroApi.set('Informe a sua matrícula no topo da ficha.');
      return;
    }
    this.submitting.set(true);
    this.erroApi.set(null);
    req.subscribe({
      next: (f) => {
        this.submitting.set(false);
        this.fichaAberta.set(f);
        this.preencherFicha(f);
        depois?.();
        this.toast.set({ titulo, detalhe: 'Ocorrência ' + f.protocolo });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        this.carregarAtendimentos();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  protected baixarFichaAph(f: FichaAphDto): void {
    this.service.baixarFichaAph(f.uuid).subscribe({
      next: (r) => {
        const url = URL.createObjectURL(r.body ?? new Blob());
        const a = document.createElement('a');
        a.href = url;
        a.download = `ficha-aph-${f.protocolo}.pdf`;
        a.click();
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: () => this.toast.set({ titulo: 'Não foi possível gerar o PDF', detalhe: 'Ocorrência ' + f.protocolo }),
    });
  }

  protected resumoSinais(s: SinaisAphDto): string {
    const p: string[] = [];
    if (s.paSistolica != null || s.paDiastolica != null) p.push(`PA ${s.paSistolica ?? '?'}/${s.paDiastolica ?? '?'}`);
    if (s.frequenciaCardiaca != null) p.push(`FC ${s.frequenciaCardiaca}`);
    if (s.frequenciaRespiratoria != null) p.push(`FR ${s.frequenciaRespiratoria}`);
    if (s.saturacao != null) p.push(`SpO2 ${s.saturacao}%`);
    if (s.temperatura != null) p.push(`T ${s.temperatura} °C`);
    if (s.glicemia != null) p.push(`Glic ${s.glicemia}`);
    if (s.dor != null) p.push(`Dor ${s.dor}`);
    if (s.glasgow != null) p.push(`GCS ${s.glasgow}`);
    return p.join(' · ');
  }

  private enviar(req: Observable<OcorrenciaUrgenciaDto>, titulo: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: (o) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe: 'Ocorrência ' + o.protocolo });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        this.selecionada.set(o);
        this.carregar();
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
