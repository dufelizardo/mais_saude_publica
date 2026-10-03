*** Settings ***
Resource    ../../../src/scenario/agenda/bloqueio_agenda/bloqueio_agenda_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST BloqueioAgenda
Metadata    Test Suite Description        This test suite validates the POST /api/v1/agenda/bloqueio endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    BloqueioAgenda    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST BloqueioAgenda - HTTP 201 CREATED
    [Documentation]    Bloqueio e remoção.
    [Tags]    POST    BloqueioAgenda    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    BLOQUEIO AGENDA - POST DELETE    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST BloqueioAgenda - HTTP 400 BAD REQUEST (Sem Profissional Nem Unidade)
    [Documentation]    Bloqueio sem alvo.
    [Tags]    POST    BloqueioAgenda    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    BLOQUEIO AGENDA - POST DELETE    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST BloqueioAgenda - HTTP 404 NOT FOUND (Remover Inexistente)
    [Documentation]    Remoção de bloqueio inexistente.
    [Tags]    POST    BloqueioAgenda    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    BLOQUEIO AGENDA - POST DELETE    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
