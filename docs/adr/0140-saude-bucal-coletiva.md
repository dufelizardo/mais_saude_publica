# 0140 — Saúde Bucal: saúde bucal coletiva (SB5)

## Status

Aceita e implementada (fatia SB5 da [ADR-0111](./0111-saude-bucal.md); usa as ações programáticas da
[ADR-0132](./0132-programas-acoes-programaticas.md) e a avaliação odontológica da
[ADR-0134](./0134-saude-bucal-base.md)). Fecha o domínio 25.

## Contexto

- **As ações coletivas de saúde bucal** (escovação supervisionada, flúor, educação, avaliação de risco, levantamento
  epidemiológico) acontecem na escola, na creche, na comunidade e na empresa, muitas vezes pelo Programa Saúde na
  Escola (PSE).
- **A ADR-0111 (MODELO, seção 11) decidiu que elas não ganham motor próprio:** são ações programáticas de Programas
  (P4) com o detalhe odontológico. Programas não ficam dentro de Saúde Bucal.
- **A ação coletiva da P4 já tem** a unidade, a equipe, o local, a lista nominal (participantes acompanhados no
  programa) e a quantidade atendida. Falta o que é odontológico, e falta usar o resultado: quem precisa de tratamento
  tem que ser convocado para a primeira consulta odontológica.
- **O catálogo de Programas não tinha o PSE.** Pelos cinco passos da [ADR-0116](./0116-marco-mapa-de-dominios-fechado.md),
  é dado de catálogo, e não domínio novo.

## Decisão

1. **Detalhe odontológico da ação coletiva** (`AcaoSaudeBucal`, `tb_acao_saude_bucal` e as atividades em
   `tb_acao_saude_bucal_atividade`), um por ação:
   - **as atividades** (ao menos uma, 400): escovação supervisionada, aplicação tópica de flúor, educação em saúde
     bucal, avaliação coletiva de risco e levantamento epidemiológico;
   - **o local:** escola, creche, UBS, comunidade, empresa, instituição de longa permanência ou outro, com o nome da
     instituição;
   - **o público-alvo** (texto) e os **kits de higiene distribuídos** (não negativos, 400);
   - **a quantidade atendida** continua a da ação (P4);
   - **regras:**
     - só na ação coletiva (a de um paciente, 422) dos programas **Saúde Bucal** e **Saúde na Escola** (422);
     - preenchido na ação prevista ou realizada; a não realizada e a cancelada não recebem (422);
     - preencher de novo substitui.
2. **Avaliação coletiva por participante** (`AvaliacaoBucalColetiva`, `tb_avaliacao_bucal_coletiva`):
   - para cada participante, o **risco em saúde bucal** e a **necessidade de tratamento**, na escala da avaliação
     individual (SB1), e a observação;
   - **só na ação realizada** (422) e **com a lista nominal** (422 sem participantes); só para quem participou (422);
   - **um resultado por participante na ação:** a correção substitui o anterior, e a trilha de auditoria guarda a
     mudança.
3. **A convocar** (`GET /saude-bucal/a-convocar?unidadeId=`):
   - a avaliação coletiva **mais recente** de cada paciente nas ações da unidade;
   - entra quem tem necessidade diferente de "sem necessidade" e **ainda não tem plano de tratamento aberto** (SB3b);
   - em ordem de necessidade (a urgência primeiro) e pelo nome;
   - é a busca ativa da primeira consulta odontológica: quando o plano é aberto, o paciente sai da lista.
4. **Programa Saúde na Escola (PSE)** entra na carga do catálogo de Programas (Atenção Básica, anual), se ainda não
   existir.
5. **Acesso:**
   - **o detalhe** com `PROGRAMA.ACOMPANHAR`, como a ação da P4: o técnico em saúde bucal registra a escovação e o
     flúor (Lei 11.889/2008);
   - **a avaliação de risco** com `ODONTOLOGIA.REGISTRAR`, do cirurgião-dentista;
   - **ler** (o detalhe e a lista a convocar) com uma das duas; a lista tem a leitura auditada.
6. **API:**
   - `GET /acao-programatica/{id}/saude-bucal` e `PUT /acao-programatica/{id}/saude-bucal`;
   - `POST /acao-programatica/{id}/avaliacao-bucal`, com os resultados em lote;
   - `GET /saude-bucal/a-convocar?unidadeId=`.
7. **Tela** (Programas):
   - **na lista de ações,** a ação coletiva de Saúde Bucal ou PSE, prevista ou realizada, ganha "Saúde bucal", que abre
     a gaveta com as atividades, o local, a instituição, o público e os kits;
   - **na ação realizada com lista nominal,** a gaveta mostra o risco e a necessidade de cada participante, editáveis
     pelo dentista;
   - **a aba "A convocar (saúde bucal)"** lista, na unidade escolhida, quem precisa de tratamento, com o atalho para o
     prontuário.
8. **Migração V33:** o PSE no catálogo, o detalhe com as atividades e a avaliação coletiva.

## Consequências

- **A saúde bucal coletiva tem registro** sem duplicar Programas, e o resultado vira convocação para o tratamento.
- **O domínio 25 fecha:** avaliação, odontograma, procedimento, plano, regulação, prótese e coletiva (SB1 a SB5).
- **Fora do escopo:**
  - o índice CPO-D do levantamento epidemiológico e a contagem dos indicadores (P6 e BI);
  - a convocação automática (agendamento a partir da lista).

## Testes

- **JUnit:**
  - **`SaudeBucalColetivaControllerTest`:**
    - **o detalhe:**
      - recusas: a ação do HiperDia (422), sem atividades (400), kits negativos (400);
      - o detalhe vazio antes de preencher; o preenchimento com as atividades e os kits;
      - a correção que substitui; a ação cancelada não recebe (422);
    - **a avaliação e a lista:**
      - recusas: a ação sem lista nominal (422), antes da realização (422), quem não participou (422);
      - os três resultados;
      - a lista a convocar: a urgência primeiro, sem quem não precisa;
      - a correção que substitui; com o plano aberto, o paciente sai da lista.
  - **`SaudeBucalAutorizacaoControllerTest`:** o técnico preenche o detalhe (200) e não avalia (403); o dentista avalia.
  - **`AcaoProgramaticaControllerTest` e `ProgramaControllerTest`:** a P4 e o catálogo continuam iguais.
  - **`MigracoesDoEsquemaTest`:** os CHECKs das enumerações novas batem com o Java.
- **Robot:**
  - **de API:** `SaudeBucalColetiva.robot` (CT-001 a CT-005): o detalhe; o programa sem odontologia; sem atividade; a
    avaliação e a lista a convocar; sem lista nominal.
  - **de interface:** `UI_saude_bucal.robot` CT-009: a escovação e o flúor na ação do PSE, com a escola e os kits.
