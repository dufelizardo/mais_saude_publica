*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${LOGIN_ENDPOINT}    /auth/login
${CONTENT_TYPE}    application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Login
    [Documentation]    Sends a POST request to /api/v1/auth/login and returns the response.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${response}=    POST On Session
    ...    msp_session
    ...    ${BASE_URL}${LOGIN_ENDPOINT}
    ...    json=${payload}
    ...    headers=${headers}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
