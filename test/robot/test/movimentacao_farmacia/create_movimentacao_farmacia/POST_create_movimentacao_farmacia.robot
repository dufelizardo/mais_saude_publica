*** Settings ***
Resource    ../../../src/scenario/movimentacao_farmacia/create_movimentacao_farmacia/create_movimentacao_farmacia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Movimentacao Farmacia
Metadata    Test Suite Description        This test suite validates the POST stock movement endpoint (livro de movimentação da Farmácia) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateMovimentacaoFarmacia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-28
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Movimentacao Farmacia - HTTP 201 CREATED (Perda)
    [Documentation]    Test case to validate that a PERDA is registered and deducted from the Lote's balance.
    [Tags]    POST    CreateMovimentacaoFarmacia    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MOVIMENTACAO FARMACIA - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Movimentacao Farmacia - HTTP 400 BAD REQUEST (Perda Sem Motivo)
    [Documentation]    Test case to validate that a PERDA without motivoPerda is rejected and the balance is kept.
    [Tags]    POST    CreateMovimentacaoFarmacia    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MOVIMENTACAO FARMACIA - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Movimentacao Farmacia - HTTP 422 UNPROCESSABLE ENTITY (Perda Maior Que O Saldo)
    [Documentation]    Test case to validate that a PERDA larger than the balance is rejected and the balance is kept.
    [Tags]    POST    CreateMovimentacaoFarmacia    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MOVIMENTACAO FARMACIA - CREATE - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
