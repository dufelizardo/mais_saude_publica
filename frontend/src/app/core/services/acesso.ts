import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AtribuicaoAcessoRequestDto,
  AtribuicaoAcessoResponseDto,
  EscopoAcessoResponseDto,
  PapelAtualizacaoRequestDto,
  PapelRequestDto,
  PapelResponseDto,
  PermissaoResponseDto,
  UsuarioAtualizacaoRequestDto,
  UsuarioRequestDto,
  UsuarioResponseDto,
} from '../models/acesso';
import { SuccessResponseDto } from '../models/profissional';

/** Usuários, papéis, permissões e atribuições de acesso (ADR-0066, ADR-0068). */
@Injectable({ providedIn: 'root' })
export class AcessoService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  listarPermissoes(): Observable<PermissaoResponseDto[]> {
    return this.http.get<PermissaoResponseDto[]>(`${this.api}/permissao/`);
  }

  listarPapeis(): Observable<PapelResponseDto[]> {
    return this.http.get<PapelResponseDto[]>(`${this.api}/papel/`);
  }

  criarPapel(dto: PapelRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.api}/papel/`, dto);
  }

  atualizarPapel(uuid: string, dto: PapelAtualizacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.api}/papel/${uuid}`, dto);
  }

  listarAtribuicoes(): Observable<AtribuicaoAcessoResponseDto[]> {
    return this.http.get<AtribuicaoAcessoResponseDto[]>(`${this.api}/atribuicao-acesso/`);
  }

  listarEscopos(): Observable<EscopoAcessoResponseDto[]> {
    return this.http.get<EscopoAcessoResponseDto[]>(`${this.api}/atribuicao-acesso/escopos`);
  }

  conceder(dto: AtribuicaoAcessoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.api}/atribuicao-acesso/`, dto);
  }

  revogar(uuid: string, motivo: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.api}/atribuicao-acesso/${uuid}/revogacao`, { motivo });
  }

  listarUsuarios(): Observable<UsuarioResponseDto[]> {
    return this.http.get<UsuarioResponseDto[]>(`${this.api}/usuario/`);
  }

  criarUsuario(dto: UsuarioRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.api}/usuario/`, dto);
  }

  atualizarUsuario(uuid: string, dto: UsuarioAtualizacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.api}/usuario/${uuid}`, dto);
  }

  desbloquearUsuario(uuid: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.api}/usuario/${uuid}/desbloqueio`, null);
  }
}
