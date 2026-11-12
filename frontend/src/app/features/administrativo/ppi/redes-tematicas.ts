import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { PainelRedeTematicaDto, REDES_TEMATICAS, RedeTematica } from '../../../core/models/rede';
import { RedeService } from '../../../core/services/rede';

/**
 * Aba Redes temáticas da Rede e pactuação (ADR-0197): por componente, as unidades do acesso com os perfis e as capacidades ligados a ele,
 * e as lacunas no alto. A RAPS tem o seu painel desde a ADR-0153.
 */
@Component({
  selector: 'app-redes-tematicas',
  imports: [RouterLink],
  template: `
    <section class="card" id="pn-redes" role="tabpanel" aria-labelledby="tab-redes">
      <div class="card__head">
        <div><h2>Redes temáticas</h2><p class="sub">Os pontos de cada componente vêm do modelo operacional das unidades</p></div>
        <div class="page-actions">
          <select class="sel" aria-label="Rede temática" [value]="rede()" (change)="escolher($any($event.target).value)">
            @for (r of redes; track r[0]) { <option [value]="r[0]">{{ r[1] }}</option> }
          </select>
          <a class="btn btn--ghost btn--sm" routerLink="/assistencia/saude-mental/raps">Rede de Atenção Psicossocial (RAPS)</a>
        </div>
      </div>
      <div style="padding: 0 var(--s-5) var(--s-5);">
        @if (painel(); as p) {
          @if (p.lacunas.length) {
            <div class="box box--warn" role="status" aria-label="Lacunas da rede"><div><strong>Lacunas</strong>
              <ul style="margin: 4px 0 0; padding-left: 18px;">@for (l of p.lacunas; track l) { <li>{{ l }}</li> }</ul></div></div>
          } @else {
            <div class="box box--ok" role="status"><div>Todos os componentes têm ao menos um ponto no seu acesso.</div></div>
          }
          @for (c of p.componentes; track c.codigo) {
            <h3 class="mo-grupo">{{ c.nome }} <span class="cnt">{{ c.pontos.length }}</span></h3>
            <ul class="eq-prof" [attr.aria-label]="'Pontos de ' + c.nome">
              @for (u of c.pontos; track u.unidadeId) { <li><span class="cell-main">{{ u.unidadeNome }}</span><span class="cell-sub">{{ u.itens.join(' · ') }}</span></li> }
              @empty { <li class="cell-sub">Nenhum ponto.</li> }
            </ul>
          }
        } @else {
          <div class="empty"><strong>{{ erro() ?? 'Carregando…' }}</strong></div>
        }
      </div>
    </section>
  `,
})
export class RedesTematicas {
  private readonly service = inject(RedeService);

  protected readonly redes = Object.entries(REDES_TEMATICAS) as [RedeTematica, string][];
  protected readonly rede = signal<RedeTematica>('URGENCIA_EMERGENCIA');
  protected readonly painel = signal<PainelRedeTematicaDto | null>(null);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.carregar();
  }

  protected escolher(r: RedeTematica): void {
    this.rede.set(r);
    this.carregar();
  }

  private carregar(): void {
    this.painel.set(null);
    this.erro.set(null);
    this.service.redeTematica(this.rede()).pipe(catchError(() => {
      this.erro.set('Não foi possível carregar a rede.');
      return of(null);
    })).subscribe((p) => this.painel.set(p));
  }
}
