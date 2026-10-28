import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { Modal } from '../../../shared/modal/modal';
import { Drawer } from '../../../shared/drawer/drawer';
import { AdscricaoDoPaciente } from './adscricao-do-paciente';
import { formatCartaoSus, formatCpf, formatTelefone } from '../../../shared/format-mask';
import { PacienteResponseDto, Sexo } from '../../../core/models/paciente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { PacienteService } from '../../../core/services/paciente';
import { CepService } from '../../../core/services/cep';
import { ProntuarioService, semVinculo } from '../../../core/services/prontuario';
import { AcessoProntuario } from '../../../shared/acesso-prontuario/acesso-prontuario';
import { ProntuarioResponseDto } from '../../../core/models/prontuario';
import { TriagemResponseDto } from '../../../core/models/triagem';
import { AgendamentoResponseDto } from '../../../core/models/agendamento';
import { AgendamentoService } from '../../../core/services/agendamento';
import { AcessoDaInterface } from '../../../core/models/auth';
import { AuthService } from '../../../core/services/auth';
import {
  AcaoProgramaticaDto,
  AcompanhamentoDto,
  AcompanhamentosDoPacienteDto,
  MOTIVOS_SAIDA,
  MotivoSaidaAcompanhamento,
  ProgramaResumoDto,
  SITUACOES_ACAO,
  SITUACOES_ACOMPANHAMENTO,
  TIPOS_ACAO,
} from '../../../core/models/programa';
import { ProgramaService } from '../../../core/services/programa';
import { CadernetaVacinacao } from '../vacinacao/caderneta-vacinacao';

type AbaDetalhe = 'resumo' | 'historico' | 'programas' | 'vacinacao' | 'anexos';
type ModoVisualizacao = 'lista' | 'cartoes';

const MESES_ABREVIADOS = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

interface TimelineItem {
  data: string;
  tipo: 'triagem' | 'evolucao' | 'consulta' | 'procedimento';
  titulo: string;
  descricao?: string;
}

@Component({
  selector: 'app-pacientes',
  imports: [ReactiveFormsModule, RouterLink, Modal, Drawer, DatePipe, AcessoProntuario, AdscricaoDoPaciente, CadernetaVacinacao],
  templateUrl: './pacientes.html',
  styleUrl: './pacientes.css',
})
export class Pacientes {
  private readonly fb = inject(FormBuilder);
  private readonly pacienteService = inject(PacienteService);
  private readonly cepService = inject(CepService);
  private readonly prontuarioService = inject(ProntuarioService);
  private readonly agendamentoService = inject(AgendamentoService);
  private readonly programaService = inject(ProgramaService);

  // ── Programas de saúde do paciente (ADR-0119) ──────────────────────────────────────────────
  protected readonly situacoesAcompanhamento = SITUACOES_ACOMPANHAMENTO;
  /** Ações programáticas do paciente (ADR-0132). */
  protected readonly tiposAcao = TIPOS_ACAO;
  protected readonly situacoesAcao = SITUACOES_ACAO;
  protected readonly acoesDoPaciente = signal<AcaoProgramaticaDto[]>([]);
  protected readonly motivosSaida = MOTIVOS_SAIDA;
  /** `undefined` carregando, `null` sem acesso ou bloqueado (ver programasBloqueio). */
  protected readonly programasDoPaciente = signal<AcompanhamentosDoPacienteDto | null | undefined>(undefined);
  protected readonly programasBloqueio = signal<string | null>(null);
  /** Pacientes em algum programa, na rede (agregado). */
  protected readonly emProgramas = signal<number | null>(null);
  protected readonly catalogoProgramas = signal<ProgramaResumoDto[]>([]);
  protected readonly filtroPrograma = signal('');
  /** Ids dos pacientes do programa filtrado (no escopo de quem lê); `null` sem filtro. */
  private readonly noPrograma = signal<Set<string> | null>(null);
  protected readonly listaMotivosSaida = Object.keys(MOTIVOS_SAIDA) as MotivoSaidaAcompanhamento[];
  /** Gaveta de faltoso ou de saída na aba Programas (ADR-0119). */
  protected readonly gavetaPrograma = signal<
    { tipo: 'situacao'; acompanhamento: AcompanhamentoDto; nova: 'ATIVO' | 'FALTOSO' } | { tipo: 'saida'; acompanhamento: AcompanhamentoDto } | null
  >(null);
  protected readonly salvandoPrograma = signal(false);
  protected readonly erroPrograma = signal<string | null>(null);
  protected readonly errosPrograma = signal<Record<string, string>>({});
  protected readonly avisoPrograma = signal<{ titulo: string; detalhe: string } | null>(null);
  private avisoTimer: ReturnType<typeof setTimeout> | undefined;
  protected readonly situacaoProgramaForm = this.fb.nonNullable.group({ motivo: [''] });
  protected readonly saidaProgramaForm = this.fb.nonNullable.group({ motivoSaida: ['' as MotivoSaidaAcompanhamento | ''], observacao: [''], fim: [''] });

  protected readonly sexos: Sexo[] = ['MASCULINO', 'FEMININO', 'IGNORADO'];
  protected readonly modoVisualizacao = signal<ModoVisualizacao>('lista');

  protected readonly agendamentos = signal<AgendamentoResponseDto[]>([]);
  protected readonly carregandoAgendamentos = signal(false);

  /** Futuros e ainda válidos (agendados ou confirmados), do mais próximo ao mais distante. */
  protected readonly proximosAtendimentos = computed(() => {
    const agora = Date.now();
    return this.agendamentos()
      .filter((a) => (a.status === 'AGENDADO' || a.status === 'CONFIRMADO') && new Date(a.dataHora).getTime() >= agora)
      .slice(0, 3);
  });

  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly busca = signal('');
  protected readonly carregando = signal(true);

  protected readonly filtroStatus = signal<'TODOS' | 'ATIVO' | 'INATIVO'>('TODOS');
  protected readonly filtroFaixaEtaria = signal<'TODAS' | '0-11' | '12-17' | '18-59' | '60+'>('TODAS');
  protected readonly selecionado = signal<PacienteResponseDto | null>(null);
  protected readonly abaDetalhe = signal<AbaDetalhe>('resumo');

  protected readonly prontuario = signal<ProntuarioResponseDto | null>(null);
  protected readonly carregandoProntuario = signal(false);
  /** Recusa por falta de vínculo assistencial (ADR-0076); null quando não houve. */
  protected readonly prontuarioBloqueio = signal<string | null>(null);

  protected readonly ultimaTriagem = computed<TriagemResponseDto | null>(() => {
    const prontuario = this.prontuario();
    if (!prontuario) {
      return null;
    }
    // Versões retificadas continuam no prontuário, mas não são o dado vigente (ADR-0062).
    const todasTriagens = prontuario.atendimentos.flatMap((a) => a.triagens).filter((t) => !t.retificado);
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
      for (const triagem of atendimento.triagens.filter((t) => !t.retificado)) {
        itens.push({
          data: triagem.dataHora,
          tipo: 'triagem',
          titulo: `Triagem · ${triagem.classificacaoRisco}`,
          descricao: triagem.observacoes,
        });
      }
      for (const evolucao of atendimento.evolucoes.filter((e) => !e.retificado)) {
        itens.push({
          data: evolucao.dataHora,
          tipo: 'evolucao',
          titulo: 'Evolução de enfermagem',
          descricao: evolucao.descricao,
        });
      }
      for (const item of atendimento.consultas.filter((c) => !c.consulta.retificado)) {
        itens.push({
          data: item.consulta.dataHora,
          tipo: 'consulta',
          titulo: `Consulta · ${item.consulta.tipoConsulta}`,
          descricao: item.consulta.diagnostico,
        });
        for (const procedimento of item.procedimentos.filter((p) => !p.retificado)) {
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
      const noPrograma = this.noPrograma();
      if (noPrograma && !noPrograma.has(p.uuid)) {
        return false;
      }
      return true;
    });
  });

  protected readonly totalPacientes = computed(() => this.pacientes().length);
  protected readonly totalAtivos = computed(() => this.pacientes().filter((p) => p.ativo).length);
  protected readonly totalInativos = computed(() => this.pacientes().filter((p) => !p.ativo).length);

  /** Mês corrente no fuso do navegador. Pacientes sem data de cadastro (anteriores ao campo) não contam. */
  protected readonly novosCadastrosMes = computed(() => {
    const hoje = new Date();
    return this.pacientes().filter((p) => {
      if (!p.dataCadastro) {
        return false;
      }
      const cadastro = new Date(p.dataCadastro);
      return cadastro.getFullYear() === hoje.getFullYear() && cadastro.getMonth() === hoje.getMonth();
    }).length;
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

  private readonly authService = inject(AuthService);
  /** O que a tela oferece (ADR-0079): sem a permissão, o botão nem aparece. A API continua decidindo. */
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  private readonly rota = inject(ActivatedRoute);
  private pedidoAtendido = false;

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.form.controls.cep.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((cep) => this.buscarCep(cep));

    this.carregarPacientes();
    this.programaService.resumoAcompanhamentos().subscribe({ next: (r) => this.emProgramas.set(r.pacientes), error: () => this.emProgramas.set(null) });
    if (this.pode('PROGRAMA.ACOMPANHAR')) {
      this.programaService.listar().subscribe({ next: (ps) => this.catalogoProgramas.set(ps.filter((p) => p.inscritos > 0 || p.situacao === 'ATIVO')), error: () => this.catalogoProgramas.set([]) });
    }
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
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
    // Vindo de outra tela (a sala de vacina, ADR-0147): ?id= abre o paciente e ?aba= a aba pedida, uma vez.
    const pedido = this.rota.snapshot.queryParamMap.get('id');
    const pedidoNaLista = pedido && !this.pedidoAtendido ? pacientes.find((p) => p.uuid === pedido) : undefined;
    if (pedidoNaLista) {
      this.pedidoAtendido = true;
      const aba = this.rota.snapshot.queryParamMap.get('aba') as AbaDetalhe | null;
      this.selecionar(pedidoNaLista);
      if (aba && ['resumo', 'historico', 'programas', 'vacinacao', 'anexos'].includes(aba)) {
        this.selecionarAba(aba);
      }
      return;
    }
    const atual = this.selecionado();
    const mantido = atual ? pacientes.find((p) => p.uuid === atual.uuid) : undefined;
    const proximo = mantido ?? pacientes[0] ?? null;
    this.selecionado.set(proximo);
    if (proximo && proximo.uuid !== atual?.uuid) {
      this.carregarDetalhe(proximo.uuid);
    }
  }

  protected selecionar(paciente: PacienteResponseDto): void {
    if (this.selecionado()?.uuid === paciente.uuid) {
      return;
    }
    this.selecionado.set(paciente);
    this.abaDetalhe.set('resumo');
    this.carregarDetalhe(paciente.uuid);
  }

  private carregarDetalhe(pacienteId: string): void {
    this.carregarProntuario(pacienteId);
    this.carregarAgendamentos(pacienteId);
    this.carregarProgramas(pacienteId);
  }

  /** Programas do paciente com as regras do prontuário (vínculo ou acesso justificado; leitura auditada). */
  protected carregarProgramas(pacienteId: string): void {
    this.programasBloqueio.set(null);
    if (!this.pode('PRONTUARIO.CONSULTAR', 'PROGRAMA.ACOMPANHAR')) {
      this.programasDoPaciente.set(null);
      return;
    }
    this.programasDoPaciente.set(undefined);
    this.acoesDoPaciente.set([]);
    this.programaService.doPaciente(pacienteId).subscribe({
      next: (r) => {
        if (this.selecionado()?.uuid !== pacienteId) return;
        this.programasDoPaciente.set(r);
        this.programaService.acoesDoPaciente(pacienteId).subscribe({
          next: (a) => {
            if (this.selecionado()?.uuid === pacienteId) this.acoesDoPaciente.set(a);
          },
          error: () => this.acoesDoPaciente.set([]),
        });
      },
      error: (e) => {
        if (this.selecionado()?.uuid !== pacienteId) return;
        this.programasDoPaciente.set(null);
        this.programasBloqueio.set(semVinculo(e));
      },
    });
  }

  /** Filtro por programa: só os pacientes acompanhados nele, nas unidades do acesso de quem lê. */
  protected filtrarPorPrograma(programaId: string): void {
    this.filtroPrograma.set(programaId);
    if (!programaId) {
      this.noPrograma.set(null);
      return;
    }
    this.programaService.inscritos(programaId).subscribe({
      next: (r) => this.noPrograma.set(new Set(r.lista.map((a) => a.pacienteId))),
      error: () => this.noPrograma.set(new Set()),
    });
  }

  // ── Faltoso e saída pela aba Programas (ADR-0119) ───────────────────────────────────────────

  protected abrirSituacaoPrograma(acompanhamento: AcompanhamentoDto, nova: 'ATIVO' | 'FALTOSO'): void {
    this.situacaoProgramaForm.reset({ motivo: '' });
    this.abrirGavetaPrograma({ tipo: 'situacao', acompanhamento, nova });
  }

  protected abrirSaidaPrograma(acompanhamento: AcompanhamentoDto): void {
    this.saidaProgramaForm.reset({ motivoSaida: '', observacao: '', fim: '' });
    this.abrirGavetaPrograma({ tipo: 'saida', acompanhamento });
  }

  private abrirGavetaPrograma(g: NonNullable<ReturnType<typeof this.gavetaPrograma>>): void {
    this.erroPrograma.set(null);
    this.errosPrograma.set({});
    this.gavetaPrograma.set(g);
  }

  protected fecharGavetaPrograma(): void {
    this.gavetaPrograma.set(null);
  }

  protected erroCampoPrograma(campo: string): string {
    return this.errosPrograma()[campo] ?? '';
  }

  protected salvarGavetaPrograma(): void {
    const g = this.gavetaPrograma();
    if (!g || this.salvandoPrograma()) return;
    const erros: Record<string, string> = {};
    const hoje = new Date();
    const hojeIso = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}-${String(hoje.getDate()).padStart(2, '0')}`;
    if (g.tipo === 'situacao') {
      if (!this.situacaoProgramaForm.getRawValue().motivo.trim()) erros['motivoSituacaoPrograma'] = 'Informe o motivo.';
    } else {
      const v = this.saidaProgramaForm.getRawValue();
      if (!v.motivoSaida) erros['motivoSaidaPrograma'] = 'Escolha o motivo.';
      if (!v.observacao.trim()) erros['observacaoSaidaPrograma'] = 'Descreva a saída.';
      if (v.fim && v.fim > hojeIso) erros['fimSaidaPrograma'] = 'A saída não pode ser no futuro.';
    }
    this.errosPrograma.set(erros);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) {
      setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
      return;
    }
    const a = g.acompanhamento;
    const req = g.tipo === 'situacao'
      ? this.programaService.mudarSituacao(a.uuid, g.nova, this.situacaoProgramaForm.getRawValue().motivo.trim())
      : this.programaService.encerrarAcompanhamento(a.uuid, this.saidaProgramaForm.getRawValue().motivoSaida as MotivoSaidaAcompanhamento,
        this.saidaProgramaForm.getRawValue().observacao.trim(), this.saidaProgramaForm.getRawValue().fim);
    const titulo = g.tipo === 'saida' ? 'Saída registrada' : g.nova === 'FALTOSO' ? 'Marcado como faltoso' : 'Voltou a ativo';
    this.salvandoPrograma.set(true);
    this.erroPrograma.set(null);
    req.subscribe({
      next: () => {
        this.salvandoPrograma.set(false);
        this.gavetaPrograma.set(null);
        this.avisoPrograma.set({ titulo, detalhe: `${a.pacienteNome} · ${a.programaNome}` });
        clearTimeout(this.avisoTimer);
        this.avisoTimer = setTimeout(() => this.avisoPrograma.set(null), 4200);
        this.carregarProgramas(a.pacienteId);
        this.programaService.resumoAcompanhamentos().subscribe({ next: (r) => this.emProgramas.set(r.pacientes), error: () => undefined });
      },
      error: (e: HttpErrorResponse) => {
        this.salvandoPrograma.set(false);
        this.erroPrograma.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }

  /** Depois de um acesso justificado: libera o prontuário e os programas. */
  protected liberado(pacienteId: string): void {
    this.carregarProntuario(pacienteId);
    this.carregarProgramas(pacienteId);
  }

  protected dataCurta(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }

  private carregarAgendamentos(pacienteId: string): void {
    this.agendamentos.set([]);
    this.carregandoAgendamentos.set(true);
    this.agendamentoService.listarPorPaciente(pacienteId).subscribe({
      next: (agendamentos) => {
        if (this.selecionado()?.uuid !== pacienteId) {
          return;
        }
        this.agendamentos.set(agendamentos);
        this.carregandoAgendamentos.set(false);
      },
      // 404 = paciente sem agendamentos; qualquer outra falha também cai em "nenhum" em vez de travar o painel.
      error: () => {
        if (this.selecionado()?.uuid !== pacienteId) {
          return;
        }
        this.agendamentos.set([]);
        this.carregandoAgendamentos.set(false);
      },
    });
  }

  protected diaDoMes(dataHora: string): string {
    return String(new Date(dataHora).getDate()).padStart(2, '0');
  }

  protected mesAbreviado(dataHora: string): string {
    return MESES_ABREVIADOS[new Date(dataHora).getMonth()];
  }

  protected tipoAgendamento(tipo: AgendamentoResponseDto['tipo']): string {
    return { CONSULTA: 'Consulta', PROCEDIMENTO: 'Procedimento', RETORNO: 'Retorno' }[tipo];
  }

  protected exportarCsv(): void {
    const cabecalho = [
      'Nome', 'CPF', 'Cartão SUS', 'Data de nascimento', 'Idade', 'Sexo',
      'Telefone', 'E-mail', 'Cidade', 'UF', 'Status', 'Data de cadastro',
    ];
    const linhas = this.pacientesFiltrados().map((p) => [
      p.nome,
      p.cpf,
      this.cartaoSusFormatado(p.cartaoSus),
      p.dataNascimento,
      String(this.idade(p.dataNascimento)),
      p.sexo === 'MASCULINO' ? 'Masculino' : p.sexo === 'FEMININO' ? 'Feminino' : 'Ignorado',
      p.telefones[0] ?? '',
      p.email ?? '',
      p.endereco.cidade,
      p.endereco.estado,
      p.ativo ? 'Ativo' : 'Inativo',
      p.dataCadastro ? p.dataCadastro.slice(0, 10) : '',
    ]);

    const csv = [cabecalho, ...linhas].map((linha) => linha.map(celulaCsv).join(';')).join('\r\n');
    // BOM: sem ele o Excel abre o arquivo como ANSI e quebra os acentos.
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `pacientes-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  protected selecionarAba(aba: AbaDetalhe): void {
    this.abaDetalhe.set(aba);
  }

  protected carregarProntuario(pacienteId: string): void {
    this.carregandoProntuario.set(true);
    this.prontuarioBloqueio.set(null);
    this.prontuarioService.buscarPorPacienteId(pacienteId).subscribe({
      next: (prontuario) => {
        this.prontuario.set(prontuario);
        this.carregandoProntuario.set(false);
      },
      error: (e) => {
        this.prontuario.set(null);
        this.prontuarioBloqueio.set(semVinculo(e));
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

  protected onCartaoSusInput(event: Event): void {
    const valor = formatCartaoSus((event.target as HTMLInputElement).value);
    this.form.controls.cartaoSus.setValue(valor);
  }

  protected cartaoSusFormatado(cartaoSus: string | undefined): string {
    return cartaoSus ? formatCartaoSus(cartaoSus) : '';
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
      cartaoSus: this.cartaoSusFormatado(paciente.cartaoSus),
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

/**
 * Aspas quando o valor tem separador, aspas ou quebra de linha. Valores que começam com
 * = + - @ ganham um apóstrofo na frente para o Excel não interpretá-los como fórmula
 * (injeção de fórmula em CSV, OWASP).
 */
function celulaCsv(valor: string): string {
  const neutralizado = /^[=+\-@]/.test(valor) ? `'${valor}` : valor;
  return /[;"\r\n]/.test(neutralizado) ? `"${neutralizado.replace(/"/g, '""')}"` : neutralizado;
}
