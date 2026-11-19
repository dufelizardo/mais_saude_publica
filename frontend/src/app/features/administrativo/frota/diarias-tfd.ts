import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { BeneficiarioDiariaTfd, DiariaTfdDto } from '../../../core/models/financeiro';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { SolicitacaoTransporteDto } from '../../../core/models/transporte';
import { FinanceiroService } from '../../../core/services/financeiro';
import { Drawer } from '../../../shared/drawer/drawer';

/**
 * Diárias e ajuda de custo do TFD (Portaria SAS/MS 55/1999; ADR-0219), na solicitação autorizada: o beneficiário (paciente ou acompanhante),
 * o CPF dele como credor, os dias e o valor da diária da tabela do município. A liquidação é do Financeiro.
 */
@Component({
  selector: 'app-diarias-tfd',
  imports: [Drawer],
  template: `
    <app-drawer [titulo]="'Diárias do TFD: ' + solicitacao.pacienteNome" (fechar)="fechar.emit()">
      <form class="dw-form" (submit)="$event.preventDefault(); conceder()" novalidate>
        <div class="dw-body">
          @if (erro()) { <div class="box box--err" role="alert"><strong>{{ erro() }}</strong></div> }
          @if (aviso()) { <div class="box box--info" role="status"><strong>{{ aviso() }}</strong></div> }
          <ul class="dt-lista" aria-label="Diárias concedidas">
            @for (d of diarias(); track d.uuid) {
              <li><span>{{ d.beneficiario === 'PACIENTE' ? 'Paciente' : 'Acompanhante' }} · {{ d.beneficiarioNome }}</span>
                <span class="cell-sub">{{ d.dias }} × {{ moeda(d.valorDiaria) }} = {{ moeda(d.valorTotal) }} · {{ d.liquidada ? 'liquidada' : 'a liquidar' }}</span></li>
            } @empty { <li class="cell-sub">Nenhuma diária concedida.</li> }
          </ul>
          <div class="dw-row">
            <div class="dw-field"><label for="f-dt-beneficiario">Beneficiário</label>
              <select id="f-dt-beneficiario" (change)="set('beneficiario', $event)">
                <option value="PACIENTE">Paciente</option>
                @if (solicitacao.acompanhanteNome) { <option value="ACOMPANHANTE">Acompanhante ({{ solicitacao.acompanhanteNome }})</option> }
              </select></div>
            <div class="dw-field"><label for="f-dt-cpf">CPF do beneficiário <span class="req" aria-hidden="true">*</span></label><input id="f-dt-cpf" type="text" maxlength="14" (input)="set('cpf', $event)" /></div>
          </div>
          <div class="dw-row">
            <div class="dw-field"><label for="f-dt-dias">Dias <span class="req" aria-hidden="true">*</span></label><input id="f-dt-dias" type="number" min="1" max="30" (input)="set('dias', $event)" /></div>
            <div class="dw-field"><label for="f-dt-valor">Valor da diária (R$) <span class="req" aria-hidden="true">*</span></label><input id="f-dt-valor" type="number" min="0.01" step="0.01" (input)="set('valor', $event)" /></div>
          </div>
        </div>
        <div class="dw-foot"><button type="button" class="btn btn--ghost" (click)="fechar.emit()">Fechar</button><button type="submit" class="btn" [disabled]="enviando()">Conceder diária</button></div>
      </form>
    </app-drawer>
  `,
  styles: `
    .dt-lista { margin: 0 0 var(--s-3); padding: 0; list-style: none; display: grid; gap: var(--s-1); }
    .dt-lista li { display: flex; justify-content: space-between; gap: var(--s-2); align-items: baseline; flex-wrap: wrap; }
  `,
})
export class DiariasTfd implements OnInit {
  @Input({ required: true }) solicitacao!: SolicitacaoTransporteDto;
  @Output() fechar = new EventEmitter<void>();

  private readonly service = inject(FinanceiroService);

  protected readonly diarias = signal<DiariaTfdDto[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly f = signal<Record<string, string>>({ beneficiario: 'PACIENTE' });

  ngOnInit(): void {
    this.carregar();
  }

  private carregar(): void {
    this.service.diarias(this.solicitacao.uuid).subscribe({ next: (l) => this.diarias.set(l), error: (x: HttpErrorResponse) => this.erro.set(mensagem(x)) });
  }

  protected set(chave: string, ev: Event): void {
    this.f.set({ ...this.f(), [chave]: (ev.target as HTMLInputElement).value });
  }

  protected conceder(): void {
    const v = this.f();
    if (!v['cpf']?.trim() || !v['dias'] || !v['valor']) {
      this.erro.set('Informe o CPF, os dias e o valor da diária.');
      return;
    }
    this.enviando.set(true);
    this.erro.set(null);
    this.service.concederDiaria({ solicitacaoId: this.solicitacao.uuid, beneficiario: (v['beneficiario'] || 'PACIENTE') as BeneficiarioDiariaTfd, credorId: null,
      credorCpf: v['cpf'].trim(), dias: Number(v['dias']), valorDiaria: Number(v['valor']), observacao: null }).subscribe({
      next: (d) => {
        this.enviando.set(false);
        this.aviso.set(`Diária de ${this.moeda(d.valorTotal)} concedida a ${d.beneficiarioNome}.`);
        this.carregar();
      },
      error: (x: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set(mensagem(x));
      },
    });
  }

  protected moeda(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}

function mensagem(e: HttpErrorResponse): string {
  if (e.status === 400 && (e.error as { errors?: unknown } | undefined)?.errors) return 'Confira os campos.';
  return e.status === 403 ? 'Seu acesso não inclui esta operação.' : ((e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível concluir.');
}
