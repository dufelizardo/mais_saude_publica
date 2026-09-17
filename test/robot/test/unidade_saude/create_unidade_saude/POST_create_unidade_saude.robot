*** Settings ***
Resource    ../../../src/scenario/unidade_saude/create_unidade_saude/create_unidade_saude_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Unidade Saude
Metadata    Test Suite Description        This test suite validates the POST create Unidade de Saúde endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateUnidadeSaude    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Unidade Saude - HTTP 201 CREATED (Tipo UBS)
    [Documentation]    Test case to validate the POST create Unidade de Saúde endpoint with HTTP 201 CREATED
    ...    response, using tipo=UBS and no optional supervisaoRegional link.
    [Tags]    POST    CreateUnidadeSaude    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Unidade Saude - HTTP 201 CREATED (Tipo HOSPITAL Com Supervisao Regional)
    [Documentation]    Test case to validate the POST create Unidade de Saúde endpoint with HTTP 201 CREATED
    ...    response, using tipo=HOSPITAL and an informed supervisaoRegional link.
    [Tags]    POST    CreateUnidadeSaude    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - CREATE - POST - Tipo HOSPITAL Com Supervisao Regional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Unidade Saude - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the POST create Unidade de Saúde endpoint with HTTP 409 CONFLICT
    ...    response, when the nome is already registered.
    [Tags]    POST    CreateUnidadeSaude    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - CREATE - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Unidade Saude - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Unidade de Saúde endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields (nome, email) are left blank.
    [Tags]    POST    CreateUnidadeSaude    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - CREATE - POST - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate POST Create Unidade Saude - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the POST create Unidade de Saúde endpoint with HTTP 400 BAD REQUEST
    ...    response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    POST    CreateUnidadeSaude    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - CREATE - POST - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate POST Create Unidade Saude - HTTP 422 UNPROCESSABLE ENTITY (Tipo Divergente)
    [Documentation]    Test case to validate the POST create Unidade de Saúde endpoint with HTTP 422
    ...    UNPROCESSABLE ENTITY response, when the tipo in the payload doesn't match the /unidade-saude/
    ...    endpoint (AQUAQE-214).
    [Tags]    POST    CreateUnidadeSaude    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - CREATE - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate POST Create Unidade Saude - HTTP 422 UNPROCESSABLE ENTITY (Superior Nivel Errado)
    [Documentation]    Test case to validate the POST create Unidade de Saúde endpoint with HTTP 422
    ...    UNPROCESSABLE ENTITY response, when administracaoSuperior points at a unit of the wrong hierarchy
    ...    level (Estadual instead of Municipal) — AQUAQE-22.
    [Tags]    POST    CreateUnidadeSaude    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - CREATE - POST - Superior Nivel Errado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate POST Create Unidade Saude - HTTP 404 NOT FOUND (Superior Inexistente)
    [Documentation]    Test case to validate the POST create Unidade de Saúde endpoint with HTTP 404 NOT FOUND
    ...    response, when administracaoSuperior points at a nome that doesn't exist (AQUAQE-13).
    [Tags]    POST    CreateUnidadeSaude    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - CREATE - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
