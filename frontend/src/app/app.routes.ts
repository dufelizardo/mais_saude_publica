import { Routes } from '@angular/router';
import { LandingPage } from './features/landing/landing-page';
import { ProfissionalCadastro } from './features/profissional-cadastro/profissional-cadastro';
import { ProfissionalDesligar } from './features/profissional-desligar/profissional-desligar';

export const routes: Routes = [
  { path: '', component: LandingPage },
  { path: 'profissionais/novo', component: ProfissionalCadastro },
  { path: 'profissionais/desligar', component: ProfissionalDesligar },
];
