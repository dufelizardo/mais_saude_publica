import { Routes } from '@angular/router';
import { LandingPage } from './features/landing/landing-page';
import { ProfissionalCadastro } from './features/profissional-cadastro/profissional-cadastro';
import { ProfissionalDesligar } from './features/profissional-desligar/profissional-desligar';
import { ProfissionalPerfil } from './features/profissional-perfil/profissional-perfil';
import { CategoriasSalariais } from './features/rh/categorias-salariais/categorias-salariais';
import { Cargos } from './features/rh/cargos/cargos';
import { CargoTabelaSalarial } from './features/rh/cargo-tabela-salarial/cargo-tabela-salarial';
import { RegrasAnuenio } from './features/rh/regras-anuenio/regras-anuenio';
import { FolhaPagamento } from './features/rh/folha-pagamento/folha-pagamento';
import { Treinamentos } from './features/rh/treinamentos/treinamentos';
import { CiclosAvaliacao } from './features/rh/ciclos-avaliacao/ciclos-avaliacao';
import { AppShell } from './shared/app-shell/app-shell';

export const routes: Routes = [
  { path: '', component: LandingPage },
  {
    path: '',
    component: AppShell,
    children: [
      { path: 'profissionais/novo', component: ProfissionalCadastro, data: { breadcrumb: 'Cadastrar profissional' } },
      { path: 'profissionais/desligar', component: ProfissionalDesligar, data: { breadcrumb: 'Desligar profissional' } },
      { path: 'profissionais/perfil', component: ProfissionalPerfil, data: { breadcrumb: 'Perfil do profissional' } },
      { path: 'rh/categorias-salariais', component: CategoriasSalariais, data: { breadcrumb: 'Categorias salariais' } },
      { path: 'rh/cargos', component: Cargos, data: { breadcrumb: 'Cargos' } },
      { path: 'rh/cargos/:cargoId/tabela-salarial', component: CargoTabelaSalarial, data: { breadcrumb: 'Tabela salarial' } },
      { path: 'rh/regras-anuenio', component: RegrasAnuenio, data: { breadcrumb: 'Regras de anuênio' } },
      { path: 'rh/folha-pagamento', component: FolhaPagamento, data: { breadcrumb: 'Folha de pagamento' } },
      { path: 'rh/treinamentos', component: Treinamentos, data: { breadcrumb: 'Catálogo de treinamentos' } },
      { path: 'rh/ciclos-avaliacao', component: CiclosAvaliacao, data: { breadcrumb: 'Ciclos de avaliação' } },
    ],
  },
];
