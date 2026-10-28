import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PainelRapsDto } from '../../../core/models/saude-mental';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { RapsService } from '../../../core/services/raps';

/**
 * A Rede de Atenção Psicossocial (ADR-0153): os pontos da rede por componente, pelos perfis e capacidades das unidades,
 * com as modalidades, os casos e o que cada ponto oferece de fato (as intervenções dos últimos 90 dias), e as lacunas.
 * Só números.
 */
@Component({
  selector: 'app-rede-raps',
  imports: [RouterLink],
  template: `
    <header class="page-head">
      <div>
        <h1>Rede de Atenção Psicossocial</h1>
        <p>Os pontos da RAPS por componente, pelo modelo operacional das unidades @if (painel(); as p) { · {{ p.abrangencia }} }</p>
      </div>
      <div class="page-actions"><a class="btn btn--ghost" routerLink="/assistencia/saude-mental">Voltar à Saúde Mental</a></div>
    </header>
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (painel(); as p) {
      @if (p.lacunas.length) {
        <div class="box box--warn" role="status" aria-label="Lacunas da rede">
          <strong>Lacunas da rede</strong>
          <ul class="raps-lacunas">@for (l of p.lacunas; track l) { <li>{{ l }}</li> }</ul>
        </div>
      }
      @for (c of p.componentes; track c.componente) {
        <section class="card raps-comp" [attr.aria-label]="c.rotulo">
          <h2 class="mo-grupo">{{ c.rotulo }} <span class="cell-sub" style="display: inline;">· {{ c.unidades.length }} ponto(s)</span></h2>
          @if (c.unidades.length) {
            <div class="tbl-wrap raps-tabela">
              <table [attr.aria-label]="'Pontos de ' + c.rotulo">
                <thead><tr><th scope="col">Unidade</th><th scope="col">Modalidades e capacidades</th><th scope="col" class="num">Casos</th><th scope="col">Intervenções em 90 dias</th></tr></thead>
                <tbody>
                  @for (u of c.unidades; track u.unidadeId) {
                    <tr>
                      <td>{{ u.unidadeNome }}</td>
                      <td>@for (x of u.capacidades; track x) { <span class="tag tag--info" style="margin: 0 4px 4px 0;">{{ x }}</span> } @empty { <span class="cell-sub">pelo perfil</span> }</td>
                      <td class="num mono">{{ u.casosEmAcompanhamento }}</td>
                      <td class="cell-sub">@for (i of u.intervencoes90Dias; track i.chave; let ultimo = $last) { {{ i.rotulo }}: {{ i.quantidade }}@if (!ultimo) { · } } @empty { — }</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          } @else {
            <p class="cell-sub">Nenhuma unidade neste componente.</p>
          }
        </section>
      }
    } @else if (!erro()) {
      <p class="cell-sub">Montando a rede…</p>
    }
  `,
  styles: `
    .raps-comp { padding: var(--s-4) var(--s-5); margin-top: var(--s-4); }
    .raps-tabela table { min-width: 0; width: 100%; }
    .raps-lacunas { margin: 4px 0 0 18px; padding: 0; }
  `,
})
export class RedeRaps {
  private readonly service = inject(RapsService);

  protected readonly painel = signal<PainelRapsDto | null>(null);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.service.painel().subscribe({
      next: (p) => this.painel.set(p),
      error: (e: HttpErrorResponse) =>
        this.erro.set(e.status === 403 ? 'Seu acesso não inclui a rede de atenção psicossocial.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível montar a rede.')),
    });
  }
}
