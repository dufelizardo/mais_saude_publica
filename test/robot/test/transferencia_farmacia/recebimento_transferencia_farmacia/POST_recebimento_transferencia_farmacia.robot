*** Settings ***
Resource    ../../../src/scenario/transferencia_farmacia/recebimento_transferencia_farmacia/recebimento_transferencia_farmacia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST RecebimentoTransferenciaFarmacia
Metadata    Test Suite Description        This test suite validates the POST /api/v1/transferencia-farmacia/{uuid}/recebimento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    RecebimentoTransferenciaFarmacia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-29
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST RecebimentoTransferenciaFarmacia - HTTP 200 OK
    [Documentation]    Test case to validate that a full receipt by another profissional moves the transfer to RECEBIDA and fills the destination Lote.
    [Tags]    POST    RecebimentoTransferenciaFarmacia    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - RECEBIMENTO - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST RecebimentoTransferenciaFarmacia - HTTP 400 BAD REQUEST (Divergencia Sem Motivo)
    [Documentation]    Test case to validate that receiving less than was sent requires a motivoDivergencia.
    [Tags]    POST    RecebimentoTransferenciaFarmacia    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - RECEBIMENTO - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST RecebimentoTransferenciaFarmacia - HTTP 422 UNPROCESSABLE ENTITY (Mesmo Profissional Do Envio)
    [Documentation]    Test case to validate that the sender cannot register the receipt.
    [Tags]    POST    RecebimentoTransferenciaFarmacia    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - RECEBIMENTO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
