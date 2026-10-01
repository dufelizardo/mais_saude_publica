*** Settings ***
Resource    ../../../flow/procedimento_regulado/update_procedimento_regulado/update_procedimento_regulado_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PROCEDIMENTO REGULADO - UPDATE - PATCH
    [Documentation]    Test scenario for PATCH /api/v1/procedimento-regulado/{uuid} on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Procedimento Regulado - Update - PATCH - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
