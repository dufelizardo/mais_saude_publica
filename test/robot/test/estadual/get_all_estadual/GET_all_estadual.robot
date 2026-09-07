*** Settings ***
Resource    ../../../src/scenario/estadual/get_all_estadual/get_all_estadual_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET All Estadual
Metadata    Test Suite Description        This test suite validates the GET all Estadual endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAllEstadual    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-144: Validate GET Get All Estadual - HTTP 200 OK
    [Documentation]    Test case to validate the GET all Estadual endpoint with HTTP 200 OK response.
    [Tags]    GET    GetAllEstadual    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - GET ALL - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-144: Validate GET Get All Estadual - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the GET all Estadual endpoint with HTTP 400 BAD REQUEST response,
    ...    when the URL itself contains a raw null byte (AQUAQE-216).
    [Tags]    GET    GetAllEstadual    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - GET ALL - GET    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
