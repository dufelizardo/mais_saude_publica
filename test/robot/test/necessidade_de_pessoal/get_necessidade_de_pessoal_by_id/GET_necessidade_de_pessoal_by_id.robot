*** Settings ***
Resource    ../../../src/scenario/necessidade_de_pessoal/get_necessidade_de_pessoal_by_id/get_necessidade_de_pessoal_by_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Necessidade De Pessoal By Id
Metadata    Test Suite Description        This test suite validates the GET Necessidade De Pessoal by id endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetNecessidadeDePessoalById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Necessidade De Pessoal By Id - HTTP 200 OK
    [Documentation]    Test case to validate the GET Necessidade De Pessoal by id endpoint with HTTP 200 OK
    ...    response.
    [Tags]    GET    GetNecessidadeDePessoalById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - GET BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Necessidade De Pessoal By Id - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Necessidade De Pessoal by id endpoint with HTTP 404 NOT
    ...    FOUND response, when the uuid doesn't exist.
    [Tags]    GET    GetNecessidadeDePessoalById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - GET BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
