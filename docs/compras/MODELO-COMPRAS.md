# Modelo de Compras, Contratos e Fornecedores (domínio 14)

O desenho está na [ADR-0212](../adr/0212-compras-fornecedores-e-solicitacoes.md) e segue a Lei 14.133/2021. A licitação acontece no
portal de compras, fora do sistema. Empenho e pagamento ficam no domínio 17.

## Fatias

| Fatia | O que é | Estado |
|---|---|---|
| CP1 | Fornecedor com certidões e sanções; solicitação de compra (DFD); pesquisa de preços com a mediana | ✅ ADR-0212, V93 |
| CP2 | Contratação, ata de registro de preços e contrato com saldo, gestor e fiscal, aditivos, alertas | ⏳ |
| CP3 | Ordem de fornecimento, recebimento provisório e definitivo (entrada no Estoque e na Farmácia), ocorrências, painel | ⏳ |

## Entidades (CP1)

```text
Fornecedor (rede)
  documento (CPF/CNPJ, único) · pessoaJuridica · razaoSocial · nomeFantasia · contato · ativo
      ├── CertidaoFornecedor (histórico)   tipo: FEDERAL · FGTS · TRABALHISTA · ESTADUAL · MUNICIPAL · emitidaEm · validaAte
      └── SancaoFornecedor (histórico)     tipo: ADVERTENCIA · MULTA · IMPEDIMENTO · INIDONEIDADE · inicio · fim · valor · revogadaEm

SolicitacaoCompra (da unidade)
  numero AAAA/000001 · unidade · setor · justificativa · prioridade · situacao · motivo
      └── ItemSolicitacaoCompra   tipo: MATERIAL (catálogo do Estoque) · MEDICAMENTO · SERVICO · quantidade · precoReferencia
              └── CotacaoCompra   fonte: FORNECEDOR · CONTRATACAO_PUBLICA · SITE · OUTRA · precoUnitario · cotadoEm
```

## Regras

- **Situação do fornecedor:** derivada, nunca gravada.
  - **Impedido:** tem impedimento ou inidoneidade vigente.
  - **Regular:** ativo, sem impedimento e com todas as certidões válidas. Da pessoa física não se exige a do FGTS.
- **Sanções (art. 156):**
  - o impedimento vai até 3 anos;
  - a inidoneidade, de 3 a 6;
  - a multa pede o valor;
  - a revogação guarda o motivo.
- **Solicitação:**
  - Ciclo: rascunho → enviada → aprovada → em cotação → cotada. A análise também pode devolver (com motivo, e a unidade corrige) ou
    rejeitar.
  - Só o rascunho e a devolvida se editam.
- **Pesquisa de preços:**
  - a cotação tem até um ano;
  - o fornecedor da cotação está ativo e sem impedimento;
  - concluir pede ao menos 3 cotações por item ou a justificativa;
  - o preço de referência é a mediana.
- **Sugestão:** os materiais abaixo do mínimo na unidade, fora de solicitação em andamento, repostos até o dobro do mínimo.

## Acesso

| Permissão | Quem tem | Para quê |
|---|---|---|
| `COMPRAS.SOLICITAR` | Gestor, Almoxarife, Farmacêutico | Pedir para a unidade do escopo |
| `COMPRAS.GERENCIAR` | Compras (papel novo), Gestor | Analisar, cotar e manter os fornecedores, na rede inteira |

## Tela

Administrativo · Compras, com duas abas:
- **Solicitações:** a lista; a gaveta com os itens, as cotações e as ações; a criação com o editor de itens e "Sugerir pelo estoque".
- **Fornecedores:** a busca; a gaveta com as certidões e as sanções.
