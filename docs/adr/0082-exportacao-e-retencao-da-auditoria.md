# 0082 — Exportação e retenção da trilha de auditoria

## Status

Aceita e implementada. Resolve duas pendências das ADRs [0070](./0070-trilha-de-auditoria.md) e
[0071](./0071-consulta-da-trilha-de-auditoria.md): a exportação e a política de retenção.

## Contexto

A trilha registra quem leu ou alterou o quê (ADR-0070), e a tela Auditoria consulta com filtros (ADR-0071).
Faltavam duas coisas para operar com dado real:
- **Exportar.** A LGPD (art. 18 e 19) dá ao titular o direito de saber quem acessou os seus dados. A
  resposta formal precisa de um arquivo, não de uma tela.
- **Guardar por um prazo definido.** A trilha "só crescia", sem limite. Guardar dado pessoal além do
  necessário também fere a LGPD (princípio da necessidade), e o volume cresce indefinidamente.

## Decisão

1. **Exportação em CSV:** `GET /auditoria/exportacao`.
   - Usa os mesmos filtros e o mesmo escopo da consulta, e exige `AUDITORIA.CONSULTAR`.
   - Separador `;` e BOM, para abrir direto no Excel em português. Datas no fuso de Brasília. Do mais recente
     ao mais antigo.
   - Teto de **50.000 eventos**: acima disso, 422, pedindo um filtro mais estreito.
   - **Filtrar por paciente gera o relatório de acessos para o titular dos dados.**
   - Proteção contra fórmula: célula que começa com `=`, `+`, `-` ou `@` recebe um apóstrofo, para a
     planilha não executar nada.
   - **A exportação entra na própria trilha,** com a ação nova `EXPORTACAO`, a quantidade de eventos e quais
     filtros foram usados, sem repetir CPF ou nome. Filtrada por paciente, ela aparece também no "Quem
     acessou" daquele paciente.
   - Na tela Auditoria, o botão **Exportar CSV**, que estava "Em breve", baixa o filtro atual.
2. **Retenção de 20 anos, configurável** (`app.auditoria.retencao-anos`).
   - É o mesmo prazo de guarda do prontuário (Lei 13.787/2018, art. 6º). A trilha de quem acessou o
     prontuário vive tanto quanto ele.
   - Uma rotina diária, às 03:30 de Brasília, apaga em lotes os eventos mais antigos que o prazo e deixa um
     evento na trilha com quantos apagou e até que data.
   - Prazo **0 = guarda indefinida.**
   - `GET /auditoria/politica` informa o prazo, e a tela mostra "guardada por N anos".
3. **É a única exceção à trilha que "só cresce" (ADR-0070).** Ninguém apaga evento pela API ou pela tela;
   só a rotina, pelo prazo configurado.

## Trade-offs considerados

**CSV (escolhido)** × **PDF**
- ✅ Abre em qualquer planilha, filtra e soma, e é simples de gerar sem biblioteca nova.
- ❌ Não é um documento "assinado" para entregar ao titular. Um PDF formatado fica para quando houver um
  pedido formal que o exija.

**Apagar depois do prazo (escolhido)** × **arquivar em outra tabela ou arquivo**
- ✅ É simples e cumpre a necessidade da LGPD. No volume atual (home-lab), não há problema de desempenho a
  resolver com arquivamento.
- ❌ Depois do prazo, o evento some. Se surgir obrigação de guardar mais, basta aumentar o prazo antes de
  ele vencer.

## Consequências

- Pedidos do titular ("quem acessou meus dados?") são atendidos com um arquivo, pela tela.
- A trilha tem tamanho limitado e prazo justificado.
- **Pendente (ADR-0071):** alertas, como muitas recusas seguidas ou leituras fora do horário.

## Testes

- **JUnit (`AuditoriaExportacaoControllerTest`):**
  - o CSV traz BOM, cabeçalho, ordem, proteção contra fórmula e aspas no campo com `;`;
  - a exportação fica na trilha, com o paciente;
  - período invertido dá 400;
  - a retenção apaga só o que passou do prazo e registra na trilha;
  - com prazo 0, nada é apagado;
  - a rota de política responde.
- **Robot de API:** exportação (200 e 400) e política.
- **Robot de interface:** o botão "Exportar CSV" baixa o arquivo com o nome e o cabeçalho.
