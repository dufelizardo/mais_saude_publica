*** Settings ***
Resource    ../../../src/scenario/agendamento/create_agendamento/create_agendamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Agendamento
Metadata    Test Suite Description        This test suite validates the POST create Agendamento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateAgendamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Agendamento - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Agendamento endpoint with HTTP 201 CREATED
    ...    response.
    [Tags]    POST    CreateAgendamento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Agendamento - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Agendamento endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields are omitted.
    [Tags]    POST    CreateAgendamento    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Agendamento - HTTP 404 NOT FOUND (Paciente Inexistente)
    [Documentation]    Test case to validate the POST create Agendamento endpoint with HTTP 404 NOT FOUND
    ...    response, when pacienteId points at a Paciente that doesn't exist.
    [Tags]    POST    CreateAgendamento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - CREATE - POST - Paciente Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Agendamento - HTTP 404 NOT FOUND (Profissional Inexistente)
    [Documentation]    Test case to validate the POST create Agendamento endpoint with HTTP 404 NOT FOUND
    ...    response, when profissionalMatricula points at a Profissional that doesn't exist (ver ADR-0034).
    [Tags]    POST    CreateAgendamento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - CREATE - POST - Profissional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
