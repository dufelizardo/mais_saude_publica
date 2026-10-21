import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../../core/models/profissional';
import {
  CondicaoOdontologicaDto,
  FACES_DENTE,
  FaceDente,
  MarcacaoOdontogramaDto,
  OdontogramaDto,
  RegistroOdontogramaDto,
} from '../../../core/models/saude-bucal';
import { SaudeBucalService } from '../../../core/services/saude-bucal';

type Denticao = 'permanente' | 'decidua';
interface Selecao {
  dente: number;
  face: FaceDente | null;
}

/** Os quadrantes na ordem do desenho: superior direito e esquerdo, inferior direito e esquerdo (como o profissional vê). */
const ARCADAS: Record<Denticao, number[][]> = {
  permanente: [[18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28], [48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38]],
  decidua: [[55, 54, 53, 52, 51, 61, 62, 63, 64, 65], [85, 84, 83, 82, 81, 71, 72, 73, 74, 75]],
};

/**
 * Odontograma (ADR-0135): o desenho dos dentes (FDI) com as cinco faces, pintadas pela projeção dos registros na data;
 * marcar uma face ou o dente inteiro com a condição do catálogo e salvar na avaliação odontológica; ver como estava
 * numa data; e o histórico do dente, com a anulação da marcação errada.
 */
@Component({
  selector: 'app-odontograma',
  template: `
    <div class="filters" style="margin-bottom: var(--s-2); flex-wrap: wrap;">
      <div class="seg" role="group" aria-label="Denticao">
        <button type="button" [attr.aria-pressed]="denticao() === 'permanente'" (click)="denticao.set('permanente')">Permanente</button>
        <button type="button" [attr.aria-pressed]="denticao() === 'decidua'" (click)="denticao.set('decidua')">Decídua</button>
      </div>
      <label class="cell-sub" style="display: flex; align-items: center; gap: var(--s-1);">Em
        <input type="date" aria-label="Data do odontograma" [value]="data()" [max]="hoje" (change)="data.set($any($event.target).value || hoje)" />
      </label>
      @if (data() !== hoje) { <span class="tag tag--warn">Como estava em {{ dataBr(data()) }}</span> }
    </div>
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    <div class="odonto" role="group" aria-label="Odontograma" style="overflow-x: auto;">
      @for (arcada of arcadas(); track $index; let superior = $first) {
        <div style="display: flex; gap: 2px; justify-content: center; margin: var(--s-1) 0;">
          @for (d of arcada; track d) {
            <div style="display: flex; flex-direction: column; align-items: center; width: 40px;" [class.odonto--sel]="selecao()?.dente === d">
              @if (superior) { <button type="button" class="link-btn mono" style="font-size: 11px;" (click)="selecionar(d, null)" [attr.aria-label]="'Dente ' + d">{{ d }}</button> }
              <svg width="36" height="36" viewBox="0 0 36 36" role="group" [attr.aria-label]="'Faces do dente ' + d"
                [style.outline]="corDoDente(d) ? '2px solid ' + corDoDente(d) : 'none'" style="border-radius: 4px;">
                @for (f of facesDesenho(d, superior); track f.face) {
                  <polygon [attr.points]="f.pontos" [attr.fill]="corDaFace(d, f.face)" stroke="#94a3b8" stroke-width="1" tabindex="0" role="button"
                    [attr.aria-label]="'Dente ' + d + ', face ' + faces[f.face] + (rotuloDaFace(d, f.face) ? ': ' + rotuloDaFace(d, f.face) : '')"
                    (click)="selecionar(d, f.face)" (keydown.enter)="selecionar(d, f.face)" style="cursor: pointer;"
                    [attr.stroke-width]="selecao()?.dente === d && selecao()?.face === f.face ? 2.5 : 1" />
                }
              </svg>
              @if (!superior) { <button type="button" class="link-btn mono" style="font-size: 11px;" (click)="selecionar(d, null)" [attr.aria-label]="'Dente ' + d">{{ d }}</button> }
              <span class="mono" style="font-size: 10px; min-height: 12px;" [style.color]="corDoDente(d)">{{ siglaDoDente(d) }}</span>
            </div>
          }
        </div>
      }
    </div>
    <div class="legenda" aria-label="Legenda do odontograma" style="display: flex; gap: var(--s-2); flex-wrap: wrap; margin-top: var(--s-1);">
      @for (c of condicoesAtivas(); track c.uuid) {
        <span class="cell-sub"><span aria-hidden="true" [style.background]="c.cor" style="display: inline-block; width: 10px; height: 10px; border-radius: 2px;"></span> {{ c.sigla }} {{ c.nome }}</span>
      }
    </div>

    @if (selecao(); as s) {
      <div class="box" style="margin-top: var(--s-3);" aria-label="Dente selecionado">
        <strong>Dente {{ s.dente }}{{ s.face ? ' · face ' + faces[s.face] : ' · dente inteiro' }}</strong>
        @if (podeMarcar()) {
          <div class="row-actions" style="justify-content: flex-start; flex-wrap: wrap; margin-top: var(--s-2);" aria-label="Condições">
            @for (c of condicoesDaSelecao(); track c.uuid) {
              <button type="button" class="btn btn--ghost btn--sm" (click)="marcar(c)" [style.border-color]="c.cor">{{ c.nome }}</button>
            }
          </div>
        }
        <h4 class="mo-grupo">Histórico do dente {{ s.dente }}</h4>
        @if (historico().length) {
          <ul class="eq-prof" aria-label="Histórico do dente">
            @for (r of historico(); track r.uuid) {
              <li class="mo-row" [style.opacity]="r.anulado ? 0.6 : 1">
                <span>
                  <span class="cell-main">{{ r.condicaoNome }}{{ r.face ? ' · ' + faces[r.face] : ' · dente inteiro' }}@if (r.anulado) { <span class="tag tag--muted">Anulado</span> }</span>
                  <span class="cell-sub">{{ dataBr(r.data) }}@if (r.procedimentoNome) { · {{ r.procedimentoNome }} }@if (r.profissionalNome) { · {{ r.profissionalNome }} }@if (r.motivoAnulacao) { · {{ r.motivoAnulacao }} }</span>
                </span>
                @if (podeRegistrar() && !r.anulado && !r.procedimentoId) {
                  @if (anulando() === r.uuid) {
                    <span class="row-actions">
                      <input type="text" maxlength="500" placeholder="Motivo" aria-label="Motivo da anulação" [value]="motivo()" (input)="motivo.set($any($event.target).value)" />
                      <button type="button" class="btn btn--sm" (click)="anular(r)" [disabled]="!motivo().trim()">Anular</button>
                    </span>
                  } @else {
                    <button type="button" class="link-btn" (click)="anulando.set(r.uuid); motivo.set('')" [attr.aria-label]="'Anular ' + r.condicaoNome + ' de ' + dataBr(r.data)">Anular</button>
                  }
                }
              </li>
            }
          </ul>
        } @else { <p class="cell-sub">Nenhum registro neste dente.</p> }
      </div>
    }

    @if (pendentes().length) {
      <div class="box box--info" style="margin-top: var(--s-3);" aria-label="Marcações a salvar">
        <div>{{ pendentes().length }} {{ pendentes().length === 1 ? 'marcação' : 'marcações' }} a salvar na avaliação:
          @for (p of pendentes(); track $index) { <span class="tag tag--info">{{ p.dente }}{{ p.face ? ' ' + faces[p.face] : '' }} · {{ nomeDaCondicao(p.condicaoId) }}</span> }
        </div>
        <div class="row-actions" style="justify-content: flex-start; margin-top: var(--s-2);">
          <button type="button" class="btn btn--sm" (click)="salvar()" [disabled]="salvando()">{{ salvando() ? 'Salvando…' : 'Salvar marcações' }}</button>
          <button type="button" class="btn btn--ghost btn--sm" (click)="pendentes.set([])">Descartar</button>
        </div>
      </div>
    }
  `,
})
export class Odontograma {
  private readonly service = inject(SaudeBucalService);

  readonly pacienteId = input.required<string>();
  /** A avaliação odontológica vigente do atendimento, onde as marcações são registradas; sem ela, só leitura. */
  readonly avaliacaoId = input<string | null>(null);
  readonly podeRegistrar = input(false);
  /** Muda quando os registros do atendimento são recarregados (o procedimento realizado grava aqui, ADR-0136). */
  readonly recarregar = input<unknown>(null);
  readonly alterado = output<void>();

  protected readonly faces = FACES_DENTE;
  protected readonly hoje = iso(new Date());
  protected readonly denticao = signal<Denticao>('permanente');
  protected readonly data = signal(this.hoje);
  protected readonly condicoes = signal<CondicaoOdontologicaDto[]>([]);
  protected readonly odontograma = signal<OdontogramaDto | null>(null);
  protected readonly selecao = signal<Selecao | null>(null);
  protected readonly historico = signal<RegistroOdontogramaDto[]>([]);
  protected readonly pendentes = signal<MarcacaoOdontogramaDto[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly salvando = signal(false);
  protected readonly anulando = signal<string | null>(null);
  protected readonly motivo = signal('');

  protected readonly arcadas = computed(() => ARCADAS[this.denticao()]);
  protected readonly condicoesAtivas = computed(() => this.condicoes().filter((c) => c.ativa));
  /** Marcar só no odontograma de hoje, com a avaliação vigente e a permissão. */
  protected readonly podeMarcar = computed(() => this.podeRegistrar() && !!this.avaliacaoId() && this.data() === this.hoje);
  protected readonly condicoesDaSelecao = computed(() => {
    const s = this.selecao();
    return this.condicoesAtivas().filter((c) => c.alcance === 'AMBOS' || (s?.face ? c.alcance === 'FACE' : c.alcance === 'DENTE'));
  });

  constructor() {
    this.service.condicoes().pipe(catchError(() => of([]))).subscribe((c) => this.condicoes.set(c));
    effect(() => {
      const paciente = this.pacienteId();
      const data = this.data();
      this.recarregar();
      untracked(() => {
        this.carregar(paciente, data);
        const s = this.selecao();
        if (s) this.selecionar(s.dente, s.face);
      });
    });
  }

  private carregar(paciente: string, data: string): void {
    this.service.odontograma(paciente, data).subscribe({
      next: (o) => {
        this.erro.set(null);
        this.odontograma.set(o);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected selecionar(dente: number, face: FaceDente | null): void {
    this.selecao.set({ dente, face });
    this.anulando.set(null);
    this.service.historicoDoDente(this.pacienteId(), dente).pipe(catchError(() => of([]))).subscribe((h) => this.historico.set(h));
  }

  protected marcar(c: CondicaoOdontologicaDto): void {
    const s = this.selecao();
    if (!s) return;
    this.pendentes.update((l) => [...l.filter((p) => !(p.dente === s.dente && (p.face ?? null) === s.face)), { dente: s.dente, face: s.face, condicaoId: c.uuid }]);
  }

  protected salvar(): void {
    const avaliacao = this.avaliacaoId();
    if (!avaliacao) return;
    this.salvando.set(true);
    this.service.marcar(avaliacao, this.pendentes()).subscribe({
      next: (o) => {
        this.salvando.set(false);
        this.pendentes.set([]);
        this.erro.set(null);
        this.odontograma.set(o);
        const s = this.selecao();
        if (s) this.selecionar(s.dente, s.face);
        this.alterado.emit();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected anular(r: RegistroOdontogramaDto): void {
    this.service.anular(r.uuid, this.motivo().trim()).subscribe({
      next: () => {
        this.anulando.set(null);
        this.carregar(this.pacienteId(), this.data());
        this.selecionar(r.dente, this.selecao()?.face ?? null);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  // ── Desenho ────────────────────────────────────────────────────────────────────────────────────

  /**
   * As cinco faces: o centro (oclusal nos posteriores, incisal nos anteriores), a vestibular do lado de fora da boca
   * (em cima na arcada superior, embaixo na inferior), a lingual/palatina do lado de dentro, e a mesial voltada para a
   * linha média (à direita nos quadrantes 1, 4, 5 e 8; à esquerda nos outros).
   */
  protected facesDesenho(dente: number, superior: boolean): { face: FaceDente; pontos: string }[] {
    const q = Math.floor(dente / 10);
    const mesialADireita = q === 1 || q === 4 || q === 5 || q === 8;
    const centro: FaceDente = dente % 10 <= 3 ? 'INCISAL' : 'OCLUSAL';
    return [
      { face: superior ? 'VESTIBULAR' : 'LINGUAL_PALATINA', pontos: '0,0 36,0 26,10 10,10' },
      { face: superior ? 'LINGUAL_PALATINA' : 'VESTIBULAR', pontos: '10,26 26,26 36,36 0,36' },
      { face: mesialADireita ? 'DISTAL' : 'MESIAL', pontos: '0,0 10,10 10,26 0,36' },
      { face: mesialADireita ? 'MESIAL' : 'DISTAL', pontos: '26,10 36,0 36,36 26,26' },
      { face: centro, pontos: '10,10 26,10 26,26 10,26' },
    ];
  }

  private registroDaFace(dente: number, face: FaceDente): { cor: string; nome: string } | null {
    const p = this.pendentes().find((x) => x.dente === dente && x.face === face);
    if (p) {
      const c = this.condicoes().find((x) => x.uuid === p.condicaoId);
      return c ? { cor: c.cor, nome: c.nome + ' (a salvar)' } : null;
    }
    const r = this.odontograma()?.dentes.find((d) => d.dente === dente)?.faces.find((f) => f.face === face)?.registro;
    return r ? { cor: r.cor, nome: r.condicaoNome } : null;
  }

  private registroDoDente(dente: number): { cor: string; sigla: string } | null {
    const p = this.pendentes().find((x) => x.dente === dente && !x.face);
    if (p) {
      const c = this.condicoes().find((x) => x.uuid === p.condicaoId);
      return c ? { cor: c.cor, sigla: c.sigla } : null;
    }
    const r = this.odontograma()?.dentes.find((d) => d.dente === dente)?.condicaoDente;
    return r ? { cor: r.cor, sigla: r.sigla } : null;
  }

  protected corDaFace(dente: number, face: FaceDente): string {
    return this.registroDaFace(dente, face)?.cor ?? '#ffffff';
  }

  protected rotuloDaFace(dente: number, face: FaceDente): string {
    return this.registroDaFace(dente, face)?.nome ?? '';
  }

  protected corDoDente(dente: number): string {
    return this.registroDoDente(dente)?.cor ?? '';
  }

  protected siglaDoDente(dente: number): string {
    return this.registroDoDente(dente)?.sigla ?? '';
  }

  protected nomeDaCondicao(uuid: string): string {
    return this.condicoes().find((c) => c.uuid === uuid)?.nome ?? '';
  }

  protected dataBr(v: string): string {
    const [a, m, d] = v.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  private falhou(e: HttpErrorResponse): void {
    this.salvando.set(false);
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar o odontograma.');
  }
}

function iso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
