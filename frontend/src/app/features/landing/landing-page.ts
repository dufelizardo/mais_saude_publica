import { DOCUMENT } from '@angular/common';
import { Component, inject, signal } from '@angular/core';

type FontSize = 'default' | 'lg' | 'xl';

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.html',
})
export class LandingPage {
  private readonly document = inject(DOCUMENT);

  protected readonly fontSize = signal<FontSize>('default');
  protected readonly highContrast = signal(false);
  protected readonly mobileMenuOpen = signal(false);

  constructor() {
    this.restorePreferences();
  }

  protected setFontSize(value: FontSize): void {
    const body = this.document.body;
    body.classList.remove('fs-lg', 'fs-xl');
    if (value === 'lg') body.classList.add('fs-lg');
    if (value === 'xl') body.classList.add('fs-xl');
    this.fontSize.set(value);
    this.savePreference('msp-fs', value);
  }

  protected toggleHighContrast(): void {
    const enabled = !this.highContrast();
    this.document.body.classList.toggle('hc', enabled);
    this.highContrast.set(enabled);
    this.savePreference('msp-hc', enabled ? '1' : '0');
  }

  protected toggleMobileMenu(): void {
    this.mobileMenuOpen.update((open) => !open);
  }

  protected closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }

  private restorePreferences(): void {
    try {
      const savedFs = localStorage.getItem('msp-fs') as FontSize | null;
      if (savedFs) this.setFontSize(savedFs);

      const savedHc = localStorage.getItem('msp-hc');
      if (savedHc === '1') this.toggleHighContrast();
    } catch {
      // localStorage indisponível (modo privado, etc.) — segue com o padrão.
    }
  }

  private savePreference(key: string, value: string): void {
    try {
      localStorage.setItem(key, value);
    } catch {
      // localStorage indisponível — preferência só não persiste entre sessões.
    }
  }
}
