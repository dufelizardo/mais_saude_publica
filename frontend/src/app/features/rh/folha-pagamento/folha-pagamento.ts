import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FolhaPagamentoResponseDto } from '../../../core/models/folha-pagamento';
import { FolhaPagamentoService } from '../../../core/services/folha-pagamento';

@Component({
  selector: 'app-folha-pagamento',
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './folha-pagamento.html',
  styleUrl: './folha-pagamento.css',
})
export class FolhaPagamento {
  private readonly fb = inject(FormBuilder);
  private readonly folhaPagamentoService = inject(FolhaPagamentoService);

  protected readonly buscando = signal(false);
  protected readonly buscou = signal(false);
  protected readonly folhas = signal<FolhaPagamentoResponseDto[]>([]);

  protected readonly form = this.fb.nonNullable.group({
    competencia: ['', [Validators.required, Validators.pattern(/^\d{2}\/\d{4}$/)]],
  });

  protected readonly totalGeral = computed(() => this.folhas().reduce((soma, f) => soma + f.total, 0));

  protected buscar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.buscando.set(true);
    this.buscou.set(false);

    this.folhaPagamentoService.listarPorCompetencia(this.form.getRawValue().competencia).subscribe({
      next: (folhas) => {
        this.folhas.set(folhas);
        this.buscando.set(false);
        this.buscou.set(true);
      },
      error: () => {
        this.folhas.set([]);
        this.buscando.set(false);
        this.buscou.set(true);
      },
    });
  }
}
