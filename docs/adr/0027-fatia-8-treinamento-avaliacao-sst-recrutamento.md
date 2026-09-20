# 0027 — Fatia 8: Treinamento, Avaliação, SST, Recrutamento

## Status

Aceita e implementada.

## Contexto

Última fatia do roadmap de telas de RH, sem desenho prévio. O usuário pediu o levantamento exato
dos endpoints de 4 subdomínios (Treinamento, Avaliação, SST, Recrutamento) e devolveu uma
especificação funcional completa em cima deles — nenhuma tela inventada além do que os endpoints
já suportam.

## Decisão

### Regra central, para os 4 subdomínios: sem `PATCH`, sem edição na UI

Nenhum dos 4 subdomínios (`Treinamento`, `CicloAvaliacao`, `ExameOcupacional`,
`AcidenteTrabalho`, `Epi`, `Vaga`, `Candidato`) tem endpoint de atualização — só `POST` (criar) e
`GET` (listar). As telas desta fatia **não oferecem edição nem mudança de status** em nenhum
desses cadastros. Quando um `PATCH` existir no futuro, a ação entra — não antes. Isso evita a UI
prometer uma capacidade que o backend não sustenta.

### Catálogo separado de histórico (Treinamento e Avaliação)

Ambos já tinham a metade "histórico do profissional" pronta (abas do perfil, fatia 2b/2c). Esta
fatia completa só a metade "catálogo/gestão": `/rh/treinamentos` e `/rh/ciclos-avaliacao`, telas
de listagem + criação (modal), reaproveitando os serviços `TreinamentoService`/`AvaliacaoService`
já existentes (que ganharam `criar`/`criarCiclo`).

### Duas "situações" derivadas no frontend, não campos novos de backend

- **Status do ciclo de avaliação** (`Agendado`/`Em andamento`/`Encerrado`): função pura de
  `dataInicio`/`dataFim` vs. hoje. Não existe (nem deveria existir) um campo `status` em
  `CicloAvaliacao` que precisaria ser mantido sincronizado manualmente.
- **Situação do EPI** (`Em uso`/`Devolvido`): `dataDevolucao == null` → em uso; preenchida →
  devolvido. Mesmo raciocínio.

Em ambos os casos, decisão explícita do usuário: dado derivável de datas já existentes não vira
campo de banco.

### SST: uma aba no perfil, com 3 sub-abas internas

`ExameOcupacional`, `AcidenteTrabalho` e `Epi` viram uma única aba "SST" dentro de
`/profissionais/perfil` (não 3 abas de primeiro nível — ficaria denso demais na barra já com 10
abas), com um segundo nível de `.tabs` só dentro do conteúdo dela. Mesmo padrão de histórico +
form simples do resto do perfil. No form de Acidente de trabalho, o campo "CAT URL" só é
obrigatório quando o checkbox "CAT emitida" está marcado — validação condicional no formulário
reativo, pedida explicitamente.

### Recrutamento: área própria, fora do perfil

`Vaga` e `Candidato` não pertencem a um profissional específico — viram `/rh/vagas` (lista +
criar) e `/rh/vagas/:vagaId/candidatos` (detalhe da vaga + candidatos + registrar candidato),
mesmo padrão de rota aninhada já usado em Cargo → Tabela salarial (fatia 1).

## Trade-offs considerados

**Sem PATCH, sem edição (escolhida)**
- ✅ A UI nunca promete uma ação que o backend rejeitaria — coerência entre o que se vê e o que
  funciona.
- ❌ Um erro de digitação no cadastro de um treinamento, por exemplo, fica sem correção até o
  `PATCH` existir — aceito, é a mesma trade-off já aceita em outras fatias (ex.: Tabela salarial
  também não tem edição de valor histórico).

**Status derivado vs. campo de status gravado (escolhida)**
- ✅ Nunca fica desatualizado — não existe estado inconsistente entre "o que as datas dizem" e "o
  que o campo diz".
- ❌ Não permite um estado manual fora da lógica de datas (ex.: encerrar um ciclo antes da data
  fim) — não foi pedido, e adicionar isso htoje seria assumir uma necessidade não confirmada.

## Consequências

**Positivas**: fecha o roadmap de telas do módulo de RH (fatias 1-8) usando só o que os endpoints
já suportam hoje — nenhuma mudança de backend nesta fatia inteira.

**Negativas / pendências**: quando `PATCH` for adicionado a qualquer um desses 8 endpoints, as
telas correspondentes precisam de uma ação de editar — não é automático, é trabalho futuro.

## Referências

- [ADR-0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) — padrão de modal/CSS
  reaproveitado nas telas novas.
- [ADR-0020](./0020-tela-central-do-profissional.md) — padrão de aba do perfil, reaproveitado pra
  SST.
