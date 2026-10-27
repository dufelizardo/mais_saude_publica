*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/indicadores/producao_pom.resource
Resource    ../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base vem pelas APIs dos outros domínios: unidade, profissional, paciente e um atendimento concluído hoje.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Unidade Com Atendimento Concluido
    [Documentation]    Seeds a fresh unit with one atendimento concluded today; returns the unit and the patient name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${agora}=    Evaluate    datetime.datetime.now().replace(minute=0, second=0, microsecond=0).isoformat()    modules=datetime
    ${payload}=    Build Atendimento Payload    ${paciente}    ${matricula}    ${unidade}    status=CONCLUIDO    dataHora=${agora}
    ${r}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${unidade}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Producao Assistencial - Producao Da Unidade - Status Code 200
    [Documentation]    A concluded atendimento in a fresh unit is counted, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${nome}=    Seed Unidade Com Atendimento Concluido
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Producao Assistencial    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${PRODUCAO_ASSISTENCIAL_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[atendimentos]    1
    Length Should Be    ${r.json()}[porHora]    24
    Should Not Contain    ${r.text}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Producao Assistencial - Periodo Invertido - Status Code 400
    [Documentation]    The start cannot be after the end.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    datetime.date.today()    modules=datetime
    ${ontem}=    Evaluate    $hoje - datetime.timedelta(days=1)    modules=datetime
    ${params}=    Create Dictionary    de=${hoje}    ate=${ontem}
    ${r}=    GET Producao Assistencial    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Producao Assistencial - Periodo Acima De Um Ano - Status Code 400
    [Documentation]    The period goes up to one year.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    datetime.date.today()    modules=datetime
    ${de}=    Evaluate    $hoje - datetime.timedelta(days=400)    modules=datetime
    ${params}=    Create Dictionary    de=${de}    ate=${hoje}
    ${r}=    GET Producao Assistencial    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Producao Assistencial - Unidade Inexistente - Status Code 404
    [Documentation]    Unknown unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${params}=    Create Dictionary    unidadeId=${id}
    ${r}=    GET Producao Assistencial    ${params}
    Should Be Equal As Integers    ${r.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Producao Assistencial - Serie - Status Code 200
    [Documentation]    Thirty daily points and twelve monthly points by default.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${nome}=    Seed Unidade Com Atendimento Concluido
    ${dia}=    Create Dictionary    granularidade=DIA    unidadeId=${unidade}
    ${r}=    GET Serie Da Producao    ${dia}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${SERIE_PRODUCAO_SCHEMA}
    Length Should Be    ${r.json()}[pontos]    30
    Should Be Equal As Integers    ${r.json()}[pontos][-1][atendimentos]    1
    ${mes}=    Create Dictionary    unidadeId=${unidade}
    ${m}=    GET Serie Da Producao    ${mes}
    Length Should Be    ${m.json()}[pontos]    12
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Producao Assistencial - Serie Com Pontos Demais - Status Code 400
    [Documentation]    The daily series goes up to 90 points.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    granularidade=DIA    pontos=91
    ${r}=    GET Serie Da Producao    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Producao Assistencial - Ranking - Status Code 200
    [Documentation]    The fresh unit is in the ranking with its count.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${nome}=    Seed Unidade Com Atendimento Concluido
    ${r}=    GET Producao Por Unidade
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Array Schema    ${r.json()}    ${PRODUCAO_UNIDADE_SCHEMA}
    ${daUnidade}=    Evaluate    [u["atendimentos"] for u in $r.json() if u["unidadeId"] == $unidade]
    Should Be Equal As Integers    ${daUnidade}[0]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Producao Assistencial - Exportacao - Status Code 200
    [Documentation]    The summary comes as CSV, with no patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${nome}=    Seed Unidade Com Atendimento Concluido
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Exportacao Da Producao    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Start With    ${r.headers}[Content-Type]    text/csv
    ${texto}=    Evaluate    $r.content.decode("utf-8")
    Should Contain    ${texto}    Atendimentos concluídos;1;
    Should Not Contain    ${texto}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Producao Assistencial - By HTTP Status Code
    [Documentation]    Executes the BI1 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-producao'    Producao Assistencial - Producao Da Unidade - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-periodo'    Producao Assistencial - Periodo Invertido - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '400-ano'    Producao Assistencial - Periodo Acima De Um Ano - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '404-unidade'    Producao Assistencial - Unidade Inexistente - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '200-serie'    Producao Assistencial - Serie - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-serie'    Producao Assistencial - Serie Com Pontos Demais - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-unidades'    Producao Assistencial - Ranking - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-csv'    Producao Assistencial - Exportacao - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Producao Assistencial test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
