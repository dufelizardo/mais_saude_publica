import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { CoberturaRedeDto } from '../../../core/models/rede';
import { RedeService } from '../../../core/services/rede';

/**
 * Aba Cobertura da Rede e pactuação (ADR-0198): os indicadores de pactuação da competência, por procedimento e, ao abrir um, por origem com
 * a população adscrita, a cota por mil e os alertas; e as lacunas das redes temáticas.
 */
@Component({
  selector: 'app-cobertura-rede',
  template: `
    <div id="pn-cobertura" role="tabpanel" aria-labelledby="tab-cobertura">
      @if (cobertura(); as c) {
        <section class="stats" aria-label="Indicadores de pactuação">
          <div class="stat"><div><div class="stat__lbl">Oferta pactuada</div><div class="stat__val mono">{{ pct(c.resumo.percentualPactuado) }}</div><div class="stat__sub">{{ c.resumo.pactuado }} de {{ c.resumo.ofertado }} vagas</div></div></div>
          <div class="stat"><div><div class="stat__lbl">Cota usada</div><div class="stat__val mono">{{ pct(c.resumo.percentualCotaUsada) }}</div><div class="stat__sub">{{ c.resumo.utilizado - c.resumo.extracotas }} na cota</div></div></div>
          <div class="stat"><div><div class="stat__lbl">Extracotas</div><div class="stat__val mono">{{ pct(c.resumo.percentualExtracota) }}</div><div class="stat__sub">{{ c.resumo.extracotas }} autorizada(s) sem saldo</div></div></div>
          <div class="stat"><div><div class="stat__lbl">Fora da referência</div><div class="stat__val mono">{{ pct(c.resumo.percentualForaDaReferencia) }}</div><div class="stat__sub">{{ c.resumo.cotasEsgotadasComFila }} cota(s) esgotada(s) com fila</div></div></div>
        </section>
        @if (c.alertas.length || c.lacunas.length) {
          <div class="box box--warn" role="status" aria-label="Alertas da rede"><div><strong>Alertas</strong>
            <ul style="margin: 4px 0 0; padding-left: 18px;">
              @for (a of c.alertas; track $index) { <li>{{ a }}</li> }
              @for (l of c.lacunas; track $index) { <li>{{ l.rede }}: {{ l.lacuna }}</li> }
            </ul></div></div>
        }
        <section class="card" aria-labelledby="h-cobertura">
          <div class="card__head"><div><h2 id="h-cobertura">Cobertura por procedimento</h2><p class="sub">{{ c.abrangencia }}@if (c.fechada) { · competência fechada }</p></div></div>
          <div class="tbl-wrap">
            <table aria-label="Cobertura por procedimento">
              <thead><tr><th scope="col">Procedimento</th><th scope="col" class="num">Ofertado</th><th scope="col" class="num">Pactuado</th><th scope="col" class="num">Usado</th><th scope="col" class="num">Extracotas</th><th scope="col" class="num">Fora da ref.</th><th scope="col" class="num">Na fila</th><th scope="col" class="num">Espera (dias)</th><th scope="col" class="num">Ações</th></tr></thead>
              <tbody>
                @for (p of c.procedimentos; track p.procedimentoId) {
                  <tr>
                    <td>{{ p.procedimentoNome }}</td>
                    <td class="num">{{ p.ofertado }}</td>
                    <td class="num">{{ p.pactuado }} <span class="cell-sub">{{ pct(p.percentualPactuado) }}</span></td>
                    <td class="num">{{ p.utilizado }} <span class="cell-sub">{{ pct(p.percentualCotaUsada) }}</span></td>
                    <td class="num">{{ p.extracotas }}</td>
                    <td class="num">{{ pct(p.percentualForaDaReferencia) }}</td>
                    <td class="num">{{ p.naFila }}</td>
                    <td class="num">{{ dias(p.esperaNaFilaDias) }}</td>
                    <td class="num"><button type="button" class="btn btn--ghost btn--sm" (click)="aberto.set(aberto() === p.procedimentoId ? null : p.procedimentoId)" [attr.aria-expanded]="aberto() === p.procedimentoId" [attr.aria-label]="'Origens de ' + p.procedimentoNome">Origens</button></td>
                  </tr>
                  @if (aberto() === p.procedimentoId) {
                    <tr><td colspan="9">
                      <table [attr.aria-label]="'Origens de ' + p.procedimentoNome" style="width: 100%; font-size: 0.8125rem;">
                        <thead><tr><th scope="col">Origem</th><th scope="col" class="num">Adscritos</th><th scope="col" class="num">Cota</th><th scope="col" class="num">Por mil</th><th scope="col" class="num">Solicitadas</th><th scope="col" class="num">Autorizadas</th><th scope="col" class="num">Aguardando</th><th scope="col" class="num">Saldo</th><th scope="col">Alertas</th></tr></thead>
                        <tbody>
                          @for (x of p.origens; track x.unidadeId) {
                            <tr>
                              <td>{{ x.unidadeNome }}@if (!x.comReferencia) { <span class="cell-sub">sem referência</span> }</td>
                              <td class="num">{{ x.populacaoAdscrita }}</td>
                              <td class="num">{{ x.cota ?? '—' }}</td>
                              <td class="num">{{ x.cotaPorMil ?? '—' }}</td>
                              <td class="num">{{ x.solicitadas }}</td>
                              <td class="num">{{ x.autorizadas }}@if (x.extracotas) { <span class="cell-sub">{{ x.extracotas }} extra</span> }</td>
                              <td class="num">{{ x.aguardando }}</td>
                              <td class="num">{{ x.saldo ?? '—' }}</td>
                              <td>@for (a of x.alertas; track a) { <span class="badge badge--warn">{{ a }}</span> }</td>
                            </tr>
                          } @empty { <tr><td colspan="9" class="cell-sub">Nenhuma origem com cota ou demanda.</td></tr> }
                        </tbody>
                      </table>
                    </td></tr>
                  }
                } @empty {
                  <tr><td colspan="9"><div class="empty"><strong>Nenhuma oferta nesta competência</strong><span>A cobertura conta os procedimentos com oferta cadastrada na PPI.</span></div></td></tr>
                }
              </tbody>
            </table>
          </div>
        </section>
      } @else {
        <div class="empty"><strong>{{ erro() ?? 'Carregando…' }}</strong></div>
      }
    </div>
  `,
})
export class CoberturaRede {
  private readonly service = inject(RedeService);

  readonly competencia = input.required<string>();

  protected readonly cobertura = signal<CoberturaRedeDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly aberto = signal<string | null>(null);

  constructor() {
    effect(() => {
      const competencia = this.competencia();
      untracked(() => this.carregar(competencia));
    });
  }

  private carregar(competencia: string): void {
    this.cobertura.set(null);
    this.erro.set(null);
    this.aberto.set(null);
    this.service.cobertura(competencia).pipe(catchError(() => {
      this.erro.set('Não foi possível carregar a cobertura.');
      return of(null);
    })).subscribe((c) => this.cobertura.set(c));
  }

  protected pct(v: number | null | undefined): string {
    return v == null ? '—' : String(v).replace('.', ',') + '%';
  }

  protected dias(v: number | null | undefined): string {
    return v == null ? '—' : String(v).replace('.', ',');
  }
}
