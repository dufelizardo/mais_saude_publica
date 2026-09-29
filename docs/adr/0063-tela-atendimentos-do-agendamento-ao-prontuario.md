# 0063 — Tela Atendimentos: do agendamento ao prontuário, com acolhimento

## Status

Aceita e implementada.

## Contexto

A Assistência tinha sete itens no menu: Pacientes, Atendimentos, Agendamentos, Consultas,
Procedimentos, Prontuário e Farmácia. Cada tela de registro (F2 a F6 do `ESCOPO-ASSISTENCIA.md`) era
uma lista com modal, separada das outras, embora o trabalho real seja um fluxo só: o paciente chega
(com ou sem agendamento), é atendido, triado, consultado, recebe procedimentos e evolução de
enfermagem, e tudo vai para o prontuário.

O usuário trouxe o protótipo `Atendimentos.html`, desenhado já sobre a API: uma tela com três abas
(Atendimentos, Agendamentos, Prontuário) e o atendimento aberto numa gaveta larga que mostra o fluxo e
permite registrar cada etapa. Na revisão do protótipo ficou decidido:

- trocar o botão "Novo paciente" (o cadastro tem tela própria, e o formulário do protótipo não batia
  com a API) por **Acolhimento**, a porta de entrada da demanda espontânea;
- não reproduzir a "edição sem histórico" do protótipo: os registros clínicos passaram a ser
  imutáveis, com retificação ([ADR-0062](./0062-registros-clinicos-imutaveis-com-retificacao.md)),
  antes desta tela.

## Decisão

1. **Uma rota, `assistencia/atendimentos`, com três abas** (`page-tabs`, ADR-0058):
   - **Atendimentos** — lista com busca por paciente e filtro por status (aguardando, em andamento,
     concluídos); cada linha mostra idade e CPF, tipo, unidade e setor, **risco da triagem vigente**,
     contadores de triagens/consultas/procedimentos/evoluções e status (tudo vindo do resumo da
     própria listagem, ADR-0062).
   - **Agendamentos** — lista com filtro por paciente; "Iniciar" abre o atendimento a partir do
     agendamento (que passa a Realizado no backend); agendamento continua editável (é registro
     administrativo).
   - **Prontuário** — escolha do paciente na lista ou busca por CPF/Cartão SUS; cartão do paciente e
     linha do tempo dos atendimentos com os registros **vigentes**; retificações aparecem com data e
     motivo.
2. **O atendimento abre numa gaveta larga** (`<app-drawer [larga]="true">`, 760px): status, risco,
   fluxo Agendamento → Atendimento → Triagem → Consulta → Procedimento → Evolução, dados do
   atendimento, ações (editar, concluir, ver prontuário) e uma seção por tipo de registro com "+" para
   incluir e **"Retificar"** em cada registro. Procedimento agendado tem **"Registrar desfecho"**.
   As gavetas de registro abrem por cima e **voltam para o atendimento** ao salvar ou cancelar.
3. **Retificação na tela:** a gaveta de retificação vem preenchida com a versão vigente, explica que a
   atual continua no prontuário e exige o motivo. Não existe "editar" registro clínico.
4. **Acolhimento** (no lugar de "Novo paciente"): busca o paciente por CPF ou Cartão SUS, pede
   unidade, setor, matrícula de quem acolhe e tipo, inicia o atendimento e **abre direto a triagem**.
   Paciente não encontrado → link para o cadastro em Pacientes.
5. **As telas absorvidas saem do menu e do código** (Agendamentos, Consultas, Procedimentos,
   Prontuário). As rotas antigas redirecionam para `assistencia/atendimentos` na aba equivalente,
   mantendo os parâmetros — links já existentes, como o "Ver prontuário completo" de Pacientes,
   continuam funcionando (`?aba=pront&pacienteId=...`).
6. **Como na Farmácia:** sem os elementos de protótipo (selo "Bearer ativo", rota em cada gaveta,
   código HTTP no aviso); busca de paciente por nome/CPF nas gavetas; CSS portado para `styles.css`
   — a linha do tempo do prontuário virou `.pront-tl`, porque `.tl` já é a do histórico de Pacientes.

## Trade-offs considerados

**Uma tela com abas e gaveta larga (escolhida)** × **manter as telas separadas**
- ✅ O fluxo do atendimento fica num lugar só, com o contexto do paciente sempre à vista.
- ❌ Componente grande (maior que o da Farmácia). Se crescer mais, as abas e a gaveta do atendimento
  podem virar componentes filhos sem mudar a rota — mesma ressalva da ADR-0058.

**Acolhimento indo direto para a triagem (escolhida)** × **só abrir o atendimento**
- ✅ É o passo seguinte da demanda espontânea na atenção primária (acolhimento com classificação de
  risco); poupa um clique no momento de maior fila.

**Gavetas que voltam para o atendimento (escolhida)** × **gavetas empilhadas**
- ✅ Uma gaveta por vez, sempre com saída clara ("Voltar"), sem perder o atendimento de vista.

## Consequências

**Positivas:** o menu de Assistência cai de sete para três itens (Pacientes, Atendimentos, Farmácia);
o registro clínico acontece dentro do atendimento, sem edição sem rastro.

**Negativas / pendências:**
- A matrícula do profissional ainda é digitada em cada gaveta; com o login ligado, dá para sugerir a
  do usuário autenticado (mesma pendência da Farmácia).
- O histórico completo de versões de um registro (todas as retificações) ainda não tem visualização;
  o prontuário mostra a vigente com a marca da retificação.
- Pacientes com muitos cadastros: os seletores ainda carregam a lista completa (mesma pendência de
  busca paginada registrada na ADR-0058).

## Referências

- [ADR-0062](./0062-registros-clinicos-imutaveis-com-retificacao.md) — registros clínicos imutáveis, resumo do atendimento e fechamento do agendamento.
- [ADR-0058](./0058-tela-da-farmacia-abas-gaveta-lateral-e-livro-de-estoque.md) — padrão de abas, gaveta lateral e CSS reaproveitado.
- [ADR-0052](./0052-tela-de-pacientes-lista-mais-painel-de-detalhe.md) — tela de Pacientes, que passa a apontar para esta.
- [`docs/frontend/PADRAO-TELAS-INTERNAS.md`](../frontend/PADRAO-TELAS-INTERNAS.md) — gaveta larga.
