import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  ContagemProducaoDto,
  EsperaAtendimentoDto,
  GranularidadeSerie,
  OcorrenciaEscalaDto,
  OcupacaoAgendaDto,
  ProducaoAssistencialDto,
  ProducaoUnidadeDto,
  SerieProducaoDto,
  SituacaoAgoraDto,
} from '../../core/models/producao';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { ProducaoService } from '../../core/services/producao';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';

type Preset = 'HOJE' | 'SEMANA' | 'MES' | 'ANO' | 'PERIODO';

/**
 * Sala de Situação, pelo protótipo Painel.html: a produção assistencial da rede ou da unidade no período (BI1,
 * ADR-0143) e a situação operacional (BI2, ADR-0144) — alertas, fila agora, tempo de espera, ocupação das agendas e
 * ocorrências da escala, atualizados a cada minuto. Só números. Vacinação e programas na rede ficam "Em breve".
 */
@Component({
  selector: 'app-sala-de-situacao',
  imports: [RouterLink],
  templateUrl: './sala-de-situacao.html',
  styleUrl: './sala-de-situacao.css',
})
export class SalaDeSituacao {
  private readonly service = inject(ProducaoService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly presets: { id: Preset; rotulo: string }[] = [
    { id: 'HOJE', rotulo: 'Hoje' },
    { id: 'SEMANA', rotulo: 'Semana' },
    { id: 'MES', rotulo: 'Mês' },
    { id: 'ANO', rotulo: 'Ano' },
  ];

  protected readonly preset = signal<Preset>('MES');
  protected readonly de = signal(inicioDoMes());
  protected readonly ate = signal(hojeIso());
  protected readonly unidadeId = signal('');
  protected readonly granularidade = signal<GranularidadeSerie>('DIA');

  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly producao = signal<ProducaoAssistencialDto | null | undefined>(undefined);
  protected readonly serie = signal<SerieProducaoDto | null>(null);
  protected readonly ranking = signal<ProducaoUnidadeDto[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly exportando = signal(false);
  protected readonly situacao = signal<SituacaoAgoraDto | null>(null);
  protected readonly espera = signal<EsperaAtendimentoDto | null>(null);
  protected readonly ocupacao = signal<OcupacaoAgendaDto | null>(null);
  protected readonly escala = signal<OcorrenciaEscalaDto[]>([]);

  protected readonly largura = 640;
  protected readonly altura = 160;

  protected readonly maxSerie = computed(() => Math.max(1, ...(this.serie()?.pontos ?? []).map((p) => p.atendimentos)));
  protected readonly maxHora = computed(() => Math.max(1, ...(this.producao()?.porHora ?? [])));
  protected readonly pico = computed(() => {
    const h = this.producao()?.porHora ?? [];
    const max = Math.max(0, ...h);
    return max === 0 ? null : h.indexOf(max);
  });
  protected readonly maxRanking = computed(() => Math.max(1, ...this.ranking().map((r) => r.atendimentos)));

  constructor() {
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidades.set(l));
    effect(() => {
      const filtro = { de: this.de(), ate: this.ate(), unidadeId: this.unidadeId() };
      untracked(() => this.carregar(filtro));
    });
    // Situação ao vivo (BI2): carrega com a unidade e de novo a cada minuto.
    effect(() => {
      const u = this.unidadeId();
      untracked(() => this.carregarSituacao(u));
    });
    const relogio = setInterval(() => this.carregarSituacao(this.unidadeId()), 60_000);
    this.destroyRef.onDestroy(() => clearInterval(relogio));
    effect(() => {
      const g = this.granularidade();
      const u = this.unidadeId();
      untracked(() =>
        this.service.serie(g, u || undefined).pipe(catchError(() => of(null))).subscribe((s) => this.serie.set(s)),
      );
    });
  }

  protected escolher(p: Preset): void {
    const hoje = new Date();
    this.preset.set(p);
    this.ate.set(hojeIso());
    if (p === 'HOJE') {
      this.de.set(hojeIso());
    } else if (p === 'SEMANA') {
      this.de.set(iso(new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() - 6)));
    } else if (p === 'MES') {
      this.de.set(inicioDoMes());
    } else if (p === 'ANO') {
      this.de.set(`${hoje.getFullYear()}-01-01`);
    }
  }

  protected periodo(campo: 'de' | 'ate', valor: string): void {
    if (!valor) {
      return;
    }
    this.preset.set('PERIODO');
    (campo === 'de' ? this.de : this.ate).set(valor);
  }

  protected exportar(): void {
    this.exportando.set(true);
    this.service.exportar({ de: this.de(), ate: this.ate(), unidadeId: this.unidadeId() }).subscribe({
      next: (r) => {
        this.exportando.set(false);
        const nome = /filename="([^"]+)"/.exec(r.headers.get('Content-Disposition') ?? '')?.[1] ?? 'producao-assistencial.csv';
        const url = URL.createObjectURL(r.body ?? new Blob());
        const link = document.createElement('a');
        link.href = url;
        link.download = nome;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.exportando.set(false);
        this.erro.set('Não foi possível exportar a produção.');
      },
    });
  }

  // ── Apresentação ──

  /** Variação em %, com sinal; nula quando a base é zero. */
  protected delta(atual: number, anterior: number): number | null {
    return anterior === 0 ? null : Math.round(((atual - anterior) * 1000) / anterior) / 10;
  }

  protected textoDelta(v: number | null | undefined): string {
    if (v == null) {
      return 'sem base';
    }
    return `${v > 0 ? '+' : v < 0 ? '−' : '±'}${Math.abs(v).toLocaleString('pt-BR')}%`;
  }

  /** "up" é bom, "down" é ruim: para faltas e absenteísmo, subir é ruim. */
  protected classeDelta(v: number | null | undefined, subirEhBom = true): string {
    if (v == null || v === 0) {
      return 'ss-delta ss-delta--neutral';
    }
    return 'ss-delta ' + ((v > 0) === subirEhBom ? 'ss-delta--up' : 'ss-delta--down');
  }

  protected numero(v: number | null | undefined): string {
    return v == null ? '—' : Number(v).toLocaleString('pt-BR');
  }

  protected maior(lista: ContagemProducaoDto[]): number {
    return Math.max(1, ...lista.map((c) => c.quantidade));
  }

  protected largura100(v: number, max: number): string {
    return `${Math.round((v / max) * 100)}%`;
  }

  protected topEspecialidades(lista: ContagemProducaoDto[]): ContagemProducaoDto[] {
    return lista.slice(0, 8);
  }

  protected barraX(k: number, n: number): number {
    return 8 + (k * (this.largura - 16)) / n;
  }

  protected barraLargura(n: number): number {
    return Math.max(2, (this.largura - 16) / n - 3);
  }

  protected barraAltura(v: number, max: number): number {
    return Math.max(v > 0 ? 2 : 0, (v / max) * (this.altura - 24));
  }

  protected rotuloPonto(periodo: string, g: GranularidadeSerie): string {
    if (g === 'MES') {
      const [a, m] = periodo.split('-');
      return `${MESES[Number(m) - 1]}/${a.slice(2)}`;
    }
    return periodo.slice(8, 10);
  }

  protected mostrarRotulo(k: number, n: number): boolean {
    return n <= 12 || k % Math.ceil(n / 10) === 0 || k === n - 1;
  }

  protected minutos(v: number | null | undefined): string {
    if (v == null) {
      return '—';
    }
    const m = Math.round(Number(v));
    return m < 60 ? `${m} min` : `${Math.floor(m / 60)}h${String(m % 60).padStart(2, '0')}`;
  }

  protected hora(isoDataHora: string): string {
    return isoDataHora.slice(11, 16);
  }

  protected diaHora(isoDataHora: string): string {
    const [a, m, d] = isoDataHora.slice(0, 10).split('-');
    return `${d}/${m} ${isoDataHora.slice(11, 16)}`;
  }

  protected rotuloDaRota(tipo: string): string {
    return tipo === 'ESCALA' ? 'Ver escala' : tipo === 'AGENDA' ? 'Ver agenda' : 'Ver atendimentos';
  }

  protected classeOcupacao(p: number | null | undefined): string {
    return p == null ? 'ss-bar__fill' : p >= 95 ? 'ss-bar__fill ss-bar__fill--alert' : p >= 80 ? 'ss-bar__fill ss-bar__fill--warn' : 'ss-bar__fill';
  }

  protected data(isoData: string): string {
    const [a, m, d] = isoData.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  private carregar(filtro: { de: string; ate: string; unidadeId: string }): void {
    this.producao.set(undefined);
    this.erro.set(null);
    this.service.producao(filtro).subscribe({
      next: (p) => this.producao.set(p),
      error: (e: HttpErrorResponse) => {
        this.producao.set(null);
        this.erro.set(
          e.status === 403
            ? 'Seu acesso não inclui os indicadores desta abrangência (INDICADOR.CONSULTAR).'
            : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar a produção.'),
        );
      },
    });
    this.service.unidades(filtro).pipe(catchError(() => of([]))).subscribe((l) => this.ranking.set(l.slice(0, 10)));
    this.service.espera(filtro).pipe(catchError(() => of(null))).subscribe((e) => this.espera.set(e));
  }

  private carregarSituacao(unidadeId: string): void {
    const u = unidadeId || undefined;
    this.service.situacao(u).pipe(catchError(() => of(null))).subscribe((s) => this.situacao.set(s));
    this.service.ocupacao(u).pipe(catchError(() => of(null))).subscribe((o) => this.ocupacao.set(o));
    this.service.escala(u).pipe(catchError(() => of([]))).subscribe((l) => this.escala.set(l));
  }
}

const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

function iso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function hojeIso(): string {
  return iso(new Date());
}

function inicioDoMes(): string {
  return hojeIso().slice(0, 8) + '01';
}
