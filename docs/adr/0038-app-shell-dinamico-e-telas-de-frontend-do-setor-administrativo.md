# 0038 — App shell dinâmico e decisões de frontend do Setor Administrativo

## Status

Aceita e implementada (F0b — [PR pendente]).

## Contexto

O backend do Setor Administrativo Adaptativo (ADRs 0030–0036) está implementado e mergeado em
`developer`, sem nenhuma tela de frontend ainda. O módulo de RH já tem um precedente forte e
documentado ([ADR-0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md),
[`docs/frontend/PADRAO-TELAS-INTERNAS.md`](../frontend/PADRAO-TELAS-INTERNAS.md)): componentes
standalone, sem NgModules, sem biblioteca de componentes própria (só classes CSS globais + um
`<app-modal>` compartilhado), sem autocomplete em lugar nenhum do app. Este ADR aplica esse mesmo
padrão às 7 entidades do Setor Administrativo (`Setor`, `CapacidadeAdministrativa`,
`PerfilAdministrativo`, `PerfilPorTipoUnidade`, `ProcessoAdministrativo`,
`ResponsabilidadeAdministrativa`, `NecessidadeDePessoal`) e documenta as decisões que não têm
precedente direto no código existente.

## Decisão

1. **Novo endpoint `GET /api/v1/profissional/matricula/{matricula}`** (espelha o
   `GET /api/v1/profissional/{cpf}` existente). O Setor Administrativo referencia `Profissional`
   só por matrícula (ADR-0034), e nenhuma tela do RH até hoje precisava resolver um profissional a
   partir da matrícula sem contexto prévio — o perfil sempre é buscado por CPF primeiro. Ver
   PR #193.
2. **Campo `matriculaResponsavel` do `Setor` (opcional) é um input de texto simples**, validado só
   no submit pelo banner de erro padrão do app (o backend responde 404 se a matrícula não existir)
   — não um fluxo de busca-e-confirma. Mantém o modal de criar/editar Setor de passo único,
   consistente com o padrão de catálogo dominante do app (`cargos.ts`, `categorias-salariais.ts`,
   etc.). Na edição, `responsavelNome` (já vem na resposta) aparece como dica somente-leitura ao
   lado do campo.
3. **`ResponsabilidadeAdministrativa` é uma tela própria em `features/administrativo/`, não uma
   aba nova em `profissional-perfil.ts`.** Esse componente já tem 13 abas e ~1300 linhas, todas
   fatos de RH sobre a pessoa (lotação, afastamento, ponto, folha...). "Responsabilidade
   administrativa" é uma decisão de outro domínio, e o próprio backend (ADR-0034) mantém essa
   fronteira explícita (Administrativo lê o RH, nunca o contrário). A tela reaproveita o fluxo
   "buscar identificador → confirmar → agir" de `profissional-desligar.ts`, trocando CPF por
   matrícula (usa o endpoint da decisão 1), seguido de tabela de histórico + "Encerrar" no padrão
   de `AdesaoBeneficio` (dentro de `profissional-perfil.ts`).
4. **Breadcrumb do topbar ganha um segmento de área dinâmico.** O segmento do meio ("Recursos
   Humanos") estava fixo no HTML de `app-shell.html` — a própria ADR-0018 já sinalizava que isso
   precisaria virar dinâmico quando um segundo grupo de menu existisse. Todo `data` de rota em
   `app.routes.ts` (existente e novo) ganha um campo `area` (ex.: `'Recursos Humanos'` ou
   `'Administrativo'`), e `AppShell` computa um signal `area()` do mesmo jeito que já computa
   `breadcrumb()`. O `<span>` do meio do breadcrumb só aparece quando `area()` não é vazio.
5. **`PerfilPorTipoUnidade.tipo`**: o `<select>` do formulário mostra só os 8 valores de
   `TipoUnidadeDeSaude` que são efetivamente "Unidade de Saúde" (`UBS, HOSPITAL, UPA, LABORATORIO,
   CAPS, CENTRO_ESPECIALIDADES, CENTRO_REABILITACAO, POLICLINICA`), não os 4 níveis superiores da
   hierarquia (`FEDERAL, ESTADUAL, MUNICIPAL, REGIONAL`) — perfil administrativo não faz sentido
   pra esses níveis. É só um filtro de UX no frontend; o backend não restringe.
6. **`NecessidadeDePessoal.vincularVaga`** é uma ação dedicada na própria tela de Necessidades
   (dona do FK `vagaAssociada`), não na tela de Vagas do RH — botão "Vincular vaga" só nas linhas
   sem vaga associada, abre um modal pequeno com um `<select>` de `VagaService.listar()` (já
   existe), chama `vincularVaga(uuid, vagaId)` dedicado. Mesmo padrão do "Encerrar" (decisão 3),
   trocando o campo de data por um select.
7. As demais 5 telas (`Setor`, `CapacidadeAdministrativa`, `PerfilAdministrativo`,
   `ProcessoAdministrativo`, `NecessidadeDePessoal`) seguem sem alteração o padrão dominante de
   catálogo do app: componente único por tela, lista + `<app-modal>` de criar/editar compartilhando
   um `FormGroup`, `<select>` simples pra recurso preso a um catálogo pai já carregado.

## Trade-offs considerados

**Endpoint novo de busca por matrícula, em vez de listar tudo e filtrar no frontend (escolhida)**
- ✅ Consistente com o padrão já usado pra CPF (`GET /{cpf}`) — uma busca por chave única merece
  seu próprio endpoint, não uma listagem completa filtrada no cliente.
- ❌ Mais um endpoint pra manter — aceitável, é trivial e reaproveita `findByMatricula` já
  existente no repositório.

**Tela própria pra `ResponsabilidadeAdministrativa`, em vez de aba em `profissional-perfil.ts`
(escolhida)**
- ✅ Preserva a fronteira de domínio que o backend já define explicitamente (ADR-0034); evita
  inflar ainda mais um componente já grande com um assunto de outro módulo.
- ❌ Duplica estruturalmente o fluxo "buscar por identificador" (já existe em
  `profissional-desligar.ts`, por CPF) — aceitável, é replicar um padrão pequeno e já validado, não
  inventar um novo.

**Aba nova em `profissional-perfil.ts` (rejeitada)**
- ✅ Um só lugar pra ver tudo sobre um profissional, sem sair da tela.
- ❌ Mistura código de dois domínios frontend num componente que já não deveria crescer mais
  (13 abas); contraria a fronteira que o backend define deliberadamente.

## Consequências

**Positivas**: as 7 telas seguem exatamente o padrão já validado pelo módulo de RH, sem introduzir
nenhum componente ou biblioteca nova; a fronteira Administrativo↔RH definida no backend (ADR-0034)
se reflete no frontend.

**Negativas / pendências**: nenhuma pendência conhecida além do trabalho normal de implementar cada
tela (ver `docs/administrativo/ESCOPO-ADMINISTRATIVO.md`, seção "Estado do frontend", preenchida
fase a fase).

## Referências

- [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)
- [ADR-0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) — precedente direto do
  `AppShell`/`app-modal` e do aviso sobre o breadcrumb fixo.
- [`docs/frontend/PADRAO-TELAS-INTERNAS.md`](../frontend/PADRAO-TELAS-INTERNAS.md) — regras de CSS
  e estrutura de tela a seguir em toda tela nova.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — fronteira
  Administrativo↔RH que a decisão 3 reflete no frontend.
