*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/indicadores/indicadores_rede_pom.resource
Resource    ../programas/acompanhamento/acompanhamento_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base vem pelas APIs de Programas: a unidade nova oferece o HiperDia e tem um paciente inscrito.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed HiperDia Com Inscrito
    [Documentation]    Offers HiperDia in a fresh UBS and enrolls one patient; returns the program, the unit and the patient name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET Programas
    ${ids}=    Evaluate    [p["uuid"] for p in $lista.json() if p["codigo"] == "HIPERDIA"]
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    ${part}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    POST Participacao Programa    ${ids}[0]    ${part}
    Should Be Equal As Integers    ${r.status_code}    200
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${insc}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}
    ${i}=    POST Inscrever Paciente No Programa    ${ids}[0]    ${insc}
    Should Be Equal As Integers    ${i.status_code}    201
    RETURN    ${ids}[0]    ${unidade}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Da Rede - Programas - Status Code 200
    [Documentation]    HiperDia in a fresh unit with one enrolled patient, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Com Inscrito
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Programas Na Rede    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${PROGRAMAS_NA_REDE_SCHEMA}
    ${h}=    Evaluate    [p for p in $r.json()["programas"] if p["codigo"] == "HIPERDIA"][0]
    Should Be Equal As Integers    ${h}[inscritosAtivos]    1
    Should Be Equal As Integers    ${h}[unidades]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Da Rede - Periodo Invertido - Status Code 400
    [Documentation]    The start cannot be after the end.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    datetime.date.today()    modules=datetime
    ${ontem}=    Evaluate    $hoje - datetime.timedelta(days=1)    modules=datetime
    ${params}=    Create Dictionary    de=${hoje}    ate=${ontem}
    ${r}=    GET Programas Na Rede    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Da Rede - Programa Por Unidade - Status Code 200
    [Documentation]    The fresh unit is listed in the program by unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Com Inscrito
    ${r}=    GET Programa Por Unidade    ${programa}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${PROGRAMA_POR_UNIDADE_SCHEMA}
    ${linha}=    Evaluate    [u for u in $r.json()["unidades"] if u["unidadeId"] == $unidade]
    Length Should Be    ${linha}    1
    Should Be Equal As Integers    ${linha}[0][inscritosAtivos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Da Rede - Programa Inexistente - Status Code 404
    [Documentation]    Unknown program.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${r}=    GET Programa Por Unidade    ${id}
    Should Be Equal As Integers    ${r.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Da Rede - Vigilancia - Status Code 200
    [Documentation]    Surveillance with twelve epidemiological weeks, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Vigilancia Na Rede
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${VIGILANCIA_NA_REDE_SCHEMA}
    Length Should Be    ${r.json()}[semanas]    12
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Da Rede - Exportacao - Status Code 200
    [Documentation]    The summary comes as CSV, with no patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Com Inscrito
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Exportacao Da Rede    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Start With    ${r.headers}[Content-Type]    text/csv
    ${texto}=    Evaluate    $r.content.decode("utf-8")
    Should Contain    ${texto}    HiperDia
    Should Not Contain    ${texto}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Da Rede - By HTTP Status Code
    [Documentation]    Executes the BI3 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-programas'    Indicadores Da Rede - Programas - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-programas'    Indicadores Da Rede - Periodo Invertido - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-por-unidade'    Indicadores Da Rede - Programa Por Unidade - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404-programa'    Indicadores Da Rede - Programa Inexistente - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '200-vigilancia'    Indicadores Da Rede - Vigilancia - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-csv'    Indicadores Da Rede - Exportacao - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Indicadores Da Rede test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
