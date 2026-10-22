import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AreaDetalheDto,
  AreaRequestDto,
  ArvoreTerritorialDto,
  MicroareaRequestDto,
  SegmentoDto,
  SegmentoRequestDto,
  TerritorioNoDto,
  TerritorioRequestDto,
  MeuEscopoTerritorialDto,
  UnidadeNoMapaDto,
} from '../models/territorio';
import {
  DomicilioRequestDto,
  FamiliaRequestDto,
  ImovelDetalheDto,
  ImovelDto,
  ImovelRequestDto,
  Parentesco,
  FaixaRendaFamiliar,
} from '../models/cadastro-territorial';
import { AdscricaoDoPacienteDto, CoberturaDto } from '../models/adscricao';

/** Território (ADR-0125): a árvore, o cadastro, a equipe da área, o ACS da microárea e as coordenadas da unidade. */
@Injectable({ providedIn: 'root' })
export class TerritorioService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/territorio';

  arvore(): Observable<ArvoreTerritorialDto> {
    return this.http.get<ArvoreTerritorialDto>(`${this.api}/arvore`);
  }

  area(uuid: string): Observable<AreaDetalheDto> {
    return this.http.get<AreaDetalheDto>(`${this.api}/area/${uuid}`);
  }

  meuEscopo(): Observable<MeuEscopoTerritorialDto> {
    return this.http.get<MeuEscopoTerritorialDto>(`${this.api}/meu-escopo`);
  }

  unidades(): Observable<UnidadeNoMapaDto[]> {
    return this.http.get<UnidadeNoMapaDto[]>(`${this.api}/unidades`);
  }

  salvarSegmento(uuid: string | null, dto: SegmentoRequestDto): Observable<SegmentoDto> {
    return uuid ? this.http.patch<SegmentoDto>(`${this.api}/segmento/${uuid}`, dto) : this.http.post<SegmentoDto>(`${this.api}/segmento`, dto);
  }

  salvarTerritorio(uuid: string | null, dto: TerritorioRequestDto): Observable<TerritorioNoDto> {
    return uuid
      ? this.http.patch<TerritorioNoDto>(`${this.api}/territorio/${uuid}`, dto)
      : this.http.post<TerritorioNoDto>(`${this.api}/territorio`, dto);
  }

  salvarArea(uuid: string | null, dto: AreaRequestDto): Observable<AreaDetalheDto> {
    return uuid ? this.http.patch<AreaDetalheDto>(`${this.api}/area/${uuid}`, dto) : this.http.post<AreaDetalheDto>(`${this.api}/area`, dto);
  }

  salvarMicroarea(uuid: string | null, dto: MicroareaRequestDto): Observable<AreaDetalheDto> {
    return uuid
      ? this.http.patch<AreaDetalheDto>(`${this.api}/microarea/${uuid}`, dto)
      : this.http.post<AreaDetalheDto>(`${this.api}/microarea`, dto);
  }

  atribuirEquipe(areaId: string, equipeId: string, inicio?: string | null, motivo?: string | null): Observable<AreaDetalheDto> {
    return this.http.post<AreaDetalheDto>(`${this.api}/area/${areaId}/responsabilidade`, { equipeId, inicio: inicio || null, motivo: motivo || null });
  }

  atribuirAcs(microareaId: string, membroId: string, inicio?: string | null, motivo?: string | null): Observable<AreaDetalheDto> {
    return this.http.post<AreaDetalheDto>(`${this.api}/microarea/${microareaId}/atribuicao`, { membroId, inicio: inicio || null, motivo: motivo || null });
  }

  coordenadas(unidadeId: string, latitude: number | null, longitude: number | null): Observable<UnidadeNoMapaDto> {
    return this.http.put<UnidadeNoMapaDto>(`${this.api}/unidade/${unidadeId}/coordenadas`, { latitude, longitude });
  }

  // ── Cadastro territorial (ADR-0127) ──────────────────────────────────────────────────────────────

  imoveis(areaId: string): Observable<ImovelDto[]> {
    return this.http.get<ImovelDto[]>(`${this.api}/area/${areaId}/imoveis`);
  }

  imovel(uuid: string): Observable<ImovelDetalheDto> {
    return this.http.get<ImovelDetalheDto>(`${this.api}/imovel/${uuid}`);
  }

  salvarImovel(uuid: string | null, dto: ImovelRequestDto): Observable<ImovelDetalheDto> {
    return uuid ? this.http.patch<ImovelDetalheDto>(`${this.api}/imovel/${uuid}`, dto) : this.http.post<ImovelDetalheDto>(`${this.api}/imovel`, dto);
  }

  salvarDomicilio(uuid: string | null, dto: DomicilioRequestDto): Observable<ImovelDetalheDto> {
    return uuid ? this.http.patch<ImovelDetalheDto>(`${this.api}/domicilio/${uuid}`, dto) : this.http.post<ImovelDetalheDto>(`${this.api}/domicilio`, dto);
  }

  criarFamilia(dto: FamiliaRequestDto): Observable<ImovelDetalheDto> {
    return this.http.post<ImovelDetalheDto>(`${this.api}/familia`, dto);
  }

  renda(familiaId: string, rendaFamiliar: FaixaRendaFamiliar | null): Observable<ImovelDetalheDto> {
    return this.http.patch<ImovelDetalheDto>(`${this.api}/familia/${familiaId}/renda`, { rendaFamiliar });
  }

  incluirMembro(familiaId: string, pacienteId: string, parentesco: Parentesco, inicio?: string | null): Observable<ImovelDetalheDto> {
    return this.http.post<ImovelDetalheDto>(`${this.api}/familia/${familiaId}/membro`, { pacienteId, parentesco, inicio: inicio || null });
  }

  saidaMembro(membroId: string, motivo: string, fim?: string | null): Observable<ImovelDetalheDto> {
    return this.http.post<ImovelDetalheDto>(`${this.api}/familia/membro/${membroId}/saida`, { motivo, fim: fim || null });
  }

  trocarResponsavel(familiaId: string, membroId: string, parentescoDoAnterior: Parentesco): Observable<ImovelDetalheDto> {
    return this.http.post<ImovelDetalheDto>(`${this.api}/familia/${familiaId}/responsavel`, { membroId, parentescoDoAnterior });
  }

  mudanca(familiaId: string, domicilioId: string, motivo?: string | null, data?: string | null): Observable<ImovelDetalheDto> {
    return this.http.post<ImovelDetalheDto>(`${this.api}/familia/${familiaId}/mudanca`, { domicilioId, motivo: motivo || null, data: data || null });
  }

  enderecoDoImovel(membroId: string): Observable<ImovelDetalheDto> {
    return this.http.post<ImovelDetalheDto>(`${this.api}/familia/membro/${membroId}/endereco-do-imovel`, {});
  }

  // ── Adscrição e cobertura (ADR-0128) ─────────────────────────────────────────────────────────────

  adscricao(pacienteId: string): Observable<AdscricaoDoPacienteDto> {
    return this.http.get<AdscricaoDoPacienteDto>(`${this.api}/paciente/${pacienteId}/adscricao`);
  }

  adscrever(pacienteId: string, equipeId: string, motivo: string, inicio?: string | null): Observable<AdscricaoDoPacienteDto> {
    return this.http.post<AdscricaoDoPacienteDto>(`${this.api}/paciente/${pacienteId}/adscricao`, { equipeId, motivo, inicio: inicio || null });
  }

  encerrarAdscricao(pacienteId: string, motivo: string, fim?: string | null): Observable<AdscricaoDoPacienteDto> {
    return this.http.post<AdscricaoDoPacienteDto>(`${this.api}/paciente/${pacienteId}/adscricao/encerrar`, { motivo, fim: fim || null });
  }

  cobertura(equipeId: string): Observable<CoberturaDto> {
    return this.http.get<CoberturaDto>(`${this.api}/equipe/${equipeId}/cobertura`);
  }

  encerrarAcs(microareaId: string, motivo: string, fim?: string | null): Observable<AreaDetalheDto> {
    return this.http.post<AreaDetalheDto>(`${this.api}/microarea/${microareaId}/atribuicao/encerrar`, { motivo, fim: fim || null });
  }
}
