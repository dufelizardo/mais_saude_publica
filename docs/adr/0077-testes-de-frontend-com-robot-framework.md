# 0077 — Testes de frontend com Robot Framework

## Status

Aceita e implementada (base e primeiras telas). Substitui a parte de testes da
[ADR-0008](./0008-frontend-angular.md), que previa Playwright isolado, e o item 9 da
[ADR-0046](./0046-telas-de-frontend-da-onda-assistencia.md), que registrava "sem testes automatizados de
frontend".

## Contexto

- A ADR-0008 escolheu **Playwright** para os testes de ponta a ponta do frontend, mas eles nunca foram
  escritos.
- A ADR-0046 registrou que as telas não tinham teste automatizado e que a verificação era manual.
- Desde então, dezenas de telas foram feitas ou refeitas: RH, Administrativo, Assistência, Farmácia,
  Usuários & Perfis, Auditoria e o prontuário por vínculo. Todas foram verificadas só com `ng build` e
  conferência manual.
- Já os testes de API seguem, há tempos, o **Robot Framework** no padrão **LKDF**
  (POM → FLOW → SCENARIO → TEST, em `test/robot/`), com a regra de que todo endpoint novo vem com Robot.

Ter uma ferramenta para a API e outra para a interface duplicaria linguagem, relatórios e gate de CI.

## Decisão

1. **Os testes de frontend são Robot Framework**, no mesmo padrão LKDF e na mesma suíte dos testes de
   API (`test/robot/`). Não se usa Playwright isolado, Cypress, `ng e2e` nem `.spec.ts` como teste de
   aceitação das telas.
2. **O navegador é controlado pela Browser library** (`robotframework-browser`), que roda o Playwright
   por baixo. Isso preserva a escolha técnica da ADR-0008 dentro do Robot.
3. **As camadas valem igual para a interface:**

   | Camada | Na interface |
   |---|---|
   | **POM** (`src/pom/ui/<tela>/`) | Seletores e ações cruas (`Click`, `Fill Text`, `Select Options By`). Sem asserção. |
   | **FLOW** (`src/flow/ui/<tela>/`) | Monta a jornada da tela e faz as asserções (texto, estado, toast, 403 exibido). |
   | **SCENARIO** (`src/scenario/ui/<tela>/`) | Blueprint declarativo, só repassa para o FLOW. |
   | **TEST** (`test/ui/<tela>/`) | Casos `*.robot` com os dados de entrada. |

4. **Os seletores são estáveis e acessíveis**, na ordem de preferência:
   - o `id` dos campos (`f-<campo>`, padrão das gavetas);
   - papel e nome acessível (`role=tab[name="…"]`, `aria-label`);
   - texto visível.

   Classes CSS não entram como seletor, porque mudam com o visual.
5. **Regra, igual à da API:** todo PR que cria ou refaz uma tela traz o teste Robot da interface no mesmo
   PR, não como tarefa para depois. O mínimo é:
   - a tela abre;
   - o fluxo principal funciona (cadastrar ou editar pela gaveta);
   - a validação aparece;
   - o estado vazio aparece;
   - com autorização ligada, o que muda por permissão.
6. **Os dados da interface são preparados pela API.** O FLOW de interface pode chamar keywords dos FLOWs de
   API para semear dados (ex.: `Seed A Paciente`), em vez de cadastrar tudo clicando.

## Trade-offs considerados

**Robot + Browser library (escolhida)** × **Playwright isolado (ADR-0008)**
- ✅ Uma linguagem, um relatório e um gate de CI para API e interface. A preparação de dados reaproveita
  os FLOWs de API.
- ✅ O Playwright continua sendo o motor, então se mantém a vantagem técnica que levou à ADR-0008.
- ❌ A Browser library precisa de Node e de `rfbrowser init` no ambiente de teste e no CI.

**Sem `.spec.ts` como teste de aceitação** × **testes unitários de componente**
- ✅ O que se verifica é a tela como o usuário usa, contra a aplicação de pé.
- ❌ Lógica pura de componente fica coberta só indiretamente. Um `.spec.ts` pontual continua permitido
  para função utilitária complexa, mas não substitui o Robot da tela.

## Consequências

**Positivas:**
- A interface ganha verificação automatizada no mesmo gate da API.
- A regra "tela nova vem com Robot" passa a valer como já vale para endpoint.

**Base montada:**
- `robotframework-browser` no `requirements.txt`;
- sessão de navegador com login opcional;
- `MSP_FRONT_URL`;
- tag `UI`.

O job `robot-ui` da pipeline roda em todo PR, inclusive para `developer`, sem Docker: sobe a API pelo jar
(Postgres do próprio Actions) e o front pelo `ng serve`. A suíte de API passa a excluir a tag `UI`.

**Primeiras telas cobertas:**
- Setores;
- Modelo administrativo;
- abertura do prontuário em Pacientes, com a regra de vínculo desligada.

**Pendências:**
- **O fluxo do prontuário com o vínculo ligado** (aviso, justificativa e faixa) precisa de uma API com
  login, autorização e vínculo ligados, e de usuários com perfis. Isso pede uma segunda configuração da
  API no job, com o bootstrap do administrador e a troca de senha feita pela API.
- **Cobrir as demais telas já existentes, começando pelas mais recentes:**
  - prontuário por vínculo (ADR-0076);
  - Administrativo agrupado (ADR-0074);
  - catálogos de RH e perfil (ADRs 0073 e 0075);
  - Profissionais (ADR-0072);
  - Usuários & Perfis (ADR-0068);
  - Auditoria (ADR-0071).

## Referências

- [ADR-0008](./0008-frontend-angular.md): Angular e a escolha original de Playwright.
- [ADR-0046](./0046-telas-de-frontend-da-onda-assistencia.md): o registro de "sem testes de frontend".
- [`test/robot/README.md`](../../test/robot/README.md): o padrão LKDF e a regra de testes.
- [LKDF](https://github.com/dufelizardo/Layered-Keyword-Driven-Framework-LKDF).
- [Browser library](https://robotframework-browser.org/).
