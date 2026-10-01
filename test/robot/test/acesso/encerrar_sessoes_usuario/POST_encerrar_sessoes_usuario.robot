*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST EncerrarSessoesUsuario
Metadata    Test Suite Description        This test suite validates the POST /api/v1/usuario/{uuid}/encerramento-de-sessoes endpoint (ADR-0078) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    EncerrarSessoesUsuario    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST EncerrarSessoesUsuario - HTTP 200 OK
    [Documentation]    Test case to validate that ending the sessions of an existing Usuario returns HTTP 200.
    [Tags]    POST    EncerrarSessoesUsuario    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - ENCERRAMENTO DE SESSOES - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST EncerrarSessoesUsuario - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that a non-existent Usuario returns HTTP 404.
    [Tags]    POST    EncerrarSessoesUsuario    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - ENCERRAMENTO DE SESSOES - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
