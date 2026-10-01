# 0076 — Prontuário por vínculo assistencial e acesso justificado

## Status

Aceita e implementada, atrás do toggle `app.security.prontuario-por-vinculo.enabled`, que está desligado
em todos os ambientes. Resolve a pendência que a [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md)
deixou aberta e que o documento `docs/pm/sistema_de_acesso.md` reservou "para a próxima etapa": o acesso a
dado clínico ser só por papel e escopo, ou também por contexto.

## Contexto

A [ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md) deixou o **prontuário aberto à rede
inteira**. Com isso, quem tem `PRONTUARIO.CONSULTAR` em qualquer unidade lê o histórico completo de
qualquer paciente. O motivo foi a continuidade do cuidado: o médico da UPA precisa ver a triagem feita na
UBS. A contrapartida foi a auditoria de leitura ([ADR-0070](./0070-trilha-de-auditoria.md) e
[ADR-0071](./0071-consulta-da-trilha-de-auditoria.md)), que registra o acesso depois que ele aconteceu.

Esse alcance é grande. Uma farmacêutica de uma UBS consegue abrir o prontuário de um paciente que nunca
passou por lá. Os registros avulsos (triagem, consulta etc.) já estavam restritos à unidade, mas o
prontuário agregado, que é o histórico inteiro, não tinha restrição nenhuma.

A prática dos prontuários eletrônicos é exigir um **vínculo de cuidado**. Sem vínculo, o acesso é uma
**"quebra de vidro"**: permitido, mas declarado, com prazo e revisado depois.

## Decisão

1. **Abrir o prontuário (`GET /prontuario/{paciente}`) exige vínculo assistencial**, além de
   `PRONTUARIO.CONSULTAR`. Há vínculo quando vale pelo menos uma destas condições:
   1. **Atendimento na minha unidade:** o paciente tem atendimento em aberto (agendado ou em andamento), ou
      com data nos últimos 30 dias, numa unidade coberta pelo meu `PRONTUARIO.CONSULTAR`.
   2. **Sou o profissional dele:**
      - sou o profissional (mesmo CPF do login) de um atendimento dele nos últimos 30 dias; ou
      - sou o profissional de um agendamento dele não cancelado, entre 30 dias atrás e 30 dias à frente.

      O agendamento não tem unidade, e isso cobre o médico que vai receber o paciente.
   3. **Acesso justificado:** um registro ainda válido, feito por mim, para este paciente.
2. **Sem vínculo, a API responde 403 com `details: VINCULO_ASSISTENCIAL_AUSENTE`**, e a tela oferece
   **"Acessar com justificativa"**:
   - A pessoa informa o motivo (emergência, continuidade do cuidado, regulação ou encaminhamento, outro) e
     um texto de pelo menos 20 caracteres (`POST /prontuario/{paciente}/acesso-justificado`).
   - O acesso vale por **4 horas**, só para esse paciente e essa pessoa.
   - A trilha de auditoria recebe a ação própria `ACESSO_JUSTIFICADO`, com o motivo e a validade, e fica
     filtrável na tela Auditoria.
   - As leituras seguintes entram como leitura normal, com o detalhe "Acesso justificado (motivo) id".
3. **O texto da justificativa fica em `AcessoJustificado`, e não na trilha.** A trilha não guarda
   conteúdo (ADR-0070), e a justificativa pode citar dado de saúde. O evento de auditoria aponta para o
   registro pelo `registroId`. O registro é imutável.
4. **`PRONTUARIO.CONSULTAR_SEM_VINCULO` dispensa o vínculo.** Ela serve para auditoria clínica e
   regulação. Assim como `AUDITORIA.CONSULTAR`, **não entra em nenhum papel padrão**, e as leituras feitas
   com ela ficam auditadas com o detalhe "Sem vínculo, por PRONTUARIO.CONSULTAR_SEM_VINCULO".
5. **A resposta do prontuário diz em que se baseou a abertura** (`acesso.base`): `VINCULO`,
   `JUSTIFICADO` (com `expiraEm`), `PERMISSAO_AMPLA` ou `LIVRE` (regra desligada). A tela mostra
   "Acesso justificado até 14:30 — registrado na auditoria".
6. **O escopo da regra é só o prontuário completo.** Continua como estava:
   - os registros da própria unidade, restritos à unidade pela ADR-0067;
   - o cadastro do paciente, porque a recepção precisa encontrá-lo.
7. **Toggle próprio**, desligado por padrão, que só age com a autorização ligada. Primeiro liga-se a
   exigência de permissão, depois o vínculo. Janela, validade e mínimo do texto são configuráveis:
   `app.security.prontuario-por-vinculo.janela-dias`, `...acesso-justificado-horas` e
   `...justificativa-minimo`.

## Trade-offs considerados

**Quebra de vidro (escolhida)** × **bloqueio duro sem vínculo**
- ✅ Na emergência, ninguém fica sem o histórico, e o acesso fica declarado e revisável.
- ❌ Depende de a supervisão revisar os acessos justificados. Por enquanto a revisão é feita pelo filtro
  na tela Auditoria, sem alerta ativo.

**Só o prontuário agregado (escolhida)** × **vínculo também em cada registro avulso**
- ✅ O prontuário é o que dá o histórico da rede. Os registros avulsos já estão presos à unidade.
- ❌ Um registro antigo da própria unidade continua legível pelo endpoint dele, sem vínculo. Isso é
  aceitável: é dado da unidade de quem lê.

**Janela de 30 dias** × **vínculo permanente depois de um atendimento**
- ✅ O acesso acompanha o cuidado em curso e o retorno próximo.
- ❌ Um retorno depois de 30 dias sem agendamento exige justificativa ou um atendimento aberto. O valor é
  configurável.

## Consequências

**Positivas:**
- O prontuário deixa de ser legível pela rede inteira sem motivo.
- A continuidade do cuidado segue garantida: vínculo por unidade, por profissional e quebra de vidro.
- A pendência aberta desde a ADR-0054 fica resolvida.

**Negativas / pendências:**
- **Encaminhamento e regulação ainda não geram vínculo**, porque esses domínios não existem. Quando
  existirem, entram como uma quarta condição.
- Não há alerta ativo de acesso justificado para a supervisão. A revisão é pelo filtro da Auditoria.
- Para ligar num ambiente, siga o [guia](../acesso/GUIA-LIGAR-AUTORIZACAO.md), seção do vínculo.
- A tela não foi conferida visualmente no navegador por quem implementou.

## Referências

- [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md): a pendência resolvida aqui.
- [ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md): o prontuário na rede, refinado por esta ADR.
- [ADR-0070](./0070-trilha-de-auditoria.md) e [ADR-0071](./0071-consulta-da-trilha-de-auditoria.md): a trilha sem conteúdo e a consulta.
- `docs/pm/sistema_de_acesso.md`, seções 21 e "O desenho que eu colocaria no projeto agora".
