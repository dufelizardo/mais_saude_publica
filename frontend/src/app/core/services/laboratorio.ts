import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SuccessResponseDto } from '../models/profissional';
import {
  ColetaExameRequestDto,
  EmissaoLaudoDto,
  InsumoExameRequestDto,
  VerificacaoLaudoDto,
  ComunicacaoCriticoRequestDto,
  EntregaResultadoRequestDto,
  EtapaTrabalho,
  EvolucaoExameDto,
  ExameLaboratorialRequestDto,
  FaixaReferenciaExameRequestDto,
  LaudoImagemRequestDto,
  RealizacaoExameImagemRequestDto,
  ExameLaboratorialResponseDto,
  ItemTrabalhoExameDto,
  MotivoRejeicaoAmostra,
  PedidoExameRequestDto,
  PedidoExameResponseDto,
  PedidoExameResumoDto,
  ResultadoAvisoExameDto,
  ResultadoExameRequestDto,
  RetificacaoResultadoExameRequestDto,
} from '../models/laboratorio';

/** Laboratório assistencial (ADR-0093): catálogo, pedidos, coleta, resultado, liberação e retificação. */
@Injectable({ providedIn: 'root' })
export class LaboratorioService {
  private readonly http = inject(HttpClient);
  private readonly exameUrl = '/api/v1/exame-laboratorial';
  private readonly pedidoUrl = '/api/v1/pedido-exame';

  // ── Catálogo ──

  listarExames(): Observable<ExameLaboratorialResponseDto[]> {
    return this.http.get<ExameLaboratorialResponseDto[]>(`${this.exameUrl}/`);
  }

  criarExame(dto: ExameLaboratorialRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.exameUrl}/`, dto);
  }

  atualizarExame(uuid: string, dto: ExameLaboratorialRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.exameUrl}/${uuid}`, dto);
  }

  /** Faixas por sexo e idade (ADR-0180): devolvem o exame com as faixas. */
  adicionarFaixa(exameId: string, dto: FaixaReferenciaExameRequestDto): Observable<ExameLaboratorialResponseDto> {
    return this.http.post<ExameLaboratorialResponseDto>(`${this.exameUrl}/${exameId}/faixa`, dto);
  }

  atualizarFaixa(faixaId: string, dto: FaixaReferenciaExameRequestDto): Observable<ExameLaboratorialResponseDto> {
    return this.http.put<ExameLaboratorialResponseDto>(`${this.exameUrl}/faixa/${faixaId}`, dto);
  }

  removerFaixa(faixaId: string): Observable<ExameLaboratorialResponseDto> {
    return this.http.delete<ExameLaboratorialResponseDto>(`${this.exameUrl}/faixa/${faixaId}`);
  }

  /** Evolução de um exame do paciente, com as regras de acesso do prontuário (ADR-0180). */
  evolucao(pacienteId: string, exameId: string): Observable<EvolucaoExameDto> {
    return this.http.get<EvolucaoExameDto>(`/api/v1/prontuario/${pacienteId}/exame/${exameId}/evolucao`);
  }

  // ── Pedidos ──

  /** Sem valores; situação calculada. */
  listarPedidos(pacienteId?: string): Observable<PedidoExameResumoDto[]> {
    const params = pacienteId ? new HttpParams().set('pacienteId', pacienteId) : undefined;
    return this.http.get<PedidoExameResumoDto[]>(`${this.pedidoUrl}/`, { params });
  }

  /** Lista de trabalho por exame, urgente primeiro; sem a permissão da etapa, 403. */
  trabalho(etapa: EtapaTrabalho): Observable<ItemTrabalhoExameDto[]> {
    return this.http.get<ItemTrabalhoExameDto[]>(`${this.pedidoUrl}/trabalho`, { params: new HttpParams().set('etapa', etapa) });
  }

  /** Detalhe com resultados, amostras e eventos; leitura auditada. */
  buscarPedido(uuid: string): Observable<PedidoExameResponseDto> {
    return this.http.get<PedidoExameResponseDto>(`${this.pedidoUrl}/${uuid}`);
  }

  solicitar(dto: PedidoExameRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/`, dto);
  }

  coletar(pedidoId: string, dto: ColetaExameRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/${pedidoId}/coleta`, dto);
  }

  rejeitarAmostra(amostraId: string, profissionalMatricula: string, motivo: MotivoRejeicaoAmostra, observacao?: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/amostra/${amostraId}/rejeicao`, { profissionalMatricula, motivo, observacao });
  }

  registrarResultado(itemId: string, dto: ResultadoExameRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/resultado`, dto);
  }

  liberar(itemId: string, profissionalMatricula: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/liberacao`, { profissionalMatricula });
  }

  retificar(itemId: string, dto: RetificacaoResultadoExameRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/retificacao`, dto);
  }

  cancelar(itemId: string, profissionalMatricula: string, motivo: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/cancelamento`, { profissionalMatricula, motivo });
  }

  // ── Laudo em PDF, verificação e insumos (ADR-0183) ──

  /** O PDF da emissão vigente; o código de verificação vem no cabeçalho X-Codigo-Verificacao. */
  baixarLaudo(pedidoId: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.pedidoUrl}/${pedidoId}/laudo.pdf`, { observe: 'response', responseType: 'blob' });
  }

  emissoesDoLaudo(pedidoId: string): Observable<EmissaoLaudoDto[]> {
    return this.http.get<EmissaoLaudoDto[]>(`${this.pedidoUrl}/${pedidoId}/emissoes`);
  }

  /** Sem login. */
  verificarLaudo(codigo: string): Observable<VerificacaoLaudoDto> {
    return this.http.get<VerificacaoLaudoDto>(`/api/v1/publico/laudo/${encodeURIComponent(codigo)}`);
  }

  definirInsumos(exameId: string, insumos: InsumoExameRequestDto[]): Observable<ExameLaboratorialResponseDto> {
    return this.http.put<ExameLaboratorialResponseDto>(`${this.exameUrl}/${exameId}/insumos`, insumos);
  }

  // ── Exames de imagem (ADR-0182) ──

  realizarImagem(itemId: string, dto: RealizacaoExameImagemRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/realizacao`, dto);
  }

  laudar(itemId: string, dto: LaudoImagemRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/laudo`, dto);
  }

  adendo(itemId: string, dto: LaudoImagemRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/adendo`, dto);
  }

  // ── Ciência, comunicação do crítico e entrega (ADR-0181) ──

  /** Do profissional logado; sem login, a matrícula é obrigatória. Críticos primeiro, sem valores. */
  paraCiencia(matricula?: string): Observable<ResultadoAvisoExameDto[]> {
    const params = matricula ? new HttpParams().set('matricula', matricula) : undefined;
    return this.http.get<ResultadoAvisoExameDto[]>(`${this.pedidoUrl}/para-ciencia`, { params });
  }

  darCiencia(itemId: string, profissionalMatricula: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/ciencia`, { profissionalMatricula });
  }

  darCienciaDoPedido(pedidoId: string, profissionalMatricula: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/${pedidoId}/ciencia`, { profissionalMatricula });
  }

  criticosAComunicar(): Observable<ResultadoAvisoExameDto[]> {
    return this.http.get<ResultadoAvisoExameDto[]>(`${this.pedidoUrl}/criticos-a-comunicar`);
  }

  comunicarCritico(itemId: string, dto: ComunicacaoCriticoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/comunicacao-critico`, dto);
  }

  entregar(pedidoId: string, dto: EntregaResultadoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/${pedidoId}/entrega`, dto);
  }
}
