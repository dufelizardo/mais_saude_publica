import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { EncaminhamentoIntersetorialDto, InstituicaoExternaDto, TIPOS_INSTITUICAO } from '../../core/models/intersetorialidade';
import { ErrorResponseDto } from '../../core/models/profissional';
import { IntersetorialidadeService } from '../../core/services/intersetorialidade';

/** A comunicação obrigatória da violência notificada (ADR-0201). */
export interface ComunicacaoViolenciaDto {
  obrigatoria: boolean;
  destino: string | null;
  tiposAceitos: string[];
  baseLegal: string | null;
  prazo: string | null;
  situacao: 'NAO_SE_APLICA' | 'PENDENTE' | 'VENCIDA' | 'FEITA';
  feitaEm: string | null;
  noPrazo: boolean | null;
  encaminhamentos: EncaminhamentoIntersetorialDto[];
}

/**
 * No detalhe da notificação de violência (ADR-0201): a quem a lei manda comunicar, até quando, e a comunicação registrada como
 * encaminhamento intersetorial. A rede sugere; quem notificou ou investiga registra.
 */
@Component({
  selector: 'app-comunicacao-violencia',
  template: `
    @if (c(); as x) {
      @if (x.obrigatoria) {
        <section class="cv" aria-label="Comunicação obrigatória">
          <h4 class="mo-grupo">Comunicação obrigatória</h4>
          <p><strong>{{ x.destino }}</strong> · {{ x.baseLegal }}</p>
          <p>
            <span [class]="'tag tag--' + (x.situacao === 'FEITA' ? 'ok' : x.situacao === 'VENCIDA' ? 'alert' : 'warn')">{{ rotulos[x.situacao] }}</span>
            @if (x.situacao === 'FEITA') { <span class="cell-sub">em {{ dataHora(x.feitaEm!) }}{{ x.noPrazo ? ', no prazo' : ', fora do prazo' }}</span> }
            @else { <span class="cell-sub">prazo até {{ dataHora(x.prazo!) }}</span> }
          </p>
          @if (x.encaminhamentos.length) {
            <ul class="cv-lista" aria-label="Comunicações registradas">
              @for (e of x.encaminhamentos; track e.uuid) {
                <li>{{ e.instituicaoNome }} <span class="cell-sub">{{ data(e.enviadoEm) }} · {{ e.situacao.toLowerCase() }}@if (e.motivoCancelamento) { · {{ e.motivoCancelamento }} }</span>
                  @if ((e.situacao === 'ENVIADO' || e.situacao === 'RECEBIDO') && cancelando() !== e.uuid) { <button type="button" class="link-btn" (click)="cancelando.set(e.uuid); motivo.set('')" [attr.aria-label]="'Cancelar comunicação a ' + e.instituicaoNome">Cancelar</button> }
                  @if (cancelando() === e.uuid) {
                    <div class="cv-form">
                      <div class="dw-field" style="margin: 0;"><label for="f-cv-motivo">Motivo do cancelamento</label><input id="f-cv-motivo" type="text" maxlength="500" [value]="motivo()" (input)="motivo.set($any($event.target).value)" /></div>
                      <button type="button" class="btn btn--ghost btn--sm" (click)="cancelando.set(null)">Voltar</button>
                      <button type="button" class="btn btn--sm" (click)="cancelar(e)" [disabled]="!motivo().trim()">Confirmar</button>
                    </div>
                  }
                </li>
              }
            </ul>
          }
          @if (x.situacao !== 'FEITA') {
            @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
            @if (opcoes().length) {
              <div class="cv-form">
                <div class="dw-field" style="margin: 0;"><label for="f-cv-inst">Instituição</label>
                  <select id="f-cv-inst" (change)="escolhida.set($any($event.target).value)">
                    <option value="">Escolha…</option>
                    @for (i of opcoes(); track i.uuid) { <option [value]="i.uuid">{{ i.nome }} ({{ tipos[i.tipo].rotulo }})</option> }
                  </select></div>
                <button type="button" class="btn btn--sm" (click)="comunicar()" [disabled]="!escolhida() || enviando()">{{ enviando() ? 'Registrando…' : 'Comunicar' }}</button>
              </div>
            } @else { <p class="cell-sub">Nenhuma instituição ativa de tipo aceito no catálogo da rede intersetorial: cadastre-a para registrar a comunicação.</p> }
          }
        </section>
      }
    }
  `,
  styles: `
    .cv { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); margin: var(--s-3) 0; }
    .cv p { margin: var(--s-1) 0; }
    .cv-lista { margin: var(--s-1) 0; padding-left: var(--s-4); }
    .cv-form { display: flex; gap: var(--s-2); align-items: flex-end; flex-wrap: wrap; margin-top: var(--s-2); }
  `,
})
export class ComunicacaoViolencia {
  private readonly http = inject(HttpClient);
  private readonly intersetorial = inject(IntersetorialidadeService);

  readonly notificacaoId = input.required<string>();

  protected readonly tipos = TIPOS_INSTITUICAO;
  protected readonly rotulos: Record<string, string> = { PENDENTE: 'Pendente', VENCIDA: 'Vencida', FEITA: 'Feita', NAO_SE_APLICA: 'Não se aplica' };
  protected readonly c = signal<ComunicacaoViolenciaDto | null>(null);
  protected readonly instituicoes = signal<InstituicaoExternaDto[]>([]);
  protected readonly escolhida = signal('');
  protected readonly erro = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly cancelando = signal<string | null>(null);
  protected readonly motivo = signal('');
  protected readonly opcoes = computed(() => {
    const aceitos = this.c()?.tiposAceitos ?? [];
    return this.instituicoes().filter((i) => aceitos.includes(i.tipo));
  });

  constructor() {
    effect(() => {
      const id = this.notificacaoId();
      untracked(() => this.carregar(id));
    });
  }

  private url(id: string): string {
    return `/api/v1/vigilancia/notificacao/${id}/comunicacao`;
  }

  private carregar(id: string): void {
    this.http.get<ComunicacaoViolenciaDto>(this.url(id)).pipe(catchError(() => of(null))).subscribe((c) => {
      this.c.set(c);
      if (c?.obrigatoria && c.situacao !== 'FEITA' && !this.instituicoes().length) {
        this.intersetorial.listar({ ativas: true }).pipe(catchError(() => of([]))).subscribe((l) => this.instituicoes.set(l));
      }
    });
  }

  protected comunicar(): void {
    this.enviando.set(true);
    this.erro.set(null);
    this.http.post<ComunicacaoViolenciaDto>(this.url(this.notificacaoId()), { instituicaoId: this.escolhida() }).subscribe({
      next: (c) => {
        this.enviando.set(false);
        this.escolhida.set('');
        this.c.set(c);
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set(x.status === 403 ? 'Seu acesso não inclui esta operação.' : ((x.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível registrar.'));
      },
    });
  }

  protected cancelar(e: EncaminhamentoIntersetorialDto): void {
    this.http.post<ComunicacaoViolenciaDto>(`${this.url(this.notificacaoId())}/${e.uuid}/cancelamento`, { motivo: this.motivo().trim() }).subscribe({
      next: (c) => {
        this.c.set(c);
        this.cancelando.set(null);
        if (!this.instituicoes().length) this.intersetorial.listar({ ativas: true }).pipe(catchError(() => of([]))).subscribe((l) => this.instituicoes.set(l));
      },
      error: (x: HttpErrorResponse) => this.erro.set((x.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível cancelar.'),
    });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
  }
}
