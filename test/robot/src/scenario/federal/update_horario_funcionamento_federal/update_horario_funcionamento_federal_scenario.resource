*** Settings ***
Resource    ../../../flow/federal/update_horario_funcionamento_federal/update_horario_funcionamento_federal_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
FEDERAL - UPDATE HORARIO DE FUNCIONAMENTO - PATCH
    [Documentation]    Test scenario for updating a Federal unit's horario de funcionamento
    ...    (PATCH /api/v1/federal/horario-de-funcionamento/{nome}) on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Federal - Update Horario De Funcionamento - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
