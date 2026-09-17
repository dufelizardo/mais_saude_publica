*** Settings ***
Resource    ../../../flow/unidade_saude/update_horario_funcionamento_unidade_saude/update_horario_funcionamento_unidade_saude_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UNIDADE SAUDE - UPDATE HORARIO DE FUNCIONAMENTO - PATCH
    [Documentation]    Test scenario for updating a Unidade de Saúde unit's horario de funcionamento
    ...    (PATCH /api/v1/unidade-saude/horario-de-funcionamento/{nome}) on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Unidade Saude - Update Horario De Funcionamento - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
