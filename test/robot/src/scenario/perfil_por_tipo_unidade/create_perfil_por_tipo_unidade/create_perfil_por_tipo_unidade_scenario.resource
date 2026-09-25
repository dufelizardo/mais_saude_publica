*** Settings ***
Resource    ../../../flow/perfil_por_tipo_unidade/create_perfil_por_tipo_unidade/create_perfil_por_tipo_unidade_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PERFIL POR TIPO UNIDADE - CREATE - POST
    [Documentation]    Test scenario for associating a Perfil Administrativo to a tipo de unidade
    ...    (POST /api/v1/perfil-por-tipo-unidade/) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Perfil Por Tipo Unidade - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
