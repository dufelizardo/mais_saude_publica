# 0220 — Financeiro: centros de custo e apuração mensal (F3)

## Status

Aceita e implementada. Terceira e última fatia do domínio 17; o desenho está na [ADR-0218](./0218-financeiro-dotacao-e-empenho.md). Com ela,
o Financeiro fica completo.

## Contexto

- **A F1 e a F2 acompanham a despesa** da dotação ao pagamento. Falta responder quanto custa cada unidade e cada atendimento.
- **O Programa Nacional de Gestão de Custos (PNGC/MS)** é a referência do SUS para o custo de unidade de saúde:
  - centros de custo **produtivos** (assistência), de **apoio** e **administrativos**;
  - o custo direto de cada centro;
  - depois, o rateio dos centros de apoio e administrativos para os produtivos.
- **A plataforma já registra quase todo o custo direto,** cada parte num domínio:
  - a folha do RH, com a lotação vigente;
  - as saídas de consumo do Estoque, com o custo do lote e o setor de destino;
  - as dispensações e administrações da Farmácia;
  - as liquidações da F2;
  - as ordens de serviço de manutenção (PM2);
  - o valor contábil dos bens tombados (PM3);
  - as diárias do TFD.
- **A produção também está registrada:**
  - os atendimentos concluídos, que alimentam o BI1;
  - os pacientes-dia do censo diário (ADR-0185).

## Decisão

1. **Centro de custo:**
   - **O geral da unidade:**
     - cada unidade ativa tem um, sem setor;
     - nasce na primeira leitura dos centros ou na apuração, sem rotina (ADR-0209);
     - recebe o custo que não tem setor e não se desativa (422).
   - **O centro do setor:**
     - um por setor (409);
     - sem nome, recebe o nome do setor;
     - sem tipo, recebe o tipo do setor: assistencial é produtivo; apoio e técnico são apoio; administrativo é administrativo.
   - **O tipo do geral** segue a unidade:
     - a gestão (federal, estadual, municipal, regional) é administrativa;
     - o laboratório é apoio;
     - as demais são produtivas.
   - **O código** é `CC-00001`, em sequência.
   - O nome, o tipo e o ativo se alteram; o centro do setor inativo deixa o custo ir para o geral.
2. **Apuração mensal por competência** (AAAA-MM):
   - só do mês já encerrado (422);
   - cada custo vai ao centro do setor de onde saiu, quando o setor tem centro ativo; senão, ao geral da unidade.

   | Categoria | De onde vem |
   |-----------|-------------|
   | Pessoal | A folha da competência (`MM/AAAA` no RH), na unidade da lotação vigente no mês |
   | Material de consumo | As saídas de consumo do Estoque no mês, vezes o custo do lote, no setor de destino |
   | Medicamentos | As dispensações e administrações da Farmácia (menos os estornos), pelo preço do contrato do lote recebido de Compras (mesmo medicamento, número de lote e unidade) |
   | Serviços de terceiros | As liquidações do mês: a parte de serviço da ordem de fornecimento, na unidade da ordem; a ordem de serviço por contrato inteira, no setor do bem |
   | Manutenção | As ordens de serviço internas concluídas no mês: a mão de obra e as peças compradas |
   | Depreciação | A parcela do mês dos bens tombados, pela conta do valor contábil (linear, MCASP, ADR-0217), no setor do bem; o bem baixado antes do mês fica de fora |
   | Diárias do TFD | As liquidadas no mês, na unidade que pediu o transporte |
   | Outras | As liquidações avulsas do mês, no centro informado |

   - **Sem contar duas vezes:**
     - o material e o medicamento comprados entram pelo consumo, não pela liquidação do recebimento;
     - o bem permanente entra pela depreciação;
     - a peça tirada do Estoque pela ordem de serviço já está no consumo.
   - **Sem custo:** a saída de lote sem custo e o medicamento sem recebimento de Compras ficam fora do total e são contados à parte, como
     **saídas sem custo**.
   - **Não alocado:** a folha sem lotação na competência e a avulsa antiga sem centro.
   - **A produção da unidade é guardada com a apuração:**
     - os atendimentos concluídos;
     - os pacientes-dia do censo vigente, sem os reabertos.
3. **Aberta e fechada:**
   - a apuração aberta pode ser refeita: apaga as linhas e apura de novo;
   - fechada, vira fotografia e não muda mais (422). Assim os números não mudam com um lançamento atrasado.
4. **Indicadores da competência,** a partir da apuração guardada (404 sem apuração):
   - o custo total e por categoria de cada unidade;
   - o **custo por atendimento** e o **custo por paciente-dia** (nulos sem produção);
   - a **média do tipo de unidade** no custo por atendimento, com o destaque da unidade mais de 30% acima dela.

   Quem só tem `INDICADOR.CONSULTAR` vê as unidades do próprio acesso; a média é sempre da rede.
5. **Liquidação avulsa:** passa a pedir o centro de custo, que precisa existir (404) e estar ativo (422).
6. **Acesso:**

   | Permissão | O que libera |
   |-----------|--------------|
   | `FINANCEIRO.CONSULTAR` | Os centros, as apurações e os indicadores |
   | `FINANCEIRO.GERENCIAR` | Criar e alterar os centros, apurar e fechar |
   | `INDICADOR.CONSULTAR` | Os indicadores de custo, nas unidades do acesso |

7. **Tela:**
   - no Financeiro, a aba **Custos** traz:
     - a competência, com os botões apurar, refazer e fechar;
     - o resumo: total, não alocado e saídas sem custo;
     - o custo por unidade, com o custo por atendimento e o destaque acima da média;
     - o custo por centro e categoria;
     - os centros de custo, com o novo centro do setor e a alteração;
   - a liquidação avulsa escolhe o centro;
   - na Sala de Situação, o cartão **Custo por unidade** mostra a competência anterior, quando apurada.
8. **Banco:**
   - a `V101__financeiro_centros_de_custo_e_apuracao.sql` cria `tb_centro_custo`, `tb_apuracao_custo`, `tb_linha_apuracao_custo` e
     `tb_producao_apuracao_custo`, e põe o `centro_custo_id` em `tb_liquidacao`;
   - as chaves do centro para a unidade e o setor apagam em cascata. A unidade e o setor não se apagam na plataforma, só se inativam;
     a cascata existe porque o geral nasce para toda unidade ativa e não pode travar a remoção de uma unidade sem histórico.

## Alternativas descartadas

- **Calcular a apuração na leitura, sem guardar:**
  - os números mudariam com cada lançamento atrasado;
  - a competência fechada não seria fotografia;
  - cada leitura percorreria todas as fontes do mês.
- **Rateio dos centros de apoio e administrativos já nesta fatia,** a segunda etapa do PNGC:
  - pede um critério por centro (área, pessoas, requisições), que o município precisa definir;
  - fica para depois. O custo direto por centro já responde ao que se pediu.
- **Teto financeiro da PPI contra o executado,** previsto no desenho da ADR-0218:
  - a cota da PPI (ADR-0179) está em quantidade de procedimentos;
  - o valor depende da tabela SIGTAP, que é do domínio 20;
  - fica para lá.
- **Medicamento pelo preço médio de todas as compras:**
  - o preço do contrato do lote recebido é o custo real da saída;
  - o lote sem recebimento aparece como saída sem custo, em vez de um preço inventado.

## Consequências

- **O domínio 17 fica completo:** orçamento, despesa e custo.
- **Primeiro número de custo do SUS municipal na plataforma:**
  - o custo por atendimento e por paciente-dia por unidade;
  - a comparação entre unidades do mesmo tipo.
- **Pendências** (o rateio, a lotação por setor e a unidade mista foram feitos na F4, [ADR-0221](./0221-financeiro-rateio-pngc.md)):
  - o rateio do PNGC (apoio e administração para os produtivos);
  - o teto financeiro da PPI pela tabela SIGTAP (domínio 20);
  - o envio ao ApuraSUS;
  - a lotação por setor no RH, para a folha ir ao centro do setor;
  - a unidade mista (hospital com ambulatório), que precisa do rateio para separar o custo por atendimento do custo por paciente-dia.

## Testes

- **JUnit `CustosControllerTest`:**
  - o geral de cada unidade, produtivo na UBS;
  - o centro do setor com o nome e o tipo do setor, o código, o repetido (409), o setor inexistente (404) e o corpo sem setor (400);
  - o geral não se desativa (422); a alteração do centro do setor.
  - a apuração:
    - o mês corrente (422), o formato (400) e a competência não apurada (404);
    - a folha pela lotação, o consumo no centro do setor, a saída sem custo e a depreciação de 90 no mês;
    - refazer a aberta;
    - os indicadores, com o custo por atendimento e o nulo sem produção;
    - fechar, fechar de novo (422) e apurar a fechada (422).
- **JUnit `LiquidacaoControllerTest`:** a avulsa sem centro (400), com centro inexistente (404) e com o centro na resposta.
- **JUnit `LiquidacaoAutorizacaoControllerTest`, com a autorização ligada:**
  - o gestor vê os centros e os indicadores, mas não cria nem apura (403);
  - o Compras não apura (403);
  - o profissional sem permissão não vê os indicadores (403);
  - o Financeiro cria e apura;
  - sem login, 401.
- **Robot de API:**
  - `test/financeiro/Custos.robot`, CT-001 (centros) e CT-002 (apuração da competência);
  - `Liquidacao.robot`, CT-002, com a avulsa pelo centro.
- **Robot de tela:** `UI_financeiro.robot`, CT-006 (a aba Custos apura e cria o centro do setor).
