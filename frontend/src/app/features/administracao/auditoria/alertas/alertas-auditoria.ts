import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { catchError, of } from 'rxjs';
import {
  AlertaAuditoriaResponseDto,
  ResumoAlertasAuditoriaDto,
  SEVERIDADES,
  STATUS_ALERTA,
  SeveridadeAlertaAuditoria,
  StatusAlertaAuditoria,
  TIPOS_ALERTA,
  TipoAlertaAuditoria,
} from '../../../../core/models/alerta-auditoria';
import { ErrorResponseDto } from '../../../../core/models/profissional';
import { AlertaAuditoriaService } from '../../../../core/services/alerta-auditoria';
import { Drawer } from '../../../../shared/drawer/drawer';
import { formatCpf } from '../../../../shared/format-mask';

/** Filtro de eventos que a aba Alertas pede à aba Eventos ("Ver eventos"). */
export interface FiltroEventosDoAlerta {
  usuarioCpf: string;
  desde: string;
  ate: string;
}

/**
 * Aba Alertas da tela Auditoria (ADR-0097): indicadores, lista com filtros e a gaveta do alerta, com "Ver eventos" e a
 * análise procedente ou improcedente com parecer. O backend é a ADR-0096.
 */
@Component({
  selector: 'app-alertas-auditoria',
  imports: [Drawer, ReactiveFormsModule],
  templateUrl: './alertas-auditoria.html',
})
export class AlertasAuditoria {
  private readonly fb = inject(FormBuilder);
  private readonly alertaService = inject(AlertaAuditoriaService);

  readonly verEventos = output<FiltroEventosDoAlerta>();

  protected readonly tipos = TIPOS_ALERTA;
  protected readonly listaTipos = Object.keys(TIPOS_ALERTA) as TipoAlertaAuditoria[];
  protected readonly severidades = SEVERIDADES;
  protected readonly statusAlerta = STATUS_ALERTA;
  protected readonly formatCpf = formatCpf;

  protected readonly alertas = signal<AlertaAuditoriaResponseDto[]>([]);
  protected readonly resumo = signal<ResumoAlertasAuditoriaDto | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly verificando = signal(false);

  protected readonly status = signal<StatusAlertaAuditoria | ''>('ABERTO');
  protected readonly tipo = signal<TipoAlertaAuditoria | ''>('');
  protected readonly severidade = signal<SeveridadeAlertaAuditoria | ''>('');

  protected readonly selecionado = signal<AlertaAuditoriaResponseDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly analiseForm = this.fb.nonNullable.group({
    conclusao: ['' as '' | 'PROCEDENTE' | 'IMPROCEDENTE'],
    parecer: [''],
  });

  constructor() {
    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.alertaService.listar({ status: this.status(), tipo: this.tipo(), severidade: this.severidade() }).subscribe({
      next: (a) => {
        this.alertas.set(a);
        this.carregando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.carregando.set(false);
        this.alertas.set([]);
        this.erro.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível carregar os alertas.');
      },
    });
    this.alertaService.resumo().pipe(catchError(() => of(null))).subscribe((r) => {
      this.resumo.set(r);
      this.alertaService.abertos.set(r?.abertos ?? null);
    });
  }

  protected verificarAgora(): void {
    this.verificando.set(true);
    this.alertaService.detectar().subscribe({
      next: (r) => {
        this.verificando.set(false);
        this.mostrarToast('Verificação concluída', r.novos ? `${r.novos} alerta(s) novo(s)` : 'Nenhum alerta novo');
        this.carregar();
      },
      error: () => {
        this.verificando.set(false);
        this.erro.set('Não foi possível rodar a verificação agora.');
      },
    });
  }

  protected abrir(a: AlertaAuditoriaResponseDto): void {
    this.erroApi.set(null);
    this.erros.set({});
    this.analiseForm.reset({ conclusao: '', parecer: '' });
    this.selecionado.set(a);
    // Relê o detalhe: a leitura do alerta entra na trilha (ADR-0096).
    this.alertaService.buscar(a.uuid).pipe(catchError(() => of(a))).subscribe((d) => this.selecionado.set(d));
  }

  protected fechar(): void {
    this.selecionado.set(null);
  }

  protected analisar(): void {
    const a = this.selecionado();
    if (!a || this.submitting()) return;
    const v = this.analiseForm.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.conclusao) erros['conclusao'] = 'Escolha procedente ou improcedente.';
    if (v.parecer.trim().length < 10) erros['parecer'] = 'Escreva o parecer com pelo menos 10 caracteres.';
    this.erros.set(erros);
    const campos = Object.keys(erros);
    if (campos.length) {
      setTimeout(() => document.getElementById('f-' + campos[0])?.focus());
      return;
    }
    this.submitting.set(true);
    this.erroApi.set(null);
    this.alertaService.analisar(a.uuid, { conclusao: v.conclusao as 'PROCEDENTE' | 'IMPROCEDENTE', parecer: v.parecer.trim() }).subscribe({
      next: (r) => {
        this.submitting.set(false);
        this.selecionado.set(null);
        this.mostrarToast('Alerta analisado', `${this.tipos[r.tipo].rotulo} · ${this.statusAlerta[r.status].rotulo}`);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível registrar a análise.');
      },
    });
  }

  /** "Ver eventos": a trilha de quem foi alertado, nos dias do alerta. */
  protected eventosDo(a: AlertaAuditoriaResponseDto): void {
    if (!a.usuarioCpf) return;
    this.selecionado.set(null);
    this.verEventos.emit({ usuarioCpf: a.usuarioCpf, desde: this.dia(a.primeiroEventoEm), ate: this.dia(a.ultimoEventoEm) });
  }

  protected quem(a: AlertaAuditoriaResponseDto): string {
    if (a.usuarioNome) return a.usuarioNome;
    return a.usuarioCpf ? formatCpf(a.usuarioCpf) : a.sujeito;
  }

  protected instante(iso: string | null | undefined): string {
    if (!iso) return '—';
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  protected periodo(a: AlertaAuditoriaResponseDto): string {
    const de = this.instante(a.primeiroEventoEm);
    const ate = this.instante(a.ultimoEventoEm);
    return de === ate ? de : `${de} a ${ate.slice(de.slice(0, 10) === ate.slice(0, 10) ? 11 : 0)}`;
  }

  protected erroCampo(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  /** Data local (yyyy-MM-dd) de um instante, para o filtro de dias da trilha. */
  private dia(iso: string): string {
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
  }

  private mostrarToast(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }
}
