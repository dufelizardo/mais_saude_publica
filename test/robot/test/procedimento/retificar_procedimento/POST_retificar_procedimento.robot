*** Settings ***
Resource    ../../../src/scenario/procedimento/retificar_procedimento/retificar_procedimento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Retificar Procedimento
Metadata    Test Suite Description        This test suite validates the POST Procedimento retification endpoint (ADR-0062) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    RetificarProcedimento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-29
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Retificar Procedimento - HTTP 201 CREATED
    [Documentation]    Test case to validate that retifying a Procedimento creates a new version and keeps the original, flagged as retificado.
    [Tags]    POST    RetificarProcedimento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - RETIFICACAO - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Retificar Procedimento - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that retifying a non-existent Procedimento returns HTTP 404.
    [Tags]    POST    RetificarProcedimento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - RETIFICACAO - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Retificar Procedimento - HTTP 422 UNPROCESSABLE ENTITY (Versao Ja Retificada)
    [Documentation]    Test case to validate that a Procedimento version that was already retified cannot be retified again.
    [Tags]    POST    RetificarProcedimento    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - RETIFICACAO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
