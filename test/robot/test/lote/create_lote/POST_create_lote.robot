*** Settings ***
Resource    ../../../src/scenario/lote/create_lote/create_lote_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Lote
Metadata    Test Suite Description        This test suite validates the POST create Lote endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateLote    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-26
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Lote - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Lote endpoint with HTTP 201 CREATED
    ...    response.
    [Tags]    POST    CreateLote    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LOTE - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Lote - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Lote endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields are omitted.
    [Tags]    POST    CreateLote    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LOTE - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Lote - HTTP 404 NOT FOUND (Medicamento Inexistente)
    [Documentation]    Test case to validate the POST create Lote endpoint with HTTP 404 NOT FOUND
    ...    response, when medicamentoId points at a Medicamento that doesn't exist.
    [Tags]    POST    CreateLote    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LOTE - CREATE - POST - Medicamento Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Lote - HTTP 404 NOT FOUND (Unidade Inexistente)
    [Documentation]    Test case to validate the POST create Lote endpoint with HTTP 404 NOT FOUND
    ...    response, when unidadeId points at a UnidadeDeSaude that doesn't exist.
    [Tags]    POST    CreateLote    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LOTE - CREATE - POST - Unidade Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
