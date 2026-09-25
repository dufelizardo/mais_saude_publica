*** Settings ***
Resource    ../../../src/scenario/paciente/get_paciente_by_cartao_sus/get_paciente_by_cartao_sus_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Paciente By Cartao Sus
Metadata    Test Suite Description        This test suite validates the GET Paciente by cartaoSus (CNS) endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetPacienteByCartaoSus    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Paciente By Cartao Sus - HTTP 200 OK
    [Documentation]    Test case to validate the GET Paciente by cartaoSus endpoint with HTTP 200 OK response.
    [Tags]    GET    GetPacienteByCartaoSus    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PACIENTE - GET BY CARTAO SUS - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Paciente By Cartao Sus - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Paciente by cartaoSus endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    GET    GetPacienteByCartaoSus    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PACIENTE - GET BY CARTAO SUS - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
