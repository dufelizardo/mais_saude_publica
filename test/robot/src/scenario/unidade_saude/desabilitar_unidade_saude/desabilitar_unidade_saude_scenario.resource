*** Settings ***
Resource    ../../../flow/unidade_saude/desabilitar_unidade_saude/desabilitar_unidade_saude_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UNIDADE SAUDE - DESABILITAR - DELETE
    [Documentation]    Test scenario for enabling/disabling a Unidade de Saúde unit
    ...    (DELETE /api/v1/unidade-saude/des-habilitar/{nome}) on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Unidade Saude - Desabilitar - DELETE - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
