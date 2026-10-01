import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

const DESTINO_PADRAO = '/profissionais';

/**
 * Troca da própria senha (ADR-0069). Obrigatória com senha provisória — cadastrada ou redefinida pela
 * administração —, quando a API só atende /auth até a troca; opcional a qualquer momento, pela barra superior.
 */
@Component({
  selector: 'app-trocar-senha',
  imports: [ReactiveFormsModule],
  templateUrl: './trocar-senha.html',
})
export class TrocarSenha {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  /** Senha provisória: a troca é obrigatória e não há "voltar". */
  protected readonly obrigatoria = signal(false);
  protected readonly nome = signal('');
  private cpf = '';
  protected readonly mostrar = signal(false);
  protected readonly enviando = signal(false);
  protected readonly erroServidor = signal<string | null>(null);
  protected readonly erros = signal<Record<string, string>>({});

  protected readonly form = this.fb.nonNullable.group({
    senhaAtual: [''],
    novaSenha: [''],
    confirmacao: [''],
  });

  constructor() {
    this.authService.usuarioAtual().subscribe((u) => {
      this.obrigatoria.set(!!u?.trocarSenha);
      this.nome.set(u?.profissionalNome || u?.nome || '');
      this.cpf = u?.cpf ?? '';
    });
  }

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  /** Requisitos mostrados enquanto a pessoa digita — os mesmos que a API confere. */
  protected requisitos(): { ok: boolean; texto: string }[] {
    const v = this.form.getRawValue();
    const digitos = v.novaSenha.replace(/\D/g, '');
    return [
      { ok: v.novaSenha.length >= 8 && v.novaSenha.length <= 72, texto: 'De 8 a 72 caracteres' },
      { ok: !!v.novaSenha && v.novaSenha !== v.senhaAtual, texto: 'Diferente da senha atual' },
      { ok: !!v.novaSenha && !(digitos === this.cpf && /^[\d.\-\s]+$/.test(v.novaSenha)), texto: 'Não é o seu CPF' },
      { ok: !!v.novaSenha && v.novaSenha === v.confirmacao, texto: 'Confirmação igual à nova senha' },
    ];
  }

  protected enviar(): void {
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
    if (!v.senhaAtual) erros['senhaAtual'] = 'Informe a senha atual.';
    const falha = this.requisitos().find((r) => !r.ok);
    if (falha && v.novaSenha !== v.confirmacao && v.novaSenha.length >= 8) {
      erros['confirmacao'] = 'A confirmação não confere com a nova senha.';
    } else if (falha) {
      erros['novaSenha'] = `A nova senha não atende: ${falha.texto.toLowerCase()}.`;
    }
    this.erros.set(erros);
    this.erroServidor.set(null);
    const primeiro = Object.keys(erros)[0];
    if (primeiro) {
      document.getElementById('f-' + primeiro)?.focus();
      return;
    }
    this.enviando.set(true);
    this.authService.trocarSenha(v.senhaAtual, v.novaSenha).subscribe({
      next: () => {
        this.enviando.set(false);
        this.router.navigateByUrl(this.destino(), { state: { senhaTrocada: true } });
      },
      error: (e) => {
        this.enviando.set(false);
        this.erroServidor.set(e?.error?.message ?? 'Não foi possível trocar a senha. Tente novamente.');
      },
    });
  }

  protected voltar(): void {
    this.router.navigateByUrl(this.destino());
  }

  protected sair(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }

  /** Só caminho interno da aplicação — `returnUrl` vem da query string. */
  private destino(): string {
    const r = this.route.snapshot.queryParamMap.get('returnUrl');
    const interno = !!r && r.startsWith('/') && !r.startsWith('//') && !r.startsWith('/login') && !r.startsWith('/trocar-senha');
    return interno ? r : DESTINO_PADRAO;
  }
}
