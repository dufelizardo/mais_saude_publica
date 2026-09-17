*** Settings ***
Resource    ../../../src/scenario/profissional/get_profissional_by_cpf/get_profissional_by_cpf_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Profissional By Cpf
Metadata    Test Suite Description        This test suite validates the GET Profissional by cpf endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetProfissionalByCpf    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Profissional By Cpf - HTTP 200 OK
    [Documentation]    Test case to validate the GET Profissional by cpf endpoint with HTTP 200 OK response.
    [Tags]    GET    GetProfissionalByCpf    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - GET BY CPF - GET    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Profissional By Cpf - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Profissional by cpf endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    GET    GetProfissionalByCpf    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - GET BY CPF - GET    ${get_profissional_by_cpf_nonexistent_cpf_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate GET Get Profissional By Cpf - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the GET Profissional by cpf endpoint with HTTP 400 BAD REQUEST
    ...    response, when the cpf in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    GET    GetProfissionalByCpf    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - GET BY CPF - GET    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
