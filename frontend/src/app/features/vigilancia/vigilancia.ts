import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../core/models/auth';
import { PacienteResponseDto } from '../../core/models/paciente';
import { ErrorResponseDto } from '../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import {
  AgravoDto,
  BoletimVigilanciaDto,
  CLASSIFICACOES,
  CRITERIOS,
  CampoFichaDto,
  ClassificacaoCaso,
  CriterioClassificacao,
  EVOLUCOES,
  NotificacaoDto,
  NotificacoesDto,
  ORIGENS,
  OrigemNotificacao,
  PERIODICIDADES_NOTIFICACAO,
  PeriodicidadeNotificacao,
  SITUACOES_NOTIFICACAO,
  SemanasEpidemiologicasDto,
  SugestaoNotificacaoDto,
  TIPOS_CAMPO,
  TipoCampoFicha,
} from '../../core/models/vigilancia';
import { AuthService } from '../../core/services/auth';
import { PacienteService } from '../../core/services/paciente';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { VigilanciaService } from '../../core/services/vigilancia';
import { Drawer } from '../../shared/drawer/drawer';

type Aba = 'notificacoes' | 'sugestoes' | 'semanas' | 'agravos';
type Gaveta =
  | { tipo: 'notificar'; prefixo: Partial<{ pacienteId: string; pacienteNome: string; unidadeId: string; agravoId: string; origem: OrigemNotificacao;
      internacaoId: string; pedidoExameId: string; criseId: string }> }
  | { tipo: 'notificacao'; id: string }
  | { tipo: 'agravo'; agravo: AgravoDto | null }
  | { tipo: 'ficha'; agravo: AgravoDto };

interface CampoEdicao {
  chave: string;
  rotulo: string;
  tipo: TipoCampoFicha;
  obrigatorio: boolean;
  opcoes: string;
}

/**
 * Vigilância epidemiológica, fatia V1 (ADR-0124): notificações (com investigação, classificação e encerramento),
 * sugestões de notificação a partir da rede, notificação negativa semanal e o catálogo de agravos com a ficha
 * versionada. O formulário da notificação é montado a partir da ficha do agravo.
 */
@Component({
  selector: 'app-vigilancia',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './vigilancia.html',
})
export class Vigilancia {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly service = inject(VigilanciaService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly pacienteService = inject(PacienteService);

  protected readonly situacoes = SITUACOES_NOTIFICACAO;
  protected readonly classificacoes = CLASSIFICACOES;
  protected readonly listaClassificacoes = Object.keys(CLASSIFICACOES) as ClassificacaoCaso[];
  protected readonly criterios = CRITERIOS;
  protected readonly listaCriterios = Object.keys(CRITERIOS) as CriterioClassificacao[];
  protected readonly evolucoes = EVOLUCOES;
  protected readonly listaEvolucoes = Object.keys(EVOLUCOES);
  protected readonly origens = ORIGENS;
  protected readonly listaOrigens = Object.keys(ORIGENS) as OrigemNotificacao[];
  protected readonly periodicidades = PERIODICIDADES_NOTIFICACAO;
  protected readonly listaPeriodicidades = Object.keys(PERIODICIDADES_NOTIFICACAO) as PeriodicidadeNotificacao[];
  protected readonly tiposCampo = TIPOS_CAMPO;
  protected readonly listaTiposCampo = Object.keys(TIPOS_CAMPO) as TipoCampoFicha[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('notificacoes');
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly unidadeFiltro = signal('');
  protected readonly agravoFiltro = signal('');
  protected readonly situacaoFiltro = signal('');
  protected readonly atrasadasFiltro = signal(false);
  protected readonly agravos = signal<AgravoDto[]>([]);
  protected readonly notificacoes = signal<NotificacoesDto | null>(null);
  protected readonly sugestoes = signal<SugestaoNotificacaoDto[]>([]);
  protected readonly semanas = signal<SemanasEpidemiologicasDto | null>(null);
  protected readonly boletim = signal<BoletimVigilanciaDto | null>(null);
  protected readonly detalhe = signal<NotificacaoDto | null>(null);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly buscaPaciente = signal('');
  protected readonly respostas = signal<Record<string, unknown>>({});
  protected readonly camposEdicao = signal<CampoEdicao[]>([]);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly notificarForm = this.fb.nonNullable.group({
    pacienteId: [''], unidadeId: [''], agravoId: [''], origem: ['ATENDIMENTO' as OrigemNotificacao], dataPrimeirosSintomas: [''],
    dataNotificacao: [''], localProvavel: [''], internacaoId: [''], pedidoExameId: [''], criseId: [''],
  });
  protected readonly investigacaoForm = this.fb.nonNullable.group({ exposicao: [''], localProvavelInfeccao: [''], medidas: [''] });
  protected readonly classificacaoForm = this.fb.nonNullable.group({ classificacao: [''], criterio: [''], justificativa: [''] });
  protected readonly encerramentoForm = this.fb.nonNullable.group({ evolucao: [''] });
  protected readonly agravoForm = this.fb.nonNullable.group({
    codigo: [''], nome: [''], descricao: [''], cids: [''], compulsorio: [true], periodicidade: ['SEMANAL' as PeriodicidadeNotificacao],
    prazoEncerramentoDias: ['60'], sensivel: [false], ativo: [true],
  });
  protected readonly criteriosEscolhidos = signal<Set<CriterioClassificacao>>(new Set());
  protected readonly negativaForm = this.fb.nonNullable.group({ unidadeId: [''] });

  protected readonly podeNotificar = computed(() => this.pode('VIGILANCIA.NOTIFICAR'));
  protected readonly podeInvestigar = computed(() => this.pode('VIGILANCIA.INVESTIGAR'));
  protected readonly podeGerenciar = computed(() => this.pode('VIGILANCIA.GERENCIAR'));

  /** A ficha vigente do agravo escolhido na notificação. */
  protected readonly fichaEscolhida = computed<CampoFichaDto[]>(() => {
    const id = this.agravoEscolhido();
    return this.agravos().find((a) => a.uuid === id)?.ficha ?? [];
  });
  private readonly agravoEscolhido = signal('');

  protected readonly pacientesEncontrados = computed(() => {
    const q = this.buscaPaciente().trim().toLowerCase();
    if (q.length < 2) return [];
    const digitos = q.replace(/\D/g, '');
    return this.pacientes()
      .filter((p) => p.ativo && (p.nome.toLowerCase().includes(q) || (digitos.length >= 3 && ((p.cpf ?? '').includes(digitos) || (p.cartaoSus ?? '').includes(digitos)))))
      .slice(0, 8);
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.service.agravos().pipe(catchError(() => of([]))).subscribe((a) => {
      this.agravos.set(a);
      this.abrirPeloLink();
    });
    this.carregar();
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  /** Vindo da crise em saúde mental (ADR-0123): abre a notificação de violência já preenchida. */
  private abrirPeloLink(): void {
    const q = this.route.snapshot.queryParamMap;
    if (q.get('notificar') !== '1') return;
    const agravo = this.agravos().find((a) => a.codigo === (q.get('agravo') ?? ''));
    this.abrirNotificar({
      pacienteId: q.get('pacienteId') ?? '', pacienteNome: q.get('pacienteNome') ?? '', unidadeId: q.get('unidadeId') ?? '',
      agravoId: agravo?.uuid ?? '', origem: (q.get('origem') as OrigemNotificacao) ?? 'ATENDIMENTO', criseId: q.get('criseId') ?? '',
    });
  }

  protected carregar(): void {
    const unidadeId = this.unidadeFiltro() || undefined;
    this.service.notificacoes({ unidadeId, agravoId: this.agravoFiltro() || undefined, situacao: this.situacaoFiltro() || undefined,
      atrasadas: this.atrasadasFiltro() }).pipe(catchError(() => of(null))).subscribe((n) => this.notificacoes.set(n));
    if (this.podeNotificar()) {
      this.service.sugestoes(unidadeId).pipe(catchError(() => of([]))).subscribe((s) => this.sugestoes.set(s));
      this.service.semanas(unidadeId).pipe(catchError(() => of(null))).subscribe((s) => this.semanas.set(s));
    }
    this.service.boletim(unidadeId).pipe(catchError(() => of(null))).subscribe((b) => this.boletim.set(b));
  }

  protected filtrar(campo: 'unidade' | 'agravo' | 'situacao', valor: string): void {
    if (campo === 'unidade') this.unidadeFiltro.set(valor);
    if (campo === 'agravo') this.agravoFiltro.set(valor);
    if (campo === 'situacao') this.situacaoFiltro.set(valor);
    this.carregar();
  }

  protected alternarAtrasadas(): void {
    this.atrasadasFiltro.set(!this.atrasadasFiltro());
    this.carregar();
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  protected dataHora(iso: string | null | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  protected erroCampo(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  protected resposta(chave: string): unknown {
    return this.respostas()[chave];
  }

  protected responder(chave: string, valor: unknown): void {
    this.respostas.update((r) => ({ ...r, [chave]: valor }));
  }

  protected alternarOpcao(chave: string, opcao: string, marcado: boolean): void {
    const atual = (this.respostas()[chave] as string[] | undefined) ?? [];
    this.responder(chave, marcado ? [...atual, opcao] : atual.filter((o) => o !== opcao));
  }

  protected marcada(chave: string, opcao: string): boolean {
    return ((this.respostas()[chave] as string[] | undefined) ?? []).includes(opcao);
  }

  protected textoResposta(v: unknown): string {
    if (v === true) return 'Sim';
    if (v === false) return 'Não';
    if (Array.isArray(v)) return v.join(', ');
    return v == null || v === '' ? '—' : String(v);
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    if (g.tipo === 'notificar') return 'Notificar agravo';
    if (g.tipo === 'notificacao') return 'Notificação';
    if (g.tipo === 'agravo') return g.agravo ? 'Editar agravo' : 'Novo agravo';
    return `Ficha de ${g.agravo.nome}`;
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirNotificar(prefixo: Extract<Gaveta, { tipo: 'notificar' }>['prefixo'] = {}): void {
    this.notificarForm.reset({
      pacienteId: prefixo.pacienteId ?? '', unidadeId: prefixo.unidadeId ?? this.unidadeFiltro(), agravoId: prefixo.agravoId ?? '',
      origem: prefixo.origem ?? 'ATENDIMENTO', dataPrimeirosSintomas: '', dataNotificacao: '', localProvavel: '',
      internacaoId: prefixo.internacaoId ?? '', pedidoExameId: prefixo.pedidoExameId ?? '', criseId: prefixo.criseId ?? '',
    });
    this.agravoEscolhido.set(prefixo.agravoId ?? '');
    this.respostas.set({});
    this.buscaPaciente.set(prefixo.pacienteNome ?? '');
    if (!this.pacientes().length) this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    this.abrir({ tipo: 'notificar', prefixo });
  }

  protected notificarSugestao(s: SugestaoNotificacaoDto): void {
    this.abrirNotificar({
      pacienteId: s.pacienteId, pacienteNome: s.pacienteNome, unidadeId: s.unidadeId, agravoId: s.agravoId, origem: s.origem,
      internacaoId: s.origem === 'INTERNACAO' ? s.referenciaId : undefined, pedidoExameId: s.origem === 'EXAME' ? s.referenciaId : undefined,
      criseId: s.origem === 'SAUDE_MENTAL' ? s.referenciaId : undefined,
    });
  }

  protected escolherPaciente(p: PacienteResponseDto): void {
    this.notificarForm.patchValue({ pacienteId: p.uuid });
    this.buscaPaciente.set(p.nome);
  }

  protected escolherAgravo(id: string): void {
    this.notificarForm.patchValue({ agravoId: id });
    this.agravoEscolhido.set(id);
    this.respostas.set({});
  }

  protected abrirNotificacao(id: string): void {
    this.detalhe.set(null);
    this.investigacaoForm.reset({ exposicao: '', localProvavelInfeccao: '', medidas: '' });
    this.classificacaoForm.reset({ classificacao: '', criterio: '', justificativa: '' });
    this.encerramentoForm.reset({ evolucao: '' });
    this.abrir({ tipo: 'notificacao', id });
    this.service.notificacao(id).subscribe({
      next: (n) => {
        this.detalhe.set(n);
        this.investigacaoForm.reset({ exposicao: n.investigacao?.exposicao ?? '', localProvavelInfeccao: n.investigacao?.localProvavelInfeccao ?? '',
          medidas: n.investigacao?.medidas ?? '' });
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  /** Os critérios aceitos pelo agravo da notificação aberta. */
  protected criteriosDo(n: NotificacaoDto): CriterioClassificacao[] {
    return this.agravos().find((a) => a.uuid === n.agravoId)?.criterios ?? this.listaCriterios;
  }

  protected abrirAgravo(agravo: AgravoDto | null): void {
    this.agravoForm.reset({
      codigo: agravo?.codigo ?? '', nome: agravo?.nome ?? '', descricao: agravo?.descricao ?? '', cids: (agravo?.cids ?? []).join(', '),
      compulsorio: agravo?.compulsorio ?? true, periodicidade: agravo?.periodicidade ?? 'SEMANAL',
      prazoEncerramentoDias: String(agravo?.prazoEncerramentoDias ?? 60), sensivel: agravo?.sensivel ?? false, ativo: agravo?.ativo ?? true,
    });
    this.criteriosEscolhidos.set(new Set(agravo?.criterios ?? ['CLINICO']));
    this.abrir({ tipo: 'agravo', agravo });
  }

  protected alternarCriterio(c: CriterioClassificacao, marcado: boolean): void {
    const s = new Set(this.criteriosEscolhidos());
    if (marcado) s.add(c);
    else s.delete(c);
    this.criteriosEscolhidos.set(s);
  }

  protected abrirFicha(agravo: AgravoDto): void {
    this.camposEdicao.set(agravo.ficha.map((c) => ({ chave: c.chave, rotulo: c.rotulo, tipo: c.tipo, obrigatorio: !!c.obrigatorio,
      opcoes: (c.opcoes ?? []).join(', ') })));
    if (!agravo.ficha.length) this.adicionarCampo();
    this.abrir({ tipo: 'ficha', agravo });
  }

  protected adicionarCampo(): void {
    this.camposEdicao.update((c) => [...c, { chave: '', rotulo: '', tipo: 'SIM_NAO', obrigatorio: false, opcoes: '' }]);
  }

  protected removerCampo(i: number): void {
    this.camposEdicao.update((c) => c.filter((_, k) => k !== i));
  }

  protected editarCampo(i: number, campo: keyof CampoEdicao, valor: string | boolean): void {
    this.camposEdicao.update((c) => c.map((x, k) => (k === i ? { ...x, [campo]: valor } : x)));
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    if (g.tipo === 'notificar') this.salvarNotificacao();
    else if (g.tipo === 'agravo') this.salvarAgravo(g.agravo);
    else if (g.tipo === 'ficha') this.salvarFicha(g.agravo);
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

  private salvarNotificacao(): void {
    const v = this.notificarForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.pacienteId) erros['buscaPaciente'] = 'Escolha o paciente na lista.';
    if (!v.unidadeId) erros['unidadeNotificante'] = 'Escolha a unidade notificante.';
    if (!v.agravoId) erros['agravo'] = 'Escolha o agravo.';
    for (const c of this.fichaEscolhida()) {
      const r = this.respostas()[c.chave];
      const vazio = r == null || r === '' || (Array.isArray(r) && !r.length);
      if (c.obrigatorio && vazio) erros['ficha-' + c.chave] = 'Responda este campo.';
    }
    if (!this.validar(erros)) return;
    const respostas: Record<string, unknown> = {};
    for (const [k, r] of Object.entries(this.respostas())) {
      if (r == null || r === '' || (Array.isArray(r) && !r.length)) continue;
      const tipo = this.fichaEscolhida().find((c) => c.chave === k)?.tipo;
      respostas[k] = tipo === 'NUMERO' ? Number(r) : r;
    }
    this.enviar(this.service.notificar({
      pacienteId: v.pacienteId, unidadeId: v.unidadeId, agravoId: v.agravoId, origem: v.origem,
      dataPrimeirosSintomas: v.dataPrimeirosSintomas || null, dataNotificacao: v.dataNotificacao || null, localProvavel: v.localProvavel.trim() || null,
      internacaoId: v.internacaoId || null, pedidoExameId: v.pedidoExameId || null, criseId: v.criseId || null, respostas,
    }), (n) => {
      this.mostrarToast('Notificação registrada', n.aviso ?? `${n.pacienteNome} · ${n.agravoNome}`);
      this.abrirNotificacao(n.uuid);
    });
  }

  protected salvarInvestigacao(n: NotificacaoDto): void {
    const v = this.investigacaoForm.getRawValue();
    this.enviar(this.service.investigar(n.uuid, { exposicao: v.exposicao.trim() || null, localProvavelInfeccao: v.localProvavelInfeccao.trim() || null,
      medidas: v.medidas.trim() || null, examesIds: n.investigacao?.examesIds ?? [] }), (d) => {
      this.detalhe.set(d);
      this.mostrarToast('Investigação salva', d.pacienteNome);
    });
  }

  protected salvarClassificacao(n: NotificacaoDto): void {
    const v = this.classificacaoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.classificacao) erros['classificacao'] = 'Escolha a classificação.';
    if ((v.classificacao === 'CONFIRMADO' || v.classificacao === 'DESCARTADO') && !v.criterio) erros['criterio'] = 'Confirmado e descartado pedem o critério.';
    if (!this.validar(erros)) return;
    this.enviar(this.service.classificar(n.uuid, { classificacao: v.classificacao, criterio: v.criterio || null, justificativa: v.justificativa.trim() || null }),
      (d) => {
        this.detalhe.set(d);
        this.classificacaoForm.reset({ classificacao: '', criterio: '', justificativa: '' });
        this.mostrarToast('Caso classificado', `${d.pacienteNome} · ${this.classificacoes[d.classificacaoAtual!].rotulo}`);
      });
  }

  protected salvarEncerramento(n: NotificacaoDto): void {
    const v = this.encerramentoForm.getRawValue();
    if (!this.validar(v.evolucao ? {} : { evolucao: 'Escolha a evolução.' })) return;
    this.enviar(this.service.encerrar(n.uuid, v.evolucao), (d) => {
      this.detalhe.set(d);
      this.mostrarToast('Notificação encerrada', d.pacienteNome);
    });
  }

  protected declararNegativa(unidadeId: string, ano?: number, semana?: number): void {
    this.enviar(this.service.declararNegativa(unidadeId, ano, semana), (n) => this.mostrarToast('Notificação negativa registrada',
      `${n.unidadeNome} · semana ${n.semana}/${n.ano}`));
  }

  private salvarAgravo(agravo: AgravoDto | null): void {
    const v = this.agravoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!/^[A-Z0-9_]{2,60}$/.test(v.codigo.trim())) erros['codigoAgravo'] = 'Use letras maiúsculas, números e sublinhado (ex.: DENGUE).';
    if (!v.nome.trim()) erros['nomeAgravo'] = 'Informe o nome.';
    const prazo = Number(v.prazoEncerramentoDias);
    if (!Number.isInteger(prazo) || prazo < 1 || prazo > 730) erros['prazoEncerramento'] = 'De 1 a 730 dias.';
    if (!this.criteriosEscolhidos().size) erros['criterios'] = 'Escolha pelo menos um critério.';
    if (!this.validar(erros)) return;
    const dto = {
      codigo: v.codigo.trim(), nome: v.nome.trim(), descricao: v.descricao.trim() || null,
      cids: v.cids.split(/[\s,;]+/).map((c) => c.trim().toUpperCase()).filter((c) => c), compulsorio: v.compulsorio, periodicidade: v.periodicidade,
      prazoEncerramentoDias: prazo, criterios: [...this.criteriosEscolhidos()], sensivel: v.sensivel, ativo: v.ativo,
    };
    this.enviar(agravo ? this.service.atualizarAgravo(agravo.uuid, dto) : this.service.criarAgravo(dto), (a) => {
      this.mostrarToast(agravo ? 'Agravo atualizado' : 'Agravo cadastrado', a.nome);
      this.fecharGaveta();
    });
  }

  private salvarFicha(agravo: AgravoDto): void {
    const campos = this.camposEdicao();
    const erros: Record<string, string> = {};
    campos.forEach((c, i) => {
      if (!/^[a-zA-Z][a-zA-Z0-9]{0,39}$/.test(c.chave)) erros['chaveCampo-' + i] = 'Letras e números, começando por letra.';
      if (!c.rotulo.trim()) erros['rotuloCampo-' + i] = 'Informe o rótulo.';
      if ((c.tipo === 'OPCAO' || c.tipo === 'MULTIPLA') && !c.opcoes.trim()) erros['opcoesCampo-' + i] = 'Informe as opções, separadas por vírgula.';
    });
    if (!this.validar(erros)) return;
    this.enviar(this.service.novaFicha(agravo.uuid, campos.map((c) => ({
      chave: c.chave, rotulo: c.rotulo.trim(), tipo: c.tipo, obrigatorio: c.obrigatorio,
      opcoes: c.tipo === 'OPCAO' || c.tipo === 'MULTIPLA' ? c.opcoes.split(',').map((o) => o.trim()).filter((o) => o) : null,
    }))), (a) => {
      this.mostrarToast('Ficha salva', `${a.nome} · versão ${a.fichaVersao}`);
      this.fecharGaveta();
    });
  }

  private enviar<T>(req: Observable<T>, depois: (r: T) => void): void {
    this.submitting.set(true);
    this.erroApi.set(null);
    req.subscribe({
      next: (r) => {
        this.submitting.set(false);
        depois(r);
        this.service.agravos().pipe(catchError(() => of(this.agravos()))).subscribe((a) => this.agravos.set(a));
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private falhou(e: HttpErrorResponse): void {
    this.submitting.set(false);
    const msg = (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir. Tente novamente.';
    if (this.gaveta()) this.erroApi.set(msg);
    else this.mostrarToast('Não foi possível concluir', msg);
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}
