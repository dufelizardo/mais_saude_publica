*** Settings ***
Resource    ../../../src/scenario/triagem/create_triagem/create_triagem_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Triagem
Metadata    Test Suite Description        This test suite validates the POST create Triagem endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateTriagem    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Triagem - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Triagem endpoint with HTTP 201 CREATED
    ...    response.
    [Tags]    POST    CreateTriagem    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRIAGEM - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Triagem - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Triagem endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields are omitted.
    [Tags]    POST    CreateTriagem    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRIAGEM - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Triagem - HTTP 404 NOT FOUND (Atendimento Inexistente)
    [Documentation]    Test case to validate the POST create Triagem endpoint with HTTP 404 NOT FOUND
    ...    response, when atendimentoId points at an Atendimento that doesn't exist.
    [Tags]    POST    CreateTriagem    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRIAGEM - CREATE - POST - Atendimento Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Triagem - HTTP 404 NOT FOUND (Profissional Inexistente)
    [Documentation]    Test case to validate the POST create Triagem endpoint with HTTP 404 NOT FOUND
    ...    response, when profissionalMatricula points at a Profissional that doesn't exist (ver ADR-0034).
    [Tags]    POST    CreateTriagem    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRIAGEM - CREATE - POST - Profissional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
