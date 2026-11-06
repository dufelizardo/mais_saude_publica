# 0164 — Vigilância em Saúde: vigilância ambiental, água e risco (V4c)

## Status

Aceita e implementada. Terceira e última parte da fatia V4 (ambiental e zoonoses) do domínio 23, na
[ADR-0110](./0110-vigilancia-em-saude.md) e em [`vigilancia/MODELO-VIGILANCIA.md`](../vigilancia/MODELO-VIGILANCIA.md),
seção 5. Segue as V4a e V4b ([ADR-0162](./0162-vigilancia-controle-de-vetores.md),
[ADR-0163](./0163-vigilancia-zoonoses-animais.md)). Com ela, a V4 fica completa.

## Contexto

- **A vigilância da qualidade da água** (VIGIAGUA) coleta amostras em pontos da rede e das soluções alternativas e
  confere com o padrão de potabilidade da **Portaria GM/MS 888/2021**.
- **O território tem riscos permanentes:** área contaminada, descarte irregular, fonte poluidora. É diferente da exposição
  de um desastre, que já é registrada na emergência ([ADR-0161](./0161-emergencias-epidemia-e-exposicao-ambiental.md)).

## Decisão

1. **Ponto de coleta** (`TB_PONTO_COLETA_AGUA`, `ZOONOSES.GERENCIAR`): nome, forma de abastecimento (rede, solução
   alternativa coletiva ou individual), local, ponto no mapa (latitude e longitude juntas, 400) e microárea opcional.
2. **Amostra** (`TB_AMOSTRA_AGUA`, `ZOONOSES.VISTORIAR`, só inclusão):
   - coliformes totais e *E. coli* (presença), cloro residual livre (mg/L), turbidez (uT) e fluoreto (mg/L); ao menos um
     (400); data não futura (400); ponto ativo (422);
   - **conformidade calculada no registro,** pela Portaria GM/MS 888/2021:
     - *E. coli* ausente;
     - na água tratada (rede e solução coletiva): coliformes totais ausentes (a presença pede recoleta) e cloro residual
       livre de pelo menos 0,2 mg/L;
     - cloro residual livre até 5 mg/L, turbidez até 5 uT e fluoreto até 1,5 mg/L;
     - a solução individual não é tratada: o cloro baixo e os coliformes totais não a reprovam;
   - as não conformidades ficam listadas; **a medida** da amostra fora do padrão (`ZOONOSES.GERENCIAR`; a conforme, 422).
3. **Risco ambiental** (`TB_RISCO_AMBIENTAL`, `ZOONOSES.GERENCIAR`): tipo (área contaminada, descarte irregular, fonte
   poluidora, outro), descrição, local, ponto no mapa, microárea e população exposta; ativo → controlado (com o motivo;
   só do ativo, 422).
4. **Leitura:** as de zoonoses, `VIGILANCIA.INVESTIGAR` e `INDICADOR.CONSULTAR` (só dado do território, sem pessoa).
5. **Sem permissão nova.**

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `ambiental/pontos-agua`, `ambiental/amostras-agua`, `ambiental/riscos` | as de zoonoses, `VIGILANCIA.INVESTIGAR` ou `INDICADOR.CONSULTAR` |
| POST | `ambiental/ponto-agua`, `ambiental/amostra-agua/{id}/medida` | `ZOONOSES.GERENCIAR` |
| POST | `ambiental/amostra-agua` | `ZOONOSES.VISTORIAR` |
| POST | `ambiental/risco`, `ambiental/risco/{id}/controle` | `ZOONOSES.GERENCIAR` |

### Tela

- **Abas novas na tela Zoonoses e vetores:** "Qualidade da água" (pontos com a última coleta, amostras com os
  parâmetros, a conformidade e a medida) e "Riscos ambientais" (registrar e marcar como controlado).
- **Sala de Situação:** a amostra fora do padrão do mês, ainda sem medida, aparece como alerta médio.

### Migração

`V51__vigilancia_ambiental_agua_e_risco.sql`: as tabelas do ponto de coleta, da amostra e do risco ambiental.

## Consequências

- **A água fora do padrão vira alerta e pede medida,** com a regra da portaria aplicada por código, sem planilha.
- **A V4 está completa:** vetores, animais e ambiente. Na Vigilância faltam a V3 (sanitária, que depende de Documentos)
  e a V5 (trabalhador e integrações).
- **Ficam para depois:** o plano de amostragem mínimo por população, os demais parâmetros da portaria (agrotóxicos,
  metais) e o envio ao SISAGUA (domínio 20, adiado); o polígono do risco (hoje, o ponto).

## Testes

- **JUnit:**
  - `VigilanciaAmbientalControllerTest`: o ponto sem a longitude (400); a amostra vazia e a futura (400); a conforme (e a
    medida nela, 422); a fora do padrão com as quatro não conformidades e a medida; a solução individual sem cloro e com
    coliformes ainda conforme, e reprovada pelo *E. coli*; os filtros e os pontos com a contagem; o risco sem a latitude
    (400), registrado, controlado e controlado de novo (422);
  - `ControleVetorialAutorizacaoControllerTest.vigilanciaAmbientalPorPapel`, com segurança ligada: o ponto é da
    coordenação; o agente registra a amostra; a recepção não; o gestor e a vigilância epidemiológica leem.
- **Robot de API:** `test/vigilancia/VigilanciaAmbiental.robot`, CT-001 a CT-008.
- **Robot de tela:** `UI_controle_vetorial.robot`, CT-007 (amostra fora do padrão e medida) e CT-008 (risco ambiental).
