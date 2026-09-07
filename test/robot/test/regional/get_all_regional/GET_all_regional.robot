*** Settings ***
Resource    ../../../src/scenario/regional/get_all_regional/get_all_regional_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET All Regional
Metadata    Test Suite Description        This test suite validates the GET all Regional endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAllRegional    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-146: Validate GET Get All Regional - HTTP 200 OK
    [Documentation]    Test case to validate the GET all Regional endpoint with HTTP 200 OK response.
    [Tags]    GET    GetAllRegional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGIONAL - GET ALL - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-146: Validate GET Get All Regional - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the GET all Regional endpoint with HTTP 400 BAD REQUEST response,
    ...    when the URL itself contains a raw null byte (AQUAQE-216).
    [Tags]    GET    GetAllRegional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGIONAL - GET ALL - GET    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
