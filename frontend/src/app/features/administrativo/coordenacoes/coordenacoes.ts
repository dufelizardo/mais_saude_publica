import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { AreaGestaoDto, PapelTitularidade, TEMAS_GESTAO, TIPOS_AREA, TemaGestao, TipoAreaGestao, UnidadeDaAreaDto } from '../../../core/models/gestao';
import { ProducaoAssistencialDto, ProgramasNaRedeDto, VacinacaoNaRedeDto, VigilanciaNaRedeDto } from '../../../core/models/producao';
import { ErrorResponseDto, ProfissionalResponseDto } from '../../../core/models/profissional';
import { AuthService } from '../../../core/services/auth';
import { GestaoService } from '../../../core/services/gestao';
import { ProducaoService } from '../../../core/services/producao';
import { ProfissionalService } from '../../../core/services/profissional';
import { Drawer } from '../../../shared/drawer/drawer';
import { hojeIso } from '../../../shared/formato';

type Gaveta = { tipo: 'area'; item: AreaGestaoDto | null } | { tipo: 'designar'; area: AreaGestaoDto };

interface Indicadores {
  producao: ProducaoAssistencialDto | null;
  vacinacao: VacinacaoNaRedeDto | null;
  programas: ProgramasNaRedeDto | null;
  vigilancia: VigilanciaNaRedeDto | null;
}

/**
 * Coordenações e gerência (ADR-0175): o organograma da secretaria, quem responde por cada área, como histórico, e os indicadores da regional,
 * que vêm dos painéis da Sala de Situação somando as unidades abaixo dela.
 */
@Component({
  selector: 'app-coordenacoes',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './coordenacoes.html',
})
export class Coordenacoes {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly service = inject(GestaoService);
  private readonly producaoService = inject(ProducaoService);
  private readonly profissionalService = inject(ProfissionalService);

  protected readonly tipos = TIPOS_AREA;
  protected readonly listaTipos = Object.entries(TIPOS_AREA) as [TipoAreaGestao, string][];
  protected readonly temas = TEMAS_GESTAO;
  protected readonly listaTemas = Object.entries(TEMAS_GESTAO) as [TemaGestao, string][];
  protected readonly situacoes: Record<string, string> = { EM_OPERACAO: 'Em operação', EM_MANUTENCAO: 'Em manutenção', EM_OBRA: 'Em obra', INOPERANTE: 'Inoperante' };

  protected readonly areas = signal<AreaGestaoDto[]>([]);
  protected readonly regionais = signal<UnidadeDaAreaDto[]>([]);
  protected readonly profissionais = signal<ProfissionalResponseDto[]>([]);
  protected readonly carregando = signal(true);
  protected readonly selecionada = signal<AreaGestaoDto | null>(null);
  protected readonly indicadores = signal<Indicadores | null | undefined>(null);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly areaForm = this.fb.nonNullable.group({
    tipo: ['GERENCIA' as TipoAreaGestao],
    tema: ['ATENCAO_BASICA' as TemaGestao],
    nome: [''],
    sigla: [''],
    descricao: [''],
    superiorId: [''],
    regionalId: [''],
    ativa: [true],
  });
  protected readonly designarForm = this.fb.nonNullable.group({ matricula: [''], papel: ['TITULAR' as PapelTitularidade], inicio: [hojeIso()], ato: [''] });

  protected readonly gerencias = computed(() => this.areas().filter((a) => a.tipo === 'GERENCIA'));
  protected readonly semGerencia = computed(() => this.areas().filter((a) => a.tipo === 'COORDENACAO' && !a.superiorId));
  protected readonly resumo = computed(() => {
    const a = this.areas().filter((x) => x.ativa);
    return {
      gerencias: a.filter((x) => x.tipo === 'GERENCIA').length,
      coordenacoes: a.filter((x) => x.tipo === 'COORDENACAO').length,
      regionais: a.filter((x) => x.tipo === 'COORDENACAO' && x.regionalId).length,
      supervisoes: a.filter((x) => x.tipo === 'SUPERVISAO').length,
      comTitular: a.filter((x) => x.titular).length,
      total: a.length,
    };
  });

  /** O tipo escolhido na gaveta decide que superior e que regional cabem. */
  protected readonly tipoNaGaveta = signal<TipoAreaGestao>('GERENCIA');
  protected readonly superioresPossiveis = computed(() => {
    const tipo = this.tipoNaGaveta();
    const g = this.gaveta();
    const atual = g?.tipo === 'area' ? g.item?.uuid : null;
    if (tipo === 'GERENCIA') return [];
    return this.areas().filter((a) => a.ativa && a.uuid !== atual && (tipo === 'COORDENACAO' ? a.tipo === 'GERENCIA' : a.tipo !== 'SUPERVISAO'));
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.carregar();
  }

  private carregar(): void {
    forkJoin({
      areas: this.service.areas().pipe(catchError(() => of([]))),
      regionais: this.service.regionais().pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.areas.set(r.areas);
      this.regionais.set(r.regionais);
      this.carregando.set(false);
      const s = this.selecionada();
      if (s) this.selecionar(s.uuid);
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected abaixoDe(id: string, tipo?: TipoAreaGestao): AreaGestaoDto[] {
    return this.areas().filter((a) => a.superiorId === id && (!tipo || a.tipo === tipo));
  }

  protected iniciais(nome?: string | null): string {
    return (nome ?? '?').split(' ').filter((p) => p.length > 2).slice(0, 2).map((p) => p[0]).join('').toUpperCase() || '?';
  }

  protected dataBr(iso?: string | null): string {
    return iso ? iso.slice(0, 10).split('-').reverse().join('/') : '';
  }

  protected numero(v?: number | null): string {
    return v == null ? '—' : v.toLocaleString('pt-BR');
  }

  /** Abre o detalhe e, com regional, os indicadores do mês corrente somados nela. */
  protected selecionar(uuid: string): void {
    this.service.detalhe(uuid).subscribe({
      next: (a) => {
        this.selecionada.set(a);
        if (!a.regionalId) {
          this.indicadores.set(null);
          return;
        }
        this.indicadores.set(undefined);
        const filtro = { unidadeId: a.regionalId };
        forkJoin({
          producao: this.producaoService.producao(filtro).pipe(catchError(() => of(null))),
          vacinacao: this.producaoService.vacinacaoNaRede(filtro).pipe(catchError(() => of(null))),
          programas: this.producaoService.programasNaRede(filtro).pipe(catchError(() => of(null))),
          vigilancia: this.producaoService.vigilanciaNaRede(filtro).pipe(catchError(() => of(null))),
        }).subscribe((i) => this.indicadores.set(i));
      },
      error: () => this.selecionada.set(null),
    });
  }

  protected metas(p: ProgramasNaRedeDto): { atingidas: number; total: number } {
    let atingidas = 0;
    let total = 0;
    for (const x of p.programas) {
      atingidas += x.metasAtingidas;
      total += x.metasAtingidas + x.metasEmRisco + x.metasAbaixo;
    }
    return { atingidas, total };
  }

  // ── Gavetas ──

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected abrirArea(item: AreaGestaoDto | null, tipo: TipoAreaGestao = 'GERENCIA', superiorId = ''): void {
    this.areaForm.reset({
      tipo: item?.tipo ?? tipo,
      tema: item?.tema ?? 'ATENCAO_BASICA',
      nome: item?.nome ?? '',
      sigla: item?.sigla ?? '',
      descricao: item?.descricao ?? '',
      superiorId: item?.superiorId ?? superiorId,
      regionalId: item?.tipo === 'COORDENACAO' ? item?.regionalId ?? '' : '',
      ativa: item?.ativa ?? true,
    });
    this.tipoNaGaveta.set(item?.tipo ?? tipo);
    this.abrir({ tipo: 'area', item });
  }

  protected mudarTipo(tipo: TipoAreaGestao): void {
    this.tipoNaGaveta.set(tipo);
    if (tipo === 'GERENCIA') this.areaForm.patchValue({ superiorId: '', regionalId: '' });
    if (tipo === 'SUPERVISAO') this.areaForm.patchValue({ regionalId: '' });
  }

  protected abrirDesignar(area: AreaGestaoDto, papel: PapelTitularidade = 'TITULAR'): void {
    if (!this.profissionais().length) this.profissionalService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.profissionais.set(l.filter((p) => p.ativo !== false)));
    this.designarForm.reset({ matricula: '', papel, inicio: hojeIso(), ato: '' });
    this.abrir({ tipo: 'designar', area });
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
    if (g.tipo === 'area') {
      const v = this.areaForm.getRawValue();
      const erros: Record<string, string> = {};
      if (!v.nome.trim()) erros['nomeArea'] = 'Informe o nome.';
      if (v.tipo === 'SUPERVISAO' && !v.superiorId) erros['superiorArea'] = 'A supervisão fica sob uma coordenação ou gerência.';
      if (!this.validar(erros)) return;
      const dto = {
        tipo: v.tipo,
        tema: v.tema,
        nome: v.nome.trim(),
        sigla: v.sigla.trim() || null,
        descricao: v.descricao.trim() || null,
        superiorId: v.superiorId || null,
        regionalId: v.tipo === 'COORDENACAO' ? v.regionalId || null : null,
        ativa: v.ativa,
      };
      this.enviar(g.item ? this.service.atualizar(g.item.uuid, dto) : this.service.criar(dto), g.item ? 'Área atualizada' : 'Área criada');
    } else {
      const v = this.designarForm.getRawValue();
      const erros: Record<string, string> = {};
      if (!v.matricula) erros['profissionalArea'] = 'Escolha o profissional.';
      if (!v.inicio) erros['inicioDesignacao'] = 'Informe o início.';
      if (!this.validar(erros)) return;
      this.enviar(this.service.designar(g.area.uuid, { matriculaProfissional: v.matricula, papel: v.papel, inicio: v.inicio, ato: v.ato.trim() || null }),
        v.papel === 'TITULAR' ? 'Titular designado' : 'Substituto designado');
    }
  }

  protected encerrar(titularidadeId: string): void {
    this.enviar(this.service.encerrar(titularidadeId, hojeIso()), 'Designação encerrada');
  }

  private enviar(req: Observable<AreaGestaoDto>, titulo: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: (a) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe: a.nome });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        this.selecionada.set(a);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        const m = (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.';
        if (this.gaveta()) this.erroApi.set(m);
        else this.toast.set({ titulo: 'Não foi possível', detalhe: m });
      },
    });
  }
}
