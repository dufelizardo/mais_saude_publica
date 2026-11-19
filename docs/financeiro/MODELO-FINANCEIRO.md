# Financeiro (domínio 17)

Desenho na [ADR-0218](../adr/0218-financeiro-dotacao-e-empenho.md). A plataforma acompanha a execução do Fundo Municipal de Saúde; o
lançamento contábil, o SIOPS e a aplicação mínima ficam no SIAFIC do município (Decreto 10.540/2020).

## Fatias

| Fatia | Conteúdo | Estado |
|-------|----------|--------|
| F1 | Dotação com suplementação e anulação; empenho com reforço e anulação; ordem de fornecimento com o empenho | ✅ ADR-0218, V99 |
| F2 | Liquidação pelo atesto, retenções, pagamento, restos a pagar, diárias do TFD, painel da execução e ordem cronológica | ✅ ADR-0219, V100 |
| F3 | Centros de custo, apuração mensal do custo por unidade, custo por atendimento e por paciente-dia (o teto da PPI foi para o domínio 20) | ✅ ADR-0220, V101 |
| F4 | Rateio do PNGC em degraus, critério do centro, lotação por setor, unidade mista | ✅ ADR-0221, V102 |

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

## Entidades (F2)

```text
Liquidacao   empenho · origem: RECEBIMENTO · ORDEM_SERVICO · DIARIA_TFD · AVULSA (o atesto, único por liquidação)
             notaFiscal · valorBruto · iss · irrf · inss · outrasRetencoes · valorLiquido · liquidadaEm · vencimento
             justificativa e atestadoPorCpf (avulsa) · pagaEm · ordemBancaria · justificativaForaDeOrdem
DiariaTfd    solicitacao (TFD autorizado, ADR-0190) · beneficiario: PACIENTE · ACOMPANHANTE · credor (pessoa física) · dias · valorDiaria · valorTotal
```

## Entidades (F3)

```text
CentroCusto             unidade · setor (vazio no geral, um por unidade) · codigo (CC-00001) · nome · tipo: PRODUTIVO · APOIO · ADMINISTRATIVO · ativo
                        · criterioRateio: PESSOAS · CUSTO_DIRETO · AREA · REQUISICOES (F4) · areaM2 (F4)
ApuracaoCusto           competencia (AAAA-MM, única) · situacao: ABERTA · FECHADA · naoAlocado · saidasSemCusto · naoRateado (F4) · apuradaEm · fechadaEm
  ├── LinhaApuracaoCusto     centro · categoria: PESSOAL · MATERIAL_CONSUMO · MEDICAMENTOS · SERVICOS_TERCEIROS · MANUTENCAO
  │                          · DEPRECIACAO · DIARIAS_TFD · OUTRAS · valor
  ├── ProducaoApuracaoCusto  unidade · atendimentos · pacientesDia
  ├── CustoCentroApuracao    centro · direto · recebido · enviado · internacao (F4)
  └── RateioApuracaoCusto    origem · destino · ordem (degrau) · criterio · valor (F4)
Liquidacao.centroCusto  na avulsa
Lotacao.setor           opcional, do RH (F4)
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
- **Liquidação:** do atesto (o empenho da ordem, ou do mesmo credor e contrato), até o valor do atesto e o saldo a liquidar do empenho;
  as retenções até o bruto; a avulsa com a justificativa e quem atestou.
- **Pagamento:** a ordem bancária; pagar antes de outra vencida da mesma fonte pede a justificativa (Lei 14.133/2021, art. 141).
- **Diária do TFD:** da solicitação autorizada; o credor pessoa física (pelo cadastro ou pelo CPF); uma por beneficiário.
- **Restos a pagar:** processados (liquidado não pago) e não processados (não liquidado), dos exercícios anteriores.
- **Centro de custo:** o geral de cada unidade ativa nasce na leitura e não se desativa; o do setor, um por setor, com o nome e o tipo
  do setor quando não informados. O custo com setor vai ao centro ativo do setor; sem setor (ou com o centro inativo), ao geral.
- **Apuração:** do mês encerrado; pessoal pela lotação vigente, consumo do Estoque pelo custo do lote, medicamento pelo preço do contrato
  do lote recebido, a parte de serviço das liquidações, a manutenção interna, a depreciação linear, as diárias do TFD e as avulsas.
  A saída sem preço fica fora do total e é contada; o que não acha centro fica não alocado. Aberta, refeita; fechada, não muda.
- **Indicadores:** o custo por atendimento e por paciente-dia; acima da média do tipo de unidade a partir de 30%.
- **Rateio (F4):** em cada unidade, os administrativos e depois os de apoio, na ordem do código, mandam o direto e o recebido aos ativos
  que ainda não ratearam e aos produtivos, pela base do critério (sem critério: pessoas no administrativo, custo direto no apoio).
  Base zero, pelo custo direto; sem destino, não rateado. O produtivo não tem critério. Fechada a competência, o rateio é fotografia.
- **Unidade mista (F4):** o produtivo com leito ativo no setor é internação; o custo final dele vai ao paciente-dia e o resto ao
  atendimento.
- **Lotação por setor (F4):** o setor da unidade da lotação e ativo; a folha vai ao centro do setor.

## Acesso

| Permissão | Papéis padrão | O que libera |
|-----------|---------------|--------------|
| `FINANCEIRO.CONSULTAR` | Financeiro, Gestor, Compras | Ver as dotações e os empenhos |
| `FINANCEIRO.GERENCIAR` | Financeiro | Registrar as dotações, as alterações, os empenhos, os reforços e as anulações; liquidar e pagar; os centros de custo, apurar e fechar |
| `INDICADOR.CONSULTAR` | Gestor, coordenações | Os indicadores de custo, nas unidades do acesso |
| `TFD.AUTORIZAR` | Comissão de TFD | Conceder e ver as diárias do TFD |

## Tela

Tela **Financeiro** (Administrativo):

- **Cabeçalho:** o exercício e o resumo (autorizado, empenhado, disponível).
- **Dotações:** a lista e a gaveta com as alterações.
- **Empenhos:** a lista e a gaveta com as ordens de fornecimento, os reforços e as anulações.
- **A liquidar, Liquidações, Restos a pagar e Execução:** a fila com a liquidação (e a avulsa), o pagamento, os restos e a execução por
  dotação e por fonte.
- **Custos:** a competência com apurar, refazer e fechar; o custo por unidade e por centro, com o direto, o recebido, o enviado e o
  final; o não rateado e a tabela do rateio; os centros de custo com o critério e a área. A avulsa escolhe o centro.
- **Sala de Situação:** o cartão Custo por unidade da competência anterior, com o custo por paciente-dia quando há.
- **Perfil do profissional:** a transferência escolhe o setor da unidade.
- **Compras:** a nova ordem de fornecimento escolhe o empenho com saldo do fornecedor.
- **Frota:** a solicitação de TFD autorizada concede as diárias.
