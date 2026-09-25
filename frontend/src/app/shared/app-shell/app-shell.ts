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
}
