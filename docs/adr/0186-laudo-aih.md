# 0186 — Regulação e Leitos: laudo para solicitação de AIH e autorização interna

## Status

Aceita e implementada. Fatia **RL3** do plano da [ADR-0184](./0184-regulacao-de-leitos.md) para fechar os domínios 11 (Regulação) e
12 (Leitos e Internação):

| Fatia | O quê |
|---|---|
| RL1 | Central de regulação de leitos entre unidades, reserva de leito com prazo e faixa etária do leito |
| RL2 | Internação eletiva agendada com reserva de leito e censo hospitalar diário ([ADR-0185](./0185-internacao-eletiva-e-censo.md)) |
| **RL3 (esta)** | Laudo para solicitação de AIH e autorização interna, com o número da AIH |
| RL4 | Prescrição hospitalar diária (medicamentos, dieta e cuidados) e checagem pela enfermagem |
| RL5 | Ficha clínica do atendimento pré-hospitalar (APH) na ocorrência de urgência |

Ficam no domínio 20, sem impedir a fatia: o envio da AIH ao SIH, a validação do procedimento no SIGTAP e o faturamento.

## Contexto

- **Internação sem AIH.** A internação não tinha laudo de solicitação, autorização nem número. O STATUS do domínio 12 marcava "AIH, SIH e
  faturamento" como pendente.
- **O laudo é a porta da conta hospitalar.** No SUS, o médico assistente preenche o laudo para solicitação/autorização de internação
  hospitalar; o autorizador do gestor confere e autoriza com um número de AIH tirado das faixas que o gestor distribui. Na urgência, o
  laudo é feito depois da admissão, em prazo curto; na eletiva, antes.
- **O catálogo geral de procedimentos** ([ADR-0136](./0136-saude-bucal-procedimento-odontologico.md)) já guarda o código SIGTAP
  opcional, que serve ao laudo sem depender da tabela do SIGTAP.

## Decisão

1. **Laudo para solicitação de AIH** (`TB_LAUDO_AIH`, permissão nova `AIH.SOLICITAR`, dada ao Médico):
   - **De quê:** de uma internação ou, antes da admissão, de uma internação eletiva agendada (RL2); uma das duas (400).
   - **Conteúdo, como no laudo do SUS:**
     - paciente, unidade e caráter (da internação; eletiva para o agendamento) vêm do cadastro;
     - principais sinais e sintomas, condições que justificam a internação e resultados de provas diagnósticas;
     - diagnóstico inicial, CID-10 principal, secundário e de causa associada;
     - procedimento do catálogo geral (com o SIGTAP quando houver; inativo é 422);
     - clínica (cirúrgica, obstétrica, clínica médica, cuidados prolongados, psiquiatria, tisiologia, pediatria, reabilitação ou
       psiquiatria em hospital-dia) e causa externa (acidente de trânsito, de trabalho típico ou de trajeto, outra), quando houver;
     - médico solicitante, pela matrícula.
   - **Um laudo em andamento ou autorizado por internação ou eletiva** (409). Cancelado e negado não contam.
   - **Correção:** o laudo solicitado ou devolvido pode ser corrigido; o devolvido volta para a fila.
   - **Cancelamento:** só antes da decisão, com motivo.
2. **Autorização interna** (papel novo **Médico autorizador**, permissão `AIH.AUTORIZAR`, no escopo das unidades):
   - **Fila:** os laudos solicitados, urgência primeiro e depois a hora da solicitação.
   - **Decisões:** autorizar (observação opcional), devolver para correção ou negar (com motivo, 400 sem ele). Só o laudo solicitado é
     decidido (422).
   - **Quem solicitou não decide:** nem o mesmo profissional (matrícula), nem o usuário que registrou o laudo, nem o usuário com o CPF do
     solicitante (422).
   - **Situações:** SOLICITADO, DEVOLVIDO, AUTORIZADO, NEGADO e CANCELADO, com os eventos guardados (`TB_EVENTO_LAUDO_AIH`).
3. **Número da AIH:**
   - **Faixas** (`TB_FAIXA_AIH`, permissão nova `AIH.NUMERAR`, dada ao Gestor, na rede inteira): o ano e os 12 primeiros dígitos do
     início e do fim, como o gestor entrega. Início maior que o fim é 400; faixa sobreposta a outra é 409. A faixa pode ser desativada.
   - **Número completo:** os 12 dígitos e o dígito verificador, o resto da divisão por 11 (10 vira 0), com 13 dígitos.
   - **Na autorização:** o próximo número livre da faixa ativa do ano (a de menor início), com a faixa travada para dois autorizadores não
     pegarem o mesmo número; o número é único entre os laudos. Sem faixa ou com ela esgotada, 422 com a mensagem para cadastrar a faixa.
   - **Na internação:** o número é copiado para a internação (`TB_INTERNACAO.NUMERO_AIH`) e aparece no detalhe e no prontuário.
4. **Eletiva:** o laudo feito antes passa para a internação na admissão (evento "Passou para a internação"), com o número se já
   autorizado.
5. **Pendências:** internações de urgência ainda internadas, sem laudo (fora os cancelados), passadas 48 horas da admissão.
6. **PDF:** o laudo e a autorização, com a situação e o número, para imprimir e anexar ao prontuário físico.
7. **Telas:**
   - na gaveta da internação, a seção **Laudo de AIH**: preencher, ver a situação e o número, corrigir o devolvido, cancelar e imprimir;
   - na aba Eletivas, o botão **Laudo AIH** de cada agendamento;
   - aba **Autorização de AIH** (autorizador e gestão): fila com autorizar, devolver e negar, e as faixas de numeração com o cadastro;
   - aviso das internações de urgência sem laudo há mais de 48 horas, nas abas Internações e Autorização de AIH.

## Alternativas descartadas

- **Página pública de verificação do laudo de AIH,** como no laudo de exames ([ADR-0183](./0183-laudo-pdf-assinado-e-insumos.md)): o laudo de AIH é
  documento interno entre o hospital e o gestor; o número da AIH já identifica a autorização.
- **Gerar números sem faixa:** o número da AIH pertence ao gestor e precisa casar com o SIH. Inventar números quebraria o envio futuro.
- **Exigir o SIGTAP no procedimento:** tornaria a tabela externa impeditiva, contra a regra das integrações não impeditivas.
- **Autorização pelo próprio solicitante em unidade pequena:** a separação entre quem pede e quem autoriza é o controle da AIH; com um
  só médico, a autorização fica com o autorizador da rede.

## Consequências

- **STATUS-DOS-DOMINIOS:** "AIH" deixa a pendência do domínio 12; SIH e faturamento seguem no domínio 20.
- **Implantação:** conceder o papel Médico autorizador e cadastrar as faixas do ano recebidas do gestor, quando a autorização for ligada.
- **Fora do escopo:** laudo de mudança de procedimento e de prorrogação de permanência; espelho e faturamento da AIH; envio ao SIH.
- **Ficam para as próximas fatias:** RL4 e RL5, acima.

## Testes

- **JUnit:**
  - `LaudoAihControllerTest`:
    - dígito verificador;
    - solicitar, devolver sem e com motivo, corrigir, autorizar com o número da faixa (sem faixa, faixa sobreposta, invertida e esgotada),
      número na internação e PDF;
    - negar e cancelar com motivo;
    - laudo da eletiva passando para a internação na admissão, com o número;
    - pendência da urgência sem laudo depois de 48 horas;
  - `LaudoAihAutorizacaoControllerTest`, com a segurança ligada: o médico solicita; a recepção recebe 403; o autorizador de outro hospital
    recebe 403; o autorizador do hospital autoriza, mas não o laudo que ele mesmo registrou; a faixa é do gestor da rede (o da unidade
    recebe 403).
- **Robot:** `POST_laudo_aih.robot` (API, CT-001 a CT-005) e os casos CT-016 a CT-018 de `UI_leitos.robot`.
