import { DOCUMENT } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { VersaoService } from '../../../core/services/versao';
import { TipoIdentificadorLogin } from '../../../core/models/auth';
import { formatCpf } from '../../../shared/format-mask';

type FontSize = 'default' | 'lg' | 'xl';

// Primeira tela do menu interno — não existe uma página inicial própria da área logada.
const DESTINO_PADRAO = '/profissionais';

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
  private readonly route = inject(ActivatedRoute);

  protected readonly versao = inject(VersaoService).versao;

  protected readonly fontSize = signal<FontSize>('default');
  protected readonly highContrast = signal(false);
  protected readonly aba = signal<TipoIdentificadorLogin>('CPF');
  protected readonly mostrarSenha = signal(false);
  protected readonly enviando = signal(false);
  /** "Esqueci minha senha" por e-mail neste ambiente (ADR-0081); sem SMTP, só a administração redefine. */
  protected readonly recuperacaoDeSenha = signal(false);
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
    this.authService.recuperacaoDeSenhaDisponivel().subscribe((d) => this.recuperacaoDeSenha.set(d));
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
        next: (resposta) => {
          this.enviando.set(false);
          if (resposta.trocarSenha) {
            // Senha provisória (ADR-0069): a troca vem antes de qualquer tela.
            this.router.navigate(['/trocar-senha'], { queryParams: { returnUrl: this.destinoAposLogin() } });
            return;
          }
          this.router.navigateByUrl(this.destinoAposLogin());
        },
        error: (erro) => {
          this.enviando.set(false);
          this.erroServidor.set(erro?.error?.message ?? 'Não foi possível entrar. Tente novamente.');
        },
      });
  }

  /**
   * Só aceita caminho interno da própria aplicação ("/algo", nunca "//host" nem URL absoluta) —
   * `returnUrl` vem da query string, então não dá para confiar nele cegamente.
   */
  private destinoAposLogin(): string {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    const ehCaminhoInterno =
      !!returnUrl && returnUrl.startsWith('/') && !returnUrl.startsWith('//') && !returnUrl.startsWith('/login');
    return ehCaminhoInterno ? returnUrl : DESTINO_PADRAO;
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
