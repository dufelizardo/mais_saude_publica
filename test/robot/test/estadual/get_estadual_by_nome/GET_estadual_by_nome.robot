*** Settings ***
Resource    ../../../src/scenario/estadual/get_estadual_by_nome/get_estadual_by_nome_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Estadual By Nome
Metadata    Test Suite Description        This test suite validates the GET Estadual by nome endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetEstadualByNome    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-144: Validate GET Get Estadual By Nome - HTTP 200 OK
    [Documentation]    Test case to validate the GET Estadual by nome endpoint with HTTP 200 OK response.
    [Tags]    GET    GetEstadualByNome    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - GET BY NOME - GET    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-144: Validate GET Get Estadual By Nome - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Estadual by nome endpoint with HTTP 404 NOT FOUND response.
    [Tags]    GET    GetEstadualByNome    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - GET BY NOME - GET    ${get_estadual_by_nome_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-144: Validate GET Get Estadual By Nome - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the GET Estadual by nome endpoint with HTTP 400 BAD REQUEST
    ...    response, when the nome in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    GET    GetEstadualByNome    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - GET BY NOME - GET    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
