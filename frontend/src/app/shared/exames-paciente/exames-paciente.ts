import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, computed, inject, signal } from '@angular/core';
import {
  EvolucaoExameDto,
  ExameProntuarioDto,
  INTERPRETACOES,
  MATERIAIS,
  PontoEvolucaoExame,
  STATUS_ITEM,
  critico,
  faixaCritica,
  faixaReferencia,
  numeroLaboratorio,
  valorResultado,
} from '../../core/models/laboratorio';
import { LaboratorioService } from '../../core/services/laboratorio';
import { Drawer } from '../drawer/drawer';

/** Ponto do gráfico já em coordenadas do SVG. */
interface PontoGrafico {
  x: number;
  y: number;
  ponto: PontoEvolucaoExame;
  fora: boolean;
}

const LARGURA = 640;
const ALTURA = 240;
const MARGEM = { topo: 16, direita: 16, base: 32, esquerda: 48 };

/**
 * Exames laboratoriais do paciente (ADR-0095), no prontuário e no atendimento: situação de cada exame e, depois da
 * liberação, valor, referência (com a faixa que valeu, ADR-0180), interpretação e o laudo do pedido. Cancelados ficam de
 * fora. Com o paciente informado, o exame numérico com dois ou mais resultados liberados abre a evolução no tempo.
 */
@Component({
  selector: 'app-exames-paciente',
  imports: [Drawer],
  templateUrl: './exames-paciente.html',
  styleUrl: './exames-paciente.css',
})
export class ExamesPaciente {
  private readonly laboratorioService = inject(LaboratorioService);

  @Input({ required: true }) exames: ExameProntuarioDto[] = [];
  @Input() vazio = 'Nenhum exame pedido.';
  /** Sem paciente, não há o botão Evolução. */
  @Input() pacienteId: string | null | undefined = null;

  protected readonly materiais = MATERIAIS;
  protected readonly statusItem = STATUS_ITEM;
  protected readonly interpretacoes = INTERPRETACOES;
  protected readonly faixa = faixaReferencia;
  protected readonly faixaCritica = faixaCritica;
  protected readonly valor = valorResultado;
  protected readonly numero = numeroLaboratorio;
  protected readonly critico = critico;
  protected readonly largura = LARGURA;
  protected readonly altura = ALTURA;
  protected readonly margem = MARGEM;

  protected readonly evolucao = signal<EvolucaoExameDto | null>(null);
  protected readonly evolucaoAberta = signal<string | null>(null);
  protected readonly erroEvolucao = signal<string | null>(null);

  protected visiveis(): ExameProntuarioDto[] {
    return this.exames.filter((e) => e.status !== 'CANCELADO');
  }

  /** Dois ou mais resultados numéricos liberados do mesmo exame: dá para ver a evolução. */
  protected temEvolucao(e: ExameProntuarioDto): boolean {
    if (!this.pacienteId || !e.exameId || e.resultado?.valorNumerico == null) return false;
    return this.exames.filter((o) => o.exameId === e.exameId && o.status === 'LIBERADO' && o.resultado?.valorNumerico != null).length >= 2;
  }

  protected abrirEvolucao(e: ExameProntuarioDto): void {
    if (!this.pacienteId) return;
    this.evolucao.set(null);
    this.erroEvolucao.set(null);
    this.evolucaoAberta.set(e.exameNome);
    this.laboratorioService.evolucao(this.pacienteId, e.exameId).subscribe({
      next: (d) => this.evolucao.set(d),
      error: (err: HttpErrorResponse) =>
        this.erroEvolucao.set(err.status === 403 ? 'Sem acesso ao prontuário deste paciente.' : 'Não foi possível carregar a evolução.'),
    });
  }

  protected fecharEvolucao(): void {
    this.evolucaoAberta.set(null);
    this.evolucao.set(null);
  }

  /** Escala do eixo Y: inclui valores e referências, com folga. */
  private readonly escala = computed(() => {
    const d = this.evolucao();
    if (!d || !d.pontos.length) return null;
    const valores = d.pontos.flatMap((p) =>
      [p.valor, p.referenciaMinima, p.referenciaMaxima].filter((v): v is number => v != null).map(Number),
    );
    let min = Math.min(...valores);
    let max = Math.max(...valores);
    if (min === max) {
      min -= 1;
      max += 1;
    }
    const folga = (max - min) * 0.1;
    return { min: min - folga, max: max + folga };
  });

  private y(v: number): number {
    const e = this.escala()!;
    const alto = ALTURA - MARGEM.topo - MARGEM.base;
    return MARGEM.topo + alto - ((v - e.min) / (e.max - e.min)) * alto;
  }

  private x(i: number, total: number): number {
    const largo = LARGURA - MARGEM.esquerda - MARGEM.direita;
    return total === 1 ? MARGEM.esquerda + largo / 2 : MARGEM.esquerda + (i / (total - 1)) * largo;
  }

  protected readonly pontosGrafico = computed<PontoGrafico[]>(() => {
    const d = this.evolucao();
    if (!d || !this.escala()) return [];
    return d.pontos.map((p, i) => ({
      x: this.x(i, d.pontos.length),
      y: this.y(Number(p.valor)),
      ponto: p,
      fora: !!p.interpretacao && p.interpretacao !== 'NORMAL',
    }));
  });

  protected readonly linha = computed(() => this.pontosGrafico().map((p) => `${p.x},${p.y}`).join(' '));

  /** A faixa de referência sombreada, ponto a ponto (a faixa pode mudar com a idade). */
  protected readonly areaReferencia = computed(() => {
    const pts = this.pontosGrafico();
    const d = this.evolucao();
    if (!d || pts.length === 0 || d.pontos.some((p) => p.referenciaMinima == null || p.referenciaMaxima == null)) return '';
    if (pts.length === 1) {
      const p = pts[0].ponto;
      const a = MARGEM.esquerda;
      const b = LARGURA - MARGEM.direita;
      const topo = this.y(Number(p.referenciaMaxima));
      const base = this.y(Number(p.referenciaMinima));
      return `${a},${topo} ${b},${topo} ${b},${base} ${a},${base}`;
    }
    const topo = pts.map((p) => `${p.x},${this.y(Number(p.ponto.referenciaMaxima))}`);
    const base = pts.map((p) => `${p.x},${this.y(Number(p.ponto.referenciaMinima))}`).reverse();
    return [...topo, ...base].join(' ');
  });

  /** Quatro marcas no eixo Y. */
  protected readonly marcasY = computed(() => {
    const e = this.escala();
    if (!e) return [];
    return [0, 1, 2, 3].map((i) => {
      const v = e.min + ((e.max - e.min) * i) / 3;
      return { y: this.y(v), rotulo: numeroLaboratorio(Math.round(v * 100) / 100) };
    });
  });

  protected resumoGrafico(): string {
    const d = this.evolucao();
    if (!d || !d.pontos.length) return '';
    const n = d.pontos.length;
    const ultimo = d.pontos[n - 1];
    return `Gráfico da evolução de ${d.exameNome}: ${n} resultados; o mais recente, em ${this.data(ultimo.data)}, é ${numeroLaboratorio(ultimo.valor)}${d.unidadeMedida ? ' ' + d.unidadeMedida : ''}. A tabela abaixo traz todos os valores.`;
  }

  protected data(iso: string): string {
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
  }

  protected dataCurta(iso: string): string {
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${String(d.getFullYear()).slice(2)}`;
  }
}
