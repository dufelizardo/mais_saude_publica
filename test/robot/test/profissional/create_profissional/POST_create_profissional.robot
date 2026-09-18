*** Settings ***
Resource    ../../../src/scenario/profissional/create_profissional/create_profissional_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Profissional
Metadata    Test Suite Description        This test suite validates the POST create Profissional endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateProfissional    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Profissional - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Profissional endpoint with HTTP 201 CREATED
    ...    response.
    [Tags]    POST    CreateProfissional    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Profissional - HTTP 201 CREATED (Cpf Repetido)
    [Documentation]    Test case to validate the POST create Profissional endpoint with HTTP 201 CREATED
    ...    response even when the cpf is already registered in another ficha — cpf não é mais único, a
    ...    matrícula gerada automaticamente é a chave única de verdade (ver ADR-0017).
    [Tags]    POST    CreateProfissional    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - CREATE - POST - Cpf Repetido
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Profissional - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Profissional endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields (cpf, nome, email) are left blank.
    [Tags]    POST    CreateProfissional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - CREATE - POST - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Profissional - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the POST create Profissional endpoint with HTTP 400 BAD REQUEST
    ...    response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    POST    CreateProfissional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - CREATE - POST - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
