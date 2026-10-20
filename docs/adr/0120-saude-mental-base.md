# 0120 — Saúde Mental: base (SM1)

## Status

Aceita e implementada (fatia SM1 da [ADR-0112](./0112-saude-mental.md), sobre o acompanhamento programático da
[ADR-0119](./0119-programas-acompanhamento.md)).

## Contexto

- **A ADR-0112 desenhou a Saúde Mental (#26)** e deixou a SM1 como base: acolhimento, avaliação, o caso ligado ao
  programa Saúde Mental e a equipe e o técnico de referência.
- **A SM1 dependia de "atendimento com especialidade" (SB1), que não existe.** A dependência era para registrar o
  acolhimento e a avaliação dentro de um atendimento da especialidade. Esperar a SB1 travaria a Saúde Mental sem
  ganho: o acolhimento acontece também fora de um atendimento aberto (porta aberta do CAPS, escuta na UBS).
- **O programa Saúde Mental (`SAUDE_MENTAL`, sensível) e o `AcompanhamentoProgramatico` já existem** (ADRs 0118 e
  0119). A ADR-0112 manda reaproveitar, não duplicar.
- **Faltava o papel de acesso para quem faz saúde mental.** As funções Psicólogo e Assistente social já existiam nas
  equipes (ADR-0103), mas não havia papel padrão para elas: sem isso, o CAPS não funciona.

## Decisão

1. **A SM1 não depende da SB1.**
   - O acolhimento e a avaliação têm profissional, unidade e data próprios.
   - O acolhimento pode apontar, opcionalmente, para um `Atendimento` que já existe (do mesmo paciente).
   - A especialidade entra quando a SB1 vier; nada aqui precisa mudar para isso.
2. **Entidades** (migração V14, só inclusão onde é registro clínico):
   - **`AcolhimentoSaudeMental`:** paciente, unidade, profissional (o do usuário logado), atendimento opcional, motivo
     da procura, necessidade imediata, vulnerabilidade, **risco inicial** (`SEM_RISCO_APARENTE · BAIXO · MODERADO ·
     ALTO`) e **desfecho** (`ACOMPANHAMENTO_NA_UNIDADE · CUIDADO_COMPARTILHADO · ENCAMINHAMENTO · CRISE ·
     ORIENTACAO_SEM_SEGUIMENTO`).
     - O risco é a foto do acolhimento; o histórico de risco é da SM3. Com risco alto, a resposta e a tela avisam
       que a condução da crise chega na SM3.
     - O encaminhamento pede o destino, em texto livre até a SM4 (Regulação).
   - **`AcompanhamentoSaudeMental`:** o caso longitudinal. Paciente, unidade, o acompanhamento programático, início,
     fim e motivo de saída. Um caso vigente por paciente (409).
   - **`ReferenciaSaudeMental`:** equipe e técnico de referência, **como histórico** com início e fim. Trocar fecha a
     anterior e grava o motivo. A equipe é da unidade do caso e está ativa; o técnico é membro vigente dela (422).
     Isso não mexe na lotação do RH.
   - **`AvaliacaoSaudeMental`:** contexto e história e necessidades (obrigatórios), aspectos psicossociais, fatores
     de risco e de proteção, e **CID-10 opcional** no formato do modelo clínico geral, sem catálogo próprio.
3. **Programas sem duplicação.**
   - Abrir o caso **reaproveita** o acompanhamento vigente do programa Saúde Mental na mesma unidade, ou **inscreve**
     pelo serviço da P2 (a unidade precisa oferecer o programa, 422). Vigente em outra unidade: 422.
   - **A situação (ativo, faltoso) não é duplicada:** vem do acompanhamento programático, e as contagens do catálogo
     continuam valendo. Marcar faltoso segue pela P2.
   - **Encerrar o caso fecha a referência e encerra o programático** com o mesmo motivo. Enquanto houver caso
     aberto, o programático de Saúde Mental não sai pela P2 (422, "registre a saída pela tela de Saúde Mental").
4. **Acolhimento com seguimento.** Com desfecho que acompanha: se o paciente já tem caso vigente, o acolhimento entra
   nele (com aviso); senão, o caso abre na mesma transação e a equipe e o técnico são obrigatórios (400). Abrir o
   caso pede `SAUDE_MENTAL.ACOMPANHAR` na unidade, além do `SAUDE_MENTAL.ACOLHER`.
5. **Permissões e papéis** (catálogo de acesso, acrescentadas aos papéis padrão existentes na subida):
   - `SAUDE_MENTAL.ACOLHER` e `SAUDE_MENTAL.ACOMPANHAR`, as duas na dimensão de acesso ao dado de saúde.
   - Médico, Enfermeiro e Coordenador de enfermagem recebem as duas.
   - **Papéis novos Psicólogo e Assistente social:** paciente, prontuário, `PROGRAMA.ACOMPANHAR` e as duas de saúde
     mental.
   - O Gestor não recebe nenhuma.
6. **Sigilo pela equipe de referência** (LGPD, art. 11, grau mais alto):
   - **Ler o caso pede vínculo assistencial** (ADR-0076). **Ser o técnico ou membro da equipe de referência de um caso
     aberto passa a contar como vínculo** (regra 6 do `VinculoAssistencialService`): o CAPS acompanha sem atendimento
     recente.
   - **O conteúdo clínico** (acolhimentos e avaliações) vai só para a referência vigente, para quem registrou algo
     no caso, para o acesso justificado e para a permissão ampla (`PRONTUARIO.CONSULTAR_SEM_VINCULO`). Os demais com
     vínculo veem que o caso existe e quem é a referência, para saber a quem procurar (`conteudoLiberado: false`).
   - **Escrever no caso** (avaliação, troca de referência, saída) pede o mesmo conteúdo liberado (403).
   - Toda leitura é auditada (`@AuditarLeitura`), com a base do acesso e se o conteúdo foi liberado.
   - A lista de casos não traz conteúdo clínico e segue o escopo de `SAUDE_MENTAL.ACOMPANHAR`, como os inscritos de
     programa sensível na P2.
   - O conteúdo de psicoterapia fica fora: a SM2 registra só que a intervenção aconteceu, com resumo.
   - Com a autorização desligada, tudo passa, como no resto do sistema (ADR-0067).
7. **API** (`/api/v1/saude-mental/`):
   - `POST acolhimento`;
   - `POST acompanhamento` (abre o caso sem acolhimento novo);
   - `GET acompanhamentos` (filtros `unidadeId`, `pacienteId`, `escopo` = `TECNICO` ou `EQUIPE`, `situacao`);
   - `GET acompanhamento/{id}`;
   - `POST acompanhamento/{id}/referencia`, `POST acompanhamento/{id}/avaliacao`, `POST acompanhamento/{id}/encerramento`;
   - `GET paciente/{id}/acolhimentos`.
8. **Tela `/assistencia/saude-mental`** (menu Assistência, só para quem acolhe ou acompanha):
   - indicadores (em acompanhamento, faltosos, sem avaliação), filtros "Todos do meu acesso", "Sou o técnico" e
     "Da minha equipe", e a unidade;
   - a lista de casos e o detalhe com as abas Resumo, Acolhimentos, Avaliações e Referência (histórico);
   - gavetas Novo acolhimento, Nova avaliação, Trocar referência e Encerrar;
   - sem conteúdo liberado, o detalhe mostra a referência e oferece o "Acessar com justificativa" do prontuário.
   - **Na aba Programas de Pacientes,** o programa Saúde Mental troca a "Saída" por "Abrir em Saúde Mental". Equipes e
     Pacientes não ganham aba nova, por sigilo.

## Consequências

- **A base das próximas fatias existe:** o PTS e as intervenções (SM2), o risco com histórico e a crise (SM3) e a
  rede (SM4) se penduram no caso.
- **O vínculo pela equipe de referência vale para o prontuário inteiro**, não só para a Saúde Mental: quem é
  referência do caso lê o prontuário do paciente, como a equipe da internação na ADR-0100.
- **A dependência da SB1 sai do MODELO-SAUDE-MENTAL.** A SB1 continua necessária para a Saúde Bucal.
- **Fora do escopo:** PTS, intervenções e rede de apoio (SM2); risco com histórico e crise (SM3); referência pela
  Regulação e matriciamento (SM4); modalidades de CAPS e RAPS (SM5); intersetorialidade e indicadores (SM6).

## Testes

- **JUnit:**
  - `SaudeMentalControllerTest`:
    - o acolhimento abre o caso no programa e o seguinte entra nele (com aviso de risco alto);
    - abrir reaproveita o programático já vigente;
    - regras: sem referência (400), encaminhamento sem destino (400), técnico fora da equipe e equipe de outra
      unidade (422), unidade que não oferece o programa (422), inexistente (404);
    - a referência como histórico, a avaliação com CID-10 e o encerramento que fecha o programa (e a P2 recusando
      antes).
  - `SaudeMentalAutorizacaoControllerTest`, com login, autorização e vínculo ligados: o gestor não abre; o técnico
    de referência lê tudo pelo vínculo da referência; o colega com vínculo por atendimento só vê o resumo e não
    registra, até o acesso justificado; o médico de outra unidade não tem vínculo.
  - A suíte inteira passou localmente: 597 de 597, num banco limpo.
- **Robot de API** (`test/saude_mental`): 9 casos.
- **Robot de interface** (`UI_saude_mental.robot`): 4 casos — acolher abrindo o caso, nova avaliação, trocar
  referência e encerrar. As suítes de Programas e Pacientes passaram junto (39 de 39 no total).
