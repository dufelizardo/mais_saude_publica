import { DOCUMENT } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { TipoIdentificadorLogin } from '../../../core/models/auth';
import { formatCpf } from '../../../shared/format-mask';

type FontSize = 'default' | 'lg' | 'xl';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
})
export class Login {
  private readonly document = inject(DOCUMENT);
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly fontSize = signal<FontSize>('default');
  protected readonly highContrast = signal(false);
  protected readonly aba = signal<TipoIdentificadorLogin>('CPF');
  protected readonly mostrarSenha = signal(false);
  protected readonly enviando = signal(false);
  protected readonly erroServidor = signal<string | null>(null);
  protected readonly identificadorInvalido = signal(false);
  protected readonly senhaInvalida = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    identificador: ['', Validators.required],
    senha: ['', Validators.required],
    manterConectado: [false],
  });

  constructor() {
    this.restaurarPreferencias();
  }

  protected selecionarAba(aba: TipoIdentificadorLogin): void {
    this.aba.set(aba);
    this.form.controls.identificador.setValue('');
    this.identificadorInvalido.set(false);
    this.erroServidor.set(null);
  }

  protected onIdentificadorInput(event: Event): void {
    if (this.aba() !== 'CPF') {
      return;
    }
    const input = event.target as HTMLInputElement;
    this.form.controls.identificador.setValue(formatCpf(input.value), { emitEvent: false });
    this.identificadorInvalido.set(false);
  }

  protected toggleMostrarSenha(): void {
    this.mostrarSenha.update((visivel) => !visivel);
  }

  protected setFontSize(value: FontSize): void {
    const body = this.document.body;
    body.classList.remove('fs-lg', 'fs-xl');
    if (value === 'lg') body.classList.add('fs-lg');
    if (value === 'xl') body.classList.add('fs-xl');
    this.fontSize.set(value);
    this.salvarPreferencia('msp-fs', value);
  }

  protected toggleHighContrast(): void {
    const ativo = !this.highContrast();
    this.document.body.classList.toggle('hc', ativo);
    this.highContrast.set(ativo);
    this.salvarPreferencia('msp-hc', ativo ? '1' : '0');
  }

  protected enviar(): void {
    this.erroServidor.set(null);
    this.identificadorInvalido.set(false);
    this.senhaInvalida.set(false);

    const identificadorBruto = this.form.controls.identificador.value.trim();
    const senha = this.form.controls.senha.value;

    if (this.aba() === 'CPF') {
      if (identificadorBruto.replace(/\D/g, '').length !== 11) {
        this.identificadorInvalido.set(true);
      }
    } else if (identificadorBruto.length < 3) {
      this.identificadorInvalido.set(true);
    }

    if (senha.length < 8) {
      this.senhaInvalida.set(true);
    }

    if (this.identificadorInvalido() || this.senhaInvalida()) {
      return;
    }

    const identificador = this.aba() === 'CPF' ? identificadorBruto.replace(/\D/g, '') : identificadorBruto;

    this.enviando.set(true);
    this.authService
      .login({
        tipo: this.aba(),
        identificador,
        senha,
        manterConectado: this.form.controls.manterConectado.value,
      })
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.router.navigateByUrl('/');
        },
        error: (erro) => {
          this.enviando.set(false);
          this.erroServidor.set(erro?.error?.message ?? 'Não foi possível entrar. Tente novamente.');
        },
      });
  }

  private restaurarPreferencias(): void {
    try {
      const fsSalvo = localStorage.getItem('msp-fs') as FontSize | null;
      if (fsSalvo) this.setFontSize(fsSalvo);

      const hcSalvo = localStorage.getItem('msp-hc');
      if (hcSalvo === '1') this.toggleHighContrast();
    } catch {
      // localStorage indisponível (modo privado, etc.) — segue com o padrão.
    }
  }

  private salvarPreferencia(chave: string, valor: string): void {
    try {
      localStorage.setItem(chave, valor);
    } catch {
      // localStorage indisponível — preferência só não persiste entre sessões.
    }
  }
}
