# 0198 — Gestão da Rede: cobertura da rede e indicadores de pactuação (GR3)

## Status

Aceita e implementada. Fatia **GR3** do domínio 30 ([ADR-0179](./0179-gestao-da-rede-de-atencao.md); detalhe em
[`rede/MODELO-REDE.md`](../rede/MODELO-REDE.md)). Com ela, o domínio fica completo; teto financeiro (domínio 17) e SISREG/SIGTAP (domínio 20)
ficam fora e não impedem fechar.

## Contexto

- **A GR1** ([ADR-0179](./0179-gestao-da-rede-de-atencao.md)) grava a oferta, as cotas da PPI, o uso e as extracotas por competência.
- **A GR2** ([ADR-0197](./0197-redes-tematicas-e-referencia.md)) grava a referência estrutural, marca a autorização fora dela e mostra as
  lacunas das redes temáticas.
- **A Regulação** grava a fila (com a data do pedido) e os eventos de autorização (com a hora).
- **O Território** ([ADR-0128](./0128-adscricao-e-cobertura.md)) dá a população adscrita: pessoa → equipe → unidade.
- Faltava ver tudo junto, por procedimento e por origem, e levar os indicadores ao #19. Pela ADR-0142, o BI só consolida o que os domínios
  registram.

## Decisão

1. **Sem cadastro novo e sem migração:** a cobertura é calculada sobre o que a GR1, a GR2, a Regulação e o Território já gravam.
2. **Por procedimento com oferta no período:**
   - ofertado, pactuado, utilizado (na cota ou extracota, na executante e no mês ofertados, a regra da GR1) e extracotas;
   - os percentuais: da oferta pactuada, da cota usada (uso na cota sobre o pactuado), de extracota nas autorizações e de fora da referência
     (entre as autorizações que tinham referência);
   - a fila de hoje (solicitada ou devolvida) com a mediana de dias de espera, e a mediana de dias entre o pedido e a autorização registrada
     no período.
3. **Por origem:**
   - a origem é a beneficiária mais específica que cobre a solicitante (a unidade ou a esfera acima) ou, sem cota, a própria solicitante;
   - a **população adscrita** às equipes da unidade e, na esfera, a soma das unidades abaixo dela;
   - a cota, a **cota por mil adscritos**, as solicitadas no período, as autorizadas (e as extracotas), as que aguardam e o saldo;
   - se há referência vigente que a cobre.
4. **Alertas:**
   - **cota esgotada com fila:** saldo zero e solicitações aguardando dessa origem;
   - **cota ociosa:** só na competência fechada, com uso abaixo da metade da cota;
   - **demanda sem cota:** a origem pede um procedimento com oferta controlada e não tem cota;
   - **sem referência:** a origem com demanda, ou a UBS do acesso, sem referência num procedimento que tem referência na rede;
   - as **lacunas das redes temáticas** da GR2, na mesma abrangência.
5. **No #19:** o relatório `PACTUACAO_REDE` ("Pactuação e cobertura da rede", categoria "Gestão da rede") no catálogo da BI4
   ([ADR-0167](./0167-bi-relatorios.md)):
   - o resumo com os percentuais, as cotas esgotadas com fila e as lacunas;
   - as seções por procedimento, por origem, os alertas e as lacunas;
   - o período de até um ano (as competências do período) e o escopo dos indicadores (`INDICADOR.CONSULTAR`); o CSV na trilha de auditoria;
   - o painel das redes temáticas ganhou um cálculo sem a checagem de acesso (`montar`), para o relatório usar o escopo dos indicadores em vez
     do acesso de rede.
6. **Tela:** a Rede e pactuação ganha a aba **Cobertura**, com a competência, os cartões dos percentuais, os alertas e as lacunas, a tabela por
   procedimento e, ao abrir um, a tabela por origem com a cota por mil e os alertas em etiqueta.
7. **Acesso:** sem permissão nova. `REDE.CONSULTAR` ou `REDE.GERENCIAR` na tela, com as ofertas visíveis pela executante ou por alguma
   beneficiária, e as origens do acesso; `INDICADOR.CONSULTAR` no relatório. Só agregados, nada de paciente.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `rede/cobertura` (`competencia`, `procedimentoId`) | `REDE.CONSULTAR` ou `REDE.GERENCIAR` |
| GET | `relatorios/PACTUACAO_REDE` (`de`, `ate`, `unidadeId`; e `/exportacao`) | `INDICADOR.CONSULTAR` (escopo) |

### Migração

Nenhuma: a fatia só lê.

## Alternativas descartadas

- **Parâmetros assistenciais por portaria (necessidade estimada por habitante):** variam por procedimento e por portaria e pedem cadastro
  próprio. O sistema mostra a cota por mil adscritos e deixa a comparação com o parâmetro à gestão.
- **População do IBGE:** é integração (domínio 20); a adscrita já existe e é a que a atenção básica acompanha.
- **Guardar a cobertura mês a mês:** os números se recalculam do que está gravado; o histórico da autorização (cota, extracota, fora da
  referência) já fica na solicitação.

## Consequências

- **STATUS-DOS-DOMINIOS:** o domínio 30 passa a ✅; o teto financeiro e o SISREG seguem nos domínios 17 e 20.
- **A cota por mil depende da adscrição:** unidade sem equipe com adscritos mostra população zero e não calcula a razão.
- **Fora do escopo:** teto financeiro (MAC) e programação físico-orçamentária (17); SISREG e SIGTAP (20).

## Testes

- **JUnit `CoberturaRedeControllerTest`:**
  - a competência com a oferta de 10, as cotas da UBS e do município, a referência, as autorizações (na cota, extracota e pela esfera), a fila e
    a demanda de fora: 50% pactuado, 60% da cota usada, 25% de extracota, 0% fora da referência, duas na fila;
  - a UBS com 4 adscritos (500 por mil) e a cota esgotada com fila; o município com 10 adscritos somando as unidades abaixo; fora, a demanda
    sem cota e sem referência; a UBS sem demanda nos alertas;
  - o relatório pelo município, com a espera até a autorização, e a exportação;
  - a cota ociosa na competência fechada e não na aberta.
- **JUnit `CoberturaRedeAutorizacaoControllerTest`,** com a segurança ligada: sem login (401), o ACS (403), o gestor do município vendo o
  procedimento e o gestor de outra unidade sem ele.
- **`RelatorioControllerTest` e `RelatorioAutorizacaoControllerTest`** passam a cobrir o `PACTUACAO_REDE`.
- **Robot:** `CoberturaRede.robot` (API, CT-001); `Relatorios.robot` com os oito relatórios; CT-005 de `UI_ppi.robot`; CT-006 de
  `UI_relatorios.robot`.
