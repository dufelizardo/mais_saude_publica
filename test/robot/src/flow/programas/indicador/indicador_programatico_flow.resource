*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/programas/indicador/indicador_programatico_pom.resource
Resource    ../programa/programa_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed HiperDia Na Unidade
    [Documentation]    Offers HiperDia in a fresh UBS; returns the program and the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET Programas
    ${ids}=    Evaluate    [p["uuid"] for p in $lista.json() if p["codigo"] == "HIPERDIA"]
    ${unidade}    ${nome}=    Seed Unidade Para Programa
    ${part}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    POST Participacao Programa    ${ids}[0]    ${part}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${ids}[0]    ${unidade}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Meta Para A Unidade
    [Documentation]    A goal for the unit in the current month.
    [Arguments]    ${unidade}    ${indicador}=ACOMPANHADOS_EM_DIA    ${alvo}=${70}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${inicio}=    Evaluate    datetime.date.today().replace(day=1).isoformat()    modules=datetime
    ${fim}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=30)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    indicador=${indicador}    unidadeId=${unidade}    alvo=${alvo}    inicio=${inicio}    fim=${fim}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicador Programatico - Indicadores - Status Code 200
    [Documentation]    The HiperDia indicators in a fresh unit, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Na Unidade
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Indicadores Do Programa    ${programa}    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Array Schema    ${r.json()}    ${INDICADOR_APURADO_SCHEMA}
    ${codigos}=    Evaluate    [i["indicador"] for i in $r.json()]
    List Should Contain Value    ${codigos}    PA_CONTROLADA
    List Should Not Contain Value    ${codigos}    PRE_NATAL_6_CONSULTAS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicador Programatico - Meta Cadastrada - Status Code 201
    [Documentation]    The goal is registered and the indicator brings it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Na Unidade
    ${payload}=    Meta Para A Unidade    ${unidade}
    ${r}=    POST Meta Programatica    ${programa}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${META_PROGRAMATICA_SCHEMA}
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${i}=    GET Indicadores Do Programa    ${programa}    ${params}
    ${metas}=    Evaluate    [x.get("metaId") for x in $i.json() if x["indicador"] == "ACOMPANHADOS_EM_DIA"]
    Should Be Equal    ${metas}[0]    ${r.json()}[uuid]
    ${l}=    GET Metas Do Programa    ${programa}
    Validate Array Schema    ${l.json()}    ${META_PROGRAMATICA_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicador Programatico - Meta Repetida - Status Code 409
    [Documentation]    One goal per indicator, unit and period.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Na Unidade
    ${payload}=    Meta Para A Unidade    ${unidade}
    POST Meta Programatica    ${programa}    ${payload}
    ${r}=    POST Meta Programatica    ${programa}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicador Programatico - Meta De Outro Programa - Status Code 422
    [Documentation]    The prenatal indicator does not apply to HiperDia.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Na Unidade
    ${payload}=    Meta Para A Unidade    ${unidade}    PRE_NATAL_6_CONSULTAS
    ${r}=    POST Meta Programatica    ${programa}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicador Programatico - Meta Acima De 100 - Status Code 400
    [Documentation]    A percent goal cannot exceed 100.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Na Unidade
    ${payload}=    Meta Para A Unidade    ${unidade}    alvo=${120}
    ${r}=    POST Meta Programatica    ${programa}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicador Programatico - Serie - Status Code 200
    [Documentation]    Twelve monthly points by default.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Na Unidade
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Serie Do Indicador    ${programa}    PA_CONTROLADA    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${SERIE_INDICADOR_SCHEMA}
    Length Should Be    ${r.json()}[pontos]    12
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicador Programatico - Relatorio Csv - Status Code 200
    [Documentation]    The report comes as CSV, with no patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Na Unidade
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Indicadores Csv    ${programa}    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Start With    ${r.headers}[Content-Type]    text/csv
    ${texto}=    Evaluate    $r.content.decode("utf-8")
    Should Contain    ${texto}    PA controlada
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicador Programatico - Meta Excluida - Status Code 204
    [Documentation]    The goal is configuration and can be deleted.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Na Unidade
    ${payload}=    Meta Para A Unidade    ${unidade}
    ${c}=    POST Meta Programatica    ${programa}    ${payload}
    ${r}=    DELETE Meta Programatica    ${c.json()}[uuid]
    Should Be Equal As Integers    ${r.status_code}    204
    ${l}=    GET Metas Do Programa    ${programa}
    ${ids}=    Evaluate    [m["uuid"] for m in $l.json()]
    List Should Not Contain Value    ${ids}    ${c.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicador Programatico - By HTTP Status Code
    [Documentation]    Executes the P6 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-indicadores'    Indicador Programatico - Indicadores - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-meta'    Indicador Programatico - Meta Cadastrada - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-meta'    Indicador Programatico - Meta Repetida - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422-meta'    Indicador Programatico - Meta De Outro Programa - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '400-meta'    Indicador Programatico - Meta Acima De 100 - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-serie'    Indicador Programatico - Serie - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-csv'    Indicador Programatico - Relatorio Csv - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '204-meta'    Indicador Programatico - Meta Excluida - Status Code 204
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Indicador Programatico test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
