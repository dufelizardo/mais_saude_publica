*** Settings ***
Resource    ../../../src/scenario/agendamento/get_agendamento_by_id/get_agendamento_by_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Agendamento By Id
Metadata    Test Suite Description        This test suite validates the GET Agendamento by id endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAgendamentoById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Agendamento By Id - HTTP 200 OK
    [Documentation]    Test case to validate the GET Agendamento by id endpoint with HTTP 200 OK response.
    [Tags]    GET    GetAgendamentoById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - GET BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Agendamento By Id - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Agendamento by id endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    GET    GetAgendamentoById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - GET BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
