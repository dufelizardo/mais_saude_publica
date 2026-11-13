import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { InstituicaoExternaDto } from '../../core/models/intersetorialidade';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  MedidaTrabalhoDto,
  NEXOS_TRABALHO,
  NexoTrabalho,
  NotificacaoDto,
  SITUACOES_MEDIDA_TRABALHO,
  SituacaoMedidaTrabalho,
  TIPOS_MEDIDA_TRABALHO,
  TIPOS_RISCO_OCUPACIONAL,
  TipoMedidaTrabalho,
  TipoRiscoOcupacional,
} from '../../core/models/vigilancia';
import { IntersetorialidadeService } from '../../core/services/intersetorialidade';
import { VigilanciaService } from '../../core/services/vigilancia';

/**
 * A investigação do ambiente de trabalho no detalhe da notificação (ADR-0208): a visita e os achados, o nexo com a justificativa, as
 * medidas com prazo e verificação, a comunicação à vigilância sanitária, o encaminhamento intersetorial e os riscos encontrados.
 */
@Component({
  selector: 'app-investigacao-trabalho',
  template: `
    @if (inv() || (podeInvestigar() && aberta())) {
      <section aria-label="Investigação do trabalho">
        <h4 class="mo-grupo">Investigação do ambiente de trabalho</h4>
        @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

        @if (inv(); as i) {
          <dl class="info-list" aria-label="Visita ao local de trabalho">
            <div><dt>Nexo</dt><dd><span [class]="'tag tag--' + nexos[i.nexo].classe">{{ nexos[i.nexo].rotulo }}</span>@if (i.justificativaNexo) { · {{ i.justificativaNexo }} }</dd></div>
            <div><dt>Visita</dt><dd>{{ i.visitaEm ? data(i.visitaEm) : 'sem visita' }}@if (i.localVisitado) { · {{ i.localVisitado }} }@if (i.investigadorNome) { · {{ i.investigadorNome }} }</dd></div>
            @if (i.achados) { <div><dt>Achados</dt><dd>{{ i.achados }}</dd></div> }
            @if (i.denunciaProtocolo) { <div><dt>Vigilância sanitária</dt><dd>Denúncia {{ i.denunciaProtocolo }}@if (i.denunciaSituacao) { · {{ i.denunciaSituacao.toLowerCase().replace('_', ' ') }} }</dd></div> }
          </dl>
        }

        @if (podeInvestigar() && aberta()) {
          @if (!inv() || editandoVisita()) {
            <div class="dw-row">
              <div class="dw-field"><label for="f-inv-visita">Data da visita<span class="opt">opcional</span></label>
                <input id="f-inv-visita" type="date" [value]="visita()" (input)="visita.set($any($event.target).value)" /></div>
              <div class="dw-field"><label for="f-inv-local">Local visitado<span class="opt">opcional</span></label>
                <input id="f-inv-local" type="text" maxlength="500" [value]="local()" (input)="local.set($any($event.target).value)" /></div>
            </div>
            <div class="dw-field"><label for="f-inv-achados">Achados<span class="opt">opcional</span></label>
              <textarea id="f-inv-achados" rows="2" maxlength="2000" [value]="achados()" (input)="achados.set($any($event.target).value)"></textarea></div>
            <button type="button" class="btn btn--sm" [disabled]="salvando()" (click)="salvarVisita()">{{ inv() ? 'Salvar a visita' : 'Iniciar a investigação' }}</button>
          } @else {
            <button type="button" class="link-btn" (click)="editandoVisita.set(true)">Editar a visita</button>
          }
        }

        @if (inv(); as i) {
          @if (podeInvestigar() && aberta()) {
            <div class="dw-row" style="margin-top: var(--s-3);">
              <div class="dw-field"><label for="f-inv-nexo">Nexo com o trabalho</label>
                <select id="f-inv-nexo" [value]="nexo()" (change)="nexo.set($any($event.target).value)">
                  <option value="">Escolha…</option>
                  @for (x of listaNexos; track x) { <option [value]="x">{{ nexos[x].rotulo }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-inv-justificativa">Justificativa</label>
                <input id="f-inv-justificativa" type="text" maxlength="1000" [value]="justificativa()" (input)="justificativa.set($any($event.target).value)" /></div>
            </div>
            <button type="button" class="btn btn--ghost btn--sm" [disabled]="salvando() || !nexo() || !justificativa().trim()" (click)="salvarNexo()">Concluir o nexo</button>
          }

          <h4 class="mo-grupo">Medidas</h4>
          @if (i.medidas.length) {
            <ul class="eq-prof" aria-label="Medidas do trabalho">
              @for (m of i.medidas; track m.uuid) {
                <li class="mo-row">
                  <span><span class="cell-main">{{ tiposMedida[m.tipo] }} · {{ m.descricao }}</span>
                    <span class="cell-sub">prazo {{ data(m.prazo) }}@if (m.verificadaEm) { · verificada em {{ data(m.verificadaEm) }} }@if (m.observacao) { · {{ m.observacao }} }</span></span>
                  <span [class]="'tag tag--' + situacoesMedida[m.situacao].classe">{{ situacoesMedida[m.situacao].rotulo }}</span>
                  @if (m.vencida) { <span class="tag tag--alert">Vencida</span> }
                  @if (m.situacao === 'RECOMENDADA' && podeInvestigar()) {
                    @if (verificando() === m.uuid) {
                      <span class="row-actions">
                        <label class="sr-only" for="f-ver-situacao">Resultado da verificação</label>
                        <select id="f-ver-situacao" [value]="verSituacao()" (change)="verSituacao.set($any($event.target).value)">
                          <option value="CUMPRIDA">Cumprida</option><option value="NAO_CUMPRIDA">Não cumprida</option>
                        </select>
                        <label class="sr-only" for="f-ver-obs">Observação</label>
                        <input id="f-ver-obs" type="text" maxlength="1000" placeholder="Observação" [value]="verObs()" (input)="verObs.set($any($event.target).value)" />
                        <button type="button" class="btn btn--sm" [disabled]="salvando()" (click)="verificar(m)">Confirmar</button>
                      </span>
                    } @else {
                      <button type="button" class="link-btn" (click)="verificando.set(m.uuid); verSituacao.set('CUMPRIDA'); verObs.set('')" [attr.aria-label]="'Verificar medida: ' + m.descricao">Verificar</button>
                    }
                  }
                </li>
              }
            </ul>
          } @else { <p class="cell-sub">Nenhuma medida recomendada.</p> }
          @if (podeInvestigar() && aberta()) {
            <div class="dw-row">
              <div class="dw-field"><label for="f-med-tipo">Tipo</label>
                <select id="f-med-tipo" [value]="medTipo()" (change)="medTipo.set($any($event.target).value)">
                  @for (t of listaTiposMedida; track t) { <option [value]="t">{{ tiposMedida[t] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-med-prazo">Prazo</label>
                <input id="f-med-prazo" type="date" [value]="medPrazo()" (input)="medPrazo.set($any($event.target).value)" /></div>
            </div>
            <div class="dw-field"><label for="f-med-descricao">Medida</label>
              <input id="f-med-descricao" type="text" maxlength="1000" [value]="medDescricao()" (input)="medDescricao.set($any($event.target).value)" /></div>
            <button type="button" class="btn btn--ghost btn--sm" [disabled]="salvando() || !medDescricao().trim() || !medPrazo()" (click)="recomendar()">Recomendar a medida</button>
          }

          <h4 class="mo-grupo">Riscos encontrados</h4>
          @if (i.riscos.length) {
            <ul class="eq-prof" aria-label="Riscos encontrados">
              @for (r of i.riscos; track r.uuid) {
                <li><span class="cell-main">{{ tiposRisco[r.tipo] }} · {{ r.agente }}</span><span class="cell-sub">{{ r.empregadorNome }}@if (r.trabalhadoresExpostos != null) { · {{ r.trabalhadoresExpostos }} expostos } · {{ r.situacao === 'ATIVO' ? 'ativo' : 'controlado' }}</span></li>
              }
            </ul>
          } @else { <p class="cell-sub">Nenhum risco registrado pela investigação.</p> }
          @if (podeInvestigar() && aberta()) {
            <div class="dw-row">
              <div class="dw-field"><label for="f-risco-tipo">Tipo do risco</label>
                <select id="f-risco-tipo" [value]="riscoTipo()" (change)="riscoTipo.set($any($event.target).value)">
                  @for (t of listaTiposRisco; track t) { <option [value]="t">{{ tiposRisco[t] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-risco-agente">Agente</label>
                <input id="f-risco-agente" type="text" maxlength="200" placeholder="Ex.: ruído, sílica" [value]="riscoAgente()" (input)="riscoAgente.set($any($event.target).value)" /></div>
              <div class="dw-field"><label for="f-risco-expostos">Expostos<span class="opt">opcional</span></label>
                <input id="f-risco-expostos" type="number" min="0" [value]="riscoExpostos()" (input)="riscoExpostos.set($any($event.target).value)" /></div>
            </div>
            <button type="button" class="btn btn--ghost btn--sm" [disabled]="salvando() || !riscoAgente().trim()" (click)="registrarRisco()">Registrar o risco</button>

            @if (!i.denunciaProtocolo) {
              <h4 class="mo-grupo">Vigilância sanitária</h4>
              <div class="dw-field"><label for="f-visa-descricao">O que a vigilância sanitária precisa verificar</label>
                <textarea id="f-visa-descricao" rows="2" maxlength="1500" [value]="visaDescricao()" (input)="visaDescricao.set($any($event.target).value)"></textarea>
                <p class="cell-sub">A denúncia vai contra o empregador, sem os dados do trabalhador.</p></div>
              <button type="button" class="btn btn--ghost btn--sm" [disabled]="salvando() || !visaDescricao().trim()" (click)="comunicarVisa()">Comunicar a vigilância sanitária</button>
            }

            <h4 class="mo-grupo">Encaminhamento intersetorial</h4>
            <div class="dw-row">
              <div class="dw-field"><label for="f-enc-inst">Instituição</label>
                <select id="f-enc-inst" [value]="encInstituicao()" (change)="encInstituicao.set($any($event.target).value)">
                  <option value="">Escolha…</option>
                  @for (x of instituicoes(); track x.uuid) { <option [value]="x.uuid">{{ x.nome }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-enc-motivo">Motivo</label>
                <input id="f-enc-motivo" type="text" maxlength="1000" [value]="encMotivo()" (input)="encMotivo.set($any($event.target).value)" /></div>
            </div>
            <button type="button" class="btn btn--ghost btn--sm" [disabled]="salvando() || !encInstituicao() || !encMotivo().trim()" (click)="encaminhar()">Encaminhar</button>
          }
          @if (i.encaminhamentos.length) {
            <ul class="eq-prof" aria-label="Encaminhamentos da investigação">
              @for (e of i.encaminhamentos; track e.uuid) {
                <li><span class="cell-main">{{ e.instituicaoNome }}</span><span class="cell-sub">{{ data(e.enviadoEm) }} · retorno até {{ data(e.retornoEsperadoEm) }} · {{ e.situacao.toLowerCase() }}</span></li>
              }
            </ul>
          }
        }
      </section>
    }
  `,
})
export class InvestigacaoTrabalho {
  private readonly service = inject(VigilanciaService);
  private readonly intersetorialService = inject(IntersetorialidadeService);

  readonly notificacao = input.required<NotificacaoDto>();
  readonly podeInvestigar = input(false);
  readonly atualizada = output<NotificacaoDto>();

  protected readonly nexos = NEXOS_TRABALHO;
  protected readonly listaNexos: NexoTrabalho[] = ['CONFIRMADO', 'DESCARTADO', 'INCONCLUSIVO'];
  protected readonly tiposMedida = TIPOS_MEDIDA_TRABALHO;
  protected readonly listaTiposMedida = Object.keys(TIPOS_MEDIDA_TRABALHO) as TipoMedidaTrabalho[];
  protected readonly situacoesMedida = SITUACOES_MEDIDA_TRABALHO;
  protected readonly tiposRisco = TIPOS_RISCO_OCUPACIONAL;
  protected readonly listaTiposRisco = Object.keys(TIPOS_RISCO_OCUPACIONAL) as TipoRiscoOcupacional[];

  protected readonly inv = computed(() => this.notificacao().trabalho?.investigacao ?? null);
  protected readonly aberta = computed(() => this.notificacao().situacao !== 'ENCERRADA');
  protected readonly instituicoes = signal<InstituicaoExternaDto[]>([]);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly editandoVisita = signal(false);
  protected readonly visita = signal('');
  protected readonly local = signal('');
  protected readonly achados = signal('');
  protected readonly nexo = signal<NexoTrabalho | ''>('');
  protected readonly justificativa = signal('');
  protected readonly medTipo = signal<TipoMedidaTrabalho>('ADEQUACAO_DO_AMBIENTE');
  protected readonly medDescricao = signal('');
  protected readonly medPrazo = signal('');
  protected readonly verificando = signal<string | null>(null);
  protected readonly verSituacao = signal<SituacaoMedidaTrabalho>('CUMPRIDA');
  protected readonly verObs = signal('');
  protected readonly riscoTipo = signal<TipoRiscoOcupacional>('FISICO');
  protected readonly riscoAgente = signal('');
  protected readonly riscoExpostos = signal('');
  protected readonly visaDescricao = signal('');
  protected readonly encInstituicao = signal('');
  protected readonly encMotivo = signal('');

  constructor() {
    effect(() => {
      const i = this.inv();
      untracked(() => {
        this.visita.set(i?.visitaEm ?? '');
        this.local.set(i?.localVisitado ?? '');
        this.achados.set(i?.achados ?? '');
      });
    });
    effect(() => {
      if (this.podeInvestigar() && this.inv() && !this.instituicoes().length) {
        untracked(() => this.intersetorialService.listar({ ativas: true }).pipe(catchError(() => of([]))).subscribe((l) => this.instituicoes.set(l)));
      }
    });
  }

  protected salvarVisita(): void {
    this.executar(this.service.investigarTrabalho(this.notificacao().uuid, {
      visitaEm: this.visita() || null, localVisitado: this.local().trim() || null, achados: this.achados().trim() || null,
    }), () => this.editandoVisita.set(false));
  }

  protected salvarNexo(): void {
    this.executar(this.service.concluirNexo(this.notificacao().uuid, this.nexo() as NexoTrabalho, this.justificativa().trim()), () => {
      this.nexo.set('');
      this.justificativa.set('');
    });
  }

  protected recomendar(): void {
    this.executar(this.service.recomendarMedidaTrabalho(this.notificacao().uuid, { tipo: this.medTipo(), descricao: this.medDescricao().trim(), prazo: this.medPrazo() }), () => {
      this.medDescricao.set('');
      this.medPrazo.set('');
    });
  }

  protected verificar(m: MedidaTrabalhoDto): void {
    this.executar(this.service.verificarMedidaTrabalho(m.uuid, { situacao: this.verSituacao(), observacao: this.verObs().trim() || null }),
      () => this.verificando.set(null));
  }

  protected registrarRisco(): void {
    const expostos = this.riscoExpostos() === '' ? null : Number(this.riscoExpostos());
    this.salvando.set(true);
    this.erro.set(null);
    this.service.registrarRisco({ tipo: this.riscoTipo(), agente: this.riscoAgente().trim(), trabalhadoresExpostos: expostos, notificacaoId: this.notificacao().uuid })
      .subscribe({
        next: () => {
          this.riscoAgente.set('');
          this.riscoExpostos.set('');
          this.executar(this.service.notificacao(this.notificacao().uuid), () => undefined);
        },
        error: (e: HttpErrorResponse) => this.falha(e),
      });
  }

  protected comunicarVisa(): void {
    this.executar(this.service.comunicarVisa(this.notificacao().uuid, this.visaDescricao().trim()), () => this.visaDescricao.set(''));
  }

  protected encaminhar(): void {
    this.executar(this.service.encaminharTrabalho(this.notificacao().uuid, { instituicaoId: this.encInstituicao(), motivo: this.encMotivo().trim() }), () => {
      this.encInstituicao.set('');
      this.encMotivo.set('');
    });
  }

  private executar(req: Observable<NotificacaoDto>, depois: () => void): void {
    this.salvando.set(true);
    this.erro.set(null);
    req.subscribe({
      next: (n) => {
        this.salvando.set(false);
        depois();
        this.atualizada.emit(n);
      },
      error: (e: HttpErrorResponse) => this.falha(e),
    });
  }

  private falha(e: HttpErrorResponse): void {
    this.salvando.set(false);
    this.erro.set(e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | null)?.message ?? 'Não foi possível salvar.'));
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [a, m, d] = iso.substring(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}
