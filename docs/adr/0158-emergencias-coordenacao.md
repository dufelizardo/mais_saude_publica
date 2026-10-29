# 0158 — Emergências e Desastres: ações, mobilização e capacidade emergencial (E3)

## Status

Aceita e implementada. Terceira fatia do domínio 27, desenhado na [ADR-0113](./0113-emergencias-e-desastres.md)
(detalhe em [`emergencias/MODELO-EMERGENCIAS.md`](../emergencias/MODELO-EMERGENCIAS.md), seções 8 a 10). Usa a E1
([ADR-0156](./0156-emergencias-nucleo.md)) e a E2 ([ADR-0157](./0157-emergencias-contingencia.md)). Abre uma exceção
controlada na Escala ([ADR-0105](./0105-escalas-backend.md)).

## Contexto

- **O princípio da ADR-0113:** Emergências registra; o dono executa. A coordenação precisa de um quadro de ações, de
  saber o que foi mobilizado para onde e de ativar a capacidade extra das unidades.
- **A E2 já traz as ações previstas por nível,** mas só para leitura. Faltava acompanhar quem fez o quê e quando.
- **A Escala recusa (422) o profissional sem lotação na unidade do turno.** Numa emergência, a ADR-0113 manda que a
  mobilização autorize turnos fora da lotação, sem lotação falsa e sem desligar os alertas de jornada e descanso.
- **Estoque (13) e Transporte (16) não existem ainda.** Insumo, equipamento, veículo e espaço físico só podem ser
  registrados como necessidade.

## Decisão

1. **Ações emergenciais** (`TB_ACAO_EMERGENCIAL`):
   - **campos:** descrição, responsável (função, setor ou pessoa), unidade, prioridade (baixa, média, alta, urgente),
     prazo, situação, início, conclusão, motivo do cancelamento e observação;
   - **situações:** pendente → em execução → concluída, ou cancelada com o motivo (400). Concluída ou cancelada não muda
     (422);
   - **gerar do plano:** uma ação para cada ação prevista do nível atual nos planos ativos, com prioridade alta e o
     prazo contado do passo mais recente do plano (a ativação ou a mudança de nível). A ação guarda a ação prevista e a
     ativação de onde veio; a já gerada nesta emergência não se repete. Sem nada novo a gerar, 422;
   - **atrasada:** em aberto depois do prazo. O quadro põe as atrasadas no alto, depois as abertas por prioridade e prazo.
     A contagem aparece no detalhe, na lista das emergências e no aviso da Sala de Situação.
2. **Mobilização** (`TB_MOBILIZACAO`):
   - **campos:** tipo (profissional, medicamento, insumo, equipamento, veículo, leito, espaço físico), origem e destino
     (unidades), o profissional (no tipo profissional) ou o recurso, quantidade, responsável, período (início, padrão
     hoje, e fim opcional) e situação;
   - **situações:** solicitada → em andamento → concluída, ou cancelada com o motivo (400). Concluída ou cancelada não muda
     (422). O profissional já nasce em andamento; os demais, solicitados;
   - **profissional** (pela matrícula; desligado, 422; o mesmo profissional em aberto para o mesmo destino, 409):
     - **a exceção da Escala:** com a mobilização em andamento, dentro do período e com a emergência ativa, a Escala aceita
       turno do profissional na unidade de destino sem lotação lá. O turno guarda a mobilização
       (`tb_turno_escala.mobilizacao_id`) e aparece marcado "Mobilização" na tela da Escala;
     - **continua valendo:** desligamento, afastamento, sobreposição de turnos e os alertas de jornada e descanso;
     - **fim da exceção:** concluir ou cancelar a mobilização, passar do fim ou encerrar a emergência. Vale para turnos
       novos; os já designados ficam;
     - **a semana da Escala** passa a incluir quem está mobilizado para a unidade, para que a linha dele exista e receba
       turno;
   - **medicamento:** pode ser ligado a uma transferência da Farmácia ([ADR-0059](./0059-transferencia-de-estoque-entre-unidades.md))
     para a mesma unidade de destino (422 se for outra). Ligada, a situação segue a da transferência (em trânsito anda,
     recebida conclui, cancelada cancela) e não muda à mão (422);
   - **leito:** pode apontar a capacidade de leitos ativada nesta emergência (item 3);
   - **insumo, equipamento, veículo, espaço físico:** registram a necessidade (recurso obrigatório, 400). A execução é
     informada à mão até Estoque e Transporte existirem.
3. **Capacidade emergencial:**
   - **cadastro** (`TB_CAPACIDADE_EMERGENCIAL`, feito antes): unidade, recurso (atendimentos por dia, leitos,
     consultórios, outro), capacidade normal, quanto se pode ativar a mais, observação e ativa. Uma por unidade e recurso
     (409); unidade e recurso não mudam (422); o ativável não fica abaixo do que está ativado (422). Nos leitos, a resposta
     traz os leitos ativos da unidade (Leitos), para conferir o normal;
   - **ativação na emergência** (`TB_ATIVACAO_CAPACIDADE`): a quantidade, até o que resta do ativável somando todas as
     emergências (422), com a justificativa; capacidade inativa, 422. Desativar pede a justificativa; encerrar a emergência
     desativa o que estiver ativo.
4. **Unidades afetadas:** o detalhe mostra as unidades da última avaliação de impacto com a situação operacional atual
   (ADR-0101). A situação muda em Equipamentos de Saúde, sem estado novo.
5. **Permissões:**
   - `EMERGENCIA.COORDENAR` (nova, prevista na ADR-0113): ações, mobilização e ativação de capacidade. Papéis: Gestor e
     Coordenador de enfermagem;
   - `CONTINGENCIA.GERENCIAR`: o cadastro de capacidade;
   - **leitura:** a da E1, mais `EMERGENCIA.COORDENAR`;
   - **na Escala:** quem designa continua sendo quem tem a permissão da Escala. A mobilização autoriza a exceção, não dá
     permissão.
6. **Escritas só na emergência ativa** (422).

### API

| Método | Caminho | Permissão |
|---|---|---|
| POST | `emergencia/{id}/acao`, `emergencia/{id}/acoes-do-plano`, `emergencia/{id}/acao/{acao}/situacao` | `EMERGENCIA.COORDENAR` |
| POST | `emergencia/{id}/mobilizacao`, `emergencia/{id}/mobilizacao/{mob}/situacao` | `EMERGENCIA.COORDENAR` |
| PUT | `emergencia/{id}/mobilizacao/{mob}/transferencia` | `EMERGENCIA.COORDENAR` |
| POST | `emergencia/{id}/capacidade-ativa`, `emergencia/{id}/capacidade-ativa/{ativacao}/desativacao` | `EMERGENCIA.COORDENAR` |
| GET | `emergencias/capacidades?unidadeId` | leitura |
| POST, PUT | `emergencias/capacidade` (`/{id}`) | `CONTINGENCIA.GERENCIAR` |

A emergência ganha `acoes`, `acoesAtrasadas` (também na lista), `mobilizacoes`, `capacidades` e `unidadesAfetadas`. O
turno da Escala ganha `mobilizacaoEmergencia`.

### Tela

- **Detalhe da emergência:** as seções "Ações" (quadro, "Gerar ações do plano", "Nova ação", iniciar, concluir,
  cancelar), "Mobilização" (mobilizar, andamento, ligar a transferência, concluir, cancelar, a marca "Autoriza turno na
  Escala"), "Capacidade emergencial" (ativar e desativar) e "Unidades afetadas".
- **Aba "Capacidade emergencial"** na tela Emergências: o cadastro por unidade e recurso.
- **Lista das emergências e Sala de Situação:** a contagem de ações atrasadas.
- **Escala:** o turno autorizado pela mobilização aparece com a marca "Mobilização".

### Migração

`V45__emergencias_coordenacao.sql`: as tabelas de ação, mobilização, capacidade e ativação de capacidade, com os CHECK
dos enums, e a coluna `mobilizacao_id` em `tb_turno_escala`. A permissão nasce pelo catálogo de acesso.

## Consequências

- **A coordenação acompanha a resposta,** sem tirar dos donos a execução: a Escala continua designando, a Farmácia
  transferindo e Equipamentos mudando a situação das unidades.
- **A exceção da Escala é rastreável:** cada turno fora da lotação aponta a mobilização e a emergência.
- **Ficam para depois:**
  - E4: redistribuição pela Farmácia e pela Regulação, vulneráveis e pacientes deslocados;
  - E5: desmobilização formal e relatório pós-evento;
  - a execução de insumo, equipamento e veículo pelo Estoque e pelo Transporte, quando existirem;
  - o leito extra marcado no próprio `Leito` (hoje a capacidade é contada, não cadastrada leito a leito).

## Testes

- **JUnit:**
  - `CoordenacaoEmergenciaControllerTest`:
    - ações: nada a gerar sem plano ativo (422), gerar do plano (prazo e código), gerar de novo (422), avulsa atrasada no
      alto e na lista, cancelar sem motivo (400), iniciar, concluir, mudar a concluída (422);
    - mobilização: sem lotação a Escala recusa; mobilizado, o turno entra marcado; fora do período, não; o mesmo
      profissional de novo (409); concluída, não autoriza mais; encerrada a emergência, também não;
    - medicamento: insumo sem recurso (400), transferência para outra unidade (422), ligada segue a transferência e não
      conclui à mão (422);
    - capacidade: repetida (409), ativar, acima do que resta (422), ativável abaixo do ativado (422), o ativado agora,
      desativada no encerramento, ativar depois de encerrada (422);
  - `EmergenciaAutorizacaoControllerTest.coordenacaoPorPapel`, com segurança ligada: o coordenador de enfermagem
    coordena e não cadastra capacidade; a vigilância não coordena; o gestor cadastra;
  - `EscalaControllerTest`: as regras da Escala sem mudança.
- **Robot de API:** `test/emergencias/Coordenacao.robot`, CT-001 a CT-011 (inclusive o turno por mobilização).
- **Robot de tela:** `UI_emergencias.robot`, CT-005 (quadro de ações) e CT-006 (mobilizar profissional).
