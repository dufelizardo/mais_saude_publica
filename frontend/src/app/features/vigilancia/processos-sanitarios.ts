import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  AGRAVANTES,
  ATENUANTES,
  AgravanteSanitaria,
  AtenuanteSanitaria,
  EstabelecimentoReguladoDto,
  FASES_PROCESSO,
  FORMAS_CIENCIA,
  FaseProcessoSanitario,
  FormaCiencia,
  GRAVIDADES,
  GravidadeIrregularidade,
  IrregularidadeSanitariaDto,
  MedidaSanitariaDto,
  PENALIDADES,
  ProcessoSanitarioDto,
  TIPOS_MEDIDA,
  TIPOS_TERMO,
  TermoSanitarioDto,
  TipoMedidaSanitaria,
  TipoPenalidadeSanitaria,
} from '../../core/models/vigilancia-sanitaria';
import { VigilanciaSanitariaService } from '../../core/services/vigilancia-sanitaria';
import { Drawer } from '../../shared/drawer/drawer';

export type SecaoProcessos = 'processos' | 'medidas';
type Gaveta =
  | { tipo: 'lavrar' }
  | { tipo: 'processo'; id: string }
  | { tipo: 'medida'; medida: 'INTERDICAO_CAUTELAR' | 'APREENSAO' }
  | { tipo: 'encerrarMedida'; medida: MedidaSanitariaDto };
type PenalidadeNova = { tipo: TipoPenalidadeSanitaria; valorMulta: string; suspensaoAte: string; descricao: string };

/**
 * Processo administrativo sanitário e medidas (ADR-0200), nas abas da tela Vigilância sanitária: o auto de infração, as fases do
 * processo (ciência, defesa, decisão, recurso, encerramento) com os prazos, as penalidades e os termos; a interdição cautelar, a
 * apreensão e a desinterdição.
 */
@Component({
  selector: 'app-processos-sanitarios',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (secao() === 'processos') {
      <div class="ps-cab">
        <div class="dw-field" style="margin: 0;"><label for="f-ps-fase">Fase</label>
          <select id="f-ps-fase" (change)="fase.set($any($event.target).value); carregar()">
            <option value="">Todas</option>
            @for (x of listaFases; track x) { <option [value]="x" [selected]="fase() === x">{{ fases[x] }}</option> }
          </select></div>
        @if (podeInspecionar()) { <button type="button" class="btn btn--sm" (click)="abrirLavrar('')">Lavrar auto de infração</button> }
      </div>
      <div class="tbl-wrap card">
        <table class="ps-tabela" aria-label="Processos sanitários">
          <thead><tr><th scope="col">Processo</th><th scope="col">Estabelecimento</th><th scope="col">Gravidade</th><th scope="col">Fase</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (p of processos(); track p.uuid) {
              <tr>
                <td><span class="cell-main mono">{{ p.numero }}</span><span class="cell-sub">auto de {{ data(p.dataLavratura) }}</span></td>
                <td>{{ p.estabelecimentoNome }}</td>
                <td><span [class]="'tag tag--' + (p.gravidade === 'GRAVISSIMA' ? 'alert' : p.gravidade === 'GRAVE' ? 'warn' : 'ok')">{{ gravidades[p.gravidade] }}</span></td>
                <td><span class="tag">{{ fases[p.fase] }}</span>@if (prazo(p); as x) { <span class="cell-sub">{{ x }}</span> }</td>
                <td class="row-actions"><button type="button" class="link-btn" (click)="abrirProcesso(p.uuid)" [attr.aria-label]="'Abrir processo ' + p.numero">Abrir</button></td>
              </tr>
            } @empty { <tr><td colspan="5" class="cell-sub">Nenhum processo.</td></tr> }
          </tbody>
        </table>
      </div>
    } @else {
      <div class="ps-cab">
        <div class="dw-field" style="margin: 0;"><label for="f-ps-sit">Situação</label>
          <select id="f-ps-sit" (change)="situacao.set($any($event.target).value); carregar()">
            <option value="VIGENTE" [selected]="situacao() === 'VIGENTE'">Vigentes</option>
            <option value="ENCERRADA" [selected]="situacao() === 'ENCERRADA'">Encerradas</option>
            <option value="" [selected]="!situacao()">Todas</option>
          </select></div>
        @if (podeInspecionar()) {
          <div class="row-actions">
            <button type="button" class="btn btn--ghost btn--sm" (click)="abrirMedida('APREENSAO')">Apreender produto</button>
            <button type="button" class="btn btn--sm" (click)="abrirMedida('INTERDICAO_CAUTELAR')">Interdição cautelar</button>
          </div>
        }
      </div>
      <div class="tbl-wrap card">
        <table class="ps-tabela" aria-label="Medidas sanitárias">
          <thead><tr><th scope="col">Medida</th><th scope="col">Estabelecimento</th><th scope="col">Tipo</th><th scope="col">Período</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (m of medidas(); track m.uuid) {
              <tr>
                <td><span class="cell-main mono">{{ m.numero }}</span>@if (m.processoNumero) { <span class="cell-sub">processo {{ m.processoNumero }}</span> }</td>
                <td>{{ m.estabelecimentoNome }}</td>
                <td>{{ tiposMedida[m.tipo] }}@if (m.tipo !== 'APREENSAO') { {{ m.total ? 'total' : 'parcial' }} }<span class="cell-sub">{{ m.produto ?? m.descricao }}</span></td>
                <td class="mono">{{ data(m.inicio) }}@if (m.fimPrevisto) { a {{ data(m.fimPrevisto) }} }</td>
                <td><span [class]="'tag tag--' + (m.situacao === 'VIGENTE' ? (m.vencida ? 'alert' : 'warn') : 'ok')">{{ m.situacao === 'VIGENTE' ? (m.vencida ? 'Vencida' : 'Vigente') : 'Encerrada' }}</span>@if (m.motivoEncerramento) { <span class="cell-sub">{{ m.motivoEncerramento }}</span> }</td>
                <td class="row-actions">
                  @for (t of m.termos; track t.uuid) { <button type="button" class="link-btn" (click)="baixar(t)" [attr.aria-label]="tiposTermo[t.tipo] + ' ' + m.numero">{{ tiposTermo[t.tipo] }}</button> }
                  @if (m.situacao === 'VIGENTE' && podeGerenciar()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'encerrarMedida', medida: m })" [attr.aria-label]="'Encerrar medida ' + m.numero">{{ m.tipo === 'APREENSAO' ? 'Destinar' : 'Desinterditar' }}</button> }
                </td>
              </tr>
            } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma medida.</td></tr> }
          </tbody>
        </table>
      </div>
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" (fechar)="fechar()">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @switch (g.tipo) {
              @case ('lavrar') {
                <div class="dw-field"><label for="f-ps-estab">Estabelecimento <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-ps-estab" (change)="mudarEstabelecimento($any($event.target).value)">
                    <option value="" [selected]="!f()['estabelecimentoId']">Escolha…</option>
                    @for (e of estabelecimentos(); track e.uuid) { <option [value]="e.uuid" [selected]="f()['estabelecimentoId'] === e.uuid">{{ e.nomeFantasia ?? e.razaoSocial }}</option> }
                  </select></div>
                <fieldset class="ps-grupo"><legend>Irregularidades abertas a autuar</legend>
                  @for (r of abertas(); track r.uuid) {
                    <label class="chk"><input type="checkbox" [checked]="marcadas().has(r.uuid)" (change)="alternar(marcadas, r.uuid)" [attr.aria-label]="'Autuar: ' + r.descricao" /> {{ r.descricao }} <span class="cell-sub">{{ gravidades[r.gravidade] }}</span></label>
                  } @empty { <p class="cell-sub">{{ f()['estabelecimentoId'] ? 'Nenhuma irregularidade aberta.' : 'Escolha o estabelecimento.' }}</p> }
                </fieldset>
                <div class="dw-field"><label for="f-ps-disp">Dispositivo legal infringido <span class="req" aria-hidden="true">*</span></label><input id="f-ps-disp" type="text" maxlength="500" placeholder="Ex.: Lei 6.437/1977, art. 10, IV" [value]="f()['dispositivoLegal'] ?? ''" (input)="set('dispositivoLegal', $event)" /></div>
                <div class="dw-field"><label for="f-ps-desc">Descrição da infração <span class="req" aria-hidden="true">*</span></label><textarea id="f-ps-desc" rows="3" maxlength="2000" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)"></textarea></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-ps-grav">Gravidade<span class="opt">a maior se vazio</span></label>
                    <select id="f-ps-grav" (change)="f.set({ ...f(), gravidade: $any($event.target).value })">
                      <option value="">Pela maior irregularidade</option>
                      @for (x of listaGravidades; track x) { <option [value]="x">{{ gravidades[x] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-ps-data">Lavrado em</label><input id="f-ps-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-ps-fiscais">Matrículas dos fiscais <span class="req" aria-hidden="true">*</span></label><input id="f-ps-fiscais" type="text" placeholder="Separadas por vírgula; inclua a sua" [value]="f()['fiscais'] ?? ''" (input)="set('fiscais', $event)" /></div>
              }
              @case ('processo') {
                @if (processo(); as p) {
                  <dl class="ro">
                    <div><dt>Estabelecimento</dt><dd>{{ p.estabelecimentoNome }}</dd></div>
                    <div><dt>Infração</dt><dd>{{ p.dispositivoLegal }} · {{ gravidades[p.gravidade] }}<span class="cell-sub">{{ p.descricao }}</span></dd></div>
                    <div><dt>Irregularidades</dt><dd>@for (r of p.irregularidades; track r.uuid) { <span class="cell-sub">{{ r.descricao }} ({{ gravidades[r.gravidade] }})</span> }</dd></div>
                    <div><dt>Fiscais</dt><dd>{{ nomes(p) }}</dd></div>
                  </dl>
                  <h3 class="mo-grupo ps-titulo">Andamento</h3>
                  <ol class="ps-linha" aria-label="Andamento do processo">
                    <li>Auto lavrado em {{ data(p.dataLavratura) }}</li>
                    @if (p.cienciaAutoEm) { <li>Ciência do auto em {{ data(p.cienciaAutoEm) }} ({{ formas[p.formaCienciaAuto!] }}); defesa até {{ data(p.prazoDefesa!) }}</li> }
                    @if (p.defesaEm) { <li>Defesa em {{ data(p.defesaEm) }}@if (p.defesaIntempestiva) { (intempestiva) }<span class="cell-sub">{{ p.defesaTexto }}</span></li> } @else if (p.revelia) { <li>Sem defesa no prazo (revelia)</li> }
                    @if (p.dataDecisao) { <li>Decisão em {{ data(p.dataDecisao) }}: {{ p.decisaoResultado === 'ARQUIVAMENTO' ? 'arquivamento' : 'penalidades' }}<span class="cell-sub">{{ p.decisaoFundamentacao }}</span></li> }
                    @if (p.cienciaDecisaoEm) { <li>Ciência da decisão em {{ data(p.cienciaDecisaoEm) }}; recurso até {{ data(p.prazoRecurso!) }}</li> }
                    @if (p.recursoEm) { <li>Recurso em {{ data(p.recursoEm) }}<span class="cell-sub">{{ p.recursoTexto }}</span></li> }
                    @if (p.dataJulgamentoRecurso) { <li>Recurso julgado em {{ data(p.dataJulgamentoRecurso) }}: {{ resultadosRecurso[p.recursoResultado!] }}</li> }
                    @if (p.encerradoEm) { <li>Encerrado em {{ data(p.encerradoEm) }}</li> }
                  </ol>
                  @if (p.penalidades.length) {
                    <h3 class="mo-grupo ps-titulo">Penalidades</h3>
                    <ul class="ps-lista" aria-label="Penalidades">
                      @for (x of p.penalidades; track x.uuid) { <li [class.ps-revogada]="x.revogada">{{ penalidades[x.tipo] }}@if (x.valorMulta != null) { de {{ moeda(x.valorMulta) }} }@if (x.suspensaoAte) { até {{ data(x.suspensaoAte) }} }@if (x.descricao) { : {{ x.descricao }} }@if (x.revogada) { (revogada) }</li> }
                    </ul>
                  }
                  <h3 class="mo-grupo ps-titulo">Termos</h3>
                  <ul class="ps-lista" aria-label="Termos do processo">
                    @for (t of termosDoProcesso(p); track t.uuid) { <li>{{ tiposTermo[t.tipo] }} <span class="cell-sub mono">{{ t.codigo }}</span> <button type="button" class="link-btn" (click)="baixar(t)">Baixar</button></li> }
                  </ul>

                  @switch (proximo(p)) {
                    @case ('ciencia') {
                      <h3 class="mo-grupo ps-titulo">{{ p.fase === 'AGUARDANDO_CIENCIA' ? 'Ciência do auto' : 'Ciência da decisão' }}</h3>
                      <div class="dw-row">
                        <div class="dw-field"><label for="f-ps-forma">Forma</label>
                          <select id="f-ps-forma" (change)="f.set({ ...f(), forma: $any($event.target).value })">
                            @for (x of listaFormas; track x) { <option [value]="x" [selected]="f()['forma'] === x">{{ formas[x] }}</option> }
                          </select></div>
                        <div class="dw-field"><label for="f-ps-dciencia">Data</label><input id="f-ps-dciencia" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                      </div>
                    }
                    @case ('defesa') {
                      <h3 class="mo-grupo ps-titulo">Defesa</h3>
                      <div class="dw-field"><label for="f-ps-ddefesa">Protocolada em</label><input id="f-ps-ddefesa" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                      <div class="dw-field"><label for="f-ps-defesa">Resumo da defesa <span class="req" aria-hidden="true">*</span></label><textarea id="f-ps-defesa" rows="3" maxlength="4000" [value]="f()['texto'] ?? ''" (input)="set('texto', $event)"></textarea></div>
                    }
                    @case ('decisao') {
                      <h3 class="mo-grupo ps-titulo">Decisão</h3>
                      <div class="dw-row">
                        <div class="dw-field"><label for="f-ps-res">Resultado</label>
                          <select id="f-ps-res" (change)="f.set({ ...f(), resultado: $any($event.target).value })">
                            <option value="PENALIDADE" [selected]="f()['resultado'] === 'PENALIDADE'">Aplicar penalidades</option>
                            <option value="ARQUIVAMENTO" [selected]="f()['resultado'] === 'ARQUIVAMENTO'">Arquivar</option>
                          </select></div>
                        <div class="dw-field"><label for="f-ps-ddec">Data</label><input id="f-ps-ddec" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                      </div>
                      <div class="dw-field"><label for="f-ps-fund">Fundamentação <span class="req" aria-hidden="true">*</span></label><textarea id="f-ps-fund" rows="3" maxlength="4000" [value]="f()['fundamentacao'] ?? ''" (input)="set('fundamentacao', $event)"></textarea></div>
                      @if (f()['resultado'] === 'PENALIDADE') {
                        <fieldset class="ps-grupo"><legend>Atenuantes (art. 7º)</legend>
                          @for (x of listaAtenuantes; track x) { <label class="chk"><input type="checkbox" [checked]="atenuantes().has(x)" (change)="alternar(atenuantes, x)" /> {{ atenuantesRot[x] }}</label> }
                        </fieldset>
                        <fieldset class="ps-grupo"><legend>Agravantes (art. 8º)</legend>
                          @for (x of listaAgravantes; track x) { <label class="chk"><input type="checkbox" [checked]="agravantes().has(x)" (change)="alternar(agravantes, x)" /> {{ agravantesRot[x] }}</label> }
                        </fieldset>
                        <fieldset class="ps-grupo"><legend>Penalidades (art. 2º)</legend>
                          @for (x of novas(); track $index; let i = $index) {
                            <div class="dw-row">
                              <div class="dw-field"><label [for]="'f-ps-pen-' + i">Penalidade</label>
                                <select [id]="'f-ps-pen-' + i" (change)="mudarPenalidade(i, 'tipo', $event)">
                                  @for (t of listaPenalidades; track t) { <option [value]="t" [selected]="x.tipo === t">{{ penalidades[t] }}</option> }
                                </select></div>
                              @if (x.tipo === 'MULTA') { <div class="dw-field"><label [for]="'f-ps-valor-' + i">Valor (R$)</label><input [id]="'f-ps-valor-' + i" type="number" min="0.01" step="0.01" [value]="x.valorMulta" (input)="mudarPenalidade(i, 'valorMulta', $event)" /></div> }
                              @if (x.tipo === 'SUSPENSAO_LICENCA') { <div class="dw-field"><label [for]="'f-ps-ate-' + i">Suspensa até</label><input [id]="'f-ps-ate-' + i" type="date" [value]="x.suspensaoAte" (input)="mudarPenalidade(i, 'suspensaoAte', $event)" /></div> }
                              <div class="dw-field"><label [for]="'f-ps-pdesc-' + i">Detalhe<span class="opt">opcional</span></label><input [id]="'f-ps-pdesc-' + i" type="text" maxlength="500" [value]="x.descricao" (input)="mudarPenalidade(i, 'descricao', $event)" /></div>
                              <button type="button" class="link-btn" (click)="novas.set(remover(novas(), i))">Remover</button>
                            </div>
                          }
                          <button type="button" class="btn btn--ghost btn--sm" (click)="novas.set([...novas(), { tipo: 'ADVERTENCIA', valorMulta: '', suspensaoAte: '', descricao: '' }])">Adicionar penalidade</button>
                        </fieldset>
                      }
                    }
                    @case ('recurso') {
                      <h3 class="mo-grupo ps-titulo">Recurso</h3>
                      <div class="dw-field"><label for="f-ps-drec">Protocolado em</label><input id="f-ps-drec" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                      <div class="dw-field"><label for="f-ps-rec">Resumo do recurso <span class="req" aria-hidden="true">*</span></label><textarea id="f-ps-rec" rows="3" maxlength="4000" [value]="f()['texto'] ?? ''" (input)="set('texto', $event)"></textarea></div>
                    }
                    @case ('julgamento') {
                      <h3 class="mo-grupo ps-titulo">Julgamento do recurso</h3>
                      <div class="dw-row">
                        <div class="dw-field"><label for="f-ps-jres">Resultado</label>
                          <select id="f-ps-jres" (change)="f.set({ ...f(), resultado: $any($event.target).value })">
                            <option value="IMPROVIDO" [selected]="f()['resultado'] === 'IMPROVIDO'">Improvido</option>
                            <option value="PARCIALMENTE_PROVIDO" [selected]="f()['resultado'] === 'PARCIALMENTE_PROVIDO'">Parcialmente provido</option>
                            <option value="PROVIDO" [selected]="f()['resultado'] === 'PROVIDO'">Provido</option>
                          </select></div>
                        <div class="dw-field"><label for="f-ps-djul">Data</label><input id="f-ps-djul" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                      </div>
                      <div class="dw-field"><label for="f-ps-jfund">Fundamentação <span class="req" aria-hidden="true">*</span></label><textarea id="f-ps-jfund" rows="3" maxlength="4000" [value]="f()['fundamentacao'] ?? ''" (input)="set('fundamentacao', $event)"></textarea></div>
                      @if (f()['resultado'] === 'PARCIALMENTE_PROVIDO') {
                        <fieldset class="ps-grupo"><legend>Penalidades que ficam</legend>
                          @for (x of p.penalidades; track x.uuid) { <label class="chk"><input type="checkbox" [checked]="mantidas().has(x.uuid)" (change)="alternar(mantidas, x.uuid)" /> {{ penalidades[x.tipo] }}</label> }
                        </fieldset>
                      }
                    }
                    @case ('encerrar') {
                      <p class="cell-sub">O prazo de recurso acabou em {{ data(p.prazoRecurso!) }} sem recurso: encerre o processo para as penalidades valerem.</p>
                    }
                    @case ('aguardar') {
                      <p class="cell-sub">{{ espera(p) }}</p>
                    }
                  }
                } @else { <p class="cell-sub">Carregando…</p> }
              }
              @case ('medida') {
                <div class="dw-field"><label for="f-ps-mestab">Estabelecimento <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-ps-mestab" (change)="f.set({ ...f(), estabelecimentoId: $any($event.target).value })">
                    <option value="" [selected]="!f()['estabelecimentoId']">Escolha…</option>
                    @for (e of estabelecimentos(); track e.uuid) { <option [value]="e.uuid" [selected]="f()['estabelecimentoId'] === e.uuid">{{ e.nomeFantasia ?? e.razaoSocial }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-ps-mdesc">{{ g.medida === 'APREENSAO' ? 'Motivo da apreensão' : 'O que é interditado e por quê' }} <span class="req" aria-hidden="true">*</span></label><textarea id="f-ps-mdesc" rows="2" maxlength="1000" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)"></textarea></div>
                @if (g.medida === 'APREENSAO') {
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-ps-prod">Produto <span class="req" aria-hidden="true">*</span></label><input id="f-ps-prod" type="text" maxlength="200" [value]="f()['produto'] ?? ''" (input)="set('produto', $event)" /></div>
                    <div class="dw-field"><label for="f-ps-lote">Lote<span class="opt">opcional</span></label><input id="f-ps-lote" type="text" maxlength="60" [value]="f()['lote'] ?? ''" (input)="set('lote', $event)" /></div>
                    <div class="dw-field"><label for="f-ps-qtd">Quantidade <span class="req" aria-hidden="true">*</span></label><input id="f-ps-qtd" type="text" maxlength="60" [value]="f()['quantidade'] ?? ''" (input)="set('quantidade', $event)" /></div>
                  </div>
                  <div class="dw-field"><label for="f-ps-guarda">Local de guarda<span class="opt">opcional</span></label><input id="f-ps-guarda" type="text" maxlength="200" [value]="f()['localGuarda'] ?? ''" (input)="set('localGuarda', $event)" /></div>
                } @else {
                  <label class="chk"><input type="checkbox" [checked]="f()['total'] !== 'nao'" (change)="f.set({ ...f(), total: $any($event.target).checked ? 'sim' : 'nao' })" /> Interdição total</label>
                }
                <div class="dw-row">
                  <div class="dw-field"><label for="f-ps-ini">Início</label><input id="f-ps-ini" type="date" [value]="f()['inicio'] ?? ''" (input)="set('inicio', $event)" /></div>
                  @if (g.medida === 'INTERDICAO_CAUTELAR') { <div class="dw-field"><label for="f-ps-fim">Até<span class="opt">90 dias se vazio</span></label><input id="f-ps-fim" type="date" [value]="f()['fimPrevisto'] ?? ''" (input)="set('fimPrevisto', $event)" /></div> }
                </div>
              }
              @case ('encerrarMedida') {
                <dl class="ro"><div><dt>Medida</dt><dd>{{ g.medida.numero }} · {{ tiposMedida[g.medida.tipo] }} · {{ g.medida.estabelecimentoNome }}</dd></div></dl>
                <div class="dw-field"><label for="f-ps-emot">{{ g.medida.tipo === 'APREENSAO' ? 'Destinação do produto' : 'Motivo da desinterdição' }} <span class="req" aria-hidden="true">*</span></label><textarea id="f-ps-emot" rows="2" maxlength="1000" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)"></textarea></div>
                <div class="dw-field"><label for="f-ps-edata">Data</label><input id="f-ps-edata" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
              }
            }
          </div>
          @if (g.tipo !== 'processo' || (processo() && proximo(processo()!) !== 'aguardar')) {
            <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="fechar()">Fechar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : rotuloEnviar(g) }}</button></div>
          }
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .ps-cab { display: flex; justify-content: space-between; align-items: flex-end; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-2); }
    .ps-tabela { min-width: 0; width: 100%; }
    .ps-titulo { margin-top: var(--s-4); }
    .ps-grupo { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); margin: var(--s-2) 0; display: grid; gap: var(--s-2); }
    .ps-linha, .ps-lista { margin: 0; padding-left: var(--s-4); display: grid; gap: var(--s-1); }
    .ps-revogada { text-decoration: line-through; color: var(--ink-500); }
    .chk { display: flex; gap: var(--s-2); align-items: center; }
  `,
})
export class ProcessosSanitarios {
  private readonly service = inject(VigilanciaSanitariaService);

  readonly secao = input.required<SecaoProcessos>();
  readonly estabelecimentos = input<EstabelecimentoReguladoDto[]>([]);
  readonly podeInspecionar = input(false);
  readonly podeGerenciar = input(false);
  readonly podeJulgar = input(false);
  /** O estabelecimento a autuar, vindo do detalhe da tela: abre o auto já com ele. */
  readonly autuar = input<string | null>(null);
  readonly alterou = output<void>();

  protected readonly fases = FASES_PROCESSO;
  protected readonly listaFases = Object.keys(FASES_PROCESSO) as FaseProcessoSanitario[];
  protected readonly formas = FORMAS_CIENCIA;
  protected readonly listaFormas = Object.keys(FORMAS_CIENCIA) as FormaCiencia[];
  protected readonly gravidades = GRAVIDADES;
  protected readonly listaGravidades = Object.keys(GRAVIDADES) as GravidadeIrregularidade[];
  protected readonly penalidades = PENALIDADES;
  protected readonly listaPenalidades = Object.keys(PENALIDADES) as TipoPenalidadeSanitaria[];
  protected readonly tiposMedida = TIPOS_MEDIDA;
  protected readonly tiposTermo = TIPOS_TERMO;
  protected readonly atenuantesRot = ATENUANTES;
  protected readonly listaAtenuantes = Object.keys(ATENUANTES) as AtenuanteSanitaria[];
  protected readonly agravantesRot = AGRAVANTES;
  protected readonly listaAgravantes = Object.keys(AGRAVANTES) as AgravanteSanitaria[];
  protected readonly resultadosRecurso: Record<string, string> = { PROVIDO: 'provido', PARCIALMENTE_PROVIDO: 'parcialmente provido', IMPROVIDO: 'improvido' };

  protected readonly erro = signal<string | null>(null);
  protected readonly fase = signal<string>('');
  protected readonly situacao = signal<string>('VIGENTE');
  protected readonly processos = signal<ProcessoSanitarioDto[]>([]);
  protected readonly medidas = signal<MedidaSanitariaDto[]>([]);
  protected readonly processo = signal<ProcessoSanitarioDto | null>(null);
  protected readonly abertas = signal<IrregularidadeSanitariaDto[]>([]);
  protected readonly marcadas = signal<Set<string>>(new Set());
  protected readonly atenuantes = signal<Set<string>>(new Set());
  protected readonly agravantes = signal<Set<string>>(new Set());
  protected readonly mantidas = signal<Set<string>>(new Set());
  protected readonly novas = signal<PenalidadeNova[]>([]);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly hojeIso = computed(() => hoje());

  constructor() {
    effect(() => {
      this.secao();
      untracked(() => this.carregar());
    });
    effect(() => {
      const id = this.autuar();
      if (id) untracked(() => this.abrirLavrar(id));
    });
  }

  protected titulo(g: Gaveta): string {
    switch (g.tipo) {
      case 'lavrar': return 'Lavrar auto de infração';
      case 'processo': return this.processo() ? `Processo ${this.processo()!.numero}` : 'Processo';
      case 'medida': return g.medida === 'APREENSAO' ? 'Apreensão de produto' : 'Interdição cautelar';
      default: return g.medida.tipo === 'APREENSAO' ? 'Destinação do apreendido' : 'Desinterdição';
    }
  }

  protected rotuloEnviar(g: Gaveta): string {
    if (g.tipo !== 'processo') return g.tipo === 'lavrar' ? 'Lavrar' : 'Salvar';
    const p = this.processo();
    switch (p ? this.proximo(p) : '') {
      case 'ciencia': return 'Registrar ciência';
      case 'defesa': return 'Registrar defesa';
      case 'decisao': return 'Registrar decisão';
      case 'recurso': return 'Registrar recurso';
      case 'julgamento': return 'Julgar recurso';
      case 'encerrar': return 'Encerrar processo';
      default: return 'Salvar';
    }
  }

  /** A próxima ação possível para quem está logado, pela fase e pelos prazos. */
  protected proximo(p: ProcessoSanitarioDto): 'ciencia' | 'defesa' | 'decisao' | 'recurso' | 'julgamento' | 'encerrar' | 'aguardar' {
    const hoje = this.hojeIso();
    const registra = this.podeInspecionar() || this.podeGerenciar();
    switch (p.fase) {
      case 'AGUARDANDO_CIENCIA': return registra ? 'ciencia' : 'aguardar';
      case 'EM_DEFESA':
        if (p.prazoDefesa && p.prazoDefesa < hoje && this.podeJulgar()) return 'decisao';
        return this.podeGerenciar() ? 'defesa' : 'aguardar';
      case 'AGUARDANDO_DECISAO': return this.podeJulgar() ? 'decisao' : 'aguardar';
      case 'DECIDIDO':
        if (!p.cienciaDecisaoEm) return registra ? 'ciencia' : 'aguardar';
        if (p.prazoRecurso && p.prazoRecurso < hoje) return this.podeGerenciar() ? 'encerrar' : 'aguardar';
        return this.podeGerenciar() ? 'recurso' : 'aguardar';
      case 'EM_RECURSO': return this.podeJulgar() ? 'julgamento' : 'aguardar';
      default: return 'aguardar';
    }
  }

  protected espera(p: ProcessoSanitarioDto): string {
    if (p.fase === 'ENCERRADO') return 'Processo encerrado.';
    return 'A próxima etapa não está no seu acesso.';
  }

  protected prazo(p: ProcessoSanitarioDto): string | null {
    if (p.fase === 'EM_DEFESA' && p.prazoDefesa) return (p.prazoDefesa < this.hojeIso() ? 'revelia desde ' : 'defesa até ') + this.data(p.prazoDefesa);
    if (p.fase === 'DECIDIDO' && p.prazoRecurso) return (p.prazoRecurso < this.hojeIso() ? 'a encerrar desde ' : 'recurso até ') + this.data(p.prazoRecurso);
    if (p.fase === 'DECIDIDO') return 'ciência da decisão por registrar';
    return null;
  }

  protected abrir(g: Gaveta, campos: Record<string, string> = {}): void {
    this.erroGaveta.set(null);
    this.f.set(campos);
    this.gaveta.set(g);
  }

  protected fechar(): void {
    this.gaveta.set(null);
    this.processo.set(null);
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected alternar(s: ReturnType<typeof signal<Set<string>>>, v: string): void {
    const n = new Set(s());
    if (n.has(v)) n.delete(v); else n.add(v);
    s.set(n);
  }

  protected remover<T>(lista: T[], i: number): T[] {
    return lista.filter((_, j) => j !== i);
  }

  protected abrirLavrar(estabelecimentoId: string): void {
    this.marcadas.set(new Set());
    this.abertas.set([]);
    this.abrir({ tipo: 'lavrar' }, { data: hoje(), estabelecimentoId });
    if (estabelecimentoId) this.carregarAbertas(estabelecimentoId);
  }

  protected mudarEstabelecimento(id: string): void {
    this.f.set({ ...this.f(), estabelecimentoId: id });
    this.marcadas.set(new Set());
    this.carregarAbertas(id);
  }

  private carregarAbertas(id: string): void {
    this.abertas.set([]);
    if (!id) return;
    this.service.estabelecimento(id).pipe(catchError(() => of(null))).subscribe((d) => {
      const abertas = (d?.irregularidades ?? []).filter((r) => r.situacao === 'ABERTA');
      this.abertas.set(abertas);
      this.marcadas.set(new Set(abertas.map((r) => r.uuid)));
    });
  }

  protected abrirProcesso(id: string): void {
    this.processo.set(null);
    this.abrir({ tipo: 'processo', id });
    this.recarregarProcesso(id);
  }

  private recarregarProcesso(id: string): void {
    this.service.processo(id).subscribe({
      next: (p) => {
        this.processo.set(p);
        this.atenuantes.set(new Set());
        this.agravantes.set(new Set());
        this.mantidas.set(new Set());
        this.novas.set([]);
        const proximo = this.proximo(p);
        this.f.set({ data: hoje(), forma: 'PESSOAL', resultado: proximo === 'julgamento' ? 'IMPROVIDO' : 'PENALIDADE' });
      },
      error: (x: HttpErrorResponse) => this.erroGaveta.set(mensagem(x)),
    });
  }

  protected abrirMedida(medida: 'INTERDICAO_CAUTELAR' | 'APREENSAO'): void {
    this.abrir({ tipo: 'medida', medida }, { inicio: hoje(), total: 'sim' });
  }

  protected mudarPenalidade(i: number, campo: keyof PenalidadeNova, ev: Event): void {
    const v = (ev.target as HTMLInputElement).value;
    this.novas.set(this.novas().map((x, j) => (j === i ? { ...x, [campo]: v } : x)));
  }

  protected termosDoProcesso(p: ProcessoSanitarioDto): TermoSanitarioDto[] {
    return [...p.termos, ...p.medidas.flatMap((m) => m.termos)];
  }

  protected salvar(g: Gaveta): void {
    if (this.enviando()) return;
    const c = this.f();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    let req: Observable<unknown>;
    let falta: string | null = null;
    let reabrir: string | null = null;
    switch (g.tipo) {
      case 'lavrar': {
        const fiscais = (c['fiscais'] ?? '').split(/[,;\s]+/).map((x) => x.trim()).filter(Boolean);
        if (!t('estabelecimentoId')) falta = 'Escolha o estabelecimento.';
        else if (!this.marcadas().size) falta = 'Marque ao menos uma irregularidade aberta.';
        else if (!t('dispositivoLegal') || !t('descricao')) falta = 'Informe o dispositivo legal e a descrição.';
        else if (!fiscais.length) falta = 'Informe a matrícula de ao menos um fiscal.';
        req = this.service.lavrar({ estabelecimentoId: t('estabelecimentoId') ?? '', inspecaoId: null, irregularidades: [...this.marcadas()], dispositivoLegal: t('dispositivoLegal') ?? '',
          descricao: t('descricao') ?? '', gravidade: t('gravidade'), dataLavratura: t('data'), fiscais });
        break;
      }
      case 'processo': {
        const p = this.processo();
        if (!p) return;
        reabrir = p.uuid;
        switch (this.proximo(p)) {
          case 'ciencia':
            req = this.service.acao(p.uuid, 'ciencia', { forma: c['forma'], data: t('data') });
            break;
          case 'defesa':
          case 'recurso':
            if (!t('texto')) falta = 'Resuma o que foi apresentado.';
            req = this.service.acao(p.uuid, this.proximo(p) as 'defesa' | 'recurso', { texto: t('texto') ?? '', data: t('data') });
            break;
          case 'decisao': {
            const penal = c['resultado'] === 'PENALIDADE';
            if (!t('fundamentacao')) falta = 'Informe a fundamentação.';
            else if (penal && !this.novas().length) falta = 'Informe ao menos uma penalidade.';
            req = this.service.acao(p.uuid, 'decisao', {
              resultado: c['resultado'], fundamentacao: t('fundamentacao') ?? '', data: t('data'),
              atenuantes: penal ? [...this.atenuantes()] : [], agravantes: penal ? [...this.agravantes()] : [],
              penalidades: penal ? this.novas().map((x) => ({ tipo: x.tipo, valorMulta: x.tipo === 'MULTA' && x.valorMulta ? Number(x.valorMulta) : null,
                suspensaoAte: x.tipo === 'SUSPENSAO_LICENCA' && x.suspensaoAte ? x.suspensaoAte : null, descricao: x.descricao.trim() || null })) : [],
            });
            break;
          }
          case 'julgamento':
            if (!t('fundamentacao')) falta = 'Informe a fundamentação.';
            req = this.service.acao(p.uuid, 'julgamento-recurso', { resultado: c['resultado'], fundamentacao: t('fundamentacao') ?? '', data: t('data'),
              penalidadesMantidas: c['resultado'] === 'PARCIALMENTE_PROVIDO' ? [...this.mantidas()] : [] });
            break;
          case 'encerrar':
            req = this.service.acao(p.uuid, 'encerramento');
            break;
          default:
            return;
        }
        break;
      }
      case 'medida': {
        const apreensao = g.medida === 'APREENSAO';
        if (!t('estabelecimentoId') || !t('descricao')) falta = 'Escolha o estabelecimento e descreva a medida.';
        else if (apreensao && (!t('produto') || !t('quantidade'))) falta = 'Informe o produto e a quantidade.';
        req = this.service.registrarMedida({ estabelecimentoId: t('estabelecimentoId') ?? '', tipo: g.medida as TipoMedidaSanitaria, total: apreensao ? null : c['total'] !== 'nao',
          descricao: t('descricao') ?? '', produto: t('produto'), lote: t('lote'), quantidade: t('quantidade'), localGuarda: t('localGuarda'), inicio: t('inicio'),
          fimPrevisto: apreensao ? null : t('fimPrevisto') });
        break;
      }
      default:
        if (!t('motivo')) falta = 'Informe o motivo.';
        req = this.service.encerrarMedida(g.medida.uuid, t('motivo') ?? '', t('data'));
    }
    if (falta) {
      this.erroGaveta.set(falta);
      return;
    }
    this.enviando.set(true);
    req.subscribe({
      next: (r) => {
        this.enviando.set(false);
        this.erroGaveta.set(null);
        this.carregar();
        this.alterou.emit();
        if (reabrir) this.recarregarProcesso(reabrir);
        else if (g.tipo === 'lavrar') this.abrirProcesso((r as ProcessoSanitarioDto).uuid);
        else this.fechar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected baixar(t: TermoSanitarioDto): void {
    if (!t.versaoId) return;
    this.service.abrirDocumento(t.versaoId).subscribe({
      next: (r) => salvarArquivo(r, `${t.tipo.toLowerCase()}-${t.numero.replace('/', '-')}.pdf`),
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  protected nomes(p: ProcessoSanitarioDto): string {
    return p.fiscais.map((x) => `${x.nome} (${x.matricula})`).join(', ');
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  carregar(): void {
    this.erro.set(null);
    const falhou = (x: HttpErrorResponse) => this.erro.set(mensagem(x));
    if (this.secao() === 'processos') {
      this.service.processos({ fase: (this.fase() || undefined) as FaseProcessoSanitario | undefined }).subscribe({ next: (l) => this.processos.set(l), error: falhou });
    } else {
      this.service.medidas({ situacao: (this.situacao() || undefined) as 'VIGENTE' | 'ENCERRADA' | undefined }).subscribe({ next: (l) => this.medidas.set(l), error: falhou });
    }
  }
}

function hoje(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function salvarArquivo(r: HttpResponse<Blob>, nome: string): void {
  const url = URL.createObjectURL(r.body ?? new Blob());
  const a = document.createElement('a');
  a.href = url;
  a.download = nome;
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 60_000);
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
