# Padrão de telas internas (dashboard) — referência antes de construir qualquer tela nova

**Status:** Vivo. Consultar **antes** de implementar qualquer tela nova sob o `AppShell`
(`frontend/src/app/shared/app-shell/`), não confiar em memória de uma leitura anterior de
`modelo_front/` — foi exatamente essa falha (implementar de detalhes "lembrados" em vez de
reconferir o arquivo) que gerou as correções na [ADR-0018](../adr/0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md)
que motivaram este documento.

## 1. Dois design systems no mesmo repositório — não misturar

Este projeto tem **dois** layouts distintos, ambos vindos de `modelo_front/`, para públicos
diferentes:

| | Portal público | App interno (dashboard) |
|---|---|---|
| Mockup de referência | `Landing Page.html`, `Login.html` | `Usuarios.html`, `Profissionais.html`, `Equipamentos.html`, etc. (11 dos 13 arquivos) |
| Layout raiz | `.container` (max-width 1200px, centralizado) + `section.block` (padding vertical grande) | `.app` (grid `.sidebar`+`.topbar`+`.content`) |
| Fundo de página | `body { background: var(--white); }` | `.app { background: var(--ink-50); }` |
| Cabeçalho | `<header class="site-header">` (marca `.brand__mark`/`.brand__name`/`.brand__sub`, nav horizontal) | `.sidebar__brand` (marca `.sb-mark`/`.sb-name`/`.sb-sub`, menu vertical) + `.topbar` (breadcrumb) |
| Tag de contexto ("eyebrow", pílula com bolinha verde) | Usada em seções de hero/institucional | **Não existe** — o breadcrumb já cumpre esse papel |
| `<h1>` de página | `var(--fs-h1)` = `clamp(1.75rem, 1.2rem + 2vw, 2.5rem)` | `.page-head h1` = `clamp(1.375rem, 1.1rem + 0.7vw, 1.75rem)` — **menor**, token próprio |
| Onde vive no Angular | `features/landing/` | Qualquer `features/**` cuja rota é filha de `AppShell` em `app.routes.ts` |

Regra prática: qualquer tela nova que faça parte do `AppShell` (rota filha, aparece no menu) usa
**só** a coluna da direita. Nunca reaproveitar `.container`/`.block`/`.eyebrow`/`var(--fs-h1)` — são
do portal público, visualmente parecidos mas não os mesmos, e reaproveitá-los foi a causa de 3
correções seguidas nesta sessão.

## 2. Estrutura de uma tela de listagem (padrão `Usuarios.html`)

```html
<header class="page-head">
  <div>
    <h1>Título da tela</h1>
    <p>Subtítulo/contador — color: var(--ink-500), font-size: 0.875rem</p>
  </div>
  <button class="btn">Ação primária</button>
</header>

<div class="toolbar"> <!-- opcional: busca/filtro -->
  <label class="search"><input type="search" /></label>
</div>

<div class="card">
  <table>...</table>
  <!-- ou .card__head + conteúdo, pra cards com título próprio -->
</div>
```

Nenhum `<span class="eyebrow">` antes do `<h1>` — o `AppShell` já mostra o breadcrumb
(`Início → módulo → página`) no topo da página, contexto duplicado não é necessário.

## 3. Botões — desvio deliberado do mockup, não corrigir

`modelo_front/Usuarios.html` define um `.btn` outline-by-default com `.btn--primary` pra ação
principal. Este projeto **não** portou esse sistema — as telas de Profissional (anteriores ao
módulo de RH) já usavam `.btn` preenchido azul + `.btn--ghost` pra secundário (`styles.css`,
seção "Forms"), e as telas de RH continuaram esse padrão pra não ter dois sistemas de botão
convivendo. **Isso é intencional**, documentado na ADR-0018 — não "corrigir" pra bater com o
mockup pixel a pixel neste ponto específico.

## 4. Tabela de classes/tokens — nome errado mais fácil de usar por engano

| Elemento | Classe/token correto (app interno) | Não usar (é do portal público) |
|---|---|---|
| Marca na sidebar | `.sb-mark` / `.sb-name` / `.sb-sub` | `.brand__mark` / `.brand__name` / `.brand__sub` |
| Título de página | `.page-head h1` (clamp próprio, ver seção 1) | `var(--fs-h1)` |
| Fundo da página | `.app { background: var(--ink-50) }` (herda) | `body`'s `var(--white)` |
| Tag de contexto | Breadcrumb do `AppShell` | `.eyebrow` |
| Layout raiz | `.content` (dentro do `AppShell`) | `.container` + `section.block` |

## 5. Checklist antes de abrir uma PR de tela nova

1. A tela é filha de `AppShell` em `app.routes.ts`, com `data.breadcrumb` definido?
2. O componente **não** duplica `<header class="site-header">`/skip-link (isso já existe uma vez
   só, no `AppShell`)?
3. O `<h1>` usa o clamp de `.page-head`, não `var(--fs-h1)`?
4. Não tem `<span class="eyebrow">` sobrando?
5. Alguma classe nova precisa ser portada de `modelo_front/`? Reabrir o arquivo `.html` de origem
   e conferir o **valor exato** (font-size, padding, cor) antes de escrever a regra — não
   implementar de memória de uma leitura anterior.

## 6. Tabelas largas — nunca `overflow: hidden`, sempre `overflow-x: auto`

Achado num levantamento de usabilidade: 12 telas (mais 14 pontos dentro do perfil do profissional)
envolviam `<table>` num `<div class="card" style="overflow: hidden;">` — pensado só pra cortar os
cantos arredondados. Efeito colateral real: quando a tabela é mais larga que o `.card` (telas com
muitas colunas, ou viewport estreito), o conteúdo excedente fica **invisível e inacessível**, sem
nenhuma barra de rolagem — `overflow: hidden` corta, não rola.

Some a isso que `.content` (área à direita do `.app`, grid `1fr`) não tinha `min-width: 0` — por
comportamento padrão do CSS Grid, uma track `1fr` não encolhe abaixo do tamanho do seu conteúdo
("min-content") a menos que isso seja setado explicitamente. Sem isso, uma tabela larga empurra a
página inteira (incluindo a sidebar) pra além da viewport, criando uma rolagem horizontal
inconsistente que não coincide com o que realmente ficou de fora.

**Correção (`.content { min-width: 0; }` em `styles.css` + `overflow-x: auto` no lugar de
`overflow: hidden` em todo `.card`/wrapper que contém `<table>`)**: a `.content` para de forçar a
página a crescer, e cada tabela larga passa a rolar horizontalmente dentro do seu próprio card —
sidebar e cabeçalho continuam fixos, só a tabela rola.

**Regra pra toda tela nova**: qualquer `<table>` precisa estar dentro de um elemento com
`overflow-x: auto` (nunca `overflow: hidden`) — geralmente o próprio `.card` que a envolve, ou o
`<div style="padding: var(--s-5);">` de uma aba do perfil quando não há `.card` por perto.

## 7. Referências

- [ADR-0018](../adr/0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) — decisão original do
  `AppShell` e histórico das correções que motivaram este documento.
- `modelo_front/Usuarios.html` — mockup de referência mais completo pro padrão de listagem.
