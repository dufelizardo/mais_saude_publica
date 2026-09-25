*** Settings ***
Resource    ../../../pom/perfil_administrativo/get_perfil_administrativo_by_id/get_perfil_administrativo_by_id_pom.resource
Resource    ../create_perfil_administrativo/create_perfil_administrativo_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Perfil Administrativo By Id
    [Documentation]    Calls the GET Get Perfil Administrativo By Id keyword from the POM.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Perfil Administrativo By Id    ${uuid}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Perfil Administrativo By Id - Status Code 200
    [Documentation]    Seeds a fresh Perfil Administrativo (unless ${uuid} is informed) and fetches it by id,
    ...    expecting HTTP 200.
    [Arguments]    ${uuid}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${uuid}' == '${EMPTY}'
        ${uuid}    ${codigo}=    Seed A Perfil Administrativo
    END
    ${response}=    Call GET Get Perfil Administrativo By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${PERFIL_ADMINISTRATIVO_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Perfil Administrativo By Id - Status Code 404
    [Documentation]    Fetches a Perfil Administrativo by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    Call GET Get Perfil Administrativo By Id    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Perfil Administrativo - Get By Id - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Perfil Administrativo By Id test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Perfil Administrativo By Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Perfil Administrativo By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Perfil Administrativo By Id test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
