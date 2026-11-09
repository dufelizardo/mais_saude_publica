# 0175 — Indicadores e BI: coordenações e gerência (BI5)

## Status

Aceita e implementada. Fatia BI5 da [ADR-0142](./0142-indicadores-bi-e-gestao.md), com o protótipo `prototipo/Coordenacoes.html`.

## Contexto

- **A regional já existia:** uma `UnidadeDeSaude` do tipo REGIONAL, para a qual as unidades apontam pela `supervisaoRegional`
  ([ADR-0013](./0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md)). A permissão dada nela já vale para as unidades abaixo
  ([ADR-0066](./0066-papeis-permissoes-e-escopo-por-unidade.md)).
- **Os painéis da BI1 a BI4** usam o mesmo recorte, `EscopoDosIndicadores`. Escolher a regional mostrava só ela, que não atende.
- **Faltava a estrutura de gestão** do protótipo: gerências por tema, coordenações, supervisões técnicas e quem responde por cada uma.

## Decisão

1. **Indicadores por regional.** Escolher uma esfera ou uma regional soma as unidades abaixo dela, pela unidade superior e pela supervisão
   regional. A mudança é no recorte comum (`AutorizacaoService.abaixoDe`), e por isso vale de uma vez para a Sala de Situação, os relatórios
   e a exportação. Unidade assistencial continua sendo só ela.
2. **Estrutura de gestão** (`TB_AREA_GESTAO`): tipo, tema, nome, sigla, descrição, área superior e, na coordenação, a regional.
   - **Hierarquia:** a gerência responde à secretaria (sem superior); a coordenação fica sob uma gerência, ou sem ela; a supervisão, sob uma
     coordenação ou uma gerência. Fora disso, ou com ciclo: 422.
   - **Regional:** só a coordenação aponta para uma, do tipo REGIONAL (422), e uma coordenação ativa por regional (409). A supervisão usa a
     regional da coordenação acima dela.
   - O tipo não muda (422). Área com áreas ativas abaixo não se desativa (422).
3. **Quem responde, como histórico** (`TB_TITULARIDADE_AREA_GESTAO`): profissional, papel (titular ou substituto), início, fim e o ato de
   designação.
   - Designar o seguinte encerra o anterior no mesmo papel no dia antes; ele não pode começar antes do atual (422). Nada se apaga.
   - Profissional inativo (422); o mesmo profissional não é titular e substituto ao mesmo tempo (422).
   - A designação também termina sem substituto, com a data (fim antes do início: 400).
4. **Permissões:** sem permissão nova. `ADMINISTRATIVO.GERENCIAR` mantém a estrutura, na regional quando a área tem uma; a consulta pede
   `ADMINISTRATIVO.CONSULTAR`, `ADMINISTRATIVO.GERENCIAR` ou `INDICADOR.CONSULTAR`. Os indicadores da regional pedem
   `INDICADOR.CONSULTAR` nela, como antes.

### API

- `GET, POST /api/v1/gestao/area`, `GET, PATCH /area/{uuid}`, `GET /regional`;
- `POST /api/v1/gestao/area/{uuid}/titularidade` e `POST /titularidade/{uuid}/encerramento`;
- os painéis da BI1 a BI4 com `unidadeId` de uma regional ou esfera passam a somar as unidades abaixo.

### Tela

**Coordenações e gerência,** em Administrativo: o resumo, o organograma por nível e o detalhe da área, com titular e substituto, o histórico,
as unidades da regional com a situação operacional e os indicadores da regional no mês (atendimentos, doses e vacinas na meta, metas dos
programas, notificações), lidos dos painéis que já existem.

### Migração

`V60__estrutura_de_gestao.sql`.

## Consequências

- **Os painéis por regional usam a mesma regra de acesso de sempre:** quem só tem acesso a uma UBS não lê a regional (403).
- **Ficam para depois:** cargo gestor e gratificação no RH; metas próprias de cada coordenação; exportar o organograma; supervisão com um
  recorte próprio de unidades.

## Testes

- **JUnit:** `EstruturaDeGestaoControllerTest` (hierarquia e regional; histórico de quem responde; vigilância da regional somando só a UBS
  abaixo) e `EstruturaDeGestaoAutorizacaoControllerTest`, com a segurança ligada (sem login, o ACS, o gestor da UBS e o da regional).
- **Robot:** `Gestao.robot` (API, cinco casos) e `UI_coordenacoes.robot`.
