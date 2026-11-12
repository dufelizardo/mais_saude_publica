import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../core/models/auth';
import { TipoDocumentoDto } from '../../core/models/documento';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  AtividadeDto,
  CANAIS_DENUNCIA,
  CanalDenuncia,
  DenunciaSanitariaDto,
  EstabelecimentoDetalheDto,
  EstabelecimentoReguladoDto,
  GRAVIDADES,
  GrauRiscoSanitario,
  GravidadeIrregularidade,
  InspecaoSanitariaDto,
  LicencaSanitariaDto,
  MOTIVOS_INSPECAO,
  MOTIVOS_PENDENCIA_LICENCA,
  MOTIVOS_PENDENCIA_PROCESSO,
  MotivoInspecao,
  PendenciasVisaDto,
  RESPOSTAS_ROTEIRO,
  RESULTADOS_INSPECAO,
  RISCOS,
  RespostaRoteiro,
  ResultadoInspecao,
  RoteiroInspecaoDto,
  SITUACOES_DENUNCIA,
  SITUACOES_IRREGULARIDADE,
  SITUACOES_LICENCA,
  SituacaoDenuncia,
} from '../../core/models/vigilancia-sanitaria';
import { AuthService } from '../../core/services/auth';
import { DocumentoService } from '../../core/services/documento';
import { VigilanciaSanitariaService } from '../../core/services/vigilancia-sanitaria';
import { Drawer } from '../../shared/drawer/drawer';
import { ProcessosSanitarios } from './processos-sanitarios';

type Aba = 'estabelecimentos' | 'inspecoes' | 'denuncias' | 'licencas' | 'processos' | 'medidas' | 'pendencias';
type Gaveta =
  | { tipo: 'estabelecimento'; atual: EstabelecimentoReguladoDto | null }
  | { tipo: 'detalhe'; id: string }
  | { tipo: 'licenca'; estabelecimento: EstabelecimentoReguladoDto }
  | { tipo: 'inspecao' }
  | { tipo: 'denuncia' }
  | { tipo: 'conclusao'; denuncia: DenunciaSanitariaDto };
type IrregularidadeNova = { itemCodigo: string | null; descricao: string; enquadramento: string; gravidade: GravidadeIrregularidade; prazoCorrecao: string };

/**
 * Vigilância Sanitária, fatia V3a (ADR-0199): os estabelecimentos regulados com as atividades e o risco, a licença sanitária e a
 * dispensa, as inspeções com o roteiro e as irregularidades (e o termo em PDF), as denúncias e as pendências.
 */
@Component({
  selector: 'app-vigilancia-sanitaria',
  imports: [Drawer, ProcessosSanitarios],
  template: `
    <header class="page-head">
      <div>
        <h1>Vigilância sanitária</h1>
        <p>Estabelecimentos regulados, licenciamento, inspeções e denúncias</p>
      </div>
      <div class="page-actions">
        @if (podeAlguma('VISA.INSPECIONAR', 'VISA.GERENCIAR')) { <button type="button" class="btn btn--ghost" (click)="abrirEstabelecimento(null)">Novo estabelecimento</button> }
        @if (pode('VISA.GERENCIAR')) { <button type="button" class="btn btn--ghost" (click)="abrirDenuncia()">Registrar denúncia</button> }
        @if (pode('VISA.INSPECIONAR')) { <button type="button" class="btn" (click)="abrirInspecao({})" [disabled]="!estabelecimentos().length">Registrar inspeção</button> }
      </div>
    </header>

    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }

    @if (pendencias(); as p) {
      <section class="stats" aria-label="Pendências da vigilância sanitária">
        <div class="stat"><div><div class="stat__lbl">Licenças pendentes</div><div class="stat__val mono">{{ p.licencas.length }}</div><div class="stat__sub">sem licença, vencida ou vencendo</div></div></div>
        <div class="stat"><div><div class="stat__lbl">Prazos de correção vencidos</div><div class="stat__val mono">{{ p.prazosVencidos.length }}</div><div class="stat__sub">irregularidades abertas</div></div></div>
        <div class="stat"><div><div class="stat__lbl">Denúncias paradas</div><div class="stat__val mono">{{ p.denunciasParadas.length }}</div><div class="stat__sub">há mais de 30 dias</div></div></div>
        <div class="stat"><div><div class="stat__lbl">Processos com pendência</div><div class="stat__val mono">{{ p.processos.length }}</div><div class="stat__sub">ciência, revelia, decisão ou encerramento</div></div></div>
      </section>
    }

    <div class="tabs" role="tablist" aria-label="Seções da vigilância sanitária">
      <button type="button" role="tab" id="tab-estabelecimentos" [attr.aria-selected]="aba() === 'estabelecimentos'" (click)="aba.set('estabelecimentos')">Estabelecimentos <span class="cnt">{{ estabelecimentos().length }}</span></button>
      <button type="button" role="tab" id="tab-inspecoes" [attr.aria-selected]="aba() === 'inspecoes'" (click)="aba.set('inspecoes')">Inspeções</button>
      <button type="button" role="tab" id="tab-denuncias" [attr.aria-selected]="aba() === 'denuncias'" (click)="aba.set('denuncias')">Denúncias @if (denunciasAbertas().length) { <span class="cnt">{{ denunciasAbertas().length }}</span> }</button>
      <button type="button" role="tab" id="tab-licencas" [attr.aria-selected]="aba() === 'licencas'" (click)="aba.set('licencas')">Licenças</button>
      <button type="button" role="tab" id="tab-processos" [attr.aria-selected]="aba() === 'processos'" (click)="aba.set('processos')">Processos</button>
      <button type="button" role="tab" id="tab-medidas" [attr.aria-selected]="aba() === 'medidas'" (click)="aba.set('medidas')">Medidas</button>
      <button type="button" role="tab" id="tab-pendencias" [attr.aria-selected]="aba() === 'pendencias'" (click)="aba.set('pendencias')">Pendências</button>
    </div>

    @switch (aba()) {
      @case ('estabelecimentos') {
        <div class="vs-cab">
          <div class="dw-field" style="margin: 0; min-width: 260px;"><label for="f-vs-busca">Buscar</label><input id="f-vs-busca" type="search" placeholder="Nome, fantasia ou CNPJ/CPF" [value]="busca()" (input)="busca.set($any($event.target).value)" /></div>
        </div>
        <div class="tbl-wrap card">
          <table class="vs-tabela" aria-label="Estabelecimentos regulados">
            <thead><tr><th scope="col">Estabelecimento</th><th scope="col">Risco</th><th scope="col">Licença</th><th scope="col">Irregularidades</th><th scope="col">Última inspeção</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (e of estabelecimentosFiltrados(); track e.uuid) {
                <tr>
                  <td><span class="cell-main">{{ e.nomeFantasia ?? e.razaoSocial }}</span><span class="cell-sub mono">{{ doc(e.documento) }}</span><span class="cell-sub">{{ e.endereco }}</span>@if (!e.ativo) { <span class="tag">Inativo</span> }</td>
                  <td><span [class]="'tag tag--' + (e.risco === 'ALTO' ? 'alert' : e.risco === 'MEDIO' ? 'warn' : 'ok')">{{ riscos[e.risco] }}</span></td>
                  <td>@if (e.licencaAtual; as l) { <span [class]="'tag tag--' + tagLicenca(l.situacao)">{{ situacoesLicenca[l.situacao] }}</span><span class="cell-sub mono">{{ l.numero }}@if (l.validade) { · até {{ data(l.validade) }} }</span> } @else { <span class="cell-sub">{{ e.risco === 'BAIXO' ? 'dispensa não registrada' : 'sem licença' }}</span> }</td>
                  <td>@if (e.irregularidadesAbertas) { <span class="tag tag--alert">{{ e.irregularidadesAbertas }} aberta(s)</span> } @else { <span class="cell-sub">nenhuma aberta</span> }</td>
                  <td class="mono">{{ e.ultimaInspecao ? data(e.ultimaInspecao) : '—' }}</td>
                  <td class="row-actions">
                    <button type="button" class="link-btn" (click)="abrirDetalhe(e.uuid)" [attr.aria-label]="'Abrir ' + (e.nomeFantasia ?? e.razaoSocial)">Abrir</button>
                    @if (pode('VISA.INSPECIONAR') && e.ativo) { <button type="button" class="link-btn" (click)="abrirInspecao({ estabelecimentoId: e.uuid })" [attr.aria-label]="'Inspecionar ' + (e.nomeFantasia ?? e.razaoSocial)">Inspecionar</button> }
                    @if (pode('VISA.GERENCIAR') && e.ativo) { <button type="button" class="link-btn" (click)="abrirLicenca(e)" [attr.aria-label]="'Licença de ' + (e.nomeFantasia ?? e.razaoSocial)">{{ e.risco === 'BAIXO' ? 'Dispensa' : 'Licença' }}</button> }
                  </td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhum estabelecimento.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('inspecoes') {
        <div class="tbl-wrap card">
          <table class="vs-tabela" aria-label="Inspeções sanitárias">
            <thead><tr><th scope="col">Data</th><th scope="col">Estabelecimento</th><th scope="col">Motivo</th><th scope="col">Resultado</th><th scope="col">Irregularidades</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (i of inspecoes(); track i.uuid) {
                <tr>
                  <td class="mono">{{ data(i.dataInspecao) }}</td>
                  <td><span class="cell-main">{{ i.estabelecimentoNome }}</span><span class="cell-sub">{{ fiscais(i) }}</span></td>
                  <td>{{ motivos[i.motivo] }}@if (i.denunciaProtocolo) { <span class="cell-sub mono">denúncia {{ i.denunciaProtocolo }}</span> }</td>
                  <td><span [class]="'tag tag--' + tagResultado(i.resultado)">{{ resultados[i.resultado] }}</span>@if (i.assinada) { <span class="cell-sub">assinada</span> }</td>
                  <td>{{ i.irregularidades.length }}@if (i.corrigidas.length) { <span class="cell-sub">{{ i.corrigidas.length }} corrigida(s)</span> }</td>
                  <td class="row-actions"><button type="button" class="link-btn" (click)="baixarTermo(i)" [attr.aria-label]="'Termo da inspeção de ' + i.estabelecimentoNome">Termo</button></td>
                </tr>
              } @empty { <tr><td colspan="6" class="cell-sub">Nenhuma inspeção.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('denuncias') {
        <div class="tbl-wrap card">
          <table class="vs-tabela" aria-label="Denúncias sanitárias">
            <thead><tr><th scope="col">Protocolo</th><th scope="col">Estabelecimento</th><th scope="col">Denúncia</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
            <tbody>
              @for (d of denuncias(); track d.uuid) {
                <tr>
                  <td><span class="cell-main mono">{{ d.protocolo }}</span><span class="cell-sub">{{ canais[d.canal] }} · {{ dataHora(d.recebidaEm) }}</span></td>
                  <td>{{ d.estabelecimentoNome ?? d.estabelecimentoDescricao }}</td>
                  <td><span class="cell-sub" style="white-space: normal;">{{ d.descricao }}</span>@if (d.anonima) { <span class="cell-sub">anônima</span> } @else if (d.denuncianteNome) { <span class="cell-sub">{{ d.denuncianteNome }}@if (d.denuncianteContato) { · {{ d.denuncianteContato }} }</span> }</td>
                  <td><span [class]="'tag tag--' + tagDenuncia(d.situacao)">{{ situacoesDenuncia[d.situacao] }}</span><span class="cell-sub">{{ d.diasEmAberto }} dia(s)@if (d.inspecoes) { · {{ d.inspecoes }} inspeção(ões) }</span>@if (d.conclusao) { <span class="cell-sub">{{ d.conclusao }}</span> }</td>
                  <td class="row-actions">
                    @if (aberta(d)) {
                      @if (pode('VISA.INSPECIONAR') && d.estabelecimentoId) { <button type="button" class="link-btn" (click)="abrirInspecao({ estabelecimentoId: d.estabelecimentoId, motivo: 'DENUNCIA', denunciaId: d.uuid })" [attr.aria-label]="'Inspecionar denúncia ' + d.protocolo">Inspecionar</button> }
                      @if (pode('VISA.GERENCIAR')) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'conclusao', denuncia: d }, { situacao: d.inspecoes ? 'PROCEDENTE' : 'IMPROCEDENTE' })" [attr.aria-label]="'Concluir denúncia ' + d.protocolo">Concluir</button> }
                    }
                  </td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma denúncia.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('licencas') {
        <div class="tbl-wrap card">
          <table class="vs-tabela" aria-label="Licenças sanitárias">
            <thead><tr><th scope="col">Número</th><th scope="col">Estabelecimento</th><th scope="col">Emissão</th><th scope="col">Validade</th><th scope="col">Situação</th></tr></thead>
            <tbody>
              @for (l of licencas(); track l.uuid) {
                <tr>
                  <td class="mono">{{ l.numero }}</td>
                  <td><span class="cell-main">{{ l.estabelecimentoNome }}</span>@if (l.observacao) { <span class="cell-sub">{{ l.observacao }}</span> }</td>
                  <td class="mono">{{ data(l.emitidaEm) }}</td>
                  <td class="mono">{{ l.validade ? data(l.validade) : '—' }}</td>
                  <td><span [class]="'tag tag--' + tagLicenca(l.situacao)">{{ situacoesLicenca[l.situacao] }}</span></td>
                </tr>
              } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma licença.</td></tr> }
            </tbody>
          </table>
        </div>
      }
      @case ('processos') { <app-processos-sanitarios secao="processos" [estabelecimentos]="estabelecimentosAtivos()" [autuar]="autuar()" [podeInspecionar]="pode('VISA.INSPECIONAR')" [podeGerenciar]="pode('VISA.GERENCIAR')" [podeJulgar]="pode('VISA.JULGAR')" (alterou)="carregar()" /> }
      @case ('medidas') { <app-processos-sanitarios secao="medidas" [estabelecimentos]="estabelecimentosAtivos()" [podeInspecionar]="pode('VISA.INSPECIONAR')" [podeGerenciar]="pode('VISA.GERENCIAR')" [podeJulgar]="pode('VISA.JULGAR')" (alterou)="carregar()" /> }
      @case ('pendencias') {
        @if (pendencias(); as p) {
          <h3 class="mo-grupo">Licenciamento</h3>
          <div class="tbl-wrap card">
            <table class="vs-tabela" aria-label="Pendências de licença">
              <thead><tr><th scope="col">Estabelecimento</th><th scope="col">Risco</th><th scope="col">Pendência</th></tr></thead>
              <tbody>
                @for (x of p.licencas; track x.estabelecimentoId) {
                  <tr><td class="cell-main">{{ x.estabelecimentoNome }}</td><td>{{ riscos[x.risco] }}</td><td><span class="tag tag--alert">{{ motivosPendencia[x.motivo] }}</span>@if (x.validade) { <span class="cell-sub">validade {{ data(x.validade) }}</span> }</td></tr>
                } @empty { <tr><td colspan="3" class="cell-sub">Nenhuma pendência de licença.</td></tr> }
              </tbody>
            </table>
          </div>
          <h3 class="mo-grupo vs-titulo">Prazos de correção vencidos</h3>
          <div class="tbl-wrap card">
            <table class="vs-tabela" aria-label="Prazos de correção vencidos">
              <thead><tr><th scope="col">Estabelecimento</th><th scope="col">Irregularidade</th><th scope="col">Prazo</th></tr></thead>
              <tbody>
                @for (r of p.prazosVencidos; track r.uuid) {
                  <tr><td class="cell-main">{{ r.estabelecimentoNome }}</td><td>{{ r.descricao }} <span class="cell-sub">{{ gravidades[r.gravidade] }}</span></td><td class="mono">{{ data(r.prazoCorrecao!) }}</td></tr>
                } @empty { <tr><td colspan="3" class="cell-sub">Nenhum prazo vencido.</td></tr> }
              </tbody>
            </table>
          </div>
          <h3 class="mo-grupo vs-titulo">Processos sanitários</h3>
          <div class="tbl-wrap card">
            <table class="vs-tabela" aria-label="Pendências dos processos">
              <thead><tr><th scope="col">Processo</th><th scope="col">Estabelecimento</th><th scope="col">Pendência</th></tr></thead>
              <tbody>
                @for (x of p.processos; track x.processoId) {
                  <tr><td class="mono">{{ x.numero }}</td><td>{{ x.estabelecimentoNome }}</td><td><span class="tag tag--alert">{{ motivosProcesso[x.motivo] }}</span><span class="cell-sub">desde {{ data(x.desde) }}</span></td></tr>
                } @empty { <tr><td colspan="3" class="cell-sub">Nenhum processo pendente.</td></tr> }
              </tbody>
            </table>
          </div>
          <h3 class="mo-grupo vs-titulo">Interdições cautelares no fim</h3>
          <div class="tbl-wrap card">
            <table class="vs-tabela" aria-label="Interdições cautelares no fim">
              <thead><tr><th scope="col">Medida</th><th scope="col">Estabelecimento</th><th scope="col">Até</th></tr></thead>
              <tbody>
                @for (x of p.medidas; track x.medidaId) {
                  <tr><td class="mono">{{ x.numero }}</td><td>{{ x.estabelecimentoNome }}</td><td><span [class]="'tag tag--' + (x.motivo === 'CAUTELAR_VENCIDA' ? 'alert' : 'warn')">{{ x.motivo === 'CAUTELAR_VENCIDA' ? 'Vencida' : 'Vence em breve' }}</span> <span class="mono">{{ data(x.fimPrevisto) }}</span></td></tr>
                } @empty { <tr><td colspan="3" class="cell-sub">Nenhuma interdição cautelar perto do fim.</td></tr> }
              </tbody>
            </table>
          </div>
          <h3 class="mo-grupo vs-titulo">Denúncias paradas</h3>
          <div class="tbl-wrap card">
            <table class="vs-tabela" aria-label="Denúncias paradas">
              <thead><tr><th scope="col">Protocolo</th><th scope="col">Estabelecimento</th><th scope="col">Em aberto</th></tr></thead>
              <tbody>
                @for (d of p.denunciasParadas; track d.uuid) {
                  <tr><td class="mono">{{ d.protocolo }}</td><td>{{ d.estabelecimentoNome ?? d.estabelecimentoDescricao }}</td><td>{{ d.diasEmAberto }} dias</td></tr>
                } @empty { <tr><td colspan="3" class="cell-sub">Nenhuma denúncia parada.</td></tr> }
              </tbody>
            </table>
          </div>
        }
      }
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" (fechar)="fechar()">
        @if (g.tipo === 'detalhe') {
          <div class="dw-body">
            @if (detalhe(); as d) {
              <dl class="ro">
                <div><dt>Razão social</dt><dd>{{ d.estabelecimento.razaoSocial }}</dd></div>
                <div><dt>Documento</dt><dd class="mono">{{ doc(d.estabelecimento.documento) }}</dd></div>
                <div><dt>Endereço</dt><dd>{{ d.estabelecimento.endereco }}</dd></div>
                <div><dt>Atividades</dt><dd>@for (a of d.estabelecimento.atividades; track a.cnae) { <span class="cell-sub">{{ a.cnae }} · {{ a.descricao }} ({{ riscos[a.risco] }})</span> }</dd></div>
                @if (d.estabelecimento.responsavelTecnico) { <div><dt>Responsável técnico</dt><dd>{{ d.estabelecimento.responsavelTecnico }} {{ d.estabelecimento.registroConselhoRt ?? '' }}</dd></div> }
              </dl>
              <div class="row-actions">
                @if (podeAlguma('VISA.INSPECIONAR', 'VISA.GERENCIAR')) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrirEstabelecimento(d.estabelecimento)">Alterar cadastro</button> }
                @if (pode('VISA.INSPECIONAR') && temAberta(d)) { <button type="button" class="btn btn--sm" (click)="lavrarAuto(d.estabelecimento.uuid)">Lavrar auto de infração</button> }
              </div>
              <h3 class="mo-grupo vs-titulo">Licenças</h3>
              <ul class="vs-lista" aria-label="Histórico de licenças">
                @for (l of d.licencas; track l.uuid) { <li><span class="mono">{{ l.numero }}</span> · {{ situacoesLicenca[l.situacao] }} · {{ data(l.emitidaEm) }}@if (l.validade) { a {{ data(l.validade) }} }</li> } @empty { <li class="cell-sub">Nenhuma.</li> }
              </ul>
              <h3 class="mo-grupo vs-titulo">Irregularidades</h3>
              <ul class="vs-lista" aria-label="Irregularidades do estabelecimento">
                @for (r of d.irregularidades; track r.uuid) {
                  <li><span [class]="'tag tag--' + (r.situacao === 'ABERTA' ? (r.prazoVencido ? 'alert' : 'warn') : 'ok')">{{ situacoesIrregularidade[r.situacao] }}</span> {{ r.descricao }} <span class="cell-sub">{{ gravidades[r.gravidade] }}@if (r.prazoCorrecao) { · corrigir até {{ data(r.prazoCorrecao) }} }</span></li>
                } @empty { <li class="cell-sub">Nenhuma.</li> }
              </ul>
              <h3 class="mo-grupo vs-titulo">Inspeções</h3>
              <ul class="vs-lista" aria-label="Inspeções do estabelecimento">
                @for (i of d.inspecoes; track i.uuid) { <li><span class="mono">{{ data(i.dataInspecao) }}</span> · {{ motivos[i.motivo] }} · {{ resultados[i.resultado] }} <button type="button" class="link-btn" (click)="baixarTermo(i)">Termo</button></li> } @empty { <li class="cell-sub">Nenhuma.</li> }
              </ul>
              <h3 class="mo-grupo vs-titulo">Documentos</h3>
              <ul class="vs-lista" aria-label="Documentos do estabelecimento">
                @for (x of d.documentos; track x.uuid) { <li>{{ x.titulo }} <span class="cell-sub">{{ x.tipoNome }} · {{ data(x.dataDocumento) }}</span> @if (x.versoes.length) { <button type="button" class="link-btn" (click)="abrirArquivo(x.versoes[0].uuid, x.versoes[0].nomeArquivo)">Abrir</button> }</li> } @empty { <li class="cell-sub">Nenhum.</li> }
              </ul>
              @if (podeAlguma('VISA.INSPECIONAR', 'VISA.GERENCIAR')) {
                <form class="vs-anexo" (submit)="$event.preventDefault(); anexar(d.estabelecimento.uuid)" novalidate>
                  @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-vs-doc-tipo">Tipo</label>
                      <select id="f-vs-doc-tipo" (change)="f.set({ ...f(), docTipo: $any($event.target).value })">
                        @for (t of tiposDocumento(); track t.uuid) { <option [value]="t.uuid" [selected]="f()['docTipo'] === t.uuid">{{ t.nome }}</option> }
                      </select></div>
                    <div class="dw-field"><label for="f-vs-doc-titulo">Título</label><input id="f-vs-doc-titulo" type="text" maxlength="150" [value]="f()['docTitulo'] ?? ''" (input)="set('docTitulo', $event)" /></div>
                  </div>
                  <div class="dw-field"><label for="f-vs-doc-arquivo">Arquivo (PDF, JPG ou PNG, até 10 MB)</label><input id="f-vs-doc-arquivo" type="file" accept=".pdf,.jpg,.jpeg,.png" (change)="arquivo.set($any($event.target).files?.[0] ?? null)" /></div>
                  <button type="submit" class="btn btn--sm" [disabled]="enviando()">Anexar</button>
                </form>
              }
            } @else { <p class="cell-sub">Carregando…</p> }
          </div>
        } @else {
          <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
            <div class="dw-body">
              @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
              @switch (g.tipo) {
                @case ('estabelecimento') {
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-vs-doc">CNPJ ou CPF <span class="req" aria-hidden="true">*</span></label><input id="f-vs-doc" type="text" maxlength="20" [value]="f()['documento'] ?? ''" (input)="set('documento', $event)" /></div>
                    <div class="dw-field"><label for="f-vs-tel">Telefone<span class="opt">opcional</span></label><input id="f-vs-tel" type="text" maxlength="30" [value]="f()['telefone'] ?? ''" (input)="set('telefone', $event)" /></div>
                  </div>
                  <div class="dw-field"><label for="f-vs-razao">Razão social ou nome <span class="req" aria-hidden="true">*</span></label><input id="f-vs-razao" type="text" maxlength="200" [value]="f()['razaoSocial'] ?? ''" (input)="set('razaoSocial', $event)" /></div>
                  <div class="dw-field"><label for="f-vs-fantasia">Nome fantasia<span class="opt">opcional</span></label><input id="f-vs-fantasia" type="text" maxlength="200" [value]="f()['nomeFantasia'] ?? ''" (input)="set('nomeFantasia', $event)" /></div>
                  <div class="dw-field"><label for="f-vs-endereco">Endereço <span class="req" aria-hidden="true">*</span></label><input id="f-vs-endereco" type="text" maxlength="300" [value]="f()['endereco'] ?? ''" (input)="set('endereco', $event)" /></div>
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-vs-rl">Responsável legal<span class="opt">opcional</span></label><input id="f-vs-rl" type="text" maxlength="200" [value]="f()['responsavelLegal'] ?? ''" (input)="set('responsavelLegal', $event)" /></div>
                    <div class="dw-field"><label for="f-vs-rt">Responsável técnico<span class="opt">opcional</span></label><input id="f-vs-rt" type="text" maxlength="200" [value]="f()['responsavelTecnico'] ?? ''" (input)="set('responsavelTecnico', $event)" /></div>
                    <div class="dw-field"><label for="f-vs-conselho">Registro no conselho<span class="opt">opcional</span></label><input id="f-vs-conselho" type="text" maxlength="50" [value]="f()['registroConselhoRt'] ?? ''" (input)="set('registroConselhoRt', $event)" /></div>
                  </div>
                  <fieldset class="vs-atividades"><legend>Atividades (CNAE) e grau de risco</legend>
                    @for (a of atividades(); track $index; let i = $index) {
                      <div class="dw-row">
                        <div class="dw-field"><label [for]="'f-vs-cnae-' + i">CNAE</label><input [id]="'f-vs-cnae-' + i" type="text" maxlength="10" placeholder="0000-0/00" [value]="a.cnae" (input)="mudarAtividade(i, 'cnae', $event)" /></div>
                        <div class="dw-field"><label [for]="'f-vs-ativ-' + i">Descrição</label><input [id]="'f-vs-ativ-' + i" type="text" maxlength="200" [value]="a.descricao" (input)="mudarAtividade(i, 'descricao', $event)" /></div>
                        <div class="dw-field"><label [for]="'f-vs-risco-' + i">Risco</label>
                          <select [id]="'f-vs-risco-' + i" (change)="mudarAtividade(i, 'risco', $event)">
                            @for (r of listaRiscos; track r) { <option [value]="r" [selected]="a.risco === r">{{ riscos[r] }}</option> }
                          </select></div>
                        @if (atividades().length > 1) { <button type="button" class="link-btn" (click)="removerAtividade(i)" [attr.aria-label]="'Remover atividade ' + (i + 1)">Remover</button> }
                      </div>
                    }
                    <button type="button" class="btn btn--ghost btn--sm" (click)="adicionarAtividade()">Adicionar atividade</button>
                  </fieldset>
                  @if (g.atual) { <label class="chk"><input type="checkbox" [checked]="f()['ativo'] !== 'nao'" (change)="f.set({ ...f(), ativo: $any($event.target).checked ? 'sim' : 'nao' })" /> Ativo</label> }
                }
                @case ('licenca') {
                  <dl class="ro"><div><dt>Estabelecimento</dt><dd>{{ g.estabelecimento.nomeFantasia ?? g.estabelecimento.razaoSocial }} · risco {{ riscos[g.estabelecimento.risco] }}</dd></div></dl>
                  @if (g.estabelecimento.risco === 'BAIXO') { <p class="cell-sub">Atividade de risco I: registra a dispensa de licenciamento, sem validade (RDC 153/2017; Lei 13.874/2019).</p> }
                  @if (g.estabelecimento.risco === 'ALTO') { <p class="cell-sub">Atividade de risco III: a licença pede uma inspeção satisfatória nos últimos 12 meses.</p> }
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-vs-emissao">Emissão</label><input id="f-vs-emissao" type="date" [value]="f()['emitidaEm'] ?? ''" (input)="set('emitidaEm', $event)" /></div>
                    @if (g.estabelecimento.risco !== 'BAIXO') { <div class="dw-field"><label for="f-vs-validade">Validade<span class="opt">um ano se vazio</span></label><input id="f-vs-validade" type="date" [value]="f()['validade'] ?? ''" (input)="set('validade', $event)" /></div> }
                  </div>
                  <div class="dw-field"><label for="f-vs-obs">Observação<span class="opt">opcional</span></label><input id="f-vs-obs" type="text" maxlength="500" [value]="f()['observacao'] ?? ''" (input)="set('observacao', $event)" /></div>
                }
                @case ('inspecao') {
                  <div class="dw-field"><label for="f-vs-i-estab">Estabelecimento <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-vs-i-estab" (change)="mudarEstabelecimentoInspecao($any($event.target).value)">
                      <option value="" [selected]="!f()['estabelecimentoId']">Escolha…</option>
                      @for (e of estabelecimentosAtivos(); track e.uuid) { <option [value]="e.uuid" [selected]="f()['estabelecimentoId'] === e.uuid">{{ e.nomeFantasia ?? e.razaoSocial }}</option> }
                    </select></div>
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-vs-i-motivo">Motivo</label>
                      <select id="f-vs-i-motivo" (change)="f.set({ ...f(), motivo: $any($event.target).value })">
                        @for (m of listaMotivos; track m) { <option [value]="m" [selected]="f()['motivo'] === m">{{ motivos[m] }}</option> }
                      </select></div>
                    <div class="dw-field"><label for="f-vs-i-data">Data</label><input id="f-vs-i-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                  </div>
                  @if (f()['motivo'] === 'DENUNCIA') {
                    <div class="dw-field"><label for="f-vs-i-den">Denúncia <span class="req" aria-hidden="true">*</span></label>
                      <select id="f-vs-i-den" (change)="f.set({ ...f(), denunciaId: $any($event.target).value })">
                        <option value="" [selected]="!f()['denunciaId']">Escolha…</option>
                        @for (d of denunciasAbertas(); track d.uuid) { <option [value]="d.uuid" [selected]="f()['denunciaId'] === d.uuid">{{ d.protocolo }} · {{ d.estabelecimentoNome ?? d.estabelecimentoDescricao }}</option> }
                      </select></div>
                  }
                  @if (f()['motivo'] === 'RETORNO') {
                    <div class="dw-field"><label for="f-vs-i-origem">Inspeção de origem <span class="req" aria-hidden="true">*</span></label>
                      <select id="f-vs-i-origem" (change)="f.set({ ...f(), origemId: $any($event.target).value })">
                        <option value="" [selected]="!f()['origemId']">Escolha…</option>
                        @for (i of inspecoesDoEstabelecimento(); track i.uuid) { <option [value]="i.uuid" [selected]="f()['origemId'] === i.uuid">{{ data(i.dataInspecao) }} · {{ motivos[i.motivo] }} · {{ resultados[i.resultado] }}</option> }
                      </select></div>
                    @if (abertasDoEstabelecimento().length) {
                      <fieldset class="vs-atividades"><legend>Irregularidades corrigidas</legend>
                        @for (r of abertasDoEstabelecimento(); track r.uuid) {
                          <label class="chk"><input type="checkbox" [checked]="corrigidas().has(r.uuid)" (change)="alternarCorrigida(r.uuid)" /> {{ r.descricao }} <span class="cell-sub">{{ gravidades[r.gravidade] }}</span></label>
                        }
                      </fieldset>
                    }
                  }
                  <div class="dw-field"><label for="f-vs-i-roteiro">Roteiro<span class="opt">opcional</span></label>
                    <select id="f-vs-i-roteiro" (change)="mudarRoteiro($any($event.target).value)">
                      <option value="" [selected]="!f()['roteiroId']">Sem roteiro</option>
                      @for (r of roteiros(); track r.uuid) { <option [value]="r.uuid" [selected]="f()['roteiroId'] === r.uuid">{{ r.nome }} (v{{ r.versao }})</option> }
                    </select></div>
                  @if (roteiroEscolhido(); as r) {
                    <fieldset class="vs-atividades"><legend>Itens do roteiro</legend>
                      @for (item of r.itens; track item.codigo) {
                        <div class="vs-item">
                          <span><strong class="mono">{{ item.codigo }}</strong> {{ item.texto }}</span>
                          <select [attr.aria-label]="'Resposta do item ' + item.codigo" (change)="responder(item.codigo, $any($event.target).value)">
                            @for (x of listaRespostas; track x) { <option [value]="x" [selected]="respostas()[item.codigo] === x">{{ respostasRoteiro[x] }}</option> }
                          </select>
                        </div>
                      }
                    </fieldset>
                  }
                  <fieldset class="vs-atividades"><legend>Irregularidades</legend>
                    @for (x of irregularidades(); track $index; let i = $index) {
                      <div class="vs-irreg">
                        @if (x.itemCodigo) { <span class="tag">Item {{ x.itemCodigo }}</span> }
                        <div class="dw-field"><label [for]="'f-vs-ir-desc-' + i">Descrição</label><input [id]="'f-vs-ir-desc-' + i" type="text" maxlength="1000" [value]="x.descricao" (input)="mudarIrregularidade(i, 'descricao', $event)" /></div>
                        <div class="dw-row">
                          <div class="dw-field"><label [for]="'f-vs-ir-grav-' + i">Gravidade</label>
                            <select [id]="'f-vs-ir-grav-' + i" (change)="mudarIrregularidade(i, 'gravidade', $event)">
                              @for (gr of listaGravidades; track gr) { <option [value]="gr" [selected]="x.gravidade === gr">{{ gravidades[gr] }}</option> }
                            </select></div>
                          <div class="dw-field"><label [for]="'f-vs-ir-prazo-' + i">Corrigir até<span class="opt">opcional</span></label><input [id]="'f-vs-ir-prazo-' + i" type="date" [value]="x.prazoCorrecao" (input)="mudarIrregularidade(i, 'prazoCorrecao', $event)" /></div>
                          <div class="dw-field"><label [for]="'f-vs-ir-enq-' + i">Enquadramento<span class="opt">opcional</span></label><input [id]="'f-vs-ir-enq-' + i" type="text" maxlength="500" [value]="x.enquadramento" (input)="mudarIrregularidade(i, 'enquadramento', $event)" /></div>
                        </div>
                        @if (!x.itemCodigo) { <button type="button" class="link-btn" (click)="removerIrregularidade(i)">Remover</button> }
                      </div>
                    }
                    <button type="button" class="btn btn--ghost btn--sm" (click)="adicionarIrregularidade()">Adicionar irregularidade</button>
                  </fieldset>
                  <div class="dw-row">
                    <div class="dw-field"><label for="f-vs-i-res">Resultado</label>
                      <select id="f-vs-i-res" (change)="f.set({ ...f(), resultado: $any($event.target).value })">
                        @for (r of listaResultados; track r) { <option [value]="r" [selected]="f()['resultado'] === r">{{ resultados[r] }}</option> }
                      </select></div>
                    <div class="dw-field"><label for="f-vs-i-fiscais">Matrículas dos fiscais <span class="req" aria-hidden="true">*</span></label><input id="f-vs-i-fiscais" type="text" placeholder="Separadas por vírgula; inclua a sua" [value]="f()['fiscais'] ?? ''" (input)="set('fiscais', $event)" /></div>
                  </div>
                  <div class="dw-field"><label for="f-vs-i-rel">Relatório <span class="req" aria-hidden="true">*</span></label><textarea id="f-vs-i-rel" rows="4" maxlength="4000" [value]="f()['relatorio'] ?? ''" (input)="set('relatorio', $event)"></textarea></div>
                }
                @case ('denuncia') {
                  <div class="dw-field"><label for="f-vs-d-canal">Canal</label>
                    <select id="f-vs-d-canal" (change)="f.set({ ...f(), canal: $any($event.target).value })">
                      @for (c of listaCanais; track c) { <option [value]="c" [selected]="f()['canal'] === c">{{ canais[c] }}</option> }
                    </select></div>
                  <div class="dw-field"><label for="f-vs-d-estab">Estabelecimento cadastrado<span class="opt">ou descreva abaixo</span></label>
                    <select id="f-vs-d-estab" (change)="f.set({ ...f(), estabelecimentoId: $any($event.target).value })">
                      <option value="" [selected]="!f()['estabelecimentoId']">Não cadastrado</option>
                      @for (e of estabelecimentosAtivos(); track e.uuid) { <option [value]="e.uuid" [selected]="f()['estabelecimentoId'] === e.uuid">{{ e.nomeFantasia ?? e.razaoSocial }}</option> }
                    </select></div>
                  @if (!f()['estabelecimentoId']) { <div class="dw-field"><label for="f-vs-d-estabdesc">Estabelecimento denunciado</label><input id="f-vs-d-estabdesc" type="text" maxlength="300" placeholder="Nome e endereço" [value]="f()['estabelecimentoDescricao'] ?? ''" (input)="set('estabelecimentoDescricao', $event)" /></div> }
                  <div class="dw-field"><label for="f-vs-d-desc">Denúncia <span class="req" aria-hidden="true">*</span></label><textarea id="f-vs-d-desc" rows="3" maxlength="2000" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)"></textarea></div>
                  <label class="chk"><input type="checkbox" id="f-vs-d-anonima" [checked]="f()['anonima'] === 'sim'" (change)="f.set({ ...f(), anonima: $any($event.target).checked ? 'sim' : 'nao' })" /> Anônima</label>
                  @if (f()['anonima'] !== 'sim') {
                    <div class="dw-row">
                      <div class="dw-field"><label for="f-vs-d-nome">Denunciante</label><input id="f-vs-d-nome" type="text" maxlength="200" [value]="f()['denuncianteNome'] ?? ''" (input)="set('denuncianteNome', $event)" /></div>
                      <div class="dw-field"><label for="f-vs-d-contato">Contato<span class="opt">opcional</span></label><input id="f-vs-d-contato" type="text" maxlength="200" [value]="f()['denuncianteContato'] ?? ''" (input)="set('denuncianteContato', $event)" /></div>
                    </div>
                    <p class="cell-sub">Os dados do denunciante só aparecem para a coordenação da vigilância sanitária.</p>
                  }
                }
                @case ('conclusao') {
                  <dl class="ro"><div><dt>Denúncia</dt><dd>{{ g.denuncia.protocolo }} · {{ g.denuncia.descricao }}</dd></div></dl>
                  <div class="dw-field"><label for="f-vs-c-sit">Conclusão</label>
                    <select id="f-vs-c-sit" (change)="f.set({ ...f(), situacao: $any($event.target).value })">
                      <option value="PROCEDENTE" [selected]="f()['situacao'] === 'PROCEDENTE'" [disabled]="!g.denuncia.inspecoes">Procedente</option>
                      <option value="IMPROCEDENTE" [selected]="f()['situacao'] === 'IMPROCEDENTE'">Improcedente</option>
                    </select></div>
                  <div class="dw-field"><label for="f-vs-c-texto">Justificativa <span class="req" aria-hidden="true">*</span></label><textarea id="f-vs-c-texto" rows="3" maxlength="1000" [value]="f()['conclusao'] ?? ''" (input)="set('conclusao', $event)"></textarea></div>
                }
              }
            </div>
            <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="fechar()">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar' }}</button></div>
          </form>
        }
      </app-drawer>
    }
  `,
  styles: `
    .vs-cab { display: flex; justify-content: space-between; align-items: flex-end; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-2); }
    .vs-tabela { min-width: 0; width: 100%; }
    .vs-titulo { margin-top: var(--s-4); }
    .vs-lista { margin: 0; padding-left: var(--s-4); display: grid; gap: var(--s-1); }
    .vs-atividades { border: 1px solid var(--ink-200); border-radius: var(--r-2, 8px); padding: var(--s-2) var(--s-3); margin: var(--s-2) 0; display: grid; gap: var(--s-2); }
    .vs-item { display: grid; grid-template-columns: 1fr auto; gap: var(--s-2); align-items: center; }
    .vs-irreg { border-top: 1px dashed var(--ink-200); padding-top: var(--s-2); }
    .vs-anexo { margin-top: var(--s-3); display: grid; gap: var(--s-2); }
    .chk { display: flex; gap: var(--s-2); align-items: center; }
  `,
})
export class VigilanciaSanitaria {
  private readonly service = inject(VigilanciaSanitariaService);
  private readonly documentoService = inject(DocumentoService);
  private readonly authService = inject(AuthService);

  protected readonly riscos = RISCOS;
  protected readonly listaRiscos = Object.keys(RISCOS) as GrauRiscoSanitario[];
  protected readonly canais = CANAIS_DENUNCIA;
  protected readonly listaCanais = Object.keys(CANAIS_DENUNCIA) as CanalDenuncia[];
  protected readonly situacoesDenuncia = SITUACOES_DENUNCIA;
  protected readonly motivos = MOTIVOS_INSPECAO;
  protected readonly listaMotivos = (Object.keys(MOTIVOS_INSPECAO) as MotivoInspecao[]).filter((m) => m !== 'SURTO');
  protected readonly resultados = RESULTADOS_INSPECAO;
  protected readonly listaResultados = Object.keys(RESULTADOS_INSPECAO) as ResultadoInspecao[];
  protected readonly respostasRoteiro = RESPOSTAS_ROTEIRO;
  protected readonly listaRespostas = Object.keys(RESPOSTAS_ROTEIRO) as RespostaRoteiro[];
  protected readonly gravidades = GRAVIDADES;
  protected readonly listaGravidades = Object.keys(GRAVIDADES) as GravidadeIrregularidade[];
  protected readonly situacoesIrregularidade = SITUACOES_IRREGULARIDADE;
  protected readonly situacoesLicenca = SITUACOES_LICENCA;
  protected readonly motivosPendencia = MOTIVOS_PENDENCIA_LICENCA;
  protected readonly motivosProcesso = MOTIVOS_PENDENCIA_PROCESSO;
  /** O estabelecimento a autuar na aba Processos (ADR-0200). */
  protected readonly autuar = signal<string | null>(null);

  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });
  protected readonly aba = signal<Aba>('estabelecimentos');
  protected readonly erro = signal<string | null>(null);
  protected readonly busca = signal('');
  protected readonly estabelecimentos = signal<EstabelecimentoReguladoDto[]>([]);
  protected readonly inspecoes = signal<InspecaoSanitariaDto[]>([]);
  protected readonly denuncias = signal<DenunciaSanitariaDto[]>([]);
  protected readonly licencas = signal<LicencaSanitariaDto[]>([]);
  protected readonly pendencias = signal<PendenciasVisaDto | null>(null);
  protected readonly roteiros = signal<RoteiroInspecaoDto[]>([]);
  protected readonly tiposDocumento = signal<TipoDocumentoDto[]>([]);
  protected readonly detalhe = signal<EstabelecimentoDetalheDto | null>(null);
  protected readonly abertasDoEstabelecimento = signal<EstabelecimentoDetalheDto['irregularidades']>([]);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});
  protected readonly atividades = signal<AtividadeDto[]>([]);
  protected readonly respostas = signal<Record<string, RespostaRoteiro>>({});
  protected readonly irregularidades = signal<IrregularidadeNova[]>([]);
  protected readonly corrigidas = signal<Set<string>>(new Set());
  protected readonly arquivo = signal<File | null>(null);

  protected readonly estabelecimentosFiltrados = computed(() => {
    const b = this.busca().trim().toLowerCase();
    const digitos = b.replace(/\D/g, '');
    if (!b) return this.estabelecimentos();
    return this.estabelecimentos().filter((e) => e.razaoSocial.toLowerCase().includes(b) || (e.nomeFantasia ?? '').toLowerCase().includes(b)
      || (digitos.length > 0 && e.documento.includes(digitos)));
  });
  protected readonly estabelecimentosAtivos = computed(() => this.estabelecimentos().filter((e) => e.ativo));
  protected readonly denunciasAbertas = computed(() => this.denuncias().filter((d) => this.aberta(d)));
  protected readonly roteiroEscolhido = computed(() => this.roteiros().find((r) => r.uuid === this.f()['roteiroId']) ?? null);
  protected readonly inspecoesDoEstabelecimento = computed(() => this.inspecoes().filter((i) => i.estabelecimentoId === this.f()['estabelecimentoId']));

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => {
      this.acesso.set(a);
      this.carregar();
    });
  }

  protected pode(permissao: string): boolean {
    const a = this.acesso();
    return !a.restrito || a.permissoes.has(permissao);
  }

  protected podeAlguma(...permissoes: string[]): boolean {
    return permissoes.some((p) => this.pode(p));
  }

  protected aberta(d: DenunciaSanitariaDto): boolean {
    return d.situacao === 'RECEBIDA' || d.situacao === 'EM_APURACAO';
  }

  protected titulo(g: Gaveta): string {
    switch (g.tipo) {
      case 'estabelecimento': return g.atual ? 'Alterar estabelecimento' : 'Novo estabelecimento';
      case 'detalhe': return this.detalhe()?.estabelecimento.nomeFantasia ?? this.detalhe()?.estabelecimento.razaoSocial ?? 'Estabelecimento';
      case 'licenca': return g.estabelecimento.risco === 'BAIXO' ? 'Dispensa de licenciamento' : 'Conceder licença sanitária';
      case 'inspecao': return 'Registrar inspeção';
      case 'denuncia': return 'Registrar denúncia';
      default: return 'Concluir denúncia';
    }
  }

  protected abrir(g: Gaveta, campos: Record<string, string> = {}): void {
    this.erroGaveta.set(null);
    this.f.set(campos);
    this.gaveta.set(g);
  }

  protected fechar(): void {
    this.gaveta.set(null);
    this.detalhe.set(null);
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected temAberta(d: EstabelecimentoDetalheDto): boolean {
    return d.irregularidades.some((r) => r.situacao === 'ABERTA');
  }

  protected lavrarAuto(estabelecimentoId: string): void {
    this.fechar();
    this.autuar.set(null);
    this.aba.set('processos');
    setTimeout(() => this.autuar.set(estabelecimentoId));
  }

  // ── Estabelecimento ──

  protected abrirEstabelecimento(e: EstabelecimentoReguladoDto | null): void {
    this.atividades.set(e ? e.atividades.map((a) => ({ ...a })) : [{ cnae: '', descricao: '', risco: 'MEDIO' }]);
    this.detalhe.set(null);
    this.abrir({ tipo: 'estabelecimento', atual: e }, e ? {
      documento: e.documento, razaoSocial: e.razaoSocial, nomeFantasia: e.nomeFantasia ?? '', endereco: e.endereco, telefone: e.telefone ?? '',
      responsavelLegal: e.responsavelLegal ?? '', responsavelTecnico: e.responsavelTecnico ?? '', registroConselhoRt: e.registroConselhoRt ?? '', ativo: e.ativo ? 'sim' : 'nao',
    } : {});
  }

  protected adicionarAtividade(): void {
    this.atividades.set([...this.atividades(), { cnae: '', descricao: '', risco: 'MEDIO' }]);
  }

  protected removerAtividade(i: number): void {
    this.atividades.set(this.atividades().filter((_, j) => j !== i));
  }

  protected mudarAtividade(i: number, campo: keyof AtividadeDto, ev: Event): void {
    const v = (ev.target as HTMLInputElement).value;
    this.atividades.set(this.atividades().map((a, j) => (j === i ? { ...a, [campo]: v } : a)));
  }

  protected abrirDetalhe(id: string): void {
    this.detalhe.set(null);
    this.arquivo.set(null);
    this.abrir({ tipo: 'detalhe', id }, { docTipo: this.tiposDocumento().find((t) => t.codigo === 'OUTRO')?.uuid ?? this.tiposDocumento()[0]?.uuid ?? '' });
    this.service.estabelecimento(id).subscribe({ next: (d) => this.detalhe.set(d), error: (x: HttpErrorResponse) => this.erroGaveta.set(mensagem(x)) });
  }

  protected abrirLicenca(e: EstabelecimentoReguladoDto): void {
    this.abrir({ tipo: 'licenca', estabelecimento: e }, { emitidaEm: hoje() });
  }

  // ── Inspeção ──

  protected abrirInspecao(campos: Record<string, string>): void {
    this.respostas.set({});
    this.irregularidades.set([]);
    this.corrigidas.set(new Set());
    this.abertasDoEstabelecimento.set([]);
    this.abrir({ tipo: 'inspecao' }, { motivo: 'PROGRAMADA', resultado: 'SATISFATORIA', data: hoje(), ...campos });
    if (campos['estabelecimentoId']) this.carregarAbertas(campos['estabelecimentoId']);
  }

  protected mudarEstabelecimentoInspecao(id: string): void {
    this.f.set({ ...this.f(), estabelecimentoId: id, origemId: '' });
    this.corrigidas.set(new Set());
    this.carregarAbertas(id);
  }

  private carregarAbertas(id: string): void {
    this.abertasDoEstabelecimento.set([]);
    if (!id) return;
    this.service.estabelecimento(id).pipe(catchError(() => of(null)))
      .subscribe((d) => this.abertasDoEstabelecimento.set((d?.irregularidades ?? []).filter((r) => r.situacao === 'ABERTA')));
  }

  protected alternarCorrigida(id: string): void {
    const s = new Set(this.corrigidas());
    if (s.has(id)) s.delete(id); else s.add(id);
    this.corrigidas.set(s);
  }

  protected mudarRoteiro(id: string): void {
    this.f.set({ ...this.f(), roteiroId: id });
    const r = this.roteiros().find((x) => x.uuid === id);
    this.respostas.set(Object.fromEntries((r?.itens ?? []).map((i) => [i.codigo, 'CONFORME' as RespostaRoteiro])));
    this.irregularidades.set(this.irregularidades().filter((x) => !x.itemCodigo));
  }

  /** Item não conforme ganha a irregularidade com o texto e a gravidade do roteiro; o que volta a conforme perde a sua. */
  protected responder(codigo: string, valor: RespostaRoteiro): void {
    this.respostas.set({ ...this.respostas(), [codigo]: valor });
    const outras = this.irregularidades().filter((x) => x.itemCodigo !== codigo);
    if (valor === 'NAO_CONFORME') {
      const item = this.roteiroEscolhido()?.itens.find((i) => i.codigo === codigo);
      const existente = this.irregularidades().find((x) => x.itemCodigo === codigo);
      this.irregularidades.set([...outras, existente ?? { itemCodigo: codigo, descricao: item?.texto ?? '', enquadramento: '', gravidade: item?.gravidade ?? 'LEVE', prazoCorrecao: '' }]);
      if (this.f()['resultado'] === 'SATISFATORIA') this.f.set({ ...this.f(), resultado: 'COM_RESSALVAS' });
    } else {
      this.irregularidades.set(outras);
    }
  }

  protected adicionarIrregularidade(): void {
    this.irregularidades.set([...this.irregularidades(), { itemCodigo: null, descricao: '', enquadramento: '', gravidade: 'LEVE', prazoCorrecao: '' }]);
    if (this.f()['resultado'] === 'SATISFATORIA') this.f.set({ ...this.f(), resultado: 'COM_RESSALVAS' });
  }

  protected removerIrregularidade(i: number): void {
    this.irregularidades.set(this.irregularidades().filter((_, j) => j !== i));
  }

  protected mudarIrregularidade(i: number, campo: 'descricao' | 'enquadramento' | 'gravidade' | 'prazoCorrecao', ev: Event): void {
    const v = (ev.target as HTMLInputElement).value;
    this.irregularidades.set(this.irregularidades().map((x, j) => (j === i ? { ...x, [campo]: v } : x)));
  }

  // ── Denúncia ──

  protected abrirDenuncia(): void {
    this.abrir({ tipo: 'denuncia' }, { canal: 'TELEFONE', anonima: 'nao' });
  }

  // ── Salvar ──

  protected salvar(g: Gaveta): void {
    if (this.enviando()) return;
    const c = this.f();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    let req: Observable<unknown>;
    let falta: string | null = null;
    switch (g.tipo) {
      case 'estabelecimento': {
        const atividades = this.atividades().map((a) => ({ ...a, cnae: a.cnae.trim(), descricao: a.descricao.trim() }));
        if (!t('documento') || !t('razaoSocial') || !t('endereco')) falta = 'Informe o CNPJ ou CPF, a razão social e o endereço.';
        else if (atividades.some((a) => !a.cnae || !a.descricao)) falta = 'Informe o CNAE e a descrição de cada atividade.';
        const dto = {
          documento: t('documento') ?? '', razaoSocial: t('razaoSocial') ?? '', nomeFantasia: t('nomeFantasia'), endereco: t('endereco') ?? '', telefone: t('telefone'),
          responsavelLegal: t('responsavelLegal'), responsavelTecnico: t('responsavelTecnico'), registroConselhoRt: t('registroConselhoRt'), atividades,
        };
        req = g.atual ? this.service.atualizarEstabelecimento(g.atual.uuid, { ...dto, ativo: c['ativo'] !== 'nao' }) : this.service.criarEstabelecimento(dto);
        break;
      }
      case 'licenca':
        req = this.service.concederLicenca(g.estabelecimento.uuid, { emitidaEm: t('emitidaEm'), validade: g.estabelecimento.risco === 'BAIXO' ? null : t('validade'), observacao: t('observacao') });
        break;
      case 'inspecao': {
        const fiscais = (c['fiscais'] ?? '').split(/[,;\s]+/).map((x) => x.trim()).filter(Boolean);
        if (!t('estabelecimentoId')) falta = 'Escolha o estabelecimento.';
        else if (!fiscais.length) falta = 'Informe a matrícula de ao menos um fiscal.';
        else if (!t('relatorio')) falta = 'Escreva o relatório da inspeção.';
        else if (this.irregularidades().some((x) => !x.descricao.trim())) falta = 'Descreva cada irregularidade.';
        const motivo = c['motivo'] as MotivoInspecao;
        req = this.service.registrarInspecao({
          estabelecimentoId: t('estabelecimentoId') ?? '', motivo, dataInspecao: t('data'),
          denunciaId: motivo === 'DENUNCIA' ? t('denunciaId') : null, inspecaoOrigemId: motivo === 'RETORNO' ? t('origemId') : null,
          roteiroVersaoId: this.roteiroEscolhido()?.versaoId ?? null, respostas: this.roteiroEscolhido() ? this.respostas() : {},
          resultado: c['resultado'] as ResultadoInspecao, relatorio: t('relatorio') ?? '', fiscais,
          irregularidades: this.irregularidades().map((x) => ({ itemCodigo: x.itemCodigo, descricao: x.descricao.trim(), enquadramento: x.enquadramento.trim() || null,
            gravidade: x.gravidade, prazoCorrecao: x.prazoCorrecao || null })),
          corrigidas: motivo === 'RETORNO' ? [...this.corrigidas()] : [],
        });
        break;
      }
      case 'denuncia': {
        const anonima = c['anonima'] === 'sim';
        if (!t('descricao')) falta = 'Descreva a denúncia.';
        else if (!t('estabelecimentoId') && !t('estabelecimentoDescricao')) falta = 'Escolha ou descreva o estabelecimento.';
        else if (!anonima && !t('denuncianteNome')) falta = 'Informe o denunciante ou marque como anônima.';
        req = this.service.registrarDenuncia({
          canal: c['canal'] as CanalDenuncia, descricao: t('descricao') ?? '', estabelecimentoId: t('estabelecimentoId'),
          estabelecimentoDescricao: t('estabelecimentoId') ? null : t('estabelecimentoDescricao'), anonima,
          denuncianteNome: anonima ? null : t('denuncianteNome'), denuncianteContato: anonima ? null : t('denuncianteContato'),
        });
        break;
      }
      case 'conclusao':
        if (!t('conclusao')) falta = 'Informe a justificativa.';
        req = this.service.concluirDenuncia(g.denuncia.uuid, c['situacao'] as SituacaoDenuncia, t('conclusao') ?? '');
        break;
      default:
        return;
    }
    if (falta) {
      this.erroGaveta.set(falta);
      return;
    }
    this.enviando.set(true);
    req.subscribe({
      next: () => {
        this.enviando.set(false);
        this.fechar();
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected anexar(estabelecimentoId: string): void {
    const c = this.f();
    const arquivo = this.arquivo();
    if (!c['docTipo'] || !(c['docTitulo'] ?? '').trim() || !arquivo) {
      this.erroGaveta.set('Escolha o tipo, o título e o arquivo.');
      return;
    }
    this.enviando.set(true);
    this.erroGaveta.set(null);
    this.service.anexar('estabelecimento', estabelecimentoId, { tipoId: c['docTipo'], titulo: c['docTitulo'].trim(), dataDocumento: '' }, arquivo).subscribe({
      next: (docs) => {
        this.enviando.set(false);
        const d = this.detalhe();
        if (d) this.detalhe.set({ ...d, documentos: docs });
        this.f.set({ ...c, docTitulo: '' });
        this.arquivo.set(null);
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erroGaveta.set(mensagem(x));
      },
    });
  }

  protected baixarTermo(i: InspecaoSanitariaDto): void {
    this.service.termo(i.uuid).subscribe({
      next: (r) => baixar(r, `termo-inspecao-${i.dataInspecao}.pdf`),
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  protected abrirArquivo(versaoId: string, nome: string): void {
    this.service.abrirDocumento(versaoId).subscribe({ next: (r) => baixar(r, nome), error: (x: HttpErrorResponse) => this.erroGaveta.set(mensagem(x)) });
  }

  // ── Apresentação ──

  protected fiscais(i: InspecaoSanitariaDto): string {
    return i.fiscais.map((x) => x.nome).join(', ');
  }

  protected tagLicenca(s: string): string {
    return s === 'VIGENTE' || s === 'DISPENSADA' ? 'ok' : 'alert';
  }

  protected tagResultado(r: ResultadoInspecao): string {
    return r === 'SATISFATORIA' ? 'ok' : r === 'COM_RESSALVAS' ? 'warn' : 'alert';
  }

  protected tagDenuncia(s: SituacaoDenuncia): string {
    return s === 'PROCEDENTE' ? 'alert' : s === 'IMPROCEDENTE' ? 'ok' : 'warn';
  }

  protected doc(d: string): string {
    if (d.length === 14) return `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8, 12)}-${d.slice(12)}`;
    if (d.length === 11) return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
    return d;
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
  }

  protected carregar(): void {
    this.erro.set(null);
    const falhou = (x: HttpErrorResponse) => this.erro.set(mensagem(x));
    this.service.estabelecimentos().subscribe({ next: (l) => this.estabelecimentos.set(l), error: falhou });
    this.service.inspecoes().pipe(catchError(() => of([]))).subscribe((l) => this.inspecoes.set(l));
    this.service.denuncias().pipe(catchError(() => of([]))).subscribe((l) => this.denuncias.set(l));
    this.service.licencas().pipe(catchError(() => of([]))).subscribe((l) => this.licencas.set(l));
    this.service.pendencias().pipe(catchError(() => of(null))).subscribe((p) => this.pendencias.set(p));
    this.service.roteiros().pipe(catchError(() => of([]))).subscribe((l) => this.roteiros.set(l.filter((r) => r.ativo && r.versaoId)));
    this.documentoService.tipos().pipe(catchError(() => of([]))).subscribe((l) => this.tiposDocumento.set(l.filter((x) => x.ativo)));
  }
}

function hoje(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function baixar(r: HttpResponse<Blob>, nome: string): void {
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
