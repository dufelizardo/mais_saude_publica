*** Settings ***
Resource    ../../../src/scenario/auth/login/login_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Login
Metadata    Test Suite Description        This test suite validates the POST login endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    Login    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-26
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Login - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST login endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields are omitted.
    [Tags]    POST    Login    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUTH - LOGIN - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Login - HTTP 401 UNAUTHORIZED (Credenciais Inexistentes)
    [Documentation]    Test case to validate the POST login endpoint with HTTP 401 UNAUTHORIZED
    ...    response, when the identifier doesn't match any Usuario.
    [Tags]    POST    Login    HTTP401
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUTH - LOGIN - POST    401
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
