import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal, viewChild } from '@angular/core';
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
import { EdicaoMapa, Mapa, PinoMapa, PoligonoMapa } from '../../../shared/mapa/mapa';
import { emComum, foraDe, geometriaDoArquivo, hectares, relevante } from '../../../shared/mapa/geometria';
import { TIPOS_UNIDADE } from '../../../core/models/rede-unidades';
import { ImovelDto, TIPOS_IMOVEL } from '../../../core/models/cadastro-territorial';
import { ImoveisDaArea } from './imoveis-da-area';

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

/** O desenho em curso no mapa (ADR-0126). */
interface Desenho {
  tipo: 'area' | 'microarea';
  area: AreaNoDto;
  microarea: MicroareaNoDto | null;
}

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
 * pela área e o ACS de cada microárea como histórico. Layout de lista e detalhe de Equipamentos de Saúde. O mapa
 * (ADR-0126, F2) mostra a área, as microáreas (sem ACS em âmbar) e as unidades, e desenha os polígonos; o GeoJSON
 * colado ou importado (inclusive KML) continua nas gavetas. Os imóveis e as famílias da área (ADR-0127, F3a) ficam no
 * componente {@link ImoveisDaArea}; os imóveis com ponto aparecem no mapa.
 */
@Component({
  selector: 'app-territorio',
  imports: [ReactiveFormsModule, RouterLink, Drawer, Mapa, ImoveisDaArea],
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

  /** Mapa: a área sozinha ou com as vizinhas do território, a microárea em destaque e o desenho em curso. */
  protected readonly visaoMapa = signal<'area' | 'territorio'>('area');
  protected readonly microareaSelecionada = signal<string | null>(null);
  protected readonly desenhando = signal<Desenho | null>(null);
  protected readonly desenhoAtual = signal<string | null>(null);
  protected readonly erroDesenho = signal<string | null>(null);
  protected readonly salvandoDesenho = signal(false);

  /** Imóveis da área aberta (ADR-0127), para os pinos do mapa. */
  protected readonly imoveisDaArea = signal<ImovelDto[]>([]);
  private readonly cadastro = viewChild(ImoveisDaArea);

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

  protected carregar(): void {
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

  // ── Mapa ───────────────────────────────────────────────────────────────────────────────────────

  protected readonly poligonosDoMapa = computed<PoligonoMapa[]>(() => {
    const d = this.detalhe();
    if (!d) return [];
    const lista: PoligonoMapa[] = [];
    if (this.visaoMapa() === 'territorio') {
      const territorio = this.todosTerritorios().find((t) => t.uuid === d.area.territorioId);
      for (const a of territorio?.areas ?? []) {
        if (a.uuid !== d.area.uuid && a.geometria) {
          lista.push({ id: a.uuid, geometria: a.geometria, rotulo: a.codigo, descricao: `Área ${a.codigo}`, tipo: 'vizinha' });
        }
      }
    }
    if (d.area.geometria) lista.push({ id: d.area.uuid, geometria: d.area.geometria, descricao: `Área ${d.area.codigo}`, tipo: 'area' });
    for (const m of d.area.microareas) {
      if (!m.geometria) continue;
      lista.push({
        id: m.uuid, geometria: m.geometria, rotulo: m.codigo, tipo: m.acs ? 'microarea' : 'sem-acs',
        descricao: `Microárea ${m.codigo}, ${m.acs ? 'ACS ' + m.acs.nome : 'sem ACS'}`,
      });
    }
    return lista;
  });

  protected readonly pinosDoMapa = computed<PinoMapa[]>(() => {
    const unidadeDaEquipe = this.detalhe()?.area.equipe?.unidadeId;
    const imoveis: PinoMapa[] = this.imoveisDaArea()
      .filter((i) => i.latitude != null && i.longitude != null && i.ativo)
      .map((i) => ({
        id: 'imovel:' + i.uuid, lat: i.latitude!, lng: i.longitude!, sigla: TIPOS_IMOVEL[i.tipo].sigla, classe: 'unit-pin t-imovel',
        descricao: `${TIPOS_IMOVEL[i.tipo].rotulo}: ${i.enderecoTexto}`,
      }));
    return imoveis.concat(this.unidades()
      .filter((u) => u.latitude != null && u.longitude != null)
      .map((u) => ({
        id: 'unidade:' + u.uuid, lat: u.latitude!, lng: u.longitude!, sigla: TIPOS_UNIDADE[u.tipo]?.sigla ?? 'U',
        classe: `unit-pin ${TIPOS_UNIDADE[u.tipo]?.familia ?? ''}`, descricao: u.nome, destaque: u.uuid === unidadeDaEquipe,
      })));
  });

  /** Áreas para o destino da mudança de família. */
  protected readonly rotulosDasAreas = computed(() =>
    this.todosTerritorios().flatMap((t) => t.areas.map((a) => ({ uuid: a.uuid, rotulo: `Área ${a.codigo} · ${a.nome} (${t.nome})` }))));

  /** A geometria de partida do desenho em curso. */
  protected readonly edicaoDoMapa = computed<EdicaoMapa | null>(() => {
    const g = this.desenhando();
    if (!g) return null;
    return g.tipo === 'area'
      ? { id: g.area.uuid, geometria: g.area.geometria ?? null, nome: `área ${g.area.codigo}` }
      : { id: g.microarea!.uuid, geometria: g.microarea!.geometria ?? null, nome: `microárea ${g.microarea!.codigo}` };
  });

  /** Avisos enquanto se desenha (o backend decide; ADR-0125). */
  protected readonly avisosDoDesenho = computed<string[]>(() => {
    const g = this.desenhando();
    const atual = this.desenhoAtual();
    if (!g || !atual) return [];
    const avisos: string[] = [];
    if (g.tipo === 'microarea') {
      if (g.area.geometria && relevante(foraDe(atual, g.area.geometria))) avisos.push(`Parte do desenho sai do contorno da área ${g.area.codigo}.`);
      for (const m of g.area.microareas) {
        if (m.uuid !== g.microarea!.uuid && m.geometria && relevante(emComum(atual, m.geometria))) avisos.push(`Sobrepõe a microárea ${m.codigo}.`);
      }
    } else {
      for (const m of g.area.microareas) {
        if (m.geometria && relevante(foraDe(m.geometria, atual))) avisos.push(`A microárea ${m.codigo} ficaria fora da área.`);
      }
    }
    return avisos;
  });

  protected readonly hectaresDoDesenho = computed(() => hectares(this.desenhoAtual()));

  protected noMapa(id: string): void {
    if (id.startsWith('unidade:')) return;
    if (id.startsWith('imovel:')) {
      this.cadastro()?.abrirImovel(id.substring('imovel:'.length));
      return;
    }
    this.microareaSelecionada.set(id);
    setTimeout(() => document.getElementById('mi-' + id)?.scrollIntoView({ block: 'nearest', behavior: 'smooth' }));
  }

  protected desenharArea(area: AreaNoDto): void {
    this.iniciarDesenho({ tipo: 'area', area, microarea: null }, area.geometria ?? null);
  }

  protected desenharMicroarea(area: AreaNoDto, m: MicroareaNoDto): void {
    this.microareaSelecionada.set(m.uuid);
    this.iniciarDesenho({ tipo: 'microarea', area, microarea: m }, m.geometria ?? null);
  }

  private iniciarDesenho(d: Desenho, geometria: string | null): void {
    this.desenhoAtual.set(geometria);
    this.erroDesenho.set(null);
    this.desenhando.set(d);
    setTimeout(() => document.getElementById('mapa-territorio')?.scrollIntoView({ block: 'nearest', behavior: 'smooth' }));
  }

  protected cancelarDesenho(): void {
    this.desenhando.set(null);
    this.desenhoAtual.set(null);
    this.erroDesenho.set(null);
  }

  protected salvarDesenho(): void {
    const g = this.desenhando();
    if (!g || this.salvandoDesenho()) return;
    const geometria = this.desenhoAtual();
    const req: Observable<AreaDetalheDto> = g.tipo === 'area'
      ? this.service.salvarArea(g.area.uuid, { territorioId: g.area.territorioId, codigo: g.area.codigo, codigoCnes: g.area.codigoCnes ?? null, nome: g.area.nome, geometria })
      : this.service.salvarMicroarea(g.microarea!.uuid, {
        areaId: g.area.uuid, codigo: g.microarea!.codigo, nome: g.microarea!.nome ?? null,
        populacaoReferencia: g.microarea!.populacaoReferencia ?? null, geometria,
      });
    this.salvandoDesenho.set(true);
    this.erroDesenho.set(null);
    req.subscribe({
      next: (d) => {
        this.salvandoDesenho.set(false);
        this.desenhando.set(null);
        this.desenhoAtual.set(null);
        this.detalhe.set(d);
        this.toast.set({ titulo: 'Desenho salvo', detalhe: g.tipo === 'area' ? `Área ${g.area.codigo}` : `Microárea ${g.microarea!.codigo}` });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.salvandoDesenho.set(false);
        this.erroDesenho.set((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar o desenho.');
      },
    });
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
    if (uuid !== this.selecionadaId()) {
      this.cancelarDesenho();
      this.microareaSelecionada.set(null);
      this.imoveisDaArea.set([]);
    }
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

  /** GeoJSON passa como veio; KML vira GeoJSON (ADR-0126). */
  protected importarArquivo(event: Event): void {
    const arquivo = (event.target as HTMLInputElement).files?.[0];
    if (!arquivo) return;
    arquivo.text()
      .then((t) => geometriaDoArquivo(arquivo.name, t))
      .then((g) => {
        this.form.controls.geometria.setValue(g);
        this.erros.update((e) => ({ ...e, geometria: '' }));
      })
      .catch((e: Error) => this.erros.update((x) => ({ ...x, geometria: e.message || 'Não foi possível ler o arquivo.' })));
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
