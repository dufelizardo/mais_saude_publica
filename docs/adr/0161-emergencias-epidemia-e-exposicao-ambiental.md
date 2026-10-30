# 0161 — Emergências e Desastres: painel da epidemia e exposição ambiental (E6b)

## Status

Aceita e implementada. Segunda parte da fatia E6 (eventos especializados) do domínio 27, na
[ADR-0113](./0113-emergencias-e-desastres.md) e em [`emergencias/MODELO-EMERGENCIAS.md`](../emergencias/MODELO-EMERGENCIAS.md),
seções 14 e 17. Usa o perfil do tipo da E6a ([ADR-0160](./0160-emergencias-evento-de-massa-e-estruturas-temporarias.md)) e
a Vigilância ([ADR-0124](./0124-vigilancia-epidemiologica-fundacao.md), [ADR-0130](./0130-vigilancia-contatos-local-e-mapa.md),
[ADR-0131](./0131-vigilancia-surto-e-medidas.md)). Com ela, a E6 fica completa; no domínio 27 falta só a E4.

## Contexto

- **Vigilância e Emergências não se duplicam** (seção 14). A Vigilância já tem o agravo, a notificação, a classificação,
  a evolução, o surto e a curva por semana epidemiológica. Na epidemia, a emergência precisa **ler** esses números, não
  guardar outros.
- **No desastre ambiental, a saúde vigia a exposição** (VIGIDESASTRES, a vigilância em saúde dos desastres do Ministério
  da Saúde): que meio foi afetado, por qual agente, quantas pessoas estão expostas e quais unidades estão na área. A
  "gestão completa de desastres ambientais" fica fora do escopo (seção 17).

## Decisão

1. **O agravo da epidemia** (`TB_EMERGENCIA.agravo_id`, opcional):
   - escolhido na emergência ativa de perfil epidemia (`EMERGENCIA.REGISTRAR` ou `EMERGENCIA.ATIVAR`; outro perfil ou
     fora da ativa, 422; inexistente, 404);
   - sem ele, vale o agravo do surto de origem do evento (E1);
   - **agravo sensível não entra** (422): o painel é de toda a rede, e o sensível só é visto por quem investiga
     (ADR-0124).
2. **Painel da epidemia** (`GET emergencia/{id}/painel-epidemia`, leitura da E1), calculado na hora:
   - as notificações do agravo desde a semana epidemiológica de 4 semanas antes do início até hoje (ou o encerramento),
     pela data dos primeiros sintomas (ou da notificação);
   - **na área afetada:** se a emergência tem microáreas, só as notificações delas; senão, a rede toda (o painel diz qual);
   - **números:** notificados, confirmados, descartados, em investigação (o resto), óbitos pelo agravo, os casos da semana
     atual e da anterior;
   - **a curva por semana epidemiológica,** com a mesma regra e o mesmo cálculo do mapa da Vigilância (ADR-0130);
   - só números, sem dado de paciente. Sem agravo, ou em outro perfil, 422.
3. **Exposição ambiental** (`TB_EXPOSICAO_AMBIENTAL`, `EMERGENCIA.REGISTRAR`), datada e repetível, só inclusão, como a
   avaliação de impacto:
   - meio (água, ar, solo, produto químico, outro), agente ou substância, fonte, população exposta, unidades na área (as
     que existem, 404), medidas e recomendação à população;
   - na emergência ativa de perfil desastre ambiental (422);
   - **a tela sugere** a população e as unidades da sugestão do impacto (Território e Organização, E1); a equipe confere;
   - a recomendação fica registrada; divulgar é do domínio Comunicação (ADR-0114).
4. **Relatório pós-evento (E5):** na epidemia, os notificados, os confirmados e os óbitos pelo agravo; no desastre
   ambiental, o número de exposições e a maior população exposta. O CSV leva as mesmas linhas.
5. **Sem permissão nova.**

### API

| Método | Caminho | Permissão |
|---|---|---|
| PUT | `emergencia/{id}/agravo` | `EMERGENCIA.REGISTRAR` ou `EMERGENCIA.ATIVAR` |
| GET | `emergencia/{id}/painel-epidemia` | leitura |
| POST | `emergencia/{id}/exposicao-ambiental` | `EMERGENCIA.REGISTRAR` |

A emergência ganha `agravoId`, `agravoNome` (o escolhido ou o do surto) e `exposicoes`; os indicadores do relatório, os
números da epidemia e das exposições.

### Tela

- **Seção "Situação epidemiológica"** (perfil epidemia): o agravo ("Escolher" ou "Trocar agravo"), os números, a marca
  "subindo" quando a semana atual passa a anterior e a curva epidêmica (o mesmo componente da Vigilância).
- **Seção "Exposição ambiental"** (perfil desastre ambiental): as exposições e "Nova exposição", com a população e as
  unidades sugeridas.
- **Relatório pós-evento:** os números da epidemia e das exposições.

### Migração

`V48__emergencias_epidemia_e_exposicao_ambiental.sql`: a coluna do agravo na emergência e as tabelas da exposição e das
unidades dela.

## Consequências

- **A epidemia é acompanhada com os números da Vigilância,** sem planilha paralela, e a curva é a mesma nos dois lugares.
- **O desastre ambiental ganha o registro da exposição,** que é o que a saúde precisa saber para agir.
- **A E6 está completa.** No domínio 27 falta a E4 (integração da rede), que depende de Estoque e Transporte.
- **Fica para depois:** cálculo de incidência (precisa da população por microárea confiável) e a integração com sistemas
  externos (domínio 20, adiado).

## Testes

- **JUnit:**
  - `EpidemiaExposicaoControllerTest`:
    - o painel: sem agravo (422); agravo sensível, outro perfil (422) e inexistente (404); escolher o agravo; as
      notificações no período (a antiga fica de fora), confirmados, descartados, em investigação, óbitos, a semana atual e
      a soma da curva; os números no relatório; o agravo não muda na desmobilização (422);
    - a exposição: sem agente (400), unidade inexistente (404), duas exposições com as unidades, outro perfil (422) e os
      números no relatório;
  - `EmergenciaAutorizacaoControllerTest.exposicaoAmbientalPorPapel`, com segurança ligada: a vigilância registra; a
    recepção, não.
- **Robot de API:** `test/emergencias/EpidemiaExposicao.robot`, CT-001 a CT-009.
- **Robot de tela:** `UI_emergencias.robot`, CT-011 (painel da epidemia) e CT-012 (exposição ambiental).
