# 0155 — Saúde Mental: intersetorialidade e indicadores (SM6)

## Status

Aceita e implementada. Sexta e última fatia do domínio 26, desenhado na [ADR-0112](./0112-saude-mental.md) (detalhe em
[`saude-mental/MODELO-SAUDE-MENTAL.md`](../saude-mental/MODELO-SAUDE-MENTAL.md)). Usa a Intersetorialidade
([ADR-0154](./0154-intersetorialidade.md)) e os indicadores programáticos da P6 ([ADR-0141](./0141-programas-metas-e-indicadores.md)).
Com ela, o domínio Saúde Mental fica completo.

## Contexto

- **O cuidado em saúde mental passa por outros setores:** CRAS e CREAS (benefício, violação de direitos), escola,
  Conselho Tutelar, trabalho e habitação. A ADR-0112 decidiu que entram como encaminhamento a instituição externa de
  um catálogo simples, e o catálogo é do item 31.
- **Faltavam indicadores do domínio:** a ADR-0112 manda que os indicadores venham do programa Saúde Mental, só com
  agregados. A P6 já tem o conjunto fixo de indicadores programáticos, com metas, série de 12 meses, CSV e o bloco de
  programas da Sala de Situação.

## Decisão

1. **Encaminhamento intersetorial no caso:**
   - pelo encaminhamento da IS1, com o caso como origem (`dominio_origem` = `SAUDE_MENTAL`, `origem_id` = o caso);
   - **escrever** (encaminhar, registrar o recebimento e o retorno, cancelar) é de quem escreve no caso: a permissão
     `SAUDE_MENTAL.ACOMPANHAR` na unidade, o caso vigente e o conteúdo liberado (equipe de referência, autoria, acesso
     justificado);
   - **ler:** os encaminhamentos vêm no detalhe do caso, só com o conteúdo liberado;
   - **pendência:** o encaminhamento em aberto depois do retorno esperado marca o caso (`retornoIntersetorialVencido`),
     também na lista; a lista ganha o filtro `RETORNO_INTERSETORIAL` e a contagem, como o PTS a revisar.
2. **Cinco indicadores no conjunto fixo da P6,** só para o programa Saúde Mental:

   | Indicador | Numerador / denominador |
   |---|---|
   | **PTS em dia** | casos abertos no fim do período com o PTS que valia naquele dia dentro da data de revisão / casos abertos |
   | **Avaliação em até 30 dias** | casos abertos no período com a primeira avaliação em até 30 dias do início / casos abertos no período |
   | **Pós-crise em até 7 dias** | crises identificadas no período com o acompanhamento pós-crise em até 7 dias / crises do período |
   | **Comunicação ao MP no prazo** | internações involuntárias admitidas no período comunicadas ao Ministério Público em até 72 horas (Lei 10.216/2001, art. 8º) / involuntárias do período |
   | **Retorno intersetorial** | encaminhamentos intersetoriais enviados no período com o retorno da instituição / enviados (sem os cancelados) |

   - **Prazo ainda aberto não conta:** o caso de menos de 30 dias sem avaliação, a crise de menos de 7 dias sem
     pós-crise e a internação de menos de 72 horas sem comunicação ficam fora do denominador até o prazo vencer, ou
     até cumprirem.
   - **Mudança do desenho aprovado:** o desenho previa "pós-crise registrado sobre as crises encerradas". Mas a SM3 só
     deixa encerrar a crise depois do pós-crise, então esse indicador daria sempre 100%. Passou a medir o prazo: em até 7
     dias da identificação.
   - **Unidade:** a do caso, da crise, da internação e do encaminhamento.
   - **Por reaproveitar a P6:** metas por unidade, série mensal, CSV e a Sala de Situação valem sem código novo. O
     programa é sensível: os números são de quem tem `PROGRAMA.ACOMPANHAR`.
3. **Sem permissão nova** na SM6.

### API

| Método | Caminho | Permissão |
|---|---|---|
| POST | `saude-mental/acompanhamento/{id}/encaminhamento-intersetorial` | `SAUDE_MENTAL.ACOMPANHAR` (escopo), caso liberado |
| POST | `…/encaminhamento-intersetorial/{eid}/recebimento`, `…/retorno`, `…/cancelamento` | idem |
| GET | `saude-mental/acompanhamentos?pendencia=RETORNO_INTERSETORIAL` | como a lista de casos |
| GET | `programa/{id}/indicadores` (e a série, o CSV e as metas) | como na P6; programa sensível |

O caso ganha `encaminhamentosIntersetoriais` e `retornoIntersetorialVencido`; a lista, a contagem
`retornoIntersetorialVencido`.

### Tela

- **Lista de casos:** o filtro "Sem retorno intersetorial", com a contagem.
- **Caso, aba Rede de apoio:** a seção "Encaminhamentos intersetoriais":
  - cada encaminhamento mostra a instituição, a situação, o tipo e o setor, o envio, o prazo e o motivo, e o retorno ou
    o cancelamento;
  - **"Sem retorno no prazo":** o encaminhamento em aberto depois do retorno esperado ganha essa marca;
  - **ações:** "Encaminhar a outro setor" (instituições ativas do catálogo, motivo, envio e prazo, que a tela sugere em
    30 dias); "Recebido", "Retorno" (desfecho e texto) e "Cancelar".
- **Indicadores:** no detalhe do programa Saúde Mental e na Sala de Situação, pelo que já existe.

### Migração

`V42__intersetorialidade_e_saude_mental_indicadores.sql` (a mesma da IS1): o CHECK de
`tb_meta_programatica.indicador` com os cinco indicadores novos.

## Consequências

- **O domínio Saúde Mental fica completo** (SM1 a SM6). O SAMU na crise segue pendente da regulação de urgência.
- **Os indicadores medem o que o sistema registra:** uma internação sem a modalidade marcada não entra na comunicação
  ao MP.
- **Ficam para depois:**
  - a guia de encaminhamento para imprimir (Documentos);
  - escalas clínicas e indicadores de desfecho clínico (fora do escopo da ADR-0112).

## Testes

- **JUnit:**
  - `IntersetorialidadeControllerTest`:
    - o ciclo do encaminhamento no caso;
    - o sem retorno no prazo como pendência do caso e da lista, e o cancelamento tirando a pendência;
    - os cinco indicadores com casos de dentro e fora do prazo; não aparecem em outro programa; a meta vale para eles;
  - `IntersetorialidadeAutorizacaoControllerTest`, com segurança ligada:
    - encaminhar: o psicólogo do caso sim; o outro psicólogo da unidade, sem vínculo, e a recepção não (403);
    - os indicadores do programa sensível: só para quem acompanha (o gestor recebe 403).
- **Robot de API:** `test/intersetorialidade/Intersetorialidade.robot`.
- **Robot de tela:** `test/ui/assistencia/saude_mental/UI_saude_mental.robot`, CT-018: encaminhar o caso e registrar o
  retorno.
