# 0154 — Intersetorialidade e Proteção Social: domínio e fatia IS1 (catálogo e encaminhamento)

## Status

Aceita e implementada (fatia IS1). Desenha o item 31 do mapa, reservado na
[ADR-0115](./0115-modelo-operacional-dos-equipamentos.md), seguindo os cinco passos da
[ADR-0116](./0116-marco-mapa-de-dominios-fechado.md). O primeiro uso é a Saúde Mental, na SM6
([ADR-0155](./0155-saude-mental-intersetorialidade-e-indicadores.md)).

## Contexto

- **O item 31 da ADR-0115 é o catálogo único de instituições externas e o encaminhamento intersetorial,** usados pela
  Saúde Mental (CRAS, CREAS, escola), pela Vigilância (violência: Conselho Tutelar, CREAS), pelas Emergências (abrigos)
  e pela Comunicação. Ficou reservado, sem desenho.
- **Os cinco passos da ADR-0116:**
  1. É dado? Em parte: a instituição externa é cadastro.
  2. Cabe num domínio existente? Não. Se o catálogo morasse na Saúde Mental, a Vigilância e as Emergências fariam
     outro, e a mesma instituição ficaria em três lugares.
  3. É configuração de equipamento? Não: a instituição externa **não é unidade de saúde.** Não tem perfil, capacidade,
     lotação nem atendimento, e pô-la em `UnidadeDeSaude` misturaria a rede de saúde com a rede de outros setores.
  4. É interface com outro setor? Sim: é exatamente o item 31.
  5. Por isso é o item 31, já no mapa, e não um domínio novo.
- **O sistema não implementa assistência social, educação ou justiça.** Só registra para onde o paciente foi
  encaminhado, por quê, até quando se espera resposta e o que voltou.

## Decisão

1. **O domínio** cobre três coisas:
   - o catálogo único de instituições externas;
   - o encaminhamento intersetorial, com origem em qualquer domínio;
   - o retorno, como uma contrarreferência informal.

   **Não cobre:**
   - o prontuário SUAS, o CadÚnico ou o sistema da escola (integrações, domínio 20, adiado);
   - a gestão dos serviços dos outros setores;
   - a "proteção social" como benefício: o que o paciente recebeu entra no texto do retorno.
2. **Catálogo** (`TB_INSTITUICAO_EXTERNA`):
   - **campos:** nome (único), setor, tipo, endereço, telefone, e-mail, pessoa de contato, observação e ativa;
   - **setores:** assistência social, educação, garantia de direitos, trabalho e renda, habitação, segurança pública e
     outro;
   - **tipos:** CRAS, CREAS, Centro POP e acolhimento institucional (assistência social); escola (educação); Conselho
     Tutelar, Defensoria Pública, Ministério Público e Judiciário (garantia de direitos); serviço de emprego (trabalho
     e renda); habitação; segurança pública; outro;
   - **o tipo diz o setor:** um tipo de outro setor dá 422. "Outro" vale para qualquer setor;
   - **inativar** tira a instituição do catálogo para encaminhamentos novos, sem apagar os já feitos. Não há exclusão.
3. **Encaminhamento** (`TB_ENCAMINHAMENTO_INTERSETORIAL`):
   - **campos:** paciente, instituição, domínio e registro de origem (`dominio_origem`, `origem_id`), unidade,
     profissional, motivo, data de envio e retorno esperado;
   - **domínio de origem:** só `SAUDE_MENTAL` por enquanto. A Vigilância e as Emergências entram com as próprias fatias,
     acrescentando o valor;
   - **situações:**
     - ENVIADO → RECEBIDO (a instituição confirmou) → CONCLUIDO (atendeu) ou RECUSADO (não atendeu), com a data e o
       texto do retorno;
     - CANCELADO, por quem encaminhou, com o motivo;
     - **"sem retorno" não é guardado:** é o encaminhamento em aberto depois do retorno esperado. Assim não precisa de
       rotina agendada;
   - **regras:**
     - instituição inativa, 422;
     - envio no futuro ou retorno esperado antes do envio, 400;
     - já há encaminhamento em aberto para a mesma instituição na mesma origem, 409;
     - receber só do ENVIADO; retorno e cancelamento só em aberto (422);
     - o retorno pede o desfecho e o texto (400); a data vai do envio até hoje (400).
4. **Acesso:**
   - **ler o catálogo:** qualquer usuário logado, porque é cadastro e não tem paciente;
   - **manter o catálogo:** `INTERSETORIAL.GERENCIAR`, permissão nova, nos papéis Gestor e Assistente social (quem
     conhece a rede socioassistencial do território);
   - **o encaminhamento é dado de saúde:** cada domínio de origem aplica as próprias regras. O núcleo não confere acesso;
     na Saúde Mental valem a equipe de referência e o sigilo do caso.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `instituicoes-externas?setor&ativas` | logado |
| GET | `instituicao-externa/{id}` | logado |
| POST | `instituicao-externa` | `INTERSETORIAL.GERENCIAR` |
| PUT | `instituicao-externa/{id}` | `INTERSETORIAL.GERENCIAR` |

O encaminhamento não tem rota própria: entra pela rota do domínio de origem (na SM6, `saude-mental/acompanhamento/{id}/
encaminhamento-intersetorial`).

### Tela

**Instituições da rede intersetorial** (`/administrativo/instituicoes-externas`, menu Administrativo · Rede
intersetorial):
- **filtros:** busca por nome ou contato, setor, ativas ou com as inativas;
- **tabela:** instituição e tipo, setor, contato;
- **gaveta "Nova instituição" e "Editar":** o tipo acompanha o setor escolhido; na edição, o interruptor "Ativa no
  catálogo".

### Migração

`V42__intersetorialidade_e_saude_mental_indicadores.sql`: as tabelas `tb_instituicao_externa` e
`tb_encaminhamento_intersetorial`, com os CHECK dos enums. A permissão nasce pelo catálogo de acesso.

## Consequências

- **A Vigilância e as Emergências reaproveitam sem migração de modelo:** acrescentam o domínio de origem ao CHECK e a
  rota própria com as próprias regras de acesso.
- **O município cadastra a rede intersetorial uma vez.** Sem catálogo, não há para onde encaminhar; a gaveta de
  encaminhamento avisa.
- **Ficam para depois:**
  - a guia de encaminhamento para imprimir (depende de Documentos);
  - a integração com o CadÚnico e o prontuário SUAS (domínio 20, adiado);
  - o painel de encaminhamentos por instituição, para a gestão da rede intersetorial.

## Testes

- **JUnit:** `IntersetorialidadeControllerTest`:
  - catálogo: nome repetido (409), tipo de outro setor (422), nome vazio (400), filtro por setor, inativar e o filtro das
    ativas;
  - encaminhamento: as regras de data (400), o ciclo enviado → recebido → concluído, o recebimento repetido (422), o
    retorno sem texto ou com desfecho inválido (400), o cancelamento depois do retorno (422), a instituição inativa
    (422) e o encaminhamento de outro caso (404).
- **JUnit com segurança ligada:** `IntersetorialidadeAutorizacaoControllerTest`: a recepção e o psicólogo leem o
  catálogo e não mantêm; o assistente social e o gestor mantêm.
- **Robot de API:** `test/intersetorialidade/Intersetorialidade.robot`.
- **Robot de tela:** `test/ui/administrativo/instituicoes_externas/UI_instituicoes_externas.robot`.
