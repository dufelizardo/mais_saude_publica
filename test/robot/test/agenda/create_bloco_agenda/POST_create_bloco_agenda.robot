*** Settings ***
Resource    ../../../src/scenario/agenda/create_bloco_agenda/create_bloco_agenda_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST CreateBlocoAgenda
Metadata    Test Suite Description        This test suite validates the POST /api/v1/agenda/bloco endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateBlocoAgenda    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST CreateBlocoAgenda - HTTP 201 CREATED
    [Documentation]    Bloco com quatro vagas na próxima segunda.
    [Tags]    POST    CreateBlocoAgenda    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    BLOCO AGENDA - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST CreateBlocoAgenda - HTTP 400 BAD REQUEST (Fim Antes Do Inicio)
    [Documentation]    Fim antes do início.
    [Tags]    POST    CreateBlocoAgenda    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    BLOCO AGENDA - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST CreateBlocoAgenda - HTTP 409 CONFLICT (Bloco Cruzado)
    [Documentation]    Bloco cruzado em outra unidade.
    [Tags]    POST    CreateBlocoAgenda    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    BLOCO AGENDA - CREATE - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
