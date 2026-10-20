# 0123 — Saúde Mental: risco com histórico, crise e internação psiquiátrica (SM3)

## Status

Aceita e implementada (fatia SM3 da [ADR-0112](./0112-saude-mental.md), sem a sugestão de notificação no sistema
— Vigilância V1 — e sem o acionamento do SAMU — regulação de urgência).

## Contexto

- **Até a SM2, o risco era só um aviso:** o acolhimento com risco inicial alto avisava que a crise viria na SM3. A
  ADR-0112 manda tratar o risco separado da avaliação, com histórico, e nunca como um `boolean riscoAlto`.
- **A crise não é simplesmente um atendimento:** tem identificação, avaliação, intervenção, acionamento da rede,
  encaminhamento e acompanhamento pós-crise, e pode acontecer fora do CAPS (UBS, urgência).
- **A internação psiquiátrica usa a Internação que já existe** (ADR-0098). Faltavam a modalidade (Lei 10.216/2001,
  art. 6º) e a comunicação da involuntária ao Ministério Público em 72 horas (art. 8º).
- **Duas partes dependem de fora:** a notificação de violência autoprovocada no sistema (Vigilância V1) e o
  acionamento do SAMU (regulação de urgência, pendente na Regulação).

## Decisão

1. **Risco com histórico** (`AvaliacaoRisco`, migração V17):
   - No caso: tipo (`AUTOAGRESSAO · HETEROAGRESSAO · USO_DE_SUBSTANCIAS · VULNERABILIDADE · OUTRO`), nível
     (`SEM_RISCO_ATUAL · BAIXO · MODERADO · ALTO`), fatores, medidas adotadas (obrigatórias de moderado para cima,
     400), plano de segurança, frequência de contato combinada e "reavaliar em" (obrigatória no alto, 400; nunca no
     passado), profissional e data.
   - **Reavaliar grava um registro novo.** O vigente de cada tipo é o mais recente; "sem risco atual" fecha o tipo.
   - **Indicadores:** "Risco alto" (algum tipo vigente em alto) e "Reavaliação vencida", na lista, nos números e no
     filtro por pendência; os casos com risco alto vêm primeiro.
   - O acolhimento com risco alto passa a sugerir a avaliação de risco no caso e o registro da crise.
2. **Crise** (`EventoCrise` e `EtapaCrise`):
   - **Permissão nova `SAUDE_MENTAL.CRISE`** (Médico, Enfermeiro, Coordenador de enfermagem, Psicólogo, Assistente
     social).
   - Do paciente na unidade, **com ou sem caso**: se há caso vigente, liga-se a ele. Tipo (`AUTOAGRESSAO ·
     TENTATIVA_DE_SUICIDIO · HETEROAGRESSAO · AGITACAO_PSICOMOTORA · SURTO_PSICOTICO · INTOXICACAO · OUTRO`), local,
     descrição e hora da identificação.
   - **Etapas, só inclusão:** avaliação, intervenção, acionamento da rede (em texto: família, SAMU, CAPS, hospital),
     encaminhamento e pós-crise, cada uma com descrição, profissional e hora (entre a identificação e agora, 400).
   - **Encerrar** pede a etapa de pós-crise (422) e o desfecho (`PERMANECEU_NA_UNIDADE · RETORNO_AO_DOMICILIO ·
     ENCAMINHADO_A_URGENCIA · INTERNACAO · OUTRO`); com caso, a tela sugere reavaliar o risco e revisar o PTS.
   - **Notificação compulsória:** autoagressão e tentativa de suicídio marcam a notificação de violência
     autoprovocada como sugerida. O profissional registra que notificou (data e número, opcional); a sugerida sem
     esse registro é pendência ("notificações pendentes"). Quando a V1 vier, vira a notificação de verdade.
   - **Sigilo:** lê o conteúdo quem registrou, quem tem `SAUDE_MENTAL.CRISE` ou `SAUDE_MENTAL.ACOMPANHAR` na unidade
     da crise, quem tem o conteúdo do caso liberado e o acesso justificado (senão, 403). Escrever pede
     `SAUDE_MENTAL.CRISE` na unidade da crise. Leitura auditada.
3. **Internação psiquiátrica** (na Internação, ADR-0098):
   - **Modalidade** (`VOLUNTARIA · INVOLUNTARIA · COMPULSORIA`), opcional (só na psiquiátrica), informada com
     `INTERNACAO.GERENCIAR`; na compulsória, a ordem judicial é obrigatória (400).
   - **Comunicação ao Ministério Público** (`ComunicacaoMinisterioPublico`), só na involuntária (outra modalidade:
     422): na admissão e na alta, cada uma uma vez (422 se repetida; a de alta só depois da alta), com responsável,
     protocolo e observação.
   - **Prazo de 72 horas** da admissão e da alta: a internação mostra a comunicação pendente, o prazo e se venceu; a
     lista tem o filtro `comunicacaoMpPendente`.
   - **A alta não é bloqueada:** o cuidado não espera; a pendência da comunicação de alta fica à vista.
   - O encerramento do programa e o resto da Internação não mudam.
4. **No caso em saúde mental:** o detalhe traz o histórico de risco (vigentes marcados), as crises ligadas ao caso,
   com as etapas, e as internações do paciente, só leitura.
5. **API:**
   - `POST /saude-mental/acompanhamento/{id}/risco`;
   - `POST /saude-mental/crise`, `POST /saude-mental/crise/{id}/etapa`, `/encerramento` e `/notificacao`;
   - `GET /saude-mental/crises` (escopo, filtros `unidadeId` e `situacao`) e `GET /saude-mental/crise/{id}`;
   - `POST /internacao/{id}/modalidade` e `POST /internacao/{id}/comunicacao-mp`; `GET /internacao/?comunicacaoMpPendente=true`.
6. **Tela:**
   - **Saúde Mental:** aba **Risco** no caso (vigente por tipo, Avaliar e Reavaliar, histórico, crises e internações);
     **Registrar crise** no cabeçalho e no caso; painel **Crises em curso ou com notificação pendente**; gaveta da
     crise com a linha do tempo, Registrar etapa, Encerrar e Registrar notificação; filtros "Risco alto" e
     "Reavaliação vencida".
   - **Leitos:** no detalhe da internação, a modalidade e a comunicação ao MP com o prazo; o aviso "Comunicação ao
     Ministério Público pendente" no topo.

## Consequências

- **O risco deixa de ser um aviso solto:** tem histórico, vigência por tipo e reavaliação com prazo.
- **A crise atravessa a rede:** pode ser registrada na UBS ou na urgência e aparece no caso do CAPS.
- **A internação involuntária cumpre a Lei 10.216/2001** com prazo e registro de quem comunicou.
- **Fora do escopo:** notificação de verdade no sistema (Vigilância V1); acionamento do SAMU pela regulação de
  urgência; escalas clínicas de risco padronizadas; intervenção em crise como tipo de intervenção (a crise tem o seu
  registro).

## Testes

- **JUnit:**
  - `RiscoCriseControllerTest`: risco com histórico e vigente por tipo; medidas e reavaliação obrigatórias e
    reavaliação no passado (400); indicadores e filtro de risco alto e reavaliação vencida; crise com caso, etapas,
    encerrar sem pós-crise (422), etapa depois de encerrar (422), notificação sugerida, pendente e registrada (repetida,
    422); crise sem caso e sem notificação.
  - `InternacaoPsiquiatricaControllerTest`: compulsória sem ordem judicial (400) e com ela; involuntária com a
    comunicação pendente e vencida (admitida há 80 horas), filtro, comunicação na admissão, alta sem bloqueio com a
    pendência da alta e a comunicação da alta.
  - `SaudeMentalAutorizacaoControllerTest`: o gestor não registra crise; a referência lê a crise registrada por outro
    da unidade; o médico de outra unidade não lê, não lista e não registra etapa.
- **Robot de API** (`test/saude_mental/SaudeMentalRiscoCrise.robot`) e **de interface** (`UI_saude_mental.robot`).
