*** Settings ***
Resource    ../../../src/scenario/transferencia_farmacia/cancelamento_transferencia_farmacia/cancelamento_transferencia_farmacia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST CancelamentoTransferenciaFarmacia
Metadata    Test Suite Description        This test suite validates the POST /api/v1/transferencia-farmacia/{uuid}/cancelamento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CancelamentoTransferenciaFarmacia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-29
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST CancelamentoTransferenciaFarmacia - HTTP 200 OK
    [Documentation]    Test case to validate that cancelling an in-transit transfer returns the balance to the origin Lote.
    [Tags]    POST    CancelamentoTransferenciaFarmacia    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - CANCELAMENTO - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST CancelamentoTransferenciaFarmacia - HTTP 422 UNPROCESSABLE ENTITY (Ja Recebida)
    [Documentation]    Test case to validate that a received transfer cannot be cancelled.
    [Tags]    POST    CancelamentoTransferenciaFarmacia    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - CANCELAMENTO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
