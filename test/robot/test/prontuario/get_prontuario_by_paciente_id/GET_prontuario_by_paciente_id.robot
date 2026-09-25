*** Settings ***
Resource    ../../../src/scenario/prontuario/get_prontuario_by_paciente_id/get_prontuario_by_paciente_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Prontuario By Paciente Id
Metadata    Test Suite Description        This test suite validates the GET Prontuário by paciente id endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetProntuarioByPacienteId    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Prontuario By Paciente Id - HTTP 200 OK
    [Documentation]    Test case to validate the GET Prontuário by paciente id endpoint with HTTP 200 OK
    ...    response, agregando Atendimento/Consulta/Procedimento.
    [Tags]    GET    GetProntuarioByPacienteId    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRONTUARIO - GET BY PACIENTE ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Prontuario By Paciente Id - HTTP 200 OK (Sem Atendimentos)
    [Documentation]    Test case to validate the GET Prontuário by paciente id endpoint with HTTP 200 OK
    ...    response for a Paciente with no Atendimento yet.
    [Tags]    GET    GetProntuarioByPacienteId    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRONTUARIO - GET BY PACIENTE ID - GET - Sem Atendimentos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate GET Get Prontuario By Paciente Id - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Prontuário by paciente id endpoint with HTTP 404 NOT
    ...    FOUND response.
    [Tags]    GET    GetProntuarioByPacienteId    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRONTUARIO - GET BY PACIENTE ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
