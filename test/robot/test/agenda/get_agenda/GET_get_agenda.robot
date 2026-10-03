*** Settings ***
Resource    ../../../src/scenario/agenda/get_agenda/get_agenda_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET GetAgenda
Metadata    Test Suite Description        This test suite validates the GET /api/v1/agenda/ endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAgenda    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET GetAgenda - HTTP 200 OK
    [Documentation]    Agenda do dia com o resumo.
    [Tags]    GET    GetAgenda    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDA - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET GetAgenda - HTTP 400 BAD REQUEST (Periodo Longo)
    [Documentation]    Período acima do limite.
    [Tags]    GET    GetAgenda    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDA - GET    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
