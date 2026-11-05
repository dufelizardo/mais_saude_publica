import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AnimalZoonosesDto,
  DesfechoLeishmanioseCanina,
  DesfechoObservacaoAnimal,
  EspecieAnimal,
  ExameAnimalDto,
  ObservacaoAnimalDto,
  ResultadoExameAnimal,
  TipoExameAnimal,
  VacinacaoAntirrabicaDto,
} from '../models/zoonoses';

/** Animais das zoonoses, fatia V4b da Vigilância (ADR-0163). */
@Injectable({ providedIn: 'root' })
export class ZoonosesService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/zoonoses';

  animais(busca?: string | null): Observable<AnimalZoonosesDto[]> {
    const params = busca ? new HttpParams().set('busca', busca) : undefined;
    return this.http.get<AnimalZoonosesDto[]>(`${this.api}/animais`, { params });
  }

  registrarAnimal(dto: { especie: EspecieAnimal; nome: string | null; descricao: string | null; tutorNome: string | null; tutorTelefone: string | null; imovelId: string | null; endereco: string | null }): Observable<AnimalZoonosesDto> {
    return this.http.post<AnimalZoonosesDto>(`${this.api}/animal`, dto);
  }

  observacoes(filtro: { abertas?: boolean; notificacaoId?: string } = {}): Observable<ObservacaoAnimalDto[]> {
    let params = new HttpParams();
    if (filtro.abertas != null) params = params.set('abertas', filtro.abertas);
    if (filtro.notificacaoId) params = params.set('notificacaoId', filtro.notificacaoId);
    return this.http.get<ObservacaoAnimalDto[]>(`${this.api}/observacoes`, { params });
  }

  abrirObservacao(dto: { animalId: string; notificacaoId: string | null; dataAgressao: string; inicio: string | null; observacao: string | null }): Observable<ObservacaoAnimalDto> {
    return this.http.post<ObservacaoAnimalDto>(`${this.api}/observacao`, dto);
  }

  desfecho(id: string, desfecho: DesfechoObservacaoAnimal, data: string | null, observacao: string | null): Observable<ObservacaoAnimalDto> {
    return this.http.post<ObservacaoAnimalDto>(`${this.api}/observacao/${id}/desfecho`, { desfecho, data, observacao });
  }

  exames(): Observable<ExameAnimalDto[]> {
    return this.http.get<ExameAnimalDto[]>(`${this.api}/exames`);
  }

  coletar(dto: { animalId: string; tipo: TipoExameAnimal; dataColeta: string | null; observacaoId: string | null; observacao: string | null }): Observable<ExameAnimalDto> {
    return this.http.post<ExameAnimalDto>(`${this.api}/exame`, dto);
  }

  resultado(id: string, resultado: ResultadoExameAnimal, desfechoLeishmaniose: DesfechoLeishmanioseCanina | null): Observable<ExameAnimalDto> {
    return this.http.post<ExameAnimalDto>(`${this.api}/exame/${id}/resultado`, { resultado, desfechoLeishmaniose });
  }

  vacinacao(ano?: number): Observable<VacinacaoAntirrabicaDto> {
    const params = ano ? new HttpParams().set('ano', ano) : undefined;
    return this.http.get<VacinacaoAntirrabicaDto>(`${this.api}/vacinacao-antirrabica`, { params });
  }

  registrarVacinacao(dto: { data: string | null; microareaId: string; posto: string; caes: number | null; gatos: number | null; responsavel: string }): Observable<VacinacaoAntirrabicaDto> {
    return this.http.post<VacinacaoAntirrabicaDto>(`${this.api}/vacinacao-antirrabica`, dto);
  }
}
