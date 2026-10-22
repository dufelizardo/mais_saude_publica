# 0138 — Saúde Bucal: encaminhamento odontológico pela Regulação (SB4a)

## Status

Aceita e implementada (primeira parte da fatia SB4 da [ADR-0111](./0111-saude-bucal.md); usa a Regulação das ADRs
0087 a 0089, o Modelo Operacional da [ADR-0115](./0115-modelo-operacional.md) e o plano de tratamento da
[ADR-0137](./0137-saude-bucal-plano-de-tratamento.md)). O tratamento protético fica para a SB4b.

## Contexto

- **A ADR-0111 (seção 9 do MODELO) decidiu que não há fluxo odontológico próprio:** o encaminhamento usa a Regulação,
  com o CEO (Centro de Especialidades Odontológicas) como executante, e a contrarreferência volta ao item do plano.
- **Ficou para a SB4 a decisão do tipo do CEO,** pelo critério da ADR-0053: tipo próprio de unidade ou unidade de
  especialidades com especialização.
- **A MO1 (ADR-0115) já semeou as capacidades odontológicas** do CEO: endodontia, periodontia, cirurgia oral menor,
  diagnóstico bucal, pacientes com necessidades especiais e prótese.
- **A SB3b deixou o item do plano encaminhado com a solicitação opcional,** sem amarrar a volta.
- **Hoje a Regulação aceita qualquer unidade como executante:** nada impede autorizar uma endodontia numa policlínica
  sem dentista.

## Decisão

1. **O CEO não ganha tipo de unidade.**
   - É uma unidade de especialidades (Centro de Especialidades ou Policlínica) com as capacidades odontológicas do
     Modelo Operacional. É a "especialização" do critério 2 da ADR-0053.
   - O que a unidade faz vem das capacidades, e não do tipo. Uma policlínica com endodontia é executante de
     endodontia.
2. **O item regulado pode exigir uma capacidade da executante.**
   - `ProcedimentoRegulado.capacidadeExigida` é opcional (404 se não existir; 422 se estiver inativa).
   - **Na autorização,** a executante precisa ter a capacidade vigente (422, com o nome dela e o do item).
   - **A regra serve a toda a Regulação,** e não só à odontologia: o regulador pode exigir imagem na tomografia, por
     exemplo.
   - **O catálogo** mostra a capacidade exigida, e a gaveta do item regulado a escolhe entre as ativas.
3. **Carga das especialidades odontológicas no catálogo da Regulação,** como consulta especializada e com a capacidade
   correspondente:
   - endodontia, periodontia e cirurgia oral menor;
   - diagnóstico bucal e câncer de boca;
   - pacientes com necessidades especiais;
   - prótese dentária.

   Um item já cadastrado com o mesmo nome não é duplicado.
4. **Encaminhar a partir do item do plano.**
   - A solicitação de regulação ganha o `itemPlanoTratamentoId`, opcional.
   - **Na solicitação,** o item precisa ser do mesmo paciente, estar planejado ou em andamento e pertencer a um plano
     aberto (422; a solicitação não é gravada).
   - **O item fica encaminhado,** ligado à solicitação, com o motivo "Regulação: <item>".
5. **A volta ao plano:**
   - **o item mostra** o item regulado, a situação da solicitação, a executante e, quando ela é realizada, a
     **contrarreferência**. O item continua encaminhado, e o plano pode ser concluído;
   - **negada, cancelada ou com falta do paciente:** o item volta a planejado, sem o vínculo, com o motivo
     ("Regulação negada: …", "Regulação cancelada: …", "Paciente faltou na regulação: …"). A necessidade continua, e o
     dentista decide o próximo passo;
   - **fora do plano aberto,** nada muda no item.
6. **Acesso:** sem permissão nova. Solicitar, regular e executar seguem a Regulação, e ler o plano segue o prontuário.
7. **Tela:**
   - **no item pendente do plano,** "Pela Regulação" abre a nova solicitação na tela Regulação, já com o paciente, a
     unidade do atendimento e o item. A gaveta avisa qual item é encaminhado;
   - "Encaminhar" (sem regulação) continua para o encaminhamento por fora;
   - **o item mostra** a regulação (item, situação, executante) e a contrarreferência.
8. **Migração V31:** `capacidade_exigida_id` em `tb_procedimento_regulado` e a carga das especialidades odontológicas.

## Consequências

- **A Regulação não autoriza a endodontia onde não há endodontia,** e o Modelo Operacional passa a valer na decisão.
- **O dentista acompanha o encaminhamento** no plano do paciente, da fila à contrarreferência, e retoma o item quando a
  regulação não sai.
- **A SB4b** (tratamento protético) usa o item "Prótese dentária" e a executante com a capacidade de prótese.
- **Fora do escopo:**
  - a escolha automática da executante pela capacidade e pela vaga (as cotas por unidade, PPI, são da Gestão da Rede);
  - o tratamento protético e o laboratório de prótese (SB4b).

## Testes

- **JUnit:**
  - **`RegulacaoOdontologicaControllerTest`:**
    - **o catálogo:**
      - a endodontia exige a capacidade Endodontia; a prótese é consulta especializada;
      - a capacidade inexistente dá 404; o item com a capacidade de periodontia é aceito;
    - **a executante:** autorizar na policlínica sem a capacidade (422) e no CEO com ela;
    - **o encaminhamento:**
      - o item de outro paciente não entra (422); o do paciente fica encaminhado, com o item regulado e a situação;
      - o item encaminhado não se encaminha de novo (422);
      - a negativa devolve o item a planejado, com o motivo;
      - de novo encaminhado, autorizado no CEO, agendado e realizado: o plano mostra a situação realizada, a
        executante e a contrarreferência.
  - **`SolicitacaoRegulacaoControllerTest`, `RegulacaoAutorizacaoControllerTest`, `RedeSaudeMentalControllerTest` e
    `PlanoTratamentoControllerTest`:** a Regulação e o plano continuam iguais sem a capacidade exigida.
  - **`MigracoesDoEsquemaTest`:** a migração aplicada.
- **Robot:**
  - **de API:** `RegulacaoOdontologica.robot` (CT-001 a CT-005):
    - o catálogo;
    - a executante sem a capacidade;
    - encaminhar o item;
    - a negativa que devolve o item;
    - o item já encaminhado.
  - **de interface:** `UI_saude_bucal.robot` CT-007: a pulpotomia do 46 encaminhada pela tela Regulação à endodontia
    do CEO, e o item encaminhado.
