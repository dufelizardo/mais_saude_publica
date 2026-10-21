import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { catchError, of } from 'rxjs';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { EspecialidadeDto } from '../../../core/models/saude-bucal';
import { SaudeBucalService } from '../../../core/services/saude-bucal';

/**
 * Catálogo de especialidades (ADR-0134): as do atendimento e da agenda. Quem mantém a estrutura da rede cadastra e
 * desativa; o código não muda, e a especialidade odontológica habilita a avaliação odontológica no atendimento.
 */
@Component({
  selector: 'app-catalogo-especialidades',
  imports: [ReactiveFormsModule],
  template: `
    <section class="card" style="padding: var(--s-5); margin-top: var(--s-4);" aria-labelledby="especialidades-titulo">
      <h2 id="especialidades-titulo" style="font-size: 1rem;">Especialidades</h2>
      <p class="cell-sub">As especialidades do atendimento e da agenda. A odontológica habilita a avaliação odontológica.</p>
      @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
      <ul class="eq-prof" aria-label="Catálogo de especialidades">
        @for (e of especialidades(); track e.uuid) {
          <li class="mo-row">
            <span>
              <span class="cell-main">{{ e.nome }}@if (e.odontologica) { <span class="tag tag--info">Odontológica</span> }@if (!e.ativa) { <span class="tag tag--muted">Inativa</span> }</span>
              <span class="cell-sub mono">{{ e.codigo }}</span>
            </span>
            @if (podeGerenciar()) {
              <button type="button" class="link-btn" (click)="alternar(e)" [attr.aria-label]="(e.ativa ? 'Desativar: ' : 'Reativar: ') + e.nome">{{ e.ativa ? 'Desativar' : 'Reativar' }}</button>
            }
          </li>
        }
      </ul>
      @if (podeGerenciar()) {
        <form [formGroup]="form" (ngSubmit)="criar()" novalidate aria-label="Nova especialidade" class="dw-row" style="margin-top: var(--s-3); align-items: end;">
          <div class="dw-field"><label for="f-especialidade-codigo">Código</label><input id="f-especialidade-codigo" type="text" class="mono" formControlName="codigo" placeholder="Ex.: ODONTOPEDIATRIA" /></div>
          <div class="dw-field"><label for="f-especialidade-nome">Nome</label><input id="f-especialidade-nome" type="text" maxlength="100" formControlName="nome" /></div>
          <div class="dw-field"><label class="switch" for="f-especialidade-odonto"><span><b style="font-weight: 500;">Odontológica</b></span><input type="checkbox" role="switch" id="f-especialidade-odonto" formControlName="odontologica" /></label></div>
          <div class="dw-field"><button type="submit" class="btn btn--sm">Incluir especialidade</button></div>
        </form>
      }
    </section>
  `,
})
export class CatalogoEspecialidades {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(SaudeBucalService);

  readonly podeGerenciar = input(false);

  protected readonly especialidades = signal<EspecialidadeDto[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly form = this.fb.nonNullable.group({ codigo: '', nome: '', odontologica: false });

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.service.especialidades().pipe(catchError(() => of([]))).subscribe((e) => this.especialidades.set(e));
  }

  protected criar(): void {
    const v = this.form.getRawValue();
    const codigo = v.codigo.trim().toUpperCase();
    if (!/^[A-Z][A-Z0-9_]{1,39}$/.test(codigo) || !v.nome.trim()) {
      this.erro.set('Informe o código (letras maiúsculas, números e sublinhado) e o nome.');
      return;
    }
    this.service.criarEspecialidade({ codigo, nome: v.nome.trim(), odontologica: v.odontologica, ativa: true }).subscribe({
      next: () => {
        this.erro.set(null);
        this.form.reset({ codigo: '', nome: '', odontologica: false });
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected alternar(e: EspecialidadeDto): void {
    this.service.atualizarEspecialidade(e.uuid, { codigo: e.codigo, nome: e.nome, odontologica: e.odontologica, ativa: !e.ativa }).subscribe({
      next: () => {
        this.erro.set(null);
        this.carregar();
      },
      error: (er: HttpErrorResponse) => this.falhou(er),
    });
  }

  private falhou(e: HttpErrorResponse): void {
    this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar a especialidade.');
  }
}
