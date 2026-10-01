*** Settings ***
Resource    ../../../flow/movimentacao_farmacia/get_extrato_lote/get_extrato_lote_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
MOVIMENTACAO FARMACIA - EXTRATO DO LOTE - GET
    [Documentation]    Test scenario for a Lote's movement ledger (GET /api/v1/movimentacao-farmacia/lote/{loteId})
    ...    on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Movimentacao Farmacia - Extrato Do Lote - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
