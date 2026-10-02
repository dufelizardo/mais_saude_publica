import { NgTemplateOutlet } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  AtribuicaoAcessoResponseDto,
  EscopoAcessoResponseDto,
  PapelResponseDto,
  PermissaoResponseDto,
  UsuarioResponseDto,
} from '../../../core/models/acesso';
import { AcessoDaInterface } from '../../../core/models/auth';
import { ErrorResponseDto, ProfissionalResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { AcessoService } from '../../../core/services/acesso';
import { AuthService } from '../../../core/services/auth';
import { ProfissionalService } from '../../../core/services/profissional';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';

type Aba = 'usuarios' | 'perfis';

type Gaveta =
  | { tipo: 'usuario'; usuarioId: string }
  | { tipo: 'novo-usuario' }
  | { tipo: 'editar-usuario'; usuario: UsuarioResponseDto }
  | { tipo: 'senha'; usuario: UsuarioResponseDto }
  | { tipo: 'conceder'; usuario: UsuarioResponseDto }
  | { tipo: 'revogar'; usuario: UsuarioResponseDto; atribuicao: AtribuicaoAcessoResponseDto }
  | { tipo: 'papel'; papel: PapelResponseDto | null; base: PapelResponseDto | null };

type FiltroStatus = '' | 'ativos' | 'bloqueados' | 'inativos' | 'sem-acesso';

/** Classe visual (cor do ícone, da etiqueta e do avatar) por família de papel — mesmas do protótipo Usuarios.html. */
type ClassePapel = 'coord' | 'med' | 'enf' | 'acs' | 'adm' | 'audit';

const CLASSE_POR_PAPEL: Record<string, ClassePapel> = {
  ADMINISTRADOR_PLATAFORMA: 'adm',
  GESTOR: 'coord',
  COORDENADOR_DE_ENFERMAGEM: 'coord',
  MEDICO: 'med',
  MEDICO_REGULADOR: 'med',
  TECNICO_DE_LABORATORIO: 'audit',
  RESPONSAVEL_TECNICO_LABORATORIO: 'coord',
  ENFERMEIRO: 'enf',
  TECNICO_DE_ENFERMAGEM: 'enf',
  RECEPCAO: 'acs',
  FARMACEUTICO: 'audit',
};

const GRADIENTE: Record<ClassePapel, string> = {
  coord: 'linear-gradient(135deg,#1351b4,#2f8de4)',
  med: 'linear-gradient(135deg,#0b8a8a,#38b2b2)',
  enf: 'linear-gradient(135deg,#6936c2,#9c6ce8)',
  acs: 'linear-gradient(135deg,#1f8a5b,#4cb083)',
  adm: 'linear-gradient(135deg,var(--ink-700),var(--ink-500))',
  audit: 'linear-gradient(135deg,#b86b00,#d18820)',
};

/**
 * Módulos da matriz de permissões. O catálogo é RECURSO.ACAO (ADR-0066); a matriz agrupa por módulo nas
 * colunas Visualizar / Registrar / Gerenciar. Uma permissão nova que não esteja aqui aparece em
 * "Outras permissões", para nada sumir da tela.
 */
interface Modulo {
  nome: string;
  sub: string;
  ver: string[];
  registrar: string[];
  gerenciar: string[];
  /** Leitura aberta a todo usuário logado (ADR-0067). */
  verLiberado?: boolean;
}

const MODULOS: Modulo[] = [
  { nome: 'Pacientes', sub: 'cadastro e identificação', ver: ['PACIENTE.CONSULTAR'], registrar: ['PACIENTE.CADASTRAR'], gerenciar: [] },
  { nome: 'Agenda & atendimentos', sub: 'agendamento, acolhimento, atendimento', ver: [], registrar: ['AGENDAMENTO.GERENCIAR', 'ATENDIMENTO.GERENCIAR'], gerenciar: [] },
  {
    nome: 'Prontuário & registros clínicos',
    sub: 'triagem, evolução, consulta, procedimento',
    ver: ['PRONTUARIO.CONSULTAR'],
    registrar: ['TRIAGEM.REGISTRAR', 'EVOLUCAO.REGISTRAR', 'CONSULTA.REGISTRAR', 'PROCEDIMENTO.REGISTRAR'],
    gerenciar: ['REGISTRO_CLINICO.RETIFICAR_DE_OUTROS'],
  },
  { nome: 'Medicação', sub: 'checagem da prescrição', ver: [], registrar: ['MEDICACAO.ADMINISTRAR'], gerenciar: [] },
  { nome: 'Regulação', sub: 'encaminhamento, fila e vaga (ADR-0087)', ver: ['REGULACAO.CONSULTAR'], registrar: ['REGULACAO.SOLICITAR'], gerenciar: ['REGULACAO.REGULAR'] },
  {
    nome: 'Laboratório',
    sub: 'pedido, coleta, resultado e liberação (ADR-0093)',
    ver: [],
    registrar: ['EXAME.SOLICITAR', 'LABORATORIO.COLETAR', 'LABORATORIO.ANALISAR'],
    gerenciar: ['LABORATORIO.LIBERAR', 'LABORATORIO.GERENCIAR'],
  },
  {
    nome: 'Farmácia',
    sub: 'estoque, dispensação, transferências',
    ver: ['FARMACIA.CONSULTAR'],
    registrar: ['FARMACIA.DISPENSAR', 'FARMACIA.TRANSFERIR'],
    gerenciar: ['FARMACIA.GERENCIAR_ESTOQUE'],
  },
  { nome: 'Recursos humanos', sub: 'profissionais, folha, vínculos', ver: ['RH.CONSULTAR'], registrar: [], gerenciar: ['RH.GERENCIAR'] },
  { nome: 'Setor administrativo', sub: 'capacidades, processos, responsabilidades', ver: ['ADMINISTRATIVO.CONSULTAR'], registrar: [], gerenciar: ['ADMINISTRATIVO.GERENCIAR'] },
  { nome: 'Estrutura da rede', sub: 'unidades de saúde e setores', ver: [], registrar: [], gerenciar: ['ORGANIZACAO.GERENCIAR'], verLiberado: true },
  { nome: 'Usuários & acessos', sub: 'identidades, papéis, concessões', ver: [], registrar: [], gerenciar: ['USUARIO.GERENCIAR', 'ACESSO.GERENCIAR'] },
  { nome: 'Auditoria', sub: 'quem acessou e alterou o quê', ver: ['AUDITORIA.CONSULTAR'], registrar: [], gerenciar: [] },
];

type Celula = { estado: 'allow' | 'partial' | 'deny' | 'na'; titulo: string };

interface LinhaUsuario {
  usuario: UsuarioResponseDto;
  profissional: ProfissionalResponseDto | undefined;
  vigentes: AtribuicaoAcessoResponseDto[];
  classe: ClassePapel;
  status: { classe: string; rotulo: string; dot: '' | 'off' | 'warn' };
}

const POR_PAGINA = 8;

/**
 * Usuários & Perfis (ADR-0068) — portada do protótipo Usuarios.html: usuários com o papel e a unidade de
 * cada um, perfis (papéis) com a matriz de permissões por módulo, concessão e revogação de acesso por
 * escopo. O que não tem backend ainda (MFA, sessões, política de senha, exportação) fica "Em breve".
 */
@Component({
  selector: 'app-usuarios',
  imports: [ReactiveFormsModule, NgTemplateOutlet, Drawer],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios {
  private readonly fb = inject(FormBuilder);
  private readonly acessoService = inject(AcessoService);
  private readonly authService = inject(AuthService);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly router = inject(Router);

  protected readonly formatCpf = formatCpf;
  protected readonly modulos = MODULOS;

  protected readonly usuarios = signal<UsuarioResponseDto[]>([]);
  protected readonly papeis = signal<PapelResponseDto[]>([]);
  protected readonly permissoes = signal<PermissaoResponseDto[]>([]);
  protected readonly atribuicoes = signal<AtribuicaoAcessoResponseDto[]>([]);
  protected readonly escopos = signal<EscopoAcessoResponseDto[]>([]);
  protected readonly profissionais = signal<ProfissionalResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  /** CPF de quem está logado: as próprias sessões se encerram trocando a senha, não por aqui. */
  protected readonly meuCpf = signal<string | null>(null);

  protected readonly aba = signal<Aba>('usuarios');
  protected readonly busca = signal('');
  protected readonly filtroPapel = signal<string | null>(null);
  protected readonly filtroUnidade = signal('');
  protected readonly filtroStatus = signal<FiltroStatus>('');
  protected readonly pagina = signal(1);
  protected readonly papelSelecionadoId = signal<string | null>(null);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;
  /** Gaveta a abrir quando o recarregamento depois de salvar terminar. */
  private depoisDeCarregar: (() => Gaveta | null) | null = null;

  /** Permissões marcadas na gaveta de perfil. */
  protected readonly marcadas = signal<ReadonlySet<string>>(new Set());

  protected readonly novoUsuarioForm = this.fb.nonNullable.group({
    matricula: [''],
    cpf: [''],
    nome: [''],
    senha: [''],
    confirmacao: [''],
  });

  protected readonly editarUsuarioForm = this.fb.nonNullable.group({
    nome: [''],
    ativo: [true],
  });

  protected readonly concederForm = this.fb.nonNullable.group({
    papelId: [''],
    unidadeId: [''],
    inicio: [''],
    fim: [''],
  });

  protected readonly revogarForm = this.fb.nonNullable.group({ motivo: [''] });

  protected readonly senhaForm = this.fb.nonNullable.group({ senha: [''], confirmacao: [''] });

  protected readonly papelForm = this.fb.nonNullable.group({
    codigo: [''],
    nome: [''],
    descricao: [''],
    ativo: [true],
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.authService.usuarioAtual().subscribe((u) => this.meuCpf.set(u?.cpf ?? null));
    this.carregarTudo();
    this.profissionalService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.profissionais.set(p));
  }

  /** Listagens vazias respondem 404 no backend — tratadas como lista vazia. */
  private carregarTudo(): void {
    this.carregando.set(true);
    forkJoin({
      usuarios: this.acessoService.listarUsuarios().pipe(catchError(() => of([]))),
      papeis: this.acessoService.listarPapeis().pipe(catchError(() => of([]))),
      permissoes: this.acessoService.listarPermissoes().pipe(catchError(() => of([]))),
      atribuicoes: this.acessoService.listarAtribuicoes().pipe(catchError(() => of([]))),
      escopos: this.acessoService.listarEscopos().pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.usuarios.set(r.usuarios);
      this.papeis.set(r.papeis);
      this.permissoes.set(r.permissoes);
      this.atribuicoes.set(r.atribuicoes);
      this.escopos.set(r.escopos);
      this.carregando.set(false);
      if (this.depoisDeCarregar) {
        this.gaveta.set(this.depoisDeCarregar());
        this.depoisDeCarregar = null;
      }
    });
  }

  /** Com a autorização desligada, tudo aparece; ligada, só o que as permissões liberam (ADR-0068). */
  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  // ── Dados derivados ────────────────────────────────────────────────────────────────────────────

  private readonly profissionalPorCpf = computed(() => {
    const mapa = new Map<string, ProfissionalResponseDto>();
    for (const p of this.profissionais()) {
      const cpf = (p.cpf ?? '').replace(/\D/g, '');
      if (cpf && (p.ativo || !mapa.has(cpf))) mapa.set(cpf, p);
    }
    return mapa;
  });

  private readonly vigentesPorUsuario = computed(() => {
    const mapa = new Map<string, AtribuicaoAcessoResponseDto[]>();
    for (const a of this.atribuicoes()) {
      if (!a.vigente) continue;
      const lista = mapa.get(a.usuarioUuid) ?? [];
      lista.push(a);
      mapa.set(a.usuarioUuid, lista);
    }
    return mapa;
  });

  protected readonly linhas = computed<LinhaUsuario[]>(() =>
    this.usuarios()
      .map((u) => {
        const vigentes = this.vigentesPorUsuario().get(u.uuid) ?? [];
        return {
          usuario: u,
          profissional: this.profissionalPorCpf().get(u.cpf),
          vigentes,
          classe: vigentes.length ? this.classePapel(vigentes[0].papelCodigo) : 'adm',
          status: statusDoUsuario(u, vigentes.length),
        };
      })
      .sort((a, b) => (b.usuario.ultimoAcessoEm ?? '').localeCompare(a.usuario.ultimoAcessoEm ?? '')),
  );

  protected readonly filtradas = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const digitos = q.replace(/\D/g, '');
    const papel = this.filtroPapel();
    const unidade = this.filtroUnidade();
    const status = this.filtroStatus();
    return this.linhas()
      .filter((l) => !q
        || l.usuario.nome.toLowerCase().includes(q)
        || (digitos.length >= 3 && l.usuario.cpf.includes(digitos))
        || (l.profissional?.matricula ?? '').toLowerCase().includes(q)
        || (l.profissional?.nome ?? '').toLowerCase().includes(q))
      .filter((l) => !papel || l.vigentes.some((a) => a.papelUuid === papel))
      .filter((l) => !unidade || l.vigentes.some((a) => (unidade === 'REDE' ? !a.unidadeUuid : a.unidadeUuid === unidade)))
      .filter((l) => {
        switch (status) {
          case 'ativos': return l.usuario.ativo && !l.usuario.bloqueado;
          case 'bloqueados': return l.usuario.bloqueado;
          case 'inativos': return !l.usuario.ativo;
          case 'sem-acesso': return l.vigentes.length === 0;
          default: return true;
        }
      });
  });

  protected readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.filtradas().length / POR_PAGINA)));

  protected readonly paginadas = computed(() => {
    const pagina = Math.min(this.pagina(), this.totalPaginas());
    return this.filtradas().slice((pagina - 1) * POR_PAGINA, pagina * POR_PAGINA);
  });

  protected readonly faixa = computed(() => {
    const total = this.filtradas().length;
    if (!total) return { de: 0, ate: 0, total };
    const pagina = Math.min(this.pagina(), this.totalPaginas());
    return { de: (pagina - 1) * POR_PAGINA + 1, ate: Math.min(pagina * POR_PAGINA, total), total };
  });

  /** Números da paginação: primeira, vizinhas da atual e última, com reticências entre os saltos. */
  protected readonly paginas = computed<(number | null)[]>(() => {
    const total = this.totalPaginas();
    const atual = Math.min(this.pagina(), total);
    const lista: (number | null)[] = [];
    for (let p = 1; p <= total; p++) {
      if (p === 1 || p === total || Math.abs(p - atual) <= 1 || (atual <= 3 && p <= 4)) {
        lista.push(p);
      } else if (lista[lista.length - 1] !== null) {
        lista.push(null);
      }
    }
    return lista;
  });

  /** Unidades que aparecem em algum acesso vigente — opções do filtro por unidade. */
  protected readonly unidadesComAcesso = computed(() => {
    const mapa = new Map<string, string>();
    for (const a of this.atribuicoes()) {
      if (a.vigente && a.unidadeUuid) mapa.set(a.unidadeUuid, a.unidadeNome ?? a.unidadeUuid);
    }
    return [...mapa.entries()].map(([uuid, nome]) => ({ uuid, nome })).sort((a, b) => a.nome.localeCompare(b.nome));
  });

  protected readonly usuariosPorPapel = computed(() => {
    const mapa = new Map<string, Set<string>>();
    for (const a of this.atribuicoes()) {
      if (!a.vigente) continue;
      const set = mapa.get(a.papelUuid) ?? new Set<string>();
      set.add(a.usuarioUuid);
      mapa.set(a.papelUuid, set);
    }
    return mapa;
  });

  protected readonly papeisOrdenados = computed(() =>
    [...this.papeis()].sort((a, b) =>
      Number(b.ativo) - Number(a.ativo) || this.quantosUsuarios(b) - this.quantosUsuarios(a) || a.nome.localeCompare(b.nome)),
  );

  protected readonly papelSelecionado = computed(() => this.papeis().find((p) => p.uuid === this.papelSelecionadoId()) ?? null);

  protected readonly resumo = computed(() => {
    const usuarios = this.usuarios();
    const ativos = usuarios.filter((u) => u.ativo).length;
    const bloqueados = usuarios.filter((u) => u.bloqueado).length;
    const vigentes = this.atribuicoes().filter((a) => a.vigente);
    const comAcesso = new Set(vigentes.map((a) => a.usuarioUuid)).size;
    return {
      ativos,
      inativos: usuarios.length - ativos,
      comAcesso,
      semAcesso: usuarios.length - comAcesso,
      vigentes: vigentes.length,
      temporarios: vigentes.filter((a) => a.fim).length,
      naRede: vigentes.filter((a) => !a.unidadeUuid).length,
      bloqueados,
    };
  });

  private readonly codigosMapeados = new Set(MODULOS.flatMap((m) => [...m.ver, ...m.registrar, ...m.gerenciar]));

  /** Permissões do catálogo que a matriz ainda não agrupa. */
  protected readonly outrasPermissoes = computed(() => this.permissoes().filter((p) => !this.codigosMapeados.has(p.codigo)));

  private readonly permissaoPorCodigo = computed(() => new Map(this.permissoes().map((p) => [p.codigo, p])));

  // ── Apoio de exibição ──────────────────────────────────────────────────────────────────────────

  protected classePapel(codigo: string): ClassePapel {
    return CLASSE_POR_PAPEL[codigo] ?? 'adm';
  }

  protected gradiente(classe: ClassePapel): string {
    return GRADIENTE[classe];
  }

  protected iniciais(nome: string): string {
    const partes = nome.trim().split(/\s+/).filter((p) => p.length > 2 || /^[A-ZÀ-Ú]/.test(p));
    const primeira = partes[0]?.[0] ?? '?';
    const ultima = partes.length > 1 ? partes[partes.length - 1][0] : '';
    return (primeira + ultima).toUpperCase();
  }

  protected quantosUsuarios(papel: PapelResponseDto): number {
    return this.usuariosPorPapel().get(papel.uuid)?.size ?? 0;
  }

  protected escopoDe(a: AtribuicaoAcessoResponseDto): string {
    return a.unidadeNome ?? 'Rede inteira';
  }

  protected periodoDe(a: AtribuicaoAcessoResponseDto): string {
    if (a.inicio && a.fim) return `${formatarData(a.inicio)} a ${formatarData(a.fim)}`;
    if (a.fim) return `até ${formatarData(a.fim)}`;
    if (a.inicio) return `desde ${formatarData(a.inicio)}`;
    return 'sem prazo';
  }

  protected formatarInstante(iso: string | null | undefined): string {
    if (!iso) return '—';
    const d = new Date(iso);
    const hora = `${pad(d.getHours())}:${pad(d.getMinutes())}`;
    const hoje = new Date();
    if (d.toDateString() === hoje.toDateString()) return `hoje · ${hora}`;
    const mes = d.toLocaleString('pt-BR', { month: 'short' }).replace('.', '');
    const ano = d.getFullYear() !== hoje.getFullYear() ? `/${d.getFullYear()}` : '';
    return `${pad(d.getDate())}/${mes}${ano} · ${hora}`;
  }

  protected formatarData = formatarData;

  protected temDadoDeSaude(papel: PapelResponseDto): boolean {
    return papel.permissoes.some((c) => this.permissaoPorCodigo().get(c)?.dimensao === 'ACESSO_AO_DADO_DE_SAUDE');
  }

  protected celula(papel: PapelResponseDto, codigos: string[], liberado = false): Celula {
    if (liberado) return { estado: 'allow', titulo: 'Liberado para todo usuário logado' };
    if (!codigos.length) return { estado: 'na', titulo: 'Não se aplica' };
    const tem = codigos.filter((c) => papel.permissoes.includes(c));
    const titulo = codigos.map((c) => `${papel.permissoes.includes(c) ? '✓' : '×'} ${c}`).join('\n');
    if (tem.length === codigos.length) return { estado: 'allow', titulo };
    return { estado: tem.length ? 'partial' : 'deny', titulo };
  }

  protected simbolo(estado: Celula['estado']): string {
    return { allow: '✓', partial: '~', deny: '×', na: '—' }[estado];
  }

  protected descricaoPermissao(codigo: string): string {
    return this.permissaoPorCodigo().get(codigo)?.descricao ?? codigo;
  }

  protected ehDadoDeSaude(codigo: string): boolean {
    return this.permissaoPorCodigo().get(codigo)?.dimensao === 'ACESSO_AO_DADO_DE_SAUDE';
  }

  /** Permissões de um módulo na ordem das colunas, para os grupos da gaveta de perfil. */
  protected codigosDoModulo(m: Modulo): string[] {
    return [...m.ver, ...m.registrar, ...m.gerenciar];
  }

  // ── Filtros, abas, seleção ─────────────────────────────────────────────────────────────────────

  protected selecionarAba(aba: Aba): void {
    this.aba.set(aba);
    if (aba === 'perfis' && !this.papelSelecionadoId()) {
      this.papelSelecionadoId.set(this.papeisOrdenados()[0]?.uuid ?? null);
    }
  }

  protected navegarAbas(event: KeyboardEvent): void {
    if (event.key !== 'ArrowRight' && event.key !== 'ArrowLeft') return;
    event.preventDefault();
    const proxima: Aba = this.aba() === 'usuarios' ? 'perfis' : 'usuarios';
    this.selecionarAba(proxima);
    document.getElementById('tab-' + proxima)?.focus();
  }

  /** Na aba Usuários, escolher um perfil filtra a lista por ele e mostra a matriz (como no protótipo). */
  protected selecionarPapel(papel: PapelResponseDto): void {
    this.papelSelecionadoId.set(papel.uuid);
    if (this.aba() === 'usuarios') {
      this.filtroPapel.set(papel.uuid);
      this.pagina.set(1);
    }
  }

  protected limparFiltroPapel(): void {
    this.filtroPapel.set(null);
    this.papelSelecionadoId.set(null);
    this.pagina.set(1);
  }

  protected nomeDoFiltroPapel(): string {
    return this.papeis().find((p) => p.uuid === this.filtroPapel())?.nome ?? '';
  }

  protected atualizarBusca(valor: string): void {
    this.busca.set(valor);
    this.pagina.set(1);
  }

  protected atualizarFiltroUnidade(valor: string): void {
    this.filtroUnidade.set(valor);
    this.pagina.set(1);
  }

  protected atualizarFiltroStatus(valor: string): void {
    this.filtroStatus.set(valor as FiltroStatus);
    this.pagina.set(1);
  }

  protected irParaPagina(p: number): void {
    this.pagina.set(Math.max(1, Math.min(p, this.totalPaginas())));
  }

  protected teclaNoPerfil(event: KeyboardEvent, papel: PapelResponseDto): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.selecionarPapel(papel);
    }
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  protected usuarioDaGaveta(id: string): LinhaUsuario | null {
    return this.linhas().find((l) => l.usuario.uuid === id) ?? null;
  }

  protected historicoDe(id: string): AtribuicaoAcessoResponseDto[] {
    return this.atribuicoes()
      .filter((a) => a.usuarioUuid === id && !a.vigente)
      .sort((a, b) => (b.revogadoEm ?? b.concedidoEm).localeCompare(a.revogadoEm ?? a.concedidoEm));
  }

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'usuario': return this.usuarioDaGaveta(g.usuarioId)?.usuario.nome ?? 'Usuário';
      case 'novo-usuario': return 'Novo usuário';
      case 'editar-usuario': return 'Editar usuário';
      case 'senha': return 'Definir senha provisória';
      case 'conceder': return 'Conceder acesso';
      case 'revogar': return 'Revogar acesso';
      case 'papel': return g.papel ? 'Editar perfil' : g.base ? 'Duplicar perfil' : 'Novo perfil';
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

  protected abrirUsuario(u: UsuarioResponseDto): void {
    this.abrir({ tipo: 'usuario', usuarioId: u.uuid });
  }

  protected teclaNaLinha(event: KeyboardEvent, u: UsuarioResponseDto): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.abrirUsuario(u);
    }
  }

  protected voltarAoUsuario(u: UsuarioResponseDto): void {
    this.abrir({ tipo: 'usuario', usuarioId: u.uuid });
  }

  protected abrirNovoUsuario(): void {
    this.novoUsuarioForm.reset({ matricula: '', cpf: '', nome: '', senha: '', confirmacao: '' });
    this.abrir({ tipo: 'novo-usuario' });
  }

  /** Preenche CPF e nome a partir do profissional — a identidade de login costuma ser de alguém do quadro. */
  protected buscarProfissional(): void {
    const matricula = this.novoUsuarioForm.getRawValue().matricula.trim().toLowerCase();
    const p = this.profissionais().find((x) => x.matricula.toLowerCase() === matricula);
    if (!p) {
      this.erros.set({ matricula: 'Nenhum profissional com esta matrícula.' });
      return;
    }
    this.erros.set({});
    this.novoUsuarioForm.patchValue({ cpf: formatCpf(p.cpf), nome: p.nome });
  }

  protected mascararCpf(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.novoUsuarioForm.controls.cpf.setValue(formatCpf(input.value));
  }

  protected abrirEditarUsuario(u: UsuarioResponseDto): void {
    this.editarUsuarioForm.reset({ nome: u.nome, ativo: u.ativo });
    this.abrir({ tipo: 'editar-usuario', usuario: u });
  }

  protected abrirSenha(u: UsuarioResponseDto): void {
    this.senhaForm.reset({ senha: '', confirmacao: '' });
    this.abrir({ tipo: 'senha', usuario: u });
  }

  /** Situação da senha na gaveta do usuário (ADR-0069). */
  protected situacaoSenha(u: UsuarioResponseDto): string {
    if (u.trocarSenha) return 'Provisória — troca no próximo acesso';
    if (u.senhaAlteradaEm) return `Trocada em ${this.formatarInstante(u.senhaAlteradaEm)}`;
    return 'Definida no cadastro';
  }

  protected abrirConceder(u: UsuarioResponseDto): void {
    this.concederForm.reset({ papelId: '', unidadeId: '', inicio: '', fim: '' });
    this.abrir({ tipo: 'conceder', usuario: u });
  }

  protected abrirRevogar(u: UsuarioResponseDto, a: AtribuicaoAcessoResponseDto): void {
    this.revogarForm.reset({ motivo: '' });
    this.abrir({ tipo: 'revogar', usuario: u, atribuicao: a });
  }

  protected abrirPapel(papel: PapelResponseDto | null, base: PapelResponseDto | null = null): void {
    const origem = papel ?? base;
    this.papelForm.reset({
      codigo: base ? `${base.codigo}_COPIA` : '',
      nome: papel?.nome ?? (base ? `${base.nome} (cópia)` : ''),
      descricao: origem?.descricao ?? '',
      ativo: papel?.ativo ?? true,
    });
    this.marcadas.set(new Set(origem?.permissoes ?? []));
    this.abrir({ tipo: 'papel', papel, base });
  }

  protected alternarPermissao(codigo: string): void {
    this.marcadas.update((atual) => {
      const novo = new Set(atual);
      if (novo.has(codigo)) novo.delete(codigo);
      else novo.add(codigo);
      return novo;
    });
  }

  protected papeisAtivos = computed(() => this.papeis().filter((p) => p.ativo).sort((a, b) => a.nome.localeCompare(b.nome)));

  protected nivelDoEscopo(e: EscopoAcessoResponseDto): string {
    const t = e.tipo ?? '';
    return t ? t.charAt(0) + t.slice(1).toLowerCase().replace(/_/g, ' ') : '';
  }

  // ── Ações diretas ──────────────────────────────────────────────────────────────────────────────

  /** A trilha de auditoria desta pessoa (ADR-0071). */
  protected verNaAuditoria(u: UsuarioResponseDto): void {
    this.gaveta.set(null);
    this.router.navigate(['/administracao/auditoria'], { queryParams: { usuarioCpf: u.cpf } });
  }

  /** Derruba as sessões abertas da pessoa (ADR-0078) — computador esquecido logado, suspeita de uso indevido. */
  protected encerrarSessoes(u: UsuarioResponseDto): void {
    this.enviar(this.acessoService.encerrarSessoes(u.uuid), 'Sessões encerradas', `${u.nome} precisa entrar de novo`,
      () => ({ tipo: 'usuario', usuarioId: u.uuid }));
  }

  protected desbloquear(u: UsuarioResponseDto): void {
    this.enviar(this.acessoService.desbloquearUsuario(u.uuid), 'Usuário desbloqueado', u.nome,
      () => ({ tipo: 'usuario', usuarioId: u.uuid }));
  }

  protected alternarPapelAtivo(papel: PapelResponseDto): void {
    this.enviar(
      this.acessoService.atualizarPapel(papel.uuid, {
        nome: papel.nome,
        descricao: papel.descricao ?? null,
        ativo: !papel.ativo,
        permissoes: papel.permissoes,
      }),
      papel.ativo ? 'Perfil desativado' : 'Perfil reativado',
      papel.nome,
    );
  }

  // ── Salvar ─────────────────────────────────────────────────────────────────────────────────────

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  private validar(erros: Record<string, string>): boolean {
    this.erros.set(erros);
    this.erroApi.set(null);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) {
      setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
      return false;
    }
    return true;
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g) return;
    switch (g.tipo) {
      case 'novo-usuario': return this.salvarNovoUsuario();
      case 'editar-usuario': return this.salvarEdicaoUsuario(g.usuario);
      case 'senha': return this.salvarSenha(g.usuario);
      case 'conceder': return this.salvarConcessao(g.usuario);
      case 'revogar': return this.salvarRevogacao(g.usuario, g.atribuicao);
      case 'papel': return this.salvarPapel(g.papel);
      default: return;
    }
  }

  private salvarNovoUsuario(): void {
    const v = this.novoUsuarioForm.getRawValue();
    const cpf = v.cpf.replace(/\D/g, '');
    const erros: Record<string, string> = {};
    if (!cpfValido(cpf)) erros['cpf'] = 'Informe um CPF válido.';
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome.';
    if (v.senha.length < 8) erros['senha'] = 'A senha inicial precisa ter ao menos 8 caracteres.';
    else if (v.senha !== v.confirmacao) erros['confirmacao'] = 'A confirmação não confere com a senha.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.acessoService.criarUsuario({ cpf, nome: v.nome.trim(), senha: v.senha }),
      'Usuário cadastrado',
      `${v.nome.trim()} · agora conceda o acesso`,
      () => {
        const u = this.usuarios().find((x) => x.cpf === cpf);
        return u ? { tipo: 'usuario', usuarioId: u.uuid } : null;
      },
    );
  }

  private salvarEdicaoUsuario(u: UsuarioResponseDto): void {
    const v = this.editarUsuarioForm.getRawValue();
    if (!this.validar(v.nome.trim() ? {} : { nome: 'Informe o nome.' })) return;
    this.enviar(
      this.acessoService.atualizarUsuario(u.uuid, { nome: v.nome.trim(), ativo: v.ativo }),
      'Usuário atualizado',
      `${v.nome.trim()} · ${v.ativo ? 'ativo' : 'desativado'}`,
      () => ({ tipo: 'usuario', usuarioId: u.uuid }),
    );
  }

  private salvarSenha(u: UsuarioResponseDto): void {
    const v = this.senhaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (v.senha.length < 8 || v.senha.length > 72) erros['senha'] = 'A senha provisória precisa ter de 8 a 72 caracteres.';
    else if (v.senha !== v.confirmacao) erros['confirmacao'] = 'A confirmação não confere com a senha.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.acessoService.redefinirSenha(u.uuid, v.senha),
      'Senha provisória definida',
      `${u.nome} · troca no próximo acesso`,
      () => ({ tipo: 'usuario', usuarioId: u.uuid }),
    );
  }

  private salvarConcessao(u: UsuarioResponseDto): void {
    const v = this.concederForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.papelId) erros['papelId'] = 'Escolha o perfil.';
    if (v.inicio && v.fim && v.fim < v.inicio) erros['fim'] = 'O fim não pode ser antes do início.';
    if (!this.validar(erros)) return;
    const papel = this.papeis().find((p) => p.uuid === v.papelId);
    const escopo = this.escopos().find((e) => e.uuid === v.unidadeId);
    this.enviar(
      this.acessoService.conceder({
        usuarioId: u.uuid,
        papelId: v.papelId,
        unidadeId: v.unidadeId || null,
        inicio: v.inicio || null,
        fim: v.fim || null,
      }),
      'Acesso concedido',
      `${papel?.nome ?? ''} · ${escopo?.nome ?? 'rede inteira'}`,
      () => ({ tipo: 'usuario', usuarioId: u.uuid }),
    );
  }

  private salvarRevogacao(u: UsuarioResponseDto, a: AtribuicaoAcessoResponseDto): void {
    const motivo = this.revogarForm.getRawValue().motivo.trim();
    if (!this.validar(motivo ? {} : { motivo: 'Informe o motivo da revogação.' })) return;
    this.enviar(
      this.acessoService.revogar(a.uuid, motivo),
      'Acesso revogado',
      `${a.papelNome} · ${this.escopoDe(a)}`,
      () => ({ tipo: 'usuario', usuarioId: u.uuid }),
    );
  }

  private salvarPapel(papel: PapelResponseDto | null): void {
    const v = this.papelForm.getRawValue();
    const codigo = v.codigo.trim().toUpperCase();
    const erros: Record<string, string> = {};
    if (!papel && !/^[A-Z][A-Z0-9_]*$/.test(codigo)) erros['codigo'] = 'Use letras, números e _ (ex.: AGENTE_COMUNITARIO).';
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome do perfil.';
    if (!this.validar(erros)) return;
    const permissoes = [...this.marcadas()].sort();
    const descricao = v.descricao.trim() || null;
    const req = papel
      ? this.acessoService.atualizarPapel(papel.uuid, { nome: v.nome.trim(), descricao, ativo: v.ativo, permissoes })
      : this.acessoService.criarPapel({ codigo, nome: v.nome.trim(), descricao, permissoes });
    this.enviar(req, papel ? 'Perfil atualizado' : 'Perfil criado', `${v.nome.trim()} · ${permissoes.length} permissões`, () => {
      const salvo = papel ?? this.papeis().find((p) => p.codigo === codigo);
      if (salvo) this.papelSelecionadoId.set(salvo.uuid);
      return null;
    });
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string, proxima?: () => Gaveta | null): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.mostrarToast(titulo, detalhe);
        this.depoisDeCarregar = proxima ?? null;
        this.carregarTudo();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        const mensagem = body?.message ?? 'Não foi possível salvar. Tente novamente.';
        if (this.gaveta()) {
          this.erroApi.set(mensagem);
        } else {
          this.mostrarToast('Não foi possível concluir', mensagem);
        }
      },
    });
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}

function statusDoUsuario(u: UsuarioResponseDto, acessos: number): LinhaUsuario['status'] {
  if (!u.ativo) return { classe: 'muted', rotulo: 'Inativo', dot: 'off' };
  if (u.bloqueado) return { classe: 'alert', rotulo: 'Bloqueado', dot: 'warn' };
  if (!acessos) return { classe: 'warn', rotulo: 'Sem acesso', dot: 'off' };
  return { classe: 'ok', rotulo: 'Ativo', dot: '' };
}

function pad(v: number): string {
  return String(v).padStart(2, '0');
}

function formatarData(iso: string | null | undefined): string {
  if (!iso) return '—';
  const [a, m, d] = iso.slice(0, 10).split('-');
  return `${d}/${m}/${a}`;
}

/** Mesmo cálculo do backend (ADR-0068): 11 dígitos, não todos iguais, dígitos verificadores corretos. */
function cpfValido(cpf: string): boolean {
  if (!/^\d{11}$/.test(cpf) || /^(\d)\1{10}$/.test(cpf)) return false;
  for (const posicao of [9, 10]) {
    let soma = 0;
    for (let i = 0; i < posicao; i++) soma += Number(cpf[i]) * (posicao + 1 - i);
    if (((soma * 10) % 11) % 10 !== Number(cpf[posicao])) return false;
  }
  return true;
}
