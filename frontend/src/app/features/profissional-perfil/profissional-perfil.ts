import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ProfissionalResponseDto } from '../../core/models/profissional';
import { LotacaoResponseDto } from '../../core/models/lotacao';
import { ProfissionalService } from '../../core/services/profissional';
import { LotacaoService } from '../../core/services/lotacao';
import { formatCpf } from '../../shared/format-mask';

type Aba = 'dados' | 'lotacao';

@Component({
  selector: 'app-profissional-perfil',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './profissional-perfil.html',
  styleUrl: './profissional-perfil.css',
})
export class ProfissionalPerfil {
  private readonly fb = inject(FormBuilder);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly lotacaoService = inject(LotacaoService);

  protected readonly buscando = signal(false);
  protected readonly naoEncontrado = signal(false);
  protected readonly profissional = signal<ProfissionalResponseDto | null>(null);

  protected readonly abaAtiva = signal<Aba>('dados');

  protected readonly lotacaoVigente = signal<LotacaoResponseDto | null>(null);
  protected readonly lotacaoHistorico = signal<LotacaoResponseDto[]>([]);
  protected readonly carregandoLotacao = signal(false);

  protected readonly cpfForm = this.fb.nonNullable.group({
    cpf: ['', [Validators.required]],
  });

  protected onCpfInput(event: Event): void {
    const valor = formatCpf((event.target as HTMLInputElement).value);
    this.cpfForm.controls.cpf.setValue(valor);
  }

  protected buscar(): void {
    if (this.cpfForm.invalid) {
      this.cpfForm.markAllAsTouched();
      return;
    }

    this.buscando.set(true);
    this.naoEncontrado.set(false);
    this.profissional.set(null);

    this.profissionalService.buscarPorCpf(this.cpfForm.getRawValue().cpf).subscribe({
      next: (profissional) => {
        this.buscando.set(false);
        this.profissional.set(profissional);
        this.abaAtiva.set('dados');
        this.carregarLotacao(profissional.matricula);
      },
      error: () => {
        this.buscando.set(false);
        this.naoEncontrado.set(true);
      },
    });
  }

  private carregarLotacao(matricula: string): void {
    this.carregandoLotacao.set(true);
    this.lotacaoService.buscarVigente(matricula).subscribe({
      next: (vigente) => this.lotacaoVigente.set(vigente),
      error: () => this.lotacaoVigente.set(null),
    });
    this.lotacaoService.listarHistorico(matricula).subscribe({
      next: (historico) => {
        this.lotacaoHistorico.set(historico);
        this.carregandoLotacao.set(false);
      },
      error: () => {
        this.lotacaoHistorico.set([]);
        this.carregandoLotacao.set(false);
      },
    });
  }

  protected selecionarAba(aba: Aba): void {
    this.abaAtiva.set(aba);
  }
}
