*** Settings ***
Resource    ../../../flow/necessidade_de_pessoal/update_necessidade_de_pessoal/update_necessidade_de_pessoal_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
NECESSIDADE DE PESSOAL - UPDATE - PATCH
    [Documentation]    Test scenario for updating a Necessidade De Pessoal (PATCH /api/v1/necessidade-de-pessoal/{uuid})
    ...    on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Necessidade De Pessoal - Update - PATCH - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
