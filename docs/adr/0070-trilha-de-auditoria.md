# 0070 — Trilha de auditoria: registro

## Status

Aceita e implementada (fatia 1 de 2: registro). A consulta (tela Auditoria, aba "Quem acessou" no
atendimento e permissão `AUDITORIA.CONSULTAR`) é a fatia 2. Implementa o item 6 da
[ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md).

## Contexto

Com login, perfis e escopo prontos (ADRs 0055, 0066 a 0069), o sistema sabe quem pode fazer o quê. Ainda
não sabe dizer **quem fez**. Cada registro clínico e cada lançamento de estoque guarda quem digitou
(`registradoPorCpf`), mas cada um do seu jeito, e nada registra **leitura**. Para um sistema público
com dado de saúde, a LGPD e o sigilo profissional exigem responder:
- quem abriu o prontuário deste paciente, e quando?
- o que esta pessoa acessou ou alterou neste período?
- quem tentou acessar o que não podia?

A [ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md) deixou o prontuário visível na rede
inteira, por continuidade do cuidado, e registrou a auditoria de leitura como a contrapartida dessa
decisão.

## Decisão

1. **`EventoAuditoria`** (`TB_EVENTO_AUDITORIA`), um evento por ação, com os campos:
   - quando (`ocorridoEm`);
   - quem (`usuarioCpf`);
   - a ação: `LOGIN`, `TROCA_DE_SENHA`, `LEITURA`, `CRIACAO`, `ALTERACAO`, `RETIFICACAO`, `REVOGACAO`
     ou `EXCLUSAO`;
   - o resultado (`PERMITIDO` ou `NEGADO`);
   - o recurso da API;
   - o método e a rota *como padrão* (ex.: `/api/v1/triagem/{uuid}/retificacao`);
   - o status HTTP;
   - o **registro**, o **paciente** e a **unidade** afetados;
   - o IP de origem;
   - o motivo da recusa, quando houver.

   Índices por paciente, por usuário e por registro, que são as três perguntas da consulta.
2. **Imutável.** A entidade é `@Immutable`, e o repositório estende `Repository`, não `JpaRepository`:
   só `save` e buscas, sem `delete`. A trilha só cresce.
3. **Nunca guarda conteúdo.** Nem corpo da requisição, nem parâmetros, nem senha, nem texto clínico.
   O evento aponta para o registro. O registro é imutável e tem histórico de retificação (ADR-0062),
   então o "o quê" já está lá. Guardar o conteúdo duplicaria dado sensível numa segunda tabela.
4. **O que entra** (um `HandlerInterceptor`, depois que a resposta está decidida):
   - **alterações bem-sucedidas** (POST, PATCH, PUT, DELETE), em todas as rotas da API;
   - **leituras de dado de saúde ou pessoal:** só as rotas marcadas com `@AuditarLeitura`, que são o
     detalhe de:
     - prontuário;
     - atendimento;
     - triagem, evolução, consulta, procedimento e medicação;
     - paciente (por id, CPF ou cartão SUS);
     - dispensação.

     **Listagens não entram.** A pergunta é "quem abriu", e cada tela carregada viraria ruído;
   - **tudo o que foi recusado com 403**, inclusive listagens. Tentativa de acesso conta;
   - **login, aceito ou recusado**, com o CPF tentado, e **troca de senha**.

   Erros de validação (400, 404, 409, 422) ficam de fora: não são ação sobre dado nem tentativa de
   acesso.
5. **Enriquecimento pelo que o serviço já sabe** (`ContextoAuditoria`, nos atributos da requisição):
   - o `ControleDeAcesso` anota a **unidade** toda vez que confere o escopo (ADR-0067), mesmo com a
     exigência de permissão desligada;
   - os serviços clínicos, o de atendimento, o de dispensação e o de paciente anotam o **paciente** e o
     **id do registro** criado;
   - o login anota o CPF tentado;
   - o 403 anota o motivo;
   - sem anotação, o registro é o id que está na rota.
6. **A auditoria não derruba a operação.** O evento é gravado depois que a requisição terminou. Uma falha
   ao gravar fica no log de erro e não muda a resposta.
7. **Registra sempre, com ou sem os toggles.** Com o login desligado, o `usuarioCpf` fica nulo, mas a
   ação continua registrada.

## Trade-offs considerados

**Interceptor + contexto dos serviços (escolhida)** × **trigger no banco / Envers (versionamento
por entidade)**
- ✅ Registra **leitura** e **recusa**, que um trigger ou o Envers não veem. Sabe quem fez e em qual
  unidade, no mesmo lugar onde a permissão é conferida. Não duplica conteúdo clínico.
- ❌ Não guarda o "antes e depois" de cada campo. Nos registros clínicos e no estoque isso já existe,
  porque são imutáveis ou têm livro próprio (ADRs 0057, 0062). Nos cadastros (RH, administrativo), o
  evento diz quem alterou e quando, mas não o valor anterior. Guardar o antes e depois só se justifica
  com um requisito concreto.

**Leitura só do detalhe (escolhida)** × **toda leitura**
- ✅ A trilha responde "quem abriu o prontuário de fulano" sem se afogar em eventos de listagem.
- ❌ Uma listagem mostra nome e alguns dados de vários pacientes sem gerar evento. É o mesmo corte
  usado pelos prontuários eletrônicos em geral: o acesso ao detalhe é o que se audita.

## Consequências

**Positivas:**
- A LGPD e o sigilo têm base: quem abriu, quem alterou, quem tentou e de onde.
- A decisão de deixar o prontuário na rede inteira (ADR-0067) ganha a contrapartida prometida.
- As recusas de acesso ficam visíveis, e dão para detectar perfil mal configurado ou tentativa indevida.

**Negativas / pendências:**
- **Fatia 2:** consulta, com tela, filtros e a aba "Quem acessou", e a permissão `AUDITORIA.CONSULTAR`,
  que nem o administrador da plataforma recebe por padrão.
- Política de retenção e arquivamento da trilha (volume).
- Antes e depois dos cadastros, só se surgir requisito.

## Referências

- [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md): item 6, auditoria.
- [ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md): o `ControleDeAcesso` e a decisão do prontuário na rede.
- [ADR-0062](./0062-registros-clinicos-imutaveis-com-retificacao.md): registros clínicos imutáveis (o "o quê" da trilha).
