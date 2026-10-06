*** Settings ***
Library     DateTime
Library     Collections
Resource    ../../../pom/escalas/escala/escala_pom.resource
Resource    ../../equipes/equipe/equipe_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A unidade e o profissional lotado vêm dos seeds de Equipes; os turnos ficam na semana que vem.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Proxima Segunda
    [Documentation]    Returns next Monday as YYYY-MM-DD.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=7 - datetime.date.today().weekday())).isoformat()    modules=datetime
    RETURN    ${seg}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Payload De Turno
    [Documentation]    Builds a shift payload.
    [Arguments]    ${unidadeId}    ${matricula}    ${funcao}    ${tipo}    ${data}    ${inicio}    ${fim}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    tipo=${tipo}    data=${data}    inicio=${inicio}    fim=${fim}
    IF    '${matricula}' != '${EMPTY}'    Set To Dictionary    ${payload}    profissionalMatricula=${matricula}
    IF    '${funcao}' != '${EMPTY}'    Set To Dictionary    ${payload}    funcao=${funcao}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - Turno - Status Code 201
    [Documentation]    Creates a morning shift for a posted professional and sees it in the week.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Payload De Turno    ${unidadeId}    ${matricula}    ${EMPTY}    MANHA    ${seg}    07:00    13:00
    ${response}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${TURNO_ESCALA_SCHEMA}
    Should Be Equal As Numbers    ${response.json()}[horas]    6
    ${params}=    Create Dictionary    unidadeId=${unidadeId}    semana=${seg}
    ${semana}=    GET Escala Semana    ${params}
    Should Be Equal As Integers    ${semana.status_code}    200
    Validate Object Schema    ${semana.json()}    ${ESCALA_SEMANA_SCHEMA}
    Should Be Equal As Integers    ${semana.json()}[turnos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - Turno - Status Code 400 - Vaga Sem Funcao
    [Documentation]    Creates an open slot without the function.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${payload}=    Payload De Turno    ${unidadeId}    ${EMPTY}    ${EMPTY}    MANHA    ${seg}    07:00    13:00
    ${response}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - Turno - Status Code 422 - Plantao Fora De Unidade 24h
    [Documentation]    Creates a 12-hour duty in a unit that is not open 24 hours.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Payload De Turno    ${unidadeId}    ${matricula}    ${EMPTY}    PLANTAO_12H    ${seg}    07:00    19:00
    ${response}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - Turno - Status Code 409 - Sobreposicao
    [Documentation]    Creates two overlapping shifts for the same professional.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${primeiro}=    Payload De Turno    ${unidadeId}    ${matricula}    ${EMPTY}    MANHA    ${seg}    07:00    13:00
    ${response}=    POST Create Turno    ${primeiro}
    Should Be Equal As Integers    ${response.status_code}    201
    ${segundo}=    Payload De Turno    ${unidadeId}    ${matricula}    ${EMPTY}    TARDE    ${seg}    12:00    16:00
    ${conflito}=    POST Create Turno    ${segundo}
    Should Be Equal As Integers    ${conflito.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - Designar - Status Code 200
    [Documentation]    Fills an open slot with a posted professional.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Payload De Turno    ${unidadeId}    ${EMPTY}    MEDICO    MANHA    ${seg}    07:00    13:00
    ${vaga}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${vaga.status_code}    201
    Should Be True    ${vaga.json()}[vaga]
    ${designacao}=    Create Dictionary    profissionalMatricula=${matricula}
    ${response}=    POST Designar Turno    ${vaga.json()}[uuid]    ${designacao}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[profissionalMatricula]    ${matricula}
    Should Not Be True    ${response.json()}[vaga]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - Copiar Semana - Status Code 200
    [Documentation]    Copies a week with one shift to the following week.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Payload De Turno    ${unidadeId}    ${matricula}    ${EMPTY}    MANHA    ${seg}    07:00    13:00
    ${response}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    ${destino}=    Add Time To Date    ${seg}    7 days    result_format=%Y-%m-%d    date_format=%Y-%m-%d
    ${copia}=    Create Dictionary    unidadeId=${unidadeId}    origem=${seg}    destino=${destino}
    ${resultado}=    POST Copiar Semana    ${copia}
    Should Be Equal As Integers    ${resultado.status_code}    200
    Should Be Equal As Integers    ${resultado.json()}[copiados]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - Remover - Status Code 204
    [Documentation]    Removes a future shift; removing it again gives 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${payload}=    Payload De Turno    ${unidadeId}    ${EMPTY}    ACS    MANHA    ${seg}    07:00    11:00
    ${vaga}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${vaga.status_code}    201
    ${response}=    DELETE Turno    ${vaga.json()}[uuid]
    Should Be Equal As Integers    ${response.status_code}    204
    ${denovo}=    DELETE Turno    ${vaga.json()}[uuid]
    Should Be Equal As Integers    ${denovo.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - Intervalo - Status Code 201
    [Documentation]    Creates an 08:00-17:00 day shift with a 1h break; it counts 8 worked hours.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Payload De Turno    ${unidadeId}    ${matricula}    ${EMPTY}    DIURNO    ${seg}    08:00    17:00
    Set To Dictionary    ${payload}    intervaloMinutos=${60}
    ${response}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Should Be Equal As Numbers    ${response.json()}[horas]    8
    Should Be Equal As Integers    ${response.json()}[intervaloMinutos]    60
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - Modelo - Status Code 200
    [Documentation]    Applies the 40h model (8h + 1h): five shifts, 40 worked hours.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    profissionalMatricula=${matricula}    modelo=H40_8H    semana=${seg}
    ${response}=    POST Aplicar Modelo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal As Integers    ${response.json()}[criados]    5
    Should Be Equal As Numbers    ${response.json()}[horas]    40
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Escala - By HTTP Status Code
    [Documentation]    Executes the schedule scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Escala - Turno - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-vaga'    Escala - Turno - Status Code 400 - Vaga Sem Funcao
    ...    ELSE IF    '${meuHTTP}' == '422-plantao'    Escala - Turno - Status Code 422 - Plantao Fora De Unidade 24h
    ...    ELSE IF    '${meuHTTP}' == '409'    Escala - Turno - Status Code 409 - Sobreposicao
    ...    ELSE IF    '${meuHTTP}' == '200-designar'    Escala - Designar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-copiar'    Escala - Copiar Semana - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '204'    Escala - Remover - Status Code 204
    ...    ELSE IF    '${meuHTTP}' == '201-intervalo'    Escala - Intervalo - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-modelo'    Escala - Modelo - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Escala test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
