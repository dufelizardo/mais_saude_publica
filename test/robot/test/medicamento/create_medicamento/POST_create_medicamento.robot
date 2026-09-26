*** Settings ***
Resource    ../../../src/scenario/medicamento/create_medicamento/create_medicamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Medicamento
Metadata    Test Suite Description        This test suite validates the POST create Medicamento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateMedicamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-26
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Medicamento - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Medicamento endpoint with HTTP 201 CREATED
    ...    response.
    [Tags]    POST    CreateMedicamento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MEDICAMENTO - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Medicamento - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Medicamento endpoint with HTTP 400 BAD
    ...    REQUEST response, when required fields are omitted.
    [Tags]    POST    CreateMedicamento    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MEDICAMENTO - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
