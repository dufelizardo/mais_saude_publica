import { Component, HostListener, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-modal',
  imports: [],
  templateUrl: './modal.html',
  styleUrl: './modal.css',
})
export class Modal {
  @Input({ required: true }) titulo!: string;
  @Output() fechar = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.fechar.emit();
  }

  onOverlayClick(): void {
    this.fechar.emit();
  }

  onPanelClick(event: MouseEvent): void {
    event.stopPropagation();
  }
}
