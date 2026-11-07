# 0165 — Modelo Operacional: serviços da unidade (MO2)

## Status

Aceita e implementada. Fatia MO2 da camada 29, desenhada na [ADR-0115](./0115-modelo-operacional-dos-equipamentos.md) e em
[`equipamentos/MODELO-OPERACIONAL.md`](../equipamentos/MODELO-OPERACIONAL.md). Segue a MO1
([ADR-0117](./0117-modelo-operacional-perfis-e-capacidades.md)).

## Contexto

- **A capacidade diz o que a unidade pode fazer;** não diz como isso está organizado ali. Um hospital com a capacidade UTI
  pode ter a "UTI Adulto" e a "UTI Pediátrica", cada uma com seu responsável, seu horário e seus setores.
- **O hospital é composto.** Sem o serviço, os leitos só se agrupam por setor, e a gestão quer vê-los por serviço.

## Decisão

1. **Catálogo de tipos** (`TB_TIPO_SERVICO`, `ORGANIZACAO.GERENCIAR`), editável em Modelo operacional: código, nome,
   descrição, ativo e capacidade exigida opcional.
   - Carga com 11 tipos: pronto-socorro, UTI, internação clínica, centro cirúrgico, centro obstétrico, sala de vacina,
     ambulatório, odontologia, farmácia, laboratório e diagnóstico por imagem.
   - **A sala de vacina nasce sem capacidade exigida,** porque o catálogo da MO1 não tem capacidade de vacinação. Quando
     tiver, o município aponta na tela.
   - A odontologia aponta o diagnóstico bucal, porta de entrada do serviço. O município revisa.
   - **A capacidade exigida não muda enquanto houver serviço vigente do tipo** (422), para não deixar serviço incoerente.
2. **Serviço da unidade** (`TB_SERVICO_UNIDADE`, `ORGANIZACAO.GERENCIAR`): tipo, nome, responsável, 24 horas ou horário.
   - Sem horário, vale o da unidade.
   - **Histórico:** o encerramento tem motivo e data, entre o início e hoje (400); nada se apaga. Encerrado não muda (422).
   - Nome único entre os vigentes da unidade (409). Nível de gestão não tem serviço (422), como na MO1.
3. **A capacidade é pré-requisito, não efeito.** O tipo exige capacidade que a unidade não tem: 422, e o serviço não a
   cria sozinho. Na tela, o tipo aparece bloqueado com o nome da capacidade.
4. **Setores** (`TB_SERVICO_SETOR`): um serviço tem vários; todos da mesma unidade e ativos (422); um setor fica em um
   serviço vigente por vez (409). Encerrar o serviço libera os setores.
5. **Leitos por serviço, calculados.** Total, livres e ocupados vêm dos leitos ativos dos setores. O `Leito` não muda: o
   setor já é a ligação, e um campo novo só duplicaria a informação.
6. **Uma regra a mais na MO1:** a capacidade não encerra com serviço vigente que a exige (422, com os nomes). É a mesma
   lógica do perfil, que não encerra antes das capacidades do grupo.
7. **Leitura:** a mesma de Equipamentos (`ORGANIZACAO.GERENCIAR` ou `ADMINISTRATIVO.CONSULTAR`, na unidade visível).

### API

- `GET, POST /api/v1/tipo-servico/`, `PATCH /api/v1/tipo-servico/{uuid}`;
- `GET /api/v1/unidade-saude/id/{uuid}/servicos`: vigentes, encerrados e setores livres;
- `POST .../servico`, `PATCH .../servico/{id}`, `POST .../servico/{id}/encerramento`.

### Telas

- **Equipamentos de Saúde:** aba **Serviços** na ficha da unidade, ao lado de Capacidades, com a gaveta de inclusão e a
  de encerramento. As abas da ficha passam a quebrar linha, porque sete não cabem no painel.
- **Modelo operacional:** aba **Tipos de serviço**, com o cadastro.

### Migração

`V52__modelo_operacional_servicos.sql`: as três tabelas e a carga dos tipos, com a capacidade apontada pelo código.

## Consequências

- **O hospital composto fica visível:** cada serviço com seus setores e seus leitos, sem `HospitalService` nem tipo novo.
- **Fica para a MO4:** a agenda e os domínios consultarem serviços. Hoje nada depende deles.
- **Fica de fora:** o leito apontar direto para o serviço; a vacinação no catálogo de capacidades.

## Testes

- **JUnit:**
  - `ServicoUnidadeControllerTest`: a carga; o tipo sem a capacidade (422) e o sem exigência; o nível de gestão (422); os
    leitos vindos dos setores; o setor ocupado (409) e o de outra unidade (422); o nome repetido (409); o encerramento,
    o repetido (422), a edição do encerrado (422) e o setor liberado; o fim inválido (400); o ajuste; a capacidade presa
    ao serviço (422) e liberada depois; o catálogo, com a capacidade protegida; o tipo inativo (422);
  - `ServicoUnidadeAutorizacaoControllerTest`, com a segurança ligada: quem gerencia inclui, o gestor só lê, o gestor de
    outra unidade nem lê nem encerra, e o catálogo pede a permissão.
- **Robot:** `ServicosUnidade.robot` (API, sete casos) e os casos 7 a 9 de `UI_modelo_operacional.robot` (aba Serviços e
  aba Tipos de serviço).
