import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import {
  ACOES_AUDITORIA,
  AcaoAuditoria,
  EventoAuditoriaResponseDto,
  PaginaAuditoriaResponseDto,
  ResultadoAuditoria,
  rotuloRecurso,
} from '../../../core/models/auditoria';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AuditoriaService } from '../../../core/services/auditoria';
import { AlertaAuditoriaService } from '../../../core/services/alerta-auditoria';
import { AlertasAuditoria, FiltroEventosDoAlerta } from './alertas/alertas-auditoria';
import { PacienteService } from '../../../core/services/paciente';
import { Drawer } from '../../../shared/drawer/drawer';
import { formatCpf } from '../../../shared/format-mask';

const TAMANHO = 20;

/**
 * Auditoria (ADR-0071): a trilha imutável de quem acessou e alterou o quê (ADR-0070), com filtros por pessoa,
 * paciente, ação, resultado e período. Aceita `?usuarioCpf=` e `?pacienteId=` para chegar já filtrada (links da
 * tela Usuários & Perfis e do atendimento). Só leitura: nada aqui altera a trilha.
 */
@Component({
  selector: 'app-auditoria',
  imports: [Drawer, AlertasAuditoria],
  templateUrl: './auditoria.html',
  styleUrl: './auditoria.css',
})
export class Auditoria {
  private readonly auditoriaService = inject(AuditoriaService);
  private readonly pacienteService = inject(PacienteService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly alertaService = inject(AlertaAuditoriaService);

  /** Abas da tela (ADR-0097): a trilha e os alertas. */
  protected readonly aba = signal<'eventos' | 'alertas'>('eventos');
  protected readonly alertasAbertos = this.alertaService.abertos;

  protected readonly formatCpf = formatCpf;
  protected readonly acoes = ACOES_AUDITORIA;
  protected readonly listaAcoes = Object.entries(ACOES_AUDITORIA) as [AcaoAuditoria, { rotulo: string }][];
  protected readonly rotuloRecurso = rotuloRecurso;

  protected readonly pagina = signal<PaginaAuditoriaResponseDto | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly exportando = signal(false);
  /** Prazo de guarda da trilha, em anos (0 = indefinido); null enquanto carrega (ADR-0082). */
  protected readonly retencaoAnos = signal<number | null>(null);
  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly selecionado = signal<EventoAuditoriaResponseDto | null>(null);

  protected readonly usuarioCpf = signal('');
  protected readonly pacienteId = signal('');
  protected readonly buscaPaciente = signal('');
  protected readonly acao = signal<AcaoAuditoria | ''>('');
  protected readonly resultado = signal<ResultadoAuditoria | ''>('');
  protected readonly desde = signal('');
  protected readonly ate = signal('');
  protected readonly numeroPagina = signal(0);

  protected readonly totalPaginas = computed(() => Math.max(1, Math.ceil((this.pagina()?.total ?? 0) / TAMANHO)));

  protected readonly faixa = computed(() => {
    const p = this.pagina();
    if (!p || !p.total) return { de: 0, ate: 0, total: 0 };
    return { de: p.pagina * TAMANHO + 1, ate: p.pagina * TAMANHO + p.itens.length, total: p.total };
  });

  protected readonly paginas = computed<(number | null)[]>(() => {
    const total = this.totalPaginas();
    const atual = this.numeroPagina() + 1;
    const lista: (number | null)[] = [];
    for (let p = 1; p <= total; p++) {
      if (p === 1 || p === total || Math.abs(p - atual) <= 1 || (atual <= 3 && p <= 4)) lista.push(p);
      else if (lista[lista.length - 1] !== null) lista.push(null);
    }
    return lista;
  });

  protected readonly pacienteFiltrado = computed(() => this.pacientes().find((p) => p.uuid === this.pacienteId()) ?? null);

  /** Sugestões da busca de paciente: até 8, por nome, CPF ou cartão SUS. */
  protected readonly sugestoes = computed(() => {
    const q = this.buscaPaciente().trim().toLowerCase();
    if (q.length < 2 || this.pacienteId()) return [];
    const digitos = q.replace(/\D/g, '');
    return this.pacientes()
      .filter((p) => p.nome.toLowerCase().includes(q) || (digitos.length >= 3 && ((p.cpf ?? '').includes(digitos) || (p.cartaoSus ?? '').includes(digitos))))
      .slice(0, 8);
  });

  constructor() {
    const params = this.route.snapshot.queryParamMap;
    this.usuarioCpf.set(formatCpf(params.get('usuarioCpf') ?? ''));
    this.pacienteId.set(params.get('pacienteId') ?? '');
    this.desde.set(params.get('desde') ?? '');
    this.ate.set(params.get('ate') ?? '');
    if (params.get('aba') === 'alertas') this.aba.set('alertas');
    this.alertaService.atualizarContador();
    this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
    this.auditoriaService.politica().pipe(catchError(() => of(null))).subscribe((p) => this.retencaoAnos.set(p?.retencaoAnos ?? null));
    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.auditoriaService
      .consultar({
        usuarioCpf: this.usuarioCpf().replace(/\D/g, ''),
        pacienteId: this.pacienteId(),
        acao: this.acao(),
        resultado: this.resultado(),
        desde: this.desde(),
        ate: this.ate(),
        pagina: this.numeroPagina(),
        tamanho: TAMANHO,
      })
      .subscribe({
        next: (p) => {
          this.pagina.set(p);
          this.carregando.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.carregando.set(false);
          this.pagina.set(null);
          this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível consultar a auditoria.');
        },
      });
  }

  /**
   * Baixa o filtro atual em CSV (ADR-0082). Com o filtro por paciente, é o relatório de acessos para atender o titular
   * dos dados. A exportação também entra na trilha.
   */
  protected exportar(): void {
    this.exportando.set(true);
    this.erro.set(null);
    this.auditoriaService
      .exportar({
        usuarioCpf: this.usuarioCpf().replace(/\D/g, ''),
        pacienteId: this.pacienteId(),
        acao: this.acao(),
        resultado: this.resultado(),
        desde: this.desde(),
        ate: this.ate(),
      })
      .subscribe({
        next: (r) => {
          this.exportando.set(false);
          const nome = /filename="([^"]+)"/.exec(r.headers.get('Content-Disposition') ?? '')?.[1] ?? 'auditoria.csv';
          const url = URL.createObjectURL(r.body ?? new Blob());
          const link = document.createElement('a');
          link.href = url;
          link.download = nome;
          link.click();
          URL.revokeObjectURL(url);
        },
        error: async (e: HttpErrorResponse) => {
          this.exportando.set(false);
          // O corpo do erro vem como Blob (a requisição pediu arquivo).
          let mensagem = 'Não foi possível exportar a auditoria.';
          try {
            mensagem = (JSON.parse(await (e.error as Blob).text()) as ErrorResponseDto).message ?? mensagem;
          } catch {
            /* mantém a mensagem padrão */
          }
          this.erro.set(mensagem);
        },
      });
  }

  /** Qualquer filtro novo volta para a primeira página. */
  protected filtrar(): void {
    this.numeroPagina.set(0);
    this.carregar();
  }

  protected mudarCpf(valor: string): void {
    this.usuarioCpf.set(formatCpf(valor));
    const digitos = valor.replace(/\D/g, '');
    if (digitos.length === 0 || digitos.length === 11) this.filtrar();
  }

  protected escolherPaciente(p: PacienteResponseDto): void {
    this.pacienteId.set(p.uuid);
    this.buscaPaciente.set('');
    this.filtrar();
  }

  protected limparPaciente(): void {
    this.pacienteId.set('');
    this.filtrar();
  }

  protected limparTudo(): void {
    this.usuarioCpf.set('');
    this.pacienteId.set('');
    this.buscaPaciente.set('');
    this.acao.set('');
    this.resultado.set('');
    this.desde.set('');
    this.ate.set('');
    this.router.navigate([], { queryParams: {} });
    this.filtrar();
  }

  protected irParaPagina(p: number): void {
    this.numeroPagina.set(Math.max(0, Math.min(p, this.totalPaginas() - 1)));
    this.carregar();
  }

  protected temFiltro(): boolean {
    return !!(this.usuarioCpf() || this.pacienteId() || this.acao() || this.resultado() || this.desde() || this.ate());
  }

  protected selecionarAba(aba: 'eventos' | 'alertas', focar = false): void {
    this.aba.set(aba);
    this.router.navigate([], { queryParams: { aba: aba === 'alertas' ? 'alertas' : null }, queryParamsHandling: 'merge', replaceUrl: true });
    if (focar) setTimeout(() => document.getElementById('tab-' + aba)?.focus());
  }

  protected navegarAbas(event: KeyboardEvent): void {
    if (event.key !== 'ArrowRight' && event.key !== 'ArrowLeft') return;
    event.preventDefault();
    this.selecionarAba(this.aba() === 'eventos' ? 'alertas' : 'eventos', true);
  }

  /** "Ver eventos" do alerta: a trilha de quem foi alertado, nos dias do alerta. */
  protected verEventosDoAlerta(f: FiltroEventosDoAlerta): void {
    this.usuarioCpf.set(formatCpf(f.usuarioCpf));
    this.pacienteId.set('');
    this.acao.set('');
    this.resultado.set('');
    this.desde.set(f.desde);
    this.ate.set(f.ate);
    this.selecionarAba('eventos');
    this.filtrar();
  }

  /** Da gaveta: vê tudo desta pessoa ou deste paciente. */
  protected filtrarPorPessoa(cpf: string): void {
    this.selecionado.set(null);
    this.usuarioCpf.set(formatCpf(cpf));
    this.filtrar();
  }

  protected filtrarPorPaciente(id: string): void {
    this.selecionado.set(null);
    this.pacienteId.set(id);
    this.filtrar();
  }

  protected teclaNaLinha(event: KeyboardEvent, e: EventoAuditoriaResponseDto): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.selecionado.set(e);
    }
  }

  protected quando(iso: string): string {
    const d = new Date(iso);
    const data = d.toLocaleDateString('pt-BR');
    const hora = d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    return `${data} · ${hora}`;
  }

  protected quem(e: EventoAuditoriaResponseDto): string {
    if (e.usuarioNome) return e.usuarioNome;
    if (e.usuarioCpf) return e.acao === 'LOGIN' ? 'CPF não cadastrado' : 'Usuário removido';
    return 'Sem login';
  }
}
