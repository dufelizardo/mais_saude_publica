*** Settings ***
Resource    ../../../src/scenario/movimentacao_farmacia/get_extrato_lote/get_extrato_lote_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Extrato Do Lote
Metadata    Test Suite Description        This test suite validates the GET Lote movement ledger endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetExtratoDoLote    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-28
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Extrato Do Lote - HTTP 200 OK
    [Documentation]    Test case to validate the Lote ledger lists the entrada and a later perda, in order.
    [Tags]    GET    GetExtratoDoLote    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MOVIMENTACAO FARMACIA - EXTRATO DO LOTE - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Extrato Do Lote - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the ledger of a non-existent Lote returns HTTP 404.
    [Tags]    GET    GetExtratoDoLote    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MOVIMENTACAO FARMACIA - EXTRATO DO LOTE - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
