import { NgTemplateOutlet } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, catchError, of } from 'rxjs';
import {
  AGUA,
  AbastecimentoAgua,
  DestinoLixo,
  DomicilioDto,
  ESGOTO,
  EscoamentoSanitario,
  FaixaRendaFamiliar,
  FamiliaDto,
  ImovelDetalheDto,
  ImovelDto,
  LIXO,
  LOCALIZACOES,
  LocalizacaoDomicilio,
  MembroFamiliaDto,
  PARENTESCOS,
  Parentesco,
  RENDAS,
  SITUACOES_MORADIA,
  SituacaoMoradia,
  TIPOS_IMOVEL,
  TipoImovel,
} from '../../../core/models/cadastro-territorial';
import { PacienteResponseDto } from '../../../core/models/paciente';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AreaNoDto } from '../../../core/models/territorio';
import { PacienteService } from '../../../core/services/paciente';
import { TerritorioService } from '../../../core/services/territorio';
import { Drawer } from '../../../shared/drawer/drawer';
import { Mapa, PinoMapa, PoligonoMapa } from '../../../shared/mapa/mapa';

/** Valores do formulário único das gavetas do cadastro. */
const VAZIO = {
  microareaId: '',
  tipoImovel: 'DOMICILIO',
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  estado: '',
  latitude: '',
  longitude: '',
  ativo: true,
  identificacao: '',
  situacao: '',
  localizacao: '',
  comodos: '',
  agua: '',
  esgoto: '',
  lixo: '',
  energia: '',
  pacienteId: '',
  parentesco: 'FILHO',
  renda: '',
  data: '',
  motivo: '',
  membroId: '',
  destinoArea: '',
  destinoImovel: '',
  destinoDomicilio: '',
};

type Gaveta =
  | { tipo: 'imovel'; item: ImovelDto | null }
  | { tipo: 'domicilio'; imovel: ImovelDetalheDto; item: DomicilioDto | null }
  | { tipo: 'familia'; domicilio: DomicilioDto }
  | { tipo: 'membro'; familia: FamiliaDto }
  | { tipo: 'saida'; familia: FamiliaDto; membro: MembroFamiliaDto }
  | { tipo: 'responsavel'; familia: FamiliaDto }
  | { tipo: 'renda'; familia: FamiliaDto }
  | { tipo: 'mudanca'; familia: FamiliaDto };

/**
 * Imóveis e famílias da área (ADR-0127, F3a): a lista por microárea e logradouro, a ficha do imóvel com os domicílios, as
 * famílias e os membros, e as gavetas do cadastro. As pessoas são os pacientes que já existem. O endereço do cadastro do
 * paciente só muda pela ação "Usar o endereço do imóvel".
 */
@Component({
  selector: 'app-imoveis-da-area',
  imports: [ReactiveFormsModule, RouterLink, NgTemplateOutlet, Drawer, Mapa],
  templateUrl: './imoveis-da-area.html',
})
export class ImoveisDaArea {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(TerritorioService);
  private readonly pacienteService = inject(PacienteService);

  readonly area = input.required<AreaNoDto>();
  /** Áreas para o destino da mudança: id e rótulo. */
  readonly areas = input<{ uuid: string; rotulo: string }[]>([]);
  readonly podeCadastrar = input(false);
  /** Escopo territorial (ADR-0129): as microáreas alcançadas nesta área; nulo, sem restrição. */
  readonly microareasDoEscopo = input<string[] | null>(null);

  /** As microáreas que entram nos filtros e no cadastro: as do escopo, ou todas. */
  protected readonly microareasPermitidas = computed(() => {
    const escopo = this.microareasDoEscopo();
    return this.area().microareas.filter((m) => !escopo || escopo.includes(m.uuid));
  });

  /** A lista carregada, para os pinos do mapa da área. */
  readonly imoveisCarregados = output<ImovelDto[]>();
  /** Algo mudou (contagens da árvore). */
  readonly alterado = output<void>();

  protected readonly tiposImovel = TIPOS_IMOVEL;
  protected readonly listaTipos = Object.keys(TIPOS_IMOVEL) as TipoImovel[];
  protected readonly situacoes = SITUACOES_MORADIA;
  protected readonly listaSituacoes = Object.keys(SITUACOES_MORADIA) as SituacaoMoradia[];
  protected readonly localizacoes = LOCALIZACOES;
  protected readonly listaLocalizacoes = Object.keys(LOCALIZACOES) as LocalizacaoDomicilio[];
  protected readonly agua = AGUA;
  protected readonly listaAgua = Object.keys(AGUA) as AbastecimentoAgua[];
  protected readonly esgoto = ESGOTO;
  protected readonly listaEsgoto = Object.keys(ESGOTO) as EscoamentoSanitario[];
  protected readonly lixo = LIXO;
  protected readonly listaLixo = Object.keys(LIXO) as DestinoLixo[];
  protected readonly rendas = RENDAS;
  protected readonly listaRendas = Object.keys(RENDAS) as FaixaRendaFamiliar[];
  protected readonly parentescos = PARENTESCOS;
  protected readonly listaParentescos = (Object.keys(PARENTESCOS) as Parentesco[]).filter((p) => p !== 'RESPONSAVEL');

  /** `null`: sem acesso ao cadastro domiciliar. */
  protected readonly imoveis = signal<ImovelDto[] | null>([]);
  protected readonly carregando = signal(false);
  protected readonly filtroMicroarea = signal('');
  protected readonly aberto = signal<ImovelDetalheDto | null>(null);
  protected readonly mostrarAnteriores = signal(false);

  protected readonly pacientes = signal<PacienteResponseDto[]>([]);
  protected readonly buscaPaciente = signal('');
  protected readonly destinoImoveis = signal<ImovelDto[]>([]);
  protected readonly destinoDomicilios = signal<DomicilioDto[]>([]);

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly form = this.fb.nonNullable.group({ ...VAZIO });

  /** Só a troca de área recarrega (o pai recarrega o detalhe da mesma área depois de cada mudança). */
  private readonly areaId = computed(() => this.area().uuid);

  constructor() {
    effect(() => {
      const id = this.areaId();
      untracked(() => {
        this.aberto.set(null);
        const escopo = this.microareasDoEscopo();
        this.filtroMicroarea.set(escopo?.length === 1 ? escopo[0] : '');
        this.carregar(id);
      });
    });
  }

  private carregar(areaId = this.area().uuid): void {
    this.carregando.set(true);
    this.service.imoveis(areaId).subscribe({
      next: (l) => {
        this.imoveis.set(l);
        this.imoveisCarregados.emit(l);
        this.carregando.set(false);
      },
      error: () => {
        this.imoveis.set(null);
        this.imoveisCarregados.emit([]);
        this.carregando.set(false);
      },
    });
  }

  protected readonly filtrados = computed(() => {
    const f = this.filtroMicroarea();
    return (this.imoveis() ?? []).filter((i) => !f || i.microareaId === f);
  });

  /** Abre a ficha do imóvel (também pelo pino do mapa). */
  abrirImovel(uuid: string): void {
    this.service.imovel(uuid).pipe(catchError(() => of(null))).subscribe((d) => {
      this.aberto.set(d);
      this.mostrarAnteriores.set(false);
      setTimeout(() => document.getElementById('ficha-imovel')?.scrollIntoView({ block: 'nearest', behavior: 'smooth' }));
    });
  }

  protected fecharImovel(): void {
    this.aberto.set(null);
  }

  protected idade(nascimento?: string | null): string {
    if (!nascimento) return '';
    const n = new Date(nascimento + 'T00:00:00');
    const hoje = new Date();
    let anos = hoje.getFullYear() - n.getFullYear();
    if (hoje.getMonth() < n.getMonth() || (hoje.getMonth() === n.getMonth() && hoje.getDate() < n.getDate())) anos--;
    return `${anos} ano(s)`;
  }

  protected data(iso?: string | null): string {
    if (!iso) return '—';
    const [a, m, d] = iso.split('-');
    return `${d}/${m}/${a}`;
  }

  protected condicoes(d: DomicilioDto): string {
    return [
      d.situacao ? this.situacoes[d.situacao] : null,
      d.localizacao ? this.localizacoes[d.localizacao] : null,
      d.comodos ? `${d.comodos} cômodo(s)` : null,
      d.abastecimentoAgua ? 'água: ' + this.agua[d.abastecimentoAgua].toLowerCase() : null,
      d.escoamentoSanitario ? 'esgoto: ' + this.esgoto[d.escoamentoSanitario].toLowerCase() : null,
      d.destinoLixo ? 'lixo: ' + this.lixo[d.destinoLixo].toLowerCase() : null,
      d.energiaEletrica == null ? null : d.energiaEletrica ? 'com energia elétrica' : 'sem energia elétrica',
    ].filter((x) => !!x).join(' · ') || 'Condições não informadas';
  }

  // ── Mapa da gaveta do imóvel ──────────────────────────────────────────────────────────────────

  protected readonly poligonosDaArea = computed<PoligonoMapa[]>(() => {
    const a = this.area();
    const lista: PoligonoMapa[] = [];
    if (a.geometria) lista.push({ id: a.uuid, geometria: a.geometria, descricao: `Área ${a.codigo}`, tipo: 'area' });
    for (const m of a.microareas) {
      if (m.geometria) lista.push({ id: m.uuid, geometria: m.geometria, rotulo: m.codigo, descricao: `Microárea ${m.codigo}`, tipo: 'microarea' });
    }
    return lista;
  });

  protected readonly pontoDoForm = signal<{ lat: number; lng: number } | null>(null);

  protected readonly pinoDoForm = computed<PinoMapa[]>(() => {
    const p = this.pontoDoForm();
    return p ? [{ id: 'novo', lat: p.lat, lng: p.lng, sigla: 'D', classe: 'unit-pin t-caps', descricao: 'Ponto do imóvel' }] : [];
  });

  protected marcarPonto(p: { lat: number; lng: number }): void {
    this.form.patchValue({ latitude: String(p.lat), longitude: String(p.lng) });
    this.pontoDoForm.set(p);
  }

  protected mudarCoordenada(): void {
    const v = this.form.getRawValue();
    const lat = Number(v.latitude.replace(',', '.'));
    const lng = Number(v.longitude.replace(',', '.'));
    this.pontoDoForm.set(v.latitude.trim() && v.longitude.trim() && Number.isFinite(lat) && Number.isFinite(lng) ? { lat, lng } : null);
  }

  // ── Pacientes ─────────────────────────────────────────────────────────────────────────────────

  private carregarPacientes(): void {
    if (!this.pacientes().length) this.pacienteService.listar().pipe(catchError(() => of([]))).subscribe((p) => this.pacientes.set(p));
  }

  protected readonly pacientesEncontrados = computed(() => {
    const q = this.buscaPaciente().trim().toLowerCase();
    if (q.length < 2) return [];
    const digitos = q.replace(/\D/g, '');
    return this.pacientes()
      .filter((p) => p.ativo && (p.nome.toLowerCase().includes(q) || (digitos.length >= 3 && ((p.cpf ?? '').includes(digitos) || (p.cartaoSus ?? '').includes(digitos)))))
      .slice(0, 20);
  });

  // ── Gavetas ────────────────────────────────────────────────────────────────────────────────────

  protected tituloGaveta(g: Gaveta): string {
    switch (g.tipo) {
      case 'imovel': return g.item ? 'Editar imóvel' : 'Novo imóvel';
      case 'domicilio': return g.item ? 'Editar domicílio' : 'Novo domicílio';
      case 'familia': return 'Nova família';
      case 'membro': return `Incluir pessoa na família de ${g.familia.responsavelNome}`;
      case 'saida': return `Saída de ${g.membro.nome}`;
      case 'responsavel': return 'Trocar o responsável';
      case 'renda': return 'Renda familiar';
      case 'mudanca': return `Mudança da família de ${g.familia.responsavelNome}`;
    }
  }

  private abrir(g: Gaveta, valores: Partial<typeof VAZIO> = {}): void {
    this.form.reset({ ...VAZIO, ...valores });
    this.erros.set({});
    this.erroApi.set(null);
    this.buscaPaciente.set('');
    this.gaveta.set(g);
  }

  protected novoImovel(): void {
    this.pontoDoForm.set(null);
    this.abrir({ tipo: 'imovel', item: null }, { microareaId: this.filtroMicroarea() });
  }

  protected editarImovel(d: ImovelDetalheDto): void {
    const i = d.imovel;
    const e = i.endereco ?? {};
    this.pontoDoForm.set(i.latitude != null && i.longitude != null ? { lat: i.latitude, lng: i.longitude } : null);
    this.abrir({ tipo: 'imovel', item: i }, {
      microareaId: i.microareaId, tipoImovel: i.tipo, cep: e.cep ?? '', logradouro: e.logradouro ?? '', numero: e.numeroLogradouro ?? '',
      complemento: e.complemento ?? '', bairro: e.bairro ?? '', cidade: e.cidade ?? '', estado: e.estado ?? '',
      latitude: i.latitude?.toString() ?? '', longitude: i.longitude?.toString() ?? '', ativo: i.ativo,
    });
  }

  protected novoDomicilio(d: ImovelDetalheDto): void {
    this.abrir({ tipo: 'domicilio', imovel: d, item: null });
  }

  protected editarDomicilio(d: ImovelDetalheDto, dom: DomicilioDto): void {
    this.abrir({ tipo: 'domicilio', imovel: d, item: dom }, {
      identificacao: dom.identificacao ?? '', situacao: dom.situacao ?? '', localizacao: dom.localizacao ?? '', comodos: dom.comodos?.toString() ?? '',
      agua: dom.abastecimentoAgua ?? '', esgoto: dom.escoamentoSanitario ?? '', lixo: dom.destinoLixo ?? '',
      energia: dom.energiaEletrica == null ? '' : dom.energiaEletrica ? 'sim' : 'nao', ativo: dom.ativo,
    });
  }

  protected novaFamilia(dom: DomicilioDto): void {
    this.carregarPacientes();
    this.abrir({ tipo: 'familia', domicilio: dom });
  }

  protected incluirPessoa(f: FamiliaDto): void {
    this.carregarPacientes();
    this.abrir({ tipo: 'membro', familia: f });
  }

  protected saida(f: FamiliaDto, m: MembroFamiliaDto): void {
    this.abrir({ tipo: 'saida', familia: f, membro: m });
  }

  protected trocarResponsavel(f: FamiliaDto): void {
    this.abrir({ tipo: 'responsavel', familia: f }, { parentesco: 'PAI_OU_MAE' });
  }

  protected mudarRenda(f: FamiliaDto): void {
    this.abrir({ tipo: 'renda', familia: f }, { renda: f.rendaFamiliar ?? '' });
  }

  protected mudanca(f: FamiliaDto): void {
    this.destinoImoveis.set([]);
    this.destinoDomicilios.set([]);
    this.abrir({ tipo: 'mudanca', familia: f }, { destinoArea: this.area().uuid });
    this.escolherAreaDestino(this.area().uuid);
  }

  protected escolherAreaDestino(areaId: string): void {
    this.form.patchValue({ destinoArea: areaId, destinoImovel: '', destinoDomicilio: '' });
    this.destinoDomicilios.set([]);
    if (!areaId) return this.destinoImoveis.set([]);
    this.service.imoveis(areaId).pipe(catchError(() => of([]))).subscribe((l) => this.destinoImoveis.set(l.filter((i) => i.tipo === 'DOMICILIO' && i.ativo)));
  }

  protected escolherImovelDestino(imovelId: string): void {
    this.form.patchValue({ destinoImovel: imovelId, destinoDomicilio: '' });
    this.destinoDomicilios.set([]);
    if (!imovelId) return;
    this.service.imovel(imovelId).pipe(catchError(() => of(null))).subscribe((d) => {
      const lista = (d?.domicilios ?? []).filter((x) => x.ativo);
      this.destinoDomicilios.set(lista);
      if (lista.length === 1) this.form.patchValue({ destinoDomicilio: lista[0].uuid });
    });
  }

  protected outrosMembros(f: FamiliaDto): MembroFamiliaDto[] {
    return f.membros.filter((m) => m.pacienteId !== f.responsavelId);
  }

  protected fecharGaveta(): void {
    this.gaveta.set(null);
  }

  protected usarEnderecoDoImovel(m: MembroFamiliaDto): void {
    this.enviar(this.service.enderecoDoImovel(m.uuid), 'Endereço atualizado', m.nome);
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

  private static opcional<T extends string>(v: string): T | null {
    return (v || null) as T | null;
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    switch (g.tipo) {
      case 'imovel': {
        for (const [c, rotulo] of [['logradouro', 'o logradouro'], ['numero', 'o número'], ['bairro', 'o bairro'], ['cidade', 'a cidade'], ['estado', 'o estado'], ['cep', 'o CEP']]) {
          if (!(v as Record<string, unknown>)[c]?.toString().trim()) erros[c] = `Informe ${rotulo}.`;
        }
        const lat = v.latitude.trim() ? Number(v.latitude.replace(',', '.')) : null;
        const lng = v.longitude.trim() ? Number(v.longitude.replace(',', '.')) : null;
        if ((lat == null) !== (lng == null)) erros['latitude'] = 'Informe a latitude e a longitude juntas, ou nenhuma.';
        if (!v.microareaId && lat == null) erros['microareaId'] = 'Escolha a microárea ou marque o ponto no mapa.';
        if (!this.validar(erros)) return;
        const dto = {
          microareaId: v.microareaId || null, tipo: v.tipoImovel as TipoImovel, latitude: lat, longitude: lng, ativo: v.ativo,
          endereco: { cep: v.cep.trim(), logradouro: v.logradouro.trim(), numeroLogradouro: v.numero.trim(), complemento: v.complemento.trim() || undefined,
            bairro: v.bairro.trim(), cidade: v.cidade.trim(), estado: v.estado.trim() },
        };
        this.enviar(this.service.salvarImovel(g.item?.uuid ?? null, dto), g.item ? 'Imóvel atualizado' : 'Imóvel cadastrado', `${dto.endereco.logradouro}, ${dto.endereco.numeroLogradouro}`);
        return;
      }
      case 'domicilio': {
        if (v.comodos.toString().trim() && !/^\d+$/.test(v.comodos.toString().trim())) erros['comodos'] = 'Informe um número inteiro.';
        if (!this.validar(erros)) return;
        const dto = {
          imovelId: g.imovel.imovel.uuid, identificacao: v.identificacao.trim() || null, situacao: ImoveisDaArea.opcional<SituacaoMoradia>(v.situacao),
          localizacao: ImoveisDaArea.opcional<LocalizacaoDomicilio>(v.localizacao), comodos: v.comodos.toString().trim() ? Number(v.comodos) : null,
          abastecimentoAgua: ImoveisDaArea.opcional<AbastecimentoAgua>(v.agua), escoamentoSanitario: ImoveisDaArea.opcional<EscoamentoSanitario>(v.esgoto),
          destinoLixo: ImoveisDaArea.opcional<DestinoLixo>(v.lixo), energiaEletrica: v.energia === '' ? null : v.energia === 'sim', ativo: v.ativo,
        };
        this.enviar(this.service.salvarDomicilio(g.item?.uuid ?? null, dto), g.item ? 'Domicílio atualizado' : 'Domicílio cadastrado', dto.identificacao ?? g.imovel.imovel.enderecoTexto);
        return;
      }
      case 'familia': {
        if (!v.pacienteId) erros['pacienteId'] = 'Escolha o responsável.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.criarFamilia({ domicilioId: g.domicilio.uuid, responsavelId: v.pacienteId, rendaFamiliar: ImoveisDaArea.opcional<FaixaRendaFamiliar>(v.renda), inicio: v.data || null }),
          'Família cadastrada', this.pacientes().find((p) => p.uuid === v.pacienteId)?.nome ?? '');
        return;
      }
      case 'membro': {
        if (!v.pacienteId) erros['pacienteId'] = 'Escolha a pessoa.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.incluirMembro(g.familia.uuid, v.pacienteId, v.parentesco as Parentesco, v.data), 'Pessoa incluída',
          this.pacientes().find((p) => p.uuid === v.pacienteId)?.nome ?? '');
        return;
      }
      case 'saida': {
        if (!v.motivo.trim()) erros['motivo'] = 'Informe o motivo.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.saidaMembro(g.membro.uuid, v.motivo.trim(), v.data), 'Saída registrada', g.membro.nome);
        return;
      }
      case 'responsavel': {
        if (!v.membroId) erros['membroId'] = 'Escolha o novo responsável.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.trocarResponsavel(g.familia.uuid, v.membroId, v.parentesco as Parentesco), 'Responsável trocado',
          g.familia.membros.find((m) => m.uuid === v.membroId)?.nome ?? '');
        return;
      }
      case 'renda': {
        this.enviar(this.service.renda(g.familia.uuid, ImoveisDaArea.opcional<FaixaRendaFamiliar>(v.renda)), 'Renda atualizada', g.familia.responsavelNome);
        return;
      }
      case 'mudanca': {
        if (!v.destinoDomicilio) erros['destinoDomicilio'] = 'Escolha o domicílio de destino.';
        if (!this.validar(erros)) return;
        this.enviar(this.service.mudanca(g.familia.uuid, v.destinoDomicilio, v.motivo.trim(), v.data), 'Mudança registrada', `Família de ${g.familia.responsavelNome}`);
        return;
      }
    }
  }

  private enviar(req: Observable<ImovelDetalheDto>, titulo: string, detalhe: string): void {
    this.submitting.set(true);
    req.subscribe({
      next: (d) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.aberto.set(d);
        this.toast.set({ titulo, detalhe });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        this.carregar();
        this.alterado.emit();
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        const msg = (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.';
        if (this.gaveta()) this.erroApi.set(msg);
        else this.toast.set({ titulo: 'Não foi possível salvar', detalhe: msg });
      },
    });
  }
}
