import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { ErrorResponseDto } from '../../core/models/profissional';
import { NotificacaoDto } from '../../core/models/vigilancia';
import { VigilanciaService } from '../../core/services/vigilancia';
import { Mapa, PinoMapa } from '../../shared/mapa/mapa';

/**
 * Local do caso (ADR-0130), na gaveta da notificação: de onde vem (o domicílio do paciente no cadastro territorial, ou
 * marcado no mapa), a microárea e o minimapa. Quem investiga marca o local provável de infecção com um clique.
 */
@Component({
  selector: 'app-local-do-caso',
  imports: [Mapa],
  template: `
    <h4 class="mo-grupo">Local do caso</h4>
    <p class="cell-sub" aria-label="Local do caso">
      @switch (notificacao().origemLocal) {
        @case ('DOMICILIO') { Pelo domicílio do paciente no cadastro territorial }
        @case ('MARCADO') { Marcado no mapa }
        @default { Sem local: o paciente não tem domicílio cadastrado e o ponto não foi marcado }
      }@if (notificacao().microareaCodigo) { · microárea {{ notificacao().microareaCodigo }} da área {{ notificacao().areaCodigo }} }
    </p>
    @if (marcando() || pino().length) {
      <app-mapa [pinos]="pino()" [marcarPonto]="marcando()" [altura]="200" [rotulo]="'Local do caso de ' + notificacao().pacienteNome"
        (ponto)="ponto.set($event)" />
    }
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (podeInvestigar() && notificacao().situacao !== 'ENCERRADA') {
      <div class="row-actions" style="justify-content: flex-start; margin-top: var(--s-2);">
        @if (!marcando()) {
          <button type="button" class="btn btn--ghost btn--sm" (click)="marcando.set(true); ponto.set(null)">Marcar local no mapa</button>
        } @else {
          <button type="button" class="btn btn--sm" (click)="salvar()" [disabled]="!ponto() || salvando()">{{ salvando() ? 'Salvando…' : 'Salvar local' }}</button>
          <button type="button" class="btn btn--ghost btn--sm" (click)="marcando.set(false)">Cancelar</button>
          @if (!ponto()) { <span class="cell-sub">Clique no mapa onde o caso aconteceu.</span> }
        }
      </div>
    }
  `,
})
export class LocalDoCaso {
  private readonly service = inject(VigilanciaService);

  readonly notificacao = input.required<NotificacaoDto>();
  readonly podeInvestigar = input(false);
  readonly atualizada = output<NotificacaoDto>();

  protected readonly marcando = signal(false);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly ponto = signal<{ lat: number; lng: number } | null>(null);

  protected readonly pino = computed<PinoMapa[]>(() => {
    const p = this.ponto();
    const n = this.notificacao();
    const lat = this.marcando() ? p?.lat : n.latitude;
    const lng = this.marcando() ? p?.lng : n.longitude;
    return lat != null && lng != null ? [{ id: n.uuid, lat, lng, sigla: '!', classe: 'unit-pin t-closed', descricao: 'Local do caso' }] : [];
  });

  protected salvar(): void {
    const p = this.ponto();
    if (!p) return;
    this.salvando.set(true);
    this.service.marcarLocal(this.notificacao().uuid, p.lat, p.lng).subscribe({
      next: (n) => {
        this.salvando.set(false);
        this.marcando.set(false);
        this.erro.set(null);
        this.atualizada.emit(n);
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar o local.');
      },
    });
  }
}
