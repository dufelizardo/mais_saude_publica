# 0218 — Financeiro: desenho do domínio, dotação e empenho (F1)

## Status

Aceita e implementada (F1). Desenho do domínio 17 e primeira fatia. A F2 (liquidação, pagamento e acompanhamento) está na
[ADR-0219](./0219-financeiro-liquidacao-e-pagamento.md); a F3 (centros de custo e apuração) está na
[ADR-0220](./0220-financeiro-centros-de-custo-e-apuracao.md), que deixa o teto financeiro da PPI para o domínio 20 (tabela SIGTAP).

## Contexto

- **O domínio 17 tinha uma decisão em aberto** (MAPA, STATUS): um sistema financeiro completo ou o registro e o acompanhamento ao lado
  de um sistema externo.
- **O Decreto 10.540/2020 exige um sistema único de execução orçamentária, financeira e contábil por ente (o SIAFIC).** O lançamento
  contábil, a execução formal e a prestação de contas ao SIOPS ficam nele, e o município não pode ter dois.
- **Compras guardava o empenho em texto** na ordem de fornecimento (ADR-0214). Nada conferia se havia dotação ou saldo para a ordem.
- **A Lei 4.320/1964** exige o empenho prévio (art. 60) e veda a despesa sem o empenho. Ela também define os tipos ordinário,
  estimativo e global (art. 60, §§ 2º e 3º).

## Decisão

### O domínio: acompanhamento, não contabilidade

1. **A plataforma acompanha a execução do Fundo Municipal de Saúde do ponto de vista da saúde.**
   - Ela espelha a dotação e o empenho com os números do SIAFIC.
   - Ela liga o empenho à compra e ao atesto e apura o custo das unidades.
   - O lançamento contábil, o SIOPS e o cálculo da aplicação mínima (LC 141/2012) ficam no SIAFIC.
   - A integração automática com o SIAFIC entra no domínio 20, sem travar o domínio: até lá, os números são digitados.
2. **Fatias:**

   | Fatia | Conteúdo |
   |-------|----------|
   | **F1** (esta) | Dotação com suplementação e anulação; empenho (ordinário, estimativo, global) com reforço e anulação; a ordem de fornecimento que consome o saldo do empenho |
   | **F2** | Liquidação a partir do atesto (recebimento definitivo, ordem de serviço por contrato), com as retenções; pagamento com a ordem bancária; restos a pagar; diárias e ajuda de custo do TFD; painel da execução e a ordem cronológica de pagamentos |
   | **F3** | Centro de custo (unidade e setor); apuração mensal do custo (estoque, manutenção, contratos, depreciação, pessoal); custo por atendimento; teto financeiro da PPI comparado ao executado |

### F1

3. **Dotação do exercício:**
   - o programa de trabalho (`10.301.0010.2045`) e a natureza da despesa (`3.3.90.30`), no formato (400);
   - a fonte: recursos próprios, bloco de Manutenção, bloco de Estruturação (Portaria GM/MS 3.992/2017), transferência estadual ou
     outras, com o código do SIAFIC;
   - o valor da lei;
   - o exercício anterior, o atual ou o seguinte (400); única pelo programa, pela natureza e pela fonte no exercício (409).
4. **Alterações:**
   - a suplementação e a anulação têm o ato (decreto ou lei) e a data do exercício, não futura (400);
   - a anulação vai até o disponível (422);
   - o **autorizado** é a lei mais as suplementações menos as anulações. O **disponível** é o autorizado menos o empenhado.
5. **Empenho:**
   - o número do SIAFIC, único no exercício (409);
   - o tipo, a dotação e a data do exercício da dotação, não futura (400);
   - o **fornecedor ativo e não impedido** (422);
   - a ata ou o contrato, se houver, do mesmo fornecedor (422);
   - o valor dentro do disponível da dotação (422).
6. **Reforço e anulação do empenho:**
   - o reforço fica dentro do disponível da dotação (422);
   - a anulação vai até o saldo do empenho (422);
   - o **valor atual** é o inicial mais os reforços menos as anulações. O **comprometido** são as ordens de fornecimento ligadas a ele
     (a cancelada conta só o aceito). O **saldo** é o atual menos o comprometido.
7. **Ordem de fornecimento** (Compras, ADR-0214):
   - quem compra escolhe entre os empenhos com saldo do fornecedor, sem outro instrumento;
   - o empenho fica ligado à ordem, e o número em texto vem dele;
   - a ordem acima do saldo do empenho dá 422;
   - o cancelamento devolve ao saldo o que não foi aceito;
   - o número digitado continua para as ordens antigas e para quem ainda não registra o empenho.
8. **Acesso:**

   | Permissão | Papéis padrão | O que libera |
   |-----------|---------------|--------------|
   | `FINANCEIRO.CONSULTAR` | Financeiro (novo), Gestor, Compras | Dotações e empenhos |
   | `FINANCEIRO.GERENCIAR` | Financeiro | Registrar a dotação, as alterações, o empenho, o reforço e a anulação |
   | `COMPRAS.GERENCIAR` | Compras (e o Financeiro) | Os empenhos com saldo para a ordem de fornecimento |

   O papel **Financeiro** também tem `COMPRAS.GERENCIAR`, para escolher o fornecedor e o contrato no empenho. O acesso é da rede inteira,
   porque o Fundo é um só.
9. **Tela Financeiro** (Administrativo):
   - o exercício escolhido no cabeçalho e o resumo (autorizado, empenhado, disponível);
   - as abas **Dotações** e **Empenhos**, com as gavetas da dotação (alterações) e do empenho (ordens, reforços e anulações);
   - em Compras, a nova ordem ganha a escolha do empenho.
10. **Banco:** a `V99__financeiro_dotacao_e_empenho.sql` cria `tb_dotacao_orcamentaria`, `tb_alteracao_dotacao`, `tb_empenho` e
    `tb_movimento_empenho`. Também acrescenta `empenho_id` em `tb_ordem_fornecimento`.

## Alternativas descartadas

- **Sistema financeiro completo, com o lançamento contábil:** o município teria dois sistemas de execução, contra o Decreto 10.540/2020,
  e a contabilidade da saúde ficaria fora da do ente.
- **Só o número do empenho em texto:** a plataforma não saberia se a ordem cabe no empenho nem quanto da dotação foi comprometido.
- **Empenho obrigatório na ordem desde já:** quem ainda não registra os empenhos ficaria sem emitir ordens. O texto continua até o
  município decidir.

## Consequências

- **A compra fica amarrada ao orçamento:** da dotação ao empenho e à ordem, com o saldo conferido em cada passo.
- **A F2 parte do empenho:** a liquidação abate o empenho e alimenta o painel da execução.
- **Pendências:** a importação do SIAFIC e o SIOPS (domínio 20); o empenho na ordem de serviço por contrato da manutenção (F2, junto
  com a liquidação).

## Testes

- **JUnit `FinanceiroControllerTest`:**
  - a dotação: exercício fora, programa e natureza fora do formato (400), a repetida (409);
  - a suplementação e a anulação: acima do disponível (422), data futura (400), o autorizado e o disponível;
  - o empenho: acima do disponível (422), de outro exercício (400), o número repetido (409), o contrato de outro fornecedor (422) e os
    empenhos para a ordem;
  - a ordem: o empenho ligado, o comprometido, o saldo, a ordem acima do saldo (422);
  - os movimentos: a anulação acima do saldo e o reforço acima do disponível (422); o cancelamento da ordem que devolve o saldo.
- **JUnit `FinanceiroAutorizacaoControllerTest`:**
  - só o Financeiro registra a dotação (o gestor e o Compras, 403);
  - o gestor e o Compras consultam; o Compras vê os empenhos para a ordem;
  - outro profissional recebe 403; sem login, 401.
- **Robot de API:** `test/financeiro/Financeiro.robot`, CT-001 a CT-004.
- **Robot de tela:** `test/ui/administrativo/financeiro/UI_financeiro.robot`, CT-001 a CT-003.
