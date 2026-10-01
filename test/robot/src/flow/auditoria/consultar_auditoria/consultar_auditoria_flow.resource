*** Settings ***
Resource    ../../../pom/auditoria/consultar_auditoria/consultar_auditoria_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Trilha de auditoria (ADR-0070/0071). A suíte não tem credencial de login: com o login desligado a consulta é
    aberta; com a exigência de permissão ligada, só AUDITORIA.CONSULTAR (coberto pelo AutorizacaoControllerTest).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Consultar Auditoria - Status Code 200
    [Documentation]    Queries the latest audit events, expecting HTTP 200, a page and valid events.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    tamanho=10
    ${response}=    GET Consultar Auditoria    ${params}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${PAGINA_AUDITORIA_SCHEMA}
    Validate Array Schema    ${response.json()}[itens]    ${EVENTO_AUDITORIA_SCHEMA}
    Should Be Equal As Integers    ${response.json()}[tamanho]    10

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Consultar Auditoria - Status Code 400
    [Documentation]    Queries with an inverted period, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    desde=2026-10-10    ate=2026-10-01
    ${response}=    GET Consultar Auditoria    ${params}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Auditoria - Consultar - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Consultar Auditoria test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Consultar Auditoria - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Consultar Auditoria - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Consultar Auditoria test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

