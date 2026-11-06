import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import {
  CamaraRefrigeracaoDto,
  EstoqueSalaVacinaDto,
  ImunobiologicoDto,
  LoteImunobiologicoDto,
  MOTIVOS_PERDA,
  MotivoPerdaImunobiologico,
  MovimentacaoImunobiologicoDto,
  SITUACOES_LOTE,
  TIPOS_MOVIMENTACAO,
  dataBr,
  prazoTexto,
} from '../../../core/models/imunizacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta = { tipo: 'entrada' } | { tipo: 'perda' | 'ajuste' | 'livro'; lote: LoteImunobiologicoDto } | null;

/**
 * Estoque da sala de vacina (ADR-0148): o saldo aplicável por vacina, os lotes com a validade e a situação, os frascos
 * multidose abertos com o prazo e o mês (aplicadas e perdas). A entrada e a perda física são de quem vacina; o ajuste
 * de inventário, da coordenação (a API responde 403 a quem não pode).
 */
@Component({
  selector: 'app-estoque-sala-vacina',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (estoque(); as e) {
      @if (!e.temSalaDeVacina) {
        <div class="box box--info" role="status">
          <strong>{{ e.unidadeNome }} não controla estoque de vacina.</strong>
          Para controlar o estoque e a cadeia de frio, a unidade precisa da capacidade "Sala de vacina" no modelo operacional. Sem ela, a dose
          aplicada leva o lote em texto.
        </div>
      } @else {
        <div class="kpis" style="margin-bottom: var(--s-4);">
          <div class="kpi"><div class="kpi__value">{{ e.aplicadasMes }}</div><div class="kpi__label">Doses aplicadas no mês</div></div>
          <div class="kpi"><div class="kpi__value">{{ e.frascos.length }}</div><div class="kpi__label">Frascos abertos agora</div></div>
          <div class="kpi"><div class="kpi__value">{{ e.perdaTecnicaMes }}</div><div class="kpi__label">Perda técnica no mês (frasco vencido)</div></div>
          <div class="kpi"><div class="kpi__value">{{ e.perdaFisicaMes }}</div><div class="kpi__label">Perda física no mês</div></div>
        </div>

        <section class="card" style="padding: var(--s-5);" aria-label="Lotes no estoque">
          <div class="est-cab">
            <h3 class="mo-grupo">Lotes</h3>
            <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'entrada' })">Entrada de lote</button>
          </div>
          @if (e.porVacina.length) {
            <div class="est-chips" aria-label="Doses aplicáveis por vacina">
              @for (s of e.porVacina; track s.imunobiologicoId) { <span class="tag tag--info" [title]="s.nome">{{ s.sigla }}: {{ s.doses }}</span> }
            </div>
          }
          <div class="tbl-wrap est-tabela">
            <table aria-label="Lotes">
              <thead><tr><th scope="col">Vacina</th><th scope="col">Lote</th><th scope="col">Validade</th><th scope="col">Doses</th><th scope="col">Câmara</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
              <tbody>
                @for (l of e.lotes; track l.uuid) {
                  <tr>
                    <td>{{ l.sigla }}</td>
                    <td class="mono">{{ l.numeroLote }}</td>
                    <td>{{ data(l.validade) }}
                      @if (l.vencido) { <span class="tag tag--alert">Vencido</span> } @else if (l.venceEm30Dias) { <span class="tag tag--warn">Vence em 30 dias</span> }</td>
                    <td class="mono">{{ l.doses }}</td>
                    <td>{{ l.camaraNome ?? '—' }}</td>
                    <td><span class="tag tag--{{ situacoes[l.situacao].classe }}">{{ situacoes[l.situacao].rotulo }}</span></td>
                    <td class="est-acoes">
                      <button type="button" class="link-btn" (click)="abrir({ tipo: 'perda', lote: l })" [attr.aria-label]="'Perda do lote ' + l.numeroLote">Perda</button>
                      <button type="button" class="link-btn" (click)="abrir({ tipo: 'ajuste', lote: l })" [attr.aria-label]="'Ajuste do lote ' + l.numeroLote">Ajuste</button>
                      <button type="button" class="link-btn" (click)="abrir({ tipo: 'livro', lote: l })" [attr.aria-label]="'Livro do lote ' + l.numeroLote">Livro</button>
                    </td>
                  </tr>
                } @empty { <tr><td colspan="7" class="cell-sub">Nenhum lote no estoque. Registre a entrada do que chegou.</td></tr> }
              </tbody>
            </table>
          </div>
        </section>

        <section class="card" style="padding: var(--s-5); margin-top: var(--s-4);" aria-label="Frascos abertos">
          <h3 class="mo-grupo">Frascos multidose abertos</h3>
          <div class="tbl-wrap est-tabela">
            <table aria-label="Frascos abertos">
              <thead><tr><th scope="col">Vacina · lote</th><th scope="col">Aberto às</th><th scope="col">Restam</th><th scope="col">Vale por</th></tr></thead>
              <tbody>
                @for (f of e.frascos; track f.uuid) {
                  <tr>
                    <td>{{ f.sigla }} · <span class="mono">{{ f.numeroLote }}</span></td>
                    <td class="mono">{{ data(f.abertoEm.slice(0, 10)) }} {{ f.abertoEm.slice(11, 16) }}</td>
                    <td class="mono">{{ f.dosesRestantes }} de {{ f.dosesNoFrasco }}</td>
                    <td><span class="tag" [class.tag--warn]="f.minutosRestantes < 60" [class.tag--info]="f.minutosRestantes >= 60">{{ prazo(f.minutosRestantes) }}</span></td>
                  </tr>
                } @empty { <tr><td colspan="4" class="cell-sub">Nenhum frasco aberto. O frasco se abre na primeira dose aplicada.</td></tr> }
              </tbody>
            </table>
          </div>
        </section>
      }
    } @else if (!erro()) {
      <p class="cell-sub">Carregando o estoque…</p>
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @if (g.tipo === 'entrada') {
              <div class="dw-field">
                <label for="f-ent-imuno">Vacina <span class="req" aria-hidden="true">*</span></label>
                <select id="f-ent-imuno" [value]="imunoId()" (change)="imunoId.set($any($event.target).value)">
                  <option value="">Escolha…</option>
                  @for (i of imunosAtivos(); track i.uuid) { <option [value]="i.uuid">{{ i.sigla }} · {{ i.nome }}</option> }
                </select>
              </div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-ent-lote">Número do lote <span class="req" aria-hidden="true">*</span></label><input id="f-ent-lote" type="text" maxlength="40" [value]="numero()" (input)="numero.set($any($event.target).value)" /></div>
                <div class="dw-field"><label for="f-ent-fab">Fabricante<span class="opt">opcional</span></label><input id="f-ent-fab" type="text" maxlength="100" [value]="fabricante()" (input)="fabricante.set($any($event.target).value)" /></div>
              </div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-ent-validade">Validade <span class="req" aria-hidden="true">*</span></label><input id="f-ent-validade" type="date" [value]="validade()" (change)="validade.set($any($event.target).value)" /></div>
                <div class="dw-field"><label for="f-ent-doses">Doses recebidas <span class="req" aria-hidden="true">*</span></label><input id="f-ent-doses" type="number" min="1" [value]="quantidade()" (input)="quantidade.set($any($event.target).value)" /></div>
              </div>
              <div class="dw-field">
                <label for="f-ent-camara">Câmara<span class="opt">opcional</span></label>
                <select id="f-ent-camara" [value]="camaraId()" (change)="camaraId.set($any($event.target).value)">
                  <option value="">—</option>
                  @for (c of camaras(); track c.uuid) { @if (c.ativa) { <option [value]="c.uuid">{{ c.nome }}</option> } }
                </select>
                <p class="dw-hint">Na câmara, o lote fica sob suspeita se uma leitura sair da faixa.</p>
              </div>
            } @else if (g.tipo === 'perda') {
              <dl class="ro"><div><dt>Lote</dt><dd>{{ g.lote.sigla }} · {{ g.lote.numeroLote }} ({{ g.lote.doses }} doses)</dd></div></dl>
              <div class="dw-row">
                <div class="dw-field"><label for="f-per-doses">Doses perdidas <span class="req" aria-hidden="true">*</span></label><input id="f-per-doses" type="number" min="1" [value]="quantidade()" (input)="quantidade.set($any($event.target).value)" /></div>
                <div class="dw-field">
                  <label for="f-per-motivo">Motivo <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-per-motivo" [value]="motivo()" (change)="motivo.set($any($event.target).value)">
                    @for (m of motivosLista; track m) { <option [value]="m">{{ motivos[m] }}</option> }
                  </select>
                </div>
              </div>
              <div class="dw-field"><label for="f-per-just">Justificativa@if (motivo() !== 'OUTRO') { <span class="opt">opcional</span> }</label><textarea id="f-per-just" rows="2" maxlength="500" [value]="justificativa()" (input)="justificativa.set($any($event.target).value)"></textarea></div>
            } @else if (g.tipo === 'ajuste') {
              <dl class="ro"><div><dt>Lote</dt><dd>{{ g.lote.sigla }} · {{ g.lote.numeroLote }}</dd></div><div><dt>Saldo no sistema</dt><dd>{{ g.lote.doses }} doses</dd></div></dl>
              <div class="dw-field"><label for="f-aj-saldo">Saldo contado <span class="req" aria-hidden="true">*</span></label><input id="f-aj-saldo" type="number" min="0" [value]="quantidade()" (input)="quantidade.set($any($event.target).value)" /></div>
              <div class="dw-field"><label for="f-aj-just">Justificativa <span class="req" aria-hidden="true">*</span></label><textarea id="f-aj-just" rows="2" maxlength="500" [value]="justificativa()" (input)="justificativa.set($any($event.target).value)"></textarea></div>
            } @else {
              <div class="tbl-wrap est-tabela">
                <table aria-label="Livro do lote">
                  <thead><tr><th scope="col">Quando</th><th scope="col">Lançamento</th><th scope="col">Doses</th><th scope="col">Saldo</th></tr></thead>
                  <tbody>
                    @for (m of livro(); track m.uuid) {
                      <tr>
                        <td class="mono">{{ quando(m.registradoEm) }}</td>
                        <td>{{ tipos[m.tipo] }}@if (m.justificativa) { <span class="cell-sub">{{ m.justificativa }}</span> }</td>
                        <td class="mono">{{ m.doses > 0 ? '+' + m.doses : m.doses }}</td>
                        <td class="mono">{{ m.saldoApos }}</td>
                      </tr>
                    } @empty { <tr><td colspan="4" class="cell-sub">Carregando…</td></tr> }
                  </tbody>
                </table>
              </div>
            }
          </div>
          <div class="dw-foot">
            <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">{{ g.tipo === 'livro' ? 'Fechar' : 'Cancelar' }}</button>
            @if (g.tipo !== 'livro') { <button type="submit" class="btn" [disabled]="salvando()">Registrar</button> }
          </div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .est-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-3); margin-bottom: var(--s-3); }
    .est-cab .mo-grupo { margin: 0; }
    .est-chips { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: var(--s-3); }
    .est-tabela table { min-width: 0; width: 100%; }
    .est-acoes { white-space: nowrap; text-align: right; }
    .est-acoes .link-btn + .link-btn { margin-left: var(--s-3); }
  `,
})
export class EstoqueSalaVacina {
  private readonly service = inject(ImunizacaoService);

  readonly unidadeId = input.required<string>();
  /** Muda quando uma dose é registrada fora daqui: recarrega. */
  readonly versao = input(0);

  protected readonly situacoes = SITUACOES_LOTE;
  protected readonly tipos = TIPOS_MOVIMENTACAO;
  protected readonly motivos = MOTIVOS_PERDA;
  protected readonly motivosLista = Object.keys(MOTIVOS_PERDA) as MotivoPerdaImunobiologico[];

  protected readonly estoque = signal<EstoqueSalaVacinaDto | null>(null);
  protected readonly camaras = signal<CamaraRefrigeracaoDto[]>([]);
  protected readonly imunos = signal<ImunobiologicoDto[]>([]);
  protected readonly livro = signal<MovimentacaoImunobiologicoDto[]>([]);
  protected readonly gaveta = signal<Gaveta>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly salvando = signal(false);
  protected readonly imunoId = signal('');
  protected readonly numero = signal('');
  protected readonly fabricante = signal('');
  protected readonly validade = signal('');
  protected readonly quantidade = signal('');
  protected readonly camaraId = signal('');
  protected readonly motivo = signal<MotivoPerdaImunobiologico>('QUEBRA');
  protected readonly justificativa = signal('');

  protected readonly imunosAtivos = computed(() => this.imunos().filter((i) => i.ativo));

  constructor() {
    this.service.imunobiologicos().pipe(catchError(() => of([]))).subscribe((l) => this.imunos.set(l));
    effect(() => {
      const u = this.unidadeId();
      this.versao();
      untracked(() => this.carregar(u));
    });
  }

  protected data(iso: string): string {
    return dataBr(iso);
  }

  protected prazo(minutos: number): string {
    return prazoTexto(minutos);
  }

  protected quando(instante: string): string {
    const d = new Date(instante);
    return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  }

  protected titulo(g: NonNullable<Gaveta>): string {
    return g.tipo === 'entrada' ? 'Entrada de lote' : g.tipo === 'perda' ? 'Registrar perda' : g.tipo === 'ajuste' ? 'Ajuste de inventário' : `Livro do lote ${g.lote.numeroLote}`;
  }

  protected abrir(g: NonNullable<Gaveta>): void {
    this.erroGaveta.set(null);
    this.imunoId.set('');
    this.numero.set('');
    this.fabricante.set('');
    this.validade.set('');
    this.camaraId.set('');
    this.motivo.set('QUEBRA');
    this.justificativa.set('');
    this.quantidade.set(g.tipo === 'ajuste' ? String(g.lote.doses) : '');
    this.livro.set([]);
    if (g.tipo === 'livro') {
      this.service.movimentacoes(g.lote.uuid).subscribe({ next: (l) => this.livro.set(l), error: (e: HttpErrorResponse) => this.erroGaveta.set(mensagem(e)) });
    }
    this.gaveta.set(g);
  }

  protected salvar(g: NonNullable<Gaveta>): void {
    const n = Number(this.quantidade());
    let chamada;
    if (g.tipo === 'entrada') {
      if (!this.imunoId() || !this.numero().trim() || !this.validade() || !(n > 0)) {
        this.erroGaveta.set('Preencha a vacina, o lote, a validade e as doses.');
        return;
      }
      chamada = this.service.entrada({
        unidadeId: this.unidadeId(),
        imunobiologicoId: this.imunoId(),
        numeroLote: this.numero().trim(),
        fabricante: this.fabricante().trim() || null,
        validade: this.validade(),
        doses: n,
        camaraId: this.camaraId() || null,
      });
    } else if (g.tipo === 'perda') {
      if (!(n > 0)) {
        this.erroGaveta.set('Informe as doses perdidas.');
        return;
      }
      chamada = this.service.perda(g.lote.uuid, n, this.motivo(), this.justificativa().trim() || null);
    } else if (g.tipo === 'ajuste') {
      if (this.quantidade() === '' || n < 0 || !this.justificativa().trim()) {
        this.erroGaveta.set('Informe o saldo contado e a justificativa.');
        return;
      }
      chamada = this.service.ajuste(g.lote.uuid, n, this.justificativa().trim());
    } else {
      return;
    }
    this.salvando.set(true);
    this.erroGaveta.set(null);
    chamada.subscribe({
      next: () => {
        this.salvando.set(false);
        this.gaveta.set(null);
        this.carregar(this.unidadeId());
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erroGaveta.set(e.status === 403 ? 'Seu acesso não inclui esta operação nesta unidade.' : mensagem(e));
      },
    });
  }

  private carregar(unidadeId: string): void {
    this.erro.set(null);
    this.service.estoque(unidadeId).subscribe({
      next: (e) => {
        this.estoque.set(e);
        if (e.temSalaDeVacina) {
          this.service.cadeiaDeFrio(unidadeId).pipe(catchError(() => of(null))).subscribe((c) => this.camaras.set(c?.camaras ?? []));
        }
      },
      error: (e: HttpErrorResponse) => {
        this.estoque.set(null);
        this.erro.set(e.status === 403 ? 'Seu acesso não inclui o estoque da sala de vacina desta unidade.' : mensagem(e));
      },
    });
  }
}

function mensagem(e: HttpErrorResponse): string {
  return (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir a operação.';
}
