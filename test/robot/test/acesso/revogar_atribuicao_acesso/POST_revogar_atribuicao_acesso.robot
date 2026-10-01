*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST RevogarAtribuicaoAcesso
Metadata    Test Suite Description        This test suite validates the POST /api/v1/atribuicao-acesso/{uuid}/revogacao endpoint (ADR-0066) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    RevogarAtribuicaoAcesso    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST RevogarAtribuicaoAcesso - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that revoking a non-existent assignment returns HTTP 404.
    [Tags]    POST    RevogarAtribuicaoAcesso    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - REVOGACAO - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST RevogarAtribuicaoAcesso - HTTP 400 BAD REQUEST
    [Documentation]    Test case to validate that revoking without a reason returns HTTP 400.
    [Tags]    POST    RevogarAtribuicaoAcesso    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - REVOGACAO - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
