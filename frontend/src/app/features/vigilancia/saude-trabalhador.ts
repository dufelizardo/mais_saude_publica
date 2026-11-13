import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, input, output, signal, untracked } from '@angular/core';
import {
  AcidenteSemNotificacaoDto,
  ETAPAS_ACOMPANHAMENTO,
  PainelTrabalhadorDto,
  SITUACOES_NOTIFICACAO,
  SITUACOES_TRABALHO,
} from '../../core/models/vigilancia';
import { VigilanciaService } from '../../core/services/vigilancia';

/**
 * A aba Saúde do trabalhador da Vigilância (ADR-0207): as notificações relacionadas ao trabalho do escopo, as etapas do
 * acompanhamento sorológico a fazer e, para a vigilância com acesso à rede inteira, os acidentes de servidores do RH ainda sem
 * notificação.
 */
@Component({
  selector: 'app-saude-trabalhador',
  template: `
    @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
    @if (painel(); as p) {
      <p class="cell-sub" aria-label="Totais da saúde do trabalhador">{{ p.total }} {{ p.total === 1 ? 'notificação relacionada' : 'notificações relacionadas' }} ao trabalho
        · {{ p.acidentes }} {{ p.acidentes === 1 ? 'acidente' : 'acidentes' }} · {{ p.materialBiologico }} com material biológico · {{ p.obitos }} {{ p.obitos === 1 ? 'óbito' : 'óbitos' }}</p>
      @if (p.acompanhamentos.length) {
        <section class="box box--warn" aria-label="Acompanhamentos sorológicos a fazer">
          <strong>Acompanhamento sorológico a fazer</strong>
          <ul class="eq-prof">
            @for (a of p.acompanhamentos; track a.notificacaoId + a.etapa) {
              <li class="mo-row">
                <span><span class="cell-main">{{ a.pacienteNome }} · {{ etapas[a.etapa] }}</span><span class="cell-sub">{{ a.unidadeNome }} · prevista em {{ data(a.prevista) }}@if (a.vencida) { · <span class="tag tag--alert">Vencida</span> }</span></span>
                <button type="button" class="btn btn--ghost btn--sm" (click)="abrirNotificacao.emit(a.notificacaoId)" [attr.aria-label]="'Abrir notificação de ' + a.pacienteNome">Abrir</button>
              </li>
            }
          </ul>
        </section>
      }
      @if (p.notificacoes.length) {
        <div class="tbl-wrap">
          <table aria-label="Notificações relacionadas ao trabalho">
            <thead><tr><th scope="col">Trabalhador</th><th scope="col">Agravo</th><th scope="col">Ocupação</th><th scope="col">Empregador</th><th scope="col">Evento</th><th scope="col">Situação</th><th scope="col" class="num">Ações</th></tr></thead>
            <tbody>
              @for (n of p.notificacoes; track n.notificacaoId) {
                <tr>
                  <td><span class="cell-main">{{ n.pacienteNome }}</span><span class="cell-sub">{{ situacoesTrabalho[n.situacaoTrabalho] }}@if (n.servidor) { · servidor da rede }</span></td>
                  <td>{{ n.agravoNome }}@if (n.obito) { <span class="tag tag--alert">Óbito</span> }</td>
                  <td class="mono">{{ n.ocupacao }}</td>
                  <td>{{ n.empregadorNome ?? '—' }}@if (n.empregadorCnae) { <span class="cell-sub mono">CNAE {{ n.empregadorCnae }}</span> }</td>
                  <td class="mono">{{ data(n.dataEvento ?? n.dataNotificacao) }}</td>
                  <td><span [class]="'tag tag--' + situacoes[n.situacao].classe">{{ situacoes[n.situacao].rotulo }}</span></td>
                  <td class="num"><button type="button" class="btn btn--ghost btn--sm" (click)="abrirNotificacao.emit(n.notificacaoId)" [attr.aria-label]="'Abrir notificação: ' + n.pacienteNome + ' ' + n.agravoNome">Abrir</button></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else { <p class="cell-sub">Nenhuma notificação relacionada ao trabalho neste filtro.</p> }
    }
    @if (podeInvestigar()) {
      <h4 class="mo-grupo">Acidentes de servidores sem notificação</h4>
      @if (semAcessoRede()) {
        <p class="cell-sub">Os acidentes dos servidores são da rede inteira: só a vigilância com acesso a todas as unidades vê esta lista.</p>
      } @else if (acidentes().length) {
        <ul class="eq-prof" aria-label="Acidentes de servidores sem notificação">
          @for (a of acidentes(); track a.acidenteId) {
            <li class="mo-row">
              <span><span class="cell-main">{{ a.profissionalNome }} · matrícula {{ a.profissionalMatricula }}</span>
                <span class="cell-sub">{{ dataHora(a.dataHora) }}@if (a.descricao) { · {{ a.descricao }} } · CAT {{ a.catEmitida ? 'emitida' : 'não emitida' }}@if (!a.pacienteId) { · sem cadastro de paciente com o mesmo CPF }</span></span>
              <button type="button" class="btn btn--sm" (click)="notificarAcidente.emit(a)" [attr.aria-label]="'Notificar acidente de ' + a.profissionalNome">Notificar</button>
            </li>
          }
        </ul>
      } @else { <p class="cell-sub">Nenhum acidente de servidor sem notificação.</p> }
    }
  `,
})
export class SaudeTrabalhador {
  private readonly service = inject(VigilanciaService);

  readonly unidadeId = input('');
  readonly podeInvestigar = input(false);
  /** Muda a cada notificação registrada, para recarregar. */
  readonly versao = input(0);
  readonly abrirNotificacao = output<string>();
  readonly notificarAcidente = output<AcidenteSemNotificacaoDto>();

  protected readonly situacoes = SITUACOES_NOTIFICACAO;
  protected readonly situacoesTrabalho = SITUACOES_TRABALHO;
  protected readonly etapas = ETAPAS_ACOMPANHAMENTO;
  protected readonly painel = signal<PainelTrabalhadorDto | null>(null);
  protected readonly acidentes = signal<AcidenteSemNotificacaoDto[]>([]);
  protected readonly semAcessoRede = signal(false);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    effect(() => {
      const unidade = this.unidadeId();
      const investiga = this.podeInvestigar();
      this.versao();
      untracked(() => this.carregar(unidade, investiga));
    });
  }

  private carregar(unidade: string, investiga: boolean): void {
    this.erro.set(null);
    this.service.painelTrabalhador(unidade || undefined).subscribe({
      next: (p) => this.painel.set(p),
      error: () => this.erro.set('Não foi possível carregar a saúde do trabalhador.'),
    });
    if (!investiga) return;
    this.service.acidentesSemNotificacao().subscribe({
      next: (a) => {
        this.semAcessoRede.set(false);
        this.acidentes.set(a);
      },
      error: (e: HttpErrorResponse) => {
        this.acidentes.set([]);
        this.semAcessoRede.set(e.status === 403);
      },
    });
  }

  protected data(iso: string | null | undefined): string {
    if (!iso) return '—';
    const [a, m, d] = iso.substring(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string | null | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
