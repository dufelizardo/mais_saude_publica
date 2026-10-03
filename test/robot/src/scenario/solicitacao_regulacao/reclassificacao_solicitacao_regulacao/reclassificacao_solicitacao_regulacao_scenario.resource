*** Settings ***
Resource    ../../../flow/solicitacao_regulacao/reclassificacao_solicitacao_regulacao/reclassificacao_solicitacao_regulacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
SOLICITACAO REGULACAO - RECLASSIFICACAO - POST
    [Documentation]    Test scenario for POST /api/v1/solicitacao-regulacao/{uuid}/reclassificacao on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Solicitacao Regulacao - Reclassificacao - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
