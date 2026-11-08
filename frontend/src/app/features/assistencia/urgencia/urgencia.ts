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
  | { tipo: 'paciente'; o: OcorrenciaUrgenciaDto };

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

  /** A decisão escolhida na gaveta decide se pede recurso e destino. */
  protected readonly decisaoNaGaveta = signal<DecisaoRegulacaoUrgencia>('ENVIO_DE_RECURSO');
  protected readonly vagaZeroNaGaveta = signal(false);
  protected readonly origemNaGaveta = signal<OrigemOcorrenciaUrgencia>('CHAMADO_192');

  protected readonly abertas = computed(() => this.painel()?.abertas ?? []);

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
      conduta: '', tipoRecurso: '', recursoIdentificacao: '', destinoId: '', vagaZero: false, justificativaVagaZero: '' });
    this.decisaoNaGaveta.set(this.regularForm.getRawValue().decisao);
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
      if (v.decisao === 'ENVIO_DE_RECURSO' && !v.tipoRecurso) erros['recursoRegular'] = 'Escolha o recurso.';
      if ((v.decisao === 'ENCAMINHAMENTO' || v.decisao === 'TRANSFERENCIA') && !v.destinoId) erros['destinoRegular'] = 'Escolha o destino.';
      if (v.vagaZero && !v.justificativaVagaZero.trim()) erros['vagaZeroRegular'] = 'Justifique a vaga zero.';
      if (!this.validar(erros)) return;
      const orientacao = v.decisao === 'ORIENTACAO';
      this.enviar(this.service.regular(g.o.uuid, {
        profissionalMatricula: v.profissionalMatricula.trim(), prioridade: v.prioridade as PrioridadeRegulacao, decisao: v.decisao, conduta: v.conduta.trim(),
        tipoRecurso: comRecurso && v.tipoRecurso ? v.tipoRecurso : null, recursoIdentificacao: comRecurso ? v.recursoIdentificacao.trim() || null : null,
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
