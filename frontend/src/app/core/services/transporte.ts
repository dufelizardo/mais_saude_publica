import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { FuncaoTripulante, PlantaoDto, SituacaoVeiculo, VeiculoDto, VeiculoRequestDto } from '../models/transporte';

/** Transporte (ADR-0178): frota, situação e plantão das viaturas. */
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
}
