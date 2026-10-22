# 0121 — Saúde Mental: PTS, intervenções e rede de apoio (SM2)

## Status

Aceita e implementada (fatia SM2 da [ADR-0112](./0112-saude-mental.md), sobre o caso da
[ADR-0120](./0120-saude-mental-base.md)).

## Contexto

- **A SM1 abriu o caso em saúde mental** (acolhimento, avaliação, referência). Faltava o agregado central do domínio,
  o **Projeto Terapêutico Singular**, e o registro do cuidado que acontece: intervenções e rede de apoio.
- **O PTS é construído em equipe, com a pessoa e a família, e é revisado.** A ADR-0112 manda versionar: a revisão gera
  uma versão nova e a anterior não é sobrescrita.
- **O cuidado não se resume a consulta:** atendimento individual e familiar, grupo, oficina, atividade comunitária,
  visita, atendimento compartilhado e orientação.

## Decisão

1. **PTS versionado** (`ProjetoTerapeuticoSingular`, migração V15):
   - **Uma versão por linha** (v1, v2…), única por caso e número. Revisar marca a vigente como substituída
     (`substituidoEm`) e grava a seguinte; nada é sobrescrito.
   - Cada versão tem necessidades, periodicidade da revisão (a do catálogo de programas; trimestral por padrão), a
     data "revisar em" (por padrão, hoje mais a periodicidade; não pode ser no passado), a pactuação com a pessoa ou a
     família (com observação), quem elaborou e, na revisão, a **síntese obrigatória** do que mudou (400).
   - **`ObjetivoTerapeutico`:** descrição, meta, prazo, responsável e situação (`EM_ANDAMENTO · ALCANCADO ·
     NAO_ALCANCADO · SUSPENSO`). Pelo menos um objetivo (400).
   - **`IntervencaoPrevista`:** tipo, frequência (texto), responsável e serviço envolvido (texto até a SM4 e a SM6).
   - **Responsável:** opcional; quando informado, é o técnico ou membro vigente da equipe de referência (422).
   - **Os responsáveis do caso são a referência da SM1**, não duplicada no PTS.
   - **Revisão concorrente:** o pedido diz de qual versão parte (`versaoBase`, nula na primeira). Se a vigente mudou,
     409: uma revisão não apaga a outra.
2. **Intervenções realizadas** (`IntervencaoSaudeMental`):
   - **Tipo** de um catálogo como dado (`TipoIntervencaoSaudeMental`), semeado na V15 com os oito tipos do modelo.
     Intervenção em crise entra com a SM3. O cadastro de tipos pela tela fica para depois.
   - Data e hora (não no futuro, nem antes do início do caso; 400), o profissional (o usuário), o profissional
     compartilhado (qualquer profissional ativo: matriciamento atravessa equipes), a intervenção prevista do PTS que
     ela cumpre (do mesmo caso; 422), a situação (`REALIZADA · NAO_COMPARECEU`) e o **resumo** obrigatório.
   - **Só inclusão.** Da psicoterapia entra só que aconteceu, com resumo (ADR-0112, seção 13).
   - **Faltas seguidas:** o caso conta as faltas mais recentes em sequência e, com duas ou mais num caso ativo, a tela
     sugere marcar faltoso no programa. A marcação continua pela P2: sugerir não muda a situação.
3. **Rede de apoio** (`PessoaRedeApoio`): ligada ao caso, não à versão do PTS, porque muda por conta própria. Nome,
   papel (`FAMILIAR · CUIDADOR · PESSOA_DE_REFERENCIA · SERVICO · OUTRO`), vínculo, telefone e observação, com início e
   fim: sair da rede encerra com motivo e fica no histórico (já fora: 422).
4. **Sigilo da SM1, sem permissão nova.**
   - Escrever pede `SAUDE_MENTAL.ACOMPANHAR` na unidade e o conteúdo do caso liberado (403).
   - `GET pts` pede vínculo e o conteúdo liberado (403); leitura auditada.
   - No detalhe do caso, quem não tem o conteúdo liberado vê só o número da versão vigente do PTS, sem conteúdo; as
     listas de PTS, intervenções e rede vão vazias.
   - Registrar algo da SM2 no caso conta como autoria, para o sigilo (como na SM1).
5. **API** (`/api/v1/saude-mental/`):
   - `GET tipos-intervencao`;
   - `GET` e `POST acompanhamento/{id}/pts`;
   - `POST acompanhamento/{id}/intervencao`;
   - `POST acompanhamento/{id}/rede-apoio` e `POST acompanhamento/{id}/rede-apoio/{pessoa}/encerramento`;
   - **o detalhe do caso** traz as versões do PTS (vigente primeiro), as intervenções, a rede de apoio e as faltas
     seguidas;
   - **a lista de casos** traz a versão e a revisão do PTS, os números "Sem PTS" e "PTS a revisar" e o filtro
     `pendencia` (`SEM_AVALIACAO`, `SEM_PTS`, `PTS_A_REVISAR`).
6. **Tela (no caso, sem tela nova):**
   - **Aba PTS:** a versão vigente com necessidades, revisão, pactuação, objetivos (situação em tag) e intervenções
     previstas; **Elaborar PTS** ou **Revisar PTS** (pré-preenchida com a vigente, pede a síntese); versões
     anteriores, que expandem.
   - **Aba Intervenções:** a lista e **Registrar intervenção** (também no cabeçalho do caso).
   - **Aba Rede de apoio:** quem está na rede, **Incluir pessoa**, **Tirar da rede** com motivo e o histórico.
   - **Lista:** coluna PTS (versão, "A revisar", "Sem PTS"), indicador "Sem PTS / a revisar" e o filtro por pendência.
   - **Resumo:** o aviso de faltas seguidas.

## Consequências

- **O núcleo PTS + acompanhamento existe.** A crise (SM3) e a rede de atenção (SM4) se penduram no caso; a SM6 parte
  dos serviços que hoje são texto no PTS.
- **Grupos e oficinas são registrados por pessoa.** A atividade coletiva com vários participantes vem da P4 (ações
  programáticas) e não ganha motor próprio aqui.
- **Fora do escopo:** intervenção em crise e risco (SM3); grupo ligado à atividade coletiva (P4); serviços e
  instituições como catálogo (SM4, SM6); retificação de intervenção; cadastro de tipos pela tela.

## Testes

- **JUnit:**
  - `ProjetoTerapeuticoControllerTest`: versões sem sobrescrever e revisão concorrente (409); revisão sem síntese
    (400); regras (sem objetivo, responsável fora da referência, revisão no passado, tipo inexistente); intervenções
    com compartilhado, futuro (400), prevista de outro caso (422) e faltas seguidas; rede de apoio como histórico;
    indicadores e filtro por pendência.
  - `SaudeMentalAutorizacaoControllerTest`: o colega com vínculo e fora da referência não elabora, não registra e não
    lê o PTS, e vê só a versão no detalhe; o gestor não lê o catálogo.
- **Robot de API** (`test/saude_mental/SaudeMentalPts.robot`): 7 casos.
- **Robot de interface** (`UI_saude_mental.robot`): mais 4 casos — elaborar o PTS, revisar, registrar intervenção e
  rede de apoio.
- **Jackson 3:** campo `boolean` primitivo ausente do JSON derruba o parse. Por isso `pactuadoComPessoa` é `Boolean`
  no pedido, com nulo valendo "não pactuado".
