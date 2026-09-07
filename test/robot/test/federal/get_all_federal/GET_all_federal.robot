*** Settings ***
Resource    ../../../src/scenario/federal/get_all_federal/get_all_federal_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET All Federal
Metadata    Test Suite Description        This test suite validates the GET all Federal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAllFederal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-143: Validate GET Get All Federal - HTTP 200 OK
    [Documentation]    Test case to validate the GET all Federal endpoint with HTTP 200 OK response.
    [Tags]    GET    GetAllFederal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - GET ALL - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-143: Validate GET Get All Federal - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the GET all Federal endpoint with HTTP 400 BAD REQUEST response,
    ...    when the URL itself contains a raw null byte (AQUAQE-216).
    [Tags]    GET    GetAllFederal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - GET ALL - GET    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
