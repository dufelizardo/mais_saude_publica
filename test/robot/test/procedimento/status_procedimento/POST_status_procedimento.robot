*** Settings ***
Resource    ../../../src/scenario/procedimento/status_procedimento/status_procedimento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Status Procedimento
Metadata    Test Suite Description        This test suite validates the POST Procedimento outcome endpoint (ADR-0062) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    StatusProcedimento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-29
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Status Procedimento - HTTP 200 OK
    [Documentation]    Test case to validate that a scheduled Procedimento can be registered as REALIZADO.
    [Tags]    POST    StatusProcedimento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - STATUS - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Status Procedimento - HTTP 422 UNPROCESSABLE ENTITY (Desfecho Ja Registrado)
    [Documentation]    Test case to validate that the outcome of a Procedimento is registered only once.
    [Tags]    POST    StatusProcedimento    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - STATUS - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
