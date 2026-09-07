*** Settings ***
Resource    ../../../src/scenario/municipal/create_municipal/create_municipal_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Municipal
Metadata    Test Suite Description        This test suite validates the POST create Municipal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateMunicipal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-145: Validate POST Create Municipal - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Municipal endpoint with HTTP 201 CREATED response.
    [Tags]    POST    CreateMunicipal    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-145: Validate POST Create Municipal - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the POST create Municipal endpoint with HTTP 409 CONFLICT response,
    ...    when the nome is already registered.
    [Tags]    POST    CreateMunicipal    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - CREATE - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-145: Validate POST Create Municipal - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Municipal endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields (nome, email) are left blank.
    [Tags]    POST    CreateMunicipal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - CREATE - POST - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - AQUAQE-145: Validate POST Create Municipal - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the POST create Municipal endpoint with HTTP 400 BAD REQUEST
    ...    response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    POST    CreateMunicipal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - CREATE - POST - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - AQUAQE-145: Validate POST Create Municipal - HTTP 422 UNPROCESSABLE ENTITY (Tipo Divergente)
    [Documentation]    Test case to validate the POST create Municipal endpoint with HTTP 422 UNPROCESSABLE
    ...    ENTITY response, when the tipo in the payload doesn't match the /municipal/ endpoint (AQUAQE-214).
    [Tags]    POST    CreateMunicipal    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - CREATE - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - AQUAQE-145: Validate POST Create Municipal - HTTP 422 UNPROCESSABLE ENTITY (Superior Nivel Errado)
    [Documentation]    Test case to validate the POST create Municipal endpoint with HTTP 422 UNPROCESSABLE
    ...    ENTITY response, when administracaoSuperior points at a unit of the wrong hierarchy level
    ...    (Municipal instead of Estadual) — AQUAQE-22.
    [Tags]    POST    CreateMunicipal    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - CREATE - POST - Superior Nivel Errado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - AQUAQE-145: Validate POST Create Municipal - HTTP 404 NOT FOUND (Superior Inexistente)
    [Documentation]    Test case to validate the POST create Municipal endpoint with HTTP 404 NOT FOUND response,
    ...    when administracaoSuperior points at a nome that doesn't exist (AQUAQE-13).
    [Tags]    POST    CreateMunicipal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - CREATE - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
