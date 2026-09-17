*** Settings ***
Resource    ../../../src/scenario/unidade_saude/get_all_unidade_saude/get_all_unidade_saude_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET All Unidade Saude
Metadata    Test Suite Description        This test suite validates the GET all Unidade de Saúde endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAllUnidadeSaude    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get All Unidade Saude - HTTP 200 OK
    [Documentation]    Test case to validate the GET all Unidade de Saúde endpoint with HTTP 200 OK response.
    [Tags]    GET    GetAllUnidadeSaude    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - GET ALL - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get All Unidade Saude - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the GET all Unidade de Saúde endpoint with HTTP 400 BAD REQUEST
    ...    response, when the URL itself contains a raw null byte (AQUAQE-216).
    [Tags]    GET    GetAllUnidadeSaude    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - GET ALL - GET    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
