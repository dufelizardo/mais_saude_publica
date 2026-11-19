import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  BemDto, BemRequest, ClasseBem, ConclusaoOrdemServicoRequest, ContratoServicoDto, DetalheBemDto, ExecucaoManutencao, ManutencaoDoBemDto, OrdemServicoDto,
  PrioridadeManutencao, ResumoManutencaoDto, SituacaoBem, SituacaoOrdemServico, TipoManutencao, TransferenciaBemDto,
} from '../models/patrimonio';

/** Patrimônio: PM1 bens e transferências (ADR-0215) e PM2 manutenção (ADR-0216). */
@Injectable({ providedIn: 'root' })
export class PatrimonioService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/patrimonio';

  bens(filtro: { unidadeId?: string; classe?: ClasseBem; situacao?: SituacaoBem; busca?: string }): Observable<BemDto[]> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries(filtro)) if (v) params = params.set(k, v);
    return this.http.get<BemDto[]>(`${this.api}/bens`, { params });
  }

  meusBens(): Observable<BemDto[]> {
    return this.http.get<BemDto[]>(`${this.api}/meus-bens`);
  }

  bem(uuid: string): Observable<DetalheBemDto> {
    return this.http.get<DetalheBemDto>(`${this.api}/bem/${uuid}`);
  }

  cadastrar(dto: BemRequest): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem`, dto);
  }

  editar(uuid: string, dto: { descricao: string; marca: string | null; modelo: string | null; numeroSerie: string | null; setorId: string | null;
    observacao: string | null }): Observable<DetalheBemDto> {
    return this.http.put<DetalheBemDto>(`${this.api}/bem/${uuid}`, dto);
  }

  tombar(uuid: string, dto: { classe: ClasseBem; marca: string | null; modelo: string | null; numeroSerie: string | null; setorId: string | null;
    responsavelMatricula: string; observacao: string | null }): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/tombamento`, dto);
  }

  responsavel(uuid: string, responsavelMatricula: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/responsavel`, { responsavelMatricula });
  }

  situacao(uuid: string, situacao: SituacaoBem): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/situacao`, { situacao });
  }

  veiculo(uuid: string, veiculoId: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/veiculo`, { veiculoId });
  }

  transferencias(pendentes?: boolean): Observable<TransferenciaBemDto[]> {
    return this.http.get<TransferenciaBemDto[]>(`${this.api}/transferencias`, { params: pendentes ? { pendentes: true } : {} });
  }

  transferir(uuid: string, destinoId: string, motivo: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/transferencia`, { destinoId, motivo });
  }

  aceitar(transferenciaId: string, setorId: string | null, responsavelMatricula: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/transferencia/${transferenciaId}/aceite`, { setorId, responsavelMatricula });
  }

  recusar(transferenciaId: string, motivo: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/transferencia/${transferenciaId}/recusa`, { motivo });
  }

  cancelar(transferenciaId: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/transferencia/${transferenciaId}/cancelamento`, {});
  }

  // ── Manutenção (ADR-0216) ──

  manutencaoDoBem(uuid: string): Observable<ManutencaoDoBemDto> {
    return this.http.get<ManutencaoDoBemDto>(`${this.api}/bem/${uuid}/manutencao`);
  }

  criarPlano(uuid: string, dto: { tipo: TipoManutencao; periodicidadeDias: number; ultimaExecucao: string | null; descricao: string | null }): Observable<ManutencaoDoBemDto> {
    return this.http.post<ManutencaoDoBemDto>(`${this.api}/bem/${uuid}/plano`, dto);
  }

  desativarPlano(planoId: string): Observable<ManutencaoDoBemDto> {
    return this.http.post<ManutencaoDoBemDto>(`${this.api}/plano/${planoId}/desativacao`, {});
  }

  abrirChamado(uuid: string, defeito: string, bemParado: boolean): Observable<OrdemServicoDto> {
    return this.http.post<OrdemServicoDto>(`${this.api}/bem/${uuid}/chamado`, { defeito, bemParado });
  }

  meusChamados(): Observable<OrdemServicoDto[]> {
    return this.http.get<OrdemServicoDto[]>(`${this.api}/meus-chamados`);
  }

  ordensServico(situacao?: SituacaoOrdemServico): Observable<OrdemServicoDto[]> {
    return this.http.get<OrdemServicoDto[]>(`${this.api}/ordens-servico`, { params: situacao ? { situacao } : {} });
  }

  resumoManutencao(): Observable<ResumoManutencaoDto> {
    return this.http.get<ResumoManutencaoDto>(`${this.api}/manutencao/resumo`);
  }

  contratosDeServico(): Observable<ContratoServicoDto[]> {
    return this.http.get<ContratoServicoDto[]>(`${this.api}/manutencao/contratos-servico`);
  }

  ordemServico(uuid: string): Observable<OrdemServicoDto> {
    return this.http.get<OrdemServicoDto>(`${this.api}/ordem-servico/${uuid}`);
  }

  triar(uuid: string, prioridade: PrioridadeManutencao): Observable<OrdemServicoDto> {
    return this.http.post<OrdemServicoDto>(`${this.api}/ordem-servico/${uuid}/triagem`, { prioridade });
  }

  iniciar(uuid: string, execucao: ExecucaoManutencao, instrumentoId: string | null, tecnico: string | null): Observable<OrdemServicoDto> {
    return this.http.post<OrdemServicoDto>(`${this.api}/ordem-servico/${uuid}/inicio`, { execucao, instrumentoId, tecnico });
  }

  concluir(uuid: string, dto: ConclusaoOrdemServicoRequest): Observable<OrdemServicoDto> {
    return this.http.post<OrdemServicoDto>(`${this.api}/ordem-servico/${uuid}/conclusao`, dto);
  }

  cancelarOrdem(uuid: string, motivo: string): Observable<OrdemServicoDto> {
    return this.http.post<OrdemServicoDto>(`${this.api}/ordem-servico/${uuid}/cancelamento`, { motivo });
  }
}
