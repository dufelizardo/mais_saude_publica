# 0173 — Comunicação e Educação em Saúde: educação permanente (C4)

## Status

Aceita e implementada. Fatia C4 do domínio 28, desenhada na [ADR-0114](./0114-comunicacao-e-educacao-em-saude.md) e na seção 12 de
[`comunicacao/MODELO-COMUNICACAO.md`](../comunicacao/MODELO-COMUNICACAO.md).

## Contexto

- **A capacitação já existe no RH:** `Treinamento` (nome, carga horária, validade, obrigatório) e `ParticipacaoTreinamento` (profissional,
  conclusão, validade, certificado), com a tela Desenvolvimento ([ADR-0073](./0073-catalogos-de-rh-em-telas-com-abas.md)).
- **O conteúdo educativo já existe na C1** ([ADR-0168](./0168-comunicacao-basica.md)), com revisão técnica, versões e validade.
- **Faltava a ligação** e dizer o que cada treinamento desenvolve. "Competência" só aparecia como texto livre na Necessidade de pessoal.
- **Referência:** a Política Nacional de Educação Permanente em Saúde (Portaria GM/MS nº 198/2004, hoje no Anexo XL da Portaria de
  Consolidação nº 2/2017): a capacitação parte dos problemas do trabalho, e o registro de quem fez o quê continua no RH.

## Decisão

1. **Catálogo de competências** (`TB_COMPETENCIA`): código, nome, descrição e ativa, mantido com `RH.GERENCIAR`. A carga traz sete:
   acolhimento e classificação de risco, biossegurança e controle de infecção, suporte básico de vida, vigilância e notificação de agravos,
   imunização e sala de vacina, trabalho em equipe e comunicação, gestão do cuidado e linhas de cuidado. Código repetido: 409.
2. **Competências do treinamento** (`TB_TREINAMENTO_COMPETENCIA`). Competência inativa não entra em treinamento novo (422), mas continua
   onde já estava.
3. **Material didático** (`TB_TREINAMENTO_CONTEUDO`): conteúdos educativos ligados pelo **grupo de versões**. O treinamento mostra sempre a
   versão revisada mais recente.
   - **Só entra conteúdo que passou pela revisão técnica** (aprovado ou publicado). Rascunho e em revisão: 422.
   - **Vencido continua ligado e aparece marcado,** para quem cuida do treinamento atualizar.
4. **Competências do profissional:** calculadas a partir das participações concluídas, sem registro paralelo. Com mais de um treinamento
   para a mesma competência, vale a participação em dia de validade mais longa; a vencida só aparece se não houver outra.
5. **Sem permissão nova:** `RH.GERENCIAR` mantém o catálogo e as ligações; `RH.CONSULTAR` consulta. O uso dos conteúdos aparece também para
   quem escreve, revisa ou publica conteúdos.
6. **Não se cria** uma `Capacitacao` paralela nem uma plataforma de ensino a distância, como já previa a ADR-0114.

### API

- `GET, POST /api/v1/educacao-permanente/competencia`, `PATCH /competencia/{uuid}`;
- `GET /api/v1/educacao-permanente/treinamento`, `PUT /treinamento/{uuid}` (`competenciaIds`, `conteudoGrupoIds`; substitui as ligações);
- `GET /api/v1/educacao-permanente/conteudo` (os disponíveis) e `/uso-dos-conteudos`;
- `GET /api/v1/educacao-permanente/profissional/{matricula}/competencias`.

### Telas

- **Desenvolvimento:** aba Competências; no Catálogo de treinamentos, a coluna "Competências e material" e a gaveta Competências e conteúdos.
- **Perfil do profissional,** aba Treinamentos: o bloco Competências desenvolvidas.
- **Comunicação,** aba Conteúdos educativos: em quais treinamentos o conteúdo é usado.

### Migração

`V58__educacao_permanente.sql`: as três tabelas e a carga das competências.

## Consequências

- **A Necessidade de pessoal continua com o texto livre de competências.** Trocar pelo catálogo é parte da próxima etapa.
- **Ficam para depois:** competências exigidas por cargo, com o painel de quem não as tem; arquivos de material didático, que dependem da
  D2 de Documentos; trilhas de formação, inscrição e ensino a distância.

## Testes

- **JUnit:**
  - `EducacaoPermanenteControllerTest`: a carga, o código repetido e a edição; a versão em vigor do conteúdo; o conteúdo sem revisão; o
    vencido marcado; a competência inativa; as competências do profissional pelas participações, com a vencida substituída pela em dia;
  - `EducacaoPermanenteAutorizacaoControllerTest`, com a segurança ligada: sem login, o médico sem RH e o gestor.
- **Robot:** `EducacaoPermanente.robot` (API, cinco casos) e os casos CT-003 e CT-004 de `UI_catalogos_edicao.robot`.
