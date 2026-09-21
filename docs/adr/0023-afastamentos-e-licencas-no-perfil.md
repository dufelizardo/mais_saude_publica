# 0023 — Afastamentos e Licenças no perfil (fatia 5)

## Status

Aceita e implementada.

## Contexto

Fatia 5 do roadmap. Lacuna já identificada desde a primeira análise de gaps (fatia 1):
`LicencaRepository` só tinha `findByAfastamento_Uuid` — pra listar todas as licenças de um
profissional seria preciso percorrer cada afastamento dele, um por um. `Afastamento` em si nunca
teve essa lacuna (`findByProfissional_MatriculaOrderByDataInicioDesc` já existia desde a Fase 1).

## Decisão

### Backend: uma query derivada resolve a lacuna

`LicencaRepository.findByAfastamento_Profissional_MatriculaOrderByAfastamento_DataInicioDesc` —
Spring Data JPA navega a relação `Licenca.afastamento.profissional.matricula` direto pelo nome do
método, sem `@Query` manual. Novo `GET /api/v1/licenca/profissional/{matricula}` em
`LicencaController`, mesmo padrão dos demais endpoints de listagem por profissional.

### Frontend: uma aba só pras duas entidades, não duas abas separadas

"Afastamentos e Licenças" no perfil do profissional (ADR-0020) — as duas tabelas + os dois forms
de registro vivem na mesma aba, porque Licença é conceitualmente um subtipo de Afastamento (1:1,
ver `docs/rh/MODELO-RH.md` seção 5), não uma entidade independente que mereça navegação própria.

### Seletor de afastamento no form de Licença: só ofertar os sem licença ainda

Mesmo truque já usado em Regras de anuênio (fatia 1): computar no frontend
`afastamentos().filter(a => não está em licencas().map(l => l.afastamentoId))` e popular o
`<select>` só com esses — evita o usuário bater no 409 que o backend já retorna (`Licenca` é 1:1
com `Afastamento`) só depois de preencher o formulário inteiro.

## Trade-offs considerados

**Query derivada por nome de método (escolhida)**
- ✅ Consistente com todo o resto do repositório — nenhuma outra entidade do módulo usa `@Query`
  manual até aqui.
- ❌ Nome de método longo (`findByAfastamento_Profissional_MatriculaOrderByAfastamento_DataInicioDesc`)
  — aceitável, mesmo estilo já usado em `TabelaSalarialRepository`.

**Duas abas separadas, Afastamentos e Licenças (rejeitada)**
- ✅ Mais "limpo" à primeira vista.
- ❌ Rejeitada: fragmentaria uma relação 1:1 conceitualmente única em duas navegações, sem ganho —
  o usuário sempre pensa "o afastamento X tem uma licença?", não "me mostra licenças" isolado.

## Consequências

**Positivas**: fecha a fatia 5 sem nenhuma lacuna restante nas fases 1 e 3 do backend.

**Negativas / pendências**: nenhuma nova.

## Referências

- [MODELO-RH.md](../rh/MODELO-RH.md) seção 3 (Afastamento) e seção 5 (Licença).
- [ADR-0020](./0020-tela-central-do-profissional.md) — tela onde esta aba vive.
