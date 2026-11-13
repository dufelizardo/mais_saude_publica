import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SITUACOES_LICENCA, TIPOS_TERMO, VerificacaoLicencaDto, VerificacaoTermoDto } from '../../../core/models/vigilancia-sanitaria';
import { VigilanciaSanitariaService } from '../../../core/services/vigilancia-sanitaria';

/**
 * Verificação pública da vigilância sanitária (ADR-0200), sem login: o termo pelo código impresso ou lido no QR code (auto de
 * infração, decisão, interdição, apreensão, desinterdição) e a licença sanitária pelo número, como o alvará afixado.
 */
@Component({
  selector: 'app-verificar-visa',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './verificar-visa.html',
})
export class VerificarVisa {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(VigilanciaSanitariaService);
  private readonly rota = inject(ActivatedRoute).snapshot.queryParamMap;

  protected readonly tiposTermo = TIPOS_TERMO;
  protected readonly situacoesLicenca = SITUACOES_LICENCA;
  protected readonly form = this.fb.nonNullable.group({ codigo: [this.rota.get('codigo') ?? ''], licenca: [this.rota.get('licenca') ?? ''] });
  protected readonly termo = signal<VerificacaoTermoDto | null>(null);
  protected readonly licenca = signal<VerificacaoLicencaDto | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroLicenca = signal<string | null>(null);
  protected readonly consultando = signal(false);

  constructor() {
    if (this.form.controls.codigo.value) this.verificarTermo();
    if (this.form.controls.licenca.value) this.verificarLicenca();
  }

  protected verificarTermo(): void {
    const codigo = this.form.controls.codigo.value.trim().toUpperCase();
    this.termo.set(null);
    this.erro.set(null);
    if (!/^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$/.test(codigo)) {
      this.erro.set('Digite o código como está no rodapé do termo, por exemplo ABCD-EFGH-JKLM.');
      return;
    }
    this.consultando.set(true);
    this.service.verificarTermo(codigo).subscribe({
      next: (r) => {
        this.consultando.set(false);
        this.termo.set(r);
      },
      error: (e: HttpErrorResponse) => {
        this.consultando.set(false);
        this.erro.set(e.status === 404 ? 'Nenhum termo foi emitido com este código. Confira as letras e os números.' : 'Não foi possível verificar agora. Tente de novo.');
      },
    });
  }

  protected verificarLicenca(): void {
    const numero = this.form.controls.licenca.value.trim();
    this.licenca.set(null);
    this.erroLicenca.set(null);
    if (!/^\d{4}\/\d{6}$/.test(numero)) {
      this.erroLicenca.set('Digite o número como está na licença, por exemplo 2026/000123.');
      return;
    }
    this.service.verificarLicenca(numero).subscribe({
      next: (r) => this.licenca.set(r),
      error: (e: HttpErrorResponse) => this.erroLicenca.set(e.status === 404 ? 'Nenhuma licença sanitária tem este número.' : 'Não foi possível verificar agora. Tente de novo.'),
    });
  }

  protected data(iso: string): string {
    const [a, m, d] = iso.slice(0, 10).split('-');
    return `${d}/${m}/${a}`;
  }

  protected dataHora(iso: string): string {
    const d = new Date(iso);
    const p = (v: number) => String(v).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} às ${p(d.getHours())}:${p(d.getMinutes())}`;
  }
}
