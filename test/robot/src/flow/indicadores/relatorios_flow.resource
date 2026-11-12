*** Settings ***
Library     Collections
Library     DateTime
Resource    ../../pom/indicadores/relatorios_pom.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Relatorios - Catalogo - Status Code 200
    [Documentation]    The catalog brings the eight reports.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Catalogo De Relatorios
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${RELATORIO_CATALOGO_SCHEMA}
    ${codigos}=    Evaluate    [r["codigo"] for r in $response.json()]
    FOR    ${codigo}    IN    PRODUCAO    SITUACAO    PROGRAMAS    VACINACAO    VIGILANCIA    CAMPANHAS_COMUNICACAO    EDUCACAO_EM_SAUDE    PACTUACAO_REDE
        Should Contain    ${codigos}    ${codigo}
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorios - Cada Relatorio - Status Code 200
    [Documentation]    Every report brings a summary and tables.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FOR    ${codigo}    IN    PRODUCAO    SITUACAO    PROGRAMAS    VACINACAO    VIGILANCIA    CAMPANHAS_COMUNICACAO    EDUCACAO_EM_SAUDE    PACTUACAO_REDE
        ${response}=    GET Relatorio    ${codigo}
        Should Be Equal As Integers    ${response.status_code}    200
        Validate Object Schema    ${response.json()}    ${RELATORIO_SCHEMA}
        Should Not Be Empty    ${response.json()}[resumo]
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorios - Exportacao - Status Code 200
    [Documentation]    The CSV carries the report and is named after it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Exportacao Do Relatorio    VIGILANCIA
    Should Be Equal As Integers    ${response.status_code}    200
    Should Contain    ${response.headers}[Content-Disposition]    relatorio-vigilancia.csv
    ${texto}=    Evaluate    $response.content.decode("utf-8-sig")
    Should Start With    ${texto}    Vigilância em saúde;
    Should Contain    ${texto}    Indicador;Valor
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorios - Inexistente - Status Code 404
    [Documentation]    An unknown report is not found.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Relatorio    INEXISTENTE
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorios - Periodo Invalido - Status Code 400
    [Documentation]    The end before the start is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${ontem}=    Subtract Time From Date    ${hoje}    1 day    result_format=%Y-%m-%d
    ${params}=    Create Dictionary    de=${hoje}    ate=${ontem}
    ${response}=    GET Relatorio    PRODUCAO    ${params}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorios - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogo'    Relatorios - Catalogo - Status Code 200
    Run Keyword If    '${meuHTTP}' == '200-relatorios'    Relatorios - Cada Relatorio - Status Code 200
    Run Keyword If    '${meuHTTP}' == '200-exportacao'    Relatorios - Exportacao - Status Code 200
    Run Keyword If    '${meuHTTP}' == '404-relatorio'    Relatorios - Inexistente - Status Code 404
    Run Keyword If    '${meuHTTP}' == '400-periodo'    Relatorios - Periodo Invalido - Status Code 400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

