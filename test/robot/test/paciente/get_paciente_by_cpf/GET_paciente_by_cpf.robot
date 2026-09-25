*** Settings ***
Resource    ../../../src/scenario/paciente/get_paciente_by_cpf/get_paciente_by_cpf_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Paciente By Cpf
Metadata    Test Suite Description        This test suite validates the GET Paciente by cpf endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetPacienteByCpf    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Paciente By Cpf - HTTP 200 OK
    [Documentation]    Test case to validate the GET Paciente by cpf endpoint with HTTP 200 OK response.
    [Tags]    GET    GetPacienteByCpf    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PACIENTE - GET BY CPF - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Paciente By Cpf - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Paciente by cpf endpoint with HTTP 404 NOT FOUND response.
    [Tags]    GET    GetPacienteByCpf    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PACIENTE - GET BY CPF - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
