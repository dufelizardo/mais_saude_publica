*** Settings ***
Resource    ../../../src/scenario/estadual/create_estadual/create_estadual_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Estadual
Metadata    Test Suite Description        This test suite validates the POST create Estadual endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateEstadual    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-144: Validate POST Create Estadual - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Estadual endpoint with HTTP 201 CREATED response.
    [Tags]    POST    CreateEstadual    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-144: Validate POST Create Estadual - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the POST create Estadual endpoint with HTTP 409 CONFLICT response,
    ...    when the nome is already registered.
    [Tags]    POST    CreateEstadual    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - CREATE - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-144: Validate POST Create Estadual - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Estadual endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields (nome, email) are left blank.
    [Tags]    POST    CreateEstadual    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - CREATE - POST - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - AQUAQE-144: Validate POST Create Estadual - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the POST create Estadual endpoint with HTTP 400 BAD REQUEST
    ...    response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    POST    CreateEstadual    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - CREATE - POST - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - AQUAQE-144: Validate POST Create Estadual - HTTP 422 UNPROCESSABLE ENTITY (Tipo Divergente)
    [Documentation]    Test case to validate the POST create Estadual endpoint with HTTP 422 UNPROCESSABLE
    ...    ENTITY response, when the tipo in the payload doesn't match the /estadual/ endpoint (AQUAQE-214).
    [Tags]    POST    CreateEstadual    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - CREATE - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - AQUAQE-144: Validate POST Create Estadual - HTTP 422 UNPROCESSABLE ENTITY (Superior Nivel Errado)
    [Documentation]    Test case to validate the POST create Estadual endpoint with HTTP 422 UNPROCESSABLE
    ...    ENTITY response, when administracaoSuperior points at a unit of the wrong hierarchy level
    ...    (Estadual instead of Federal) — AQUAQE-22.
    [Tags]    POST    CreateEstadual    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - CREATE - POST - Superior Nivel Errado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - AQUAQE-144: Validate POST Create Estadual - HTTP 404 NOT FOUND (Superior Inexistente)
    [Documentation]    Test case to validate the POST create Estadual endpoint with HTTP 404 NOT FOUND response,
    ...    when administracaoSuperior points at a nome that doesn't exist (AQUAQE-13).
    [Tags]    POST    CreateEstadual    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - CREATE - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
