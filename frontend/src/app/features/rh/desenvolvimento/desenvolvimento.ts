import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { CicloAvaliacaoResponseDto } from '../../../core/models/avaliacao';
import { ErrorResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { TreinamentoResponseDto } from '../../../core/models/treinamento';
import { AuthService } from '../../../core/services/auth';
import { AvaliacaoService } from '../../../core/services/avaliacao';
import { TreinamentoService } from '../../../core/services/treinamento';
import { Drawer } from '../../../shared/drawer/drawer';
import { dataBr, hojeIso, proximaAba } from '../../../shared/formato';

type Aba = 'treinamentos' | 'ciclos';
type Gaveta = { tipo: 'treinamento' } | { tipo: 'ciclo' };
type StatusCiclo = { rotulo: 'Agendado' | 'Em andamento' | 'Encerrado'; classe: string };

/**
 * Desenvolvimento (ADR-0073): catálogo de treinamentos e ciclos de avaliação de desempenho. Junta as antigas telas
 * Catálogo de treinamentos e Ciclos de avaliação; as rotas antigas redirecionam. A participação em treinamento e a
 * avaliação de cada profissional continuam nas abas do perfil.
 */
@Component({
  selector: 'app-desenvolvimento',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './desenvolvimento.html',
})
export class Desenvolvimento {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly treinamentoService = inject(TreinamentoService);
  private readonly avaliacaoService = inject(AvaliacaoService);
  private readonly route = inject(ActivatedRoute);

  protected readonly dataBr = dataBr;
  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'treinamentos', rotulo: 'Catálogo de treinamentos' },
    { id: 'ciclos', rotulo: 'Ciclos de avaliação' },
  ];

  protected readonly treinamentos = signal<TreinamentoResponseDto[]>([]);
  protected readonly ciclos = signal<CicloAvaliacaoResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly aba = signal<Aba>('treinamentos');
  protected readonly busca = signal('');
  protected readonly soObrigatorios = signal(false);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly treinamentoForm = this.fb.nonNullable.group({ nome: [''], cargaHoraria: [''], validadeMeses: [''], obrigatorio: [false] });
  protected readonly cicloForm = this.fb.nonNullable.group({ nome: [''], dataInicio: [hojeIso()], dataFim: [''] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    if (this.route.snapshot.queryParamMap.get('aba') === 'ciclos') this.aba.set('ciclos');
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    forkJoin({
      treinamentos: this.treinamentoService.listarCatalogo().pipe(catchError(() => of([]))),
      ciclos: this.avaliacaoService.listarCiclos().pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.treinamentos.set(r.treinamentos);
      this.ciclos.set(r.ciclos);
      this.carregando.set(false);
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected readonly treinamentosFiltrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    return this.treinamentos()
      .filter((t) => (!q || t.nome.toLowerCase().includes(q)) && (!this.soObrigatorios() || t.obrigatorio))
      .sort((a, b) => Number(b.obrigatorio) - Number(a.obrigatorio) || a.nome.localeCompare(b.nome));
  });

  protected readonly ciclosOrdenados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    return this.ciclos()
      .filter((c) => !q || c.nome.toLowerCase().includes(q))
      .sort((a, b) => b.dataInicio.localeCompare(a.dataInicio));
  });

  protected readonly resumo = computed(() => {
    const status = this.ciclos().map((c) => this.statusDoCiclo(c).rotulo);
    return {
      treinamentos: this.treinamentos().length,
      obrigatorios: this.treinamentos().filter((t) => t.obrigatorio).length,
      comValidade: this.treinamentos().filter((t) => t.validadeMeses).length,
      emAndamento: status.filter((s) => s === 'Em andamento').length,
      agendados: status.filter((s) => s === 'Agendado').length,
      encerrados: status.filter((s) => s === 'Encerrado').length,
    };
  });

  protected statusDoCiclo(c: CicloAvaliacaoResponseDto): StatusCiclo {
    const hoje = hojeIso();
    if (hoje < c.dataInicio) return { rotulo: 'Agendado', classe: 'info' };
    if (hoje > c.dataFim) return { rotulo: 'Encerrado', classe: 'muted' };
    return { rotulo: 'Em andamento', classe: 'ok' };
  }

  protected duracaoDias(c: CicloAvaliacaoResponseDto): number {
    return Math.round((new Date(c.dataFim + 'T12:00:00').getTime() - new Date(c.dataInicio + 'T12:00:00').getTime()) / 864e5) + 1;
  }

  protected contagem(aba: Aba): number {
    return aba === 'treinamentos' ? this.treinamentos().length : this.ciclos().length;
  }

  protected selecionarAba(aba: Aba): void {
    this.aba.set(aba);
    this.busca.set('');
  }

  protected navegarAbas(event: KeyboardEvent): void {
    const proxima = proximaAba(event, this.abas.map((a) => a.id), this.aba());
    if (proxima) this.selecionarAba(proxima);
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirTreinamento(): void {
    this.treinamentoForm.reset({ nome: '', cargaHoraria: '', validadeMeses: '', obrigatorio: false });
    this.abrir({ tipo: 'treinamento' });
  }

  protected abrirCiclo(): void {
    this.cicloForm.reset({ nome: '', dataInicio: hojeIso(), dataFim: '' });
    this.abrir({ tipo: 'ciclo' });
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
    if (g?.tipo === 'treinamento') this.salvarTreinamento();
    if (g?.tipo === 'ciclo') this.salvarCiclo();
  }

  private salvarTreinamento(): void {
    const v = this.treinamentoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome do treinamento.';
    if (v.cargaHoraria && !(Number(v.cargaHoraria) > 0)) erros['cargaHoraria'] = 'A carga horária precisa ser maior que zero.';
    if (v.validadeMeses && !(Number(v.validadeMeses) > 0)) erros['validadeMeses'] = 'A validade precisa ser maior que zero.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.treinamentoService.criar({
        nome: v.nome.trim(),
        cargaHoraria: v.cargaHoraria ? Number(v.cargaHoraria) : undefined,
        validadeMeses: v.validadeMeses ? Number(v.validadeMeses) : undefined,
        obrigatorio: v.obrigatorio,
      }),
      'Treinamento cadastrado',
      v.nome.trim(),
    );
  }

  private salvarCiclo(): void {
    const v = this.cicloForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome do ciclo.';
    if (!v.dataInicio) erros['dataInicio'] = 'Informe o início.';
    if (!v.dataFim) erros['dataFim'] = 'Informe o fim.';
    else if (v.dataInicio && v.dataFim < v.dataInicio) erros['dataFim'] = 'O fim não pode ser antes do início.';
    if (!this.validar(erros)) return;
    this.enviar(
      this.avaliacaoService.criarCiclo({ nome: v.nome.trim(), dataInicio: v.dataInicio, dataFim: v.dataFim }),
      'Ciclo cadastrado',
      `${v.nome.trim()} · ${dataBr(v.dataInicio)} a ${dataBr(v.dataFim)}`,
    );
  }

  private enviar(req: Observable<SuccessResponseDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }
}
