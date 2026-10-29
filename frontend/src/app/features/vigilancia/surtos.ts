import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ErrorResponseDto } from '../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import {
  AgravoDto,
  CLASSIFICACOES,
  NotificacaoDto,
  SITUACOES_NOTIFICACAO,
  SITUACOES_SURTO,
  SituacaoSurto,
  SurtoDto,
  SurtosDto,
  TIPOS_INSTITUICAO,
  TipoInstituicaoSurto,
  TipoSurto,
} from '../../core/models/vigilancia';
import { VigilanciaService } from '../../core/services/vigilancia';
import { Drawer } from '../../shared/drawer/drawer';
import { CurvaEpidemica } from './curva-epidemica';
import { LinhaDoTempo } from './linha-do-tempo';
import { MedidasDeControle } from './medidas-de-controle';

/** Abertura vinda do mapa: o agravo e os casos da microárea escolhida. */
export interface NovoSurto {
  agravoId: string;
  notificacoesIds: string[];
  microareasIds: string[];
  seq: number;
}

/**
 * Aba Surtos (ADR-0131): a lista com a situação e os números, a abertura (com os casos escolhidos na lista ou vindos da
 * microárea no mapa) e a gaveta do surto com o ciclo, os casos, as medidas de controle, a curva e a linha do tempo.
 */
@Component({
  selector: 'app-surtos',
  imports: [ReactiveFormsModule, Drawer, CurvaEpidemica, LinhaDoTempo, MedidasDeControle],
  template: `
    <div class="filters" style="margin-bottom: var(--s-3); justify-content: space-between;">
      <select class="sel" aria-label="Situação do surto" [value]="situacaoFiltro()" (change)="situacaoFiltro.set($any($event.target).value)">
        <option value="">Todas as situações</option>
        @for (s of listaSituacoes; track s) { <option [value]="s">{{ situacoes[s].rotulo }}</option> }
      </select>
      @if (podeInvestigar()) { <button type="button" class="btn btn--sm" (click)="abrirNovo()">Abrir surto</button> }
    </div>
    @if (surtos(); as d) {
      <p class="cell-sub" aria-label="Resumo dos surtos">{{ d.abertos }} {{ d.abertos === 1 ? 'surto aberto' : 'surtos abertos' }} · {{ d.confirmados }}
        {{ d.confirmados === 1 ? 'confirmado' : 'confirmados' }}@if (d.medidasVencidas) { · {{ d.medidasVencidas }} {{ d.medidasVencidas === 1 ? 'medida' : 'medidas' }} com prazo vencido }</p>
      @if (lista().length) {
        <div class="tbl-wrap">
          <table aria-label="Surtos">
            <thead><tr><th scope="col">Surto</th><th scope="col">Agravo</th><th scope="col">Início</th><th scope="col" class="num">Casos</th><th scope="col">Situação</th><th scope="col" class="num">Ações</th></tr></thead>
            <tbody>
              @for (s of lista(); track s.uuid) {
                <tr>
                  <td><span class="cell-main">{{ s.nome }}</span><span class="cell-sub">{{ s.tipo === 'INSTITUCIONAL' ? (s.nomeInstituicao ?? 'Institucional') : 'Comunitário' }} · {{ s.unidadeNome }}</span></td>
                  <td>{{ s.agravoNome }}</td>
                  <td class="mono">{{ data(s.inicio) }}</td>
                  <td class="num mono">{{ s.casos }}@if (s.confirmados) { <span class="cell-sub" style="display: inline;"> · {{ s.confirmados }} conf.</span> }</td>
                  <td><span [class]="'tag tag--' + situacoes[s.situacao].classe">{{ situacoes[s.situacao].rotulo }}</span>@if (s.medidasVencidas) { <span class="tag tag--warn">Medida vencida</span> }</td>
                  <td class="num"><button type="button" class="link-btn" (click)="abrirSurto(s.uuid)" [attr.aria-label]="'Abrir surto: ' + s.nome">Abrir</button></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else {
        <p class="cell-sub">Nenhum surto.</p>
      }
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g === 'novo' ? 'Abrir surto' : (detalhe()?.nome ?? 'Surto')" [larga]="g !== 'novo'" (fechar)="fechar()">
        @if (g === 'novo') {
          <form class="dw-form" [formGroup]="form" (ngSubmit)="salvarNovo()" novalidate>
            <div class="dw-body">
              @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
              <div class="dw-row">
                <div class="dw-field">
                  <label for="f-surto-agravo">Agravo</label>
                  <select id="f-surto-agravo" formControlName="agravoId" (change)="escolhidos.set([])">
                    <option value="">Escolha…</option>
                    @for (a of agravos(); track a.uuid) { <option [value]="a.uuid">{{ a.nome }}</option> }
                  </select>
                </div>
                <div class="dw-field">
                  <label for="f-surto-unidade">Unidade responsável</label>
                  <select id="f-surto-unidade" formControlName="unidadeId">
                    <option value="">Escolha…</option>
                    @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                  </select>
                </div>
              </div>
              <div class="dw-field">
                <label for="f-surto-nome">Nome</label>
                <input id="f-surto-nome" type="text" maxlength="200" formControlName="nome" placeholder="Ex.: Dengue · microáreas 04 a 06" />
              </div>
              <div class="dw-row">
                <div class="dw-field">
                  <label for="f-surto-tipo">Tipo</label>
                  <select id="f-surto-tipo" formControlName="tipo">
                    <option value="COMUNITARIO">Comunitário (no território)</option>
                    <option value="INSTITUCIONAL">Institucional (num estabelecimento)</option>
                  </select>
                </div>
                <div class="dw-field">
                  <label for="f-surto-inicio">Início</label>
                  <input id="f-surto-inicio" type="date" formControlName="inicio" />
                </div>
              </div>
              @if (form.controls.tipo.value === 'INSTITUCIONAL') {
                <div class="dw-row">
                  <div class="dw-field">
                    <label for="f-surto-tipo-inst">Estabelecimento</label>
                    <select id="f-surto-tipo-inst" formControlName="tipoInstituicao">
                      <option value="">Escolha…</option>
                      @for (t of listaInstituicoes; track t) { <option [value]="t">{{ instituicoes[t] }}</option> }
                    </select>
                  </div>
                  <div class="dw-field">
                    <label for="f-surto-nome-inst">Nome do estabelecimento</label>
                    <input id="f-surto-nome-inst" type="text" maxlength="200" formControlName="nomeInstituicao" />
                  </div>
                </div>
                <div class="dw-field">
                  <label for="f-surto-endereco-inst">Endereço<span class="opt">opcional</span></label>
                  <input id="f-surto-endereco-inst" type="text" maxlength="500" formControlName="enderecoInstituicao" />
                </div>
              }
              <div class="dw-field">
                <label for="f-surto-definicao">Definição de caso<span class="opt">quem entra no surto</span></label>
                <textarea id="f-surto-definicao" rows="2" maxlength="2000" formControlName="definicaoCaso"></textarea>
              </div>
              @if (microareasNovas().length) {
                <p class="cell-sub">{{ microareasNovas().length }} {{ microareasNovas().length === 1 ? 'microárea escolhida' : 'microáreas escolhidas' }} no mapa.</p>
              }
              <fieldset class="dw-field">
                <legend>Casos</legend>
                @if (candidatos().length) {
                  <div class="radios" aria-label="Casos do agravo" style="flex-direction: column; align-items: flex-start;">
                    @for (n of candidatos(); track n.uuid) {
                      <label><input type="checkbox" [checked]="escolhidos().includes(n.uuid)" (change)="alternar(n.uuid, $any($event.target).checked)" />
                        {{ n.pacienteNome }} · {{ data(n.dataPrimeirosSintomas ?? n.dataNotificacao) }}@if (n.microareaCodigo) { · microárea {{ n.microareaCodigo }} }</label>
                    }
                  </div>
                } @else {
                  <p class="cell-sub">{{ form.controls.agravoId.value ? 'Nenhum caso do agravo fora de surto.' : 'Escolha o agravo para ver os casos.' }}</p>
                }
              </fieldset>
            </div>
            <footer class="dw-foot">
              <button type="button" class="btn btn--ghost" (click)="fechar()">Cancelar</button>
              <button type="submit" class="btn" [disabled]="salvando()">{{ salvando() ? 'Salvando…' : 'Abrir surto' }}</button>
            </footer>
          </form>
        } @else {
          <div class="dw-form">
            <div class="dw-body">
              @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
              @if (detalhe(); as s) {
                <dl class="ro">
                  <div><dt>Agravo</dt><dd>{{ s.agravoNome }}@if (s.sensivel) { · sensível }</dd></div>
                  <div><dt>Situação</dt><dd><span [class]="'tag tag--' + situacoes[s.situacao].classe">{{ situacoes[s.situacao].rotulo }}</span></dd></div>
                  <div><dt>Onde</dt><dd>@if (s.tipo === 'INSTITUCIONAL') { {{ s.tipoInstituicao ? instituicoes[s.tipoInstituicao] : '' }} · {{ s.nomeInstituicao }}@if (s.enderecoInstituicao) { · {{ s.enderecoInstituicao }} } } @else { Comunitário@if (s.microareas.length) { · microáreas {{ codigos(s) }} } }</dd></div>
                  <div><dt>Período</dt><dd>{{ data(s.inicio) }} a {{ s.fim ? data(s.fim) : 'hoje' }} · {{ s.unidadeNome }}</dd></div>
                  <div><dt>Números</dt><dd>{{ s.casos }} {{ s.casos === 1 ? 'caso' : 'casos' }} · {{ s.confirmados }} {{ s.confirmados === 1 ? 'confirmado' : 'confirmados' }} · {{ s.obitos }} {{ s.obitos === 1 ? 'óbito' : 'óbitos' }}</dd></div>
                  @if (s.definicaoCaso) { <div><dt>Definição de caso</dt><dd>{{ s.definicaoCaso }}</dd></div> }
                  @if (s.conclusao) { <div><dt>Conclusão</dt><dd>{{ s.conclusao }}</dd></div> }
                </dl>
                <div class="row-actions" style="justify-content: flex-start; flex-wrap: wrap; margin: var(--s-2) 0;">
                  <button type="button" class="btn btn--ghost btn--sm" (click)="verNoMapa.emit(s.uuid); fechar()">Ver no mapa</button>
                  @if (podeRegistrarEmergencia() && s.situacao !== 'ENCERRADO' && s.situacao !== 'DESCARTADO') {
                    <button type="button" class="btn btn--ghost btn--sm" (click)="router.navigate(['/vigilancia/emergencias'], { queryParams: { surtoId: s.uuid } })">Registrar evento emergencial</button>
                  }
                  @if (podeInvestigar() && s.situacao !== 'ENCERRADO') {
                    @for (p of proximas(s.situacao); track p.situacao) {
                      <button type="button" class="btn btn--ghost btn--sm" (click)="mudar(s, p.situacao)">{{ p.rotulo }}</button>
                    }
                    @if (s.situacao === 'CONFIRMADO' || s.situacao === 'DESCARTADO') {
                      <button type="button" class="btn btn--sm" (click)="encerrando.set(!encerrando())">Encerrar…</button>
                    }
                  }
                </div>
                @if (encerrando()) {
                  <div class="dw-field">
                    <label for="f-surto-conclusao">Conclusão</label>
                    <textarea id="f-surto-conclusao" rows="2" maxlength="2000" [value]="conclusao()" (input)="conclusao.set($any($event.target).value)"></textarea>
                    <p class="dw-hint">Encerrar pede as medidas concluídas ou canceladas.</p>
                  </div>
                  <button type="button" class="btn btn--sm" (click)="mudar(s, 'ENCERRADO')" [disabled]="!conclusao().trim()">Encerrar o surto</button>
                }

                <h4 class="mo-grupo">Casos <span class="cell-sub" style="display: inline;">· {{ s.listaCasos.length }}</span></h4>
                @if (s.listaCasos.length) {
                  <ul class="eq-prof" aria-label="Casos do surto">
                    @for (c of s.listaCasos; track c.notificacaoId) {
                      <li class="mo-row">
                        <span>
                          <span class="cell-main">{{ c.pacienteNome }}</span>
                          <span class="cell-sub">{{ data(c.dataPrimeirosSintomas ?? c.dataNotificacao) }} · {{ situacoesNotificacao[c.situacao].rotulo }}@if (c.classificacao) { · {{ classificacoes[c.classificacao].rotulo }} }@if (c.microareaCodigo) { · microárea {{ c.microareaCodigo }} }</span>
                        </span>
                        <span class="row-actions">
                          <button type="button" class="link-btn" (click)="abrirNotificacao.emit(c.notificacaoId); fechar()" [attr.aria-label]="'Abrir caso: ' + c.pacienteNome">Abrir</button>
                          @if (podeInvestigar() && s.situacao !== 'ENCERRADO') {
                            <button type="button" class="link-btn" (click)="retirar(s, c.notificacaoId)" [attr.aria-label]="'Retirar do surto: ' + c.pacienteNome">Retirar</button>
                          }
                        </span>
                      </li>
                    }
                  </ul>
                }
                @if (podeInvestigar() && s.situacao !== 'ENCERRADO' && foraDoSurto(s).length) {
                  <div class="dw-field">
                    <label for="f-surto-incluir">Incluir caso</label>
                    <select id="f-surto-incluir" (change)="incluir(s, $any($event.target).value)">
                      <option value="">Escolha um caso de {{ s.agravoNome }}…</option>
                      @for (n of foraDoSurto(s); track n.uuid) { <option [value]="n.uuid">{{ n.pacienteNome }} · {{ data(n.dataPrimeirosSintomas ?? n.dataNotificacao) }}</option> }
                    </select>
                  </div>
                }

                <h4 class="mo-grupo">Medidas de controle</h4>
                <app-medidas-de-controle [medidas]="s.medidas" [surtoId]="s.uuid" [podeEditar]="podeInvestigar() && s.situacao !== 'ENCERRADO'"
                  (alterada)="recarregar(s.uuid)" />

                <h4 class="mo-grupo">Curva por semana epidemiológica</h4>
                <app-curva-epidemica [curva]="s.curva" rotulo="Casos do surto por semana epidemiológica" />

                <h4 class="mo-grupo">Linha do tempo</h4>
                <app-linha-do-tempo [eventos]="s.linhaDoTempo" rotulo="Linha do tempo do surto" />
              } @else if (!erro()) {
                <p class="cell-sub">Carregando o surto…</p>
              }
            </div>
            <footer class="dw-foot"><button type="button" class="btn btn--ghost" (click)="fechar()">Fechar</button></footer>
          </div>
        }
      </app-drawer>
    }
  `,
})
export class Surtos {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(VigilanciaService);

  readonly surtos = input<SurtosDto | null>(null);
  readonly agravos = input<AgravoDto[]>([]);
  readonly unidades = input<UnidadeSaudeResponseDto[]>([]);
  readonly notificacoes = input<NotificacaoDto[]>([]);
  readonly unidadePadrao = input('');
  readonly podeInvestigar = input(false);
  readonly novo = input<NovoSurto | null>(null);
  /** Quem registra evento emergencial (ADR-0156) leva o surto para Emergências. */
  readonly podeRegistrarEmergencia = input(false);
  protected readonly router = inject(Router);
  readonly abrirNotificacao = output<string>();
  readonly verNoMapa = output<string>();
  readonly alterado = output<void>();

  protected readonly situacoes = SITUACOES_SURTO;
  protected readonly listaSituacoes = Object.keys(SITUACOES_SURTO) as SituacaoSurto[];
  protected readonly situacoesNotificacao = SITUACOES_NOTIFICACAO;
  protected readonly classificacoes = CLASSIFICACOES;
  protected readonly instituicoes = TIPOS_INSTITUICAO;
  protected readonly listaInstituicoes = Object.keys(TIPOS_INSTITUICAO) as TipoInstituicaoSurto[];

  protected readonly situacaoFiltro = signal('');
  protected readonly gaveta = signal<'novo' | 'surto' | null>(null);
  protected readonly detalhe = signal<SurtoDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly salvando = signal(false);
  protected readonly escolhidos = signal<string[]>([]);
  protected readonly microareasNovas = signal<string[]>([]);
  protected readonly encerrando = signal(false);
  protected readonly conclusao = signal('');
  private readonly agravoEscolhido = signal('');
  protected readonly form = this.fb.nonNullable.group({
    agravoId: '', unidadeId: '', nome: '', tipo: 'COMUNITARIO', tipoInstituicao: '', nomeInstituicao: '', enderecoInstituicao: '', definicaoCaso: '',
    inicio: '',
  });

  protected readonly lista = computed(() => (this.surtos()?.lista ?? []).filter((s) => !this.situacaoFiltro() || s.situacao === this.situacaoFiltro()));

  /** Os casos do agravo escolhido que ainda não estão em surto. */
  protected readonly candidatos = computed(() => {
    const agravo = this.agravoEscolhido();
    return agravo ? this.notificacoes().filter((n) => n.agravoId === agravo && !n.surtoId) : [];
  });

  constructor() {
    this.form.controls.agravoId.valueChanges.subscribe((v) => this.agravoEscolhido.set(v));
    // Vindo do mapa: abre o formulário com o agravo, os casos e a microárea.
    effect(() => {
      const n = this.novo();
      if (n) untracked(() => this.abrirNovo(n));
    });
  }

  protected data(iso?: string | null): string {
    if (!iso) return '—';
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected codigos(s: SurtoDto): string {
    return s.microareas.map((m) => m.codigo).join(', ');
  }

  protected proximas(s: SituacaoSurto): { situacao: SituacaoSurto; rotulo: string }[] {
    if (s === 'SUSPEITO') return [{ situacao: 'EM_INVESTIGACAO', rotulo: 'Investigar' }, { situacao: 'DESCARTADO', rotulo: 'Descartar' }];
    if (s === 'EM_INVESTIGACAO') return [{ situacao: 'CONFIRMADO', rotulo: 'Confirmar' }, { situacao: 'DESCARTADO', rotulo: 'Descartar' }];
    return [{ situacao: 'EM_INVESTIGACAO', rotulo: 'Reabrir a investigação' }];
  }

  protected foraDoSurto(s: SurtoDto): NotificacaoDto[] {
    return this.notificacoes().filter((n) => n.agravoId === s.agravoId && !n.surtoId);
  }

  protected abrirNovo(n?: NovoSurto): void {
    const hoje = new Date();
    const inicio = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}-${String(hoje.getDate()).padStart(2, '0')}`;
    this.form.reset({ agravoId: n?.agravoId ?? '', unidadeId: this.unidadePadrao(), nome: '', tipo: 'COMUNITARIO', tipoInstituicao: '', nomeInstituicao: '',
      enderecoInstituicao: '', definicaoCaso: '', inicio });
    this.agravoEscolhido.set(n?.agravoId ?? '');
    this.escolhidos.set(n?.notificacoesIds ?? []);
    this.microareasNovas.set(n?.microareasIds ?? []);
    this.erro.set(null);
    this.gaveta.set('novo');
  }

  protected alternar(id: string, marcado: boolean): void {
    this.escolhidos.update((l) => (marcado ? [...l, id] : l.filter((x) => x !== id)));
  }

  protected salvarNovo(): void {
    const v = this.form.getRawValue();
    if (!v.agravoId || !v.unidadeId || !v.nome.trim() || !v.inicio) {
      this.erro.set('Informe o agravo, a unidade responsável, o nome e o início.');
      return;
    }
    if (v.tipo === 'INSTITUCIONAL' && (!v.tipoInstituicao || !v.nomeInstituicao.trim())) {
      this.erro.set('O surto institucional pede o tipo e o nome do estabelecimento.');
      return;
    }
    this.salvando.set(true);
    this.service.abrirSurto({
      agravoId: v.agravoId, unidadeId: v.unidadeId, nome: v.nome.trim(), tipo: v.tipo as TipoSurto,
      tipoInstituicao: v.tipo === 'INSTITUCIONAL' ? (v.tipoInstituicao as TipoInstituicaoSurto) : null,
      nomeInstituicao: v.nomeInstituicao.trim() || null, enderecoInstituicao: v.enderecoInstituicao.trim() || null,
      definicaoCaso: v.definicaoCaso.trim() || null, inicio: v.inicio, microareasIds: this.microareasNovas(), notificacoesIds: this.escolhidos(),
    }).subscribe({
      next: (s) => {
        this.salvando.set(false);
        this.alterado.emit();
        this.mostrar(s);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected abrirSurto(id: string): void {
    this.detalhe.set(null);
    this.erro.set(null);
    this.encerrando.set(false);
    this.gaveta.set('surto');
    this.recarregar(id);
  }

  protected recarregar(id: string): void {
    this.service.surto(id).subscribe({ next: (s) => this.detalhe.set(s), error: (e: HttpErrorResponse) => this.falhou(e) });
    this.alterado.emit();
  }

  protected mudar(s: SurtoDto, situacao: SituacaoSurto): void {
    this.service.mudarSituacaoSurto(s.uuid, situacao, situacao === 'ENCERRADO' ? this.conclusao().trim() : null).subscribe({
      next: (d) => {
        this.encerrando.set(false);
        this.conclusao.set('');
        this.alterado.emit();
        this.mostrar(d);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected incluir(s: SurtoDto, notificacaoId: string): void {
    if (!notificacaoId) return;
    this.service.incluirCasos(s.uuid, [notificacaoId]).subscribe({
      next: (d) => {
        this.alterado.emit();
        this.mostrar(d);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected retirar(s: SurtoDto, notificacaoId: string): void {
    this.service.retirarCaso(s.uuid, notificacaoId).subscribe({
      next: (d) => {
        this.alterado.emit();
        this.mostrar(d);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected fechar(): void {
    this.gaveta.set(null);
  }

  /** Mostra o surto e busca de novo: a linha do tempo vem da trilha de auditoria, gravada depois da resposta da ação. */
  private mostrar(s: SurtoDto): void {
    this.erro.set(null);
    this.detalhe.set(s);
    this.gaveta.set('surto');
    this.service.surto(s.uuid).subscribe({ next: (d) => this.detalhe.set(d), error: () => undefined });
  }

  private falhou(e: HttpErrorResponse): void {
    this.salvando.set(false);
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir. Tente novamente.');
  }
}
