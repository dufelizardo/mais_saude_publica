# 0206 — Imunização: remanejamento entre salas, alerta de ESAVI por lote e revogação pelo CRIE (I6)

## Status

Aceita e implementada. Fatia **I6** da Imunização ([ADR-0147](./0147-imunizacao.md)), com os itens internos que a I2
([ADR-0148](./0148-imunizacao-sala-de-vacina.md)) e a I4 ([ADR-0150](./0150-imunizacao-esavi.md)) deixaram para depois. A I5 (SI-PNI/RNDS)
segue com o domínio 20.

## Contexto

- **A I2 deixou o remanejamento de fora:** a saída virava ajuste na origem e a chegada, entrada no destino, sem ligação entre as duas e
  sem a conferência da caixa térmica na chegada.
- **A I4 deixou o alerta por lote de fora:** cada ESAVI ficava isolado, e um lote com desvio de qualidade seguia sendo aplicado em outras
  salas. O lote existe em várias unidades, com o mesmo número, e na unidade sem estoque o lote é digitado na dose.
- **A contraindicação da I4 não tinha fim:** só valia "até alguém registrar a dose com justificativa"; faltava o registro da reavaliação
  no CRIE.

## Decisão

1. **Remanejamento** (`TB_REMANEJAMENTO_IMUNOBIOLOGICO`), em duas etapas, como a transferência da Farmácia
   ([ADR-0061](./0061-transferencia-em-duas-etapas-envio-e-recebimento.md)):
   - **envio** pela coordenação da origem (`IMUNIZACAO.GERENCIAR`): o destino precisa de sala de vacina (422) e não é a própria unidade
     (400); o lote está disponível e na validade (422), e as doses não passam do que está fora dos frascos abertos (422); motivo (excesso,
     validade próxima, desabastecimento ou outro, que pede a observação, 400);
   - as doses **saem na hora** (`REMANEJAMENTO_SAIDA` no livro) e ficam em trânsito, fora dos dois estoques;
   - **recebimento** pela sala do destino (`VACINACAO.REGISTRAR`), com a **temperatura da caixa** (obrigatória) e a câmara: o mesmo lote
     é criado ou somado (`REMANEJAMENTO_ENTRADA`); **fora de 2 a 8 °C**, o lote fica sob suspeita pela excursão e passa pela avaliação da
     I2; o lote vencido em trânsito ou inutilizado no destino é 422 (recusar);
   - **recusa** pelo destino, com o motivo, e **cancelamento** pela origem enquanto enviado: as doses voltam (`ESTORNO_REMANEJAMENTO`);
   - ENVIADO → RECEBIDO, RECUSADO ou CANCELADO; o segundo passo é 422.
2. **Alerta de ESAVI por lote**, calculado e sem o paciente (`IMUNIZACAO.GERENCIAR`):
   - **o lote da rede** é o imunobiológico mais o número do lote **sem espaços e em maiúsculas**; junta as doses com baixa no estoque e as
     de lote digitado;
   - **o sinal:** dois ou mais ESAVI graves não descartados notificados em 30 dias, ou um ESAVI encerrado com causalidade **A2** (desvio
     de qualidade do produto) no último ano;
   - cada alerta traz as notificações (data, unidade, gravidade, causalidade), as salas com o lote e as doses em estoque e a última
     suspensão.
3. **Suspensão do lote na rede** (`TB_SUSPENSAO_LOTE_REDE`):
   - **da coordenação da rede:** `IMUNIZACAO.GERENCIAR` com acesso a todas as unidades; quem gerencia só algumas recebe 403;
   - com ou sem sinal (o comunicado de desvio de qualidade do fabricante também vale), com o motivo (400); **uma vigente por lote** (409);
   - todos os lotes com o número ficam **sob suspeita** pelo motivo novo `ESAVI_LOTE` (`motivo_suspeita` no lote); o lote que **entra
     depois** com esse número, por compra ou remanejamento, já chega suspenso; a **dose de lote digitado** com esse número é 422;
   - a unidade **não avalia** o lote suspenso pela rede (422); ele não se aplica nem sai em remanejamento;
   - **liberar:** os lotes suspensos só pelo ESAVI voltam a disponíveis; os que também passaram por excursão de temperatura seguem para a
     avaliação da unidade; **inutilizar:** o saldo de cada sala sai como perda física pelo motivo `ESAVI_LOTE` e os frascos abertos se
     encerram; a decisão é uma só (422).
4. **Revogação da contraindicação pelo CRIE:**
   - permissão nova **`IMUNIZACAO.REAVALIAR`**, nos papéis Médico e Coordenador de enfermagem, com o vínculo assistencial do prontuário;
   - registra a data, quem decidiu e o **parecer** da reavaliação (400 sem ele); revogar de novo é 422;
   - a revogada **fica no histórico** da caderneta; só uma contraindicação vigente por paciente e imunobiológico (índice parcial), e a dose
     volta a entrar sem justificativa. Um ESAVI novo pode contraindicar de novo.
5. **Telas:**
   - **Sala de Vacina:** "Remanejar" no lote do estoque; aba **Remanejamentos** (a receber, com a temperatura da caixa, e a recusa; o
     histórico, com o cancelamento da origem); aba **Alertas de lote** (o sinal, as notificações, suspender, e liberar ou inutilizar com o
     parecer); o lote mostra o motivo da suspeita, e a cadeia de frio não oferece "Avaliar" ao lote suspenso pela rede;
   - **Caderneta:** "Revogar (CRIE)" na contraindicação vigente, para quem tem `IMUNIZACAO.REAVALIAR`, e o histórico das revogadas.

### API

| Método | Caminho (`/api/v1/`) | Permissão |
|---|---|---|
| GET | `sala-de-vacina/remanejamentos?unidadeId` | `VACINACAO.REGISTRAR` ou `IMUNIZACAO.GERENCIAR` |
| POST | `sala-de-vacina/remanejamento`, `remanejamento-imunobiologico/{id}/cancelamento` | `IMUNIZACAO.GERENCIAR` na origem |
| POST | `remanejamento-imunobiologico/{id}/recebimento`, `/recusa` | `VACINACAO.REGISTRAR` no destino |
| GET | `imunizacao/alertas-lote`, `imunizacao/suspensoes-lote` | `IMUNIZACAO.GERENCIAR` |
| POST | `imunizacao/suspensao-lote`, `imunizacao/suspensao-lote/{id}/decisao` | `IMUNIZACAO.GERENCIAR` com abrangência da rede |
| POST | `contraindicacao-vacinal/{id}/revogacao` | `IMUNIZACAO.REAVALIAR` |

O lote do estoque devolve `motivoSuspeita` e `suspensoNaRede`; a contraindicação da caderneta, `uuid`, `revogadaEm` e `parecerRevogacao`.

### Migração

`V88__imunizacao_remanejamento_lote_rede_crie.sql`:
- `tb_suspensao_lote_rede` (com o índice único parcial da vigente) e `tb_remanejamento_imunobiologico`;
- no lote, `motivo_suspeita` (os sob suspeita existentes ficam com a excursão) e `suspensao_rede_id`;
- no livro, os tipos de remanejamento, o motivo de perda `ESAVI_LOTE` e `remanejamento_id`;
- na contraindicação, a revogação, e o índice parcial da vigente no lugar da restrição única.

## Alternativas descartadas

- **Remanejamento em uma etapa** (saída e entrada juntas): perde a conferência da caixa e o recebimento de quem guarda o lote.
- **Suspender sozinho pelo sinal:** o ESAVI grave pode ser coincidente; a rede decide, o sistema avisa.
- **Suspensão por unidade:** o lote com desvio de qualidade é o mesmo em toda a rede; a unidade avaliar a sua cópia isolada repetiria a
  decisão com informação parcial.
- **Apagar a contraindicação revogada:** perde o histórico clínico do ESAVI e da reavaliação.

## Consequências

- **STATUS-DOS-DOMINIOS:** o remanejamento e o alerta por lote com a revogação saem do "Falta" da Imunização; ficam o pedido à rede de
  frio, o termômetro automático (integração, #20), a cobertura sobre o SINASC e a I5.
- **O recebimento fora da faixa** põe sob suspeita o lote inteiro do destino quando ele já existia: as doses se misturam na câmara.

## Testes

- **JUnit `ImunizacaoRedeControllerTest`:** o remanejamento (as recusas do envio: 400, 422; recusa e cancelamento devolvem; recebimento
  na faixa cria o lote; fora da faixa, sob suspeita; o lote suspeito não sai; o livro e as listas); o alerta (dois graves com o número
  escrito de jeitos diferentes; o não grave não conta; o A2); a suspensão (os dois lotes sob suspeita, 409, a dose barrada no estoque e no
  lote digitado, o hífen não é normalizado, a avaliação da unidade e o remanejamento barrados, a suspensão sem sinal alcança a entrada
  posterior, liberar e inutilizar com a perda `ESAVI_LOTE`); a revogação (400, 200 com o histórico, 422 ao repetir, a dose sem
  justificativa).
- **JUnit `ImunizacaoRedeAutorizacaoControllerTest`:** a coordenação da origem envia, a do destino e o técnico não; o técnico do destino
  recebe, o da origem não; a coordenação de uma unidade vê os alertas, mas só o gestor da rede suspende; o médico revoga, o técnico não;
  sem token, 401.
- **Robot `ImunizacaoRede.robot`** (API) e os CT-009 a CT-011 de **`UI_sala_de_vacina.robot`** (remanejar e receber, suspender e
  liberar, revogar na caderneta).
