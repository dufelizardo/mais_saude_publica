*** Settings ***
Resource    ../../../src/scenario/medicamento/get_medicamento_by_id/get_medicamento_by_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Medicamento By Id
Metadata    Test Suite Description        This test suite validates the GET Medicamento by id endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetMedicamentoById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-26
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Medicamento By Id - HTTP 200 OK
    [Documentation]    Test case to validate the GET Medicamento by id endpoint with HTTP 200 OK response.
    [Tags]    GET    GetMedicamentoById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MEDICAMENTO - GET BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Medicamento By Id - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Medicamento by id endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    GET    GetMedicamentoById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MEDICAMENTO - GET BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
