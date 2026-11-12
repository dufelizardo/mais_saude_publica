import { inject } from '@angular/core';
import { Routes, Params, Router } from '@angular/router';
import { LandingPage } from './features/landing/landing-page';
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
  // Verificação pública do laudo pelo código (ADR-0183): sem login.
  { path: 'verificar-laudo', loadComponent: () => import('./features/publico/verificar-laudo/verificar-laudo').then((m) => m.VerificarLaudo) },
  // Verificação pública dos termos e da licença sanitária (ADR-0200): sem login.
  { path: 'verificar-visa', loadComponent: () => import('./features/publico/verificar-visa/verificar-visa').then((m) => m.VerificarVisa) },
  // Laudo de exames (ADR-0095): fora do AppShell, para imprimir limpo.
  {
    path: 'laudo/exame/:pedidoId',
    canActivate: [authGuard],
    loadComponent: () => import('./features/assistencia/laboratorio/laudo/laudo-exame').then((m) => m.LaudoExame),
  },
  // Lista de busca ativa de vacinação (ADR-0149): fora do AppShell, para imprimir limpo.
  {
    path: 'busca-ativa/vacinacao',
    canActivate: [authGuard],
    loadComponent: () => import('./features/assistencia/sala-de-vacina/busca-ativa-vacinacao').then((m) => m.BuscaAtivaVacinacao),
  },
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: 'profissionais', loadComponent: () => import('./features/profissionais-lista/profissionais-lista').then((m) => m.ProfissionaisLista), data: { breadcrumb: 'Profissionais da Saúde', area: 'Recursos Humanos' } },
      // Cadastro e desligamento viraram gavetas da tela Profissionais (ADR-0072).
      { path: 'profissionais/novo', redirectTo: () => inject(Router).createUrlTree(['/profissionais'], { queryParams: { acao: 'novo' } }) },
      {
        path: 'profissionais/desligar',
        redirectTo: ({ queryParams }) => inject(Router).createUrlTree(['/profissionais'], { queryParams: { acao: 'desligar', ...queryParams } }),
      },
      {
        path: 'profissionais/perfil',
        loadComponent: () => import('./features/profissional-perfil/profissional-perfil').then((m) => m.ProfissionalPerfil),
        data: { breadcrumb: 'Perfil do profissional', area: 'Recursos Humanos' },
      },
      // Equipes de saúde (ADR-0104), carregadas sob demanda.
      {
        path: 'rh/equipes',
        loadComponent: () => import('./features/rh/equipes/equipes').then((m) => m.Equipes),
        data: { breadcrumb: 'Equipes', area: 'Recursos Humanos' },
      },
      // Escalas (ADR-0106), carregadas sob demanda.
      {
        path: 'rh/escalas',
        loadComponent: () => import('./features/rh/escalas/escalas').then((m) => m.Escalas),
        data: { breadcrumb: 'Escalas', area: 'Recursos Humanos' },
      },
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
      { path: 'rh/folha-pagamento', loadComponent: () => import('./features/rh/folha-pagamento/folha-pagamento').then((m) => m.FolhaPagamento), data: { breadcrumb: 'Folha de pagamento', area: 'Recursos Humanos' } },
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
      { path: 'administrativo/setores', loadComponent: () => import('./features/administrativo/setores/setores').then((m) => m.Setores), data: { breadcrumb: 'Setores', area: 'Administrativo' } },
      // Equipamentos de Saúde (ADR-0102), carregados sob demanda.
      {
        path: 'administrativo/equipamentos',
        loadComponent: () => import('./features/administrativo/equipamentos/equipamentos').then((m) => m.Equipamentos),
        data: { breadcrumb: 'Equipamentos de Saúde', area: 'Administrativo' },
      },
      {
        path: 'administrativo/modelo',
        loadComponent: () => import('./features/administrativo/modelo-administrativo/modelo-administrativo').then((m) => m.ModeloAdministrativo),
        data: { breadcrumb: 'Modelo administrativo', area: 'Administrativo' },
      },
      {
        path: 'administrativo/modelo-operacional',
        loadComponent: () => import('./features/administrativo/modelo-operacional/modelo-operacional').then((m) => m.ModeloOperacional),
        data: { breadcrumb: 'Modelo operacional', area: 'Administrativo' },
      },
      // Instituições da rede intersetorial (ADR-0154): o catálogo é aberto a quem está logado.
      {
        path: 'administrativo/instituicoes-externas',
        loadComponent: () => import('./features/administrativo/instituicoes-externas/instituicoes-externas').then((m) => m.InstituicoesExternas),
        data: { breadcrumb: 'Instituições da rede intersetorial', area: 'Administrativo' },
      },
      // Território (ADR-0125), carregado sob demanda.
      {
        path: 'administrativo/territorio',
        loadComponent: () => import('./features/administrativo/territorio/territorio').then((m) => m.Territorio),
        data: { breadcrumb: 'Território', area: 'Administrativo' },
      },
      // Coordenações e gerência (ADR-0175).
      {
        path: 'administrativo/coordenacoes',
        loadComponent: () => import('./features/administrativo/coordenacoes/coordenacoes').then((m) => m.Coordenacoes),
        data: { breadcrumb: 'Coordenações e gerência', area: 'Administrativo' },
      },
      // Estoque e almoxarifado (ADR-0177).
      {
        path: 'administrativo/estoque',
        loadComponent: () => import('./features/administrativo/estoque/estoque').then((m) => m.Estoque),
        data: { breadcrumb: 'Estoque e almoxarifado', area: 'Administrativo' },
      },
      // Frota e transporte (ADR-0178).
      {
        path: 'administrativo/frota',
        loadComponent: () => import('./features/administrativo/frota/frota').then((m) => m.Frota),
        data: { breadcrumb: 'Frota e transporte', area: 'Administrativo' },
      },
      // Rede e pactuação, PPI (ADR-0179).
      {
        path: 'administrativo/ppi',
        loadComponent: () => import('./features/administrativo/ppi/ppi').then((m) => m.Ppi),
        data: { breadcrumb: 'Rede e pactuação (PPI)', area: 'Administrativo' },
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
      { path: 'assistencia/pacientes', loadComponent: () => import('./features/assistencia/pacientes/pacientes').then((m) => m.Pacientes), data: { breadcrumb: 'Pacientes', area: 'Assistência' } },
      { path: 'assistencia/atendimentos', loadComponent: () => import('./features/assistencia/atendimentos/atendimentos').then((m) => m.Atendimentos), data: { breadcrumb: 'Atendimentos', area: 'Assistência' } },
      // Agendamentos, Consultas, Procedimentos e Prontuário viraram abas/seções da tela Atendimentos (ADR-0063).
      { path: 'assistencia/agendamentos', redirectTo: ({ queryParams }) => paraAtendimentos('ag', queryParams) },
      { path: 'assistencia/consultas', redirectTo: ({ queryParams }) => paraAtendimentos('atend', queryParams) },
      { path: 'assistencia/procedimentos', redirectTo: ({ queryParams }) => paraAtendimentos('atend', queryParams) },
      { path: 'assistencia/prontuario', redirectTo: ({ queryParams }) => paraAtendimentos('pront', queryParams) },
      { path: 'assistencia/farmacia', loadComponent: () => import('./features/assistencia/farmacia/farmacia').then((m) => m.Farmacia), data: { breadcrumb: 'Farmácia', area: 'Assistência' } },
      // Agenda do profissional (ADR-0092), carregada sob demanda.
      {
        path: 'assistencia/agenda',
        loadComponent: () => import('./features/assistencia/agenda/agenda').then((m) => m.Agenda),
        data: { breadcrumb: 'Agenda', area: 'Assistência' },
      },
      // Regulação do acesso (ADR-0088), carregada sob demanda.
      {
        path: 'assistencia/regulacao',
        loadComponent: () => import('./features/assistencia/regulacao/regulacao').then((m) => m.Regulacao),
        data: { breadcrumb: 'Regulação', area: 'Assistência' },
      },
      // Regulação de urgência (ADR-0176), carregada sob demanda.
      {
        path: 'assistencia/urgencia',
        loadComponent: () => import('./features/assistencia/urgencia/urgencia').then((m) => m.Urgencia),
        data: { breadcrumb: 'Regulação de urgência', area: 'Assistência' },
      },
      // Laboratório assistencial (ADR-0094), carregado sob demanda.
      {
        path: 'assistencia/laboratorio',
        loadComponent: () => import('./features/assistencia/laboratorio/laboratorio').then((m) => m.Laboratorio),
        data: { breadcrumb: 'Laboratório e imagem', area: 'Assistência' },
      },
      // Leitos e internação (ADR-0099), carregados sob demanda.
      {
        path: 'assistencia/programas',
        loadComponent: () => import('./features/assistencia/programas/programas').then((m) => m.Programas),
        data: { breadcrumb: 'Programas de Saúde', area: 'Assistência' },
      },
      // Sala de Vacina (ADR-0147), carregada sob demanda.
      {
        path: 'assistencia/sala-de-vacina',
        loadComponent: () => import('./features/assistencia/sala-de-vacina/sala-de-vacina').then((m) => m.SalaDeVacina),
        data: { breadcrumb: 'Sala de Vacina', area: 'Assistência' },
      },
      // Saúde Mental (ADR-0120), carregada sob demanda.
      // Sala de Situação (BI1, ADR-0143), carregada sob demanda.
      // Segurança do paciente (ADR-0203).
      {
        path: 'qualidade/seguranca-do-paciente',
        loadComponent: () => import('./features/qualidade/seguranca-do-paciente').then((m) => m.SegurancaDoPaciente),
        data: { breadcrumb: 'Segurança do paciente', area: 'Qualidade' },
      },
      {
        path: 'sala-de-situacao',
        loadComponent: () => import('./features/sala-de-situacao/sala-de-situacao').then((m) => m.SalaDeSituacao),
        data: { breadcrumb: 'Sala de Situação', area: 'Indicadores' },
      },
      // Comunicação (ADR-0168), carregada sob demanda.
      {
        path: 'administrativo/comunicacao',
        loadComponent: () => import('./features/administrativo/comunicacao/comunicacao').then((m) => m.Comunicacao),
        data: { breadcrumb: 'Comunicação', area: 'Administrativo' },
      },
      // Relatórios (BI4, ADR-0167), carregados sob demanda.
      {
        path: 'relatorios',
        loadComponent: () => import('./features/relatorios/relatorios').then((m) => m.Relatorios),
        data: { breadcrumb: 'Relatórios', area: 'Indicadores' },
      },
      // Vigilância epidemiológica (ADR-0124), carregada sob demanda.
      // Emergências e Desastres (ADR-0156).
      {
        path: 'vigilancia/emergencias',
        loadComponent: () => import('./features/vigilancia/emergencias').then((m) => m.Emergencias),
        data: { breadcrumb: 'Emergências e desastres', area: 'Vigilância' },
      },
      // Vigilância sanitária (ADR-0199).
      {
        path: 'vigilancia/sanitaria',
        loadComponent: () => import('./features/vigilancia/vigilancia-sanitaria').then((m) => m.VigilanciaSanitaria),
        data: { breadcrumb: 'Vigilância sanitária', area: 'Vigilância' },
      },
      // Zoonoses e vetores (ADR-0162).
      {
        path: 'vigilancia/vetores',
        loadComponent: () => import('./features/vigilancia/controle-vetorial').then((m) => m.ControleVetorial),
        data: { breadcrumb: 'Zoonoses e vetores', area: 'Vigilância' },
      },
      {
        path: 'vigilancia',
        loadComponent: () => import('./features/vigilancia/vigilancia').then((m) => m.Vigilancia),
        data: { breadcrumb: 'Vigilância epidemiológica', area: 'Vigilância' },
      },
      {
        path: 'assistencia/saude-mental',
        loadComponent: () => import('./features/assistencia/saude-mental/saude-mental').then((m) => m.SaudeMental),
        data: { breadcrumb: 'Saúde Mental', area: 'Assistência' },
      },
      // A RAPS e o acolhimento noturno do CAPS (ADR-0153).
      {
        path: 'assistencia/saude-mental/raps',
        loadComponent: () => import('./features/assistencia/saude-mental/rede-raps').then((m) => m.RedeRaps),
        data: { breadcrumb: 'Rede de Atenção Psicossocial', area: 'Assistência' },
      },
      {
        path: 'assistencia/saude-mental/acolhimento-noturno',
        loadComponent: () => import('./features/assistencia/saude-mental/acolhimento-noturno').then((m) => m.AcolhimentoNoturno),
        data: { breadcrumb: 'Acolhimento noturno', area: 'Assistência' },
      },
      {
        path: 'assistencia/leitos',
        loadComponent: () => import('./features/assistencia/leitos/leitos').then((m) => m.Leitos),
        data: { breadcrumb: 'Leitos', area: 'Assistência' },
      },
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
