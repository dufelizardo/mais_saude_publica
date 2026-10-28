# 0148 — Imunização: sala de vacina e cadeia de frio (I2)

## Status

Aceita e implementada. Segunda fatia do domínio 24, desenhado na [ADR-0147](./0147-imunizacao.md).

## Contexto

- **Na I1, o lote da dose era texto.** Não havia estoque de vacina: não se sabia quanto havia na sala, o que vencia,
  quanto se perdia nem se a dose aplicada tinha saído de um lote que estava em condições.
- **A sala de vacina tem rotinas próprias** do PNI:
  - o frasco multidose aberto vale por horas ou dias, e o que sobra no vencimento é **perda técnica**;
  - a perda por quebra, falha de equipamento, falta de energia, transporte ou vencimento do lote é **perda física**;
  - a temperatura da câmara é lida duas vezes ao dia (atual, mínima e máxima), na faixa de 2 a 8 °C;
  - na **excursão** (leitura fora da faixa), os imunobiológicos ficam **sob suspeita** e não se aplicam até a
    avaliação: a coordenação consulta o fabricante ou a rede de frio e libera ou inutiliza.
- **Nem toda unidade tem sala de vacina,** e quem vacina não é só a UBS: a maternidade aplica BCG e hepatite B ao nascer,
  e há o CRIE e o ponto de vacinação avulso.
- **O padrão de estoque já existe na Farmácia** (ADRs 0050 e 0057): lote com saldo corrente e um livro de movimentação
  imutável, com o saldo depois de cada lançamento.

## Decisão

1. **A sala de vacina é capacidade da unidade** (modelo operacional, [ADR-0117](./0117-modelo-operacional-perfis-e-capacidades.md)):
   `SALA_VACINA`, **transversal** (sem perfil), porque vacinam a UBS, a maternidade, o CRIE e o ponto de vacinação.
   - **Unidade com a capacidade:** controla estoque e cadeia de frio. A dose aplicada ali **precisa vir de um lote do
     estoque** (400 sem ele), com a baixa automática.
   - **Unidade sem a capacidade:** segue como na I1, com o lote em texto; o lote do estoque ali é 422.
   - A dose **transcrita da caderneta** nunca sai do estoque.
2. **Lote de imunobiológico por unidade** (`TB_LOTE_IMUNOBIOLOGICO`):
   - **o que tem:** imunobiológico, número (em maiúsculas), fabricante, validade, saldo em doses, câmara e situação
     (disponível, sob suspeita ou inutilizado);
   - **único** por unidade, imunobiológico e número;
   - **a entrada** cria o lote ou soma ao existente. Validade diferente da do mesmo número, lote vencido ou já
     inutilizado são 422.
3. **Livro de movimentação** (`TB_MOVIMENTACAO_IMUNOBIOLOGICO`), como o da Farmácia (ADR-0057):
   - **imutável, em doses,** com o saldo depois de cada lançamento;
   - **os tipos:** entrada, aplicação, estorno de aplicação, perda física, perda técnica e ajuste de inventário;
   - o lançamento da aplicação e do estorno aponta a dose e o frasco;
   - **erro se corrige com outro lançamento:** ajuste ou estorno.
4. **A dose aplicada** na unidade com sala de vacina:
   - **o lote precisa servir** (422 se não serve): ser da unidade e do mesmo imunobiológico, estar disponível e dentro da
     validade na data da dose, e ter saldo;
   - **a baixa** tira uma dose, com o lote travado (`PESSIMISTIC_WRITE`) para duas aplicações ao mesmo tempo não passarem
     do saldo;
   - **o lote em texto** da dose é preenchido com o número do lote, e o fabricante com o do lote, se não vier.
5. **Frasco multidose aberto** (`TB_FRASCO_ABERTO`), para o imunobiológico com mais de uma dose por frasco e validade
   depois de aberto:
   - **a primeira dose abre o frasco,** com as doses do frasco (até o saldo) e o prazo `aberto + validade depois de
     aberto`;
   - **as seguintes usam o frasco aberto;** ao usar a última dose, ele se encerra como **esgotado**;
   - **no vencimento, o frasco se encerra como vencido,** e as doses que sobraram saem do lote como **perda técnica**. A
     apuração é preguiçosa: na próxima consulta ao estoque ou na próxima aplicação do lote, sem tarefa agendada;
   - o saldo do lote conta as doses do frasco aberto, porque elas ainda estão na sala até vencer.
6. **Estorno:**
   - **a anulação** da dose que saiu do estoque devolve a dose ao lote;
   - **com frasco ainda no prazo,** a dose volta ao frasco, que reabre se tinha esgotado;
   - **com frasco vencido,** a dose volta e sai de novo como perda técnica (ela de fato se perdeu);
   - **a retificação que mantém o lote** segue ligada ao mesmo lote e frasco, sem lançamento;
   - **a retificação que troca de lote** estorna a anterior e baixa no novo.
7. **Perda física e ajuste:**
   - **a perda física** é de quem vacina: doses até o saldo (422), com o motivo. "Outro" pede a justificativa, e "frasco
     aberto vencido" é só do sistema (400);
   - **o ajuste de inventário** é da coordenação: o saldo contado, com a justificativa. Saldo igual ao do sistema é 422.
8. **Cadeia de frio:**
   - **câmara de refrigeração** (`TB_CAMARA_REFRIGERACAO`):
     - da unidade com sala de vacina (422 sem ela), com nome único na unidade (409);
     - faixa padrão de 2 a 8 °C; a mínima precisa ser menor que a máxima (400);
     - pode ser desativada;
   - **leitura de temperatura** (`TB_LEITURA_TEMPERATURA`):
     - **o que registra:** atual, mínima e máxima do período, o turno (manhã ou tarde, pela hora se não vier), quem leu e
       a observação;
     - **imutável;**
     - **em ordem:** mínima ≤ atual ≤ máxima (400); não pode ser no futuro;
     - **câmara desativada** não recebe leitura (422);
   - **excursão:** a mínima abaixo da faixa ou a máxima acima dela. Os lotes disponíveis da câmara, com saldo, ficam
     **sob suspeita**, e a dose desses lotes é 422;
   - **turno sem leitura** vira pendência no painel: a manhã depois do meio-dia e a tarde depois das 18 h, e os dois em
     dias passados;
   - **avaliação do lote sob suspeita** pela coordenação, com o parecer:
     - **liberar:** o lote volta a disponível;
     - **inutilizar:** o saldo inteiro sai como perda física por excursão de temperatura, e o frasco aberto se encerra;
     - avaliar lote que não está sob suspeita é 422.
9. **Permissões:**
   - **leitura do estoque, do livro e da cadeia de frio:** `VACINACAO.REGISTRAR` ou `IMUNIZACAO.GERENCIAR` na unidade;
   - **entrada, perda física e leitura de temperatura:** `VACINACAO.REGISTRAR` na unidade (o técnico de enfermagem);
   - **câmara, ajuste de inventário e avaliação do lote:** `IMUNIZACAO.GERENCIAR` na unidade (a coordenação e a gestão).

   Sem permissão nova.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `sala-de-vacina/estoque?unidadeId` | `VACINACAO.REGISTRAR` ou `IMUNIZACAO.GERENCIAR` (escopo) |
| POST | `sala-de-vacina/entrada` | `VACINACAO.REGISTRAR` (escopo) |
| POST | `lote-imunobiologico/{id}/perda` | `VACINACAO.REGISTRAR` (escopo) |
| POST | `lote-imunobiologico/{id}/ajuste`, `lote-imunobiologico/{id}/avaliacao` | `IMUNIZACAO.GERENCIAR` (escopo) |
| GET | `lote-imunobiologico/{id}/movimentacoes` | `VACINACAO.REGISTRAR` ou `IMUNIZACAO.GERENCIAR` (escopo) |
| GET | `sala-de-vacina/cadeia-de-frio?unidadeId&data` | `VACINACAO.REGISTRAR` ou `IMUNIZACAO.GERENCIAR` (escopo) |
| POST / PUT | `camara-refrigeracao`, `camara-refrigeracao/{id}` | `IMUNIZACAO.GERENCIAR` (escopo) |
| POST | `camara-refrigeracao/{id}/leitura` | `VACINACAO.REGISTRAR` (escopo) |

O registro da dose (`POST vacinacao`) ganha `loteEstoqueId`, e a dose devolve `loteEstoqueId` e `frascoId`.

### Tela

- **Sala de Vacina,** com duas abas novas:
  - **Estoque:**
    - o mês: doses aplicadas, frascos abertos agora, perda técnica e perda física;
    - as doses aplicáveis por vacina;
    - os lotes, com a validade (vencido, ou vence em 30 dias), as doses, a câmara e a situação;
    - as ações de cada lote: perda, ajuste e livro;
    - os frascos abertos, com o que resta e por quanto tempo valem;
    - "Entrada de lote", na gaveta;
    - a unidade sem a capacidade vê o aviso de como passar a controlar o estoque;
  - **Cadeia de frio:**
    - o dia;
    - as leituras pendentes e os lotes sob suspeita, com "Avaliar" (liberar ou inutilizar, com o parecer);
    - as câmaras, com a faixa, a última leitura e "Registrar leitura";
    - as leituras do dia, com a marca de excursão;
    - "Nova câmara" e "Editar".
- **Registro da dose:** na unidade com sala de vacina, o campo de lote em texto vira a **lista dos lotes aplicáveis da
  vacina escolhida** (número, validade e doses).

### Migração

`V37__imunizacao_sala_de_vacina.sql`:
- a capacidade `SALA_VACINA`, transversal;
- as tabelas `tb_camara_refrigeracao`, `tb_leitura_temperatura`, `tb_lote_imunobiologico`, `tb_frasco_aberto` e
  `tb_movimentacao_imunobiologico`;
- as colunas `lote_estoque_id` e `frasco_id` em `tb_registro_vacinacao`;
- conferida contra o `ddl-auto=validate`.

## Consequências

- **Ligar a capacidade muda o registro na unidade:** a partir dali, a dose aplicada exige o lote do estoque. Antes de
  ligar, a unidade dá entrada no que tem na sala.
- **A perda técnica aparece quando alguém olha:** o frasco vencido se encerra na próxima consulta ao estoque ou na
  próxima aplicação do lote. Os números do mês só mudam depois disso. Uma apuração agendada pode vir com a I3, se a
  cobertura precisar do número exato no fechamento.
- **Ficam para depois:**
  - **o remanejamento entre unidades** (transferência de lote de uma sala para outra), que pede o recebimento na
    destinatária e cabe com a rede de frio municipal. Por ora, a saída é um ajuste na origem e a chegada uma entrada no
    destino, cada uma com a justificativa;
  - **o pedido de imunobiológico** à rede de frio;
  - **o termômetro automático** (registrador de dados), que depende do domínio 20.
- **O lote da Farmácia e o lote de vacina são livros separados,** como a ADR-0147 decidiu: vacina não é medicamento do
  estoque da Farmácia.

## Testes

- **JUnit:**
  - `SalaDeVacinaControllerTest`:
    - entrada que cria e soma, e as recusas (validade divergente, lote vencido, unidade sem sala de vacina);
    - baixa na dose aplicada, sem lote do estoque (400) e com lote de outra vacina (422);
    - o frasco multidose aberto e reusado;
    - o estorno na anulação, que devolve ao lote e ao frasco;
    - a retificação que mantém o lote (sem lançamento) e a que troca (estorno e baixa);
    - a perda técnica do frasco vencido;
    - unidose sem frasco, perda física até o saldo, ajuste;
    - a unidade sem sala de vacina, como na I1;
    - a câmara (padrão, repetida, faixa invertida, sem sala de vacina);
    - a leitura (na faixa, fora de ordem) e as pendências do dia anterior;
    - a excursão que bloqueia a dose, a liberação e a inutilização;
    - a câmara desativada;
  - `SalaDeVacinaAutorizacaoControllerTest`, com segurança ligada:
    - o técnico dá entrada e lê a temperatura na própria unidade, não em outra;
    - a recepção não vê o estoque, e a gestão lê e cadastra a câmara;
    - o ajuste e a avaliação são da coordenação.
- **Robot de API:** `test/imunizacao/SalaDeVacina.robot`.
- **Robot de tela:** `test/ui/imunizacao/UI_sala_de_vacina.robot`, com dois cenários novos:
  - a entrada de lote e a baixa na dose;
  - a excursão e a avaliação do lote.
