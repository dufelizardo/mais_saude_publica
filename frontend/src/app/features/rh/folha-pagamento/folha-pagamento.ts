import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { FolhaPagamentoResponseDto } from '../../../core/models/folha-pagamento';
import { FolhaPagamentoService } from '../../../core/services/folha-pagamento';
import { moeda } from '../../../shared/formato';

/**
 * Folha de pagamento por competência (ADR-0025, visual da ADR-0073): abre no mês corrente, com indicadores da
 * competência, busca e totais. A folha de cada profissional é registrada na aba Folha do perfil.
 */
@Component({
  selector: 'app-folha-pagamento',
  imports: [RouterLink],
  templateUrl: './folha-pagamento.html',
  styleUrl: './folha-pagamento.css',
})
export class FolhaPagamento {
  private readonly folhaService = inject(FolhaPagamentoService);

  protected readonly moeda = moeda;

  /** "AAAA-MM", o formato do campo de mês; a API usa "MM/AAAA". */
  protected readonly mes = signal(mesAtual());
  protected readonly folhas = signal<FolhaPagamentoResponseDto[]>([]);
  protected readonly carregando = signal(true);
  protected readonly busca = signal('');

  protected readonly filtradas = computed(() => {
    const q = this.busca().trim().toLowerCase();
    return this.folhas()
      .filter((f) => !q || `${f.profissionalNome} ${f.profissionalMatricula}`.toLowerCase().includes(q))
      .sort((a, b) => a.profissionalNome.localeCompare(b.profissionalNome));
  });

  protected readonly totais = computed(() => {
    const soma = (campo: 'proventos' | 'descontos' | 'encargos' | 'total') => this.filtradas().reduce((t, f) => t + f[campo], 0);
    return { proventos: soma('proventos'), descontos: soma('descontos'), encargos: soma('encargos'), total: soma('total') };
  });

  protected readonly resumo = computed(() => {
    const f = this.folhas();
    const soma = (campo: 'proventos' | 'descontos' | 'encargos' | 'total') => f.reduce((t, x) => t + x[campo], 0);
    return {
      folhas: f.length,
      proventos: soma('proventos'),
      descontos: soma('descontos'),
      encargos: soma('encargos'),
      total: soma('total'),
      media: f.length ? soma('total') / f.length : 0,
    };
  });

  protected readonly rotuloMes = computed(() => {
    const [a, m] = this.mes().split('-');
    const nome = new Date(Number(a), Number(m) - 1, 1).toLocaleDateString('pt-BR', { month: 'long' });
    return `${nome.charAt(0).toUpperCase()}${nome.slice(1)} de ${a}`;
  });

  constructor() {
    this.carregar();
  }

  protected mudarMes(valor: string): void {
    if (!/^\d{4}-\d{2}$/.test(valor)) return;
    this.mes.set(valor);
    this.carregar();
  }

  protected andarMes(delta: number): void {
    const [a, m] = this.mes().split('-').map(Number);
    const d = new Date(a, m - 1 + delta, 1);
    this.mudarMes(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
  }

  /** Competência sem folha responde 404 no backend — tratada como lista vazia. */
  private carregar(): void {
    const [a, m] = this.mes().split('-');
    this.carregando.set(true);
    this.folhaService.listarPorCompetencia(`${m}/${a}`).pipe(catchError(() => of([]))).subscribe((f) => {
      this.folhas.set(f);
      this.carregando.set(false);
    });
  }
}

function mesAtual(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}
