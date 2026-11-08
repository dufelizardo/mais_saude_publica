import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { CompetenciaDto } from '../../../core/models/educacao-permanente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { EducacaoPermanenteService } from '../../../core/services/educacao-permanente';
import { Drawer } from '../../../shared/drawer/drawer';

/** Aba Competências da tela Desenvolvimento (ADR-0173). */
@Component({
  selector: 'app-competencias',
  imports: [ReactiveFormsModule, Drawer],
  template: `
    <section class="card" id="pn-competencias" role="tabpanel" aria-labelledby="tab-competencias">
      <div class="card__head">
        <div><h2>Competências</h2><p class="sub">O que os treinamentos desenvolvem; inativa não entra em treinamento novo</p></div>
        @if (podeEditar()) { <button type="button" class="btn btn--sm" (click)="abrir(null)">Nova competência</button> }
      </div>
      @if (!competencias().length) {
        <div class="empty"><strong>Nenhuma competência no catálogo</strong></div>
      } @else {
        <div class="tbl-wrap">
          <table>
            <thead><tr><th scope="col">Competência</th><th scope="col" class="num">Treinamentos</th><th scope="col">Situação</th>@if (podeEditar()) { <th scope="col" class="num">Ações</th> }</tr></thead>
            <tbody>
              @for (c of competencias(); track c.uuid) {
                <tr>
                  <td>
                    <span class="cell-main">{{ c.nome }} <span class="cell-sub mono" style="display: inline;">{{ c.codigo }}</span></span>
                    @if (c.descricao) { <span class="cell-sub">{{ c.descricao }}</span> }
                  </td>
                  <td class="num mono">{{ usos()[c.uuid] ?? 0 }}</td>
                  <td>@if (c.ativo) { <span class="badge badge--ok"><span class="dot" aria-hidden="true"></span> Ativa</span> } @else { <span class="badge badge--muted">Inativa</span> }</td>
                  @if (podeEditar()) { <td class="num"><button type="button" class="btn btn--ghost btn--sm" (click)="abrir(c)" [attr.aria-label]="'Editar ' + c.nome">Editar</button></td> }
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </section>
    @if (aberta()) {
      <app-drawer [titulo]="item() ? 'Editar competência' : 'Nova competência'" (fechar)="aberta.set(false)">
        <form class="dw-form" [formGroup]="form" (ngSubmit)="salvar()" novalidate>
          <div class="dw-body">
            @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
            <div class="dw-row">
              <div class="dw-field">
                <label for="f-codigoCompetencia">Código <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-codigoCompetencia" type="text" class="mono" maxlength="60" formControlName="codigo" autocomplete="off" [attr.aria-invalid]="!!erros()['codigoCompetencia']" aria-describedby="e-codigoCompetencia" />
                @if (erros()['codigoCompetencia']) { <p class="dw-err" id="e-codigoCompetencia">{{ erros()['codigoCompetencia'] }}</p> }
              </div>
              <div class="dw-field">
                <label for="f-nomeCompetencia">Nome <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-nomeCompetencia" type="text" maxlength="120" formControlName="nome" autocomplete="off" [attr.aria-invalid]="!!erros()['nomeCompetencia']" aria-describedby="e-nomeCompetencia" />
                @if (erros()['nomeCompetencia']) { <p class="dw-err" id="e-nomeCompetencia">{{ erros()['nomeCompetencia'] }}</p> }
              </div>
            </div>
            <div class="dw-field">
              <label for="f-descricaoCompetencia">Descrição<span class="opt">opcional</span></label>
              <textarea id="f-descricaoCompetencia" rows="3" maxlength="500" formControlName="descricao"></textarea>
            </div>
            <div class="dw-field">
              <label class="switch" for="f-ativaCompetencia"><span><b style="font-weight: 500;">Ativa</b></span><input type="checkbox" role="switch" id="f-ativaCompetencia" formControlName="ativo" /></label>
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
export class Competencias {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(EducacaoPermanenteService);

  readonly competencias = input.required<CompetenciaDto[]>();
  /** Quantos treinamentos usam cada competência. */
  readonly usos = input<Record<string, number>>({});
  readonly podeEditar = input(false);
  readonly salvou = output<string>();

  protected readonly aberta = signal(false);
  protected readonly item = signal<CompetenciaDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly form = this.fb.nonNullable.group({ codigo: [''], nome: [''], descricao: [''], ativo: [true] });

  protected abrir(item: CompetenciaDto | null): void {
    this.form.reset({ codigo: item?.codigo ?? '', nome: item?.nome ?? '', descricao: item?.descricao ?? '', ativo: item?.ativo ?? true });
    this.item.set(item);
    this.erroApi.set(null);
    this.erros.set({});
    this.aberta.set(true);
  }

  protected salvar(): void {
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.codigo.trim()) erros['codigoCompetencia'] = 'Informe o código.';
    if (!v.nome.trim()) erros['nomeCompetencia'] = 'Informe o nome.';
    this.erros.set(erros);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) {
      setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
      return;
    }
    const dto = { codigo: v.codigo.trim(), nome: v.nome.trim(), descricao: v.descricao.trim() || null, ativo: v.ativo };
    const atual = this.item();
    this.submitting.set(true);
    (atual ? this.service.atualizarCompetencia(atual.uuid, dto) : this.service.criarCompetencia(dto)).subscribe({
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
