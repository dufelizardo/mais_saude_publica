*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/emergencias/emergencias_pom.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Evento, emergência com nível e avaliação de impacto (ADR-0156).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Do Tipo Emergencia
    [Documentation]    The uuid of the type by code.
    [Arguments]    ${codigo}=INUNDACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Tipos Emergencia
    ${ids}=    Evaluate    [t["uuid"] for t in $r.json() if t["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Id Do Nivel Emergencia
    [Documentation]    The uuid of the level by order.
    [Arguments]    ${ordem}=1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Niveis Emergencia
    ${ids}=    Evaluate    [n["uuid"] for n in $r.json() if n["ordem"] == int($ordem)]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Evento Emergencial
    [Documentation]    A fresh flood event; returns the response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${tipo}=    Id Do Tipo Emergencia
    ${sufixo}=    Generate Random String    8    [NUMBERS]
    ${payload}=    Create Dictionary    tipoId=${tipo}    fonte=DEFESA_CIVIL    local=Bairro Robot    descricao=Robot rio transbordou ${sufixo}
    ${r}=    POST Evento Emergencial    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Emergencia
    [Documentation]    A fresh event turned emergency at level 1; returns the emergency response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${e}=    Seed Evento Emergencial
    ${nivel}=    Id Do Nivel Emergencia    1
    ${payload}=    Create Dictionary    nome=Enchente Robot    nivelId=${nivel}    justificativa=Rio acima da cota
    ${r}=    POST Converter Em Emergencia    ${e.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Catalogos - Status Code 200
    [Documentation]    The types with COBRADE and the levels 0 to 3.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${t}=    GET Tipos Emergencia
    Should Be Equal As Integers    ${t.status_code}    200
    ${cobrade}=    Evaluate    [x["cobrade"] for x in $t.json() if x["codigo"] == "INUNDACAO"][0]
    Should Be Equal    ${cobrade}    1.2.1.0.0
    ${n}=    GET Niveis Emergencia
    ${ordens}=    Evaluate    [x["ordem"] for x in $n.json()]
    FOR    ${o}    IN    0    1    2    3
        List Should Contain Value    ${ordens}    ${${o}}
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Evento - Status Code 201
    [Documentation]    The event is registered, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    Seed Evento Emergencial
    Validate Object Schema    ${r.json()}    ${EVENTO_EMERGENCIAL_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    REGISTRADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Evento No Futuro - Status Code 400
    [Documentation]    An event in the future is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${tipo}=    Id Do Tipo Emergencia
    ${futuro}=    Evaluate    (datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(hours=2)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    tipoId=${tipo}    fonte=GESTOR    descricao=Robot futuro    ocorridoEm=${futuro}
    ${r}=    POST Evento Emergencial    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Descarte Sem Motivo - Status Code 400
    [Documentation]    Discarding needs the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${e}=    Seed Evento Emergencial
    ${payload}=    Create Dictionary    situacao=DESCARTADO
    ${r}=    POST Situacao Evento Emergencial    ${e.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Conversao - Status Code 201
    [Documentation]    The event becomes an active emergency at the initial level.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    Seed Emergencia
    Validate Object Schema    ${r.json()}    ${EMERGENCIA_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    ATIVA
    Should Be Equal As Integers    ${r.json()}[nivelOrdem]    1
    Length Should Be    ${r.json()}[niveis]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Ja Convertido - Status Code 409
    [Documentation]    One emergency per event.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${nivel}=    Id Do Nivel Emergencia    1
    ${payload}=    Create Dictionary    nome=De novo    nivelId=${nivel}    justificativa=J
    ${r}=    POST Converter Em Emergencia    ${em.json()}[eventoId]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Mudanca De Nivel - Status Code 200
    [Documentation]    Raising the level keeps the history.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${nivel}=    Id Do Nivel Emergencia    2
    ${payload}=    Create Dictionary    nivelId=${nivel}    justificativa=Hospital atingido
    ${r}=    POST Nivel Emergencia    ${em.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Integers    ${r.json()}[nivelOrdem]    2
    Length Should Be    ${r.json()}[niveis]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Mesmo Nivel - Status Code 422
    [Documentation]    The same level is not a change.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${payload}=    Create Dictionary    nivelId=${em.json()}[nivelId]    justificativa=Nada mudou
    ${r}=    POST Nivel Emergencia    ${em.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Decreto Incompleto - Status Code 400
    [Documentation]    The decree needs the type, the number and the date.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${payload}=    Create Dictionary    decretoTipo=SITUACAO_DE_EMERGENCIA
    ${r}=    PUT Referencias Legais Emergencia    ${em.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Avaliacao De Impacto - Status Code 201
    [Documentation]    The suggestion, then a dated assessment.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${s}=    GET Sugestao Impacto    ${em.json()}[uuid]
    Should Be Equal As Integers    ${s.status_code}    200
    Validate Object Schema    ${s.json()}    ${SUGESTAO_IMPACTO_SCHEMA}
    ${payload}=    Create Dictionary    populacaoAfetada=${18000}    leitosIndisponiveis=${3}    nivelImpacto=ALTO    medicamentosCriticos=Soro de reidratação
    ${r}=    POST Avaliacao Impacto    ${em.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[ultimoImpacto]    ALTO
    Should Be Equal    ${r.json()}[avaliacoes][0][nivelImpacto]    ALTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - Encerramento - Status Code 200
    [Documentation]    Ending with the reason; afterwards, no level change.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${payload}=    Create Dictionary    motivo=Águas baixaram
    ${r}=    POST Encerrar Emergencia    ${em.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    ENCERRADA
    ${nivel}=    Id Do Nivel Emergencia    2
    ${mudanca}=    Create Dictionary    nivelId=${nivel}    justificativa=J
    ${n}=    POST Nivel Emergencia    ${em.json()}[uuid]    ${mudanca}
    Should Be Equal As Integers    ${n.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Emergencias - By HTTP Status Code
    [Documentation]    Executes the E1 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogos'    Emergencias - Catalogos - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-evento'    Emergencias - Evento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-evento-futuro'    Emergencias - Evento No Futuro - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '400-descarte-sem-motivo'    Emergencias - Descarte Sem Motivo - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-conversao'    Emergencias - Conversao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-ja-convertido'    Emergencias - Ja Convertido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '200-nivel'    Emergencias - Mudanca De Nivel - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-mesmo-nivel'    Emergencias - Mesmo Nivel - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '400-decreto-incompleto'    Emergencias - Decreto Incompleto - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-avaliacao'    Emergencias - Avaliacao De Impacto - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-encerramento'    Emergencias - Encerramento - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Emergencias test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
