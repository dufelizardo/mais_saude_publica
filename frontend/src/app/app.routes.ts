import { Routes } from '@angular/router';
import { LandingPage } from './features/landing/landing-page';
import { ProfissionalCadastro } from './features/profissional-cadastro/profissional-cadastro';

export const routes: Routes = [
  { path: '', component: LandingPage },
  { path: 'profissionais/novo', component: ProfissionalCadastro },
];
