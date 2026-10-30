import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../core/models/auth';
import {
  DECRETOS,
  EmergenciaDto,
  EventoEmergencialDto,
  FONTES_EVENTO,
  FonteEventoEmergencial,
  NIVEIS_IMPACTO,
  NivelEmergenciaDto,
  NivelImpacto,
  SITUACOES_EMERGENCIA,
  TIPOS_ESTRUTURA,
  PlanoAtivoDto,
  PlanoContingenciaDto,
  TIPOS_REGISTRO_ATIVACAO,
  SITUACOES_EVENTO,
  SugestaoImpactoDto,
  TipoDecretoEmergencia,
  TipoEmergenciaDto,
  classeDoNivel,
} from '../../core/models/emergencia';
import { ErrorResponseDto } from '../../core/models/profissional';
import { AuthService } from '../../core/services/auth';
import { EmergenciaService } from '../../core/services/emergencia';
import { Drawer } from '../../shared/drawer/drawer';
import { EdicaoMapa, Mapa, PinoMapa, PoligonoMapa } from '../../shared/mapa/mapa';
import { CapacidadeEmergencial } from './capacidade-emergencial';
import { CatalogosEmergencia } from './catalogos-emergencia';
import { CoordenacaoEmergencia } from './coordenacao-emergencia';
import { EventoEspecializado } from './evento-especializado';
import { PlanosContingencia } from './planos-contingencia';
import { RelatorioPosEvento } from './relatorio-pos-evento';

type Aba = 'ativas' | 'planejadas' | 'desmobilizacao' | 'eventos' | 'encerradas' | 'planos' | 'capacidade' | 'catalogos';
type Gaveta =
  | { tipo: 'evento' }
  | { tipo: 'descartar'; evento: EventoEmergencialDto }
  | { tipo: 'converter'; evento: EventoEmergencialDto }
  | { tipo: 'nivel' | 'territorio' | 'referencias' | 'avaliacao' | 'encerrar' | 'ativarPlano' | 'desmobilizar' | 'cancelarRestantes' | 'operacao'; emergencia: EmergenciaDto }
  | { tipo: 'desativarPlano'; emergencia: EmergenciaDto; plano: PlanoAtivoDto };

/**
 * Emergências e Desastres, fatia E1 (ADR-0156): os eventos (registrar, monitorar, descartar, converter) e as emergências
 * (nível com histórico, território no mapa, ESPIN e decreto, avaliações de impacto com as sugestões dos donos,
 * encerramento). Coordenação, não posse: nada aqui mexe em leito, escala ou estoque. O evento de massa é planejado antes
 * de acontecer e a operação começa depois (ADR-0160).
 */
@Component({
  selector: 'app-emergencias',
  imports: [Drawer, Mapa, PlanosContingencia, CatalogosEmergencia, CapacidadeEmergencial, CoordenacaoEmergencia, EventoEspecializado, RelatorioPosEvento],
  template: `
    <header class="page-head">
      <div>
        <h1>Emergências e desastres</h1>
        <p>Eventos, emergências ativas, nível de resposta e impacto na rede</p>
      </div>
      <div class="page-actions">
        @if (pode('EMERGENCIA.REGISTRAR')) { <button type="button" class="btn" (click)="abrirEvento(null)">Registrar evento</button> }
      </div>
    </header>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    <div class="tabs" role="tablist" aria-label="Seções das emergências">
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'ativas'" (click)="aba.set('ativas')">Emergências ativas <span class="cnt">{{ ativas().length }}</span></button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'planejadas'" (click)="aba.set('planejadas')">Planejadas@if (planejadas().length) { <span class="cnt">{{ planejadas().length }}</span> }</button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'eventos'" (click)="aba.set('eventos')">Eventos <span class="cnt">{{ eventosAbertos() }}</span></button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'desmobilizacao'" (click)="aba.set('desmobilizacao')">Em desmobilização@if (emDesmobilizacao().length) { <span class="cnt">{{ emDesmobilizacao().length }}</span> }</button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'encerradas'" (click)="aba.set('encerradas')">Encerradas</button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'planos'" (click)="aba.set('planos')">Planos de contingência</button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'capacidade'" (click)="aba.set('capacidade')">Capacidade emergencial</button>
      <button type="button" role="tab" [attr.aria-selected]="aba() === 'catalogos'" (click)="aba.set('catalogos')">Catálogos</button>
    </div>

    @if (aba() === 'planos') {
      <app-planos-contingencia [tipos]="tipos()" [niveis]="niveis()" [podeGerenciar]="pode('CONTINGENCIA.GERENCIAR')" />
    } @else if (aba() === 'capacidade') {
      <app-capacidade-emergencial [podeGerenciar]="pode('CONTINGENCIA.GERENCIAR')" />
    } @else if (aba() === 'catalogos') {
      <app-catalogos-emergencia [tipos]="tipos()" [niveis]="niveis()" [podeGerenciar]="pode('CONTINGENCIA.GERENCIAR')" (alterado)="carregarCatalogos()" />
    } @else if (aba() === 'eventos') {
      <div class="tbl-wrap card">
        <table aria-label="Eventos emergenciais">
          <thead><tr><th scope="col">Evento</th><th scope="col">Ocorrido</th><th scope="col">Fonte</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (e of eventos(); track e.uuid) {
              <tr>
                <td><span class="cell-main">{{ e.tipoNome }}@if (e.local) { · {{ e.local }} }</span><span class="cell-sub">{{ e.descricao }}@if (e.surtoNome) { · surto {{ e.surtoNome }} }</span></td>
                <td class="mono">{{ dataHora(e.ocorridoEm) }}</td>
                <td>{{ fontes[e.fonte] }}</td>
                <td><span [class]="'tag tag--' + situacoesEvento[e.situacao].classe">{{ situacoesEvento[e.situacao].rotulo }}</span>@if (e.motivoDescarte) { <span class="cell-sub">{{ e.motivoDescarte }}</span> }</td>
                <td class="row-actions">
                  @if (e.situacao === 'REGISTRADO' || e.situacao === 'MONITORADO') {
                    @if (pode('EMERGENCIA.REGISTRAR') && e.situacao === 'REGISTRADO') { <button type="button" class="link-btn" (click)="monitorar(e)" [attr.aria-label]="'Monitorar ' + e.tipoNome">Monitorar</button> }
                    @if (pode('EMERGENCIA.REGISTRAR')) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'descartar', evento: e })" [attr.aria-label]="'Descartar ' + e.tipoNome">Descartar</button> }
                    @if (pode('EMERGENCIA.ATIVAR')) { <button type="button" class="link-btn" (click)="abrirConverter(e)" [attr.aria-label]="'Converter em emergência: ' + e.tipoNome">Converter em emergência</button> }
                  }
                  @if (e.emergenciaId) { <button type="button" class="link-btn" (click)="selecionar(e.emergenciaId)">Ver emergência</button> }
                </td>
              </tr>
            } @empty { <tr><td colspan="5" class="cell-sub">Nenhum evento registrado.</td></tr> }
          </tbody>
        </table>
      </div>
    } @else {
      <div class="tbl-wrap card">
        <table [attr.aria-label]="rotuloDaLista()">
          <thead><tr><th scope="col">Emergência</th><th scope="col">Nível</th><th scope="col">Início</th><th scope="col">Último impacto</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (e of lista(); track e.uuid) {
              <tr [class.is-selected]="detalhe()?.uuid === e.uuid">
                <td><span class="cell-main">{{ e.nome }}</span><span class="cell-sub">{{ e.tipoNome }}@if (e.cobrade) { · COBRADE {{ e.cobrade }} }</span></td>
                <td><span [class]="'tag tag--' + classe(e.nivelOrdem)">{{ e.nivelNome }}</span></td>
                <td class="mono">@if (e.situacao === 'PLANEJADA' && e.eventoMassa) { {{ dataHora(e.eventoMassa.inicioPrevisto) }} <span class="cell-sub">previsto</span> } @else { {{ dataHora(e.inicio) }} }@if (e.fim) { <span class="cell-sub">até {{ dataHora(e.fim) }}</span> }</td>
                <td>@if (e.acoesAtrasadas) { <span class="tag tag--alert">{{ e.acoesAtrasadas }} ação(ões) atrasada(s)</span> }@if (e.ultimoImpacto) { <span [class]="'tag tag--' + impactos[e.ultimoImpacto].classe">{{ impactos[e.ultimoImpacto].rotulo }}</span> } @else { <span class="cell-sub">sem avaliação</span> }</td>
                <td class="row-actions"><button type="button" class="btn btn--ghost btn--sm" (click)="selecionar(e.uuid)" [attr.aria-label]="'Abrir ' + e.nome">Abrir</button></td>
              </tr>
            } @empty { <tr><td colspan="5" class="cell-sub">{{ vazioDaLista() }}</td></tr> }
          </tbody>
        </table>
      </div>
    }

    @if (aba() !== 'planos' && aba() !== 'catalogos' && aba() !== 'capacidade' && detalhe(); as d) {
      <section class="card em-detalhe" [attr.aria-label]="'Emergência ' + d.nome">
        <div class="em-cab">
          <div>
            <h2 class="em-titulo">{{ d.nome }}</h2>
            <p class="cell-sub">{{ d.tipoNome }}@if (d.cobrade) { · COBRADE {{ d.cobrade }} } · {{ d.situacao === 'PLANEJADA' ? 'planejada em' : 'desde' }} {{ dataHora(d.inicio) }}@if (d.responsavelNome) { · responsável {{ d.responsavelNome }} }</p>
            <span [class]="'tag tag--' + classe(d.nivelOrdem)">{{ d.nivelNome }}</span>
            <span [class]="'tag tag--' + situacoesEmergencia[d.situacao].classe">{{ situacoesEmergencia[d.situacao].rotulo }}</span>
            @if (d.ultimoImpacto) { <span [class]="'tag tag--' + impactos[d.ultimoImpacto].classe">Impacto {{ impactos[d.ultimoImpacto].rotulo.toLowerCase() }}</span> }
          </div>
          @if (d.situacao === 'ATIVA') {
            <div class="row-actions" style="flex-wrap: wrap;">
              @if (pode('EMERGENCIA.REGISTRAR')) { <button type="button" class="btn btn--sm" (click)="abrirAvaliacao(d)">Nova avaliação</button> }
              @if (pode('EMERGENCIA.REGISTRAR', 'EMERGENCIA.ATIVAR')) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirTerritorio(d)">Território</button> }
              @if (pode('EMERGENCIA.ATIVAR')) {
                <button type="button" class="btn btn--ghost btn--sm" (click)="abrirNivel(d)">Mudar nível</button>
                <button type="button" class="btn btn--ghost btn--sm" (click)="abrirReferencias(d)">ESPIN e decreto</button>
                <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'desmobilizar', emergencia: d })">Iniciar desmobilização</button>
                <button type="button" class="btn btn--ghost btn--sm" (click)="abrirEncerrar(d)">Encerrar</button>
              }
            </div>
          }
          @if (d.situacao === 'PLANEJADA') {
            <div class="row-actions" style="flex-wrap: wrap;">
              @if (pode('EMERGENCIA.REGISTRAR', 'EMERGENCIA.ATIVAR')) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirTerritorio(d)">Território</button> }
              @if (pode('EMERGENCIA.ATIVAR')) {
                <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'operacao', emergencia: d })" [disabled]="!d.eventoMassa">Iniciar operação</button>
                <button type="button" class="btn btn--ghost btn--sm" (click)="abrirEncerrar(d)">Cancelar evento</button>
              }
            </div>
          }
          @if (d.situacao === 'EM_DESMOBILIZACAO') {
            <div class="row-actions" style="flex-wrap: wrap;">
              @if (pode('EMERGENCIA.COORDENAR', 'EMERGENCIA.ATIVAR') && acoesAbertas(d)) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'cancelarRestantes', emergencia: d })">Cancelar ações restantes</button> }
              @if (pode('EMERGENCIA.ATIVAR')) { <button type="button" class="btn btn--sm" (click)="abrirEncerrar(d)" [disabled]="acoesAbertas(d) > 0">Encerrar</button> }
            </div>
          }
        </div>
        @if (d.desmobilizacao; as dm) {
          <div class="box box--info em-desmob" aria-label="Desmobilização">
            <div><strong>Desmobilização iniciada em {{ dataHora(dm.iniciadaEm) }} por {{ dm.responsavel }}.</strong> {{ dm.justificativa }}.
              Liberados: {{ dm.mobilizacoesConcluidas }} mobilização(ões) ({{ dm.profissionaisLiberados }} profissional(is)), {{ dm.capacidadesDesativadas }} capacidade(s), {{ dm.planosDesativados }} plano(s)@if (dm.estruturasDesativadas) {, {{ dm.estruturasDesativadas }} estrutura(s) temporária(s) }.</div>
          </div>
        }
        @if (d.pendencias.length && d.situacao === 'EM_DESMOBILIZACAO') {
          <ul class="eq-prof em-pendencias" aria-label="Pendências da desmobilização">
            @for (p of d.pendencias; track $index) { <li><span class="cell-main">{{ p }}</span></li> }
          </ul>
        }
        @if (d.motivoEncerramento) { <div class="box box--info"><div>Encerrada: {{ d.motivoEncerramento }}</div></div> }

        <div class="em-planos">
          <div class="em-cab" style="margin-bottom: var(--s-2);">
            <h3 class="mo-grupo" style="margin: 0;">Planos de contingência</h3>
            @if (d.situacao === 'ATIVA' && pode('EMERGENCIA.ATIVAR')) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirAtivarPlano(d)">Ativar plano</button> }
          </div>
          @if (d.planos.length) {
            <ul class="eq-prof" aria-label="Planos da emergência">
              @for (p of d.planos; track p.ativacaoId) {
                <li>
                  <div class="em-cab" style="margin: 0;">
                    <span class="cell-main">{{ p.nome }} <span class="cell-sub mono" style="display: inline;">{{ p.codigo }} v{{ p.versao }}</span>
                      <span [class]="'tag tag--' + (p.situacao === 'ATIVA' ? 'ok' : 'muted')">{{ p.situacao === 'ATIVA' ? 'Ativo' : 'Desativado' }}</span>
                      @if (p.situacao === 'ATIVA' && !p.cobreNivelAtual) { <span class="tag tag--warn">Não cobre o nível atual</span> }</span>
                    @if (p.situacao === 'ATIVA' && d.situacao === 'ATIVA' && pode('EMERGENCIA.ATIVAR')) { <button type="button" class="link-btn" (click)="abrirDesativarPlano(d, p)" [attr.aria-label]="'Desativar ' + p.nome">Desativar</button> }
                  </div>
                  @if (p.acoesDoNivel.length) {
                    <ul class="em-acoes" [attr.aria-label]="'Ações previstas para o nível: ' + p.nome">
                      @for (a of p.acoesDoNivel; track $index) {
                        <li>{{ a.descricao }} <span class="cell-sub" style="display: inline;">· {{ a.responsavel }}@if (a.prazoEm) { · até {{ dataHora(a.prazoEm) }} }@if (a.unidadeNome) { · {{ a.unidadeNome }} }</span></li>
                      }
                    </ul>
                  }
                  <span class="cell-sub">@for (r of p.registros; track $index; let ultimo = $last) { {{ tiposRegistro[r.tipo] }} ({{ r.nivelNome }}) em {{ dataHora(r.registradoEm) }}@if (r.autoridade) { por {{ r.autoridade }} }@if (!ultimo) { · } }</span>
                </li>
              }
            </ul>
          } @else { <p class="cell-sub">Nenhum plano ativado nesta emergência.</p> }
        </div>

        <app-evento-especializado [emergencia]="d" [podeCoordenar]="pode('EMERGENCIA.COORDENAR', 'EMERGENCIA.ATIVAR')" [podeRegistrar]="pode('EMERGENCIA.REGISTRAR')" (alterada)="aposCoordenacao($event)" />
        @if (d.situacao !== 'PLANEJADA') {
          <app-coordenacao-emergencia [emergencia]="d" [podeCoordenar]="pode('EMERGENCIA.COORDENAR')" (alterada)="aposCoordenacao($event)" />
        }
        @if (d.situacao === 'EM_DESMOBILIZACAO' || d.situacao === 'ENCERRADA') {
          <app-relatorio-pos-evento [emergencia]="d" [podeEscrever]="pode('EMERGENCIA.COORDENAR', 'EMERGENCIA.ATIVAR')" [podeFinalizar]="pode('EMERGENCIA.ATIVAR')" [podeRevisarPlano]="pode('CONTINGENCIA.GERENCIAR')" />
        }

        <div class="em-grade">
          <div>
            <h3 class="mo-grupo">Território afetado</h3>
            @if (d.geometria || d.microareas.length || pinos().length) {
              <app-mapa [poligonos]="poligonos()" [pinos]="pinos()" [altura]="300" rotulo="Território afetado" />
              <p class="cell-sub">@if (d.microareas.length) { Microáreas: @for (m of d.microareas; track m.uuid; let ultimo = $last) { {{ m.areaNome }} {{ m.codigo }}@if (m.nome) { ({{ m.nome }}) }@if (!ultimo) { · } } } @else { Nenhuma microárea do Território dentro da área. }</p>
            } @else { <p class="cell-sub">Sem área desenhada. Use "Território" para marcar no mapa.</p> }
            <h3 class="mo-grupo">Referências legais</h3>
            <p class="cell-sub">O sistema registra; declarar é ato do poder público.</p>
            <dl class="info-list">
              <div><dt>ESPIN</dt><dd>@if (d.espinNumero) { {{ d.espinNumero }} · {{ data(d.espinData) }} } @else { — }</dd></div>
              <div><dt>Decreto</dt><dd>@if (d.decretoTipo) { {{ decretos[d.decretoTipo] }} nº {{ d.decretoNumero }} · {{ data(d.decretoData) }} } @else { — }</dd></div>
            </dl>
          </div>
          <div>
            <h3 class="mo-grupo">Histórico de nível</h3>
            <ul class="eq-prof" aria-label="Histórico de nível">
              @for (n of d.niveis; track n.uuid) {
                <li><span class="cell-main">@if (n.nivelAnterior) { {{ n.nivelAnterior }} → } @else { Ativada no }{{ n.nivelNovo }}</span><span class="cell-sub">{{ dataHora(n.mudadoEm) }} · {{ n.justificativa }}</span></li>
              }
            </ul>
            <h3 class="mo-grupo">Avaliações de impacto</h3>
            @if (d.avaliacoes.length) {
              <ul class="eq-prof" aria-label="Avaliações de impacto">
                @for (a of d.avaliacoes; track a.uuid) {
                  <li>
                    <span class="cell-main">{{ dataHora(a.avaliadaEm) }} <span [class]="'tag tag--' + impactos[a.nivelImpacto].classe">{{ impactos[a.nivelImpacto].rotulo }}</span></span>
                    <span class="cell-sub">@if (a.populacaoAfetada != null) { {{ a.populacaoAfetada }} pessoas afetadas · }@if (a.unidadesAfetadas.length) { {{ a.unidadesAfetadas.length }} unidade(s): {{ a.unidadesAfetadas.join(', ') }} · }@if (a.leitosIndisponiveis != null) { {{ a.leitosIndisponiveis }} leito(s) indisponível(is) · }@if (a.profissionaisIndisponiveis != null) { {{ a.profissionaisIndisponiveis }} profissional(is) indisponível(is) }</span>
                    @if (a.medicamentosCriticos) { <span class="cell-sub">Medicamentos críticos: {{ a.medicamentosCriticos }}</span> }
                    @if (a.equipamentos) { <span class="cell-sub">Equipamentos: {{ a.equipamentos }}</span> }
                    @if (a.transporte) { <span class="cell-sub">Transporte: {{ a.transporte }}</span> }
                    @if (a.infraestrutura) { <span class="cell-sub">Infraestrutura: {{ a.infraestrutura }}</span> }
                    @if (a.observacao) { <span class="cell-sub">{{ a.observacao }}</span> }
                  </li>
                }
              </ul>
            } @else { <p class="cell-sub">Nenhuma avaliação ainda.</p> }
          </div>
        </div>
      </section>
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" [larga]="g.tipo === 'evento' || g.tipo === 'territorio' || g.tipo === 'avaliacao'" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @switch (g.tipo) {
              @case ('evento') {
                <div class="dw-row">
                  <div class="dw-field"><label for="f-em-tipo">Tipo <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-em-tipo" (change)="campo.set({ ...campo(), tipoId: $any($event.target).value })">
                      <option value="" [selected]="!campo()['tipoId']">Escolha…</option>
                      @for (t of tiposAtivos(); track t.uuid) { <option [value]="t.uuid" [selected]="campo()['tipoId'] === t.uuid">{{ t.nome }}@if (t.cobrade) { · {{ t.cobrade }} }</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-em-fonte">Fonte <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-em-fonte" (change)="campo.set({ ...campo(), fonte: $any($event.target).value })">
                      <option value="" [selected]="!campo()['fonte']">Escolha…</option>
                      @for (f of listaFontes; track f) { <option [value]="f" [selected]="campo()['fonte'] === f">{{ fontes[f] }}</option> }
                    </select></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-em-local">Local<span class="opt">opcional</span></label><input id="f-em-local" type="text" maxlength="300" [value]="campo()['local'] ?? ''" (input)="set('local', $event)" placeholder="Ex.: Bairro Ribeirinho" /></div>
                  <div class="dw-field"><label for="f-em-ocorrido">Ocorrido em<span class="opt">padrão: agora</span></label><input id="f-em-ocorrido" type="datetime-local" [value]="campo()['ocorridoEm'] ?? ''" (input)="set('ocorridoEm', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-em-descricao">Descrição <span class="req" aria-hidden="true">*</span></label><textarea id="f-em-descricao" rows="3" maxlength="2000" [value]="campo()['descricao'] ?? ''" (input)="set('descricao', $event)"></textarea></div>
                @if (campo()['surtoId']) { <div class="box box--info"><div>Ligado ao surto da Vigilância.</div></div> }
                <p class="cell-sub">Área do evento (opcional): desenhe o polígono no mapa.</p>
                <app-mapa [edicao]="edicao()" [altura]="260" rotulo="Área do evento" (desenho)="desenho.set($event)" />
              }
              @case ('descartar') {
                <dl class="ro"><div><dt>Evento</dt><dd>{{ g.evento.tipoNome }} · {{ g.evento.descricao }}</dd></div></dl>
                <div class="dw-field"><label for="f-em-motivo">Motivo do descarte <span class="req" aria-hidden="true">*</span></label><input id="f-em-motivo" type="text" maxlength="500" [value]="campo()['motivo'] ?? ''" (input)="set('motivo', $event)" /></div>
              }
              @case ('converter') {
                <dl class="ro"><div><dt>Evento</dt><dd>{{ g.evento.tipoNome }} · {{ g.evento.descricao }}</dd></div></dl>
                <div class="dw-field"><label for="f-em-nome">Nome da emergência <span class="req" aria-hidden="true">*</span></label><input id="f-em-nome" type="text" maxlength="200" [value]="campo()['nome'] ?? ''" (input)="set('nome', $event)" placeholder="Ex.: Enchente Região Norte" /></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-em-nivel">Nível inicial <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-em-nivel" (change)="campo.set({ ...campo(), nivelId: $any($event.target).value })">
                      <option value="" [selected]="!campo()['nivelId']">Escolha…</option>
                      @for (n of niveisAtivos(); track n.uuid) { <option [value]="n.uuid" [selected]="campo()['nivelId'] === n.uuid">{{ n.nome }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-em-responsavel">Matrícula do responsável<span class="opt">opcional</span></label><input id="f-em-responsavel" type="text" [value]="campo()['responsavel'] ?? ''" (input)="set('responsavel', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-em-justificativa">Justificativa <span class="req" aria-hidden="true">*</span></label><textarea id="f-em-justificativa" rows="3" maxlength="1000" [value]="campo()['justificativa'] ?? ''" (input)="set('justificativa', $event)"></textarea></div>
                <div class="box box--info"><div>O território afetado começa pela área do evento e pelas microáreas que ela atinge.</div></div>
                @if (perfilDoEvento(g.evento) === 'EVENTO_DE_MASSA') {
                  <div class="dw-field"><label class="em-check" for="f-em-planejada"><input type="checkbox" id="f-em-planejada" [checked]="campo()['planejada'] !== 'nao'" (change)="campo.set({ ...campo(), planejada: $any($event.target).checked ? 'sim' : 'nao' })" /> Planejar: o evento ainda não começou</label>
                    <span class="cell-sub">A emergência fica planejada: preencha os dados do evento e as estruturas, e inicie a operação na hora.</span></div>
                }
                @if (planosSugeridos().length) {
                  <div class="box box--info"><div>Planos vigentes de {{ g.evento.tipoNome }}: @for (p of planosSugeridos(); track p.uuid; let ultimo = $last) { {{ p.nome }}@if (!ultimo) { · } }. Ative depois de converter.</div></div>
                }
              }
              @case ('nivel') {
                <dl class="ro"><div><dt>Nível atual</dt><dd>{{ g.emergencia.nivelNome }}</dd></div></dl>
                <div class="dw-field"><label for="f-em-nivel">Novo nível <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-em-nivel" (change)="campo.set({ ...campo(), nivelId: $any($event.target).value })">
                    <option value="" [selected]="!campo()['nivelId']">Escolha…</option>
                    @for (n of niveisAtivos(); track n.uuid) { @if (n.uuid !== g.emergencia.nivelId) { <option [value]="n.uuid" [selected]="campo()['nivelId'] === n.uuid">{{ n.nome }}</option> } }
                  </select></div>
                <div class="dw-field"><label for="f-em-justificativa">Justificativa <span class="req" aria-hidden="true">*</span></label><textarea id="f-em-justificativa" rows="3" maxlength="1000" [value]="campo()['justificativa'] ?? ''" (input)="set('justificativa', $event)"></textarea></div>
              }
              @case ('territorio') {
                <p class="cell-sub">Desenhe ou ajuste o polígono. As microáreas do Território que ele atinge entram no território afetado.</p>
                <app-mapa [edicao]="edicao()" [altura]="380" rotulo="Território afetado" (desenho)="desenho.set($event)" />
              }
              @case ('referencias') {
                <p class="cell-sub">Registre o ato publicado; declarar é do poder público e da Defesa Civil.</p>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-em-espin">ESPIN · número<span class="opt">opcional</span></label><input id="f-em-espin" type="text" maxlength="60" [value]="campo()['espinNumero'] ?? ''" (input)="set('espinNumero', $event)" /></div>
                  <div class="dw-field"><label for="f-em-espin-data">ESPIN · data</label><input id="f-em-espin-data" type="date" [value]="campo()['espinData'] ?? ''" (input)="set('espinData', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-em-decreto-tipo">Decreto<span class="opt">opcional</span></label>
                  <select id="f-em-decreto-tipo" (change)="campo.set({ ...campo(), decretoTipo: $any($event.target).value })">
                    <option value="" [selected]="!campo()['decretoTipo']">Nenhum</option>
                    @for (t of listaDecretos; track t) { <option [value]="t" [selected]="campo()['decretoTipo'] === t">{{ decretos[t] }}</option> }
                  </select></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-em-decreto">Número do decreto</label><input id="f-em-decreto" type="text" maxlength="60" [value]="campo()['decretoNumero'] ?? ''" (input)="set('decretoNumero', $event)" /></div>
                  <div class="dw-field"><label for="f-em-decreto-data">Data do decreto</label><input id="f-em-decreto-data" type="date" [value]="campo()['decretoData'] ?? ''" (input)="set('decretoData', $event)" /></div>
                </div>
              }
              @case ('avaliacao') {
                @if (sugestao(); as s) {
                  <div class="box box--info"><div>Sugestão do sistema: {{ s.populacaoAfetada ?? '—' }} pessoas nas {{ s.microareas }} microárea(s), {{ s.unidades.length }} unidade(s) na área e {{ s.leitosIndisponiveis }} leito(s) fora de uso. Confira e ajuste.</div></div>
                }
                <div class="dw-row">
                  <div class="dw-field"><label for="f-em-populacao">População afetada</label><input id="f-em-populacao" type="number" min="0" [value]="campo()['populacao'] ?? ''" (input)="set('populacao', $event)" /></div>
                  <div class="dw-field"><label for="f-em-impacto">Nível de impacto <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-em-impacto" (change)="campo.set({ ...campo(), impacto: $any($event.target).value })">
                      <option value="" [selected]="!campo()['impacto']">Escolha…</option>
                      @for (i of listaImpactos; track i) { <option [value]="i" [selected]="campo()['impacto'] === i">{{ impactos[i].rotulo }}</option> }
                    </select></div>
                </div>
                @if (sugestao()?.unidades?.length) {
                  <fieldset class="dw-field"><legend>Unidades afetadas</legend>
                    @for (u of sugestao()!.unidades; track u.uuid) {
                      <label class="em-check"><input type="checkbox" [checked]="unidadesMarcadas().has(u.uuid)" (change)="alternarUnidade(u.uuid)" /> {{ u.nome }} <span class="cell-sub">{{ u.leitosIndisponiveis }}/{{ u.leitos }} leitos fora de uso</span></label>
                    }
                  </fieldset>
                }
                <div class="dw-row">
                  <div class="dw-field"><label for="f-em-leitos">Leitos indisponíveis</label><input id="f-em-leitos" type="number" min="0" [value]="campo()['leitos'] ?? ''" (input)="set('leitos', $event)" /></div>
                  <div class="dw-field"><label for="f-em-profissionais">Profissionais indisponíveis</label><input id="f-em-profissionais" type="number" min="0" [value]="campo()['profissionais'] ?? ''" (input)="set('profissionais', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-em-medicamentos">Medicamentos críticos<span class="opt">opcional</span></label><input id="f-em-medicamentos" type="text" maxlength="1000" [value]="campo()['medicamentos'] ?? ''" (input)="set('medicamentos', $event)" /></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-em-equipamentos">Equipamentos<span class="opt">opcional</span></label><input id="f-em-equipamentos" type="text" maxlength="1000" [value]="campo()['equipamentos'] ?? ''" (input)="set('equipamentos', $event)" /></div>
                  <div class="dw-field"><label for="f-em-transporte">Transporte<span class="opt">opcional</span></label><input id="f-em-transporte" type="text" maxlength="1000" [value]="campo()['transporte'] ?? ''" (input)="set('transporte', $event)" /></div>
                </div>
                <div class="dw-field"><label for="f-em-infraestrutura">Infraestrutura<span class="opt">opcional</span></label><input id="f-em-infraestrutura" type="text" maxlength="1000" [value]="campo()['infraestrutura'] ?? ''" (input)="set('infraestrutura', $event)" /></div>
                <div class="dw-field"><label for="f-em-observacao">Observação<span class="opt">opcional</span></label><textarea id="f-em-observacao" rows="2" maxlength="2000" [value]="campo()['observacao'] ?? ''" (input)="set('observacao', $event)"></textarea></div>
              }
              @case ('ativarPlano') {
                <dl class="ro"><div><dt>Emergência</dt><dd>{{ g.emergencia.nome }} · {{ g.emergencia.tipoNome }} · {{ g.emergencia.nivelNome }}</dd></div></dl>
                <div class="dw-field"><label for="f-em-plano">Plano vigente <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-em-plano" (change)="campo.set({ ...campo(), planoId: $any($event.target).value })">
                    <option value="" [selected]="!campo()['planoId']">Escolha…</option>
                    @for (p of planosSugeridos(); track p.uuid) { <option [value]="p.uuid" [selected]="campo()['planoId'] === p.uuid">{{ p.nome }} · v{{ p.versao }}@if (!cobre(p, g.emergencia)) { (não cobre o nível atual) }</option> }
                  </select>
                  @if (!planosSugeridos().length) { <p class="cell-sub">Nenhum plano vigente de {{ g.emergencia.tipoNome }}. Cadastre na aba Planos de contingência.</p> }
                </div>
                <div class="dw-field"><label for="f-em-autoridade">Autoridade <span class="req" aria-hidden="true">*</span></label><input id="f-em-autoridade" type="text" maxlength="200" [value]="campo()['autoridade'] ?? ''" (input)="set('autoridade', $event)" placeholder="Ex.: Secretário de Saúde" /></div>
                <div class="dw-field"><label for="f-em-justificativa">Justificativa <span class="req" aria-hidden="true">*</span></label><textarea id="f-em-justificativa" rows="3" maxlength="1000" [value]="campo()['justificativa'] ?? ''" (input)="set('justificativa', $event)"></textarea></div>
              }
              @case ('desativarPlano') {
                <dl class="ro"><div><dt>Plano</dt><dd>{{ g.plano.nome }} · v{{ g.plano.versao }}</dd></div></dl>
                <div class="dw-field"><label for="f-em-justificativa">Justificativa <span class="req" aria-hidden="true">*</span></label><textarea id="f-em-justificativa" rows="3" maxlength="1000" [value]="campo()['justificativa'] ?? ''" (input)="set('justificativa', $event)"></textarea></div>
              }
              @case ('encerrar') {
                <div class="dw-field"><label for="f-em-motivo">Motivo do encerramento <span class="req" aria-hidden="true">*</span></label><textarea id="f-em-motivo" rows="3" maxlength="1000" [value]="campo()['motivo'] ?? ''" (input)="set('motivo', $event)"></textarea></div>
                @if (g.emergencia.situacao === 'PLANEJADA') { <div class="box box--info"><div>O evento é cancelado antes da operação; as estruturas planejadas são desativadas.</div></div> }
                @if (g.emergencia.situacao === 'ATIVA') { <div class="box box--info"><div>Encerrar direto conclui as mobilizações e desativa a capacidade e os planos. Para controlar o retorno passo a passo, use "Iniciar desmobilização".</div></div> }
              }
              @case ('desmobilizar') {
                <div class="box box--info"><div>Conclui as mobilizações em aberto (a Escala deixa de aceitar turnos novos sem lotação), desativa a capacidade emergencial e os planos. Depois, só se conclui o que ficou aberto e se escreve o relatório pós-evento.</div></div>
                <div class="dw-field"><label for="f-em-responsavel-desmob">Responsável <span class="req" aria-hidden="true">*</span></label><input id="f-em-responsavel-desmob" type="text" maxlength="200" [value]="campo()['responsavel'] ?? ''" (input)="set('responsavel', $event)" placeholder="Ex.: Secretário de Saúde" /></div>
                <div class="dw-field"><label for="f-em-justificativa">Justificativa <span class="req" aria-hidden="true">*</span></label><textarea id="f-em-justificativa" rows="3" maxlength="1000" [value]="campo()['justificativa'] ?? ''" (input)="set('justificativa', $event)"></textarea></div>
              }
              @case ('operacao') {
                @if (g.emergencia.eventoMassa; as m) { <dl class="ro"><div><dt>Evento</dt><dd>{{ m.local }} · previsto para {{ dataHora(m.inicioPrevisto) }}</dd></div></dl> }
                <div class="box box--info"><div>A emergência passa a ativa a partir de agora: entram as ações, as mobilizações, os planos e a abertura das estruturas.</div></div>
                <div class="dw-field"><label for="f-em-justificativa">Justificativa <span class="req" aria-hidden="true">*</span></label><textarea id="f-em-justificativa" rows="3" maxlength="1000" [value]="campo()['justificativa'] ?? ''" (input)="set('justificativa', $event)"></textarea></div>
              }
              @case ('cancelarRestantes') {
                <div class="dw-field"><label for="f-em-motivo">Motivo <span class="req" aria-hidden="true">*</span></label><input id="f-em-motivo" type="text" maxlength="500" [value]="campo()['motivo'] ?? ''" (input)="set('motivo', $event)" /></div>
              }
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar' }}</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .em-detalhe { padding: var(--s-5); margin-top: var(--s-4); }
    .em-cab { display: flex; justify-content: space-between; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-3); }
    .em-titulo { margin: 0 0 var(--s-1); font-size: 1.25rem; }
    .em-grade { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: var(--s-5); }
    .em-check { display: flex; gap: var(--s-2); align-items: center; margin: var(--s-1) 0; font-weight: 400; }
    .dw-field .em-check input { width: auto; min-height: 0; padding: 0; }
    .em-planos { margin-bottom: var(--s-4); }
    .em-acoes { margin: var(--s-1) 0; padding-left: var(--s-4); }
    .em-desmob { margin-bottom: var(--s-3); }
    .em-pendencias { margin-bottom: var(--s-3); }
  `,
})
export class Emergencias {
  private readonly service = inject(EmergenciaService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  protected readonly fontes = FONTES_EVENTO;
  protected readonly listaFontes = Object.keys(FONTES_EVENTO) as FonteEventoEmergencial[];
  protected readonly situacoesEvento = SITUACOES_EVENTO;
  protected readonly impactos = NIVEIS_IMPACTO;
  protected readonly listaImpactos = Object.keys(NIVEIS_IMPACTO) as NivelImpacto[];
  protected readonly decretos = DECRETOS;
  protected readonly tiposRegistro = TIPOS_REGISTRO_ATIVACAO;
  protected readonly planosSugeridos = signal<PlanoContingenciaDto[]>([]);
  protected readonly listaDecretos = Object.keys(DECRETOS) as TipoDecretoEmergencia[];

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('ativas');
  protected readonly erro = signal<string | null>(null);
  protected readonly tipos = signal<TipoEmergenciaDto[]>([]);
  protected readonly niveis = signal<NivelEmergenciaDto[]>([]);
  protected readonly eventos = signal<EventoEmergencialDto[]>([]);
  protected readonly emergencias = signal<EmergenciaDto[]>([]);
  protected readonly detalhe = signal<EmergenciaDto | null>(null);
  protected readonly sugestao = signal<SugestaoImpactoDto | null>(null);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly campo = signal<Record<string, string>>({});
  protected readonly edicao = signal<EdicaoMapa | null>(null);
  protected readonly desenho = signal<string | null>(null);
  protected readonly unidadesMarcadas = signal<Set<string>>(new Set());

  protected readonly ativas = computed(() => this.emergencias().filter((e) => e.situacao === 'ATIVA'));
  protected readonly emDesmobilizacao = computed(() => this.emergencias().filter((e) => e.situacao === 'EM_DESMOBILIZACAO'));
  protected readonly planejadas = computed(() => this.emergencias().filter((e) => e.situacao === 'PLANEJADA'));
  protected readonly lista = computed(() => {
    switch (this.aba()) {
      case 'ativas': return this.ativas();
      case 'planejadas': return this.planejadas();
      case 'desmobilizacao': return this.emDesmobilizacao();
      default: return this.encerradas();
    }
  });
  protected readonly vazioDaLista = computed(() => ({ ativas: 'Nenhuma emergência ativa.', planejadas: 'Nenhuma emergência planejada.', desmobilizacao: 'Nenhuma emergência em desmobilização.' } as Record<string, string>)[this.aba()] ?? 'Nenhuma emergência encerrada.');
  protected readonly rotuloDaLista = computed(() => ({ ativas: 'Emergências ativas', planejadas: 'Emergências planejadas', desmobilizacao: 'Emergências em desmobilização' } as Record<string, string>)[this.aba()] ?? 'Emergências encerradas');
  protected readonly situacoesEmergencia = SITUACOES_EMERGENCIA;
  protected readonly encerradas = computed(() => this.emergencias().filter((e) => e.situacao === 'ENCERRADA'));
  protected readonly eventosAbertos = computed(() => this.eventos().filter((e) => e.situacao === 'REGISTRADO' || e.situacao === 'MONITORADO').length);
  protected readonly tiposAtivos = computed(() => this.tipos().filter((t) => t.ativo));
  protected readonly niveisAtivos = computed(() => this.niveis().filter((n) => n.ativo));
  protected readonly poligonos = computed<PoligonoMapa[]>(() => {
    const d = this.detalhe();
    if (!d) return [];
    const lista: PoligonoMapa[] = d.microareas.filter((m) => m.geometria)
      .map((m) => ({ id: m.uuid, geometria: m.geometria!, rotulo: m.codigo, descricao: `Microárea ${m.codigo}, ${m.areaNome}`, tipo: 'microarea' as const }));
    if (d.geometria) lista.unshift({ id: 'area', geometria: d.geometria, descricao: 'Área afetada', tipo: 'area', cor: '#dc2626' });
    return lista;
  });
  /** As estruturas temporárias com localização (ADR-0160). */
  protected readonly pinos = computed<PinoMapa[]>(() => (this.detalhe()?.estruturas ?? []).flatMap((s) => s.latitude != null && s.longitude != null
    ? [{ id: s.uuid, lat: s.latitude, lng: s.longitude, sigla: SIGLAS_ESTRUTURA[s.tipo], classe: 'unit-pin t-closed', descricao: `${TIPOS_ESTRUTURA[s.tipo]}: ${s.nome}` }]
    : []));

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.carregarCatalogos();
    this.carregar();
    const q = this.route.snapshot.queryParamMap;
    if (q.get('surtoId')) {
      this.aba.set('eventos');
      this.abrirEvento(q.get('surtoId'));
    }
    if (q.get('emergencia')) this.selecionar(q.get('emergencia')!);
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  /** A aba da lista em que a emergência aparece. */
  private abaDa(d: EmergenciaDto): Aba {
    return d.situacao === 'ATIVA' ? 'ativas' : d.situacao === 'PLANEJADA' ? 'planejadas' : d.situacao === 'EM_DESMOBILIZACAO' ? 'desmobilizacao' : 'encerradas';
  }

  protected perfilDoEvento(e: EventoEmergencialDto): string | null {
    return this.tipos().find((t) => t.uuid === e.tipoId)?.perfil ?? null;
  }

  protected acoesAbertas(d: EmergenciaDto): number {
    return d.acoes.filter((a) => a.situacao === 'PENDENTE' || a.situacao === 'EM_EXECUCAO').length;
  }

  protected aposCoordenacao(d: EmergenciaDto): void {
    this.detalhe.set(d);
    this.carregar();
  }

  protected carregarCatalogos(): void {
    this.service.tipos().pipe(catchError(() => of([]))).subscribe((t) => this.tipos.set(t));
    this.service.niveis().pipe(catchError(() => of([]))).subscribe((n) => this.niveis.set(n));
  }

  protected cobre(p: PlanoContingenciaDto, e: EmergenciaDto): boolean {
    return p.niveis.some((n) => n.uuid === e.nivelId);
  }

  private sugerirPlanos(tipoId: string): void {
    this.planosSugeridos.set([]);
    this.service.planos({ vigentes: true, tipoId }).pipe(catchError(() => of([]))).subscribe((l) => this.planosSugeridos.set(l));
  }

  protected abrirAtivarPlano(d: EmergenciaDto): void {
    this.sugerirPlanos(d.tipoId);
    this.abrir({ tipo: 'ativarPlano', emergencia: d });
  }

  protected abrirDesativarPlano(d: EmergenciaDto, p: PlanoAtivoDto): void {
    this.abrir({ tipo: 'desativarPlano', emergencia: d, plano: p });
  }

  protected classe(ordem: number): string {
    return classeDoNivel(ordem);
  }

  protected set(chave: string, ev: Event): void {
    this.campo.set({ ...this.campo(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected titulo(g: Gaveta): string {
    switch (g.tipo) {
      case 'evento': return 'Registrar evento emergencial';
      case 'descartar': return 'Descartar o evento';
      case 'converter': return 'Converter em emergência';
      case 'nivel': return 'Mudar o nível de resposta';
      case 'territorio': return 'Território afetado';
      case 'referencias': return 'ESPIN e decreto';
      case 'avaliacao': return 'Avaliação de impacto';
      case 'ativarPlano': return 'Ativar plano de contingência';
      case 'desmobilizar': return 'Iniciar a desmobilização';
      case 'operacao': return 'Iniciar a operação';
      case 'cancelarRestantes': return 'Cancelar as ações restantes';
      case 'desativarPlano': return 'Desativar o plano';
      default: return g.emergencia.situacao === 'PLANEJADA' ? 'Cancelar o evento' : 'Encerrar a emergência';
    }
  }

  protected abrir(g: Gaveta, campos: Record<string, string> = {}): void {
    this.erroGaveta.set(null);
    this.campo.set(campos);
    this.gaveta.set(g);
  }

  protected abrirEvento(surtoId: string | null): void {
    this.desenho.set(null);
    this.edicao.set({ id: 'evento', geometria: null, nome: 'a área do evento' });
    const epidemia = this.tipos().find((t) => t.codigo === 'EPIDEMIA');
    this.abrir({ tipo: 'evento' }, surtoId ? { surtoId, fonte: 'VIGILANCIA', tipoId: epidemia?.uuid ?? '' } : {});
  }

  protected abrirConverter(e: EventoEmergencialDto): void {
    this.sugerirPlanos(e.tipoId);
    this.abrir({ tipo: 'converter', evento: e }, { nome: `${e.tipoNome}${e.local ? ' · ' + e.local : ''}` });
  }

  protected abrirNivel(d: EmergenciaDto): void {
    this.abrir({ tipo: 'nivel', emergencia: d });
  }

  protected abrirTerritorio(d: EmergenciaDto): void {
    this.desenho.set(d.geometria);
    this.edicao.set({ id: 'territorio-' + d.uuid, geometria: d.geometria, nome: 'o território afetado' });
    this.abrir({ tipo: 'territorio', emergencia: d });
  }

  protected abrirReferencias(d: EmergenciaDto): void {
    this.abrir({ tipo: 'referencias', emergencia: d }, {
      espinNumero: d.espinNumero ?? '', espinData: d.espinData ?? '', decretoTipo: d.decretoTipo ?? '', decretoNumero: d.decretoNumero ?? '',
      decretoData: d.decretoData ?? '',
    });
  }

  protected abrirAvaliacao(d: EmergenciaDto): void {
    this.sugestao.set(null);
    this.unidadesMarcadas.set(new Set());
    this.abrir({ tipo: 'avaliacao', emergencia: d });
    this.service.sugestao(d.uuid).pipe(catchError(() => of(null))).subscribe((s) => {
      this.sugestao.set(s);
      if (!s) return;
      this.unidadesMarcadas.set(new Set(s.unidades.map((u) => u.uuid)));
      this.campo.set({ ...this.campo(), populacao: s.populacaoAfetada != null ? String(s.populacaoAfetada) : '', leitos: String(s.leitosIndisponiveis) });
    });
  }

  protected abrirEncerrar(d: EmergenciaDto): void {
    this.abrir({ tipo: 'encerrar', emergencia: d });
  }

  protected alternarUnidade(id: string): void {
    const s = new Set(this.unidadesMarcadas());
    if (s.has(id)) s.delete(id);
    else s.add(id);
    this.unidadesMarcadas.set(s);
  }

  protected monitorar(e: EventoEmergencialDto): void {
    this.service.situacaoDoEvento(e.uuid, 'MONITORADO', null).subscribe({ next: () => this.carregar(), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  protected selecionar(uuid: string): void {
    this.service.emergencia(uuid).subscribe({
      next: (d) => {
        this.detalhe.set(d);
        if (['ativas', 'planejadas', 'desmobilizacao', 'encerradas'].includes(this.aba())) this.aba.set(this.abaDa(d));
      },
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  protected salvar(g: Gaveta): void {
    if (this.enviando()) return;
    const c = this.campo();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    const n = (k: string) => (t(k) == null ? null : Number(c[k]));
    let req: Observable<unknown>;
    let falta: string | null = null;
    switch (g.tipo) {
      case 'evento':
        if (!t('tipoId') || !t('fonte') || !t('descricao')) falta = 'Informe o tipo, a fonte e a descrição.';
        req = this.service.registrarEvento({
          tipoId: c['tipoId'], fonte: c['fonte'] as FonteEventoEmergencial, descricao: t('descricao') ?? '', local: t('local'),
          ocorridoEm: t('ocorridoEm') ? new Date(c['ocorridoEm']).toISOString() : null, geometria: this.desenho(), surtoId: t('surtoId'),
        });
        break;
      case 'descartar':
        if (!t('motivo')) falta = 'Informe o motivo do descarte.';
        req = this.service.situacaoDoEvento(g.evento.uuid, 'DESCARTADO', t('motivo'));
        break;
      case 'converter':
        if (!t('nome') || !t('nivelId') || !t('justificativa')) falta = 'Informe o nome, o nível e a justificativa.';
        req = this.service.converter(g.evento.uuid, {
          nome: t('nome') ?? '', nivelId: c['nivelId'], justificativa: t('justificativa') ?? '', responsavelMatricula: t('responsavel'),
          planejada: this.perfilDoEvento(g.evento) === 'EVENTO_DE_MASSA' && c['planejada'] !== 'nao',
        });
        break;
      case 'nivel':
        if (!t('nivelId') || !t('justificativa')) falta = 'Escolha o nível e justifique.';
        req = this.service.mudarNivel(g.emergencia.uuid, c['nivelId'], t('justificativa') ?? '');
        break;
      case 'territorio':
        req = this.service.territorio(g.emergencia.uuid, this.desenho());
        break;
      case 'referencias':
        req = this.service.referencias(g.emergencia.uuid, {
          espinNumero: t('espinNumero'), espinData: t('espinData'), decretoTipo: (t('decretoTipo') as TipoDecretoEmergencia | null),
          decretoNumero: t('decretoNumero'), decretoData: t('decretoData'),
        });
        break;
      case 'desmobilizar':
        if (!t('responsavel') || !t('justificativa')) falta = 'Informe o responsável e a justificativa.';
        req = this.service.desmobilizar(g.emergencia.uuid, t('responsavel') ?? '', t('justificativa') ?? '');
        break;
      case 'operacao':
        if (!t('justificativa')) falta = 'Justifique o início da operação.';
        req = this.service.iniciarOperacao(g.emergencia.uuid, t('justificativa') ?? '');
        break;
      case 'cancelarRestantes':
        if (!t('motivo')) falta = 'Informe o motivo.';
        req = this.service.cancelarAcoesRestantes(g.emergencia.uuid, t('motivo') ?? '');
        break;
      case 'ativarPlano':
        if (!t('planoId') || !t('autoridade') || !t('justificativa')) falta = 'Escolha o plano e informe a autoridade e a justificativa.';
        req = this.service.ativarPlano(g.emergencia.uuid, c['planoId'], t('autoridade') ?? '', t('justificativa') ?? '');
        break;
      case 'desativarPlano':
        if (!t('justificativa')) falta = 'Justifique a desativação.';
        req = this.service.desativarPlano(g.emergencia.uuid, g.plano.ativacaoId, t('justificativa') ?? '');
        break;
      case 'avaliacao':
        if (!t('impacto')) falta = 'Escolha o nível de impacto.';
        req = this.service.avaliar(g.emergencia.uuid, {
          populacaoAfetada: n('populacao'), unidadeIds: [...this.unidadesMarcadas()], profissionaisIndisponiveis: n('profissionais'),
          leitosIndisponiveis: n('leitos'), medicamentosCriticos: t('medicamentos'), equipamentos: t('equipamentos'), transporte: t('transporte'),
          infraestrutura: t('infraestrutura'), nivelImpacto: c['impacto'] as NivelImpacto, observacao: t('observacao'),
        });
        break;
      default:
        if (!t('motivo')) falta = 'Informe o motivo do encerramento.';
        req = this.service.encerrar(g.emergencia.uuid, t('motivo') ?? '');
    }
    if (falta) {
      this.erroGaveta.set(falta);
      return;
    }
    this.enviando.set(true);
    req.subscribe({
      next: (r) => {
        this.enviando.set(false);
        this.gaveta.set(null);
        const d = r as EmergenciaDto;
        if (d && 'nivelOrdem' in d) {
          this.detalhe.set(d);
          this.aba.set(this.abaDa(d));
        }
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected data(iso: string | null): string {
    if (!iso) return '—';
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  private carregar(): void {
    this.service.emergencias().subscribe({ next: (l) => this.emergencias.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
    this.service.eventos().pipe(catchError(() => of([]))).subscribe((l) => this.eventos.set(l));
  }
}

const SIGLAS_ESTRUTURA: Record<string, string> = { POSTO_MEDICO: 'PM', TENDA_HIDRATACAO: 'TH', HOSPITAL_DE_CAMPANHA: 'HC', OUTRO: 'EST' };

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
