*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${PROCESSO_ADMINISTRATIVO_ENDPOINT}    /processo-administrativo
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Processo Administrativo By Id
    [Documentation]    Sends a GET request to fetch a Processo Administrativo by its uuid and returns the response.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${response}=    GET On Session
    ...    msp_session
    ...    ${BASE_URL}${PROCESSO_ADMINISTRATIVO_ENDPOINT}/${uuid}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
