# Modelo do Estoque e Almoxarifado (domínio 13)

Desenho na [ADR-0177](../adr/0177-estoque-e-almoxarifado.md). Material e insumo geral; medicamento fica na Farmácia e vacina, na Sala de
Vacina.

## Fatias

| Fatia | O que é | Estado |
|---|---|---|
| ES1 | Catálogo, estoque por unidade em lotes, livro (entrada, saída por consumo, perda, ajuste), mínimo e alertas | ✅ ADR-0177, V62 |
| ES2 | Almoxarifado central, requisição das unidades, transferência em duas etapas com divergência, inventário com fechamento | ⬜ só com aprovação |
| ES3 | Emergências (E4): necessidade de insumo da E3 atendida pelo estoque, reserva e consumo extraordinário | ⬜ só com aprovação |

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

## Permissões

| Permissão | Para quê | Papéis padrão |
|---|---|---|
| `ESTOQUE.CONSULTAR` | Saldo, livro e alertas | Almoxarife, Gestor, Coordenador de enfermagem |
| `ESTOQUE.MOVIMENTAR` | Entrada, saída, perda e ajuste | Almoxarife |
| `ESTOQUE.GERENCIAR` | Catálogo e mínimo | Gestor |
