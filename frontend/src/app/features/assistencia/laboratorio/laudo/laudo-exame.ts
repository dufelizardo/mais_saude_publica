import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  INTERPRETACOES,
  MATERIAIS,
  PedidoExameResponseDto,
  faixaReferencia,
  valorResultado,
} from '../../../../core/models/laboratorio';
import { PacienteResponseDto } from '../../../../core/models/paciente';
import { LaboratorioService } from '../../../../core/services/laboratorio';
import { PacienteService } from '../../../../core/services/paciente';
import { formatCpf } from '../../../../shared/format-mask';

/**
 * Laudo imprimível de um pedido de exames (ADR-0095): só os exames liberados, com valor, referência, interpretação,
 * quem liberou e a retificação. Página fora do AppShell, para imprimir ou salvar em PDF pelo navegador. Sem
 * assinatura digital. A leitura do pedido é a mesma do detalhe, auditada.
 */
@Component({
  selector: 'app-laudo-exame',
  templateUrl: './laudo-exame.html',
  styleUrl: './laudo-exame.css',
})
export class LaudoExame {
  private readonly route = inject(ActivatedRoute);
  private readonly laboratorioService = inject(LaboratorioService);
  private readonly pacienteService = inject(PacienteService);

  protected readonly materiais = MATERIAIS;
  protected readonly interpretacoes = INTERPRETACOES;
  protected readonly faixa = faixaReferencia;
  protected readonly valor = valorResultado;
  protected readonly formatCpf = formatCpf;

  protected readonly pedido = signal<PedidoExameResponseDto | null>(null);
  protected readonly paciente = signal<PacienteResponseDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly emitidoEm = new Date();

  protected readonly liberados = computed(() => (this.pedido()?.itens ?? []).filter((i) => i.status === 'LIBERADO' && i.resultado));
  protected readonly pendentes = computed(
    () => (this.pedido()?.itens ?? []).filter((i) => i.status !== 'LIBERADO' && i.status !== 'CANCELADO').length,
  );
  /** Laboratórios das amostras dos exames liberados. */
  protected readonly laboratorios = computed(() => {
    const p = this.pedido();
    if (!p) return [];
    const codigos = new Set(this.liberados().map((i) => i.amostraCodigo));
    return [...new Set(p.amostras.filter((a) => codigos.has(a.codigo)).map((a) => a.laboratorioNome))];
  });

  constructor() {
    const id = this.route.snapshot.paramMap.get('pedidoId') ?? '';
    this.laboratorioService.buscarPedido(id).subscribe({
      next: (p) => {
        this.pedido.set(p);
        this.pacienteService.buscarPorId(p.pacienteId).pipe(catchError(() => of(null))).subscribe((pac) => this.paciente.set(pac));
      },
      error: (e: HttpErrorResponse) =>
        this.erro.set(e.status === 403 ? 'Este pedido é de unidades fora do seu acesso.' : 'Não foi possível abrir o pedido.'),
    });
  }

  protected imprimir(): void {
    window.print();
  }

  protected fechar(): void {
    window.close();
  }

  protected instante(iso: string | null | undefined | Date): string {
    if (!iso) return '—';
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  protected coleta(codigo: string | null | undefined): string {
    const a = this.pedido()?.amostras.find((x) => x.codigo === codigo);
    return a ? this.instante(a.coletadaEm) : '—';
  }
}
