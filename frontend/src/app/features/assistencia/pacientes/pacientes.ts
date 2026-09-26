import { DatePipe } from '@angular/common';
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
import { ProntuarioService } from '../../../core/services/prontuario';
import { ProntuarioResponseDto } from '../../../core/models/prontuario';
import { TriagemResponseDto } from '../../../core/models/triagem';

type AbaDetalhe = 'resumo' | 'historico' | 'programas' | 'vacinacao' | 'anexos';

interface TimelineItem {
  data: string;
  tipo: 'triagem' | 'evolucao' | 'consulta' | 'procedimento';
  titulo: string;
  descricao?: string;
}

@Component({
  selector: 'app-pacientes',
  imports: [ReactiveFormsModule, RouterLink, Modal, DatePipe],
  templateUrl: './pacientes.html',
  styleUrl: './pacientes.css',
})
export class Pacientes {
  private readonly fb = inject(FormBuilder);
  private readonly pacienteService = inject(PacienteService);
  private readonly cepService = inject(CepService);
  private readonly prontuarioService = inject(ProntuarioService);

  protected readonly sexos: Sexo[] = ['MASCULINO', 'FEMININO', 'IGNORADO'];

  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly carregando = signal(true);

  protected readonly filtroStatus = signal<'TODOS' | 'ATIVO' | 'INATIVO'>('TODOS');
  protected readonly filtroFaixaEtaria = signal<'TODAS' | '0-11' | '12-17' | '18-59' | '60+'>('TODAS');
  protected readonly selecionado = signal<PacienteResponseDto | null>(null);
  protected readonly abaDetalhe = signal<AbaDetalhe>('resumo');

  protected readonly prontuario = signal<ProntuarioResponseDto | null>(null);
  protected readonly carregandoProntuario = signal(false);

  protected readonly ultimaTriagem = computed<TriagemResponseDto | null>(() => {
    const prontuario = this.prontuario();
    if (!prontuario) {
      return null;
    }
    const todasTriagens = prontuario.atendimentos.flatMap((a) => a.triagens);
    if (todasTriagens.length === 0) {
      return null;
    }
    return [...todasTriagens].sort((a, b) => b.dataHora.localeCompare(a.dataHora))[0];
  });

  protected readonly timeline = computed<TimelineItem[]>(() => {
    const prontuario = this.prontuario();
    if (!prontuario) {
      return [];
    }
    const itens: TimelineItem[] = [];
    for (const atendimento of prontuario.atendimentos) {
      for (const triagem of atendimento.triagens) {
        itens.push({
          data: triagem.dataHora,
          tipo: 'triagem',
          titulo: `Triagem · ${triagem.classificacaoRisco}`,
          descricao: triagem.observacoes,
        });
      }
      for (const evolucao of atendimento.evolucoes) {
        itens.push({
          data: evolucao.dataHora,
          tipo: 'evolucao',
          titulo: 'Evolução de enfermagem',
          descricao: evolucao.descricao,
        });
      }
      for (const item of atendimento.consultas) {
        itens.push({
          data: item.consulta.dataHora,
          tipo: 'consulta',
          titulo: `Consulta · ${item.consulta.tipoConsulta}`,
          descricao: item.consulta.diagnostico,
        });
        for (const procedimento of item.procedimentos) {
          itens.push({
            data: procedimento.dataRealizacao,
            tipo: 'procedimento',
            titulo: `Procedimento · ${procedimento.tipo}`,
            descricao: procedimento.descricao,
          });
        }
      }
    }
    return itens.sort((a, b) => b.data.localeCompare(a.data));
  });

  protected readonly timelinePreview = computed<TimelineItem[]>(() => this.timeline().slice(0, 4));

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<PacienteResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly buscandoCep = signal(false);
  protected readonly cepNaoEncontrado = signal(false);

  protected readonly pacientesFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const status = this.filtroStatus();
    const faixa = this.filtroFaixaEtaria();

    return this.pacientes().filter((p) => {
      if (termo && !p.nome.toLowerCase().includes(termo) && !p.cpf.includes(termo)) {
        return false;
      }
      if (status === 'ATIVO' && !p.ativo) {
        return false;
      }
      if (status === 'INATIVO' && p.ativo) {
        return false;
      }
      if (faixa !== 'TODAS' && this.faixaEtaria(p.dataNascimento) !== faixa) {
        return false;
      }
      return true;
    });
  });

  protected readonly totalPacientes = computed(() => this.pacientes().length);
  protected readonly totalAtivos = computed(() => this.pacientes().filter((p) => p.ativo).length);
  protected readonly totalInativos = computed(() => this.pacientes().filter((p) => !p.ativo).length);

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
        this.reselecionarAposRecarga(pacientes);
      },
      error: () => {
        this.pacientes.set([]);
        this.carregando.set(false);
        this.selecionado.set(null);
      },
    });
  }

  private reselecionarAposRecarga(pacientes: PacienteResponseDto[]): void {
    const atual = this.selecionado();
    const mantido = atual ? pacientes.find((p) => p.uuid === atual.uuid) : undefined;
    const proximo = mantido ?? pacientes[0] ?? null;
    this.selecionado.set(proximo);
    if (proximo && proximo.uuid !== atual?.uuid) {
      this.carregarProntuario(proximo.uuid);
    }
  }

  protected selecionar(paciente: PacienteResponseDto): void {
    if (this.selecionado()?.uuid === paciente.uuid) {
      return;
    }
    this.selecionado.set(paciente);
    this.abaDetalhe.set('resumo');
    this.carregarProntuario(paciente.uuid);
  }

  protected selecionarAba(aba: AbaDetalhe): void {
    this.abaDetalhe.set(aba);
  }

  private carregarProntuario(pacienteId: string): void {
    this.carregandoProntuario.set(true);
    this.prontuarioService.buscarPorPacienteId(pacienteId).subscribe({
      next: (prontuario) => {
        this.prontuario.set(prontuario);
        this.carregandoProntuario.set(false);
      },
      error: () => {
        this.prontuario.set(null);
        this.carregandoProntuario.set(false);
      },
    });
  }

  protected idade(dataNascimento: string): number {
    const nascimento = new Date(dataNascimento);
    const hoje = new Date();
    let idade = hoje.getFullYear() - nascimento.getFullYear();
    const aindaNaoFezAniversario =
      hoje.getMonth() < nascimento.getMonth() ||
      (hoje.getMonth() === nascimento.getMonth() && hoje.getDate() < nascimento.getDate());
    if (aindaNaoFezAniversario) {
      idade--;
    }
    return idade;
  }

  private faixaEtaria(dataNascimento: string): '0-11' | '12-17' | '18-59' | '60+' {
    const idade = this.idade(dataNascimento);
    if (idade <= 11) return '0-11';
    if (idade <= 17) return '12-17';
    if (idade <= 59) return '18-59';
    return '60+';
  }

  protected iniciais(nome: string): string {
    const partes = nome.trim().split(/\s+/);
    const primeira = partes[0]?.[0] ?? '';
    const ultima = partes.length > 1 ? partes[partes.length - 1][0] : '';
    return (primeira + ultima).toUpperCase();
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
