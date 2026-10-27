import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import {
  AvaliacaoOdontologicaDto,
  CondicaoOdontologicaDto,
  AvaliacaoOdontologicaRequestDto,
  EspecialidadeDto,
  EspecialidadeRequestDto,
  AcaoSaudeBucalDto,
  AcaoSaudeBucalRequestDto,
  ConvocacaoSaudeBucalDto,
  NecessidadeTratamentoOdontologico,
  RiscoSaudeBucal,
  EtapaProteticaRequestDto,
  TratamentoProteticoDto,
  TratamentoProteticoRequestDto,
  ItemPlanoTratamentoRequestDto,
  MarcacaoOdontogramaDto,
  PlanoTratamentoDto,
  PlanoTratamentoRequestDto,
  SituacaoItemPlanoTratamento,
  OdontogramaDto,
  RegistroOdontogramaDto,
} from '../models/saude-bucal';

/** Catálogo de especialidades e avaliação odontológica (ADR-0134). */
@Injectable({ providedIn: 'root' })
export class SaudeBucalService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';
  private catalogo$?: Observable<EspecialidadeDto[]>;

  /** O catálogo muda pouco: uma busca por sessão, renovada quando ele é alterado aqui. */
  especialidades(): Observable<EspecialidadeDto[]> {
    this.catalogo$ ??= this.http.get<EspecialidadeDto[]>(`${this.api}/especialidades`).pipe(shareReplay(1));
    return this.catalogo$;
  }

  criarEspecialidade(dto: EspecialidadeRequestDto): Observable<EspecialidadeDto> {
    this.catalogo$ = undefined;
    return this.http.post<EspecialidadeDto>(`${this.api}/especialidade`, dto);
  }

  atualizarEspecialidade(uuid: string, dto: EspecialidadeRequestDto): Observable<EspecialidadeDto> {
    this.catalogo$ = undefined;
    return this.http.put<EspecialidadeDto>(`${this.api}/especialidade/${uuid}`, dto);
  }

  avaliar(dto: AvaliacaoOdontologicaRequestDto): Observable<AvaliacaoOdontologicaDto> {
    return this.http.post<AvaliacaoOdontologicaDto>(`${this.api}/avaliacao-odontologica`, dto);
  }

  retificar(uuid: string, dto: AvaliacaoOdontologicaRequestDto): Observable<AvaliacaoOdontologicaDto> {
    return this.http.post<AvaliacaoOdontologicaDto>(`${this.api}/avaliacao-odontologica/${uuid}/retificacao`, dto);
  }

  // ── Odontograma (ADR-0135) ─────────────────────────────────────────────────────────────────────

  condicoes(): Observable<CondicaoOdontologicaDto[]> {
    return this.http.get<CondicaoOdontologicaDto[]>(`${this.api}/condicoes-odontologicas`);
  }

  marcar(avaliacaoId: string, marcacoes: MarcacaoOdontogramaDto[], data?: string | null): Observable<OdontogramaDto> {
    return this.http.post<OdontogramaDto>(`${this.api}/avaliacao-odontologica/${avaliacaoId}/odontograma`, { data: data ?? null, marcacoes });
  }

  anular(registroId: string, motivo: string): Observable<RegistroOdontogramaDto> {
    return this.http.post<RegistroOdontogramaDto>(`${this.api}/registro-odontograma/${registroId}/anulacao`, { motivo });
  }

  odontograma(pacienteId: string, data?: string | null): Observable<OdontogramaDto> {
    return this.http.get<OdontogramaDto>(`${this.api}/paciente/${pacienteId}/odontograma`, { params: data ? { data } : {} });
  }

  historicoDoDente(pacienteId: string, dente: number): Observable<RegistroOdontogramaDto[]> {
    return this.http.get<RegistroOdontogramaDto[]>(`${this.api}/paciente/${pacienteId}/odontograma/dente/${dente}`);
  }

  // ── Plano de tratamento (ADR-0137) ─────────────────────────────────────────────────────────────

  planos(pacienteId: string): Observable<PlanoTratamentoDto[]> {
    return this.http.get<PlanoTratamentoDto[]>(`${this.api}/paciente/${pacienteId}/planos-tratamento`);
  }

  abrirPlano(dto: PlanoTratamentoRequestDto): Observable<PlanoTratamentoDto> {
    return this.http.post<PlanoTratamentoDto>(`${this.api}/plano-tratamento`, dto);
  }

  incluirItemPlano(planoId: string, dto: ItemPlanoTratamentoRequestDto): Observable<PlanoTratamentoDto> {
    return this.http.post<PlanoTratamentoDto>(`${this.api}/plano-tratamento/${planoId}/item`, dto);
  }

  situacaoItemPlano(itemId: string, situacao: SituacaoItemPlanoTratamento, motivo?: string | null): Observable<PlanoTratamentoDto> {
    return this.http.post<PlanoTratamentoDto>(`${this.api}/item-plano-tratamento/${itemId}/situacao`, { situacao, motivo: motivo ?? null });
  }

  concluirPlano(planoId: string): Observable<PlanoTratamentoDto> {
    return this.http.post<PlanoTratamentoDto>(`${this.api}/plano-tratamento/${planoId}/conclusao`, null);
  }

  abandonarPlano(planoId: string, motivo: string): Observable<PlanoTratamentoDto> {
    return this.http.post<PlanoTratamentoDto>(`${this.api}/plano-tratamento/${planoId}/abandono`, { motivo });
  }

  // ── Tratamento protético (ADR-0139) ────────────────────────────────────────────────────────────

  proteses(pacienteId: string): Observable<TratamentoProteticoDto[]> {
    return this.http.get<TratamentoProteticoDto[]>(`${this.api}/paciente/${pacienteId}/tratamentos-proteticos`);
  }

  abrirProtese(dto: TratamentoProteticoRequestDto): Observable<TratamentoProteticoDto> {
    return this.http.post<TratamentoProteticoDto>(`${this.api}/tratamento-protetico`, dto);
  }

  etapaProtese(id: string, dto: EtapaProteticaRequestDto): Observable<TratamentoProteticoDto> {
    return this.http.post<TratamentoProteticoDto>(`${this.api}/tratamento-protetico/${id}/etapa`, dto);
  }

  concluirProtese(id: string): Observable<TratamentoProteticoDto> {
    return this.http.post<TratamentoProteticoDto>(`${this.api}/tratamento-protetico/${id}/conclusao`, null);
  }

  cancelarProtese(id: string, motivo: string): Observable<TratamentoProteticoDto> {
    return this.http.post<TratamentoProteticoDto>(`${this.api}/tratamento-protetico/${id}/cancelamento`, { motivo });
  }

  // ── Saúde bucal coletiva (ADR-0140) ────────────────────────────────────────────────────────────

  saudeBucalDaAcao(acaoId: string): Observable<AcaoSaudeBucalDto> {
    return this.http.get<AcaoSaudeBucalDto>(`${this.api}/acao-programatica/${acaoId}/saude-bucal`);
  }

  salvarSaudeBucalDaAcao(acaoId: string, dto: AcaoSaudeBucalRequestDto): Observable<AcaoSaudeBucalDto> {
    return this.http.put<AcaoSaudeBucalDto>(`${this.api}/acao-programatica/${acaoId}/saude-bucal`, dto);
  }

  avaliarColetiva(acaoId: string, resultados: { pacienteId: string; risco: RiscoSaudeBucal; necessidadeTratamento: NecessidadeTratamentoOdontologico }[]): Observable<AcaoSaudeBucalDto> {
    return this.http.post<AcaoSaudeBucalDto>(`${this.api}/acao-programatica/${acaoId}/avaliacao-bucal`, { resultados });
  }

  aConvocar(unidadeId: string): Observable<ConvocacaoSaudeBucalDto[]> {
    return this.http.get<ConvocacaoSaudeBucalDto[]>(`${this.api}/saude-bucal/a-convocar`, { params: { unidadeId } });
  }
}
