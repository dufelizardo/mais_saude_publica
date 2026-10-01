# 0065 — Profissional preenchido a partir do login

## Status

Aceita e implementada. Primeiro passo da frente de identidade; papéis e escopo por unidade
(ADR-0054) vêm a seguir.

## Contexto

Toda gaveta de registro da Farmácia e dos Atendimentos pede a **matrícula do profissional** —
dispensação, perda/ajuste, transferência, triagem, consulta, procedimento, evolução, checagem de
medicação. Com o login ligado (ADR-0055), o sistema já sabe o CPF de quem está usando a tela, e o
profissional tem o mesmo CPF: digitar a matrícula é trabalho repetido e abre espaço para registrar em
nome de outra pessoa por engano.

A ADR-0055 já tinha definido o vínculo entre `Usuario` e `Profissional` como **vínculo fraco por CPF**
(`ProfissionalRepository.findByCpfAndAtivoTrue`), sem FK — uma pessoa real tem uma identidade de login,
e o histórico funcional pode ter mais de um registro (ADR-0017).

## Decisão

1. **`GET /api/v1/auth/eu`** informa quem está logado: CPF e nome do usuário e, quando existe, o
   **profissional ativo de mesmo CPF** (id, matrícula, nome). Sem usuário autenticado — sem token, ou
   com o toggle de segurança desligado, em que toda requisição é anônima — responde **401**.
2. **As telas preenchem a matrícula** dos registros novos com a do profissional logado. O campo
   continua editável: na prática, um profissional às vezes lança o que outro fez (ex.: registro feito
   depois, por outra pessoa da equipe), e a auditoria já guarda **quem digitou** em `registradoPorCpf`
   (ADRs 0057, 0062). Em retificação, o campo vem com o profissional do registro original.
3. **A barra superior mostra quem está logado** (nome do profissional e matrícula, ou o nome do
   usuário quando não há vínculo de profissional).
4. **Sem login, nada muda**: com o toggle desligado (local, CI e hoje os ambientes além do dev), a
   rota responde 401, as telas não preenchem nada e a barra não mostra usuário.

## Trade-offs considerados

**Preencher e deixar editável (escolhida)** × **forçar a matrícula do usuário logado no backend**
- ✅ Não trava o lançamento feito por outra pessoa da equipe, e o autor real da digitação já fica
  registrado.
- ❌ Ainda é possível registrar em nome de outro profissional. Restringir isso (por exemplo, só
  permitir a própria matrícula, salvo papel de supervisão) é decisão de autorização — entra com os
  papéis e o escopo da ADR-0054, próximo passo desta frente.

**Rota em `/auth/eu` (escolhida)** × **`/usuario/eu`**
- ✅ Fica ao lado de login e status, na parte pública do filtro de segurança; a própria rota decide
  o 401 a partir do token, igual nos dois modos do toggle.

## Consequências

**Positivas:** menos digitação nas gavetas mais usadas; menos risco de registrar com a matrícula
errada; quem está logado fica visível.

**Negativas / pendências:**
- Usuário sem vínculo de profissional (ex.: administrador da plataforma) continua digitando a
  matrícula.
- A regra de quem pode registrar em nome de quem fica para o RBAC (ADR-0054).

## Referências

- [ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md) — login, toggle e vínculo por CPF.
- [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md) — papéis, permissões e escopo (próximo passo).
- [ADR-0062](./0062-registros-clinicos-imutaveis-com-retificacao.md) — `registradoPorCpf` nos registros clínicos.
