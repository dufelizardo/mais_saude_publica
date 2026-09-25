import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { Modal } from '../../../shared/modal/modal';
import { formatCpf, formatTelefone } from '../../../shared/format-mask';
import { PacienteResponseDto, Sexo } from '../../../core/models/paciente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { PacienteService } from '../../../core/services/paciente';
import { CepService } from '../../../core/services/cep';

@Component({
  selector: 'app-pacientes',
  imports: [ReactiveFormsModule, RouterLink, Modal],
  templateUrl: './pacientes.html',
  styleUrl: './pacientes.css',
})
export class Pacientes {
  private readonly fb = inject(FormBuilder);
  private readonly pacienteService = inject(PacienteService);
  private readonly cepService = inject(CepService);

  protected readonly sexos: Sexo[] = ['MASCULINO', 'FEMININO', 'IGNORADO'];

  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly carregando = signal(true);

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<PacienteResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly buscandoCep = signal(false);
  protected readonly cepNaoEncontrado = signal(false);

  protected readonly pacientesFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    return this.pacientes().filter(
      (p) => !termo || p.nome.toLowerCase().includes(termo) || p.cpf.includes(termo),
    );
  });

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required]],
    cpf: ['', [Validators.required]],
    cartaoSus: [''],
    dataNascimento: ['', [Validators.required]],
    sexo: ['FEMININO' as Sexo, [Validators.required]],
    telefone: ['', [Validators.required]],
    email: [''],
    ativo: [true],
    cep: ['', [Validators.required]],
    logradouro: ['', [Validators.required]],
    numeroLogradouro: ['', [Validators.required]],
    complemento: [''],
    bairro: ['', [Validators.required]],
    cidade: ['', [Validators.required]],
    estado: ['', [Validators.required]],
  });

  constructor() {
    this.form.controls.cep.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((cep) => this.buscarCep(cep));

    this.carregarPacientes();
  }

  private carregarPacientes(): void {
    this.carregando.set(true);
    this.pacienteService.listar().subscribe({
      next: (pacientes) => {
        this.pacientes.set(pacientes);
        this.carregando.set(false);
      },
      error: () => {
        this.pacientes.set([]);
        this.carregando.set(false);
      },
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

  protected abrirNovo(): void {
    this.editando.set(null);
    this.form.reset({
      nome: '',
      cpf: '',
      cartaoSus: '',
      dataNascimento: '',
      sexo: 'FEMININO',
      telefone: '',
      email: '',
      ativo: true,
      cep: '',
      logradouro: '',
      numeroLogradouro: '',
      complemento: '',
      bairro: '',
      cidade: '',
      estado: '',
    });
    this.errorMessage.set(null);
    this.modalAberto.set(true);
  }

  protected abrirEdicao(paciente: PacienteResponseDto): void {
    this.editando.set(paciente);
    this.form.reset({
      nome: paciente.nome,
      cpf: paciente.cpf,
      cartaoSus: paciente.cartaoSus ?? '',
      dataNascimento: paciente.dataNascimento,
      sexo: paciente.sexo,
      telefone: paciente.telefones[0] ?? '',
      email: paciente.email ?? '',
      ativo: paciente.ativo,
      cep: paciente.endereco.cep,
      logradouro: paciente.endereco.logradouro,
      numeroLogradouro: paciente.endereco.numeroLogradouro,
      complemento: paciente.endereco.complemento ?? '',
      bairro: paciente.endereco.bairro,
      cidade: paciente.endereco.cidade,
      estado: paciente.endereco.estado,
    });
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
      nome: raw.nome,
      cpf: raw.cpf,
      cartaoSus: raw.cartaoSus || undefined,
      dataNascimento: raw.dataNascimento,
      sexo: raw.sexo,
      telefones: [raw.telefone],
      email: raw.email || undefined,
      ativo: raw.ativo,
      endereco: {
        cep: raw.cep,
        logradouro: raw.logradouro,
        numeroLogradouro: raw.numeroLogradouro,
        complemento: raw.complemento || undefined,
        bairro: raw.bairro,
        cidade: raw.cidade,
        estado: raw.estado,
      },
    };

    const editando = this.editando();
    const request = editando
      ? this.pacienteService.atualizar(editando.uuid, dto)
      : this.pacienteService.criar(dto);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modalAberto.set(false);
        this.carregarPacientes();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = error.error as ErrorResponseDto | undefined;
        this.errorMessage.set(body?.message ?? 'Não foi possível salvar o paciente. Tente novamente.');
      },
    });
  }
}
