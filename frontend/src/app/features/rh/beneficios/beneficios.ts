import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { CusteioBeneficio, TipoBeneficioResponseDto } from '../../../core/models/tipo-beneficio';
import { ValorBeneficioResponseDto } from '../../../core/models/valor-beneficio';
import { AuthService } from '../../../core/services/auth';
import { TipoBeneficioService } from '../../../core/services/tipo-beneficio';
import { ValorBeneficioService } from '../../../core/services/valor-beneficio';
import { Drawer } from '../../../shared/drawer/drawer';
import { dataBr, hojeIso, moeda } from '../../../shared/formato';

type Gaveta = { tipo: 'novo' } | { tipo: 'valores'; beneficio: TipoBeneficioResponseDto };

const CUSTEIOS: { valor: CusteioBeneficio; rotulo: string; classe: string; ajuda: string }[] = [
  { valor: 'EMPRESA', rotulo: 'Empresa', classe: 'ok', ajuda: 'a rede paga tudo' },
  { valor: 'COMPARTILHADO', rotulo: 'Compartilhado', classe: 'info', ajuda: 'rede e profissional dividem' },
  { valor: 'PROFISSIONAL', rotulo: 'Profissional', classe: 'warn', ajuda: 'o profissional paga, a rede só intermedia' },
];

/**
 * Benefícios (ADR-0073): os tipos de benefício e, em gaveta, o valor vigente e o histórico de reajustes de cada
 * um. Junta as antigas telas Tipos de benefício e Valores do benefício; as rotas antigas redirecionam para cá.
 * A adesão de cada profissional continua na aba Benefícios do perfil.
 */
@Component({
  selector: 'app-beneficios',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './beneficios.html',
})
export class Beneficios {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly tipoService = inject(TipoBeneficioService);
  private readonly valorService = inject(ValorBeneficioService);
  private readonly route = inject(ActivatedRoute);

  protected readonly moeda = moeda;
  protected readonly dataBr = dataBr;
  protected readonly custeios = CUSTEIOS;

  protected readonly tipos = signal<TipoBeneficioResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly busca = signal('');
  protected readonly filtroCusteio = signal<CusteioBeneficio | ''>('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly historico = signal<ValorBeneficioResponseDto[] | null>(null);
  protected readonly vigente = signal<ValorBeneficioResponseDto | null>(null);

  protected readonly tipoForm = this.fb.nonNullable.group({ nome: [''], custeio: ['' as CusteioBeneficio | ''] });
  protected readonly valorForm = this.fb.nonNullable.group({ valor: [''], dataVigencia: [hojeIso()], motivo: [''] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    const tipoId = this.route.snapshot.queryParamMap.get('tipo');
    this.carregar(() => {
      const tipo = tipoId ? this.tipos().find((t) => t.uuid === tipoId) : null;
      if (tipo) this.abrirValores(tipo);
    });
  }

  private carregar(depois?: () => void): void {
    this.carregando.set(true);
    this.tipoService.listar().pipe(catchError(() => of([]))).subscribe((t) => {
      this.tipos.set(t);
      this.carregando.set(false);
      depois?.();
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected readonly filtrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const c = this.filtroCusteio();
    return this.tipos()
      .filter((t) => (!q || t.nome.toLowerCase().includes(q)) && (!c || t.custeio === c))
      .sort((a, b) => a.nome.localeCompare(b.nome));
  });

  protected contagemCusteio(c: CusteioBeneficio): number {
    return this.tipos().filter((t) => t.custeio === c).length;
  }

  protected custeio(c: CusteioBeneficio) {
    return CUSTEIOS.find((x) => x.valor === c)!;
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

  protected abrirNovo(): void {
    this.tipoForm.reset({ nome: '', custeio: '' });
    this.abrir({ tipo: 'novo' });
  }

  protected abrirValores(beneficio: TipoBeneficioResponseDto): void {
    this.valorForm.reset({ valor: '', dataVigencia: hojeIso(), motivo: '' });
    this.historico.set(null);
    this.vigente.set(null);
    this.abrir({ tipo: 'valores', beneficio });
    this.carregarValores(beneficio.uuid);
  }

  private carregarValores(tipoId: string): void {
    forkJoin({
      historico: this.valorService.listarPorTipo(tipoId).pipe(catchError(() => of([]))),
      vigente: this.valorService.buscarVigente(tipoId).pipe(catchError(() => of(null))),
    }).subscribe((r) => {
      this.historico.set([...r.historico].sort((a, b) => b.dataVigencia.localeCompare(a.dataVigencia)));
      this.vigente.set(r.vigente);
    });
  }

  protected teclaNaLinha(event: KeyboardEvent, t: TipoBeneficioResponseDto): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.abrirValores(t);
    }
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
    if (g?.tipo === 'novo') this.salvarTipo();
    if (g?.tipo === 'valores') this.salvarValor(g.beneficio);
  }

  private salvarTipo(): void {
    const v = this.tipoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome do benefício.';
    if (!v.custeio) erros['custeio'] = 'Escolha quem custeia.';
    if (!this.validar(erros)) return;
    this.submitting.set(true);
    this.tipoService.criar({ nome: v.nome.trim(), custeio: v.custeio as CusteioBeneficio }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.mostrarToast('Benefício cadastrado', `${v.nome.trim()} · agora registre o valor`);
        this.carregar(() => {
          const novo = this.tipos().find((t) => t.nome === v.nome.trim());
          if (novo) this.abrirValores(novo);
        });
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private salvarValor(beneficio: TipoBeneficioResponseDto): void {
    const v = this.valorForm.getRawValue();
    const erros: Record<string, string> = {};
    const valor = Number(v.valor.replace(/\./g, '').replace(',', '.'));
    if (!v.valor || !(valor >= 0) || Number.isNaN(valor)) erros['valor'] = 'Informe o valor.';
    if (!v.dataVigencia) erros['dataVigencia'] = 'Informe a data de vigência.';
    if (!this.validar(erros)) return;
    this.submitting.set(true);
    this.valorService.criar({ tipoBeneficioId: beneficio.uuid, valor, dataVigencia: v.dataVigencia, motivo: v.motivo.trim() || undefined }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.mostrarToast('Novo valor registrado', `${beneficio.nome} · ${moeda(valor)} a partir de ${dataBr(v.dataVigencia)}`);
        this.valorForm.reset({ valor: '', dataVigencia: hojeIso(), motivo: '' });
        this.carregarValores(beneficio.uuid);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private falhou(e: HttpErrorResponse): void {
    this.submitting.set(false);
    this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}
