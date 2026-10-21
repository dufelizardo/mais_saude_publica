*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/vigilancia/vigilancia_surto_pom.resource
Resource    ./vigilancia_contatos_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Dois Casos De Sarampo
    [Documentation]    Notifies measles for two new patients in one unit; returns the unit and the two notifications.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${ids}=    Create List
    FOR    ${i}    IN RANGE    2
        ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
        ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${sarampo}    unidadeId=${unidade}    origem=ATENDIMENTO
        ${n}=    POST Notificacao    ${payload}
        Should Be Equal As Integers    ${n.status_code}    201
        Append To List    ${ids}    ${n.json()}[uuid]
    END
    RETURN    ${unidade}    ${ids}[0]    ${ids}[1]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Abrir Surto
    [Documentation]    Opens a measles outbreak at a school in the unit with the given cases; returns the response.
    [Arguments]    ${unidade}    @{casos}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${ids}=    Create List    @{casos}
    ${payload}=    Create Dictionary    agravoId=${sarampo}    unidadeId=${unidade}    nome=Sarampo na escola    tipo=INSTITUCIONAL    tipoInstituicao=ESCOLA
    ...    nomeInstituicao=Escola Robot    inicio=${hoje}    notificacoesIds=${ids}
    ${r}=    POST Surto    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Surto - Abertura - Status Code 201
    [Documentation]    Opens an institutional outbreak with two measles cases.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${r}=    Abrir Surto    ${unidade}    ${c1}    ${c2}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${SURTO_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    SUSPEITO
    Should Be Equal As Integers    ${r.json()}[casos]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Surto - Institucional Sem Estabelecimento - Status Code 400
    [Documentation]    The institutional outbreak needs the establishment.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${payload}=    Create Dictionary    agravoId=${sarampo}    unidadeId=${unidade}    nome=Surto Robot    tipo=INSTITUCIONAL    tipoInstituicao=ESCOLA    inicio=${hoje}
    ${r}=    POST Surto    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Surto - Caso Em Outro Surto - Status Code 409
    [Documentation]    A case belongs to one outbreak at a time.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${r}=    Abrir Surto    ${unidade}    ${c1}    ${c2}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r2}=    Abrir Surto    ${unidade}    ${c1}
    Should Be Equal As Integers    ${r2.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Surto - Agravo Diferente - Status Code 422
    [Documentation]    The outbreak only takes cases of its agravo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${r}=    Abrir Surto    ${unidade}    ${c1}
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    ${ids}=    Create List    ${n.json()}[uuid]
    ${casos}=    Create Dictionary    notificacoesIds=${ids}
    ${i}=    POST Casos Surto    ${r.json()}[uuid]    ${casos}
    Should Be Equal As Integers    ${i.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Surto - Retirar Caso - Status Code 200
    [Documentation]    A case leaves the outbreak; the notification stays.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${r}=    Abrir Surto    ${unidade}    ${c1}    ${c2}
    ${d}=    DELETE Caso Surto    ${r.json()}[uuid]    ${c2}
    Should Be Equal As Integers    ${d.status_code}    200
    Should Be Equal As Integers    ${d.json()}[casos]    1
    ${n}=    GET Notificacao    ${c2}
    Should Be Equal    ${n.json()}[surtoId]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Surto - Ciclo E Encerramento - Status Code 200
    [Documentation]    Close needs confirmed or discarded, the conclusion and no open measure.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${r}=    Abrir Surto    ${unidade}    ${c1}    ${c2}
    ${surto}=    Set Variable    ${r.json()}[uuid]
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${medida}=    Create Dictionary    tipo=BLOQUEIO_VACINAL    descricao=Vacinar a escola    prazo=${ontem}
    ${m}=    POST Medida Surto    ${surto}    ${medida}
    Should Be Equal As Integers    ${m.status_code}    201
    Validate Object Schema    ${m.json()}    ${MEDIDA_CONTROLE_SCHEMA}
    Should Be True    ${m.json()}[vencida]
    ${encerrar}=    Create Dictionary    situacao=ENCERRADO    conclusao=Fim
    ${e1}=    POST Situacao Surto    ${surto}    ${encerrar}
    Should Be Equal As Integers    ${e1.status_code}    422
    ${investigar}=    Create Dictionary    situacao=EM_INVESTIGACAO
    POST Situacao Surto    ${surto}    ${investigar}
    ${confirmar}=    Create Dictionary    situacao=CONFIRMADO
    ${c}=    POST Situacao Surto    ${surto}    ${confirmar}
    Should Be Equal    ${c.json()}[situacao]    CONFIRMADO
    ${e2}=    POST Situacao Surto    ${surto}    ${encerrar}
    Should Be Equal As Integers    ${e2.status_code}    422
    ${concluir}=    Create Dictionary    tipo=BLOQUEIO_VACINAL    descricao=Vacinar a escola    situacao=CONCLUIDA    resultado=312 doses
    ${p}=    PATCH Medida    ${m.json()}[uuid]    ${concluir}
    Should Be Equal As Integers    ${p.status_code}    200
    ${e3}=    POST Situacao Surto    ${surto}    ${encerrar}
    Should Be Equal As Integers    ${e3.status_code}    200
    Should Be Equal    ${e3.json()}[situacao]    ENCERRADO
    ${g}=    GET Surto    ${surto}
    ${titulos}=    Evaluate    [e["titulo"] for e in $g.json()["linhaDoTempo"]]
    Should Contain    ${titulos}    Surto aberto
    Should Contain    ${titulos}    Situação
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Medida - Concluir Sem Resultado - Status Code 400
    [Documentation]    Concluding a measure needs the result.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${r}=    Abrir Surto    ${unidade}    ${c1}
    ${medida}=    Create Dictionary    tipo=ISOLAMENTO    descricao=Isolamento
    ${m}=    POST Medida Surto    ${r.json()}[uuid]    ${medida}
    ${concluir}=    Create Dictionary    tipo=ISOLAMENTO    descricao=Isolamento    situacao=CONCLUIDA
    ${p}=    PATCH Medida    ${m.json()}[uuid]    ${concluir}
    Should Be Equal As Integers    ${p.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Medida - Do Caso - Status Code 201
    [Documentation]    A control measure of a single case, without an outbreak.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${medida}=    Create Dictionary    tipo=BLOQUEIO_VACINAL    descricao=Bloqueio vacinal no quarteirão
    ${m}=    POST Medida Caso    ${c1}    ${medida}
    Should Be Equal As Integers    ${m.status_code}    201
    Should Be Equal    ${m.json()}[situacao]    PLANEJADA
    ${n}=    GET Notificacao    ${c1}
    Length Should Be    ${n.json()}[medidas]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Surtos - Lista - Status Code 200
    [Documentation]    The list brings the outbreak and the indicators.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${r}=    Abrir Surto    ${unidade}    ${c1}
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${l}=    GET Surtos    ${params}
    Should Be Equal As Integers    ${l.status_code}    200
    Should Be Equal As Integers    ${l.json()}[abertos]    1
    Validate Object Schema    ${l.json()}[lista][0]    ${SURTO_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Surto - Mapa - Status Code 200
    [Documentation]    The map of the outbreak brings only its cases.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${r}=    Abrir Surto    ${unidade}    ${c1}    ${c2}
    ${params}=    Create Dictionary    surtoId=${r.json()}[uuid]
    ${m}=    GET Mapa Vigilancia    ${params}
    Should Be Equal As Integers    ${m.status_code}    200
    Should Be Equal As Integers    ${m.json()}[total]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Surto - Inexistente - Status Code 404
    [Documentation]    Reading an outbreak that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${r}=    GET Surto    ${uuid}
    Should Be Equal As Integers    ${r.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Surto - By HTTP Status Code
    [Documentation]    Executes the V2b scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-surto'    Surto - Abertura - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-institucional'    Surto - Institucional Sem Estabelecimento - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '409-outro-surto'    Surto - Caso Em Outro Surto - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422-agravo'    Surto - Agravo Diferente - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-retirar'    Surto - Retirar Caso - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-ciclo'    Surto - Ciclo E Encerramento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-concluir'    Medida - Concluir Sem Resultado - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-medida-caso'    Medida - Do Caso - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-surtos'    Surtos - Lista - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-mapa'    Surto - Mapa - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404-surto'    Surto - Inexistente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Vigilancia Surto test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
