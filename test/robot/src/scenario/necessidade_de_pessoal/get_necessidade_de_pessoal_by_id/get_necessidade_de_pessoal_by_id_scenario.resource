*** Settings ***
Resource    ../../../flow/necessidade_de_pessoal/get_necessidade_de_pessoal_by_id/get_necessidade_de_pessoal_by_id_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
NECESSIDADE DE PESSOAL - GET BY ID - GET
    [Documentation]    Test scenario for fetching a Necessidade De Pessoal by id
    ...    (GET /api/v1/necessidade-de-pessoal/{uuid}) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Necessidade De Pessoal - Get By Id - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
