import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../core/models/profissional';
import { FiltroRelatorio, RelatorioCatalogoDto, RelatorioDto } from '../../core/models/relatorio';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { RelatorioService } from '../../core/services/relatorio';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { hojeIso } from '../../shared/formato';

const TODOS = 'Todos';

/**
 * Relatórios (BI4, ADR-0167), pelo protótipo Relatorios.html: o catálogo à esquerda e a prévia à direita, com o período e a
 * unidade da Sala de Situação. Os números vêm dos domínios; a exportação entra na trilha e o PDF é a impressão do navegador.
 */
@Component({
  selector: 'app-relatorios',
  templateUrl: './relatorios.html',
  styleUrl: './relatorios.css',
})
export class Relatorios {
  private readonly service = inject(RelatorioService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly catalogo = signal<RelatorioCatalogoDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly categoria = signal(TODOS);
  protected readonly busca = signal('');
  protected readonly selecionado = signal<string | null>(null);
  protected readonly de = signal(hojeIso().slice(0, 8) + '01');
  protected readonly ate = signal(hojeIso());
  protected readonly unidadeId = signal('');
  protected readonly relatorio = signal<RelatorioDto | null>(null);
  protected readonly carregando = signal(false);
  protected readonly exportando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly toast = signal<string | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly categorias = computed(() => {
    const lista = this.catalogo();
    return [{ nome: TODOS, total: lista.length }, ...[...new Set(lista.map((r) => r.categoria))].map((nome) => ({ nome, total: lista.filter((r) => r.categoria === nome).length }))];
  });

  protected readonly filtrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const c = this.categoria();
    return this.catalogo().filter((r) => (c === TODOS || r.categoria === c) && (!q || `${r.nome} ${r.descricao}`.toLowerCase().includes(q)));
  });

  constructor() {
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidades.set(l));
    this.service.catalogo().pipe(catchError(() => of([]))).subscribe((l) => {
      this.catalogo.set(l);
      this.selecionado.set(l[0]?.codigo ?? null);
    });
    effect(() => {
      const codigo = this.selecionado();
      const filtro = this.filtro();
      if (codigo) this.gerar(codigo, filtro);
    });
  }

  private readonly filtro = computed<FiltroRelatorio>(() => ({ de: this.de(), ate: this.ate(), unidadeId: this.unidadeId() }));

  private gerar(codigo: string, filtro: FiltroRelatorio): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.service.gerar(codigo, filtro).subscribe({
      next: (r) => {
        this.relatorio.set(r);
        this.carregando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.relatorio.set(null);
        this.carregando.set(false);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível gerar o relatório.');
      },
    });
  }

  protected dataBr(iso: string): string {
    return iso.split('-').reverse().join('/');
  }

  protected exportar(): void {
    const codigo = this.selecionado();
    if (!codigo || this.exportando()) return;
    this.exportando.set(true);
    this.service.exportar(codigo, this.filtro()).subscribe({
      next: (r) => {
        this.exportando.set(false);
        const nome = /filename="([^"]+)"/.exec(r.headers.get('Content-Disposition') ?? '')?.[1] ?? 'relatorio.csv';
        const url = URL.createObjectURL(r.body ?? new Blob());
        const link = document.createElement('a');
        link.href = url;
        link.download = nome;
        link.click();
        URL.revokeObjectURL(url);
        this.toast.set(`Relatório exportado: ${nome}`);
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
      },
      error: () => {
        this.exportando.set(false);
        this.erro.set('Não foi possível exportar o relatório.');
      },
    });
  }

  protected imprimir(): void {
    window.print();
  }
}
