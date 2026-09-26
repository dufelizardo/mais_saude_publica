*** Settings ***
Resource    ../../../src/scenario/evolucao_enfermagem/update_evolucao_enfermagem/update_evolucao_enfermagem_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Evolucao Enfermagem
Metadata    Test Suite Description        This test suite validates the PATCH update EvolucaoEnfermagem endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateEvolucaoEnfermagem    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Evolucao Enfermagem - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update EvolucaoEnfermagem endpoint with HTTP 200 OK
    ...    response.
    [Tags]    PATCH    UpdateEvolucaoEnfermagem    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVOLUCAO ENFERMAGEM - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Evolucao Enfermagem - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update EvolucaoEnfermagem endpoint with HTTP 404 NOT
    ...    FOUND response.
    [Tags]    PATCH    UpdateEvolucaoEnfermagem    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVOLUCAO ENFERMAGEM - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
