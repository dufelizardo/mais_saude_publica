import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CanalComunicacaoDto,
  CanalComunicacaoRequestDto,
  ComunicadoDto,
  ComunicadoPublicoDto,
  ComunicadoRequestDto,
  ConteudoEducativoDto,
  ConteudoEducativoRequestDto,
  ConteudoPublicoDto,
  CampanhaComunicacaoDto,
  CampanhaComunicacaoPublicaDto,
  CampanhaComunicacaoRequestDto,
  MaterialEducativoDto,
  MaterialEducativoRequestDto,
} from '../models/comunicacao';

/** Comunicação (ADR-0168): canais, comunicados, conteúdos com revisão e a consulta pública. */
@Injectable({ providedIn: 'root' })
export class ComunicacaoService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/comunicacao';

  canais(): Observable<CanalComunicacaoDto[]> {
    return this.http.get<CanalComunicacaoDto[]>(`${this.api}/canal`);
  }

  criarCanal(dto: CanalComunicacaoRequestDto): Observable<CanalComunicacaoDto> {
    return this.http.post<CanalComunicacaoDto>(`${this.api}/canal`, dto);
  }

  atualizarCanal(uuid: string, dto: CanalComunicacaoRequestDto): Observable<CanalComunicacaoDto> {
    return this.http.patch<CanalComunicacaoDto>(`${this.api}/canal/${uuid}`, dto);
  }

  comunicados(): Observable<ComunicadoDto[]> {
    return this.http.get<ComunicadoDto[]>(`${this.api}/comunicado`);
  }

  criarComunicado(dto: ComunicadoRequestDto): Observable<ComunicadoDto> {
    return this.http.post<ComunicadoDto>(`${this.api}/comunicado`, dto);
  }

  atualizarComunicado(uuid: string, dto: ComunicadoRequestDto): Observable<ComunicadoDto> {
    return this.http.patch<ComunicadoDto>(`${this.api}/comunicado/${uuid}`, dto);
  }

  publicarComunicado(uuid: string): Observable<ComunicadoDto> {
    return this.http.post<ComunicadoDto>(`${this.api}/comunicado/${uuid}/publicacao`, null);
  }

  encerrarComunicado(uuid: string, motivo: string): Observable<ComunicadoDto> {
    return this.http.post<ComunicadoDto>(`${this.api}/comunicado/${uuid}/encerramento`, { motivo });
  }

  conteudos(): Observable<ConteudoEducativoDto[]> {
    return this.http.get<ConteudoEducativoDto[]>(`${this.api}/conteudo`);
  }

  criarConteudo(dto: ConteudoEducativoRequestDto): Observable<ConteudoEducativoDto> {
    return this.http.post<ConteudoEducativoDto>(`${this.api}/conteudo`, dto);
  }

  atualizarConteudo(uuid: string, dto: ConteudoEducativoRequestDto): Observable<ConteudoEducativoDto> {
    return this.http.patch<ConteudoEducativoDto>(`${this.api}/conteudo/${uuid}`, dto);
  }

  acaoDoConteudo(uuid: string, acao: 'envio-revisao' | 'aprovacao' | 'nova-versao'): Observable<ConteudoEducativoDto> {
    return this.http.post<ConteudoEducativoDto>(`${this.api}/conteudo/${uuid}/${acao}`, null);
  }

  devolverConteudo(uuid: string, motivo: string): Observable<ConteudoEducativoDto> {
    return this.http.post<ConteudoEducativoDto>(`${this.api}/conteudo/${uuid}/devolucao`, { motivo });
  }

  publicarConteudo(uuid: string, canalIds: string[]): Observable<ConteudoEducativoDto> {
    return this.http.post<ConteudoEducativoDto>(`${this.api}/conteudo/${uuid}/publicacao`, { canalIds });
  }

  avisos(): Observable<ComunicadoPublicoDto[]> {
    return this.http.get<ComunicadoPublicoDto[]>('/api/v1/publico/comunicacao/avisos');
  }

  orientacoes(): Observable<ConteudoPublicoDto[]> {
    return this.http.get<ConteudoPublicoDto[]>('/api/v1/publico/comunicacao/orientacoes');
  }

  // ── Campanhas e materiais (ADR-0174) ──

  campanhas(): Observable<CampanhaComunicacaoDto[]> {
    return this.http.get<CampanhaComunicacaoDto[]>(`${this.api}/campanha`);
  }

  criarCampanha(dto: CampanhaComunicacaoRequestDto): Observable<CampanhaComunicacaoDto> {
    return this.http.post<CampanhaComunicacaoDto>(`${this.api}/campanha`, dto);
  }

  atualizarCampanha(uuid: string, dto: CampanhaComunicacaoRequestDto): Observable<CampanhaComunicacaoDto> {
    return this.http.patch<CampanhaComunicacaoDto>(`${this.api}/campanha/${uuid}`, dto);
  }

  iniciarCampanha(uuid: string): Observable<CampanhaComunicacaoDto> {
    return this.http.post<CampanhaComunicacaoDto>(`${this.api}/campanha/${uuid}/inicio`, {});
  }

  encerrarCampanha(uuid: string, balanco: string): Observable<CampanhaComunicacaoDto> {
    return this.http.post<CampanhaComunicacaoDto>(`${this.api}/campanha/${uuid}/encerramento`, { balanco });
  }

  cancelarCampanha(uuid: string, motivo: string): Observable<CampanhaComunicacaoDto> {
    return this.http.post<CampanhaComunicacaoDto>(`${this.api}/campanha/${uuid}/cancelamento`, { motivo });
  }

  materiais(conteudoGrupoId: string): Observable<MaterialEducativoDto[]> {
    return this.http.get<MaterialEducativoDto[]>(`${this.api}/material`, { params: { conteudoGrupoId } });
  }

  criarMaterial(dto: MaterialEducativoRequestDto): Observable<MaterialEducativoDto> {
    return this.http.post<MaterialEducativoDto>(`${this.api}/material`, dto);
  }

  retirarMaterial(uuid: string, motivo: string): Observable<MaterialEducativoDto> {
    return this.http.post<MaterialEducativoDto>(`${this.api}/material/${uuid}/retirada`, { motivo });
  }

  campanhasPublicas(): Observable<CampanhaComunicacaoPublicaDto[]> {
    return this.http.get<CampanhaComunicacaoPublicaDto[]>('/api/v1/publico/comunicacao/campanhas');
  }
}
