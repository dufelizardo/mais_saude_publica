import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  OcorrenciaDto, OrdemDto, PainelComprasDto, SituacaoOcorrenciaFornecedor, TipoOcorrenciaFornecedor,
  ContratacaoDto, InstrumentoDto, ModalidadeContratacao, ResultadoItemContratacao, SituacaoContratacao, TipoAditivoContratual, TipoInstrumentoContratual,
  DecisaoSolicitacaoCompra, FonteCotacao, FornecedorDto, SituacaoSolicitacaoCompra, SolicitacaoCompraDto, SolicitacaoCompraRequest, SugestaoCompraDto,
  TipoCertidaoFornecedor, TipoSancaoFornecedor,
} from '../models/compras';

/** Compras: CP1 (ADR-0212), CP2 contratação e contratos (ADR-0213) e CP3 entregas e painel (ADR-0214). */
@Injectable({ providedIn: 'root' })
export class ComprasService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/compras';

  fornecedores(busca?: string): Observable<FornecedorDto[]> {
    const params = busca ? new HttpParams().set('busca', busca) : new HttpParams();
    return this.http.get<FornecedorDto[]>(`${this.api}/fornecedores`, { params });
  }

  fornecedor(uuid: string): Observable<FornecedorDto> {
    return this.http.get<FornecedorDto>(`${this.api}/fornecedor/${uuid}`);
  }

  cadastrarFornecedor(dto: { documento: string; razaoSocial: string; nomeFantasia: string | null; email: string | null; telefone: string | null;
    endereco: string | null }): Observable<FornecedorDto> {
    return this.http.post<FornecedorDto>(`${this.api}/fornecedor`, dto);
  }

  atualizarFornecedor(uuid: string, dto: { documento: string; razaoSocial: string; nomeFantasia: string | null; email: string | null;
    telefone: string | null; endereco: string | null; ativo: boolean }): Observable<FornecedorDto> {
    return this.http.put<FornecedorDto>(`${this.api}/fornecedor/${uuid}`, dto);
  }

  certidao(uuid: string, dto: { tipo: TipoCertidaoFornecedor; numero: string | null; emitidaEm: string; validaAte: string }): Observable<FornecedorDto> {
    return this.http.post<FornecedorDto>(`${this.api}/fornecedor/${uuid}/certidao`, dto);
  }

  sancao(uuid: string, dto: { tipo: TipoSancaoFornecedor; inicio: string; fim: string | null; valor: number | null; motivo: string;
    processo: string | null }): Observable<FornecedorDto> {
    return this.http.post<FornecedorDto>(`${this.api}/fornecedor/${uuid}/sancao`, dto);
  }

  revogarSancao(uuid: string, motivo: string): Observable<FornecedorDto> {
    return this.http.post<FornecedorDto>(`${this.api}/sancao/${uuid}/revogacao`, { motivo });
  }

  solicitacoes(situacao?: SituacaoSolicitacaoCompra): Observable<SolicitacaoCompraDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : new HttpParams();
    return this.http.get<SolicitacaoCompraDto[]>(`${this.api}/solicitacoes`, { params });
  }

  solicitacao(uuid: string): Observable<SolicitacaoCompraDto> {
    return this.http.get<SolicitacaoCompraDto>(`${this.api}/solicitacao/${uuid}`);
  }

  criar(dto: SolicitacaoCompraRequest): Observable<SolicitacaoCompraDto> {
    return this.http.post<SolicitacaoCompraDto>(`${this.api}/solicitacao`, dto);
  }

  atualizar(uuid: string, dto: SolicitacaoCompraRequest): Observable<SolicitacaoCompraDto> {
    return this.http.put<SolicitacaoCompraDto>(`${this.api}/solicitacao/${uuid}`, dto);
  }

  enviar(uuid: string): Observable<SolicitacaoCompraDto> {
    return this.http.post<SolicitacaoCompraDto>(`${this.api}/solicitacao/${uuid}/envio`, null);
  }

  analisar(uuid: string, decisao: DecisaoSolicitacaoCompra, motivo: string | null): Observable<SolicitacaoCompraDto> {
    return this.http.post<SolicitacaoCompraDto>(`${this.api}/solicitacao/${uuid}/analise`, { decisao, motivo });
  }

  cancelar(uuid: string, motivo: string): Observable<SolicitacaoCompraDto> {
    return this.http.post<SolicitacaoCompraDto>(`${this.api}/solicitacao/${uuid}/cancelamento`, { motivo });
  }

  sugestao(unidadeId: string): Observable<SugestaoCompraDto[]> {
    return this.http.get<SugestaoCompraDto[]>(`${this.api}/sugestao`, { params: new HttpParams().set('unidadeId', unidadeId) });
  }

  cotar(itemId: string, dto: { fonte: FonteCotacao; fornecedorId: string | null; referencia: string | null; precoUnitario: number; cotadoEm: string }):
    Observable<SolicitacaoCompraDto> {
    return this.http.post<SolicitacaoCompraDto>(`${this.api}/item/${itemId}/cotacao`, dto);
  }

  removerCotacao(uuid: string): Observable<SolicitacaoCompraDto> {
    return this.http.delete<SolicitacaoCompraDto>(`${this.api}/cotacao/${uuid}`);
  }

  concluirPesquisa(uuid: string, justificativas: { itemId: string; justificativa: string }[]): Observable<SolicitacaoCompraDto> {
    return this.http.post<SolicitacaoCompraDto>(`${this.api}/solicitacao/${uuid}/pesquisa`, { justificativas });
  }

  // ── CP2 (ADR-0213) ──

  contratacoes(situacao?: SituacaoContratacao): Observable<ContratacaoDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : new HttpParams();
    return this.http.get<ContratacaoDto[]>(`${this.api}/contratacoes`, { params });
  }

  contratacao(uuid: string): Observable<ContratacaoDto> {
    return this.http.get<ContratacaoDto>(`${this.api}/contratacao/${uuid}`);
  }

  abrirContratacao(dto: { numeroProcesso: string; modalidade: ModalidadeContratacao; objeto: string; fundamentoLegal: string | null;
    ataOrigem: string | null; solicitacaoIds: string[] }): Observable<ContratacaoDto> {
    return this.http.post<ContratacaoDto>(`${this.api}/contratacao`, dto);
  }

  resultado(itemId: string, dto: { resultado: ResultadoItemContratacao; fornecedorId: string | null; precoUnitario: number | null;
    justificativaPreco: string | null }): Observable<ContratacaoDto> {
    return this.http.post<ContratacaoDto>(`${this.api}/item-contratacao/${itemId}/resultado`, dto);
  }

  homologar(uuid: string, instrumentos: { fornecedorId: string; tipo: TipoInstrumentoContratual; numero: string; inicio: string; fim: string;
    servicoContinuo: boolean; gestorMatricula: string; fiscalMatricula: string }[]): Observable<ContratacaoDto> {
    return this.http.post<ContratacaoDto>(`${this.api}/contratacao/${uuid}/homologacao`, { instrumentos });
  }

  cancelarContratacao(uuid: string, motivo: string): Observable<ContratacaoDto> {
    return this.http.post<ContratacaoDto>(`${this.api}/contratacao/${uuid}/cancelamento`, { motivo });
  }

  instrumentos(): Observable<InstrumentoDto[]> {
    return this.http.get<InstrumentoDto[]>(`${this.api}/instrumentos`);
  }

  meusContratos(): Observable<InstrumentoDto[]> {
    return this.http.get<InstrumentoDto[]>(`${this.api}/meus-contratos`);
  }

  instrumento(uuid: string): Observable<InstrumentoDto> {
    return this.http.get<InstrumentoDto>(`${this.api}/instrumento/${uuid}`);
  }

  aditivo(uuid: string, dto: { tipo: TipoAditivoContratual; novoFim: string | null; itemId: string | null; quantidade: number | null;
    novoPreco: number | null; justificativa: string; assinadoEm: string }): Observable<InstrumentoDto> {
    return this.http.post<InstrumentoDto>(`${this.api}/instrumento/${uuid}/aditivo`, dto);
  }

  // ── CP3 (ADR-0214) ──

  emitirOrdem(instrumentoId: string, dto: { unidadeId: string; empenho: string | null; prazoDias: number | null;
    itens: { itemInstrumentoId: string; quantidade: number }[] }): Observable<OrdemDto> {
    return this.http.post<OrdemDto>(`${this.api}/instrumento/${instrumentoId}/ordem`, dto);
  }

  ordensDo(instrumentoId: string): Observable<OrdemDto[]> {
    return this.http.get<OrdemDto[]>(`${this.api}/instrumento/${instrumentoId}/ordens`);
  }

  ordem(uuid: string): Observable<OrdemDto> {
    return this.http.get<OrdemDto>(`${this.api}/ordem/${uuid}`);
  }

  cancelarOrdem(uuid: string, motivo: string): Observable<OrdemDto> {
    return this.http.post<OrdemDto>(`${this.api}/ordem/${uuid}/cancelamento`, { motivo });
  }

  receber(ordemId: string, dto: { notaFiscal: string; notaEmitidaEm: string; observacao: string | null;
    itens: { itemOrdemId: string; quantidade: number; numeroLote: string | null; validade: string | null }[] }): Observable<OrdemDto> {
    return this.http.post<OrdemDto>(`${this.api}/ordem/${ordemId}/recebimento`, dto);
  }

  definitivo(recebimentoId: string, itens: { itemRecebimentoId: string; quantidadeAceita: number; motivoRecusa: string | null }[]): Observable<OrdemDto> {
    return this.http.post<OrdemDto>(`${this.api}/recebimento/${recebimentoId}/definitivo`, { itens });
  }

  registrarOcorrencia(ordemId: string, tipo: TipoOcorrenciaFornecedor, descricao: string): Observable<OrdemDto> {
    return this.http.post<OrdemDto>(`${this.api}/ordem/${ordemId}/ocorrencia`, { tipo, descricao });
  }

  ocorrencias(situacao?: SituacaoOcorrenciaFornecedor): Observable<OcorrenciaDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : new HttpParams();
    return this.http.get<OcorrenciaDto[]>(`${this.api}/ocorrencias`, { params });
  }

  encerrarOcorrencia(uuid: string, motivo: string): Observable<OcorrenciaDto> {
    return this.http.post<OcorrenciaDto>(`${this.api}/ocorrencia/${uuid}/encerramento`, { motivo });
  }

  sancionarOcorrencia(uuid: string, dto: { tipo: TipoSancaoFornecedor; inicio: string; fim: string | null; valor: number | null; motivo: string;
    processo: string | null }): Observable<OcorrenciaDto> {
    return this.http.post<OcorrenciaDto>(`${this.api}/ocorrencia/${uuid}/sancao`, dto);
  }

  painel(): Observable<PainelComprasDto> {
    return this.http.get<PainelComprasDto>(`${this.api}/painel`);
  }
}
