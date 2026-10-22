import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { ExameLaboratorialResponseDto } from '../../../core/models/laboratorio';
import { ErrorResponseDto } from '../../../core/models/profissional';
import {
  IndicadorApuradoDto,
  IndicadorProgramatico,
  MetaProgramaticaDto,
  MetaProgramaticaRequestDto,
  ParticipacaoProgramaDto,
  SITUACOES_META,
  SerieIndicadorDto,
} from '../../../core/models/programa';
import { LaboratorioService } from '../../../core/services/laboratorio';
import { ProgramaService } from '../../../core/services/programa';

/** Baixa a resposta em arquivo, com o nome do Content-Disposition. */
export function baixarArquivo(corpo: Blob | null, contentDisposition: string | null, padrao: string): void {
  const nome = /filename="([^"]+)"/.exec(contentDisposition ?? '')?.[1] ?? padrao;
  const url = URL.createObjectURL(corpo ?? new Blob());
  const link = document.createElement('a');
  link.href = url;
  link.download = nome;
  link.click();
  URL.revokeObjectURL(url);
}

/**
 * Metas e indicadores do programa (ADR-0141): os indicadores calculados no período, na rede ou numa unidade, com a meta
 * vigente e a situação; a evolução de 12 meses de um indicador; o cadastro de metas (PROGRAMA.GERENCIAR) e o relatório
 * em CSV. Só números, nenhum paciente.
 */
@Component({
  selector: 'app-indicadores-do-programa',
  template: `
    <div class="ind-filtros" style="display: flex; gap: 8px; flex-wrap: wrap; align-items: flex-end;">
      <div class="dw-field"><label for="f-ind-de">De</label><input id="f-ind-de" type="date" [value]="de()" (change)="de.set($any($event.target).value)" /></div>
      <div class="dw-field"><label for="f-ind-ate">Até</label><input id="f-ind-ate" type="date" [value]="ate()" (change)="ate.set($any($event.target).value)" /></div>
      <div class="dw-field">
        <label for="f-ind-unidade">Onde</label>
        <select id="f-ind-unidade" [value]="unidadeId()" (change)="unidadeId.set($any($event.target).value)">
          <option value="">Toda a rede</option>
          @for (u of unidadesLista(); track u.id) { <option [value]="u.id">{{ u.nome }}</option> }
        </select>
      </div>
      <button type="button" class="btn btn--ghost btn--sm" (click)="exportar()" [disabled]="exportando()">Exportar CSV</button>
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (mensagem()) { <div class="box box--info" role="status"><div>{{ mensagem() }}</div></div> }

    @if (indicadores() === undefined) {
      <p class="cell-sub">Calculando os indicadores…</p>
    } @else if (indicadores() === null) {
      <p class="cell-sub">{{ semAcesso() ? 'Os indicadores deste programa sensível são de quem acompanha os pacientes.' : 'Não foi possível calcular os indicadores.' }}</p>
    } @else {
      <ul class="eq-prof" aria-label="Indicadores do programa">
        @for (i of indicadores(); track i.indicador) {
          <li class="mo-row">
            <span>
              <span class="cell-main">{{ i.nome }}
                @if (i.situacao && i.aplicavel) { <span [class]="'tag tag--' + situacoes[i.situacao].classe">{{ situacoes[i.situacao].rotulo }}</span> }
              </span>
              <span class="cell-sub">{{ i.aplicavel ? i.descricao : i.motivo }}</span>
            </span>
            <span class="row-actions">
              @if (i.aplicavel) {
                <span class="mono" [attr.aria-label]="'Valor: ' + i.nome"><b>{{ formatar(i.valor, i.tipo === 'PERCENTUAL') }}</b>
                  @if (i.denominador != null) { <small class="cell-sub" style="display: inline;"> ({{ i.numerador }}/{{ i.denominador }})</small> }
                  @if (i.alvo != null) { <small class="cell-sub" style="display: inline;"> · meta {{ formatar(i.alvo, i.tipo === 'PERCENTUAL') }}</small> }
                </span>
                <button type="button" class="link-btn" (click)="verSerie(i.indicador)" [attr.aria-label]="'Evolução: ' + i.nome">Evolução</button>
              } @else {
                <span class="cell-sub">—</span>
              }
            </span>
          </li>
        } @empty { <li class="cell-sub">Nenhum indicador para este programa.</li> }
      </ul>
    }

    @if (serie(); as s) {
      <div class="info-block" style="margin-top: var(--s-4);" aria-label="Evolução do indicador">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h4 class="mo-grupo">{{ s.nome }} · últimos {{ s.pontos.length }} meses</h4>
          <button type="button" class="link-btn" (click)="serie.set(null)">Fechar</button>
        </div>
        <svg [attr.viewBox]="'0 0 ' + largura + ' ' + (altura + 24)" width="100%" role="img" [attr.aria-label]="'Gráfico: ' + s.nome">
          <line x1="0" [attr.y1]="altura" [attr.x2]="largura" [attr.y2]="altura" stroke="var(--ink-200)" />
          @if (alvoNaSerie() != null) {
            <line x1="0" [attr.y1]="y(alvoNaSerie()!)" [attr.x2]="largura" [attr.y2]="y(alvoNaSerie()!)" stroke="var(--warn-500, #d97706)" stroke-dasharray="4 4" />
          }
          <polyline fill="none" stroke="var(--brand-600, #2563eb)" stroke-width="2" [attr.points]="linha()" />
          @for (p of s.pontos; track p.mes; let k = $index) {
            @if (p.valor != null) { <circle [attr.cx]="x(k)" [attr.cy]="y(p.valor)" r="3" fill="var(--brand-600, #2563eb)"><title>{{ p.mes }}: {{ p.valor }}</title></circle> }
            <text [attr.x]="x(k)" [attr.y]="altura + 16" font-size="10" text-anchor="middle" fill="var(--ink-500)">{{ p.mes.slice(5) }}</text>
          }
        </svg>
        <table class="sr-only" aria-label="Valores mensais"><tbody>
          @for (p of s.pontos; track p.mes) { <tr><td>{{ p.mes }}</td><td>{{ p.valor ?? '—' }}</td></tr> }
        </tbody></table>
      </div>
    }

    <h4 class="mo-grupo" style="margin-top: var(--s-4);">Metas</h4>
    <ul class="eq-prof" aria-label="Metas do programa">
      @for (m of metas(); track m.uuid) {
        <li class="mo-row">
          <span>
            <span class="cell-main">{{ m.indicadorNome }} · {{ m.alvo }}</span>
            <span class="cell-sub">{{ m.unidadeNome || 'Toda a rede' }} · {{ data(m.inicio) }} a {{ data(m.fim) }}@if (m.exameNome) { · {{ m.exameNome }} ({{ m.limiteMinimo ?? '—' }} a {{ m.limiteMaximo ?? '—' }}) }</span>
          </span>
          @if (podeGerenciar()) {
            <span class="row-actions">
              <button type="button" class="link-btn" (click)="editar(m)" [attr.aria-label]="'Editar meta: ' + m.indicadorNome">Editar</button>
              <button type="button" class="link-btn" (click)="excluir(m)" [attr.aria-label]="'Excluir meta: ' + m.indicadorNome">Excluir</button>
            </span>
          }
        </li>
      } @empty { <li class="cell-sub">Nenhuma meta cadastrada.</li> }
    </ul>

    @if (podeGerenciar()) {
      <fieldset class="dw-field" style="margin-top: var(--s-3);">
        <legend>{{ editando() ? 'Ajustar meta' : 'Nova meta' }}</legend>
        <div class="dw-row">
          <div class="dw-field">
            <label for="f-meta-indicador">Indicador</label>
            <select id="f-meta-indicador" [value]="fIndicador()" (change)="fIndicador.set($any($event.target).value)" [disabled]="!!editando()">
              <option value="">Escolha</option>
              @for (i of indicadoresDeMeta(); track i.indicador) { <option [value]="i.indicador">{{ i.nome }}</option> }
            </select>
          </div>
          <div class="dw-field">
            <label for="f-meta-unidade">Onde</label>
            <select id="f-meta-unidade" [value]="fUnidade()" (change)="fUnidade.set($any($event.target).value)" [disabled]="!!editando()">
              <option value="">Toda a rede</option>
              @for (u of unidadesLista(); track u.id) { <option [value]="u.id">{{ u.nome }}</option> }
            </select>
          </div>
          <div class="dw-field"><label for="f-meta-alvo">Alvo</label><input id="f-meta-alvo" type="number" min="0" step="0.01" [value]="fAlvo()" (input)="fAlvo.set($any($event.target).value)" /></div>
        </div>
        <div class="dw-row">
          <div class="dw-field"><label for="f-meta-inicio">Início</label><input id="f-meta-inicio" type="date" [value]="fInicio()" (change)="fInicio.set($any($event.target).value)" /></div>
          <div class="dw-field"><label for="f-meta-fim">Fim</label><input id="f-meta-fim" type="date" [value]="fFim()" (change)="fFim.set($any($event.target).value)" /></div>
        </div>
        @if (fIndicador() === 'EXAME_NO_ALVO') {
          <div class="dw-row">
            <div class="dw-field">
              <label for="f-meta-exame">Exame</label>
              <select id="f-meta-exame" [value]="fExame()" (change)="fExame.set($any($event.target).value)">
                <option value="">Escolha</option>
                @for (e of exames(); track e.uuid) { <option [value]="e.uuid">{{ e.nome }}</option> }
              </select>
            </div>
            <div class="dw-field"><label for="f-meta-min">Limite mínimo<span class="opt">opcional</span></label><input id="f-meta-min" type="number" step="0.0001" [value]="fMin()" (input)="fMin.set($any($event.target).value)" /></div>
            <div class="dw-field"><label for="f-meta-max">Limite máximo<span class="opt">opcional</span></label><input id="f-meta-max" type="number" step="0.0001" [value]="fMax()" (input)="fMax.set($any($event.target).value)" /></div>
          </div>
        }
        <div class="row-actions" style="justify-content: flex-start;">
          <button type="button" class="btn btn--sm" (click)="salvarMeta()">{{ editando() ? 'Salvar meta' : 'Cadastrar meta' }}</button>
          @if (editando()) { <button type="button" class="btn btn--ghost btn--sm" (click)="limparForm()">Cancelar</button> }
        </div>
      </fieldset>
    }
  `,
})
export class IndicadoresDoPrograma {
  private readonly service = inject(ProgramaService);
  private readonly laboratorio = inject(LaboratorioService);

  readonly programaId = input.required<string>();
  readonly participacoes = input<ParticipacaoProgramaDto[]>([]);
  readonly podeGerenciar = input(false);

  protected readonly situacoes = SITUACOES_META;
  protected readonly largura = 600;
  protected readonly altura = 140;

  protected readonly de = signal(primeiroDoMes());
  protected readonly ate = signal(hojeIso());
  protected readonly unidadeId = signal('');
  protected readonly indicadores = signal<IndicadorApuradoDto[] | null | undefined>(undefined);
  protected readonly semAcesso = signal(false);
  protected readonly serie = signal<SerieIndicadorDto | null>(null);
  protected readonly metas = signal<MetaProgramaticaDto[]>([]);
  protected readonly exames = signal<ExameLaboratorialResponseDto[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly mensagem = signal<string | null>(null);
  protected readonly exportando = signal(false);

  protected readonly editando = signal<MetaProgramaticaDto | null>(null);
  protected readonly fIndicador = signal('');
  protected readonly fUnidade = signal('');
  protected readonly fAlvo = signal('');
  protected readonly fInicio = signal(primeiroDoMes());
  protected readonly fFim = signal(hojeIso());
  protected readonly fExame = signal('');
  protected readonly fMin = signal('');
  protected readonly fMax = signal('');

  /** Uma unidade por participação vigente (a mesma unidade pode vir com equipes diferentes). */
  protected readonly unidadesLista = computed(() => {
    const vistos = new Map<string, string>();
    for (const p of this.participacoes()) {
      vistos.set(p.unidadeId, p.unidadeNome);
    }
    return [...vistos].map(([id, nome]) => ({ id, nome }));
  });

  /** A meta vale para os indicadores do programa; o "exame no alvo" aparece mesmo sem meta (é ela que o torna aplicável). */
  protected readonly indicadoresDeMeta = computed(() =>
    (this.indicadores() ?? []).filter((i) => i.aplicavel || i.indicador === 'EXAME_NO_ALVO'),
  );

  protected readonly alvoNaSerie = computed(() => {
    const s = this.serie();
    return s ? (this.indicadores() ?? []).find((i) => i.indicador === s.indicador)?.alvo ?? null : null;
  });

  private readonly maximo = computed(() => {
    const valores = (this.serie()?.pontos ?? []).map((p) => p.valor ?? 0);
    const alvo = this.alvoNaSerie() ?? 0;
    const percentual = (this.indicadores() ?? []).find((i) => i.indicador === this.serie()?.indicador)?.tipo === 'PERCENTUAL';
    return percentual ? 100 : Math.max(1, alvo, ...valores);
  });

  protected readonly linha = computed(() =>
    (this.serie()?.pontos ?? [])
      .map((p, k) => (p.valor == null ? null : `${this.x(k)},${this.y(p.valor)}`))
      .filter((p) => p)
      .join(' '),
  );

  constructor() {
    effect(() => {
      const programa = this.programaId();
      const filtro = { de: this.de(), ate: this.ate(), unidadeId: this.unidadeId() };
      untracked(() => this.carregar(programa, filtro));
    });
    effect(() => {
      const programa = this.programaId();
      untracked(() => {
        this.serie.set(null);
        this.limparForm();
        this.carregarMetas(programa);
      });
    });
  }

  protected x(k: number): number {
    const n = Math.max(1, (this.serie()?.pontos.length ?? 1) - 1);
    return 20 + (k * (this.largura - 40)) / n;
  }

  protected y(v: number): number {
    return this.altura - 8 - (v / this.maximo()) * (this.altura - 16);
  }

  protected formatar(v: number | null | undefined, percentual: boolean): string {
    if (v == null) {
      return '—';
    }
    return percentual ? `${Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 2 })}%` : Number(v).toLocaleString('pt-BR');
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected verSerie(indicador: IndicadorProgramatico): void {
    this.service
      .serieIndicador(this.programaId(), indicador, this.unidadeId() || undefined)
      .pipe(catchError(() => of(null)))
      .subscribe((s) => this.serie.set(s));
  }

  exportar(): void {
    this.exportando.set(true);
    this.service.indicadoresCsv(this.programaId(), { de: this.de(), ate: this.ate(), unidadeId: this.unidadeId() }).subscribe({
      next: (r) => {
        this.exportando.set(false);
        baixarArquivo(r.body, r.headers.get('Content-Disposition'), 'indicadores-programa.csv');
      },
      error: () => {
        this.exportando.set(false);
        this.erro.set('Não foi possível exportar os indicadores.');
      },
    });
  }

  protected editar(m: MetaProgramaticaDto): void {
    this.editando.set(m);
    this.fIndicador.set(m.indicador);
    this.fUnidade.set(m.unidadeId ?? '');
    this.fAlvo.set(String(m.alvo));
    this.fInicio.set(m.inicio);
    this.fFim.set(m.fim);
    this.fExame.set(m.exameId ?? '');
    this.fMin.set(m.limiteMinimo != null ? String(m.limiteMinimo) : '');
    this.fMax.set(m.limiteMaximo != null ? String(m.limiteMaximo) : '');
    this.carregarExames();
  }

  protected limparForm(): void {
    this.editando.set(null);
    this.fIndicador.set('');
    this.fUnidade.set('');
    this.fAlvo.set('');
    this.fInicio.set(primeiroDoMes());
    this.fFim.set(hojeIso());
    this.fExame.set('');
    this.fMin.set('');
    this.fMax.set('');
  }

  protected salvarMeta(): void {
    if (!this.fIndicador() || this.fAlvo() === '') {
      this.erro.set('Escolha o indicador e informe o alvo.');
      return;
    }
    const dto: MetaProgramaticaRequestDto = {
      indicador: this.fIndicador() as IndicadorProgramatico,
      unidadeId: this.fUnidade() || null,
      alvo: Number(this.fAlvo()),
      inicio: this.fInicio(),
      fim: this.fFim(),
      exameId: this.fExame() || null,
      limiteMinimo: this.fMin() === '' ? null : Number(this.fMin()),
      limiteMaximo: this.fMax() === '' ? null : Number(this.fMax()),
    };
    const m = this.editando();
    (m ? this.service.atualizarMeta(m.uuid, dto) : this.service.criarMeta(this.programaId(), dto)).subscribe({
      next: () => this.depoisDeMudar(m ? 'Meta ajustada.' : 'Meta cadastrada.'),
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected excluir(m: MetaProgramaticaDto): void {
    this.service.excluirMeta(m.uuid).subscribe({
      next: () => this.depoisDeMudar('Meta excluída.'),
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private depoisDeMudar(msg: string): void {
    this.erro.set(null);
    this.mensagem.set(msg);
    this.limparForm();
    this.carregarMetas(this.programaId());
    this.carregar(this.programaId(), { de: this.de(), ate: this.ate(), unidadeId: this.unidadeId() });
  }

  private falhou(e: HttpErrorResponse): void {
    this.mensagem.set(null);
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar a meta.');
  }

  private carregar(programa: string, filtro: { de: string; ate: string; unidadeId: string }): void {
    this.indicadores.set(undefined);
    this.semAcesso.set(false);
    this.service.indicadores(programa, filtro).subscribe({
      next: (l) => {
        this.indicadores.set(l);
        if (this.podeGerenciar() && l.some((i) => i.indicador === 'EXAME_NO_ALVO')) {
          this.carregarExames();
        }
      },
      error: (e: HttpErrorResponse) => {
        this.semAcesso.set(e.status === 403);
        this.indicadores.set(null);
        if (e.status === 400) {
          this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Período inválido.');
        }
      },
    });
  }

  private carregarMetas(programa: string): void {
    this.service.metas(programa).pipe(catchError(() => of([]))).subscribe((l) => this.metas.set(l));
  }

  private carregarExames(): void {
    if (this.exames().length) {
      return;
    }
    this.laboratorio.listarExames().pipe(catchError(() => of([]))).subscribe((l) => this.exames.set(l));
  }
}

function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function primeiroDoMes(): string {
  return hojeIso().slice(0, 8) + '01';
}
