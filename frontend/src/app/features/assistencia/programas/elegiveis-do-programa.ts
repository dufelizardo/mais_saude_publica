import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { ElegiveisDto, ElegivelDto } from '../../../core/models/adscricao';
import { ProgramaDto } from '../../../core/models/programa';
import { AreaNoDto } from '../../../core/models/territorio';
import { ProgramaService } from '../../../core/services/programa';
import { TerritorioService } from '../../../core/services/territorio';

/** Uma equipe com área, nas unidades que oferecem o programa. */
interface EquipeComArea {
  equipeId: string;
  rotulo: string;
  unidadeId: string;
  area: AreaNoDto;
}

/**
 * Elegíveis do programa (ADR-0128): os adscritos de uma equipe (ou de uma microárea dela) no público do programa que ainda
 * não estão inscritos. A lista avalia idade e sexo; gestação e condição clínica aparecem como não avaliadas. "Inscrever"
 * abre a inscrição do programa já com o paciente.
 */
@Component({
  selector: 'app-elegiveis-do-programa',
  template: `
    <div class="info-block" id="elegiveis" style="margin: 0 var(--s-5) var(--s-5);">
      <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap;">
        <h3>Elegíveis <span class="cell-sub" style="display: inline;">· adscritos ainda fora do programa</span></h3>
        @if (equipes().length) {
          <div style="display: flex; gap: 8px; flex-wrap: wrap;">
            <select class="sel" aria-label="Equipe dos elegíveis" [value]="equipeId()" (change)="escolherEquipe($any($event.target).value)">
              <option value="">Escolha a equipe…</option>
              @for (e of equipes(); track e.equipeId) { <option [value]="e.equipeId">{{ e.rotulo }}</option> }
            </select>
            @if (equipeEscolhida(); as e) {
              <select class="sel" aria-label="Microárea dos elegíveis" [value]="microareaId()" (change)="microareaId.set($any($event.target).value); carregar()">
                <option value="">Todas as microáreas</option>
                @for (m of e.area.microareas; track m.uuid) { <option [value]="m.uuid">Microárea {{ m.codigo }}</option> }
              </select>
            }
          </div>
        }
      </div>
      @if (!equipes().length) {
        <p class="cell-sub">Nenhuma equipe com área no Território nas unidades que oferecem o programa: a lista sai da adscrição das famílias.</p>
      } @else if (!equipeId()) {
        <p class="cell-sub">Escolha a equipe para ver os adscritos que cabem no público do programa.</p>
      } @else if (resultado() === undefined) {
        <p class="cell-sub">Carregando…</p>
      } @else if (resultado() === null) {
        <p class="cell-sub">Não foi possível carregar os elegíveis.</p>
      } @else if (resultado(); as r) {
        <p class="cell-sub">
          @if (r.criteriosAvaliados.length) { Avaliados: {{ r.criteriosAvaliados.join(', ') }}. } @else { O programa não define idade nem sexo: todos os adscritos entram. }
          @if (r.criteriosNaoAvaliados.length) { Não avaliados: {{ r.criteriosNaoAvaliados.join(', ') }} — confirme antes de inscrever. }
          @if (r.semDataNascimento) { {{ r.semDataNascimento }} adscrito(s) sem data de nascimento ficaram de fora. }
        </p>
        @if (!r.elegiveis.length) {
          <p class="cell-sub">Nenhum elegível fora do programa.</p>
        } @else {
          <div class="tbl-wrap">
            <table aria-label="Elegíveis do programa">
              <thead><tr><th scope="col">Paciente</th><th scope="col">Microárea</th>@if (podeInscrever()) { <th scope="col" class="num">Ações</th> }</tr></thead>
              <tbody>
                @for (e of r.elegiveis; track e.pacienteId) {
                  <tr>
                    <td><span class="cell-main">{{ e.nome }}</span>@if (e.idade != null) { <span class="cell-sub">{{ e.idade }} anos</span> }</td>
                    <td>{{ e.microareaCodigo || '—' }}@if (e.acsNome) { <span class="cell-sub">ACS {{ e.acsNome }}</span> }</td>
                    @if (podeInscrever()) {
                      <td class="num"><button type="button" class="btn btn--ghost btn--sm" (click)="inscrever(e)" [attr.aria-label]="'Inscrever ' + e.nome">Inscrever</button></td>
                    }
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      }
    </div>
  `,
})
export class ElegiveisDoPrograma {
  private readonly territorio = inject(TerritorioService);
  private readonly programaService = inject(ProgramaService);

  readonly programa = input.required<ProgramaDto>();
  readonly podeInscrever = input(false);
  /** Muda depois de uma inscrição, para a lista recarregar. */
  readonly versao = input(0);

  readonly inscreverPaciente = output<{ pacienteId: string; nome: string; unidadeId: string; equipeId: string }>();

  private readonly todas = signal<EquipeComArea[]>([]);
  protected readonly equipeId = signal('');
  protected readonly microareaId = signal('');
  /** `undefined` carregando, `null` erro. */
  protected readonly resultado = signal<ElegiveisDto | null | undefined>(undefined);

  private readonly programaId = computed(() => this.programa().resumo.uuid);

  /** As equipes com área nas unidades que oferecem o programa. */
  protected readonly equipes = computed(() => {
    const unidades = new Set(this.programa().participacoes.map((p) => p.unidadeId));
    return this.todas().filter((e) => unidades.has(e.unidadeId));
  });

  protected readonly equipeEscolhida = computed(() => this.equipes().find((e) => e.equipeId === this.equipeId()));

  constructor() {
    this.territorio.arvore().pipe(catchError(() => of(null))).subscribe((a) => {
      const lista: EquipeComArea[] = [];
      for (const s of a?.segmentos ?? []) {
        for (const t of s.territorios) {
          for (const area of t.areas) {
            if (area.equipe) lista.push({ equipeId: area.equipe.uuid, rotulo: `${area.equipe.nome} · área ${area.codigo}`, unidadeId: area.equipe.unidadeId, area });
          }
        }
      }
      this.todas.set(lista);
    });
    effect(() => {
      this.programaId();
      untracked(() => {
        this.equipeId.set('');
        this.microareaId.set('');
        this.resultado.set(undefined);
      });
    });
    effect(() => {
      this.versao();
      untracked(() => {
        if (this.equipeId()) this.carregar();
      });
    });
  }

  protected escolherEquipe(id: string): void {
    this.equipeId.set(id);
    this.microareaId.set('');
    this.carregar();
  }

  protected carregar(): void {
    const equipe = this.equipeId();
    if (!equipe) return;
    this.resultado.set(undefined);
    this.programaService.elegiveis(this.programaId(), equipe, this.microareaId() || null).pipe(catchError(() => of(null)))
      .subscribe((r) => this.resultado.set(r));
  }

  protected inscrever(e: ElegivelDto): void {
    const equipe = this.equipeEscolhida();
    if (equipe) this.inscreverPaciente.emit({ pacienteId: e.pacienteId, nome: e.nome, unidadeId: equipe.unidadeId, equipeId: equipe.equipeId });
  }
}
