import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { catchError, of } from 'rxjs';
import { AtrasadoVacinacaoDto, dataBr, idadeTexto } from '../../../core/models/imunizacao';
import { ImunizacaoService } from '../../../core/services/imunizacao';
import { UnidadeSaudeService } from '../../../core/services/unidade-saude';

/**
 * Lista de busca ativa de vacinação para imprimir (ADR-0149): os atrasados da unidade (da equipe e da microárea, se
 * filtrados), agrupados por microárea com o agente comunitário, com onde mora e uma coluna para anotar a visita. Página
 * fora do AppShell, como o laudo (ADR-0095). Tem nome de paciente: a leitura é a mesma da Sala de Vacina, auditada.
 */
@Component({
  selector: 'app-busca-ativa-vacinacao',
  template: `
    <div class="ba-acoes">
      <button type="button" class="btn" (click)="imprimir()" [disabled]="!lista()?.length">Imprimir</button>
      <button type="button" class="btn btn--ghost" (click)="fechar()">Fechar</button>
    </div>
    <article class="ba">
      <header class="ba__cab">
        <h1>Busca ativa de vacinação</h1>
        <p>{{ unidadeNome() }} · emitida em {{ emitidaEm }}</p>
      </header>
      @if (erro()) {
        <p role="alert">{{ erro() }}</p>
      } @else if (lista() === undefined) {
        <p>Carregando…</p>
      } @else {
        @for (g of grupos(); track g.chave) {
          <section class="ba__grupo" [attr.aria-label]="g.titulo">
            <h2>{{ g.titulo }}@if (g.acs) { <small> · ACS {{ g.acs }}</small> }</h2>
            <table>
              <thead><tr><th scope="col">Paciente</th><th scope="col">Nascimento</th><th scope="col">Onde mora</th><th scope="col">Doses atrasadas</th><th scope="col">Visita</th></tr></thead>
              <tbody>
                @for (a of g.itens; track a.pacienteId) {
                  <tr>
                    <td>{{ a.pacienteNome }}</td>
                    <td>{{ data(a.dataNascimento) }} ({{ idade(a.idadeMeses) }})</td>
                    <td>{{ a.endereco ?? '—' }}</td>
                    <td>{{ a.atrasadas.join('; ') }}</td>
                    <td class="ba__visita"></td>
                  </tr>
                }
              </tbody>
            </table>
          </section>
        } @empty {
          <p>Nenhum adscrito com dose atrasada.</p>
        }
        <p class="ba__rod">{{ lista()?.length ?? 0 }} paciente(s). Atrasos pelo calendário vacinal vigente, os mais antigos primeiro.</p>
      }
    </article>
  `,
  styles: `
    :host { display: block; background: var(--ink-50); min-height: 100vh; padding: 24px 16px; }
    .ba-acoes { display: flex; gap: 8px; justify-content: flex-end; max-width: 960px; margin: 0 auto 12px; }
    .ba { background: #ffffff; max-width: 960px; margin: 0 auto; padding: 32px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08); }
    .ba__cab h1 { font-size: 1.2rem; margin: 0; }
    .ba__cab p { margin: 4px 0 16px; color: #4a5361; font-size: 0.85rem; }
    .ba__grupo h2 { font-size: 0.95rem; margin: 16px 0 6px; }
    .ba__grupo small { font-weight: 400; color: #4a5361; }
    table { width: 100%; border-collapse: collapse; font-size: 0.8rem; }
    th, td { border: 1px solid #d5dae1; padding: 4px 6px; text-align: left; vertical-align: top; }
    .ba__visita { width: 110px; }
    .ba__rod { margin-top: 16px; font-size: 0.72rem; color: #4a5361; }
    @media print {
      :host { background: #ffffff; padding: 0; }
      .ba-acoes { display: none; }
      .ba { box-shadow: none; border-radius: 0; padding: 0; max-width: none; }
      @page { size: A4 landscape; margin: 12mm; }
    }
  `,
})
export class BuscaAtivaVacinacao {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(ImunizacaoService);
  private readonly unidadeService = inject(UnidadeSaudeService);

  protected readonly lista = signal<AtrasadoVacinacaoDto[] | undefined>(undefined);
  protected readonly unidadeNome = signal('');
  protected readonly erro = signal<string | null>(null);
  protected readonly emitidaEm = new Date().toLocaleDateString('pt-BR');

  /** Por microárea, as sem microárea no fim. */
  protected readonly grupos = computed(() => {
    const m = new Map<string, { chave: string; titulo: string; acs: string | null; itens: AtrasadoVacinacaoDto[] }>();
    for (const a of this.lista() ?? []) {
      const chave = `${a.equipeNome ?? ''}|${a.microareaNome ?? ''}`;
      const titulo = `${a.equipeNome ?? 'Sem equipe'} · ${a.microareaNome ?? 'Sem microárea'}`;
      const g = m.get(chave) ?? { chave, titulo, acs: a.acsNome ?? null, itens: [] };
      g.itens.push(a);
      m.set(chave, g);
    }
    return [...m.values()].sort((x, y) => x.titulo.endsWith('Sem microárea') === y.titulo.endsWith('Sem microárea')
      ? x.titulo.localeCompare(y.titulo) : x.titulo.endsWith('Sem microárea') ? 1 : -1);
  });

  constructor() {
    const q = this.route.snapshot.queryParamMap;
    const unidadeId = q.get('unidadeId') ?? '';
    this.unidadeService.listar().pipe(catchError(() => of([]))).subscribe((l) => this.unidadeNome.set(l.find((u) => u.uuid === unidadeId)?.nome ?? ''));
    this.service.atrasados(unidadeId, q.get('equipeId') ?? undefined, q.get('microareaId') ?? undefined).subscribe({
      next: (l) => this.lista.set(l),
      error: (e: HttpErrorResponse) =>
        this.erro.set(e.status === 403 ? 'Seu acesso não inclui a sala de vacina desta unidade (VACINACAO.REGISTRAR).' : 'Não foi possível montar a lista.'),
    });
  }

  protected data(iso: string): string {
    return dataBr(iso);
  }

  protected idade(meses: number): string {
    return idadeTexto(meses);
  }

  protected imprimir(): void {
    window.print();
  }

  protected fechar(): void {
    window.close();
  }
}
