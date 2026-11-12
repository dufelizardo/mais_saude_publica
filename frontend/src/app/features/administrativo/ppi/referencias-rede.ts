import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { PainelReferenciasDto, ReferenciaRedeDto } from '../../../core/models/rede';
import { ProcedimentoReguladoResponseDto } from '../../../core/models/regulacao';
import { RedeService } from '../../../core/services/rede';
import { Drawer } from '../../../shared/drawer/drawer';
import { hojeIso } from '../../../shared/formato';

/**
 * Aba Referências da Rede e pactuação (ADR-0197): para onde cada origem (unidade ou esfera) encaminha cada procedimento regulado, com a
 * vigência e o histórico, e as autorizações fora da referência nos últimos 90 dias. A Regulação recebe a referência como sugestão.
 */
@Component({
  selector: 'app-referencias-rede',
  imports: [ReactiveFormsModule, Drawer],
  template: `
    <section class="card" id="pn-referencias" role="tabpanel" aria-labelledby="tab-referencias">
      <div class="card__head">
        <div><h2>Referências da rede</h2><p class="sub">A referência mais específica vale: a da unidade e, sem ela, a do município ou da regional</p></div>
        <div class="page-actions">
          <select class="sel" aria-label="Procedimento das referências" [value]="procedimentoId()" (change)="filtrar($any($event.target).value)">
            <option value="">Todos os procedimentos</option>
            @for (p of procedimentos(); track p.uuid) { <option [value]="p.uuid">{{ p.nome }}</option> }
          </select>
          @if (podeGerenciar()) { <button type="button" class="btn btn--sm" (click)="abrirNova()">Nova referência</button> }
        </div>
      </div>
      <div style="padding: 0 var(--s-5) var(--s-5);">
        @if (painel(); as p) {
          @if (p.indicadores.length) {
            <div class="tbl-wrap">
              <table aria-label="Autorizações fora da referência nos últimos 90 dias">
                <thead><tr><th scope="col">Procedimento</th><th scope="col" class="num">Autorizações com referência</th><th scope="col" class="num">Fora da referência</th><th scope="col" class="num">%</th></tr></thead>
                <tbody>
                  @for (i of p.indicadores; track i.procedimentoId) {
                    <tr><td>{{ i.procedimentoNome }}</td><td class="num">{{ i.autorizacoes }}</td><td class="num">{{ i.foraDaReferencia }}</td><td class="num">{{ pct(i.percentualFora) }}</td></tr>
                  }
                </tbody>
              </table>
            </div>
          }
          <div class="tbl-wrap">
            <table aria-label="Referências">
              <thead><tr><th scope="col">Procedimento</th><th scope="col">Origem</th><th scope="col">Executante</th><th scope="col">Vigência</th><th scope="col">Situação</th><th scope="col" class="num">Ações</th></tr></thead>
              <tbody>
                @for (r of p.referencias; track r.uuid) {
                  <tr>
                    <td>{{ r.procedimentoNome }}</td>
                    <td>{{ r.origemNome }}@if (r.origemTipo && r.origemTipo !== 'UBS') { <span class="cell-sub">e unidades abaixo</span> }</td>
                    <td>{{ r.executanteNome }}</td>
                    <td class="mono">{{ dataBr(r.inicio) }}{{ r.fim ? ' a ' + dataBr(r.fim) : ' em diante' }}</td>
                    <td><span class="badge" [class]="'badge badge--' + classe(r)"><span class="dot" aria-hidden="true"></span> {{ situacao(r) }}</span></td>
                    <td class="num">
                      @if (podeGerenciar() && !r.fim && r.inicio <= hoje) {
                        <button type="button" class="btn btn--ghost btn--sm" (click)="encerrar(r)" [attr.aria-label]="'Encerrar a referência de ' + r.origemNome + ' para ' + r.procedimentoNome">Encerrar</button>
                      }
                    </td>
                  </tr>
                } @empty { <tr><td colspan="6"><div class="empty"><strong>Nenhuma referência</strong><span>Sem referência, a Regulação escolhe a executante sem sugestão.</span></div></td></tr> }
              </tbody>
            </table>
          </div>
        } @else {
          <div class="empty"><strong>Carregando…</strong></div>
        }
      </div>
    </section>

    @if (gaveta()) {
      <app-drawer titulo="Nova referência" (fechar)="gaveta.set(false)">
        <form class="dw-form" [formGroup]="form" (ngSubmit)="salvar()" novalidate>
          <div class="dw-body">
            @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
            <p class="dw-hint">Se a origem já tem referência para o procedimento, a nova começa depois dela e a encerra na véspera.</p>
            <div class="dw-field">
              <label for="f-procedimentoReferencia">Procedimento <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
              <select id="f-procedimentoReferencia" formControlName="procedimentoId" [attr.aria-invalid]="!!erro('procedimentoReferencia')" aria-describedby="e-procedimentoReferencia">
                <option value="">Escolha…</option>
                @for (p of procedimentos(); track p.uuid) { <option [value]="p.uuid">{{ p.nome }}</option> }
              </select>
              @if (erro('procedimentoReferencia')) { <p class="dw-err" id="e-procedimentoReferencia">{{ erro('procedimentoReferencia') }}</p> }
            </div>
            <div class="dw-row">
              <div class="dw-field">
                <label for="f-origemReferencia">Origem (unidade ou esfera) <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <select id="f-origemReferencia" formControlName="origemId" [attr.aria-invalid]="!!erro('origemReferencia')" aria-describedby="e-origemReferencia">
                  <option value="">Escolha…</option>
                  @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                </select>
                @if (erro('origemReferencia')) { <p class="dw-err" id="e-origemReferencia">{{ erro('origemReferencia') }}</p> }
              </div>
              <div class="dw-field">
                <label for="f-executanteReferencia">Executante <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <select id="f-executanteReferencia" formControlName="executanteId" [attr.aria-invalid]="!!erro('executanteReferencia')" aria-describedby="e-executanteReferencia">
                  <option value="">Escolha…</option>
                  @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                </select>
                @if (erro('executanteReferencia')) { <p class="dw-err" id="e-executanteReferencia">{{ erro('executanteReferencia') }}</p> }
              </div>
            </div>
            <div class="dw-field">
              <label for="f-inicioReferencia">Início</label>
              <input id="f-inicioReferencia" type="date" formControlName="inicio" />
            </div>
            <div class="dw-field"><label for="f-observacaoReferencia">Observação<span class="opt">opcional</span></label><input id="f-observacaoReferencia" type="text" maxlength="500" formControlName="observacao" /></div>
          </div>
          <footer class="dw-foot">
            <button type="button" class="btn btn--ghost" (click)="gaveta.set(false)">Cancelar</button>
            <button type="submit" class="btn" [disabled]="submitting()">{{ submitting() ? 'Salvando…' : 'Salvar referência' }}</button>
          </footer>
        </form>
      </app-drawer>
    }
  `,
})
export class ReferenciasRede {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(RedeService);

  readonly procedimentos = input.required<ProcedimentoReguladoResponseDto[]>();
  readonly unidades = input.required<{ uuid: string; nome: string }[]>();
  readonly podeGerenciar = input(false);
  readonly aviso = output<{ titulo: string; detalhe: string }>();

  protected readonly hoje = hojeIso();
  protected readonly procedimentoId = signal('');
  protected readonly painel = signal<PainelReferenciasDto | null>(null);
  protected readonly gaveta = signal(false);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});

  protected readonly form = this.fb.nonNullable.group({ procedimentoId: [''], origemId: [''], executanteId: [''], inicio: [''], observacao: [''] });

  constructor() {
    this.carregar();
  }

  protected filtrar(id: string): void {
    this.procedimentoId.set(id);
    this.carregar();
  }

  private carregar(): void {
    this.service.referencias(this.procedimentoId() || null).pipe(catchError(() => of({ referencias: [], indicadores: [] })))
      .subscribe((p) => this.painel.set(p));
  }

  protected dataBr(iso: string): string {
    return iso.slice(0, 10).split('-').reverse().join('/');
  }

  protected pct(v: number): string {
    return String(v).replace('.', ',') + '%';
  }

  protected situacao(r: ReferenciaRedeDto): string {
    if (r.vigente) return 'Vigente';
    return r.inicio > this.hoje ? 'A partir de ' + this.dataBr(r.inicio) : 'Encerrada';
  }

  protected classe(r: ReferenciaRedeDto): string {
    return r.vigente ? 'ok' : r.inicio > this.hoje ? 'info' : 'muted';
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  protected abrirNova(): void {
    this.form.reset({ procedimentoId: this.procedimentoId(), origemId: '', executanteId: '', inicio: this.hoje, observacao: '' });
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(true);
  }

  protected salvar(): void {
    if (this.submitting()) return;
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.procedimentoId) erros['procedimentoReferencia'] = 'Escolha o procedimento.';
    if (!v.origemId) erros['origemReferencia'] = 'Escolha a origem.';
    if (!v.executanteId) erros['executanteReferencia'] = 'Escolha a executante.';
    else if (v.executanteId === v.origemId) erros['executanteReferencia'] = 'A executante não pode ser a própria origem.';
    this.erros.set(erros);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) {
      setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
      return;
    }
    this.submitting.set(true);
    this.service.registrarReferencia({ procedimentoId: v.procedimentoId, origemId: v.origemId, executanteId: v.executanteId, inicio: v.inicio || null,
      observacao: v.observacao.trim() || null }).subscribe({
      next: (r) => {
        this.submitting.set(false);
        this.gaveta.set(false);
        this.aviso.emit({ titulo: 'Referência registrada', detalhe: `${r.origemNome} → ${r.executanteNome}` });
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  protected encerrar(r: ReferenciaRedeDto): void {
    this.service.encerrarReferencia(r.uuid).subscribe({
      next: () => {
        this.aviso.emit({ titulo: 'Referência encerrada', detalhe: `${r.origemNome} → ${r.executanteNome}` });
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.aviso.emit({ titulo: 'Não foi possível', detalhe: (e.error as ErrorResponseDto | undefined)?.message ?? 'Tente novamente.' }),
    });
  }
}
