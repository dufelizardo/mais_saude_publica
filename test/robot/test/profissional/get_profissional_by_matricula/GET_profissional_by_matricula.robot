*** Settings ***
Resource    ../../../src/scenario/profissional/get_profissional_by_matricula/get_profissional_by_matricula_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Profissional By Matricula
Metadata    Test Suite Description        This test suite validates the GET Profissional by matrícula endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetProfissionalByMatricula    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-24
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Profissional By Matricula - HTTP 200 OK
    [Documentation]    Test case to validate the GET Profissional by matrícula endpoint with HTTP 200 OK
    ...    response.
    [Tags]    GET    GetProfissionalByMatricula    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - GET BY MATRICULA - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Profissional By Matricula - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Profissional by matrícula endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    GET    GetProfissionalByMatricula    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - GET BY MATRICULA - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
