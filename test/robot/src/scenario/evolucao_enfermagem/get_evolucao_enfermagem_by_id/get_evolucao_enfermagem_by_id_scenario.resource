*** Settings ***
Resource    ../../../flow/evolucao_enfermagem/get_evolucao_enfermagem_by_id/get_evolucao_enfermagem_by_id_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
EVOLUCAO ENFERMAGEM - GET BY ID - GET
    [Documentation]    Test scenario for fetching an EvolucaoEnfermagem by id
    ...    (GET /api/v1/evolucao-enfermagem/{uuid}) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Evolucao Enfermagem - Get By Id - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
