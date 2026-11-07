import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TipoDocumentoDto } from '../../../core/models/documento';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { DocumentoService } from '../../../core/services/documento';
import { Drawer } from '../../../shared/drawer/drawer';

/** Aba Tipos de documento do Modelo administrativo (ADR-0172). */
@Component({
  selector: 'app-tipos-de-documento',
  imports: [ReactiveFormsModule, Drawer],
  template: `
    <section class="card" id="pn-documentos" role="tabpanel" aria-labelledby="tab-documentos">
      <div class="card__head">
        <div><h2>Tipos de documento</h2><p class="sub">O que pode ser anexado ao paciente; inativo não entra em documento novo</p></div>
        @if (podeEditar()) { <button type="button" class="btn btn--sm" (click)="abrir(null)">Novo tipo</button> }
      </div>
      <div class="tbl-wrap">
        <table>
          <thead><tr><th scope="col">Tipo</th><th scope="col">Situação</th>@if (podeEditar()) { <th scope="col" class="num">Ações</th> }</tr></thead>
          <tbody>
            @for (t of tipos(); track t.uuid) {
              <tr>
                <td><span class="cell-main">{{ t.nome }} <span class="cell-sub mono" style="display: inline;">{{ t.codigo }}</span></span></td>
                <td>@if (t.ativo) { <span class="badge badge--ok"><span class="dot" aria-hidden="true"></span> Ativo</span> } @else { <span class="badge badge--muted">Inativo</span> }</td>
                @if (podeEditar()) { <td class="num"><button type="button" class="btn btn--ghost btn--sm" (click)="abrir(t)" [attr.aria-label]="'Editar ' + t.nome">Editar</button></td> }
              </tr>
            }
          </tbody>
        </table>
      </div>
    </section>
    @if (aberta()) {
      <app-drawer [titulo]="item() ? 'Editar tipo de documento' : 'Novo tipo de documento'" (fechar)="aberta.set(false)">
        <form class="dw-form" [formGroup]="form" (ngSubmit)="salvar()" novalidate>
          <div class="dw-body">
            @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
            <div class="dw-row">
              <div class="dw-field">
                <label for="f-codigoTipoDoc">Código <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-codigoTipoDoc" type="text" class="mono" maxlength="60" formControlName="codigo" autocomplete="off" />
              </div>
              <div class="dw-field">
                <label for="f-nomeTipoDoc">Nome <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-nomeTipoDoc" type="text" maxlength="100" formControlName="nome" autocomplete="off" />
              </div>
            </div>
            <div class="dw-field">
              <label class="switch" for="f-ativoTipoDoc"><span><b style="font-weight: 500;">Ativo</b></span><input type="checkbox" role="switch" id="f-ativoTipoDoc" formControlName="ativo" /></label>
            </div>
          </div>
          <footer class="dw-foot">
            <button type="button" class="btn btn--ghost" (click)="aberta.set(false)">Cancelar</button>
            <button type="submit" class="btn" [disabled]="submitting()">{{ submitting() ? 'Salvando…' : 'Salvar' }}</button>
          </footer>
        </form>
      </app-drawer>
    }
  `,
})
export class TiposDeDocumento {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(DocumentoService);

  readonly tipos = input.required<TipoDocumentoDto[]>();
  readonly podeEditar = input(false);
  readonly salvou = output<string>();

  protected readonly aberta = signal(false);
  protected readonly item = signal<TipoDocumentoDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly form = this.fb.nonNullable.group({ codigo: [''], nome: [''], ativo: [true] });

  protected abrir(item: TipoDocumentoDto | null): void {
    this.form.reset({ codigo: item?.codigo ?? '', nome: item?.nome ?? '', ativo: item?.ativo ?? true });
    this.item.set(item);
    this.erroApi.set(null);
    this.aberta.set(true);
  }

  protected salvar(): void {
    const v = this.form.getRawValue();
    if (!v.codigo.trim() || !v.nome.trim()) {
      this.erroApi.set('Informe o código e o nome.');
      return;
    }
    const dto = { codigo: v.codigo.trim(), nome: v.nome.trim(), ativo: v.ativo };
    const atual = this.item();
    this.submitting.set(true);
    (atual ? this.service.atualizarTipo(atual.uuid, dto) : this.service.criarTipo(dto)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.aberta.set(false);
        this.salvou.emit(dto.nome);
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }
}
