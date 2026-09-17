*** Settings ***
Resource    ../../../flow/unidade_saude/update_horario_atendimento_unidade_saude/update_horario_atendimento_unidade_saude_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UNIDADE SAUDE - UPDATE HORARIO DE ATENDIMENTO - PATCH
    [Documentation]    Test scenario for updating a Unidade de Saúde unit's horario de atendimento
    ...    (PATCH /api/v1/unidade-saude/horario-de-atendimento/{nome}) on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Unidade Saude - Update Horario De Atendimento - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
