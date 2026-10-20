import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { catchError, of } from 'rxjs';
import { PacienteResponseDto } from '../../core/models/paciente';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  ContatoDto,
  NotificacaoDto,
  SITUACOES_CONTATO,
  SituacaoContato,
  TIPOS_EXPOSICAO,
  TipoExposicaoContato,
} from '../../core/models/vigilancia';
import { PacienteService } from '../../core/services/paciente';
import { VigilanciaService } from '../../core/services/vigilancia';

/**
 * Contatos do caso (ADR-0130), dentro da gaveta da notificação: a lista com o acompanhamento e o fim do monitoramento,
 * o registro (paciente cadastrado, ou nome e telefone de quem ainda não tem cadastro), a mudança de situação e
 * "Notificar", para o contato que adoeceu — a notificação dele fica ligada ao caso índice.
 */
@Component({
  selector: 'app-contatos-da-notificacao',
  imports: [ReactiveFormsModule],
  template: `
    <h4 class="mo-grupo" id="contatos">Contatos <span class="cell-sub" style="display: inline;">· {{ notificacao().contatos.length }}</span></h4>
    @if (notificacao().contatos.length) {
      <ul class="eq-prof" aria-label="Contatos do caso">
        @for (c of notificacao().contatos; track c.uuid) {
          <li style="display: block;">
            <div style="display: flex; justify-content: space-between; gap: var(--s-2); flex-wrap: wrap;">
              <span>
                <span class="cell-main">{{ c.nome }}@if (!c.pacienteId) { <span class="tag tag--muted">sem cadastro</span> }</span>
                <span class="cell-sub">{{ exposicoes[c.tipoExposicao] }}@if (c.dataExposicao) { · exposto em {{ data(c.dataExposicao) }} }@if (c.telefone) { · {{ c.telefone }} }@if (c.fimMonitoramento) { · monitorar até {{ data(c.fimMonitoramento) }} }</span>
              </span>
              <span>
                <span [class]="'tag tag--' + situacoes[c.situacao].classe">{{ situacoes[c.situacao].rotulo }}</span>
                @if (c.monitoramentoVencido) { <span class="tag tag--warn">Monitoramento vencido</span> }
              </span>
            </div>
            @if (podeInvestigar() && c.situacao !== 'VIROU_CASO') {
              <div class="row-actions" style="justify-content: flex-start; margin-top: var(--s-1); flex-wrap: wrap;">
                <select class="sel" [attr.aria-label]="'Situação de ' + c.nome" [value]="c.situacao" (change)="mudarSituacao(c, $any($event.target).value)">
                  @for (s of situacoesEditaveis; track s) { <option [value]="s">{{ situacoes[s].rotulo }}</option> }
                </select>
                <button type="button" class="btn btn--ghost btn--sm" (click)="notificar.emit(c)" [attr.aria-label]="'Notificar o contato ' + c.nome">Notificar</button>
              </div>
            }
          </li>
        }
      </ul>
    } @else {
      <p class="cell-sub">Nenhum contato registrado.</p>
    }
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (podeInvestigar()) {
      @if (!abrindo()) {
        <button type="button" class="btn btn--ghost btn--sm" (click)="abrir()">Registrar contato</button>
      } @else {
        <form [formGroup]="form" (ngSubmit)="salvar()" novalidate aria-label="Novo contato" style="margin-top: var(--s-2);">
          <div class="dw-field">
            <label for="f-contato-busca">Paciente<span class="opt">se tem cadastro</span></label>
            <input id="f-contato-busca" type="search" placeholder="Nome ou CPF…" [value]="busca()" (input)="busca.set($any($event.target).value)" autocomplete="off" />
            @if (encontrados().length) {
              <select id="f-contato-paciente" formControlName="pacienteId" aria-label="Paciente encontrado">
                <option value="">Sem cadastro</option>
                @for (p of encontrados(); track p.uuid) { <option [value]="p.uuid">{{ p.nome }}</option> }
              </select>
            }
          </div>
          @if (!form.controls.pacienteId.value) {
            <div class="dw-row">
              <div class="dw-field">
                <label for="f-contato-nome">Nome<span class="opt">sem cadastro</span></label>
                <input id="f-contato-nome" type="text" maxlength="200" formControlName="nome" />
              </div>
              <div class="dw-field">
                <label for="f-contato-telefone">Telefone</label>
                <input id="f-contato-telefone" type="text" maxlength="40" formControlName="telefone" />
              </div>
            </div>
            <div class="dw-field">
              <label for="f-contato-endereco">Endereço</label>
              <input id="f-contato-endereco" type="text" maxlength="500" formControlName="endereco" />
            </div>
          }
          <div class="dw-row">
            <div class="dw-field">
              <label for="f-contato-tipo">Exposição</label>
              <select id="f-contato-tipo" formControlName="tipoExposicao">
                @for (t of listaExposicoes; track t) { <option [value]="t">{{ exposicoes[t] }}</option> }
              </select>
            </div>
            <div class="dw-field">
              <label for="f-contato-data">Data da exposição</label>
              <input id="f-contato-data" type="date" formControlName="dataExposicao" />
            </div>
          </div>
          <div class="row-actions" style="justify-content: flex-start;">
            <button type="submit" class="btn btn--sm" [disabled]="salvando()">{{ salvando() ? 'Salvando…' : 'Salvar contato' }}</button>
            <button type="button" class="btn btn--ghost btn--sm" (click)="abrindo.set(false)">Cancelar</button>
          </div>
        </form>
      }
    }
  `,
})
export class ContatosDaNotificacao {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(VigilanciaService);
  private readonly pacienteService = inject(PacienteService);

  readonly notificacao = input.required<NotificacaoDto>();
  readonly podeInvestigar = input(false);
  readonly atualizada = output<NotificacaoDto>();
  readonly notificar = output<ContatoDto>();

  protected readonly exposicoes = TIPOS_EXPOSICAO;
  protected readonly listaExposicoes = Object.keys(TIPOS_EXPOSICAO) as TipoExposicaoContato[];
  protected readonly situacoes = SITUACOES_CONTATO;
  protected readonly situacoesEditaveis: SituacaoContato[] = ['EM_ACOMPANHAMENTO', 'SINTOMATICO', 'LIBERADO', 'PERDA_DE_SEGUIMENTO'];

  protected readonly abrindo = signal(false);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly busca = signal('');
  private readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly form = this.fb.nonNullable.group({ pacienteId: '', nome: '', telefone: '', endereco: '', tipoExposicao: 'DOMICILIAR', dataExposicao: '' });

  protected readonly encontrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    if (q.length < 2) return [];
    const digitos = q.replace(/\D/g, '');
    return this.pacientes().filter((p) => p.uuid !== this.notificacao().pacienteId
      && (p.nome.toLowerCase().includes(q) || (digitos.length >= 3 && (p.cpf ?? '').includes(digitos)))).slice(0, 15);
  });

  protected data(iso?: string | null): string {
    if (!iso) return '—';
    const [a, m, d] = iso.split('-');
    return `${d}/${m}/${a}`;
  }

  protected abrir(): void {
    this.form.reset({ pacienteId: '', nome: '', telefone: '', endereco: '', tipoExposicao: 'DOMICILIAR', dataExposicao: '' });
    this.busca.set('');
    this.erro.set(null);
    this.abrindo.set(true);
    if (!this.pacientes().length) this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
  }

  protected salvar(): void {
    const v = this.form.getRawValue();
    if (!v.pacienteId && !v.nome.trim()) {
      this.erro.set('Sem cadastro, informe o nome do contato.');
      return;
    }
    this.salvando.set(true);
    this.service.adicionarContato(this.notificacao().uuid, {
      pacienteId: v.pacienteId || null, nome: v.nome.trim() || null, telefone: v.telefone.trim() || null, endereco: v.endereco.trim() || null,
      tipoExposicao: v.tipoExposicao as TipoExposicaoContato, dataExposicao: v.dataExposicao || null,
    }).subscribe({
      next: (n) => {
        this.salvando.set(false);
        this.abrindo.set(false);
        this.erro.set(null);
        this.atualizada.emit(n);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected mudarSituacao(c: ContatoDto, situacao: SituacaoContato): void {
    this.service.atualizarContato(c.uuid, {
      pacienteId: c.pacienteId ?? null, nome: c.pacienteId ? null : c.nome, telefone: c.telefone ?? null, endereco: c.endereco ?? null,
      tipoExposicao: c.tipoExposicao, dataExposicao: c.dataExposicao ?? null, situacao, observacao: c.observacao ?? null,
    }).subscribe({ next: (n) => this.atualizada.emit(n), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  private falhou(e: HttpErrorResponse): void {
    this.salvando.set(false);
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar o contato.');
  }
}
