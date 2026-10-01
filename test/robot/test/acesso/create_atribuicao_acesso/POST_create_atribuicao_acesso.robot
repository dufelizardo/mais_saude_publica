*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST CreateAtribuicaoAcesso
Metadata    Test Suite Description        This test suite validates the POST /api/v1/atribuicao-acesso/ endpoint (ADR-0066) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateAtribuicaoAcesso    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST CreateAtribuicaoAcesso - HTTP 201 CREATED
    [Documentation]    Test case to validate granting, repeating (409), revoking (200) and re-revoking (422) an access.
    [Tags]    POST    CreateAtribuicaoAcesso    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - ATRIBUICAO - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST CreateAtribuicaoAcesso - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that a non-existent Usuario returns HTTP 404.
    [Tags]    POST    CreateAtribuicaoAcesso    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - ATRIBUICAO - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST CreateAtribuicaoAcesso - HTTP 400 BAD REQUEST
    [Documentation]    Test case to validate that missing usuarioId/papelId returns HTTP 400.
    [Tags]    POST    CreateAtribuicaoAcesso    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - ATRIBUICAO - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
