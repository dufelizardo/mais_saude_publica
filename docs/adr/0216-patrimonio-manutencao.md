# 0216 — Patrimônio e Manutenção: planos, chamados e ordens de serviço (PM2)

## Status

Aceita e implementada. Segunda fatia do domínio 15; o desenho está na [ADR-0215](./0215-patrimonio-bens-tombamento-e-transferencia.md).

## Contexto

- **A PM1 deixou o bem tombado e o vínculo com o veículo da frota,** mas a situação "em manutenção" do bem não tinha registro nenhum.
- **A frota marcava o veículo em manutenção à mão** (ADR-0178), sem o que foi feito nem o custo.
- **A RDC 509/2021 pede um plano de manutenção** para os equipamentos de saúde (preventiva, calibração, segurança elétrica) e o
  registro de cada intervenção.
- **Quem percebe o defeito é quem usa o bem:** o enfermeiro, o recepcionista, o motorista. Essas pessoas não têm permissão de
  Patrimônio nem de manutenção.
- **As peças são materiais do Estoque** (ADR-0177). A equipe de manutenção não deve ganhar `ESTOQUE.MOVIMENTAR` só por causa delas.

## Decisão

1. **Plano de manutenção, por bem:**
   - o tipo é preventiva, calibração ou segurança elétrica. A corretiva não tem plano (400);
   - a periodicidade vai de 1 a 1825 dias, com a última execução opcional (não futura, 400). A próxima data conta dela, ou de hoje;
   - um plano ativo por tipo (409), só do bem tombado e em operação (422);
   - o plano se desativa (o inativo de novo dá 422), e as ordens já abertas continuam.
2. **Ordem do plano vencido:**
   - quando o plano vence, a ordem nasce na leitura das ordens, do resumo ou da manutenção do bem, sem rotina agendada (ADR-0209);
   - uma por vencimento: não nasce outra enquanto houver ordem do plano aberta desde a data prevista;
   - a conclusão conta a próxima data a partir de hoje. O cancelamento também, para o plano não ficar vencido sem ordem.
3. **Chamado corretivo:**
   - qualquer pessoa logada abre o chamado do bem tombado e em operação (422), com o defeito e se o bem parou;
   - o bem parado começa a contar o tempo parado;
   - quem abriu acompanha em **"Meus chamados"** e lê a própria ordem.

   O chamado e a ordem da corretiva são o mesmo registro, para não duplicar o fluxo.
4. **Ordem de serviço:** o número é `AAAA/000001`. As situações são aberta → em execução → concluída, ou cancelada.
   - **Triagem:** a prioridade (baixa, normal, alta, urgente), só na aberta (422).
   - **Início,** só da aberta (422):
     - a execução é **interna**, que pede o técnico (400), ou **por contrato**: uma ata ou um contrato (400) vigente (422) com item de
       serviço (422) da CP2;
     - o bem fica **em manutenção** e guarda a situação de antes. Uma ordem sem parada informada passa a contar o tempo parado no
       início;
     - o **veículo** ligado ao bem também vai para manutenção. O veículo em serviço não entra (422).
   - **Conclusão,** só da ordem em execução (422):
     - o serviço feito e o custo de mão de obra;
     - na **calibração**, o número do certificado e a validade futura (400);
     - as **peças**: do Estoque, escolhendo o material (404), ou trazidas pelo contrato e descritas (400). A peça do Estoque **sai do
       saldo da unidade do bem** pelo FEFO, por um método interno que respeita a reserva das emergências e o inventário aberto (sem
       saldo, 422);
     - o bem volta à situação de antes e o veículo volta a disponível, se não houver outra ordem em execução no mesmo bem.
   - **Cancelamento:** da aberta ou da em execução (422), com o motivo. Na execução, devolve o bem e o veículo.
5. **Custos e tempo parado:**
   - o custo da ordem é a mão de obra mais as peças com custo informado;
   - os dias parado contam da parada até a conclusão (ou até hoje);
   - a ficha do bem soma o custo e os dias parado das ordens e mostra a validade do último certificado de calibração, com o alerta de
     calibração vencida (certificado vencido ou plano vencido).
6. **Resumo:**
   - as ordens abertas, em execução e com o bem parado;
   - os planos vencendo em 30 dias ou vencidos;
   - as calibrações vencidas.
7. **Contratos de serviço:** a manutenção lista as atas e os contratos vigentes com item de serviço, para escolher a execução por
   contrato sem a permissão de Compras.
8. **Acesso:**

   | Permissão | Papéis padrão | O que libera |
   |-----------|---------------|--------------|
   | `MANUTENCAO.EXECUTAR` | Manutenção (novo), Gestor | Planos, triagem, início, conclusão e cancelamento, na unidade do bem; ordens, resumo e contratos de serviço das unidades no escopo |
   | (login) | Todos | Abrir o chamado, "Meus chamados" e ler a própria ordem |

   - O papel **Manutenção** também consulta o Patrimônio e o Estoque.
   - A manutenção na ficha do bem é lida por quem vê o bem (Patrimônio ou Manutenção da unidade) ou pelo responsável.
9. **Tela,** no Patrimônio:
   - a aba **Manutenção** traz o resumo, os planos vencendo, o aviso de calibração vencida e as ordens com a gaveta da ordem (triagem,
     início, conclusão com as peças, cancelamento). A tela aceita `?aba=manutencao`;
   - a **ficha do bem** ganha o chamado (para quem pode abrir), os planos com a criação e a desativação, as ordens, o custo acumulado,
     os dias parado e a calibração;
   - a aba **Meus bens** ganha **"Meus chamados"**;
   - o menu mostra o Patrimônio também para quem tem só `MANUTENCAO.EXECUTAR`.
10. **Banco:** a `V97__patrimonio_manutencao.sql` cria `tb_plano_manutencao`, `tb_ordem_servico_manutencao` e `tb_peca_ordem_servico`.

## Alternativas descartadas

- **Chamado em tabela própria:** o chamado vira ordem na triagem, sempre. Duas tabelas só repetiriam o defeito, o bem e quem abriu.
- **Rotina diária para os planos vencidos:** o vencimento é derivado da data. Gerar na leitura evita a rotina e a trava entre réplicas
  (ADR-0209), como os atrasos de Compras (ADR-0214).
- **Dar `ESTOQUE.MOVIMENTAR` à manutenção:** ela ganharia entrada, perda e ajuste em todo o estoque. A saída da peça é efeito da
  conclusão da ordem, como a entrada pelo recebimento da CP3.
- **Abrir chamado só com a permissão de Patrimônio:** quem usa o bem e vê o defeito ficaria sem caminho. O chamado não altera o bem.
  Quem abre só acompanha a própria ordem.

## Consequências

- **O bem e o veículo têm o histórico de manutenção** com o custo e o tempo parado, que a PM3 leva ao painel.
- **A frota deixa de depender da marcação à mão** quando o veículo é um bem: a ordem de serviço põe e tira o veículo da manutenção.
- **Pendências:**
  - o alerta de equipamento parado na Sala de Situação e o painel consolidado ficam para a PM3;
  - a liquidação do contrato de serviço é do domínio 17;
  - o certificado de calibração fica em texto até a D2 de Documentos.

## Testes

- **JUnit `ManutencaoControllerTest`:**
  - o chamado com o bem parado; a triagem; o início interno sem técnico (400); o bem em manutenção que não muda de uso à mão e a
    triagem fora da aberta (422);
  - a conclusão: peça sem material nem descrição (400), acima do saldo (422); o custo, os dias parado, as peças, o bem de volta ao uso,
    a saída do Estoque e o cancelamento da concluída (422);
  - o plano: corretiva e última execução futura (400), o plano vencido, o repetido (409), a ordem gerada uma só vez, a calibração
    vencida no resumo, o certificado e a validade (400), a próxima data, a calibração na ficha e a desativação (repetida, 422);
  - o contrato: os contratos de serviço, o início sem contrato (400), o veículo em manutenção e o cancelamento que devolve o bem e o
    veículo.
- **JUnit `ManutencaoAutorizacaoControllerTest`:**
  - quem tem só login abre o chamado e o acompanha, mas não tria, não lista as ordens, não lê a manutenção do bem e não cria plano
    (403);
  - a manutenção de outra unidade não lê nem inicia (403);
  - a da unidade inicia, lê a manutenção do bem e cria o plano; o gestor lê; sem login, 401.
- **Robot de API:** `test/patrimonio/Manutencao.robot`, CT-001 a CT-004.
- **Robot de tela:** `UI_patrimonio.robot`, CT-005 (chamado pela ficha) e CT-006 (ordem de serviço pela aba Manutenção).
