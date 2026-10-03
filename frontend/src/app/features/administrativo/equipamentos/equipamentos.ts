import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { ErrorResponseDto } from '../../../core/models/profissional';
import {
  DIAS_SEMANA,
  DiaSemana,
  FichaUnidadeDto,
  NIVEIS_DE_GESTAO,
  RedeUnidadeResumoDto,
  RedeUnidadesResponseDto,
  SITUACOES_OPERACIONAIS,
  SituacaoOperacional,
  TIPOS_UNIDADE,
  TurnoHorarioDto,
} from '../../../core/models/rede-unidades';
import { TipoUnidadeDeSaude } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { RedeUnidadesService } from '../../../core/services/rede-unidades';
import { Drawer } from '../../../shared/drawer/drawer';

type AbaFicha = 'dados' | 'operacional' | 'equipes' | 'vinculacoes' | 'historico';
type Gaveta = { tipo: 'unidade'; ficha: FichaUnidadeDto | null } | { tipo: 'horario'; ficha: FichaUnidadeDto } | { tipo: 'situacao'; ficha: FichaUnidadeDto };

interface DiaEditavel {
  dia: DiaSemana;
  rotulo: string;
  aberto: boolean;
  abre1: string;
  fecha1: string;
  abre2: string;
  fecha2: string;
}

const UNIDADES_DE_ATENDIMENTO = (Object.keys(TIPOS_UNIDADE) as TipoUnidadeDeSaude[]).filter((t) => !NIVEIS_DE_GESTAO.includes(t));
const ESPECIALIZADAS: TipoUnidadeDeSaude[] = ['HOSPITAL', 'UPA', 'CAPS', 'CENTRO_ESPECIALIDADES', 'CENTRO_REABILITACAO', 'POLICLINICA', 'LABORATORIO'];

/**
 * Equipamentos de Saúde (ADR-0102), pelo protótipo `Equipamentos.html`: indicadores, filtros, a lista de unidades e o
 * painel da unidade selecionada (dados gerais, operacional, equipes, vinculações e histórico), com as gavetas de
 * cadastro, horário e situação. O backend é a ADR-0101.
 */
@Component({
  selector: 'app-equipamentos',
  imports: [ReactiveFormsModule, RouterLink, Drawer],
  templateUrl: './equipamentos.html',
})
export class Equipamentos {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly redeService = inject(RedeUnidadesService);

  protected readonly tipos = TIPOS_UNIDADE;
  protected readonly tiposDeAtendimento = UNIDADES_DE_ATENDIMENTO;
  protected readonly todosOsTipos = Object.keys(TIPOS_UNIDADE) as TipoUnidadeDeSaude[];
  protected readonly situacoes = SITUACOES_OPERACIONAIS;
  protected readonly listaSituacoes = Object.keys(SITUACOES_OPERACIONAIS) as SituacaoOperacional[];
  protected readonly dias = DIAS_SEMANA;

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly rede = signal<RedeUnidadesResponseDto | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly selecionadaId = signal<string | null>(null);
  protected readonly ficha = signal<FichaUnidadeDto | null>(null);
  protected readonly carregandoFicha = signal(false);
  protected readonly abaFicha = signal<AbaFicha>('dados');

  protected readonly busca = signal('');
  protected readonly regionalFiltro = signal('');
  protected readonly tipoFiltro = signal<TipoUnidadeDeSaude | '' | 'GESTAO'>('');
  protected readonly situacaoFiltro = signal<SituacaoOperacional | ''>('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly horarioEdit = signal<DiaEditavel[]>([]);
  protected readonly funciona24hEdit = signal(false);

  protected readonly unidadeForm = this.fb.nonNullable.group({
    nome: [''],
    tipo: ['UBS' as TipoUnidadeDeSaude],
    cnes: [''],
    unidadeSuperiorId: [''],
    supervisaoRegionalId: [''],
    responsavelCpf: [''],
    email: [''],
    telefone: [''],
    cep: [''],
    logradouro: [''],
    numeroLogradouro: [''],
    complemento: [''],
    bairro: [''],
    cidade: [''],
    estado: [''],
  });
  protected readonly situacaoForm = this.fb.nonNullable.group({
    situacao: ['EM_OPERACAO' as SituacaoOperacional],
    motivo: [''],
    previsaoRetorno: [''],
  });

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────

  protected readonly podeEditar = computed(() => this.pode('ORGANIZACAO.GERENCIAR'));

  protected readonly regionais = computed(() => (this.rede()?.rede ?? []).filter((u) => u.tipo === 'REGIONAL'));

  protected readonly filtradas = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const tipo = this.tipoFiltro();
    return (this.rede()?.rede ?? [])
      .filter((u) => (tipo === 'GESTAO' ? NIVEIS_DE_GESTAO.includes(u.tipo) : tipo ? u.tipo === tipo : !NIVEIS_DE_GESTAO.includes(u.tipo)))
      .filter((u) => !this.regionalFiltro() || u.supervisaoRegionalId === this.regionalFiltro())
      .filter((u) => !this.situacaoFiltro() || u.situacaoOperacional === this.situacaoFiltro())
      .filter((u) => !q || u.nome.toLowerCase().includes(q) || (u.cnes ?? '').includes(q.replace(/\D/g, '') || '§'));
  });

  protected readonly indicadores = computed(() => {
    const r = this.rede();
    const porTipo = r?.porTipo ?? {};
    const atencaoBasica = porTipo.UBS ?? 0;
    const especializadas = ESPECIALIZADAS.reduce((s, t) => s + (porTipo[t] ?? 0), 0);
    return {
      ativas: r?.ativas ?? 0,
      total: r?.unidades ?? 0,
      atencaoBasica,
      percentualBasica: r?.unidades ? Math.round((atencaoBasica * 1000) / r.unidades) / 10 : 0,
      especializadas,
      upa: porTipo.UPA ?? 0,
      hospitais: porTipo.HOSPITAL ?? 0,
      caps: porTipo.CAPS ?? 0,
      foraDeOperacao: r?.foraDeOperacao ?? 0,
    };
  });

  /** Unidades do nível acima do tipo escolhido (ADR-0009): método, porque o tipo vem do formulário, não de um signal. */
  protected superioresPossiveis(): RedeUnidadeResumoDto[] {
    const tipo = this.unidadeForm.controls.tipo.value;
    const esperado: TipoUnidadeDeSaude | null =
      tipo === 'ESTADUAL' ? 'FEDERAL' : tipo === 'MUNICIPAL' ? 'ESTADUAL' : tipo === 'REGIONAL' ? 'MUNICIPAL' : tipo === 'FEDERAL' ? null : 'MUNICIPAL';
    return (this.rede()?.rede ?? []).filter((u) => esperado && u.tipo === esperado);
  }

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.selecionadaId.set(this.route.snapshot.queryParamMap.get('unidade'));
    this.carregar();
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected carregar(depois?: () => void): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.redeService.rede().subscribe({
      next: (r) => {
        this.rede.set(r);
        this.carregando.set(false);
        const atual = this.selecionadaId();
        const lista = this.filtradas();
        if (atual && r.rede.some((u) => u.uuid === atual)) this.selecionar(atual, false);
        else if (lista.length) this.selecionar(lista[0].uuid, false);
        depois?.();
      },
      error: (e: HttpErrorResponse) => {
        this.carregando.set(false);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar a rede de unidades.');
      },
    });
  }

  protected selecionar(uuid: string, atualizarUrl = true): void {
    this.selecionadaId.set(uuid);
    this.carregandoFicha.set(true);
    if (atualizarUrl) this.router.navigate([], { queryParams: { unidade: uuid }, queryParamsHandling: 'merge', replaceUrl: true });
    this.redeService.ficha(uuid).subscribe({
      next: (f) => {
        this.ficha.set(f);
        this.carregandoFicha.set(false);
      },
      error: () => {
        this.ficha.set(null);
        this.carregandoFicha.set(false);
      },
    });
  }

  protected teclaNaLinha(event: KeyboardEvent, uuid: string): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.selecionar(uuid);
    }
  }

  // ── Apresentação ───────────────────────────────────────────────────────────────────────────

  protected marcador(u: RedeUnidadeResumoDto): string {
    if (!u.ativo || u.situacaoOperacional === 'INOPERANTE') return 'unit-pin t-closed';
    if (u.situacaoOperacional !== 'EM_OPERACAO') return 'unit-pin t-manut';
    return `unit-pin ${this.tipos[u.tipo].familia}`;
  }

  protected ocupacao(u: RedeUnidadeResumoDto): number {
    return u.leitos ? Math.round((u.leitosOcupados * 100) / u.leitos) : 0;
  }

  protected classeBarra(p: number): string {
    return p >= 95 ? 'crit' : p >= 85 ? 'hi' : p < 60 ? 'lo' : '';
  }

  protected meta(u: RedeUnidadeResumoDto): string {
    const partes = [this.tipos[u.tipo].rotulo];
    if (u.cnes) partes.push(`CNES ${this.cnes(u.cnes)}`);
    if (u.situacaoOperacional !== 'EM_OPERACAO') partes.push(this.situacoes[u.situacaoOperacional].rotulo);
    else if (u.profissionaisLotados) partes.push(`${u.profissionaisLotados} prof.`);
    if (u.endereco) partes.push(u.endereco);
    return partes.join(' · ');
  }

  protected rotuloTipo(tipo: string): string {
    return TIPOS_UNIDADE[tipo as TipoUnidadeDeSaude]?.rotulo ?? tipo;
  }

  protected cnes(c: string | null | undefined): string {
    return c ? `${c.slice(0, 1)} ${c.slice(1, 4)} ${c.slice(4)}` : '—';
  }

  protected iniciais(nome: string): string {
    return nome
      .split(/\s+/)
      .filter((p) => p.length > 2)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  protected instante(iso: string | null | undefined): string {
    if (!iso) return '—';
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  /** Horário do dia no painel: turnos estruturados, o texto antigo, ou "Fechado". */
  protected horarioDoDia(f: FichaUnidadeDto, dia: DiaSemana): string {
    if (f.resumo.funciona24h) return '24 horas';
    const turnos = f.turnos.filter((t) => t.diaSemana === dia);
    if (turnos.length) return turnos.map((t) => `${t.abre.slice(0, 5)} — ${t.fecha.slice(0, 5)}`).join(' · ');
    if (!f.turnos.length && f.horarioFuncionamentoTexto?.[dia]) return f.horarioFuncionamentoTexto[dia]!;
    return 'Fechado';
  }

  protected hoje(dia: DiaSemana): boolean {
    return DIAS_SEMANA[(new Date().getDay() + 6) % 7].id === dia;
  }

  protected erroCampo(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    if (g.tipo === 'unidade') return g.ficha ? 'Editar unidade' : 'Nova unidade';
    if (g.tipo === 'horario') return 'Horário de funcionamento';
    return 'Situação operacional';
  }

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirUnidade(f: FichaUnidadeDto | null = null): void {
    const r = f?.resumo;
    this.unidadeForm.reset({
      nome: r?.nome ?? '',
      tipo: r?.tipo ?? 'UBS',
      cnes: r?.cnes ?? '',
      unidadeSuperiorId: r?.unidadeSuperiorId ?? '',
      supervisaoRegionalId: r?.supervisaoRegionalId ?? '',
      responsavelCpf: f?.responsavelCpf ?? '',
      email: r?.email ?? '',
      telefone: f?.telefones?.[0] ?? '',
      cep: f?.cep ?? '',
      logradouro: f?.logradouro ?? '',
      numeroLogradouro: f?.numeroLogradouro ?? '',
      complemento: f?.complemento ?? '',
      bairro: f?.bairro ?? '',
      cidade: f?.cidade ?? '',
      estado: f?.uf ?? '',
    });
    this.abrir({ tipo: 'unidade', ficha: f });
  }

  protected abrirHorario(f: FichaUnidadeDto): void {
    this.funciona24hEdit.set(f.resumo.funciona24h);
    this.horarioEdit.set(
      DIAS_SEMANA.map((d) => {
        const turnos = f.turnos.filter((t) => t.diaSemana === d.id);
        return {
          dia: d.id,
          rotulo: d.rotulo,
          aberto: turnos.length > 0,
          abre1: turnos[0]?.abre.slice(0, 5) ?? '07:00',
          fecha1: turnos[0]?.fecha.slice(0, 5) ?? '19:00',
          abre2: turnos[1]?.abre.slice(0, 5) ?? '',
          fecha2: turnos[1]?.fecha.slice(0, 5) ?? '',
        };
      }),
    );
    this.abrir({ tipo: 'horario', ficha: f });
  }

  protected mudarDia(i: number, campo: keyof DiaEditavel, valor: string | boolean): void {
    this.horarioEdit.update((dias) => dias.map((d, j) => (j === i ? { ...d, [campo]: valor } : d)));
  }

  protected abrirSituacao(f: FichaUnidadeDto): void {
    this.situacaoForm.reset({
      situacao: f.resumo.situacaoOperacional === 'EM_OPERACAO' ? 'EM_MANUTENCAO' : 'EM_OPERACAO',
      motivo: '',
      previsaoRetorno: '',
    });
    this.abrir({ tipo: 'situacao', ficha: f });
  }

  // ── Envio ──────────────────────────────────────────────────────────────────────────────────

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    this.erros.set({});
    this.erroApi.set(null);
    if (g.tipo === 'unidade') this.salvarUnidade(g.ficha);
    else if (g.tipo === 'horario') this.salvarHorario(g.ficha);
    else this.salvarSituacao(g.ficha);
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

  private salvarUnidade(f: FichaUnidadeDto | null): void {
    const v = this.unidadeForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome da unidade.';
    if (v.cnes.trim() && !/^\d{7}$/.test(v.cnes.trim())) erros['cnes'] = 'O CNES tem 7 dígitos.';
    if (v.email.trim() && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(v.email.trim())) erros['email'] = 'E-mail inválido.';
    // Endereço vai inteiro ou não vai: com algum campo preenchido, os obrigatórios precisam estar todos.
    const comEndereco = [v.cep, v.logradouro, v.numeroLogradouro, v.complemento, v.bairro, v.cidade, v.estado].some((c) => c.trim());
    if (comEndereco) {
      const obrigatorios: [string, string, string][] = [
        ['logradouro', v.logradouro, 'Informe o logradouro.'],
        ['numeroLogradouro', v.numeroLogradouro, 'Informe o número.'],
        ['bairro', v.bairro, 'Informe o bairro.'],
        ['cep', v.cep, 'Informe o CEP.'],
        ['cidade', v.cidade, 'Informe a cidade.'],
        ['estado', v.estado, 'Informe a UF.'],
      ];
      for (const [campo, valor, mensagem] of obrigatorios) if (!valor.trim()) erros[campo] = mensagem;
    }
    if (!this.validar(erros)) return;
    const dto = {
      nome: v.nome.trim(),
      tipo: v.tipo,
      cnes: v.cnes.trim() || undefined,
      unidadeSuperiorId: f ? undefined : v.unidadeSuperiorId || undefined,
      supervisaoRegionalId: v.supervisaoRegionalId || undefined,
      responsavelCpf: v.responsavelCpf.replace(/\D/g, '') || undefined,
      email: v.email.trim() || undefined,
      telefones: v.telefone.trim() ? [v.telefone.trim()] : [],
      endereco: !comEndereco ? undefined : {
        cep: v.cep.trim() || undefined,
        logradouro: v.logradouro.trim() || undefined,
        numeroLogradouro: v.numeroLogradouro.trim() || undefined,
        complemento: v.complemento.trim() || undefined,
        bairro: v.bairro.trim() || undefined,
        cidade: v.cidade.trim() || undefined,
        estado: v.estado.trim().toUpperCase() || undefined,
      },
    };
    this.enviar(
      f ? this.redeService.atualizar(f.resumo.uuid, dto) : this.redeService.criar(dto),
      f ? 'Unidade atualizada' : 'Unidade cadastrada',
      dto.nome,
    );
  }

  private salvarHorario(f: FichaUnidadeDto): void {
    const turnos: TurnoHorarioDto[] = [];
    const erros: Record<string, string> = {};
    for (const d of this.horarioEdit()) {
      if (!d.aberto) continue;
      if (!d.abre1 || !d.fecha1 || d.fecha1 <= d.abre1) {
        erros['horario'] = `${d.rotulo}: o turno precisa fechar depois de abrir.`;
        break;
      }
      turnos.push({ diaSemana: d.dia, abre: d.abre1, fecha: d.fecha1 });
      if (d.abre2 || d.fecha2) {
        if (!d.abre2 || !d.fecha2 || d.fecha2 <= d.abre2 || d.abre2 < d.fecha1) {
          erros['horario'] = `${d.rotulo}: o segundo turno precisa começar depois do primeiro e fechar depois de abrir.`;
          break;
        }
        turnos.push({ diaSemana: d.dia, abre: d.abre2, fecha: d.fecha2 });
      }
    }
    if (!this.validar(erros)) return;
    this.enviar(this.redeService.horarios(f.resumo.uuid, this.funciona24hEdit(), turnos), 'Horário salvo', f.resumo.nome);
  }

  private salvarSituacao(f: FichaUnidadeDto): void {
    const v = this.situacaoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (v.situacao !== 'EM_OPERACAO' && !v.motivo.trim()) erros['motivoSituacao'] = 'Informe o motivo.';
    if (v.previsaoRetorno && v.previsaoRetorno < new Date().toISOString().slice(0, 10)) erros['previsaoRetorno'] = 'A previsão não pode ser no passado.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.redeService.situacao(f.resumo.uuid, v.situacao, v.motivo.trim() || undefined, v.previsaoRetorno || undefined),
      'Situação atualizada',
      `${f.resumo.nome} · ${this.situacoes[v.situacao].rotulo}`,
    );
  }

  private enviar(req: Observable<FichaUnidadeDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: (f) => {
        this.submitting.set(false);
        this.fecharGaveta();
        this.mostrarToast(titulo, detalhe);
        this.selecionadaId.set(f.resumo.uuid);
        this.carregar();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((error.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}
