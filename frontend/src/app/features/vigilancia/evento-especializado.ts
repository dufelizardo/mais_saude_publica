import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import {
  EmergenciaDto,
  EstruturaTemporariaDto,
  NATUREZAS_EVENTO_MASSA,
  NaturezaEventoMassa,
  RISCOS_EVENTO_MASSA,
  RiscoEventoMassa,
  SITUACOES_ESTRUTURA,
  TIPOS_ESTRUTURA,
  TipoEstruturaTemporaria,
} from '../../core/models/emergencia';
import { ErrorResponseDto } from '../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { EmergenciaService } from '../../core/services/emergencia';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { Drawer } from '../../shared/drawer/drawer';

type Gaveta =
  | { tipo: 'eventoMassa' }
  | { tipo: 'estrutura'; item: EstruturaTemporariaDto | null }
  | { tipo: 'desativar'; item: EstruturaTemporariaDto }
  | { tipo: 'boletim'; item: EstruturaTemporariaDto };

/**
 * Os eventos especializados no detalhe da emergência (ADR-0160): os dados do evento de massa e as estruturas temporárias
 * de atendimento (posto médico, tenda de hidratação, hospital de campanha), com a abertura, a desativação e os boletins.
 * A estrutura é capacidade, não unidade: a unidade de referência responde por escala, estoque e leitos.
 */
@Component({
  selector: 'app-evento-especializado',
  imports: [Drawer],
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
  `,
})
export class EventoEspecializado {
  protected readonly service = inject(EmergenciaService);
  private readonly unidadeService = inject(UnidadeSaudeService);

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
