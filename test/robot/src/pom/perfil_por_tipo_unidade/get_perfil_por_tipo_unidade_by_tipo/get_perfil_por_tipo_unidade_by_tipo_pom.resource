*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${PERFIL_POR_TIPO_UNIDADE_ENDPOINT}    /perfil-por-tipo-unidade
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Perfil Por Tipo Unidade By Tipo
    [Documentation]    Sends a GET request to resolve the Perfil Administrativo configured for a tipo de unidade
    ...    and returns the response.
    [Arguments]    ${tipo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${response}=    GET On Session
    ...    msp_session
    ...    ${BASE_URL}${PERFIL_POR_TIPO_UNIDADE_ENDPOINT}/tipo/${tipo}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
