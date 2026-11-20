import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { BaixaBemDto, CLASSES_BEM, MOTIVOS_BAIXA, PainelPatrimonioDto } from '../../../core/models/patrimonio';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { PatrimonioService } from '../../../core/services/patrimonio';

/**
 * Painel do Patrimônio, fatia PM3 (ADR-0217): os bens e os valores (aquisição e contábil estimado) por unidade e por classe, os
 * inventários, as baixas, a manutenção dos bens mais caros e as baixas à espera, que a gestão confirma ou recusa.
 */
@Component({
  selector: 'app-painel-patrimonio',
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (aviso()) { <div class="box box--info" role="status"><strong>{{ aviso() }}</strong></div> }
    @if (painel(); as p) {
      <div class="pp-numeros" aria-label="Resumo do patrimônio">
        <div class="card pp-num"><span class="pp-num__v">{{ p.bens }}</span><span class="cell-sub">bens tombados</span></div>
        <div class="card pp-num"><span class="pp-num__v">{{ moeda(p.valorAquisicao) }}</span><span class="cell-sub">valor de aquisição</span></div>
        <div class="card pp-num"><span class="pp-num__v">{{ moeda(p.valorContabil) }}</span><span class="cell-sub">valor contábil estimado</span></div>
        <div class="card pp-num"><span class="pp-num__v">{{ p.ociosos }}</span><span class="cell-sub">ociosos</span></div>
        <div class="card pp-num"><span class="pp-num__v">{{ p.aTombar }}</span><span class="cell-sub">a tombar</span></div>
        <div class="card pp-num"><span class="pp-num__v">{{ p.inventariosAbertos }}</span><span class="cell-sub">inventários abertos</span></div>
        <div class="card pp-num"><span class="pp-num__v">{{ p.baixasNoAno }}</span><span class="cell-sub">baixas no ano</span></div>
      </div>

      @if (p.baixasPendentes.length) {
        <h3 class="mo-grupo pp-titulo">Baixas à espera da confirmação</h3>
        <ul class="pp-lista" aria-label="Baixas à espera">
          @for (b of p.baixasPendentes; track b.uuid) {
            <li>
              <span><span class="mono">{{ b.numeroTombamento }}</span> {{ b.bemDescricao }} <span class="cell-sub">{{ b.unidadeNome }} · {{ motivos[b.motivo] }} · processo {{ b.processo }} · {{ b.valorAquisicao != null ? moeda(b.valorAquisicao) : '—' }}</span></span>
              @if (podeConfirmar) {
                <span class="pp-acoes">
                  <button type="button" class="link-btn" (click)="confirmar(b)" [attr.aria-label]="'Confirmar baixa de ' + b.numeroTombamento">Confirmar</button>
                  <input type="text" maxlength="500" placeholder="motivo da recusa" [attr.aria-label]="'Motivo da recusa da baixa de ' + b.numeroTombamento" (input)="motivo.set($any($event.target).value)" />
                  <button type="button" class="link-btn" (click)="recusar(b)" [attr.aria-label]="'Recusar baixa de ' + b.numeroTombamento">Recusar</button>
                </span>
              }
            </li>
          }
        </ul>
      }

      <h3 class="mo-grupo pp-titulo">Por unidade</h3>
      <div class="tbl-wrap card">
        <table class="pp-tabela" aria-label="Patrimônio por unidade">
          <thead><tr><th scope="col">Unidade</th><th scope="col">Bens</th><th scope="col">Aquisição</th><th scope="col">Contábil</th><th scope="col">Último inventário</th></tr></thead>
          <tbody>
            @for (u of p.porUnidade; track u.unidadeId) {
              <tr>
                <td>{{ u.unidadeNome }}</td><td>{{ u.bens }}</td><td class="mono">{{ moeda(u.valorAquisicao) }}</td><td class="mono">{{ moeda(u.valorContabil) }}</td>
                <td>{{ u.ultimoInventario ? data(u.ultimoInventario) : 'nunca' }}@if (u.semInventarioHa12Meses) { <span class="tag tag--warn">há mais de 12 meses</span> }</td>
              </tr>
            } @empty { <tr><td colspan="5" class="cell-sub">Nenhum bem.</td></tr> }
          </tbody>
        </table>
      </div>

      <h3 class="mo-grupo pp-titulo">Por classe</h3>
      <ul class="pp-lista" aria-label="Patrimônio por classe">
        @for (c of p.porClasse; track c.classe) { <li><span>{{ classes[c.classe] }}</span><span class="cell-sub">{{ c.bens }} bem(ns) · {{ moeda(c.valorAquisicao) }} · contábil {{ moeda(c.valorContabil) }}</span></li> }
      </ul>

      <h3 class="mo-grupo pp-titulo">Manutenção: os bens que mais custaram</h3>
      <ul class="pp-lista" aria-label="Manutenção por bem">
        @for (m of p.manutencao; track m.bemId) { <li><span><span class="mono">{{ m.numeroTombamento }}</span> {{ m.descricao }} <span class="cell-sub">{{ m.unidadeNome }}</span></span><span class="cell-sub">{{ moeda(m.custo) }} · {{ m.diasParado }} dia(s) parado</span></li> } @empty { <li class="cell-sub">Sem manutenção registrada.</li> }
      </ul>
    }
  `,
  styles: `
    .pp-numeros { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: var(--s-2); margin-bottom: var(--s-3); }
    .pp-num { padding: var(--s-3); display: grid; gap: 2px; }
    .pp-num__v { font-size: 1.25rem; font-weight: 600; }
    .pp-titulo { margin-top: var(--s-4); }
    .pp-tabela { min-width: 0; width: 100%; }
    .pp-lista { margin: 0 0 var(--s-2); padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .pp-lista li { display: flex; justify-content: space-between; gap: var(--s-2); align-items: baseline; flex-wrap: wrap; }
    .pp-acoes { display: flex; gap: var(--s-2); align-items: center; flex-wrap: wrap; }
  `,
})
export class PainelPatrimonio implements OnInit {
  @Input() podeConfirmar = false;

  private readonly service = inject(PatrimonioService);

  protected readonly classes = CLASSES_BEM;
  protected readonly motivos = MOTIVOS_BAIXA;
  protected readonly painel = signal<PainelPatrimonioDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly motivo = signal('');

  ngOnInit(): void {
    this.carregar();
  }

  private carregar(): void {
    this.service.painel().subscribe({ next: (p) => this.painel.set(p), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  protected confirmar(b: BaixaBemDto): void {
    this.service.confirmarBaixa(b.uuid).subscribe({
      next: () => {
        this.aviso.set(`Baixa de ${b.numeroTombamento} confirmada.`);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  protected recusar(b: BaixaBemDto): void {
    const motivo = this.motivo().trim();
    if (!motivo) {
      this.erro.set('Informe o motivo da recusa.');
      return;
    }
    this.service.recusarBaixa(b.uuid, motivo).subscribe({
      next: () => {
        this.aviso.set(`Baixa de ${b.numeroTombamento} recusada.`);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)),
    });
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }
}

function mensagem(e: HttpErrorResponse): string {
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
