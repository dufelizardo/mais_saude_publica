import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { catchError, forkJoin, of } from 'rxjs';
import { AcaoProgramaticaDto } from '../../../core/models/programa';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { AcaoEducativaDto, CampanhaComunicacaoDto, MaterialEducativoDto, NIVEIS_PARTICIPACAO, NivelParticipacao } from '../../../core/models/comunicacao';
import { ConteudoDoTreinamentoDto } from '../../../core/models/educacao-permanente';
import { ComunicacaoService } from '../../../core/services/comunicacao';
import { EducacaoPermanenteService } from '../../../core/services/educacao-permanente';
import { TerritorioService } from '../../../core/services/territorio';

/**
 * Educação em saúde da ação coletiva (ADR-0195): o tema, os conteúdos revisados, os materiais distribuídos, as microáreas alvo e a campanha
 * de comunicação; na realizada, a avaliação da equipe. A ação, o local e os participantes continuam em Programas.
 */
@Component({
  selector: 'app-educacao-da-acao',
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (mensagem()) { <div class="box box--info" role="status"><div>{{ mensagem() }}</div></div> }
    <div class="dw-field"><label for="f-ed-tema">Tema <span class="req" aria-hidden="true">*</span></label><input id="f-ed-tema" type="text" maxlength="120" [value]="tema()" (input)="tema.set($any($event.target).value)" [disabled]="!podeAcompanhar()" placeholder="Ex.: prevenção da dengue" /></div>
    <fieldset class="dw-field" [disabled]="!podeAcompanhar()">
      <legend>Conteúdos revisados</legend>
      <div class="radios" aria-label="Conteúdos educativos" style="flex-direction: column; align-items: flex-start;">
        @for (c of conteudosDisponiveis(); track c.grupoId) { <label><input type="checkbox" [checked]="conteudos().includes(c.grupoId)" (change)="alternarConteudo(c.grupoId)" />{{ c.titulo }}@if (c.vencido) { <span class="tag tag--warn">Vencido</span> }</label> }
        @empty { <span class="cell-sub">Nenhum conteúdo revisado na Comunicação.</span> }
      </div>
    </fieldset>
    @if (materiaisDisponiveis().length) {
      <fieldset class="dw-field" [disabled]="!podeAcompanhar()">
        <legend>Materiais distribuídos</legend>
        <ul class="eq-prof" aria-label="Materiais distribuídos">
          @for (m of materiaisDisponiveis(); track m.uuid) {
            <li class="mo-row"><span class="cell-main">{{ m.titulo }}</span><input type="number" min="0" style="width: 7rem;" [attr.aria-label]="'Quantidade de ' + m.titulo" [value]="quantidades()[m.uuid] ?? ''" (input)="quantidade(m.uuid, $any($event.target).value)" /></li>
          }
        </ul>
      </fieldset>
    }
    <div class="dw-row">
      <div class="dw-field"><label for="f-ed-microarea">Microárea alvo<span class="opt">opcional</span></label>
        <select id="f-ed-microarea" multiple size="4" (change)="microareasSelecionadas($event)" [disabled]="!podeAcompanhar()">
          @for (m of microareasDisponiveis(); track m.uuid) { <option [value]="m.uuid" [selected]="microareas().includes(m.uuid)">{{ m.rotulo }}</option> }
        </select></div>
      <div class="dw-field"><label for="f-ed-campanha">Campanha de comunicação<span class="opt">opcional</span></label>
        <select id="f-ed-campanha" (change)="campanha.set($any($event.target).value)" [disabled]="!podeAcompanhar()">
          <option value="" [selected]="!campanha()">Nenhuma</option>
          @for (c of campanhasAbertas(); track c.uuid) { <option [value]="c.uuid" [selected]="campanha() === c.uuid">{{ c.nome }}</option> }
        </select></div>
    </div>
    @if (podeAcompanhar() && acao().situacao !== 'CANCELADA' && acao().situacao !== 'NAO_REALIZADA') {
      <div class="row-actions" style="justify-content: flex-start;"><button type="button" class="btn btn--sm" (click)="salvar()">Salvar educação em saúde</button></div>
    }

    @if (acao().situacao === 'REALIZADA' && detalhe()?.tema) {
      <h4 class="mo-grupo" style="margin-top: var(--s-4);">Avaliação da equipe</h4>
      <p class="cell-sub">Público alcançado: {{ detalhe()?.publicoAlcancado ?? 'não informado' }}</p>
      <div class="dw-row">
        <div class="dw-field"><label for="f-ed-participacao">Participação <span class="req" aria-hidden="true">*</span></label>
          <select id="f-ed-participacao" (change)="participacao.set($any($event.target).value)" [disabled]="!podeAcompanhar()">
            <option value="" [selected]="!participacao()">Escolha…</option>
            @for (n of niveis; track n) { <option [value]="n" [selected]="participacao() === n">{{ nomesNivel[n] }}</option> }
          </select></div>
        <div class="dw-field"><label for="f-ed-avaliacao">Avaliação<span class="opt">opcional</span></label><input id="f-ed-avaliacao" type="text" maxlength="1000" [value]="avaliacao()" (input)="avaliacao.set($any($event.target).value)" [disabled]="!podeAcompanhar()" placeholder="Ex.: muitas perguntas sobre os focos" /></div>
      </div>
      @if (podeAcompanhar()) { <div class="row-actions" style="justify-content: flex-start;"><button type="button" class="btn btn--ghost btn--sm" (click)="avaliar()">Salvar avaliação</button></div> }
    }
  `,
})
export class EducacaoDaAcao {
  private readonly comunicacao = inject(ComunicacaoService);
  private readonly educacao = inject(EducacaoPermanenteService);
  private readonly territorio = inject(TerritorioService);

  readonly acao = input.required<AcaoProgramaticaDto>();
  readonly podeAcompanhar = input(false);

  protected readonly niveis = Object.keys(NIVEIS_PARTICIPACAO) as NivelParticipacao[];
  protected readonly nomesNivel = NIVEIS_PARTICIPACAO;
  protected readonly detalhe = signal<AcaoEducativaDto | null>(null);
  protected readonly conteudosDisponiveis = signal<ConteudoDoTreinamentoDto[]>([]);
  protected readonly materiaisPorConteudo = signal<Record<string, MaterialEducativoDto[]>>({});
  protected readonly microareasDisponiveis = signal<{ uuid: string; rotulo: string }[]>([]);
  protected readonly campanhas = signal<CampanhaComunicacaoDto[]>([]);
  protected readonly campanhasAbertas = computed(() => this.campanhas().filter((c) => c.situacao === 'PLANEJADA' || c.situacao === 'EM_ANDAMENTO'
    || c.uuid === this.campanha()));
  protected readonly tema = signal('');
  protected readonly conteudos = signal<string[]>([]);
  protected readonly quantidades = signal<Record<string, string>>({});
  protected readonly microareas = signal<string[]>([]);
  protected readonly campanha = signal('');
  protected readonly participacao = signal<NivelParticipacao | ''>('');
  protected readonly avaliacao = signal('');
  protected readonly erro = signal<string | null>(null);
  protected readonly mensagem = signal<string | null>(null);
  /** Os materiais ativos dos conteúdos marcados, mais os já distribuídos. */
  protected readonly materiaisDisponiveis = computed(() => this.conteudos().flatMap((g) => (this.materiaisPorConteudo()[g] ?? []).filter((m) => !m.retiradoEm)));

  constructor() {
    forkJoin({
      conteudos: this.educacao.conteudosDisponiveis().pipe(catchError(() => of([]))),
      campanhas: this.comunicacao.campanhas().pipe(catchError(() => of([]))),
      arvore: this.territorio.arvore().pipe(catchError(() => of(null))),
    }).subscribe((r) => {
      this.conteudosDisponiveis.set(r.conteudos);
      this.campanhas.set(r.campanhas);
      const lista: { uuid: string; rotulo: string }[] = [];
      for (const s of r.arvore?.segmentos ?? []) for (const t of s.territorios) for (const a of t.areas) for (const m of a.microareas) {
        lista.push({ uuid: m.uuid, rotulo: `${a.nome} ${m.codigo}${m.nome ? ' (' + m.nome + ')' : ''}` });
      }
      this.microareasDisponiveis.set(lista);
    });
    effect(() => {
      const a = this.acao();
      untracked(() => this.carregar(a.uuid));
    });
  }

  private carregar(acaoId: string): void {
    this.erro.set(null);
    this.comunicacao.educacaoDaAcao(acaoId).subscribe({
      next: (d) => this.aplicar(d),
      error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)),
    });
  }

  private aplicar(d: AcaoEducativaDto): void {
    this.detalhe.set(d);
    this.tema.set(d.tema ?? '');
    this.conteudos.set(d.conteudos.map((c) => c.grupoId));
    this.quantidades.set(Object.fromEntries(d.materiais.map((m) => [m.materialId, String(m.quantidade)])));
    this.microareas.set(d.microareas.map((m) => m.microareaId));
    this.campanha.set(d.campanhaComunicacaoId ?? '');
    this.participacao.set(d.participacao ?? '');
    this.avaliacao.set(d.avaliacao ?? '');
    d.conteudos.forEach((c) => this.carregarMateriais(c.grupoId));
  }

  private carregarMateriais(grupoId: string): void {
    if (this.materiaisPorConteudo()[grupoId]) return;
    this.comunicacao.materiais(grupoId).pipe(catchError(() => of([]))).subscribe((l) =>
      this.materiaisPorConteudo.set({ ...this.materiaisPorConteudo(), [grupoId]: l }));
  }

  protected alternarConteudo(grupoId: string): void {
    const atual = this.conteudos();
    this.conteudos.set(atual.includes(grupoId) ? atual.filter((g) => g !== grupoId) : [...atual, grupoId]);
    this.carregarMateriais(grupoId);
  }

  protected quantidade(materialId: string, valor: string): void {
    this.quantidades.set({ ...this.quantidades(), [materialId]: valor });
  }

  protected microareasSelecionadas(ev: Event): void {
    const sel = ev.target as HTMLSelectElement;
    this.microareas.set(Array.from(sel.selectedOptions).map((o) => o.value));
  }

  protected salvar(): void {
    this.erro.set(null);
    this.mensagem.set(null);
    if (!this.tema().trim()) {
      this.erro.set('Informe o tema.');
      return;
    }
    const disponiveis = new Set(this.materiaisDisponiveis().map((m) => m.uuid));
    const materiais = Object.entries(this.quantidades())
      .filter(([id, q]) => disponiveis.has(id) && String(q).trim() !== '' && Number(q) > 0)
      .map(([materialId, q]) => ({ materialId, quantidade: Number(q) }));
    this.comunicacao.salvarEducacaoDaAcao(this.acao().uuid, {
      tema: this.tema().trim(), conteudoGrupoIds: this.conteudos(), materiais, microareaIds: this.microareas(), campanhaComunicacaoId: this.campanha() || null,
    }).subscribe({
      next: (d) => {
        this.aplicar(d);
        this.mensagem.set('Educação em saúde salva.');
      },
      error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)),
    });
  }

  protected avaliar(): void {
    this.erro.set(null);
    this.mensagem.set(null);
    if (!this.participacao()) {
      this.erro.set('Informe como foi a participação.');
      return;
    }
    this.comunicacao.avaliarAcaoEducativa(this.acao().uuid, this.participacao() as NivelParticipacao, this.avaliacao().trim() || null).subscribe({
      next: (d) => {
        this.aplicar(d);
        this.mensagem.set('Avaliação salva.');
      },
      error: (e: HttpErrorResponse) => this.erro.set(mensagem(e)),
    });
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta ação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
