import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { catchError, of } from 'rxjs';
import { ConteudoEducativoDto, FORMATOS_MATERIAL, FormatoMaterial, MaterialEducativoDto } from '../../../core/models/comunicacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { ComunicacaoService } from '../../../core/services/comunicacao';
import { Drawer } from '../../../shared/drawer/drawer';

/** Gaveta Materiais de um conteúdo (ADR-0174): formatos do mesmo conhecimento, com link externo até a D2 de Documentos. */
@Component({
  selector: 'app-materiais-do-conteudo',
  imports: [ReactiveFormsModule, Drawer],
  template: `
    <app-drawer [titulo]="'Materiais'" (fechar)="fechar.emit()">
      <div class="dw-body">
        @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
        <dl class="ro"><div><dt>Conteúdo</dt><dd>{{ conteudo().titulo }}</dd></div></dl>
        <ul class="eq-prof" aria-label="Materiais do conteúdo">
          @for (m of materiais(); track m.uuid) {
            <li class="mo-row">
              <span>
                <span class="cell-main">{{ m.titulo }} <span class="tag tag--info">{{ formatos[m.formato] }}</span></span>
                @if (m.retiradoEm) { <span class="cell-sub">Retirado · {{ m.motivoRetirada }}</span> }
                @else if (m.link) { <span class="cell-sub"><a [href]="m.link" target="_blank" rel="noopener noreferrer">{{ m.link }}</a></span> }
              </span>
              @if (!m.retiradoEm && podeEditar()) {
                <div style="flex: none;"><button type="button" class="link-btn" (click)="retirando.set(m)" [attr.aria-label]="'Retirar ' + m.titulo">Retirar</button></div>
              }
            </li>
          } @empty { <li class="cell-sub">Nenhum material.</li> }
        </ul>
        @if (retirando(); as r) {
          <form class="dw-form" [formGroup]="motivoForm" (ngSubmit)="retirar(r)" novalidate>
            <div class="dw-field">
              <label for="f-motivoMaterial">Motivo da retirada de {{ r.titulo }}</label>
              <input id="f-motivoMaterial" type="text" maxlength="500" formControlName="motivo" />
            </div>
            <div style="display: flex; gap: 8px;"><button type="submit" class="btn btn--sm">Retirar</button><button type="button" class="btn btn--ghost btn--sm" (click)="retirando.set(null)">Voltar</button></div>
          </form>
        }
        @if (podeEditar()) {
          <h3 class="mo-grupo">Novo material</h3>
          <form class="dw-form" [formGroup]="form" (ngSubmit)="criar()" novalidate>
            <div class="dw-row">
              <div class="dw-field">
                <label for="f-formatoMaterial">Formato</label>
                <select id="f-formatoMaterial" formControlName="formato">
                  @for (f of listaFormatos; track f[0]) { <option [value]="f[0]">{{ f[1] }}</option> }
                </select>
              </div>
              <div class="dw-field">
                <label for="f-tituloMaterial">Título <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-tituloMaterial" type="text" maxlength="150" formControlName="titulo" [attr.aria-invalid]="!!erros()['tituloMaterial']" aria-describedby="e-tituloMaterial" />
                @if (erros()['tituloMaterial']) { <p class="dw-err" id="e-tituloMaterial">{{ erros()['tituloMaterial'] }}</p> }
              </div>
            </div>
            <div class="dw-field">
              <label for="f-linkMaterial">Link<span class="opt">opcional · https</span></label>
              <input id="f-linkMaterial" type="url" maxlength="500" formControlName="link" placeholder="https://" [attr.aria-invalid]="!!erros()['linkMaterial']" aria-describedby="h-linkMaterial e-linkMaterial" />
              <p class="dw-hint" id="h-linkMaterial">O arquivo do material entra com o domínio Documentos; por ora, o link de onde ele já está publicado.</p>
              @if (erros()['linkMaterial']) { <p class="dw-err" id="e-linkMaterial">{{ erros()['linkMaterial'] }}</p> }
            </div>
            <div><button type="submit" class="btn btn--sm" [disabled]="submitting()">Adicionar material</button></div>
          </form>
        }
      </div>
    </app-drawer>
  `,
})
export class MateriaisDoConteudo {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ComunicacaoService);

  readonly conteudo = input.required<ConteudoEducativoDto>();
  readonly podeEditar = input(false);
  readonly fechar = output<void>();
  readonly salvou = output<string>();

  protected readonly formatos = FORMATOS_MATERIAL;
  protected readonly listaFormatos = Object.entries(FORMATOS_MATERIAL) as [FormatoMaterial, string][];
  protected readonly materiais = signal<MaterialEducativoDto[]>([]);
  protected readonly retirando = signal<MaterialEducativoDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly form = this.fb.nonNullable.group({ formato: ['CARTILHA' as FormatoMaterial], titulo: [''], link: [''] });
  protected readonly motivoForm = this.fb.nonNullable.group({ motivo: [''] });

  ngOnInit(): void {
    this.carregar();
  }

  private carregar(): void {
    this.service.materiais(this.conteudo().grupoId).pipe(catchError(() => of([]))).subscribe((l) => this.materiais.set(l));
  }

  protected criar(): void {
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.titulo.trim()) erros['tituloMaterial'] = 'Informe o título.';
    if (v.link.trim() && !v.link.trim().toLowerCase().startsWith('https://')) erros['linkMaterial'] = 'O link precisa começar com https://.';
    this.erros.set(erros);
    if (Object.keys(erros).length) return;
    this.submitting.set(true);
    this.service.criarMaterial({ conteudoGrupoId: this.conteudo().grupoId, formato: v.formato, titulo: v.titulo.trim(), link: v.link.trim() || null }).subscribe({
      next: (m) => {
        this.submitting.set(false);
        this.form.reset({ formato: 'CARTILHA', titulo: '', link: '' });
        this.carregar();
        this.salvou.emit(m.titulo);
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  protected retirar(m: MaterialEducativoDto): void {
    const motivo = this.motivoForm.getRawValue().motivo.trim();
    if (!motivo) {
      this.erroApi.set('Informe o motivo da retirada.');
      return;
    }
    this.service.retirarMaterial(m.uuid, motivo).subscribe({
      next: () => {
        this.retirando.set(null);
        this.motivoForm.reset();
        this.erroApi.set(null);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private falhou(e: HttpErrorResponse): void {
    this.submitting.set(false);
    this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
  }
}
