*** Settings ***
Resource    ../../../src/scenario/agenda/encerramento_bloco_agenda/encerramento_bloco_agenda_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST EncerramentoBlocoAgenda
Metadata    Test Suite Description        This test suite validates the POST /api/v1/agenda/bloco/{uuid}/encerramento endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    EncerramentoBlocoAgenda    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST EncerramentoBlocoAgenda - HTTP 200 OK
    [Documentation]    Encerramento tira as vagas.
    [Tags]    POST    EncerramentoBlocoAgenda    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    BLOCO AGENDA - ENCERRAMENTO - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST EncerramentoBlocoAgenda - HTTP 404 NOT FOUND
    [Documentation]    Bloco inexistente.
    [Tags]    POST    EncerramentoBlocoAgenda    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    BLOCO AGENDA - ENCERRAMENTO - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
