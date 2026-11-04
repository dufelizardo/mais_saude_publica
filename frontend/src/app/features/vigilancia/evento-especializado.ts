import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import {
  EmergenciaDto,
  EstruturaTemporariaDto,
  ExposicaoAmbientalDto,
  MEIOS_EXPOSICAO,
  MeioExposicaoAmbiental,
  PainelEpidemiaDto,
  SugestaoImpactoDto,
  NATUREZAS_EVENTO_MASSA,
  NaturezaEventoMassa,
  RISCOS_EVENTO_MASSA,
  RiscoEventoMassa,
  SITUACOES_ESTRUTURA,
  TIPOS_ESTRUTURA,
  TipoEstruturaTemporaria,
} from '../../core/models/emergencia';
import { ErrorResponseDto } from '../../core/models/profissional';
import { AgravoDto } from '../../core/models/vigilancia';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { EmergenciaService } from '../../core/services/emergencia';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { VigilanciaService } from '../../core/services/vigilancia';
import { Drawer } from '../../shared/drawer/drawer';
import { CurvaEpidemica } from './curva-epidemica';

type Gaveta =
  | { tipo: 'eventoMassa' }
  | { tipo: 'estrutura'; item: EstruturaTemporariaDto | null }
  | { tipo: 'desativar'; item: EstruturaTemporariaDto }
  | { tipo: 'boletim'; item: EstruturaTemporariaDto }
  | { tipo: 'agravo' }
  | { tipo: 'exposicao' };

/**
 * Os eventos especializados no detalhe da emergência (ADR-0160): os dados do evento de massa e as estruturas temporárias
 * de atendimento (posto médico, tenda de hidratação, hospital de campanha), com a abertura, a desativação e os boletins.
 * A estrutura é capacidade, não unidade: a unidade de referência responde por escala, estoque e leitos. Na epidemia, o
 * painel lido da Vigilância; no desastre ambiental, as exposições (ADR-0161).
 */
@Component({
  selector: 'app-evento-especializado',
  imports: [Drawer, CurvaEpidemica],
  template: `
    @let e = emergencia();
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    @if (e.perfil === 'EVENTO_DE_MASSA') {
      <div class="es-secao" aria-label="Evento de massa">
        <div class="es-cab">
          <h3 class="mo-grupo" style="margin: 0;">Evento de massa</h3>
          @if (editavel() && podeCoordenar()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirEventoMassa()">{{ e.eventoMassa ? 'Editar dados do evento' : 'Dados do evento' }}</button> }
        </div>
        @if (e.eventoMassa; as m) {
          <dl class="info-list">
            <div><dt>Natureza</dt><dd>{{ naturezas[m.natureza] }} <span [class]="'tag tag--' + riscos[m.risco].classe">{{ riscos[m.risco].rotulo }}</span></dd></div>
            <div><dt>Local</dt><dd>{{ m.local }}</dd></div>
            <div><dt>Período previsto</dt><dd>{{ dataHora(m.inicioPrevisto) }} a {{ dataHora(m.fimPrevisto) }}</dd></div>
            <div><dt>Público</dt><dd>{{ numero(m.publicoEstimado) }} estimado@if (m.publicoMaximo) { · até {{ numero(m.publicoMaximo) }} }</dd></div>
            @if (m.organizador || m.contato) { <div><dt>Organizador</dt><dd>{{ m.organizador ?? '—' }}@if (m.contato) { · {{ m.contato }} }</dd></div> }
            @if (m.observacao) { <div><dt>Observação</dt><dd>{{ m.observacao }}</dd></div> }
          </dl>
        } @else {
          <p class="cell-sub">Preencha os dados do evento: a operação só começa com eles.</p>
        }
      </div>
    }

    @if (e.perfil === 'EPIDEMIA') {
      <div class="es-secao" aria-label="Situação epidemiológica">
        <div class="es-cab">
          <h3 class="mo-grupo" style="margin: 0;">Situação epidemiológica@if (e.agravoNome) { · {{ e.agravoNome }} }</h3>
          @if (e.situacao === 'ATIVA' && podeRegistrar()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirAgravo()">{{ e.agravoId ? 'Trocar agravo' : 'Escolher agravo' }}</button> }
        </div>
        @if (painel(); as p) {
          <div class="es-numeros" aria-label="Números da epidemia">
            <div class="es-num"><b>{{ p.notificados }}</b><span>notificado(s)</span></div>
            <div class="es-num"><b>{{ p.confirmados }}</b><span>confirmado(s)</span></div>
            <div class="es-num"><b>{{ p.emInvestigacao }}</b><span>em investigação</span></div>
            <div class="es-num"><b>{{ p.descartados }}</b><span>descartado(s)</span></div>
            <div class="es-num"><b>{{ p.obitos }}</b><span>óbito(s) pelo agravo</span></div>
            <div class="es-num"><b>{{ p.semanaAtual }}</b><span>na semana atual · {{ p.semanaAnterior }} na anterior@if (p.semanaAtual > p.semanaAnterior) { <span class="tag tag--alert">subindo</span> }</span></div>
          </div>
          <app-curva-epidemica [curva]="p.curva" [rotulo]="'Notificações de ' + p.agravoNome + ' por semana epidemiológica'" />
          <p class="cell-sub">{{ fontePainel(p) }}</p>
        } @else {
          <p class="cell-sub">{{ e.agravoId ? 'Carregando o painel…' : 'Escolha o agravo da epidemia para ver as notificações por semana.' }}</p>
        }
      </div>
    }

    @if (e.perfil === 'DESASTRE_AMBIENTAL') {
      <div class="es-secao" aria-label="Exposição ambiental">
        <div class="es-cab">
          <h3 class="mo-grupo" style="margin: 0;">Exposição ambiental</h3>
          @if (e.situacao === 'ATIVA' && podeRegistrar()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirExposicao()">Nova exposição</button> }
        </div>
        @if (e.exposicoes.length) {
          <ul class="eq-prof" aria-label="Exposições ambientais">
            @for (x of e.exposicoes; track x.uuid) {
              <li>
                <span class="cell-main">{{ meios[x.meio] }} · {{ x.agente }} <span class="cell-sub" style="display: inline;">{{ dataHora(x.avaliadaEm) }}</span></span>
                @if (detalheExposicao(x); as det) { <span class="cell-sub">{{ det }}</span> }
                @if (x.medidas) { <span class="cell-sub">Medidas: {{ x.medidas }}</span> }
                @if (x.recomendacao) { <span class="cell-sub">Recomendação à população: {{ x.recomendacao }}</span> }
              </li>
            }
          </ul>
        } @else {
          <p class="cell-sub">Nenhuma exposição registrada. Registre o meio afetado, o agente e a população exposta; repita a cada nova avaliação.</p>
        }
      </div>
    }

    <div class="es-secao">
      <div class="es-cab">
        <h3 class="mo-grupo" style="margin: 0;">Estruturas temporárias</h3>
        @if (editavel() && podeCoordenar()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirEstrutura(null)">Nova estrutura</button> }
      </div>
      @if (e.estruturas.length) {
        <div class="tbl-wrap"><table class="es-tabela" aria-label="Estruturas temporárias">
          <thead><tr><th scope="col">Estrutura</th><th scope="col">Capacidade</th><th scope="col">Situação</th><th scope="col">Boletins</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (s of e.estruturas; track s.uuid) {
              <tr>
                <td><span class="cell-main">{{ s.nome }}</span><span class="cell-sub">{{ tipos[s.tipo] }} · {{ s.local }} · referência {{ s.unidadeReferenciaNome }}@if (s.horario) { · {{ s.horario }} }</span></td>
                <td class="cell-sub">{{ s.leitos }} leito(s) de observação@if (s.atendimentosDia) { · {{ s.atendimentosDia }} atendimentos/dia }@if (s.ambulancias) { · {{ s.ambulancias }} ambulância(s) }</td>
                <td><span [class]="'tag tag--' + situacoes[s.situacao].classe">{{ situacoes[s.situacao].rotulo }}</span>@if (s.motivoDesativacao) { <span class="cell-sub">{{ s.motivoDesativacao }}</span> }</td>
                <td>@if (s.boletins.length) {
                    <span class="cell-main">{{ s.atendimentos }} atendimento(s)</span>
                    <span class="cell-sub">{{ s.remocoes }} remoção(ões) · {{ s.casosGraves }} grave(s) · {{ s.obitos }} óbito(s) · {{ s.boletins.length }} boletim(ns), o último em {{ data(s.boletins[0].data) }}</span>
                  } @else { <span class="cell-sub">sem boletim</span> }</td>
                <td class="row-actions">
                  @if (s.situacao === 'PLANEJADA' && e.situacao === 'ATIVA' && podeCoordenar()) { <button type="button" class="link-btn" (click)="executar(service.situacaoDaEstrutura(e.uuid, s.uuid, 'EM_OPERACAO', null))" [attr.aria-label]="'Abrir ' + s.nome">Abrir</button> }
                  @if (s.situacao !== 'DESATIVADA' && editavel() && podeCoordenar()) { <button type="button" class="link-btn" (click)="abrirEstrutura(s)" [attr.aria-label]="'Editar ' + s.nome">Editar</button> }
                  @if (s.ativadaEm && e.situacao !== 'ENCERRADA' && podeRegistrar()) { <button type="button" class="link-btn" (click)="abrirBoletim(s)" [attr.aria-label]="'Boletim de ' + s.nome">Boletim</button> }
                  @if (s.situacao !== 'DESATIVADA' && e.situacao !== 'ENCERRADA' && podeCoordenar()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'desativar', item: s })" [attr.aria-label]="'Desativar ' + s.nome">Desativar</button> }
                </td>
              </tr>
            }
          </tbody>
        </table></div>
      } @else {
        <p class="cell-sub">Nenhuma estrutura temporária. Posto médico, tenda de hidratação ou hospital de campanha entram aqui, ligados a uma unidade de referência.</p>
      }
    </div>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @switch (g.tipo) {
              @case ('eventoMassa') {
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-natureza">Natureza <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-es-natureza" (change)="f.set({ ...f(), natureza: $any($event.target).value })">
                      @for (n of listaNaturezas; track n) { <option [value]="n" [selected]="f()['natureza'] === n">{{ naturezas[n] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-es-risco">Classificação de risco <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-es-risco" (change)="f.set({ ...f(), risco: $any($event.target).value })">
                      @for (r of listaRiscos; track r) { <option [value]="r" [selected]="f()['risco'] === r">{{ riscos[r].rotulo }}</option> }
                    </select></div>
                </div>
                <div class="dw-field"><label for="f-es-local">Local <span class="req" aria-hidden="true">*</span></label><input id="f-es-local" type="text" maxlength="300" [value]="f()['local'] ?? ''" (input)="set('local', $event)" placeholder="Ex.: Avenida Central, do km 0 ao 3" /></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-inicio">Início previsto <span class="req" aria-hidden="true">*</span></label><input id="f-es-inicio" type="datetime-local" [value]="f()['inicio'] ?? ''" (input)="set('inicio', $event)" /></div>
                  <div class="dw-field"><label for="f-es-fim">Fim previsto <span class="req" aria-hidden="true">*</span></label><input id="f-es-fim" type="datetime-local" [value]="f()['fim'] ?? ''" (input)="set('fim', $event)" /></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-publico">Público estimado <span class="req" aria-hidden="true">*</span></label><input id="f-es-publico" type="number" min="1" [value]="f()['publico'] ?? ''" (input)="set('publico', $event)" /></div>
                  <div class="dw-field"><label for="f-es-maximo">Público máximo<span class="opt">opcional</span></label><input id="f-es-maximo" type="number" min="1" [value]="f()['maximo'] ?? ''" (input)="set('maximo', $event)" /></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-organizador">Organizador<span class="opt">opcional</span></label><input id="f-es-organizador" type="text" maxlength="200" [value]="f()['organizador'] ?? ''" (input)="set('organizador', $event)" /></div>
                  <div class="dw-field"><label for="f-es-contato">Contato<span class="opt">opcional</span></label><input id="f-es-contato" type="text" maxlength="200" [value]="f()['contato'] ?? ''" (input)="set('contato', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-es-observacao">Observação<span class="opt">opcional</span></label><textarea id="f-es-observacao" rows="2" maxlength="1000" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)"></textarea></div>
              }
              @case ('estrutura') {
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-tipo">Tipo <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-es-tipo" (change)="f.set({ ...f(), tipo: $any($event.target).value })">
                      @for (t of listaTipos; track t) { <option [value]="t" [selected]="f()['tipo'] === t">{{ tipos[t] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-es-nome">Nome <span class="req" aria-hidden="true">*</span></label><input id="f-es-nome" type="text" maxlength="200" [value]="f()['nome'] ?? ''" (input)="set('nome', $event)" placeholder="Ex.: Posto 1 · Concentração" /></div>
                </div>
                <div class="dw-field"><label for="f-es-local-est">Local <span class="req" aria-hidden="true">*</span></label><input id="f-es-local-est" type="text" maxlength="300" [value]="f()['local'] ?? ''" (input)="set('local', $event)" /></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-lat">Latitude<span class="opt">opcional</span></label><input id="f-es-lat" type="number" step="any" [value]="f()['latitude'] ?? ''" (input)="set('latitude', $event)" /></div>
                  <div class="dw-field"><label for="f-es-lng">Longitude<span class="opt">opcional</span></label><input id="f-es-lng" type="number" step="any" [value]="f()['longitude'] ?? ''" (input)="set('longitude', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-es-unidade">Unidade de referência <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-es-unidade" (change)="f.set({ ...f(), unidadeId: $any($event.target).value })">
                    <option value="" [selected]="!f()['unidadeId']">Escolha…</option>
                    @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['unidadeId'] === u.uuid">{{ u.nome }}</option> }
                  </select>
                  <span class="cell-sub">A unidade responde pela escala, pelo estoque e pelos leitos da estrutura.</span></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-leitos">Leitos de observação</label><input id="f-es-leitos" type="number" min="0" [value]="f()['leitos'] ?? ''" (input)="set('leitos', $event)" /></div>
                  <div class="dw-field"><label for="f-es-atendimentos">Atendimentos por dia<span class="opt">opcional</span></label><input id="f-es-atendimentos" type="number" min="1" [value]="f()['atendimentosDia'] ?? ''" (input)="set('atendimentosDia', $event)" /></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-ambulancias">Ambulâncias de prontidão</label><input id="f-es-ambulancias" type="number" min="0" [value]="f()['ambulancias'] ?? ''" (input)="set('ambulancias', $event)" /></div>
                  <div class="dw-field"><label for="f-es-horario">Horário<span class="opt">opcional</span></label><input id="f-es-horario" type="text" maxlength="200" [value]="f()['horario'] ?? ''" (input)="set('horario', $event)" placeholder="Ex.: 18h às 6h" /></div>
                </div>
              }
              @case ('desativar') {
                <dl class="ro"><div><dt>Estrutura</dt><dd>{{ g.item.nome }} · {{ tipos[g.item.tipo] }}</dd></div></dl>
                <div class="dw-field"><label for="f-es-motivo">Motivo <span class="req" aria-hidden="true">*</span></label><input id="f-es-motivo" type="text" maxlength="500" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)" /></div>
              }
              @case ('agravo') {
                <div class="dw-field"><label for="f-es-agravo">Agravo <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-es-agravo" (change)="f.set({ ...f(), agravoId: $any($event.target).value })">
                    <option value="" [selected]="!f()['agravoId']">Escolha…</option>
                    @for (a of agravos(); track a.uuid) { <option [value]="a.uuid" [selected]="f()['agravoId'] === a.uuid">{{ a.nome }}</option> }
                  </select>
                  <span class="cell-sub">O painel conta as notificações deste agravo na Vigilância. Agravo sensível não entra.</span></div>
              }
              @case ('exposicao') {
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-meio">Meio <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-es-meio" (change)="f.set({ ...f(), meio: $any($event.target).value })">
                      @for (m of listaMeios; track m) { <option [value]="m" [selected]="f()['meio'] === m">{{ meios[m] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-es-agente">Agente ou substância <span class="req" aria-hidden="true">*</span></label><input id="f-es-agente" type="text" maxlength="200" [value]="f()['agente'] ?? ''" (input)="set('agente', $event)" placeholder="Ex.: benzeno, fumaça, lama" /></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-fonte">Fonte<span class="opt">opcional</span></label><input id="f-es-fonte" type="text" maxlength="300" [value]="f()['fonte'] ?? ''" (input)="set('fonte', $event)" /></div>
                  <div class="dw-field"><label for="f-es-expostos">População exposta</label><input id="f-es-expostos" type="number" min="0" [value]="f()['populacao'] ?? ''" (input)="set('populacao', $event)" /></div>
                </div>
                @if (sugestao()?.unidades?.length) {
                  <fieldset class="dw-field"><legend>Unidades na área</legend>
                    @for (u of sugestao()!.unidades; track u.uuid) {
                      <label class="es-check"><input type="checkbox" [checked]="unidadesMarcadas().has(u.uuid)" (change)="alternarUnidade(u.uuid)" /> {{ u.nome }}</label>
                    }
                  </fieldset>
                }
                <div class="dw-field"><label for="f-es-medidas">Medidas<span class="opt">opcional</span></label><textarea id="f-es-medidas" rows="2" maxlength="1000" [value]="f()['medidas'] ?? ''" (input)="set('medidas', $event)"></textarea></div>
                <div class="dw-field"><label for="f-es-recomendacao">Recomendação à população<span class="opt">opcional</span></label><textarea id="f-es-recomendacao" rows="2" maxlength="1000" [value]="f()['recomendacao'] ?? ''" (input)="set('recomendacao', $event)"></textarea>
                  <span class="cell-sub">Fica registrada aqui; a divulgação é da Comunicação.</span></div>
              }
              @case ('boletim') {
                <dl class="ro"><div><dt>Estrutura</dt><dd>{{ g.item.nome }}</dd></div></dl>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-data">Data <span class="req" aria-hidden="true">*</span></label><input id="f-es-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                  <div class="dw-field"><label for="f-es-atend">Atendimentos <span class="req" aria-hidden="true">*</span></label><input id="f-es-atend" type="number" min="0" [value]="f()['atendimentos'] ?? ''" (input)="set('atendimentos', $event)" /></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-es-remocoes">Remoções para a rede</label><input id="f-es-remocoes" type="number" min="0" [value]="f()['remocoes'] ?? ''" (input)="set('remocoes', $event)" /></div>
                  <div class="dw-field"><label for="f-es-graves">Casos graves</label><input id="f-es-graves" type="number" min="0" [value]="f()['graves'] ?? ''" (input)="set('graves', $event)" /></div>
                  <div class="dw-field"><label for="f-es-obitos">Óbitos</label><input id="f-es-obitos" type="number" min="0" [value]="f()['obitos'] ?? ''" (input)="set('obitos', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-es-obs-bol">Observação<span class="opt">opcional</span></label><input id="f-es-obs-bol" type="text" maxlength="1000" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" placeholder="Ex.: turno da madrugada" /></div>
              }
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Voltar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar' }}</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .es-secao { margin-top: var(--s-4); }
    .es-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-2); flex-wrap: wrap; margin-bottom: var(--s-2); }
    .es-tabela { min-width: 0; width: 100%; }
    .es-numeros { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: var(--s-2); margin-bottom: var(--s-3); }
    .es-num { border: 1px solid var(--ink-200); border-radius: 10px; padding: var(--s-2) var(--s-3); display: grid; gap: 2px; }
    .es-num b { font-size: 1.05rem; }
    .es-num span { font-size: 0.75rem; color: var(--ink-500); }
    .es-check { display: flex; gap: var(--s-2); align-items: center; margin: var(--s-1) 0; font-weight: 400; }
    .dw-field .es-check input { width: auto; min-height: 0; padding: 0; }
  `,
})
export class EventoEspecializado {
  protected readonly service = inject(EmergenciaService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly vigilanciaService = inject(VigilanciaService);

  readonly emergencia = input.required<EmergenciaDto>();
  readonly podeCoordenar = input(false);
  readonly podeRegistrar = input(false);
  readonly alterada = output<EmergenciaDto>();

  protected readonly naturezas = NATUREZAS_EVENTO_MASSA;
  protected readonly listaNaturezas = Object.keys(NATUREZAS_EVENTO_MASSA) as NaturezaEventoMassa[];
  protected readonly riscos = RISCOS_EVENTO_MASSA;
  protected readonly listaRiscos = Object.keys(RISCOS_EVENTO_MASSA) as RiscoEventoMassa[];
  protected readonly tipos = TIPOS_ESTRUTURA;
  protected readonly listaTipos = Object.keys(TIPOS_ESTRUTURA) as TipoEstruturaTemporaria[];
  protected readonly situacoes = SITUACOES_ESTRUTURA;
  protected readonly meios = MEIOS_EXPOSICAO;
  protected readonly listaMeios = Object.keys(MEIOS_EXPOSICAO) as MeioExposicaoAmbiental[];
  protected readonly painel = signal<PainelEpidemiaDto | null>(null);
  protected readonly agravos = signal<AgravoDto[]>([]);
  protected readonly sugestao = signal<SugestaoImpactoDto | null>(null);
  protected readonly unidadesMarcadas = signal<Set<string>>(new Set());

  constructor() {
    // O painel da epidemia acompanha a emergência e o agravo escolhido.
    effect(() => {
      const e = this.emergencia();
      this.painel.set(null);
      if (e.perfil !== 'EPIDEMIA' || !e.agravoId) return;
      this.service.painelEpidemia(e.uuid).pipe(catchError(() => of(null))).subscribe((p) => this.painel.set(p));
    });
  }

  /** Planejada ou ativa: os dados do evento e as estruturas ainda mudam. */
  protected readonly editavel = computed(() => ['PLANEJADA', 'ATIVA'].includes(this.emergencia().situacao));
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});

  protected titulo(g: Gaveta): string {
    switch (g.tipo) {
      case 'eventoMassa': return 'Dados do evento de massa';
      case 'estrutura': return g.item ? 'Editar a estrutura' : 'Nova estrutura temporária';
      case 'desativar': return 'Desativar a estrutura';
      case 'agravo': return 'Agravo da epidemia';
      case 'exposicao': return 'Exposição ambiental';
      default: return 'Boletim da estrutura';
    }
  }

  protected abrir(g: Gaveta, campos: Record<string, string> = {}): void {
    this.erroGaveta.set(null);
    this.f.set(campos);
    this.gaveta.set(g);
  }

  protected abrirEventoMassa(): void {
    const m = this.emergencia().eventoMassa;
    this.abrir({ tipo: 'eventoMassa' }, {
      natureza: m?.natureza ?? 'FESTA_POPULAR', risco: m?.risco ?? 'MEDIO', local: m?.local ?? '', inicio: local(m?.inicioPrevisto), fim: local(m?.fimPrevisto),
      publico: m ? String(m.publicoEstimado) : '', maximo: m?.publicoMaximo != null ? String(m.publicoMaximo) : '', organizador: m?.organizador ?? '',
      contato: m?.contato ?? '', observacao: m?.observacao ?? '',
    });
  }

  protected abrirEstrutura(s: EstruturaTemporariaDto | null): void {
    if (!this.unidades().length) this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    const padrao = this.emergencia().perfil === 'EPIDEMIA' ? 'TENDA_HIDRATACAO' : this.emergencia().perfil === 'EVENTO_DE_MASSA' ? 'POSTO_MEDICO' : 'HOSPITAL_DE_CAMPANHA';
    this.abrir({ tipo: 'estrutura', item: s }, {
      tipo: s?.tipo ?? padrao, nome: s?.nome ?? '', local: s?.local ?? '', latitude: s?.latitude != null ? String(s.latitude) : '',
      longitude: s?.longitude != null ? String(s.longitude) : '', unidadeId: s?.unidadeReferenciaId ?? '', leitos: s ? String(s.leitos) : '0',
      atendimentosDia: s?.atendimentosDia != null ? String(s.atendimentosDia) : '', ambulancias: s ? String(s.ambulancias) : '0', horario: s?.horario ?? '',
    });
  }

  protected abrirAgravo(): void {
    if (!this.agravos().length) {
      this.vigilanciaService.agravos().pipe(catchError(() => of([]))).subscribe((l) => this.agravos.set(l.filter((a) => a.ativo && !a.sensivel)));
    }
    this.abrir({ tipo: 'agravo' }, { agravoId: this.emergencia().agravoId ?? '' });
  }

  /** As unidades e a população vêm da sugestão do impacto (Território e Organização); confira e ajuste. */
  protected abrirExposicao(): void {
    this.sugestao.set(null);
    this.unidadesMarcadas.set(new Set());
    this.abrir({ tipo: 'exposicao' }, { meio: 'AGUA' });
    this.service.sugestao(this.emergencia().uuid).pipe(catchError(() => of(null))).subscribe((s) => {
      this.sugestao.set(s);
      if (!s) return;
      this.unidadesMarcadas.set(new Set(s.unidades.map((u) => u.uuid)));
      if (s.populacaoAfetada != null) this.f.set({ ...this.f(), populacao: String(s.populacaoAfetada) });
    });
  }

  protected alternarUnidade(id: string): void {
    const s = new Set(this.unidadesMarcadas());
    if (s.has(id)) s.delete(id);
    else s.add(id);
    this.unidadesMarcadas.set(s);
  }

  protected abrirBoletim(s: EstruturaTemporariaDto): void {
    const hoje = new Date();
    const iso = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}-${String(hoje.getDate()).padStart(2, '0')}`;
    this.abrir({ tipo: 'boletim', item: s }, { data: iso });
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  /** Ação sem gaveta (abrir a estrutura). */
  protected executar(req: Observable<EmergenciaDto>): void {
    this.erro.set(null);
    req.subscribe({ next: (d) => this.alterada.emit(d), error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)) });
  }

  protected salvar(g: Gaveta): void {
    if (this.enviando()) return;
    const id = this.emergencia().uuid;
    const f = this.f();
    const t = (k: string) => (f[k] ?? '').trim() || null;
    const n = (k: string) => (t(k) == null ? null : Number(f[k]));
    let req: Observable<EmergenciaDto>;
    let falta: string | null = null;
    switch (g.tipo) {
      case 'eventoMassa':
        if (!t('local') || !t('inicio') || !t('fim') || !t('publico')) falta = 'Informe o local, o período e o público estimado.';
        req = this.service.salvarEventoMassa(id, {
          natureza: f['natureza'] as NaturezaEventoMassa, risco: f['risco'] as RiscoEventoMassa, local: t('local') ?? '',
          inicioPrevisto: t('inicio') ? new Date(f['inicio']).toISOString() : '', fimPrevisto: t('fim') ? new Date(f['fim']).toISOString() : '',
          publicoEstimado: n('publico') ?? 0, publicoMaximo: n('maximo'), organizador: t('organizador'), contato: t('contato'), observacao: t('observacao'),
        });
        break;
      case 'estrutura':
        if (!t('nome') || !t('local') || !t('unidadeId')) falta = 'Informe o nome, o local e a unidade de referência.';
        req = this.service.salvarEstrutura(id, g.item?.uuid ?? null, {
          tipo: f['tipo'] as TipoEstruturaTemporaria, nome: t('nome') ?? '', local: t('local') ?? '', latitude: n('latitude'), longitude: n('longitude'),
          unidadeReferenciaId: t('unidadeId') ?? '', leitos: n('leitos'), atendimentosDia: n('atendimentosDia'), ambulancias: n('ambulancias'), horario: t('horario'),
        });
        break;
      case 'agravo':
        if (!t('agravoId')) falta = 'Escolha o agravo.';
        req = this.service.definirAgravo(id, t('agravoId') ?? '');
        break;
      case 'exposicao':
        if (!t('agente')) falta = 'Informe o agente ou a substância.';
        req = this.service.registrarExposicao(id, {
          meio: f['meio'] as MeioExposicaoAmbiental, agente: t('agente') ?? '', fonte: t('fonte'), populacaoExposta: n('populacao'),
          unidadeIds: [...this.unidadesMarcadas()], medidas: t('medidas'), recomendacao: t('recomendacao'),
        });
        break;
      case 'desativar':
        if (!t('motivo')) falta = 'Informe o motivo.';
        req = this.service.situacaoDaEstrutura(id, g.item.uuid, 'DESATIVADA', t('motivo'));
        break;
      default:
        if (!t('data') || t('atendimentos') == null) falta = 'Informe a data e os atendimentos.';
        req = this.service.boletim(id, g.item.uuid, {
          data: t('data') ?? '', atendimentos: n('atendimentos') ?? 0, remocoes: n('remocoes'), casosGraves: n('graves'), obitos: n('obitos'), observacao: t('observacao'),
        });
    }
    if (falta) {
      this.erroGaveta.set(falta);
      return;
    }
    this.enviando.set(true);
    req.subscribe({
      next: (d) => {
        this.enviando.set(false);
        this.gaveta.set(null);
        this.alterada.emit(d);
      },
      error: (e: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(e));
      },
    });
  }

  protected fontePainel(p: PainelEpidemiaDto): string {
    const area = p.naAreaAfetada ? 'nas microáreas afetadas' : 'em toda a rede';
    return `Das notificações da Vigilância desde ${this.data(p.desde)}, pela data dos primeiros sintomas, ${area}.` + (p.doSurto ? ' O agravo vem do surto de origem.' : '');
  }

  protected detalheExposicao(x: ExposicaoAmbientalDto): string {
    const partes: string[] = [];
    if (x.populacaoExposta != null) partes.push(`${this.numero(x.populacaoExposta)} pessoas expostas`);
    if (x.fonte) partes.push(`fonte: ${x.fonte}`);
    if (x.unidades.length) partes.push(`${x.unidades.length} unidade(s): ${x.unidades.join(', ')}`);
    return partes.join(' · ');
  }

  protected numero(v: number): string {
    return v.toLocaleString('pt-BR');
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}

/** O instante no formato do campo datetime-local, na hora local. */
function local(iso: string | null | undefined): string {
  if (!iso) return '';
  const d = new Date(iso);
  const p = (v: number) => String(v).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
