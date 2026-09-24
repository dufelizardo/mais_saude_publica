*** Settings ***
Resource    ../../../flow/responsabilidade_administrativa/get_historico_responsabilidade_administrativa/get_historico_responsabilidade_administrativa_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
RESPONSABILIDADE ADMINISTRATIVA - GET HISTORICO - GET
    [Documentation]    Test scenario for fetching the Responsabilidade Administrativa histórico of a Profissional
    ...    (GET /api/v1/responsabilidade-administrativa/profissional/{matricula}) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Responsabilidade Administrativa - Get Historico - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
