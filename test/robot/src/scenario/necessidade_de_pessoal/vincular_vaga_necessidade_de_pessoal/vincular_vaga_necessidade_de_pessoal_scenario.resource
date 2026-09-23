*** Settings ***
Resource    ../../../flow/necessidade_de_pessoal/vincular_vaga_necessidade_de_pessoal/vincular_vaga_necessidade_de_pessoal_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
NECESSIDADE DE PESSOAL - VINCULAR VAGA - PATCH
    [Documentation]    Test scenario for vincular-ing a Vaga to a Necessidade De Pessoal
    ...    (PATCH /api/v1/necessidade-de-pessoal/{uuid}/vincular-vaga) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Necessidade De Pessoal - Vincular Vaga - PATCH - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
