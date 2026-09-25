*** Settings ***
Resource    ../../../flow/perfil_por_tipo_unidade/get_perfil_por_tipo_unidade_by_tipo/get_perfil_por_tipo_unidade_by_tipo_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PERFIL POR TIPO UNIDADE - GET BY TIPO - GET
    [Documentation]    Test scenario for resolving the Perfil Administrativo configured for a tipo de unidade
    ...    (GET /api/v1/perfil-por-tipo-unidade/tipo/{tipo}) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Perfil Por Tipo Unidade - Get By Tipo - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
