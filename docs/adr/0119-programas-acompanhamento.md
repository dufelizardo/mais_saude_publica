# 0119 — Programas de saúde: acompanhamento programático (P2)

## Status

Aceita e implementada (fatia P2 da [ADR-0109](./0109-programas-acoes-e-linhas-de-cuidado.md), sobre o catálogo da
[ADR-0118](./0118-programas-catalogo.md)).

## Contexto

- **Com o catálogo (P1), falta o paciente no programa.**
  - Pacientes, Programas de Saúde e Equipes mostravam "Em breve" para programas vinculados, inscritos, faltosos e o
    filtro por programa.
- **É a primeira fatia do domínio com dado de saúde.** Estar no HiperDia revela hipertensão; estar em Saúde Mental
  revela diagnóstico psiquiátrico (LGPD, art. 11).
- **A ADR-0109 já decidiu como tratar esse dado:**
  - a leitura segue o prontuário: permissão clínica, vínculo assistencial da
    [ADR-0076](./0076-prontuario-por-vinculo-assistencial.md) e auditoria de leitura;
  - programa sensível restringe mais;
  - agregados não identificam.

## Decisão

1. **`AcompanhamentoProgramatico`:**
   - campos: paciente, programa, unidade responsável, equipe (opcional, da unidade), profissional responsável
     (opcional, com lotação vigente na unidade), início, fim, situação, motivo e descrição da saída;
   - **histórico**: voltar ao programa abre um registro novo;
   - cada mudança de situação vira um **`EventoAcompanhamento`**, só de inclusão, com situação anterior e nova,
     motivo, data e quem registrou.
   - **Situação:** ativo, faltoso, concluído (saída por alta) ou encerrado (transferência, mudança de município,
     abandono, óbito, outro).
2. **Regras:**

   | Situação | Resposta |
   |---|---|
   | Mais de um acompanhamento vigente por paciente e programa | 409 |
   | Unidade responsável que não oferece o programa (sem participação vigente da P1); equipe de outra unidade; profissional sem lotação na unidade | 422 |
   | Programa suspenso ou encerrado; paciente inativo | 422 |
   | Início no futuro; saída fora do intervalo entre o início e hoje; motivo ou descrição vazios | 400 |
   | Marcar a mesma situação; mexer em acompanhamento encerrado | 422 |

   - **Fora do público-alvo é aviso, não bloqueio** (`avisoPublico`, por exemplo "15 anos; o programa é para 18 anos
     ou mais"). Quem decide é o profissional. Gestante e condições não são conferidas: o cadastro do paciente não tem
     esses dados.
3. **Faltoso e atrasado:**
   - **faltoso** é marcado e desmarcado pelo profissional, com motivo;
   - **atrasado** é sugestão calculada: o paciente está sem atendimento realizado na unidade responsável há mais
     tempo que a periodicidade do programa (mensal = 30 dias, trimestral = 90…), contando do início quando nunca
     houve atendimento;
   - **a sugestão não muda a situação.** Ligar as ações e consultas ao programa é da P4.
4. **Acesso:**
   - **Inscrever, mudar a situação e registrar a saída:** permissão nova `PROGRAMA.ACOMPANHAR` (natureza dado de
     saúde), no escopo da unidade responsável.
     - Entra nos papéis padrão Médico, Enfermeiro e Coordenador de enfermagem pela rotina do catálogo de acesso
       (ADR-0087).
     - A Recepção não recebe.
   - **Programas de um paciente** (`GET /paciente/{id}/programas`):
     - permissão: `PRONTUARIO.CONSULTAR` ou `PROGRAMA.ACOMPANHAR`;
     - mais o **vínculo assistencial** da ADR-0076: sem vínculo, 403 `VINCULO_ASSISTENCIAL_AUSENTE`, e o acesso
       justificado do prontuário também libera os programas;
     - leitura auditada (`@AuditarLeitura`); a resposta traz a base do acesso.
   - **Programa sensível:** só quem tem `PROGRAMA.ACOMPANHAR` na unidade responsável vê o acompanhamento. Os outros
     recebem só a contagem em `restritos`.
   - **Inscritos de um programa:** `PROGRAMA.ACOMPANHAR`, filtrado pelas unidades do escopo, com leitura auditada.
   - **Números agregados** (inscritos, faltosos e atrasados por programa; pacientes em algum programa na rede) não
     identificam ninguém.
     - Vão no catálogo para quem está logado.
     - O resumo para a tela de Pacientes pede `PACIENTE.CONSULTAR`, `PRONTUARIO.CONSULTAR` ou `PROGRAMA.ACOMPANHAR`.
5. **API:**
   - `POST /api/v1/programa/{id}/acompanhamento`;
   - `GET /api/v1/programa/{id}/acompanhamentos?unidadeId&situacao`;
   - `POST /api/v1/acompanhamento/{id}/situacao` e `POST /api/v1/acompanhamento/{id}/encerramento`;
   - `GET /api/v1/paciente/{id}/programas`;
   - `GET /api/v1/acompanhamento/resumo`;
   - **no catálogo (`GET /programa`):**
     - os campos `inscritos`, `faltosos` e `atrasados`;
     - o filtro `equipeId`;
     - com unidade ou equipe no filtro, a contagem considera só os acompanhamentos dela.
6. **Migração V13:** as duas tabelas, com as restrições dos enums. A permissão nasce na subida.
7. **Telas:**
   - **Programas de Saúde:**
     - os cartões e os indicadores mostram pacientes, faltosos e atrasados;
     - o detalhe mostra os indicadores Inscritos, Faltosos e Atrasados; o controle clínico segue "Em breve" (P6);
     - **Inscrever paciente:** gaveta com busca por nome, CPF ou cartão SUS e unidade só entre as que oferecem o
       programa; o aviso de público aparece no retorno;
     - **Pacientes acompanhados:** a lista do escopo, com filtro Faltosos/Atrasados e as ações Marcar faltoso,
       Voltar a ativo e Saída.
   - **Pacientes:**
     - o indicador "Em programas de saúde";
     - o filtro por programa, para quem acompanha;
     - "Programas vinculados" no resumo e a aba Programas, com vigentes, histórico e restritos;
     - o bloqueio por vínculo usa o mesmo "Acessar com justificativa" do prontuário.
     - **A inscrição e as ações ficam na tela Programas de Saúde**, para onde a aba leva. Assim não há duas gavetas
       iguais em duas telas.
   - **Equipes:** "Programas vinculados", com os programas em que a equipe participa e os inscritos e faltosos dela.

## Consequências

- **O acompanhamento ganha base para as próximas fatias:**
  - a Saúde Mental (SM1) abre o acompanhamento psicossocial sobre ele;
  - as ações programáticas (P4) ligam consultas e busca ativa;
  - os indicadores (P6) partem dos números de inscritos e faltosos.
- **O "atrasado" usa o atendimento da unidade, não do programa.** Até a P4, uma consulta de outro motivo na mesma
  unidade conta como contato.
- **Fora do escopo:**
  - **lista de elegíveis** (idosos sem acompanhamento, gestantes fora do pré-natal). O paciente não tem unidade nem
    território, então a lista seria a base inteira da rede. Fica para depois da adscrição (Território F3);
  - ações programáticas e o programa na Agenda (P4);
  - indicadores clínicos (P6).

## Testes

- **JUnit:**
  - `AcompanhamentoControllerTest`:
    - inscrição com as regras e o aviso de público;
    - faltoso, saída e reinscrição como histórico;
    - atrasado pela periodicidade;
    - contagens no catálogo e o resumo;
    - inexistentes.
  - `AcompanhamentoAutorizacaoControllerTest`, com login, autorização e vínculo ligados:
    - o enfermeiro da unidade inscreve; o gestor e o enfermeiro de outra unidade não;
    - os inscritos respeitam o escopo;
    - ler os programas do paciente pede vínculo, e o acesso justificado libera;
    - o programa sensível de outra unidade fica em `restritos`.
  - A suíte inteira passou localmente, salvo o `SetorControllerTest`, que tropeça em resto de dado do banco local
    (resolvido num banco limpo, como na ADR-0118).
- **Robot de API** (`test/programas/acompanhamento`): 7 casos.
- **Robot de interface** (`UI_programas.robot`): mais 3 casos — inscrever pela tela, marcar faltoso e a aba Programas
  em Pacientes.
