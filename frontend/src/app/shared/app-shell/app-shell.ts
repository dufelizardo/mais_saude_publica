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
import { AuthService } from '../../core/services/auth';
import { AcessoDaInterface, UsuarioAtualResponseDto } from '../../core/models/auth';

const RH = ['RH.CONSULTAR', 'RH.GERENCIAR'];
const ADMINISTRATIVO = ['ADMINISTRATIVO.CONSULTAR', 'ADMINISTRATIVO.GERENCIAR'];

/**
 * Permissões que mostram cada item do menu (ADR-0068) — basta uma. Sem entrada, o item é aberto a todo
 * usuário logado (estrutura e quadro de profissionais, ADR-0067). A API continua decidindo; o menu só
 * não oferece o que responderia 403.
 */
const MENU: Record<string, string[]> = {
  '/profissionais/novo': ['RH.GERENCIAR'],
  '/profissionais/desligar': ['RH.GERENCIAR'],
  '/rh': RH,
  '/administrativo': ADMINISTRATIVO,
  '/administrativo/setores': [],
  '/administrativo/necessidades-de-pessoal': [...ADMINISTRATIVO, ...RH],
  '/assistencia/pacientes': ['PACIENTE.CONSULTAR'],
  '/assistencia/atendimentos': ['ATENDIMENTO.GERENCIAR', 'PRONTUARIO.CONSULTAR', 'AGENDAMENTO.GERENCIAR'],
  '/assistencia/farmacia': ['FARMACIA.CONSULTAR', 'FARMACIA.DISPENSAR', 'FARMACIA.TRANSFERIR', 'FARMACIA.GERENCIAR_ESTOQUE'],
  '/administracao/usuarios': ['ACESSO.GERENCIAR', 'USUARIO.GERENCIAR'],
  '/administracao/auditoria': ['AUDITORIA.CONSULTAR'],
};

/** Itens de cada grupo, para esconder o grupo inteiro quando nenhum item aparece. */
const GRUPOS: Record<string, string[]> = {
  Assistência: ['/assistencia/pacientes', '/assistencia/atendimentos', '/assistencia/farmacia'],
  Administração: ['/administracao/usuarios', '/administracao/auditoria'],
};

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app-shell.html',
  styleUrl: './app-shell.css',
})
export class AppShell {
  protected readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);

  /** Quem está logado (ADR-0065); nulo com o login desligado. */
  protected readonly usuario = signal<UsuarioAtualResponseDto | null>(null);

  /** O que o menu mostra (ADR-0068): tudo, com a autorização desligada. */
  private readonly acesso = signal<AcessoDaInterface>({ restrito: false, permissoes: new Set() });

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
    new Set(['Recursos Humanos', 'Administrativo', 'Assistência', 'Administração']),
  );

  constructor() {
    this.authService.usuarioAtual().subscribe((u) => this.usuario.set(u));
    this.authService.acessoDaInterface().subscribe((a) => this.acesso.set(a));
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

  /** Item do menu visível: a entrada mais específica de MENU que casa com a rota decide. */
  protected podeVer(rota: string): boolean {
    const acesso = this.acesso();
    if (!acesso.restrito) return true;
    const chave = Object.keys(MENU)
      .filter((k) => rota === k || rota.startsWith(k + '/'))
      .sort((x, y) => y.length - x.length)[0];
    const permissoes = chave ? MENU[chave] : [];
    return !permissoes.length || permissoes.some((p) => acesso.permissoes.has(p));
  }

  protected grupoVisivel(grupo: string): boolean {
    const itens = GRUPOS[grupo];
    return !itens || itens.some((rota) => this.podeVer(rota));
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

  protected sair(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
