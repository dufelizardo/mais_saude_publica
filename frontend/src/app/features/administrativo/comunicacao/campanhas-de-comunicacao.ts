import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, of } from 'rxjs';
import {
  CampanhaComunicacaoDto,
  CanalComunicacaoDto,
  ConteudoEducativoDto,
  PUBLICOS_ALVO,
  PublicoAlvoCampanha,
  SITUACOES_CAMPANHA,
  SITUACOES_COMUNICADO,
} from '../../../core/models/comunicacao';
import { ErrorResponseDto } from '../../../core/models/profissional';
import { UnidadeSaudeResponseDto } from '../../../core/models/unidade-saude';
import { ComunicacaoService } from '../../../core/services/comunicacao';
import { TerritorioService } from '../../../core/services/territorio';
import { Drawer } from '../../../shared/drawer/drawer';
import { hojeIso } from '../../../shared/formato';

type Gaveta = { tipo: 'campanha'; item: CampanhaComunicacaoDto | null } | { tipo: 'detalhe'; item: CampanhaComunicacaoDto } | { tipo: 'encerrar' | 'cancelar'; item: CampanhaComunicacaoDto };

const PROFISSIONAIS: PublicoAlvoCampanha[] = ['PROFISSIONAIS_SAUDE', 'ACS'];

/** Aba Campanhas da Comunicação (ADR-0174): lista, cadastro, detalhe com conteúdos, comunicados e alcance, e o ciclo da campanha. */
@Component({
  selector: 'app-campanhas-de-comunicacao',
  imports: [ReactiveFormsModule, Drawer],
  template: `
    <section class="card" id="pn-campanhas" role="tabpanel" aria-labelledby="tab-campanhas">
      <div class="card__head">
        <div><h2>Campanhas de comunicação</h2><p class="sub">Objetivo, público, territórios, conteúdos e comunicados; distintas das campanhas operacionais de Programas</p></div>
        @if (podePublicar()) { <button type="button" class="btn btn--sm" (click)="abrirCampanha(null)">Nova campanha</button> }
      </div>
      @if (!campanhas().length) {
        <div class="empty"><strong>Nenhuma campanha de comunicação</strong></div>
      } @else {
        <div class="tbl-wrap">
          <table>
            <thead><tr><th scope="col">Campanha</th><th scope="col">Público</th><th scope="col">Período</th><th scope="col">Situação</th><th scope="col" class="num">Ações</th></tr></thead>
            <tbody>
              @for (c of campanhas(); track c.uuid) {
                <tr>
                  <td>
                    <span class="cell-main">{{ c.nome }}</span>
                    <span class="cell-sub">{{ c.objetivo }}</span>
                  </td>
                  <td>{{ publicos[c.publicoAlvo] }}@if (c.territorios.length) { <span class="cell-sub">{{ c.territorios.length }} território(s)</span> }</td>
                  <td class="mono">{{ dataBr(c.inicio) }} a {{ dataBr(c.fim) }}</td>
                  <td><span class="badge" [class]="'badge badge--' + classe(c)"><span class="dot" aria-hidden="true"></span> {{ situacoes[c.situacao] }}</span></td>
                  <td class="num">
                    <div class="row-actions">
                      <button type="button" class="btn btn--ghost btn--sm" (click)="abrir({ tipo: 'detalhe', item: c })" [attr.aria-label]="'Detalhes de ' + c.nome">Detalhes</button>
                      @if (c.situacao === 'PLANEJADA' && podePublicar()) {
                        <button type="button" class="btn btn--ghost btn--sm" (click)="abrirCampanha(c)" [attr.aria-label]="'Editar ' + c.nome">Editar</button>
                        <button type="button" class="btn btn--sm" (click)="iniciar(c)" [attr.aria-label]="'Iniciar ' + c.nome">Iniciar</button>
                      }
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </section>

    @if (gaveta(); as g) {
      <app-drawer [titulo]="titulo(g)" [larga]="g.tipo === 'detalhe' || g.tipo === 'campanha'" (fechar)="gaveta.set(null)">
        @if (g.tipo === 'detalhe') {
          @let c = g.item;
          <div class="dw-body">
            @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
            <dl class="ro ro3">
              <div><dt>Situação</dt><dd>{{ situacoes[c.situacao] }}</dd></div>
              <div><dt>Período</dt><dd>{{ dataBr(c.inicio) }} a {{ dataBr(c.fim) }}</dd></div>
              <div><dt>Público</dt><dd>{{ publicos[c.publicoAlvo] }}@if (c.faixaEtaria) { · {{ c.faixaEtaria }} }</dd></div>
              <div><dt>Abrangência</dt><dd>{{ c.unidadeNome ?? 'Toda a rede' }}@if (c.territorios.length) { · {{ nomes(c.territorios) }} }</dd></div>
              <div><dt>Temas</dt><dd>{{ c.temas.join(', ') || '—' }}</dd></div>
              <div><dt>Canais</dt><dd>{{ nomesDosCanais(c.canalIds) || '—' }}</dd></div>
              @if (c.programaNome) { <div><dt>Programa</dt><dd>{{ c.programaNome }}@if (c.campanhaOperacionalNome) { · {{ c.campanhaOperacionalNome }} }</dd></div> }
            </dl>
            <p>{{ c.objetivo }}</p>
            @if (c.descricao) { <p class="cell-sub" style="white-space: pre-line;">{{ c.descricao }}</p> }
            <h3 class="mo-grupo">Conteúdos</h3>
            <ul class="eq-prof">
              @for (x of c.conteudos; track x.grupoId) { <li><span class="cell-main">{{ x.titulo }}</span><span class="cell-sub">v{{ x.versao }} · validade {{ dataBr(x.validade) }}@if (x.vencido) { · vencido }</span></li> }
              @empty { <li class="cell-sub">Nenhum conteúdo.</li> }
            </ul>
            <h3 class="mo-grupo">Comunicados</h3>
            <ul class="eq-prof">
              @for (x of c.comunicados; track x.uuid) { <li><span class="cell-main">{{ x.titulo }}</span><span class="cell-sub">{{ situacoesComunicado[x.situacao] }} · {{ dataBr(x.inicio) }} a {{ dataBr(x.fim) }}</span></li> }
              @empty { <li class="cell-sub">Nenhum comunicado. Crie na aba Comunicados, escolhendo esta campanha.</li> }
            </ul>
            <h3 class="mo-grupo">Ações educativas</h3>
            <ul class="eq-prof" aria-label="Ações educativas da campanha">
              @for (x of c.acoesEducativas ?? []; track x.acaoId) {
                <li><span class="cell-main">{{ x.tema }} · {{ x.local ?? x.unidadeNome ?? '' }}</span><span class="cell-sub">{{ x.realizadaEm ? 'realizada em ' + dataBr(x.realizadaEm) : 'prevista para ' + dataBr(x.dataPrevista) }}@if (x.publicoAlcancado != null) { · {{ x.publicoAlcancado }} pessoa(s) }@if (x.materiaisDistribuidos) { · {{ x.materiaisDistribuidos }} material(is) }@if (x.participacao) { · participação {{ x.participacao.toLowerCase() }} }</span></li>
              }
              @empty { <li class="cell-sub">Nenhuma ação educativa. Ligue em Programas, na ação coletiva.</li> }
            </ul>
            <h3 class="mo-grupo">Alcance por canal</h3>
            <ul class="eq-prof" aria-label="Alcance por canal">
              @for (a of c.alcance; track a.canal) { <li><span class="cell-main">{{ a.canal }}</span><span class="cell-sub">{{ a.publicacoes }} publicação(ões) · {{ a.enviados }} enviado(s) · {{ a.falhas }} falha(s)</span></li> }
              @empty { <li class="cell-sub">Nada publicado ainda.</li> }
            </ul>
            @if (c.balanco) { <div class="box box--info"><div><strong>Balanço</strong><p style="margin: 4px 0 0; white-space: pre-line;">{{ c.balanco }}</p></div></div> }
            @if (c.motivoCancelamento) { <div class="box box--info"><div><strong>Cancelada</strong> · {{ c.motivoCancelamento }}</div></div> }
          </div>
          @if (podePublicar() && (c.situacao === 'PLANEJADA' || c.situacao === 'EM_ANDAMENTO')) {
            <footer class="dw-foot">
              <button type="button" class="btn btn--ghost" (click)="abrir({ tipo: 'cancelar', item: c })">Cancelar campanha</button>
              @if (c.situacao === 'EM_ANDAMENTO') { <button type="button" class="btn" (click)="abrir({ tipo: 'encerrar', item: c })">Encerrar</button> }
              @else { <button type="button" class="btn" (click)="iniciar(c)">Iniciar</button> }
            </footer>
          }
        } @else if (g.tipo === 'campanha') {
          <form class="dw-form" [formGroup]="form" (ngSubmit)="salvar()" novalidate>
            <div class="dw-body">
              @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
              <div class="dw-field">
                <label for="f-nomeCampanha">Nome <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-nomeCampanha" type="text" maxlength="150" formControlName="nome" placeholder="Ex.: Outubro Rosa" [attr.aria-invalid]="!!erro('nomeCampanha')" aria-describedby="e-nomeCampanha" />
                @if (erro('nomeCampanha')) { <p class="dw-err" id="e-nomeCampanha">{{ erro('nomeCampanha') }}</p> }
              </div>
              <div class="dw-field">
                <label for="f-objetivoCampanha">Objetivo <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <input id="f-objetivoCampanha" type="text" maxlength="500" formControlName="objetivo" placeholder="Ex.: estimular a mamografia entre 50 e 69 anos" [attr.aria-invalid]="!!erro('objetivoCampanha')" aria-describedby="e-objetivoCampanha" />
                @if (erro('objetivoCampanha')) { <p class="dw-err" id="e-objetivoCampanha">{{ erro('objetivoCampanha') }}</p> }
              </div>
              <div class="dw-field">
                <label for="f-descricaoCampanha">Descrição<span class="opt">opcional</span></label>
                <textarea id="f-descricaoCampanha" rows="3" maxlength="2000" formControlName="descricao"></textarea>
              </div>
              <div class="dw-row">
                <div class="dw-field">
                  <label for="f-inicioCampanha">Início <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                  <input id="f-inicioCampanha" type="date" formControlName="inicio" [attr.aria-invalid]="!!erro('inicioCampanha')" aria-describedby="e-inicioCampanha" />
                  @if (erro('inicioCampanha')) { <p class="dw-err" id="e-inicioCampanha">{{ erro('inicioCampanha') }}</p> }
                </div>
                <div class="dw-field">
                  <label for="f-fimCampanha">Fim <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                  <input id="f-fimCampanha" type="date" formControlName="fim" [attr.aria-invalid]="!!erro('fimCampanha')" aria-describedby="e-fimCampanha" />
                  @if (erro('fimCampanha')) { <p class="dw-err" id="e-fimCampanha">{{ erro('fimCampanha') }}</p> }
                </div>
              </div>
              <div class="dw-row">
                <div class="dw-field">
                  <label for="f-publicoAlvo">Público-alvo</label>
                  <select id="f-publicoAlvo" formControlName="publicoAlvo">
                    @for (p of listaPublicos; track p[0]) { <option [value]="p[0]">{{ p[1] }}</option> }
                  </select>
                </div>
                <div class="dw-field">
                  <label for="f-faixaEtariaCampanha">Faixa etária<span class="opt">opcional</span></label>
                  <input id="f-faixaEtariaCampanha" type="text" maxlength="60" formControlName="faixaEtaria" placeholder="Ex.: 50 a 69 anos" />
                </div>
              </div>
              <div class="dw-row">
                <div class="dw-field">
                  <label for="f-unidadeCampanha">Unidade<span class="opt">opcional · sem ela, toda a rede</span></label>
                  <select id="f-unidadeCampanha" formControlName="unidadeId">
                    <option value="">Toda a rede</option>
                    @for (u of unidades(); track u.uuid) { <option [value]="u.uuid">{{ u.nome }}</option> }
                  </select>
                </div>
                <div class="dw-field">
                  <label for="f-temasCampanha">Temas<span class="opt">separados por vírgula</span></label>
                  <input id="f-temasCampanha" type="text" formControlName="temas" placeholder="Ex.: câncer de mama, mamografia" />
                </div>
              </div>
              <fieldset class="perm-groups">
                <legend>Canais planejados <span class="cnt">{{ canaisEscolhidos().size }}</span></legend>
                <div class="perm-group">
                  @for (k of canaisAtivos(); track k.uuid) {
                    <label class="perm-check"><input type="checkbox" [checked]="canaisEscolhidos().has(k.uuid)" (change)="alternar(canaisEscolhidos, k.uuid)" /><span><span class="perm-check__txt">{{ k.nome }}</span></span></label>
                  }
                </div>
              </fieldset>
              <fieldset class="perm-groups">
                <legend>Conteúdos <span class="cnt">{{ conteudosEscolhidos().size }}</span></legend>
                <p class="dw-hint">Só os que passaram pela revisão técnica; a campanha mostra sempre a versão em vigor.</p>
                <div class="perm-group">
                  @for (x of conteudosRevisados(); track x.grupoId) {
                    <label class="perm-check"><input type="checkbox" [checked]="conteudosEscolhidos().has(x.grupoId)" (change)="alternar(conteudosEscolhidos, x.grupoId)" /><span><span class="perm-check__txt">{{ x.titulo }}</span><span class="perm-check__cod mono">v{{ x.versao }} · {{ x.tema }}</span></span></label>
                  } @empty { <p class="cell-sub" style="padding: 8px 12px;">Nenhum conteúdo revisado.</p> }
                </div>
              </fieldset>
              @if (territorios().length) {
                <fieldset class="perm-groups">
                  <legend>Territórios <span class="cnt">{{ territoriosEscolhidos().size }}</span></legend>
                  <div class="perm-group">
                    @for (t of territorios(); track t.uuid) {
                      <label class="perm-check"><input type="checkbox" [checked]="territoriosEscolhidos().has(t.uuid)" (change)="alternar(territoriosEscolhidos, t.uuid)" /><span><span class="perm-check__txt">{{ t.nome }}</span></span></label>
                    }
                  </div>
                </fieldset>
              }
            </div>
            <footer class="dw-foot">
              <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Cancelar</button>
              <button type="submit" class="btn" [disabled]="submitting()">{{ submitting() ? 'Salvando…' : 'Salvar' }}</button>
            </footer>
          </form>
        } @else {
          <form class="dw-form" [formGroup]="textoForm" (ngSubmit)="salvarTexto(g.tipo, g.item)" novalidate>
            <div class="dw-body">
              @if (erroApi()) { <div class="box box--err" role="alert"><strong>{{ erroApi() }}</strong></div> }
              <dl class="ro"><div><dt>Campanha</dt><dd>{{ g.item.nome }}</dd></div></dl>
              <div class="dw-field">
                <label for="f-textoCampanha">{{ g.tipo === 'encerrar' ? 'Balanço' : 'Motivo' }} <span class="req" aria-hidden="true">*</span><span class="sr-only"> (obrigatório)</span></label>
                <textarea id="f-textoCampanha" rows="4" [attr.maxlength]="g.tipo === 'encerrar' ? 2000 : 500" formControlName="texto" [attr.aria-invalid]="!!erro('textoCampanha')" aria-describedby="e-textoCampanha"></textarea>
                @if (erro('textoCampanha')) { <p class="dw-err" id="e-textoCampanha">{{ erro('textoCampanha') }}</p> }
              </div>
            </div>
            <footer class="dw-foot">
              <button type="button" class="btn btn--ghost" (click)="gaveta.set(null)">Voltar</button>
              <button type="submit" class="btn" [disabled]="submitting()">{{ g.tipo === 'encerrar' ? 'Encerrar' : 'Cancelar campanha' }}</button>
            </footer>
          </form>
        }
      </app-drawer>
    }
  `,
})
export class CampanhasDeComunicacao {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ComunicacaoService);
  private readonly territorioService = inject(TerritorioService);

  readonly campanhas = input.required<CampanhaComunicacaoDto[]>();
  readonly canais = input.required<CanalComunicacaoDto[]>();
  readonly conteudos = input.required<ConteudoEducativoDto[]>();
  readonly unidades = input.required<UnidadeSaudeResponseDto[]>();
  readonly podePublicar = input(false);
  readonly salvou = output<{ titulo: string; detalhe: string }>();

  protected readonly publicos = PUBLICOS_ALVO;
  protected readonly listaPublicos = Object.entries(PUBLICOS_ALVO) as [PublicoAlvoCampanha, string][];
  protected readonly situacoes = SITUACOES_CAMPANHA;
  protected readonly situacoesComunicado = SITUACOES_COMUNICADO;
  protected readonly gaveta = signal<Gaveta | null>(null);
  protected readonly territorios = signal<{ uuid: string; nome: string }[]>([]);
  protected readonly canaisEscolhidos = signal(new Set<string>());
  protected readonly conteudosEscolhidos = signal(new Set<string>());
  protected readonly territoriosEscolhidos = signal(new Set<string>());
  protected readonly submitting = signal(false);
  protected readonly erroApi = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});
  private territoriosCarregados = false;

  protected readonly form = this.fb.nonNullable.group({
    nome: [''],
    objetivo: [''],
    descricao: [''],
    inicio: [''],
    fim: [''],
    publicoAlvo: ['POPULACAO_GERAL' as PublicoAlvoCampanha],
    faixaEtaria: [''],
    unidadeId: [''],
    temas: [''],
  });
  protected readonly textoForm = this.fb.nonNullable.group({ texto: [''] });

  protected readonly canaisAtivos = computed(() => this.canais().filter((k) => k.ativo));

  /** Por grupo, a versão revisada mais recente, como no servidor. */
  protected readonly conteudosRevisados = computed(() => {
    const porGrupo = new Map<string, ConteudoEducativoDto>();
    for (const c of this.conteudos()) {
      if (!['APROVADO', 'PUBLICADO', 'EXPIRADO'].includes(c.situacao)) continue;
      const atual = porGrupo.get(c.grupoId);
      if (!atual || c.versao > atual.versao) porGrupo.set(c.grupoId, c);
    }
    return [...porGrupo.values()].sort((a, b) => a.titulo.localeCompare(b.titulo));
  });

  protected dataBr(iso: string): string {
    return iso.slice(0, 10).split('-').reverse().join('/');
  }

  protected classe(c: CampanhaComunicacaoDto): string {
    return { PLANEJADA: 'info', EM_ANDAMENTO: 'ok', ENCERRADA: 'muted', CANCELADA: 'muted' }[c.situacao];
  }

  protected nomes(l: { nome: string }[]): string {
    return l.map((x) => x.nome).join(', ');
  }

  protected nomesDosCanais(ids: string[]): string {
    return this.canais().filter((k) => ids.includes(k.uuid)).map((k) => k.nome).join(', ');
  }

  protected titulo(g: Gaveta): string {
    if (g.tipo === 'campanha') return g.item ? 'Editar campanha' : 'Nova campanha';
    if (g.tipo === 'detalhe') return g.item.nome;
    return g.tipo === 'encerrar' ? 'Encerrar campanha' : 'Cancelar campanha';
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  protected abrir(g: Gaveta): void {
    this.erroApi.set(null);
    this.erros.set({});
    this.textoForm.reset();
    this.gaveta.set(g);
  }

  protected abrirCampanha(item: CampanhaComunicacaoDto | null): void {
    if (!this.territoriosCarregados) {
      this.territoriosCarregados = true;
      this.territorioService.arvore().pipe(catchError(() => of(null))).subscribe((a) =>
        this.territorios.set((a?.segmentos ?? []).flatMap((s) => s.territorios).filter((t) => t.ativo).map((t) => ({ uuid: t.uuid, nome: t.nome }))));
    }
    this.form.reset({
      nome: item?.nome ?? '',
      objetivo: item?.objetivo ?? '',
      descricao: item?.descricao ?? '',
      inicio: item?.inicio ?? hojeIso(),
      fim: item?.fim ?? '',
      publicoAlvo: item?.publicoAlvo ?? 'POPULACAO_GERAL',
      faixaEtaria: item?.faixaEtaria ?? '',
      unidadeId: item?.unidadeId ?? '',
      temas: item?.temas.join(', ') ?? '',
    });
    this.canaisEscolhidos.set(new Set(item?.canalIds ?? []));
    this.conteudosEscolhidos.set(new Set(item?.conteudos.map((c) => c.grupoId) ?? []));
    this.territoriosEscolhidos.set(new Set(item?.territorios.map((t) => t.uuid) ?? []));
    this.abrir({ tipo: 'campanha', item });
  }

  protected alternar(alvo: typeof this.canaisEscolhidos, id: string): void {
    const novo = new Set(alvo());
    if (novo.has(id)) novo.delete(id);
    else novo.add(id);
    alvo.set(novo);
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
    if (g?.tipo !== 'campanha' || this.submitting()) return;
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.nome.trim()) erros['nomeCampanha'] = 'Informe o nome.';
    if (!v.objetivo.trim()) erros['objetivoCampanha'] = 'Informe o objetivo.';
    if (!v.inicio) erros['inicioCampanha'] = 'Informe o início.';
    if (!v.fim) erros['fimCampanha'] = 'Informe o fim.';
    else if (v.inicio && v.fim < v.inicio) erros['fimCampanha'] = 'O fim vem depois do início.';
    if (!this.validar(erros)) return;
    const portal = this.canais().find((k) => k.tipo === 'PORTAL');
    const canais = [...this.canaisEscolhidos()].filter((id) => !(PROFISSIONAIS.includes(v.publicoAlvo) && portal?.uuid === id));
    const dto = {
      nome: v.nome.trim(),
      objetivo: v.objetivo.trim(),
      descricao: v.descricao.trim() || null,
      inicio: v.inicio,
      fim: v.fim,
      publicoAlvo: v.publicoAlvo,
      faixaEtaria: v.faixaEtaria.trim() || null,
      unidadeId: v.unidadeId || null,
      temas: v.temas.split(',').map((t) => t.trim()).filter((t) => t),
      canalIds: canais,
      conteudoGrupoIds: [...this.conteudosEscolhidos()],
      territorioIds: [...this.territoriosEscolhidos()],
    };
    this.enviar(g.item ? this.service.atualizarCampanha(g.item.uuid, dto) : this.service.criarCampanha(dto), g.item ? 'Campanha atualizada' : 'Campanha planejada');
  }

  protected iniciar(c: CampanhaComunicacaoDto): void {
    this.erroApi.set(null);
    this.enviar(this.service.iniciarCampanha(c.uuid), 'Campanha iniciada', (e) => {
      if (this.gaveta()) this.erroApi.set(e);
      else this.salvou.emit({ titulo: 'Não foi possível iniciar', detalhe: e });
    });
  }

  protected salvarTexto(tipo: 'encerrar' | 'cancelar', c: CampanhaComunicacaoDto): void {
    const texto = this.textoForm.getRawValue().texto.trim();
    if (!this.validar(texto ? {} : { textoCampanha: tipo === 'encerrar' ? 'Informe o balanço.' : 'Informe o motivo.' })) return;
    this.enviar(tipo === 'encerrar' ? this.service.encerrarCampanha(c.uuid, texto) : this.service.cancelarCampanha(c.uuid, texto),
      tipo === 'encerrar' ? 'Campanha encerrada' : 'Campanha cancelada');
  }

  private enviar(req: Observable<CampanhaComunicacaoDto>, titulo: string, aoFalhar?: (mensagem: string) => void): void {
    this.submitting.set(true);
    req.subscribe({
      next: (c) => {
        this.submitting.set(false);
        this.gaveta.set(null);
        this.salvou.emit({ titulo, detalhe: c.nome });
      },
      error: (e: HttpErrorResponse) => {
        this.submitting.set(false);
        const mensagem = (e.error as ErrorResponseDto | undefined)?.message ?? 'Não foi possível salvar. Tente novamente.';
        if (aoFalhar) aoFalhar(mensagem);
        else this.erroApi.set(mensagem);
      },
    });
  }
}
