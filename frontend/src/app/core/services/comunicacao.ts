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
}
