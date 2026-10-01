import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { CandidatoResponseDto, StatusCandidato } from '../../../core/models/candidato';
import { CargoResponseDto } from '../../../core/models/cargo';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { StatusVaga, VagaResponseDto } from '../../../core/models/vaga';
import { AuthService } from '../../../core/services/auth';
import { CandidatoService } from '../../../core/services/candidato';
import { CargoService } from '../../../core/services/cargo';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { VagaService } from '../../../core/services/vaga';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';

type Gaveta = { tipo: 'vaga'; vaga: VagaResponseDto | null } | { tipo: 'candidatos'; vaga: VagaResponseDto };

const STATUS_VAGA: { valor: StatusVaga; rotulo: string; classe: string }[] = [
  { valor: 'ABERTA', rotulo: 'Aberta', classe: 'ok' },
  { valor: 'EM_ANDAMENTO', rotulo: 'Em andamento', classe: 'info' },
  { valor: 'FECHADA', rotulo: 'Fechada', classe: 'muted' },
  { valor: 'CANCELADA', rotulo: 'Cancelada', classe: 'alert' },
];

const STATUS_CANDIDATO: { valor: StatusCandidato; rotulo: string; classe: string }[] = [
  { valor: 'INSCRITO', rotulo: 'Inscrito', classe: 'muted' },
  { valor: 'TRIAGEM', rotulo: 'Triagem', classe: 'info' },
  { valor: 'ENTREVISTA', rotulo: 'Entrevista', classe: 'purple' },
  { valor: 'APROVADO', rotulo: 'Aprovado', classe: 'ok' },
  { valor: 'REPROVADO', rotulo: 'Reprovado', classe: 'alert' },
];

/**
 * Recrutamento (ADR-0073): vagas por unidade e cargo e, em gaveta, os candidatos de cada vaga. Junta as antigas
 * telas Vagas e Candidatos; as rotas antigas redirecionam para cá.
 */
@Component({
  selector: 'app-recrutamento',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './recrutamento.html',
})
export class Recrutamento {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly vagaService = inject(VagaService);
  private readonly candidatoService = inject(CandidatoService);
  private readonly cargoService = inject(CargoService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly route = inject(ActivatedRoute);

  protected readonly formatCpf = formatCpf;
  protected readonly statusVaga = STATUS_VAGA;
  protected readonly statusCandidato = STATUS_CANDIDATO;

  protected readonly vagas = signal<VagaResponseDto[]>([]);
  protected readonly cargos = signal<CargoResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly busca = signal('');
  protected readonly filtroStatus = signal<StatusVaga | ''>('');
  protected readonly filtroUnidade = signal('');

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly candidatos = signal<CandidatoResponseDto[] | null>(null);

  protected readonly vagaForm = this.fb.nonNullable.group({ unidadeId: [''], cargoId: [''], quantidade: ['1'], status: ['ABERTA' as StatusVaga] });
  protected readonly candidatoForm = this.fb.nonNullable.group({ nome: [''], cpf: [''], curriculoUrl: [''], status: ['INSCRITO' as StatusCandidato] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    const vagaId = this.route.snapshot.queryParamMap.get('vaga');
    this.carregar(() => {
      const vaga = vagaId ? this.vagas().find((v) => v.uuid === vagaId) : null;
      if (vaga) this.abrirCandidatos(vaga);
    });
  }

  private carregar(depois?: () => void): void {
    this.carregando.set(true);
    this.vagaService.listar().pipe(catchError(() => of([]))).subscribe((v) => {
      this.vagas.set(v);
      this.carregando.set(false);
      depois?.();
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected readonly filtradas = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const st = this.filtroStatus();
    const un = this.filtroUnidade();
    const ordem = STATUS_VAGA.map((s) => s.valor);
    return this.vagas()
      .filter((v) => (!q || `${v.cargoNome} ${v.unidadeNome}`.toLowerCase().includes(q)) && (!st || v.status === st) && (!un || v.unidadeUuid === un))
      .sort((a, b) => ordem.indexOf(a.status) - ordem.indexOf(b.status) || a.cargoNome.localeCompare(b.cargoNome));
  });

  protected readonly unidadesComVaga = computed(() => {
    const mapa = new Map(this.vagas().map((v) => [v.unidadeUuid, v.unidadeNome]));
    return [...mapa.entries()].map(([uuid, nome]) => ({ uuid, nome })).sort((a, b) => a.nome.localeCompare(b.nome));
  });

  protected readonly resumo = computed(() => {
    const de = (s: StatusVaga) => this.vagas().filter((v) => v.status === s);
    const abertas = de('ABERTA');
    return {
      abertas: abertas.length,
      posicoesAbertas: abertas.reduce((t, v) => t + v.quantidade, 0),
      emAndamento: de('EM_ANDAMENTO').length,
      fechadas: de('FECHADA').length,
      canceladas: de('CANCELADA').length,
    };
  });

  protected rotuloVaga(s: StatusVaga) {
    return STATUS_VAGA.find((x) => x.valor === s)!;
  }

  protected rotuloCandidato(s: StatusCandidato) {
    return STATUS_CANDIDATO.find((x) => x.valor === s)!;
  }

  /** O cargo escolhido na gaveta da vaga — a descrição aparece como dica, como na tela antiga. */
  protected cargoSelecionado(): CargoResponseDto | undefined {
    return this.cargos().find((c) => c.uuid === this.vagaForm.controls.cargoId.value);
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected abrirVaga(vaga: VagaResponseDto | null): void {
    if (!this.cargos().length) this.cargoService.listar().pipe(catchError(() => of([]))).subscribe((c) => this.cargos.set(c));
    if (!this.unidades().length) this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((u) => this.unidades.set(u));
    this.vagaForm.reset({
      unidadeId: vaga?.unidadeUuid ?? '',
      cargoId: vaga?.cargoUuid ?? '',
      quantidade: vaga ? String(vaga.quantidade) : '1',
      status: vaga?.status ?? 'ABERTA',
    });
    this.abrir({ tipo: 'vaga', vaga });
  }

  protected abrirCandidatos(vaga: VagaResponseDto): void {
    this.candidatoForm.reset({ nome: '', cpf: '', curriculoUrl: '', status: 'INSCRITO' });
    this.candidatos.set(null);
    this.abrir({ tipo: 'candidatos', vaga });
    this.carregarCandidatos(vaga.uuid);
  }

  private carregarCandidatos(vagaId: string): void {
    this.candidatoService.listarPorVaga(vagaId).pipe(catchError(() => of([]))).subscribe((c) => this.candidatos.set(c));
  }

  protected contagemCandidatos(s: StatusCandidato): number {
    return (this.candidatos() ?? []).filter((c) => c.status === s).length;
  }

  protected teclaNaLinha(event: KeyboardEvent, v: VagaResponseDto): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.abrirCandidatos(v);
    }
  }

  protected mascararCpf(event: Event): void {
    this.candidatoForm.controls.cpf.setValue(formatCpf((event.target as HTMLInputElement).value));
  }

  // ── Salvar ─────────────────────────────────────────────────────────────────────────────────────

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  private validar(erros: Record<string, string>): boolean {
    this.erros.set(erros);
    this.erroApi.set(null);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) {
      setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
      return false;
    }
    return true;
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (g?.tipo === 'vaga') this.salvarVaga(g.vaga);
    if (g?.tipo === 'candidatos') this.salvarCandidato(g.vaga);
  }

  private salvarVaga(vaga: VagaResponseDto | null): void {
    const v = this.vagaForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.unidadeId) erros['unidadeId'] = 'Escolha a unidade.';
    if (!v.cargoId) erros['cargoId'] = 'Escolha o cargo.';
    if (!(Number(v.quantidade) >= 1)) erros['quantidade'] = 'Informe ao menos uma posição.';
    if (!this.validar(erros)) return;
    const dto = { unidadeId: v.unidadeId, cargoId: v.cargoId, quantidade: Number(v.quantidade), status: v.status };
    const cargo = this.cargos().find((c) => c.uuid === v.cargoId)?.nome ?? '';
    this.submitting.set(true);
    (vaga ? this.vagaService.atualizar(vaga.uuid, dto) : this.vagaService.criar(dto)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.mostrarToast(vaga ? 'Vaga atualizada' : 'Vaga aberta', `${cargo} · ${dto.quantidade} posição(ões)`);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.falhou(e),
    });
  }

  private salvarCandidato(vaga: VagaResponseDto): void {
    const v = this.candidatoForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nome'] = 'Informe o nome.';
    if (v.cpf.replace(/\D/g, '').length !== 11) erros['cpf'] = 'Informe o CPF (11 dígitos).';
    if (!this.validar(erros)) return;
    this.submitting.set(true);
    this.candidatoService
      .criar({ vagaId: vaga.uuid, nome: v.nome.trim(), cpf: v.cpf, curriculoUrl: v.curriculoUrl.trim() || undefined, status: v.status })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.mostrarToast('Candidato registrado', `${v.nome.trim()} · ${vaga.cargoNome}`);
          this.candidatoForm.reset({ nome: '', cpf: '', curriculoUrl: '', status: 'INSCRITO' });
          this.carregarCandidatos(vaga.uuid);
        },
        error: (e: HttpErrorResponse) => this.falhou(e),
      });
  }

  private falhou(e: HttpErrorResponse): void {
    this.submitting.set(false);
    this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}
