import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DocumentosDoPacienteDto, TipoDocumentoDto } from '../models/documento';

/** Documentos (ADR-0172): o catálogo e os anexos do paciente. */
@Injectable({ providedIn: 'root' })
export class DocumentoService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  tipos(): Observable<TipoDocumentoDto[]> {
    return this.http.get<TipoDocumentoDto[]>(`${this.api}/tipo-documento`);
  }

  criarTipo(dto: Pick<TipoDocumentoDto, 'codigo' | 'nome' | 'ativo'>): Observable<TipoDocumentoDto> {
    return this.http.post<TipoDocumentoDto>(`${this.api}/tipo-documento`, dto);
  }

  atualizarTipo(uuid: string, dto: Pick<TipoDocumentoDto, 'codigo' | 'nome' | 'ativo'>): Observable<TipoDocumentoDto> {
    return this.http.patch<TipoDocumentoDto>(`${this.api}/tipo-documento/${uuid}`, dto);
  }

  doPaciente(pacienteId: string): Observable<DocumentosDoPacienteDto> {
    return this.http.get<DocumentosDoPacienteDto>(`${this.api}/paciente/${pacienteId}/documentos`);
  }

  anexar(pacienteId: string, dados: { tipoId: string; unidadeId: string; titulo: string; dataDocumento: string }, arquivo: File): Observable<DocumentosDoPacienteDto> {
    const corpo = new FormData();
    corpo.set('tipoId', dados.tipoId);
    corpo.set('unidadeId', dados.unidadeId);
    corpo.set('titulo', dados.titulo);
    if (dados.dataDocumento) corpo.set('dataDocumento', dados.dataDocumento);
    corpo.set('arquivo', arquivo);
    return this.http.post<DocumentosDoPacienteDto>(`${this.api}/paciente/${pacienteId}/documento`, corpo);
  }

  novaVersao(documentoId: string, arquivo: File): Observable<DocumentosDoPacienteDto> {
    const corpo = new FormData();
    corpo.set('arquivo', arquivo);
    return this.http.post<DocumentosDoPacienteDto>(`${this.api}/documento/${documentoId}/versao`, corpo);
  }

  retirar(documentoId: string, motivo: string): Observable<DocumentosDoPacienteDto> {
    return this.http.post<DocumentosDoPacienteDto>(`${this.api}/documento/${documentoId}/retirada`, { motivo });
  }

  abrir(versaoId: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.api}/documento/versao/${versaoId}/arquivo`, { responseType: 'blob', observe: 'response' });
  }
}
