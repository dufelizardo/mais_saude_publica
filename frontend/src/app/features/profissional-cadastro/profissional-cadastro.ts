import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ErrorResponseDto } from '../../core/models/profissional';
import { ProfissionalService } from '../../core/services/profissional';
import { CepService } from '../../core/services/cep';

@Component({
  selector: 'app-profissional-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profissional-cadastro.html',
})
export class ProfissionalCadastro {
  private readonly fb = inject(FormBuilder);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly cepService = inject(CepService);

  protected readonly submitting = signal(false);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly buscandoCep = signal(false);
  protected readonly cepNaoEncontrado = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    cpf: ['', [Validators.required]],
    nome: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    conselhoClasse: [''],
    numeroConselho: [''],
    telefone: ['', [Validators.required]],
    cep: ['', [Validators.required]],
    logradouro: ['', [Validators.required]],
    numeroLogradouro: ['', [Validators.required]],
    complemento: [''],
    bairro: ['', [Validators.required]],
    cidade: ['', [Validators.required]],
    estado: ['', [Validators.required]],
    dataAdmissao: [''],
  });

  constructor() {
    this.form.controls.cep.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((cep) => this.buscarCep(cep));
  }

  private buscarCep(cepDigitado: string): void {
    const cep = cepDigitado.replace(/\D/g, '');
    this.cepNaoEncontrado.set(false);

    if (cep.length !== 8) {
      return;
    }

    this.buscandoCep.set(true);
    this.cepService.buscar(cep).subscribe({
      next: (endereco) => {
        this.buscandoCep.set(false);
        if (endereco.erro) {
          this.cepNaoEncontrado.set(true);
          return;
        }
        this.form.patchValue({
          logradouro: endereco.logradouro,
          bairro: endereco.bairro,
          cidade: endereco.localidade,
          estado: endereco.uf,
        });
      },
      error: () => {
        this.buscandoCep.set(false);
        this.cepNaoEncontrado.set(true);
      },
    });
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.successMessage.set(null);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    this.profissionalService
      .create({
        cpf: raw.cpf,
        nome: raw.nome,
        email: raw.email,
        telefones: [raw.telefone],
        endereco: {
          cep: raw.cep,
          logradouro: raw.logradouro,
          numeroLogradouro: raw.numeroLogradouro,
          complemento: raw.complemento || undefined,
          bairro: raw.bairro,
          cidade: raw.cidade,
          estado: raw.estado,
        },
        conselhoClasse: raw.conselhoClasse || undefined,
        numeroConselho: raw.numeroConselho || undefined,
        dataAdmissao: raw.dataAdmissao || undefined,
      })
      .subscribe({
        next: (response) => {
          this.submitting.set(false);
          this.successMessage.set(response.details);
          this.form.reset();
        },
        error: (error: HttpErrorResponse) => {
          this.submitting.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.errorMessage.set(body?.message ?? 'Não foi possível cadastrar o profissional. Tente novamente.');
        },
      });
  }
}
