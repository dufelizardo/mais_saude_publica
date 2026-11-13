import { Component, effect, input, signal, untracked } from '@angular/core';
import {
  EventoTrabalhoRequestDto,
  RESPOSTAS_CAT,
  RespostaCat,
  SITUACOES_TRABALHO,
  SituacaoMercadoTrabalho,
  TIPOS_ACIDENTE,
  TipoAcidenteTrabalho,
  TrabalhoDto,
} from '../../core/models/vigilancia';

interface Campos {
  ocupacaoCbo: string;
  ocupacaoDescricao: string;
  situacaoTrabalho: SituacaoMercadoTrabalho | '';
  empregadorDocumento: string;
  empregadorNome: string;
  empregadorCnae: string;
  localEvento: string;
  tipoAcidente: TipoAcidenteTrabalho | '';
  horasAposInicioJornada: string;
  cat: RespostaCat | '';
  outrosTrabalhadoresAtingidos: string;
  obito: boolean;
}

const VAZIO: Campos = {
  ocupacaoCbo: '', ocupacaoDescricao: '', situacaoTrabalho: '', empregadorDocumento: '', empregadorNome: '', empregadorCnae: '', localEvento: '',
  tipoAcidente: '', horasAposInicioJornada: '', cat: '', outrosTrabalhadoresAtingidos: '', obito: false,
};

/**
 * O bloco do trabalho (ADR-0207): a ocupação e a situação na data do evento, o empregador, o local e, só no acidente, o tipo, a
 * jornada, a CAT, os atingidos e o óbito. Usado no formulário da notificação e na correção pela investigação; quem usa chama
 * `validar()` e `valor()`.
 */
@Component({
  selector: 'app-trabalho-form',
  template: `
    <fieldset class="dw-field" aria-label="Trabalho">
      <legend>Trabalho</legend>
      <div class="dw-row">
        <div class="dw-field">
          <label for="f-ocupacaoCbo">Ocupação (CBO) <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
          <input id="f-ocupacaoCbo" type="text" class="mono" maxlength="8" placeholder="Ex.: 7152-10" [value]="v().ocupacaoCbo" (input)="mudar('ocupacaoCbo', $any($event.target).value)"
            [attr.aria-invalid]="!!erros()['ocupacaoCbo']" aria-describedby="e-ocupacaoCbo" />
          @if (erros()['ocupacaoCbo']) { <p class="dw-err" id="e-ocupacaoCbo">{{ erros()['ocupacaoCbo'] }}</p> }
        </div>
        <div class="dw-field">
          <label for="f-ocupacaoDescricao">Descrição da ocupação <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
          <input id="f-ocupacaoDescricao" type="text" maxlength="200" placeholder="Ex.: Pedreiro" [value]="v().ocupacaoDescricao" (input)="mudar('ocupacaoDescricao', $any($event.target).value)"
            [attr.aria-invalid]="!!erros()['ocupacaoDescricao']" aria-describedby="e-ocupacaoDescricao" />
          @if (erros()['ocupacaoDescricao']) { <p class="dw-err" id="e-ocupacaoDescricao">{{ erros()['ocupacaoDescricao'] }}</p> }
        </div>
      </div>
      <div class="dw-field">
        <label for="f-situacaoTrabalho">Situação no mercado de trabalho <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
        <select id="f-situacaoTrabalho" [value]="v().situacaoTrabalho" (change)="mudar('situacaoTrabalho', $any($event.target).value)"
          [attr.aria-invalid]="!!erros()['situacaoTrabalho']" aria-describedby="e-situacaoTrabalho">
          <option value="">Escolha…</option>
          @for (s of listaSituacoes; track s) { <option [value]="s">{{ situacoes[s] }}</option> }
        </select>
        @if (erros()['situacaoTrabalho']) { <p class="dw-err" id="e-situacaoTrabalho">{{ erros()['situacaoTrabalho'] }}</p> }
      </div>
      <div class="dw-row">
        <div class="dw-field">
          <label for="f-empregadorDocumento">CPF ou CNPJ do empregador<span class="opt">opcional</span></label>
          <input id="f-empregadorDocumento" type="text" class="mono" maxlength="18" [value]="v().empregadorDocumento" (input)="mudar('empregadorDocumento', $any($event.target).value)"
            [attr.aria-invalid]="!!erros()['empregadorDocumento']" aria-describedby="e-empregadorDocumento" />
          @if (erros()['empregadorDocumento']) { <p class="dw-err" id="e-empregadorDocumento">{{ erros()['empregadorDocumento'] }}</p> }
        </div>
        <div class="dw-field">
          <label for="f-empregadorCnae">CNAE<span class="opt">opcional</span></label>
          <input id="f-empregadorCnae" type="text" class="mono" maxlength="10" placeholder="Ex.: 4120-4/00" [value]="v().empregadorCnae" (input)="mudar('empregadorCnae', $any($event.target).value)"
            [attr.aria-invalid]="!!erros()['empregadorCnae']" aria-describedby="e-empregadorCnae" />
          @if (erros()['empregadorCnae']) { <p class="dw-err" id="e-empregadorCnae">{{ erros()['empregadorCnae'] }}</p> }
        </div>
      </div>
      <div class="dw-field">
        <label for="f-empregadorNome">Empregador<span class="opt">{{ v().empregadorDocumento.trim() ? 'com o documento, se não cadastrado na vigilância sanitária' : 'opcional' }}</span></label>
        <input id="f-empregadorNome" type="text" maxlength="200" [value]="v().empregadorNome" (input)="mudar('empregadorNome', $any($event.target).value)" />
      </div>
      <div class="dw-field">
        <label for="f-localEvento">Local do evento<span class="opt">opcional</span></label>
        <input id="f-localEvento" type="text" maxlength="500" placeholder="Endereço do local de trabalho" [value]="v().localEvento" (input)="mudar('localEvento', $any($event.target).value)" />
      </div>
      @if (acidente()) {
        <div class="dw-row">
          <div class="dw-field">
            <label for="f-tipoAcidente">Tipo do acidente@if (grave()) { <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span> } @else { <span class="opt">opcional</span> }</label>
            <select id="f-tipoAcidente" [value]="v().tipoAcidente" (change)="mudar('tipoAcidente', $any($event.target).value)"
              [attr.aria-invalid]="!!erros()['tipoAcidente']" aria-describedby="e-tipoAcidente">
              <option value="">Escolha…</option>
              @for (t of listaTipos; track t) { <option [value]="t">{{ tipos[t] }}</option> }
            </select>
            @if (erros()['tipoAcidente']) { <p class="dw-err" id="e-tipoAcidente">{{ erros()['tipoAcidente'] }}</p> }
          </div>
          <div class="dw-field">
            <label for="f-cat">CAT<span class="opt">opcional</span></label>
            <select id="f-cat" [value]="v().cat" (change)="mudar('cat', $any($event.target).value)">
              <option value="">—</option>
              @for (c of listaCat; track c) { <option [value]="c">{{ cats[c] }}</option> }
            </select>
          </div>
        </div>
        <div class="dw-row">
          <div class="dw-field">
            <label for="f-horasJornada">Horas depois do início da jornada<span class="opt">opcional</span></label>
            <input id="f-horasJornada" type="number" min="0" max="24" [value]="v().horasAposInicioJornada" (input)="mudar('horasAposInicioJornada', $any($event.target).value)"
              [attr.aria-invalid]="!!erros()['horasJornada']" aria-describedby="e-horasJornada" />
            @if (erros()['horasJornada']) { <p class="dw-err" id="e-horasJornada">{{ erros()['horasJornada'] }}</p> }
          </div>
          <div class="dw-field">
            <label for="f-outrosAtingidos">Outros trabalhadores atingidos<span class="opt">opcional</span></label>
            <input id="f-outrosAtingidos" type="number" min="0" [value]="v().outrosTrabalhadoresAtingidos" (input)="mudar('outrosTrabalhadoresAtingidos', $any($event.target).value)"
              [attr.aria-invalid]="!!erros()['outrosAtingidos']" aria-describedby="e-outrosAtingidos" />
            @if (erros()['outrosAtingidos']) { <p class="dw-err" id="e-outrosAtingidos">{{ erros()['outrosAtingidos'] }}</p> }
          </div>
        </div>
        <div class="dw-field"><label class="switch" for="f-obito"><span><b style="font-weight: 500;">Óbito</b></span>
          <input type="checkbox" role="switch" id="f-obito" [checked]="v().obito" (change)="mudar('obito', $any($event.target).checked)" /></label></div>
        @if (acidenteTrabalhoId()) { <div class="box box--info"><div>Ligada ao acidente do servidor registrado no RH (a CAT continua com o RH).</div></div> }
      }
    </fieldset>
  `,
})
export class TrabalhoForm {
  /** O bloco já gravado, na correção; nulo na notificação nova. */
  readonly inicial = input<TrabalhoDto | null>(null);
  /** O agravo é de acidente de trabalho: mostra tipo, jornada, CAT, atingidos e óbito. */
  readonly acidente = input(false);
  /** O acidente de trabalho grave pede o tipo do acidente. */
  readonly grave = input(false);
  /** O acidente do servidor no RH, quando a notificação parte dele. */
  readonly acidenteTrabalhoId = input<string | null>(null);

  protected readonly situacoes = SITUACOES_TRABALHO;
  protected readonly listaSituacoes = Object.keys(SITUACOES_TRABALHO) as SituacaoMercadoTrabalho[];
  protected readonly tipos = TIPOS_ACIDENTE;
  protected readonly listaTipos = Object.keys(TIPOS_ACIDENTE) as TipoAcidenteTrabalho[];
  protected readonly cats = RESPOSTAS_CAT;
  protected readonly listaCat = Object.keys(RESPOSTAS_CAT) as RespostaCat[];

  protected readonly v = signal<Campos>({ ...VAZIO });
  protected readonly erros = signal<Record<string, string>>({});

  constructor() {
    effect(() => {
      const t = this.inicial();
      untracked(() => this.v.set(t ? {
        ocupacaoCbo: t.ocupacaoCbo, ocupacaoDescricao: t.ocupacaoDescricao, situacaoTrabalho: t.situacaoTrabalho,
        empregadorDocumento: t.empregadorDocumento ?? '', empregadorNome: t.empregadorNome ?? '', empregadorCnae: t.empregadorCnae ?? '',
        localEvento: t.localEvento ?? '', tipoAcidente: t.tipoAcidente ?? '', horasAposInicioJornada: t.horasAposInicioJornada != null ? String(t.horasAposInicioJornada) : '',
        cat: t.cat ?? '', outrosTrabalhadoresAtingidos: t.outrosTrabalhadoresAtingidos != null ? String(t.outrosTrabalhadoresAtingidos) : '', obito: t.obito,
      } : { ...VAZIO }));
    });
  }

  protected mudar<K extends keyof Campos>(campo: K, valor: Campos[K]): void {
    this.v.update((x) => ({ ...x, [campo]: valor }));
  }

  /** Confere os campos; com erro, mostra e põe o foco no primeiro. */
  validar(): boolean {
    const x = this.v();
    const e: Record<string, string> = {};
    if (x.ocupacaoCbo.replace(/\D/g, '').length !== 6) e['ocupacaoCbo'] = 'O código da CBO tem seis dígitos (ex.: 7152-10).';
    if (!x.ocupacaoDescricao.trim()) e['ocupacaoDescricao'] = 'Informe a ocupação.';
    if (!x.situacaoTrabalho) e['situacaoTrabalho'] = 'Escolha a situação.';
    const doc = x.empregadorDocumento.replace(/\D/g, '');
    if (x.empregadorDocumento.trim() && doc.length !== 11 && doc.length !== 14) e['empregadorDocumento'] = 'Informe um CPF ou um CNPJ.';
    if (x.empregadorCnae.trim() && x.empregadorCnae.replace(/\D/g, '').length !== 7) e['empregadorCnae'] = 'O CNAE tem sete dígitos (ex.: 4120-4/00).';
    if (this.acidente()) {
      if (this.grave() && !x.tipoAcidente) e['tipoAcidente'] = 'Escolha o tipo do acidente.';
      const h = x.horasAposInicioJornada === '' ? null : Number(x.horasAposInicioJornada);
      if (h != null && (!Number.isInteger(h) || h < 0 || h > 24)) e['horasJornada'] = 'De 0 a 24 horas.';
      const o = x.outrosTrabalhadoresAtingidos === '' ? null : Number(x.outrosTrabalhadoresAtingidos);
      if (o != null && (!Number.isInteger(o) || o < 0)) e['outrosAtingidos'] = 'Zero ou mais.';
    }
    this.erros.set(e);
    const campos = Object.keys(e);
    if (campos.length) {
      const ids: Record<string, string> = { horasJornada: 'f-horasJornada', outrosAtingidos: 'f-outrosAtingidos' };
      setTimeout(() => document.getElementById(ids[campos[0]] ?? 'f-' + campos[0])?.focus());
      return false;
    }
    return true;
  }

  valor(): EventoTrabalhoRequestDto {
    const x = this.v();
    const acidente = this.acidente();
    return {
      ocupacaoCbo: x.ocupacaoCbo.trim(), ocupacaoDescricao: x.ocupacaoDescricao.trim(), situacaoTrabalho: x.situacaoTrabalho as SituacaoMercadoTrabalho,
      empregadorDocumento: x.empregadorDocumento.trim() || null, empregadorNome: x.empregadorNome.trim() || null, empregadorCnae: x.empregadorCnae.trim() || null,
      localEvento: x.localEvento.trim() || null,
      tipoAcidente: acidente && x.tipoAcidente ? x.tipoAcidente : null,
      horasAposInicioJornada: acidente && x.horasAposInicioJornada !== '' ? Number(x.horasAposInicioJornada) : null,
      cat: acidente && x.cat ? x.cat : null,
      outrosTrabalhadoresAtingidos: acidente && x.outrosTrabalhadoresAtingidos !== '' ? Number(x.outrosTrabalhadoresAtingidos) : null,
      obito: acidente ? x.obito : null,
      acidenteTrabalhoId: acidente ? (this.acidenteTrabalhoId() ?? this.inicial()?.acidenteTrabalhoId ?? null) : null,
    };
  }
}
