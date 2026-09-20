# 0026 — Histórico funcional consolidado (fatia 7)

## Status

Aceita e implementada.

## Contexto

Última fatia de tela com desenho definido no roadmap. Já estava mapeada desde o planejamento
original como "sem endpoint novo — dá pra montar no frontend combinando os endpoints existentes".

## Decisão

### Nova aba no perfil, sem nenhuma chamada de API nova

"Histórico funcional" mescla os dados que **já estavam carregados** por outras abas do mesmo
componente (`profissional-perfil.ts`) — `lotacaoHistorico`, `afastamentos` (+ `licencas`, pra
anotar o tipo legal quando existir), `ajustes`, e `profissional.dataDesligamento` — num único
`computed()` que gera uma lista de eventos (`data`, `tipo`, `descrição`) ordenada por data
decrescente. Nenhum service novo, nenhum model novo, nenhuma chamada HTTP adicional: é puramente
uma projeção sobre sinais que a própria tela já mantém.

### Escopo dos eventos: exatamente o combinado no planejamento original

Lotação, Afastamento (com a Licença anotada quando existir), Ajuste individual, Desligamento —
não inclui Treinamentos/Avaliações/Ponto/Folha, que não faziam parte da definição original de
"histórico funcional" (essas continuam com suas próprias abas dedicadas).

## Trade-offs considerados

**`computed()` sobre sinais já carregados (escolhida)**
- ✅ Zero custo de rede adicional — os dados já estão na página quando o profissional é
  encontrado.
- ✅ Reativo por natureza: se qualquer aba disparar um recarregamento (ex.: registrar uma
  transferência), o histórico consolidado atualiza sozinho, sem código extra.
- ❌ Só funciona porque todos os dados-fonte já são carregados eagerly no `buscar()` — se algum
  dia uma dessas abas passar a ser lazy-loaded (carregada só ao clicar), o histórico precisaria
  disparar o carregamento também.

**Endpoint de backend dedicado que já devolve o histórico consolidado (rejeitada)**
- ✅ Um resultado só, sem duplicar consultas.
- ❌ Rejeitada: era exatamente o gap que o planejamento original já tinha descartado — os dados
  já vêm de 4 endpoints diferentes que a tela já consome; um quinto endpoint só pra remontar o que
  o frontend já tem em mãos seria trabalho redundante sem ganho real.

## Consequências

**Positivas**: fecha a última fatia de tela com desenho definido — restam só os módulos do roadmap
original (Treinamento, SST, Recrutamento, Avaliação) sem telas específicas pedidas ainda.

**Negativas / pendências**: nenhuma.

## Referências

- [ADR-0020](./0020-tela-central-do-profissional.md) — tela onde esta aba vive e de onde vêm os
  sinais reaproveitados.
