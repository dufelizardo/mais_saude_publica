# 0020 — Tela central do Profissional (fatia 2)

## Status

Aceita e implementada.

## Contexto

Segunda fatia do frontend de RH (`docs/rh/ESCOPO-RH.md` seção 6), depois da fatia 1 (cadastros
estruturais). O objetivo era construir um hub de leitura reunindo tudo que já se sabe sobre um
profissional — dados, lotação, ajustes salariais, treinamentos, avaliações, desligamento — sem
depender de nenhuma lacuna de backend.

Antes de planejar, corrigi uma imprecisão nos meus próprios docs: a tabela de fatias em
`ESCOPO-RH.md` listava "Ajustes individuais" tanto na fatia 2 quanto no título da fatia 5,
como se dependesse do mesmo gap de backend de Licenças (falta "listar licenças por profissional").
Conferido direto nos controllers: `AjusteIndividual`, `Lotacao`, `ParticipacaoTreinamento`,
`Avaliacao` e `CalculoRescisao` **já tinham** `GET .../profissional/{matricula}` completo desde
suas respectivas fases — só "Licença" (e por extensão, uma tela dedicada de Afastamentos) dependia
do gap. A fatia 5 foi reescrita pra não incluir mais Ajustes individuais.

## Decisão

### Sem tela de listagem de Profissionais — busca por CPF

Não existe uma tela "Profissionais" (listagem) ainda. Em vez de construir uma só pra isso, a tela
de perfil (`/profissionais/perfil`) reaproveita o mesmo padrão de busca por CPF já usado em
`profissional-desligar.ts`. Quando uma listagem existir de verdade, ela pode linkar direto pra cá.

### Padrão visual: `.detail-card`/`.detail-head`/`.tabs`, conferido em `Equipamentos.html`

Portado de `modelo_front/Equipamentos.html` (a tela de detalhe de uma unidade de saúde), conferido
direto no arquivo (não de memória, aprendizado das correções da ADR-0018): cabeçalho com ícone +
nome + chips de status, abas com sublinhado azul no item ativo. Nenhuma cor/tamanho inventado —
todos os valores (`0.9375rem`, `52px`, etc.) vieram do CSS do mockup.

### Sem componente de "timeline" novo

Uma nota antiga em `ESCOPO-RH.md` dizia que um componente de histórico cronológico precisaria ser
desenhado do zero. Revisto: o padrão `table` dentro de `.card`, já construído e testado na fatia 1
(histórico de Tabela salarial), resolve todo histórico desta tela igual de bem — Lotação, Ajustes,
Treinamentos, Avaliações e Cálculos de rescisão são todos listas cronológicas simples, sem
necessidade de um widget visual mais complexo. Não inventar um componente sem precedente no mockup
quando o padrão já existente resolve.

### Cinco abas, cada uma com endpoint de backend já completo

| Aba | Sub-fatia | Conteúdo | Registro? |
|---|---|---|---|
| Dados | 2a | Contato, endereço, vínculo (`GET /profissional/{cpf}`) | Não — edição de contato é outro fluxo já existente |
| Lotação | 2a | Vigente + histórico (`GET /lotacao/profissional/{matricula}[/atual]`) | Não — transferir é a fatia 3 |
| Ajustes individuais | 2b | Histórico (`GET /ajuste-individual/profissional/{matricula}`) | Sim — form simples (mesmo padrão da Tabela salarial) |
| Treinamentos | 2b | Participações (`GET /participacao-treinamento/profissional/{matricula}`) | Sim — select do catálogo `GET /treinamento/` + datas |
| Avaliações | 2c | Histórico (`GET /avaliacao/profissional/{matricula}`) | Sim — select do catálogo `GET /ciclo-avaliacao/` + nota/avaliador |
| Desligamento/Rescisão | 2c | Status (`Profissional.ativo`/`dataDesligamento`, já na resposta) + histórico de `CalculoRescisao` | Sim, só quando `!ativo` e ainda não há cálculo registrado |

**Ponto ficou de fora desta fatia**, mesmo tendo endpoint pronto — decisão já registrada no
roadmap original: sem filtro por período/competência, uma lista crua não agrega valor real; a
fatia 6 constrói a tela própria.

### Desligamento/Rescisão: form condicional, não sempre visível

O form de registrar `CalculoRescisao` só aparece quando o profissional está desligado **e** ainda
não existe nenhum cálculo registrado — evita sugerir "registrar rescisão" pra alguém ativo, e evita
sugerir um segundo registro quando já existe um (o desligamento em si continua sendo feito pela
tela já existente `/profissionais/desligar`, não duplicado aqui).

## Trade-offs considerados

**3 sub-fatias em vez de uma PR só (escolhida)**
- ✅ Mesma disciplina usada nas 3 sub-fatias da Fase 0 do backend — PRs menores, revisáveis,
  cada uma com seu próprio CI verde antes de avançar.
- ❌ Uma ADR só ao final (em vez de uma por sub-fatia) significa que 2a e 2b ficaram sem
  documentação formal individual até 2c fechar — aceito porque as 3 sub-fatias são a mesma
  decisão de design, só fatiada por tamanho de PR.

**Buscar por CPF em vez de esperar uma tela de listagem (escolhida)**
- ✅ Não bloqueia esta fatia numa dependência (listagem de Profissionais) que nem estava no
  roadmap ainda.
- ❌ Sem uma lista, não há como "navegar" até um profissional sem já saber o CPF — aceitável pelo
  mesmo motivo que `/profissionais/desligar` já aceita essa limitação.

## Consequências

**Positivas**
- Um hub único pra consultar (e, em 3 das 5 abas, registrar) tudo que já existe sobre um
  profissional, sem esperar nenhuma fatia de backend adicional.
- Nenhum componente novo de UI foi necessário além do padrão de detalhe já portado — reforça que
  o design system de `modelo_front/` é suficiente pro resto do roadmap de telas de RH.

**Negativas / pendências**
- Sem listagem de Profissionais, a navegação até esta tela depende de já ter o CPF em mãos.
- A aba Desligamento/Rescisão registra o cálculo, mas o **cálculo em si** continua manual (mesma
  decisão de `docs/rh/MODELO-RH.md`: campos de registro, não cálculo automático).

## Referências

- [MODELO-RH.md](../rh/MODELO-RH.md) — entidades consumidas nesta tela.
- [ADR-0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) — padrão de app shell,
  precedente de "conferir o mockup no arquivo, não de memória".
- `modelo_front/Equipamentos.html` — fonte do padrão `.detail-card`/`.tabs`.
