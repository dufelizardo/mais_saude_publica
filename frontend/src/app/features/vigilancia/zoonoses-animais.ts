import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../core/models/profissional';
import { AreaNoDto } from '../../core/models/territorio';
import {
  AnimalZoonosesDto,
  DESFECHOS_LEISHMANIOSE,
  DESFECHOS_OBSERVACAO,
  DesfechoLeishmanioseCanina,
  DesfechoObservacaoAnimal,
  ESPECIES_ANIMAL,
  EspecieAnimal,
  ExameAnimalDto,
  ObservacaoAnimalDto,
  RESULTADOS_EXAME_ANIMAL,
  ResultadoExameAnimal,
  TIPOS_EXAME_ANIMAL,
  TipoExameAnimal,
  VacinacaoAntirrabicaDto,
} from '../../core/models/zoonoses';
import { ZoonosesService } from '../../core/services/zoonoses';
import { Drawer } from '../../shared/drawer/drawer';

export type SecaoZoonoses = 'animais' | 'agressores' | 'vacinacao';
type Gaveta =
  | { tipo: 'animal' }
  | { tipo: 'observacao'; animal: AnimalZoonosesDto | null }
  | { tipo: 'desfecho'; obs: ObservacaoAnimalDto }
  | { tipo: 'exame'; animal: AnimalZoonosesDto | null; obs: ObservacaoAnimalDto | null }
  | { tipo: 'resultado'; exame: ExameAnimalDto }
  | { tipo: 'vacinacao' };

/**
 * Os animais das zoonoses (ADR-0163), nas abas da tela Zoonoses e vetores: o cadastro do animal com os exames (raiva e
 * leishmaniose visceral canina), a observação do agressor por 10 dias e a vacinação antirrábica por posto.
 */
@Component({
  selector: 'app-zoonoses-animais',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @switch (secao()) {
      @case ('animais') {
        <div class="za-cab">
          <div class="dw-field" style="margin: 0;"><label for="f-za-busca">Buscar</label><input id="f-za-busca" type="text" [value]="busca()" (input)="buscar($any($event.target).value)" placeholder="Nome, tutor ou endereço" /></div>
          @if (podeRegistrar()) {
            <div class="row-actions">
              <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'exame', animal: null, obs: null })" [disabled]="!animais().length">Registrar coleta</button>
              <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'animal' }, { especie: 'CAO' })">Registrar animal</button>
            </div>
          }
        </div>
        <div class="tbl-wrap card">
          <table class="za-tabela" aria-label="Animais">
            <thead><tr><th scope="col">Animal</th><th scope="col">Tutor</th><th scope="col">Local</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (a of animais(); track a.uuid) {
                <tr>
                  <td><span class="cell-main">{{ a.nome || especies[a.especie] }}@if (a.emObservacao) { <span class="tag tag--warn">Em observação</span> }</span><span class="cell-sub">{{ especies[a.especie] }}@if (a.descricao) { · {{ a.descricao }} }</span></td>
                  <td>{{ a.tutorNome ?? '—' }}@if (a.tutorTelefone) { <span class="cell-sub">{{ a.tutorTelefone }}</span> }</td>
                  <td>{{ a.endereco || '—' }}@if (a.microareaCodigo) { <span class="cell-sub">{{ a.areaNome }} · microárea {{ a.microareaCodigo }}</span> }</td>
                  <td class="row-actions">
                    @if (podeRegistrar() && !a.emObservacao && (a.especie === 'CAO' || a.especie === 'GATO')) { <button type="button" class="link-btn" (click)="abrirObservacao(a)" [attr.aria-label]="'Abrir observação: ' + (a.nome || especies[a.especie])">Observar</button> }
                    @if (podeRegistrar()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'exame', animal: a, obs: null }, { animalId: a.uuid, tipo: a.especie === 'CAO' ? 'LEISHMANIOSE_TR_DPP' : 'RAIVA_IMUNOFLUORESCENCIA' })" [attr.aria-label]="'Coleta de exame: ' + (a.nome || especies[a.especie])">Coleta</button> }
                  </td>
                </tr>
              } @empty { <tr><td colspan="4" class="cell-sub">Nenhum animal registrado.</td></tr> }
            </tbody>
          </table>
        </div>
        <h3 class="mo-grupo za-titulo">Exames</h3>
        <div class="tbl-wrap card">
          <table class="za-tabela" aria-label="Exames de animais">
            <thead><tr><th scope="col">Animal</th><th scope="col">Exame</th><th scope="col">Coleta</th><th scope="col">Resultado</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (e of exames(); track e.uuid) {
                <tr>
                  <td><span class="cell-main">{{ e.animalNome || especies[e.especie] }}</span><span class="cell-sub">{{ e.endereco }}</span></td>
                  <td>{{ tiposExame[e.tipo] }}</td>
                  <td class="mono">{{ data(e.dataColeta) }}</td>
                  <td>@if (e.resultado) { <span [class]="'tag tag--' + resultados[e.resultado].classe">{{ resultados[e.resultado].rotulo }}</span>@if (e.desfechoLeishmaniose) { <span class="cell-sub">{{ desfechosLeish[e.desfechoLeishmaniose] }}</span> } } @else { <span class="cell-sub">aguardando</span> }</td>
                  <td class="row-actions">@if (podeResultado()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'resultado', exame: e }, { resultado: e.resultado ?? '', desfecho: e.desfechoLeishmaniose ?? '' })" [attr.aria-label]="'Resultado: ' + tiposExame[e.tipo]">Resultado</button> }</td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhum exame.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('agressores') {
        <div class="za-cab">
          <p class="cell-sub" style="margin: 0; max-width: 70ch;">O cão ou o gato que agrediu alguém fica em observação por 10 dias a partir da agressão. O desfecho aparece no atendimento antirrábico notificado, para quem investiga decidir o esquema da pessoa.</p>
          @if (podeRegistrar()) { <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'observacao', animal: null }, { agressao: hoje() })" [disabled]="!observaveis().length">Abrir observação</button> }
        </div>
        <div class="tbl-wrap card">
          <table class="za-tabela" aria-label="Observações de agressores">
            <thead><tr><th scope="col">Animal</th><th scope="col">Agressão</th><th scope="col">Até</th><th scope="col">Desfecho</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (o of observacoes(); track o.uuid) {
                <tr>
                  <td><span class="cell-main">{{ o.animalNome || especies[o.especie] }}</span><span class="cell-sub">{{ o.tutorNome ?? 'sem tutor' }}@if (o.tutorTelefone) { · {{ o.tutorTelefone }} } · {{ o.endereco }}</span></td>
                  <td class="mono">{{ data(o.dataAgressao) }}</td>
                  <td class="mono">{{ data(o.fimPrevisto) }}@if (o.vencida) { <span class="tag tag--alert">vencida</span> }</td>
                  <td>@if (o.desfecho) { <span [class]="'tag tag--' + desfechos[o.desfecho].classe">{{ desfechos[o.desfecho].rotulo }}</span> } @else { <span class="tag tag--info">Em observação</span> }</td>
                  <td class="row-actions">
                    @if (!o.desfecho && podeRegistrar()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'desfecho', obs: o }, { desfecho: o.vencida ? 'SADIO' : 'SUSPEITO_RAIVA', data: hoje() })" [attr.aria-label]="'Desfecho: ' + (o.animalNome || especies[o.especie])">Desfecho</button> }
                    @if (o.desfecho === 'SUSPEITO_RAIVA' || o.desfecho === 'MORTO') { @if (podeRegistrar()) { <button type="button" class="link-btn" (click)="coletaDaObservacao(o)" [attr.aria-label]="'Coleta para raiva: ' + (o.animalNome || especies[o.especie])">Coleta para raiva</button> } }
                  </td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma observação.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('vacinacao') {
        <div class="za-cab">
          <div class="dw-field" style="margin: 0;"><label for="f-za-ano">Ano</label><input id="f-za-ano" type="number" [value]="ano()" (change)="mudarAno(+$any($event.target).value)" /></div>
          @if (podeRegistrar()) { <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'vacinacao' }, { data: hoje() })" [disabled]="!areas().length">Registrar posto</button> }
        </div>
        @if (vacinacao(); as v) {
          <div class="za-numeros" aria-label="Vacinação antirrábica do ano">
            <div class="za-num"><b>{{ v.caes.toLocaleString('pt-BR') }}</b><span>cães vacinados em {{ v.ano }}</span></div>
            <div class="za-num"><b>{{ v.gatos.toLocaleString('pt-BR') }}</b><span>gatos vacinados em {{ v.ano }}</span></div>
          </div>
          <div class="tbl-wrap card">
            <table class="za-tabela" aria-label="Vacinação por posto">
              <thead><tr><th scope="col">Posto</th><th scope="col">Data</th><th scope="col">Cães</th><th scope="col">Gatos</th></tr></thead>
              <tbody>
                @for (r of v.registros; track r.uuid) {
                  <tr><td><span class="cell-main">{{ r.posto }}</span><span class="cell-sub">{{ r.areaNome }} · microárea {{ r.microareaCodigo }} · {{ r.responsavel }}</span></td><td class="mono">{{ data(r.data) }}</td><td>{{ r.caes }}</td><td>{{ r.gatos }}</td></tr>
                } @empty { <tr><td colspan="4" class="cell-sub">Nenhum posto registrado em {{ v.ano }}.</td></tr> }
              </tbody>
            </table>
          </div>
        }
      }
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @switch (g.tipo) {
              @case ('animal') {
                <div class="dw-row">
                  <div class="dw-field"><label for="f-za-especie">Espécie</label>
                    <select id="f-za-especie" (change)="f.set({ ...f(), especie: $any($event.target).value })">
                      @for (e of listaEspecies; track e) { <option [value]="e" [selected]="f()['especie'] === e">{{ especies[e] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-za-nome">Nome<span class="opt">opcional</span></label><input id="f-za-nome" type="text" maxlength="200" [value]="f()['nome'] ?? ''" (input)="set('nome', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-za-descricao">Descrição<span class="opt">opcional</span></label><input id="f-za-descricao" type="text" maxlength="500" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)" placeholder="Porte, cor, sinais" /></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-za-tutor">Tutor<span class="opt">opcional</span></label><input id="f-za-tutor" type="text" maxlength="200" [value]="f()['tutor'] ?? ''" (input)="set('tutor', $event)" /></div>
                  <div class="dw-field"><label for="f-za-telefone">Telefone<span class="opt">opcional</span></label><input id="f-za-telefone" type="text" maxlength="40" [value]="f()['telefone'] ?? ''" (input)="set('telefone', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-za-endereco">Endereço <span class="req" aria-hidden="true">*</span></label><input id="f-za-endereco" type="text" maxlength="300" [value]="f()['endereco'] ?? ''" (input)="set('endereco', $event)" />
                  <span class="cell-sub">O tutor não vira paciente: ficam só o nome e o telefone.</span></div>
              }
              @case ('observacao') {
                <div class="dw-field"><label for="f-za-obs-animal">Animal <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-za-obs-animal" (change)="f.set({ ...f(), animalId: $any($event.target).value })">
                    <option value="" [selected]="!f()['animalId']">Escolha…</option>
                    @for (a of observaveis(); track a.uuid) { <option [value]="a.uuid" [selected]="f()['animalId'] === a.uuid">{{ a.nome || especies[a.especie] }} · {{ a.endereco }}</option> }
                  </select></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-za-agressao">Data da agressão <span class="req" aria-hidden="true">*</span></label><input id="f-za-agressao" type="date" [value]="f()['agressao'] ?? ''" (input)="set('agressao', $event)" /></div>
                  <div class="dw-field"><label for="f-za-notificacao">Notificação do atendimento antirrábico<span class="opt">opcional</span></label><input id="f-za-notificacao" type="text" [value]="f()['notificacao'] ?? ''" (input)="set('notificacao', $event)" placeholder="Identificador da notificação" /></div>
                </div>
                <div class="dw-field"><label for="f-za-obs-texto">Observação<span class="opt">opcional</span></label><input id="f-za-obs-texto" type="text" maxlength="1000" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" /></div>
              }
              @case ('desfecho') {
                <dl class="ro"><div><dt>Animal</dt><dd>{{ g.obs.animalNome || especies[g.obs.especie] }} · observação até {{ data(g.obs.fimPrevisto) }}</dd></div></dl>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-za-desfecho">Desfecho</label>
                    <select id="f-za-desfecho" (change)="f.set({ ...f(), desfecho: $any($event.target).value })">
                      @for (d of listaDesfechos; track d) { <option [value]="d" [selected]="f()['desfecho'] === d">{{ desfechos[d].rotulo }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-za-desfecho-data">Data</label><input id="f-za-desfecho-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-za-desfecho-obs">Observação<span class="opt">opcional</span></label><input id="f-za-desfecho-obs" type="text" maxlength="1000" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" /></div>
              }
              @case ('exame') {
                @if (g.animal) { <dl class="ro"><div><dt>Animal</dt><dd>{{ g.animal.nome || especies[g.animal.especie] }} · {{ g.animal.endereco }}</dd></div></dl> } @else {
                  <div class="dw-field"><label for="f-za-ex-animal">Animal <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-za-ex-animal" (change)="f.set({ ...f(), animalId: $any($event.target).value })">
                      <option value="" [selected]="!f()['animalId']">Escolha…</option>
                      @for (a of animais(); track a.uuid) { <option [value]="a.uuid" [selected]="f()['animalId'] === a.uuid">{{ a.nome || especies[a.especie] }} · {{ a.endereco }}</option> }
                    </select></div>
                }
                <div class="dw-row">
                  <div class="dw-field"><label for="f-za-ex-tipo">Exame</label>
                    <select id="f-za-ex-tipo" (change)="f.set({ ...f(), tipo: $any($event.target).value })">
                      @for (t of listaTiposExame; track t) { <option [value]="t" [selected]="f()['tipo'] === t">{{ tiposExame[t] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-za-ex-data">Data da coleta</label><input id="f-za-ex-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                </div>
              }
              @case ('resultado') {
                <dl class="ro"><div><dt>Exame</dt><dd>{{ tiposExame[g.exame.tipo] }} · {{ g.exame.animalNome || especies[g.exame.especie] }}</dd></div></dl>
                <div class="dw-field"><label for="f-za-resultado">Resultado <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-za-resultado" (change)="f.set({ ...f(), resultado: $any($event.target).value })">
                    <option value="" [selected]="!f()['resultado']">Escolha…</option>
                    @for (r of listaResultados; track r) { <option [value]="r" [selected]="f()['resultado'] === r">{{ resultados[r].rotulo }}</option> }
                  </select></div>
                @if (g.exame.tipo !== 'RAIVA_IMUNOFLUORESCENCIA' && g.exame.tipo !== 'OUTRO' && f()['resultado'] === 'REAGENTE') {
                  <div class="dw-field"><label for="f-za-leish">Desfecho do cão<span class="opt">opcional</span></label>
                    <select id="f-za-leish" (change)="f.set({ ...f(), desfecho: $any($event.target).value })">
                      <option value="" [selected]="!f()['desfecho']">Não informado</option>
                      @for (d of listaDesfechosLeish; track d) { <option [value]="d" [selected]="f()['desfecho'] === d">{{ desfechosLeish[d] }}</option> }
                    </select></div>
                }
              }
              @case ('vacinacao') {
                <div class="dw-row">
                  <div class="dw-field"><label for="f-za-v-area">Área <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-za-v-area" (change)="f.set({ ...f(), areaId: $any($event.target).value, microareaId: '' })">
                      <option value="" [selected]="!f()['areaId']">Escolha…</option>
                      @for (a of areas(); track a.uuid) { <option [value]="a.uuid" [selected]="f()['areaId'] === a.uuid">{{ a.nome }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-za-v-micro">Microárea <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-za-v-micro" (change)="f.set({ ...f(), microareaId: $any($event.target).value })">
                      <option value="" [selected]="!f()['microareaId']">Escolha…</option>
                      @for (m of microareas(); track m.uuid) { <option [value]="m.uuid" [selected]="f()['microareaId'] === m.uuid">{{ m.codigo }}@if (m.nome) { · {{ m.nome }} }</option> }
                    </select></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-za-v-posto">Posto <span class="req" aria-hidden="true">*</span></label><input id="f-za-v-posto" type="text" maxlength="200" [value]="f()['posto'] ?? ''" (input)="set('posto', $event)" placeholder="Ex.: Escola Municipal" /></div>
                  <div class="dw-field"><label for="f-za-v-data">Data</label><input id="f-za-v-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-za-v-caes">Cães vacinados</label><input id="f-za-v-caes" type="number" min="0" [value]="f()['caes'] ?? ''" (input)="set('caes', $event)" /></div>
                  <div class="dw-field"><label for="f-za-v-gatos">Gatos vacinados</label><input id="f-za-v-gatos" type="number" min="0" [value]="f()['gatos'] ?? ''" (input)="set('gatos', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-za-v-resp">Responsável <span class="req" aria-hidden="true">*</span></label><input id="f-za-v-resp" type="text" maxlength="200" [value]="f()['responsavel'] ?? ''" (input)="set('responsavel', $event)" /></div>
              }
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar' }}</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .za-cab { display: flex; justify-content: space-between; align-items: flex-end; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-2); }
    .za-tabela { min-width: 0; width: 100%; }
    .za-titulo { margin-top: var(--s-4); }
    .za-numeros { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: var(--s-2); margin-bottom: var(--s-3); }
    .za-num { border: 1px solid var(--ink-200); border-radius: 10px; padding: var(--s-2) var(--s-3); display: grid; gap: 2px; background: var(--white); }
    .za-num b { font-size: 1.05rem; }
    .za-num span { font-size: 0.75rem; color: var(--ink-500); }
  `,
})
export class ZoonosesAnimais {
  private readonly service = inject(ZoonosesService);

  readonly secao = input.required<SecaoZoonoses>();
  readonly areas = input<AreaNoDto[]>([]);
  readonly podeRegistrar = input(false);
  readonly podeResultado = input(false);

  protected readonly especies = ESPECIES_ANIMAL;
  protected readonly listaEspecies = Object.keys(ESPECIES_ANIMAL) as EspecieAnimal[];
  protected readonly desfechos = DESFECHOS_OBSERVACAO;
  protected readonly listaDesfechos = Object.keys(DESFECHOS_OBSERVACAO) as DesfechoObservacaoAnimal[];
  protected readonly tiposExame = TIPOS_EXAME_ANIMAL;
  protected readonly listaTiposExame = Object.keys(TIPOS_EXAME_ANIMAL) as TipoExameAnimal[];
  protected readonly resultados = RESULTADOS_EXAME_ANIMAL;
  protected readonly listaResultados = Object.keys(RESULTADOS_EXAME_ANIMAL) as ResultadoExameAnimal[];
  protected readonly desfechosLeish = DESFECHOS_LEISHMANIOSE;
  protected readonly listaDesfechosLeish = Object.keys(DESFECHOS_LEISHMANIOSE) as DesfechoLeishmanioseCanina[];

  protected readonly erro = signal<string | null>(null);
  protected readonly busca = signal('');
  protected readonly ano = signal(new Date().getFullYear());
  protected readonly animais = signal<AnimalZoonosesDto[]>([]);
  protected readonly observacoes = signal<ObservacaoAnimalDto[]>([]);
  protected readonly exames = signal<ExameAnimalDto[]>([]);
  protected readonly vacinacao = signal<VacinacaoAntirrabicaDto | null>(null);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});

  protected readonly observaveis = computed(() => this.animais().filter((a) => !a.emObservacao && (a.especie === 'CAO' || a.especie === 'GATO')));
  protected readonly microareas = computed(() => this.areas().find((a) => a.uuid === this.f()['areaId'])?.microareas ?? []);

  constructor() {
    effect(() => {
      this.secao();
      this.carregar();
    });
  }

  protected buscar(v: string): void {
    this.busca.set(v);
    this.service.animais(v.trim() || null).pipe(catchError(() => of([]))).subscribe((l) => this.animais.set(l));
  }

  protected mudarAno(a: number): void {
    if (!a) return;
    this.ano.set(a);
    this.service.vacinacao(a).pipe(catchError(() => of(null))).subscribe((v) => this.vacinacao.set(v));
  }

  protected titulo(g: Gaveta): string {
    switch (g.tipo) {
      case 'animal': return 'Registrar animal';
      case 'observacao': return 'Observação do animal agressor';
      case 'desfecho': return 'Desfecho da observação';
      case 'exame': return 'Coleta de exame';
      case 'resultado': return 'Resultado do exame';
      default: return 'Vacinação antirrábica do posto';
    }
  }

  protected abrir(g: Gaveta, campos: Record<string, string> = {}): void {
    this.erroGaveta.set(null);
    this.f.set({ data: this.hoje(), ...campos });
    this.gaveta.set(g);
  }

  protected abrirObservacao(a: AnimalZoonosesDto): void {
    this.abrir({ tipo: 'observacao', animal: a }, { animalId: a.uuid, agressao: this.hoje() });
  }

  protected coletaDaObservacao(o: ObservacaoAnimalDto): void {
    this.abrir({ tipo: 'exame', animal: null, obs: o }, { animalId: o.animalId, tipo: 'RAIVA_IMUNOFLUORESCENCIA' });
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected salvar(g: Gaveta): void {
    if (this.enviando()) return;
    const c = this.f();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    const n = (k: string) => (t(k) == null ? null : Number(c[k]));
    let req: Observable<unknown>;
    let falta: string | null = null;
    switch (g.tipo) {
      case 'animal':
        if (!t('endereco')) falta = 'Informe o endereço do animal.';
        req = this.service.registrarAnimal({
          especie: c['especie'] as EspecieAnimal, nome: t('nome'), descricao: t('descricao'), tutorNome: t('tutor'), tutorTelefone: t('telefone'), imovelId: null,
          endereco: t('endereco'),
        });
        break;
      case 'observacao':
        if (!t('animalId') || !t('agressao')) falta = 'Escolha o animal e informe a data da agressão.';
        req = this.service.abrirObservacao({ animalId: t('animalId') ?? '', notificacaoId: t('notificacao'), dataAgressao: t('agressao') ?? '', inicio: null, observacao: t('observacao') });
        break;
      case 'desfecho':
        req = this.service.desfecho(g.obs.uuid, c['desfecho'] as DesfechoObservacaoAnimal, t('data'), t('observacao'));
        break;
      case 'exame':
        if (!t('animalId')) falta = 'Escolha o animal.';
        req = this.service.coletar({ animalId: t('animalId') ?? '', tipo: (c['tipo'] || 'RAIVA_IMUNOFLUORESCENCIA') as TipoExameAnimal, dataColeta: t('data'), observacaoId: g.obs?.uuid ?? null, observacao: null });
        break;
      case 'resultado':
        if (!t('resultado')) falta = 'Escolha o resultado.';
        req = this.service.resultado(g.exame.uuid, c['resultado'] as ResultadoExameAnimal, c['resultado'] === 'REAGENTE' ? (t('desfecho') as DesfechoLeishmanioseCanina | null) : null);
        break;
      default:
        if (!t('microareaId') || !t('posto') || !t('responsavel')) falta = 'Informe a microárea, o posto e o responsável.';
        req = this.service.registrarVacinacao({ data: t('data'), microareaId: t('microareaId') ?? '', posto: t('posto') ?? '', caes: n('caes'), gatos: n('gatos'), responsavel: t('responsavel') ?? '' });
    }
    if (falta) {
      this.erroGaveta.set(falta);
      return;
    }
    this.enviando.set(true);
    req.subscribe({
      next: () => {
        this.enviando.set(false);
        this.gaveta.set(null);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected hoje(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  private carregar(): void {
    this.erro.set(null);
    const falhou = (x: HttpErrorResponse) => this.erro.set(mensagem(x));
    switch (this.secao()) {
      case 'animais':
        this.service.animais(this.busca().trim() || null).subscribe({ next: (l) => this.animais.set(l), error: falhou });
        this.service.exames().pipe(catchError(() => of([]))).subscribe((l) => this.exames.set(l));
        break;
      case 'agressores':
        this.service.observacoes().subscribe({ next: (l) => this.observacoes.set(l), error: falhou });
        this.service.animais().pipe(catchError(() => of([]))).subscribe((l) => this.animais.set(l));
        break;
      default:
        this.service.vacinacao(this.ano()).subscribe({ next: (v) => this.vacinacao.set(v), error: falhou });
    }
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
