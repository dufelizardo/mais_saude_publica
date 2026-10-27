# 0144 — Indicadores e BI: situação operacional (BI2)

## Status

Aceita e implementada (fatia BI2 da [ADR-0142](./0142-indicadores-bi-e-gestao.md); soma-se à BI1 da
[ADR-0143](./0143-bi-producao-assistencial.md) na Sala de Situação).

## Contexto

- **O protótipo `Painel.html` tem o que a BI1 deixou em "Em breve":**
  - os alertas (superlotação, agenda saturada);
  - o tempo médio de espera;
  - a ocupação das agendas por especialidade;
  - os profissionais ausentes e as ocorrências da escala.
- **Quase tudo já é registrado:**
  - a triagem e a consulta têm data e hora;
  - as vagas saem dos blocos da agenda menos bloqueios, afastamentos e horário da unidade
    ([ADR-0091](./0091-agenda-do-profissional.md));
  - os turnos da escala têm profissional ou vaga aberta, e os afastamentos vêm do RH
    ([ADR-0105](./0105-escalas-backend.md)).
- **Faltava a chegada.** A data e hora do atendimento é a hora marcada quando ele nasce agendado, e não a hora em que
  o paciente chegou.

## Decisão

1. **Chegada do paciente:**
   - coluna `chegada_em` em `tb_atendimento` (migração `V35__atendimento_chegada.sql`);
   - o sistema a grava na primeira vez que o atendimento fica "em andamento" (na abertura, no acolhimento ou na mudança
     de situação), e ela não muda depois;
   - **sem preenchimento retroativo:** a chegada dos atendimentos antigos não é conhecida, e o atendimento registrado
     já concluído fica sem ela.
2. **Situação agora** (os em andamento nas últimas 24 horas):
   - em atendimento;
   - aguardando a triagem (sem triagem e sem consulta);
   - aguardando a consulta (com triagem e sem consulta);
   - a maior espera de quem aguarda.

   Sem a chegada registrada, vale a data e hora do atendimento.
3. **Tempo de espera no período** (padrão: o mês corrente; até um ano):
   - da chegada ao primeiro contato (triagem ou consulta, o que vier primeiro) e da chegada à consulta;
   - média, mediana e percentil 90, com a amostra;
   - só com a chegada registrada;
   - tempos negativos ou acima de 24 horas são descartados como erro de registro, e a contagem dos descartados
     aparece.
4. **Ocupação das agendas** (a partir de hoje; padrão 7 dias, até 31):
   - vagas ofertadas e ocupadas por especialidade;
   - pela **mesma montagem da Agenda** (`AgendaService.ocupacaoPorEspecialidade`), sem regra paralela;
   - a vaga bloqueada não é ofertada, e a marcada conta como ocupada.
5. **Ocorrências da escala** (a partir de agora; padrão 7 dias, até 31):
   - o turno sem profissional (vaga aberta) aparece como "Sem cobertura", com a função que falta;
   - o turno com o profissional afastado (aprovado ou em andamento) aparece como "Afastado";
   - o motivo é "Férias" ou "Licença pessoal". **A licença médica aparece só como "Afastado"**, porque o motivo de
     saúde é dado sensível ([ADR-0072](./0072-tela-profissionais-a-partir-do-prototipo.md)).
6. **Alertas** calculados na hora, com gravidade e a tela onde se age:

   | Alerta | Quando | Gravidade | Tela |
   |---|---|---|---|
   | **Fila** | aguardando na unidade acima do limite | alta | Atendimentos |
   | **Espera** | maior espera na unidade acima do limite | média; alta acima do dobro | Atendimentos |
   | **Agenda** | especialidade com a ocupação dos próximos 7 dias no limite ou acima | média | Agenda |
   | **Escala** | turno das próximas 24 horas sem cobertura | alta | Escalas |
   | **Escala** | turno das próximas 24 horas só com profissional afastado | média | Escalas |

   **Os limites ficam na configuração:**
   - `app.indicadores.alertas.fila-aguardando=20`;
   - `app.indicadores.alertas.espera-minutos=120`;
   - `app.indicadores.alertas.ocupacao-agenda-percentual=95`.

   Limites por unidade ficam para quando fizerem falta, em ADR própria.
7. **Acesso:** `INDICADOR.CONSULTAR`, com o mesmo escopo da BI1. A regra do escopo foi extraída para
   `EscopoDosIndicadores`, que as duas fatias usam.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `situacao?unidadeId` | `INDICADOR.CONSULTAR` (escopo) |
| GET | `situacao/espera?de&ate&unidadeId` | idem |
| GET | `situacao/ocupacao?unidadeId&dias` | idem |
| GET | `situacao/escala?unidadeId&dias` | idem |

### Tela

Na Sala de Situação:
- **faixa de alertas no topo**, como no protótipo, com o atalho para a tela de origem;
- **cartão "Agora":** em atendimento, aguardando triagem, aguardando consulta e maior espera, com "Dados ao vivo ·
  HH:MM" e atualização a cada minuto;
- **cartão "Tempo de espera":** média, mediana e 90% até o primeiro contato e até a consulta, com a amostra;
- **ocupação das agendas por especialidade:** a barra fica amarela a partir de 80% e vermelha a partir de 95%;
- **tabela de ocorrências da escala.**

Do bloco "Em breve" ficam só a vacinação (domínio 24) e os programas na rede (BI3).

## Consequências

- **O tempo de espera só vale para os atendimentos abertos depois da BI2.** No começo, a amostra é pequena, e a tela
  mostra o tamanho dela.
- **Atendimento esquecido aberto** sai da situação agora depois de 24 horas, e da espera pelo descarte acima de 24
  horas.
- **A cobertura do afastado** (quem substitui) não é registrada pela escala. A ocorrência diz que há afastado, não se
  alguém cobre; a substituição fica para uma fatia das Escalas.

## Testes

- **JUnit:**
  - `SituacaoOperacionalControllerTest`:
    - a chegada gravada na transição e imutável;
    - a espera com média, mediana e p90 e o descarte;
    - a fila agora e os alertas de fila e espera (limite da fila baixado no teste);
    - a ocupação igual à da Agenda;
    - as ocorrências sem expor a licença médica, e o alerta de escala;
  - o caso de segurança ligada em `ProducaoAutorizacaoControllerTest`.
- **Robot de API:** `test/indicadores/SituacaoOperacional.robot`.
- **Robot de tela:** CT-003 de `test/ui/indicadores/UI_sala_de_situacao.robot`.
