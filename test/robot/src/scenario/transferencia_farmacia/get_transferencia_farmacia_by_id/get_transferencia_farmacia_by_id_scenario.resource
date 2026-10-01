*** Settings ***
Resource    ../../../flow/transferencia_farmacia/get_transferencia_farmacia_by_id/get_transferencia_farmacia_by_id_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
TRANSFERENCIA FARMACIA - GET BY ID - GET
    [Documentation]    Test scenario for fetching a stock transfer by id (GET /api/v1/transferencia-farmacia/{uuid}) on
    ...    the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Transferencia Farmacia - Get By Id - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
