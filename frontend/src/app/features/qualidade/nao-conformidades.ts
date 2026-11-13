import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../core/models/auth';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  AcaoQualidadeDto,
  CATEGORIAS_CAUSA,
  ChecklistDto,
  INDICADORES_QUALIDADE,
  IndicadorQualidade,
  CategoriaCausa,
  GRAVIDADES_NC,
  GravidadeNaoConformidade,
  MinhaAcaoDto,
  NaoConformidadeDto,
  ORIGENS_NC,
  OrigemNaoConformidade,
  PainelNcDto,
  SITUACOES_ACAO,
  SITUACOES_NC,
  SituacaoNaoConformidade,
  SugestaoNcDto,
} from '../../core/models/qualidade';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { AuthService } from '../../core/services/auth';
import { QualidadeService } from '../../core/services/qualidade';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { Drawer } from '../../shared/drawer/drawer';

type Aba = 'lista' | 'minhas' | 'sugestoes' | 'painel';

/**
 * Não conformidades e planos de ação, fatia Q2 da Qualidade (ADR-0204): o registro com a origem, a análise de causa (6M), o plano 5W2H
 * por ciclo, a verificação de eficácia (PDCA), as ações de quem está logado, as sugestões do incidente e da VISA, e o painel.
 */
@Component({
  selector: 'app-nao-conformidades',
  imports: [Drawer],
  template: `
    <header class="page-head">
      <div>
        <h1>Não conformidades</h1>
        <p>Planos de ação 5W2H e verificação de eficácia (PDCA)</p>
      </div>
      <div class="page-actions">
        @if (pode('QUALIDADE.GERIR')) { <button type="button" class="btn" (click)="abrirRegistro({})">Registrar não conformidade</button> }
      </div>
    </header>

    <div class="tabs" role="tablist" aria-label="Seções das não conformidades">
      @if (pode('QUALIDADE.GERIR')) { <button type="button" role="tab" id="tab-lista" [attr.aria-selected]="aba() === 'lista'" (click)="aba.set('lista')">Não conformidades</button> }
      <button type="button" role="tab" id="tab-minhas" [attr.aria-selected]="aba() === 'minhas'" (click)="aba.set('minhas')">Minhas ações @if (minhas().length) { <span class="cnt">{{ minhas().length }}</span> }</button>
      @if (pode('QUALIDADE.GERIR')) { <button type="button" role="tab" id="tab-sugestoes" [attr.aria-selected]="aba() === 'sugestoes'" (click)="aba.set('sugestoes')">Sugestões @if (sugestoes().length) { <span class="cnt">{{ sugestoes().length }}</span> }</button> }
      @if (podeAlguma('QUALIDADE.GERIR', 'INDICADOR.CONSULTAR')) { <button type="button" role="tab" id="tab-painel" [attr.aria-selected]="aba() === 'painel'" (click)="aba.set('painel')">Painel</button> }
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    @switch (aba()) {
      @case ('lista') {
        <div class="nc-cab">
          <div class="dw-field" style="margin: 0;"><label for="f-nc-sit">Situação</label>
            <select id="f-nc-sit" (change)="situacao.set($any($event.target).value); carregar()">
              <option value="">Todas</option>
              @for (s of listaSituacoes; track s) { <option [value]="s" [selected]="situacao() === s">{{ situacoes[s] }}</option> }
            </select></div>
        </div>
        <div class="tbl-wrap card">
          <table class="nc-tabela" aria-label="Não conformidades">
            <thead><tr><th scope="col">Número</th><th scope="col">Não conformidade</th><th scope="col">Origem</th><th scope="col">Gravidade</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (n of lista(); track n.uuid) {
                <tr>
                  <td><span class="cell-main mono">{{ n.numero }}</span><span class="cell-sub">{{ n.unidadeNome }}</span></td>
                  <td><span class="cell-sub" style="white-space: normal;">{{ n.descricao }}</span><span class="cell-sub">resp. {{ n.responsavelNome }}</span></td>
                  <td>{{ origens[n.origem] }}</td>
                  <td><span [class]="'tag tag--' + (n.gravidade === 'CRITICA' ? 'alert' : n.gravidade === 'MAIOR' ? 'warn' : 'ok')">{{ gravidades[n.gravidade] }}</span></td>
                  <td><span [class]="'tag tag--' + tagSituacao(n.situacao)">{{ situacoes[n.situacao] }}</span>@if (n.ciclo > 1) { <span class="cell-sub">ciclo {{ n.ciclo }}</span> }@if (n.acoesAtrasadas) { <span class="cell-sub nc-atraso">{{ n.acoesAtrasadas }} ação(ões) atrasada(s)</span> }</td>
                  <td class="row-actions"><button type="button" class="link-btn" (click)="abrirNc(n.uuid)" [attr.aria-label]="'Abrir não conformidade ' + n.numero">Abrir</button></td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma não conformidade.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('minhas') {
        <div class="tbl-wrap card">
          <table class="nc-tabela" aria-label="Minhas ações">
            <thead><tr><th scope="col">Ação</th><th scope="col">Não conformidade</th><th scope="col">Prazo</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (m of minhas(); track m.acao.uuid) {
                <tr>
                  <td><span class="cell-main">{{ m.acao.oQue }}</span><span class="cell-sub">{{ m.acao.porQue }}</span></td>
                  <td><span class="mono">{{ m.numero }}</span><span class="cell-sub">{{ m.unidadeNome }}</span></td>
                  <td class="mono">{{ data(m.acao.prazo) }}@if (m.acao.atrasada) { <span class="tag tag--alert">Atrasada</span> }</td>
                  <td>{{ situacoesAcao[m.acao.situacao] }}</td>
                  <td class="row-actions">
                    @if (m.acao.situacao === 'PENDENTE') { <button type="button" class="link-btn" (click)="andamento(m.acao, 'EM_ANDAMENTO')" [attr.aria-label]="'Iniciar ' + m.acao.oQue">Iniciar</button> }
                    <button type="button" class="link-btn" (click)="concluindo.set(m.acao.uuid); f.set({ ...f(), evidencia: '' })" [attr.aria-label]="'Concluir ' + m.acao.oQue">Concluir</button>
                  </td>
                </tr>
                @if (concluindo() === m.acao.uuid) {
                  <tr><td colspan="5">
                    <div class="nc-linha">
                      <div class="dw-field" style="margin: 0; flex: 1;"><label for="f-nc-evid">Evidência do que foi feito</label><input id="f-nc-evid" type="text" maxlength="2000" [value]="f()['evidencia'] ?? ''" (input)="set('evidencia', $event)" /></div>
                      <button type="button" class="btn btn--sm" (click)="andamento(m.acao, 'CONCLUIDA')" [disabled]="enviando()">Confirmar</button>
                    </div>
                  </td></tr>
                }
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma ação sob sua responsabilidade.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('sugestoes') {
        <div class="tbl-wrap card">
          <table class="nc-tabela" aria-label="Sugestões de não conformidade">
            <thead><tr><th scope="col">Origem</th><th scope="col">Unidade</th><th scope="col">O que foi visto</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (s of sugestoes(); track s.referenciaId + s.data) {
                <tr>
                  <td>{{ s.referencia }}<span class="cell-sub">{{ data(s.data) }}</span></td>
                  <td>{{ s.unidadeNome }}</td>
                  <td><span class="cell-sub" style="white-space: normal;">{{ s.descricao }}</span></td>
                  <td class="row-actions"><button type="button" class="link-btn" (click)="registrarDaSugestao(s)" [attr.aria-label]="'Abrir não conformidade de ' + s.referencia">Abrir não conformidade</button></td>
                </tr>
              } @empty { <tr><td colspan="4" class="cell-sub">Nenhuma sugestão.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('painel') {
        @if (painel(); as p) {
          <section class="stats" aria-label="Indicadores das não conformidades">
            <div class="stat"><div><div class="stat__lbl">Não conformidades</div><div class="stat__val mono">{{ p.total }}</div><div class="stat__sub">{{ p.emAberto }} em aberto</div></div></div>
            <div class="stat"><div><div class="stat__lbl">Ações atrasadas</div><div class="stat__val mono">{{ p.acoesAtrasadas }}</div><div class="stat__sub">prazo vencido</div></div></div>
            <div class="stat"><div><div class="stat__lbl">Eficácia na 1ª verificação</div><div class="stat__val mono">{{ p.eficaciaNaPrimeira == null ? '—' : p.eficaciaNaPrimeira.toLocaleString('pt-BR') + '%' }}</div><div class="stat__sub">{{ p.verificadas }} verificada(s)</div></div></div>
          </section>
          <div class="nc-grade">
            <div class="card"><h3 class="mo-grupo">Por origem</h3><ul class="nc-lista">@for (x of p.porOrigem; track x.codigo) { <li>{{ origens[x.codigo] }} <span class="mono">{{ x.quantidade }}</span></li> } @empty { <li class="cell-sub">Nenhuma.</li> }</ul></div>
            <div class="card"><h3 class="mo-grupo">Por gravidade</h3><ul class="nc-lista">@for (x of p.porGravidade; track x.codigo) { <li>{{ gravidades[x.codigo] }} <span class="mono">{{ x.quantidade }}</span></li> } @empty { <li class="cell-sub">Nenhuma.</li> }</ul></div>
            <div class="card"><h3 class="mo-grupo">Por situação</h3><ul class="nc-lista">@for (x of p.porSituacao; track x.codigo) { <li>{{ situacoes[x.codigo] }} <span class="mono">{{ x.quantidade }}</span></li> } @empty { <li class="cell-sub">Nenhuma.</li> }</ul></div>
          </div>
        }
      }
    }

    @if (registrando()) {
      <app-drawer titulo="Registrar não conformidade" (fechar)="registrando.set(false)">
        <form class="dw-form" (submit)="$event.preventDefault(); registrar()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <div class="dw-row">
              <div class="dw-field"><label for="f-nc-unidade">Unidade <span class="req" aria-hidden="true">*</span></label>
                <select id="f-nc-unidade" (change)="set('unidadeId', $event)">
                  <option value="" [selected]="!f()['unidadeId']">Escolha…</option>
                  @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['unidadeId'] === u.uuid">{{ u.nome }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-nc-origem">Origem</label>
                <select id="f-nc-origem" (change)="set('origem', $event)" [disabled]="!!f()['incidenteId'] || !!f()['irregularidadeId']">
                  @for (o of listaOrigens; track o) { <option [value]="o" [selected]="f()['origem'] === o">{{ origens[o] }}</option> }
                </select></div>
            </div>
            @if (f()['referencia']) { <p class="cell-sub">Vinculada a: {{ f()['referencia'] }}</p> }
            @if (f()['origem'] === 'INDICADOR') {
              <div class="dw-row">
                <div class="dw-field"><label for="f-nc-ind">Indicador <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-nc-ind" (change)="set('indicador', $event)">
                    <option value="" [selected]="!f()['indicador']">Escolha…</option>
                    @for (i of listaIndicadores; track i) { <option [value]="i" [selected]="f()['indicador'] === i">{{ indicadores[i] }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-nc-comp">Competência <span class="req" aria-hidden="true">*</span></label><input id="f-nc-comp" type="month" [value]="f()['competencia'] ?? ''" (input)="set('competencia', $event)" /></div>
              </div>
              @if (f()['indicador'] === 'ADESAO_PROTOCOLO') {
                <div class="dw-field"><label for="f-nc-check">Protocolo <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-nc-check" (change)="set('checklistId', $event)">
                    <option value="" [selected]="!f()['checklistId']">Escolha…</option>
                    @for (c of checklists(); track c.uuid) { <option [value]="c.uuid" [selected]="f()['checklistId'] === c.uuid">{{ c.nome }}</option> }
                  </select></div>
              }
            }
            <div class="dw-field"><label for="f-nc-desc">Não conformidade <span class="req" aria-hidden="true">*</span></label><textarea id="f-nc-desc" rows="3" maxlength="4000" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)"></textarea></div>
            <div class="dw-field"><label for="f-nc-correcao">Correção imediata<span class="opt">opcional</span></label><input id="f-nc-correcao" type="text" maxlength="2000" [value]="f()['correcao'] ?? ''" (input)="set('correcao', $event)" /></div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-nc-grav">Gravidade</label>
                <select id="f-nc-grav" (change)="set('gravidade', $event)">
                  @for (g of listaGravidades; track g) { <option [value]="g" [selected]="f()['gravidade'] === g">{{ gravidades[g] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-nc-resp">Matrícula do responsável <span class="req" aria-hidden="true">*</span></label><input id="f-nc-resp" type="text" [value]="f()['responsavel'] ?? ''" (input)="set('responsavel', $event)" /></div>
              <div class="dw-field"><label for="f-nc-data">Identificada em</label><input id="f-nc-data" type="date" [value]="f()['identificadaEm'] ?? ''" (input)="set('identificadaEm', $event)" /></div>
            </div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="registrando.set(false)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Registrar</button></div>
        </form>
      </app-drawer>
    }

    @if (detalhe(); as n) {
      <app-drawer [titulo]="'Não conformidade ' + n.numero" (fechar)="detalhe.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Situação</dt><dd>{{ situacoes[n.situacao] }} · ciclo {{ n.ciclo }}@if (n.verificacaoPrevista) { · verificar em {{ data(n.verificacaoPrevista) }} }@if (n.motivoCancelamento) { · {{ n.motivoCancelamento }} }</dd></div>
            <div><dt>Origem</dt><dd>{{ origens[n.origem] }}@if (n.incidenteNumero) { · incidente {{ n.incidenteNumero }} }@if (n.irregularidadeDescricao) { · {{ n.irregularidadeDescricao }} }@if (n.indicador) { · {{ indicadores[n.indicador] }}@if (n.checklistNome) { ({{ n.checklistNome }}) } em {{ competenciaRot(n.competencia) }} }</dd></div>
            <div><dt>Unidade</dt><dd>{{ n.unidadeNome }}@if (n.setorNome) { · {{ n.setorNome }} }</dd></div>
            <div><dt>Não conformidade</dt><dd>{{ n.descricao }}</dd></div>
            @if (n.correcaoImediata) { <div><dt>Correção imediata</dt><dd>{{ n.correcaoImediata }}</dd></div> }
            <div><dt>Responsável</dt><dd>{{ n.responsavelNome }} ({{ n.responsavelMatricula }})</dd></div>
          </dl>
          @if (aberta(n)) {
            <h3 class="mo-grupo nc-titulo">Análise de causa</h3>
            <fieldset class="nc-grupo"><legend>Categorias (6M)</legend>
              @for (c of listaCategorias; track c) { <label class="chk"><input type="checkbox" [checked]="categorias().has(c)" (change)="alternarCategoria(c)" /> {{ categoriasRot[c] }}</label> }
            </fieldset>
            <div class="dw-field"><label for="f-nc-analise">Causa</label><textarea id="f-nc-analise" rows="2" maxlength="4000" [value]="f()['analise'] ?? ''" (input)="set('analise', $event)"></textarea></div>
            <div class="row-actions"><button type="button" class="btn btn--ghost btn--sm" (click)="salvarAnalise(n)">Salvar análise</button></div>
          } @else if (n.analiseCausa) { <p><strong>Causa:</strong> {{ n.analiseCausa }}</p> }

          <h3 class="mo-grupo nc-titulo">Plano de ação (5W2H)</h3>
          <ul class="nc-acoes" aria-label="Ações do plano">
            @for (a of n.acoes; track a.uuid) {
              <li [class.nc-antiga]="a.ciclo !== n.ciclo">
                <span><strong>{{ a.oQue }}</strong> <span class="cell-sub">por quê: {{ a.porQue }} · quem: {{ a.responsavelNome }} · até {{ data(a.prazo) }}@if (a.onde) { · onde: {{ a.onde }} }@if (a.como) { · como: {{ a.como }} }@if (a.custo != null) { · {{ moeda(a.custo) }} } · ciclo {{ a.ciclo }}</span></span>
                <span><span [class]="'tag tag--' + (a.situacao === 'CONCLUIDA' ? 'ok' : a.atrasada ? 'alert' : a.situacao === 'CANCELADA' ? '' : 'warn')">{{ situacoesAcao[a.situacao] }}{{ a.atrasada ? ' · atrasada' : '' }}</span>@if (a.evidencia) { <span class="cell-sub">{{ a.evidencia }}</span> }</span>
                @if (a.ciclo === n.ciclo && (a.situacao === 'PENDENTE' || a.situacao === 'EM_ANDAMENTO') && aberta(n)) {
                  <span class="row-actions">
                    <button type="button" class="link-btn" (click)="concluindo.set(a.uuid); f.set({ ...f(), evidencia: '' })" [attr.aria-label]="'Concluir ' + a.oQue">Concluir</button>
                    <button type="button" class="link-btn" (click)="cancelarAcao(n, a)" [attr.aria-label]="'Cancelar ' + a.oQue">Cancelar</button>
                  </span>
                  @if (concluindo() === a.uuid) {
                    <div class="nc-linha">
                      <div class="dw-field" style="margin: 0; flex: 1;"><label for="f-nc-evid2">Evidência</label><input id="f-nc-evid2" type="text" maxlength="2000" [value]="f()['evidencia'] ?? ''" (input)="set('evidencia', $event)" /></div>
                      <button type="button" class="btn btn--sm" (click)="andamento(a, 'CONCLUIDA')" [disabled]="enviando()">Confirmar</button>
                    </div>
                  }
                }
              </li>
            } @empty { <li class="cell-sub">Nenhuma ação.</li> }
          </ul>
          @if (n.situacao === 'ABERTA' || n.situacao === 'EM_TRATAMENTO') {
            <fieldset class="nc-grupo"><legend>Nova ação</legend>
              <div class="dw-row">
                <div class="dw-field"><label for="f-nc-oque">O quê</label><input id="f-nc-oque" type="text" maxlength="500" [value]="f()['oque'] ?? ''" (input)="set('oque', $event)" /></div>
                <div class="dw-field"><label for="f-nc-porque">Por quê</label><input id="f-nc-porque" type="text" maxlength="500" [value]="f()['porque'] ?? ''" (input)="set('porque', $event)" /></div>
              </div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-nc-quem">Quem (matrícula)</label><input id="f-nc-quem" type="text" [value]="f()['quem'] ?? ''" (input)="set('quem', $event)" /></div>
                <div class="dw-field"><label for="f-nc-quando">Quando (prazo)</label><input id="f-nc-quando" type="date" [value]="f()['quando'] ?? ''" (input)="set('quando', $event)" /></div>
                <div class="dw-field"><label for="f-nc-onde">Onde<span class="opt">opcional</span></label><input id="f-nc-onde" type="text" maxlength="200" [value]="f()['onde'] ?? ''" (input)="set('onde', $event)" /></div>
              </div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-nc-como">Como<span class="opt">opcional</span></label><input id="f-nc-como" type="text" maxlength="1000" [value]="f()['como'] ?? ''" (input)="set('como', $event)" /></div>
                <div class="dw-field"><label for="f-nc-custo">Quanto custa (R$)<span class="opt">opcional</span></label><input id="f-nc-custo" type="number" min="0" step="0.01" [value]="f()['custo'] ?? ''" (input)="set('custo', $event)" /></div>
              </div>
              <div class="row-actions"><button type="button" class="btn btn--sm" (click)="adicionarAcao(n)" [disabled]="enviando()">Adicionar ação</button></div>
            </fieldset>
          }
          @if (n.verificacoes.length) {
            <h3 class="mo-grupo nc-titulo">Verificações</h3>
            <ul class="nc-lista" aria-label="Verificações de eficácia">@for (v of n.verificacoes; track v.ciclo) { <li>Ciclo {{ v.ciclo }}: {{ v.eficaz ? 'eficaz' : 'não eficaz' }} em {{ data(v.verificadaEm) }}@if (v.observacao) { · {{ v.observacao }} }</li> }</ul>
          }
          @if (n.situacao === 'EM_VERIFICACAO') {
            <h3 class="mo-grupo nc-titulo">Verificação de eficácia</h3>
            <div class="dw-field"><label for="f-nc-obs">Observação (obrigatória se não eficaz)</label><input id="f-nc-obs" type="text" maxlength="2000" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" /></div>
            <div class="row-actions">
              <button type="button" class="btn btn--ghost btn--sm" (click)="verificar(n, false)" [disabled]="enviando()">Não eficaz</button>
              <button type="button" class="btn btn--sm" (click)="verificar(n, true)" [disabled]="enviando()">Eficaz: encerrar</button>
            </div>
          }
          @if (aberta(n)) {
            <h3 class="mo-grupo nc-titulo">Cancelar</h3>
            <div class="nc-linha">
              <div class="dw-field" style="margin: 0; flex: 1;"><label for="f-nc-cancel">Motivo</label><input id="f-nc-cancel" type="text" maxlength="1000" [value]="f()['cancelamento'] ?? ''" (input)="set('cancelamento', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="cancelar(n)" [disabled]="enviando()">Cancelar não conformidade</button>
            </div>
          }
        </div>
      </app-drawer>
    }
  `,
  styles: `
    .nc-cab { display: flex; gap: var(--s-3); align-items: flex-end; margin-bottom: var(--s-2); }
    .nc-tabela { min-width: 0; width: 100%; }
    .nc-titulo { margin-top: var(--s-4); }
    .nc-grupo { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); margin: var(--s-2) 0; display: grid; gap: var(--s-1); }
    .nc-acoes { list-style: none; margin: 0; padding: 0; display: grid; gap: var(--s-2); }
    .nc-acoes li { border-left: 3px solid var(--ink-200); padding-left: var(--s-2); display: grid; gap: var(--s-1); }
    .nc-antiga { opacity: 0.7; }
    .nc-linha { display: flex; gap: var(--s-2); align-items: flex-end; flex-wrap: wrap; }
    .nc-atraso { color: var(--danger-600, #b42318); }
    .nc-grade { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: var(--s-3); margin-top: var(--s-3); }
    .nc-grade .card { padding: var(--s-3); }
    .nc-lista { margin: 0; padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .nc-lista li { display: flex; justify-content: space-between; gap: var(--s-2); }
    .chk { display: flex; gap: var(--s-2); align-items: center; }
  `,
})
export class NaoConformidades {
  private readonly service = inject(QualidadeService);
  private readonly authService = inject(AuthService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly rota = inject(ActivatedRoute).snapshot.queryParamMap;

  protected readonly origens = ORIGENS_NC;
  protected readonly listaOrigens = Object.keys(ORIGENS_NC) as OrigemNaoConformidade[];
  protected readonly indicadores = INDICADORES_QUALIDADE;
  protected readonly listaIndicadores = Object.keys(INDICADORES_QUALIDADE) as IndicadorQualidade[];
  protected readonly gravidades = GRAVIDADES_NC;
  protected readonly listaGravidades = Object.keys(GRAVIDADES_NC) as GravidadeNaoConformidade[];
  protected readonly situacoes = SITUACOES_NC;
  protected readonly listaSituacoes = Object.keys(SITUACOES_NC) as SituacaoNaoConformidade[];
  protected readonly situacoesAcao = SITUACOES_ACAO;
  protected readonly categoriasRot = CATEGORIAS_CAUSA;
  protected readonly listaCategorias = Object.keys(CATEGORIAS_CAUSA) as CategoriaCausa[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('lista');
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly situacao = signal('');
  protected readonly lista = signal<NaoConformidadeDto[]>([]);
  protected readonly minhas = signal<MinhaAcaoDto[]>([]);
  protected readonly sugestoes = signal<SugestaoNcDto[]>([]);
  protected readonly painel = signal<PainelNcDto | null>(null);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly checklists = signal<ChecklistDto[]>([]);
  protected readonly registrando = signal(false);
  protected readonly detalhe = signal<NaoConformidadeDto | null>(null);
  protected readonly categorias = signal<Set<string>>(new Set());
  protected readonly concluindo = signal<string | null>(null);
  protected readonly abertas = computed(() => this.lista().filter((n) => this.aberta(n)).length);

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      if (!this.pode('QUALIDADE.GERIR')) this.aba.set('minhas');
      this.carregar();
      // O atalho do incidente e da irregularidade (ADR-0204): abre o registro já vinculado.
      const origem = this.rota.get('origem');
      if (origem && this.pode('QUALIDADE.GERIR')) {
        this.abrirRegistro({ origem, unidadeId: this.rota.get('unidade') ?? '', incidenteId: this.rota.get('incidente') ?? '',
          irregularidadeId: this.rota.get('irregularidade') ?? '', referencia: this.rota.get('referencia') ?? '', indicador: this.rota.get('indicador') ?? '',
          competencia: this.rota.get('competencia') ?? '', checklistId: this.rota.get('checklist') ?? '', descricao: this.rota.get('descricao') ?? '' });
      }
    });
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidades.set(l));
  }

  protected pode(permissao: string): boolean {
    const a = this.acesso();
    return !a.restrito || a.permissoes.has(permissao);
  }

  protected podeAlguma(...permissoes: string[]): boolean {
    return permissoes.some((p) => this.pode(p));
  }

  protected aberta(n: NaoConformidadeDto): boolean {
    return n.situacao !== 'ENCERRADA' && n.situacao !== 'CANCELADA';
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected abrirRegistro(campos: Record<string, string>): void {
    this.erroGaveta.set(null);
    this.detalhe.set(null);
    this.f.set({ origem: 'AUDITORIA_INTERNA', gravidade: 'MAIOR', ...campos });
    this.registrando.set(true);
  }

  protected registrarDaSugestao(s: SugestaoNcDto): void {
    this.abrirRegistro({ origem: s.origem, unidadeId: s.unidadeId, referencia: s.referencia + ': ' + s.descricao, descricao: s.descricao,
      incidenteId: s.origem === 'INCIDENTE' ? s.referenciaId : '', irregularidadeId: s.origem === 'INSPECAO_SANITARIA' ? s.referenciaId : '',
      indicador: s.indicador ?? '', competencia: s.indicador ? s.data.slice(0, 7) : '', checklistId: s.checklistId ?? '' });
  }

  protected registrar(): void {
    const c = this.f();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    if (!t('unidadeId') || !t('descricao') || !t('responsavel')) {
      this.erroGaveta.set('Informe a unidade, a não conformidade e a matrícula do responsável.');
      return;
    }
    const indicador = c['origem'] === 'INDICADOR';
    if (indicador && (!t('indicador') || !t('competencia') || (c['indicador'] === 'ADESAO_PROTOCOLO' && !t('checklistId')))) {
      this.erroGaveta.set('Informe o indicador, a competência e, na adesão, o protocolo.');
      return;
    }
    this.executar(this.service.registrarNc({ unidadeId: t('unidadeId'), origem: c['origem'], incidenteId: t('incidenteId'), irregularidadeId: t('irregularidadeId'),
      descricao: t('descricao'), correcaoImediata: t('correcao'), gravidade: c['gravidade'], identificadaEm: t('identificadaEm'), responsavelMatricula: t('responsavel'),
      indicador: indicador ? t('indicador') : null, competencia: indicador ? t('competencia') + '-01' : null,
      checklistId: indicador && c['indicador'] === 'ADESAO_PROTOCOLO' ? t('checklistId') : null }),
      (n) => {
        this.registrando.set(false);
        this.mostrar(n);
      });
  }

  protected abrirNc(id: string): void {
    this.erroGaveta.set(null);
    this.service.naoConformidade(id).subscribe({ next: (n) => this.mostrar(n), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  private mostrar(n: NaoConformidadeDto): void {
    this.detalhe.set(n);
    this.categorias.set(new Set(n.categoriasCausa));
    this.concluindo.set(null);
    this.f.set({ analise: n.analiseCausa ?? '' });
  }

  protected alternarCategoria(c: string): void {
    const s = new Set(this.categorias());
    if (s.has(c)) s.delete(c); else s.add(c);
    this.categorias.set(s);
  }

  protected salvarAnalise(n: NaoConformidadeDto): void {
    const analise = (this.f()['analise'] ?? '').trim();
    if (!analise) {
      this.erroGaveta.set('Descreva a causa.');
      return;
    }
    this.executar(this.service.acaoNc(n.uuid, 'analise', { analiseCausa: analise, categorias: [...this.categorias()] }), (x) => this.mostrar(x));
  }

  protected adicionarAcao(n: NaoConformidadeDto): void {
    const c = this.f();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    if (!t('oque') || !t('porque') || !t('quem') || !t('quando')) {
      this.erroGaveta.set('Informe o quê, por quê, quem e o prazo.');
      return;
    }
    this.executar(this.service.acaoNc(n.uuid, 'acao', { descricao: t('oque'), justificativa: t('porque'), responsavelMatricula: t('quem'), prazo: t('quando'),
      onde: t('onde'), como: t('como'), custo: t('custo') == null ? null : Number(c['custo']) }), (x) => this.mostrar(x));
  }

  protected andamento(a: AcaoQualidadeDto, situacao: 'EM_ANDAMENTO' | 'CONCLUIDA'): void {
    const evidencia = (this.f()['evidencia'] ?? '').trim();
    if (situacao === 'CONCLUIDA' && !evidencia) {
      this.erro.set('Informe a evidência do que foi feito.');
      this.erroGaveta.set('Informe a evidência do que foi feito.');
      return;
    }
    this.enviando.set(true);
    this.service.andamento(a.uuid, { situacao, evidencia: evidencia || null }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.concluindo.set(null);
        this.erro.set(null);
        const d = this.detalhe();
        if (d) this.abrirNc(d.uuid);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set(mensagem(x));
      },
    });
  }

  protected cancelarAcao(n: NaoConformidadeDto, a: AcaoQualidadeDto): void {
    this.executar(this.service.cancelarAcao(a.uuid, 'Cancelada pela gestão da qualidade.'), (x) => this.mostrar(x));
  }

  protected verificar(n: NaoConformidadeDto, eficaz: boolean): void {
    const observacao = (this.f()['observacao'] ?? '').trim();
    if (!eficaz && !observacao) {
      this.erroGaveta.set('A verificação não eficaz pede a observação.');
      return;
    }
    this.executar(this.service.acaoNc(n.uuid, 'verificacao', { eficaz, observacao: observacao || null }), (x) => this.mostrar(x));
  }

  protected cancelar(n: NaoConformidadeDto): void {
    const motivo = (this.f()['cancelamento'] ?? '').trim();
    if (!motivo) {
      this.erroGaveta.set('Informe o motivo do cancelamento.');
      return;
    }
    this.executar(this.service.acaoNc(n.uuid, 'cancelamento', { motivo }), (x) => this.mostrar(x));
  }

  private executar(req: Observable<NaoConformidadeDto>, depois: (n: NaoConformidadeDto) => void): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    req.subscribe({
      next: (n) => {
        this.enviando.set(false);
        depois(n);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected tagSituacao(s: SituacaoNaoConformidade): string {
    return s === 'ENCERRADA' ? 'ok' : s === 'CANCELADA' ? '' : s === 'EM_VERIFICACAO' ? 'info' : 'warn';
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  protected competenciaRot(iso: string | null): string {
    return iso ? `${iso.slice(5, 7)}/${iso.slice(0, 4)}` : '';
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected carregar(): void {
    this.erro.set(null);
    this.service.minhasAcoes().pipe(catchError(() => of([]))).subscribe((l) => this.minhas.set(l));
    if (this.pode('QUALIDADE.GERIR')) {
      this.service.naoConformidades((this.situacao() || undefined) as SituacaoNaoConformidade | undefined).subscribe({
        next: (l) => this.lista.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
      });
      this.service.sugestoesNc().pipe(catchError(() => of([]))).subscribe((l) => this.sugestoes.set(l));
      if (!this.checklists().length) this.service.checklists().pipe(catchError(() => of([]))).subscribe((l) => this.checklists.set(l));
    }
    if (this.podeAlguma('QUALIDADE.GERIR', 'INDICADOR.CONSULTAR')) {
      this.service.painelNc().pipe(catchError(() => of(null))).subscribe((p) => this.painel.set(p));
    }
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
