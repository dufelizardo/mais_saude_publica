*** Settings ***
Resource    ../../../src/scenario/atendimento/create_atendimento/create_atendimento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Atendimento
Metadata    Test Suite Description        This test suite validates the POST create Atendimento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateAtendimento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Atendimento - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Atendimento endpoint with HTTP 201 CREATED
    ...    response.
    [Tags]    POST    CreateAtendimento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ATENDIMENTO - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Atendimento - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Atendimento endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields are omitted.
    [Tags]    POST    CreateAtendimento    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ATENDIMENTO - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Atendimento - HTTP 404 NOT FOUND (Paciente Inexistente)
    [Documentation]    Test case to validate the POST create Atendimento endpoint with HTTP 404 NOT FOUND
    ...    response, when pacienteId points at a Paciente that doesn't exist.
    [Tags]    POST    CreateAtendimento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ATENDIMENTO - CREATE - POST - Paciente Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Atendimento - HTTP 404 NOT FOUND (Profissional Inexistente)
    [Documentation]    Test case to validate the POST create Atendimento endpoint with HTTP 404 NOT FOUND
    ...    response, when profissionalMatricula points at a Profissional that doesn't exist (ver ADR-0034).
    [Tags]    POST    CreateAtendimento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ATENDIMENTO - CREATE - POST - Profissional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate POST Create Atendimento - HTTP 201 CREATED (Com Agendamento)
    [Documentation]    Test case to validate the POST create Atendimento endpoint linked to a pre-existing
    ...    Agendamento, expecting HTTP 201 CREATED and the GET response to carry agendamentoUuid (ver
    ...    ADR-0042).
    [Tags]    POST    CreateAtendimento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ATENDIMENTO - CREATE - POST - Com Agendamento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
