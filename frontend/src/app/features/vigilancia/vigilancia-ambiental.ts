import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, input, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../core/models/profissional';
import {
  AmostraAguaDto,
  PontoColetaAguaDto,
  RiscoAmbientalDto,
  TIPOS_ABASTECIMENTO,
  TIPOS_RISCO_AMBIENTAL,
  TipoAbastecimentoAgua,
  TipoRiscoAmbiental,
} from '../../core/models/vigilancia-ambiental';
import { VigilanciaAmbientalService } from '../../core/services/vigilancia-ambiental';
import { Drawer } from '../../shared/drawer/drawer';

export type SecaoAmbiental = 'agua' | 'riscos';
type Gaveta =
  | { tipo: 'ponto' }
  | { tipo: 'amostra'; ponto: PontoColetaAguaDto | null }
  | { tipo: 'medida'; amostra: AmostraAguaDto }
  | { tipo: 'risco' }
  | { tipo: 'controle'; risco: RiscoAmbientalDto };

/**
 * A vigilância ambiental (ADR-0164), nas abas da tela Zoonoses e vetores: os pontos de coleta e as amostras da qualidade
 * da água, com a conformidade pela Portaria GM/MS 888/2021 e a medida da fora do padrão, e os riscos ambientais.
 */
@Component({
  selector: 'app-vigilancia-ambiental',
  imports: [Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (secao() === 'agua') {
      <div class="va-cab">
        <p class="cell-sub" style="margin: 0; max-width: 70ch;">Conformidade pela Portaria GM/MS 888/2021: E. coli ausente; na água tratada, coliformes totais ausentes e cloro residual livre de 0,2 a 5 mg/L; turbidez até 5 uT; fluoreto até 1,5 mg/L.</p>
        <div class="row-actions">
          @if (podeGerenciar()) { <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'ponto' }, { tipo: 'REDE' })">Novo ponto</button> }
          @if (podeRegistrar()) { <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'amostra', ponto: null }, { data: hoje() })" [disabled]="!pontos().length">Registrar amostra</button> }
        </div>
      </div>
      <div class="tbl-wrap card">
        <table class="va-tabela" aria-label="Pontos de coleta">
          <thead><tr><th scope="col">Ponto</th><th scope="col">Abastecimento</th><th scope="col">Última coleta</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (p of pontos(); track p.uuid) {
              <tr>
                <td><span class="cell-main">{{ p.nome }}</span><span class="cell-sub">{{ p.local }}@if (p.microareaCodigo) { · {{ p.areaNome }}, microárea {{ p.microareaCodigo }} }</span></td>
                <td>{{ abastecimentos[p.tipo] }}</td>
                <td>@if (p.ultimaColeta) { {{ data(p.ultimaColeta) }} <span [class]="'tag tag--' + (p.ultimaConforme ? 'ok' : 'alert')">{{ p.ultimaConforme ? 'Conforme' : 'Fora do padrão' }}</span><span class="cell-sub">{{ p.amostras }} amostra(s)</span> } @else { <span class="cell-sub">sem coleta</span> }</td>
                <td class="row-actions">@if (podeRegistrar()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'amostra', ponto: p }, { pontoId: p.uuid, data: hoje() })" [attr.aria-label]="'Amostra em ' + p.nome">Amostra</button> }</td>
              </tr>
            } @empty { <tr><td colspan="4" class="cell-sub">Nenhum ponto de coleta.</td></tr> }
          </tbody>
        </table>
      </div>
      <h3 class="mo-grupo va-titulo">Amostras</h3>
      <div class="tbl-wrap card">
        <table class="va-tabela" aria-label="Amostras de água">
          <thead><tr><th scope="col">Ponto</th><th scope="col">Coleta</th><th scope="col">Parâmetros</th><th scope="col">Resultado</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (a of amostras(); track a.uuid) {
              <tr>
                <td class="cell-main">{{ a.pontoNome }}</td>
                <td class="mono">{{ data(a.dataColeta) }}</td>
                <td class="cell-sub">{{ parametros(a) }}</td>
                <td>@if (a.conforme) { <span class="tag tag--ok">Conforme</span> } @else { <span class="tag tag--alert">Fora do padrão</span><span class="cell-sub">{{ a.naoConformidades.join('; ') }}</span> }@if (a.medida) { <span class="cell-sub">Medida: {{ a.medida }}</span> }</td>
                <td class="row-actions">@if (!a.conforme && !a.medida && podeGerenciar()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'medida', amostra: a })" [attr.aria-label]="'Medida da amostra de ' + a.pontoNome">Medida</button> }</td>
              </tr>
            } @empty { <tr><td colspan="5" class="cell-sub">Nenhuma amostra.</td></tr> }
          </tbody>
        </table>
      </div>
    } @else {
      <div class="va-cab">
        <span class="cell-sub">Riscos permanentes do território. A exposição de um desastre fica na emergência.</span>
        @if (podeGerenciar()) { <button type="button" class="btn btn--sm" (click)="abrir({ tipo: 'risco' }, { tipo: 'DESCARTE_IRREGULAR' })">Registrar risco</button> }
      </div>
      <div class="tbl-wrap card">
        <table class="va-tabela" aria-label="Riscos ambientais">
          <thead><tr><th scope="col">Risco</th><th scope="col">Local</th><th scope="col">Expostos</th><th scope="col">Situação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            @for (r of riscos(); track r.uuid) {
              <tr>
                <td><span class="cell-main">{{ tiposRisco[r.tipo] }}</span><span class="cell-sub">{{ r.descricao }}</span></td>
                <td>{{ r.local }}@if (r.microareaCodigo) { <span class="cell-sub">{{ r.areaNome }}, microárea {{ r.microareaCodigo }}</span> }</td>
                <td>{{ r.populacaoExposta != null ? r.populacaoExposta.toLocaleString('pt-BR') : '—' }}</td>
                <td><span [class]="'tag tag--' + (r.situacao === 'ATIVO' ? 'alert' : 'ok')">{{ r.situacao === 'ATIVO' ? 'Ativo' : 'Controlado' }}</span>@if (r.motivoControle) { <span class="cell-sub">{{ r.motivoControle }}</span> }</td>
                <td class="row-actions">@if (r.situacao === 'ATIVO' && podeGerenciar()) { <button type="button" class="link-btn" (click)="abrir({ tipo: 'controle', risco: r })" [attr.aria-label]="'Controlar: ' + r.local">Controlado</button> }</td>
              </tr>
            } @empty { <tr><td colspan="5" class="cell-sub">Nenhum risco ambiental.</td></tr> }
          </tbody>
        </table>
      </div>
    }

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" (fechar)="gaveta.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); salvar(g)" novalidate>
          <div class="dw-body">
            @if (erroGaveta()) { <div class="box box--err" role="alert"><strong>{{ erroGaveta() }}</strong></div> }
            @switch (g.tipo) {
              @case ('ponto') {
                <div class="dw-field"><label for="f-va-nome">Nome <span class="req" aria-hidden="true">*</span></label><input id="f-va-nome" type="text" maxlength="200" [value]="f()['nome'] ?? ''" (input)="set('nome', $event)" placeholder="Ex.: Torneira da Escola Municipal" /></div>
                <div class="dw-field"><label for="f-va-tipo">Abastecimento</label>
                  <select id="f-va-tipo" (change)="f.set({ ...f(), tipo: $any($event.target).value })">
                    @for (t of listaAbastecimentos; track t) { <option [value]="t" [selected]="f()['tipo'] === t">{{ abastecimentos[t] }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-va-local">Local <span class="req" aria-hidden="true">*</span></label><input id="f-va-local" type="text" maxlength="300" [value]="f()['local'] ?? ''" (input)="set('local', $event)" /></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-va-lat">Latitude<span class="opt">opcional</span></label><input id="f-va-lat" type="number" step="any" [value]="f()['latitude'] ?? ''" (input)="set('latitude', $event)" /></div>
                  <div class="dw-field"><label for="f-va-lng">Longitude<span class="opt">opcional</span></label><input id="f-va-lng" type="number" step="any" [value]="f()['longitude'] ?? ''" (input)="set('longitude', $event)" /></div>
                </div>
              }
              @case ('amostra') {
                @if (g.ponto) { <dl class="ro"><div><dt>Ponto</dt><dd>{{ g.ponto.nome }} · {{ abastecimentos[g.ponto.tipo] }}</dd></div></dl> } @else {
                  <div class="dw-field"><label for="f-va-ponto">Ponto <span class="req" aria-hidden="true">*</span></label>
                    <select id="f-va-ponto" (change)="f.set({ ...f(), pontoId: $any($event.target).value })">
                      <option value="" [selected]="!f()['pontoId']">Escolha…</option>
                      @for (p of pontos(); track p.uuid) { <option [value]="p.uuid" [selected]="f()['pontoId'] === p.uuid">{{ p.nome }}</option> }
                    </select></div>
                }
                <div class="dw-field"><label for="f-va-data">Data da coleta</label><input id="f-va-data" type="date" [value]="f()['data'] ?? ''" (input)="set('data', $event)" /></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-va-coli">Coliformes totais</label>
                    <select id="f-va-coli" (change)="f.set({ ...f(), coliformes: $any($event.target).value })">
                      <option value="" [selected]="!f()['coliformes']">Não analisado</option><option value="nao" [selected]="f()['coliformes'] === 'nao'">Ausente</option><option value="sim" [selected]="f()['coliformes'] === 'sim'">Presente</option>
                    </select></div>
                  <div class="dw-field"><label for="f-va-ecoli">E. coli</label>
                    <select id="f-va-ecoli" (change)="f.set({ ...f(), ecoli: $any($event.target).value })">
                      <option value="" [selected]="!f()['ecoli']">Não analisado</option><option value="nao" [selected]="f()['ecoli'] === 'nao'">Ausente</option><option value="sim" [selected]="f()['ecoli'] === 'sim'">Presente</option>
                    </select></div>
                </div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-va-cloro">Cloro residual livre (mg/L)</label><input id="f-va-cloro" type="number" min="0" step="0.01" [value]="f()['cloro'] ?? ''" (input)="set('cloro', $event)" /></div>
                  <div class="dw-field"><label for="f-va-turbidez">Turbidez (uT)</label><input id="f-va-turbidez" type="number" min="0" step="0.1" [value]="f()['turbidez'] ?? ''" (input)="set('turbidez', $event)" /></div>
                  <div class="dw-field"><label for="f-va-fluor">Fluoreto (mg/L)</label><input id="f-va-fluor" type="number" min="0" step="0.01" [value]="f()['fluor'] ?? ''" (input)="set('fluor', $event)" /></div>
                </div>
              }
              @case ('medida') {
                <dl class="ro"><div><dt>Amostra</dt><dd>{{ g.amostra.pontoNome }} · {{ g.amostra.naoConformidades.join('; ') }}</dd></div></dl>
                <div class="dw-field"><label for="f-va-medida">Medida <span class="req" aria-hidden="true">*</span></label><textarea id="f-va-medida" rows="3" maxlength="1000" [value]="f()['medida'] ?? ''" (input)="set('medida', $event)" placeholder="Ex.: comunicada a concessionária, recoleta em 48 h"></textarea></div>
              }
              @case ('risco') {
                <div class="dw-field"><label for="f-va-r-tipo">Tipo</label>
                  <select id="f-va-r-tipo" (change)="f.set({ ...f(), tipo: $any($event.target).value })">
                    @for (t of listaRiscos; track t) { <option [value]="t" [selected]="f()['tipo'] === t">{{ tiposRisco[t] }}</option> }
                  </select></div>
                <div class="dw-field"><label for="f-va-r-desc">Descrição <span class="req" aria-hidden="true">*</span></label><textarea id="f-va-r-desc" rows="2" maxlength="1000" [value]="f()['descricao'] ?? ''" (input)="set('descricao', $event)"></textarea></div>
                <div class="dw-row">
                  <div class="dw-field"><label for="f-va-r-local">Local <span class="req" aria-hidden="true">*</span></label><input id="f-va-r-local" type="text" maxlength="300" [value]="f()['local'] ?? ''" (input)="set('local', $event)" /></div>
                  <div class="dw-field"><label for="f-va-r-pop">População exposta<span class="opt">opcional</span></label><input id="f-va-r-pop" type="number" min="0" [value]="f()['populacao'] ?? ''" (input)="set('populacao', $event)" /></div>
                </div>
              }
              @case ('controle') {
                <dl class="ro"><div><dt>Risco</dt><dd>{{ tiposRisco[g.risco.tipo] }} · {{ g.risco.local }}</dd></div></dl>
                <div class="dw-field"><label for="f-va-motivo">Como foi controlado <span class="req" aria-hidden="true">*</span></label><input id="f-va-motivo" type="text" maxlength="1000" [value]="f()['motivo'] ?? ''" (input)="set('motivo', $event)" /></div>
              }
            }
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="enviando()">{{ enviando() ? 'Salvando…' : 'Salvar' }}</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .va-cab { display: flex; justify-content: space-between; align-items: flex-end; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-2); }
    .va-tabela { min-width: 0; width: 100%; }
    .va-titulo { margin-top: var(--s-4); }
  `,
})
export class VigilanciaAmbiental {
  private readonly service = inject(VigilanciaAmbientalService);

  readonly secao = input.required<SecaoAmbiental>();
  readonly podeRegistrar = input(false);
  readonly podeGerenciar = input(false);

  protected readonly abastecimentos = TIPOS_ABASTECIMENTO;
  protected readonly listaAbastecimentos = Object.keys(TIPOS_ABASTECIMENTO) as TipoAbastecimentoAgua[];
  protected readonly tiposRisco = TIPOS_RISCO_AMBIENTAL;
  protected readonly listaRiscos = Object.keys(TIPOS_RISCO_AMBIENTAL) as TipoRiscoAmbiental[];

  protected readonly erro = signal<string | null>(null);
  protected readonly pontos = signal<PontoColetaAguaDto[]>([]);
  protected readonly amostras = signal<AmostraAguaDto[]>([]);
  protected readonly riscos = signal<RiscoAmbientalDto[]>([]);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly erroGaveta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({});

  constructor() {
    effect(() => {
      this.secao();
      this.carregar();
    });
  }

  protected titulo(g: Gaveta): string {
    switch (g.tipo) {
      case 'ponto': return 'Novo ponto de coleta';
      case 'amostra': return 'Registrar amostra de água';
      case 'medida': return 'Medida da amostra fora do padrão';
      case 'risco': return 'Registrar risco ambiental';
      default: return 'Risco controlado';
    }
  }

  protected abrir(g: Gaveta, campos: Record<string, string> = {}): void {
    this.erroGaveta.set(null);
    this.f.set(campos);
    this.gaveta.set(g);
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected parametros(a: AmostraAguaDto): string {
    const p: string[] = [];
    const n = (v: number) => v.toLocaleString('pt-BR');
    if (a.coliformesTotais != null) p.push(`coliformes ${a.coliformesTotais ? 'presentes' : 'ausentes'}`);
    if (a.ecoli != null) p.push(`E. coli ${a.ecoli ? 'presente' : 'ausente'}`);
    if (a.cloroResidual != null) p.push(`cloro ${n(a.cloroResidual)} mg/L`);
    if (a.turbidez != null) p.push(`turbidez ${n(a.turbidez)} uT`);
    if (a.fluor != null) p.push(`flúor ${n(a.fluor)} mg/L`);
    return p.join(' · ');
  }

  protected salvar(g: Gaveta): void {
    if (this.enviando()) return;
    const c = this.f();
    const t = (k: string) => (c[k] ?? '').trim() || null;
    const n = (k: string) => (t(k) == null ? null : Number(c[k]));
    const b = (k: string) => (t(k) == null ? null : c[k] === 'sim');
    let req: Observable<unknown>;
    let falta: string | null = null;
    switch (g.tipo) {
      case 'ponto':
        if (!t('nome') || !t('local')) falta = 'Informe o nome e o local.';
        req = this.service.criarPonto({ nome: t('nome') ?? '', tipo: c['tipo'] as TipoAbastecimentoAgua, local: t('local') ?? '', latitude: n('latitude'), longitude: n('longitude'), microareaId: null });
        break;
      case 'amostra': {
        const pontoId = g.ponto?.uuid ?? t('pontoId');
        if (!pontoId) falta = 'Escolha o ponto.';
        req = this.service.registrarAmostra({ pontoId: pontoId ?? '', dataColeta: t('data'), coliformesTotais: b('coliformes'), ecoli: b('ecoli'), cloroResidual: n('cloro'), turbidez: n('turbidez'), fluor: n('fluor') });
        break;
      }
      case 'medida':
        if (!t('medida')) falta = 'Informe a medida.';
        req = this.service.registrarMedida(g.amostra.uuid, t('medida') ?? '');
        break;
      case 'risco':
        if (!t('descricao') || !t('local')) falta = 'Informe a descrição e o local.';
        req = this.service.registrarRisco({ tipo: c['tipo'] as TipoRiscoAmbiental, descricao: t('descricao') ?? '', local: t('local') ?? '', populacaoExposta: n('populacao'), latitude: null, longitude: null, microareaId: null });
        break;
      default:
        if (!t('motivo')) falta = 'Informe como o risco foi controlado.';
        req = this.service.controlar(g.risco.uuid, t('motivo') ?? '');
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

  protected hoje(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  private carregar(): void {
    this.erro.set(null);
    const falhou = (x: HttpErrorResponse) => this.erro.set(mensagem(x));
    if (this.secao() === 'agua') {
      this.service.pontos().subscribe({ next: (l) => this.pontos.set(l), error: falhou });
      this.service.amostras().pipe(catchError(() => of([]))).subscribe((l) => this.amostras.set(l));
    } else {
      this.service.riscos().subscribe({ next: (l) => this.riscos.set(l), error: falhou });
    }
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
