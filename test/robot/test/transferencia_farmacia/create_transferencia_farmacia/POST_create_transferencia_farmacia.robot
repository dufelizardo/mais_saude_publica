*** Settings ***
Resource    ../../../src/scenario/transferencia_farmacia/create_transferencia_farmacia/create_transferencia_farmacia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Transferencia Farmacia
Metadata    Test Suite Description        This test suite validates the POST stock transfer between units endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateTransferenciaFarmacia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-29
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Transferencia Farmacia - HTTP 201 CREATED
    [Documentation]    Test case to validate that a transfer is registered and deducted from the origin Lote.
    [Tags]    POST    CreateTransferenciaFarmacia    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Transferencia Farmacia - HTTP 400 BAD REQUEST (Mesma Unidade)
    [Documentation]    Test case to validate that a transfer to the Lote's own unit is rejected and the balance is kept.
    [Tags]    POST    CreateTransferenciaFarmacia    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Transferencia Farmacia - HTTP 422 UNPROCESSABLE ENTITY (Maior Que O Saldo)
    [Documentation]    Test case to validate that a transfer larger than the balance is rejected and the balance is kept.
    [Tags]    POST    CreateTransferenciaFarmacia    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - CREATE - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
