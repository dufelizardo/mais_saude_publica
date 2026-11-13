import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal, viewChild } from '@angular/core';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  AGRAVO_ACIDENTE_GRAVE,
  AGRAVOS_ACIDENTE,
  AcompanhamentoSorologicoDto,
  ETAPAS_ACOMPANHAMENTO,
  NotificacaoDto,
  RESPOSTAS_CAT,
  SITUACOES_TRABALHO,
  TIPOS_ACIDENTE,
} from '../../core/models/vigilancia';
import { VigilanciaService } from '../../core/services/vigilancia';
import { InvestigacaoTrabalho } from './investigacao-trabalho';
import { TrabalhoForm } from './trabalho-form';

/**
 * No detalhe da notificação (ADR-0207): o bloco do trabalho, a correção pela investigação e, no acidente com material biológico, o
 * acompanhamento sorológico com as datas previstas e o registro de cada etapa.
 */
@Component({
  selector: 'app-trabalho-notificacao',
  imports: [TrabalhoForm, InvestigacaoTrabalho],
  template: `
    @if (notificacao().trabalho || (podeInvestigar() && aberta())) {
      <section aria-label="Saúde do trabalhador">
        <h4 class="mo-grupo">Trabalho</h4>
        @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
        @if (editando()) {
          <app-trabalho-form [inicial]="notificacao().trabalho ?? null" [acidente]="acidente()" [grave]="grave()" />
          <div class="row-actions" style="margin-top: var(--s-2);">
            <button type="button" class="btn btn--ghost btn--sm" (click)="editando.set(false)">Cancelar</button>
            <button type="button" class="btn btn--sm" [disabled]="salvando()" (click)="salvar()">{{ salvando() ? 'Salvando…' : 'Salvar o bloco' }}</button>
          </div>
        } @else if (notificacao().trabalho; as t) {
          <dl class="info-list" aria-label="Bloco do trabalho">
            <div><dt>Ocupação</dt><dd><span class="mono">{{ t.ocupacaoCbo }}</span> · {{ t.ocupacaoDescricao }}</dd></div>
            <div><dt>Situação</dt><dd>{{ situacoes[t.situacaoTrabalho] }}</dd></div>
            @if (t.empregadorNome || t.empregadorDocumento) {
              <div><dt>Empregador</dt><dd>{{ t.empregadorNome ?? '—' }}@if (t.empregadorDocumento) { · <span class="mono">{{ t.empregadorDocumento }}</span> }@if (t.empregadorCnae) { · CNAE <span class="mono">{{ t.empregadorCnae }}</span> }@if (t.estabelecimentoId) { <span class="tag tag--info">Na vigilância sanitária</span> }</dd></div>
            }
            @if (t.localEvento) { <div><dt>Local</dt><dd>{{ t.localEvento }}</dd></div> }
            @if (t.acidente) {
              <div><dt>Acidente</dt><dd>{{ t.tipoAcidente ? tipos[t.tipoAcidente] : 'tipo não informado' }}@if (t.horasAposInicioJornada != null) { · {{ t.horasAposInicioJornada }}h de jornada }@if (t.cat) { · CAT {{ cats[t.cat].toLowerCase() }} }@if (t.outrosTrabalhadoresAtingidos) { · {{ t.outrosTrabalhadoresAtingidos }} outro(s) atingido(s) }@if (t.obito) { <span class="tag tag--alert">Óbito</span> }</dd></div>
            }
            @if (t.acidenteTrabalhoId) { <div><dt>Servidor</dt><dd>Acidente registrado no RH em {{ dataHora(t.acidenteTrabalhoDataHora) }}</dd></div> }
          </dl>
          @if (t.materialBiologico) {
            <h4 class="mo-grupo">Acompanhamento sorológico</h4>
            <ul class="eq-prof" aria-label="Acompanhamento sorológico">
              @for (a of t.acompanhamentos; track a.etapa) {
                <li class="mo-row">
                  <span><span class="cell-main">{{ etapas[a.etapa] }}</span>
                    <span class="cell-sub">prevista em {{ data(a.prevista) }}@if (a.realizadoEm) { · realizada em {{ data(a.realizadoEm) }} }</span></span>
                  @if (a.realizadoEm) { <span class="tag tag--ok">Feita</span> }
                  @else if (a.vencida) { <span class="tag tag--alert">Vencida</span> }
                  @if (!a.realizadoEm && aberta() && podeRegistrar()) {
                    @if (registrando() === a.etapa) {
                      <span class="row-actions">
                        <label class="sr-only" [for]="'f-acomp-' + a.etapa">Data da etapa {{ etapas[a.etapa] }}</label>
                        <input [id]="'f-acomp-' + a.etapa" type="date" [value]="dataEtapa()" (input)="dataEtapa.set($any($event.target).value)" />
                        <button type="button" class="btn btn--sm" [disabled]="salvando() || !dataEtapa()" (click)="registrar(a)">Registrar</button>
                      </span>
                    } @else {
                      <button type="button" class="link-btn" (click)="registrando.set(a.etapa); dataEtapa.set(hoje())" [attr.aria-label]="'Registrar etapa ' + etapas[a.etapa]">Registrar</button>
                    }
                  }
                </li>
              }
            </ul>
          }
          @if (podeInvestigar() && aberta()) { <button type="button" class="link-btn" (click)="editando.set(true)">Corrigir o bloco do trabalho</button> }
          <app-investigacao-trabalho [notificacao]="notificacao()" [podeInvestigar]="podeInvestigar()" (atualizada)="atualizada.emit($event)" />
        } @else {
          <p class="cell-sub">Sem o bloco do trabalho. Se o caso tem relação com o trabalho, inclua.</p>
          <button type="button" class="link-btn" (click)="editando.set(true)">Incluir o bloco do trabalho</button>
        }
      </section>
    }
  `,
})
export class TrabalhoNotificacao {
  private readonly service = inject(VigilanciaService);

  readonly notificacao = input.required<NotificacaoDto>();
  readonly podeInvestigar = input(false);
  readonly podeNotificar = input(false);
  readonly atualizada = output<NotificacaoDto>();

  private readonly form = viewChild(TrabalhoForm);
  protected readonly situacoes = SITUACOES_TRABALHO;
  protected readonly tipos = TIPOS_ACIDENTE;
  protected readonly cats = RESPOSTAS_CAT;
  protected readonly etapas = ETAPAS_ACOMPANHAMENTO;

  protected readonly editando = signal(false);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly registrando = signal<string | null>(null);
  protected readonly dataEtapa = signal('');

  protected readonly aberta = computed(() => this.notificacao().situacao !== 'ENCERRADA');
  protected readonly acidente = computed(() => AGRAVOS_ACIDENTE.includes(this.notificacao().agravoCodigo));
  protected readonly grave = computed(() => this.notificacao().agravoCodigo === AGRAVO_ACIDENTE_GRAVE);
  protected readonly podeRegistrar = computed(() => this.podeInvestigar() || this.podeNotificar());

  protected salvar(): void {
    const f = this.form();
    if (!f || !f.validar()) return;
    this.executar(this.service.atualizarTrabalho(this.notificacao().uuid, f.valor()), () => this.editando.set(false));
  }

  protected registrar(a: AcompanhamentoSorologicoDto): void {
    this.executar(this.service.registrarAcompanhamento(this.notificacao().uuid, a.etapa, this.dataEtapa()), () => this.registrando.set(null));
  }

  private executar(req: ReturnType<VigilanciaService['atualizarTrabalho']>, depois: () => void): void {
    this.salvando.set(true);
    this.erro.set(null);
    req.subscribe({
      next: (n) => {
        this.salvando.set(false);
        depois();
        this.atualizada.emit(n);
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erro.set((e.error as ErrorResponseDto | null)?.message ?? 'Não foi possível salvar.');
      },
    });
  }

  protected hoje(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [a, m, d] = iso.substring(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string | null | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
