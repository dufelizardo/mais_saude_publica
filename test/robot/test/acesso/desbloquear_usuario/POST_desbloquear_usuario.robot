*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST DesbloquearUsuario
Metadata    Test Suite Description        This test suite validates the POST /api/v1/usuario/{uuid}/desbloqueio endpoint (ADR-0068) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    DesbloquearUsuario    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST DesbloquearUsuario - HTTP 422 UNPROCESSABLE ENTITY
    [Documentation]    Test case to validate that unlocking a user who is not blocked returns HTTP 422.
    [Tags]    POST    DesbloquearUsuario    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - DESBLOQUEIO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST DesbloquearUsuario - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that a non-existent Usuario returns HTTP 404.
    [Tags]    POST    DesbloquearUsuario    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - DESBLOQUEIO - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
