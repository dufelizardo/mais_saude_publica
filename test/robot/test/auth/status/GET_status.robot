*** Settings ***
Resource    ../../../src/scenario/auth/status/status_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Auth Status
Metadata    Test Suite Description        This test suite validates the GET auth status endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    AuthStatus    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-26
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Auth Status - HTTP 200 OK
    [Documentation]    Test case to validate the GET auth status endpoint with HTTP 200 OK response,
    ...    always public regardless of the app.security.enabled toggle.
    [Tags]    GET    AuthStatus    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUTH - STATUS - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
