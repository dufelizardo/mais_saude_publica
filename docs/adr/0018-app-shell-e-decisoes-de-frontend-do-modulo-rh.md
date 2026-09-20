# 0018 — App shell de navegação e decisões de frontend da fatia 1 do módulo de RH

## Status

Aceita e implementada.

## Contexto

Com o backend do módulo de RH completo (Fases 0-9, ver [MODELO-RH.md](../rh/MODELO-RH.md)), o
usuário pediu para estudar o design system já existente em `modelo_front/` (13 mockups estáticos)
e trouxe um documento próprio, a "Especificação Funcional das Telas do Domínio de RH", com a lista
de telas propostas e a ordem recomendada de construção (seção 16 daquele documento, "Diretriz de
evolução"). Esse documento — colado na conversa, não anexado ao repositório — foi a fonte real da
tabela de lacunas de backend (endpoints de edição faltando em Categoria salarial/Cargo/Regra de
anuênio, cálculo de composição remuneratória ainda não implementado, etc.) e da ordem de
implementação por fatias adotada a partir daqui. Essa análise só existia num arquivo de plano local
(fora do repositório) até esta ADR — é um dos dois problemas de documentação que esta ADR corrige.

A primeira fatia entregue (Categorias salariais, Cargos, Tabela salarial, Regras de anuênio) foi
construída **sem** o menu de navegação lateral que aparece em praticamente todo mockup interno do
design system (`Usuarios.html`, `Equipes.html`, `Escalas.html`, `Coordenacoes.html`,
`Equipamentos.html`, `Profissionais.html`, `Painel.html`, `Pacientes.html`, `Programas.html`,
`Relatorios.html`, `Agenda.html` — 11 dos 13 arquivos; só `Landing Page.html` e `Login.html`, o
portal público, não têm). A decisão de pular o menu foi tomada sozinha, justificada por um
precedente errado (2 telas de Profissional já existentes também não tinham menu — mas isso era uma
lacuna delas, não um padrão a seguir). Resultado: as 4 telas da fatia 1 só eram alcançáveis
digitando a URL, sem nenhuma navegação real — o oposto do que "estudar o design system" pedia.

## Decisão

### 1. Componente de modal (`frontend/src/app/shared/modal/`)

Nenhum mockup de `modelo_front/` tem um modal/painel real por trás dos botões "Novo X" (são
decorativos nos mockups). Construído do zero: overlay (`position: fixed; inset: 0`, fundo
`rgba(14,17,22,0.4)`) + painel central (`--white`/`--r-lg`/`--shadow-lg`, já existentes),
`<ng-content>` para o corpo — o formulário dentro reaproveita `.form`/`.form-group`/`.form-row`/
`.form-actions` já existentes, sem CSS de formulário novo. Fecha no Esc
(`@HostListener('document:keydown.escape')`) ou clique fora. `role="dialog"` `aria-modal="true"`.
Usado pelas 3 telas de cadastro/edição da fatia 1 (Categoria salarial, Cargo, Regra de anuênio) —
a de Tabela salarial não usa modal, porque um valor histórico novo é sempre um formulário simples
na própria página, nunca uma edição.

### 2. App shell (`frontend/src/app/shared/app-shell/`)

Construído o shell que faltava, portado de `Usuarios.html`/`Profissionais.html`:
- **Sidebar**: um grupo só, "Recursos Humanos" — é todo o domínio que este app tem hoje. Cinco
  itens, todos com destino real (nenhum item decorativo `href="#"` como o mockup usa livremente):
  Cadastrar profissional, Desligar profissional, Categorias salariais, Cargos, Regras de anuênio.
  Item ativo via `routerLinkActive` + `[attr.aria-current]="'page'"`.
- **Topbar**: botão de menu mobile (`<980px`) + breadcrumb de 3 níveis, igual a todo mockup interno
  (`Início` → nome do módulo → nome da página, ex. `Início / Recursos Humanos / Cargos`): o nível do
  meio (nome do módulo) é fixo em "Recursos Humanos" por enquanto — hoje só existe um grupo de menu,
  então não há necessidade de derivar dinamicamente qual grupo a rota ativa pertence; quando um
  segundo grupo existir, isso precisa virar dinâmico. O nível final (nome da página) vem de
  `route.data.breadcrumb`, configurado por rota em `app.routes.ts`.
- **Ficou de fora, deliberadamente**: rodapé de usuário logado (avatar/nome/sair) — não existe
  login real ainda, a ADR-0008 diz que telas com autenticação esperam a ADR-0006/JWT, e não faz
  sentido inventar um nome de usuário fake só para preencher o visual. Busca global e notificações
  da topbar também ficaram de fora, pelo mesmo motivo: não há dado real por trás. Os outros grupos
  de menu do mockup (Assistencial, Gestão, Administração) também não entraram — nenhum domínio
  deles existe neste app ainda; um item de menu sem tela real seria decoração enganosa.
- As 6 telas internas (as 2 de Profissional + as 4 novas de RH) viram filhas de uma rota que usa
  `AppShell` como componente pai (com seu próprio `<router-outlet>`); a landing page (`/`) fica
  fora do shell, sem mudança — ela é o portal público, corretamente sem menu.
- Cada tela migrada perdeu o `<header class="site-header">`/skip-link que duplicava (esse chrome
  passa a existir uma vez só, no `AppShell`) e trocou o layout `.container`+`.block` (portal
  público, `Landing Page.html`) pelo `.content` (dashboard interno, `padding: var(--s-6); max-width:
  1600px`) — são dois layouts diferentes do mesmo design system, e a fatia 1 usou o errado.

### 3. CSS novo em `styles.css`, tudo portado de `Usuarios.html` reaproveitando tokens já existentes

`--side-w`/`--top-h` (tokens novos); `.app`/`.sidebar`/`.sidebar__brand`/`.sidebar__nav`/
`.nav-group`/`.nav-group__label`/`.nav-item`/`.topbar`/`.crumbs`/`.menu-btn`/`.content` (shell);
`table`/`.card`/`.toolbar`/`.search`/`select` (listagens da fatia 1).

## Trade-offs considerados

**Corrigir agora, antes da fatia 2 (escolhida)**
- ✅ Toda tela futura do módulo de RH nasce já dentro do shell — sem outra correção de arquitetura
  pela frente.
- ❌ Retrabalho nas 6 telas já entregues (remover header duplicado, trocar layout) — aceito porque
  o custo só cresce se mais telas forem construídas em cima do padrão errado.

**Deixar pra corrigir quando o número de telas justificasse (rejeitada)**
- ✅ Menos retrabalho imediato.
- ❌ Rejeitada explicitamente pelo usuário — o pedido original já era pra seguir o design system
  estudado, não para postergar a parte estrutural dele.

**Construir os outros grupos de menu do mockup (Assistencial, Gestão, Administração) já de cara
(rejeitada)**
- ✅ Visual mais parecido com o mockup completo.
- ❌ Rejeitada: seriam itens de menu para domínios sem nenhuma tela real por trás — o mesmo erro
  invertido (inventar o que não existe, em vez de esconder o que falta).

## Correção pós-implementação (2026-09-19)

Ao remover o `:host { background: var(--ink-50); }` de cada tela migrada (a intenção era que o
`.app`/`.content` compartilhado assumisse essa responsabilidade), a regra equivalente nunca foi
adicionada no shell — as 6 telas internas voltaram a cair no `background: var(--white)` do `body`
global, que é o fundo do portal público, repetindo um erro visual que o usuário já tinha pedido
para corrigir antes (individualmente, no `profissional-cadastro`, comparando com
`Painel.html`). Corrigido adicionando `background: var(--ink-50)` na regra `.app` em
`frontend/src/styles.css` — é o mesmo valor usado no `body` de todo mockup interno.

Duas correções adicionais, mesma causa raiz: implementar de memória do que "achei que vi" no
mockup, em vez de reabrir o arquivo e conferir o markup exato de cada peça.
- **Breadcrumb com 2 níveis em vez de 3**: todo mockup interno tem `Início → nome do módulo →
  nome da página` (ex. `Início / Recursos Humanos / Cargos`, conferido em `Usuarios.html`,
  `Profissionais.html`, `Equipamentos.html` etc.) — o shell só tinha `Início → nome da página`.
  Corrigido adicionando o segmento do meio, fixo em "Recursos Humanos" por ora (só existe um grupo
  de menu hoje).
- **Bloco de marca da sidebar reaproveitando as classes erradas**: usei `.brand__mark`/
  `.brand__name`/`.brand__sub` (do cabeçalho público, `--fs` maiores) em vez de `.sb-mark`/
  `.sb-name`/`.sb-sub` (classes próprias e menores que `Usuarios.html` define especificamente pra
  sidebar interna — `font-size: 0.9375rem`/`0.66rem` contra `1.0625rem`/`0.72rem` do público).
  Corrigido portando as 3 classes que faltavam.

## Consequências

**Positivas**
- Navegação real entre as 6 telas internas existentes, consistente com o design system estudado.
- Modal reutilizável disponível para toda fatia futura que precisar de criar/editar em painel.
- A análise da especificação funcional do usuário (lacunas de backend, ordem de fatias) fica
  registrada no repositório pela primeira vez, não só num arquivo de plano local.

**Negativas / pendências**
- Sidebar sem rodapé de usuário — quando a ADR-0006 (JWT) avançar, esse rodapé entra com dado real.
- Não existe uma tela "Profissionais" (listagem) ainda, só cadastrar/desligar como ações separadas
  no menu — quando essa tela existir, os 2 itens colapsam para dentro dela.
- Os grupos de menu para outros domínios (Assistencial, Gestão, Administração) nascem só quando
  esses domínios forem construídos de verdade — não há prazo definido.

## Referências

- [MODELO-RH.md](../rh/MODELO-RH.md), [ESCOPO-RH.md](../rh/ESCOPO-RH.md) — desenho e roadmap do
  módulo de RH.
- [ADR-0008](./0008-frontend-angular.md) — escolha de Angular; ADR-0016 — deploy do frontend em dev.
- `modelo_front/Usuarios.html` e `modelo_front/Profissionais.html` — fonte do padrão de shell
  (`.app`/`.sidebar`/`.topbar`) portado nesta ADR.
