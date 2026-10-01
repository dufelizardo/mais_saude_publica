import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { formatCpf } from '../../../shared/format-mask';

/**
 * "Esqueci minha senha" (ADR-0081): a pessoa informa o CPF e recebe por e-mail um link para criar uma senha
 * nova. A resposta é a mesma com ou sem cadastro — a tela nunca diz se o CPF existe.
 */
@Component({
  selector: 'app-recuperar-senha',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './recuperar-senha.html',
})
export class RecuperarSenha {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  /** null enquanto carrega o status do ambiente. */
  protected readonly disponivel = signal<boolean | null>(null);
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroServidor = signal<string | null>(null);
  /** Mensagem da API depois do pedido. */
  protected readonly enviado = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({ cpf: [''] });

  constructor() {
    this.authService.recuperacaoDeSenhaDisponivel().subscribe((d) => this.disponivel.set(d));
  }

  protected onCpfInput(event: Event): void {
    this.form.controls.cpf.setValue(formatCpf((event.target as HTMLInputElement).value));
  }

  protected enviar(): void {
    const cpf = this.form.getRawValue().cpf.replace(/\D/g, '');
    this.erroServidor.set(null);
    if (cpf.length !== 11) {
      this.erro.set('Informe os 11 dígitos do CPF.');
      document.getElementById('f-cpf')?.focus();
      return;
    }
    this.erro.set(null);
    this.enviando.set(true);
    this.authService.solicitarRecuperacaoDeSenha(cpf).subscribe({
      next: (r) => {
        this.enviando.set(false);
        this.enviado.set(r.message);
      },
      error: (e) => {
        this.enviando.set(false);
        this.erroServidor.set(e?.error?.message ?? 'Não foi possível enviar o pedido. Tente novamente.');
      },
    });
  }
}
