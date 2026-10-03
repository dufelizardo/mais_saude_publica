# 0095 — Laboratório assistencial: resultados no prontuário, laudo e recoleta

## Status

Aceita e implementada (parte 3 de 3). Fecha o primeiro ciclo do domínio #10. Usa a API da
[ADR-0093](./0093-laboratorio-backend.md) e a tela da [ADR-0094](./0094-laboratorio-tela.md).

## Contexto

- Depois da ADR-0094, o resultado liberado só aparecia no detalhe do pedido, na tela Laboratório.
- Quem atende o paciente olha o **prontuário** e o **atendimento**. Nenhum dos dois mostrava os exames.
- Não havia como entregar ou arquivar o resultado em papel.
- Na rejeição de amostra, o exame voltava para **Para coletar** igual a um pedido novo. Quem coleta não
  sabia que era recoleta, nem o motivo.

## Decisão

1. **Exames no prontuário:** a resposta de `GET /api/v1/prontuario/{pacienteId}` ganha `exames`.
   - São todos os exames pedidos para o paciente, do pedido mais recente ao mais antigo.
   - Cada um traz: pedido, atendimento de origem, exame, material, situação, prioridade, data, unidade e
     profissional que pediu.
   - O **resultado só sai depois de liberado**. Resultado registrado e ainda não revisado não aparece fora do
     laboratório.
   - **Sem endpoint novo:** o prontuário já exige vínculo assistencial ou acesso justificado (ADR-0076) e já
     é leitura auditada. O exame herda essa proteção.
2. **Tela de Atendimentos:**
   - A aba **Prontuário** mostra a seção **Exames laboratoriais** acima da linha do tempo (o pedido avulso não
     tem atendimento). Para cada exame: situação, valor, referência, interpretação, aviso de retificação e o
     link **Laudo**. Os cancelados ficam de fora.
   - O detalhe do atendimento ganha a seção **Exames**, com os exames pedidos naquele atendimento e o botão
     **+ Exames**, que abre o pedido já preenchido (ADR-0094).
   - As duas listas usam o mesmo componente (`shared/exames-paciente`).
3. **Laudo imprimível** em `/laudo/exame/{pedidoId}`:
   - fora do layout do sistema, com papel branco nos dois temas e A4 na impressão;
   - cabeçalho com os laboratórios das amostras, e os dados do paciente e do pedido;
   - **só os exames liberados**: valor, referência, interpretação, amostra, coleta, quem liberou e quando,
     e o motivo quando há retificação;
   - avisa quantos exames do pedido ainda não estão liberados;
   - **sem assinatura digital**: é o navegador que imprime ou salva em PDF, e a validade é a do registro no
     sistema;
   - lê o pedido pelo mesmo detalhe auditado da ADR-0093;
   - o link aparece no detalhe do pedido (**Imprimir laudo**, quando há liberado) e em cada exame liberado do
     prontuário e do atendimento.
4. **Recoleta indicada:**
   - na lista de trabalho **Para coletar**, o item ganha `motivoRecoleta`;
   - a regra: o exame aguardando coleta é recoleta quando o pedido tem amostra rejeitada do mesmo material,
     e vale o motivo da rejeição mais recente. A amostra é uma por material, e a rejeição solta os exames
     dela;
   - **sem migração**, porque a informação sai das amostras rejeitadas, que já ficam guardadas;
   - na tela, a linha mostra **Recoleta · motivo** e a gaveta de coleta avisa que a amostra anterior foi
     rejeitada.
5. Rótulos e formatação do laboratório (material, situação, interpretação, faixa e valor) passam para
   `core/models/laboratorio.ts`, para que a tela, o prontuário e o laudo mostrem o mesmo texto.

## Consequências

- O resultado chega a quem cuida do paciente pelo prontuário, com o mesmo controle de acesso e a mesma
  trilha de auditoria dos outros registros clínicos.
- O laudo em papel sai do sistema sem integração externa.
- **Continuam fora do escopo** (em `PENDENCIAS.md`):
  - laudo PDF assinado digitalmente (ICP-Brasil);
  - envio do resultado ao paciente;
  - gráfico da evolução de um exame no tempo;
  - aviso ao solicitante quando o resultado é liberado.

## Testes

- **JUnit** (`PedidoExameControllerTest`):
  - o prontuário traz os exames e o resultado só depois da liberação;
  - a recoleta vem com o motivo na lista de coleta, e o pedido sem rejeição vem sem motivo.
- **Robot de API** (`test/laboratorio/pedido_exame`):
  - CT-016, recoleta com motivo;
  - CT-017, exames no prontuário com resultado só depois de liberado;
  - schema do item da lista de trabalho com `motivoRecoleta` e o schema novo do exame no prontuário.
- **Robot de interface** (`UI_laboratorio.robot`):
  - CT-009, recoleta indicada na coleta;
  - CT-010, laudo imprimível;
  - CT-011, exame liberado no prontuário e no atendimento.
