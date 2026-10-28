import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import {
  CadernetaVacinacaoDto,
  ESTRATEGIAS,
  OrigemRegistroVacinacao,
  PUBLICOS,
  RegistroVacinacaoDto,
  SITUACOES_DOSE,
  idadeTexto,
} from '../../../core/models/imunizacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { RegistrarDose } from './registrar-dose';

/**
 * Caderneta de vacinação do paciente (ADR-0147): a situação pelo calendário (atrasadas, próximas, futuras, aplicadas),
 * as doses registradas — com retificação e anulação — e as recomendações conforme a situação vacinal. Dado de saúde:
 * a API aplica as regras do prontuário.
 */
@Component({
  selector: 'app-caderneta-vacinacao',
  imports: [RegistrarDose, Drawer],
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (caderneta() === undefined) {
      <p class="cell-sub">Carregando a caderneta…</p>
    } @else if (caderneta(); as c) {
      <div class="vac-topo">
        <div>
          @if (!c.situacaoDisponivel) {
            <span class="tag tag--muted">Sem data de nascimento: situação vacinal indisponível</span>
          } @else if (c.emDia) {
            <span class="tag tag--ok" aria-label="Situação vacinal">Em dia</span>
          } @else {
            <span class="tag tag--alert" aria-label="Situação vacinal">{{ c.atrasadas }} dose(s) atrasada(s)</span>
          }
          @if (c.proximas) { <span class="tag tag--warn">{{ c.proximas }} nos próximos 30 dias</span> }
          @if (c.idadeMeses != null) { <span class="cell-sub" style="display: inline; margin-left: 6px;">{{ idade(c.idadeMeses) }}</span> }
        </div>
        @if (podeRegistrar()) {
          <div class="row-actions">
            <button type="button" class="btn btn--ghost btn--sm" (click)="abrirRegistro('TRANSCRITA')">Transcrever da caderneta</button>
            <button type="button" class="btn btn--sm" (click)="abrirRegistro('APLICADA')">Registrar vacinação</button>
          </div>
        }
      </div>

      @if (c.situacaoDisponivel) {
        <div class="vac-cab">
          <h3>Calendário</h3>
          <label class="switch" for="f-vac-futuras"><span><b style="font-weight: 500;">Mostrar futuras e fora da idade</b></span>
            <input type="checkbox" role="switch" id="f-vac-futuras" [checked]="todas()" (change)="todas.set($any($event.target).checked)" /></label>
        </div>
        <div class="tbl-wrap vac-tabela">
          <table aria-label="Calendário vacinal do paciente">
            <thead><tr><th scope="col">Vacina</th><th scope="col">Dose</th><th scope="col">Prevista</th><th scope="col">Situação</th></tr></thead>
            <tbody>
              @for (d of calendarioVisivel(); track d.esquemaId) {
                <tr>
                  <td><span class="cell-main">{{ d.sigla }}</span><span class="cell-sub">{{ d.imunobiologicoNome }}</span></td>
                  <td>{{ d.doseRotulo }}</td>
                  <td class="mono">{{ data(d.dataPrevista) }}</td>
                  <td><span [class]="'tag tag--' + situacoes[d.situacao].classe">{{ situacoes[d.situacao].rotulo }}</span>@if (d.aplicadaEm) { <span class="cell-sub">em {{ data(d.aplicadaEm) }}</span> }</td>
                </tr>
              } @empty { <tr><td colspan="4" class="cell-sub">Nada pendente no calendário.</td></tr> }
            </tbody>
          </table>
        </div>
      }

      <h3 style="margin-top: var(--s-4);">Doses registradas</h3>
      <ul class="eq-prof" aria-label="Doses registradas">
        @for (r of c.doses; track r.uuid) {
          <li class="mo-row">
            <span>
              <span class="cell-main">{{ r.sigla }} · {{ r.doseRotulo }} <span class="cell-sub" style="display: inline;">em {{ data(r.aplicadaEm) }}</span></span>
              <span class="cell-sub">@if (r.origem === 'TRANSCRITA') { Transcrita da caderneta@if (r.localOrigem) { · {{ r.localOrigem }} } } @else { {{ r.unidadeNome }} }@if (r.lote) { · lote {{ r.lote }} } · {{ estrategias[r.estrategia] }}@if (r.justificativa) { · fora do calendário: {{ r.justificativa }} }</span>
            </span>
            @if (podeRegistrar()) {
              <span class="row-actions">
                <button type="button" class="link-btn" (click)="retificar(r)" [attr.aria-label]="'Retificar ' + r.sigla + ' ' + r.doseRotulo">Retificar</button>
                <button type="button" class="link-btn" (click)="anulando.set(r)" [attr.aria-label]="'Anular ' + r.sigla + ' ' + r.doseRotulo">Anular</button>
              </span>
            }
          </li>
        } @empty { <li class="cell-sub">Nenhuma dose registrada.</li> }
      </ul>

      @if (c.recomendacoes.length) {
        <h3 style="margin-top: var(--s-4);">Conforme a situação vacinal</h3>
        <ul class="eq-prof" aria-label="Recomendações">
          @for (e of c.recomendacoes; track e.uuid) {
            <li class="mo-row"><span><span class="cell-main">{{ e.sigla }} · {{ e.doseRotulo }}</span><span class="cell-sub">{{ e.observacao }}</span></span><span class="tag tag--info">{{ publicos[e.publico] }}</span></li>
          }
        </ul>
      }
    }

    @if (registrando(); as origem) {
      <app-registrar-dose [paciente]="{ uuid: pacienteId(), nome: pacienteNome() }" [unidades]="unidades()" [origemInicial]="origem"
        [retificando]="retificandoRegistro()" (salvo)="aoSalvar()" (fechar)="fecharRegistro()" />
    }

    @if (anulando(); as a) {
      <app-drawer titulo="Anular dose" (fechar)="anulando.set(null)">
        <form class="dw-form" (submit)="$event.preventDefault(); anular(a)" novalidate>
          <div class="dw-body">
            <dl class="ro"><div><dt>Dose</dt><dd>{{ a.sigla }} · {{ a.doseRotulo }} em {{ data(a.aplicadaEm) }}</dd></div></dl>
            <p class="cell-sub">A anulação é para o registro feito por engano (paciente errado, dose que não houve). Para corrigir um dado, retifique.</p>
            <div class="dw-field"><label for="f-vac-anulacao">Motivo <span class="req" aria-hidden="true">*</span></label><textarea id="f-vac-anulacao" rows="2" maxlength="500" [value]="motivoAnulacao()" (input)="motivoAnulacao.set($any($event.target).value)"></textarea></div>
          </div>
          <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="anulando.set(null)">Cancelar</button><button type="submit" class="btn" [disabled]="!motivoAnulacao().trim()">Anular</button></div>
        </form>
      </app-drawer>
    }
  `,
  styles: `
    .vac-topo { display: flex; justify-content: space-between; align-items: center; gap: var(--s-3); flex-wrap: wrap; margin-bottom: var(--s-4); }
    .vac-cab { display: flex; justify-content: space-between; align-items: center; gap: var(--s-3); flex-wrap: wrap; }
    .vac-tabela table { min-width: 0; width: 100%; }
  `,
})
export class CadernetaVacinacao {
  private readonly service = inject(ImunizacaoService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  readonly pacienteId = input.required<string>();
  readonly pacienteNome = input('');
  readonly podeRegistrar = input(false);

  protected readonly situacoes = SITUACOES_DOSE;
  protected readonly estrategias = ESTRATEGIAS;
  protected readonly publicos = PUBLICOS;
  protected readonly caderneta = signal<CadernetaVacinacaoDto | null | undefined>(undefined);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly todas = signal(false);
  protected readonly registrando = signal<OrigemRegistroVacinacao | null>(null);
  protected readonly retificandoRegistro = signal<RegistroVacinacaoDto | null>(null);
  protected readonly anulando = signal<RegistroVacinacaoDto | null>(null);
  protected readonly motivoAnulacao = signal('');
  protected readonly erro = signal<string | null>(null);

  protected readonly calendarioVisivel = computed(() => {
    const c = this.caderneta();
    if (!c) {
      return [];
    }
    return this.todas() ? c.calendario : c.calendario.filter((d) => d.situacao === 'ATRASADA' || d.situacao === 'PROXIMA' || d.situacao === 'APLICADA');
  });

  constructor() {
    effect(() => {
      const id = this.pacienteId();
      untracked(() => this.carregar(id));
    });
  }

  protected abrirRegistro(origem: OrigemRegistroVacinacao): void {
    this.retificandoRegistro.set(null);
    this.carregarUnidades();
    this.registrando.set(origem);
  }

  protected retificar(r: RegistroVacinacaoDto): void {
    this.retificandoRegistro.set(r);
    this.carregarUnidades();
    this.registrando.set(r.origem);
  }

  protected fecharRegistro(): void {
    this.registrando.set(null);
    this.retificandoRegistro.set(null);
  }

  protected aoSalvar(): void {
    this.fecharRegistro();
    this.carregar(this.pacienteId());
  }

  protected anular(r: RegistroVacinacaoDto): void {
    this.service.anular(r.uuid, this.motivoAnulacao().trim()).subscribe({
      next: () => {
        this.anulando.set(null);
        this.motivoAnulacao.set('');
        this.carregar(this.pacienteId());
      },
      error: (e: HttpErrorResponse) => this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível anular a dose.'),
    });
  }

  protected idade(meses: number): string {
    return idadeTexto(meses);
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) {
      return '—';
    }
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  private carregarUnidades(): void {
    if (!this.unidades().length) {
      this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidades.set(l));
    }
  }

  private carregar(id: string): void {
    this.erro.set(null);
    this.service.caderneta(id).subscribe({
      next: (c) => this.caderneta.set(c),
      error: (e: HttpErrorResponse) => {
        this.caderneta.set(null);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar a caderneta de vacinação.');
      },
    });
  }
}
