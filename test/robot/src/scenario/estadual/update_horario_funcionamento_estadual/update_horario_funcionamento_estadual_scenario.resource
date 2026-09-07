*** Settings ***
Resource    ../../../flow/estadual/update_horario_funcionamento_estadual/update_horario_funcionamento_estadual_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
ESTADUAL - UPDATE HORARIO DE FUNCIONAMENTO - PATCH
    [Documentation]    Test scenario for updating an Estadual unit's horario de funcionamento
    ...    (PATCH /api/v1/estadual/horario-de-funcionamento/{nome}) on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Estadual - Update Horario De Funcionamento - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
