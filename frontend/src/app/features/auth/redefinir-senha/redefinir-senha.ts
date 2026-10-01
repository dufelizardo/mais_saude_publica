import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

/**
 * Senha nova pelo link do e-mail (ADR-0081). O token vem na URL; a API confere se ainda vale. Depois de trocar,
 * a pessoa entra pela tela de login — as sessões abertas antes foram encerradas (ADR-0078).
 */
@Component({
  selector: 'app-redefinir-senha',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './redefinir-senha.html',
})
export class RedefinirSenha {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token') ?? '';

  protected readonly semToken = !this.token;
  protected readonly mostrar = signal(false);
  protected readonly enviando = signal(false);
  protected readonly erros = signal<Record<string, string>>({});
  protected readonly erroServidor = signal<string | null>(null);
  /** O link não vale mais (422): oferece pedir outro. */
  protected readonly linkInvalido = signal(false);
  protected readonly concluido = signal(false);

  protected readonly form = this.fb.nonNullable.group({ novaSenha: [''], confirmacao: [''] });

  protected erro(campo: string): string {
    return this.erros()[campo] ?? '';
  }

  /** Requisitos conferidos na tela; "não pode ser o CPF" fica com a API, que sabe de quem é o link. */
  protected requisitos(): { ok: boolean; texto: string }[] {
    const v = this.form.getRawValue();
    return [
      { ok: v.novaSenha.length >= 8 && v.novaSenha.length <= 72, texto: 'De 8 a 72 caracteres' },
      { ok: !!v.novaSenha && v.novaSenha === v.confirmacao, texto: 'Confirmação igual à nova senha' },
    ];
  }

  protected enviar(): void {
    const v = this.form.getRawValue();
    const erros: Record<string, string> = {};
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
    this.authService.redefinirSenhaPorLink(this.token, v.novaSenha).subscribe({
      next: () => {
        this.enviando.set(false);
        this.concluido.set(true);
      },
      error: (e) => {
        this.enviando.set(false);
        this.linkInvalido.set(e?.status === 422);
        this.erroServidor.set(e?.error?.message ?? 'Não foi possível criar a senha. Tente novamente.');
      },
    });
  }
}
