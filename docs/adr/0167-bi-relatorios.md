# 0167 — Indicadores e BI: relatórios (BI4)

## Status

Aceita e implementada. Fatia BI4 do domínio 19, desenhada na [ADR-0142](./0142-indicadores-bi-e-gestao.md). Segue as BI1 a BI3
([ADR-0143](./0143-bi-producao-assistencial.md), [ADR-0144](./0144-bi-situacao-operacional.md),
[ADR-0146](./0146-bi-indicadores-da-rede.md)).

## Contexto

- **A Sala de Situação é um painel:** responde "como estamos agora". A gestão também precisa de um documento do período, para
  levar à reunião, ao conselho ou à planilha.
- **Os números já existem.** Pela ADR-0142, o BI consome e consolida, sem recalcular com outra regra.
- **O protótipo `Relatorios.html` vai além do que o sistema tem hoje:** PMAQ, SISAB, envio agendado, montador de relatórios.

## Decisão

1. **Catálogo fixo no código** (`TipoRelatorio`), sem tabela nova:

   | Categoria | Relatório | Fonte |
   |---|---|---|
   | Assistencial | Produção assistencial | BI1 |
   | Operacional | Situação operacional | BI2 |
   | Programas | Indicadores dos programas | BI3 |
   | Epidemiológico | Cobertura vacinal | Imunização, pela BI3 |
   | Epidemiológico | Vigilância em saúde | BI3 |

2. **Um formato só:** resumo, tabelas e notas, com os números como texto no formato brasileiro. A tela e o CSV saem do mesmo
   `RelatorioDto`, então não divergem. As exportações antigas da Sala de Situação continuam como estão.
3. **Escopo, período e sensíveis vêm do domínio:** o `RelatorioService` só organiza. Sem unidade, soma as unidades do acesso;
   o período vale até um ano (400). O relatório avisa em nota quando há programa ou notificação sensível oculta.
4. **Só agregados.** As ocorrências da escala saem contadas por unidade e situação, sem o nome do profissional, ao contrário da
   Sala de Situação, que é uma tela de operação.
5. **Exportação:** CSV com ponto e vírgula e BOM, na rota `/exportacao`, que entra na trilha de auditoria. O texto que uma
   planilha leria como fórmula é neutralizado.
6. **PDF pela impressão do navegador,** com folha própria, sem gerar arquivo no servidor.
7. **Permissão:** `INDICADOR.CONSULTAR`, sem permissão nova.

### O que sai do protótipo

- **PMAQ e SISAB:** o PMAQ foi substituído no financiamento da atenção primária, e o envio ao SISAB depende de Integrações (#20),
  adiado.
- **Produtividade por profissional:** ranking individual, com exposição de pessoa. Os agregados por categoria seguem na produção.
- **Agendar envio por e-mail:** depende do e-mail da C1.
- **Novo relatório (montador livre):** é um produto à parte.
- **Bloco de endpoint da API:** as rotas já estão no Swagger.

### API

- `GET /api/v1/relatorios`: o catálogo;
- `GET /api/v1/relatorios/{codigo}?de=&ate=&unidadeId=`: o relatório (código desconhecido, 404);
- `GET /api/v1/relatorios/{codigo}/exportacao`: o CSV.

### Tela

**Relatórios**, no menu logo abaixo da Sala de Situação, porque o sistema não tem o grupo Gestão do protótipo: filtros de período e
unidade, catálogo com categorias e busca à esquerda, prévia à direita, com Imprimir e Exportar CSV.

### Migração

Nenhuma.

## Consequências

- **Relatório novo é um caso no enum e um método,** sem tabela.
- **Ficam para depois:** o envio agendado (depois da C1), favoritos e o painel por coordenação (BI5).

## Testes

- **JUnit:**
  - `RelatorioControllerTest`: o catálogo; cada relatório com resumo e tabelas; o código sem diferenciar maiúsculas; o desconhecido
    (404); o período invertido e o longo (400); o CSV com o nome, o BOM e os títulos;
  - `RelatorioAutorizacaoControllerTest`, com a segurança ligada: sem login (401), sem a permissão (403), a unidade fora do acesso
    (403) em cada relatório, e a exportação na trilha.
- **Robot:** `Relatorios.robot` (API, cinco casos) e `UI_relatorios.robot` (menu, filtros, exportação e período inválido).
