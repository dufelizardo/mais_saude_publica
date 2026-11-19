import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import {
  ContratacaoDto,
  FornecedorDto,
  InstrumentoDto,
  ItemContratacaoDto,
  MODALIDADES,
  ModalidadeContratacao,
  RESULTADOS_ITEM,
  ResultadoItemContratacao,
  SITUACOES_CONTRATACAO,
  SolicitacaoCompraDto,
  TIPOS_ADITIVO,
  TIPOS_INSTRUMENTO,
  TipoAditivoContratual,
  TipoInstrumentoContratual,
  OrdemDto,
  RecebimentoDto,
  SITUACOES_ORDEM,
  TIPOS_OCORRENCIA,
  TipoOcorrenciaFornecedor,
} from '../../../core/models/compras';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AuthService } from '../../../core/services/auth';
import { ComprasService } from '../../../core/services/compras';
import { Drawer } from '../../../shared/drawer/drawer';

const GERENCIAR = 'COMPRAS.GERENCIAR';

/**
 * Compras, fatia CP2 (ADR-0213): a aba Contratações (abrir das solicitações cotadas, resultado por item, homologação com a ata ou o
 * contrato de cada vencedor) e a aba Contratos (atas e contratos com saldo, aditivos e alertas; quem não compra vê só os que fiscaliza
 * ou gere). A gaveta do contrato traz as ordens de fornecimento, e a da ordem, o recebimento e as ocorrências (CP3, ADR-0214).
 */
@Component({
  selector: 'app-contratos-compras',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    @if (aba() === 'contratacoes') {
      <div class="ct-cab">
        <button type="button" class="btn btn--sm" (click)="novaContratacao()">Nova contratação</button>
      </div>
      <div class="tbl-wrap card">
        <table class="ct-tabela" aria-label="Contratações">
          <thead><tr><th scope="col">Processo</th><th scope="col">Modalidade</th><th scope="col">Valor</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (c of contratacoes(); track c.uuid) {
              <tr>
                <td><span class="cell-main mono">{{ c.numeroProcesso }}</span><span class="cell-sub">{{ c.objeto }}</span></td>
                <td>{{ modalidades[c.modalidade] }}</td>
                <td class="mono">{{ moeda(c.valorAdjudicado || c.valorReferencia) }}<span class="cell-sub">{{ c.valorAdjudicado ? 'adjudicado' : 'referência' }}</span></td>
                <td><span [class]="'tag tag--' + (c.situacao === 'HOMOLOGADA' ? 'ok' : c.situacao === 'CANCELADA' ? '' : 'warn')">{{ situacoes[c.situacao] }}</span></td>
                <td class="row-actions"><button type="button" class="link-btn" (click)="abrirContratacao(c.uuid)" [attr.aria-label]="'Abrir contratação ' + c.numeroProcesso">Abrir</button></td>
              </tr>
            } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma contratação.</td></tr> }
          </tbody>
        </table>
      </div>
    } @else {
      <div class="tbl-wrap card">
        <table class="ct-tabela" aria-label="Atas e contratos">
          <thead><tr><th scope="col">Número</th><th scope="col">Fornecedor</th><th scope="col">Vigência</th><th scope="col">Valor atual</th><th scope="col">Alertas</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (i of instrumentos(); track i.uuid) {
              <tr>
                <td><span class="cell-main mono">{{ i.numero }}</span><span class="cell-sub">{{ tiposInstrumento[i.tipo] }}</span></td>
                <td>{{ i.fornecedorNome }}<span class="cell-sub">gestor {{ i.gestorNome }} · fiscal {{ i.fiscalNome }}</span></td>
                <td class="mono">{{ data(i.inicio) }} a {{ data(i.fim) }}</td>
                <td class="mono">{{ moeda(i.valorAtual) }}</td>
                <td>@if (i.alertas.length) { <span class="tag tag--warn">{{ i.alertas.length }} alerta(s)</span> } @else { <span class="cell-sub">—</span> }</td>
                <td class="row-actions"><button type="button" class="link-btn" (click)="abrirInstrumento(i.uuid)" [attr.aria-label]="'Abrir ' + i.numero">Abrir</button></td>
              </tr>
            } @empty { <tr><td colspan="6" class="cell-sub">{{ gerencia() ? 'Nenhuma ata nem contrato.' : 'Nenhum contrato em que você seja gestor ou fiscal.' }}</td></tr> }
          </tbody>
        </table>
      </div>
    }

    @if (abrindo()) {
      <app-drawer titulo="Nova contratação" (fechar)="abrindo.set(false)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvarContratacao()" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            <div class="dw-row">
              <div class="dw-field"><label for="f-ct-processo">Número do processo <span class="req" aria-hidden="true">*</span></label><input id="f-ct-processo" type="text" maxlength="60" [value]="f()['processo'] ?? ''" (input)="set('processo', $event)" /></div>
              <div class="dw-field"><label for="f-ct-modalidade">Modalidade</label>
                <select id="f-ct-modalidade" (change)="set('modalidade', $event)">
                  @for (m of listaModalidades; track m) { <option [value]="m" [selected]="f()['modalidade'] === m">{{ modalidades[m] }}</option> }
                </select></div>
            </div>
            @if (f()['modalidade'] === 'DISPENSA' || f()['modalidade'] === 'INEXIGIBILIDADE') {
              <div class="dw-field"><label for="f-ct-fundamento">Fundamento legal <span class="req" aria-hidden="true">*</span></label><input id="f-ct-fundamento" type="text" maxlength="300" placeholder="Art. 75, II" [value]="f()['fundamento'] ?? ''" (input)="set('fundamento', $event)" /></div>
            }
            @if (f()['modalidade'] === 'ADESAO_ATA') {
              <div class="dw-field"><label for="f-ct-ata">Ata de origem <span class="req" aria-hidden="true">*</span></label><input id="f-ct-ata" type="text" maxlength="200" placeholder="Órgão gerenciador e número da ata" [value]="f()['ata'] ?? ''" (input)="set('ata', $event)" /></div>
            }
            <div class="dw-field"><label for="f-ct-objeto">Objeto <span class="req" aria-hidden="true">*</span></label><input id="f-ct-objeto" type="text" maxlength="1000" [value]="f()['objeto'] ?? ''" (input)="set('objeto', $event)" /></div>
            <fieldset class="ct-grupo"><legend>Solicitações cotadas</legend>
              @for (s of cotadas(); track s.uuid) {
                <label class="chk"><input type="checkbox" [checked]="escolhidas().has(s.uuid)" (change)="alternar(s.uuid)" [attr.aria-label]="'Incluir ' + s.numero" /> <span class="mono">{{ s.numero }}</span> {{ s.unidadeNome }} · {{ moeda(s.valorEstimado ?? 0) }}</label>
              } @empty { <p class="cell-sub">Nenhuma solicitação cotada fora de contratação.</p> }
            </fieldset>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="abrindo.set(false)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">Abrir contratação</button></div>
        </form>
      </app-drawer>
    }

    @if (contratacao(); as c) {
      <app-drawer [titulo]="'Contratação ' + c.numeroProcesso" (fechar)="contratacao.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Situação</dt><dd>{{ situacoes[c.situacao] }}@if (c.homologadaEm) { em {{ data(c.homologadaEm) }} }@if (c.motivoCancelamento) { · {{ c.motivoCancelamento }} }</dd></div>
            <div><dt>Modalidade</dt><dd>{{ modalidades[c.modalidade] }}@if (c.fundamentoLegal) { · {{ c.fundamentoLegal }} }@if (c.ataOrigem) { · {{ c.ataOrigem }} }</dd></div>
            <div><dt>Objeto</dt><dd>{{ c.objeto }}</dd></div>
            <div><dt>Solicitações</dt><dd>@for (s of c.solicitacoes; track s.uuid) { <span class="mono">{{ s.numero }}</span> ({{ s.unidadeNome }}) }</dd></div>
            <div><dt>Valor</dt><dd>referência {{ moeda(c.valorReferencia) }} · adjudicado {{ moeda(c.valorAdjudicado) }}</dd></div>
          </dl>

          <h3 class="mo-grupo ct-titulo">Itens</h3>
          <ul class="ct-itens" aria-label="Itens da contratação">
            @for (i of c.itens; track i.uuid) {
              <li>
                <span><strong>{{ i.ordem }}. {{ i.descricao }}</strong> <span class="cell-sub">{{ i.quantidade }} {{ i.unidadeMedida.toLowerCase() }} · referência {{ moeda(i.precoReferencia ?? 0) }}</span></span>
                <span><span [class]="'tag tag--' + (i.resultado === 'ADJUDICADO' ? 'ok' : i.resultado === 'PENDENTE' ? 'warn' : 'alert')">{{ resultados[i.resultado] }}</span>@if (i.fornecedorNome) { {{ i.fornecedorNome }} a {{ moeda(i.precoUnitario ?? 0) }} }@if (i.justificativaPreco) { <span class="cell-sub">{{ i.justificativaPreco }}</span> }</span>
                @if (c.situacao === 'EM_ANDAMENTO') {
                  <div class="dw-row">
                    <div class="dw-field"><label [for]="'f-ct-res-' + i.ordem">Resultado</label>
                      <select [id]="'f-ct-res-' + i.ordem" (change)="set('res-' + i.uuid, $event)">
                        @for (r of listaResultados; track r) { <option [value]="r" [selected]="(f()['res-' + i.uuid] ?? 'ADJUDICADO') === r">{{ resultados[r] }}</option> }
                      </select></div>
                    @if ((f()['res-' + i.uuid] ?? 'ADJUDICADO') === 'ADJUDICADO') {
                      <div class="dw-field" style="flex: 2;"><label [for]="'f-ct-forn-' + i.ordem">Fornecedor</label>
                        <select [id]="'f-ct-forn-' + i.ordem" (change)="set('forn-' + i.uuid, $event)">
                          <option value="">Escolha…</option>
                          @for (x of regulares(); track x.uuid) { <option [value]="x.uuid" [selected]="f()['forn-' + i.uuid] === x.uuid">{{ x.razaoSocial }}</option> }
                        </select></div>
                      <div class="dw-field"><label [for]="'f-ct-preco-' + i.ordem">Preço unitário</label><input [id]="'f-ct-preco-' + i.ordem" type="number" min="0" step="0.0001" [value]="f()['preco-' + i.uuid] ?? ''" (input)="set('preco-' + i.uuid, $event)" /></div>
                    }
                  </div>
                  @if ((f()['res-' + i.uuid] ?? 'ADJUDICADO') === 'ADJUDICADO' && acimaDaReferencia(i)) {
                    <div class="dw-field" style="margin: 0;"><label [for]="'f-ct-just-' + i.ordem">Justificativa do preço acima da referência</label><input [id]="'f-ct-just-' + i.ordem" type="text" maxlength="1000" [value]="f()['just-' + i.uuid] ?? ''" (input)="set('just-' + i.uuid, $event)" /></div>
                  }
                  <div class="row-actions"><button type="button" class="link-btn" (click)="registrarResultado(i)" [attr.aria-label]="'Registrar resultado do item ' + i.ordem">Registrar resultado</button></div>
                }
              </li>
            }
          </ul>

          @if (c.situacao === 'EM_ANDAMENTO') {
            @if (vencedores(c).length) {
              <h3 class="mo-grupo ct-titulo">Homologação</h3>
              @for (v of vencedores(c); track v.id; let k = $index) {
                <fieldset class="ct-grupo"><legend>{{ v.nome }}</legend>
                  <div class="dw-row">
                    <div class="dw-field"><label [for]="'f-ho-tipo-' + k">Instrumento</label>
                      <select [id]="'f-ho-tipo-' + k" (change)="set('tipo-' + v.id, $event)">
                        @for (t of listaInstrumentos; track t) { <option [value]="t" [selected]="(f()['tipo-' + v.id] ?? 'ATA_REGISTRO_PRECOS') === t">{{ tiposInstrumento[t] }}</option> }
                      </select></div>
                    <div class="dw-field"><label [for]="'f-ho-numero-' + k">Número</label><input [id]="'f-ho-numero-' + k" type="text" maxlength="60" [value]="f()['numero-' + v.id] ?? ''" (input)="set('numero-' + v.id, $event)" /></div>
                  </div>
                  <div class="dw-row">
                    <div class="dw-field"><label [for]="'f-ho-inicio-' + k">Início</label><input [id]="'f-ho-inicio-' + k" type="date" [value]="f()['inicio-' + v.id] ?? ''" (input)="set('inicio-' + v.id, $event)" /></div>
                    <div class="dw-field"><label [for]="'f-ho-fim-' + k">Fim</label><input [id]="'f-ho-fim-' + k" type="date" [value]="f()['fim-' + v.id] ?? ''" (input)="set('fim-' + v.id, $event)" /></div>
                    @if ((f()['tipo-' + v.id] ?? 'ATA_REGISTRO_PRECOS') === 'CONTRATO') {
                      <label class="chk"><input type="checkbox" [checked]="f()['continuo-' + v.id] === 'sim'" (change)="alternarContinuo(v.id)" /> Serviço contínuo</label>
                    }
                  </div>
                  <div class="dw-row">
                    <div class="dw-field"><label [for]="'f-ho-gestor-' + k">Matrícula do gestor</label><input [id]="'f-ho-gestor-' + k" type="text" [value]="f()['gestor-' + v.id] ?? ''" (input)="set('gestor-' + v.id, $event)" /></div>
                    <div class="dw-field"><label [for]="'f-ho-fiscal-' + k">Matrícula do fiscal</label><input [id]="'f-ho-fiscal-' + k" type="text" [value]="f()['fiscal-' + v.id] ?? ''" (input)="set('fiscal-' + v.id, $event)" /></div>
                  </div>
                </fieldset>
              }
            }
            <div class="row-actions ct-acoes"><button type="button" class="btn btn--sm" (click)="homologar(c)" [disabled]="enviando()">Homologar</button></div>
            <h3 class="mo-grupo ct-titulo">Cancelar</h3>
            <div class="ct-linha">
              <div class="dw-field" style="margin: 0; flex: 1;"><label for="f-ct-cancel">Motivo</label><input id="f-ct-cancel" type="text" maxlength="500" [value]="f()['cancelamento'] ?? ''" (input)="set('cancelamento', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="cancelar(c)" [disabled]="enviando()">Cancelar contratação</button>
            </div>
          }
          @if (c.instrumentos.length) {
            <h3 class="mo-grupo ct-titulo">Atas e contratos gerados</h3>
            <ul class="ct-lista">@for (x of c.instrumentos; track x.uuid) { <li><button type="button" class="link-btn" (click)="abrirInstrumento(x.uuid)">{{ tiposInstrumento[x.tipo] }} {{ x.numero }}</button> <span class="cell-sub">{{ x.fornecedorNome }}</span></li> }</ul>
          }
        </div>
      </app-drawer>
    }

    @if (instrumento(); as i) {
      <app-drawer [titulo]="tiposInstrumento[i.tipo] + ' ' + i.numero" (fechar)="instrumento.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          @if (i.alertas.length) {
            <div class="box box--warn" role="status" aria-label="Alertas do contrato"><ul class="ct-lista">@for (a of i.alertas; track a) { <li>{{ a }}</li> }</ul></div>
          }
          <dl class="ro">
            <div><dt>Fornecedor</dt><dd>{{ i.fornecedorNome }}</dd></div>
            <div><dt>Vigência</dt><dd>{{ data(i.inicio) }} a {{ data(i.fim) }}@if (i.servicoContinuo) { · serviço contínuo }{{ i.vigente ? '' : ' · fora da vigência' }}</dd></div>
            <div><dt>Processo</dt><dd class="mono">{{ i.numeroProcesso }}</dd></div>
            <div><dt>Valor</dt><dd>inicial {{ moeda(i.valorInicial) }} · atual {{ moeda(i.valorAtual) }}@if (i.percentualAcrescido) { · acrescido {{ i.percentualAcrescido }}% }@if (i.percentualSuprimido) { · suprimido {{ i.percentualSuprimido }}% }</dd></div>
            <div><dt>Gestor e fiscal</dt><dd>{{ i.gestorNome }} ({{ i.gestorMatricula }}) · {{ i.fiscalNome }} ({{ i.fiscalMatricula }})</dd></div>
          </dl>
          <div class="tbl-wrap">
            <table class="ct-tabela" aria-label="Itens e saldos">
              <thead><tr><th scope="col">Item</th><th scope="col">Quantidade</th><th scope="col">Saldo</th><th scope="col">Preço</th></tr></thead>
              <tbody>
                @for (x of i.itens; track x.uuid) {
                  <tr><td>{{ x.descricao }}</td><td class="mono">{{ x.quantidade }}@if (x.quantidade !== x.quantidadeInicial) { <span class="cell-sub">inicial {{ x.quantidadeInicial }}</span> }</td><td class="mono">{{ x.saldo }}@if (x.saldoBaixo) { <span class="tag tag--warn">baixo</span> }</td><td class="mono">{{ moeda(x.precoUnitario) }}</td></tr>
                }
              </tbody>
            </table>
          </div>
          <h3 class="mo-grupo ct-titulo">Aditivos</h3>
          <ul class="ct-lista" aria-label="Aditivos">
            @for (a of i.aditivos; track a.uuid) {
              <li><span>{{ a.numero }}º · {{ tiposAditivo[a.tipo] }}@if (a.novoFim) { até {{ data(a.novoFim) }} }@if (a.quantidade) { · {{ a.quantidade }} de {{ a.itemDescricao }} }@if (a.novoPreco) { · {{ a.itemDescricao }} de {{ moeda(a.precoAnterior ?? 0) }} para {{ moeda(a.novoPreco) }} }</span><span class="cell-sub">{{ data(a.assinadoEm) }} · {{ a.justificativa }}</span></li>
            } @empty { <li class="cell-sub">Nenhum aditivo.</li> }
          </ul>
          @if (gerencia() && i.vigente) {
            <fieldset class="ct-grupo"><legend>Novo aditivo</legend>
              <div class="dw-row">
                <div class="dw-field"><label for="f-ad-tipo">Tipo</label>
                  <select id="f-ad-tipo" (change)="set('adTipo', $event)">
                    @for (t of tiposAditivoDe(i); track t) { <option [value]="t" [selected]="f()['adTipo'] === t">{{ tiposAditivo[t] }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-ad-assinado">Assinado em</label><input id="f-ad-assinado" type="date" [value]="f()['adAssinado'] ?? ''" (input)="set('adAssinado', $event)" /></div>
              </div>
              <div class="dw-row">
                @if (f()['adTipo'] === 'PRAZO') {
                  <div class="dw-field"><label for="f-ad-fim">Novo fim</label><input id="f-ad-fim" type="date" [value]="f()['adFim'] ?? ''" (input)="set('adFim', $event)" /></div>
                } @else {
                  <div class="dw-field" style="flex: 2;"><label for="f-ad-item">Item</label>
                    <select id="f-ad-item" (change)="set('adItem', $event)">
                      @for (x of i.itens; track x.uuid) { <option [value]="x.uuid" [selected]="f()['adItem'] === x.uuid">{{ x.descricao }}</option> }
                    </select></div>
                  @if (f()['adTipo'] === 'REEQUILIBRIO') {
                    <div class="dw-field"><label for="f-ad-preco">Novo preço</label><input id="f-ad-preco" type="number" min="0" step="0.0001" [value]="f()['adPreco'] ?? ''" (input)="set('adPreco', $event)" /></div>
                  } @else {
                    <div class="dw-field"><label for="f-ad-qtd">Quantidade</label><input id="f-ad-qtd" type="number" min="1" [value]="f()['adQtd'] ?? ''" (input)="set('adQtd', $event)" /></div>
                  }
                }
              </div>
              <div class="dw-field"><label for="f-ad-just">Justificativa</label><input id="f-ad-just" type="text" maxlength="1000" [value]="f()['adJust'] ?? ''" (input)="set('adJust', $event)" /></div>
              <div class="row-actions"><button type="button" class="btn btn--sm" (click)="aditar(i)" [disabled]="enviando()">Registrar aditivo</button></div>
            </fieldset>
          }

          <h3 class="mo-grupo ct-titulo">Ordens de fornecimento</h3>
          <ul class="ct-lista" aria-label="Ordens de fornecimento">
            @for (o of ordens(); track o.uuid) {
              <li><span><button type="button" class="link-btn" (click)="abrirOrdem(o.uuid)" [attr.aria-label]="'Abrir ordem ' + o.numero">{{ o.numero }}</button> <span class="cell-sub">{{ o.unidadeNome }} · prazo {{ data(o.prazoEm) }} · {{ moeda(o.valor) }}</span></span>
                <span [class]="'tag tag--' + (o.atrasada ? 'alert' : o.situacao === 'RECEBIDA' ? 'ok' : o.situacao === 'CANCELADA' ? '' : 'warn')">{{ situacoesOrdem[o.situacao] }}{{ o.atrasada ? ' · ' + o.diasDeAtraso + ' dia(s) de atraso' : '' }}</span></li>
            } @empty { <li class="cell-sub">Nenhuma ordem.</li> }
          </ul>
          @if (gerencia() && i.vigente) {
            <fieldset class="ct-grupo"><legend>Nova ordem de fornecimento</legend>
              <div class="dw-row">
                <div class="dw-field" style="flex: 2;"><label for="f-of-unidade">Unidade de entrega</label>
                  <select id="f-of-unidade" (change)="set('ofUnidade', $event)">
                    <option value="" [selected]="!f()['ofUnidade']">Escolha…</option>
                    @for (u of unidades(); track u.uuid) { <option [value]="u.uuid" [selected]="f()['ofUnidade'] === u.uuid">{{ u.nome }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-of-empenho">Empenho<span class="opt">opcional</span></label><input id="f-of-empenho" type="text" maxlength="60" [value]="f()['ofEmpenho'] ?? ''" (input)="set('ofEmpenho', $event)" /></div>
                <div class="dw-field" style="max-width: 120px;"><label for="f-of-prazo">Prazo (dias)</label><input id="f-of-prazo" type="number" min="1" max="365" [value]="f()['ofPrazo'] ?? '30'" (input)="set('ofPrazo', $event)" /></div>
              </div>
              @for (x of i.itens; track x.uuid; let k = $index) {
                <div class="dw-field" style="margin: 0;"><label [for]="'f-of-qtd-' + k">{{ x.descricao }} (saldo {{ x.saldo }})</label><input [id]="'f-of-qtd-' + k" type="number" min="0" [max]="x.saldo" [value]="f()['ofQtd-' + x.uuid] ?? ''" (input)="set('ofQtd-' + x.uuid, $event)" /></div>
              }
              <div class="row-actions"><button type="button" class="btn btn--sm" (click)="emitirOrdem(i)" [disabled]="enviando()">Emitir ordem</button></div>
            </fieldset>
          }
        </div>
      </app-drawer>
    }

    @if (ordem(); as o) {
      <app-drawer [titulo]="'Ordem ' + o.numero" (fechar)="ordem.set(null)">
        <div class="dw-body">
          @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
          <dl class="ro">
            <div><dt>Situação</dt><dd>{{ situacoesOrdem[o.situacao] }}@if (o.atrasada) { · {{ o.diasDeAtraso }} dia(s) de atraso }@if (o.motivoCancelamento) { · {{ o.motivoCancelamento }} }</dd></div>
            <div><dt>Fornecedor</dt><dd>{{ o.fornecedorNome }} · {{ o.instrumentoNumero }}</dd></div>
            <div><dt>Entrega</dt><dd>{{ o.unidadeNome }} · prazo {{ data(o.prazoEm) }}</dd></div>
            <div><dt>Valor</dt><dd>{{ moeda(o.valor) }}@if (o.empenho) { · empenho {{ o.empenho }} }</dd></div>
          </dl>
          <div class="tbl-wrap">
            <table class="ct-tabela" aria-label="Itens da ordem">
              <thead><tr><th scope="col">Item</th><th scope="col">Pedido</th><th scope="col">Aceito</th><th scope="col">Em recebimento</th><th scope="col">Falta</th></tr></thead>
              <tbody>@for (x of o.itens; track x.uuid) { <tr><td>{{ x.descricao }}</td><td class="mono">{{ x.quantidade }}</td><td class="mono">{{ x.aceita }}</td><td class="mono">{{ x.emRecebimento }}</td><td class="mono">{{ x.pendente }}</td></tr> }</tbody>
            </table>
          </div>

          @if (ordemAberta(o)) {
            <fieldset class="ct-grupo"><legend>Receber entrega (provisório)</legend>
              <div class="dw-row">
                <div class="dw-field"><label for="f-rc-nota">Nota fiscal</label><input id="f-rc-nota" type="text" maxlength="60" [value]="f()['rcNota'] ?? ''" (input)="set('rcNota', $event)" /></div>
                <div class="dw-field"><label for="f-rc-data">Data da nota</label><input id="f-rc-data" type="date" [value]="f()['rcData'] ?? ''" (input)="set('rcData', $event)" /></div>
              </div>
              @for (x of o.itens; track x.uuid; let k = $index) {
                @if (x.pendente > 0) {
                  <div class="dw-row">
                    <div class="dw-field" style="flex: 2;"><label [for]="'f-rc-qtd-' + k">{{ x.descricao }} (falta {{ x.pendente }})</label><input [id]="'f-rc-qtd-' + k" type="number" min="0" [max]="x.pendente" [value]="f()['rcQtd-' + x.uuid] ?? ''" (input)="set('rcQtd-' + x.uuid, $event)" /></div>
                    @if (x.pedeLote) {
                      <div class="dw-field"><label [for]="'f-rc-lote-' + k">Lote</label><input [id]="'f-rc-lote-' + k" type="text" maxlength="60" [value]="f()['rcLote-' + x.uuid] ?? ''" (input)="set('rcLote-' + x.uuid, $event)" /></div>
                      <div class="dw-field"><label [for]="'f-rc-val-' + k">Validade</label><input [id]="'f-rc-val-' + k" type="date" [value]="f()['rcVal-' + x.uuid] ?? ''" (input)="set('rcVal-' + x.uuid, $event)" /></div>
                    }
                  </div>
                }
              }
              <div class="row-actions"><button type="button" class="btn btn--sm" (click)="receber(o)" [disabled]="enviando()">Registrar recebimento provisório</button></div>
            </fieldset>
          }

          <h3 class="mo-grupo ct-titulo">Recebimentos</h3>
          <ul class="ct-itens" aria-label="Recebimentos">
            @for (r of o.recebimentos; track r.uuid; let ri = $index) {
              <li>
                <span><strong>NF {{ r.notaFiscal }}</strong> <span class="cell-sub">recebida em {{ data(r.recebidoEm) }}@if (r.definitivoEm) { · definitivo em {{ data(r.definitivoEm) }} }</span> <span [class]="'tag tag--' + (r.situacao === 'DEFINITIVO' ? 'ok' : 'warn')">{{ r.situacao === 'DEFINITIVO' ? 'Definitivo' : 'Provisório' }}</span></span>
                @for (x of r.itens; track x.uuid; let k = $index) {
                  <span class="cell-sub">{{ x.descricao }}: {{ x.entregue }} entregue(s)@if (x.numeroLote) { · lote {{ x.numeroLote }} até {{ data(x.validade ?? '') }} }@if (x.aceita != null) { · {{ x.aceita }} aceito(s)@if (x.recusada) { · {{ x.recusada }} recusado(s): {{ x.motivoRecusa }} } }</span>
                  @if (r.situacao === 'PROVISORIO') {
                    <div class="dw-row">
                      <div class="dw-field"><label [for]="'f-df-' + ri + '-' + k">Aceito de {{ x.descricao }}</label><input [id]="'f-df-' + ri + '-' + k" type="number" min="0" [max]="x.entregue" [value]="f()['dfAc-' + x.uuid] ?? x.entregue" (input)="set('dfAc-' + x.uuid, $event)" /></div>
                      <div class="dw-field" style="flex: 2;"><label [for]="'f-dm-' + ri + '-' + k">Motivo da recusa</label><input [id]="'f-dm-' + ri + '-' + k" type="text" maxlength="500" [value]="f()['dfMo-' + x.uuid] ?? ''" (input)="set('dfMo-' + x.uuid, $event)" /></div>
                    </div>
                  }
                }
                @if (r.situacao === 'PROVISORIO') { <div class="row-actions"><button type="button" class="btn btn--sm" (click)="definitivo(r)" [disabled]="enviando()">Confirmar recebimento definitivo</button></div> }
              </li>
            } @empty { <li class="cell-sub">Nenhum recebimento.</li> }
          </ul>

          <h3 class="mo-grupo ct-titulo">Ocorrências</h3>
          <ul class="ct-lista" aria-label="Ocorrências da ordem">
            @for (x of o.ocorrencias; track x.uuid) { <li><span>{{ tiposOcorrencia[x.tipo] }} <span class="cell-sub">{{ data(x.registradaEm) }} · {{ x.descricao }}</span></span><span class="cell-sub">{{ x.situacao === 'ABERTA' ? 'Aberta' : x.situacao === 'ENCERRADA' ? 'Encerrada' : 'Sancionada' }}</span></li> }
            @empty { <li class="cell-sub">Nenhuma ocorrência.</li> }
          </ul>
          <fieldset class="ct-grupo"><legend>Nova ocorrência</legend>
            <div class="dw-row">
              <div class="dw-field"><label for="f-oc-tipo">Tipo</label>
                <select id="f-oc-tipo" (change)="set('ocTipo', $event)">
                  @for (t of listaOcorrencias; track t) { <option [value]="t" [selected]="f()['ocTipo'] === t">{{ tiposOcorrencia[t] }}</option> }
                </select></div>
              <div class="dw-field" style="flex: 2;"><label for="f-oc-desc">Descrição</label><input id="f-oc-desc" type="text" maxlength="1000" [value]="f()['ocDesc'] ?? ''" (input)="set('ocDesc', $event)" /></div>
            </div>
            <div class="row-actions"><button type="button" class="btn btn--ghost btn--sm" (click)="registrarOcorrencia(o)" [disabled]="enviando()">Registrar ocorrência</button></div>
          </fieldset>

          @if (gerencia() && ordemAberta(o)) {
            <h3 class="mo-grupo ct-titulo">Cancelar</h3>
            <div class="ct-linha">
              <div class="dw-field" style="margin: 0; flex: 1;"><label for="f-of-cancel">Motivo</label><input id="f-of-cancel" type="text" maxlength="500" [value]="f()['ofCancel'] ?? ''" (input)="set('ofCancel', $event)" /></div>
              <button type="button" class="btn btn--ghost btn--sm" (click)="cancelarOrdem(o)" [disabled]="enviando()">Cancelar ordem</button>
            </div>
          }
          <div class="row-actions ct-acoes"><button type="button" class="btn btn--ghost btn--sm" (click)="abrirInstrumento(o.instrumentoId)">Voltar ao contrato</button></div>
        </div>
      </app-drawer>
    }
  `,
  styles: `
    .ct-cab { display: flex; justify-content: flex-end; margin-bottom: var(--s-2); }
    .ct-tabela { min-width: 0; width: 100%; }
    .ct-titulo { margin-top: var(--s-4); }
    .ct-acoes { margin-top: var(--s-3); }
    .ct-grupo { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); margin: var(--s-2) 0; display: grid; gap: var(--s-1); }
    .ct-itens { list-style: none; margin: 0; padding: 0; display: grid; gap: var(--s-2); }
    .ct-itens > li { border-left: 3px solid var(--ink-200); padding-left: var(--s-2); display: grid; gap: var(--s-1); }
    .ct-lista { margin: 0; padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .ct-lista li { display: flex; justify-content: space-between; gap: var(--s-2); align-items: baseline; flex-wrap: wrap; }
    .ct-linha { display: flex; gap: var(--s-2); align-items: flex-end; flex-wrap: wrap; }
    .chk { display: flex; gap: var(--s-2); align-items: center; }
  `,
})
export class ContratosCompras {
  readonly aba = input.required<'contratacoes' | 'contratos'>();

  private readonly service = inject(ComprasService);
  private readonly authService = inject(AuthService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly modalidades = MODALIDADES;
  protected readonly listaModalidades = Object.keys(MODALIDADES) as ModalidadeContratacao[];
  protected readonly situacoes = SITUACOES_CONTRATACAO;
  protected readonly resultados = RESULTADOS_ITEM;
  protected readonly listaResultados = (Object.keys(RESULTADOS_ITEM) as ResultadoItemContratacao[]).filter((r) => r !== 'PENDENTE');
  protected readonly tiposInstrumento = TIPOS_INSTRUMENTO;
  protected readonly listaInstrumentos = Object.keys(TIPOS_INSTRUMENTO) as TipoInstrumentoContratual[];
  protected readonly tiposAditivo = TIPOS_ADITIVO;
  protected readonly situacoesOrdem = SITUACOES_ORDEM;
  protected readonly tiposOcorrencia = TIPOS_OCORRENCIA;
  protected readonly listaOcorrencias = (Object.keys(TIPOS_OCORRENCIA) as TipoOcorrenciaFornecedor[]).filter((t) => t !== 'ATRASO');
  protected readonly ordens = signal<OrdemDto[]>([]);
  protected readonly ordem = signal<OrdemDto | null>(null);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly contratacoes = signal<ContratacaoDto[]>([]);
  protected readonly instrumentos = signal<InstrumentoDto[]>([]);
  protected readonly cotadas = signal<SolicitacaoCompraDto[]>([]);
  protected readonly fornecedores = signal<FornecedorDto[]>([]);
  protected readonly escolhidas = signal<Set<string>>(new Set());
  protected readonly abrindo = signal(false);
  protected readonly contratacao = signal<ContratacaoDto | null>(null);
  protected readonly instrumento = signal<InstrumentoDto | null>(null);
  protected readonly regulares = computed(() => this.fornecedores().filter((x) => x.regular));
  protected readonly gerencia = computed(() => {
    const a = this.acesso();
    return !a.restrito || a.permissoes.has(GERENCIAR);
  });
  /** Quantos contratos têm alerta, para o contador da aba. */
  readonly comAlerta = computed(() => this.instrumentos().filter((i) => i.alertas.length).length);

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      this.carregar();
    });
  }

  carregar(): void {
    if (this.gerencia()) {
      this.service.contratacoes().subscribe({ next: (l) => this.contratacoes.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
      this.service.instrumentos().subscribe({ next: (l) => this.instrumentos.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
    } else {
      this.service.meusContratos().subscribe({ next: (l) => this.instrumentos.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
    }
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  private fechar(): void {
    this.erroGaveta.set(null);
    this.abrindo.set(false);
    this.contratacao.set(null);
    this.instrumento.set(null);
    this.ordem.set(null);
  }

  // ── Contratação ──

  protected novaContratacao(): void {
    this.fechar();
    this.f.set({ modalidade: 'PREGAO_ELETRONICO' });
    this.escolhidas.set(new Set());
    this.service.solicitacoes('COTADA').subscribe((l) => this.cotadas.set(l.filter((s) => !s.contratacaoId)));
    this.abrindo.set(true);
  }

  protected alternar(id: string): void {
    const s = new Set(this.escolhidas());
    if (s.has(id)) s.delete(id); else s.add(id);
    this.escolhidas.set(s);
  }

  protected salvarContratacao(): void {
    const c = this.f();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    if (!t('processo') || !t('objeto') || !this.escolhidas().size) {
      this.erroGaveta.set('Informe o processo, o objeto e ao menos uma solicitação cotada.');
      return;
    }
    this.executar(this.service.abrirContratacao({ numeroProcesso: t('processo')!, modalidade: c['modalidade'] as ModalidadeContratacao, objeto: t('objeto')!,
      fundamentoLegal: t('fundamento'), ataOrigem: t('ata'), solicitacaoIds: [...this.escolhidas()] }), (x) => this.mostrarContratacao(x));
  }

  protected abrirContratacao(id: string): void {
    this.service.contratacao(id).subscribe({ next: (c) => this.mostrarContratacao(c), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  private mostrarContratacao(c: ContratacaoDto): void {
    this.fechar();
    const hoje = hojeIso();
    const campos: Record<string, string> = {};
    for (const i of c.itens) {
      if (i.fornecedorId) campos['forn-' + i.uuid] = i.fornecedorId;
      if (i.precoUnitario != null) campos['preco-' + i.uuid] = String(i.precoUnitario);
    }
    for (const v of this.vencedores(c)) campos['inicio-' + v.id] = hoje;
    this.f.set(campos);
    this.contratacao.set(c);
    if (c.situacao === 'EM_ANDAMENTO' && !this.fornecedores().length) this.service.fornecedores().subscribe((l) => this.fornecedores.set(l));
  }

  protected acimaDaReferencia(i: ItemContratacaoDto): boolean {
    const preco = Number(this.f()['preco-' + i.uuid]);
    return i.precoReferencia != null && preco > i.precoReferencia;
  }

  protected registrarResultado(i: ItemContratacaoDto): void {
    const c = this.f();
    const resultado = (c['res-' + i.uuid] ?? 'ADJUDICADO') as ResultadoItemContratacao;
    const adjudicado = resultado === 'ADJUDICADO';
    const preco = Number(c['preco-' + i.uuid]);
    if (adjudicado && (!c['forn-' + i.uuid] || !(preco > 0))) {
      this.erroGaveta.set(`Item ${i.ordem}: informe o fornecedor e o preço.`);
      return;
    }
    this.executar(this.service.resultado(i.uuid, { resultado, fornecedorId: adjudicado ? c['forn-' + i.uuid] : null, precoUnitario: adjudicado ? preco : null,
      justificativaPreco: adjudicado ? (c['just-' + i.uuid] ?? '').trim() || null : null }), (x) => this.manter(x));
  }

  /** Mostra a contratação devolvida sem perder o que já foi digitado na homologação. */
  private manter(x: ContratacaoDto): void {
    const antes = this.f();
    this.mostrarContratacao(x);
    this.f.set({ ...this.f(), ...Object.fromEntries(Object.entries(antes).filter(([k]) => !k.startsWith('preco-') && !k.startsWith('forn-'))) });
  }

  protected vencedores(c: ContratacaoDto): { id: string; nome: string }[] {
    const m = new Map<string, string>();
    for (const i of c.itens) if (i.resultado === 'ADJUDICADO' && i.fornecedorId) m.set(i.fornecedorId, i.fornecedorNome ?? '');
    return [...m.entries()].map(([id, nome]) => ({ id, nome }));
  }

  protected alternarContinuo(id: string): void {
    this.f.set({ ...this.f(), ['continuo-' + id]: this.f()['continuo-' + id] === 'sim' ? '' : 'sim' });
  }

  protected homologar(c: ContratacaoDto): void {
    const f = this.f();
    const instrumentos = [];
    for (const v of this.vencedores(c)) {
      const t = (k: string) => (f[k + '-' + v.id] ?? '').trim();
      if (!t('numero') || !t('inicio') || !t('fim') || !t('gestor') || !t('fiscal')) {
        this.erroGaveta.set(`${v.nome}: informe o número, a vigência, o gestor e o fiscal.`);
        return;
      }
      const tipo = (f['tipo-' + v.id] ?? 'ATA_REGISTRO_PRECOS') as TipoInstrumentoContratual;
      instrumentos.push({ fornecedorId: v.id, tipo, numero: t('numero'), inicio: t('inicio'), fim: t('fim'),
        servicoContinuo: tipo === 'CONTRATO' && f['continuo-' + v.id] === 'sim', gestorMatricula: t('gestor'), fiscalMatricula: t('fiscal') });
    }
    this.executar(this.service.homologar(c.uuid, instrumentos), (x) => this.mostrarContratacao(x));
  }

  protected cancelar(c: ContratacaoDto): void {
    const motivo = (this.f()['cancelamento'] ?? '').trim();
    if (!motivo) {
      this.erroGaveta.set('Informe o motivo do cancelamento.');
      return;
    }
    this.executar(this.service.cancelarContratacao(c.uuid, motivo), (x) => this.mostrarContratacao(x));
  }

  // ── Atas e contratos ──

  protected abrirInstrumento(id: string): void {
    this.service.instrumento(id).subscribe({ next: (i) => this.mostrarInstrumento(i), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  private mostrarInstrumento(i: InstrumentoDto): void {
    this.fechar();
    this.f.set({ adTipo: 'PRAZO', adAssinado: hojeIso(), adItem: i.itens[0]?.uuid ?? '', ofPrazo: '30' });
    this.instrumento.set(i);
    this.ordens.set([]);
    this.service.ordensDo(i.uuid).subscribe({ next: (l) => this.ordens.set(l), error: () => this.ordens.set([]) });
    if (this.gerencia() && !this.unidades().length) this.unidadeService.listar().subscribe((l) => this.unidades.set(l));
  }

  // ── Ordens de fornecimento e recebimento (CP3, ADR-0214) ──

  protected emitirOrdem(i: InstrumentoDto): void {
    const c = this.f();
    const itens = i.itens.map((x) => ({ itemInstrumentoId: x.uuid, quantidade: Number(c['ofQtd-' + x.uuid] || 0) })).filter((x) => x.quantidade > 0);
    if (!c['ofUnidade'] || !itens.length) {
      this.erroGaveta.set('Informe a unidade de entrega e a quantidade de ao menos um item.');
      return;
    }
    this.executarOrdem(this.service.emitirOrdem(i.uuid, { unidadeId: c['ofUnidade'], empenho: (c['ofEmpenho'] ?? '').trim() || null,
      prazoDias: Number(c['ofPrazo'] || 30), itens }));
  }

  protected abrirOrdem(id: string): void {
    this.service.ordem(id).subscribe({ next: (o) => this.mostrarOrdem(o), error: (x: HttpErrorResponse) => this.erroGaveta.set(mensagem(x)) });
  }

  private mostrarOrdem(o: OrdemDto): void {
    this.fechar();
    this.f.set({ rcData: hojeIso(), ocTipo: 'ENTREGA_PARCIAL' });
    this.ordem.set(o);
  }

  protected ordemAberta(o: OrdemDto): boolean {
    return o.situacao === 'EMITIDA' || o.situacao === 'PARCIAL';
  }

  protected receber(o: OrdemDto): void {
    const c = this.f();
    const itens = o.itens.filter((x) => Number(c['rcQtd-' + x.uuid] || 0) > 0).map((x) => ({ itemOrdemId: x.uuid, quantidade: Number(c['rcQtd-' + x.uuid]),
      numeroLote: x.pedeLote ? (c['rcLote-' + x.uuid] ?? '').trim() || null : null, validade: x.pedeLote ? c['rcVal-' + x.uuid] || null : null }));
    if (!(c['rcNota'] ?? '').trim() || !c['rcData'] || !itens.length) {
      this.erroGaveta.set('Informe a nota fiscal, a data e a quantidade entregue de ao menos um item.');
      return;
    }
    if (itens.some((x) => o.itens.find((i) => i.uuid === x.itemOrdemId)?.pedeLote && (!x.numeroLote || !x.validade))) {
      this.erroGaveta.set('Informe o lote e a validade dos itens que pedem.');
      return;
    }
    this.executarOrdem(this.service.receber(o.uuid, { notaFiscal: c['rcNota'].trim(), notaEmitidaEm: c['rcData'], observacao: null, itens }));
  }

  protected definitivo(r: RecebimentoDto): void {
    const c = this.f();
    const itens = r.itens.map((x) => ({ itemRecebimentoId: x.uuid, quantidadeAceita: Number(c['dfAc-' + x.uuid] ?? x.entregue),
      motivoRecusa: (c['dfMo-' + x.uuid] ?? '').trim() || null }));
    if (itens.some((x, k) => x.quantidadeAceita < r.itens[k].entregue && !x.motivoRecusa)) {
      this.erroGaveta.set('Informe o motivo da recusa.');
      return;
    }
    this.executarOrdem(this.service.definitivo(r.uuid, itens));
  }

  protected registrarOcorrencia(o: OrdemDto): void {
    const desc = (this.f()['ocDesc'] ?? '').trim();
    if (!desc) {
      this.erroGaveta.set('Descreva a ocorrência.');
      return;
    }
    this.executarOrdem(this.service.registrarOcorrencia(o.uuid, this.f()['ocTipo'] as TipoOcorrenciaFornecedor, desc));
  }

  protected cancelarOrdem(o: OrdemDto): void {
    const motivo = (this.f()['ofCancel'] ?? '').trim();
    if (!motivo) {
      this.erroGaveta.set('Informe o motivo do cancelamento.');
      return;
    }
    this.executarOrdem(this.service.cancelarOrdem(o.uuid, motivo));
  }

  private executarOrdem(req: Observable<OrdemDto>): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    req.subscribe({
      next: (o) => {
        this.enviando.set(false);
        this.mostrarOrdem(o);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected tiposAditivoDe(i: InstrumentoDto): TipoAditivoContratual[] {
    const todos = Object.keys(TIPOS_ADITIVO) as TipoAditivoContratual[];
    return i.tipo === 'ATA_REGISTRO_PRECOS' ? todos.filter((t) => t === 'PRAZO' || t === 'REEQUILIBRIO') : todos;
  }

  protected aditar(i: InstrumentoDto): void {
    const c = this.f();
    const tipo = c['adTipo'] as TipoAditivoContratual;
    const just = (c['adJust'] ?? '').trim();
    const quantidade = Number(c['adQtd']);
    const preco = Number(c['adPreco']);
    const falta = !just || !c['adAssinado'] || (tipo === 'PRAZO' ? !c['adFim'] : !c['adItem'])
      || ((tipo === 'ACRESCIMO' || tipo === 'SUPRESSAO') && !(quantidade >= 1)) || (tipo === 'REEQUILIBRIO' && !(preco > 0));
    if (falta) {
      this.erroGaveta.set('Informe a assinatura, a justificativa e, conforme o tipo, o novo fim, o item com a quantidade ou o novo preço.');
      return;
    }
    this.enviando.set(true);
    this.erroGaveta.set(null);
    this.service.aditivo(i.uuid, { tipo, novoFim: tipo === 'PRAZO' ? c['adFim'] : null, itemId: tipo === 'PRAZO' ? null : c['adItem'],
      quantidade: tipo === 'ACRESCIMO' || tipo === 'SUPRESSAO' ? quantidade : null, novoPreco: tipo === 'REEQUILIBRIO' ? preco : null,
      justificativa: just, assinadoEm: c['adAssinado'] }).subscribe({
      next: (x) => {
        this.enviando.set(false);
        this.mostrarInstrumento(x);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  private executar(req: Observable<ContratacaoDto>, depois: (c: ContratacaoDto) => void): void {
    this.enviando.set(true);
    this.erroGaveta.set(null);
    req.subscribe({
      next: (c) => {
        this.enviando.set(false);
        depois(c);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', minimumFractionDigits: 2, maximumFractionDigits: 4 });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}

function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
