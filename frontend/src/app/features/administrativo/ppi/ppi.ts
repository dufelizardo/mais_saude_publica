import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { CotaPpiDto, OfertaAssistencialDto, PainelPpiDto } from '../../../core/models/rede';
import { ProcedimentoReguladoResponseDto } from '../../../core/models/regulacao';
import { AuthService } from '../../../core/services/auth';
import { RedeService } from '../../../core/services/rede';
import { RegulacaoService } from '../../../core/services/regulacao';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';

type Gaveta =
  | { tipo: 'oferta'; item: OfertaAssistencialDto | null }
  | { tipo: 'cota'; oferta: OfertaAssistencialDto; item: CotaPpiDto | null };

function mesAtual(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}

function mesAnterior(m: string): string {
  const [a, mm] = m.split('-').map(Number);
  return mm === 1 ? `${a - 1}-12` : `${a}-${String(mm - 1).padStart(2, '0')}`;
}

/**
 * Rede e pactuação (ADR-0179): a oferta de cada executante por procedimento e as cotas da PPI que a repartem entre unidades e esferas, com o
 * usado e o saldo. A Regulação consome a cota na autorização.
 */
@Component({
  selector: 'app-ppi',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './ppi.html',
})
export class Ppi {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly service = inject(RedeService);
  private readonly regulacaoService = inject(RegulacaoService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly competencia = signal(mesAtual());
  protected readonly painel = signal<PainelPpiDto | null>(null);
  protected readonly procedimentos = signal<ProcedimentoReguladoResponseDto[]>([]);
  protected readonly unidades = signal<{ uuid: string; nome: string }[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly ofertaForm = this.fb.nonNullable.group({ executanteId: [''], procedimentoId: [''], quantidade: [''], observacao: [''] });
  protected readonly cotaForm = this.fb.nonNullable.group({ beneficiariaId: [''], quantidade: [''] });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.regulacaoService.listarProcedimentos().pipe(catchError(() => of([]))).subscribe((p) => this.procedimentos.set(p.filter((x) => x.ativo)));
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) =>
      this.unidades.set(l.map((u) => ({ uuid: u.uuid, nome: u.nome })).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))));
    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.service.painel(this.competencia()).pipe(catchError(() => of(null))).subscribe((p) => {
      this.painel.set(p);
      this.carregando.set(false);
    });
  }

  protected mudarCompetencia(m: string): void {
    if (!m) return;
    this.competencia.set(m);
    this.carregar();
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected rotuloMes(m: string): string {
    const [a, mm] = m.split('-');
    return `${mm}/${a}`;
  }

  // ── Gavetas ──

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected abrirOferta(o: OfertaAssistencialDto | null): void {
    this.ofertaForm.reset({ executanteId: o?.executanteId ?? '', procedimentoId: o?.procedimentoId ?? '', quantidade: o ? String(o.quantidade) : '',
      observacao: o?.observacao ?? '' });
    this.abrir({ tipo: 'oferta', item: o });
  }

  protected abrirCota(oferta: OfertaAssistencialDto, c: CotaPpiDto | null): void {
    this.cotaForm.reset({ beneficiariaId: c?.beneficiariaId ?? '', quantidade: c ? String(c.quantidade) : '' });
    this.abrir({ tipo: 'cota', oferta, item: c });
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  private validar(erros: Record<string, string>): boolean {
    this.erros.set(erros);
    this.erroApi.set(null);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) setTimeout(() => document.getElementById('f-' + primeiro)?.focus());
    return !primeiro;
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    const erros: Record<string, string> = {};
    if (g.tipo === 'oferta') {
      const v = this.ofertaForm.getRawValue();
      const qtd = Number(v.quantidade);
      if (!g.item && !v.executanteId) erros['executanteOferta'] = 'Escolha a executante.';
      if (!g.item && !v.procedimentoId) erros['procedimentoOferta'] = 'Escolha o procedimento.';
      if (v.quantidade === '' || !Number.isInteger(qtd) || qtd < 0) erros['quantidadeOferta'] = 'Informe as vagas do mês.';
      if (!this.validar(erros)) return;
      this.enviar(g.item ? this.service.ajustarOferta(g.item.uuid, qtd, v.observacao.trim() || null)
        : this.service.criarOferta({ executanteId: v.executanteId, procedimentoId: v.procedimentoId, competencia: this.competencia(), quantidade: qtd,
          observacao: v.observacao.trim() || null }), g.item ? 'Oferta ajustada' : 'Oferta cadastrada', (o) => `${o.procedimentoNome} · ${o.executanteNome}`);
    } else {
      const v = this.cotaForm.getRawValue();
      const qtd = Number(v.quantidade);
      if (!g.item && !v.beneficiariaId) erros['beneficiariaCota'] = 'Escolha a unidade ou a esfera.';
      if (v.quantidade === '' || !Number.isInteger(qtd) || qtd <= 0) erros['quantidadeCota'] = 'Informe a cota.';
      else if (qtd - (g.item?.quantidade ?? 0) > g.oferta.semPactuar) erros['quantidadeCota'] = `Restam ${g.oferta.semPactuar} vaga(s) sem pactuar.`;
      if (!this.validar(erros)) return;
      this.enviar(g.item ? this.service.ajustarCota(g.item.uuid, qtd) : this.service.criarCota(g.oferta.uuid, v.beneficiariaId, qtd),
        g.item ? 'Cota ajustada' : 'Cota pactuada', (o) => `${o.procedimentoNome} · ${o.pactuado} de ${o.quantidade} pactuada(s)`);
    }
  }

  protected removerCota(c: CotaPpiDto): void {
    this.enviar(this.service.removerCota(c.uuid), 'Cota removida', () => c.beneficiariaNome);
  }

  protected copiarMesAnterior(): void {
    this.submitting.set(true);
    this.service.copiar(mesAnterior(this.competencia()), this.competencia()).subscribe({
      next: (c) => {
        this.submitting.set(false);
        this.mostrar('PPI copiada', `${c.ofertasCopiadas} oferta(s) e ${c.cotasCopiadas} cota(s) de ${this.rotuloMes(c.origem)}`
          + (c.ofertasJaExistentes ? `; ${c.ofertasJaExistentes} já existia(m)` : ''));
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.mostrar('Não foi possível', (e.error as ErrorResponseDto | undefined)?.message ?? 'Tente novamente.');
      },
    });
  }

  private mostrar(titulo: string, detalhe: string): void {
    this.toast.set({ titulo, detalhe });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
  }

  private enviar<T>(req: Observable<T>, titulo: string, detalhe: (r: T) => string): void {
    this.submitting.set(true);
    req.subscribe({
      next: (r) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.mostrar(titulo, detalhe(r));
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        const m = (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.';
        if (this.gaveta()) this.erroApi.set(m);
        else this.mostrar('Não foi possível', m);
      },
    });
  }
}
