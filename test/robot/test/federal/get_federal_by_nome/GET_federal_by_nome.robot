*** Settings ***
Resource    ../../../src/scenario/federal/get_federal_by_nome/get_federal_by_nome_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Federal By Nome
Metadata    Test Suite Description        This test suite validates the GET Federal by nome endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetFederalByNome    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-143: Validate GET Get Federal By Nome - HTTP 200 OK
    [Documentation]    Test case to validate the GET Federal by nome endpoint with HTTP 200 OK response.
    [Tags]    GET    GetFederalByNome    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - GET BY NOME - GET    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-143: Validate GET Get Federal By Nome - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Federal by nome endpoint with HTTP 404 NOT FOUND response.
    [Tags]    GET    GetFederalByNome    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - GET BY NOME - GET    ${get_federal_by_nome_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-143: Validate GET Get Federal By Nome - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the GET Federal by nome endpoint with HTTP 400 BAD REQUEST
    ...    response, when the nome in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    GET    GetFederalByNome    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - GET BY NOME - GET    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
