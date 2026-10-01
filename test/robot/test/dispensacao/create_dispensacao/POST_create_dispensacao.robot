*** Settings ***
Resource    ../../../src/scenario/dispensacao/create_dispensacao/create_dispensacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Dispensacao
Metadata    Test Suite Description        This test suite validates the POST create Dispensacao endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateDispensacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-26
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Dispensacao - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Dispensacao endpoint with HTTP 201 CREATED
    ...    response.
    [Tags]    POST    CreateDispensacao    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    DISPENSACAO - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Dispensacao - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Dispensacao endpoint with HTTP 400 BAD
    ...    REQUEST response, when required fields are omitted.
    [Tags]    POST    CreateDispensacao    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    DISPENSACAO - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Dispensacao - HTTP 404 NOT FOUND (Lote Inexistente)
    [Documentation]    Test case to validate the POST create Dispensacao endpoint with HTTP 404 NOT
    ...    FOUND response, when loteId points at a Lote that doesn't exist.
    [Tags]    POST    CreateDispensacao    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    DISPENSACAO - CREATE - POST - Lote Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Dispensacao - HTTP 422 UNPROCESSABLE ENTITY (Estoque Insuficiente)
    [Documentation]    Test case to validate the POST create Dispensacao endpoint with HTTP 422
    ...    UNPROCESSABLE ENTITY response, when quantidade exceeds the Lote's available stock (ver
    ...    ADR-0051).
    [Tags]    POST    CreateDispensacao    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    DISPENSACAO - CREATE - POST - Estoque Insuficiente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
