import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ErrorResponseDto } from '../../core/models/profissional';
import { ProfissionalService } from '../../core/services/profissional';
import { CepService } from '../../core/services/cep';
import { CargoResponseDto } from '../../core/models/cargo';
import { CargoService } from '../../core/services/cargo';
import { UnidadeSaudeResponseDto } from '../../core/models/unidade-saude';
import { UnidadeSaudeService } from '../../core/services/unidade-saude';
import { LotacaoService } from '../../core/services/lotacao';
import { formatCpf, formatTelefone } from '../../shared/format-mask';

@Component({
  selector: 'app-profissional-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profissional-cadastro.html',
  styleUrl: './profissional-cadastro.css',
})
export class ProfissionalCadastro {
  private readonly fb = inject(FormBuilder);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly cepService = inject(CepService);
  private readonly cargoService = inject(CargoService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly lotacaoService = inject(LotacaoService);

  protected readonly submitting = signal(false);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly buscandoCep = signal(false);
  protected readonly cepNaoEncontrado = signal(false);

  protected readonly cargos = signal<CargoResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);

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
    dataAdmissao: ['', [Validators.required]],
    unidadeId: ['', [Validators.required]],
    cargoId: ['', [Validators.required]],
  });

  constructor() {
    this.form.controls.cep.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((cep) => this.buscarCep(cep));

    this.cargoService.listar().subscribe({
      next: (cargos) => this.cargos.set(cargos),
      error: () => this.cargos.set([]),
    });
    this.unidadeSaudeService.listar().subscribe({
      next: (unidades) => this.unidades.set(unidades),
      error: () => this.unidades.set([]),
    });
  }

  protected onCpfInput(event: Event): void {
    const valor = formatCpf((event.target as HTMLInputElement).value);
    this.form.controls.cpf.setValue(valor);
  }

  protected onTelefoneInput(event: Event): void {
    const valor = formatTelefone((event.target as HTMLInputElement).value);
    this.form.controls.telefone.setValue(valor);
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
        next: () => this.vincularLotacao(raw.cpf, raw.unidadeId, raw.cargoId, raw.dataAdmissao),
        error: (error: HttpErrorResponse) => {
          this.submitting.set(false);
          const body = error.error as ErrorResponseDto | undefined;
          this.errorMessage.set(body?.message ?? 'Não foi possível cadastrar o profissional. Tente novamente.');
        },
      });
  }

  private vincularLotacao(cpf: string, unidadeId: string, cargoId: string, dataAdmissao: string): void {
    this.profissionalService.buscarPorCpf(cpf).subscribe({
      next: (profissional) => {
        this.lotacaoService
          .criar({
            matriculaProfissional: profissional.matricula,
            unidadeId,
            cargoId,
            dataInicio: dataAdmissao,
            motivo: 'Admissão',
          })
          .subscribe({
            next: () => {
              this.submitting.set(false);
              this.successMessage.set(`Profissional cadastrado com sucesso! Matrícula: ${profissional.matricula}`);
              this.form.reset();
            },
            error: (error: HttpErrorResponse) => {
              this.submitting.set(false);
              const body = error.error as ErrorResponseDto | undefined;
              this.errorMessage.set(
                `Profissional cadastrado (matrícula ${profissional.matricula}), mas não foi possível vincular cargo/unidade: ` +
                  (body?.message ?? 'erro desconhecido') +
                  '. Registre a lotação manualmente.',
              );
            },
          });
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set(
          'Profissional cadastrado, mas não foi possível confirmar a matrícula para vincular cargo/unidade. Registre a lotação manualmente.',
        );
      },
    });
  }
}
