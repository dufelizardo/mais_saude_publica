import { Component, ElementRef, EventEmitter, HostListener, Input, OnDestroy, Output, afterNextRender, inject } from '@angular/core';

/**
 * Gaveta lateral (painel à direita) para formulários longos — portada do mockup da Farmácia.
 * O conteúdo projetado usa `.dw-body` e `.dw-foot` (estilos globais em styles.css), para que um
 * `<form>` possa envolver corpo e rodapé juntos.
 */
@Component({
  selector: 'app-drawer',
  imports: [],
  templateUrl: './drawer.html',
  styleUrl: './drawer.css',
})
export class Drawer implements OnDestroy {
  @Input({ required: true }) titulo!: string;
  /** Gaveta larga (760px), para painéis de leitura com várias seções — ex.: o atendimento (ADR-0063). */
  @Input() larga = false;
  @Output() fechar = new EventEmitter<void>();

  private readonly host = inject(ElementRef<HTMLElement>);
  private readonly focoAnterior = document.activeElement as HTMLElement | null;

  constructor() {
    document.body.style.overflow = 'hidden';
    afterNextRender(() => {
      const primeiro = this.host.nativeElement.querySelector(
        '.dw-body input:not([type=radio]):not([type=checkbox]), .dw-body select, .dw-body textarea',
      ) as HTMLElement | null;
      (primeiro ?? this.focaveis()[0])?.focus();
    });
  }

  ngOnDestroy(): void {
    document.body.style.overflow = '';
    this.focoAnterior?.focus?.();
  }

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      this.fechar.emit();
      return;
    }
    if (event.key !== 'Tab') return;
    const focaveis = this.focaveis();
    if (!focaveis.length) return;
    const primeiro = focaveis[0];
    const ultimo = focaveis[focaveis.length - 1];
    if (event.shiftKey && document.activeElement === primeiro) {
      event.preventDefault();
      ultimo.focus();
    } else if (!event.shiftKey && document.activeElement === ultimo) {
      event.preventDefault();
      primeiro.focus();
    }
  }

  private focaveis(): HTMLElement[] {
    const lista = this.host.nativeElement.querySelectorAll(
      'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled])',
    ) as NodeListOf<HTMLElement>;
    return Array.from(lista).filter((el) => el.offsetParent !== null);
  }
}
