import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DocumentoDto } from '../models/documento';
import {
  CanalDenuncia,
  DenunciaSanitariaDto,
  EstabelecimentoDetalheDto,
  EstabelecimentoReguladoDto,
  EstabelecimentoRequest,
  InspecaoRequest,
  InspecaoSanitariaDto,
  LicencaSanitariaDto,
  FaseProcessoSanitario,
  FormaCiencia,
  InterdicaoDto,
  MedidaSanitariaDto,
  PendenciasVisaDto,
  ProcessoSanitarioDto,
  TipoMedidaSanitaria,
  VerificacaoLicencaDto,
  VerificacaoTermoDto,
  RoteiroInspecaoDto,
  SituacaoDenuncia,
} from '../models/vigilancia-sanitaria';

/** Vigilância Sanitária, fatia V3a da Vigilância (ADR-0199). */
@Injectable({ providedIn: 'root' })
export class VigilanciaSanitariaService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/vigilancia-sanitaria';

  estabelecimentos(busca = ''): Observable<EstabelecimentoReguladoDto[]> {
    const params = busca.trim() ? new HttpParams().set('busca', busca.trim()) : new HttpParams();
    return this.http.get<EstabelecimentoReguladoDto[]>(`${this.api}/estabelecimentos`, { params });
  }

  estabelecimento(uuid: string): Observable<EstabelecimentoDetalheDto> {
    return this.http.get<EstabelecimentoDetalheDto>(`${this.api}/estabelecimento/${uuid}`);
  }

  criarEstabelecimento(dto: EstabelecimentoRequest): Observable<EstabelecimentoReguladoDto> {
    return this.http.post<EstabelecimentoReguladoDto>(`${this.api}/estabelecimento`, dto);
  }

  atualizarEstabelecimento(uuid: string, dto: EstabelecimentoRequest): Observable<EstabelecimentoReguladoDto> {
    return this.http.put<EstabelecimentoReguladoDto>(`${this.api}/estabelecimento/${uuid}`, dto);
  }

  licencas(): Observable<LicencaSanitariaDto[]> {
    return this.http.get<LicencaSanitariaDto[]>(`${this.api}/licencas`);
  }

  concederLicenca(estabelecimentoId: string, dto: { emitidaEm: string | null; validade: string | null; observacao: string | null }): Observable<LicencaSanitariaDto> {
    return this.http.post<LicencaSanitariaDto>(`${this.api}/estabelecimento/${estabelecimentoId}/licenca`, dto);
  }

  roteiros(): Observable<RoteiroInspecaoDto[]> {
    return this.http.get<RoteiroInspecaoDto[]>(`${this.api}/roteiros`);
  }

  denuncias(situacao?: SituacaoDenuncia): Observable<DenunciaSanitariaDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : new HttpParams();
    return this.http.get<DenunciaSanitariaDto[]>(`${this.api}/denuncias`, { params });
  }

  registrarDenuncia(dto: {
    canal: CanalDenuncia; descricao: string; estabelecimentoId: string | null; estabelecimentoDescricao: string | null; anonima: boolean;
    denuncianteNome: string | null; denuncianteContato: string | null;
  }): Observable<DenunciaSanitariaDto> {
    return this.http.post<DenunciaSanitariaDto>(`${this.api}/denuncia`, dto);
  }

  concluirDenuncia(uuid: string, situacao: SituacaoDenuncia, conclusao: string): Observable<DenunciaSanitariaDto> {
    return this.http.post<DenunciaSanitariaDto>(`${this.api}/denuncia/${uuid}/conclusao`, { situacao, conclusao });
  }

  inspecoes(estabelecimentoId?: string): Observable<InspecaoSanitariaDto[]> {
    const params = estabelecimentoId ? new HttpParams().set('estabelecimentoId', estabelecimentoId) : new HttpParams();
    return this.http.get<InspecaoSanitariaDto[]>(`${this.api}/inspecoes`, { params });
  }

  registrarInspecao(dto: InspecaoRequest): Observable<InspecaoSanitariaDto> {
    return this.http.post<InspecaoSanitariaDto>(`${this.api}/inspecao`, dto);
  }

  termo(uuid: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.api}/inspecao/${uuid}/termo.pdf`, { responseType: 'blob', observe: 'response' });
  }

  pendencias(): Observable<PendenciasVisaDto> {
    return this.http.get<PendenciasVisaDto>(`${this.api}/pendencias`);
  }

  anexar(dono: 'estabelecimento' | 'inspecao', uuid: string, dados: { tipoId: string; titulo: string; dataDocumento: string }, arquivo: File): Observable<DocumentoDto[]> {
    const corpo = new FormData();
    corpo.set('tipoId', dados.tipoId);
    corpo.set('titulo', dados.titulo);
    if (dados.dataDocumento) corpo.set('dataDocumento', dados.dataDocumento);
    corpo.set('arquivo', arquivo);
    return this.http.post<DocumentoDto[]>(`${this.api}/${dono}/${uuid}/documento`, corpo);
  }

  // ── Processo sanitário e medidas (ADR-0200) ──

  processos(filtro: { estabelecimentoId?: string; fase?: FaseProcessoSanitario } = {}): Observable<ProcessoSanitarioDto[]> {
    let params = new HttpParams();
    if (filtro.estabelecimentoId) params = params.set('estabelecimentoId', filtro.estabelecimentoId);
    if (filtro.fase) params = params.set('fase', filtro.fase);
    return this.http.get<ProcessoSanitarioDto[]>(`${this.api}/processos`, { params });
  }

  processo(uuid: string): Observable<ProcessoSanitarioDto> {
    return this.http.get<ProcessoSanitarioDto>(`${this.api}/processo/${uuid}`);
  }

  lavrar(dto: { estabelecimentoId: string; inspecaoId: string | null; irregularidades: string[]; dispositivoLegal: string; descricao: string; gravidade: string | null;
    dataLavratura: string | null; fiscais: string[] }): Observable<ProcessoSanitarioDto> {
    return this.http.post<ProcessoSanitarioDto>(`${this.api}/processo`, dto);
  }

  acao(uuid: string, acao: 'ciencia' | 'defesa' | 'decisao' | 'recurso' | 'julgamento-recurso' | 'encerramento', corpo: object = {}): Observable<ProcessoSanitarioDto> {
    return this.http.post<ProcessoSanitarioDto>(`${this.api}/processo/${uuid}/${acao}`, corpo);
  }

  medidas(filtro: { estabelecimentoId?: string; situacao?: 'VIGENTE' | 'ENCERRADA' } = {}): Observable<MedidaSanitariaDto[]> {
    let params = new HttpParams();
    if (filtro.estabelecimentoId) params = params.set('estabelecimentoId', filtro.estabelecimentoId);
    if (filtro.situacao) params = params.set('situacao', filtro.situacao);
    return this.http.get<MedidaSanitariaDto[]>(`${this.api}/medidas`, { params });
  }

  registrarMedida(dto: { estabelecimentoId: string; tipo: TipoMedidaSanitaria; total: boolean | null; descricao: string; produto: string | null; lote: string | null;
    quantidade: string | null; localGuarda: string | null; inicio: string | null; fimPrevisto: string | null }): Observable<MedidaSanitariaDto> {
    return this.http.post<MedidaSanitariaDto>(`${this.api}/medida`, dto);
  }

  encerrarMedida(uuid: string, motivo: string, data: string | null): Observable<MedidaSanitariaDto> {
    return this.http.post<MedidaSanitariaDto>(`${this.api}/medida/${uuid}/encerramento`, { motivo, data });
  }

  interdicoes(): Observable<InterdicaoDto[]> {
    return this.http.get<InterdicaoDto[]>(`${this.api}/interdicoes`);
  }

  verificarTermo(codigo: string): Observable<VerificacaoTermoDto> {
    return this.http.get<VerificacaoTermoDto>(`/api/v1/publico/visa/verificacao/${encodeURIComponent(codigo)}`);
  }

  verificarLicenca(numero: string): Observable<VerificacaoLicencaDto> {
    return this.http.get<VerificacaoLicencaDto>('/api/v1/publico/visa/licenca', { params: new HttpParams().set('numero', numero) });
  }

  abrirDocumento(versaoId: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.api}/documento/versao/${versaoId}/arquivo`, { responseType: 'blob', observe: 'response' });
  }
}
