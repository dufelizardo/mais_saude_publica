*** Settings ***
Resource    ../../../src/scenario/agendamento/falta_agendamento/falta_agendamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST FaltaAgendamento
Metadata    Test Suite Description        This test suite validates the POST /api/v1/agendamento/{uuid}/falta endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    FaltaAgendamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST FaltaAgendamento - HTTP 200 OK
    [Documentation]    Marcação na vaga e falta registrada.
    [Tags]    POST    FaltaAgendamento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - FALTA - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST FaltaAgendamento - HTTP 422 UNPROCESSABLE ENTITY (Fora Da Agenda)
    [Documentation]    Marcação fora das vagas sem encaixe.
    [Tags]    POST    FaltaAgendamento    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - FALTA - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
