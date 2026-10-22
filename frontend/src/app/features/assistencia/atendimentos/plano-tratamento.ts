import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { ProcedimentoCatalogoDto } from '../../../core/models/procedimento';
import { ErrorResponseDto } from '../../../core/models/profissional';
import {
  FACES_DENTE,
  FaceDente,
  ItemPlanoTratamentoDto,
  NECESSIDADES_TRATAMENTO,
  NecessidadeTratamentoOdontologico,
  PlanoTratamentoDto,
  SITUACOES_ITEM_PLANO,
  SITUACOES_PLANO,
  SituacaoItemPlanoTratamento,
} from '../../../core/models/saude-bucal';
import { SaudeBucalService } from '../../../core/services/saude-bucal';

/** A ação que pede confirmação (e motivo) antes de ir para a API. */
type Acao = { tipo: 'item'; itemId: string; situacao: SituacaoItemPlanoTratamento } | { tipo: 'abandono' };

/**
 * Plano de tratamento odontológico (ADR-0137): abrir o plano, incluir os itens do catálogo (dente e faces), executar
 * cada item pelo procedimento (a gaveta do atendimento), mudar a situação, concluir o tratamento ou abandonar. Os
 * planos encerrados ficam recolhidos.
 */
@Component({
  selector: 'app-plano-tratamento',
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (aberto(); as p) {
      <div class="rec" aria-label="Plano aberto">
        <div class="rec__head">
          <div>
            <span [class]="'tag tag--' + situacoesPlano[p.situacao].classe">{{ situacoesPlano[p.situacao].rotulo }}</span>
            <span class="cell-main" style="display: inline;">{{ necessidades[p.prioridade] }}</span>
            <span class="cell-sub">Aberto em {{ data(p.abertoEm) }} · {{ p.profissionalNome }}</span>
          </div>
        </div>
        @if (p.diagnostico || p.objetivo) {
          <dl class="kv">
            @if (p.diagnostico) { <div><dt>Diagnóstico</dt><dd>{{ p.diagnostico }}</dd></div> }
            @if (p.objetivo) { <div><dt>Objetivo</dt><dd>{{ p.objetivo }}</dd></div> }
          </dl>
        }
        <ul class="eq-prof" aria-label="Itens do plano">
          @for (i of p.itens; track i.uuid) {
            <li class="mo-row">
              <span>
                <span class="cell-main">{{ i.ordem }}. {{ i.procedimentoNome }} <span [class]="'tag tag--' + situacoesItem[i.situacao].classe">{{ situacoesItem[i.situacao].rotulo }}</span></span>
                <span class="cell-sub">{{ complemento(i) }}@if (i.motivoSituacao) { · {{ i.motivoSituacao }} }</span>
              </span>
              @if (pendente(i)) {
                <span class="row-actions">
                  @if (podeExecutar()) { <button type="button" class="btn btn--sm" (click)="executar.emit(i)" [attr.aria-label]="'Executar: ' + i.procedimentoNome">Executar</button> }
                  @if (podePlanejar()) {
                    @if (i.situacao === 'PLANEJADO') { <button type="button" class="link-btn" (click)="mudar(i.uuid, 'EM_ANDAMENTO', null)" [attr.aria-label]="'Em andamento: ' + i.procedimentoNome">Em andamento</button> }
                    <button type="button" class="link-btn" (click)="pedir({ tipo: 'item', itemId: i.uuid, situacao: 'ENCAMINHADO' })" [attr.aria-label]="'Encaminhar: ' + i.procedimentoNome">Encaminhar</button>
                    <button type="button" class="link-btn" (click)="pedir({ tipo: 'item', itemId: i.uuid, situacao: 'CANCELADO' })" [attr.aria-label]="'Cancelar: ' + i.procedimentoNome">Cancelar</button>
                  }
                </span>
              }
            </li>
          } @empty { <li class="cell-sub">Nenhum item. Inclua os procedimentos previstos.</li> }
        </ul>

        @if (acao(); as a) {
          <div class="box box--info" aria-label="Confirmar ação do plano">
            <div>{{ a.tipo === 'abandono' ? 'Abandonar o plano de tratamento' : (a.situacao === 'CANCELADO' ? 'Cancelar o item' : 'Encaminhar o item') }}</div>
            <div class="row-actions" style="justify-content: flex-start; margin-top: var(--s-2);">
              <input type="text" maxlength="500" [attr.aria-label]="a.tipo === 'item' && a.situacao === 'ENCAMINHADO' ? 'Motivo ou destino (opcional)' : 'Motivo'"
                [placeholder]="a.tipo === 'item' && a.situacao === 'ENCAMINHADO' ? 'Destino ou motivo (opcional)' : 'Motivo'" [value]="motivo()" (input)="motivo.set($any($event.target).value)" />
              <button type="button" class="btn btn--sm" (click)="confirmar()" [disabled]="exigeMotivo() && !motivo().trim()">Confirmar</button>
              <button type="button" class="btn btn--ghost btn--sm" (click)="acao.set(null)">Voltar</button>
            </div>
          </div>
        }

        @if (podePlanejar()) {
          <div class="dw-row" style="align-items: end; margin-top: var(--s-3);" aria-label="Novo item do plano">
            <div class="dw-field">
              <label for="f-plano-item">Procedimento previsto</label>
              <select id="f-plano-item" [value]="novoItem()" (change)="escolherItem($any($event.target).value)">
                <option value="">Escolha</option>
                @for (c of catalogo(); track c.uuid) { <option [value]="c.uuid">{{ c.nome }}</option> }
              </select>
            </div>
            @if (itemEscolhido(); as c) {
              @if (c.alcance !== 'NENHUM') {
                <div class="dw-field"><label for="f-plano-dente">Dente (FDI)</label><input id="f-plano-dente" type="text" inputmode="numeric" maxlength="2" [value]="novoDente()" (input)="novoDente.set($any($event.target).value)" /></div>
              }
              @if (c.alcance === 'FACE') {
                <fieldset class="dw-field"><legend>Faces</legend>
                  <div class="radios">
                    @for (f of facesLista; track f) { <label><input type="checkbox" [checked]="novasFaces().includes(f)" (change)="alternarFace(f)" />{{ faces[f] }}</label> }
                  </div>
                </fieldset>
              }
            }
            <div class="dw-field"><button type="button" class="btn btn--ghost btn--sm" (click)="incluirItem(p.uuid)" [disabled]="!novoItem()">Incluir item</button></div>
          </div>
          <div class="row-actions" style="justify-content: flex-start; margin-top: var(--s-3);">
            <button type="button" class="btn btn--sm" (click)="concluir(p.uuid)">Concluir tratamento</button>
            <button type="button" class="btn btn--ghost btn--sm" (click)="pedir({ tipo: 'abandono' })">Abandonar plano</button>
          </div>
        }
      </div>
    } @else {
      <p class="cell-sub">Sem plano de tratamento aberto.@if (!avaliacaoId()) { Registre a avaliação odontológica: o diagnóstico e a prioridade vêm dela. }</p>
      @if (podePlanejar()) {
        <div class="dw-row" style="align-items: end;" aria-label="Novo plano de tratamento">
          <div class="dw-field"><label for="f-plano-matricula">Matrícula do dentista</label><input id="f-plano-matricula" type="text" inputmode="numeric" [value]="matricula()" (input)="matricula.set($any($event.target).value)" /></div>
          <div class="dw-field">
            <label for="f-plano-prioridade">Prioridade</label>
            <select id="f-plano-prioridade" [value]="prioridade()" (change)="prioridade.set($any($event.target).value)">
              <option value="">{{ avaliacaoId() ? 'A da avaliação' : 'Escolha' }}</option>
              @for (n of necessidadesLista; track n) { <option [value]="n">{{ necessidades[n] }}</option> }
            </select>
          </div>
          <div class="dw-field"><label for="f-plano-objetivo">Objetivo<span class="opt">opcional</span></label><input id="f-plano-objetivo" type="text" maxlength="1000" [value]="objetivo()" (input)="objetivo.set($any($event.target).value)" /></div>
          <div class="dw-field"><button type="button" class="btn btn--sm" (click)="abrir()">Abrir plano de tratamento</button></div>
        </div>
      }
    }
    @if (encerrados().length) {
      <details style="margin-top: var(--s-3);">
        <summary class="cell-sub">Planos anteriores ({{ encerrados().length }})</summary>
        <ul class="eq-prof" aria-label="Planos anteriores">
          @for (p of encerrados(); track p.uuid) {
            <li class="mo-row"><span>
              <span class="cell-main"><span [class]="'tag tag--' + situacoesPlano[p.situacao].classe">{{ situacoesPlano[p.situacao].rotulo }}</span> {{ p.itens.length }} item(ns)</span>
              <span class="cell-sub">{{ data(p.abertoEm) }} a {{ data(p.encerradoEm) }}@if (p.motivoEncerramento) { · {{ p.motivoEncerramento }} }</span>
            </span></li>
          }
        </ul>
      </details>
    }
  `,
})
export class PlanoTratamento {
  private readonly service = inject(SaudeBucalService);

  readonly pacienteId = input.required<string>();
  readonly atendimentoId = input.required<string>();
  /** A avaliação odontológica vigente: o diagnóstico e a prioridade do plano vêm dela. */
  readonly avaliacaoId = input<string | null>(null);
  readonly podePlanejar = input(false);
  readonly podeExecutar = input(false);
  /** Itens odontológicos ativos do catálogo. */
  readonly catalogo = input<ProcedimentoCatalogoDto[]>([]);
  readonly matriculaPadrao = input('');
  /** Muda quando os registros do atendimento são recarregados (o procedimento executa o item). */
  readonly recarregar = input<unknown>(null);
  readonly executar = output<ItemPlanoTratamentoDto>();

  protected readonly faces = FACES_DENTE;
  protected readonly facesLista = Object.keys(FACES_DENTE) as FaceDente[];
  protected readonly necessidades = NECESSIDADES_TRATAMENTO;
  protected readonly necessidadesLista = (Object.keys(NECESSIDADES_TRATAMENTO) as NecessidadeTratamentoOdontologico[]).filter((n) => n !== 'SEM_NECESSIDADE');
  protected readonly situacoesPlano = SITUACOES_PLANO;
  protected readonly situacoesItem = SITUACOES_ITEM_PLANO;

  protected readonly planos = signal<PlanoTratamentoDto[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly acao = signal<Acao | null>(null);
  protected readonly motivo = signal('');
  protected readonly matricula = signal('');
  protected readonly prioridade = signal('');
  protected readonly objetivo = signal('');
  protected readonly novoItem = signal('');
  protected readonly novoDente = signal('');
  protected readonly novasFaces = signal<FaceDente[]>([]);

  protected readonly aberto = computed(() => this.planos().find((p) => p.situacao === 'ABERTO') ?? null);
  protected readonly encerrados = computed(() => this.planos().filter((p) => p.situacao !== 'ABERTO'));
  protected readonly itemEscolhido = computed(() => this.catalogo().find((c) => c.uuid === this.novoItem()) ?? null);
  protected readonly exigeMotivo = computed(() => {
    const a = this.acao();
    return !!a && (a.tipo === 'abandono' || a.situacao === 'CANCELADO');
  });

  constructor() {
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
    this.service.planos(paciente).subscribe({ next: (p) => this.planos.set(p), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected pendente(i: ItemPlanoTratamentoDto): boolean {
    return i.situacao === 'PLANEJADO' || i.situacao === 'EM_ANDAMENTO';
  }

  protected complemento(i: ItemPlanoTratamentoDto): string {
    if (!i.dente) return 'Sem dente';
    const faces = i.faces.map((f) => FACES_DENTE[f]).join(', ');
    return 'Dente ' + i.dente + (faces ? ' · ' + faces : '');
  }

  protected data(iso?: string | null): string {
    return iso ? new Date(iso).toLocaleDateString('pt-BR') : '';
  }

  protected escolherItem(id: string): void {
    this.novoItem.set(id);
    this.novoDente.set('');
    this.novasFaces.set([]);
  }

  protected alternarFace(f: FaceDente): void {
    this.novasFaces.update((l) => (l.includes(f) ? l.filter((x) => x !== f) : [...l, f]));
  }

  protected abrir(): void {
    if (!this.matricula().trim()) {
      this.erro.set('Informe a matrícula do dentista responsável.');
      return;
    }
    this.service.abrirPlano({
      atendimentoId: this.atendimentoId(),
      avaliacaoId: this.avaliacaoId(),
      profissionalMatricula: this.matricula().trim(),
      prioridade: (this.prioridade() || null) as NecessidadeTratamentoOdontologico | null,
      objetivo: this.objetivo().trim() || null,
    }).subscribe({ next: () => this.recarregarPlanos(), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected incluirItem(planoId: string): void {
    const c = this.itemEscolhido();
    if (!c) return;
    this.service.incluirItemPlano(planoId, {
      procedimentoCatalogoId: c.uuid,
      dente: c.alcance === 'NENHUM' || !this.novoDente().trim() ? null : Number(this.novoDente().trim()),
      faces: c.alcance === 'FACE' ? this.facesLista.filter((f) => this.novasFaces().includes(f)) : [],
    }).subscribe({
      next: () => {
        this.escolherItem('');
        this.recarregarPlanos();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected pedir(a: Acao): void {
    this.motivo.set('');
    this.acao.set(a);
  }

  protected confirmar(): void {
    const a = this.acao();
    const p = this.aberto();
    if (!a || !p) return;
    if (a.tipo === 'abandono') {
      this.service.abandonarPlano(p.uuid, this.motivo().trim()).subscribe({ next: () => this.recarregarPlanos(), error: (e: HttpErrorResponse) => this.falhou(e) });
    } else {
      this.mudar(a.itemId, a.situacao, this.motivo().trim() || null);
    }
  }

  protected mudar(itemId: string, situacao: SituacaoItemPlanoTratamento, motivo: string | null): void {
    this.service.situacaoItemPlano(itemId, situacao, motivo).subscribe({ next: () => this.recarregarPlanos(), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected concluir(planoId: string): void {
    this.service.concluirPlano(planoId).subscribe({ next: () => this.recarregarPlanos(), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  private recarregarPlanos(): void {
    this.erro.set(null);
    this.acao.set(null);
    this.carregar(this.pacienteId());
  }

  private falhou(e: HttpErrorResponse): void {
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar o plano de tratamento.');
  }
}
