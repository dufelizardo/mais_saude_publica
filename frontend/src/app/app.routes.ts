import { Routes } from '@angular/router';
import { LandingPage } from './features/landing/landing-page';
import { ProfissionalCadastro } from './features/profissional-cadastro/profissional-cadastro';
import { ProfissionalDesligar } from './features/profissional-desligar/profissional-desligar';
import { CategoriasSalariais } from './features/rh/categorias-salariais/categorias-salariais';
import { Cargos } from './features/rh/cargos/cargos';
import { CargoTabelaSalarial } from './features/rh/cargo-tabela-salarial/cargo-tabela-salarial';
import { RegrasAnuenio } from './features/rh/regras-anuenio/regras-anuenio';

export const routes: Routes = [
  { path: '', component: LandingPage },
  { path: 'profissionais/novo', component: ProfissionalCadastro },
  { path: 'profissionais/desligar', component: ProfissionalDesligar },
  { path: 'rh/categorias-salariais', component: CategoriasSalariais },
  { path: 'rh/cargos', component: Cargos },
  { path: 'rh/cargos/:cargoId/tabela-salarial', component: CargoTabelaSalarial },
  { path: 'rh/regras-anuenio', component: RegrasAnuenio },
];
