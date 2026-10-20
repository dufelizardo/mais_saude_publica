import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { EquipeResumoDto, TIPOS_EQUIPE } from '../../../core/models/equipe';
import { ErrorResponseDto } from '../../../core/models/profissional';
import {
  AreaDetalheDto,
  AreaNoDto,
  ArvoreTerritorialDto,
  FINALIDADES,
  FinalidadeTerritorio,
  MicroareaNoDto,
  POPULACAO_REFERENCIA_CNES,
  SegmentoDto,
  TIPOS_SEGMENTO,
  TerritorioNoDto,
  TipoSegmentoTerritorial,
  UnidadeNoMapaDto,
} from '../../../core/models/territorio';
import { AuthService } from '../../../core/services/auth';
import { EquipeService } from '../../../core/services/equipe';
import { TerritorioService } from '../../../core/services/territorio';
import { Drawer } from '../../../shared/drawer/drawer';

/** Valores do formulário único das gavetas. */
const VAZIO = {
  codigo: '',
  nome: '',
  tipo: 'DISTRITO_SANITARIO',
  unidadeGestaoId: '',
  segmentoId: '',
  finalidade: 'ATENCAO_PRIMARIA',
  ativo: true,
  territorioId: '',
  codigoCnes: '',
  populacao: '',
  geometria: '',
  equipeId: '',
  membroId: '',
  data: '',
  motivo: '',
};

type Gaveta =
  | { tipo: 'segmento'; item: SegmentoDto | null }
  | { tipo: 'territorio'; item: TerritorioNoDto | null }
  | { tipo: 'area'; item: AreaNoDto | null }
  | { tipo: 'microarea'; item: MicroareaNoDto | null; area: AreaNoDto }
  | { tipo: 'equipe'; area: AreaNoDto }
  | { tipo: 'acs'; microarea: MicroareaNoDto }
  | { tipo: 'encerrar'; microarea: MicroareaNoDto };

/**
 * Território (ADR-0125, fatia F1 da ADR-0108): segmento → território → área → microárea, com a equipe responsável
 * pela área e o ACS de cada microárea como histórico. O mapa (Leaflet) vem na F2; aqui a geometria entra como GeoJSON
 * colado ou importado de arquivo. Layout de lista e detalhe de Equipamentos de Saúde.
 */
@Component({
  selector: 'app-territorio',
  imports: [ReactiveFormsModule, RouterLink, Drawer],
  templateUrl: './territorio.html',
})
export class Territorio {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly service = inject(TerritorioService);
  private readonly equipeService = inject(EquipeService);
  private readonly route = inject(ActivatedRoute);

  protected readonly tiposSegmento = TIPOS_SEGMENTO;
  protected readonly finalidades = FINALIDADES;
  protected readonly tiposEquipe = TIPOS_EQUIPE;
  protected readonly referencia = POPULACAO_REFERENCIA_CNES;
  protected readonly listaTipos = Object.keys(TIPOS_SEGMENTO) as TipoSegmentoTerritorial[];
  protected readonly listaFinalidades = Object.keys(FINALIDADES) as FinalidadeTerritorio[];

  protected readonly arvore = signal<ArvoreTerritorialDto | null>(null);
  protected readonly unidades = signal<UnidadeNoMapaDto[]>([]);
  protected readonly equipes = signal<EquipeResumoDto[]>([]);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly busca = signal('');
  protected readonly selecionadaId = signal<string | null>(null);
  protected readonly detalhe = signal<AreaDetalheDto | null>(null);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;
  /** Vindo de Equipes (?equipe=): abre a área da equipe. */
  private equipeDaUrl: string | null = null;

  protected readonly form = this.fb.nonNullable.group({ ...VAZIO });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    const q = this.route.snapshot.queryParamMap;
    if (q.get('area')) this.selecionadaId.set(q.get('area'));
    this.equipeDaUrl = q.get('equipe');
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    forkJoin({
      arvore: this.service.arvore().pipe(catchError(() => of(null))),
      unidades: this.service.unidades().pipe(catchError(() => of([]))),
      equipes: this.equipeService.listar().pipe(map((r) => r.lista), catchError(() => of([]))),
    }).subscribe((r) => {
      this.arvore.set(r.arvore);
      this.unidades.set(r.unidades);
      this.equipes.set(r.equipes);
      this.carregando.set(false);
      const daEquipe = this.equipeDaUrl ? this.todasAreas().find((a) => a.equipe?.uuid === this.equipeDaUrl)?.uuid : undefined;
      this.equipeDaUrl = null;
      const id = daEquipe ?? this.selecionadaId() ?? this.todasAreas()[0]?.uuid ?? null;
      if (id) this.selecionar(id);
    });
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected readonly podeEditar = computed(() => this.pode('TERRITORIO.GERENCIAR'));

  // ── Derivados ──────────────────────────────────────────────────────────────────────────────────

  protected readonly segmentos = computed(() => this.arvore()?.segmentos ?? []);
  protected readonly indicadores = computed(() => this.arvore()?.indicadores);
  protected readonly todosTerritorios = computed(() => this.segmentos().flatMap((s) => s.territorios));
  private readonly todasAreas = computed(() => this.todosTerritorios().flatMap((t) => t.areas));

  /** A árvore filtrada pela busca (código ou nome da área, da microárea, da equipe ou do ACS). */
  protected readonly filtrados = computed(() => {
    const q = this.busca().trim().toLowerCase();
    if (!q) return this.segmentos();
    const casa = (a: AreaNoDto) =>
      `${a.codigo} ${a.nome} ${a.equipe?.nome ?? ''} ${a.microareas.map((m) => `${m.codigo} ${m.acs?.nome ?? ''}`).join(' ')}`.toLowerCase().includes(q);
    return this.segmentos()
      .map((s) => ({ ...s, territorios: s.territorios.map((t) => ({ ...t, areas: t.areas.filter(casa) })).filter((t) => t.areas.length) }))
      .filter((s) => s.territorios.length);
  });

  protected semAcs(a: AreaNoDto): number {
    return a.microareas.filter((m) => !m.acs).length;
  }

  protected acimaDaReferencia(m: MicroareaNoDto): boolean {
    return (m.populacaoReferencia ?? 0) > POPULACAO_REFERENCIA_CNES;
  }

  /** O marcador da lista cabe três caracteres. */
  protected marca(codigo: string): string {
    return codigo.length <= 3 ? codigo : codigo.slice(0, 3);
  }

  protected nomes(lista: { nome: string }[]): string {
    return lista.map((a) => a.nome).join(', ');
  }

  protected data(iso?: string | null): string {
    if (!iso) return '—';
    const [a, m, d] = iso.split('-');
    return `${d}/${m}/${a}`;
  }

  // ── Seleção ────────────────────────────────────────────────────────────────────────────────────

  protected selecionar(uuid: string): void {
    this.selecionadaId.set(uuid);
    this.service.area(uuid).pipe(catchError(() => of(null))).subscribe((d) => this.detalhe.set(d));
  }

  protected teclaNaLinha(event: KeyboardEvent, uuid: string): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.selecionar(uuid);
    }
  }

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'segmento': return g.item ? 'Editar segmento territorial' : 'Novo segmento territorial';
      case 'territorio': return g.item ? 'Editar território' : 'Novo território';
      case 'area': return g.item ? 'Editar área' : 'Nova área';
      case 'microarea': return g.item ? `Editar microárea ${g.item.codigo}` : `Nova microárea na área ${g.area.codigo}`;
      case 'equipe': return g.area.equipe ? `Trocar a equipe da área ${g.area.codigo}` : `Equipe responsável pela área ${g.area.codigo}`;
      case 'acs': return g.microarea.acs ? `Trocar o ACS da microárea ${g.microarea.codigo}` : `ACS da microárea ${g.microarea.codigo}`;
      case 'encerrar': return `Deixar a microárea ${g.microarea.codigo} sem ACS`;
    }
  }

  private abrir(g: Gaveta, valores: Partial<typeof VAZIO>): void {
    this.form.reset({ ...VAZIO, ...valores });
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected abrirSegmento(s: SegmentoDto | null): void {
    this.abrir({ tipo: 'segmento', item: s }, s
      ? { codigo: s.codigo, nome: s.nome, tipo: s.tipo, unidadeGestaoId: s.unidadeGestaoId ?? '', geometria: s.geometria ?? '' }
      : {});
  }

  protected abrirTerritorio(t: TerritorioNoDto | null, segmentoId?: string): void {
    this.abrir({ tipo: 'territorio', item: t }, t
      ? { segmentoId: t.segmentoId, nome: t.nome, finalidade: t.finalidade, ativo: t.ativo }
      : { segmentoId: segmentoId ?? this.segmentos()[0]?.uuid ?? '' });
  }

  protected abrirArea(a: AreaNoDto | null, territorioId?: string): void {
    this.abrir({ tipo: 'area', item: a }, a
      ? { territorioId: a.territorioId, codigo: a.codigo, codigoCnes: a.codigoCnes ?? '', nome: a.nome, geometria: a.geometria ?? '' }
      : { territorioId: territorioId ?? this.todosTerritorios().find((t) => t.ativo)?.uuid ?? '' });
  }

  protected abrirMicroarea(area: AreaNoDto, m: MicroareaNoDto | null): void {
    this.abrir({ tipo: 'microarea', item: m, area }, m
      ? { codigo: m.codigo, nome: m.nome ?? '', populacao: m.populacaoReferencia?.toString() ?? '', geometria: m.geometria ?? '' }
      : {});
  }

  protected abrirEquipe(area: AreaNoDto): void {
    this.abrir({ tipo: 'equipe', area }, {});
  }

  protected abrirAcs(m: MicroareaNoDto): void {
    this.abrir({ tipo: 'acs', microarea: m }, { membroId: this.detalhe()?.acsSemMicroarea[0]?.membroId ?? '' });
  }

  protected abrirEncerrar(m: MicroareaNoDto): void {
    this.abrir({ tipo: 'encerrar', microarea: m }, {});
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  /** Equipes que podem assumir a área: ativas, sem outra área vigente e diferentes da atual. */
  protected equipesParaArea(area: AreaNoDto): EquipeResumoDto[] {
    return this.equipes().filter((e) => e.ativa && !e.microareasPeloTerritorio && e.uuid !== area.equipe?.uuid)
      .sort((a, b) => a.nome.localeCompare(b.nome));
  }

  protected importarArquivo(event: Event): void {
    const arquivo = (event.target as HTMLInputElement).files?.[0];
    if (!arquivo) return;
    arquivo.text().then((t) => this.form.controls.geometria.setValue(t.trim()));
  }

  // ── Salvar ─────────────────────────────────────────────────────────────────────────────────────

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
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    const geometria = v.geometria.trim() || null;
    if (geometria) {
      try { JSON.parse(geometria); } catch { erros['geometria'] = 'O GeoJSON não é um JSON válido.'; }
    }
    switch (g.tipo) {
      case 'segmento': {
        if (!v.codigo.trim()) erros['codigo'] = 'Informe o código.';
        if (!v.nome.trim()) erros['nome'] = 'Informe o nome.';
        if (!this.validar(erros)) return;
        const dto = { codigo: v.codigo.trim(), nome: v.nome.trim(), tipo: v.tipo as TipoSegmentoTerritorial, unidadeGestaoId: v.unidadeGestaoId || null, geometria };
        this.enviar(this.service.salvarSegmento(g.item?.uuid ?? null, dto), g.item ? 'Segmento atualizado' : 'Segmento cadastrado', dto.nome);
        return;
      }
      case 'territorio': {
        if (!v.segmentoId) erros['segmentoId'] = 'Escolha o segmento.';
        if (!v.nome.trim()) erros['nome'] = 'Informe o nome.';
        if (!this.validar(erros)) return;
        const dto = { segmentoId: v.segmentoId, nome: v.nome.trim(), finalidade: v.finalidade as FinalidadeTerritorio, ativo: v.ativo };
        this.enviar(this.service.salvarTerritorio(g.item?.uuid ?? null, dto), g.item ? 'Território atualizado' : 'Território cadastrado', dto.nome);
        return;
      }
      case 'area': {
        if (!v.territorioId) erros['territorioId'] = 'Escolha o território.';
        if (!v.codigo.trim()) erros['codigo'] = 'Informe o código.';
        if (!v.nome.trim()) erros['nome'] = 'Informe o nome.';
        if (v.codigoCnes.trim() && !/^\d{1,10}$/.test(v.codigoCnes.trim())) erros['codigoCnes'] = 'O código CNES tem até 10 dígitos.';
        if (!this.validar(erros)) return;
        const dto = { territorioId: v.territorioId, codigo: v.codigo.trim(), codigoCnes: v.codigoCnes.trim() || null, nome: v.nome.trim(), geometria };
        this.enviar(this.service.salvarArea(g.item?.uuid ?? null, dto), g.item ? 'Área atualizada' : 'Área cadastrada', `Área ${dto.codigo.toUpperCase()}`,
          (d) => this.selecionadaId.set(d.area.uuid));
        return;
      }
      case 'microarea': {
        if (!v.codigo.trim()) erros['codigo'] = 'Informe o código.';
        const populacao = v.populacao.toString().trim();
        if (populacao && !/^\d+$/.test(populacao)) erros['populacao'] = 'Informe um número inteiro.';
        if (!this.validar(erros)) return;
        const dto = { areaId: g.area.uuid, codigo: v.codigo.trim(), nome: v.nome.trim() || null, populacaoReferencia: populacao ? Number(populacao) : null, geometria };
        this.enviar(this.service.salvarMicroarea(g.item?.uuid ?? null, dto), g.item ? 'Microárea atualizada' : 'Microárea cadastrada',
          `Microárea ${dto.codigo.toUpperCase()}`);
        return;
      }
      case 'equipe': {
        if (!v.equipeId) erros['equipeId'] = 'Escolha a equipe.';
        if (g.area.equipe && !v.motivo.trim()) erros['motivo'] = 'Informe o motivo da troca.';
        if (!this.validar(erros)) return;
        const nome = this.equipes().find((e) => e.uuid === v.equipeId)?.nome ?? '';
        this.enviar(this.service.atribuirEquipe(g.area.uuid, v.equipeId, v.data, v.motivo.trim()), 'Equipe atribuída', `${nome} · área ${g.area.codigo}`);
        return;
      }
      case 'acs': {
        if (!v.membroId) erros['membroId'] = 'Escolha o ACS.';
        if (g.microarea.acs && !v.motivo.trim()) erros['motivo'] = 'Informe o motivo da troca.';
        if (!this.validar(erros)) return;
        const nome = this.detalhe()?.acsSemMicroarea.find((a) => a.membroId === v.membroId)?.nome ?? '';
        this.enviar(this.service.atribuirAcs(g.microarea.uuid, v.membroId, v.data, v.motivo.trim()), 'ACS atribuído', `${nome} · microárea ${g.microarea.codigo}`);
        return;
      }
      case 'encerrar': {
        if (!v.motivo.trim()) erros['motivo'] = 'Informe o motivo.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.encerrarAcs(g.microarea.uuid, v.motivo.trim(), v.data), 'Microárea sem ACS', `Microárea ${g.microarea.codigo}`);
        return;
      }
    }
  }

  private enviar<T>(req: Observable<T>, titulo: string, detalhe: string, depois?: (r: T) => void): void {
    this.submitting.set(true);
    req.subscribe({
      next: (r) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        depois?.(r);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        this.erroApi.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.');
      },
    });
  }
}
