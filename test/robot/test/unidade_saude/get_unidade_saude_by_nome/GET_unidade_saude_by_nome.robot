*** Settings ***
Resource    ../../../src/scenario/unidade_saude/get_unidade_saude_by_nome/get_unidade_saude_by_nome_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Unidade Saude By Nome
Metadata    Test Suite Description        This test suite validates the GET Unidade de Saúde by nome endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetUnidadeSaudeByNome    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Unidade Saude By Nome - HTTP 200 OK
    [Documentation]    Test case to validate the GET Unidade de Saúde by nome endpoint with HTTP 200 OK response.
    [Tags]    GET    GetUnidadeSaudeByNome    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - GET BY NOME - GET    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Unidade Saude By Nome - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Unidade de Saúde by nome endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    GET    GetUnidadeSaudeByNome    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - GET BY NOME - GET    ${get_unidade_saude_by_nome_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate GET Get Unidade Saude By Nome - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the GET Unidade de Saúde by nome endpoint with HTTP 400 BAD REQUEST
    ...    response, when the nome in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    GET    GetUnidadeSaudeByNome    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - GET BY NOME - GET    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
