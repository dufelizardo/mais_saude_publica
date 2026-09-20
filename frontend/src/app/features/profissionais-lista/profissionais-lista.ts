import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProfissionalResponseDto } from '../../core/models/profissional';
import { ProfissionalService } from '../../core/services/profissional';

type FiltroSituacao = 'todos' | 'ativos' | 'desligados';

@Component({
  selector: 'app-profissionais-lista',
  imports: [RouterLink],
  templateUrl: './profissionais-lista.html',
  styleUrl: './profissionais-lista.css',
})
export class ProfissionaisLista {
  private readonly profissionalService = inject(ProfissionalService);

  protected readonly profissionais = signal<ProfissionalResponseDto[]>([]);
  protected readonly carregando = signal(true);
  protected readonly busca = signal('');
  protected readonly filtroSituacao = signal<FiltroSituacao>('todos');

  protected readonly profissionaisFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const situacao = this.filtroSituacao();
    return this.profissionais().filter((p) => {
      const bateTermo = !termo || p.nome.toLowerCase().includes(termo) || p.cpf.includes(termo);
      const bateSituacao = situacao === 'todos' || (situacao === 'ativos' ? p.ativo : !p.ativo);
      return bateTermo && bateSituacao;
    });
  });

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.profissionalService.listar().subscribe({
      next: (profissionais) => {
        this.profissionais.set(profissionais);
        this.carregando.set(false);
      },
      error: () => {
        this.profissionais.set([]);
        this.carregando.set(false);
      },
    });
  }
}
