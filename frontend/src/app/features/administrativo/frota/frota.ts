import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, of } from 'rxjs';
import { AcessoDaInterface } from '../../../core/models/auth';
import { ErrorResponseDto, ProfissionalResponseDto } from '../../../core/models/profissional';
import {
  COMPOSICAO_MINIMA,
  FUNCOES_TRIPULANTE,
  FuncaoTripulante,
  PlantaoDto,
  SITUACOES_VEICULO,
  SituacaoVeiculo,
  TIPOS_VEICULO,
  TipoVeiculo,
  VeiculoDto,
} from '../../../core/models/transporte';
import { AuthService } from '../../../core/services/auth';
import { ProfissionalService } from '../../../core/services/profissional';
import { TransporteService } from '../../../core/services/transporte';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';
import { Drawer } from '../../../shared/drawer/drawer';
import { proximaAba } from '../../../shared/formato';

type Aba = 'frota' | 'plantoes';
type Gaveta =
  | { tipo: 'veiculo'; item: VeiculoDto | null }
  | { tipo: 'situacao'; item: VeiculoDto }
  | { tipo: 'plantao'; item: VeiculoDto | null };

/** "2026-11-08T10:00" no fuso do navegador, para o input datetime-local. */
function localIso(d: Date): string {
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
}

/**
 * Frota e transporte (ADR-0178): os veículos com a situação e o histórico, e o plantão de cada viatura com a tripulação. O SAMU põe a
 * viatura em serviço e a devolve; aqui se registra a manutenção.
 */
@Component({
  selector: 'app-frota',
  imports: [ReactiveFormsModule, Drawer],
  templateUrl: './frota.html',
})
export class Frota {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly service = inject(TransporteService);
  private readonly unidadeService = inject(UnidadeSaudeService);
  private readonly profissionalService = inject(ProfissionalService);

  protected readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'frota', rotulo: 'Frota' },
    { id: 'plantoes', rotulo: 'Plantões' },
  ];
  protected readonly tipos = TIPOS_VEICULO;
  protected readonly listaTipos = Object.entries(TIPOS_VEICULO) as [TipoVeiculo, string][];
  protected readonly situacoes = SITUACOES_VEICULO;
  protected readonly funcoes = FUNCOES_TRIPULANTE;
  protected readonly listaFuncoes = Object.entries(FUNCOES_TRIPULANTE) as [FuncaoTripulante, string][];
  protected readonly composicao = COMPOSICAO_MINIMA;

  protected readonly aba = signal<Aba>('frota');
  protected readonly veiculos = signal<VeiculoDto[]>([]);
  protected readonly plantoes = signal<PlantaoDto[]>([]);
  protected readonly unidades = signal<{ uuid: string; nome: string }[]>([]);
  protected readonly profissionais = signal<ProfissionalResponseDto[]>([]);
  protected readonly selecionado = signal<VeiculoDto | null>(null);
  protected readonly carregando = signal(true);
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly toast = signal<{ titulo: string; detalhe: string } | null>(null);
  private toastTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly veiculoForm = this.fb.nonNullable.group({
    placa: [''], prefixo: [''], tipo: ['USB' as TipoVeiculo], modelo: [''], ano: [''], lugares: [''], maca: [true], cadeiraDeRodas: [false],
    quilometragem: [''], unidadeId: [''], observacao: [''],
  });
  protected readonly situacaoForm = this.fb.nonNullable.group({ situacao: ['MANUTENCAO' as SituacaoVeiculo], motivo: [''], quilometragem: [''] });
  protected readonly plantaoForm = this.fb.nonNullable.group({
    veiculoId: [''], inicio: [''], fim: [''], observacao: [''],
    tripulacao: this.fb.array([this.tripulante('CONDUTOR'), this.tripulante('TECNICO_ENFERMAGEM')]),
  });
  protected readonly veiculoDoPlantao = signal<TipoVeiculo | null>(null);

  protected readonly resumo = computed(() => {
    const v = this.veiculos();
    return {
      disponiveis: v.filter((x) => x.situacao === 'DISPONIVEL').length,
      emServico: v.filter((x) => x.situacao === 'EM_SERVICO').length,
      manutencao: v.filter((x) => x.situacao === 'MANUTENCAO').length,
      comPlantao: v.filter((x) => x.plantaoVigente).length,
      total: v.length,
    };
  });

  constructor() {
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) =>
      this.unidades.set(l.map((u) => ({ uuid: u.uuid, nome: u.nome })).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))));
    this.carregar();
  }

  private tripulante(funcao: FuncaoTripulante | '' = '') {
    return this.fb.nonNullable.group({ matricula: [''], funcao: [funcao as FuncaoTripulante | ''] });
  }

  protected get tripulacao(): FormArray {
    return this.plantaoForm.controls.tripulacao;
  }

  private carregar(): void {
    this.service.veiculos().pipe(catchError(() => of([]))).subscribe((v) => {
      this.veiculos.set(v);
      this.carregando.set(false);
    });
    this.service.plantoes().pipe(catchError(() => of([]))).subscribe((p) => this.plantoes.set(p));
  }

  protected pode(...permissoes: string[]): boolean {
    const a = this.acesso();
    return !a.restrito || permissoes.some((p) => a.permissoes.has(p));
  }

  protected navegarAbas(event: KeyboardEvent): void {
    const proxima = proximaAba(event, this.abas.map((a) => a.id), this.aba());
    if (proxima) this.aba.set(proxima);
  }

  protected selecionar(uuid: string): void {
    this.service.veiculo(uuid).subscribe({ next: (v) => this.selecionado.set(v), error: () => this.selecionado.set(null) });
  }

  /** Vigente ou futuro: ainda dá para encerrar ou cancelar. */
  protected emAberto(p: PlantaoDto): boolean {
    return new Date(p.fim).getTime() > Date.now();
  }

  protected hora(iso?: string | null): string {
    return iso ? new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }) : '';
  }

  // ── Gavetas ──

  private abrir(g: Gaveta): void {
    this.erros.set({});
    this.erroApi.set(null);
    this.gaveta.set(g);
  }

  protected abrirVeiculo(v: VeiculoDto | null): void {
    this.veiculoForm.reset({
      placa: v?.placa ?? '', prefixo: v?.prefixo ?? '', tipo: v?.tipo ?? 'USB', modelo: v?.modelo ?? '', ano: v?.ano != null ? String(v.ano) : '',
      lugares: v?.lugares != null ? String(v.lugares) : '', maca: v?.maca ?? true, cadeiraDeRodas: v?.cadeiraDeRodas ?? false,
      quilometragem: v?.quilometragem != null ? String(v.quilometragem) : '', unidadeId: v?.unidadeId ?? '', observacao: v?.observacao ?? '',
    });
    this.abrir({ tipo: 'veiculo', item: v });
  }

  protected abrirSituacao(v: VeiculoDto): void {
    this.situacaoForm.reset({ situacao: v.situacao === 'DISPONIVEL' ? 'MANUTENCAO' : 'DISPONIVEL', motivo: '', quilometragem: '' });
    this.abrir({ tipo: 'situacao', item: v });
  }

  protected abrirPlantao(v: VeiculoDto | null): void {
    const agora = new Date();
    agora.setMinutes(0, 0, 0);
    const fim = new Date(agora.getTime() + 12 * 3600_000);
    this.tripulacao.clear();
    const funcoesPadrao: FuncaoTripulante[] = v?.tipo === 'USA' ? ['CONDUTOR', 'ENFERMEIRO', 'MEDICO'] : v?.tipo === 'MOTOLANCIA' ? ['TECNICO_ENFERMAGEM']
      : v && !v.doSamu ? ['CONDUTOR'] : ['CONDUTOR', 'TECNICO_ENFERMAGEM'];
    funcoesPadrao.forEach((f) => this.tripulacao.push(this.tripulante(f)));
    this.plantaoForm.patchValue({ veiculoId: v?.uuid ?? '', inicio: localIso(agora), fim: localIso(fim), observacao: '' });
    this.veiculoDoPlantao.set(v?.tipo ?? null);
    if (!this.profissionais().length) {
      this.profissionalService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.profissionais.set(l.filter((p) => p.ativo !== false)));
    }
    this.abrir({ tipo: 'plantao', item: v });
  }

  protected mudarVeiculoDoPlantao(id: string): void {
    this.veiculoDoPlantao.set(this.veiculos().find((v) => v.uuid === id)?.tipo ?? null);
  }

  protected adicionarTripulante(): void {
    this.tripulacao.push(this.tripulante());
  }

  protected removerTripulante(i: number): void {
    this.tripulacao.removeAt(i);
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

  private numero(v: string): number | null {
    return String(v).trim() === '' ? null : Number(v);
  }

  protected salvar(): void {
    const g = this.gaveta();
    if (!g || this.submitting()) return;
    const erros: Record<string, string> = {};
    if (g.tipo === 'veiculo') {
      const v = this.veiculoForm.getRawValue();
      if (!/^[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}$/.test(v.placa.trim())) erros['placaVeiculo'] = 'Use o padrão ABC1234 ou o Mercosul ABC1D23.';
      if (!v.prefixo.trim()) erros['prefixoVeiculo'] = 'Informe o prefixo.';
      if (!v.unidadeId) erros['unidadeVeiculo'] = 'Escolha a unidade de lotação.';
      if (!this.validar(erros)) return;
      const dto = { placa: v.placa.trim(), prefixo: v.prefixo.trim(), tipo: v.tipo, modelo: v.modelo.trim() || null, ano: this.numero(v.ano),
        lugares: this.numero(v.lugares), maca: v.maca, cadeiraDeRodas: v.cadeiraDeRodas, quilometragem: this.numero(v.quilometragem), unidadeId: v.unidadeId,
        observacao: v.observacao.trim() || null };
      this.enviar(g.item ? this.service.atualizar(g.item.uuid, dto) : this.service.criar(dto), g.item ? 'Veículo atualizado' : 'Veículo cadastrado', (r) => r.prefixo, true);
    } else if (g.tipo === 'situacao') {
      const v = this.situacaoForm.getRawValue();
      if (!v.motivo.trim()) erros['motivoSituacao'] = 'Informe o motivo.';
      if (!this.validar(erros)) return;
      this.enviar(this.service.mudarSituacao(g.item.uuid, v.situacao, v.motivo.trim(), this.numero(v.quilometragem)),
        SITUACOES_VEICULO[v.situacao].rotulo, (r) => r.prefixo, true);
    } else {
      const v = this.plantaoForm.getRawValue();
      if (!v.veiculoId) erros['veiculoPlantao'] = 'Escolha a viatura.';
      if (!v.inicio) erros['inicioPlantao'] = 'Informe o início.';
      if (!v.fim) erros['fimPlantao'] = 'Informe o fim.';
      else if (v.inicio && new Date(v.fim) <= new Date(v.inicio)) erros['fimPlantao'] = 'O fim precisa ser depois do início.';
      const tripulacao = v.tripulacao.filter((t) => t.matricula && t.funcao) as { matricula: string; funcao: FuncaoTripulante }[];
      if (!tripulacao.length) erros['tripulante0'] = 'Informe a tripulação.';
      if (!this.validar(erros)) return;
      this.enviar(this.service.criarPlantao({ veiculoId: v.veiculoId, inicio: new Date(v.inicio).toISOString(), fim: new Date(v.fim).toISOString(), tripulacao,
        observacao: v.observacao.trim() || null }), 'Plantão registrado', (p) => p.prefixo, false);
    }
  }

  protected encerrarPlantao(p: PlantaoDto): void {
    this.enviar(this.service.encerrarPlantao(p.uuid), p.vigente ? 'Plantão encerrado' : 'Plantão cancelado', (r) => r.prefixo, false);
  }

  private enviar<T>(req: Observable<T>, titulo: string, detalhe: (r: T) => string, veiculo: boolean): void {
    this.submitting.set(true);
    req.subscribe({
      next: (r) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.toast.set({ titulo, detalhe: detalhe(r) });
        clearTimeout(this.toastTimer);
        this.toastTimer = setTimeout(() => this.toast.set(null), 4200);
        if (veiculo) this.selecionado.set(r as unknown as VeiculoDto);
        else if (this.selecionado()) this.selecionar(this.selecionado()!.uuid);
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
