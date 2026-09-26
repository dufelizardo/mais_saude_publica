*** Settings ***
Resource    ../../../flow/evolucao_enfermagem/update_evolucao_enfermagem/update_evolucao_enfermagem_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
EVOLUCAO ENFERMAGEM - UPDATE - PATCH
    [Documentation]    Test scenario for updating an EvolucaoEnfermagem
    ...    (PATCH /api/v1/evolucao-enfermagem/{uuid}) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Evolucao Enfermagem - Update - PATCH - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
