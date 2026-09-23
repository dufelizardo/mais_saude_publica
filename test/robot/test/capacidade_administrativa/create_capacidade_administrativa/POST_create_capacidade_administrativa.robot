*** Settings ***
Resource    ../../../src/scenario/capacidade_administrativa/create_capacidade_administrativa/create_capacidade_administrativa_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Capacidade Administrativa
Metadata    Test Suite Description        This test suite validates the POST create Capacidade Administrativa endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateCapacidadeAdministrativa    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Capacidade Administrativa - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Capacidade Administrativa endpoint with HTTP 201
    ...    CREATED response.
    [Tags]    POST    CreateCapacidadeAdministrativa    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAPACIDADE ADMINISTRATIVA - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Capacidade Administrativa - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the POST create Capacidade Administrativa endpoint with HTTP 409
    ...    CONFLICT response, when codigo is already registered.
    [Tags]    POST    CreateCapacidadeAdministrativa    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAPACIDADE ADMINISTRATIVA - CREATE - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Capacidade Administrativa - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Capacidade Administrativa endpoint with HTTP 400 BAD
    ...    REQUEST response, when required fields (codigo, nome, ativo) are left blank/omitted.
    [Tags]    POST    CreateCapacidadeAdministrativa    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAPACIDADE ADMINISTRATIVA - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
