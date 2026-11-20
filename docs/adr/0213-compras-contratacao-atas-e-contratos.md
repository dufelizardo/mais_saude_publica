# 0213 — Compras: contratação, atas de registro de preços e contratos (CP2)

## Status

Aceita e implementada. Segunda fatia do domínio 14, cujo desenho está na [ADR-0212](./0212-compras-fornecedores-e-solicitacoes.md).

## Contexto

- **A CP1 termina na solicitação cotada,** com o preço de referência de cada item. Falta registrar o resultado da licitação e o
  instrumento que obriga o fornecedor.
- **A Lei 14.133/2021 traz os limites que o sistema deve conferir:**
  - a ata de registro de preços vale 1 ano e se prorroga uma vez, até 2 anos, sem acréscimo de quantidade (arts. 82 a 86);
  - o contrato de fornecimento vai até 1 ano (art. 105); o de serviço contínuo, até 5, prorrogável até 10 (arts. 106 e 107);
  - os acréscimos e as supressões vão até 25% do valor inicial (art. 125);
  - a execução fica com o gestor e o fiscal designados (art. 117), que são pessoas diferentes (segregação de funções, art. 7º, § 1º);
  - quem contrata está habilitado e sem impedimento.
- **A licitação acontece no portal de compras.** Aqui fica o resultado: quem venceu cada item e por quanto.

## Decisão

### A contratação

1. **Abertura.** Reúne uma ou mais solicitações **cotadas** (422 nas outras), que não estejam em outra contratação em andamento (409).
   - O número do processo é único (409).
   - **Modalidades:** pregão eletrônico, concorrência, dispensa, inexigibilidade e adesão a ata. A dispensa e a inexigibilidade pedem o
     fundamento legal (arts. 74 e 75); a adesão pede a ata de origem (400).
   - **Itens iguais somam a quantidade:** o mesmo material, o mesmo medicamento ou o mesmo serviço com a mesma unidade de medida. O preço
     de referência do item somado é a média ponderada pela quantidade.
2. **Resultado por item,** enquanto a contratação está em andamento (422 depois):
   - **adjudicado**, com o fornecedor e o preço (400 sem eles). O fornecedor precisa estar **regular**: ativo, sem impedimento e com as
     certidões válidas (422). Preço acima da referência pede justificativa (422);
   - **fracassado** (nenhuma proposta aceitável) ou **deserto** (nenhuma proposta).
3. **Homologação.** Exige que nenhum item esteja pendente (422) e gera **um instrumento para cada fornecedor vencedor**, nem mais nem
   menos (400):
   - **tipo:** ata de registro de preços ou contrato;
   - **número:** único (409);
   - **vigência:** fim depois do início. A ata e o contrato de fornecimento vão até 1 ano; o serviço contínuo, só em contrato de
     serviços, até 5 (400);
   - **gestor e fiscal:** indicados pela matrícula e diferentes (422);
   - **fornecedor:** conferido de novo (422).

   As solicitações passam a **contratadas**. Se todos os itens fracassarem, a homologação acontece sem instrumento.
4. **Cancelamento** da contratação em andamento, com o motivo. As solicitações voltam a ficar livres para outra contratação.

### Atas e contratos

5. **Itens do instrumento:** a quantidade inicial, a quantidade atual (com os aditivos), o preço e o **saldo**, que a ordem de
   fornecimento da CP3 vai consumir. O valor inicial, base dos 25%, fica gravado e não muda.
6. **Aditivos,** numerados e sem apagar, assinados até hoje (400) e dentro da vigência (422):
   - **prazo:** novo fim depois do atual (400). A ata se prorroga uma vez, até 2 anos do início; o serviço contínuo, até 10 anos (422);
   - **acréscimo e supressão:** só em contrato (422). A soma de cada tipo vai até 25% do valor inicial; a supressão não passa do saldo
     (422);
   - **reequilíbrio:** o novo preço do item.
7. **Alertas,** calculados na leitura:
   - vigência terminando em até 90 dias ou já encerrada;
   - item com saldo abaixo de 20%;
   - fornecedor do instrumento vigente impedido ou com certidão pendente.

### Acesso e tela

8. **Permissões:**
   - **`COMPRAS.GERENCIAR`:** conduz a contratação, lista todos os instrumentos e registra os aditivos.
   - **Gestor e fiscal designados:** veem os próprios instrumentos em `meus-contratos` e o detalhe, mesmo sem permissão de Compras. O
     endpoint é liberado a quem está logado, e o filtro é o CPF do profissional.
9. **Tela Administrativo · Compras,** com duas abas novas:
   - **Contratações:** abrir das solicitações cotadas, resultado por item e homologação com os dados de cada vencedor;
   - **Contratos:** a lista com os alertas e a gaveta com os itens, os saldos, os aditivos e o formulário de aditivo.

   Quem não pede nem compra vê só a aba Contratos, com os próprios contratos.
10. **Banco:** a `V94__compras_contratacao_e_contratos.sql` cria `tb_contratacao`, `tb_item_contratacao`,
    `tb_instrumento_contratual`, `tb_item_instrumento` e `tb_aditivo_contratual`. Também liga a solicitação e o item dela à
    contratação e acrescenta a situação `CONTRATADA`.

## Alternativas descartadas

- **Um instrumento por item:** o fornecedor que vence vários itens assina uma ata ou um contrato só; é o que a gestão acompanha.
- **Situação do instrumento numa coluna (vigente, vencido):** a vigência é uma data. A situação derivada não depende de rotina, como já
  se fez com o fornecedor na ADR-0212.
- **Conferir os 25% sobre o valor atual:** a lei conta sobre o valor inicial atualizado. O valor inicial fica gravado; a atualização
  monetária fica para o Financeiro (domínio 17).

## Consequências

- **A CP3 começa do saldo:** a ordem de fornecimento baixa o saldo do item, e o recebimento definitivo dá entrada no Estoque ou na
  Farmácia.
- **O fiscal sem permissão de Compras não vê o item no menu.** O menu é por permissão, não por designação. Ele abre os próprios contratos
  pela rota de Compras, em que só aparece a aba Contratos. A CP3 ([ADR-0214](./0214-compras-ordens-recebimento-e-painel.md)) pôs o item "Meus
  contratos" no menu para essas pessoas.
- **Fora daqui:** empenho e atualização monetária (domínio 17); publicação no PNCP (domínio 20).

## Testes

- **JUnit `ContratosControllerTest`:**
  - a abertura: dispensa sem fundamento (400), solicitação não cotada (422), processo repetido e solicitação em outra contratação
    (409), soma dos itens e referência ponderada;
  - o resultado: fornecedor irregular (422), preço acima da referência sem justificativa (422), adjudicado sem fornecedor (400);
  - a homologação: sem instrumento (400), ata acima de 1 ano (400), gestor igual ao fiscal (422), solicitação contratada;
  - a ata: sem acréscimo, prorrogada uma vez, até 2 anos;
  - o contrato contínuo: acréscimo de 20% e o que passa de 25% (422), supressão acima do saldo (422), reequilíbrio, prazo acima de
    10 anos (422) e assinatura futura (400);
  - o cancelamento que libera a solicitação, a adesão e o item fracassado.
- **JUnit `ContratosAutorizacaoControllerTest`:**
  - o gestor conduz e o almoxarife não;
  - o fiscal com papel de recepção vê o próprio contrato e não registra aditivo;
  - outro profissional recebe 403;
  - sem login, 401.
- **Robot de API:** `test/compras/Contratos.robot`, CT-001 a CT-004.
- **Robot de tela:** `test/ui/administrativo/compras/UI_compras.robot`, CT-005 (contratação e homologação) e CT-006 (aditivo).
