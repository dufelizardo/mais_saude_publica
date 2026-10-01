*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST RedefinirSenhaUsuario
Metadata    Test Suite Description        This test suite validates the POST /api/v1/usuario/{uuid}/redefinicao-senha endpoint (ADR-0069) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    RedefinirSenhaUsuario    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST RedefinirSenhaUsuario - HTTP 200 OK
    [Documentation]    Test case to validate setting a provisional password.
    [Tags]    POST    RedefinirSenhaUsuario    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - REDEFINICAO SENHA - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST RedefinirSenhaUsuario - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that a non-existent Usuario returns HTTP 404.
    [Tags]    POST    RedefinirSenhaUsuario    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - REDEFINICAO SENHA - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST RedefinirSenhaUsuario - HTTP 400 BAD REQUEST
    [Documentation]    Test case to validate that a too-short password returns HTTP 400.
    [Tags]    POST    RedefinirSenhaUsuario    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - REDEFINICAO SENHA - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
