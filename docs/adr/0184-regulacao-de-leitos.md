# 0184 — Regulação e Leitos: central de regulação de leitos entre unidades

## Status

Aceita e implementada. Fatia **RL1** do plano para fechar os domínios 11 (Regulação) e 12 (Leitos e Internação):

| Fatia | O quê |
|---|---|
| **RL1 (esta)** | Central de regulação de leitos entre unidades, reserva de leito com prazo e faixa etária do leito |
| **RL2** | Reserva de leito para cirurgia eletiva e censo hospitalar diário formal |
| **RL3** | Laudo para solicitação de AIH e autorização interna, com o número da AIH |
| **RL4** | Prescrição hospitalar diária (medicamentos, dieta e cuidados) e checagem pela enfermagem |
| **RL5** | Ficha clínica do atendimento pré-hospitalar (APH) na ocorrência de urgência |

Ficam no domínio 20, sem impedir nenhuma fatia:

- SISREG e SIGTAP;
- o envio da AIH ao SIH;
- a sincronização dos leitos com o CNES;
- a telefonia do 192.

## Contexto

- **A internação só acontecia na própria unidade.** A UPA ou o hospital sem vaga não tinha a quem pedir leito de outra unidade.
- **A transferência da urgência** ([ADR-0176](./0176-regulacao-de-urgencia.md)) escolhe o destino e a vaga zero, mas não reserva leito. A
  própria ADR deixou a regulação de leitos para o domínio 12.
- **O leito não tinha faixa etária:** nada impedia internar um recém-nascido em leito adulto.

## Decisão

1. **Pedido de leito** (`TB_SOLICITACAO_LEITO`, `INTERNACAO.SOLICITAR_VAGA` na unidade de origem):
   - **Conteúdo:** paciente, origem, quem pede, CID, quadro clínico, tipo de leito e prioridade nas quatro cores da Regulação.
   - **Origem do pedido:** o atendimento ("Pedir vaga"), uma internação que precisa de outro nível (a internação de origem é informada) ou
     a ocorrência de urgência (o identificador fica no pedido).
   - **Limite:** um pedido em andamento por paciente (409).
2. **Fila da central** (`LEITO.REGULAR`, no escopo das unidades de origem):
   - **Ordem:** prioridade e depois hora do pedido, com a posição de quem aguarda.
   - **Vagas compatíveis:** leitos livres de **outras** unidades, do tipo pedido, com o sexo da enfermaria e a faixa etária do paciente.
3. **Reserva:**
   - **O que acontece:** o regulador reserva um leito compatível. O leito vai para **RESERVADO** (situação nova), com o prazo e o pedido.
   - **Prazo:** padrão de 6 horas, de 1 a 24.
   - **Validações:** leito incompatível ou da própria origem dá 422.
   - **Transporte:** a reserva registra como o paciente vai, em texto, até a ligação com o Transporte.
   - **Ninguém interna no leito reservado** fora deste pedido.
4. **Reserva vencida, sem rotina agendada:**
   - **Na consulta:** quem consulta vê o leito como livre e o pedido de volta à fila.
   - **Na ação:** quem age (reservar de novo, admitir) grava o vencimento como evento, e o leito volta a livre.
   - **Admitir com a reserva vencida** dá 422.
5. **Vaga zero:**
   - **Quando:** sem leito livre, o regulador manda para uma unidade com justificativa (400 sem ela), como na urgência.
   - **Na admissão:** a unidade escolhe o leito livre (400 sem ele).
6. **Destino** (`INTERNACAO.GERENCIAR` na unidade de destino), aba "A receber":
   - **Admitir:**
     - cria a internação no leito reservado, como urgência;
     - se o paciente estava internado na origem, a internação de lá fecha com **alta por transferência** e o leito de lá vai para
       higienização.
   - **Recusar:** com motivo, o pedido volta à fila **na posição original** e o leito volta a livre.
7. **Ciclo:**
   - **Estados:** aguardando vaga → reservado (ou vaga zero) → admitido.
   - **Negado** pelo regulador, com motivo, só enquanto aguarda vaga.
   - **Cancelado** pela origem, com motivo, até a admissão. O cancelamento desfaz a reserva.
   - **Eventos:** cada passo vira evento imutável (`TB_EVENTO_SOLICITACAO_LEITO`).
8. **Faixa etária do leito** (`faixa_etaria`): adulto, pediátrico ou neonatal.
   - **Faixas:** neonatal até 28 dias, pediátrico até 13 anos incompletos e adulto a partir de 13.
   - **Na internação**, inclusive na própria unidade: fora da faixa dá 422. Com a justificativa do médico, a internação passa e a
     justificativa vai para o evento de admissão.
   - **Sem data de nascimento,** não confere.
   - **No cadastro,** a faixa vem do tipo do leito quando não for informada. A migração aplica a mesma regra aos leitos existentes: UTI
     neonatal fica neonatal; pediátrico e UTI pediátrica ficam pediátricos; os demais ficam adultos.
9. **Papel e permissões:**
   - `INTERNACAO.SOLICITAR_VAGA` vai para o Médico e o Médico regulador de urgência;
   - `LEITO.REGULAR` é do papel novo **Regulador de leitos**, que também consulta internações e transporte.

### API

- `POST /api/v1/regulacao-leito/` e `/{id}/cancelamento` (`INTERNACAO.SOLICITAR_VAGA`);
- `GET /regulacao-leito/fila` e `/{id}/vagas`; `POST /{id}/reserva` e `/{id}/negacao` (`LEITO.REGULAR`);
- `GET /regulacao-leito/a-receber`; `POST /{id}/admissao` e `/{id}/recusa` (`INTERNACAO.GERENCIAR`);
- `GET /regulacao-leito/{id}` (leitura auditada) e `GET /regulacao-leito/?pacienteId=`;
- no leito: `faixaEtaria`, `reservadoAte` e `reservaSolicitacaoId`; na internação: `justificativaFaixaEtaria`.

### Telas

- **Leitos:**
  - abas **"Central de leitos"** (fila, com a gaveta do pedido: vagas compatíveis, reservar, vaga zero e negar) e **"A receber"** (admitir
    e recusar);
  - botão **"Pedir vaga em outra unidade"** no cabeçalho e no detalhe da internação;
  - no mapa, o leito reservado com o prazo e a faixa etária;
  - no cadastro, a faixa etária;
  - na internação, a justificativa de faixa etária.
- **Atendimentos:** "Pedir vaga" ao lado de "Internar".
- **Usuários:** o grupo "Regulação de leitos".

### Migração

`V69__regulacao_de_leitos.sql`:

- a faixa etária e a reserva no leito, com o preenchimento dos leitos existentes;
- a situação RESERVADO;
- as tabelas do pedido e dos eventos.

## Alternativas descartadas

- **Rotina agendada para vencer as reservas:** mais uma peça para operar. O vencimento visto na consulta e gravado na ação tem o mesmo
  efeito.
- **Gerar o pedido sozinho a partir da transferência da urgência:** a ocorrência guarda o identificador do pedido de vaga, mas quem pede o
  leito continua sendo a unidade com o paciente. Gerar sozinho criaria pedido sem quem responda por ele.
- **Isolamento como marcação do pedido:** o tipo de leito Isolamento já existe e entra na compatibilidade pelo tipo.
- **Conferir a faixa etária na troca de leito:** a troca é na mesma unidade e não tem campo de justificativa. A regra vale na admissão,
  que é quando o leito é escolhido.

## Consequências

- **Correção do STATUS:** a modalidade da internação e a comunicação ao Ministério Público estavam como pendentes, mas foram feitas na
  [ADR-0123](./0123-saude-mental-risco-crise.md).
- **Médico regulador:** conceder o papel nos ambientes, quando a autorização for ligada, é tarefa de implantação, não de código.
- **Ficam para as próximas fatias:** RL2 a RL5, acima.

## Testes

- **JUnit:**
  - `RegulacaoLeitoControllerTest`:
    - pedido, fila, vagas compatíveis, reserva e admissão;
    - recusa e reserva vencida;
    - vaga zero;
    - transferência da internação da origem;
    - faixa etária na vaga e na internação;
    - negar e cancelar;
  - `RegulacaoLeitoAutorizacaoControllerTest`, com a segurança ligada: o médico da origem pede, o regulador da rede reserva, o NIR do
    destino admite; a recepção e o NIR de outro hospital recebem 403.
- **Robot:** `POST_regulacao_leito.robot` (API, CT-001 a CT-006) e os casos CT-010 a CT-012 de `UI_leitos.robot`.
