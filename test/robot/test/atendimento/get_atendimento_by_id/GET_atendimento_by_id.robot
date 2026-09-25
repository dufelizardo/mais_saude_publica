*** Settings ***
Resource    ../../../src/scenario/atendimento/get_atendimento_by_id/get_atendimento_by_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Atendimento By Id
Metadata    Test Suite Description        This test suite validates the GET Atendimento by id endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAtendimentoById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Atendimento By Id - HTTP 200 OK
    [Documentation]    Test case to validate the GET Atendimento by id endpoint with HTTP 200 OK response.
    [Tags]    GET    GetAtendimentoById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ATENDIMENTO - GET BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Atendimento By Id - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Atendimento by id endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    GET    GetAtendimentoById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ATENDIMENTO - GET BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
