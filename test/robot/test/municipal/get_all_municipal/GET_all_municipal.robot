*** Settings ***
Resource    ../../../src/scenario/municipal/get_all_municipal/get_all_municipal_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET All Municipal
Metadata    Test Suite Description        This test suite validates the GET all Municipal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAllMunicipal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-145: Validate GET Get All Municipal - HTTP 200 OK
    [Documentation]    Test case to validate the GET all Municipal endpoint with HTTP 200 OK response.
    [Tags]    GET    GetAllMunicipal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - GET ALL - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-145: Validate GET Get All Municipal - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the GET all Municipal endpoint with HTTP 400 BAD REQUEST response,
    ...    when the URL itself contains a raw null byte (AQUAQE-216).
    [Tags]    GET    GetAllMunicipal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - GET ALL - GET    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
