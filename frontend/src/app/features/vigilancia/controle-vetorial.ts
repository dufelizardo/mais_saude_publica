import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../core/models/auth';
import {
  AcaoControleVetorialDto,
  BloqueioPendenteDto,
  CLASSIFICACOES_IIP,
  ESPECIES_VETOR,
  EspecieVetor,
  FASES_FOCO,
  FaseFoco,
  FocoVetorialDto,
  ImovelVetorialDto,
  IndicesVetoriaisDto,
  RESULTADOS_LABORATORIO,
  RESULTADOS_VISTORIA,
  ResultadoLaboratorioFoco,
  ResultadoVistoriaVetorial,
  SITUACOES_ACAO_VETORIAL,
  SITUACOES_FOCO,
  TIPOS_ACAO_VETORIAL,
  TIPOS_DEPOSITO,
  TIPOS_VISTORIA,
  TipoAcaoControleVetorial,
  TipoDeposito,
  TipoVistoriaVetorial,
  VistoriaVetorialDto,
} from '../../core/models/controle-vetorial';
import { ErrorResponseDto } from '../../core/models/profissional';
import { AreaNoDto } from '../../core/models/territorio';
import { AuthService } from '../../core/services/auth';
import { ControleVetorialService } from '../../core/services/controle-vetorial';
import { TerritorioService } from '../../core/services/territorio';
import { Drawer } from '../../shared/drawer/drawer';
import { Mapa, PinoMapa } from '../../shared/mapa/mapa';

type Aba = 'vistorias' | 'focos' | 'indices' | 'acoes';
type Gaveta =
  | { tipo: 'vistoria'; imovel: ImovelVetorialDto | null }
  | { tipo: 'laboratorio'; foco: FocoVetorialDto }
  | { tipo: 'encerrarFoco'; foco: FocoVetorialDto; situacao: 'TRATADO' | 'ELIMINADO' }
  | { tipo: 'acao'; bloqueio: BloqueioPendenteDto | null }
  | { tipo: 'realizar' | 'cancelar'; acao: AcaoControleVetorialDto };
type FocoNovo = { tipoDeposito: TipoDeposito; fase: FaseFoco; especieSuspeita: EspecieVetor; amostraColetada: boolean };

/**
 * Zoonoses e vetores, fatia V4a da Vigilância (ADR-0162): a vistoria do agente de endemias com os depósitos do PNCD e os
 * focos, as pendências de revisita, os focos no mapa com o laboratório e o tratamento, os índices do ciclo (IIP e Breteau)
 * e as ações de controle, com os casos de arbovirose ainda sem bloqueio.
 */
@Component({
  selector: 'app-controle-vetorial',
  imports: [Drawer, Mapa],
  template: `
    <header class="page-head">
      <div>
        <h1>Zoonoses e vetores</h1>
        <p>Vistorias do agente de endemias, focos, índices do ciclo e ações de controle do Aedes</p>
      </div>
      <div class="page-actions">
        <div class="dw-field" style="margin: 0;"><label for="f-cv-area">Área</label>
          <select id="f-cv-area" (change)="mudarArea($any($event.target).value)">
            <option value="" [selected]="!areaId()">Todas</option>
            @for (a of areas(); track a.uuid) { <option [value]="a.uuid" [selected]="areaId() === a.uuid">{{ a.nome }}</option> }
          </select></div>
        @if (pode('ZOONOSES.VISTORIAR')) { <button type="button" class="btn" (click)="abrirVistoria(null)" [disabled]="!areas().length">Registrar vistoria</button> }
      </div>
    </header>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    <div class="tabs" role="tablist" aria-label="Seções do controle de vetores">
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'vistorias'" (click)="aba.set('vistorias')">Vistorias @if (pendencias().length) { <span class="cnt">{{ pendencias().length }}</span> }</button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'focos'" (click)="aba.set('focos')">Focos <span class="cnt">{{ focosAtivos().length }}</span></button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'indices'" (click)="aba.set('indices')">Índices do ciclo</button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'acoes'" (click)="aba.set('acoes')">Ações de controle @if (bloqueios().length) { <span class="cnt">{{ bloqueios().length }}</span> }</button>
    </div>

    @switch (aba()) {
      @case ('vistorias') {
        @if (pendencias().length) {
          <section class="card cv-bloco" aria-label="Pendências de revisita">
            <h3 class="mo-grupo">Pendências de revisita do ciclo</h3>
            <ul class="eq-prof">
              @for (p of pendencias(); track p.uuid) {
                <li class="cv-linha">
                  <span><span class="cell-main">{{ p.endereco || 'Imóvel sem endereço' }}</span><span class="cell-sub">{{ p.areaNome }} · microárea {{ p.microareaCodigo }} · {{ resultados[p.ultimoResultado!].rotulo.toLowerCase() }} em {{ data(p.ultimaVistoria!) }}</span></span>
                  @if (pode('ZOONOSES.VISTORIAR')) { <button type="button" class="link-btn" (click)="abrirVistoria(p)" [attr.aria-label]="'Revisitar ' + p.endereco">Revisitar</button> }
                </li>
              }
            </ul>
          </section>
        }
        <div class="cv-cab"><span class="cell-sub">Ciclo {{ ciclo() }}/{{ ano() }} · {{ vistorias().length }} vistoria(s)</span></div>
        <div class="tbl-wrap card">
          <table class="cv-tabela" aria-label="Vistorias do ciclo">
            <thead><tr><th scope="col">Imóvel</th><th scope="col">Data</th><th scope="col">Agente</th><th scope="col">Resultado</th><th scope="col">Depósitos</th><th scope="col">Focos</th></tr></thead>
            <tbody>
              @for (v of vistorias(); track v.uuid) {
                <tr>
                  <td><span class="cell-main">{{ v.endereco || 'Imóvel sem endereço' }}</span><span class="cell-sub">{{ v.areaNome }} · microárea {{ v.microareaCodigo }} · {{ tiposVistoria[v.tipo] }}</span></td>
                  <td class="mono">{{ data(v.data) }}</td>
                  <td>{{ v.agenteNome }}</td>
                  <td><span [class]="'tag tag--' + resultados[v.resultado].classe">{{ resultados[v.resultado].rotulo }}</span></td>
                  <td>{{ v.depositosInspecionados }}@if (v.depositosTratados || v.depositosEliminados) { <span class="cell-sub">{{ v.depositosTratados }} tratado(s) · {{ v.depositosEliminados }} eliminado(s)</span> }</td>
                  <td>@if (v.focos.length) { <span class="tag tag--alert">{{ v.focos.length }} foco(s)</span> } @else { <span class="cell-sub">—</span> }</td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma vistoria neste ciclo.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('focos') {
        @if (pinos().length) { <app-mapa [pinos]="pinos()" [altura]="300" rotulo="Focos ativos" /> }
        <div class="tbl-wrap card cv-bloco">
          <table class="cv-tabela" aria-label="Focos">
            <thead><tr><th scope="col">Imóvel</th><th scope="col">Depósito</th><th scope="col">Espécie</th><th scope="col">Laboratório</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (f of focos(); track f.uuid) {
                <tr>
                  <td><span class="cell-main">{{ f.endereco || 'Imóvel sem endereço' }}</span><span class="cell-sub">{{ f.areaNome }} · microárea {{ f.microareaCodigo }} · {{ data(f.data) }}</span></td>
                  <td>{{ depositos[f.tipoDeposito] }}<span class="cell-sub">{{ fases[f.fase] }}</span></td>
                  <td>{{ especies[f.especieSuspeita] }}</td>
                  <td>@if (f.resultadoLaboratorio) { <span [class]="'tag tag--' + (f.contaNosIndices ? 'warn' : 'muted')">{{ laboratorio[f.resultadoLaboratorio] }}</span> } @else if (f.amostraColetada) { <span class="cell-sub">amostra enviada</span> } @else { <span class="cell-sub">sem amostra</span> }</td>
                  <td><span [class]="'tag tag--' + situacoesFoco[f.situacao].classe">{{ situacoesFoco[f.situacao].rotulo }}</span>@if (f.observacao) { <span class="cell-sub">{{ f.observacao }}</span> }</td>
                  <td class="row-actions">
                    @if (f.amostraColetada && podeAlguma('ZOONOSES.VISTORIAR', 'ZOONOSES.GERENCIAR')) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'laboratorio', foco: f })" [attr.aria-label]="'Resultado do laboratório: ' + f.endereco">Resultado</button> }
                    @if (f.situacao === 'ATIVO' && pode('ZOONOSES.VISTORIAR')) {
                      <button type="button" class="link-btn" (click)="abrir({ tipo: 'encerrarFoco', foco: f, situacao: 'TRATADO' })" [attr.aria-label]="'Tratar foco: ' + f.endereco">Tratado</button>
                      <button type="button" class="link-btn" (click)="abrir({ tipo: 'encerrarFoco', foco: f, situacao: 'ELIMINADO' })" [attr.aria-label]="'Eliminar foco: ' + f.endereco">Eliminado</button>
                    }
                  </td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhum foco registrado.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('indices') {
        <div class="cv-cab">
          <div class="dw-row" style="margin: 0;">
            <div class="dw-field"><label for="f-cv-ano">Ano</label><input id="f-cv-ano" type="number" [value]="ano()" (change)="mudarCiclo(+$any($event.target).value, ciclo())" /></div>
            <div class="dw-field"><label for="f-cv-ciclo">Ciclo</label>
              <select id="f-cv-ciclo" (change)="mudarCiclo(ano(), +$any($event.target).value)">
                @for (c of [1, 2, 3, 4, 5, 6]; track c) { <option [value]="c" [selected]="ciclo() === c">{{ c }} · {{ bimestre(c) }}</option> }
              </select></div>
          </div>
        </div>
        @if (indices(); as ix) {
          <div class="cv-numeros" aria-label="Índices do município">
            <div class="cv-num"><b>{{ pct(ix.municipio.iip) }}</b><span>IIP do município@if (ix.municipio.classificacao) { <span [class]="'tag tag--' + classificacoes[ix.municipio.classificacao].classe">{{ classificacoes[ix.municipio.classificacao].rotulo }}</span> }</span></div>
            <div class="cv-num"><b>{{ num(ix.municipio.ib) }}</b><span>índice de Breteau</span></div>
            <div class="cv-num"><b>{{ ix.municipio.imoveisInspecionados }}</b><span>imóveis inspecionados · {{ ix.municipio.imoveisPositivos }} positivo(s)</span></div>
            <div class="cv-num"><b>{{ pct(ix.municipio.percentualPendencia) }}</b><span>pendência (fechado ou recusado)</span></div>
          </div>
          <div class="tbl-wrap card">
            <table class="cv-tabela" aria-label="Índices por área">
              <thead><tr><th scope="col">Área</th><th scope="col">Visitados</th><th scope="col">Inspecionados</th><th scope="col">Pendência</th><th scope="col">Positivos</th><th scope="col">IIP</th><th scope="col">Breteau</th></tr></thead>
              <tbody>
                @for (l of ix.areas; track l.areaId) {
                  <tr>
                    <td class="cell-main">{{ l.areaNome }}</td>
                    <td>{{ l.imoveisVisitados }}</td>
                    <td>{{ l.imoveisInspecionados }}</td>
                    <td>{{ l.imoveisPendentes }}@if (l.percentualPendencia != null) { <span class="cell-sub">{{ pct(l.percentualPendencia) }}</span> }</td>
                    <td>{{ l.imoveisPositivos }}<span class="cell-sub">{{ l.depositosPositivos }} depósito(s)</span></td>
                    <td>@if (l.iip != null) { {{ pct(l.iip) }} <span [class]="'tag tag--' + classificacoes[l.classificacao!].classe">{{ classificacoes[l.classificacao!].rotulo }}</span> } @else { — }</td>
                    <td>{{ num(l.ib) }}</td>
                  </tr>
                } @empty { <tr><td colspan="7" class="cell-sub">Nenhuma vistoria no ciclo {{ ix.ciclo }}/{{ ix.ano }}.</td></tr> }
              </tbody>
            </table>
          </div>
          <p class="cell-sub">IIP: imóveis com foco ÷ imóveis inspecionados × 100. Breteau: depósitos positivos ÷ imóveis inspecionados × 100. Classificação do Ministério da Saúde: abaixo de 1%, satisfatório; de 1% a 3,9%, alerta; 4% ou mais, risco. Foco negativo no laboratório não conta.</p>
        }
      }
      @case ('acoes') {
        @if (bloqueios().length) {
          <section class="card cv-bloco" aria-label="Casos sem bloqueio">
            <h3 class="mo-grupo">Casos de arbovirose sem bloqueio (últimos 30 dias)</h3>
            <ul class="eq-prof">
              @for (b of bloqueios(); track b.notificacaoId) {
                <li class="cv-linha">
                  <span><span class="cell-main">{{ b.agravoNome }} · notificado em {{ data(b.dataNotificacao) }}</span><span class="cell-sub">{{ b.unidadeNome }}@if (b.microareaCodigo) { · {{ b.areaNome }}, microárea {{ b.microareaCodigo }} }@if (b.localProvavel) { · {{ b.localProvavel }} }</span></span>
                  @if (pode('ZOONOSES.GERENCIAR')) { <button type="button" class="link-btn" (click)="abrirAcao(b)" [attr.aria-label]="'Planejar bloqueio: ' + b.agravoNome + ' de ' + data(b.dataNotificacao)">Planejar bloqueio</button> }
                </li>
              }
            </ul>
          </section>
        }
        <div class="cv-cab">
          <span class="cell-sub">{{ acoes().length }} ação(ões)</span>
          @if (pode('ZOONOSES.GERENCIAR')) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirAcao(null)" [disabled]="!areas().length">Planejar ação</button> }
        </div>
        <div class="tbl-wrap card">
          <table class="cv-tabela" aria-label="Ações de controle">
            <thead><tr><th scope="col">Ação</th><th scope="col">Data</th><th scope="col">Situação</th><th scope="col">Resultado</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (a of acoes(); track a.uuid) {
                <tr>
                  <td><span class="cell-main">{{ tiposAcao[a.tipo] }}@if (a.agravoNome) { · caso de {{ a.agravoNome }} }</span><span class="cell-sub">{{ a.microareas.join(', ') }} · {{ a.responsavel }}@if (a.descricao) { · {{ a.descricao }} }</span></td>
                  <td class="mono">{{ data(a.dataRealizada ?? a.dataPrevista) }}<span class="cell-sub">{{ a.dataRealizada ? 'realizada' : 'prevista' }}</span></td>
                  <td><span [class]="'tag tag--' + situacoesAcao[a.situacao].classe">{{ situacoesAcao[a.situacao].rotulo }}</span>@if (a.motivoCancelamento) { <span class="cell-sub">{{ a.motivoCancelamento }}</span> }</td>
                  <td class="cell-sub">@if (a.imoveisTrabalhados != null) { {{ a.imoveisTrabalhados }} imóvel(is) · }{{ a.vistorias }} vistoria(s)@if (a.resultado) { · {{ a.resultado }} }</td>
                  <td class="row-actions">
                    @if (a.situacao === 'PLANEJADA' && pode('ZOONOSES.GERENCIAR')) {
                      <button type="button" class="link-btn" (click)="abrir({ tipo: 'realizar', acao: a })" [attr.aria-label]="'Registrar realização: ' + tiposAcao[a.tipo]">Realizada</button>
                      <button type="button" class="link-btn" (click)="abrir({ tipo: 'cancelar', acao: a })" [attr.aria-label]="'Cancelar: ' + tiposAcao[a.tipo]">Cancelar</button>
                    }
                  </td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma ação de controle.</td></tr> }
            </tbody>
          </table>
        </div>
      }
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" [larga]="g.tipo === 'vistoria'" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @switch (g.tipo) {
              @case ('vistoria') {
                @if (g.imovel) {
                  <dl class="ro"><div><dt>Imóvel</dt><dd>{{ g.imovel.endereco || 'Imóvel sem endereço' }} · {{ g.imovel.areaNome }}, microárea {{ g.imovel.microareaCodigo }}</dd></div></dl>
                } @else {
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-cv-v-area">Área <span class="req" aria-hidden="true">*</span></label>
                      <select id="f-cv-v-area" (change)="carregarImoveis($any($event.target).value)">
                        <option value="" [selected]="!f()['areaId']">Escolha…</option>
                        @for (a of areas(); track a.uuid) { <option [value]="a.uuid" [selected]="f()['areaId'] === a.uuid">{{ a.nome }}</option> }
                      </select></div>
                    <div class="dw-field"><label for="f-cv-v-busca">Buscar endereço</label><input id="f-cv-v-busca" type="text" [value]="f()['busca'] ?? ''" (input)="set('busca', $event)" placeholder="Rua, número ou bairro" /></div>
                  </div>
                  <div class="dw-field"><label for="f-cv-v-imovel">Imóvel <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-cv-v-imovel" (change)="f.set({ ...f(), imovelId: $any($event.target).value })">
                      <option value="" [selected]="!f()['imovelId']">Escolha…</option>
                      @for (i of imoveisFiltrados(); track i.uuid) { <option [value]="i.uuid" [selected]="f()['imovelId'] === i.uuid">{{ i.endereco || 'Imóvel sem endereço' }} · microárea {{ i.microareaCodigo }}@if (i.pendente) { (pendente) }</option> }
                    </select></div>
                }
                <div class="dw-row">
                  <div class="dw-field"><label for="f-cv-v-data">Data</label><input id="f-cv-v-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                  <div class="dw-field"><label for="f-cv-v-agente">Matrícula do agente <span class="req" aria-hidden="true">*</span></label><input id="f-cv-v-agente" type="text" [value]="f()['agente'] ?? ''" (input)="set('agente', $event)" /></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-cv-v-tipo">Tipo de visita</label>
                    <select id="f-cv-v-tipo" (change)="f.set({ ...f(), tipo: $any($event.target).value })">
                      @for (t of listaTiposVistoria; track t) { <option [value]="t" [selected]="f()['tipo'] === t">{{ tiposVistoria[t] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-cv-v-resultado">Resultado</label>
                    <select id="f-cv-v-resultado" (change)="f.set({ ...f(), resultado: $any($event.target).value })">
                      @for (r of listaResultados; track r) { <option [value]="r" [selected]="f()['resultado'] === r">{{ resultados[r].rotulo }}</option> }
                    </select></div>
                </div>
                @if (acoesAbertas().length) {
                  <div class="dw-field"><label for="f-cv-v-acao">Ação de controle<span class="opt">opcional</span></label>
                    <select id="f-cv-v-acao" (change)="f.set({ ...f(), acaoId: $any($event.target).value })">
                      <option value="" [selected]="!f()['acaoId']">Nenhuma</option>
                      @for (a of acoesAbertas(); track a.uuid) { <option [value]="a.uuid" [selected]="f()['acaoId'] === a.uuid">{{ tiposAcao[a.tipo] }} · {{ data(a.dataPrevista) }}</option> }
                    </select></div>
                }
                @if (f()['resultado'] === 'INSPECIONADA') {
                  <fieldset class="dw-field"><legend>Depósitos inspecionados por tipo</legend>
                    <div class="cv-depositos">
                      @for (t of listaDepositos; track t) {
                        <label class="cv-deposito" [for]="'f-cv-dep-' + t"><span>{{ depositos[t] }}</span><input [id]="'f-cv-dep-' + t" type="number" min="0" [value]="f()['dep' + t] ?? ''" (input)="set('dep' + t, $event)" /></label>
                      }
                    </div>
                  </fieldset>
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-cv-v-eliminados">Depósitos eliminados</label><input id="f-cv-v-eliminados" type="number" min="0" [value]="f()['eliminados'] ?? ''" (input)="set('eliminados', $event)" /></div>
                    <div class="dw-field"><label for="f-cv-v-tratados">Depósitos tratados</label><input id="f-cv-v-tratados" type="number" min="0" [value]="f()['tratados'] ?? ''" (input)="set('tratados', $event)" /></div>
                  </div>
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-cv-v-larvicida">Larvicida<span class="opt">opcional</span></label><input id="f-cv-v-larvicida" type="text" maxlength="100" [value]="f()['larvicida'] ?? ''" (input)="set('larvicida', $event)" /></div>
                    <div class="dw-field"><label for="f-cv-v-gramas">Quantidade (g)</label><input id="f-cv-v-gramas" type="number" min="0" step="0.1" [value]="f()['gramas'] ?? ''" (input)="set('gramas', $event)" /></div>
                  </div>
                  <fieldset class="dw-field"><legend>Focos (depósitos com larva ou pupa)</legend>
                    @for (fo of focosNovos(); track $index; let i = $index) {
                      <div class="cv-foco" [attr.aria-label]="'Foco ' + (i + 1)">
                        <select [attr.aria-label]="'Depósito do foco ' + (i + 1)" (change)="mudarFoco(i, 'tipoDeposito', $any($event.target).value)">
                          @for (t of listaDepositos; track t) { <option [value]="t" [selected]="fo.tipoDeposito === t">{{ t }}</option> }
                        </select>
                        <select [attr.aria-label]="'Fase do foco ' + (i + 1)" (change)="mudarFoco(i, 'fase', $any($event.target).value)">
                          @for (fa of listaFases; track fa) { <option [value]="fa" [selected]="fo.fase === fa">{{ fases[fa] }}</option> }
                        </select>
                        <select [attr.aria-label]="'Espécie do foco ' + (i + 1)" (change)="mudarFoco(i, 'especieSuspeita', $any($event.target).value)">
                          @for (e of listaEspecies; track e) { <option [value]="e" [selected]="fo.especieSuspeita === e">{{ especies[e] }}</option> }
                        </select>
                        <label class="cv-check"><input type="checkbox" [checked]="fo.amostraColetada" (change)="mudarFoco(i, 'amostraColetada', $any($event.target).checked)" /> amostra</label>
                        <button type="button" class="link-btn" (click)="removerFoco(i)" [attr.aria-label]="'Remover foco ' + (i + 1)">Remover</button>
                      </div>
                    }
                    <button type="button" class="btn btn--ghost btn--sm" (click)="adicionarFoco()">Adicionar foco</button>
                  </fieldset>
                } @else {
                  <div class="box box--info"><div>Imóvel fechado ou recusado fica como pendência de revisita no ciclo.</div></div>
                }
                <div class="dw-field"><label for="f-cv-v-obs">Observação<span class="opt">opcional</span></label><input id="f-cv-v-obs" type="text" maxlength="1000" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" /></div>
              }
              @case ('laboratorio') {
                <dl class="ro"><div><dt>Foco</dt><dd>{{ g.foco.endereco }} · {{ depositos[g.foco.tipoDeposito] }}</dd></div></dl>
                <div class="dw-field"><label for="f-cv-lab">Resultado <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-cv-lab" (change)="f.set({ ...f(), resultado: $any($event.target).value })">
                    <option value="" [selected]="!f()['resultado']">Escolha…</option>
                    @for (r of listaLaboratorio; track r) { <option [value]="r" [selected]="f()['resultado'] === r">{{ laboratorio[r] }}</option> }
                  </select>
                  <span class="cell-sub">Negativo deixa de contar nos índices.</span></div>
              }
              @case ('encerrarFoco') {
                <dl class="ro"><div><dt>Foco</dt><dd>{{ g.foco.endereco }} · {{ depositos[g.foco.tipoDeposito] }}</dd></div></dl>
                <div class="dw-field"><label for="f-cv-foco-obs">Observação<span class="opt">opcional</span></label><input id="f-cv-foco-obs" type="text" maxlength="500" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" /></div>
              }
              @case ('acao') {
                @if (g.bloqueio; as b) { <dl class="ro"><div><dt>Caso</dt><dd>{{ b.agravoNome }} · notificado em {{ data(b.dataNotificacao) }}@if (b.localProvavel) { · {{ b.localProvavel }} }</dd></div></dl> }
                <div class="dw-row">
                  <div class="dw-field"><label for="f-cv-a-tipo">Tipo</label>
                    <select id="f-cv-a-tipo" (change)="f.set({ ...f(), tipo: $any($event.target).value })">
                      @for (t of listaTiposAcao; track t) { <option [value]="t" [selected]="f()['tipo'] === t">{{ tiposAcao[t] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-cv-a-data">Data prevista <span class="req" aria-hidden="true">*</span></label><input id="f-cv-a-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-cv-a-area">Área <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-cv-a-area" (change)="mudarAreaDaAcao($any($event.target).value)">
                    <option value="" [selected]="!f()['areaId']">Escolha…</option>
                    @for (a of areas(); track a.uuid) { <option [value]="a.uuid" [selected]="f()['areaId'] === a.uuid">{{ a.nome }}</option> }
                  </select></div>
                @if (microareasDaArea().length) {
                  <fieldset class="dw-field"><legend>Microáreas <span class="req" aria-hidden="true">*</span></legend>
                    @for (m of microareasDaArea(); track m.uuid) {
                      <label class="cv-check"><input type="checkbox" [checked]="microareasMarcadas().has(m.uuid)" (change)="alternarMicroarea(m.uuid)" /> {{ m.codigo }}@if (m.nome) { · {{ m.nome }} }</label>
                    }
                  </fieldset>
                }
                <div class="dw-field"><label for="f-cv-a-resp">Responsável <span class="req" aria-hidden="true">*</span></label><input id="f-cv-a-resp" type="text" maxlength="200" [value]="f()['responsavel'] ?? ''" (input)="set('responsavel', $event)" placeholder="Ex.: Equipe de endemias da área" /></div>
                <div class="dw-field"><label for="f-cv-a-desc">Descrição<span class="opt">opcional</span></label><input id="f-cv-a-desc" type="text" maxlength="1000" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)" placeholder="Ex.: raio de 150 m do caso" /></div>
              }
              @case ('realizar') {
                <div class="dw-row">
                  <div class="dw-field"><label for="f-cv-r-data">Data</label><input id="f-cv-r-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                  <div class="dw-field"><label for="f-cv-r-imoveis">Imóveis trabalhados</label><input id="f-cv-r-imoveis" type="number" min="0" [value]="f()['imoveis'] ?? ''" (input)="set('imoveis', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-cv-r-resultado">Resultado<span class="opt">opcional</span></label><input id="f-cv-r-resultado" type="text" maxlength="1000" [value]="f()['resultado'] ?? ''" (input)="set('resultado', $event)" /></div>
              }
              @case ('cancelar') {
                <div class="dw-field"><label for="f-cv-c-motivo">Motivo <span class="req" aria-hidden="true">*</span></label><input id="f-cv-c-motivo" type="text" maxlength="500" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)" /></div>
              }
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar' }}</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .cv-bloco { padding: var(--s-4); margin-bottom: var(--s-4); }
    .cv-cab { display: flex; justify-content: space-between; align-items: flex-end; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-2); }
    .cv-tabela { min-width: 0; width: 100%; }
    .cv-linha { display: flex; justify-content: space-between; align-items: center; gap: var(--s-3); }
    .cv-numeros { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: var(--s-2); margin-bottom: var(--s-3); }
    .cv-num { border: 1px solid var(--ink-200); border-radius: 10px; padding: var(--s-2) var(--s-3); display: grid; gap: 2px; background: var(--white); }
    .cv-num b { font-size: 1.05rem; }
    .cv-num span { font-size: 0.75rem; color: var(--ink-500); }
    .cv-depositos { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: var(--s-2); }
    .cv-deposito { display: grid; gap: 4px; font-weight: 400; font-size: 0.8125rem; }
    .cv-foco { display: flex; gap: var(--s-2); align-items: center; flex-wrap: wrap; margin-bottom: var(--s-2); }
    .cv-foco select { width: auto; }
    .cv-check { display: flex; gap: var(--s-2); align-items: center; margin: var(--s-1) 0; font-weight: 400; }
    .dw-field .cv-check input { width: auto; min-height: 0; padding: 0; }
  `,
})
export class ControleVetorial {
  private readonly service = inject(ControleVetorialService);
  private readonly territorio = inject(TerritorioService);
  private readonly authService = inject(AuthService);

  protected readonly tiposVistoria = TIPOS_VISTORIA;
  protected readonly listaTiposVistoria = Object.keys(TIPOS_VISTORIA) as TipoVistoriaVetorial[];
  protected readonly resultados = RESULTADOS_VISTORIA;
  protected readonly listaResultados = Object.keys(RESULTADOS_VISTORIA) as ResultadoVistoriaVetorial[];
  protected readonly depositos = TIPOS_DEPOSITO;
  protected readonly listaDepositos = Object.keys(TIPOS_DEPOSITO) as TipoDeposito[];
  protected readonly fases = FASES_FOCO;
  protected readonly listaFases = Object.keys(FASES_FOCO) as FaseFoco[];
  protected readonly especies = ESPECIES_VETOR;
  protected readonly listaEspecies = Object.keys(ESPECIES_VETOR) as EspecieVetor[];
  protected readonly laboratorio = RESULTADOS_LABORATORIO;
  protected readonly listaLaboratorio = Object.keys(RESULTADOS_LABORATORIO) as ResultadoLaboratorioFoco[];
  protected readonly situacoesFoco = SITUACOES_FOCO;
  protected readonly tiposAcao = TIPOS_ACAO_VETORIAL;
  protected readonly listaTiposAcao = Object.keys(TIPOS_ACAO_VETORIAL) as TipoAcaoControleVetorial[];
  protected readonly situacoesAcao = SITUACOES_ACAO_VETORIAL;
  protected readonly classificacoes = CLASSIFICACOES_IIP;

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('vistorias');
  protected readonly erro = signal<string | null>(null);
  protected readonly areas = signal<AreaNoDto[]>([]);
  protected readonly areaId = signal<string>('');
  protected readonly ano = signal(new Date().getFullYear());
  protected readonly ciclo = signal(Math.floor(new Date().getMonth() / 2) + 1);
  protected readonly vistorias = signal<VistoriaVetorialDto[]>([]);
  protected readonly pendencias = signal<ImovelVetorialDto[]>([]);
  protected readonly focos = signal<FocoVetorialDto[]>([]);
  protected readonly indices = signal<IndicesVetoriaisDto | null>(null);
  protected readonly acoes = signal<AcaoControleVetorialDto[]>([]);
  protected readonly bloqueios = signal<BloqueioPendenteDto[]>([]);
  protected readonly imoveis = signal<ImovelVetorialDto[]>([]);
  protected readonly focosNovos = signal<FocoNovo[]>([]);
  protected readonly microareasMarcadas = signal<Set<string>>(new Set());
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});

  protected readonly focosAtivos = computed(() => this.focos().filter((x) => x.situacao === 'ATIVO'));
  protected readonly acoesAbertas = computed(() => this.acoes().filter((a) => a.situacao === 'PLANEJADA'));
  protected readonly pinos = computed<PinoMapa[]>(() => this.focosAtivos().flatMap((x) => x.latitude != null && x.longitude != null
    ? [{ id: x.uuid, lat: x.latitude, lng: x.longitude, sigla: x.tipoDeposito, classe: 'unit-pin t-closed', descricao: `Foco ${x.tipoDeposito}: ${x.endereco}` }] : []));
  protected readonly imoveisFiltrados = computed(() => {
    const busca = (this.f()['busca'] ?? '').trim().toLowerCase();
    return busca ? this.imoveis().filter((i) => i.endereco.toLowerCase().includes(busca)) : this.imoveis();
  });
  protected readonly microareasDaArea = computed(() => this.areas().find((a) => a.uuid === this.f()['areaId'])?.microareas ?? []);

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      this.carregar();
    });
    this.territorio.arvore().pipe(catchError(() => of(null))).subscribe((t) => {
      const lista = (t?.segmentos ?? []).flatMap((s) => s.territorios.filter((x) => x.ativo).flatMap((x) => x.areas));
      this.areas.set(lista.sort((a, b) => a.nome.localeCompare(b.nome)));
    });
  }

  protected pode(permissao: string): boolean {
    const a = this.acesso();
    return !a.restrito || a.permissoes.has(permissao);
  }

  protected podeAlguma(...permissoes: string[]): boolean {
    return permissoes.some((p) => this.pode(p));
  }

  protected mudarArea(id: string): void {
    this.areaId.set(id);
    this.carregar();
  }

  protected mudarCiclo(ano: number, ciclo: number): void {
    if (!ano || ciclo < 1 || ciclo > 6) return;
    this.ano.set(ano);
    this.ciclo.set(ciclo);
    this.carregar();
  }

  protected bimestre(c: number): string {
    return ['jan-fev', 'mar-abr', 'mai-jun', 'jul-ago', 'set-out', 'nov-dez'][c - 1];
  }

  protected titulo(g: Gaveta): string {
    switch (g.tipo) {
      case 'vistoria': return g.imovel ? 'Revisita ao imóvel' : 'Registrar vistoria';
      case 'laboratorio': return 'Resultado do laboratório';
      case 'encerrarFoco': return g.situacao === 'TRATADO' ? 'Foco tratado' : 'Foco eliminado';
      case 'acao': return g.bloqueio ? 'Planejar bloqueio de transmissão' : 'Planejar ação de controle';
      case 'realizar': return 'Registrar a realização';
      default: return 'Cancelar a ação';
    }
  }

  protected abrir(g: Gaveta, campos: Record<string, string> = {}): void {
    this.erroGaveta.set(null);
    this.f.set(campos);
    this.gaveta.set(g);
  }

  protected abrirVistoria(imovel: ImovelVetorialDto | null): void {
    this.focosNovos.set([]);
    this.imoveis.set([]);
    const area = imovel?.areaId ?? this.areaId();
    this.abrir({ tipo: 'vistoria', imovel }, { areaId: area, imovelId: imovel?.uuid ?? '', tipo: 'ROTINA', resultado: 'INSPECIONADA', data: hoje() });
    if (!imovel && area) this.carregarImoveis(area);
  }

  protected carregarImoveis(areaId: string): void {
    this.f.set({ ...this.f(), areaId, imovelId: '' });
    this.imoveis.set([]);
    if (areaId) this.service.imoveis(areaId).pipe(catchError(() => of([]))).subscribe((l) => this.imoveis.set(l));
  }

  protected abrirAcao(b: BloqueioPendenteDto | null): void {
    const area = b?.microareaId ? (this.areas().find((a) => a.microareas.some((m) => m.uuid === b.microareaId))?.uuid ?? '') : this.areaId();
    this.microareasMarcadas.set(new Set(b?.microareaId ? [b.microareaId] : []));
    this.abrir({ tipo: 'acao', bloqueio: b }, { tipo: b ? 'BLOQUEIO_TRANSMISSAO' : 'MUTIRAO_LIMPEZA', data: hoje(), areaId: area });
  }

  protected mudarAreaDaAcao(areaId: string): void {
    this.f.set({ ...this.f(), areaId });
    this.microareasMarcadas.set(new Set());
  }

  protected alternarMicroarea(id: string): void {
    const s = new Set(this.microareasMarcadas());
    if (s.has(id)) s.delete(id);
    else s.add(id);
    this.microareasMarcadas.set(s);
  }

  protected adicionarFoco(): void {
    this.focosNovos.set([...this.focosNovos(), { tipoDeposito: 'B', fase: 'LARVA', especieSuspeita: 'AEDES_AEGYPTI', amostraColetada: false }]);
  }

  protected mudarFoco(i: number, campo: keyof FocoNovo, valor: string | boolean): void {
    this.focosNovos.set(this.focosNovos().map((x, j) => (j === i ? { ...x, [campo]: valor } : x)));
  }

  protected removerFoco(i: number): void {
    this.focosNovos.set(this.focosNovos().filter((_, j) => j !== i));
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
      case 'vistoria': {
        const imovelId = g.imovel?.uuid ?? t('imovelId');
        if (!imovelId || !t('agente')) falta = 'Escolha o imóvel e informe a matrícula do agente.';
        const inspecionada = c['resultado'] === 'INSPECIONADA';
        const dep = (x: TipoDeposito) => (inspecionada ? n('dep' + x) : null);
        req = this.service.registrarVistoria({
          imovelId: imovelId ?? '', data: t('data'), agenteMatricula: t('agente') ?? '', tipo: c['tipo'] as TipoVistoriaVetorial,
          resultado: c['resultado'] as ResultadoVistoriaVetorial, depositosA1: dep('A1'), depositosA2: dep('A2'), depositosB: dep('B'), depositosC: dep('C'),
          depositosD1: dep('D1'), depositosD2: dep('D2'), depositosE: dep('E'), depositosEliminados: inspecionada ? n('eliminados') : null,
          depositosTratados: inspecionada ? n('tratados') : null, larvicida: inspecionada ? t('larvicida') : null, larvicidaGramas: inspecionada ? n('gramas') : null,
          acaoId: t('acaoId'), observacao: t('observacao'), focos: inspecionada ? this.focosNovos() : [],
        });
        break;
      }
      case 'laboratorio':
        if (!t('resultado')) falta = 'Escolha o resultado.';
        req = this.service.laboratorio(g.foco.uuid, c['resultado'] as ResultadoLaboratorioFoco);
        break;
      case 'encerrarFoco':
        req = this.service.situacaoDoFoco(g.foco.uuid, g.situacao, t('observacao'));
        break;
      case 'acao':
        if (!t('data') || !t('responsavel') || !this.microareasMarcadas().size) falta = 'Informe a data, as microáreas e o responsável.';
        req = this.service.planejar({
          tipo: c['tipo'] as TipoAcaoControleVetorial, descricao: t('descricao'), microareaIds: [...this.microareasMarcadas()], dataPrevista: t('data') ?? '',
          responsavel: t('responsavel') ?? '', notificacaoId: g.bloqueio?.notificacaoId ?? null, surtoId: null,
        });
        break;
      case 'realizar':
        req = this.service.realizar(g.acao.uuid, { data: t('data'), imoveisTrabalhados: n('imoveis'), resultado: t('resultado') });
        break;
      default:
        if (!t('motivo')) falta = 'Informe o motivo.';
        req = this.service.cancelar(g.acao.uuid, t('motivo') ?? '');
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

  protected num(v: number | null): string {
    return v == null ? '—' : v.toLocaleString('pt-BR', { maximumFractionDigits: 1 });
  }

  protected pct(v: number | null): string {
    return v == null ? '—' : this.num(v) + '%';
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  private carregar(): void {
    const area = this.areaId() || null;
    this.service.vistorias({ ano: this.ano(), ciclo: this.ciclo(), areaId: area }).subscribe({
      next: (l) => this.vistorias.set(l),
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
    this.service.focos(null, area).pipe(catchError(() => of([]))).subscribe((l) => this.focos.set(l));
    this.service.indices(this.ano(), this.ciclo()).pipe(catchError(() => of(null))).subscribe((i) => this.indices.set(i));
    this.service.acoes().pipe(catchError(() => of([]))).subscribe((l) => this.acoes.set(l));
    if (this.podeAlguma('ZOONOSES.VISTORIAR', 'ZOONOSES.GERENCIAR')) {
      this.service.pendencias(area).pipe(catchError(() => of([]))).subscribe((l) => this.pendencias.set(l));
    }
    if (this.podeAlguma('ZOONOSES.GERENCIAR', 'VIGILANCIA.INVESTIGAR')) {
      this.service.bloqueiosPendentes().pipe(catchError(() => of([]))).subscribe((l) => this.bloqueios.set(l));
    }
  }
}

function hoje(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
