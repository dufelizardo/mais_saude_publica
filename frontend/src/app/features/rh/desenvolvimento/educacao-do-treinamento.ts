import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { CompetenciaDto, ConteudoDoTreinamentoDto, EducacaoDoTreinamentoDto } from '../../../core/models/educacao-permanente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { EducacaoPermanenteService } from '../../../core/services/educacao-permanente';
import { Drawer } from '../../../shared/drawer/drawer';
import { dataBr } from '../../../shared/formato';

/** Gaveta Competências e conteúdos de um treinamento (ADR-0173). */
@Component({
  selector: 'app-educacao-do-treinamento',
  imports: [Drawer],
  template: `
    <app-drawer [titulo]="'Competências e conteúdos'" (fechar)="fechar.emit()">
      <form class="dw-form" (submit)="$event.preventDefault(); salvar()" novalidate>
        <div class="dw-body">
          @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
          <dl class="ro"><div><dt>Treinamento</dt><dd>{{ atual().treinamentoNome }}</dd></div></dl>
          <fieldset class="perm-groups">
            <legend>Competências desenvolvidas <span class="cnt">{{ competenciasEscolhidas().size }}</span></legend>
            <div class="perm-group">
              @for (c of competenciasVisiveis(); track c.uuid) {
                <label class="perm-check">
                  <input type="checkbox" [checked]="competenciasEscolhidas().has(c.uuid)" (change)="alternar(competenciasEscolhidas, c.uuid)" />
                  <span><span class="perm-check__txt">{{ c.nome }}@if (!c.ativo) { · inativa }</span><span class="perm-check__cod mono">{{ c.codigo }}</span></span>
                </label>
              } @empty {
                <p class="cell-sub" style="padding: 8px 12px;">Nenhuma competência ativa no catálogo.</p>
              }
            </div>
          </fieldset>
          <fieldset class="perm-groups">
            <legend>Material didático <span class="cnt">{{ conteudosEscolhidos().size }}</span></legend>
            <p class="dw-hint">Conteúdos educativos que já passaram pela revisão técnica. O treinamento mostra sempre a versão em vigor.</p>
            <div class="perm-group">
              @for (c of conteudosVisiveis(); track c.grupoId) {
                <label class="perm-check">
                  <input type="checkbox" [checked]="conteudosEscolhidos().has(c.grupoId)" (change)="alternar(conteudosEscolhidos, c.grupoId)" />
                  <span><span class="perm-check__txt">{{ c.titulo }}</span><span class="perm-check__cod mono">v{{ c.versao }} · validade {{ dataBr(c.validade) }}</span></span>
                  @if (c.vencido) { <span class="tag tag--warn">Vencido</span> }
                </label>
              } @empty {
                <p class="cell-sub" style="padding: 8px 12px;">Nenhum conteúdo revisado. Os conteúdos são escritos e revisados em Comunicação.</p>
              }
            </div>
          </fieldset>
        </div>
        <footer class="dw-foot">
          <button type="button" class="btn btn--ghost" (click)="fechar.emit()">Cancelar</button>
          <button type="submit" class="btn" [disabled]="submitting()">{{ submitting() ? 'Salvando…' : 'Salvar' }}</button>
        </footer>
      </form>
    </app-drawer>
  `,
})
export class EducacaoDoTreinamento {
  private readonly service = inject(EducacaoPermanenteService);

  readonly atual = input.required<EducacaoDoTreinamentoDto>();
  readonly competencias = input.required<CompetenciaDto[]>();
  readonly fechar = output<void>();
  readonly salvou = output<string>();

  protected readonly dataBr = dataBr;
  protected readonly disponiveis = signal<ConteudoDoTreinamentoDto[]>([]);
  protected readonly competenciasEscolhidas = signal(new Set<string>());
  protected readonly conteudosEscolhidos = signal(new Set<string>());
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);

  /** As ativas e as inativas que já estavam ligadas. */
  protected readonly competenciasVisiveis = computed(() => {
    const ligadas = new Set(this.atual().competencias.map((c) => c.uuid));
    return this.competencias().filter((c) => c.ativo || ligadas.has(c.uuid));
  });

  protected readonly conteudosVisiveis = computed(() => {
    const vistos = new Set(this.disponiveis().map((c) => c.grupoId));
    return [...this.disponiveis(), ...this.atual().conteudos.filter((c) => !vistos.has(c.grupoId))];
  });

  ngOnInit(): void {
    this.competenciasEscolhidas.set(new Set(this.atual().competencias.map((c) => c.uuid)));
    this.conteudosEscolhidos.set(new Set(this.atual().conteudos.map((c) => c.grupoId)));
    this.service.conteudosDisponiveis().pipe(catchError(() => of([]))).subscribe((l) => this.disponiveis.set(l));
  }

  protected alternar(alvo: typeof this.competenciasEscolhidas, id: string): void {
    const novo = new Set(alvo());
    if (novo.has(id)) novo.delete(id);
    else novo.add(id);
    alvo.set(novo);
  }

  protected salvar(): void {
    if (this.submitting()) return;
    this.submitting.set(true);
    this.erroApi.set(null);
    this.service.ligar(this.atual().treinamentoId, [...this.competenciasEscolhidas()], [...this.conteudosEscolhidos()]).subscribe({
      next: (r) => {
        this.submitting.set(false);
        this.salvou.emit(r.treinamentoNome);
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }
}
