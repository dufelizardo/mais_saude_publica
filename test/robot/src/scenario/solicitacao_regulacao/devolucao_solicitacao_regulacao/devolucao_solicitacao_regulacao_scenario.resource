*** Settings ***
Resource    ../../../flow/solicitacao_regulacao/devolucao_solicitacao_regulacao/devolucao_solicitacao_regulacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
SOLICITACAO REGULACAO - DEVOLUCAO - POST
    [Documentation]    Test scenario for POST /api/v1/solicitacao-regulacao/{uuid}/devolucao on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Solicitacao Regulacao - Devolucao - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
