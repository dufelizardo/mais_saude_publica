import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CompetenciaDoProfissionalDto, CompetenciaDto, ConteudoDoTreinamentoDto, EducacaoDoTreinamentoDto, UsoDoConteudoDto } from '../models/educacao-permanente';

type CompetenciaForm = Pick<CompetenciaDto, 'codigo' | 'nome' | 'descricao' | 'ativo'>;

/** Educação permanente (ADR-0173): competências e material didático dos treinamentos. */
@Injectable({ providedIn: 'root' })
export class EducacaoPermanenteService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/educacao-permanente';

  competencias(): Observable<CompetenciaDto[]> {
    return this.http.get<CompetenciaDto[]>(`${this.api}/competencia`);
  }

  criarCompetencia(dto: CompetenciaForm): Observable<CompetenciaDto> {
    return this.http.post<CompetenciaDto>(`${this.api}/competencia`, dto);
  }

  atualizarCompetencia(uuid: string, dto: CompetenciaForm): Observable<CompetenciaDto> {
    return this.http.patch<CompetenciaDto>(`${this.api}/competencia/${uuid}`, dto);
  }

  treinamentos(): Observable<EducacaoDoTreinamentoDto[]> {
    return this.http.get<EducacaoDoTreinamentoDto[]>(`${this.api}/treinamento`);
  }

  ligar(treinamentoId: string, competenciaIds: string[], conteudoGrupoIds: string[]): Observable<EducacaoDoTreinamentoDto> {
    return this.http.put<EducacaoDoTreinamentoDto>(`${this.api}/treinamento/${treinamentoId}`, { competenciaIds, conteudoGrupoIds });
  }

  conteudosDisponiveis(): Observable<ConteudoDoTreinamentoDto[]> {
    return this.http.get<ConteudoDoTreinamentoDto[]>(`${this.api}/conteudo`);
  }

  usoDosConteudos(): Observable<UsoDoConteudoDto[]> {
    return this.http.get<UsoDoConteudoDto[]>(`${this.api}/uso-dos-conteudos`);
  }

  doProfissional(matricula: string): Observable<CompetenciaDoProfissionalDto[]> {
    return this.http.get<CompetenciaDoProfissionalDto[]>(`${this.api}/profissional/${encodeURIComponent(matricula)}/competencias`);
  }
}
