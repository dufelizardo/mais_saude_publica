import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ProntuarioResponseDto } from '../../../core/models/prontuario';
import { PacienteService } from '../../../core/services/paciente';
import { ProntuarioService } from '../../../core/services/prontuario';

@Component({
  selector: 'app-prontuario',
  imports: [ReactiveFormsModule],
  templateUrl: './prontuario.html',
  styleUrl: './prontuario.css',
})
export class Prontuario {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly pacienteService = inject(PacienteService);
  private readonly prontuarioService = inject(ProntuarioService);

  protected readonly buscando = signal(false);
  protected readonly naoEncontrado = signal(false);
  protected readonly prontuario = signal<ProntuarioResponseDto | null>(null);

  protected readonly cpfForm = this.fb.nonNullable.group({
    cpf: ['', [Validators.required]],
  });

  constructor() {
    const pacienteIdNaUrl = this.route.snapshot.queryParamMap.get('pacienteId');
    if (pacienteIdNaUrl) {
      this.buscarProntuario(pacienteIdNaUrl);
    }
  }

  protected buscarPorCpf(): void {
    if (this.cpfForm.invalid) {
      this.cpfForm.markAllAsTouched();
      return;
    }

    this.buscando.set(true);
    this.naoEncontrado.set(false);
    this.prontuario.set(null);

    const cpf = this.cpfForm.getRawValue().cpf;
    this.pacienteService.buscarPorCpf(cpf).subscribe({
      next: (pacientes) => {
        const paciente = pacientes[0];
        if (!paciente) {
          this.buscando.set(false);
          this.naoEncontrado.set(true);
          return;
        }
        this.buscarProntuario(paciente.uuid);
      },
      error: () => {
        this.buscando.set(false);
        this.naoEncontrado.set(true);
      },
    });
  }

  private buscarProntuario(pacienteId: string): void {
    this.buscando.set(true);
    this.naoEncontrado.set(false);

    this.prontuarioService.buscarPorPacienteId(pacienteId).subscribe({
      next: (prontuario) => {
        this.buscando.set(false);
        this.prontuario.set(prontuario);
      },
      error: () => {
        this.buscando.set(false);
        this.naoEncontrado.set(true);
        this.prontuario.set(null);
      },
    });
  }
}
