import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, map, of } from 'rxjs';

/**
 * Versão da release em execução, gravada na imagem do frontend como /version.json pelo CI a partir
 * do pom.xml (ver publish-image.yml e VERSIONING.md). Em build local o arquivo não existe — o sinal
 * fica `null` e as telas simplesmente não exibem versão, em vez de mostrar um número inventado.
 */
@Injectable({ providedIn: 'root' })
export class VersaoService {
  private readonly http = inject(HttpClient);

  readonly versao = toSignal(
    this.http.get<{ version: string }>('/version.json').pipe(
      map((resposta) => resposta.version || null),
      catchError(() => of(null)),
    ),
    { initialValue: null },
  );
}
