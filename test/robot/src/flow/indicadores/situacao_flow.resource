*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/indicadores/situacao_pom.resource
Resource    ../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../escalas/escala/escala_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base vem pelas APIs dos outros domínios: o atendimento aberto agora (a chegada é gravada) e a vaga aberta na escala.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Unidade Com Paciente Aguardando
    [Documentation]    Seeds a fresh unit with a walk-in atendimento in progress (no triage yet); returns the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${agora}=    Evaluate    datetime.datetime.now().replace(second=0, microsecond=0).isoformat()    modules=datetime
    ${payload}=    Build Atendimento Payload    ${paciente}    ${matricula}    ${unidade}    status=EM_ANDAMENTO    dataHora=${agora}
    ${r}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Unidade Com Turno Sem Cobertura
    [Documentation]    Seeds a fresh unit with an open slot (no professional) starting in the next hour; returns the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${inicio}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(hours=1)).replace(minute=0, second=0, microsecond=0)    modules=datetime
    ${fim}=    Evaluate    $inicio + datetime.timedelta(hours=1)    modules=datetime
    ${payload}=    Payload De Turno    ${unidade}    ${EMPTY}    MEDICO    MANHA    ${inicio.date().isoformat()}    ${inicio.strftime('%H:%M')}    ${fim.strftime('%H:%M')}
    ${r}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Operacional - Agora - Status Code 200
    [Documentation]    A walk-in atendimento in a fresh unit is waiting for triage, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed Unidade Com Paciente Aguardando
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Situacao Agora    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${SITUACAO_AGORA_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[emAtendimento]    1
    Should Be Equal As Integers    ${r.json()}[aguardandoTriagem]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Operacional - Unidade Inexistente - Status Code 404
    [Documentation]    Unknown unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${params}=    Create Dictionary    unidadeId=${id}
    ${r}=    GET Situacao Agora    ${params}
    Should Be Equal As Integers    ${r.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Operacional - Espera - Status Code 200
    [Documentation]    The arrival is recorded and counted in the period.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed Unidade Com Paciente Aguardando
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Espera Do Atendimento    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${ESPERA_ATENDIMENTO_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[comChegada]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Operacional - Espera Periodo Invertido - Status Code 400
    [Documentation]    The start cannot be after the end.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    datetime.date.today()    modules=datetime
    ${ontem}=    Evaluate    $hoje - datetime.timedelta(days=1)    modules=datetime
    ${params}=    Create Dictionary    de=${hoje}    ate=${ontem}
    ${r}=    GET Espera Do Atendimento    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Operacional - Ocupacao - Status Code 200
    [Documentation]    A fresh unit has no schedule slots.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Ocupacao Das Agendas    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${OCUPACAO_AGENDA_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[ofertadas]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Operacional - Ocupacao Horizonte Longo - Status Code 400
    [Documentation]    The horizon goes up to 31 days.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    dias=40
    ${r}=    GET Ocupacao Das Agendas    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Operacional - Escala Sem Cobertura - Status Code 200
    [Documentation]    An open slot in the next hour is an occurrence and an alert.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed Unidade Com Turno Sem Cobertura
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Ocorrencias Da Escala    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Array Schema    ${r.json()}    ${OCORRENCIA_ESCALA_SCHEMA}
    Should Be Equal    ${r.json()}[0][situacao]    SEM_COBERTURA
    ${s}=    GET Situacao Agora    ${params}
    ${escala}=    Evaluate    [a for a in $s.json()["alertas"] if a["tipo"] == "ESCALA"]
    Length Should Be    ${escala}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Operacional - By HTTP Status Code
    [Documentation]    Executes the BI2 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-agora'    Situacao Operacional - Agora - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404-unidade'    Situacao Operacional - Unidade Inexistente - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '200-espera'    Situacao Operacional - Espera - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-espera'    Situacao Operacional - Espera Periodo Invertido - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-ocupacao'    Situacao Operacional - Ocupacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-ocupacao'    Situacao Operacional - Ocupacao Horizonte Longo - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-escala'    Situacao Operacional - Escala Sem Cobertura - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Situacao Operacional test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
