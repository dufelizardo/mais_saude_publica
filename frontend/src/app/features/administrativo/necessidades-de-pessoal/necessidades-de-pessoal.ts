import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { CargoResponseDto } from '../../../core/models/cargo';
import { NecessidadeDePessoalResponseDto } from '../../../core/models/necessidade-de-pessoal';
import { ErrorResponseDto, SuccessResponseDto } from '../../../core/models/profissional';
import { SetorResponseDto } from '../../../core/models/setor';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { StatusVaga, VagaResponseDto } from '../../../core/models/vaga';
import { AuthService } from '../../../core/services/auth';
import { CargoService } from '../../../core/services/cargo';
import { NecessidadeDePessoalService } from '../../../core/services/necessidade-de-pessoal';
import { SetorService } from '../../../core/services/setor';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { VagaService } from '../../../core/services/vaga';
import { Drawer } from '../../../shared/drawer/drawer';
import { dataBr } from '../../../shared/formato';

type Gaveta =
  | { tipo: 'necessidade'; item: NecessidadeDePessoalResponseDto | null }
  | { tipo: 'vincular'; item: NecessidadeDePessoalResponseDto };

const STATUS_VAGA: Record<StatusVaga, string> = {
  ABERTA: 'Aberta',
  EM_ANDAMENTO: 'Em andamento',
  FECHADA: 'Fechada',
  CANCELADA: 'Cancelada',
};

/**
 * Necessidades de pessoal (ADR-0074): o pedido operacional da unidade ("preciso de 2 técnicos de enfermagem, 40h"),
 * que o RH atende abrindo e vinculando uma vaga. Mesmo layout das telas recentes: indicadores, filtros, tabela e
 * gavetas no lugar dos modais.
 */
@Component({
  selector: 'app-necessidades-de-pessoal',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './necessidades-de-pessoal.html',
  styleUrl: './necessidades-de-pessoal.css',
})
export class NecessidadesDePessoal {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly necessidadeService = inject(NecessidadeDePessoalService);
  private readonly unidadeSaudeService = inject(UnidadeSaudeService);
  private readonly setorService = inject(SetorService);
  private readonly cargoService = inject(CargoService);
  private readonly vagaService = inject(VagaService);

  protected readonly dataBr = dataBr;

  protected readonly necessidades = signal<NecessidadeDePessoalResponseDto[]>([]);
  protected readonly unidades = signal<UnidadeSaudeResponseDto[]>([]);
  protected readonly setores = signal<SetorResponseDto[]>([]);
  protected readonly cargos = signal<CargoResponseDto[]>([]);
  protected readonly vagas = signal<VagaResponseDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly busca = signal('');
  protected readonly filtroUnidade = signal('');
  protected readonly soSemVaga = signal(false);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly form = this.fb.nonNullable.group({
    unidadeId: [''],
    setorId: [''],
    cargoId: [''],
    quantidade: [1],
    jornadaSemanalHoras: [40],
    competenciasNecessarias: [''],
    justificativa: [''],
  });
  protected readonly vincularForm = this.fb.nonNullable.group({ vagaId: [''] });

  private readonly unidadeDoForm = toSignal(this.form.controls.unidadeId.valueChanges, { initialValue: '' });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    // Trocar a unidade descarta um setor que era da unidade anterior.
    this.form.controls.unidadeId.valueChanges.subscribe((unidade) => {
      const setor = this.setores().find((s) => s.uuid === this.form.controls.setorId.value);
      if (setor && setor.unidadeUuid !== unidade) this.form.controls.setorId.setValue('');
    });
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    forkJoin({
      necessidades: this.necessidadeService.listar().pipe(catchError(() => of([]))),
      unidades: this.unidadeSaudeService.listar().pipe(catchError(() => of([]))),
      setores: this.setorService.listar().pipe(catchError(() => of([]))),
      cargos: this.cargoService.listar().pipe(catchError(() => of([]))),
      vagas: this.vagaService.listar().pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.necessidades.set(r.necessidades);
      this.unidades.set(r.unidades);
      this.setores.set(r.setores);
      this.cargos.set(r.cargos);
      this.vagas.set(r.vagas);
      this.carregando.set(false);
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected readonly podeGerenciar = computed(() => this.pode('ADMINISTRATIVO.GERENCIAR', 'RH.GERENCIAR'));

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────────

  protected readonly vagaPorUuid = computed(() => new Map(this.vagas().map((v) => [v.uuid, v])));

  protected readonly unidadesComNecessidade = computed(() => {
    const uuids = new Set(this.necessidades().map((n) => n.unidadeUuid));
    return this.unidades().filter((u) => uuids.has(u.uuid));
  });

  protected readonly filtradas = computed(() => {
    const q = this.busca().trim().toLowerCase();
    const unidade = this.filtroUnidade();
    return this.necessidades()
      .filter((n) => !unidade || n.unidadeUuid === unidade)
      .filter((n) => !this.soSemVaga() || !n.vagaAssociadaUuid)
      .filter((n) => !q || `${n.cargoNome} ${n.unidadeNome} ${n.setorNome ?? ''} ${n.competenciasNecessarias ?? ''}`.toLowerCase().includes(q))
      .sort((a, b) => Number(!!a.vagaAssociadaUuid) - Number(!!b.vagaAssociadaUuid) || b.dataRegistro.localeCompare(a.dataRegistro));
  });

  protected readonly resumo = computed(() => {
    const lista = this.necessidades();
    const semVaga = lista.filter((n) => !n.vagaAssociadaUuid);
    return {
      total: lista.length,
      unidades: new Set(lista.map((n) => n.unidadeUuid)).size,
      posicoes: lista.reduce((s, n) => s + n.quantidade, 0),
      horas: lista.reduce((s, n) => s + n.quantidade * n.jornadaSemanalHoras, 0),
      semVaga: semVaga.length,
      posicoesSemVaga: semVaga.reduce((s, n) => s + n.quantidade, 0),
      comVaga: lista.length - semVaga.length,
    };
  });

  /** Setores da unidade escolhida no formulário. */
  protected readonly setoresDaUnidade = computed(() => {
    const unidade = this.unidadeDoForm();
    return this.setores().filter((s) => s.unidadeUuid === unidade && (s.ativo || s.uuid === this.form.controls.setorId.value));
  });

  /** Na gaveta de vínculo: primeiro as vagas da mesma unidade e cargo, depois as demais. */
  protected readonly vagasParaVincular = computed(() => {
    const g = this.gaveta();
    if (g?.tipo !== 'vincular') return { sugeridas: [], outras: [] };
    const n = g.item;
    const combina = (v: VagaResponseDto) => v.unidadeUuid === n.unidadeUuid && v.cargoUuid === n.cargoUuid;
    return { sugeridas: this.vagas().filter(combina), outras: this.vagas().filter((v) => !combina(v)) };
  });

  protected rotuloStatusVaga(s: StatusVaga): string {
    return STATUS_VAGA[s] ?? s;
  }

  protected limparFiltros(): void {
    this.busca.set('');
    this.filtroUnidade.set('');
    this.soSemVaga.set(false);
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

  protected abrirNecessidade(n: NecessidadeDePessoalResponseDto | null): void {
    this.form.reset({
      unidadeId: n?.unidadeUuid ?? this.filtroUnidade(),
      setorId: n?.setorUuid ?? '',
      cargoId: n?.cargoUuid ?? '',
      quantidade: n?.quantidade ?? 1,
      jornadaSemanalHoras: n?.jornadaSemanalHoras ?? 40,
      competenciasNecessarias: n?.competenciasNecessarias ?? '',
      justificativa: n?.justificativa ?? '',
    });
    this.abrir({ tipo: 'necessidade', item: n });
  }

  protected abrirVincular(n: NecessidadeDePessoalResponseDto): void {
    this.vincularForm.reset({ vagaId: '' });
    this.abrir({ tipo: 'vincular', item: n });
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
    if (!g) return;
    if (g.tipo === 'vincular') {
      const vagaId = this.vincularForm.getRawValue().vagaId;
      if (!this.validar(vagaId ? {} : { vagaId: 'Escolha a vaga.' })) return;
      this.enviar(this.necessidadeService.vincularVaga(g.item.uuid, vagaId), 'Vaga vinculada', `${g.item.cargoNome} · ${g.item.unidadeNome}`);
      return;
    }
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.unidadeId) erros['unidadeId'] = 'Escolha a unidade.';
    if (!v.cargoId) erros['cargoId'] = 'Escolha o cargo.';
    if (!(v.quantidade >= 1)) erros['quantidade'] = 'Informe ao menos 1.';
    if (!(v.jornadaSemanalHoras >= 1)) erros['jornadaSemanalHoras'] = 'Informe a jornada semanal.';
    if (!this.validar(erros)) return;
    const dto = {
      unidadeId: v.unidadeId,
      setorId: v.setorId || undefined,
      cargoId: v.cargoId,
      quantidade: v.quantidade,
      jornadaSemanalHoras: v.jornadaSemanalHoras,
      competenciasNecessarias: v.competenciasNecessarias.trim() || undefined,
      justificativa: v.justificativa.trim() || undefined,
    };
    const cargo = this.cargos().find((c) => c.uuid === v.cargoId)?.nome ?? '';
    this.enviar(
      g.item ? this.necessidadeService.atualizar(g.item.uuid, dto) : this.necessidadeService.criar(dto),
      g.item ? 'Necessidade atualizada' : 'Necessidade registrada',
      `${dto.quantidade} × ${cargo}`,
    );
  }

  private enviar(req: Observable<SuccessResponseDto | void>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: () => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }
}
