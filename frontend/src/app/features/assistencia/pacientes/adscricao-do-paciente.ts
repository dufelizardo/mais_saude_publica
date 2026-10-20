import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AdscricaoDoPacienteDto } from '../../../core/models/adscricao';
import { EquipeResumoDto, TIPOS_EQUIPE } from '../../../core/models/equipe';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { EquipeService } from '../../../core/services/equipe';
import { TerritorioService } from '../../../core/services/territorio';
import { Drawer } from '../../../shared/drawer/drawer';

/**
 * Equipe de referência do paciente (ADR-0128), no resumo da ficha: a adscrição vigente (pelo domicílio ou manual), onde
 * mora, a microárea com o ACS e o histórico. Quem cadastra o território registra a adscrição manual (sem domicílio no
 * território, por escolha) e a encerra.
 */
@Component({
  selector: 'app-adscricao-do-paciente',
  imports: [ReactiveFormsModule, RouterLink, Drawer],
  template: `
    <div class="section-title" style="font-size: 0.7rem; font-weight: 600; color: var(--ink-500); letter-spacing: 0.06em; text-transform: uppercase; padding: 0 var(--s-5); margin-bottom: 10px;">
      Equipe de referência
    </div>
    <div style="padding: 0 var(--s-5) var(--s-4);" aria-label="Equipe de referência">
      @if (adscricao() === undefined) {
        <p class="cell-sub">Carregando…</p>
      } @else if (adscricao() === null) {
        <p class="cell-sub">Sem acesso à equipe de referência deste paciente.</p>
      } @else if (adscricao(); as a) {
        @if (a.atual; as at) {
          <p>
            <strong>{{ at.equipeNome }}</strong> · {{ tiposEquipe[at.equipeTipo].sigla }} · {{ at.unidadeNome }}
            @if (at.origem === 'MANUAL') { <span class="tag tag--purple">Manual</span> } @else { <span class="tag tag--info">Pelo domicílio</span> }
          </p>
          <span class="cell-sub">
            desde {{ data(at.inicio) }}@if (a.microareaCodigo) { · microárea {{ a.microareaCodigo }} }@if (a.acsNome) { · ACS {{ a.acsNome }} }@if (at.motivo) { · {{ at.motivo }} }
          </span>
        } @else {
          <p class="cell-sub">Sem equipe de referência: o paciente não mora em domicílio cadastrado de área com equipe.</p>
        }
        @if (a.domicilio) {
          <p class="cell-sub">Mora em {{ a.domicilio }}@if (a.areaId) { · <a routerLink="/administrativo/territorio" [queryParams]="{ area: a.areaId }">ver no Território</a> }</p>
        }
        @if (podeCadastrar()) {
          <div class="row-actions" style="justify-content: flex-start; margin-top: var(--s-2);">
            @if (a.atual?.origem === 'MANUAL') {
              <button type="button" class="btn btn--ghost btn--sm" (click)="abrir('encerrar')">Encerrar adscrição manual</button>
            } @else {
              <button type="button" class="btn btn--ghost btn--sm" (click)="abrir('manual')">Adscrever manualmente</button>
            }
          </div>
        }
        @if (a.historico.length) {
          <details style="margin-top: var(--s-2);">
            <summary class="cell-sub" style="cursor: pointer;">Histórico ({{ a.historico.length }})</summary>
            <ul class="tl" aria-label="Histórico de adscrição">
              @for (h of a.historico; track h.uuid) {
                <li class="tl-row">{{ h.equipeNome }}@if (h.microareaCodigo) { · microárea {{ h.microareaCodigo }} } · {{ data(h.inicio) }} a {{ data(h.fim) }}@if (h.motivoFim) { <span class="cell-sub">{{ h.motivoFim }}</span> }</li>
              }
            </ul>
          </details>
        }
      }
    </div>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g === 'manual' ? 'Adscrição manual' : 'Encerrar a adscrição manual'" (fechar)="gaveta.set(null)">
        <form class="dw-form" [formGroup]="form" (ngSubmit)="salvar()" novalidate>
          <div class="dw-body">
            @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
            @if (g === 'manual') {
              <p class="cell-sub">Para quem não mora em domicílio do território (população em situação de rua, por exemplo) ou escolheu outra equipe. A adscrição pelo domicílio não substitui a manual.</p>
              <div class="dw-field">
                <label for="f-equipeId">Equipe <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <select id="f-equipeId" formControlName="equipeId" [attr.aria-invalid]="!!erro('equipeId')" aria-describedby="e-equipeId">
                  <option value="">Escolha…</option>
                  @for (e of equipes(); track e.uuid) { <option [value]="e.uuid">{{ e.nome }} · {{ tiposEquipe[e.tipo].sigla }} · {{ e.unidadeNome }}</option> }
                </select>
                @if (erro('equipeId')) { <p class="dw-err" id="e-equipeId">{{ erro('equipeId') }}</p> }
              </div>
            } @else {
              <p class="cell-sub">Encerrada a manual, o paciente volta à equipe do domicílio, se mora em área com equipe.</p>
            }
            <div class="dw-field">
              <label for="f-motivo">Motivo <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
              <input id="f-motivo" type="text" maxlength="400" formControlName="motivo" [attr.aria-invalid]="!!erro('motivo')" aria-describedby="e-motivo" />
              @if (erro('motivo')) { <p class="dw-err" id="e-motivo">{{ erro('motivo') }}</p> }
            </div>
          </div>
          <footer class="dw-foot">
            <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button>
            <button type="submit" class="btn" [disabled]="submitting()">{{ submitting() ? 'Salvando…' : 'Salvar' }}</button>
          </footer>
        </form>
      </app-drawer>
    }
  `,
})
export class AdscricaoDoPaciente {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(TerritorioService);
  private readonly equipeService = inject(EquipeService);

  readonly pacienteId = input.required<string>();
  readonly podeCadastrar = input(false);

  protected readonly tiposEquipe = TIPOS_EQUIPE;
  /** `undefined` carregando, `null` sem acesso. */
  protected readonly adscricao = signal<AdscricaoDoPacienteDto | null | undefined>(undefined);
  protected readonly equipes = signal<EquipeResumoDto[]>([]);
  protected readonly gaveta = signal<'manual' | 'encerrar' | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly form = this.fb.nonNullable.group({ equipeId: '', motivo: '' });

  private readonly id = computed(() => this.pacienteId());

  constructor() {
    effect(() => {
      const id = this.id();
      untracked(() => this.carregar(id));
    });
  }

  private carregar(id: string): void {
    this.adscricao.set(undefined);
    this.service.adscricao(id).pipe(catchError(() => of(null))).subscribe((a) => {
      if (this.id() === id) this.adscricao.set(a);
    });
  }

  protected data(iso?: string | null): string {
    if (!iso) return '—';
    const [a, m, d] = iso.split('-');
    return `${d}/${m}/${a}`;
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  protected abrir(tipo: 'manual' | 'encerrar'): void {
    this.form.reset({ equipeId: '', motivo: '' });
    this.erros.set({});
    this.erroApi.set(null);
    if (tipo === 'manual' && !this.equipes().length) {
      this.equipeService.listar().pipe(map((r) => r.lista.filter((e) => e.ativa)), catchError(() => of([]))).subscribe((e) => this.equipes.set(e));
    }
    this.gaveta.set(tipo);
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    if (g === 'manual' && !v.equipeId) erros['equipeId'] = 'Escolha a equipe.';
    if (!v.motivo.trim()) erros['motivo'] = 'Informe o motivo.';
    this.erros.set(erros);
    if (Object.keys(erros).length) return;
    this.submitting.set(true);
    const req = g === 'manual'
      ? this.service.adscrever(this.pacienteId(), v.equipeId, v.motivo.trim())
      : this.service.encerrarAdscricao(this.pacienteId(), v.motivo.trim());
    req.subscribe({
      next: (a) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.adscricao.set(a);
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }
}
