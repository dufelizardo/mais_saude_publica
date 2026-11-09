# 0176 — Regulação de urgência

## Status

Aceita e implementada. Completa a Regulação (domínio 11) com a parte de urgência que a [ADR-0087](./0087-regulacao-do-acesso-backend.md)
deixou de fora, e fecha o que faltava da SM3 ([ADR-0123](./0123-saude-mental-risco-crise.md)): o acionamento do SAMU pela crise.

## Contexto

- **A Central de Regulação do Acesso é eletiva:** fila por cor, vaga marcada na executante, contrarreferência
  ([ADR-0087](./0087-regulacao-do-acesso-backend.md) a [ADR-0089](./0089-regulacao-agendamento-desfecho-e-vinculo.md)). Uma urgência não
  entra em fila de dias: o chamado chega, um médico regulador decide em minutos e um recurso sai.
- **O modelo operacional já tem a central e o destino:** a capacidade **Regulação médica de urgência (192)**, do perfil Atenção móvel, e o
  perfil **Urgência** (UPA e pronto-socorro), com estabilização e transferência ([ADR-0117](./0117-modelo-operacional-perfis-e-capacidades.md)).
  Pela MO4 ([ADR-0170](./0170-modelo-operacional-dominios-consultam-capacidades.md)), o domínio pergunta pela capacidade, e não pelo tipo de
  unidade.
- **A crise em saúde mental** registrava em texto que o SAMU foi acionado, porque não havia para onde mandar o pedido.
- **Viaturas, equipes e bases são do Transporte (#16),** o próximo item da ordem do usuário. Esta fatia não espera por ele.

## Decisão

1. **Ocorrência de urgência** (`TB_OCORRENCIA_URGENCIA`).
   - **Protocolo** `2026-000123`: o ano e uma sequência (`sq_protocolo_urgencia`) que não reinicia, para informar ao telefone.
   - **Central:** uma unidade com a capacidade `REGULACAO_MEDICA_URGENCIA` vigente; sem ela, 422.
   - **Origem:**
     - chamado 192, com o telefone de retorno obrigatório (400);
     - pedido de uma unidade, com a unidade obrigatória (400);
     - crise em saúde mental, só pelo acionamento na crise (400 pela abertura comum).
   - **Paciente opcional:** na rua, muitas vezes ninguém sabe quem é. Fica uma descrição (sexo, idade aproximada), e o paciente é vinculado
     depois, pela central ou pela unidade de destino (409 se já tem).
   - **Local:** o tipo (o mesmo `LocalRealizacao` do atendimento, ADR-0166), endereço, referência e coordenadas opcionais (as duas juntas, 400).
   - **Queixa,** dado de saúde, e quem recebeu: o CPF do login e, opcional, a matrícula do atendente (TARM).
2. **Regulação médica,** sempre com a matrícula do médico regulador.
   - **Gravidade** nas quatro cores da `PrioridadeRegulacao`, a mesma linguagem da central eletiva. Antes da regulação, a ocorrência fica sem
     gravidade, como "aguardando regulação".
   - **Decisão:**
     - **orientação por telefone:** sem recurso nem destino (400); fecha a ocorrência como orientada;
     - **envio de recurso:** o tipo é obrigatório (USA, USB, motolância, aeromédico, outro), mais a viatura em texto até o Transporte; o
       destino pode ficar para depois, com a equipe no local;
     - **encaminhamento:** o paciente vai por meios próprios a uma unidade indicada; destino obrigatório e sem recurso (400);
     - **transferência:** só de um pedido de unidade (422), com destino obrigatório e recurso opcional.
   - **Destino:** unidade ativa, com o perfil Urgência vigente, diferente da que pediu (422). A tela mostra os leitos livres como informação.
   - **Vaga zero:** a prerrogativa do regulador de mandar sem vaga confirmada. Não é bloqueada; pede justificativa (400) e fica registrada.
   - **Reclassificação** da gravidade enquanto aberta e já regulada, com motivo (a mesma: 422). O **destino** pode ser definido ou trocado até
     a chegada ao destino.
3. **Deslocamentos,** pelo rádio-operador (a mesma permissão do atendente): envio do recurso, chegada ao local, saída do local e chegada ao
   destino.
   - Só com recurso (422), um de cada (409), na ordem (422); a chegada ao destino pede o destino (422).
   - A hora pode ser informada, entre o último passo e agora (400): o rádio-operador registra depois de saber.
   - O envio põe a ocorrência em atendimento.
4. **Recebimento no destino,** pela unidade que recebe (`ATENDIMENTO.GERENCIAR` lá).
   - Com recurso, só depois da saída do local (422); registra a chegada ao destino se ela faltava.
   - Fecha a ocorrência com o desfecho **recebido no destino**.
5. **Encerramento** pelo regulador: atendido no local e óbito no local pedem a chegada ao local; recusa, sem ocorrência (engano ou trote) e
   desistência. Antes da regulação, só sem ocorrência ou desistência (422). Orientado e recebido no destino têm caminho próprio (422).
6. **Cancelamento** (duplicada, por engano), pelo atendente ou pelo regulador, antes do envio do recurso (422).
7. **Eventos imutáveis** (`TB_EVENTO_URGENCIA`), no padrão do `EventoRegulacao`: tipo, situação e gravidade resultantes, texto, profissional
   e hora. Os **tempos de resposta** saem deles: do chamado à regulação, do chamado ao envio, do envio à chegada ao local e o total.
8. **Acionar o SAMU pela crise** (`POST /saude-mental/crise/{uuid}/acionamento-samu`, `SAUDE_MENTAL.CRISE` na unidade da crise):
   - abre a ocorrência com o paciente, a unidade da crise como solicitante, o local da crise e o motivo informado (a descrição da crise não é
     copiada);
   - registra a etapa **acionamento da rede** com o protocolo;
   - crise encerrada ou central sem a capacidade: 422; já há ocorrência aberta para a crise: 409.
9. **Quem vê o quê.**
   - **A central** (atendente e regulador, no escopo da central) e **a unidade de destino** veem o dado clínico: queixa, conduta, paciente,
     quem chamou e o texto dos eventos.
   - **Quem só consulta** (`URGENCIA.CONSULTAR`, a gestão) e **a unidade que pediu** veem o andamento: protocolo, situação, gravidade,
     decisão, recurso, destino e os tempos.
   - **O prontuário** ganha as ocorrências de urgência do paciente, com o vínculo assistencial de sempre.
10. **Permissões e papéis.**
    - `URGENCIA.CONSULTAR` (operação), `URGENCIA.RECEBER` e `URGENCIA.REGULAR` (dado de saúde).
    - Papéis novos: **Atendente de regulação (TARM)** (receber) e **Médico regulador de urgência** (regular), pela Portaria 2.048/2002. O
      **Gestor** ganha a consulta.

### API

- `POST /api/v1/urgencia/ocorrencia`; `GET /ocorrencia` (`centralId`, `situacao`, `destinoId`, `dias`: 7, até 90); `GET /ocorrencia/{uuid}`;
- `POST /ocorrencia/{uuid}/regulacao`, `/destino`, `/reclassificacao`, `/deslocamento`, `/recebimento`, `/encerramento`, `/cancelamento`
  e `/paciente`;
- `GET /painel` (`centralId`), `GET /destinos`, `GET /centrais`;
- `POST /api/v1/saude-mental/crise/{uuid}/acionamento-samu`;
- `GET /api/v1/prontuario/{pacienteId}` passa a trazer `ocorrenciasUrgencia`.

### Tela

**Regulação de urgência,** em Assistência (menu "Urgência 192"):

- o resumo: aguardando regulação, abertas em vermelho e amarelo, recurso em campo e os tempos médios do dia;
- o painel das abertas, aguardando regulação primeiro e depois por gravidade e espera, com o tempo correndo;
- a ficha: os dados, a linha do tempo, os tempos de resposta e as ações de cada papel (regular, registrar o próximo passo, definir o destino,
  reclassificar, receber no destino, identificar o paciente, encerrar, cancelar);
- os destinos de urgência, com os leitos livres e o que já vai para cada um.

Na crise em saúde mental, **Acionar o SAMU** escolhe a central e informa o motivo. O prontuário, na tela de atendimentos, mostra as
ocorrências de urgência do paciente.

### Migração

`V61__regulacao_de_urgencia.sql`.

## Consequências

- **O recurso é um tipo e um texto** até o Transporte (#16), que troca o texto pela viatura cadastrada, com equipe e base.
- **Os tempos de resposta já existem** para o BI usar depois (indicadores do SAMU por central).
- **Ficam para depois:**
  - a ficha clínica do atendimento pré-hospitalar;
  - a regulação de leitos entre unidades (domínio 12);
  - a telefonia e a integração com o 192;
  - a reserva de leito na vaga zero;
  - a E4.

## Testes

- **JUnit:**
  - `UrgenciaControllerTest`: o ciclo do chamado ao recebimento, com vaga zero e o prontuário; as regras da abertura, da decisão, do
    deslocamento e do encerramento; o painel em ordem; o acionamento pela crise com a etapa e o 409;
  - `UrgenciaAutorizacaoControllerTest`, com a segurança ligada: sem login, o atendente de outra central, o atendente que não regula, o
    gestor sem dado clínico, a recepção do destino que recebe e a de outra unidade que não, e a enfermagem que aciona pela crise.
- **Robot:** `Urgencia.robot` (API, cinco casos) e `UI_urgencia.robot`.
