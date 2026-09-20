# 0021 — Fluxo de transferência de lotação embutido no perfil, sem tela própria

## Status

Aceita e implementada.

## Contexto

Fatia 3 do roadmap (`docs/rh/ESCOPO-RH.md` seção 6): "Lotações (fluxo de transferência)", já
identificada desde o planejamento original como sem lacuna de backend — `POST /api/v1/lotacao/`
já cobre admissão, transferência e mudança de cargo (fecha a vigente, abre uma nova).

## Decisão

Não criar uma tela nova (ex.: `/rh/lotacoes`). Uma `Lotacao` só existe em função de um
`Profissional` específico — não há um "catálogo de lotações" que faça sentido navegar
independentemente. Em vez disso, o formulário de "Registrar transferência / mudança de cargo" foi
adicionado dentro da aba **Lotação** de `/profissionais/perfil` (fatia 2, ADR-0020), logo abaixo do
histórico já exibido ali — mesmo padrão de "ver histórico + registrar novo" já usado em Tabela
salarial (fatia 1) e nas demais abas do perfil (Ajustes, Treinamentos, Avaliações).

Campos do formulário: Unidade (select, reaproveita `UnidadeSaudeService` já usado no cadastro),
Cargo (select, reaproveita `CargoService`), data de início, jornada semanal opcional, e motivo —
um `<select>` com as 3 opções que fazem sentido neste contexto (Transferência, Promoção, Mudança de
cargo); "Admissão" fica de fora porque só se aplica no cadastro inicial (ADR-0019).

## Trade-offs considerados

**Embutir na aba Lotação do perfil (escolhida)**
- ✅ Reaproveita o contexto do profissional já carregado (matrícula, histórico) sem precisar de
  outra busca por CPF.
- ✅ Consistente com o padrão já estabelecido em toda a fatia 2 (view + form simples na mesma
  aba).
- ❌ Não existe uma rota direta "vou transferir alguém" sem antes buscar o profissional — aceitável,
  mesma limitação já presente em todo o resto do app (sem listagem de Profissionais ainda).

**Tela dedicada de transferência (rejeitada)**
- ✅ Rota própria, poderia ser linkada de fora.
- ❌ Rejeitada: seria uma segunda busca por CPF duplicando a que já existe no perfil, só pra
  reaproveitar o mesmo formulário — sem ganho real, mais uma tela pra manter.

## Consequências

**Positivas**: fecha a fatia 3 sem nenhuma mudança de backend nem componente novo — só formulário
sobre serviços já existentes.

**Negativas / pendências**: nenhuma nova.

## Referências

- [ADR-0020](./0020-tela-central-do-profissional.md) — aba Lotação onde este formulário vive.
- [ADR-0019](./0019-vincular-cargo-e-lotacao-no-cadastro-de-profissional.md) — outro consumidor de
  `LotacaoService.criar`, motivo "Admissão".
