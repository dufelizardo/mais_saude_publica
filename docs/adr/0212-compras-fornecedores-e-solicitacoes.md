# 0212 — Compras, Contratos e Fornecedores: desenho do domínio e fatia CP1

## Status

Aceita. O desenho do domínio 14 está aceito; a fatia CP1 (fornecedores, solicitação de compra e pesquisa de preços) está implementada.
A CP2 está na [ADR-0213](./0213-compras-contratacao-atas-e-contratos.md); a CP3 ganha a própria ADR.

## Contexto

- **O domínio 14 estava vazio.** Só existiam os rótulos `COMPRAS` e `CONTRATOS`, reservados pelas ADRs 0032 e 0037.
- **A entrada no Estoque guarda o fornecedor em texto livre** (`tb_movimentacao_estoque.fornecedor`, ADR-0177). Não há cadastro, nem
  certidão, nem sanção.
- **A compra pública segue a Lei 14.133/2021:**
  - a necessidade é formalizada no documento de formalização da demanda (DFD), art. 12, VII;
  - o preço estimado vem de uma pesquisa com várias fontes, art. 23, na linha da IN SEGES 65/2021;
  - a contratação é feita por licitação, dispensa, inexigibilidade ou adesão a ata;
  - a execução fica com o gestor e o fiscal designados, art. 117;
  - o recebimento é provisório e depois definitivo, art. 140;
  - as sanções estão no art. 156.
- **A licitação acontece fora do sistema,** no portal de compras do município ou no Compras.gov. Aqui fica registrado o resultado.

## Decisão

### O domínio em três fatias

| Fatia | O que faz |
|---|---|
| **CP1** | Fornecedores com as certidões e as sanções; solicitação de compra (DFD); pesquisa de preços com o preço de referência |
| **CP2** | Contratação (modalidade, processo, vencedores); ata de registro de preços e contrato com saldo por item; gestor e fiscal; aditivos; alertas de vigência e de saldo |
| **CP3** | Ordem de fornecimento contra o saldo; recebimento provisório e definitivo, que gera a entrada no Estoque ou na Farmácia; ocorrências com o fornecedor; painel |

**Ficam fora:**
- **Domínio 17:** empenho, liquidação e pagamento.
- **Domínio 20:** PNCP, Compras.gov, SICAF, consulta de CNPJ na Receita e Painel de Preços. São integrações não impeditivas.

### CP1: o fornecedor

1. **Cadastro.**
   - CPF ou CNPJ, conferido pelo dígito verificador (400) e único (409). O documento não muda depois (422).
   - Razão social, nome fantasia, contato e endereço.
   - Ativo ou inativo.
2. **Certidões de regularidade** (art. 68): federal com a previdência, FGTS, trabalhista, estadual e municipal.
   - Cada uma tem a emissão (não futura) e a validade (não antes da emissão), 400.
   - Ficam em histórico; vale a que estiver dentro da validade.
   - A pessoa física não tem o certificado do FGTS (400 se vier).
3. **Sanções** (art. 156), também em histórico:
   - a **advertência** não tem fim nem valor;
   - a **multa** pede o valor;
   - o **impedimento** vai até 3 anos (§ 4º);
   - a **inidoneidade** vai de 3 a 6 anos (§ 5º);
   - fora disso, 400.

   A revogação (reabilitação do art. 163 ou decisão judicial) guarda a data e o motivo, sem apagar a sanção.
4. **A situação é derivada, sem coluna:**
   - **impedido:** com impedimento ou inidoneidade vigente;
   - **certidões pendentes:** os tipos exigidos sem certidão válida hoje;
   - **regular:** ativo, sem impedimento e sem pendência.

   Com isso, a certidão vence sozinha, sem rotina que atualize nada.

### CP1: a solicitação de compra (DFD)

5. **A solicitação é da unidade.**
   - Tem setor (opcional, da mesma unidade, 422), justificativa da necessidade e prioridade (normal ou urgente).
   - Recebe um número no formato `AAAA/000001`.
6. **Os itens:**
   - **material** do catálogo do Estoque: herda a unidade de medida;
   - **medicamento** da Farmácia;
   - **serviço** descrito, com a unidade de medida (por exemplo, serviço ou mês).

   Cada item tem a quantidade. Material ou medicamento inativo dá 422, e o mesmo produto repetido dá 400.
7. **Situações:**

   ```text
   rascunho ──envio──▶ enviada ──aprovar──▶ aprovada ──1ª cotação──▶ em cotação ──concluir──▶ cotada (segue para a CP2)
       ▲                  │
       └──── devolvida ◀──┤ devolver (com motivo)
                          └─▶ rejeitada (com motivo)
   ```

   - O rascunho e a devolvida se editam e se enviam.
   - O cancelamento pede o motivo:
     - **quem pede** cancela antes da aprovação;
     - **quem compra** cancela até concluir a pesquisa;
     - rejeitada, cotada ou cancelada, 422.
8. **Sugestão pelo estoque:** os materiais abaixo do mínimo na unidade (ES1) que ainda não estão em solicitação em andamento. A sugestão
   repõe até o dobro do mínimo.

### CP1: a pesquisa de preços

9. **Cotações por item,** na solicitação aprovada ou em cotação (422 fora disso):
   - a fonte é proposta de fornecedor, contratação pública similar, site ou outra;
   - na fonte fornecedor, o fornecedor é obrigatório, ativo e sem impedimento (422); nas outras fontes, a referência é obrigatória (400);
   - a data não é futura e tem até um ano (400);
   - a cotação lançada errada é removida enquanto a pesquisa não conclui.
10. **Conclusão da pesquisa:**
    - todo item precisa de ao menos uma cotação (422);
    - com menos de 3, a justificativa é obrigatória (422);
    - o **preço de referência** de cada item fica fixado na **mediana**, que é menos sensível a uma proposta fora da curva do que a média;
    - a solicitação vai a **cotada** e mostra o valor estimado (preço de referência vezes quantidade).

### Acesso e tela

11. **Permissões:**
    - **`COMPRAS.SOLICITAR`:** pedir no escopo da unidade. Gestor, almoxarife e farmacêutico.
    - **`COMPRAS.GERENCIAR`:** analisar, cotar e manter os fornecedores, na rede inteira, porque o setor de compras é central. Papel
      novo **Compras** e o Gestor.

    **Visibilidade:**
    - quem pede vê as solicitações da sua unidade;
    - quem compra vê as da rede que já saíram do rascunho;
    - quem só pede lê os fornecedores com o CPF de pessoa física mascarado.
12. **Tela Administrativo · Compras:**
    - **aba Solicitações:** a lista com a situação e o valor estimado. A gaveta de cada solicitação mostra os itens e as cotações e
      reúne o envio, a análise, a cotação, a conclusão e o cancelamento. A gaveta de criação tem o editor de itens e o botão "Sugerir
      pelo estoque".
    - **aba Fornecedores:** a busca por nome, CPF ou CNPJ e a situação. A gaveta mostra as certidões e as sanções, com os formulários.
13. **Banco:** a `V93__compras_fornecedores_e_solicitacoes.sql` cria seis tabelas:
    - `tb_fornecedor`, `tb_certidao_fornecedor` e `tb_sancao_fornecedor`;
    - `tb_solicitacao_compra`, `tb_item_solicitacao_compra` e `tb_cotacao_compra`.

## Alternativas descartadas

- **Guardar a situação do fornecedor numa coluna:** ficaria errada no dia em que a certidão vence ou a sanção termina, a menos que uma
  rotina a corrigisse (e rotina trava entre réplicas, ADR-0209). Derivada, ela está sempre certa.
- **Média como preço de referência:** uma proposta muito alta ou muito baixa puxa a média. A mediana é a prática recomendada para
  conjuntos pequenos.
- **Conduzir a licitação no sistema:** o pregão eletrônico acontece em portal próprio, com regras e certificação que não cabem aqui. O
  sistema começa na demanda e retoma no resultado (CP2).
- **Compras por unidade:** a compra pública é feita pelo órgão (a secretaria). As unidades pedem, e o setor de compras atende a rede.

## Consequências

- O fornecedor passa a existir como cadastro. Na CP3, o recebimento definitivo liga a entrada no Estoque ao fornecedor por id; até lá, o
  texto livre do ES1 continua.
- A CP2 começa da solicitação cotada, com o preço de referência de cada item.
- **Ainda sem tela de compras:** a ordem de fornecimento, o recebimento e o painel (CP3) e os alertas de contrato (CP2).

## Testes

- **JUnit `ComprasControllerTest`:**
  - o documento inválido (400) e o repetido (409);
  - as certidões até ficar regular, e a do FGTS da pessoa física (400);
  - as regras das sanções, o impedimento e a revogação;
  - o documento que não muda (422);
  - a solicitação do rascunho à cotada, com a devolução e o motivo (400);
  - as regras das cotações (fonte, data, fornecedor impedido), a remoção, a justificativa (422) e a mediana;
  - o cancelamento e a rejeição;
  - a sugestão pelo estoque mínimo, que some quando o material entra numa solicitação.
- **JUnit `ComprasAutorizacaoControllerTest`:**
  - o almoxarife pede na sua unidade e não na de outra;
  - Compras não pede, só vê depois do envio e é quem analisa;
  - aprovada, a unidade não cancela;
  - o CPF sai mascarado para quem só pede;
  - a recepção recebe 403; sem login, 401.
- **Robot de API:** `test/compras/Compras.robot`, CT-001 a CT-006.
- **Robot de tela:** `test/ui/administrativo/compras/UI_compras.robot`, CT-001 a CT-004 (menu, nova solicitação, cotação e conclusão,
  fornecedor com certidão).
