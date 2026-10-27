# 0146 — Indicadores e BI: indicadores da rede (BI3)

## Status

Aceita e implementada (fatia BI3 da [ADR-0142](./0142-indicadores-bi-e-gestao.md); soma-se à BI1
([ADR-0143](./0143-bi-producao-assistencial.md)) e à BI2 ([ADR-0144](./0144-bi-situacao-operacional.md)) na Sala de
Situação).

## Contexto

- **Os indicadores de cada programa** (ADR-0141) só aparecem no detalhe do programa, um programa por vez. A gestão
  precisa ver todos juntos, comparar as unidades e saber quais estão abaixo da meta.
- **A vigilância** tem o boletim por agravo (ADR-0124), sem período, sem semana epidemiológica e sem território.
  Faltavam os indicadores clássicos da vigilância: a oportunidade de encerramento e a regularidade da notificação.
- **O protótipo `Painel.html`** tem o cartão "Programas de saúde" com os pacientes acompanhados de cada programa.

## Decisão

1. **Sem regra nova.** Os programas usam a apuração da P6: `IndicadorProgramaticoService.apurarParaRede` é a mesma
   apuração do detalhe do programa. A vigilância usa os registros de notificação e de notificação negativa. A BI3 só
   consolida.
2. **Programas na rede** (os ativos, no período):
   - **o que mostra:** inscritos ativos no fim do período, novos e saídas no período, faltosos;
   - **acompanhados em dia:** na rede inteira, a apuração da rede; nas unidades do acesso, a soma dos numeradores e dos
     denominadores de cada unidade, que é a mesma conta, porque cada inscrição é de uma unidade;
   - **metas:** quantas estão atingidas, em risco, abaixo e sem dados. Na rede, contam as metas da rede; por unidade,
     a meta da unidade e, sem ela, a da rede.
3. **Programa por unidade:** cada unidade do acesso que oferece o programa, com os indicadores da P6, o alvo e a
   situação. A meta da unidade vale primeiro e, sem ela, vale a da rede. As unidades com mais metas abaixo vêm
   primeiro, depois as em risco.
4. **Vigilância na rede** (pela data da notificação, no período):
   - **por agravo:** notificadas, confirmadas, descartadas, em aberto e encerradas;
   - **oportunidade de encerramento:** encerradas até o prazo do agravo, sobre as encerradas;
   - **as últimas 12 semanas epidemiológicas;**
   - **regularidade da notificação, na última semana fechada:**
     - as unidades que notificaram ou declararam a negativa, sobre as unidades **assistenciais ativas** do escopo;
     - esferas e regionais ficam fora, porque organizam a rede mas não atendem;
   - **território:** território, área e microárea do caso; sem microárea, aparece como "Sem território".
5. **Dado sensível:**
   - **programa sensível (Saúde Mental):** só com `PROGRAMA.ACOMPANHAR`, como na P6. Para os demais, fica fora da
     lista, com a contagem de quantos foram ocultados; a lista por unidade dá 403.
   - **agravo sensível:** com `VIGILANCIA.INVESTIGAR`, aparece com o nome. Para os demais, entra só nos totais, nas
     semanas e na regularidade, sem o nome e sem o território, com a contagem das ocultadas.
   - `ControleDeAcesso.temAlguma` confere a permissão sem lançar exceção, para esconder em vez de recusar.
6. **Acesso:** `INDICADOR.CONSULTAR` com o escopo da unidade (`EscopoDosIndicadores`). A exportação em CSV entra na
   trilha de auditoria.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `indicadores-rede/programas?de&ate&unidadeId` | `INDICADOR.CONSULTAR` (escopo) |
| GET | `indicadores-rede/programa/{id}/unidades?de&ate` | idem; programa sensível pede `PROGRAMA.ACOMPANHAR` |
| GET | `indicadores-rede/vigilancia?de&ate&unidadeId` | idem; agravo sensível com nome pede `VIGILANCIA.INVESTIGAR` |
| GET | `indicadores-rede/exportacao?de&ate&unidadeId` | idem; auditada |

### Tela

Na Sala de Situação, dois cartões novos:
- **Programas de saúde:**
  - cada programa com acompanhados, unidades, faltosos, % em dia, os novos no período e as etiquetas das metas;
  - ao clicar, abre a tabela por unidade, com o valor de cada indicador e a meta colorida pela situação.
- **Vigilância:**
  - notificadas e em aberto, oportunidade de encerramento e regularidade da semana;
  - o gráfico das 12 semanas;
  - os agravos mais notificados;
  - o território.

O bloco "Em breve" fica só com a vacinação (domínio 24). As peças de painel (cartão, grade, barras e variação) foram
para o `styles.css` global, para servir a qualquer tela de indicadores.

## Consequências

- **Na rede inteira, a apuração é uma por programa. Nas unidades do acesso, é uma por programa e por unidade.** É o
  bastante para a escala municipal; se pesar, entra a materialização prevista na ADR-0142.
- **A regularidade conta quem notificou ou declarou a negativa.** A unidade que não cumpriu nenhum dos dois na semana
  aparece pela diferença.

## Testes

- **JUnit:**
  - `IndicadoresDaRedeControllerTest`:
    - o programa na unidade igual à apuração da P6;
    - a meta da unidade antes da meta da rede, e as abaixo da meta primeiro;
    - a vigilância: oportunidade, regularidade com a negativa, semanas, território e CSV;
  - `ProducaoAutorizacaoControllerTest`, com segurança ligada: o gestor não vê o programa sensível nem o nome do
    agravo sensível; o coordenador de enfermagem vê o programa sensível; sem a permissão, 403.
- **Robot de API:** `test/indicadores/IndicadoresDaRede.robot`.
- **Robot de tela:** CT-004 de `test/ui/indicadores/UI_sala_de_situacao.robot`.
