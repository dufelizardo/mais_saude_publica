*** Settings ***
Resource    ../../../flow/federal/update_horario_atendimento_federal/update_horario_atendimento_federal_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
FEDERAL - UPDATE HORARIO DE ATENDIMENTO - PATCH
    [Documentation]    Test scenario for updating a Federal unit's horario de atendimento
    ...    (PATCH /api/v1/federal/horario-de-atendimento/{nome}) on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Federal - Update Horario De Atendimento - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
