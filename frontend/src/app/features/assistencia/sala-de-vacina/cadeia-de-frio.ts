import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import {
  CadeiaFrioDto,
  CamaraRefrigeracaoDto,
  LoteImunobiologicoDto,
  TURNOS,
  TurnoLeituraTemperatura,
  dataBr,
} from '../../../core/models/imunizacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AuthService } from '../../../core/services/auth';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta = { tipo: 'camara'; camara: CamaraRefrigeracaoDto | null } | { tipo: 'leitura'; camara: CamaraRefrigeracaoDto } | { tipo: 'avaliacao'; lote: LoteImunobiologicoDto } | null;

/**
 * Cadeia de frio da sala de vacina (ADR-0148): as câmaras com a faixa e a última leitura, as duas leituras do dia (os
 * turnos sem leitura aparecem como pendência) e os lotes sob suspeita depois de uma excursão, para a coordenação avaliar.
 */
@Component({
  selector: 'app-cadeia-de-frio',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    <div class="cf-cab">
      <div class="dw-field" style="margin: 0;"><label for="f-cf-data">Dia</label><input id="f-cf-data" type="date" [value]="data()" (change)="data.set($any($event.target).value)" /></div>
      <button type="button" class="btn btn--sm btn--ghost" (click)="abrir({ tipo: 'camara', camara: null })">Nova câmara</button>
    </div>
    @if (painel(); as p) {
      @if (p.pendencias.length) {
        <div class="box box--warn" role="status" aria-label="Leituras pendentes">
          <strong>Leituras pendentes</strong>
          <ul class="cf-lista">@for (x of p.pendencias; track x) { <li>{{ x }}</li> }</ul>
        </div>
      }
      @if (p.lotesSobSuspeita.length) {
        <section class="card cf-bloco" aria-label="Lotes sob suspeita">
          <h3 class="mo-grupo">Lotes sob suspeita</h3>
          <p class="cell-sub">Não se aplicam até a coordenação avaliar, com a consulta ao fabricante ou à rede de frio.</p>
          <ul class="eq-prof">
            @for (l of p.lotesSobSuspeita; track l.uuid) {
              <li class="mo-row">
                <span><span class="cell-main">{{ l.sigla }} · {{ l.numeroLote }}</span><span class="cell-sub">{{ l.doses }} doses · {{ l.camaraNome ?? 'sem câmara' }} · validade {{ fmt(l.validade) }}</span></span>
                <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'avaliacao', lote: l })" [attr.aria-label]="'Avaliar o lote ' + l.numeroLote">Avaliar</button>
              </li>
            }
          </ul>
        </section>
      }
      <section class="card cf-bloco" aria-label="Câmaras">
        <h3 class="mo-grupo">Câmaras</h3>
        <ul class="eq-prof">
          @for (c of p.camaras; track c.uuid) {
            <li class="mo-row">
              <span>
                <span class="cell-main">{{ c.nome }} @if (!c.ativa) { <span class="tag tag--muted">Desativada</span> }</span>
                <span class="cell-sub">Faixa de {{ c.temperaturaMinima }} a {{ c.temperaturaMaxima }} °C
                  @if (c.ultimaLeitura; as u) { · última: {{ u.temperaturaAtual }} °C em {{ fmt(u.lidaEm.slice(0, 10)) }} {{ u.lidaEm.slice(11, 16) }} }</span>
              </span>
              <span class="cf-acoes">
                @if (c.lotesSobSuspeita) { <span class="tag tag--alert">{{ c.lotesSobSuspeita }} sob suspeita</span> }
                @if (c.ativa) { <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'leitura', camara: c })" [attr.aria-label]="'Registrar leitura da ' + c.nome">Registrar leitura</button> }
                <button type="button" class="link-btn" (click)="abrir({ tipo: 'camara', camara: c })" [attr.aria-label]="'Editar a ' + c.nome">Editar</button>
              </span>
            </li>
          } @empty { <li class="cell-sub">Nenhuma câmara cadastrada. A gestão da imunização cadastra as câmaras da sala.</li> }
        </ul>
      </section>
      <section class="card cf-bloco" aria-label="Leituras do dia">
        <h3 class="mo-grupo">Leituras do dia</h3>
        <div class="tbl-wrap cf-tabela">
          <table aria-label="Leituras de temperatura">
            <thead><tr><th scope="col">Hora</th><th scope="col">Câmara</th><th scope="col">Turno</th><th scope="col">Atual</th><th scope="col">Mín.</th><th scope="col">Máx.</th><th scope="col">Faixa</th></tr></thead>
            <tbody>
              @for (l of p.leituras; track l.uuid) {
                <tr>
                  <td class="mono">{{ l.lidaEm.slice(11, 16) }}</td>
                  <td>{{ l.camaraNome }}</td>
                  <td>{{ turnos[l.turno] }}</td>
                  <td class="mono">{{ l.temperaturaAtual }} °C</td>
                  <td class="mono">{{ l.temperaturaMinima }} °C</td>
                  <td class="mono">{{ l.temperaturaMaxima }} °C</td>
                  <td>@if (l.excursao) { <span class="tag tag--alert" [title]="l.observacao ?? ''">Excursão</span> } @else { <span class="tag tag--ok">Na faixa</span> }</td>
                </tr>
              } @empty { <tr><td colspan="7" class="cell-sub">Nenhuma leitura neste dia.</td></tr> }
            </tbody>
          </table>
        </div>
      </section>
    } @else if (!erro()) {
      <p class="cell-sub">Carregando a cadeia de frio…</p>
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="g.tipo === 'camara' ? (g.camara ? 'Editar câmara' : 'Nova câmara') : g.tipo === 'leitura' ? 'Leitura de ' + g.camara.nome : 'Avaliar lote sob suspeita'" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g, 'LIBERAR')" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @if (g.tipo === 'camara') {
              <div class="dw-field"><label for="f-cam-nome">Nome <span class="req" aria-hidden="true">*</span></label><input id="f-cam-nome" type="text" maxlength="80" placeholder="Ex.: Câmara 1" [value]="nome()" (input)="nome.set($any($event.target).value)" /></div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-cam-min">Mínima (°C)</label><input id="f-cam-min" type="number" step="0.1" [value]="tMin()" (input)="tMin.set($any($event.target).value)" /></div>
                <div class="dw-field"><label for="f-cam-max">Máxima (°C)</label><input id="f-cam-max" type="number" step="0.1" [value]="tMax()" (input)="tMax.set($any($event.target).value)" /></div>
              </div>
              @if (g.camara) {
                <label class="switch" for="f-cam-ativa"><span><b style="font-weight: 500;">Ativa</b></span>
                  <input type="checkbox" role="switch" id="f-cam-ativa" [checked]="ativa()" (change)="ativa.set($any($event.target).checked)" /></label>
              }
            } @else if (g.tipo === 'leitura') {
              <p class="dw-hint">Faixa da câmara: {{ g.camara.temperaturaMinima }} a {{ g.camara.temperaturaMaxima }} °C. Fora dela é excursão: os lotes da câmara ficam sob suspeita.</p>
              <div class="dw-row">
                <div class="dw-field"><label for="f-lei-atual">Atual (°C) <span class="req" aria-hidden="true">*</span></label><input id="f-lei-atual" type="number" step="0.1" [value]="tAtual()" (input)="tAtual.set($any($event.target).value)" /></div>
                <div class="dw-field">
                  <label for="f-lei-turno">Turno</label>
                  <select id="f-lei-turno" [value]="turno()" (change)="turno.set($any($event.target).value)">
                    @for (t of turnosLista; track t) { <option [value]="t">{{ turnos[t] }}</option> }
                  </select>
                </div>
              </div>
              <div class="dw-row">
                <div class="dw-field"><label for="f-lei-min">Mínima do período (°C) <span class="req" aria-hidden="true">*</span></label><input id="f-lei-min" type="number" step="0.1" [value]="tMin()" (input)="tMin.set($any($event.target).value)" /></div>
                <div class="dw-field"><label for="f-lei-max">Máxima do período (°C) <span class="req" aria-hidden="true">*</span></label><input id="f-lei-max" type="number" step="0.1" [value]="tMax()" (input)="tMax.set($any($event.target).value)" /></div>
              </div>
              <div class="dw-field"><label for="f-lei-obs">Observação<span class="opt">opcional</span></label><input id="f-lei-obs" type="text" maxlength="500" [value]="texto()" (input)="texto.set($any($event.target).value)" /></div>
            } @else {
              <dl class="ro"><div><dt>Lote</dt><dd>{{ g.lote.sigla }} · {{ g.lote.numeroLote }} ({{ g.lote.doses }} doses)</dd></div></dl>
              <div class="dw-field"><label for="f-ava-parecer">Parecer <span class="req" aria-hidden="true">*</span></label><textarea id="f-ava-parecer" rows="3" maxlength="500" placeholder="Ex.: consulta ao fabricante, estabilidade, decisão da rede de frio" [value]="texto()" (input)="texto.set($any($event.target).value)"></textarea></div>
              <p class="dw-hint">Inutilizar tira o saldo inteiro do estoque como perda física por excursão de temperatura.</p>
            }
          </div>
          <div class="dw-foot">
            <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button>
            @if (g.tipo === 'avaliacao') {
              <button type="button" class="btn btn--danger" [disabled]="salvando()" (click)="salvar(g, 'INUTILIZAR')">Inutilizar</button>
              <button type="submit" class="btn" [disabled]="salvando()">Liberar</button>
            } @else {
              <button type="submit" class="btn" [disabled]="salvando()">Salvar</button>
            }
          </div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .cf-cab { display: flex; justify-content: space-between; align-items: flex-end; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-4); }
    .cf-bloco { padding: var(--s-5); margin-top: var(--s-4); }
    .cf-bloco .mo-grupo { margin-top: 0; }
    .cf-lista { margin: 6px 0 0; padding-left: 18px; }
    .cf-acoes { display: flex; gap: var(--s-3); align-items: center; flex-wrap: wrap; justify-content: flex-end; }
    .cf-tabela table { min-width: 0; width: 100%; }
  `,
})
export class CadeiaDeFrio {
  private readonly service = inject(ImunizacaoService);
  private readonly authService = inject(AuthService);

  readonly unidadeId = input.required<string>();
  readonly versao = input(0);

  protected readonly turnos = TURNOS;
  protected readonly turnosLista = Object.keys(TURNOS) as TurnoLeituraTemperatura[];
  protected readonly painel = signal<CadeiaFrioDto | null>(null);
  protected readonly data = signal(hojeIso());
  protected readonly gaveta = signal<Gaveta>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly salvando = signal(false);
  protected readonly nome = signal('');
  protected readonly tMin = signal('');
  protected readonly tMax = signal('');
  protected readonly tAtual = signal('');
  protected readonly ativa = signal(true);
  protected readonly turno = signal<TurnoLeituraTemperatura>('MANHA');
  protected readonly texto = signal('');
  private matricula: string | null = null;

  constructor() {
    this.authService.usuarioAtual().pipe(catchError(() => of(null))).subscribe((u) => (this.matricula = u?.profissionalMatricula ?? null));
    effect(() => {
      const u = this.unidadeId();
      const d = this.data();
      this.versao();
      untracked(() => this.carregar(u, d));
    });
  }

  protected fmt(iso: string): string {
    return dataBr(iso);
  }

  protected abrir(g: NonNullable<Gaveta>): void {
    this.erroGaveta.set(null);
    this.texto.set('');
    this.tAtual.set('');
    if (g.tipo === 'camara') {
      this.nome.set(g.camara?.nome ?? '');
      this.tMin.set(String(g.camara?.temperaturaMinima ?? 2));
      this.tMax.set(String(g.camara?.temperaturaMaxima ?? 8));
      this.ativa.set(g.camara?.ativa ?? true);
    } else {
      this.tMin.set('');
      this.tMax.set('');
      this.turno.set(new Date().getHours() < 12 ? 'MANHA' : 'TARDE');
    }
    this.gaveta.set(g);
  }

  protected salvar(g: NonNullable<Gaveta>, decisao: 'LIBERAR' | 'INUTILIZAR'): void {
    let chamada: Observable<unknown>;
    const min = this.tMin() === '' ? NaN : Number(this.tMin());
    const max = this.tMax() === '' ? NaN : Number(this.tMax());
    if (g.tipo === 'camara') {
      if (!this.nome().trim() || isNaN(min) || isNaN(max)) {
        this.erroGaveta.set('Informe o nome e a faixa de temperatura.');
        return;
      }
      chamada = g.camara
        ? this.service.atualizarCamara(g.camara.uuid, this.unidadeId(), this.nome().trim(), min, max, this.ativa())
        : this.service.criarCamara(this.unidadeId(), this.nome().trim(), min, max);
    } else if (g.tipo === 'leitura') {
      const atual = this.tAtual() === '' ? NaN : Number(this.tAtual());
      if (isNaN(atual) || isNaN(min) || isNaN(max)) {
        this.erroGaveta.set('Informe a temperatura atual, a mínima e a máxima.');
        return;
      }
      chamada = this.service.leitura(g.camara.uuid, {
        temperaturaAtual: atual,
        temperaturaMinima: min,
        temperaturaMaxima: max,
        turno: this.turno(),
        profissionalMatricula: this.matricula,
        observacao: this.texto().trim() || null,
      });
    } else {
      if (!this.texto().trim()) {
        this.erroGaveta.set('Informe o parecer.');
        return;
      }
      chamada = this.service.avaliar(g.lote.uuid, decisao, this.texto().trim());
    }
    this.salvando.set(true);
    this.erroGaveta.set(null);
    chamada.subscribe({
      next: () => {
        this.salvando.set(false);
        this.gaveta.set(null);
        this.carregar(this.unidadeId(), this.data());
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erroGaveta.set(e.status === 403 ? 'Seu acesso não inclui esta operação nesta unidade.' : mensagem(e));
      },
    });
  }

  private carregar(unidadeId: string, data: string): void {
    this.erro.set(null);
    this.service.cadeiaDeFrio(unidadeId, data).subscribe({
      next: (p) => this.painel.set(p),
      error: (e: HttpErrorResponse) => {
        this.painel.set(null);
        this.erro.set(e.status === 403 ? 'Seu acesso não inclui a cadeia de frio desta unidade.' : mensagem(e));
      },
    });
  }
}

function mensagem(e: HttpErrorResponse): string {
  return (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir a operação.';
}

function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
