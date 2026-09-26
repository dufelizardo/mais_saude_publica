*** Settings ***
Resource    ../../../src/scenario/evolucao_enfermagem/create_evolucao_enfermagem/create_evolucao_enfermagem_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Evolucao Enfermagem
Metadata    Test Suite Description        This test suite validates the POST create EvolucaoEnfermagem endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateEvolucaoEnfermagem    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Evolucao Enfermagem - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create EvolucaoEnfermagem endpoint with HTTP 201
    ...    CREATED response.
    [Tags]    POST    CreateEvolucaoEnfermagem    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVOLUCAO ENFERMAGEM - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Evolucao Enfermagem - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create EvolucaoEnfermagem endpoint with HTTP 400 BAD
    ...    REQUEST response, when required fields are omitted.
    [Tags]    POST    CreateEvolucaoEnfermagem    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVOLUCAO ENFERMAGEM - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Evolucao Enfermagem - HTTP 404 NOT FOUND (Atendimento Inexistente)
    [Documentation]    Test case to validate the POST create EvolucaoEnfermagem endpoint with HTTP 404 NOT
    ...    FOUND response, when atendimentoId points at an Atendimento that doesn't exist.
    [Tags]    POST    CreateEvolucaoEnfermagem    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVOLUCAO ENFERMAGEM - CREATE - POST - Atendimento Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Evolucao Enfermagem - HTTP 404 NOT FOUND (Profissional Inexistente)
    [Documentation]    Test case to validate the POST create EvolucaoEnfermagem endpoint with HTTP 404 NOT
    ...    FOUND response, when profissionalMatricula points at a Profissional that doesn't exist (ver
    ...    ADR-0034).
    [Tags]    POST    CreateEvolucaoEnfermagem    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVOLUCAO ENFERMAGEM - CREATE - POST - Profissional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
