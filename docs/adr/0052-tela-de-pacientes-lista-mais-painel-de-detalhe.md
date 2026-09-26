# 0052 — Tela de Pacientes: lista + painel de detalhe, com estados "Em breve"

## Status

Aceita e implementada.

## Contexto

O usuário trouxe um mockup estático (`Pacientes.html`, mesma família de `modelo_front/` que já
originou o app shell — ADR-0018 — e o padrão de telas internas —
`docs/frontend/PADRAO-TELAS-INTERNAS.md`) pedindo pra tela de Pacientes atual (lista simples +
modal de criar/editar, mesmo formato de toda tela interna do projeto) ficar parecida com ele: tiras
de KPI, barra de filtros com pills, e um layout dividido lista+painel-de-detalhe (abas
Resumo/Histórico/Programas/Vacinação/Anexos, sinais vitais, programas vinculados, próximos
atendimentos e uma timeline de histórico).

Levantamento feito antes de desenhar:

1. **O layout lista+painel-de-detalhe não existe em nenhuma tela do app hoje.** RH, Administrativo
   e Assistência (incluindo a própria tela de Pacientes) seguem todos
   `page-head + toolbar + card>table + <app-modal>` pra criar/editar. A tela mais próxima de um
   "detalhe rico" é `profissional-perfil` (busca por CPF → `.card.detail-card` com
   `.detail-head`/`.tabs`/`.info-grid`), mas é uma **página separada** (não fica ao lado de uma
   lista) — mesmo assim, suas classes (`.detail-card`, `.detail-head*`, `.tabs`,
   `.info-grid`/`.info-block`/`.info-list`, `.tag`/`.tag--ok`/`.tag--alert`) já existem em
   `styles.css` e são exatamente o que o painel de detalhe desta tela precisa — reaproveitadas
   quase sem alteração.
2. **Tiras de KPI/stat cards não existem em nenhuma tela interna hoje** — só no portal público
   (`.kpis`/`.kpi` da landing page, sistema de design diferente, não reaproveitável aqui).
3. **Boa parte do conteúdo do mockup não tem dado real por trás no backend de hoje**: Programas de
   saúde (HiperDia, Saúde da Família, Pré-Natal), Vacinação e Anexos não têm nenhuma entidade no
   backend; "Novos cadastros · mês" não é calculável (nenhuma entidade da plataforma tem campo de
   data de criação); "Em programas"/"Com alerta clínico" como KPI dependeriam de Programas (não
   existe) e de uma agregação cara (N+1) que não existe; "Próximos atendimentos" precisaria de um
   `findByPacienteUuid` em `AgendamentoRepository` que não existe. **Sinais vitais e histórico
   clínico, porém, já existem** — `Triagem`/`EvolucaoEnfermagem` (ADRs 0047/0048), agregados pelo
   `Prontuário` (ADR-0045, estendido por 0047/0048) — só na branch `developer` (ainda não promovida
   a `main`) e nunca espelhados no frontend (`core/models/prontuario.ts` só tinha os campos da onda
   Assistência original).

Diante disso, duas abordagens foram discutidas com o usuário:

- **(A) Construir a tela com dado fictício/mock agora, plugar serviços reais depois** — prática comum
  em outros contextos, mas contraria o princípio já registrado na ADR-0018 (nunca decorar com algo
  sem funcionalidade real por trás) e seguido pela plataforma inteira até aqui.
- **(B) Meio-termo, escolhido pelo usuário**: construir a tela **inteira** como no mockup — todos os
  KPIs, todas as pills de filtro, as 5 abas do painel de detalhe — mas cada seção sem dado real
  nasce marcada como **"Em breve"** (estado visualmente distinto e desabilitado, nunca um número ou
  conteúdo inventado). Nada fica de fora da tela; o que muda é se a seção mostra dado real ou um
  estado "em breve" honesto. O plano de backend pra tirar cada "em breve" do papel fica pra uma
  conversa separada, fora desta ADR.

## Decisão

- **Introduz o layout lista+painel-de-detalhe como novo padrão de UI da plataforma**, reaproveitando
  ao máximo classes já existentes em `styles.css`: `.card.detail-card`,
  `.detail-head`/`.detail-head__mark`/`.detail-head__info`/`.detail-head__chips`/
  `.detail-head__actions`, `.tabs`, `.info-grid`/`.info-block`/`.info-list`,
  `.tag`/`.tag--ok`/`.tag--alert` — todos vindos de `profissional-perfil`. Novidades genuínas:
  `.stats`/`.stat` (tira de KPI), `.split` (grid de duas colunas, colapsa pra uma coluna abaixo de
  ~1100px), `.pill`/`.pill--select` (filtro, ver abaixo), `.avatar`/`.who-cell` (iniciais do
  paciente).
- **Cada filtro é um pill por dimensão (Programa/Unidade/Faixa etária/Status), não um botão por
  valor possível.** A primeira versão usava um botão-toggle por valor (ex.: "Status: Ativo" e
  "Status: Inativo" como dois pills sempre visíveis); comparação direta com o mockup mostrou que o
  padrão real é um `<select>` nativo estilizado como pill (`.pill--select`: ícone + select sem
  moldura própria, herda a borda do `.pill`), mostrando o valor escolhido dentro do próprio pill —
  mesma ordem do mockup (Programa, Unidade, Faixa etária, Status) e um botão "Mais filtros" (em
  breve) alinhado à direita. A busca ganhou o ícone de lupa (`.search--icon`), que nenhuma tela do
  projeto tinha até aqui.
- **Cabeçalho ganhou ícones nos três botões** (Exportar/Importar CSV/Novo paciente), mesmos SVGs do
  mockup. O subtítulo do `page-head` usa `{{ totalPacientes() }}` — dado real e dinâmico, não mais
  o texto fixo genérico que a tela tinha antes.
- **Introduz um padrão visual novo e reaproveitável pra "seção sem backend ainda"**:
  - `.stat--pending` — mesmo card `.stat`, número substituído por "Em breve" em `var(--ink-400)`,
    ícone com opacidade reduzida. Sem inventar valor/delta.
  - Pills e botões "em breve" usam o atributo real `disabled` (não só estilo) + `title="Em breve"`.
  - `.empty-state` — bloco com ícone + "Em breve" + uma linha curta do que vai aparecer ali quando o
    backend existir, usado nas abas Programas/Vacinação/Anexos e na seção "Próximos atendimentos".
- **KPIs: os mesmos 4 do mockup** — "Pacientes ativos" (real, client-side sobre a lista já
  carregada), "Novos cadastros · mês" (em breve), "Em programas de saúde" (em breve), "Com alerta
  clínico" (em breve).
- **Filtros: os mesmos do mockup, na mesma ordem** — busca (real, já existia), Programa (em breve),
  Unidade (em breve — `Paciente` não tem FK de unidade), Faixa etária (real, calculada client-side a
  partir de `dataNascimento`: 0–11, 12–17, 18–59, 60+), Status (real), "Mais filtros" (em breve).
- **Ações do cabeçalho: "Novo paciente" (real) + Exportar + Importar CSV (em breve)**.
- **Painel de detalhe com as 5 abas do mockup**: A releitura do mockup mostrou que os botões de aba
  ali são só visuais — o arquivo estático não troca de conteúdo ao clicar, sempre mostra o mesmo
  painel, que já mistura sinais vitais + Programas vinculados + Próximos atendimentos + um preview
  curto de histórico recente, tudo sob "Resumo". A tela replica essa mistura, mas com uma aba
  Histórico própria pra a versão **completa** (não truncada) do mesmo dado:
  - **Resumo**: identidade/endereço/contato (reais) + sinais vitais da triagem mais recente via
    Prontuário (real) + Programas vinculados (em breve) + Próximos atendimentos (em breve) +
    Histórico recente (real, preview truncado aos 4 itens mais recentes) + botão "Ver histórico
    completo" que **troca pra aba Histórico** (não navega pra outra página).
  - **Histórico**: a mesma timeline (achatada e ordenada por data, a partir da agregação do
    Prontuário), sem truncar, com um link "Ver prontuário completo" no fim pra árvore inteira
    Atendimento→Consulta→Procedimento na tela `/assistencia/prontuario`.
  - Programas, Vacinação, Anexos: cada uma sua própria aba "em breve".
  `core/models/prontuario.ts` ganha `TriagemResponseDto`/`EvolucaoEnfermagemResponseDto` e os campos `triagens`/`evolucoes` em
  `ProntuarioAtendimentoDto`, espelhando o que o backend já expõe desde as ADRs 0047/0048.
- **Editar continua sendo o `<app-modal>` já existente**, aberto agora por um botão só-ícone
  (`.icon-btn`, novo — nenhuma tela do projeto tinha até aqui) no cabeçalho do painel, igual ao
  mockup — decisão deliberada de divergir do botão de texto que `profissional-perfil` usa pro
  mesmo tipo de ação; outras telas migram pra esse estilo depois, uma de cada vez, não em bloco.
  Ao lado, um botão "Mais opções" (⋮) "em breve" (o próprio mockup não define o que tem ali, é
  estático).
- **CPF, CNS e "Prontuário" ficam sempre visíveis no cabeçalho do painel** (`.detail-head__ids`),
  não só dentro da aba Resumo — mesmo comportamento do mockup, onde esses identificadores não
  somem ao trocar de aba. **"Prontuário" fica marcado "Em breve"**: o mockup mostra um número fixo
  (`#08471`) que não existe no nosso modelo (o Prontuário é agregado por uuid do paciente, sem
  numeração própria — ADR-0045). Decisão de manter o rótulo (não remover) pensando num possível
  "prontuário familiar" ou numeração própria no futuro — a decidir quando/se surgir requisito real.
- **Sem toggle "Lista/Cartões"** do mockup — só uma visualização.

## Trade-offs considerados

**(A) Dado fictício agora, serviço real depois — rejeitada** (ver Contexto acima)
- ✅ Visual 100% completo imediatamente, sem esperar nenhum backend.
- ❌ Risco de um usuário real interpretar "HiperDia"/contagens fictícias como funcionalidade
  existente; contraria o princípio já estabelecido (ADR-0018) e usado o projeto inteiro de nunca
  decorar sem feature real por trás.

**Pill como botão-toggle por valor (uma primeira tentativa) — rejeitada depois de comparar com o
mockup**
- ✅ Simples de implementar com os signals já existentes (`filtroStatus`/`filtroFaixaEtaria`).
- ❌ O mockup usa um pill por dimensão de filtro (mostrando o valor escolhido dentro do pill), não
  um botão por valor — visualmente muito diferente do resultado pretendido. Resolvido com
  `<select>` nativo (já é a convenção de filtro do resto do projeto, `.toolbar select`) estilizado
  como pill — o melhor dos dois mundos: reaproveita o elemento HTML acessível já usado no projeto,
  mas com a aparência de pill do mockup.

**Reimplementar a árvore completa Atendimento→Consulta→Procedimento dentro da aba Histórico —
rejeitada**
- ✅ Mostraria tudo no mesmo lugar, sem navegação.
- ❌ A tela de Prontuário completo (`/assistencia/prontuario`) já existe pra isso. A aba Histórico
  mostra uma timeline achatada (preview), com link "ver prontuário completo" pra quem quiser a
  árvore inteira — evita duplicar a mesma lógica de agregação em dois lugares com estilos
  diferentes.

## Consequências

**Positivas**: primeiro uso de um padrão lista+detalhe na plataforma, reaproveitando quase
inteiramente classes já existentes; visual completo e fiel ao mockup desde já, sem nenhuma seção
escondida; o padrão `.stat--pending`/`disabled`+`title`/`.empty-state` fica disponível pra qualquer
tela futura que precise do mesmo meio-termo "constrói tudo, marca o que falta"; sinais vitais e
timeline (dado que já existia no backend desde ADRs 0047/0048, mas nunca tinha sido puxado pro
frontend) finalmente aparecem em algum lugar do produto.

**Negativas / pendências**: cinco lacunas de backend ficam registradas, mas **sem plano de
implementação ainda** (a ser conversado separadamente): `AgendamentoRepository.findByPacienteUuid`
(pequeno), domínio Programas de Saúde (novo, grande), domínio Vacinação (novo, grande), domínio
Documentos/Anexos (novo, depende do domínio transversal "Documentos" do `MAPA-DE-DOMINIOS.md`), e
um campo de data de criação em `Paciente` pra viabilizar o KPI de novos cadastros. Até essas
lacunas fecharem, a tela convive com múltiplos estados "Em breve" visíveis ao usuário.

## Referências

- `Pacientes.html` (mockup trazido pelo usuário, `modelo_front/`).
- [`docs/frontend/PADRAO-TELAS-INTERNAS.md`](../frontend/PADRAO-TELAS-INTERNAS.md) — convenção de
  tela interna que este redesenho estende (não substitui).
- [ADR-0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) — app shell e o princípio de
  "não decorar sem feature real por trás", que motivou o padrão "Em breve" em vez de dado fictício.
- [ADR-0038](./0038-app-shell-dinamico-e-telas-de-frontend-do-setor-administrativo.md) — origem de
  `.detail-card`/`.tabs`/`.info-grid` em `profissional-perfil`, reaproveitados aqui.
- [ADR-0045](./0045-prontuario-agregacao-de-leitura.md), [ADR-0047](./0047-triagem-primeira-entidade-da-enfermagem.md),
  [ADR-0048](./0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md) — a agregação e os
  dados (`triagens`/`evolucoes`) que esta tela finalmente expõe no frontend.
