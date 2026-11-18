import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DecisaoSolicitacaoCompra, FonteCotacao, FornecedorDto, SituacaoSolicitacaoCompra, SolicitacaoCompraDto, SolicitacaoCompraRequest, SugestaoCompraDto,
  TipoCertidaoFornecedor, TipoSancaoFornecedor,
} from '../models/compras';

/** Compras, fatia CP1 (ADR-0212). */
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
}
