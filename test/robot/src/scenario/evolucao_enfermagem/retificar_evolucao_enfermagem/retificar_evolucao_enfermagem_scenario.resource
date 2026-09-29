*** Settings ***
Resource    ../../../flow/evolucao_enfermagem/retificar_evolucao_enfermagem/retificar_evolucao_enfermagem_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
EVOLUCAO ENFERMAGEM - RETIFICACAO - POST
    [Documentation]    Test scenario for retifying a Evolucao Enfermagem (POST /api/v1/evolucao-enfermagem/{uuid}/retificacao) on the Mais Saúde
    ...    Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Evolucao Enfermagem - Retificacao - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

