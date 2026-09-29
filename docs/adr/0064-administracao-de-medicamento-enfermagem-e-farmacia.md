# 0064 — Administração de medicamento: checagem de enfermagem ligada à Farmácia

## Status

Aceita e implementada.

## Contexto

`AdministracaoDeMedicamento` estava adiada desde as ADRs 0047 e 0048 porque dependia de a Farmácia
existir. Com medicamento, lote por unidade e livro de movimentação prontos (ADRs 0049–0061) e os
registros clínicos imutáveis (ADR-0062), faltava o elo entre a prescrição do atendimento e o estoque:
a enfermagem dá o medicamento ao paciente, mas nada registrava isso nem baixava o estoque.

Na prática, é o que acontece na sala de medicação (UPA) ou na sala de procedimentos de enfermagem
(UBS): a equipe confere a prescrição, administra e **checa** cada item — administrado, com dose, via
e lote, ou não administrado, com o motivo. O usuário deu essa referência e pediu que a checagem fosse
uma aba da tela de Atendimentos, sem amarrar a regra a um setor específico.

## Decisão

1. **`AdministracaoMedicamento`** (Enfermagem, #8) registra a checagem de um item prescrito, dentro de
   um atendimento.
2. **Só com prescrição.** A checagem nasce de uma **consulta vigente do mesmo atendimento** — o
   receituário dela é a prescrição (400 se for de outro atendimento, 422 se a consulta foi retificada).
   Protocolos que dispensam prescrição (vacinação, por exemplo) ficam para os domínios próprios.
3. **Duas situações:**
   - **Administrado:** medicamento, **lote**, dose descrita ("1 g", "10 gotas"), **via** (oral,
     sublingual, IM, IV, SC, tópica, inalatória, retal, outra), quantidade retirada do lote, data e
     hora e profissional. O lote precisa ser do medicamento (400), da **unidade do atendimento** e
     estar dentro da validade (422). A quantidade sai do lote pelo livro da Farmácia, com o tipo novo
     **`ADMINISTRACAO`** ligado à checagem — mesma trava de linha e mesmo 422 de saldo insuficiente de
     toda movimentação (ADR-0057).
   - **Não administrado:** motivo obrigatório (recusa do paciente, paciente ausente, medicamento em
     falta, suspenso pelo médico, outro — este exige observação). Não mexe no estoque.
4. **Imutável, como todo registro clínico** (ADR-0062). Correção é retificação com motivo. Se a
   versão anterior baixou estoque, a quantidade volta ao lote (**`ADMINISTRACAO_ESTORNO`**) antes da
   nova baixa — o livro mostra as duas operações, e o estoque fica certo mesmo quando a retificação
   troca o lote, a quantidade ou passa a "não administrado". Os lotes envolvidos são travados em ordem
   de id, como nas demais movimentações.
5. **Endpoints** em `/api/v1/administracao-medicamento/`: `POST` (checagem), `POST {id}/retificacao`,
   `GET` (lista), `GET atendimento/{atendimentoId}` (404 sem registros) e `GET {id}`. O prontuário
   passa a trazer as checagens de cada atendimento; o resumo do atendimento ganha
   `totalAdministracoes`; a checagem também impede trocar o paciente do atendimento (ADR-0062).
6. **Tela** (adendo da ADR-0063): aba **Medicação** em Atendimentos — a fila da sala de medicação,
   com os atendimentos que têm prescrição (em aberto por padrão, filtro por unidade), o receituário e
   as checagens de cada um; seção Medicação dentro do atendimento; grupo Medicação no prontuário; o
   extrato do lote na Farmácia mostra "Administração ao paciente".

## Trade-offs considerados

**Baixa no lote da unidade (escolhida)** × **estoque próprio da sala de medicação**
- ✅ Usa o estoque que já existe, sem um fluxo novo de requisição interna.
- ❌ Não representa o estoque de uso imediato que a sala guarda à parte. Se isso virar requisito,
  entra como transferência interna farmácia → sala (mesmo livro), e a administração passa a baixar
  do estoque da sala.

**Prescrição = consulta vigente (escolhida)** × **prescrição estruturada item a item**
- ✅ Aproveita o receituário que já é registrado; a enfermagem confere e checa.
- ❌ O sistema não sabe quais itens foram prescritos, então não aponta item "pendente de checagem".
  Uma prescrição estruturada (medicamento, dose, via, horários) é a evolução natural e deixaria a
  checagem apontar para o item.

**Regra por setor (descartada)** — o usuário indicou a sala de medicação como referência, não como
restrição; o registro guarda a unidade e o setor pelo próprio atendimento.

## Consequências

**Positivas:** a Enfermagem passa a registrar a medicação dada ao paciente, com rastreabilidade de
lote (recall) e baixa automática e auditável no estoque; não administração fica registrada com motivo.

**Negativas / pendências:**
- Prescrição estruturada e aprazamento (horários) — ver trade-offs.
- Estoque da sala de medicação — ver trade-offs.
- Restringir quem checa (enfermagem da unidade) depende do RBAC (ADR-0054).

## Referências

- [ADR-0047](./0047-triagem-primeira-entidade-da-enfermagem.md), [ADR-0048](./0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md) — onde a administração ficou adiada.
- [ADR-0057](./0057-livro-de-movimentacao-do-estoque-da-farmacia.md) — livro de movimentação.
- [ADR-0062](./0062-registros-clinicos-imutaveis-com-retificacao.md) — registros clínicos imutáveis.
- [ADR-0063](./0063-tela-atendimentos-do-agendamento-ao-prontuario.md) — tela Atendimentos, que ganha a aba Medicação.
