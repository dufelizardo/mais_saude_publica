import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import {
  BemDto,
  CLASSES_BEM,
  ClasseBem,
  DetalheBemDto,
  ORIGENS_BEM,
  OrigemBem,
  SITUACOES_BEM,
  SITUACOES_TRANSFERENCIA,
  SituacaoBem,
  TransferenciaBemDto,
} from '../../../core/models/patrimonio';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { SetorResponseDto } from '../../../core/models/setor';
import { VeiculoDto } from '../../../core/models/transporte';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { PatrimonioService } from '../../../core/services/patrimonio';
import { SetorService } from '../../../core/services/setor';
import { TransporteService } from '../../../core/services/transporte';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';

type Aba = 'bens' | 'tombar' | 'transferencias' | 'meus';
type Gaveta = 'cadastro' | 'tombamento' | 'aceite' | 'recusa' | null;

const CONSULTAR = 'PATRIMONIO.CONSULTAR';
const GERENCIAR = 'PATRIMONIO.GERENCIAR';

/**
 * Patrimônio, fatia PM1 (ADR-0215): os bens tombados com o responsável, os bens comprados à espera do tombamento, as transferências entre
 * unidades em duas etapas e "Meus bens". Quem não tem permissão de Patrimônio vê só os próprios bens.
 */
@Component({
  selector: 'app-patrimonio',
  imports: [Drawer],
  template: `
    <header class="page-head">
      <div>
        <h1>Patrimônio</h1>
        <p>Bens, tombamento, responsáveis e transferências entre unidades</p>
      </div>
      <div class="page-actions">
        @if (pode(GERENCIAR) && aba() === 'bens') { <button type="button" class="btn" (click)="novoBem()">Cadastrar bem</button> }
      </div>
    </header>

    <div class="tabs" role="tablist" aria-label="Seções do patrimônio">
      @if (vePatrimonio()) {
        <button type="button" role="tab" id="tab-bens" [attr.aria-selected]="aba() === 'bens'" (click)="irPara('bens')">Bens</button>
        <button type="button" role="tab" id="tab-tombar" [attr.aria-selected]="aba() === 'tombar'" (click)="irPara('tombar')">A tombar @if (aTombar().length) { <span class="cnt">{{ aTombar().length }}</span> }</button>
        <button type="button" role="tab" id="tab-transferencias" [attr.aria-selected]="aba() === 'transferencias'" (click)="irPara('transferencias')">Transferências @if (pendentes()) { <span class="cnt">{{ pendentes() }}</span> }</button>
      }
      <button type="button" role="tab" id="tab-meus" [attr.aria-selected]="aba() === 'meus'" (click)="irPara('meus')">Meus bens</button>
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (aviso()) { <div class="box box--info" role="status"><strong>{{ aviso() }}</strong></div> }

    @switch (aba()) {
      @case ('bens') {
        <div class="pt-cab">
          <div class="dw-field"><label for="f-pt-unidade">Unidade</label>
            <select id="f-pt-unidade" (change)="filtro.set({ ...filtro(), unidadeId: $any($event.target).value }); carregar()">
              <option value="">Todas</option>
              @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
            </select></div>
          <div class="dw-field"><label for="f-pt-classe">Classe</label>
            <select id="f-pt-classe" (change)="filtro.set({ ...filtro(), classe: $any($event.target).value }); carregar()">
              <option value="">Todas</option>
              @for (c of listaClasses; track c) { <option [value]="c">{{ classes[c] }}</option> }
            </select></div>
          <div class="dw-field"><label for="f-pt-situacao">Situação</label>
            <select id="f-pt-situacao" (change)="filtro.set({ ...filtro(), situacao: $any($event.target).value }); carregar()">
              <option value="">Todas</option>
              @for (s of listaSituacoes; track s) { <option [value]="s">{{ situacoes[s] }}</option> }
            </select></div>
          <div class="dw-field" style="flex: 1; max-width: 320px;"><label for="f-pt-busca">Tombamento, descrição ou série</label>
            <input id="f-pt-busca" type="search" [value]="filtro().busca ?? ''" (input)="filtro.set({ ...filtro(), busca: $any($event.target).value })" (keydown.enter)="carregar()" /></div>
          <button type="button" class="btn btn--ghost btn--sm" (click)="carregar()">Buscar</button>
        </div>
        <div class="tbl-wrap card">
          <table class="pt-tabela" aria-label="Bens patrimoniais">
            <thead><tr><th scope="col">Tombamento</th><th scope="col">Bem</th><th scope="col">Unidade</th><th scope="col">Responsável</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (b of bens(); track b.uuid) {
                <tr>
                  <td class="mono">{{ b.numeroTombamento ?? '—' }}</td>
                  <td><span class="cell-main">{{ b.descricao }}</span><span class="cell-sub">{{ identificacao(b) }}</span></td>
                  <td><span class="cell-main">{{ b.unidadeNome }}</span>@if (b.setorNome) { <span class="cell-sub">{{ b.setorNome }}</span> }</td>
                  <td>{{ b.responsavelNome ?? '—' }}</td>
                  <td><span [class]="'tag tag--' + tag(b.situacao)">{{ situacoes[b.situacao] }}</span>@if (b.transferenciaPendente) { <span class="cell-sub">Indo para {{ b.transferenciaPendente.destinoNome }}</span> }</td>
                  <td class="row-actions"><button type="button" class="link-btn" (click)="abrir(b.uuid)" [attr.aria-label]="'Abrir bem ' + (b.numeroTombamento ?? b.descricao)">Abrir</button></td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhum bem.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('tombar') {
        <p class="cell-sub">Bens permanentes aceitos no recebimento definitivo de Compras, à espera do número de tombamento e do responsável.</p>
        <div class="tbl-wrap card">
          <table class="pt-tabela" aria-label="Bens a tombar">
            <thead><tr><th scope="col">Bem</th><th scope="col">Unidade</th><th scope="col">Fornecedor e nota</th><th scope="col">Valor</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (b of aTombar(); track b.uuid) {
                <tr>
                  <td>{{ b.descricao }}</td>
                  <td>{{ b.unidadeNome }}</td>
                  <td><span class="cell-main">{{ b.fornecedorNome ?? '—' }}</span><span class="cell-sub">{{ b.documento ?? '' }}@if (b.dataAquisicao) { · {{ data(b.dataAquisicao) }} }</span></td>
                  <td class="mono">{{ b.valorAquisicao == null ? '—' : moeda(b.valorAquisicao) }}</td>
                  <td class="row-actions">@if (pode(GERENCIAR)) { <button type="button" class="link-btn" (click)="abrirTombamento(b)" [attr.aria-label]="'Tombar ' + b.descricao">Tombar</button> }</td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhum bem a tombar.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('transferencias') {
        <div class="tbl-wrap card">
          <table class="pt-tabela" aria-label="Transferências de bens">
            <thead><tr><th scope="col">Bem</th><th scope="col">De → para</th><th scope="col">Enviada</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (t of transferencias(); track t.uuid) {
                <tr>
                  <td><span class="cell-main mono">{{ t.numeroTombamento }}</span><span class="cell-sub">{{ t.bemDescricao }}</span></td>
                  <td><span class="cell-main">{{ t.origemNome }} → {{ t.destinoNome }}</span><span class="cell-sub">{{ t.motivo }}</span></td>
                  <td>{{ data(t.enviadaEm) }}</td>
                  <td><span [class]="'tag tag--' + tagTransferencia(t)">{{ situacoesTransferencia[t.situacao] }}</span>@if (t.motivoRecusa) { <span class="cell-sub">{{ t.motivoRecusa }}</span> }</td>
                  <td class="row-actions">
                    @if (t.situacao === 'PENDENTE' && pode(GERENCIAR)) {
                      <button type="button" class="link-btn" (click)="abrirAceite(t)" [attr.aria-label]="'Aceitar ' + t.numeroTombamento">Aceitar</button>
                      <button type="button" class="link-btn" (click)="abrirRecusa(t)" [attr.aria-label]="'Recusar ' + t.numeroTombamento">Recusar</button>
                      <button type="button" class="link-btn" (click)="cancelar(t)" [attr.aria-label]="'Cancelar envio de ' + t.numeroTombamento">Cancelar envio</button>
                    }
                  </td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma transferência.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('meus') {
        <p class="cell-sub">Os bens sob a sua responsabilidade pelo termo assinado.</p>
        <div class="tbl-wrap card">
          <table class="pt-tabela" aria-label="Meus bens">
            <thead><tr><th scope="col">Tombamento</th><th scope="col">Bem</th><th scope="col">Unidade</th><th scope="col">Desde</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (b of meus(); track b.uuid) {
                <tr>
                  <td class="mono">{{ b.numeroTombamento }}</td>
                  <td><span class="cell-main">{{ b.descricao }}</span><span class="cell-sub">{{ identificacao(b) }}</span></td>
                  <td><span class="cell-main">{{ b.unidadeNome }}</span>@if (b.setorNome) { <span class="cell-sub">{{ b.setorNome }}</span> }</td>
                  <td>{{ b.responsavelDesde ? data(b.responsavelDesde) : '—' }}</td>
                  <td class="row-actions"><button type="button" class="link-btn" (click)="abrir(b.uuid)" [attr.aria-label]="'Abrir bem ' + b.numeroTombamento">Abrir</button></td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhum bem sob a sua responsabilidade.</td></tr> }
            </tbody>
          </table>
        </div>
      }
    }

    @if (gaveta() === 'cadastro') {
      <app-drawer titulo="Cadastrar bem" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); cadastrar()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <p class="cell-sub">Para o bem que não veio de uma compra pelo sistema: doação, cessão, transferência de outro ente ou o acervo antigo. Entra tombado.</p>
            <div class="dw-field"><label for="f-pt-descricao">Descrição <span class="req" aria-hidden="true">*</span></label><input id="f-pt-descricao" type="text" maxlength="500" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)" /></div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-pt-cad-classe">Classe <span class="req" aria-hidden="true">*</span></label>
                <select id="f-pt-cad-classe" (change)="set('classe', $event)">
                  <option value="" [selected]="!f()['classe']">Escolha…</option>
                  @for (c of listaClasses; track c) { <option [value]="c" [selected]="f()['classe'] === c">{{ classes[c] }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-pt-origem">Origem <span class="req" aria-hidden="true">*</span></label>
                <select id="f-pt-origem" (change)="set('origem', $event)">
                  @for (o of listaOrigens; track o) { <option [value]="o" [selected]="f()['origem'] === o">{{ origens[o] }}</option> }
                </select></div>
            </div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-pt-marca">Marca<span class="opt">opcional</span></label><input id="f-pt-marca" type="text" maxlength="80" [value]="f()['marca'] ?? ''" (input)="set('marca', $event)" /></div>
              <div class="dw-field"><label for="f-pt-modelo">Modelo<span class="opt">opcional</span></label><input id="f-pt-modelo" type="text" maxlength="80" [value]="f()['modelo'] ?? ''" (input)="set('modelo', $event)" /></div>
              <div class="dw-field"><label for="f-pt-serie">Número de série<span class="opt">opcional</span></label><input id="f-pt-serie" type="text" maxlength="80" [value]="f()['numeroSerie'] ?? ''" (input)="set('numeroSerie', $event)" /></div>
            </div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-pt-cad-unidade">Unidade <span class="req" aria-hidden="true">*</span></label>
                <select id="f-pt-cad-unidade" (change)="set('unidadeId', $event); f.set({ ...f(), setorId: '' })">
                  <option value="" [selected]="!f()['unidadeId']">Escolha…</option>
                  @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['unidadeId'] === u.uuid">{{ u.nome }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-pt-cad-setor">Setor<span class="opt">opcional</span></label>
                <select id="f-pt-cad-setor" (change)="set('setorId', $event)">
                  <option value="" [selected]="!f()['setorId']">—</option>
                  @for (s of setoresDa(f()['unidadeId']); track s.uuid) { <option [value]="s.uuid" [selected]="f()['setorId'] === s.uuid">{{ s.nome }}</option> }
                </select></div>
            </div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-pt-data">Data de aquisição <span class="req" aria-hidden="true">*</span></label><input id="f-pt-data" type="date" [max]="hoje" [value]="f()['dataAquisicao'] ?? ''" (input)="set('dataAquisicao', $event)" /></div>
              <div class="dw-field"><label for="f-pt-valor">Valor (R$)<span class="opt">opcional</span></label><input id="f-pt-valor" type="number" min="0" step="0.01" [value]="f()['valorAquisicao'] ?? ''" (input)="set('valorAquisicao', $event)" /></div>
              <div class="dw-field"><label for="f-pt-documento">Documento<span class="opt">opcional</span></label><input id="f-pt-documento" type="text" maxlength="60" placeholder="nota, termo de doação…" [value]="f()['documento'] ?? ''" (input)="set('documento', $event)" /></div>
            </div>
            <div class="dw-field"><label for="f-pt-responsavel">Matrícula do responsável <span class="req" aria-hidden="true">*</span></label><input id="f-pt-responsavel" type="text" maxlength="30" [value]="f()['responsavelMatricula'] ?? ''" (input)="set('responsavelMatricula', $event)" /></div>
            <div class="dw-field"><label for="f-pt-obs">Observação<span class="opt">opcional</span></label><textarea id="f-pt-obs" rows="2" maxlength="500" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)"></textarea></div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Cadastrar</button></div>
        </form>
      </app-drawer>
    }

    @if (gaveta() === 'tombamento' && alvo(); as b) {
      <app-drawer [titulo]="'Tombar: ' + b.descricao" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); tombar(b)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <dl class="ro">
              <div><dt>Unidade</dt><dd>{{ b.unidadeNome }}</dd></div>
              <div><dt>Fornecedor</dt><dd>{{ b.fornecedorNome ?? '—' }} · {{ b.documento ?? '' }}</dd></div>
            </dl>
            <div class="dw-field"><label for="f-pt-tb-classe">Classe <span class="req" aria-hidden="true">*</span></label>
              <select id="f-pt-tb-classe" (change)="set('classe', $event)">
                <option value="" [selected]="!f()['classe']">Escolha…</option>
                @for (c of listaClasses; track c) { <option [value]="c" [selected]="f()['classe'] === c">{{ classes[c] }}</option> }
              </select></div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-pt-tb-marca">Marca<span class="opt">opcional</span></label><input id="f-pt-tb-marca" type="text" maxlength="80" (input)="set('marca', $event)" /></div>
              <div class="dw-field"><label for="f-pt-tb-modelo">Modelo<span class="opt">opcional</span></label><input id="f-pt-tb-modelo" type="text" maxlength="80" (input)="set('modelo', $event)" /></div>
              <div class="dw-field"><label for="f-pt-tb-serie">Número de série<span class="opt">opcional</span></label><input id="f-pt-tb-serie" type="text" maxlength="80" (input)="set('numeroSerie', $event)" /></div>
            </div>
            <div class="dw-field"><label for="f-pt-tb-setor">Setor<span class="opt">opcional</span></label>
              <select id="f-pt-tb-setor" (change)="set('setorId', $event)">
                <option value="">—</option>
                @for (s of setoresDa(b.unidadeId); track s.uuid) { <option [value]="s.uuid">{{ s.nome }}</option> }
              </select></div>
            <div class="dw-field"><label for="f-pt-tb-responsavel">Matrícula do responsável <span class="req" aria-hidden="true">*</span></label><input id="f-pt-tb-responsavel" type="text" maxlength="30" (input)="set('responsavelMatricula', $event)" /></div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Tombar</button></div>
        </form>
      </app-drawer>
    }

    @if ((gaveta() === 'aceite' || gaveta() === 'recusa') && transferencia(); as t) {
      <app-drawer [titulo]="(gaveta() === 'aceite' ? 'Aceitar ' : 'Recusar ') + t.numeroTombamento" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); gaveta() === 'aceite' ? aceitar(t) : recusar(t)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <dl class="ro">
              <div><dt>Bem</dt><dd>{{ t.bemDescricao }}</dd></div>
              <div><dt>De → para</dt><dd>{{ t.origemNome }} → {{ t.destinoNome }}</dd></div>
              <div><dt>Motivo</dt><dd>{{ t.motivo }}</dd></div>
            </dl>
            @if (gaveta() === 'aceite') {
              <div class="dw-field"><label for="f-pt-ac-setor">Setor no destino<span class="opt">opcional</span></label>
                <select id="f-pt-ac-setor" (change)="set('setorId', $event)">
                  <option value="">—</option>
                  @for (s of setoresDa(t.destinoId); track s.uuid) { <option [value]="s.uuid">{{ s.nome }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-pt-ac-responsavel">Matrícula do novo responsável <span class="req" aria-hidden="true">*</span></label><input id="f-pt-ac-responsavel" type="text" maxlength="30" (input)="set('responsavelMatricula', $event)" /></div>
            } @else {
              <div class="dw-field"><label for="f-pt-rc-motivo">Motivo da recusa <span class="req" aria-hidden="true">*</span></label><textarea id="f-pt-rc-motivo" rows="2" maxlength="500" (input)="set('motivo', $event)"></textarea></div>
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Voltar</button><button type="submit" class="btn" [disabled]="enviando()">{{ gaveta() === 'aceite' ? 'Aceitar' : 'Recusar' }}</button></div>
        </form>
      </app-drawer>
    }

    @if (detalhe(); as d) {
      <app-drawer [titulo]="'Bem ' + (d.bem.numeroTombamento ?? 'a tombar')" (fechar)="detalhe.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Bem</dt><dd>{{ d.bem.descricao }}</dd></div>
            <div><dt>Classe</dt><dd>{{ d.bem.classe ? classes[d.bem.classe] : '—' }}</dd></div>
            <div><dt>Identificação</dt><dd>{{ identificacao(d.bem) || '—' }}</dd></div>
            <div><dt>Situação</dt><dd>{{ situacoes[d.bem.situacao] }}</dd></div>
            <div><dt>Unidade</dt><dd>{{ d.bem.unidadeNome }}@if (d.bem.setorNome) { · {{ d.bem.setorNome }} }</dd></div>
            <div><dt>Responsável</dt><dd>{{ d.bem.responsavelNome ?? '—' }}@if (d.bem.responsavelMatricula) { <span class="cell-sub">matrícula {{ d.bem.responsavelMatricula }}</span> }</dd></div>
            <div><dt>Aquisição</dt><dd>{{ origens[d.bem.origem] }}@if (d.bem.dataAquisicao) { · {{ data(d.bem.dataAquisicao) }} }@if (d.bem.valorAquisicao != null) { · {{ moeda(d.bem.valorAquisicao) }} }@if (d.bem.documento) { · {{ d.bem.documento }} }@if (d.bem.fornecedorNome) { · {{ d.bem.fornecedorNome }} }</dd></div>
            @if (d.bem.veiculoPlaca) { <div><dt>Veículo da frota</dt><dd class="mono">{{ d.bem.veiculoPlaca }}</dd></div> }
            @if (d.bem.observacao) { <div><dt>Observação</dt><dd>{{ d.bem.observacao }}</dd></div> }
          </dl>

          @if (pode(GERENCIAR) && ativo(d.bem)) {
            <h3 class="mo-grupo pt-titulo">Ações</h3>
            <div class="pt-linha">
              <div class="dw-field"><label for="f-pt-novo-resp">Novo responsável (matrícula)</label><input id="f-pt-novo-resp" type="text" maxlength="30" (input)="set('novoResponsavel', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="trocarResponsavel(d.bem)" [disabled]="enviando()">Trocar responsável</button>
            </div>
            <div class="pt-linha">
              <button type="button" class="btn btn--ghost btn--sm" (click)="mudarUso(d.bem)" [disabled]="enviando()">{{ d.bem.situacao === 'EM_USO' ? 'Marcar como ocioso' : 'Marcar como em uso' }}</button>
            </div>
            @if (!d.bem.transferenciaPendente) {
              <div class="pt-linha">
                <div class="dw-field"><label for="f-pt-destino">Enviar para</label>
                  <select id="f-pt-destino" (change)="set('destinoId', $event)">
                    <option value="">Escolha…</option>
                    @for (u of unidades(); track u.uuid) { @if (u.uuid !== d.bem.unidadeId) { <option [value]="u.uuid">{{ u.nome }}</option> } }
                  </select></div>
                <div class="dw-field" style="flex: 1;"><label for="f-pt-motivo-envio">Motivo</label><input id="f-pt-motivo-envio" type="text" maxlength="500" (input)="set('motivoEnvio', $event)" /></div>
                <button type="button" class="btn btn--ghost btn--sm" (click)="transferir(d.bem)" [disabled]="enviando()">Enviar</button>
              </div>
            } @else {
              <p class="cell-sub">Enviado para {{ d.bem.transferenciaPendente.destinoNome }} em {{ data(d.bem.transferenciaPendente.enviadaEm) }}, à espera do aceite.</p>
            }
            @if (d.bem.classe === 'VEICULO') {
              <div class="pt-linha">
                <div class="dw-field"><label for="f-pt-veiculo">Veículo da frota</label>
                  <select id="f-pt-veiculo" (change)="set('veiculoId', $event)">
                    <option value="">Escolha…</option>
                    @for (v of veiculos(); track v.uuid) { <option [value]="v.uuid" [selected]="d.bem.veiculoId === v.uuid">{{ v.placa }} · {{ v.prefixo }}</option> }
                  </select></div>
                <button type="button" class="btn btn--ghost btn--sm" (click)="vincularVeiculo(d.bem)" [disabled]="enviando()">Ligar</button>
              </div>
            }
          }

          <h3 class="mo-grupo pt-titulo">Responsáveis</h3>
          <ul class="pt-lista" aria-label="Histórico de responsáveis">
            @for (r of d.responsaveis; track r.uuid) {
              <li><span>{{ r.nome }} <span class="cell-sub">{{ r.matricula }}</span></span><span class="cell-sub">{{ data(r.desde) }} – {{ r.ate ? data(r.ate) : 'atual' }}</span></li>
            } @empty { <li class="cell-sub">Ainda sem responsável.</li> }
          </ul>

          @if (d.transferencias.length) {
            <h3 class="mo-grupo pt-titulo">Transferências</h3>
            <ul class="pt-lista" aria-label="Histórico de transferências">
              @for (t of d.transferencias; track t.uuid) {
                <li><span>{{ t.origemNome }} → {{ t.destinoNome }} <span class="cell-sub">{{ t.motivo }}</span></span><span class="cell-sub">{{ data(t.enviadaEm) }} · {{ situacoesTransferencia[t.situacao] }}</span></li>
              }
            </ul>
          }
        </div>
      </app-drawer>
    }
  `,
  styles: `
    .pt-cab { display: flex; gap: var(--s-3); align-items: flex-end; margin-bottom: var(--s-2); flex-wrap: wrap; }
    .pt-cab .dw-field { margin: 0; }
    .pt-tabela { min-width: 0; width: 100%; }
    .pt-titulo { margin-top: var(--s-4); }
    .pt-linha { display: flex; gap: var(--s-2); align-items: flex-end; flex-wrap: wrap; margin-bottom: var(--s-2); }
    .pt-linha .dw-field { margin: 0; }
    .pt-lista { margin: 0; padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .pt-lista li { display: flex; justify-content: space-between; gap: var(--s-2); align-items: baseline; flex-wrap: wrap; }
  `,
})
export class Patrimonio {
  private readonly service = inject(PatrimonioService);
  private readonly authService = inject(AuthService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly setorService = inject(SetorService);
  private readonly transporteService = inject(TransporteService);

  protected readonly GERENCIAR = GERENCIAR;
  protected readonly classes = CLASSES_BEM;
  protected readonly listaClasses = Object.keys(CLASSES_BEM) as ClasseBem[];
  protected readonly situacoes = SITUACOES_BEM;
  protected readonly listaSituacoes = Object.keys(SITUACOES_BEM) as SituacaoBem[];
  protected readonly origens = ORIGENS_BEM;
  protected readonly listaOrigens = Object.keys(ORIGENS_BEM) as OrigemBem[];
  protected readonly situacoesTransferencia = SITUACOES_TRANSFERENCIA;
  protected readonly hoje = hojeIso();

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('bens');
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly filtro = signal<{ unidadeId?: string; classe?: ClasseBem; situacao?: SituacaoBem; busca?: string }>({});
  protected readonly bens = signal<BemDto[]>([]);
  protected readonly aTombar = signal<BemDto[]>([]);
  protected readonly transferencias = signal<TransferenciaBemDto[]>([]);
  protected readonly meus = signal<BemDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  private readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly veiculos = signal<VeiculoDto[]>([]);
  protected readonly gaveta = signal<Gaveta>(null);
  protected readonly alvo = signal<BemDto | null>(null);
  protected readonly transferencia = signal<TransferenciaBemDto | null>(null);
  protected readonly detalhe = signal<DetalheBemDto | null>(null);

  protected readonly pendentes = computed(() => this.transferencias().filter((t) => t.situacao === 'PENDENTE').length);

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      if (!this.vePatrimonio()) {
        this.irPara('meus');
        return;
      }
      this.carregar();
      this.carregarATombar();
      this.carregarTransferencias();
      this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidades.set(l));
      this.setorService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.setores.set(l));
    });
  }

  protected vePatrimonio(): boolean {
    return this.pode(CONSULTAR) || this.pode(GERENCIAR);
  }

  protected pode(permissao: string): boolean {
    const a = this.acesso();
    return !a.restrito || a.permissoes.has(permissao);
  }

  protected irPara(aba: Aba): void {
    this.aba.set(aba);
    this.aviso.set(null);
    if (aba === 'bens') this.carregar();
    if (aba === 'tombar') this.carregarATombar();
    if (aba === 'transferencias') this.carregarTransferencias();
    if (aba === 'meus') this.service.meusBens().subscribe({ next: (l) => this.meus.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  protected carregar(): void {
    const filtro = { ...this.filtro(), busca: this.filtro().busca?.trim() || undefined };
    this.service.bens(filtro).subscribe({
      next: (l) => {
        this.bens.set(l);
        this.erro.set(null);
      },
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  private carregarATombar(): void {
    this.service.bens({ situacao: 'A_TOMBAR' }).pipe(catchError(() => of([]))).subscribe((l) => this.aTombar.set(l));
  }

  private carregarTransferencias(): void {
    this.service.transferencias().pipe(catchError(() => of([]))).subscribe((l) => this.transferencias.set(l));
  }

  protected setoresDa(unidadeId: string | undefined): SetorResponseDto[] {
    return this.setores().filter((s) => s.unidadeUuid === unidadeId);
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  // ── Cadastro e tombamento ──

  protected novoBem(): void {
    this.f.set({ origem: 'DOACAO', dataAquisicao: hojeIso() });
    this.erroGaveta.set(null);
    this.gaveta.set('cadastro');
  }

  protected cadastrar(): void {
    const v = this.f();
    if (!v['descricao']?.trim() || !v['classe'] || !v['unidadeId'] || !v['dataAquisicao'] || !v['responsavelMatricula']?.trim()) {
      this.erroGaveta.set('Preencha a descrição, a classe, a unidade, a data de aquisição e o responsável.');
      return;
    }
    this.executar(this.service.cadastrar({
      descricao: v['descricao'].trim(), classe: v['classe'] as ClasseBem, marca: texto(v['marca']), modelo: texto(v['modelo']), numeroSerie: texto(v['numeroSerie']),
      unidadeId: v['unidadeId'], setorId: v['setorId'] || null, origem: (v['origem'] || 'DOACAO') as OrigemBem,
      valorAquisicao: v['valorAquisicao'] ? Number(v['valorAquisicao']) : null, dataAquisicao: v['dataAquisicao'], documento: texto(v['documento']),
      responsavelMatricula: v['responsavelMatricula'].trim(), observacao: texto(v['observacao']),
    }), (d) => {
      this.gaveta.set(null);
      this.aviso.set(`Bem cadastrado com o tombamento ${d.bem.numeroTombamento}.`);
      this.carregar();
    });
  }

  protected abrirTombamento(b: BemDto): void {
    this.alvo.set(b);
    this.f.set({});
    this.erroGaveta.set(null);
    this.gaveta.set('tombamento');
  }

  protected tombar(b: BemDto): void {
    const v = this.f();
    if (!v['classe'] || !v['responsavelMatricula']?.trim()) {
      this.erroGaveta.set('Informe a classe e o responsável.');
      return;
    }
    this.executar(this.service.tombar(b.uuid, { classe: v['classe'] as ClasseBem, marca: texto(v['marca']), modelo: texto(v['modelo']),
      numeroSerie: texto(v['numeroSerie']), setorId: v['setorId'] || null, responsavelMatricula: v['responsavelMatricula'].trim(), observacao: null }), (d) => {
      this.gaveta.set(null);
      this.aviso.set(`Bem tombado com o número ${d.bem.numeroTombamento}.`);
      this.carregarATombar();
      this.carregar();
    });
  }

  // ── Ficha ──

  protected abrir(id: string): void {
    this.erroGaveta.set(null);
    this.f.set({});
    this.service.bem(id).subscribe({
      next: (d) => {
        this.detalhe.set(d);
        if (d.bem.classe === 'VEICULO' && this.pode(GERENCIAR)) {
          this.transporteService.veiculos(d.bem.unidadeId).pipe(catchError(() => of([]))).subscribe((l) => this.veiculos.set(l));
        }
      },
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  protected ativo(b: BemDto): boolean {
    return b.situacao === 'EM_USO' || b.situacao === 'OCIOSO';
  }

  protected trocarResponsavel(b: BemDto): void {
    const m = this.f()['novoResponsavel']?.trim();
    if (!m) {
      this.erroGaveta.set('Informe a matrícula do novo responsável.');
      return;
    }
    this.executar(this.service.responsavel(b.uuid, m), (d) => this.atualizarFicha(d));
  }

  protected mudarUso(b: BemDto): void {
    this.executar(this.service.situacao(b.uuid, b.situacao === 'EM_USO' ? 'OCIOSO' : 'EM_USO'), (d) => this.atualizarFicha(d));
  }

  protected transferir(b: BemDto): void {
    const v = this.f();
    if (!v['destinoId'] || !v['motivoEnvio']?.trim()) {
      this.erroGaveta.set('Escolha o destino e informe o motivo.');
      return;
    }
    this.executar(this.service.transferir(b.uuid, v['destinoId'], v['motivoEnvio'].trim()), (d) => {
      this.atualizarFicha(d);
      this.carregarTransferencias();
    });
  }

  protected vincularVeiculo(b: BemDto): void {
    const v = this.f()['veiculoId'];
    if (!v) {
      this.erroGaveta.set('Escolha o veículo.');
      return;
    }
    this.executar(this.service.veiculo(b.uuid, v), (d) => this.atualizarFicha(d));
  }

  private atualizarFicha(d: DetalheBemDto): void {
    this.detalhe.set(d);
    this.f.set({});
    this.carregar();
  }

  // ── Transferências ──

  protected abrirAceite(t: TransferenciaBemDto): void {
    this.transferencia.set(t);
    this.f.set({});
    this.erroGaveta.set(null);
    this.gaveta.set('aceite');
  }

  protected abrirRecusa(t: TransferenciaBemDto): void {
    this.transferencia.set(t);
    this.f.set({});
    this.erroGaveta.set(null);
    this.gaveta.set('recusa');
  }

  protected aceitar(t: TransferenciaBemDto): void {
    const m = this.f()['responsavelMatricula']?.trim();
    if (!m) {
      this.erroGaveta.set('Informe a matrícula do novo responsável.');
      return;
    }
    this.executar(this.service.aceitar(t.uuid, this.f()['setorId'] || null, m), () => this.depoisDaTransferencia('Bem recebido na unidade.'));
  }

  protected recusar(t: TransferenciaBemDto): void {
    const m = this.f()['motivo']?.trim();
    if (!m) {
      this.erroGaveta.set('Informe o motivo da recusa.');
      return;
    }
    this.executar(this.service.recusar(t.uuid, m), () => this.depoisDaTransferencia('Transferência recusada.'));
  }

  protected cancelar(t: TransferenciaBemDto): void {
    this.service.cancelar(t.uuid).subscribe({
      next: () => this.depoisDaTransferencia('Envio cancelado.'),
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  private depoisDaTransferencia(texto: string): void {
    this.gaveta.set(null);
    this.aviso.set(texto);
    this.carregarTransferencias();
    this.carregar();
  }

  private executar(chamada: Observable<DetalheBemDto>, depois: (d: DetalheBemDto) => void): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    chamada.subscribe({
      next: (d) => {
        this.enviando.set(false);
        depois(d);
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  // ── Formatação ──

  protected identificacao(b: BemDto): string {
    return [b.marca, b.modelo, b.numeroSerie ? `série ${b.numeroSerie}` : null].filter((x) => !!x).join(' · ');
  }

  protected tag(s: SituacaoBem): string {
    switch (s) {
      case 'EM_USO':
        return 'ok';
      case 'A_TOMBAR':
        return 'info';
      case 'EM_MANUTENCAO':
        return 'warn';
      default:
        return 'muted';
    }
  }

  protected tagTransferencia(t: TransferenciaBemDto): string {
    return t.situacao === 'PENDENTE' ? 'warn' : t.situacao === 'ACEITA' ? 'ok' : 'muted';
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}

function texto(v: string | undefined): string | null {
  return v?.trim() ? v.trim() : null;
}

/** Hoje no fuso de São Paulo, o dia que o servidor usa nas validações de data (o navegador pode estar em outro fuso). */
function hojeIso(): string {
  return new Date().toLocaleDateString('en-CA', { timeZone: 'America/Sao_Paulo' });
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
