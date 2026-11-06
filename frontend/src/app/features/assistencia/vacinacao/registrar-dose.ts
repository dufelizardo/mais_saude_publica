import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output, computed, effect, inject, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import {
  DOSES,
  DoseVacinal,
  ESTRATEGIAS,
  EstrategiaVacinacao,
  EsquemaVacinalDto,
  EstoqueSalaVacinaDto,
  ImunobiologicoDto,
  LOCAIS_APLICACAO,
  LocalAplicacaoVacina,
  OrigemRegistroVacinacao,
  RegistroVacinacaoDto,
  RegistroVacinacaoRequestDto,
  VIAS,
  ViaVacina,
  dataBr,
} from '../../../core/models/imunizacao';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { AuthService } from '../../../core/services/auth';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { PacienteService } from '../../../core/services/paciente';
import { Drawer } from '../../../shared/drawer/drawer';

/**
 * Registro de dose (ADR-0147), na gaveta: aplicada na unidade (com quem aplicou e o lote) ou transcrita da caderneta
 * (com a data e onde foi aplicada). Fora da idade ou do intervalo do calendário, a API pede a justificativa e o campo
 * aparece. Também retifica uma dose vigente, com o motivo (ADR-0062). Na unidade com sala de vacina (ADR-0148), a dose
 * aplicada sai de um lote do estoque, escolhido na lista dos aplicáveis daquela vacina, em vez do lote em texto.
 */
@Component({
  selector: 'app-registrar-dose',
  imports: [Drawer],
  template: `
    <app-drawer [titulo]="retificando ? 'Retificar dose' : origem() === 'APLICADA' ? 'Registrar vacinação' : 'Transcrever da caderneta'" (fechar)="fechar.emit()">
      <form class="dw-form" (submit)="$event.preventDefault(); salvar()" novalidate>
        <div class="dw-body">
          @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
          @if (paciente) {
            <dl class="ro"><div><dt>Paciente</dt><dd>{{ paciente.nome }}</dd></div></dl>
          } @else {
            <div class="dw-field">
              <label for="f-vac-paciente">Paciente <span class="req" aria-hidden="true">*</span></label>
              <input id="f-vac-paciente" type="search" autocomplete="off" placeholder="Nome, CPF ou cartão SUS" [value]="busca()"
                (input)="busca.set($any($event.target).value); pacienteEscolhido.set(null)" />
              @if (!pacienteEscolhido() && encontrados().length) {
                <ul class="eq-prof prg-busca" role="listbox" aria-label="Pacientes encontrados">
                  @for (p of encontrados(); track p.uuid) {
                    <li><button type="button" class="link-btn" role="option" (click)="escolher(p)">{{ p.nome }}</button><span class="cell-sub">CPF {{ p.cpf }}</span></li>
                  }
                </ul>
              }
              @if (pacienteEscolhido(); as pe) { <p class="dw-hint">Escolhido: <strong>{{ pe.nome }}</strong></p> }
            </div>
          }
          @if (!retificando) {
            <div class="radios" role="radiogroup" aria-label="Origem da dose">
              <label><input type="radio" name="origem" [checked]="origem() === 'APLICADA'" (change)="origem.set('APLICADA')" />Aplicada aqui</label>
              <label><input type="radio" name="origem" [checked]="origem() === 'TRANSCRITA'" (change)="origem.set('TRANSCRITA')" />Transcrita da caderneta</label>
            </div>
          }
          <div class="dw-row">
            <div class="dw-field">
              <label for="f-vac-unidade">Unidade <span class="req" aria-hidden="true">*</span></label>
              <select id="f-vac-unidade" [value]="unidadeId()" (change)="unidadeId.set($any($event.target).value)">
                <option value="">Escolha…</option>
                @for (u of unidades; track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
              </select>
            </div>
            <div class="dw-field">
              <label for="f-vac-quando">{{ origem() === 'APLICADA' ? 'Data e hora' : 'Data da dose' }}@if (origem() === 'TRANSCRITA') { <span class="req" aria-hidden="true"> *</span> } @else { <span class="opt">agora, se vazio</span> }</label>
              <input id="f-vac-quando" type="datetime-local" [value]="quando()" (change)="quando.set($any($event.target).value)" />
            </div>
          </div>
          <div class="dw-row">
            <div class="dw-field">
              <label for="f-vac-imuno">Vacina <span class="req" aria-hidden="true">*</span></label>
              <select id="f-vac-imuno" [value]="imunoId()" (change)="escolherImuno($any($event.target).value)">
                <option value="">Escolha…</option>
                @for (i of imunosAtivos(); track i.uuid) { <option [value]="i.uuid">{{ i.sigla }} · {{ i.nome }}</option> }
              </select>
            </div>
            <div class="dw-field">
              <label for="f-vac-dose">Dose <span class="req" aria-hidden="true">*</span></label>
              <select id="f-vac-dose" [value]="dose()" (change)="dose.set($any($event.target).value)">
                <option value="">Escolha…</option>
                @for (d of dosesDaVacina(); track d) { <option [value]="d">{{ doses[d] }}</option> }
              </select>
            </div>
          </div>
          @if (origem() === 'APLICADA') {
            <div class="dw-row">
              <div class="dw-field"><label for="f-vac-matricula">Quem aplicou (matrícula) <span class="req" aria-hidden="true">*</span></label><input id="f-vac-matricula" type="text" [value]="matricula()" (input)="matricula.set($any($event.target).value)" /></div>
              @if (estoque()?.temSalaDeVacina) {
                <div class="dw-field">
                  <label for="f-vac-lote-estoque">Lote do estoque <span class="req" aria-hidden="true">*</span></label>
                  <select id="f-vac-lote-estoque" [value]="loteEstoqueId()" (change)="loteEstoqueId.set($any($event.target).value)">
                    <option value="">Escolha…</option>
                    @for (l of lotesDaVacina(); track l.uuid) { <option [value]="l.uuid">{{ l.numeroLote }} · val. {{ data(l.validade) }} · {{ l.doses }} doses</option> }
                  </select>
                  @if (imunoId() && !lotesDaVacina().length) { <p class="dw-hint">Sem lote aplicável desta vacina no estoque da unidade.</p> }
                </div>
              } @else {
                <div class="dw-field"><label for="f-vac-lote">Lote <span class="req" aria-hidden="true">*</span></label><input id="f-vac-lote" type="text" maxlength="40" [value]="lote()" (input)="lote.set($any($event.target).value)" /></div>
              }
            </div>
            <div class="dw-row">
              <div class="dw-field">
                <label for="f-vac-via">Via</label>
                <select id="f-vac-via" [value]="via()" (change)="via.set($any($event.target).value)">
                  @for (v of viasLista; track v) { <option [value]="v">{{ vias[v] }}</option> }
                </select>
              </div>
              <div class="dw-field">
                <label for="f-vac-local">Local de aplicação<span class="opt">opcional</span></label>
                <select id="f-vac-local" [value]="local()" (change)="local.set($any($event.target).value)">
                  <option value="">—</option>
                  @for (l of locaisLista; track l) { <option [value]="l">{{ locais[l] }}</option> }
                </select>
              </div>
            </div>
            <div class="dw-row">
              <div class="dw-field">
                <label for="f-vac-estrategia">Estratégia</label>
                <select id="f-vac-estrategia" [value]="estrategia()" (change)="estrategia.set($any($event.target).value)">
                  @for (e of estrategiasLista; track e) { <option [value]="e">{{ estrategias[e] }}</option> }
                </select>
              </div>
              <div class="dw-field"><label for="f-vac-fabricante">Fabricante<span class="opt">opcional</span></label><input id="f-vac-fabricante" type="text" maxlength="100" [value]="fabricante()" (input)="fabricante.set($any($event.target).value)" /></div>
            </div>
            <label class="switch" for="f-vac-gestante">
              <span><b style="font-weight: 500;">Gestante</b></span>
              <input type="checkbox" role="switch" id="f-vac-gestante" [checked]="gestante()" (change)="gestante.set($any($event.target).checked)" />
            </label>
          } @else {
            <div class="dw-row">
              <div class="dw-field"><label for="f-vac-origem">Onde foi aplicada<span class="opt">opcional</span></label><input id="f-vac-origem" type="text" maxlength="200" placeholder="Ex.: Maternidade Municipal" [value]="localOrigem()" (input)="localOrigem.set($any($event.target).value)" /></div>
              <div class="dw-field"><label for="f-vac-lote-t">Lote<span class="opt">opcional</span></label><input id="f-vac-lote-t" type="text" maxlength="40" [value]="lote()" (input)="lote.set($any($event.target).value)" /></div>
            </div>
          }
          @if (pedeJustificativa() || justificativa()) {
            <div class="dw-field"><label for="f-vac-justificativa">Justificativa (dose fora do calendário) <span class="req" aria-hidden="true">*</span></label><textarea id="f-vac-justificativa" rows="2" maxlength="500" [value]="justificativa()" (input)="justificativa.set($any($event.target).value)"></textarea></div>
          }
          @if (retificando) {
            <div class="dw-field"><label for="f-vac-motivo">Motivo da retificação <span class="req" aria-hidden="true">*</span></label><textarea id="f-vac-motivo" rows="2" maxlength="500" [value]="motivo()" (input)="motivo.set($any($event.target).value)"></textarea></div>
          }
          <div class="dw-field"><label for="f-vac-obs">Observação<span class="opt">opcional</span></label><input id="f-vac-obs" type="text" maxlength="500" [value]="observacao()" (input)="observacao.set($any($event.target).value)" /></div>
        </div>
        <div class="dw-foot">
          <button type="button" class="btn btn--ghost" (click)="fechar.emit()">Cancelar</button>
          <button type="submit" class="btn" [disabled]="salvando()">{{ retificando ? 'Retificar' : 'Registrar' }}</button>
        </div>
      </form>
    </app-drawer>
  `,
})
export class RegistrarDose implements OnInit {
  private readonly service = inject(ImunizacaoService);
  private readonly pacienteService = inject(PacienteService);
  private readonly authService = inject(AuthService);

  @Input() paciente: { uuid: string; nome: string } | null = null;
  @Input() unidades: UnidadeSaudeResponseDto[] = [];
  @Input() unidadeInicial = '';
  @Input() origemInicial: OrigemRegistroVacinacao = 'APLICADA';
  @Input() retificando: RegistroVacinacaoDto | null = null;
  @Output() salvo = new EventEmitter<RegistroVacinacaoDto>();
  @Output() fechar = new EventEmitter<void>();

  protected readonly doses = DOSES;
  protected readonly vias = VIAS;
  protected readonly viasLista = Object.keys(VIAS) as ViaVacina[];
  protected readonly locais = LOCAIS_APLICACAO;
  protected readonly locaisLista = Object.keys(LOCAIS_APLICACAO) as LocalAplicacaoVacina[];
  protected readonly estrategias = ESTRATEGIAS;
  protected readonly estrategiasLista = Object.keys(ESTRATEGIAS) as EstrategiaVacinacao[];

  protected readonly imunos = signal<ImunobiologicoDto[]>([]);
  protected readonly calendario = signal<EsquemaVacinalDto[]>([]);
  protected readonly todosPacientes = signal<PacienteResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly pacienteEscolhido = signal<PacienteResponseDto | null>(null);
  protected readonly origem = signal<OrigemRegistroVacinacao>('APLICADA');
  protected readonly unidadeId = signal('');
  protected readonly quando = signal('');
  protected readonly imunoId = signal('');
  protected readonly dose = signal<DoseVacinal | ''>('');
  protected readonly matricula = signal('');
  protected readonly lote = signal('');
  protected readonly loteEstoqueId = signal('');
  protected readonly estoque = signal<EstoqueSalaVacinaDto | null>(null);
  protected readonly fabricante = signal('');
  protected readonly via = signal<ViaVacina>('INTRAMUSCULAR');
  protected readonly local = signal<LocalAplicacaoVacina | ''>('');
  protected readonly estrategia = signal<EstrategiaVacinacao>('ROTINA');
  protected readonly gestante = signal(false);
  protected readonly localOrigem = signal('');
  protected readonly justificativa = signal('');
  protected readonly motivo = signal('');
  protected readonly observacao = signal('');
  protected readonly pedeJustificativa = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly salvando = signal(false);

  /** Os lotes aplicáveis da vacina escolhida (o da dose em retificação também, para manter). */
  protected readonly lotesDaVacina = computed(() =>
    (this.estoque()?.lotes ?? []).filter((l) => l.imunobiologicoId === this.imunoId() && (l.aplicavel || l.uuid === this.retificando?.loteEstoqueId)),
  );
  protected readonly imunosAtivos = computed(() => this.imunos().filter((i) => i.ativo || i.uuid === this.imunoId()));
  /** As doses do calendário para a vacina escolhida; sem calendário, todas. */
  protected readonly dosesDaVacina = computed<DoseVacinal[]>(() => {
    const doCalendario = [...new Set(this.calendario().filter((e) => e.imunobiologicoId === this.imunoId()).map((e) => e.dose))];
    return doCalendario.length ? [...doCalendario, ...(doCalendario.includes('ADICIONAL') ? [] : (['ADICIONAL'] as DoseVacinal[]))] : (Object.keys(DOSES) as DoseVacinal[]);
  });
  protected readonly encontrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    if (q.length < 2) {
      return [];
    }
    const digitos = q.replace(/\D/g, '');
    return this.todosPacientes()
      .filter((p) => p.nome.toLowerCase().includes(q) || (digitos.length >= 3 && ((p.cpf ?? '').includes(digitos) || (p.cartaoSus ?? '').includes(digitos))))
      .slice(0, 8);
  });

  constructor() {
    effect(() => {
      const u = this.unidadeId();
      const aplicada = this.origem() === 'APLICADA';
      untracked(() => {
        this.estoque.set(null);
        if (u && aplicada) {
          this.service.estoque(u).pipe(catchError(() => of(null))).subscribe((e) => this.estoque.set(e));
        }
      });
    });
  }

  protected data(iso: string): string {
    return dataBr(iso);
  }

  ngOnInit(): void {
    this.origem.set(this.origemInicial);
    this.unidadeId.set(this.unidadeInicial || (this.unidades.length === 1 ? this.unidades[0].uuid : ''));
    this.service.imunobiologicos().pipe(catchError(() => of([]))).subscribe((l) => this.imunos.set(l));
    this.service.calendario().pipe(catchError(() => of([]))).subscribe((l) => this.calendario.set(l));
    if (!this.paciente) {
      this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.todosPacientes.set(l));
    }
    this.authService.usuarioAtual().pipe(catchError(() => of(null))).subscribe((u) => {
      if (u?.profissionalMatricula && !this.matricula()) {
        this.matricula.set(u.profissionalMatricula);
      }
    });
    const r = this.retificando;
    if (r) {
      this.origem.set(r.origem);
      this.unidadeId.set(r.unidadeId);
      this.quando.set(r.aplicadaEm.slice(0, 16));
      this.imunoId.set(r.imunobiologicoId);
      this.dose.set(r.dose);
      this.matricula.set(r.profissionalMatricula ?? '');
      this.lote.set(r.lote ?? '');
      this.loteEstoqueId.set(r.loteEstoqueId ?? '');
      this.fabricante.set(r.fabricante ?? '');
      this.via.set(r.via ?? 'INTRAMUSCULAR');
      this.local.set(r.localAplicacao ?? '');
      this.estrategia.set(r.estrategia);
      this.gestante.set(r.gestante);
      this.localOrigem.set(r.localOrigem ?? '');
      this.justificativa.set(r.justificativa ?? '');
      this.observacao.set(r.observacao ?? '');
    }
  }

  protected escolher(p: PacienteResponseDto): void {
    this.pacienteEscolhido.set(p);
    this.busca.set(p.nome);
  }

  protected escolherImuno(uuid: string): void {
    this.imunoId.set(uuid);
    this.dose.set('');
    this.loteEstoqueId.set('');
    const i = this.imunos().find((x) => x.uuid === uuid);
    if (i) {
      this.via.set(i.via);
    }
  }

  protected salvar(): void {
    const pacienteId = this.paciente?.uuid ?? this.pacienteEscolhido()?.uuid;
    if (!pacienteId || !this.unidadeId() || !this.imunoId() || !this.dose()) {
      this.erro.set('Preencha o paciente, a unidade, a vacina e a dose.');
      return;
    }
    const sala = this.origem() === 'APLICADA' && !!this.estoque()?.temSalaDeVacina;
    if (sala && !this.loteEstoqueId()) {
      this.erro.set('Escolha o lote do estoque de onde a dose saiu.');
      return;
    }
    const dto: RegistroVacinacaoRequestDto = {
      pacienteId,
      imunobiologicoId: this.imunoId(),
      dose: this.dose() as DoseVacinal,
      unidadeId: this.unidadeId(),
      origem: this.origem(),
      aplicadaEm: this.quando() ? `${this.quando()}:00`.slice(0, 19) : null,
      profissionalMatricula: this.origem() === 'APLICADA' ? this.matricula().trim() : null,
      lote: sala ? null : this.lote().trim() || null,
      loteEstoqueId: sala ? this.loteEstoqueId() || null : null,
      fabricante: this.origem() === 'APLICADA' ? this.fabricante().trim() || null : null,
      via: this.origem() === 'APLICADA' ? this.via() : null,
      localAplicacao: this.origem() === 'APLICADA' ? this.local() || null : null,
      estrategia: this.estrategia(),
      gestante: this.gestante(),
      justificativa: this.justificativa().trim() || null,
      localOrigem: this.origem() === 'TRANSCRITA' ? this.localOrigem().trim() || null : null,
      observacao: this.observacao().trim() || null,
      motivoRetificacao: this.retificando ? this.motivo().trim() : null,
    };
    this.salvando.set(true);
    this.erro.set(null);
    const chamada = this.retificando ? this.service.retificar(this.retificando.uuid, dto) : this.service.registrar(dto);
    chamada.subscribe({
      next: (r) => {
        this.salvando.set(false);
        this.salvo.emit(r);
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        const msg = (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível registrar a dose.';
        if (e.status === 422 && msg.includes('fora do calendário')) {
          this.pedeJustificativa.set(true);
        }
        this.erro.set(msg);
      },
    });
  }
}
