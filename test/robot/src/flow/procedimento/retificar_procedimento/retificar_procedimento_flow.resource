*** Settings ***
Resource    ../../../pom/procedimento/retificar_procedimento/retificar_procedimento_pom.resource
Resource    ../create_procedimento/create_procedimento_flow.resource
Resource    ../get_procedimento_by_id/get_procedimento_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Registro clínico não é editado (ADR-0062): a retificação cria uma nova versão ligada à anterior, que
    continua existindo e passa a ser marcada como retificada. Só a versão vigente pode ser retificada.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Retificar Procedimento - Status Code 201
    [Documentation]    Seeds a fresh Procedimento, retifies it and checks the original is kept and flagged as retificado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimentoId}    ${consultaId}    ${matricula}=    Seed Um Procedimento
    ${payload}=    Build Procedimento Payload    ${consultaId}    ${matricula}    status=REALIZADO
    Set To Dictionary    ${payload}    motivoRetificacao=Valor registrado errado

    ${response}=    POST Retificar Procedimento    ${procedimentoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${original}=    Call GET Get Procedimento By Id    ${procedimentoId}
    Should Be Equal As Integers    ${original.status_code}    200
    Should Be True    ${original.json()}[retificado]
    Should Not Be Empty    ${original.json()}[retificadoPorUuid]

    ${nova}=    Call GET Get Procedimento By Id    ${original.json()}[retificadoPorUuid]
    Should Be Equal As Strings    ${nova.json()}[status]    REALIZADO
    Should Be Equal As Strings    ${nova.json()}[retificacaoDeUuid]    ${procedimentoId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Retificar Procedimento - Status Code 404
    [Documentation]    Attempts to retify a Procedimento identified by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${consultaId}    ${atendimentoId}    ${matricula}=    Seed Uma Consulta
    ${payload}=    Build Procedimento Payload    ${consultaId}    ${matricula}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    Set To Dictionary    ${payload}    motivoRetificacao=Correção

    ${response}=    POST Retificar Procedimento    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Retificar Procedimento - Status Code 422 - Versao Ja Retificada
    [Documentation]    Retifies the same Procedimento twice: the second attempt targets a version that is no longer current,
    ...    expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimentoId}    ${consultaId}    ${matricula}=    Seed Um Procedimento
    ${payload}=    Build Procedimento Payload    ${consultaId}    ${matricula}    status=REALIZADO
    Set To Dictionary    ${payload}    motivoRetificacao=Correção
    ${primeira}=    POST Retificar Procedimento    ${procedimentoId}    ${payload}
    Should Be Equal As Integers    ${primeira.status_code}    201

    ${response}=    POST Retificar Procedimento    ${procedimentoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento - Retificacao - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Retificar Procedimento test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Retificar Procedimento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Retificar Procedimento - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Retificar Procedimento - Status Code 422 - Versao Ja Retificada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Retificar Procedimento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

