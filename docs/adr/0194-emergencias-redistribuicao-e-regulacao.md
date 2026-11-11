# 0194 — Emergências e Desastres: onde tem na rede, compromissos nas unidades afetadas e prioridade na Regulação (E4b)

## Status

Aceita e implementada. Segunda metade da fatia **E4** do domínio 27 ([ADR-0113](./0113-emergencias-e-desastres.md); detalhe em
[`emergencias/MODELO-EMERGENCIAS.md`](../emergencias/MODELO-EMERGENCIAS.md)), depois da E4a
([ADR-0193](./0193-emergencias-vulneraveis-e-deslocados.md)). Com ela, o domínio 27 fica completo.

## Contexto

- **A ADR-0113 deixou para a E4 a redistribuição pela Farmácia e a Regulação** (redirecionamentos e prioridades decididos), sempre com a
  coordenação apontando e o dono executando.
- **A E3 liga a mobilização de medicamento a uma transferência da Farmácia** ([ADR-0158](./0158-emergencias-coordenacao.md)); a ES3 deu
  ao insumo o material do catálogo e a reserva ([ADR-0191](./0191-estoque-nas-emergencias.md)). Faltava saber onde há o recurso.
- **As unidades afetadas fora de operação** (situação operacional, [ADR-0101](./0101-equipamentos-de-saude-backend.md)) têm
  agendamentos e regulações marcados que precisam ser movidos.
- **A fila da Regulação** ([ADR-0087](./0087-regulacao-do-acesso-backend.md)) ordena pela cor e pela hora do pedido.

## Decisão

1. **Onde tem na rede** (`GET emergencia/{id}/disponibilidade`, com o medicamento ou o material, um só; 400 nos outros casos):
   - **Por unidade:** o saldo dentro da validade (o lote incorporado e o vencido ficam de fora), a próxima validade e a situação
     operacional;
   - **No material,** o reservado para emergências e o livre;
   - **A ordem:** as afetadas pela última avaliação de impacto vão para o fim; as demais, as de mais saldo livre primeiro.
   - **A mobilização de medicamento** ganha o medicamento do catálogo (só nela; outro tipo, 422), como o insumo ganhou o material na ES3.
     A gaveta mostra o "onde tem" ao escolher. A execução continua sendo a transferência da Farmácia, ligada como na E3.
2. **Compromissos nas unidades afetadas** (`GET emergencia/{id}/rede`, calculado):
   - **Agendamentos** agendados ou confirmados;
   - **Solicitações reguladas** autorizadas ou agendadas com a executante afetada.
   - **Quando:** nos próximos 30 dias, nas unidades da última avaliação de impacto que estão fora de operação.
   - **Quem redireciona** é o dono: a tela leva à Agenda ou à Regulação. Não há remarcação automática.
3. **Prioridade da emergência na Regulação** (`tb_solicitacao_regulacao.emergencia_id`):
   - **Quem marca:** a coordenação, com a emergência ativa (422).
   - **Qual:** a solicitação em aberto (solicitada, devolvida, autorizada ou agendada; as outras, 422) de paciente deslocado nesta
     emergência ou morador da área afetada (422).
   - **Já marcada:** por esta ou por outra emergência aberta, 409.
   - **Efeito:** na fila, sobe dentro da mesma cor e aparece com a marca "Emergência". **A decisão continua com o regulador:** a marca não
     autoriza nem muda a cor.
   - **Retirar:** só a marca desta emergência (422); vale também na desmobilização.
   - **Encerrada a emergência,** a marca deixa de valer (a fila volta à ordem de sempre) e fica no registro para o relatório.
   - **Para escolher,** a coordenação lista as solicitações em aberto do paciente (`GET emergencia/{id}/regulacoes?pacienteId`).
4. **Relatório pós-evento:** as solicitações reguladas priorizadas.
5. **Acesso:**
   - o "onde tem" pede `EMERGENCIA.COORDENAR` (não tem paciente);
   - os compromissos, as regulações do paciente e a prioridade pedem também `PACIENTE.CONSULTAR`.
6. **Telas:**
   - **Emergência:**
     - na gaveta de mobilizar, o medicamento da Farmácia e o "onde tem" (também para o material);
     - na seção Pessoas, "Ver compromissos" (com o atalho para a Agenda e a Regulação) e as regulações priorizadas, com "Retirar";
     - no deslocado, "Priorizar regulação".
   - **Regulação:** a marca "Emergência" na fila.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `emergencia/{id}/disponibilidade?medicamentoId` ou `?materialId` | `EMERGENCIA.COORDENAR` |
| GET | `emergencia/{id}/rede`, `emergencia/{id}/regulacoes?pacienteId` | `EMERGENCIA.COORDENAR` e `PACIENTE.CONSULTAR` |
| POST, DELETE | `emergencia/{id}/regulacao/{solicitacaoId}/prioridade` | idem |

A mobilização aceita `medicamentoId`; o resumo da solicitação regulada ganha `emergenciaId` e `emergenciaNome` (enquanto aberta).

### Migração

`V78__emergencias_redistribuicao_e_regulacao.sql`: `medicamento_id` em `tb_mobilizacao` e `emergencia_id` em `tb_solicitacao_regulacao`.

## Alternativas descartadas

- **A coordenação muda a cor da regulação:** a cor é decisão clínica do regulador; a emergência só pede prioridade dentro dela.
- **Remarcar a agenda das unidades afetadas automaticamente:** cada caso tem um destino diferente; a lista aponta, a Agenda e a Regulação
  decidem.
- **Transferência da Farmácia criada pela coordenação:** a Farmácia escolhe o lote; a E3 já liga a transferência feita.

## Consequências

- **STATUS-DOS-DOMINIOS:** o domínio 27 fica completo.
- **Implantação:** sem permissão nova.
- **Fora do escopo:** o remanejamento de cotas da PPI entre executantes (Gestão da Rede, GR2/GR3); o reagendamento em lote.

## Testes

- **JUnit `RedeEmergenciaControllerTest`:**
  - **onde tem:** sem filtro e com os dois (400); o vencido de fora, a afetada no fim, a reserva descontada; a mobilização com o
    medicamento (outro tipo, 422);
  - **compromissos:** o agendamento e a regulação da UBS inoperante, sem os do hospital nem os passados;
  - **prioridade:**
    - de quem não é da emergência e da solicitação encerrada (422);
    - a lista do paciente, priorizar e de novo (409);
    - a fila com a da emergência no alto da mesma cor e a marca;
    - retirar a de outra (422), retirar e pôr de novo;
    - o relatório;
    - encerrada, a fila volta à ordem sem a marca.
- **JUnit `RedeEmergenciaAutorizacaoControllerTest`,** com a segurança ligada: a coordenação vê os compromissos e as regulações do
  paciente; a recepção não vê nem prioriza; o almoxarife não vê o onde tem.
- **Robot:** `RedeEmergencia.robot` (API, CT-001 e CT-002) e CT-015 de `UI_emergencias.robot`.
