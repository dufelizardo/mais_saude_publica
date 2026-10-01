import { inject } from '@angular/core';
import { Routes, Params, Router } from '@angular/router';
import { LandingPage } from './features/landing/landing-page';
import { ProfissionaisLista } from './features/profissionais-lista/profissionais-lista';
import { ProfissionalPerfil } from './features/profissional-perfil/profissional-perfil';
import { FolhaPagamento } from './features/rh/folha-pagamento/folha-pagamento';
import { Setores } from './features/administrativo/setores/setores';
import { Pacientes } from './features/assistencia/pacientes/pacientes';
import { Atendimentos } from './features/assistencia/atendimentos/atendimentos';
import { Farmacia } from './features/assistencia/farmacia/farmacia';
import { AppShell } from './shared/app-shell/app-shell';
import { Login } from './features/auth/login/login';
import { authGuard, trocaDeSenhaGuard } from './core/guards/auth-guard';
import { TrocarSenha } from './features/auth/trocar-senha/trocar-senha';

export const routes: Routes = [
  { path: '', component: LandingPage },
  { path: 'login', component: Login },
  { path: 'trocar-senha', component: TrocarSenha, canActivate: [trocaDeSenhaGuard] },
  // Recuperação de senha por e-mail (ADR-0081): públicas, fora do AppShell.
  { path: 'recuperar-senha', loadComponent: () => import('./features/auth/recuperar-senha/recuperar-senha').then((m) => m.RecuperarSenha) },
  { path: 'redefinir-senha', loadComponent: () => import('./features/auth/redefinir-senha/redefinir-senha').then((m) => m.RedefinirSenha) },
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: 'profissionais', component: ProfissionaisLista, data: { breadcrumb: 'Profissionais da Saúde', area: 'Recursos Humanos' } },
      // Cadastro e desligamento viraram gavetas da tela Profissionais (ADR-0072).
      { path: 'profissionais/novo', redirectTo: () => inject(Router).createUrlTree(['/profissionais'], { queryParams: { acao: 'novo' } }) },
      {
        path: 'profissionais/desligar',
        redirectTo: ({ queryParams }) => inject(Router).createUrlTree(['/profissionais'], { queryParams: { acao: 'desligar', ...queryParams } }),
      },
      { path: 'profissionais/perfil', component: ProfissionalPerfil, data: { breadcrumb: 'Perfil do profissional', area: 'Recursos Humanos' } },
      // Catálogos de RH agrupados em telas com abas (ADR-0073), carregadas sob demanda.
      {
        path: 'rh/cargos-e-salarios',
        loadComponent: () => import('./features/rh/cargos-e-salarios/cargos-e-salarios').then((m) => m.CargosESalarios),
        data: { breadcrumb: 'Cargos & salários', area: 'Recursos Humanos' },
      },
      {
        path: 'rh/beneficios',
        loadComponent: () => import('./features/rh/beneficios/beneficios').then((m) => m.Beneficios),
        data: { breadcrumb: 'Benefícios', area: 'Recursos Humanos' },
      },
      {
        path: 'rh/desenvolvimento',
        loadComponent: () => import('./features/rh/desenvolvimento/desenvolvimento').then((m) => m.Desenvolvimento),
        data: { breadcrumb: 'Desenvolvimento', area: 'Recursos Humanos' },
      },
      {
        path: 'rh/recrutamento',
        loadComponent: () => import('./features/rh/recrutamento/recrutamento').then((m) => m.Recrutamento),
        data: { breadcrumb: 'Recrutamento', area: 'Recursos Humanos' },
      },
      { path: 'rh/folha-pagamento', component: FolhaPagamento, data: { breadcrumb: 'Folha de pagamento', area: 'Recursos Humanos' } },
      // Rotas antigas dos catálogos → a aba (ou a gaveta) certa na tela nova.
      { path: 'rh/categorias-salariais', redirectTo: () => para('/rh/cargos-e-salarios', { aba: 'categorias' }) },
      { path: 'rh/cargos', redirectTo: () => para('/rh/cargos-e-salarios', {}) },
      { path: 'rh/cargos/:cargoId/tabela-salarial', redirectTo: ({ params }) => para('/rh/cargos-e-salarios', { cargo: params['cargoId'] }) },
      { path: 'rh/regras-anuenio', redirectTo: () => para('/rh/cargos-e-salarios', { aba: 'anuenio' }) },
      { path: 'rh/tipos-beneficio', redirectTo: () => para('/rh/beneficios', {}) },
      { path: 'rh/tipos-beneficio/:tipoId/valores', redirectTo: ({ params }) => para('/rh/beneficios', { tipo: params['tipoId'] }) },
      { path: 'rh/treinamentos', redirectTo: () => para('/rh/desenvolvimento', {}) },
      { path: 'rh/ciclos-avaliacao', redirectTo: () => para('/rh/desenvolvimento', { aba: 'ciclos' }) },
      { path: 'rh/vagas', redirectTo: () => para('/rh/recrutamento', {}) },
      { path: 'rh/vagas/:vagaId/candidatos', redirectTo: ({ params }) => para('/rh/recrutamento', { vaga: params['vagaId'] }) },
      { path: 'administrativo/setores', component: Setores, data: { breadcrumb: 'Setores', area: 'Administrativo' } },
      {
        path: 'administrativo/modelo',
        loadComponent: () => import('./features/administrativo/modelo-administrativo/modelo-administrativo').then((m) => m.ModeloAdministrativo),
        data: { breadcrumb: 'Modelo administrativo', area: 'Administrativo' },
      },
      {
        path: 'administrativo/necessidades-de-pessoal',
        loadComponent: () => import('./features/administrativo/necessidades-de-pessoal/necessidades-de-pessoal').then((m) => m.NecessidadesDePessoal),
        data: { breadcrumb: 'Necessidades de pessoal', area: 'Administrativo' },
      },
      // Telas antigas do Administrativo → a aba certa nas telas agrupadas (ADR-0074).
      { path: 'administrativo/responsabilidades', redirectTo: ({ queryParams }) => para('/administrativo/setores', { ...queryParams, aba: 'responsabilidades' }) },
      { path: 'administrativo/capacidades', redirectTo: () => para('/administrativo/modelo', { aba: 'capacidades' }) },
      { path: 'administrativo/processos', redirectTo: () => para('/administrativo/modelo', { aba: 'processos' }) },
      { path: 'administrativo/perfis', redirectTo: () => para('/administrativo/modelo', { aba: 'perfis' }) },
      { path: 'administrativo/perfis-por-tipo-unidade', redirectTo: () => para('/administrativo/modelo', { aba: 'tipos' }) },
      { path: 'assistencia/pacientes', component: Pacientes, data: { breadcrumb: 'Pacientes', area: 'Assistência' } },
      { path: 'assistencia/atendimentos', component: Atendimentos, data: { breadcrumb: 'Atendimentos', area: 'Assistência' } },
      // Agendamentos, Consultas, Procedimentos e Prontuário viraram abas/seções da tela Atendimentos (ADR-0063).
      { path: 'assistencia/agendamentos', redirectTo: ({ queryParams }) => paraAtendimentos('ag', queryParams) },
      { path: 'assistencia/consultas', redirectTo: ({ queryParams }) => paraAtendimentos('atend', queryParams) },
      { path: 'assistencia/procedimentos', redirectTo: ({ queryParams }) => paraAtendimentos('atend', queryParams) },
      { path: 'assistencia/prontuario', redirectTo: ({ queryParams }) => paraAtendimentos('pront', queryParams) },
      { path: 'assistencia/farmacia', component: Farmacia, data: { breadcrumb: 'Farmácia', area: 'Assistência' } },
      {
        path: 'administracao/usuarios',
        // Sob demanda: tela de administração, fora do bundle inicial (orçamento de 1 MB).
        loadComponent: () => import('./features/administracao/usuarios/usuarios').then((m) => m.Usuarios),
        data: { breadcrumb: 'Usuários & Perfis', area: 'Administração' },
      },
      {
        path: 'administracao/auditoria',
        loadComponent: () => import('./features/administracao/auditoria/auditoria').then((m) => m.Auditoria),
        data: { breadcrumb: 'Auditoria', area: 'Administração' },
      },
    ],
  },
];

function para(caminho: string, queryParams: Params) {
  return inject(Router).createUrlTree([caminho], { queryParams });
}

function paraAtendimentos(aba: string, queryParams: Params) {
  return inject(Router).createUrlTree(['/assistencia/atendimentos'], { queryParams: { ...queryParams, aba } });
}
