*** Settings ***
Resource    ../../../src/scenario/procedimento/create_procedimento/create_procedimento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Procedimento
Metadata    Test Suite Description        This test suite validates the POST create Procedimento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateProcedimento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Procedimento - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Procedimento endpoint with HTTP 201 CREATED
    ...    response.
    [Tags]    POST    CreateProcedimento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Procedimento - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Procedimento endpoint with HTTP 400 BAD
    ...    REQUEST response, when required fields are omitted.
    [Tags]    POST    CreateProcedimento    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Procedimento - HTTP 404 NOT FOUND (Consulta Inexistente)
    [Documentation]    Test case to validate the POST create Procedimento endpoint with HTTP 404 NOT FOUND
    ...    response, when consultaId points at a Consulta that doesn't exist.
    [Tags]    POST    CreateProcedimento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - CREATE - POST - Consulta Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Procedimento - HTTP 404 NOT FOUND (Profissional Inexistente)
    [Documentation]    Test case to validate the POST create Procedimento endpoint with HTTP 404 NOT FOUND
    ...    response, when profissionalMatricula points at a Profissional that doesn't exist (ver ADR-0034).
    [Tags]    POST    CreateProcedimento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - CREATE - POST - Profissional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
