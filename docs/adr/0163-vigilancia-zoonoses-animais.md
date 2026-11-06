# 0163 — Vigilância em Saúde: animais das zoonoses (V4b)

## Status

Aceita e implementada. Segunda parte da fatia V4 (ambiental e zoonoses) do domínio 23, na
[ADR-0110](./0110-vigilancia-em-saude.md) e em [`vigilancia/MODELO-VIGILANCIA.md`](../vigilancia/MODELO-VIGILANCIA.md),
seção 6. Usa o controle de vetores ([ADR-0162](./0162-vigilancia-controle-de-vetores.md)), o imóvel do Território
([ADR-0127](./0127-cadastro-territorial.md)) e a notificação do atendimento antirrábico humano
([ADR-0124](./0124-vigilancia-epidemiologica-fundacao.md)). A V4c (vigilância ambiental: água e risco) vem depois.

## Contexto

- **A raiva começa num animal:** a pessoa agredida por um cão ou um gato é notificada (atendimento antirrábico, já no
  catálogo de agravos), e o animal fica em observação por 10 dias. O desfecho do animal orienta o esquema da pessoa.
- **A leishmaniose visceral canina** é vigiada pelo inquérito no cão: teste rápido na triagem, ELISA na confirmação e o
  desfecho do cão reagente.
- **A vacinação antirrábica animal** é feita em postos e registrada em números por dia, não animal a animal.

## Decisão

1. **Animal de interesse** (`TB_ANIMAL_ZOONOSES`, `ZOONOSES.VISTORIAR`):
   - espécie (cão, gato, morcego, primata, herbívoro, outra), nome e descrição;
   - **o tutor não vira paciente:** só o nome e o telefone; a leitura da lista é auditada;
   - o imóvel do Território (a microárea vem dele) ou o endereço em texto: um dos dois (400).
2. **Observação do animal agressor** (`TB_OBSERVACAO_ANIMAL`, `ZOONOSES.VISTORIAR`):
   - só de cão ou gato (422; outra espécie vai direto para o exame), uma aberta por animal (409);
   - data da agressão (não futura, 400), início (não antes da agressão, 400) e **fim previsto: a agressão mais 10 dias**;
   - o atendimento antirrábico notificado, opcional (outro agravo, 422; já com observação aberta, 409);
   - desfecho: sadio (só a partir do fim previsto, 422), morto, desaparecido ou suspeito de raiva; encerrada não muda
     (422); **vencida** é a aberta depois do fim previsto;
   - **no caso notificado,** quem investiga vê a observação e o desfecho.
3. **Exame do animal** (`TB_EXAME_ANIMAL`):
   - raiva por imunofluorescência; leishmaniose pelo teste rápido (TR DPP) e pelo ELISA; outro;
   - a coleta (`ZOONOSES.VISTORIAR`, data não futura, 400) pode apontar a observação do mesmo animal (outro, 422);
   - o resultado (`ZOONOSES.VISTORIAR` ou `ZOONOSES.GERENCIAR`): reagente, não reagente ou indeterminado, retificável;
     **o desfecho do cão** (eutanásia, tratamento veterinário, acompanhamento, óbito) só na leishmaniose reagente (422).
4. **Vacinação antirrábica animal** (`TB_VACINACAO_ANTIRRABICA_ANIMAL`, `ZOONOSES.VISTORIAR`):
   - o posto no dia: microárea, posto, cães e gatos vacinados (ao menos um, 400), responsável;
   - os totais do ano, por área, e os registros; a leitura agregada também é de quem consulta indicadores.
5. **Sem permissão nova:** as de zoonoses da V4a; quem investiga lê.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `zoonoses/animais`, `zoonoses/observacoes`, `zoonoses/exames` | as de zoonoses ou `VIGILANCIA.INVESTIGAR` |
| POST | `zoonoses/animal`, `zoonoses/observacao`, `zoonoses/observacao/{id}/desfecho`, `zoonoses/exame` | `ZOONOSES.VISTORIAR` |
| POST | `zoonoses/exame/{id}/resultado` | `ZOONOSES.VISTORIAR` ou `ZOONOSES.GERENCIAR` |
| GET | `zoonoses/vacinacao-antirrabica` | as de zoonoses, `VIGILANCIA.INVESTIGAR` ou `INDICADOR.CONSULTAR` |
| POST | `zoonoses/vacinacao-antirrabica` | `ZOONOSES.VISTORIAR` |

### Tela

- **Abas novas na tela Zoonoses e vetores:**
  - **Animais:** a busca, registrar o animal, "Observar", "Coleta" e a tabela de exames com o resultado;
  - **Observação de agressores:** abrir a observação, o prazo dos 10 dias (vencida em destaque), o desfecho e a coleta
    para raiva do agressor morto ou suspeito;
  - **Vacinação antirrábica:** o ano, os totais de cães e gatos e os postos.
- **Vigilância epidemiológica:** no detalhe do atendimento antirrábico, o animal agressor com o prazo e o desfecho.

### Migração

`V50__vigilancia_zoonoses_animais.sql`: as tabelas do animal, da observação, do exame e da vacinação antirrábica.

## Consequências

- **O caso humano e o animal ficam ligados:** o investigador vê o desfecho sem ligar para a zoonoses.
- **O inquérito da leishmaniose tem registro,** com o desfecho do cão reagente.
- **Ficam para depois:**
  - a V4c: qualidade da água (VIGIAGUA) e risco ambiental;
  - a cobertura da vacinação antirrábica (precisa da população animal estimada);
  - o envio aos sistemas nacionais (domínio 20, adiado).

## Testes

- **JUnit:**
  - `ZoonosesControllerTest`:
    - animal e observação: sem imóvel nem endereço (400); a busca com a microárea do imóvel; morcego em observação
      (422); agressão futura e início antes dela (400); outro agravo (422); o fim previsto; repetida (409); a
      observação pela notificação; sadio antes do prazo (422); suspeito de raiva; encerrada (422); a vencida e o sadio;
    - exames e vacinação: a observação de outro animal (422); coleta futura (400); os pendentes; o desfecho do cão fora
      da leishmaniose reagente (422); o desfecho gravado; o posto sem animal (400) e os totais por área;
  - `ControleVetorialAutorizacaoControllerTest.zoonosesPorPapel`, com segurança ligada: o agente registra; a recepção não
    registra nem lê; a vigilância epidemiológica lê as observações; o gestor lê a vacinação.
- **Robot de API:** `test/vigilancia/Zoonoses.robot`, CT-001 a CT-011.
- **Robot de tela:** `UI_controle_vetorial.robot`, CT-004 (animal e observação), CT-005 (exame de leishmaniose), CT-006
  (vacinação antirrábica) e CT-009 (o animal agressor na notificação).
