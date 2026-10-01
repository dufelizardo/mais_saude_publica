*** Settings ***
Resource    ../../../src/scenario/consulta/retificar_consulta/retificar_consulta_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Retificar Consulta
Metadata    Test Suite Description        This test suite validates the POST Consulta retification endpoint (ADR-0062) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    RetificarConsulta    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-29
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Retificar Consulta - HTTP 201 CREATED
    [Documentation]    Test case to validate that retifying a Consulta creates a new version and keeps the original, flagged as retificado.
    [Tags]    POST    RetificarConsulta    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CONSULTA - RETIFICACAO - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Retificar Consulta - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that retifying a non-existent Consulta returns HTTP 404.
    [Tags]    POST    RetificarConsulta    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CONSULTA - RETIFICACAO - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Retificar Consulta - HTTP 422 UNPROCESSABLE ENTITY (Versao Ja Retificada)
    [Documentation]    Test case to validate that a Consulta version that was already retified cannot be retified again.
    [Tags]    POST    RetificarConsulta    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CONSULTA - RETIFICACAO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
