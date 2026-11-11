# Modelo do Estoque e Almoxarifado (domínio 13)

Desenho na [ADR-0177](../adr/0177-estoque-e-almoxarifado.md). Material e insumo geral; medicamento fica na Farmácia e vacina, na Sala de
Vacina.

## Fatias

| Fatia | O que é | Estado |
|---|---|---|
| ES1 | Catálogo, estoque por unidade em lotes, livro (entrada, saída por consumo, perda, ajuste), mínimo e alertas | ✅ ADR-0177, V62 |
| ES2 | Almoxarifado central, requisição das unidades, transferência em duas etapas com divergência, inventário com fechamento | ✅ ADR-0189, V74 |
| ES3 | Emergências: necessidade de insumo da E3 atendida pelo estoque, reserva e consumo extraordinário | ✅ ADR-0191, V76 |

## Entidades (ES1)

```text
MaterialEstoque (catálogo da rede)
  codigo, nome (únicos) · catmat · categoria · unidadeMedida · controlaLote · ativo
      │
      ▼
LoteMaterial (por unidade)                EstoqueMinimo (material × unidade)
  material · unidade · numeroLote           quantidadeMinima
  validade · quantidade · custoUnitario
      │
      ▼
MovimentacaoEstoque (livro, só inclusão)
  tipo: SALDO_INICIAL · ENTRADA · SAIDA_CONSUMO · PERDA · AJUSTE_INVENTARIO
  quantidade (com sinal) · saldoApos · origem · documento · fornecedor
  setor (no consumo) · motivoPerda · justificativa · profissional · registradoEm
```

## Regras

- **Saldo:** nunca negativo; só muda por lançamento no livro.
- **Lote:** material com controle exige lote e validade na entrada; sem controle, um saldo único por unidade. O controle não muda depois da
  primeira entrada.
- **Validade:** lote vencido não entra e não sai por consumo; sai como perda.
- **Saída:** sem lote escolhido, sai dos que vencem primeiro (FEFO), em quantos lotes precisar.
- **Alertas:** abaixo do mínimo; vencendo em até 90 dias; vencido com saldo.

## Estoque nas emergências (ES3)

```text
Mobilizacao (insumo, E3) ──► MaterialEstoque
      ├──► RequisicaoEstoque (urgente, emergencia_id; origem central)    ou
      └──► TransferenciaEstoque (avulsa, mesmo destino)

ReservaEstoque (emergência, unidade, material)
  quantidade · consumida · situacao (ativa, liberada, consumida) · justificativa
MovimentacaoEstoque.emergencia_id (o consumo extraordinário)
```

- **Livre** = saldo dentro da validade − reservas ativas. A saída comum, a perda de lote válido, a transferência avulsa e a requisição de
  fora da emergência não passam do livre (422).
- **Usam a reserva:** a requisição urgente da emergência e o consumo marcado com ela; abatem das reservas dela, as mais antigas primeiro.
- **Fim:** a desmobilização e o encerramento liberam o que resta.

## Permissões

| Permissão | Para quê | Papéis padrão |
|---|---|---|
| `ESTOQUE.CONSULTAR` | Saldo, livro e alertas | Almoxarife, Gestor, Coordenador de enfermagem |
| `ESTOQUE.MOVIMENTAR` | Entrada, saída, perda e ajuste | Almoxarife |
| `ESTOQUE.GERENCIAR` | Catálogo e mínimo | Gestor |
