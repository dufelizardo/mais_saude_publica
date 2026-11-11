import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  FuncaoTripulante,
  MotivoTransporte,
  PlantaoDto,
  QuilometragemDto,
  SituacaoSolicitacaoTransporte,
  SituacaoVeiculo,
  SolicitacaoTransporteDto,
  SolicitacaoTransporteRequestDto,
  VeiculoDto,
  VeiculoRequestDto,
  ViagemDto,
  CessaoFeitaDto,
  CessaoVeiculoDto,
  PedidoVeiculoEmergenciaDto,
} from '../models/transporte';

/** Transporte: frota, situação e plantão das viaturas (ADR-0178); transporte eletivo, TFD e viagens (ADR-0190). */
@Injectable({ providedIn: 'root' })
export class TransporteService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/transporte';

  veiculos(unidadeId?: string | null): Observable<VeiculoDto[]> {
    return this.http.get<VeiculoDto[]>(`${this.api}/veiculo`, { params: unidadeId ? { unidadeId } : {} });
  }

  veiculo(uuid: string): Observable<VeiculoDto> {
    return this.http.get<VeiculoDto>(`${this.api}/veiculo/${uuid}`);
  }

  criar(dto: VeiculoRequestDto): Observable<VeiculoDto> {
    return this.http.post<VeiculoDto>(`${this.api}/veiculo`, dto);
  }

  atualizar(uuid: string, dto: VeiculoRequestDto): Observable<VeiculoDto> {
    return this.http.put<VeiculoDto>(`${this.api}/veiculo/${uuid}`, dto);
  }

  mudarSituacao(uuid: string, situacao: SituacaoVeiculo, motivo: string, quilometragem?: number | null): Observable<VeiculoDto> {
    return this.http.post<VeiculoDto>(`${this.api}/veiculo/${uuid}/situacao`, { situacao, motivo, quilometragem: quilometragem ?? null });
  }

  plantoes(unidadeId?: string | null): Observable<PlantaoDto[]> {
    return this.http.get<PlantaoDto[]>(`${this.api}/plantao`, { params: unidadeId ? { unidadeId } : {} });
  }

  criarPlantao(dto: { veiculoId: string; inicio: string; fim: string; tripulacao: { matricula: string; funcao: FuncaoTripulante }[]; observacao?: string | null }): Observable<PlantaoDto> {
    return this.http.post<PlantaoDto>(`${this.api}/plantao`, dto);
  }

  encerrarPlantao(uuid: string): Observable<PlantaoDto> {
    return this.http.post<PlantaoDto>(`${this.api}/plantao/${uuid}/encerramento`, {});
  }

  // ── Transporte eletivo e TFD (ADR-0190) ──

  solicitacoes(filtro: { unidadeId?: string | null; pacienteId?: string | null; situacao?: SituacaoSolicitacaoTransporte | null;
    motivo?: MotivoTransporte | null; de?: string | null; ate?: string | null } = {}): Observable<SolicitacaoTransporteDto[]> {
    const params: Record<string, string> = {};
    Object.entries(filtro).forEach(([k, v]) => { if (v) params[k] = v; });
    return this.http.get<SolicitacaoTransporteDto[]>(`${this.api}/solicitacao`, { params });
  }

  solicitar(dto: SolicitacaoTransporteRequestDto): Observable<SolicitacaoTransporteDto> {
    return this.http.post<SolicitacaoTransporteDto>(`${this.api}/solicitacao`, dto);
  }

  solicitarRecorrente(dto: SolicitacaoTransporteRequestDto): Observable<SolicitacaoTransporteDto[]> {
    return this.http.post<SolicitacaoTransporteDto[]>(`${this.api}/solicitacao/recorrente`, dto);
  }

  cancelarSolicitacao(uuid: string, motivo: string, recorrencia: boolean): Observable<SolicitacaoTransporteDto[]> {
    return this.http.post<SolicitacaoTransporteDto[]>(`${this.api}/solicitacao/${uuid}/cancelamento`, { motivo, recorrencia });
  }

  autorizarTfd(uuid: string): Observable<SolicitacaoTransporteDto> {
    return this.http.post<SolicitacaoTransporteDto>(`${this.api}/solicitacao/${uuid}/autorizacao`, {});
  }

  negarTfd(uuid: string, motivo: string): Observable<SolicitacaoTransporteDto> {
    return this.http.post<SolicitacaoTransporteDto>(`${this.api}/solicitacao/${uuid}/negacao`, { motivo });
  }

  viagens(data?: string | null, dias = 7): Observable<ViagemDto[]> {
    const params: Record<string, string> = { dias: String(dias) };
    if (data) params['data'] = data;
    return this.http.get<ViagemDto[]>(`${this.api}/viagem`, { params });
  }

  programarViagem(dto: { veiculoId: string; condutorMatricula: string; saidaPrevista: string; retornoPrevisto: string; destino?: string | null;
    observacao?: string | null; solicitacoes: string[] }): Observable<ViagemDto> {
    return this.http.post<ViagemDto>(`${this.api}/viagem`, dto);
  }

  trocarEmbarques(uuid: string, solicitacoes: string[]): Observable<ViagemDto> {
    return this.http.put<ViagemDto>(`${this.api}/viagem/${uuid}/embarques`, { solicitacoes });
  }

  sair(uuid: string, quilometragem: number): Observable<ViagemDto> {
    return this.http.post<ViagemDto>(`${this.api}/viagem/${uuid}/saida`, { quilometragem });
  }

  registrarEmbarque(uuid: string, itens: { solicitacaoId: string; embarcou: boolean; motivo?: string | null }[]): Observable<ViagemDto> {
    return this.http.post<ViagemDto>(`${this.api}/viagem/${uuid}/embarque`, { itens });
  }

  chegar(uuid: string, quilometragem: number): Observable<ViagemDto> {
    return this.http.post<ViagemDto>(`${this.api}/viagem/${uuid}/chegada`, { quilometragem });
  }

  cancelarViagem(uuid: string, motivo: string): Observable<ViagemDto> {
    return this.http.post<ViagemDto>(`${this.api}/viagem/${uuid}/cancelamento`, { motivo });
  }

  // ── Veículos nas emergências (ADR-0192) ──

  pedidosDeEmergencia(): Observable<PedidoVeiculoEmergenciaDto[]> {
    return this.http.get<PedidoVeiculoEmergenciaDto[]>(`${this.api}/emergencia/pedidos`);
  }

  ceder(mobilizacaoId: string, veiculoId: string, observacao: string | null): Observable<CessaoFeitaDto> {
    return this.http.post<CessaoFeitaDto>(`${this.api}/emergencia/mobilizacao/${mobilizacaoId}/cessao`, { veiculoId, observacao });
  }

  devolver(cessaoId: string, quilometragem: number | null, observacao: string | null): Observable<CessaoVeiculoDto> {
    return this.http.post<CessaoVeiculoDto>(`${this.api}/emergencia/cessao/${cessaoId}/devolucao`, { quilometragem, observacao });
  }

  quilometragem(de?: string | null, ate?: string | null): Observable<QuilometragemDto[]> {
    const params: Record<string, string> = {};
    if (de) params['de'] = de;
    if (ate) params['ate'] = ate;
    return this.http.get<QuilometragemDto[]>(`${this.api}/quilometragem`, { params });
  }
}
