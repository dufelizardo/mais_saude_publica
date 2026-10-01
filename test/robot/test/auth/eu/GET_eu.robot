*** Settings ***
Resource    ../../../src/scenario/auth/eu/eu_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Auth Eu
Metadata    Test Suite Description        This test suite validates the GET who-is-logged-in endpoint (ADR-0065) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    AuthEu    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Auth Eu - HTTP 401 UNAUTHORIZED (Sem Token)
    [Documentation]    Test case to validate that asking who is logged in without a token returns HTTP 401.
    [Tags]    GET    AuthEu    HTTP401
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUTH - EU - GET    401
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
