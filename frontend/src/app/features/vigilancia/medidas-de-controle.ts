import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  MedidaControleDto,
  SITUACOES_MEDIDA,
  SituacaoMedidaControle,
  TIPOS_MEDIDA,
  TipoMedidaControle,
} from '../../core/models/vigilancia';
import { VigilanciaService } from '../../core/services/vigilancia';

/**
 * Medidas de controle (ADR-0131) de um surto ou de um caso: a lista com o andamento e o prazo, o registro e a mudança de
 * situação. Concluir pede o resultado; concluída ou cancelada, a medida não muda.
 */
@Component({
  selector: 'app-medidas-de-controle',
  imports: [ReactiveFormsModule],
  template: `
    @if (medidas().length) {
      <ul class="eq-prof" aria-label="Medidas de controle">
        @for (m of medidas(); track m.uuid) {
          <li style="display: block;">
            <div style="display: flex; justify-content: space-between; gap: var(--s-2); flex-wrap: wrap;">
              <span>
                <span class="cell-main">{{ tipos[m.tipo] }}</span>
                <span class="cell-sub">{{ m.descricao }}@if (m.responsavelNome) { · {{ m.responsavelNome }} }@if (m.prazo) { · prazo {{ data(m.prazo) }} }@if (m.resultado) { · {{ m.resultado }} }</span>
              </span>
              <span>
                <span [class]="'tag tag--' + situacoes[m.situacao].classe">{{ situacoes[m.situacao].rotulo }}</span>
                @if (m.vencida) { <span class="tag tag--warn">Prazo vencido</span> }
              </span>
            </div>
            @if (podeEditar() && (m.situacao === 'PLANEJADA' || m.situacao === 'EM_ANDAMENTO')) {
              <div class="row-actions" style="justify-content: flex-start; margin-top: var(--s-1); flex-wrap: wrap;">
                @if (concluindo() === m.uuid) {
                  <input type="text" maxlength="2000" placeholder="Resultado da medida" [attr.aria-label]="'Resultado de ' + tipos[m.tipo]"
                    [value]="resultado()" (input)="resultado.set($any($event.target).value)" />
                  <button type="button" class="btn btn--sm" (click)="mudar(m, 'CONCLUIDA')" [disabled]="!resultado().trim()">Concluir</button>
                  <button type="button" class="btn btn--ghost btn--sm" (click)="concluindo.set(null)">Voltar</button>
                } @else {
                  @if (m.situacao === 'PLANEJADA') {
                    <button type="button" class="btn btn--ghost btn--sm" (click)="mudar(m, 'EM_ANDAMENTO')" [attr.aria-label]="'Iniciar ' + tipos[m.tipo]">Iniciar</button>
                  }
                  <button type="button" class="btn btn--ghost btn--sm" (click)="concluindo.set(m.uuid); resultado.set('')" [attr.aria-label]="'Concluir ' + tipos[m.tipo]">Concluir…</button>
                  <button type="button" class="btn btn--ghost btn--sm" (click)="mudar(m, 'CANCELADA')" [attr.aria-label]="'Cancelar ' + tipos[m.tipo]">Cancelar</button>
                }
              </div>
            }
          </li>
        }
      </ul>
    } @else {
      <p class="cell-sub">Nenhuma medida registrada.</p>
    }
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (podeEditar()) {
      @if (!abrindo()) {
        <button type="button" class="btn btn--ghost btn--sm" (click)="abrir()">Registrar medida</button>
      } @else {
        <form [formGroup]="form" (ngSubmit)="salvar()" novalidate aria-label="Nova medida" style="margin-top: var(--s-2);">
          <div class="dw-row">
            <div class="dw-field">
              <label for="f-medida-tipo">Medida</label>
              <select id="f-medida-tipo" formControlName="tipo">
                @for (t of listaTipos; track t) { <option [value]="t">{{ tipos[t] }}</option> }
              </select>
            </div>
            <div class="dw-field">
              <label for="f-medida-prazo">Prazo<span class="opt">opcional</span></label>
              <input id="f-medida-prazo" type="date" formControlName="prazo" />
            </div>
          </div>
          <div class="dw-field">
            <label for="f-medida-descricao">Descrição</label>
            <input id="f-medida-descricao" type="text" maxlength="1000" formControlName="descricao" placeholder="O que fazer, onde e para quem" />
          </div>
          <div class="dw-field">
            <label for="f-medida-responsavel">Matrícula do responsável<span class="opt">em branco, você</span></label>
            <input id="f-medida-responsavel" type="text" formControlName="responsavelMatricula" />
          </div>
          <div class="row-actions" style="justify-content: flex-start;">
            <button type="submit" class="btn btn--sm" [disabled]="salvando()">{{ salvando() ? 'Salvando…' : 'Salvar medida' }}</button>
            <button type="button" class="btn btn--ghost btn--sm" (click)="abrindo.set(false)">Cancelar</button>
          </div>
        </form>
      }
    }
  `,
})
export class MedidasDeControle {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(VigilanciaService);

  readonly medidas = input<MedidaControleDto[]>([]);
  readonly podeEditar = input(false);
  readonly surtoId = input<string | null>(null);
  readonly notificacaoId = input<string | null>(null);
  readonly alterada = output<void>();

  protected readonly tipos = TIPOS_MEDIDA;
  protected readonly listaTipos = Object.keys(TIPOS_MEDIDA) as TipoMedidaControle[];
  protected readonly situacoes = SITUACOES_MEDIDA;

  protected readonly abrindo = signal(false);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly concluindo = signal<string | null>(null);
  protected readonly resultado = signal('');
  protected readonly form = this.fb.nonNullable.group({ tipo: 'BLOQUEIO_VACINAL', descricao: '', prazo: '', responsavelMatricula: '' });

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected abrir(): void {
    this.form.reset({ tipo: 'BLOQUEIO_VACINAL', descricao: '', prazo: '', responsavelMatricula: '' });
    this.erro.set(null);
    this.abrindo.set(true);
  }

  protected salvar(): void {
    const v = this.form.getRawValue();
    if (!v.descricao.trim()) {
      this.erro.set('Descreva a medida.');
      return;
    }
    const dto = { tipo: v.tipo as TipoMedidaControle, descricao: v.descricao.trim(), prazo: v.prazo || null,
      responsavelMatricula: v.responsavelMatricula.trim() || null };
    const surto = this.surtoId();
    const req = surto ? this.service.medidaDoSurto(surto, dto) : this.service.medidaDoCaso(this.notificacaoId()!, dto);
    this.salvando.set(true);
    req.subscribe({
      next: () => {
        this.salvando.set(false);
        this.abrindo.set(false);
        this.erro.set(null);
        this.alterada.emit();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected mudar(m: MedidaControleDto, situacao: SituacaoMedidaControle): void {
    this.service.atualizarMedida(m.uuid, {
      tipo: m.tipo, descricao: m.descricao, prazo: m.prazo ?? null, situacao,
      resultado: situacao === 'CONCLUIDA' ? this.resultado().trim() : m.resultado ?? null,
    }).subscribe({
      next: () => {
        this.concluindo.set(null);
        this.erro.set(null);
        this.alterada.emit();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private falhou(e: HttpErrorResponse): void {
    this.salvando.set(false);
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar a medida.');
  }
}
