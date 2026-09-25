import { Component, inject, signal } from '@angular/core';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app-shell.html',
  styleUrl: './app-shell.css',
})
export class AppShell {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly breadcrumb = signal('');
  protected readonly area = signal('');
  protected readonly sidebarAberta = signal(false);

  /**
   * Grupos do menu que estão expandidos — todos começam abertos (mesmo visual de antes do
   * acordeão). O estado vive aqui, não em cada rota, porque o AppShell nunca é destruído entre
   * navegações — não precisa de persistência em localStorage pra sobreviver a troca de página
   * dentro da mesma sessão.
   */
  protected readonly gruposExpandidos = signal<ReadonlySet<string>>(
    new Set(['Recursos Humanos', 'Administrativo', 'Assistência']),
  );

  constructor() {
    this.atualizarBreadcrumb();
    this.router.events.pipe(filter((evento) => evento instanceof NavigationEnd)).subscribe(() => {
      this.atualizarBreadcrumb();
      this.sidebarAberta.set(false);
    });
  }

  private atualizarBreadcrumb(): void {
    let rota = this.route.snapshot;
    while (rota.firstChild) {
      rota = rota.firstChild;
    }
    this.breadcrumb.set((rota.data['breadcrumb'] as string) ?? '');
    this.area.set((rota.data['area'] as string) ?? '');
  }

  protected toggleSidebar(): void {
    this.sidebarAberta.update((aberta) => !aberta);
  }

  protected estaExpandido(grupo: string): boolean {
    return this.gruposExpandidos().has(grupo);
  }

  protected toggleGrupo(grupo: string): void {
    this.gruposExpandidos.update((atual) => {
      const novo = new Set(atual);
      if (novo.has(grupo)) {
        novo.delete(grupo);
      } else {
        novo.add(grupo);
      }
      return novo;
    });
  }
}
