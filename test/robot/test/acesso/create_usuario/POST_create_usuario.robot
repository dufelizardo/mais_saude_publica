*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST CreateUsuario
Metadata    Test Suite Description        This test suite validates the POST /api/v1/usuario/ endpoint (ADR-0068) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateUsuario    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST CreateUsuario - HTTP 201 CREATED
    [Documentation]    Test case to validate creating a Usuario with a valid CPF.
    [Tags]    POST    CreateUsuario    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - USUARIO - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST CreateUsuario - HTTP 409 CONFLICT
    [Documentation]    Test case to validate that a repeated CPF returns HTTP 409.
    [Tags]    POST    CreateUsuario    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - USUARIO - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST CreateUsuario - HTTP 400 BAD REQUEST
    [Documentation]    Test case to validate that an invalid CPF returns HTTP 400.
    [Tags]    POST    CreateUsuario    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - USUARIO - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
