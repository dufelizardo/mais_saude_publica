# 0124 — Vigilância epidemiológica: fundação (V1)

## Status

Aceita e implementada (fatia V1 da [ADR-0110](./0110-vigilancia-em-saude.md); detalhe em
[`vigilancia/MODELO-VIGILANCIA.md`](../vigilancia/MODELO-VIGILANCIA.md)).

## Contexto

- **A ADR-0110 desenhou a Vigilância em Saúde (#23)** como subdomínios sobre uma base compartilhada, começando pela
  epidemiológica: agravo como catálogo e ficha como dado, notificação, investigação, classificação, notificação
  negativa e a sugestão de notificação a partir da rede.
- **A SM3 deixou uma pendência** que só a vigilância resolve: a crise com autoagressão sugere a notificação de
  violência autoprovocada, e o profissional registrava que notificou fora do sistema ([ADR-0123](./0123-saude-mental-risco-crise.md)).
- **O sistema não reproduz o SINAN:** primeiro o modelo próprio; o adaptador vem na V5.

## Decisão

1. **Catálogo de agravos** (`Agravo`, migração V18): sem tabela por doença.
   - Código (fixo), nome, descrição, os CID-10 que cobre (prefixos: "A90" cobre A90 e A90.x), compulsório,
     periodicidade (`IMEDIATA`, até 24 horas, ou `SEMANAL`), prazo de encerramento em dias, critérios de
     classificação aceitos (`LABORATORIAL · CLINICO_EPIDEMIOLOGICO · CLINICO`), sensível e ativo.
   - **Carga inicial** de 22 agravos da Lista Nacional (Portaria de Consolidação GM/MS nº 4/2017, anexo V): dengue,
     chikungunya, zika, febre amarela, malária, tuberculose, hanseníase, sífilis (adquirida, em gestante e congênita),
     HIV/aids, hepatites virais, leptospirose, meningite, sarampo, coqueluche, SRAG hospitalizado, animal peçonhento,
     atendimento antirrábico, intoxicação exógena, acidente de trabalho grave e violência interpessoal e
     autoprovocada. Sensíveis: sífilis, HIV/aids e violência.
   - O município acrescenta agravos de interesse local com `VIGILANCIA.GERENCIAR` (código repetido: 409).
2. **Ficha específica como dado e versionada** (`FichaAgravo`): a definição dos campos em JSON (chave, rótulo, tipo
   `SIM_NAO · TEXTO · NUMERO · DATA · OPCAO · MULTIPLA`, obrigatório, opções). Mudar a ficha grava a versão seguinte; a
   notificação guarda as respostas junto com a versão e continua lida com a ficha dela. Fichas iniciais: dengue e
   violência.
3. **Notificação** (`Notificacao`):
   - Paciente, agravo (ativo), unidade e profissional notificantes, datas dos primeiros sintomas e da notificação (não
     no futuro; sintomas não depois da notificação), origem (`ATENDIMENTO · INTERNACAO · EXAME · SAUDE_MENTAL ·
     BUSCA_ATIVA · COMUNIDADE · OUTRA`) com a referência opcional (atendimento, internação, pedido de exame ou crise,
     do mesmo paciente; 422) e o local provável em texto (o ponto no mapa vem com o Território).
   - **Respostas conferidas contra a ficha vigente** (400): chaves conhecidas, obrigatórios preenchidos e tipos.
   - Situação `NOTIFICADA → EM_INVESTIGACAO → ENCERRADA` e o **prazo de encerramento** (notificação mais o prazo do
     agravo); vencido, "encerramento atrasado".
   - Outra notificação aberta do mesmo agravo para o paciente **só avisa**: pode ser caso novo; decide a vigilância.
   - **Ponte com a SM3:** notificar a partir da crise marca a crise como notificada (a pendência some); o registro
     manual da SM3 continua para a notificação feita em outro sistema.
4. **Investigação e classificação:**
   - `InvestigacaoEpidemiologica`, uma por notificação, atualizada até o encerramento: investigador, exposição e
     histórico, local provável de infecção, medidas e os pedidos de exame do paciente (de outro, 422). Abrir leva a
     notificação a "em investigação".
   - `ClassificacaoCaso`, como histórico: `SUSPEITO · PROVAVEL · CONFIRMADO · DESCARTADO · INCONCLUSIVO`, com critério
     (obrigatório em confirmado e descartado, 400; precisa ser aceito pelo agravo, 422), justificativa e responsável.
   - **Encerramento** pede a classificação final (a última precisa ser confirmado, descartado ou inconclusivo; 422) e
     a evolução (`CURA · OBITO_PELO_AGRAVO · OBITO_POR_OUTRA_CAUSA · IGNORADA`). Encerrada, não muda (422).
5. **Notificação negativa** (`NotificacaoNegativa`): a unidade declara que não houve caso na **semana
   epidemiológica** (domingo a sábado; a semana 1 é a que contém 4 de janeiro). Sem semana, a anterior. Uma por
   unidade e semana (409); semana em que a unidade notificou: 422; semana inexistente ou futura: 400. A tela mostra as
   oito últimas semanas sem notificação nem declaração das unidades que notificam.
6. **Sugestão de notificação, calculada:** internações e **pedidos de exame** dos últimos 30 dias com CID de agravo
   compulsório e as crises da SM3 com notificação pendente, quando o paciente não tem notificação do agravo em aberto
   nem feita depois da detecção; uma por paciente e agravo. A consulta não entra: o diagnóstico dela é texto livre,
   sem CID. O exame positivo como origem fica para depois (o catálogo de exames não liga exame a agravo).
7. **Permissões e sigilo:**
   - `VIGILANCIA.NOTIFICAR`: Médico, Enfermeiro, Coordenador de enfermagem, Técnico de enfermagem, Psicólogo e
     Assistente social. `VIGILANCIA.INVESTIGAR`: o **papel novo "Vigilância epidemiológica"** (com paciente,
     prontuário, notificar e gerenciar). `VIGILANCIA.GERENCIAR`: o papel da vigilância e o Gestor.
   - Notificação identificada é dado de saúde: leitura auditada. **Agravo sensível:** o detalhe e a linha na lista só
     para quem tem `VIGILANCIA.INVESTIGAR` na unidade da notificação e para quem notificou (403).
   - **Boletim** só com agregados por agravo (notificadas, em investigação, encerradas, confirmadas, descartadas).
8. **API** (`/api/v1/vigilancia/`): `agravos`, `agravo` (POST/PATCH), `agravo/{id}/ficha`; `notificacao`,
   `notificacoes` (filtros unidade, agravo, situação, atrasadas), `notificacao/{id}`, `/investigacao`,
   `/classificacao`, `/encerramento`; `notificacao-negativa`, `semanas`; `sugestoes`; `boletim`.
9. **Tela "Vigilância epidemiológica"** num grupo novo do menu: indicadores (abertas, em investigação, encerramento
   atrasado, semanas sem declaração); abas Notificações (com o boletim), Sugestões, Notificação negativa e Agravos;
   gaveta de notificação com o **formulário montado a partir da ficha**; detalhe com a ficha respondida, a
   investigação, as classificações e o encerramento; cadastro de agravo e editor da ficha. Na Saúde Mental, a crise
   ganha **"Notificar na Vigilância"**, que abre a notificação de violência já preenchida.

## Consequências

- **A notificação compulsória entra no sistema** com ficha, investigação e encerramento oportuno; a pendência da SM3
  vira notificação de verdade.
- **Destrava a V2** (contato, surto, medida de controle) e a **V5** (adaptador SINAN).
- **Fora do escopo:** contato e surto (V2); mapa dos casos (Território); sugestão por exame positivo; adaptador SINAN
  (V5); calendário oficial de semanas além da regra padrão.

## Testes

- **JUnit:**
  - `SemanaEpidemiologicaTest`: a semana 1 contém 4 de janeiro, a virada de ano e o ano de 53 semanas.
  - `VigilanciaControllerTest`: ficha versionada e validação das respostas (sem ficha com respostas, obrigatório,
    tipo, opção fora da lista, campo desconhecido), a notificação antiga lida na versão dela e o aviso de outra em
    aberto; o ciclo notificar, investigar, classificar (critério obrigatório e não aceito), encerrar (sem final, 422;
    encerrada, 422), o boletim e o encerramento atrasado; a notificação negativa (repetida 409, semana com caso 422,
    semana inexistente 400, semanas pendentes); as sugestões da internação e da crise e a ponte com a crise.
  - `VigilanciaAutorizacaoControllerTest`: o gestor não notifica; o agravo sensível só para a vigilância e quem
    notificou; investigar e o catálogo são da vigilância.
- **Robot de API** (`test/vigilancia/Vigilancia.robot`) e **de interface** (`test/ui/vigilancia/UI_vigilancia.robot`).
