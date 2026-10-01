*** Settings ***
Resource    ../../../flow/dispensacao/create_dispensacao/create_dispensacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
DISPENSACAO - CREATE - POST
    [Documentation]    Test scenario for creating a Dispensacao (POST /api/v1/dispensacao/) on the Mais
    ...    Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Dispensacao - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DISPENSACAO - CREATE - POST - Lote Inexistente
    [Documentation]    Test scenario for creating a Dispensacao pointing at a loteId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Dispensacao - Status Code 404 - Lote Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DISPENSACAO - CREATE - POST - Estoque Insuficiente
    [Documentation]    Test scenario for creating a Dispensacao requesting more than the Lote's
    ...    available quantidade, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Dispensacao - Status Code 422 - Estoque Insuficiente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
