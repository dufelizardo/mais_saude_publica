*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/imunizacao/vacinacao_pom.resource
Resource    ../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base vem pelas APIs dos outros domínios: unidade, profissional e paciente (adulto, nascido em 1990).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Vacinacao
    [Documentation]    Seeds a fresh unit, a professional and an adult patient; returns a dictionary with them.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${dados}=    Create Dictionary    unidade=${unidade}    matricula=${matricula}    paciente=${paciente}    nome=${nome}
    RETURN    ${dados}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Id Do Imunobiologico
    [Documentation]    Returns the uuid of the immunobiological with the code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Imunobiologicos
    ${ids}=    Evaluate    [i["uuid"] for i in $r.json() if i["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Dose Aplicada
    [Documentation]    An applied dose payload for the seeded data.
    [Arguments]    ${dados}    ${codigo}    ${dose}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${imuno}=    Id Do Imunobiologico    ${codigo}
    ${payload}=    Create Dictionary    pacienteId=${dados}[paciente]    imunobiologicoId=${imuno}    dose=${dose}    unidadeId=${dados}[unidade]
    ...    origem=APLICADA    profissionalMatricula=${dados}[matricula]    lote=LT2026A
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Catalogo E Calendario - Status Code 200
    [Documentation]    The initial load has the PNI immunobiologicals and the calendar.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Imunobiologicos
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Array Schema    ${r.json()}    ${IMUNOBIOLOGICO_SCHEMA}
    ${codigos}=    Evaluate    [i["codigo"] for i in $r.json()]
    List Should Contain Value    ${codigos}    BCG
    ${c}=    GET Calendario Vacinal
    Should Be Equal As Integers    ${c.status_code}    200
    Validate Array Schema    ${c.json()}    ${ESQUEMA_VACINAL_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Registro Aplicada - Status Code 201
    [Documentation]    An applied dose in a fresh unit, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    DT    REFORCO
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${REGISTRO_VACINACAO_SCHEMA}
    Should Be Equal    ${r.json()}[origem]    APLICADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Dose Repetida - Status Code 409
    [Documentation]    The same valid dose is not repeated.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    HEPATITE_B    PRIMEIRA
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r2}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r2.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Sem Lote - Status Code 400
    [Documentation]    The applied dose needs the batch.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    DT    REFORCO
    Set To Dictionary    ${payload}    lote=${EMPTY}
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Fora Do Calendario - Status Code 422
    [Documentation]    BCG for an adult is out of the calendar without a justification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    BCG    UNICA
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    Set To Dictionary    ${payload}    justificativa=Exposição ocupacional    estrategia=ESPECIAL
    ${r2}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r2.status_code}    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Transcrita Da Caderneta - Status Code 201
    [Documentation]    A dose transcribed from the card needs the date, not the batch.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${imuno}=    Id Do Imunobiologico    FEBRE_AMARELA
    ${payload}=    Create Dictionary    pacienteId=${dados}[paciente]    imunobiologicoId=${imuno}    dose=UNICA    unidadeId=${dados}[unidade]
    ...    origem=TRANSCRITA    localOrigem=Posto de outro município
    ${sem}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${sem.status_code}    400
    Set To Dictionary    ${payload}    aplicadaEm=2010-03-15T09:00:00
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[origem]    TRANSCRITA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Caderneta - Status Code 200
    [Documentation]    The vaccination card with the registered doses, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    DT    REFORCO
    POST Registro Vacinacao    ${payload}
    ${r}=    GET Caderneta Vacinacao    ${dados}[paciente]
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${CADERNETA_VACINACAO_SCHEMA}
    Length Should Be    ${r.json()}[doses]    1
    Should Be True    ${r.json()}[situacaoDisponivel]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Anulacao - Status Code 200
    [Documentation]    A voided dose leaves the card.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    DT    REFORCO
    ${r}=    POST Registro Vacinacao    ${payload}
    ${motivo}=    Create Dictionary    motivo=Paciente errado
    ${a}=    POST Anulacao Vacinacao    ${r.json()}[uuid]    ${motivo}
    Should Be Equal As Integers    ${a.status_code}    200
    ${c}=    GET Caderneta Vacinacao    ${dados}[paciente]
    Length Should Be    ${c.json()}[doses]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Sala De Vacina Do Dia - Status Code 200
    [Documentation]    The doses applied in the unit today, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    DT    REFORCO
    POST Registro Vacinacao    ${payload}
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]
    ${r}=    GET Vacinacao Do Dia    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${VACINACAO_DO_DIA_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[total]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - Atrasados - Status Code 200
    [Documentation]    The overdue list of the unit, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]
    ${r}=    GET Vacinacao Atrasados    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Array Schema    ${r.json()}    ${ATRASADO_VACINACAO_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacinacao - By HTTP Status Code
    [Documentation]    Executes the I1 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogo'    Vacinacao - Catalogo E Calendario - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-registro'    Vacinacao - Registro Aplicada - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-repetida'    Vacinacao - Dose Repetida - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '400-sem-lote'    Vacinacao - Sem Lote - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422-fora'    Vacinacao - Fora Do Calendario - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-transcrita'    Vacinacao - Transcrita Da Caderneta - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-caderneta'    Vacinacao - Caderneta - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-anulacao'    Vacinacao - Anulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-dia'    Vacinacao - Sala De Vacina Do Dia - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-atrasados'    Vacinacao - Atrasados - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Vacinacao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
