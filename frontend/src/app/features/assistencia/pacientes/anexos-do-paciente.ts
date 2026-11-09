import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { catchError, of } from 'rxjs';
import { DocumentoDto, DocumentosDoPacienteDto, TipoDocumentoDto, VersaoDocumentoDto } from '../../../core/models/documento';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { DocumentoService } from '../../../core/services/documento';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { hojeIso } from '../../../shared/formato';

const TAMANHO_MAXIMO = 10 * 1024 * 1024;

type Gaveta = { tipo: 'anexar' } | { tipo: 'versao'; documento: DocumentoDto } | { tipo: 'retirar'; documento: DocumentoDto };

/** Aba Anexos do paciente (ADR-0172): lista, envio, versões, download e retirada. Quem só anexa vê o que anexou e não abre o arquivo. */
@Component({
  selector: 'app-anexos-do-paciente',
  imports: [ReactiveFormsModule, Drawer],
  template: `
    <div style="padding: var(--s-5);" aria-label="Anexos do paciente">
      @if (podeAnexar()) {
        <div style="display: flex; justify-content: flex-end; margin-bottom: var(--s-3);">
          <button type="button" class="btn btn--sm" (click)="abrirAnexar()">Anexar documento</button>
        </div>
      }
      @if (erroLista()) { <div class="box box--err" role="alert"><strong>{{ erroLista() }}</strong></div> }
      @if (dados() === undefined) {
        <p class="cell-sub">Carregando…</p>
      } @else if (dados(); as d) {
        @if (!d.podeAbrir) { <p class="cell-sub">Você vê só o que anexou. Abrir os arquivos é de quem lê o prontuário.</p> }
        <ul class="eq-prof" aria-label="Documentos do paciente">
          @for (x of d.documentos; track x.uuid) {
            <li class="mo-row">
              <span>
                <span class="cell-main">{{ x.titulo }} <span class="tag tag--info">{{ x.tipoNome }}</span></span>
                <span class="cell-sub">{{ dataBr(x.dataDocumento) }} · {{ x.unidadeNome }} · versão {{ x.versoes[0].numero }} · {{ tamanho(x.versoes[0]) }}</span>
                @for (v of x.versoes.slice(1); track v.uuid) {
                  <span class="cell-sub">Versão {{ v.numero }}: {{ v.nomeArquivo }}@if (d.podeAbrir) { · <button type="button" class="link-btn" (click)="baixar(v)" [attr.aria-label]="'Baixar versão ' + v.numero + ' de ' + x.titulo">Baixar</button> }</span>
                }
              </span>
              <div class="row-actions" style="flex: none; gap: 12px;">
                @if (d.podeAbrir) { <button type="button" class="link-btn" (click)="baixar(x.versoes[0])" [attr.aria-label]="'Baixar ' + x.titulo">Baixar</button> }
                @if (podeAnexar()) {
                  <button type="button" class="link-btn" (click)="abrirVersao(x)" [attr.aria-label]="'Nova versão de ' + x.titulo">Nova versão</button>
                  <button type="button" class="link-btn" (click)="abrirRetirar(x)" [attr.aria-label]="'Retirar ' + x.titulo">Retirar</button>
                }
              </div>
            </li>
          } @empty {
            <li class="cell-sub">Nenhum documento anexado.</li>
          }
        </ul>
        @if (d.retirados.length) {
          <h3 class="mo-grupo">Retirados</h3>
          <ul class="eq-prof" aria-label="Documentos retirados">
            @for (x of d.retirados; track x.uuid) {
              <li><span class="cell-main">{{ x.titulo }}</span><span class="cell-sub">{{ x.tipoNome }} · {{ x.motivoRetirada }}</span></li>
            }
          </ul>
        }
      } @else {
        <p class="cell-sub">Sem acesso aos documentos deste paciente.</p>
      }
    </div>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="tituloGaveta(g)" (fechar)="gaveta.set(null)">
        <form class="dw-form" [formGroup]="form" (ngSubmit)="salvar()" novalidate>
          <div class="dw-body">
            @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
            @if (g.tipo !== 'anexar') { <dl class="ro"><div><dt>Documento</dt><dd>{{ g.documento.titulo }}</dd></div></dl> }
            @if (g.tipo === 'anexar') {
              <div class="dw-field">
                <label for="f-tipoDocumento">Tipo <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <select id="f-tipoDocumento" formControlName="tipoId" [attr.aria-invalid]="!!erro('tipoDocumento')" aria-describedby="e-tipoDocumento">
                  <option value="">Escolha…</option>
                  @for (t of tiposAtivos(); track t.uuid) { <option [value]="t.uuid">{{ t.nome }}</option> }
                </select>
                @if (erro('tipoDocumento')) { <p class="dw-err" id="e-tipoDocumento">{{ erro('tipoDocumento') }}</p> }
              </div>
              <div class="dw-field">
                <label for="f-tituloDocumento">Título <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-tituloDocumento" type="text" maxlength="150" formControlName="titulo" placeholder="Ex.: Hemograma de março" [attr.aria-invalid]="!!erro('tituloDocumento')" aria-describedby="e-tituloDocumento" />
                @if (erro('tituloDocumento')) { <p class="dw-err" id="e-tituloDocumento">{{ erro('tituloDocumento') }}</p> }
              </div>
              <div class="dw-row">
                <div class="dw-field">
                  <label for="f-unidadeDocumento">Unidade <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                  <select id="f-unidadeDocumento" formControlName="unidadeId" [attr.aria-invalid]="!!erro('unidadeDocumento')" aria-describedby="e-unidadeDocumento">
                    <option value="">Escolha…</option>
                    @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                  </select>
                  @if (erro('unidadeDocumento')) { <p class="dw-err" id="e-unidadeDocumento">{{ erro('unidadeDocumento') }}</p> }
                </div>
                <div class="dw-field">
                  <label for="f-dataDocumento">Data<span class="opt">opcional</span></label>
                  <input id="f-dataDocumento" type="date" formControlName="dataDocumento" [attr.max]="hoje" />
                </div>
              </div>
            }
            @if (g.tipo === 'retirar') {
              <div class="box box--info"><div>O documento sai da lista, mas nada se apaga.</div></div>
              <div class="dw-field">
                <label for="f-motivoRetirada">Motivo <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-motivoRetirada" type="text" maxlength="500" formControlName="motivo" placeholder="Ex.: anexado no paciente errado" [attr.aria-invalid]="!!erro('motivoRetirada')" aria-describedby="e-motivoRetirada" />
                @if (erro('motivoRetirada')) { <p class="dw-err" id="e-motivoRetirada">{{ erro('motivoRetirada') }}</p> }
              </div>
            } @else {
              <div class="dw-field">
                <label for="f-arquivo">Arquivo <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-arquivo" type="file" accept=".pdf,.jpg,.jpeg,.png" [attr.aria-invalid]="!!erro('arquivo')" aria-describedby="h-arquivo e-arquivo" (change)="escolherArquivo($event)" />
                <p class="dw-hint" id="h-arquivo">PDF, JPG ou PNG, até 10 MB.</p>
                @if (erro('arquivo')) { <p class="dw-err" id="e-arquivo">{{ erro('arquivo') }}</p> }
              </div>
            }
          </div>
          <footer class="dw-foot">
            <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button>
            <button type="submit" class="btn" [disabled]="submitting()">{{ submitting() ? 'Enviando…' : g.tipo === 'retirar' ? 'Retirar' : 'Enviar' }}</button>
          </footer>
        </form>
      </app-drawer>
    }

    @if (toast(); as t) {
      <div class="toast" role="status" aria-live="polite">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12"></polyline></svg>
        <div>{{ t }}</div>
      </div>
    }
  `,
})
export class AnexosDoPaciente {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(DocumentoService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  readonly pacienteId = input.required<string>();
  readonly podeAnexar = input(false);

  protected readonly hoje = hojeIso();
  /** undefined carregando; null sem acesso. */
  protected readonly dados = signal<DocumentosDoPacienteDto | null | undefined>(undefined);
  protected readonly tipos = signal<TipoDocumentoDto[]>([]);
  protected readonly tiposAtivos = computed(() => this.tipos().filter((t) => t.ativo));
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erroLista = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<string | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;
  private arquivo: File | null = null;

  protected readonly form = this.fb.nonNullable.group({ tipoId: [''], titulo: [''], unidadeId: [''], dataDocumento: [''], motivo: [''] });

  constructor() {
    effect(() => {
      const id = this.pacienteId();
      untracked(() => this.carregar(id));
    });
  }

  private carregar(id: string): void {
    this.dados.set(undefined);
    this.service.doPaciente(id).pipe(catchError(() => of(null))).subscribe((d) => this.dados.set(d));
  }

  protected dataBr(iso: string): string {
    return iso.slice(0, 10).split('-').reverse().join('/');
  }

  protected tamanho(v: VersaoDocumentoDto): string {
    return v.tamanho < 1024 * 1024 ? `${Math.max(1, Math.round(v.tamanho / 1024))} KB` : `${(v.tamanho / (1024 * 1024)).toFixed(1).replace('.', ',')} MB`;
  }

  protected tituloGaveta(g: Gaveta): string {
    return g.tipo === 'anexar' ? 'Anexar documento' : g.tipo === 'versao' ? 'Nova versão' : 'Retirar documento';
  }

  private abrir(g: Gaveta): void {
    this.form.reset();
    this.arquivo = null;
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected abrirAnexar(): void {
    if (!this.tipos().length) this.service.tipos().pipe(catchError(() => of([]))).subscribe((t) => this.tipos.set(t));
    if (!this.unidades().length) this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.abrir({ tipo: 'anexar' });
  }

  protected abrirVersao(documento: DocumentoDto): void {
    this.abrir({ tipo: 'versao', documento });
  }

  protected abrirRetirar(documento: DocumentoDto): void {
    this.abrir({ tipo: 'retirar', documento });
  }

  protected escolherArquivo(evento: Event): void {
    this.arquivo = (evento.target as HTMLInputElement).files?.[0] ?? null;
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  private validar(erros: Record<string, string>): boolean {
    this.erros.set(erros);
    this.erroApi.set(null);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
    return !primeiro;
  }

  private validarArquivo(erros: Record<string, string>): void {
    if (!this.arquivo) erros['arquivo'] = 'Escolha o arquivo.';
    else if (this.arquivo.size > TAMANHO_MAXIMO) erros['arquivo'] = 'O arquivo vai até 10 MB.';
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    if (g.tipo === 'anexar') {
      if (!v.tipoId) erros['tipoDocumento'] = 'Escolha o tipo.';
      if (!v.titulo.trim()) erros['tituloDocumento'] = 'Informe o título.';
      if (!v.unidadeId) erros['unidadeDocumento'] = 'Escolha a unidade.';
      this.validarArquivo(erros);
      if (!this.validar(erros)) return;
      this.enviar(this.service.anexar(this.pacienteId(), { tipoId: v.tipoId, unidadeId: v.unidadeId, titulo: v.titulo.trim(), dataDocumento: v.dataDocumento }, this.arquivo!), 'Documento anexado');
    } else if (g.tipo === 'versao') {
      this.validarArquivo(erros);
      if (!this.validar(erros)) return;
      this.enviar(this.service.novaVersao(g.documento.uuid, this.arquivo!), 'Nova versão enviada');
    } else {
      if (!v.motivo.trim()) erros['motivoRetirada'] = 'Informe o motivo.';
      if (!this.validar(erros)) return;
      this.enviar(this.service.retirar(g.documento.uuid, v.motivo.trim()), 'Documento retirado');
    }
  }

  private enviar(req: ReturnType<DocumentoService['retirar']>, aviso: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: (d) => {
        this.submitting.set(false);
        this.dados.set(d);
        this.gaveta.set(null);
        this.mostrar(aviso);
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível enviar. Tente novamente.');
      },
    });
  }

  private mostrar(aviso: string): void {
    this.toast.set(aviso);
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }

  protected baixar(v: VersaoDocumentoDto): void {
    this.erroLista.set(null);
    this.service.abrir(v.uuid).subscribe({
      next: (r) => {
        const url = URL.createObjectURL(r.body ?? new Blob());
        const link = document.createElement('a');
        link.href = url;
        link.download = v.nomeArquivo;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.erroLista.set('Não foi possível abrir o arquivo.'),
    });
  }
}
