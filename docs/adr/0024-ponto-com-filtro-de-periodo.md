# 0024 — Ponto com filtro de período (fatia 6a)

## Status

Aceita e implementada. **Atualizada** — a decisão de "aba só consulta" (seção "Aba Ponto no
perfil") foi revista; ver seção "Atualização: registro manual + correção sujeita a aprovação" no
fim deste documento.

## Contexto

Fatia 2 (ADR-0020) deixou "Ponto" deliberadamente fora: `GET .../profissional/{matricula}`
devolvia tudo, sem filtro, e uma lista crua de todos os registros de ponto de um profissional
(potencialmente anos de entradas/saídas) não agrega valor real. A decisão registrada foi esperar
a fatia 6, que resolve exatamente isso.

## Decisão

### Filtro por período, opcional, no mesmo endpoint

`GET /api/v1/registro-ponto/profissional/{matricula}?dataInicio=...&dataFim=...` — query params
opcionais, `@DateTimeFormat(iso = DATE)`. Sem os dois, comportamento idêntico ao de antes (lista
tudo) — não quebra nenhum uso existente. Com os dois, filtra `dataHora` entre
`dataInicio.atStartOfDay()` e `dataFim.atTime(LocalTime.MAX)` (inclusive nas duas pontas).

### Aba "Ponto" no perfil, agora que o filtro existe

Segue o mesmo padrão de aba do perfil (ADR-0020) — form de período (De/Até) + tabela. Sem form de
registro: lançar ponto manualmente por um formulário web não é o fluxo real (é tipicamente
automatizado — relógio de ponto, app, biometria), e não foi pedido; a aba é só de consulta.

## Trade-offs considerados

**Mesmo endpoint com filtro opcional (escolhida)**
- ✅ Sem endpoint novo, sem duplicar lógica de busca.
- ✅ 100% retrocompatível — nenhum outro consumidor do endpoint (nenhum existe ainda além desta
  tela) precisa mudar.

**Endpoint separado só pra período (rejeitada)**
- ✅ Assinatura mais "limpa" (sempre exige os dois parâmetros).
- ❌ Rejeitada: duplicaria a query, sem ganho real — o padrão de parâmetro opcional já é usado em
  outros pontos do projeto (ex.: `dataDesligamento` no `des-habilitar`, ADR-0017).

## Consequências

**Positivas**: fecha a primeira metade da fatia 6; a lista de ponto agora é utilizável (filtrada
por competência/período, não uma parede de registros).

**Negativas / pendências**: nenhum agrupamento "dia trabalhado" (entrada+saída consolidadas numa
linha) — mostra os registros crus, um por linha. Se isso for necessário, é uma evolução futura da
mesma aba, não um novo endpoint.

## Atualização: registro manual + correção sujeita a aprovação

Revisão feita durante o levantamento de endpoints sem uso do módulo RH inteiro: `POST
/registro-ponto/` já existia no backend, mas nunca foi exposto na UI (por causa da decisão acima).
O usuário pediu para reverter isso parcialmente:

- **`POST /registro-ponto/` ganha UI, com fluxo limpo, sem aprovação** — a aba "Ponto" passa a ter
  um form "Registrar ponto" (Tipo + Data/hora). Continua sendo o mesmo endpoint de sempre, sem
  mudança de contrato.
- **Corrigir um registro já existente é diferente e passa por aprovação do gestor.** `RegistroPonto`
  ganha 3 campos novos, todos opcionais: `dataHoraProposta`, `tipoProposto`,
  `justificativaCorrecao`. "Correção pendente" é **derivado** (`dataHoraProposta != null`), sem
  campo de status novo — mesmo padrão já usado em EPI/Ciclo de avaliação/Adesão de benefício.
  Três endpoints de ação explícita, mesmo padrão de `encerrar`/`des-habilitar`:
  `PATCH .../{uuid}/solicitar-correcao`, `.../aprovar-correcao`, `.../rejeitar-correcao`. Aprovar
  aplica a proposta em `dataHora`/`tipo` e limpa os 3 campos; rejeitar só limpa, mantendo o
  original. Cada operação recusa (409) se o estado não bate (já tem pendência ao solicitar; não
  tem pendência ao aprovar/rejeitar).
- **"Gestor"**, aqui, é o `responsavel` da `UnidadeDeSaude` do profissional — mas o sistema não tem
  autenticação/login, então isso não é imposto por identidade. Aprovar/rejeitar são ações que
  qualquer um que acessa a tela pode executar, representando conceitualmente a decisão do gestor —
  mesmo nível de confiança já aceito em todas as outras mutações deste sistema (nenhuma tem
  controle de acesso hoje).
- **Sem entidade nova, sem histórico de correções passadas.** Decisão explícita do usuário: campos
  embutidos em `RegistroPonto` (não uma tabela `CorrecaoRegistroPonto` separada). Depois de
  aprovada ou rejeitada, a proposta é limpa — não fica registro de correções antigas. Trade-off
  aceito conscientemente (perde auditoria de pedidos passados) em troca de simplicidade — só um
  pedido de correção pendente por vez por registro.

## Referências

- [ADR-0020](./0020-tela-central-do-profissional.md) — decisão original de deixar Ponto de fora
  até ter filtro.
