*** Settings ***
Resource    ../../../flow/perfil_por_tipo_unidade/update_perfil_por_tipo_unidade/update_perfil_por_tipo_unidade_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PERFIL POR TIPO UNIDADE - UPDATE - PATCH
    [Documentation]    Test scenario for reassigning the Perfil Administrativo of an existing association
    ...    (PATCH /api/v1/perfil-por-tipo-unidade/{uuid}) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Perfil Por Tipo Unidade - Update - PATCH - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
