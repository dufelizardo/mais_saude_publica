# Modelo de Compras, Contratos e Fornecedores (domínio 14)

O desenho está na [ADR-0212](../adr/0212-compras-fornecedores-e-solicitacoes.md) e segue a Lei 14.133/2021. A licitação acontece no
portal de compras, fora do sistema. Empenho e pagamento ficam no domínio 17.

## Fatias

| Fatia | O que é | Estado |
|---|---|---|
| CP1 | Fornecedor com certidões e sanções; solicitação de compra (DFD); pesquisa de preços com a mediana | ✅ ADR-0212, V93 |
| CP2 | Contratação, ata de registro de preços e contrato com saldo, gestor e fiscal, aditivos, alertas | ✅ ADR-0213, V94 |
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

## Entidades (CP2)

```text
Contratacao  numeroProcesso (único) · modalidade · fundamentoLegal · ataOrigem · objeto · situacao: EM_ANDAMENTO · HOMOLOGADA · CANCELADA
  ├── SolicitacaoCompra (cotadas, ligadas pela contratacao)
  └── ItemContratacao   itens iguais somados · precoReferencia ponderado · resultado: PENDENTE · ADJUDICADO · FRACASSADO · DESERTO
                        fornecedor · precoUnitario · justificativaPreco
InstrumentoContratual (um por vencedor)  tipo: ATA_REGISTRO_PRECOS · CONTRATO · numero (único) · inicio · fim · servicoContinuo
                                         valorInicial (fixo) · gestor ≠ fiscal
  ├── ItemInstrumento    quantidadeInicial · quantidade · saldo · precoUnitario
  └── AditivoContratual  numero · tipo: PRAZO · ACRESCIMO · SUPRESSAO · REEQUILIBRIO · antes e depois · justificativa · assinadoEm
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
- **Contratação:**
  - só solicitações cotadas e fora de outra contratação;
  - o vencedor está regular;
  - preço acima da referência pede justificativa;
  - a homologação gera um instrumento por vencedor.
- **Vigência:**
  - a ata vai até 1 ano e se prorroga uma vez, até 2;
  - o contrato de fornecimento vai até 1 ano;
  - o serviço contínuo, até 5, prorrogável até 10.
- **Aditivos:**
  - são assinados dentro da vigência;
  - acréscimos e supressões, cada um até 25% do valor inicial, só em contrato;
  - a supressão não passa do saldo.
- **Alertas:** vigência em até 90 dias, saldo abaixo de 20%, fornecedor impedido ou com certidão pendente.
- **Sugestão:** os materiais abaixo do mínimo na unidade, fora de solicitação em andamento, repostos até o dobro do mínimo.

## Acesso

| Permissão | Quem tem | Para quê |
|---|---|---|
| `COMPRAS.SOLICITAR` | Gestor, Almoxarife, Farmacêutico | Pedir para a unidade do escopo |
| `COMPRAS.GERENCIAR` | Compras (papel novo), Gestor | Analisar, cotar, manter os fornecedores, contratar e aditar, na rede inteira |
| (designação) | Gestor e fiscal do instrumento | Ver os próprios contratos em "meus contratos", sem permissão de Compras |

## Tela

Administrativo · Compras, com duas abas:
- **Solicitações:** a lista; a gaveta com os itens, as cotações e as ações; a criação com o editor de itens e "Sugerir pelo estoque".
- **Fornecedores:** a busca; a gaveta com as certidões e as sanções.
- **Contratações:** abrir das cotadas; o resultado por item; a homologação.
- **Contratos:** os alertas; a gaveta com os saldos e os aditivos. Quem não pede nem compra vê só esta aba, com os próprios contratos.
