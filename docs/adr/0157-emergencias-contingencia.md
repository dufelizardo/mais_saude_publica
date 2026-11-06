# 0157 — Emergências e Desastres: plano de contingência e ativação (E2)

## Status

Aceita e implementada. Segunda fatia do domínio 27, desenhado na [ADR-0113](./0113-emergencias-e-desastres.md)
(detalhe em [`emergencias/MODELO-EMERGENCIAS.md`](../emergencias/MODELO-EMERGENCIAS.md), seções 5 e 6). Usa o núcleo
da E1 ([ADR-0156](./0156-emergencias-nucleo.md)).

## Contexto

- **O plano é a preparação feita antes da emergência** (ADR-0113). É configurável: não há `PlanoEnchente` nem
  `PlanoEpidemia`. É versionado, porque o relatório pós-evento (E5) leva a revisões.
- **O plano não é "ligado":** a ativação é um ato explícito da autoridade, com histórico. Cada elevação ou redução do
  nível da emergência entra nesse histórico.
- **A E1 deixou a tela dos catálogos de tipo e nível para depois:** ela entra aqui, junto com a tela dos planos.

## Decisão

1. **Plano de contingência versionado** (`TB_PLANO_CONTINGENCIA`, uma linha por versão; o código liga as versões):
   - **campos:** código estável, versão, nome, tipo de emergência, objetivo, vigência (início e fim opcional), os
     níveis que cobre (`TB_PLANO_CONTINGENCIA_NIVEL`) e as unidades envolvidas (`TB_PLANO_CONTINGENCIA_UNIDADE`);
   - **por nível:**
     - critérios de ativação (`TB_CRITERIO_ATIVACAO`), em texto. Orientam, não decidem: não há motor de regras;
     - ações previstas (`TB_ACAO_PLANO`): descrição, responsável (função ou setor), prazo em horas e unidade;
   - **recursos necessários** (`TB_RECURSO_PLANO`, texto e quantidade) e **contatos** (`TB_CONTATO_PLANO`, nome, papel e
     telefone);
   - **situação da versão:** rascunho → vigente → substituída (pela versão seguinte) ou revogada;
   - **regras:**
     - código obrigatório (400) e único (409); não muda (422);
     - critério ou ação em nível que o plano não cobre, 422; vigência que termina antes de começar, 400;
     - só o rascunho se edita (422); o conteúdo inteiro vai a cada ajuste;
     - **publicar** pede ao menos uma ação prevista e a vigência que não terminou (422). A vigente anterior passa a
       substituída;
     - **revisar** copia a vigente (422 se não for) para um rascunho com a versão seguinte; um rascunho por plano (409);
     - **revogar** tira de uso a vigente ou descarta o rascunho, com o motivo; já fora de uso, 422.
2. **Ativação do plano na emergência** (`TB_ATIVACAO_PLANO`, com os passos em `TB_REGISTRO_ATIVACAO_PLANO`):
   - **ativar:** o plano precisa estar vigente, em vigência, ser do tipo da emergência e cobrir o nível atual (422). A
     emergência precisa estar ativa (422). Pede a autoridade e a justificativa. Pode haver mais de um plano ativo; o
     mesmo plano (pelo código), uma vez só (409);
   - **passos:** ativado, nível alterado e desativado, com o nível, se o plano cobre esse nível, a autoridade, a
     justificativa e o momento. Só inclusão;
   - **mudança de nível da emergência:** cada plano ativo ganha o passo "nível alterado", que diz se ele cobre o nível
     novo. O plano continua ativo; a tela avisa quando ele não cobre;
   - **desativar:** com a justificativa; já desativado, 422. **Encerrar a emergência** desativa os planos ativos;
   - **a versão fica na ativação:** publicar uma versão nova não muda as ativações já feitas;
   - **ações previstas para o nível atual:** o detalhe da emergência mostra, para cada plano ativo, as ações do nível
     atual, com o prazo contado do passo mais recente (a ativação ou a mudança de nível). Só leitura: acompanhar a
     execução é da E3.
3. **Permissões** (sem permissão nova): `CONTINGENCIA.GERENCIAR` mantém planos e catálogos; `EMERGENCIA.ATIVAR` ativa e
   desativa; a leitura é a da E1.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `emergencias/planos?vigentes&tipoId`, `emergencias/plano/{id}` | leitura |
| POST | `emergencias/plano` | `CONTINGENCIA.GERENCIAR` |
| PUT | `emergencias/plano/{id}` | `CONTINGENCIA.GERENCIAR` |
| POST | `emergencias/plano/{id}/revisao`, `/publicacao`, `/revogacao` | `CONTINGENCIA.GERENCIAR` |
| POST | `emergencia/{id}/plano-ativo`, `emergencia/{id}/plano-ativo/{ativacao}/desativacao` | `EMERGENCIA.ATIVAR` |

A emergência ganha `planos`: as ativações, com os passos e as ações previstas do nível atual.

### Tela

- **Emergências, aba "Planos de contingência":**
  - a lista das versões ("Em uso" ou "Todas as versões");
  - o detalhe com os critérios e as ações por nível, os recursos, os contatos e as versões;
  - o editor do rascunho, com níveis, unidades, critérios, ações, recursos e contatos em linhas;
  - publicar, revisar e revogar (ou descartar o rascunho).
- **Emergências, aba "Catálogos":** tipos (código, categoria, COBRADE, ativo) e níveis (ordem, nome, descrição, ativo).
- **Detalhe da emergência:** a seção "Planos de contingência", com ativar (os planos vigentes do tipo), desativar, o
  aviso de nível não coberto, as ações previstas com o prazo e os passos.
- **Conversão do evento:** a gaveta lista os planos vigentes do tipo, para ativar depois.

### Migração

`V44__emergencias_contingencia.sql`: as tabelas do plano (com níveis, unidades, critérios, ações, recursos e contatos) e
da ativação (com os passos), com os CHECK dos enums.

## Consequências

- **A coordenação ganha o roteiro do que fazer em cada nível,** sem automatizar a decisão.
- **Os prazos das ações já aparecem,** mas sem acompanhamento: quem fez o quê vem com a E3.
- **Ficam para depois:**
  - E3: ações emergenciais acompanhadas (nascendo das ações previstas), mobilização e capacidade emergencial;
  - E5: o relatório pós-evento recomenda a revisão; a versão nova se faz pelo "Revisar" desta fatia;
  - a escolha manual das microáreas na tela (da E1).

## Testes

- **JUnit:**
  - `PlanoContingenciaControllerTest`:
    - versionamento: sem código (400), código repetido (409), critério fora dos níveis (422), vigência invertida (400),
      código que muda (422), publicar sem ação (422), publicar, editar a vigente (422), revisar o rascunho (422), revisar
      (v2 com ações e contatos copiados), rascunho já aberto (409), publicar a v2 (a v1 substituída, as versões), a lista
      das vigentes, revogar e revogar de novo (422);
    - ativação: plano de outro tipo, que não cobre o nível ou em rascunho (422), ativar (ações do nível com prazo,
      autoridade), já ativo (409), mudança de nível (passo automático, nível não coberto, sem ações), desativar e
      desativar de novo (422), encerramento desativando o plano ativo;
  - `EmergenciaAutorizacaoControllerTest.planosPorPapel`, com segurança ligada: o gestor mantém e ativa; a vigilância
    lê e não mantém nem ativa; a recepção não lê.
- **Robot de API:** `test/emergencias/Contingencia.robot`, CT-001 a CT-011.
- **Robot de tela:** `test/ui/vigilancia/emergencias/UI_emergencias.robot`, CT-003 (ativar plano) e CT-004 (plano
  pelo editor e publicação).
