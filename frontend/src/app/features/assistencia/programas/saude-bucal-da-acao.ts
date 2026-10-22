import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { AcaoProgramaticaDto } from '../../../core/models/programa';
import { ErrorResponseDto } from '../../../core/models/profissional';
import {
  ATIVIDADES_SAUDE_BUCAL,
  AcaoSaudeBucalDto,
  AtividadeSaudeBucalColetiva,
  LOCAIS_SAUDE_BUCAL,
  LocalSaudeBucalColetiva,
  NECESSIDADES_TRATAMENTO,
  NecessidadeTratamentoOdontologico,
  RISCOS_SAUDE_BUCAL,
  RiscoSaudeBucal,
} from '../../../core/models/saude-bucal';
import { SaudeBucalService } from '../../../core/services/saude-bucal';

/**
 * Saúde bucal da ação coletiva (ADR-0140): as atividades, o local, o público e os kits (quem acompanha o programa,
 * inclusive o técnico em saúde bucal) e, na ação realizada com lista nominal, a avaliação de risco de cada participante
 * (o cirurgião-dentista).
 */
@Component({
  selector: 'app-saude-bucal-da-acao',
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (mensagem()) { <div class="box box--info" role="status"><div>{{ mensagem() }}</div></div> }
    <fieldset class="dw-field" [disabled]="!podeAcompanhar()">
      <legend>Atividades</legend>
      <div class="radios" aria-label="Atividades de saúde bucal" style="flex-direction: column; align-items: flex-start;">
        @for (a of atividadesLista; track a) { <label><input type="checkbox" [checked]="atividades().includes(a)" (change)="alternar(a)" />{{ nomes[a] }}</label> }
      </div>
    </fieldset>
    <div class="dw-row">
      <div class="dw-field">
        <label for="f-sb-local">Local</label>
        <select id="f-sb-local" [value]="localTipo()" (change)="localTipo.set($any($event.target).value)" [disabled]="!podeAcompanhar()">
          @for (l of locaisLista; track l) { <option [value]="l">{{ locais[l] }}</option> }
        </select>
      </div>
      <div class="dw-field"><label for="f-sb-instituicao">Instituição<span class="opt">opcional</span></label><input id="f-sb-instituicao" type="text" maxlength="200" [value]="instituicao()" (input)="instituicao.set($any($event.target).value)" [disabled]="!podeAcompanhar()" placeholder="Ex.: Escola Municipal Rui Barbosa" /></div>
    </div>
    <div class="dw-row">
      <div class="dw-field"><label for="f-sb-publico">Público-alvo<span class="opt">opcional</span></label><input id="f-sb-publico" type="text" maxlength="300" [value]="publico()" (input)="publico.set($any($event.target).value)" [disabled]="!podeAcompanhar()" placeholder="Ex.: 1º ao 5º ano" /></div>
      <div class="dw-field"><label for="f-sb-kits">Kits distribuídos<span class="opt">opcional</span></label><input id="f-sb-kits" type="number" min="0" [value]="kits()" (input)="kits.set($any($event.target).value)" [disabled]="!podeAcompanhar()" /></div>
    </div>
    @if (podeAcompanhar()) {
      <div class="row-actions" style="justify-content: flex-start;"><button type="button" class="btn btn--sm" (click)="salvar()">Salvar saúde bucal</button></div>
    }

    @if (acao().situacao === 'REALIZADA') {
      <h4 class="mo-grupo" style="margin-top: var(--s-4);">Avaliação coletiva</h4>
      @if (!acao().participantes.length) {
        <p class="cell-sub">A ação não tem lista nominal: sem os participantes, não há avaliação por pessoa.</p>
      } @else {
        <ul class="eq-prof" aria-label="Avaliação dos participantes">
          @for (p of acao().participantes; track p.pacienteId) {
            <li class="mo-row">
              <span class="cell-main">{{ p.nome }}</span>
              <span class="row-actions">
                <select [attr.aria-label]="'Risco: ' + p.nome" [value]="risco(p.pacienteId)" (change)="definirRisco(p.pacienteId, $any($event.target).value)" [disabled]="!podeAvaliar()">
                  <option value="">Risco</option>
                  @for (r of riscosLista; track r) { <option [value]="r">{{ riscos[r].rotulo }}</option> }
                </select>
                <select [attr.aria-label]="'Necessidade: ' + p.nome" [value]="necessidade(p.pacienteId)" (change)="definirNecessidade(p.pacienteId, $any($event.target).value)" [disabled]="!podeAvaliar()">
                  <option value="">Necessidade</option>
                  @for (n of necessidadesLista; track n) { <option [value]="n">{{ necessidades[n] }}</option> }
                </select>
              </span>
            </li>
          }
        </ul>
        @if (podeAvaliar()) {
          <div class="row-actions" style="justify-content: flex-start;"><button type="button" class="btn btn--sm" (click)="avaliar()">Salvar avaliações</button></div>
        } @else {
          <p class="cell-sub">A avaliação de risco é do cirurgião-dentista.</p>
        }
      }
    }
  `,
})
export class SaudeBucalDaAcao {
  private readonly service = inject(SaudeBucalService);

  readonly acao = input.required<AcaoProgramaticaDto>();
  readonly podeAcompanhar = input(false);
  readonly podeAvaliar = input(false);

  protected readonly nomes = ATIVIDADES_SAUDE_BUCAL;
  protected readonly atividadesLista = Object.keys(ATIVIDADES_SAUDE_BUCAL) as AtividadeSaudeBucalColetiva[];
  protected readonly locais = LOCAIS_SAUDE_BUCAL;
  protected readonly locaisLista = Object.keys(LOCAIS_SAUDE_BUCAL) as LocalSaudeBucalColetiva[];
  protected readonly riscos = RISCOS_SAUDE_BUCAL;
  protected readonly riscosLista = Object.keys(RISCOS_SAUDE_BUCAL) as RiscoSaudeBucal[];
  protected readonly necessidades = NECESSIDADES_TRATAMENTO;
  protected readonly necessidadesLista = Object.keys(NECESSIDADES_TRATAMENTO) as NecessidadeTratamentoOdontologico[];

  protected readonly atividades = signal<AtividadeSaudeBucalColetiva[]>([]);
  protected readonly localTipo = signal<LocalSaudeBucalColetiva>('ESCOLA');
  protected readonly instituicao = signal('');
  protected readonly publico = signal('');
  protected readonly kits = signal('');
  protected readonly resultados = signal<Record<string, { risco: string; necessidade: string }>>({});
  protected readonly erro = signal<string | null>(null);
  protected readonly mensagem = signal<string | null>(null);

  constructor() {
    effect(() => {
      const a = this.acao();
      untracked(() => this.service.saudeBucalDaAcao(a.uuid).subscribe({ next: (d) => this.aplicar(d), error: (e: HttpErrorResponse) => this.falhou(e) }));
    });
  }

  private aplicar(d: AcaoSaudeBucalDto): void {
    if (d.preenchido) {
      this.atividades.set(d.atividades);
      this.localTipo.set(d.localTipo ?? 'ESCOLA');
      this.instituicao.set(d.instituicao ?? '');
      this.publico.set(d.publicoAlvo ?? '');
      this.kits.set(d.kitsDistribuidos != null ? String(d.kitsDistribuidos) : '');
    }
    const r: Record<string, { risco: string; necessidade: string }> = {};
    d.avaliacoes.forEach((a) => (r[a.pacienteId] = { risco: a.risco, necessidade: a.necessidadeTratamento }));
    this.resultados.set(r);
  }

  protected alternar(a: AtividadeSaudeBucalColetiva): void {
    this.atividades.update((l) => (l.includes(a) ? l.filter((x) => x !== a) : [...l, a]));
  }

  protected risco(id: string): string {
    return this.resultados()[id]?.risco ?? '';
  }

  protected necessidade(id: string): string {
    return this.resultados()[id]?.necessidade ?? '';
  }

  protected definirRisco(id: string, v: string): void {
    this.resultados.update((r) => ({ ...r, [id]: { risco: v, necessidade: r[id]?.necessidade ?? '' } }));
  }

  protected definirNecessidade(id: string, v: string): void {
    this.resultados.update((r) => ({ ...r, [id]: { risco: r[id]?.risco ?? '', necessidade: v } }));
  }

  protected salvar(): void {
    if (!this.atividades().length) {
      this.erro.set('Marque ao menos uma atividade.');
      return;
    }
    this.service.salvarSaudeBucalDaAcao(this.acao().uuid, {
      atividades: this.atividadesLista.filter((a) => this.atividades().includes(a)),
      localTipo: this.localTipo(),
      instituicao: this.instituicao().trim() || null,
      publicoAlvo: this.publico().trim() || null,
      kitsDistribuidos: this.kits() !== '' ? Number(this.kits()) : null,
    }).subscribe({ next: (d) => this.ok(d, 'Saúde bucal da ação salva.'), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  protected avaliar(): void {
    const lista = Object.entries(this.resultados())
      .filter(([, v]) => v.risco && v.necessidade)
      .map(([pacienteId, v]) => ({ pacienteId, risco: v.risco as RiscoSaudeBucal, necessidadeTratamento: v.necessidade as NecessidadeTratamentoOdontologico }));
    if (!lista.length) {
      this.erro.set('Informe o risco e a necessidade de ao menos um participante.');
      return;
    }
    this.service.avaliarColetiva(this.acao().uuid, lista).subscribe({ next: (d) => this.ok(d, 'Avaliações salvas.'), error: (e: HttpErrorResponse) => this.falhou(e) });
  }

  private ok(d: AcaoSaudeBucalDto, msg: string): void {
    this.erro.set(null);
    this.mensagem.set(msg);
    this.aplicar(d);
  }

  private falhou(e: HttpErrorResponse): void {
    this.mensagem.set(null);
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar a saúde bucal da ação.');
  }
}
