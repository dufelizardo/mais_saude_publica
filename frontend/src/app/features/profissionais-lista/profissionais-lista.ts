import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, catchError, debounceTime, distinctUntilChanged, of } from 'rxjs';
import { AcessoDaInterface } from '../../core/models/auth';
import { CargoResponseDto } from '../../core/models/cargo';
import { ErrorResponseDto, QuadroProfissionalResponseDto, SuccessResponseDto } from '../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { AuthService } from '../../core/services/auth';
import { CargoService } from '../../core/services/cargo';
import { CepService } from '../../core/services/cep';
import { LotacaoService } from '../../core/services/lotacao';
import { ProfissionalService } from '../../core/services/profissional';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { Drawer } from '../../shared/drawer/drawer';
import { formatCpf, formatTelefone } from '../../shared/format-mask';
import { CATEGORIAS_PROFISSIONAL, CategoriaProfissional, categoriaDoProfissional, situacaoDoProfissional } from '../../shared/profissional-categoria';

type Visao = 'cartoes' | 'lista';
type FiltroSituacao = '' | 'ativos' | 'afastados' | 'sem-lotacao' | 'desligados';
type Gaveta = { tipo: 'novo' } | { tipo: 'desligar'; item: QuadroProfissionalResponseDto };

const POR_PAGINA = 9;
const CHAVE_VISAO = 'msp-profissionais-visao';

/**
 * Profissionais (ADR-0072) — portada do protótipo Profissionais.html: quadro em cartões (padrão) ou lista, com
 * categoria pelo conselho de classe, lotação e afastamento vigentes. Cadastro e desligamento são gavetas desta
 * tela; as rotas antigas redirecionam para cá. Escala, equipe e carga realizada não existem ainda: "Em breve".
 */
@Component({
  selector: 'app-profissionais-lista',
  imports: [ReactiveFormsModule, RouterLink, Drawer],
  templateUrl: './profissionais-lista.html',
  styleUrl: './profissionais-lista.css',
})
export class ProfissionaisLista {
  private readonly fb = inject(FormBuilder);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly authService = inject(AuthService);
  private readonly cargoService = inject(CargoService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly lotacaoService = inject(LotacaoService);
  private readonly cepService = inject(CepService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly formatCpf = formatCpf;
  protected readonly categorias = CATEGORIAS_PROFISSIONAL;
  protected readonly categoriaDe = categoriaDoProfissional;
  protected readonly situacaoDe = situacaoDoProfissional;

  protected readonly quadro = signal<QuadroProfissionalResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly visao = signal<Visao>(lerVisao());
  protected readonly categoria = signal<CategoriaProfissional['id'] | ''>('');
  protected readonly busca = signal('');
  protected readonly filtroUnidade = signal('');
  protected readonly filtroFuncao = signal('');
  protected readonly filtroSituacao = signal<FiltroSituacao>('ativos');
  protected readonly pagina = signal(1);
  protected readonly menuAberto = signal<string | null>(null);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly cargos = signal<CargoResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly buscandoCep = signal(false);
  protected readonly cepNaoEncontrado = signal(false);

  protected readonly novoForm = this.fb.nonNullable.group({
    cpf: [''],
    nome: [''],
    email: [''],
    conselhoClasse: [''],
    numeroConselho: [''],
    telefone: [''],
    dataAdmissao: [hoje()],
    unidadeId: [''],
    cargoId: [''],
    cep: [''],
    logradouro: [''],
    numeroLogradouro: [''],
    complemento: [''],
    bairro: [''],
    cidade: [''],
    estado: [''],
  });

  protected readonly desligarForm = this.fb.nonNullable.group({ dataDesligamento: [hoje()] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.carregar();
    this.novoForm.controls.cep.valueChanges.pipe(debounceTime(400), distinctUntilChanged()).subscribe((cep) => this.buscarCep(cep));

    // Rotas antigas (/profissionais/novo, /profissionais/desligar) chegam aqui com ?acao= (ADR-0072).
    const params = this.route.snapshot.queryParamMap;
    if (params.get('acao') === 'novo') this.abrirNovo();
    this.cpfParaDesligar = params.get('acao') === 'desligar' ? params.get('cpf') : null;
  }

  private cpfParaDesligar: string | null = null;

  /** Lista vazia responde 404 no backend — tratada como quadro vazio. */
  private carregar(): void {
    this.carregando.set(true);
    this.profissionalService.quadro().pipe(catchError(() => of([]))).subscribe((q) => {
      this.quadro.set(q);
      this.carregando.set(false);
      if (this.cpfParaDesligar) {
        const item = q.find((i) => i.profissional.cpf.replace(/\D/g, '') === this.cpfParaDesligar!.replace(/\D/g, ''));
        this.cpfParaDesligar = null;
        if (item) this.abrirDesligar(item);
      }
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  // ── Filtros ────────────────────────────────────────────────────────────────────────────────────

  /** Tudo, menos a categoria — base das contagens das abas. */
  private readonly semCategoria = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const digitos = q.replace(/\D/g, '');
    const unidade = this.filtroUnidade();
    const funcao = this.filtroFuncao();
    const situacao = this.filtroSituacao();
    return this.quadro().filter((i) => {
      const p = i.profissional;
      if (q) {
        const texto = `${p.nome} ${p.matricula} ${p.conselhoClasse ?? ''} ${p.numeroConselho ?? ''}`.toLowerCase();
        if (!texto.includes(q) && !(digitos.length >= 3 && p.cpf.replace(/\D/g, '').includes(digitos))) return false;
      }
      if (unidade && i.lotacao?.unidadeUuid !== unidade) return false;
      if (funcao && i.lotacao?.cargoNome !== funcao) return false;
      switch (situacao) {
        case 'ativos': return p.ativo;
        case 'afastados': return p.ativo && !!i.afastamento;
        case 'sem-lotacao': return p.ativo && !i.lotacao;
        case 'desligados': return !p.ativo;
        default: return true;
      }
    });
  });

  /** Contagem da aba "Todos". */
  protected readonly faixaTodos = computed(() => this.semCategoria().length);

  protected readonly filtrados = computed(() => {
    const cat = this.categoria();
    return cat ? this.semCategoria().filter((i) => categoriaDoProfissional(i.profissional).id === cat) : this.semCategoria();
  });

  protected readonly contagemPorCategoria = computed(() => {
    const mapa = new Map<string, number>();
    for (const i of this.semCategoria()) {
      const id = categoriaDoProfissional(i.profissional).id;
      mapa.set(id, (mapa.get(id) ?? 0) + 1);
    }
    return mapa;
  });

  /** Só as categorias que existem no quadro filtrado aparecem como aba. */
  protected readonly abasCategoria = computed(() => CATEGORIAS_PROFISSIONAL.filter((c) => (this.contagemPorCategoria().get(c.id) ?? 0) > 0));

  protected readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.filtrados().length / this.porPagina())));
  private readonly porPagina = computed(() => (this.visao() === 'cartoes' ? POR_PAGINA : 20));

  protected readonly paginados = computed(() => {
    const pagina = Math.min(this.pagina(), this.totalPaginas());
    return this.filtrados().slice((pagina - 1) * this.porPagina(), pagina * this.porPagina());
  });

  protected readonly faixa = computed(() => {
    const total = this.filtrados().length;
    if (!total) return { de: 0, ate: 0, total };
    const pagina = Math.min(this.pagina(), this.totalPaginas());
    return { de: (pagina - 1) * this.porPagina() + 1, ate: Math.min(pagina * this.porPagina(), total), total };
  });

  protected readonly paginas = computed<(number | null)[]>(() => {
    const total = this.totalPaginas();
    const atual = Math.min(this.pagina(), total);
    const lista: (number | null)[] = [];
    for (let p = 1; p <= total; p++) {
      if (p === 1 || p === total || Math.abs(p - atual) <= 1 || (atual <= 3 && p <= 4)) lista.push(p);
      else if (lista[lista.length - 1] !== null) lista.push(null);
    }
    return lista;
  });

  protected readonly unidadesDoQuadro = computed(() => {
    const mapa = new Map<string, string>();
    for (const i of this.quadro()) if (i.lotacao) mapa.set(i.lotacao.unidadeUuid, i.lotacao.unidadeNome);
    return [...mapa.entries()].map(([uuid, nome]) => ({ uuid, nome })).sort((a, b) => a.nome.localeCompare(b.nome));
  });

  protected readonly funcoesDoQuadro = computed(() =>
    [...new Set(this.quadro().map((i) => i.lotacao?.cargoNome).filter((c): c is string => !!c))].sort((a, b) => a.localeCompare(b)),
  );

  protected readonly resumo = computed(() => {
    const ativos = this.quadro().filter((i) => i.profissional.ativo);
    const mes = hoje().slice(0, 7);
    const pct = (n: number) => (ativos.length ? `${Math.round((n / ativos.length) * 1000) / 10}`.replace('.', ',') + '% do quadro' : '—');
    const medicos = ativos.filter((i) => categoriaDoProfissional(i.profissional).id === 'MED').length;
    const enfermagem = ativos.filter((i) => categoriaDoProfissional(i.profissional).id === 'ENF').length;
    const afastados = ativos.filter((i) => i.afastamento);
    const ferias = afastados.filter((i) => i.afastamento!.tipo === 'FERIAS').length;
    return {
      ativos: ativos.length,
      admissoesMes: ativos.filter((i) => (i.profissional.dataAdmissao ?? '').startsWith(mes)).length,
      medicos,
      medicosPct: pct(medicos),
      enfermagem,
      enfermagemPct: pct(enfermagem),
      afastados: afastados.length,
      ferias,
      outrosAfastamentos: afastados.length - ferias,
    };
  });

  protected mudarVisao(v: Visao): void {
    this.visao.set(v);
    this.pagina.set(1);
    try {
      localStorage.setItem(CHAVE_VISAO, v);
    } catch {
      // Sem storage: a escolha vale só nesta visita.
    }
  }

  protected escolherCategoria(id: CategoriaProfissional['id'] | ''): void {
    this.categoria.set(id);
    this.pagina.set(1);
  }

  protected limparFiltros(): void {
    this.busca.set('');
    this.filtroUnidade.set('');
    this.filtroFuncao.set('');
    this.filtroSituacao.set('ativos');
    this.categoria.set('');
    this.pagina.set(1);
  }

  protected irParaPagina(p: number): void {
    this.pagina.set(Math.max(1, Math.min(p, this.totalPaginas())));
  }

  // ── Apoio de exibição ──────────────────────────────────────────────────────────────────────────

  protected iniciais(nome: string): string {
    const partes = nome.replace(/^(dra?\.?|enf\.?|t[eé]c\.?)\s+/i, '').trim().split(/\s+/).filter((p) => p.length > 2);
    return ((partes[0]?.[0] ?? '?') + (partes.length > 1 ? partes[partes.length - 1][0] : '')).toUpperCase();
  }

  protected conselhoDe(i: QuadroProfissionalResponseDto): string | null {
    const p = i.profissional;
    if (!p.conselhoClasse && !p.numeroConselho) return null;
    return `${p.conselhoClasse ?? ''} ${p.numeroConselho ?? ''}`.trim();
  }

  protected telefoneDe(i: QuadroProfissionalResponseDto): string | null {
    const t = i.profissional.telefones?.[0];
    return t ? formatTelefone(t) : null;
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected alternarMenu(matricula: string, event: Event): void {
    event.stopPropagation();
    this.menuAberto.update((atual) => (atual === matricula ? null : matricula));
  }

  protected fecharMenu(): void {
    this.menuAberto.set(null);
  }

  protected verPerfil(i: QuadroProfissionalResponseDto): void {
    this.router.navigate(['/profissionais/perfil'], { queryParams: { cpf: i.profissional.cpf } });
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  private abrir(g: Gaveta): void {
    this.menuAberto.set(null);
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirNovo(): void {
    this.novoForm.reset({ dataAdmissao: hoje() });
    this.cepNaoEncontrado.set(false);
    if (!this.cargos().length) this.cargoService.listar().pipe(catchError(() => of([]))).subscribe((c) => this.cargos.set(c));
    if (!this.unidades().length) this.unidadeSaudeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.abrir({ tipo: 'novo' });
  }

  protected abrirDesligar(item: QuadroProfissionalResponseDto): void {
    this.desligarForm.reset({ dataDesligamento: hoje() });
    this.abrir({ tipo: 'desligar', item });
  }

  protected mascararCpf(event: Event): void {
    this.novoForm.controls.cpf.setValue(formatCpf((event.target as HTMLInputElement).value));
  }

  protected mascararTelefone(event: Event): void {
    this.novoForm.controls.telefone.setValue(formatTelefone((event.target as HTMLInputElement).value));
  }

  private buscarCep(valor: string): void {
    const cep = valor.replace(/\D/g, '');
    this.cepNaoEncontrado.set(false);
    if (cep.length !== 8) return;
    this.buscandoCep.set(true);
    this.cepService.buscar(cep).subscribe({
      next: (e) => {
        this.buscandoCep.set(false);
        if (e.erro) {
          this.cepNaoEncontrado.set(true);
          return;
        }
        this.novoForm.patchValue({ logradouro: e.logradouro, bairro: e.bairro, cidade: e.localidade, estado: e.uf });
      },
      error: () => {
        this.buscandoCep.set(false);
        this.cepNaoEncontrado.set(true);
      },
    });
  }

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
    if (g?.tipo === 'novo') this.salvarNovo();
    if (g?.tipo === 'desligar') this.salvarDesligamento(g.item);
  }

  private salvarNovo(): void {
    const v = this.novoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (v.cpf.replace(/\D/g, '').length !== 11) erros['cpf'] = 'Informe o CPF (11 dígitos).';
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome completo.';
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(v.email.trim())) erros['email'] = 'Informe um e-mail válido.';
    if (!v.telefone.trim()) erros['telefone'] = 'Informe o telefone.';
    if (!v.dataAdmissao) erros['dataAdmissao'] = 'Informe a data de admissão.';
    if (!v.unidadeId) erros['unidadeId'] = 'Escolha a unidade de lotação.';
    if (!v.cargoId) erros['cargoId'] = 'Escolha o cargo.';
    for (const [campo, rotulo] of [['cep', 'o CEP'], ['logradouro', 'o logradouro'], ['numeroLogradouro', 'o número'], ['bairro', 'o bairro'], ['cidade', 'a cidade'], ['estado', 'a UF']] as const) {
      if (!v[campo].trim()) erros[campo] = `Informe ${rotulo}.`;
    }
    if (!this.validar(erros)) return;

    this.submitting.set(true);
    this.profissionalService
      .create({
        cpf: v.cpf,
        nome: v.nome.trim(),
        email: v.email.trim(),
        telefones: [v.telefone],
        endereco: {
          cep: v.cep,
          logradouro: v.logradouro,
          numeroLogradouro: v.numeroLogradouro,
          complemento: v.complemento || undefined,
          bairro: v.bairro,
          cidade: v.cidade,
          estado: v.estado,
        },
        conselhoClasse: v.conselhoClasse.trim() || undefined,
        numeroConselho: v.numeroConselho.trim() || undefined,
        dataAdmissao: v.dataAdmissao,
      })
      .subscribe({
        next: () => this.lotarNaAdmissao(v.cpf, v.unidadeId, v.cargoId, v.dataAdmissao, v.nome.trim()),
        error: (e: HttpErrorResponse) => this.falhou(e, 'Não foi possível cadastrar o profissional.'),
      });
  }

  /** Cadastro e lotação de admissão (ADR-0019): se a lotação falhar, o cadastro fica e o aviso diz o que fazer. */
  private lotarNaAdmissao(cpf: string, unidadeId: string, cargoId: string, dataAdmissao: string, nome: string): void {
    this.profissionalService.buscarPorCpf(cpf).subscribe({
      next: (p) => {
        this.lotacaoService
          .criar({ matriculaProfissional: p.matricula, unidadeId, cargoId, dataInicio: dataAdmissao, motivo: 'Admissão' })
          .subscribe({
            next: () => this.concluir('Profissional cadastrado', `${nome} · matrícula ${p.matricula}`),
            error: (e: HttpErrorResponse) => {
              this.submitting.set(false);
              this.gaveta.set(null);
              this.carregar();
              this.mostrarToast('Cadastrado sem lotação',
                `Matrícula ${p.matricula}. Não foi possível lotar: ${(e.error as ErrorResponseDto | undefined)?.message ?? 'erro desconhecido'} — registre a lotação no perfil.`);
            },
          });
      },
      error: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.carregar();
        this.mostrarToast('Cadastrado sem lotação', 'Não foi possível confirmar a matrícula — registre a lotação no perfil.');
      },
    });
  }

  private salvarDesligamento(item: QuadroProfissionalResponseDto): void {
    const data = this.desligarForm.getRawValue().dataDesligamento;
    if (!this.validar(data ? {} : { dataDesligamento: 'Informe a data de desligamento.' })) return;
    this.enviar(this.profissionalService.desligar(item.profissional.cpf, data), 'Profissional desligado',
      `${item.profissional.nome} · ${this.data(data)}`);
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => this.concluir(titulo, detalhe),
      error: (e: HttpErrorResponse) => this.falhou(e, 'Não foi possível salvar. Tente novamente.'),
    });
  }

  private concluir(titulo: string, detalhe: string): void {
    this.submitting.set(false);
    this.gaveta.set(null);
    this.mostrarToast(titulo, detalhe);
    this.carregar();
  }

  private falhou(e: HttpErrorResponse, padrao: string): void {
    this.submitting.set(false);
    this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? padrao);
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 5000);
  }
}

function hoje(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function lerVisao(): Visao {
  try {
    return localStorage.getItem(CHAVE_VISAO) === 'lista' ? 'lista' : 'cartoes';
  } catch {
    return 'cartoes';
  }
}
