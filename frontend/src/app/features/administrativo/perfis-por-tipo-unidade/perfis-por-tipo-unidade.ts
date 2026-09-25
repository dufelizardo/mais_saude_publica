import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../shared/modal/modal';
import { PerfilPorTipoUnidadeResponseDto } from '../../../core/models/perfil-por-tipo-unidade';
import { PerfilAdministrativoResponseDto } from '../../../core/models/perfil-administrativo';
import { TipoUnidadeDeSaude } from '../../../core/models/unidade-saude';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { PerfilPorTipoUnidadeService } from '../../../core/services/perfil-por-tipo-unidade';
import { PerfilAdministrativoService } from '../../../core/services/perfil-administrativo';

@Component({
  selector: 'app-perfis-por-tipo-unidade',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './perfis-por-tipo-unidade.html',
  styleUrl: './perfis-por-tipo-unidade.css',
})
export class PerfisPorTipoUnidade {
  private readonly fb = inject(FormBuilder);
  private readonly perfilPorTipoService = inject(PerfilPorTipoUnidadeService);
  private readonly perfilAdministrativoService = inject(PerfilAdministrativoService);

  // Só os tipos que são efetivamente "Unidade de Saúde" (ver ADR-0038, decisão 5) — os 4 níveis
  // superiores da hierarquia (FEDERAL/ESTADUAL/MUNICIPAL/REGIONAL) não fazem sentido aqui.
  protected readonly tiposDeUnidade: TipoUnidadeDeSaude[] = [
    'UBS',
    'HOSPITAL',
    'UPA',
    'LABORATORIO',
    'CAPS',
    'CENTRO_ESPECIALIDADES',
    'CENTRO_REABILITACAO',
    'POLICLINICA',
  ];

  protected readonly mapeamentos = signal<PerfilPorTipoUnidadeResponseDto[]>([]);
  protected readonly perfis = signal<PerfilAdministrativoResponseDto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<PerfilPorTipoUnidadeResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly tiposDisponiveis = computed(() => {
    const editandoUuid = this.editando()?.uuid;
    const tiposJaMapeados = new Set(
      this.mapeamentos()
        .filter((m) => m.uuid !== editandoUuid)
        .map((m) => m.tipo),
    );
    return this.tiposDeUnidade.filter((tipo) => !tiposJaMapeados.has(tipo));
  });

  protected readonly form = this.fb.nonNullable.group({
    tipo: ['' as TipoUnidadeDeSaude | '', [Validators.required]],
    perfilAdministrativoId: ['', [Validators.required]],
  });

  constructor() {
    this.carregarPerfis();
    this.carregarMapeamentos();
  }

  private carregarPerfis(): void {
    this.perfilAdministrativoService.listar().subscribe({
      next: (perfis) => this.perfis.set(perfis),
      error: () => this.perfis.set([]),
    });
  }

  private carregarMapeamentos(): void {
    this.carregando.set(true);
    this.perfilPorTipoService.listar().subscribe({
      next: (mapeamentos) => {
        this.mapeamentos.set(mapeamentos);
        this.carregando.set(false);
      },
      error: () => {
        this.mapeamentos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({ tipo: '', perfilAdministrativoId: '' });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(mapeamento: PerfilPorTipoUnidadeResponseDto): void {
    this.editando.set(mapeamento);
    this.form.reset({ tipo: mapeamento.tipo, perfilAdministrativoId: mapeamento.perfilAdministrativoUuid });
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
      tipo: raw.tipo as TipoUnidadeDeSaude,
      perfilAdministrativoId: raw.perfilAdministrativoId,
    };

    const editando = this.editando();
    const request = editando
      ? this.perfilPorTipoService.atualizar(editando.uuid, dto)
      : this.perfilPorTipoService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarMapeamentos();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar o mapeamento. Tente novamente.');
      },
    });
  }
}
