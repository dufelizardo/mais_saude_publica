# Financeiro (domínio 17)

Desenho na [ADR-0218](../adr/0218-financeiro-dotacao-e-empenho.md). A plataforma acompanha a execução do Fundo Municipal de Saúde; o
lançamento contábil, o SIOPS e a aplicação mínima ficam no SIAFIC do município (Decreto 10.540/2020).

## Fatias

| Fatia | Conteúdo | Estado |
|-------|----------|--------|
| F1 | Dotação com suplementação e anulação; empenho com reforço e anulação; ordem de fornecimento com o empenho | ✅ ADR-0218, V99 |
| F2 | Liquidação pelo atesto, retenções, pagamento, restos a pagar, diárias do TFD, painel da execução e ordem cronológica | ⏳ |
| F3 | Centros de custo, apuração mensal do custo por unidade, custo por atendimento, teto da PPI | ⏳ |

## Entidades (F1)

```text
DotacaoOrcamentaria   exercicio · programaTrabalho (10.301.0010.2045) · descricao · naturezaDespesa (3.3.90.30)
                      fonte: RECURSOS_PROPRIOS · BLOCO_MANUTENCAO · BLOCO_ESTRUTURACAO · TRANSFERENCIA_ESTADUAL · OUTRAS · fonteCodigo · valorInicial
  └── AlteracaoDotacao   tipo: SUPLEMENTACAO · ANULACAO · valor · ato · data · motivo
Empenho               numero (do SIAFIC, único no exercício) · exercicio · tipo: ORDINARIO · ESTIMATIVO · GLOBAL · dotacao · fornecedor
                      instrumento (a ata ou o contrato, ADR-0213) · valor · data · historico
  └── MovimentoEmpenho   tipo: REFORCO · ANULACAO · valor · data · motivo
OrdemFornecimento.empenhoRef (ADR-0214): a ordem consome o saldo do empenho
```

## Regras

- **Dotação:**
  - o exercício anterior, o atual ou o seguinte; única pelo programa, pela natureza e pela fonte;
  - o autorizado é a lei mais as suplementações menos as anulações; o disponível é o autorizado menos o empenhado;
  - a anulação vai até o disponível.
- **Empenho:**
  - o número do SIAFIC, a data do exercício; o fornecedor ativo e não impedido; a ata ou o contrato do mesmo fornecedor;
  - o valor e o reforço cabem no disponível da dotação; a anulação vai até o saldo do empenho.
- **Saldo do empenho:** o valor atual menos o comprometido nas ordens de fornecimento (a cancelada conta só o aceito).
- **Ordem de fornecimento:** com o empenho, só do mesmo fornecedor e dentro do saldo; sem ele, o número em texto, como antes.

## Acesso

| Permissão | Papéis padrão | O que libera |
|-----------|---------------|--------------|
| `FINANCEIRO.CONSULTAR` | Financeiro, Gestor, Compras | Ver as dotações e os empenhos |
| `FINANCEIRO.GERENCIAR` | Financeiro | Registrar as dotações, as alterações, os empenhos, os reforços e as anulações |

## Tela

Tela **Financeiro** (Administrativo):

- **Cabeçalho:** o exercício e o resumo (autorizado, empenhado, disponível).
- **Dotações:** a lista e a gaveta com as alterações.
- **Empenhos:** a lista e a gaveta com as ordens de fornecimento, os reforços e as anulações.
- **Compras:** a nova ordem de fornecimento escolhe o empenho com saldo do fornecedor.
