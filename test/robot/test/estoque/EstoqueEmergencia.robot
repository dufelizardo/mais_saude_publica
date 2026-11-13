*** Settings ***
Resource    ../../src/scenario/estoque/estoque_emergencia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/GET Estoque nas Emergencias
Metadata    Test Suite Description        Reserva para a emergência, requisição urgente da mobilização e consumo extraordinário (ADR-0191).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    EstoqueEmergencia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-11
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Reserva E Requisicao Urgente - HTTP 422 201 E 200
    [Documentation]    A reserva não passa do livre; o insumo pela central vira a requisição urgente, que usa a reserva no atendimento.
    [Tags]    POST    GET    EstoqueEmergencia    HTTP422    HTTP201    HTTP200
    ESTOQUE NAS EMERGENCIAS    RESERVA_REQUISICAO

CT-002 - Validate Consumo Extraordinario E Desmobilizacao - HTTP 422 201 E 200
    [Documentation]    A saída comum não tira do reservado; o consumo da emergência usa a reserva; a desmobilização libera e o relatório conta.
    [Tags]    POST    GET    EstoqueEmergencia    HTTP422    HTTP201    HTTP200
    ESTOQUE NAS EMERGENCIAS    CONSUMO_DESMOBILIZACAO
