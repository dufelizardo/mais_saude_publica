import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../../core/models/profissional';
import {
  ARCADAS,
  ArcadaDentaria,
  ETAPAS_PROTETICAS,
  EtapaProtetica,
  SITUACOES_PROTESE,
  TIPOS_PROTESE,
  TipoProtese,
  TratamentoProteticoDto,
} from '../../../core/models/saude-bucal';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { SaudeBucalService } from '../../../core/services/saude-bucal';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';

/**
 * Tratamento protético (ADR-0139): abrir a prótese (tipo, arcada ou dente, laboratório), registrar as etapas na ordem do
 * ciclo (só as permitidas aparecem), concluir depois da entrega ou cancelar com motivo.
 */
@Component({
  selector: 'app-tratamento-protetico',
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @for (t of tratamentos(); track t.uuid) {
      <article class="rec" [attr.aria-label]="'Prótese: ' + tipos[t.tipo]">
        <div class="rec__head">
          <div>
            <span [class]="'tag tag--' + situacoes[t.situacao].classe">{{ situacoes[t.situacao].rotulo }}</span>
            <span class="cell-main" style="display: inline;">{{ tipos[t.tipo] }} · {{ t.dente ? 'dente ' + t.dente : 'arcada ' + arcadas[t.arcada].toLowerCase() }}</span>
            <span class="cell-sub">Etapa: {{ etapas[t.etapaAtual] }}@if (t.laboratorioNome) { · {{ t.laboratorioContratado ? 'contratado: ' : '' }}{{ t.laboratorioNome }} }@if (t.dataEntrega) { · entregue em {{ data(t.dataEntrega) }} }</span>
          </div>
        </div>
        <ol class="eq-prof" aria-label="Etapas da prótese">
          @for (e of t.etapas; track e.uuid) {
            <li class="mo-row"><span><span class="cell-main">{{ etapas[e.etapa] }}</span><span class="cell-sub">{{ data(e.data) }} · {{ e.profissionalNome }}@if (e.observacao) { · {{ e.observacao }} }</span></span></li>
          }
        </ol>
        @if (podePlanejar() && t.situacao === 'EM_ANDAMENTO') {
          <div class="dw-row" style="align-items: end; margin-top: var(--s-2);" aria-label="Próxima etapa">
            @if (t.proximasEtapas.includes('PROVA')) {
              <div class="dw-field">
                <label [for]="'f-protese-lab-' + t.uuid">Laboratório da rede</label>
                <select [id]="'f-protese-lab-' + t.uuid" [value]="laboratorio()" (change)="laboratorio.set($any($event.target).value)">
                  <option value="">{{ t.laboratorioNome ? 'Manter ' + t.laboratorioNome : 'Escolha (precisa ter a capacidade LRPD)' }}</option>
                  @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                </select>
              </div>
              <div class="dw-field"><label [for]="'f-protese-ext-' + t.uuid">Ou contratado<span class="opt">opcional</span></label><input [id]="'f-protese-ext-' + t.uuid" type="text" maxlength="200" [value]="externo()" (input)="externo.set($any($event.target).value)" /></div>
            }
            <div class="dw-field"><label [for]="'f-protese-obs-' + t.uuid">Observação<span class="opt">opcional</span></label><input [id]="'f-protese-obs-' + t.uuid" type="text" maxlength="500" [value]="observacao()" (input)="observacao.set($any($event.target).value)" /></div>
            <div class="row-actions">
              @for (e of todasEtapas; track e) {
                @if (t.proximasEtapas.includes(e) || (e === 'LABORATORIO' && t.proximasEtapas.includes('PROVA'))) {
                  <button type="button" class="btn btn--ghost btn--sm" (click)="registrar(t.uuid, e)" [attr.aria-label]="'Registrar ' + etapas[e] + ': ' + tipos[t.tipo]">{{ etapas[e] }}</button>
                }
              }
            </div>
          </div>
          <div class="row-actions" style="justify-content: flex-start; margin-top: var(--s-2);">
            @if (t.dataEntrega) { <button type="button" class="btn btn--sm" (click)="concluir(t.uuid)">Concluir prótese</button> }
            @if (cancelando() === t.uuid) {
              <input type="text" maxlength="500" aria-label="Motivo do cancelamento da prótese" placeholder="Motivo" [value]="motivo()" (input)="motivo.set($any($event.target).value)" />
              <button type="button" class="btn btn--sm" (click)="cancelar(t.uuid)" [disabled]="!motivo().trim()">Confirmar cancelamento</button>
            } @else {
              <button type="button" class="btn btn--ghost btn--sm" (click)="cancelando.set(t.uuid); motivo.set('')">Cancelar prótese</button>
            }
          </div>
        }
      </article>
    } @empty { <p class="cell-sub">Nenhuma prótese.</p> }

    @if (podePlanejar()) {
      <div class="dw-row" style="align-items: end; margin-top: var(--s-3);" aria-label="Nova prótese">
        <div class="dw-field">
          <label for="f-protese-tipo">Prótese</label>
          <select id="f-protese-tipo" [value]="tipo()" (change)="tipo.set($any($event.target).value)">
            @for (t of tiposLista; track t) { <option [value]="t">{{ tipos[t] }}</option> }
          </select>
        </div>
        @if (tipo() === 'UNITARIA') {
          <div class="dw-field"><label for="f-protese-dente">Dente (FDI)</label><input id="f-protese-dente" type="text" inputmode="numeric" maxlength="2" [value]="dente()" (input)="dente.set($any($event.target).value)" /></div>
        } @else {
          <div class="dw-field">
            <label for="f-protese-arcada">Arcada</label>
            <select id="f-protese-arcada" [value]="arcada()" (change)="arcada.set($any($event.target).value)">
              @for (a of arcadasLista; track a) { <option [value]="a">{{ arcadas[a] }}</option> }
            </select>
          </div>
        }
        <div class="dw-field"><label for="f-protese-matricula">Matrícula do dentista</label><input id="f-protese-matricula" type="text" inputmode="numeric" [value]="matricula()" (input)="matricula.set($any($event.target).value)" /></div>
        <div class="dw-field"><button type="button" class="btn btn--sm" (click)="abrir()">Abrir tratamento protético</button></div>
      </div>
    }
  `,
})
export class TratamentoProteticoSecao {
  private readonly service = inject(SaudeBucalService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  readonly pacienteId = input.required<string>();
  readonly atendimentoId = input.required<string>();
  readonly podePlanejar = input(false);
  readonly matriculaPadrao = input('');
  readonly recarregar = input<unknown>(null);

  protected readonly tipos = TIPOS_PROTESE;
  protected readonly tiposLista = Object.keys(TIPOS_PROTESE) as TipoProtese[];
  protected readonly arcadas = ARCADAS;
  protected readonly arcadasLista = Object.keys(ARCADAS) as ArcadaDentaria[];
  protected readonly etapas = ETAPAS_PROTETICAS;
  protected readonly todasEtapas = Object.keys(ETAPAS_PROTETICAS) as EtapaProtetica[];
  protected readonly situacoes = SITUACOES_PROTESE;

  protected readonly tratamentos = signal<TratamentoProteticoDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly tipo = signal<TipoProtese>('TOTAL');
  protected readonly arcada = signal<ArcadaDentaria>('SUPERIOR');
  protected readonly dente = signal('');
  protected readonly matricula = signal('');
  protected readonly laboratorio = signal('');
  protected readonly externo = signal('');
  protected readonly observacao = signal('');
  protected readonly cancelando = signal<string | null>(null);
  protected readonly motivo = signal('');

  constructor() {
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    effect(() => {
      const paciente = this.pacienteId();
      this.recarregar();
      untracked(() => this.carregar(paciente));
    });
    effect(() => {
      const m = this.matriculaPadrao();
      untracked(() => { if (!this.matricula()) this.matricula.set(m); });
    });
  }

  private carregar(paciente: string): void {
    this.service.proteses(paciente).subscribe({ next: (t) => this.tratamentos.set(t), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected data(iso?: string | null): string {
    if (!iso) return '';
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected abrir(): void {
    if (!this.matricula().trim()) {
      this.erro.set('Informe a matrícula do dentista responsável.');
      return;
    }
    const unitaria = this.tipo() === 'UNITARIA';
    this.service.abrirProtese({
      atendimentoId: this.atendimentoId(),
      profissionalMatricula: this.matricula().trim(),
      tipo: this.tipo(),
      arcada: unitaria ? null : this.arcada(),
      dente: unitaria && this.dente().trim() ? Number(this.dente().trim()) : null,
    }).subscribe({ next: () => this.recarregarTudo(), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected registrar(id: string, etapa: EtapaProtetica): void {
    this.service.etapaProtese(id, {
      etapa,
      profissionalMatricula: this.matricula().trim(),
      observacao: this.observacao().trim() || null,
      laboratorioUnidadeId: etapa === 'LABORATORIO' ? this.laboratorio() || null : null,
      laboratorioExterno: etapa === 'LABORATORIO' ? this.externo().trim() || null : null,
    }).subscribe({ next: () => this.recarregarTudo(), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected concluir(id: string): void {
    this.service.concluirProtese(id).subscribe({ next: () => this.recarregarTudo(), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected cancelar(id: string): void {
    this.service.cancelarProtese(id, this.motivo().trim()).subscribe({ next: () => this.recarregarTudo(), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  private recarregarTudo(): void {
    this.erro.set(null);
    this.cancelando.set(null);
    this.observacao.set('');
    this.laboratorio.set('');
    this.externo.set('');
    this.carregar(this.pacienteId());
  }

  private falhou(e: HttpErrorResponse): void {
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar o tratamento protético.');
  }
}
