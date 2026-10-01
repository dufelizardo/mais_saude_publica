*** Settings ***
Resource    ../../../src/scenario/agendamento/get_agendamentos_by_paciente/get_agendamentos_by_paciente_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Agendamentos By Paciente
Metadata    Test Suite Description        This test suite validates the GET Agendamentos by Paciente endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAgendamentosByPaciente    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-27
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Agendamentos By Paciente - HTTP 200 OK
    [Documentation]    Test case to validate the GET Agendamentos by Paciente endpoint with HTTP 200 OK response.
    [Tags]    GET    GetAgendamentosByPaciente    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - GET BY PACIENTE - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Agendamentos By Paciente - HTTP 404 NOT FOUND (Paciente Sem Agendamentos)
    [Documentation]    Test case to validate the GET Agendamentos by Paciente endpoint with HTTP 404 NOT FOUND
    ...    response, when the Paciente has no Agendamentos.
    [Tags]    GET    GetAgendamentosByPaciente    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - GET BY PACIENTE - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
