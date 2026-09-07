*** Settings ***
Resource    ../../../src/scenario/federal/create_federal/create_federal_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Federal
Metadata    Test Suite Description        This test suite validates the POST create Federal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateFederal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-143: Validate POST Create Federal - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Federal endpoint with HTTP 201 CREATED response.
    [Tags]    POST    CreateFederal    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-143: Validate POST Create Federal - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the POST create Federal endpoint with HTTP 409 CONFLICT response,
    ...    when the nome is already registered.
    [Tags]    POST    CreateFederal    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - CREATE - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-143: Validate POST Create Federal - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Federal endpoint with HTTP 400 BAD REQUEST response,
    ...    when required fields (nome, email) are left blank.
    [Tags]    POST    CreateFederal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - CREATE - POST - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - AQUAQE-143: Validate POST Create Federal - HTTP 422 UNPROCESSABLE ENTITY (Tipo Divergente)
    [Documentation]    Test case to validate the POST create Federal endpoint with HTTP 422 UNPROCESSABLE
    ...    ENTITY response, when the tipo in the payload doesn't match the /federal/ endpoint (AQUAQE-214).
    [Tags]    POST    CreateFederal    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - CREATE - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - AQUAQE-143: Validate POST Create Federal - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the POST create Federal endpoint with HTTP 400 BAD REQUEST response,
    ...    when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    POST    CreateFederal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - CREATE - POST - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
