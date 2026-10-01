*** Settings ***
Library    Collections
Resource    ../../../pom/auditoria/exportar_auditoria/exportar_auditoria_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Exportação da trilha em CSV e política de retenção (ADR-0082). Com o login desligado, a rota responde a qualquer um.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Exportar Auditoria - Status Code 200
    [Documentation]    Exports the last day, expecting HTTP 200, a CSV attachment and the header row.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    __import__("datetime").date.today().isoformat()
    ${response}=    GET Exportar Auditoria    desde=${hoje}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Start With    ${response.headers}[Content-Type]    text/csv
    Should Start With    ${response.headers}[Content-Disposition]    attachment; filename="auditoria-
    ${texto}=    Evaluate    $response.content.decode("utf-8")
    Should Start With    ${texto}    \ufeffData e hora (Brasília);Usuário (CPF);
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Exportar Auditoria - Status Code 400
    [Documentation]    Exports with an inverted period, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Exportar Auditoria    desde=2026-09-30    ate=2026-09-01
    Should Be Equal As Integers    ${response.status_code}    400
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Politica Auditoria - Status Code 200
    [Documentation]    Reads the retention policy, expecting HTTP 200 and the retention in years.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Politica Auditoria
    Should Be Equal As Integers    ${response.status_code}    200
    Dictionary Should Contain Key    ${response.json()}    retencaoAnos
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Auditoria - Exportar - GET - By HTTP Status Code
    [Documentation]    Executes the Exportar Auditoria scenario for the status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Exportar Auditoria - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Exportar Auditoria - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Exportar Auditoria test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
