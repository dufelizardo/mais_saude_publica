import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Modal } from '../../../shared/modal/modal';
import { RegraAnuenioResponseDto } from '../../../core/models/regra-anuenio';
import { CategoriaSalarialResponseDto } from '../../../core/models/categoria-salarial';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { RegraAnuenioService } from '../../../core/services/regra-anuenio';
import { CategoriaSalarialService } from '../../../core/services/categoria-salarial';

@Component({
  selector: 'app-regras-anuenio',
  imports: [ReactiveFormsModule, RouterLink, Modal],
  templateUrl: './regras-anuenio.html',
  styleUrl: './regras-anuenio.css',
})
export class RegrasAnuenio {
  private readonly fb = inject(FormBuilder);
  private readonly regraAnuenioService = inject(RegraAnuenioService);
  private readonly categoriaSalarialService = inject(CategoriaSalarialService);

  protected readonly regras = signal<RegraAnuenioResponseDto[]>([]);
  protected readonly categorias = signal<CategoriaSalarialResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<RegraAnuenioResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly categoriasSemRegra = computed(() => {
    const idsComRegra = new Set(this.regras().map((r) => r.categoriaUuid));
    return this.categorias().filter((c) => !idsComRegra.has(c.uuid));
  });

  protected readonly form = this.fb.nonNullable.group({
    categoriaId: ['', [Validators.required]],
    percentualPorAno: ['', [Validators.required]],
    tetoAnos: [''],
  });

  constructor() {
    this.carregarCategorias();
    this.carregarRegras();
  }

  private carregarCategorias(): void {
    this.categoriaSalarialService.listar().subscribe({
      next: (categorias) => this.categorias.set(categorias),
      error: () => this.categorias.set([]),
    });
  }

  private carregarRegras(): void {
    this.carregando.set(true);
    this.regraAnuenioService.listar().subscribe({
      next: (regras) => {
        this.regras.set(regras);
        this.carregando.set(false);
      },
      error: () => {
        this.regras.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({ categoriaId: '', percentualPorAno: '', tetoAnos: '' });
    this.form.controls.categoriaId.enable();
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(regra: RegraAnuenioResponseDto): void {
    this.editando.set(regra);
    this.form.reset({
      categoriaId: regra.categoriaUuid,
      percentualPorAno: String(regra.percentualPorAno),
      tetoAnos: regra.tetoAnos != null ? String(regra.tetoAnos) : '',
    });
    this.form.controls.categoriaId.disable();
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected fecharModal(): void {
    this.modalAberto.set(false);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    const dto = {
      categoriaId: raw.categoriaId,
      percentualPorAno: Number(raw.percentualPorAno),
      tetoAnos: raw.tetoAnos ? Number(raw.tetoAnos) : undefined,
    };

    const editando = this.editando();
    const request = editando
      ? this.regraAnuenioService.atualizar(editando.uuid, dto)
      : this.regraAnuenioService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarRegras();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar a regra de anuênio. Tente novamente.');
      },
    });
  }
}
