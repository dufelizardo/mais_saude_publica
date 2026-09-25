import { Routes } from '@angular/router';
import { LandingPage } from './features/landing/landing-page';
import { ProfissionaisLista } from './features/profissionais-lista/profissionais-lista';
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
import { Vagas } from './features/rh/vagas/vagas';
import { VagaCandidatos } from './features/rh/vaga-candidatos/vaga-candidatos';
import { TiposBeneficio } from './features/rh/tipos-beneficio/tipos-beneficio';
import { TipoBeneficioValores } from './features/rh/tipo-beneficio-valores/tipo-beneficio-valores';
import { Setores } from './features/administrativo/setores/setores';
import { CapacidadesAdministrativas } from './features/administrativo/capacidades-administrativas/capacidades-administrativas';
import { PerfisAdministrativos } from './features/administrativo/perfis-administrativos/perfis-administrativos';
import { PerfisPorTipoUnidade } from './features/administrativo/perfis-por-tipo-unidade/perfis-por-tipo-unidade';
import { ProcessosAdministrativos } from './features/administrativo/processos-administrativos/processos-administrativos';
import { ResponsabilidadesAdministrativas } from './features/administrativo/responsabilidades-administrativas/responsabilidades-administrativas';
import { NecessidadesDePessoal } from './features/administrativo/necessidades-de-pessoal/necessidades-de-pessoal';
import { Pacientes } from './features/assistencia/pacientes/pacientes';
import { Atendimentos } from './features/assistencia/atendimentos/atendimentos';
import { Agendamentos } from './features/assistencia/agendamentos/agendamentos';
import { AppShell } from './shared/app-shell/app-shell';

export const routes: Routes = [
  { path: '', component: LandingPage },
  {
    path: '',
    component: AppShell,
    children: [
      { path: 'profissionais', component: ProfissionaisLista, data: { breadcrumb: 'Profissionais', area: 'Recursos Humanos' } },
      { path: 'profissionais/novo', component: ProfissionalCadastro, data: { breadcrumb: 'Cadastrar profissional', area: 'Recursos Humanos' } },
      { path: 'profissionais/desligar', component: ProfissionalDesligar, data: { breadcrumb: 'Desligar profissional', area: 'Recursos Humanos' } },
      { path: 'profissionais/perfil', component: ProfissionalPerfil, data: { breadcrumb: 'Perfil do profissional', area: 'Recursos Humanos' } },
      { path: 'rh/categorias-salariais', component: CategoriasSalariais, data: { breadcrumb: 'Categorias salariais', area: 'Recursos Humanos' } },
      { path: 'rh/cargos', component: Cargos, data: { breadcrumb: 'Cargos', area: 'Recursos Humanos' } },
      { path: 'rh/cargos/:cargoId/tabela-salarial', component: CargoTabelaSalarial, data: { breadcrumb: 'Tabela salarial', area: 'Recursos Humanos' } },
      { path: 'rh/regras-anuenio', component: RegrasAnuenio, data: { breadcrumb: 'Regras de anuênio', area: 'Recursos Humanos' } },
      { path: 'rh/folha-pagamento', component: FolhaPagamento, data: { breadcrumb: 'Folha de pagamento', area: 'Recursos Humanos' } },
      { path: 'rh/treinamentos', component: Treinamentos, data: { breadcrumb: 'Catálogo de treinamentos', area: 'Recursos Humanos' } },
      { path: 'rh/ciclos-avaliacao', component: CiclosAvaliacao, data: { breadcrumb: 'Ciclos de avaliação', area: 'Recursos Humanos' } },
      { path: 'rh/vagas', component: Vagas, data: { breadcrumb: 'Vagas', area: 'Recursos Humanos' } },
      { path: 'rh/vagas/:vagaId/candidatos', component: VagaCandidatos, data: { breadcrumb: 'Candidatos', area: 'Recursos Humanos' } },
      { path: 'rh/tipos-beneficio', component: TiposBeneficio, data: { breadcrumb: 'Tipos de benefício', area: 'Recursos Humanos' } },
      { path: 'rh/tipos-beneficio/:tipoId/valores', component: TipoBeneficioValores, data: { breadcrumb: 'Valores do benefício', area: 'Recursos Humanos' } },
      { path: 'administrativo/setores', component: Setores, data: { breadcrumb: 'Setores', area: 'Administrativo' } },
      { path: 'administrativo/capacidades', component: CapacidadesAdministrativas, data: { breadcrumb: 'Capacidades administrativas', area: 'Administrativo' } },
      { path: 'administrativo/perfis', component: PerfisAdministrativos, data: { breadcrumb: 'Perfis administrativos', area: 'Administrativo' } },
      { path: 'administrativo/perfis-por-tipo-unidade', component: PerfisPorTipoUnidade, data: { breadcrumb: 'Perfil por tipo de unidade', area: 'Administrativo' } },
      { path: 'administrativo/processos', component: ProcessosAdministrativos, data: { breadcrumb: 'Processos administrativos', area: 'Administrativo' } },
      { path: 'administrativo/responsabilidades', component: ResponsabilidadesAdministrativas, data: { breadcrumb: 'Responsabilidades administrativas', area: 'Administrativo' } },
      { path: 'administrativo/necessidades-de-pessoal', component: NecessidadesDePessoal, data: { breadcrumb: 'Necessidades de pessoal', area: 'Administrativo' } },
      { path: 'assistencia/pacientes', component: Pacientes, data: { breadcrumb: 'Pacientes', area: 'Assistência' } },
      { path: 'assistencia/atendimentos', component: Atendimentos, data: { breadcrumb: 'Atendimentos', area: 'Assistência' } },
      { path: 'assistencia/agendamentos', component: Agendamentos, data: { breadcrumb: 'Agendamentos', area: 'Assistência' } },
    ],
  },
];
