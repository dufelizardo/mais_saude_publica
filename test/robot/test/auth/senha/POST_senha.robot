*** Settings ***
Resource    ../../../src/scenario/auth/senha/senha_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST AuthSenha
Metadata    Test Suite Description        This test suite validates the POST /api/v1/auth/senha endpoint (ADR-0069) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    AuthSenha    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST AuthSenha - HTTP 401 UNAUTHORIZED (Sem Token)
    [Documentation]    Test case to validate that changing the password without a token returns HTTP 401.
    [Tags]    POST    AuthSenha    HTTP401
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUTH - SENHA - POST    401
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
