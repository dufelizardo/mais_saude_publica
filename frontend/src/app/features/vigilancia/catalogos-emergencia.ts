import { HttpErrorResponse } from '@angular/common/http';
import { Component, input, output, signal } from '@angular/core';
import { inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CategoriaTipoEmergencia, NivelEmergenciaDto, TipoEmergenciaDto } from '../../core/models/emergencia';
import { ErrorResponseDto } from '../../core/models/profissional';
import { EmergenciaService } from '../../core/services/emergencia';
import { Drawer } from '../../shared/drawer/drawer';

type Gaveta = { tipo: 'tipo'; item: TipoEmergenciaDto | null } | { tipo: 'nivel'; item: NivelEmergenciaDto | null };

/**
 * Catálogos das emergências (ADR-0157, pendência da ADR-0156): os tipos (com o COBRADE) e os níveis de resposta. O
 * código do tipo e a ordem do nível não mudam; inativar tira do uso sem apagar.
 */
@Component({
  selector: 'app-catalogos-emergencia',
  imports: [Drawer],
  template: `
    <div class="ce-grade">
      <section aria-label="Tipos de emergência">
        <div class="ce-cab"><h3 class="mo-grupo">Tipos de emergência</h3>@if (podeGerenciar()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'tipo', item: null })">Novo tipo</button> }</div>
        <div class="tbl-wrap card">
          <table aria-label="Tipos de emergência">
            <thead><tr><th scope="col">Tipo</th><th scope="col">COBRADE</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (t of tipos(); track t.uuid) {
                <tr>
                  <td><span class="cell-main">{{ t.nome }}@if (!t.ativo) { <span class="tag tag--muted">Inativo</span> }</span><span class="cell-sub mono">{{ t.codigo }} · {{ t.categoria === 'DESASTRE' ? 'Desastre' : 'Saúde' }}</span></td>
                  <td class="mono">{{ t.cobrade ?? '—' }}</td>
                  <td class="row-actions">@if (podeGerenciar()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'tipo', item: t })" [attr.aria-label]="'Editar tipo ' + t.nome">Editar</button> }</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </section>
      <section aria-label="Níveis de resposta">
        <div class="ce-cab"><h3 class="mo-grupo">Níveis de resposta</h3>@if (podeGerenciar()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'nivel', item: null })">Novo nível</button> }</div>
        <div class="tbl-wrap card">
          <table aria-label="Níveis de resposta">
            <thead><tr><th scope="col">Nível</th><th scope="col">Descrição</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (n of niveis(); track n.uuid) {
                <tr>
                  <td><span class="cell-main">{{ n.nome }}@if (!n.ativo) { <span class="tag tag--muted">Inativo</span> }</span><span class="cell-sub">ordem {{ n.ordem }}</span></td>
                  <td class="cell-sub">{{ n.descricao ?? '—' }}</td>
                  <td class="row-actions">@if (podeGerenciar()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'nivel', item: n })" [attr.aria-label]="'Editar nível ' + n.nome">Editar</button> }</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </section>
    </div>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g.tipo === 'tipo' ? (g.item ? 'Editar tipo' : 'Novo tipo de emergência') : (g.item ? 'Editar nível' : 'Novo nível de resposta')" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
            @if (g.tipo === 'tipo') {
              <div class="dw-row">
                <div class="dw-field"><label for="f-ce-codigo">Código <span class="req" aria-hidden="true">*</span></label><input id="f-ce-codigo" type="text" maxlength="40" [disabled]="!!g.item" [value]="f()['codigo'] ?? ''" (input)="set('codigo', $event)" /></div>
                <div class="dw-field"><label for="f-ce-categoria">Categoria</label>
                  <select id="f-ce-categoria" (change)="f.set({ ...f(), categoria: $any($event.target).value })">
                    <option value="DESASTRE" [selected]="f()['categoria'] === 'DESASTRE'">Desastre</option>
                    <option value="SAUDE" [selected]="f()['categoria'] === 'SAUDE'">Saúde</option>
                  </select></div>
              </div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-ce-nome">Nome <span class="req" aria-hidden="true">*</span></label><input id="f-ce-nome" type="text" maxlength="120" [value]="f()['nome'] ?? ''" (input)="set('nome', $event)" /></div>
                <div class="dw-field"><label for="f-ce-cobrade">COBRADE<span class="opt">opcional</span></label><input id="f-ce-cobrade" type="text" maxlength="20" [value]="f()['cobrade'] ?? ''" (input)="set('cobrade', $event)" placeholder="Ex.: 1.2.1.0.0" /></div>
              </div>
            } @else {
              <div class="dw-row">
                <div class="dw-field"><label for="f-ce-ordem">Ordem <span class="req" aria-hidden="true">*</span></label><input id="f-ce-ordem" type="number" min="0" [disabled]="!!g.item" [value]="f()['ordem'] ?? ''" (input)="set('ordem', $event)" /></div>
                <div class="dw-field"><label for="f-ce-nome">Nome <span class="req" aria-hidden="true">*</span></label><input id="f-ce-nome" type="text" maxlength="120" [value]="f()['nome'] ?? ''" (input)="set('nome', $event)" /></div>
              </div>
              <div class="dw-field"><label for="f-ce-descricao">Descrição<span class="opt">opcional</span></label><input id="f-ce-descricao" type="text" maxlength="500" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)" /></div>
            }
            @if (g.item) {
              <div class="dw-field"><label class="switch" for="f-ce-ativo"><span><b style="font-weight: 500;">Ativo</b><span class="cell-sub">Inativo não entra em evento ou mudança de nível novos.</span></span><input type="checkbox" role="switch" id="f-ce-ativo" [checked]="ativo()" (change)="ativo.set($any($event.target).checked)" /></label></div>
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn">Salvar</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .ce-grade { display: grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); gap: var(--s-5); }
    .ce-cab { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--s-2); }
    .ce-grade table { min-width: 0; width: 100%; }
  `,
})
export class CatalogosEmergencia {
  private readonly service = inject(EmergenciaService);

  readonly tipos = input<TipoEmergenciaDto[]>([]);
  readonly niveis = input<NivelEmergenciaDto[]>([]);
  readonly podeGerenciar = input(false);
  readonly alterado = output<void>();

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly ativo = signal(true);

  protected abrir(g: Gaveta): void {
    this.erro.set(null);
    if (g.tipo === 'tipo') {
      const t = g.item;
      this.f.set({ codigo: t?.codigo ?? '', nome: t?.nome ?? '', categoria: t?.categoria ?? 'DESASTRE', cobrade: t?.cobrade ?? '' });
      this.ativo.set(t?.ativo ?? true);
    } else {
      const n = g.item;
      this.f.set({ ordem: n != null ? String(n.ordem) : '', nome: n?.nome ?? '', descricao: n?.descricao ?? '' });
      this.ativo.set(n?.ativo ?? true);
    }
    this.gaveta.set(g);
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected salvar(g: Gaveta): void {
    const f = this.f();
    const t = (k: string) => (f[k] ?? '').trim();
    let req: Observable<unknown>;
    if (g.tipo === 'tipo') {
      if (!t('codigo') || !t('nome')) {
        this.erro.set('Informe o código e o nome.');
        return;
      }
      req = this.service.salvarTipo(g.item?.uuid ?? null, {
        codigo: t('codigo'), nome: t('nome'), categoria: (t('categoria') || 'DESASTRE') as CategoriaTipoEmergencia, cobrade: t('cobrade') || null,
        ativo: g.item ? this.ativo() : null,
      });
    } else {
      if (t('ordem') === '' || !t('nome')) {
        this.erro.set('Informe a ordem e o nome.');
        return;
      }
      req = this.service.salvarNivel(g.item?.uuid ?? null, { ordem: Number(t('ordem')), nome: t('nome'), descricao: t('descricao') || null, ativo: g.item ? this.ativo() : null });
    }
    req.subscribe({
      next: () => {
        this.gaveta.set(null);
        this.alterado.emit();
      },
      error: (e: HttpErrorResponse) => this.erro.set(e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar.')),
    });
  }
}
