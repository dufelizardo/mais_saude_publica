# 0141 — Programas: metas e indicadores programáticos (P6)

## Status

Aceita e implementada (fatia P6 da [ADR-0109](./0109-programas-acoes-e-linhas-de-cuidado.md); usa o acompanhamento da
[ADR-0119](./0119-programas-acompanhamento.md), as ações programáticas da [ADR-0132](./0132-programas-acoes-programaticas.md),
a adscrição da [ADR-0128](./0128-adscricao-e-cobertura.md) e os registros de Saúde Bucal das ADRs
[0137](./0137-saude-bucal-plano-de-tratamento.md), [0139](./0139-saude-bucal-tratamento-protetico.md) e [0140](./0140-saude-bucal-coletiva.md)).
Fica faltando a P5 (linhas de cuidado).

## Contexto

- **O detalhe do programa mostrava** os inscritos, os faltosos e os atrasados, e "Em breve" no controle clínico. O botão
  "Exportar relatório" estava desligado esperando esta fatia.
- **A coordenação de um programa precisa saber se ele funciona:** quantos estão em dia, a pressão controlada no
  HiperDia, o pré-natal com seis consultas, o tratamento odontológico concluído. E precisa comparar com a meta que o
  município pactuou.
- **Tudo isso já está registrado** (atendimentos, triagens, resultados de exame, planos de tratamento, próteses, ações
  coletivas). Falta calcular e guardar a meta.
- **Indicadores de produção** (atendimentos por dia e por mês na unidade) não são de programa: ficam para o #19
  Indicadores e BI (BI1), que recebe também estes indicadores.

## Decisão

1. **Indicadores calculados, não guardados.** Cada indicador (`IndicadorProgramatico`) tem nome, descrição, tipo
   (percentual ou quantidade) e os programas a que se aplica; é apurado na hora, a partir dos registros, no período
   (padrão: do primeiro dia do mês até hoje) e na rede ou numa unidade:
   - **Acompanhados em dia** (todos os programas com periodicidade): inscritos com atendimento realizado dentro da
     periodicidade, sobre os inscritos ativos no fim do período;
   - **Cobertura:** inscritos sobre os elegíveis adscritos às equipes, pela idade e pelo sexo do público. Não se aplica
     ao programa por condição ou gestação, porque o cadastro não diz quem tem a condição (não há denominador);
   - **Ações realizadas:** realizadas sobre previstas no período, sem as canceladas;
   - **PA controlada** (HiperDia): a última pressão aferida no período abaixo de 140/90 mmHg (a triagem retificada não
     conta), sobre os inscritos com pressão aferida;
   - **Exame no alvo** (qualquer programa, só com meta): o último resultado liberado do exame da meta, no período,
     dentro dos limites da meta (ex.: HbA1c abaixo de 7%);
   - **Pré-natal com 6 consultas** (Pré-natal): gestantes com seis ou mais atendimentos desde a inscrição;
   - **Tratamento odontológico concluído** (Saúde Bucal): planos concluídos sobre os abertos no período;
   - **Próteses entregues** (Saúde Bucal) e **escovação supervisionada** (Saúde Bucal e PSE, pessoas atendidas nas
     ações coletivas com escovação), em quantidade.

   O indicador que não se aplica volta com o motivo, e não com zero.
2. **Meta** (`MetaProgramatica`, `tb_meta_programatica`): indicador, alvo, período (início e fim) e unidade (vazia é a
   rede). No exame no alvo, o exame e os limites mínimo e máximo.
   - **Regras:** o indicador tem que ser do programa (422); o período válido (400); o percentual até 100 (400); o exame
     no alvo pede o exame e ao menos um limite (400); uma meta por indicador e unidade em cada período: períodos que se cruzam dão 409, para que a meta vigente seja uma só; no
     ajuste, o indicador e a unidade não mudam (422).
   - **A meta é configuração, e não registro clínico:** pode ser ajustada e excluída. Quem cadastra é
     `PROGRAMA.GERENCIAR`.
3. **Situação** de cada indicador diante da meta vigente no fim do período: **atingida** (valor no alvo ou acima),
   **em risco** (a partir de 80% do alvo), **abaixo** ou **sem dados** (sem valor no período).
4. **Evolução mensal:** a série dos últimos meses (padrão 12, até 24) de um indicador. A cobertura não tem série,
   porque é apurada hoje.
5. **Relatório em CSV** (ponto e vírgula, UTF-8 com BOM, para abrir no Excel): os mesmos indicadores do painel.
6. **Só números.** Nenhum indicador, série ou relatório traz paciente. O programa sensível (Saúde Mental) só mostra os
   números a quem tem `PROGRAMA.ACOMPANHAR` (403); os demais, a qualquer logado.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `programa/{id}/indicadores?de&ate&unidadeId` | logado; sensível: `PROGRAMA.ACOMPANHAR` |
| GET | `programa/{id}/indicador/{indicador}/serie?unidadeId&meses` | idem |
| GET | `programa/{id}/indicadores.csv?de&ate&unidadeId` | idem |
| GET | `programa/{id}/metas` | logado |
| POST | `programa/{id}/meta` | `PROGRAMA.GERENCIAR` |
| PUT / DELETE | `meta-programatica/{id}` | `PROGRAMA.GERENCIAR` |

### Tela

No detalhe do programa, o bloco "Indicadores do programa" tem o período, a unidade (ou a rede), cada indicador com o
valor, a fração, a meta e a situação, a evolução de 12 meses em gráfico, a lista de metas e o cadastro de metas. O
botão "Exportar relatório" do cabeçalho baixa o CSV do programa aberto.

### Migração

`V34__programas_metas_e_indicadores.sql`: `tb_meta_programatica`, com o CHECK do indicador e as chaves para programa,
unidade e exame.

## Consequências

- **O domínio 22 fica com P1 a P4 e P6;** falta a P5 (linhas de cuidado).
- **O #19 Indicadores e BI (BI1) consome estes indicadores** e traz os de produção da unidade (atendimentos por dia e
  por mês), que não são de programa.
- **O cálculo é na hora.** Na escala de um município pequeno ou médio basta; se pesar, o BI materializa (decisão do
  BI1).
- **CPO-D** e outros indicadores epidemiológicos de saúde bucal ficam para o BI, porque pedem o levantamento
  epidemiológico consolidado.

## Testes

- JUnit `IndicadorProgramaticoControllerTest` (HiperDia: em dia, PA controlada, metas, série e CSV; Pré-natal;
  Saúde Bucal; período inválido) e o caso de autorização em `ProgramaAutorizacaoControllerTest` (sensível pede
  acompanhar; meta pede gerenciar).
- Robot de API `test/programas/indicador/IndicadorProgramatico.robot` e de tela CT-017 e CT-018 em
  `test/ui/assistencia/programas/UI_programas.robot`.
