# 0075 — Cabeçalho do perfil do profissional no estilo do cartão

## Status

Aceita e implementada. Última de três entregas da padronização das telas de RH, depois da
[ADR-0072](./0072-tela-profissionais-a-partir-do-prototipo.md) (Profissionais) e da
[ADR-0073](./0073-catalogos-de-rh-em-telas-com-abas.md) (catálogos).

## Contexto

O perfil do profissional é a tela mais antiga do RH: 13 abas e 6 modais. O plano previa mexer só no
visual e deixá-lo por último.

O cabeçalho mostrava apenas nome, matrícula, CPF e "Ativo/Desligado", com um ícone genérico. Já o cartão
da tela Profissionais mostra categoria, situação (férias, afastado, sem lotação), cargo, unidade e jornada.
Quem abria o perfil pelo cartão perdia essa informação.

Acima do cabeçalho havia um card de busca por CPF no estilo antigo (`form-group`), que ocupava a tela
mesmo quando o profissional já vinha do cartão.

## Decisão

1. **O cabeçalho repete o cartão**, com as mesmas funções de `shared/profissional-categoria.ts`:
   - avatar com as iniciais, na cor da categoria, e o ponto de situação;
   - nome, e cargo · unidade da lotação vigente;
   - etiquetas de categoria e de situação;
   - linha de identificação: matrícula, CPF, conselho, jornada semanal, primeiro telefone e admissão.
2. **Situação com a regra do quadro.** O afastamento vigente é o aprovado ou em andamento que cobre hoje;
   se houver mais de um, vale o que termina por último. O motivo de saúde não aparece no cabeçalho, só
   "Afastado até", como no cartão (ADR-0072). Enquanto a lotação carrega, nenhuma situação é mostrada,
   para não exibir por engano "Sem lotação vigente".
3. **A busca por CPF vai para o cabeçalho da página**, como campo compacto ("outro profissional pelo
   CPF"), ao lado de um link de volta para Profissionais. Sem profissional aberto, a tela mostra um estado
   vazio que leva a Profissionais.
4. **As 13 abas e os modais não mudam.** Nenhuma regra, endpoint ou campo foi alterado.
5. `iniciaisDoNome` passou para `shared/profissional-categoria.ts`, e `situacaoDoProfissional` aceita a
   lotação e o afastamento do perfil além do item do quadro.

## Trade-offs considerados

**Só o cabeçalho (escolhida)** × **refazer as 13 abas em gavetas**
- ✅ É a parte vista toda vez e a que destoava do cartão. O custo e o risco são pequenos.
- ❌ As abas continuam com os modais antigos (`app-modal`). Refazer as abas é trabalho próprio e fica como
  pendência.

## Consequências

**Positivas:**
- Do cartão para o perfil, a pessoa vê a mesma identificação e a mesma situação.
- A padronização do RH (ADRs 0072, 0073 e 0075) está concluída no que foi planejado.

**Negativas / pendências:**
- As abas do perfil seguem na primeira geração (PADRAO-TELAS-INTERNAS §7).
- A tela não foi conferida visualmente no navegador por quem implementou. A conferência fica com o revisor.

## Referências

- [ADR-0072](./0072-tela-profissionais-a-partir-do-prototipo.md): o cartão, a categoria e a situação.
- [ADR-0073](./0073-catalogos-de-rh-em-telas-com-abas.md): catálogos de RH.
