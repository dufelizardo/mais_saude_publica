import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { CapacidadeEmergencialDto, RECURSOS_CAPACIDADE, RecursoCapacidade } from '../../core/models/emergencia';
import { ErrorResponseDto } from '../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { EmergenciaService } from '../../core/services/emergencia';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { Drawer } from '../../shared/drawer/drawer';

/**
 * Capacidade emergencial das unidades (ADR-0158): o cadastro feito antes da emergência, com a capacidade normal, quanto
 * se pode ativar a mais e o que está ativado agora. Nos leitos, os leitos ativos da unidade ajudam a conferir o normal.
 */
@Component({
  selector: 'app-capacidade-emergencial',
  imports: [Drawer],
  template: `
    <div class="ca-cab">
      <p class="cell-sub" style="margin: 0;">A capacidade que cada unidade pode ativar numa emergência. Ativar é na emergência, pela coordenação.</p>
      @if (podeGerenciar()) { <button type="button" class="btn btn--sm" (click)="abrir(null)">Nova capacidade</button> }
    </div>
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    <div class="tbl-wrap card">
      <table class="ca-tabela" aria-label="Capacidade emergencial">
        <thead><tr><th scope="col">Unidade</th><th scope="col">Recurso</th><th scope="col" class="num">Normal</th><th scope="col" class="num">Ativável</th><th scope="col" class="num">Ativado agora</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
        <tbody>
          @for (c of lista(); track c.uuid) {
            <tr>
              <td><span class="cell-main">{{ c.unidadeNome }}@if (!c.ativa) { <span class="tag tag--muted">Inativa</span> }</span>@if (c.observacao) { <span class="cell-sub">{{ c.observacao }}</span> }</td>
              <td>{{ recursos[c.recurso] }}@if (c.leitosAtivos != null) { <span class="cell-sub">{{ c.leitosAtivos }} leito(s) ativo(s) no cadastro</span> }</td>
              <td class="num mono">{{ c.normal }}</td>
              <td class="num mono">+{{ c.emergencial }}</td>
              <td class="num mono">{{ c.ativadaAgora }}</td>
              <td class="row-actions">@if (podeGerenciar()) { <button type="button" class="link-btn" (click)="abrir(c)" [attr.aria-label]="'Editar capacidade ' + c.unidadeNome + ' ' + recursos[c.recurso]">Editar</button> }</td>
            </tr>
          } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma capacidade emergencial cadastrada.</td></tr> }
        </tbody>
      </table>
    </div>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g.item ? 'Editar capacidade' : 'Nova capacidade emergencial'" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g.item)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <div class="dw-row">
              <div class="dw-field"><label for="f-ca-unidade">Unidade <span class="req" aria-hidden="true">*</span></label>
                <select id="f-ca-unidade" [disabled]="!!g.item" (change)="f.set({ ...f(), unidadeId: $any($event.target).value })">
                  <option value="" [selected]="!f()['unidadeId']">Escolha…</option>
                  @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['unidadeId'] === u.uuid">{{ u.nome }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-ca-recurso">Recurso <span class="req" aria-hidden="true">*</span></label>
                <select id="f-ca-recurso" [disabled]="!!g.item" (change)="f.set({ ...f(), recurso: $any($event.target).value })">
                  @for (r of listaRecursos; track r) { <option [value]="r" [selected]="(f()['recurso'] || 'LEITOS') === r">{{ recursos[r] }}</option> }
                </select></div>
            </div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-ca-normal">Capacidade normal <span class="req" aria-hidden="true">*</span></label><input id="f-ca-normal" type="number" min="0" [value]="f()['normal'] ?? ''" (input)="set('normal', $event)" /></div>
              <div class="dw-field"><label for="f-ca-emergencial">Ativável a mais <span class="req" aria-hidden="true">*</span></label><input id="f-ca-emergencial" type="number" min="0" [value]="f()['emergencial'] ?? ''" (input)="set('emergencial', $event)" /></div>
            </div>
            <div class="dw-field"><label for="f-ca-observacao">Observação<span class="opt">opcional</span></label><input id="f-ca-observacao" type="text" maxlength="500" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" placeholder="Ex.: ala de observação com 30 macas" /></div>
            @if (g.item) {
              <div class="dw-field"><label class="switch" for="f-ca-ativa"><span><b style="font-weight: 500;">Ativa</b><span class="cell-sub">Inativa não pode ser ativada em emergência.</span></span><input type="checkbox" role="switch" id="f-ca-ativa" [checked]="ativa()" (change)="ativa.set($any($event.target).checked)" /></label></div>
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn">Salvar</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .ca-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-3); margin-bottom: var(--s-3); flex-wrap: wrap; }
    .ca-tabela { min-width: 0; width: 100%; }
  `,
})
export class CapacidadeEmergencial {
  private readonly service = inject(EmergenciaService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  readonly podeGerenciar = input(false);

  protected readonly recursos = RECURSOS_CAPACIDADE;
  protected readonly listaRecursos = Object.keys(RECURSOS_CAPACIDADE) as RecursoCapacidade[];
  protected readonly lista = signal<CapacidadeEmergencialDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly gaveta = signal<{ item: CapacidadeEmergencialDto | null } | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly ativa = signal(true);

  constructor() {
    this.carregar();
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
  }

  protected abrir(c: CapacidadeEmergencialDto | null): void {
    this.erroGaveta.set(null);
    this.f.set(c ? { unidadeId: c.unidadeId, recurso: c.recurso, normal: String(c.normal), emergencial: String(c.emergencial), observacao: c.observacao ?? '' } : { recurso: 'LEITOS' });
    this.ativa.set(c?.ativa ?? true);
    this.gaveta.set({ item: c });
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected salvar(item: CapacidadeEmergencialDto | null): void {
    const f = this.f();
    if (!f['unidadeId'] || f['normal'] === undefined || f['normal'] === '' || f['emergencial'] === undefined || f['emergencial'] === '') {
      this.erroGaveta.set('Informe a unidade, a capacidade normal e quanto se pode ativar.');
      return;
    }
    this.service.salvarCapacidade(item?.uuid ?? null, {
      unidadeId: f['unidadeId'], recurso: (f['recurso'] || 'LEITOS') as RecursoCapacidade, normal: Number(f['normal']), emergencial: Number(f['emergencial']),
      observacao: (f['observacao'] ?? '').trim() || null, ativa: item ? this.ativa() : null,
    }).subscribe({
      next: () => {
        this.gaveta.set(null);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.erroGaveta.set(e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar.')),
    });
  }

  private carregar(): void {
    this.service.capacidades().subscribe({ next: (l) => this.lista.set(l), error: (e: HttpErrorResponse) => this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar.') });
  }
}
