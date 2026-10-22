import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { catchError, of } from 'rxjs';
import { AlcanceProcedimento, ProcedimentoCatalogoDto, ProcedimentoCatalogoRequestDto } from '../../../core/models/procedimento';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { CondicaoOdontologicaDto, EspecialidadeDto } from '../../../core/models/saude-bucal';
import { ProcedimentoService } from '../../../core/services/procedimento';
import { SaudeBucalService } from '../../../core/services/saude-bucal';

const ALCANCES: Record<AlcanceProcedimento, string> = { NENHUM: 'Sem dente', DENTE: 'Dente inteiro', FACE: 'Dente e faces' };

/**
 * Catálogo geral de procedimentos (ADR-0136): de todas as especialidades, com o SIGTAP opcional. Quem mantém a estrutura
 * da rede cadastra e desativa; o código não muda. No odontológico, o alcance (dente ou face), a condição que o
 * procedimento realizado grava no odontograma e se ele é só do cirurgião-dentista.
 */
@Component({
  selector: 'app-catalogo-procedimentos',
  imports: [ReactiveFormsModule],
  template: `
    <section class="card" style="padding: var(--s-5); margin-top: var(--s-4);" aria-labelledby="procedimentos-titulo">
      <h2 id="procedimentos-titulo" style="font-size: 1rem;">Catálogo de procedimentos</h2>
      <p class="cell-sub">Os procedimentos de todas as especialidades. O odontológico com dente ou face, realizado, grava o odontograma.</p>
      @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
      <ul class="eq-prof" aria-label="Catálogo de procedimentos">
        @for (p of itens(); track p.uuid) {
          <li class="mo-row">
            <span>
              <span class="cell-main">{{ p.nome }}@if (p.especialidadeNome) { <span class="tag tag--info">{{ p.especialidadeNome }}</span> }@if (p.exigeDentista) { <span class="tag tag--warn">Só dentista</span> }@if (!p.ativo) { <span class="tag tag--muted">Inativo</span> }</span>
              <span class="cell-sub"><span class="mono">{{ p.codigo }}</span>@if (p.sigtap) { · SIGTAP <span class="mono">{{ p.sigtap }}</span> }@if (p.odontologico) { · {{ alcances[p.alcance] }} }@if (p.condicaoResultanteNome) { · grava {{ p.condicaoResultanteNome }} }</span>
            </span>
            @if (podeGerenciar()) {
              <button type="button" class="link-btn" (click)="alternar(p)" [attr.aria-label]="(p.ativo ? 'Desativar: ' : 'Reativar: ') + p.nome">{{ p.ativo ? 'Desativar' : 'Reativar' }}</button>
            }
          </li>
        }
      </ul>
      @if (podeGerenciar()) {
        <form [formGroup]="form" (ngSubmit)="criar()" novalidate aria-label="Novo procedimento no catálogo" style="margin-top: var(--s-3);">
          <div class="dw-row" style="align-items: end;">
            <div class="dw-field"><label for="f-proc-codigo">Código</label><input id="f-proc-codigo" type="text" class="mono" formControlName="codigo" placeholder="Ex.: CURATIVO" /></div>
            <div class="dw-field"><label for="f-proc-nome">Nome</label><input id="f-proc-nome" type="text" maxlength="150" formControlName="nome" /></div>
            <div class="dw-field"><label for="f-proc-sigtap">SIGTAP<span class="opt">opcional</span></label><input id="f-proc-sigtap" type="text" inputmode="numeric" maxlength="10" class="mono" formControlName="sigtap" /></div>
          </div>
          <div class="dw-row" style="align-items: end;">
            <div class="dw-field">
              <label for="f-proc-especialidade">Especialidade</label>
              <select id="f-proc-especialidade" formControlName="especialidadeId" (change)="aoTrocarEspecialidade()">
                <option value="">Uso geral</option>
                @for (e of especialidadesAtivas(); track e.uuid) { <option [value]="e.uuid">{{ e.nome }}</option> }
              </select>
            </div>
            @if (odontologica()) {
              <div class="dw-field">
                <label for="f-proc-alcance">Registra</label>
                <select id="f-proc-alcance" formControlName="alcance">
                  @for (a of alcancesLista; track a) { <option [value]="a">{{ alcances[a] }}</option> }
                </select>
              </div>
              @if (form.controls.alcance.value !== 'NENHUM') {
                <div class="dw-field">
                  <label for="f-proc-condicao">Grava no odontograma</label>
                  <select id="f-proc-condicao" formControlName="condicaoResultanteId">
                    <option value="">Nada</option>
                    @for (c of condicoesDoAlcance(); track c.uuid) { <option [value]="c.uuid">{{ c.nome }}</option> }
                  </select>
                </div>
              }
              <div class="dw-field"><label class="switch" for="f-proc-dentista"><span><b style="font-weight: 500;">Só dentista</b></span><input type="checkbox" role="switch" id="f-proc-dentista" formControlName="exigeDentista" /></label></div>
            }
            <div class="dw-field"><button type="submit" class="btn btn--sm">Incluir procedimento</button></div>
          </div>
        </form>
      }
    </section>
  `,
})
export class CatalogoProcedimentos {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProcedimentoService);
  private readonly saudeBucal = inject(SaudeBucalService);

  readonly podeGerenciar = input(false);

  protected readonly alcances = ALCANCES;
  protected readonly alcancesLista = Object.keys(ALCANCES) as AlcanceProcedimento[];
  protected readonly itens = signal<ProcedimentoCatalogoDto[]>([]);
  protected readonly especialidades = signal<EspecialidadeDto[]>([]);
  protected readonly condicoes = signal<CondicaoOdontologicaDto[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly especialidadeEscolhida = signal('');
  protected readonly form = this.fb.nonNullable.group({
    codigo: '',
    nome: '',
    sigtap: '',
    especialidadeId: '',
    alcance: 'NENHUM' as AlcanceProcedimento,
    condicaoResultanteId: '',
    exigeDentista: false,
  });

  protected readonly especialidadesAtivas = computed(() => this.especialidades().filter((e) => e.ativa));
  protected readonly odontologica = computed(() => this.especialidades().find((e) => e.uuid === this.especialidadeEscolhida())?.odontologica ?? false);

  constructor() {
    this.carregar();
    this.saudeBucal.especialidades().pipe(catchError(() => of([]))).subscribe((e) => this.especialidades.set(e));
    this.saudeBucal.condicoes().pipe(catchError(() => of([]))).subscribe((c) => this.condicoes.set(c));
  }

  /** A condição precisa caber no que o procedimento registra: face com face, dente inteiro com dente. */
  protected condicoesDoAlcance(): CondicaoOdontologicaDto[] {
    const alcance = this.form.controls.alcance.value;
    return this.condicoes().filter((c) => c.ativa && (c.alcance === 'AMBOS' || (alcance === 'FACE' ? c.alcance === 'FACE' : c.alcance === 'DENTE')));
  }

  protected aoTrocarEspecialidade(): void {
    this.especialidadeEscolhida.set(this.form.controls.especialidadeId.value);
    if (!this.odontologica()) this.form.patchValue({ alcance: 'NENHUM', condicaoResultanteId: '', exigeDentista: false });
  }

  private carregar(): void {
    this.service.catalogo().pipe(catchError(() => of([]))).subscribe((p) => this.itens.set(p));
  }

  protected criar(): void {
    const v = this.form.getRawValue();
    const codigo = v.codigo.trim().toUpperCase();
    if (!/^[A-Z][A-Z0-9_]{1,39}$/.test(codigo) || !v.nome.trim()) {
      this.erro.set('Informe o código (letras maiúsculas, números e sublinhado) e o nome.');
      return;
    }
    if (v.sigtap.trim() && !/^\d{10}$/.test(v.sigtap.trim())) {
      this.erro.set('O código SIGTAP tem 10 dígitos.');
      return;
    }
    const odonto = this.odontologica();
    this.service.criarItemCatalogo({
      codigo,
      nome: v.nome.trim(),
      sigtap: v.sigtap.trim() || null,
      especialidadeId: v.especialidadeId || null,
      alcance: odonto ? v.alcance : 'NENHUM',
      condicaoResultanteId: odonto && v.alcance !== 'NENHUM' ? v.condicaoResultanteId || null : null,
      exigeDentista: odonto && v.exigeDentista,
      ativo: true,
    }).subscribe({
      next: () => {
        this.erro.set(null);
        this.form.reset();
        this.especialidadeEscolhida.set('');
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected alternar(p: ProcedimentoCatalogoDto): void {
    const dto: ProcedimentoCatalogoRequestDto = {
      codigo: p.codigo, nome: p.nome, especialidadeId: p.especialidadeId, sigtap: p.sigtap, alcance: p.alcance,
      condicaoResultanteId: p.condicaoResultanteId, exigeDentista: p.exigeDentista, ativo: !p.ativo,
    };
    this.service.atualizarItemCatalogo(p.uuid, dto).subscribe({
      next: () => {
        this.erro.set(null);
        this.carregar();
      },
      error: (er: HttpErrorResponse) => this.falhou(er),
    });
  }

  private falhou(e: HttpErrorResponse): void {
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar o procedimento.');
  }
}
