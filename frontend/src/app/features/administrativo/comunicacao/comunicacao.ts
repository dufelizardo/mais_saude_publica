import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import {
  CanalComunicacaoDto,
  ComunicadoDto,
  ConteudoEducativoDto,
  PRIORIDADES,
  PUBLICOS,
  PrioridadeComunicado,
  PublicacaoDto,
  PublicoComunicacao,
  SITUACOES_COMUNICADO,
  SITUACOES_CONTEUDO,
  SITUACOES_PUBLICACAO,
  TIPOS_DE_CANAL,
  TipoCanal,
} from '../../../core/models/comunicacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { ComunicacaoService } from '../../../core/services/comunicacao';
import { EducacaoPermanenteService } from '../../../core/services/educacao-permanente';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { hojeIso, proximaAba } from '../../../shared/formato';

type Aba = 'comunicados' | 'conteudos' | 'canais';

type Gaveta =
  | { tipo: 'comunicado'; item: ComunicadoDto | null }
  | { tipo: 'conteudo'; item: ConteudoEducativoDto | null }
  | { tipo: 'canal'; item: CanalComunicacaoDto | null }
  | { tipo: 'publicarConteudo'; item: ConteudoEducativoDto }
  | { tipo: 'motivo'; alvo: 'encerrar' | 'devolver'; uuid: string; titulo: string }
  | { tipo: 'historico'; titulo: string; publicacoes: PublicacaoDto[] };

/**
 * Comunicação (ADR-0168): comunicados, conteúdos educativos com revisão técnica e canais. O portal é a landing page; o e-mail vai
 * só aos profissionais, porque não há registro de consentimento para a população.
 */
@Component({
  selector: 'app-comunicacao',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './comunicacao.html',
})
export class Comunicacao {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly service = inject(ComunicacaoService);
  private readonly educacaoService = inject(EducacaoPermanenteService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly tiposDeCanal = TIPOS_DE_CANAL;
  protected readonly listaTipos = Object.entries(TIPOS_DE_CANAL) as [TipoCanal, string][];
  protected readonly prioridades = PRIORIDADES;
  protected readonly listaPrioridades = Object.entries(PRIORIDADES) as [PrioridadeComunicado, string][];
  protected readonly publicos = PUBLICOS;
  protected readonly listaPublicos = Object.entries(PUBLICOS) as [PublicoComunicacao, string][];
  protected readonly situacoesComunicado = SITUACOES_COMUNICADO;
  protected readonly situacoesConteudo = SITUACOES_CONTEUDO;
  protected readonly situacoesPublicacao = SITUACOES_PUBLICACAO;
  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'comunicados', rotulo: 'Comunicados' },
    { id: 'conteudos', rotulo: 'Conteúdos educativos' },
    { id: 'canais', rotulo: 'Canais' },
  ];

  protected readonly comunicados = signal<ComunicadoDto[]>([]);
  protected readonly conteudos = signal<ConteudoEducativoDto[]>([]);
  /** Treinamentos que usam cada conteúdo, pelo grupo (ADR-0173). */
  protected readonly usoNosTreinamentos = signal<Record<string, string[]>>({});
  protected readonly canais = signal<CanalComunicacaoDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly aba = signal<Aba>('comunicados');
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly canaisEscolhidos = signal<Set<string>>(new Set());
  protected readonly publicoDaGaveta = signal<PublicoComunicacao>('POPULACAO');
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erroLista = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly comunicadoForm = this.fb.nonNullable.group({
    titulo: [''],
    mensagem: [''],
    prioridade: ['NORMAL' as PrioridadeComunicado],
    publico: ['POPULACAO' as PublicoComunicacao],
    inicio: [''],
    fim: [''],
    unidadeId: [''],
  });
  protected readonly conteudoForm = this.fb.nonNullable.group({
    titulo: [''],
    resumo: [''],
    texto: [''],
    tema: [''],
    publico: ['POPULACAO' as PublicoComunicacao],
    faixaEtaria: [''],
    validade: [''],
  });
  protected readonly canalForm = this.fb.nonNullable.group({ tipo: ['PAINEL' as TipoCanal], nome: [''], descricao: [''], ativo: [true] });
  protected readonly motivoForm = this.fb.nonNullable.group({ motivo: [''] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidades.set(l));
    this.carregar();
  }

  private carregar(): void {
    forkJoin({
      comunicados: this.service.comunicados().pipe(catchError(() => of([]))),
      conteudos: this.service.conteudos().pipe(catchError(() => of([]))),
      canais: this.service.canais().pipe(catchError(() => of([]))),
      uso: this.educacaoService.usoDosConteudos().pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.usoNosTreinamentos.set(Object.fromEntries(r.uso.map((u) => [u.grupoId, u.treinamentos])));
      this.comunicados.set(r.comunicados);
      this.conteudos.set(r.conteudos);
      this.canais.set(r.canais);
      this.carregando.set(false);
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected readonly podePublicar = computed(() => this.pode('COMUNICACAO.PUBLICAR'));
  protected readonly podeEditar = computed(() => this.pode('CONTEUDO.EDITAR'));
  protected readonly podeRevisar = computed(() => this.pode('CONTEUDO.REVISAR'));

  /** Portal para a população; os demais canais ativos para os profissionais. */
  protected canaisDoPublico(publico: PublicoComunicacao): CanalComunicacaoDto[] {
    return this.canais().filter((k) => k.ativo && (publico === 'POPULACAO' ? k.tipo === 'PORTAL' : k.tipo !== 'PORTAL'));
  }

  protected contagem(aba: Aba): number {
    return aba === 'comunicados' ? this.comunicados().length : aba === 'conteudos' ? this.conteudos().length : this.canais().length;
  }

  protected selecionarAba(aba: Aba): void {
    this.aba.set(aba);
    this.erroLista.set(null);
  }

  protected navegarAbas(event: KeyboardEvent): void {
    const proxima = proximaAba(event, this.abas.map((a) => a.id), this.aba());
    if (proxima) this.selecionarAba(proxima);
  }

  protected dataBr(iso: string): string {
    return iso.slice(0, 10).split('-').reverse().join('/');
  }

  protected rotuloNovo(): string {
    const a = this.aba();
    return a === 'comunicados' ? 'Novo comunicado' : a === 'conteudos' ? 'Novo conteúdo' : 'Novo canal';
  }

  protected podeCriar(): boolean {
    return this.aba() === 'conteudos' ? this.podeEditar() : this.podePublicar();
  }

  protected novoNaAba(): void {
    const a = this.aba();
    if (a === 'comunicados') this.abrirComunicado(null);
    else if (a === 'conteudos') this.abrirConteudo(null);
    else this.abrirCanal(null);
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'comunicado':
        return g.item ? 'Editar comunicado' : 'Novo comunicado';
      case 'conteudo':
        return g.item ? 'Editar conteúdo' : 'Novo conteúdo';
      case 'canal':
        return g.item ? 'Editar canal' : 'Novo canal';
      case 'publicarConteudo':
        return 'Publicar conteúdo';
      case 'motivo':
        return g.alvo === 'encerrar' ? 'Encerrar comunicado' : 'Devolver ao autor';
      default:
        return 'Histórico de publicação';
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

  protected abrirComunicado(item: ComunicadoDto | null): void {
    this.comunicadoForm.reset({
      titulo: item?.titulo ?? '',
      mensagem: item?.mensagem ?? '',
      prioridade: item?.prioridade ?? 'NORMAL',
      publico: item?.publico ?? 'POPULACAO',
      inicio: item?.inicio ?? hojeIso(),
      fim: item?.fim ?? '',
      unidadeId: item?.unidadeId ?? '',
    });
    this.publicoDaGaveta.set(item?.publico ?? 'POPULACAO');
    this.canaisEscolhidos.set(new Set(item?.canalIds ?? this.canaisDoPublico('POPULACAO').map((k) => k.uuid)));
    this.abrir({ tipo: 'comunicado', item });
  }

  protected mudarPublico(publico: PublicoComunicacao): void {
    this.publicoDaGaveta.set(publico);
    this.canaisEscolhidos.set(new Set(this.canaisDoPublico(publico).map((k) => k.uuid)));
  }

  protected abrirConteudo(item: ConteudoEducativoDto | null): void {
    this.conteudoForm.reset({
      titulo: item?.titulo ?? '',
      resumo: item?.resumo ?? '',
      texto: item?.texto ?? '',
      tema: item?.tema ?? '',
      publico: item?.publico ?? 'POPULACAO',
      faixaEtaria: item?.faixaEtaria ?? '',
      validade: item?.validade ?? '',
    });
    this.abrir({ tipo: 'conteudo', item });
  }

  protected abrirCanal(item: CanalComunicacaoDto | null): void {
    this.canalForm.reset({ tipo: item?.tipo ?? 'PAINEL', nome: item?.nome ?? '', descricao: item?.descricao ?? '', ativo: item?.ativo ?? true });
    this.abrir({ tipo: 'canal', item });
  }

  protected abrirPublicarConteudo(item: ConteudoEducativoDto): void {
    this.publicoDaGaveta.set(item.publico);
    this.canaisEscolhidos.set(new Set(this.canaisDoPublico(item.publico).map((k) => k.uuid)));
    this.abrir({ tipo: 'publicarConteudo', item });
  }

  protected abrirMotivo(alvo: 'encerrar' | 'devolver', uuid: string, titulo: string): void {
    this.motivoForm.reset({ motivo: '' });
    this.abrir({ tipo: 'motivo', alvo, uuid, titulo });
  }

  protected abrirHistorico(titulo: string, publicacoes: PublicacaoDto[]): void {
    this.abrir({ tipo: 'historico', titulo, publicacoes });
  }

  protected alternarCanal(uuid: string): void {
    this.canaisEscolhidos.update((atual) => {
      const novo = new Set(atual);
      if (!novo.delete(uuid)) novo.add(uuid);
      return novo;
    });
  }

  // ── Salvar ─────────────────────────────────────────────────────────────────────────────────────

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

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    if (g.tipo === 'comunicado') this.salvarComunicado(g.item);
    else if (g.tipo === 'conteudo') this.salvarConteudo(g.item);
    else if (g.tipo === 'canal') this.salvarCanal(g.item);
    else if (g.tipo === 'publicarConteudo') this.confirmarPublicacaoDoConteudo(g.item);
    else if (g.tipo === 'motivo') this.salvarMotivo(g);
  }

  private salvarComunicado(item: ComunicadoDto | null): void {
    const v = this.comunicadoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.titulo.trim()) erros['titulo'] = 'Informe o título.';
    if (!v.mensagem.trim()) erros['mensagem'] = 'Informe a mensagem.';
    if (!v.inicio) erros['inicio'] = 'Informe o início.';
    if (!v.fim) erros['fim'] = 'Informe o fim.';
    else if (v.inicio && v.fim < v.inicio) erros['fim'] = 'O fim vem depois do início.';
    if (!this.validar(erros)) return;
    const dto = { ...v, titulo: v.titulo.trim(), mensagem: v.mensagem.trim(), unidadeId: v.unidadeId || null, canalIds: [...this.canaisEscolhidos()] };
    this.enviar(item ? this.service.atualizarComunicado(item.uuid, dto) : this.service.criarComunicado(dto), item ? 'Comunicado atualizado' : 'Rascunho salvo', dto.titulo);
  }

  private salvarConteudo(item: ConteudoEducativoDto | null): void {
    const v = this.conteudoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.titulo.trim()) erros['tituloConteudo'] = 'Informe o título.';
    if (!v.resumo.trim()) erros['resumo'] = 'Informe o resumo.';
    if (!v.texto.trim()) erros['texto'] = 'Informe o texto.';
    if (!v.tema.trim()) erros['tema'] = 'Informe o tema.';
    if (!v.validade) erros['validade'] = 'Informe a validade.';
    if (!this.validar(erros)) return;
    const dto = { ...v, titulo: v.titulo.trim(), faixaEtaria: v.faixaEtaria.trim() || null };
    this.enviar(item ? this.service.atualizarConteudo(item.uuid, dto) : this.service.criarConteudo(dto), item ? 'Conteúdo atualizado' : 'Rascunho salvo', dto.titulo);
  }

  private salvarCanal(item: CanalComunicacaoDto | null): void {
    const v = this.canalForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nomeCanal'] = 'Informe o nome.';
    if (!this.validar(erros)) return;
    const dto = { ...v, nome: v.nome.trim(), descricao: v.descricao.trim() || null };
    this.enviar(item ? this.service.atualizarCanal(item.uuid, dto) : this.service.criarCanal(dto), item ? 'Canal atualizado' : 'Canal cadastrado', dto.nome);
  }

  private confirmarPublicacaoDoConteudo(item: ConteudoEducativoDto): void {
    if (!this.canaisEscolhidos().size) {
      this.erroApi.set('Escolha ao menos um canal.');
      return;
    }
    this.enviar(this.service.publicarConteudo(item.uuid, [...this.canaisEscolhidos()]), 'Conteúdo publicado', item.titulo);
  }

  private salvarMotivo(g: Extract<Gaveta, { tipo: 'motivo' }>): void {
    const motivo = this.motivoForm.getRawValue().motivo.trim();
    if (!this.validar(motivo ? {} : { motivo: 'Informe o motivo.' })) return;
    if (g.alvo === 'encerrar') this.enviar(this.service.encerrarComunicado(g.uuid, motivo), 'Comunicado encerrado', g.titulo);
    else this.enviar(this.service.devolverConteudo(g.uuid, motivo), 'Conteúdo devolvido', g.titulo);
  }

  private enviar(req: Observable<unknown>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.mostrarToast(titulo, detalhe);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }

  /** As ações da linha, sem gaveta: o erro aparece acima da tabela. */
  private acao(req: Observable<{ publicacoes: PublicacaoDto[] }>, titulo: string, detalhe: string): void {
    this.erroLista.set(null);
    req.subscribe({
      next: (r) => {
        const pendente = r.publicacoes.find((p) => p.situacao === 'NAO_ENVIADA');
        this.mostrarToast(titulo, pendente ? `${pendente.canalNome}: ${pendente.detalhe ?? 'não enviada'}` : detalhe);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.erroLista.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir a ação.'),
    });
  }

  protected publicarComunicado(c: ComunicadoDto): void {
    this.acao(this.service.publicarComunicado(c.uuid), 'Comunicado publicado', c.titulo);
  }

  protected acaoDoConteudo(c: ConteudoEducativoDto, acao: 'envio-revisao' | 'aprovacao' | 'nova-versao', titulo: string): void {
    this.acao(this.service.acaoDoConteudo(c.uuid, acao), titulo, c.titulo);
  }
}
