*** Settings ***
Resource    ../../../src/scenario/evolucao_enfermagem/retificar_evolucao_enfermagem/retificar_evolucao_enfermagem_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Retificar Evolucao Enfermagem
Metadata    Test Suite Description        This test suite validates the POST Evolucao Enfermagem retification endpoint (ADR-0062) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    RetificarEvolucaoEnfermagem    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-29
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Retificar Evolucao Enfermagem - HTTP 201 CREATED
    [Documentation]    Test case to validate that retifying a Evolucao Enfermagem creates a new version and keeps the original, flagged as retificado.
    [Tags]    POST    RetificarEvolucaoEnfermagem    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVOLUCAO ENFERMAGEM - RETIFICACAO - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Retificar Evolucao Enfermagem - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that retifying a non-existent Evolucao Enfermagem returns HTTP 404.
    [Tags]    POST    RetificarEvolucaoEnfermagem    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVOLUCAO ENFERMAGEM - RETIFICACAO - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Retificar Evolucao Enfermagem - HTTP 422 UNPROCESSABLE ENTITY (Versao Ja Retificada)
    [Documentation]    Test case to validate that a Evolucao Enfermagem version that was already retified cannot be retified again.
    [Tags]    POST    RetificarEvolucaoEnfermagem    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVOLUCAO ENFERMAGEM - RETIFICACAO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
