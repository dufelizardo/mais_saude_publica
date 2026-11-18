import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import {
  CERTIDOES,
  CotacaoDto,
  FONTES_COTACAO,
  FonteCotacao,
  FornecedorDto,
  ItemCompraRequest,
  SANCOES,
  SITUACOES_SOLICITACAO,
  SituacaoSolicitacaoCompra,
  SolicitacaoCompraDto,
  TIPOS_ITEM,
  TipoCertidaoFornecedor,
  TipoItemCompra,
  TipoSancaoFornecedor,
} from '../../../core/models/compras';
import { MaterialEstoqueDto } from '../../../core/models/estoque';
import { MedicamentoResponseDto } from '../../../core/models/medicamento';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { SetorResponseDto } from '../../../core/models/setor';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { ComprasService } from '../../../core/services/compras';
import { EstoqueService } from '../../../core/services/estoque';
import { MedicamentoService } from '../../../core/services/medicamento';
import { SetorService } from '../../../core/services/setor';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { ContratosCompras } from './contratos-compras';

type Aba = 'solicitacoes' | 'fornecedores' | 'contratacoes' | 'contratos';

/** Uma linha do editor de itens da solicitação. */
interface LinhaItem {
  tipo: TipoItemCompra;
  materialId: string;
  medicamentoId: string;
  descricao: string;
  unidadeMedida: string;
  quantidade: string;
}

const SOLICITAR = 'COMPRAS.SOLICITAR';
const GERENCIAR = 'COMPRAS.GERENCIAR';

/**
 * Compras, fatia CP1 (ADR-0212): a unidade monta a solicitação de compra (DFD), com a sugestão pelo estoque mínimo, e envia; o setor de
 * compras analisa, pesquisa os preços (mediana de ao menos 3 cotações) e mantém os fornecedores com as certidões e as sanções. As abas
 * Contratações e Contratos (CP2, ADR-0213) ficam no componente {@link ContratosCompras}; quem só gere ou fiscaliza contrato vê só a última.
 */
@Component({
  selector: 'app-compras',
  imports: [Drawer, ContratosCompras],
  template: `
    <header class="page-head">
      <div>
        <h1>Compras</h1>
        <p>Solicitações de compra, pesquisa de preços e fornecedores</p>
      </div>
      <div class="page-actions">
        @if (aba() === 'solicitacoes' && pode(SOLICITAR)) { <button type="button" class="btn" (click)="novaSolicitacao()">Nova solicitação</button> }
        @if (aba() === 'fornecedores' && pode(GERENCIAR)) { <button type="button" class="btn" (click)="novoFornecedor()">Cadastrar fornecedor</button> }
      </div>
    </header>

    <div class="tabs" role="tablist" aria-label="Seções de compras">
      @if (compraOuPede()) {
        <button type="button" role="tab" id="tab-solicitacoes" [attr.aria-selected]="aba() === 'solicitacoes'" (click)="aba.set('solicitacoes')">Solicitações @if (paraAgir()) { <span class="cnt">{{ paraAgir() }}</span> }</button>
        <button type="button" role="tab" id="tab-fornecedores" [attr.aria-selected]="aba() === 'fornecedores'" (click)="abrirFornecedores()">Fornecedores</button>
      }
      @if (pode(GERENCIAR)) { <button type="button" role="tab" id="tab-contratacoes" [attr.aria-selected]="aba() === 'contratacoes'" (click)="aba.set('contratacoes')">Contratações</button> }
      <button type="button" role="tab" id="tab-contratos" [attr.aria-selected]="aba() === 'contratos'" (click)="aba.set('contratos')">Contratos</button>
    </div>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    @switch (aba()) {
      @case ('contratacoes') { <app-contratos-compras aba="contratacoes" /> }
      @case ('contratos') { <app-contratos-compras aba="contratos" /> }
      @case ('solicitacoes') {
        <div class="cp-cab">
          <div class="dw-field" style="margin: 0;"><label for="f-cp-filtro">Situação</label>
            <select id="f-cp-filtro" (change)="situacao.set($any($event.target).value); carregar()">
              <option value="">Todas</option>
              @for (s of listaSituacoes; track s) { <option [value]="s" [selected]="situacao() === s">{{ situacoes[s] }}</option> }
            </select></div>
        </div>
        <div class="tbl-wrap card">
          <table class="cp-tabela" aria-label="Solicitações de compra">
            <thead><tr><th scope="col">Número</th><th scope="col">Itens</th><th scope="col">Prioridade</th><th scope="col">Valor estimado</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (s of solicitacoes(); track s.uuid) {
                <tr>
                  <td><span class="cell-main mono">{{ s.numero }}</span><span class="cell-sub">{{ s.unidadeNome }}@if (s.setorNome) { · {{ s.setorNome }} }</span></td>
                  <td><span class="cell-main">{{ s.itens.length }} item(ns)</span><span class="cell-sub">{{ resumo(s) }}</span></td>
                  <td>@if (s.prioridade === 'URGENTE') { <span class="tag tag--alert">Urgente</span> } @else { Normal }</td>
                  <td class="mono">{{ s.valorEstimado == null ? '—' : moeda(s.valorEstimado) }}</td>
                  <td><span [class]="'tag tag--' + tagSituacao(s.situacao)">{{ situacoes[s.situacao] }}</span></td>
                  <td class="row-actions"><button type="button" class="link-btn" (click)="abrirSolicitacao(s.uuid)" [attr.aria-label]="'Abrir solicitação ' + s.numero">Abrir</button></td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma solicitação de compra.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('fornecedores') {
        <div class="cp-cab">
          <div class="dw-field" style="margin: 0; flex: 1; max-width: 360px;"><label for="f-cp-busca">Buscar por nome, CPF ou CNPJ</label>
            <input id="f-cp-busca" type="search" [value]="busca()" (input)="busca.set($any($event.target).value)" (keydown.enter)="carregarFornecedores()" /></div>
          <button type="button" class="btn btn--ghost btn--sm" (click)="carregarFornecedores()">Buscar</button>
        </div>
        <div class="tbl-wrap card">
          <table class="cp-tabela" aria-label="Fornecedores">
            <thead><tr><th scope="col">Fornecedor</th><th scope="col">Situação</th><th scope="col">Certidões</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (f of fornecedores(); track f.uuid) {
                <tr>
                  <td><span class="cell-main">{{ f.razaoSocial }}</span><span class="cell-sub mono">{{ documento(f) }}@if (f.nomeFantasia) { · {{ f.nomeFantasia }} }</span></td>
                  <td><span [class]="'tag tag--' + tagFornecedor(f)">{{ rotuloFornecedor(f) }}</span></td>
                  <td>@if (f.certidoesPendentes.length) { <span class="cell-sub">Pendentes: {{ pendentes(f) }}</span> } @else { <span class="cell-sub">Em dia</span> }</td>
                  <td class="row-actions"><button type="button" class="link-btn" (click)="abrirFornecedor(f.uuid)" [attr.aria-label]="'Abrir fornecedor ' + f.razaoSocial">Abrir</button></td>
                </tr>
              } @empty { <tr><td colspan="4" class="cell-sub">Nenhum fornecedor.</td></tr> }
            </tbody>
          </table>
        </div>
      }
    }

    @if (editando()) {
      <app-drawer [titulo]="editandoId() ? 'Editar solicitação' : 'Nova solicitação de compra'" (fechar)="editando.set(false)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvarSolicitacao()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <div class="dw-row">
              <div class="dw-field"><label for="f-cp-unidade">Unidade <span class="req" aria-hidden="true">*</span></label>
                <select id="f-cp-unidade" (change)="set('unidadeId', $event)" [disabled]="!!editandoId()">
                  <option value="" [selected]="!f()['unidadeId']">Escolha…</option>
                  @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['unidadeId'] === u.uuid">{{ u.nome }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-cp-setor">Setor<span class="opt">opcional</span></label>
                <select id="f-cp-setor" (change)="set('setorId', $event)">
                  <option value="" [selected]="!f()['setorId']">—</option>
                  @for (s of setoresDaUnidade(); track s.uuid) { <option [value]="s.uuid" [selected]="f()['setorId'] === s.uuid">{{ s.nome }}</option> }
                </select></div>
              <div class="dw-field"><label for="f-cp-prioridade">Prioridade</label>
                <select id="f-cp-prioridade" (change)="set('prioridade', $event)">
                  <option value="NORMAL" [selected]="f()['prioridade'] !== 'URGENTE'">Normal</option>
                  <option value="URGENTE" [selected]="f()['prioridade'] === 'URGENTE'">Urgente</option>
                </select></div>
            </div>
            <div class="dw-field"><label for="f-cp-justificativa">Justificativa da necessidade <span class="req" aria-hidden="true">*</span></label><textarea id="f-cp-justificativa" rows="2" maxlength="2000" [value]="f()['justificativa'] ?? ''" (input)="set('justificativa', $event)"></textarea></div>

            <h3 class="mo-grupo cp-titulo">Itens</h3>
            <ul class="cp-itens" aria-label="Itens da solicitação">
              @for (l of linhas(); track $index; let i = $index) {
                <li>
                  <div class="dw-row">
                    <div class="dw-field"><label [for]="'f-cp-tipo-' + i">Tipo</label>
                      <select [id]="'f-cp-tipo-' + i" (change)="mudarLinha(i, 'tipo', $event)">
                        @for (t of listaTipos; track t) { <option [value]="t" [selected]="l.tipo === t">{{ tipos[t] }}</option> }
                      </select></div>
                    @switch (l.tipo) {
                      @case ('MATERIAL') {
                        <div class="dw-field" style="flex: 2;"><label [for]="'f-cp-material-' + i">Material</label>
                          <select [id]="'f-cp-material-' + i" (change)="mudarLinha(i, 'materialId', $event)">
                            <option value="" [selected]="!l.materialId">Escolha…</option>
                            @for (m of materiais(); track m.uuid) { <option [value]="m.uuid" [selected]="l.materialId === m.uuid">{{ m.nome }} ({{ m.codigo }})</option> }
                          </select></div>
                      }
                      @case ('MEDICAMENTO') {
                        <div class="dw-field" style="flex: 2;"><label [for]="'f-cp-medicamento-' + i">Medicamento</label>
                          <select [id]="'f-cp-medicamento-' + i" (change)="mudarLinha(i, 'medicamentoId', $event)">
                            <option value="" [selected]="!l.medicamentoId">Escolha…</option>
                            @for (m of medicamentos(); track m.uuid) { <option [value]="m.uuid" [selected]="l.medicamentoId === m.uuid">{{ m.nome }}@if (m.apresentacao) { {{ m.apresentacao }} }</option> }
                          </select></div>
                      }
                      @default {
                        <div class="dw-field" style="flex: 2;"><label [for]="'f-cp-descricao-' + i">Serviço</label><input [id]="'f-cp-descricao-' + i" type="text" maxlength="500" [value]="l.descricao" (input)="mudarLinha(i, 'descricao', $event)" /></div>
                        <div class="dw-field"><label [for]="'f-cp-medida-' + i">Unidade de medida</label><input [id]="'f-cp-medida-' + i" type="text" maxlength="30" placeholder="serviço, mês…" [value]="l.unidadeMedida" (input)="mudarLinha(i, 'unidadeMedida', $event)" /></div>
                      }
                    }
                    <div class="dw-field" style="max-width: 110px;"><label [for]="'f-cp-qtd-' + i">Quantidade</label><input [id]="'f-cp-qtd-' + i" type="number" min="1" [value]="l.quantidade" (input)="mudarLinha(i, 'quantidade', $event)" /></div>
                  </div>
                  <div class="row-actions"><button type="button" class="link-btn" (click)="removerLinha(i)" [attr.aria-label]="'Remover item ' + (i + 1)">Remover</button></div>
                </li>
              }
            </ul>
            <div class="row-actions">
              <button type="button" class="btn btn--ghost btn--sm" (click)="adicionarLinha()">Adicionar item</button>
              <button type="button" class="btn btn--ghost btn--sm" (click)="sugerir()" [disabled]="!f()['unidadeId']">Sugerir pelo estoque</button>
            </div>
            @if (aviso()) { <p class="cell-sub" role="status">{{ aviso() }}</p> }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="editando.set(false)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Salvar rascunho</button></div>
        </form>
      </app-drawer>
    }

    @if (detalhe(); as s) {
      <app-drawer [titulo]="'Solicitação ' + s.numero" (fechar)="detalhe.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Situação</dt><dd>{{ situacoes[s.situacao] }}@if (s.motivo) { · {{ s.motivo }} }</dd></div>
            <div><dt>Unidade</dt><dd>{{ s.unidadeNome }}@if (s.setorNome) { · {{ s.setorNome }} }</dd></div>
            <div><dt>Prioridade</dt><dd>{{ s.prioridade === 'URGENTE' ? 'Urgente' : 'Normal' }}</dd></div>
            <div><dt>Justificativa</dt><dd>{{ s.justificativa }}</dd></div>
            @if (s.valorEstimado != null) { <div><dt>Valor estimado</dt><dd>{{ moeda(s.valorEstimado) }}</dd></div> }
          </dl>

          <h3 class="mo-grupo cp-titulo">Itens e cotações</h3>
          <ul class="cp-itens" aria-label="Itens e cotações">
            @for (i of s.itens; track i.uuid) {
              <li>
                <span><strong>{{ i.ordem }}. {{ i.descricao }}</strong> <span class="cell-sub">{{ i.quantidade }} {{ i.unidadeMedida.toLowerCase() }} · {{ tipos[i.tipo] }}</span></span>
                @if (i.cotacoes.length) {
                  <ul class="cp-cotacoes" [attr.aria-label]="'Cotações do item ' + i.ordem">
                    @for (c of i.cotacoes; track c.uuid) {
                      <li><span>{{ fonteRot(c) }}</span><span class="mono">{{ moeda(c.precoUnitario) }}</span><span class="cell-sub">{{ data(c.cotadoEm) }}</span>
                        @if (podeCotar(s)) { <button type="button" class="link-btn" (click)="removerCotacao(c)" [attr.aria-label]="'Remover cotação ' + fonteRot(c)">Remover</button> }</li>
                    }
                  </ul>
                  <span class="cell-sub">{{ i.cotacoes.length }} cotação(ões) · {{ i.precoReferencia != null ? 'preço de referência' : 'mediana' }} <span class="mono">{{ moeda(i.precoReferencia ?? i.mediana ?? 0) }}</span>@if (i.valorEstimado != null) { · total {{ moeda(i.valorEstimado) }} }</span>
                  @if (i.justificativaCotacao) { <span class="cell-sub">Justificativa: {{ i.justificativaCotacao }}</span> }
                }
                @if (podeCotar(s) && i.cotacoes.length < 3) {
                  <div class="dw-field" style="margin: 0;"><label [for]="'f-cp-just-' + i.ordem">Justificativa para menos de 3 cotações</label><input [id]="'f-cp-just-' + i.ordem" type="text" maxlength="1000" [value]="f()['just-' + i.uuid] ?? ''" (input)="set('just-' + i.uuid, $event)" /></div>
                }
              </li>
            }
          </ul>

          @if (podeEditar(s)) {
            <div class="row-actions cp-acoes">
              <button type="button" class="btn btn--ghost btn--sm" (click)="editar(s)">Editar</button>
              <button type="button" class="btn btn--sm" (click)="executar(service.enviar(s.uuid))" [disabled]="enviando()">Enviar para análise</button>
            </div>
          }

          @if (s.situacao === 'ENVIADA' && pode(GERENCIAR)) {
            <h3 class="mo-grupo cp-titulo">Análise</h3>
            <div class="dw-field"><label for="f-cp-motivo">Motivo (para devolver ou rejeitar)</label><input id="f-cp-motivo" type="text" maxlength="1000" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)" /></div>
            <div class="row-actions">
              <button type="button" class="btn btn--ghost btn--sm" (click)="analisar(s, 'REJEITAR')" [disabled]="enviando()">Rejeitar</button>
              <button type="button" class="btn btn--ghost btn--sm" (click)="analisar(s, 'DEVOLVER')" [disabled]="enviando()">Devolver</button>
              <button type="button" class="btn btn--sm" (click)="analisar(s, 'APROVAR')" [disabled]="enviando()">Aprovar</button>
            </div>
          }

          @if (podeCotar(s)) {
            <fieldset class="cp-grupo"><legend>Nova cotação</legend>
              <div class="dw-row">
                <div class="dw-field" style="flex: 2;"><label for="f-cp-item">Item</label>
                  <select id="f-cp-item" (change)="set('itemId', $event)">
                    <option value="" [selected]="!f()['itemId']">Escolha…</option>
                    @for (i of s.itens; track i.uuid) { <option [value]="i.uuid" [selected]="f()['itemId'] === i.uuid">{{ i.ordem }}. {{ i.descricao }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-cp-fonte">Fonte</label>
                  <select id="f-cp-fonte" (change)="set('fonte', $event)">
                    @for (o of listaFontes; track o) { <option [value]="o" [selected]="f()['fonte'] === o">{{ fontes[o] }}</option> }
                  </select></div>
              </div>
              <div class="dw-row">
                @if (f()['fonte'] === 'FORNECEDOR') {
                  <div class="dw-field" style="flex: 2;"><label for="f-cp-fornecedor">Fornecedor</label>
                    <select id="f-cp-fornecedor" (change)="set('fornecedorId', $event)">
                      <option value="" [selected]="!f()['fornecedorId']">Escolha…</option>
                      @for (x of fornecedoresAptos(); track x.uuid) { <option [value]="x.uuid" [selected]="f()['fornecedorId'] === x.uuid">{{ x.razaoSocial }}</option> }
                    </select></div>
                } @else {
                  <div class="dw-field" style="flex: 2;"><label for="f-cp-referencia">Referência (contrato, site ou fonte)</label><input id="f-cp-referencia" type="text" maxlength="300" [value]="f()['referencia'] ?? ''" (input)="set('referencia', $event)" /></div>
                }
                <div class="dw-field"><label for="f-cp-preco">Preço unitário (R$)</label><input id="f-cp-preco" type="number" min="0" step="0.0001" [value]="f()['preco'] ?? ''" (input)="set('preco', $event)" /></div>
                <div class="dw-field"><label for="f-cp-data">Data</label><input id="f-cp-data" type="date" [value]="f()['cotadoEm'] ?? ''" (input)="set('cotadoEm', $event)" /></div>
              </div>
              <div class="row-actions"><button type="button" class="btn btn--sm" (click)="cotar()" [disabled]="enviando()">Registrar cotação</button></div>
            </fieldset>
            @if (s.situacao === 'EM_COTACAO') {
              <div class="row-actions cp-acoes"><button type="button" class="btn btn--sm" (click)="concluir(s)" [disabled]="enviando()">Concluir pesquisa de preços</button></div>
            }
          }

          @if (podeCancelar(s)) {
            <h3 class="mo-grupo cp-titulo">Cancelar</h3>
            <div class="cp-linha">
              <div class="dw-field" style="margin: 0; flex: 1;"><label for="f-cp-cancel">Motivo</label><input id="f-cp-cancel" type="text" maxlength="500" [value]="f()['cancelamento'] ?? ''" (input)="set('cancelamento', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="cancelar(s)" [disabled]="enviando()">Cancelar solicitação</button>
            </div>
          }
        </div>
      </app-drawer>
    }

    @if (cadastrando()) {
      <app-drawer titulo="Cadastrar fornecedor" (fechar)="cadastrando.set(false)">
        <form class="dw-form" (submit)="$event.preventDefault(); cadastrarFornecedor()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <div class="dw-row">
              <div class="dw-field"><label for="f-fo-doc">CPF ou CNPJ <span class="req" aria-hidden="true">*</span></label><input id="f-fo-doc" type="text" maxlength="20" [value]="f()['documento'] ?? ''" (input)="set('documento', $event)" /></div>
              <div class="dw-field" style="flex: 2;"><label for="f-fo-razao">Razão social ou nome <span class="req" aria-hidden="true">*</span></label><input id="f-fo-razao" type="text" maxlength="200" [value]="f()['razaoSocial'] ?? ''" (input)="set('razaoSocial', $event)" /></div>
            </div>
            <div class="dw-field"><label for="f-fo-fantasia">Nome fantasia<span class="opt">opcional</span></label><input id="f-fo-fantasia" type="text" maxlength="200" [value]="f()['nomeFantasia'] ?? ''" (input)="set('nomeFantasia', $event)" /></div>
            <div class="dw-row">
              <div class="dw-field"><label for="f-fo-email">E-mail<span class="opt">opcional</span></label><input id="f-fo-email" type="email" maxlength="150" [value]="f()['email'] ?? ''" (input)="set('email', $event)" /></div>
              <div class="dw-field"><label for="f-fo-tel">Telefone<span class="opt">opcional</span></label><input id="f-fo-tel" type="text" maxlength="30" [value]="f()['telefone'] ?? ''" (input)="set('telefone', $event)" /></div>
            </div>
            <div class="dw-field"><label for="f-fo-end">Endereço<span class="opt">opcional</span></label><input id="f-fo-end" type="text" maxlength="500" [value]="f()['endereco'] ?? ''" (input)="set('endereco', $event)" /></div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="cadastrando.set(false)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Cadastrar</button></div>
        </form>
      </app-drawer>
    }

    @if (fornecedor(); as fo) {
      <app-drawer [titulo]="fo.razaoSocial" (fechar)="fornecedor.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Situação</dt><dd>{{ rotuloFornecedor(fo) }}</dd></div>
            <div><dt>{{ fo.pessoaJuridica ? 'CNPJ' : 'CPF' }}</dt><dd class="mono">{{ documento(fo) }}</dd></div>
            @if (fo.nomeFantasia) { <div><dt>Nome fantasia</dt><dd>{{ fo.nomeFantasia }}</dd></div> }
            @if (fo.email || fo.telefone) { <div><dt>Contato</dt><dd>{{ fo.email }}@if (fo.email && fo.telefone) { · }{{ fo.telefone }}</dd></div> }
            @if (fo.endereco) { <div><dt>Endereço</dt><dd>{{ fo.endereco }}</dd></div> }
          </dl>

          <h3 class="mo-grupo cp-titulo">Certidões</h3>
          @if (fo.certidoesPendentes.length) { <p class="cell-sub cp-alerta">Sem certidão válida: {{ pendentes(fo) }}</p> }
          <ul class="cp-lista" aria-label="Certidões do fornecedor">
            @for (c of fo.certidoes; track c.uuid) {
              <li><span>{{ certidoes[c.tipo] }}@if (c.numero) { <span class="cell-sub">nº {{ c.numero }}</span> }</span><span [class]="'tag tag--' + (c.vencida ? 'alert' : 'ok')">{{ c.vencida ? 'Vencida em' : 'Válida até' }} {{ data(c.validaAte) }}</span></li>
            } @empty { <li class="cell-sub">Nenhuma certidão.</li> }
          </ul>
          @if (pode(GERENCIAR)) {
            <fieldset class="cp-grupo"><legend>Nova certidão</legend>
              <div class="dw-row">
                <div class="dw-field"><label for="f-ce-tipo">Tipo</label>
                  <select id="f-ce-tipo" (change)="set('certTipo', $event)">
                    @for (t of tiposCertidao(fo); track t) { <option [value]="t" [selected]="f()['certTipo'] === t">{{ certidoes[t] }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-ce-numero">Número<span class="opt">opcional</span></label><input id="f-ce-numero" type="text" maxlength="60" [value]="f()['certNumero'] ?? ''" (input)="set('certNumero', $event)" /></div>
              </div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-ce-emitida">Emitida em</label><input id="f-ce-emitida" type="date" [value]="f()['certEmitida'] ?? ''" (input)="set('certEmitida', $event)" /></div>
                <div class="dw-field"><label for="f-ce-validade">Válida até</label><input id="f-ce-validade" type="date" [value]="f()['certValidade'] ?? ''" (input)="set('certValidade', $event)" /></div>
              </div>
              <div class="row-actions"><button type="button" class="btn btn--sm" (click)="registrarCertidao(fo)" [disabled]="enviando()">Registrar certidão</button></div>
            </fieldset>
          }

          <h3 class="mo-grupo cp-titulo">Sanções</h3>
          <ul class="cp-lista" aria-label="Sanções do fornecedor">
            @for (x of fo.sancoes; track x.uuid) {
              <li>
                <span>{{ sancoes[x.tipo] }} <span class="cell-sub">desde {{ data(x.inicio) }}@if (x.fim) { até {{ data(x.fim) }} }@if (x.valor != null) { · {{ moeda(x.valor) }} }@if (x.processo) { · processo {{ x.processo }} } · {{ x.motivo }}@if (x.revogadaEm) { · revogada em {{ data(x.revogadaEm) }}: {{ x.motivoRevogacao }} }</span></span>
                <span>@if (x.vigente) { <span class="tag tag--alert">Vigente</span> } @if (x.vigente && pode(GERENCIAR)) { <button type="button" class="link-btn" (click)="revogar(x.uuid)" [attr.aria-label]="'Revogar ' + sancoes[x.tipo]">Revogar</button> }</span>
              </li>
            } @empty { <li class="cell-sub">Nenhuma sanção.</li> }
          </ul>
          @if (pode(GERENCIAR)) {
            <fieldset class="cp-grupo"><legend>Nova sanção (art. 156 da Lei 14.133/2021)</legend>
              <div class="dw-row">
                <div class="dw-field" style="flex: 2;"><label for="f-sa-tipo">Tipo</label>
                  <select id="f-sa-tipo" (change)="set('sanTipo', $event)">
                    @for (t of listaSancoes; track t) { <option [value]="t" [selected]="f()['sanTipo'] === t">{{ sancoes[t] }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-sa-processo">Processo<span class="opt">opcional</span></label><input id="f-sa-processo" type="text" maxlength="60" [value]="f()['sanProcesso'] ?? ''" (input)="set('sanProcesso', $event)" /></div>
              </div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-sa-inicio">Início</label><input id="f-sa-inicio" type="date" [value]="f()['sanInicio'] ?? ''" (input)="set('sanInicio', $event)" /></div>
                @if (f()['sanTipo'] === 'IMPEDIMENTO' || f()['sanTipo'] === 'INIDONEIDADE') {
                  <div class="dw-field"><label for="f-sa-fim">Fim</label><input id="f-sa-fim" type="date" [value]="f()['sanFim'] ?? ''" (input)="set('sanFim', $event)" /></div>
                }
                @if (f()['sanTipo'] === 'MULTA') {
                  <div class="dw-field"><label for="f-sa-valor">Valor (R$)</label><input id="f-sa-valor" type="number" min="0" step="0.01" [value]="f()['sanValor'] ?? ''" (input)="set('sanValor', $event)" /></div>
                }
              </div>
              <div class="dw-field"><label for="f-sa-motivo">Motivo</label><input id="f-sa-motivo" type="text" maxlength="1000" [value]="f()['sanMotivo'] ?? ''" (input)="set('sanMotivo', $event)" /></div>
              <div class="row-actions"><button type="button" class="btn btn--sm" (click)="registrarSancao(fo)" [disabled]="enviando()">Registrar sanção</button></div>
            </fieldset>
            <div class="row-actions cp-acoes"><button type="button" class="btn btn--ghost btn--sm" (click)="alternarAtivo(fo)" [disabled]="enviando()">{{ fo.ativo ? 'Desativar fornecedor' : 'Reativar fornecedor' }}</button></div>
          }
        </div>
      </app-drawer>
    }
  `,
  styles: `
    .cp-cab { display: flex; gap: var(--s-3); align-items: flex-end; margin-bottom: var(--s-2); flex-wrap: wrap; }
    .cp-tabela { min-width: 0; width: 100%; }
    .cp-titulo { margin-top: var(--s-4); }
    .cp-acoes { margin-top: var(--s-3); }
    .cp-grupo { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); margin: var(--s-3) 0; display: grid; gap: var(--s-1); }
    .cp-itens { list-style: none; margin: 0; padding: 0; display: grid; gap: var(--s-2); }
    .cp-itens > li { border-left: 3px solid var(--ink-200); padding-left: var(--s-2); display: grid; gap: var(--s-1); }
    .cp-cotacoes { list-style: none; margin: 0; padding: 0; display: grid; gap: 2px; }
    .cp-cotacoes li { display: flex; gap: var(--s-2); align-items: baseline; flex-wrap: wrap; }
    .cp-lista { margin: 0; padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .cp-lista li { display: flex; justify-content: space-between; gap: var(--s-2); align-items: baseline; }
    .cp-linha { display: flex; gap: var(--s-2); align-items: flex-end; flex-wrap: wrap; }
    .cp-alerta { color: var(--danger-600, #b42318); }
  `,
})
export class Compras {
  protected readonly service = inject(ComprasService);
  private readonly authService = inject(AuthService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly setorService = inject(SetorService);
  private readonly estoqueService = inject(EstoqueService);
  private readonly medicamentoService = inject(MedicamentoService);

  protected readonly SOLICITAR = SOLICITAR;
  protected readonly GERENCIAR = GERENCIAR;
  protected readonly situacoes = SITUACOES_SOLICITACAO;
  protected readonly listaSituacoes = Object.keys(SITUACOES_SOLICITACAO) as SituacaoSolicitacaoCompra[];
  protected readonly tipos = TIPOS_ITEM;
  protected readonly listaTipos = Object.keys(TIPOS_ITEM) as TipoItemCompra[];
  protected readonly fontes = FONTES_COTACAO;
  protected readonly listaFontes = Object.keys(FONTES_COTACAO) as FonteCotacao[];
  protected readonly certidoes = CERTIDOES;
  protected readonly sancoes = SANCOES;
  protected readonly listaSancoes = Object.keys(SANCOES) as TipoSancaoFornecedor[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('solicitacoes');
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly situacao = signal('');
  protected readonly busca = signal('');
  protected readonly solicitacoes = signal<SolicitacaoCompraDto[]>([]);
  protected readonly fornecedores = signal<FornecedorDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  private readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly materiais = signal<MaterialEstoqueDto[]>([]);
  protected readonly medicamentos = signal<MedicamentoResponseDto[]>([]);
  protected readonly editando = signal(false);
  protected readonly editandoId = signal<string | null>(null);
  protected readonly linhas = signal<LinhaItem[]>([]);
  protected readonly detalhe = signal<SolicitacaoCompraDto | null>(null);
  protected readonly cadastrando = signal(false);
  protected readonly fornecedor = signal<FornecedorDto | null>(null);

  protected readonly setoresDaUnidade = computed(() => this.setores().filter((s) => s.unidadeUuid === this.f()['unidadeId']));
  protected readonly fornecedoresAptos = computed(() => this.fornecedores().filter((x) => x.ativo && !x.impedido));
  /** O que espera a pessoa: para quem compra, as enviadas e as em cotação; para quem pede, as devolvidas. */
  protected readonly paraAgir = computed(() => this.solicitacoes().filter((s) =>
    (this.pode(GERENCIAR) && (s.situacao === 'ENVIADA' || s.situacao === 'APROVADA' || s.situacao === 'EM_COTACAO'))
    || (this.pode(SOLICITAR) && s.situacao === 'DEVOLVIDA')).length);

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      if (!this.compraOuPede()) {
        this.aba.set('contratos');
        return;
      }
      this.carregar();
      if (this.pode(GERENCIAR)) this.carregarFornecedores();
    });
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidades.set(l));
    this.setorService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.setores.set(l));
    this.estoqueService.materiais(true).pipe(catchError(() => of([]))).subscribe((l) => this.materiais.set(l));
    this.medicamentoService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.medicamentos.set(l.filter((m) => m.ativo)));
  }

  /** Quem pede ou compra; o gestor e o fiscal sem essas permissões veem só a aba Contratos. */
  protected compraOuPede(): boolean {
    return this.pode(SOLICITAR) || this.pode(GERENCIAR);
  }

  protected pode(permissao: string): boolean {
    const a = this.acesso();
    return !a.restrito || a.permissoes.has(permissao);
  }

  protected carregar(): void {
    this.service.solicitacoes((this.situacao() || undefined) as SituacaoSolicitacaoCompra | undefined).subscribe({
      next: (l) => {
        this.solicitacoes.set(l);
        this.erro.set(null);
      },
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  protected abrirFornecedores(): void {
    this.aba.set('fornecedores');
    this.carregarFornecedores();
  }

  protected carregarFornecedores(): void {
    this.service.fornecedores(this.busca().trim() || undefined).subscribe({
      next: (l) => this.fornecedores.set(l),
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  // ── Solicitação ──

  protected novaSolicitacao(): void {
    this.fecharGavetas();
    const unidade = this.unidades().length === 1 ? this.unidades()[0].uuid : '';
    this.f.set({ unidadeId: unidade, prioridade: 'NORMAL' });
    this.linhas.set([linhaVazia()]);
    this.editandoId.set(null);
    this.editando.set(true);
  }

  protected editar(s: SolicitacaoCompraDto): void {
    this.fecharGavetas();
    this.f.set({ unidadeId: s.unidadeId, setorId: s.setorId ?? '', prioridade: s.prioridade, justificativa: s.justificativa });
    this.linhas.set(s.itens.map((i) => ({ tipo: i.tipo, materialId: i.materialId ?? '', medicamentoId: i.medicamentoId ?? '',
      descricao: i.tipo === 'SERVICO' ? i.descricao : '', unidadeMedida: i.tipo === 'SERVICO' ? i.unidadeMedida.toLowerCase() : '', quantidade: String(i.quantidade) })));
    this.editandoId.set(s.uuid);
    this.editando.set(true);
  }

  protected adicionarLinha(): void {
    this.linhas.set([...this.linhas(), linhaVazia()]);
  }

  protected removerLinha(i: number): void {
    this.linhas.set(this.linhas().filter((_, k) => k !== i));
  }

  protected mudarLinha(i: number, campo: keyof LinhaItem, ev: Event): void {
    const valor = (ev.target as HTMLInputElement).value;
    this.linhas.set(this.linhas().map((l, k) => (k === i ? { ...l, [campo]: valor } : l)));
  }

  /** Acrescenta os materiais abaixo do mínimo que ainda não estão na lista. */
  protected sugerir(): void {
    this.aviso.set(null);
    this.service.sugestao(this.f()['unidadeId']).subscribe({
      next: (sug) => {
        const ja = new Set(this.linhas().map((l) => l.materialId).filter((x) => x));
        const novas = sug.filter((x) => !ja.has(x.materialId)).map((x) => ({ ...linhaVazia(), materialId: x.materialId, quantidade: String(x.quantidadeSugerida) }));
        const vazias = this.linhas().filter((l) => !(l.tipo === 'MATERIAL' && !l.materialId));
        this.linhas.set([...vazias, ...novas]);
        this.aviso.set(novas.length ? `${novas.length} material(is) abaixo do mínimo acrescentado(s).` : 'Nenhum material abaixo do mínimo fora de solicitação.');
      },
      error: (x: HttpErrorResponse) => this.erroGaveta.set(mensagem(x)),
    });
  }

  protected salvarSolicitacao(): void {
    const c = this.f();
    const justificativa = (c['justificativa'] ?? '').trim();
    if (!c['unidadeId'] || !justificativa) {
      this.erroGaveta.set('Informe a unidade e a justificativa.');
      return;
    }
    const itens: ItemCompraRequest[] = [];
    for (const l of this.linhas()) {
      const quantidade = Number(l.quantidade);
      const sem = (l.tipo === 'MATERIAL' && !l.materialId) || (l.tipo === 'MEDICAMENTO' && !l.medicamentoId)
        || (l.tipo === 'SERVICO' && (!l.descricao.trim() || !l.unidadeMedida.trim()));
      if (sem || !(quantidade >= 1)) {
        this.erroGaveta.set('Complete cada item: o produto (ou o serviço e a unidade de medida) e a quantidade.');
        return;
      }
      itens.push({ tipo: l.tipo, materialId: l.tipo === 'MATERIAL' ? l.materialId : null, medicamentoId: l.tipo === 'MEDICAMENTO' ? l.medicamentoId : null,
        descricao: l.tipo === 'SERVICO' ? l.descricao.trim() : null, unidadeMedida: l.tipo === 'SERVICO' ? l.unidadeMedida.trim() : null, quantidade });
    }
    if (!itens.length) {
      this.erroGaveta.set('Informe ao menos um item.');
      return;
    }
    const dto = { unidadeId: c['unidadeId'], setorId: c['setorId'] || null, justificativa, prioridade: (c['prioridade'] === 'URGENTE' ? 'URGENTE' : 'NORMAL') as 'URGENTE' | 'NORMAL', itens };
    const id = this.editandoId();
    this.executar(id ? this.service.atualizar(id, dto) : this.service.criar(dto));
  }

  protected abrirSolicitacao(id: string): void {
    this.service.solicitacao(id).subscribe({ next: (s) => this.mostrar(s), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  private mostrar(s: SolicitacaoCompraDto): void {
    this.fecharGavetas();
    this.f.set({ fonte: 'FORNECEDOR', cotadoEm: hojeIso(), itemId: s.itens.length === 1 ? s.itens[0].uuid : '' });
    this.detalhe.set(s);
    if (this.podeCotar(s) && !this.fornecedores().length) this.carregarFornecedores();
  }

  protected podeEditar(s: SolicitacaoCompraDto): boolean {
    return (s.situacao === 'RASCUNHO' || s.situacao === 'DEVOLVIDA') && this.pode(SOLICITAR);
  }

  protected podeCotar(s: SolicitacaoCompraDto): boolean {
    return (s.situacao === 'APROVADA' || s.situacao === 'EM_COTACAO') && this.pode(GERENCIAR);
  }

  protected podeCancelar(s: SolicitacaoCompraDto): boolean {
    if (s.situacao === 'REJEITADA' || s.situacao === 'COTADA' || s.situacao === 'CANCELADA') return false;
    return s.situacao === 'APROVADA' || s.situacao === 'EM_COTACAO' ? this.pode(GERENCIAR) : this.pode(SOLICITAR) || this.pode(GERENCIAR);
  }

  protected analisar(s: SolicitacaoCompraDto, decisao: 'APROVAR' | 'DEVOLVER' | 'REJEITAR'): void {
    const motivo = (this.f()['motivo'] ?? '').trim();
    if (decisao !== 'APROVAR' && !motivo) {
      this.erroGaveta.set('Devolver ou rejeitar pede o motivo.');
      return;
    }
    this.executar(this.service.analisar(s.uuid, decisao, motivo || null));
  }

  protected cotar(): void {
    const c = this.f();
    const preco = Number(c['preco']);
    const doFornecedor = c['fonte'] === 'FORNECEDOR';
    if (!c['itemId'] || !(preco > 0) || !c['cotadoEm'] || (doFornecedor ? !c['fornecedorId'] : !(c['referencia'] ?? '').trim())) {
      this.erroGaveta.set(doFornecedor ? 'Informe o item, o fornecedor, o preço e a data.' : 'Informe o item, a referência, o preço e a data.');
      return;
    }
    this.executar(this.service.cotar(c['itemId'], { fonte: c['fonte'] as FonteCotacao, fornecedorId: doFornecedor ? c['fornecedorId'] : null,
      referencia: doFornecedor ? null : c['referencia'].trim(), precoUnitario: preco, cotadoEm: c['cotadoEm'] }), true);
  }

  protected removerCotacao(c: CotacaoDto): void {
    this.executar(this.service.removerCotacao(c.uuid), true);
  }

  protected concluir(s: SolicitacaoCompraDto): void {
    const justificativas = s.itens.filter((i) => i.cotacoes.length < 3)
      .map((i) => ({ itemId: i.uuid, justificativa: (this.f()['just-' + i.uuid] ?? '').trim() })).filter((j) => j.justificativa);
    this.executar(this.service.concluirPesquisa(s.uuid, justificativas));
  }

  protected cancelar(s: SolicitacaoCompraDto): void {
    const motivo = (this.f()['cancelamento'] ?? '').trim();
    if (!motivo) {
      this.erroGaveta.set('Informe o motivo do cancelamento.');
      return;
    }
    this.executar(this.service.cancelar(s.uuid, motivo));
  }

  /** Executa e mostra a solicitação devolvida; {@code manter} preserva o formulário de cotação (item e fonte). */
  protected executar(req: Observable<SolicitacaoCompraDto>, manter = false): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    req.subscribe({
      next: (s) => {
        this.enviando.set(false);
        const antes = this.f();
        this.mostrar(s);
        if (manter) this.f.set({ ...this.f(), itemId: antes['itemId'] ?? '', fonte: antes['fonte'] ?? 'FORNECEDOR', cotadoEm: antes['cotadoEm'] ?? hojeIso() });
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  // ── Fornecedor ──

  protected novoFornecedor(): void {
    this.fecharGavetas();
    this.f.set({});
    this.cadastrando.set(true);
  }

  protected cadastrarFornecedor(): void {
    const c = this.f();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    if (!t('documento') || !t('razaoSocial')) {
      this.erroGaveta.set('Informe o CPF ou o CNPJ e a razão social.');
      return;
    }
    this.executarFornecedor(this.service.cadastrarFornecedor({ documento: t('documento')!, razaoSocial: t('razaoSocial')!, nomeFantasia: t('nomeFantasia'),
      email: t('email'), telefone: t('telefone'), endereco: t('endereco') }));
  }

  protected abrirFornecedor(id: string): void {
    this.service.fornecedor(id).subscribe({ next: (fo) => this.mostrarFornecedor(fo), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  private mostrarFornecedor(fo: FornecedorDto): void {
    this.fecharGavetas();
    this.f.set({ certTipo: fo.certidoesPendentes[0] ?? 'FEDERAL', certEmitida: hojeIso(), sanTipo: 'ADVERTENCIA', sanInicio: hojeIso() });
    this.fornecedor.set(fo);
  }

  protected tiposCertidao(fo: FornecedorDto): TipoCertidaoFornecedor[] {
    return (Object.keys(CERTIDOES) as TipoCertidaoFornecedor[]).filter((t) => fo.pessoaJuridica || t !== 'FGTS');
  }

  protected registrarCertidao(fo: FornecedorDto): void {
    const c = this.f();
    if (!c['certEmitida'] || !c['certValidade']) {
      this.erroGaveta.set('Informe a emissão e a validade.');
      return;
    }
    this.executarFornecedor(this.service.certidao(fo.uuid, { tipo: c['certTipo'] as TipoCertidaoFornecedor, numero: (c['certNumero'] ?? '').trim() || null,
      emitidaEm: c['certEmitida'], validaAte: c['certValidade'] }));
  }

  protected registrarSancao(fo: FornecedorDto): void {
    const c = this.f();
    const tipo = c['sanTipo'] as TipoSancaoFornecedor;
    const comFim = tipo === 'IMPEDIMENTO' || tipo === 'INIDONEIDADE';
    const motivo = (c['sanMotivo'] ?? '').trim();
    if (!c['sanInicio'] || !motivo || (comFim && !c['sanFim']) || (tipo === 'MULTA' && !(Number(c['sanValor']) > 0))) {
      this.erroGaveta.set('Informe o início, o motivo e, conforme o tipo, o fim ou o valor.');
      return;
    }
    this.executarFornecedor(this.service.sancao(fo.uuid, { tipo, inicio: c['sanInicio'], fim: comFim ? c['sanFim'] : null,
      valor: tipo === 'MULTA' ? Number(c['sanValor']) : null, motivo, processo: (c['sanProcesso'] ?? '').trim() || null }));
  }

  protected revogar(sancaoId: string): void {
    this.executarFornecedor(this.service.revogarSancao(sancaoId, 'Revogada pelo setor de compras.'));
  }

  protected alternarAtivo(fo: FornecedorDto): void {
    this.executarFornecedor(this.service.atualizarFornecedor(fo.uuid, { documento: fo.documento, razaoSocial: fo.razaoSocial, nomeFantasia: fo.nomeFantasia,
      email: fo.email, telefone: fo.telefone, endereco: fo.endereco, ativo: !fo.ativo }));
  }

  private executarFornecedor(req: Observable<FornecedorDto>): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    req.subscribe({
      next: (fo) => {
        this.enviando.set(false);
        this.mostrarFornecedor(fo);
        this.carregarFornecedores();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  private fecharGavetas(): void {
    this.erroGaveta.set(null);
    this.aviso.set(null);
    this.editando.set(false);
    this.detalhe.set(null);
    this.cadastrando.set(false);
    this.fornecedor.set(null);
  }

  // ── Apoio de exibição ──

  protected resumo(s: SolicitacaoCompraDto): string {
    const nomes = s.itens.slice(0, 2).map((i) => i.descricao).join('; ');
    return s.itens.length > 2 ? `${nomes}; …` : nomes;
  }

  protected tagSituacao(s: SituacaoSolicitacaoCompra): string {
    switch (s) {
      case 'COTADA':
      case 'APROVADA':
        return 'ok';
      case 'REJEITADA':
      case 'DEVOLVIDA':
        return 'alert';
      case 'CANCELADA':
      case 'RASCUNHO':
        return '';
      default:
        return 'warn';
    }
  }

  protected rotuloFornecedor(f: FornecedorDto): string {
    if (!f.ativo) return 'Inativo';
    if (f.impedido) return 'Impedido';
    return f.certidoesPendentes.length ? 'Certidão pendente' : 'Regular';
  }

  protected tagFornecedor(f: FornecedorDto): string {
    if (!f.ativo) return '';
    if (f.impedido) return 'alert';
    return f.certidoesPendentes.length ? 'warn' : 'ok';
  }

  protected pendentes(f: FornecedorDto): string {
    return f.certidoesPendentes.map((t) => CERTIDOES[t]).join(', ');
  }

  protected documento(f: FornecedorDto): string {
    const d = f.documento;
    if (!/^\d+$/.test(d)) return d;
    return d.length === 14 ? `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8, 12)}-${d.slice(12)}` : `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
  }

  protected fonteRot(c: CotacaoDto): string {
    return c.fornecedorNome ?? `${FONTES_COTACAO[c.fonte]}: ${c.referencia ?? ''}`;
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', minimumFractionDigits: 2, maximumFractionDigits: 4 });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}

function linhaVazia(): LinhaItem {
  return { tipo: 'MATERIAL', materialId: '', medicamentoId: '', descricao: '', unidadeMedida: '', quantidade: '1' };
}

function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}

